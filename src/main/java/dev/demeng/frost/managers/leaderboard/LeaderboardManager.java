package dev.demeng.frost.managers.leaderboard;

import static dev.demeng.frost.util.CC.color;

import com.google.common.collect.Lists;
import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.pluginbase.mongo.lib.bson.Document;
import dev.demeng.pluginbase.mongo.lib.driver.client.MongoCursor;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardManager {

  private final Frost plugin;
  private final Set<Leaderboard> leaderboards = ConcurrentHashMap.newKeySet();

  public LeaderboardManager(Frost plugin) {
    this.plugin = plugin;
    updateLeaderboards();
  }

  public void createLeaderboards() {
    for (Kit kit : plugin.getManagerHandler().getKitManager().getKits()) {
      try (MongoCursor<Document> iterator = plugin.getManagerHandler().getPlayerManager()
          .getPlayersSortByLadderElo(kit)) {
        while (iterator.hasNext()) {
          try {
            Document document = iterator.next();
            UUID uuid = UUID.fromString(document.getString("uuid"));
            String username = document.getString("username");
            if (!document.containsKey("stats")) {
              continue;
            }

            Document statistics = (Document) document.get("stats");
            int elo = PracticePlayerData.DEFAULT_ELO;
            int winStreak = 0;
            if (statistics.containsKey(kit.getName())) {
              Document ladder = (Document) statistics.get(kit.getName());
              if (kit.isRanked()) {
                elo = ladder.getInteger("elo");
              }
              if (ladder.containsKey("currentStreak")) {
                winStreak = ladder.getInteger("currentStreak");
              }
            }

            Leaderboard leaderboard = new Leaderboard(elo, winStreak, uuid, username, kit);
            leaderboards.add(leaderboard);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public void updateLeaderboards() {
    leaderboards.clear();
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::createLeaderboards);
    if (plugin.getSettingsConfig().getConfig()
        .getBoolean("SETTINGS.LEADERBOARDS.SHOW-UPDATE-MESSAGE")) {
      plugin.getServer().broadcastMessage(color(plugin.getSettingsConfig().getConfig()
          .getString("SETTINGS.LEADERBOARDS.UPDATE-MESSAGE")));
    }
  }

  public List<Leaderboard> getKitLeaderboards(Kit kit) {
    List<Leaderboard> leaderboardsKit = Lists.newArrayList();
    this.leaderboards.stream().filter(leaderboard -> leaderboard.getKit() == kit)
        .forEach(leaderboardsKit::add);

    return leaderboardsKit;
  }

  public List<Leaderboard> getSortedKitLeaderboards(Kit kit, String type) {
    List<Leaderboard> leaderboardsKit = Lists.newArrayList();

    try {
      for (Leaderboard leaderboard : this.leaderboards) {
        if (leaderboard.getKit() == kit) {
          leaderboardsKit.add(leaderboard);
        }
      }

      switch (type.toLowerCase()) {
        case "elo":
          leaderboardsKit.sort((leaderboard1, leaderboard2) -> leaderboard2.getPlayerElo()
              - leaderboard1.getPlayerElo());
          break;
        case "winstreak":
          leaderboardsKit.sort((leaderboard1, leaderboard2) -> leaderboard2.getPlayerWinStreak()
              - leaderboard1.getPlayerWinStreak());
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return leaderboardsKit;
  }
}
