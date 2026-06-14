package api.hbm.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Legacy 1.7.10 package bridge for radar scan entries.
 */
@Deprecated(forRemoval = false)
public class RadarEntry {
    public String unlocalizedName;
    public int blipLevel;
    public int posX;
    public int posY;
    public int posZ;
    public int dim;
    public int entityID;
    public boolean redstone;

    public RadarEntry() {
    }

    public RadarEntry(String name, int level, int x, int y, int z, int dim, int entityID, boolean redstone) {
        this.unlocalizedName = name == null ? "" : name;
        this.blipLevel = level;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.dim = dim;
        this.entityID = entityID;
        this.redstone = redstone;
    }

    public RadarEntry(IRadarDetectableNT detectable, Entity entity, boolean redstone) {
        this(detectable.getUnlocalizedName(), detectable.getBlipLevel(),
                entity.getBlockX(), entity.getBlockY(), entity.getBlockZ(),
                legacyDimensionId(entity.level().dimension().location()), entity.getId(), redstone);
    }

    public RadarEntry(IRadarDetectable detectable, Entity entity) {
        this(detectable.getTargetType().radarName(), detectable.getTargetType().ordinal(),
                entity.getBlockX(), entity.getBlockY(), entity.getBlockZ(),
                legacyDimensionId(entity.level().dimension().location()), entity.getId(),
                entity.getDeltaMovement().y < 0.0D);
    }

    public RadarEntry(Player player) {
        this(player.getDisplayName().getString(), IRadarDetectableNT.PLAYER,
                player.getBlockX(), player.getBlockY(), player.getBlockZ(),
                legacyDimensionId(player.level().dimension().location()), player.getId(), true);
    }

    public RadarEntry(com.hbm.ntm.api.entity.RadarEntry entry) {
        this(entry.name(), entry.blipLevel(), entry.pos().getX(), entry.pos().getY(), entry.pos().getZ(),
                legacyDimensionId(entry.dimension()), entry.entityId(), entry.redstone());
    }

    public com.hbm.ntm.api.entity.RadarEntry toModern() {
        return new com.hbm.ntm.api.entity.RadarEntry(unlocalizedName, blipLevel,
                new BlockPos(posX, posY, posZ), dimensionFromLegacyId(dim), entityID, redstone);
    }

    public void fromBytes(ByteBuf buffer) {
        FriendlyByteBuf friendly = new FriendlyByteBuf(buffer);
        this.unlocalizedName = friendly.readUtf();
        this.blipLevel = friendly.readShort();
        this.posX = friendly.readInt();
        this.posY = friendly.readInt();
        this.posZ = friendly.readInt();
        this.dim = friendly.readShort();
        this.entityID = friendly.readInt();
    }

    public void toBytes(ByteBuf buffer) {
        FriendlyByteBuf friendly = new FriendlyByteBuf(buffer);
        friendly.writeUtf(this.unlocalizedName == null ? "" : this.unlocalizedName);
        friendly.writeShort(this.blipLevel);
        friendly.writeInt(this.posX);
        friendly.writeInt(this.posY);
        friendly.writeInt(this.posZ);
        friendly.writeShort(this.dim);
        friendly.writeInt(this.entityID);
    }

    private static int legacyDimensionId(ResourceLocation dimension) {
        if (new ResourceLocation("minecraft", "the_nether").equals(dimension)) {
            return -1;
        }
        if (new ResourceLocation("minecraft", "the_end").equals(dimension)) {
            return 1;
        }
        return 0;
    }

    private static ResourceLocation dimensionFromLegacyId(int legacyDimension) {
        if (legacyDimension == -1) {
            return new ResourceLocation("minecraft", "the_nether");
        }
        if (legacyDimension == 1) {
            return new ResourceLocation("minecraft", "the_end");
        }
        return new ResourceLocation("minecraft", "overworld");
    }
}
