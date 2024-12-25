package dev.demeng.frost.util;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.team.KillableTeam;
import dev.demeng.frost.game.tournament.TournamentTeam;
import java.util.UUID;
import org.bukkit.entity.Player;

public class TeamUtil {

  public static String getNames(KillableTeam team) {
    String names = "";

    for (int i = 0; i < team.getPlayers().size(); i++) {
      UUID teammateUUID = team.getPlayers().get(i);
      Player teammate = Frost.getInstance().getServer().getPlayer(teammateUUID);
      String name = "";

      if (teammate == null) {
        if (team instanceof TournamentTeam) {
          name = ((TournamentTeam) team).getPlayerName(teammateUUID);
        }
      } else {
        name = teammate.getName();
      }

      int players = team.getPlayers().size();

      if (teammate != null) {
        names += name + (((players - 1) == i) ? ""
            : ((players - 2) == i) ? (players > 2 ? "," : "") + " & " : ", ");
      }
    }

    return names;
  }
}
