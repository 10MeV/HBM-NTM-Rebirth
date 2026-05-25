package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidContainerRules;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HbmFluidContainerItem extends Item implements IFillableItem {
    private static final String TAG_FLUID = "hbm_fluid";
    private static final String TAG_AMOUNT = "hbm_fluid_amount";
    private static final String TAG_PRESSURE = "hbm_fluid_pressure";

    private final HbmFluidContainerRules.ContainerKind kind;
    private final int capacity;

    public HbmFluidContainerItem(Properties properties, HbmFluidContainerRules.ContainerKind kind) {
        this(properties, kind, HbmFluidContainerRules.capacity(kind));
    }

    public HbmFluidContainerItem(Properties properties, HbmFluidContainerRules.ContainerKind kind, int capacity) {
        super(properties);
        this.kind = kind;
        this.capacity = Math.max(0, capacity);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        FluidType storedType = getFirstFluidType(stack);
        return type != null
                && type != HbmFluids.NONE
                && HbmFluidContainerRules.accepts(kind, type)
                && (storedType == HbmFluids.NONE || storedType == type)
                && getFill(stack) < capacity;
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        if (amount <= 0 || !acceptsFluid(type, stack)) {
            return Math.max(0, amount);
        }
        int filled = getFill(stack);
        int moved = Math.min(amount, capacity - filled);
        setFluid(stack, type, filled + moved, getPressure(stack));
        return amount - moved;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return type != null && type != HbmFluids.NONE && getFirstFluidType(stack) == type && getFill(stack) > 0;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        if (amount <= 0 || !providesFluid(type, stack)) {
            return 0;
        }
        int moved = Math.min(amount, getFill(stack));
        int remaining = getFill(stack) - moved;
        if (remaining <= 0) {
            clearFluid(stack);
        } else {
            setFluid(stack, type, remaining, getPressure(stack));
        }
        return moved;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_FLUID)) {
            return HbmFluids.NONE;
        }
        FluidType type = HbmFluids.fromName(tag.getString(TAG_FLUID));
        return type == null ? HbmFluids.NONE : type;
    }

    @Override
    public int getFill(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, Math.min(capacity, tag.getInt(TAG_AMOUNT)));
    }

    public ItemStack createFilledStack(FluidType type) {
        return createFilledStack(type, capacity, 0);
    }

    public ItemStack createFilledStack(FluidType type, int amount, int pressure) {
        ItemStack stack = new ItemStack(this);
        setFluid(stack, type, amount, pressure);
        return stack;
    }

    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (FluidType type : HbmFluids.all()) {
            if (type == HbmFluids.NONE || !HbmFluidContainerRules.accepts(kind, type)) {
                continue;
            }
            output.accept(createFilledStack(type));
        }
    }

    public int getTintColor(ItemStack stack) {
        FluidType type = getFirstFluidType(stack);
        return type == HbmFluids.NONE ? 0xFFFFFF : type.getColor();
    }

    public int getCapacity() {
        return capacity;
    }

    public HbmFluidContainerRules.ContainerKind getContainerKind() {
        return kind;
    }

    public int getPressure(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_PRESSURE);
    }

    public void setFluid(ItemStack stack, FluidType type, int amount, int pressure) {
        if (type == null || type == HbmFluids.NONE || amount <= 0) {
            clearFluid(stack);
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_FLUID, type.getName());
        tag.putInt(TAG_AMOUNT, Math.min(amount, capacity));
        tag.putInt(TAG_PRESSURE, pressure);
    }

    public void clearFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(TAG_FLUID);
        tag.remove(TAG_AMOUNT);
        tag.remove(TAG_PRESSURE);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidType type = getFirstFluidType(stack);
        if (type == HbmFluids.NONE) {
            return super.getName(stack);
        }
        return Component.literal(pretty(kind.name()) + " ").append(type.getDisplayName());
    }

    private static String pretty(String raw) {
        StringBuilder builder = new StringBuilder();
        for (String part : raw.toLowerCase().split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
