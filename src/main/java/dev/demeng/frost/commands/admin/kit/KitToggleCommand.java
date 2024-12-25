package dev.demeng.frost.commands.admin.kit;

import dev.demeng.frost.Frost;
import dev.demeng.frost.game.kit.Kit;
import dev.demeng.frost.util.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("kit toggle")
@CommandPermission("frost.admin")
public class KitToggleCommand {

  @Dependency private Frost plugin;
  private final String NO_KIT = CC.color("&4&lERROR&4! &cThat kit doesn't exist!");

  @DefaultFor("kit toggle")
  public void getHelp(Player player) {
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()
        + " &8- &fKit Toggle - Command Help"));
    player.sendMessage(CC.CHAT_BAR);
    player.sendMessage(CC.color("&a • &b/kit toggle playable <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle ranked <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle build <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle combo <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle sumo <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle skywars <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle bridges <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle spleef <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle boxing <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle bedwars <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle mlgrush <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle battlerush <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle stickfight <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle nofall <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle noregen <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle nohunger <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle showhealth <kitName>"));
    player.sendMessage(CC.color("&a • &b/kit toggle spawnffa <kitName>"));
    player.sendMessage(CC.CHAT_BAR);
  }

  @Subcommand("playable")
  public void setPlayable(Player player, Kit kit) {
    if (kit != null) {
      kit.setEnabled(!kit.isEnabled());
      player.sendMessage(CC.color(kit.isEnabled()
          ? "&aSuccessfully updated " + kit.getName() + " to be playable."
          : "&cSuccessfully updated " + kit.getName() + " to be unplayable."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("ranked")
  public void setRanked(Player player, Kit kit) {
    if (kit != null) {
      kit.setRanked(!kit.isRanked());
      player.sendMessage(CC.color(kit.isRanked()
          ? "&aSuccessfully enabled ranked for kit " + kit.getName() + "."
          : "&cSuccessfully disabled ranked for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("combo")
  public void setCombo(Player player, Kit kit) {
    if (kit != null) {
      kit.setCombo(!kit.isCombo());
      player.sendMessage(CC.color(kit.isCombo()
          ? "&aSuccessfully enabled combo mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled combo mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("sumo")
  public void setSumo(Player player, Kit kit) {
    if (kit != null) {
      kit.setSumo(!kit.isSumo());
      player.sendMessage(CC.color(kit.isSumo()
          ? "&aSuccessfully enabled Sumo mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled Sumo mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("skywars")
  public void setSkyWars(Player player, Kit kit) {
    if (kit != null) {
      kit.setSkyWars(!kit.isSkyWars());
      player.sendMessage(CC.color(kit.isSkyWars()
          ? "&aSuccessfully enabled SkyWars mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled SkyWars mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("build")
  public void setBuild(Player player, Kit kit) {
    if (kit != null) {
      kit.setBuild(!kit.isBuild());
      player.sendMessage(CC.color(kit.isBuild()
          ? "&aSuccessfully enabled Build mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled Build mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("bridges")
  public void setBridges(Player player, Kit kit) {
    if (kit != null) {
      kit.setBridges(!kit.isBridges());
      player.sendMessage(CC.color(kit.isBridges()
          ? "&aSuccessfully enabled Bridges mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled Bridges mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("spleef")
  public void setSpleef(Player player, Kit kit) {
    if (kit != null) {
      kit.setSpleef(!kit.isSpleef());
      player.sendMessage(CC.color(kit.isSpleef()
          ? "&aSuccessfully enabled Spleef mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled Spleef mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("boxing")
  public void setBoxing(Player player, Kit kit) {
    if (kit != null) {
      kit.setBoxing(!kit.isBoxing());
      player.sendMessage(CC.color(kit.isBoxing()
          ? "&aSuccessfully enabled Boxing mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled Boxing mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("bedwars")
  public void setBedWars(Player player, Kit kit) {
    if (kit != null) {
      kit.setBedWars(!kit.isBedWars());
      player.sendMessage(CC.color(kit.isBedWars()
          ? "&aSuccessfully enabled BedWars mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled BedWars mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("battlerush")
  public void setBattleRush(Player player, Kit kit) {
    if (kit != null) {
      kit.setBattleRush(!kit.isBattleRush());
      player.sendMessage(CC.color(kit.isBattleRush()
          ? "&aSuccessfully enabled BattleRush mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled BattleRush mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("stickfight")
  public void setStickFight(Player player, Kit kit) {
    if (kit != null) {
      kit.setStickFight(!kit.isStickFight());
      player.sendMessage(CC.color(kit.isStickFight()
          ? "&aSuccessfully enabled StickFight mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled StickFight mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("nohunger")
  public void setNoHunger(Player player, Kit kit) {
    if (kit != null) {
      kit.setNoHunger(!kit.isNoHunger());
      player.sendMessage(CC.color(kit.isNoHunger()
          ? "&aSuccessfully enabled food consumption for kit " + kit.getName() + "."
          : "&cSuccessfully disabled food consumption for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("noregen")
  public void setNoRegen(Player player, Kit kit) {
    if (kit != null) {
      kit.setNoRegen(!kit.isNoRegen());
      player.sendMessage(CC.color(kit.isNoRegen()
          ? "&aSuccessfully enabled natural regen for kit " + kit.getName() + "."
          : "&cSuccessfully disabled natural regen for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("nofall")
  public void setNoFall(Player player, Kit kit) {
    if (kit != null) {
      kit.setNoFall(!kit.isNoFall());
      player.sendMessage(CC.color(kit.isNoFall()
          ? "&aSuccessfully disabled fall damage for kit " + kit.getName() + "."
          : "&cSuccessfully enabled fall damage for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("mlgrush")
  public void setMlgRush(Player player, Kit kit) {
    if (kit != null) {
      kit.setMlgRush(!kit.isMlgRush());
      player.sendMessage(CC.color(kit.isMlgRush()
          ? "&aSuccessfully enabled MLGRush mode for kit " + kit.getName() + "."
          : "&cSuccessfully disabled MLGRush mode for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("showhealth")
  public void setShowHealth(Player player, Kit kit) {
    if (kit != null) {
      kit.setShowHealth(!kit.isShowHealth());
      player.sendMessage(CC.color(kit.isShowHealth()
          ? "&aSuccessfully showing player health for kit " + kit.getName() + "."
          : "&cSuccessfully hiding player health for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }

  @Subcommand("spawnffa")
  public void setSpawnFfa(Player player, Kit kit) {
    if (kit != null) {
      kit.setAllowSpawnFfa(!kit.isAllowSpawnFfa());
      player.sendMessage(CC.color(kit.isAllowSpawnFfa()
          ? "&aSuccessfully allowed spawn ffa for kit " + kit.getName() + "."
          : "&cSuccessfully disabled spawn ffa for kit " + kit.getName() + "."
      ));
    } else {
      player.sendMessage(NO_KIT);
    }
  }
}
