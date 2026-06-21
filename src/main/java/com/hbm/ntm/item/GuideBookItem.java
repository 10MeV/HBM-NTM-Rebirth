package com.hbm.ntm.item;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GuideBookItem extends Item {
    public static final String TAG_BOOK_TYPE = "BookType";

    public GuideBookItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack stack(Item item, BookType type) {
        ItemStack stack = new ItemStack(item);
        setType(stack, type);
        return stack;
    }

    public static void setType(ItemStack stack, BookType type) {
        stack.getOrCreateTag().putInt(TAG_BOOK_TYPE, type.legacyMeta());
    }

    public static BookType getType(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_BOOK_TYPE)) {
            return BookType.byLegacyMeta(stack.getTag().getInt(TAG_BOOK_TYPE));
        }
        return BookType.TEST;
    }

    public static boolean isType(ItemStack stack, BookType type) {
        return stack.getItem() instanceof GuideBookItem && getType(stack) == type;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, Item item) {
        output.accept(stack(item, BookType.RBMK));
        output.accept(stack(item, BookType.STARTER));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getType(stack).coverKey()).withStyle(ChatFormatting.GRAY));
    }

    public enum BookType {
        TEST(0, "book.test.cover"),
        RBMK(1, "book.rbmk.cover"),
        HADRON(2, "book.error.cover"),
        STARTER(3, "book.starter.cover");

        private final int legacyMeta;
        private final String coverKey;

        BookType(int legacyMeta, String coverKey) {
            this.legacyMeta = legacyMeta;
            this.coverKey = coverKey;
        }

        public int legacyMeta() {
            return legacyMeta;
        }

        public String coverKey() {
            return coverKey;
        }

        public String id() {
            return name().toUpperCase(Locale.ROOT);
        }

        public static BookType byLegacyMeta(int meta) {
            BookType[] values = values();
            return values[Math.abs(meta) % values.length];
        }
    }
}
