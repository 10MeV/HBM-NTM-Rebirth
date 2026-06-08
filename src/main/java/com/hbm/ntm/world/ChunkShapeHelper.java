package com.hbm.ntm.world;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public final class ChunkShapeHelper {
    public static List<ChunkPos> getChunksAlongLineSegment(int x0, int z0, int x1, int z1, double paddingSize) {
        int dx = Math.abs(x1 - x0);
        int sx = x0 < x1 ? 1 : -1;
        int dz = -Math.abs(z1 - z0);
        int sz = z0 < z1 ? 1 : -1;
        int error = dx + dz;
        int originalX = x0;
        int originalZ = z0;
        List<ChunkPos> out = new ArrayList<>();
        List<ChunkPos> checked = new ArrayList<>();

        while (true) {
            ChunkPos coords = new ChunkPos(x0 >> 4, z0 >> 4);
            if (!out.contains(coords)) {
                out.add(coords);
            }

            int[][] neighbors = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
            for (int[] neighbor : neighbors) {
                ChunkPos neighborCoords = new ChunkPos((x0 >> 4) + neighbor[0], (z0 >> 4) + neighbor[1]);
                if (checked.contains(neighborCoords)) {
                    continue;
                }
                checked.add(neighborCoords);
                if (!out.contains(neighborCoords)
                        && boxLineDistance(originalX, originalZ, x1, z1, neighborCoords.getMinBlockX(),
                        neighborCoords.getMinBlockZ()) < paddingSize) {
                    out.add(neighborCoords);
                }
            }

            int e2 = 2 * error;
            if (e2 >= dz) {
                if (x0 == x1) {
                    break;
                }
                error += dz;
                x0 += sx;
            }
            if (e2 <= dx) {
                if (z0 == z1) {
                    break;
                }
                error += dx;
                z0 += sz;
            }
        }
        return out;
    }

    private static double pointSegmentDistance(int x1, int z1, int x2, int z2, int px, int pz) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        if (dx == 0 && dz == 0) {
            return Math.sqrt((px - x1) * (px - x1) + (pz - z1) * (pz - z1));
        }

        double t = ((px - x1) * dx + (pz - z1) * dz) / (double) (dx * dx + dz * dz);
        if (t < 0) {
            return Math.sqrt((px - x1) * (px - x1) + (pz - z1) * (pz - z1));
        }
        if (t > 1) {
            return Math.sqrt((px - x2) * (px - x2) + (pz - z2) * (pz - z2));
        }

        double projX = x1 + t * dx;
        double projZ = z1 + t * dz;
        return Math.sqrt((px - projX) * (px - projX) + (pz - projZ) * (pz - projZ));
    }

    private static double boxLineDistance(int lineX0, int lineZ0, int lineX1, int lineZ1, int boxX, int boxZ) {
        double minDistance = Double.MAX_VALUE;
        int[][] corners = {{0, 0}, {0, 16}, {16, 0}, {16, 16}};
        for (int[] corner : corners) {
            minDistance = Math.min(minDistance,
                    pointSegmentDistance(lineX0, lineZ0, lineX1, lineZ1, boxX + corner[0], boxZ + corner[1]));
        }
        return minDistance;
    }

    private ChunkShapeHelper() {
    }
}
