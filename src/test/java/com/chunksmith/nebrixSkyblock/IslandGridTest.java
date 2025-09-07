package com.chunksmith.nebrixSkyblock;

import com.chunksmith.nebrixSkyblock.island.IslandGrid;
import java.awt.Point;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IslandGridTest {
  @Test
  void spiralFirstFifty() {
    List<Point> points = IslandGrid.first(50);
    List<Point> expected = List.of(
        new Point(0,0),
        new Point(1,0),
        new Point(1,1),
        new Point(0,1),
        new Point(-1,1),
        new Point(-1,0),
        new Point(-1,-1),
        new Point(0,-1),
        new Point(1,-1),
        new Point(2,-1),
        new Point(2,0),
        new Point(2,1),
        new Point(2,2),
        new Point(1,2),
        new Point(0,2),
        new Point(-1,2),
        new Point(-2,2),
        new Point(-2,1),
        new Point(-2,0),
        new Point(-2,-1),
        new Point(-2,-2),
        new Point(-1,-2),
        new Point(0,-2),
        new Point(1,-2),
        new Point(2,-2),
        new Point(3,-2),
        new Point(3,-1),
        new Point(3,0),
        new Point(3,1),
        new Point(3,2),
        new Point(3,3),
        new Point(2,3),
        new Point(1,3),
        new Point(0,3),
        new Point(-1,3),
        new Point(-2,3),
        new Point(-3,3),
        new Point(-3,2),
        new Point(-3,1),
        new Point(-3,0),
        new Point(-3,-1),
        new Point(-3,-2),
        new Point(-3,-3),
        new Point(-2,-3),
        new Point(-1,-3),
        new Point(0,-3),
        new Point(1,-3),
        new Point(2,-3),
        new Point(3,-3),
        new Point(4,-3)
    );
    assertEquals(expected, points);
  }
}
