package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidBarrelBlock;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public class FluidBarrelBlockEntity extends FluidTankBlockEntity {
    private static final List<FluidPort> ADJACENT_PORTS = List.of(
            FluidPort.of(1, 0, 0, Direction.EAST),
            FluidPort.of(-1, 0, 0, Direction.WEST),
            FluidPort.of(0, 1, 0, Direction.UP),
            FluidPort.of(0, -1, 0, Direction.DOWN),
            FluidPort.of(0, 0, 1, Direction.SOUTH),
            FluidPort.of(0, 0, -1, Direction.NORTH));

    public FluidBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, ModBlockEntities.FLUID_BARREL.get(),
                new HbmFluidTank(HbmFluids.NONE, getVariant(state).capacity()));
    }

    public FluidBarrelBlock.Variant getVariant() {
        return getVariant(getBlockState());
    }

    @Override
    protected boolean checkHazards() {
        if (getTank().isEmpty() || level == null) {
            return false;
        }

        FluidBarrelBlock.Variant variant = getVariant();
        FluidType type = getTank().getTankType();

        if (variant != FluidBarrelBlock.Variant.ANTIMATTER && type.isAntimatter()) {
            int fill = getTank().getFill();
            dropInventoryItems();
            level.destroyBlock(worldPosition, false);
            level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D, 5.0F, Level.ExplosionInteraction.BLOCK);
            getTank().setFill(0);
            return fill > 0;
        }

        CorrosiveFluidTrait corrosive = type.getTrait(CorrosiveFluidTrait.class);
        if (variant == FluidBarrelBlock.Variant.PLASTIC && (type.isHot() || corrosive != null)) {
            dropInventoryItems();
            level.destroyBlock(worldPosition, false);
            playFizz();
            return true;
        }

        if (variant == FluidBarrelBlock.Variant.CORRODED) {
            boolean changed = false;
            if (level.random.nextInt(3) == 0) {
                getTank().release(level, worldPosition, 1, FluidReleaseType.SPILL, false);
                changed = true;
            }
            if (level.random.nextInt(3 * 60 * 20) == 0) {
                dropInventoryItems();
                level.destroyBlock(worldPosition, false);
                changed = true;
            }
            return changed;
        }

        return false;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return ADJACENT_PORTS;
    }

    @Override
    protected long getTransferSpeedFloor() {
        return 250L;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.barrel");
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return super.canConnectFluid(type, side) && type == getTank().getTankType();
    }

    @Override
    protected IItemHandler getExternalItemHandler() {
        return getTankContainerAutomationItemHandler();
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        if (getTank().getFill() == 0) {
            return;
        }
        getTank().writeToNbt(persistent, "tank");
        persistent.putShort("mode", (short) getMode());
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        getTank().readFromNbt(persistent, "tank");
        int mode = persistent.contains("mode") ? persistent.getShort("mode") : persistent.getShort("nbt");
        setMode(mode);
        setChanged();
        if (level != null) {
            refreshFluidNodeState();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    private void playFizz() {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void dropInventoryItems() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (net.minecraft.world.item.ItemStack stack : getDrops()) {
            Block.popResource(level, worldPosition, stack);
        }
    }

    private static FluidBarrelBlock.Variant getVariant(BlockState state) {
        return state.getBlock() instanceof FluidBarrelBlock barrel
                ? barrel.variant()
                : FluidBarrelBlock.Variant.PLASTIC;
    }
}
