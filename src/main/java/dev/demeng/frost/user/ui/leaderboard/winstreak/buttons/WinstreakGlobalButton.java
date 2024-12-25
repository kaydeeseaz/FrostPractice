package dev.demeng.frost.user.ui.leaderboard.winstreak.buttons;

import com.google.common.collect.Lists;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.pluginbase.mongo.lib.bson.Document;
import dev.demeng.pluginbase.mongo.lib.driver.client.MongoCursor;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class WinstreakGlobalButton extends Button {

  private final Material material;
  private final String document;

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = Lists.newArrayList();
    AtomicInteger lineNum = new AtomicInteger();
    String name = plugin.getMenusConfig().getConfig()
        .getString("LEADERBOARDS-INVENTORY.WINSTREAK-GLOBAL-ITEM.NAME");

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
          Document winstreak = (Document) document.get("global");
          int winstreakInt = winstreak.getInteger(this.document);

          lore.add(plugin.getMenusConfig().getConfig()
              .getString("LEADERBOARDS-INVENTORY.WINSTREAK-GLOBAL-ITEM.LINE")
              .replace("<position>", String.valueOf(lineNum))
              .replace("<player>",
                  Bukkit.getOfflinePlayer(uuid) != null ? Bukkit.getOfflinePlayer(uuid).getName()
                      : "???")
              .replace("<winstreak>", String.valueOf(winstreakInt))
          );
        } catch (Exception ignored) {
        }
      }
    }

    lore.add(CC.BOTTOM_SPLITTER);

    return new ItemBuilder(material).name(name).lore(lore).durability(0).build();
  }
}
