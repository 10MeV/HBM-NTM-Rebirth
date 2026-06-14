package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RustedLaunchPadBlockEntity;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class RustedLaunchPadMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = RustedLaunchPadBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final RustedLaunchPadBlockEntity blockEntity;

    public RustedLaunchPadMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RustedLaunchPadMenu(int containerId, Inventory playerInventory, RustedLaunchPadBlockEntity blockEntity) {
        super(ModMenuTypes.LAUNCH_PAD_RUSTED.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                RustedLaunchPadBlockEntity.SLOT_RELEASED_MISSILE, 26, 72));
        addSlot(new SlotItemHandler(blockEntity.getItems(), RustedLaunchPadBlockEntity.SLOT_CODE, 116, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.LAUNCH_CODE.get());
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getItems(), RustedLaunchPadBlockEntity.SLOT_KEY, 134, 45) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.LAUNCH_KEY.get());
            }
        });
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(),
                RustedLaunchPadBlockEntity.SLOT_DESIGNATOR, 26, 99));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 154, 212);
    }

    public RustedLaunchPadBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isMissileLoaded() {
        return blockEntity.isMissileLoaded();
    }

    public boolean hasLaunchCode() {
        return blockEntity.hasLaunchCode();
    }

    public boolean hasLaunchKey() {
        return blockEntity.hasLaunchKey();
    }

    public int launchCodeNumber() {
        BlockPos pos = blockEntity.getBlockPos();
        return new java.util.Random(pos.getX() * 131_071L + pos.getZ()).nextInt(100_000_000);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                RustedLaunchPadBlockEntity.SLOT_CODE, RustedLaunchPadBlockEntity.SLOT_CODE + 1,
                RustedLaunchPadBlockEntity.SLOT_KEY, RustedLaunchPadBlockEntity.SLOT_KEY + 1,
                RustedLaunchPadBlockEntity.SLOT_DESIGNATOR, RustedLaunchPadBlockEntity.SLOT_DESIGNATOR + 1);
    }

    private static RustedLaunchPadBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RustedLaunchPadBlockEntity launchPad) {
            return launchPad;
        }
        throw new IllegalStateException("Expected rusted launch pad block entity at " + pos);
    }
}
