package com.hbm.ntm.event;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.PlayerRadiationSyncPacket;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.ItemRadiationRegistry;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonForgeEvents {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            ChunkRadiationManager.tick(level);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (player.tickCount % 20 != 0) {
            return;
        }

        float itemRadiation = 0.0F;
        for (ItemStack stack : player.getInventory().items) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount();
        }
        for (ItemStack stack : player.getInventory().armor) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount();
        }
        for (ItemStack stack : player.getInventory().offhand) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount();
        }

        if (itemRadiation > 0.0F) {
            RadiationUtil.contaminate(player, itemRadiation, false);
        }

        float totalRadiation = RadiationData.getRadiation(player);
        float chunkRadiation = ChunkRadiationManager.getRadiation(player.level(), player.blockPosition());

        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(new PlayerRadiationSyncPacket(
                    totalRadiation,
                    RadiationData.getDigamma(player),
                    RadiationData.getRadBuf(player),
                    chunkRadiation,
                    HazmatRegistry.getResistance(player)), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || entity.isDeadOrDying()) {
            return;
        }

        if (entity.tickCount % 20 == 0) {
            RadiationData.setRadBuf(entity, RadiationData.getRadEnv(entity));
            RadiationData.setRadEnv(entity, 0.0F);
        }

        handleChunkRadiation(entity);
        handleLegacyRadiationEffects(entity);
        handleDigammaEffects(entity);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        RadiationData.copyForRespawn(event.getOriginal(), event.getEntity());
        RadiationData.applyDigammaModifier(event.getEntity());
    }

    private static void handleChunkRadiation(LivingEntity entity) {
        if (RadiationUtil.isRadImmune(entity)) {
            return;
        }

        float chunkRadiation = ChunkRadiationManager.getRadiation(entity.level(), entity.blockPosition());
        if (chunkRadiation > 0.0F) {
            RadiationUtil.contaminate(entity, chunkRadiation / 20.0F, false);
        }
    }

    private static void handleLegacyRadiationEffects(LivingEntity entity) {
        if ((entity instanceof Player player && player.isCreative()) || entity.isDeadOrDying()) {
            return;
        }

        handleLegacyRadiationTransformations(entity);

        float radiation = RadiationData.getRadiation(entity);
        if (radiation < 200.0F) {
            return;
        }
        if (RadiationUtil.isRadImmune(entity)) {
            return;
        }
        if (radiation > 2500.0F) {
            RadiationData.setRadiation(entity, 2500.0F);
        }

        if (radiation >= 1000.0F) {
            entity.hurt(ModDamageSources.radiation(entity.level()), 1000.0F);
            RadiationData.setRadiation(entity, 0.0F);
            if (entity.isAlive()) {
                entity.setHealth(0.0F);
            }
        } else if (radiation >= 800.0F) {
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 30, 0);
            addRandomEffect(entity, 300, MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2);
            addRandomEffect(entity, 300, MobEffects.WEAKNESS, 10 * 20, 2);
            addRandomEffect(entity, 500, MobEffects.POISON, 3 * 20, 2);
            addRandomEffect(entity, 700, MobEffects.WITHER, 3 * 20, 1);
        } else if (radiation >= 600.0F) {
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 30, 0);
            addRandomEffect(entity, 300, MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2);
            addRandomEffect(entity, 300, MobEffects.WEAKNESS, 10 * 20, 2);
            addRandomEffect(entity, 500, MobEffects.POISON, 3 * 20, 1);
        } else if (radiation >= 400.0F) {
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 30, 0);
            addRandomEffect(entity, 500, MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0);
            addRandomEffect(entity, 300, MobEffects.WEAKNESS, 5 * 20, 1);
        } else {
            RadiationUtil.addRadiationPoisoning(entity, 20 * 10, 0);
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 20, 0);
            addRandomEffect(entity, 500, MobEffects.WEAKNESS, 5 * 20, 0);
        }

        handleRadiationParticles(entity, radiation);
    }

    private static void handleLegacyRadiationTransformations(LivingEntity entity) {
        float radiation = RadiationData.getRadiation(entity);
        if (radiation >= 200.0F && entity.getClass().equals(Creeper.class)) {
            entity.hurt(ModDamageSources.radiation(entity.level()), 100.0F);
        } else if (radiation >= 50.0F && entity instanceof Cow cow && !(entity instanceof MushroomCow) && cow.level() instanceof ServerLevel level) {
            MushroomCow mushroomCow = EntityType.MOOSHROOM.create(level);
            if (mushroomCow != null) {
                mushroomCow.moveTo(cow.getX(), cow.getY(), cow.getZ(), cow.getYRot(), cow.getXRot());
                level.addFreshEntity(mushroomCow);
                cow.discard();
            }
        } else if (radiation >= 500.0F && entity instanceof Villager villager && villager.level() instanceof ServerLevel level) {
            Zombie zombie = EntityType.ZOMBIE.create(level);
            if (zombie != null) {
                zombie.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
                level.addFreshEntity(zombie);
                villager.discard();
            }
        }
    }

    private static void handleRadiationParticles(LivingEntity entity, float radiation) {
        if (!(entity.level() instanceof ServerLevel level) || (entity instanceof Player player && player.isCreative())) {
            return;
        }

        long time = level.getGameTime();
        int seed = entity.getId();
        if (radiation > 600.0F && (time + Math.floorMod(seed * 31, 600)) % 600 < 20) {
            spawnVomit(level, entity, true);
            if ((time + Math.floorMod(seed * 31, 600)) % 600 == 1) {
                playVomit(level, entity);
            }
        } else if (radiation > 200.0F && (time + Math.floorMod(seed * 17, 1200)) % 1200 < 20) {
            spawnVomit(level, entity, false);
            if ((time + Math.floorMod(seed * 17, 1200)) % 1200 == 1) {
                playVomit(level, entity);
            }
        }

        if (radiation > 900.0F && entity.getRandom().nextInt(10) == 0) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    1, 0.25D, 0.35D, 0.25D, 0.02D);
        }
    }

    private static void handleDigammaEffects(LivingEntity entity) {
        RadiationData.applyDigammaModifier(entity);
        float digamma = RadiationData.getDigamma(entity);
        if (digamma < 0.01F) {
            return;
        }

        if ((entity.getMaxHealth() <= 0.0F || digamma >= 10.0F) && entity.isAlive()) {
            entity.setAbsorptionAmount(0.0F);
            entity.hurt(ModDamageSources.digamma(entity.level()), 500.0F);
            entity.setHealth(0.0F);
        }

        int chance = Math.max(10 - (int) digamma, 1);
        if (entity.level() instanceof ServerLevel serverLevel
                && (chance == 1 || entity.getRandom().nextInt(chance) == 0)) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SOUL_SAND.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    1, 0.25D, 0.35D, 0.25D, 0.02D);
        }
    }

    private static void spawnVomit(ServerLevel level, LivingEntity entity, boolean blood) {
        if (blood) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.45D, entity.getZ(),
                    25, 0.2D, 0.2D, 0.2D, 0.04D);
        } else {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SLIME_BALL)),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.45D, entity.getZ(),
                    15, 0.2D, 0.2D, 0.2D, 0.04D);
        }
    }

    private static void playVomit(ServerLevel level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), com.hbm.ntm.registry.ModSounds.PLAYER_VOMIT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 19));
    }

    private static void addRandomEffect(LivingEntity entity, int chance, net.minecraft.world.effect.MobEffect effect, int duration, int amplifier) {
        if (entity.getRandom().nextInt(chance) == 0) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private CommonForgeEvents() {
    }
}
