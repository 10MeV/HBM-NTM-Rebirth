package com.hbm.ntm.entity.effect;

import com.hbm.ntm.block.FalloutLayerBlock;
import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.entity.logic.ExplosionChunkLoadingEntity;
import com.hbm.ntm.multiblock.DummyBlock;
import com.hbm.ntm.radiation.CraterBiomeUtil;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.LegacyFalloutConversions;
import com.hbm.ntm.radiation.LegacyRadiationWorldUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FalloutRainEntity extends ExplosionChunkLoadingEntity {
    private final List<Long> chunksToProcess = new ArrayList<>();
    private final List<Long> outerChunksToProcess = new ArrayList<>();
    private boolean firstTick = true;
    private int tickDelay;
    private int scale = 1;

    public FalloutRainEntity(EntityType<? extends FalloutRainEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public FalloutRainEntity(Level level, int scale) {
        this(ModEntityTypes.FALLOUT_RAIN.get(), level);
        setScale(scale);
    }

    public static FalloutRainEntity create(Level level, double x, double y, double z, int scale) {
        FalloutRainEntity entity = new FalloutRainEntity(level, scale);
        entity.setPos(x, y, z);
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }

        if (firstTick) {
            forceCenterChunk();
            if (chunksToProcess.isEmpty() && outerChunksToProcess.isEmpty()) {
                gatherChunks();
            }
            firstTick = false;
        }

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        tickDelay = BombConfig.FALLOUT_DELAY.get();
        long deadline = System.currentTimeMillis() + BombConfig.MK5_BUDGET_MS.get();
        while (System.currentTimeMillis() < deadline) {
            if (!processNextChunk()) {
                discard();
                return;
            }
        }
    }

    private boolean processNextChunk() {
        if (!chunksToProcess.isEmpty()) {
            processChunk(chunksToProcess.remove(chunksToProcess.size() - 1), false);
            return true;
        }
        if (!outerChunksToProcess.isEmpty()) {
            processChunk(outerChunksToProcess.remove(outerChunksToProcess.size() - 1), true);
            return true;
        }
        return false;
    }

    private void gatherChunks() {
        int radius = getScale();
        int minChunkX = ((int) Math.floor(getX() - radius)) >> 4;
        int maxChunkX = ((int) Math.floor(getX() + radius)) >> 4;
        int minChunkZ = ((int) Math.floor(getZ() - radius)) >> 4;
        int maxChunkZ = ((int) Math.floor(getZ() + radius)) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                double nearestX = clamp(getX(), chunkX << 4, (chunkX << 4) + 15);
                double nearestZ = clamp(getZ(), chunkZ << 4, (chunkZ << 4) + 15);
                double distance = Math.hypot(nearestX - getX(), nearestZ - getZ());
                if (distance > radius) {
                    continue;
                }

                long packed = ChunkPos.asLong(chunkX, chunkZ);
                double centerDistance = Math.hypot(((chunkX << 4) + 8) - getX(), ((chunkZ << 4) + 8) - getZ());
                if (centerDistance <= Math.max(0, radius - 12)) {
                    chunksToProcess.add(packed);
                } else {
                    outerChunksToProcess.add(packed);
                }
            }
        }

        Comparator<Long> byDistanceDescending = Comparator.comparingDouble(this::chunkCenterDistance).reversed();
        chunksToProcess.sort(byDistanceDescending);
        outerChunksToProcess.sort(byDistanceDescending);
    }

    private void processChunk(long packedChunk, boolean checkDistance) {
        ChunkPos chunkPos = new ChunkPos(packedChunk);
        loadChunk(chunkPos.x, chunkPos.z);
        boolean biomeModified = false;
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                double distance = Math.hypot(x - getX(), z - getZ());
                if (checkDistance && distance > getScale()) {
                    continue;
                }

                double percent = distance * 100.0D / getScale();
                stomp(x, z, percent);
                biomeModified |= applyCraterRadiationMarker(x, z, percent);
            }
        }
        if (biomeModified && level() instanceof ServerLevel serverLevel) {
            CraterBiomeUtil.resendCraterBiomes(serverLevel, chunkPos);
        }
    }

    private void stomp(int x, int z, double percent) {
        int depth = 0;
        int yStart = Math.min(level().getMaxBuildHeight() - 1, LegacyRadiationWorldUtil.legacyHeightValue(level(), x, z));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = yStart; y >= level().getMinBuildHeight(); y--) {
            if (depth >= 3) {
                return;
            }

            pos.set(x, y, z);
            BlockState state = level().getBlockState(pos);
            if (state.isAir() || state.is(ModBlocks.FALLOUT.get())) {
                continue;
            }

            BlockPos above = pos.above();
            BlockState aboveState = level().getBlockState(above);
            if (depth == 0 && canReplaceWithFallout(aboveState, above)) {
                double distance = percent / 100.0D;
                double chance = 0.1D - Math.pow(distance - 0.7D, 2.0D);
                BlockState fallout = ModBlocks.FALLOUT.get().defaultBlockState();
                if (chance >= level().random.nextDouble() && fallout.canSurvive(level(), above)) {
                    level().setBlock(above, fallout, 3);
                }
            }

            if (percent < 65.0D && state.isFlammable(level(), pos, Direction.UP)
                    && level().random.nextInt(5) == 0 && level().getBlockState(above).isAir()) {
                level().setBlock(above, BaseFireBlock.getState(level(), above), 3);
            }

            LegacyFalloutConversions.Result falloutEval = LegacyFalloutConversions.apply(level(), pos, state, percent);
            if (falloutEval.restrictDepth()) {
                depth++;
            }

            triggerLegacyCollapse(pos, state, percent, depth);

            if (!falloutEval.matched()
                    && state.isCollisionShapeFullBlock(level(), pos)
                    && !(state.getBlock() instanceof FalloutLayerBlock)) {
                depth++;
            }
        }
    }

    private void triggerLegacyCollapse(BlockPos pos, BlockState originalState, double percent, int depth) {
        if (percent >= 65.0D
                || pos.getY() <= level().getMinBuildHeight()
                || originalState.getBlock() instanceof DummyBlock
                || !level().getBlockState(pos.below()).isAir()) {
            return;
        }

        float hardness = originalState.getDestroySpeed(level(), pos);
        if (!isLegacyFalloutCollapsible(hardness)) {
            return;
        }

        for (int i = 0; i <= depth; i++) {
            BlockPos fallingPos = pos.above(i);
            if (fallingPos.getY() >= level().getMaxBuildHeight()) {
                break;
            }

            BlockState fallingState = level().getBlockState(fallingPos);
            if (fallingState.isAir() || !isLegacyFalloutCollapsible(fallingState.getDestroySpeed(level(), fallingPos))) {
                continue;
            }

            FallingBlockEntity falling = FallingBlockEntity.fall(level(), fallingPos, fallingState);
            falling.dropItem = false;
        }
    }

    private static boolean isLegacyFalloutCollapsible(float hardness) {
        return hardness >= 0.0F && hardness <= Blocks.STONE_BRICKS.getExplosionResistance();
    }

    private boolean applyCraterRadiationMarker(int x, int z, double percent) {
        if (level() instanceof ServerLevel serverLevel) {
            CraterRadiationData.CraterZone zone = CraterRadiationData.zoneForFalloutPercent(percent, getScale());
            if (zone != CraterRadiationData.CraterZone.NONE && CraterRadiationData.setZone(serverLevel, x, z, zone)) {
                return CraterBiomeUtil.setCraterBiome(serverLevel, x, z, zone);
            }
        }
        return false;
    }

    private boolean canReplaceWithFallout(BlockState state, BlockPos pos) {
        return state.isAir() || (state.canBeReplaced() && state.getFluidState().isEmpty() && !state.is(Blocks.FIRE));
    }

    private double chunkCenterDistance(long packedChunk) {
        ChunkPos chunkPos = new ChunkPos(packedChunk);
        return Math.hypot(((chunkPos.x << 4) + 8) - getX(), ((chunkPos.z << 4) + 8) - getZ());
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public void setScale(int scale) {
        this.scale = Math.max(1, scale);
    }

    public int getScale() {
        return Math.max(1, scale);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setScale(tag.getInt("scale"));
        tickDelay = tag.getInt("tickDelay");
        firstTick = tag.getBoolean("firstTick");
        readChunkList(tag.getLongArray("chunks"), chunksToProcess);
        readChunkList(tag.getLongArray("outerChunks"), outerChunksToProcess);
        readChunkLoader(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("scale", getScale());
        tag.putInt("tickDelay", tickDelay);
        tag.putBoolean("firstTick", firstTick);
        tag.putLongArray("chunks", chunksToProcess);
        tag.putLongArray("outerChunks", outerChunksToProcess);
        saveChunkLoader(tag);
    }

    private static void readChunkList(long[] packedChunks, List<Long> target) {
        target.clear();
        for (long packedChunk : packedChunks) {
            target.add(packedChunk);
        }
    }
}
