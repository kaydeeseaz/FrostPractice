package dev.demeng.frost.handler;

import dev.demeng.frost.Frost;
import dev.demeng.frost.providers.tab.TablistProvider;
import dev.demeng.frost.runnable.ExpBarRunnable;
import dev.demeng.frost.runnable.FollowRunnable;
import dev.demeng.frost.runnable.ItemDespawnRunnable;
import dev.demeng.frost.runnable.SaveDataRunnable;
import dev.demeng.frost.tablist.FrozedTablist;
import dev.demeng.frost.util.threads.Threads;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class MiscHandler {

  private final Frost plugin;

  public MiscHandler(Frost plugin) {
    this.plugin = plugin;
    this.register();
  }

  private void register() {
    if (plugin.getSettingsConfig().getConfig().getBoolean("SETTINGS.GENERAL.TABLIST-ENABLED")) {
      new FrozedTablist(plugin, new TablistProvider(), 0, 20);
    }

    Threads.scheduleData(new SaveDataRunnable(plugin), 5L, 60L * 5L);
    plugin.getServer().getScheduler()
        .runTaskTimerAsynchronously(plugin, new ExpBarRunnable(plugin), 1L, 1L);
    plugin.getServer().getScheduler()
        .runTaskTimerAsynchronously(plugin, new FollowRunnable(plugin), 20L, 20L);
    plugin.getServer().getScheduler()
        .runTaskTimerAsynchronously(plugin, new ItemDespawnRunnable(plugin), 2L, 2L);
    plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
      plugin.getManagerHandler().getLeaderboardManager().updateLeaderboards();
    }, 20L, plugin.getLeaderboardUpdateTime());

    for (World world : Bukkit.getWorlds()) {
      world.setGameRuleValue("doDaylightCycle", "false");
      world.setGameRuleValue("doMobSpawning", "false");
      world.setTime(0L);
      world.setStorm(false);
    }

    for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
      if (entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.ITEM_FRAME) {
        entity.remove();
      }
    }
  }
}
