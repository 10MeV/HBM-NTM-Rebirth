package com.hbm.ntm.entity.mob;

import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

/** Direct 1.20.1 migration of the block-bodied legacy {@code EntityBlockSpider}. */
public final class EntityBlockSpider extends Monster {
    private static final EntityDataAccessor<BlockState> BODY_STATE =
            SynchedEntityData.defineId(EntityBlockSpider.class, EntityDataSerializers.BLOCK_STATE);

    public EntityBlockSpider(EntityType<? extends EntityBlockSpider> type, Level level) {
        super(type, level);
        getNavigation().setCanFloat(false);
        setPathfindingMalus(BlockPathTypes.WATER, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        // Old watcher block id 1 resolves to stone; BlockState replaces the old id + metadata pair.
        entityData.define(BODY_STATE, Blocks.STONE.defaultBlockState());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RandomStrollGoal(this, 0.5D));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Modern form of legacy {@code makeBlock(Block, int)}; all former metadata lives in the BlockState. */
    public void makeBlock(BlockState state) {
        BlockState resolved = state == null ? Blocks.STONE.defaultBlockState() : state;
        entityData.set(BODY_STATE, resolved);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1.0D, resolved.getBlock().getExplosionResistance()));
        setHealth(getMaxHealth());
    }

    public BlockState bodyState() {
        BlockState state = entityData.get(BODY_STATE);
        return state == null ? Blocks.STONE.defaultBlockState() : state;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
}
