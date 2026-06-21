package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fusion.FusionPowerReceiver;
import com.hbm.ntm.menu.FusionTorusMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeExtraData;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.uninos.networkproviders.KlystronNetwork;
import com.hbm.ntm.uninos.networkproviders.KlystronNode;
import com.hbm.ntm.uninos.networkproviders.KlystronNodespace;
import com.hbm.ntm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.ntm.uninos.networkproviders.PlasmaNode;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FusionTorusBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyControlReceiver, RORValueProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int COOLANT_TANK_CAPACITY = 4_000;
    public static final int RECIPE_TANK_CAPACITY = 4_000;
    public static final long MAX_POWER = 10_000_000L;
    public static final float MAGNET_ACCELERATION = 0.25F;
    public static final float KELVIN = 273.0F;
    public static final float TEMPERATURE_TARGET = KELVIN - 150.0F;
    public static final float TEMP_CHANGE_PER_MB = 0.5F;
    public static final float TEMP_PASSIVE_HEATING = 2.5F;
    public static final float TEMP_CHANGE_MAX = 5.0F + TEMP_PASSIVE_HEATING;
    private static final String TAG_DID_PROCESS = "didProcess";
    private static final String TAG_SELECTED_RECIPE = "selectedRecipe";
    private static final String TAG_KLYSTRON_ENERGY = "klystronEnergy";
    private static final String TAG_PLASMA_ENERGY = "plasmaEnergy";
    private static final String TAG_FUEL_CONSUMPTION = "fuelConsumption";
    private static final String TAG_PLASMA_R = "plasmaR";
    private static final String TAG_PLASMA_G = "plasmaG";
    private static final String TAG_PLASMA_B = "plasmaB";
    private static final String TAG_CONNECTIONS = "connections";
    private static final String[] ROR = {
            RORInfo.PREFIX_VALUE + "plasma",
            RORInfo.PREFIX_VALUE + "consumption"
    };

    private static final Direction[] NETWORK_DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };

    private final HbmFluidTank coolantTank;
    private final HbmFluidTank hotCoolantTank;
    private final HbmFluidTank[] recipeTanks;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_OUTPUT ? 64 : 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_BLUEPRINT -> stack.is(ModItems.BLUEPRINTS.get());
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private final KlystronNode[] klystronNodes = new KlystronNode[4];
    private final PlasmaNode[] plasmaNodes = new PlasmaNode[4];
    private final boolean[] connections = new boolean[4];

    private String selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
    private double progress;
    private double bonus;
    private boolean didProcess;
    private float temperature = KELVIN + 20.0F;
    private long klystronEnergy;
    private long plasmaEnergy;
    private double fuelConsumption;
    private float plasmaR;
    private float plasmaG;
    private float plasmaB;
    private float magnet;
    private float prevMagnet;
    private float magnetSpeed;
    private Object audio;

    public FusionTorusBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                new HbmFluidTank(HbmFluids.PERFLUOROMETHYL_COLD, COOLANT_TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.PERFLUOROMETHYL, COOLANT_TANK_CAPACITY),
                new HbmFluidTank[] {
                        new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY),
                        new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY),
                        new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY),
                        new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY)
                });
    }

    private FusionTorusBlockEntity(BlockPos pos, BlockState state, HbmFluidTank coolantTank,
            HbmFluidTank hotCoolantTank, HbmFluidTank[] recipeTanks) {
        super(ModBlockEntities.FUSION_TORUS.get(), pos, state, new HbmEnergyStorage(MAX_POWER),
                List.of(coolantTank, hotCoolantTank, recipeTanks[0], recipeTanks[1], recipeTanks[2], recipeTanks[3]));
        this.coolantTank = coolantTank;
        this.hotCoolantTank = hotCoolantTank;
        this.recipeTanks = recipeTanks;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionTorusBlockEntity torus) {
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, torus);
        boolean changed = torus.tickServer(level);
        torus.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            torus.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FusionTorusBlockEntity torus) {
        double powerFactor = getSpeedScaled(torus.getMaxPower(), torus.getPower());
        torus.prevMagnet = torus.magnet;
        torus.magnetSpeed += torus.didProcess ? MAGNET_ACCELERATION : -MAGNET_ACCELERATION;
        torus.magnetSpeed = Math.max(0.0F, Math.min(30.0F * (float) powerFactor, torus.magnetSpeed));
        torus.magnet += torus.magnetSpeed;
        if (torus.magnet >= 360.0F) {
            torus.magnet -= 360.0F;
            torus.prevMagnet -= 360.0F;
        }
        float speed = torus.magnetSpeed / 30.0F;
        torus.audio = LegacyMachineAudioBridge.updateLoop(torus.audio, torus, "FUSION_REACTOR_LOOP",
                torus.magnetSpeed > 0.0F, 50.0D, 30.0F, torus.getVolume(speed), speed);
    }

    public static double getSpeedScaled(double max, double level) {
        if (max == 0.0D) {
            return 0.0D;
        }
        if (level >= max * 0.5D) {
            return 1.0D;
        }
        return level / max * 2.0D;
    }

    public static double outputIntensity(int receiverCount) {
        if (receiverCount == 1) return 1.0D;
        if (receiverCount == 2) return 0.625D;
        if (receiverCount == 3) return 0.5D;
        return 0.4375D;
    }

    public ItemStackHandler getItems() { return items; }
    public HbmFluidTank getCoolantTank() { return coolantTank; }
    public HbmFluidTank getHotCoolantTank() { return hotCoolantTank; }
    public HbmFluidTank getRecipeTank(int index) { return recipeTanks[index]; }
    public boolean getConnection(int index) { return connections[index]; }
    public double getProgress() { return progress; }
    public double getBonus() { return bonus; }
    public boolean didProcess() { return didProcess; }
    public float getTemperature() { return temperature; }
    public long getKlystronEnergy() { return klystronEnergy; }
    public long getPlasmaEnergy() { return plasmaEnergy; }
    public double getFuelConsumption() { return fuelConsumption; }
    public float getPlasmaR() { return plasmaR; }
    public float getPlasmaG() { return plasmaG; }
    public float getPlasmaB() { return plasmaB; }
    public float getMagnet(float partialTick) { return prevMagnet + (magnet - prevMagnet) * partialTick; }
    public List<ItemStack> getDrops() { return HbmInventoryMenuHelper.clearToDrops(items); }

    public void addKlystronEnergy(long amount) {
        if (amount > 0L) {
            klystronEnergy += amount;
            setChanged();
        }
    }

    public String getSelectedRecipeName() {
        return selectedRecipe;
    }

    @Nullable
    public GenericMachineRecipe getSelectedRecipeDefinition() {
        if (level == null) {
            return null;
        }
        return GenericMachineRecipeRuntime.findByInternalName(level, GenericMachineRecipe.Machine.FUSION_REACTOR,
                selectedRecipe);
    }

    public boolean selectRecipe(String selection) {
        if (level == null || GenericMachineRecipeSelector.isNullSelection(selection)) {
            selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
            setChanged();
            return true;
        }
        if (!GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.FUSION_REACTOR,
                selection, items.getStackInSlot(SLOT_BLUEPRINT))) {
            return false;
        }
        selectedRecipe = GenericMachineRecipeSelector.normalize(selection);
        GenericMachineRecipe recipe = GenericMachineRecipeRuntime.findByInternalName(level,
                GenericMachineRecipe.Machine.FUSION_REACTOR, selectedRecipe);
        if (recipe != null) {
            conformRecipeTanks(recipe);
        }
        setChanged();
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    public static CompoundTag recipeSelectionTag(String selection) {
        return GenericMachineRecipeSelector.selectionTag(selection);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.fusionTorus", "Fusion Reactor Vessel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FusionTorusMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(coolantTank, recipeTanks[0], recipeTanks[1], recipeTanks[2]);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(hotCoolantTank, recipeTanks[3]);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return List.of(
                FluidPort.of(0, -1, 0, Direction.DOWN),
                FluidPort.of(0, 5, 0, Direction.UP),
                FluidPort.of(6, -1, 0, Direction.DOWN),
                FluidPort.of(6, 5, 0, Direction.UP),
                FluidPort.of(6, -1, 2, Direction.DOWN),
                FluidPort.of(6, 5, 2, Direction.UP),
                FluidPort.of(6, -1, -2, Direction.DOWN),
                FluidPort.of(6, 5, -2, Direction.UP),
                FluidPort.of(-6, -1, 0, Direction.DOWN),
                FluidPort.of(-6, 5, 0, Direction.UP),
                FluidPort.of(-6, -1, 2, Direction.DOWN),
                FluidPort.of(-6, 5, 2, Direction.UP),
                FluidPort.of(-6, -1, -2, Direction.DOWN),
                FluidPort.of(-6, 5, -2, Direction.UP),
                FluidPort.of(0, -1, 6, Direction.DOWN),
                FluidPort.of(0, 5, 6, Direction.UP),
                FluidPort.of(2, -1, 6, Direction.DOWN),
                FluidPort.of(2, 5, 6, Direction.UP),
                FluidPort.of(-2, -1, 6, Direction.DOWN),
                FluidPort.of(-2, 5, 6, Direction.UP),
                FluidPort.of(0, -1, -6, Direction.DOWN),
                FluidPort.of(0, 5, -6, Direction.UP),
                FluidPort.of(2, -1, -6, Direction.DOWN),
                FluidPort.of(2, 5, -6, Direction.UP),
                FluidPort.of(-2, -1, -6, Direction.DOWN),
                FluidPort.of(-2, 5, -6, Direction.UP));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(EnergyPort.of(0, -1, 0, Direction.DOWN), EnergyPort.of(0, 5, 0, Direction.UP));
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return getReceivingTanks().stream().anyMatch(tank -> tank.getTankType() == type);
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return getSendingTanks().stream().anyMatch(tank -> tank.getTankType() == type && tank.getFill() > 0);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-8, 0, -8), worldPosition.offset(9, 5, 9));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        for (int i = 0; i < recipeTanks.length; i++) {
            recipeTanks[i].writeToNbt(tag, "ft" + i);
        }
        coolantTank.writeToNbt(tag, "t0");
        hotCoolantTank.writeToNbt(tag, "t1");
        tag.putString("recipe0", selectedRecipe);
        tag.putDouble("progress0", progress);
        tag.putDouble("bonus0", bonus);
        tag.putFloat("temperature", temperature);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        for (int i = 0; i < recipeTanks.length; i++) {
            recipeTanks[i].readFromNbt(tag, "ft" + i);
        }
        if (tag.contains("t0_type") || tag.contains("t0")) {
            coolantTank.readFromNbt(tag, "t0");
        }
        if (tag.contains("t1_type") || tag.contains("t1")) {
            hotCoolantTank.readFromNbt(tag, "t1");
        }
        selectedRecipe = tag.getString("recipe0");
        progress = tag.getDouble("progress0");
        bonus = tag.getDouble("bonus0");
        if (tag.contains("temperature")) {
            temperature = tag.getFloat("temperature");
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putString(TAG_SELECTED_RECIPE, selectedRecipe);
        tag.putBoolean(TAG_DID_PROCESS, didProcess);
        tag.putLong(TAG_KLYSTRON_ENERGY, klystronEnergy);
        tag.putLong(TAG_PLASMA_ENERGY, plasmaEnergy);
        tag.putDouble(TAG_FUEL_CONSUMPTION, fuelConsumption);
        tag.putFloat(TAG_PLASMA_R, plasmaR);
        tag.putFloat(TAG_PLASMA_G, plasmaG);
        tag.putFloat(TAG_PLASMA_B, plasmaB);
        byte[] connectionBytes = new byte[connections.length];
        for (int i = 0; i < connections.length; i++) {
            connectionBytes[i] = (byte) (connections[i] ? 1 : 0);
        }
        tag.putByteArray(TAG_CONNECTIONS, connectionBytes);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        selectedRecipe = GenericMachineRecipeSelector.normalize(tag.getString(TAG_SELECTED_RECIPE));
        didProcess = tag.getBoolean(TAG_DID_PROCESS);
        klystronEnergy = tag.getLong(TAG_KLYSTRON_ENERGY);
        plasmaEnergy = tag.getLong(TAG_PLASMA_ENERGY);
        fuelConsumption = tag.getDouble(TAG_FUEL_CONSUMPTION);
        plasmaR = tag.getFloat(TAG_PLASMA_R);
        plasmaG = tag.getFloat(TAG_PLASMA_G);
        plasmaB = tag.getFloat(TAG_PLASMA_B);
        byte[] connectionBytes = tag.getByteArray(TAG_CONNECTIONS);
        for (int i = 0; i < connections.length; i++) {
            connections[i] = i < connectionBytes.length && connectionBytes[i] != 0;
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        writeTank(data, coolantTank);
        writeTank(data, hotCoolantTank);
        data.writeFloat(temperature);
        data.writeLong(getPower());
        data.writeBoolean(didProcess);
        data.writeLong(klystronEnergy);
        data.writeLong(plasmaEnergy);
        data.writeDouble(fuelConsumption);
        data.writeDouble(progress);
        data.writeUtf(selectedRecipe);
        data.writeDouble(bonus);
        data.writeFloat(plasmaR);
        data.writeFloat(plasmaG);
        data.writeFloat(plasmaB);
        for (HbmFluidTank tank : recipeTanks) {
            writeTank(data, tank);
        }
        for (boolean connection : connections) {
            data.writeBoolean(connection);
        }
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        readTank(data, coolantTank);
        readTank(data, hotCoolantTank);
        temperature = data.readFloat();
        setPower(data.readLong());
        didProcess = data.readBoolean();
        klystronEnergy = data.readLong();
        plasmaEnergy = data.readLong();
        fuelConsumption = data.readDouble();
        progress = data.readDouble();
        selectedRecipe = GenericMachineRecipeSelector.normalize(data.readUtf());
        bonus = data.readDouble();
        plasmaR = data.readFloat();
        plasmaG = data.readFloat();
        plasmaB = data.readFloat();
        for (HbmFluidTank tank : recipeTanks) {
            readTank(data, tank);
        }
        for (int i = 0; i < connections.length; i++) {
            connections[i] = data.readBoolean();
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void setRemoved() {
        destroyNodes();
        super.setRemoved();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return GenericMachineRecipeSelector.isSelectionTag(tag)
                && GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.FUSION_REACTOR,
                GenericMachineRecipeSelector.readSelection(tag), items.getStackInSlot(SLOT_BLUEPRINT));
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (GenericMachineRecipeSelector.isSelectionTag(data)) {
            selectRecipe(GenericMachineRecipeSelector.readSelection(data));
        }
    }

    @Override
    public String[] getFunctionInfo() {
        return ROR;
    }

    @Override
    public String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "plasma").equals(name)) {
            return "" + plasmaEnergy;
        }
        if ((RORInfo.PREFIX_VALUE + "consumption").equals(name)) {
            return "" + (int) (fuelConsumption * 100.0D);
        }
        return null;
    }

    private boolean tickServer(Level level) {
        ensureNodes(level);
        HbmBatteryTransfer.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, getMaxPower());
        boolean changed = updateCoolingTemperature();
        int receiverCount = 0;
        int collectors = 0;
        for (int i = 0; i < plasmaNodes.length; i++) {
            PlasmaNetwork net = plasmaNodes[i] == null ? null : plasmaNodes[i].getPlasmaNet();
            connections[i] = false;
            if (klystronNodes[i] != null && klystronNodes[i].getKlystronNet() != null
                    && !klystronNodes[i].getKlystronNet().providerEntries.isEmpty()) {
                connections[i] = true;
            }
            if (net != null && !net.receiverEntries.isEmpty()) {
                connections[i] = true;
                for (Object receiver : new ArrayList<>(net.receiverEntries.keySet())) {
                    if (receiver instanceof FusionPowerReceiver fusionReceiver && fusionReceiver.receivesFusionPower()) {
                        receiverCount++;
                    }
                    if (receiver instanceof FusionCollectorBlockEntity) {
                        collectors++;
                    }
                    break;
                }
            }
        }
        GenericMachineRecipe recipe = findActiveRecipe(level);
        changed |= processRecipe(level, recipe, collectors, receiverCount);
        tryProvideFluidToPorts(hotCoolantTank.getTankType(), hotCoolantTank.getPressure(), this);
        tryProvideFluidToPorts(recipeTanks[3].getTankType(), recipeTanks[3].getPressure(), this);
        klystronEnergy = 0L;
        return changed;
    }

    private void ensureNodes(Level level) {
        for (int i = 0; i < NETWORK_DIRECTIONS.length; i++) {
            Direction direction = NETWORK_DIRECTIONS[i];
            BlockPos nodePos = worldPosition.relative(direction, 7).above(2);
            if (klystronNodes[i] == null || klystronNodes[i].isExpired()) {
                KlystronNode existing = KlystronNodespace.getNode(level, nodePos);
                klystronNodes[i] = existing == null
                        ? KlystronNodespace.createNode(level, new KlystronNode(nodePos, Set.of(direction)))
                        : existing;
            }
            if (plasmaNodes[i] == null || plasmaNodes[i].isExpired()) {
                PlasmaNode existing = PlasmaNodespace.getNode(level, nodePos);
                plasmaNodes[i] = existing == null
                        ? PlasmaNodespace.createNode(level, new PlasmaNode(nodePos, Set.of(direction)))
                        : existing;
            }
            KlystronNetwork kNet = klystronNodes[i].getKlystronNet();
            if (kNet != null) {
                kNet.addReceiver(this);
            }
            PlasmaNetwork pNet = plasmaNodes[i].getPlasmaNet();
            if (pNet != null) {
                pNet.addProvider(this);
            }
        }
    }

    private GenericMachineRecipe findActiveRecipe(Level level) {
        GenericMachineRecipe selected = GenericMachineRecipeRuntime.findByInternalName(level,
                GenericMachineRecipe.Machine.FUSION_REACTOR, selectedRecipe);
        if (selected == null) {
            resetRecipeTanks();
            return null;
        }
        if (!GenericMachineRecipeSelector.isAllowedByBlueprint(selected, items.getStackInSlot(SLOT_BLUEPRINT))) {
            selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
            resetRecipeTanks();
            return null;
        }
        conformRecipeTanks(selected);
        return selected;
    }

    private boolean processRecipe(Level level, @Nullable GenericMachineRecipe recipe, int collectors, int receiverCount) {
        didProcess = false;
        plasmaEnergy = 0L;
        fuelConsumption = 0.0D;
        if (recipe == null) {
            progress = 0.0D;
            return false;
        }
        conformRecipeTanks(recipe);
        GenericMachineRecipeExtraData.Fusion fusion = recipe.getExtraData().fusion().orElse(null);
        if (fusion == null) {
            progress = 0.0D;
            return false;
        }
        double factor = processingFactor(recipe);
        boolean ignition = fusion.ignitionTemp() <= klystronEnergy;
        if (factor <= 0.0D || isTilted() || !isCool() || !ignition || !canRunRecipe(recipe, factor)) {
            progress = 0.0D;
            return false;
        }
        long powerUse = (long) Math.ceil(recipe.getPower() * factor);
        energy.setPower(energy.getPower() - powerUse);
        consumeRecipeFluids(recipe, factor);
        double step = Math.min(factor / Math.max(1, recipe.getDuration()), 1.0D);
        progress += step;
        bonus = Math.min(1.5D, bonus + step * collectors * 0.5D);
        if (progress >= 1.0D) {
            produceRecipeOutputs(recipe);
            progress -= canRunRecipe(recipe, factor) ? 1.0D : progress;
        }
        if (bonus >= 1.0D && canFitItemOutput(recipe)) {
            produceRecipeOutputs(recipe);
            bonus -= 1.0D;
        }
        didProcess = true;
        plasmaEnergy = (long) Math.ceil(fusion.outputTemp() * factor);
        fuelConsumption = factor;
        plasmaR = fusion.r();
        plasmaG = fusion.g();
        plasmaB = fusion.b();
        distributePlasma(receiverCount, fusion.outputFlux() * factor);
        return true;
    }

    private double processingFactor(GenericMachineRecipe recipe) {
        double factor = getSpeedScaled(getMaxPower(), getPower());
        List<HbmFluidStack> inputs = recipe.getFluidInputs();
        for (int i = 0; i < Math.min(3, inputs.size()); i++) {
            factor = Math.min(factor, getSpeedScaled(recipeTanks[i].getMaxFill(), recipeTanks[i].getFill()));
        }
        return factor;
    }

    private void conformRecipeTanks(GenericMachineRecipe recipe) {
        List<HbmFluidStack> inputs = recipe.getFluidInputs();
        for (int i = 0; i < 3; i++) {
            if (i < inputs.size()) {
                recipeTanks[i].conform(inputs.get(i));
            } else {
                recipeTanks[i].resetTank();
            }
        }
        if (!recipe.getFluidOutputs().isEmpty()) {
            recipeTanks[3].conform(recipe.getFluidOutputs().get(0));
        } else {
            recipeTanks[3].resetTank();
        }
    }

    private void resetRecipeTanks() {
        for (HbmFluidTank tank : recipeTanks) {
            tank.resetTank();
        }
    }

    private boolean canRunRecipe(GenericMachineRecipe recipe, double factor) {
        if (recipe.getExtraData().fusion().isEmpty()) {
            return false;
        }
        long powerUse = (long) Math.ceil(recipe.getPower() * Math.max(0.0D, factor));
        if (energy.getPower() < powerUse) {
            return false;
        }
        for (int i = 0; i < Math.min(3, recipe.getFluidInputs().size()); i++) {
            int need = (int) Math.ceil(recipe.getFluidInputs().get(i).amount() * Math.max(0.0D, factor));
            if (recipeTanks[i].getFill() < need) {
                return false;
            }
        }
        if (!canFitItemOutput(recipe)) {
            return false;
        }
        for (int i = 0; i < recipe.getFluidOutputs().size() && i == 0; i++) {
            HbmFluidStack output = recipe.getFluidOutputs().get(i);
            if (recipeTanks[3].getSpaceFor(output.type()) < output.amount()) {
                return false;
            }
        }
        return true;
    }

    private boolean canFitItemOutput(GenericMachineRecipe recipe) {
        if (recipe.getItemOutputEntries().isEmpty()) {
            return true;
        }
        ItemStack output = recipe.getItemOutputEntries().get(0).representativeStack();
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        return existing.isEmpty() || (ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize());
    }

    private void consumeRecipeFluids(GenericMachineRecipe recipe, double factor) {
        for (int i = 0; i < Math.min(3, recipe.getFluidInputs().size()); i++) {
            int amount = (int) Math.ceil(recipe.getFluidInputs().get(i).amount() * factor);
            recipeTanks[i].drain(amount, false);
        }
    }

    private void produceRecipeOutputs(GenericMachineRecipe recipe) {
        if (!recipe.getItemOutputEntries().isEmpty()) {
            ItemStack output = recipe.getItemOutputEntries().get(0).representativeStack();
            ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
            if (existing.isEmpty()) {
                items.setStackInSlot(SLOT_OUTPUT, output.copy());
            } else if (ItemStack.isSameItemSameTags(existing, output)) {
                existing.grow(output.getCount());
                items.setStackInSlot(SLOT_OUTPUT, existing);
            }
        }
        if (!recipe.getFluidOutputs().isEmpty()) {
            HbmFluidStack output = recipe.getFluidOutputs().get(0);
            recipeTanks[3].fill(output.type(), output.amount(), output.pressure(), false);
        }
    }

    private void distributePlasma(int receiverCount, double neutronFlux) {
        if (plasmaEnergy <= 0L) {
            return;
        }
        double intensity = outputIntensity(receiverCount);
        for (PlasmaNode node : plasmaNodes) {
            PlasmaNetwork net = node == null ? null : node.getPlasmaNet();
            if (net == null) {
                continue;
            }
            for (Object receiver : new ArrayList<>(net.receiverEntries.keySet())) {
                if (receiver instanceof FusionPowerReceiver fusionReceiver) {
                    fusionReceiver.receiveFusionPower((long) Math.ceil(plasmaEnergy * intensity),
                            neutronFlux, plasmaR, plasmaG, plasmaB);
                }
            }
        }
    }

    private boolean updateCoolingTemperature() {
        float previousTemperature = temperature;
        int previousCold = coolantTank.getFill();
        int previousHot = hotCoolantTank.getFill();
        temperature += TEMP_PASSIVE_HEATING;
        if (temperature > KELVIN + 20.0F) {
            temperature = KELVIN + 20.0F;
        }
        if (temperature <= TEMPERATURE_TARGET) {
            return previousTemperature != temperature;
        }
        int cyclesTemp = (int) Math.ceil(Math.min(temperature - TEMPERATURE_TARGET, TEMP_CHANGE_MAX)
                / TEMP_CHANGE_PER_MB);
        int cyclesCool = coolantTank.getTankType() == HbmFluids.PERFLUOROMETHYL_COLD
                ? coolantTank.getFill()
                : 0;
        int cyclesHot = hotCoolantTank.getSpaceFor(HbmFluids.PERFLUOROMETHYL);
        int cycles = Math.min(cyclesTemp, Math.min(cyclesCool, cyclesHot));
        if (cycles > 0) {
            coolantTank.drain(cycles, false);
            hotCoolantTank.fill(HbmFluids.PERFLUOROMETHYL, cycles, hotCoolantTank.getPressure(), false);
            temperature -= TEMP_CHANGE_PER_MB * cycles;
        }
        return previousTemperature != temperature || previousCold != coolantTank.getFill()
                || previousHot != hotCoolantTank.getFill();
    }

    private boolean isCool() {
        return temperature <= TEMPERATURE_TARGET;
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        data.writeInt(tank.getFill());
        data.writeInt(tank.getMaxFill());
        data.writeInt(tank.getTankType().getId());
        data.writeShort((short) tank.getPressure());
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        int fill = data.readInt();
        int maxFill = data.readInt();
        FluidType type = HbmFluids.fromId(data.readInt());
        int pressure = data.readShort();
        tank.changeTankSize(maxFill);
        tank.withPressure(pressure);
        tank.setTankType(type);
        tank.setFill(fill);
    }

    private void destroyNodes() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (KlystronNode node : klystronNodes) {
            if (node != null) {
                KlystronNodespace.destroyNode(level, node.getPos());
            }
        }
        for (PlasmaNode node : plasmaNodes) {
            if (node != null) {
                PlasmaNodespace.destroyNode(level, node.getPos());
            }
        }
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override public int getSlots() { return SLOT_COUNT; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return items.getStackInSlot(slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == SLOT_OUTPUT ? stack : items.insertItem(slot, stack, simulate);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == SLOT_OUTPUT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return items.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return items.isItemValid(slot, stack); }
    }
}
