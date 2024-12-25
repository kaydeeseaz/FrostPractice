package dev.demeng.frost.scoreboard.scoreboard;

import dev.demeng.frost.scoreboard.scoreboard.cooldown.BoardCooldown;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public interface BoardAdapter {

  String getTitle(Player player);

  List<String> getScoreboard(Player player, Board board, Set<BoardCooldown> cooldowns);

  void onScoreboardCreate(Player player, Scoreboard board);
}
