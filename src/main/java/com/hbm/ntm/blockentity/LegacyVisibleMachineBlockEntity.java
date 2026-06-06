package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayPorts;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LegacyVisibleMachineBlockEntity extends BlockEntity implements LegacyLookOverlayProvider {
    public LegacyVisibleMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_VISIBLE_MACHINE.get(), pos, state);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(net.minecraft.world.level.Level level, BlockPos viewedPos) {
        if (getBlockState().is(ModBlocks.MACHINE_CHEMICAL_FACTORY.get())
                || getBlockState().is(ModBlocks.MACHINE_ASSEMBLY_FACTORY.get())) {
            return LegacyLookOverlayPorts.factoryMachinePort(this, viewedPos);
        }
        if (getBlockState().is(ModBlocks.MACHINE_TURBINEGAS.get())) {
            return LegacyLookOverlayPorts.turbineGasPort(this, viewedPos);
        }
        if (getBlockState().is(ModBlocks.MACHINE_ROTARY_FURNACE.get())) {
            return LegacyLookOverlayPorts.rotaryFurnacePort(this, viewedPos);
        }
        return null;
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            return block.definition().renderBoundingBox(state, worldPosition);
        }
        return super.getRenderBoundingBox();
    }
}
