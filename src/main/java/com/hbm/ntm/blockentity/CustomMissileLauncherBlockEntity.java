package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.entity.RadarCommandReceiver;
import com.hbm.ntm.api.item.DesignatorItem;
import com.hbm.ntm.block.CustomMissileLauncherBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.missile.CustomMissileEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.hbm.ntm.item.missile.CustomMissilePartProfile.PartSize;
import com.hbm.ntm.network.HbmClientMissileMultipartReceiver;
import com.hbm.ntm.network.MissileMultipartSnapshot;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CustomMissileLauncherBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, RadarCommandReceiver, HbmClientMissileMultipartReceiver {
    public static final int SLOT_MISSILE = 0;
    public static final int SLOT_DESIGNATOR = 1;
    public static final int SLOT_FUEL_INPUT = 2;
    public static final int SLOT_OXIDIZER_INPUT = 3;
    public static final int SLOT_SOLID_FUEL = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int SLOT_FUEL_OUTPUT = 6;
    public static final int SLOT_OXIDIZER_OUTPUT = 7;
    public static final int SLOT_COUNT = 8;

    private static final long MAX_POWER = 100_000L;
    private static final long LAUNCH_POWER = 75_000L;
    private static final double MULTIPART_SYNC_RANGE = 250.0D;
    private static final String TAG_POWER = "power";
    private static final String TAG_SOLID = "solidfuel";
    private static final String TAG_PAD_SIZE = "padSize";
    private static final String TAG_REDSTONE = "redstonePower";
    private static final String TAG_PREV_REDSTONE = "prevRedstonePower";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == SLOT_MISSILE) {
                syncMultipart();
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_MISSILE -> stack.getItem() instanceof CustomMissileItem && customAssembly(stack) != null;
                case SLOT_DESIGNATOR -> stack.getItem() instanceof DesignatorItem || hasLegacyDesignatorCoords(stack);
                case SLOT_FUEL_OUTPUT, SLOT_OXIDIZER_OUTPUT -> false;
                case SLOT_SOLID_FUEL -> isRocketFuel(stack);
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                default -> true;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };

    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new EmptyExternalItemHandler());
    private MissileMultipartSnapshot clientMultipart = MissileMultipartSnapshot.EMPTY;
    private boolean redstonePowered;
    private boolean prevRedstonePowered;
    private int solidFuel;
    private PartSize padSize;

    protected CustomMissileLauncherBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            int tankCapacity, PartSize padSize) {
        super(type, pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(new HbmFluidTank(HbmFluids.NONE, tankCapacity),
                        new HbmFluidTank(HbmFluids.NONE, tankCapacity)));
        this.padSize = padSize;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CustomMissileLauncherBlockEntity launcher) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, launcher);
        }
        boolean changed = launcher.tickMachine(level, pos, state);
        if (changed) {
            launcher.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        launcher.networkPackNT(50);
        if (level.getGameTime() % 20L == 0L) {
            launcher.syncMultipart();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CustomMissileLauncherBlockEntity launcher) {
        if (!level.isClientSide) {
            return;
        }
        if (level.getEntitiesOfClass(CustomMissileEntity.class,
                        new net.minecraft.world.phys.AABB(pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D,
                                pos.getX() + 1.5D, pos.getY() + 10.0D, pos.getZ() + 1.5D)).isEmpty()) {
            return;
        }
        ParticleUtil.spawnLaunchTableSmokeBurst(level, pos, launcher.kind() == CustomMissileLauncherBlock.Kind.LAUNCH_TABLE ? 0.65D : 0.5D);
    }

    private boolean tickMachine(Level level, BlockPos pos, BlockState state) {
        long oldPower = energy.getPower();
        int oldSolid = solidFuel;
        int oldFuel = fuelTank().getFill();
        int oldOxidizer = oxidizerTank().getFill();
        boolean oldRedstone = redstonePowered;

        processFluidItemTransfers(items, HbmFluidItemTransfer.loadTransfers(
                SLOT_FUEL_INPUT, SLOT_FUEL_OUTPUT, 2, fuelTank(), oxidizerTank()));
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        loadSolidFuel();
        updateTankTypes();

        redstonePowered = isPowered(level, pos);
        if (redstonePowered && !prevRedstonePowered && canLaunch()) {
            launchFromDesignator();
        }
        prevRedstonePowered = redstonePowered;

        return oldPower != energy.getPower()
                || oldSolid != solidFuel
                || oldFuel != fuelTank().getFill()
                || oldOxidizer != oxidizerTank().getFill()
                || oldRedstone != redstonePowered;
    }

    private void loadSolidFuel() {
        if (solidFuel + 250 <= maxSolidFuel() && isRocketFuel(items.getStackInSlot(SLOT_SOLID_FUEL))) {
            items.extractItem(SLOT_SOLID_FUEL, 1, false);
            solidFuel += 250;
        }
    }

    private boolean isPowered(Level level, BlockPos pos) {
        int radius = redstoneRadius();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (level.hasNeighborSignal(pos.offset(x, 0, z))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canLaunch() {
        return energy.getPower() >= LAUNCH_POWER
                && isMissileValid()
                && (!requiresDesignatorForCanLaunch() || hasDesignator())
                && hasFuel();
    }

    public boolean launchFromDesignator() {
        ItemStack stack = items.getStackInSlot(SLOT_DESIGNATOR);
        BlockPos target = designatorTarget(stack);
        return target != null && launchToCoordinate(target.getX(), target.getZ());
    }

    public boolean launchToCoordinate(int targetX, int targetZ) {
        if (!(level instanceof ServerLevel serverLevel) || !canLaunch()) {
            return false;
        }
        CustomMissilePartProfile.Assembly assembly = customAssembly();
        if (assembly == null) {
            return false;
        }
        int[] adjustedTarget = adjustedTarget(serverLevel, targetX, targetZ, assembly);
        CustomMissileEntity missile = new CustomMissileEntity(ModEntityTypes.MISSILE_CUSTOM.get(), serverLevel);
        missile.configureParts(assembly);
        missile.configureLaunch(worldPosition.getX() + 0.5D, worldPosition.getY() + 2.5D,
                worldPosition.getZ() + 0.5D, adjustedTarget[0], adjustedTarget[1]);
        serverLevel.addFreshEntity(missile);
        LegacySoundPlayer.playSoundEffect(serverLevel, worldPosition.getX(), worldPosition.getY(),
                worldPosition.getZ(), "hbm:weapon.missileTakeOff", SoundSource.PLAYERS, 10.0F, 1.0F);
        subtractFuel(assembly);
        items.setStackInSlot(SLOT_MISSILE, ItemStack.EMPTY);
        syncMultipart();
        setChanged();
        return true;
    }

    @Override
    public boolean sendCommandPosition(int x, int y, int z) {
        return launchToCoordinate(x, z);
    }

    @Override
    public boolean sendCommandEntity(Entity target) {
        return target != null && sendCommandPosition((int) Math.floor(target.getX()), worldPosition.getY(),
                (int) Math.floor(target.getZ()));
    }

    private int[] adjustedTarget(ServerLevel level, int targetX, int targetZ,
            CustomMissilePartProfile.Assembly assembly) {
        float inaccuracy = assembly.launchInaccuracy();
        double offsetX = (worldPosition.getX() - targetX) * inaccuracy;
        double offsetZ = (worldPosition.getZ() - targetZ) * inaccuracy;
        double angle = level.random.nextFloat() * Math.PI * 2.0D;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new int[] {
                targetX + (int) (offsetX * cos + offsetZ * sin),
                targetZ + (int) (offsetZ * cos - offsetX * sin)
        };
    }

    private void subtractFuel(CustomMissilePartProfile.Assembly assembly) {
        int fuel = requiredFuel(assembly);
        switch (assembly.fuselage().profile().fuelType()) {
            case KEROSENE, HYDROGEN, BALEFIRE -> {
                fuelTank().drain(fuel, false);
                oxidizerTank().drain(fuel, false);
            }
            case XENON -> fuelTank().drain(fuel, false);
            case SOLID -> solidFuel = Math.max(0, solidFuel - fuel);
        }
        energy.setPower(Math.max(0L, energy.getPower() - LAUNCH_POWER));
    }

    public boolean isMissileValid() {
        CustomMissilePartProfile.Assembly assembly = customAssembly();
        return assembly != null && assembly.isCompleteForLaunch() && assembly.fuselage().profile().top() == padSize;
    }

    public boolean hasDesignator() {
        return designatorTarget(items.getStackInSlot(SLOT_DESIGNATOR)) != null;
    }

    public int solidState() {
        CustomMissilePartProfile.Assembly assembly = customAssembly();
        if (assembly == null || assembly.fuselage() == null) {
            return -1;
        }
        if (assembly.fuselage().profile().fuelType() != CustomMissilePartProfile.FuelType.SOLID) {
            return -1;
        }
        return solidFuel >= requiredFuel(assembly) ? 1 : 0;
    }

    public int liquidState() {
        CustomMissilePartProfile.Assembly assembly = customAssembly();
        if (assembly == null || assembly.fuselage() == null) {
            return -1;
        }
        return switch (assembly.fuselage().profile().fuelType()) {
            case KEROSENE, HYDROGEN, XENON, BALEFIRE ->
                    fuelTank().getFill() >= requiredFuel(assembly) ? 1 : 0;
            case SOLID -> -1;
        };
    }

    public int oxidizerState() {
        CustomMissilePartProfile.Assembly assembly = customAssembly();
        if (assembly == null || assembly.fuselage() == null) {
            return -1;
        }
        return switch (assembly.fuselage().profile().fuelType()) {
            case KEROSENE, HYDROGEN, BALEFIRE -> oxidizerTank().getFill() >= requiredFuel(assembly) ? 1 : 0;
            case XENON, SOLID -> -1;
        };
    }

    public boolean hasFuel() {
        return solidState() != 0 && liquidState() != 0 && oxidizerState() != 0;
    }

    private void updateTankTypes() {
        CustomMissilePartProfile.Assembly assembly = customAssembly();
        if (assembly == null || assembly.fuselage() == null) {
            return;
        }
        switch (assembly.fuselage().profile().fuelType()) {
            case KEROSENE -> {
                fuelTank().setTankType(HbmFluids.KEROSENE);
                oxidizerTank().setTankType(HbmFluids.PEROXIDE);
            }
            case HYDROGEN -> {
                fuelTank().setTankType(HbmFluids.HYDROGEN);
                oxidizerTank().setTankType(HbmFluids.OXYGEN);
            }
            case XENON -> fuelTank().setTankType(HbmFluids.XENON);
            case BALEFIRE -> {
                fuelTank().setTankType(HbmFluids.BALEFIRE);
                oxidizerTank().setTankType(HbmFluids.PEROXIDE);
            }
            case SOLID -> {
            }
        }
    }

    @Nullable
    private BlockPos designatorTarget(ItemStack stack) {
        if (level != null && stack.getItem() instanceof DesignatorItem designator
                && designator.isReady(level, stack, worldPosition)) {
            return BlockPos.containing(designator.getCoords(level, stack, worldPosition));
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("xCoord") && tag.contains("zCoord")) {
            return new BlockPos(tag.getInt("xCoord"), worldPosition.getY(), tag.getInt("zCoord"));
        }
        return null;
    }

    @Nullable
    public CustomMissilePartProfile.Assembly customAssembly() {
        return customAssembly(items.getStackInSlot(SLOT_MISSILE));
    }

    @Nullable
    private static CustomMissilePartProfile.Assembly customAssembly(ItemStack stack) {
        return CustomMissilePartProfile.assemblyFromStack(stack);
    }

    public MissileMultipartSnapshot multipartSnapshot() {
        return MissileMultipartSnapshot.ofMissile(items.getStackInSlot(SLOT_MISSILE));
    }

    @Nullable
    public CustomMissilePartProfile.Assembly assemblyForPreview() {
        if (level != null && level.isClientSide && !clientMultipart.isEmpty()) {
            return clientMultipart.toAssembly();
        }
        return customAssembly();
    }

    @Override
    public void handleClientMissileMultipart(MissileMultipartSnapshot multipart) {
        clientMultipart = multipart == null ? MissileMultipartSnapshot.EMPTY : multipart;
        setChanged();
    }

    private void syncMultipart() {
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            ModMessages.sendToAllAround(ModMessages.missileMultipartPacket(worldPosition, multipartSnapshot()),
                    serverLevel, worldPosition, MULTIPART_SYNC_RANGE);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public HbmFluidTank fuelTank() {
        return getAllTanks().get(0);
    }

    public HbmFluidTank oxidizerTank() {
        return getAllTanks().get(1);
    }

    public long getStoredPower() {
        return energy.getPower();
    }

    public long getMaxStoredPower() {
        return energy.getMaxPower();
    }

    public int getSolidFuel() {
        return solidFuel;
    }

    public int getMaxSolidFuel() {
        return maxSolidFuel();
    }

    public int getSolidBarHeight(int height) {
        return maxSolidFuel() <= 0 ? 0 : solidFuel * height / maxSolidFuel();
    }

    public PartSize getPadSize() {
        return padSize;
    }

    public void setPadSize(PartSize padSize) {
        this.padSize = padSize == null ? defaultPadSize() : padSize;
        setChanged();
    }

    protected abstract CustomMissileLauncherBlock.Kind kind();

    protected abstract int tankCapacity();

    protected abstract int maxSolidFuel();

    protected abstract int redstoneRadius();

    protected abstract PartSize defaultPadSize();

    protected boolean requiresDesignatorForCanLaunch() {
        return true;
    }

    protected abstract List<FluidPort> fluidPorts();

    protected abstract List<EnergyPort> energyPorts();

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(fuelTank(), oxidizerTank());
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
        return List.of(fuelTank(), oxidizerTank());
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == fuelTank().getTankType() || type == oxidizerTank().getTankType();
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        return fluidPorts();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return energyPorts();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return type != null && type != HbmFluids.NONE
                && (type == fuelTank().getTankType() || type == oxidizerTank().getTankType());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        fuelTank().writeToNbt(tag, "fuel");
        oxidizerTank().writeToNbt(tag, "oxidizer");
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_SOLID, solidFuel);
        tag.putInt(TAG_PAD_SIZE, padSize.ordinal());
        tag.putBoolean(TAG_REDSTONE, redstonePowered);
        tag.putBoolean(TAG_PREV_REDSTONE, prevRedstonePowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        fuelTank().readFromNbt(tag, "fuel");
        oxidizerTank().readFromNbt(tag, "oxidizer");
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        solidFuel = tag.getInt(TAG_SOLID);
        if (tag.contains(TAG_PAD_SIZE)) {
            int ordinal = tag.getInt(TAG_PAD_SIZE);
            PartSize[] values = PartSize.values();
            padSize = ordinal >= 0 && ordinal < values.length ? values[ordinal] : defaultPadSize();
        }
        redstonePowered = tag.getBoolean(TAG_REDSTONE);
        prevRedstonePowered = tag.getBoolean(TAG_PREV_REDSTONE);
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
        BlockState state = getBlockState();
        AABB structureBounds = state.getBlock() instanceof CustomMissileLauncherBlock block
                ? block.getMultiblockLayout(state, level, worldPosition).renderBoundingBox(worldPosition, 0.5D)
                : new AABB(worldPosition).inflate(0.5D);
        return structureBounds.minmax(launcherVisibleBounds(state));
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

    @Override
    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static boolean hasLegacyDesignatorCoords(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("xCoord") && tag.contains("zCoord");
    }

    private static boolean isRocketFuel(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return ForgeRegistries.ITEMS.getKey(stack.getItem()) != null
                && "rocket_fuel".equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath());
    }

    private static int requiredFuel(CustomMissilePartProfile.Assembly assembly) {
        return Math.max(0, Math.round(assembly.fuselage().profile().fuel()));
    }

    private AABB launcherVisibleBounds(BlockState state) {
        boolean launchTable = state.getBlock() instanceof CustomMissileLauncherBlock block
                && block.kind() == CustomMissileLauncherBlock.Kind.LAUNCH_TABLE;
        int radius = launchTable ? 4 : 2;
        CustomMissilePartProfile.Assembly assembly = assemblyForPreview();
        double missileHeight = estimatedMissileHeight(assembly);
        double missileTop = isValidForPad(assembly, launchTable ? getPadSize() : PartSize.SIZE_10)
                ? 2.0625D + missileHeight
                : 1.0D;
        double top = launchTable
                ? Math.max(missileTop, Math.max(10.0D, Math.ceil(missileHeight)) + 2.0D)
                : Math.max(missileTop, 2.0D);
        return new AABB(
                worldPosition.getX() - radius,
                worldPosition.getY(),
                worldPosition.getZ() - radius,
                worldPosition.getX() + radius + 1,
                worldPosition.getY() + top,
                worldPosition.getZ() + radius + 1);
    }

    private static boolean isValidForPad(@Nullable CustomMissilePartProfile.Assembly assembly, PartSize padSize) {
        return assembly != null && assembly.isCompleteForLaunch()
                && assembly.fuselage() != null
                && assembly.fuselage().profile().top() == padSize;
    }

    private static double estimatedMissileHeight(@Nullable CustomMissilePartProfile.Assembly assembly) {
        if (assembly == null) {
            return 10.0D;
        }
        double height = estimatedPartHeight(assembly.thruster())
                + estimatedPartHeight(assembly.fuselage())
                + estimatedPartHeight(assembly.warhead());
        return height <= 0.0D ? 10.0D : height;
    }

    private static double estimatedPartHeight(@Nullable CustomMissilePartProfile.ResolvedPart part) {
        if (part == null || part.profile() == null) {
            return 0.0D;
        }
        CustomMissilePartProfile profile = part.profile();
        return switch (profile.type()) {
            case THRUSTER -> switch (profile.top()) {
                case SIZE_10 -> 1.0D;
                case SIZE_15, SIZE_20 -> 3.0D;
                default -> 0.0D;
            };
            case FUSELAGE -> estimatedFuselageHeight(part.legacyName(), profile.top(), profile.bottom());
            case WARHEAD -> switch (profile.bottom()) {
                case SIZE_10 -> 2.5D;
                case SIZE_15 -> 3.5D;
                case SIZE_20 -> 5.0D;
                default -> 0.0D;
            };
            default -> 0.0D;
        };
    }

    private static double estimatedFuselageHeight(String legacyName, PartSize top, PartSize bottom) {
        if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) {
            return 16.0D;
        }
        if (top == PartSize.SIZE_15) {
            return 10.0D;
        }
        if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) {
            return 9.0D;
        }
        if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) {
            return legacyName != null && legacyName.contains("_long_") ? 7.0D : 4.0D;
        }
        return 10.0D;
    }

    private final class EmptyExternalItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_MISSILE) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(SLOT_MISSILE) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
