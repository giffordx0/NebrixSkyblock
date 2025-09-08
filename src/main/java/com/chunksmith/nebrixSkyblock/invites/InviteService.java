package com.chunksmith.nebrixSkyblock.invites;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteService {
  private final Map<UUID, Invite> invites = new HashMap<>();

  public void addInvite(UUID islandId, UUID from, UUID to) {
    invites.put(to, new Invite(islandId, from, to));
  }

  public Invite getInvite(UUID to) {
    return invites.get(to);
  }

  public void removeInvite(UUID to) {
    invites.remove(to);
  }
}
