package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;

public class LeaderboardCommand {

  @Dependency private Frost plugin;

  @Command({"leaderboard", "leaderboards", "lb", "topelo"})
  public void getLeaderboard(Player player) {
    Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      PlayerUtil.getStyle((player), null, "ELO", this.plugin);
    });
  }
}
