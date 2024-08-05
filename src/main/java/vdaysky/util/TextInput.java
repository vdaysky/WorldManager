package vdaysky.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import vdaysky.task.Task;

import java.util.HashMap;
import java.util.function.Consumer;

public class TextInput {

    private static final HashMap<Player, TextInput> TEXT_REQUESTS = new HashMap<>();

    private final Consumer<String> onComplete;
    private final int maxLength;

    private TextInput(int maxLength, Consumer<String> onComplete) {
        this.onComplete = onComplete;
        this.maxLength = maxLength;
    }

    /** Request user to enter some text through chat
     *
     * @param player player to request text from
     * @param maxLength max acceptable length of the text
     * @param onComplete action to run when user submits valid string
     * */
    public static void requestText(Player player, int maxLength, Consumer<String> onComplete) {
        TEXT_REQUESTS.put(player, new TextInput(maxLength, onComplete));
    }

    public static void onChat(AsyncPlayerChatEvent e) {
        var player = e.getPlayer();

        if (!TEXT_REQUESTS.containsKey(player)) {
            return;
        }
        e.setCancelled(true);

        var input = TEXT_REQUESTS.get(player);
        var content = e.getMessage();

        if (content.equals("!cancel")) {
            TEXT_REQUESTS.remove(player);
            player.sendMessage(ChatColor.GRAY + "Input cancelled.");
            return;
        }

        if (content.length() > input.maxLength) {
            int diff = content.length() - input.maxLength;
            player.sendMessage(
                ChatColor.RED + "Text length limit exceeded by " +
                ChatColor.YELLOW + diff + ChatColor.RED +
                " characters. Please try again, or type !cancel to cancel input."
            );
            return;
        }

        TEXT_REQUESTS.remove(player);
        Task.runSoon(() -> input.onComplete.accept(content));
    }
}
