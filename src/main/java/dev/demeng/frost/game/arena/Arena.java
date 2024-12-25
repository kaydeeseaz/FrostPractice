package dev.demeng.frost.game.arena;

import dev.demeng.frost.util.CustomLocation;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Arena {

  private final String name;

  private List<StandaloneArena> standaloneArenas;
  private List<StandaloneArena> availableArenas;

  private String icon;
  private int iconData;

  private CustomLocation a;
  private CustomLocation b;

  private CustomLocation min;
  private CustomLocation max;

  private int buildMax;
  private int deadZone;
  private int portalProt;

  private boolean enabled;

  public StandaloneArena getAvailableArena() {
    StandaloneArena arena = this.availableArenas.get(0);
    this.availableArenas.remove(0);

    return arena;
  }

  public void addStandaloneArena(StandaloneArena arena) {
    this.standaloneArenas.add(arena);
  }

  public void addAvailableArena(StandaloneArena arena) {
    this.availableArenas.add(arena);
  }
}
