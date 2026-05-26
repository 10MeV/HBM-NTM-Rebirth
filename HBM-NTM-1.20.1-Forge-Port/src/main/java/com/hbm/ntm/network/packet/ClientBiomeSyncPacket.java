package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientBiomeSyncData;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public record ClientBiomeSyncPacket(int chunkX, int chunkZ, int blockX, int blockZ, short biome, short[] biomeArray) implements HbmPreparablePacket {
    public ClientBiomeSyncPacket {
        biomeArray = biomeArray == null ? null : Arrays.copyOf(biomeArray, biomeArray.length);
    }

    public static ClientBiomeSyncPacket single(int blockX, int blockZ, short biome) {
        return new ClientBiomeSyncPacket(blockX >> 4, blockZ >> 4, blockX & 15, blockZ & 15, biome, null);
    }

    public static ClientBiomeSyncPacket chunk(int chunkX, int chunkZ, short[] biomeArray) {
        return new ClientBiomeSyncPacket(chunkX, chunkZ, 0, 0, (short) 0, biomeArray);
    }

    public static ClientBiomeSyncPacket decode(FriendlyByteBuf buffer) {
        int chunkX = buffer.readVarInt();
        int chunkZ = buffer.readVarInt();
        boolean hasArray = buffer.readBoolean();
        if (hasArray) {
            short[] biomes = new short[256];
            for (int i = 0; i < biomes.length; i++) {
                biomes[i] = buffer.readShort();
            }
            return chunk(chunkX, chunkZ, biomes);
        }
        return new ClientBiomeSyncPacket(chunkX, chunkZ, buffer.readByte(), buffer.readByte(), buffer.readShort(), null);
    }

    public static void encode(ClientBiomeSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.chunkX);
        buffer.writeVarInt(packet.chunkZ);
        buffer.writeBoolean(packet.biomeArray != null);
        if (packet.biomeArray != null) {
            for (int i = 0; i < 256; i++) {
                buffer.writeShort(i < packet.biomeArray.length ? packet.biomeArray[i] : 0);
            }
        } else {
            buffer.writeByte(packet.blockX & 15);
            buffer.writeByte(packet.blockZ & 15);
            buffer.writeShort(packet.biome);
        }
    }

    public static void handle(ClientBiomeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.biomeArray != null) {
                ClientBiomeSyncData.updateChunk(packet.chunkX, packet.chunkZ, packet.biomeArray);
            } else {
                ClientBiomeSyncData.updateCell(packet.chunkX, packet.chunkZ, packet.blockX, packet.blockZ, packet.biome);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ClientBiomeSyncPacket(chunkX, chunkZ, blockX, blockZ, biome, biomeArray);
    }
}
