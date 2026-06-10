package com.hbm.ntm.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class OilDrillConfig {
    public static final long DERRICK_POWER_CAP_DEFAULT = 100_000L;
    public static final int DERRICK_CONSUMPTION_DEFAULT = 100;
    public static final int DERRICK_DELAY_DEFAULT = 50;
    public static final int DERRICK_OIL_PER_DEPOSIT_DEFAULT = 500;
    public static final int DERRICK_GAS_PER_DEPOSIT_MIN_DEFAULT = 100;
    public static final int DERRICK_GAS_PER_DEPOSIT_MAX_DEFAULT = 500;
    public static final double DERRICK_DRAIN_CHANCE_DEFAULT = 0.05D;

    public static final long PUMPJACK_POWER_CAP_DEFAULT = 250_000L;
    public static final int PUMPJACK_CONSUMPTION_DEFAULT = 200;
    public static final int PUMPJACK_DELAY_DEFAULT = 25;
    public static final int PUMPJACK_OIL_PER_DEPOSIT_DEFAULT = 750;
    public static final int PUMPJACK_GAS_PER_DEPOSIT_MIN_DEFAULT = 50;
    public static final int PUMPJACK_GAS_PER_DEPOSIT_MAX_DEFAULT = 250;
    public static final double PUMPJACK_DRAIN_CHANCE_DEFAULT = 0.025D;

    public static final long FRACKING_POWER_CAP_DEFAULT = 5_000_000L;
    public static final int FRACKING_CONSUMPTION_DEFAULT = 5_000;
    public static final int FRACKING_SOLUTION_REQUIRED_DEFAULT = 10;
    public static final int FRACKING_DELAY_DEFAULT = 20;
    public static final int FRACKING_OIL_PER_DEPOSIT_DEFAULT = 1_000;
    public static final int FRACKING_GAS_PER_DEPOSIT_MIN_DEFAULT = 100;
    public static final int FRACKING_GAS_PER_DEPOSIT_MAX_DEFAULT = 500;
    public static final double FRACKING_DRAIN_CHANCE_DEFAULT = 0.02D;
    public static final int FRACKING_BEDROCK_OIL_PER_DEPOSIT_DEFAULT = 100;
    public static final int FRACKING_BEDROCK_GAS_PER_DEPOSIT_MIN_DEFAULT = 10;
    public static final int FRACKING_BEDROCK_GAS_PER_DEPOSIT_MAX_DEFAULT = 50;
    public static final int FRACKING_DESTRUCTION_RANGE_DEFAULT = 75;

    public static ForgeConfigSpec.LongValue DERRICK_POWER_CAP;
    public static ForgeConfigSpec.IntValue DERRICK_CONSUMPTION;
    public static ForgeConfigSpec.IntValue DERRICK_DELAY;
    public static ForgeConfigSpec.IntValue DERRICK_OIL_PER_DEPOSIT;
    public static ForgeConfigSpec.IntValue DERRICK_GAS_PER_DEPOSIT_MIN;
    public static ForgeConfigSpec.IntValue DERRICK_GAS_PER_DEPOSIT_MAX;
    public static ForgeConfigSpec.DoubleValue DERRICK_DRAIN_CHANCE;

    public static ForgeConfigSpec.LongValue PUMPJACK_POWER_CAP;
    public static ForgeConfigSpec.IntValue PUMPJACK_CONSUMPTION;
    public static ForgeConfigSpec.IntValue PUMPJACK_DELAY;
    public static ForgeConfigSpec.IntValue PUMPJACK_OIL_PER_DEPOSIT;
    public static ForgeConfigSpec.IntValue PUMPJACK_GAS_PER_DEPOSIT_MIN;
    public static ForgeConfigSpec.IntValue PUMPJACK_GAS_PER_DEPOSIT_MAX;
    public static ForgeConfigSpec.DoubleValue PUMPJACK_DRAIN_CHANCE;

    public static ForgeConfigSpec.LongValue FRACKING_POWER_CAP;
    public static ForgeConfigSpec.IntValue FRACKING_CONSUMPTION;
    public static ForgeConfigSpec.IntValue FRACKING_SOLUTION_REQUIRED;
    public static ForgeConfigSpec.IntValue FRACKING_DELAY;
    public static ForgeConfigSpec.IntValue FRACKING_OIL_PER_DEPOSIT;
    public static ForgeConfigSpec.IntValue FRACKING_GAS_PER_DEPOSIT_MIN;
    public static ForgeConfigSpec.IntValue FRACKING_GAS_PER_DEPOSIT_MAX;
    public static ForgeConfigSpec.DoubleValue FRACKING_DRAIN_CHANCE;
    public static ForgeConfigSpec.IntValue FRACKING_BEDROCK_OIL_PER_DEPOSIT;
    public static ForgeConfigSpec.IntValue FRACKING_BEDROCK_GAS_PER_DEPOSIT_MIN;
    public static ForgeConfigSpec.IntValue FRACKING_BEDROCK_GAS_PER_DEPOSIT_MAX;
    public static ForgeConfigSpec.IntValue FRACKING_DESTRUCTION_RANGE;

    static void define(ForgeConfigSpec.Builder builder) {
        builder.push("machines");
        defineDerrick(builder);
        definePumpjack(builder);
        defineFrackingTower(builder);
        builder.pop();
    }

    private static void defineDerrick(ForgeConfigSpec.Builder builder) {
        builder.push("derrick");
        DERRICK_POWER_CAP = positiveLong(builder, "powerCap", DERRICK_POWER_CAP_DEFAULT,
                "Legacy derrick I:powerCap: internal HE storage capacity.");
        DERRICK_CONSUMPTION = positiveInt(builder, "consumption", DERRICK_CONSUMPTION_DEFAULT,
                "Legacy derrick I:consumption: base HE consumed per drilling tick.");
        DERRICK_DELAY = positiveInt(builder, "delay", DERRICK_DELAY_DEFAULT,
                "Legacy derrick I:delay: base ticks between drilling steps.");
        DERRICK_OIL_PER_DEPOSIT = nonNegativeInt(builder, "oilPerDeposit", DERRICK_OIL_PER_DEPOSIT_DEFAULT,
                "Legacy derrick I:oilPerDeposit: oil added when sucking one oil deposit block.");
        DERRICK_GAS_PER_DEPOSIT_MIN = nonNegativeInt(builder, "gasPerDepositMin",
                DERRICK_GAS_PER_DEPOSIT_MIN_DEFAULT,
                "Legacy derrick I:gasPerDepositMin: minimum gas byproduct per oil deposit.");
        DERRICK_GAS_PER_DEPOSIT_MAX = nonNegativeInt(builder, "gasPerDepositMax",
                DERRICK_GAS_PER_DEPOSIT_MAX_DEFAULT,
                "Legacy derrick I:gasPerDepositMax: maximum gas byproduct per oil deposit.");
        DERRICK_DRAIN_CHANCE = chance(builder, "drainChance", DERRICK_DRAIN_CHANCE_DEFAULT,
                "Legacy derrick D:drainChance: chance to turn an oil deposit block into empty oil.");
        builder.pop();
    }

    private static void definePumpjack(ForgeConfigSpec.Builder builder) {
        builder.push("pumpjack");
        PUMPJACK_POWER_CAP = positiveLong(builder, "powerCap", PUMPJACK_POWER_CAP_DEFAULT,
                "Legacy pumpjack I:powerCap: internal HE storage capacity.");
        PUMPJACK_CONSUMPTION = positiveInt(builder, "consumption", PUMPJACK_CONSUMPTION_DEFAULT,
                "Legacy pumpjack I:consumption: base HE consumed per drilling tick.");
        PUMPJACK_DELAY = positiveInt(builder, "delay", PUMPJACK_DELAY_DEFAULT,
                "Legacy pumpjack I:delay: base ticks between drilling steps.");
        PUMPJACK_OIL_PER_DEPOSIT = nonNegativeInt(builder, "oilPerDeposit", PUMPJACK_OIL_PER_DEPOSIT_DEFAULT,
                "Legacy pumpjack I:oilPerDeposit: oil added when sucking one oil deposit block.");
        PUMPJACK_GAS_PER_DEPOSIT_MIN = nonNegativeInt(builder, "gasPerDepositMin",
                PUMPJACK_GAS_PER_DEPOSIT_MIN_DEFAULT,
                "Legacy pumpjack I:gasPerDepositMin: minimum gas byproduct per oil deposit.");
        PUMPJACK_GAS_PER_DEPOSIT_MAX = nonNegativeInt(builder, "gasPerDepositMax",
                PUMPJACK_GAS_PER_DEPOSIT_MAX_DEFAULT,
                "Legacy pumpjack I:gasPerDepositMax: maximum gas byproduct per oil deposit.");
        PUMPJACK_DRAIN_CHANCE = chance(builder, "drainChance", PUMPJACK_DRAIN_CHANCE_DEFAULT,
                "Legacy pumpjack D:drainChance: chance to turn an oil deposit block into empty oil.");
        builder.pop();
    }

    private static void defineFrackingTower(ForgeConfigSpec.Builder builder) {
        builder.push("frackingtower");
        FRACKING_POWER_CAP = positiveLong(builder, "powerCap", FRACKING_POWER_CAP_DEFAULT,
                "Legacy frackingtower I:powerCap: internal HE storage capacity.");
        FRACKING_CONSUMPTION = positiveInt(builder, "consumption", FRACKING_CONSUMPTION_DEFAULT,
                "Legacy frackingtower I:consumption: base HE consumed per drilling tick.");
        FRACKING_SOLUTION_REQUIRED = nonNegativeInt(builder, "solutionRequired",
                FRACKING_SOLUTION_REQUIRED_DEFAULT,
                "Legacy frackingtower I:solutionRequired: fracking solution consumed per successful suck.");
        FRACKING_DELAY = positiveInt(builder, "delay", FRACKING_DELAY_DEFAULT,
                "Legacy frackingtower I:delay: base ticks between drilling steps.");
        FRACKING_OIL_PER_DEPOSIT = nonNegativeInt(builder, "oilPerDeposit",
                FRACKING_OIL_PER_DEPOSIT_DEFAULT,
                "Legacy frackingtower I:oilPerDeposit: oil added from normal oil deposits.");
        FRACKING_GAS_PER_DEPOSIT_MIN = nonNegativeInt(builder, "gasPerDepositMin",
                FRACKING_GAS_PER_DEPOSIT_MIN_DEFAULT,
                "Legacy frackingtower I:gasPerDepositMin: minimum gas byproduct from normal oil deposits.");
        FRACKING_GAS_PER_DEPOSIT_MAX = nonNegativeInt(builder, "gasPerDepositMax",
                FRACKING_GAS_PER_DEPOSIT_MAX_DEFAULT,
                "Legacy frackingtower I:gasPerDepositMax: maximum gas byproduct from normal oil deposits.");
        FRACKING_DRAIN_CHANCE = chance(builder, "drainChance", FRACKING_DRAIN_CHANCE_DEFAULT,
                "Legacy frackingtower D:drainChance: chance to turn a normal oil deposit block into empty oil.");
        FRACKING_BEDROCK_OIL_PER_DEPOSIT = nonNegativeInt(builder, "oilPerBedrockDeposit",
                FRACKING_BEDROCK_OIL_PER_DEPOSIT_DEFAULT,
                "Legacy frackingtower I:oilPerBedrockDeposit: oil added from bedrock oil deposits.");
        FRACKING_BEDROCK_GAS_PER_DEPOSIT_MIN = nonNegativeInt(builder, "gasPerBedrockDepositMin",
                FRACKING_BEDROCK_GAS_PER_DEPOSIT_MIN_DEFAULT,
                "Legacy frackingtower I:gasPerBedrockDepositMin: minimum gas byproduct from bedrock oil deposits.");
        FRACKING_BEDROCK_GAS_PER_DEPOSIT_MAX = nonNegativeInt(builder, "gasPerBedrockDepositMax",
                FRACKING_BEDROCK_GAS_PER_DEPOSIT_MAX_DEFAULT,
                "Legacy frackingtower I:gasPerBedrockDepositMax: maximum gas byproduct from bedrock oil deposits.");
        FRACKING_DESTRUCTION_RANGE = nonNegativeInt(builder, "destructionRange",
                FRACKING_DESTRUCTION_RANGE_DEFAULT,
                "Legacy frackingtower I:destructionRange: radius used for oil spot surface destruction.");
        builder.pop();
    }

    public static long derrickPowerCap() {
        return positiveLong(DERRICK_POWER_CAP, DERRICK_POWER_CAP_DEFAULT);
    }

    public static int derrickConsumption() {
        return positiveInt(DERRICK_CONSUMPTION, DERRICK_CONSUMPTION_DEFAULT);
    }

    public static int derrickDelay() {
        return positiveInt(DERRICK_DELAY, DERRICK_DELAY_DEFAULT);
    }

    public static int derrickOilPerDeposit() {
        return nonNegativeInt(DERRICK_OIL_PER_DEPOSIT, DERRICK_OIL_PER_DEPOSIT_DEFAULT);
    }

    public static int derrickGasPerDepositMin() {
        return nonNegativeInt(DERRICK_GAS_PER_DEPOSIT_MIN, DERRICK_GAS_PER_DEPOSIT_MIN_DEFAULT);
    }

    public static int derrickGasPerDepositMax() {
        return Math.max(derrickGasPerDepositMin(),
                nonNegativeInt(DERRICK_GAS_PER_DEPOSIT_MAX, DERRICK_GAS_PER_DEPOSIT_MAX_DEFAULT));
    }

    public static double derrickDrainChance() {
        return chance(DERRICK_DRAIN_CHANCE, DERRICK_DRAIN_CHANCE_DEFAULT);
    }

    public static long pumpjackPowerCap() {
        return positiveLong(PUMPJACK_POWER_CAP, PUMPJACK_POWER_CAP_DEFAULT);
    }

    public static int pumpjackConsumption() {
        return positiveInt(PUMPJACK_CONSUMPTION, PUMPJACK_CONSUMPTION_DEFAULT);
    }

    public static int pumpjackDelay() {
        return positiveInt(PUMPJACK_DELAY, PUMPJACK_DELAY_DEFAULT);
    }

    public static int pumpjackOilPerDeposit() {
        return nonNegativeInt(PUMPJACK_OIL_PER_DEPOSIT, PUMPJACK_OIL_PER_DEPOSIT_DEFAULT);
    }

    public static int pumpjackGasPerDepositMin() {
        return nonNegativeInt(PUMPJACK_GAS_PER_DEPOSIT_MIN, PUMPJACK_GAS_PER_DEPOSIT_MIN_DEFAULT);
    }

    public static int pumpjackGasPerDepositMax() {
        return Math.max(pumpjackGasPerDepositMin(),
                nonNegativeInt(PUMPJACK_GAS_PER_DEPOSIT_MAX, PUMPJACK_GAS_PER_DEPOSIT_MAX_DEFAULT));
    }

    public static double pumpjackDrainChance() {
        return chance(PUMPJACK_DRAIN_CHANCE, PUMPJACK_DRAIN_CHANCE_DEFAULT);
    }

    public static long frackingPowerCap() {
        return positiveLong(FRACKING_POWER_CAP, FRACKING_POWER_CAP_DEFAULT);
    }

    public static int frackingConsumption() {
        return positiveInt(FRACKING_CONSUMPTION, FRACKING_CONSUMPTION_DEFAULT);
    }

    public static int frackingSolutionRequired() {
        return nonNegativeInt(FRACKING_SOLUTION_REQUIRED, FRACKING_SOLUTION_REQUIRED_DEFAULT);
    }

    public static int frackingDelay() {
        return positiveInt(FRACKING_DELAY, FRACKING_DELAY_DEFAULT);
    }

    public static int frackingOilPerDeposit() {
        return nonNegativeInt(FRACKING_OIL_PER_DEPOSIT, FRACKING_OIL_PER_DEPOSIT_DEFAULT);
    }

    public static int frackingGasPerDepositMin() {
        return nonNegativeInt(FRACKING_GAS_PER_DEPOSIT_MIN, FRACKING_GAS_PER_DEPOSIT_MIN_DEFAULT);
    }

    public static int frackingGasPerDepositMax() {
        return Math.max(frackingGasPerDepositMin(),
                nonNegativeInt(FRACKING_GAS_PER_DEPOSIT_MAX, FRACKING_GAS_PER_DEPOSIT_MAX_DEFAULT));
    }

    public static double frackingDrainChance() {
        return chance(FRACKING_DRAIN_CHANCE, FRACKING_DRAIN_CHANCE_DEFAULT);
    }

    public static int frackingBedrockOilPerDeposit() {
        return nonNegativeInt(FRACKING_BEDROCK_OIL_PER_DEPOSIT, FRACKING_BEDROCK_OIL_PER_DEPOSIT_DEFAULT);
    }

    public static int frackingBedrockGasPerDepositMin() {
        return nonNegativeInt(FRACKING_BEDROCK_GAS_PER_DEPOSIT_MIN,
                FRACKING_BEDROCK_GAS_PER_DEPOSIT_MIN_DEFAULT);
    }

    public static int frackingBedrockGasPerDepositMax() {
        return Math.max(frackingBedrockGasPerDepositMin(),
                nonNegativeInt(FRACKING_BEDROCK_GAS_PER_DEPOSIT_MAX,
                        FRACKING_BEDROCK_GAS_PER_DEPOSIT_MAX_DEFAULT));
    }

    public static int frackingDestructionRange() {
        return nonNegativeInt(FRACKING_DESTRUCTION_RANGE, FRACKING_DESTRUCTION_RANGE_DEFAULT);
    }

    private static ForgeConfigSpec.IntValue positiveInt(ForgeConfigSpec.Builder builder, String name, int fallback,
            String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 1, Integer.MAX_VALUE);
    }

    private static ForgeConfigSpec.IntValue nonNegativeInt(ForgeConfigSpec.Builder builder, String name, int fallback,
            String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 0, Integer.MAX_VALUE);
    }

    private static ForgeConfigSpec.LongValue positiveLong(ForgeConfigSpec.Builder builder, String name, long fallback,
            String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 1L, Long.MAX_VALUE);
    }

    private static ForgeConfigSpec.DoubleValue chance(ForgeConfigSpec.Builder builder, String name, double fallback,
            String comment) {
        return builder
                .comment(comment)
                .defineInRange(name, fallback, 0.0D, 1.0D);
    }

    private static int positiveInt(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value == null ? fallback : Math.max(1, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static int nonNegativeInt(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value == null ? fallback : Math.max(0, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static long positiveLong(ForgeConfigSpec.LongValue value, long fallback) {
        try {
            return value == null ? fallback : Math.max(1L, value.get());
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private static double chance(ForgeConfigSpec.DoubleValue value, double fallback) {
        try {
            return value == null ? fallback : Math.max(0.0D, Math.min(1.0D, value.get()));
        } catch (IllegalStateException ignored) {
            return fallback;
        }
    }

    private OilDrillConfig() {
    }
}
