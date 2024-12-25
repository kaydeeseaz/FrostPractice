package dev.demeng.frost.runnable;

import static dev.demeng.frost.util.CC.sendTitle;

import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CustomMatchRespawnRunnable extends BukkitRunnable {

  private final Player player;
  private final PracticePlayerData practicePlayerData;

  private final Match match;
  private final MatchTeam playerTeam;
  private final PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS,
      Integer.MAX_VALUE, 0);

  private final int startingTime;
  private int respawnTime;

  public CustomMatchRespawnRunnable(Player player, PracticePlayerData practicePlayerData,
      Match match, MatchTeam playerTeam, int startingTime, int respawnTime) {
    this.player = player;
    this.practicePlayerData = practicePlayerData;

    this.match = match;
    this.playerTeam = playerTeam;

    this.startingTime = startingTime;
    this.respawnTime = respawnTime;
  }

  @Override
  public void run() {
    if (!practicePlayerData.isInMatch()) {
      cancel();
      return;
    }

    if (respawnTime <= 1) {
      player.removePotionEffect(PotionEffectType.WEAKNESS);
      player.teleport(
          playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
              : match.getStandaloneArena().getB().toBukkitLocation());

      match.getTeams().forEach(team -> team.alivePlayers()
          .forEach(matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, player, false)));

      player.setFallDistance(50);
      player.setAllowFlight(false);
      player.setFlying(false);

      sendTitle(player, "&aRespawning...", "");
      player.sendMessage(CC.color("&aYou have respawned!"));
      player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10, 1);
      player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, 0));

      match.getTeams().forEach(
          team -> team.alivePlayers().filter(player1 -> !player.equals(player1)).forEach(
              matchplayer -> matchplayer.sendMessage(CC.color(playerTeam.getTeamID() == 1 ? "&9"
                  : "&c" + player.getName() + "&a has respawned!"))));

      player.resetMaxHealth();
      player.setHealth(player.getMaxHealth());
      player.setFoodLevel(20);

      match.getKit().applyKit(player, practicePlayerData);

      cancel();
      return;
    }

    if (respawnTime == startingTime) {
      match.getTeams().forEach(team -> team.alivePlayers()
          .forEach(matchplayer -> PlayerUtil.hideOrShowPlayer(matchplayer, player, true)));

      player.addPotionEffect(weakness);
      player.getInventory().clear();
      player.updateInventory();

      player.setHealth(player.getMaxHealth());
      player.setFoodLevel(20);

      player.setVelocity(player.getVelocity().add(new Vector(0, 0.25, 0)));
      player.setAllowFlight(true);
      player.setFlying(true);
      player.setVelocity(player.getVelocity().add(new Vector(0, 0.15, 0)));
      player.setAllowFlight(true);
      player.setFlying(true);
    }

    respawnTime--;
    sendTitle(player, "&a" + respawnTime, "");
    player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.7f, 1.0f);
  }
}
