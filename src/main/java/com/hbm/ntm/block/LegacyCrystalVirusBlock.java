package com.hbm.ntm.block;

import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("deprecation")
public class LegacyCrystalVirusBlock extends Block {
    public LegacyCrystalVirusBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!HbmCommonConfig.crystalVirusSpreadingEnabled()) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            BlockState targetState = level.getBlockState(target);
            if (!isBlockedFromSpread(targetState)) {
                level.setBlock(target, defaultBlockState(), 3);
            }
        }
        harden(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (level.isClientSide) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            BlockState targetState = level.getBlockState(target);
            if (!isAirOrCrystal(targetState)) {
                return;
            }
        }
        harden(level, pos);
    }

    private void harden(Level level, BlockPos pos) {
        RegistryObject<? extends Block> hardened = ModBlocks.legacyBlock("crystal_hardened");
        if (hardened != null && hardened.isPresent()) {
            level.setBlock(pos, hardened.get().defaultBlockState(), 3);
        }
    }

    private boolean isBlockedFromSpread(BlockState state) {
        return state.isAir() || isCrystal(state);
    }

    private boolean isAirOrCrystal(BlockState state) {
        return state.isAir() || isCrystal(state);
    }

    private boolean isCrystal(BlockState state) {
        return state.is(this)
                || isLegacyBlock(state, "crystal_hardened")
                || isLegacyBlock(state, "crystal_pulsar");
    }

    private boolean isLegacyBlock(BlockState state, String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block != null && block.isPresent() && state.is(block.get());
    }
}
