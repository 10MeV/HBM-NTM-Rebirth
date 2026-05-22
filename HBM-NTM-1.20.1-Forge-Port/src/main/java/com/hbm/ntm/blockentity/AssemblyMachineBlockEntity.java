package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AssemblyMachineBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_DID_PROCESS = "DidProcess";
    private static final String TAG_RING = "Ring";
    private static final String TAG_RING_TARGET = "RingTarget";
    private static final String TAG_RING_SPEED = "RingSpeed";
    private static final String TAG_RING_DELAY = "RingDelay";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_LEGACY_MAX_POWER = "maxPower";
    private static final String TAG_INPUT_TANK = "i";
    private static final String TAG_OUTPUT_TANK = "o";
    private static final long DEFAULT_MAX_POWER = 100_000L;
    private static final int TANK_CAPACITY = 4_000;

    private final ItemStackHandler items = new ItemStackHandler(17) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final HbmEnergyStorage energy = new HbmEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0L);
    private final HbmFluidTank inputTank = new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY);
    private final HbmFluidTank outputTank = new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(AssemblyFluidHandler::new);
    private final AssemblerArm[] arms = new AssemblerArm[] { new AssemblerArm(1L), new AssemblerArm(2L) };

    private boolean didProcess;
    private double prevRing;
    private double ring;
    private double ringTarget;
    private double ringSpeed;
    private int ringDelay;

    public AssemblyMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_MACHINE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AssemblyMachineBlockEntity blockEntity) {
        blockEntity.setChanged();
        if (level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AssemblyMachineBlockEntity blockEntity) {
        blockEntity.prevRing = blockEntity.ring;
        boolean animate = blockEntity.didProcess || blockEntity.previewAnimation();
        if (animate) {
            blockEntity.updateRing(level);
        }
        for (AssemblerArm arm : blockEntity.arms) {
            arm.updateInterp();
            if (animate) {
                arm.updateArm();
            } else {
                arm.returnToNullPos();
            }
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmEnergyStorage getEnergyStorage() {
        return energy;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    public double getRing(float partialTick) {
        return Mth.lerp(partialTick, prevRing, ring);
    }

    public AssemblerArm getArm(int index) {
        return arms[index];
    }

    public boolean shouldRenderFrame() {
        return level != null && !level.getBlockState(worldPosition.above(3)).isAir();
    }

    private boolean previewAnimation() {
        return level != null && level.isClientSide && level.getGameTime() % 120L < 80L;
    }

    private void updateRing(Level level) {
        if (ring != ringTarget) {
            double ringDelta = Math.abs(ringTarget - ring);
            if (ringDelta <= ringSpeed) {
                ring = ringTarget;
            }
            if (ringTarget > ring) {
                ring += ringSpeed;
            }
            if (ringTarget < ring) {
                ring -= ringSpeed;
            }
            if (ringTarget == ring) {
                double sub = ringTarget >= 360.0D ? -360.0D : 360.0D;
                ringTarget += sub;
                ring += sub;
                prevRing += sub;
                ringDelay = 20 + level.random.nextInt(21);
            }
        } else {
            if (ringDelay > 0) {
                ringDelay--;
            }
            if (ringDelay <= 0) {
                ringTarget += (level.random.nextDouble() * 2.0D - 1.0D) * 135.0D;
                ringSpeed = 10.0D + level.random.nextDouble() * 5.0D;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putLong(TAG_LEGACY_MAX_POWER, energy.getMaxPower());
        inputTank.writeToNbt(tag, TAG_INPUT_TANK);
        outputTank.writeToNbt(tag, TAG_OUTPUT_TANK);
        tag.putBoolean(TAG_DID_PROCESS, didProcess);
        tag.putDouble(TAG_RING, ring);
        tag.putDouble(TAG_RING_TARGET, ringTarget);
        tag.putDouble(TAG_RING_SPEED, ringSpeed);
        tag.putInt(TAG_RING_DELAY, ringDelay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        } else if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        if (tag.contains(TAG_INPUT_TANK)) {
            inputTank.readFromNbt(tag, TAG_INPUT_TANK);
        }
        if (tag.contains(TAG_OUTPUT_TANK)) {
            outputTank.readFromNbt(tag, TAG_OUTPUT_TANK);
        }
        didProcess = tag.getBoolean(TAG_DID_PROCESS);
        ring = tag.getDouble(TAG_RING);
        prevRing = ring;
        ringTarget = tag.getDouble(TAG_RING_TARGET);
        ringSpeed = tag.getDouble(TAG_RING_SPEED);
        ringDelay = tag.getInt(TAG_RING_DELAY);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.machineAssemblyMachine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
        fluidHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private class AssemblyFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            HbmFluidTank hbmTank = getTank(tank);
            return hbmTank == null ? FluidStack.EMPTY : HbmFluidForgeMappings.toForge(hbmTank.getTankType(), hbmTank.getFill());
        }

        @Override
        public int getTankCapacity(int tank) {
            HbmFluidTank hbmTank = getTank(tank);
            return hbmTank == null ? 0 : hbmTank.getMaxFill();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            if (tank != 0 || stack == null || stack.isEmpty()) {
                return false;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(stack);
            return type != HbmFluids.NONE && inputTank.canAccept(type, 0);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(resource);
            if (type == HbmFluids.NONE) {
                return 0;
            }
            int filled = inputTank.fill(type, resource.getAmount(), 0, action.simulate());
            if (!action.simulate() && filled > 0) {
                onFluidContentsChanged();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(resource);
            if (type == HbmFluids.NONE || outputTank.getTankType() != type) {
                return FluidStack.EMPTY;
            }
            int drained = outputTank.drain(resource.getAmount(), action.simulate());
            if (!action.simulate() && drained > 0) {
                onFluidContentsChanged();
            }
            return drained <= 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), drained);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || outputTank.isEmpty() || !HbmFluidForgeMappings.canExport(outputTank.getTankType())) {
                return FluidStack.EMPTY;
            }
            FluidType type = outputTank.getTankType();
            int drained = outputTank.drain(maxDrain, action.simulate());
            if (!action.simulate() && drained > 0) {
                onFluidContentsChanged();
            }
            return HbmFluidForgeMappings.toForge(type, drained);
        }

        @Nullable
        private HbmFluidTank getTank(int tank) {
            return tank == 0 ? inputTank : tank == 1 ? outputTank : null;
        }
    }

    public static class AssemblerArm {
        private static final double[][] POSITIONS = new double[][] {
                {45.0D, -15.0D, -5.0D},
                {15.0D, 15.0D, -15.0D},
                {25.0D, 10.0D, -15.0D},
                {30.0D, 0.0D, -10.0D},
                {70.0D, -10.0D, -25.0D}
        };

        private final java.util.Random random;
        private final double[] angles = new double[4];
        private final double[] prevAngles = new double[4];
        private final double[] targetAngles = new double[4];
        private final double[] speed = new double[4];
        private ArmActionState state = ArmActionState.ASSUME_POSITION;
        private int actionDelay;

        private AssemblerArm(long seed) {
            this.random = new java.util.Random(seed);
            resetSpeed();
        }

        private void updateInterp() {
            System.arraycopy(angles, 0, prevAngles, 0, angles.length);
        }

        private void returnToNullPos() {
            for (int i = 0; i < 4; i++) {
                targetAngles[i] = 0.0D;
            }
            for (int i = 0; i < 3; i++) {
                speed[i] = 3.0D;
            }
            speed[3] = 0.25D;
            state = ArmActionState.RETRACT_STRIKER;
            move();
        }

        private void updateArm() {
            resetSpeed();
            if (actionDelay > 0) {
                actionDelay--;
                return;
            }
            switch (state) {
                case ASSUME_POSITION -> {
                    if (move()) {
                        actionDelay = 2;
                        state = ArmActionState.EXTEND_STRIKER;
                        targetAngles[3] = -0.75D;
                    }
                }
                case EXTEND_STRIKER -> {
                    if (move()) {
                        state = ArmActionState.RETRACT_STRIKER;
                        targetAngles[3] = 0.0D;
                    }
                }
                case RETRACT_STRIKER -> {
                    if (move()) {
                        actionDelay = 2 + random.nextInt(5);
                        chooseNewArmPosition();
                        state = ArmActionState.ASSUME_POSITION;
                    }
                }
            }
        }

        private void resetSpeed() {
            speed[0] = 15.0D;
            speed[1] = 15.0D;
            speed[2] = 15.0D;
            speed[3] = 0.5D;
        }

        private void chooseNewArmPosition() {
            int chosen = random.nextInt(POSITIONS.length);
            targetAngles[0] = POSITIONS[chosen][0];
            targetAngles[1] = POSITIONS[chosen][1];
            targetAngles[2] = POSITIONS[chosen][2];
        }

        private boolean move() {
            boolean didMove = false;
            for (int i = 0; i < angles.length; i++) {
                if (angles[i] == targetAngles[i]) {
                    continue;
                }
                didMove = true;
                double delta = Math.abs(angles[i] - targetAngles[i]);
                if (delta <= speed[i]) {
                    angles[i] = targetAngles[i];
                } else if (angles[i] < targetAngles[i]) {
                    angles[i] += speed[i];
                } else {
                    angles[i] -= speed[i];
                }
            }
            return !didMove;
        }

        public double[] getPositions(float partialTick) {
            return new double[] {
                    Mth.lerp(partialTick, prevAngles[0], angles[0]),
                    Mth.lerp(partialTick, prevAngles[1], angles[1]),
                    Mth.lerp(partialTick, prevAngles[2], angles[2]),
                    Mth.lerp(partialTick, prevAngles[3], angles[3])
            };
        }

        private enum ArmActionState {
            ASSUME_POSITION,
            EXTEND_STRIKER,
            RETRACT_STRIKER
        }
    }
}
