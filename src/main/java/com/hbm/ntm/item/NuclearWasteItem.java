package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NuclearWasteItem extends Item {
    private static final String[] LONG_NAMES = {
            "Uranium-235",
            "Uranium-233",
            "Neptunium-237",
            "Thorium-232",
            "Schrabidium-326"
    };
    private static final String[] SHORT_NAMES = {
            "Uranium-235",
            "Uranium-233",
            "Neptunium-237",
            "Plutonium-239",
            "Plutonium-240",
            "Plutonium-241",
            "Americium-242",
            "Schrabidium-326"
    };

    private final WasteFamily family;

    public NuclearWasteItem(Properties properties, WasteFamily family) {
        super(properties);
        this.family = family;
    }

    public WasteFamily family() {
        return family;
    }

    public static ItemStack stack(Item item, int meta, int count) {
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        stack.setDamageValue(Math.max(0, meta));
        return stack;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, NuclearWasteItem item) {
        for (int meta = 0; meta < item.family.variantCount(); meta++) {
            output.accept(stack(item, meta, 1));
        }
    }

    public static int rectify(int meta, int variants) {
        return variants <= 0 ? 0 : Math.abs(meta) % variants;
    }

    public int wasteMeta(ItemStack stack) {
        return rectify(stack.getDamageValue(), family.variantCount());
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return family.hasLegacyWasteEntity() ? Integer.MAX_VALUE : super.getEntityLifespan(stack, level);
    }

    @Override
    public boolean canBeHurtBy(DamageSource damageSource) {
        return !family.hasLegacyWasteEntity() && super.canBeHurtBy(damageSource);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal(family.variantName(wasteMeta(stack))).withStyle(ChatFormatting.ITALIC));
    }

    public enum WasteFamily {
        LONG(LONG_NAMES, true),
        SHORT(SHORT_NAMES, false);

        private final String[] names;
        private final boolean legacyWasteEntity;

        WasteFamily(String[] names, boolean legacyWasteEntity) {
            this.names = names;
            this.legacyWasteEntity = legacyWasteEntity;
        }

        public int variantCount() {
            return names.length;
        }

        public String variantName(int meta) {
            return names[rectify(meta, names.length)];
        }

        public boolean hasLegacyWasteEntity() {
            return legacyWasteEntity;
        }
    }
}
