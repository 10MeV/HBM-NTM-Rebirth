package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyCoriumFiniteBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingStep;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.fluid.trait.PwrModeratorFluidTrait;
import com.hbm.ntm.menu.PWRMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.recipe.PWRFuelRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PWRControllerBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyControlReceiver, RORValueProvider, RORInteractive {
    public static final int SLOT_FUEL_INPUT = 0;
    public static final int SLOT_HOT_OUTPUT = 1;
    public static final int SLOT_IDENTIFIER = 2;
    public static final int SLOT_COUNT = 3;
    public static final long CORE_HEAT_CAPACITY_BASE = 10_000_000L;
    public static final long HULL_HEAT_CAPACITY_BASE = 10_000_000L;
    public static final double USE_DISTANCE_SQR = 128.0D;
    private static final int MAX_SIZE = 4096;
    private static final int[] AUTOMATION_SLOTS = {SLOT_FUEL_INPUT, SLOT_HOT_OUTPUT};
    public static final String[] ROR = new String[] {
            RORInfo.PREFIX_VALUE + "rods",
            RORInfo.PREFIX_VALUE + "coreheat",
            RORInfo.PREFIX_VALUE + "hullheat",
            RORInfo.PREFIX_VALUE + "flux",
            RORInfo.PREFIX_VALUE + "depletion",
            RORInfo.PREFIX_FUNCTION + "setrods" + RORInteractive.NAME_SEPARATOR + "percent",
            RORInfo.PREFIX_FUNCTION + "jettison",
    };

    private final HbmFluidTank coolantTank;
    private final HbmFluidTank hotCoolantTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_FUEL_INPUT, SLOT_IDENTIFIER -> !stack.isEmpty();
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private long coreHeat;
    private long coreHeatCapacity = CORE_HEAT_CAPACITY_BASE;
    private long hullHeat;
    private double flux;
    private double rodLevel = 100.0D;
    private double rodTarget = 100.0D;
    private int typeLoaded = -1;
    private int amountLoaded;
    private double progress;
    private double processTime;
    private int rodCount;
    private int connections;
    private int connectionsControlled;
    private int heatexCount;
    private int heatsinkCount;
    private int channelCount;
    private int sourceCount;
    private int unloadDelay;
    private boolean assembled;
    private Object audioLoop;
    private final List<BlockPos> ports = new ArrayList<>();
    private final List<BlockPos> rods = new ArrayList<>();

    public PWRControllerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.COOLANT, 128_000),
                new HbmFluidTank(HbmFluids.COOLANT_HOT, 128_000));
    }

    private PWRControllerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank coolantTank,
            HbmFluidTank hotCoolantTank) {
        super(ModBlockEntities.PWR_CONTROLLER.get(), pos, state, List.of(coolantTank, hotCoolantTank));
        this.coolantTank = coolantTank;
        this.hotCoolantTank = hotCoolantTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PWRControllerBlockEntity pwr) {
        boolean changed = pwr.updatePwrTankTypesFromIdentifier();
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, pwr);
        changed |= pwr.tickServer(level);
        pwr.networkPackNT(150);
        if (changed || level.getGameTime() % 20L == 0L) {
            pwr.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PWRControllerBlockEntity pwr) {
        pwr.audioLoop = LegacyMachineAudioBridge.updateLoop(pwr.audioLoop, pwr,
                "hbm:block.reactorLoop", pwr.amountLoaded > 0, 10.0D, 10.0F, pwr.getVolume(1.0F), 1.0F);
    }

    public void assemble(Player player) {
        if (level == null || level.isClientSide) {
            return;
        }
        AssemblyResult result = scanAssembly(level);
        if (!result.ok()) {
            if (player instanceof ServerPlayer serverPlayer) {
                sendAssemblyErrorMarker(serverPlayer, result.error());
            }
            assembled = false;
            setChanged();
            return;
        }
        for (Map.Entry<BlockPos, BlockState> entry : result.parts().entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState original = entry.getValue();
            if (pos.equals(worldPosition)) {
                continue;
            }
            boolean port = isPwrPort(original);
            level.setBlock(pos, ModBlocks.PWR_BLOCK.get().defaultBlockState()
                    .setValue(com.hbm.ntm.block.PWRAssembledBlock.PORT, port), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof PWRAssembledBlockEntity assembledBlock) {
                assembledBlock.setOriginal(original, worldPosition, port);
            }
        }
        setup(result.parts(), result.rods());
        assembled = true;
        setChanged();
    }

    public void setAssembled(boolean assembled) {
        this.assembled = assembled;
        setChanged();
    }

    public boolean isAssembled() {
        return assembled;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getCoolantTank() {
        return coolantTank;
    }

    public HbmFluidTank getHotCoolantTank() {
        return hotCoolantTank;
    }

    public long getCoreHeat() {
        return coreHeat;
    }

    public long getCoreHeatCapacity() {
        return coreHeatCapacity;
    }

    public long getHullHeat() {
        return hullHeat;
    }

    public int getFluxScaled() {
        return (int) Math.round(flux * 1000.0D);
    }

    public double getFlux() {
        return flux;
    }

    public double getRodLevel() {
        return rodLevel;
    }

    public double getRodTarget() {
        return rodTarget;
    }

    public int getTypeLoaded() {
        return typeLoaded;
    }

    public int getAmountLoaded() {
        return amountLoaded;
    }

    public double getProgress() {
        return progress;
    }

    public double getProcessTime() {
        return processTime;
    }

    public int getRodCount() {
        return rodCount;
    }

    public int getHeatexCount() {
        return heatexCount;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getHeatsinkCount() {
        return heatsinkCount;
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.pwrController", "PWR Controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PWRMenu(containerId, inventory, this);
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= USE_DISTANCE_SQR;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("control")) {
            rodTarget = Math.max(0, Math.min(100, data.getInt("control")));
            setChanged();
        }
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(coolantTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(hotCoolantTank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(coolantTank, hotCoolantTank);
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
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    public long getProviderSpeed(FluidType type, int pressure) {
        return type == hotCoolantTank.getTankType() ? Math.max(1L, hotCoolantTank.getFill()) : 1L;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        List<FluidPort> result = new ArrayList<>();
        for (BlockPos port : ports) {
            BlockPos relative = port.subtract(worldPosition);
            for (Direction direction : Direction.values()) {
                result.add(FluidPort.of(relative.getX() + direction.getStepX(),
                        relative.getY() + direction.getStepY(),
                        relative.getZ() + direction.getStepZ(), direction));
            }
        }
        return result;
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == coolantTank.getTankType() && assembled;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == hotCoolantTank.getTankType() && hotCoolantTank.getFill() > 0 && assembled;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(coolantTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(hotCoolantTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-16, -16, -16), worldPosition.offset(17, 17, 17));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        coolantTank.writeToNbt(tag, "t0");
        hotCoolantTank.writeToNbt(tag, "t1");
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        tag.putBoolean("assembled", assembled);
        tag.putLong("coreHeatL", coreHeat);
        tag.putInt("coreHeat", (int) Math.min(Integer.MAX_VALUE, coreHeat));
        tag.putLong("hullHeatL", hullHeat);
        tag.putInt("hullHeat", (int) Math.min(Integer.MAX_VALUE, hullHeat));
        tag.putInt("fluxScaled", getFluxScaled());
        tag.putDouble("flux", flux);
        tag.putDouble("rodLevel", rodLevel);
        tag.putDouble("rodTarget", rodTarget);
        tag.putInt("typeLoaded", typeLoaded);
        tag.putInt("amountLoaded", amountLoaded);
        tag.putLong("progressL", (long) progress);
        tag.putDouble("progress", progress);
        tag.putLong("processTimeL", (long) processTime);
        tag.putDouble("processTime", processTime);
        tag.putLong("coreHeatCapacityL", coreHeatCapacity);
        tag.putInt("coreHeatCapacity", (int) Math.min(Integer.MAX_VALUE, coreHeatCapacity));
        tag.putInt("connections", connections);
        tag.putInt("connectionsControlled", connectionsControlled);
        tag.putInt("heatexCount", heatexCount);
        tag.putInt("channelCount", channelCount);
        tag.putInt("sourceCount", sourceCount);
        tag.putInt("heatsinkCount", heatsinkCount);
        savePositions(tag, "ports", ports);
        savePositions(tag, "rods", rods);
        saveLegacyPositions(tag, "p", "portCount", ports);
        saveLegacyPositions(tag, "r", "rodCount", rods);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (hasLegacyTankKey(tag, "t0")) {
            coolantTank.readFromNbt(tag, "t0");
        }
        if (hasLegacyTankKey(tag, "t1")) {
            hotCoolantTank.readFromNbt(tag, "t1");
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        assembled = tag.getBoolean("assembled");
        coreHeat = Math.max(tag.getInt("coreHeat"), tag.getLong("coreHeatL"));
        hullHeat = Math.max(tag.getInt("hullHeat"), tag.getLong("hullHeatL"));
        flux = tag.contains("flux") ? tag.getDouble("flux") : tag.getInt("fluxScaled") / 1000.0D;
        rodLevel = tag.contains("rodLevel") ? tag.getDouble("rodLevel") : 100.0D;
        rodTarget = tag.contains("rodTarget") ? tag.getDouble("rodTarget") : 100.0D;
        typeLoaded = tag.contains("typeLoaded") ? tag.getInt("typeLoaded") : -1;
        amountLoaded = tag.getInt("amountLoaded");
        progress = tag.contains("progress") ? tag.getDouble("progress") : tag.getLong("progressL");
        processTime = tag.contains("processTime") ? tag.getDouble("processTime") : tag.getLong("processTimeL");
        coreHeatCapacity = Math.max(CORE_HEAT_CAPACITY_BASE,
                Math.max(tag.getInt("coreHeatCapacity"), tag.getLong("coreHeatCapacityL")));
        connections = tag.getInt("connections");
        connectionsControlled = tag.getInt("connectionsControlled");
        heatexCount = tag.getInt("heatexCount");
        channelCount = tag.getInt("channelCount");
        sourceCount = tag.getInt("sourceCount");
        heatsinkCount = tag.getInt("heatsinkCount");
        ports.clear();
        ports.addAll(loadPositions(tag, "ports"));
        if (ports.isEmpty()) {
            ports.addAll(loadLegacyPositions(tag, "p", "portCount"));
        }
        rods.clear();
        rods.addAll(loadPositions(tag, "rods"));
        if (rods.isEmpty()) {
            rods.addAll(loadLegacyPositions(tag, "r", "rodCount"));
        }
        rodCount = rods.size();
    }

    @Override
    public void serialize(FriendlyByteBuf data) {
        data.writeBoolean(false);
        writeLegacyLoadedTileBinary(data);
        data.writeInt(rodCount);
        data.writeLong(coreHeat);
        data.writeLong(hullHeat);
        data.writeDouble(flux);
        data.writeDouble(processTime);
        data.writeDouble(progress);
        data.writeInt(typeLoaded);
        data.writeInt(amountLoaded);
        data.writeDouble(rodLevel);
        data.writeDouble(rodTarget);
        data.writeLong(coreHeatCapacity);
        writeTank(data, coolantTank);
        writeTank(data, hotCoolantTank);
    }

    @Override
    public void deserialize(FriendlyByteBuf data) {
        if (data.readBoolean()) {
            return;
        }
        readLegacyLoadedTileBinary(data);
        rodCount = data.readInt();
        coreHeat = data.readLong();
        hullHeat = data.readLong();
        flux = data.readDouble();
        processTime = data.readDouble();
        progress = data.readDouble();
        typeLoaded = data.readInt();
        amountLoaded = data.readInt();
        rodLevel = data.readDouble();
        rodTarget = data.readDouble();
        coreHeatCapacity = Math.max(CORE_HEAT_CAPACITY_BASE, data.readLong());
        readTank(data, coolantTank);
        readTank(data, hotCoolantTank);
    }

    @Override
    public String[] getFunctionInfo() {
        return ROR;
    }

    @Override
    public String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "rods").equals(name)) return "" + (int) (100 - rodLevel);
        if ((RORInfo.PREFIX_VALUE + "coreheat").equals(name)) return "" + coreHeat;
        if ((RORInfo.PREFIX_VALUE + "hullheat").equals(name)) return "" + hullHeat;
        if ((RORInfo.PREFIX_VALUE + "flux").equals(name)) return "" + (int) flux;
        if ((RORInfo.PREFIX_VALUE + "depletion").equals(name)) {
            return "" + (processTime <= 0.0D ? 0 : (int) (progress * 100.0D / processTime));
        }
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((RORInfo.PREFIX_FUNCTION + "setrods").equals(name) && params.length > 0) {
            rodTarget = RORInteractive.parseInt(params[0], 0, 100);
            setChanged();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "jettison").equals(name)) {
            jettisonFuel();
            return null;
        }
        return null;
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

    private boolean tickServer(Level level) {
        long oldCore = coreHeat;
        long oldHull = hullHeat;
        double oldFlux = flux;
        int oldInput = coolantTank.getFill();
        int oldOutput = hotCoolantTank.getFill();
        double oldTarget = rodTarget;
        int oldType = typeLoaded;
        int oldAmount = amountLoaded;
        if (!assembled) {
            return oldInput != coolantTank.getFill() || oldOutput != hotCoolantTank.getFill();
        }
        if (!surroundingChunksLoaded(level)) {
            unloadDelay = 60;
        }
        if (unloadDelay > 0) {
            unloadDelay--;
            coreHeat = 0;
            hullHeat = 0;
            return true;
        }
        loadFuelFromSlot();
        approachRods();
        double newFlux = sourceCount * 20.0D;
        if (typeLoaded != -1 && amountLoaded > 0 && rodCount > 0) {
            PWRFuelRuntime.Type fuel = PWRFuelRuntime.type(typeLoaded);
            double usedRods = getTotalProcessMultiplier();
            double fluxPerRod = flux / rodCount;
            double outputPerRod = fuel.curve().eval(fluxPerRod);
            double totalOutput = outputPerRod * amountLoaded * usedRods;
            coreHeat += (long) (totalOutput * fuel.heatEmission());
            newFlux += totalOutput;
            processTime = fuel.yield();
            progress += totalOutput;
            if (progress >= processTime) {
                progress -= processTime;
                tryPushHotFuel(typeLoaded);
                amountLoaded--;
                setChanged();
            }
        }
        if (amountLoaded <= 0) {
            typeLoaded = -1;
            amountLoaded = 0;
            progress = 0.0D;
        }
        amountLoaded = Math.min(amountLoaded, rodCount);
        moveCoreHeatToHull();
        updateCoolant();
        coreHeat = Math.max(0L, (long) (coreHeat * 0.999D));
        hullHeat = Math.max(0L, (long) (hullHeat * 0.999D));
        PwrModeratorFluidTrait moderator = coolantTank.getTankType().getTrait(PwrModeratorFluidTrait.class);
        if (moderator != null && coolantTank.getFill() > 0) {
            newFlux *= moderator.getMultiplier();
        }
        flux = newFlux;
        if (hotCoolantTank.getFill() > 0) {
            tryProvideFluidToPorts(hotCoolantTank.getTankType(), hotCoolantTank.getPressure(), this);
        }
        if (coreHeat > coreHeatCapacity) {
            meltDown(level);
            return true;
        }
        return oldCore != coreHeat || oldHull != hullHeat || oldFlux != flux
                || oldInput != coolantTank.getFill() || oldOutput != hotCoolantTank.getFill()
                || oldTarget != rodTarget || oldType != typeLoaded || oldAmount != amountLoaded;
    }

    private boolean surroundingChunksLoaded(Level level) {
        int chunkX = worldPosition.getX() >> 4;
        int chunkZ = worldPosition.getZ() >> 4;
        return level.hasChunk(chunkX, chunkZ)
                && level.hasChunk(chunkX + 2, chunkZ + 2)
                && level.hasChunk(chunkX + 2, chunkZ - 2)
                && level.hasChunk(chunkX - 2, chunkZ + 2)
                && level.hasChunk(chunkX - 2, chunkZ - 2);
    }

    private void meltDown(Level level) {
        assembled = false;
        typeLoaded = -1;
        amountLoaded = 0;
        progress = 0.0D;
        processTime = 0.0D;
        coreHeat = 0;
        hullHeat = 0;
        coolantTank.setFill(0);
        hotCoolantTank.setFill(0);
        level.destroyBlock(worldPosition, false);

        double x = 0.0D;
        double y = 0.0D;
        double z = 0.0D;
        int count = 0;
        BlockState corium = ModBlocks.CORIUM_BLOCK.get() instanceof LegacyCoriumFiniteBlock finiteCorium
                ? finiteCorium.legacyState(LegacyCoriumFiniteBlock.LEGACY_RBMK_META)
                : ModBlocks.CORIUM_BLOCK.get().defaultBlockState();
        for (BlockPos rod : rods) {
            if (level.getBlockEntity(rod) instanceof PWRAssembledBlockEntity assembledBlock) {
                assembledBlock.suppressRestore();
            }
            level.setBlock(rod, corium, Block.UPDATE_ALL);
            x += rod.getX() + 0.5D;
            y += rod.getY() + 0.5D;
            z += rod.getZ() + 0.5D;
            count++;
        }
        if (count > 0) {
            x /= count;
            y /= count;
            z /= count;
        } else {
            x = worldPosition.getX() + 0.5D;
            y = worldPosition.getY() + 0.5D;
            z = worldPosition.getZ() + 0.5D;
        }
        level.explode(null, x, y, z, 15.0F, true, Level.ExplosionInteraction.BLOCK);
    }

    private void setup(Map<BlockPos, BlockState> parts, Set<BlockPos> rodPositions) {
        rodCount = 0;
        connections = 0;
        connectionsControlled = 0;
        heatexCount = 0;
        channelCount = 0;
        heatsinkCount = 0;
        sourceCount = 0;
        ports.clear();
        rods.clear();
        int connectionsDouble = 0;
        int connectionsControlledDouble = 0;
        for (Map.Entry<BlockPos, BlockState> entry : parts.entrySet()) {
            BlockState state = entry.getValue();
            if (isPwrFuel(state)) {
                rodCount++;
            } else if (isPwrHeatex(state)) {
                heatexCount++;
            } else if (isPwrChannel(state)) {
                channelCount++;
            } else if (isPwrHeatsink(state)) {
                heatsinkCount++;
            } else if (isPwrSource(state)) {
                sourceCount++;
            } else if (isPwrPort(state)) {
                ports.add(entry.getKey().immutable());
            }
        }
        for (BlockPos fuelPos : rodPositions) {
            rods.add(fuelPos.immutable());
            for (Direction direction : Direction.values()) {
                boolean controlled = false;
                for (int i = 1; i < 16; i++) {
                    BlockPos check = fuelPos.relative(direction, i);
                    BlockState atPos = parts.get(check);
                    if (atPos == null || isPwrCasing(atPos)) {
                        break;
                    }
                    if (isPwrControl(atPos)) {
                        controlled = true;
                    }
                    if (isPwrFuel(atPos)) {
                        if (controlled) {
                            connectionsControlledDouble++;
                        } else {
                            connectionsDouble++;
                        }
                        break;
                    }
                    if (isPwrReflector(atPos)) {
                        if (controlled) {
                            connectionsControlledDouble += 2;
                        } else {
                            connectionsDouble += 2;
                        }
                        break;
                    }
                }
            }
        }
        connections = connectionsDouble / 2;
        connectionsControlled = connectionsControlledDouble / 2;
        heatsinkCount = Math.min(heatsinkCount, 80);
        coreHeatCapacity = CORE_HEAT_CAPACITY_BASE + heatsinkCount * (CORE_HEAT_CAPACITY_BASE / 20L);
    }

    private AssemblyResult scanAssembly(Level level) {
        Map<BlockPos, BlockState> parts = new HashMap<>();
        Set<BlockPos> rods = new HashSet<>();
        Set<BlockPos> sources = new HashSet<>();
        Direction facing = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        BlockPos start = worldPosition.relative(facing.getOpposite());
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        parts.put(worldPosition.immutable(), getBlockState());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (parts.containsKey(pos)) {
                continue;
            }
            if (parts.size() >= MAX_SIZE) {
                return AssemblyResult.error("Max size exceeded");
            }
            BlockState state = level.getBlockState(pos);
            if (isValidCasing(state)) {
                parts.put(pos.immutable(), state);
                continue;
            }
            if (isValidCore(state)) {
                parts.put(pos.immutable(), state);
                if (isPwrFuel(state)) {
                    rods.add(pos.immutable());
                }
                if (isPwrSource(state)) {
                    sources.add(pos.immutable());
                }
                for (Direction direction : Direction.values()) {
                    BlockPos next = pos.relative(direction);
                    if (!parts.containsKey(next)) {
                        queue.add(next);
                    }
                }
                continue;
            }
            return AssemblyResult.error("Non-reactor block");
        }
        if (rods.isEmpty()) {
            return AssemblyResult.error("Fuel rods required");
        }
        if (sources.isEmpty()) {
            return AssemblyResult.error("Neutron sources required");
        }
        return new AssemblyResult(true, "", parts, rods);
    }

    private void loadFuelFromSlot() {
        ItemStack input = items.getStackInSlot(SLOT_FUEL_INPUT);
        int index = PWRFuelRuntime.fuelIndex(input);
        if (index < 0) {
            return;
        }
        if ((typeLoaded == -1 || amountLoaded <= 0) || (typeLoaded == index && amountLoaded < rodCount)) {
            typeLoaded = index;
            amountLoaded++;
            items.extractItem(SLOT_FUEL_INPUT, 1, false);
            setChanged();
        }
    }

    private void jettisonFuel() {
        typeLoaded = -1;
        amountLoaded = 0;
        progress = 0.0D;
        setChanged();
    }

    private void sendAssemblyErrorMarker(ServerPlayer player, String message) {
        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_MARKER);
        data.putInt("color", 0xff0000);
        data.putInt("expires", 5_000);
        data.putDouble("dist", 128D);
        if (message != null && !message.isBlank()) {
            data.putString("label", message);
        }
        ModMessages.sendAuxParticle(player, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), data);
    }

    private void tryPushHotFuel(int index) {
        ItemStack product = PWRFuelRuntime.hotProduct(index);
        if (product.isEmpty()) {
            return;
        }
        items.insertItem(SLOT_HOT_OUTPUT, product, false);
    }

    private void approachRods() {
        if (Math.abs(rodLevel - rodTarget) < 1) {
            rodLevel = rodTarget;
        } else if (rodTarget > rodLevel) {
            rodLevel++;
        } else if (rodTarget < rodLevel) {
            rodLevel--;
        }
    }

    private void moveCoreHeatToHull() {
        int coolantRods = Math.max(1, getRodCountForCoolant());
        double coreCoolingApproach = getXOverE(heatexCount * 5.0D / coolantRods, 2.0D) / 2.0D;
        long average = (coreHeat + hullHeat) / 2L;
        coreHeat -= (long) ((coreHeat - average) * coreCoolingApproach);
        hullHeat -= (long) ((hullHeat - average) * coreCoolingApproach);
    }

    private void updateCoolant() {
        HeatableFluidTrait trait = coolantTank.getTankType().getTrait(HeatableFluidTrait.class);
        if (trait == null || trait.getEfficiency(HeatingType.PWR) <= 0.0D) {
            return;
        }
        double coolingEff = channelCount / (double) Math.max(1, getRodCountForCoolant()) * 0.1D;
        coolingEff = Math.min(coolingEff, 1.0D);
        int heatToUse = (int) Math.min(Math.min(hullHeat, (long) (hullHeat * coolingEff
                * trait.getEfficiency(HeatingType.PWR))), 2_000_000_000L);
        HeatingStep step = trait.getFirstStep();
        if (step == null || step.amountRequired() <= 0 || step.amountProduced() <= 0 || step.heatRequired() <= 0) {
            return;
        }
        int coolCycles = coolantTank.getFill() / step.amountRequired();
        int hotCycles = hotCoolantTank.getSpaceFor(step.producedType()) / step.amountProduced();
        int heatCycles = heatToUse / step.heatRequired();
        int cycles = Math.min(coolCycles, Math.min(hotCycles, heatCycles));
        if (cycles <= 0) {
            return;
        }
        hullHeat -= (long) step.heatRequired() * cycles;
        coolantTank.setFill(coolantTank.getFill() - step.amountRequired() * cycles);
        hotCoolantTank.setFill(hotCoolantTank.getFill() + step.amountProduced() * cycles);
    }

    private void setupTanks() {
        HeatableFluidTrait trait = coolantTank.getTankType().getTrait(HeatableFluidTrait.class);
        if (trait == null || trait.getEfficiency(HeatingType.PWR) <= 0.0D) {
            coolantTank.setTankType(HbmFluids.NONE);
            hotCoolantTank.setTankType(HbmFluids.NONE);
            return;
        }
        HeatingStep step = trait.getFirstStep();
        hotCoolantTank.setTankType(step.producedType());
    }

    private boolean updatePwrTankTypesFromIdentifier() {
        FluidType oldCoolantType = coolantTank.getTankType();
        FluidType oldHotType = hotCoolantTank.getTankType();
        int oldCoolantFill = coolantTank.getFill();
        int oldHotFill = hotCoolantTank.getFill();
        boolean changed = setFluidTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, coolantTank);
        setupTanks();
        return changed
                || oldCoolantType != coolantTank.getTankType()
                || oldHotType != hotCoolantTank.getTankType()
                || oldCoolantFill != coolantTank.getFill()
                || oldHotFill != hotCoolantTank.getFill();
    }

    private int getRodCountForCoolant() {
        return rodCount + (int) Math.ceil(heatsinkCount / 4.0D);
    }

    private double getTotalProcessMultiplier() {
        double totalConnections = connections + connectionsControlled * (1.0D - (rodLevel / 100.0D));
        return connectinFunc(totalConnections);
    }

    private double connectinFunc(double connectionCount) {
        return connectionCount / 10.0D * (1.0D - getXOverE(connectionCount, 300.0D))
                + connectionCount / 150.0D * getXOverE(connectionCount, 300.0D);
    }

    private double getXOverE(double x, double d) {
        return 1.0D - Math.pow(Math.E, -x / d);
    }

    private static boolean isValidCore(BlockState state) {
        return isPwrFuel(state) || isPwrControl(state) || isPwrChannel(state)
                || isPwrHeatex(state) || isPwrHeatsink(state) || isPwrSource(state);
    }

    private static boolean isValidCasing(BlockState state) {
        return isPwrCasing(state) || isPwrReflector(state) || isPwrPort(state);
    }

    private static boolean isPwrFuel(BlockState state) {
        return state.is(ModBlocks.PWR_FUEL.get());
    }

    private static boolean isPwrControl(BlockState state) {
        return state.is(ModBlocks.PWR_CONTROL.get());
    }

    private static boolean isPwrChannel(BlockState state) {
        return state.is(ModBlocks.PWR_CHANNEL.get());
    }

    private static boolean isPwrHeatex(BlockState state) {
        return state.is(ModBlocks.PWR_HEATEX.get());
    }

    private static boolean isPwrHeatsink(BlockState state) {
        return state.is(ModBlocks.PWR_HEATSINK.get());
    }

    private static boolean isPwrSource(BlockState state) {
        return state.is(ModBlocks.PWR_NEUTRON_SOURCE.get());
    }

    private static boolean isPwrReflector(BlockState state) {
        return state.is(ModBlocks.PWR_REFLECTOR.get());
    }

    private static boolean isPwrCasing(BlockState state) {
        return state.is(ModBlocks.PWR_CASING.get());
    }

    private static boolean isPwrPort(BlockState state) {
        return state.is(ModBlocks.PWR_PORT.get());
    }

    private static void savePositions(CompoundTag tag, String key, List<BlockPos> positions) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", pos.getX());
            entry.putInt("y", pos.getY());
            entry.putInt("z", pos.getZ());
            list.add(entry);
        }
        tag.put(key, list);
    }

    private static List<BlockPos> loadPositions(CompoundTag tag, String key) {
        List<BlockPos> positions = new ArrayList<>();
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            positions.add(new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z")));
        }
        return positions;
    }

    private static void saveLegacyPositions(CompoundTag tag, String prefix, String countKey, List<BlockPos> positions) {
        tag.putInt(countKey, positions.size());
        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            tag.putIntArray(prefix + i, new int[] {pos.getX(), pos.getY(), pos.getZ()});
        }
    }

    private static List<BlockPos> loadLegacyPositions(CompoundTag tag, String prefix, String countKey) {
        List<BlockPos> positions = new ArrayList<>();
        int count = tag.getInt(countKey);
        for (int i = 0; i < count; i++) {
            String key = prefix + i;
            if (tag.contains(key, Tag.TAG_INT_ARRAY)) {
                int[] pos = tag.getIntArray(key);
                if (pos.length >= 3) {
                    positions.add(new BlockPos(pos[0], pos[1], pos[2]));
                }
            }
        }
        return positions;
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

    private static boolean hasLegacyTankKey(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private record AssemblyResult(boolean ok, String error, Map<BlockPos, BlockState> parts, Set<BlockPos> rods) {
        static AssemblyResult error(String message) {
            return new AssemblyResult(false, message, Map.of(), Set.of());
        }
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return AUTOMATION_SLOTS.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            return mapped == SLOT_FUEL_INPUT && PWRFuelRuntime.isFuel(stack)
                    ? items.insertItem(mapped, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            return mapped == SLOT_HOT_OUTPUT ? items.extractItem(mapped, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return mapped == SLOT_FUEL_INPUT && PWRFuelRuntime.isFuel(stack);
        }

        private int map(int slot) {
            return slot >= 0 && slot < AUTOMATION_SLOTS.length ? AUTOMATION_SLOTS[slot] : -1;
        }
    }
}
