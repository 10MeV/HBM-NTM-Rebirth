package com.hbm.ntm.uninos;

import com.hbm.ntm.uninos.networkproviders.FoundryNodespace;
import com.hbm.ntm.uninos.networkproviders.KlystronNodespace;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import com.hbm.ntm.uninos.networkproviders.RebarNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class HbmUninosNodespaces {
    public static void tick(ServerLevel level) {
        PlasmaNodespace.tick(level);
        KlystronNodespace.tick(level);
        FoundryNodespace.tick(level);
        RebarNodespace.tick(level);
        PneumaticNodespace.tick(level);
    }

    public static void unloadLevel(Level level) {
        PlasmaNodespace.unloadLevel(level);
        KlystronNodespace.unloadLevel(level);
        FoundryNodespace.unloadLevel(level);
        RebarNodespace.unloadLevel(level);
        PneumaticNodespace.unloadLevel(level);
    }

    public static void reapLevel(Level level) {
        PlasmaNodespace.reapLevel(level);
        KlystronNodespace.reapLevel(level);
        FoundryNodespace.reapLevel(level);
        RebarNodespace.reapLevel(level);
        PneumaticNodespace.reapLevel(level);
    }

    private HbmUninosNodespaces() {
    }
}
