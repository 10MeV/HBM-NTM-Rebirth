package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class LegacyBarbedWireBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape NORTH_SOUTH_SHAPE = box(0.0D, 0.0D, 5.0D, 16.0D, 16.0D, 11.0D);
    private static final VoxelShape EAST_WEST_SHAPE = box(5.0D, 0.0D, 0.0D, 11.0D, 16.0D, 16.0D);
    private static final Vec3 STUCK_SPEED = new Vec3(0.15D, 0.1D, 0.15D);
    private final Variant variant;

    public LegacyBarbedWireBlock(BlockBehaviour.Properties properties) {
        this(properties, Variant.NORMAL);
    }

    public LegacyBarbedWireBlock(BlockBehaviour.Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int quadrant = LegacyDirectionalShapeBlock.legacyYawQuadrant(context.getRotation());
        Direction facing = switch (quadrant) {
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.EAST;
            default -> Direction.SOUTH;
        };
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, STUCK_SPEED);
        if (level.isClientSide) {
            return;
        }

        switch (variant) {
            case NORMAL -> entity.hurt(level.damageSources().cactus(), 2.0F);
            case FIRE -> {
                entity.hurt(level.damageSources().cactus(), 2.0F);
                entity.setSecondsOnFire(1);
            }
            case POISON -> {
                entity.hurt(level.damageSources().cactus(), 2.0F);
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 5 * 20, 2));
                }
            }
            case ACID -> {
                entity.hurt(level.damageSources().cactus(), 2.0F);
                if (entity instanceof Player player) {
                    ArmorUtil.damageSuitAll(player, 1);
                }
            }
            case WITHER -> {
                entity.hurt(level.damageSources().cactus(), 2.0F);
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.WITHER, 5 * 20, 4));
                }
            }
            case ULTRADEATH -> {
                entity.hurt(ModDamageSources.pc(level), 5.0F);
                if (entity instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(ModEffects.RADIATION.get(), 5 * 20, 9));
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? EAST_WEST_SHAPE : NORTH_SOUTH_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public enum Variant {
        NORMAL,
        FIRE,
        POISON,
        ACID,
        WITHER,
        ULTRADEATH
    }
}
