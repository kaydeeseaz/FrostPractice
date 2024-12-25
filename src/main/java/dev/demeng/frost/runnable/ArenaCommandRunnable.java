package dev.demeng.frost.runnable;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.util.CustomLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class ArenaCommandRunnable implements Runnable {

  private final Frost plugin;
  private final Arena copiedArena;

  private int copies;

  @Override
  public void run() {
    this.duplicateArena(this.copiedArena, 10000, 10000);
  }

  private void duplicateArena(Arena arena, int offsetX, int offsetZ) {
    new DuplicateArenaRunnable(plugin, arena, offsetX, offsetZ, 500, 500) {
      @Override
      public void onComplete() {
        double minX = arena.getMin().getX() + this.getOffsetX();
        double minZ = arena.getMin().getZ() + this.getOffsetZ();
        double maxX = arena.getMax().getX() + this.getOffsetX();
        double maxZ = arena.getMax().getZ() + this.getOffsetZ();

        double aX = arena.getA().getX() + this.getOffsetX();
        double aZ = arena.getA().getZ() + this.getOffsetZ();
        double bX = arena.getB().getX() + this.getOffsetX();
        double bZ = arena.getB().getZ() + this.getOffsetZ();

        CustomLocation min = new CustomLocation(minX, arena.getMin().getY(), minZ,
            arena.getMin().getYaw(), arena.getMin().getPitch());
        CustomLocation max = new CustomLocation(maxX, arena.getMax().getY(), maxZ,
            arena.getMax().getYaw(), arena.getMax().getPitch());
        CustomLocation a = new CustomLocation(aX, arena.getA().getY(), aZ, arena.getA().getYaw(),
            arena.getA().getPitch());
        CustomLocation b = new CustomLocation(bX, arena.getB().getY(), bZ, arena.getB().getYaw(),
            arena.getB().getPitch());

        StandaloneArena standaloneArena = new StandaloneArena(a, b, min, max);
        arena.addStandaloneArena(standaloneArena);
        arena.addAvailableArena(standaloneArena);

        String arenaPasteMessage =
            "[Standalone Arena] - " + arena.getName() + " placed at " + (int) minX + ", "
                + (int) minZ + ". " + copies + " copies remaining.";

        if (--copies > 0) {
          plugin.getServer().getLogger().info(arenaPasteMessage);
          for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
              player.sendMessage(ChatColor.GREEN + arenaPasteMessage);
            }
          }
          duplicateArena(arena, (int) Math.round(maxX), (int) Math.round(maxZ));
        } else {
          for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp()) {
              player.sendMessage(ChatColor.GREEN + "All the copies for " + copiedArena.getName()
                  + " have been pasted successfully!");
            }
          }

          plugin.getServer().getLogger().info(
              "All the copies for " + copiedArena.getName() + " have been pasted successfully!");
          plugin.getManagerHandler().getArenaManager().setGeneratingArenaRunnable(
              plugin.getManagerHandler().getArenaManager().getGeneratingArenaRunnable() - 1);
          getPlugin().getManagerHandler().getArenaManager().reloadArenas();
        }
      }
    }.run();
  }
}