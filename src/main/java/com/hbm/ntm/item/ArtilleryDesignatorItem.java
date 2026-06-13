package com.hbm.ntm.item;

import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.turret.ArtilleryTargetReceiver;
import com.hbm.ntm.util.RayTraceUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArtilleryDesignatorItem extends Item {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";

    public ArtilleryDesignatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity core = MultiblockHelper.resolveCoreBlockEntity(level, context.getClickedPos());
        if (!(core instanceof ArtilleryTargetReceiver)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockPos corePos = core.getBlockPos();
            CompoundTag tag = context.getItemInHand().getOrCreateTag();
            tag.putInt(TAG_X, corePos.getX());
            tag.putInt(TAG_Y, corePos.getY());
            tag.putInt(TAG_Z, corePos.getZ());
            play(level, context.getPlayer(), ModSounds.TOOL_TECH_BLEEP.get());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_X) || !tag.contains(TAG_Y) || !tag.contains(TAG_Z)) {
            return InteractionResultHolder.pass(stack);
        }
        BlockHitResult hit = RayTraceUtil.rayTrace(player, 500.0D, 1.0F);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z)));
            if (blockEntity instanceof ArtilleryTargetReceiver receiver) {
                BlockPos pos = hit.getBlockPos();
                if (receiver.enqueueTarget(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) {
                    play(level, player, ModSounds.TOOL_TECH_BOOP.get());
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_X) || !tag.contains(TAG_Y) || !tag.contains(TAG_Z)) {
            tooltip.add(Component.literal("No turret linked!").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.literal("Linked to " + tag.getInt(TAG_X) + ", " + tag.getInt(TAG_Y)
                + ", " + tag.getInt(TAG_Z)).withStyle(ChatFormatting.YELLOW));
    }

    private static void play(Level level, @Nullable Player player, net.minecraft.sounds.SoundEvent sound) {
        if (player != null) {
            level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
