package com.hbm.ntm.blockentity;

import com.hbm.ntm.item.SirenCassetteItem;
import com.hbm.ntm.menu.SirenMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySirenTrack;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SirenBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_CASSETTE = 0;
    private static final int SLOT_COUNT = 1;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_NAME = "name";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> externalItemHandler = LazyOptional.of(() -> new SirenExternalItemHandler(items));
    private boolean lock;
    private String customName;

    public SirenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SIREN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SirenBlockEntity siren) {
        LegacySirenTrack track = siren.getCurrentTrack();
        if (track == LegacySirenTrack.NULL) {
            ModMessages.sendSirenEvent(level, pos, track.id(), false);
            return;
        }

        boolean active = level.hasNeighborSignal(pos);
        if (track.type() == LegacySirenTrack.SoundType.LOOP) {
            ModMessages.sendSirenEvent(level, pos, track.id(), active);
            return;
        }

        if (!siren.lock && active) {
            siren.lock = true;
            ModMessages.sendSirenEvent(level, pos, track.id(), false);
            ModMessages.sendSirenEvent(level, pos, track.id(), true);
        }
        if (siren.lock && !active) {
            siren.lock = false;
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public IItemHandler getMenuItemHandler() {
        return items;
    }

    public LegacySirenTrack getCurrentTrack() {
        return SirenCassetteItem.track(items.getStackInSlot(SLOT_CASSETTE));
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isEmpty()
                ? Component.literal(customName)
                : Component.translatable("container.siren");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SirenMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmItemStackUtil.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        if (customName != null && !customName.isEmpty()) {
            tag.putString(TAG_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        customName = tag.contains(TAG_NAME, Tag.TAG_STRING) ? tag.getString(TAG_NAME) : null;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        externalItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return externalItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private static final class SirenExternalItemHandler implements IItemHandler {
        private final ItemStackHandler items;

        private SirenExternalItemHandler(ItemStackHandler items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == SLOT_CASSETTE ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
