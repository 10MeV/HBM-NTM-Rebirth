package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CoinEntity extends Entity {
    private static final String TAG_OWNER = "Owner";
    private static final double GRAVITY = 0.02D;

    private UUID ownerUuid;

    public CoinEntity(EntityType<? extends CoinEntity> type, Level level) {
        super(type, level);
    }

    public CoinEntity(Level level) {
        this(ModEntityTypes.COIN.get(), level);
    }

    @Override
    public void tick() {
        Vec3 previous = position();
        super.tick();
        Vec3 motion = getDeltaMovement();
        Vec3 next = previous.add(motion);

        if (!level().isClientSide()) {
            BlockHitResult hit = level().clip(new ClipContext(previous, next, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this));
            if (hit.getType() != HitResult.Type.MISS) {
                discard();
                return;
            }
        }

        move(MoverType.SELF, motion);
        setDeltaMovement(motion.x, motion.y - GRAVITY, motion.z);
    }

    public void setOwner(@Nullable Entity owner) {
        ownerUuid = owner == null ? null : owner.getUUID();
    }

    @Nullable
    public Entity getOwner() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getEntity(ownerUuid);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(TAG_OWNER)) {
            ownerUuid = tag.getUUID(TAG_OWNER);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(TAG_OWNER, ownerUuid);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
