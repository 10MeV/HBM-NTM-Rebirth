package com.hbm.ntm.itempool;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HbmItemPoolRegistry {
    public static ResourceLocation lootTableId(String legacyPoolId) {
        return HbmItemPoolIds.tableFor(legacyPoolId);
    }

    public static ItemStack getStack(ServerLevel level, String legacyPoolId, Vec3 origin) {
        List<ItemStack> stacks = rollStacks(level, legacyPoolId, origin).stacks();
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy();
    }

    public static List<ItemStack> getStacks(ServerLevel level, String legacyPoolId, Vec3 origin) {
        return rollStacks(level, legacyPoolId, origin).stacks();
    }

    public static RollResult rollStacks(ServerLevel level, String legacyPoolId, Vec3 origin) {
        ResourceLocation id = lootTableId(legacyPoolId);
        List<ItemStack> stacks = roll(level, id, origin);
        if (!stacks.isEmpty()) {
            return new RollResult(legacyPoolId, id, id, false, stacks);
        }

        if (!id.equals(HbmItemPoolIds.backupTable())) {
            HbmNtm.LOGGER.warn("Item pool '{}' produced no loot from {}, falling back to backup pool.", legacyPoolId, id);
            ResourceLocation backupId = HbmItemPoolIds.backupTable();
            return new RollResult(legacyPoolId, id, backupId, true, roll(level, backupId, origin));
        }
        return new RollResult(legacyPoolId, id, id, false, List.of());
    }

    public static boolean hasTable(ServerLevel level, String legacyPoolId) {
        ResourceLocation id = lootTableId(legacyPoolId);
        return hasTable(level, id);
    }

    public static boolean isKnownPoolId(String legacyPoolId) {
        return HbmItemPoolIds.knownPoolIds().contains(legacyPoolId);
    }

    public static boolean hasTable(ServerLevel level, ResourceLocation id) {
        return level.getServer().getLootData().getElement(new LootDataId<>(LootDataType.TABLE, id)) != null;
    }

    public static Set<String> knownPoolIds() {
        return HbmItemPoolIds.knownPoolIds();
    }

    public static Map<String, ResourceLocation> knownTables() {
        return HbmItemPoolIds.explicitTables();
    }

    public static List<MissingTable> missingKnownTables(ServerLevel level) {
        List<MissingTable> missing = new ArrayList<>();
        HbmItemPoolIds.explicitTables().forEach((legacyPoolId, tableId) -> {
            if (!hasTable(level, tableId)) {
                missing.add(new MissingTable(legacyPoolId, tableId));
            }
        });
        return List.copyOf(missing);
    }

    public static List<TableStatus> knownTableStatuses(ServerLevel level) {
        List<TableStatus> statuses = new ArrayList<>();
        HbmItemPoolIds.explicitTables().forEach((legacyPoolId, tableId) ->
                statuses.add(new TableStatus(legacyPoolId, tableId, hasTable(level, tableId))));
        return statuses.stream()
                .sorted((left, right) -> left.legacyPoolId().compareTo(right.legacyPoolId()))
                .toList();
    }

    public static long availableKnownTableCount(ServerLevel level) {
        return knownTableStatuses(level).stream().filter(TableStatus::available).count();
    }

    private static List<ItemStack> roll(ServerLevel level, ResourceLocation id, Vec3 origin) {
        LootTable table = level.getServer().getLootData().getElement(new LootDataId<>(LootDataType.TABLE, id));
        if (table == null) {
            return List.of();
        }
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin)
                .create(LootContextParamSets.CHEST);
        return table.getRandomItems(params);
    }

    public record MissingTable(String legacyPoolId, ResourceLocation tableId) {
    }

    public record TableStatus(String legacyPoolId, ResourceLocation tableId, boolean available) {
    }

    public record RollResult(String legacyPoolId, ResourceLocation requestedTableId, ResourceLocation rolledTableId,
                             boolean usedBackup, List<ItemStack> stacks) {
    }

    private HbmItemPoolRegistry() {
    }
}
