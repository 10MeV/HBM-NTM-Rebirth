package com.hbm.ntm.item;

import com.hbm.ntm.api.block.AnalyzableBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AnalysisToolItem extends Item {
    public AnalysisToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        if (!(clickedState.getBlock() instanceof AnalyzableBlock analyzable)) {
            return InteractionResult.PASS;
        }

        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, clickedPos);
        BlockPos debugPos = core == null ? clickedPos : core.pos();
        if (!level.isClientSide) {
            Player player = context.getPlayer();
            List<Component> debug = analyzable.getDebugInfo(level, debugPos);
            if (player != null && debug != null) {
                for (Component line : debug) {
                    player.sendSystemMessage(line.copy().withStyle(ChatFormatting.YELLOW));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
