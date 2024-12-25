package dev.demeng.frost.handler;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.ffa.FFAListener;
import dev.demeng.frost.game.match.listeners.MatchEndListener;
import dev.demeng.frost.game.match.listeners.MatchFireballListener;
import dev.demeng.frost.game.match.listeners.MatchStartListener;
import dev.demeng.frost.game.match.listeners.MenuBugFixListener;
import dev.demeng.frost.game.match.listeners.SkyWarsMatchListener;
import dev.demeng.frost.game.match.listeners.SpecialMatchListener;
import dev.demeng.frost.listeners.EntityListener;
import dev.demeng.frost.listeners.InventoryListener;
import dev.demeng.frost.listeners.KitEditorListener;
import dev.demeng.frost.listeners.MovementListener;
import dev.demeng.frost.listeners.PlayerListener;
import dev.demeng.frost.listeners.WorldListener;
import dev.demeng.frost.util.inventory.UIListener;
import dev.demeng.frost.util.menu.ButtonListener;
import java.util.Arrays;
import java.util.List;
import org.bukkit.event.Listener;

public class ListenerHandler {

  private final Frost plugin;

  public ListenerHandler(Frost plugin) {
    this.plugin = plugin;
    this.register();
  }

  private void register() {
    List<Listener> listeners = Arrays.asList(
        new PlayerListener(), new InventoryListener(), new MovementListener(), new WorldListener(),
        new KitEditorListener(), new EntityListener(),
        new MatchStartListener(), new MatchEndListener(), new SkyWarsMatchListener(),
        new SpecialMatchListener(), new MatchFireballListener(plugin),
        new FFAListener(), new UIListener(), new MenuBugFixListener(), new ButtonListener()
    );

    for (Listener listener : listeners) {
      plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
  }
}
