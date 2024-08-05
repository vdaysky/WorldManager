package vdaysky.worldmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import vdaysky.task.Task;
import vdaysky.events.ServerEventListener;
import vdaysky.gui.*;
import vdaysky.world.WorldOperations;

import java.util.*;

public final class WorldManager extends JavaPlugin {

    private static final LinkedList<Long> lastTicks = new LinkedList<>();

    private void startTpsTask() {
        Task.repeat(() -> {
            long thisTick = System.currentTimeMillis();
            lastTicks.addLast(thisTick);
            while (lastTicks.size() > 30 * 20) {
                lastTicks.removeFirst();
            }
            long elapsed = thisTick - lastTicks.getFirst();
            double mspt = (double) lastTicks.size() / elapsed;
            int newTps = Math.min((int) (20 / mspt), 20);
            GUI.setState("TPS", newTps);

        }, 1);
    }

    private void startRamTask() {
        Task.repeat(() -> {
            var rt = Runtime.getRuntime();
            GUI.setState("memoryTotal", rt.maxMemory());
            GUI.setState("memoryFree", rt.freeMemory());
        }, 20);
    }


    @Override
    public void onEnable() {
        Listener listener = new ServerEventListener();
        getServer().getPluginManager().registerEvents(listener, this);

        WorldOperations.loadManagedWorlds();
        WorldOperations.startUnloadCheck();

        startTpsTask();
        startRamTask();
    }

    private final HashMap<String, PermissionAttachment> attachments = new HashMap<>();

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            String[] args
    ) {

        // temporary placeholder for whatever permission system is used
        if (command.getName().equals("permission")) {
            if (args.length != 1) {
                return false;
            }

            String perm = args[0];
            if (perm.startsWith("-")) {
                perm = perm.substring(1);
                var attachment = attachments.get(perm);
                if (attachment != null) {
                    sender.removeAttachment(attachment);
                }
                return true;
            }
            var attachment = sender.addAttachment(this, perm, true);
            attachments.put(perm, attachment);

            return true;
        }

        if (command.getName().equals("worldmanager")) {
            if (!sender.hasPermission("worldmanager.command")) {
                sender.sendMessage("You do not have permission to use this command.");
            }

            if (sender instanceof Player player) {
                // actual entrypoint
                GUIs.MAIN_GUI.displayFor(player);
                return true;
            }

            sender.sendMessage("Only players can use this command.");
        }
        return false;
    }
}
