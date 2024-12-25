package dev.demeng.frost.listeners;

import static dev.demeng.frost.util.CC.color;
import static dev.demeng.frost.util.CC.sendMessage;

import com.google.common.collect.Sets;
import dev.demeng.frost.Frost;
import dev.demeng.frost.events.EventState;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.events.games.dropper.DropperEvent;
import dev.demeng.frost.events.games.gulag.GulagEvent;
import dev.demeng.frost.events.games.oitc.OITCEvent;
import dev.demeng.frost.events.games.oitc.OITCPlayer;
import dev.demeng.frost.events.games.parkour.ParkourEvent;
import dev.demeng.frost.events.games.thimble.ThimbleEvent;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.kit.PlayerKit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchState;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.managers.ItemManager;
import dev.demeng.frost.scoreboard.Aether;
import dev.demeng.frost.scoreboard.event.BoardCreateEvent;
import dev.demeng.frost.scoreboard.scoreboard.Board;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.editor.SelectLadderKitMenu;
import dev.demeng.frost.user.ui.host.EventHostingMenu;
import dev.demeng.frost.user.ui.matches.OngoingMatchesMenu;
import dev.demeng.frost.user.ui.players.PlayersMenu;
import dev.demeng.frost.user.ui.queue.QueuesMenu;
import dev.demeng.frost.user.ui.queue.ffa.FFASelectionMenu;
import dev.demeng.frost.user.ui.settings.SettingsMenu;
import dev.demeng.frost.util.PlayerUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

public class PlayerListener implements Listener {

  private final Frost plugin = Frost.getInstance();
  private final List<String> blockedCommands = new ArrayList<>(
      plugin.getSettingsConfig().getConfig().getStringList("SETTINGS.MATCH.BLOCKED-COMMANDS"));

  private final Set<Material> blockedBlocks = Sets.newHashSet(
      Material.WORKBENCH, Material.FURNACE, Material.ANVIL, Material.ENCHANTMENT_TABLE,
      Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
      Material.DISPENSER, Material.DROPPER, Material.HOPPER,
      Material.BREWING_STAND, Material.BEACON,
      Material.JUKEBOX, Material.NOTE_BLOCK,
      Material.CAKE, Material.BED_BLOCK
  );

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    String command = event.getMessage().split(" ")[0];
    Player player = event.getPlayer();

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.isRenaming()) {
      player.sendMessage(ChatColor.RED + "A kit name cannot start with \"/\".");
      player.sendMessage(ChatColor.RED + "Event cancelled.");
      practicePlayerData.setActive(false);
      practicePlayerData.setRename(false);
      event.setCancelled(true);
    }

    if (!practicePlayerData.isInSpawn()) {
      blockedCommands.forEach(blockedCmd -> {
        if (command.equalsIgnoreCase(blockedCmd)) {
          sendMessage(player, plugin.getMessagesConfig().getConfig()
              .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
          event.setCancelled(true);
        }
      });
    }
  }

  @EventHandler
  @SuppressWarnings("deprecation")
  public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
    ItemStack item = event.getItem();
    if (item == null) {
      return;
    }

    Material type = item.getType();
    Player player = event.getPlayer();
    PracticePlayerData playerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    PlayerState state = playerData.getPlayerState();
    if (state == PlayerState.SPAWN || state == PlayerState.EDITING
        || state == PlayerState.SPECTATING) {
      return;
    }

    if (type.getId() == 373 && plugin.getSettingsConfig().getConfig()
        .getBoolean("SETTINGS.MATCH.REMOVE-BOTTLE")) {
      plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
        player.setItemInHand(new ItemStack(Material.AIR));
        player.updateInventory();
      }, 1L);
      return;
    }
    if (type != Material.GOLDEN_APPLE) {
      return;
    }
    if (state != PlayerState.FIGHTING) {
      return;
    }

    Match match = plugin.getManagerHandler().getMatchManager().getMatch(playerData);
    if (match.getKit().isBridges()) {
      player.setHealth(player.getMaxHealth());
      plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin,
          () -> player.removePotionEffect(PotionEffectType.REGENERATION), 1L);
    } else {
      if (!item.hasItemMeta()) {
        return;
      }
      if (!item.getItemMeta().hasDisplayName()) {
        return;
      }
      if (!item.getItemMeta().getDisplayName().contains("Golden Head")) {
        return;
      }

      player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
      player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
      player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
    }
  }

  @EventHandler
  public void onRegenerate(EntityRegainHealthEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
      return;
    }

    Player player = (Player) event.getEntity();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    switch (practicePlayerData.getPlayerState()) {
      case FIGHTING:
        Match match = plugin.getManagerHandler().getMatchManager().getMatch(practicePlayerData);
        if (match.getKit().isNoRegen()) {
          event.setCancelled(true);
        }
        break;
      case EVENT:
        if (plugin.getManagerHandler().getEventManager()
            .getEventPlaying(player) instanceof GulagEvent) {
          event.setCancelled(true);
        }
        break;
    }
  }

  @EventHandler
  public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    Player player = Bukkit.getServer().getPlayerExact(event.getName());
    if (player == null) {
      return;
    }

    if (player.isOnline()) {
      event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          "§cA player with the same name is already connected!\n§cPlease try again later.");
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.setJoinMessage(null);

    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    plugin.getManagerHandler().getPlayerManager().resetPlayerOrSpawn(player, true);

    if (Board.getByPlayer(player) == null) {
      Aether board = plugin.getManagerHandler().getAether();
      Bukkit.getPluginManager()
          .callEvent(new BoardCreateEvent(new Board(player, board, board.getOptions()), player));
    }

    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
      player.setPlayerTime(practicePlayerData.getPlayerSettings().getPlayerTime().getTime(), false);
    }, 20L);

    plugin.getManagerHandler().getNyaHologramManager().show(player);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    event.setQuitMessage(null);
    Player player = event.getPlayer();
    UUID uniqueId = player.getUniqueId();
    Party party = plugin.getManagerHandler().getPartyManager().getParty(uniqueId);

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(uniqueId);
    if (practicePlayerData == null) {
      return;
    }

    Board board = Board.getByPlayer(player);
    if (board != null) {
      Board.getBoards().remove(board);
    }

    practicePlayerData.setSilent(false);
    practicePlayerData.setFollowing(false);
    practicePlayerData.setFollowingId(null);

    practicePlayerData.setCurrentKitContents(null);
    practicePlayerData.setCurrentKitArmor(null);

    switch (practicePlayerData.getPlayerState()) {
      case FIGHTING:
        plugin.getManagerHandler().getMatchManager()
            .removeFighter(player, practicePlayerData, false);
        break;
      case SPECTATING:
        if (plugin.getManagerHandler().getEventManager().getSpectators().containsKey(uniqueId)) {
          plugin.getManagerHandler().getEventManager().removeSpectator(player,
              plugin.getManagerHandler().getEventManager().getOngoingEvent());
        } else {
          plugin.getManagerHandler().getMatchManager().removeSpectator(player);
        }
        break;
      case QUEUE:
        if (party == null) {
          plugin.getManagerHandler().getQueueManager().removePlayerFromQueue(player);
        } else if (plugin.getManagerHandler().getPartyManager().isLeader(uniqueId)) {
          plugin.getManagerHandler().getQueueManager().removePartyFromQueue(party);
        }
        break;
      case FFA:
        plugin.getManagerHandler().getFfaManager().getByPlayer(player).removePlayer(player);
        break;
      case EVENT:
        PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
            .getEventPlaying(player);
        if (practiceEvent != null) {
          practiceEvent.leave(player);
        }
        break;
    }

    practicePlayerData.getCachedPlayer().clear();
    plugin.getManagerHandler().getTournamentManager().leaveTournament(player);
    plugin.getManagerHandler().getPartyManager().leaveParty(player);
    plugin.getManagerHandler().getMatchManager().removeMatchRequests(uniqueId);
    plugin.getManagerHandler().getPartyManager().removePartyInvites(uniqueId);
    plugin.getManagerHandler().getPlayerManager().removePlayerData(practicePlayerData);

    plugin.getManagerHandler().getNyaHologramManager().hide(player);
  }

  @EventHandler
  public void onPlayerKick(PlayerKickEvent event) {
    Player player = event.getPlayer();
    UUID uniqueId = player.getUniqueId();
    Party party = plugin.getManagerHandler().getPartyManager().getParty(uniqueId);

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(uniqueId);
    if (practicePlayerData == null) {
      return;
    }

    Board board = Board.getByPlayer(player);
    if (board != null) {
      Board.getBoards().remove(board);
    }

    practicePlayerData.setSilent(false);
    practicePlayerData.setFollowing(false);
    practicePlayerData.setFollowingId(null);

    practicePlayerData.setCurrentKitContents(null);
    practicePlayerData.setCurrentKitArmor(null);

    switch (practicePlayerData.getPlayerState()) {
      case FIGHTING:
        plugin.getManagerHandler().getMatchManager()
            .removeFighter(player, practicePlayerData, false);
        break;
      case SPECTATING:
        if (plugin.getManagerHandler().getEventManager().getSpectators().containsKey(uniqueId)) {
          plugin.getManagerHandler().getEventManager().removeSpectator(player,
              plugin.getManagerHandler().getEventManager().getOngoingEvent());
        } else {
          plugin.getManagerHandler().getMatchManager().removeSpectator(player);
        }
        break;
      case QUEUE:
        if (party == null) {
          plugin.getManagerHandler().getQueueManager().removePlayerFromQueue(player);
        } else if (plugin.getManagerHandler().getPartyManager().isLeader(uniqueId)) {
          plugin.getManagerHandler().getQueueManager().removePartyFromQueue(party);
        }
        break;
      case FFA:
        plugin.getManagerHandler().getFfaManager().getByPlayer(player).removePlayer(player);
        break;
      case EVENT:
        PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
            .getEventPlaying(player);
        if (practiceEvent != null) {
          practiceEvent.leave(player);
        }
        break;
    }

    practicePlayerData.getCachedPlayer().clear();
    plugin.getManagerHandler().getTournamentManager().leaveTournament(player);
    plugin.getManagerHandler().getPartyManager().leaveParty(player);
    plugin.getManagerHandler().getMatchManager().removeMatchRequests(uniqueId);
    plugin.getManagerHandler().getPartyManager().removePartyInvites(uniqueId);
    plugin.getManagerHandler().getPlayerManager().removePlayerData(practicePlayerData);

    plugin.getManagerHandler().getNyaHologramManager().hide(player);
  }

  @EventHandler
  public void onFireExtinguish(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId())
        .isSpectating()) {
      if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
        if (player.getTargetBlock((HashSet<Byte>) null, 5).getType() == Material.FIRE) {
          event.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void onPlayerInteractSoup(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!player.isDead() && player.getItemInHand().getType() == Material.MUSHROOM_SOUP
        && player.getHealth() < 19.0) {
      final double newHealth = Math.min(player.getHealth() + 7.0, 20.0);
      player.setHealth(newHealth);
      player.getItemInHand().setType(Material.BOWL);
      player.updateInventory();
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if ((event.getAction() != Action.RIGHT_CLICK_BLOCK
        && event.getAction() != Action.RIGHT_CLICK_AIR)) {
      return;
    }
    if (player.getGameMode() == GameMode.CREATIVE) {
      if (!practicePlayerData.isSpectating()) {
        return;
      }
    }
    if (practicePlayerData.isSpectating()) {
      event.setCancelled(true);
    }
    if (event.getAction().name().endsWith("_BLOCK")) {
      if (event.getAction().name().endsWith("_BLOCK")) {
        Block block = event.getClickedBlock();
        if (blockedBlocks.contains(event.getClickedBlock().getType()) || block.getType().name()
            .contains("GATE") || block.getType().name().contains("DOOR")) {
          event.setCancelled(true);
        }
      }
    }
    if (event.getAction().name().startsWith("RIGHT_")) {
      ItemStack item = event.getPlayer().getItemInHand();
      Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
      if (player.getItemInHand() == null || !event.hasItem()) {
        return;
      }

      ItemManager.HotbarItem hotbarItem = ItemManager.HotbarItem.getItemByItem(item);
      if (item == null) {
        return;
      }

      switch (practicePlayerData.getPlayerState()) {
        case LOADING:
          player.sendMessage(ChatColor.RED + "Please wait until your player data is loaded.");
          break;
        case FIGHTING:
          Match match = plugin.getManagerHandler().getMatchManager().getMatch(practicePlayerData);
          if (item.getType() == Material.POTION && item.getDurability() == 16421) {
            if (match.getMatchState() == MatchState.STARTING) {
              event.setCancelled(true);
              player.updateInventory();
            }

            return;
          }

          if (item.isSimilar(
              plugin.getManagerHandler().getItemManager().getPlayAgainItem().getItemStack())
              && hotbarItem.getAction() == ItemManager.HotbarItem.ActionType.PLAY_AGAIN) {
            if (plugin.getManagerHandler().getMatchManager()
                .hasPlayAgainRequest(player.getUniqueId())) {
              player.performCommand("playagain");
            }
          }

          ItemStack itemStack = event.getItem();
          if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            if (itemStack.isSimilar(plugin.getManagerHandler().getItemManager().getDefaultBook())) {
              event.setCancelled(true);

              sendMessage(player, plugin.getMessagesConfig().getConfig()
                  .getString("MESSAGES.MATCH.EQUIPPED-DEFAULT"));
              practicePlayerData.setCurrentKitContents(match.getKit().getContents());
              practicePlayerData.setCurrentKitArmor(match.getKit().getArmor());
              match.getKit().applyKit(player, practicePlayerData);
            } else {
              String displayName = color(itemStack.getItemMeta().getDisplayName());
              for (ItemStack items : player.getInventory().getContents()) {
                if (items != null && !player.getInventory()
                    .contains(plugin.getManagerHandler().getItemManager().getDefaultBook())) {
                  return;
                }
              }

              Kit matchKit = match.getKit();
              for (PlayerKit kit : practicePlayerData.getKits(matchKit)) {
                if (kit != null) {
                  if (color(kit.getName()).equals(displayName)) {
                    event.setCancelled(true);

                    sendMessage(player, plugin.getMessagesConfig().getConfig()
                        .getString("MESSAGES.MATCH.EQUIPPED-EDITED")
                        .replace("<custom_kit_name>", displayName)
                        .replace("<queued_kit_name>", match.getKit().getName())
                    );

                    Color color = practicePlayerData.getTeamId() == 0 ? Color.RED : Color.BLUE;
                    int data = color == Color.RED ? 14 : 11;

                    int i = 0;
                    ItemStack[] armor = matchKit.getColoredArmor(matchKit.getArmor(), color, i);
                    ItemStack[] contents = matchKit.getColoredItems(kit.getContents(), data, i);

                    if (!matchKit.getPotionEffects().isEmpty()) {
                      for (PotionEffect potionEffect : matchKit.getPotionEffects()) {
                        player.addPotionEffect(potionEffect);
                      }
                    }

                    practicePlayerData.setCurrentKitContents(contents);
                    practicePlayerData.setCurrentKitArmor(armor);

                    matchKit.applyKit(player, practicePlayerData);
                  }
                }
              }
            }
          }

          if (item.getType() == Material.ENDER_PEARL) {
            if (match.getMatchState() == MatchState.STARTING) {
              event.setCancelled(true);
              player.sendMessage(
                  ChatColor.RED + "You can't throw enderpearls in your current state!");
              player.updateInventory();
            }
          }
          break;
        case SPAWN:
          event.setCancelled(true);
          if (hotbarItem == null) {
            return;
          }
          switch (hotbarItem.getAction()) {
            case JOIN_UNRANKED:
            case JOIN_UNRANKED_2V2:
              if (party != null && !plugin.getManagerHandler().getPartyManager()
                  .isLeader(player.getUniqueId())) {
                sendMessage(player, plugin.getMessagesConfig().getConfig()
                    .getString("ERROR-MESSAGES.PLAYER.NOT-PARTY-LEADER"));
                return;
              }
              player.openInventory(
                  plugin.getManagerHandler().getInventoryManager().getUnrankedInventory()
                      .getCurrentPage());
              break;
            case JOIN_RANKED:
              if (PlayerUtil.getPing(player) >= plugin.getSettingsConfig().getConfig()
                  .getInt("SETTINGS.MATCH.MAX-RANKED-PING")) {
                sendMessage(player, plugin.getSettingsConfig().getConfig()
                    .getString("SETTINGS.MATCH.PING-TOO-HIGH-MESSAGE"));
                return;
              }

              if (plugin.getSettingsConfig().getConfig().getInt("SETTINGS.MATCH.RANKEDS-REQUIRED")
                  >= 1) {
                if (practicePlayerData.getMatchesPlayed() >= plugin.getSettingsConfig().getConfig()
                    .getInt("SETTINGS.MATCH.RANKEDS-REQUIRED")) {
                  player.openInventory(
                      plugin.getManagerHandler().getInventoryManager().getRankedInventory()
                          .getCurrentPage());
                } else if (player.hasPermission("frost.bypass.ranked")) {
                  player.openInventory(
                      plugin.getManagerHandler().getInventoryManager().getRankedInventory()
                          .getCurrentPage());
                } else {
                  player.sendMessage(ChatColor.RED + "You need to play " + (
                      plugin.getSettingsConfig().getConfig()
                          .getInt("SETTINGS.MATCH.RANKEDS-REQUIRED")
                          - practicePlayerData.getMatchesPlayed())
                      + " unranked matches before playing ranked!");
                }
              } else {
                player.openInventory(
                    plugin.getManagerHandler().getInventoryManager().getRankedInventory()
                        .getCurrentPage());
              }
              break;
            case JOIN_PREMIUM:
              if (!plugin.getSettingsConfig().getConfig()
                  .getBoolean("SETTINGS.GENERAL.PREMIUM-ENABLED")) {
                player.sendMessage(color("&cPremium is currently disabled."));
                return;
              }

              if (party != null) {
                player.sendMessage(
                    ChatColor.RED + "You can't join this queue! Please leave your party.");
                return;
              }
              if (practicePlayerData.getPremiumMatches() <= 0) {
                sendMessage(player, plugin.getMessagesConfig().getConfig()
                    .getString("MESSAGES.PLAYER.NO-PREMIUM-MATCHES"));
                return;
              }
              player.openInventory(
                  plugin.getManagerHandler().getInventoryManager().getPremiumInventory()
                      .getCurrentPage());
              break;
            case JOIN_FFA:
              if (practicePlayerData.isInParty()) {
                sendMessage(player, plugin.getMessagesConfig().getConfig()
                    .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE"));
                return;
              }
              new FFASelectionMenu(plugin.getManagerHandler().getFfaManager()).openMenu(player);
              break;
            case PLAY_AGAIN:
              if (plugin.getManagerHandler().getMatchManager()
                  .hasPlayAgainRequest(player.getUniqueId())) {
                player.performCommand("playagain");
              }
              break;
            case CREATE_PARTY:
              plugin.getManagerHandler().getPartyManager().createParty(player);
              break;
            case EVENTS_MENU:
              if (plugin.getManagerHandler().getEventManager().getEvents().values().stream()
                  .anyMatch(e -> e.getState() != EventState.UNANNOUNCED)) {
                player.sendMessage(ChatColor.RED + "There's an event currently ongoing.");
                return;
              }

              new EventHostingMenu().openMenu(player);
              break;
            case LEADERBOARDS_MENU:
              PlayerUtil.getStyle(player, null, "ELO", this.plugin);
              break;
            case EDITOR_MENU:
              new SelectLadderKitMenu().openMenu(player);
              break;
            case SETTINGS_MENU:
              new SettingsMenu().openMenu(player);
              break;
            case PARTY_EVENTS:
              if (party != null && !plugin.getManagerHandler().getPartyManager()
                  .isLeader(player.getUniqueId())) {
                sendMessage(player, plugin.getMessagesConfig().getConfig()
                    .getString("ERROR-MESSAGES.PLAYER.NOT-PARTY-LEADER"));
                return;
              }
              player.openInventory(
                  plugin.getManagerHandler().getInventoryManager().getPartyEventInventory()
                      .getCurrentPage());
              break;
            case PARTY_INFO:
              player.performCommand("party info");
              break;
            case PARTY_LEAVE:
              plugin.getManagerHandler().getPartyManager().leaveParty(player);
              break;
            case OPEN_CURRENT_MATCHES:
              new OngoingMatchesMenu().openMenu(player);
              break;
            case OTHER_PARTIES:
              if (party != null && !plugin.getManagerHandler().getPartyManager()
                  .isLeader(player.getUniqueId())) {
                sendMessage(player, plugin.getMessagesConfig().getConfig()
                    .getString("ERROR-MESSAGES.PLAYER.NOT-PARTY-LEADER"));
                return;
              }
              player.openInventory(
                  plugin.getManagerHandler().getInventoryManager().getPartyInventory()
                      .getCurrentPage());
              break;
            case LEAVE_TOURNAMENT:
              plugin.getManagerHandler().getTournamentManager().leaveTournament(player);
              break;
            case QUEUES_MENU:
              new QueuesMenu().openMenu(player);
              break;
            case EXECUTABLE_COMMAND:
              player.performCommand(hotbarItem.getCommand());
              break;
            default:
              throw new IllegalStateException("Unexpected value: " + hotbarItem.getAction());
          }
          break;
        case QUEUE:
          if (hotbarItem.getAction() == ItemManager.HotbarItem.ActionType.LEAVE_QUEUE) {
            if (party == null) {
              plugin.getManagerHandler().getQueueManager().removePlayerFromQueue(player);
            } else {
              plugin.getManagerHandler().getQueueManager().removePartyFromQueue(party);
            }
          }
          break;
        case EVENT:
          if (hotbarItem == null) {
            return;
          }

          PracticeEvent<?> practiceEvent = plugin.getManagerHandler().getEventManager()
              .getEventPlaying(player);
          if (hotbarItem.getAction() == ItemManager.HotbarItem.ActionType.LEAVE_EVENT) {
            if (practiceEvent != null) {
              practiceEvent.leave(player);
            }
          } else if (hotbarItem.getAction() == ItemManager.HotbarItem.ActionType.HIDE_PLAYERS) {
            if (practiceEvent instanceof ParkourEvent) {
              ((ParkourEvent) practiceEvent).toggleVisibility(player);
            } else if (practiceEvent instanceof ThimbleEvent) {
              ((ThimbleEvent) practiceEvent).toggleVisibility(player);
            } else if (practiceEvent instanceof DropperEvent) {
              ((DropperEvent) practiceEvent).toggleVisibility(player);
            }
          }
          break;
        case SPECTATING:
          switch (hotbarItem.getAction()) {
            case LEAVE_SPECTATOR:
              if (plugin.getManagerHandler().getEventManager().isSpectating(player)) {
                plugin.getManagerHandler().getEventManager().removeSpectator(player,
                    plugin.getManagerHandler().getEventManager().getOngoingEvent());
              } else if (party == null) {
                if (practicePlayerData.isFollowing()
                    || practicePlayerData.getFollowingId() != null) {
                  practicePlayerData.setFollowing(false);
                  practicePlayerData.setFollowingId(null);
                }
                plugin.getManagerHandler().getMatchManager().removeSpectator(player);
                if (!practicePlayerData.getCachedPlayer().isEmpty()) {
                  practicePlayerData.getCachedPlayer().clear();
                }
              } else {
                plugin.getManagerHandler().getPartyManager().leaveParty(player);
              }
              break;
            case PLAYERS_MENU:
              if (plugin.getManagerHandler().getEventManager().getSpectators()
                  .containsKey(player.getUniqueId())) {
                new PlayersMenu(
                    plugin.getManagerHandler().getEventManager().getOngoingEvent()).openMenu(
                    player);
              } else {
                new PlayersMenu(plugin.getManagerHandler().getMatchManager()
                    .getSpectatingMatch(player.getUniqueId())).openMenu(player);
              }
              break;
            case PLAY_AGAIN:
              if (plugin.getManagerHandler().getMatchManager()
                  .hasPlayAgainRequest(player.getUniqueId())) {
                player.performCommand("playagain");
              }
              break;
          }
          break;
        case EDITING:
          if (event.getClickedBlock() == null) {
            return;
          }
          break;
      }
    }
  }

  @EventHandler
  public void onItemDamage(PlayerItemDamageEvent event) {
    Player player = event.getPlayer();
    if (player.getLastDamageCause() != null
        && player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
      if (((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof FishHook) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    Item itemDrop = event.getItemDrop();
    Material drop = itemDrop.getItemStack().getType();

    boolean noDrop = drop.name().contains("_SWORD") || drop.name().contains("_AXE") || drop.name()
        .contains("_SPADE") || drop.name().contains("_PICKAXE") || drop == Material.BOW
        || drop == Material.ENCHANTED_BOOK || drop == Material.MUSHROOM_SOUP;
    switch (practicePlayerData.getPlayerState()) {
      case FFA:
        if (noDrop) {
          event.setCancelled(true);
        } else {
          itemDrop.remove();
        }
        break;
      case FIGHTING:
        Match match = plugin.getManagerHandler().getMatchManager().getMatch(practicePlayerData);
        if (noDrop) {
          event.setCancelled(true);
        } else if (drop == Material.GLASS_BOTTLE || drop == Material.BOWL) {
          itemDrop.remove();
        } else {
          plugin.getManagerHandler().getMatchManager().addDroppedItem(match, itemDrop);
        }
        break;
      case EVENT:
        if (drop == Material.GLASS_BOTTLE || drop == Material.BOWL) {
          itemDrop.remove();
        } else {
          event.setCancelled(true);
        }
        break;
      default:
        event.setCancelled(true);
        break;
    }
  }

  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    Item item = event.getItem();
    switch (practicePlayerData.getPlayerState()) {
      case FIGHTING:
        Match match = plugin.getManagerHandler().getMatchManager().getMatch(practicePlayerData);
        if (match.getEntitiesToRemove().contains(item)) {
          match.removeEntityToRemove(item);
        } else {
          event.setCancelled(true);
        }
        break;
      case FFA:
        event.setCancelled(false);
        break;
      default:
        event.setCancelled(true);
        break;
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
    Player player = event.getPlayer();
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    String chatMessage = event.getMessage();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (party != null) {
      if (chatMessage.startsWith("!") || chatMessage.startsWith("@")) {
        event.setCancelled(true);
        String message = ChatColor.AQUA.toString() + ChatColor.BOLD + "[Party] " + player.getName()
            + ChatColor.RESET + ": " + ChatColor.GRAY + chatMessage.replaceFirst("!", "")
            .replaceFirst("@", "");
        party.broadcast(message);
      }
    }

    if (practicePlayerData.isRenaming()) {
      event.setCancelled(true);
      if (event.getMessage().length() > 16) {
        player.sendMessage(ChatColor.RED + "A kit name cannot be more than 16 characters long.");
        return;
      }

      practicePlayerData.getSelectedKit().setName(event.getMessage());
      practicePlayerData.setActive(false);
      practicePlayerData.setRename(false);
      player.sendMessage(
          color("&aYour kit name has been changed to &e" + event.getMessage() + "&a."));
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() == PlayerState.EVENT) {
      PracticeEvent<?> currentEvent = plugin.getManagerHandler().getEventManager()
          .getEventPlaying(player);
      if (currentEvent != null) {
        if (currentEvent instanceof OITCEvent) {
          event.setRespawnLocation(player.getLocation());
          currentEvent.onDeath().accept(player);
        }
      }
    }
  }

  @EventHandler
  private void onHungerDamage(EntityDamageEvent event) {
    if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    event.setDeathMessage(null);

    Player player = event.getEntity().getPlayer();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    switch (practicePlayerData.getPlayerState()) {
      case FIGHTING: {
        Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
        if (!match.getKit().isBridges() && !match.getKit().isBedWars()) {
          plugin.getManagerHandler().getMatchManager()
              .removeFighter(player, practicePlayerData, true);
        } else {
          plugin.getServer().getScheduler()
              .runTaskLater(plugin, () -> player.spigot().respawn(), 1L);
          plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            MatchTeam playerTeam = match.getTeams().get(practicePlayerData.getTeamId());
            player.resetMaxHealth();
            player.setHealth(player.getMaxHealth());
            match.getKit().applyKit(player, practicePlayerData);
            player.teleport(
                playerTeam.getTeamID() == 1 ? match.getStandaloneArena().getA().toBukkitLocation()
                    : match.getStandaloneArena().getB().toBukkitLocation());
            player.updateInventory();
          }, 2L);
        }
        break;
      }
      case EVENT: {
        PracticeEvent<?> currentEvent = plugin.getManagerHandler().getEventManager()
            .getEventPlaying(player);
        if (currentEvent != null) {
          if (currentEvent instanceof OITCEvent) {
            OITCEvent oitcEvent = (OITCEvent) currentEvent;
            OITCPlayer oitcKiller = oitcEvent.getPlayer(player.getKiller());
            OITCPlayer oitcPlayer = oitcEvent.getPlayer(player);
            oitcPlayer.setLastKiller(oitcKiller);
            plugin.getServer().getScheduler()
                .runTaskLater(plugin, () -> player.spigot().respawn(), 1L);
            break;
          }

          currentEvent.onDeath().accept(player);
        }
        break;
      }
    }

    event.setDroppedExp(0);
    event.getDrops().clear();
  }

  @EventHandler
  public void onFoodLevelChange(FoodLevelChangeEvent event) {
    Player player = (Player) event.getEntity();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (practicePlayerData.getPlayerState() == PlayerState.FIGHTING) {
      Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
      if (match.getKit().isNoHunger()
          || plugin.getManagerHandler().getEventManager().getEventPlaying(player) != null) {
        event.setCancelled(true);
      }
    } else {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onProjectileLaunch(ProjectileLaunchEvent event) {
    if (event.getEntity().getShooter() instanceof Player) {
      Player shooter = (Player) event.getEntity().getShooter();
      PracticePlayerData shooterData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(shooter.getUniqueId());
      if (shooterData.getPlayerState() == PlayerState.FIGHTING) {
        Match match = plugin.getManagerHandler().getMatchManager().getMatch(shooter.getUniqueId());
        match.addEntityToRemove(event.getEntity());
      }
    }
  }

  @EventHandler
  public void onProjectileHit(ProjectileHitEvent event) {
    if (event.getEntity().getShooter() instanceof Player) {
      Player shooter = (Player) event.getEntity().getShooter();
      PracticePlayerData shooterData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(shooter.getUniqueId());
      if (shooterData != null) {
        if (shooterData.getPlayerState() == PlayerState.FIGHTING) {
          Match match = plugin.getManagerHandler().getMatchManager()
              .getMatch(shooter.getUniqueId());
          match.removeEntityToRemove(event.getEntity());
          if (event.getEntityType() == EntityType.ARROW) {
            event.getEntity().remove();
          }

          if (match.getKit().isSpleef()) {
            Entity shooterSnowball = event.getEntity();
            Location shooterSnowballLocation = shooterSnowball.getLocation();
            BlockIterator blockIterator = new BlockIterator(shooterSnowballLocation.getWorld(),
                shooterSnowballLocation.toVector(), shooterSnowball.getVelocity().normalize(), 0,
                1);
            while (blockIterator.hasNext()) {
              Block blockHit = blockIterator.next();
              if (event.getEntity().getType().equals(EntityType.SNOWBALL)) {
                if (blockHit.getType() == Material.SNOW_BLOCK) {
                  shooterSnowball.remove();
                  blockHit.setType(Material.AIR);
                }
              }
            }
          }
        }
      }
    }
  }
}
