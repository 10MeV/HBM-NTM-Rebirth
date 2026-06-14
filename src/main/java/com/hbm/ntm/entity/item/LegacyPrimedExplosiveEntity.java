package com.hbm.ntm.entity.item;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class LegacyPrimedExplosiveEntity extends Entity {
    private static final EntityDataAccessor<Integer> BLOCK_STATE_ID =
            SynchedEntityData.defineId(LegacyPrimedExplosiveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FUSE =
            SynchedEntityData.defineId(LegacyPrimedExplosiveEntity.class, EntityDataSerializers.INT);

    private int fuse = 80;
    private boolean detonateOnCollision;
    @Nullable
    private Block bombBlock;

    public LegacyPrimedExplosiveEntity(EntityType<? extends LegacyPrimedExplosiveEntity> type, Level level) {
        super(type, level);
        blocksBuilding = true;
    }

    public LegacyPrimedExplosiveEntity(Level level) {
        this(ModEntityTypes.LEGACY_PRIMED_EXPLOSIVE.get(), level);
    }

    public static LegacyPrimedExplosiveEntity create(Level level, double x, double y, double z, Block bombBlock,
            int fuseWindow, boolean detonateOnCollision) {
        int fuse = fuseWindow <= 0 ? 0 : level.random.nextInt(fuseWindow) + fuseWindow / 2;
        return createFixedFuse(level, x, y, z, bombBlock, fuse, detonateOnCollision);
    }

    public static LegacyPrimedExplosiveEntity createFixedFuse(Level level, double x, double y, double z, Block bombBlock,
            int fuse, boolean detonateOnCollision) {
        LegacyPrimedExplosiveEntity entity = new LegacyPrimedExplosiveEntity(level);
        entity.bombBlock = bombBlock;
        entity.fuse = Math.max(0, fuse);
        entity.detonateOnCollision = detonateOnCollision;
        entity.entityData.set(BLOCK_STATE_ID, Block.getId(bombBlock.defaultBlockState()));
        entity.entityData.set(FUSE, entity.fuse);
        entity.setPos(x, y, z);
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        entity.setDeltaMovement(-Math.sin(angle) * 0.02D, 0.2D, -Math.cos(angle) * 0.02D);
        entity.xo = x;
        entity.yo = y;
        entity.zo = z;
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        entityData.set(FUSE, fuse);
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x, motion.y - 0.04D, motion.z);
        move(MoverType.SELF, getDeltaMovement());
        motion = getDeltaMovement();
        setDeltaMovement(motion.x * 0.98D, motion.y * 0.98D, motion.z * 0.98D);

        if (onGround()) {
            motion = getDeltaMovement();
            setDeltaMovement(motion.x * 0.7D, motion.y * -0.5D, motion.z * 0.7D);
        }

        if (fuse-- <= 0 || (detonateOnCollision && (horizontalCollision || verticalCollision))) {
            discard();
            if (!level().isClientSide()) {
                explode();
            }
        } else if (level() instanceof ServerLevel serverLevel) {
            ParticleUtil.spawnLegacyPrimedSmoke(serverLevel, getX(), getY(), getZ());
        }
    }

    public int fuse() {
        return entityData.get(FUSE);
    }

    public BlockState blockState() {
        BlockState state = Block.stateById(entityData.get(BLOCK_STATE_ID));
        return state == null ? Blocks.TNT.defaultBlockState() : state;
    }

    private void explode() {
        Block block = bombBlock;
        if (block instanceof ChainExplodable chainExplodable) {
            chainExplodable.explodeEntity(level(), position(), this);
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !isRemoved();
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BLOCK_STATE_ID, Block.getId(Blocks.TNT.defaultBlockState()));
        entityData.define(FUSE, fuse);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        fuse = tag.getByte("Fuse");
        entityData.set(FUSE, fuse);
        detonateOnCollision = tag.getBoolean("DetonateOnCollision");
        if (tag.contains("Block")) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("Block"));
            bombBlock = id == null ? null : HbmRegistryUtil.block(id).orElse(null);
            if (bombBlock != null) {
                entityData.set(BLOCK_STATE_ID, Block.getId(bombBlock.defaultBlockState()));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("Fuse", (byte) fuse);
        tag.putBoolean("DetonateOnCollision", detonateOnCollision);
        if (bombBlock != null) {
            tag.putString("Block", HbmRegistryUtil.blockKey(bombBlock).toString());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
