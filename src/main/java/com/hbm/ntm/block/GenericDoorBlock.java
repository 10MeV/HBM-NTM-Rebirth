package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.api.multiblock.LegacyMultiblock;
import com.hbm.ntm.blockentity.GenericDoorBlockEntity;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyMultiblockPlaceable;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Registered 1.20.1 carrier for one legacy {@code BlockDoorGeneric/DoorDecl}.
 * The cube models remain a temporary visual carrier; every collision, ray
 * trace and dummy shape comes from {@link LegacyDoorDefinition} instead.
 */
@SuppressWarnings("deprecation")
public class GenericDoorBlock extends LegacyXrMultiblockBlock implements EntityBlock, Toolable,
        LegacyMultiblockPlaceable, LegacyMultiblock, MultiblockCoreBlock {
    private final LegacyDoorDefinition definition;

    public GenericDoorBlock(Properties properties, LegacyDoorDefinition definition) {
        super(properties.noOcclusion());
        this.definition = definition;
    }

    public LegacyDoorDefinition definition() {
        return definition;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        LegacyDoorDefinition.LegacyDoorDimensions d = definition.dimensions();
        return new int[] {d.x1(), d.y1(), d.z1(), d.x2(), d.y2(), d.z2()};
    }

    @Override
    protected int getLegacyOffset() {
        return definition.blockOffset();
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        Direction facing = state.getValue(FACING);
        LegacyMultiblockLayout layout = LegacyMultiblockLayout.ofLegacyXrChecked(getLegacyXrDimensions(), facing);
        for (LegacyDoorDefinition.LegacyDoorDimensions extra : definition.extraDimensions()) {
            layout = layout.withLegacyXrCheckedFill(new int[] {
                    extra.x1(), extra.y1(), extra.z1(), extra.x2(), extra.y2(), extra.z2()
            }, facing);
        }
        return layout;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GenericDoorBlockEntity(ModBlockEntities.GENERIC_DOOR.get(), pos, state, definition);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return type == ModBlockEntities.GENERIC_DOOR.get() ? (tickLevel, tickPos, tickState, entity) -> {
            GenericDoorBlockEntity door = (GenericDoorBlockEntity) entity;
            GenericDoorBlockEntity.serverTick(tickLevel, tickPos, tickState, door);
            if (!tickLevel.isClientSide) {
                reconcileDynamicDummies(tickLevel, tickPos, tickState, door);
            }
        } : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // The old BlockDoorGeneric leaves crouching interaction to its tool path.
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof GenericDoorBlockEntity door)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof PadlockItem) {
            return door.tryApplyPadlock(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return door.tryToggle(player) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER || !player.isShiftKeyDown()) {
            return false;
        }
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, pos);
        if (!(level.getBlockEntity(corePos) instanceof GenericDoorBlockEntity door) || !definition.hasSkins()) {
            return false;
        }
        if (!level.isClientSide) {
            door.cycleSkinIndex();
        }
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos fromPos,
            boolean moving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof GenericDoorBlockEntity door) {
            door.updateRedstonePower(level, fromPos);
        }
        super.neighborChanged(state, level, pos, changedBlock, fromPos, moving);
    }

    @Override
    public boolean usesLocalDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesLocalDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForCell(level, pos, pos, state.getValue(FACING), false, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForCell(level, pos, pos, state.getValue(FACING), false, true);
    }

    @Override
    public VoxelShape getMultiblockDummyShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return shapeForCell(level, corePos, dummyPos, state.getValue(FACING), isExtra(level, dummyPos), false);
    }

    @Override
    public VoxelShape getMultiblockDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            BlockPos dummyPos, CollisionContext context) {
        return shapeForCell(level, corePos, dummyPos, state.getValue(FACING), isExtra(level, dummyPos), true);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos,
            net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private VoxelShape shapeForCell(BlockGetter level, BlockPos corePos, BlockPos cellPos, Direction facing,
            boolean dummyExtra, boolean collision) {
        boolean core = corePos.equals(cellPos);
        boolean coreOpen = core && level.getBlockEntity(corePos) instanceof GenericDoorBlockEntity door
                && door.state() != GenericDoorBlockEntity.STATE_CLOSED;
        AABB box = GenericDoorLogic.cellBounds(definition, corePos, facing, cellPos, coreOpen || dummyExtra, collision);
        return isDegenerate(box) ? Shapes.empty() : Shapes.create(box);
    }

    private static boolean isExtra(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy && dummy.isLegacyExtra();
    }

    private static boolean isDegenerate(AABB box) {
        return box.minX == box.maxX && box.minY == box.maxY && box.minZ == box.maxZ;
    }

    private static void reconcileDynamicDummies(Level level, BlockPos corePos, BlockState state,
            GenericDoorBlockEntity door) {
        Direction facing = state.getValue(FACING);
        var active = door.dynamicOpenCells(facing);
        LegacyMultiblockLayout layout = ((GenericDoorBlock) state.getBlock()).getLayout(state);
        for (BlockPos offset : layout.offsets()) {
            if (offset.equals(BlockPos.ZERO)) {
                continue;
            }
            BlockPos cell = corePos.offset(offset);
            if (active.contains(cell)) {
                MultiblockHelper.makeLegacyExtra(level, corePos, cell, LegacyProxyMode.none());
            } else {
                MultiblockHelper.removeLegacyExtra(level, corePos, cell, LegacyProxyMode.none());
            }
        }
    }
}
