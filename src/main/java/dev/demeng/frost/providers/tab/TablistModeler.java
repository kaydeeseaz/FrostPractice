package dev.demeng.frost.providers.tab;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.game.match.Match;
import dev.demeng.frost.user.party.Party;
import dev.demeng.frost.user.player.PracticePlayerData;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.config.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TablistModeler {

  private final Frost plugin = Frost.getInstance();
  private final FileConfig tabListConfig = plugin.getTablistConfig();

  private boolean isKitFormat(String string) {
    return string.contains("<kit_");
  }

  private boolean isPartyFormat(String string) {
    return string.contains("<party_");
  }

  private boolean isPartyFFA(String string) {
    return string.contains("<opponent_");
  }

  private boolean isOpponentSplit(String string) {
    return string.contains("<opponent_");
  }

  private boolean isTeamSplit(String string) {
    return string.contains("<team_");
  }

  public int getColumn(String string) {
    int i = 0;
    switch (string.toUpperCase()) {
      case "LEFT":
        break;
      case "MIDDLE":
        i = 1;
        break;
      case "RIGHT":
        i = 2;
        break;
      case "FAR-RIGHT":
        i = 3;
        break;
    }

    return i;
  }

  private String getKitFormat(String string, PracticePlayerData practicePlayerData) {
    String format = tabListConfig.getConfig().getString("TABLIST.KIT-FORMAT");
    String text = " ";
    String[] split = string.replace(">", "").replace("<", "").split("_");
    Kit kit = plugin.getManagerHandler().getKitManager().getKit(split[1]);
    if (kit == null) {
      text = "Unknown kit";
    } else {
      text = format.replace("<name>", kit.getName())
          .replace("<elo>", String.valueOf(practicePlayerData.getElo(kit.getName())));
    }

    return CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
  }

  private String getPartyMember(String string, PracticePlayerData practicePlayerData) {
    Party party = plugin.getManagerHandler().getPartyManager()
        .getParty(practicePlayerData.getUniqueId());
    if (party == null) {
      return "Unknown";
    }

    String text = " ";
    String[] split = string.replace(">", "").replace("<", "").split("_");
    if (split[1].replace("*", "").equalsIgnoreCase("leader")) {
      return CC.color(
          Bukkit.getPlayer(party.getLeader()).getName() + (split[1].contains("*") ? "*" : ""));
    }

    int member = Integer.parseInt(split[2]);
    if (party.getPartyMembersExcludeLeader().size() >= (member)) {
      text = Bukkit.getPlayer(party.getPartyMembersExcludeLeader().get(member - 1)).getName();
    } else {
      text = " ";
    }

    return CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
  }

  private String getTeamSplit(String string, PracticePlayerData practicePlayerData, Party party) {
    if (practicePlayerData == null) {
      return "null pd";
    }

    if (party == null) {
      return "Unknown";
    }

    String text = " ";
    String[] split = string.replace(">", "").replace("<", "").split("_");
    if (split[1].equalsIgnoreCase("your")) {
      return Bukkit.getPlayer(practicePlayerData.getUniqueId()).getName();
    }

    int member = Integer.parseInt(split[1]);
    if (party.getPartySplitTeam(party.getLeader()).size() >= (member)) {
      text = string;
      text = text
          .replace("<opponent_" + member + ">", Bukkit.getPlayer(
              party.getPartySplitTeam(practicePlayerData.getUniqueId()).get(member - 1)) == null
              ? "Null" : Bukkit.getPlayer(
              party.getPartySplitTeam(practicePlayerData.getUniqueId()).get(member - 1)).getName())

          .replace("<team_" + member + ">", Bukkit.getPlayer(
              party.getPartySplitTeam(practicePlayerData.getUniqueId()).get(member - 1)) == null
              ? "Null" : Bukkit.getPlayer(
              party.getPartySplitTeam(practicePlayerData.getUniqueId()).get(member - 1)).getName())
      ;
    } else {
      text = " ";
    }

    return CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
  }

  private String getTeamOpponent(String string, PracticePlayerData practicePlayerData,
      Party party) {
    if (party == null) {
      return "Unknown";
    }

    String text = " ";
    String[] split = string.replace(">", "").replace("<", "").split("_");

    int member = Integer.parseInt(split[1]);
    if (party.findOpponent(practicePlayerData.getUniqueId()).getAlivePlayers().size() >= (member)) {
      text = string;
      text = text.replace("<opponent_" + member + ">", Bukkit.getPlayer(
              party.findOpponent(practicePlayerData.getUniqueId()).getAlivePlayers().get(member - 1))
          .getName());
    } else {
      text = " ";
    }

    return CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
  }

  private String getTeamOpponents(String string, PracticePlayerData practicePlayerData,
      Party party) {
    if (party == null) {
      return "Unknown";
    }

    String text = " ";
    String[] split = string.replace(">", "").replace("<", "").split("_");

    int member = Integer.parseInt(split[1]);

    if (party.getPartyMembersExcludeMember(practicePlayerData.getUniqueId()).size() >= (member)) {
      text = string;
      text = text.replace("<opponent_" + member + ">", Bukkit.getPlayer(
              party.getPartyMembersExcludeMember(practicePlayerData.getUniqueId()).get(member - 1))
          .getName());
    } else {
      text = " ";
    }

    return CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
  }

  public String spawnTranslate(String string, PracticePlayerData practicePlayerData) {
    String text = string;
    text = CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
    text = text
        .replace("<players>", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()))
        .replace("<queued>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))
    ;

    if (isPartyFormat(text)) {
      return getPartyMember(text, practicePlayerData);
    }
    if (isKitFormat(text)) {
      return getKitFormat(text, practicePlayerData);
    }

    return text;
  }

  public String matchTranslate(String string, PracticePlayerData practicePlayerData, Match match) {
    Player opponentPlayer =
        match.getTeams().get(0).getPlayers().get(0) == practicePlayerData.getUniqueId()
            ? plugin.getServer().getPlayer(match.getTeams().get(1).getPlayers().get(0))
            : plugin.getServer().getPlayer(match.getTeams().get(0).getPlayers().get(0));

    String text = string;
    text = CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
    text = text
        .replace("<players>", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()))
        .replace("<queued>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))

        .replace("<your_player>",
            Bukkit.getPlayer(practicePlayerData.getUniqueId()) == null ? "Null"
                : Bukkit.getPlayer(practicePlayerData.getUniqueId()).getName())
        .replace("<your_enemy>", opponentPlayer == null ? "None" : opponentPlayer.getName())
        .replace("<arena>", match.getArena().getName())
        .replace("<kit>", match.getKit().getName())
        .replace("<duration>", String.valueOf(match.getDuration()))
    ;

    return text;
  }

  public String partyTranslate(String string, PracticePlayerData practicePlayerData, Match match,
      boolean split) {
    Party party = plugin.getManagerHandler().getPartyManager()
        .getParty(practicePlayerData.getUniqueId());
    String text = string;
    text = CC.parse(Bukkit.getPlayer(practicePlayerData.getUniqueId()), text);
    text = text
        .replace("<players>", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()))
        .replace("<queued>",
            String.valueOf(plugin.getManagerHandler().getQueueManager().getQueueSize()))
        .replace("<fighting>",
            String.valueOf(plugin.getManagerHandler().getMatchManager().getFighters()))

        .replace("<arena>", match.getArena().getName())
        .replace("<kit>", match.getKit().getName())
        .replace("<duration>", String.valueOf(match.getDuration()))
    ;

    if (split) {
      if (isTeamSplit(string)) {
        return getTeamSplit(text, practicePlayerData, party);
      }
      if (isOpponentSplit(string)) {
        return getTeamOpponent(string, practicePlayerData, party);
      }
    } else {
      text = text.replace("<team_your>",
          Bukkit.getPlayer(practicePlayerData.getUniqueId()).getName());
      if (isPartyFFA(string)) {
        return getTeamOpponents(text, practicePlayerData, party);
      }
    }

    return text;
  }
}
