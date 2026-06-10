package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.neutron.PileGraphiteBlockEntityPlanner;
import com.hbm.ntm.neutron.PileGraphiteInteractionPlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PileGraphiteBlock extends Block implements Toolable {
    public PileGraphiteBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.HAND_DRILL) {
            return false;
        }
        PileGraphiteBlockEntityPlanner.ToolUseExecutionPlan plan =
                PileGraphiteBlockEntityPlanner.planToolUse(
                        pos,
                        null,
                        0,
                        side,
                        PileGraphiteBlockEntityPlanner.ToolAction.HAND_DRILL,
                        player.isShiftKeyDown(),
                        null,
                        null);
        if (plan.interaction() == null || !plan.interaction().accepted()) {
            return false;
        }
        if (!level.isClientSide) {
            PileGraphiteDrilledBaseBlock.executeInteraction(level, player, plan.interaction());
        }
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool == ToolType.HAND_DRILL && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
