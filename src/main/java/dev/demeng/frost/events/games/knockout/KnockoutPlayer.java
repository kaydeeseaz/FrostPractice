package dev.demeng.frost.events.games.knockout;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnockoutPlayer extends EventPlayer {

  private KnockoutState state = KnockoutState.WAITING;

  public KnockoutPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum KnockoutState {
    WAITING, FIGHTING, ELIMINATED
  }
}
