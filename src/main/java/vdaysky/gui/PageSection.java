package vdaysky.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import vdaysky.util.SoundEffect;

import java.util.List;

/** GUI section that implements pagination
 * @param <T> Type of items to display
 * */
public abstract class PageSection<T> extends Section {

    public PageSection(int x0, int y0, int x1, int y1) {
        super(x0, y0, x1, y1);
    }

    /** Full list of items to be shown in GUI */
    public abstract List<T> getItems();

    /** Render a single item into displayable component
     *
     * @param item Item to render
     * @param x relative X coordinate of item in GUI Section
     * @param y relative Y coordinate of item in GUI Section
     * @param state Reference to actual open inventory by a player
     *
     * @return icon to display
     * */
    public abstract ItemComponent renderItem(T item, int x, int y, GUIDisplay state);

    /** Create a back button that works with pagination
     *
     * @param _state GUIDisplay state
     * @return Icon of back button
     * */
    public Icon backButton(GUIDisplay _state) {
        return Icon.builder().name(ChatColor.GRAY + "< Back").mat(Material.ARROW).onClick(
                (e, state) -> {
                    if (state.getInt("page", 0) == 0) {
                        SoundEffect.deny(state.getPlayer());
                        return;
                    }

                    SoundEffect.success(state.getPlayer());
                    state.set("page", state.getInt("page", 0) - 1);
                }
        ).build();
    }

    public int pageCapacity() {
        return (getX1() - getX0()) * (getY1() - getY0());
    }

    public int pageCount() {
        return (int) Math.ceil(getItems().size() / (float) pageCapacity());
    }

    /** Create a next button that works with pagination
     *
     * @param _state GUIDisplay state
     * @return Icon of next button
     * */
    public Icon nextButton(GUIDisplay _state) {
        return Icon.builder().name(ChatColor.GRAY + "Next >").mat(Material.ARROW).onClick(
                (e, state) -> {
                    if (state.getInt("page", 0) >= pageCount() - 1) {
                        SoundEffect.deny(state.getPlayer());
                        return;
                    }

                    SoundEffect.success(state.getPlayer());
                    state.set("page", state.getInt("page", 0) + 1);
                }
        ).build();
    }

    /** Create an icon that displays current page number
     *
     * @param state GUIDisplay state
     * @return Icon of page number
     * */
    public Icon pageNumberIcon(GUIDisplay state) {
        return Icon.builder()
                .name(ChatColor.GRAY + "Page " + ChatColor.YELLOW + (state.getInt("page", 0) + 1) + ChatColor.GRAY + "/" + ChatColor.YELLOW + Math.max(1, pageCount()))
                .mat(Material.PAPER)
                .amount(state.getInt("page", 0) + 1)
                .build();
    }

    @Override
    public ItemComponent render(int x, int y, GUIDisplay state) {

        var pageCapacity = pageCapacity();
        var page = state.get("page", 0);
        var items = getItems();
        var pageData = items.subList(page * pageCapacity, Math.min(items.size(), (page + 1) * pageCapacity));

        var index = y * 9 + x;

        if (index >= pageData.size()) {
            return Icon.EMPTY;
        }

        var item = pageData.get(index);
        return renderItem(item, x, y, state);
    }
}
