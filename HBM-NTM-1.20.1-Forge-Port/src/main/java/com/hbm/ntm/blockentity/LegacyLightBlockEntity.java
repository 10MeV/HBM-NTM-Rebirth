package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.block.LegacyDirectionalShapeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyLightBlockEntity extends BlockEntity {
    public static final long FLOODLIGHT_MAX_POWER = 5_000L;

    private float rotation;
    private long power;
    private int delay;
    private boolean on;

    public LegacyLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_LIGHT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyLightBlockEntity blockEntity) {
    }

    public float rotation() {
        return rotation;
    }

    public long power() {
        return power;
    }

    public int delay() {
        return delay;
    }

    public boolean isOn() {
        return on;
    }

    public void setPower(long power) {
        this.power = Math.max(0L, Math.min(FLOODLIGHT_MAX_POWER, power));
        setChangedAndSync();
    }

    public void setDelay(int delay) {
        this.delay = Math.max(0, delay);
        setChangedAndSync();
    }

    public void setOn(boolean on) {
        this.on = on;
        setChangedAndSync();
    }

    public void setRotationFromPlacement(float playerPitch, float playerYaw, Direction face) {
        float result = playerPitch;
        if (face == Direction.DOWN || face == Direction.UP) {
            int quadrant = LegacyDirectionalShapeBlock.legacyYawQuadrant(playerYaw);
            if (face == Direction.UP && (quadrant == 0 || quadrant == 1)) {
                result = 180.0F - result;
            }
            if (face == Direction.DOWN && (quadrant == 0 || quadrant == 3)) {
                result = 180.0F - result;
            }
        }
        rotation = -Math.round(result / 5.0F) * 5.0F;
        setChangedAndSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("rotation", rotation);
        tag.putLong("power", power);
        tag.putInt("delay", delay);
        tag.putBoolean("isOn", on);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rotation = tag.getFloat("rotation");
        power = tag.getLong("power");
        delay = tag.getInt("delay");
        on = tag.getBoolean("isOn");
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
