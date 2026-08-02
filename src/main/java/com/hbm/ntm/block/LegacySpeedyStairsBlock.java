package com.hbm.ntm.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Source-shaped BlockSpeedyStairs for the legacy asphalt stair. */
public class LegacySpeedyStairsBlock extends StairBlock {
    private static final double SPEED = 1.5D;
    public LegacySpeedyStairsBlock(BlockState baseState, Properties properties) { super(baseState, properties); }
    @Override public void stepOn(Level level, net.minecraft.core.BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide && entity instanceof Player player) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.x != 0.0D || motion.z != 0.0D) player.setDeltaMovement(motion.x * SPEED, motion.y, motion.z * SPEED);
        }
    }
    @Override public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Increases speed by 50%").withStyle(ChatFormatting.BLUE));
    }
}
