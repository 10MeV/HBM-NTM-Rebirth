package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

/** 1.7.10 BlockModDoor: manually operable metal doors with the old HBM sounds. */
public class LegacyModDoorBlock extends DoorBlock {
    public LegacyModDoorBlock(BlockBehaviour.Properties properties) {
        super(properties, BlockSetType.IRON);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide) {
            setOpen(level, pos, state, !state.getValue(OPEN), player, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) {
            return;
        }

        BlockPos otherPos = otherHalf(pos, state);
        boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(otherPos);
        if (powered != state.getValue(POWERED)) {
            setOpen(level, pos, state, powered, null, true);
        }
    }

    private void setOpen(Level level, BlockPos pos, BlockState state, boolean open, Player player,
            boolean updatePowered) {
        boolean changedOpen = state.getValue(OPEN) != open;
        BlockState updated = state.setValue(OPEN, open);
        if (updatePowered) {
            updated = updated.setValue(POWERED, open);
        }
        level.setBlock(pos, updated, 10);

        BlockPos otherPos = otherHalf(pos, state);
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this)) {
            BlockState updatedOther = otherState.setValue(OPEN, open);
            if (updatePowered) {
                updatedOther = updatedOther.setValue(POWERED, open);
            }
            level.setBlock(otherPos, updatedOther, 10);
        }

        if (changedOpen) {
            SoundEvent sound = (open ? ModSounds.BLOCK_OPEN_DOOR : ModSounds.BLOCK_CLOSE_DOOR).get();
            level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F,
                    level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    private static BlockPos otherHalf(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
    }
}
