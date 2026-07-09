package com.hbm.ntm.world;

import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Optional;

public final class LegacyItemStackMigration {
    private static final String KEY_ID = "id";
    private static final String KEY_COUNT = "Count";
    private static final String KEY_DAMAGE = "Damage";

    public static int migrateAll(CompoundTag root) {
        return migrateAll(root, LegacyWorldItemIdMap.empty()).migrated();
    }

    public static Result migrateAll(CompoundTag root, LegacyWorldItemIdMap legacyItemIds) {
        if (root == null) {
            return Result.empty();
        }
        return migrateCompoundRecursively(root, legacyItemIds == null ? LegacyWorldItemIdMap.empty() : legacyItemIds);
    }

    public static boolean migrateItemStackTag(CompoundTag stackTag) {
        return migrateItemStackTag(stackTag, LegacyWorldItemIdMap.empty());
    }

    public static boolean migrateItemStackTag(CompoundTag stackTag, LegacyWorldItemIdMap legacyItemIds) {
        return migrateItemStackTagWithResult(stackTag,
                legacyItemIds == null ? LegacyWorldItemIdMap.empty() : legacyItemIds).migrated() > 0;
    }

    private static Result migrateItemStackTagWithResult(CompoundTag stackTag, LegacyWorldItemIdMap legacyItemIds) {
        if (stackTag == null
                || !stackTag.contains(KEY_COUNT)
                || !stackTag.contains(KEY_DAMAGE)) {
            return Result.empty();
        }

        Optional<RegistryObject<Item>> legacySingle = Optional.empty();
        Optional<ResourceLocation> legacyFamily = Optional.empty();
        if (stackTag.contains(KEY_ID, Tag.TAG_STRING)) {
            String legacyId = stackTag.getString(KEY_ID);
            legacySingle = legacySingleBattery(legacyId);
            legacyFamily = legacySingle.isPresent() ? Optional.empty() : legacyBatteryFamily(legacyId);
        } else if (stackTag.contains(KEY_ID, Tag.TAG_ANY_NUMERIC)) {
            if (legacyItemIds.isEmpty()) {
                return new Result(0, 1, 0, 0);
            }
            Optional<String> legacyId = legacyItemIds.legacyId(stackTag.getInt(KEY_ID));
            if (legacyId.isEmpty()) {
                return new Result(0, 0, 1, 0);
            }
            legacySingle = legacySingleBattery(legacyId.get());
            legacyFamily = legacySingle.isPresent() ? Optional.empty() : legacyBatteryFamily(legacyId.get());
        }
        if (legacySingle.isPresent()) {
            stackTag.putString(KEY_ID, legacySingle.get().getId().toString());
            stackTag.remove(KEY_DAMAGE);
            return new Result(1, 0, 0, 0);
        }
        if (legacyFamily.isEmpty()) {
            return Result.empty();
        }
        Optional<RegistryObject<net.minecraft.world.item.Item>> mapped =
                LegacyMetaItemMappings.item(legacyFamily.get(), stackTag.getInt(KEY_DAMAGE));
        if (mapped.isEmpty()) {
            return new Result(0, 0, 0, 1);
        }
        stackTag.putString(KEY_ID, mapped.get().getId().toString());
        stackTag.remove(KEY_DAMAGE);
        return new Result(1, 0, 0, 0);
    }

    private static Result migrateCompoundRecursively(CompoundTag compound, LegacyWorldItemIdMap legacyItemIds) {
        Result result = migrateItemStackTagWithResult(compound, legacyItemIds);
        for (String key : new ArrayList<>(compound.getAllKeys())) {
            Tag child = compound.get(key);
            if (child instanceof CompoundTag childCompound) {
                result = result.plus(migrateCompoundRecursively(childCompound, legacyItemIds));
            } else if (child instanceof ListTag list) {
                result = result.plus(migrateListRecursively(list, legacyItemIds));
            }
        }
        return result;
    }

    private static Result migrateListRecursively(ListTag list, LegacyWorldItemIdMap legacyItemIds) {
        Result result = Result.empty();
        for (Tag child : list) {
            if (child instanceof CompoundTag childCompound) {
                result = result.plus(migrateCompoundRecursively(childCompound, legacyItemIds));
            } else if (child instanceof ListTag childList) {
                result = result.plus(migrateListRecursively(childList, legacyItemIds));
            }
        }
        return result;
    }

    private static Optional<ResourceLocation> legacyBatteryFamily(String legacyId) {
        String id = LegacyWorldItemIdMap.normalizeLegacyKey(legacyId);
        return switch (id) {
            case "battery_pack", "item.battery_pack", "hbm:battery_pack", "hbm:item.battery_pack" ->
                    Optional.of(LegacyMetaItemMappings.BATTERY_PACK);
            case "battery_sc", "item.battery_sc", "hbm:battery_sc", "hbm:item.battery_sc" ->
                    Optional.of(LegacyMetaItemMappings.BATTERY_SC);
            default -> Optional.empty();
        };
    }

    private static Optional<RegistryObject<Item>> legacySingleBattery(String legacyId) {
        String id = LegacyWorldItemIdMap.normalizeLegacyKey(legacyId);
        return switch (id) {
            case "cube_power", "item.cube_power", "hbm:cube_power", "hbm:item.cube_power" ->
                    Optional.of(ModItems.CUBE_POWER);
            case "battery_potato", "item.battery_potato", "hbm:battery_potato", "hbm:item.battery_potato" ->
                    Optional.of(ModItems.BATTERY_POTATO);
            case "battery_potatos", "item.battery_potatos", "hbm:battery_potatos", "hbm:item.battery_potatos" ->
                    Optional.of(ModItems.BATTERY_POTATOS);
            case "hev_battery", "item.hev_battery", "hbm:hev_battery", "hbm:item.hev_battery" ->
                    Optional.of(ModItems.HEV_BATTERY);
            case "fusion_core", "item.fusion_core", "hbm:fusion_core", "hbm:item.fusion_core" ->
                    Optional.of(ModItems.FUSION_CORE);
            case "energy_core", "item.energy_core", "hbm:energy_core", "hbm:item.energy_core" ->
                    Optional.of(ModItems.ENERGY_CORE);
            case "battery_creative", "item.battery_creative", "hbm:battery_creative", "hbm:item.battery_creative" ->
                    Optional.of(ModItems.BATTERY_CREATIVE);
            default -> Optional.empty();
        };
    }

    public record Result(int migrated, int numericItemStacksWithoutMap, int unknownNumericItemStacks,
                         int unknownLegacyBatteryMetas) {
        static Result empty() {
            return new Result(0, 0, 0, 0);
        }

        Result plus(Result other) {
            if (other == null) {
                return this;
            }
            return new Result(
                    migrated + other.migrated,
                    numericItemStacksWithoutMap + other.numericItemStacksWithoutMap,
                    unknownNumericItemStacks + other.unknownNumericItemStacks,
                    unknownLegacyBatteryMetas + other.unknownLegacyBatteryMetas);
        }
    }

    private LegacyItemStackMigration() {
    }
}
