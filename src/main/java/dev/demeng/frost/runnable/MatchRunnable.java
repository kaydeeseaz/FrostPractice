package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.util.config.ConfigCursor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class MatchRunnable extends BukkitRunnable {

  private final Frost plugin = Frost.getInstance();
  private final Match match;
  private final ConfigCursor matchMessage = new ConfigCursor(
      Frost.getInstance().getMessagesConfig(), "MESSAGES.MATCH");

  @Override
  public void run() {
    switch (this.match.getMatchState()) {
      case STARTING:
        if (this.match.decrementCountdown() == 0) {
          this.match.setMatchState(MatchState.FIGHTING);
          this.match.broadcastWithSound(matchMessage.getString("COUNTDOWN-STARTED"),
              Sound.FIREWORK_BLAST);
          if (this.match.getKit().isBuild() || this.match.getKit().isBuild() && this.match.getKit()
              .getName().contains("UHC")) {
            this.match.broadcast(
                plugin.getMessagesConfig().getConfig().getString("MESSAGES.MATCH.BUILD-ALERT"));
          } else if (this.match.getKit().isBoxing()) {
            this.match.broadcast(
                plugin.getMessagesConfig().getConfig().getString("MESSAGES.MATCH.BOXING-ALERT"));
          } else {
            this.match.broadcast(
                plugin.getMessagesConfig().getConfig().getString("MESSAGES.MATCH.COUNTDOWN-ALERT"));
          }
        } else {
          this.match.broadcastWithSound(matchMessage.getString("COUNTDOWN")
                  .replace("<match_countdown>", String.valueOf(this.match.getCountdown())),
              Sound.NOTE_PLING);
          this.match.broadcastTitle(
              matchMessage.getString("COUNTDOWN-TITLE")
                  .replace("<match_countdown>", String.valueOf(this.match.getCountdown())),
              matchMessage.getString("COUNTDOWN-SUBTITLE")
                  .replace("<match_countdown>", String.valueOf(this.match.getCountdown()))
          );
        }
        break;
      case FIGHTING:
        match.incrementDuration();
        break;
      case ENDING:
        if (this.match.decrementCountdown() == 0) {
          plugin.getManagerHandler().getTournamentManager().removeTournamentMatch(this.match);
          for (Integer id : this.match.getRunnables()) {
            plugin.getServer().getScheduler().cancelTask(id);
          }
          for (Entity entity : this.match.getEntitiesToRemove()) {
            entity.remove();
          }
          for (MatchTeam team : this.match.getTeams()) {
            team.alivePlayers().forEach(player -> plugin.getManagerHandler().getPlayerManager()
                .resetPlayerOrSpawn(player, true));
          }

          this.match.spectatorPlayers()
              .forEach(plugin.getManagerHandler().getMatchManager()::removeSpectator);

          if (this.match.getKit().isBuild() || this.match.getKit().isSpleef() || this.match.getKit()
              .isSkyWars()) {
            ChunkRestorationManager.getIChunkRestoration().reset(match.getStandaloneArena());
            this.match.getArena().addAvailableArena(this.match.getStandaloneArena());
            plugin.getManagerHandler().getArenaManager()
                .removeArenaMatchUUID(this.match.getStandaloneArena());
          }

          plugin.getManagerHandler().getMatchManager().removeMatch(this.match);
          this.cancel();
        }
        break;
    }
  }
}
