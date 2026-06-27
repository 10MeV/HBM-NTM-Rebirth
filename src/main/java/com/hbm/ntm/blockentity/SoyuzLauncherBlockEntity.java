package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.item.DesignatorItem;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.missile.SoyuzEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.SoyuzLauncherMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoyuzLauncherBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmLegacyButtonReceiver {
    public static final int SLOT_ROCKET = 0;
    public static final int SLOT_DESIGNATOR = 1;
    public static final int SLOT_SATELLITE = 2;
    public static final int SLOT_ORBITAL = 3;
    public static final int SLOT_KEROSENE_INPUT = 4;
    public static final int SLOT_KEROSENE_OUTPUT = 5;
    public static final int SLOT_OXYGEN_INPUT = 6;
    public static final int SLOT_OXYGEN_OUTPUT = 7;
    public static final int SLOT_BATTERY = 8;
    public static final int SLOT_CARGO_START = 9;
    public static final int SLOT_CARGO_END = 27;
    public static final int SLOT_COUNT = 27;
    public static final int CONTROL_MODE = 0;
    public static final int CONTROL_START = 1;
    public static final int MODE_SATELLITE = 0;
    public static final int MODE_CARGO = 1;
    public static final int MAX_COUNTDOWN = 600;

    private static final long MAX_POWER = 1_000_000L;
    private static final int TANK_CAPACITY = 128_000;
    private static final int ROCKET_OPEN_TIMER = 20;
    private static final String TAG_MODE = "mode";
    private static final String TAG_STARTING = "starting";
    private static final String TAG_COUNTDOWN = "countdown";
    private static final String TAG_POWER = "power";
    private static final String TAG_CUSTOM_NAME = "name";

    private final HbmFluidTank keroseneTank = new HbmFluidTank(HbmFluids.KEROSENE, TANK_CAPACITY);
    private final HbmFluidTank oxygenTank = new HbmFluidTank(HbmFluids.OXYGEN, TANK_CAPACITY);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_ROCKET -> stack.is(ModItems.MISSILE_SOYUZ.get());
                case SLOT_DESIGNATOR -> stack.getItem() instanceof DesignatorItem || hasLegacyDesignatorCoords(stack);
                case SLOT_SATELLITE -> Satellite.getTypeFromStack(stack).isPresent();
                case SLOT_ORBITAL -> stack.is(ModItems.MISSILE_SOYUZ_LANDER.get());
                case SLOT_BATTERY -> stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
                default -> true;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private int mode;
    private boolean starting;
    private int countdown = MAX_COUNTDOWN;
    private String customName;
    private Object audioLoop;
    private List<LauncherPort> launcherPorts;
    private List<FluidPort> networkFluidPorts;
    private List<EnergyPort> energyPorts;

    public SoyuzLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOYUZ_LAUNCHER.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(new HbmFluidTank(HbmFluids.KEROSENE, TANK_CAPACITY),
                        new HbmFluidTank(HbmFluids.OXYGEN, TANK_CAPACITY)));
        getAllTanks().get(0).setTankType(HbmFluids.KEROSENE);
        getAllTanks().get(1).setTankType(HbmFluids.OXYGEN);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SoyuzLauncherBlockEntity launcher) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, launcher);
        }
        boolean changed = launcher.tickMachine(level, pos, state);
        if (changed) {
            launcher.setChanged();
        }
        launcher.networkPackNT(250);
        if (changed) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SoyuzLauncherBlockEntity launcher) {
        if (!level.isClientSide) {
            return;
        }
        launcher.updateAudioLoop();
        if (!level.getEntitiesOfClass(SoyuzEntity.class,
                        new AABB(pos.getX() - 5.0D, pos.getY(), pos.getZ() - 5.0D,
                                pos.getX() + 5.0D, pos.getY() + 10.0D, pos.getZ() + 5.0D)).isEmpty()) {
            ParticleUtil.spawnSmokeShockRandom(level, pos.getX() + 0.5D, pos.getY() - 3.0D,
                    pos.getZ() + 0.5D, 50, level.random.nextGaussian() * 3.0D + 6.0D);
        }
    }

    private void updateAudioLoop() {
        boolean active = starting && countdown > 0 && canLaunch();
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.soyuzReady",
                active, 100.0D, 100.0F, 2.0F, 1.0F);
    }

    private boolean tickMachine(Level level, BlockPos pos, BlockState state) {
        long oldPower = energy.getPower();
        int oldFuel = keroseneTank().getFill();
        int oldOxygen = oxygenTank().getFill();
        int oldCountdown = countdown;
        boolean oldStarting = starting;

        processFluidItemTransfers(items, HbmFluidItemTransfer.loadTransfers(
                SLOT_KEROSENE_INPUT, SLOT_KEROSENE_OUTPUT, 2, keroseneTank(), oxygenTank()));
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());

        if (!starting || !canLaunch()) {
            countdown = MAX_COUNTDOWN;
            starting = false;
        } else if (countdown > 0) {
            countdown--;
            if (countdown % 100 == 0 && countdown > 0) {
                LegacySoundPlayer.playSoundEffect(level, pos.getX(), pos.getY(), pos.getZ(),
                        "hbm:alarm.hatch", SoundSource.RECORDS, 100.0F, 1.1F);
            }
        } else if (level instanceof ServerLevel serverLevel) {
            liftOff(serverLevel);
        }

        return oldPower != energy.getPower()
                || oldFuel != keroseneTank().getFill()
                || oldOxygen != oxygenTank().getFill()
                || oldCountdown != countdown
                || oldStarting != starting;
    }

    public void startCountdown() {
        if (canLaunch()) {
            starting = true;
        }
    }

    private void liftOff(ServerLevel level) {
        starting = false;
        int fuelRequired = getFuelRequired();
        int powerRequired = getPowerRequired();

        SoyuzEntity soyuz = new SoyuzEntity(level);
        soyuz.setSkin(getRocketType());
        soyuz.setMode(mode);
        soyuz.setPos(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D);
        level.addFreshEntity(soyuz);
        LegacySoundPlayer.playSoundEffect(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                "hbm:entity.soyuzTakeoff", SoundSource.PLAYERS, 100.0F, 1.1F);

        keroseneTank().drain(fuelRequired, false);
        oxygenTank().drain(fuelRequired, false);
        energy.setPower(energy.getPower() - powerRequired);

        if (mode == MODE_SATELLITE) {
            soyuz.setSatellitePayload(items.getStackInSlot(SLOT_SATELLITE));
            if (orbitalStatus() == 2) {
                items.setStackInSlot(SLOT_ORBITAL, ItemStack.EMPTY);
            }
            items.setStackInSlot(SLOT_SATELLITE, ItemStack.EMPTY);
        } else if (mode == MODE_CARGO) {
            List<ItemStack> payload = new ArrayList<>();
            for (int slot = SLOT_CARGO_START; slot < SLOT_CARGO_END; slot++) {
                payload.add(items.getStackInSlot(slot).copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
            BlockPos target = designatorTarget();
            soyuz.setTarget(target.getX(), target.getZ());
            soyuz.setPayload(payload);
        }

        items.setStackInSlot(SLOT_ROCKET, ItemStack.EMPTY);
        countdown = MAX_COUNTDOWN;
    }

    public boolean canLaunch() {
        return hasRocket()
                && hasFuel()
                && hasRocket()
                && hasPower()
                && designatorStatus() != 1
                && orbitalStatus() != 1
                && satelliteStatus() != 1;
    }

    public boolean hasRocket() {
        return items.getStackInSlot(SLOT_ROCKET).is(ModItems.MISSILE_SOYUZ.get());
    }

    public boolean hasFuel() {
        return keroseneTank().getFill() >= getFuelRequired();
    }

    public boolean hasOxygen() {
        return oxygenTank().getFill() >= getFuelRequired();
    }

    public boolean hasPower() {
        return energy.getPower() >= getPowerRequired();
    }

    public int getFuelRequired() {
        return mode == MODE_CARGO ? Math.min(5_000 + getDistance(), TANK_CAPACITY) : TANK_CAPACITY;
    }

    public int getDistance() {
        if (designatorStatus() == 2) {
            BlockPos target = designatorTarget();
            return (int) new Vec3(worldPosition.getX() - target.getX(), 0.0D,
                    worldPosition.getZ() - target.getZ()).length();
        }
        return 0;
    }

    public int getPowerRequired() {
        return (int) (MAX_POWER * 0.75D);
    }

    public int designatorStatus() {
        if (mode == MODE_SATELLITE) {
            return 0;
        }
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        if (!stack.isEmpty() && isDesignatorReady(stack)) {
            return 2;
        }
        return 1;
    }

    public int satelliteStatus() {
        if (mode == MODE_CARGO) {
            return 0;
        }
        return Satellite.getTypeFromStack(items.getStackInSlot(SLOT_SATELLITE)).isPresent() ? 2 : 1;
    }

    public int orbitalStatus() {
        if (mode == MODE_CARGO) {
            return 0;
        }
        LegacySatelliteType type = Satellite.getTypeFromStack(items.getStackInSlot(SLOT_SATELLITE)).orElse(null);
        if (type == LegacySatelliteType.HORIZONS || type == LegacySatelliteType.LUNAR_MINER) {
            return items.getStackInSlot(SLOT_ORBITAL).is(ModItems.MISSILE_SOYUZ_LANDER.get()) ? 2 : 1;
        }
        return 0;
    }

    private boolean isDesignatorReady(ItemStack stack) {
        if (level == null) {
            return false;
        }
        if (stack.getItem() instanceof DesignatorItem designator) {
            return designator.isReady(level, stack, worldPosition);
        }
        return hasLegacyDesignatorCoords(stack);
    }

    private BlockPos designatorTarget() {
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        if (level != null && stack.getItem() instanceof DesignatorItem designator) {
            Vec3 coords = designator.getCoords(level, stack, worldPosition);
            return BlockPos.containing(coords);
        }
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return new BlockPos(tag.getInt("xCoord"), 0, tag.getInt("zCoord"));
        }
        return worldPosition;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank keroseneTank() {
        return getAllTanks().get(0);
    }

    public HbmFluidTank oxygenTank() {
        return getAllTanks().get(1);
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>(HbmInventoryMenuHelper.clearToDrops(items));
        appendDropStacks(drops, ModBlocks.STRUCT_LAUNCHER.get(), 414);
        appendDropStacks(drops, ModBlocks.legacyBlock("concrete_smooth").get(), 294);
        appendDropStacks(drops, ModBlocks.STRUCT_SCAFFOLD.get(), 447);
        appendDropStacks(drops, ModBlocks.STRUCT_SOYUZ_CORE.get(), 1);
        return drops;
    }

    public int getMode() {
        return mode;
    }

    public boolean isStarting() {
        return starting;
    }

    public int getCountdown() {
        return countdown;
    }

    public int getRocketType() {
        return hasRocket() ? SoyuzRocketItem.getSkin(items.getStackInSlot(SLOT_ROCKET)) : -1;
    }

    public long getStoredPower() {
        return energy.getPower();
    }

    public long getMaxStoredPower() {
        return energy.getMaxPower();
    }

    public int getPowerScaled(int height) {
        return energy.getMaxPower() <= 0L ? 0 : (int) (energy.getPower() * height / energy.getMaxPower());
    }

    public float getTowerRotation(float partialTick) {
        double open = 45.0D;
        double rotation = getRocketType() >= 0 ? 0.0D : open;
        if (starting && countdown < ROCKET_OPEN_TIMER) {
            rotation = (ROCKET_OPEN_TIMER - countdown + partialTick) * open / ROCKET_OPEN_TIMER;
        }
        return (float) rotation;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(keroseneTank(), oxygenTank());
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(keroseneTank(), oxygenTank());
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.KEROSENE || type == HbmFluids.OXYGEN;
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        if (networkFluidPorts == null) {
            networkFluidPorts = launcherPorts().stream()
                    .map(port -> new FluidPort(port.offset(), port.direction()))
                    .toList();
        }
        return networkFluidPorts;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        if (energyPorts == null) {
            energyPorts = launcherPorts().stream()
                    .map(port -> new EnergyPort(port.offset(), port.direction()))
                    .toList();
        }
        return energyPorts;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return type == HbmFluids.KEROSENE || type == HbmFluids.OXYGEN;
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
    public Component getDisplayName() {
        if (hasCustomName()) {
            return Component.literal(customName);
        }
        return Component.translatableWithFallback("container.soyuzLauncher",
                "Soyuz Launch Platform");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SoyuzLauncherMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D
                && ((id == CONTROL_MODE && (value == MODE_SATELLITE || value == MODE_CARGO))
                || (id == CONTROL_START && value == 0));
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_MODE) {
            mode = value;
            starting = false;
            countdown = MAX_COUNTDOWN;
            setChanged();
        } else if (id == CONTROL_START) {
            startCountdown();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        keroseneTank().writeToNbt(tag, "fuel");
        oxygenTank().writeToNbt(tag, "oxidizer");
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_MODE, mode);
        tag.putBoolean(TAG_STARTING, starting);
        tag.putInt(TAG_COUNTDOWN, countdown);
        if (hasCustomName()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        keroseneTank().readFromNbt(tag, "fuel");
        oxygenTank().readFromNbt(tag, "oxidizer");
        keroseneTank().setTankType(HbmFluids.KEROSENE);
        oxygenTank().setTankType(HbmFluids.OXYGEN);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        mode = tag.getInt(TAG_MODE);
        starting = tag.getBoolean(TAG_STARTING);
        countdown = tag.contains(TAG_COUNTDOWN) ? tag.getInt(TAG_COUNTDOWN) : MAX_COUNTDOWN;
        customName = tag.getString(TAG_CUSTOM_NAME);
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
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    private boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    private List<LauncherPort> launcherPorts() {
        if (launcherPorts != null) {
            return launcherPorts;
        }
        List<LauncherPort> ports = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction side = LegacyMultiblockOffsets.legacyUpSide(direction);
            for (int i = -6; i <= 6; i++) {
                ports.add(new LauncherPort(LegacyMultiblockOffsets.relative(direction, side, 7, i, 0),
                        direction));
                ports.add(new LauncherPort(LegacyMultiblockOffsets.relative(direction, side, 7, i, -1),
                        direction));
            }
        }
        launcherPorts = List.copyOf(ports);
        return launcherPorts;
    }

    private static void appendDropStacks(List<ItemStack> drops, Block block, int count) {
        int remaining = count;
        int maxStackSize = new ItemStack(block).getMaxStackSize();
        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            drops.add(new ItemStack(block, stackSize));
            remaining -= stackSize;
        }
    }

    private static boolean hasLegacyDesignatorCoords(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("xCoord") && tag.contains("zCoord");
    }

    private record LauncherPort(BlockPos offset, Direction direction) {
    }
}
