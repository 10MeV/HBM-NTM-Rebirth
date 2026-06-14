package com.hbm.util;

import com.hbm.ntm.util.HbmMobEquipmentUtil;
import com.hbm.ntm.util.HbmWeightedRandomUtil;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Legacy-name mob equipment utility facade.
 */
@Deprecated(forRemoval = false)
public final class MobUtil {
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolCommonS = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolRangedS = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolCommon = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolRanged = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolAdv = new HashMap<>();
    public static Map<Integer, List<WeightedRandomObject>> slotPoolAdvRanged = new HashMap<>();
    public static final HashMap<Double, List<WeightedRandomObject>> slotPoolGuns = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolGunsTier1 = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolGunsTier2 = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolGunsTier3 = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolMasks = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolHelms = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolTierArmor = new HashMap<>();
    public static final Map<Integer, List<WeightedRandomObject>> slotPoolMelee = new HashMap<>();

    private MobUtil() {
    }

    public static void intializeMobPools() {
        slotPoolAdvRanged = new HashMap<>(slotPoolAdv);
        slotPoolAdvRanged.remove(0);
    }

    public static List<WeightedRandomObject> createSlotPool(int nullWeight, Object[][] items) {
        List<WeightedRandomObject> pool = new ArrayList<>();
        if (nullWeight > 0) {
            pool.add(new WeightedRandomObject(null, nullWeight));
        }
        pool.addAll(createSlotPool(items));
        return pool;
    }

    public static List<WeightedRandomObject> createSlotPool(Object[][] items) {
        List<WeightedRandomObject> pool = new ArrayList<>();
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
                pool.add(new WeightedRandomObject(stack.copy(), weight));
            } else if (value instanceof Item itemValue) {
                pool.add(new WeightedRandomObject(new ItemStack(itemValue), weight));
            }
        }
        return pool;
    }

    public static void equipFullSet(LivingEntity entity, Item helmet, Item chest, Item legs, Item boots) {
        HbmMobEquipmentUtil.equipFullSet(entity, helmet, chest, legs, boots);
    }

    public static void assignItemsToEntity(LivingEntity entity, Map<Integer, List<WeightedRandomObject>> slotPools,
            Random random) {
        if (entity == null || slotPools == null) {
            return;
        }
        Random roll = random == null ? new Random() : random;
        for (Map.Entry<Integer, List<WeightedRandomObject>> entry : slotPools.entrySet()) {
            EquipmentSlot slot = HbmMobEquipmentUtil.legacySlot(entry.getKey());
            if (slot != null) {
                applyChoice(entity, slot, HbmWeightedRandomUtil.getRandomItem(roll, entry.getValue()));
            }
        }
    }

    public static void assignItemsToEntityModern(LivingEntity entity,
            Map<EquipmentSlot, List<WeightedRandomObject>> slotPools, RandomSource random) {
        if (entity == null || slotPools == null) {
            return;
        }
        RandomSource roll = random == null ? RandomSource.create() : random;
        for (Map.Entry<EquipmentSlot, List<WeightedRandomObject>> entry : slotPools.entrySet()) {
            applyChoice(entity, entry.getKey(), HbmWeightedRandomUtil.getRandomItem(roll, entry.getValue()));
        }
    }

    public static void addFireTask(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }
    }

    private static void applyChoice(LivingEntity entity, EquipmentSlot slot, WeightedRandomObject choice) {
        if (choice == null) {
            return;
        }
        ItemStack stack = choice.asStack();
        if ((stack == null || stack.isEmpty()) && choice.asItem() != null) {
            stack = new ItemStack(choice.asItem());
        }
        if (stack != null && !stack.isEmpty()) {
            entity.setItemSlot(slot, stack);
        }
    }
}
