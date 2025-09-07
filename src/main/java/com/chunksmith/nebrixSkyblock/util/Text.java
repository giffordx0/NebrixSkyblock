package com.chunksmith.nebrixSkyblock.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** Utility wrapper around MiniMessage parsing. */
public final class Text {
  private static final MiniMessage MM = MiniMessage.miniMessage();

  private Text() {}

  public static Component mini(String input) {
    try {
      return MM.deserialize(input);
    } catch (Exception ex) {
      return Component.text(input);
    }
  }
}
