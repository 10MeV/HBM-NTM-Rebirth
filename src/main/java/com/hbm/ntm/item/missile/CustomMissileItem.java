package com.hbm.ntm.item.missile;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.explosion.CustomMissileExplosion;
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
            return;
        }
        CustomMissilePartProfile.Assembly assembly = CustomMissilePartProfile.assemblyFromStack(stack);
        if (assembly == null || assembly.chip() == null || assembly.warhead() == null
                || assembly.fuselage() == null || assembly.thruster() == null) {
            tooltip.add(Component.translatable("error.generic").withStyle(ChatFormatting.RED));
            return;
        }

        CustomMissilePartProfile warhead = assembly.warhead().profile();
        CustomMissilePartProfile fuselage = assembly.fuselage().profile();
        CustomMissilePartProfile chip = assembly.chip().profile();
        CustomMissilePartProfile fins = assembly.fins() == null ? null : assembly.fins().profile();

        tooltip.add(descriptionLine("item.missile.desc.warhead", warheadName(warhead.warheadType())));
        tooltip.add(descriptionLine("item.missile.desc.strength", gray(Float.toString(warhead.strength()))));
        tooltip.add(descriptionLine("item.missile.desc.fuelType", fuelName(fuselage.fuelType())));
        tooltip.add(descriptionLine("item.missile.desc.fuelAmount",
                gray(Float.toString(fuselage.fuel()) + "l")));
        tooltip.add(descriptionLine("item.missile.desc.chipInaccuracy",
                gray(Float.toString(chip.inaccuracy() * 100.0F) + "%")));
        tooltip.add(descriptionLine("item.missile.desc.finInaccuracy",
                gray(Float.toString((fins == null ? 1.0F : fins.inaccuracy()) * 100.0F) + "%")));
        Component size = sizeName(fuselage.top()).copy()
                .append(Component.literal("/"))
                .append(sizeName(fuselage.bottom()));
        tooltip.add(descriptionLine("item.missile.desc.size",
                gray(size)));
        tooltip.add(descriptionLine("item.missile.desc.health",
                gray(Float.toString(assembly.displayHealth()) + "HP")));
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

    private static Component descriptionLine(String labelKey, Component value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.BOLD)
                .append(Component.literal(": "))
                .append(value);
    }

    private static Component gray(String value) {
        return Component.literal(value).withStyle(ChatFormatting.GRAY);
    }

    private static Component gray(Component value) {
        return value.copy().withStyle(ChatFormatting.GRAY);
    }

    private static Component warheadName(@Nullable CustomMissileExplosion.WarheadType type) {
        if (type == null) {
            return Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        }
        return switch (type) {
            case HE -> Component.translatable("item.warhead.desc.he").withStyle(ChatFormatting.YELLOW);
            case INC -> Component.translatable("item.warhead.desc.incendiary").withStyle(ChatFormatting.GOLD);
            case CLUSTER -> Component.translatable("item.warhead.desc.cluster").withStyle(ChatFormatting.GRAY);
            case BUSTER -> Component.translatable("item.warhead.desc.bunker_buster").withStyle(ChatFormatting.WHITE);
            case NUCLEAR -> Component.translatable("item.warhead.desc.nuclear").withStyle(ChatFormatting.DARK_GREEN);
            case TX -> Component.translatable("item.warhead.desc.thermonuclear").withStyle(ChatFormatting.DARK_PURPLE);
            case N2 -> Component.translatable("item.warhead.desc.n2").withStyle(ChatFormatting.RED);
            case BALEFIRE -> Component.translatable("item.warhead.desc.balefire").withStyle(ChatFormatting.GREEN);
            case SCHRAB -> Component.translatable("item.warhead.desc.schrabidium").withStyle(ChatFormatting.AQUA);
            case TAINT -> Component.translatable("item.warhead.desc.taint").withStyle(ChatFormatting.DARK_PURPLE);
            case CLOUD -> Component.translatable("item.warhead.desc.cloud").withStyle(ChatFormatting.LIGHT_PURPLE);
            case TURBINE -> Component.translatable("item.warhead.desc.turbine")
                    .withStyle(System.currentTimeMillis() % 1000L < 500L ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            case CUSTOM0, CUSTOM1, CUSTOM2, CUSTOM3, CUSTOM4, CUSTOM5, CUSTOM6, CUSTOM7, CUSTOM8, CUSTOM9 ->
                    Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        };
    }

    private static Component fuelName(@Nullable CustomMissilePartProfile.FuelType type) {
        if (type == null) {
            return Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        }
        return switch (type) {
            case KEROSENE -> Component.translatable("item.missile.fuel.kerosene_peroxide")
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
            case SOLID -> Component.translatable("item.missile.fuel.solid").withStyle(ChatFormatting.GOLD);
            case HYDROGEN -> Component.translatable("item.missile.fuel.hydrogen").withStyle(ChatFormatting.DARK_AQUA);
            case XENON -> Component.translatable("item.missile.fuel.xenon").withStyle(ChatFormatting.DARK_PURPLE);
            case BALEFIRE -> Component.translatable("item.missile.fuel.balefire").withStyle(ChatFormatting.GREEN);
        };
    }

    private static Component sizeName(CustomMissilePartProfile.PartSize size) {
        return switch (size) {
            case SIZE_10 -> Component.literal("1.0m");
            case SIZE_15 -> Component.literal("1.5m");
            case SIZE_20 -> Component.literal("2.0m");
            case ANY -> Component.translatable("item.missile.part.size.any");
            case NONE -> Component.translatable("item.missile.part.size.none");
        };
    }
}
