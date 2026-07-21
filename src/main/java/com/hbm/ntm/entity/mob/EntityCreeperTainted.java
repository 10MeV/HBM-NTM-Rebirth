package com.hbm.ntm.entity.mob;

import com.hbm.ntm.api.entity.IRadiationImmune;
import com.hbm.ntm.block.LegacyTaintBlock;
import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EntityCreeperTainted extends Creeper implements IRadiationImmune {
    public EntityCreeperTainted(EntityType<? extends EntityCreeperTainted> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes()
                .add(Attributes.MAX_HEALTH, 15.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isAlive() && getHealth() < getMaxHealth() && tickCount % 10 == 0) {
            heal(1.0F);
        }
    }

    @Override
    protected void explodeCreeper() {
        if (!(level() instanceof ServerLevel level)) {
            return;
        }

        discard();
        double x = getX();
        double y = getY();
        double z = getZ();
        level.explode(this, x, y, z, 5.0F, false, Level.ExplosionInteraction.NONE);

        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        if (isPowered()) {
            taintNearbyBlocks(level, 255, 15, 7, ServerConfig.taintTrailsEnabled() ? 0 : 5);
        } else {
            taintNearbyBlocks(level, 85, 7, 3, ServerConfig.taintTrailsEnabled() ? 4 : 10);
        }
    }

    private void taintNearbyBlocks(ServerLevel level, int attempts, int bound, int offset, int levelBase) {
        int levelRange = ServerConfig.taintTrailsEnabled() ? 3 : isPowered() ? 3 : 6;
        for (int i = 0; i < attempts; i++) {
            BlockPos pos = new BlockPos(
                    level.random.nextInt(bound) + (int) getX() - offset,
                    level.random.nextInt(bound) + (int) getY() - offset,
                    level.random.nextInt(bound) + (int) getZ() - offset);
            if (level.isOutsideBuildHeight(pos)) {
                continue;
            }
            BlockState target = level.getBlockState(pos);
            if (target.isAir() || !target.isCollisionShapeFullBlock(level, pos)) {
                continue;
            }
            int taintLevel = levelBase + level.random.nextInt(levelRange);
            level.setBlock(pos, LegacyTaintBlock.stateForLevel(taintLevel), Block.UPDATE_ALL);
        }
    }
}
