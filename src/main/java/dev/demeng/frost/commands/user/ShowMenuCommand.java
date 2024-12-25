package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.editor.SelectLadderKitMenu;
import dev.demeng.frost.user.ui.matches.OngoingMatchesMenu;
import dev.demeng.frost.user.ui.queue.ffa.FFASelectionMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.PlayerUtil;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;

@Command("showmenu")
public class ShowMenuCommand {

  @Dependency private Frost plugin;

  @Subcommand("unranked")
  public void openUnrankedMenu(Player player, PracticePlayerData practicePlayerData) {
    if (practicePlayerData.isInSpawn()) {
      player.openInventory(
          plugin.getManagerHandler().getInventoryManager().getUnrankedInventory().getCurrentPage());
    }
  }

  @Subcommand("premium")
  public void openPremiumMenu(Player player, PracticePlayerData practicePlayerData) {
    if (practicePlayerData.isInSpawn()) {
      player.openInventory(
          plugin.getManagerHandler().getInventoryManager().getPremiumInventory().getCurrentPage());
    }
  }

  @Subcommand("ranked")
  public void openRankedMenu(Player player, PracticePlayerData practicePlayerData) {
    if (PlayerUtil.getPing(player) >= plugin.getSettingsConfig().getConfig()
        .getInt("SETTINGS.MATCH.MAX-RANKED-PING")) {
      CC.sendMessage(player,
          plugin.getSettingsConfig().getConfig().getString("SETTINGS.MATCH.PING-TOO-HIGH-MESSAGE"));
      return;
    }

    if (practicePlayerData.isInSpawn()) {
      if (plugin.getSettingsConfig().getConfig().getInt("SETTINGS.MATCH.RANKEDS-REQUIRED") >= 1) {
        if (practicePlayerData.getMatchesPlayed() >= plugin.getSettingsConfig().getConfig()
            .getInt("SETTINGS.MATCH.RANKEDS-REQUIRED")) {
          player.openInventory(plugin.getManagerHandler().getInventoryManager().getRankedInventory()
              .getCurrentPage());
        } else if (player.hasPermission("frost.bypass.ranked")) {
          player.openInventory(plugin.getManagerHandler().getInventoryManager().getRankedInventory()
              .getCurrentPage());
        } else {
          player.sendMessage(CC.color("&cYou need to play " + (
              plugin.getSettingsConfig().getConfig().getInt("SETTINGS.MATCH.RANKEDS-REQUIRED")
                  - practicePlayerData.getMatchesPlayed())
              + " unranked matches before playing ranked!"));
        }
      } else {
        player.openInventory(
            plugin.getManagerHandler().getInventoryManager().getRankedInventory().getCurrentPage());
      }
    }
  }

  @Subcommand("ffa")
  public void openFfaMenu(Player player, PracticePlayerData practicePlayerData) {
    if (practicePlayerData.isInSpawn()) {
      new FFASelectionMenu(plugin.getManagerHandler().getFfaManager()).openMenu(player);
    }
  }

  @Subcommand("editor")
  public void openKitEditor(Player player, PracticePlayerData practicePlayerData) {
    if (practicePlayerData.isInSpawn()) {
      new SelectLadderKitMenu().openMenu(player);
    }
  }

  @Subcommand("matches")
  public void openMatchesMenu(Player player, PracticePlayerData practicePlayerData) {
    if (practicePlayerData.isInSpawn()) {
      new OngoingMatchesMenu().openMenu(player);
    }
  }
}
