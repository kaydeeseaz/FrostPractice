package dev.demeng.frost.events.games.sumo;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

@Setter
@Getter
public class SumoPlayer extends EventPlayer {

  private SumoState state = SumoState.WAITING;
  private BukkitTask fightTask;
  private SumoPlayer fighting;

  public SumoPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum SumoState {
    WAITING, PREPARING, FIGHTING, ELIMINATED
  }
}

