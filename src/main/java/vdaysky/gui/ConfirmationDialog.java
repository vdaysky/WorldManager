package vdaysky.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/** GUI that presents user with yes/no options */
public class ConfirmationDialog {

    String title;
    String message;
    Runnable onYes;
    Runnable onNo;

    /**
     * @param title title of the dialog (inventory)
     * @param message message to display to the user on paper item
     * @param onYes action to perform when user clicks "Yes"
     * @param onNo action to perform when user clicks "No"
     * */
    public ConfirmationDialog(String title, String message, Runnable onYes, Runnable onNo) {
        this.message = message;
        this.title = title;
        this.onYes = onYes;
        this.onNo = onNo;
    }

    public void displayFor(Player player) {
        //   012345678
        // 0 .........
        // 1 ....P....
        // 2 .........
        // 3 ..E...R..
        // 4 .........
        var gui = GUI.builder()
                .title(title)
                .size(GUI.Size.FIVE_ROWS)
                .item(4, 1, Icon.builder().mat(Material.PAPER).name(message).build())
                .item(2, 3, Icon.builder().mat(Material.EMERALD_BLOCK).name("" + ChatColor.GREEN + ChatColor.BOLD + "Yes").onClickC((e, s)-> onYes.run()).build())
                .item(6, 3, Icon.builder().mat(Material.REDSTONE_BLOCK).name("" + ChatColor.RED + ChatColor.BOLD + "No").onClickC((e, s)-> onNo.run()).build())
                .build();

        gui.displayFor(player);

    }

}
