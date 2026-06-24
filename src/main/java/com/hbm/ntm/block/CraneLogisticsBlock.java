package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.IConveyorItem;
import com.hbm.ntm.api.conveyor.IConveyorPackage;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CraneLogisticsBlock extends HorizontalMachineBlock implements EntityBlock, IEnterableBlock,
        IConveyorBelt, Toolable {
    private final CraneLogisticsBlockEntity.Kind kind;

    public CraneLogisticsBlock(Properties properties, CraneLogisticsBlockEntity.Kind kind) {
        super(properties, false);
        this.kind = kind;
    }

    public CraneLogisticsBlockEntity.Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneLogisticsBlockEntity(pos, state, kind);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!player.isShiftKeyDown() && !level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof CraneLogisticsBlockEntity crane) {
            NetworkHooks.openScreen(serverPlayer, crane, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CraneLogisticsBlockEntity crane) {
            if (player.isShiftKeyDown()) {
                crane.setOutputOverride(side);
            } else {
                crane.setInput(side);
            }
        }
        return true;
    }

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity) {
        return level.getBlockEntity(pos) instanceof CraneLogisticsBlockEntity crane && crane.canItemEnter(side);
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity) {
        if (level.getBlockEntity(pos) instanceof CraneLogisticsBlockEntity crane) {
            crane.onItemEnter(side, entity.getItemStack());
        }
    }

    @Override
    public boolean canPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity) {
        return level.getBlockEntity(pos) instanceof CraneLogisticsBlockEntity crane && crane.canPackageEnter(side);
    }

    @Override
    public void onPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity) {
        if (level.getBlockEntity(pos) instanceof CraneLogisticsBlockEntity crane) {
            crane.onPackageEnter(side, entity.getItemStacks());
        }
    }

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        return kind == CraneLogisticsBlockEntity.Kind.PARTITIONER;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        Direction direction = stateFacing(level.getBlockState(pos));
        Vec3 snap = getClosestSnappingPosition(level, pos, itemPos);
        return ConveyorMath.travelLocation(pos, itemPos, direction, snap, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        Direction direction = stateFacing(level.getBlockState(pos));
        return ConveyorMath.closestSnappingPosition(pos, itemPos, direction);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.CRANE_LOGISTICS.get()) {
            return null;
        }
        return level.isClientSide ? null : (tickLevel, tickPos, tickState, blockEntity) ->
                CraneLogisticsBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (CraneLogisticsBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (kind != CraneLogisticsBlockEntity.Kind.PARTITIONER || random.nextInt(6) != 0) {
            return;
        }
        Direction facing = stateFacing(state);
        double x = pos.getX() + 0.5D - facing.getStepX() * 0.35D + random.nextDouble() * 0.2D - 0.1D;
        double y = pos.getY() + 0.18D;
        double z = pos.getZ() + 0.5D - facing.getStepZ() * 0.35D + random.nextDouble() * 0.2D - 0.1D;
        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof CraneLogisticsBlockEntity crane) {
            HbmInventoryMenuHelper.spillItems(level, pos, crane.getItems());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static Direction stateFacing(BlockState state) {
        return state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
    }
}
