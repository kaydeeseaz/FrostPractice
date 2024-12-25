package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.threads.Threads;

public class SaveDataRunnable implements Runnable {

  private final Frost plugin;

  public SaveDataRunnable(Frost plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    int amount = 0;
    plugin.getLogger().finest("Saving player data!");

    for (PracticePlayerData practicePlayerData : plugin.getManagerHandler().getPlayerManager()
        .getAllData()) {
      Threads.executeData(() -> {
        plugin.getManagerHandler().getPlayerManager().saveData(practicePlayerData);
      });

      ++amount;
    }

    plugin.getLogger().finest("Successfully saved " + amount + " players!");
  }
}
