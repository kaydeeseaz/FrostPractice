package dev.demeng.frost.user.player;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.kit.PlayerKit;
import dev.demeng.frost.util.MathUtil;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.time.Cooldown;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@RequiredArgsConstructor
public class PracticePlayerData {

  public static final int DEFAULT_ELO = 1000;

  private final Map<String, Map<Integer, PlayerKit>> playerKits = new ConcurrentHashMap<>();
  private final Map<String, Integer> currentWinstreak = new ConcurrentHashMap<>();
  private final Map<String, Integer> highestWinStreak = new ConcurrentHashMap<>();
  private final Map<String, Integer> rankedLosses = new ConcurrentHashMap<>();
  private final Map<String, Integer> rankedWins = new ConcurrentHashMap<>();
  private final Map<String, Integer> rankedElo = new ConcurrentHashMap<>();
  private final Map<String, PlayerKit[]> kits = new ConcurrentHashMap<>();

  private Cooldown partyAlertCooldown = new Cooldown(0);
  private Cooldown playerCommandCooldown = new Cooldown(0);

  private Map<Player, Player> cachedPlayer = new ConcurrentHashMap<>();

  private final UUID uniqueId;
  private String username;
  private UUID followingId;

  private PlayerState playerState = PlayerState.LOADING;
  private PlayerSettings playerSettings = new PlayerSettings();

  private boolean silent;
  private boolean following;

  private UUID currentMatchID;
  private UUID duelSelecting;

  private int teamId = -1;
  private int postMatchId = -1;
  private int missedPots;
  private int thrownPots;
  private int longestCombo;
  private int combo;
  private long lastDamagedMillis = 0;
  private int hits;

  private int matchesPlayed;
  private int premiumMatches;
  private int premiumElo = PracticePlayerData.DEFAULT_ELO;
  private int globalElo = PracticePlayerData.DEFAULT_ELO;

  private int globalWinStreak;
  private int globalHighestWinStreak;

  private Kit selectedLadder;
  private PlayerKit selectedKit;
  private boolean active;
  private boolean rename;
  private boolean dataLoaded;

  private ItemStack[] currentKitContents = new ItemStack[36];
  private ItemStack[] currentKitArmor = new ItemStack[4];

  public PlayerKit[] getKits(Kit ladder) {
    return this.kits.get(ladder.getName());
  }

  public PlayerKit getKit(Kit ladder, int index) {
    return this.kits.get(ladder.getName())[index];
  }

  public void replaceKit(Kit ladder, int index, PlayerKit kit) {
    PlayerKit[] kits = this.kits.get(ladder.getName());
    kits[index] = kit;

    this.kits.put(ladder.getName(), kits);
  }

  public void deleteKit(Kit ladder, PlayerKit kit) {
    if (kit == null) {
      return;
    }

    PlayerKit[] kits = this.kits.get(ladder.getName());
    for (int i = 0; i < 4; i++) {
      if (kits[i] != null && kits[i].equals(kit)) {
        kits[i] = null;
        break;
      }
    }

    this.kits.put(ladder.getName(), kits);
  }

  public int getWins(String kitName) {
    return this.rankedWins.computeIfAbsent(kitName, k -> 0);
  }

  public void setWins(String kitName, int wins) {
    this.rankedWins.put(kitName, wins);
  }

  public int getLosses(String kitName) {
    return this.rankedLosses.computeIfAbsent(kitName, k -> 0);
  }

  public void setLosses(String kitName, int losses) {
    this.rankedLosses.put(kitName, losses);
  }

  public int getCurrentWinstreak(String kitName) {
    return this.currentWinstreak.computeIfAbsent(kitName, k -> 0);
  }

  public void setCurrentWinstreak(String kitName, int streak) {
    this.currentWinstreak.put(kitName, streak);
  }

  public int getHighestWinStreak(String kitName) {
    return this.highestWinStreak.computeIfAbsent(kitName, k -> 0);
  }

  public void setHighestWinStreak(String kitName, int streak) {
    this.highestWinStreak.put(kitName, streak);
  }

  public int getElo(String kitName) {
    return this.rankedElo.computeIfAbsent(kitName, k -> PracticePlayerData.DEFAULT_ELO);
  }

  public void setElo(String kitName, int elo) {
    this.rankedElo.put(kitName, elo);
  }

  public void addPlayerKit(int index, PlayerKit playerKit) {
    this.getPlayerKits(playerKit.getName()).put(index, playerKit);
  }

  public boolean isInSpawn() {
    return (this.playerState == PlayerState.SPAWN);
  }

  public boolean isInEvent() {
    return (this.playerState == PlayerState.EVENT);
  }

  public boolean isInMatch() {
    return (this.playerState == PlayerState.FIGHTING);
  }

  public boolean isQueueing() {
    return (this.playerState == PlayerState.QUEUE);
  }

  public boolean isSpectating() {
    return (this.playerState == PlayerState.SPECTATING);
  }

  public boolean isInTournament() {
    return Frost.getInstance().getManagerHandler().getTournamentManager()
        .isInTournament(this.uniqueId);
  }

  public boolean isRenaming() {
    return this.active && this.rename && this.selectedKit != null;
  }

  public boolean isInParty() {
    return Frost.getInstance().getManagerHandler().getPartyManager().getParty(this.uniqueId)
        != null;
  }

  public Map<Integer, PlayerKit> getPlayerKits(String kitName) {
    return this.playerKits.computeIfAbsent(kitName, k -> new HashMap<>());
  }

  private boolean tickedPing = false;

  private int varMinPing = 0;
  private int varMaxPing = 0;

  private int minPing = 0;
  private int maxPing = 0;

  public void resetPing() {
    tickedPing = false;
    varMinPing = 0;
    varMaxPing = 0;
    minPing = 0;
    maxPing = 0;
  }

  public void tickPing(ConfigCursor matchCursor) {
    int playerPing = PlayerUtil.getPing(Bukkit.getPlayer(this.getUniqueId()));
    boolean minus = Math.random() > matchCursor.getDouble("MAX-PING-MINUS-PROBABILITY", 0.80);
    boolean plus = Math.random() > matchCursor.getDouble("MIN-PING-PLUS-PROBABILITY", 0.65);

    if (minus) {
      varMaxPing -= MathUtil.randomNumber(1, 3);
    } else {
      varMaxPing += MathUtil.randomNumber(1, 3);
    }

    if (plus) {
      varMinPing += MathUtil.randomNumber(1, 3);
    } else {
      varMinPing -= MathUtil.randomNumber(1, 3);
    }

    minPing = playerPing - (this.getPlayerSettings().getPingRange() / 2);
    maxPing = playerPing + (this.getPlayerSettings().getPingRange() / 2);

    minPing += varMinPing;
    maxPing += varMaxPing;

    tickedPing = true;
  }

  public void setHits(int hits) {
    if (System.currentTimeMillis() - lastDamagedMillis < 250) {
      return;
    }

    this.hits = hits;
    this.lastDamagedMillis = System.currentTimeMillis();
  }
}
