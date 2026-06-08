package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.energy.HbmEnergyConnector;
import com.hbm.ntm.energy.HbmEnergyNodeHost;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public abstract class HbmEnergyNodeBlockEntity extends BlockEntity implements HbmEnergyNodeHost, HbmEnergyConnector {
    private HbmEnergyNode energyNode;

    protected HbmEnergyNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public HbmEnergyNode getEnergyNode() {
        return energyNode;
    }

    protected Set<Direction> getEnergyConnections() {
        return level == null
                ? Set.of()
                : HbmEnergyConnectionUtil.collectNodeConnections(level, worldPosition, this);
    }

    protected HbmEnergyNode createEnergyNode() {
        return new HbmEnergyNode(worldPosition, getEnergyConnections());
    }

    @Override
    public void refreshEnergyNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (!shouldCreateEnergyNode()) {
            removeEnergyNode();
            return;
        }
        if (energyNode != null) {
            HbmEnergyNodespace.destroyNode(level, worldPosition);
            energyNode = null;
        }
        energyNode = HbmEnergyNodespace.createNode(level, createEnergyNode());
    }

    protected boolean shouldCreateEnergyNode() {
        return true;
    }

    @Override
    public void removeEnergyNode() {
        if (level == null || level.isClientSide) {
            energyNode = null;
            return;
        }
        HbmEnergyNodespace.destroyNode(level, worldPosition);
        energyNode = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshEnergyNode();
    }

    @Override
    public void setRemoved() {
        removeEnergyNode();
        super.setRemoved();
    }
}
