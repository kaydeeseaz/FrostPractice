package dev.demeng.frost.managers;

import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.TtlHashMap;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.bukkit.entity.Player;

public class PartyManager {

  private final Frost plugin = Frost.getInstance();
  private final ConfigCursor partyMessage = new ConfigCursor(
      Frost.getInstance().getMessagesConfig(), "MESSAGES.PARTY");

  @Getter private final Map<UUID, Party> parties = new ConcurrentHashMap<>();
  private final Map<UUID, UUID> partyLeaders = new ConcurrentHashMap<>();
  private final Map<UUID, List<UUID>> partyInvites = new TtlHashMap<>(TimeUnit.SECONDS, 15);

  public boolean isLeader(UUID uuid) {
    return this.parties.containsKey(uuid);
  }

  public void removePartyInvites(UUID uuid) {
    this.partyInvites.remove(uuid);
  }

  public boolean hasPartyInvite(UUID player, UUID other) {
    return this.partyInvites.get(player) != null && this.partyInvites.get(player).contains(other);
  }

  public void createPartyInvite(UUID requester, UUID requested) {
    this.partyInvites.computeIfAbsent(requested, k -> new ArrayList<>()).add(requester);
  }

  public boolean isInParty(UUID player, Party party) {
    Party targetParty = this.getParty(player);
    return targetParty != null && targetParty.getLeader() == party.getLeader();
  }

  public Party getPartyByLeader(UUID uuid) {
    if (this.partyLeaders.containsKey(uuid)) {
      UUID leader = this.partyLeaders.get(uuid);
      return this.parties.get(leader);
    }

    return null;
  }

  public Party getParty(UUID player) {
    if (this.parties.containsKey(player)) {
      return this.parties.get(player);
    }

    if (this.partyLeaders.containsKey(player)) {
      UUID leader = this.partyLeaders.get(player);
      return this.parties.get(leader);
    }

    return null;
  }

  public void createParty(Player player) {
    Party party = new Party(player.getUniqueId());
    this.parties.put(player.getUniqueId(), party);

    plugin.getManagerHandler().getInventoryManager().addParty(player);
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, false);
    sendMessage(player, partyMessage.getString("CREATED"));
  }

  private void disbandParty(Party party, boolean tournament) {
    plugin.getManagerHandler().getInventoryManager().removeParty(party);
    this.parties.remove(party.getLeader());

    party.broadcast(partyMessage.getString("DISBANDED"));
    party.members().forEach(member -> {
      PracticePlayerData memberData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(member.getUniqueId());
      if (this.partyLeaders.get(memberData.getUniqueId()) != null) {
        this.partyLeaders.remove(memberData.getUniqueId());
      }

      if (memberData.getPlayerState() == PlayerState.SPAWN) {
        plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(member, false);
      }
    });
  }

  public void leaveParty(Player player) {
    Party party = this.getParty(player.getUniqueId());
    if (party == null) {
      return;
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (this.parties.containsKey(player.getUniqueId())) {
      this.disbandParty(party, false);
    } else if (plugin.getManagerHandler().getTournamentManager().getTournament(player.getUniqueId())
        != null) {
      this.disbandParty(party, true);
    } else {
      party.removeMember(player.getUniqueId());
      this.partyLeaders.remove(player.getUniqueId());
      plugin.getManagerHandler().getInventoryManager().updateParty(party);
      party.broadcast(partyMessage.getString("LEFT").replace("<player>", player.getName()));
    }

    switch (practicePlayerData.getPlayerState()) {
      case FIGHTING:
        plugin.getManagerHandler().getMatchManager()
            .removeFighter(player, practicePlayerData, false);
        break;
      case SPECTATING:
        if (plugin.getManagerHandler().getEventManager().getSpectators()
            .containsKey(player.getUniqueId())) {
          plugin.getManagerHandler().getEventManager().removeSpectator(player,
              plugin.getManagerHandler().getEventManager().getEventPlaying(player));
        } else {
          plugin.getManagerHandler().getMatchManager().removeSpectator(player);
        }
        break;
    }

    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, false);
  }

  public void joinParty(UUID leader, Player player) {
    Party party = this.getParty(leader);
    if (plugin.getManagerHandler().getTournamentManager().getTournament(leader) != null) {
      sendMessage(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.TARGET-IN-TOURNAMENT"));
      return;
    }

    this.partyLeaders.put(player.getUniqueId(), leader);
    party.addMember(player.getUniqueId());
    plugin.getManagerHandler().getInventoryManager().updateParty(party);
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, false);
    party.broadcast(partyMessage.getString("JOINED").replace("<player>", player.getName()));
  }
}
