package dev.demeng.frost.user.party;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.util.CC;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Party {

  private final Frost plugin = Frost.getInstance();

  private UUID leader;
  private Set<UUID> members = new HashSet<>();
  private int limit = plugin.getSettingsConfig().getConfig()
      .getInt("SETTINGS.GENERAL.PARTY-LIMIT-BY-DEFAULT");
  private boolean open;

  private List<MatchTeam> matchTeams = new ArrayList<>();

  public Party(UUID leader) {
    this.leader = leader;
    this.members.add(leader);
  }

  public void addMember(UUID uuid) {
    this.members.add(uuid);
  }

  public void removeMember(UUID uuid) {
    this.members.remove(uuid);
  }

  public void broadcast(String message) {
    this.members().forEach(member -> CC.sendMessage(member, message));
  }

  public List<UUID> getPartySplitTeam(UUID uuid) {
    List<UUID> uuids = new ArrayList<>();
    for (UUID pUuid : findTeam(uuid).getAlivePlayers()) {
      if (pUuid != uuid) {
        uuids.add(pUuid);
      }
    }

    return uuids;
  }

  private MatchTeam findTeam(UUID uuid) {
    MatchTeam team = null;
    for (MatchTeam matchTeam : matchTeams) {
      if (matchTeam.getPlayers().contains(uuid)) {
        team = matchTeam;
      }
    }

    return team;
  }

  public MatchTeam findOpponent(UUID uuid) {
    MatchTeam team = null;
    for (MatchTeam matchTeam : matchTeams) {
      if (!matchTeam.getPlayers().contains(uuid)) {
        team = matchTeam;
      }
    }

    return team;
  }

  public MatchTeam[] split() {
    matchTeams.clear();

    List<UUID> teamA = new ArrayList<>();
    List<UUID> teamB = new ArrayList<>();

    ThreadLocalRandom random = ThreadLocalRandom.current();

    for (UUID member : this.members) {
      if (teamA.size() == teamB.size()) {
        if (random.nextBoolean()) {
          teamA.add(member);
        } else {
          teamB.add(member);
        }
      } else {
        if (teamA.size() < teamB.size()) {
          teamA.add(member);
        } else {
          teamB.add(member);
        }
      }
    }

    MatchTeam team1 = new MatchTeam(teamA.get(0), teamA, 0);
    MatchTeam team2 = new MatchTeam(teamB.get(0), teamB, 1);
    matchTeams.add(team1);
    matchTeams.add(team2);

    return new MatchTeam[]{
        team1,
        team2
    };
  }

  public List<UUID> getPartyMembersExcludeMember(UUID uuid) {
    return members.stream().filter(m -> m != uuid).collect(Collectors.toList());
  }

  public List<UUID> getPartyMembersExcludeLeader() {
    return members.stream().filter(m -> !this.leader.equals(m)).collect(Collectors.toList());
  }

  public Stream<Player> members() {
    return this.members.stream().map(plugin.getServer()::getPlayer).filter(Objects::nonNull);
  }
}
