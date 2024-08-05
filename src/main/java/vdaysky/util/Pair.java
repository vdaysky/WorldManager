package vdaysky.util;

public class Pair<L, R> {

    L left;
    R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getFirst() {
        return left;
    }

    public R getSecond() {
        return right;
    }
}
