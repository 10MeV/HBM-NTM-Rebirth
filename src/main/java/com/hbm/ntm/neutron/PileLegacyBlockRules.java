package com.hbm.ntm.neutron;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public final class PileLegacyBlockRules {
    public static final PileNeutronBlockRules LEGACY_DEFAULTS = PileLegacyBlockRules::evaluateLegacyBlocks;

    private static final Set<String> CONCRETE_SHIELD_BLOCKS = Set.of(
            "concrete",
            "concrete_smooth",
            "concrete_asbestos",
            "concrete_colored",
            "brick_concrete");

    private PileLegacyBlockRules() {
    }

    private static PileNeutronBlockResult evaluateLegacyBlocks(
            Level level,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity) {
        if (isLegacyBlock(state, "block_boron")) {
            return PileNeutronBlockResult.halt();
        }
        for (String concrete : CONCRETE_SHIELD_BLOCKS) {
            if (isLegacyBlock(state, concrete)) {
                return PileNeutronBlockResult.attenuate(0.25D);
            }
        }
        return PileNeutronBlockResult.pass();
    }

    private static boolean isLegacyBlock(BlockState state, String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block != null && block.isPresent() && state.is(block.get());
    }
}
