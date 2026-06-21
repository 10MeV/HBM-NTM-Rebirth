package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BalefireBombBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class BalefireBombMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 222;
    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 140;
    public static final int HOTBAR_Y = 198;

    @Nullable
    private final BalefireBombBlockEntity blockEntity;
    private final BlockPos blockPos;
    private final ItemStackHandler items;
    private final int playerInventoryStart;
    private final int hotbarEnd;
    private int timer;
    private boolean started;
    private boolean loaded;

    public BalefireBombMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, sourceFromClient(inventory, data));
    }

    public BalefireBombMenu(int containerId, Inventory inventory, BalefireBombBlockEntity blockEntity) {
        this(containerId, inventory, new MenuSource(blockEntity, blockEntity.getBlockPos(), blockEntity.getItems()));
    }

    private BalefireBombMenu(int containerId, Inventory inventory, MenuSource source) {
        super(ModMenuTypes.BALEFIRE_BOMB.get(), containerId);
        this.blockEntity = source.blockEntity();
        this.blockPos = source.blockPos();
        this.items = source.items();

        addSlot(new SlotItemHandler(items, 0, 17, 36));
        addSlot(new SlotItemHandler(items, 1, 53, 36));

        this.playerInventoryStart = BalefireBombBlockEntity.SLOT_COUNT;
        addPlayerInventory(inventory);
        this.hotbarEnd = slots.size();

        HbmMenuDataSlots.addInt(this::addDataSlot, this::serverTimer, value -> timer = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, this::serverStarted, value -> started = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, this::serverLoaded, value -> loaded = value);
    }

    @Nullable
    public BalefireBombBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getTimer() {
        return hasServerBlockEntity() ? blockEntity.getTimer() : timer;
    }

    public int getTimerSeconds() {
        return Math.max(0, getTimer() / 20);
    }

    public boolean isStarted() {
        return hasServerBlockEntity() ? blockEntity.isStarted() : started;
    }

    public boolean isLoaded() {
        return hasServerBlockEntity() ? blockEntity.isLoadedSynced() : loaded;
    }

    public boolean hasEgg() {
        return blockEntity != null ? blockEntity.hasEgg() : isLegacyItem(items.getStackInSlot(0), "egg_balefire");
    }

    public boolean hasBattery() {
        return getBattery() > 0;
    }

    public int getBattery() {
        if (blockEntity != null) {
            return blockEntity.getBattery();
        }
        if (isLegacyItem(items.getStackInSlot(1), "battery_spark")) {
            return 1;
        }
        if (isLegacyItem(items.getStackInSlot(1), "battery_trixite")) {
            return 2;
        }
        return 0;
    }

    public String getMinutesText() {
        int minutes = Math.max(0, getTimer()) / 1200;
        return minutes < 10 ? "0" + minutes : Integer.toString(minutes);
    }

    public String getSecondsText() {
        int seconds = (Math.max(0, getTimer()) / 20) % 60;
        return seconds < 10 ? "0" + seconds : Integer.toString(seconds);
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity != null && blockEntity.getLevel() != null && blockEntity.isRemoved()) {
            return false;
        }
        return player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < playerInventoryStart) {
            if (!moveItemStackTo(stack, playerInventoryStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
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

    private int serverTimer() {
        return blockEntity == null ? timer : blockEntity.getTimer();
    }

    private boolean serverStarted() {
        return blockEntity == null ? started : blockEntity.isStarted();
    }

    private boolean serverLoaded() {
        return blockEntity == null ? loaded : blockEntity.isLoadedSynced();
    }

    private boolean hasServerBlockEntity() {
        return blockEntity != null && blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide();
    }

    private static MenuSource sourceFromClient(Inventory inventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof BalefireBombBlockEntity balefireBomb) {
            return new MenuSource(balefireBomb, pos, balefireBomb.getItems());
        }
        return new MenuSource(null, pos, new ItemStackHandler(BalefireBombBlockEntity.SLOT_COUNT));
    }

    private static boolean isLegacyItem(ItemStack stack, String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item != null && !stack.isEmpty() && stack.is(item.get());
    }

    private record MenuSource(@Nullable BalefireBombBlockEntity blockEntity, BlockPos blockPos,
            ItemStackHandler items) {
    }
}
