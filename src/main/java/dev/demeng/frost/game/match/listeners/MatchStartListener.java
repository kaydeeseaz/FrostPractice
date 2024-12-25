package dev.demeng.frost.game.match.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.event.match.MatchStartEvent;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.managers.PlayerManager;
import dev.demeng.frost.managers.chunk.data.ChunkPacket;
import dev.demeng.frost.runnable.MatchRunnable;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.PlayerUtil;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MatchStartListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler
  public void onMatchStart(MatchStartEvent event) {
    Match match = event.getMatch();
    Kit kit = match.getKit();
    if (!kit.isEnabled()) {
      match.broadcast("&cThis kit is currently disabled.");
      plugin.getManagerHandler().getMatchManager().removeMatch(match);
      return;
    }
    if (kit.isBuild() || kit.isSpleef()) {
      if (match.getArena().getAvailableArenas().size() > 0) {
        match.setStandaloneArena(match.getArena().getAvailableArena());
        plugin.getManagerHandler().getArenaManager()
            .setArenaMatchUUID(match.getStandaloneArena(), match.getMatchId());
      } else {
        match.broadcast("&cThere are no arenas available at this moment.");
        plugin.getManagerHandler().getMatchManager().removeMatch(match);
        return;
      }
    }

    Set<Player> matchPlayers = new HashSet<>();
    for (MatchTeam team : match.getTeams()) {
      team.alivePlayers().forEach(player -> {
        matchPlayers.add(player);

        plugin.getManagerHandler().getMatchManager().removeMatchRequests(player.getUniqueId());

        PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(player.getUniqueId());
        practicePlayerData.getCachedPlayer().clear();
        practicePlayerData.setFollowingId(null);
        practicePlayerData.setFollowing(false);

        player.setAllowFlight(false);
        player.setFlying(false);

        practicePlayerData.setCurrentMatchID(match.getMatchId());
        practicePlayerData.setTeamId(team.getTeamID());

        practicePlayerData.setMissedPots(0);
        practicePlayerData.setThrownPots(0);
        practicePlayerData.setLongestCombo(0);
        practicePlayerData.setCombo(0);
        practicePlayerData.setHits(0);

        PlayerUtil.clearPlayer(player, false);

        CustomLocation locationA =
            match.getStandaloneArena() != null ? match.getStandaloneArena().getA()
                : match.getArena().getA();
        CustomLocation locationB =
            match.getStandaloneArena() != null ? match.getStandaloneArena().getB()
                : match.getArena().getB();
        Location spawn =
            team.getTeamID() == 1 ? locationA.toBukkitLocation() : locationB.toBukkitLocation();

        if (kit.isBridges()) {
          team.setBridgeSpawnLocation(spawn);
        } else if (kit.isStickFight()) {
          team.setLives(kit.getLives());
        }

        final int pX = spawn.getBlockX();
        final int pZ = spawn.getBlockZ();

        final Chunk[] chunks = new Chunk[]{
            player.getLocation().getWorld().getChunkAt(pX, pZ),
            player.getLocation().getWorld().getChunkAt(pX + 16, pZ),
            player.getLocation().getWorld().getChunkAt(pX, pZ + 16),
            player.getLocation().getWorld().getChunkAt(pX + 16, pZ + 16)
        };
        for (Chunk chunk : chunks) {
          for (Player online : Bukkit.getOnlinePlayers()) {
            new ChunkPacket(chunk).send(online);
          }

          player.getLocation().getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        }

        player.teleport(
            team.getTeamID() == 1 ? locationA.toBukkitLocation() : locationB.toBukkitLocation());

        for (ItemStack itemStack : plugin.getManagerHandler().getMatchManager()
            .getKitItems(player, match.getKit(), match)) {
          player.getInventory().addItem(itemStack);
        }

        if (Frost.getInstance().isUsingCustomKB()) {
          PlayerManager.setKnockbackProfile(player,
              kit.getKbProfile() == null ? "default" : kit.getKbProfile());
        }

        if (!kit.getMatchStartCommands().isEmpty()) {
          kit.getMatchStartCommands().forEach(command -> {
            command = command.replace("<player>", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
          });
        }

        practicePlayerData.setPlayerState(PlayerState.FIGHTING);
      });
    }

    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
      for (Player player : matchPlayers) {
        for (Player online : plugin.getServer().getOnlinePlayers()) {
          PlayerUtil.hideOrShowPlayer(online, player, true);
          PlayerUtil.hideOrShowPlayer(player, online, true);
        }
      }

      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
        for (Player player : matchPlayers) {
          for (Player other : matchPlayers) {
            PlayerUtil.hideOrShowPlayer(player, other, false);
          }
        }
      }, 1L);
    }, 15L);

    new MatchRunnable(match).runTaskTimer(plugin, 20L, 20L);
  }
}
