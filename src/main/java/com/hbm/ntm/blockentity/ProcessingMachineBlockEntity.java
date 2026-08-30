package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyPortInspectable;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeRecipeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortSubscriptionTracker;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.LegacyFluidTankPacket;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ProcessingMachineMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipeRuntime;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.LegacyUpgradeSlotSound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ProcessingMachineBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmEnergyPortInspectable, HbmStandardFluidReceiver, HbmLegacyLoadedTile {
    private static final String TAG_INVENTORY = "items";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_TANK = "tank";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_IS_ON = "isOn";
    private static final String TAG_ANGLE = "angle";
    private static final int CENTRIFUGE_MAX_POWER = 100_000;
    private static final int CENTRIFUGE_PROCESS_TIME = 200;
    private static final int CENTRIFUGE_BASE_CONSUMPTION = 200;
    private static final int CRYSTALLIZER_MAX_POWER = 1_000_000;
    private static final int CRYSTALLIZER_DEMAND = 1_000;
    private static final int CRYSTALLIZER_TANK_CAPACITY = 8_000;

    private static final Map<UpgradeType, Integer> CENTRIFUGE_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);
    private static final Map<UpgradeType, Integer> CRYSTALLIZER_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.EFFECT, 3,
            UpgradeType.OVERDRIVE, 3);

    private final Kind kind;
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final LegacyMachineUpgradeManager.SlotCache centrifugeUpgradeSlotCache =
            new LegacyMachineUpgradeManager.SlotCache(2);
    private final LegacyMachineUpgradeManager.SlotCache crystallizerUpgradeSlotCache =
            new LegacyMachineUpgradeManager.SlotCache(2);
    private final ItemStackHandler items = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot >= 5 && slot <= 7) {
                centrifugeUpgradeSlotCache.invalidate();
                crystallizerUpgradeSlotCache.invalidate();
            }
            if (kind == Kind.CRYSTALLIZER) {
                LegacyUpgradeSlotSound.playIfUpgrade(ProcessingMachineBlockEntity.this, slot, getStackInSlot(slot),
                        5, 6, 0.5D, 1.0F, 1.0F);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (kind) {
                case CENTRIFUGE -> switch (slot) {
                    case 0 -> true;
                    case 1 -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                    case 6, 7 -> stack.getItem() instanceof ItemMachineUpgrade;
                    default -> false;
                };
                case CRYSTALLIZER -> switch (slot) {
                    case 0 -> true;
                    case 1 -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                    case 3, 4, 7 -> true;
                    case 5, 6 -> stack.getItem() instanceof ItemMachineUpgrade;
                    default -> false;
                };
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final HbmEnergyStorage energy = new HbmEnergyStorage(CRYSTALLIZER_MAX_POWER, CRYSTALLIZER_MAX_POWER, 0L);
    private final HbmFluidTank crystallizerTank = new HbmFluidTank(HbmFluids.PEROXIDE, CRYSTALLIZER_TANK_CAPACITY);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private final LazyOptional<IEnergyStorage> energyHandler =
            LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() ->
            ForgeRecipeFluidHandlerAdapter.create(List.of(crystallizerTank), List.of(), 0,
                    this::onFluidContentsChanged));
    private final HbmFluidPortSubscriptionTracker fluidPortSubscriptions = new HbmFluidPortSubscriptionTracker();

    private int progress;
    private int duration = CENTRIFUGE_PROCESS_TIME;
    private long consumption = CENTRIFUGE_BASE_CONSUMPTION;
    private boolean isOn;
    private int audioDuration;
    private Object audioLoop;
    private float angle;
    private float prevAngle;
    @Nullable
    private String customName;

    public ProcessingMachineBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, inferKind(state));
    }

    public ProcessingMachineBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        super(ModBlockEntities.PROCESSING_MACHINE.get(), pos, state);
        this.kind = kind;
        int maxPower = maxPowerForKind();
        energy.setMaxPower(maxPower);
        energy.setTransferRates(maxPower, 0L);
        if (kind == Kind.CRYSTALLIZER) {
            this.duration = 600;
            this.consumption = CRYSTALLIZER_DEMAND;
        }
    }

    private int maxPowerForKind() {
        return kind == Kind.CENTRIFUGE ? CENTRIFUGE_MAX_POWER : CRYSTALLIZER_MAX_POWER;
    }

    private static Kind inferKind(BlockState state) {
        if (state.is(ModBlocks.MACHINE_CRYSTALLIZER.get())) {
            return Kind.CRYSTALLIZER;
        }
        return Kind.CENTRIFUGE;
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity machine) {
        long oldPower = machine.energy.getPower();
        int oldProgress = machine.progress;
        int oldDuration = machine.duration;
        long oldConsumption = machine.consumption;
        boolean oldOn = machine.isOn;
        FluidType oldType = machine.crystallizerTank.getTankType();
        int oldFill = machine.crystallizerTank.getFill();

        HbmEnergyUtil.chargeStorageFromItem(machine.items.getStackInSlot(machine.batterySlot()),
                machine.energy, machine.energy.getReceiverSpeed());
        if (machine.kind == Kind.CRYSTALLIZER) {
            machine.markCrystallizerLadderPlayers(level, pos, state);
            HbmFluidItemTransfer.setTankTypeFromIdentifierSlot(machine.items, 7, machine.crystallizerTank, level, pos);
            HbmFluidItemTransfer.loadTankFromSlot(machine.items, 3, 4, machine.crystallizerTank);
            // TileEntityMachineCrystallizer#updateConnections refreshes its typed
            // Fluid Mk2 receiver at all eight remote ports every server tick.
            machine.fluidPortSubscriptions.refreshReceiver(level, pos, machine.crystallizerFluidPorts(),
                    List.of(machine.crystallizerTank), machine);
        }
        if (machine.kind == Kind.CENTRIFUGE || level.getGameTime() % 20L == 0L) {
            if (machine.kind == Kind.CRYSTALLIZER) {
                HbmEnergyUtil.subscribeReceiverToPorts(level, pos, machine.crystallizerEnergyPorts(), machine);
            } else {
                // TileEntityMachineCentrifuge#updateEntity retries all six
                // adjacent Energy Mk2 receiver subscriptions every tick.
                HbmEnergyUtil.subscribeReceiverToAllNeighborNetworks(level, pos, machine);
            }
        }

        boolean changed = machine.kind == Kind.CENTRIFUGE
                ? machine.tickCentrifuge(level)
                : machine.tickCrystallizer(level);
        machine.markCrystallizerFauxLadderPlayers(level, pos, state);

        changed |= oldPower != machine.energy.getPower()
                || oldProgress != machine.progress
                || oldDuration != machine.duration
                || oldConsumption != machine.consumption
                || oldOn != machine.isOn
                || oldType != machine.crystallizerTank.getTankType()
                || oldFill != machine.crystallizerTank.getFill();
        if (changed) {
            machine.setChanged();
        }
        machine.networkPackNT(25);
        if (changed) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity machine) {
        if (machine.kind == Kind.CENTRIFUGE) {
            if (machine.isOn) {
                machine.audioDuration += 2;
            } else {
                machine.audioDuration -= 3;
            }
            machine.audioDuration = Math.max(0, Math.min(60, machine.audioDuration));
            float pitch = (machine.audioDuration - 10) / 100.0F + 0.5F;
            machine.audioLoop = LegacyMachineAudioBridge.updateLoop(machine.audioLoop, machine,
                    "hbm:block.centrifugeOperate", machine.audioDuration > 10, 25.0D, 20.0F, 1.0F, pitch);
            return;
        }

        boolean skipAnimation = LegacyClientAnimationLod.shouldSkipAnimationUpdate(level, pos);
        machine.prevAngle = machine.angle;
        if (machine.isOn && !skipAnimation) {
            machine.angle += 5.0F * machine.getCycleCount();
            if (machine.angle >= 360.0F) {
                machine.angle -= 360.0F;
                machine.prevAngle -= 360.0F;
            }
            if (level.random.nextInt(20) == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                        pos.getX() + level.random.nextDouble(), pos.getY() + 6.5D,
                        pos.getZ() + level.random.nextDouble(), 0.0D, 0.1D, 0.0D);
            }
        }
        machine.audioLoop = LegacyMachineAudioBridge.updateLoop(machine.audioLoop, machine,
                "hbm:block.chemplantOperate", machine.isOn, 25.0D, 15.0F, 1.0F, 0.75F);
    }

    private boolean tickCentrifuge(Level level) {
        UpgradeFactors factors = centrifugeUpgradeFactors();
        consumption = factors.consumption();
        duration = CENTRIFUGE_PROCESS_TIME;

        boolean wasOn = isOn;
        isOn = false;
        if (energy.getPower() > 0 && progress > 0) {
            energy.setPower(Math.max(0L, energy.getPower() - consumption));
        }

        ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level,
                ItemProcessingRecipe.Machine.CENTRIFUGE, items.getStackInSlot(0));
        if (recipe == null || energy.getPower() <= 0L || !canOutput(recipe.outputStacks(), 2, 5)) {
            progress = 0;
            return wasOn;
        }

        isOn = true;
        progress += factors.speed();
        if (progress >= CENTRIFUGE_PROCESS_TIME) {
            progress = 0;
            List<ItemStack> outputs = recipe.rollOutputStacks(level.random);
            mergeOutputs(outputs, 2, 5);
            items.extractItem(0, recipe.input().count(), false);
            return true;
        }
        return true;
    }

    private boolean tickCrystallizer(Level level) {
        LegacyMachineUpgradeManager.Levels levels = crystallizerUpgradeLevels();
        UpgradeFactors factors = crystallizerUpgradeFactors(levels);
        consumption = factors.consumption();
        boolean changed = false;
        boolean wasOn = isOn;
        isOn = false;

        int cycles = getCycleCount(levels);
        for (int i = 0; i < cycles; i++) {
            ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level,
                    ItemProcessingRecipe.Machine.CRYSTALLIZER, items.getStackInSlot(0),
                    crystallizerTank.getTankType());
            duration = recipe == null ? 600 : crystallizerDuration(recipe, levels);
            if (recipe == null || !canCrystallize(recipe)) {
                progress = 0;
                continue;
            }

            progress++;
            energy.setPower(energy.getPower() - consumption);
            isOn = true;
            changed = true;
            if (progress > duration) {
                progress = 0;
                processCrystallizerRecipe(recipe, levels, level);
            }
        }
        return changed || wasOn != isOn;
    }

    /**
     * Carries TileEntityMachineCrystallizer's source-backed side ladder into
     * the modern faux-ladder player-property path. ForgeDirection's old
     * UP-axis rotation maps to Direction#getClockWise for horizontal facings.
     */
    private void markCrystallizerLadderPlayers(Level level, BlockPos pos, BlockState state) {
        Direction side = state.getValue(HorizontalMachineBlock.FACING).getClockWise();
        AABB ladder = new AABB(pos.getX() + 0.25D, pos.getY() + 1.0D, pos.getZ() + 0.25D,
                pos.getX() + 0.75D, pos.getY() + 6.0D, pos.getZ() + 0.75D)
                .move(side.getStepX() * 1.5D, 0.0D, side.getStepZ() * 1.5D);
        for (Player player : level.getEntitiesOfClass(Player.class, ladder)) {
            HbmPlayerProperties.markOnLadder(player);
        }
    }

    /**
     * {@code TileEntityMachineCrystallizer#updateEntity} marks players in its
     * narrow side column as being on a faux ladder each tick.  This is not the
     * controller block's local collision volume: the column is offset by the
     * legacy facing rotated about UP.
     */
    private void markCrystallizerFauxLadderPlayers(Level level, BlockPos pos, BlockState state) {
        if (kind != Kind.CRYSTALLIZER) {
            return;
        }
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING) : Direction.NORTH;
        Direction side = facing.getClockWise(); // ForgeDirection#getRotation(UP)
        double centerX = pos.getX() + 0.5D + side.getStepX() * 1.5D;
        double centerZ = pos.getZ() + 0.5D + side.getStepZ() * 1.5D;
        AABB ladderColumn = new AABB(centerX - 0.25D, pos.getY() + 1.0D, centerZ - 0.25D,
                centerX + 0.25D, pos.getY() + 6.0D, centerZ + 0.25D);
        for (Player player : level.getEntitiesOfClass(Player.class, ladderColumn)) {
            HbmPlayerProperties.markOnLadder(player);
        }
    }

    private boolean canCrystallize(ItemProcessingRecipe recipe) {
        if (energy.getPower() < consumption || !recipe.input().test(items.getStackInSlot(0))) {
            return false;
        }
        if (recipe.fluidInput().isEmpty()) {
            return false;
        }
        if (crystallizerTank.getFill() < recipe.fluidInput().get().amount()) {
            return false;
        }
        return canOutput(recipe.outputStacks(), 2, 2);
    }

    private void processCrystallizerRecipe(ItemProcessingRecipe recipe, LegacyMachineUpgradeManager.Levels levels,
            Level level) {
        mergeOutputs(recipe.rollOutputStacks(level.random), 2, 2);
        recipe.fluidInput().ifPresent(fluid -> crystallizerTank.drain(fluid.amount(), false));
        int effect = Math.min(levels.getLevel(UpgradeType.EFFECT), 3);
        float freeChance = effect <= 0 ? 0.0F : Math.min(effect * recipe.productivity(), 0.99F);
        if (freeChance == 0.0F || freeChance < level.random.nextFloat()) {
            items.extractItem(0, recipe.input().count(), false);
        }
    }

    private UpgradeFactors centrifugeUpgradeFactors() {
        LegacyMachineUpgradeManager.Levels levels = centrifugeUpgradeLevels();
        int speed = 1 + Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        long power = CENTRIFUGE_BASE_CONSUMPTION
                + (long) Math.min(levels.getLevel(UpgradeType.SPEED), 3) * CENTRIFUGE_BASE_CONSUMPTION;
        int overdrive = Math.min(levels.getLevel(UpgradeType.OVERDRIVE), 3);
        speed *= 1 + overdrive * 5;
        power += (long) overdrive * CENTRIFUGE_BASE_CONSUMPTION * 50L;
        power /= 1 + Math.min(levels.getLevel(UpgradeType.POWER), 3);
        return new UpgradeFactors(speed, power);
    }

    private UpgradeFactors crystallizerUpgradeFactors() {
        return crystallizerUpgradeFactors(crystallizerUpgradeLevels());
    }

    private UpgradeFactors crystallizerUpgradeFactors(LegacyMachineUpgradeManager.Levels levels) {
        int speed = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        int effect = Math.min(levels.getLevel(UpgradeType.EFFECT), 3);
        return new UpgradeFactors(1, CRYSTALLIZER_DEMAND + (long) speed * CRYSTALLIZER_DEMAND
                + (long) effect * CRYSTALLIZER_DEMAND * 2L);
    }

    private int crystallizerDuration(ItemProcessingRecipe recipe, LegacyMachineUpgradeManager.Levels levels) {
        int base = recipe.duration() > 0 ? recipe.duration() : 600;
        int speed = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        if (speed <= 0) {
            return base;
        }
        return (int) Math.ceil(base * Math.max(1.0F - 0.25F * speed, 0.25F));
    }

    private int getCycleCount() {
        if (kind != Kind.CRYSTALLIZER) {
            return 1;
        }
        return getCycleCount(crystallizerUpgradeLevels());
    }

    private int getCycleCount(LegacyMachineUpgradeManager.Levels levels) {
        return Math.min(1 + Math.min(levels.getLevel(UpgradeType.OVERDRIVE), 3) * 2, 7);
    }

    private LegacyMachineUpgradeManager.Levels centrifugeUpgradeLevels() {
        return centrifugeUpgradeSlotCache.get(items, 6, 7, CENTRIFUGE_UPGRADES);
    }

    private LegacyMachineUpgradeManager.Levels crystallizerUpgradeLevels() {
        return crystallizerUpgradeSlotCache.get(items, 5, 6, CRYSTALLIZER_UPGRADES);
    }

    private boolean canOutput(List<ItemStack> outputs, int firstSlot, int lastSlot) {
        for (int i = 0; i < outputs.size() && firstSlot + i <= lastSlot; i++) {
            ItemStack output = outputs.get(i);
            if (output.isEmpty()) {
                continue;
            }
            ItemStack existing = items.getStackInSlot(firstSlot + i);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, output)) {
                return false;
            }
            if (!existing.isEmpty() && existing.getCount() + output.getCount() > existing.getMaxStackSize()) {
                return false;
            }
        }
        return outputs.size() <= lastSlot - firstSlot + 1;
    }

    private void mergeOutputs(List<ItemStack> outputs, int firstSlot, int lastSlot) {
        for (int i = 0; i < outputs.size() && firstSlot + i <= lastSlot; i++) {
            ItemStack output = outputs.get(i);
            if (output.isEmpty()) {
                continue;
            }
            int slot = firstSlot + i;
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()) {
                items.setStackInSlot(slot, output.copy());
            } else if (ItemStack.isSameItemSameTags(existing, output)) {
                existing.grow(output.getCount());
                items.setStackInSlot(slot, existing);
            }
        }
    }

    private int batterySlot() {
        return kind == Kind.CENTRIFUGE ? 1 : 1;
    }

    private List<FluidPort> crystallizerFluidPorts() {
        return LegacyMultiblockPorts.crystallizerFluidPorts();
    }

    private List<HbmEnergyUtil.EnergyPort> crystallizerEnergyPorts() {
        return List.of(
                HbmEnergyUtil.EnergyPort.of(2, 0, 1, Direction.EAST),
                HbmEnergyUtil.EnergyPort.of(2, 0, -1, Direction.EAST),
                HbmEnergyUtil.EnergyPort.of(-2, 0, 1, Direction.WEST),
                HbmEnergyUtil.EnergyPort.of(-2, 0, -1, Direction.WEST),
                HbmEnergyUtil.EnergyPort.of(1, 0, 2, Direction.SOUTH),
                HbmEnergyUtil.EnergyPort.of(-1, 0, 2, Direction.SOUTH),
                HbmEnergyUtil.EnergyPort.of(1, 0, -2, Direction.NORTH),
                HbmEnergyUtil.EnergyPort.of(-1, 0, -2, Direction.NORTH));
    }

    public Kind kind() {
        return kind;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmEnergyStorage getEnergyStorage() {
        return energy;
    }

    public HbmFluidTank getCrystallizerTank() {
        return crystallizerTank;
    }

    public int getProgress() {
        return progress;
    }

    public int getDuration() {
        return Math.max(1, duration);
    }

    public long getConsumption() {
        return consumption;
    }

    public boolean isOn() {
        return isOn;
    }

    public float getAngle(float partialTick) {
        return prevAngle + (angle - prevAngle) * partialTick;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.putString("kind", kind.name());
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_DURATION, duration);
        tag.putLong("consumption", consumption);
        tag.putBoolean(TAG_IS_ON, isOn);
        tag.putFloat(TAG_ANGLE, angle);
        crystallizerTank.writeToNbt(tag, TAG_TANK);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        } else if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        progress = tag.getInt(TAG_PROGRESS);
        duration = Math.max(1, tag.getInt(TAG_DURATION));
        consumption = tag.contains("consumption") ? tag.getLong("consumption") : consumption;
        isOn = tag.getBoolean(TAG_IS_ON);
        angle = tag.getFloat(TAG_ANGLE);
        prevAngle = angle;
        if (tag.contains(TAG_TANK)) {
            crystallizerTank.readFromNbt(tag, TAG_TANK);
        }
        centrifugeUpgradeSlotCache.invalidate();
        crystallizerUpgradeSlotCache.invalidate();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
}

    @Override
    public CompoundTag getClientSyncTag() {
        // The legacy centrifuge and crystallizer have distinct runtime
        // packets.  Mirror exactly those fields for the initial chunk/update
        // packet instead of loading an empty persistence tag on the client.
        CompoundTag tag = new CompoundTag();
        writeLegacyLoadedTileClientTag(tag);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putBoolean(TAG_IS_ON, isOn);
        if (kind == Kind.CRYSTALLIZER) {
            tag.putInt(TAG_DURATION, duration);
            crystallizerTank.writeToNbt(tag, TAG_TANK);
        }
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        if (!tag.contains(TAG_LEGACY_POWER)) {
            return;
        }
        readLegacyLoadedTileClientTag(tag);
        energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        progress = tag.getInt(TAG_PROGRESS);
        isOn = tag.getBoolean(TAG_IS_ON);
        if (kind == Kind.CRYSTALLIZER) {
            duration = Math.max(1, tag.getInt(TAG_DURATION));
            if (tag.contains(TAG_TANK, Tag.TAG_COMPOUND)) {
                crystallizerTank.readFromNbt(tag, TAG_TANK);
            }
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        if (kind == Kind.CENTRIFUGE) {
            // TileEntityMachineCentrifuge#serialize.
            data.writeLong(energy.getPower());
            data.writeInt(progress);
            data.writeBoolean(isOn);
        } else {
            // TileEntityMachineCrystallizer#serialize.
            data.writeShort((short) progress);
            data.writeShort((short) duration);
            data.writeLong(energy.getPower());
            data.writeBoolean(isOn);
            LegacyFluidTankPacket.write(data, crystallizerTank);
        }
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        if (kind == Kind.CENTRIFUGE) {
            energy.setPower(data.readLong());
            progress = data.readInt();
            isOn = data.readBoolean();
        } else {
            progress = data.readShort();
            duration = Math.max(1, data.readShort());
            energy.setPower(data.readLong());
            isOn = data.readBoolean();
            LegacyFluidTankPacket.read(data, crystallizerTank);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return kind == Kind.CRYSTALLIZER
                ? new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 10, 2))
                : new AABB(worldPosition, worldPosition.offset(1, 4, 1));
    }

    @Override
    public Component getDisplayName() {
        if (customName != null && !customName.isBlank()) {
            return Component.literal(customName);
        }
        return kind == Kind.CRYSTALLIZER
                ? Component.translatableWithFallback("container.crystallizer", "Ore Acidizer")
                : Component.translatableWithFallback("container.centrifuge", "Centrifuge");
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_INVENTORY)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ProcessingMachineMenu(containerId, inventory, this);
    }

    @Override
    public void setRemoved() {
        if (kind == Kind.CRYSTALLIZER) {
            HbmEnergyUtil.unsubscribeReceiverFromPorts(level, worldPosition, crystallizerEnergyPorts(), this);
            fluidPortSubscriptions.detachAllTrackedDetailed(level, worldPosition, this, null);
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (kind == Kind.CRYSTALLIZER) {
            HbmEnergyUtil.unsubscribeReceiverFromPorts(level, worldPosition, crystallizerEnergyPorts(), this);
            fluidPortSubscriptions.detachAllTrackedDetailed(level, worldPosition, this, null);
        }
        super.onChunkUnloaded();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
        fluidHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER && kind == Kind.CRYSTALLIZER) {
            return fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public long getPower() {
        return energy.getPower();
    }

    @Override
    public void setPower(long power) {
        energy.setPower(power);
    }

    @Override
    public long getMaxPower() {
        return energy.getMaxPower();
    }

    @Override
    public long getReceiverSpeed() {
        return energy.getReceiverSpeed();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return kind == Kind.CRYSTALLIZER ? List.of(crystallizerTank) : List.of();
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return getReceivingTanks();
    }

    @Override
    public HbmEnergyUtil.PortSetSnapshot inspectEnergyPorts() {
        if (level == null || kind != Kind.CRYSTALLIZER) {
            return new HbmEnergyUtil.PortSetSnapshot(0, 0, 0, 0, 0, 0, 0L, 0L);
        }
        return HbmEnergyUtil.inspectPorts(level, worldPosition, crystallizerEnergyPorts());
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return kind == Kind.CENTRIFUGE ? 5 : 2;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            if (mapped != 0) {
                return stack;
            }
            return items.insertItem(mapped, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            boolean canExtract = kind == Kind.CENTRIFUGE ? mapped >= 2 && mapped <= 5 : mapped == 2;
            return canExtract ? items.extractItem(mapped, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapExternalSlot(slot);
            return mapped == 0 && items.isItemValid(mapped, stack);
        }

        private int mapExternalSlot(int slot) {
            if (kind == Kind.CENTRIFUGE) {
                return switch (slot) {
                    case 0 -> 0;
                    case 1 -> 2;
                    case 2 -> 3;
                    case 3 -> 4;
                    case 4 -> 5;
                    default -> -1;
                };
            }
            return switch (slot) {
                case 0 -> 0;
                case 1 -> 2;
                default -> -1;
            };
        }
    }

    private record UpgradeFactors(int speed, long consumption) {
    }

    public enum Kind {
        CENTRIFUGE,
        CRYSTALLIZER
    }
}
