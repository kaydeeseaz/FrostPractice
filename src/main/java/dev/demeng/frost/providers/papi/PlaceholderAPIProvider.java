package dev.demeng.frost.providers.papi;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.util.CC;
import dev.demeng.pluginbase.Common;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIProvider extends PlaceholderExpansion {

  private final Frost plugin;

  public PlaceholderAPIProvider(Frost plugin) {
    this.plugin = plugin;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "frost";
  }

  @Override
  public @NotNull String getAuthor() {
    return "Demeng";
  }

  @Override
  public @NotNull String getVersion() {
    return Common.getVersion();
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String onPlaceholderRequest(Player player, @NotNull String identifier) {
    if (player == null) {
      return CC.color("&cNo Data Saved!");
    }

    String[] split = identifier.split("_");

    // %frost_elo_Sumo_0% - Returns the first position in the leaderboard for the Sumo Kit
    if (identifier.contains("elo")) {
      String kitName = split[1];
      int position = Integer.parseInt(split[2]);
      if (position < 0 || position > 9) {
        return CC.color("&cNo data saved!");
      }

      Kit kit = plugin.getManagerHandler().getKitManager().getKit(kitName);
      if (kit == null) {
        return "&7Non-Existent Kit";
      }

      String playerName = plugin.getManagerHandler().getLeaderboardManager()
          .getSortedKitLeaderboards(kit, "elo").get(position).getPlayerName();
      int elo = plugin.getManagerHandler().getLeaderboardManager()
          .getSortedKitLeaderboards(kit, "elo").get(position).getPlayerElo();

      return CC.parse(player,
          plugin.getSettingsConfig().getConfig().getString("PLACEHOLDER-API.PLAYER-KIT-ELO-FORMAT")
              .replace("<kit>", kitName)
              .replace("<player>", playerName)
              .replace("<position>", String.valueOf(position))
              .replace("<elo>", String.valueOf(elo)))
          ;
    }

    // %frost_winstreak_Sumo_0% - Returns the first position in the winstreak leaderboard for the Sumo Kit
    if (identifier.contains("winstreak")) {
      String kitName = split[1];
      int position = Integer.parseInt(split[2]);
      if (position < 0 || position > 9) {
        return CC.color("&cNo data saved!");
      }

      Kit kit = plugin.getManagerHandler().getKitManager().getKit(kitName);
      if (kit == null) {
        return "&7Non-Existent Kit";
      }

      String playerName = plugin.getManagerHandler().getLeaderboardManager()
          .getSortedKitLeaderboards(kit, "winstreak").get(position).getPlayerName();
      int winstreak = plugin.getManagerHandler().getLeaderboardManager()
          .getSortedKitLeaderboards(kit, "winstreak").get(position).getPlayerWinStreak();

      return CC.parse(player, plugin.getSettingsConfig().getConfig()
          .getString("PLACEHOLDER-API.PLAYER-KIT-WINSTREAK-FORMAT")
          .replace("<kit>", kitName)
          .replace("<player>", playerName)
          .replace("<position>", String.valueOf(position))
          .replace("<wins>", String.valueOf(winstreak)))
          ;
    }

    // %frost_playing_unranked_Sumo% - Returns the amount of players playing the Sumo kit in unranked
    // %frost_playing_ranked_Sumo% - Returns the amount of players playing the Sumo kit in ranked
    if (identifier.contains("playing")) {
      return String.valueOf(plugin.getManagerHandler().getMatchManager()
          .getFighters(split[1], QueueType.valueOf(split[2].toUpperCase())));
    }

    // %frost_queueing_unranked_Sumo% - Returns the amount of players queueing the Sumo kit in unranked
    // %frost_queueing_ranked_Sumo% - Returns the amount of players queueing the Sumo kit in ranked
    if (identifier.contains("queueing")) {
      return String.valueOf(plugin.getManagerHandler().getQueueManager()
          .getQueueSize(split[1], QueueType.valueOf(split[2].toUpperCase())));
    }

    return null;
  }
}
