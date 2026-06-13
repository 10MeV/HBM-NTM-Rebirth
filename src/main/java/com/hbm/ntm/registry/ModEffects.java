package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.effect.BangMobEffect;
import com.hbm.ntm.effect.LeadMobEffect;
import com.hbm.ntm.effect.PhosphorusMobEffect;
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
    public static final RegistryObject<MobEffect> BANG =
            EFFECTS.register("bang", BangMobEffect::new);
    public static final RegistryObject<MobEffect> RADAWAY =
            EFFECTS.register("radaway", RadawayMobEffect::new);
    public static final RegistryObject<MobEffect> RADX =
            EFFECTS.register("radx", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0xBB4B00));
    public static final RegistryObject<MobEffect> TAINT =
            EFFECTS.register("taint", TaintMobEffect::new);
    public static final RegistryObject<MobEffect> MUTATION =
            EFFECTS.register("mutation", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0x800080));
    public static final RegistryObject<MobEffect> STABILITY =
            EFFECTS.register("stability", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0xD0D0D0));
    public static final RegistryObject<MobEffect> LEAD =
            EFFECTS.register("lead", LeadMobEffect::new);
    public static final RegistryObject<MobEffect> PHOSPHORUS =
            EFFECTS.register("phosphorus", PhosphorusMobEffect::new);
    public static final RegistryObject<MobEffect> POTION_SICKNESS =
            EFFECTS.register("potionsickness", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0xFF8080));
    public static final RegistryObject<MobEffect> DEATH =
            EFFECTS.register("death", () -> new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 0x111111));

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }

    private ModEffects() {
    }
}
