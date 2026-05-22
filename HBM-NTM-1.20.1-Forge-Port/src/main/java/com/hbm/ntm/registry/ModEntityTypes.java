package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.effect.FalloutRainEntity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
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

    public static final RegistryObject<EntityType<FalloutRainEntity>> FALLOUT_RAIN =
            ENTITY_TYPES.register("entity_fallout_rain", () -> EntityType.Builder
                    .<FalloutRainEntity>of(FalloutRainEntity::new, MobCategory.MISC)
                    .sized(4.0F, 20.0F)
                    .clientTrackingRange(256)
                    .updateInterval(20)
                    .noSummon()
                    .build("entity_fallout_rain"));

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }

    private ModEntityTypes() {
    }
}
