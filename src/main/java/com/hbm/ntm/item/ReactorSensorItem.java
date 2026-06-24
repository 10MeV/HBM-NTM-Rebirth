package com.hbm.ntm.item;

import com.hbm.ntm.blockentity.ResearchReactorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class ReactorSensorItem extends Item {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";

    public ReactorSensorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(level, context.getClickedPos());
        if (!(blockEntity instanceof ResearchReactorBlockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_X, context.getClickedPos().getX());
        tag.putInt(TAG_Y, context.getClickedPos().getY());
        tag.putInt(TAG_Z, context.getClickedPos().getZ());

        Player player = context.getPlayer();
        if (!level.isClientSide && player != null) {
            player.displayClientMessage(Component.literal("[")
                    .withStyle(ChatFormatting.DARK_AQUA)
                    .append(stack.getHoverName().copy().withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("] ").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("Position set!").withStyle(ChatFormatting.GREEN)), false);
        }
        if (player != null) {
            LegacySoundPlayer.playLegacyTechBoop(player, 1.0F, 1.0F);
            player.swing(context.getHand(), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            tooltip.add(Component.literal("No reactor selected!"));
            return;
        }
        tooltip.add(Component.literal("x: " + tag.getInt(TAG_X)));
        tooltip.add(Component.literal("y: " + tag.getInt(TAG_Y)));
        tooltip.add(Component.literal("z: " + tag.getInt(TAG_Z)));
    }
}
