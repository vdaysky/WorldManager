package vdaysky.world;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import vdaysky.config.WorldManagerConfig;
import vdaysky.task.Task;
import vdaysky.util.Pair;
import vdaysky.util.Utils;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static vdaysky.util.Utils.getLogger;

public class WorldOperations {

    public final static String WORLD_MANAGER_FILENAME = "world-manager.yml";
    private final static String TEMPLATES_DIR = "templates";
    private final static String UNLOADED_DIR = "unloaded";

    private static final int WORLD_UNLOAD_CHECK_INTERVAL_TICKS = 20 * 30;

    // too lazy to implement LRU cache, this will do
    private static final LinkedList<Pair<String, WorldData>> worldUnloadOrder = new LinkedList<>();

    private static void addWorldToUnloadQueue(String worldName) {
        var data = getWorldData(worldName, false);
        var lastVisited = data.getLastVisitedAt();

        for (int i = 0; i < worldUnloadOrder.size(); i++) {
            if (worldUnloadOrder.get(i).getSecond().getLastVisitedAt() > lastVisited) {
                worldUnloadOrder.add(i, new Pair<>(worldName, data));
                return;
            }
        }
        worldUnloadOrder.add(new Pair<>(worldName, data));
    }

    private static void removeWorldFromUnloadQueue(String worldName) {
        worldUnloadOrder.removeIf(pair -> pair.getFirst().equals(worldName));
    }

    private static Runnable unloadCheckTask = null;

    public static void startUnloadCheck() {

        if (unloadCheckTask != null) {
            unloadCheckTask.run();
        }

        var config = WorldManagerConfig.getInstance();
        var tickInterval = config.getWorldUnloadCheckDelay().toTicks();

        var cancel = Task.repeat(()->{
            getLogger().info("Running world unload check...");
            if (worldUnloadOrder.isEmpty()) {
                return;
            }
            var _cfg = WorldManagerConfig.getInstance();

            var item = worldUnloadOrder.getFirst();
            var data = item.getSecond();
            var elapsedMs = System.currentTimeMillis() - data.getLastVisitedAt();
            if (elapsedMs < _cfg.getWorldUnloadDelay().toMs()) {
                return;
            }
            var world = Bukkit.getWorld(item.getFirst());

            if (world == null) {
                return;
            }

            // removes item from the queue
            WorldOperations.unloadWorld(world, null);
            getLogger().info("Unloaded world: " + world.getName());

        }, tickInterval);
        unloadCheckTask = cancel;
    }

    private static File getTemplatesFolder() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldManager");
        File dataFolder = plugin.getDataFolder();
        File templates = new File(dataFolder, TEMPLATES_DIR);
        if (!templates.exists()) {
            templates.mkdirs();
        }
        return templates;
    }

    public static File getUnloadedWorldsFolder() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldManager");
        File dataFolder = plugin.getDataFolder();
        File unloaded = new File(dataFolder, UNLOADED_DIR);
        if (!unloaded.exists()) {
            unloaded.mkdirs();
        }
        return unloaded;
    }

    private static File getWorldFolder(String worldName) {
        File worldContainer = Bukkit.getWorldContainer();
        return new File(worldContainer, worldName);
    }

    private static File getUnloadedWorldFolder(String worldName) {
        return new File(getUnloadedWorldsFolder(), worldName);
    }

    private static void copyWorldData(File source, File target){
        try {
            ArrayList<String> ignore = new ArrayList<>(Arrays.asList("uid.dat", "session.dat"));
            if(!ignore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists())
                        target.mkdirs();
                    String[] files = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyWorldData(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            getLogger().warning("Failed to copy world data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void moveWorldData(File source, File target) {
        // could be optimized with mv instead of cp
        copyWorldData(source, target);
        Utils.deleteDirectory(source);
    }

    private static World createOrLoadWorld(String worldName, String displayName, Player player) {
        // try both loaded and unloaded data.
        // if this world was loaded before server shut down,
        // it will have to be loaded here
        WorldData data = getWorldData(worldName, false);
        if (data == null) {
            data = getWorldData(worldName, true);
        }

        var actionBy = player != null ? player.getName() : "Server";

        // create the world, so we surely have a folder
        WorldCreator flatWorldCreator = new WorldCreator(worldName);
        flatWorldCreator.type(WorldType.FLAT);
        var world = Bukkit.createWorld(flatWorldCreator);

        // world is being created / imported
        if (data == null) {
            WorldData.createBlank(worldName, displayName, player);
            getLogger().info("%s is creating/importing a world %s (%s)".formatted(actionBy, worldName, displayName));
        } else { // world is being loaded from unloaded state of after server restart
            getLogger().info("%s is loading world %s (%s)".formatted(actionBy, worldName, displayName));
            data.setLastVisit(System.currentTimeMillis());
            data.save(true);
        }

        addWorldToUnloadQueue(worldName);
        getLoadedWorlds().add(world);
        getUnloadedWorlds().remove(worldName);

        return world;
    }

    public static @Nullable World importWorld(String templateWorldName, Player player) {
        File templates = getTemplatesFolder();

        UUID uuid = UUID.randomUUID();
        String newWorldName = uuid + "-" + templateWorldName;

        newWorldName = nameToSafe(newWorldName);

        File source = new File(templates, templateWorldName);
        File target = getWorldFolder(newWorldName);

        if (!source.exists()) {
            return null;
        }

        copyWorldData(source, target);
        return createOrLoadWorld(newWorldName, templateWorldName, player);

    }

    private static String nameToSafe(String name) {
        // make sure world name is valid but still recognizable
        return name.replaceAll("[^a-zA-Z0-9_-]", "-").toLowerCase();
    }

    public static World createWorld(String realName, Player player) {
        String safeName = nameToSafe(realName + System.currentTimeMillis());
        return createOrLoadWorld(safeName, realName, player);
    }

    public static World loadWorld(String worldName, Player player) {
        moveWorldData(getUnloadedWorldFolder(worldName), getWorldFolder(worldName));
        return createOrLoadWorld(worldName, null, player);
    }

    public static void deleteWorld(String worldName, Player player) {
        File worldFolder = new File(getUnloadedWorldsFolder(), worldName);

        getLogger().info("Player %s is deleting the world %s".formatted(player.getName(), worldName));
        removeWorldFromUnloadQueue(worldName);
        getUnloadedWorlds().remove(worldName);

        if (worldFolder.exists()) {
            Utils.deleteDirectory(worldFolder);
        }
    }

    public static void unloadWorld(World world, @Nullable Player player) {

        String unloadedBy = player != null ? player.getName() : "Server";

        getLogger().info("World %s is unloaded by %s".formatted(world.getName(), unloadedBy));

        Location loc = Bukkit.getWorld("world").getSpawnLocation();
        for (Player p : world.getPlayers()) {
            p.sendMessage(ChatColor.RED + "This world is being unloaded. You were moved to hub");
            p.teleport(loc);
        }

        // sync and laggy
        Bukkit.unloadWorld(world, true);

        removeWorldFromUnloadQueue(world.getName());
        getUnloadedWorlds().add(world.getName());
        getLoadedWorlds().remove(world);

        moveWorldData(world.getWorldFolder(), new File(getUnloadedWorldsFolder(), world.getName()));

        var worldData = getWorldData(world);

        worldData.setUnloadedBy(player);
        worldData.setUnloadedAt(System.currentTimeMillis());
        worldData.save(true);
    }

    private static List<String> getManagedWorlds() {
        return Arrays
            .stream(Objects.requireNonNull(Bukkit.getWorldContainer().listFiles()))
            .filter(f -> f.isDirectory() && new File(f, WORLD_MANAGER_FILENAME).exists())
            .collect(
                ArrayList::new,
                (list, file) -> list.add(file.getName()),
                ArrayList::addAll
            );
    }

    public static void loadManagedWorlds() {
        for (String worldName : getManagedWorlds()) {
            createOrLoadWorld(worldName, null, null);
        }
    }

    private static final HashMap<String, WorldData> WORLD_DATA_CACHE = new HashMap<>();

    public static @Nullable WorldData getWorldData(String worldName, boolean unloaded) {

        if (!WORLD_DATA_CACHE.containsKey(worldName)) {
            var data = WorldData.load(worldName, unloaded);
            if (data == null) {
                return null;
            }
            WORLD_DATA_CACHE.put(worldName, data);
        }

        return WORLD_DATA_CACHE.get(worldName);
    }

    public static WorldData getWorldData(World w) {
        return getWorldData(w.getName(), false);
    }

    public static WorldData getOfflineWorldData(String worldName) {
        return getWorldData(worldName, true);
    }

    public static void markLastVisit(World world) {
        var data = getWorldData(world.getName(), false);
        if (data == null) {
            return;
        }
        data.setLastVisit(System.currentTimeMillis());
        data.save(false);

        // move to the end of the queue
        removeWorldFromUnloadQueue(world.getName());
        addWorldToUnloadQueue(world.getName());
    }

    private static LinkedList<String> UNLOADED_WORLDS = null;

    // on boot loaded worlds are empty.
    // they will be loaded automatically
    private static LinkedList<World> LOADED_WORLDS = new LinkedList<>();

    public static List<String> getUnloadedWorlds() {
        if (UNLOADED_WORLDS == null) {
            UNLOADED_WORLDS = new LinkedList<>();
            UNLOADED_WORLDS.addAll(Arrays.stream(getUnloadedWorldsFolder().listFiles()).map(File::getName).toList());
        }
        return UNLOADED_WORLDS;
    }

    public static List<World> getLoadedWorlds() {
        if (LOADED_WORLDS == null) {
            LOADED_WORLDS = Bukkit.getWorlds().stream()
                    .filter(w -> getWorldData(w) != null)
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return LOADED_WORLDS;
    }

    public static void invalidateCaches() {
        LOADED_WORLDS = null;
        UNLOADED_WORLDS = null;
        WORLD_DATA_CACHE.clear();
        startUnloadCheck(); // task interval might have changed
    }
}
