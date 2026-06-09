package com.hbm.ntm.bullet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SednaMagazineConfig(
        String legacyKey,
        String legacyOwnerName,
        String sourceClassName,
        Kind kind,
        int index,
        int capacity,
        List<String> acceptedBulletConfigNames,
        List<String> acceptedFluidNames,
        AmountUnit amountUnit,
        HudStyle hudStyle,
        ConsumptionMode consumptionMode,
        String nbtTypeKey,
        String nbtCountKey,
        String nbtBeforeReloadKey,
        String nbtAfterReloadKey,
        boolean acceptsAmmoBag,
        boolean acceptsInfiniteAmmoBag,
        boolean returnsCasingsToBag,
        boolean affectedByTrenchmaster,
        String notes) {

    public static final String KEY_MAG_COUNT = "magcount";
    public static final String KEY_MAG_TYPE = "magtype";
    public static final String KEY_MAG_PREV = "magprev";
    public static final String KEY_MAG_AFTER = "magafter";

    public SednaMagazineConfig {
        legacyKey = clean(legacyKey);
        legacyOwnerName = clean(legacyOwnerName);
        sourceClassName = clean(sourceClassName);
        kind = kind == null ? Kind.FULL_RELOAD : kind;
        capacity = Math.max(0, capacity);
        acceptedBulletConfigNames = immutableCleanList(acceptedBulletConfigNames);
        acceptedFluidNames = immutableCleanList(acceptedFluidNames);
        amountUnit = amountUnit == null ? AmountUnit.ROUNDS : amountUnit;
        hudStyle = hudStyle == null ? HudStyle.ROUNDS_WITH_CAPACITY : hudStyle;
        consumptionMode = consumptionMode == null ? ConsumptionMode.STORED_SINGLE_TYPE : consumptionMode;
        nbtTypeKey = clean(nbtTypeKey);
        nbtCountKey = clean(nbtCountKey);
        nbtBeforeReloadKey = clean(nbtBeforeReloadKey);
        nbtAfterReloadKey = clean(nbtAfterReloadKey);
        notes = clean(notes);
    }

    public int reloadLoadLimit() {
        return switch (kind) {
            case SINGLE_RELOAD -> 1;
            case FULL_RELOAD -> capacity;
            default -> 0;
        };
    }

    public boolean hasPersistentAmount() {
        return !nbtCountKey.isEmpty();
    }

    public boolean hasPersistentType() {
        return !nbtTypeKey.isEmpty();
    }

    public Optional<SednaBulletConfig> firstAcceptedBulletConfig() {
        if (acceptedBulletConfigNames.isEmpty()) {
            return Optional.empty();
        }
        return LegacySednaBulletConfigs.byName(acceptedBulletConfigNames.get(0));
    }

    public List<SednaBulletConfig> acceptedBulletConfigs() {
        List<SednaBulletConfig> configs = new ArrayList<>();
        for (String name : acceptedBulletConfigNames) {
            LegacySednaBulletConfigs.byName(name).ifPresent(configs::add);
        }
        return List.copyOf(configs);
    }

    public List<String> missingBulletConfigNames() {
        List<String> missing = new ArrayList<>();
        for (String name : acceptedBulletConfigNames) {
            if (LegacySednaBulletConfigs.byName(name).isEmpty()) {
                missing.add(name);
            }
        }
        return List.copyOf(missing);
    }

    public static SednaMagazineConfig fullReload(String legacyKey, String owner, String sourceClassName, int index,
            int capacity, List<String> acceptedBulletConfigNames) {
        return singleType(legacyKey, owner, sourceClassName, Kind.FULL_RELOAD, index, capacity,
                acceptedBulletConfigNames, HudStyle.ROUNDS_WITH_CAPACITY, "MagazineFullReload");
    }

    public static SednaMagazineConfig singleReload(String legacyKey, String owner, String sourceClassName, int index,
            int capacity, List<String> acceptedBulletConfigNames) {
        return singleType(legacyKey, owner, sourceClassName, Kind.SINGLE_RELOAD, index, capacity,
                acceptedBulletConfigNames, HudStyle.ROUNDS_WITH_CAPACITY, "MagazineSingleReload");
    }

    public static SednaMagazineConfig belt(String legacyKey, String owner, String sourceClassName,
            List<String> acceptedBulletConfigNames, String notes) {
        return new SednaMagazineConfig(legacyKey, owner, sourceClassName, Kind.BELT, 0, 0,
                acceptedBulletConfigNames, List.of(), AmountUnit.ROUNDS, HudStyle.BELT_COUNT,
                ConsumptionMode.BELT_INVENTORY_SCAN, KEY_MAG_TYPE, "", "", "",
                true, true, true, true, notes);
    }

    public static SednaMagazineConfig infinite(String legacyKey, String owner, String sourceClassName,
            String fixedBulletConfigName) {
        return new SednaMagazineConfig(legacyKey, owner, sourceClassName, Kind.INFINITE, 0, 9999,
                List.of(fixedBulletConfigName), List.of(), AmountUnit.ROUNDS, HudStyle.INFINITE,
                ConsumptionMode.NONE, "", "", "", "", false, false, false, false,
                "Legacy MagazineInfinite returns a fixed BulletConfig and never consumes ammo.");
    }

    public static SednaMagazineConfig fluid(String legacyKey, String owner, String sourceClassName, int index,
            int capacity, String notes) {
        return new SednaMagazineConfig(legacyKey, owner, sourceClassName, Kind.FLUID, index, capacity,
                List.of(), List.of(), AmountUnit.MILLIBUCKETS, HudStyle.FLUID_AMOUNT_ONLY,
                ConsumptionMode.STORED_AMOUNT, indexed(KEY_MAG_TYPE, index), indexed(KEY_MAG_COUNT, index),
                indexed(KEY_MAG_PREV, index), indexed(KEY_MAG_AFTER, index),
                false, false, false, false, notes);
    }

    public static SednaMagazineConfig liquidEngine(String legacyKey, String owner, String sourceClassName, int index,
            int capacity, List<String> acceptedFluidNames, String notes) {
        return new SednaMagazineConfig(legacyKey, owner, sourceClassName, Kind.LIQUID_ENGINE, index, capacity,
                List.of(), acceptedFluidNames, AmountUnit.MILLIBUCKETS, HudStyle.ENGINE_FLUID_WITH_CAPACITY,
                ConsumptionMode.STORED_AMOUNT, "", indexed(KEY_MAG_COUNT, index),
                indexed(KEY_MAG_PREV, index), indexed(KEY_MAG_AFTER, index),
                false, false, false, false, notes);
    }

    public static SednaMagazineConfig electricEngine(String legacyKey, String owner, String sourceClassName, int index,
            int capacity, String notes) {
        return new SednaMagazineConfig(legacyKey, owner, sourceClassName, Kind.ELECTRIC_ENGINE, index, capacity,
                List.of(), List.of(), AmountUnit.HE, HudStyle.ENGINE_ENERGY_WITH_CAPACITY,
                ConsumptionMode.STORED_AMOUNT, "", indexed(KEY_MAG_COUNT, index),
                indexed(KEY_MAG_PREV, index), indexed(KEY_MAG_AFTER, index),
                false, false, false, false, notes);
    }

    private static SednaMagazineConfig singleType(String legacyKey, String owner, String sourceClassName, Kind kind,
            int index, int capacity, List<String> acceptedBulletConfigNames, HudStyle hudStyle, String noteKind) {
        boolean capacityOne = capacity == 1;
        return new SednaMagazineConfig(legacyKey, owner, sourceClassName, kind, index, capacity,
                acceptedBulletConfigNames, List.of(), AmountUnit.ROUNDS, hudStyle,
                ConsumptionMode.STORED_SINGLE_TYPE, indexed(KEY_MAG_TYPE, index), indexed(KEY_MAG_COUNT, index),
                indexed(KEY_MAG_PREV, index), indexed(KEY_MAG_AFTER, index),
                true, true, true, !capacityOne,
                noteKind + " uses standardReload with loadLimit=" + (kind == Kind.SINGLE_RELOAD ? "1" : "capacity") + ".");
    }

    private static String indexed(String key, int index) {
        return key + index;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    private static List<String> immutableCleanList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> cleaned = new ArrayList<>();
        for (String value : values) {
            String clean = clean(value);
            if (!clean.isEmpty()) {
                cleaned.add(clean);
            }
        }
        return List.copyOf(cleaned);
    }

    public enum Kind {
        FULL_RELOAD,
        SINGLE_RELOAD,
        BELT,
        INFINITE,
        FLUID,
        LIQUID_ENGINE,
        ELECTRIC_ENGINE
    }

    public enum AmountUnit {
        ROUNDS,
        MILLIBUCKETS,
        HE
    }

    public enum HudStyle {
        ROUNDS_WITH_CAPACITY,
        BELT_COUNT,
        INFINITE,
        FLUID_AMOUNT_ONLY,
        ENGINE_FLUID_WITH_CAPACITY,
        ENGINE_ENERGY_WITH_CAPACITY
    }

    public enum ConsumptionMode {
        STORED_SINGLE_TYPE,
        BELT_INVENTORY_SCAN,
        STORED_AMOUNT,
        NONE
    }
}
