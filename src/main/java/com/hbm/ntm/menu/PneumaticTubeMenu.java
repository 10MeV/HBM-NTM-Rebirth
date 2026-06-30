package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PneumaticTubeBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.util.LegacyPatternMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class PneumaticTubeMenu extends AbstractContainerMenu {
    private static final int FILTER_SLOT_COUNT = PneumaticTubeBlockEntity.FILTER_SLOTS;
    private static final int PLAYER_INVENTORY_START = FILTER_SLOT_COUNT;
    private static final int HOTBAR_END = PLAYER_INVENTORY_START + 36;

    private final PneumaticTubeBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData tankData;
    private final int[] modeIndexes = new int[FILTER_SLOT_COUNT];
    private boolean whitelist;
    private boolean redstone;
    private int sendOrder;
    private int receiveOrder;

    public PneumaticTubeMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public PneumaticTubeMenu(int containerId, Inventory inventory, PneumaticTubeBlockEntity blockEntity) {
        super(ModMenuTypes.PNEUMATIC_TUBE.get(), containerId);
        this.blockEntity = blockEntity;
        addFilterSlots();
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 103, 161);
        this.tankData = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.compair());
        addDataSlots();
    }

    public PneumaticTubeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isEndpointOnly() {
        return !blockEntity.isCompressor() && blockEntity.isEndpoint();
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean isRedstoneEnabled() {
        return redstone;
    }

    public int getSendOrder() {
        return sendOrder;
    }

    public int getReceiveOrder() {
        return receiveOrder;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tankData;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tankData.tooltip(showHidden);
    }

    public int getRangeFromPressure() {
        return rangeFromPressure(tankData.pressure());
    }

    public Component getModeLabel(int slot) {
        if (slot < 0 || slot >= modeIndexes.length) {
            return Component.empty();
        }
        String mode = LegacyPatternMatcher.modeForIndex(blockEntity.getFilterStack(slot), modeIndexes[slot]);
        return LegacyPatternMatcher.label(mode).copy().withStyle(ChatFormatting.RED);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 128.0D);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (HbmInventoryMenuHelper.handleLegacyPatternSlotClick(slots, slotId, button, clickType,
                getCarried(), 0, FILTER_SLOT_COUNT, blockEntity::nextMode,
                blockEntity::updatePatternSlot, this::broadcastChanges)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private void addFilterSlots() {
        HbmInventoryMenuHelper.addPatternSlots(this::addSlot, blockEntity.getFilterItems(), 0,
                35, 17, 3, 5);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isWhitelist, value -> whitelist = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isRedstoneEnabled, value -> redstone = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSendOrder, value -> sendOrder = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getReceiveOrder, value -> receiveOrder = value);
        for (int slot = 0; slot < modeIndexes.length; slot++) {
            final int filterSlot = slot;
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getModeIndex(filterSlot),
                    value -> modeIndexes[filterSlot] = value);
        }
    }

    private static PneumaticTubeBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PneumaticTubeBlockEntity tube) {
            return tube;
        }
        throw new IllegalStateException("Expected pneumatic tube block entity at " + pos);
    }

    private static int rangeFromPressure(int pressure) {
        return switch (pressure) {
            case 1 -> 10;
            case 2 -> 25;
            case 3 -> 100;
            case 4 -> 250;
            case 5 -> 1_000;
            default -> 0;
        };
    }
}
