package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public class FluidDuctExhaustBlockEntity extends BlockEntity implements HbmFluidConnector, HbmFluidNodeHost,
        LegacyLookOverlayProvider {
    private final HbmFluidNode[] nodes = new HbmFluidNode[SmokeExhaustPollution.SMOKES.length];

    public FluidDuctExhaustBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_DUCT_EXHAUST.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidDuctExhaustBlockEntity exhaust) {
        if (!level.isClientSide) {
            exhaust.ensureFluidNodes();
        }
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return nodes[0];
    }

    public HbmFluidNet getFluidNet(FluidType type) {
        for (int i = 0; i < SmokeExhaustPollution.SMOKES.length; i++) {
            if (SmokeExhaustPollution.SMOKES[i] == type) {
                return nodes[i] == null ? null : nodes[i].getFluidNet();
            }
        }
        return null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(net.minecraft.world.level.Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.fluidNames(SmokeExhaustPollution.SMOKES));
    }

    @Override
    public void refreshFluidNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int i = 0; i < SmokeExhaustPollution.SMOKES.length; i++) {
            FluidType type = SmokeExhaustPollution.SMOKES[i];
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
            for (FluidType type : SmokeExhaustPollution.SMOKES) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
            }
        }
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = null;
        }
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && SmokeExhaustPollution.isSmoke(type);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshFluidNode();
    }

    private void ensureFluidNodes() {
        for (HbmFluidNode node : nodes) {
            if (node == null || node.isExpired()) {
                refreshFluidNode();
                return;
            }
        }
    }

    @Override
    public void setRemoved() {
        removeFluidNode();
        super.setRemoved();
    }
}
