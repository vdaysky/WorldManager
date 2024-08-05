package vdaysky.util;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffect {
    public static void deny(Player player) {
        player.playNote(player.getLocation(), Instrument.BASS_GUITAR, Note.flat(1, Note.Tone.A));
    }
    public static void ping(Player player) {
        player.playNote(player.getLocation(), Instrument.CHIME, Note.natural(1, Note.Tone.C));
    }

    public static void success(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    public static void click(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
    }
}
