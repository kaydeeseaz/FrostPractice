package dev.demeng.frost.game.event.match;

import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import lombok.Getter;

@Getter
public class MatchEndEvent extends MatchEvent {

  private final MatchTeam winningTeam;
  private final MatchTeam losingTeam;

  public MatchEndEvent(Match match, MatchTeam winningTeam, MatchTeam losingTeam) {
    super(match);

    this.winningTeam = winningTeam;
    this.losingTeam = losingTeam;
  }

}
