package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class FluidDuctExhaustBlockEntity extends BlockEntity implements HbmFluidConnector, HbmFluidNodeHost {
    private static final FluidType[] SMOKES = {
            HbmFluids.SMOKE,
            HbmFluids.SMOKE_LEADED,
            HbmFluids.SMOKE_POISON
    };

    private final HbmFluidNode[] nodes = new HbmFluidNode[SMOKES.length];

    public FluidDuctExhaustBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_DUCT_EXHAUST.get(), pos, state);
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return nodes[0];
    }

    public HbmFluidNet getFluidNet(FluidType type) {
        for (int i = 0; i < SMOKES.length; i++) {
            if (SMOKES[i] == type) {
                return nodes[i] == null ? null : nodes[i].getFluidNet();
            }
        }
        return null;
    }

    @Override
    public void refreshFluidNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int i = 0; i < SMOKES.length; i++) {
            FluidType type = SMOKES[i];
            if (nodes[i] != null) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
                nodes[i] = null;
            }
            Set<Direction> connections = HbmFluidConnectionUtil.collectNodeConnections(level, worldPosition, type, this);
            nodes[i] = HbmFluidNodespace.createNode(level, new HbmFluidNode(worldPosition, type, connections));
        }
    }

    @Override
    public void removeFluidNode() {
        if (level != null && !level.isClientSide) {
            for (FluidType type : SMOKES) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
            }
        }
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = null;
        }
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && (type == HbmFluids.SMOKE
                || type == HbmFluids.SMOKE_LEADED
                || type == HbmFluids.SMOKE_POISON);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshFluidNode();
    }

    @Override
    public void setRemoved() {
        removeFluidNode();
        super.setRemoved();
    }
}
