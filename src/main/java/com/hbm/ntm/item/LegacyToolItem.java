package com.hbm.ntm.item;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LegacyToolItem extends Item {
    private final Toolable.ToolType toolType;

    public LegacyToolItem(Properties properties, Toolable.ToolType toolType) {
        super(properties);
        this.toolType = toolType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ToolTarget target = resolveToolTarget(level, pos, state);
        if (target == null) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Direction side = context.getClickedFace();
        Vec3 hit = context.getClickLocation();
        boolean used = target.toolable().onToolUse(level, player, target.pos(), side, hit, toolType);
        if (!used) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && player != null && !player.getAbilities().instabuild) {
            ItemStack stack = context.getItemInHand();
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public Toolable.ToolType getToolType() {
        return toolType;
    }

    private static ToolTarget resolveToolTarget(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof Toolable toolable) {
            return new ToolTarget(pos, toolable);
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core != null && core.state().getBlock() instanceof Toolable toolable) {
            return new ToolTarget(core.pos(), toolable);
        }
        return null;
    }

    private record ToolTarget(BlockPos pos, Toolable toolable) {
    }
}
