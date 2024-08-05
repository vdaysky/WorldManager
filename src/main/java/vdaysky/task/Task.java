package vdaysky.task;

import org.bukkit.Bukkit;

public class Task {

    public static void runSoon(Runnable task) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(
            Bukkit.getPluginManager().getPlugin("WorldManager"),
            task,
            0
        );
    }

    public static Runnable repeat(Runnable task, long delay) {
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
            Bukkit.getPluginManager().getPlugin("WorldManager"),
            task,
            0,
            delay
        );
        return () -> Bukkit.getScheduler().cancelTask(taskId);
    }
}
