package dev.demeng.frost.managers;

import static dev.demeng.frost.util.CC.sendMessage;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.queue.QueueEntry;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.RatingUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class QueueManager {

  private final Frost plugin = Frost.getInstance();
  private final Map<UUID, QueueEntry> queued = new ConcurrentHashMap<>();
  private final Map<UUID, Long> playerQueueTime = new HashMap<>();

  @Getter @Setter private boolean rankedEnabled = true;

  @Getter private final ConfigCursor matchCursor = new ConfigCursor(
      Frost.getInstance().getSettingsConfig(), "SETTINGS.MATCH.QUEUE-PING-LIMIT-SETTING");

  public QueueManager() {
    plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
      this.queued.forEach((uuid, queueEntry) -> {
        PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(uuid);
        if (practicePlayerData.getPlayerSettings().getPingRange() != -1) {
          practicePlayerData.tickPing(matchCursor);
        }
      });
      this.queued.forEach((key, value) -> {
        if (!value.isFound()) {
          if (value.isParty()) {
            this.findMatch(plugin.getManagerHandler().getPartyManager().getParty(key),
                value.getKitName(), value.getElo(), value.getQueueType());
          } else {
            this.findMatch(plugin.getServer().getPlayer(key), value.getKitName(), value.getElo(),
                value.getQueueType());
          }
        }
      });
    }, 20L, 20L);
  }

  public void addPlayerToQueue(Player player, PracticePlayerData practicePlayerData, String kitName,
      QueueType type) {
    if (type != QueueType.UNRANKED && !this.rankedEnabled) {
      player.closeInventory();
      return;
    }

    practicePlayerData.resetPing();
    practicePlayerData.setPlayerState(PlayerState.QUEUE);

    if (practicePlayerData.isFollowing()) {
      practicePlayerData.setFollowing(false);
      practicePlayerData.setFollowingId(null);
    }

    int elo = type == QueueType.RANKED ? practicePlayerData.getElo(kitName)
        : type == QueueType.PREMIUM ? practicePlayerData.getPremiumElo() : 0;

    QueueEntry entry = new QueueEntry(type, kitName, elo, false);
    this.queued.put(practicePlayerData.getUniqueId(), entry);
    this.giveQueueItems(player);

    for (String message : plugin.getMessagesConfig().getConfig()
        .getStringList("MESSAGES.QUEUE.JOINED-SOLO")) {
      sendMessage(player,
          message.replace("<queue_type>", type.getName()).replace("<kit_name>", kitName));
    }

    this.playerQueueTime.put(player.getUniqueId(), System.currentTimeMillis());
  }

  private void giveQueueItems(Player player) {
    player.closeInventory();
    player.getInventory().clear();

    plugin.getManagerHandler().getItemManager().getQueueItems()
        .stream()
        .filter(ItemManager.HotbarItem::isEnabled)
        .forEach(item -> player.getInventory().setItem(item.getSlot(), item.getItemStack())
        );

    player.updateInventory();
  }

  public QueueEntry getQueueEntry(UUID uuid) {
    return this.queued.get(uuid);
  }

  public long getPlayerQueueTime(UUID uuid) {
    return this.playerQueueTime.get(uuid);
  }

  public int getQueueSize(String ladder, QueueType type) {
    return (int) this.queued.entrySet().stream()
        .filter(entry -> entry.getValue().getQueueType() == type)
        .filter(entry -> entry.getValue().getKitName().equals(ladder)).count();
  }

  public int getQueueSize() {
    return this.queued.entrySet().size();
  }

  public int getQueueSize(QueueType type) {
    return (int) this.queued.entrySet().stream()
        .filter(entry -> entry.getValue().getQueueType() == type).count();
  }

  private void findMatch(Player player, String kitName, int elo, QueueType type) {
    if (player == null) {
      return;
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData == null) {
      plugin.getLogger().warning(
          player.getName() + "'s player data is null" + "(" + this.getClass().getName() + ")");
      return;
    }

    int ping = PlayerUtil.getPing(player);
    for (Map.Entry<UUID, QueueEntry> queueData : this.queued.entrySet()) {
      UUID opponent = queueData.getKey();
      QueueEntry queueEntry = queueData.getValue();
      if (opponent == player.getUniqueId()) {
        continue;
      }

      if (!queueEntry.getKitName().equals(kitName)) {
        continue;
      }
      if (queueEntry.getQueueType() != type) {
        continue;
      }
      if (queueEntry.isParty()) {
        continue;
      }
      if (queueEntry.isFound()) {
        continue;
      }

      Player opponentPlayer = plugin.getServer().getPlayer(opponent);
      int opponentPing = PlayerUtil.getPing(opponentPlayer);
      PracticePlayerData opponentData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(opponent);

      int minPing = practicePlayerData.getMinPing();
      int maxPing = practicePlayerData.getMaxPing();
      int minPingOpponent = opponentData.getMinPing();
      int maxPingOpponent = opponentData.getMaxPing();

      if (matchCursor.getBoolean(type.toString())) {
        if (practicePlayerData.getPlayerSettings().getPingRange() != -1
            || opponentData.getPlayerSettings().getPingRange() != -1) {
          if ((opponentPing > maxPing || opponentPing < minPing) || (ping > maxPingOpponent
              || ping < minPingOpponent)) {
            continue;
          }
        }
      }

      Kit kit = plugin.getManagerHandler().getKitManager().getKit(kitName);
      Arena arena = plugin.getManagerHandler().getArenaManager().getRandomArena(kit);
      ConfigCursor matchMessage = new ConfigCursor(Frost.getInstance().getMessagesConfig(),
          "MESSAGES.MATCH");

      if (arena == null) {
        player.sendMessage(ChatColor.RED + "There are no arenas available for this kit.");
        removePlayerFromQueue(player);
        return;
      }

      if (type.isRanked()) {
        for (String info : matchMessage.getStringList("RANKED-STARTING")) {
          sendRankedMessage(player, kitName, opponentPlayer, arena, info);
          sendRankedMessage(opponentPlayer, kitName, player, arena, info);
        }
      } else if (type.isPremium()) {
        for (String info : matchMessage.getStringList("PREMIUM-STARTING")) {
          sendRankedMessage(player, kitName, opponentPlayer, arena, info);
          sendRankedMessage(opponentPlayer, kitName, player, arena, info);
        }
      } else {
        for (String info : matchMessage.getStringList("STARTING")) {
          sendUnrankedMessage(player, kitName, opponentPlayer, arena, info);
          sendUnrankedMessage(opponentPlayer, kitName, player, arena, info);
        }
      }

      MatchTeam teamA = new MatchTeam(player.getUniqueId(),
          Collections.singletonList(player.getUniqueId()), 0);
      MatchTeam teamB = new MatchTeam(opponentPlayer.getUniqueId(),
          Collections.singletonList(opponentPlayer.getUniqueId()), 1);
      Match match = new Match(arena, kit, type, teamA, teamB);

      QueueEntry queueEntry1 = queued.get(player.getUniqueId());
      if (queueEntry1 != null) { // should not be null, but, things happen.gi
        queueEntry1.setFound(true);
      }
      queueEntry.setFound(true);

      this.queued.remove(player.getUniqueId());
      this.queued.remove(opponentPlayer.getUniqueId());

      this.playerQueueTime.remove(player.getUniqueId());
      this.playerQueueTime.remove(opponentPlayer.getUniqueId());

      plugin.getManagerHandler().getMatchManager().createMatch(match);

      return;
    }
  }

  private void sendRankedMessage(Player player, String kitName, Player opponentPlayer, Arena arena,
      String info) {
    sendMessage(player, info.replace("<kit_name>", kitName)
        .replace("<arena_name>", arena.getName())
        .replace("<opponent_name>", opponentPlayer.getName())
        .replace("<opponent_elo>",
            String.valueOf(this.queued.get(opponentPlayer.getUniqueId()).getElo()))
        .replace("<opponent_rating>", String.valueOf(
            RatingUtil.getRankByElo(this.queued.get(opponentPlayer.getUniqueId()).getElo())
                .getName()))
        .replace("<opponent_ping>", String.valueOf(PlayerUtil.getPing(opponentPlayer)))
    );
  }

  private void sendUnrankedMessage(Player player, String kitName, Player opponentPlayer,
      Arena arena, String info) {
    sendMessage(player, info
        .replace("<kit_name>", kitName)
        .replace("<arena_name>", arena.getName())
        .replace("<opponent_name>", opponentPlayer.getName())
        .replace("<opponent_ping>", String.valueOf(PlayerUtil.getPing(opponentPlayer)))
    );
  }

  public void removePlayerFromQueue(Player player) {
    if (player == null) {
      return;
    }

    QueueEntry entry = this.queued.get(player.getUniqueId());
    if (entry == null) {
      return;
    }

    this.queued.remove(player.getUniqueId());
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, false);
    sendMessage(player, plugin.getMessagesConfig().getConfig().getString("MESSAGES.QUEUE.LEFT")
        .replace("<kit_name>", entry.getKitName())
        .replace("<queue_type>", entry.getQueueType().getName())
    );
  }

  public void addPartyToQueue(Player leader, Party party, String kitName) {
    if (party.getMembers().size() != 2) {
      leader.sendMessage(
          ChatColor.RED + "There must be at least 2 players in your party to do this.");
      leader.closeInventory();
    } else {
      party.getMembers().stream().map(plugin.getManagerHandler().getPlayerManager()::getPlayerData)
          .forEach(member -> member.setPlayerState(PlayerState.QUEUE));

      PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(leader.getUniqueId());
      this.queued.put(practicePlayerData.getUniqueId(),
          new QueueEntry(QueueType.UNRANKED, kitName, -1, true));
      this.giveQueueItems(leader);

      for (String message : plugin.getMessagesConfig().getConfig()
          .getStringList("MESSAGES.QUEUE.JOINED-PARTY")) {
        party.broadcast(message
            .replace("<queue_type>", QueueType.UNRANKED.getName())
            .replace("<kit_name>", kitName)
        );
      }

      this.playerQueueTime.put(party.getLeader(), System.currentTimeMillis());
      this.findMatch(party, kitName, -1, QueueType.UNRANKED);
    }
  }

  private void findMatch(Party partyA, String kitName, int elo, QueueType type) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(partyA.getLeader());
    UUID opponent = this.queued.entrySet().stream()
        .filter(entry -> entry.getKey() != partyA.getLeader())
        .filter(entry -> plugin.getManagerHandler().getPlayerManager().getPlayerData(entry.getKey())
            != null)
        .filter(entry -> plugin.getManagerHandler().getPlayerManager().getPlayerData(entry.getKey())
            .getPlayerState() == PlayerState.QUEUE)
        .filter(entry -> entry.getValue().isParty())
        .filter(entry -> entry.getValue().getQueueType() == type)
        .filter(entry -> !type.isRanked())
        .filter(entry -> entry.getValue().getKitName().equals(kitName))
        .map(Map.Entry::getKey)
        .findFirst().orElse(null);

    if (opponent == null) {
      return;
    }

    PracticePlayerData opponentData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(opponent);
    if (opponentData.getPlayerState() == PlayerState.FIGHTING) {
      return;
    }
    if (practicePlayerData.getPlayerState() == PlayerState.FIGHTING) {
      return;
    }

    Player leaderA = plugin.getServer().getPlayer(partyA.getLeader());
    Player leaderB = plugin.getServer().getPlayer(opponent);

    Kit kit = plugin.getManagerHandler().getKitManager().getKit(kitName);
    Arena arena = plugin.getManagerHandler().getArenaManager().getRandomArena(kit);
    Party partyB = plugin.getManagerHandler().getPartyManager().getParty(opponent);

    List<UUID> playersA = new ArrayList<>(partyA.getMembers());
    List<UUID> playersB = new ArrayList<>(partyB.getMembers());

    MatchTeam teamA = new MatchTeam(leaderA.getUniqueId(), playersA, 0);
    MatchTeam teamB = new MatchTeam(leaderB.getUniqueId(), playersB, 1);

    Match match = new Match(arena, kit, type, teamA, teamB);
    plugin.getManagerHandler().getMatchManager().createMatch(match);

    if (type.isRanked()) {
      for (String info : plugin.getMessagesConfig().getConfig()
          .getStringList("MESSAGES.MATCH.PARTY-STARTING")) {
        unrankedPartyMessage(kitName, arena, partyA, playersB, info);
        unrankedPartyMessage(kitName, arena, partyB, playersA, info);
      }
    }

    this.queued.remove(partyA.getLeader());
    this.queued.remove(partyB.getLeader());
  }

  private void unrankedPartyMessage(String kitName, Arena arena, Party party, List<UUID> players,
      String info) {
    party.broadcast(info
        .replace("<kit_name>", kitName)
        .replace("<arena_name>", arena.getName())

        .replace("<opponent_a>", Bukkit.getPlayer(players.get(0)).getName())
        .replace("<opponent_b>", Bukkit.getPlayer(players.get(1)).getName())
        .replace("<opponent_a_ping>",
            String.valueOf(PlayerUtil.getPing(Bukkit.getPlayer(players.get(0)))))
        .replace("<opponent_b_ping>",
            String.valueOf(PlayerUtil.getPing(Bukkit.getPlayer(players.get(1)))))
    );
  }

  public void removePartyFromQueue(Party party) {
    if (party == null) {
      return;
    }

    QueueEntry entry = this.queued.get(party.getLeader());
    this.queued.remove(party.getLeader());

    party.members().forEach(
        player -> plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, false));
    party.broadcast(plugin.getMessagesConfig().getConfig().getString("MESSAGES.QUEUE.LEFT")
        .replace("<kit_name>", entry.getKitName())
        .replace("<queue_type>", entry.getQueueType().getName())
    );
  }
}
