package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.TurbofanMenu;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
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

public class TurbofanBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmFluidCopiable {
    public static final int SLOT_FLUID_INPUT = 0;
    public static final int SLOT_FLUID_OUTPUT = 1;
    public static final int SLOT_AFTERBURN = 2;
    public static final int SLOT_BATTERY = 3;
    public static final int SLOT_IDENTIFIER = 4;
    public static final int SLOT_COUNT = 5;

    public static final long MAX_POWER = 1_000_000L;
    private static final int TANK_CAPACITY = 24_000;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(UpgradeType.AFTERBURN, 3);
    private static final String TAG_ITEMS = "items";
    private static final String TAG_LEGACY_POWER = "powerTime";
    private static final String TAG_SHOW_BLOOD = "showBlood";

    private final HbmFluidTank fuelTank;
    private final HbmFluidTank bloodTank;
    private final MachinePollutionBuffers pollution = new MachinePollutionBuffers(150);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_FLUID_INPUT, SLOT_BATTERY, SLOT_IDENTIFIER -> true;
                case SLOT_AFTERBURN -> stack.getItem() instanceof ItemMachineUpgrade upgrade
                        && upgrade.getUpgradeType() == UpgradeType.AFTERBURN;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int afterburner;
    private boolean wasOn;
    private boolean showBlood;
    private int lastOutput;
    private int lastConsumption;
    private float spin;
    private float lastSpin;
    private int momentum;
    private Object audioLoop;

    public TurbofanBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER),
                new HbmFluidTank(HbmFluids.KEROSENE, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.BLOOD, TANK_CAPACITY));
    }

    private TurbofanBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank fuelTank,
            HbmFluidTank bloodTank) {
        super(ModBlockEntities.TURBOFAN.get(), pos, state, energy, List.of(fuelTank, bloodTank));
        this.fuelTank = fuelTank;
        this.fuelTank.conform(new HbmFluidStack(HbmFluids.KEROSENE, 0));
        this.bloodTank = bloodTank;
        this.bloodTank.conform(new HbmFluidStack(HbmFluids.BLOOD, 0));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TurbofanBlockEntity turbofan) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, turbofan);
        long oldPower = turbofan.energy.getPower();
        int oldFuel = turbofan.fuelTank.getFill();
        int oldBlood = turbofan.bloodTank.getFill();
        boolean oldWasOn = turbofan.wasOn;
        boolean oldShowBlood = turbofan.showBlood;
        int oldAfterburner = turbofan.afterburner;

        boolean changed = turbofan.setFluidTankTypeFromIdentifierSlot(turbofan.items, SLOT_IDENTIFIER,
                turbofan.fuelTank);
        changed |= turbofan.processFluidItemTransfers(turbofan.items,
                HbmFluidItemTransfer.loadTransfers(SLOT_FLUID_INPUT, SLOT_FLUID_OUTPUT, turbofan.fuelTank));
        turbofan.bloodTank.setTankType(HbmFluids.BLOOD);

        turbofan.wasOn = false;
        turbofan.lastOutput = 0;
        turbofan.lastConsumption = 0;
        turbofan.afterburner = LegacyMachineUpgradeManager
                .checkSlots(turbofan.items, SLOT_AFTERBURN, SLOT_AFTERBURN, VALID_UPGRADES)
                .getLevel(UpgradeType.AFTERBURN);

        if (!turbofan.isRedstoneStopped(level, pos)) {
            changed |= turbofan.burnFuel(level, pos);
        }

        HbmEnergyUtil.chargeItemFromStorage(turbofan.items.getStackInSlot(SLOT_BATTERY),
                turbofan.energy, turbofan.energy.getProviderSpeed());
        turbofan.tryProvideEnergyToPorts();
        if (!turbofan.bloodTank.isEmpty()) {
            turbofan.tryProvideFluidToPorts(turbofan.bloodTank.getTankType(), turbofan.bloodTank.getPressure(),
                    turbofan);
        }
        turbofan.tryProvideSmokeToPorts(level, pos);
        if (turbofan.fuelTank.getTankType() != HbmFluids.NONE) {
            turbofan.refreshTrackedReceiverFluidPortsReport(List.of(turbofan.fuelTank), turbofan);
        }
        turbofan.energy.setPower(Math.min(MAX_POWER, turbofan.energy.getPower()));

        if (turbofan.lastOutput > 0) {
            turbofan.runEntityAndParticleEffects(level, pos);
        }

        changed |= oldPower != turbofan.energy.getPower()
                || oldFuel != turbofan.fuelTank.getFill()
                || oldBlood != turbofan.bloodTank.getFill()
                || oldWasOn != turbofan.wasOn
                || oldShowBlood != turbofan.showBlood
                || oldAfterburner != turbofan.afterburner;
        turbofan.networkPackNT(150);
        if (changed || level.getGameTime() % 20L == 0L) {
            turbofan.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TurbofanBlockEntity turbofan) {
        if (!level.isClientSide) {
            return;
        }
        turbofan.lastSpin = turbofan.spin;
        if (turbofan.wasOn) {
            if (turbofan.momentum < 100) {
                turbofan.momentum++;
            }
        } else if (turbofan.momentum > 0) {
            turbofan.momentum--;
        }
        turbofan.spin += turbofan.momentum / 2.0F;
        if (turbofan.spin >= 360.0F) {
            turbofan.spin -= 360.0F;
            turbofan.lastSpin -= 360.0F;
        }
        float volume = turbofan.momentum / 50.0F;
        float pitch = turbofan.momentum / 200.0F + 0.5F + turbofan.afterburner * 0.16F;
        turbofan.audioLoop = LegacyMachineAudioBridge.updateLoop(turbofan.audioLoop, turbofan,
                "hbm:block.turbofanOperate", turbofan.momentum > 0, 1.0D, 50.0F, volume, pitch);
    }

    private boolean burnFuel(Level level, BlockPos pos) {
        int amount = 1 + afterburner;
        int amountToBurn = Math.min(amount, fuelTank.getFill());
        if (amountToBurn <= 0) {
            return false;
        }

        CombustibleFluidTrait combustible = fuelTank.getTankType().getTrait(CombustibleFluidTrait.class);
        long burnValue = combustible != null && combustible.getGrade() == CombustibleFluidTrait.FuelGrade.AERO
                ? combustible.getCombustionEnergyPerBucket() / 1_000L
                : 0L;
        fuelTank.drain(amountToBurn, false);
        wasOn = true;
        lastOutput = (int) Math.min(Integer.MAX_VALUE,
                burnValue * amountToBurn * (1.0D + Math.min(afterburner / 3.0D, 4.0D)));
        lastConsumption = amountToBurn;
        energy.setPower(Math.min(MAX_POWER, energy.getPower() + lastOutput));
        if (level.getGameTime() % 20L == 0L) {
            pollution.polluteFluidRelease(level, pos, fuelTank.getTankType(), FluidReleaseType.BURN,
                    amountToBurn * 5.0F);
        }
        return true;
    }

    private boolean isRedstoneStopped(Level level, BlockPos pos) {
        for (FluidPort port : turbofanFluidPorts()) {
            if (level.hasNeighborSignal(pos.offset(port.offset()))) {
                return true;
            }
        }
        return false;
    }

    private void runEntityAndParticleEffects(Level level, BlockPos pos) {
        Direction dir = facing();
        Direction side = dir.getClockWise();
        if (afterburner > 0) {
            ParticleUtil.spawnTurbofanAfterburnerFlame(level, pos, dir, 0);
            ParticleUtil.spawnTurbofanAfterburnerFlame(level, pos, dir, 1);
            if (afterburner > 90 && level.random.nextInt(30) == 0) {
                LegacySoundPlayer.playSoundEffect(level, pos, "hbm:block.damage", SoundSource.BLOCKS, 3.0F,
                        0.95F + level.random.nextFloat() * 0.2F);
            }
            if (afterburner > 90) {
                ParticleUtil.spawnTurbofanDamageGasFlame(level, pos, dir);
            }
        }

        AABB exhaust = horizontalBox(pos, dir, side, -3.5D, -19.5D, 1.5D, 0.0D, 3.0D);
        for (Entity entity : level.getEntities(null, exhaust)) {
            if (afterburner > 0) {
                entity.setSecondsOnFire(5);
                entity.hurt(level.damageSources().onFire(), 5.0F);
            }
            push(entity, dir);
        }

        AABB intake = horizontalBox(pos, dir, side, 3.5D, 8.5D, 1.5D, 0.0D, 3.0D);
        for (Entity entity : level.getEntities(null, intake)) {
            push(entity, dir);
        }

        AABB blades = horizontalBox(pos, dir, side, 3.5D, 3.75D, 1.5D, 0.0D, 3.0D);
        for (Entity entity : level.getEntities(null, blades)) {
            boolean wasAlive = entity.isAlive();
            entity.hurt(ModDamageSources.source(level, ModDamageSources.BLENDER), 1_000.0F);
            entity.makeStuckInBlock(Blocks.COBWEB.defaultBlockState(), new Vec3(0.25D, 0.05D, 0.25D));
            if (wasAlive && !entity.isAlive() && entity instanceof LivingEntity) {
                ParticleUtil.spawnGiblets(entity, ParticleUtil.GIBLET_MEAT, 5);
                LegacySoundPlayer.playSoundAtEntity(entity, "mob.zombie.woodbreak", SoundSource.NEUTRAL, 2.0F,
                        0.95F + level.random.nextFloat() * 0.2F);
                bloodTank.setFill(bloodTank.getFill() + 50);
                showBlood = true;
            }
        }
    }

    private static void push(Entity entity, Direction dir) {
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x - dir.getStepX() * 0.2D, motion.y,
                motion.z - dir.getStepZ() * 0.2D);
        entity.hurtMarked = true;
    }

    private static AABB horizontalBox(BlockPos pos, Direction dir, Direction side, double forwardA, double forwardB,
            double sideRadius, double minY, double maxY) {
        double x1 = pos.getX() + 0.5D + dir.getStepX() * forwardA - side.getStepX() * sideRadius;
        double x2 = pos.getX() + 0.5D + dir.getStepX() * forwardB + side.getStepX() * sideRadius;
        double z1 = pos.getZ() + 0.5D + dir.getStepZ() * forwardA - side.getStepZ() * sideRadius;
        double z2 = pos.getZ() + 0.5D + dir.getStepZ() * forwardB + side.getStepZ() * sideRadius;
        return new AABB(Math.min(x1, x2), pos.getY() + minY, Math.min(z1, z2),
                Math.max(x1, x2), pos.getY() + maxY, Math.max(z1, z2));
    }

    private void tryProvideSmokeToPorts(Level level, BlockPos pos) {
        for (FluidPort port : turbofanFluidPorts()) {
            BlockPos connector = pos.offset(port.offset());
            pollution.sendSmoke(level, connector.getX(), connector.getY(), connector.getZ(), port.direction());
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private List<FluidPort> turbofanFluidPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX(), 0, facing.getStepZ(), facing),
                FluidPort.of(facing.getStepX() - side.getStepX(), 0,
                        facing.getStepZ() - side.getStepZ(), facing),
                FluidPort.of(-facing.getStepX(), 0, -facing.getStepZ(), facing.getOpposite()),
                FluidPort.of(-facing.getStepX() - side.getStepX(), 0,
                        -facing.getStepZ() - side.getStepZ(), facing.getOpposite()));
    }

    private List<EnergyPort> turbofanEnergyPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                EnergyPort.of(facing.getStepX(), 0, facing.getStepZ(), facing),
                EnergyPort.of(facing.getStepX() - side.getStepX(), 0,
                        facing.getStepZ() - side.getStepZ(), facing),
                EnergyPort.of(-facing.getStepX(), 0, -facing.getStepZ(), facing.getOpposite()),
                EnergyPort.of(-facing.getStepX() - side.getStepX(), 0,
                        -facing.getStepZ() - side.getStepZ(), facing.getOpposite()));
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getFuelTank() {
        return fuelTank;
    }

    public HbmFluidTank getBloodTank() {
        return bloodTank;
    }

    public int getAfterburner() {
        return afterburner;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public boolean showsBlood() {
        return showBlood;
    }

    public int getLastOutput() {
        return lastOutput;
    }

    public int getLastConsumption() {
        return lastConsumption;
    }

    public float getBladeSpin(float partialTick) {
        return Mth.lerp(partialTick, lastSpin, spin);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return turbofanEnergyPorts();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return turbofanFluidPorts();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == fuelTank.getTankType();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(fuelTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(bloodTank);
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
        onFluidContentsChanged();
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return fuelTank.getSpace();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == Direction.DOWN ? HbmFluidSideMode.NONE : HbmFluidSideMode.BOTH;
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.energyStored(energy.getPower(), energy.getMaxPower()),
                LegacyLookOverlayLines.tank(true, fuelTank),
                LegacyLookOverlayLines.tank(false, bloodTank),
                Component.literal("Afterburner: " + afterburner)));
    }

    @Override
    public CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray(HbmFluidCopiable.TAG_FLUID_IDS, getFluidIdsToCopy());
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        if (tag == null || !tag.contains(HbmFluidCopiable.TAG_FLUID_IDS)) {
            return false;
        }
        int[] ids = tag.getIntArray(HbmFluidCopiable.TAG_FLUID_IDS);
        if (ids.length == 0) {
            return false;
        }
        int safeIndex = index >= 0 && index < ids.length ? index : 0;
        fuelTank.setTankType(HbmFluids.fromId(ids[safeIndex]));
        onFluidContentsChanged();
        return true;
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, lastOutput > 0);
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, lastConsumption);
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, lastOutput);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineTurbofan", "Turbofan");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new TurbofanMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        pollution.writeLegacyNbt(tag);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putBoolean(TAG_SHOW_BLOOD, showBlood);
        tag.putInt("afterburner", afterburner);
        tag.putBoolean("wasOn", wasOn);
        tag.putInt("output", lastOutput);
        tag.putInt("consumption", lastConsumption);
        tag.putFloat("spin", spin);
        tag.putFloat("lastSpin", lastSpin);
        tag.putInt("momentum", momentum);
        fuelTank.writeToNbt(tag, "fuel");
        bloodTank.writeToNbt(tag, "blood");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        pollution.readLegacyNbt(tag);
        if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        showBlood = tag.getBoolean(TAG_SHOW_BLOOD);
        afterburner = tag.getInt("afterburner");
        wasOn = tag.getBoolean("wasOn");
        lastOutput = tag.getInt("output");
        lastConsumption = tag.getInt("consumption");
        spin = tag.getFloat("spin");
        lastSpin = tag.getFloat("lastSpin");
        momentum = tag.getInt("momentum");
        if (tag.contains("fuel") || tag.contains("fuel_type")) {
            fuelTank.readFromNbt(tag, "fuel");
        }
        if (tag.contains("blood") || tag.contains("blood_type")) {
            bloodTank.readFromNbt(tag, "blood");
        }
        if (bloodTank.getTankType() == HbmFluids.NONE) {
            bloodTank.setTankType(HbmFluids.BLOOD);
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

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return switch (slot) {
                case SLOT_FLUID_INPUT, SLOT_AFTERBURN, SLOT_BATTERY, SLOT_IDENTIFIER ->
                        items.insertItem(slot, stack, simulate);
                default -> stack;
            };
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == SLOT_FLUID_OUTPUT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < SLOT_COUNT && items.isItemValid(slot, stack);
        }
    }
}
