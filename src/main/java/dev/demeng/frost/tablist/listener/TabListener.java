package dev.demeng.frost.tablist.listener;

import dev.demeng.frost.tablist.FrozedTablist;
import dev.demeng.frost.tablist.FrozedTablist.Version;
import dev.demeng.frost.tablist.layout.TabLayout;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class TabListener implements Listener {

  private FrozedTablist instance;

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    TabLayout tabLayout_;
    boolean validate = false;

    if (Objects.requireNonNull(FrozedTablist.getInstance().getVersion()) == Version.v1_8_R3) {
      tabLayout_ = new TabLayout(instance, player);
      if (TabLayout.getLayoutMapping().containsKey(player.getUniqueId())) {
        validate = true;
      }

      if (TabLayout.getLayoutMapping().get(player.getUniqueId()) != null) {
        validate = true;
      }

      if (!validate) {
        tabLayout_.create();
        tabLayout_.setHeaderAndFooter();
      }

      TabLayout.getLayoutMapping().put(player.getUniqueId(), tabLayout_);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    instance.removePlayer(player);
  }

  @EventHandler
  public void onKick(PlayerKickEvent event) {
    Player player = event.getPlayer();
    instance.removePlayer(player);
  }
}
