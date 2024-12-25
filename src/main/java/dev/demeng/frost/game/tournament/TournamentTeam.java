package dev.demeng.frost.game.tournament;

import dev.demeng.frost.game.team.KillableTeam;
import dev.demeng.frost.user.party.Party;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TournamentTeam extends KillableTeam {

  private final Map<UUID, String> playerNames = new HashMap<>();

  public TournamentTeam(UUID leader, List<UUID> players) {
    super(leader, players);
    for (UUID playerUUID : players) {
      Party party = plugin.getManagerHandler().getPartyManager().getParty(playerUUID);
      if (party == null) {
        this.playerNames.put(playerUUID, plugin.getServer().getPlayer(playerUUID).getName());
      }
    }
  }

  public void broadcast(String message) {
    this.alivePlayers().forEach(player -> player.sendMessage(message));
  }

  public String getPlayerName(UUID playerUUID) {
    return this.playerNames.get(playerUUID);
  }
}
