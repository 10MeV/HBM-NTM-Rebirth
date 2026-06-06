package com.hbm.ntm.item;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.ConveyorRoutePlanner;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.api.item.LegacyLookOverlayItemProvider;
import com.hbm.ntm.block.conveyor.ConveyorBlock;
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

        if (!hasStart(stack)) {
            saveStart(stack, clickedPos, side, countAvailable(player, stack));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            RouteStart start = readStart(stack);
            ConveyorRoutePlanner.RouteResult result = ConveyorRoutePlanner.plan(new ConveyorRoutePlanner.RouteContext(
                    getType(stack),
                    start.pos(),
                    start.side(),
                    clickedPos,
                    side,
                    start.count(),
                    player.getYRot(),
                    pos -> isReplaceable(level, player, stack, pos),
                    pos -> blockKindAt(level, pos)));

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
        ConveyorRoutePlanner.ConveyorWandType type = getType(stack);
        BlockPos placePos = clickedPos.relative(side);
        ConveyorRoutePlanner.ConveyorBlockKind kind = type.hasVertical()
                ? ConveyorRoutePlanner.blockKindForDirection(type, side)
                : ConveyorRoutePlanner.blockKindForDirection(type, Direction.NORTH);
        Block block = blockForKind(kind);
        int metadata = ConveyorRoutePlanner.metadataForDirection(
                kind,
                side,
                side,
                Direction.from3DDataValue(ConveyorMath.legacyMetadataForPlacementYaw(player.getYRot())).getOpposite());

        if (!level.isClientSide && isReplaceable(level, player, stack, placePos) && placeConveyor(level, player, stack, placePos, block, metadata)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
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
        BlockState state = block instanceof ConveyorBlock conveyor
                ? conveyor.stateFromLegacyMetadata(legacyMetadata)
                : block.defaultBlockState();
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
        if (block instanceof IEnterableBlock) {
            return ConveyorRoutePlanner.ConveyorBlockKind.ENTERABLE;
        }
        if (block instanceof IConveyorBelt) {
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

    private static boolean hasStart(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_START);
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
