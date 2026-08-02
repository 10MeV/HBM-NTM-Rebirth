package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.Crosshair;
import com.hbm.ntm.api.item.IHoldableWeapon;
import com.hbm.ntm.block.RemoteDetonatableBlock;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;

/** Source-backed port of 1.7.10 {@code ItemLaserDetonator}. */
public final class LaserDetonatorItem extends Item implements IHoldableWeapon {
    private static final double LEGACY_RANGE = 500.0D;
    private static final double LEGACY_VISUAL_RANGE = 15.0D;

    public LaserDetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public Crosshair getCrosshair() {
        return Crosshair.L_ARROWS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Aim & click to detonate!"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = player.pick(LEGACY_RANGE, 1.0F, false);
        if (level.isClientSide()) {
            spawnLegacyRedstonePath(level, player, hit);
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        BlockPos pos = hit instanceof BlockHitResult blockHit ? blockHit.getBlockPos() : null;
        if (pos != null && level.getBlockState(pos).getBlock() instanceof RemoteDetonatableBlock detonatable) {
            RemoteDetonatableBlock.BombReturnCode code = detonatable.detonateFromRemote(level, pos);
            if (HbmCommonConfig.extendedLoggingEnabled()) {
                HbmNtm.LOGGER.info("[DET] Tried to detonate block at {} / {} / {} by {}.", pos.getX(), pos.getY(),
                        pos.getZ(), player.getGameProfile().getName());
            }
            LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable(code.translationKey())
                    .withStyle(code.wasSuccessful() ? ChatFormatting.YELLOW : ChatFormatting.RED), false);
        } else {
            LegacySoundPlayer.playLegacyTechBoop(player, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable(RemoteDetonatableBlock.BombReturnCode.ERROR_NO_BOMB.translationKey())
                    .withStyle(ChatFormatting.RED), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static void spawnLegacyRedstonePath(Level level, Player player, HitResult hit) {
        Vec3 target = hit.getLocation();
        Vec3 origin = player.position();
        Vec3 delta = target.subtract(origin);
        double length = Math.min(delta.length(), LEGACY_VISUAL_RANGE);
        if (length <= 0.0D) return;
        Vec3 direction = delta.normalize();
        for (int index = 0; index < (int) length; index++) {
            double distance = level.random.nextDouble() * length + 3.0D;
            Vec3 point = origin.add(direction.scale(distance));
            level.addParticle(DustParticleOptions.REDSTONE, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            try {
                Class<?> bridge = Class.forName("com.hbm.ntm.client.renderer.LaserDetonatorItemRendererBridge");
                bridge.getMethod("accept", Consumer.class).invoke(null, consumer);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
                throw new IllegalStateException("Missing laser detonator client renderer bridge", exception);
            } catch (InvocationTargetException exception) {
                throw new IllegalStateException("Laser detonator client renderer bridge failed", exception.getCause());
            }
        });
    }
}
