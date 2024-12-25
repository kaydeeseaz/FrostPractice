package dev.demeng.frost.user.ui.settings.death;

import dev.demeng.frost.user.effects.SpecialEffects;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.ConfigurableMenuUtil;
import dev.demeng.frost.user.ui.settings.SettingUpdateButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class DeathEffectsMenu extends Menu {

  private final ConfigurableMenuUtil menuUtil = new ConfigurableMenuUtil(this.plugin);
  private final ConfigCursor cursor = new ConfigCursor(plugin.getMenusConfig(),
      "DEATH-EFFECTS-INVENTORY");

  public DeathEffectsMenu() {
    setUpdateAfterClick(true);
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, cursor.getString("TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    SpecialEffects specialEffects = practicePlayerData.getPlayerSettings().getSpecialEffect();

    buttons.put(
        plugin.getMenusConfig().getConfig().getInt("DEATH-EFFECTS-INVENTORY.SPECIAL-EFFECTS.SLOT"),
        new SpecialEffectButton(specialEffects, practicePlayerData));
    buttons.put(menuUtil.getSettingSlot("BODY_ANIMATION"),
        new SettingUpdateButton(menuUtil.getSettingName("BODY_ANIMATION"),
            menuUtil.getSettingIcon("BODY_ANIMATION"), 0,
            Arrays.asList(menuUtil.getSettingLore("BODY_ANIMATION")), "togglebodyanimation",
            "bodyAnimation")
    );
    buttons.put(menuUtil.getSettingSlot("CLEAR_INVENTORY"),
        new SettingUpdateButton(menuUtil.getSettingName("CLEAR_INVENTORY"),
            menuUtil.getSettingIcon("CLEAR_INVENTORY"), 0,
            Arrays.asList(menuUtil.getSettingLore("CLEAR_INVENTORY")), "toggleinventoryclear",
            "clearInventory")
    );
    buttons.put(menuUtil.getSettingSlot("START_FLYING"),
        new SettingUpdateButton(menuUtil.getSettingName("START_FLYING"),
            menuUtil.getSettingIcon("START_FLYING"), 0,
            Arrays.asList(menuUtil.getSettingLore("START_FLYING")), "toggleflightstart",
            "startFlying")
    );

    return buttons;
  }

  @Override
  public int getSize() {
    return cursor.getInt("SIZE") * 9;
  }
}
