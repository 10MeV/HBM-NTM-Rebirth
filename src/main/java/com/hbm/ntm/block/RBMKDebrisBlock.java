package com.hbm.ntm.block;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.AddBlockPlan;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisBlockAction;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisBlockActionType;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisBlockState;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisBlockTickPlan;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisNeighborKind;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.NeighborSample;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmBlockStateUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("deprecation")
public class RBMKDebrisBlock extends Block {
    private final Kind kind;

    public RBMKDebrisBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level.isClientSide || state.is(oldState.getBlock())) {
            return;
        }
        RandomSource random = level.getRandom();
        if (kind == Kind.BURNING || kind == Kind.RADIATING) {
            AddBlockPlan plan = RBMKDebrisBlockPlanner.planBurningAdded(pos,
                    random.nextInt(3) == 0,
                    random.nextInt(RBMKDebrisBlockPlanner.BURNING_TICK_RATE_RANDOM));
            executeActions((ServerLevel) level, state, pos, plan.actions(), random);
            schedule(level, pos, plan.scheduledTickDelay());
        } else if (kind == Kind.DIGAMMA) {
            schedule(level, pos, RBMKDebrisBlockPlanner.planDigammaAdded().scheduledTickDelay());
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.BURNING) {
            tickBurning(state, level, pos, random);
        } else if (kind == Kind.DIGAMMA) {
            tickDigamma(state, level, pos);
        }
    }

    protected void tickBurning(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        DebrisNeighborKind neighborKind = neighborKind(level.getBlockState(pos.relative(direction)));
        DebrisBlockTickPlan plan = RBMKDebrisBlockPlanner.planBurningTick(
                pos,
                direction,
                neighborKind,
                random.nextInt(RBMKDebrisBlockPlanner.FLAME_RANDOM_DENOMINATOR) == 0,
                random.nextInt(RBMKDebrisBlockPlanner.MELTDOWN_GAS_RANDOM_DENOMINATOR) == 0,
                random.nextInt(RBMKDebrisBlockPlanner.burningExtinguishChance(neighborKind)) == 0,
                random.nextInt(RBMKDebrisBlockPlanner.BURNING_TICK_RATE_RANDOM));
        executeActions(level, state, pos, plan.actions(), random);
        if (plan.scheduleNextTick()) {
            schedule(level, pos, plan.nextTickDelay());
        }
    }

    private void tickDigamma(BlockState state, ServerLevel level, BlockPos pos) {
        List<NeighborSample> samples = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            samples.add(new NeighborSample(direction, neighborKind(level.getBlockState(pos.relative(direction)))));
        }
        executeActions(level, state, pos, RBMKDebrisBlockPlanner.planDigammaSpread(pos, samples), level.random);
    }

    protected void executeActions(ServerLevel level, BlockState state, BlockPos origin, List<DebrisBlockAction> actions,
            RandomSource random) {
        for (DebrisBlockAction action : actions) {
            if (action.type() == DebrisBlockActionType.SET_BLOCK) {
                BlockState target = stateFor(action.state());
                if (target != null) {
                    level.setBlock(action.pos(), target, Block.UPDATE_ALL);
                }
            } else if (action.type() == DebrisBlockActionType.SPAWN_FLAME) {
                spawnRbmkFlame(level, origin, random);
                if (action.playFireSound()) {
                    level.playSound(null, origin, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                            1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F);
                }
            } else if (action.type() == DebrisBlockActionType.RADIATE) {
                radiate(level, origin);
            }
        }
    }

    protected void spawnRbmkFlame(ServerLevel level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
        double y = pos.getY() + 1.75D;
        double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
        ParticleUtil.spawnRbmkFlame(level, x, y, z, RBMKDebrisBlockPlanner.FLAME_MAX_AGE);
    }

    protected void radiate(Level level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(center, center).inflate(RBMKDebrisBlockPlanner.RADIATING_RANGE);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);

        for (LivingEntity entity : entities) {
            Vec3 eyes = entity.getEyePosition();
            Vec3 delta = eyes.subtract(center);
            double length = delta.length();
            if (length <= 0.0D) {
                continue;
            }

            Vec3 normal = delta.normalize();
            float resistance = 0.0F;
            for (int i = 1; i < length; i++) {
                BlockPos sample = BlockPos.containing(center.add(normal.scale(i)));
                resistance += HbmBlockStateUtil.explosionResistance(level.getBlockState(sample), level, sample);
            }
            if (resistance < 1.0F) {
                resistance = 1.0F;
            }

            float exposure = RBMKDebrisBlockPlanner.RADIATING_BASE_RADS / resistance / (float) (length * length);
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, exposure);
            if (length < RBMKDebrisBlockPlanner.RADIATING_FIRE_DAMAGE_RANGE) {
                EntityDamageUtil.attackEntityFromNt(entity, level.damageSources().inFire(), 100.0F);
            }
        }
    }

    protected static void schedule(Level level, BlockPos pos, int delay) {
        if (delay > 0) {
            level.scheduleTick(pos, level.getBlockState(pos).getBlock(), delay);
        }
    }

    protected static DebrisNeighborKind neighborKind(BlockState state) {
        if (state.isAir()) {
            return DebrisNeighborKind.AIR;
        }
        if (state.is(ModBlocks.BLOCK_FOAM.get())) {
            return DebrisNeighborKind.FOAM;
        }
        if (state.is(ModBlocks.SAND_BORON.get())) {
            return DebrisNeighborKind.BORON;
        }
        if (state.is(ModBlocks.PRIBRIS.get())) {
            return DebrisNeighborKind.PRIBRIS;
        }
        if (state.is(ModBlocks.PRIBRIS_BURNING.get())) {
            return DebrisNeighborKind.PRIBRIS_BURNING;
        }
        if (state.is(ModBlocks.PRIBRIS_RADIATING.get())) {
            return DebrisNeighborKind.PRIBRIS_RADIATING;
        }
        if (state.is(ModBlocks.PRIBRIS_DIGAMMA.get())) {
            return DebrisNeighborKind.DEBRIS;
        }
        RegistryObject<? extends Block> corium = ModBlocks.legacyBlock("block_corium");
        RegistryObject<? extends Block> coriumCobble = ModBlocks.legacyBlock("block_corium_cobble");
        if ((corium != null && state.is(corium.get())) || (coriumCobble != null && state.is(coriumCobble.get()))) {
            return DebrisNeighborKind.CORIUM_BLOCK;
        }
        return OTHER_OR_DEBRIS(state);
    }

    private static DebrisNeighborKind OTHER_OR_DEBRIS(BlockState state) {
        return state.getBlock() instanceof RBMKDebrisBlock ? DebrisNeighborKind.DEBRIS : DebrisNeighborKind.OTHER;
    }

    protected static BlockState stateFor(DebrisBlockState state) {
        return switch (state) {
            case PRIBRIS -> ModBlocks.PRIBRIS.get().defaultBlockState();
            case PRIBRIS_BURNING -> ModBlocks.PRIBRIS_BURNING.get().defaultBlockState();
            case PRIBRIS_RADIATING -> ModBlocks.PRIBRIS_RADIATING.get().defaultBlockState();
            case PRIBRIS_DIGAMMA -> ModBlocks.PRIBRIS_DIGAMMA.get().defaultBlockState();
            case MELTDOWN_GAS -> ModBlocks.GAS_MELTDOWN.get().defaultBlockState();
        };
    }

    public enum Kind {
        INERT,
        BURNING,
        RADIATING,
        DIGAMMA
    }
}
