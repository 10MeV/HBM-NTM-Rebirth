package com.hbm.ntm.uninos;

import com.hbm.ntm.uninos.networkproviders.FoundryNodespace;
import com.hbm.ntm.uninos.networkproviders.KlystronNodespace;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import com.hbm.ntm.uninos.networkproviders.RebarNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import net.minecraft.world.level.Level;

import java.util.List;

public final class HbmUninosDiagnostics {
    public static List<Entry> collect(Level level) {
        return List.of(
                PlasmaNodespace.diagnostics(level),
                KlystronNodespace.diagnostics(level),
                FoundryNodespace.diagnostics(level),
                RebarNodespace.diagnostics(level),
                PneumaticNodespace.diagnostics(level));
    }

    public static Totals totals(Level level) {
        int nodePositions = 0;
        int uniqueNodes = 0;
        int networks = 0;
        int invalidNetworks = 0;
        int linkRefs = 0;
        int dirtyNodes = 0;
        int expiredNodes = 0;
        int orphanNodes = 0;
        int providers = 0;
        int receivers = 0;
        for (Entry entry : collect(level)) {
            nodePositions += entry.core().nodePositions();
            uniqueNodes += entry.core().uniqueNodes();
            networks += entry.core().networks();
            invalidNetworks += entry.core().invalidNetworks();
            linkRefs += entry.core().linkRefs();
            dirtyNodes += entry.core().dirtyNodes();
            expiredNodes += entry.core().expiredNodes();
            orphanNodes += entry.core().orphanNodes();
            providers += entry.providers();
            receivers += entry.receivers();
        }
        return new Totals(nodePositions, uniqueNodes, networks, invalidNetworks, linkRefs,
                dirtyNodes, expiredNodes, orphanNodes, providers, receivers);
    }

    public record Entry(String name, HbmNodespace.Diagnostics core, int providers, int receivers) {
    }

    public record Totals(
            int nodePositions,
            int uniqueNodes,
            int networks,
            int invalidNetworks,
            int linkRefs,
            int dirtyNodes,
            int expiredNodes,
            int orphanNodes,
            int providers,
            int receivers) {
    }

    private HbmUninosDiagnostics() {
    }
}
