package dev.demeng.frost.managers;

import static dev.demeng.frost.util.CC.color;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.kit.PlayerKit;
import dev.demeng.frost.runnable.SpawnPlayerVisibilityRunnable;
import dev.demeng.frost.user.effects.SpecialEffects;
import dev.demeng.frost.user.player.PlayerSettings;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.InventoryUtil;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.threads.Threads;
import dev.demeng.frost.util.timer.impl.BridgeArrowTimer;
import dev.demeng.frost.util.timer.impl.EnderpearlTimer;
import dev.demeng.frost.util.timer.impl.GlockTimer;
import dev.demeng.pluginbase.mongo.lib.bson.Document;
import dev.demeng.pluginbase.mongo.lib.driver.client.MongoCursor;
import dev.demeng.pluginbase.mongo.lib.driver.client.model.Filters;
import dev.demeng.pluginbase.mongo.lib.driver.client.model.ReplaceOptions;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerManager {

  private final Frost plugin = Frost.getInstance();
  private final Map<UUID, PracticePlayerData> practicePlayers = new ConcurrentHashMap<>();

  public static void setKnockbackProfile(Player player, String knockbackProfile) {
    if (Frost.getInstance().isUsingCustomKB()) {
      me.elb1to.practice.controller.PracticeKnockbackController.setKnockbackProfile(player,
          knockbackProfile);
    }
  }

  public void createPlayerData(Player player) {
    PracticePlayerData data = new PracticePlayerData(player.getUniqueId());
    for (Kit ladder : plugin.getManagerHandler().getKitManager().getKits()) {
      data.getKits().put(ladder.getName(), new PlayerKit[4]);
    }
    if (data.getUsername() == null || data.getUsername().isEmpty() || !data.getUsername()
        .equals(player.getName())) {
      data.setUsername(player.getName());
    }

    data.setSilent(false);
    data.setFollowing(false);

    this.practicePlayers.put(data.getUniqueId(), data);

    Threads.executeData(() -> loadData(data));
  }

  public void loadData(PracticePlayerData practicePlayerData) {
    practicePlayerData.setPlayerState(PlayerState.SPAWN);

    Document document = plugin.getMongoHandler().getPlayers()
        .find(Filters.eq("uuid", practicePlayerData.getUniqueId().toString())).first();
    if (document == null) {
      for (Kit kit : plugin.getManagerHandler().getKitManager().getKits()) {
        practicePlayerData.setElo(kit.getName(), 1000);
        practicePlayerData.setWins(kit.getName(), 0);
        practicePlayerData.setLosses(kit.getName(), 0);
        practicePlayerData.setCurrentWinstreak(kit.getName(), 0);
        practicePlayerData.setHighestWinStreak(kit.getName(), 0);
      }

      Threads.executeData(() -> {
        if (!practicePlayerData.isDataLoaded()) {
          practicePlayerData.setDataLoaded(true);
        }
        this.saveData(practicePlayerData);
      });

      return;
    }

    Document statsDocument = (Document) document.get("stats");
    Document globalDocument = (Document) document.get("global");
    Document loadoutsDocument = (Document) document.get("loadouts");
    Document settingsDocument = (Document) document.get("settings");

    if (globalDocument == null) {
      practicePlayerData.setGlobalElo(1000);
      practicePlayerData.setPremiumElo(1000);
      practicePlayerData.setMatchesPlayed(0);
      practicePlayerData.setPremiumMatches(0);
      practicePlayerData.setGlobalWinStreak(0);
      practicePlayerData.setGlobalHighestWinStreak(0);
      return;
    }

    practicePlayerData.setUsername(document.getString("username"));

    practicePlayerData.getPlayerSettings()
        .setDuelRequests(settingsDocument.getBoolean("duelRequests"));
    practicePlayerData.getPlayerSettings()
        .setPartyInvites(settingsDocument.getBoolean("partyInvites"));
    practicePlayerData.getPlayerSettings()
        .setPlayerVisibility(settingsDocument.getBoolean("playerVisibility"));
    practicePlayerData.getPlayerSettings()
        .setSpectatorsAllowed(settingsDocument.getBoolean("spectatorsAllowed"));

    practicePlayerData.getPlayerSettings().setVanillaTab(settingsDocument.getBoolean("vanillaTab"));
    practicePlayerData.getPlayerSettings()
        .setScoreboardToggled(settingsDocument.getBoolean("scoreboardToggled"));
    practicePlayerData.getPlayerSettings()
        .setPingScoreboardToggled(settingsDocument.getBoolean("pingScoreboardToggled"));

    practicePlayerData.getPlayerSettings()
        .setStartFlying(settingsDocument.getBoolean("startFlying"));
    practicePlayerData.getPlayerSettings()
        .setBodyAnimation(settingsDocument.getBoolean("bodyAnimation"));
    practicePlayerData.getPlayerSettings()
        .setClearInventory(settingsDocument.getBoolean("clearInventory"));

    practicePlayerData.getPlayerSettings().setPingRange(settingsDocument.getInteger("pingRange"));

    practicePlayerData.getPlayerSettings()
        .setSpecialEffect(SpecialEffects.getByName(settingsDocument.getString("specialEffect")));
    practicePlayerData.getPlayerSettings().setPlayerTime(
        PlayerSettings.PlayerTime.getByName(settingsDocument.getString("playerTime")));

    for (String key : loadoutsDocument.keySet()) {
      Kit ladder = Frost.getInstance().getManagerHandler().getKitManager().getKit(key);
      if (ladder == null) {
        continue;
      }

      JsonArray kitsArray = plugin.getJsonParser().parse(loadoutsDocument.getString(key))
          .getAsJsonArray();
      PlayerKit[] kits = new PlayerKit[4];
      for (JsonElement kitElement : kitsArray) {
        JsonObject kitObject = kitElement.getAsJsonObject();
        PlayerKit kit = new PlayerKit(kitObject.get("name").getAsString(),
            kitObject.get("index").getAsInt(),
            InventoryUtil.deserializeInventory(kitObject.get("contents").getAsString()),
            kitObject.get("name").getAsString());
        kit.setContents(
            InventoryUtil.deserializeInventory(kitObject.get("contents").getAsString()));
        kits[kitObject.get("index").getAsInt()] = kit;
      }

      practicePlayerData.getKits().put(ladder.getName(), kits);
    }

    practicePlayerData.setGlobalElo(globalDocument.getInteger("globalElo"));
    practicePlayerData.setPremiumElo(globalDocument.getInteger("premiumElo"));
    practicePlayerData.setPremiumMatches(globalDocument.getInteger("premiumMatches"));
    practicePlayerData.setMatchesPlayed(globalDocument.getInteger("matchesPlayed"));
    practicePlayerData.setGlobalWinStreak(globalDocument.getInteger("globalWinStreak"));
    practicePlayerData.setGlobalHighestWinStreak(
        globalDocument.getInteger("highestGlobalWinStreak"));

    statsDocument.keySet().forEach(key -> {
      Document ladderDocument = (Document) statsDocument.get(key);
      if (ladderDocument == null) {
        System.out.println("Ladder document is null for ladder " + key);
        return;
      }

      if (ladderDocument.containsKey("elo")) {
        practicePlayerData.getRankedElo().put(key, ladderDocument.getInteger("elo"));
      }
      if (ladderDocument.containsKey("wins")) {
        practicePlayerData.getRankedWins().put(key, ladderDocument.getInteger("wins"));
      }
      if (ladderDocument.containsKey("losses")) {
        practicePlayerData.getRankedLosses().put(key, ladderDocument.getInteger("losses"));
      }
      if (ladderDocument.containsKey("currentStreak")) {
        practicePlayerData.getCurrentWinstreak()
            .put(key, ladderDocument.getInteger("currentStreak"));
      }
      if (ladderDocument.containsKey("highestStreak")) {
        practicePlayerData.getHighestWinStreak()
            .put(key, ladderDocument.getInteger("highestStreak"));
      }
    });

    practicePlayerData.setDataLoaded(true);
  }

  public void saveData(PracticePlayerData practicePlayerData) {
    if (practicePlayerData == null) {
      return;
    }
    if (!practicePlayerData.isDataLoaded()) {
      return;
    }

    Document document = new Document();
    Document statsDocument = new Document();
    Document globalDocument = new Document();
    Document loadoutsDocument = new Document();
    Document settingsDocument = new Document();

    practicePlayerData.getRankedElo().forEach((key, value) -> {
      Document ladderDocument;
      if (statsDocument.containsKey(key)) {
        ladderDocument = (Document) statsDocument.get(key);
      } else {
        ladderDocument = new Document();
      }

      ladderDocument.put("elo", value);
      statsDocument.put(key, ladderDocument);
    });

    practicePlayerData.getRankedWins().forEach((key, value) -> {
      Document ladderDocument;
      if (statsDocument.containsKey(key)) {
        ladderDocument = (Document) statsDocument.get(key);
      } else {
        ladderDocument = new Document();
      }

      ladderDocument.put("wins", value);
      statsDocument.put(key, ladderDocument);
    });

    practicePlayerData.getRankedLosses().forEach((key, value) -> {
      Document ladderDocument;
      if (statsDocument.containsKey(key)) {
        ladderDocument = (Document) statsDocument.get(key);
      } else {
        ladderDocument = new Document();
      }

      ladderDocument.put("losses", value);
      statsDocument.put(key, ladderDocument);
    });

    practicePlayerData.getCurrentWinstreak().forEach((key, value) -> {
      Document ladderDocument;
      if (statsDocument.containsKey(key)) {
        ladderDocument = (Document) statsDocument.get(key);
      } else {
        ladderDocument = new Document();
      }

      ladderDocument.put("currentStreak", value);
      statsDocument.put(key, ladderDocument);
    });

    practicePlayerData.getHighestWinStreak().forEach((key, value) -> {
      Document ladderDocument;
      if (statsDocument.containsKey(key)) {
        ladderDocument = (Document) statsDocument.get(key);
      } else {
        ladderDocument = new Document();
      }

      ladderDocument.put("highestStreak", value);
      statsDocument.put(key, ladderDocument);
    });

    int kits = 0, count = 0;
    for (Kit kit : plugin.getManagerHandler().getKitManager().getRankedKits()) {
      if (practicePlayerData.getRankedElo().get(kit.getName()) == null) {
        continue;
      }

      kits += practicePlayerData.getRankedElo().get(kit.getName());
      count++;
    }

    if (kits == 1) {
      kits = 0;
    }
    if (count == 0) {
      count = 1;
    }

    globalDocument.put("globalElo", (kits / count));

    practicePlayerData.getKits().forEach((key, value) -> {
      JsonArray kitsArray = new JsonArray();
      for (int i = 0; i < 4; i++) {
        PlayerKit kit = value[i];
        if (kit != null) {
          JsonObject kitObject = new JsonObject();
          kitObject.addProperty("index", i);
          kitObject.addProperty("name", kit.getName());
          kitObject.addProperty("contents", InventoryUtil.serializeInventory(kit.getContents()));

          kitsArray.add(kitObject);
        }
      }

      loadoutsDocument.put(key, kitsArray.toString());
    });

    globalDocument.put("globalWinStreak", practicePlayerData.getGlobalWinStreak());
    globalDocument.put("highestGlobalWinStreak", practicePlayerData.getGlobalHighestWinStreak());

    globalDocument.put("premiumElo", practicePlayerData.getPremiumElo());
    globalDocument.put("premiumMatches", practicePlayerData.getPremiumMatches());
    globalDocument.put("matchesPlayed", practicePlayerData.getMatchesPlayed());

    settingsDocument.put("duelRequests", practicePlayerData.getPlayerSettings().isDuelRequests());
    settingsDocument.put("partyInvites", practicePlayerData.getPlayerSettings().isPartyInvites());
    settingsDocument.put("playerVisibility",
        practicePlayerData.getPlayerSettings().isPlayerVisibility());
    settingsDocument.put("spectatorsAllowed",
        practicePlayerData.getPlayerSettings().isSpectatorsAllowed());

    settingsDocument.put("vanillaTab", practicePlayerData.getPlayerSettings().isVanillaTab());
    settingsDocument.put("scoreboardToggled",
        practicePlayerData.getPlayerSettings().isScoreboardToggled());
    settingsDocument.put("pingScoreboardToggled",
        practicePlayerData.getPlayerSettings().isPingScoreboardToggled());

    settingsDocument.put("startFlying", practicePlayerData.getPlayerSettings().isStartFlying());
    settingsDocument.put("bodyAnimation", practicePlayerData.getPlayerSettings().isBodyAnimation());
    settingsDocument.put("clearInventory",
        practicePlayerData.getPlayerSettings().isClearInventory());

    settingsDocument.put("pingRange", practicePlayerData.getPlayerSettings().getPingRange());

    settingsDocument.put("specialEffect",
        practicePlayerData.getPlayerSettings().getSpecialEffect().getName());
    settingsDocument.put("playerTime",
        practicePlayerData.getPlayerSettings().getPlayerTime().getName());

    document.put("uuid", practicePlayerData.getUniqueId().toString());
    document.put("username", practicePlayerData.getUsername());
    document.put("stats", statsDocument);
    document.put("global", globalDocument);
    document.put("loadouts", loadoutsDocument);
    document.put("settings", settingsDocument);

    plugin.getMongoHandler().getPlayers()
        .replaceOne(Filters.eq("uuid", practicePlayerData.getUniqueId().toString()), document,
            new ReplaceOptions().upsert(true));
  }

  public void removePlayerData(PracticePlayerData practicePlayerData) {
    Threads.executeData(() -> {
      this.saveData(practicePlayerData);
      this.practicePlayers.remove(practicePlayerData.getUniqueId());
    });
  }

  public Collection<PracticePlayerData> getAllData() {
    return this.practicePlayers.values();
  }

  public PracticePlayerData getPlayerData(UUID uuid) {
    if (Bukkit.getPlayer(uuid) != null) {
      if (!this.practicePlayers.containsKey(uuid)) {
        createPlayerData(Bukkit.getPlayer(uuid));
      }
    }

    return this.practicePlayers.get(uuid);
  }

  public PracticePlayerData getPlayerData(Player player) {
    if (!this.practicePlayers.containsKey(player.getUniqueId())) {
      createPlayerData(player);
    }

    return this.practicePlayers.get(player.getUniqueId());
  }

  public void giveLobbyItems(Player player) {
    boolean inParty =
        plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId()) != null;
    boolean inTournament =
        plugin.getManagerHandler().getTournamentManager().getTournament(player.getUniqueId())
            != null;
    boolean inEvent = plugin.getManagerHandler().getEventManager().getEventPlaying(player) != null;

    if (inTournament) {
      plugin.getManagerHandler().getItemManager().getTournamentItems().stream()
          .filter(ItemManager.HotbarItem::isEnabled)
          .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack()));
    } else if (inEvent) {
      plugin.getManagerHandler().getItemManager().getEventItems().stream()
          .filter(ItemManager.HotbarItem::isEnabled)
          .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack()));
    } else if (inParty) {
      plugin.getManagerHandler().getItemManager().getPartyItems().stream()
          .filter(ItemManager.HotbarItem::isEnabled)
          .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack()));
    } else {
      plugin.getManagerHandler().getItemManager().getSpawnItems().stream()
          .filter(ItemManager.HotbarItem::isEnabled)
          .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack()));
    }

    if (plugin.getManagerHandler().getMatchManager().hasPlayAgainRequest(player.getUniqueId())) {
      player.getInventory()
          .addItem(plugin.getManagerHandler().getItemManager().getPlayAgainItem().getItemStack());
    }

    player.updateInventory();
  }

  public void resetPlayerOrSpawn(Player player, boolean toSpawn) {
    if (player == null || !player.isOnline()) {
      return;
    }

    PracticePlayerData practicePlayerData = this.getPlayerData(player.getUniqueId());
    practicePlayerData.setPlayerState(PlayerState.SPAWN);
    practicePlayerData.setCurrentKitContents(null);
    practicePlayerData.setCurrentKitArmor(null);
    practicePlayerData.setSelectedLadder(null);
    practicePlayerData.setActive(false);
    PlayerUtil.clearPlayer(player, true);

    plugin.getManagerHandler().getTimerManager().getTimer(EnderpearlTimer.class)
        .clearCooldown(player.getUniqueId());
    plugin.getManagerHandler().getTimerManager().getTimer(GlockTimer.class)
        .clearCooldown(player.getUniqueId());
    plugin.getManagerHandler().getTimerManager().getTimer(BridgeArrowTimer.class)
        .clearCooldown(player.getUniqueId());

    this.giveLobbyItems(player);

    if (player.hasPermission("frost.user.fly")) {
      player.setAllowFlight(true);
      player.setFlying(true);
    } else {
      player.setAllowFlight(false);
      player.setFlying(false);
    }

    updatePlayerView();

    if (toSpawn) {
      if (plugin.getManagerHandler().getSpawnManager().getSpawnLocation() != null) {
        player.teleport(
            plugin.getManagerHandler().getSpawnManager().getSpawnLocation().toBukkitLocation());
      } else {
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(color("&8[&bFrost&8] &cYou need to set the server spawn!"));
        player.sendMessage(color("&8[&bFrost&8] &cPlease use &a/setspawn spawnLocation"));
        player.sendMessage(
            color("&8[&bFrost&8] &cAlso set &a/setspawn spawnMin and /setspawn spawnMax"));
        player.sendMessage(CC.CHAT_BAR);
      }
    }
  }

  public MongoCursor<Document> getPlayersSortByLadderElo(Kit ladder) {
    final Document sort = new Document();
    sort.put("stats." + ladder.getName() + ".elo", -1);

    return plugin.getMongoHandler().getPlayers().find().sort(sort).limit(10).iterator();
  }

  public MongoCursor<Document> getPlayersSortedByDocumentElo(String document) {
    final Document sort = new Document();
    sort.put("global." + document, -1);

    return plugin.getMongoHandler().getPlayers().find().sort(sort).limit(10).iterator();
  }

  public void updatePlayerView() {
    plugin.getServer().getScheduler()
        .runTaskAsynchronously(plugin, new SpawnPlayerVisibilityRunnable());
  }
}
