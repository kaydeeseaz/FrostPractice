package dev.demeng.frost.managers;

import static dev.demeng.frost.util.CC.color;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.event.match.MatchEndEvent;
import dev.demeng.frost.game.event.match.MatchStartEvent;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.kit.PlayerKit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchRequest;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.user.effects.SpecialEffects;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.postmatch.InventorySnapshot;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.Clickable;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.TtlHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class MatchManager {

  private final Map<UUID, Set<MatchRequest>> duelRequests = new TtlHashMap<>(TimeUnit.SECONDS, 30);

  private final Map<UUID, UUID> postMatchUuids = new TtlHashMap<>(TimeUnit.SECONDS, 90);
  private final Map<UUID, UUID> postMatchInventories = new TtlHashMap<>(TimeUnit.SECONDS, 90);

  private final Map<UUID, Kit> playAgainKit = new TtlHashMap<>(TimeUnit.SECONDS, 30);

  private final Map<UUID, UUID> spectators = new ConcurrentHashMap<>();
  private final Map<UUID, Match> matches = new ConcurrentHashMap<>();

  private final Frost plugin = Frost.getInstance();

  public int getFighters() {
    int i = 0;
    for (Match match : this.matches.values()) {
      for (MatchTeam matchTeam : match.getTeams()) {
        i += matchTeam.getAlivePlayers().size();
      }
    }

    return i;
  }

  public int getFighters(String ladder, QueueType type) {
    int i = 0;
    for (Match match : this.matches.values()) {
      if (match.getKit().getName().equalsIgnoreCase(ladder)) {
        if (match.getType() == type) {
          for (MatchTeam matchTeam : match.getTeams()) {
            i += matchTeam.getAlivePlayers().size();
          }
        }
      }
    }

    return i;
  }

  public int getFighters(QueueType type) {
    int i = 0;
    for (Match match : this.matches.values()) {
      if (match.getType() == type) {
        for (MatchTeam team : match.getTeams()) {
          i += team.getAlivePlayers().size();
        }
      }
    }

    return i;
  }

  public Map<UUID, Match> getMatches() {
    return matches;
  }

  public List<ItemStack> getKitItems(Player player, Kit kit, Match match) {
    List<ItemStack> toReturn = new ArrayList<>();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (!match.getKit().isSumo()) {
      toReturn.add(plugin.getManagerHandler().getItemManager().getDefaultBook());
      for (PlayerKit playerKit : practicePlayerData.getKits().get(kit.getName())) {
        if (playerKit != null) {
          final ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
          final ItemMeta itemMeta = itemStack.getItemMeta();
          itemMeta.setDisplayName(color(playerKit.getName()));
          itemMeta.setLore(
              Collections.singletonList(ChatColor.GRAY + "Right-click to receive this kit."));
          itemStack.setItemMeta(itemMeta);
          toReturn.remove(plugin.getManagerHandler().getItemManager().getDefaultBook());
          toReturn.add(itemStack);
          player.getInventory()
              .setItem(8, plugin.getManagerHandler().getItemManager().getDefaultBook());
        }
      }
    }

    return toReturn;
  }

  public void createMatchRequest(Player requester, Player requested, Arena arena, String kitName,
      boolean party) {
    MatchRequest request = new MatchRequest(requester.getUniqueId(), requested.getUniqueId(), arena,
        kitName, party);
    this.duelRequests.computeIfAbsent(requested.getUniqueId(), k -> new HashSet<>()).add(request);
  }

  public MatchRequest getMatchRequest(UUID requester, UUID requested) {
    Set<MatchRequest> requests = this.duelRequests.get(requested);
    if (requests == null) {
      return null;
    }

    return requests.stream().filter(req -> req.getRequester().equals(requester)).findAny()
        .orElse(null);
  }

  public MatchRequest getMatchRequest(UUID requester, UUID requested, String kitName) {
    Set<MatchRequest> requests = this.duelRequests.get(requested);
    if (requests == null) {
      return null;
    }

    return requests.stream()
        .filter(req -> req.getRequester().equals(requester) && req.getKitName().equals(kitName))
        .findAny().orElse(null);
  }

  public Match getMatch(PracticePlayerData practicePlayerData) {
    return this.matches.get(practicePlayerData.getCurrentMatchID());
  }

  public Match getMatch(UUID uuid) {
    return this.getMatch(plugin.getManagerHandler().getPlayerManager().getPlayerData(uuid));
  }

  public Match getMatchFromUUID(UUID uuid) {
    return this.matches.get(uuid);
  }

  public Match getSpectatingMatch(UUID uuid) {
    return this.matches.getOrDefault(this.spectators.get(uuid), null);
  }

  public void removeMatchRequests(UUID uuid) {
    this.duelRequests.remove(uuid);
  }

  public void createMatch(Match match) {
    this.matches.put(match.getMatchId(), match);
    plugin.getServer().getPluginManager().callEvent(new MatchStartEvent(match));
  }

  public void removeFighter(Player player, PracticePlayerData practicePlayerData,
      boolean spectateDeath) {
    Match match = this.matches.get(practicePlayerData.getCurrentMatchID());
    Player killer = player.getKiller();
    if (player.isOnline() && killer != null) {
      PlayerUtil.hideOrShowPlayer(killer, player, true);
    }

    if (practicePlayerData.getPlayerSettings().isBodyAnimation()) {
      PlayerUtil.playDeathAnimation(player);
    }

    MatchTeam entityTeam = match.getTeams().get(practicePlayerData.getTeamId());
    MatchTeam winningTeam =
        match.isFFA() ? entityTeam : match.getTeams().get(entityTeam.getTeamID() == 0 ? 1 : 0);
    if (match.getMatchState() == MatchState.ENDING) {
      return;
    }

    if (killer != null) {
      match.broadcast(plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.KILLED")
          .replace("<victim>", player.getName())
          .replace("<killer>", killer.getName())
      );
    } else {
      match.broadcast(plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAYER.DEATH")
          .replace("<victim>", player.getName())
      );
    }

    match.addSnapshot(player);
    entityTeam.killPlayer(player.getUniqueId());

    int remaining = entityTeam.getAlivePlayers().size();
    if (remaining != 0) {
      Set<Item> items = new HashSet<>();
      for (ItemStack inventory : player.getInventory().getContents()) {
        if (inventory != null && inventory.getType() != Material.AIR) {
          items.add(player.getWorld().dropItemNaturally(player.getLocation(), inventory));
        }
      }
      for (ItemStack armor : player.getInventory().getArmorContents()) {
        if (armor != null && armor.getType() != Material.AIR) {
          items.add(player.getWorld().dropItemNaturally(player.getLocation(), armor));
        }
      }

      plugin.getManagerHandler().getMatchManager().addDroppedItems(match, items);
    }

    if (spectateDeath) {
      this.addDeathSpectator(player, practicePlayerData, match);
    }

    if (match.isFFA() && remaining == 1 || match.isFFA() && remaining == 0 || remaining == 0) {
      plugin.getServer().getPluginManager()
          .callEvent(new MatchEndEvent(match, winningTeam, entityTeam));
    }
  }

  public void removeMatch(Match match) {
    this.matches.remove(match.getMatchId());
  }

  private void addDeathSpectator(Player player, PracticePlayerData practicePlayerData,
      Match match) {
    this.spectators.put(player.getUniqueId(), match.getMatchId());
    practicePlayerData.setPlayerState(PlayerState.SPECTATING);

    PlayerUtil.clearPlayer(player, false);
    SpecialEffects specialEffect = practicePlayerData.getPlayerSettings().getSpecialEffect();
    if (specialEffect != null && !specialEffect.getName().equalsIgnoreCase("none")) {
      playSpecialEffect(player, match, specialEffect);
    }

    match.addSpectator(player.getUniqueId());
    match.spectatorPlayers().forEach(member -> PlayerUtil.hideOrShowPlayer(member, player, true));
    for (MatchTeam team : match.getTeams()) {
      team.alivePlayers().forEach(member -> PlayerUtil.hideOrShowPlayer(member, player, true));
    }

    player.getActivePotionEffects().stream().map(PotionEffect::getType)
        .forEach(player::removePotionEffect);
    player.setWalkSpeed(0.2F);
    player.setFlySpeed(0.4F);
    player.setAllowFlight(true);

    if (match.isParty() || match.isFFA()) {
      player.getInventory().clear();
      for (ItemManager.HotbarItem item : plugin.getManagerHandler().getItemManager()
          .getPartySpecItems()) {
        if (item.isEnabled()) {
          player.getInventory().setItem(item.getSlot(), item.getItemStack());
        }
      }
    }

    player.updateInventory();
  }

  public void playSpecialEffect(Player player, Match match, SpecialEffects specialEffect) {
    for (MatchTeam matchTeam : match.getTeams()) {
      specialEffect.getCallable().call(player,
          matchTeam.getPlayers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
              .toArray(Player[]::new));
      specialEffect.getCallable().call(player,
          match.getSpectators().stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
              .toArray(Player[]::new));
    }
  }

  public void addSpectator(Player player, PracticePlayerData practicePlayerData, Player target,
      Match targetMatch) {
    this.spectators.put(player.getUniqueId(), targetMatch.getMatchId());

    if (targetMatch.getMatchState() != MatchState.ENDING) {
      if (!practicePlayerData.isSilent()) {
        targetMatch.broadcast(
            plugin.getMessagesConfig().getConfig().getString("MESSAGES.MATCH.SPECTATOR-JOINED")
                .replace("<player>", player.getName()));
      }
    }

    targetMatch.addSpectator(player.getUniqueId());
    practicePlayerData.setPlayerState(PlayerState.SPECTATING);

    player.teleport(target);
    player.setAllowFlight(true);
    player.setFlying(true);
    player.spigot().setCollidesWithEntities(false);

    player.getInventory().clear();
    plugin.getManagerHandler().getItemManager().getSpecItems().stream()
        .filter(ItemManager.HotbarItem::isEnabled)
        .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack()));
    player.updateInventory();

    plugin.getServer().getOnlinePlayers().forEach(online -> {
      PlayerUtil.hideOrShowPlayer(player, online, true);
      PlayerUtil.hideOrShowPlayer(online, player, true);
    });

    targetMatch.getTeams().forEach(matchTeam -> matchTeam.alivePlayers()
        .forEach(matchPlayer -> PlayerUtil.hideOrShowPlayer(player, matchPlayer, false)));
  }

  public void addDroppedItem(Match match, Item item) {
    match.addEntityToRemove(item);
    match.addRunnable(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
      match.removeEntityToRemove(item);
      item.remove();
    }, 100L).getTaskId());
  }

  public void addDroppedItems(Match match, Set<Item> items) {
    for (Item item : items) {
      match.addEntityToRemove(item);
    }

    match.addRunnable(plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
      for (Item item : items) {
        match.removeEntityToRemove(item);
        item.remove();
      }
    }, 100L).getTaskId());
  }

  public void removeSpectator(Player player) {
    Match match = this.matches.get(this.spectators.get(player.getUniqueId()));
    match.removeSpectator(player.getUniqueId());

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (match.getMatchState() != MatchState.ENDING) {
      if (!practicePlayerData.isSilent()) {
        match.broadcast(
            plugin.getMessagesConfig().getConfig().getString("MESSAGES.MATCH.SPECTATOR-LEFT")
                .replace("<player>", player.getName()));
      }
    }

    if (!practicePlayerData.getCachedPlayer().isEmpty()) {
      practicePlayerData.getCachedPlayer().clear();
    }

    this.spectators.remove(player.getUniqueId());
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);
  }

  public void savePostMatch(Match match) {
    if (match.isParty() || match.isFFA()) {
      return;
    }

    UUID playerOne = match.getTeams().get(0).getLeader();
    UUID playerTwo = match.getTeams().get(1).getLeader();
    PracticePlayerData dataOne = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(playerOne);
    PracticePlayerData dataTwo = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(playerTwo);

    processPostMatchData(match, playerOne, playerTwo, dataOne);
    processPostMatchData(match, playerTwo, playerOne, dataTwo);
  }

  private void processPostMatchData(Match match, UUID playerOne, UUID playerTwo,
      PracticePlayerData practicePlayerData) {
    if (practicePlayerData != null) {
      this.postMatchUuids.put(playerOne, playerTwo);
      InventorySnapshot snapshot = match.getSnapshot(playerTwo);
      if (snapshot != null) {
        this.postMatchInventories.put(playerOne, snapshot.getSnapshotId());
      }
      if (practicePlayerData.getPostMatchId() > -1) {
        plugin.getServer().getScheduler().cancelTask(practicePlayerData.getPostMatchId());
      }
    }
  }

  public void clearBlocks(Match match) {
    match.getPlacedBlocksLocations().forEach(location -> {
      location.getBlock().setType(Material.AIR);
      match.removePlacedBlock(location.getBlock());
    });
  }

  public UUID getPostMatchInventory(UUID uuid) {
    return this.postMatchInventories.get(uuid);
  }

  public boolean hasPostMatch(UUID uuid) {
    return this.postMatchUuids.containsKey(uuid);
  }

  public void processRequeue(Player player, Match match) {
    if (match.isParty() || match.isFFA() || match.isPartyMatch() || plugin.getManagerHandler()
        .getTournamentManager().isInTournament(player.getUniqueId())
        || plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId()) != null) {
      return;
    }

    PracticePlayerData playerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    this.playAgainKit.put(playerData.getUniqueId(), match.getKit());

    for (String message : CC.color(
        plugin.getMessagesConfig().getConfig().getStringList("MESSAGES.PLAY-AGAIN.MESSAGE"))) {
      Clickable clickable = new Clickable(message
          .replace("<kit>", match.getKit().getName())
          .replace("<kit_displayname>", match.getKit().getDisplayName()),
          color(plugin.getMessagesConfig().getConfig().getString("MESSAGES.PLAY-AGAIN.CLICKABLE")),
          "/playagain");

      clickable.sendToPlayer(player);
    }

    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
      if (player.isOnline()) {
        player.getInventory()
            .addItem(plugin.getManagerHandler().getItemManager().getPlayAgainItem().getItemStack());
      }
    }, 10L);

    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
      if (playerData.getPlayerState() == PlayerState.SPAWN && hasPlayAgainRequest(
          player.getUniqueId())
          && plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId()) == null) {
        player.getInventory()
            .setItem(plugin.getManagerHandler().getItemManager().getPlayAgainItem().getSlot(),
                new ItemStack(Material.AIR));
        player.updateInventory();
      }

      removePlayAgainRequest(player.getUniqueId());
    }, 20L * 30L);
  }

  public void removePlayAgainRequest(UUID uniqueId) {
    this.playAgainKit.remove(uniqueId);
  }

  public Kit getPlayAgainRequestKit(UUID uniqueId) {
    return this.playAgainKit.getOrDefault(uniqueId, null);
  }

  public boolean hasPlayAgainRequest(UUID uniqueId) {
    return this.playAgainKit.containsKey(uniqueId);
  }
}
