package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.BedrockOreBaseItem;
import com.hbm.ntm.item.BedrockOreItem;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreGrade;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.OreSlopperMenu;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OreSlopperBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmFluidCopiable, LegacyUpgradeInfoProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_IDENTIFIER = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT_START = 3;
    public static final int SLOT_OUTPUT_END = 8;
    public static final int SLOT_UPGRADE_1 = 9;
    public static final int SLOT_UPGRADE_2 = 10;
    public static final int SLOT_COUNT = 11;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final long MAX_POWER = 100_000L;
    private static final long BASE_CONSUMPTION = 200L;
    private static final int WATER_USED = 1_000;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.EFFECT, 3);

    private final HbmFluidTank waterTank;
    private final HbmFluidTank slopTank;
    private final double[] ores = new double[BedrockOreType.values().length];
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_INPUT -> stack.is(ModItems.BEDROCK_ORE_BASE.get());
                case SLOT_UPGRADE_1, SLOT_UPGRADE_2 -> isValidUpgrade(stack);
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private float progress;
    private long consumption = BASE_CONSUMPTION;
    private boolean processing;
    private SlopperAnimation animation = SlopperAnimation.LOWERING;
    private float slider;
    private float prevSlider;
    private float bucket;
    private float prevBucket;
    private float blades;
    private float prevBlades;
    private float fan;
    private float prevFan;
    private int delay;
    @Nullable
    private String customName;

    public OreSlopperBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WATER, 16_000),
                new HbmFluidTank(HbmFluids.SLOP, 16_000));
    }

    private OreSlopperBlockEntity(BlockPos pos, BlockState state, HbmFluidTank waterTank, HbmFluidTank slopTank) {
        super(ModBlockEntities.ORE_SLOPPER.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L), List.of(waterTank, slopTank));
        this.waterTank = waterTank;
        this.slopTank = slopTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OreSlopperBlockEntity slopper) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, slopper);
        long oldPower = slopper.energy.getPower();
        float oldProgress = slopper.progress;
        boolean oldProcessing = slopper.processing;
        HbmFluidTank.TankState oldWater = slopper.waterTank.snapshot();
        HbmFluidTank.TankState oldSlop = slopper.slopTank.snapshot();

        HbmEnergyUtil.chargeStorageFromItem(slopper.items.getStackInSlot(SLOT_BATTERY),
                slopper.energy, slopper.energy.getReceiverSpeed());
        slopper.updateWaterTypeFromIdentifier();
        slopper.refreshTrackedTransceiverFluidPortsReport(slopper.getReceivingTanks(),
                slopper.getSendingTanks(), slopper);

        LegacyMachineUpgradeManager.Levels upgrades =
                LegacyMachineUpgradeManager.checkSlots(slopper.items, SLOT_UPGRADE_1, SLOT_UPGRADE_2, VALID_UPGRADES);
        int speed = upgrades.getLevel(UpgradeType.SPEED);
        int effect = upgrades.getLevel(UpgradeType.EFFECT);
        slopper.consumption = BASE_CONSUMPTION + BASE_CONSUMPTION * speed / 2L + BASE_CONSUMPTION * effect;
        slopper.processing = false;

        if (slopper.canSlop()) {
            slopper.energy.setPower(slopper.energy.getPower() - slopper.consumption);
            slopper.progress += 1.0F / (600.0F - speed * 150.0F);
            slopper.processing = true;
            while (slopper.progress >= 1.0F && slopper.canSlop()) {
                slopper.progress -= 1.0F;
                slopper.finishOneCycle(effect);
            }
            slopper.runEntityEffects(level, pos);
        } else {
            slopper.progress = 0.0F;
        }
        slopper.flushOreFractionsToOutputs();

        boolean changed = oldPower != slopper.energy.getPower()
                || oldProgress != slopper.progress
                || oldProcessing != slopper.processing
                || !oldWater.equals(slopper.waterTank.snapshot())
                || !oldSlop.equals(slopper.slopTank.snapshot());
        slopper.networkPackNT(150);
        if (changed || level.getGameTime() % 20L == 0L) {
            slopper.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, OreSlopperBlockEntity slopper) {
        slopper.prevSlider = slopper.slider;
        slopper.prevBucket = slopper.bucket;
        slopper.prevBlades = slopper.blades;
        slopper.prevFan = slopper.fan;
        if (!slopper.processing) {
            return;
        }
        slopper.blades += 15.0F;
        slopper.fan += 35.0F;
        if (slopper.blades >= 360.0F) {
            slopper.blades -= 360.0F;
            slopper.prevBlades -= 360.0F;
        }
        if (slopper.fan >= 360.0F) {
            slopper.fan -= 360.0F;
            slopper.prevFan -= 360.0F;
        }
        if (slopper.animation == SlopperAnimation.DUMPING) {
            Direction dir = slopper.facing();
            ParticleUtil.spawnVanillaExtBlockDust(level,
                    pos.getX() + 0.5D + dir.getStepX() + level.random.nextGaussian() * 0.25D,
                    pos.getY() + 4.25D,
                    pos.getZ() + 0.5D + dir.getStepZ() + level.random.nextGaussian() * 0.25D,
                    0.0D, -0.2D, 0.0D, Blocks.IRON_BLOCK);
        }
        if (slopper.delay > 0) {
            slopper.delay--;
            return;
        }
        switch (slopper.animation) {
            case LOWERING -> {
                slopper.bucket += 1.0F / 40.0F;
                if (slopper.bucket >= 1.0F) {
                    slopper.bucket = 1.0F;
                    slopper.animation = SlopperAnimation.LIFTING;
                    slopper.delay = 20;
                }
            }
            case LIFTING -> {
                slopper.bucket -= 1.0F / 40.0F;
                if (slopper.bucket <= 0.0F) {
                    slopper.bucket = 0.0F;
                    slopper.animation = SlopperAnimation.MOVE_SHREDDER;
                    slopper.delay = 10;
                }
            }
            case MOVE_SHREDDER -> {
                slopper.slider += 1.0F / 50.0F;
                if (slopper.slider >= 1.0F) {
                    slopper.slider = 1.0F;
                    slopper.animation = SlopperAnimation.DUMPING;
                    slopper.delay = 60;
                }
            }
            case DUMPING -> slopper.animation = SlopperAnimation.MOVE_BUCKET;
            case MOVE_BUCKET -> {
                slopper.slider -= 1.0F / 50.0F;
                if (slopper.slider <= 0.0F) {
                    slopper.slider = 0.0F;
                    slopper.animation = SlopperAnimation.LOWERING;
                    slopper.delay = 10;
                }
            }
        }
    }

    private void updateWaterTypeFromIdentifier() {
        setFluidTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, waterTank);
        FluidType output = getFluidOutput(waterTank.getTankType());
        if (output != null) {
            slopTank.setTankType(output);
        }
    }

    @Nullable
    private static FluidType getFluidOutput(FluidType input) {
        return input == HbmFluids.WATER ? HbmFluids.SLOP : null;
    }

    private boolean canSlop() {
        if (getFluidOutput(waterTank.getTankType()) == null) {
            return false;
        }
        if (waterTank.getFill() < WATER_USED || slopTank.getFill() + WATER_USED > slopTank.getMaxFill()) {
            return false;
        }
        if (energy.getPower() < consumption) {
            return false;
        }
        return items.getStackInSlot(SLOT_INPUT).is(ModItems.BEDROCK_ORE_BASE.get());
    }

    private void finishOneCycle(int effect) {
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        for (BedrockOreType type : BedrockOreType.values()) {
            ores[type.ordinal()] += BedrockOreBaseItem.getOreAmount(input, type) * (1.0D + effect * 0.1D);
        }
        items.extractItem(SLOT_INPUT, 1, false);
        waterTank.setFill(waterTank.getFill() - WATER_USED);
        slopTank.setFill(slopTank.getFill() + WATER_USED);
        onFluidContentsChanged();
    }

    private void flushOreFractionsToOutputs() {
        for (BedrockOreType type : BedrockOreType.values()) {
            while (ores[type.ordinal()] >= 1.0D) {
                ItemStack output = BedrockOreItem.make(BedrockOreGrade.BASE, type);
                if (!mergeOutput(output)) {
                    break;
                }
                ores[type.ordinal()] -= 1.0D;
            }
        }
    }

    private boolean mergeOutput(ItemStack output) {
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, output)
                    && existing.getCount() < existing.getMaxStackSize()) {
                existing.grow(1);
                items.setStackInSlot(slot, existing);
                return true;
            }
        }
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                items.setStackInSlot(slot, output.copy());
                return true;
            }
        }
        return false;
    }

    private void runEntityEffects(Level level, BlockPos pos) {
        Direction dir = facing();
        AABB box = new AABB(pos.getX() - 0.5D + dir.getStepX(), pos.getY() + 1.0D,
                pos.getZ() - 0.5D + dir.getStepZ(),
                pos.getX() + 1.5D + dir.getStepX(), pos.getY() + 3.0D,
                pos.getZ() + 1.5D + dir.getStepZ());
        for (Entity entity : level.getEntities(null, box)) {
            boolean wasAlive = entity.isAlive();
            entity.hurt(ModDamageSources.source(level, ModDamageSources.BLENDER), 1_000.0F);
            if (wasAlive && !entity.isAlive() && entity instanceof LivingEntity) {
                ParticleUtil.spawnGiblets(entity, ParticleUtil.GIBLET_MEAT, 5);
                LegacySoundPlayer.playSoundAtEntity(entity, "mob.zombie.woodbreak", SoundSource.NEUTRAL, 2.0F,
                        0.95F + level.random.nextFloat() * 0.2F);
            }
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getWaterTank() {
        return waterTank;
    }

    public HbmFluidTank getSlopTank() {
        return slopTank;
    }

    public float getProgress() {
        return progress;
    }

    public int getProgressScaled(int max) {
        return (int) (progress * max);
    }

    public long getConsumption() {
        return consumption;
    }

    public boolean isProcessing() {
        return processing;
    }

    public SlopperAnimation getAnimation() {
        return animation;
    }

    public double getSlider(float partialTick) {
        return prevSlider + (slider - prevSlider) * partialTick;
    }

    public double getBucket(float partialTick) {
        return prevBucket + (bucket - prevBucket) * partialTick;
    }

    public double getBlades(float partialTick) {
        return prevBlades + (blades - prevBlades) * partialTick;
    }

    public double getFan(float partialTick) {
        return prevFan + (fan - prevFan) * partialTick;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(waterTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(slopTank);
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
        long before = slopTank.getFill();
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (before != slopTank.getFill()) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return getReceivingTanks();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return connectionPorts().stream()
                .map(port -> EnergyPort.of(port.offset().getX(), port.offset().getY(), port.offset().getZ(),
                        port.direction()))
                .toList();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return connectionPorts();
    }

    private List<FluidPort> connectionPorts() {
        Direction dir = facing();
        Direction side = dir.getClockWise();
        List<FluidPort> ports = new ArrayList<>();
        ports.add(port(dir, 4, dir));
        ports.add(port(dir, -4, dir.getOpposite()));
        ports.add(port(side, 2, side));
        ports.add(port(side, -2, side.getOpposite()));
        ports.add(port(dir, 2, side, 2, side));
        ports.add(port(dir, 2, side, -2, side.getOpposite()));
        ports.add(port(dir, -2, side, 2, side));
        ports.add(port(dir, -2, side, -2, side.getOpposite()));
        return ports;
    }

    private static FluidPort port(Direction dir, int distance, Direction portDirection) {
        return FluidPort.of(dir.getStepX() * distance, 0, dir.getStepZ() * distance, portDirection);
    }

    private static FluidPort port(Direction dir, int forward, Direction side, int sideDistance,
            Direction portDirection) {
        return FluidPort.of(dir.getStepX() * forward + side.getStepX() * sideDistance, 0,
                dir.getStepZ() * forward + side.getStepZ() * sideDistance, portDirection);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.energyStored(energy.getPower(), energy.getMaxPower()),
                LegacyLookOverlayLines.tank(true, waterTank),
                LegacyLookOverlayLines.tank(false, slopTank),
                Component.literal("Progress: " + (int) (progress * 100.0F) + "%")));
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.machineOreSlopper", "Bedrock Ore Processor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new OreSlopperMenu(containerId, inventory, this);
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return VALID_UPGRADES;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
        tag.putLong("power", energy.getPower());
        tag.putFloat("progress", progress);
        waterTank.writeToNbt(tag, "water");
        slopTank.writeToNbt(tag, "slop");
        tag.putLong("consumption", consumption);
        tag.putBoolean("processing", processing);
        tag.putString("animation", animation.name());
        tag.putFloat("slider", slider);
        tag.putFloat("bucket", bucket);
        tag.putFloat("blades", blades);
        tag.putFloat("fan", fan);
        tag.putInt("delay", delay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        progress = tag.getFloat("progress");
        if (hasTankTag(tag, "water")) {
            waterTank.readFromNbt(tag, "water");
        }
        if (hasTankTag(tag, "slop")) {
            slopTank.readFromNbt(tag, "slop");
        }
        consumption = tag.contains("consumption") ? tag.getLong("consumption") : BASE_CONSUMPTION;
        processing = tag.getBoolean("processing");
        animation = parseAnimation(tag.getString("animation"));
        slider = tag.getFloat("slider");
        prevSlider = slider;
        bucket = tag.getFloat("bucket");
        prevBucket = bucket;
        blades = tag.getFloat("blades");
        prevBlades = blades;
        fan = tag.getFloat("fan");
        prevFan = fan;
        delay = tag.getInt("delay");
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        } else if (tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        }
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private static SlopperAnimation parseAnimation(String name) {
        try {
            return SlopperAnimation.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return SlopperAnimation.LOWERING;
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private static boolean isValidUpgrade(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemMachineUpgrade upgrade)) {
            return false;
        }
        return upgrade.getUpgradeType() == UpgradeType.SPEED || upgrade.getUpgradeType() == UpgradeType.EFFECT;
    }

    public enum SlopperAnimation {
        LOWERING,
        LIFTING,
        MOVE_SHREDDER,
        DUMPING,
        MOVE_BUCKET
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 7;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapSlot(slot);
            return mapped >= 0 ? items.getStackInSlot(mapped) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == 0 ? items.insertItem(SLOT_INPUT, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapSlot(slot);
            return mapped >= SLOT_OUTPUT_START && mapped <= SLOT_OUTPUT_END
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapSlot(slot);
            return mapped >= 0 ? items.getSlotLimit(mapped) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && items.isItemValid(SLOT_INPUT, stack);
        }

        private int mapSlot(int slot) {
            if (slot == 0) {
                return SLOT_INPUT;
            }
            int output = SLOT_OUTPUT_START + slot - 1;
            return output >= SLOT_OUTPUT_START && output <= SLOT_OUTPUT_END ? output : -1;
        }
    }
}
