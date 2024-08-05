package vdaysky.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Icon implements ItemComponent {

    public static Icon EMPTY = Icon.builder().mat(Material.AIR).build();

    public static class Builder {

        private Material material;
        private int amount = 1;
        private String name = null;
        private String[] lore = null;
        private BiFunction<InventoryClickEvent, GUIDisplay, Boolean>  onClick = (e, s) -> null;

        public Builder mat(Material material) {
            this.material = material;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder lore(String... lore) {
            this.lore = Arrays.stream(lore).filter(Objects::nonNull).toArray(String[]::new);
            return this;
        }

        public Builder onClick(BiConsumer<InventoryClickEvent, GUIDisplay> onClick) {
            return onClick((e, s) -> {
                onClick.accept(e, s);
                return true;
            });
        }

        public Builder onClickC(BiConsumer<InventoryClickEvent, GUIDisplay> onClick) {
            return onClick(onClick);
        }

        public Builder onClick(BiFunction<InventoryClickEvent, GUIDisplay, Boolean> onClick) {
            this.onClick = onClick;
            return this;
        }

        public Icon build() {
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            if (name != null) {
                meta.setDisplayName(name);
            }

            if (lore != null) {
                meta.setLore(Arrays.asList(lore));
            }

            item.setItemMeta(meta);
            return new Icon(item, onClick);
        }

    }

    private final ItemStack item;
    private final BiFunction<InventoryClickEvent, GUIDisplay, Boolean> onClick;

    public Icon(ItemStack item, BiFunction<InventoryClickEvent, GUIDisplay, Boolean> onClick) {
        this.item = item;
        this.onClick = onClick;
    }

    public ItemStack render(Player player) {
        return item;
    }

    public boolean onClick(InventoryClickEvent event, GUIDisplay state) {
        var wasClicked = onClick.apply(event, state);
        if (wasClicked == null) {
            return false;
        }
        return wasClicked;
    }

    public static Builder builder() {
        return new Builder();
    }

}
