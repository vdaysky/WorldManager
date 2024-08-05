package vdaysky.util;

public class Point2 {

    public final int x;
    public final int y;

    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int hashCode() {
        return x * 9 + y;
    }

}
