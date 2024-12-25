package dev.demeng.frost.commands.admin.kit;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.CC;
import java.util.ArrayList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("kit set")
@CommandPermission("frost.admin")
public class KitSetCommand {

  @Dependency private Frost plugin;
  private final String NO_KIT = CC.color("&4&lERROR&4! &cThat kit doesn't exist!");

  @DefaultFor("kit set")
  public void getHelpMessage(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()
        + " &8- &fKit Set - Command Help"));
    player.sendMessage(CC.color("&a&l • &7 - &fRequired command"));
    player.sendMessage(CC.color("&e&l • &7 - &fOptional command"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&a • &b/kit set icon <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit set inv <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit set editorinv <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit set unrankedpos <kitName> <slot>"));
    player.sendMessage(CC.color("&a • &b/kit set rankedpos <kitName> <slot>"));
    player.sendMessage(CC.color("&a • &b/kit set editorpos <kitName> <slot>"));
    player.sendMessage(CC.color("&a • &b/kit set spawnffapos <kitName> <slot>"));
    player.sendMessage(CC.color("&e • &b/kit set kb <kitName>"));
    player.sendMessage(CC.color("&e • &b/kit set displayName <kitName>"));
    player.sendMessage(CC.color("&e • &b/kit set damageticks <kitName> <ticks>"));
    player.sendMessage(CC.color("&e • &b/kit set lives <kitName> <amount>"));
    player.sendMessage(CC.color("&e • &b/kit set refillinv <kitName>"));
    player.sendMessage(CC.color("&e • &b/kit set effects <kitName>"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("displayname")
  public void setDisplayName(Player player, Kit kit, String displayName) {
    if (kit != null) {
      kit.setDisplayName(ChatColor.stripColor(displayName).replace("_", " "));
      player.sendMessage(CC.color(
          "&aSuccessfully updated display name for '&a&l" + kit.getName() + "&a' to "
              + kit.getDisplayName() + "&a."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("kb")
  public void setKnockback(Player player, Kit kit, String knockbackProfile) {
    if (kit != null) {
      kit.setKbProfile(knockbackProfile);
      player.sendMessage(CC.color(
          "&aSuccessfully updated knockback profile for '&a&l" + kit.getName() + "&a' to "
              + knockbackProfile + "."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("lives")
  public void setLives(Player player, Kit kit, int amount) {
    if (kit != null) {
      kit.setLives(amount);
      player.sendMessage(CC.color(
          "&aSuccessfully updated lives for '&a&l" + kit.getName() + "&a' to " + amount
              + " lives."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("damageticks")
  public void setDamageTicks(Player player, Kit kit, int ticks) {
    if (kit != null) {
      kit.setDamageTicks(ticks);
      player.sendMessage(CC.color(
          "&aSuccessfully updated damage ticks for '&a&l" + kit.getName() + "&a' to " + ticks
              + " ticks per hit."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("icon")
  public void setIcon(Player player, Kit kit) {
    if (kit != null) {
      if (player.getItemInHand().getType() != Material.AIR) {
        kit.setIcon(player.getItemInHand());
        player.sendMessage(
            CC.color("&aSuccessfully set icon for kit '&a&l" + kit.getName() + "&a'!"));
      } else {
        player.sendMessage(CC.color("&cYou must be holding an item to set the kit icon!"));
      }
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("inv")
  public void setKitInventory(Player player, Kit kit) {
    if (kit != null) {
      if (player.getGameMode() == GameMode.CREATIVE) {
        player.sendMessage(CC.color("&cYou can't set the kit inventory while in creative mode!"));
      } else {
        player.updateInventory();
        kit.setContents(player.getInventory().getContents());
        kit.setArmor(player.getInventory().getArmorContents());
        player.sendMessage(
            CC.color("&aKit inventory successfully set for '&a&l" + kit.getName() + "&a'!"));
      }
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("refillinv")
  public void setRefillInventory(Player player, Kit kit) {
    if (kit != null) {
      if (player.getGameMode() == GameMode.CREATIVE) {
        player.sendMessage(
            CC.color("&cYou can't set the refillable kit items while in creative mode!"));
      } else {
        player.updateInventory();
        kit.setEditorItems(player.getInventory().getContents());
        player.sendMessage(
            CC.color("&aSuccessfully set refillable kit items for '&a&l" + kit.getName() + "&a'!"));
      }
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("editorinv")
  public void setEditableInventory(Player player, Kit kit) {
    if (kit != null) {
      if (player.getGameMode() == GameMode.CREATIVE) {
        player.sendMessage(
            CC.color("&cYou can't set the editable kit inventory while in creative mode!"));
      } else {
        player.updateInventory();
        kit.setKitEditContents(player.getInventory().getContents());
        player.sendMessage(CC.color(
            "&aSuccessfully set editable kit inventory for '&a&l" + kit.getName() + "&a'!"));
      }
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("effects")
  public void setPotionEffects(Player player, Kit kit) {
    if (kit != null) {
      if (!player.getActivePotionEffects().isEmpty()) {
        kit.setPotionEffects(new ArrayList<>(player.getActivePotionEffects()));
      } else {
        kit.setPotionEffects(new ArrayList<>());
      }
      player.sendMessage(
          CC.color("&aSuccessfully set the effects for kit '&a&l" + kit.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("unrankedpos")
  public void setUnrankedKitPosition(Player player, Kit kit, int position) {
    if (kit != null) {
      kit.setUnrankedPos(position);
      player.sendMessage(CC.color(
          "&aSuccessfully set the unranked kit position for kit '&a&l" + kit.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("rankedpos")
  public void setRankedKitPosition(Player player, Kit kit, int position) {
    if (kit != null) {
      kit.setRankedPos(position);
      player.sendMessage(CC.color(
          "&aSuccessfully set the ranked kit position for kit '&a&l" + kit.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("editorpos")
  public void setEditorKitPosition(Player player, Kit kit, int position) {
    if (kit != null) {
      kit.setEditorPos(position);
      player.sendMessage(CC.color(
          "&aSuccessfully set the ranked kit position for kit '&a&l" + kit.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("spawnffapos")
  public void setSpawnFfaKitPosition(Player player, Kit kit, int position) {
    if (kit != null) {
      kit.setSpawnFfaPos(position);
      player.sendMessage(CC.color(
          "&aSuccessfully set the spawn ffa kit position for kit '&a&l" + kit.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_KIT);
    }
  }
}
