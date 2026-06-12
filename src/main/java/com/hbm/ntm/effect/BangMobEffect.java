package com.hbm.ntm.effect;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class BangMobEffect extends MobEffect {
    private static final ResourceLocation CHEESE_ID = new ResourceLocation(HbmNtm.MOD_ID, "cheese");

    public BangMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x111111);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }

        EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.source(entity.level(), ModDamageSources.BANG), 1000.0F);
        entity.setHealth(0.0F);
        if (!(entity instanceof Player)) {
            entity.discard();
        }
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                ModSounds.WEAPON_LASER_BANG.get(), SoundSource.HOSTILE, 100.0F, 1.0F);
        ExplosionLarge.spawnParticles(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 10);
        dropCowCheese(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration <= 10;
    }

    private static void dropCowCheese(LivingEntity entity) {
        if (!(entity instanceof Cow cow)) {
            return;
        }
        Item cheese = ForgeRegistries.ITEMS.getValue(CHEESE_ID);
        if (cheese == null || cheese == Items.AIR) {
            return;
        }
        cow.spawnAtLocation(new ItemStack(cheese, cow.isBaby() ? 10 : 3), 1.0F);
    }
}
