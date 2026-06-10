package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class RadarGuiTargetProfile {
    private RadarGuiTargetProfile() {
    }

    @Nullable
    public static Target hoveredEntry(List<RadarEntry> entries, BlockPos radarPos, int range,
            int leftPos, int topPos, int mouseX, int mouseY) {
        for (RadarEntry entry : entries) {
            RadarDisplayProjection.ScreenOffset offset =
                    RadarDisplayProjection.guiBlipHitOffset(entry.pos(), radarPos, range);
            int x = leftPos + (int) offset.x() + RadarGuiLayout.RADAR_CENTER_X;
            int z = topPos + (int) offset.z() + RadarGuiLayout.RADAR_CENTER_Y;
            if (RadarGuiHitProfile.hitsBlip(mouseX, mouseY, x, z)) {
                return Target.entry(entry, x, z);
            }
        }
        return null;
    }

    public static Target positionTarget(BlockPos radarPos, int range, int leftPos, int topPos,
            int mouseX, int mouseY) {
        int x = RadarDisplayProjection.guiTargetX(mouseX - leftPos - RadarGuiLayout.RADAR_CENTER_X,
                radarPos, range);
        int z = RadarDisplayProjection.guiTargetZ(mouseY - topPos - RadarGuiLayout.RADAR_CENTER_Y,
                radarPos, range);
        return Target.position(x, z);
    }

    public record Target(@Nullable RadarEntry entry, int x, int z, int screenX, int screenZ) {
        public static Target entry(RadarEntry entry, int screenX, int screenZ) {
            return new Target(entry, entry.pos().getX(), entry.pos().getZ(), screenX, screenZ);
        }

        public static Target position(int x, int z) {
            return new Target(null, x, z, 0, 0);
        }

        public boolean hasEntry() {
            return entry != null;
        }
    }
}
