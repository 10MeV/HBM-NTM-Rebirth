package com.hbm.ntm.blockentity;
import com.hbm.entity.particle.EntityOrangeFX;
import com.hbm.ntm.entity.projectile.ShrapnelEntity;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class LegacyGeysirBlockEntity extends BlockEntity {
    private static final double NETHER_PLAYER_RANGE = 32.0D;

    private int timer;

    public LegacyGeysirBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEYSIR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyGeysirBlockEntity geysir) {
        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }

        if (--geysir.timer <= 0) {
            geysir.timer = geysir.delay(state, level);
            level.setBlock(pos, state.cycle(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED), 2);
        }

        // The legacy tile deliberately performs with the state observed before toggling its metadata.
        if (!state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED)) {
            return;
        }

        if (state.is(ModBlocks.GEYSIR_CHLORINE.get())) {
            geysir.chlorine(level, pos);
        } else if (state.is(ModBlocks.GEYSIR_NETHER.get())) {
            geysir.fire(level, pos);
        }
    }

    private void chlorine(Level level, BlockPos pos) {
        for (int i = 0; i < 3; i++) {
            EntityOrangeFX cloud = new EntityOrangeFX(level,
                    pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
            cloud.setDeltaMovement(level.random.nextGaussian() * 0.45D,
                    timer * 0.3D, level.random.nextGaussian() * 0.45D);
            level.addFreshEntity(cloud);
        }
    }

    private void fire(Level level, BlockPos pos) {
        AABB playerRange = new AABB(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D).inflate(NETHER_PLAYER_RANGE);
        if (level.getEntitiesOfClass(Player.class, playerRange).isEmpty()) {
            return;
        }

        if (level.random.nextInt(3) == 0) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(level);
            shrapnel.setPos(pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
            shrapnel.setDeltaMovement(level.random.nextGaussian() * 0.05D,
                    0.5D + level.random.nextDouble() * timer * 0.01D,
                    level.random.nextGaussian() * 0.05D);
            level.addFreshEntity(shrapnel);
        }

        if (timer % 2 == 0) {
            ParticleUtil.spawnGeysirGasFlame(level, pos);
        }
    }

    private int delay(BlockState state, Level level) {
        boolean active = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED);
        if (state.is(ModBlocks.GEYSIR_CHLORINE.get())) {
            return active ? 400 + level.random.nextInt(100) : 20;
        }
        if (state.is(ModBlocks.GEYSIR_NETHER.get())) {
            return active ? 80 + level.random.nextInt(60) : (level.random.nextBoolean() ? 300 : 450);
        }
        return 0;
    }
}
