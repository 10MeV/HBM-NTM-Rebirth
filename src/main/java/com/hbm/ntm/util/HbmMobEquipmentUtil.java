package com.hbm.ntm.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class HbmMobEquipmentUtil {
    private HbmMobEquipmentUtil() {
    }

    public static List<HbmWeightedRandomObject> createSlotPool(int nullWeight, Object[][] items) {
        List<HbmWeightedRandomObject> pool = new ArrayList<>();
        if (nullWeight > 0) {
            pool.add(new HbmWeightedRandomObject(ItemStack.EMPTY, nullWeight));
        }
        pool.addAll(createSlotPool(items));
        return pool;
    }

    public static List<HbmWeightedRandomObject> createSlotPool(Object[][] items) {
        List<HbmWeightedRandomObject> pool = new ArrayList<>();
        if (items == null) {
            return pool;
        }
        for (Object[] item : items) {
            if (item == null || item.length < 2 || !(item[1] instanceof Number number)) {
                continue;
            }
            Object value = item[0];
            int weight = number.intValue();
            if (value instanceof ItemStack stack) {
                pool.add(new HbmWeightedRandomObject(stack.copy(), weight));
            } else if (value instanceof Item itemValue) {
                pool.add(new HbmWeightedRandomObject(new ItemStack(itemValue), weight));
            }
        }
        return pool;
    }

    public static void equipFullSet(LivingEntity entity, Item helmet, Item chest, Item legs, Item boots) {
        if (entity == null) {
            return;
        }
        entity.setItemSlot(EquipmentSlot.HEAD, stackOf(helmet));
        entity.setItemSlot(EquipmentSlot.CHEST, stackOf(chest));
        entity.setItemSlot(EquipmentSlot.LEGS, stackOf(legs));
        entity.setItemSlot(EquipmentSlot.FEET, stackOf(boots));
    }

    public static void assignItemsToEntity(LivingEntity entity, Map<Integer, List<HbmWeightedRandomObject>> slotPools,
            Random random) {
        if (entity == null || slotPools == null) {
            return;
        }
        for (Map.Entry<Integer, List<HbmWeightedRandomObject>> entry : slotPools.entrySet()) {
            EquipmentSlot slot = legacySlot(entry.getKey());
            if (slot != null) {
                assignItemToSlot(entity, slot, entry.getValue(), random);
            }
        }
    }

    public static void assignItemsToEntity(LivingEntity entity, Map<EquipmentSlot, List<HbmWeightedRandomObject>> slotPools,
            RandomSource random) {
        if (entity == null || slotPools == null) {
            return;
        }
        for (Map.Entry<EquipmentSlot, List<HbmWeightedRandomObject>> entry : slotPools.entrySet()) {
            assignItemToSlot(entity, entry.getKey(), entry.getValue(), random);
        }
    }

    public static boolean assignItemToSlot(LivingEntity entity, EquipmentSlot slot,
            List<HbmWeightedRandomObject> pool, Random random) {
        HbmWeightedRandomObject choice = HbmWeightedRandomUtil.getRandomItem(random == null ? new Random() : random, pool);
        return applyChoice(entity, slot, choice);
    }

    public static boolean assignItemToSlot(LivingEntity entity, EquipmentSlot slot,
            List<HbmWeightedRandomObject> pool, RandomSource random) {
        HbmWeightedRandomObject choice = HbmWeightedRandomUtil.getRandomItem(random == null ? RandomSource.create() : random, pool);
        return applyChoice(entity, slot, choice);
    }

    public static EquipmentSlot legacySlot(int slot) {
        return switch (slot) {
            case 0 -> EquipmentSlot.MAINHAND;
            case 1 -> EquipmentSlot.FEET;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.CHEST;
            case 4 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    private static boolean applyChoice(LivingEntity entity, EquipmentSlot slot, HbmWeightedRandomObject choice) {
        if (entity == null || slot == null || choice == null) {
            return false;
        }
        ItemStack stack = choice.asStack();
        if (stack.isEmpty() && choice.asItem() != null) {
            stack = new ItemStack(choice.asItem());
        }
        if (stack.isEmpty()) {
            return false;
        }
        entity.setItemSlot(slot, stack);
        return true;
    }

    private static ItemStack stackOf(Item item) {
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
}
