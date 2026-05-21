package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluidProvider;
import com.hbm.ntm.fluid.HbmFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class HbmFluidNetworkBlockEntity extends HbmFluidBlockEntity implements HbmFluidConnector {
    protected HbmFluidNetworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, java.util.List<HbmFluidTank> tanks) {
        super(type, pos, state, tanks);
    }

    public static <T extends HbmFluidNetworkBlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (!level.isClientSide) {
            blockEntity.refreshFluidNodeState();
            blockEntity.refreshFluidNetworkSubscriptions();
        }
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return super.getFluidNode();
    }

    public HbmFluidNet getFluidNet() {
        HbmFluidNode node = getFluidNode();
        return node == null ? null : node.getFluidNet();
    }

    public HbmFluidNet getFluidNet(FluidType type) {
        HbmFluidNode node = getFluidNode(type);
        return node == null ? null : node.getFluidNet();
    }

    protected Set<Direction> getFluidConnections(FluidType type) {
        return level == null
                ? Set.of()
                : HbmFluidConnectionUtil.collectNodeConnections(level, worldPosition, type, this);
    }

    protected List<FluidType> getFluidNodeTypes() {
        return getAllTanks().stream()
                .map(HbmFluidTank::getTankType)
                .filter(type -> type != HbmFluids.NONE)
                .distinct()
                .toList();
    }

    protected HbmFluidNode createFluidNode(FluidType type) {
        return new HbmFluidNode(worldPosition, type, getFluidConnections(type));
    }

    protected void refreshFluidNodeState() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (!shouldCreateFluidNode()) {
            removeFluidNode();
            return;
        }
        for (FluidType type : getFluidNodeTypes()) {
            HbmFluidNode existing = getFluidNode(type);
            if (existing == null || existing.isExpired()) {
                HbmFluidNode node = HbmFluidNodespace.createNode(level, createFluidNode(type));
                setFluidNode(node);
            }
        }
        removeObsoleteFluidNodes();
    }

    protected boolean shouldCreateFluidNode() {
        return true;
    }

    protected boolean shouldSubscribeAsFluidProvider() {
        return false;
    }

    protected boolean shouldSubscribeAsFluidReceiver() {
        return false;
    }

    protected HbmFluidProvider getNetworkFluidProvider() {
        return this instanceof HbmFluidProvider provider ? provider : null;
    }

    protected HbmFluidReceiver getNetworkFluidReceiver() {
        return this instanceof HbmFluidReceiver receiver ? receiver : null;
    }

    protected void refreshFluidNetworkSubscriptions() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (FluidType type : getFluidNodeTypes()) {
            HbmFluidNet fluidNet = getFluidNet(type);
            if (fluidNet == null || !fluidNet.isValid()) {
                continue;
            }
            if (shouldSubscribeAsFluidProvider(type)) {
                fluidNet.addProvider(getNetworkFluidProvider());
            }
            if (shouldSubscribeAsFluidReceiver(type)) {
                fluidNet.addReceiver(getNetworkFluidReceiver());
            }
        }
    }

    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return shouldSubscribeAsFluidProvider();
    }

    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return shouldSubscribeAsFluidReceiver();
    }

    private void removeObsoleteFluidNodes() {
        Set<FluidType> activeTypes = new HashSet<>(getFluidNodeTypes());
        for (FluidType type : getTrackedFluidNodeTypes()) {
            if (!activeTypes.contains(type)) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
                removeFluidNode(type);
            }
        }
    }

    @Override
    public void removeFluidNode() {
        if (level == null || level.isClientSide) {
            super.removeFluidNode();
            return;
        }
        for (FluidType type : getFluidNodeTypes()) {
            HbmFluidNodespace.destroyNode(level, worldPosition, type);
        }
        super.removeFluidNode();
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && type != null && getFluidNodeTypes().contains(type);
    }
}
