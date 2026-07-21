package com.hbm.ntm.satellite;

import com.hbm.ntm.entity.projectile.TomProjectileEntity;
import com.hbm.ntm.util.AchievementHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class SatelliteHorizons extends Satellite {
    private boolean used;

    public SatelliteHorizons() {
        setSatelliteInterface(Interfaces.SAT_COORD);
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.HORIZONS;
    }

    @Override
    public void onOrbit(ServerLevel level, double x, double y, double z) {
        for (ServerPlayer player : level.players()) {
            AchievementHandler.award(player, AchievementHandler.HORIZONS_START);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("used", used);
    }

    @Override
    public void load(CompoundTag tag) {
        used = tag.getBoolean("used");
    }

    public boolean used() {
        return used;
    }

    @Override
    public void onCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        // SatCoordPacket and ItemSatDesignator dispatch this legacy public
        // virtual directly.  Keep tryCoordAction as a modern result helper,
        // but do not leave the source entry point on Satellite's empty base
        // implementation.
        tryCoordAction(level, player, x, y, z);
    }

    @Override
    public boolean tryCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        if (used) {
            return false;
        }
        used = true;
        SatelliteSavedData.get(level).markDirty();

        TomProjectileEntity tom = new TomProjectileEntity(level);
        tom.setPos(x + 0.5D, 600.0D, z + 0.5D);
        level.getChunk(x >> 4, z >> 4);
        level.addFreshEntity(tom);

        for (ServerPlayer worldPlayer : level.players()) {
            AchievementHandler.award(worldPlayer, AchievementHandler.HORIZONS_END);
        }
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("Horizons has been activated.").withStyle(ChatFormatting.RED), false);
        return true;
    }
}
