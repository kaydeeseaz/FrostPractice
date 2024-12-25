package dev.demeng.frost.user.ui.arena.buttons;

import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class ArenaGenerateButton extends Button {

  private final Arena arena;
  private final int currentCopyAmount;

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.PAPER)
        .name("&aCreate " + currentCopyAmount + " Arena Copies")
        .lore(CC.color(
            Arrays.asList(
                " ",
                "&7Clicking here will generate &b&l" + currentCopyAmount,
                "&7arenas for the map &b" + arena.getName() + "&7!",
                " ",
                "&a&lLEFT-CLICK &ato generate arenas")
        ))
        .amount(currentCopyAmount)
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    player.performCommand("arena generate " + arena.getName() + " " + currentCopyAmount);

    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lGENERATING ARENAS&b..."));
    player.sendMessage(CC.color(" "));
    player.sendMessage(CC.color("&fFrost is currently generating copies for:"));
    player.sendMessage(CC.color(" &9&l▸ &fArena: &b" + arena.getName()));
    player.sendMessage(CC.color(" &9&l▸ &fCopies: &b" + currentCopyAmount));
    player.sendMessage(CC.color(" "));
    player.sendMessage(CC.color("&7&oYou can check the progress in console."));
    player.sendMessage(CC.CHAT_BAR);

    player.closeInventory();
  }
}
