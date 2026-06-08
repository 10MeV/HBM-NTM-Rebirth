package com.hbm.ntm.item;

import com.hbm.ntm.api.entity.RadarCommandReceiver;
import com.hbm.ntm.blockentity.RadarScreenBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class RadarLinkerItem extends ItemCoordinateBase {
    public RadarLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canGrabCoordinateHere(Level level, BlockPos pos) {
        BlockEntity target = MultiblockHelper.resolveCoreBlockEntity(level, pos);
        return target instanceof RadarCommandReceiver || target instanceof RadarScreenBlockEntity;
    }

    @Override
    public BlockPos getCoordinates(Level level, BlockPos pos) {
        return MultiblockHelper.resolveCorePos(level, pos);
    }

    @Override
    public void onTargetSet(Level level, BlockPos pos, @Nullable Player player) {
        if (player != null) {
            level.playSound(null, player.blockPosition(), ModSounds.TOOL_TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
