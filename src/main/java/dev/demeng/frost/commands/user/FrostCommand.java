package dev.demeng.frost.commands.user;

import dev.demeng.pluginbase.Common;
import dev.demeng.pluginbase.text.Text;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;

public class FrostCommand {

  @Command({"frost", "frostpractice", "practice"})
  public void sendInformation(CommandSender sender) {
    Text.coloredTell(sender, "&9&lRunning Frost v" + Common.getVersion() + " by Demeng.");
    Text.coloredTell(sender, "&bLink: &fhttps://demeng.dev/frost");
  }
}
