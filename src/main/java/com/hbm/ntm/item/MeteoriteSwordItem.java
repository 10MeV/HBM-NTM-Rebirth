package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class MeteoriteSwordItem extends HbmAbilitySwordItem {
    private final String legacyStageKey;

    public MeteoriteSwordItem(float attackDamage, String legacyStageKey, Properties properties) {
        super(HbmToolTiers.METEORITE, attackDamage, 0.0D, properties);
        this.legacyStageKey = legacyStageKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Component description = Component.translatable("item.hbm_ntm_rebirth.meteorite_sword." + legacyStageKey + ".desc");
        for (String line : description.getString().split("\\$")) {
            tooltip.add(Component.literal(line).withStyle(ChatFormatting.ITALIC));
        }
    }
}
