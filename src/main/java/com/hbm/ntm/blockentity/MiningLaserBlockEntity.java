package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.MiningLaserMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiningLaserBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidSender, HbmLegacyButtonReceiver, LegacyLookOverlayProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_END = 8;
    public static final int SLOT_OUTPUT_START = 9;
    public static final int SLOT_OUTPUT_END = 29;
    public static final int SLOT_COUNT = 30;
    public static final int CONTROL_TOGGLE = 0;

    public static final long MAX_POWER = 100_000_000L;
    public static final int BASE_CONSUMPTION = 10_000;
    public static final int OIL_CAPACITY = 64_000;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_POWER = "power";
    private static final String TAG_IS_ON = "isOn";
    private static final String TAG_TARGET_X = "targetX";
    private static final String TAG_TARGET_Y = "targetY";
    private static final String TAG_TARGET_Z = "targetZ";
    private static final String TAG_LAST_TARGET_X = "lastTargetX";
    private static final String TAG_LAST_TARGET_Y = "lastTargetY";
    private static final String TAG_LAST_TARGET_Z = "lastTargetZ";
    private static final String TAG_BEAM = "beam";
    private static final String TAG_BREAK_PROGRESS = "breakProgress";
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 12,
            UpgradeType.POWER, 12,
            UpgradeType.EFFECT, 12,
            UpgradeType.OVERDRIVE, 9,
            UpgradeType.FORTUNE, 3,
            UpgradeType.SMELTER, 1,
            UpgradeType.NULLIFIER, 1,
            UpgradeType.SHREDDER, 1,
            UpgradeType.CENTRIFUGE, 1,
            UpgradeType.CRYSTALLIZER, 1);

    private final HbmFluidTank oilTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return HbmInventoryMenuHelper.isBatteryLike(stack);
            }
            if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
                return stack.getItem() instanceof ItemMachineUpgrade || stack.is(ModItems.UPGRADE_SCREM.get());
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(AccessibleItemHandler::new);

    private boolean isOn;
    private boolean redstonePowered;
    private int targetX;
    private int targetY;
    private int targetZ;
    private int lastTargetX;
    private int lastTargetY;
    private int lastTargetZ;
    private boolean beam;
    private double breakProgress;
    private double clientBreakProgress;
    private int speedLevel;
    private int powerLevel;
    private int effectLevel;
    private int overdriveLevel;
    private int fortuneLevel;
    private boolean smelterUpgrade;
    private boolean nullifierUpgrade;
    private boolean shredderUpgrade;
    private boolean centrifugeUpgrade;
    private boolean crystallizerUpgrade;

    public MiningLaserBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                new HbmFluidTank(HbmFluids.OIL, OIL_CAPACITY));
    }

    private MiningLaserBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank oilTank) {
        super(ModBlockEntities.MINING_LASER.get(), pos, state, energy, List.of(oilTank));
        this.oilTank = oilTank;
        this.oilTank.conform(new HbmFluidStack(HbmFluids.OIL, 0));
        this.targetX = pos.getX();
        this.targetY = pos.getY() - 2;
        this.targetZ = pos.getZ();
        this.lastTargetX = targetX;
        this.lastTargetY = targetY;
        this.lastTargetZ = targetZ;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MiningLaserBlockEntity laser) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, laser);
        long oldPower = laser.energy.getPower();
        int oldOil = laser.oilTank.getFill();
        boolean oldBeam = laser.beam;
        boolean oldOn = laser.isOn;
        int oldTargetY = laser.targetY;

        HbmEnergyUtil.chargeStorageFromItem(laser.items.getStackInSlot(SLOT_BATTERY),
                laser.energy, laser.energy.getReceiverSpeed());
        laser.oilTank.setTankType(HbmFluids.OIL);
        laser.tryProvideFluidToPorts(laser.oilTank.getTankType(), laser.oilTank.getPressure(), laser);

        if (laser.lastTargetX != laser.targetX || laser.lastTargetY != laser.targetY
                || laser.lastTargetZ != laser.targetZ) {
            laser.breakProgress = 0.0D;
        }
        laser.lastTargetX = laser.targetX;
        laser.lastTargetY = laser.targetY;
        laser.lastTargetZ = laser.targetZ;
        laser.redstonePowered = laser.isMultiblockRedstonePowered(level, pos);
        laser.updateUpgrades();

        if (laser.isOn && !laser.redstonePowered) {
            laser.runCycles((ServerLevel) level);
        } else {
            laser.targetY = pos.getY() - 2;
            laser.beam = false;
            laser.breakProgress = 0.0D;
        }

        laser.networkPackNT(250);
        boolean changed = oldPower != laser.energy.getPower()
                || oldOil != laser.oilTank.getFill()
                || oldBeam != laser.beam
                || oldOn != laser.isOn
                || oldTargetY != laser.targetY;
        if (changed || level.getGameTime() % 20L == 0L) {
            laser.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MiningLaserBlockEntity laser) {
        laser.clientBreakProgress = laser.breakProgress;
    }

    private void updateUpgrades() {
        var upgrades = LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_START, SLOT_UPGRADE_END,
                VALID_UPGRADES);
        speedLevel = upgrades.getLevel(UpgradeType.SPEED);
        powerLevel = upgrades.getLevel(UpgradeType.POWER);
        effectLevel = upgrades.getLevel(UpgradeType.EFFECT);
        overdriveLevel = upgrades.getLevel(UpgradeType.OVERDRIVE);
        fortuneLevel = upgrades.getLevel(UpgradeType.FORTUNE);
        smelterUpgrade = upgrades.getLevel(UpgradeType.SMELTER) > 0;
        nullifierUpgrade = upgrades.getLevel(UpgradeType.NULLIFIER) > 0;
        shredderUpgrade = upgrades.getLevel(UpgradeType.SHREDDER) > 0;
        centrifugeUpgrade = upgrades.getLevel(UpgradeType.CENTRIFUGE) > 0;
        crystallizerUpgrade = upgrades.getLevel(UpgradeType.CRYSTALLIZER) > 0;
    }

    private void runCycles(ServerLevel level) {
        int cycles = 1 + overdriveLevel;
        int speed = 1 + speedLevel;
        int fortune = fortuneLevel;
        int range = getRange();
        int consumption = getAdjustedConsumption();
        for (int i = 0; i < cycles; i++) {
            if (energy.getPower() < consumption) {
                beam = false;
                break;
            }
            energy.setPower(energy.getPower() - consumption);
            if (targetY <= level.getMinBuildHeight()) {
                targetY = worldPosition.getY() - 2;
            }
            scan(level, range);
            BlockPos target = new BlockPos(targetX, targetY, targetZ);
            BlockState targetState = level.getBlockState(target);
            if (isLiquid(targetState)) {
                level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                continue;
            }
            if (beam && canBreak(level, target, targetState)) {
                breakProgress += getBreakSpeed(level, target, targetState, speed);
                clientBreakProgress = Math.min(breakProgress, 1.0D);
                if (breakProgress < 1.0D) {
                    level.destroyBlockProgress(-1, target, (int) Math.floor(breakProgress * 10.0D));
                } else {
                    breakTargetBlock(level, target, targetState, fortune);
                }
            }
        }
    }

    private void scan(ServerLevel level, int range) {
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                BlockPos candidate = new BlockPos(worldPosition.getX() + x, targetY, worldPosition.getZ() + z);
                BlockState state = level.getBlockState(candidate);
                if (isLiquid(state)) {
                    continue;
                }
                if (canBreak(level, candidate, state)) {
                    targetX = candidate.getX();
                    targetZ = candidate.getZ();
                    beam = true;
                    return;
                }
            }
        }
        beam = false;
        targetY--;
    }

    private void breakTargetBlock(ServerLevel level, BlockPos target, BlockState state, int fortune) {
        List<ItemStack> drops = processBlockDrops(level, target, state, fortune);
        level.destroyBlock(target, false);
        for (ItemStack drop : drops) {
            Block.popResource(level, target, drop);
        }
        suckDrops(level, target);
        if (hasScremUpgrade()) {
            level.playSound(null, target, com.hbm.ntm.registry.ModSounds.BLOCK_SCREM.get(),
                    SoundSource.BLOCKS, 2000.0F, 1.0F);
        }
        level.destroyBlockProgress(-1, target, -1);
        breakProgress = 0.0D;
        clientBreakProgress = 0.0D;
    }

    private List<ItemStack> processBlockDrops(ServerLevel level, BlockPos target, BlockState state, int fortune) {
        ItemStack blockStack = new ItemStack(state.getBlock().asItem());
        if (!blockStack.isEmpty() && hasCrystallizerUpgrade()) {
            List<ItemStack> processed = processCrystallizerDrop(level, blockStack);
            if (processed != null) {
                return processed;
            }
        }
        if (!blockStack.isEmpty() && hasCentrifugeUpgrade()) {
            List<ItemStack> processed = processItemMachineDrop(level, ItemProcessingRecipe.Machine.CENTRIFUGE,
                    blockStack, false);
            if (processed != null) {
                return processed;
            }
        }
        if (!blockStack.isEmpty() && hasShredderUpgrade()) {
            List<ItemStack> processed = processItemMachineDrop(level, ItemProcessingRecipe.Machine.SHREDDER,
                    blockStack, true);
            if (processed != null) {
                return processed;
            }
        }
        if (!blockStack.isEmpty() && hasSmelterUpgrade()) {
            SimpleContainer container = new SimpleContainer(blockStack.copy());
            SmeltingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                    .orElse(null);
            if (recipe != null) {
                return List.of(recipe.getResultItem(level.registryAccess()).copy());
            }
        }
        ItemStack tool = ItemStack.EMPTY;
        if (fortune > 0) {
            tool = new ItemStack(Items.DIAMOND_PICKAXE);
            tool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        }
        return Block.getDrops(state, level, target, level.getBlockEntity(target), null, tool);
    }

    @Nullable
    private List<ItemStack> processCrystallizerDrop(ServerLevel level, ItemStack blockStack) {
        ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level, ItemProcessingRecipe.Machine.CRYSTALLIZER,
                blockStack, HbmFluids.PEROXIDE);
        if (recipe == null) {
            recipe = ItemProcessingRecipeRuntime.find(level, ItemProcessingRecipe.Machine.CRYSTALLIZER,
                    blockStack, HbmFluids.SULFURIC_ACID);
        }
        return recipe == null ? null : recipe.rollOutputStacks(level.random);
    }

    @Nullable
    private List<ItemStack> processItemMachineDrop(ServerLevel level, ItemProcessingRecipe.Machine machine,
            ItemStack blockStack, boolean ignoreScrapOutput) {
        ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level, machine, blockStack);
        if (recipe == null) {
            return null;
        }
        List<ItemStack> outputs = recipe.rollOutputStacks(level.random);
        if (ignoreScrapOutput && outputs.stream().allMatch(stack -> stack.is(ModItems.SCRAP.get()))) {
            return null;
        }
        return outputs;
    }

    private void suckDrops(ServerLevel level, BlockPos target) {
        AABB itemsBox = new AABB(target).inflate(3.0D, 1.0D, 3.0D);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, itemsBox)) {
            if (!entity.isAlive()) {
                continue;
            }
            ItemStack stack = entity.getItem();
            if (hasNullifierUpgrade() && isNullifiedDrop(stack)) {
                entity.discard();
                continue;
            }
            if (stack.isEmpty()) {
                entity.discard();
                continue;
            }
            if (isOilOreDrop(stack)) {
                oilTank.setTankType(HbmFluids.OIL);
                oilTank.setFill(Math.min(oilTank.getFill() + 500, oilTank.getMaxFill()));
                entity.discard();
                continue;
            }
            ItemStack remaining = insertIntoOutputs(stack.copy());
            if (remaining.isEmpty()) {
                entity.discard();
            } else if (remaining.getCount() != stack.getCount()) {
                entity.setItem(remaining);
            }
        }
        AABB mobBox = new AABB(target).inflate(1.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, mobBox)) {
            entity.setSecondsOnFire(5);
        }
    }

    private ItemStack insertIntoOutputs(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && !remaining.isEmpty(); slot++) {
            remaining = items.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    private boolean isMultiblockRedstonePowered(Level level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos proxy = pos.relative(direction, 2);
            BlockPos check = proxy.relative(direction.getOpposite());
            if (level.hasNeighborSignal(check)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canBreak(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.isAir()
                && !isLiquid(state)
                && !state.is(Blocks.BEDROCK)
                && state.getDestroySpeed(level, pos) >= 0.0F;
    }

    private static boolean isLiquid(BlockState state) {
        return state.getBlock() instanceof LiquidBlock
                || state.getFluidState().getType() != Fluids.EMPTY
                || !state.getFluidState().isEmpty();
    }

    private double getBreakSpeed(ServerLevel level, BlockPos target, BlockState state, int speed) {
        float hardness = state.getDestroySpeed(level, target) * 15.0F / speed;
        return hardness == 0.0F ? 1.0D : 1.0D / hardness;
    }

    public int getRange() {
        return Math.min(1 + effectLevel * 2, 25);
    }

    public int getWidth() {
        return 1 + getRange() * 2;
    }

    public int getAdjustedConsumption() {
        return BASE_CONSUMPTION
                - (BASE_CONSUMPTION * powerLevel / 16)
                + (BASE_CONSUMPTION * speedLevel / 16);
    }

    private boolean hasSmelterUpgrade() {
        return smelterUpgrade;
    }

    private boolean hasNullifierUpgrade() {
        return nullifierUpgrade;
    }

    private boolean hasShredderUpgrade() {
        return shredderUpgrade;
    }

    private boolean hasCentrifugeUpgrade() {
        return centrifugeUpgrade;
    }

    private boolean hasCrystallizerUpgrade() {
        return crystallizerUpgrade;
    }

    private boolean hasScremUpgrade() {
        for (int slot = SLOT_UPGRADE_START; slot <= SLOT_UPGRADE_END; slot++) {
            if (items.getStackInSlot(slot).is(ModItems.UPGRADE_SCREM.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNullifiedDrop(ItemStack stack) {
        return stack.is(Blocks.DIRT.asItem())
                || stack.is(Blocks.STONE.asItem())
                || stack.is(Blocks.COBBLESTONE.asItem())
                || stack.is(Blocks.SAND.asItem())
                || stack.is(Blocks.SANDSTONE.asItem())
                || stack.is(Blocks.GRAVEL.asItem())
                || stack.is(net.minecraft.world.item.Items.FLINT)
                || stack.is(net.minecraft.world.item.Items.SNOWBALL)
                || stack.is(net.minecraft.world.item.Items.WHEAT_SEEDS);
    }

    private static boolean isOilOreDrop(ItemStack stack) {
        RegistryObject<? extends Block> oilOre = ModBlocks.legacyBlock("ore_oil");
        return oilOre != null && oilOre.isPresent() && stack.is(oilOre.get().asItem());
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getOilTank() {
        return oilTank;
    }

    public long getPower() {
        return energy.getPower();
    }

    public long getMaxPower() {
        return energy.getMaxPower();
    }

    public boolean isOn() {
        return isOn;
    }

    public boolean isRedstonePowered() {
        return redstonePowered;
    }

    public boolean hasBeam() {
        return beam;
    }

    public double getBreakProgress() {
        return clientBreakProgress;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public int getTargetZ() {
        return targetZ;
    }

    public int getLastTargetX() {
        return lastTargetX;
    }

    public int getLastTargetY() {
        return lastTargetY;
    }

    public int getLastTargetZ() {
        return lastTargetZ;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(oilTank);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(EnergyPort.of(0, 2, 0, Direction.UP));
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return List.of(
                FluidPort.of(2, 0, 0, Direction.EAST),
                FluidPort.of(-2, 0, 0, Direction.WEST),
                FluidPort.of(0, 0, 2, Direction.SOUTH),
                FluidPort.of(0, 0, -2, Direction.NORTH));
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.OUTPUT;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.energyStored(energy.getPower(), energy.getMaxPower()),
                LegacyLookOverlayLines.tank(false, oilTank),
                Component.literal(isOn ? "Online" : "Offline"),
                Component.literal("Width: " + getWidth())));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.miningLaser", "Mining Laser");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MiningLaserMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_TOGGLE
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_TOGGLE) {
            isOn = !isOn;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putBoolean(TAG_IS_ON, isOn);
        tag.putInt(TAG_TARGET_X, targetX);
        tag.putInt(TAG_TARGET_Y, targetY);
        tag.putInt(TAG_TARGET_Z, targetZ);
        tag.putInt(TAG_LAST_TARGET_X, lastTargetX);
        tag.putInt(TAG_LAST_TARGET_Y, lastTargetY);
        tag.putInt(TAG_LAST_TARGET_Z, lastTargetZ);
        tag.putBoolean(TAG_BEAM, beam);
        tag.putDouble(TAG_BREAK_PROGRESS, breakProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        isOn = tag.getBoolean(TAG_IS_ON);
        targetX = tag.contains(TAG_TARGET_X) ? tag.getInt(TAG_TARGET_X) : worldPosition.getX();
        targetY = tag.contains(TAG_TARGET_Y) ? tag.getInt(TAG_TARGET_Y) : worldPosition.getY() - 2;
        targetZ = tag.contains(TAG_TARGET_Z) ? tag.getInt(TAG_TARGET_Z) : worldPosition.getZ();
        lastTargetX = tag.contains(TAG_LAST_TARGET_X) ? tag.getInt(TAG_LAST_TARGET_X) : targetX;
        lastTargetY = tag.contains(TAG_LAST_TARGET_Y) ? tag.getInt(TAG_LAST_TARGET_Y) : targetY;
        lastTargetZ = tag.contains(TAG_LAST_TARGET_Z) ? tag.getInt(TAG_LAST_TARGET_Z) : targetZ;
        beam = tag.getBoolean(TAG_BEAM);
        breakProgress = tag.getDouble(TAG_BREAK_PROGRESS);
        clientBreakProgress = breakProgress;
        updateUpgrades();
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
            return 21;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.extractItem(mapped, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        private int map(int slot) {
            return slot >= 0 && slot < 21 ? SLOT_OUTPUT_START + slot : -1;
        }
    }
}
