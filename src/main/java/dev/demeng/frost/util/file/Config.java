package dev.demeng.frost.util.file;

import java.io.File;
import java.io.IOException;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Config {

  private final FileConfiguration config;
  private final File configFile;
  protected boolean wasCreated;

  public Config(String name, JavaPlugin plugin) {
    this.configFile = new File(plugin.getDataFolder() + File.separator + name + ".yml");
    if (!this.configFile.exists()) {
      try {
        this.configFile.getParentFile().mkdirs();
        this.configFile.createNewFile();
        this.wasCreated = true;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    this.config = YamlConfiguration.loadConfiguration(this.configFile);
  }

  public void save() {
    try {
      this.config.save(configFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
