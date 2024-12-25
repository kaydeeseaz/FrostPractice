package dev.demeng.frost.commands.admin;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.chests.ChestSelectionMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.MathUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetLootCommand extends Command {

  private final Frost plugin = Frost.getInstance();

  public SetLootCommand() {
    super("setloot");
    this.setDescription("Set SkyWars loot for chests.");
    this.setUsage(ChatColor.RED + "Usage: /setloot");
  }

  @Override
  public boolean execute(CommandSender sender, String alias, String[] args) {
    if (!(sender instanceof Player)) {
      return true;
    }

    Player player = (Player) sender;
    if (!player.hasPermission("frost.admin")) {
      player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
      return true;
    }

    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    if (!practicePlayerData.isInSpawn()) {
      player.sendMessage(ChatColor.RED + "You can only edit SkyWars Loot Chests at spawn.");
      return true;
    }

    if (args.length == 0) {
      new ChestSelectionMenu().openMenu(player);
    } else if (args.length == 1) {
      List<ItemStack> itemList = new ArrayList<>();
      for (int i = 0; i <= 9; i++) {
        ItemStack it = player.getInventory().getItem(i);
        if (it != null) {
          itemList.add(it);
        }
      }

      ItemStack[] items = itemList.toArray(new ItemStack[0]);
      if (MathUtil.isInteger(args[0])) {
        int key = Integer.parseInt(args[0]);
        plugin.getManagerHandler().getChestManager().updateChestItems(key, items);
        plugin.getManagerHandler().getChestManager().saveChestsToConfig();
        player.sendMessage(CC.color("&aSuccessfully set SkyWars Loot Chest &e#" + key + "&a."));
      } else {
        player.sendMessage(CC.color("&cInvalid chest number. Chests are numbered from 1 to 9."));
      }
    }

    return true;
  }
}
