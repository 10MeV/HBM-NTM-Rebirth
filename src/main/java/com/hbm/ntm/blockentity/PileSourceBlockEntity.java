package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.PileGraphiteDrilledBaseBlock;
import com.hbm.ntm.neutron.PileGraphiteBlockEntityPlanner;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.neutron.PileNeutronColumn;
import com.hbm.ntm.neutron.PileNeutronHandler;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PileSourceBlockEntity extends PileGraphiteBlockEntity implements PileNeutronColumn {
    public PileSourceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PILE_SOURCE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PileSourceBlockEntity blockEntity) {
        PileGraphiteInsertionPlanner.GraphiteBlockKind kind = state.getBlock() instanceof PileGraphiteDrilledBaseBlock graphite
                ? graphite.graphiteKind()
                : PileGraphiteInsertionPlanner.GraphiteBlockKind.SOURCE;
        PileGraphiteBlockEntityPlanner.SourceBlockEntityTickPlan plan =
                PileGraphiteBlockEntityPlanner.planSourceTick(pos, kind);
        for (PileGraphiteBlockEntityPlanner.RayCastRequest ray : plan.rayCasts()) {
            PileNeutronHandler.castRandomRay(blockEntity, ray.flux());
        }
    }
}
