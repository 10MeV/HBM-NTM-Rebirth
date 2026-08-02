package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RemoteDetonatableBlock;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.config.WeaponConfig;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Source-backed {@code ItemDrop} behavior for the two legacy dead-man items. */
public final class DroppedDetonatorItem extends Item {
    public enum Type {
        DEADMAN_DETONATOR,
        DEADMAN_EXPLOSIVE
    }

    private final Type type;

    public DroppedDetonatorItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (type == Type.DEADMAN_DETONATOR) {
            if (!entity.level().isClientSide) {
                updateDeadmanDetonator(stack, entity);
                entity.discard();
                return true;
            }
            return false;
        }

        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel level
                && WeaponConfig.droppedDeadManExplosivesEnabled()) {
            WeaponExplosionUtil.explodeStandard(level, entity.getX(), entity.getY(), entity.getZ(), 15.0F,
                    entity, true, false);
            if (HbmCommonConfig.extendedLoggingEnabled()) {
                HbmNtm.LOGGER.info("[DET] Detonated dead man's explosive at {} / {} / {}!", (int) entity.getX(),
                        (int) entity.getY(), (int) entity.getZ());
            }
        }
        entity.discard();
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (type != Type.DEADMAN_DETONATOR || context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());

        Player player = context.getPlayer();
        LegacySoundPlayer.playLegacyTechBoop(player, 2.0F, 1.0F);
        if (context.getLevel().isClientSide()) {
            // ItemDrop#onItemUse emitted this unprefixed literal on the old client only.
            player.displayClientMessage(Component.literal("Position set!"), false);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (type == Type.DEADMAN_DETONATOR) {
            tooltip.add(Component.literal("Shift right-click to set position,"));
            tooltip.add(Component.literal("drop to detonate!"));
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                tooltip.add(Component.literal("No position set!"));
            } else {
                tooltip.add(Component.literal("Set pos to " + tag.getInt("x") + ", " + tag.getInt("y") + ", "
                        + tag.getInt("z")));
            }
        } else {
            tooltip.add(Component.literal("Explodes when dropped!"));
        }
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.trait.drop").withStyle(ChatFormatting.RED));
    }

    /** Replays the lowest-priority legacy player-death inventory pass. */
    public static void triggerOnPlayerDeath(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof DroppedDetonatorItem detonator)
                    || detonator.type != Type.DEADMAN_DETONATOR || !stack.hasTag()) {
                continue;
            }

            detonateLinkedTarget(level, stack.getTag(), player);
            // Legacy clears every tagged dead-man detonator even if the recorded target is no longer a bomb.
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
    }

    private static void updateDeadmanDetonator(ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            detonateLinkedTarget(level, tag, null);
        }
        WeaponExplosionUtil.explodeStandard(level, entity.getX(), entity.getY(), entity.getZ(), 0.0F,
                entity, true, false);
    }

    private static void detonateLinkedTarget(ServerLevel level, CompoundTag tag, @Nullable Player player) {
        BlockPos target = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        if (!(level.getBlockState(target).getBlock() instanceof RemoteDetonatableBlock detonatable)) {
            return;
        }

        detonatable.detonateFromRemote(level, target);
        if (!HbmCommonConfig.extendedLoggingEnabled()) {
            return;
        }
        if (player == null) {
            HbmNtm.LOGGER.info("[DET] Tried to detonate block at {} / {} / {} by dead man's switch!", target.getX(),
                    target.getY(), target.getZ());
        } else {
            HbmNtm.LOGGER.info("[DET] Tried to detonate block at {} / {} / {} by dead man's switch from {}!",
                    target.getX(), target.getY(), target.getZ(), player.getDisplayName().getString());
        }
    }
}
