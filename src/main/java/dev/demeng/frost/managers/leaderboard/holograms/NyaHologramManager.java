package dev.demeng.frost.managers.leaderboard.holograms;

import dev.demeng.frost.Frost;
import dev.demeng.frost.managers.leaderboard.holograms.impl.HologramImplementation;
import io.netty.util.internal.ConcurrentSet;
import java.util.Set;
import org.bukkit.entity.Player;

public class NyaHologramManager {

  private final Set<NyaHologram> holograms = new ConcurrentSet<>();

  public NyaHologramManager(Frost plugin) {
    int update = Frost.getInstance().getSettingsConfig().getConfig().getInt("HOLOGRAM.UPDATE-TIME");

    if (plugin.getManagerHandler().getSpawnManager().getHoloLeaderboardsLocation() != null) {
      NyaHologram ranked = new HologramImplementation(
          plugin.getManagerHandler().getSpawnManager().getHoloLeaderboardsLocation()
              .toBukkitLocation(), update, "RANKED");
      holograms.add(ranked);
      ranked.start("Ranked Leaderboard");
    }

    if (plugin.getManagerHandler().getSpawnManager().getHoloWinstreakLocation() != null) {
      NyaHologram winstreak = new HologramImplementation(
          plugin.getManagerHandler().getSpawnManager().getHoloWinstreakLocation()
              .toBukkitLocation(), update, "WINSTREAK");
      holograms.add(winstreak);
      winstreak.start("Winstreak Leaderboard");
    }
  }

  public void show(Player player) {
    holograms.forEach(nyaHologram -> nyaHologram.show(player));
  }

  public void hide(Player player) {
    holograms.forEach(nyaHologram -> nyaHologram.hide(player));
  }
}
