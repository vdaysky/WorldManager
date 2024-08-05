package vdaysky.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import vdaysky.util.Point2;
import vdaysky.util.SoundEffect;

import java.util.*;

public class GUIDisplay {

    private final GUI gui;
    private final ItemComponent[][] items;
    private final HashMap<String, Object> state = new HashMap<>();
    private final Player owner;
    private Inventory handle;

    // reactivity magic
    private boolean isCapturingStateCalls = false;
    private final List<String> stateCalls = new ArrayList<>();
    private final HashMap<String, HashSet<Point2>> stateDependencies = new HashMap<>();

    public GUIDisplay(GUI gui, Player owner) {
        this.gui = gui;
        this.owner = owner;
        items = new ItemComponent[9][6];
    }

    public void setInventory(Inventory inventory) {
        this.handle = inventory;
    }

    /** Reactively set state of this GUI, updating cells that depend on it */
    public void set(String key, Object value) {
        state.put(key, value);

        var stateDeps = stateDependencies.get(key);
        if (stateDeps == null) {
            return;
        }

        // update any cells that depend on this value
        for (var dep : stateDeps) {
            // we will not be re-capturing dependencies during re-render because that is too complicated,
            // and I am not paid for any of this
            updateCell(dep.x, dep.y);
        }
    }

    /** Get state value */
    public <T> T get(String key, T initial) {
        if (isCapturingStateCalls) {
            stateCalls.add(key);
        }
        if (!state.containsKey(key)) {
            state.put(key, initial);
        }
        return (T) state.get(key);
    }

    public Integer getInt(String key, Integer initial) {
        return get(key, initial);
    }

    private @NotNull ItemComponent renderCell(int x, int y) {

        var items = gui.getItems();

        if (items[x][y] == null) {
            return gui.getFill();
        }

        var icon = items[x][y].apply(this);
        if (icon != null) {
            return icon;
        }

        return gui.getFill();
    }

    private void updateCell(int x, int y) {
        var icon = renderCell(x, y);
        setCell(x, y, icon);
    }

    public void setCell(int x, int y, ItemComponent icon) {
        items[x][y] = icon;
        var item = icon.render(owner);
        var index = x + y * 9;
        this.handle.setItem(index, item);
    }

    /** Create and fill inventory just for this player */
    public Inventory prepareInventory(Player player) {

        // let's capture all state dependencies during this initial render
        isCapturingStateCalls = true;
        Inventory inventory = Bukkit.createInventory(player, gui.getSize().getSize(), gui.getTitle());

        // since we will be rendering entire UI, state calls can be cleared altogether.
        // (they shouldn't've been set yet but who knows)
        stateDependencies.clear();

        for (int x = 0; x < 9; ++x) {
            for (int y = 0; y < gui.getSize().getSize() / 9; ++y) {

                int index = x + y * 9;

                stateCalls.clear();
                var icon = renderCell(x, y);
                var item = icon.render(player);
                for (String state : stateCalls) {
                    var depsOfState = stateDependencies.getOrDefault(state, new HashSet<>());
                    depsOfState.add(new Point2(x, y));
                    stateDependencies.put(state, depsOfState);
                }

                inventory.setItem(index, item);
                items[x][y] = icon;
            }
        }
        isCapturingStateCalls = false;
        return inventory;
    }

    public void onClick(int x, int y, InventoryClickEvent e) {
        var icon = items[x][y];
        if (icon == null) {
            return;
        }
        if (icon.onClick(e, this)) {
            SoundEffect.click(getPlayer());
        }
    }

    public Player getPlayer() {
        return owner;
    }
}
