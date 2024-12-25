package dev.demeng.frost.events.games.corners;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FourCornersPlayer extends EventPlayer {

  private FourCornerState state = FourCornerState.WAITING;
  private boolean wasEliminated = false;

  public FourCornersPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum FourCornerState {
    WAITING, INGAME, ELIMINATED
  }
}
