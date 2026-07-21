package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DroneCrateBlockEntity;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

/** Exact legacy crate geometry: 3x6 cargo, one identifier, then player inventory. */
public class DroneCrateMenu extends AbstractContainerMenu {
    private final DroneCrateBlockEntity crate;
    private final HbmFluidGuiHelper.TankData tank;
    public DroneCrateMenu(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory, get(inventory, data.readBlockPos())); }
    public DroneCrateMenu(int id, Inventory inventory, DroneCrateBlockEntity crate) {
        super(ModMenuTypes.DRONE_CRATE.get(), id); this.crate = crate;
        HbmInventoryMenuHelper.addSlots(this::addSlot, crate.items(), 0, 8, 17, 3, 6, 18);
        addSlot(new SlotItemHandler(crate.items(), DroneCrateBlockEntity.FLUID_IDENTIFIER_SLOT, 125, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 103, 161);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, crate.tank());
    }
    public DroneCrateBlockEntity getBlockEntity() { return crate; }
    public HbmFluidGuiHelper.TankData getTankData() { return tank; }
    public List<net.minecraft.network.chat.Component> getTankTooltip(boolean showHidden) { return tank.tooltip(showHidden); }
    @Override public boolean stillValid(Player player) { return crate.stillValid(player); }
    @Override public boolean clickMenuButton(Player player, int id) { if (id < 0 || id > 1) return false; crate.handleLegacyButton(null, 0, id); return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index); if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), result = stack.copy();
        if (index < 19) { if (!moveItemStackTo(stack, 19, 55, true)) return ItemStack.EMPTY; }
        else if (stack.getItem() instanceof IFluidIdentifierItem) {
            // ContainerDroneCrate routed identifier stacks to its dedicated 19th slot
            // before considering the eighteen cargo slots.
            if (!moveItemStackTo(stack, 18, 19, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 18, false)) return ItemStack.EMPTY;
        HbmInventoryMenuHelper.finishQuickMove(slot, stack); return result;
    }
    private static DroneCrateBlockEntity get(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof DroneCrateBlockEntity crate) return crate;
        throw new IllegalStateException("Expected drone crate at " + pos);
    }
}
