package com.hbm.ntm.block;

import com.hbm.ntm.neutron.PileGraphiteBlockPlanner;
import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;
import com.hbm.ntm.neutron.PileGraphiteMetadata;
import com.hbm.ntm.neutron.PileGraphiteTogglePlanner;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("deprecation")
public class PileGraphiteRodBlock extends PileGraphiteDrilledBaseBlock {
    public PileGraphiteRodBlock(Properties properties) {
        super(properties, PileGraphiteInsertionPlanner.GraphiteBlockKind.ROD);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown() || !player.getItemInHand(hand).isEmpty()) {
            return super.use(state, level, pos, player, hand, hit);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        PileGraphiteTogglePlanner.TogglePlan plan = PileGraphiteTogglePlanner.manualRodToggle(
                pos,
                legacyMeta(state),
                hit.getDirection(),
                probe -> chainState(level, probe, PileGraphiteInsertionPlanner.GraphiteBlockKind.ROD));
        if (!plan.hasMutations()) {
            return InteractionResult.PASS;
        }
        for (PileGraphiteTogglePlanner.ToggleMutation mutation : plan.mutations()) {
            BlockState target = level.getBlockState(mutation.pos());
            if (target.getBlock() instanceof PileGraphiteRodBlock) {
                level.setBlock(
                        mutation.pos(),
                        withLegacyMeta(target, mutation.newMeta()),
                        Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
            }
        }
        LegacySoundPlayer.playSoundEffect(
                level,
                pos,
                PileGraphiteBlockPlanner.LEGACY_RANDOM_CLICK_SOUND,
                SoundSource.BLOCKS,
                0.3F,
                PileGraphiteMetadata.isActive(legacyMeta(state)) ? 0.65F : 0.75F);
        return InteractionResult.CONSUME;
    }
}
