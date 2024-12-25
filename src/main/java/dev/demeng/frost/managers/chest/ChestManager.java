package dev.demeng.frost.managers.chest;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.InventoryUtil;
import dev.demeng.frost.util.MathUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class ChestManager {

  private final Frost plugin;
  private List<Chest> chests = new ArrayList<>();

  public ChestManager(Frost plugin) {
    this.plugin = plugin;

    if (plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS") == null) {
      return;
    }

    plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS").getKeys(false)
        .forEach(key -> {
          if (plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS") != null) {
            if (!plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS")
                .contains(String.valueOf(key))) {
              chests.add(new Chest(null, Integer.parseInt(key)));
            }
          } else {
            chests.add(new Chest(null, Integer.parseInt(key)));
          }
        });

    loadChestsFromConfig();
    sortChestsByNumber();
  }

  public Chest getChest(int chestNumber) {
    return chests.get(chestNumber - 1);
  }

  public void updateChestItems(int chestNumber, ItemStack[] items) {
    Chest chest = chests.get(chestNumber - 1);
    chest.setItems(items);
  }

  public void saveChestsToConfig() {
    plugin.getChestConfig().getConfig().createSection("CHESTS");
    for (Chest chest : chests) {
      if (chest.getItems() != null) {
        plugin.getChestConfig().getConfig()
            .set("CHESTS." + chest.getNumber(), InventoryUtil.serializeInventory(chest.getItems()));
      }
    }

    plugin.getChestConfig().save();
  }

  public void loadChestsFromConfig() {
    if (plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS") == null) {
      return;
    }

    plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS").getKeys(false)
        .forEach(key -> {
          if (MathUtil.isInteger(key)) {
            ItemStack[] items = InventoryUtil.deserializeInventory(
                plugin.getChestConfig().getConfig().getString("CHESTS." + key));
            chests.add(new Chest(items, Integer.parseInt(key)));
          }
        });
  }

  public ItemStack[] getRandomItemsFromChests() {
    if (plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS") == null) {
      return null;
    }

    List<Chest> availableChests = new ArrayList<>();
    plugin.getChestConfig().getConfig().getConfigurationSection("CHESTS").getKeys(false)
        .forEach(key -> {
          if (MathUtil.isInteger(key)) {
            ItemStack[] items = InventoryUtil.deserializeInventory(
                plugin.getChestConfig().getConfig().getString("CHESTS." + key));
            if (items.length >= 1) {
              availableChests.add(new Chest(items, Integer.parseInt(key)));
            }
          }
        });

    if (availableChests.isEmpty()) {
      return null;
    }

    int random = plugin.getRandom().nextInt(9);
    if (this.chests.size() <= random) {
      return null;
    }

    return availableChests.get(random).getItems();
  }

  public void sortChestsByNumber() {
    List<Chest> fixed = chests.stream().sorted(Comparator.comparing(Chest::getNumber))
        .collect(Collectors.toList());

    chests.clear();
    chests.addAll(fixed);
  }
}
