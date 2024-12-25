package dev.demeng.frost.user.ui.queue.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.game.tournament.Tournament;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class TournamentButton extends Button {

  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(),
      "QUEUES.TYPES.TOURNAMENT");

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();

    Tournament tournament = plugin.getManagerHandler().getTournamentManager().getTournaments()
        .get(0);
    for (String text : config.getStringList("LORE")) {
      lore.add(text
          .replace("<tournament_kit>", String.valueOf(tournament.getKitName()))
          .replace("<tournament_round>", String.valueOf(tournament.getCurrentRound()))
          .replace("<tournament_players>", String.valueOf(tournament.getPlayers().size()))
          .replace("<tournament_max_players>", String.valueOf(tournament.getSize()))
          .replace("<tournament_team_size>", String.valueOf(tournament.getTeamSize()))
      );
    }

    return new ItemBuilder(Material.valueOf(config.getString("ICON")))
        .name(CC.parse(player, config.getString("NAME")))
        .durability(config.getInt("DATA"))
        .lore(CC.parse(player, lore))
        .amount(1)
        .hideFlags()
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    player.performCommand("tournament join");
  }
}
