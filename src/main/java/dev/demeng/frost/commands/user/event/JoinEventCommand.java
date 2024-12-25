package dev.demeng.frost.commands.user.event;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.EventState;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;

public class JoinEventCommand {

  @Dependency private Frost plugin;

  @Command({"join", "join_event", "event_join"})
  public void joinEvent(Player player, String eventName) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (plugin.getManagerHandler().getPartyManager().getParty(practicePlayerData.getUniqueId())
        != null || practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
      return;
    }

    String eventId = eventName.toLowerCase();
    if (!NumberUtils.isNumber(eventId)) {
      PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getByName(eventId);
      if (event == null) {
        player.sendMessage(CC.color("&cThat event doesn't exist."));
        return;
      }
      if (event.getState() != EventState.WAITING) {
        player.sendMessage(CC.color("&cThat event is currently not available."));
        return;
      }
      if (event.getPlayers().containsKey(player.getUniqueId())) {
        player.sendMessage(CC.color("&cYou're already in this event."));
        return;
      }
      if (event.getPlayers().size() >= event.getLimit()) {
        player.sendMessage(CC.color("&cThe event is already full."));
      }

      event.join(player);
    }
  }
}
