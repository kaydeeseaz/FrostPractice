package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.ui.postmatch.InventorySnapshot;
import dev.demeng.frost.util.CC;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;

public class PostMatchInventoryCommand {

  @Dependency
  private Frost plugin;

  private final Pattern UUID_PATTERN = Pattern.compile(
      "[a-fA-F\\d]{8}-[a-fA-F\\d]{4}-[a-fA-F\\d]{4}-[a-fA-F\\d]{4}-[a-fA-F\\d]{12}");

  @Command("_")
  public void getPostMatchInventory(Player player, String target) {
    if (!target.matches(UUID_PATTERN.pattern())) {
      CC.sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.INVENTORY-EXPIRED"));
      return;
    }

    InventorySnapshot snapshot = plugin.getManagerHandler().getInventoryManager()
        .getSnapshot(UUID.fromString(target));
    if (snapshot == null) {
      CC.sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.INVENTORY-EXPIRED"));
    } else {
      player.openInventory(snapshot.getInventoryUI().getCurrentPage());
    }
  }
}
