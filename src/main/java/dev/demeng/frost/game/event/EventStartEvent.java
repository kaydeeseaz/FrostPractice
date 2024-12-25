package dev.demeng.frost.game.event;

import dev.demeng.frost.events.PracticeEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EventStartEvent extends BaseEvent {

  private final PracticeEvent<?> event;
}
