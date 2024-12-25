package dev.demeng.frost.commands.admin;

import dev.demeng.frost.Frost;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.CommandActor;

public class ForceMenuUpdateCommand {

  @Dependency private Frost plugin;

  @Command("updatemenus")
  @CommandPermission("frost.admin")
  public void forceMenuUpdate(CommandActor player) {
    plugin.getManagerHandler().getInventoryManager().reloadInventories();
    player.reply("&aSuccessfully updated menus.");
  }
}
