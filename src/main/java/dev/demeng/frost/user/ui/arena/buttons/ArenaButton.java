package dev.demeng.frost.user.ui.arena.buttons;

import dev.demeng.frost.game.arena.Arena;
import dev.demeng.frost.user.ui.arena.ArenaCopyMenu;
import dev.demeng.frost.user.ui.arena.ArenaGenerationMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.ItemBuilder;
import dev.demeng.frost.util.menu.Button;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class ArenaButton extends Button {

  private final Arena arena;

  @Override
  public ItemStack getButtonItem(Player player) {
    return new ItemBuilder(Material.valueOf(arena.getIcon()))
        .durability(arena.getIconData())
        .name("&b" + arena.getName())
        .lore(CC.color(
                Arrays.asList(
                    " ",
                    "&bArena Information&7:",
                    " &9&l▸ &fState: " + (arena.isEnabled() ? "&aEnabled" : "&cDisabled"),
                    " &9&l▸ &fType: &b" + (arena.getAvailableArenas().isEmpty() ? "Shared"
                        : "Standalone"),
                    " &9&l▸ &fCopies: &b" + (arena.getStandaloneArenas().isEmpty() ? "Not Required!"
                        : arena.getStandaloneArenas().size()),
                    " &9&l▸ &fAvailable: &b" + (arena.getAvailableArenas().isEmpty() ? +1
                        : arena.getAvailableArenas().size()),
                    " ",
                    (arena.getStandaloneArenas().isEmpty()
                        ? "&4&l&mMIDDLE-CLICK &4&mto see arena copies"
                        : "&6&lMIDDLE-CLICK &6to see arena copies"),
                    "&a&lLEFT-CLICK &ato teleport to arena",
                    "&b&lRIGHT CLICK &bto generate standalone arenas")
            )
        )
        .hideFlags()
        .build();
  }

  @Override
  public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
    playSuccess(player);
    switch (clickType) {
      case LEFT:
        player.teleport(arena.getA().toBukkitLocation());
        break;
      case RIGHT:
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
            () -> new ArenaGenerationMenu(arena).openMenu(player), 1L);
        break;
      case MIDDLE:
        if (!arena.getStandaloneArenas().isEmpty()) {
          Bukkit.getScheduler()
              .runTaskLaterAsynchronously(plugin, () -> new ArenaCopyMenu(arena).openMenu(player),
                  1L);
        }
        break;
    }

    player.closeInventory();
  }
}
