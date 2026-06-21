package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Legacy ParticleBurstPacket facade. The old packet wrote x/y/z plus block id
 * and metadata; metadata no longer exists, so modern sends resolve the block id
 * to that block's default state and preserve meta only for diagnostic byte order.
 */
public class ParticleBurstPacket extends ThreadedPacket {
    public int x;
    public int y;
    public int z;
    public int block;
    public int meta;

    public ParticleBurstPacket() {
    }

    public ParticleBurstPacket(int x, int y, int z, int block, int meta) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.meta = meta;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        block = buffer.readInt();
        meta = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(block);
        buffer.writeInt(meta);
    }

    @Override
    public com.hbm.ntm.network.packet.ParticleBurstPacket toModernPacket() {
        BlockState state = Block.stateById(block);
        if (state == null) {
            state = Block.stateById(0);
        }
        return ModMessages.particleBurstPacket(new BlockPos(x, y, z), state);
    }
}