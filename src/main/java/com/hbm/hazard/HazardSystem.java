package com.hbm.hazard;

import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.hazard.transformer.HazardTransformerBase;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Legacy package facade for the 1.7.10 item hazard system.
 */
@Deprecated(forRemoval = false)
public final class HazardSystem {
    public static final HashMap<String, HazardData> oreMap = new LegacyOreMap();
    public static final HashMap<Item, HazardData> itemMap = new LegacyItemMap();
    public static final HashMap<ComparableStack, HazardData> stackMap = new LegacyStackMap();
    public static final HashSet<ComparableStack> stackBlacklist = new LegacyStackBlacklist();
    public static final HashSet<String> dictBlacklist = new LegacyDictBlacklist();

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
            stackMap.put(new ComparableStack(stack), data);
        } else if (o instanceof ComparableStack stack) {
            stackMap.put(stack, data);
        }
    }

    public static void blacklist(Object o) {
        if (o instanceof String oreName) {
            dictBlacklist.add(oreName);
        } else if (o instanceof TagKey<?> tag) {
            blacklistTag(tag);
        } else if (o instanceof ItemStack stack) {
            stackBlacklist.add(new ComparableStack(stack).makeSingular());
        } else if (o instanceof ComparableStack stack) {
            stackBlacklist.add(stack);
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
        if (stack.isEmpty()) {
            return false;
        }
        ComparableStack comp = new ComparableStack(stack).makeSingular();
        if (stackBlacklist.contains(comp)) {
            return true;
        }
        if (com.hbm.ntm.radiation.HazardRegistry.isBlacklisted(stack)) {
            stackBlacklist.add(comp);
            return true;
        }
        return false;
    }

    public static List<HazardEntry> getHazardsFromStack(ItemStack stack) {
        if (isItemBlacklisted(stack)) {
            return new ArrayList<>();
        }

        List<HazardEntry> entries = new ArrayList<>();
        for (com.hbm.ntm.radiation.HazardEntry entry : com.hbm.ntm.radiation.HazardRegistry.getHazards(stack)) {
            entries.add(HazardEntry.fromModern(entry));
        }
        return entries;
    }

    public static float getHazardLevelFromStack(ItemStack stack, com.hbm.hazard.type.HazardTypeBase hazard) {
        for (HazardEntry entry : getHazardsFromStack(stack)) {
            if (entry.type == hazard) {
                return entry.modifiedLevel(stack, null);
            }
        }
        return 0.0F;
    }

    public static void applyHazards(ItemStack stack, LivingEntity entity) {
        for (HazardEntry hazard : getHazardsFromStack(stack)) {
            hazard.applyHazard(stack, entity);
        }
    }

    public static void updatePlayerInventory(Player player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            applyHazards(stack, player);
            if (stack.isEmpty()) {
                player.getInventory().items.set(i, ItemStack.EMPTY);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            applyHazards(stack, player);
        }
    }

    public static void updateLivingInventory(LivingEntity entity) {
        applyHazards(entity.getItemBySlot(EquipmentSlot.MAINHAND), entity);
        applyHazards(entity.getItemBySlot(EquipmentSlot.FEET), entity);
        applyHazards(entity.getItemBySlot(EquipmentSlot.LEGS), entity);
        applyHazards(entity.getItemBySlot(EquipmentSlot.CHEST), entity);
        applyHazards(entity.getItemBySlot(EquipmentSlot.HEAD), entity);
    }

    public static void updateDroppedItem(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        if (entity.isRemoved() || stack.isEmpty()) {
            return;
        }
        for (HazardEntry entry : getHazardsFromStack(stack)) {
            entry.type.updateEntity(entity, entry.modifiedLevel(stack, null));
        }
    }

    public static void addFullTooltip(ItemStack stack, Player player, List<Component> list) {
        for (HazardEntry hazard : getHazardsFromStack(stack)) {
            hazard.type.addHazardInformation(player, list, hazard.baseLevel, stack, hazard.mods);
        }
    }

    public static void clearLegacyMirrors() {
        ((LegacyOreMap) oreMap).replaceMirror(Map.of());
        ((LegacyItemMap) itemMap).replaceMirror(Map.of());
        ((LegacyStackMap) stackMap).replaceMirror(Map.of());
        ((LegacyStackBlacklist) stackBlacklist).replaceMirror(Set.of());
        ((LegacyDictBlacklist) dictBlacklist).replaceMirror(Set.of());
    }

    public static com.hbm.ntm.radiation.HazardData mirrorTag(TagKey<Item> tag, com.hbm.ntm.radiation.HazardData data) {
        HazardData legacy = toLegacyData(data);
        for (String oreName : LegacyOreDictionaryMappings.legacyNamesForTag(tag.location())) {
            ((LegacyOreMap) oreMap).mirrorPut(oreName, legacy);
        }
        return legacy;
    }

    public static void unmirrorTag(TagKey<Item> tag) {
        for (String oreName : LegacyOreDictionaryMappings.legacyNamesForTag(tag.location())) {
            ((LegacyOreMap) oreMap).mirrorRemove(oreName);
        }
    }

    public static com.hbm.ntm.radiation.HazardData mirrorItem(Item item, com.hbm.ntm.radiation.HazardData data) {
        HazardData legacy = toLegacyData(data);
        ((LegacyItemMap) itemMap).mirrorPut(item, legacy);
        return legacy;
    }

    public static void unmirrorItem(Item item) {
        ((LegacyItemMap) itemMap).mirrorRemove(item);
    }

    public static com.hbm.ntm.radiation.HazardData mirrorStack(ItemStack stack, com.hbm.ntm.radiation.HazardData data) {
        HazardData legacy = toLegacyData(data);
        ((LegacyStackMap) stackMap).mirrorPut(new ComparableStack(stack), legacy);
        return legacy;
    }

    public static void unmirrorStack(ItemStack stack) {
        ((LegacyStackMap) stackMap).mirrorRemove(new ComparableStack(stack));
    }

    public static void mirrorTagBlacklist(TagKey<Item> tag) {
        for (String oreName : LegacyOreDictionaryMappings.legacyNamesForTag(tag.location())) {
            ((LegacyDictBlacklist) dictBlacklist).mirrorAdd(oreName);
        }
    }

    public static void unmirrorTagBlacklist(TagKey<Item> tag) {
        for (String oreName : LegacyOreDictionaryMappings.legacyNamesForTag(tag.location())) {
            ((LegacyDictBlacklist) dictBlacklist).mirrorRemove(oreName);
        }
    }

    public static void mirrorStackBlacklist(ItemStack stack) {
        ((LegacyStackBlacklist) stackBlacklist).mirrorAdd(new ComparableStack(stack).makeSingular());
    }

    public static void unmirrorStackBlacklist(ItemStack stack) {
        ((LegacyStackBlacklist) stackBlacklist).mirrorRemove(new ComparableStack(stack).makeSingular());
    }

    private static HazardData toLegacyData(com.hbm.ntm.radiation.HazardData data) {
        if (data instanceof HazardData legacy) {
            return legacy;
        }
        HazardData legacy = new HazardData()
                .setMutex(data.mutexBits())
                .setOverrides(data.overrides());
        for (com.hbm.ntm.radiation.HazardEntry entry : data.entries()) {
            legacy.addEntry(HazardEntry.fromModern(entry));
        }
        return legacy;
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

    private static ComparableStack singularCopy(ComparableStack stack) {
        return stack == null ? null : stack.copy().makeSingular();
    }

    private static ItemStack runtimeStack(ComparableStack stack) {
        ComparableStack singular = singularCopy(stack);
        return singular == null ? ItemStack.EMPTY : singular.toStack();
    }

    private static boolean isRuntimeStackKey(ComparableStack stack) {
        return stack != null && stack.stacksize == 1 && !runtimeStack(stack).isEmpty();
    }

    private static void registerRuntimeStack(ComparableStack stack, HazardData data) {
        if (isRuntimeStackKey(stack)) {
            com.hbm.ntm.radiation.HazardRegistry.registerStack(runtimeStack(stack), data);
        }
    }

    private static void removeRuntimeStack(ComparableStack stack) {
        if (isRuntimeStackKey(stack)) {
            com.hbm.ntm.radiation.HazardRegistry.removeStack(runtimeStack(stack));
        }
    }

    private static void blacklistRuntimeStack(ComparableStack stack) {
        ComparableStack singular = singularCopy(stack);
        if (singular != null) {
            com.hbm.ntm.radiation.HazardRegistry.blacklist(runtimeStack(singular));
        }
    }

    private static void unblacklistRuntimeStack(ComparableStack stack) {
        ComparableStack singular = singularCopy(stack);
        if (singular != null) {
            com.hbm.ntm.radiation.HazardRegistry.unblacklist(runtimeStack(singular));
        }
    }

    private static <K> Set<Entry<K, HazardData>> liveHazardEntrySet(Set<Entry<K, HazardData>> backingEntries,
                                                                    BiConsumer<K, HazardData> registerRuntime,
                                                                    Consumer<K> removeRuntime) {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<K, HazardData>> iterator() {
                Iterator<Entry<K, HazardData>> iterator = backingEntries.iterator();
                return new Iterator<>() {
                    private Entry<K, HazardData> current;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Entry<K, HazardData> next() {
                        current = iterator.next();
                        return new LiveHazardEntry<>(current, registerRuntime);
                    }

                    @Override
                    public void remove() {
                        K key = current == null ? null : current.getKey();
                        iterator.remove();
                        if (current != null) {
                            removeRuntime.accept(key);
                        }
                    }
                };
            }

            @Override
            public int size() {
                return backingEntries.size();
            }
        };
    }

    private static final class LiveHazardEntry<K> implements Entry<K, HazardData> {
        private final Entry<K, HazardData> delegate;
        private final BiConsumer<K, HazardData> registerRuntime;

        private LiveHazardEntry(Entry<K, HazardData> delegate, BiConsumer<K, HazardData> registerRuntime) {
            this.delegate = delegate;
            this.registerRuntime = registerRuntime;
        }

        @Override
        public K getKey() {
            return delegate.getKey();
        }

        @Override
        public HazardData getValue() {
            return delegate.getValue();
        }

        @Override
        public HazardData setValue(HazardData value) {
            HazardData previous = delegate.setValue(value);
            registerRuntime.accept(delegate.getKey(), value);
            return previous;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Entry<?, ?> other)) {
                return false;
            }
            return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    private abstract static class LegacyHazardMap<K> extends HashMap<K, HazardData> {
        void mirrorPut(K key, HazardData value) {
            super.put(key, value);
        }

        void mirrorRemove(K key) {
            super.remove(key);
        }

        void replaceMirror(Map<K, HazardData> mirror) {
            super.clear();
            super.putAll(mirror);
        }

        protected abstract void registerRuntime(K key, HazardData value);

        protected abstract void removeRuntime(K key);

        @Override
        public HazardData put(K key, HazardData value) {
            HazardData previous = super.put(key, value);
            registerRuntime(key, value);
            return previous;
        }

        @Override
        public void putAll(Map<? extends K, ? extends HazardData> map) {
            for (Entry<? extends K, ? extends HazardData> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public HazardData remove(Object key) {
            boolean hadKey = super.containsKey(key);
            HazardData previous = super.remove(key);
            if (hadKey) {
                removeRuntime((K) key);
            }
            return previous;
        }

        @Override
        public void clear() {
            for (K key : new ArrayList<>(super.keySet())) {
                remove(key);
            }
        }

        @Override
        public Set<Entry<K, HazardData>> entrySet() {
            return liveHazardEntrySet(super.entrySet(), this::registerRuntime, this::removeRuntime);
        }

        @Override
        public Set<K> keySet() {
            return new AbstractSet<>() {
                @Override
                public Iterator<K> iterator() {
                    Iterator<Entry<K, HazardData>> iterator = LegacyHazardMap.this.entrySet().iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public K next() {
                            return iterator.next().getKey();
                        }

                        @Override
                        public void remove() {
                            iterator.remove();
                        }
                    };
                }

                @Override
                public int size() {
                    return LegacyHazardMap.this.size();
                }

                @Override
                public boolean contains(Object value) {
                    return LegacyHazardMap.this.containsKey(value);
                }

                @Override
                public boolean remove(Object value) {
                    boolean hadKey = LegacyHazardMap.this.containsKey(value);
                    LegacyHazardMap.this.remove(value);
                    return hadKey;
                }

                @Override
                public void clear() {
                    LegacyHazardMap.this.clear();
                }
            };
        }

        @Override
        public Collection<HazardData> values() {
            return new AbstractCollection<>() {
                @Override
                public Iterator<HazardData> iterator() {
                    Iterator<Entry<K, HazardData>> iterator = LegacyHazardMap.this.entrySet().iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public HazardData next() {
                            return iterator.next().getValue();
                        }

                        @Override
                        public void remove() {
                            iterator.remove();
                        }
                    };
                }

                @Override
                public int size() {
                    return LegacyHazardMap.this.size();
                }

                @Override
                public boolean contains(Object value) {
                    return LegacyHazardMap.this.containsValue(value);
                }

                @Override
                public void clear() {
                    LegacyHazardMap.this.clear();
                }
            };
        }

        @Override
        public Object clone() {
            return new HashMap<>(this);
        }
    }

    private static final class LegacyOreMap extends LegacyHazardMap<String> {
        @Override
        protected void registerRuntime(String oreName, HazardData data) {
            com.hbm.ntm.radiation.HazardRegistry.registerTag(legacyTag(oreName), data);
        }

        @Override
        protected void removeRuntime(String oreName) {
            com.hbm.ntm.radiation.HazardRegistry.removeTag(legacyTag(oreName));
        }
    }

    private static final class LegacyItemMap extends LegacyHazardMap<Item> {
        @Override
        protected void registerRuntime(Item item, HazardData data) {
            com.hbm.ntm.radiation.HazardRegistry.register(item, data);
        }

        @Override
        protected void removeRuntime(Item item) {
            com.hbm.ntm.radiation.HazardRegistry.remove(item);
        }
    }

    private static final class LegacyStackMap extends LegacyHazardMap<ComparableStack> {
        @Override
        void mirrorPut(ComparableStack key, HazardData value) {
            super.mirrorPut(key == null ? null : key.copy(), value);
        }

        @Override
        protected void registerRuntime(ComparableStack stack, HazardData data) {
            registerRuntimeStack(stack, data);
        }

        @Override
        protected void removeRuntime(ComparableStack stack) {
            removeRuntimeStack(stack);
        }
    }

    private static final class LegacyStackBlacklist extends HashSet<ComparableStack> {
        void mirrorAdd(ComparableStack stack) {
            super.add(singularCopy(stack));
        }

        void mirrorRemove(ComparableStack stack) {
            super.remove(singularCopy(stack));
        }

        void replaceMirror(Set<ComparableStack> mirror) {
            super.clear();
            super.addAll(mirror);
        }

        @Override
        public boolean add(ComparableStack stack) {
            ComparableStack singular = singularCopy(stack);
            boolean added = super.add(singular);
            blacklistRuntimeStack(singular);
            return added;
        }

        @Override
        public boolean remove(Object value) {
            if (!(value instanceof ComparableStack stack)) {
                return false;
            }
            ComparableStack singular = singularCopy(stack);
            boolean removed = super.remove(singular);
            unblacklistRuntimeStack(singular);
            return removed;
        }

        @Override
        public boolean contains(Object value) {
            return value instanceof ComparableStack stack && super.contains(singularCopy(stack));
        }

        @Override
        public Iterator<ComparableStack> iterator() {
            Iterator<ComparableStack> iterator = super.iterator();
            return new Iterator<>() {
                private ComparableStack current;

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public ComparableStack next() {
                    current = iterator.next();
                    return current;
                }

                @Override
                public void remove() {
                    iterator.remove();
                    if (current != null) {
                        unblacklistRuntimeStack(current);
                    }
                }
            };
        }

        @Override
        public void clear() {
            for (ComparableStack stack : new ArrayList<>(this)) {
                remove(stack);
            }
        }

        @Override
        public Object clone() {
            return new HashSet<>(this);
        }
    }

    private static final class LegacyDictBlacklist extends HashSet<String> {
        void mirrorAdd(String oreName) {
            if (oreName != null) {
                super.add(oreName);
            }
        }

        void mirrorRemove(String oreName) {
            super.remove(oreName);
        }

        void replaceMirror(Set<String> mirror) {
            super.clear();
            super.addAll(mirror);
        }

        @Override
        public boolean add(String oreName) {
            if (oreName == null) {
                return false;
            }
            com.hbm.ntm.radiation.HazardRegistry.blacklist(legacyTag(oreName));
            return super.add(oreName);
        }

        @Override
        public boolean remove(Object value) {
            if (!(value instanceof String oreName)) {
                return false;
            }
            boolean removed = super.remove(oreName);
            com.hbm.ntm.radiation.HazardRegistry.unblacklist(legacyTag(oreName));
            return removed;
        }

        @Override
        public Iterator<String> iterator() {
            Iterator<String> iterator = super.iterator();
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
        public void clear() {
            for (String oreName : new ArrayList<>(this)) {
                remove(oreName);
            }
        }

        @Override
        public Object clone() {
            return new HashSet<>(this);
        }
    }

    private HazardSystem() {
    }
}
