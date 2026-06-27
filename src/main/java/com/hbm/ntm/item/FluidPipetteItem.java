package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFillableItemCapabilityProvider;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

public class FluidPipetteItem extends Item implements IFillableItem {
    private static final String TAG_TYPE = "type";
    private static final String TAG_FILL = "fill";
    private static final String TAG_CAPACITY = "capacity";

    private final Kind kind;

    public FluidPipetteItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    public int getMaxFill() {
        return kind == Kind.LABORATORY ? 50 : 1_000;
    }

    public int getCapacity(ItemStack stack) {
        CompoundTag tag = initializedTag(stack);
        int fallback = getMaxFill();
        if (!tag.contains(TAG_CAPACITY)) {
            tag.putShort(TAG_CAPACITY, (short) fallback);
            return fallback;
        }
        int capacity = Short.toUnsignedInt(tag.getShort(TAG_CAPACITY));
        if (kind == Kind.LABORATORY) {
            return Math.max(1, Math.min(50, capacity));
        }
        return Math.max(50, Math.min(1_000, capacity));
    }

    public boolean willFizzle(FluidType type) {
        return kind == Kind.NORMAL
                && type != null
                && type != HbmFluids.PEROXIDE
                && type.hasTrait(CorrosiveFluidTrait.class);
    }

    public int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1 || getFill(stack) <= 0) {
            return 0xFFFFFF;
        }
        FluidType type = getType(stack);
        return type == null || type == HbmFluids.NONE ? 0xFFFFFF : type.getColor();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (getFill(stack) == 0) {
                int step = kind == Kind.LABORATORY ? 1 : 50;
                int min = kind == Kind.LABORATORY ? 1 : 50;
                int max = getMaxFill();
                int next = player.isShiftKeyDown()
                        ? Math.max(getCapacity(stack) - step, min)
                        : Math.min(getCapacity(stack) + step, max);
                initializedTag(stack).putShort(TAG_CAPACITY, (short) next);
                player.displayClientMessage(Component.literal(next + "/" + max + "mB"), false);
            } else {
                player.displayClientMessage(Component.translatable("desc.item.pipette.noEmpty"), false);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return type != null
                && type != HbmFluids.NONE
                && (type == getType(stack) || getFill(stack) == 0)
                && !type.isAntimatter();
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        if (amount <= 0 || !acceptsFluid(type, stack)) {
            return Math.max(0, amount);
        }
        if (getFill(stack) == 0) {
            setFill(stack, type, 0);
        }
        int moved = Math.min(amount, getCapacity(stack) - getFill(stack));
        if (moved > 0) {
            setFill(stack, type, getFill(stack) + moved);
            if (willFizzle(type)) {
                stack.setCount(0);
            }
        }
        return amount - moved;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return type != null && type != HbmFluids.NONE && type == getType(stack) && getFill(stack) > 0;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        if (amount <= 0 || !providesFluid(type, stack)) {
            return 0;
        }
        int moved = Math.min(amount, getFill(stack));
        int remaining = getFill(stack) - moved;
        setFill(stack, remaining <= 0 ? HbmFluids.NONE : type, remaining);
        return moved;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return getType(stack);
    }

    @Override
    public int getFill(ItemStack stack) {
        CompoundTag tag = initializedTag(stack);
        return Math.max(0, Math.min(getCapacity(stack), Short.toUnsignedInt(tag.getShort(TAG_FILL))));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HbmFillableItemCapabilityProvider(stack, this, getMaxFill());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (kind == Kind.LABORATORY) {
            tooltip.add(Component.translatable("desc.item.pipette.corrosive"));
            tooltip.add(Component.translatable("desc.item.pipette.laboratory"));
        } else if (kind == Kind.BORON) {
            tooltip.add(Component.translatable("desc.item.pipette.corrosive"));
        } else {
            tooltip.add(Component.translatable("desc.item.pipette.noCorrosive").withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.literal("Fluid: ").append(getType(stack).getDisplayName()));
        tooltip.add(Component.literal("Amount: " + getFill(stack) + "/" + getCapacity(stack) + "mB ("
                + getMaxFill() + "mB)"));
    }

    private FluidType getType(ItemStack stack) {
        CompoundTag tag = initializedTag(stack);
        return HbmFluids.fromId(Short.toUnsignedInt(tag.getShort(TAG_TYPE)));
    }

    private void setFill(ItemStack stack, FluidType type, int fill) {
        CompoundTag tag = initializedTag(stack);
        if (type == null) {
            type = HbmFluids.NONE;
        }
        tag.putShort(TAG_TYPE, (short) type.getId());
        tag.putShort(TAG_FILL, (short) Math.max(0, Math.min(getCapacity(stack), fill)));
    }

    private CompoundTag initializedTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_TYPE)) {
            tag.putShort(TAG_TYPE, (short) HbmFluids.NONE.getId());
        }
        if (!tag.contains(TAG_FILL)) {
            tag.putShort(TAG_FILL, (short) 0);
        }
        if (!tag.contains(TAG_CAPACITY)) {
            tag.putShort(TAG_CAPACITY, (short) getMaxFill());
        }
        return tag;
    }

    public enum Kind {
        NORMAL,
        BORON,
        LABORATORY
    }
}
