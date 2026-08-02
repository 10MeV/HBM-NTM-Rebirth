package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.common.CopiableSettings;
import com.hbm.ntm.api.redstoneoverradio.RTTYCounterState;
import com.hbm.ntm.api.redstoneoverradio.RTTYPatternMatcher;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchCounterBlockEntity extends RadioTorchBlockEntity implements CopiableSettings {
    public static final int FILTER_SLOT_COUNT = RTTYCounterState.SLOT_COUNT;
    private static final String TAG_FILTER_ITEMS = "FilterItems";
    private static final String TAG_SETTINGS_ITEMS = "items";
    private static final String TAG_SETTINGS_SLOT = "slot";

    private final RTTYCounterState radio = new RTTYCounterState();
    private final RTTYPatternMatcher matcher = new RTTYPatternMatcher(FILTER_SLOT_COUNT);
    private boolean suppressFilterModeInit;
    private final ItemStackHandler filterItems = new ItemStackHandler(FILTER_SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!suppressFilterModeInit) {
                matcher.initPatternStandard(getStackInSlot(slot), slot);
            }
            setChangedAndSync(false);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            ItemStack result = super.insertItem(slot, stack, simulate);
            if (!simulate && !getStackInSlot(slot).isEmpty()) {
                setStackInSlot(slot, HbmItemStackUtil.carefulCopyWithSize(getStackInSlot(slot), 1));
            }
            return result;
        }
    };

    public RadioTorchCounterBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RADIO_TORCH_COUNTER.get(), pos, state);
    }

    public RadioTorchCounterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RTTYCounterState counterState() {
        return radio;
    }

    public ItemStackHandler getFilterItems() {
        return filterItems;
    }

    public String filterModeLabel(int slot) {
        return matcher.label(slot);
    }

    public void nextFilterMode(int slot) {
        if (slot < 0 || slot >= FILTER_SLOT_COUNT || filterItems.getStackInSlot(slot).isEmpty()) {
            return;
        }
        matcher.nextMode(filterItems.getStackInSlot(slot), slot);
        setChangedAndSync(false);
    }

    /**
     * Legacy {@code IControlReceiverFilter} settings-tool contract for the
     * three counter filters.  It stores only non-empty filters as relative
     * slots and deliberately advances the target's matcher mode after each
     * raw slot write instead of serializing the source matcher state.
     */
    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        ListTag copied = new ListTag();
        for (int slot = 0; slot < FILTER_SLOT_COUNT; slot++) {
            ItemStack filter = filterItems.getStackInSlot(slot);
            if (filter.isEmpty()) {
                continue;
            }
            CompoundTag slotTag = new CompoundTag();
            slotTag.putByte(TAG_SETTINGS_SLOT, (byte) slot);
            filter.save(slotTag);
            copied.add(slotTag);
        }
        tag.put(TAG_SETTINGS_ITEMS, copied);
        return tag;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, net.minecraft.world.entity.player.Player player,
            BlockPos pos) {
        if (tag == null) {
            return;
        }
        ListTag copied = tag.getList(TAG_SETTINGS_ITEMS, Tag.TAG_COMPOUND);
        boolean changed = false;
        for (int entry = 0; entry < copied.size(); entry++) {
            CompoundTag slotTag = copied.getCompound(entry);
            int slot = slotTag.getByte(TAG_SETTINGS_SLOT);
            ItemStack filter = ItemStack.of(slotTag);
            if (slot < 0 || slot >= FILTER_SLOT_COUNT || filter.isEmpty()) {
                continue;
            }
            suppressFilterModeInit = true;
            try {
                filterItems.setStackInSlot(slot, filter);
            } finally {
                suppressFilterModeInit = false;
            }
            matcher.nextMode(filter, slot);
            changed = true;
        }
        if (changed) {
            setChangedAndSync(false);
        }
    }

    @Override
    public List<Component> infoForDisplay(Level level, BlockPos pos) {
        return List.of(Component.translatable("copytool.filter"));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioTorchCounterBlockEntity torch) {
        BlockEntity attached = torch.attachedBlockEntity(level);
        if (attached != null) {
            attached.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                int[] counts = new int[RTTYCounterState.SLOT_COUNT];
                for (int i = 0; i < counts.length; i++) {
                    ItemStack pattern = torch.filterItems.getStackInSlot(i);
                    counts[i] = pattern.isEmpty() ? 0 : countItems(handler, pattern, i, torch.matcher);
                }
                if (torch.radio.broadcastCounts(level, counts) > 0) {
                    torch.setChangedAndSync(false);
                }
            });
        }
        torch.networkPackLegacyRadioTorch();
    }

    @Override
    protected int legacyNetworkPackRange() {
        return 15;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        for (int i = 0; i < RTTYCounterState.SLOT_COUNT; i++) {
            if (!radio.channel(i).isEmpty()) {
                lines.add(LegacyLookOverlayLines.freq(i + 1, radio.channel(i)));
                lines.add(LegacyLookOverlayLines.signal(i + 1, radio.lastCount(i)));
            }
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    public boolean applyRadioConfiguration(CompoundTag tag) {
        return finishRadioConfiguration(radio.applyControl(tag), false);
    }

    @Override
    public List<Component> describeRadioConfiguration() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("polling=" + radio.polling()));
        for (int i = 0; i < RTTYCounterState.SLOT_COUNT; i++) {
            if (!radio.channel(i).isEmpty() || radio.lastCount(i) != 0) {
                lines.add(Component.literal("slot " + i
                        + ": c" + i + "=" + radio.channel(i)
                        + " last=" + radio.lastCount(i)
                        + " filter=" + filterItems.getStackInSlot(i).getHoverName().getString()
                        + " mode=" + matcher.label(i)));
            }
        }
        return lines;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        radio.save(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_FILTER_ITEMS, filterItems);
        matcher.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        radio.load(tag);
        HbmInventoryMenuHelper.loadLegacyItems(tag, TAG_FILTER_ITEMS, filterItems);
        matcher.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return new CompoundTag();
}

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        // TileEntityRadioTorchCounter#serialize: TileEntityMachineBase's
        // LoadedBase flags, then counter state, matcher modes and channels.
        writeLegacyLoadedTileBinary(data);
        radio.writeLegacyWireHead(data);
        matcher.writeLegacyWire(data);
        radio.writeLegacyWireChannels(data);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        radio.readLegacyWireHead(data);
        matcher.readLegacyWire(data);
        radio.readLegacyWireChannels(data);
    }

    private static int countItems(IItemHandler handler, ItemStack pattern, int filterSlot, RTTYPatternMatcher matcher) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && matcher.isValidForFilter(pattern, filterSlot, stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

}
