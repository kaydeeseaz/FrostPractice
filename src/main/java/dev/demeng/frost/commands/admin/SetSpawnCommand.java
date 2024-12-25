package dev.demeng.frost.commands.admin;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.CC;
import dev.demeng.frost.util.CustomLocation;
import java.util.List;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetSpawnCommand extends Command {

  private final Frost plugin = Frost.getInstance();

  public SetSpawnCommand() {
    super("setspawn");
  }

  private void saveLocation(Player player, String location) {
    FileConfiguration config = plugin.getMainConfig().getConfig();
    config.set(location,
        CustomLocation.locationToString(CustomLocation.fromBukkitLocation(player.getLocation())));
    plugin.getMainConfig().save();
  }

  private void saveLocations(String locationType) {
    FileConfiguration config = plugin.getMainConfig().getConfig();
    config.set(locationType, plugin.getManagerHandler().getSpawnManager()
        .fromLocations(Objects.requireNonNull(getLocationsByName(locationType))));
    plugin.getMainConfig().save();
  }

  private List<CustomLocation> getLocationsByName(String locationType) {
    switch (locationType.toLowerCase()) {
      case "lms":
        return plugin.getManagerHandler().getSpawnManager().getLmsLocations();
      case "oitc":
        return plugin.getManagerHandler().getSpawnManager().getOitcSpawnpoints();
      case "skywars":
        return plugin.getManagerHandler().getSpawnManager().getSkywarsLocations();
      case "knockout":
        return plugin.getManagerHandler().getSpawnManager().getKnockoutLocations();
      case "stoplight":
        return plugin.getManagerHandler().getSpawnManager().getStoplightLocations();
      case "droppermap":
        return plugin.getManagerHandler().getSpawnManager().getDropperMaps();
      default:
        return null;
    }
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

    if (args.length == 0) {
      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(CC.color("&b&lFrost &8- &7v" + plugin.getDescription().getVersion()));
      player.sendMessage(CC.color("&7&oSpawn Setup Help"));
      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(CC.color("&8 * &b/setSpawn spawnMin"));
      player.sendMessage(CC.color("&8 * &b/setSpawn spawnMax"));
      player.sendMessage(CC.color("&8 * &b/setSpawn spawnLocation"));
      player.sendMessage(CC.color("&8 * &b/setSpawn holoLeaderboard"));
      player.sendMessage(CC.color("&8 * &b/setSpawn winstreakHolo"));
      player.sendMessage(CC.color("&8 * &b/setSpawn ffaLocation"));
      player.sendMessage(CC.CHAT_BAR);
      player.sendMessage(CC.color("&8 * &b/setSpawn LmsHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn SumoHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn OitcHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn GulagHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn TNTTagHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn ParkourHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn DropperHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn CornersHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn SkyWarsHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn ThimbleHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn StopLightHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn KnockoutHelp"));
      player.sendMessage(CC.color("&8 * &b/setSpawn BracketsHelp"));
      player.sendMessage(CC.CHAT_BAR);

      return true;
    }

    switch (args[0].toLowerCase()) {
      case "lmshelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn lms"));
        player.sendMessage(CC.color("&8 * &b/setSpawn lmsLocation"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "sumohelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn sumoLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn sumoFirst"));
        player.sendMessage(CC.color("&8 * &b/setSpawn sumoSecond"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "oitchelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn oitc"));
        player.sendMessage(CC.color("&8 * &b/setSpawn oitcLocation"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "gulaghelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn gulagLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn gulagFirework"));
        player.sendMessage(CC.color("&8 * &b/setSpawn gulagFirst"));
        player.sendMessage(CC.color("&8 * &b/setSpawn gulagSecond"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "tnttaghelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn tntTagGameLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn tntTagLocation"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "knockouthelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn knockout"));
        player.sendMessage(CC.color("&8 * &b/setSpawn knockoutLocation"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "parkourhelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn parkourGameLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn parkourLocation"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "skywarshelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn skywars"));
        player.sendMessage(CC.color("&8 * &b/setSpawn skywarsMin"));
        player.sendMessage(CC.color("&8 * &b/setSpawn skywarsMax"));
        player.sendMessage(CC.color("&8 * &b/setSpawn skywarsLocation"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "cornershelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn cornersLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn cornersMin"));
        player.sendMessage(CC.color("&8 * &b/setSpawn cornersMax"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "thimblehelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn thimbleGameLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn thimbleLocation"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "dropperhelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn dropperLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn dropperMap"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "spleefhelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn spleefLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn spleefMin"));
        player.sendMessage(CC.color("&8 * &b/setSpawn spleefMax"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "stoplighthelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn stoplightLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn stoplight"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "bracketshelp":
        player.sendMessage(CC.CHAT_BAR);
        player.sendMessage(CC.color("&8 * &b/setSpawn bracketsLocation"));
        player.sendMessage(CC.color("&8 * &b/setSpawn bracketsFirst"));
        player.sendMessage(CC.color("&8 * &b/setSpawn bracketsSecond"));
        player.sendMessage(CC.CHAT_BAR);
        break;
      case "spawnlocation":
        plugin.getManagerHandler().getSpawnManager()
            .setSpawnLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the spawn location.");
        saveLocation(player, "spawnLocation");
        break;
      case "spawnmin":
        plugin.getManagerHandler().getSpawnManager()
            .setSpawnMin(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the spawn min.");
        saveLocation(player, "spawnMin");
        break;
      case "spawnmax":
        plugin.getManagerHandler().getSpawnManager()
            .setSpawnMax(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the spawn max.");
        saveLocation(player, "spawnMax");
        break;
      case "hololeaderboard":
        plugin.getManagerHandler().getSpawnManager()
            .setHoloLeaderboardsLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(
            ChatColor.GREEN + "Successfully set the leaderboards hologram location.");
        saveLocation(player, "holoLeaderboardsLocation");
        break;
      case "winstreakholo":
        plugin.getManagerHandler().getSpawnManager()
            .setHoloWinstreakLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the winstreak hologram location.");
        saveLocation(player, "winstreakHoloLocation");
        break;
      case "ffalocation":
        plugin.getManagerHandler().getSpawnManager()
            .setFfaLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the FFA location.");
        saveLocation(player, "ffaLocation");
        break;
      case "gulaglocation":
        plugin.getManagerHandler().getSpawnManager()
            .setGulagLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Gulag location.");
        saveLocation(player, "gulagLocation");
        break;
      case "gulagfirework":
        plugin.getManagerHandler().getSpawnManager()
            .setGulagFirework(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Gulag firework location.");
        saveLocation(player, "gulagFirework");
        break;
      case "gulagfirst":
        plugin.getManagerHandler().getSpawnManager()
            .setGulagFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Gulag location A.");
        saveLocation(player, "gulagFirst");
        break;
      case "gulagsecond":
        plugin.getManagerHandler().getSpawnManager()
            .setGulagSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Gulag location B.");
        saveLocation(player, "gulagSecond");
        break;
      case "bracketslocation":
        plugin.getManagerHandler().getSpawnManager()
            .setBracketsLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Brackets location.");
        saveLocation(player, "bracketsLocation");
        break;
      case "bracketsfirst":
        plugin.getManagerHandler().getSpawnManager()
            .setBracketsFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Brackets location A.");
        saveLocation(player, "bracketsFirst");
        break;
      case "bracketssecond":
        plugin.getManagerHandler().getSpawnManager()
            .setBracketsSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Brackets location B.");
        saveLocation(player, "bracketsSecond");
        break;
      case "cornerslocation":
        plugin.getManagerHandler().getSpawnManager()
            .setCornersLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the 4Corners location.");
        saveLocation(player, "cornersLocation");
        break;
      case "cornersmin":
        plugin.getManagerHandler().getSpawnManager()
            .setCornersMin(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the 4Corners min.");
        saveLocation(player, "cornersMin");
        break;
      case "cornersmax":
        plugin.getManagerHandler().getSpawnManager()
            .setCornersMax(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the 4Corners max.");
        saveLocation(player, "cornersMax");
        break;
      case "thimblegamelocation":
        plugin.getManagerHandler().getSpawnManager()
            .setThimbleGameLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Thimble location.");
        saveLocation(player, "thimbleGameLocation");
        break;
      case "thimblelocation":
        plugin.getManagerHandler().getSpawnManager()
            .setThimbleLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Thimble location.");
        saveLocation(player, "thimbleLocation");
        break;
      case "sumolocation":
        plugin.getManagerHandler().getSpawnManager()
            .setSumoLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Sumo location.");
        saveLocation(player, "sumoLocation");
        break;
      case "sumofirst":
        plugin.getManagerHandler().getSpawnManager()
            .setSumoFirst(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Sumo location A.");
        saveLocation(player, "sumoFirst");
        break;
      case "sumosecond":
        plugin.getManagerHandler().getSpawnManager()
            .setSumoSecond(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Sumo location B.");
        saveLocation(player, "sumoSecond");
        break;
      case "droppermap":
        plugin.getManagerHandler().getSpawnManager().getDropperMaps()
            .add(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(
            ChatColor.GREEN + "Successfully set #" + plugin.getManagerHandler().getSpawnManager()
                .getDropperMaps().size() + " Dropper map.");
        saveLocations("droppermap");
        break;
      case "dropperlocation":
        plugin.getManagerHandler().getSpawnManager()
            .setDropperLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Dropper Waiting location.");
        saveLocation(player, "dropperLocation");
        break;
      case "oitc":
        plugin.getManagerHandler().getSpawnManager().getOitcSpawnpoints()
            .add(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(
            ChatColor.GREEN + "Successfully set the OITC spawn-point #" + plugin.getManagerHandler()
                .getSpawnManager().getOitcSpawnpoints().size() + ".");
        saveLocations("oitc");
        break;
      case "oitclocation":
        plugin.getManagerHandler().getSpawnManager()
            .setOitcLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the OITC location.");
        saveLocation(player, "oitcLocation");
        break;
      case "parkourlocation":
        plugin.getManagerHandler().getSpawnManager()
            .setParkourLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Parkour location.");
        saveLocation(player, "parkourLocation");
        break;
      case "parkourgamelocation":
        plugin.getManagerHandler().getSpawnManager()
            .setParkourGameLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Parkour Game location.");
        saveLocation(player, "parkourGameLocation");
        break;
      case "lmslocation":
        plugin.getManagerHandler().getSpawnManager()
            .setLmsLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the LMS location.");
        saveLocation(player, "lmsLocation");
        break;
      case "lms":
        plugin.getManagerHandler().getSpawnManager().getLmsLocations()
            .add(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(
            ChatColor.GREEN + "Successfully set the LMS spawn-point #" + plugin.getManagerHandler()
                .getSpawnManager().getLmsLocations().size() + ".");
        saveLocations("lms");
        break;
      case "knockoutlocation":
        plugin.getManagerHandler().getSpawnManager()
            .setKnockoutLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Knockout location.");
        saveLocation(player, "knockoutLocation");
        break;
      case "knockout":
        plugin.getManagerHandler().getSpawnManager().getKnockoutLocations()
            .add(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the Knockout spawn-point #"
            + plugin.getManagerHandler().getSpawnManager().getKnockoutLocations().size() + ".");
        saveLocations("knockout");
        break;
      case "skywarsmin":
        plugin.getManagerHandler().getSpawnManager()
            .setSkywarsMin(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the skywars min location.");
        saveLocation(player, "skywarsMin");
        break;
      case "skywarsmax":
        plugin.getManagerHandler().getSpawnManager()
            .setSkywarsMax(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the skywars max location.");
        saveLocation(player, "skywarsMax");
        break;
      case "skywarslocation":
        plugin.getManagerHandler().getSpawnManager()
            .setSkywarsLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the skywars location.");
        saveLocation(player, "skywarsLocation");
        break;
      case "skywars":
        plugin.getManagerHandler().getSpawnManager().getSkywarsLocations()
            .add(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the SkyWars spawn-point #"
            + plugin.getManagerHandler().getSpawnManager().getSkywarsLocations().size() + ".");
        saveLocations("skywars");
        break;
      case "stoplightlocation":
        plugin.getManagerHandler().getSpawnManager()
            .setStoplightLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the StopLight location.");
        saveLocation(player, "stoplightLocation");
        break;
      case "stoplight":
        plugin.getManagerHandler().getSpawnManager().getStoplightLocations()
            .add(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the StopLight spawn-point #"
            + plugin.getManagerHandler().getSpawnManager().getStoplightLocations().size() + ".");
        saveLocations("stoplight");
        break;
      case "spleeflocation":
        plugin.getManagerHandler().getSpawnManager()
            .setSpleefLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the spleef location.");
        saveLocation(player, "spleefLocation");
        break;
      case "spleefmin":
        plugin.getManagerHandler().getSpawnManager()
            .setSpleefMin(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the spleef min location.");
        saveLocation(player, "spleefMin");
        break;
      case "spleefmax":
        plugin.getManagerHandler().getSpawnManager()
            .setSpleefMax(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the spleef max location.");
        saveLocation(player, "spleefMax");
        break;
      case "tnttaglocation":
        plugin.getManagerHandler().getSpawnManager()
            .setTntTagLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the tnt tag location.");
        saveLocation(player, "tntTagLocation");
        break;
      case "tnttaggamelocation":
        plugin.getManagerHandler().getSpawnManager()
            .setTntTagGameLocation(CustomLocation.fromBukkitLocation(player.getLocation()));
        player.sendMessage(ChatColor.GREEN + "Successfully set the tnt tag game location.");
        saveLocation(player, "tntTagGameLocation");
        break;
    }

    return false;
  }
}
