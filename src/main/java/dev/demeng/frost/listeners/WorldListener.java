package dev.demeng.frost.listeners;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.EventState;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.skywars.SkyWarsEvent;
import dev.demeng.frost.events.games.spleef.SpleefEvent;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.game.event.match.MatchEndEvent;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldListener implements Listener {

  private final Frost plugin = Frost.getInstance();

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData == null) {
      plugin.getLogger().warning(player.getName() + "'s player data is null (BlockBreakEvent)");
      event.setCancelled(true);
      return;
    }

    if (practicePlayerData.getPlayerState() == PlayerState.FIGHTING) {
      if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
        event.setCancelled(true);
        return;
      }

      Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
      if (match == null) {
        event.setCancelled(true);
        return;
      }
      if (match.getMatchState() != MatchState.FIGHTING) {
        event.setCancelled(true);
        return;
      }
      if (!match.getKit().isBuild()) {
        event.setCancelled(true);
        return;
      }
      if (!match.isBreakable(event.getBlock())) {
        event.setCancelled(true);
        return;
      }

      if (event.getBlock().getType() == Material.BED_BLOCK) {
        ConfigCursor matchMessage = new ConfigCursor(Frost.getInstance().getMessagesConfig(),
            "MESSAGES.MATCH");
        MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
        MatchTeam opposingTeam = match.isFFA() ? match.getTeams().get(0)
            : ((practicePlayerData.getTeamId() == 0) ? match.getTeams().get(1)
                : match.getTeams().get(0));
        if ((playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
            : match.getStandaloneArena().getB().toBukkitLocation()).distance(
            event.getBlock().getLocation()) > 20.0) {
          if (match.getKit().isBedWars()) {
            if (playerTeam.getPlayers().contains(player.getUniqueId()) && !match.isEnding()
                && opposingTeam.isHasBed()) {
              opposingTeam.destroyBed();
              match.broadcast(playerTeam.getTeamID() == 1 ? "&9"
                  : "&c" + player.getName() + " &7destroyed the bed of " + (
                      playerTeam.getTeamID() == 1 ? "&cRed" : "&9Blue") + "&7!");
              match.broadcastTitle(
                  playerTeam.getTeamID() == 1 ? "&cRed's" : "&9Blue's" + " destroyed",
                  "&7By " + (playerTeam.getTeamID() == 1 ? "&9" : "&c") + player.getName());
              match.broadcastSound(Sound.ENDERDRAGON_GROWL);
              return;
            }
          }

          if (match.getKit().isMlgRush()) {
            event.setCancelled(true);
            playerTeam.addPoint();
            if (playerTeam.getBridgesPoints() >= 5) {
              match.setCanContinue(false);
              if (!match.isCanContinue()) {
                Bukkit.getPluginManager()
                    .callEvent(new MatchEndEvent(match, playerTeam, opposingTeam));
                return;
              }

              return;
            }

            match.setCountdown(6);
            plugin.getManagerHandler().getMatchManager().clearBlocks(match);
            match.broadcast(playerTeam.getTeamID() == 1 ? "&9"
                : "&c" + player.getName() + " &ascored a point!");

            new BukkitRunnable() {
              @Override
              public void run() {
                if (!practicePlayerData.isInMatch()) {
                  cancel();
                  return;
                }
                if (match.getCountdown() <= 1) {
                  match.getTeams()
                      .forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
                        Player playerBukkitTeam = Bukkit.getPlayer(uuid);
                        if (playerBukkitTeam == null) {
                          return;
                        }

                        playerBukkitTeam.setMaxHealth(playerBukkitTeam.getMaxHealth());
                        playerBukkitTeam.setFoodLevel(20);
                      }));

                  match.broadcastSound(Sound.FIREWORK_BLAST);
                  cancel();

                  return;
                }

                if (match.getCountdown() == 6) {
                  match.getTeams()
                      .forEach(matchTeam -> matchTeam.getAlivePlayers().forEach(uuid -> {
                        Player mlgrushPlayer = Bukkit.getPlayer(uuid);
                        if (mlgrushPlayer == null) {
                          return;
                        }

                        player.resetMaxHealth();
                        player.setHealth(player.getMaxHealth());
                        player.setFoodLevel(20);

                        match.getKit().applyKit(player, practicePlayerData);

                        PlayerUtil.lockPos(mlgrushPlayer, 5);
                        mlgrushPlayer.teleport(
                            matchTeam.getTeamID() == 1 ? match.getStandaloneArena().getA()
                                .toBukkitLocation()
                                : match.getStandaloneArena().getB().toBukkitLocation());
                      }));
                }

                match.setCountdown(match.getCountdown() - 1);
                match.broadcastTitle(
                    matchMessage.getString("BRIDGE-COUNTDOWN-TITLE")
                        .replace("<team_color>", playerTeam.getTeamID() == 1 ? "&9" : "&c")
                        .replace("<match_countdown>", String.valueOf(match.getCountdown()))
                        .replace("<player>", player.getName()),
                    matchMessage.getString("BRIDGE-COUNTDOWN-SUBTITLE")
                        .replace("<match_countdown>", String.valueOf(match.getCountdown()))
                        .replace("<scores>", (playerTeam.getTeamID() == 1 ? "&9" : "&c")
                            + playerTeam.getBridgesPoints() + " &7- " + (
                            opposingTeam.getTeamID() == 1 ? "&9" : "&c")
                            + opposingTeam.getBridgesPoints())
                );
                match.broadcastWithSound(matchMessage.getString("BRIDGE-COUNTDOWN")
                        .replace("<match_countdown>", String.valueOf(match.getCountdown())),
                    Sound.NOTE_PLING);
              }
            }.runTaskTimer(plugin, 0L, 20L);
          }
        } else {
          event.setCancelled(true);
          return;
        }

        return;
      }

      player.getInventory().addItem(event.getBlock().getDrops().toArray(new ItemStack[0]));
      match.removePlacedBlock(event.getBlock());
      event.getBlock().setType(Material.AIR);

      return;
    } else if (practicePlayerData.getPlayerState() == PlayerState.EVENT) {
      PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
          .getOngoingEvent();
      if (practiceEvent instanceof SkyWarsEvent) {
        if (practiceEvent.getState() == EventState.STARTED) {
          event.setCancelled(false);
          return;
        }
      } else if (practiceEvent instanceof SpleefEvent) {
        if (practiceEvent.getState() == EventState.STARTED) {
          if (event.getBlock().getType() == Material.SNOW_BLOCK) {
            SpleefEvent spleefEvent = (SpleefEvent) plugin.getManagerHandler().getEventManager()
                .getOngoingEvent();
            spleefEvent.getBrokenBlocks().add(event.getBlock().getLocation());
            event.getBlock().setType(Material.AIR);
            event.setCancelled(true);
            return;
          }
        }
      }
    }

    if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData == null) {
      plugin.getLogger().warning(player.getName() + "'s player data is null (BlockPlaceEvent)");
      event.setCancelled(true);
      return;
    }

    switch (practicePlayerData.getPlayerState()) {
      case SPAWN:
        if (!player.isOp() && player.getGameMode() != GameMode.CREATIVE) {
          event.setCancelled(true);
        }
        break;
      case FIGHTING:
        if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
          event.setCancelled(true);
          return;
        }

        Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
        if (match == null) {
          event.setCancelled(true);
          return;
        }
        if (match.getMatchState() != MatchState.FIGHTING) {
          event.setCancelled(true);
          return;
        }
        if (!match.getKit().isBuild()) {
          event.setCancelled(true);
          return;
        }
        if (match.isPlaceable(player, match)) {
          event.setCancelled(true);
          return;
        }

        Block block = event.getBlock();
        if (block.getLocation().getBlockY() >= match.getArena().getBuildMax()) {
          event.setCancelled(true);
          return;
        }
        if (!canPlaceBlock(match.getArena(), block.getLocation())) {
          event.setCancelled(true);
          return;
        }

        Location teamA = match.getStandaloneArena().getA().toBukkitLocation().getBlock()
            .getLocation();
        Location teamB = match.getStandaloneArena().getB().toBukkitLocation().getBlock()
            .getLocation();
        if (teamA.equals(block.getLocation()) || teamA.add(0, 1, 0).equals(block.getLocation())
            || teamB.equals(block.getLocation()) || teamB.add(0, 1, 0)
            .equals(block.getLocation())) {
          event.setCancelled(true);
          return;
        }

        if (match.getKit().isBattleRush() || match.getKit().isStickFight()) {
          Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!practicePlayerData.isInMatch()) {
              return;
            }

            player.getInventory().addItem(block.getDrops().toArray(new ItemStack[0]));
            block.setType(Material.AIR);
            match.removePlacedBlock(block);
          }, 200L);
        }

        match.addPlacedBlock(event.getBlock());
        break;
      case EVENT:
        PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
            .getOngoingEvent();
        if (practiceEvent instanceof SkyWarsEvent
            && practiceEvent.getState() != EventState.STARTED) {
          event.setCancelled(true);
        }
        break;
    }
  }

  @EventHandler
  public void onBucketEmpty(PlayerBucketEmptyEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData == null) {
      plugin.getLogger()
          .warning(player.getName() + "'s player data is null (PlayerBucketEmptyEvent)");
      event.setCancelled(true);
      return;
    }

    if (practicePlayerData.getPlayerState() == PlayerState.FIGHTING) {
      Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
      if (match == null) {
        event.setCancelled(true);
        return;
      }
      if (match.getMatchState() != MatchState.FIGHTING) {
        event.setCancelled(true);
        return;
      }
      if (!match.getKit().isBuild()) {
        event.setCancelled(true);
        return;
      }
      if (match.isPlaceable(player, match)) {
        event.setCancelled(true);
        return;
      }
      if (event.getBlockClicked().getRelative(event.getBlockFace()).getLocation().getBlockY()
          >= match.getArena().getBuildMax()) {
        event.setCancelled(true);
        return;
      }

      match.addPlacedBlock(event.getBlockClicked().getRelative(event.getBlockFace()));
      return;
    } else if (practicePlayerData.getPlayerState() == PlayerState.EVENT) {
      PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
          .getOngoingEvent();
      if (practiceEvent instanceof SkyWarsEvent) {
        if (practiceEvent.getState() == EventState.STARTED) {
          event.setCancelled(false);
          return;
        }
      }
    }

    if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockFromTo(BlockFromToEvent event) {
    if (event.getToBlock() == null) {
      return;
    }

    for (StandaloneArena arena : plugin.getManagerHandler().getArenaManager().getArenaMatchUUIDs()
        .keySet()) {
      double minX = arena.getMin().getX();
      double minZ = arena.getMin().getZ();
      double maxX = arena.getMax().getX();
      double maxZ = arena.getMax().getZ();
      if (minX > maxX) {
        double lastMinX = minX;
        minX = maxX;
        maxX = lastMinX;
      }
      if (minZ > maxZ) {
        double lastMinZ = minZ;
        minZ = maxZ;
        maxZ = lastMinZ;
      }

      if (event.getToBlock().getX() >= minX && event.getToBlock().getZ() >= minZ
          && event.getToBlock().getX() <= maxX && event.getToBlock().getZ() <= maxZ) {
        UUID matchUUID = plugin.getManagerHandler().getArenaManager().getArenaMatchUUID(arena);
        Match match = plugin.getManagerHandler().getMatchManager().getMatchFromUUID(matchUUID);
        match.addPlacedBlock(event.getToBlock());
        break;
      }
    }
  }

  @EventHandler
  public void onWeatherChange(WeatherChangeEvent event) {
    if (event.toWeatherState()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onLeavesDecay(LeavesDecayEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onHangingBreak(HangingBreakEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onBlockBurn(BlockBurnEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onBlockSpread(BlockSpreadEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onBlockIgnite(BlockIgniteEvent event) {
    if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onCropsTrampling(PlayerInteractEvent event) {
    if (event.getAction() == Action.PHYSICAL
        && event.getClickedBlock().getType() == Material.SOIL) {
      event.setCancelled(true);
    }
  }

  private boolean canPlaceBlock(Arena arena, Location location) {
    for (Block entity : getBlocksAroundCenter(location, arena.getPortalProt())) {
      if (entity.getTypeId() == 119 || entity.getTypeId() == 120) {
        return false;
      }
    }

    return true;
  }

  private List<Block> getBlocksAroundCenter(Location loc, int radius) {
    List<Block> blocks = new ArrayList<>();

    for (int x = (loc.getBlockX() - radius); x <= (loc.getBlockX() + radius); x++) {
      for (int y = (loc.getBlockY() - radius); y <= (loc.getBlockY() + radius); y++) {
        for (int z = (loc.getBlockZ() - radius); z <= (loc.getBlockZ() + radius); z++) {
          Location l = new Location(loc.getWorld(), x, y, z);
          if (l.distance(loc) <= radius) {
            blocks.add(l.getBlock());
          }
        }
      }
    }

    return blocks;
  }
}
