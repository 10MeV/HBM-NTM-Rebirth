package com.hbm.ntm.api.entity;

import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.item.ItemCoordinateBase;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Optional;

public final class RadarScreenLinkUpdate {
    private RadarScreenLinkUpdate() {
    }

    public static RadarScreenSnapshot snapshot(BlockPos radarPos, int range, Collection<RadarEntry> entries) {
        return RadarScreenSnapshot.linked(radarPos, range, entries);
    }

    public static boolean deliver(Level level, ItemStack linker, RadarScreenSnapshot snapshot) {
        Optional<RadarScreenBlockEntity> screen = resolveScreen(level, linker);
        if (screen.isEmpty()) {
            return false;
        }
        screen.get().receiveRadarSnapshot(snapshot);
        return true;
    }

    public static Optional<RadarScreenBlockEntity> resolveScreen(Level level, ItemStack linker) {
        if (level == null || level.isClientSide || !isRadarScreenLinker(linker)) {
            return Optional.empty();
        }
        BlockPos screenPos = ItemCoordinateBase.getPosition(linker);
        if (screenPos == null || !(level.getBlockEntity(screenPos) instanceof RadarScreenBlockEntity screen)) {
            return Optional.empty();
        }
        return Optional.of(screen);
    }

    public static boolean isRadarScreenLinker(ItemStack stack) {
        return stack != null && stack.is(ModItems.RADAR_LINKER.get());
    }
}
