package com.hbm.ntm.item.missile;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;

public class CustomMissileItem extends Item {
    public static final String TAG_CHIP = "chip";
    public static final String TAG_WARHEAD = "warhead";
    public static final String TAG_FUSELAGE = "fuselage";
    public static final String TAG_STABILITY = "stability";
    public static final String TAG_THRUSTER = "thruster";

    public CustomMissileItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        acceptClientExtensions("com.hbm.ntm.client.renderer.MissileItemRendererBridge", consumer);
    }

    private static void acceptClientExtensions(String className, Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName(className);
            bridge.getMethod("acceptCustomMissile", Consumer.class).invoke(null, consumer);
        } catch (ReflectiveOperationException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocation && invocation.getCause() != null
                    ? invocation.getCause()
                    : exception;
            throw new IllegalStateException("Unable to initialize custom missile item client renderer", cause);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.custom_missile.empty")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        appendPart(tooltip, "chip", tag, TAG_CHIP);
        appendPart(tooltip, "warhead", tag, TAG_WARHEAD);
        appendPart(tooltip, "fuselage", tag, TAG_FUSELAGE);
        appendPart(tooltip, "stability", tag, TAG_STABILITY);
        appendPart(tooltip, "thruster", tag, TAG_THRUSTER);
    }

    public static void setPart(ItemStack stack, String key, ItemStack part) {
        CompoundTag tag = stack.getOrCreateTag();
        if (part.isEmpty()) {
            tag.remove(key);
            return;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(part.getItem());
        if (id != null) {
            tag.putString(key, id.toString());
        }
    }

    public static ItemStack buildMissile(ItemStack chip, ItemStack warhead, ItemStack fuselage,
            @Nullable ItemStack stability, ItemStack thruster) {
        ItemStack missile = new ItemStack(ModItems.MISSILE_CUSTOM.get());
        setPart(missile, TAG_CHIP, chip);
        setPart(missile, TAG_WARHEAD, warhead);
        setPart(missile, TAG_FUSELAGE, fuselage);
        if (stability != null && !stability.isEmpty()) {
            setPart(missile, TAG_STABILITY, stability);
        }
        setPart(missile, TAG_THRUSTER, thruster);
        return missile;
    }

    public static boolean isCompleteForLaunch(ItemStack stack) {
        CustomMissilePartProfile.Assembly assembly = CustomMissilePartProfile.assemblyFromStack(stack);
        return assembly != null && assembly.isCompleteForLaunch();
    }

    @Nullable
    public static ResourceLocation getPartId(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key)) {
            return null;
        }
        return ResourceLocation.tryParse(tag.getString(key));
    }

    private static void appendPart(List<Component> tooltip, String label, CompoundTag tag, String key) {
        if (tag.contains(key)) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.custom_missile." + label, tag.getString(key))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
