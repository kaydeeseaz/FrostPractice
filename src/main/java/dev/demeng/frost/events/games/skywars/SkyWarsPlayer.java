package dev.demeng.frost.events.games.skywars;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SkyWarsPlayer extends EventPlayer {

  private SkyWarsState state = SkyWarsState.WAITING;

  public SkyWarsPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum SkyWarsState {
    WAITING, FIGHTING, ELIMINATED
  }
}

