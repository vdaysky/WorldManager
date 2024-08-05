package vdaysky.world;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import vdaysky.util.Utils;

import java.io.File;
import java.io.IOException;

import static vdaysky.util.Utils.getLogger;

public class WorldData {

    static File loadedWorldsDir = Bukkit.getWorldContainer();
    static File unloadedWorldsDir = WorldOperations.getUnloadedWorldsFolder();

    OfflinePlayer owner;
    OfflinePlayer unloadedBy;
    String name;
    Long lastVisitedAt;
    Long createdAt;
    Long unloadedAt;
    String filename;

    private WorldData(String filename, OfflinePlayer owner, String name, Long lastVisitedAt, Long createdAt, OfflinePlayer unloadedBy, Long unloadedAt) {
        this.owner = owner;
        this.name = name;
        this.lastVisitedAt = lastVisitedAt;
        this.createdAt = createdAt;
        this.unloadedBy = unloadedBy;
        this.unloadedAt = unloadedAt;
        this.filename = filename;
    }

    public static void createBlank(String filename, String worldDisplayName, Player player) {
        File f =  getConfigFile(filename, false);
        try {
            f.createNewFile();
        } catch (IOException e) {
            getLogger().warning("Failed to create new config: " + e.getMessage());
            throw new RuntimeException(e);
        }
        new WorldData(filename, player, worldDisplayName, System.currentTimeMillis(), System.currentTimeMillis(), null, null).save(false);
    }

    private static File getConfigFile(String filename, boolean isUnloaded) {
        File f = isUnloaded ? new File(unloadedWorldsDir, filename) : new File(loadedWorldsDir, filename);
        return new File(f, WorldOperations.WORLD_MANAGER_FILENAME);
    }

    public void save(boolean isUnloaded) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("world.owner", owner);
        config.set("world.name", name);
        config.set("world.lastVisitedAt", lastVisitedAt);
        config.set("world.createdAt", createdAt);
        config.set("world.unloadedBy", unloadedBy);
        config.set("world.unloadedAt", unloadedAt);
        File file = getConfigFile(filename, isUnloaded);
        try {
            config.save(file);
        } catch (Exception e) {
            getLogger().warning("Failed to save config to %s: %s".formatted(file.getAbsolutePath(), e.getMessage()));
            e.printStackTrace();
        }
    }

    public static WorldData load(String worldName, boolean isUnloaded) {
        File file = getConfigFile(worldName, isUnloaded);
        if (!file.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        OfflinePlayer owner = config.getOfflinePlayer("world.owner");
        OfflinePlayer unloadedBy = config.getOfflinePlayer("world.unloadedBy");
        long lastVisitedAt = config.getLong("world.lastVisitedAt");
        long createdAt = config.getLong("world.createdAt");
        long unloadedAt = config.getLong("world.unloadedAt");
        String name = config.getString("world.name");

        return new WorldData(
                worldName,
                owner,
                name,
                lastVisitedAt,
                createdAt,
                unloadedBy,
                unloadedAt
        );

    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Long getLastVisitedAt() {
        return lastVisitedAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public OfflinePlayer getUnloadedBy() {
        return unloadedBy;
    }

    public Long getUnloadedAt() {
        return unloadedAt;
    }

    public void setUnloadedBy(Player player) {
        unloadedBy = player;
    }

    public void setUnloadedAt(long l) {
        unloadedAt = l;
    }

    public void setLastVisit(long l) {
        lastVisitedAt = l;
    }
}
