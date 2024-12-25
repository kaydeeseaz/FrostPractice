package dev.demeng.frost.events.games.oitc;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

@Setter
@Getter
public class OITCPlayer extends EventPlayer {

  private OITCState state = OITCState.WAITING;
  private int score = 0;
  private int lives = 5;
  private BukkitTask respawnTask;
  private OITCPlayer lastKiller;

  public OITCPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum OITCState {
    WAITING, PREPARING, FIGHTING, RESPAWNING, ELIMINATED
  }
}
