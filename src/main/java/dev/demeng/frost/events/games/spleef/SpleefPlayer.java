package dev.demeng.frost.events.games.spleef;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpleefPlayer extends EventPlayer {

  private State state = State.WAITING;

  public SpleefPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum State {
    WAITING, PLAYING, ELIMINATED
  }
}
