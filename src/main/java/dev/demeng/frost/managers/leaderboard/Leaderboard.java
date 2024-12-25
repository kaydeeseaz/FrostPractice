package dev.demeng.frost.managers.leaderboard;

import dev.demeng.frost.game.kit.Kit;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Leaderboard {

  private int playerElo;
  private int playerWinStreak;
  private UUID playerUuid;
  private String playerName;
  private Kit kit;
}
