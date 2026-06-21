package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.blockentity.SolarBoilerBlockEntity;
import com.hbm.ntm.blockentity.SolarMirrorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.util.HbmTextUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MirrorToolItem extends Item {
    private static final String TAG_POS_X = "posX";
    private static final String TAG_POS_Y = "posY";
    private static final String TAG_POS_Z = "posZ";
    private static final double MAX_REACH = 100.0D;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public MirrorToolItem(Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier", 2.0D, AttributeModifier.Operation.ADDITION));
        defaultModifiers = builder.build();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        BlockEntity targetEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, clickedPos);
        if (targetEntity instanceof SolarBoilerBlockEntity) {
            if (!level.isClientSide) {
                BlockPos target = targetEntity.getBlockPos().above();
                CompoundTag tag = stack.getOrCreateTag();
                tag.putInt(TAG_POS_X, target.getX());
                tag.putInt(TAG_POS_Y, target.getY());
                tag.putInt(TAG_POS_Z, target.getZ());
                sendStatus(player, "item.mirror_tool.linked", ChatFormatting.YELLOW);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockEntity clickedEntity = level.getBlockEntity(clickedPos);
        if (clickedEntity instanceof SolarMirrorBlockEntity mirror && hasTarget(stack)) {
            if (!level.isClientSide) {
                BlockPos target = storedTarget(stack);
                boolean withinReach = Vec3.atLowerCornerOf(clickedPos.subtract(target)).length() <= MAX_REACH;
                int dx = clickedPos.getX() - target.getX();
                int dy = clickedPos.getY() - target.getY();
                int dz = clickedPos.getZ() - target.getZ();
                boolean withinAngle = dx * dx + dz * dz <= dy * dy;
                if (!withinReach) {
                    sendStatus(player, "item.mirror_tool.reach", ChatFormatting.RED);
                } else if (!withinAngle) {
                    sendStatus(player, "item.mirror_tool.angle", ChatFormatting.RED);
                } else {
                    mirror.setTarget(target);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (String line : HbmTextUtil.resolveKeyArray("item.hbm_ntm_rebirth.mirror_tool.desc")) {
            tooltip.add(Component.literal(line).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    private static boolean hasTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(TAG_POS_X, Tag.TAG_INT)
                && tag.contains(TAG_POS_Y, Tag.TAG_INT)
                && tag.contains(TAG_POS_Z, Tag.TAG_INT);
    }

    private static BlockPos storedTarget(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return new BlockPos(tag.getInt(TAG_POS_X), tag.getInt(TAG_POS_Y), tag.getInt(TAG_POS_Z));
    }

    private static void sendStatus(Player player, String key, ChatFormatting color) {
        player.sendSystemMessage(Component.translatable(key).withStyle(color));
    }
}
