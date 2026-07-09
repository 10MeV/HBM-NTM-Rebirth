package com.hbm.items.machine;

import com.hbm.ntm.item.RBMKPelletItem;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.neutron.RBMKItemPlanner;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy 1.7.10 package bridge for RBMK fuel pellets.
 */
@Deprecated(forRemoval = false)
public class ItemRBMKPellet extends RBMKPelletItem {
    public String fullName = "";
    protected boolean hasXenon = true;

    public ItemRBMKPellet(Item.Properties properties, RBMKFuelRodRegistry.Entry entry) {
        super(properties, entry);
        this.fullName = entry.fullName();
        this.hasXenon = entry.pelletXenonOverlay();
    }

    public ItemRBMKPellet(String fullName) {
        this(new Item.Properties(), entryByFullName(fullName));
        this.fullName = fullName;
    }

    public ItemRBMKPellet disableXenon() {
        this.hasXenon = false;
        return this;
    }

    @Override
    public boolean isXenonEnabled() {
        return hasXenon;
    }

    @Override
    public RBMKItemPlanner.PelletMetaPlan getMetaPlan(ItemStack stack) {
        return RBMKItemPlanner.pelletMeta(stack.getDamageValue(), hasXenon);
    }

    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    public int getRenderPasses(int meta) {
        return hasXenon(meta) ? 3 : 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RBMKItemPlanner.PelletTooltipPlan plan =
                RBMKItemPlanner.pelletTooltip(fullName, stack.getDamageValue(), hasXenon);
        for (RBMKItemPlanner.TooltipLine line : plan.lines()) {
            tooltip.add(applyStyle(component(line), line.style()));
        }
    }

    public static boolean hasXenon(int meta) {
        return rectify(meta) >= 5;
    }

    public static int rectify(int meta) {
        return Math.abs(meta) % 10;
    }

    private static RBMKFuelRodRegistry.Entry entryByFullName(String fullName) {
        return RBMKFuelRodRegistry.all().stream()
                .filter(entry -> entry.fullName().equals(fullName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown RBMK pellet full name: " + fullName));
    }

    private static MutableComponent component(RBMKItemPlanner.TooltipLine line) {
        if (!line.literal().isEmpty()) {
            return Component.literal(line.literal());
        }
        if (!line.argumentTranslationKey().isEmpty()) {
            return Component.translatable(line.translationKey(), Component.translatable(line.argumentTranslationKey()));
        }
        if (!line.argument().isEmpty()) {
            return Component.translatable(line.translationKey(), line.argument());
        }
        return Component.translatable(line.translationKey());
    }

    private static MutableComponent applyStyle(MutableComponent component, RBMKItemPlanner.TooltipStyle style) {
        return switch (style) {
            case ITALIC -> component.withStyle(ChatFormatting.ITALIC);
            case DARK_GRAY_ITALIC -> component.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
            case GOLD -> component.withStyle(ChatFormatting.GOLD);
            case RED -> component.withStyle(ChatFormatting.RED);
            case GREEN -> component.withStyle(ChatFormatting.GREEN);
            case DARK_PURPLE -> component.withStyle(ChatFormatting.DARK_PURPLE);
            case BLUE -> component.withStyle(ChatFormatting.BLUE);
            case YELLOW -> component.withStyle(ChatFormatting.YELLOW);
            case DARK_RED -> component.withStyle(ChatFormatting.DARK_RED);
            case DARK_GREEN -> component.withStyle(ChatFormatting.DARK_GREEN);
            case DARK_GRAY -> component.withStyle(ChatFormatting.DARK_GRAY);
        };
    }
}
