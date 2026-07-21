package com.hbm.ntm.item;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.ConveyorRoutePlanner;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.api.item.LegacyLookOverlayItemProvider;
import com.hbm.ntm.block.conveyor.ChuteConveyorBlock;
import com.hbm.ntm.block.conveyor.ConveyorBlock;
import com.hbm.ntm.block.conveyor.LiftConveyorBlock;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class ConveyorWandItem extends Item implements LegacyLookOverlayItemProvider {
    private static final String TAG_TYPE = "Type";
    private static final String TAG_START = "Start";
    private static final String TAG_SIDE = "Side";
    private static final String TAG_COUNT = "Count";

    public ConveyorWandItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createStack(Item item, ConveyorRoutePlanner.ConveyorWandType type, int count) {
        ItemStack stack = new ItemStack(item, count);
        stack.getOrCreateTag().putString(TAG_TYPE, type.name());
        return stack;
    }

    public static ConveyorRoutePlanner.ConveyorWandType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_TYPE)) {
            return ConveyorRoutePlanner.ConveyorWandType.REGULAR;
        }
        try {
            return ConveyorRoutePlanner.ConveyorWandType.valueOf(tag.getString(TAG_TYPE));
        } catch (IllegalArgumentException ignored) {
            return ConveyorRoutePlanner.ConveyorWandType.REGULAR;
        }
    }

    public static void addCreativeStacks(net.minecraft.world.item.CreativeModeTab.Output output, ConveyorWandItem item) {
        for (ConveyorRoutePlanner.ConveyorWandType type : ConveyorRoutePlanner.ConveyorWandType.values()) {
            output.accept(createStack(item, type, 1));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        String typeName = getType(stack).name().toLowerCase(Locale.ROOT);
        return Component.translatable("item.hbm_ntm_rebirth.conveyor_wand." + typeName);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.conveyor_wand.desc").withStyle(ChatFormatting.GRAY));
        if (getType(stack).hasVertical()) {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth.conveyor_wand.vertical.desc").withStyle(ChatFormatting.AQUA));
        }
        if (hasStart(stack)) {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth.conveyor_wand.selected").withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Direction side = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && !hasStart(stack)) {
            return placeSingle(context, stack, player, clickedPos, side);
        }

        side = snapSideToConveyorEdge(level, player, stack, clickedPos, side);

        if (!hasStart(stack)) {
            saveStart(stack, clickedPos, side, countAvailable(player, stack));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            ConveyorRoutePlanner.RouteResult result = planRoute(level, player, stack, clickedPos, side);

            switch (result.status()) {
                case SUCCESS -> {
                    placeRoute(level, player, stack, result.placements());
                    consumeRouteItems(player, stack, result.placements().size());
                    player.displayClientMessage(Component.translatable("item.hbm_ntm_rebirth.conveyor_wand.built"), true);
                }
                case NOT_ENOUGH_CONVEYORS -> player.displayClientMessage(Component.translatable("item.hbm_ntm_rebirth.conveyor_wand.not_enough"), true);
                case OBSTRUCTED -> player.displayClientMessage(Component.translatable("item.hbm_ntm_rebirth.conveyor_wand.obstructed"), true);
            }
        }

        clearStart(stack);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!selected && hasStart(stack)) {
            clearStart(stack);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasStart(stack) || super.isFoil(stack);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, ItemStack stack, BlockHitResult hit) {
        if (player == null || !player.isShiftKeyDown() || !player.getAbilities().instabuild) {
            return null;
        }
        BlockState state = level.getBlockState(hit.getBlockPos());
        if (!(state.getBlock() instanceof ConveyorBlock)) {
            return null;
        }
        return LegacyLookOverlay.forBlockState(state, List.of(Component.literal("Break whole conveyor line")));
    }

    private InteractionResult placeSingle(UseOnContext context, ItemStack stack, Player player, BlockPos clickedPos, Direction side) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            ConveyorRoutePlanner.ConveyorWandType type = getType(stack);
            BlockState clickedState = level.getBlockState(clickedPos);
            Block clickedBlock = clickedState.getBlock();
            Block toPlace = blockForType(type);

            if (type.hasVertical() && clickedBlock == ModBlocks.CONVEYOR.get()) {
                ConveyorBlock conveyor = (ConveyorBlock) clickedBlock;
                int clickedMetadata = conveyor.legacyMetadata(clickedState);
                if (clickedMetadata < 6 && side == Direction.UP) {
                    toPlace = ModBlocks.CONVEYOR_LIFT.get();
                    level.setBlock(clickedPos,
                            ((ConveyorBlock) ModBlocks.CONVEYOR_LIFT.get()).stateFromLegacyMetadata(clickedMetadata),
                            Block.UPDATE_ALL);
                    clickedBlock = toPlace;
                } else if (clickedMetadata < 6 && side == Direction.DOWN) {
                    toPlace = ModBlocks.CONVEYOR_CHUTE.get();
                    level.setBlock(clickedPos,
                            ((ConveyorBlock) ModBlocks.CONVEYOR_CHUTE.get()).stateFromLegacyMetadata(clickedMetadata),
                            Block.UPDATE_ALL);
                    clickedBlock = toPlace;
                }
            }

            if (type.hasVertical()) {
                if (clickedBlock == ModBlocks.CONVEYOR_LIFT.get() && side == Direction.UP) {
                    toPlace = ModBlocks.CONVEYOR_LIFT.get();
                } else if (clickedBlock == ModBlocks.CONVEYOR_CHUTE.get() && side == Direction.DOWN) {
                    toPlace = ModBlocks.CONVEYOR_CHUTE.get();
                }
            }

            BlockPos placePos = clickedPos.relative(side);
            int metadata = ConveyorMath.legacyMetadataForPlacementYaw(player.getYRot());
            if (isReplaceable(level, player, stack, placePos) && placeConveyor(level, player, stack, placePos, toPlace, metadata)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void placeRoute(Level level, Player player, ItemStack stack, List<ConveyorRoutePlanner.Placement> placements) {
        for (ConveyorRoutePlanner.Placement placement : placements) {
            placeConveyor(level, player, stack, placement.pos(), blockForKind(placement.kind()), placement.legacyMetadata());
        }
    }

    private static boolean placeConveyor(Level level, Player player, ItemStack stack, BlockPos pos, Block block, int legacyMetadata) {
        BlockState state = blockStateFor(block, legacyMetadata);
        if (!level.setBlock(pos, state, 3)) {
            return false;
        }
        return true;
    }

    private static boolean isReplaceable(Level level, Player player, ItemStack stack, BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(pos.getCenter(), Direction.UP, pos, false);
        BlockPlaceContext placeContext = new BlockPlaceContext(level, player, net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
        return level.getBlockState(pos).canBeReplaced(placeContext);
    }

    private static ConveyorRoutePlanner.ConveyorBlockKind blockKindAt(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (block == ModBlocks.CONVEYOR.get()) {
            return ConveyorRoutePlanner.ConveyorBlockKind.REGULAR;
        }
        if (block == ModBlocks.CONVEYOR_EXPRESS.get()) {
            return ConveyorRoutePlanner.ConveyorBlockKind.EXPRESS;
        }
        if (block == ModBlocks.CONVEYOR_DOUBLE.get()) {
            return ConveyorRoutePlanner.ConveyorBlockKind.DOUBLE;
        }
        if (block == ModBlocks.CONVEYOR_TRIPLE.get()) {
            return ConveyorRoutePlanner.ConveyorBlockKind.TRIPLE;
        }
        if (block == ModBlocks.CONVEYOR_LIFT.get()) {
            return ConveyorRoutePlanner.ConveyorBlockKind.LIFT;
        }
        if (block == ModBlocks.CONVEYOR_CHUTE.get()) {
            return ConveyorRoutePlanner.ConveyorBlockKind.CHUTE;
        }
        // ItemConveyorWand only treated legacy BlockCraneBase subclasses as route endpoints.
        // Router, partitioner, and splitter expose conveyor entry hooks, but were not endpoint targets.
        if (block == ModBlocks.CRANE_EXTRACTOR.get()
                || block == ModBlocks.CRANE_INSERTER.get()
                || block == ModBlocks.CRANE_GRABBER.get()
                || block == ModBlocks.CRANE_BOXER.get()
                || block == ModBlocks.CRANE_UNBOXER.get()) {
            return ConveyorRoutePlanner.ConveyorBlockKind.CRANE;
        }
        if (ConveyorMath.isConveyor(level, pos)) {
            return ConveyorRoutePlanner.ConveyorBlockKind.OTHER;
        }
        return ConveyorRoutePlanner.ConveyorBlockKind.OTHER;
    }

    private static Block blockForKind(ConveyorRoutePlanner.ConveyorBlockKind kind) {
        return switch (kind) {
            case EXPRESS -> ModBlocks.CONVEYOR_EXPRESS.get();
            case DOUBLE -> ModBlocks.CONVEYOR_DOUBLE.get();
            case TRIPLE -> ModBlocks.CONVEYOR_TRIPLE.get();
            case LIFT -> ModBlocks.CONVEYOR_LIFT.get();
            case CHUTE -> ModBlocks.CONVEYOR_CHUTE.get();
            default -> ModBlocks.CONVEYOR.get();
        };
    }

    private static Block blockForType(ConveyorRoutePlanner.ConveyorWandType type) {
        return switch (type) {
            case EXPRESS -> ModBlocks.CONVEYOR_EXPRESS.get();
            case DOUBLE -> ModBlocks.CONVEYOR_DOUBLE.get();
            case TRIPLE -> ModBlocks.CONVEYOR_TRIPLE.get();
            default -> ModBlocks.CONVEYOR.get();
        };
    }

    private static Direction snapSideToConveyorEdge(Level level, Player player, ItemStack stack, BlockPos clickedPos,
            Direction side) {
        BlockState state = level.getBlockState(clickedPos);
        if (!(state.getBlock() instanceof ConveyorBlock conveyor) || !conveyor.supportsWandEdgeSnapping(state)) {
            return side;
        }

        Direction moveDirection = hasStart(stack) ? conveyor.getInputDirection(state) : conveyor.getOutputDirection(state);
        return isReplaceable(level, player, stack, clickedPos.relative(moveDirection)) ? moveDirection : side;
    }

    private static int countAvailable(Player player, ItemStack selected) {
        if (player.getAbilities().instabuild) {
            return ConveyorRoutePlanner.CREATIVE_MAX_CONVEYORS;
        }

        int count = 0;
        ConveyorRoutePlanner.ConveyorWandType selectedType = getType(selected);
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(selected.getItem()) && getType(stack) == selectedType) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeRouteItems(Player player, ItemStack selected, int count) {
        if (player.getAbilities().instabuild) {
            return;
        }

        ConveyorRoutePlanner.ConveyorWandType selectedType = getType(selected);
        int remaining = count;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(selected.getItem()) && getType(stack) == selectedType) {
                int removing = Math.min(remaining, stack.getCount());
                stack.shrink(removing);
                remaining -= removing;
                if (remaining <= 0) {
                    break;
                }
            }
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    public static boolean hasStart(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_START);
    }

    /**
     * Shared route construction for the authoritative placement and the client-only action preview.
     */
    public static ConveyorRoutePlanner.RouteResult planRoute(Level level, Player player, ItemStack stack,
            BlockPos clickedPos, Direction clickedSide) {
        if (!hasStart(stack)) {
            return ConveyorRoutePlanner.RouteResult.obstructed();
        }

        Direction side = snapSideToConveyorEdge(level, player, stack, clickedPos, clickedSide);
        RouteStart start = readStart(stack);
        return ConveyorRoutePlanner.plan(new ConveyorRoutePlanner.RouteContext(
                getType(stack),
                start.pos(),
                start.side(),
                clickedPos,
                side,
                start.count(),
                player.getYRot(),
                pos -> isReplaceable(level, player, stack, pos),
                pos -> blockKindAt(level, pos)));
    }

    public static BlockState blockStateForPlacement(ConveyorRoutePlanner.Placement placement) {
        return blockStateFor(blockForKind(placement.kind()), placement.legacyMetadata());
    }

    /**
     * Reconstructs the planned route's local conveyor neighborhood for the client preview.
     * Legacy WorldInAJar rendered all planned blocks together, so lift and chute visuals could
     * see their neighboring planned conveyors.
     */
    public static List<BlockState> blockStatesForPreview(Level level, List<ConveyorRoutePlanner.Placement> placements) {
        Map<BlockPos, Block> plannedBlocks = new HashMap<>();
        for (ConveyorRoutePlanner.Placement placement : placements) {
            plannedBlocks.put(placement.pos(), blockForKind(placement.kind()));
        }

        return placements.stream()
                .map(placement -> previewState(level, placement, plannedBlocks))
                .toList();
    }

    private static BlockState previewState(Level level, ConveyorRoutePlanner.Placement placement,
            Map<BlockPos, Block> plannedBlocks) {
        BlockState state = blockStateForPlacement(placement);
        BlockPos pos = placement.pos();
        if (state.getBlock() instanceof LiftConveyorBlock) {
            return LiftConveyorBlock.withSegmentState(state,
                    isPlannedConveyor(level, plannedBlocks, pos.below()),
                    isPlannedConveyor(level, plannedBlocks, pos.above()),
                    isPlannedEnterable(level, plannedBlocks, pos.above()));
        }
        if (state.getBlock() instanceof ChuteConveyorBlock) {
            return ChuteConveyorBlock.withVisualState(state, pos.getY() > level.getMinBuildHeight(),
                    isPlannedConveyorOrEnterable(level, plannedBlocks, pos.below()),
                    isPlannedConveyor(level, plannedBlocks, pos.west()),
                    isPlannedConveyor(level, plannedBlocks, pos.east()),
                    isPlannedConveyor(level, plannedBlocks, pos.north()),
                    isPlannedConveyor(level, plannedBlocks, pos.south()));
        }
        return state;
    }

    private static boolean isPlannedConveyor(Level level, Map<BlockPos, Block> plannedBlocks, BlockPos pos) {
        Block planned = plannedBlocks.get(pos);
        return planned != null ? planned instanceof IConveyorBelt : ConveyorMath.isConveyor(level, pos);
    }

    private static boolean isPlannedEnterable(Level level, Map<BlockPos, Block> plannedBlocks, BlockPos pos) {
        Block planned = plannedBlocks.get(pos);
        return planned != null ? planned instanceof IEnterableBlock : ConveyorMath.isEnterable(level, pos);
    }

    private static boolean isPlannedConveyorOrEnterable(Level level, Map<BlockPos, Block> plannedBlocks,
            BlockPos pos) {
        return isPlannedConveyor(level, plannedBlocks, pos) || isPlannedEnterable(level, plannedBlocks, pos);
    }

    private static BlockState blockStateFor(Block block, int legacyMetadata) {
        return block instanceof ConveyorBlock conveyor
                ? conveyor.stateFromLegacyMetadata(legacyMetadata)
                : block.defaultBlockState();
    }

    private static void saveStart(ItemStack stack, BlockPos pos, Direction side, int count) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TAG_START, pos.asLong());
        tag.putString(TAG_SIDE, side.getName());
        tag.putInt(TAG_COUNT, count);
    }

    private static RouteStart readStart(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        BlockPos pos = BlockPos.of(tag.getLong(TAG_START));
        Direction side = Direction.byName(tag.getString(TAG_SIDE));
        if (side == null) {
            side = Direction.UP;
        }
        return new RouteStart(pos, side, tag.getInt(TAG_COUNT));
    }

    private static void clearStart(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(TAG_START);
        tag.remove(TAG_SIDE);
        tag.remove(TAG_COUNT);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private record RouteStart(BlockPos pos, Direction side, int count) {
    }
}
