package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import java.util.Iterator;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Item;

@AllArgsConstructor
public class ItemDespawnRunnable implements Runnable {

  private final Frost plugin;

  @Override
  public void run() {
    plugin.getManagerHandler().getFfaManager().getSpawnFfaKits().forEach((s, spawnFFA) -> {
      Iterator<Item> iterator = spawnFFA.getFfaItems().keySet().iterator();
      while (iterator.hasNext()) {
        Item item = iterator.next();
        long time = spawnFFA.getFfaItems().get(item);
        if (time + 10000 < System.currentTimeMillis()) {
          item.remove();
          iterator.remove();
        }
      }
    });
  }
}
