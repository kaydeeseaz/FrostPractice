package dev.demeng.frost.commands.admin;

import dev.demeng.frost.util.CC;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GotoEventCommand extends Command {

  public GotoEventCommand() {
    super("gotoevent");
    this.setAliases(Arrays.asList("eventsworld", "eventworld", "tpevent"));
    this.setUsage(ChatColor.RED + "Usage: /gotoevent");
  }

  @Override
  public boolean execute(CommandSender sender, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }

    Player player = (Player) sender;
    if (!player.hasPermission("frost.admin")) {
      player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
      return true;
    }

    Location eventWorld = Bukkit.getWorld("event").getSpawnLocation();
    player.teleport(eventWorld);
    player.sendMessage(CC.color("&aYou have teleported to the events world."));

    return true;
  }
}
