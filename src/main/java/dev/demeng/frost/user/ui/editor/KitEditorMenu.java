package dev.demeng.frost.user.ui.editor;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.editor.buttons.ArmorButton;
import dev.demeng.frost.user.ui.editor.buttons.CancelButton;
import dev.demeng.frost.user.ui.editor.buttons.ClearButton;
import dev.demeng.frost.user.ui.editor.buttons.KitButton;
import dev.demeng.frost.user.ui.editor.buttons.LoadButton;
import dev.demeng.frost.user.ui.editor.buttons.RefillableItemButton;
import dev.demeng.frost.user.ui.editor.buttons.SaveButton;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitEditorMenu extends Menu {

  private final Frost plugin;

  private final int[] BORDER_POSITIONS = new int[]{1, 3, 4, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17,
      19, 20, 21, 22, 23, 24, 25, 26, 28, 29, 30, 31, 32, 33, 34, 35, 37, 38, 39, 40, 41, 42, 43,
      44, 46, 47, 48, 49, 50, 51, 52, 53};
  private final Button BORDER_BUTTON = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15,
      " ");

  public KitEditorMenu(Frost plugin) {
    this.plugin = plugin;
  }

  @Override
  public String getTitle(Player player) {
    return "Editing " + plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId()).getSelectedKit().getName();
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    for (int border : BORDER_POSITIONS) {
      buttons.put(border, BORDER_BUTTON);
    }

    buttons.put(0, new KitButton());
    buttons.put(2, new SaveButton());
    buttons.put(6, new LoadButton());
    buttons.put(7, new ClearButton());
    buttons.put(8, new CancelButton());

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    buttons.put(18, new ArmorButton(practicePlayerData.getSelectedLadder().getArmor()[3]));
    buttons.put(27, new ArmorButton(practicePlayerData.getSelectedLadder().getArmor()[2]));
    buttons.put(36, new ArmorButton(practicePlayerData.getSelectedLadder().getArmor()[1]));
    buttons.put(45, new ArmorButton(practicePlayerData.getSelectedLadder().getArmor()[0]));

    Arrays.stream(practicePlayerData.getSelectedLadder().getEditorItems()).forEach(itemStack -> {
          for (int i = 20; i < 26; i++) {
            itemStack = practicePlayerData.getSelectedLadder().getEditorItems()[i - 20];
            if (itemStack != null) {
              buttons.remove(i);
              buttons.put(i, new RefillableItemButton(itemStack));
            }
          }
        }
    );

    return buttons;
  }

  @Override
  public void onOpen(Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    practicePlayerData.setPlayerState(PlayerState.EDITING);
    practicePlayerData.setActive(true);

    if (practicePlayerData.getSelectedKit() != null) {
      player.getInventory().setContents(practicePlayerData.getSelectedKit().getContents());
    }

    player.updateInventory();
  }

  @Override
  public void onClose(Player player) {
    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(player.getUniqueId());
      if (practicePlayerData == null) {
        return;
      }

      practicePlayerData.setPlayerState(PlayerState.SPAWN);
      practicePlayerData.setActive(false);
      player.getInventory().clear();
      plugin.getManagerHandler().getPlayerManager().giveLobbyItems(player);
    }, 1L);
  }
}
