package dev.demeng.frost.managers;

import static dev.demeng.frost.util.CC.sendMessage;

import com.google.common.collect.Lists;
import dev.demeng.frost.Frost;
import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.game.match.MatchTeam;
import dev.demeng.frost.game.queue.QueueType;
import dev.demeng.frost.managers.leaderboard.Leaderboard;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.postmatch.InventorySnapshot;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.Clickable;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.ItemUtil;
import dev.demeng.frost.util.PlayerUtil;
import dev.demeng.frost.util.RatingUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.inventory.InventoryUI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryManager {

  private final Frost plugin = Frost.getInstance();
  private final ConfigCursor menu = new ConfigCursor(Frost.getInstance().getMenusConfig(),
      "QUEUE-INVENTORY");
  private final String MORE_PLAYERS =
      ChatColor.RED + "There must be at least 2 players in your party to do this.";

  private final int unrankedMenuSize = menu.getInt("UNRANKED-SIZE");
  private final int rankedMenuSize = menu.getInt("RANKED-SIZE");

  public final static ItemStack PLACEHOLDER_ITEM = new ItemBuilder(Material
      .valueOf(Frost.getInstance().getMenusConfig().getConfig()
          .getString("QUEUE-INVENTORY.PLACEHOLDER-ITEM-MATERIAL")))
      .durability(Frost.getInstance().getMenusConfig().getConfig()
          .getInt("QUEUE-INVENTORY.PLACEHOLDER-ITEM-DATA"))
      .name("&b")
      .hideFlags()
      .build();

  @Getter private final InventoryUI unrankedInventory = new InventoryUI(
      CC.color(menu.getString("UNRANKED.TITLE")), true, unrankedMenuSize);
  @Getter private final InventoryUI rankedInventory = new InventoryUI(
      CC.color(menu.getString("RANKED.TITLE")), true, rankedMenuSize);
  @Getter private final InventoryUI partySplitInventory = new InventoryUI(
      CC.color(menu.getString("PARTY-SPLIT-INVENTORY-TITLE")), true, unrankedMenuSize);
  @Getter private final InventoryUI partyFFAInventory = new InventoryUI(
      CC.color(menu.getString("PARTY-FFA-INVENTORY-TITLE")), true, unrankedMenuSize);
  @Getter private final InventoryUI premiumInventory = new InventoryUI(
      CC.color(menu.getString("PREMIUM-INVENTORY-TITLE")), true, 1);
  @Getter private final InventoryUI duelInventory = new InventoryUI(
      CC.color(menu.getString("DUEL-INVENTORY-TITLE")), true, unrankedMenuSize);

  @Getter private final InventoryUI partyEventInventory = new InventoryUI(
      CC.color(menu.getString("PARTY-EVENTS-INVENTORY-TITLE")), true, 1);
  @Getter private final InventoryUI partyInventory = new InventoryUI(
      CC.color(menu.getString("PARTY-OTHER-PARTIES-INVENTORY-TITLE")), true, 6);

  private final Map<UUID, InventorySnapshot> snapshots = new HashMap<>();
  private final Map<String, InventoryUI> duelMapInventories = new HashMap<>();
  private final Map<String, InventoryUI> partyFFAMapInventories = new HashMap<>();
  private final Map<String, InventoryUI> partySplitMapInventories = new HashMap<>();

  public InventoryManager() {
    this.setupInventories();
    plugin.getServer().getScheduler()
        .runTaskTimerAsynchronously(plugin, this::updateInventories, 20L, 20L);
  }

  public void reloadInventories() {
    snapshots.clear();
    duelMapInventories.clear();
    partyFFAMapInventories.clear();
    partySplitMapInventories.clear();

    setupInventories();
  }

  private void setupInventories() {
    if (menu.getBoolean("PLACEHOLDER-ITEMS-ENABLED")) {
      for (int i = 0; i < 9 * menu.getInt("UNRANKED-SIZE"); i++) {
        this.unrankedInventory.setItem(i, new InventoryUI.EmptyClickableItem(PLACEHOLDER_ITEM));
        this.duelInventory.setItem(i, new InventoryUI.EmptyClickableItem(PLACEHOLDER_ITEM));
        this.partyFFAInventory.setItem(i, new InventoryUI.EmptyClickableItem(PLACEHOLDER_ITEM));
        this.partySplitInventory.setItem(i, new InventoryUI.EmptyClickableItem(PLACEHOLDER_ITEM));
      }
      for (int i = 0; i < 9 * menu.getInt("RANKED-SIZE"); i++) {
        this.rankedInventory.setItem(i, new InventoryUI.EmptyClickableItem(PLACEHOLDER_ITEM));
      }
    }

    Collection<Kit> kits = plugin.getManagerHandler().getKitManager().getKits();
    for (Kit kit : kits) {
      if (kit.isEnabled()) {
        this.unrankedInventory.setItem(kit.getUnrankedPos(), new InventoryUI.AbstractClickableItem(
            new ItemBuilder(kit.getIcon().getType()).durability(kit.getIcon().getDurability())
                .amount(1).name(
                    CC.color(menu.getString("UNRANKED.NAME")).replace("<kit_name>", kit.getName())
                        .replace("<kit_displayname>", kit.getDisplayName())).hideFlags().build()) {
          @Override
          public void onClick(InventoryClickEvent event) {
            Player player = (Player) event.getWhoClicked();
            addToQueue(player,
                plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
                kit, plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId()),
                QueueType.UNRANKED);
          }
        });
        if (kit.isRanked()) {
          this.rankedInventory.setItem(kit.getRankedPos(), new InventoryUI.AbstractClickableItem(
              new ItemBuilder(kit.getIcon().getType()).durability(kit.getIcon().getDurability())
                  .amount(1).name(
                      CC.color(menu.getString("RANKED.NAME")).replace("<kit_name>", kit.getName())
                          .replace("<kit_displayname>", kit.getDisplayName())).hideFlags().build()) {
            @Override
            public void onClick(InventoryClickEvent event) {
              Player player = (Player) event.getWhoClicked();
              addToQueue(player,
                  plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
                  kit, plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId()),
                  QueueType.RANKED
              );
            }
          });
        }
        this.duelInventory.setItem(kit.getUnrankedPos(), new InventoryUI.AbstractClickableItem(
            ItemUtil.createNoFlagsItem(kit.getIcon().getType(),
                CC.color(menu.getString("NAME")).replace("<kit_name>", kit.getName()), 1,
                kit.getIcon().getDurability())) {
          @Override
          public void onClick(InventoryClickEvent event) {
            handleDuelClick((Player) event.getWhoClicked(), kit);
          }
        });
        this.partySplitInventory.setItem(kit.getUnrankedPos(),
            new InventoryUI.AbstractClickableItem(
                ItemUtil.createNoFlagsItem(kit.getIcon().getType(),
                    CC.color(menu.getString("NAME")).replace("<kit_name>", kit.getName()), 1,
                    kit.getIcon().getDurability())) {
              @Override
              public void onClick(InventoryClickEvent event) {
                handlePartySplitClick((Player) event.getWhoClicked(), kit);
              }
            });
        this.partyFFAInventory.setItem(kit.getUnrankedPos(), new InventoryUI.AbstractClickableItem(
            ItemUtil.createNoFlagsItem(kit.getIcon().getType(),
                CC.color(menu.getString("NAME")).replace("<kit_name>", kit.getName()), 1,
                kit.getIcon().getDurability())) {
          @Override
          public void onClick(InventoryClickEvent event) {
            handleFFAClick((Player) event.getWhoClicked(), kit);
          }
        });
      }
    }

    this.partyEventInventory.setItem(2, new InventoryUI.AbstractClickableItem(
        ItemUtil.createItem(Material.FIREWORK_CHARGE, ChatColor.AQUA + "Split Fights")) {
      @Override
      public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        player.openInventory(getPartySplitInventory().getCurrentPage());
      }
    });
    this.partyEventInventory.setItem(6, new InventoryUI.AbstractClickableItem(
        ItemUtil.createItem(Material.SLIME_BALL, ChatColor.AQUA + "Party FFA")) {
      @Override
      public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        player.openInventory(getPartyFFAInventory().getCurrentPage());
      }
    });

    for (int i = 3; i < 6; i++) {
      this.premiumInventory.setItem(i, new InventoryUI.AbstractClickableItem(
          ItemUtil.reloreItem(ItemUtil.createItem(Material.INK_SACK,
                  ChatColor.GREEN.toString() + ChatColor.BOLD + "Join Queue", 1, (short) 10),
              " ",
              CC.color("&7By clicking here you will be added"),
              CC.color("&7to the Premium NoDebuff queue."),
              "",
              CC.color("&cThis costs 1 Premium Match when a match is found!"))) {

        @Override
        public void onClick(InventoryClickEvent event) {
          Player player = (Player) event.getWhoClicked();
          ItemStack item = event.getCurrentItem();
          if (item != null) {
            plugin.getManagerHandler().getQueueManager().addPlayerToQueue(
                player,
                plugin.getManagerHandler().getPlayerManager().getPlayerData(player.getUniqueId()),
                Frost.getInstance().getSettingsConfig().getConfig()
                    .getString("SETTINGS.GENERAL.PREMIUM-QUEUE-KIT"),
                QueueType.PREMIUM
            );
          }
        }
      });
    }

    String arenaSelection = menu.getString("DUEL-ARENA-SELECTION-TITLE");
    for (Kit kit : plugin.getManagerHandler().getKitManager().getKits()) {
      InventoryUI duelInventory = new InventoryUI(CC.color(arenaSelection), true, 3);
      InventoryUI partySplitInventory = new InventoryUI(CC.color(arenaSelection), true, 3);
      InventoryUI partyFFAInventory = new InventoryUI(CC.color(arenaSelection), true, 3);

      Arena randomArena = plugin.getManagerHandler().getArenaManager().getRandomArena(kit);
      ItemStack random = ItemUtil.createItem(
          Material.valueOf(
              plugin.getMenusConfig().getConfig().getString("MAP-SELECTOR.RANDOM-ITEM")),
          CC.color(plugin.getMenusConfig().getConfig().getString("MAP-SELECTOR.RANDOM")),
          1, (short) plugin.getMenusConfig().getConfig().getInt("MAP-SELECTOR.RANDOM-DATA")
      );
      arenaSelector(kit, duelInventory, partySplitInventory, partyFFAInventory, randomArena,
          random);

      for (Arena arena : plugin.getManagerHandler().getArenaManager().getArenas().values()) {
        if (!arena.isEnabled()) {
          continue;
        }
        if (kit.getArenaWhiteList().size() > 0 && !kit.getArenaWhiteList()
            .contains(arena.getName())) {
          continue;
        }

        ItemStack select = ItemUtil.createItem(
            Material.valueOf(arena.getIcon()),
            CC.color(plugin.getMenusConfig().getConfig().getString("MAP-SELECTOR.SELECT")
                .replace("<arena>", arena.getName())),
            1, (short) arena.getIconData()
        );
        arenaSelector(kit, duelInventory, partySplitInventory, partyFFAInventory, arena, select);
      }

      this.duelMapInventories.put(kit.getName(), duelInventory);
      this.partySplitMapInventories.put(kit.getName(), partySplitInventory);
      this.partyFFAMapInventories.put(kit.getName(), partyFFAInventory);
    }
  }

  private void arenaSelector(Kit kit, InventoryUI duelInventory, InventoryUI partySplitInventory,
      InventoryUI partyFFAInventory, Arena arena, ItemStack itemStack) {
    duelInventory.addItem(new InventoryUI.AbstractClickableItem(itemStack) {
      @Override
      public void onClick(InventoryClickEvent event) {
        handleDuelMapClick((Player) event.getWhoClicked(), arena, kit);
      }
    });
    partySplitInventory.addItem(new InventoryUI.AbstractClickableItem(itemStack) {
      @Override
      public void onClick(InventoryClickEvent event) {
        handlePartySplitMapClick((Player) event.getWhoClicked(), arena, kit);
      }
    });
    partyFFAInventory.addItem(new InventoryUI.AbstractClickableItem(itemStack) {
      @Override
      public void onClick(InventoryClickEvent event) {
        handlePartyFFAMapClick((Player) event.getWhoClicked(), arena, kit);
      }
    });
  }

  private void addToQueue(Player player, PracticePlayerData practicePlayerData, Kit kit,
      Party party, QueueType queueType) {
    if (kit != null) {
      if (party == null) {
        plugin.getManagerHandler().getQueueManager()
            .addPlayerToQueue(player, practicePlayerData, kit.getName(), queueType);
      } else if (plugin.getManagerHandler().getPartyManager().isLeader(player.getUniqueId())) {
        plugin.getManagerHandler().getQueueManager().addPartyToQueue(player, party, kit.getName());
      }
    }
  }

  public void addSnapshot(InventorySnapshot snapshot) {
    this.snapshots.put(snapshot.getSnapshotId(), snapshot);
    plugin.getServer().getScheduler()
        .runTaskLater(plugin, () -> removeSnapshot(snapshot.getSnapshotId()), 20L * 30L);
  }

  public void removeSnapshot(UUID snapshotId) {
    InventorySnapshot snapshot = this.snapshots.get(snapshotId);
    if (snapshot != null) {
      this.snapshots.remove(snapshotId);
    }
  }

  public InventorySnapshot getSnapshot(UUID snapshotId) {
    return this.snapshots.get(snapshotId);
  }

  public void addParty(Player player) {
    ItemStack playerHead = ItemUtil.createPlayerHead(Material.SKULL_ITEM,
        CC.color("&7" + player.getName() + "'s Party"), player.getName(), 1, (short) 3);
    this.partyInventory.addItem(new InventoryUI.AbstractClickableItem(playerHead) {
      @Override
      public void onClick(InventoryClickEvent inventoryClickEvent) {
        player.closeInventory();
        if (inventoryClickEvent.getWhoClicked() instanceof Player) {
          Player sender = (Player) inventoryClickEvent.getWhoClicked();
          sender.performCommand("duel " + player.getName());
        }
      }
    });
  }

  public void updateParty(Party party) {
    Player player = plugin.getServer().getPlayer(party.getLeader());
    for (int i = 0; i < this.partyInventory.getSize(); i++) {
      InventoryUI.ClickableItem item = this.partyInventory.getItem(i);
      if (item != null) {
        ItemStack stack = item.getItemStack();
        if (stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName()
            .contains(player.getName())) {
          List<String> strings = new ArrayList<>();

          strings.add(CC.MENU_BAR);
          strings.add(ChatColor.GRAY + "Members: ");
          party.members().forEach(
              member -> strings.add(ChatColor.GRAY + " - " + ChatColor.AQUA + member.getName()));
          strings.add(ChatColor.WHITE.toString());
          strings.add(CC.color("&fClick here to duel " + player.getName() + "'s party."));
          strings.add(CC.MENU_BAR);

          ItemUtil.reloreItem(stack, strings.toArray(new String[0]));
          ItemUtil.renameItem(stack,
              ChatColor.AQUA + player.getName() + "'s Party " + ChatColor.GRAY + "("
                  + ChatColor.WHITE + party.getMembers().size() + ChatColor.GRAY + ")");

          item.setItemStack(stack);
          break;
        }
      }
    }
  }

  public void removeParty(Party party) {
    Player player = plugin.getServer().getPlayer(party.getLeader());
    for (int i = 0; i < this.partyInventory.getSize(); i++) {
      InventoryUI.ClickableItem item = this.partyInventory.getItem(i);
      if (item != null) {
        ItemStack stack = item.getItemStack();
        if (stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName()
            .contains(player.getName())) {
          this.partyInventory.removeItem(i);
          break;
        }
      }
    }
  }

  private void updateInventories() {
    for (int i = 0; i < 9 * unrankedMenuSize; i++) {
      InventoryUI.ClickableItem unrankedItem = this.unrankedInventory.getItem(i);
      if (unrankedItem != null) {
        if (unrankedItem.getItemStack() != PLACEHOLDER_ITEM) {
          unrankedItem.setItemStack(
              this.updateQueueLore(unrankedItem.getItemStack(), QueueType.UNRANKED));
        } else {
          unrankedItem.setItemStack(PLACEHOLDER_ITEM);
        }
        this.unrankedInventory.setItem(i, unrankedItem);
      }
    }
    for (int i = 0; i < 9 * rankedMenuSize; i++) {
      InventoryUI.ClickableItem rankedItem = this.rankedInventory.getItem(i);
      if (rankedItem != null) {
        if (rankedItem.getItemStack() != PLACEHOLDER_ITEM) {
          rankedItem.setItemStack(
              this.updateQueueLore(rankedItem.getItemStack(), QueueType.RANKED));
        } else {
          rankedItem.setItemStack(PLACEHOLDER_ITEM);
        }
        this.rankedInventory.setItem(i, rankedItem);
      }
    }
  }

  private ItemStack updateQueueLore(ItemStack itemStack, QueueType type) {
    if (itemStack == null) {
      return null;
    }

    String ladder;
    if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
      ladder = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
      if (ladder.contains(" ")) {
        ladder = ladder.split(" ")[1];
      }
      ladder = ladder.replaceAll("[^a-zA-Z]+", "");
    } else {
      return null;
    }

    int unrankedQueue = plugin.getManagerHandler().getQueueManager()
        .getQueueSize(ladder, QueueType.UNRANKED);
    int unrankedGame = plugin.getManagerHandler().getMatchManager()
        .getFighters(ladder, QueueType.UNRANKED);
    int rankedQueue = plugin.getManagerHandler().getQueueManager()
        .getQueueSize(ladder, QueueType.RANKED);
    int rankedGame = plugin.getManagerHandler().getMatchManager()
        .getFighters(ladder, QueueType.RANKED);

    ArrayList<String> lore = new ArrayList<>();
    if (!type.isRanked()) {
      for (String string : menu.getStringList("UNRANKED.LORE")) {
        if (!string.equalsIgnoreCase("<top-3>")) {
          lore.add(CC.color(string)
              .replace("<queueing_unranked>", String.valueOf(unrankedQueue))
              .replace("<fighting_unranked>", String.valueOf(unrankedGame))
              .replace("<queueing_ranked>", String.valueOf(rankedQueue))
              .replace("<fighting_ranked>", String.valueOf(rankedGame))
              .replace("<kit_name>", ladder)
          );
        } else {
          int i = 0;
          for (Leaderboard winStreak : plugin.getManagerHandler().getLeaderboardManager()
              .getKitLeaderboards(plugin.getManagerHandler().getKitManager().getKit(ladder))
              .stream().sorted(Comparator.comparingInt(Leaderboard::getPlayerWinStreak).reversed())
              .limit(3).collect(Collectors.toList())) {
            if (winStreak != null) {
              if (winStreak.getPlayerUuid() != null) {
                lore.add(CC.color(menu.getString("UNRANKED.TOP-3-FORMAT")
                    .replace("<number>", String.valueOf(i + 1))
                    .replace("<name>", winStreak.getPlayerName())
                    .replace("<value>", String.valueOf(winStreak.getPlayerWinStreak()))
                ));
                i++;
              }
            }
          }
        }
      }
    } else {
      for (String string : menu.getStringList("RANKED.LORE")) {
        if (!string.equalsIgnoreCase("<top-3>")) {
          lore.add(CC.color(string)
              .replace("<queueing_unranked>", String.valueOf(unrankedQueue))
              .replace("<fighting_unranked>", String.valueOf(unrankedGame))
              .replace("<queueing_ranked>", String.valueOf(rankedQueue))
              .replace("<fighting_ranked>", String.valueOf(rankedGame))
              .replace("<kit_name>", ladder)
          );
        } else {
          int i = 0;
          for (Leaderboard leaderboard : plugin.getManagerHandler().getLeaderboardManager()
              .getSortedKitLeaderboards(plugin.getManagerHandler().getKitManager().getKit(ladder),
                  "elo").stream().limit(3).collect(Collectors.toList())) {
            if (leaderboard != null) {
              if (leaderboard.getPlayerUuid() != null) {
                lore.add(CC.color(menu.getString("RANKED.TOP-3-FORMAT")
                    .replace("<number>", String.valueOf(i + 1))
                    .replace("<name>", leaderboard.getPlayerName())
                    .replace("<value>", String.valueOf(leaderboard.getPlayerElo()))
                    .replace("<elo_rating>",
                        RatingUtil.getRankByElo(leaderboard.getPlayerElo()).getName())
                ));
                i++;
              }
            }
          }
        }
      }
    }

    switch (type) {
      case UNRANKED:
        return ItemUtil.updateLoreAndAmount(itemStack, unrankedGame, lore.toArray(new String[0]));
      case RANKED:
        return ItemUtil.updateLoreAndAmount(itemStack, rankedGame, lore.toArray(new String[0]));
    }

    return itemStack;
  }

  private void handleDuelClick(Player player, Kit kit) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Player selected = plugin.getServer().getPlayer(practicePlayerData.getDuelSelecting());
    if (selected == null) {
      player.sendMessage(
          String.format(PlayerUtil.PLAYER_NOT_FOUND, practicePlayerData.getDuelSelecting()));
      return;
    }

    PracticePlayerData targetData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(selected.getUniqueId());
    if (targetData.getPlayerState() != PlayerState.SPAWN) {
      player.sendMessage(ChatColor.RED + "That player is currently busy.");
      return;
    }

    Party targetParty = plugin.getManagerHandler().getPartyManager()
        .getParty(selected.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());

    boolean partyDuel = party != null;
    if (partyDuel) {
      if (targetParty == null) {
        player.sendMessage(ChatColor.RED + "That player is not in a party.");
        return;
      }
    }

    if (player.hasPermission("frost.user.map_selector")) {
      player.closeInventory();
      player.openInventory(this.duelMapInventories.get(kit.getName()).getCurrentPage());
      return;
    }

    if (plugin.getManagerHandler().getMatchManager()
        .getMatchRequest(player.getUniqueId(), selected.getUniqueId()) != null) {
      player.sendMessage(
          ChatColor.RED + "You have already sent a duel request to this player, please wait.");
      return;
    }

    Arena arena = plugin.getManagerHandler().getArenaManager().getRandomArena(kit);
    if (arena == null) {
      player.sendMessage(ChatColor.RED + "There are no arenas available at this moment.");
      plugin.getManagerHandler().getQueueManager().removePlayerFromQueue(player);
      return;
    }

    this.sendDuel(player, selected, kit, partyDuel, party, targetParty, arena);
  }

  private void handlePartySplitClick(Player player, Kit kit) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null || kit == null || !plugin.getManagerHandler().getPartyManager()
        .isLeader(player.getUniqueId())) {
      return;
    }
    player.closeInventory();
    if (party.getMembers().size() < 2) {
      player.sendMessage(MORE_PLAYERS);
    } else {
      if (kit.isStickFight()) {
        player.sendMessage(CC.color("&cThis kit cannot be played on Party Split!"));
        return;
      }

      if (player.hasPermission("frost.user.map_selector")) {
        player.closeInventory();
        player.openInventory(this.partySplitMapInventories.get(kit.getName()).getCurrentPage());
        return;
      }

      Arena arena = plugin.getManagerHandler().getArenaManager().getRandomArena(kit);
      if (arena == null) {
        player.sendMessage(ChatColor.RED + "There are no arenas available at this moment.");
        plugin.getManagerHandler().getQueueManager().removePartyFromQueue(party);
        return;
      }

      this.createPartySplitMatch(party, arena, kit);
    }
  }

  private void handleFFAClick(Player player, Kit kit) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null || kit == null || !plugin.getManagerHandler().getPartyManager()
        .isLeader(player.getUniqueId())) {
      return;
    }
    if (party.getMembers().size() < 2) {
      player.sendMessage(MORE_PLAYERS);
    } else {
      if (kit.isBridges() || kit.isBedWars() || kit.isBoxing() || kit.isBattleRush()
          || kit.isStickFight() || kit.isMlgRush()) {
        player.sendMessage(CC.color("&cThis kit cannot be played on Party FFA!"));
        return;
      }

      if (player.hasPermission("frost.user.map_selector")) {
        player.closeInventory();
        player.openInventory(this.partyFFAMapInventories.get(kit.getName()).getCurrentPage());
        return;
      }

      Arena arena = plugin.getManagerHandler().getArenaManager().getRandomArena(kit);
      if (arena == null) {
        player.sendMessage(ChatColor.RED + "There are no arenas available at this moment.");
        plugin.getManagerHandler().getQueueManager().removePartyFromQueue(party);
        return;
      }

      this.createFFAMatch(party, arena, kit);
    }
  }

  private void handleDuelMapClick(Player player, Arena arena, Kit kit) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Player selected = plugin.getServer().getPlayer(practicePlayerData.getDuelSelecting());
    if (selected == null) {
      player.sendMessage(
          String.format(PlayerUtil.PLAYER_NOT_FOUND, practicePlayerData.getDuelSelecting()));
      return;
    }

    PracticePlayerData targetData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(selected.getUniqueId());
    if (targetData.getPlayerState() != PlayerState.SPAWN) {
      player.sendMessage(ChatColor.RED + "That player is currently busy.");
      return;
    }

    Party targetParty = plugin.getManagerHandler().getPartyManager()
        .getParty(selected.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    boolean partyDuel = party != null;
    if (partyDuel && targetParty == null) {
      player.sendMessage(ChatColor.RED + "That player is not in a party.");
      return;
    }
    if (plugin.getManagerHandler().getMatchManager()
        .getMatchRequest(player.getUniqueId(), selected.getUniqueId()) != null) {
      player.sendMessage(
          ChatColor.RED + "You have already sent a duel request to this player, please wait.");
      return;
    }

    this.sendDuel(player, selected, kit, partyDuel, party, targetParty, arena);
  }

  private void handlePartyFFAMapClick(Player player, Arena arena, Kit kit) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null || !plugin.getManagerHandler().getPartyManager()
        .isLeader(player.getUniqueId())) {
      return;
    }
    player.closeInventory();
    if (party.getMembers().size() < 2) {
      player.sendMessage(MORE_PLAYERS);
    } else {
      this.createFFAMatch(party, arena, kit);
    }
  }

  private void handlePartySplitMapClick(Player player, Arena arena, Kit kit) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null || !plugin.getManagerHandler().getPartyManager()
        .isLeader(player.getUniqueId())) {
      return;
    }

    player.closeInventory();
    if (party.getMembers().size() < 2) {
      player.sendMessage(MORE_PLAYERS);
    } else {
      this.createPartySplitMatch(party, arena, kit);
    }
  }

  private void sendDuel(Player player, Player opponent, Kit kit, boolean partyDuel, Party party,
      Party targetParty, Arena arena) {
    player.closeInventory();

    plugin.getManagerHandler().getMatchManager()
        .createMatchRequest(player, opponent, arena, kit.getName(), partyDuel);

    if (partyDuel) {
      for (String info : plugin.getMessagesConfig().getConfig()
          .getStringList("MESSAGES.DUEL.PARTY-SENT")) {
        partyDuelMessage(kit.getName(), arena, party, targetParty, info);
      }

      for (String info : plugin.getMessagesConfig().getConfig()
          .getStringList("MESSAGES.DUEL.PARTY-RECEIVED")) {
        Clickable requestMessage = new Clickable(CC.color(info)
            .replace("<kit_name>", kit.getName())
            .replace("<arena_name>", arena.getName())
            .replace("<party_leader>", Bukkit.getPlayer(party.getLeader()).getName())
            .replace("<party_members>", String.valueOf(party.getMembers().size()))
            ,
            CC.color(plugin.getMessagesConfig().getConfig()
                .getString("MESSAGES.DUEL.CLICKABLE-MESSAGE")),
            "/accept " + player.getName() + " " + kit.getName()
        );
        requestMessage.sendToPlayer(Bukkit.getPlayer(targetParty.getLeader()));
      }
    } else {
      for (String info : plugin.getMessagesConfig().getConfig()
          .getStringList("MESSAGES.DUEL.SENT")) {
        duelMessage(kit.getName(), arena, player, opponent, info);
      }

      for (String info : plugin.getMessagesConfig().getConfig()
          .getStringList("MESSAGES.DUEL.RECEIVED")) {
        Clickable requestMessage = new Clickable(CC.color(info)
            .replace("<kit_name>", kit.getName())
            .replace("<arena_name>", arena.getName())
            .replace("<opponent>", player.getName())
            .replace("<opponent_ping>", String.valueOf(PlayerUtil.getPing(player)))
            ,
            CC.color(plugin.getMessagesConfig().getConfig()
                .getString("MESSAGES.DUEL.CLICKABLE-MESSAGE")),
            "/accept " + player.getName() + " " + kit.getName()
        );
        requestMessage.sendToPlayer(opponent);
      }
    }
  }

  private void duelMessage(String kitName, Arena arena, Player receiver, Player opponent,
      String info) {
    sendMessage(receiver, info
        .replace("<kit_name>", kitName)
        .replace("<arena_name>", arena.getName())
        .replace("<opponent>", opponent.getName())
        .replace("<opponent_ping>", String.valueOf(PlayerUtil.getPing(opponent)))
    );
  }

  private void partyDuelMessage(String kitName, Arena arena, Party receiverParty,
      Party opponentParty, String info) {
    receiverParty.broadcast(info
        .replace("<kit_name>", kitName)
        .replace("<arena_name>", arena.getName())
        .replace("<party_leader>", Bukkit.getPlayer(opponentParty.getLeader()).getName())
        .replace("<party_members>", String.valueOf(opponentParty.getMembers().size()))
    );
  }

  private void createPartySplitMatch(Party party, Arena arena, Kit kit) {
    MatchTeam[] teams = party.split();
    Match match = new Match(arena, kit, QueueType.UNRANKED, teams);

    for (String info : plugin.getMessagesConfig().getConfig()
        .getStringList("MESSAGES.MATCH.PARTY-SPLIT-STARTING")) {
      match.broadcast(info
          .replace("<kit_name>", kit.getName())
          .replace("<arena_name>", arena.getName())
          .replace("<party_members>", String.valueOf(party.getMembers().size()))
      );
    }

    plugin.getManagerHandler().getMatchManager().createMatch(match);
  }

  private void createFFAMatch(Party party, Arena arena, Kit kit) {
    MatchTeam team = new MatchTeam(party.getLeader(), Lists.newArrayList(party.getMembers()), 0);
    Match match = new Match(arena, kit, QueueType.UNRANKED, team);

    for (String info : plugin.getMessagesConfig().getConfig()
        .getStringList("MESSAGES.MATCH.PARTY-FFA-STARTING")) {
      match.broadcast(info
          .replace("<kit_name>", kit.getName())
          .replace("<arena_name>", arena.getName())
          .replace("<party_members>", String.valueOf(party.getMembers().size()))
      );
    }

    plugin.getManagerHandler().getMatchManager().createMatch(match);
  }
}
