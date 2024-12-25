package dev.demeng.frost.events;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EventPlayer {

  private final UUID uuid;
  private final PracticeEvent<?> event;
}
