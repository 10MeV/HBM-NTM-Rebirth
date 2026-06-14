package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.menu.OilburnerMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OilburnerBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HeatSource, HbmLegacyButtonReceiver,
        RORValueProvider, RORInteractive {
    public static final int SLOT_FLUID_INPUT = 0;
    public static final int SLOT_FLUID_OUTPUT = 1;
    public static final int SLOT_IDENTIFIER = 2;
    public static final int SLOT_COUNT = 3;
    public static final int CONTROL_TOGGLE = 0;
    public static final int MAX_HEAT = 100_000;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.cardinal(2);
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_IS_ON = "isOn";
    private static final String TAG_HEAT = "heatEnergy";
    private static final String TAG_SETTING = "setting";

    private final MachinePollutionBuffers pollution = new MachinePollutionBuffers(100);
    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_FLUID_INPUT || slot == SLOT_IDENTIFIER;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private boolean on;
    private int setting = 1;
    private int heatEnergy;
    private int lastBurned;
    private int lastHeatProduced;

    public OilburnerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.HEATINGOIL, 16_000));
    }

    private OilburnerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.OILBURNER.get(), pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OilburnerBlockEntity burner) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, burner);

        int oldHeat = burner.heatEnergy;
        int oldFill = burner.tank.getFill();
        boolean oldOn = burner.on;
        int oldSetting = burner.setting;

        burner.lastBurned = 0;
        burner.lastHeatProduced = 0;
        boolean changed = burner.setFluidTankTypeFromIdentifierSlot(burner.items, SLOT_IDENTIFIER, burner.tank);
        changed |= burner.processFluidItemTransfers(burner.items,
                HbmFluidItemTransfer.loadTransfers(SLOT_FLUID_INPUT, SLOT_FLUID_OUTPUT, burner.tank));

        for (FluidPort port : FLUID_PORTS) {
            BlockPos connector = pos.offset(port.offset());
            burner.pollution.sendSmoke(level, connector.getX(), connector.getY(), connector.getZ(), port.direction());
        }

        boolean shouldCool = true;
        if (burner.on && burner.heatEnergy < MAX_HEAT) {
            FlammableFluidTrait flammable = burner.tank.getTankType().getTrait(FlammableFluidTrait.class);
            if (flammable != null) {
                int toBurn = Math.min(burner.setting, burner.tank.getFill());
                if (toBurn > 0) {
                    burner.tank.drain(toBurn, false);
                    int heat = (int) (flammable.getHeatEnergyPerBucket() / 1_000L);
                    burner.heatEnergy = Math.min(MAX_HEAT, burner.heatEnergy + heat * toBurn);
                    burner.lastBurned = toBurn;
                    burner.lastHeatProduced = heat * toBurn;
                    if (level.getGameTime() % 5L == 0L) {
                        burner.pollution.polluteFluidRelease(level, pos, burner.tank.getTankType(),
                                FluidReleaseType.BURN, toBurn * 5.0F);
                    }
                    shouldCool = false;
                    changed = true;
                }
            }
        }
        if (burner.heatEnergy >= MAX_HEAT) {
            shouldCool = false;
        }
        if (shouldCool) {
            burner.heatEnergy = Math.max(burner.heatEnergy - Math.max(burner.heatEnergy / 1000, 1), 0);
        }

        changed |= oldHeat != burner.heatEnergy
                || oldFill != burner.tank.getFill()
                || oldOn != burner.on
                || oldSetting != burner.setting;
        if (changed) {
            burner.setChanged();
        }
        burner.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isOn() {
        return on;
    }

    public int getSetting() {
        return setting;
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public int getLastBurned() {
        return lastBurned;
    }

    public int getLastHeatProduced() {
        return lastHeatProduced;
    }

    public int getCurrentHeatOutputPerTick() {
        FlammableFluidTrait flammable = tank.getTankType().getTrait(FlammableFluidTrait.class);
        return flammable == null ? 0 : (int) (flammable.getHeatEnergyPerBucket() * setting / 1_000L);
    }

    public void toggleOn() {
        on = !on;
        setChanged();
        onFluidContentsChanged();
    }

    public void toggleSetting() {
        setting++;
        if (setting > 10) {
            setting = 1;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        heatEnergy = Math.max(0, heatEnergy - Math.max(0, heat));
        setChanged();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return tank.getSpace();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == tank.getTankType();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(setting + " mB/t").withStyle(ChatFormatting.RESET)),
                Component.literal("<- ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(NUMBER_FORMAT.format(getCurrentHeatOutputPerTick()) + " TU/t")
                                .withStyle(ChatFormatting.RESET)),
                LegacyLookOverlayLines.heatTu(heatEnergy)));
    }

    @Override
    public CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        int[] ids = getFluidIdsToCopy();
        if (ids.length > 0) {
            tag.putIntArray(HbmFluidCopiable.TAG_FLUID_IDS, ids);
        }
        tag.putInt("burnRate", setting);
        tag.putBoolean("isOn", on);
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        if (tag == null) {
            return false;
        }
        boolean changed = false;
        if (tag.contains(HbmFluidCopiable.TAG_FLUID_IDS)) {
            int[] ids = tag.getIntArray(HbmFluidCopiable.TAG_FLUID_IDS);
            if (ids.length > 0) {
                int safeIndex = index >= 0 && index < ids.length ? index : 0;
                tank.setTankType(HbmFluids.fromId(ids[safeIndex]));
                changed = true;
            }
        }
        if (tag.contains("isOn")) {
            on = tag.getBoolean("isOn");
            changed = true;
        }
        if (tag.contains("burnRate")) {
            setting = Math.max(1, Math.min(10, tag.getInt("burnRate")));
            changed = true;
        }
        if (changed) {
            onFluidSettingsPasted();
        }
        return changed;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.heaterOilburner", "Fluid Burner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new OilburnerMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_TOGGLE && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_TOGGLE) {
            toggleOn();
        }
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                RORInfo.PREFIX_VALUE + "heat",
                RORInfo.PREFIX_VALUE + "fuel",
                RORInfo.PREFIX_VALUE + "burnRate",
                RORInfo.PREFIX_VALUE + "state",
                RORInfo.PREFIX_FUNCTION + "setState" + RORInteractive.NAME_SEPARATOR + "active",
                RORInfo.PREFIX_FUNCTION + "setBurnRate" + RORInteractive.NAME_SEPARATOR + "rate"
        };
    }

    @Override
    public String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "heat").equals(name)) {
            return Integer.toString(heatEnergy);
        }
        if ((RORInfo.PREFIX_VALUE + "fuel").equals(name)) {
            return Integer.toString(tank.getFill());
        }
        if ((RORInfo.PREFIX_VALUE + "burnRate").equals(name)) {
            return Integer.toString(setting);
        }
        if ((RORInfo.PREFIX_VALUE + "state").equals(name)) {
            return on ? "1" : "0";
        }
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((RORInfo.PREFIX_FUNCTION + "setState").equals(name)) {
            on = params.length > 0 && "1".equals(params[0]);
            setChanged();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "setBurnRate").equals(name)) {
            try {
                int rate = Integer.parseInt(params[0]);
                setting = Math.max(1, Math.min(10, rate));
                setChanged();
                return null;
            } catch (Exception ex) {
                return "Invalid number";
            }
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        pollution.writeLegacyNbt(tag);
        tag.putBoolean(TAG_IS_ON, on);
        tag.putInt(TAG_HEAT, heatEnergy);
        tag.putByte(TAG_SETTING, (byte) setting);
        tag.putInt("lastBurned", lastBurned);
        tag.putInt("lastHeatProduced", lastHeatProduced);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        pollution.readLegacyNbt(tag);
        on = tag.getBoolean(TAG_IS_ON);
        heatEnergy = Math.max(0, tag.getInt(TAG_HEAT));
        setting = Math.max(1, Math.min(10, tag.contains(TAG_SETTING) ? tag.getByte(TAG_SETTING) : 1));
        lastBurned = Math.max(0, tag.getInt("lastBurned"));
        lastHeatProduced = Math.max(0, tag.getInt("lastHeatProduced"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }
}
