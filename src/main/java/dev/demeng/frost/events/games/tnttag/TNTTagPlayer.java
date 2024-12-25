package dev.demeng.frost.events.games.tnttag;

import dev.demeng.frost.events.EventPlayer;
import dev.demeng.frost.events.PracticeEvent;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

@Setter
@Getter
public class TNTTagPlayer extends EventPlayer {

  private TNTTagState state = TNTTagState.WAITING;
  private BukkitTask fightTask;
  private TNTTagPlayer fighting;

  public TNTTagPlayer(UUID uuid, PracticeEvent<?> event) {
    super(uuid, event);
  }

  public enum TNTTagState {
    WAITING, INGAME, ELIMINATED, TAGGED
  }
}
