package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.Blowable;
import com.hbm.ntm.block.LegacyFanBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** Runtime state and powered air-stream logic for 1.7.10 MachineFan.TileEntityFan. */
public class LegacyFanBlockEntity extends BlockEntity {
    private static final int RANGE = 10;
    private static final double PUSH = 0.1D;
    private static final String TAG_FALLOFF = "falloff";
    private static final String TAG_SUCK = "suck";

    private float spin;
    private float prevSpin;
    private boolean falloff = true;
    private boolean suck;

    public LegacyFanBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FAN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyFanBlockEntity fan) {
        fan.prevSpin = fan.spin;
        if (level.hasNeighborSignal(pos)) {
            Direction direction = state.getValue(LegacyFanBlock.FACING);
            int effectiveRange = fan.findEffectiveRange(level, pos, direction);
            fan.pushEntities(level, pos, direction, effectiveRange);
            if (level.isClientSide && level.random.nextInt(30) == 0) {
                double speed = fan.suck ? -0.2D : 0.2D;
                level.addParticle(ParticleTypes.CLOUD,
                        pos.getX() + 0.5D + direction.getStepX() * 0.5D,
                        pos.getY() + 0.5D + direction.getStepY() * 0.5D,
                        pos.getZ() + 0.5D + direction.getStepZ() * 0.5D,
                        direction.getStepX() * speed, direction.getStepY() * speed, direction.getStepZ() * speed);
            }
            fan.spin += 30.0F;
        }
        if (fan.spin >= 360.0F) {
            fan.prevSpin -= 360.0F;
            fan.spin -= 360.0F;
        }
    }

    private int findEffectiveRange(Level level, BlockPos pos, Direction direction) {
        int effectiveRange = 0;
        for (int distance = 1; distance <= RANGE; distance++) {
            BlockPos target = pos.relative(direction, distance);
            BlockState targetState = level.getBlockState(target);
            Block targetBlock = targetState.getBlock();
            boolean blowable = targetBlock instanceof Blowable;
            if (targetState.isSolidRender(level, target) || blowable) {
                if (!level.isClientSide && blowable) {
                    ((Blowable) targetBlock).applyFan(level, target, direction, distance);
                }
                break;
            }
            effectiveRange = distance;
        }
        return effectiveRange;
    }

    private void pushEntities(Level level, BlockPos pos, Direction direction, int effectiveRange) {
        int dx = direction.getStepX() * effectiveRange;
        int dy = direction.getStepY() * effectiveRange;
        int dz = direction.getStepZ() * effectiveRange;
        AABB area = new AABB(pos.getX() + 0.5D + Math.min(dx, 0), pos.getY() + 0.5D + Math.min(dy, 0),
                pos.getZ() + 0.5D + Math.min(dz, 0), pos.getX() + 0.5D + Math.max(dx, 0),
                pos.getY() + 0.5D + Math.max(dy, 0), pos.getZ() + 0.5D + Math.max(dz, 0)).inflate(0.5D);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
            double coefficient = PUSH;
            if (falloff) {
                double distance = entity.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                coefficient *= 1.5D * (1.0D - Math.sqrt(distance) / RANGE / 2.0D);
            }
            if (suck) {
                coefficient *= -1.0D;
            }
            entity.push(direction.getStepX() * coefficient, direction.getStepY() * coefficient,
                    direction.getStepZ() * coefficient);
        }
    }

    public float getSpin(float partialTick) {
        return prevSpin + (spin - prevSpin) * partialTick;
    }

    public boolean toggleFalloff() {
        falloff = !falloff;
        syncSettings();
        return falloff;
    }

    public boolean toggleSuck() {
        suck = !suck;
        syncSettings();
        return suck;
    }

    private void syncSettings() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_FALLOFF, falloff);
        tag.putBoolean(TAG_SUCK, suck);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        // Intentionally retains the old missing-key behavior of NBTTagCompound#getBoolean.
        falloff = tag.getBoolean(TAG_FALLOFF);
        suck = tag.getBoolean(TAG_SUCK);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_FALLOFF, falloff);
        tag.putBoolean(TAG_SUCK, suck);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        falloff = tag.getBoolean(TAG_FALLOFF);
        suck = tag.getBoolean(TAG_SUCK);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
