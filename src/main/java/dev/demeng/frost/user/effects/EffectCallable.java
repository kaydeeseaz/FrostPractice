package dev.demeng.frost.user.effects;

import org.bukkit.entity.Player;

public interface EffectCallable {

  void call(Player player, Player... watchers);
}
