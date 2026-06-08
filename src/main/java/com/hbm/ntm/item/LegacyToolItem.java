package com.hbm.ntm.item;

import com.hbm.ntm.api.block.Toolable;
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
        if (!(state.getBlock() instanceof Toolable toolable)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Direction side = context.getClickedFace();
        Vec3 hit = context.getClickLocation();
        boolean used = toolable.onToolUse(level, player, pos, side, hit, toolType);
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
}
