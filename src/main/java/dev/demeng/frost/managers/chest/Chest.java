package dev.demeng.frost.managers.chest;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class Chest {

  private ItemStack[] items;
  private int number;

  public Chest(ItemStack[] items, int number) {
    this.items = items;
    this.number = number;
  }
}
