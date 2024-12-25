package dev.demeng.frost.managers;

import dev.demeng.frost.Frost;
import dev.demeng.frost.util.CustomLocation;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
@Setter
public class SpawnManager {

  private final Frost plugin = Frost.getInstance();

  private CustomLocation spawnLocation;
  private CustomLocation spawnMin;
  private CustomLocation spawnMax;

  private CustomLocation sumoLocation;
  private CustomLocation sumoFirst;
  private CustomLocation sumoSecond;

  private CustomLocation bracketsLocation;
  private CustomLocation bracketsFirst;
  private CustomLocation bracketsSecond;

  private CustomLocation gulagLocation;
  private CustomLocation gulagFirework;
  private CustomLocation gulagFirst;
  private CustomLocation gulagSecond;

  private List<CustomLocation> lmsLocations;
  private CustomLocation lmsLocation;

  private List<CustomLocation> knockoutLocations;
  private CustomLocation knockoutLocation;

  private List<CustomLocation> skywarsLocations;
  private CustomLocation skywarsLocation;
  private CustomLocation skywarsMin;
  private CustomLocation skywarsMax;

  private List<CustomLocation> dropperMaps;
  private CustomLocation dropperLocation;

  private CustomLocation spleefLocation;
  private CustomLocation spleefMin;
  private CustomLocation spleefMax;

  private CustomLocation tntTagLocation;
  private CustomLocation tntTagGameLocation;

  private CustomLocation cornersLocation;
  private CustomLocation cornersMin;
  private CustomLocation cornersMax;

  private CustomLocation thimbleGameLocation;
  private CustomLocation thimbleLocation;

  private List<CustomLocation> stoplightLocations;
  private CustomLocation stoplightLocation;

  private List<CustomLocation> oitcSpawnpoints;
  private CustomLocation oitcLocation;

  private CustomLocation parkourLocation;
  private CustomLocation parkourGameLocation;

  private CustomLocation holoLeaderboardsLocation;
  private CustomLocation holoWinstreakLocation;

  private CustomLocation ffaLocation;

  public SpawnManager() {
    this.dropperMaps = new ArrayList<>();
    this.lmsLocations = new ArrayList<>();
    this.oitcSpawnpoints = new ArrayList<>();
    this.skywarsLocations = new ArrayList<>();
    this.knockoutLocations = new ArrayList<>();
    this.stoplightLocations = new ArrayList<>();

    this.loadConfig();
  }

  private void loadConfig() {
    FileConfiguration config = plugin.getMainConfig().getConfig();

    if (config.contains("spawnLocation")) {
      try {
        this.spawnLocation = CustomLocation.stringToLocation(config.getString("spawnLocation"));
        this.spawnMin = CustomLocation.stringToLocation(config.getString("spawnMin"));
        this.spawnMax = CustomLocation.stringToLocation(config.getString("spawnMax"));
      } catch (NullPointerException e) {
        plugin.getLogger().info("spawnMin and spawnMax locations not found!");
      }
    }

    if (config.contains("cornersLocation")) {
      this.cornersLocation = CustomLocation.stringToLocation(config.getString("cornersLocation"));
      this.cornersMin = CustomLocation.stringToLocation(config.getString("cornersMin"));
      this.cornersMax = CustomLocation.stringToLocation(config.getString("cornersMax"));
    }

    if (config.contains("thimbleLocation")) {
      this.thimbleGameLocation = CustomLocation.stringToLocation(
          config.getString("thimbleGameLocation"));
      this.thimbleLocation = CustomLocation.stringToLocation(config.getString("thimbleLocation"));
    }

    if (config.contains("sumoLocation")) {
      this.sumoLocation = CustomLocation.stringToLocation(config.getString("sumoLocation"));
      this.sumoFirst = CustomLocation.stringToLocation(config.getString("sumoFirst"));
      this.sumoSecond = CustomLocation.stringToLocation(config.getString("sumoSecond"));
    }

    if (config.contains("bracketsLocation")) {
      this.bracketsLocation = CustomLocation.stringToLocation(config.getString("bracketsLocation"));
      this.bracketsFirst = CustomLocation.stringToLocation(config.getString("bracketsFirst"));
      this.bracketsSecond = CustomLocation.stringToLocation(config.getString("bracketsSecond"));
    }

    if (config.contains("gulagLocation")) {
      this.gulagLocation = CustomLocation.stringToLocation(config.getString("gulagLocation"));
      this.gulagFirework = CustomLocation.stringToLocation(config.getString("gulagFirework"));
      this.gulagFirst = CustomLocation.stringToLocation(config.getString("gulagFirst"));
      this.gulagSecond = CustomLocation.stringToLocation(config.getString("gulagSecond"));
    }

    if (config.contains("oitcLocation")) {
      this.oitcLocation = CustomLocation.stringToLocation(config.getString("oitcLocation"));

      for (String spawnpoint : config.getStringList("oitc")) {
        this.oitcSpawnpoints.add(CustomLocation.stringToLocation(spawnpoint));
      }
    }

    if (config.contains("dropperLocation")) {
      this.dropperLocation = CustomLocation.stringToLocation(config.getString("dropperLocation"));

      for (String map : config.getStringList("dropperMaps")) {
        this.dropperMaps.add(CustomLocation.stringToLocation(map));
      }
    }

    if (config.contains("lmsLocation")) {
      this.lmsLocation = CustomLocation.stringToLocation(config.getString("lmsLocation"));

      for (String spawnpoint : config.getStringList("lms")) {
        this.lmsLocations.add(CustomLocation.stringToLocation(spawnpoint));
      }
    }

    if (config.contains("knockoutLocation")) {
      this.knockoutLocation = CustomLocation.stringToLocation(config.getString("knockoutLocation"));

      for (String spawnpoint : config.getStringList("knockout")) {
        this.knockoutLocations.add(CustomLocation.stringToLocation(spawnpoint));
      }
    }

    if (config.contains("skywarsLocation")) {
      this.skywarsLocation = CustomLocation.stringToLocation(config.getString("skywarsLocation"));
      this.skywarsMin = CustomLocation.stringToLocation(config.getString("skywarsMin"));
      this.skywarsMax = CustomLocation.stringToLocation(config.getString("skywarsMax"));

      for (String spawnpoint : config.getStringList("skywars")) {
        this.skywarsLocations.add(CustomLocation.stringToLocation(spawnpoint));
      }
    }

    if (config.contains("stoplightLocation")) {
      this.stoplightLocation = CustomLocation.stringToLocation(
          config.getString("stoplightLocation"));

      for (String spawnpoint : config.getStringList("stoplight")) {
        this.stoplightLocations.add(CustomLocation.stringToLocation(spawnpoint));
      }
    }

    if (config.contains("spleefLocation")) {
      this.spleefLocation = CustomLocation.stringToLocation(config.getString("spleefLocation"));
      this.spleefMin = CustomLocation.stringToLocation(config.getString("spleefMin"));
      this.spleefMax = CustomLocation.stringToLocation(config.getString("spleefMax"));
    }

    if (config.contains("tntTagLocation")) {
      this.tntTagLocation = CustomLocation.stringToLocation(config.getString("tntTagLocation"));
      this.tntTagGameLocation = CustomLocation.stringToLocation(
          config.getString("tntTagGameLocation"));
    }

    if (config.contains("parkourLocation")) {
      this.parkourLocation = CustomLocation.stringToLocation(config.getString("parkourLocation"));
      this.parkourGameLocation = CustomLocation.stringToLocation(
          config.getString("parkourGameLocation"));
    }

    if (config.contains("holoLeaderboardsLocation")) {
      this.holoLeaderboardsLocation = CustomLocation.stringToLocation(
          config.getString("holoLeaderboardsLocation"));
    }

    if (config.contains("winstreakHoloLocation")) {
      this.holoWinstreakLocation = CustomLocation.stringToLocation(
          config.getString("winstreakHoloLocation"));
    }

    if (config.contains("ffaLocation")) {
      this.ffaLocation = CustomLocation.stringToLocation(config.getString("ffaLocation"));
    }
  }

  public void saveConfig() {
    FileConfiguration config = plugin.getMainConfig().getConfig();
    config.set("spawnLocation", CustomLocation.locationToString(this.spawnLocation));
    config.set("spawnMin", CustomLocation.locationToString(this.spawnMin));
    config.set("spawnMax", CustomLocation.locationToString(this.spawnMax));

    config.set("cornersLocation", CustomLocation.locationToString(this.cornersLocation));
    config.set("cornersMin", CustomLocation.locationToString(this.cornersMin));
    config.set("cornersMax", CustomLocation.locationToString(this.cornersMax));

    config.set("thimbleGameLocation", CustomLocation.locationToString(this.thimbleGameLocation));
    config.set("thimbleLocation", CustomLocation.locationToString(this.thimbleLocation));

    config.set("sumoLocation", CustomLocation.locationToString(this.sumoLocation));
    config.set("sumoFirst", CustomLocation.locationToString(this.sumoFirst));
    config.set("sumoSecond", CustomLocation.locationToString(this.sumoSecond));

    config.set("bracketsLocation", CustomLocation.locationToString(this.bracketsLocation));
    config.set("bracketsFirst", CustomLocation.locationToString(this.bracketsFirst));
    config.set("bracketsSecond", CustomLocation.locationToString(this.bracketsSecond));

    config.set("gulagLocation", CustomLocation.locationToString(this.gulagLocation));
    config.set("gulagFirework", CustomLocation.locationToString(this.gulagFirework));
    config.set("gulagFirst", CustomLocation.locationToString(this.gulagFirst));
    config.set("gulagSecond", CustomLocation.locationToString(this.gulagSecond));

    config.set("oitcLocation", CustomLocation.locationToString(this.oitcLocation));
    config.set("oitc", this.fromLocations(this.oitcSpawnpoints));

    config.set("dropperLocation", CustomLocation.locationToString(this.dropperLocation));
    config.set("dropperMaps", this.fromLocations(this.dropperMaps));

    config.set("lmsLocation", CustomLocation.locationToString(this.lmsLocation));
    config.set("lms", this.fromLocations(this.lmsLocations));

    config.set("knockoutLocation", CustomLocation.locationToString(this.knockoutLocation));
    config.set("knockout", this.fromLocations(this.knockoutLocations));

    config.set("stoplightLocation", CustomLocation.locationToString(this.stoplightLocation));
    config.set("stoplight", this.fromLocations(this.stoplightLocations));

    config.set("skywarsLocation", CustomLocation.locationToString(this.skywarsLocation));
    config.set("skywarsMin", CustomLocation.locationToString(this.skywarsMin));
    config.set("skywarsMax", CustomLocation.locationToString(this.skywarsMax));
    config.set("skywars", this.fromLocations(this.skywarsLocations));

    config.set("spleefLocation", CustomLocation.locationToString(this.spleefLocation));
    config.set("spleefMin", CustomLocation.locationToString(this.spleefMin));
    config.set("spleefMax", CustomLocation.locationToString(this.spleefMax));

    config.set("tntTagLocation", CustomLocation.locationToString(this.tntTagLocation));
    config.set("tntTagGameLocation", CustomLocation.locationToString(this.tntTagGameLocation));

    config.set("parkourLocation", CustomLocation.locationToString(this.parkourLocation));
    config.set("parkourGameLocation", CustomLocation.locationToString(this.parkourGameLocation));

    config.set("holoLeaderboardsLocation",
        CustomLocation.locationToString(this.holoLeaderboardsLocation));
    config.set("winstreakHoloLocation",
        CustomLocation.locationToString(this.holoWinstreakLocation));

    config.set("ffaLocation", CustomLocation.locationToString(this.ffaLocation));

    plugin.getMainConfig().save();
  }

  public List<String> fromLocations(List<CustomLocation> locations) {
    List<String> toReturn = new ArrayList<>();
    for (CustomLocation location : locations) {
      toReturn.add(CustomLocation.locationToString(location));
    }

    return toReturn;
  }

  public CustomLocation getEventLocation(String eventName) {
    CustomLocation toReturn = null;

    switch (eventName.toLowerCase()) {
      case "4corners":
        toReturn = this.cornersLocation;
        break;
      case "thimble":
        toReturn = this.thimbleLocation;
        break;
      case "sumo":
        toReturn = this.sumoLocation;
        break;
      case "brackets":
        toReturn = this.bracketsLocation;
        break;
      case "gulag":
        toReturn = this.gulagLocation;
        break;
      case "oitc":
        toReturn = this.oitcLocation;
        break;
      case "dropper":
        toReturn = this.dropperLocation;
        break;
      case "lms":
        toReturn = this.lmsLocation;
        break;
      case "knockout":
        toReturn = this.knockoutLocation;
        break;
      case "stoplight":
        toReturn = this.stoplightLocation;
        break;
      case "skywars":
        toReturn = this.skywarsLocation;
        break;
      case "spleef":
        toReturn = this.spleefLocation;
        break;
      case "tnttag":
        toReturn = this.tntTagLocation;
        break;
      case "parkour":
        toReturn = this.parkourLocation;
        break;
    }

    return toReturn;
  }
}
