package com.hbm.ntm.uninos;

import com.hbm.ntm.uninos.networkproviders.FoundryNodespace;
import com.hbm.ntm.uninos.networkproviders.KlystronNodespace;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import com.hbm.ntm.uninos.networkproviders.RebarNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class HbmUninosNodespaces {
    public static void tick(ServerLevel level) {
        PlasmaNodespace.tick(level);
        KlystronNodespace.tick(level);
        FoundryNodespace.tick(level);
        RebarNodespace.tick(level);
        PneumaticNodespace.tick(level);
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        PlasmaNodespace.unloadChunk(level, chunkPos);
        KlystronNodespace.unloadChunk(level, chunkPos);
        FoundryNodespace.unloadChunk(level, chunkPos);
        RebarNodespace.unloadChunk(level, chunkPos);
        PneumaticNodespace.unloadChunk(level, chunkPos);
    }

    public static void unloadLevel(Level level) {
        PlasmaNodespace.unloadLevel(level);
        KlystronNodespace.unloadLevel(level);
        FoundryNodespace.unloadLevel(level);
        RebarNodespace.unloadLevel(level);
        PneumaticNodespace.unloadLevel(level);
    }

    private HbmUninosNodespaces() {
    }
}
