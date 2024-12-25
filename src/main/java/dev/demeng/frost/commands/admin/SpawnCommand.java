package dev.demeng.frost.commands.admin;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends Command {

  private final Frost plugin = Frost.getInstance();

  public SpawnCommand() {
    super("spawn");
    this.setDescription("Spawn command.");
    this.setUsage(ChatColor.RED + "Usage: /spawn [args]");
  }

  @Override
  public boolean execute(CommandSender sender, String alias, String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }

    Player player = (Player) sender;
    if (!player.hasPermission("frost.staff.spawn")) {
      player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
      return true;
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() != PlayerState.SPAWN
        && practicePlayerData.getPlayerState() != PlayerState.FFA) {
      CC.sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return true;
    }

    if (plugin.getManagerHandler().getFfaManager().getByPlayer(player) != null) {
      CC.sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return true;
    }

    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);

    return true;
  }
}
