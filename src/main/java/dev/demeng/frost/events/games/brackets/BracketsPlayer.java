package dev.demeng.frost.events.games.brackets;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public class BracketsPlayer extends EventPlayer {

  private BracketsState state = BracketsState.WAITING;
  private BukkitTask fightTask;
  private BracketsPlayer fighting;

  public BracketsPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum BracketsState {
    WAITING, PREPARING, FIGHTING, ELIMINATED
  }
}
