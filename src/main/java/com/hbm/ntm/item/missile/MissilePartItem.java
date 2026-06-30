package com.hbm.ntm.item.missile;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

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

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        if (usesObjItemRenderer()) {
            acceptClientExtensions("com.hbm.ntm.client.renderer.MissileItemRendererBridge", consumer);
        }
    }

    private static void acceptClientExtensions(String className, Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName(className);
            bridge.getMethod("acceptMissilePart", Consumer.class).invoke(null, consumer);
        } catch (ReflectiveOperationException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocation && invocation.getCause() != null
                    ? invocation.getCause()
                    : exception;
            throw new IllegalStateException("Unable to initialize missile part item client renderer", cause);
        }
    }

    public PartType type() {
        return type;
    }

    public String legacyModelKey() {
        return legacyModelKey;
    }

    public boolean usesObjItemRenderer() {
        return type != PartType.CHIP;
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
