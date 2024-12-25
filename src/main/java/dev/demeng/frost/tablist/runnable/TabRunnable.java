package dev.demeng.frost.tablist.runnable;

import dev.demeng.frost.tablist.adapter.TabAdapter;
import dev.demeng.frost.tablist.entry.TabEntry;
import dev.demeng.frost.tablist.latency.TabLatency;
import dev.demeng.frost.tablist.layout.TabLayout;
import dev.demeng.frost.tablist.skin.Skin;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class TabRunnable implements Runnable {

  private TabAdapter adapter;

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (TabLayout.getLayoutMapping().containsKey(player.getUniqueId())) {
        TabLayout layout = TabLayout.getLayoutMapping().get(player.getUniqueId());

        for (TabEntry entry : adapter.getLines(player)) {
          layout.update(entry.getColumn(), entry.getRow(), entry.getText(), entry.getPing(),
              entry.getSkin());
        }

        for (int row = 0; row < 20; row++) {
          for (int column = 0; column < 3; column++) {
            if (layout.getByLocation(adapter.getLines(player), column, row) == null) {
              layout.update(column, row, "", TabLatency.NO_BAR.getValue(), Skin.DEFAULT_SKIN);
            }
          }
        }
      }
    }
  }
}
