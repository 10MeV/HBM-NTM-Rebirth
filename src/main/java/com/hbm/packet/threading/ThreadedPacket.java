package com.hbm.packet.threading;

import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.network.LegacyPacketAdapter;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Source-migration base for old packets that participated in PacketThreading.
 * Modern sends should override {@link #toModernPacket()} instead of registering
 * the old packet class on the SimpleChannel.
 */
public abstract class ThreadedPacket implements HbmPreparablePacket, LegacyPacketAdapter {
    private FriendlyByteBuf compiledBuffer;

    public void compile() {
        if (compiledBuffer != null) {
            compiledBuffer.release();
        }
        compiledBuffer = new FriendlyByteBuf(Unpooled.buffer());
        toBytes(compiledBuffer);
    }

    public FriendlyByteBuf getCompiledBuffer() {
        if (compiledBuffer == null || compiledBuffer.readableBytes() <= 0) {
            compile();
        }
        return compiledBuffer;
    }

    public void releaseCompiledBuffer() {
        if (compiledBuffer != null) {
            compiledBuffer.release();
            compiledBuffer = null;
        }
    }

    public void fromBytes(FriendlyByteBuf buffer) {
    }

    public void toBytes(FriendlyByteBuf buffer) {
    }

    @Override
    public Object toModernPacket() {
        return this;
    }

    @Override
    public Object prepareForThreadedSend() {
        return toModernPacket();
    }
}
