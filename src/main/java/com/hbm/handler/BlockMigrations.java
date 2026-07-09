package com.hbm.handler;

import com.hbm.ntm.world.BlockMigrationHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Legacy package facade for the 1.7.10 chunk migration marker.
 */
public class BlockMigrations {
    public static final String NBT_KEY_BUILD_NUMBER = BlockMigrationHelper.NBT_KEY_BUILD_NUMBER;

    public static int buildNumber() {
        return BlockMigrationHelper.buildNumber();
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        BlockMigrationHelper.load(event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        BlockMigrationHelper.save(event.getData());
    }

    public static void doMigraion(ChunkAccess chunk) {
        BlockMigrationHelper.doMigration(chunk, buildNumber(), buildNumber());
    }

    public static void doMigration(ChunkAccess chunk) {
        doMigraion(chunk);
    }

    public static void doMigration(ChunkAccess chunk, CompoundTag tag, int previousBuild, int currentBuild) {
        BlockMigrationHelper.doMigration(chunk, tag, previousBuild, currentBuild);
    }
}
