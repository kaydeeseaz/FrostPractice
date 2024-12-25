package dev.demeng.frost.events.games.gulag;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public class GulagPlayer extends EventPlayer {

  private GulagState state = GulagState.WAITING;
  private BukkitTask fightTask;
  private GulagPlayer fighting;

  public GulagPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum GulagState {
    WAITING, PREPARING, FIGHTING, ELIMINATED
  }
}
