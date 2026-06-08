package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ToolConfig {
    public static ForgeConfigSpec.IntValue RECURSION_DEPTH;
    public static ForgeConfigSpec.BooleanValue RECURSIVE_STONE;
    public static ForgeConfigSpec.BooleanValue RECURSIVE_NETHERRACK;
    public static ForgeConfigSpec.BooleanValue ABILITY_HAMMER;
    public static ForgeConfigSpec.BooleanValue ABILITY_VEIN;
    public static ForgeConfigSpec.BooleanValue ABILITY_LUCK;
    public static ForgeConfigSpec.BooleanValue ABILITY_SILK;
    public static ForgeConfigSpec.BooleanValue ABILITY_FURNACE;
    public static ForgeConfigSpec.BooleanValue ABILITY_SHREDDER;
    public static ForgeConfigSpec.BooleanValue ABILITY_CENTRIFUGE;
    public static ForgeConfigSpec.BooleanValue ABILITY_CRYSTALLIZER;
    public static ForgeConfigSpec.BooleanValue ABILITY_MERCURY;
    public static ForgeConfigSpec.BooleanValue ABILITY_EXPLOSION;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("tools");
        RECURSION_DEPTH = builder
                .comment("Legacy 11.00_recursionDepth: maximum recursive vein-miner search depth.")
                .defineInRange("recursionDepth", 1000, 1, 100000);
        RECURSIVE_STONE = builder
                .comment("Legacy 11.01_recursionStone: whether vein-miner can break stone.")
                .define("recursionStone", false);
        RECURSIVE_NETHERRACK = builder
                .comment("Legacy 11.02_recursionNetherrack: whether vein-miner can break netherrack.")
                .define("recursionNetherrack", false);
        ABILITY_HAMMER = builder
                .comment("Legacy 11.03_hammerAbility: allows AoE hammer abilities.")
                .define("hammerAbility", true);
        ABILITY_VEIN = builder
                .comment("Legacy 11.04_abilityVein: allows vein-miner ability.")
                .define("abilityVein", true);
        ABILITY_LUCK = builder
                .comment("Legacy 11.05_abilityLuck: allows luck/fortune ability.")
                .define("abilityLuck", true);
        ABILITY_SILK = builder
                .comment("Legacy 11.06_abilitySilk: allows silk-touch ability.")
                .define("abilitySilk", true);
        ABILITY_FURNACE = builder
                .comment("Legacy 11.07_abilityFurnace: allows auto-smelter ability.")
                .define("abilityFurnace", true);
        ABILITY_SHREDDER = builder
                .comment("Legacy 11.08_abilityShredder: allows auto-shredder ability.")
                .define("abilityShredder", true);
        ABILITY_CENTRIFUGE = builder
                .comment("Legacy 11.09_abilityCentrifuge: allows auto-centrifuge ability.")
                .define("abilityCentrifuge", true);
        ABILITY_CRYSTALLIZER = builder
                .comment("Legacy 11.10_abilityCrystallizer: allows auto-crystallizer ability.")
                .define("abilityCrystallizer", true);
        ABILITY_MERCURY = builder
                .comment("Legacy 11.11_abilityMercury: allows mercury touch ability.")
                .define("abilityMercury", true);
        ABILITY_EXPLOSION = builder
                .comment("Legacy 11.12_abilityExplosion: allows explosion tool ability.")
                .define("abilityExplosion", true);
        builder.pop();
    }

    public static boolean enabled(ForgeConfigSpec.BooleanValue value) {
        try {
            return value == null || value.get();
        } catch (IllegalStateException ignored) {
            return true;
        }
    }

    public static int intValue(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value == null ? fallback : value.get();
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private ToolConfig() {
    }
}
