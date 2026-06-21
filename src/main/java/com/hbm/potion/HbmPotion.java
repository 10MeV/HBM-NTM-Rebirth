package com.hbm.potion;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.Optional;

/**
 * Legacy package facade for the old HBM potion registry.
 *
 * <p>The modern port registers effects through {@link ModEffects}; this class
 * only preserves old field names and metadata for source migrations.</p>
 */
@Deprecated(forRemoval = false)
public final class HbmPotion {
    public static final ResourceLocation POTION_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/potions.png");

    public static final RegistryObject<MobEffect> taint = ModEffects.TAINT;
    public static final RegistryObject<MobEffect> radiation = ModEffects.RADIATION;
    public static final RegistryObject<MobEffect> bang = ModEffects.BANG;
    public static final RegistryObject<MobEffect> mutation = ModEffects.MUTATION;
    public static final RegistryObject<MobEffect> radx = ModEffects.RADX;
    public static final RegistryObject<MobEffect> lead = ModEffects.LEAD;
    public static final RegistryObject<MobEffect> radaway = ModEffects.RADAWAY;
    public static final RegistryObject<MobEffect> phosphorus = ModEffects.PHOSPHORUS;
    public static final RegistryObject<MobEffect> stability = ModEffects.STABILITY;
    public static final RegistryObject<MobEffect> potionsickness = ModEffects.POTION_SICKNESS;
    public static final RegistryObject<MobEffect> death = ModEffects.DEATH;

    private HbmPotion() {
    }

    public static void init() {
        // Modern DeferredRegister ownership lives in ModEffects.
    }

    public static RegistryObject<MobEffect> registerPotion(int id, boolean isBad, int color, String name, int x, int y) {
        return legacySpec(name)
                .filter(spec -> spec.isBad == isBad && spec.color == color && spec.iconX == x && spec.iconY == y)
                .map(LegacyPotionSpec::effect)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Legacy potion '" + name + "' is not a modern registered HBM effect"));
    }

    public static MobEffect get(RegistryObject<MobEffect> effect) {
        return effect.get();
    }

    public static Optional<LegacyPotionSpec> legacySpec(String legacyName) {
        if (legacyName == null) {
            return Optional.empty();
        }
        String key = legacyName.toLowerCase(Locale.ROOT);
        return switch (key) {
            case "potion.hbm_taint", "taint" -> Optional.of(spec(taint, true, 0x800080, "potion.hbm_taint", 0, 0));
            case "potion.hbm_radiation", "radiation" -> Optional.of(spec(radiation, true, 0x84C128, "potion.hbm_radiation", 1, 0));
            case "potion.hbm_bang", "bang" -> Optional.of(spec(bang, true, 0x111111, "potion.hbm_bang", 3, 0));
            case "potion.hbm_mutation", "mutation" -> Optional.of(spec(mutation, false, 0x800080, "potion.hbm_mutation", 2, 0));
            case "potion.hbm_radx", "radx" -> Optional.of(spec(radx, false, 0xBB4B00, "potion.hbm_radx", 5, 0));
            case "potion.hbm_lead", "lead" -> Optional.of(spec(lead, true, 0x767682, "potion.hbm_lead", 6, 0));
            case "potion.hbm_radaway", "radaway" -> Optional.of(spec(radaway, false, 0xBB4B00, "potion.hbm_radaway", 7, 0));
            case "potion.hbm_phosphorus", "phosphorus" -> Optional.of(spec(phosphorus, true, 0xFFFF00, "potion.hbm_phosphorus", 1, 1));
            case "potion.hbm_stability", "stability" -> Optional.of(spec(stability, false, 0xD0D0D0, "potion.hbm_stability", 2, 1));
            case "potion.hbm_potionsickness", "potionsickness", "potion_sickness" -> Optional.of(spec(potionsickness, false, 0xFF8080, "potion.hbm_potionsickness", 3, 1));
            case "potion.hbm_death", "death" -> Optional.of(spec(death, false, 0x111111, "potion.hbm_death", 4, 1));
            default -> Optional.empty();
        };
    }

    public static Optional<LegacyPotionSpec> legacySpec(RegistryObject<MobEffect> effect) {
        if (effect == taint) return legacySpec("taint");
        if (effect == radiation) return legacySpec("radiation");
        if (effect == bang) return legacySpec("bang");
        if (effect == mutation) return legacySpec("mutation");
        if (effect == radx) return legacySpec("radx");
        if (effect == lead) return legacySpec("lead");
        if (effect == radaway) return legacySpec("radaway");
        if (effect == phosphorus) return legacySpec("phosphorus");
        if (effect == stability) return legacySpec("stability");
        if (effect == potionsickness) return legacySpec("potionsickness");
        if (effect == death) return legacySpec("death");
        return Optional.empty();
    }

    public static Optional<LegacyPotionSpec> legacySpec(MobEffect effect) {
        if (same(effect, taint)) return legacySpec("taint");
        if (same(effect, radiation)) return legacySpec("radiation");
        if (same(effect, bang)) return legacySpec("bang");
        if (same(effect, mutation)) return legacySpec("mutation");
        if (same(effect, radx)) return legacySpec("radx");
        if (same(effect, lead)) return legacySpec("lead");
        if (same(effect, radaway)) return legacySpec("radaway");
        if (same(effect, phosphorus)) return legacySpec("phosphorus");
        if (same(effect, stability)) return legacySpec("stability");
        if (same(effect, potionsickness)) return legacySpec("potionsickness");
        if (same(effect, death)) return legacySpec("death");
        return Optional.empty();
    }

    public static boolean isReady(RegistryObject<MobEffect> effect, int durationTicks, int amplifier) {
        if (effect == taint) {
            return durationTicks % 2 == 0;
        }
        if (effect == radiation || effect == radaway || effect == phosphorus) {
            return true;
        }
        if (effect == bang) {
            return durationTicks <= 10;
        }
        if (effect == lead) {
            return durationTicks % 60 == 0;
        }
        return false;
    }

    public static boolean isReady(MobEffect effect, int durationTicks, int amplifier) {
        return legacySpec(effect).map(spec -> isReady(spec.effect, durationTicks, amplifier)).orElse(false);
    }

    public static boolean getIsBadEffect(MobEffect effect) {
        return effect != null && effect.getCategory() == MobEffectCategory.HARMFUL;
    }

    public static boolean getIsBadEffect(RegistryObject<MobEffect> effect) {
        return legacySpec(effect).map(LegacyPotionSpec::isBad).orElse(false);
    }

    private static LegacyPotionSpec spec(RegistryObject<MobEffect> effect, boolean isBad, int color, String legacyName,
            int iconX, int iconY) {
        return new LegacyPotionSpec(effect, isBad, color, legacyName, iconX, iconY);
    }

    private static boolean same(MobEffect effect, RegistryObject<MobEffect> expected) {
        return effect != null && expected.isPresent() && effect == expected.get();
    }

    public record LegacyPotionSpec(RegistryObject<MobEffect> effect, boolean isBad, int color, String legacyName,
            int iconX, int iconY) {
    }
}
