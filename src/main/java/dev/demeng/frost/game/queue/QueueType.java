package dev.demeng.frost.game.queue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueType {

  UNRANKED("Unranked"),
  RANKED("Ranked"),
  PREMIUM("Premium");

  private final String name;

  public boolean isRanked() {
    return this != QueueType.UNRANKED;
  }

  public boolean isPremium() {
    return this == QueueType.PREMIUM;
  }
}
