package dev.demeng.frost.providers.tab;

import com.google.common.collect.Lists;
import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.tablist.adapter.TabAdapter;
import dev.demeng.frost.tablist.entry.TabEntry;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.config.FileConfig;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TablistProvider implements TabAdapter {

  private final Frost plugin = Frost.getInstance();
  private final FileConfig tabListConfig = plugin.getTablistConfig();
  private final ConfigCursor tab = new ConfigCursor(Frost.getInstance().getTablistConfig(),
      "TABLIST");

  @Override
  public String getHeader(Player player) {
    return CC.parse(player, tab.getString("HEADER"));
  }

  @Override
  public String getFooter(Player player) {
    return CC.parse(player, tab.getString("FOOTER"));
  }

  @Override
  public List<TabEntry> getLines(Player player) {
    List<TabEntry> lines = Lists.newArrayList();
    TablistModeler tablistModeler = new TablistModeler();
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());

    if (plugin.getSettingsConfig().getConfig()
        .getBoolean("SETTINGS.OVERRIDE-PLAYER.VANILLA-TABLIST")
        || practicePlayerData.getPlayerSettings().isVanillaTab()) {
      int column = 0;
      int row = 0;
      for (Player online : Bukkit.getOnlinePlayers()) {
        try {
          lines.add(new TabEntry(column, row,
              Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null ?
                  PlaceholderAPI.setPlaceholders(online,
                      this.tabListConfig.getConfig().getString("TABLIST.VANILLA-PLAYER-COLOR"))
                      + online.getName() : this.tabListConfig.getConfig()
                  .getString("TABLIST.VANILLA-PLAYER-COLOR")).setPing(
              ((CraftPlayer) online).getHandle().ping));
          if (column++ < 3) {
            continue;
          }
          column = 0;

          if (row++ < 19) {
            continue;
          }
          row = 0;
        } catch (Exception ignored) {
          break;
        }
      }
    } else {
      if (practicePlayerData.getPlayerState() == PlayerState.FIGHTING) {
        Match match = plugin.getManagerHandler().getMatchManager().getMatch(player.getUniqueId());
        if (!match.isParty() && !match.isPartyMatch() && !match.isFFA()) {
          this.tabListConfig.getConfig().getConfigurationSection("MATCH.NORMAL.").getKeys(false)
              .forEach(tabColumn -> {
                int column = tablistModeler.getColumn(tabColumn);
                this.tabListConfig.getConfig().getConfigurationSection("MATCH.NORMAL." + tabColumn)
                    .getKeys(false).forEach(tabRow -> {
                      int row = Integer.parseInt(tabRow) - 1;
                      String text = this.tabListConfig.getConfig()
                          .getString("MATCH.NORMAL." + tabColumn + "." + tabRow);
                      lines.add(new TabEntry(column, row,
                          tablistModeler.matchTranslate(text, practicePlayerData, match)).setPing(-1));
                    });
              });
        } else if (match.isPartyMatch() && !match.isFFA() && !practicePlayerData.isInTournament()) {
          this.tabListConfig.getConfig().getConfigurationSection("MATCH.PARTY-SPLIT.")
              .getKeys(false).forEach(tabColumn -> {
                int column = tablistModeler.getColumn(tabColumn);
                this.tabListConfig.getConfig().getConfigurationSection("MATCH.PARTY-SPLIT." + tabColumn)
                    .getKeys(false).forEach(tabRow -> {
                      int row = Integer.parseInt(tabRow) - 1;
                      String text = this.tabListConfig.getConfig()
                          .getString("MATCH.PARTY-SPLIT." + tabColumn + "." + tabRow);
                      lines.add(new TabEntry(column, row,
                          tablistModeler.partyTranslate(text, practicePlayerData, match, true)).setPing(
                          -1));
                    });
              });
        } else if (match.isFFA()) {
          this.tabListConfig.getConfig().getConfigurationSection("MATCH.PARTY-FFA.").getKeys(false)
              .forEach(tabColumn -> {
                int column = tablistModeler.getColumn(tabColumn);
                this.tabListConfig.getConfig()
                    .getConfigurationSection("MATCH.PARTY-FFA." + tabColumn).getKeys(false)
                    .forEach(tabRow -> {
                      int row = Integer.parseInt(tabRow) - 1;
                      String text = this.tabListConfig.getConfig()
                          .getString("MATCH.PARTY-FFA." + tabColumn + "." + tabRow);
                      lines.add(new TabEntry(column, row,
                          tablistModeler.partyTranslate(text, practicePlayerData, match,
                              false)).setPing(-1));
                    });
              });
        }
      } else if (practicePlayerData.getPlayerState() == PlayerState.SPECTATING) {
        this.tabListConfig.getConfig().getConfigurationSection("MATCH.SPECTATING.").getKeys(false)
            .forEach(tabColumn -> {
              int column = tablistModeler.getColumn(tabColumn);
              this.tabListConfig.getConfig()
                  .getConfigurationSection("MATCH.SPECTATING." + tabColumn).getKeys(false)
                  .forEach(tabRow -> {
                    int row = Integer.parseInt(tabRow) - 1;
                    String text = this.tabListConfig.getConfig()
                        .getString("MATCH.SPECTATING." + tabColumn + "." + tabRow);
                    lines.add(new TabEntry(column, row,
                        tablistModeler.spawnTranslate(text, practicePlayerData)).setPing(-1));
                  });
            });
      } else {
        if (practicePlayerData.isInParty()) {
          this.tabListConfig.getConfig().getConfigurationSection("LOBBY.IN-PARTY").getKeys(false)
              .forEach(tabColumn -> {
                int column = tablistModeler.getColumn(tabColumn);
                this.tabListConfig.getConfig()
                    .getConfigurationSection("LOBBY.IN-PARTY." + tabColumn).getKeys(false)
                    .forEach(tabRow -> {
                      int row = Integer.parseInt(tabRow) - 1;
                      String text = this.tabListConfig.getConfig()
                          .getString("LOBBY.IN-PARTY." + tabColumn + "." + tabRow);
                      lines.add(new TabEntry(column, row,
                          tablistModeler.spawnTranslate(text, practicePlayerData)).setPing(-1));
                    });
              });
        } else {
          this.tabListConfig.getConfig().getConfigurationSection("LOBBY.NORMAL").getKeys(false)
              .forEach(tabColumn -> {
                int column = tablistModeler.getColumn(tabColumn);
                this.tabListConfig.getConfig().getConfigurationSection("LOBBY.NORMAL." + tabColumn)
                    .getKeys(false).forEach(tabRow -> {
                      int row = Integer.parseInt(tabRow) - 1;
                      String text = this.tabListConfig.getConfig()
                          .getString("LOBBY.NORMAL." + tabColumn + "." + tabRow);
                      lines.add(new TabEntry(column, row,
                          tablistModeler.spawnTranslate(text, practicePlayerData)).setPing(-1));
                    });
              });
        }
      }
    }

    return lines;
  }
}
