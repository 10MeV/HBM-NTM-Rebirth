package com.hbm.ntm.item;

import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.util.AchievementHandler;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class LegacyShimmerWeaponItem extends HbmAbilitySwordItem {
    private static final float LEGACY_ATTACK_DAMAGE = 30.0F;
    private static final double LEGACY_MOVEMENT_MODIFIER = -0.2D;
    private static final float MAX_BREAKABLE_RESISTANCE = 6000.0F;

    private final Kind kind;

    public LegacyShimmerWeaponItem(Kind kind, Properties properties) {
        super(HbmToolTiers.SHIMMER_SLEDGE, LEGACY_ATTACK_DAMAGE, LEGACY_MOVEMENT_MODIFIER, properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide) {
            // Legacy WeaponSpecial granted the victim's Fiend achievement when
            // a shimmer weapon hit a player wearing its matching set.
            if (target instanceof ServerPlayer player) {
                if (ArmorUtil.checkForFiend(player)) {
                    AchievementHandler.award(player, AchievementHandler.FIEND);
                } else if (ArmorUtil.checkForFiend2(player)) {
                    AchievementHandler.award(player, AchievementHandler.FIEND2);
                }
            }
            if (kind == Kind.SLEDGE) {
                Vec3 push = attacker.getLookAngle().scale(5.0D);
                target.setDeltaMovement(target.getDeltaMovement().add(push));
                target.hurtMarked = true;
                play(level, target.getX(), target.getY(), target.getZ(), ModSounds.WEAPON_BANG.get(),
                        SoundSource.PLAYERS);
            } else {
                target.setHealth(target.getHealth() * 0.5F);
                play(level, target.getX(), target.getY(), target.getZ(), ModSounds.WEAPON_SLICE.get(),
                        SoundSource.PLAYERS);
            }
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        return kind == Kind.SLEDGE ? useSledgeOn(level, context.getClickedPos(), player)
                : useAxeOn(level, context.getClickedPos());
    }

    private InteractionResult useSledgeOn(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !canBreak(level, pos, state)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            RubbleEntity rubble = new RubbleEntity(level);
            rubble.setOwner(player);
            rubble.setBlockState(state);
            rubble.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            rubble.setDeltaMovement(player.getLookAngle().scale(5.0D));
            play(level, pos, ModSounds.WEAPON_BANG.get());
            level.addFreshEntity(rubble);
            level.destroyBlock(pos, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult useAxeOn(Level level, BlockPos clickedPos) {
        if (!level.isClientSide) {
            play(level, clickedPos, ModSounds.WEAPON_KAPENG.get());
            destroyIfBreakable(level, clickedPos);
            destroyIfBreakable(level, clickedPos.above());
            destroyIfBreakable(level, clickedPos.below());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void destroyIfBreakable(Level level, BlockPos pos) {
        if (!level.isInWorldBounds(pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && canBreak(level, pos, state)) {
            level.destroyBlock(pos, false);
        }
    }

    private static boolean canBreak(Level level, BlockPos pos, BlockState state) {
        return state.getExplosionResistance(level, pos, null) < MAX_BREAKABLE_RESISTANCE;
    }

    private static void play(Level level, BlockPos pos, SoundEvent sound) {
        play(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound, SoundSource.BLOCKS);
    }

    private static void play(Level level, double x, double y, double z, SoundEvent sound, SoundSource source) {
        level.playSound(null, x, y, z, sound, source, 3.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(getDescriptionId() + ".desc"));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptShimmerWeapon", consumer);
    }

    public enum Kind {
        SLEDGE,
        AXE
    }
}
