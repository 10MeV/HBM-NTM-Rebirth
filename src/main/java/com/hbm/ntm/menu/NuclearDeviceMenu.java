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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

public class NuclearDeviceMenu extends AbstractContainerMenu {
    @Nullable
    private final NuclearDeviceBlockEntity blockEntity;
    private final NuclearDeviceBlock.Kind kind;
    private final BlockPos blockPos;
    private final ItemStackHandler items;
    private final Layout layout;
    private final int playerInventoryStart;
    private final int playerInventoryEnd;
    private final int hotbarEnd;

    public NuclearDeviceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, readClientSource(playerInventory, data));
    }

    public NuclearDeviceMenu(int containerId, Inventory playerInventory, NuclearDeviceBlockEntity blockEntity) {
        this(containerId, playerInventory, MenuSource.fromBlockEntity(blockEntity));
    }

    private NuclearDeviceMenu(int containerId, Inventory playerInventory, MenuSource source) {
        super(ModMenuTypes.NUCLEAR_DEVICE.get(), containerId);
        this.blockEntity = source.blockEntity();
        this.kind = source.kind();
        this.blockPos = source.blockPos();
        this.items = source.items();
        this.layout = Layout.forKind(kind);

        for (SlotPos slot : layout.deviceSlots()) {
            addSlot(new SlotItemHandler(items, slot.index(), slot.x(), slot.y()));
        }
        this.playerInventoryStart = layout.deviceSlots().length;
        this.playerInventoryEnd = playerInventoryStart + 27;
        this.hotbarEnd = playerInventoryEnd + 9;
        addPlayerInventory(playerInventory);
    }

    @Nullable
    public NuclearDeviceBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public Layout layout() {
        return layout;
    }

    public NuclearDeviceBlock.Kind kind() {
        return kind;
    }

    public boolean isReady() {
        return NuclearDeviceBlockEntity.isReady(kind, items);
    }

    public boolean isFilled() {
        return NuclearDeviceBlockEntity.isFilled(kind, items);
    }

    public boolean hasComponent(int slot) {
        return slot >= 0 && slot < items.getSlots() && !items.getStackInSlot(slot).isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity != null && blockEntity.getLevel() != null && blockEntity.isRemoved()) {
            return false;
        }
        return player.distanceToSqr(
                blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D) <= 64.0D;
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

    private static MenuSource readClientSource(Inventory inventory, FriendlyByteBuf data) {
        BlockPos pos = data.readBlockPos();
        NuclearDeviceBlock.Kind payloadKind = readPayloadKind(inventory, data, pos);
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof NuclearDeviceBlockEntity device && device.kind() == payloadKind) {
            return MenuSource.fromBlockEntity(device);
        }
        return new MenuSource(null, payloadKind, pos, NuclearDeviceBlockEntity.createItemHandler(payloadKind));
    }

    private static NuclearDeviceBlock.Kind readPayloadKind(Inventory inventory, FriendlyByteBuf data, BlockPos pos) {
        if (data.readableBytes() > 0) {
            int ordinal = data.readVarInt();
            NuclearDeviceBlock.Kind[] values = NuclearDeviceBlock.Kind.values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
        }
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof NuclearDeviceBlockEntity device) {
            return device.kind();
        }
        net.minecraft.world.level.block.state.BlockState state = inventory.player.level().getBlockState(pos);
        if (state.getBlock() instanceof NuclearDeviceBlock device) {
            return device.kind();
        }
        return NuclearDeviceBlock.Kind.GADGET;
    }

    private record MenuSource(@Nullable NuclearDeviceBlockEntity blockEntity, NuclearDeviceBlock.Kind kind,
            BlockPos blockPos, ItemStackHandler items) {
        static MenuSource fromBlockEntity(NuclearDeviceBlockEntity blockEntity) {
            return new MenuSource(blockEntity, blockEntity.kind(), blockEntity.getBlockPos(), blockEntity.getItems());
        }
    }

    public record SlotPos(int index, int x, int y) {
    }

    public record Layout(int imageWidth, int imageHeight, int playerInventoryX, int playerInventoryY, int hotbarY,
            String texturePath, SlotPos[] deviceSlots) {
        public static Layout forKind(NuclearDeviceBlock.Kind kind) {
            return switch (kind) {
                case GADGET -> new Layout(176, 166, 8, 84, 142, "gadget_schematic", new SlotPos[]{
                        new SlotPos(0, 26, 35), new SlotPos(1, 8, 17), new SlotPos(2, 44, 17),
                        new SlotPos(3, 8, 53), new SlotPos(4, 44, 53), new SlotPos(5, 98, 35)});
                case BOY -> new Layout(176, 222, 8, 140, 198, "lil_boy_schematic", new SlotPos[]{
                        new SlotPos(0, 26, 36), new SlotPos(1, 44, 36), new SlotPos(2, 62, 36),
                        new SlotPos(3, 80, 36), new SlotPos(4, 98, 36)});
                case MAN -> new Layout(176, 166, 8, 84, 142, "fat_man_schematic", new SlotPos[]{
                        new SlotPos(0, 26, 35), new SlotPos(1, 8, 17), new SlotPos(2, 44, 17),
                        new SlotPos(3, 8, 53), new SlotPos(4, 44, 53), new SlotPos(5, 98, 35)});
                case TSAR -> new Layout(256, 233, 48, 151, 209, "tsar_bomba_schematic", new SlotPos[]{
                        new SlotPos(0, 48, 101), new SlotPos(1, 66, 101), new SlotPos(2, 84, 101),
                        new SlotPos(3, 102, 101), new SlotPos(4, 55, 51), new SlotPos(5, 138, 101)});
                case MIKE -> new Layout(176, 217, 8, 135, 193, "ivy_mike_schematic", new SlotPos[]{
                        new SlotPos(0, 26, 83), new SlotPos(1, 26, 101), new SlotPos(2, 44, 83),
                        new SlotPos(3, 44, 101), new SlotPos(4, 39, 35), new SlotPos(5, 98, 91),
                        new SlotPos(6, 116, 91), new SlotPos(7, 134, 91)});
                case PROTOTYPE -> new Layout(176, 166, 8, 84, 142, "gui_prototype", new SlotPos[]{
                        new SlotPos(0, 8, 35), new SlotPos(1, 26, 35), new SlotPos(2, 44, 26),
                        new SlotPos(3, 44, 44), new SlotPos(4, 62, 26), new SlotPos(5, 62, 44),
                        new SlotPos(6, 80, 26), new SlotPos(7, 80, 44), new SlotPos(8, 98, 26),
                        new SlotPos(9, 98, 44), new SlotPos(10, 116, 26), new SlotPos(11, 116, 44),
                        new SlotPos(12, 134, 35), new SlotPos(13, 152, 35)});
                case FLEIJA -> new Layout(176, 222, 8, 140, 198, "fleija_schematic", new SlotPos[]{
                        new SlotPos(0, 8, 36), new SlotPos(1, 152, 36), new SlotPos(2, 44, 18),
                        new SlotPos(3, 44, 36), new SlotPos(4, 44, 54), new SlotPos(5, 80, 18),
                        new SlotPos(6, 98, 18), new SlotPos(7, 80, 36), new SlotPos(8, 98, 36),
                        new SlotPos(9, 80, 54), new SlotPos(10, 98, 54)});
                case SOLINIUM -> new Layout(176, 222, 8, 140, 198, "solinium_schematic", new SlotPos[]{
                        new SlotPos(0, 26, 18), new SlotPos(1, 53, 18), new SlotPos(2, 107, 18),
                        new SlotPos(3, 134, 18), new SlotPos(4, 80, 36), new SlotPos(5, 26, 54),
                        new SlotPos(6, 53, 54), new SlotPos(7, 107, 54), new SlotPos(8, 134, 54)});
                case N2 -> new Layout(176, 222, 8, 140, 198, "n2_schematic", new SlotPos[]{
                        new SlotPos(0, 98, 36), new SlotPos(1, 116, 36), new SlotPos(2, 134, 36),
                        new SlotPos(3, 98, 54), new SlotPos(4, 116, 54), new SlotPos(5, 134, 54),
                        new SlotPos(6, 98, 72), new SlotPos(7, 116, 72), new SlotPos(8, 134, 72),
                        new SlotPos(9, 98, 90), new SlotPos(10, 116, 90), new SlotPos(11, 134, 90)});
            };
        }
    }
}
