package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CargoElevatorBlockEntity;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CargoElevatorBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private static final int[] LEGACY_DIMENSIONS = { 0, 0, 1, 1, 1, 1 };
    private static final int LEGACY_OFFSET = 1;

    public CargoElevatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return LEGACY_OFFSET;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CargoElevatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.CARGO_ELEVATOR.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        CargoElevatorBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (CargoElevatorBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        CargoElevatorBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (CargoElevatorBlockEntity) blockEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, pos);
        if (!(level.getBlockEntity(corePos) instanceof CargoElevatorBlockEntity elevator)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.is(asItem())) {
            if (addLayer((ServerLevel) level, corePos, elevator)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
        } else {
            elevator.toggleElevator();
        }
        return InteractionResult.CONSUME;
    }

    public static boolean addLayer(ServerLevel level, BlockPos corePos, CargoElevatorBlockEntity elevator) {
        int y = corePos.getY() + elevator.getHeight() + 1;
        for (BlockPos pos : layerPositions(corePos, y)) {
            if (!level.getBlockState(pos).canBeReplaced()) {
                return false;
            }
        }
        for (BlockPos pos : layerPositions(corePos, y)) {
            setOwnedDummy(level, corePos, pos);
        }
        elevator.setHeight(elevator.getHeight() + 1);
        elevator.syncChanged();
        return true;
    }

    public static void convertTowerToDummies(ServerLevel level, BlockPos newCorePos, BlockPos lowerCorePos,
            int height) {
        level.removeBlock(newCorePos, false);
        int maxY = newCorePos.getY() + height;
        for (int y = newCorePos.getY(); y <= maxY; y++) {
            for (BlockPos pos : layerPositions(newCorePos, y)) {
                setOwnedDummy(level, lowerCorePos, pos);
            }
        }
    }

    private static List<BlockPos> layerPositions(BlockPos corePos, int y) {
        List<BlockPos> positions = new ArrayList<>(9);
        for (int x = corePos.getX() - 1; x <= corePos.getX() + 1; x++) {
            for (int z = corePos.getZ() - 1; z <= corePos.getZ() + 1; z++) {
                positions.add(new BlockPos(x, y, z));
            }
        }
        return positions;
    }

    private static void setOwnedDummy(Level level, BlockPos corePos, BlockPos pos) {
        level.setBlock(pos, ModBlocks.DUMMY_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
            dummy.configure(corePos, com.hbm.ntm.multiblock.LegacyProxyMode.none(), false);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getMultiblockShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getMultiblockCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return elevatorShape(level, corePos);
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return elevatorShape(level, corePos);
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    @Override
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return true;
    }

    private static VoxelShape elevatorShape(BlockGetter level, BlockPos corePos) {
        int height = 1;
        double extension = 0.0D;
        if (level.getBlockEntity(corePos) instanceof CargoElevatorBlockEntity elevator) {
            height = elevator.getHeight() + 1;
            extension = elevator.getExtension();
        }

        VoxelShape shape = Shapes.empty();
        shape = Shapes.joinUnoptimized(shape, Shapes.box(-1.0D, 0.0D, -1.0D, -0.75D, height, -0.75D),
                BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(-1.0D, 0.0D, 1.75D, -0.75D, height, 2.0D),
                BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(1.75D, 0.0D, -1.0D, 2.0D, height, -0.75D),
                BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(1.75D, 0.0D, 1.75D, 2.0D, height, 2.0D),
                BooleanOp.OR);
        shape = Shapes.joinUnoptimized(shape, Shapes.box(-1.0D, 0.75D + extension, -1.0D,
                2.0D, 1.0D + extension, 2.0D), BooleanOp.OR);
        return shape.optimize();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY)
                instanceof CargoElevatorBlockEntity elevator) {
            int count = elevator.getHeight() + 1;
            List<ItemStack> drops = new ArrayList<>();
            while (count > 0) {
                int perStack = Math.min(count, 64);
                count -= perStack;
                drops.add(new ItemStack(this, perStack));
            }
            return drops;
        }
        return super.getDrops(state, builder);
    }

    @Override
    public BlockState multiblockParticleState(BlockState state, BlockGetter level, BlockPos corePos) {
        return MultiblockHelper.steelParticleState();
    }
}
