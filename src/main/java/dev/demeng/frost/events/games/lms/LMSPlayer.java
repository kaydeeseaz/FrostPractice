package dev.demeng.frost.events.games.lms;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LMSPlayer extends EventPlayer {

  private LMSState state = LMSState.WAITING;

  public LMSPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum LMSState {
    WAITING, FIGHTING, ELIMINATED
  }
}

