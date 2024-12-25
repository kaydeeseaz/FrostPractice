package dev.demeng.frost.user.ui.queue.ffa;

import dev.demeng.frost.game.ffa.FfaInstance;
import dev.demeng.frost.game.ffa.FfaManager;
import dev.demeng.frost.user.ui.queue.ffa.button.FFASelectionButton;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class FFASelectionMenu extends Menu {

  private final FfaManager spawnFFAManager;
  private final ConfigCursor config = new ConfigCursor(plugin.getMenusConfig(), "FFA-MENU");

  public FFASelectionMenu(FfaManager spawnFFAManager) {
    this.spawnFFAManager = spawnFFAManager;

    if (plugin.getMenusConfig().getConfig()
        .getBoolean("QUEUE-INVENTORY.PLACEHOLDER-ITEMS-ENABLED")) {
      this.setPlaceholder(true);
    }

    setUpdateAfterClick(true);
  }

  @Override
  public String getTitle(Player player) {
    return CC.parse(player, config.getString("TITLE"));
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    HashMap<Integer, Button> buttons = new HashMap<>();

    for (Map.Entry<String, FfaInstance> entry : spawnFFAManager.getSpawnFfaKits().entrySet()) {
      FfaInstance ffaInstance = entry.getValue();
      if (ffaInstance.getKit().isAllowSpawnFfa()) {
        buttons.put(ffaInstance.getKit().getSpawnFfaPos(), new FFASelectionButton(ffaInstance));
      }
    }

    return buttons;
  }

  @Override
  public int getSize() {
    return 9 * config.getInt("SIZE");
  }
}
