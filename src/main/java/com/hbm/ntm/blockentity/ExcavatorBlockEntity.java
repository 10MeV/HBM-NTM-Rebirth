package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyDepthBlock;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.item.BedrockOreBaseItem;
import com.hbm.ntm.item.DrillbitItem;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ExcavatorMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipeRuntime;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmBlockStateUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.LegacyUpgradeSlotSound;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExcavatorBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmLegacyControlReceiver, LegacyUpgradeInfoProvider {
    public static final long MAX_POWER = 1_000_000L;
    public static final long BASE_CONSUMPTION = 10_000L;
    public static final int TANK_CAPACITY = 16_000;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_FLUID_ID = 1;
    public static final int SLOT_UPGRADE_START = 2;
    public static final int SLOT_UPGRADE_END = 4;
    public static final int SLOT_DRILLBIT = 4;
    public static final int SLOT_OUTPUT_START = 5;
    public static final int SLOT_OUTPUT_END = 13;
    public static final int SLOT_COUNT = 14;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final String TAG_DRILL = "d";
    private static final String TAG_CRUSHER = "c";
    private static final String TAG_WALLING = "w";
    private static final String TAG_VEIN = "v";
    private static final String TAG_SILK = "s";
    private static final String TAG_TARGET_DEPTH = "t";
    private static final String TAG_TICKS_WORKED = "ticksWorked";
    private static final String TAG_CHUTE = "chuteTimer";
    private static final String TAG_OPERATIONAL = "operational";
    private static final TagKey<Block> FORGE_ORES =
            TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores"));
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.EFFECT, 3);

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            LegacyUpgradeSlotSound.playIfUpgrade(ExcavatorBlockEntity.this, slot, getStackInSlot(slot),
                    SLOT_UPGRADE_START, SLOT_UPGRADE_END, 1.5D, 1.0F, 1.0F);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
            }
            if (slot == SLOT_FLUID_ID) {
                return stack.getItem() instanceof IFluidIdentifierItem;
            }
            if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
                return stack.getItem() instanceof ItemMachineUpgrade
                        || slot == SLOT_DRILLBIT && stack.getItem() instanceof DrillbitItem;
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalItemHandler = LazyOptional.of(ExcavatorExternalItemHandler::new);

    private boolean enableDrill;
    private boolean enableCrusher;
    private boolean enableWalling;
    private boolean enableVeinMiner;
    private boolean enableSilkTouch;
    private boolean operational;
    private int ticksWorked;
    private int targetDepth;
    private boolean bedrockDrilling;
    private int speedLevel;
    private int powerLevel;
    private int radiusLevel;
    private long consumption = BASE_CONSUMPTION;
    private float drillRotation;
    private float previousDrillRotation;
    private float drillExtension;
    private float previousDrillExtension;
    private float crusherRotation;
    private float previousCrusherRotation;
    private int chuteTimer;
    @Nullable
    private String customName;

    public ExcavatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXCAVATOR.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY)));
        this.tank = getAllTanks().get(0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExcavatorBlockEntity excavator) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, excavator);
        boolean changed = excavator.tickServer(serverLevel);
        excavator.networkPackNT(150);
        if (changed) {
            excavator.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ExcavatorBlockEntity excavator) {
        excavator.tickClient();
    }

    private boolean tickServer(ServerLevel level) {
        boolean changed = updateUpgrades();
        long oldPower = energy.getPower();
        boolean oldOperational = operational;
        int oldDepth = targetDepth;
        int oldChute = chuteTimer;

        changed |= setFluidTankTypeFromIdentifierSlot(items, SLOT_FLUID_ID, tank);
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        changed |= oldPower != energy.getPower();

        if (level.getGameTime() % 20L == 0L) {
            changed |= tryEjectBuffer(level);
            refreshTrackedReceiverFluidPortsReport(getReceivingTanks(), this);
        }

        if (chuteTimer > 0) {
            chuteTimer--;
        }

        operational = false;
        DrillbitItem.Type drillType = getInstalledDrillType();
        if (enableDrill && drillType != null && energy.getPower() >= getPowerConsumption()) {
            operational = true;
            energy.setPower(energy.getPower() - getPowerConsumption());
            changed = true;

            int maxDepth = Math.max(0, worldPosition.getY() - level.getMinBuildHeight() - 4);
            if ((bedrockDrilling || targetDepth <= maxDepth) && tryDrill(level, 1 + radiusLevel * 2)) {
                targetDepth++;
                if (targetDepth > maxDepth) {
                    enableDrill = false;
                }
            }
        } else if (targetDepth != 0) {
            targetDepth = 0;
            bedrockDrilling = false;
            ticksWorked = 0;
        }

        return changed || oldOperational != operational || oldDepth != targetDepth || oldChute != chuteTimer;
    }

    private void tickClient() {
        previousDrillExtension = drillExtension;
        if (drillExtension != targetDepth) {
            float diff = Math.abs(drillExtension - targetDepth);
            float speed = Math.max(0.15F, diff / 10.0F);
            if (diff <= speed) {
                drillExtension = targetDepth;
            } else {
                drillExtension -= Math.signum(drillExtension - targetDepth) * speed;
            }
        }
        previousDrillRotation = drillRotation;
        previousCrusherRotation = crusherRotation;
        if (operational) {
            drillRotation += 15.0F;
            if (enableCrusher) {
                crusherRotation += 15.0F;
            }
        }
        if (drillRotation >= 360.0F) {
            drillRotation -= 360.0F;
            previousDrillRotation -= 360.0F;
        }
        if (crusherRotation >= 360.0F) {
            crusherRotation -= 360.0F;
            previousCrusherRotation -= 360.0F;
        }
    }

    private boolean tryDrill(ServerLevel level, int radius) {
        int y = drillY(level);
        if (targetDepth == 0 || y <= level.getMinBuildHeight()) {
            radius = 1;
        }

        for (int ring = 1; ring <= radius; ring++) {
            boolean ignoreAll = true;
            float combinedHardness = 0.0F;
            BlockPos bedrockOre = null;
            bedrockDrilling = false;

            for (int x = worldPosition.getX() - ring; x <= worldPosition.getX() + ring; x++) {
                for (int z = worldPosition.getZ() - ring; z <= worldPosition.getZ() + ring; z++) {
                    if (!isRingEdge(ring, x, z)) {
                        continue;
                    }
                    BlockPos target = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(target);
                    if (state.is(ModBlocks.ORE_BEDROCK.get()) || state.is(ModBlocks.ORE_BEDROCK_COLTAN.get())) {
                        combinedHardness = 5.0F * 60.0F * 20.0F;
                        bedrockOre = target;
                        bedrockDrilling = true;
                        enableCrusher = false;
                        ignoreAll = false;
                        break;
                    }
                    if (state.getBlock() instanceof LegacyDepthBlock) {
                        enableDrill = false;
                    }
                    if (shouldIgnoreBlock(level, target, state)) {
                        continue;
                    }
                    ignoreAll = false;
                    combinedHardness += Math.max(0.0F, state.getDestroySpeed(level, target));
                }
                if (bedrockOre != null) {
                    break;
                }
            }

            if (!ignoreAll) {
                ticksWorked++;
                double drillSpeed = effectiveDrillSpeed();
                int ticksToWork = (int) Math.ceil(combinedHardness / Math.max(0.01D, drillSpeed));
                if (ticksWorked >= ticksToWork) {
                    if (bedrockOre == null) {
                        breakBlocks(level, ring);
                        buildWall(level, ring + 1, ring == radius && enableWalling);
                        if (ring == radius) {
                            mineOuterOres(level, ring + 1);
                        }
                        tryCollect(level, radius + 1);
                    } else {
                        collectBedrock(level, bedrockOre);
                    }
                    ticksWorked = 0;
                }
                return false;
            }
            tryCollect(level, radius + 1);
        }

        buildWall(level, radius + 1, enableWalling);
        ticksWorked = 0;
        return true;
    }

    private void collectBedrock(ServerLevel level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof BedrockOreDepositBlockEntity ore)) {
            return;
        }
        DrillbitItem.Type type = getInstalledDrillType();
        if (type == null || ore.getTier() > type.tier()) {
            return;
        }
        FluidType required = ore.getRequiredFluid();
        int amount = ore.getRequiredFluidAmount();
        if (required != HbmFluids.NONE && amount > 0) {
            if (tank.getTankType() != required || tank.getFill() < amount) {
                return;
            }
            tank.setFill(tank.getFill() - amount);
            onFluidContentsChanged();
        }
        ItemStack stack = ore.getResource();
        if (stack.isEmpty()) {
            return;
        }
        if (stack.is(ModItems.BEDROCK_ORE_BASE.get())) {
            BedrockOreBaseItem.setOreAmount(stack, pos.getX(), pos.getZ(), 1.0D + type.fortune() * 0.1D);
        }
        if (supplyStacks(level, List.of(stack))) {
            chuteTimer = 40;
        }
    }

    private void breakBlocks(ServerLevel level, int ring) {
        int y = drillY(level);
        for (int x = worldPosition.getX() - ring; x <= worldPosition.getX() + ring; x++) {
            for (int z = worldPosition.getZ() - ring; z <= worldPosition.getZ() + ring; z++) {
                if (!isRingEdge(ring, x, z)) {
                    continue;
                }
                BlockPos target = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(target);
                if (!shouldIgnoreBlock(level, target, state)) {
                    tryMineAtLocation(level, target, state);
                }
            }
        }
    }

    private void tryMineAtLocation(ServerLevel level, BlockPos pos, BlockState state) {
        if (enableVeinMiner && canVeinMine() && isOre(state)) {
            Set<BlockPos> visited = new HashSet<>();
            Bounds bounds = new Bounds(pos);
            breakRecursively(level, pos, state.getBlock(), 10, visited, bounds);
            Vec3 collectPos = Vec3.atCenterOf(pos);
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, bounds.toAabb())) {
                item.setPos(collectPos.x, collectPos.y, collectPos.z);
            }
            return;
        }
        breakSingleBlock(level, pos, state);
    }

    private void breakRecursively(ServerLevel level, BlockPos pos, Block block, int depth, Set<BlockPos> visited,
            Bounds bounds) {
        if (depth < 0 || !visited.add(pos) || level.getBlockState(pos).getBlock() != block) {
            return;
        }
        for (Direction direction : Direction.values()) {
            breakRecursively(level, pos.relative(direction), block, depth - 1, visited, bounds);
        }
        breakSingleBlock(level, pos, level.getBlockState(pos));
        bounds.include(pos);
        if (enableWalling) {
            level.setBlock(pos, ModBlocks.BARRICADE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void breakSingleBlock(ServerLevel level, BlockPos pos, BlockState state) {
        List<ItemStack> drops = blockDrops(level, pos, state);
        if (enableCrusher) {
            drops = crushDrops(level, drops);
        }
        if (state.is(ModBlocks.BARRICADE.get())) {
            drops = List.of();
        }
        level.destroyBlock(pos, false);
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        drop.copy()));
            }
        }
    }

    private List<ItemStack> blockDrops(ServerLevel level, BlockPos pos, BlockState state) {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        int fortune = getFortuneLevel();
        if (canSilkTouch()) {
            tool.enchant(Enchantments.SILK_TOUCH, 1);
        } else if (fortune > 0) {
            tool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        }
        return Block.getDrops(state, level, pos, level.getBlockEntity(pos), null, tool);
    }

    private List<ItemStack> crushDrops(ServerLevel level, List<ItemStack> drops) {
        List<ItemStack> crushed = new ArrayList<>();
        for (ItemStack stack : drops) {
            ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level,
                    ItemProcessingRecipe.Machine.SHREDDER, stack);
            if (recipe == null) {
                crushed.add(stack);
                continue;
            }
            List<ItemStack> outputs = recipe.rollOutputStacks(level.random);
            if (outputs.isEmpty() || outputs.stream().allMatch(out -> out.is(ModItems.SCRAP.get()))) {
                crushed.add(stack);
                continue;
            }
            for (ItemStack output : outputs) {
                ItemStack copy = output.copy();
                copy.setCount(copy.getCount() * stack.getCount());
                crushed.add(copy);
            }
        }
        return crushed;
    }

    private void buildWall(ServerLevel level, int ring, boolean wallEverything) {
        int y = drillY(level);
        for (int x = worldPosition.getX() - ring; x <= worldPosition.getX() + ring; x++) {
            for (int z = worldPosition.getZ() - ring; z <= worldPosition.getZ() + ring; z++) {
                BlockPos target = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(target);
                if (x == worldPosition.getX() - ring || x == worldPosition.getX() + ring
                        || z == worldPosition.getZ() - ring || z == worldPosition.getZ() + ring) {
                    if ((state.canBeReplaced() || state.isAir()) && (wallEverything || isLiquid(state))) {
                        level.setBlock(target, ModBlocks.BARRICADE.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                } else if (isLiquid(state)) {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void mineOuterOres(ServerLevel level, int ring) {
        int y = drillY(level);
        for (int x = worldPosition.getX() - ring; x <= worldPosition.getX() + ring; x++) {
            for (int z = worldPosition.getZ() - ring; z <= worldPosition.getZ() + ring; z++) {
                if (!isRingEdge(ring, x, z)) {
                    continue;
                }
                BlockPos target = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(target);
                if (!shouldIgnoreBlock(level, target, state) && isOre(state)) {
                    tryMineAtLocation(level, target, state);
                }
            }
        }
    }

    private boolean tryEjectBuffer(ServerLevel level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        if (stacks.isEmpty() || !supplyExternal(level, stacks)) {
            return false;
        }
        compactOutputs(stacks);
        return true;
    }

    private void tryCollect(ServerLevel level, int radius) {
        int y = drillY(level);
        AABB box = new AABB(
                worldPosition.getX() - radius, y - 1, worldPosition.getZ() - radius,
                worldPosition.getX() + radius + 1, y + 2, worldPosition.getZ() + radius + 1);
        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, box);
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : entities) {
            if (entity.isAlive() && !entity.getItem().isEmpty()) {
                stacks.add(entity.getItem());
            }
        }
        supplyExternal(level, stacks);
        for (ItemEntity entity : entities) {
            if (!entity.isAlive() || entity.getItem().isEmpty()) {
                entity.discard();
                continue;
            }
            ItemStack remaining = insertIntoBuffer(entity.getItem().copy());
            if (remaining.isEmpty()) {
                entity.discard();
                chuteTimer = 40;
            } else if (remaining.getCount() != entity.getItem().getCount()) {
                entity.setItem(remaining);
                chuteTimer = 40;
            }
        }
    }

    private boolean supplyStacks(ServerLevel level, List<ItemStack> stacks) {
        boolean moved = supplyExternal(level, stacks);
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                ItemStack remaining = insertIntoBuffer(stack.copy());
                if (remaining.getCount() != stack.getCount()) {
                    stack.setCount(remaining.getCount());
                    moved = true;
                }
            }
        }
        return moved;
    }

    private boolean supplyExternal(ServerLevel level, List<ItemStack> stacks) {
        boolean moved = supplyContainer(level, stacks);
        moved |= supplyConveyor(level, stacks);
        return moved;
    }

    private boolean supplyContainer(ServerLevel level, List<ItemStack> stacks) {
        BlockPos out = outputPos();
        BlockEntity tile = level.getBlockEntity(out);
        if (tile == null) {
            return false;
        }
        Direction side = facing().getOpposite();
        boolean moved = false;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack before = stack.copy();
            ItemStack remaining = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                    .map(handler -> ItemHandlerHelper.insertItem(handler, stack.copy(), false))
                    .orElse(stack);
            stack.setCount(remaining.getCount());
            if (remaining.isEmpty() || remaining.getCount() != before.getCount()) {
                moved = true;
                chuteTimer = 40;
            }
        }
        return moved;
    }

    private boolean supplyConveyor(ServerLevel level, List<ItemStack> stacks) {
        BlockPos out = outputPos();
        BlockState state = level.getBlockState(out);
        if (!(state.getBlock() instanceof IConveyorBelt belt)) {
            return false;
        }
        boolean moved = false;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            Vec3 base = new Vec3(out.getX() + level.random.nextDouble(), out.getY() + 0.5D,
                    out.getZ() + level.random.nextDouble());
            Vec3 snap = belt.getClosestSnappingPosition(level, out, base);
            MovingItemEntity moving = new MovingItemEntity(level, stack.copy());
            moving.setPos(base.x, snap.y, base.z);
            level.addFreshEntity(moving);
            stack.setCount(0);
            moved = true;
            chuteTimer = 40;
        }
        return moved;
    }

    private ItemStack insertIntoBuffer(ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && !remaining.isEmpty(); slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (!existing.isEmpty() && ItemHandlerHelper.canItemStacksStack(existing, remaining)) {
                int add = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (add > 0) {
                    existing.grow(add);
                    remaining.shrink(add);
                    items.setStackInSlot(slot, existing);
                }
            }
        }
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && !remaining.isEmpty(); slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                int add = Math.min(remaining.getCount(), remaining.getMaxStackSize());
                ItemStack copy = remaining.copy();
                copy.setCount(add);
                items.setStackInSlot(slot, copy);
                remaining.shrink(add);
            }
        }
        return remaining;
    }

    private void compactOutputs(List<ItemStack> stacks) {
        stacks.removeIf(ItemStack::isEmpty);
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            int index = slot - SLOT_OUTPUT_START;
            items.setStackInSlot(slot, index < stacks.size() ? stacks.get(index).copy() : ItemStack.EMPTY);
        }
    }

    private boolean updateUpgrades() {
        int oldSpeed = speedLevel;
        int oldPower = powerLevel;
        int oldRadius = radiusLevel;
        LegacyMachineUpgradeManager.Levels levels =
                LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_START, SLOT_UPGRADE_END, VALID_UPGRADES);
        speedLevel = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        powerLevel = Math.min(levels.getLevel(UpgradeType.POWER), 3);
        radiusLevel = Math.min(levels.getLevel(UpgradeType.EFFECT), 3);
        consumption = BASE_CONSUMPTION * (1 + speedLevel) / (1 + powerLevel);
        return oldSpeed != speedLevel || oldPower != powerLevel || oldRadius != radiusLevel;
    }

    private int drillY(ServerLevel level) {
        return Math.max(level.getMinBuildHeight(), worldPosition.getY() - targetDepth - 4);
    }

    private double effectiveDrillSpeed() {
        DrillbitItem.Type type = getInstalledDrillType();
        return (type == null ? 1.0D : type.speed()) * (1.0D + speedLevel / 2.0D);
    }

    private static boolean isRingEdge(int ring, int x, int z, BlockPos center) {
        return ring == 1 || x == center.getX() - ring || x == center.getX() + ring
                || z == center.getZ() - ring || z == center.getZ() + ring;
    }

    private boolean isRingEdge(int ring, int x, int z) {
        return isRingEdge(ring, x, z, worldPosition);
    }

    private boolean shouldIgnoreBlock(ServerLevel level, BlockPos pos, BlockState state) {
        return state.isAir()
                || isLiquid(state)
                || state.is(Blocks.BEDROCK)
                || HbmBlockStateUtil.explosionResistance(state, level, pos) < 0.0F
                || state.getDestroySpeed(level, pos) < 0.0F;
    }

    private static boolean isLiquid(BlockState state) {
        return state.getBlock() instanceof LiquidBlock
                || state.getFluidState().getType() != Fluids.EMPTY
                || !state.getFluidState().isEmpty();
    }

    private static boolean isOre(BlockState state) {
        return state.is(FORGE_ORES);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public boolean isDrillEnabled() {
        return enableDrill;
    }

    public boolean isCrusherEnabled() {
        return enableCrusher;
    }

    public boolean isWallingEnabled() {
        return enableWalling;
    }

    public boolean isVeinMinerEnabled() {
        return enableVeinMiner;
    }

    public boolean isSilkTouchEnabled() {
        return enableSilkTouch;
    }

    public boolean isOperational() {
        return operational;
    }

    public int getTargetDepth() {
        return targetDepth;
    }

    public int getChuteTimer() {
        return chuteTimer;
    }

    public long getPowerConsumption() {
        return consumption;
    }

    public int getPowerBarHeight(int maxHeight) {
        return getMaxPower() <= 0L ? 0 : (int) (getPower() * maxHeight / getMaxPower());
    }

    public float getDrillRotation(float partialTick) {
        return previousDrillRotation + (drillRotation - previousDrillRotation) * partialTick;
    }

    public float getDrillExtension(float partialTick) {
        return previousDrillExtension + (drillExtension - previousDrillExtension) * partialTick;
    }

    public float getCrusherRotation(float partialTick) {
        return previousCrusherRotation + (crusherRotation - previousCrusherRotation) * partialTick;
    }

    @Nullable
    public DrillbitItem.Type getInstalledDrillType() {
        ItemStack stack = items.getStackInSlot(SLOT_DRILLBIT);
        return stack.getItem() instanceof DrillbitItem drillbit ? drillbit.type() : null;
    }

    public boolean canVeinMine() {
        DrillbitItem.Type type = getInstalledDrillType();
        return enableVeinMiner && type != null && type.vein();
    }

    public boolean canSilkTouch() {
        DrillbitItem.Type type = getInstalledDrillType();
        return enableSilkTouch && type != null && type.silk();
    }

    public int getFortuneLevel() {
        DrillbitItem.Type type = getInstalledDrillType();
        return type == null ? 0 : type.fortune();
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(tank);
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
    protected Iterable<FluidPort> getFluidPorts() {
        Direction dir = facing();
        Direction rot = dir.getCounterClockWise();
        return List.of(
                new FluidPort(offset(dir, 4).offset(offset(rot, 1)), dir),
                new FluidPort(offset(dir, 4).offset(offset(rot.getOpposite(), 1)), dir),
                new FluidPort(offset(rot, 4), rot),
                new FluidPort(offset(rot.getOpposite(), 4), rot.getOpposite()));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction dir = facing();
        Direction rot = dir.getCounterClockWise();
        return List.of(
                new EnergyPort(offset(dir, 4).offset(offset(rot, 1)), dir),
                new EnergyPort(offset(dir, 4).offset(offset(rot.getOpposite(), 1)), dir),
                new EnergyPort(offset(rot, 4), rot),
                new EnergyPort(offset(rot.getOpposite(), 4), rot.getOpposite()));
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == tank.getTankType();
    }

    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return tank;
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if (data.contains("drill")) {
            enableDrill = !enableDrill;
        }
        if (data.contains("crusher")) {
            enableCrusher = !enableCrusher;
        }
        if (data.contains("walling")) {
            enableWalling = !enableWalling;
        }
        if (data.contains("veinminer")) {
            enableVeinMiner = !enableVeinMiner;
        }
        if (data.contains("silktouch")) {
            enableSilkTouch = !enableSilkTouch;
        }
        setChanged();
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return VALID_UPGRADES;
    }

    @Override
    public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        return type == UpgradeType.SPEED || type == UpgradeType.POWER;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<Component> info, boolean extendedInfo) {
        info.add(Component.translatable("block.hbm_ntm_rebirth.machine_excavator"));
        if (type == UpgradeType.SPEED) {
            info.add(Component.translatable("tooltip.hbm_ntm_rebirth.upgrade.delay",
                    "-" + (100 - 200 / (level + 2)) + "%").withStyle(ChatFormatting.GREEN));
            info.add(Component.translatable("tooltip.hbm_ntm_rebirth.upgrade.consumption",
                    "+" + (level * 100) + "%").withStyle(ChatFormatting.RED));
        }
        if (type == UpgradeType.POWER) {
            info.add(Component.translatable("tooltip.hbm_ntm_rebirth.upgrade.consumption",
                    "-" + (100 - 100 / (level + 1)) + "%").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.machineExcavator", "Large Mining Drill");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ExcavatorMenu(containerId, inventory, this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        int minY = level == null ? worldPosition.getY() - 64 : level.getMinBuildHeight();
        return new AABB(worldPosition.getX() - 3, minY, worldPosition.getZ() - 3,
                worldPosition.getX() + 4, worldPosition.getY() + 5, worldPosition.getZ() + 4);
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
        tag.putBoolean(TAG_DRILL, enableDrill);
        tag.putBoolean(TAG_CRUSHER, enableCrusher);
        tag.putBoolean(TAG_WALLING, enableWalling);
        tag.putBoolean(TAG_VEIN, enableVeinMiner);
        tag.putBoolean(TAG_SILK, enableSilkTouch);
        tag.putBoolean(TAG_OPERATIONAL, operational);
        tag.putInt(TAG_TARGET_DEPTH, targetDepth);
        tag.putInt(TAG_TICKS_WORKED, ticksWorked);
        tag.putInt(TAG_CHUTE, chuteTimer);
        tag.putLong("p", getPower());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        }
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        enableDrill = tag.getBoolean(TAG_DRILL);
        enableCrusher = tag.getBoolean(TAG_CRUSHER);
        enableWalling = tag.getBoolean(TAG_WALLING);
        enableVeinMiner = tag.getBoolean(TAG_VEIN);
        enableSilkTouch = tag.getBoolean(TAG_SILK);
        operational = tag.getBoolean(TAG_OPERATIONAL);
        targetDepth = tag.getInt(TAG_TARGET_DEPTH);
        ticksWorked = tag.getInt(TAG_TICKS_WORKED);
        chuteTimer = tag.getInt(TAG_CHUTE);
        if (tag.contains("p")) {
            setPower(tag.getLong("p"));
        }
        updateUpgrades();
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private BlockPos outputPos() {
        Direction dir = facing();
        return worldPosition.relative(dir, 4).below(3);
    }

    private static BlockPos offset(Direction direction, int amount) {
        return new BlockPos(direction.getStepX() * amount, direction.getStepY() * amount,
                direction.getStepZ() * amount);
    }

    private static final class Bounds {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        private Bounds(BlockPos pos) {
            minX = maxX = pos.getX();
            minY = maxY = pos.getY();
            minZ = maxZ = pos.getZ();
        }

        private void include(BlockPos pos) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        private AABB toAabb() {
            return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        }
    }

    private final class ExcavatorExternalItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots() ? items.getStackInSlot(SLOT_OUTPUT_START + slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= 0 && slot < getSlots()
                    ? items.extractItem(SLOT_OUTPUT_START + slot, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
