package dev.demeng.frost;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import dev.demeng.frost.commands.admin.CancelMatchCommand;
import dev.demeng.frost.commands.admin.GotoEventCommand;
import dev.demeng.frost.commands.admin.SetLootCommand;
import dev.demeng.frost.commands.admin.SetSpawnCommand;
import dev.demeng.frost.commands.admin.SpawnCommand;
import dev.demeng.frost.commands.user.TournamentCommand;
import dev.demeng.frost.database.MongoHandler;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.handler.CommandHandler;
import dev.demeng.frost.handler.ListenerHandler;
import dev.demeng.frost.handler.ManagerHandler;
import dev.demeng.frost.handler.MiscHandler;
import dev.demeng.frost.managers.chunk.ChunkRestorationManager;
import dev.demeng.frost.providers.papi.PlaceholderAPIProvider;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.FileConfig;
import dev.demeng.frost.util.file.Config;
import dev.demeng.frost.util.threads.Threads;
import dev.demeng.pluginbase.plugin.BasePlugin;
import dev.demeng.sentinel.wrapper.SentinelClient;
import dev.demeng.sentinel.wrapper.exception.ApiException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.elb1to.practice.controller.PracticeKnockbackController;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bstats.bukkit.Metrics;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

@Getter
public final class Frost extends BasePlugin {

  @Getter @Setter(AccessLevel.PRIVATE) private static Frost instance;

  private final Gson gson = new Gson();
  private final Random random = new Random();
  private final JsonParser jsonParser = new JsonParser();

  private Config mainConfig;
  private Config arenasConfig;
  private Config kitsConfig;

  private FileConfig settingsConfig;
  private FileConfig messagesConfig;
  private FileConfig scoreboardConfig;
  private FileConfig eventScoreboardConfig;
  private FileConfig tablistConfig;
  private FileConfig hotbarConfig;
  private FileConfig menusConfig;
  private FileConfig chestConfig;

  private MongoHandler mongoHandler;
  private ManagerHandler managerHandler;

  private boolean usingCustomKB = false;
  private long leaderboardUpdateTime;

  @Override
  public void enable() {
    setInstance(this);
    init();
  }

  @Override
  public void disable() {

    try {
      for (PracticePlayerData practicePlayerData : managerHandler.getPlayerManager().getAllData()) {
        Threads.executeData(() -> managerHandler.getPlayerManager().saveData(practicePlayerData));
      }

      managerHandler.getArenaManager().saveArenas();
      managerHandler.getKitManager().saveKits();

      for (Map.Entry<UUID, Match> entry : managerHandler.getMatchManager().getMatches()
          .entrySet()) {
        Match match = entry.getValue();
        if (match.getKit().isBuild() || match.getKit().isSpleef()) {
          ChunkRestorationManager.getIChunkRestoration().reset(match.getStandaloneArena());
        }
      }

      for (Entity entity : this.getServer().getWorld("world").getEntities()) {
        if (entity.getType() == EntityType.DROPPED_ITEM) {
          entity.remove();
        }
      }

      for (Chunk chunk : this.getServer().getWorld("world").getLoadedChunks()) {
        chunk.unload(true);
      }

      if (this.mongoHandler != null) {
        this.mongoHandler.close();
      }
    } catch (NullPointerException ignored) {
      // Avoid stack traces for uninitialized managers.
    }
  }

  public void init() {
    Threads.init();

    this.mainConfig = new Config("config", this);
    this.arenasConfig = new Config("arenas", this);
    this.kitsConfig = new Config("kits", this);
    this.settingsConfig = new FileConfig(this, "settings.yml");
    this.messagesConfig = new FileConfig(this, "messages.yml");
    this.scoreboardConfig = new FileConfig(this, "scoreboard.yml");
    this.eventScoreboardConfig = new FileConfig(this, "event-scoreboard.yml");
    this.tablistConfig = new FileConfig(this, "tablist.yml");
    this.hotbarConfig = new FileConfig(this, "hotbar.yml");
    this.menusConfig = new FileConfig(this, "menus.yml");
    this.chestConfig = new FileConfig(this, "chest.yml");

    // ============================================================================================
    // LICENSE AUTHENTICATION START - REMOVE IF COMPILING FROM SOURCE CODE

    final String licenseKey = settingsConfig.getConfig().getString("LICENSE-KEY", "NOT_SET");

    if (licenseKey.equals("NOT_SET")) {
      getLogger().severe("License key not found! Join https://demeng.dev/discord "
          + "and create a ticket to obtain your key.");
      getPluginLoader().disablePlugin(this);
      return;
    }

    final SentinelClient client = new SentinelClient(
        "https://sen.demeng.dev/api/v1", "nqgfbprlaponvbnrv11da0bmmc");

    String ip = SentinelClient.getCurrentIp();

    if (ip == null) {
      ip = "127.0.0.1";
    }

    try {
      client.getLicenseController().auth(licenseKey,
          "Frost", null, null, SentinelClient.getCurrentHwid(), ip);
    } catch (ApiException ex) {
      getLogger().severe("Failed to authenticate license: " + ex.getResponse().getMessage());
      getPluginLoader().disablePlugin(this);
      return;
    } catch (Throwable ex) {
      getLogger().severe(
          "Unexpected auth error: " + ex.getClass().getName() + " - " + ex.getMessage());
      getPluginLoader().disablePlugin(this);
      return;
    }

    // LICENSE AUTHENTICATION END
    // ============================================================================================

    if (getServer().getPluginManager().getPlugin("KnockbackController") == null) {
      getServer().getLogger().severe(
          "You don't have any KnockbackController for Frost in your plugins folder. "
              + "You won't be able to bind custom knockback to kits.");
    } else {
      usingCustomKB = true;
      getServer().getLogger().finest("This server is running " + PracticeKnockbackController.name
          + ", implementing custom knockback support.");
    }

    if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
      CC.setUsingPlaceholderAPI(true);
      new PlaceholderAPIProvider(this).register();
      getServer().getLogger()
          .finest("Placeholder API expansion and compatibility successfully registered.");
    }

    leaderboardUpdateTime =
        (settingsConfig.getConfig().getInt("SETTINGS.LEADERBOARDS.UPDATE-TIME") * 60L) * 20L;

    try {
      mongoHandler = new MongoHandler(this);
    } catch (Exception ex) {
      ex.printStackTrace();
      getLogger().severe("Failed to connect to MongoDB.");
      getPluginLoader().disablePlugin(this);
      return;
    }

    managerHandler = new ManagerHandler(this);
    managerHandler.register();

    this.registerCommands();
    new CommandHandler(this);
    new ListenerHandler(this);
    new MiscHandler(this);

    try {
      new Metrics(this, 18349);
    } catch (IllegalStateException ex) {
      if (!ex.getMessage().equals("bStats Metrics class has not been relocated correctly!")) {
        ex.printStackTrace();
      }
    }
  }

  private void registerCommands() {
    Arrays.asList(new GotoEventCommand(), new SetLootCommand(), new SpawnCommand(),
            new SetSpawnCommand(), new CancelMatchCommand(), new TournamentCommand())
        .forEach(command -> this.registerCommand(command, getName()));
  }

  public void registerCommand(Command cmd, String fallbackPrefix) {
    MinecraftServer.getServer().server.getCommandMap().register(cmd.getName(), fallbackPrefix, cmd);
  }
}
