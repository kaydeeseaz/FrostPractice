package dev.demeng.frost.scoreboard.scoreboard;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Accessors(chain = true)
public class BoardEntry {

  @Getter private final Board board;
  @Getter @Setter private String text;
  @Getter private final String originalText;
  @Getter private final String key;
  @Getter private Team team;

  public BoardEntry(Board board, String text) {
    this.board = board;
    this.text = text;
    this.originalText = text;
    this.key = board.getNewKey(this);

    setup();
  }

  public BoardEntry setup() {
    Scoreboard scoreboard = board.getScoreboard();

    text = ChatColor.translateAlternateColorCodes('&', text);

    String teamName = key;

    if (teamName.length() > 16) {
      teamName = teamName.substring(0, 16);
    }

    if (scoreboard.getTeam(teamName) != null) {
      team = scoreboard.getTeam(teamName);
    } else {
      team = scoreboard.registerNewTeam(teamName);
    }

    if (!(team.getEntries().contains(key))) {
      team.addEntry(key);
    }

    if (!(board.getEntries().contains(this))) {
      board.getEntries().add(this);
    }

    return this;
  }

  public void send(int position) {
    Objective objective = board.getObjective();

    if (text.length() > 16) {
      boolean fix = text.toCharArray()[15] == ChatColor.COLOR_CHAR;

      String prefix = fix ? text.substring(0, 15) : text.substring(0, 16);
      String suffix =
          fix ? text.substring(15) : ChatColor.getLastColors(prefix) + text.substring(16);

      team.setPrefix(prefix);

      if (suffix.length() > 16) {
        team.setSuffix(suffix.substring(0, 16));
      } else {
        team.setSuffix(suffix);
      }
    } else {
      team.setPrefix(text);
      team.setSuffix("");
    }

    Score score = objective.getScore(key);
    score.setScore(position);

  }

  public void remove() {
    board.getKeys().remove(key);
    board.getScoreboard().resetScores(key);
  }

}
