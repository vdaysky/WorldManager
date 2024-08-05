package vdaysky.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/** Icon that is hidden unless user has respective permissions */
public class PermissibleIcon implements ItemComponent {

    Icon icon;
    String permission;
    Icon fallback;

    public PermissibleIcon(Icon icon, String permission, Icon fallback) {
        this.icon = icon;
        this.permission = permission;
        this.fallback = fallback;
    }

    public static class Builder {
        private Icon icon = Icon.EMPTY;
        private String permission = "*";
        private Icon fallback = Icon.EMPTY;

        public Builder icon(Icon icon) {
            this.icon = icon;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder fallback(Icon fallback) {
            this.fallback = fallback;
            return this;
        }

        public PermissibleIcon build() {
            return new PermissibleIcon(icon, permission, fallback);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private Icon getEffectiveIcon(Player p) {
        if (p.hasPermission(permission)) {
            return icon;
        } else {
            return fallback;
        }
    }

    @Override
    public boolean onClick(InventoryClickEvent e, GUIDisplay state) {
        return getEffectiveIcon((Player) e.getWhoClicked()).onClick(e, state);
    }

    @Override
    public ItemStack render(Player p) {
        return getEffectiveIcon(p).render(p);
    }
}
