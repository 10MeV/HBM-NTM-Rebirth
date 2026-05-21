package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
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
import net.minecraftforge.items.ItemStackHandler;
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

    private final ItemStackHandler items = new ItemStackHandler(17) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
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
