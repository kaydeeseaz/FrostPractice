package dev.demeng.frost.user.ui.host;

import dev.demeng.frost.Frost;
import dev.demeng.frost.events.EventState;
import dev.demeng.frost.events.PracticeEvent;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import dev.demeng.frost.util.menu.Menu;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class EventManagerMenu extends Menu {

  private final Frost plugin = Frost.getInstance();

  @Override
  public String getTitle(Player player) {
    return CC.color("&bEvent Manager");
  }

  @Override
  public Map<Integer, Button> getButtons(Player player) {
    Map<Integer, Button> buttons = new HashMap<>();

    PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();
    buttons.put(2, new EventManageButton(CC.color("&a&lForce-Start Event &7▸ &f" + event.getName()),
        Material.EMERALD, 0, Arrays.asList(
        CC.DARK_MENU_BAR,
        "&7Clicking here will reduce",
        "&7the event countdown down to 5",
        " ",
        "&eClick here to force-start the &a" + event.getName() + "&e event.",
        CC.DARK_MENU_BAR)
    ));

    buttons.put(4, new EventManageButton(CC.color("&e&lEvent Status &7▸ &f" + event.getName()),
        Material.GLOWSTONE_DUST, 0, Arrays.asList(
        CC.DARK_MENU_BAR,
        " &9&l▸ &fHost: &b" + (event.getHost() == null ? "&cNone" : event.getHost().getName()),
        " &9&l▸ &fEvent: &b" + event.getName(),
        " &9&l▸ &fPlayers: &b" + event.getPlayers().size() + "&7 out of &b" + event.getLimit(),
        " &9&l▸ &fState: &b" + WordUtils.capitalize(event.getState().name().toLowerCase()),
        CC.DARK_MENU_BAR)
    ));

    buttons.put(6, new EventManageButton(CC.color("&c&lStop Event &7▸ &f" + event.getName()),
        Material.REDSTONE, 0, Arrays.asList(
        CC.DARK_MENU_BAR,
        "&7Clicking here will end",
        "&7the event that is ongoing",
        "",
        "&eClick here to stop the &a" + event.getName() + "&e event.",
        CC.DARK_MENU_BAR)
    ));

    return buttons;
  }

  @Override
  public int size(Map<Integer, Button> buttons) {
    return 9;
  }

  @AllArgsConstructor
  public static class EventManageButton extends Button {

    private final Frost plugin = Frost.getInstance();

    private final String name;
    private final Material material;
    private final int durability;
    private final List<String> lore;

    @Override
    public ItemStack getButtonItem(Player player) {
      List<String> lines = new ArrayList<>(lore);
      return new ItemBuilder(material).name(name).amount(1).lore(lines).durability(durability)
          .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
      PracticeEvent<?> event = plugin.getManagerHandler().getEventManager().getOngoingEvent();

      if (slot == 2) {
        if (event.getState() != EventState.UNANNOUNCED) {
          playSuccess(player);
          event.getCountdownTask().setTimeUntilStart(5);
          player.sendMessage(
              CC.color("&aSuccessfully force-started the " + event.getName() + " event!"));
        } else {
          player.sendMessage(CC.color("&cThe event is not active!"));
        }
        player.closeInventory();
      } else if (slot == 6) {
        if (event.getState() != EventState.UNANNOUNCED) {
          event.end();
          playFail(player);
          player.sendMessage(CC.color("&a" + event.getName() + " event successfully stopped."));
        } else {
          player.sendMessage(CC.color("&cThe event is not active!"));
        }
        player.closeInventory();
      }
    }
  }
}
