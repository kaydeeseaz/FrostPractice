package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.player.PlayerSettings;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.user.ui.settings.SettingsMenu;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.time.Cooldown;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class SettingsCommands {

  @Dependency private final Frost plugin;
  private final ConfigCursor cursor;

  public SettingsCommands(Frost plugin) {
    this.plugin = plugin;
    this.cursor = new ConfigCursor(plugin.getMessagesConfig(), "MESSAGES.PLAYER.SETTINGS");
  }

  @Command({"playersettings", "practicesettings", "psettings", "playsettings", "setting",
      "settings"})
  public void getSettingsMenu(Player player) {
    new SettingsMenu().openMenu(player);
  }

  @Command("toggleduelrequests")
  public void toggleDuels(Player player, PlayerSettings settings) {
    settings.setDuelRequests(!settings.isDuelRequests());
    player.sendMessage(CC.parse(player,
        settings.isDuelRequests() ? cursor.getString("DUELS-ENABLED")
            : cursor.getString("DUELS-DISABLED")));
  }

  @Command("togglepartyinvites")
  public void toggleInvites(Player player, PlayerSettings settings) {
    settings.setPartyInvites(!settings.isPartyInvites());
    player.sendMessage(CC.parse(player,
        settings.isPartyInvites() ? cursor.getString("INVITES-ENABLED")
            : cursor.getString("INVITES-DISABLED")));
  }

  @Command("toggleflightstart")
  public void toggleFlight(Player player, PlayerSettings settings) {
    settings.setStartFlying(!settings.isStartFlying());
    player.sendMessage(CC.parse(player, settings.isStartFlying() ? cursor.getString("FLIGHT-START")
        : cursor.getString("FLIGHT-STOP")));
  }

  @Command("toggleinventoryclear")
  public void toggleInventoryClear(Player player, PlayerSettings settings) {
    settings.setClearInventory(!settings.isClearInventory());
    player.sendMessage(CC.parse(player,
        settings.isClearInventory() ? cursor.getString("INV-CLEAR-ENABLED")
            : cursor.getString("INV-CLEAR-DISABLED")));
  }

  @Command("togglebodyanimation")
  public void toggleBodyAnimation(Player player, PlayerSettings settings) {
    settings.setBodyAnimation(!settings.isBodyAnimation());
    player.sendMessage(CC.parse(player,
        settings.isBodyAnimation() ? cursor.getString("BODY-ANIMATION-ENABLED")
            : cursor.getString("BODY-ANIMATION-DISABLED")));
  }

  @Command({"toggleplayervisibility", "toggleplayers", "tpv"})
  @CommandPermission("frost.user.spawn-visibility")
  public void toggleSpawnVisibility(Player player, PracticePlayerData practicePlayerData,
      PlayerSettings settings) {
    if (!practicePlayerData.getPlayerCommandCooldown().hasExpired()) {
      player.sendMessage(CC.parse(player, plugin.getSettingsConfig().getConfig()
          .getString("SETTINGS.GENERAL.TOGGLE-PLAYERS-COOLDOWN-MESSAGE")
          .replace("<time>", practicePlayerData.getPlayerCommandCooldown().getTimeMilisLeft())
          .replace("<left>", practicePlayerData.getPlayerCommandCooldown().getContextLeft()))
      );

      return;
    }
    if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      player.sendMessage(CC.parse(player, plugin.getMessagesConfig().getConfig()
          .getString("ERROR-MESSAGES.PLAYER.CANNOT-EXECUTE-IN-CURRENT-STATE")));
      return;
    }

    practicePlayerData.setPlayerCommandCooldown(new Cooldown(
        plugin.getSettingsConfig().getConfig().getInt("SETTINGS.GENERAL.TOGGLE-PLAYERS-COOLDOWN")));
    settings.setPlayerVisibility(!settings.isPlayerVisibility());
    player.sendMessage(CC.parse(player, settings.isPlayerVisibility()
        ? cursor.getString("PLAYERS-NOW-SHOWN")
        : cursor.getString("PLAYERS-NOW-HIDDEN")
    ));

    plugin.getManagerHandler().getPlayerManager().updatePlayerView();
  }

  @Command("togglescoreboard")
  public void toggleScoreboard(Player player, PlayerSettings settings) {
    settings.setScoreboardToggled(!settings.isScoreboardToggled());
    player.sendMessage(CC.parse(player,
        settings.isScoreboardToggled() ? cursor.getString("SCOREBOARD-ENABLED")
            : cursor.getString("SCOREBOARD-DISABLED")));
  }

  @Command("togglepingscoreboard")
  public void toggleScoreboardPing(Player player, PlayerSettings settings) {
    settings.setPingScoreboardToggled(!settings.isPingScoreboardToggled());
    player.sendMessage(CC.parse(player,
        settings.isPingScoreboardToggled() ? cursor.getString("PING-ENABLED")
            : cursor.getString("PING-DISABLED")));
  }

  @Command("togglevanillatab")
  public void toggleTablist(Player player, PlayerSettings settings) {
    settings.setVanillaTab(!settings.isVanillaTab());
    player.sendMessage(CC.parse(player, settings.isVanillaTab() ? cursor.getString("TAB-ENABLED")
        : cursor.getString("TAB-DISABLED")));
  }

  @Command("togglespectators")
  public void toggleSpectators(Player player, PlayerSettings settings) {
    settings.setSpectatorsAllowed(!settings.isSpectatorsAllowed());
    player.sendMessage(CC.parse(player,
        settings.isSpectatorsAllowed() ? cursor.getString("SPECTATORS-ENABLED")
            : cursor.getString("SPECTATORS-DISABLED")));
  }
}
