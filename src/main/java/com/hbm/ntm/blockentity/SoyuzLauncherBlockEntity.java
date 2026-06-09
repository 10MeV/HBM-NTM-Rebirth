package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.item.DesignatorItem;
import com.hbm.ntm.block.HorizontalMachineBlock;
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
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.satellite.SatelliteChipItem;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
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
                case SLOT_SATELLITE -> stack.getItem() instanceof SatelliteChipItem;
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
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private int mode;
    private boolean starting;
    private int countdown = MAX_COUNTDOWN;
    private Object audioLoop;
    private List<BlockPos> launcherPorts;
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
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SoyuzLauncherBlockEntity launcher) {
        if (!level.isClientSide) {
            return;
        }
        launcher.updateAudioLoop();
        if (!launcher.starting || launcher.countdown <= 0) {
            return;
        }
        if (level.getGameTime() % 8L == 0L
                && !level.getEntitiesOfClass(SoyuzEntity.class,
                        new AABB(pos.getX() - 5.0D, pos.getY(), pos.getZ() - 5.0D,
                                pos.getX() + 5.0D, pos.getY() + 10.0D, pos.getZ() + 5.0D)).isEmpty()) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                    pos.getX() + level.random.nextDouble() * 12.0D - 6.0D,
                    pos.getY() + level.random.nextDouble() * 4.0D,
                    pos.getZ() + level.random.nextDouble() * 12.0D - 6.0D,
                    0.0D, 0.05D, 0.0D);
        }
    }

    private void updateAudioLoop() {
        boolean active = starting && countdown > 0 && canLaunch();
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, ModSounds.BLOCK_SOYUZ_READY.getId(),
                active, 100.0D, 100.0F, 2.0F, 1.0F);
    }

    private boolean tickMachine(Level level, BlockPos pos, BlockState state) {
        long oldPower = energy.getPower();
        int oldFuel = keroseneTank().getFill();
        int oldOxygen = oxygenTank().getFill();
        int oldCountdown = countdown;
        boolean oldStarting = starting;

        HbmFluidItemTransfer.processTransfers(items, List.of(
                HbmFluidItemTransfer.TankSlotTransfer.load(SLOT_KEROSENE_INPUT, SLOT_KEROSENE_OUTPUT, keroseneTank()),
                HbmFluidItemTransfer.TankSlotTransfer.load(SLOT_OXYGEN_INPUT, SLOT_OXYGEN_OUTPUT, oxygenTank())));
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());

        if (!starting || !canLaunch()) {
            countdown = MAX_COUNTDOWN;
            starting = false;
        } else if (countdown > 0) {
            countdown--;
            if (countdown % 100 == 0) {
                level.playSound(null, pos, ModSounds.ALARM_HATCH.get(), SoundSource.RECORDS, 100.0F, 1.1F);
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
        level.playSound(null, worldPosition, ModSounds.ENTITY_SOYUZ_TAKEOFF.get(), SoundSource.PLAYERS, 100.0F, 1.1F);

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
        return items.getStackInSlot(SLOT_SATELLITE).isEmpty() ? 1 : 2;
    }

    public int orbitalStatus() {
        if (mode == MODE_CARGO) {
            return 0;
        }
        ItemStack satellite = items.getStackInSlot(SLOT_SATELLITE);
        if (satellite.is(ModItems.SAT_GERALD.get()) || satellite.is(ModItems.SAT_LUNAR_MINER.get())) {
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
        return HbmInventoryMenuHelper.clearToDrops(items);
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
            networkFluidPorts = launcherPorts().stream().map(pos -> new FluidPort(pos, Direction.UP)).toList();
        }
        return networkFluidPorts;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        if (energyPorts == null) {
            energyPorts = launcherPorts().stream().map(pos -> new EnergyPort(pos, Direction.UP)).toList();
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
        return Component.translatableWithFallback("container.hbm_ntm_rebirth.soyuz_launcher",
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

    private List<BlockPos> launcherPorts() {
        if (launcherPorts != null) {
            return launcherPorts;
        }
        Direction facing = facing();
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        List<BlockPos> ports = new ArrayList<>();
        for (int i = -6; i <= 6; i++) {
            ports.add(LegacyMultiblockOffsets.relative(facing, side, 7, i, 0));
            ports.add(LegacyMultiblockOffsets.relative(facing, side, 7, i, -1));
        }
        launcherPorts = List.copyOf(ports);
        return launcherPorts;
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.EAST;
    }

    private static boolean hasLegacyDesignatorCoords(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("xCoord") && tag.contains("zCoord");
    }
}
