package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyEmitterBlock;
import com.hbm.ntm.client.obj.LegacyEmitterBeamRenderer;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LegacyEmitterBlockEntity extends BlockEntity {
    private static final int RANGE = 100;
    private static final int EFFECT_COUNT = 5;

    private int color;
    private int beam;
    private float girth = 0.5F;
    private int effect;

    public LegacyEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_EMITTER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyEmitterBlockEntity emitter) {
        if (level.isClientSide || !state.hasProperty(LegacyEmitterBlock.FACING)) {
            return;
        }
        Direction direction = state.getValue(LegacyEmitterBlock.FACING);
        boolean changed = false;
        if (level.getGameTime() % 20L == 0L) {
            int previous = emitter.beam;
            emitter.beam = emitter.scanBeam(level, pos, direction);
            changed = previous != emitter.beam;
        }
        if (emitter.effect == 4 && emitter.beam > 0 && level.getGameTime() % 5L == 0L) {
            emitter.spawnPlasmaBlast(level, pos, direction);
        }
        if (changed) {
            emitter.setChangedAndSync();
        }
    }

    private int scanBeam(Level level, BlockPos pos, Direction direction) {
        int result = 0;
        for (int i = 1; i <= RANGE; i++) {
            result = i;
            BlockPos beamPos = pos.relative(direction, i);
            BlockState beamState = level.getBlockState(beamPos);
            if (beamState.isFaceSturdy(level, beamPos, direction)) {
                break;
            }
        }
        return result;
    }

    private void spawnPlasmaBlast(Level level, BlockPos pos, Direction direction) {
        long step = level.getGameTime() / 5L;
        int x = (int) (direction.getStepX() * step % beam);
        int y = (int) (direction.getStepY() * step % beam);
        int z = (int) (direction.getStepZ() * step % beam);
        Vec3 position = Vec3.atCenterOf(pos.offset(x, y, z));
        ParticleUtil.spawnEmitterPlasmaBlast(level, position,
                LegacyEmitterBeamRenderer.emitterColor(level.getGameTime(), color), direction, girth);
    }

    public int getColor() {
        return color;
    }

    public int getBeam() {
        return beam;
    }

    public float getGirth() {
        return girth;
    }

    public int getEffect() {
        return effect;
    }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
        setChangedAndSync();
    }

    public void adjustGirth(float amount) {
        girth = Math.max(0.125F, girth + amount);
        setChangedAndSync();
    }

    public void cycleEffect() {
        effect = (effect + 1) % EFFECT_COUNT;
        setChangedAndSync();
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB box = new AABB(worldPosition);
        BlockState state = getBlockState();
        if (!state.hasProperty(LegacyEmitterBlock.FACING)) {
            return box.inflate(1.0D);
        }
        int renderRange = LegacyEmitterBeamRenderer.range(beam);
        if (renderRange <= 0) {
            return box.inflate(1.0D);
        }
        Direction direction = state.getValue(LegacyEmitterBlock.FACING);
        Vec3 start = Vec3.atCenterOf(worldPosition);
        Vec3 end = start.add(
                direction.getStepX() * (renderRange + 1.0D),
                direction.getStepY() * (renderRange + 1.0D),
                direction.getStepZ() * (renderRange + 1.0D));
        double pad = Math.max(1.0D, Math.max(girth, girth * 2.25D));
        return box.minmax(new AABB(start, end).inflate(pad));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("color", color);
        tag.putFloat("girth", girth);
        tag.putInt("effect", effect);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        color = tag.getInt("color");
        girth = tag.contains("girth") ? Math.max(0.125F, tag.getFloat("girth")) : 0.5F;
        effect = Math.floorMod(tag.getInt("effect"), EFFECT_COUNT);
        if (tag.contains("beam")) {
            beam = tag.getInt("beam");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        tag.putInt("beam", beam);
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
