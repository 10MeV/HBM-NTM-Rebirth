package com.hbm.items.machine;

import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModItems;
import com.hbm.util.EnumUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

/**
 * Legacy 1.7.10 package bridge for breeder rod metadata families.
 *
 * <p>The modern port splits old {@code rod}, {@code rod_dual}, and
 * {@code rod_quad} metadata families into separate item IDs. This facade keeps
 * the legacy enum order and stack helper surface on those split items without
 * restoring a second metadata item registry.
 */
@Deprecated(forRemoval = false)
public class ItemBreedingRod extends Item {
    private final RodFamily family;
    private final BreedingRodType type;

    public ItemBreedingRod() {
        this(new Item.Properties(), RodFamily.SINGLE, BreedingRodType.LITHIUM);
    }

    public ItemBreedingRod(Item.Properties properties, RodFamily family, BreedingRodType type) {
        super(properties);
        this.family = family;
        this.type = type;
    }

    public RodFamily family() {
        return family;
    }

    public BreedingRodType type() {
        return type;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return family != null;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (family == null) {
            return ItemStack.EMPTY;
        }
        String emptyRodName = switch (family) {
            case SINGLE -> "rod_empty";
            case DUAL -> "rod_dual_empty";
            case QUAD -> "rod_quad_empty";
        };
        RegistryObject<Item> emptyRod = ModItems.legacyItem(emptyRodName);
        return emptyRod == null ? ItemStack.EMPTY : new ItemStack(emptyRod.get());
    }

    public ItemStack stackFromEnum(int count, Enum<?> material) {
        if (!(material instanceof BreedingRodType rodType)) {
            return null;
        }
        return stack(family, rodType, count);
    }

    public ItemStack stackFromEnum(Enum<?> material) {
        return stackFromEnum(1, material);
    }

    public ItemStack stackFromEnum(int count, BreedingRodType type) {
        return stack(family, type, count);
    }

    public ItemStack stackFromEnum(BreedingRodType type) {
        return stackFromEnum(type, 1);
    }

    public ItemStack stackFromEnum(BreedingRodType type, int count) {
        return stack(family, type, count);
    }

    public static BreedingRodType byMeta(int meta) {
        return EnumUtil.grabEnumSafely(BreedingRodType.class, meta);
    }

    public static BreedingRodType typeFor(ItemStack stack) {
        if (stack.getItem() instanceof ItemBreedingRod rod) {
            return rod.type;
        }
        return byMeta(stack.getDamageValue());
    }

    public static RodFamily familyFor(ItemStack stack) {
        if (stack.getItem() instanceof ItemBreedingRod rod) {
            return rod.family;
        }
        return RodFamily.SINGLE;
    }

    public static ItemStack stack(RodFamily family, BreedingRodType type, int count) {
        if (family == null || type == null) {
            return ItemStack.EMPTY;
        }
        return LegacyMetaItemMappings.stackPreservingCount(family.legacyId(), type.ordinal(), count)
                .orElse(ItemStack.EMPTY);
    }

    public enum BreedingRodType {
        LITHIUM,
        TRITIUM,
        CO,
        CO60,
        TH232,
        THF,
        U235,
        NP237,
        U238,
        PU238,
        PU239,
        RGP,
        WASTE,
        LEAD,
        URANIUM,
        RA226,
        AC227
    }

    public enum RodFamily {
        SINGLE(LegacyMetaItemMappings.ROD, "rod"),
        DUAL(LegacyMetaItemMappings.ROD_DUAL, "rod_dual"),
        QUAD(LegacyMetaItemMappings.ROD_QUAD, "rod_quad");

        private final ResourceLocation legacyId;
        private final String prefix;

        RodFamily(ResourceLocation legacyId, String prefix) {
            this.legacyId = legacyId;
            this.prefix = prefix;
        }

        public ResourceLocation legacyId() {
            return legacyId;
        }

        public String registryName(BreedingRodType type) {
            return prefix + "_" + type.name().toLowerCase(java.util.Locale.US);
        }
    }
}
