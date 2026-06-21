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
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Legacy package facade for the 1.7.10 item hazard system.
 */
@Deprecated(forRemoval = false)
public final class HazardSystem {
    public static final Map<String, HazardData> oreMap = new LegacyOreMap();
    public static final Map<Item, HazardData> itemMap = new LegacyItemMap();
    public static final Set<String> dictBlacklist = new LegacyDictBlacklist();

    public static final List<HazardTransformerBase> trafos = new AbstractList<>() {
        private final List<TransformerRegistration> backing = new ArrayList<>();

        @Override
        public HazardTransformerBase get(int index) {
            return backing.get(index).legacy();
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public void add(int index, HazardTransformerBase element) {
            TransformerRegistration registration = TransformerRegistration.of(element);
            if (index < backing.size()) {
                com.hbm.ntm.radiation.HazardRegistry.registerTransformerBefore(registration.modern(), backing.get(index).modern());
            } else {
                com.hbm.ntm.radiation.HazardRegistry.registerTransformer(registration.modern());
            }
            backing.add(index, registration);
        }

        @Override
        public HazardTransformerBase set(int index, HazardTransformerBase element) {
            TransformerRegistration registration = TransformerRegistration.of(element);
            TransformerRegistration previous = backing.set(index, registration);
            com.hbm.ntm.radiation.HazardRegistry.replaceTransformer(previous.modern(), registration.modern());
            return previous.legacy();
        }

        @Override
        public HazardTransformerBase remove(int index) {
            TransformerRegistration removed = backing.remove(index);
            com.hbm.ntm.radiation.HazardRegistry.unregisterTransformer(removed.modern());
            return removed.legacy();
        }
    };

    public static void register(Object o, HazardData data) {
        if (o instanceof String oreName) {
            oreMap.put(oreName, data);
        } else if (o instanceof TagKey<?> tag) {
            registerTag(tag, data);
        } else if (o instanceof Item item) {
            itemMap.put(item, data);
        } else if (o instanceof Block block) {
            itemMap.put(block.asItem(), data);
        } else if (o instanceof ItemStack stack) {
            com.hbm.ntm.radiation.HazardRegistry.registerStack(stack, data);
        }
    }

    public static void blacklist(Object o) {
        if (o instanceof String oreName) {
            dictBlacklist.add(oreName);
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
        com.hbm.ntm.radiation.HazardTooltipUtil.addHazardInformation(stack, list, player);
    }

    private record TransformerRegistration(HazardTransformerBase legacy, com.hbm.ntm.radiation.HazardTransformer modern) {
        private static TransformerRegistration of(HazardTransformerBase legacy) {
            return new TransformerRegistration(legacy, legacy.toModern());
        }
    }

    private static TagKey<Item> legacyTag(String oreName) {
        ResourceLocation id = LegacyOreDictionaryMappings.itemTagId(oreName);
        return TagKey.create(Registries.ITEM, id);
    }

    private static final class LegacyOreMap extends AbstractMap<String, HazardData> {
        private final Map<String, HazardData> backing = new LinkedHashMap<>();

        @Override
        public HazardData put(String key, HazardData value) {
            HazardData previous = backing.put(key, value);
            com.hbm.ntm.radiation.HazardRegistry.registerTag(legacyTag(key), value);
            return previous;
        }

        @Override
        public HazardData get(Object key) {
            return key instanceof String oreName ? backing.get(oreName) : null;
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof String oreName && backing.containsKey(oreName);
        }

        @Override
        public HazardData remove(Object key) {
            if (!(key instanceof String oreName)) {
                return null;
            }
            com.hbm.ntm.radiation.HazardRegistry.removeTag(legacyTag(oreName));
            return backing.remove(oreName);
        }

        @Override
        public void clear() {
            for (String oreName : new ArrayList<>(backing.keySet())) {
                com.hbm.ntm.radiation.HazardRegistry.removeTag(legacyTag(oreName));
            }
            backing.clear();
        }

        @Override
        public Set<Entry<String, HazardData>> entrySet() {
            return Set.copyOf(backing.entrySet());
        }
    }

    private static final class LegacyItemMap extends AbstractMap<Item, HazardData> {
        private final Map<Item, HazardData> backing = new LinkedHashMap<>();

        @Override
        public HazardData put(Item key, HazardData value) {
            HazardData previous = backing.put(key, value);
            com.hbm.ntm.radiation.HazardRegistry.register(key, value);
            return previous;
        }

        @Override
        public HazardData get(Object key) {
            return key instanceof Item item ? backing.get(item) : null;
        }

        @Override
        public boolean containsKey(Object key) {
            return key instanceof Item item && backing.containsKey(item);
        }

        @Override
        public HazardData remove(Object key) {
            if (!(key instanceof Item item)) {
                return null;
            }
            com.hbm.ntm.radiation.HazardRegistry.remove(item);
            return backing.remove(item);
        }

        @Override
        public void clear() {
            for (Item item : new ArrayList<>(backing.keySet())) {
                com.hbm.ntm.radiation.HazardRegistry.remove(item);
            }
            backing.clear();
        }

        @Override
        public Set<Entry<Item, HazardData>> entrySet() {
            return Set.copyOf(backing.entrySet());
        }
    }

    private static final class LegacyDictBlacklist extends AbstractSet<String> {
        private final Set<String> backing = new LinkedHashSet<>();

        @Override
        public boolean add(String oreName) {
            if (oreName == null) {
                return false;
            }
            com.hbm.ntm.radiation.HazardRegistry.blacklist(legacyTag(oreName));
            return backing.add(oreName);
        }

        @Override
        public boolean remove(Object value) {
            if (!(value instanceof String oreName)) {
                return false;
            }
            com.hbm.ntm.radiation.HazardRegistry.unblacklist(legacyTag(oreName));
            return backing.remove(oreName);
        }

        @Override
        public boolean contains(Object value) {
            return value instanceof String oreName && backing.contains(oreName);
        }

        @Override
        public Iterator<String> iterator() {
            Iterator<String> iterator = backing.iterator();
            return new Iterator<>() {
                private String current;

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public String next() {
                    current = iterator.next();
                    return current;
                }

                @Override
                public void remove() {
                    iterator.remove();
                    if (current != null) {
                        com.hbm.ntm.radiation.HazardRegistry.unblacklist(legacyTag(current));
                    }
                }
            };
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public void clear() {
            for (String oreName : new ArrayList<>(backing)) {
                com.hbm.ntm.radiation.HazardRegistry.unblacklist(legacyTag(oreName));
            }
            backing.clear();
        }
    }

    private HazardSystem() {
    }
}
