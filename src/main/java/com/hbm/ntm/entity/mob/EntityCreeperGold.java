package com.hbm.ntm.entity.mob;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorBulkie;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorBulkie;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectStandard;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;

/** Exact 1.7.10 EntityCreeperGold behavior. */
public class EntityCreeperGold extends Creeper {
    public EntityCreeperGold(EntityType<? extends EntityCreeperGold> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return super.checkSpawnRules(level, spawnType) && level().dimension().equals(Level.OVERWORLD)
                && getY() <= 40.0D;
    }

    @Override
    protected void explodeCreeper() {
        if (level().isClientSide()) {
            return;
        }
        discard();
        int resolution = isPowered() ? 32 : 16;
        float radius = isPowered() ? 14.0F : 7.0F;
        new ExplosionVnt(level(), getX(), getY(), getZ(), radius, this, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY)
                .setBlockAllocator(new BlockAllocatorBulkie(60.0D, resolution))
                .setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBulkie(Blocks.GOLD_ORE)))
                .setEntityProcessor(new EntityProcessorStandard().withRangeMod(0.5F))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setEffects(new ExplosionEffectStandard())
                .explode();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        int count = recentlyHit ? 5 + random.nextInt(6 + looting * 2) : 3;
        RegistryObject<Item> item = ModItems.legacyItem("crystal_gold");
        if (item == null) {
            return;
        }
        for (int i = 0; i < count; i++) {
            spawnAtLocation(new ItemStack(item.get()));
        }
    }
}
