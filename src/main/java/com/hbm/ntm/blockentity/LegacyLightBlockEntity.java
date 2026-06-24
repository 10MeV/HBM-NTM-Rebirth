package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyDirectionalShapeBlock;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LegacyLightBlockEntity extends BlockEntity implements HbmEnergyReceiver {
    public static final long FLOODLIGHT_MAX_POWER = 5_000L;
    private static final long FLOODLIGHT_CONSUMPTION = 100L;
    private static final int FLOODLIGHT_BEAM_COUNT = 15;
    private static final int FLOODLIGHT_RANGE = 63;
    private static final int FLOODLIGHT_RESTART_DELAY = 60;

    private float rotation;
    private long power;
    private int delay;
    private boolean on;
    private final BlockPos[] lightPos = new BlockPos[FLOODLIGHT_BEAM_COUNT];
    private final LazyOptional<IEnergyStorage> energyHandler =
            LazyOptional.of(() -> new ForgeEnergyAdapter(this, true, false));

    public LegacyLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_LIGHT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LegacyLightBlockEntity blockEntity) {
        if (level.isClientSide || !isFloodlight(state)) {
            return;
        }

        Direction inputSide = state.getValue(LegacyDirectionalShapeBlock.FACE).getOpposite();
        HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, pos, inputSide, blockEntity);

        if (blockEntity.delay > 0) {
            blockEntity.delay--;
            blockEntity.setChanged();
            return;
        }

        if (blockEntity.power >= FLOODLIGHT_CONSUMPTION) {
            blockEntity.power -= FLOODLIGHT_CONSUMPTION;
            if (!blockEntity.on) {
                blockEntity.on = true;
                blockEntity.castLights();
                blockEntity.setChangedAndSync();
            } else if (level.getGameTime() % 5L == 0L) {
                int index = (int) Math.abs((level.getGameTime() / 5L) % blockEntity.lightPos.length);
                blockEntity.castLight(index);
                blockEntity.setChanged();
            }
            return;
        }

        if (blockEntity.on) {
            blockEntity.on = false;
            blockEntity.delay = FLOODLIGHT_RESTART_DELAY;
            blockEntity.destroyLights();
            blockEntity.setChangedAndSync();
        }
    }

    public float rotation() {
        return rotation;
    }

    public long power() {
        return power;
    }

    @Override
    public long getPower() {
        return power;
    }

    public int delay() {
        return delay;
    }

    public boolean isOn() {
        return on;
    }

    @Override
    public void setPower(long power) {
        this.power = Math.max(0L, Math.min(FLOODLIGHT_MAX_POWER, power));
        setChangedAndSync();
    }

    @Override
    public long getMaxPower() {
        return FLOODLIGHT_MAX_POWER;
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
        setRotationFromPlayer(playerPitch, playerYaw, face);
    }

    public void setRotationFromPlayer(float playerPitch, float playerYaw, Direction face) {
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
        if (on) {
            destroyLights();
        }
        setChangedAndSync();
    }

    public BlockPos lightPos(int index) {
        return index >= 0 && index < lightPos.length ? lightPos[index] : null;
    }

    private void castLight(int index) {
        BlockPos newPos = getRayEndpoint(index);
        BlockPos oldPos = lightPos[index];
        lightPos[index] = null;

        if (newPos == null || !newPos.equals(oldPos)) {
            destroyLightAt(index, oldPos);
        }

        if (newPos == null || level == null || !level.isInWorldBounds(newPos)) {
            return;
        }

        BlockState state = level.getBlockState(newPos);
        if (state.isAir() || state.canBeReplaced()) {
            level.setBlock(newPos, ModBlocks.FLOODLIGHT_BEAM.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(newPos) instanceof FloodlightBeamBlockEntity beam) {
                beam.setSource(this, index);
            }
            lightPos[index] = newPos;
        } else if (state.is(ModBlocks.FLOODLIGHT_BEAM.get())) {
            if (level.getBlockEntity(newPos) instanceof FloodlightBeamBlockEntity beam) {
                beam.setSource(this, index);
            }
            lightPos[index] = newPos;
        }
    }

    private BlockPos getRayEndpoint(int index) {
        if (level == null || index < 0 || index >= lightPos.length) {
            return null;
        }

        int meta = legacyMeta(getBlockState());
        Vec3 dir = new Vec3(1.0D, 0.0D, 0.0D);
        double[] angles = variation(index);

        float adjustedRotation = rotation;
        if (meta == 1 || meta == 7) {
            adjustedRotation = 180.0F - adjustedRotation;
        }
        if (meta == 6) {
            adjustedRotation = 180.0F - adjustedRotation;
        }
        dir = rotateAroundZ(dir, Math.toRadians(adjustedRotation) + angles[0]);

        if (meta == 6 || meta == 7 || meta == 2) {
            dir = rotateAroundY(dir, Math.PI / 2.0D);
        }
        if (meta == 3) {
            dir = rotateAroundY(dir, -Math.PI / 2.0D);
        }
        if (meta == 4) {
            dir = rotateAroundY(dir, Math.PI);
        }
        dir = rotateAroundY(dir, angles[1]);

        for (int i = 1; i <= FLOODLIGHT_RANGE; i++) {
            BlockPos scan = rayPos(dir, i);
            if (scan.equals(worldPosition)) {
                continue;
            }
            if (!level.isInWorldBounds(scan)) {
                return null;
            }
            BlockState state = level.getBlockState(scan);
            if (state.getLightBlock(level, scan) < 127) {
                continue;
            }
            return i > 1 ? rayPos(dir, i - 1) : null;
        }

        return null;
    }

    private BlockPos rayPos(Vec3 dir, int distance) {
        return new BlockPos(
                (int) Math.floor(worldPosition.getX() + 0.5D + dir.x * distance),
                (int) Math.floor(worldPosition.getY() + 0.5D + dir.y * distance),
                (int) Math.floor(worldPosition.getZ() + 0.5D + dir.z * distance));
    }

    private void castLights() {
        for (int i = 0; i < lightPos.length; i++) {
            castLight(i);
        }
    }

    private void destroyLight(int index) {
        destroyLightAt(index, lightPos[index]);
        lightPos[index] = null;
    }

    private void destroyLightAt(int index, @Nullable BlockPos pos) {
        if (level == null || pos == null || !level.isLoaded(pos) || !level.getBlockState(pos).is(ModBlocks.FLOODLIGHT_BEAM.get())) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof FloodlightBeamBlockEntity beam) {
            BlockPos recorded = lightPos(index);
            if (recorded == null || recorded.equals(pos)) {
                level.removeBlock(pos, false);
            }
        }
    }

    private void destroyLights() {
        for (int i = 0; i < lightPos.length; i++) {
            destroyLight(i);
        }
    }

    private static double[] variation(int index) {
        return new double[] {
                Math.toRadians(((index / 3) - 2) * 7.5D),
                Math.toRadians(((index % 3) - 1) * 15.0D)
        };
    }

    private static Vec3 rotateAroundZ(Vec3 vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vec.x * cos - vec.y * sin, vec.y * cos + vec.x * sin, vec.z);
    }

    private static Vec3 rotateAroundY(Vec3 vec, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    private static int legacyMeta(BlockState state) {
        int meta = state.getValue(LegacyDirectionalShapeBlock.FACE).get3DDataValue();
        if ((meta == 0 || meta == 1) && state.getValue(LegacyDirectionalShapeBlock.TOP_BOTTOM_ROTATED)) {
            return meta + 6;
        }
        return meta;
    }

    private static boolean isFloodlight(BlockState state) {
        return state.getBlock() == ModBlocks.legacyBlock("floodlight").get();
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
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY && isFloodlight(getBlockState())
                && (side == null || side == getBlockState().getValue(LegacyDirectionalShapeBlock.FACE).getOpposite())) {
            return energyHandler.cast();
        }
        return super.getCapability(capability, side);
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
