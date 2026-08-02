package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.api.common.CopiableSettings;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.CraneLogisticsMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.network.HbmLegacyBufPacketReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipeRuntime;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.util.LegacyPatternMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraneLogisticsBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyControlReceiver,
        HbmLegacyBufPacketReceiver, CopiableSettings {
    public static final ModelProperty<CraneRenderData> RENDER_DATA_PROPERTY = new ModelProperty<>();
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
    private static final String TAG_SETTINGS_OUTPUT_SIDE = "outputSide";
    private static final String TAG_SETTINGS_SLOT = "slot";
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
    private final LegacyPatternMatcher patternMatcher;

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
        this.patternMatcher = new LegacyPatternMatcher(kind.filterSlots);
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

    public boolean isValidUpgradeForSlot(int slot, ItemStack stack) {
        return isItemValid(slot, stack);
    }

    public int getComparatorSignal() {
        float filled = 0.0F;
        int nonEmpty = 0;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            int capacity = Math.min(items.getSlotLimit(slot), stack.getMaxStackSize());
            if (capacity > 0) {
                filled += (float) stack.getCount() / capacity;
            }
            nonEmpty++;
        }
        return Mth.floor(filled / items.getSlots() * 14.0F) + (nonEmpty > 0 ? 1 : 0);
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

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(RENDER_DATA_PROPERTY, new CraneRenderData(getInputSide(), getOutputSide()))
                .build();
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
        return slot >= 0 && slot < kind.filterSlots
                ? patternMatcher.getModeIndex(items.getStackInSlot(slot), slot)
                : -1;
    }

    public void cyclePatternMode(int slot) {
        if (slot < 0 || slot >= kind.filterSlots) {
            return;
        }
        patternMatcher.nextMode(items.getStackInSlot(slot), slot);
        setChangedAndUpdate();
    }

    public void setPatternStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= kind.filterSlots || slot >= items.getSlots()) {
            return;
        }
        ItemStack pattern = stack.isEmpty() ? ItemStack.EMPTY : HbmItemStackUtil.carefulCopyWithSize(stack, 1);
        items.setStackInSlot(slot, pattern);
        patternMatcher.initPatternSmart(pattern, slot);
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

    /**
     * Source-backed settings-tool bridge for {@code TileEntityCraneBase} and
     * {@code TileEntityCraneRouter}.  The legacy tool stores filter slots
     * relative to the crane's filter range, then uses its selected copy index
     * for either the normal orientation branch or one router pattern lane.
     */
    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        if (kind == Kind.ROUTER) {
            writeCopiedFilterItems(tag);
            tag.putIntArray(TAG_ROUTER_MODES, routerModes);
            return tag;
        }

        tag.putInt(TAG_INPUT_SIDE, getInputSide().get3DDataValue());
        tag.putInt(TAG_SETTINGS_OUTPUT_SIDE, getOutputSide().get3DDataValue());
        writeCopiedFilterItems(tag);
        return tag;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {
        if (tag == null) {
            return;
        }
        if (kind == Kind.ROUTER) {
            pasteRouterSettings(tag, index);
            return;
        }
        if (index == 1) {
            // TileEntityCraneBase first applied outputSide, then inputSide.
            if (tag.contains(TAG_SETTINGS_OUTPUT_SIDE, Tag.TAG_ANY_NUMERIC)) {
                setOutputOverride(Direction.from3DDataValue(tag.getInt(TAG_SETTINGS_OUTPUT_SIDE)));
            }
            if (tag.contains(TAG_INPUT_SIDE, Tag.TAG_ANY_NUMERIC)) {
                setInput(Direction.from3DDataValue(tag.getInt(TAG_INPUT_SIDE)));
            }
            return;
        }
        pasteCopiedFilterItems(tag);
    }

    @Override
    public List<Component> infoForDisplay(Level level, BlockPos pos) {
        if (kind == Kind.ROUTER) {
            List<Component> patterns = new ArrayList<>(6);
            for (int pattern = 0; pattern < 6; pattern++) {
                patterns.add(Component.translatable("copytool.pattern" + pattern));
            }
            return patterns;
        }
        return List.of(Component.translatable("copytool.filter"), Component.translatable("copytool.orientation"));
    }

    private void writeCopiedFilterItems(CompoundTag tag) {
        if (kind.filterSlots <= 0) {
            return;
        }
        ListTag copied = new ListTag();
        for (int slot = 0; slot < kind.filterSlots; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag slotTag = new CompoundTag();
            slotTag.putByte(TAG_SETTINGS_SLOT, (byte) slot);
            stack.save(slotTag);
            copied.add(slotTag);
        }
        tag.put(TAG_ITEMS, copied);
    }

    private void pasteCopiedFilterItems(CompoundTag tag) {
        if (kind.filterSlots <= 0) {
            return;
        }
        ListTag copied = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        // TileEntityCraneBase only changes slots represented in its sparse NBT list.
        for (int entry = 0; entry < copied.size(); entry++) {
            CompoundTag slotTag = copied.getCompound(entry);
            int slot = slotTag.getByte(TAG_SETTINGS_SLOT);
            ItemStack copiedStack = ItemStack.of(slotTag);
            if (slot < 0 || slot >= kind.filterSlots || copiedStack.isEmpty()) {
                continue;
            }
            items.setStackInSlot(slot, copiedStack);
            patternMatcher.nextMode(copiedStack, slot);
        }
        if (!copied.isEmpty()) {
            setChangedAndUpdate();
        }
    }

    private void pasteRouterSettings(CompoundTag tag, int index) {
        ListTag copied = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        if (copied.isEmpty() || !tag.contains(TAG_ROUTER_MODES, Tag.TAG_INT_ARRAY)) {
            return;
        }
        int start = index * 5;
        int end = Math.min(start + 5, kind.filterSlots);
        for (int entry = 0; entry < copied.size(); entry++) {
            CompoundTag slotTag = copied.getCompound(entry);
            int slot = slotTag.getByte(TAG_SETTINGS_SLOT);
            ItemStack copiedStack = ItemStack.of(slotTag);
            // Preserve the strict legacy router comparison: the first slot of
            // a selected five-slot group is intentionally not overwritten.
            if (slot <= start || slot >= end || copiedStack.isEmpty()) {
                continue;
            }
            items.setStackInSlot(slot, copiedStack);
            patternMatcher.nextMode(copiedStack, slot);
        }
        int[] copiedModes = tag.getIntArray(TAG_ROUTER_MODES);
        routerModes = Arrays.copyOf(copiedModes, routerModes.length);
        setChangedAndUpdate();
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
            // Legacy CraneInserter/CraneBoxer accept moving packages from every side.
            // Their package handlers still use the legacy output-side inventory access.
            case INSERTER, BOXER -> true;
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
        if (level.getGameTime() % ejectionDelay() != 0L || level.hasNeighborSignal(worldPosition)) {
            syncChanged(15);
            return;
        }
        int amount = ejectionAmount();
        Direction input = getOutputSide();
        Direction output = getInputSide();
        BlockEntity source = level.getBlockEntity(worldPosition.relative(input));
        IConveyorBelt belt = beltAt(worldPosition.relative(output));
        boolean sent = false;
        if (source != null) {
            for (int slot = 0; slot < slotCount(source, input.getOpposite()); slot++) {
                ItemStack available = stackIn(source, slot, input.getOpposite());
                if (available.isEmpty() || !filterAllows(available, 0, 9, whitelist)) {
                    continue;
                }
                int targetAmount = Math.min(amount, available.getMaxStackSize());
                if (maxEject && available.getCount() < targetAmount) {
                    continue;
                }
                int toSend = Math.min(amount, available.getCount());
                if (belt != null) {
                    ItemStack extracted = extractFrom(source, slot, toSend, input.getOpposite(), false);
                    if (!extracted.isEmpty()) {
                        spawnMovingItemAndEnter(worldPosition.relative(output), extracted, output, output.getOpposite());
                        sent = true;
                    }
                } else {
                    int accepted = amountAcceptedByHandlerRange(available, 9, 17, toSend);
                    if (accepted > 0) {
                        ItemStack extracted = extractFrom(source, slot, accepted, input.getOpposite(), false);
                        if (!extracted.isEmpty()) {
                            addToHandlerRange(extracted, 9, 17);
                        }
                    }
                    sent = true;
                }
                break;
            }
        }
        if (!sent && belt != null) {
            for (int slot = 9; slot < 18; slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                int targetAmount = Math.min(amount, stack.getMaxStackSize());
                if (maxEject && stack.getCount() < targetAmount) {
                    continue;
                }
                ItemStack extracted = items.extractItem(slot, Math.min(amount, stack.getCount()), false);
                if (!extracted.isEmpty()) {
                    spawnMovingItem(worldPosition.relative(output), extracted, output);
                }
                break;
            }
        }
        syncChanged(15);
    }

    private void tickGrabber(Level level) {
        if (level.getGameTime() < lastGrabbedTick + ejectionDelay() || level.hasNeighborSignal(worldPosition)) {
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
        int remainingAmount = ejectionAmount();
        for (MovingItemEntity moving : movingItems) {
            ItemStack stack = moving.getItemStack();
            if (!filterAllows(stack, 0, 9, whitelist)) {
                continue;
            }
            lastGrabbedTick = level.getGameTime();
            if (belt != null) {
                spawnMovingItem(worldPosition.relative(output), stack.copy(), output);
                moving.discard();
                break;
            } else if (target != null) {
                ItemStack toAdd = HbmItemStackUtil.carefulCopyWithSize(stack, Math.min(remainingAmount, stack.getCount()));
                ItemStack remainder = insertInto(target, toAdd, output.getOpposite());
                int added = toAdd.getCount() - remainder.getCount();
                if (added > 0) {
                    stack.shrink(added);
                    moving.setItemStack(stack);
                    if (stack.isEmpty()) {
                        moving.discard();
                    }
                }
                remainingAmount -= added;
                if (remainingAmount <= 0) {
                    break;
                }
            }
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
            if (fullStacks >= pack && beltAt(worldPosition.relative(getOutputSide())) != null) {
                ItemStack[] box = new ItemStack[pack];
                for (int i = 0; i < 21 && pack > 0; i++) {
                    ItemStack stack = items.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getCount() == stack.getMaxStackSize()) {
                        box[--pack] = stack.copy();
                        items.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
                spawnMovingPackage(worldPosition.relative(getOutputSide()), box, getOutputSide());
            }
        }
        syncChanged(15);
    }

    private void tickUnboxer(Level level) {
        if (level.getGameTime() % ejectionDelay() != 0L || level.hasNeighborSignal(worldPosition)) {
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
            int toSendCount = Math.min(ejectionAmount(), stack.getCount());
            ItemStack toSend = HbmItemStackUtil.carefulCopyWithSize(stack, toSendCount);
            items.extractItem(i, toSendCount, false);
            spawnMovingItem(worldPosition.relative(output), toSend, output);
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
            spawnMovingPackage(worldPosition.relative(getOutputSide()), box.toArray(new ItemStack[0]), getOutputSide());
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
            } else {
                BlockPos output = worldPosition.relative(direction);
                if (beltAt(output) != null) {
                    if (packageMode) {
                        spawnMovingPackage(output, sorted[i].toArray(new ItemStack[0]), direction);
                    } else {
                        for (ItemStack stack : sorted[i]) {
                            spawnMovingItem(output, stack, direction);
                        }
                    }
                } else {
                    for (ItemStack stack : sorted[i]) {
                        dropRouterOutput(direction, stack);
                    }
                }
            }
        }
    }

    private void dropRouterOutput(Direction direction, ItemStack stack) {
        Vec3 origin = Vec3.atCenterOf(worldPosition);
        HbmInventoryUtil.dropStack(level,
                origin.x + direction.getStepX() * 0.55D,
                origin.y + direction.getStepY() * 0.55D,
                origin.z + direction.getStepZ() * 0.55D,
                stack);
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
        return patternMatcher.isValidForFilter(filter, slot, stack);
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
        return ItemProcessingRecipeRuntime.inputAmount(level, ItemProcessingRecipe.Machine.CRYSTALLIZER, stack);
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
        return ConveyorMath.conveyorAt(level, pos);
    }

    private void spawnMovingItem(BlockPos pos, ItemStack stack) {
        spawnMovingItem(pos, stack, null);
    }

    private void spawnMovingItem(BlockPos pos, ItemStack stack, @Nullable Direction approach) {
        if (level == null || stack.isEmpty()) {
            return;
        }
        MovingItemEntity moving = new MovingItemEntity(level, stack);
        Vec3 snap = snapFor(pos, approach);
        moving.moveTo(snap.x, snap.y, snap.z, 0.0F, 0.0F);
        level.addFreshEntity(moving);
    }

    private void spawnMovingItemAndEnter(BlockPos pos, ItemStack stack, Direction approach, Direction side) {
        if (level == null || stack.isEmpty()) {
            return;
        }
        MovingItemEntity moving = new MovingItemEntity(level, stack);
        Vec3 snap = snapFor(pos, approach);
        moving.moveTo(snap.x, snap.y, snap.z, 0.0F, 0.0F);
        level.addFreshEntity(moving);

        IEnterableBlock enterable = ConveyorMath.enterableAt(level, pos);
        if (enterable != null) {
            moving.enterBlock(enterable, pos, side);
        }
    }

    private void spawnMovingPackage(BlockPos pos, ItemStack[] stacks) {
        spawnMovingPackage(pos, stacks, null);
    }

    private void spawnMovingPackage(BlockPos pos, ItemStack[] stacks, @Nullable Direction approach) {
        if (level == null || stacks == null || stacks.length == 0 || beltAt(pos) == null) {
            return;
        }
        MovingPackageEntity moving = new MovingPackageEntity(level, stacks);
        Vec3 snap = snapFor(pos, approach);
        moving.moveTo(snap.x, snap.y, snap.z, 0.0F, 0.0F);
        level.addFreshEntity(moving);
    }

    private Vec3 snapFor(BlockPos pos) {
        return snapFor(pos, null);
    }

    private Vec3 snapFor(BlockPos pos, @Nullable Direction approach) {
        IConveyorBelt belt = beltAt(pos);
        Vec3 center = Vec3.atCenterOf(pos);
        if (belt == null || approach == null) {
            return belt == null ? center : belt.getClosestSnappingPosition(level, pos, center);
        }

        Vec3 incoming = center.subtract(approach.getStepX() * 0.45D,
                approach.getStepY() * 0.45D, approach.getStepZ() * 0.45D);
        return belt.getClosestSnappingPosition(level, pos, incoming);
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

    private ItemStack stackIn(BlockEntity source, int slot, Direction side) {
        IItemHandler handler = source.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(slot);
    }

    private int amountAcceptedByHandlerRange(ItemStack stack, int start, int end, int amount) {
        ItemStack remainder = HbmItemStackUtil.carefulCopyWithSize(stack, amount);
        for (int slot = start; slot <= end && !remainder.isEmpty(); slot++) {
            remainder = items.insertItem(slot, remainder, true);
        }
        return amount - remainder.getCount();
    }

    private int ejectionDelay() {
        return switch (upgradeTier(ejectorUpgradeSlot(), UpgradeType.EJECTOR)) {
            case 1 -> 10;
            case 2 -> 5;
            case 3 -> 2;
            default -> 20;
        };
    }

    private int ejectionAmount() {
        return switch (upgradeTier(stackUpgradeSlot(), UpgradeType.STACK)) {
            case 1 -> 4;
            case 2 -> 16;
            case 3 -> 64;
            default -> 1;
        };
    }

    private int stackUpgradeSlot() {
        return switch (kind) {
            case EXTRACTOR -> 18;
            case GRABBER -> 9;
            case UNBOXER -> 21;
            default -> -1;
        };
    }

    private int ejectorUpgradeSlot() {
        return switch (kind) {
            case EXTRACTOR -> 19;
            case GRABBER -> 10;
            case UNBOXER -> 22;
            default -> -1;
        };
    }

    private int upgradeTier(int slot, UpgradeType expectedType) {
        if (slot < 0 || slot >= items.getSlots()) {
            return 0;
        }
        ItemStack stack = items.getStackInSlot(slot);
        if (stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == expectedType) {
            return upgrade.getTier();
        }
        return 0;
    }

    private AABB grabBox(Direction input) {
        double reach = 1.0D;
        BlockPos target = worldPosition.relative(input);
        if (level != null && input.getAxis().isHorizontal()) {
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
        return state.hasProperty(com.hbm.ntm.block.CraneLogisticsBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.CraneLogisticsBlock.FACING)
                : Direction.SOUTH;
    }

    private boolean isItemValid(int slot, ItemStack stack) {
        return switch (kind) {
            case EXTRACTOR -> (slot >= 9 && slot < 18)
                    || (slot == 18 && isUpgrade(stack, UpgradeType.STACK))
                    || (slot == 19 && isUpgrade(stack, UpgradeType.EJECTOR));
            case GRABBER -> slot < 9
                    || (slot == 9 && isUpgrade(stack, UpgradeType.STACK))
                    || (slot == 10 && isUpgrade(stack, UpgradeType.EJECTOR));
            case UNBOXER -> slot < 21
                    || (slot == 21 && isUpgrade(stack, UpgradeType.STACK))
                    || (slot == 22 && isUpgrade(stack, UpgradeType.EJECTOR));
            case PARTITIONER -> slot < 45 && partitionerAmount(stack) > 0;
            default -> true;
        };
    }

    private boolean isUpgrade(ItemStack stack, UpgradeType expectedType) {
        return stack.getItem() instanceof ItemMachineUpgrade upgrade && upgrade.getUpgradeType() == expectedType;
    }

    private void writePatternModes(FriendlyByteBuf data) {
        data.writeVarInt(kind.filterSlots);
        for (int slot = 0; slot < kind.filterSlots; slot++) {
            String mode = patternMatcher.getMode(slot);
            data.writeBoolean(mode != null);
            if (mode != null) {
                data.writeUtf(mode);
            }
        }
    }

    private void readPatternModes(FriendlyByteBuf data) {
        int count = data.readVarInt();
        for (int slot = 0; slot < kind.filterSlots; slot++) {
            patternMatcher.setMode(slot, null);
        }
        for (int slot = 0; slot < count; slot++) {
            String mode = data.readBoolean() ? data.readUtf() : null;
            if (slot < kind.filterSlots) {
                patternMatcher.setMode(slot, mode);
            }
        }
    }

    private void migrateApproximatePatternModes(byte[] modes) {
        for (int slot = 0; slot < modes.length && slot < kind.filterSlots; slot++) {
            patternMatcher.setMode(slot, modes[slot] == 1
                    ? LegacyPatternMatcher.MODE_WILDCARD
                    : LegacyPatternMatcher.MODE_EXACT);
        }
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
            level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
        }
    }

    private void refreshCraneModelData() {
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
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
        writePatternModes(data);
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
        readPatternModes(data);
        refreshCraneModelData();
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
        patternMatcher.readFromNbt(tag);
        if (!tag.contains("mode0") && tag.contains(TAG_PATTERN)) {
            migrateApproximatePatternModes(tag.getByteArray(TAG_PATTERN));
        }
        refreshCraneModelData();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshCraneModelData();
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
        patternMatcher.writeToNbt(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return new CompoundTag();
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
        return state.hasProperty(com.hbm.ntm.block.CraneLogisticsBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.CraneLogisticsBlock.FACING)
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

    public record CraneRenderData(Direction input, Direction output) {
    }
}
