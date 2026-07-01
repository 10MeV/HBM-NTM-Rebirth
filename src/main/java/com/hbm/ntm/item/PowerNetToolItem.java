package com.hbm.ntm.item;

import com.hbm.ntm.energy.HbmEnergyConnectorBlock;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowerNetToolItem extends Item {
    private static final int RADIUS = 20;
    private static final int LINK_COLOR = 0xffff00;
    private static final float TEXT_SCALE = 0.5F;

    public PowerNetToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = resolveCorePos(level, context.getClickedPos());
        if (!isEnergyConductor(level, pos)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, pos);
        HbmEnergyNodespace.NetworkDebugSnapshot snapshot = HbmEnergyNodespace.getNetworkDebugSnapshot(level, pos);
        if (powerNet == null || !snapshot.networkPresent() || snapshot.network() == null) {
            if (player != null) {
                player.sendSystemMessage(Component.literal("Error: No network found!").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.SUCCESS;
        }

        String id = Integer.toHexString(System.identityHashCode(powerNet));
        HbmPowerNet.DebugSnapshot debug = snapshot.network();
        if (player != null) {
            player.sendSystemMessage(Component.literal("Start of diagnostic for network " + id).withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("Links: " + debug.links()).withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("Providers: " + debug.providers()).withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("Receivers: " + debug.receivers()).withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(Component.literal("End of diagnostic for network " + id).withStyle(ChatFormatting.GOLD));
        }

        if (level instanceof ServerLevel serverLevel) {
            for (HbmEnergyNode link : powerNet.getLinks()) {
                for (BlockPos linkPos : link.getPositions()) {
                    ParticleUtil.spawnDebugText(serverLevel,
                            linkPos.getX() + 0.5D,
                            linkPos.getY() + 1.5D,
                            linkPos.getZ() + 0.5D,
                            id,
                            LINK_COLOR,
                            TEXT_SCALE,
                            RADIUS);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static BlockPos resolveCorePos(Level level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        return core == null ? pos : core.pos();
    }

    private static boolean isEnergyConductor(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof HbmEnergyConnectorBlock;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click cable to analyze the power net.").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Links (cables, poles, etc.) are YELLOW").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Subscribers (any receiver) are BLUE").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Links with mismatching network info (BUGGED!) are RED").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Displays stats such as link and subscriber count").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Proxies are connection points for multiblock links (e.g. 4 for substations)").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Particles only spawn in a " + RADIUS + " block radius!").withStyle(ChatFormatting.RED));
    }
}
