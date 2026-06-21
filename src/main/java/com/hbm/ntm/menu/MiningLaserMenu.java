package com.hbm.ntm.menu;

import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.blockentity.MiningLaserBlockEntity;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class MiningLaserMenu extends AbstractContainerMenu implements LegacyUpgradeInfoProvider {
    private static final int MACHINE_SLOT_COUNT = MiningLaserBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;
    private static final Map<UpgradeType, Integer> INFO_UPGRADES = Map.of(
            UpgradeType.SPEED, 12,
            UpgradeType.POWER, 12,
            UpgradeType.EFFECT, 12,
            UpgradeType.FORTUNE, 3,
            UpgradeType.OVERDRIVE, 9);

    private final MiningLaserBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData oilTank;
    private long power;
    private long maxPower;
    private boolean on;
    private boolean redstonePowered;
    private int width;
    private int consumption;
    private int progress;

    public MiningLaserMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public MiningLaserMenu(int containerId, Inventory playerInventory, MiningLaserBlockEntity blockEntity) {
        super(ModMenuTypes.MINING_LASER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                MiningLaserBlockEntity.SLOT_BATTERY, 8, 108));
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 4; column++) {
                addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(),
                        MiningLaserBlockEntity.SLOT_UPGRADE_START + row * 4 + column,
                        98 + column * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 7; column++) {
                addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                        MiningLaserBlockEntity.SLOT_OUTPUT_START + row * 7 + column,
                        44 + column * 18, 72 + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        addDataSlots();
    }

    public MiningLaserBlockEntity getBlockEntity() {
        return blockEntity;
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

    public boolean isOn() {
        return on;
    }

    public boolean isRedstonePowered() {
        return redstonePowered;
    }

    public int getWidth() {
        return width;
    }

    public int getConsumption() {
        return consumption;
    }

    public int getProgressBarHeight(int maxHeight) {
        return progress * maxHeight / 1000;
    }

    public HbmFluidGuiHelper.TankData getOilTank() {
        return oilTank;
    }

    public List<Component> oilTooltip(boolean showHidden) {
        return oilTank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (stack.getItem() instanceof HbmBatteryItem
                || stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent()) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    MiningLaserBlockEntity.SLOT_BATTERY, MiningLaserBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade || stack.is(ModItems.UPGRADE_SCREM.get())) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    MiningLaserBlockEntity.SLOT_UPGRADE_START, MiningLaserBlockEntity.SLOT_UPGRADE_END + 1);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<Component> info, boolean extendedInfo) {
        info.add(Component.literal(">>> ")
                .append(Component.translatableWithFallback("block.hbm_ntm_rebirth.machine_mining_laser",
                        "Mining Laser"))
                .append(" <<<")
                .withStyle(ChatFormatting.YELLOW));
        switch (type) {
            case SPEED -> {
                info.add(Component.translatableWithFallback(KEY_DELAY, "Delay %s",
                        "-" + (100 - 100 / (level + 1)) + "%").withStyle(ChatFormatting.GREEN));
                info.add(Component.translatableWithFallback(KEY_CONSUMPTION, "Consumption %s",
                        "+" + (100 * level / 16) + "%").withStyle(ChatFormatting.RED));
            }
            case POWER -> info.add(Component.translatableWithFallback(KEY_CONSUMPTION, "Consumption %s",
                    "-" + (100 * level / 16) + "%").withStyle(ChatFormatting.GREEN));
            case EFFECT -> info.add(Component.translatableWithFallback(KEY_RANGE, "Range %s",
                    "+" + (2 * level) + "m").withStyle(ChatFormatting.GREEN));
            case FORTUNE -> info.add(Component.translatableWithFallback(KEY_FORTUNE, "Fortune %s",
                    "+" + level).withStyle(ChatFormatting.GREEN));
            case OVERDRIVE -> info.add(Component.literal("YES").withStyle(ChatFormatting.RED));
            default -> {
            }
        }
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return INFO_UPGRADES;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isRedstonePowered, value -> redstonePowered = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getWidth, value -> width = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getAdjustedConsumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> (int) Math.min(1000.0D, blockEntity.getBreakProgress() * 1000.0D),
                value -> progress = value);
        oilTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOilTank());
    }

    private static MiningLaserBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof MiningLaserBlockEntity laser) {
            return laser;
        }
        throw new IllegalStateException("Expected mining laser block entity at " + pos);
    }
}
