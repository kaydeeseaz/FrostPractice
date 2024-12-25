package dev.demeng.frost.game.kit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@AllArgsConstructor
public class PlayerKit {

  private String name;
  private final int index;

  private ItemStack[] contents;
  private String displayName;
}
