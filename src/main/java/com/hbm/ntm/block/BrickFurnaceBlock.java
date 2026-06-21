package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.BrickFurnaceBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BrickFurnaceBlock extends HorizontalMachineBlock implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BrickFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrickFurnaceBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof BrickFurnaceBlockEntity furnace) {
            NetworkHooks.openScreen(serverPlayer, furnace, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.BRICK_FURNACE.get()) {
            return null;
        }
        return level.isClientSide ? null : (tickLevel, tickPos, tickState, blockEntity) ->
                BrickFurnaceBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (BrickFurnaceBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        frontSmokeFlame(state, level, pos, random);
    }

    static void frontSmokeFlame(BlockState state, Level level, BlockPos pos, RandomSource random) {
        frontSmokeFlame(state, level, pos, random, 0.0D);
    }

    static void frontSmokeFlame(BlockState state, Level level, BlockPos pos, RandomSource random, double yBase) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + yBase + random.nextDouble() * 0.375D;
        double z = pos.getZ() + 0.5D;
        double off = 0.52D;
        double var = random.nextDouble() * 0.6D - 0.3D;
        double px = x + facing.getStepX() * off + Math.abs(facing.getStepZ()) * var;
        double pz = z + facing.getStepZ() * off + Math.abs(facing.getStepX()) * var;
        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, px, y, pz, 0.0D, 0.0D, 0.0D);
        level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, px, y, pz, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof BrickFurnaceBlockEntity furnace) {
            for (ItemStack stack : furnace.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
