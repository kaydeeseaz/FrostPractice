package dev.demeng.frost.events.games.stoplight;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StopLightPlayer extends EventPlayer {

  private State state = State.WAITING;

  public StopLightPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum State {
    LOBBY, WAITING, INGAME
  }
}
