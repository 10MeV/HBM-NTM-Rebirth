package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fusion.FusionKlystronProvider;
import com.hbm.ntm.fusion.FusionPowerReceiver;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.KlystronNetwork;
import com.hbm.ntm.uninos.networkproviders.KlystronNode;
import com.hbm.ntm.uninos.networkproviders.KlystronNodespace;
import com.hbm.ntm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.ntm.uninos.networkproviders.PlasmaNode;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FusionCouplerBlockEntity extends BlockEntity
        implements FusionKlystronProvider, FusionPowerReceiver, HbmLegacyLoadedTile {
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private KlystronNode klystronNode;
    private PlasmaNode plasmaNode;
    private long klystronEnergy;
    private long lastReceivedTick = Long.MIN_VALUE;

    public FusionCouplerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_COUPLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionCouplerBlockEntity coupler) {
        coupler.ensureNodes(level);
        coupler.networkPackNT(100);
        if (level.getGameTime() % 20L == 0L) {
            coupler.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public long provideKlystronEnergy() {
        return 0L;
    }

    @Override
    public boolean receivesFusionPower() {
        return true;
    }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        klystronEnergy = Math.max(0L, fusionPower);
        lastReceivedTick = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (klystronNode != null) {
            FusionKlystronBlockEntity.provideKyU(klystronNode.getKlystronNet(), klystronEnergy);
        }
        setChanged();
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 4, 2));
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            if (klystronNode != null) {
                KlystronNodespace.destroyNode(level, klystronNode.getPos());
            }
            if (plasmaNode != null) {
                PlasmaNodespace.destroyNode(level, plasmaNode.getPos());
            }
        }
        super.setRemoved();
    }

    private void ensureNodes(Level level) {
        Direction rot = facing().getOpposite().getClockWise();
        BlockPos klystronPos = worldPosition.relative(rot).above(2);
        if (klystronNode == null || klystronNode.isExpired()) {
            KlystronNode existing = KlystronNodespace.getNode(level, klystronPos);
            klystronNode = existing == null
                    ? KlystronNodespace.createNode(level, new KlystronNode(klystronPos, Set.of(rot)))
                    : existing;
        }
        KlystronNetwork klystronNetwork = klystronNode.getKlystronNet();
        if (klystronNetwork != null) {
            klystronNetwork.addProvider(this);
        }

        Direction oppositeRot = rot.getOpposite();
        BlockPos plasmaPos = worldPosition.relative(oppositeRot).above(2);
        if (plasmaNode == null || plasmaNode.isExpired()) {
            PlasmaNode existing = PlasmaNodespace.getNode(level, plasmaPos);
            plasmaNode = existing == null
                    ? PlasmaNodespace.createNode(level, new PlasmaNode(plasmaPos, Set.of(oppositeRot)))
                    : existing;
        }
        PlasmaNetwork plasmaNetwork = plasmaNode.getPlasmaNet();
        if (plasmaNetwork != null) {
            plasmaNetwork.addReceiver(this);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }
}
