package vdaysky.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import vdaysky.gui.Icon;
import vdaysky.world.WorldData;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

public class Utils {

    public static void deleteDirectory(File directory) {
        if (!directory.isDirectory()){
            directory.delete();
            return;
        }
        Arrays.stream(directory.listFiles()).forEach(Utils::deleteDirectory);
        directory.delete();
    }

    private static String plural(long count, String word) {
        return count + " " + (count == 1 ? word : word + "s");
    }

    public static String formatTimestamp(Long timestamp) {
        if (timestamp == null) {
            return "Never";
        }
        long elapsed = System.currentTimeMillis() - timestamp;

        if (elapsed < 1000) {
            return "Just now";
        }
        if (elapsed < 60000) {
            return plural(elapsed / 1000, "second") + " ago";
        }
        if (elapsed < 3600000) {
            return plural(elapsed / 60000, "minute") + " ago";
        }
        if (elapsed < 86400000) {
            return plural(elapsed / 3600000, "hour") + " ago";
        }
        return plural(elapsed / 86400000, "day") + " ago";
    }

    public static Icon.Builder formatLoaded(WorldData data, Icon.Builder builder, boolean canUnload) {

        return builder
            .name(ChatColor.DARK_GREEN + data.getName())
            .lore(
                ChatColor.GREEN + "Click to visit",
                "",
                ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + data.getOwner().getName(),
                ChatColor.GRAY + "Last Visited: " + ChatColor.YELLOW + formatTimestamp(data.getLastVisitedAt()),
                ChatColor.GRAY + "Created: " + ChatColor.YELLOW + formatTimestamp(data.getCreatedAt()),
                ChatColor.GRAY + "Type: " + ChatColor.YELLOW + "SkyWars",
                "",
                canUnload ? ChatColor.GRAY + "Shift-click to unload" : ""
            );
    }

    public static Icon.Builder formatUnloaded(WorldData data, Icon.Builder builder, boolean canDelete) {
        String unloadedTime = formatTimestamp(data.getUnloadedAt());
        String unloadedBy = (data.getUnloadedBy() != null ? data.getUnloadedBy().getName() : "Server");

        return builder
                .name(ChatColor.GRAY + data.getName())
                .lore(
                    ChatColor.GREEN + "Click to load",
                    "",
                    ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + data.getOwner().getName(),
                    ChatColor.GRAY + "Last Visited: " + ChatColor.YELLOW + formatTimestamp(data.getLastVisitedAt()),
                    ChatColor.GRAY + "Created: " + ChatColor.YELLOW + formatTimestamp(data.getCreatedAt()),
                    ChatColor.GRAY + "Unloaded: " + ChatColor.YELLOW + unloadedTime + " by " + unloadedBy,
                    ChatColor.GRAY + "Type: " + ChatColor.YELLOW + "SkyWars",
                    "",
                    canDelete ? ChatColor.GRAY + "Shift-click to delete" : ""
                );
    }

    public static Object formatMemory(Long memoryUsage) {
        if (memoryUsage == null) {
            return "Unknown";
        }

        double megabyte = 1024 * 1024;
        double gigabyte = megabyte * 1024;

        if (memoryUsage > gigabyte) {
            double gigabytes = memoryUsage / gigabyte;
            return Math.round(gigabytes * 100.0) / 100.0 + "GB";
        }

        double megabytes = memoryUsage / megabyte;
        return Math.round(megabytes * 100.0) / 100.0 + "MB";
    }

    public static Logger getLogger() {
        return Bukkit.getPluginManager().getPlugin("WorldManager").getLogger();
    }
}
