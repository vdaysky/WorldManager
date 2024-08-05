package vdaysky.util;

public class Time {

    private final long ms;

    private Time(long ms) {
        this.ms = ms;
    }

    public static Time fromMs(long ms) {
        return new Time(ms);
    }

    public static Time fromSeconds(long seconds) {
        return new Time(seconds * 1000);
    }

    public static Time fromMinutes(long minutes) {
        return new Time(minutes * 60 * 1000);
    }

    public static Time fromHours(long hours) {
        return new Time(hours * 60 * 60 * 1000);
    }

    public static Time fromDays(long days) {
        return new Time(days * 24 * 60 * 60 * 1000);
    }

    public static Time fromTicks(long ticks) {
        return new Time(ticks * 50);
    }

    public long toMs() {
        return ms;
    }

    public long toSeconds() {
        return ms / 1000;
    }

    public long toTicks() {
        return ms / 50;
    }

}
