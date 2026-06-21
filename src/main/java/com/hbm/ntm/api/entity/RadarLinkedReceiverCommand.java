package com.hbm.ntm.api.entity;

import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class RadarLinkedReceiverCommand {
    private RadarLinkedReceiverCommand() {
    }

    public static RadarCommandResult dispatch(ServerLevel level, BlockPos radarPos, BlockPos receiverPos,
            RadarLaunchCommand command) {
        if (level == null || radarPos == null || receiverPos == null || command == null) {
            return RadarCommandResult.ERROR_INCOMPATIBLE;
        }
        BlockEntity target = MultiblockHelper.resolveOperationalCoreBlockEntity(level, receiverPos);
        if (target == null) {
            return RadarCommandResult.ERROR_NO_TARGET;
        }
        if (!(target instanceof RadarCommandReceiver receiver)) {
            return RadarCommandResult.ERROR_INCOMPATIBLE;
        }
        return command.dispatch(level, radarPos, receiver);
    }
}
