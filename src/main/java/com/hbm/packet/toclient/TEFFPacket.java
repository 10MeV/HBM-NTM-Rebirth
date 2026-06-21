package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy force-field tile packet facade. The old primitive field order is kept
 * for migrated call sites; modern handling delegates to TileSyncPacket.
 */
public class TEFFPacket extends ThreadedPacket {
    public int x;
    public int y;
    public int z;
    public float rad;
    public int health;
    public int maxHealth;
    public int power;
    public boolean isOn;
    public int color;
    public int cooldown;

    public TEFFPacket() {
    }

    public TEFFPacket(int x, int y, int z, float rad, int health, int maxHealth, int power,
            boolean isOn, int color, int cooldown) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rad = rad;
        this.health = health;
        this.maxHealth = maxHealth;
        this.power = power;
        this.isOn = isOn;
        this.color = color;
        this.cooldown = cooldown;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        rad = buffer.readFloat();
        health = buffer.readInt();
        maxHealth = buffer.readInt();
        power = buffer.readInt();
        isOn = buffer.readBoolean();
        color = buffer.readInt();
        cooldown = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeFloat(rad);
        buffer.writeInt(health);
        buffer.writeInt(maxHealth);
        buffer.writeInt(power);
        buffer.writeBoolean(isOn);
        buffer.writeInt(color);
        buffer.writeInt(cooldown);
    }

    @Override
    public TileSyncPacket toModernPacket() {
        return ModMessages.teffPacket(x, y, z, rad, health, maxHealth, power, isOn, color, cooldown);
    }
}
