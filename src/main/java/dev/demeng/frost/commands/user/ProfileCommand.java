package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Dependency;

public class ProfileCommand {

  @Dependency private Frost plugin;

  @Command({"elo", "stats", "profile", "playerstats"})
  public void getProfile(Player player, @Default("me") Player target) {
    Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      PlayerUtil.getStyle(player, target, "STATS", plugin);
    });
  }
}
