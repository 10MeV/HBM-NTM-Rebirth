package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.entity.projectile.TomProjectileEntity;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.util.AchievementHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SatelliteHorizons extends Satellite {
    boolean used = false;

    public SatelliteHorizons() {
        this.satIface = Interfaces.SAT_COORD;
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
