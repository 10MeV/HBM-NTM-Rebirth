package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fusion.FusionPowerReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.ntm.uninos.networkproviders.PlasmaNode;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FusionCollectorBlockEntity extends BlockEntity implements FusionPowerReceiver {
    private PlasmaNode plasmaNode;

    public FusionCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_COLLECTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionCollectorBlockEntity collector) {
        collector.ensureNode(level);
    }

    @Override
    public boolean receivesFusionPower() {
        return false;
    }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 4, 3));
    }

    @Override
    public void setRemoved() {
        destroyNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        destroyNode();
        super.onChunkUnloaded();
    }

    private void destroyNode() {
        if (level != null && !level.isClientSide && plasmaNode != null) {
            PlasmaNodespace.destroyNode(level, plasmaNode.getPos());
        }
        plasmaNode = null;
    }

    private void ensureNode(Level level) {
        Direction direction = facing().getOpposite();
        BlockPos nodePos = worldPosition.relative(direction, 2).above(2);
        if (plasmaNode == null || plasmaNode.isExpired()) {
            PlasmaNode existing = PlasmaNodespace.getNode(level, nodePos);
            plasmaNode = existing == null
                    ? PlasmaNodespace.createNode(level, new PlasmaNode(nodePos, Set.of(direction)))
                    : existing;
        }
        PlasmaNetwork net = plasmaNode.getPlasmaNet();
        if (net != null) {
            net.addReceiver(this);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }
}
