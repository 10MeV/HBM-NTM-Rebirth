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
import com.hbm.ntm.fluid.LegacyFluidTankPacket;
import com.hbm.ntm.menu.SoyuzLauncherMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.satellite.SatelliteItem;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
    private static final String TAG_ROCKET_TYPE = "rocketType";
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
            // TileEntitySoyuzLauncher inherits TileEntityMachineBase's all-false
            // ISidedInventory insertion contract.  Its GUI separately used ordinary
            // Slots, so manual placement must be expressed by SoyuzLauncherMenu rather
            // than by opening an automation insertion path here.
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private int mode;
    private boolean starting;
    private int countdown;
    private int syncedRocketType = -1;
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
        // TileEntitySoyuzLauncher independently advances its client countdown.
        // It only stops the loop on the !starting / !canLaunch branch; countdown
        // reaching zero waits for the server's lift-off state sync.
        if (!launcher.starting || !launcher.canLaunch()) {
            launcher.countdown = MAX_COUNTDOWN;
            launcher.updateAudioLoop(false);
        } else if (launcher.countdown > 0) {
            launcher.updateAudioLoop(true);
            launcher.countdown--;
        }
        if (LegacyClientAnimationLod.shouldSkipAnimationUpdate(level, pos)) {
            return;
        }
        if (!level.getEntitiesOfClass(SoyuzEntity.class,
                        new AABB(pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D)).isEmpty()) {
            ParticleUtil.spawnSmokeShockRandom(level, pos.getX() + 0.5D, pos.getY() - 3.0D,
                    pos.getZ() + 0.5D, 50, level.random.nextGaussian() * 3.0D + 6.0D);
        }
    }

    private void updateAudioLoop(boolean active) {
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.soyuzReady",
                active, 100.0D, 100.0F, 2.0F, 1.0F);
    }

    private boolean tickMachine(Level level, BlockPos pos, BlockState state) {
        long oldPower = energy.getPower();
        int oldFuel = keroseneTank().getFill();
        int oldOxygen = oxygenTank().getFill();
        int oldCountdown = countdown;
        boolean oldStarting = starting;

        processFluidItemLoadTransfers(items, SLOT_KEROSENE_INPUT, SLOT_KEROSENE_OUTPUT, 2,
                keroseneTank(), oxygenTank());
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
                // TileEntitySoyuzLauncher hands its slot reference straight to
                // EntitySoyuz before clearing the launcher slot.  The entity and
                // later recovered capsule therefore retain this exact stack.
                payload.add(items.getStackInSlot(slot));
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
        if (requiresOrbitalModule(items.getStackInSlot(SLOT_SATELLITE))) {
            return items.getStackInSlot(SLOT_ORBITAL).is(ModItems.MISSILE_SOYUZ_LANDER.get()) ? 2 : 1;
        }
        return 0;
    }

    private static boolean requiresOrbitalModule(ItemStack stack) {
        return stack.getItem() instanceof SatelliteItem
                && (SatelliteItem.variantOf(stack) == SatelliteItem.Variant.MINER_LUNAR
                || SatelliteItem.variantOf(stack).satelliteType() == LegacySatelliteType.HORIZONS);
    }

    private boolean isDesignatorReady(ItemStack stack) {
        if (level == null) {
            return false;
        }
        if (stack.getItem() instanceof DesignatorItem designator) {
            return designator.isReady(level, stack, worldPosition);
        }
        return false;
    }

    private BlockPos designatorTarget() {
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        if (level != null && stack.getItem() instanceof DesignatorItem designator) {
            return designator.getHorizontalTarget(level, stack, worldPosition);
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
        if (level != null && level.isClientSide) {
            return syncedRocketType;
        }
        return hasRocket() ? SoyuzRocketItem.getRawSkin(items.getStackInSlot(SLOT_ROCKET)) : -1;
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
        // TileEntitySoyuzLauncher#canConnect rejected UNKNOWN, UP and DOWN.
        // Keep the unsided view for internal/diagnostic callers, but do not
        // expose a physical Forge-fluid endpoint on the vertical core faces.
        return side == null || side.getAxis().isHorizontal()
                ? HbmFluidSideMode.INPUT
                : HbmFluidSideMode.NONE;
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

    /**
     * Soyuz performs its legacy remote endpoint scan every 20 ticks in its
     * outer ticker; keep every such pass retryable after duct topology changes.
     */
    @Override
    protected boolean shouldRefreshFluidNetworkSubscriptionsEveryTick() {
        return true;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
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
        return side != null && side.getAxis().isHorizontal()
                && (type == HbmFluids.KEROSENE || type == HbmFluids.OXYGEN);
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
                // AuxButtonPacket passed both integers through verbatim.  The
                // Soyuz branch only distinguished the two button ids: id 0
                // assigned `(byte) value`, while id 1 called startCountdown()
                // regardless of its value.  Keep the modern proximity gate,
                // but do not add an un-sourced value whitelist to this legacy
                // machine control contract.
                && (id == CONTROL_MODE || id == CONTROL_START);
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_MODE) {
            // AuxButtonPacket#Handler assigned launcher.mode = (byte) value
            // directly.  In particular, changing modes did not immediately
            // cancel a running countdown or reset it; the normal following
            // machine tick decides whether the retained start state is valid.
            mode = (byte) value;
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
        tag.putByte(TAG_MODE, (byte) mode);
        // TileEntitySoyuzLauncher only persisted tanks, power, mode and items.
        // Countdown/start are runtime state and are supplied separately in the
        // modern client update tag below, not in world-save NBT.
        if (customName != null) {
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
        mode = tag.getByte(TAG_MODE);
        // World saves deliberately omit these two old non-persistent fields.
        // Update tags include them solely to keep the existing modern client
        // animation/audio bridge in sync with the server.
        starting = tag.contains(TAG_STARTING) && tag.getBoolean(TAG_STARTING);
        countdown = tag.contains(TAG_COUNTDOWN) ? tag.getInt(TAG_COUNTDOWN) : 0;
        customName = tag.getString(TAG_CUSTOM_NAME);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putByte(TAG_MODE, (byte) mode);
        tag.putBoolean(TAG_STARTING, starting);
        tag.putInt(TAG_COUNTDOWN, countdown);
        tag.putByte(TAG_ROCKET_TYPE, (byte) getRocketType());
        keroseneTank().writeToNbt(tag, "fuel");
        oxygenTank().writeToNbt(tag, "oxidizer");
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        if (tag.contains(TAG_MODE)) {
            mode = tag.getByte(TAG_MODE);
        }
        if (tag.contains(TAG_STARTING)) {
            starting = tag.getBoolean(TAG_STARTING);
        }
        if (tag.contains(TAG_COUNTDOWN)) {
            countdown = tag.getInt(TAG_COUNTDOWN);
        }
        if (tag.contains(TAG_ROCKET_TYPE)) {
            syncedRocketType = tag.getByte(TAG_ROCKET_TYPE);
        }
        if (tag.contains("fuel") || tag.contains("fuel_type") || tag.contains("fuel_type_id")) {
            keroseneTank().readFromNbt(tag, "fuel");
            keroseneTank().setTankType(HbmFluids.KEROSENE);
        }
        if (tag.contains("oxidizer") || tag.contains("oxidizer_type") || tag.contains("oxidizer_type_id")) {
            oxygenTank().readFromNbt(tag, "oxidizer");
            oxygenTank().setTankType(HbmFluids.OXYGEN);
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        // TileEntitySoyuzLauncher#serialize: this state drives the world animation and ready loop.
        writeLegacyLoadedTileBinary(data);
        data.writeLong(energy.getPower());
        data.writeByte(mode);
        data.writeBoolean(starting);
        data.writeByte(getRocketType());
        LegacyFluidTankPacket.write(data, keroseneTank());
        LegacyFluidTankPacket.write(data, oxygenTank());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        energy.setPower(data.readLong());
        mode = data.readByte();
        starting = data.readBoolean();
        syncedRocketType = data.readByte();
        LegacyFluidTankPacket.read(data, keroseneTank());
        LegacyFluidTankPacket.read(data, oxygenTank());
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

    private record LauncherPort(BlockPos offset, Direction direction) {
    }
}
