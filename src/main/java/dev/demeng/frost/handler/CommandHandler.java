package dev.demeng.frost.handler;

import dev.demeng.frost.Frost;
import dev.demeng.frost.commands.admin.ForceMenuUpdateCommand;
import dev.demeng.frost.commands.admin.ForceQueueCommand;
import dev.demeng.frost.commands.admin.PremiumMatchCommand;
import dev.demeng.frost.commands.admin.SilentCommand;
import dev.demeng.frost.commands.admin.arena.ArenaChunkCommands;
import dev.demeng.frost.commands.admin.arena.ArenaCommand;
import dev.demeng.frost.commands.admin.arena.ArenaSetCommand;
import dev.demeng.frost.commands.admin.kit.KitCommand;
import dev.demeng.frost.commands.admin.kit.KitGetCommand;
import dev.demeng.frost.commands.admin.kit.KitSetCommand;
import dev.demeng.frost.commands.admin.kit.KitToggleCommand;
import dev.demeng.frost.commands.admin.stats.PlayerDebugCommand;
import dev.demeng.frost.commands.admin.stats.PlayerStatsCommand;
import dev.demeng.frost.commands.admin.stats.ResetCommand;
import dev.demeng.frost.commands.exceptions.CommandExceptionHandler;
import dev.demeng.frost.commands.user.FlyCommand;
import dev.demeng.frost.commands.user.FollowCommands;
import dev.demeng.frost.commands.user.FrostCommand;
import dev.demeng.frost.commands.user.LeaderboardCommand;
import dev.demeng.frost.commands.user.LeaveCommand;
import dev.demeng.frost.commands.user.MatchCommands;
import dev.demeng.frost.commands.user.MatchHistoryCommand;
import dev.demeng.frost.commands.user.PartyCommands;
import dev.demeng.frost.commands.user.PostMatchInventoryCommand;
import dev.demeng.frost.commands.user.ProfileCommand;
import dev.demeng.frost.commands.user.QueueCommand;
import dev.demeng.frost.commands.user.SettingsCommands;
import dev.demeng.frost.commands.user.ShowMenuCommand;
import dev.demeng.frost.commands.user.event.EventCommand;
import dev.demeng.frost.commands.user.event.JoinEventCommand;
import dev.demeng.frost.commands.user.event.SpectateEventCommand;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.user.player.PlayerSettings;
import dev.demeng.frost.user.player.PracticePlayerData;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public class CommandHandler {

  private final Frost plugin;
  private final BukkitCommandHandler handler;

  public CommandHandler(Frost plugin) {
    this.plugin = plugin;

    handler = BukkitCommandHandler.create(plugin);
    handler.registerDependency(Frost.class, plugin);
    handler.setExceptionHandler(new CommandExceptionHandler());

    handler.registerValueResolver(Kit.class,
        context -> plugin.getManagerHandler().getKitManager().getKit(context.pop()));
    handler.registerValueResolver(Arena.class,
        context -> plugin.getManagerHandler().getArenaManager().getArena(context.pop()));

    handler.registerContextResolver(PracticePlayerData.class,
        context -> plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(context.actor().getUniqueId()));
    handler.registerContextResolver(PlayerSettings.class,
        context -> plugin.getManagerHandler().getPlayerManager()
            .getPlayerData(context.actor().getUniqueId()).getPlayerSettings());

    handler.getAutoCompleter().registerParameterSuggestions(Kit.class,
        (args, sender, command) -> plugin.getManagerHandler().getKitManager().getKitNames());
    handler.getAutoCompleter().registerParameterSuggestions(Arena.class,
        (args, sender, command) -> plugin.getManagerHandler().getArenaManager().getArenas()
            .keySet());
    handler.getAutoCompleter().registerParameterSuggestions(Player.class,
        (args, sender, command) -> plugin.getServer().getOnlinePlayers().stream()
            .map(Player::getName).collect(Collectors.toList()));

    register();
  }

  private void register() {
    handler.register(new FrostCommand());

    handler.register(new KitCommand());
    handler.register(new KitSetCommand());
    handler.register(new KitGetCommand());
    handler.register(new KitToggleCommand());

    handler.register(new ArenaCommand());
    handler.register(new ArenaSetCommand());

    handler.register(new FlyCommand());
    handler.register(new ProfileCommand());
    handler.register(new FollowCommands());
    handler.register(new QueueCommand());
    handler.register(new LeaderboardCommand());
    handler.register(new PartyCommands(plugin));
    handler.register(new MatchHistoryCommand());
    handler.register(new MatchCommands(plugin));
    handler.register(new SettingsCommands(plugin));
    handler.register(new PostMatchInventoryCommand());

    handler.register(new ResetCommand());
    handler.register(new SilentCommand());
    handler.register(new ShowMenuCommand());
    handler.register(new ForceQueueCommand());
    handler.register(new PlayerStatsCommand());
    handler.register(new PlayerDebugCommand());
    handler.register(new ArenaChunkCommands());
    handler.register(new PremiumMatchCommand());

    handler.register(new EventCommand());
    handler.register(new LeaveCommand());
    handler.register(new JoinEventCommand());
    handler.register(new SpectateEventCommand());

    handler.register(new ForceMenuUpdateCommand());
  }
}
