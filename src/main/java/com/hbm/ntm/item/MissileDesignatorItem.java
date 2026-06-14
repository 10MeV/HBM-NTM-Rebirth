package com.hbm.ntm.item;

import com.hbm.ntm.api.item.DesignatorItem;
import com.hbm.ntm.network.HbmItemActionReceiver;
import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.RayTraceUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MissileDesignatorItem extends Item implements DesignatorItem, HbmItemActionReceiver {
    public static final String TAG_X = "xCoord";
    public static final String TAG_Z = "zCoord";

    private final Mode mode;

    public MissileDesignatorItem(Properties properties, Mode mode) {
        super(properties);
        this.mode = mode;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (mode != Mode.BLOCK) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.isClientSide) {
            setTarget(context.getItemInHand(), pos.getX(), pos.getZ());
            playSetSound(level, context.getPlayer());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (mode == Mode.MANUAL) {
            if (level.isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.hbm.ntm.client.screen.ManualDesignatorScreen.open(hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (mode == Mode.RANGE && !level.isClientSide) {
            BlockHitResult hit = RayTraceUtil.rayTrace(player, 300.0D, 1.0F);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hit.getBlockPos();
                setTarget(stack, pos.getX(), pos.getZ());
                playSetSound(level, player);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean canReceiveItemAction(ServerPlayer player, InteractionHand hand, ItemStack stack,
                                        ResourceLocation actionType, CompoundTag data) {
        return mode == Mode.MANUAL && HbmNetworkActions.DESIGNATOR.equals(actionType);
    }

    @Override
    public void handleItemAction(ServerPlayer player, InteractionHand hand, ItemStack stack,
                                 ResourceLocation actionType, CompoundTag data) {
        int operator = data.getInt("operator");
        int value = data.getInt("value");
        int reference = data.getInt("reference");
        CompoundTag tag = stack.getOrCreateTag();
        if (operator == 2) {
            if (reference == 0) {
                tag.putInt(TAG_X, Math.round((float) player.getX()));
            } else {
                tag.putInt(TAG_Z, Math.round((float) player.getZ()));
            }
            playSetSound(player.level(), player);
            return;
        }
        int result = operator == 1 ? -value : value;
        if (reference == 0) {
            tag.putInt(TAG_X, tag.getInt(TAG_X) + result);
        } else {
            tag.putInt(TAG_Z, tag.getInt(TAG_Z) + result);
        }
        playSetSound(player.level(), player);
    }

    @Override
    public boolean isReady(Level level, ItemStack stack, BlockPos launchPos) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_X) && tag.contains(TAG_Z);
    }

    @Override
    public Vec3 getCoords(Level level, ItemStack stack, BlockPos launchPos) {
        CompoundTag tag = stack.getOrCreateTag();
        return new Vec3(tag.getInt(TAG_X), 0.0D, tag.getInt(TAG_Z));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_X) || !tag.contains(TAG_Z)) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.designator.no_target")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.designator.target"));
        tooltip.add(Component.literal("X: " + tag.getInt(TAG_X)));
        tooltip.add(Component.literal("Z: " + tag.getInt(TAG_Z)));
    }

    public Mode mode() {
        return mode;
    }

    public static void setTarget(ItemStack stack, int x, int z) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_X, x);
        tag.putInt(TAG_Z, z);
    }

    private static void playSetSound(Level level, @Nullable Player player) {
        if (player != null) {
            LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
        }
    }

    public enum Mode {
        BLOCK,
        RANGE,
        MANUAL
    }
}
