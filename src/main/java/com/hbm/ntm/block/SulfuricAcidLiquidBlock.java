package com.hbm.ntm.block;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.util.AchievementHandler;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

public class SulfuricAcidLiquidBlock extends LiquidBlock {
    private static final float ENTITY_DAMAGE = 5.0F;
    private static final float ITEM_DAMAGE = ENTITY_DAMAGE * 0.1F;

    public SulfuricAcidLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (entity instanceof ItemEntity itemEntity) {
            collideItem(level, itemEntity);
        } else {
            collideEntity(level, entity);
        }
    }

    private static void collideItem(Level level, ItemEntity itemEntity) {
        itemEntity.setDeltaMovement(Vec3.ZERO);
        if (level instanceof ServerLevel serverLevel) {
            if (itemEntity.tickCount % 20 == 0) {
                boolean wasSlimeBall = itemEntity.getItem().is(Items.SLIME_BALL);
                boolean damaged = EntityDamageUtil.attackEntityFromNt(itemEntity,
                        ModDamageSources.source(level, ModDamageSources.ACID), ITEM_DAMAGE);
                if (wasSlimeBall && damaged && itemEntity.isRemoved()) {
                    for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class,
                            itemEntity.getBoundingBox().inflate(10.0D))) {
                        AchievementHandler.award(player, AchievementHandler.SULFURIC);
                    }
                }
            }
            if (itemEntity.tickCount % 5 == 0) {
                serverLevel.sendParticles(ParticleTypes.CLOUD, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                playFizz(level, itemEntity);
            }
        }
    }

    private static void collideEntity(Level level, Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        if (movement.y < -0.2D) {
            entity.setDeltaMovement(movement.x, movement.y * 0.5D, movement.z);
        }
        if (level instanceof ServerLevel) {
            EntityDamageUtil.attackEntityFromNt(entity,
                    ModDamageSources.source(level, ModDamageSources.ACID), ENTITY_DAMAGE);
            if (entity.tickCount % 5 == 0) {
                playFizz(level, entity);
            }
        }
    }

    private static void playFizz(Level level, Entity entity) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 1.0F);
    }
}
