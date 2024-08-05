package vdaysky.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import vdaysky.util.TriFunction;

import java.util.HashMap;
import java.util.function.Function;

public class GUI {

    private static final HashMap<String, GUI> GUI_REGISTRY = new HashMap<>();

    public enum Size {
        ONE_ROW(9),
        TWO_ROWS(18),
        THREE_ROWS(27),
        FOUR_ROWS(36),
        FIVE_ROWS(45),
        SIX_ROWS(54);

        private final int size;

        Size(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    /** Reactively set state, updating all UIs depending on it
     *
     * @param key state key to set
     * @param value new value
     * */
    public static void setState(String key, Object value) {
        for (var display : OPEN_INVENTORIES.values()) {
            // prevent unnecessary GUI updates
            if (display.get(key, new Object()) == value) {
                continue;
            }
            display.set(key, value);
        }
    }

    public static class Builder {

        private Size size = Size.ONE_ROW;
        private String title = "";
        private final Function<GUIDisplay, ItemComponent>[][] computedItems = new Function[9][6];
        private String id = null;
        private ItemComponent fill = Icon.EMPTY;

        /** GUI id to reference dynamically
         *
         * @param id unique identifier
         * @see GUI#getById(String)
         * @return builder
         * */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /** Set GUI size. Default is one row
         *
         * @param size size of GUI
         * @return builder
         * */
        public Builder size(Size size) {
            this.size = size;
            return this;
        }

        /** Set fill item for empty slots. Defaults to empty.
         *
         * @param item item to fill empty slots
         * @see Icon#EMPTY
         * @return builder
         * */
        public Builder fill(ItemComponent item) {
            this.fill = item;
            return this;
        }

        /** Set GUI title. Defaults to empty.
         *
         * @param title title of GUI
         * @return builder
         * */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /** Set item at given coordinates
         *
         * @param x X coordinate
         * @param y Y coordinate
         * @param icon item to set
         * @return builder
         * */
        public Builder item(int x, int y, ItemComponent icon) {
            computedItems[x][y] = (s) -> icon;
            return this;
        }

        /** Set item at given coordinates
         *
         * @param x X coordinate
         * @param y Y coordinate
         * @param render function to render item on demand
         * @return builder
         * */
        public Builder item(int x, int y, Function<GUIDisplay, ItemComponent> render) {
            return this.section(x, y, x + 1, y + 1, (x_, y_, state) -> render.apply(state));
        }

        /** Define a GUI section with render function
         * @param x0 absolute X coordinate of section start (inclusive)
         * @param y0 absolute Y coordinate of section start (inclusive)
         * @param x1 absolute X coordinate of section end (exclusive)
         * @param y1 absolute Y coordinate of section end (exclusive)
         * @param render function to render each cell of section on demand
         * @return builder
         * */
        public Builder section(
                int x0, int y0, int x1, int y1,
                TriFunction<Integer, Integer, GUIDisplay, ItemComponent> render
        ) {
            for (int x = x0; x < x1; ++x) {
                for (int y = y0; y < y1; ++y) {
                    int x_ = x;
                    int y_ = y;
                    computedItems[x][y] = (state) -> render.apply(x_ - x0, y_ - y0, state);
                }
            }
            return this;
        }

        /** Define a GUI section with render function
         * @param section section to render
         * @return builder
         * */
        public Builder section(
                Section section
        ) {
            for (int x = section.getX0(); x < section.getX1(); ++x) {
                for (int y = section.getY0(); y < section.getY1(); ++y) {
                    int x_ = x;
                    int y_ = y;
                    computedItems[x][y] = (state) -> section.render(x_ - section.getX0(), y_ - section.getY0(), state);
                }
            }
            return this;
        }

        public GUI build() {
            return new GUI(size, title, id, computedItems, fill);
        }

    }

    private final static HashMap<Inventory, GUIDisplay> OPEN_INVENTORIES = new HashMap<>();

    private final Size size;
    private final String title;
    private final Function<GUIDisplay, ? extends ItemComponent>[][] computedItems;
    private final ItemComponent fill;

    public static GUI getById(String id) {
        return GUI_REGISTRY.get(id);
    }

    public GUI(
            Size size,
            String title,
            String id,
            Function<GUIDisplay, ? extends ItemComponent>[][] computedItems,
            ItemComponent fill
    ) {
        if (id != null) {
            GUI_REGISTRY.put(id, this);
        }

        this.size = size;
        this.title = title;
        this.computedItems = computedItems;
        this.fill = fill;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Size getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }

    public ItemComponent getFill() {
        return fill;
    }

    public Function<GUIDisplay, ? extends ItemComponent>[][] getItems() {
        return computedItems;
    }

    public GUIDisplay displayFor(Player player) {
        var display = new GUIDisplay(this, player);
        Inventory inventory = display.prepareInventory(player);
        display.setInventory(inventory);
        OPEN_INVENTORIES.put(inventory, display);
        player.openInventory(inventory);
        return display;
    }

    public static void onInventoryClose(InventoryCloseEvent e) {
        OPEN_INVENTORIES.remove(e.getInventory());
    }

    public static void onInventoryClick(InventoryClickEvent e) {
        Inventory clickedInventory = e.getClickedInventory();
        if (!OPEN_INVENTORIES.containsKey(clickedInventory)) {
            return;
        }

        e.setCancelled(true);

        int x = e.getSlot() % 9;
        int y = e.getSlot() / 9;

        GUIDisplay display = OPEN_INVENTORIES.get(clickedInventory);
        display.onClick(x, y, e);
    }

    public static void onInventoryDrag(InventoryDragEvent e) {
        Inventory clickedInventory = e.getView().getTopInventory();
        if (!OPEN_INVENTORIES.containsKey(clickedInventory)) {
            return;
        }

        e.setCancelled(true);
    }
}
