package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DroneLogisticsBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.minecraftforge.items.SlotItemHandler;

/** Slot geometry is the legacy 3x3 dock/provider and split 3x3+3x3 requester layouts. */
public class DroneLogisticsMenu extends AbstractContainerMenu {
    private final DroneLogisticsBlockEntity blockEntity;
    private final SimpleContainer filterDisplay = new SimpleContainer(9);
    public DroneLogisticsMenu(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory, get(inventory, data.readBlockPos())); }
    public DroneLogisticsMenu(int id, Inventory inventory, DroneLogisticsBlockEntity blockEntity) {
        super(ModMenuTypes.DRONE_LOGISTICS.get(), id); this.blockEntity = blockEntity;
        if (blockEntity.kind() == DroneLogisticsBlockEntity.Kind.REQUESTER) {
            for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
                int filter = column + row * 3;
                addSlot(new GhostFilterSlot(filterDisplay, filter, 98 + column * 18, 17 + row * 18, blockEntity));
            }
            HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.items(), 9, 26,17,3,3,18);
        } else HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.items(),0,62,17,3,3,18);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory,8,103,161);
    }
    public DroneLogisticsBlockEntity getBlockEntity(){return blockEntity;}
    @Override public boolean stillValid(Player player){return blockEntity.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        ItemStack result=ItemStack.EMPTY;
        if (index < 0 || index >= slots.size()) return result;
        if (blockEntity.kind() == DroneLogisticsBlockEntity.Kind.REQUESTER && index < 9) return result;
        Slot slot=slots.get(index); int machineStart=blockEntity.kind()==DroneLogisticsBlockEntity.Kind.REQUESTER ? 9 : 0; int machineEnd=machineStart+blockEntity.items().getSlots()-(blockEntity.kind()==DroneLogisticsBlockEntity.Kind.REQUESTER ? 9 : 0);
        if(slot!=null&&slot.hasItem()){ItemStack stack=slot.getItem();result=stack.copy();if(index>=machineStart&&index<machineEnd){if(!moveItemStackTo(stack,machineEnd,machineEnd+36,true))return ItemStack.EMPTY;}else if(blockEntity.kind()==DroneLogisticsBlockEntity.Kind.REQUESTER){if(!moveItemStackTo(stack,9,18,false))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,0,blockEntity.items().getSlots(),false))return ItemStack.EMPTY;HbmInventoryMenuHelper.finishQuickMove(slot,stack);} return result;
    }
    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (blockEntity.kind() == DroneLogisticsBlockEntity.Kind.REQUESTER && slotId >= 0 && slotId < 9) {
            // ContainerDroneRequester#slotClick only reserved an existing filter's normal
            // right click for mode cycling.  Every other click mode reset the ghost pattern
            // from the carried stack without consuming it.
            if (clickType == ClickType.PICKUP && button == 1 && blockEntity.filter(slotId) != null) {
                blockEntity.cycleFilter(slotId);
            } else {
                blockEntity.setFilter(slotId, getCarried());
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
    private static DroneLogisticsBlockEntity get(Inventory inventory,BlockPos pos){if(inventory.player.level().getBlockEntity(pos) instanceof DroneLogisticsBlockEntity entity)return entity;throw new IllegalStateException("Expected drone logistics block entity at "+pos);}

    private static final class GhostFilterSlot extends Slot {
        private final int filter;
        private final DroneLogisticsBlockEntity requester;
        private GhostFilterSlot(SimpleContainer display, int filter, int x, int y, DroneLogisticsBlockEntity requester) { super(display, filter, x, y); this.filter = filter; this.requester = requester; }
        @Override public ItemStack getItem() { var filterValue = requester.filter(filter); return filterValue == null ? ItemStack.EMPTY : filterValue.pattern(); }
        @Override public boolean hasItem() { return requester.filter(filter) != null; }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
