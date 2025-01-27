package dev.demeng.frost.managers;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.util.CustomLocation;
import dev.demeng.frost.util.file.Config;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ArenaManager {

  private final Frost plugin = Frost.getInstance();
  private final Config config = plugin.getArenasConfig();

  @Getter private final Map<String, Arena> arenas = new HashMap<>();
  @Getter private final Map<StandaloneArena, UUID> arenaMatchUUIDs = new HashMap<>();

  @Getter @Setter private int generatingArenaRunnable;

  public ArenaManager() {
    this.loadArenas();
  }

  private void loadArenas() {
    FileConfiguration fileConfig = config.getConfig();
    ConfigurationSection arenaSection = fileConfig.getConfigurationSection("arenas");
    if (arenaSection == null) {
      return;
    }

    arenaSection.getKeys(false).forEach(name -> {
      String icon = arenaSection.getString(name + ".icon") == null ? Material.PAPER.name()
          : arenaSection.getString(name + ".icon");
      int iconData = arenaSection.getInt(name + ".icon-data");
      String a = arenaSection.getString(name + ".a");
      String b = arenaSection.getString(name + ".b");
      String min = arenaSection.getString(name + ".min");
      String max = arenaSection.getString(name + ".max");
      int buildMax = arenaSection.getInt(name + ".build-max");
      int deadZone = arenaSection.getInt(name + ".dead-zone");
      int portalProt = arenaSection.getInt(name + ".portalProt");

      CustomLocation locA = CustomLocation.stringToLocation(a);
      CustomLocation locB = CustomLocation.stringToLocation(b);
      CustomLocation locMin = CustomLocation.stringToLocation(min);
      CustomLocation locMax = CustomLocation.stringToLocation(max);

      List<StandaloneArena> standaloneArenas = new ArrayList<>();
      ConfigurationSection saSection = arenaSection.getConfigurationSection(
          name + ".standaloneArenas");
      if (saSection != null) {
        saSection.getKeys(false).forEach(id -> {
          String saA = saSection.getString(id + ".a");
          String saB = saSection.getString(id + ".b");
          String saMin = saSection.getString(id + ".min");
          String saMax = saSection.getString(id + ".max");

          CustomLocation locSaA = CustomLocation.stringToLocation(saA);
          CustomLocation locSaB = CustomLocation.stringToLocation(saB);
          CustomLocation locSaMin = CustomLocation.stringToLocation(saMin);
          CustomLocation locSaMax = CustomLocation.stringToLocation(saMax);

          StandaloneArena standaloneArena = new StandaloneArena(locSaA, locSaB, locSaMin, locSaMax);
          ChunkRestorationManager.getIChunkRestoration().copy(standaloneArena);

          standaloneArenas.add(standaloneArena);
        });
      }

      boolean enabled = arenaSection.getBoolean(name + ".enabled", false);

      Arena arena = new Arena(name, standaloneArenas, new ArrayList<>(standaloneArenas), icon,
          iconData, locA, locB, locMin, locMax, buildMax, deadZone, portalProt, enabled);
      this.arenas.put(name, arena);
    });
  }

  public void reloadArenas() {
    this.saveArenas();
    this.arenas.clear();
    this.loadArenas();
  }

  public void saveArenas() {
    FileConfiguration fileConfig = this.config.getConfig();

    fileConfig.set("arenas", null);
    arenas.forEach((arenaName, arena) -> {
      String icon = arena.getIcon();
      int iconData = arena.getIconData();
      String a = CustomLocation.locationToString(arena.getA());
      String b = CustomLocation.locationToString(arena.getB());
      String min = CustomLocation.locationToString(arena.getMin());
      String max = CustomLocation.locationToString(arena.getMax());
      int buildMax = arena.getBuildMax();
      int deadZone = arena.getDeadZone();
      int portalProt = arena.getPortalProt();

      String arenaRoot = "arenas." + arenaName;

      fileConfig.set(arenaRoot + ".icon", icon);
      fileConfig.set(arenaRoot + ".icon-data", iconData);
      fileConfig.set(arenaRoot + ".a", a);
      fileConfig.set(arenaRoot + ".b", b);
      fileConfig.set(arenaRoot + ".min", min);
      fileConfig.set(arenaRoot + ".max", max);
      fileConfig.set(arenaRoot + ".build-max", buildMax);
      fileConfig.set(arenaRoot + ".dead-zone", deadZone);
      fileConfig.set(arenaRoot + ".portalProt", portalProt);
      fileConfig.set(arenaRoot + ".enabled", arena.isEnabled());
      fileConfig.set(arenaRoot + ".standaloneArenas", null);

      int i = 0;
      if (arena.getStandaloneArenas() != null) {
        for (StandaloneArena saArena : arena.getStandaloneArenas()) {
          String saA = CustomLocation.locationToString(saArena.getA());
          String saB = CustomLocation.locationToString(saArena.getB());
          String saMin = CustomLocation.locationToString(saArena.getMin());
          String saMax = CustomLocation.locationToString(saArena.getMax());

          String standAloneRoot = arenaRoot + ".standaloneArenas." + i;

          fileConfig.set(standAloneRoot + ".a", saA);
          fileConfig.set(standAloneRoot + ".b", saB);
          fileConfig.set(standAloneRoot + ".min", saMin);
          fileConfig.set(standAloneRoot + ".max", saMax);

          i++;
        }
      }
    });

    this.config.save();
  }

  public void createArena(String name) {
    this.arenas.put(name, new Arena(name));
  }

  public void deleteArena(String name) {
    this.arenas.remove(name);
  }

  public Arena getArena(String name) {
    return this.arenas.get(name);
  }

  public Arena getRandomArena(Kit kit) {
    List<Arena> arenasList = new ArrayList<>(this.arenas.values());
    arenasList.removeIf(arena -> arena != null && !arena.isEnabled());
    arenasList.removeIf(
        arena -> arena != null && !kit.getArenaWhiteList().contains(arena.getName()));

    if (arenasList.isEmpty()) {
      return null;
    }

    return arenasList.get(ThreadLocalRandom.current().nextInt(arenasList.size()));
  }

  public void removeArenaMatchUUID(StandaloneArena arena) {
    this.arenaMatchUUIDs.remove(arena);
  }

  public UUID getArenaMatchUUID(StandaloneArena arena) {
    return this.arenaMatchUUIDs.get(arena);
  }

  public void setArenaMatchUUID(StandaloneArena arena, UUID matchUUID) {
    this.arenaMatchUUIDs.put(arena, matchUUID);
  }
}
