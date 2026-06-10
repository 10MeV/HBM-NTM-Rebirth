package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.CatalyticReformerBlockEntity;
import com.hbm.ntm.blockentity.CokerBlockEntity;
import com.hbm.ntm.blockentity.HydrotreaterBlockEntity;
import com.hbm.ntm.blockentity.LegacyRemoteFluidMachineBlockEntity;
import com.hbm.ntm.blockentity.LegacyRemoteFluidMachineBlockEntity.LegacyGuiProfile;
import com.hbm.ntm.blockentity.VacuumDistillBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class RemoteFluidMachineMenu extends AbstractContainerMenu {
    private static final int PLAYER_INVENTORY_SIZE = 36;

    private final LegacyRemoteFluidMachineBlockEntity blockEntity;
    private final LegacyGuiProfile profile;
    private final List<HbmFluidGuiHelper.TankData> tanks = new ArrayList<>();
    private final int machineSlotCount;
    private final int playerInventoryStart;
    private final int playerSlotEnd;
    private long power;
    private long maxPower;
    private int cokerProgress;
    private int cokerHeat;

    public RemoteFluidMachineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RemoteFluidMachineMenu(int containerId, Inventory playerInventory,
            LegacyRemoteFluidMachineBlockEntity blockEntity) {
        super(ModMenuTypes.REMOTE_FLUID_MACHINE.get(), containerId);
        this.blockEntity = blockEntity;
        this.profile = blockEntity.getLegacyGuiProfile();
        this.machineSlotCount = addMachineSlots();
        this.playerInventoryStart = machineSlotCount;
        this.playerSlotEnd = playerInventoryStart + PLAYER_INVENTORY_SIZE;
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory,
                8, profile.inventoryY(), profile.hotbarY());
        addDataSlots();
    }

    public LegacyRemoteFluidMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public LegacyGuiProfile getProfile() {
        return profile;
    }

    public int getImageWidth() {
        return profile.width();
    }

    public int getImageHeight() {
        return profile.height();
    }

    public List<HbmFluidGuiHelper.TankData> getTanks() {
        return tanks;
    }

    public HbmFluidGuiHelper.TankData getTank(int index) {
        return index >= 0 && index < tanks.size() ? tanks.get(index) : null;
    }

    public List<Component> getTankTooltip(int index) {
        HbmFluidGuiHelper.TankData tank = getTank(index);
        return tank == null ? List.of() : tank.tooltip();
    }

    public List<Component> getTankTooltip(int index, boolean showHidden) {
        HbmFluidGuiHelper.TankData tank = getTank(index);
        return tank == null ? List.of() : tank.tooltip(showHidden);
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getCokerProgress() {
        return cokerProgress;
    }

    public int getCokerHeat() {
        return cokerHeat;
    }

    public int getCokerProcessTime() {
        return blockEntity instanceof CokerBlockEntity coker ? coker.getProcessTime() : 20_000;
    }

    public int getCokerMaxHeat() {
        return blockEntity instanceof CokerBlockEntity coker ? coker.getMaxHeat() : 100_000;
    }

    public int getCokerProgressBarWidth(int width) {
        int max = getCokerProcessTime();
        return max <= 0 ? 0 : cokerProgress * width / max;
    }

    public int getCokerHeatBarWidth(int width) {
        int max = getCokerMaxHeat();
        return max <= 0 ? 0 : cokerHeat * width / max;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        var slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        boolean moved;
        if (index < machineSlotCount) {
            moved = moveItemStackTo(stack, playerInventoryStart, playerSlotEnd, true);
        } else {
            moved = HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, insertionRanges(stack));
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        tanks.addAll(HbmFluidGuiHelper.watchTanks(this::addDataSlot, blockEntity.getAllTanks()));
        if (blockEntity instanceof CokerBlockEntity coker) {
            HbmMenuDataSlots.addInt(this::addDataSlot, coker::getProgress, value -> cokerProgress = value);
            HbmMenuDataSlots.addLong(this::addDataSlot, coker::getHeat, () -> cokerHeat, value -> cokerHeat = (int) value);
        }
    }

    private int addMachineSlots() {
        ItemStackHandler items = blockEntity.getItems();
        if (items == null) {
            return 0;
        }
        switch (profile) {
            case COKER -> addCokerSlots(items);
            case HYDROTREATER -> addHydrotreaterSlots(items);
            case CATALYTIC_REFORMER -> addCatalyticReformerSlots(items);
            case VACUUM_DISTILL -> addVacuumDistillSlots(items);
            default -> {
                return 0;
            }
        }
        return items.getSlots();
    }

    private void addCokerSlots(ItemStackHandler items) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CokerBlockEntity.SLOT_IDENTIFIER, 35, 72));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, CokerBlockEntity.SLOT_OUTPUT, 97, 27));
    }

    private void addHydrotreaterSlots(ItemStackHandler items) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, HydrotreaterBlockEntity.SLOT_BATTERY, 17, 90));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, HydrotreaterBlockEntity.SLOT_INPUT_CONTAINER, 35, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, HydrotreaterBlockEntity.SLOT_INPUT_CONTAINER_OUTPUT, 35, 108));
        addSlot(HbmInventoryMenuHelper.deprecatedSlot(items, HydrotreaterBlockEntity.SLOT_HYDROGEN_INPUT, 53, 90));
        addSlot(HbmInventoryMenuHelper.deprecatedSlot(items, HydrotreaterBlockEntity.SLOT_HYDROGEN_OUTPUT, 53, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, HydrotreaterBlockEntity.SLOT_OUTPUT_LEFT_CONTAINER, 125, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, HydrotreaterBlockEntity.SLOT_OUTPUT_LEFT_CONTAINER_OUTPUT, 125, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, HydrotreaterBlockEntity.SLOT_OUTPUT_RIGHT_CONTAINER, 143, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, HydrotreaterBlockEntity.SLOT_OUTPUT_RIGHT_CONTAINER_OUTPUT, 143, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, HydrotreaterBlockEntity.SLOT_IDENTIFIER, 17, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, HydrotreaterBlockEntity.SLOT_CATALYST, 89, 36));
    }

    private void addCatalyticReformerSlots(ItemStackHandler items) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CatalyticReformerBlockEntity.SLOT_BATTERY, 17, 90));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CatalyticReformerBlockEntity.SLOT_INPUT_CONTAINER, 35, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, CatalyticReformerBlockEntity.SLOT_INPUT_CONTAINER_OUTPUT, 35, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CatalyticReformerBlockEntity.SLOT_OUTPUT_1_CONTAINER, 107, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, CatalyticReformerBlockEntity.SLOT_OUTPUT_1_CONTAINER_OUTPUT, 107, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CatalyticReformerBlockEntity.SLOT_OUTPUT_2_CONTAINER, 125, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, CatalyticReformerBlockEntity.SLOT_OUTPUT_2_CONTAINER_OUTPUT, 125, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CatalyticReformerBlockEntity.SLOT_OUTPUT_3_CONTAINER, 143, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, CatalyticReformerBlockEntity.SLOT_OUTPUT_3_CONTAINER_OUTPUT, 143, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CatalyticReformerBlockEntity.SLOT_IDENTIFIER, 17, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, CatalyticReformerBlockEntity.SLOT_CATALYST, 71, 36));
    }

    private void addVacuumDistillSlots(ItemStackHandler items) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, VacuumDistillBlockEntity.SLOT_BATTERY, 26, 90));
        addSlot(HbmInventoryMenuHelper.deprecatedSlot(items, VacuumDistillBlockEntity.SLOT_INPUT_CONTAINER, 44, 90));
        addSlot(HbmInventoryMenuHelper.deprecatedSlot(items, VacuumDistillBlockEntity.SLOT_INPUT_CONTAINER_OUTPUT, 44, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_HEAVY_CONTAINER, 80, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_HEAVY_CONTAINER_OUTPUT, 80, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_REFORMATE_CONTAINER, 98, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_REFORMATE_CONTAINER_OUTPUT, 98, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_LIGHT_CONTAINER, 116, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_LIGHT_CONTAINER_OUTPUT, 116, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_GAS_CONTAINER, 134, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, VacuumDistillBlockEntity.SLOT_OUTPUT_GAS_CONTAINER_OUTPUT, 134, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, VacuumDistillBlockEntity.SLOT_IDENTIFIER, 26, 108));
    }

    private int[] insertionRanges(ItemStack stack) {
        return switch (profile) {
            case COKER -> stack.getItem() instanceof IFluidIdentifierItem
                    ? new int[] { CokerBlockEntity.SLOT_IDENTIFIER, CokerBlockEntity.SLOT_IDENTIFIER + 1 }
                    : new int[0];
            case HYDROTREATER -> hydrotreaterInsertionRanges(stack);
            case CATALYTIC_REFORMER -> catalyticReformerInsertionRanges(stack);
            case VACUUM_DISTILL -> new int[] {
                    VacuumDistillBlockEntity.SLOT_BATTERY, VacuumDistillBlockEntity.SLOT_BATTERY + 1,
                    VacuumDistillBlockEntity.SLOT_OUTPUT_HEAVY_CONTAINER, VacuumDistillBlockEntity.SLOT_OUTPUT_HEAVY_CONTAINER + 1,
                    VacuumDistillBlockEntity.SLOT_OUTPUT_REFORMATE_CONTAINER, VacuumDistillBlockEntity.SLOT_OUTPUT_REFORMATE_CONTAINER + 1,
                    VacuumDistillBlockEntity.SLOT_OUTPUT_LIGHT_CONTAINER, VacuumDistillBlockEntity.SLOT_OUTPUT_LIGHT_CONTAINER + 1,
                    VacuumDistillBlockEntity.SLOT_OUTPUT_GAS_CONTAINER, VacuumDistillBlockEntity.SLOT_OUTPUT_GAS_CONTAINER + 1 };
            default -> new int[0];
        };
    }

    private int[] hydrotreaterInsertionRanges(ItemStack stack) {
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return new int[] { HydrotreaterBlockEntity.SLOT_BATTERY, HydrotreaterBlockEntity.SLOT_BATTERY + 1 };
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return new int[] { HydrotreaterBlockEntity.SLOT_IDENTIFIER, HydrotreaterBlockEntity.SLOT_IDENTIFIER + 1 };
        }
        if (stack.is(ModItems.CATALYTIC_CONVERTER.get())) {
            return new int[] { HydrotreaterBlockEntity.SLOT_CATALYST, HydrotreaterBlockEntity.SLOT_CATALYST + 1 };
        }
        return new int[] {
                HydrotreaterBlockEntity.SLOT_INPUT_CONTAINER, HydrotreaterBlockEntity.SLOT_INPUT_CONTAINER + 1,
                HydrotreaterBlockEntity.SLOT_HYDROGEN_INPUT, HydrotreaterBlockEntity.SLOT_HYDROGEN_INPUT + 1,
                HydrotreaterBlockEntity.SLOT_OUTPUT_LEFT_CONTAINER, HydrotreaterBlockEntity.SLOT_OUTPUT_LEFT_CONTAINER + 1,
                HydrotreaterBlockEntity.SLOT_OUTPUT_RIGHT_CONTAINER, HydrotreaterBlockEntity.SLOT_OUTPUT_RIGHT_CONTAINER + 1 };
    }

    private int[] catalyticReformerInsertionRanges(ItemStack stack) {
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return new int[] { CatalyticReformerBlockEntity.SLOT_BATTERY, CatalyticReformerBlockEntity.SLOT_BATTERY + 1 };
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return new int[] { CatalyticReformerBlockEntity.SLOT_IDENTIFIER, CatalyticReformerBlockEntity.SLOT_IDENTIFIER + 1 };
        }
        if (stack.is(ModItems.CATALYTIC_CONVERTER.get())) {
            return new int[] { CatalyticReformerBlockEntity.SLOT_CATALYST, CatalyticReformerBlockEntity.SLOT_CATALYST + 1 };
        }
        return new int[] {
                CatalyticReformerBlockEntity.SLOT_INPUT_CONTAINER, CatalyticReformerBlockEntity.SLOT_INPUT_CONTAINER + 1,
                CatalyticReformerBlockEntity.SLOT_OUTPUT_1_CONTAINER, CatalyticReformerBlockEntity.SLOT_OUTPUT_1_CONTAINER + 1,
                CatalyticReformerBlockEntity.SLOT_OUTPUT_2_CONTAINER, CatalyticReformerBlockEntity.SLOT_OUTPUT_2_CONTAINER + 1,
                CatalyticReformerBlockEntity.SLOT_OUTPUT_3_CONTAINER, CatalyticReformerBlockEntity.SLOT_OUTPUT_3_CONTAINER + 1 };
    }

    private static LegacyRemoteFluidMachineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof LegacyRemoteFluidMachineBlockEntity remoteMachine) {
            return remoteMachine;
        }
        throw new IllegalStateException("Expected remote fluid machine block entity at " + pos);
    }
}
