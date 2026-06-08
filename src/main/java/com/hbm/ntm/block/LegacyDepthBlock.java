package com.hbm.ntm.block;

import com.hbm.ntm.api.item.DepthRockTool;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class LegacyDepthBlock extends RadiatingHazardBlock {
    public LegacyDepthBlock(String legacyName, Properties properties) {
        super(legacyName, properties);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level instanceof Level realLevel && player.getMainHandItem().getItem() instanceof DepthRockTool tool
                && tool.canBreakRock(realLevel, player, player.getMainHandItem(), state, pos)) {
            return 1.0F / 50.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("trait.tile.depth").withStyle(net.minecraft.ChatFormatting.YELLOW));
    }
}
