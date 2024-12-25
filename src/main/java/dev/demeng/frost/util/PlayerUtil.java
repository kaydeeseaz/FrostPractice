package dev.demeng.frost.util;

import com.google.common.collect.Sets;
import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.leaderboard.style.minehq.MHQLeaderboardsMenu;
import dev.demeng.frost.user.ui.leaderboard.style.minemen.MMCLeaderboardsMenu;
import dev.demeng.frost.user.ui.leaderboard.style.silex.SilexLeaderboardsMenu;
import dev.demeng.frost.user.ui.leaderboard.winstreak.style.minehq.MHQWinstreaksMenu;
import dev.demeng.frost.user.ui.leaderboard.winstreak.style.minemen.MMCWinstreaksMenu;
import dev.demeng.frost.user.ui.leaderboard.winstreak.style.silex.SilexWinstreaksMenu;
import dev.demeng.frost.user.ui.profile.style.minehq.MHQProfileMenu;
import dev.demeng.frost.user.ui.profile.style.minemen.MMCProfileMenu;
import dev.demeng.frost.user.ui.profile.style.silex.SilexProfileMenu;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@UtilityClass
public final class PlayerUtil {

  private Field SPAWN_PACKET_ID_FIELD;
  private Field STATUS_PACKET_ID_FIELD;
  private Field STATUS_PACKET_STATUS_FIELD;

  public final String PLAYER_NOT_FOUND = ChatColor.RED + "%s not found.";
  private final Set<Material> liquids = Sets.newHashSet(Material.WATER, Material.STATIONARY_WATER,
      Material.LAVA, Material.STATIONARY_LAVA);

  public boolean isStandingOn(Player player, Material material) {
    Block legs = player.getLocation().getBlock();
    Block head = player.getEyeLocation().getBlock();

    return legs.getType() == material || head.getType() == material;
  }

  public boolean isStandingOnLiquid(Player player) {
    Block legs = player.getLocation().getBlock();
    Block head = player.getEyeLocation().getBlock();

    return liquids.contains(legs.getType()) || liquids.contains(head.getType());
  }

  public boolean isInventoryEmpty(Inventory inv) {
    ItemStack[] contents;
    for (int length = (contents = inv.getContents()).length, i = 0; i < length; ++i) {
      final ItemStack item = contents[i];
      if (item != null && item.getType() != Material.AIR) {
        return false;
      }
    }

    return true;
  }

  public void lockPos(Player player, int seconds) {
    player.setFlying(false);
    player.setSprinting(false);
    player.setWalkSpeed(0.0F);
    player.setFoodLevel(0);

    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * seconds, 250));
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * seconds, 250));

    Bukkit.getServer().getScheduler().runTaskLater(Frost.getInstance(), () -> {
      player.setFlying(false);
      player.setSprinting(true);
      player.setWalkSpeed(0.2F);
      player.setFoodLevel(20);
    }, seconds * 20L);
  }

  public String getBridgesScore(int point, boolean friend) {
    String icon = Frost.getInstance().getScoreboardConfig().getConfig()
        .getString("SCOREBOARD.BRIDGES-GOAL");
    String color = friend ? "&9" : "&c";
    String defaultString = "&7";

    String text = icon + icon + icon + icon + icon;
    switch (point) {
      case 1:
        text = color + icon + defaultString + icon + icon + icon + icon;
        break;
      case 2:
        text = color + icon + icon + defaultString + icon + icon + icon;
        break;
      case 3:
        text = color + icon + icon + icon + defaultString + icon + icon;
        break;
      case 4:
        text = color + icon + icon + icon + icon + defaultString + icon;
        break;
      case 5:
        text = color + text;
        break;
    }

    return defaultString + text;
  }

  public String getBedWarsScore(boolean bed, boolean friend) {
    String alive = Frost.getInstance().getScoreboardConfig().getConfig()
        .getString("SCOREBOARD.BEDWARS-ALIVE");
    String dead = Frost.getInstance().getScoreboardConfig().getConfig()
        .getString("SCOREBOARD.BEDWARS-DEAD");

    String text = alive;
    if (!bed) {
      text = CC.color("&c" + dead);
    }

    return CC.color("&a" + text);
  }

  public String getBattleRushScore(int point, boolean friend) {
    String icon = Frost.getInstance().getScoreboardConfig().getConfig()
        .getString("SCOREBOARD.BRIDGES-GOAL");
    String color = friend ? "&9" : "&c";
    String defaultString = "&7";

    String text = icon + icon + icon;
    switch (point) {
      case 1:
        text = color + icon + defaultString + icon + icon;
        break;
      case 2:
        text = color + icon + icon + defaultString + icon;
        break;
      case 3:
        text = color + text;
        break;
    }

    return defaultString + text;
  }

  public int getQueuePing(Player player, PracticePlayerData practicePlayerData) {
    return player.hasPermission("frost.vip.ping_range") ? practicePlayerData.getPlayerSettings()
        .getPingRange() : -1;
  }

  public void hideOrShowPlayer(Player player, Player target, boolean hide) {
    CraftPlayer craftPlayer = (CraftPlayer) target.getPlayer();

    if (hide) {
      player.hidePlayer(target);
      ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
          new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
              craftPlayer.getHandle()));
    } else {
      player.showPlayer(target);
    }
  }

  public void clearPlayer(Player player, boolean atSpawn) {
    player.closeInventory();
    player.setFireTicks(0);
    player.setHealth(20.0D);
    player.setFoodLevel(20);
    player.setSaturation(12.8F);
    player.setFallDistance(0.0F);
    player.setLevel(0);
    player.setExp(0.0F);
    player.setWalkSpeed(0.2F);
    player.setFlySpeed(0.2F);
    if (!atSpawn) {
      player.setAllowFlight(false);
    }
    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    player.spigot().setCollidesWithEntities(true);
    player.setGameMode(GameMode.SURVIVAL);
    player.setMaximumNoDamageTicks(20);
    player.getActivePotionEffects().stream().map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
    ((CraftPlayer) player).getHandle().getDataWatcher()
        .watch(9, (byte) 0); // removes players arrows on body
    player.updateInventory();
  }

  public void playDeathAnimation(Player player) {
    int entityId = EntityUtils.getFakeEntityId();
    PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(
        ((CraftPlayer) player).getHandle());
    PacketPlayOutEntityStatus statusPacket = new PacketPlayOutEntityStatus();

    try {
      SPAWN_PACKET_ID_FIELD.set(spawnPacket, entityId);
      STATUS_PACKET_ID_FIELD.set(statusPacket, entityId);
      STATUS_PACKET_STATUS_FIELD.set(statusPacket, (byte) 3);
      int radius = MinecraftServer.getServer().getPlayerList().d();
      Set<Player> sentTo = new HashSet<>();

      for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
        if (entity instanceof Player) {
          Player watcher = (Player) entity;
          if (!watcher.getUniqueId().equals(player.getUniqueId())) {
            ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(spawnPacket);
            ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(statusPacket);
            sentTo.add(watcher);
          }
        }
      }

      Bukkit.getScheduler().runTaskLater(Frost.getInstance(), () -> {
        for (Player watcher : sentTo) {
          ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(
              new PacketPlayOutEntityDestroy(entityId));
        }
      }, 40L);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getPing(Player player) {
    return ((CraftPlayer) player).getHandle().ping;
  }

  public int getPing(String playerName) {
    return ((CraftPlayer) Bukkit.getPlayer(playerName)).getHandle().ping;
  }

  public void getStyle(Player player, Player target, String type, Frost plugin) {
    String style = plugin.getSettingsConfig().getConfig().getString("SETTINGS.LEADERBOARDS.STYLE");

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
      switch (type) {
        case "STATS":
          switch (style) {
            case "MHQ":
              new MHQProfileMenu(target).openMenu(player);
              break;
            case "MMC":
              new MMCProfileMenu(target).openMenu(player);
              break;
            case "Silex":
              new SilexProfileMenu(target).openMenu(player);
              break;
          }
          break;
        case "ELO":
          switch (style) {
            case "MHQ":
              new MHQLeaderboardsMenu().openMenu(player);
              break;
            case "MMC":
              new MMCLeaderboardsMenu().openMenu(player);
              break;
            case "Silex":
              new SilexLeaderboardsMenu().openMenu(player);
              break;
          }
          break;
        case "STREAK":
          switch (style) {
            case "MHQ":
              new MHQWinstreaksMenu().openMenu(player);
              break;
            case "MMC":
              new MMCWinstreaksMenu().openMenu(player);
              break;
            case "Silex":
              new SilexWinstreaksMenu().openMenu(player);
              break;
          }
          break;
      }
    });
  }

  static {
    try {
      STATUS_PACKET_ID_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("a");
      STATUS_PACKET_ID_FIELD.setAccessible(true);

      STATUS_PACKET_STATUS_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("b");
      STATUS_PACKET_STATUS_FIELD.setAccessible(true);

      SPAWN_PACKET_ID_FIELD = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("a");
      SPAWN_PACKET_ID_FIELD.setAccessible(true);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }
}
