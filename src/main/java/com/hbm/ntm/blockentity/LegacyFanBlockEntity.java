package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.Blowable;
import com.hbm.ntm.block.LegacyFanBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LegacyFanBlockEntity extends BlockEntity {
    private static final int RANGE = 10;
    private static final double BASE_PUSH = 0.1D;

    private float spin;
    private float previousSpin;
    private boolean falloff = true;
    private boolean suck;

    public LegacyFanBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_FAN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyFanBlockEntity fan) {
        fan.previousSpin = fan.spin;
        if (!state.hasProperty(LegacyFanBlock.FACING) || !level.hasNeighborSignal(pos)) {
            fan.wrapSpin();
            return;
        }

        Direction direction = state.getValue(LegacyFanBlock.FACING);
        int effectiveRange = fan.effectiveRange(level, pos, direction);
        fan.pushEntities(level, pos, direction, effectiveRange);
        if (level.isClientSide) {
            fan.spawnCloud(level, pos, direction);
        }
        fan.spin += 30.0F;
        fan.wrapSpin();
    }

    private int effectiveRange(Level level, BlockPos pos, Direction direction) {
        int effectiveRange = 0;
        for (int i = 1; i <= RANGE; i++) {
            BlockPos target = pos.relative(direction, i);
            BlockState targetState = level.getBlockState(target);
            Block block = targetState.getBlock();
            boolean blowable = block instanceof Blowable;
            if (targetState.isCollisionShapeFullBlock(level, target) || blowable) {
                if (!level.isClientSide && blowable) {
                    ((Blowable) block).applyFan(level, target, direction, i);
                }
                break;
            }
            effectiveRange = i;
        }
        return effectiveRange;
    }

    private void pushEntities(Level level, BlockPos pos, Direction direction, int effectiveRange) {
        int x = direction.getStepX() * effectiveRange;
        int y = direction.getStepY() * effectiveRange;
        int z = direction.getStepZ() * effectiveRange;
        AABB area = new AABB(
                pos.getX() + 0.5D + Math.min(x, 0),
                pos.getY() + 0.5D + Math.min(y, 0),
                pos.getZ() + 0.5D + Math.min(z, 0),
                pos.getX() + 0.5D + Math.max(x, 0),
                pos.getY() + 0.5D + Math.max(y, 0),
                pos.getZ() + 0.5D + Math.max(z, 0)).inflate(0.5D);
        List<Entity> affected = level.getEntitiesOfClass(Entity.class, area);
        for (Entity entity : affected) {
            double coeff = BASE_PUSH;
            if (falloff) {
                double dist = entity.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                coeff *= 1.5D * (1.0D - Math.sqrt(dist) / RANGE / 2.0D);
            }
            if (suck) {
                coeff *= -1.0D;
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    direction.getStepX() * coeff,
                    direction.getStepY() * coeff,
                    direction.getStepZ() * coeff));
            entity.hasImpulse = true;
        }
    }

    private void spawnCloud(Level level, BlockPos pos, Direction direction) {
        if (level.random.nextInt(30) != 0) {
            return;
        }
        double speed = suck ? -0.2D : 0.2D;
        level.addParticle(ParticleTypes.CLOUD,
                pos.getX() + 0.5D + direction.getStepX() * 0.5D,
                pos.getY() + 0.5D + direction.getStepY() * 0.5D,
                pos.getZ() + 0.5D + direction.getStepZ() * 0.5D,
                direction.getStepX() * speed,
                direction.getStepY() * speed,
                direction.getStepZ() * speed);
    }

    private void wrapSpin() {
        if (spin >= 360.0F) {
            previousSpin -= 360.0F;
            spin -= 360.0F;
        }
    }

    public float spin(float partialTick) {
        return previousSpin + (spin - previousSpin) * partialTick;
    }

    public boolean falloff() {
        return falloff;
    }

    public void setFalloff(boolean falloff) {
        this.falloff = falloff;
        setChangedAndSync();
    }

    public boolean suck() {
        return suck;
    }

    public void setSuck(boolean suck) {
        this.suck = suck;
        setChangedAndSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("falloff", falloff);
        tag.putBoolean("suck", suck);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        falloff = tag.getBoolean("falloff");
        suck = tag.getBoolean("suck");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
