package com.hbm.ntm.world.saveddata;

import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.world.BlockMigrationHelper;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class WorldSavedDataDiagnostics {
    public static LevelStatus inspect(ServerLevel level) {
        Optional<TomImpactSavedData> tom = TomImpactSavedData.getExisting(level);
        Optional<AnnihilatorSavedData> annihilator = AnnihilatorSavedData.getExisting(level);
        Optional<SatelliteSavedData> satellites = SatelliteSavedData.getExisting(level);
        return new LevelStatus(
                level.dimension().location(),
                tom.isPresent(),
                tom.map(TomImpactSavedData::snapshot).orElse(TomImpactSavedData.Snapshot.EMPTY),
                annihilator.isPresent(),
                annihilator.map(AnnihilatorSavedData::poolCount).orElse(0),
                annihilator.map(AnnihilatorSavedData::poolEntryCount).orElse(0),
                annihilator.map(AnnihilatorSavedData::totalAmount).orElse(BigInteger.ZERO),
                satellites.isPresent(),
                satellites.map(SatelliteSavedData::size).orElse(0));
    }

    public static ServerStatus inspect(MinecraftServer server) {
        List<LevelStatus> levels = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            levels.add(inspect(level));
        }
        return new ServerStatus(levels, BlockMigrationHelper.diagnostics());
    }

    public static Optional<LevelStatus> inspect(MinecraftServer server, ResourceKey<Level> dimension) {
        ServerLevel level = server.getLevel(dimension);
        return level == null ? Optional.empty() : Optional.of(inspect(level));
    }

    private WorldSavedDataDiagnostics() {
    }

    public record LevelStatus(ResourceLocation dimension, boolean hasTomImpact,
                              TomImpactSavedData.Snapshot tomImpact,
                              boolean hasAnnihilator, int annihilatorPools,
                              int annihilatorEntries, BigInteger annihilatorTotalAmount,
                              boolean hasSatellites, int satelliteCount) {
        public int presentDataCount() {
            return (hasTomImpact ? 1 : 0) + (hasAnnihilator ? 1 : 0) + (hasSatellites ? 1 : 0);
        }

        public boolean hasAnyData() {
            return presentDataCount() > 0;
        }

        public String summary() {
            return "dimension=" + dimension
                    + " impactData=" + (hasTomImpact ? "present" : "absent")
                    + " annihilator=" + (hasAnnihilator ? "present" : "absent")
                    + " satellites=" + (hasSatellites ? "present" : "absent");
        }
    }

    public record ServerStatus(List<LevelStatus> levels,
                               BlockMigrationHelper.MigrationDiagnostics migrations) {
        public int presentDataCount() {
            return levels.stream().mapToInt(LevelStatus::presentDataCount).sum();
        }

        public long levelsWithDataCount() {
            return levels.stream().filter(level -> level.presentDataCount() > 0).count();
        }
    }
}
