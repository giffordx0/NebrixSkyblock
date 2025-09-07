package com.chunksmith.nebrixSkyblock.invites;

import java.util.HashSet;
import java.util.Set;

public class InviteService {
  private final Set<Invite> invites = new HashSet<>();

  public void add(Invite invite) {
    invites.add(invite);
  }

  public boolean has(Invite invite) {
    return invites.contains(invite);
  }
}
