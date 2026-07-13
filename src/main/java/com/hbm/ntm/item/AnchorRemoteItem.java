package com.hbm.ntm.item;

import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** The 1.7.10 Recall Device, retaining its local-coordinate-only anchor contract. */
public class AnchorRemoteItem extends HbmBatteryItem {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final long TELEPORT_COST = 10_000L;

    public AnchorRemoteItem(Properties properties) {
        super(properties, 1_000_000L, 10_000L, 0L);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(ModBlocks.TELEANCHOR.get())) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            CompoundTag tag = context.getItemInHand().getOrCreateTag();
            tag.putInt(TAG_X, pos.getX());
            tag.putInt(TAG_Y, pos.getY());
            tag.putInt(TAG_Z, pos.getZ());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || getCharge(stack) < TELEPORT_COST) {
            playFailureSound(serverPlayer);
            return InteractionResultHolder.success(stack);
        }

        BlockPos target = new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
        serverLevel.getChunk(target);
        if (!serverLevel.getBlockState(target).is(ModBlocks.TELEANCHOR.get())) {
            playFailureSound(serverPlayer);
            return InteractionResultHolder.success(stack);
        }

        serverPlayer.stopRiding();
        serverLevel.explode(serverPlayer, target.getX() + 0.5D,
                target.getY() + 1.0D + serverPlayer.getBbHeight() / 2.0D, target.getZ() + 0.5D,
                2.0F, false, Level.ExplosionInteraction.NONE);
        LegacySoundPlayer.playSoundEffect(serverLevel, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                "VANILLA_TELEPORT", 1.0F, 1.0F);
        serverPlayer.teleportTo(target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D);
        serverPlayer.fallDistance = 0.0F;

        for (int i = 0; i < 32; i++) {
            ParticleUtil.spawnPortalParticle(serverLevel, serverPlayer.getX(),
                    serverPlayer.getY() + serverPlayer.getRandom().nextDouble() * 2.0D, serverPlayer.getZ(),
                    serverPlayer.getRandom().nextGaussian(), 0.0D, serverPlayer.getRandom().nextGaussian());
        }
        dischargeBattery(stack, TELEPORT_COST);
        return InteractionResultHolder.success(stack);
    }

    private static void playFailureSound(ServerPlayer player) {
        LegacySoundPlayer.playSoundAtEntity(player, "VANILLA_ORB", 0.25F, 0.75F);
    }
}
