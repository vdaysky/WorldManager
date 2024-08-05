package vdaysky.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface ItemComponent {

    /** Handle click on the icon
     *
     * @param e InventoryClickEvent that triggered this handler
     * @param display GUI state
     * @return whether click was successful (i.e. return false on missing permissions) */
    boolean onClick(InventoryClickEvent e, GUIDisplay display);

    /** Render this item for given player
     *
     * @param p player to render for
     * @return rendered itemstack ready to be placed in GUI */
    ItemStack render(Player p);

}
