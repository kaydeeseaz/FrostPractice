package dev.demeng.frost.user.effects;

import dev.demeng.frost.Frost;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityLightning;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityWeather;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@Getter
public enum SpecialEffects {

  // Extra Information: The TRUE value in the Packet Spawning belongs to its force, meaning that if it's true its visible from 128 blocks away
  NONE("None", Material.RECORD_11, "", (player, watchers) -> {
  }),
  BLOOD("Blood", Material.REDSTONE, "frost.effects.blood", (player, watchers) -> {
    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.BLOCK_CRACK,
        false, (float) player.getLocation().getX(), (float) player.getLocation().getY(),
        (float) player.getLocation().getZ(), 0.2f, 0.2f, 0.2f, 1.0f, 20,
        Material.REDSTONE_BLOCK.getId());
    for (Player watcher : watchers) {
      for (int i = 0; i < 5; i++) {
        ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);
      }
      watcher.playSound(player.getLocation(), Sound.FALL_BIG, 1.0f, 0.5f);
    }
  }),
  EXPLOSION("Explosion", Material.TNT, "frost.effects.explosion", (player, watchers) -> {
    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
        EnumParticle.EXPLOSION_LARGE, false, (float) player.getLocation().getX(),
        (float) player.getLocation().getY(), (float) player.getLocation().getZ(), 0.2f, 0.2f, 0.2f,
        1.0f, 20);
    for (Player watcher : watchers) {
      ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);
      watcher.playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 0.7f);
    }
  }),
  LIGHTNING("Lightning", Material.BEACON, "frost.effects.lightning", (player, watchers) -> {
    EntityLightning entityLightning = new EntityLightning(
        ((CraftPlayer) player).getHandle().getWorld(), player.getLocation().getX(),
        player.getLocation().getY(), player.getLocation().getZ());
    PacketPlayOutSpawnEntityWeather lightning = new PacketPlayOutSpawnEntityWeather(
        entityLightning);
    PacketPlayOutNamedSoundEffect lightningSound = new PacketPlayOutNamedSoundEffect(
        "ambient.weather.thunder", player.getLocation().getX(), player.getLocation().getY(),
        player.getLocation().getZ(), 10000.0F, 63);
    PacketPlayOutWorldParticles cloud = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, false,
        (float) player.getLocation().getX(), (float) player.getLocation().getY(),
        (float) player.getLocation().getZ(), 0.5f, 0.5f, 0.5f, 0.1f, 10);
    PacketPlayOutWorldParticles flame = new PacketPlayOutWorldParticles(EnumParticle.FLAME, false,
        (float) player.getLocation().getX(), (float) player.getLocation().getY(),
        (float) player.getLocation().getZ(), 0.3f, 0.3f, 0.3f, 0.1f, 12);

    for (Player watcher : watchers) {
      ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(lightning);
      ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(lightningSound);
      ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(cloud);
      ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(flame);
    }
  }),
  FLAME("Flame", Material.BLAZE_POWDER, "frost.effects.flame", (player, watchers) -> {
    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.FLAME, false,
        (float) player.getLocation().getX(), (float) player.getLocation().getY(),
        (float) player.getLocation().getZ(), 0.5f, 0.5f, 0.5f, 0.1f, 20);
    for (Player watcher : watchers) {
      ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);
    }
  }),
  PINATA("Pinata", Material.STICK, "frost.effects.pinata", (player, watchers) -> {
    final byte[] colors = new byte[]{1, 2, 4, 5, 6, 9, 10, 11, 12, 13, 14, 15};
    for (byte itemData : colors) {
      PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.ITEM_CRACK,
          false, (float) player.getLocation().getX(), (float) player.getLocation().getY(),
          (float) player.getLocation().getZ(), 0f, 0f, 0f, 0.5f, 10, Material.INK_SACK.getId(),
          itemData);
      for (Player watcher : watchers) {
        ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);
        watcher.playSound(player.getLocation(), Sound.FIREWORK_LARGE_BLAST, 1.0f, 0.7f);
      }
    }
  }),
  SHATTERED("Shattered", Material.GLASS, "frost.effects.shattered", (player, watchers) -> {
    final byte[] grayscale = new byte[]{0, 7, 8, 15};
    for (byte itemData : grayscale) {
      PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.ITEM_CRACK,
          false, (float) player.getLocation().getX(),
          (float) player.getLocation().add(0, 1, 0).getY(), (float) player.getLocation().getZ(), 0f,
          0f, 0f, 0.5f, 20, Material.STAINED_GLASS.getId(), itemData);
      for (Player watcher : watchers) {
        ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);
        watcher.playSound(player.getLocation(), Sound.GLASS, 1.0f, 0.55f);
      }
    }
  }),
  SHOCKWAVE("Shockwave", Material.FIREWORK_CHARGE, "frost.effects.shockwave",
      (player, watchers) -> {
        final Location loc = player.getLocation().clone();
        Material block = loc.getBlock().getRelative(BlockFace.DOWN).getType();
        if (block == Material.AIR) {
          block = Material.ICE;
        }

        final Material finalBlock = block;
        new BukkitRunnable() {
          int i = 0;
          double radius = 0.5;

          public void run() {
            radius += 0.5;

            for (double t = 0; t < 50; t += 1.5) {
              double x = (radius * Math.sin(t));
              double z = (radius * Math.cos(t));

              PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                  EnumParticle.BLOCK_CRACK, false, (float) (loc.getX() + x), (float) loc.getY(),
                  (float) (loc.getZ() + z), 0.0f, 0.0f, 0.0f, 1.0f, 6, finalBlock.getId());
              for (Player watcher : watchers) {
                ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);
                watcher.playSound(loc, Sound.DIG_GRAVEL, 0.3f, 0.45f);
              }
            }

            i++;
            if (i >= 4) {
              this.cancel();
            }
          }
        }.runTaskTimerAsynchronously(Frost.getInstance(), 0L, 5L);
      }),
  WISDOM("Wisdom", Material.BOOK, "frost.effects.wisdom", (player, watchers) -> {
    Location loc = player.getLocation().clone().add(0, 2.8, 0);
    for (double d = 0; d < Math.PI * 2; d += Math.PI / 6) {
      double x = Math.sin(d);
      double z = Math.cos(d);
      Vector v = new Vector(x, -0.5, z).multiply(1.5);

      PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
          EnumParticle.ENCHANTMENT_TABLE, false, (float) loc.getX(), (float) loc.getY(),
          (float) loc.getZ(), (float) v.getX(), (float) v.getY(), (float) v.getZ(), 0.7f, 0);
      PacketPlayOutWorldParticles books = new PacketPlayOutWorldParticles(EnumParticle.ITEM_CRACK,
          false, (float) player.getLocation().getX(), (float) player.getLocation().getY(),
          (float) player.getLocation().getZ(), 0f, 0f, 0f, 0.5f, 1, Material.BOOK.getId(), 0);
      for (Player watcher : watchers) {
        for (int i = 0; i < 5; i++) {
          ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);
        }
        ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(books);
        watcher.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 0.3f, 0.4f);
      }
    }
  }),
  SOUL("Soul", Material.MAGMA_CREAM, "frost.effects.soul", (player, watchers) -> {
    new BukkitRunnable() {
      final Location loc = player.getLocation();

      double t = 0;
      final double r = 0.75;

      public void run() {
        t = t + Math.PI / 10;
        double x = r * Math.cos(t);
        double y = 0.25 * t;
        double z = r * Math.sin(t);
        loc.add(x, y, z);

        for (Player watcher : watchers) {
          PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
              EnumParticle.VILLAGER_HAPPY, false, (float) loc.getX(), (float) loc.getY(),
              (float) loc.getZ(), 0.0f, 0.0f, 0.0f, 1.0f, 0);
          ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(packet);

          if (t > Math.PI * 3) {
            PacketPlayOutWorldParticles pop = new PacketPlayOutWorldParticles(EnumParticle.HEART,
                false, (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0.0f, 0.0f, 0.0f,
                1.0f, 0);
            ((CraftPlayer) watcher).getHandle().playerConnection.sendPacket(pop);

            Bukkit.getScheduler().runTaskLater(Frost.getInstance(),
                () -> watcher.playSound(loc, Sound.NOTE_PLING, 1.0f, 1.6f), 1L);
            Bukkit.getScheduler().runTaskLater(Frost.getInstance(),
                () -> watcher.playSound(loc, Sound.NOTE_PLING, 1.0f, 1.7f), 3L);
            Bukkit.getScheduler().runTaskLater(Frost.getInstance(),
                () -> watcher.playSound(loc, Sound.NOTE_PLING, 1.0f, 1.8f), 5L);
            Bukkit.getScheduler().runTaskLater(Frost.getInstance(),
                () -> watcher.playSound(loc, Sound.NOTE_PLING, 1.0f, 1.9f), 7L);
            Bukkit.getScheduler().runTaskLater(Frost.getInstance(),
                () -> watcher.playSound(loc, Sound.NOTE_PLING, 1.0f, 2.0f), 9L);

            this.cancel();
          } else {
            loc.subtract(x, y, z);
          }
        }
      }
    }.runTaskTimerAsynchronously(Frost.getInstance(), 0L, 1L);
  });

  private final String name;
  private final Material icon;
  private final String permission;
  private final EffectCallable callable;

  SpecialEffects(String name, Material icon, String permission, EffectCallable callable) {
    this.name = name;
    this.icon = icon;
    this.permission = permission;
    this.callable = callable;
  }

  public static SpecialEffects getByName(String input) {
    for (SpecialEffects type : values()) {
      if (type.name().equalsIgnoreCase(input) || type.getName().equalsIgnoreCase(input)) {
        return type;
      }
    }

    return null;
  }

  public boolean hasPermission(Player player) {
    return player.hasPermission(permission) || permission.isEmpty();
  }
}
