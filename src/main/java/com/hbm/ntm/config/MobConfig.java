package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Source-backed MobConfig subset with active non-raid entity consumers. */
public final class MobConfig {
    private static ForgeConfigSpec.IntValue RAID_ATTACK_DELAY;
    private static ForgeConfigSpec.IntValue RAID_ATTACK_REACH;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("fbi");
        RAID_ATTACK_DELAY = builder.comment("Legacy MobConfig 12.F04_raidAttackDelay: ticks between FBI machine-break attempts.")
                .defineInRange("attackDelay", 40, 1, Integer.MAX_VALUE);
        RAID_ATTACK_REACH = builder.comment("Legacy MobConfig 12.F05_raidAttackReach: FBI random machine-break ray length.")
                .defineInRange("attackReach", 2, 0, Integer.MAX_VALUE);
        builder.pop();
    }

    public static int raidAttackDelay() { return intValue(RAID_ATTACK_DELAY, 40); }
    public static int raidAttackReach() { return intValue(RAID_ATTACK_REACH, 2); }

    private static int intValue(ForgeConfigSpec.IntValue value, int fallback) {
        try { return value == null ? fallback : value.get(); } catch (IllegalStateException ignored) { return fallback; }
    }

    private MobConfig() { }
}
