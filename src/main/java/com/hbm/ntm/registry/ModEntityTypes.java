package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.CloudFleijaEntity;
import com.hbm.ntm.entity.effect.CloudFleijaRainbowEntity;
import com.hbm.ntm.entity.effect.CloudSoliniumEntity;
import com.hbm.ntm.entity.effect.CloudTomEntity;
import com.hbm.ntm.entity.effect.DigammaSpearEntity;
import com.hbm.ntm.entity.effect.EmpBlastEntity;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.entity.effect.FireLingeringEntity;
import com.hbm.ntm.entity.effect.BlackHoleEntity;
import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.entity.particle.EntityChlorineFX;
import com.hbm.entity.particle.EntityCloudFX;
import com.hbm.entity.particle.EntityPinkCloudFX;
import com.hbm.entity.particle.EntityOrangeFX;
import com.hbm.entity.particle.EntityFogFX;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.entity.effect.QuasarEntity;
import com.hbm.ntm.entity.effect.RagingVortexEntity;
import com.hbm.ntm.entity.effect.VortexEntity;
import com.hbm.entity.logic.EntityWaypoint;
import com.hbm.ntm.entity.item.LegacyFallingBlockEntity;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.entity.item.DeliveryDroneEntity;
import com.hbm.ntm.entity.item.RequestDroneEntity;
import com.hbm.ntm.entity.item.ParachuteCrateEntity;
import com.hbm.ntm.entity.item.FireworksEntity;
import com.hbm.ntm.entity.cart.NtmCrateMinecartEntity;
import com.hbm.ntm.entity.cart.NtmDestroyerMinecartEntity;
import com.hbm.ntm.entity.cart.NtmEmptyMinecartEntity;
import com.hbm.ntm.entity.cart.NtmPowderMinecartEntity;
import com.hbm.ntm.entity.cart.NtmSemtexMinecartEntity;
import com.hbm.ntm.entity.train.RailCarBoundingDummyEntity;
import com.hbm.ntm.entity.train.RailCarSeatDummyEntity;
import com.hbm.ntm.entity.train.CargoTramEntity;
import com.hbm.ntm.entity.train.CargoTramTrailerEntity;
import com.hbm.ntm.entity.logic.BalefireExplosionEntity;
import com.hbm.ntm.entity.logic.AirstrikeBomberEntity;
import com.hbm.ntm.entity.logic.C130Entity;
import com.hbm.ntm.entity.projectile.BoxcarEntity;
import com.hbm.ntm.entity.logic.DeathBlastEntity;
import com.hbm.ntm.entity.logic.OrbitalLaserEntity;
import com.hbm.ntm.entity.logic.EmpLogicEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.entity.logic.TomBlastEntity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.hbm.ntm.entity.item.RubberBoatEntity;
import com.hbm.ntm.entity.item.BuoyantItemEntity;
import com.hbm.ntm.entity.mob.EntityCyberCrab;
import com.hbm.ntm.entity.mob.EntityCreeperNuclear;
import com.hbm.ntm.entity.mob.EntityCreeperTainted;
import com.hbm.ntm.entity.mob.EntityCreeperPhosgene;
import com.hbm.ntm.entity.mob.EntityCreeperVolatile;
import com.hbm.ntm.entity.mob.EntityCreeperGold;
import com.hbm.ntm.entity.mob.EntityGhost;
import com.hbm.ntm.entity.mob.EntityPlasticBag;
import com.hbm.ntm.entity.mob.EntityPigeon;
import com.hbm.ntm.entity.mob.EntityBlockSpider;
import com.hbm.ntm.entity.mob.EntityDuck;
import com.hbm.ntm.entity.mob.EntityFBI;
import com.hbm.ntm.entity.mob.EntityFBIDrone;
import com.hbm.ntm.entity.mob.EntityUndeadSoldier;
import com.hbm.ntm.entity.mob.EntityRADBeast;
import com.hbm.ntm.entity.mob.EntityParasiteMaggot;
import com.hbm.ntm.entity.mob.EntityTaintCrab;
import com.hbm.ntm.entity.mob.EntityTeslaCrab;
import com.hbm.ntm.entity.missile.AntiBallisticMissileEntity;
import com.hbm.ntm.entity.missile.BobmazonDeliveryEntity;
import com.hbm.ntm.entity.missile.CustomMissileEntity;
import com.hbm.ntm.entity.missile.MinerRocketEntity;
import com.hbm.ntm.entity.missile.MissileEntity;
import com.hbm.ntm.entity.missile.SoyuzCapsuleEntity;
import com.hbm.ntm.entity.missile.SoyuzEntity;
import com.hbm.ntm.entity.projectile.ArtilleryRocketEntity;
import com.hbm.ntm.entity.projectile.ArtilleryShellEntity;
import com.hbm.ntm.entity.projectile.EntityAcidBomb;
import com.hbm.ntm.entity.projectile.AirstrikeBombletEntity;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.entity.projectile.BurningFoeqEntity;
import com.hbm.ntm.entity.projectile.ChemicalProjectileEntity;
import com.hbm.ntm.entity.projectile.CoinEntity;
import com.hbm.ntm.entity.projectile.CogEntity;
import com.hbm.ntm.entity.projectile.DynamiteStickEntity;
import com.hbm.ntm.entity.projectile.DisperserCanisterEntity;
import com.hbm.ntm.entity.projectile.FallingNukeEntity;
import com.hbm.ntm.entity.projectile.RBMKDebrisEntity;
import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.hbm.ntm.entity.projectile.SawbladeEntity;
import com.hbm.ntm.entity.projectile.ShrapnelEntity;
import com.hbm.ntm.entity.projectile.TomProjectileEntity;
import com.hbm.ntm.entity.projectile.WastePearlEntity;
import com.hbm.ntm.entity.projectile.ZirnoxDebrisEntity;
import com.hbm.entity.mob.glyphid.EntityGlyphidDigger;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.entity.mob.glyphid.EntityGlyphidBrawler;
import com.hbm.entity.mob.glyphid.EntityGlyphidBombardier;
import com.hbm.entity.mob.glyphid.EntityGlyphidBlaster;
import com.hbm.entity.mob.glyphid.EntityGlyphidBehemoth;
import com.hbm.entity.mob.glyphid.EntityGlyphidBrenda;
import com.hbm.entity.mob.glyphid.EntityGlyphidNuclear;
import com.hbm.entity.mob.glyphid.EntityGlyphidScout;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {
    /**
     * 1.7.10 launch pads replaced the entity-mapping default with this value
     * after each successful missile spawn. Forge 1.20.1 stores this
     * EntityType value in chunks, so 500 legacy blocks become 32 chunks.
     */
    private static final int LAUNCH_PAD_MISSILE_TRACKING_RANGE = legacyTrackingRange(500);

    /** Converts the 1.7.10 block-based tracker distance to 1.20.1 chunks. */
    private static int legacyTrackingRange(int blocks) {
        return (blocks + 15) / 16;
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HbmNtm.MOD_ID);

    public static final RegistryObject<EntityType<MovingItemEntity>> MOVING_ITEM =
            ENTITY_TYPES.register("entity_c_item", () -> EntityType.Builder
                    .<MovingItemEntity>of(MovingItemEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_c_item"));

    public static final RegistryObject<EntityType<MovingPackageEntity>> MOVING_PACKAGE =
            ENTITY_TYPES.register("entity_c_package", () -> EntityType.Builder
                    .<MovingPackageEntity>of(MovingPackageEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_c_package"));

    public static final RegistryObject<EntityType<DeliveryDroneEntity>> DELIVERY_DRONE =
            ENTITY_TYPES.register("entity_delivery_drone", () -> EntityType.Builder
                    .<DeliveryDroneEntity>of(DeliveryDroneEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_delivery_drone"));
    public static final RegistryObject<EntityType<RequestDroneEntity>> REQUEST_DRONE =
            ENTITY_TYPES.register("entity_request_drone", () -> EntityType.Builder
                    .<RequestDroneEntity>of(RequestDroneEntity::new, MobCategory.MISC)
                    .sized(0.75F, 0.75F).clientTrackingRange(legacyTrackingRange(250)).updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false).build("entity_request_drone"));

    public static final RegistryObject<EntityType<RubberBoatEntity>> RUBBER_BOAT =
            ENTITY_TYPES.register("entity_rubber_boat", () -> EntityType.Builder
                    .<RubberBoatEntity>of(RubberBoatEntity::new, MobCategory.MISC)
                    .sized(1.5F, 0.6F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_rubber_boat"));

    public static final RegistryObject<EntityType<NtmEmptyMinecartEntity>> NTM_CART_ORE =
            ENTITY_TYPES.register("entity_ntm_cart_ore", () -> EntityType.Builder
                    .<NtmEmptyMinecartEntity>of(NtmEmptyMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_cart_ore"));

    public static final RegistryObject<EntityType<NtmCrateMinecartEntity>> NTM_CART_CRATE =
            ENTITY_TYPES.register("entity_ntm_cart_crate", () -> EntityType.Builder
                    .<NtmCrateMinecartEntity>of(NtmCrateMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_cart_crate"));

    public static final RegistryObject<EntityType<NtmPowderMinecartEntity>> NTM_CART_POWDER =
            ENTITY_TYPES.register("entity_ntm_cart_powder", () -> EntityType.Builder
                    .<NtmPowderMinecartEntity>of(NtmPowderMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_cart_powder"));

    public static final RegistryObject<EntityType<NtmSemtexMinecartEntity>> NTM_CART_SEMTEX =
            ENTITY_TYPES.register("entity_ntm_cart_semtex", () -> EntityType.Builder
                    .<NtmSemtexMinecartEntity>of(NtmSemtexMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_cart_semtex"));

    public static final RegistryObject<EntityType<NtmDestroyerMinecartEntity>> NTM_CART_DESTROYER =
            ENTITY_TYPES.register("entity_ntm_cart_destroyer", () -> EntityType.Builder
                    .<NtmDestroyerMinecartEntity>of(NtmDestroyerMinecartEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.7F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_cart_destroyer"));

    /** Invisible dynamic collision member of the non-vanilla HBM rail-car system. */
    public static final RegistryObject<EntityType<RailCarBoundingDummyEntity>> RAIL_CAR_BOUNDING_DUMMY =
            ENTITY_TYPES.register("entity_ntm_bounding_dummy", () -> EntityType.Builder
                    .<RailCarBoundingDummyEntity>of(RailCarBoundingDummyEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_bounding_dummy"));

    /** Dynamic passenger seat for the non-vanilla HBM rail-car system. */
    public static final RegistryObject<EntityType<RailCarSeatDummyEntity>> RAIL_CAR_SEAT_DUMMY =
            ENTITY_TYPES.register("entity_ntm_seat_dummy", () -> EntityType.Builder
                    .<RailCarSeatDummyEntity>of(RailCarSeatDummyEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_seat_dummy"));

    public static final RegistryObject<EntityType<CargoTramEntity>> CARGO_TRAM =
            ENTITY_TYPES.register("entity_ntm_cargo_tram", () -> EntityType.Builder
                    .<CargoTramEntity>of(CargoTramEntity::new, MobCategory.MISC)
                    .sized(5.0F, 2.0F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_cargo_tram"));

    public static final RegistryObject<EntityType<CargoTramTrailerEntity>> CARGO_TRAM_TRAILER =
            ENTITY_TYPES.register("entity_ntm_cargo_tram_trailer", () -> EntityType.Builder
                    .<CargoTramTrailerEntity>of(CargoTramTrailerEntity::new, MobCategory.MISC)
                    .sized(5.0F, 2.0F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("entity_ntm_cargo_tram_trailer"));

    public static final RegistryObject<EntityType<EntityCreeperNuclear>> NUCLEAR_CREEPER =
            ENTITY_TYPES.register("entity_mob_nuclear_creeper", () -> EntityType.Builder
                    .<EntityCreeperNuclear>of(com.hbm.entity.mob.EntityCreeperNuclear::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.7F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_mob_nuclear_creeper"));

    public static final RegistryObject<EntityType<EntityCreeperTainted>> TAINTED_CREEPER =
            ENTITY_TYPES.register("entity_mob_tainted_creeper", () -> EntityType.Builder
                    .<EntityCreeperTainted>of(com.hbm.entity.mob.EntityCreeperTainted::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.7F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_mob_tainted_creeper"));

    public static final RegistryObject<EntityType<EntityCreeperPhosgene>> PHOSGENE_CREEPER =
            ENTITY_TYPES.register("entity_mob_phosgene_creeper", () -> EntityType.Builder
                    .<EntityCreeperPhosgene>of(EntityCreeperPhosgene::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.7F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_mob_phosgene_creeper"));

    public static final RegistryObject<EntityType<EntityCreeperVolatile>> VOLATILE_CREEPER =
            ENTITY_TYPES.register("entity_mob_volatile_creeper", () -> EntityType.Builder
                    .<EntityCreeperVolatile>of(EntityCreeperVolatile::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.7F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_mob_volatile_creeper"));

    public static final RegistryObject<EntityType<EntityCreeperGold>> GOLD_CREEPER =
            ENTITY_TYPES.register("entity_mob_gold_creeper", () -> EntityType.Builder
                    .<EntityCreeperGold>of(EntityCreeperGold::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.7F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_mob_gold_creeper"));

    public static final RegistryObject<EntityType<EntityGhost>> GHOST =
            ENTITY_TYPES.register("entity_ntm_ghost", () -> EntityType.Builder
                    .<EntityGhost>of(EntityGhost::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(3)
                    .build("entity_ntm_ghost"));

    public static final RegistryObject<EntityType<BuoyantItemEntity>> BUOYANT_ITEM =
            ENTITY_TYPES.register("entity_item_buoyant", () -> EntityType.Builder
                    .<BuoyantItemEntity>of(BuoyantItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(100))
                    .updateInterval(20)
                    .build("entity_item_buoyant"));

    public static final RegistryObject<EntityType<EntityPlasticBag>> PLASTIC_BAG =
            ENTITY_TYPES.register("entity_plastic_bag", () -> EntityType.Builder
                    .<EntityPlasticBag>of(EntityPlasticBag::new, MobCategory.WATER_AMBIENT)
                    .sized(0.45F, 0.45F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_plastic_bag"));

    public static final RegistryObject<EntityType<EntityPigeon>> PIGEON =
            ENTITY_TYPES.register("entity_pigeon", () -> EntityType.Builder
                    .<EntityPigeon>of(EntityPigeon::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_pigeon"));

    public static final RegistryObject<EntityType<EntityBlockSpider>> TAINTCRAWLER =
            ENTITY_TYPES.register("entity_taintcrawler", () -> EntityType.Builder
                    .<EntityBlockSpider>of(EntityBlockSpider::new, MobCategory.MONSTER)
                    .sized(0.95F, 1.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(3)
                    .build("entity_taintcrawler"));

    public static final RegistryObject<EntityType<EntityCyberCrab>> CYBER_CRAB =
            ENTITY_TYPES.register("entity_cyber_crab", () -> EntityType.Builder
                    .<EntityCyberCrab>of(EntityCyberCrab::new, MobCategory.MONSTER)
                    .sized(0.75F, 0.35F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_cyber_crab"));

    public static final RegistryObject<EntityType<EntityTeslaCrab>> TESLA_CRAB =
            ENTITY_TYPES.register("entity_tesla_crab", () -> EntityType.Builder
                    .<EntityTeslaCrab>of(com.hbm.entity.mob.EntityTeslaCrab::new, MobCategory.MONSTER)
                    .sized(0.75F, 1.25F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_tesla_crab"));

    public static final RegistryObject<EntityType<EntityTaintCrab>> TAINT_CRAB =
            ENTITY_TYPES.register("entity_taint_crab", () -> EntityType.Builder
                    .<EntityTaintCrab>of(com.hbm.entity.mob.EntityTaintCrab::new, MobCategory.MONSTER)
                    .sized(1.25F, 1.25F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_taint_crab"));

    public static final RegistryObject<EntityType<EntityDuck>> DUCK =
            ENTITY_TYPES.register("entity_fucc_a_ducc", () -> EntityType.Builder
                    .<EntityDuck>of(com.hbm.entity.mob.EntityDuck::new, MobCategory.CREATURE)
                    .sized(0.3F, 0.7F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_fucc_a_ducc"));

    public static final RegistryObject<EntityType<EntityFBIDrone>> FBI_DRONE =
            ENTITY_TYPES.register("entity_ntm_fbi_drone", () -> EntityType.Builder
                    .<EntityFBIDrone>of(EntityFBIDrone::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_ntm_fbi_drone"));

    public static final RegistryObject<EntityType<EntityFBI>> FBI =
            ENTITY_TYPES.register("entity_ntm_fbi", () -> EntityType.Builder
                    .<EntityFBI>of(EntityFBI::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .fireImmune()
                    .build("entity_ntm_fbi"));

    public static final RegistryObject<EntityType<EntityUndeadSoldier>> UNDEAD_SOLDIER =
            ENTITY_TYPES.register("entity_ntm_undead_soldier", () -> EntityType.Builder
                    .<EntityUndeadSoldier>of(EntityUndeadSoldier::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F).clientTrackingRange(legacyTrackingRange(80)).updateInterval(3)
                    .build("entity_ntm_undead_soldier"));

    public static final RegistryObject<EntityType<EntityRADBeast>> RAD_BEAST =
            ENTITY_TYPES.register("entity_ntm_radiation_blaze", () -> EntityType.Builder
                    .<EntityRADBeast>of(com.hbm.entity.mob.EntityRADBeast::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .fireImmune()
                    .build("entity_ntm_radiation_blaze"));

    public static final RegistryObject<EntityType<EntityGlyphid>> GLYPHID =
            ENTITY_TYPES.register("entity_glyphid", () -> EntityType.Builder
                    .<EntityGlyphid>of(EntityGlyphid::new, MobCategory.MONSTER)
                    .sized(1.75F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_glyphid"));

    public static final RegistryObject<EntityType<EntityWaypoint>> GLYPHID_WAYPOINT =
            ENTITY_TYPES.register("entity_waypoint", () -> EntityType.Builder
                    .<EntityWaypoint>of(EntityWaypoint::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(false)
                    .fireImmune()
                    .noSummon()
                    .build("entity_waypoint"));

    public static final RegistryObject<EntityType<EntityGlyphidScout>> GLYPHID_SCOUT =
            ENTITY_TYPES.register("entity_glyphid_scout", () -> EntityType.Builder
                    .<EntityGlyphidScout>of(EntityGlyphidScout::new, MobCategory.MONSTER)
                    .sized(1.25F, 0.75F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_glyphid_scout"));

    public static final RegistryObject<EntityType<EntityGlyphidDigger>> GLYPHID_DIGGER =
            ENTITY_TYPES.register("entity_glyphid_digger", () -> EntityType.Builder
                    .<EntityGlyphidDigger>of(EntityGlyphidDigger::new, MobCategory.MONSTER)
                    .sized(1.75F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_glyphid_digger"));

    public static final RegistryObject<EntityType<EntityGlyphidBrawler>> GLYPHID_BRAWLER =
            ENTITY_TYPES.register("entity_glyphid_brawler", () -> EntityType.Builder
                    .<EntityGlyphidBrawler>of(EntityGlyphidBrawler::new, MobCategory.MONSTER)
                    .sized(2.0F, 1.125F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_glyphid_brawler"));

    public static final RegistryObject<EntityType<EntityGlyphidBombardier>> GLYPHID_BOMBARDIER =
            ENTITY_TYPES.register("entity_glyphid_bombardier", () -> EntityType.Builder
                    .<EntityGlyphidBombardier>of(EntityGlyphidBombardier::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_glyphid_bombardier"));

    public static final RegistryObject<EntityType<EntityGlyphidBlaster>> GLYPHID_BLASTER =
            ENTITY_TYPES.register("entity_glyphid_blaster", () -> EntityType.Builder
                    .<EntityGlyphidBlaster>of(EntityGlyphidBlaster::new, MobCategory.MONSTER)
                    .sized(2.0F, 1.125F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_glyphid_blaster"));

    public static final RegistryObject<EntityType<EntityGlyphidBehemoth>> GLYPHID_BEHEMOTH =
            ENTITY_TYPES.register("entity_glyphid_behemoth", () -> EntityType.Builder
                    .<EntityGlyphidBehemoth>of(EntityGlyphidBehemoth::new, MobCategory.MONSTER)
                    .sized(2.5F, 1.5F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_glyphid_behemoth"));

    public static final RegistryObject<EntityType<EntityGlyphidBrenda>> GLYPHID_BRENDA =
            ENTITY_TYPES.register("entity_glyphid_brenda", () -> EntityType.Builder
                    .<EntityGlyphidBrenda>of(EntityGlyphidBrenda::new, MobCategory.MONSTER)
                    .sized(2.5F, 1.75F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .fireImmune()
                    .build("entity_glyphid_brenda"));

    public static final RegistryObject<EntityType<EntityGlyphidNuclear>> GLYPHID_NUCLEAR =
            ENTITY_TYPES.register("entity_glyphid_nuclear", () -> EntityType.Builder
                    .<EntityGlyphidNuclear>of(EntityGlyphidNuclear::new, MobCategory.MONSTER)
                    .sized(2.5F, 1.75F).clientTrackingRange(legacyTrackingRange(80)).updateInterval(3).fireImmune().noSummon()
                    .build("entity_glyphid_nuclear"));

    public static final RegistryObject<EntityType<EntityAcidBomb>> ACID_BOMB =
            ENTITY_TYPES.register("entity_acid_bomb", () -> EntityType.Builder
                    .<EntityAcidBomb>of(com.hbm.entity.projectile.EntityAcidBomb::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_acid_bomb"));

    public static final RegistryObject<EntityType<EntityParasiteMaggot>> PARASITE_MAGGOT =
            ENTITY_TYPES.register("entity_parasite_maggot", () -> EntityType.Builder
                    .<EntityParasiteMaggot>of(EntityParasiteMaggot::new, MobCategory.MONSTER)
                    .sized(0.3F, 0.7F)
                    .clientTrackingRange(legacyTrackingRange(80))
                    .updateInterval(3)
                    .build("entity_parasite_maggot"));

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
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .noSummon()
                    .build("entity_nuke_mk3"));

    public static final RegistryObject<EntityType<TomBlastEntity>> TOM_BLAST =
            ENTITY_TYPES.register("entity_tom_bust", () -> EntityType.Builder
                    .<TomBlastEntity>of(TomBlastEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .noSummon()
                    .build("entity_tom_bust"));

    public static final RegistryObject<EntityType<TomProjectileEntity>> TOM_PROJECTILE =
            ENTITY_TYPES.register("entity_tom_the_moonstone", () -> EntityType.Builder
                    .<TomProjectileEntity>of(TomProjectileEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_tom_the_moonstone"));

    public static final RegistryObject<EntityType<BurningFoeqEntity>> BURNING_FOEQ =
            ENTITY_TYPES.register("entity_burning_foeq", () -> EntityType.Builder
                    .<BurningFoeqEntity>of(BurningFoeqEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_burning_foeq"));

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
                    .clientTrackingRange(legacyTrackingRange(500))
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

    public static final RegistryObject<EntityType<CloudTomEntity>> CLOUD_TOM =
            ENTITY_TYPES.register("entity_moonstone_blast", () -> EntityType.Builder
                    .<CloudTomEntity>of(CloudTomEntity::new, MobCategory.MISC)
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_moonstone_blast"));

    public static final RegistryObject<EntityType<CloudFleijaRainbowEntity>> CLOUD_FLEIJA_RAINBOW =
            ENTITY_TYPES.register("entity_cloud_rainbow", () -> EntityType.Builder
                    .<CloudFleijaRainbowEntity>of(CloudFleijaRainbowEntity::new, MobCategory.MISC)
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_cloud_rainbow"));

    public static final RegistryObject<EntityType<EmpBlastEntity>> EMP_BLAST =
            ENTITY_TYPES.register("entity_emp_blast", () -> EntityType.Builder
                    .<EmpBlastEntity>of(EmpBlastEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_emp_blast"));

    public static final RegistryObject<EntityType<FireLingeringEntity>> FIRE_LINGERING =
            ENTITY_TYPES.register("entity_fire_lingering", () -> EntityType.Builder
                    .<FireLingeringEntity>of(FireLingeringEntity::new, MobCategory.MISC)
                    .sized(6.0F, 2.0F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .fireImmune()
                    .noSummon()
                    .build("entity_fire_lingering"));

    public static final RegistryObject<EntityType<MistEntity>> MIST =
            ENTITY_TYPES.register("entity_mist", () -> EntityType.Builder
                    .<MistEntity>of(com.hbm.entity.effect.EntityMist::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .noSummon()
                    .build("entity_mist"));

    public static final RegistryObject<EntityType<EntityChlorineFX>> CHLORINE_FX =
            ENTITY_TYPES.register("entity_chlorine_fx", () -> EntityType.Builder
                    .<EntityChlorineFX>of(EntityChlorineFX::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(legacyTrackingRange(1000)).updateInterval(1).noSummon()
                    .build("entity_chlorine_fx"));
    public static final RegistryObject<EntityType<EntityCloudFX>> CLOUD_FX =
            ENTITY_TYPES.register("entity_cloud_fx", () -> EntityType.Builder
                    .<EntityCloudFX>of(EntityCloudFX::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(legacyTrackingRange(1000)).updateInterval(1).noSummon()
                    .build("entity_cloud_fx"));
    public static final RegistryObject<EntityType<EntityPinkCloudFX>> PINK_CLOUD_FX =
            ENTITY_TYPES.register("entity_pink_cloud_fx", () -> EntityType.Builder
                    .<EntityPinkCloudFX>of(EntityPinkCloudFX::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(legacyTrackingRange(1000)).updateInterval(1).noSummon()
                    .build("entity_pink_cloud_fx"));
    public static final RegistryObject<EntityType<EntityOrangeFX>> ORANGE_FX = ENTITY_TYPES.register("entity_agent_orange", () -> EntityType.Builder.<EntityOrangeFX>of(EntityOrangeFX::new, MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(legacyTrackingRange(1000)).updateInterval(1).noSummon().build("entity_agent_orange"));
    public static final RegistryObject<EntityType<EntityFogFX>> NUCLEAR_FOG = ENTITY_TYPES.register("entity_nuclear_fog", () -> EntityType.Builder.<EntityFogFX>of(EntityFogFX::new, MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(legacyTrackingRange(1000)).updateInterval(1).noSummon().build("entity_nuclear_fog"));

    public static final RegistryObject<EntityType<EmpLogicEntity>> EMP_LOGIC =
            ENTITY_TYPES.register("entity_emp_logic", () -> EntityType.Builder
                    .<EmpLogicEntity>of(EmpLogicEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(20)
                    .noSummon()
                    .build("entity_emp_logic"));

    public static final RegistryObject<EntityType<NukeTorexEntity>> NUKE_TOREX =
            ENTITY_TYPES.register("entity_effect_torex", () -> EntityType.Builder
                    .<NukeTorexEntity>of(NukeTorexEntity::new, MobCategory.MISC)
                    .sized(1.0F, 50.0F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .fireImmune()
                    .noSummon()
                    .build("entity_effect_torex"));

    public static final RegistryObject<EntityType<BlackHoleEntity>> BLACK_HOLE =
            ENTITY_TYPES.register("entity_black_hole", () -> EntityType.Builder
                    .<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_black_hole"));

    public static final RegistryObject<EntityType<VortexEntity>> VORTEX =
            ENTITY_TYPES.register("entity_vortex", () -> EntityType.Builder
                    .<VortexEntity>of(VortexEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_vortex"));

    public static final RegistryObject<EntityType<RagingVortexEntity>> RAGING_VORTEX =
            ENTITY_TYPES.register("entity_raging_vortex", () -> EntityType.Builder
                    .<RagingVortexEntity>of(RagingVortexEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_raging_vortex"));

    public static final RegistryObject<EntityType<QuasarEntity>> QUASAR =
            ENTITY_TYPES.register("entity_digamma_quasar", () -> EntityType.Builder
                    .<QuasarEntity>of(QuasarEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(250))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_digamma_quasar"));

    public static final RegistryObject<EntityType<DigammaSpearEntity>> DIGAMMA_SPEAR =
            ENTITY_TYPES.register("entity_spear", () -> EntityType.Builder
                    .<DigammaSpearEntity>of(DigammaSpearEntity::new, MobCategory.MISC)
                    .sized(2.0F, 10.0F)
                    .clientTrackingRange(1000)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_spear"));

    public static final RegistryObject<EntityType<BalefireExplosionEntity>> BALEFIRE_EXPLOSION =
            ENTITY_TYPES.register("entity_balefire", () -> EntityType.Builder
                    .<BalefireExplosionEntity>of(BalefireExplosionEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_balefire"));

    public static final RegistryObject<EntityType<DeathBlastEntity>> DEATH_BLAST =
            ENTITY_TYPES.register("entity_laser_blast", () -> EntityType.Builder
                    .<DeathBlastEntity>of(DeathBlastEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .noSummon()
                    .build("entity_laser_blast"));

    public static final RegistryObject<EntityType<OrbitalLaserEntity>> ORBITAL_LASER =
            ENTITY_TYPES.register("entity_orbital_laser", () -> EntityType.Builder
                    .<OrbitalLaserEntity>of(OrbitalLaserEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .noSummon()
                    .build("entity_orbital_laser"));

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

    public static final RegistryObject<EntityType<LegacyFallingBlockEntity>> LEGACY_FALLING_BLOCK =
            ENTITY_TYPES.register("entity_falling_block_nt", () -> EntityType.Builder
                    .<LegacyFallingBlockEntity>of(LegacyFallingBlockEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_falling_block_nt"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_GENERIC =
            ENTITY_TYPES.register("entity_missile_generic", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.GENERIC),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_generic"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DECOY =
            ENTITY_TYPES.register("entity_missile_decoy", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DECOY),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_decoy"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_INCENDIARY =
            ENTITY_TYPES.register("entity_missile_incendiary", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.INCENDIARY),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_incendiary"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_CLUSTER =
            ENTITY_TYPES.register("entity_missile_cluster", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.CLUSTER),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_cluster"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BUSTER =
            ENTITY_TYPES.register("entity_missile_buster", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BUSTER),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_buster"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_STRONG =
            ENTITY_TYPES.register("entity_missile_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.STRONG),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_INCENDIARY_STRONG =
            ENTITY_TYPES.register("entity_missile_incendiary_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.INCENDIARY_STRONG),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_incendiary_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_CLUSTER_STRONG =
            ENTITY_TYPES.register("entity_missile_cluster_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.CLUSTER_STRONG),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_cluster_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BUSTER_STRONG =
            ENTITY_TYPES.register("entity_missile_buster_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BUSTER_STRONG),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_buster_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_EMP_STRONG =
            ENTITY_TYPES.register("entity_missile_emp_strong", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.EMP_STRONG),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_emp_strong"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BURST =
            ENTITY_TYPES.register("entity_missile_burst", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BURST),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_burst"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_INFERNO =
            ENTITY_TYPES.register("entity_missile_inferno", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.INFERNO),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_inferno"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_RAIN =
            ENTITY_TYPES.register("entity_missile_rain", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.RAIN),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_rain"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DRILL =
            ENTITY_TYPES.register("entity_missile_drill", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DRILL),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_drill"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_STEALTH =
            ENTITY_TYPES.register("entity_missile_stealth", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.STEALTH),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_stealth"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_EMP =
            ENTITY_TYPES.register("entity_missile_emp", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.EMP),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_emp"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_MICRO =
            ENTITY_TYPES.register("entity_missile_micronuclear", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.MICRO),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_micronuclear"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_TEST =
            ENTITY_TYPES.register("entity_missile_test_mk2", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.TEST),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_test_mk2"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_SCHRABIDIUM =
            ENTITY_TYPES.register("entity_missile_schrabidium", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.SCHRABIDIUM),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_schrabidium"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_BHOLE =
            ENTITY_TYPES.register("entity_missile_blackhole", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.BHOLE),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_blackhole"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_TAINT =
            ENTITY_TYPES.register("entity_missile_taint", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.TAINT),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_taint"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_NUCLEAR =
            ENTITY_TYPES.register("entity_missile_nuclear", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.NUCLEAR),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_nuclear"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_NUCLEAR_CLUSTER =
            ENTITY_TYPES.register("entity_missile_mirv", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.MIRV),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_mirv"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_VOLCANO =
            ENTITY_TYPES.register("entity_missile_volcano", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.VOLCANO),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_volcano"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DOOMSDAY =
            ENTITY_TYPES.register("entity_missile_doomsday", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DOOMSDAY),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_doomsday"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_SHUTTLE =
            ENTITY_TYPES.register("entity_missile_shuttle", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.SHUTTLE),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_shuttle"));

    public static final RegistryObject<EntityType<MissileEntity>> MISSILE_DOOMSDAY_RUSTED =
            ENTITY_TYPES.register("entity_missile_doomsday_rusted", () -> EntityType.Builder
                    .<MissileEntity>of((type, level) -> new MissileEntity(type, level, MissileEntity.Variant.DOOMSDAY_RUSTED),
                            MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_doomsday_rusted"));

    public static final RegistryObject<EntityType<AntiBallisticMissileEntity>> MISSILE_ANTI_BALLISTIC =
            ENTITY_TYPES.register("entity_missile_anti", () -> EntityType.Builder
                    .<AntiBallisticMissileEntity>of(AntiBallisticMissileEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(LAUNCH_PAD_MISSILE_TRACKING_RANGE)
                    .updateInterval(1)
                    .build("entity_missile_anti"));

    public static final RegistryObject<EntityType<CustomMissileEntity>> MISSILE_CUSTOM =
            ENTITY_TYPES.register("entity_custom_missile", () -> EntityType.Builder
                    .<CustomMissileEntity>of(CustomMissileEntity::new, MobCategory.MISC)
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_custom_missile"));

    public static final RegistryObject<EntityType<MinerRocketEntity>> MINER_ROCKET =
            ENTITY_TYPES.register("entity_miner_lander", () -> EntityType.Builder
                    .<MinerRocketEntity>of(MinerRocketEntity::new, MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_miner_lander"));

    public static final RegistryObject<EntityType<BobmazonDeliveryEntity>> BOBMAZON_DELIVERY =
            ENTITY_TYPES.register("entity_bobmazon_delivery", () -> EntityType.Builder
                    .<BobmazonDeliveryEntity>of(BobmazonDeliveryEntity::new, MobCategory.MISC)
                    .sized(1.0F, 3.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_bobmazon_delivery"));

    public static final RegistryObject<EntityType<SoyuzEntity>> SOYUZ =
            ENTITY_TYPES.register("entity_soyuz", () -> EntityType.Builder
                    .<SoyuzEntity>of(SoyuzEntity::new, MobCategory.MISC)
                    .sized(5.0F, 50.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_soyuz"));

    public static final RegistryObject<EntityType<SoyuzCapsuleEntity>> SOYUZ_CAPSULE =
            ENTITY_TYPES.register("entity_soyuz_capsule", () -> EntityType.Builder
                    .<SoyuzCapsuleEntity>of(SoyuzCapsuleEntity::new, MobCategory.MISC)
                    // EntitySoyuzCapsule inherits EntityThrowable's 1.7.10 default size;
                    // its visible OBJ does not define a larger collision box.
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_soyuz_capsule"));

    public static final RegistryObject<EntityType<ShrapnelEntity>> SHRAPNEL =
            ENTITY_TYPES.register("entity_shrapnel", () -> EntityType.Builder
                    .<ShrapnelEntity>of(ShrapnelEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_shrapnel"));

    public static final RegistryObject<EntityType<FireworksEntity>> FIREWORKS =
            ENTITY_TYPES.register("entity_firework_ball", () -> EntityType.Builder
                    .<FireworksEntity>of(FireworksEntity::new, MobCategory.MISC)
                    // EntityFireworks directly extends 1.7.10 Entity, retaining its default 0.6 x 1.8 box.
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_firework_ball"));

    public static final RegistryObject<EntityType<RubbleEntity>> RUBBLE =
            ENTITY_TYPES.register("entity_rubble", () -> EntityType.Builder
                    .<RubbleEntity>of(RubbleEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_rubble"));

    public static final RegistryObject<EntityType<RBMKDebrisEntity>> RBMK_DEBRIS =
            ENTITY_TYPES.register("entity_rbmk_debris", () -> EntityType.Builder
                    .<RBMKDebrisEntity>of(RBMKDebrisEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_rbmk_debris"));

    public static final RegistryObject<EntityType<ZirnoxDebrisEntity>> ZIRNOX_DEBRIS =
            ENTITY_TYPES.register("entity_zirnox_debris", () -> EntityType.Builder
                    .<ZirnoxDebrisEntity>of(ZirnoxDebrisEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_zirnox_debris"));

    public static final RegistryObject<EntityType<CogEntity>> COG =
            ENTITY_TYPES.register("entity_stray_cog", () -> EntityType.Builder
                    .<CogEntity>of(CogEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_stray_cog"));

    public static final RegistryObject<EntityType<SawbladeEntity>> SAWBLADE =
            ENTITY_TYPES.register("entity_stray_saw", () -> EntityType.Builder
                    .<SawbladeEntity>of(SawbladeEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_stray_saw"));

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
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_artillery_shell"));

    public static final RegistryObject<EntityType<ArtilleryRocketEntity>> ARTILLERY_ROCKET =
            ENTITY_TYPES.register("entity_himars", () -> EntityType.Builder
                    .<ArtilleryRocketEntity>of(ArtilleryRocketEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_himars"));

    public static final RegistryObject<EntityType<AirstrikeBomberEntity>> AIRSTRIKE_BOMBER =
            ENTITY_TYPES.register("entity_bomber", () -> EntityType.Builder
                    .<AirstrikeBomberEntity>of(AirstrikeBomberEntity::new, MobCategory.MISC)
                    .sized(8.0F, 4.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_bomber"));

    public static final RegistryObject<EntityType<C130Entity>> C130 =
            ENTITY_TYPES.register("entity_c130", () -> EntityType.Builder
                    .<C130Entity>of(C130Entity::new, MobCategory.MISC)
                    .sized(8.0F, 4.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_c130"));

    public static final RegistryObject<EntityType<ParachuteCrateEntity>> PARACHUTE_CRATE =
            ENTITY_TYPES.register("entity_parachute_crate", () -> EntityType.Builder
                    .<ParachuteCrateEntity>of(ParachuteCrateEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_parachute_crate"));

    public static final RegistryObject<EntityType<AirstrikeBombletEntity>> AIRSTRIKE_BOMBLET =
            ENTITY_TYPES.register("entity_bomblet_zeta", () -> EntityType.Builder
                    .<AirstrikeBombletEntity>of(AirstrikeBombletEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .build("entity_bomblet_zeta"));

    public static final RegistryObject<EntityType<BoxcarEntity>> BOXCAR =
            ENTITY_TYPES.register("entity_boxcar", () -> EntityType.Builder
                    .<BoxcarEntity>of(BoxcarEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_boxcar"));

    public static final RegistryObject<EntityType<ChemicalProjectileEntity>> CHEMICAL_PROJECTILE =
            ENTITY_TYPES.register("entity_chemthrower_splash", () -> EntityType.Builder
                    .<ChemicalProjectileEntity>of(com.hbm.entity.projectile.EntityChemical::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .fireImmune()
                    .build("entity_chemthrower_splash"));

    public static final RegistryObject<EntityType<WastePearlEntity>> WASTE_PEARL =
            ENTITY_TYPES.register("entity_waste_pearl", () -> EntityType.Builder
                    .<WastePearlEntity>of(com.hbm.entity.grenade.EntityWastePearl::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_waste_pearl"));

    public static final RegistryObject<EntityType<DynamiteStickEntity>> DYNAMITE_STICK =
            ENTITY_TYPES.register("entity_dynamite_stick", () -> EntityType.Builder
                    .<DynamiteStickEntity>of(DynamiteStickEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("entity_dynamite_stick"));

    public static final RegistryObject<EntityType<DisperserCanisterEntity>> DISPERSER_CANISTER =
            ENTITY_TYPES.register("entity_disperser_canister", () -> EntityType.Builder
                    .<DisperserCanisterEntity>of(DisperserCanisterEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("entity_disperser_canister"));

    public static final RegistryObject<EntityType<CoinEntity>> COIN =
            ENTITY_TYPES.register("entity_coin", () -> EntityType.Builder
                    .<CoinEntity>of(CoinEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(legacyTrackingRange(1000))
                    .updateInterval(1)
                    .build("entity_coin"));

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    private ModEntityTypes() {
    }
}
