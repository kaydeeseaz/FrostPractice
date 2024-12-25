package dev.demeng.frost.events.tasks;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.Clickable;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Setter
@Getter
public abstract class EventCountdownTask extends BukkitRunnable {

  private final PracticeEvent<?> event;
  private final int countdownTime;

  private int timeUntilStart;
  private boolean ended;

  public EventCountdownTask(PracticeEvent<?> event, int countdownTime) {
    this.event = event;
    this.countdownTime = countdownTime;
    this.timeUntilStart = countdownTime;
  }

  @Override
  public void run() {
    if (isEnded()) {
      return;
    }

    if (timeUntilStart <= 0) {
      if (canStart()) {
        event.start();
      } else {
        onCancel();
      }

      ended = true;
      return;
    }

    if (shouldAnnounce(timeUntilStart)) {
      for (String message : CC.color(Frost.getInstance().getMessagesConfig().getConfig()
          .getStringList("MESSAGES.EVENT.ANNOUNCEMENT"))) {
        Clickable clickable = new Clickable(message
            .replace("<host>", event.getHost().getName())
            .replace("<countdown>", String.valueOf(event.getCountdownTask().timeUntilStart))
            .replace("<maxPlayers>", String.valueOf(event.getLimit()))
            .replace("<players>", String.valueOf(event.getPlayersX().size()))
            .replace("<eventName>", event.getName()),
            CC.color(Frost.getInstance().getMessagesConfig().getConfig()
                .getString("MESSAGES.EVENT.ANNOUNCEMENT-CLICKABLE")),
            "/join_event " + event.getName());

        Bukkit.getServer().getOnlinePlayers()
            .stream()
            .filter(eventPlayer -> !event.getPlayers().containsKey(eventPlayer.getUniqueId()))
            .collect(Collectors.toList()).forEach(clickable::sendToPlayer);
      }
    }

    timeUntilStart--;
  }

  public abstract boolean shouldAnnounce(int timeUntilStart);

  public abstract boolean canStart();

  public abstract void onCancel();
}
