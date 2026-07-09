package com.hbm.items.machine;

import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.util.EnumUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for ZIRNOX fuel rods.
 *
 * <p>The modern port keeps the old metadata family split into one item per rod
 * type. This facade preserves the old enum order, NBT helper names, durability
 * helper shape, and {@code instanceof ItemZirnoxRod} identity while delegating
 * runtime behavior to the modern ZIRNOX rod implementation.
 */
@Deprecated(forRemoval = false)
public class ItemZirnoxRod extends com.hbm.ntm.item.ZirnoxRodItem {
    private final EnumZirnoxType legacyType;

    public ItemZirnoxRod() {
        this(new Item.Properties().stacksTo(1).durability(EnumZirnoxType.NATURAL_URANIUM_FUEL.maxLife),
                EnumZirnoxType.NATURAL_URANIUM_FUEL);
    }

    public ItemZirnoxRod(Item.Properties properties, EnumZirnoxType type) {
        super(properties, type.heat, type.breeding);
        this.legacyType = type;
    }

    public EnumZirnoxType legacyType() {
        return legacyType;
    }

    public ItemStack stackFromEnum(int count, Enum<?> material) {
        if (!(material instanceof EnumZirnoxType zirnoxType)) {
            return null;
        }
        return stack(zirnoxType, count);
    }

    public ItemStack stackFromEnum(Enum<?> material) {
        return stackFromEnum(1, material);
    }

    public ItemStack stackFromEnum(int count, EnumZirnoxType type) {
        return stack(type, count);
    }

    public ItemStack stackFromEnum(EnumZirnoxType type) {
        return stack(type, 1);
    }

    public boolean showDurabilityBar(ItemStack stack) {
        return getDurabilityForDisplay(stack) > 0.0D;
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        int maxLife = typeFor(stack).maxLife;
        if (maxLife <= 0) {
            return 0.0D;
        }
        return (double) getLifeTime(stack) / (double) maxLife;
    }

    public static void incrementLifeTime(ItemStack stack) {
        com.hbm.ntm.item.ZirnoxRodItem.incrementLifeTime(stack);
    }

    public static void setLifeTime(ItemStack stack, int time) {
        com.hbm.ntm.item.ZirnoxRodItem.setLifeTime(stack, time);
    }

    public static int getLifeTime(ItemStack stack) {
        return com.hbm.ntm.item.ZirnoxRodItem.getLifeTime(stack);
    }

    public static EnumZirnoxType typeFor(ItemStack stack) {
        if (stack.getItem() instanceof ItemZirnoxRod rod) {
            return rod.legacyType;
        }
        return byMeta(stack.getDamageValue());
    }

    public static EnumZirnoxType byMeta(int meta) {
        return EnumUtil.grabEnumSafely(EnumZirnoxType.class, meta);
    }

    public static ItemStack stack(EnumZirnoxType type, int count) {
        if (type == null) {
            return ItemStack.EMPTY;
        }
        return LegacyMetaItemMappings.stackPreservingCount(
                LegacyMetaItemMappings.ROD_ZIRNOX, type.ordinal(), count)
                .orElse(ItemStack.EMPTY);
    }

    public enum EnumZirnoxType {
        NATURAL_URANIUM_FUEL(250_000, 30),
        URANIUM_FUEL(200_000, 50),
        TH232(20_000, 0, true),
        THORIUM_FUEL(200_000, 40),
        MOX_FUEL(165_000, 75),
        PLUTONIUM_FUEL(175_000, 65),
        U233_FUEL(150_000, 100),
        U235_FUEL(165_000, 85),
        LES_FUEL(150_000, 150),
        LITHIUM(20_000, 0, true),
        ZFB_MOX(50_000, 35);

        public final int maxLife;
        public final int heat;
        public final boolean breeding;

        EnumZirnoxType(int life, int heat, boolean breeding) {
            this.maxLife = life;
            this.heat = heat;
            this.breeding = breeding;
        }

        EnumZirnoxType(int life, int heat) {
            this(life, heat, false);
        }
    }
}
