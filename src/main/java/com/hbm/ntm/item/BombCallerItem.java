package com.hbm.ntm.item;

import com.hbm.ntm.entity.logic.AirstrikeBomberEntity;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.RayTraceUtil;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Modern metadata split of the 1.7.10 Airstrike Designator (damage values 0 through 4). */
public class BombCallerItem extends Item {
    private final int type;

    public BombCallerItem(Properties properties, int type) {
        super(properties.stacksTo(1));
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BlockHitResult hit = RayTraceUtil.rayTrace(player, 500.0D, 1.0F);
            if (hit.getType() != HitResult.Type.BLOCK) {
                return InteractionResultHolder.fail(stack);
            }
            BlockPos pos = hit.getBlockPos();
            WorldUtil.loadAndSpawnEntityInWorld(
                    AirstrikeBomberEntity.create(level, pos.getX(), pos.getY(), pos.getZ(), type));
            player.displayClientMessage(Component.literal("Called in airstrike!"), false);
            LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Aim & click to call an airstrike!"));
        tooltip.add(Component.literal("Type: " + typeName()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return type >= AirstrikeBomberEntity.TYPE_ATOMIC;
    }

    private String typeName() {
        return switch (type) {
            case AirstrikeBomberEntity.TYPE_CARPET -> "Carpet bombing";
            case AirstrikeBomberEntity.TYPE_NAPALM -> "Napalm";
            case AirstrikeBomberEntity.TYPE_CHLORINE -> "Poison gas";
            case AirstrikeBomberEntity.TYPE_ORANGE -> "Agent orange";
            case AirstrikeBomberEntity.TYPE_ATOMIC -> "Atomic bomb";
            default -> throw new IllegalStateException("Unexpected airstrike type " + type);
        };
    }
}
