package dev.demeng.frost.managers;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.EffectUtils;
import dev.demeng.frost.util.file.Config;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class KitManager {

  private final Frost plugin = Frost.getInstance();
  private final Map<String, Kit> kits = new HashMap<>();
  private final List<Kit> rankedKits = new ArrayList<>();
  private final List<Kit> allKits = new ArrayList<>();
  private final Config config = plugin.getKitsConfig();

  public KitManager() {
    this.loadKits();
    this.kits.entrySet().stream().filter(kit -> kit.getValue().isEnabled())
        .filter(kit -> kit.getValue().isRanked())
        .forEach(kit -> this.rankedKits.add(kit.getValue()));
    this.kits.forEach((key, value) -> this.allKits.add(value));
  }

  @SuppressWarnings("unchecked")
  private void loadKits() {
    FileConfiguration fileConfig = config.getConfig();
    ConfigurationSection kitSection = fileConfig.getConfigurationSection("kits");
    if (kitSection == null) {
      return;
    }

    for (String name : kitSection.getKeys(false)) {
      String displayName = kitSection.getString(name + ".displayName") == null ? "&b" + name
          : kitSection.getString(name + ".displayName");
      String kbProfile = kitSection.getString(name + ".kbProfile");
      int unrankedPos = kitSection.getInt(name + ".unrankedPos");
      int rankedPos = kitSection.getInt(name + ".rankedPos");
      int editorPos = kitSection.getInt(name + ".editorPos");
      int spawnFfaPos = kitSection.getInt(name + ".spawnFfaPos");
      ItemStack icon = (ItemStack) kitSection.get(name + ".icon");

      ItemStack[] contents = ((List<ItemStack>) kitSection.get(name + ".contents")).toArray(
          new ItemStack[0]);
      ItemStack[] armor = ((List<ItemStack>) kitSection.get(name + ".armor")).toArray(
          new ItemStack[0]);
      ItemStack[] kitEditContents = ((List<ItemStack>) kitSection.get(
          name + ".kitEditContents")).toArray(new ItemStack[0]);

      List<String> arenaWhiteList = kitSection.getStringList(name + ".arenaWhitelist");

      List<String> matchStartCommands = kitSection.getStringList(name + ".matchStartCmds");
      List<String> matchEndCommands = kitSection.getStringList(name + ".matchEndCmds");

      boolean enabled = kitSection.getBoolean(name + ".enabled");
      boolean ranked = kitSection.getBoolean(name + ".ranked");

      boolean combo = kitSection.getBoolean(name + ".combo");
      boolean sumo = kitSection.getBoolean(name + ".sumo");
      boolean build = kitSection.getBoolean(name + ".build");
      boolean spleef = kitSection.getBoolean(name + ".spleef");
      boolean boxing = kitSection.getBoolean(name + ".boxing");
      boolean bridges = kitSection.getBoolean(name + ".bridges");
      boolean noRegen = kitSection.getBoolean(name + ".noRegen");
      boolean noHunger = kitSection.getBoolean(name + ".noHunger");
      boolean noFall = kitSection.getBoolean(name + ".noFall");
      boolean skyWars = kitSection.getBoolean(name + ".skyWars");
      boolean bedWars = kitSection.getBoolean(name + ".bedWars");
      boolean battleRush = kitSection.getBoolean(name + ".battleRush");
      boolean stickFight = kitSection.getBoolean(name + ".stickFight");
      boolean mlgRush = kitSection.getBoolean(name + ".mlgRush");

      int lives = kitSection.getInt(name + ".lives");
      int damageTicks = kitSection.getInt(name + ".damageTicks");

      boolean showHealth = kitSection.getBoolean(name + ".showHealth");
      boolean allowPotionFill = kitSection.getBoolean(name + ".allowPotionFill");
      boolean allowSpawnFfa = kitSection.getBoolean(name + ".allowSpawnFfa");
      ItemStack[] editorItems = ((List<ItemStack>) kitSection.get(
          name + ".refillableItems")).toArray(new ItemStack[0]);

      Collection<PotionEffect> effects;
      if (kitSection.contains(name + ".potionEffects")) {
        effects = EffectUtils.deserializeEffects(kitSection.getString(name + ".potionEffects"));
      } else {
        effects = new ArrayList<>();
      }

      Kit kit = new Kit(
          name, displayName,
          kbProfile,
          unrankedPos, rankedPos, editorPos, spawnFfaPos,
          icon,
          contents, armor, kitEditContents,
          (effects != null ? new ArrayList<>(effects) : new ArrayList<>()),
          arenaWhiteList, matchStartCommands, matchEndCommands,
          enabled, ranked,
          combo, sumo, build, spleef, boxing, bridges, noRegen, noHunger, noFall, skyWars, bedWars,
          battleRush, stickFight, mlgRush,
          lives, damageTicks, showHealth, allowPotionFill, allowSpawnFfa, editorItems
      );

      this.kits.put(name, kit);
    }
  }

  public void saveKits() {
    FileConfiguration fileConfig = this.config.getConfig();
    fileConfig.set("kits", null);
    this.kits.forEach((kitName, kit) -> {
      if (kit.getIcon() != null && kit.getContents() != null && kit.getArmor() != null) {
        fileConfig.set("kits." + kitName + ".displayName", kit.getDisplayName());
        fileConfig.set("kits." + kitName + ".kbProfile", kit.getKbProfile());
        fileConfig.set("kits." + kitName + ".unrankedPos", kit.getUnrankedPos());
        fileConfig.set("kits." + kitName + ".rankedPos", kit.getRankedPos());
        fileConfig.set("kits." + kitName + ".editorPos", kit.getEditorPos());
        fileConfig.set("kits." + kitName + ".spawnFfaPos", kit.getSpawnFfaPos());
        fileConfig.set("kits." + kitName + ".icon", kit.getIcon());

        fileConfig.set("kits." + kitName + ".contents", kit.getContents());
        fileConfig.set("kits." + kitName + ".armor", kit.getArmor());
        fileConfig.set("kits." + kitName + ".kitEditContents", kit.getKitEditContents());

        fileConfig.set("kits." + kitName + ".arenaWhitelist", kit.getArenaWhiteList());
        fileConfig.set("kits." + kitName + ".matchStartCmds", kit.getMatchStartCommands());
        fileConfig.set("kits." + kitName + ".matchEndCmds", kit.getMatchEndCommands());

        fileConfig.set("kits." + kitName + ".enabled", kit.isEnabled());
        fileConfig.set("kits." + kitName + ".ranked", kit.isRanked());

        fileConfig.set("kits." + kitName + ".combo", kit.isCombo());
        fileConfig.set("kits." + kitName + ".sumo", kit.isSumo());
        fileConfig.set("kits." + kitName + ".build", kit.isBuild());
        fileConfig.set("kits." + kitName + ".spleef", kit.isSpleef());
        fileConfig.set("kits." + kitName + ".boxing", kit.isBoxing());
        fileConfig.set("kits." + kitName + ".bridges", kit.isBridges());
        fileConfig.set("kits." + kitName + ".noRegen", kit.isNoRegen());
        fileConfig.set("kits." + kitName + ".noHunger", kit.isNoHunger());
        fileConfig.set("kits." + kitName + ".noFall", kit.isNoFall());
        fileConfig.set("kits." + kitName + ".skyWars", kit.isSkyWars());
        fileConfig.set("kits." + kitName + ".bedWars", kit.isBedWars());
        fileConfig.set("kits." + kitName + ".battleRush", kit.isBattleRush());
        fileConfig.set("kits." + kitName + ".stickFight", kit.isStickFight());
        fileConfig.set("kits." + kitName + ".mlgRush", kit.isMlgRush());

        fileConfig.set("kits." + kitName + ".lives", kit.getLives());
        fileConfig.set("kits." + kitName + ".damageTicks", kit.getDamageTicks());

        fileConfig.set("kits." + kitName + ".showHealth", kit.isShowHealth());
        fileConfig.set("kits." + kitName + ".allowPotionFill", kit.isAllowPotionFill());
        fileConfig.set("kits." + kitName + ".allowSpawnFfa", kit.isAllowSpawnFfa());

        fileConfig.set("kits." + kitName + ".refillableItems", kit.getEditorItems());
        fileConfig.set("kits." + kitName + ".potionEffects",
            EffectUtils.serializeEffects(kit.getPotionEffects()));
      }
    });

    this.config.save();
  }

  public void createKit(String name) {
    this.kits.put(name, new Kit(name));
  }

  public void deleteKit(String name) {
    this.kits.remove(name);
  }

  public Kit getKit(String name) {
    return this.kits.get(name);
  }

  public Kit getKit() {
    return plugin.getManagerHandler().getKitManager().getKits().stream()
        .min(Comparator.comparingInt(Kit::getUnrankedPos)).orElse(null);
  }

  public List<String> getBuildKitNames() {
    return this.kits.values().stream().filter(Kit::isBuild).map(Kit::getName)
        .collect(Collectors.toList());
  }

  public Set<String> getKitNames() {
    return this.kits.keySet();
  }

  public Collection<Kit> getKits() {
    return this.kits.values();
  }

  public List<Kit> getRankedKits() {
    return rankedKits;
  }

  public List<Kit> getAllKits() {
    return allKits;
  }
}
