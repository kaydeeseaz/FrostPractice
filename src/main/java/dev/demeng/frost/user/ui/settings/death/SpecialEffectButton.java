package dev.demeng.frost.user.ui.settings.death;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.effects.SpecialEffects;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SpecialEffectButton extends Button {

  private final Frost plugin = Frost.getInstance();

  private final SpecialEffects specialEffect;
  private final PracticePlayerData practicePlayerData;

  private final List<SpecialEffects> effects;

  public SpecialEffectButton(SpecialEffects specialEffect, PracticePlayerData practicePlayerData) {
    this.specialEffect = specialEffect;
    this.practicePlayerData = practicePlayerData;

    this.effects = Arrays.stream(SpecialEffects.values()).filter(
        specialEffects -> specialEffects.hasPermission(
            Bukkit.getPlayer(practicePlayerData.getUniqueId()))).collect(Collectors.toList());
  }

  @Override
  public ItemStack getButtonItem(Player player) {
    List<String> lore = new ArrayList<>();

    lore.add(CC.TOP_SPLITTER);
    for (SpecialEffects effect : effects) {
      lore.add(practicePlayerData.getPlayerSettings().getSpecialEffect().getName()
          .equals(effect.getName())
          ? plugin.getMenusConfig().getConfig()
          .getString("DEATH-EFFECTS-INVENTORY.SPECIAL-EFFECTS.SELECTED") + effect.getName()
          : plugin.getMenusConfig().getConfig()
              .getString("DEATH-EFFECTS-INVENTORY.SPECIAL-EFFECTS.UNSELECTED") + effect.getName()
      );
    }
    lore.add(" ");
    lore.add(plugin.getMenusConfig().getConfig()
        .getString("DEATH-EFFECTS-INVENTORY.SPECIAL-EFFECTS.CLICK-TO-CHANGE"));
    lore.add(CC.BOTTOM_SPLITTER);

    return new ItemBuilder(specialEffect.getIcon())
        .name(CC.parse(player, plugin.getMenusConfig().getConfig()
            .getString("DEATH-EFFECTS-INVENTORY.SPECIAL-EFFECTS.NAME") + specialEffect.getName()))
        .lore(CC.parse(player, lore))
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    int currentValue = effects.indexOf(specialEffect);
    if (clickType == ClickType.LEFT) {
      if (currentValue >= effects.size() - 1) {
        currentValue = 0;
      } else {
        currentValue++;
      }
    } else if (clickType == ClickType.RIGHT) {
      if (currentValue <= 0) {
        currentValue = effects.size() - 1;
      } else {
        currentValue--;
      }
    }

    playSuccess(player);
    practicePlayerData.getPlayerSettings().setSpecialEffect(effects.get(currentValue));
    player.sendMessage(CC.color(
        "&aYou've selected the " + effects.get(currentValue).getName() + " special effect!"));
  }
}
