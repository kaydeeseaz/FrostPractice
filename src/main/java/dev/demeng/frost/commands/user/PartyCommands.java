package dev.demeng.frost.commands.user;

import dev.demeng.frost.Frost;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PlayerState;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.Clickable;
import dev.demeng.frost.util.config.ConfigCursor;
import dev.demeng.frost.util.time.Cooldown;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"p", "party", "parties", "team"})
public class PartyCommands {

  @Dependency
  private final Frost plugin;

  private final ConfigCursor errorMessage;
  private final ConfigCursor partyMessage;

  public PartyCommands(Frost plugin) {
    this.plugin = plugin;

    errorMessage = new ConfigCursor(plugin.getMessagesConfig(), "ERROR-MESSAGES.PLAYER");
    partyMessage = new ConfigCursor(plugin.getMessagesConfig(), "MESSAGES.PARTY");
  }

  @DefaultFor({"p", "party", "parties", "team"})
  public void partyHelp(Player player) {
    for (String info : partyMessage.getStringList("HELP")) {
      CC.sendMessage(player, info);
    }
  }

  @Subcommand("info")
  public void partyInfo(Player player) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null) {
      CC.sendMessage(player, errorMessage.getString("NOT-IN-PARTY"));
    } else {
      for (String info : partyMessage.getStringList("INFO")) {
        CC.sendMessage(player,
            info.replace("<leader>", plugin.getServer().getPlayer(party.getLeader()).getName())
                .replace("<status>", CC.color(party.isOpen() ? "&aPublic" : "&cPrivate"))
                .replace("<members>", String.valueOf(party.getMembers().size()))
                .replace("<members_list>",
                    party.getMembers().stream().map(uuid -> Bukkit.getPlayer(uuid).getName())
                        .collect(Collectors.joining(", ")))
                .replace("<max>", String.valueOf(party.getLimit()))
        );
      }
    }
  }

  @Subcommand("create")
  public void createParty(Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party != null) {
      CC.sendMessage(player, errorMessage.getString("ALREADY-IN-PARTY"));
    } else if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-EXECUTE-IN-CURRENT-STATE"));
    } else {
      plugin.getManagerHandler().getPartyManager().createParty(player);
    }
  }

  @Subcommand("leave")
  public void leaveParty(Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null) {
      CC.sendMessage(player, errorMessage.getString("NOT-IN-PARTY"));
    } else if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-EXECUTE-IN-CURRENT-STATE"));
    } else {
      plugin.getManagerHandler().getPartyManager().leaveParty(player);
    }
  }

  @Subcommand("alert")
  @CommandPermission("frost.party.announce")
  public void alertParty(Player player) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null) {
      CC.sendMessage(player, errorMessage.getString("NOT-IN-PARTY"));
    } else {
      if (party.getLeader() != player.getUniqueId()) {
        for (String info : partyMessage.getStringList("HELP")) {
          CC.sendMessage(player, info);
        }
        return;
      }
      if (!party.isOpen()) {
        player.sendMessage(CC.color("&cYour party is not open! Use /party open"));
        return;
      }
      if (!practicePlayerData.getPartyAlertCooldown().hasExpired()) {
        CC.sendMessage(player, plugin.getSettingsConfig().getConfig()
            .getString("SETTINGS.GENERAL.PARTY-COOLDOWN-MESSAGE")
            .replace("<time>", practicePlayerData.getPartyAlertCooldown().getTimeMilisLeft())
            .replace("<left>", practicePlayerData.getPartyAlertCooldown().getContextLeft())
        );

        return;
      }

      practicePlayerData.setPartyAlertCooldown(new Cooldown(
          Frost.getInstance().getSettingsConfig().getConfig()
              .getInt("SETTINGS.GENERAL.PARTY-ALERT-COOLDOWN")));
      Clickable message = new Clickable(
          CC.parse(player, partyMessage.getString("ANNOUNCEMENT.MESSAGE"))
              .replace("<player>", player.getName()),
          CC.parse(player, partyMessage.getString("ANNOUNCEMENT.HOVER-OVER"))
              .replace("<player>", player.getName()),
          "/party join " + player.getName()
      );

      Bukkit.getServer().getOnlinePlayers().forEach(message::sendToPlayer);
    }
  }

  @Subcommand("open")
  @Usage("Usage: /party open")
  public void togglePartyState(Player player) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null) {
      CC.sendMessage(player, errorMessage.getString("NOT-IN-PARTY"));
    } else {
      if (party.getLeader() != player.getUniqueId()) {
        for (String info : partyMessage.getStringList("HELP")) {
          CC.sendMessage(player, info);
        }
        return;
      }

      party.setOpen(!party.isOpen());
      CC.sendMessage(player, partyMessage.getString("STATUS-UPDATED")
          .replace("<status>", CC.color(party.isOpen() ? "&aPublic" : "&cPrivate")));
    }
  }

  @Subcommand("invite")
  @Usage("Usage: /party invite <player>")
  public void invitePlayerToParty(Player player, Player target) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null) {
      CC.sendMessage(player, errorMessage.getString("NOT-IN-PARTY"));
    } else if (!plugin.getManagerHandler().getPartyManager().isLeader(player.getUniqueId())) {
      CC.sendMessage(player, errorMessage.getString("NOT-PARTY-LEADER"));
    } else if (plugin.getManagerHandler().getTournamentManager().getTournament(player.getUniqueId())
        != null) {
      CC.sendMessage(player, errorMessage.getString("IN-TOURNAMENT"));
    } else if (party.isOpen()) {
      CC.sendMessage(player, errorMessage.getString("PARTY-IS-OPEN"));
    } else if (party.getMembers().size() >= party.getLimit()) {
      CC.sendMessage(player, errorMessage.getString("PARTY-LIMIT-REACHED"));
    } else {
      if (party.getLeader() != player.getUniqueId()) {
        for (String info : partyMessage.getStringList("HELP")) {
          CC.sendMessage(player, info);
        }
        return;
      }

      PracticePlayerData targetData = plugin.getManagerHandler().getPlayerManager()
          .getPlayerData(target.getUniqueId());
      if (target.getUniqueId() == player.getUniqueId()) {
        CC.sendMessage(player, errorMessage.getString("CANNOT-INVITE-YOURSELF"));
      } else if (plugin.getManagerHandler().getPartyManager().getParty(target.getUniqueId())
          != null) {
        CC.sendMessage(player, errorMessage.getString("TARGET-ALREADY-IN-PARTY"));
      } else if (targetData.getPlayerState() != PlayerState.SPAWN) {
        CC.sendMessage(player, errorMessage.getString("CURRENTLY-BUSY"));
      } else if (!targetData.getPlayerSettings().isPartyInvites()) {
        CC.sendMessage(player, errorMessage.getString("DOES-NOT-ACCEPT-PARTY-INVITES"));
      } else if (plugin.getManagerHandler().getPartyManager()
          .hasPartyInvite(target.getUniqueId(), player.getUniqueId())) {
        CC.sendMessage(player, errorMessage.getString("ALREADY-INVITED-TO-PARTY"));
      } else {
        plugin.getManagerHandler().getPartyManager()
            .createPartyInvite(player.getUniqueId(), target.getUniqueId());

        Clickable partyInvite = new Clickable(
            CC.parse(player, partyMessage.getString("INVITE.MESSAGE"))
                .replace("<player>", player.getName()),
            CC.parse(player, partyMessage.getString("INVITE.HOVER-OVER")),
            "/party accept " + player.getName()
        );

        partyInvite.sendToPlayer(target);
        party.broadcast(partyMessage.getString("INVITED").replace("<player>", target.getName()));
      }
    }
  }

  @Subcommand("accept")
  @Usage("Usage: /party accept <player>")
  public void acceptPartyInvite(Player player, Player target) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party != null) {
      CC.sendMessage(player, errorMessage.getString("ALREADY-IN-PARTY"));
    } else if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-EXECUTE-IN-CURRENT-STATE"));
    } else {
      Party targetParty = plugin.getManagerHandler().getPartyManager()
          .getParty(target.getUniqueId());
      if (targetParty == null) {
        CC.sendMessage(player, errorMessage.getString("TARGET-NOT-IN-PARTY"));
      } else if (targetParty.getMembers().size() >= targetParty.getLimit()) {
        CC.sendMessage(player, errorMessage.getString("PARTY-LIMIT-REACHED"));
      } else if (!plugin.getManagerHandler().getPartyManager()
          .hasPartyInvite(player.getUniqueId(), targetParty.getLeader())) {
        CC.sendMessage(player, errorMessage.getString("NO-INVITES-PENDING"));
      } else {
        plugin.getManagerHandler().getPartyManager().joinParty(targetParty.getLeader(), player);
      }
    }
  }

  @Subcommand("join")
  @Usage("Usage: /party join <player>")
  public void joinParty(Player player, Player target) {
    PracticePlayerData practicePlayerData = plugin.getManagerHandler().getPlayerManager()
        .getPlayerData(player.getUniqueId());
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party != null) {
      CC.sendMessage(player, errorMessage.getString("ALREADY-IN-PARTY"));
    } else if (practicePlayerData.getPlayerState() != PlayerState.SPAWN) {
      CC.sendMessage(player, errorMessage.getString("CANNOT-EXECUTE-IN-CURRENT-STATE"));
    } else {
      Party targetParty = plugin.getManagerHandler().getPartyManager()
          .getParty(target.getUniqueId());
      if (targetParty == null || !targetParty.isOpen()
          || targetParty.getMembers().size() >= targetParty.getLimit()) {
        CC.sendMessage(player, errorMessage.getString("CANNOT-JOIN-PARTY"));
      } else {
        plugin.getManagerHandler().getPartyManager().joinParty(targetParty.getLeader(), player);
      }
    }
  }

  @Subcommand("kick")
  @Usage("Usage: /party kick <player>")
  public void kickPartyPlayer(Player player, Player target) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null) {
      CC.sendMessage(player, errorMessage.getString("NOT-IN-PARTY"));
    } else {
      if (party.getLeader() != player.getUniqueId()) {
        for (String info : partyMessage.getStringList("HELP")) {
          CC.sendMessage(player, info);
        }
        return;
      }

      Party targetParty = plugin.getManagerHandler().getPartyManager()
          .getParty(target.getUniqueId());
      if (targetParty == null || targetParty.getLeader() != party.getLeader()) {
        CC.sendMessage(player, errorMessage.getString("TARGET-NOT-IN-YOUR-PARTY"));
      } else {
        plugin.getManagerHandler().getPartyManager().leaveParty(target);
      }
    }
  }

  @Subcommand("limit")
  @CommandPermission("frost.party.limit")
  @Usage("Usage: /party limit <limit>")
  public void updatePartyLimit(Player player, String partyLimit) {
    Party party = plugin.getManagerHandler().getPartyManager().getParty(player.getUniqueId());
    if (party == null) {
      CC.sendMessage(player, errorMessage.getString("NOT-IN-PARTY"));
    } else {
      if (party.getLeader() != player.getUniqueId()) {
        for (String info : partyMessage.getStringList("HELP")) {
          CC.sendMessage(player, info);
        }
        return;
      }

      try {
        int limit = Integer.parseInt(partyLimit);
        int maxLimit = plugin.getSettingsConfig().getConfig()
            .getInt("SETTINGS.GENERAL.MAXIMUM-PARTY-SIZE");
        if (limit < 2 || limit > maxLimit) {
          player.sendMessage(CC.color("&cThe party limit must be between 2 and " + maxLimit + "."));
        } else {
          party.setLimit(limit);
          CC.sendMessage(player,
              partyMessage.getString("LIMIT-UPDATED").replace("<limit>", String.valueOf(limit)));
        }
      } catch (NumberFormatException e) {
        player.sendMessage(CC.color("&cThat is not a number."));
      }
    }
  }
}
