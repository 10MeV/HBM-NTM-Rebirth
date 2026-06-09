package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.effect.RadawayMobEffect;
import com.hbm.ntm.effect.RadiationMobEffect;
import com.hbm.ntm.effect.SimpleMobEffect;
import com.hbm.ntm.effect.TaintMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
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
    public static final RegistryObject<MobEffect> RADX =
            EFFECTS.register("radx", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0x4FAE4F));
    public static final RegistryObject<MobEffect> TAINT =
            EFFECTS.register("taint", TaintMobEffect::new);
    public static final RegistryObject<MobEffect> MUTATION =
            EFFECTS.register("mutation", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0x63C642));
    public static final RegistryObject<MobEffect> STABILITY =
            EFFECTS.register("stability", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0x4F9CFF));
    public static final RegistryObject<MobEffect> LEAD =
            EFFECTS.register("lead", () -> new SimpleMobEffect(MobEffectCategory.HARMFUL, 0x767682));
    public static final RegistryObject<MobEffect> POTION_SICKNESS =
            EFFECTS.register("potionsickness", () -> new SimpleMobEffect(MobEffectCategory.HARMFUL, 0xFF8080));

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }

    private ModEffects() {
    }
}
