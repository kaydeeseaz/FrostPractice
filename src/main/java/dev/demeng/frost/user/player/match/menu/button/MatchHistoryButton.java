package dev.demeng.frost.user.player.match.menu.button;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.match.MatchLocatedData;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class MatchHistoryButton extends Button {

  private final MatchLocatedData locatedData;
  private final ConfigCursor menu = new ConfigCursor(Frost.getInstance().getMenusConfig(),
      "MATCH-HISTORY");

  @Override
  public ItemStack getButtonItem(Player player) {
    OfflinePlayer winner = Bukkit.getOfflinePlayer(locatedData.getWinnerUUID());
    OfflinePlayer loser = Bukkit.getOfflinePlayer(locatedData.getLoserUUID());

    ArrayList<String> lore = new ArrayList<>();
    for (String string : menu.getStringList("MATCH.LORE")) {
      lore.add(string
          .replace("<winner>", winner.getName())
          .replace("<loser>", loser.getName())
          .replace("<date>", locatedData.getDate())
          .replace("<kit>", locatedData.getKit())
          .replace("<winner_elo>", String.valueOf(locatedData.getWinnerElo()))
          .replace("<loser_elo>", String.valueOf(locatedData.getLoserElo()))
          .replace("<winner_elo_modifier>", String.valueOf(locatedData.getWinnerEloModifier()))
          .replace("<loser_elo_modifier>", String.valueOf(locatedData.getLoserEloModifier()))
      );
    }

    return new ItemBuilder(Material.SKULL_ITEM)
        .name(menu.getString("MATCH.NAME").replace("<winner>", winner.getName())
            .replace("<loser>", loser.getName()))
        .owner(winner.getName())
        .durability(3)
        .lore(lore)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    playNeutral(player);
    player.openInventory(
        locatedData.getMatchHistoryInvSnap().getWinnerInventory().getCurrentPage());
  }
}
