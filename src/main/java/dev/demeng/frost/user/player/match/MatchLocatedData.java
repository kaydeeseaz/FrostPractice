package dev.demeng.frost.user.player.match;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.InventoryUtil;
import dev.demeng.pluginbase.mongo.lib.bson.Document;
import dev.demeng.pluginbase.mongo.lib.driver.client.model.Filters;
import dev.demeng.pluginbase.mongo.lib.driver.client.model.ReplaceOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@NoArgsConstructor
public class MatchLocatedData {

  private final Frost plugin = Frost.getInstance();

  private String id;

  private UUID winnerUUID;
  private UUID loserUUID;

  private int winnerEloModifier;
  private int loserEloModifier;

  private int winnerElo;
  private int loserElo;

  private String date;
  private String kit;

  private ItemStack[] winnerArmor;
  private ItemStack[] winnerContents;

  private ItemStack[] loserArmor;
  private ItemStack[] loserContents;

  private MatchHistoryInvSnap matchHistoryInvSnap;

  public MatchLocatedData(Document document) {
    this.id = document.getString("id");

    this.winnerUUID = UUID.fromString(document.getString("winnerUuid"));
    this.loserUUID = UUID.fromString(document.getString("loserUuid"));

    this.winnerEloModifier = document.getInteger("winnerEloModifier");
    this.loserEloModifier = document.getInteger("loserEloModifier");

    this.winnerElo = document.getInteger("winnerElo");
    this.loserElo = document.getInteger("loserElo");

    this.date = document.getString("date");
    this.kit = document.getString("kit");

    this.winnerArmor = InventoryUtil.deserializeInventory(document.getString("winnerArmor"));
    this.winnerContents = InventoryUtil.deserializeInventory(document.getString("winnerContents"));

    this.loserArmor = InventoryUtil.deserializeInventory(document.getString("loserArmor"));
    this.loserContents = InventoryUtil.deserializeInventory(document.getString("loserContents"));

    this.matchHistoryInvSnap = new MatchHistoryInvSnap(this);
  }

  public void save() {
    Document document = new Document();
    document.put("id", this.id);

    document.put("winnerUuid", this.winnerUUID.toString());
    document.put("loserUuid", this.loserUUID.toString());

    document.put("winnerEloModifier", this.winnerEloModifier);
    document.put("loserEloModifier", this.loserEloModifier);

    document.put("winnerElo", this.winnerElo);
    document.put("loserElo", this.loserElo);

    document.put("date", this.date);
    document.put("kit", this.kit);

    document.put("winnerArmor", InventoryUtil.serializeInventory(this.winnerArmor));
    document.put("winnerContents", InventoryUtil.serializeInventory(this.winnerContents));
    document.put("loserArmor", InventoryUtil.serializeInventory(this.loserArmor));
    document.put("loserContents", InventoryUtil.serializeInventory(this.loserContents));

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      plugin.getMongoHandler().getMatchHistory().replaceOne(
          Filters.eq("id", this.id), document, new ReplaceOptions().upsert(true));
    });
  }

  public List<MatchLocatedData> getMatchesByUser(UUID uuid) {
    List<MatchLocatedData> locatedData = new ArrayList<>();
    List<Document> documents = plugin.getMongoHandler().getMatchHistory().find()
        .into(new ArrayList<>());

    for (Document document : documents) {
      if (document.getString("winnerUuid").equalsIgnoreCase(uuid.toString()) || document.getString(
          "loserUuid").equalsIgnoreCase(uuid.toString())) {
        locatedData.add(new MatchLocatedData(document));
      }
    }

    return locatedData;
  }
}
