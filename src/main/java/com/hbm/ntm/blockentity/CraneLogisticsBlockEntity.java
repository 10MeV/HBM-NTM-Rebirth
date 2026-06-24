package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.hbm.ntm.menu.CraneLogisticsMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.network.HbmLegacyBufPacketReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraneLogisticsBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyControlReceiver,
        HbmLegacyBufPacketReceiver {
    private static final String TAG_ITEMS = "items";
    private static final String TAG_INPUT_SIDE = "inputSide";
    private static final String TAG_OUTPUT_OVERRIDE = "CraneOutputOverride";
    private static final String TAG_DESTROYER = "destroyer";
    private static final String TAG_WHITELIST = "isWhitelist";
    private static final String TAG_MAX_EJECT = "maxEject";
    private static final String TAG_LAST_GRABBED = "lastGrabbedTick";
    private static final String TAG_MODE = "mode";
    private static final String TAG_LAST_REDSTONE = "lastRedstone";
    private static final String TAG_ROUTER_MODES = "modes";
    private static final String TAG_PATTERN = "patternModes";
    private static final int NO_OVERRIDE = -1;

    private final Kind kind;
    private final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemCapability;
    private Direction inputSide;
    private Direction outputOverride;
    private boolean destroyer = true;
    private boolean whitelist;
    private boolean maxEject;
    private long lastGrabbedTick;
    private byte mode;
    private boolean lastRedstone;
    private int[] routerModes = new int[6];
    private byte[] patternModes;

    public CraneLogisticsBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, Kind.fromBlock(state));
    }

    public CraneLogisticsBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        super(ModBlockEntities.CRANE_LOGISTICS.get(), pos, state);
        this.kind = kind;
        this.items = new ItemStackHandler(kind.slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChangedAndUpdate();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return CraneLogisticsBlockEntity.this.isItemValid(slot, stack);
            }
        };
        this.itemCapability = LazyOptional.of(() -> items);
        this.inputSide = defaultInput(state);
        this.outputOverride = null;
        this.patternModes = new byte[Math.max(kind.filterSlots, 1)];
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CraneLogisticsBlockEntity crane) {
        if (level.isClientSide) {
            return;
        }
        switch (crane.kind) {
            case INSERTER -> crane.tickInserter(level);
            case EXTRACTOR -> crane.tickExtractor(level);
            case GRABBER -> crane.tickGrabber(level);
            case BOXER -> crane.tickBoxer(level);
            case UNBOXER -> crane.tickUnboxer(level);
            case PARTITIONER -> crane.tickPartitioner(level);
            default -> {
            }
        }
    }

    public Kind kind() {
        return kind;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public Direction getInputSide() {
        return inputSide == null ? Direction.NORTH : inputSide;
    }

    public Direction getOutputSide() {
        return outputOverride == null ? getInputSide().getOpposite() : outputOverride;
    }

    public int getOutputOverrideOrdinal() {
        return outputOverride == null ? NO_OVERRIDE : outputOverride.get3DDataValue();
    }

    public boolean isDestroyer() {
        return destroyer;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean isMaxEject() {
        return maxEject;
    }

    public int getMode() {
        return mode;
    }

    public int getRouterMode(int side) {
        return side >= 0 && side < routerModes.length ? routerModes[side] : 0;
    }

    public int getPatternMode(int slot) {
        return slot >= 0 && slot < patternModes.length ? patternModes[slot] : 0;
    }

    public void cyclePatternMode(int slot) {
        if (slot < 0 || slot >= patternModes.length) {
            return;
        }
        patternModes[slot] = (byte) ((patternModes[slot] + 1) % 4);
        setChangedAndUpdate();
    }

    public void setPatternStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= patternModes.length || slot >= items.getSlots()) {
            return;
        }
        items.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : HbmItemStackUtil.carefulCopyWithSize(stack, 1));
        setChangedAndUpdate();
    }

    public void setInput(Direction direction) {
        if (direction == null) {
            return;
        }
        Direction oldSide = getInputSide();
        Direction target = direction == getOutputSide() ? direction.getOpposite() : direction;
        boolean needSwapOutput = target == getOutputSide();
        inputSide = target;
        if (needSwapOutput) {
            setOutputOverride(oldSide);
        } else {
            setChangedAndUpdate();
        }
    }

    public void setOutputOverride(Direction direction) {
        if (direction == null) {
            return;
        }
        Direction oldSide = getOutputSide();
        Direction target = oldSide == direction ? direction.getOpposite() : direction;
        outputOverride = target == getInputSide().getOpposite() ? null : target;
        if (target == getInputSide()) {
            setInput(oldSide);
        } else {
            setChangedAndUpdate();
        }
    }

    public boolean canItemEnter(Direction side) {
        return switch (kind) {
            case INSERTER, BOXER -> getInputSide() == side;
            case UNBOXER -> false;
            case ROUTER -> true;
            case PARTITIONER -> partitionerTravelDirection() == side;
            default -> false;
        };
    }

    public boolean canPackageEnter(Direction side) {
        return switch (kind) {
            case INSERTER, BOXER -> getInputSide() == side;
            case ROUTER -> true;
            case UNBOXER -> getOutputSide() == side;
            default -> false;
        };
    }

    public void onItemEnter(Direction side, ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) {
            return;
        }
        switch (kind) {
            case INSERTER -> insertEnteringItem(stack);
            case BOXER -> addOrDrop(stack, 0, 20);
            case ROUTER -> routeStacks(List.of(stack.copy()), false);
            case PARTITIONER -> partitionerAccept(stack);
            default -> {
            }
        }
    }

    public void onPackageEnter(Direction side, ItemStack[] stacks) {
        if (level == null || level.isClientSide || stacks == null) {
            return;
        }
        switch (kind) {
            case INSERTER -> {
                for (ItemStack stack : stacks) {
                    insertEnteringItem(stack);
                }
            }
            case BOXER -> {
                Direction accessedSide = getOutputSide().getOpposite();
                for (ItemStack stack : stacks) {
                    addOrDrop(stack, 0, 20, accessedSide);
                }
            }
            case UNBOXER -> {
                Direction accessedSide = getOutputSide().getOpposite();
                for (ItemStack stack : stacks) {
                    addOrDrop(stack, 0, 20, accessedSide);
                }
            }
            case ROUTER -> routeStacks(Arrays.stream(stacks).map(ItemStack::copy).toList(), true);
            default -> {
            }
        }
    }

    public Vec3 closestSnappingPosition(BlockPos pos, Vec3 itemPos) {
        Direction dir = kind == Kind.PARTITIONER ? partitionerTravelDirection() : getInputSide();
        return com.hbm.ntm.api.conveyor.ConveyorMath.closestSnappingPosition(pos, itemPos, dir);
    }

    private void tickInserter(Level level) {
        if (level.hasNeighborSignal(worldPosition)) {
            syncChanged(15);
            return;
        }
        BlockEntity target = level.getBlockEntity(worldPosition.relative(getOutputSide()));
        if (target == null) {
            syncChanged(15);
            return;
        }
        Direction side = getOutputSide().getOpposite();
        boolean didSomething = false;
        for (int i = 0; i < 21; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remainder = insertInto(target, stack.copy(), side);
            if (remainder.getCount() != stack.getCount()) {
                items.setStackInSlot(i, remainder);
                didSomething = true;
                break;
            }
        }
        if (!didSomething) {
            for (int i = 0; i < 21; i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack single = HbmItemStackUtil.carefulCopyWithSize(stack, 1);
                if (insertInto(target, single, side).isEmpty()) {
                    items.extractItem(i, 1, false);
                    break;
                }
            }
        }
        syncChanged(15);
    }

    private void tickExtractor(Level level) {
        if (level.getGameTime() % 20L != 0L || level.hasNeighborSignal(worldPosition)) {
            syncChanged(15);
            return;
        }
        Direction input = getOutputSide();
        Direction output = getInputSide();
        BlockEntity source = level.getBlockEntity(worldPosition.relative(input));
        if (source == null) {
            syncChanged(15);
            return;
        }
        IConveyorBelt belt = beltAt(worldPosition.relative(output));
        for (int slot = 0; slot < slotCount(source, input.getOpposite()); slot++) {
            ItemStack available = extractFrom(source, slot, 1, input.getOpposite(), true);
            if (available.isEmpty() || !filterAllows(available, 0, 9, whitelist)) {
                continue;
            }
            ItemStack extracted = extractFrom(source, slot, 1, input.getOpposite(), false);
            if (extracted.isEmpty()) {
                continue;
            }
            if (belt != null) {
                spawnMovingItem(worldPosition.relative(output), extracted);
            } else {
                ItemStack remainder = addToHandlerRange(extracted, 9, 17);
                if (!remainder.isEmpty()) {
                    HbmInventoryUtil.dropStack(level, worldPosition, remainder);
                }
            }
            break;
        }
        syncChanged(15);
    }

    private void tickGrabber(Level level) {
        if (level.getGameTime() < lastGrabbedTick + 20L || level.hasNeighborSignal(worldPosition)) {
            syncChanged(15);
            return;
        }
        Direction input = getInputSide();
        Direction output = getOutputSide();
        AABB box = grabBox(input);
        List<MovingItemEntity> movingItems = level.getEntitiesOfClass(MovingItemEntity.class, box,
                entity -> entity.isAlive() && !entity.getItemStack().isEmpty());
        if (movingItems.isEmpty()) {
            syncChanged(15);
            return;
        }
        IConveyorBelt belt = beltAt(worldPosition.relative(output));
        BlockEntity target = belt == null ? level.getBlockEntity(worldPosition.relative(output)) : null;
        for (MovingItemEntity moving : movingItems) {
            ItemStack stack = moving.getItemStack();
            if (!filterAllows(stack, 0, 9, whitelist)) {
                continue;
            }
            lastGrabbedTick = level.getGameTime();
            if (belt != null) {
                spawnMovingItem(worldPosition.relative(output), stack.copy());
                moving.discard();
            } else if (target != null) {
                ItemStack toAdd = HbmItemStackUtil.carefulCopyWithSize(stack, Math.min(1, stack.getCount()));
                ItemStack remainder = insertInto(target, toAdd, output.getOpposite());
                int added = toAdd.getCount() - remainder.getCount();
                if (added > 0) {
                    stack.shrink(added);
                    moving.setItemStack(stack);
                    if (stack.isEmpty()) {
                        moving.discard();
                    }
                }
            }
            break;
        }
        syncChanged(15);
    }

    private void tickBoxer(Level level) {
        boolean redstone = level.hasNeighborSignal(worldPosition);
        if (mode == 3 && redstone && !lastRedstone) {
            packAllNonEmpty();
        }
        lastRedstone = redstone;
        if (mode != 3 && level.getGameTime() % 2L == 0L) {
            int pack = switch (mode) {
                case 1 -> 8;
                case 2 -> 16;
                default -> 4;
            };
            int fullStacks = 0;
            for (int i = 0; i < 21; i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getCount() == stack.getMaxStackSize()) {
                    fullStacks++;
                }
            }
            if (fullStacks >= pack) {
                ItemStack[] box = new ItemStack[pack];
                for (int i = 0; i < 21 && pack > 0; i++) {
                    ItemStack stack = items.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getCount() == stack.getMaxStackSize()) {
                        box[--pack] = stack.copy();
                        items.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
                spawnMovingPackage(worldPosition.relative(getOutputSide()), box);
            }
        }
        syncChanged(15);
    }

    private void tickUnboxer(Level level) {
        if (level.getGameTime() % 20L != 0L || level.hasNeighborSignal(worldPosition)) {
            return;
        }
        Direction output = getInputSide();
        if (beltAt(worldPosition.relative(output)) == null) {
            return;
        }
        for (int i = 0; i < 21; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack toSend = HbmItemStackUtil.carefulCopyWithSize(stack, 1);
            items.extractItem(i, 1, false);
            spawnMovingItem(worldPosition.relative(output), toSend);
            break;
        }
    }

    private void tickPartitioner(Level level) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                slots.add(i);
            }
        }
        slots.sort((a, b) -> Integer.compare(items.getStackInSlot(a).getCount(), items.getStackInSlot(b).getCount()));
        for (int slot : slots) {
            ItemStack stack = items.getStackInSlot(slot);
            int amount = partitionerAmount(stack);
            if (amount <= 0) {
                amount = stack.getCount();
            }
            while (stack.getCount() >= amount) {
                ItemStack entityStack = HbmItemStackUtil.carefulCopyWithSize(stack, amount);
                stack.shrink(amount);
                spawnMovingItem(worldPosition, entityStack);
            }
            if (stack.isEmpty()) {
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private void packAllNonEmpty() {
        if (level == null || beltAt(worldPosition.relative(getOutputSide())) == null) {
            return;
        }
        List<ItemStack> box = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                box.add(stack.copy());
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        if (!box.isEmpty()) {
            spawnMovingPackage(worldPosition.relative(getOutputSide()), box.toArray(new ItemStack[0]));
        }
    }

    private void insertEnteringItem(ItemStack stack) {
        ItemStack toAdd = stack.copy();
        if (level == null) {
            return;
        }
        if (!level.hasNeighborSignal(worldPosition)) {
            BlockEntity target = level.getBlockEntity(worldPosition.relative(getOutputSide()));
            if (target != null) {
                toAdd = insertInto(target, toAdd, getOutputSide().getOpposite());
            }
        }
        if (!toAdd.isEmpty()) {
            toAdd = addToHandlerRange(toAdd, 0, 20);
        }
        if (!toAdd.isEmpty() && !destroyer) {
            HbmInventoryUtil.dropStack(level, Vec3.atCenterOf(worldPosition).x,
                    Vec3.atCenterOf(worldPosition).y, Vec3.atCenterOf(worldPosition).z, toAdd.copy());
        }
    }

    private void routeStacks(List<ItemStack> stacks, boolean packageMode) {
        if (level == null) {
            return;
        }
        List<ItemStack>[] sorted = new List[7];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = new ArrayList<>();
        }
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            Direction direction = routerOutput(stack);
            sorted[direction == null ? 6 : direction.get3DDataValue()].add(stack.copy());
        }
        for (int i = 0; i < sorted.length; i++) {
            if (sorted[i].isEmpty()) {
                continue;
            }
            Direction direction = i == 6 ? null : Direction.from3DDataValue(i);
            if (direction == null) {
                for (ItemStack stack : sorted[i]) {
                    HbmInventoryUtil.dropStack(level, worldPosition, stack);
                }
            } else if (packageMode) {
                spawnMovingPackage(worldPosition.relative(direction), sorted[i].toArray(new ItemStack[0]));
            } else {
                for (ItemStack stack : sorted[i]) {
                    spawnMovingItem(worldPosition.relative(direction), stack);
                }
            }
        }
    }

    @Nullable
    private Direction routerOutput(ItemStack stack) {
        List<Direction> valid = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            int side = direction.get3DDataValue();
            int routeMode = routerModes[side];
            if (routeMode == 0 || routeMode == 3) {
                continue;
            }
            boolean matches = matchesAnyFilter(stack, side * 5, side * 5 + 4);
            if ((routeMode == 1 && matches) || (routeMode == 2 && !matches)) {
                valid.add(direction);
            }
        }
        if (valid.isEmpty()) {
            for (Direction direction : Direction.values()) {
                if (routerModes[direction.get3DDataValue()] == 3) {
                    valid.add(direction);
                }
            }
        }
        return valid.isEmpty() || level == null ? null : valid.get(level.random.nextInt(valid.size()));
    }

    private boolean filterAllows(ItemStack stack, int start, int end, boolean whitelistMode) {
        boolean matches = matchesAnyFilter(stack, start, end - 1);
        return whitelistMode == matches;
    }

    private boolean matchesAnyFilter(ItemStack stack, int start, int endInclusive) {
        for (int i = start; i <= endInclusive && i < items.getSlots(); i++) {
            ItemStack filter = items.getStackInSlot(i);
            if (!filter.isEmpty() && filterMatches(filter, stack, i)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterMatches(ItemStack filter, ItemStack stack, int slot) {
        if (filter.isEmpty() || stack.isEmpty()) {
            return false;
        }
        int modeIndex = kind == Kind.ROUTER ? slot : slot - kind.filterStart;
        int patternMode = getPatternMode(modeIndex);
        return switch (patternMode) {
            case 1 -> stack.is(filter.getItem());
            case 2 -> ItemStack.isSameItemSameTags(stack, filter);
            case 3 -> stack.getItem() == filter.getItem() && stack.getDamageValue() == filter.getDamageValue();
            default -> ItemStack.isSameItemSameTags(stack, filter);
        };
    }

    private void partitionerAccept(ItemStack stack) {
        int amount = partitionerAmount(stack);
        ItemStack remainder = amount > 0 ? addToHandlerRange(stack, 0, 44) : addToHandlerRange(stack, 45, 89);
        if (!remainder.isEmpty() && level != null) {
            HbmInventoryUtil.dropStack(level, worldPosition, remainder);
        }
    }

    private int partitionerAmount(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return 0;
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.CRYSTALLIZER.type().get()).stream()
                .filter(recipe -> recipe.matches(stack))
                .mapToInt(recipe -> Math.max(1, recipe.input().count()))
                .findFirst()
                .orElse(0);
    }

    private ItemStack addToHandlerRange(ItemStack stack, int start, int end) {
        ItemStack remainder = HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, start, end, stack);
        setChangedAndUpdate();
        return remainder;
    }

    private void addOrDrop(ItemStack stack, int start, int end) {
        addOrDrop(stack, start, end, Direction.DOWN);
    }

    private void addOrDrop(ItemStack stack, int start, int end, Direction side) {
        ItemStack remainder = addToHandlerRange(stack, start, end);
        if (!remainder.isEmpty() && level != null) {
            HbmInventoryUtil.dropStack(level, worldPosition, remainder);
        }
    }

    private IConveyorBelt beltAt(BlockPos pos) {
        if (level == null) {
            return null;
        }
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof IConveyorBelt belt ? belt : null;
    }

    private void spawnMovingItem(BlockPos pos, ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return;
        }
        MovingItemEntity moving = new MovingItemEntity(level, stack);
        Vec3 snap = snapFor(pos);
        moving.moveTo(snap.x, snap.y, snap.z, 0.0F, 0.0F);
        level.addFreshEntity(moving);
    }

    private void spawnMovingPackage(BlockPos pos, ItemStack[] stacks) {
        if (level == null || stacks == null || stacks.length == 0 || beltAt(pos) == null) {
            return;
        }
        MovingPackageEntity moving = new MovingPackageEntity(level, stacks);
        Vec3 snap = snapFor(pos);
        moving.moveTo(snap.x, snap.y, snap.z, 0.0F, 0.0F);
        level.addFreshEntity(moving);
    }

    private Vec3 snapFor(BlockPos pos) {
        IConveyorBelt belt = beltAt(pos);
        Vec3 center = Vec3.atCenterOf(pos);
        return belt == null ? center : belt.getClosestSnappingPosition(level, pos, center);
    }

    private ItemStack insertInto(BlockEntity target, ItemStack stack, Direction side) {
        if (target == null || stack.isEmpty()) {
            return stack;
        }
        LazyOptional<IItemHandler> capability = target.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
        IItemHandler handler = capability.orElse(null);
        if (handler == null) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(handler, stack, false);
    }

    private int slotCount(BlockEntity source, Direction side) {
        IItemHandler handler = source.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        return handler == null ? 0 : handler.getSlots();
    }

    private ItemStack extractFrom(BlockEntity source, int slot, int amount, Direction side, boolean simulate) {
        IItemHandler handler = source.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        return handler == null ? ItemStack.EMPTY : handler.extractItem(slot, amount, simulate);
    }

    private AABB grabBox(Direction input) {
        double reach = 1.0D;
        BlockPos target = worldPosition.relative(input);
        if (level != null) {
            Block block = level.getBlockState(target).getBlock();
            if (block == ModBlocks.CONVEYOR_DOUBLE.get()) {
                reach = 0.5D;
            } else if (block == ModBlocks.CONVEYOR_TRIPLE.get()) {
                reach = 0.33D;
            }
        }
        double x = worldPosition.getX() + input.getStepX() * reach;
        double y = worldPosition.getY() + input.getStepY() * reach;
        double z = worldPosition.getZ() + input.getStepZ() * reach;
        return new AABB(x + 0.1875D, y + 0.1875D, z + 0.1875D,
                x + 0.8125D, y + 0.8125D, z + 0.8125D);
    }

    private Direction partitionerTravelDirection() {
        BlockState state = getBlockState();
        return state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private boolean isItemValid(int slot, ItemStack stack) {
        return switch (kind) {
            case EXTRACTOR -> slot >= 9 && slot < 18;
            case PARTITIONER -> slot < 45 && partitionerAmount(stack) > 0;
            default -> true;
        };
    }

    private void syncChanged(int range) {
        setChanged();
        networkPackNT(range);
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("destroyer")) {
            destroyer = !destroyer;
        }
        if (data.contains("whitelist")) {
            whitelist = !whitelist;
        }
        if (data.contains("maxEject")) {
            maxEject = !maxEject;
        }
        if (data.contains("toggle")) {
            int index = data.getInt("toggle");
            if (kind == Kind.ROUTER && index >= 0 && index < routerModes.length) {
                routerModes[index] = (routerModes[index] + 1) % 4;
            } else if (kind == Kind.BOXER) {
                mode = (byte) ((mode + 1) % 4);
            }
        }
        setChangedAndUpdate();
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) < 400.0D;
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return hasPermission(player);
    }

    @Override
    public void serialize(FriendlyByteBuf data) {
        data.writeEnum(kind);
        data.writeByte(getInputSide().get3DDataValue());
        data.writeByte(getOutputOverrideOrdinal());
        data.writeBoolean(destroyer);
        data.writeBoolean(whitelist);
        data.writeBoolean(maxEject);
        data.writeByte(mode);
        data.writeVarIntArray(routerModes);
        data.writeByteArray(patternModes);
    }

    @Override
    public void deserialize(FriendlyByteBuf data) {
        data.readEnum(Kind.class);
        inputSide = Direction.from3DDataValue(data.readByte());
        int output = data.readByte();
        outputOverride = output == NO_OVERRIDE ? null : Direction.from3DDataValue(output);
        destroyer = data.readBoolean();
        whitelist = data.readBoolean();
        maxEject = data.readBoolean();
        mode = data.readByte();
        routerModes = data.readVarIntArray();
        patternModes = data.readByteArray();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmItemStackUtil.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains(TAG_INPUT_SIDE)) {
            inputSide = Direction.from3DDataValue(tag.getByte(TAG_INPUT_SIDE));
        }
        if (tag.contains(TAG_OUTPUT_OVERRIDE)) {
            int output = tag.getByte(TAG_OUTPUT_OVERRIDE);
            outputOverride = output < 0 || output >= Direction.values().length || output == NO_OVERRIDE
                    ? null : Direction.from3DDataValue(output);
        }
        destroyer = !tag.contains(TAG_DESTROYER) || tag.getBoolean(TAG_DESTROYER);
        whitelist = tag.getBoolean(TAG_WHITELIST);
        maxEject = tag.getBoolean(TAG_MAX_EJECT);
        lastGrabbedTick = tag.getLong(TAG_LAST_GRABBED);
        mode = tag.getByte(TAG_MODE);
        lastRedstone = tag.getBoolean(TAG_LAST_REDSTONE);
        if (tag.contains(TAG_ROUTER_MODES)) {
            int[] loaded = tag.getIntArray(TAG_ROUTER_MODES);
            System.arraycopy(loaded, 0, routerModes, 0, Math.min(loaded.length, routerModes.length));
        }
        if (tag.contains(TAG_PATTERN)) {
            byte[] loaded = tag.getByteArray(TAG_PATTERN);
            patternModes = new byte[Math.max(kind.filterSlots, 1)];
            System.arraycopy(loaded, 0, patternModes, 0, Math.min(loaded.length, patternModes.length));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmItemStackUtil.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        tag.putByte(TAG_INPUT_SIDE, (byte) getInputSide().get3DDataValue());
        tag.putByte(TAG_OUTPUT_OVERRIDE, (byte) getOutputOverrideOrdinal());
        tag.putBoolean(TAG_DESTROYER, destroyer);
        tag.putBoolean(TAG_WHITELIST, whitelist);
        tag.putBoolean(TAG_MAX_EJECT, maxEject);
        tag.putLong(TAG_LAST_GRABBED, lastGrabbedTick);
        tag.putByte(TAG_MODE, mode);
        tag.putBoolean(TAG_LAST_REDSTONE, lastRedstone);
        tag.putIntArray(TAG_ROUTER_MODES, routerModes);
        tag.putByteArray(TAG_PATTERN, patternModes);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CraneLogisticsMenu(containerId, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container." + switch (kind) {
            case EXTRACTOR -> "craneExtractor";
            case INSERTER -> "craneInserter";
            case GRABBER -> "craneGrabber";
            case ROUTER -> "craneRouter";
            case BOXER -> "craneBoxer";
            case UNBOXER -> "craneUnboxer";
            case PARTITIONER -> "partitioner";
        });
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    private static Direction defaultInput(BlockState state) {
        return state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    public enum Kind {
        EXTRACTOR(20, 0, 9),
        INSERTER(21, 0, 0),
        GRABBER(11, 0, 9),
        ROUTER(30, 0, 30),
        BOXER(21, 0, 0),
        UNBOXER(23, 0, 0),
        PARTITIONER(90, 0, 0);

        private final int slots;
        private final int filterStart;
        private final int filterSlots;

        Kind(int slots, int filterStart, int filterSlots) {
            this.slots = slots;
            this.filterStart = filterStart;
            this.filterSlots = filterSlots;
        }

        public int slots() {
            return slots;
        }

        public int filterSlots() {
            return filterSlots;
        }

        public static Kind fromBlock(BlockState state) {
            if (state.is(ModBlocks.CRANE_INSERTER.get())) return INSERTER;
            if (state.is(ModBlocks.CRANE_GRABBER.get())) return GRABBER;
            if (state.is(ModBlocks.CRANE_ROUTER.get())) return ROUTER;
            if (state.is(ModBlocks.CRANE_BOXER.get())) return BOXER;
            if (state.is(ModBlocks.CRANE_UNBOXER.get())) return UNBOXER;
            if (state.is(ModBlocks.CRANE_PARTITIONER.get())) return PARTITIONER;
            return EXTRACTOR;
        }
    }
}
