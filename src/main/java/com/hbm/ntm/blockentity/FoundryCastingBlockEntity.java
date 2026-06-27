package com.hbm.ntm.blockentity;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class FoundryCastingBlockEntity extends FoundryBaseBlockEntity implements LegacyLookOverlayProvider {
    private static final int FOUNDRY_TITLE_COLOR = 0xFF4000;
    private static final int FOUNDRY_TITLE_SHADOW_COLOR = 0x401000;
    public static final int SLOT_MOLD = 0;
    public static final int SLOT_OUTPUT = 1;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_COOLOFF = "cooloff";

    private final int moldSize;
    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndUpdate();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_MOLD && FoundryMoldItem.getMold(stack) != null;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private int cooloff = 100;

    public FoundryCastingBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state, int moldSize) {
        super(entityType, pos, state);
        this.moldSize = moldSize;
    }

    public static FoundryCastingBlockEntity mold(BlockPos pos, BlockState state) {
        return new FoundryCastingBlockEntity(ModBlockEntities.FOUNDRY_MOLD.get(), pos, state, 0);
    }

    public static FoundryCastingBlockEntity basin(BlockPos pos, BlockState state) {
        return new FoundryCastingBlockEntity(ModBlockEntities.FOUNDRY_BASIN.get(), pos, state, 1);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryCastingBlockEntity casting) {
        if (level.isClientSide) {
            return;
        }
        int oldAmount = casting.amount;
        Object oldType = casting.type;
        casting.tickServer();
        if (oldAmount != casting.amount || oldType != casting.type) {
            casting.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public IItemHandler getExternalItemHandler() {
        return items;
    }

    public ItemStack getMoldStack() {
        return items.getStackInSlot(SLOT_MOLD);
    }

    public ItemStack getOutputStack() {
        return items.getStackInSlot(SLOT_OUTPUT);
    }

    public int getMoldSize() {
        return moldSize;
    }

    public int getCooloff() {
        return cooloff;
    }

    public FoundryMoldItem.Mold getInstalledMold() {
        FoundryMoldItem.Mold mold = FoundryMoldItem.getMold(items.getStackInSlot(SLOT_MOLD));
        return mold != null && mold.size() == moldSize ? mold : null;
    }

    @Override
    public int getCapacity() {
        FoundryMoldItem.Mold mold = getInstalledMold();
        return mold == null ? 0 : mold.cost();
    }

    public boolean installMold(ItemStack stack) {
        FoundryMoldItem.Mold mold = FoundryMoldItem.getMold(stack);
        if (mold == null || mold.size() != moldSize || !items.getStackInSlot(SLOT_MOLD).isEmpty()) {
            return false;
        }
        items.setStackInSlot(SLOT_MOLD, stack.copyWithCount(1));
        return true;
    }

    public ItemStack removeMold() {
        if (amount > 0) {
            return ItemStack.EMPTY;
        }
        ItemStack mold = items.getStackInSlot(SLOT_MOLD);
        if (mold.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.setStackInSlot(SLOT_MOLD, ItemStack.EMPTY);
        return mold.copy();
    }

    public ItemStack removeOutput() {
        ItemStack out = items.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
        return out.copy();
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>(HbmInventoryMenuHelper.clearToDrops(items));
        ItemStack scrap = drainAsScrap();
        if (!scrap.isEmpty()) {
            drops.add(scrap);
        }
        return drops;
    }

    public double getMoltenLevel() {
        int capacity = getCapacity();
        if (capacity <= 0 || amount <= 0) {
            return 0.0D;
        }
        return moldSize == 0
                ? 0.125D + amount * 0.25D / capacity
                : 0.125D + amount * 0.75D / capacity;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        ItemStack moldStack = getMoldStack();
        if (moldStack.isEmpty()) {
            lines.add(Component.translatable("foundry.noCast").withStyle(ChatFormatting.RED));
        } else {
            FoundryMoldItem.Mold mold = FoundryMoldItem.getMold(moldStack);
            if (mold != null) {
                lines.add(mold.title().copy().withStyle(ChatFormatting.BLUE));
            }
        }
        if (type != null && amount > 0) {
            lines.add(Component.literal(type.names[0] + ": " + amount + " / " + getCapacity())
                    .withStyle(ChatFormatting.YELLOW));
        }
        return LegacyLookOverlay.withTitle(Component.translatable(getBlockState().getBlock().getDescriptionId()),
                FOUNDRY_TITLE_COLOR, FOUNDRY_TITLE_SHADOW_COLOR, lines);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        if (moldSize == 1) {
            return false;
        }
        return standardCastingCheck(stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return canAcceptPartialFlow(level, pos, side, stack) ? standardAdd(stack) : stack;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return side == Direction.UP && standardCastingCheck(stack);
    }

    private boolean standardCastingCheck(MaterialStack stack) {
        if (!standardCheck(stack) || !items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            return false;
        }
        FoundryMoldItem.Mold mold = getInstalledMold();
        return mold != null && !mold.getOutput(stack.material).isEmpty();
    }

    private void tickServer() {
        cleanupMaterial();
        FoundryMoldItem.Mold mold = getInstalledMold();
        if (mold != null && amount == getCapacity() && items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            cooloff--;
            if (cooloff <= 0) {
                ItemStack out = mold.getOutput(type);
                amount = 0;
                type = null;
                if (!out.isEmpty()) {
                    items.setStackInSlot(SLOT_OUTPUT, out.copy());
                }
                cooloff = 200;
                setChanged();
            }
        } else {
            cooloff = 200;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        tag.putInt(TAG_COOLOFF, cooloff);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        cooloff = tag.contains(TAG_COOLOFF) ? tag.getInt(TAG_COOLOFF) : 200;
    }
}
