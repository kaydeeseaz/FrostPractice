package dev.demeng.frost.commands.admin;

import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class SilentCommand {

  @Command("silent")
  @CommandPermission("frost.staff.silent")
  public void execute(Player player, PracticePlayerData practicePlayerData) {
    boolean toggledSilentMode = !practicePlayerData.isSilent();
    practicePlayerData.setSilent(toggledSilentMode);
    player.sendMessage(CC.color(
        "&8[&c❖&8] &7Silent mode is now " + (toggledSilentMode ? "&aenabled&7!"
            : "&cdisabled&7!")));
  }
}
