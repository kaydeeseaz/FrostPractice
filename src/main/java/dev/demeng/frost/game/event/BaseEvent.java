package dev.demeng.frost.game.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BaseEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public boolean call() {
    Bukkit.getServer().getPluginManager().callEvent(this);
    return this instanceof Cancellable && ((Cancellable) this).isCancelled();
  }
}
