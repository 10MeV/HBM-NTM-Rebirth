package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BombMultiBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
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

public class BombMultiMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 166;
    public static final int PLAYER_INV_X = 8;
    public static final int PLAYER_INV_Y = 84;
    public static final int HOTBAR_Y = 142;

    @Nullable
    private final BombMultiBlockEntity blockEntity;
    private final BlockPos blockPos;
    private final ItemStackHandler items;
    private final int playerInventoryStart;
    private final int hotbarEnd;

    public BombMultiMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, sourceFromClient(inventory, data));
    }

    public BombMultiMenu(int containerId, Inventory inventory, BombMultiBlockEntity blockEntity) {
        this(containerId, inventory, new MenuSource(blockEntity, blockEntity.getBlockPos(), blockEntity.getItems()));
    }

    private BombMultiMenu(int containerId, Inventory inventory, MenuSource source) {
        super(ModMenuTypes.BOMB_MULTI.get(), containerId);
        this.blockEntity = source.blockEntity();
        this.blockPos = source.blockPos();
        this.items = source.items();

        addSlot(new SlotItemHandler(items, 0, 44, 26));
        addSlot(new SlotItemHandler(items, 1, 62, 26));
        addSlot(new SlotItemHandler(items, 2, 80, 26));
        addSlot(new SlotItemHandler(items, 3, 44, 44));
        addSlot(new SlotItemHandler(items, 4, 62, 44));
        addSlot(new SlotItemHandler(items, 5, 80, 44));

        this.playerInventoryStart = BombMultiBlockEntity.SLOT_COUNT;
        addPlayerInventory(inventory);
        this.hotbarEnd = slots.size();
    }

    public int modifierOverlayType() {
        int type2 = modifierType(2);
        int type5 = modifierType(5);
        if (type2 == 0 && type5 == 0) {
            return 0;
        }
        return type2 == type5 ? type2 : 7;
    }

    private int modifierType(int slot) {
        if (blockEntity != null) {
            return blockEntity.modifierType(slot);
        }
        return BombMultiClientLogic.modifierType(items.getStackInSlot(slot));
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

    private static MenuSource sourceFromClient(Inventory inventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof BombMultiBlockEntity bombMulti) {
            return new MenuSource(bombMulti, pos, bombMulti.getItems());
        }
        return new MenuSource(null, pos, new ItemStackHandler(BombMultiBlockEntity.SLOT_COUNT));
    }

    private record MenuSource(@Nullable BombMultiBlockEntity blockEntity, BlockPos blockPos, ItemStackHandler items) {
    }

    private static final class BombMultiClientLogic {
        private static int modifierType(ItemStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            if (stack.is(net.minecraft.world.item.Items.GUNPOWDER)) {
                return 1;
            }
            if (stack.is(net.minecraft.world.level.block.Blocks.TNT.asItem())) {
                return 2;
            }
            if (isLegacyItem(stack, "pellet_cluster")) {
                return 3;
            }
            if (isLegacyItem(stack, "powder_fire")) {
                return 4;
            }
            if (isLegacyItem(stack, "powder_poison")) {
                return 5;
            }
            if (isLegacyItem(stack, "pellet_gas")) {
                return 6;
            }
            return 0;
        }

        private static boolean isLegacyItem(ItemStack stack, String name) {
            net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> item =
                    com.hbm.ntm.registry.ModItems.legacyItem(name);
            return item != null && stack.is(item.get());
        }
    }
}
