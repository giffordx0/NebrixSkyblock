package com.chunksmith.nebrixSkyblock.island;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/** Spiral allocator for island centers. */
public final class IslandGrid {
  private int x;
  private int z;
  private int leg = 0;
  private int steps = 1;
  private int stepCount = 0;

  /** Returns next grid point in a clockwise spiral. */
  public Point next() {
    Point p = new Point(x, z);
    move();
    return p;
  }

  private void move() {
    switch (leg) {
      case 0 -> x++; // east
      case 1 -> z++; // north
      case 2 -> x--; // west
      case 3 -> z--; // south
    }
    stepCount++;
    if (stepCount == steps) {
      stepCount = 0;
      leg = (leg + 1) % 4;
      if (leg == 0 || leg == 2) {
        steps++;
      }
    }
  }

  /** Utility to generate first n points from origin. */
  public static List<Point> first(int n) {
    IslandGrid grid = new IslandGrid();
    List<Point> pts = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      pts.add(grid.next());
    }
    return pts;
  }
}
