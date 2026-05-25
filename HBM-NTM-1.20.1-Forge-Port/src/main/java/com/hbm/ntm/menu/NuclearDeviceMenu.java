package com.hbm.ntm.menu;

import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.blockentity.NuclearDeviceBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class NuclearDeviceMenu extends AbstractContainerMenu {
    private final NuclearDeviceBlockEntity blockEntity;
    private final Layout layout;
    private final int playerInventoryStart;
    private final int playerInventoryEnd;
    private final int hotbarEnd;

    public NuclearDeviceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public NuclearDeviceMenu(int containerId, Inventory playerInventory, NuclearDeviceBlockEntity blockEntity) {
        super(ModMenuTypes.NUCLEAR_DEVICE.get(), containerId);
        this.blockEntity = blockEntity;
        this.layout = Layout.forKind(blockEntity.kind());

        for (SlotPos slot : layout.deviceSlots()) {
            addSlot(new SlotItemHandler(blockEntity.getItems(), slot.index(), slot.x(), slot.y()));
        }
        this.playerInventoryStart = layout.deviceSlots().length;
        this.playerInventoryEnd = playerInventoryStart + 27;
        this.hotbarEnd = playerInventoryEnd + 9;
        addPlayerInventory(playerInventory);
    }

    public NuclearDeviceBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public Layout layout() {
        return layout;
    }

    public NuclearDeviceBlock.Kind kind() {
        return blockEntity.kind();
    }

    public boolean isReady() {
        return blockEntity.isReady();
    }

    public boolean isFilled() {
        return blockEntity.isFilled();
    }

    public boolean hasComponent(int slot) {
        return slot >= 0 && slot < blockEntity.getItems().getSlots() && !blockEntity.getItems().getStackInSlot(slot).isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < playerInventoryStart) {
                if (!moveItemStackTo(stack, playerInventoryStart, hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        layout.playerInventoryX() + column * 18,
                        layout.playerInventoryY() + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, layout.playerInventoryX() + column * 18, layout.hotbarY()));
        }
    }

    private static NuclearDeviceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof NuclearDeviceBlockEntity device) {
            return device;
        }
        throw new IllegalStateException("Expected nuclear device block entity at " + pos);
    }

    public record SlotPos(int index, int x, int y) {
    }

    public record Layout(int imageWidth, int imageHeight, int playerInventoryX, int playerInventoryY, int hotbarY,
            String texturePath, SlotPos[] deviceSlots) {
        public static Layout forKind(NuclearDeviceBlock.Kind kind) {
            return switch (kind) {
                case GADGET -> new Layout(176, 166, 8, 84, 142, "gadgetSchematic", new SlotPos[]{
                        new SlotPos(0, 26, 35), new SlotPos(1, 8, 17), new SlotPos(2, 44, 17),
                        new SlotPos(3, 8, 53), new SlotPos(4, 44, 53), new SlotPos(5, 98, 35)});
                case BOY -> new Layout(176, 222, 8, 140, 198, "lilBoySchematic", new SlotPos[]{
                        new SlotPos(0, 26, 36), new SlotPos(1, 44, 36), new SlotPos(2, 62, 36),
                        new SlotPos(3, 80, 36), new SlotPos(4, 98, 36)});
                case MAN -> new Layout(176, 166, 8, 84, 142, "fatManSchematic", new SlotPos[]{
                        new SlotPos(0, 26, 35), new SlotPos(1, 8, 17), new SlotPos(2, 44, 17),
                        new SlotPos(3, 8, 53), new SlotPos(4, 44, 53), new SlotPos(5, 98, 35)});
                case TSAR -> new Layout(256, 233, 48, 151, 209, "tsarBombaSchematic", new SlotPos[]{
                        new SlotPos(0, 48, 101), new SlotPos(1, 66, 101), new SlotPos(2, 84, 101),
                        new SlotPos(3, 102, 101), new SlotPos(4, 55, 51), new SlotPos(5, 138, 101)});
                case MIKE -> new Layout(176, 217, 8, 135, 193, "ivyMikeSchematic", new SlotPos[]{
                        new SlotPos(0, 26, 83), new SlotPos(1, 26, 101), new SlotPos(2, 44, 83),
                        new SlotPos(3, 44, 101), new SlotPos(4, 39, 35), new SlotPos(5, 98, 91),
                        new SlotPos(6, 116, 91), new SlotPos(7, 134, 91)});
                case PROTOTYPE -> new Layout(176, 166, 8, 84, 142, "gui_prototype", new SlotPos[]{
                        new SlotPos(0, 8, 35), new SlotPos(1, 26, 35), new SlotPos(2, 44, 26),
                        new SlotPos(3, 44, 44), new SlotPos(4, 62, 26), new SlotPos(5, 62, 44),
                        new SlotPos(6, 80, 26), new SlotPos(7, 80, 44), new SlotPos(8, 98, 26),
                        new SlotPos(9, 98, 44), new SlotPos(10, 116, 26), new SlotPos(11, 116, 44),
                        new SlotPos(12, 134, 35), new SlotPos(13, 152, 35)});
                case FLEIJA -> new Layout(176, 222, 8, 140, 198, "fleijaSchematic", new SlotPos[]{
                        new SlotPos(0, 8, 36), new SlotPos(1, 152, 36), new SlotPos(2, 44, 18),
                        new SlotPos(3, 44, 36), new SlotPos(4, 44, 54), new SlotPos(5, 80, 18),
                        new SlotPos(6, 98, 18), new SlotPos(7, 80, 36), new SlotPos(8, 98, 36),
                        new SlotPos(9, 80, 54), new SlotPos(10, 98, 54)});
                case SOLINIUM -> new Layout(176, 222, 8, 140, 198, "soliniumSchematic", new SlotPos[]{
                        new SlotPos(0, 26, 18), new SlotPos(1, 53, 18), new SlotPos(2, 107, 18),
                        new SlotPos(3, 134, 18), new SlotPos(4, 80, 36), new SlotPos(5, 26, 54),
                        new SlotPos(6, 53, 54), new SlotPos(7, 107, 54), new SlotPos(8, 134, 54)});
                case N2 -> new Layout(176, 222, 8, 140, 198, "n2Schematic", new SlotPos[]{
                        new SlotPos(0, 98, 36), new SlotPos(1, 116, 36), new SlotPos(2, 134, 36),
                        new SlotPos(3, 98, 54), new SlotPos(4, 116, 54), new SlotPos(5, 134, 54),
                        new SlotPos(6, 98, 72), new SlotPos(7, 116, 72), new SlotPos(8, 134, 72),
                        new SlotPos(9, 98, 90), new SlotPos(10, 116, 90), new SlotPos(11, 134, 90)});
            };
        }
    }
}
