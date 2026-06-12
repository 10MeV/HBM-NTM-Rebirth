package com.hbm.ntm.blockentity;

import com.hbm.ntm.neutron.NeutronNodeWorld;
import com.hbm.ntm.neutron.PileGraphiteBlockEntityPlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PileGraphiteBlockEntity extends BlockEntity {
    protected PileGraphiteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setRemoved() {
        removePileNode(PileGraphiteBlockEntityPlanner.planInvalidate(getBlockPos()));
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        removePileNode(PileGraphiteBlockEntityPlanner.planChunkUnload(getBlockPos()));
        super.onChunkUnloaded();
    }

    private void removePileNode(PileGraphiteBlockEntityPlanner.LifecycleNodePlan plan) {
        if (level != null && plan.removeNeutronNode()) {
            NeutronNodeWorld.removeNode(level, plan.pos());
        }
    }
}
