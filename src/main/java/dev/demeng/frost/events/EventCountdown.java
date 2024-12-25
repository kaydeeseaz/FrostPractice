package dev.demeng.frost.events;

import dev.demeng.frost.events.tasks.EventCountdownTask;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EventCountdown extends EventCountdownTask {

  private final int requiredPlayers;

  public EventCountdown(PracticeEvent<?> event, int requiredPlayers) {
    super(event, 60);
    this.requiredPlayers = requiredPlayers;
  }

  @Override
  public boolean shouldAnnounce(int timeUntilStart) {
    return Arrays.asList(60, 45, 30, 15, 10, 5).contains(timeUntilStart);
  }

  @Override
  public boolean canStart() {
    return this.getEvent().getPlayers().size() >= requiredPlayers;
  }

  @Override
  public void onCancel() {
    this.getEvent()
        .sendMessage(ChatColor.RED + "There were not enough players to start the event.");
    for (Map.Entry<UUID, ?> entry : getEvent().getPlayers().entrySet()) {
      UUID uuid = entry.getKey();
      if (uuid != null) {
        this.getEvent().getPlugin().getServer().getScheduler()
            .runTaskLater(this.getEvent().getPlugin(), () -> {
              Player player = Bukkit.getPlayer(uuid);
              if (player != null) {
                this.getEvent().getPlugin().getManagerHandler().getPlayerManager()
                    .resetPlayerOrSpawn(Bukkit.getPlayer(uuid), true);
              }
            }, 1L);
      }
    }
    this.getEvent().end();
  }
}
