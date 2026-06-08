package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.CloudFleijaEntity;
import com.hbm.ntm.entity.effect.CloudFleijaRainbowEntity;
import com.hbm.ntm.entity.effect.CloudSoliniumEntity;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.entity.effect.BlackHoleEntity;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.entity.effect.QuasarEntity;
import com.hbm.ntm.entity.effect.RagingVortexEntity;
import com.hbm.ntm.entity.effect.VortexEntity;
import com.hbm.ntm.entity.logic.BalefireExplosionEntity;
import com.hbm.ntm.entity.logic.DeathBlastEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.hbm.ntm.entity.missile.MinerRocketEntity;
import com.hbm.ntm.entity.missile.SoyuzCapsuleEntity;
import com.hbm.ntm.entity.missile.SoyuzEntity;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.entity.projectile.FallingNukeEntity;
import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.hbm.ntm.entity.projectile.ShrapnelEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HbmNtm.MOD_ID);

    public static final RegistryObject<EntityType<MovingItemEntity>> MOVING_ITEM =
            ENTITY_TYPES.register("entity_c_item", () -> EntityType.Builder
                    .<MovingItemEntity>of(MovingItemEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F)
                    .clientTrackingRange(64)
                    .updateInterval(3)
                    .build("entity_c_item"));

    public static final RegistryObject<EntityType<MovingPackageEntity>> MOVING_PACKAGE =
            ENTITY_TYPES.register("entity_c_package", () -> EntityType.Builder
                    .<MovingPackageEntity>of(MovingPackageEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(3)
                    .build("entity_c_package"));

    public static final RegistryObject<EntityType<NukeExplosionMk5Entity>> NUKE_EXPLOSION_MK5 =
            ENTITY_TYPES.register("entity_nuke_explosion_mk5", () -> EntityType.Builder
                    .<NukeExplosionMk5Entity>of(NukeExplosionMk5Entity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .noSummon()
                    .build("entity_nuke_explosion_mk5"));

    public static final RegistryObject<EntityType<NukeExplosionMk3Entity>> NUKE_EXPLOSION_MK3 =
            ENTITY_TYPES.register("entity_nuke_mk3", () -> EntityType.Builder
                    .<NukeExplosionMk3Entity>of(NukeExplosionMk3Entity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .noSummon()
                    .build("entity_nuke_mk3"));

    public static final RegistryObject<EntityType<FalloutRainEntity>> FALLOUT_RAIN =
            ENTITY_TYPES.register("entity_fallout_rain", () -> EntityType.Builder
                    .<FalloutRainEntity>of(FalloutRainEntity::new, MobCategory.MISC)
                    .sized(4.0F, 20.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(20)
                    .fireImmune()
                    .noSummon()
                    .build("entity_fallout_rain"));

    public static final RegistryObject<EntityType<CloudFleijaEntity>> CLOUD_FLEIJA =
            ENTITY_TYPES.register("entity_cloud_fleija", () -> EntityType.Builder
                    .<CloudFleijaEntity>of(CloudFleijaEntity::new, MobCategory.MISC)
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(500)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_cloud_fleija"));

    public static final RegistryObject<EntityType<CloudSoliniumEntity>> CLOUD_SOLINIUM =
            ENTITY_TYPES.register("entity_cloud_solinium", () -> EntityType.Builder
                    .<CloudSoliniumEntity>of(CloudSoliniumEntity::new, MobCategory.MISC)
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_cloud_solinium"));

    public static final RegistryObject<EntityType<CloudFleijaRainbowEntity>> CLOUD_FLEIJA_RAINBOW =
            ENTITY_TYPES.register("entity_cloud_rainbow", () -> EntityType.Builder
                    .<CloudFleijaRainbowEntity>of(CloudFleijaRainbowEntity::new, MobCategory.MISC)
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_cloud_rainbow"));

    public static final RegistryObject<EntityType<NukeTorexEntity>> NUKE_TOREX =
            ENTITY_TYPES.register("entity_effect_torex", () -> EntityType.Builder
                    .<NukeTorexEntity>of(NukeTorexEntity::new, MobCategory.MISC)
                    .sized(1.0F, 50.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_effect_torex"));

    public static final RegistryObject<EntityType<BlackHoleEntity>> BLACK_HOLE =
            ENTITY_TYPES.register("entity_black_hole", () -> EntityType.Builder
                    .<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_black_hole"));

    public static final RegistryObject<EntityType<VortexEntity>> VORTEX =
            ENTITY_TYPES.register("entity_vortex", () -> EntityType.Builder
                    .<VortexEntity>of(VortexEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_vortex"));

    public static final RegistryObject<EntityType<RagingVortexEntity>> RAGING_VORTEX =
            ENTITY_TYPES.register("entity_raging_vortex", () -> EntityType.Builder
                    .<RagingVortexEntity>of(RagingVortexEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_raging_vortex"));

    public static final RegistryObject<EntityType<QuasarEntity>> QUASAR =
            ENTITY_TYPES.register("entity_digamma_quasar", () -> EntityType.Builder
                    .<QuasarEntity>of(QuasarEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_digamma_quasar"));

    public static final RegistryObject<EntityType<BalefireExplosionEntity>> BALEFIRE_EXPLOSION =
            ENTITY_TYPES.register("entity_balefire", () -> EntityType.Builder
                    .<BalefireExplosionEntity>of(BalefireExplosionEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_balefire"));

    public static final RegistryObject<EntityType<DeathBlastEntity>> DEATH_BLAST =
            ENTITY_TYPES.register("entity_laser_blast", () -> EntityType.Builder
                    .<DeathBlastEntity>of(DeathBlastEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_laser_blast"));

    public static final RegistryObject<EntityType<FallingNukeEntity>> FALLING_NUKE =
            ENTITY_TYPES.register("entity_falling_nuke", () -> EntityType.Builder
                    .<FallingNukeEntity>of(FallingNukeEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("entity_falling_nuke"));

    public static final RegistryObject<EntityType<MinerRocketEntity>> MINER_ROCKET =
            ENTITY_TYPES.register("entity_miner_lander", () -> EntityType.Builder
                    .<MinerRocketEntity>of(MinerRocketEntity::new, MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("entity_miner_lander"));

    public static final RegistryObject<EntityType<SoyuzEntity>> SOYUZ =
            ENTITY_TYPES.register("entity_soyuz", () -> EntityType.Builder
                    .<SoyuzEntity>of(SoyuzEntity::new, MobCategory.MISC)
                    .sized(5.0F, 50.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_soyuz"));

    public static final RegistryObject<EntityType<SoyuzCapsuleEntity>> SOYUZ_CAPSULE =
            ENTITY_TYPES.register("entity_soyuz_capsule", () -> EntityType.Builder
                    .<SoyuzCapsuleEntity>of(SoyuzCapsuleEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.0F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("entity_soyuz_capsule"));

    public static final RegistryObject<EntityType<ShrapnelEntity>> SHRAPNEL =
            ENTITY_TYPES.register("entity_shrapnel", () -> EntityType.Builder
                    .<ShrapnelEntity>of(ShrapnelEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_shrapnel"));

    public static final RegistryObject<EntityType<RubbleEntity>> RUBBLE =
            ENTITY_TYPES.register("entity_rubble", () -> EntityType.Builder
                    .<RubbleEntity>of(RubbleEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("entity_rubble"));

    public static final RegistryObject<EntityType<BulletProjectileEntity>> BULLET_PROJECTILE =
            ENTITY_TYPES.register("entity_bullet_base_nt", () -> EntityType.Builder
                    .<BulletProjectileEntity>of(BulletProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("entity_bullet_base_nt"));

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    private ModEntityTypes() {
    }
}
