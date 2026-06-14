package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.CloudFleijaEntity;
import com.hbm.ntm.entity.effect.CloudFleijaRainbowEntity;
import com.hbm.ntm.entity.effect.CloudSoliniumEntity;
import com.hbm.ntm.entity.effect.EmpBlastEntity;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.entity.effect.FireLingeringEntity;
import com.hbm.ntm.entity.effect.BlackHoleEntity;
import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.entity.effect.QuasarEntity;
import com.hbm.ntm.entity.effect.RagingVortexEntity;
import com.hbm.ntm.entity.effect.VortexEntity;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.entity.logic.BalefireExplosionEntity;
import com.hbm.ntm.entity.logic.DeathBlastEntity;
import com.hbm.ntm.entity.logic.EmpLogicEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.hbm.ntm.entity.missile.AntiBallisticMissileEntity;
import com.hbm.ntm.entity.missile.CustomMissileEntity;
import com.hbm.ntm.entity.missile.MinerRocketEntity;
import com.hbm.ntm.entity.missile.MissileEntity;
import com.hbm.ntm.entity.missile.SoyuzCapsuleEntity;
import com.hbm.ntm.entity.missile.SoyuzEntity;
import com.hbm.ntm.entity.projectile.ArtilleryRocketEntity;
import com.hbm.ntm.entity.projectile.ArtilleryShellEntity;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.entity.projectile.ChemicalProjectileEntity;
import com.hbm.ntm.entity.projectile.CoinEntity;
import com.hbm.ntm.entity.projectile.DynamiteStickEntity;
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

    public static final RegistryObject<EntityType<EmpBlastEntity>> EMP_BLAST =
            ENTITY_TYPES.register("entity_emp_blast", () -> EntityType.Builder
                    .<EmpBlastEntity>of(EmpBlastEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_emp_blast"));

    public static final RegistryObject<EntityType<FireLingeringEntity>> FIRE_LINGERING =
            ENTITY_TYPES.register("entity_fire_lingering", () -> EntityType.Builder
                    .<FireLingeringEntity>of(FireLingeringEntity::new, MobCategory.MISC)
                    .sized(6.0F, 2.0F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_fire_lingering"));

    public static final RegistryObject<EntityType<MistEntity>> MIST =
            ENTITY_TYPES.register("entity_mist", () -> EntityType.Builder
                    .<MistEntity>of(MistEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .noSummon()
                    .build("entity_mist"));

    public static final RegistryObject<EntityType<EmpLogicEntity>> EMP_LOGIC =
            ENTITY_TYPES.register("entity_emp_logic", () -> EntityType.Builder
                    .<EmpLogicEntity>of(EmpLogicEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(256)
                    .updateInterval(20)
                    .noSummon()
                    .build("entity_emp_logic"));

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

    public static final RegistryObject<EntityType<LegacyPrimedExplosiveEntity>> LEGACY_PRIMED_EXPLOSIVE =
            ENTITY_TYPES.register("entity_tnt_primed_base", () -> EntityType.Builder
                    .<LegacyPrimedExplosiveEntity>of(LegacyPrimedExplosiveEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(160)
                    .updateInterval(1)
                    .build("entity_tnt_primed_base"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_GENERIC =
            ENTITY_TYPES.register("entity_missile_generic", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.GENERIC),
                            MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_generic"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DECOY =
            ENTITY_TYPES.register("entity_missile_decoy", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DECOY),
                            MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_decoy"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_INCENDIARY =
            ENTITY_TYPES.register("entity_missile_incendiary", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.INCENDIARY),
                            MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_incendiary"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_CLUSTER =
            ENTITY_TYPES.register("entity_missile_cluster", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.CLUSTER),
                            MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_cluster"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BUSTER =
            ENTITY_TYPES.register("entity_missile_buster", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BUSTER),
                            MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_buster"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_STRONG =
            ENTITY_TYPES.register("entity_missile_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.STRONG),
                            MobCategory.MISC)
                    .sized(1.25F, 4.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_INCENDIARY_STRONG =
            ENTITY_TYPES.register("entity_missile_incendiary_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.INCENDIARY_STRONG),
                            MobCategory.MISC)
                    .sized(1.25F, 4.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_incendiary_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_CLUSTER_STRONG =
            ENTITY_TYPES.register("entity_missile_cluster_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.CLUSTER_STRONG),
                            MobCategory.MISC)
                    .sized(1.25F, 4.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_cluster_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BUSTER_STRONG =
            ENTITY_TYPES.register("entity_missile_buster_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BUSTER_STRONG),
                            MobCategory.MISC)
                    .sized(1.25F, 4.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_buster_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_EMP_STRONG =
            ENTITY_TYPES.register("entity_missile_emp_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.EMP_STRONG),
                            MobCategory.MISC)
                    .sized(1.25F, 4.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_emp_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BURST =
            ENTITY_TYPES.register("entity_missile_burst", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BURST),
                            MobCategory.MISC)
                    .sized(1.5F, 5.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_burst"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_INFERNO =
            ENTITY_TYPES.register("entity_missile_inferno", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.INFERNO),
                            MobCategory.MISC)
                    .sized(1.5F, 5.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_inferno"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_RAIN =
            ENTITY_TYPES.register("entity_missile_rain", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.RAIN),
                            MobCategory.MISC)
                    .sized(1.5F, 5.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_rain"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DRILL =
            ENTITY_TYPES.register("entity_missile_drill", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DRILL),
                            MobCategory.MISC)
                    .sized(1.5F, 5.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_drill"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_STEALTH =
            ENTITY_TYPES.register("entity_missile_stealth", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.STEALTH),
                            MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_stealth"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_EMP =
            ENTITY_TYPES.register("entity_missile_emp", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.EMP),
                            MobCategory.MISC)
                    .sized(0.75F, 2.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_emp"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_MICRO =
            ENTITY_TYPES.register("entity_missile_micronuclear", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.MICRO),
                            MobCategory.MISC)
                    .sized(0.75F, 2.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_micronuclear"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_SCHRABIDIUM =
            ENTITY_TYPES.register("entity_missile_schrabidium", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.SCHRABIDIUM),
                            MobCategory.MISC)
                    .sized(0.75F, 2.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_schrabidium"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BHOLE =
            ENTITY_TYPES.register("entity_missile_blackhole", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BHOLE),
                            MobCategory.MISC)
                    .sized(0.75F, 2.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_blackhole"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_TAINT =
            ENTITY_TYPES.register("entity_missile_taint", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.TAINT),
                            MobCategory.MISC)
                    .sized(0.75F, 2.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_taint"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_NUCLEAR =
            ENTITY_TYPES.register("entity_missile_nuclear", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.NUCLEAR),
                            MobCategory.MISC)
                    .sized(2.0F, 7.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_nuclear"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_NUCLEAR_CLUSTER =
            ENTITY_TYPES.register("entity_missile_mirv", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.MIRV),
                            MobCategory.MISC)
                    .sized(2.0F, 7.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_mirv"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_VOLCANO =
            ENTITY_TYPES.register("entity_missile_volcano", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.VOLCANO),
                            MobCategory.MISC)
                    .sized(2.0F, 7.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_volcano"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DOOMSDAY =
            ENTITY_TYPES.register("entity_missile_doomsday", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DOOMSDAY),
                            MobCategory.MISC)
                    .sized(2.0F, 7.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_doomsday"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DOOMSDAY_RUSTED =
            ENTITY_TYPES.register("entity_missile_doomsday_rusted", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DOOMSDAY_RUSTED),
                            MobCategory.MISC)
                    .sized(2.0F, 7.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_doomsday_rusted"));

    public static final RegistryObject<EntityType<AntiBallisticMissileEntity>> MISSILE_ANTI_BALLISTIC =
            ENTITY_TYPES.register("entity_missile_anti", () -> EntityType.Builder
                    .<AntiBallisticMissileEntity>of(AntiBallisticMissileEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_anti"));

    public static final RegistryObject<EntityType<CustomMissileEntity>> MISSILE_CUSTOM =
            ENTITY_TYPES.register("entity_missile_custom", () -> EntityType.Builder
                    .<CustomMissileEntity>of(CustomMissileEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_missile_custom"));

    public static final RegistryObject<EntityType<MinerRocketEntity>> MINER_ROCKET =
            ENTITY_TYPES.register("entity_miner_lander", () -> EntityType.Builder
                    .<MinerRocketEntity>of(MinerRocketEntity::new, MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(1000)
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
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .fireImmune()
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

    public static final RegistryObject<EntityType<ArtilleryShellEntity>> ARTILLERY_SHELL =
            ENTITY_TYPES.register("entity_artillery_shell", () -> EntityType.Builder
                    .<ArtilleryShellEntity>of(ArtilleryShellEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_artillery_shell"));

    public static final RegistryObject<EntityType<ArtilleryRocketEntity>> ARTILLERY_ROCKET =
            ENTITY_TYPES.register("entity_himars", () -> EntityType.Builder
                    .<ArtilleryRocketEntity>of(ArtilleryRocketEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .build("entity_himars"));

    public static final RegistryObject<EntityType<ChemicalProjectileEntity>> CHEMICAL_PROJECTILE =
            ENTITY_TYPES.register("entity_chemthrower_splash", () -> EntityType.Builder
                    .<ChemicalProjectileEntity>of(ChemicalProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_chemthrower_splash"));

    public static final RegistryObject<EntityType<DynamiteStickEntity>> DYNAMITE_STICK =
            ENTITY_TYPES.register("entity_dynamite_stick", () -> EntityType.Builder
                    .<DynamiteStickEntity>of(DynamiteStickEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("entity_dynamite_stick"));

    public static final RegistryObject<EntityType<CoinEntity>> COIN =
            ENTITY_TYPES.register("entity_coin", () -> EntityType.Builder
                    .<CoinEntity>of(CoinEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("entity_coin"));

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    private ModEntityTypes() {
    }
}
