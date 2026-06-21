package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientBiomeSyncPacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;

/**
 * Legacy BiomeSyncPacket facade. The old packet wrote chunk coordinates as
 * fixed ints and, for single-cell updates, wrote biome before local X/Z.
 * Modern sends delegate to ClientBiomeSyncPacket.
 */
public class BiomeSyncPacket extends ThreadedPacket {
    public int chunkX;
    public int chunkZ;
    public byte blockX;
    public byte blockZ;
    public short biome;
    public short[] biomeArray;

    public BiomeSyncPacket() {
    }

    public BiomeSyncPacket(int chunkX, int chunkZ, byte[] biomeArray) {
        this(chunkX, chunkZ, bytesToShorts(biomeArray));
    }

    public BiomeSyncPacket(int blockX, int blockZ, byte biome) {
        this(blockX, blockZ, (short) biome);
    }

    public BiomeSyncPacket(int chunkX, int chunkZ, short[] biomeArray) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.biomeArray = biomeArray == null ? null : Arrays.copyOf(biomeArray, biomeArray.length);
    }

    public BiomeSyncPacket(int blockX, int blockZ, short biome) {
        this.chunkX = blockX >> 4;
        this.chunkZ = blockZ >> 4;
        this.blockX = (byte) (blockX & 15);
        this.blockZ = (byte) (blockZ & 15);
        this.biome = biome;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(chunkX);
        buffer.writeInt(chunkZ);
        if (biomeArray == null) {
            buffer.writeBoolean(false);
            buffer.writeShort(biome);
            buffer.writeByte(blockX);
            buffer.writeByte(blockZ);
            return;
        }
        buffer.writeBoolean(true);
        for (int i = 0; i < 256; i++) {
            buffer.writeShort(i < biomeArray.length ? biomeArray[i] : 0);
        }
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        chunkX = buffer.readInt();
        chunkZ = buffer.readInt();
        if (!buffer.readBoolean()) {
            biome = buffer.readShort();
            blockX = buffer.readByte();
            blockZ = buffer.readByte();
            biomeArray = null;
            return;
        }
        biomeArray = new short[256];
        for (int i = 0; i < biomeArray.length; i++) {
            biomeArray[i] = buffer.readShort();
        }
    }

    @Override
    public ClientBiomeSyncPacket toModernPacket() {
        if (biomeArray != null) {
            return ModMessages.biomeSyncChunkPacket(chunkX, chunkZ, biomeArray);
        }
        return new ClientBiomeSyncPacket(chunkX, chunkZ, blockX & 15, blockZ & 15, biome, null);
    }

    private static short[] bytesToShorts(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        short[] shorts = new short[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            shorts[i] = bytes[i];
        }
        return shorts;
    }
}
