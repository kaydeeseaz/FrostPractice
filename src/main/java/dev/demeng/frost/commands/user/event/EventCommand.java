package dev.demeng.frost.commands.user.event;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.EventState;
import dev.demeng.frost.user.ui.host.EventHostingMenu;
import dev.demeng.frost.user.ui.host.EventManagerMenu;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("event")
public class EventCommand {

  @Dependency private Frost plugin;

  @Subcommand("host")
  @CommandPermission("frost.host.event")
  public void hostEvent(Player player) {
    if (plugin.getManagerHandler().getEventManager().getEvents().values().stream()
        .anyMatch(e -> e.getState() != EventState.UNANNOUNCED)) {
      player.sendMessage(CC.color("&cThere's an event currently ongoing."));
      return;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      new EventHostingMenu().openMenu(player);
    });
  }

  @Subcommand("manage")
  @CommandPermission("frost.event.manage")
  public void manageEvent(Player player) {
    if (plugin.getManagerHandler().getEventManager().getOngoingEvent() == null) {
      player.sendMessage(CC.color("&cThere are no manageable events currently available."));
      return;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      new EventManagerMenu().openMenu(player);
    });
  }
}
