package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.network.HbmItemControlReceiver;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

public class FluidIdentifierItem extends Item implements IFluidIdentifierItem, HbmItemControlReceiver {
    private static final String TAG_PRIMARY = "fluid1";
    private static final String TAG_SECONDARY = "fluid2";
    private static final String TAG_PRIMARY_NAME = "fluid1_name";
    private static final String TAG_SECONDARY_NAME = "fluid2_name";

    public FluidIdentifierItem(Properties properties) {
        super(properties);
    }

    @Override
    public FluidType getIdentifiedFluid(@Nullable Level level, BlockPos pos, ItemStack stack) {
        return getType(stack, true);
    }

    @Override
    public boolean setIdentifiedFluid(ItemStack stack, FluidType type, boolean primary) {
        setType(stack, type, primary);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.hbm.ntm.client.FluidIdentifierScreenBridge.open(hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                FluidType primary = getType(stack, true);
                FluidType secondary = getType(stack, false);
                setType(stack, secondary, true);
                setType(stack, primary, false);
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.25F, 1.25F);
                if (player instanceof ServerPlayer serverPlayer) {
                    ModMessages.informPlayer(serverPlayer, secondary.getDisplayName(), 7, 3000);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void handleItemControl(ServerPlayer player, ItemStack stack, CompoundTag tag) {
        if (tag.contains("primary")) {
            setType(stack, HbmFluids.fromId(tag.getInt("primary")), true);
        }
        if (tag.contains("secondary")) {
            setType(stack, HbmFluids.fromId(tag.getInt("secondary")), false);
        }
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.fluid_identifier_multi.info"));
        tooltip.add(Component.literal("   ").append(getType(stack, true).getDisplayName()));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.fluid_identifier_multi.info2"));
        tooltip.add(Component.literal("   ").append(getType(stack, false).getDisplayName()));
    }

    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (FluidType type : HbmFluids.niceOrder()) {
            if (type == HbmFluids.NONE || type.hasNoId()) {
                continue;
            }
            ItemStack stack = new ItemStack(this);
            setType(stack, type, true);
            output.accept(stack);
        }
    }

    public int getTintColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFF;
        }
        int color = getType(stack, true).getColor();
        return color < 0 ? 0xFFFFFF : color;
    }

    public static void setType(ItemStack stack, FluidType type, boolean primary) {
        CompoundTag tag = stack.getOrCreateTag();
        FluidType next = type == null ? HbmFluids.NONE : type;
        tag.putInt(primary ? TAG_PRIMARY : TAG_SECONDARY, next.getId());
        tag.putString(primary ? TAG_PRIMARY_NAME : TAG_SECONDARY_NAME, next.getName());
    }

    public static FluidType getType(ItemStack stack, boolean primary) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return HbmFluids.NONE;
        }
        String key = primary ? TAG_PRIMARY : TAG_SECONDARY;
        if (tag.contains(key, Tag.TAG_INT)) {
            return HbmFluids.fromId(tag.getInt(key));
        }
        if (tag.contains(key + "_id", Tag.TAG_INT)) {
            return HbmFluids.fromId(tag.getInt(key + "_id"));
        }
        FluidType type = tag.contains(key, Tag.TAG_STRING)
                ? HbmFluidJsonUtil.readFluidReference(tag.getString(key))
                : HbmFluids.NONE;
        if (type == HbmFluids.NONE) {
            type = HbmFluidJsonUtil.readFluidReference(tag.getString(primary ? TAG_PRIMARY_NAME : TAG_SECONDARY_NAME));
        }
        return type == null ? HbmFluids.NONE : type;
    }

}
