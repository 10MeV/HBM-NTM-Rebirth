package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.entity.logic.BalefireExplosionEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
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
                    .clientTrackingRange(256)
                    .updateInterval(20)
                    .noSummon()
                    .build("entity_fallout_rain"));

    public static final RegistryObject<EntityType<BalefireExplosionEntity>> BALEFIRE_EXPLOSION =
            ENTITY_TYPES.register("entity_balefire", () -> EntityType.Builder
                    .<BalefireExplosionEntity>of(BalefireExplosionEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .fireImmune()
                    .noSummon()
                    .build("entity_balefire"));

    public static final RegistryObject<EntityType<FallingNukeEntity>> FALLING_NUKE =
            ENTITY_TYPES.register("entity_falling_nuke", () -> EntityType.Builder
                    .<FallingNukeEntity>of(FallingNukeEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("entity_falling_nuke"));

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

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    private ModEntityTypes() {
    }
}
