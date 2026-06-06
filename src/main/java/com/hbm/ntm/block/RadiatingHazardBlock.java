package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ChunkRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

@SuppressWarnings("deprecation")
public class RadiatingHazardBlock extends Block {
    private static final Map<String, Float> LEGACY_CHUNK_RADIATION_SOURCES = Map.ofEntries(
            Map.entry("block_actinium", 30.0F),
            Map.entry("block_corium", 15.0F),
            Map.entry("block_fallout", 1.05F),
            Map.entry("block_mox_fuel", 2.5F),
            Map.entry("block_neptunium", 2.5F),
            Map.entry("block_plutonium", 7.5F),
            Map.entry("block_plutonium_fuel", 4.25F),
            Map.entry("block_polonium", 75.0F),
            Map.entry("block_pu238", 10.0F),
            Map.entry("block_pu239", 5.0F),
            Map.entry("block_pu240", 7.5F),
            Map.entry("block_pu_mix", 6.25F),
            Map.entry("block_ra226", 7.5F),
            Map.entry("block_schrabidate", 1.5F),
            Map.entry("block_schrabidium", 15.0F),
            Map.entry("block_schrabidium_fuel", 5.85F),
            Map.entry("block_schraranium", 1.5F),
            Map.entry("block_solinium", 17.5F),
            Map.entry("block_thorium", 0.1F),
            Map.entry("block_thorium_fuel", 1.75F),
            Map.entry("block_trinitite", 0.1F),
            Map.entry("block_u233", 5.0F),
            Map.entry("block_u235", 1.0F),
            Map.entry("block_u238", 0.25F),
            Map.entry("block_uranium", 0.35F),
            Map.entry("block_uranium_fuel", 0.5F),
            Map.entry("block_waste", 15.0F),
            Map.entry("block_waste_painted", 15.0F),
            Map.entry("block_waste_vitrified", 7.5F),
            Map.entry("block_yellowcake", 1.05F),
            Map.entry("ore_schrabidium", 0.1F)
    );
    private final float chunkRadiationPerTick;

    public RadiatingHazardBlock(String legacyName, Properties properties) {
        super(properties);
        this.chunkRadiationPerTick = LEGACY_CHUNK_RADIATION_SOURCES.getOrDefault(legacyName, 0.0F);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock() && chunkRadiationPerTick > 0.0F) {
            level.scheduleTick(pos, this, tickRate());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (chunkRadiationPerTick > 0.0F) {
            ChunkRadiationManager.incrementRadiation(level, pos, chunkRadiationPerTick);
            level.scheduleTick(pos, this, tickRate());
        }
    }

    private static int tickRate() {
        return 20;
    }
}

