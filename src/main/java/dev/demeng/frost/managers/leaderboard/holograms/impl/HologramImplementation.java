package dev.demeng.frost.managers.leaderboard.holograms.impl;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.managers.leaderboard.Leaderboard;
import dev.demeng.frost.managers.leaderboard.holograms.NyaHologram;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.RatingUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.bukkit.Location;

public class HologramImplementation extends NyaHologram {

  private final Frost plugin = Frost.getInstance();
  private int i = 0;
  private Kit kit;
  private Kit nextKit;

  public HologramImplementation(Location location, int time, String type) {
    super(location, time, type);
  }

  @Override
  public void update() {
    i++;
    try {
      kit = plugin.getManagerHandler().getKitManager().getAllKits().get(i);
      if (kit == null) {
        i = 0;
      }
    } catch (IndexOutOfBoundsException e) {
      i = 0;
      kit = plugin.getManagerHandler().getKitManager().getAllKits().get(0);
    }

    try {
      nextKit = plugin.getManagerHandler().getKitManager().getAllKits().get(i + 1);
    } catch (IndexOutOfBoundsException e) {
      nextKit = plugin.getManagerHandler().getKitManager().getAllKits().get(0);
    }
  }

  @Override
  public void updateLines() {
    final ConfigCursor configCursor = new ConfigCursor(plugin.getSettingsConfig(), "HOLOGRAM");

    switch (this.getType().toUpperCase()) {
      case "RANKED":
        for (String s : configCursor.getStringList("FORMAT.LINES")) {
          if (s.equalsIgnoreCase("<top>")) {
            int index = 0;
            for (Leaderboard leaderboard : plugin.getManagerHandler().getLeaderboardManager()
                .getSortedKitLeaderboards(kit, "elo").stream().limit(10)
                .collect(Collectors.toList())) {
              if (leaderboard.getPlayerUuid() != null) {
                getLines().add(CC.color(configCursor.getString("FORMAT.FORMAT")
                    .replace("<number>", String.valueOf(index + 1))
                    .replace("<name>", leaderboard.getPlayerName())
                    .replace("<value>", String.valueOf(leaderboard.getPlayerElo()))
                    .replace("<elo_rating>",
                        RatingUtil.getRankByElo(leaderboard.getPlayerElo()).getName())
                ));
                index++;
              }
            }
          } else {
            getLines().add(CC.color(s
                .replaceAll("<kit>", kit.getName())
                .replaceAll("<nextKit>", nextKit.getName())
                .replaceAll("<update>", String.valueOf(getActualTime()))
            ));
          }
        }
        break;
      case "WINSTREAK":
        for (String s : configCursor.getStringList("WINSTREAK.LINES")) {
          if (s.equalsIgnoreCase("<top>")) {
            int index = 0;
            for (Leaderboard winStreak : plugin.getManagerHandler().getLeaderboardManager()
                .getKitLeaderboards(kit).stream()
                .sorted(Comparator.comparingInt(Leaderboard::getPlayerWinStreak).reversed())
                .limit(10).collect(Collectors.toList())) {
              if (winStreak.getPlayerUuid() != null) {
                getLines().add(CC.color(configCursor.getString("WINSTREAK.FORMAT")
                    .replace("<number>", String.valueOf(index + 1))
                    .replace("<name>", winStreak.getPlayerName())
                    .replace("<value>", String.valueOf(winStreak.getPlayerWinStreak()))
                ));
                index++;
              }
            }
          } else {
            getLines().add(CC.color(s
                .replaceAll("<kit>", kit.getName())
                .replaceAll("<nextKit>", nextKit.getName())
                .replaceAll("<update>", String.valueOf(getActualTime()))
            ));
          }
        }
        break;
    }
  }
}
