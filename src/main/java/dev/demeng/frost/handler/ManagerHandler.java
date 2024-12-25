package dev.demeng.frost.handler;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.ffa.FfaManager;
import dev.demeng.frost.managers.ArenaManager;
import dev.demeng.frost.managers.EventManager;
import dev.demeng.frost.managers.InventoryManager;
import dev.demeng.frost.managers.ItemManager;
import dev.demeng.frost.managers.KitManager;
import dev.demeng.frost.managers.MatchManager;
import dev.demeng.frost.managers.PartyManager;
import dev.demeng.frost.managers.PlayerManager;
import dev.demeng.frost.managers.QueueManager;
import dev.demeng.frost.managers.SpawnManager;
import dev.demeng.frost.managers.TournamentManager;
import dev.demeng.frost.managers.chest.ChestManager;
import dev.demeng.frost.managers.chunk.ChunkManager;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.managers.leaderboard.LeaderboardManager;
import dev.demeng.frost.managers.leaderboard.holograms.NyaHologramManager;
import dev.demeng.frost.providers.board.ScoreboardProvider;
import dev.demeng.frost.scoreboard.Aether;
import dev.demeng.frost.user.player.match.MatchLocatedData;
import dev.demeng.frost.util.timer.TimerManager;
import dev.demeng.frost.util.timer.impl.BridgeArrowTimer;
import dev.demeng.frost.util.timer.impl.EnderpearlTimer;
import dev.demeng.frost.util.timer.impl.GlockTimer;
import java.util.Iterator;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;

@Getter
public class ManagerHandler {

  private final Frost plugin;

  private InventoryManager inventoryManager;
  private PlayerManager playerManager;
  private ArenaManager arenaManager;
  private MatchManager matchManager;
  private PartyManager partyManager;
  private QueueManager queueManager;
  private EventManager eventManager;
  private ItemManager itemManager;
  private KitManager kitManager;
  private FfaManager ffaManager;
  private SpawnManager spawnManager;
  private TournamentManager tournamentManager;
  private ChestManager chestManager;
  private ChunkManager chunkManager;
  private TimerManager timerManager;
  private LeaderboardManager leaderboardManager;
  private NyaHologramManager nyaHologramManager;
  private MatchLocatedData matchLocatedData;
  private ChunkRestorationManager chunkRestorationManager;

  private Aether aether;

  public ManagerHandler(Frost plugin) {
    this.plugin = plugin;
  }

  public void register() {
    itemManager = new ItemManager();
    spawnManager = new SpawnManager();
    chunkRestorationManager = new ChunkRestorationManager();
    arenaManager = new ArenaManager();
    chunkManager = new ChunkManager();

    kitManager = new KitManager();
    matchManager = new MatchManager();
    partyManager = new PartyManager();
    playerManager = new PlayerManager();
    queueManager = new QueueManager();
    eventManager = new EventManager();
    chestManager = new ChestManager(plugin);
    timerManager = new TimerManager(plugin);
    matchLocatedData = new MatchLocatedData();
    inventoryManager = new InventoryManager();
    tournamentManager = new TournamentManager();
    ffaManager = new FfaManager(plugin);

    leaderboardManager = new LeaderboardManager(plugin);
    nyaHologramManager = new NyaHologramManager(plugin);

    timerManager.registerTimer(new GlockTimer());
    timerManager.registerTimer(new EnderpearlTimer());
    timerManager.registerTimer(new BridgeArrowTimer());

    removeCrafting(Material.SNOW_BLOCK);
    removeCrafting(Material.WORKBENCH);
    removeCrafting(Material.FURNACE);

    aether = new Aether(plugin, new ScoreboardProvider());
  }

  private void removeCrafting(Material material) {
    Iterator<Recipe> iterator = plugin.getServer().recipeIterator();
    while (iterator.hasNext()) {
      Recipe recipe = iterator.next();
      if (recipe.getResult().getType() == material) {
        iterator.remove();
      }
    }
  }
}
