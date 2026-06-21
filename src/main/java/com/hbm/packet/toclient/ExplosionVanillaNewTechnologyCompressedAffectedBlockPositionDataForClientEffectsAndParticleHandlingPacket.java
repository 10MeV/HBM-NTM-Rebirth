package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.packet.threading.ThreadedPacket;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy compressed explosion effect facade. The old packet wrote float center,
 * float size, count, then byte-relative affected block positions.
 */
public class ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket extends ThreadedPacket {
    public double posX;
    public double posY;
    public double posZ;
    public float size;
    public List<BlockPos> affectedBlocks = new ArrayList<>();

    public ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket() {
    }

    public ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket(
            double x, double y, double z, float size, List<?> blocks) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.size = size;
        this.affectedBlocks = normalizeBlocks(blocks);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        posX = buffer.readFloat();
        posY = buffer.readFloat();
        posZ = buffer.readFloat();
        size = buffer.readFloat();
        int count = buffer.readInt();
        affectedBlocks = new ArrayList<>(Math.max(0, count));
        int baseX = (int) posX;
        int baseY = (int) posY;
        int baseZ = (int) posZ;
        for (int i = 0; i < count; i++) {
            affectedBlocks.add(new BlockPos(baseX + buffer.readByte(), baseY + buffer.readByte(), baseZ + buffer.readByte()));
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeFloat((float) posX);
        buffer.writeFloat((float) posY);
        buffer.writeFloat((float) posZ);
        buffer.writeFloat(size);
        buffer.writeInt(affectedBlocks.size());
        int baseX = (int) posX;
        int baseY = (int) posY;
        int baseZ = (int) posZ;
        for (BlockPos pos : affectedBlocks) {
            buffer.writeByte(pos.getX() - baseX);
            buffer.writeByte(pos.getY() - baseY);
            buffer.writeByte(pos.getZ() - baseZ);
        }
    }

    @Override
    public com.hbm.ntm.network.packet.CompressedExplosionEffectPacket toModernPacket() {
        return ModMessages.compressedExplosionEffectPacket(new Vec3(posX, posY, posZ), size, affectedBlocks);
    }

    private static List<BlockPos> normalizeBlocks(List<?> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return new ArrayList<>();
        }
        List<BlockPos> normalized = new ArrayList<>(blocks.size());
        for (Object block : blocks) {
            BlockPos pos = normalizeBlockPos(block);
            if (pos != null) {
                normalized.add(pos);
            }
        }
        return normalized;
    }

    private static BlockPos normalizeBlockPos(Object value) {
        if (value instanceof BlockPos pos) {
            return pos.immutable();
        }
        try {
            Class<?> type = value.getClass();
            int x = type.getField("chunkPosX").getInt(value);
            int y = type.getField("chunkPosY").getInt(value);
            int z = type.getField("chunkPosZ").getInt(value);
            return new BlockPos(x, y, z);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}