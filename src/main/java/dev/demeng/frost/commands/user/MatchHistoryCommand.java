package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.player.match.MatchLocatedData;
import dev.demeng.frost.user.player.match.menu.MatchHistoryMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.time.Cooldown;
import java.util.List;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Usage;

public class MatchHistoryCommand {

  @Dependency private Frost plugin;

  @Command({"matchhistory", "matchhist", "gamehistory", "rankedhistory"})
  @Usage("Usage: /matchhistory <player>")
  public void getMatchHistory(Player player, Player target) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (!practicePlayerData.getPlayerCommandCooldown().hasExpired()) {
      CC.sendMessage(player, plugin.getSettingsConfig().getConfig()
          .getString("SETTINGS.GENERAL.TOGGLE-PLAYERS-COOLDOWN-MESSAGE")
          .replace("<time>", practicePlayerData.getPlayerCommandCooldown().getTimeMilisLeft())
          .replace("<left>", practicePlayerData.getPlayerCommandCooldown().getContextLeft())
      );

      return;
    }

    practicePlayerData.setPlayerCommandCooldown(new Cooldown(
        plugin.getSettingsConfig().getConfig().getInt("SETTINGS.GENERAL.TOGGLE-PLAYERS-COOLDOWN")));
    List<MatchLocatedData> matchHistory = plugin.getManagerHandler().getMatchLocatedData()
        .getMatchesByUser(target.getUniqueId());
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      new MatchHistoryMenu(target.getUniqueId(), matchHistory).openMenu(player);
    });
  }
}
