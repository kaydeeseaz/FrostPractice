package dev.demeng.frost.events.games.parkour;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.util.CustomLocation;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParkourPlayer extends EventPlayer {

  private ParkourState state = ParkourState.WAITING;
  private CustomLocation lastCheckpoint;
  private int checkpointId;

  public ParkourPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum ParkourState {
    WAITING, INGAME
  }
}
