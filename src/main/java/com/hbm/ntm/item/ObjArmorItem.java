package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.LegacyObjArmorRenderer;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class ObjArmorItem extends ArmorItem {
    private final List<TooltipLine> tooltipLines;

    public ObjArmorItem(ArmorMaterial material, Type type, Properties properties, List<TooltipLine> tooltipLines) {
        super(material, type, properties.stacksTo(1));
        this.tooltipLines = List.copyOf(tooltipLines);
    }

    public ObjArmorItem(ArmorMaterial material, Type type, Properties properties) {
        this(material, type, properties, List.of());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyObjArmorRenderer.acceptObjArmorExtensions(consumer);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (TooltipLine line : tooltipLines) {
            tooltip.add(Component.translatable(line.translationKey()).withStyle(line.color()));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public record TooltipLine(String translationKey, ChatFormatting color) {
    }
}
