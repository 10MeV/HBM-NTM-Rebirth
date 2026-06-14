package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.MissileAssemblyBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MissileAssemblyMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = MissileAssemblyBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final MissileAssemblyBlockEntity blockEntity;
    private int chipState;
    private int warheadState;
    private int fuselageState;
    private int stabilityState;
    private int thrusterState;
    private int canBuild;

    public MissileAssemblyMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public MissileAssemblyMenu(int containerId, Inventory playerInventory, MissileAssemblyBlockEntity blockEntity) {
        super(ModMenuTypes.MISSILE_ASSEMBLY.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                MissileAssemblyBlockEntity.SLOT_CHIP, 8, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                MissileAssemblyBlockEntity.SLOT_WARHEAD, 26, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                MissileAssemblyBlockEntity.SLOT_FUSELAGE, 44, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                MissileAssemblyBlockEntity.SLOT_STABILITY, 62, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                MissileAssemblyBlockEntity.SLOT_THRUSTER, 80, 36));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                MissileAssemblyBlockEntity.SLOT_OUTPUT, 152, 36));

        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        addDataSlots();
    }

    public MissileAssemblyBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getChipState() {
        return chipState;
    }

    public int getWarheadState() {
        return warheadState;
    }

    public int getFuselageState() {
        return fuselageState;
    }

    public int getStabilityState() {
        return stabilityState;
    }

    public int getThrusterState() {
        return thrusterState;
    }

    public boolean canBuild() {
        return canBuild == 1;
    }

    public ItemStack previewMissileStack() {
        return blockEntity.previewMissileStack();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                MissileAssemblyBlockEntity.SLOT_CHIP, MissileAssemblyBlockEntity.SLOT_THRUSTER + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::chipState, value -> chipState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::warheadState, value -> warheadState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::fuselageState, value -> fuselageState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::stabilityState, value -> stabilityState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::thrusterState, value -> thrusterState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.canBuild() ? 1 : 0,
                value -> canBuild = value);
    }

    private static MissileAssemblyBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof MissileAssemblyBlockEntity missileAssembly) {
            return missileAssembly;
        }
        throw new IllegalStateException("Expected missile assembly block entity at " + pos);
    }
}
