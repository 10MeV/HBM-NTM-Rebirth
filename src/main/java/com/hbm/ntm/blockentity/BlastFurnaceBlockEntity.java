package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.BlastFurnaceBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.HbmFluidReleaseEffects;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.LegacyFluidTankPacket;
import com.hbm.ntm.fuel.LegacyBurnTimeModule;
import com.hbm.ntm.menu.BlastFurnaceMenu;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.recipe.BlastFurnaceRecipe;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlastFurnaceBlockEntity extends HbmFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver {
    private static final int FLOOR_COUNT = 2 * 2;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_OUTPUT_1 = 3;
    public static final int SLOT_OUTPUT_2 = 4;
    public static final int SLOT_COUNT = 5;

    public static final int FUEL_RATE = 800;
    // 1.7.10: FUEL_COAL (1,600) * 24; must also accept a 32,000-tick coal coke block.
    public static final int MAX_FUEL = 38_400;
    public static final int FLUE_GAS = 100;
    public static final int AIRBLAST_CAPACITY = 4_000;
    public static final int FLUE_CAPACITY = 1_000;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_NAME = "name";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_FUEL = "fuel";
    private static final String TAG_SYNC_PROGRESSING = "syncProgressing";
    private static final String TAG_SYNC_SPEED = "syncSpeed";
    private static final LegacyBurnTimeModule BURN_MODULE = new LegacyBurnTimeModule()
            .setWoodHeatMod(0D);

    private final HbmFluidTank airblastTank;
    private final HbmFluidTank flueTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_FUEL -> getBurnTime(stack) > 0;
                case SLOT_INPUT_1, SLOT_INPUT_2 -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler(items));

    private boolean progressing;
    private float progress;
    private float speed;
    private int fuel;
    private int tiltBlocksChecked;
    private int tiltBlocksValid;
    @Nullable
    private String customName;

    public BlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                new HbmFluidTank(HbmFluids.AIRBLAST, AIRBLAST_CAPACITY),
                new HbmFluidTank(HbmFluids.FLUE, FLUE_CAPACITY));
    }

    private BlastFurnaceBlockEntity(BlockPos pos, BlockState state, HbmFluidTank airblastTank,
            HbmFluidTank flueTank) {
        super(ModBlockEntities.BLAST_FURNACE.get(), pos, state, List.of(airblastTank, flueTank));
        this.airblastTank = airblastTank;
        this.flueTank = flueTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlastFurnaceBlockEntity furnace) {
        if (level.isClientSide) {
            return;
        }
        // 1.7.10 TileEntityMachineBlastFurnace#updateEntity checked the
        // standard 3x3 foundation before processing every server tick.
        boolean changed = furnace.checkTiltAgainstFoundation(level);
        changed = furnace.tickServer(level, pos, state) || changed;
        changed = furnace.syncTiltedBlockState(level, furnace.isTilted()) || changed;
        furnace.networkPackNT(100);
        if (changed) {
            furnace.setChanged();
            BlockState currentState = furnace.getBlockState();
            level.sendBlockUpdated(pos, currentState, currentState, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlastFurnaceBlockEntity furnace) {
        if (!level.isClientSide || !furnace.progressing
                || LegacyClientAnimationLod.shouldSkipAnimationUpdate(level, pos)) {
            return;
        }
        if (level.getGameTime() % 2L == 0L && level.getBlockState(pos.above(7)).isAir()) {
            level.addParticle(ParticleTypes.LAVA,
                    pos.getX() + 0.25D + level.random.nextDouble() * 0.5D,
                    pos.getY() + 7.25D,
                    pos.getZ() + 0.25D + level.random.nextDouble() * 0.5D,
                    0.0D, 0.0D, 0.0D);
            if (furnace.flueTank.getFill() >= FLUE_GAS) {
                ParticleUtil.spawnCoolingTower(level,
                        pos.getX() + 0.5D,
                        pos.getY() + 7.0D,
                        pos.getZ() + 0.5D,
                        10.0F, 0.25F, 2.5F,
                        100 + level.random.nextInt(20),
                        false, 0.075F, 0.25F, 0x202020);
            }
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        // 1.7.10 TileEntityMachineBlastFurnace#serialize.
        writeLegacyLoadedTileBinary(data);
        data.writeBoolean(progressing);
        data.writeFloat(progress);
        data.writeFloat(speed);
        data.writeInt(fuel);
        LegacyFluidTankPacket.write(data, airblastTank);
        LegacyFluidTankPacket.write(data, flueTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        // The top lava/flue visual is driven from this exact server-side working snapshot.
        readLegacyLoadedTileBinary(data);
        readClientRuntimeSnapshot(data.readBoolean(), data.readFloat(), data.readFloat(), data.readInt());
        LegacyFluidTankPacket.read(data, airblastTank);
        LegacyFluidTankPacket.read(data, flueTank);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putBoolean(TAG_SYNC_PROGRESSING, progressing);
        tag.putFloat(TAG_PROGRESS, progress);
        tag.putFloat(TAG_SYNC_SPEED, speed);
        tag.putInt(TAG_FUEL, fuel);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_SYNC_PROGRESSING) && tag.contains(TAG_PROGRESS)
                && tag.contains(TAG_SYNC_SPEED) && tag.contains(TAG_FUEL)) {
            readClientRuntimeSnapshot(tag.getBoolean(TAG_SYNC_PROGRESSING), tag.getFloat(TAG_PROGRESS),
                    tag.getFloat(TAG_SYNC_SPEED), tag.getInt(TAG_FUEL));
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getAirblastTank() {
        return airblastTank;
    }

    public HbmFluidTank getFlueTank() {
        return flueTank;
    }

    public boolean isProgressing() {
        return progressing;
    }

    public float getProgress() {
        return progress;
    }

    public int getProgressScaled() {
        return Math.round(progress * 10_000.0F);
    }

    public float getSpeed() {
        return speed;
    }

    public int getSpeedPercent() {
        return Math.round(speed * 100.0F);
    }

    public int getFuel() {
        return fuel;
    }

    public static LegacyBurnTimeModule burnModule() {
        return BURN_MODULE;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.blastFurnace", "Blast Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BlastFurnaceMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(airblastTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(flueTank);
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
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts(getBlockState());
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
    public long getProviderSpeed(FluidType type, int pressure) {
        return Math.max(flueTank.getFill() * 50L / Math.max(1, flueTank.getMaxFill()), 1L);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_NAME, customName);
        }
        tag.putFloat(TAG_PROGRESS, progress);
        tag.putInt(TAG_FUEL, fuel);
        airblastTank.writeToNbt(tag, "t0");
        flueTank.writeToNbt(tag, "t1");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_NAME, Tag.TAG_STRING) ? tag.getString(TAG_NAME) : null;
        progress = tag.getFloat(TAG_PROGRESS);
        fuel = tag.getInt(TAG_FUEL);
        if (hasTankTag(tag, "t0")) {
            airblastTank.readFromNbt(tag, "t0");
        }
        if (hasTankTag(tag, "t1")) {
            flueTank.readFromNbt(tag, "t1");
        }
    }

    private void readClientRuntimeSnapshot(boolean progressing, float progress, float speed, int fuel) {
        this.progressing = progressing;
        this.progress = Math.max(0.0F, progress);
        this.speed = Math.max(0.0F, speed);
        this.fuel = Math.max(0, fuel);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 7, 2));
    }

    private boolean tickServer(Level level, BlockPos pos, BlockState state) {
        int oldFuel = fuel;
        float oldProgress = progress;
        float oldSpeed = speed;
        boolean oldProgressing = progressing;
        int oldAirblast = airblastTank.getFill();
        int oldFlue = flueTank.getFill();

        refreshTrackedTransceiverFluidPorts(getReceivingTanks(), getSendingTanks(), this);
        if (flueTank.getFill() > 0) {
            tryProvideFluidToPorts(flueTank.getTankType(), flueTank.getPressure(), this);
        }

        loadFuel();
        speed = 0.0F;
        BlastFurnaceRecipe recipe = findRecipe(level);
        if (!isTilted() && recipe != null && fuel >= FUEL_RATE && canProcess(recipe)) {
            speed = Math.max(0.5F, Math.min(5.0F,
                    0.5F + airblastTank.getFill() * 8.0F / Math.max(1, airblastTank.getMaxFill())));
            progressing = true;
            progress += speed / recipe.duration();
            if (progress >= 1.0F) {
                process(recipe);
                progress = 0.0F;
                fuel -= FUEL_RATE;
                flueTank.setFill(flueTank.getFill() + FLUE_GAS);
                if (flueTank.getFill() > flueTank.getMaxFill()) {
                    int spill = flueTank.getFill() - flueTank.getMaxFill();
                    HbmFluidReleaseEffects.applyLegacyTraitRelease(level, pos.above(7), flueTank.getTankType(), spill,
                            FluidReleaseType.SPILL);
                    HbmFluidReleaseEffects.applyLegacyPollutingRelease(level, pos, flueTank.getTankType(),
                            FluidReleaseType.SPILL, spill);
                    flueTank.setFill(flueTank.getMaxFill());
                }
            }
            if (level.random.nextInt(10) == 0) {
                LegacySoundPlayer.playSoundEffect(level, pos.getX(), pos.getY(), pos.getZ(),
                        "minecraft:block.fire.ambient", 1.0F, 0.5F + level.random.nextFloat() * 0.25F);
            }
        } else {
            progressing = false;
            progress = 0.0F;
        }

        if (airblastTank.getFill() > 0) {
            airblastTank.setFill((int) (airblastTank.getFill() * 0.95D));
        }

        return oldFuel != fuel
                || oldProgress != progress
                || oldSpeed != speed
                || oldProgressing != progressing
                || oldAirblast != airblastTank.getFill()
                || oldFlue != flueTank.getFill();
    }

    private boolean syncTiltedBlockState(Level level, boolean tilted) {
        BlockState state = getBlockState();
        if (!state.hasProperty(BlastFurnaceBlock.TILTED)
                || state.getValue(BlastFurnaceBlock.TILTED) == tilted) {
            return false;
        }
        level.setBlock(worldPosition, state.setValue(BlastFurnaceBlock.TILTED, tilted), Block.UPDATE_CLIENTS);
        return true;
    }

    private boolean checkTiltAgainstFoundation(Level level) {
        if (!HbmCommonConfig.machineGravityEnabled() || getFloorCount() <= 0) {
            tiltBlocksChecked = 0;
            tiltBlocksValid = 0;
            return setTiltedState(level, false);
        }
        if ((level.getGameTime() + blockIdentity(worldPosition)) % 20L != 0L) {
            return false;
        }
        boolean changed = false;
        if (tiltBlocksChecked >= getFloorCount()) {
            changed = setTiltedState(level, tiltBlocksValid < tiltBlocksChecked * 0.95D);
            tiltBlocksChecked = 0;
            tiltBlocksValid = 0;
        }
        changed = syncTiltedBlockState(level, isTilted()) || changed;

        BlockPos floor = getFloorPosFromIndex(tiltBlocksChecked);
        tiltBlocksChecked++;
        if (isValidStandardFoundation(level, floor)) {
            tiltBlocksValid++;
        }
        return changed;
    }

    private boolean setTiltedState(Level level, boolean tilted) {
        if (isTilted() == tilted) {
            return syncTiltedBlockState(level, tilted);
        }
        if (tilted) {
            level.playSound(null, worldPosition, ModSounds.BLOCK_METAL_IMPACT.get(),
                    SoundSource.BLOCKS, 3.0F, 1.0F);
        }
        setTilted(tilted);
        setChanged();
        if (!syncTiltedBlockState(level, tilted)) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    /**
     * Preserves TileEntityMachineBlastFurnace's public 1.7.10 floor sampler.
     * The four samples are the corners at y-1 of the standard 3x3 footprint.
     */
    public int getFloorCount() {
        return FLOOR_COUNT;
    }

    public BlockPos getFloorPosFromIndex(int index) {
        if (index < 0 || index >= FLOOR_COUNT) {
            throw new IndexOutOfBoundsException("blast furnace floor sample " + index);
        }
        return new BlockPos(
                worldPosition.getX() - 1 + (index / 2) * 2,
                worldPosition.getY() - 1,
                worldPosition.getZ() - 1 + (index % 2) * 2);
    }

    private static int blockIdentity(BlockPos pos) {
        return (pos.getY() + pos.getZ() * 27_644_437) * 27_644_437 + pos.getX();
    }

    private static boolean isValidStandardFoundation(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP)
                && !state.is(BlockTags.SAND)
                && !state.is(ModBlocks.legacyBlock("dirt_dead").get())
                && !state.is(ModBlocks.legacyBlock("dirt_oily").get())
                && !state.is(ModBlocks.legacyBlock("stone_cracked").get());
    }

    @Nullable
    private BlastFurnaceRecipe findRecipe(Level level) {
        ItemStack first = items.getStackInSlot(SLOT_INPUT_1);
        ItemStack second = items.getStackInSlot(SLOT_INPUT_2);
        for (BlastFurnaceRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.BLAST_FURNACE.type().get())) {
            if (recipe.matches(first, second)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canProcess(BlastFurnaceRecipe recipe) {
        if (recipe.consumedCountForSlot(0, items.getStackInSlot(SLOT_INPUT_1),
                items.getStackInSlot(SLOT_INPUT_2)) <= 0) {
            return false;
        }
        List<ItemStack> outputs = recipe.outputs().stream()
                .map(HbmItemOutput::representativeStack)
                .toList();
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT_1, SLOT_OUTPUT_2, outputs);
    }

    private void process(BlastFurnaceRecipe recipe) {
        for (HbmItemOutput output : recipe.outputs()) {
            HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT_1, SLOT_OUTPUT_2,
                    output.representativeStack());
        }
        ItemStack first = items.getStackInSlot(SLOT_INPUT_1);
        ItemStack second = items.getStackInSlot(SLOT_INPUT_2);
        int consumeFirst = recipe.consumedCountForSlot(0, first, second);
        int consumeSecond = recipe.consumedCountForSlot(1, first, second);
        if (consumeFirst > 0) {
            items.extractItem(SLOT_INPUT_1, consumeFirst, false);
        }
        if (consumeSecond > 0) {
            items.extractItem(SLOT_INPUT_2, consumeSecond, false);
        }
    }

    private void loadFuel() {
        ItemStack stack = items.getStackInSlot(SLOT_FUEL);
        if (stack.isEmpty()) {
            return;
        }
        int capacity = MAX_FUEL - fuel;
        int burnValue = getBurnTime(stack);
        if (burnValue <= 0 || burnValue > capacity) {
            return;
        }
        fuel += burnValue;
        ItemStack remainder = stack.getCraftingRemainingItem();
        items.extractItem(SLOT_FUEL, 1, false);
        if (items.getStackInSlot(SLOT_FUEL).isEmpty() && !remainder.isEmpty()) {
            items.setStackInSlot(SLOT_FUEL, remainder.copy());
        }
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST) || tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
            return;
        }
        if (tag.contains(TAG_INVENTORY, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty() || stack.hasCraftingRemainingItem()) {
            return 0;
        }
        return BURN_MODULE.getBurnHeat(BURN_MODULE.getBurnTime(stack, 0D), stack);
    }

    private static List<FluidPort> fluidPorts(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        List<FluidPort> ports = new ArrayList<>();
        ports.add(FluidPort.of(2, 0, 0, Direction.EAST));
        ports.add(FluidPort.of(-2, 0, 0, Direction.WEST));
        ports.add(FluidPort.of(0, 0, 2, Direction.SOUTH));
        ports.add(FluidPort.of(facing.getStepX() * 2, 3, facing.getStepZ() * 2, facing));
        ports.add(FluidPort.of(facing.getStepX() * 2, 5, facing.getStepZ() * 2, facing));
        ports.add(FluidPort.of(0, 7, 0, Direction.UP));
        return ports;
    }

    private static final class AccessibleItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private AccessibleItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            if (mapped != SLOT_FUEL && mapped != SLOT_INPUT_1 && mapped != SLOT_INPUT_2) {
                return stack;
            }
            if ((mapped == SLOT_INPUT_1 && ItemStack.isSameItemSameTags(stack, items.getStackInSlot(SLOT_INPUT_2)))
                    || (mapped == SLOT_INPUT_2
                    && ItemStack.isSameItemSameTags(stack, items.getStackInSlot(SLOT_INPUT_1)))) {
                return stack;
            }
            return items.insertItem(mapped, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            return mapped == SLOT_OUTPUT_1 || mapped == SLOT_OUTPUT_2
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return mapped >= 0 && items.isItemValid(mapped, stack);
        }

        private int map(int slot) {
            return switch (slot) {
                case 0 -> SLOT_INPUT_1;
                case 1 -> SLOT_INPUT_2;
                case 2 -> SLOT_FUEL;
                case 3 -> SLOT_OUTPUT_1;
                case 4 -> SLOT_OUTPUT_2;
                default -> -1;
            };
        }
    }
}
