package com.hbm.ntm.blockentity;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Saved/client-synchronised state for legacy {@code PartEmitter.TileEntityPartEmitter}. */
public class PartEmitterBlockEntity extends BlockEntity {
    public static final int EFFECT_COUNT = 4;
    private static final String TAG_EFFECT = "effect";

    private int effect;

    public PartEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PART_EMITTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PartEmitterBlockEntity emitter) {
        // Exact PartEmitter#updateEntity server branches. Effect zero intentionally emits nothing.
        switch (emitter.effect) {
            case 1 -> ParticleUtil.spawnGasFlame(level,
                    pos.getX() + level.random.nextDouble(),
                    pos.getY() + 4.5D + level.random.nextDouble(),
                    pos.getZ() + level.random.nextDouble(),
                    level.random.nextGaussian() * 0.2D,
                    0.1D,
                    level.random.nextGaussian() * 0.2D);
            case 2 -> spawnTower(level, pos,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    5.0F,
                    0.25F,
                    5.0F,
                    560 + level.random.nextInt(20),
                    0x404040);
            case 3 -> spawnTower(level, pos,
                    pos.getX() + 0.5D + level.random.nextDouble() * 3.0D - 1.5D,
                    pos.getY() + 1.0D,
                    pos.getZ() + 0.5D + level.random.nextDouble() * 3.0D - 1.5D,
                    0.5F,
                    1.0F,
                    10.0F,
                    750 + level.random.nextInt(250),
                    null);
            default -> {
            }
        }
    }

    public int effect() {
        return effect;
    }

    public void cycleEffect() {
        effect = (effect + 1) % EFFECT_COUNT;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_EFFECT, effect);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        effect = Math.floorMod(tag.getInt(TAG_EFFECT), EFFECT_COUNT);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return createRuntimeSnapshot();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        readRuntimeSnapshot(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        readRuntimeSnapshot(packet.getTag());
    }

    private CompoundTag createRuntimeSnapshot() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_EFFECT, effect);
        return tag;
    }

    private void readRuntimeSnapshot(CompoundTag tag) {
        if (tag != null) {
            effect = Math.floorMod(tag.getInt(TAG_EFFECT), EFFECT_COUNT);
        }
    }

    /**
     * The legacy TargetPoint was anchored to the emitter block, independently of the particle
     * spawn position. Keep that network visibility center instead of using the helper's default.
     */
    private static void spawnTower(Level level, BlockPos emitterPos, double particleX, double particleY,
                                   double particleZ, float lift, float base, float max, int life,
                                   Integer color) {
        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_COOLING_TOWER);
        data.putFloat("lift", lift);
        data.putFloat("base", base);
        data.putFloat("max", max);
        data.putInt("life", life);
        if (color != null) {
            data.putInt("color", color);
        }
        ParticleUtil.spawnAux(level, particleX, particleY, particleZ,
                emitterPos.getX(), emitterPos.getY(), emitterPos.getZ(), data, 150.0D);
    }
}
