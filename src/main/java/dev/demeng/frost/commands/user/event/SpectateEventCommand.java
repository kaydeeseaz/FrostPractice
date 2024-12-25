package dev.demeng.frost.commands.user.event;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.EventState;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;

public class SpectateEventCommand {

  @Dependency private Frost plugin;

  @Command({"eventspectate", "eventspec", "specevent"})
  public void spectateEvent(Player player, String eventName) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager()
        .getParty(practicePlayerData.getUniqueId());
    if (party != null || (practicePlayerData.getPlayerState() != PlayerState.SPAWN
        && practicePlayerData.getPlayerState() != PlayerState.SPECTATING)) {
      CC.sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return;
    }

    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getByName(eventName);
    if (event == null) {
      player.sendMessage(CC.color("&cThat player is currently not in an event."));
      return;
    }
    if (event.getState() == EventState.UNANNOUNCED) {
      player.sendMessage(CC.color("&cThat event is not available right now."));
      return;
    }
    if (practicePlayerData.getPlayerState() == PlayerState.SPECTATING) {
      if (plugin.getManagerHandler().getEventManager().getSpectators()
          .containsKey(player.getUniqueId())) {
        player.sendMessage(CC.color("&cYou are already spectating this event."));
        return;
      }
      plugin.getManagerHandler().getEventManager().removeSpectator(player, event);
    }

    plugin.getManagerHandler().getEventManager().addSpectator(player, practicePlayerData, event);
    player.sendMessage(
        CC.color("&aYou are now spectating the &f" + event.getName() + " Event &a."));
  }
}
