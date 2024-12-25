package dev.demeng.frost.user.ui.postmatch;

import static dev.demeng.frost.util.CC.parse;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.ItemUtil;
import dev.demeng.frost.util.MathUtil;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.inventory.InventoryUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

@Getter
public class InventorySnapshot {

  private final InventoryUI inventoryUI;
  private final ItemStack[] originalInventory;
  private final ItemStack[] originalArmor;

  private final Frost plugin = Frost.getInstance();
  private final ConfigCursor menu = new ConfigCursor(plugin.getMenusConfig(),
      "POST-MATCH-INVENTORY");

  @Getter
  private final UUID snapshotId = UUID.randomUUID();

  public InventorySnapshot(Player player, Match match) {
    ItemStack[] contents = player.getInventory().getContents();
    ItemStack[] armor = player.getInventory().getArmorContents();

    this.originalInventory = contents;
    this.originalArmor = armor;

    double health = player.getHealth();
    double food = player.getFoodLevel();

    List<String> potionEffectStrings = new ArrayList<>();
    for (PotionEffect potionEffect : player.getActivePotionEffects()) {
      String romanNumeral = MathUtil.convertToRomanNumeral(potionEffect.getAmplifier() + 1);
      String potionName = WordUtils.capitalize(
          potionEffect.getType().getName().replace("_", "").toLowerCase());
      String duration = MathUtil.convertTicksToMinutes(potionEffect.getDuration());

      potionEffectStrings.add(parse(player, menu.getString("POTIONS.POTION-EFFECTS")
          .replace("<potion_name>", potionName)
          .replace("<potion_lvl>", romanNumeral == null ? "" : romanNumeral)
          .replace("<potion_duration>", duration)
      ));
    }

    this.inventoryUI = new InventoryUI(parse(player,
        plugin.getMenusConfig().getConfig().getString("POST-MATCH-INVENTORY.TITLE")
            .replace("<player>", player.getName())), true, 6);
    for (int i = 0; i < 9; i++) {
      this.inventoryUI.setItem(i + 27, new InventoryUI.EmptyClickableItem(contents[i]));
      this.inventoryUI.setItem(i + 18, new InventoryUI.EmptyClickableItem(contents[i + 27]));
      this.inventoryUI.setItem(i + 9, new InventoryUI.EmptyClickableItem(contents[i + 18]));
      this.inventoryUI.setItem(i, new InventoryUI.EmptyClickableItem(contents[i + 9]));
    }

    boolean potionMatch = false;
    boolean soupMatch = false;

    for (ItemStack item : match.getKit().getContents()) {
      if (item == null) {
        continue;
      }
      if (item.getType() == Material.MUSHROOM_SOUP) {
        soupMatch = true;
        break;
      } else if (item.getType() == Material.POTION && item.getDurability() == (short) 16421) {
        potionMatch = true;
        break;
      }
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (potionMatch) {
      int potCount = (int) Arrays.stream(contents).filter(Objects::nonNull)
          .map(ItemStack::getDurability).filter(d -> d == 16421).count();
      this.inventoryUI.setItem(47, new InventoryUI.EmptyClickableItem(
          ItemUtil.reloreItem(ItemUtil.createItem(Material.POTION,
                  parse(player, menu.getString("COUNTERS.HEALTH-POTIONS")
                      .replace("<potions_left>", String.valueOf(potCount))), potCount, (short) 16421),
              parse(player, menu.getString("COUNTERS.POTIONS-MISSED")
                  .replace("<potions_missed>", String.valueOf(practicePlayerData.getMissedPots()))),
              parse(player, menu.getString("COUNTERS.POTION-ACCURACY").replace("<potion_accuracy>",
                  potionAccuracy(practicePlayerData.getMissedPots(),
                      practicePlayerData.getThrownPots())))))
      );
    } else if (soupMatch) {
      int soupCount = (int) Arrays.stream(contents).filter(Objects::nonNull).map(ItemStack::getType)
          .filter(d -> d == Material.MUSHROOM_SOUP).count();
      this.inventoryUI.setItem(47,
          new InventoryUI.EmptyClickableItem(ItemUtil.createItem(Material.MUSHROOM_SOUP,
              parse(player, menu.getString("COUNTERS.REMAINING-SOUPS")
                  .replace("<soups_left>", String.valueOf(soupCount))), soupCount))
      );
    }

    this.inventoryUI.setItem(48, new InventoryUI.EmptyClickableItem(
        ItemUtil.createItem(Material.SKULL_ITEM, parse(player, menu.getString("PLAYER.HEALTH")
            .replace("<health_amount>", String.valueOf(MathUtil.roundToHalves(health / 2.0D)))))));
    this.inventoryUI.setItem(49, new InventoryUI.EmptyClickableItem(
        ItemUtil.createItem(Material.COOKED_BEEF, parse(player, menu.getString("PLAYER.HUNGER")
            .replace("<hunger_amount>", String.valueOf(MathUtil.roundToHalves(food / 2.0D)))))));
    this.inventoryUI.setItem(50, new InventoryUI.EmptyClickableItem(ItemUtil.reloreItem(
        ItemUtil.createItem(Material.BREWING_STAND_ITEM,
            parse(player, menu.getString("POTIONS.NAME")), potionEffectStrings.size()),
        potionEffectStrings.toArray(new String[]{}))));
    this.inventoryUI.setItem(51, new InventoryUI.EmptyClickableItem(
        ItemUtil.reloreItem(ItemUtil.createItem(Material.DIAMOND_SWORD,
                parse(player, menu.getString("MATCH.STATISTICS-NAME"))),
            parse(player, menu.getString("MATCH.TOTAL-HITS")
                .replace("<hits_amount>", String.valueOf(practicePlayerData.getHits()))),
            parse(player, menu.getString("MATCH.LONGEST-COMBO")
                .replace("<longest_combo>", String.valueOf(practicePlayerData.getLongestCombo())))))
    );

    if (!match.isParty()) {
      this.inventoryUI.setItem(45, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(
          ItemUtil.createItem(Material.LEVER,
              parse(player, menu.getString("MATCH.NEXT-INVENTORY.NAME"))),
          parse(player, menu.getString("MATCH.NEXT-INVENTORY.LORE")))) {
        @Override
        public void onClick(InventoryClickEvent inventoryClickEvent) {
          Player clicker = (Player) inventoryClickEvent.getWhoClicked();
          if (plugin.getManagerHandler().getMatchManager().hasPostMatch(player.getUniqueId())) {
            clicker.performCommand("_ " + plugin.getManagerHandler().getMatchManager()
                .getPostMatchInventory(player.getUniqueId()));
          }
        }
      });
      this.inventoryUI.setItem(53, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(
          ItemUtil.createItem(Material.LEVER,
              parse(player, menu.getString("MATCH.NEXT-INVENTORY.NAME"))),
          parse(player, menu.getString("MATCH.NEXT-INVENTORY.LORE")))) {
        @Override
        public void onClick(InventoryClickEvent inventoryClickEvent) {
          Player clicker = (Player) inventoryClickEvent.getWhoClicked();
          if (plugin.getManagerHandler().getMatchManager().hasPostMatch(player.getUniqueId())) {
            clicker.performCommand("_ " + plugin.getManagerHandler().getMatchManager()
                .getPostMatchInventory(player.getUniqueId()));
          }
        }
      });
    }

    for (int i = 36; i < 40; i++) {
      this.inventoryUI.setItem(i, new InventoryUI.EmptyClickableItem(armor[39 - i]));
    }
  }

  private String potionAccuracy(int potionsMissed, int potionsThrown) {
    if (potionsThrown == 0) {
      return "N/A";
    } else if (potionsMissed == 0) {
      return "100%";
    } else if (potionsThrown == potionsMissed) {
      return "50%";
    }

    return Math.round(100.0D - ((double) potionsMissed / (double) potionsThrown) * 100.0D) + "%";
  }
}
