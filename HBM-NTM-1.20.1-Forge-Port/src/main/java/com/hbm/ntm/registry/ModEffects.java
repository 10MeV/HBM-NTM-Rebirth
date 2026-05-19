package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.effect.RadawayMobEffect;
import com.hbm.ntm.effect.RadiationMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HbmNtm.MOD_ID);

    public static final RegistryObject<MobEffect> RADIATION =
            EFFECTS.register("radiation", RadiationMobEffect::new);
    public static final RegistryObject<MobEffect> RADAWAY =
            EFFECTS.register("radaway", RadawayMobEffect::new);

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }

    private ModEffects() {
    }
}
