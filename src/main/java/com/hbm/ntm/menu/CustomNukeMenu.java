package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.CustomNukeBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class CustomNukeMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 222;
    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 140;
    public static final int HOTBAR_Y = 198;

    @Nullable
    private final CustomNukeBlockEntity blockEntity;
    private final BlockPos blockPos;
    private final ItemStackHandler items;
    private final int playerInventoryStart;
    private final int hotbarEnd;
    private final StatsData statsData = new StatsData();

    public CustomNukeMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, sourceFromClient(inventory, data));
    }

    public CustomNukeMenu(int containerId, Inventory inventory, CustomNukeBlockEntity blockEntity) {
        this(containerId, inventory, new MenuSource(blockEntity, blockEntity.getBlockPos(), blockEntity.getItems()));
    }

    private CustomNukeMenu(int containerId, Inventory inventory, MenuSource source) {
        super(ModMenuTypes.CUSTOM_NUKE.get(), containerId);
        this.blockEntity = source.blockEntity();
        this.blockPos = source.blockPos();
        this.items = source.items();

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new SlotItemHandler(items, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }

        this.playerInventoryStart = CustomNukeBlockEntity.SLOT_COUNT;
        addPlayerInventory(inventory);
        this.hotbarEnd = slots.size();

        for (int i = 0; i < StatsData.COUNT; i++) {
            final int index = i;
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> statsData.get(index),
                    value -> statsData.set(index, value));
        }
    }

    @Override
    public void broadcastChanges() {
        updateStatsData();
        super.broadcastChanges();
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        updateStatsData();
        super.slotsChanged(container);
    }

    private void updateStatsData() {
        if (blockEntity == null) {
            return;
        }
        CustomNukeBlockEntity.CustomNukeStats stats = blockEntity.getStats();
        statsData.setStats(stats);
    }

    public CustomNukeBlockEntity.CustomNukeStats stats() {
        return statsData.toStats();
    }

    public boolean hasMainStage(int stage) {
        CustomNukeBlockEntity.CustomNukeStats stats = stats();
        return switch (stage) {
            case 0 -> stats.tnt() > 0.0F;
            case 1 -> stats.nuke() > 0.0F;
            case 2 -> stats.hydro() > 0.0F;
            case 3 -> stats.amat() > 0.0F;
            case 4 -> stats.schrab() > 0.0F;
            case 5 -> stats.euph() > 0.0F;
            default -> false;
        };
    }

    public boolean hasDirtyOverlay() {
        CustomNukeBlockEntity.CustomNukeStats stats = stats();
        return stats.dirty() > 0.0F && stats.nuke() > 0.0F
                && stats.amat() == 0.0F && stats.schrab() == 0.0F && stats.euph() == 0.0F;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity != null && blockEntity.getLevel() != null && blockEntity.isRemoved()) {
            return false;
        }
        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, playerInventoryStart,
                playerInventoryStart, hotbarEnd, 0, playerInventoryStart);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        PLAYER_INV_X + column * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PLAYER_INV_X + column * 18, HOTBAR_Y));
        }
    }

    private static MenuSource sourceFromClient(Inventory inventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof CustomNukeBlockEntity customNuke) {
            return new MenuSource(customNuke, pos, customNuke.getItems());
        }
        return new MenuSource(null, pos, new ItemStackHandler(CustomNukeBlockEntity.SLOT_COUNT));
    }

    private record MenuSource(@Nullable CustomNukeBlockEntity blockEntity, BlockPos blockPos, ItemStackHandler items) {
    }

    private static final class StatsData {
        private static final int SCALE = 10;
        private static final int COUNT = 8;
        private final int[] values = new int[COUNT];

        int get(int index) {
            return values[index];
        }

        void set(int index, int value) {
            values[index] = value;
        }

        void setStats(CustomNukeBlockEntity.CustomNukeStats stats) {
            values[0] = encode(stats.tnt());
            values[1] = encode(stats.nuke());
            values[2] = encode(stats.hydro());
            values[3] = encode(stats.amat());
            values[4] = encode(stats.dirty());
            values[5] = encode(stats.schrab());
            values[6] = encode(stats.euph());
            values[7] = stats.falling() ? 1 : 0;
        }

        CustomNukeBlockEntity.CustomNukeStats toStats() {
            return new CustomNukeBlockEntity.CustomNukeStats(
                    decode(values[0]), decode(values[1]), decode(values[2]), decode(values[3]),
                    decode(values[4]), decode(values[5]), decode(values[6]), values[7] != 0);
        }

        private static int encode(float value) {
            return Math.round(value * SCALE);
        }

        private static float decode(int value) {
            return value / (float) SCALE;
        }
    }
}
