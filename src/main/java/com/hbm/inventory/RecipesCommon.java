package com.hbm.inventory;

import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Legacy 1.7.10 recipe input helper facade.
 * <p>
 * Modern recipe storage remains datapack-first; this class only preserves the
 * old ComparableStack/OreDictStack matching surface for migrated helpers and
 * serializers that still speak the legacy API.
 */
@Deprecated(forRemoval = false)
public class RecipesCommon {
    public static ItemStack[] copyStackArray(ItemStack[] array) {
        if (array == null) {
            return null;
        }

        ItemStack[] clone = new ItemStack[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                clone[i] = array[i].copy();
            }
        }
        return clone;
    }

    public static ItemStack[] objectToStackArray(Object[] array) {
        if (array == null) {
            return null;
        }

        ItemStack[] clone = new ItemStack[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] instanceof ItemStack stack) {
                clone[i] = stack;
            }
        }
        return clone;
    }

    public abstract static class AStack implements Comparable<AStack> {
        public int stacksize;

        public abstract boolean matchesRecipe(ItemStack stack, boolean ignoreSize);

        public abstract AStack copy();

        public abstract AStack copy(int stacksize);

        public abstract List<ItemStack> extractForNEI();

        public ItemStack extractForCyclingDisplay(int cycle) {
            List<ItemStack> list = extractForNEI();
            cycle *= 50;
            if (list.isEmpty()) {
                return new ItemStack(nothing());
            }
            return list.get((int) (System.currentTimeMillis() % (cycle * list.size()) / cycle));
        }
    }

    public static class ComparableStack extends AStack {
        public Item item;
        public int meta;

        public ComparableStack(ItemStack stack) {
            if (stack == null) {
                this.item = nothing();
                this.stacksize = 1;
                this.meta = 0;
                return;
            }
            try {
                this.item = stack.getItem();
                if (this.item == null) {
                    this.item = nothing();
                }
                this.stacksize = stack.getCount();
                this.meta = stack.getDamageValue();
            } catch (Exception exception) {
                this.item = nothing();
                if (!GeneralConfig.enableSilentCompStackErrors) {
                    exception.printStackTrace();
                }
            }
        }

        public ComparableStack makeSingular() {
            stacksize = 1;
            return this;
        }

        public ComparableStack(Item item) {
            this.item = item == null ? nothing() : item;
            this.stacksize = 1;
            this.meta = 0;
        }

        public ComparableStack(Block block) {
            this.item = block == null ? null : block.asItem();
            this.stacksize = 1;
            this.meta = 0;
        }

        public ComparableStack(Block block, int stacksize) {
            this(block);
            this.stacksize = stacksize;
        }

        public ComparableStack(Block block, int stacksize, int meta) {
            this(block, stacksize);
            this.meta = meta;
        }

        public ComparableStack(Block block, int stacksize, Enum<?> meta) {
            this(block, stacksize, meta.ordinal());
        }

        public ComparableStack(Item item, int stacksize) {
            this(item);
            this.stacksize = stacksize;
        }

        public ComparableStack(Item item, int stacksize, int meta) {
            this(item, stacksize);
            this.meta = meta;
        }

        public ComparableStack(Item item, int stacksize, Enum<?> meta) {
            this(item, stacksize, meta.ordinal());
        }

        public ItemStack toStack() {
            ItemStack stack = new ItemStack(item == null ? nothing() : item, stacksize);
            stack.setDamageValue(legacyItemDamage(meta));
            return stack;
        }

        public String[] getDictKeys() {
            return toStack().getTags()
                    .flatMap(tag -> LegacyOreDictionaryMappings.legacyNamesForTag(tag.location()).stream())
                    .distinct()
                    .toArray(String[]::new);
        }

        @Override
        public int hashCode() {
            if (item == null) {
                if (!GeneralConfig.enableSilentCompStackErrors) {
                    MainRegistry.logger.error("ComparableStack has a null item! This is a serious issue!");
                    Thread.currentThread().dumpStack();
                }
                item = nothing();
            }

            ResourceLocation name = ForgeRegistries.ITEMS.getKey(item);
            if (name == null) {
                if (!GeneralConfig.enableSilentCompStackErrors) {
                    MainRegistry.logger.error("ComparableStack holds an item that does not seem to be registered. How does that even happen? This error can be turned off with the config <enableSilentCompStackErrors>. Item name: " + item.getDescriptionId());
                    Thread.currentThread().dumpStack();
                }
                item = nothing();
                name = ForgeRegistries.ITEMS.getKey(item);
            }

            int result = 1;
            result = 31 * result + (name == null ? 0 : name.toString().hashCode());
            result = 31 * result + meta;
            result = 31 * result + stacksize;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ComparableStack other = (ComparableStack) obj;
            if (item == null) {
                if (other.item != null) {
                    return false;
                }
            } else if (!item.equals(other.item)) {
                return false;
            }
            if (meta != HbmIngredient.WILDCARD_META && other.meta != HbmIngredient.WILDCARD_META
                    && meta != other.meta) {
                return false;
            }
            return stacksize == other.stacksize;
        }

        @Override
        public int compareTo(AStack stack) {
            if (stack instanceof NBTStack) {
                return -1;
            }
            if (stack instanceof OreDictStack) {
                return 1;
            }
            if (stack instanceof ComparableStack comp) {
                int idCompare = Integer.compare(rawItemId(item), rawItemId(comp.item));
                if (idCompare != 0) {
                    return idCompare;
                }
                return Integer.compare(meta, comp.meta);
            }
            return 0;
        }

        @Override
        public ComparableStack copy() {
            return new ComparableStack(item, stacksize, meta);
        }

        @Override
        public ComparableStack copy(int stacksize) {
            return new ComparableStack(item, stacksize, meta);
        }

        @Override
        public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {
            if (stack == null) {
                return false;
            }
            if (stack.getItem() != this.item) {
                return false;
            }
            if (this.meta != HbmIngredient.WILDCARD_META && stack.getDamageValue() != this.meta) {
                return false;
            }
            return ignoreSize || stack.getCount() >= this.stacksize;
        }

        @Override
        public List<ItemStack> extractForNEI() {
            return Arrays.asList(this.toStack());
        }

        @Override
        public String toString() {
            return this.stacksize + "x" + itemDebugName(item) + "@" + this.meta;
        }
    }

    public static class NBTStack extends ComparableStack {
        public CompoundTag nbt;

        public NBTStack(Item item) {
            super(item);
        }

        public NBTStack(Block block) {
            super(block);
        }

        public NBTStack(Block block, int stacksize) {
            super(block, stacksize);
        }

        public NBTStack(Block block, int stacksize, int meta) {
            super(block, stacksize, meta);
        }

        public NBTStack(Block block, int stacksize, Enum<?> meta) {
            super(block, stacksize, meta);
        }

        public NBTStack(Item item, int stacksize) {
            super(item, stacksize);
        }

        public NBTStack(Item item, int stacksize, int meta) {
            super(item, stacksize, meta);
        }

        public NBTStack(Item item, int stacksize, Enum<?> meta) {
            super(item, stacksize, meta);
        }

        public NBTStack(ItemStack stack) {
            super(stack.getItem(), stack.getCount(), stack.getDamageValue());
            this.withNBT(stack.getTag());
        }

        public NBTStack withNBT(CompoundTag nbt) {
            this.nbt = nbt;
            return this;
        }

        public NBTStack initNBT() {
            if (this.nbt == null) {
                this.nbt = new CompoundTag();
            }
            return this;
        }

        public NBTStack setInt(String key, int value) {
            initNBT().nbt.putInt(key, value);
            return this;
        }

        @Override
        public ItemStack toStack() {
            ItemStack stack = super.toStack();
            if (this.nbt != null) {
                stack.setTag(this.nbt.copy());
            }
            return stack;
        }

        @Override
        public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {
            if (!super.matchesRecipe(stack, ignoreSize)) {
                return false;
            }
            if (this.nbt == null || this.nbt.isEmpty()) {
                return true;
            }
            if (!stack.hasTag()) {
                return false;
            }

            CompoundTag stackTag = stack.getTag();
            Set<String> neededKeys = this.nbt.getAllKeys();
            for (String key : neededKeys) {
                Tag tag = stackTag.get(key);
                if (tag == null || !this.nbt.get(key).equals(tag)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public NBTStack copy() {
            return new NBTStack(item, stacksize, meta).withNBT(nbt == null ? null : nbt.copy());
        }

        @Override
        public NBTStack copy(int stacksize) {
            return new NBTStack(item, stacksize, meta).withNBT(nbt == null ? null : nbt.copy());
        }

        @Override
        public int compareTo(AStack stack) {
            if (stack instanceof NBTStack comp) {
                int idCompare = Integer.compare(rawItemId(item), rawItemId(comp.item));
                if (idCompare != 0) {
                    return idCompare;
                }
                int metaCompare = Integer.compare(meta, comp.meta);
                if (metaCompare != 0) {
                    return metaCompare;
                }
                if (nbt != null && comp.nbt == null) {
                    return 1;
                }
                if (nbt == null && comp.nbt != null) {
                    return -1;
                }
                return 0;
            }
            if (stack instanceof ComparableStack || stack instanceof OreDictStack) {
                return 1;
            }
            return 0;
        }

        @Override
        public String toString() {
            return this.stacksize + "x" + itemDebugName(item) + "@" + this.meta + "?" + this.nbt;
        }
    }

    public static class OreDictStack extends AStack {
        public String name;

        public OreDictStack(String name) {
            this.name = name;
            this.stacksize = 1;
        }

        public OreDictStack(String name, int stacksize) {
            this(name);
            this.stacksize = stacksize;
        }

        public List<ItemStack> toStacks() {
            return Collections.unmodifiableList(new ArrayList<>(
                    Arrays.asList(Ingredient.of(LegacyOreDictionaryMappings.itemTag(name)).getItems())));
        }

        @Override
        public int compareTo(AStack stack) {
            if (stack instanceof OreDictStack comp) {
                return name.compareTo(comp.name);
            }
            if (stack instanceof ComparableStack) {
                return -1;
            }
            return 0;
        }

        @Override
        public OreDictStack copy() {
            return new OreDictStack(name, stacksize);
        }

        @Override
        public OreDictStack copy(int stacksize) {
            return new OreDictStack(name, stacksize);
        }

        @Override
        public boolean matchesRecipe(ItemStack stack, boolean ignoreSize) {
            if (stack == null) {
                return false;
            }
            if (!ignoreSize && stack.getCount() < this.stacksize) {
                return false;
            }
            return stack.is(LegacyOreDictionaryMappings.itemTag(name));
        }

        @Override
        public List<ItemStack> extractForNEI() {
            return displayStacks();
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + (name == null ? 0 : name.hashCode());
            result = 31 * result + this.stacksize;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            OreDictStack other = (OreDictStack) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return this.stacksize == other.stacksize;
        }

        @Override
        public String toString() {
            return this.stacksize + "x" + this.name;
        }

        private List<ItemStack> displayStacks() {
            return new ArrayList<>(Arrays.stream(Ingredient.of(LegacyOreDictionaryMappings.itemTag(name)).getItems())
                    .map(stack -> {
                        ItemStack copy = stack.copy();
                        copy.setCount(stacksize);
                        return copy;
                    })
                    .toList());
        }
    }

    public static class MetaBlock {
        public Block block;
        public int meta;

        public MetaBlock(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }

        public MetaBlock(Block block) {
            this(block, 0);
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + ForgeRegistries.BLOCKS.getKey(block).toString().hashCode();
            result = 31 * result + meta;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MetaBlock other = (MetaBlock) obj;
            if (block == null) {
                if (other.block != null) {
                    return false;
                }
            } else if (!block.equals(other.block)) {
                return false;
            }
            return meta == other.meta;
        }

        @Deprecated
        public int getID() {
            return hashCode();
        }
    }

    private static Item nothing() {
        return ModItems.NOTHING.get();
    }

    private static String itemDebugName(Item item) {
        return item == null ? "null" : item.getDescriptionId();
    }

    private static int rawItemId(Item item) {
        return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
    }

    private static int legacyItemDamage(int meta) {
        return Math.max(0, meta);
    }
}
