package dev.demeng.frost.game.kit;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.ItemBuilder;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Kit {

  private final Frost plugin = Frost.getInstance();

  private final String name;
  private String displayName;

  private String kbProfile;
  private int unrankedPos, rankedPos, editorPos, spawnFfaPos;
  private ItemStack icon;

  private ItemStack[] contents = new ItemStack[36];
  private ItemStack[] armor = new ItemStack[4];
  private ItemStack[] kitEditContents = new ItemStack[36];

  private List<PotionEffect> potionEffects = new ArrayList<>();

  private List<String> arenaWhiteList = new ArrayList<>();

  private List<String> matchStartCommands = new ArrayList<>();
  private List<String> matchEndCommands = new ArrayList<>();

  private boolean enabled;
  private boolean ranked;

  private boolean combo;
  private boolean sumo;
  private boolean build;
  private boolean spleef;
  private boolean boxing;
  private boolean bridges;
  private boolean noRegen;
  private boolean noHunger;
  private boolean noFall;
  private boolean skyWars;
  private boolean bedWars;
  private boolean battleRush;
  private boolean stickFight;
  private boolean mlgRush;

  private int lives;
  private int damageTicks;

  private boolean showHealth;
  private boolean allowPotionFill;
  private boolean allowSpawnFfa;
  private ItemStack[] editorItems = new ItemStack[6];

  public void whitelistArena(String arena) {
    if (!this.arenaWhiteList.remove(arena)) {
      this.arenaWhiteList.add(arena);
    }
  }

  public boolean isMisc() {
    return this.bridges || this.bedWars || this.battleRush || this.stickFight || this.mlgRush;
  }

  public void applyKit(Player player) {
    player.getInventory().setContents(this.contents);
    player.getInventory().setArmorContents(this.armor);
    player.setMaximumNoDamageTicks(this.damageTicks);

    if (!this.potionEffects.isEmpty()) {
      for (PotionEffect potionEffect : this.potionEffects) {
        player.addPotionEffect(potionEffect);
      }
    }

    player.updateInventory();
  }

  public void applyKit(Player player, PracticePlayerData playerData) {
    ItemStack[] playerContents = this.contents;
    ItemStack[] playerArmor = this.armor;
    if (playerData.getCurrentKitContents() != null) {
      playerContents = playerData.getCurrentKitContents();
    }

    Color color = playerData.getTeamId() == 0 ? Color.RED : Color.BLUE;
    int data = color == Color.RED ? 14 : 11;

    int i = 0;
    ItemStack[] finalContents = this.getColoredItems(playerContents, data, i);
    player.getInventory().setContents(finalContents);
    playerData.setCurrentKitContents(finalContents);

    ItemStack[] finalArmor = this.getColoredArmor(playerArmor, color, i);
    player.getInventory().setArmorContents(finalArmor);

    if (!this.potionEffects.isEmpty()) {
      for (PotionEffect potionEffect : this.potionEffects) {
        player.addPotionEffect(potionEffect);
      }
    }

    player.setMaximumNoDamageTicks(this.damageTicks);
    player.updateInventory();
  }

  public ItemStack[] getColoredItems(ItemStack[] items, int color, int i) {
    ItemStack[] finalItems = new ItemStack[36];

    for (ItemStack item : items) {
      if (item != null && item.getType() != Material.AIR) {
        if (item.getType() == Material.STAINED_CLAY) {
          finalItems[i] = new ItemBuilder(item).durability(color).build();
        } else if (item.getType() == Material.WOOL) {
          finalItems[i] = new ItemBuilder(item).durability(color).build();
        } else {
          finalItems[i] = item;
        }
      }

      i++;
    }

    return finalItems;
  }

  public ItemStack[] getColoredArmor(ItemStack[] armor, Color color, int i) {
    ItemStack[] finalArmor = new ItemStack[4];

    for (ItemStack item : armor) {
      if (item != null && item.getType() != Material.AIR) {
        if (item.getType().name().startsWith("LEATHER_")) {
          finalArmor[i] = new ItemBuilder(item).color(color).build();
        } else {
          finalArmor[i] = item;
        }
      }

      i++;
    }

    return finalArmor;
  }
}
