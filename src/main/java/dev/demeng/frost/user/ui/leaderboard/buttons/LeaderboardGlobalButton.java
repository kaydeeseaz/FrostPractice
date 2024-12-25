package dev.demeng.frost.user.ui.leaderboard.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.RatingUtil;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.pluginbase.mongo.lib.bson.Document;
import dev.demeng.pluginbase.mongo.lib.driver.client.MongoCursor;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LeaderboardGlobalButton extends Button {

  private final Material material;
  private final String document;
  private final String type;

  public LeaderboardGlobalButton(Material material, String document, String type) {
    this.material = material;
    this.document = document;
    this.type = type;
  }

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();
    AtomicInteger lineNum = new AtomicInteger();
    String name = plugin.getMenusConfig().getConfig()
        .getString("LEADERBOARDS-INVENTORY." + type + "-ELO.TITLE").replace("<premium_kit>",
            plugin.getSettingsConfig().getConfig().getString("SETTINGS.GENERAL.PREMIUM-QUEUE-KIT"));

    lore.add(CC.TOP_SPLITTER);

    try (MongoCursor<Document> iterator = plugin.getManagerHandler().getPlayerManager()
        .getPlayersSortedByDocumentElo(this.document)) {
      while (iterator.hasNext()) {
        lineNum.getAndIncrement();
        try {
          Document document = iterator.next();
          if (document.getString("uuid") == null) {
            continue;
          }

          UUID uuid = UUID.fromString(document.getString("uuid"));
          Document stat = (Document) document.get("global");
          int statElo = stat.getInteger(this.document);

          lore.add(plugin.getMenusConfig().getConfig()
              .getString("LEADERBOARDS-INVENTORY." + type + "-ELO.LINE")
              .replace("<position>", String.valueOf(lineNum))
              .replace("<player>",
                  Bukkit.getOfflinePlayer(uuid).getName() != null ? Bukkit.getOfflinePlayer(uuid)
                      .getName() : "???")
              .replace("<elo>", String.valueOf(statElo))
              .replace("<rating>", RatingUtil.getRankByElo(statElo).getName())
          );
        } catch (Exception ignored) {
        }
      }
    }

    lore.add(CC.BOTTOM_SPLITTER);

    return new ItemBuilder(material).name(name).lore(lore).durability(0).build();
  }
}
