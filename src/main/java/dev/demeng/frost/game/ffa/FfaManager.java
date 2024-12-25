package dev.demeng.frost.game.ffa;

import dev.demeng.frost.Frost;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class FfaManager {

  private final Map<String, FfaInstance> spawnFfaKits = new HashMap<>();

  public FfaManager(Frost plugin) {
    plugin.getManagerHandler().getKitManager().getKits().forEach(kit -> {
      this.spawnFfaKits.put(kit.getName(), new FfaInstance(plugin, kit));
    });
  }

  public FfaInstance getByKitName(String kitName) {
    return spawnFfaKits.get(kitName);
  }

  public FfaInstance getByPlayer(Player player) {
    for (Map.Entry<String, FfaInstance> entry : spawnFfaKits.entrySet()) {
      FfaInstance ffaInstance = entry.getValue();
      if (ffaInstance.getFfaPlayers().contains(player.getUniqueId())) {
        return ffaInstance;
      }
    }

    return null;
  }

  public List<UUID> getAllPlayers() {
    List<UUID> totalPlayers = new ArrayList<>();
    for (Map.Entry<String, FfaInstance> entry : spawnFfaKits.entrySet()) {
      totalPlayers.addAll(entry.getValue().getFfaPlayers());
    }

    return totalPlayers;
  }
}
