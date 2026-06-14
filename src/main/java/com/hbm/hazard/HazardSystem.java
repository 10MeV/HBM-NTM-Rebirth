package com.hbm.hazard;

import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.hazard.transformer.HazardTransformerBase;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.List;

/**
 * Legacy package facade for the 1.7.10 item hazard system.
 */
@Deprecated(forRemoval = false)
public final class HazardSystem {
    public static final List<HazardTransformerBase> trafos = new AbstractList<>() {
        private final List<HazardTransformerBase> backing = new ArrayList<>();

        @Override
        public HazardTransformerBase get(int index) {
            return backing.get(index);
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public void add(int index, HazardTransformerBase element) {
            backing.add(index, element);
            com.hbm.ntm.radiation.HazardRegistry.registerTransformer(element.toModern());
        }

        @Override
        public HazardTransformerBase remove(int index) {
            return backing.remove(index);
        }
    };

    public static void register(Object o, HazardData data) {
        if (o instanceof String oreName) {
            ResourceLocation id = LegacyOreDictionaryMappings.itemTagId(oreName);
            com.hbm.ntm.radiation.HazardRegistry.registerTag(TagKey.create(Registries.ITEM, id), data);
        } else if (o instanceof TagKey<?> tag) {
            registerTag(tag, data);
        } else if (o instanceof Item item) {
            com.hbm.ntm.radiation.HazardRegistry.register(item, data);
        } else if (o instanceof Block block) {
            com.hbm.ntm.radiation.HazardRegistry.register(block.asItem(), data);
        } else if (o instanceof ItemStack stack) {
            com.hbm.ntm.radiation.HazardRegistry.registerStack(stack, data);
        }
    }

    public static void blacklist(Object o) {
        if (o instanceof String oreName) {
            ResourceLocation id = LegacyOreDictionaryMappings.itemTagId(oreName);
            com.hbm.ntm.radiation.HazardRegistry.blacklist(TagKey.create(Registries.ITEM, id));
        } else if (o instanceof TagKey<?> tag) {
            blacklistTag(tag);
        } else if (o instanceof ItemStack stack) {
            com.hbm.ntm.radiation.HazardRegistry.blacklist(stack);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerTag(TagKey<?> tag, HazardData data) {
        com.hbm.ntm.radiation.HazardRegistry.registerTag((TagKey<Item>) tag, data);
    }

    @SuppressWarnings("unchecked")
    private static void blacklistTag(TagKey<?> tag) {
        com.hbm.ntm.radiation.HazardRegistry.blacklist((TagKey<Item>) tag);
    }

    public static boolean isItemBlacklisted(ItemStack stack) {
        return !stack.isEmpty() && com.hbm.ntm.radiation.HazardRegistry.isBlacklisted(stack);
    }

    public static List<HazardEntry> getHazardsFromStack(ItemStack stack) {
        List<HazardEntry> entries = new ArrayList<>();
        for (com.hbm.ntm.radiation.HazardEntry entry : com.hbm.ntm.radiation.HazardRegistry.getHazards(stack)) {
            entries.add(HazardEntry.fromModern(entry));
        }
        return entries;
    }

    public static float getHazardLevelFromStack(ItemStack stack, com.hbm.hazard.type.HazardTypeBase hazard) {
        return com.hbm.ntm.radiation.HazardRegistry.getHazardLevel(stack, hazard.modernType());
    }

    public static void applyHazards(ItemStack stack, LivingEntity entity) {
        com.hbm.ntm.radiation.HazardExposureUtil.applyHazards(entity, stack);
    }

    public static void updatePlayerInventory(Player player) {
        com.hbm.ntm.radiation.HazardExposureUtil.updatePlayerInventory(player);
    }

    public static void updateLivingInventory(LivingEntity entity) {
        com.hbm.ntm.radiation.HazardExposureUtil.updateLivingInventory(entity);
    }

    public static void updateDroppedItem(ItemEntity entity) {
        com.hbm.ntm.radiation.HazardExposureUtil.updateDroppedItem(entity);
    }

    public static void addFullTooltip(ItemStack stack, Player player, List<Component> list) {
        com.hbm.ntm.radiation.HazardTooltipUtil.addHazardInformation(stack, list);
    }

    private HazardSystem() {
    }
}
