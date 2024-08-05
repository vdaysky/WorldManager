package vdaysky.worldmanager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import vdaysky.config.WorldManagerConfig;
import vdaysky.gui.*;
import vdaysky.util.SoundEffect;
import vdaysky.util.TextInput;
import vdaysky.util.Utils;
import vdaysky.world.WorldOperations;

import java.util.List;

import static vdaysky.util.Utils.getLogger;

/** All the GUIs! Seriously, is this React? */
public class GUIs {

    // region Icons
    /** Back to main GUI button */
    private static final Icon BACK_TO_MAIN = Icon.builder()
            .mat(Material.BARRIER)
            .name(ChatColor.RED + "Back to Main Menu")
            .onClick((event, s) -> {
                Player player = (Player) event.getWhoClicked();
                GUI.getById("MAIN_GUI").displayFor(player);
            }).build();

    /** World creation placeholder */
    private static final Icon CREATE_WORLD_ICON = Icon.builder()
            .mat(Material.EMERALD)
            .name(ChatColor.GREEN + "Create World")
            .lore(
                    "",
                    ChatColor.GRAY + "Create a brand new world.",
                    ChatColor.GRAY + "You will be prompted to enter the name in chat."
            )
            .onClick((event, s) -> {

                Player player = s.getPlayer();
                player.closeInventory();

                SoundEffect.ping(player);
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "> Please type name of the world in chat:");

                TextInput.requestText(player, 32, worldName -> {
                    SoundEffect.success(player);
                    player.sendMessage(ChatColor.GRAY + worldName + " created. Teleporting...");
                    World world = WorldOperations.createWorld(worldName, player);
                    player.teleport(world.getSpawnLocation());
                });

            }).build();

    /** Link to unloaded worlds page */
    private static final Icon UNLOADED_ICON = Icon.builder()
            .mat(Material.DARK_OAK_DOOR)
            .name(ChatColor.GRAY + "Unloaded Worlds")
            .lore(
                    "",
                    ChatColor.GRAY + "Unloaded worlds that don't take up memory",
                    ChatColor.GRAY + "You can load them again at any time"
            )
            .onClickC((e, s) -> GUI.getById("UNLOADED_GUI").displayFor((Player) e.getWhoClicked()))
            .build();

    /** Close button to close active inventory */
    private static final Icon CLOSE_BUTTON = Icon.builder()
            .mat(Material.BARRIER)
            .name(ChatColor.RED + "Close")
            .onClick((e, s) -> {
                Player player = (Player) e.getWhoClicked();
                player.closeInventory();
            })
            .build();

    /** Fancy border tile */
    private static final Icon BORDER = Icon.builder().mat(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();

    /** Import world button, prompts user to enter world name and proceeds with creation */
    private static final Icon IMPORT_WORLD_BUTTON = Icon.builder()
            .mat(Material.DIAMOND)
            .name(ChatColor.AQUA + "Import World")
            .lore(
                    "",
                    ChatColor.GRAY + "Import a world from the list",
                    ChatColor.GRAY + "You will be prompted to enter the name in chat."
            )
            .onClick((e, s)-> {
                Player player = (Player) e.getWhoClicked();

                player.closeInventory();
                SoundEffect.ping(player);
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "> Please type name of the world to import:");

                TextInput.requestText(player, 32, worldName -> {
                    World world = WorldOperations.importWorld(worldName, player);

                    if (world == null) {
                        SoundEffect.deny(player);
                        player.sendMessage(ChatColor.RED + "World '"+ ChatColor.YELLOW + worldName + ChatColor.RED + "' not found");
                        GUI.getById("MAIN_GUI").displayFor(player);
                        return;
                    }

                    SoundEffect.success(player);
                    player.sendMessage(ChatColor.GRAY + "World '" + ChatColor.YELLOW+ worldName + ChatColor.GRAY + "' imported. Teleporting...");
                    player.teleport(world.getSpawnLocation());
                });
            })
            .build();

    /** Create world button with permissions applied */
    private static final PermissibleIcon CREATE_WORLD_ICON_P = PermissibleIcon.builder()
            .icon(CREATE_WORLD_ICON)
            .permission("worldmanager.create")
            .fallback(BORDER).build();

    /** Unloaded worlds button with permissions applied */
    private static final PermissibleIcon UNLOADED_ICON_P = PermissibleIcon.builder()
            .icon(UNLOADED_ICON)
            .permission("worldmanager.load")
            .fallback(BORDER).build();

    /** Import world button with permissions applied  */
    private static final PermissibleIcon IMPORT_WORLD_BUTTON_P = PermissibleIcon.builder()
            .icon(IMPORT_WORLD_BUTTON)
            .permission("worldmanager.import")
            .fallback(BORDER)
            .build();
    // endregion


    // region Sections

    /** Unloaded worlds section with pagination */
    private static final PageSection<String> UNLOADED_WORLD_SECTION = new PageSection<>(1, 1, 8, 5) {
        @Override
        public List<String> getItems() {
            return WorldOperations.getUnloadedWorlds();
        }

        @Override
        public ItemComponent renderItem(String unloadedWorldName, int x, int y, GUIDisplay state) {
            var worldData = WorldOperations.getOfflineWorldData(unloadedWorldName);
            var hasDeletePerm = state.getPlayer().hasPermission("worldmanager.delete");

            return Utils.formatUnloaded(worldData, Icon.builder(), hasDeletePerm)
                    .mat(Material.GRASS_BLOCK)
                    .onClick((e, s) -> {
                        Player player = (Player) e.getWhoClicked();

                        if (e.isShiftClick()) {

                            if (!hasDeletePerm) {
                                SoundEffect.deny(player);
                                return;
                            }

                            var dialog = new ConfirmationDialog(
                                    "Delete World?",
                                    ChatColor.GRAY + "Are you sure you want to delete " + ChatColor.YELLOW + worldData.getName() + ChatColor.GRAY + "?",
                                    () -> {
                                        var newState = UNLOADED_GUI.displayFor(player);

                                        // make sure same page is opened
                                        newState.set("page", state.get("page", 0));

                                        newState.setCell(
                                                toAbsoluteX(x),
                                                toAbsoluteY(y),
                                                Icon.builder()
                                                        .mat(Material.REDSTONE_BLOCK)
                                                        .name(ChatColor.GREEN + "You deleted this world")
                                                        .build()
                                        );

                                        WorldOperations.deleteWorld(unloadedWorldName, player);
                                    },
                                    () -> {
                                        var newState = UNLOADED_GUI.displayFor(player);
                                        // make sure same page is opened
                                        newState.set("page", state.get("page", 0));
                                    }
                            );
                            dialog.displayFor(player);
                            return;
                        }

                        WorldOperations.loadWorld(unloadedWorldName, player);
                        state.setCell(
                                UNLOADED_WORLD_SECTION.toAbsoluteX(x),
                                UNLOADED_WORLD_SECTION.toAbsoluteY(y),
                                Icon.builder()
                                        .mat(Material.PAPER)
                                        .name(ChatColor.GREEN + "You just loaded this world")
                                        .lore(
                                                ChatColor.GRAY + "It was moved to main tab"
                                        )
                                        .build()
                        );
                    }).build();
        }
    };

    /** Loaded worlds section with pagination */
    private static final PageSection<World> LOADED_WORLD_SECTION = new PageSection<>(1, 1, 8, 5) {

        @Override
        public List<World> getItems() {
            return WorldOperations.getLoadedWorlds();
        }

        @Override
        public ItemComponent renderItem(World world, int x, int y, GUIDisplay state) {
            var worldData = WorldOperations.getWorldData(world);
            var hasUnloadPerm = state.getPlayer().hasPermission("worldmanager.unload");

            return Utils
                    .formatLoaded(worldData, Icon.builder(), hasUnloadPerm)
                    .mat(Material.GRASS_BLOCK)
                    .onClick((e, s) -> {
                        Player player = (Player) e.getWhoClicked();

                        if (e.isShiftClick()) {
                            if (!hasUnloadPerm) {
                                SoundEffect.deny(player);
                                return false;
                            }

                            state.setCell(
                                    LOADED_WORLD_SECTION.toAbsoluteX(x),
                                    LOADED_WORLD_SECTION.toAbsoluteY(y),
                                    Icon.builder()
                                            .mat(Material.PAPER)
                                            .name(ChatColor.GREEN + "You unloaded this world")
                                            .lore(
                                                    ChatColor.GRAY + "It was moved to unloaded tab"
                                            )
                                            .build()
                            );
                            WorldOperations.unloadWorld(world, player);
                            return true;
                        }

                        player.teleport(world.getSpawnLocation());
                        var data = WorldOperations.getWorldData(world);

                        player.sendMessage("");
                        player.sendMessage(ChatColor.GRAY + "Welcome to " + ChatColor.YELLOW + data.getName());
                        player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + data.getOwner().getName());
                        player.sendMessage(ChatColor.GRAY + "Last Visited: " + ChatColor.YELLOW + Utils.formatTimestamp(data.getLastVisitedAt()));
                        player.sendMessage("");

                        WorldOperations.markLastVisit(world);
                        return true;

                    }).build();
        }
    };
    // endregion


    // region GUIs

    // ........X
    // ._______.
    // ._______.
    // ._______.
    // ._______.
    // <...P...>
    public static final GUI UNLOADED_GUI = GUI.builder()
            .title("Unloaded Worlds")
            .id("UNLOADED_GUI")
            .fill(BORDER)
            .size(GUI.Size.SIX_ROWS)
            .item(8, 0, BACK_TO_MAIN)
            .section(UNLOADED_WORLD_SECTION)
            .item(0, 5, UNLOADED_WORLD_SECTION::backButton)
            .item(4, 5, UNLOADED_WORLD_SECTION::pageNumberIcon)
            .item(8, 5, UNLOADED_WORLD_SECTION::nextButton)
            .build();

    // CI..M..UX
    // ._______.
    // ._______.
    // ._______.
    // ._______.
    // <...P...>
    public static final GUI MAIN_GUI = GUI.builder()
            .id("MAIN_GUI")
            .title("World Manager")
            .size(GUI.Size.SIX_ROWS)
            .fill(BORDER)
            .item(0, 0, CREATE_WORLD_ICON_P)
            .item(1, 0, IMPORT_WORLD_BUTTON_P)
            .item(4, 0, (state) -> {

                var tps = state.get("TPS", -1);
                var memTotal = state.get("memoryTotal", 0L);
                var memFree = state.get("memoryFree", 0L);

                var loadedWorldCount = WorldOperations.getLoadedWorlds().size();
                var unloadedWorldCount = WorldOperations.getUnloadedWorlds().size();
                var hasAdminPerm = state.getPlayer().hasPermission("worldmanager.admin");

                return Icon.builder()
                        .mat(Material.CRAFTING_TABLE)
                        .name("" + ChatColor.GOLD + ChatColor.BOLD + "World Manager")
                        .lore(
                                "",
                                ChatColor.GRAY + "TPS: " + (tps == -1 ? ChatColor.GRAY + "Estimating..." : "" + ChatColor.YELLOW + tps),
                                ChatColor.GRAY + "Memory (free/total): " + ChatColor.YELLOW + Utils.formatMemory(memFree) + " / " + Utils.formatMemory(memTotal),
                                ChatColor.GRAY + "Worlds Loaded: " + ChatColor.YELLOW + loadedWorldCount,
                                ChatColor.GRAY + "Worlds Unloaded: " + ChatColor.YELLOW + unloadedWorldCount,
                                hasAdminPerm ? "" : null,
                                hasAdminPerm ? ChatColor.GRAY + "Click to invalidate caches" : null
                        )
                        .onClick((e, s) -> {
                            if (hasAdminPerm) {
                                getLogger().info("%s invalidated caches".formatted(s.getPlayer().getName()));
                                WorldManagerConfig.unload();
                                WorldOperations.invalidateCaches();
                                return true;
                            }
                            return false;
                        } )
                        .build();
            })
            .item(7, 0, UNLOADED_ICON_P)
            .item(8, 0, CLOSE_BUTTON)
            .section(LOADED_WORLD_SECTION)
            .item(0, 5, LOADED_WORLD_SECTION::backButton)
            .item(4, 5, LOADED_WORLD_SECTION::pageNumberIcon)
            .item(8, 5, LOADED_WORLD_SECTION::nextButton)
            .build();
    // endregion
}
