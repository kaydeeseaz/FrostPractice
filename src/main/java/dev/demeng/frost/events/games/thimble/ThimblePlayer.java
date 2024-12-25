package dev.demeng.frost.events.games.thimble;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThimblePlayer extends EventPlayer {

  private State state = State.WAITING;

  public ThimblePlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum State {
    WAITING, JUMPING, NEXT_ROUND, ELIMINATED
  }
}
