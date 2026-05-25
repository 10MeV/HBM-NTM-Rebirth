package com.hbm.ntm.item;

import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlockItem extends BlockItem {
    private static final String TAG_TYPE = "hbm_fluid_type";
    private static final String TAG_TYPE_ID = "hbm_fluid_type_id";

    public FluidPipeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidPipeBlockEntity pipe) {
            pipe.setFluidType(getFluidType(context.getItemInHand()));
        }
        return placed;
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidType type = getFluidType(stack);
        if (type == HbmFluids.NONE) {
            return super.getName(stack);
        }
        return Component.translatable(getDescriptionId(stack)).append(" ").append(type.getDisplayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FluidType type = getFluidType(stack);
        if (type != HbmFluids.NONE) {
            tooltip.add(type.getDisplayName());
            type.appendInfo(tooltip, flag.isAdvanced());
        }
    }

    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (FluidType type : HbmFluids.niceOrder()) {
            if (type == HbmFluids.NONE || type.hasNoId()) {
                continue;
            }
            output.accept(createStack(this, type));
        }
    }

    public int getTintColor(ItemStack stack, int tintIndex) {
        return tintIndex == 1 ? getFluidType(stack).getColor() : 0xFFFFFF;
    }

    public static FluidType getFluidType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return HbmFluids.NONE;
        }
        FluidType type = HbmFluids.fromName(tag.getString(TAG_TYPE));
        if (type == HbmFluids.NONE && tag.contains(TAG_TYPE_ID)) {
            type = HbmFluids.fromId(tag.getInt(TAG_TYPE_ID));
        }
        return type == null ? HbmFluids.NONE : type;
    }

    public static ItemStack createStack(FluidPipeBlockItem item, FluidType type) {
        ItemStack stack = new ItemStack(item);
        FluidType next = type == null ? HbmFluids.NONE : type;
        stack.getOrCreateTag().putString(TAG_TYPE, next.getName());
        stack.getOrCreateTag().putInt(TAG_TYPE_ID, next.getId());
        return stack;
    }

}
