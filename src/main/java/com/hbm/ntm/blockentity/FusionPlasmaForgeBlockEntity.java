package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
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
import com.hbm.ntm.menu.FusionPlasmaForgeMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeExtraData;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
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

public class FusionPlasmaForgeBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, FusionPowerReceiver, HbmLegacyControlReceiver {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_BOOSTER = 2;
    public static final int SLOT_INPUT_START = 3;
    public static final int SLOT_INPUT_END = 14;
    public static final int SLOT_OUTPUT = 15;
    public static final int SLOT_COUNT = 16;
    public static final int TANK_CAPACITY = 16_000;
    public static final long DEFAULT_MAX_POWER = 10_000_000L;
    public static final float ARM_ACCELERATION = 0.18F;
    private static final int[] ACCESSIBLE_SLOTS = {
            SLOT_BOOSTER, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, SLOT_OUTPUT
    };
    private static final int[] INPUT_SLOTS = {
            3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
    };
    private static final int[] OUTPUT_SLOTS = { SLOT_OUTPUT };
    private static final String TAG_POWER = "power";
    private static final String TAG_MAX_POWER = "maxPower";
    private static final String TAG_SELECTED_RECIPE = "recipe0";
    private static final String TAG_PROGRESS = "progress0";
    private static final String TAG_MODERN_SELECTED_RECIPE = "recipe";
    private static final String TAG_MODERN_PROGRESS = "progress";
    private static final String TAG_BOOSTER = "booster";
    private static final String TAG_MAX_BOOSTER = "maxBooster";
    private static final String TAG_DID_PROCESS = "didProcess";
    private static final String TAG_CONNECTED = "connected";
    private static final String TAG_PLASMA_ENERGY = "plasmaEnergy";
    private static final String TAG_PLASMA_ENERGY_SYNC = "plasmaEnergySync";
    private static final String TAG_NEUTRON_ENERGY = "neutronEnergy";
    private static final String TAG_PLASMA_R = "plasmaR";
    private static final String TAG_PLASMA_G = "plasmaG";
    private static final String TAG_PLASMA_B = "plasmaB";

    private final HbmFluidTank inputTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_BLUEPRINT ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> true;
                case SLOT_BLUEPRINT -> stack.is(ModItems.BLUEPRINTS.get());
                case SLOT_BOOSTER -> boosterValue(stack) > 0;
                case SLOT_OUTPUT -> false;
                default -> isRecipeInputValid(slot, stack);
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private PlasmaNode receiverNode;
    private PlasmaNode providerNode;
    private String selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
    private double progress;
    private boolean didProcess;
    private boolean connected;
    private int booster;
    private int maxBooster;
    private long plasmaEnergy;
    private long plasmaEnergySync;
    private double neutronEnergy;
    private float plasmaR;
    private float plasmaG;
    private float plasmaB;
    private float arm;
    private float prevArm;
    private float armSpeed;
    private int timeOffset = -1;
    private double prevRing;
    private double ring;
    private double ringSpeed;
    private double ringTarget;
    private int ringDelay;
    private final ForgeArm armStriker = new ForgeArm(ForgeArmType.STRIKER);
    private final ForgeArm armJet = new ForgeArm(ForgeArmType.JET);

    public FusionPlasmaForgeBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private FusionPlasmaForgeBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank) {
        super(ModBlockEntities.FUSION_PLASMA_FORGE.get(), pos, state, new HbmEnergyStorage(DEFAULT_MAX_POWER),
                List.of(inputTank));
        this.inputTank = inputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionPlasmaForgeBlockEntity forge) {
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, forge);
        boolean changed = forge.tickServer(level);
        forge.networkPackNT(100);
        if (changed || level.getGameTime() % 20L == 0L) {
            forge.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FusionPlasmaForgeBlockEntity forge) {
        forge.prevArm = forge.arm;
        forge.armSpeed += forge.didProcess ? ARM_ACCELERATION : -ARM_ACCELERATION;
        forge.armSpeed = Math.max(0.0F, Math.min(1.0F, forge.armSpeed));
        forge.arm += forge.armSpeed;
        if (forge.arm > 1.0F) {
            forge.arm -= 1.0F;
            forge.prevArm -= 1.0F;
        }
        if (forge.timeOffset == -1) {
            forge.timeOffset = level.random.nextInt(30_000);
        }
        forge.armStriker.updateArm(forge, level);
        forge.armJet.updateArm(forge, level);
        forge.prevRing = forge.ring;
        if (forge.didProcess) {
            if (forge.ring != forge.ringTarget) {
                double ringDelta = Math.abs(forge.ringTarget - forge.ring);
                if (ringDelta <= forge.ringSpeed) {
                    forge.ring = forge.ringTarget;
                }
                if (forge.ringTarget > forge.ring) {
                    forge.ring += forge.ringSpeed;
                }
                if (forge.ringTarget < forge.ring) {
                    forge.ring -= forge.ringSpeed;
                }
                if (forge.ringTarget == forge.ring) {
                    double sub = forge.ringTarget >= 360.0D ? -360.0D : 360.0D;
                    forge.ringTarget += sub;
                    forge.ring += sub;
                    forge.prevRing += sub;
                    forge.ringDelay = 100 + level.random.nextInt(41);
                }
            } else {
                if (forge.ringDelay > 0) {
                    forge.ringDelay--;
                }
                if (forge.ringDelay <= 0) {
                    forge.ringTarget += (level.random.nextDouble() + 1.0D) * 60.0D
                            * (level.random.nextBoolean() ? -1.0D : 1.0D);
                    forge.ringSpeed = 2.5D;
                }
            }
        }
    }

    public ItemStackHandler getItems() { return items; }
    public HbmFluidTank getInputTank() { return inputTank; }
    public long getPower() { return energy.getPower(); }
    public long getMaxPower() { return energy.getMaxPower(); }
    public double getProgress() { return progress; }
    public boolean didProcess() { return didProcess; }
    public boolean isConnected() { return connected; }
    public int getBooster() { return booster; }
    public int getMaxBooster() { return maxBooster; }
    public long getPlasmaEnergySync() { return plasmaEnergySync; }
    public float getPlasmaR() { return plasmaR; }
    public float getPlasmaG() { return plasmaG; }
    public float getPlasmaB() { return plasmaB; }
    public int getTimeOffset() { return timeOffset; }
    public float getArm(float partialTick) { return prevArm + (arm - prevArm) * partialTick; }
    public double getRotor(float partialTick) { return prevRing + (ring - prevRing) * partialTick; }
    public double[] getStrikerPositions(float partialTick) { return armStriker.getPositions(partialTick); }
    public double[] getJetPositions(float partialTick) { return armJet.getPositions(partialTick); }
    public boolean isJetStableAwayFromHome() {
        return armJet.angles[2] == armJet.prevAngles[2] && armJet.angles[2] != 0.0D;
    }
    public List<ItemStack> getDrops() { return HbmInventoryMenuHelper.clearToDrops(items); }

    public String getSelectedRecipeName() {
        return selectedRecipe;
    }

    @Nullable
    public GenericMachineRecipe getSelectedRecipeDefinition() {
        if (level == null) {
            return null;
        }
        return GenericMachineRecipeRuntime.findByInternalName(level, GenericMachineRecipe.Machine.PLASMA_FORGE,
                selectedRecipe);
    }

    public boolean selectRecipe(String selection) {
        if (level == null || GenericMachineRecipeSelector.isNullSelection(selection)) {
            selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
            progress = 0.0D;
            setChanged();
            return true;
        }
        if (!GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.PLASMA_FORGE,
                selection, items.getStackInSlot(SLOT_BLUEPRINT))) {
            return false;
        }
        selectedRecipe = GenericMachineRecipeSelector.normalize(selection);
        conformActiveRecipeTank();
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
    public boolean receivesFusionPower() {
        return true;
    }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        plasmaEnergy = Math.max(0L, fusionPower);
        neutronEnergy = Math.max(0.0D, neutronPower);
        plasmaR = r;
        plasmaG = g;
        plasmaB = b;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machinePlasmaForge", "Plasma Forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FusionPlasmaForgeMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of();
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
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        List<FluidPort> ports = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            ports.add(FluidPort.of(facing.getStepX() * 6 + rot.getStepX() * i, 0,
                    facing.getStepZ() * 6 + rot.getStepZ() * i, facing));
            ports.add(FluidPort.of(-facing.getStepX() * 6 + rot.getStepX() * i, 0,
                    -facing.getStepZ() * 6 + rot.getStepZ() * i, facing.getOpposite()));
        }
        return ports;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        List<EnergyPort> ports = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            ports.add(EnergyPort.of(facing.getStepX() * 6 + rot.getStepX() * i, 0,
                    facing.getStepZ() * 6 + rot.getStepZ() * i, facing));
            ports.add(EnergyPort.of(-facing.getStepX() * 6 + rot.getStepX() * i, 0,
                    -facing.getStepZ() * 6 + rot.getStepZ() * i, facing.getOpposite()));
        }
        return ports;
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == inputTank.getTankType() && type != HbmFluids.NONE;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-5, 0, -5), worldPosition.offset(5, 6, 6));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        inputTank.writeToNbt(tag, "i");
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putLong(TAG_MAX_POWER, energy.getMaxPower());
        tag.putString(TAG_SELECTED_RECIPE, selectedRecipe);
        tag.putDouble(TAG_PROGRESS, progress);
        tag.putInt(TAG_BOOSTER, booster);
        tag.putInt(TAG_MAX_BOOSTER, maxBooster);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        if (hasTankTag(tag, "i")) {
            inputTank.readFromNbt(tag, "i");
        }
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        if (tag.contains(TAG_MAX_POWER)) {
            long maxPower = tag.getLong(TAG_MAX_POWER);
            energy.setMaxPower(maxPower);
            energy.setTransferRates(maxPower, 0L);
        }
        selectedRecipe = GenericMachineRecipeSelector.normalize(
                tag.contains(TAG_SELECTED_RECIPE) ? tag.getString(TAG_SELECTED_RECIPE)
                        : tag.getString(TAG_MODERN_SELECTED_RECIPE));
        progress = tag.contains(TAG_PROGRESS) ? tag.getDouble(TAG_PROGRESS) : tag.getDouble(TAG_MODERN_PROGRESS);
        booster = tag.getInt(TAG_BOOSTER);
        maxBooster = tag.getInt(TAG_MAX_BOOSTER);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putString(TAG_SELECTED_RECIPE, selectedRecipe);
        tag.putBoolean(TAG_DID_PROCESS, didProcess);
        tag.putBoolean(TAG_CONNECTED, connected);
        tag.putLong(TAG_PLASMA_ENERGY_SYNC, plasmaEnergySync);
        tag.putFloat(TAG_PLASMA_R, plasmaR);
        tag.putFloat(TAG_PLASMA_G, plasmaG);
        tag.putFloat(TAG_PLASMA_B, plasmaB);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        selectedRecipe = GenericMachineRecipeSelector.normalize(tag.getString(TAG_SELECTED_RECIPE));
        didProcess = tag.getBoolean(TAG_DID_PROCESS);
        connected = tag.getBoolean(TAG_CONNECTED);
        plasmaEnergySync = tag.getLong(TAG_PLASMA_ENERGY_SYNC);
        plasmaR = tag.getFloat(TAG_PLASMA_R);
        plasmaG = tag.getFloat(TAG_PLASMA_G);
        plasmaB = tag.getFloat(TAG_PLASMA_B);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        writeTank(data, inputTank);
        data.writeFloat(plasmaR);
        data.writeFloat(plasmaG);
        data.writeFloat(plasmaB);
        data.writeLong(plasmaEnergySync);
        data.writeLong(energy.getPower());
        data.writeLong(energy.getMaxPower());
        data.writeBoolean(didProcess);
        data.writeBoolean(connected);
        data.writeInt(booster);
        data.writeInt(maxBooster);
        data.writeDouble(progress);
        data.writeUtf(selectedRecipe);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        readTank(data, inputTank);
        plasmaR = data.readFloat();
        plasmaG = data.readFloat();
        plasmaB = data.readFloat();
        plasmaEnergySync = data.readLong();
        energy.setPower(data.readLong());
        energy.setMaxPower(data.readLong());
        energy.setTransferRates(energy.getMaxPower(), 0L);
        didProcess = data.readBoolean();
        connected = data.readBoolean();
        booster = data.readInt();
        maxBooster = data.readInt();
        progress = data.readDouble();
        selectedRecipe = GenericMachineRecipeSelector.normalize(data.readUtf());
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return GenericMachineRecipeSelector.isSelectionTag(tag)
                && hasLegacyUseDistance(player)
                && GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.PLASMA_FORGE,
                GenericMachineRecipeSelector.readSelection(tag), items.getStackInSlot(SLOT_BLUEPRINT));
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (GenericMachineRecipeSelector.isSelectionTag(data)) {
            selectRecipe(GenericMachineRecipeSelector.readSelection(data));
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

    private boolean tickServer(Level level) {
        ensureNodes(level);
        long oldPower = energy.getPower();
        long oldMaxPower = energy.getMaxPower();
        double oldProgress = progress;
        boolean oldProcess = didProcess;
        int oldBooster = booster;
        plasmaEnergySync = plasmaEnergy;
        plasmaEnergy = 0L;

        GenericMachineRecipe recipe = activeRecipe(level);
        updateMaxPower(recipe);
        HbmBatteryTransfer.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, getMaxPower());
        if (inputTank.getTankType() != HbmFluids.NONE) {
            refreshTrackedReceiverFluidPortsReport(List.of(inputTank), this);
        }
        runBooster();
        boolean ignition = recipe == null || recipe.getExtraData().plasmaForge()
                .map(extra -> extra.ignitionTemp() <= plasmaEnergySync)
                .orElse(false);
        GenericMachineRecipeRuntime.ProcessingFactors factors =
                new GenericMachineRecipeRuntime.ProcessingFactors(booster > 0 ? 4.0D : 1.0D, 1.0D);
        GenericMachineRecipeRuntime.ProcessingResult result = GenericMachineRecipeRuntime.update(
                level, GenericMachineRecipe.Machine.PLASMA_FORGE, selectedRecipe, progress,
                items.getStackInSlot(SLOT_BLUEPRINT), energy, items, INPUT_SLOTS, OUTPUT_SLOTS,
                List.of(inputTank), List.of(), factors, ignition && recipe != null, TANK_CAPACITY);
        selectedRecipe = result.selectedRecipe();
        progress = result.progress();
        didProcess = result.didProcess();
        if (didProcess && booster > 0) {
            booster--;
        }
        distributePlasma();
        neutronEnergy = 0.0D;
        return result.changed() || oldPower != energy.getPower() || oldProgress != progress
                || oldProcess != didProcess || oldBooster != booster || oldMaxPower != energy.getMaxPower();
    }

    @Nullable
    private GenericMachineRecipe activeRecipe(Level level) {
        GenericMachineRecipe selected = GenericMachineRecipeRuntime.findByInternalName(level,
                GenericMachineRecipe.Machine.PLASMA_FORGE, selectedRecipe);
        if (selected != null
                && GenericMachineRecipeSelector.isAllowedByBlueprint(selected, items.getStackInSlot(SLOT_BLUEPRINT))) {
            GenericMachineRecipeRuntime.setupTanks(selected, List.of(inputTank), List.of(), TANK_CAPACITY);
            return selected;
        }
        for (GenericMachineRecipe recipe : GenericMachineRecipeSelector.recipes(level,
                GenericMachineRecipe.Machine.PLASMA_FORGE, items.getStackInSlot(SLOT_BLUEPRINT))) {
            if (GenericMachineRecipeRuntime.canProcess(recipe, items, INPUT_SLOTS, OUTPUT_SLOTS,
                    List.of(inputTank), List.of())) {
                selectedRecipe = recipe.getInternalName();
                GenericMachineRecipeRuntime.setupTanks(recipe, List.of(inputTank), List.of(), TANK_CAPACITY);
                return recipe;
            }
        }
        return selected;
    }

    private void conformActiveRecipeTank() {
        if (level == null) {
            return;
        }
        GenericMachineRecipe recipe = GenericMachineRecipeRuntime.findByInternalName(level,
                GenericMachineRecipe.Machine.PLASMA_FORGE, selectedRecipe);
        GenericMachineRecipeRuntime.setupTanks(recipe, List.of(inputTank), List.of(), TANK_CAPACITY);
    }

    private void updateMaxPower(@Nullable GenericMachineRecipe recipe) {
        long target = energy.getMaxPower() <= 0L ? 1_000_000L : energy.getMaxPower();
        if (recipe != null) {
            target = recipe.getPower() * 100L;
        }
        target = Math.max(target, energy.getPower());
        target = Math.max(target, 100_000L);
        energy.setMaxPower(target);
        energy.setTransferRates(target, 0L);
    }

    private void runBooster() {
        if (booster > 0) {
            return;
        }
        maxBooster = 0;
        ItemStack stack = items.getStackInSlot(SLOT_BOOSTER);
        int value = boosterValue(stack);
        if (value <= 0) {
            return;
        }
        items.extractItem(SLOT_BOOSTER, 1, false);
        booster = value;
        maxBooster = value;
    }

    private void ensureNodes(Level level) {
        Direction facing = facing();
        Direction receiverDirection = facing;
        Direction providerDirection = facing.getOpposite();
        BlockPos receiverPos = worldPosition.relative(receiverDirection, 5).above(2);
        BlockPos providerPos = worldPosition.relative(providerDirection, 5).above(2);
        if (receiverNode == null || receiverNode.isExpired()) {
            PlasmaNode existing = PlasmaNodespace.getNode(level, receiverPos);
            receiverNode = existing == null
                    ? PlasmaNodespace.createNode(level, new PlasmaNode(receiverPos, Set.of(receiverDirection)))
                    : existing;
        }
        if (providerNode == null || providerNode.isExpired()) {
            PlasmaNode existing = PlasmaNodespace.getNode(level, providerPos);
            providerNode = existing == null
                    ? PlasmaNodespace.createNode(level, new PlasmaNode(providerPos, Set.of(providerDirection)))
                    : existing;
        }
        PlasmaNetwork receiverNet = receiverNode.getPlasmaNet();
        if (receiverNet != null) {
            receiverNet.addReceiver(this);
        }
        PlasmaNetwork providerNet = providerNode.getPlasmaNet();
        if (providerNet != null) {
            providerNet.addProvider(this);
        }
    }

    private void distributePlasma() {
        connected = false;
        PlasmaNetwork net = providerNode == null ? null : providerNode.getPlasmaNet();
        if (net == null || net.receiverEntries.isEmpty()) {
            return;
        }
        connected = true;
        long powerReceived = (long) Math.ceil(plasmaEnergySync * 0.75D);
        if (powerReceived <= 0L) {
            return;
        }
        for (Object receiver : new ArrayList<>(net.receiverEntries.keySet())) {
            if (receiver instanceof FusionPowerReceiver fusionReceiver) {
                fusionReceiver.receiveFusionPower(powerReceived, neutronEnergy, plasmaR, plasmaG, plasmaB);
            }
        }
    }

    private void destroyNodes() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (receiverNode != null) {
            PlasmaNodespace.destroyNode(level, receiverNode.getPos());
        }
        if (providerNode != null) {
            PlasmaNodespace.destroyNode(level, providerNode.getPos());
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private static boolean isInputSlot(int slot) {
        return slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END;
    }

    private boolean isRecipeInputValid(int slot, ItemStack stack) {
        if (!isInputSlot(slot) || stack.isEmpty() || level == null) {
            return false;
        }
        GenericMachineRecipe recipe = getSelectedRecipeDefinition();
        if (recipe == null) {
            return false;
        }
        int inputIndex = slot - SLOT_INPUT_START;
        List<HbmIngredient> inputs = recipe.getItemInputs();
        if (inputIndex < inputs.size() && inputs.get(inputIndex).test(stack, true)) {
            return true;
        }
        if (inputIndex != 0 || recipe.getAutoSwitchGroup() == null) {
            return false;
        }
        String group = recipe.getAutoSwitchGroup();
        ItemStack blueprint = items.getStackInSlot(SLOT_BLUEPRINT);
        for (GenericMachineRecipe candidate : GenericMachineRecipeSelector.recipes(level,
                GenericMachineRecipe.Machine.PLASMA_FORGE, blueprint)) {
            if (!group.equals(candidate.getAutoSwitchGroup()) || candidate.getItemInputs().isEmpty()) {
                continue;
            }
            if (candidate.getItemInputs().get(0).test(stack, true)) {
                return true;
            }
        }
        return false;
    }

    private boolean canExtractExternalSlot(int slot) {
        return slot == SLOT_OUTPUT || GenericMachineRecipeRuntime.isSlotClogged(getSelectedRecipeDefinition(),
                GenericMachineRecipe.Machine.PLASMA_FORGE, level, items, slot, INPUT_SLOTS);
    }

    private static int boosterValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        if (matches(item, "nugget_co60")) return 20;
        if (matches(item, "billet_co60")) return 120;
        if (matches(item, "ingot_co60") || matches(item, "powder_co60")) return 200;
        if (matches(item, "nugget_sr90") || matches(item, "powder_sr90_tiny")) return 40;
        if (matches(item, "billet_sr90")) return 240;
        if (matches(item, "ingot_sr90") || matches(item, "powder_sr90")) return 400;
        if (matches(item, "nugget_au198")) return 60;
        if (matches(item, "billet_au198")) return 360;
        if (matches(item, "ingot_au198") || matches(item, "powder_au198")) return 600;
        if (matches(item, "powder_i131_tiny") || matches(item, "powder_xe135_tiny")) return 60;
        if (matches(item, "powder_i131") || matches(item, "powder_xe135")) return 600;
        if (matches(item, "powder_cs137_tiny")) return 50;
        if (matches(item, "powder_cs137")) return 500;
        if (matches(item, "powder_at209")) return 1200;
        return 0;
    }

    private static boolean matches(Item item, String legacyName) {
        return ModItems.legacyItem(legacyName) != null && item == ModItems.legacyItem(legacyName).get();
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private boolean hasLegacyUseDistance(Player player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 128.0D;
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

    private final class AccessibleItemHandler implements IItemHandler {
        @Override public int getSlots() { return ACCESSIBLE_SLOTS.length; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            return mapped >= 0 && mapped != SLOT_OUTPUT ? items.insertItem(mapped, stack, simulate) : stack;
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            return mapped >= 0 && canExtractExternalSlot(mapped) ? items.extractItem(mapped, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return mapped >= 0 && mapped != SLOT_OUTPUT && items.isItemValid(mapped, stack);
        }

        private int map(int slot) {
            return slot >= 0 && slot < ACCESSIBLE_SLOTS.length ? ACCESSIBLE_SLOTS[slot] : -1;
        }
    }

    public static final class ForgeArm {
        private final ForgeArmType type;
        private ForgeArmState state = ForgeArmState.RETIRE;
        private final double[] angles;
        private final double[] prevAngles;
        private final double[] targetAngles;
        private final double[] speed;
        private int actionDelay;

        private ForgeArm(ForgeArmType type) {
            this.type = type;
            this.angles = new double[type.angleCount];
            this.prevAngles = new double[type.angleCount];
            this.targetAngles = new double[type.angleCount];
            this.speed = new double[type.angleCount];
            for (int i = 0; i < speed.length; i++) {
                speed[i] = i > 4 ? 0.5D : 15.0D;
            }
        }

        private void updateArm(FusionPlasmaForgeBlockEntity forge, Level level) {
            System.arraycopy(angles, 0, prevAngles, 0, angles.length);
            if (!forge.didProcess) {
                state = ForgeArmState.RETIRE;
            }
            if (state == ForgeArmState.RETIRE) {
                actionDelay = 0;
            }
            if (actionDelay > 0) {
                actionDelay--;
                return;
            }
            type.update(this, forge, level);
        }

        private boolean move() {
            boolean didMove = false;
            for (int i = 0; i < angles.length; i++) {
                if (angles[i] == targetAngles[i]) {
                    continue;
                }
                didMove = true;
                double delta = Math.abs(angles[i] - targetAngles[i]);
                if (delta <= speed[i]) {
                    angles[i] = targetAngles[i];
                } else if (angles[i] < targetAngles[i]) {
                    angles[i] += speed[i];
                } else {
                    angles[i] -= speed[i];
                }
            }
            return !didMove;
        }

        private double[] getPositions(float partialTick) {
            double[] pos = new double[angles.length];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = prevAngles[i] + (angles[i] - prevAngles[i]) * partialTick;
            }
            return pos;
        }
    }

    public enum ForgeArmType {
        STRIKER(6) {
            @Override
            void update(ForgeArm arm, FusionPlasmaForgeBlockEntity forge, Level level) {
                switch (arm.state) {
                    case REPOSITION -> {
                        if (arm.move()) {
                            arm.actionDelay = 5;
                            arm.state = ForgeArmState.EXTEND1;
                            arm.targetAngles[4] = 0.5D;
                        }
                    }
                    case EXTEND1 -> {
                        if (arm.move()) {
                            arm.actionDelay = 0;
                            arm.state = ForgeArmState.RETRACT1;
                            arm.targetAngles[4] = 0.0D;
                            playStrikerSound(forge, level);
                        }
                    }
                    case RETRACT1 -> {
                        if (arm.move()) {
                            arm.actionDelay = 0;
                            arm.state = ForgeArmState.EXTEND2;
                            arm.targetAngles[5] = 0.5D;
                        }
                    }
                    case EXTEND2 -> {
                        if (arm.move()) {
                            arm.actionDelay = 0;
                            arm.state = ForgeArmState.RETRACT2;
                            arm.targetAngles[5] = 0.0D;
                            playStrikerSound(forge, level);
                        }
                    }
                    case RETRACT2 -> {
                        if (arm.move()) {
                            if (level.random.nextInt(3) == 0) {
                                arm.actionDelay = 10;
                                arm.state = ForgeArmState.REPOSITION;
                                choosePosition(arm, STRIKER_POSITIONS, level);
                            } else {
                                arm.actionDelay = 5;
                                arm.state = ForgeArmState.EXTEND1;
                                arm.targetAngles[4] = 0.5D;
                            }
                        }
                    }
                    case RETIRE -> {
                        for (int i = 0; i < arm.targetAngles.length; i++) {
                            arm.targetAngles[i] = 0.0D;
                        }
                        if (arm.move()) {
                            arm.actionDelay = 10;
                            arm.state = ForgeArmState.REPOSITION;
                            choosePosition(arm, STRIKER_POSITIONS, level);
                        }
                    }
                }
            }
        },
        JET(4) {
            @Override
            void update(ForgeArm arm, FusionPlasmaForgeBlockEntity forge, Level level) {
                switch (arm.state) {
                    case REPOSITION -> {
                        if (arm.move()) {
                            arm.actionDelay = 20 + level.random.nextInt(3) * 10;
                            arm.state = ForgeArmState.REPOSITION;
                            choosePosition(arm, JET_POSITIONS, level);
                        }
                    }
                    case RETIRE -> {
                        for (int i = 0; i < arm.targetAngles.length; i++) {
                            arm.targetAngles[i] = 0.0D;
                        }
                        if (arm.move()) {
                            arm.actionDelay = 10;
                            arm.state = ForgeArmState.REPOSITION;
                            choosePosition(arm, JET_POSITIONS, level);
                        }
                    }
                    default -> {
                    }
                }
            }
        };

        private final int angleCount;

        ForgeArmType(int angleCount) {
            this.angleCount = angleCount;
        }

        abstract void update(ForgeArm arm, FusionPlasmaForgeBlockEntity forge, Level level);
    }

    public enum ForgeArmState {
        REPOSITION,
        EXTEND1,
        EXTEND2,
        RETRACT1,
        RETRACT2,
        RETIRE
    }

    private static final double[][] STRIKER_POSITIONS = {
            {20.0D, -30.0D, -20.0D, 30.0D},
            {45.0D, -80.0D, 15.0D, 30.0D},
            {30.0D, -45.0D, -10.0D, 30.0D},
            {15.0D, -20.0D, -30.0D, 30.0D},
            {0.0D, 10.0D, -55.0D, 30.0D}
    };
    private static final double[][] JET_POSITIONS = {
            {10.0D, 45.0D, -120.0D},
            {20.0D, 45.0D, -140.0D},
            {0.0D, 30.0D, -80.0D},
            {0.0D, 40.0D, -100.0D},
            {30.0D, 50.0D, -160.0D}
    };

    private static void choosePosition(ForgeArm arm, double[][] positions, Level level) {
        double[] newPos = positions[level.random.nextInt(positions.length)];
        for (int i = 0; i < newPos.length; i++) {
            arm.targetAngles[i] = newPos[i];
        }
    }

    private static void playStrikerSound(FusionPlasmaForgeBlockEntity forge, Level level) {
        level.playLocalSound(forge.worldPosition.getX() + 0.5D, forge.worldPosition.getY() + 0.5D,
                forge.worldPosition.getZ() + 0.5D, ModSounds.ITEM_BOLTGUN.get(), SoundSource.BLOCKS,
                forge.getVolume(0.25F), 1.25F, false);
    }
}
