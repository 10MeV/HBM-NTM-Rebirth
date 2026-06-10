package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FensuBlockEntity extends MachineBatteryBlockEntity {
    public static final long MAX_TRANSFER = 10_000_000_000_000_000L;
    private static final HbmEnergyUtil.EnergyPort BOTTOM_PORT =
            new HbmEnergyUtil.EnergyPort(new BlockPos(0, -1, 0), Direction.DOWN);

    private float previousRotation;
    private float rotation;

    public FensuBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_FENSU.get(), pos, state, Long.MAX_VALUE, MAX_TRANSFER, MAX_TRANSFER);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FensuBlockEntity blockEntity) {
        blockEntity.previousRotation = blockEntity.rotation;
        blockEntity.rotation += blockEntity.getSpinSpeed();
        if (blockEntity.rotation >= 360.0F) {
            blockEntity.rotation -= 360.0F;
            blockEntity.previousRotation -= 360.0F;
        }
    }

    public float getInterpolatedRotation(float partialTick) {
        return previousRotation + (rotation - previousRotation) * partialTick;
    }

    public float getSpinSpeed() {
        return (float) Math.pow(Math.log(getPower() * 0.75D + 1.0D) * 0.05D, 5.0D);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        if (side != Direction.DOWN) {
            return HbmEnergySideMode.NONE;
        }
        return switch (getCurrentMode()) {
            case MODE_INPUT -> HbmEnergySideMode.INPUT;
            case MODE_BUFFER -> HbmEnergySideMode.BOTH;
            case MODE_OUTPUT -> HbmEnergySideMode.OUTPUT;
            default -> HbmEnergySideMode.NONE;
        };
    }

    @Override
    protected Iterable<HbmEnergyUtil.EnergyPort> getEnergyPorts() {
        return List.of(BOTTOM_PORT);
    }

    @Override
    protected boolean shouldUseRemotePortEnergyNode() {
        return true;
    }

    @Override
    protected void handleInputMode() {
        subscribeEnergyReceiverToSide(Direction.DOWN);
    }

    @Override
    protected void handleOutputMode() {
        if (level != null) {
            HbmEnergyUtil.tryProvideToNeighbor(level, worldPosition, Direction.DOWN, energy);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_ntm_rebirth.fensu");
    }
}
