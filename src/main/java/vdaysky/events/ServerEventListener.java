package vdaysky.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import vdaysky.util.TextInput;
import vdaysky.gui.GUI;

public class ServerEventListener implements Listener {

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        GUI.onInventoryClick(event);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        GUI.onInventoryClose(event);
    }

    @EventHandler
    private void onInventoryDrag(InventoryDragEvent event) {
        GUI.onInventoryDrag(event);
    }

    @EventHandler
    private void onAsyncChat(AsyncPlayerChatEvent e) {
        TextInput.onChat(e);
    }
}
