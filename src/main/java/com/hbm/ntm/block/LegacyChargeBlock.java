package com.hbm.ntm.block;

import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.LegacyChargeBlockEntity;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.explosion.ExplosionNT;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyChargeBlock extends BaseEntityBlock implements ChainExplodable, RemoteDetonatableBlock, Toolable {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    private static final VoxelShape SHAPE_DOWN = box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_UP = box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
    private static final VoxelShape SHAPE_NORTH = box(0.0D, 0.0D, 10.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_SOUTH = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 6.0D);
    private static final VoxelShape SHAPE_WEST = box(10.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_EAST = box(0.0D, 0.0D, 0.0D, 6.0D, 16.0D, 16.0D);

    private final Kind kind;

    public LegacyChargeBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockState state = defaultBlockState().setValue(FACING, facing);
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos)
                ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyChargeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, ModBlockEntities.LEGACY_CHARGE.get(), LegacyChargeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof LegacyChargeBlockEntity charge)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && !charge.isStarted()) {
            if (player.isShiftKeyDown()) {
                if (charge.getTimer() > 0) {
                    charge.setStarted(true);
                    LegacySoundPlayer.playLegacyFstbmbStart(level, pos, 1.0F, 1.0F);
                }
            } else {
                charge.cycleTimer();
                LegacySoundPlayer.playLegacyTechBoop(level, pos, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.DEFUSER || !(level.getBlockEntity(pos) instanceof LegacyChargeBlockEntity charge)) {
            return false;
        }
        if (!level.isClientSide) {
            if (charge.isStarted()) {
                charge.setStarted(false);
                LegacySoundPlayer.playLegacyFstbmbStart(level, pos, 1.0F, 1.0F);
            } else {
                level.removeBlock(pos, false);
                popResource(level, pos, new ItemStack(this));
            }
        }
        return true;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockState(pos).getBlock() == this) {
            detonate(level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.removeBlock(pos, false);
        if (!level.isClientSide) {
            level.addFreshEntity(LegacyPrimedExplosiveEntity.create(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, 0, false,
                    explosion.getIndirectSourceEntity()));
        }
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (!level.isClientSide) {
            explodeAt(level, position.x, position.y, position.z, source);
        }
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        return detonate(level, pos) ? BombReturnCode.DETONATED : BombReturnCode.ERROR_NO_BOMB;
    }

    public boolean detonate(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != this) {
            return false;
        }
        level.removeBlock(pos, false);
        explodeAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, null);
        return true;
    }

    private void explodeAt(Level level, double x, double y, double z, @Nullable Entity source) {
        switch (kind) {
            case DYNAMITE -> {
                new ExplosionNT(level, source, x, y, z, 4.0F).explode();
                ParticleUtil.spawnExplosionSmall(level, x, y, z, 15, 3.0F, 1.25F);
            }
            case MINER -> {
                new ExplosionNT(level, source, x, y, z, 4.0F)
                        .addAttrib(ExplosionNT.ExAttrib.NOHURT)
                        .addAttrib(ExplosionNT.ExAttrib.ALLDROP)
                        .explode();
                ParticleUtil.spawnExplosionSmall(level, x, y, z, 15, 3.0F, 1.25F);
            }
            case C4 -> {
                new ExplosionVnt(level, x, y, z, 15.0F, source)
                        .setBlockAllocator(new BlockAllocatorStandard(32))
                        .setBlockProcessor(new BlockProcessorStandard().setNoDrop())
                        .setEntityProcessor(new EntityProcessorStandard())
                        .setPlayerProcessor(new PlayerProcessorStandard())
                        .explode();
                ParticleUtil.spawnExplosionSmall(level, x, y + 0.5D, z, 10, 2.5F, 1.0F);
            }
            case SEMTEX -> {
                new ExplosionVnt(level, x, y, z, 10.0F, source)
                        .setBlockAllocator(new BlockAllocatorStandard(32))
                        .setBlockProcessor(new BlockProcessorStandard().setAllDrop().setFortune(3))
                        .explode();
                ParticleUtil.spawnExplosionSmall(level, x, y + 0.5D, z, 10, 2.5F, 1.0F);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.timer").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.arm").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.defuser").withStyle(ChatFormatting.RED));
        switch (kind) {
            case MINER -> {
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.all_drop").withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.no_damage").withStyle(ChatFormatting.BLUE));
            }
            case C4 -> tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.no_drop").withStyle(ChatFormatting.BLUE));
            case SEMTEX -> {
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.all_drop").withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.no_damage").withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.literal("").withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.charge.fortune").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            default -> {
            }
        }
    }

    public enum Kind {
        DYNAMITE,
        MINER,
        C4,
        SEMTEX
    }
}
