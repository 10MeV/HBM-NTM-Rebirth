package com.hbm.ntm.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public record RadarLaunchCommand(int linkSlot, Target target) {
    public static final String TAG_LINK = "link";
    public static final String TAG_LAUNCH_ENTITY = "launchEntity";
    public static final String TAG_LAUNCH_POS_X = "launchPosX";
    public static final String TAG_LAUNCH_POS_Z = "launchPosZ";

    public RadarLaunchCommand {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null");
        }
    }

    public static RadarLaunchCommand position(int linkSlot, int x, int z) {
        return new RadarLaunchCommand(linkSlot, Target.position(x, z));
    }

    public static RadarLaunchCommand entity(int linkSlot, int entityId) {
        return new RadarLaunchCommand(linkSlot, Target.entity(entityId));
    }

    public static CompoundTag positionTag(int linkSlot, int x, int z) {
        CompoundTag tag = baseTag(linkSlot);
        tag.putInt(TAG_LAUNCH_POS_X, x);
        tag.putInt(TAG_LAUNCH_POS_Z, z);
        return tag;
    }

    public static CompoundTag entityTag(int linkSlot, int entityId) {
        CompoundTag tag = baseTag(linkSlot);
        tag.putInt(TAG_LAUNCH_ENTITY, entityId);
        return tag;
    }

    public static Optional<RadarLaunchCommand> fromTag(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_LINK, Tag.TAG_INT)) {
            return Optional.empty();
        }
        int linkSlot = tag.getInt(TAG_LINK);
        if (tag.contains(TAG_LAUNCH_ENTITY, Tag.TAG_INT)) {
            return Optional.of(entity(linkSlot, tag.getInt(TAG_LAUNCH_ENTITY)));
        }
        if (tag.contains(TAG_LAUNCH_POS_X, Tag.TAG_INT) && tag.contains(TAG_LAUNCH_POS_Z, Tag.TAG_INT)) {
            return Optional.of(position(linkSlot, tag.getInt(TAG_LAUNCH_POS_X), tag.getInt(TAG_LAUNCH_POS_Z)));
        }
        return Optional.empty();
    }

    public static boolean isValidTag(CompoundTag tag, int linkSlotCount) {
        Optional<RadarLaunchCommand> command = fromTag(tag);
        return command.isPresent() && command.get().isValidLinkSlot(linkSlotCount);
    }

    public boolean isValidLinkSlot(int linkSlotCount) {
        return linkSlot >= 0 && linkSlot < linkSlotCount;
    }

    public RadarCommandResult dispatch(ServerLevel level, BlockPos radarPos, RadarCommandReceiver receiver) {
        if (level == null || radarPos == null || receiver == null) {
            return RadarCommandResult.ERROR_INCOMPATIBLE;
        }
        if (target.isEntity()) {
            Entity entity = level.getEntity(target.entityId());
            return entity != null ? receiver.sendCommandEntityResult(entity) : RadarCommandResult.ERROR_NO_TARGET;
        }
        return receiver.sendCommandPositionResult(target.x(), radarPos.getY(), target.z());
    }

    private static CompoundTag baseTag(int linkSlot) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_LINK, linkSlot);
        return tag;
    }

    public record Target(boolean entity, int entityId, int x, int z) {
        public static Target entity(int entityId) {
            return new Target(true, entityId, 0, 0);
        }

        public static Target position(int x, int z) {
            return new Target(false, 0, x, z);
        }

        public boolean isEntity() {
            return entity;
        }
    }
}
