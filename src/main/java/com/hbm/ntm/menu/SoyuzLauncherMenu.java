package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class SoyuzLauncherMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SoyuzLauncherBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final SoyuzLauncherBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int mode;
    private int countdown;
    private int starting;
    private int rocketStatus;
    private int designatorStatus;
    private int satelliteStatus;
    private int orbitalStatus;
    private int fuelStatus;
    private int oxygenStatus;
    private int powerStatus;
    private HbmFluidGuiHelper.TankData keroseneTank;
    private HbmFluidGuiHelper.TankData oxygenTank;

    public SoyuzLauncherMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SoyuzLauncherMenu(int containerId, Inventory playerInventory, SoyuzLauncherBlockEntity blockEntity) {
        super(ModMenuTypes.SOYUZ_LAUNCHER.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_ROCKET, 62, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.MISSILE_SOYUZ.get());
            }
        });
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_DESIGNATOR, 62, 36));
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_SATELLITE, 116, 18));
        addSlot(new SlotItemHandler(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_ORBITAL, 116, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.MISSILE_SOYUZ_LANDER.get());
            }
        });
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_KEROSENE_INPUT, 8, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_KEROSENE_OUTPUT, 8, 108));
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_OXYGEN_INPUT, 26, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_OXYGEN_OUTPUT, 26, 108));
        addSlot(new SlotItemHandler(blockEntity.getItems(), SoyuzLauncherBlockEntity.SLOT_BATTERY, 44, 108) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
            }
        });
        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(),
                SoyuzLauncherBlockEntity.SLOT_CARGO_START, 62, 72, 3, 6);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        addDataSlots();
    }

    public SoyuzLauncherBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int height) {
        return maxPower <= 0L ? 0 : (int) (power * height / maxPower);
    }

    public int getMode() {
        return mode;
    }

    public boolean isStarting() {
        return starting != 0;
    }

    public int getCountdown() {
        return countdown;
    }

    public int getRocketStatus() {
        return rocketStatus;
    }

    public int getDesignatorStatus() {
        return designatorStatus;
    }

    public int getSatelliteStatus() {
        return satelliteStatus;
    }

    public int getOrbitalStatus() {
        return orbitalStatus;
    }

    public boolean hasFuel() {
        return fuelStatus != 0;
    }

    public boolean hasOxygen() {
        return oxygenStatus != 0;
    }

    public boolean hasPower() {
        return powerStatus != 0;
    }

    public HbmFluidGuiHelper.TankData getKeroseneTankData() {
        return keroseneTank;
    }

    public HbmFluidGuiHelper.TankData getOxygenTankData() {
        return oxygenTank;
    }

    public List<Component> getKeroseneTankTooltip(boolean showHidden) {
        return keroseneTank.tooltip(showHidden);
    }

    public List<Component> getOxygenTankTooltip(boolean showHidden) {
        return oxygenTank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                SoyuzLauncherBlockEntity.SLOT_ROCKET, SoyuzLauncherBlockEntity.SLOT_CARGO_END);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getStoredPower(), () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxStoredPower(), () -> maxPower, value -> maxPower = value);
        addIntSlot(() -> blockEntity.getMode(), value -> mode = value);
        addIntSlot(() -> blockEntity.getCountdown(), value -> countdown = value);
        addIntSlot(() -> blockEntity.isStarting() ? 1 : 0, value -> starting = value);
        addIntSlot(() -> blockEntity.hasRocket() ? 1 : 0, value -> rocketStatus = value);
        addIntSlot(() -> blockEntity.designatorStatus(), value -> designatorStatus = value);
        addIntSlot(() -> blockEntity.satelliteStatus(), value -> satelliteStatus = value);
        addIntSlot(() -> blockEntity.orbitalStatus(), value -> orbitalStatus = value);
        addIntSlot(() -> blockEntity.hasFuel() ? 1 : 0, value -> fuelStatus = value);
        addIntSlot(() -> blockEntity.hasOxygen() ? 1 : 0, value -> oxygenStatus = value);
        addIntSlot(() -> blockEntity.hasPower() ? 1 : 0, value -> powerStatus = value);
        keroseneTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.keroseneTank());
        oxygenTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.oxygenTank());
    }

    private void addIntSlot(IntGetter getter, IntSetter setter) {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getter.get();
            }

            @Override
            public void set(int value) {
                setter.set(value);
            }
        });
    }

    private static SoyuzLauncherBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SoyuzLauncherBlockEntity launcher) {
            return launcher;
        }
        throw new IllegalStateException("Expected Soyuz launcher block entity at " + pos);
    }

    private interface IntGetter {
        int get();
    }

    private interface IntSetter {
        void set(int value);
    }
}
