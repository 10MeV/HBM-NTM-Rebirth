package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerItemCapabilityProvider;
import com.hbm.ntm.fluid.HbmFluidContainerRules;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmForgeFluidInterop;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.ContainerFluidTrait;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class HbmFluidContainerItem extends Item implements IFillableItem, HbmForgeFluidInterop.HbmFluidContainerItemAccess {
    private static final String TAG_FLUID = "hbm_fluid";
    private static final String TAG_AMOUNT = "hbm_fluid_amount";
    private static final String TAG_PRESSURE = "hbm_fluid_pressure";
    private static final String LEGACY_TAG_TYPE = "type";
    private static final String LEGACY_TAG_AMOUNT = "fill";
    private static final String LEGACY_TAG_PRESSURE = "pressure";

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
        return type != null
                && type != HbmFluids.NONE
                && HbmFluidContainerRules.accepts(kind, type)
                && getFirstFluidType(stack) == type
                && getFill(stack) > 0;
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
        if (tag == null) {
            return HbmFluids.NONE;
        }
        FluidType type = tag.contains(TAG_FLUID)
                ? HbmFluidJsonUtil.readFluidReference(tag.getString(TAG_FLUID))
                : tag.contains(LEGACY_TAG_TYPE)
                        ? HbmFluids.fromId(tag.getInt(LEGACY_TAG_TYPE))
                        : HbmFluids.NONE;
        return type == null ? HbmFluids.NONE : type;
    }

    @Override
    public int getFill(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        String key = tag.contains(TAG_AMOUNT) ? TAG_AMOUNT : LEGACY_TAG_AMOUNT;
        return Math.max(0, Math.min(capacity, tag.getInt(key)));
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
            if (type == HbmFluids.NONE || !HbmFluidContainerRules.canRepresentContainedFluid(kind, type)) {
                continue;
            }
            output.accept(createFilledStack(type));
        }
    }

    public int getTintColor(ItemStack stack) {
        return getTintColor(stack, 1);
    }

    public int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex <= 0) {
            return 0xFFFFFF;
        }
        FluidType type = getFirstFluidType(stack);
        if (type == HbmFluids.NONE) {
            return 0xFFFFFF;
        }
        ContainerFluidTrait container = type.getTrait(ContainerFluidTrait.class);
        if (kind == HbmFluidContainerRules.ContainerKind.CANISTER) {
            return tintIndex == 1
                    ? container == null ? type.getColor() : container.getCanisterColor()
                    : 0xFFFFFF;
        }
        if (kind == HbmFluidContainerRules.ContainerKind.GAS_TANK) {
            if (tintIndex == 1) {
                return container == null ? type.getColor() : container.getGasTankBottleColorOr(type.getColor());
            }
            return tintIndex == 2
                    ? container == null ? 0xFFFFFF : container.getGasTankLabelColorOr(0xFFFFFF)
                    : 0xFFFFFF;
        }
        return tintIndex == 1 ? type.getColor() : 0xFFFFFF;
    }

    public int getCapacity() {
        return capacity;
    }

    public HbmFluidContainerRules.ContainerKind getContainerKind() {
        return kind;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        if (kind == HbmFluidContainerRules.ContainerKind.FLUID_PACK && getFill(stack) > 0) {
            return true;
        }
        return !HbmFluidContainerRegistry.getCraftingRemainder(stack).isEmpty();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (kind == HbmFluidContainerRules.ContainerKind.FLUID_PACK && getFill(stack) > 0) {
            return HbmFluidContainerRegistry.emptyContainer(kind);
        }
        return HbmFluidContainerRegistry.getCraftingRemainder(stack);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (this instanceof HbmInfiniteFluidItem || kind == HbmFluidContainerRules.ContainerKind.FLUID_PACK) {
            return super.initCapabilities(stack, nbt);
        }
        return new HbmFluidContainerItemCapabilityProvider(stack, this);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FluidType type = tooltipFluidType(stack);
        if (type == HbmFluids.NONE) {
            return;
        }

        int fill = getFill(stack);
        if (fill > 0) {
            tooltip.add(Component.literal(fill + " / " + capacity + " mB"));
        }
        int pressure = getPressure(stack);
        if (pressure > 0) {
            tooltip.add(Component.literal(pressure + " PU").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Pressurized, use compressor!").withStyle(ChatFormatting.DARK_RED));
        }
        type.appendInfo(tooltip, HbmFluidGuiHelper.showHiddenFluidInfo());
    }

    public int getPressure(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        String key = tag.contains(TAG_PRESSURE) ? TAG_PRESSURE : LEGACY_TAG_PRESSURE;
        return HbmFluidTank.clampPressure(tag.getInt(key));
    }

    public void setFluid(ItemStack stack, FluidType type, int amount, int pressure) {
        if (type == null || type == HbmFluids.NONE || amount <= 0) {
            clearFluid(stack);
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_FLUID, type.getName());
        tag.putInt(TAG_AMOUNT, Math.min(amount, capacity));
        tag.putInt(TAG_PRESSURE, HbmFluidTank.clampPressure(pressure));
        tag.remove(LEGACY_TAG_TYPE);
        tag.remove(LEGACY_TAG_AMOUNT);
        tag.remove(LEGACY_TAG_PRESSURE);
    }

    public void clearFluid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(TAG_FLUID);
        tag.remove(TAG_AMOUNT);
        tag.remove(TAG_PRESSURE);
        tag.remove(LEGACY_TAG_TYPE);
        tag.remove(LEGACY_TAG_AMOUNT);
        tag.remove(LEGACY_TAG_PRESSURE);
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

    private FluidType tooltipFluidType(ItemStack stack) {
        if (this instanceof HbmInfiniteFluidItem infinite) {
            FluidType type = infinite.getType();
            return type == null ? HbmFluids.NONE : type;
        }
        return getFirstFluidType(stack);
    }
}
