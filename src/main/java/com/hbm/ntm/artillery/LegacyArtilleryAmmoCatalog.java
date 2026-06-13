package com.hbm.ntm.artillery;

import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData.PollutionSample;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;

public final class LegacyArtilleryAmmoCatalog {
    public static final ArtyShell AMMO_ARTY = new ArtyShell("ammo_arty", "ammo_arty",
            "ammo_arty", impact(ImpactKind.STANDARD_EXPLOSION,
                    standardExplosion(10.0F, 3.0F, false, "", 0),
                    explosionCreator(10, 2.0F, 0.5F, 25.0F, 5, 0, 20, 0.75F, 1.0F, -2.0F, 150)),
            "item.hbm_ntm_rebirth.ammo_arty.desc.normal");
    public static final ArtyShell AMMO_ARTY_CLASSIC = new ArtyShell("ammo_arty_classic", "ammo_arty_classic",
            "ammo_arty_classic", impact(ImpactKind.STANDARD_EXPLOSION,
                    standardExplosion(15.0F, 5.0F, false, "", 0),
                    explosionCreator(15, 5.0F, 1.0F, 45.0F, 10, 0, 50, 1.0F, 3.0F, -2.0F, 200)),
            "item.hbm_ntm_rebirth.ammo_arty.desc.classic");
    public static final ArtyShell AMMO_ARTY_HE = new ArtyShell("ammo_arty_he", "ammo_arty_he",
            "ammo_arty_he", impact(ImpactKind.STANDARD_EXPLOSION,
                    standardExplosion(15.0F, 3.0F, true, "block_slag", 1),
                    explosionCreator(15, 5.0F, 1.0F, 45.0F, 10, 16, 50, 1.0F, 3.0F, -2.0F, 200)),
            "item.hbm_ntm_rebirth.ammo_arty.desc.he");
    public static final ArtyShell AMMO_ARTY_PHOSPHORUS = new ArtyShell("ammo_arty_phosphorus",
            "ammo_arty_phosphorus", "ammo_arty_phosphorus", impact(ImpactKind.PHOSPHORUS,
                    sound("hbm:weapon.explosionMedium", 20.0F, 0.9F, 0.2F),
                    standardExplosion(10.0F, 3.0F, false, "", 0),
                    phosphorusArea(15, 12, 15, 5, 30 * 20, 5, 10.0D, 10.0F)),
            "item.hbm_ntm_rebirth.ammo_arty.desc.phosphorus");
    public static final ArtyShell AMMO_ARTY_PHOSPHORUS_MULTI = new ArtyShell("ammo_arty_phosphorus_multi",
            "ammo_arty_phosphorus_multi", "ammo_arty_phosphorus_multi", impact(ImpactKind.CLUSTER,
                    delegatedImpact("ammo_arty_phosphorus"),
                    cluster("ammo_arty_phosphorus", 10, 300.0D, 5.0D)),
            "item.hbm_ntm_rebirth.ammo_arty.desc.phosphorus_multi");
    public static final ArtyShell AMMO_ARTY_MINI_NUKE = new ArtyShell("ammo_arty_mini_nuke",
            "ammo_arty_mini_nuke", "ammo_arty_mini_nuke", impact(ImpactKind.MINI_NUKE,
                    nuke("ExplosionNukeSmall.PARAMS_MEDIUM")),
            "item.hbm_ntm_rebirth.ammo_arty.desc.mini_nuke");
    public static final ArtyShell AMMO_ARTY_MINI_NUKE_MULTI = new ArtyShell("ammo_arty_mini_nuke_multi",
            "ammo_arty_mini_nuke_multi", "ammo_arty_mini_nuke_multi", impact(ImpactKind.CLUSTER,
                    delegatedImpact("ammo_arty_mini_nuke"),
                    cluster("ammo_arty_mini_nuke", 5, 300.0D, 5.0D)),
            "item.hbm_ntm_rebirth.ammo_arty.desc.mini_nuke_multi");
    public static final ArtyShell AMMO_ARTY_NUKE = new ArtyShell("ammo_arty_nuke", "ammo_arty_nuke",
            "ammo_arty_nuke", impact(ImpactKind.NUKE, nuke("BombConfig.missileRadius")),
            "item.hbm_ntm_rebirth.ammo_arty.desc.nuke");
    public static final ArtyShell AMMO_ARTY_CARGO = new ArtyShell("ammo_arty_cargo", "ammo_arty_cargo",
            "ammo_arty_cargo", impact(ImpactKind.CARGO, cargoStick()),
            "item.hbm_ntm_rebirth.ammo_arty.desc.cargo");
    public static final ArtyShell AMMO_ARTY_CHLORINE = new ArtyShell("ammo_arty_chlorine",
            "ammo_arty_chlorine", "ammo_arty_chlorine", impact(ImpactKind.GAS,
                    vanillaExplosion(5.0F, false),
                    gasMist("CHLORINE", 1, 15.0F, 7.5F, -3.0D, 0.0D,
                            Map.of(PollutionType.HEAVYMETAL, 5.0F))),
            "item.hbm_ntm_rebirth.ammo_arty.desc.chlorine");
    public static final ArtyShell AMMO_ARTY_PHOSGENE = new ArtyShell("ammo_arty_phosgene",
            "ammo_arty_phosgene", "ammo_arty_phosgene", impact(ImpactKind.GAS,
                    vanillaExplosion(5.0F, false),
                    gasMist("PHOSGENE", 3, 15.0F, 10.0F, -5.0D, 15.0D,
                            Map.of(PollutionType.HEAVYMETAL, 10.0F, PollutionType.POISON, 15.0F))),
            "item.hbm_ntm_rebirth.ammo_arty.desc.phosgene");
    public static final ArtyShell AMMO_ARTY_MUSTARD_GAS = new ArtyShell("ammo_arty_mustard_gas",
            "ammo_arty_mustard_gas", "ammo_arty_mustard_gas", impact(ImpactKind.GAS,
                    vanillaExplosion(5.0F, false),
                    gasMist("MUSTARDGAS", 5, 20.0F, 10.0F, -5.0D, 25.0D,
                            Map.of(PollutionType.HEAVYMETAL, 15.0F, PollutionType.POISON, 30.0F))),
            "item.hbm_ntm_rebirth.ammo_arty.desc.mustard_gas");

    public static final HimarsRocket AMMO_HIMARS_STANDARD = new HimarsRocket("ammo_himars_standard",
            "ammo_himars", "himars_standard", 6, 0, impact(ImpactKind.STANDARD_EXPLOSION,
                    standardExplosion(20.0F, 3.0F, false, "block_slag", 1),
                    explosionCreator(15, 5.0F, 1.0F, 45.0F, 10, 0, 50, 1.0F, 3.0F, -2.0F, 200)),
            "item.hbm_ntm_rebirth.ammo_himars.desc.standard");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_HE = new HimarsRocket("ammo_himars_standard_he",
            "ammo_himars", "himars_standard_he", 6, 0, impact(ImpactKind.STANDARD_EXPLOSION,
                    standardExplosion(20.0F, 3.0F, true, "block_slag", 1),
                    explosionCreator(15, 5.0F, 1.0F, 45.0F, 10, 16, 50, 1.0F, 3.0F, -2.0F, 200)),
            "item.hbm_ntm_rebirth.ammo_himars.desc.standard_he");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_WP = new HimarsRocket("ammo_himars_standard_wp",
            "ammo_himars", "himars_standard_wp", 6, 0, impact(ImpactKind.PHOSPHORUS,
                    sound("hbm:weapon.explosionMedium", 20.0F, 0.9F, 0.2F),
                    standardExplosion(20.0F, 3.0F, false, "block_slag", 1),
                    phosphorusArea(30, 20, 30, 5, 30 * 20, 10, 15.0D, 15.0F)),
            "item.hbm_ntm_rebirth.ammo_himars.desc.standard_wp");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_TB = new HimarsRocket("ammo_himars_standard_tb",
            "ammo_himars", "himars_standard_tb", 6, 0, impact(ImpactKind.THERMOBARIC,
                    sound("hbm:weapon.explosionMedium", 20.0F, 0.9F, 0.2F),
                    standardExplosion(20.0F, 10.0F, true, "block_slag", 1),
                    shrapnel(30),
                    mushroom(20.0F)),
            "item.hbm_ntm_rebirth.ammo_himars.desc.standard_tb");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_LAVA = new HimarsRocket("ammo_himars_standard_lava",
            "ammo_himars", "himars_standard_lava", 6, 0, impact(ImpactKind.STANDARD_EXPLOSION,
                    standardExplosion(20.0F, 3.0F, true, "volcanic_lava_block", 0)),
            "item.hbm_ntm_rebirth.ammo_himars.desc.standard_lava");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_MINI_NUKE =
            new HimarsRocket("ammo_himars_standard_mini_nuke", "ammo_himars", "himars_standard_mini_nuke",
                    6, 0, impact(ImpactKind.MINI_NUKE, nuke("ExplosionNukeSmall.PARAMS_MEDIUM")),
                    "item.hbm_ntm_rebirth.ammo_himars.desc.standard_mini_nuke");
    public static final HimarsRocket AMMO_HIMARS_SINGLE = new HimarsRocket("ammo_himars_single",
            "ammo_himars", "himars_single", 1, 1, impact(ImpactKind.STANDARD_EXPLOSION,
                    standardExplosion(50.0F, 5.0F, true, "block_slag", 1),
                    explosionCreator(30, 6.5F, 2.0F, 65.0F, 25, 16, 50, 1.25F, 3.0F, -2.0F, 350)),
            "item.hbm_ntm_rebirth.ammo_himars.desc.single");
    public static final HimarsRocket AMMO_HIMARS_SINGLE_TB = new HimarsRocket("ammo_himars_single_tb",
            "ammo_himars", "himars_single_tb", 1, 1, impact(ImpactKind.THERMOBARIC,
                    sound("hbm:weapon.explosionMedium", 20.0F, 0.9F, 0.2F),
                    standardExplosion(50.0F, 12.0F, true, "block_slag", 1),
                    shrapnel(30),
                    mushroom(35.0F)),
            "item.hbm_ntm_rebirth.ammo_himars.desc.single_tb");

    private static final List<ArtyShell> ARTY_SHELLS = List.of(
            AMMO_ARTY,
            AMMO_ARTY_CLASSIC,
            AMMO_ARTY_HE,
            AMMO_ARTY_PHOSPHORUS,
            AMMO_ARTY_PHOSPHORUS_MULTI,
            AMMO_ARTY_MINI_NUKE,
            AMMO_ARTY_MINI_NUKE_MULTI,
            AMMO_ARTY_NUKE,
            AMMO_ARTY_CARGO,
            AMMO_ARTY_CHLORINE,
            AMMO_ARTY_PHOSGENE,
            AMMO_ARTY_MUSTARD_GAS);

    private static final List<HimarsRocket> HIMARS_ROCKETS = List.of(
            AMMO_HIMARS_STANDARD,
            AMMO_HIMARS_STANDARD_HE,
            AMMO_HIMARS_STANDARD_WP,
            AMMO_HIMARS_STANDARD_TB,
            AMMO_HIMARS_STANDARD_LAVA,
            AMMO_HIMARS_STANDARD_MINI_NUKE,
            AMMO_HIMARS_SINGLE,
            AMMO_HIMARS_SINGLE_TB);

    public static List<ArtyShell> artyShells() {
        return ARTY_SHELLS;
    }

    public static List<HimarsRocket> himarsRockets() {
        return HIMARS_ROCKETS;
    }

    public static List<ItemStack> artyDisplayStacks() {
        return displayStacks(ARTY_SHELLS);
    }

    public static List<ItemStack> himarsDisplayStacks() {
        return displayStacks(HIMARS_ROCKETS);
    }

    private static List<ItemStack> displayStacks(List<? extends AmmoType> types) {
        return types.stream()
                .map(AmmoType::item)
                .map(ItemStack::new)
                .toList();
    }

    public static ArtyShell findArtyShell(ItemStack stack) {
        return find(stack, ARTY_SHELLS);
    }

    public static ArtyShell findArtyShell(String legacyName) {
        return find(legacyName, ARTY_SHELLS);
    }

    public static HimarsRocket findHimarsRocket(ItemStack stack) {
        return find(stack, HIMARS_ROCKETS);
    }

    public static HimarsRocket findHimarsRocket(String legacyName) {
        return find(legacyName, HIMARS_ROCKETS);
    }

    public static Map<PollutionType, Float> artyImpactPollution(ArtyShell shell) {
        return shell == null ? Map.of() : impactPollution(shell.impactProfile());
    }

    public static Map<PollutionType, Float> artyImpactPollution(ItemStack stack) {
        return artyImpactPollution(findArtyShell(stack));
    }

    public static PollutionSample artyImpactPollutionSample(ArtyShell shell) {
        PollutionSample sample = new PollutionSample();
        for (Map.Entry<PollutionType, Float> entry : artyImpactPollution(shell).entrySet()) {
            Float amount = entry.getValue();
            if (amount != null && Float.isFinite(amount) && amount > 0.0F) {
                sample.add(entry.getKey(), amount);
            }
        }
        return sample;
    }

    public static PollutionSample artyImpactPollutionSample(ItemStack stack) {
        return artyImpactPollutionSample(findArtyShell(stack));
    }

    public static boolean applyArtyImpactPollution(Level level, BlockPos pos, ArtyShell shell) {
        return PollutionManager.applyPollutionDelta(level, pos, artyImpactPollutionSample(shell));
    }

    public static boolean applyArtyImpactPollution(Level level, BlockPos pos, ItemStack stack) {
        return applyArtyImpactPollution(level, pos, findArtyShell(stack));
    }

    private static <T extends AmmoType> T find(ItemStack stack, List<T> types) {
        if (stack.isEmpty()) {
            return null;
        }
        for (T type : types) {
            if (stack.is(type.item())) {
                return type;
            }
        }
        return null;
    }

    private static <T extends AmmoType> T find(String legacyName, List<T> types) {
        if (legacyName == null || legacyName.isBlank()) {
            return null;
        }
        for (T type : types) {
            if (type.legacyName().equals(legacyName)) {
                return type;
            }
        }
        return null;
    }

    public static RegistryObject<Item> registryObject(String legacyName) {
        return switch (legacyName) {
            case "ammo_arty" -> ModItems.AMMO_ARTY;
            case "ammo_arty_classic" -> ModItems.AMMO_ARTY_CLASSIC;
            case "ammo_arty_he" -> ModItems.AMMO_ARTY_HE;
            case "ammo_arty_phosphorus" -> ModItems.AMMO_ARTY_PHOSPHORUS;
            case "ammo_arty_phosphorus_multi" -> ModItems.AMMO_ARTY_PHOSPHORUS_MULTI;
            case "ammo_arty_mini_nuke" -> ModItems.AMMO_ARTY_MINI_NUKE;
            case "ammo_arty_mini_nuke_multi" -> ModItems.AMMO_ARTY_MINI_NUKE_MULTI;
            case "ammo_arty_nuke" -> ModItems.AMMO_ARTY_NUKE;
            case "ammo_arty_cargo" -> ModItems.AMMO_ARTY_CARGO;
            case "ammo_arty_chlorine" -> ModItems.AMMO_ARTY_CHLORINE;
            case "ammo_arty_phosgene" -> ModItems.AMMO_ARTY_PHOSGENE;
            case "ammo_arty_mustard_gas" -> ModItems.AMMO_ARTY_MUSTARD_GAS;
            case "ammo_himars_standard" -> ModItems.AMMO_HIMARS_STANDARD;
            case "ammo_himars_standard_he" -> ModItems.AMMO_HIMARS_STANDARD_HE;
            case "ammo_himars_standard_wp" -> ModItems.AMMO_HIMARS_STANDARD_WP;
            case "ammo_himars_standard_tb" -> ModItems.AMMO_HIMARS_STANDARD_TB;
            case "ammo_himars_standard_lava" -> ModItems.AMMO_HIMARS_STANDARD_LAVA;
            case "ammo_himars_standard_mini_nuke" -> ModItems.AMMO_HIMARS_STANDARD_MINI_NUKE;
            case "ammo_himars_single" -> ModItems.AMMO_HIMARS_SINGLE;
            case "ammo_himars_single_tb" -> ModItems.AMMO_HIMARS_SINGLE_TB;
            default -> null;
        };
    }

    public interface AmmoType {
        String legacyName();

        String itemTexture();

        String projectileTexture();

        ImpactProfile impactProfile();

        String tooltipKey();

        default List<String> tooltipKeys() {
            return List.of(tooltipKey());
        }

        default Item item() {
            RegistryObject<Item> object = registryObject(legacyName());
            return object.get();
        }

        default Component tooltip() {
            return Component.translatable(tooltipKey());
        }
    }

    public record ArtyShell(String legacyName, String itemTexture, String projectileTexture,
            ImpactProfile impactProfile,
            String tooltipKey) implements AmmoType {
    }

    public record HimarsRocket(String legacyName, String itemTexture, String projectileTexture,
            int amount, int modelType, ImpactProfile impactProfile, String tooltipKey) implements AmmoType {
    }

    public enum ImpactKind {
        STANDARD_EXPLOSION,
        PHOSPHORUS,
        MINI_NUKE,
        NUKE,
        CARGO,
        GAS,
        CLUSTER,
        THERMOBARIC
    }

    public record ImpactProfile(ImpactKind kind, List<ImpactEffect> effects) {
        public <T extends ImpactEffect> List<T> effects(Class<T> type) {
            return effects.stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .toList();
        }
    }

    public sealed interface ImpactEffect permits StandardExplosionEffect, VanillaExplosionEffect,
            ExplosionCreatorEffect, SoundEffect, PhosphorusAreaEffect, GasMistEffect, ClusterEffect,
            DelegatedImpactEffect, NukeEffect, CargoStickEffect, ShrapnelEffect, MushroomEffect {
    }

    public record StandardExplosionEffect(float size, float rangeMod, boolean breaksBlocks,
            String debrisBlock, int debrisMeta) implements ImpactEffect {
    }

    public record VanillaExplosionEffect(float size, boolean breaksBlocks) implements ImpactEffect {
    }

    public record ExplosionCreatorEffect(int size, float cloudScale, float cloudSpeed, float flameScale,
            int smokeCount, int debrisCount, int ashCount, float lift, float smokeScale, float yMotion,
            int lifetime) implements ImpactEffect {
    }

    public record SoundEffect(String legacySound, float volume, float pitchBase, float pitchRandom)
            implements ImpactEffect {
    }

    public record PhosphorusAreaEffect(int shrapnelCount, int igniteRadius, int entityRadius,
            int entityFireSeconds, int phosphorusTicks, int hazeCount, double hazeSpread, float mushroomScale)
            implements ImpactEffect {
    }

    public record GasMistEffect(String fluidLegacyName, int count, float width, float height, double yOffset,
            double scatter, Map<PollutionType, Float> pollution) implements ImpactEffect {
    }

    public record ClusterEffect(String childLegacyName, int amount, double splitHeight, double deviation)
            implements ImpactEffect {
    }

    public record DelegatedImpactEffect(String delegatedLegacyName) implements ImpactEffect {
    }

    public record NukeEffect(String legacyParameter) implements ImpactEffect {
    }

    public record CargoStickEffect() implements ImpactEffect {
    }

    public record ShrapnelEffect(int count) implements ImpactEffect {
    }

    public record MushroomEffect(float scale) implements ImpactEffect {
    }

    private static ImpactProfile impact(ImpactKind kind, ImpactEffect... effects) {
        return new ImpactProfile(kind, List.of(effects));
    }

    private static StandardExplosionEffect standardExplosion(float size, float rangeMod, boolean breaksBlocks,
            String debrisBlock, int debrisMeta) {
        return new StandardExplosionEffect(size, rangeMod, breaksBlocks, debrisBlock, debrisMeta);
    }

    private static VanillaExplosionEffect vanillaExplosion(float size, boolean breaksBlocks) {
        return new VanillaExplosionEffect(size, breaksBlocks);
    }

    private static ExplosionCreatorEffect explosionCreator(int size, float cloudScale, float cloudSpeed,
            float flameScale, int smokeCount, int debrisCount, int ashCount, float lift, float smokeScale,
            float yMotion, int lifetime) {
        return new ExplosionCreatorEffect(size, cloudScale, cloudSpeed, flameScale, smokeCount, debrisCount,
                ashCount, lift, smokeScale, yMotion, lifetime);
    }

    private static SoundEffect sound(String legacySound, float volume, float pitchBase, float pitchRandom) {
        return new SoundEffect(legacySound, volume, pitchBase, pitchRandom);
    }

    private static PhosphorusAreaEffect phosphorusArea(int shrapnelCount, int igniteRadius, int entityRadius,
            int entityFireSeconds, int phosphorusTicks, int hazeCount, double hazeSpread, float mushroomScale) {
        return new PhosphorusAreaEffect(shrapnelCount, igniteRadius, entityRadius, entityFireSeconds,
                phosphorusTicks, hazeCount, hazeSpread, mushroomScale);
    }

    private static GasMistEffect gasMist(String fluidLegacyName, int count, float width, float height,
            double yOffset, double scatter, Map<PollutionType, Float> pollution) {
        return new GasMistEffect(fluidLegacyName, count, width, height, yOffset, scatter, pollution);
    }

    private static ClusterEffect cluster(String childLegacyName, int amount, double splitHeight, double deviation) {
        return new ClusterEffect(childLegacyName, amount, splitHeight, deviation);
    }

    private static DelegatedImpactEffect delegatedImpact(String delegatedLegacyName) {
        return new DelegatedImpactEffect(delegatedLegacyName);
    }

    private static NukeEffect nuke(String legacyParameter) {
        return new NukeEffect(legacyParameter);
    }

    private static CargoStickEffect cargoStick() {
        return new CargoStickEffect();
    }

    private static ShrapnelEffect shrapnel(int count) {
        return new ShrapnelEffect(count);
    }

    private static MushroomEffect mushroom(float scale) {
        return new MushroomEffect(scale);
    }

    private static Map<PollutionType, Float> impactPollution(ImpactProfile profile) {
        if (profile == null) {
            return Map.of();
        }
        for (GasMistEffect gas : profile.effects(GasMistEffect.class)) {
            if (!gas.pollution().isEmpty()) {
                return gas.pollution();
            }
        }
        return Map.of();
    }

    private LegacyArtilleryAmmoCatalog() {
    }
}
