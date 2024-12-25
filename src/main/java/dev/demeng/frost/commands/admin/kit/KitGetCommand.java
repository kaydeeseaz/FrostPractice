package dev.demeng.frost.commands.admin.kit;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("kit get")
@CommandPermission("frost.admin")
public class KitGetCommand {

  @Dependency private Frost plugin;
  private final String NO_KIT = CC.color("&4&lERROR&4! &cThat kit doesn't exist!");

  @DefaultFor("kit get")
  public void getHelpMessage(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()
        + " &8- &fKit Get - Command Help"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&7 • &b/kit get kb <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit get inv <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit get editorinv <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit get refillinv <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit get effects <kitName>"));
    player.sendMessage(CC.color("&7 • &b/kit get displayName <kitName>"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("displayname")
  public void getDisplayName(Player player, Kit kit) {
    if (kit != null) {
      player.sendMessage(CC.color(
          "&aThe display name for " + kit.getName() + " kit is " + kit.getDisplayName() + "&a."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("kb")
  public void getKnockback(Player player, Kit kit) {
    if (kit != null) {
      player.sendMessage(CC.color(
          "&a" + kit.getName() + " kit is binded to the " + kit.getKbProfile()
              + " knockback profile."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("inv")
  public void getKitInventory(Player player, Kit kit) {
    if (kit != null) {
      player.getInventory().setContents(kit.getContents());
      player.getInventory().setArmorContents(kit.getArmor());
      player.sendMessage(
          CC.color("&aSuccessfully retrieved kit items from " + kit.getName() + "."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("refillinv")
  public void getKitRefillableInventory(Player player, Kit kit) {
    if (kit != null) {
      player.getInventory().setContents(kit.getEditorItems());
      player.sendMessage(
          CC.color("&aSuccessfully retrieved refillable kit items from " + kit.getName() + "."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("editorinv")
  public void getKitEditorInventory(Player player, Kit kit) {
    if (kit != null) {
      player.getInventory().setContents(kit.getKitEditContents());
      player.getInventory().setArmorContents(kit.getArmor());
      player.sendMessage(
          CC.color("&aSuccessfully retrieved editable kit inventory from " + kit.getName() + "."));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("effects")
  public void getPotionEffects(Player player, Kit kit) {
    if (kit != null) {
      if (!kit.getPotionEffects().isEmpty()) {
        for (PotionEffect potionEffect : kit.getPotionEffects()) {
          player.addPotionEffect(potionEffect);
        }
      }
      player.sendMessage(
          CC.color("&aSuccessfully listed the effects for kit '&a&l" + kit.getName() + "&a'!"));
    } else {
      player.sendMessage(NO_KIT);
    }
  }
}
