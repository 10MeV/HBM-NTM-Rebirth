package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.OilDrillMenu;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OilDrillBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmPersistentBlockState, HbmStandardFluidTransceiver, MenuProvider {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_INDICATOR = "indicator";
    private static final String TAG_SPEED_LEVEL = "speedLevel";
    private static final String TAG_ENERGY_LEVEL = "energyLevel";
    private static final String TAG_OVER_LEVEL = "overLevel";
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_OIL_CONTAINER = 1;
    public static final int SLOT_OIL_CONTAINER_OUTPUT = 2;
    public static final int SLOT_GAS_CONTAINER = 3;
    public static final int SLOT_GAS_CONTAINER_OUTPUT = 4;
    public static final int SLOT_UPGRADE_START = 5;
    public static final int SLOT_UPGRADE_END = 7;
    public static final int ITEM_COUNT = 8;

    public static final int INDICATOR_OK = 0;
    public static final int INDICATOR_COMPLETE = 1;
    public static final int INDICATOR_BLOCKED = 2;
    public static final int INDICATOR_NO_FRACKSOL = 3;

    private final Kind kind;
    private final HbmFluidTank oilTank;
    private final HbmFluidTank gasTank;
    @Nullable
    private final HbmFluidTank frackingTank;
    private final ItemStackHandler items = new ItemStackHandler(ITEM_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_OIL_CONTAINER, SLOT_GAS_CONTAINER -> true;
                case SLOT_UPGRADE_START, SLOT_UPGRADE_START + 1, SLOT_UPGRADE_END ->
                        stack.getItem() instanceof ItemMachineUpgrade;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalItemHandler =
            LazyOptional.of(() -> new OilDrillExternalItemHandler(items));

    private int indicator;
    private int speedLevel;
    private int energyLevel;
    private int overLevel = 1;

    public OilDrillBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, Kind.fromState(state));
    }

    private OilDrillBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        this(pos, state, kind, tank(HbmFluids.OIL, 64_000), tank(HbmFluids.GAS, 64_000),
                kind == Kind.FRACKING_TOWER ? tank(HbmFluids.FRACKSOL, 64_000) : null);
    }

    private OilDrillBlockEntity(BlockPos pos, BlockState state, Kind kind, HbmFluidTank oilTank,
            HbmFluidTank gasTank, @Nullable HbmFluidTank frackingTank) {
        super(ModBlockEntities.OIL_DRILL.get(), pos, state,
                new HbmEnergyStorage(kind.maxPower, kind.maxPower, 0L),
                frackingTank == null ? List.of(oilTank, gasTank) : List.of(oilTank, gasTank, frackingTank));
        this.kind = kind;
        this.oilTank = oilTank;
        this.gasTank = gasTank;
        this.frackingTank = frackingTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OilDrillBlockEntity drill) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, drill);
        boolean changed = drill.tickOilDrill(level, pos);
        if (changed) {
            drill.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getIndicator() {
        return indicator;
    }

    public Kind getKind() {
        return kind;
    }

    @Nullable
    public HbmFluidTank getFrackingTank() {
        return frackingTank;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public int getEnergyLevel() {
        return energyLevel;
    }

    public int getOverLevel() {
        return overLevel;
    }

    public int getPowerReqEff() {
        int req = kind.consumption;
        return Math.max(0, (req + (req / 4 * speedLevel) - (req / 4 * energyLevel)) * overLevel);
    }

    public int getDelayEff() {
        int delay = kind.delay;
        return Math.max((delay - (delay / 4 * speedLevel) + (delay / 10 * energyLevel)) / overLevel, 1);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return frackingTank == null ? List.of() : List.of(frackingTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(oilTank, gasTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        if (side == null) {
            return HbmFluidSideMode.BOTH;
        }
        return frackingTank == null ? HbmFluidSideMode.OUTPUT : HbmFluidSideMode.BOTH;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return frackingTank == null ? List.of() : List.of(frackingTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(oilTank, gasTank);
    }

    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return null;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type != HbmFluids.NONE && getSendingTanks().stream()
                .anyMatch(tank -> tank.getTankType() == type && tank.getFill() > 0);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return frackingTank != null && type == frackingTank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        if (kind == Kind.PUMPJACK) {
            Direction dir = facing();
            Direction rot = rotatedFacing();
            return List.of(
                    new FluidPort(offset(rot, 2).offset(offset(dir, 2)), dir),
                    new FluidPort(offset(rot, 2).offset(offset(dir.getOpposite(), 2)), dir.getOpposite()),
                    new FluidPort(offset(rot, 4).offset(offset(dir.getOpposite(), 2)), dir),
                    new FluidPort(offset(rot, 4).offset(offset(dir, 2)), dir.getOpposite()));
        }
        return List.of(
                FluidPort.of(1, 0, 0, Direction.EAST),
                FluidPort.of(-1, 0, 0, Direction.WEST),
                FluidPort.of(0, 0, 1, Direction.SOUTH),
                FluidPort.of(0, 0, -1, Direction.NORTH));
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
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
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        lines.add(LegacyLookOverlayLines.energyStored(getPower(), getMaxPower()));
        lines.add(LegacyLookOverlayLines.compactTank(false, oilTank));
        lines.add(LegacyLookOverlayLines.compactTank(false, gasTank));
        if (frackingTank != null) {
            lines.add(LegacyLookOverlayLines.compactTank(true, frackingTank));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, HbmInventoryMenuHelper.saveLegacyItems(items));
        tag.putInt(TAG_INDICATOR, indicator);
        tag.putInt(TAG_SPEED_LEVEL, speedLevel);
        tag.putInt(TAG_ENERGY_LEVEL, energyLevel);
        tag.putInt(TAG_OVER_LEVEL, overLevel);
        for (int i = 0; i < getAllTanks().size(); i++) {
            getAllTanks().get(i).writeToNbt(tag, "t" + i);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyItems(tag.getCompound(TAG_INVENTORY), items);
        indicator = tag.getInt(TAG_INDICATOR);
        speedLevel = tag.getInt(TAG_SPEED_LEVEL);
        energyLevel = tag.getInt(TAG_ENERGY_LEVEL);
        overLevel = Math.max(1, tag.contains(TAG_OVER_LEVEL) ? tag.getInt(TAG_OVER_LEVEL) : 1);
        for (int i = 0; i < getAllTanks().size(); i++) {
            getAllTanks().get(i).readFromNbt(tag, "t" + i);
        }
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
    public void writePersistentState(CompoundTag persistent) {
        boolean empty = getPower() == 0L && indicator == 0;
        for (HbmFluidTank tank : getAllTanks()) {
            if (tank.getFill() > 0) {
                empty = false;
            }
        }
        if (empty) {
            return;
        }
        persistent.putLong("power", getPower());
        persistent.putInt(TAG_INDICATOR, indicator);
        for (int i = 0; i < getAllTanks().size(); i++) {
            getAllTanks().get(i).writeToNbt(persistent, "t" + i);
        }
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        energy.setPower(persistent.getLong("power"));
        indicator = persistent.getInt(TAG_INDICATOR);
        for (int i = 0; i < getAllTanks().size(); i++) {
            getAllTanks().get(i).readFromNbt(persistent, "t" + i);
        }
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public ItemStack createPersistentBlockDrop(Item item) {
        ItemStack stack = new ItemStack(item);
        writePersistentStateToStack(stack);
        return stack;
    }

    public boolean hasStoredFluid() {
        return getAllTanks().stream().anyMatch(tank -> tank.getFill() > 0);
    }

    public void clearStoredFluids() {
        for (HbmFluidTank tank : getAllTanks()) {
            tank.setFill(0);
        }
        onFluidContentsChanged();
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new OilDrillMenu(containerId, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : externalItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        externalItemHandler.invalidate();
    }

    private boolean tickOilDrill(Level level, BlockPos pos) {
        boolean changed = updateUpgrades();
        long oldPower = energy.getPower();
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        changed |= oldPower != energy.getPower();
        changed |= HbmFluidItemTransfer.processTransfers(items, fluidTransfers());
        if (oilTank.getFill() > 0) {
            tryProvideFluidToPorts(oilTank.getTankType(), oilTank.getPressure(), this);
        }
        if (gasTank.getFill() > 0) {
            tryProvideFluidToPorts(gasTank.getTankType(), gasTank.getPressure(), this);
        }

        if (energy.getPower() >= getPowerReqEff()
                && oilTank.getFill() < oilTank.getMaxFill()
                && gasTank.getFill() < gasTank.getMaxFill()) {
            energy.setPower(energy.getPower() - getPowerReqEff());
            changed = true;
            if (level.getGameTime() % getDelayEff() == 0L) {
                indicator = INDICATOR_OK;
                changed |= advanceDrill(level, pos);
            }
        } else if (indicator != INDICATOR_BLOCKED) {
            indicator = INDICATOR_BLOCKED;
            changed = true;
        }
        return changed;
    }

    private List<HbmFluidItemTransfer.TankSlotTransfer> fluidTransfers() {
        List<HbmFluidItemTransfer.TankSlotTransfer> transfers = new ArrayList<>();
        transfers.add(HbmFluidItemTransfer.TankSlotTransfer.unload(
                SLOT_OIL_CONTAINER, SLOT_OIL_CONTAINER_OUTPUT, oilTank));
        transfers.add(HbmFluidItemTransfer.TankSlotTransfer.unload(
                SLOT_GAS_CONTAINER, SLOT_GAS_CONTAINER_OUTPUT, gasTank));
        return transfers;
    }

    private boolean updateUpgrades() {
        int oldSpeed = speedLevel;
        int oldEnergy = energyLevel;
        int oldOver = overLevel;
        LegacyMachineUpgradeManager.Levels levels =
                LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_START, SLOT_UPGRADE_END, VALID_UPGRADES);
        speedLevel = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        energyLevel = Math.min(levels.getLevel(UpgradeType.POWER), 3);
        overLevel = Math.min(levels.getLevel(UpgradeType.OVERDRIVE), 3) + 1;
        return oldSpeed != speedLevel || oldEnergy != energyLevel || oldOver != overLevel;
    }

    private boolean advanceDrill(Level level, BlockPos pos) {
        int depth = Math.max(level.getMinBuildHeight(), kind == Kind.FRACKING_TOWER ? 0 : 5);
        for (int y = pos.getY() - 1; y >= depth; y--) {
            BlockPos sample = new BlockPos(pos.getX(), y, pos.getZ());
            if (!level.getBlockState(sample).is(ModBlocks.OIL_PIPE.get())) {
                if (trySuck(level, sample)) {
                    return true;
                }
                tryDrill(level, sample);
                return true;
            }
            if (y == depth) {
                indicator = INDICATOR_COMPLETE;
                return true;
            }
        }
        indicator = INDICATOR_COMPLETE;
        return true;
    }

    private void tryDrill(Level level, BlockPos sample) {
        BlockState state = level.getBlockState(sample);
        if (!state.isAir() && state.getBlock().getExplosionResistance() < 1000.0F
                && state.getDestroySpeed(level, sample) >= 0.0F) {
            level.setBlock(sample, ModBlocks.OIL_PIPE.get().defaultBlockState(), Block.UPDATE_ALL);
        } else {
            indicator = INDICATOR_BLOCKED;
        }
    }

    private boolean trySuck(Level level, BlockPos sample) {
        if (!canSuckBlock(level.getBlockState(sample))) {
            return false;
        }
        if (!canPump()) {
            return true;
        }
        return suckRec(level, sample, 0, new HashSet<>());
    }

    private boolean suckRec(Level level, BlockPos pos, int layer, Set<BlockPos> trace) {
        if (!trace.add(pos) || layer > 64) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (isLegacyBlock(state, "ore_oil")
                || isLegacyBlock(state, "ore_bedrock_oil")) {
            doSuck(level, pos, state);
            return true;
        }
        if (isLegacyBlock(state, "ore_oil_empty")) {
            List<Direction> directions = new ArrayList<>(List.of(Direction.values()));
            shuffleDirections(directions, level);
            for (Direction direction : directions) {
                if (suckRec(level, pos.relative(direction), layer + 1, trace)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canSuckBlock(BlockState state) {
        return isLegacyBlock(state, "ore_oil")
                || isLegacyBlock(state, "ore_oil_empty")
                || kind == Kind.FRACKING_TOWER && isLegacyBlock(state, "ore_bedrock_oil");
    }

    private boolean canPump() {
        if (kind != Kind.FRACKING_TOWER || frackingTank == null) {
            return true;
        }
        if (frackingTank.getFill() >= Kind.FRACKING_TOWER.solutionRequired) {
            return true;
        }
        indicator = INDICATOR_NO_FRACKSOL;
        return false;
    }

    private void doSuck(Level level, BlockPos pos, BlockState state) {
        if (kind == Kind.FRACKING_TOWER) {
            frackingSuck(level, pos, state);
        } else if (isLegacyBlock(state, "ore_oil")) {
            oilTank.setFill(Math.min(oilTank.getFill() + kind.oilPerDeposit, oilTank.getMaxFill()));
            gasTank.setFill(Math.min(gasTank.getFill() + randomBetween(level, kind.gasMin, kind.gasMax),
                    gasTank.getMaxFill()));
            if (level.random.nextDouble() < kind.drainChance) {
                Block emptyOil = legacyBlock("ore_oil_empty");
                if (emptyOil != Blocks.AIR) {
                    level.setBlock(pos, emptyOil.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            playSuckEffects(level, pos);
        }
        onFluidContentsChanged();
    }

    private void frackingSuck(Level level, BlockPos pos, BlockState state) {
        int oil = 0;
        int gas = 0;
        if (isLegacyBlock(state, "ore_oil")) {
            oil = Kind.FRACKING_TOWER.oilPerDeposit;
            gas = randomBetween(level, Kind.FRACKING_TOWER.gasMin, Kind.FRACKING_TOWER.gasMax);
            if (level.random.nextDouble() < Kind.FRACKING_TOWER.drainChance) {
                Block emptyOil = legacyBlock("ore_oil_empty");
                if (emptyOil != Blocks.AIR) {
                    level.setBlock(pos, emptyOil.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        } else if (isLegacyBlock(state, "ore_bedrock_oil")) {
            oil = Kind.FRACKING_TOWER.bedrockOilPerDeposit;
            gas = randomBetween(level, Kind.FRACKING_TOWER.bedrockGasMin, Kind.FRACKING_TOWER.bedrockGasMax);
        }
        oilTank.setFill(Math.min(oilTank.getFill() + oil, oilTank.getMaxFill()));
        gasTank.setFill(Math.min(gasTank.getFill() + gas, gasTank.getMaxFill()));
        if (frackingTank != null) {
            frackingTank.setFill(Math.max(0, frackingTank.getFill() - Kind.FRACKING_TOWER.solutionRequired));
        }
        playSuckEffects(level, pos);
    }

    private void playSuckEffects(Level level, BlockPos pos) {
        if (kind == Kind.WELL) {
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GENERIC_SPLASH,
                    net.minecraft.sounds.SoundSource.BLOCKS, 2.0F, 0.5F);
        }
    }

    private static int randomBetween(Level level, int min, int max) {
        return min + level.random.nextInt(max - min + 1);
    }

    private static void shuffleDirections(List<Direction> directions, Level level) {
        for (int i = directions.size() - 1; i > 0; i--) {
            int j = level.random.nextInt(i + 1);
            Direction tmp = directions.get(i);
            directions.set(i, directions.get(j));
            directions.set(j, tmp);
        }
    }

    private static boolean isLegacyBlock(BlockState state, String legacyName) {
        return state.is(legacyBlock(legacyName));
    }

    private static Block legacyBlock(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block == null || !block.isPresent() ? Blocks.AIR : block.get();
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private Direction rotatedFacing() {
        return com.hbm.ntm.multiblock.LegacyMultiblockOffsets.legacyUpSide(facing());
    }

    private static BlockPos offset(Direction direction, int amount) {
        return new BlockPos(
                direction.getStepX() * amount,
                direction.getStepY() * amount,
                direction.getStepZ() * amount);
    }

    private static HbmFluidTank tank(FluidType type, int capacity) {
        HbmFluidTank tank = new HbmFluidTank(type, capacity);
        tank.setFill(0);
        return tank;
    }

    public enum Kind {
        WELL(100_000L, 100, 50, 500, 100, 500, 0.05D, 0, 0, 0, 0, 0),
        PUMPJACK(250_000L, 200, 25, 750, 50, 250, 0.025D, 0, 0, 0, 0, 0),
        FRACKING_TOWER(5_000_000L, 5_000, 20, 1_000, 100, 500, 0.02D, 10, 100, 10, 50, 75);

        private final long maxPower;
        private final int consumption;
        private final int delay;
        private final int oilPerDeposit;
        private final int gasMin;
        private final int gasMax;
        private final double drainChance;
        private final int solutionRequired;
        private final int bedrockOilPerDeposit;
        private final int bedrockGasMin;
        private final int bedrockGasMax;
        private final int destructionRange;

        Kind(long maxPower, int consumption, int delay, int oilPerDeposit, int gasMin, int gasMax,
                double drainChance, int solutionRequired, int bedrockOilPerDeposit, int bedrockGasMin,
                int bedrockGasMax, int destructionRange) {
            this.maxPower = maxPower;
            this.consumption = consumption;
            this.delay = delay;
            this.oilPerDeposit = oilPerDeposit;
            this.gasMin = gasMin;
            this.gasMax = gasMax;
            this.drainChance = drainChance;
            this.solutionRequired = solutionRequired;
            this.bedrockOilPerDeposit = bedrockOilPerDeposit;
            this.bedrockGasMin = bedrockGasMin;
            this.bedrockGasMax = bedrockGasMax;
            this.destructionRange = destructionRange;
        }

        private static Kind fromState(BlockState state) {
            if (state.is(ModBlocks.MACHINE_PUMPJACK.get())) {
                return PUMPJACK;
            }
            if (state.is(ModBlocks.MACHINE_FRACKING_TOWER.get())) {
                return FRACKING_TOWER;
            }
            return WELL;
        }
    }

    private static final class OilDrillExternalItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private OilDrillExternalItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return items.isItemValid(slot, stack);
        }
    }
}
