package dev.demeng.frost.user.ui.matches.buttons;

import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class OngoingMatchButton extends Button {

  private final Match match;
  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "ONGOING-MATCHES-INVENTORY");

  public ItemStack getButtonItem(Player player) {
    List<String> lore = config.getStringList("LORE");

    lore.replaceAll(s ->
        CC.parse(player, s
            .replace("%arena%", match.getArena().getName())
            .replace("%duration%", match.getDuration())
            .replace("%spectators%", String.valueOf(match.getSpectators().size()))
            .replace("%kit%", match.getKit().getName())
            .replace("%type%", match.getType().getName())
        )
    );

    return new ItemBuilder(match.getKit().getIcon().clone())
        .name(CC.parse(player, config.getString("NAME")
            .replaceAll("%p1%", match.getTeams().get(0).getLeaderName())
            .replaceAll("%p2%", match.getTeams().get(1).getLeaderName())))
        .lore(lore)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    player.performCommand("spectate " + match.getTeams().get(0).getLeaderName());
  }
}
