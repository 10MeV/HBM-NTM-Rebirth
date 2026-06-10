package com.hbm.ntm.item.missile;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class MissilePartItem extends Item {
    private final PartType type;
    private final String legacyModelKey;

    public MissilePartItem(Properties properties, PartType type, String legacyModelKey) {
        super(properties);
        this.type = type;
        this.legacyModelKey = legacyModelKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.missile_part.type." + type.serializedName())
                .withStyle(ChatFormatting.GRAY));
        if (!legacyModelKey.isEmpty()) {
            tooltip.add(Component.literal(legacyModelKey).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public PartType type() {
        return type;
    }

    public String legacyModelKey() {
        return legacyModelKey;
    }

    public enum PartType {
        CHIP,
        WARHEAD,
        FUSELAGE,
        FINS,
        THRUSTER;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
