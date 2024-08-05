package vdaysky.gui;


/** Fixed section of GUI to be rendered */
public abstract class Section {

    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;

    public Section(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    /** Render given cell of this section
     *
     * @param x relative X coordinate of cell in GUI Section
     * @param y relative Y coordinate of cell in GUI Section
     * @param state GUIDisplay state
     *
     * @return icon to display
     * */
    public abstract ItemComponent render(int x, int y, GUIDisplay state);

    public int getX0() {
        return x0;
    }

    public int getY0() {
        return y0;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int toAbsoluteX(int relX) {
        return relX + x0;
    }

    public int toAbsoluteY(int relY) {
        return relY + y0;
    }
}
