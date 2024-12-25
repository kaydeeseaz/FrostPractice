package dev.demeng.frost.user.ui.arena.buttons;

import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.game.arena.StandaloneArena;
import dev.demeng.frost.runnable.ArenaCopyRemovalRunnable;
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
public class ArenaCopyButton extends Button {

  private final int number;
  private final Arena arena;
  private final StandaloneArena arenaCopy;

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.PAPER)
        .name("&8" + number)
        .lore(CC.color(
            Arrays.asList(
                "&bCopy Information&7:",
                " &9&l▸ &fParent Arena: &b" + arena.getName() + " &7(&f#" + number + "&7)",
                " &9&l▸ &f1st Spawn: &b" + Math.round(arenaCopy.getA().getX()) + "&7, &b"
                    + Math.round(arenaCopy.getA().getY()) + "&7, &b" + Math.round(
                    arenaCopy.getA().getZ()),
                " &9&l▸ &f2nd Spawn: &b" + Math.round(arenaCopy.getB().getX()) + "&7, &b"
                    + Math.round(arenaCopy.getB().getY()) + "&7, &b" + Math.round(
                    arenaCopy.getB().getZ()),
                " &9&l▸ &fMin Location: &b" + Math.round(arenaCopy.getMin().getX()) + "&7, &b"
                    + Math.round(arenaCopy.getMin().getY()) + "&7, &b" + Math.round(
                    arenaCopy.getMin().getZ()),
                " &9&l▸ &fMax Location: &b" + Math.round(arenaCopy.getMax().getX()) + "&7, &b"
                    + Math.round(arenaCopy.getMax().getY()) + "&7, &b" + Math.round(
                    arenaCopy.getMax().getZ()),
                " ",
                "&a&lLEFT-CLICK &ato teleport to this copy!",
                "&c&lRIGHT-CLICK &cto delete this copy!"
            ))
        )
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {

    if (clickType == ClickType.LEFT) {
      player.teleport(arenaCopy.getA().toBukkitLocation());
    } else if (clickType == ClickType.RIGHT) {
      new ArenaCopyRemovalRunnable(number, arena, arenaCopy).runTask(this.plugin);
    }

    player.closeInventory();
  }
}
