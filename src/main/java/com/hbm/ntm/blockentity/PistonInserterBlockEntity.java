package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.IInsertable;
import com.hbm.ntm.block.PistonInserterBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Exact one-slot, redstone-edge-driven carrier for legacy TileEntityPistonInserter. */
public class PistonInserterBlockEntity extends BlockEntity {
    public static final int MAX_EXTEND = 25;

    private ItemStack slot = ItemStack.EMPTY;
    private int extend;
    private boolean retracting = true;
    private int delay;
    private boolean lastState;

    private double renderExtend;
    private double lastExtend;
    private int syncExtend;
    private int turnProgress;
    /**
     * Legacy TileEntityPistonInserter is an unrestricted one-slot IInventory:
     * pipes/hoppers may insert or extract from every side, independently of
     * the front-face player's retract-only ejection rule.
     */
    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slotIndex) {
            return slotIndex == 0 ? slot : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slotIndex, @NotNull ItemStack stack, boolean simulate) {
            if (slotIndex != 0 || stack.isEmpty() || !slot.isEmpty()) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                slot = stack.copyWithCount(1);
                sync();
            }
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slotIndex, int amount, boolean simulate) {
            if (slotIndex != 0 || amount <= 0 || slot.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = slot.copyWithCount(1);
            if (!simulate) {
                slot = ItemStack.EMPTY;
                sync();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slotIndex) {
            return slotIndex == 0 ? 1 : 0;
        }

        @Override
        public boolean isItemValid(int slotIndex, @NotNull ItemStack stack) {
            return slotIndex == 0;
        }
    });

    public PistonInserterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PISTON_INSERTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PistonInserterBlockEntity piston) {
        boolean changed = false;
        if (piston.delay <= 0) {
            if (piston.retracting && piston.extend > 0) {
                piston.extend--;
                changed = true;
            } else if (!piston.retracting) {
                piston.extend++;
                changed = true;
                if (piston.extend >= MAX_EXTEND) {
                    level.playSound(null, pos, ModSounds.BLOCK_PRESS_OPERATE.get(),
                            net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.5F);
                    Direction facing = piston.facing();
                    BlockPos target = pos.relative(facing, 2);
                    Block targetBlock = level.getBlockState(target).getBlock();
                    if (!piston.slot.isEmpty() && targetBlock instanceof IInsertable insertable
                            && insertable.insertItem(level, target, facing, piston.slot.copyWithCount(1))) {
                        piston.slot.shrink(1);
                    }
                    piston.retracting = true;
                    piston.delay = 5;
                    changed = true;
                }
            }
        } else {
            piston.delay--;
            changed = true;
        }
        if (changed) {
            piston.sync();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PistonInserterBlockEntity piston) {
        piston.lastExtend = piston.renderExtend;
        if (piston.turnProgress > 0) {
            piston.renderExtend += (piston.syncExtend - piston.renderExtend) / piston.turnProgress;
            piston.turnProgress--;
        } else {
            piston.renderExtend = piston.syncExtend;
        }
    }

    public Direction facing() {
        return getBlockState().getValue(PistonInserterBlock.FACING);
    }

    public void updateRedstoneState(boolean powered) {
        if (powered && !lastState && extend <= 0) {
            retracting = false;
            sync();
        }
        lastState = powered;
        setChanged();
    }

    public ItemStack getSlot() {
        return slot;
    }

    public boolean loadOne(ItemStack held) {
        if (!slot.isEmpty() || held.isEmpty()) {
            return false;
        }
        slot = held.copyWithCount(1);
        sync();
        return true;
    }

    public ItemStack ejectSlot() {
        if (slot.isEmpty() || !retracting) {
            return ItemStack.EMPTY;
        }
        ItemStack result = slot;
        slot = ItemStack.EMPTY;
        sync();
        return result;
    }

    public double getInterpolatedExtend(float partialTick) {
        return lastExtend + (renderExtend - lastExtend) * partialTick;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("extend", extend);
        tag.putBoolean("retract", retracting);
        tag.putBoolean("state", lastState);
        if (!slot.isEmpty()) {
            tag.put("stack", slot.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        extend = tag.getInt("extend");
        retracting = tag.getBoolean("retract");
        lastState = tag.getBoolean("state");
        slot = tag.contains("stack") ? ItemStack.of(tag.getCompound("stack")) : ItemStack.EMPTY;
        syncExtend = extend;
    }

    @Override
    public CompoundTag getUpdateTag() {
        // TileEntityPistonInserter's runtime packet carried only the visible
        // piston position and its one displayed stack.  Keep the initial
        // chunk snapshot equally narrow: retract/delay/redstone are server
        // control state and must not be reconstructed on the client.
        CompoundTag tag = new CompoundTag();
        tag.putInt("extend", extend);
        if (!slot.isEmpty()) {
            tag.put("stack", slot.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        syncExtend = tag.getInt("extend");
        slot = tag.contains("stack") ? ItemStack.of(tag.getCompound("stack")) : ItemStack.EMPTY;
        turnProgress = 2;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net,
            ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) {
            handleUpdateTag(packet.getTag());
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(facing().getStepX(), facing().getStepY(), facing().getStepZ());
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}
