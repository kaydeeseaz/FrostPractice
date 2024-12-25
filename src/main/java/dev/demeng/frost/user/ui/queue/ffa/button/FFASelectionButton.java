package dev.demeng.frost.user.ui.queue.ffa.button;

import dev.demeng.frost.game.ffa.FfaInstance;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class FFASelectionButton extends Button {

  private final FfaInstance ffaInstance;
  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "FFA-MENU.KIT-DISPLAY");

  public FFASelectionButton(FfaInstance ffaInstance) {
    this.ffaInstance = ffaInstance;
  }

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = new ArrayList<>();
    for (String string : config.getStringList("LORE")) {
      lore.add(CC.parse(player, string)
          .replace("<players>", String.valueOf(ffaInstance.getFfaPlayers().size())));
    }

    return new ItemBuilder(ffaInstance.getKit().getIcon().clone())
        .name(config.getString("NAME").replace("<kit>", ffaInstance.getKit().getName()))
        .lore(lore)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    if (plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
        .getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      Button.playFail(player);
      return;
    }

    Button.playSuccess(player);
    ffaInstance.addPlayer(player);
  }
}
