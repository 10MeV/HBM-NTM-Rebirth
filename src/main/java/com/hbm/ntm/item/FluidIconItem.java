package com.hbm.ntm.item;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FluidIconItem extends Item {
    private static final String TAG_FLUID = "hbm_fluid";
    private static final String TAG_AMOUNT = "hbm_fluid_amount";
    private static final String TAG_PRESSURE = "hbm_fluid_pressure";

    public FluidIconItem(Properties properties) {
        super(properties);
    }

    public static ItemStack make(FluidType fluid, int amount) {
        return make(fluid, amount, 0);
    }

    public static ItemStack make(FluidType fluid, int amount, int pressure) {
        ItemStack stack = new ItemStack(ModItems.FLUID_ICON.get());
        setFluid(stack, fluid, amount, pressure);
        return stack;
    }

    public static FluidType getFluidType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_FLUID)) {
            return HbmFluids.NONE;
        }
        return HbmFluids.fromName(tag.getString(TAG_FLUID));
    }

    public static int getAmount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, tag.getInt(TAG_AMOUNT));
    }

    public static int getPressure(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, tag.getInt(TAG_PRESSURE));
    }

    public static int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex <= 0) {
            return 0xFFFFFF;
        }
        FluidType type = getFluidType(stack);
        return type == HbmFluids.NONE ? 0xFFFFFF : type.getColor();
    }

    private static void setFluid(ItemStack stack, FluidType fluid, int amount, int pressure) {
        if (fluid == null || fluid == HbmFluids.NONE) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_FLUID, fluid.getName());
        if (amount > 0) {
            tag.putInt(TAG_AMOUNT, amount);
        }
        if (pressure > 0) {
            tag.putInt(TAG_PRESSURE, pressure);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidType type = getFluidType(stack);
        return type == HbmFluids.NONE ? super.getName(stack) : type.getDisplayName();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int amount = getAmount(stack);
        if (amount > 0) {
            tooltip.add(Component.literal(amount + "mB"));
        }
        int pressure = getPressure(stack);
        if (pressure > 0) {
            tooltip.add(Component.literal(pressure + "PU").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Pressurized, use compressor!").withStyle(ChatFormatting.DARK_RED));
        }
        FluidType type = getFluidType(stack);
        if (type != HbmFluids.NONE) {
            type.appendInfo(tooltip, HbmFluidGuiHelper.showHiddenFluidInfo());
        }
    }
}
