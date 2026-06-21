package com.hbm.ntm.menu;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.blockentity.StrandCasterBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StrandCasterMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = StrandCasterBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final StrandCasterBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData waterTank;
    private HbmFluidGuiHelper.TankData steamTank;
    private int moltenAmount;
    private int moltenColor;
    private int moltenMaterialId = -1;

    public StrandCasterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public StrandCasterMenu(int containerId, Inventory playerInventory, StrandCasterBlockEntity blockEntity) {
        super(ModMenuTypes.STRAND_CASTER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                StrandCasterBlockEntity.SLOT_MOLD, 57, 62));
        HbmInventoryMenuHelper.addCraftingOutputSlots(this::addSlot, playerInventory.player, blockEntity.getItems(),
                StrandCasterBlockEntity.SLOT_OUTPUT_START, 125, 26, 3, 2);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 132, 190);
        addDataSlots();
    }

    public HbmFluidGuiHelper.TankData getWaterTank() {
        return waterTank;
    }

    public HbmFluidGuiHelper.TankData getSteamTank() {
        return steamTank;
    }

    public List<Component> getWaterTankTooltip(boolean showHidden) {
        return waterTank.tooltip(showHidden);
    }

    public List<Component> getSteamTankTooltip(boolean showHidden) {
        return steamTank.tooltip(showHidden);
    }

    public int getMoltenPixels() {
        return Math.max(0, Math.min(79, moltenAmount * 79 / Math.max(1, blockEntity.getCapacity())));
    }

    public int getMoltenColor() {
        return moltenColor;
    }

    public String getMoltenText(boolean showMb) {
        NTMMaterial material = moltenMaterialId < 0 ? null : Mats.matById.get(moltenMaterialId);
        if (moltenAmount <= 0 || material == null) {
            return "Empty";
        }
        return material.names[0] + ": " + Mats.formatAmount(moltenAmount, showMb);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 128.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        if (FoundryMoldItem.isMold(stack)) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    StrandCasterBlockEntity.SLOT_MOLD, StrandCasterBlockEntity.SLOT_MOLD + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
    }

    private void addDataSlots() {
        com.hbm.ntm.util.HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMoltenAmount,
                value -> moltenAmount = value);
        com.hbm.ntm.util.HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMoltenColor,
                value -> moltenColor = value);
        com.hbm.ntm.util.HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMoltenMaterialId,
                value -> moltenMaterialId = value);
        waterTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getWaterTank());
        steamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSteamTank());
    }

    private static StrandCasterBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof StrandCasterBlockEntity caster) {
            return caster;
        }
        throw new IllegalStateException("Expected strand caster block entity at " + pos);
    }
}
