package dev.demeng.frost.game.queue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class QueueEntry {

  private final QueueType queueType;
  private final String kitName;

  private final int elo;

  private final boolean party;

  private boolean found = false; // prevent player joining multiple games with 1 queue.

}
