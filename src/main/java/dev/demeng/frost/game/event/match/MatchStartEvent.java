package dev.demeng.frost.game.event.match;

import dev.demeng.frost.game.match.Match;

public class MatchStartEvent extends MatchEvent {

  public MatchStartEvent(Match match) {
    super(match);
  }
}
