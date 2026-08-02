package com.hbm.saveddata.satellites;

import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Current-source XSatelliteRegistry base contract.  It intentionally sits on
 * the existing SavedData satellite carrier: the first nine X entries share the
 * current list order, and Precision Laser follows them at ID 9.
 */
public abstract class SatelliteBase extends Satellite {
    public static final String CHAN_SATLINK = "SAT_LINK";
    public static final String CMD_SETTARGET = "settarget";
    public static final String CMD_GETTARGET = "gettarget";
    public static final String CMD_GETTARGETX = "gettargetx";
    public static final String CMD_GETTARGETZ = "gettargetz";

    public int range = 1_000;
    public int targetX;
    public int targetZ;
    public String tx = "";

    public abstract String getType();

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("targetX", targetX);
        tag.putInt("targetZ", targetZ);
        tag.putString("tx", tx);
    }

    @Override
    public void load(CompoundTag tag) {
        targetX = tag.getInt("targetX");
        targetZ = tag.getInt("targetZ");
        tx = tag.getString("tx");
    }

    @Override
    public void onOrbit(ServerLevel level, double x, double y, double z) {
        setTarget((int) Math.floor(x), (int) Math.floor(z));
        RTTYSystem.broadcast(level, CHAN_SATLINK,
                "Established connection to " + getType() + " at " + targetX + " / " + targetZ);
    }

    public void onCommand(ServerLevel level, String... command) {
        onCommandTarget(command);
        onCommandImpl(level, command);
    }

    public void onCommandTarget(String... command) {
        if (command == null || command.length == 0) {
            return;
        }
        if (CMD_SETTARGET.equals(command[0])) {
            if (command.length == 3) {
                targetX = parseInteger(command[1]);
                targetZ = parseInteger(command[2]);
            } else if (command.length == 4) {
                targetX = parseInteger(command[1]);
                targetZ = parseInteger(command[3]);
            }
            return;
        }
        if (CMD_GETTARGET.equals(command[0])) {
            tx = targetX + ";" + targetZ;
        } else if (CMD_GETTARGETX.equals(command[0])) {
            tx = Integer.toString(targetX);
        } else if (CMD_GETTARGETZ.equals(command[0])) {
            tx = Integer.toString(targetZ);
        }
    }

    public void onCommandImpl(ServerLevel level, String... command) {
    }

    public void setTarget(int x, int z) {
        targetX = x;
        targetZ = z;
    }

    @Override
    public void onCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
    }

    private static int parseInteger(String value) {
        return RORInteractive.parseInt(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
