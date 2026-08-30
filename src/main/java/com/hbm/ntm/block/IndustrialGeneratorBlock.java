package com.hbm.ntm.block;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.blockentity.IndustrialGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The 1.7.10 industrial-generator memorial: a static model with its original look overlay. */
public class IndustrialGeneratorBlock extends LegacyVisibleMachineBlock implements LegacyLookOverlayBlockProvider {
    public IndustrialGeneratorBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IndustrialGeneratorBlockEntity(pos, state);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        return LegacyLookOverlay.withTitle(Component.literal("Industrial Generator Memorial"), 0xFF8000, 0x804000,
                List.of(Component.literal("In memory of all that we have lost")));
    }
}
