package dev.demeng.frost.game.match;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.team.KillableTeam;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Getter
public class MatchTeam extends KillableTeam {

  private final int teamID;
  private int bridgesPoints;
  @Setter private int lives;
  @Setter private Location bridgeSpawnLocation;

  private boolean hasBed = true;
  private boolean ableToScore = true;

  public MatchTeam(UUID leader, List<UUID> players, int teamID) {
    super(leader, players);
    this.teamID = teamID;
  }

  public void addPoint() {
    this.ableToScore = false;
    bridgesPoints = bridgesPoints + 1;
    Bukkit.getServer().getScheduler()
        .runTaskLater(Frost.getInstance(), () -> this.ableToScore = true, 100L);
  }

  public void destroyBed() {
    this.hasBed = false;
  }

  public void removeLife() {
    lives = lives - 1;
  }
}
