package dev.demeng.frost.events.games.dropper;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DropperPlayer extends EventPlayer {

  private State state = State.WAITING;
  private int phase;

  public DropperPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum State {
    WAITING, PLAYING
  }
}
