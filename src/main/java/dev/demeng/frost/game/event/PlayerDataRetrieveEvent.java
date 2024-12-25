package dev.demeng.frost.game.event;

import dev.demeng.frost.user.player.PracticePlayerData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class PlayerDataRetrieveEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();
  private final PracticePlayerData practicePlayerData;

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }
}
