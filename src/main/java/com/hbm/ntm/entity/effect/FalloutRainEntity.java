package com.hbm.ntm.entity.effect;

import com.hbm.ntm.block.FalloutLayerBlock;
import com.hbm.ntm.block.LegacyVolcanoCoreBlock;
import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.entity.item.LegacyFallingBlockEntity;
import com.hbm.ntm.entity.logic.ExplosionChunkLoadingEntity;
import com.hbm.ntm.multiblock.DummyBlock;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.radiation.CraterBiomeUtil;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.LegacyFalloutConversions;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.HbmBlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FalloutRainEntity extends ExplosionChunkLoadingEntity implements IEntityAdditionalSpawnData {
    private static final int CHUNK_COLUMN_COUNT = 16 * 16;

    private static final EntityDataAccessor<Integer> SCALE =
            SynchedEntityData.defineId(FalloutRainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TOTAL_CHUNKS =
            SynchedEntityData.defineId(FalloutRainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INNER_CHUNKS =
            SynchedEntityData.defineId(FalloutRainEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OUTER_CHUNKS =
            SynchedEntityData.defineId(FalloutRainEntity.class, EntityDataSerializers.INT);

    private final List<Long> chunksToProcess = new ArrayList<>();
    private final List<Long> outerChunksToProcess = new ArrayList<>();
    private boolean firstTick = true;
    private int tickDelay = BombConfig.falloutDelayTicks();
    private int scale = 1;
    private long activeChunk = Long.MIN_VALUE;
    private boolean activeOuterChunk;
    private int activeColumnIndex;
    private boolean activeBiomeModified;

    public FalloutRainEntity(EntityType<? extends FalloutRainEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        fireImmune();
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

        long start = System.currentTimeMillis();
        forceCenterChunk();

        if (firstTick) {
            if (chunksToProcess.isEmpty() && outerChunksToProcess.isEmpty()) {
                gatherChunks();
            }
            syncQueueProgress();
            firstTick = false;
        }

        if (tickDelay == 0) {
            tickDelay = BombConfig.falloutDelayTicks();
            long deadline = start + BombConfig.mk5BudgetMs();
            while (System.currentTimeMillis() < deadline) {
                if (!processNextChunk()) {
                    syncQueueProgress();
                    discard();
                    return;
                }
            }
            syncQueueProgress();
        }

        tickDelay--;
    }

    private boolean processNextChunk() {
        if (activeChunk == Long.MIN_VALUE) {
            if (!chunksToProcess.isEmpty()) {
                activeChunk = chunksToProcess.get(chunksToProcess.size() - 1);
                activeOuterChunk = false;
            } else if (!outerChunksToProcess.isEmpty()) {
                activeChunk = outerChunksToProcess.get(outerChunksToProcess.size() - 1);
                activeOuterChunk = true;
            } else {
                return false;
            }
        }

        ChunkPos chunkPos = new ChunkPos(activeChunk);
        loadChunk(chunkPos.x, chunkPos.z);
        int x = chunkPos.getMinBlockX() + activeColumnIndex / 16;
        int z = chunkPos.getMinBlockZ() + activeColumnIndex % 16;
        double distance = Math.hypot(x - getX(), z - getZ());
        if (!activeOuterChunk || distance <= getScale()) {
            double percent = distance * 100.0D / getScale();
            stomp(x, z, percent);
            activeBiomeModified |= applyCraterRadiationMarker(x, z, percent);
        }

        activeColumnIndex++;
        if (activeColumnIndex < CHUNK_COLUMN_COUNT) {
            return true;
        }

        if (activeBiomeModified && level() instanceof ServerLevel serverLevel) {
            CraterBiomeUtil.resendCraterBiomes(serverLevel, chunkPos);
        }
        List<Long> queue = activeOuterChunk ? outerChunksToProcess : chunksToProcess;
        if (!queue.isEmpty() && queue.get(queue.size() - 1) == activeChunk) {
            queue.remove(queue.size() - 1);
        }
        activeChunk = Long.MIN_VALUE;
        activeOuterChunk = false;
        activeColumnIndex = 0;
        activeBiomeModified = false;
        return true;
    }

    private void gatherChunks() {
        Set<Long> chunks = new LinkedHashSet<>();
        Set<Long> outerChunks = new LinkedHashSet<>();
        int outerRange = getScale();
        int adjustedMaxAngle = Math.max(1, 20 * outerRange / 32);

        for (int angle = 0; angle <= adjustedMaxAngle; angle++) {
            outerChunks.add(chunkAtPolar(outerRange, angle, adjustedMaxAngle));
        }
        for (int distance = 0; distance <= outerRange; distance += 8) {
            for (int angle = 0; angle <= adjustedMaxAngle; angle++) {
                long chunkCoord = chunkAtPolar(distance, angle, adjustedMaxAngle);
                if (!outerChunks.contains(chunkCoord)) {
                    chunks.add(chunkCoord);
                }
            }
        }

        chunksToProcess.addAll(chunks);
        outerChunksToProcess.addAll(outerChunks);
        Collections.reverse(chunksToProcess);
        Collections.reverse(outerChunksToProcess);
        entityData.set(TOTAL_CHUNKS, chunksToProcess.size() + outerChunksToProcess.size());
    }

    private void stomp(int x, int z, double percent) {
        int depth = 0;
        int yStart = legacyFalloutTopY();
        int yEnd = level().getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = yStart; y >= yEnd; y--) {
            if (depth >= 3) {
                return;
            }

            pos.set(x, y, z);
            BlockState state = level().getBlockState(pos);
            if (state.isAir() || state.is(ModBlocks.FALLOUT.get())) {
                continue;
            }

            if (state.is(ModBlocks.VOLCANO_CORE.get())) {
                level().setBlock(pos, copyVolcanoMode(state, ModBlocks.VOLCANO_RAD_CORE.get().defaultBlockState()), 3);
                continue;
            }

            BlockPos above = pos.above();
            boolean aboveWritable = isLegacyFalloutWritableY(above);
            if (aboveWritable) {
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
                    level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                }
            }

            LegacyFalloutConversions.Result falloutEval = LegacyFalloutConversions.apply(level(), pos, state, percent);
            if (falloutEval.restrictDepth()) {
                depth++;
            }

            triggerLegacyCollapse(pos, state, percent, depth);

            if (!falloutEval.matched()
                    && falloutEvalCountsAsSurfaceDepth(state, pos)) {
                depth++;
            }
        }
    }

    private void triggerLegacyCollapse(BlockPos pos, BlockState originalState, double percent, int depth) {
        if (percent >= 65.0D
                || pos.getY() <= level().getMinBuildHeight()
                || originalState.getBlock() instanceof DummyBlock
                || originalState.getBlock() instanceof MultiblockCoreBlock
                || !level().getBlockState(pos.below()).isAir()) {
            return;
        }

        float hardness = originalState.getDestroySpeed(level(), pos);
        if (!isLegacyFalloutCollapsible(hardness)) {
            return;
        }

        for (int i = 0; i <= depth; i++) {
            BlockPos fallingPos = pos.above(i);
            if (!isLegacyFalloutWritableY(fallingPos)) {
                break;
            }

            BlockState fallingState = level().getBlockState(fallingPos);
            if (fallingState.isAir() || !isLegacyFalloutCollapsible(fallingState.getDestroySpeed(level(), fallingPos))) {
                continue;
            }

            LegacyFallingBlockEntity falling = LegacyFallingBlockEntity.createNoDrop(level(), fallingPos, fallingState);
            level().addFreshEntity(falling);
        }
    }

    private static boolean isLegacyFalloutCollapsible(float hardness) {
        return hardness >= 0.0F && hardness <= HbmBlockStateUtil.explosionResistance(Blocks.STONE_BRICKS.defaultBlockState());
    }

    private static BlockState copyVolcanoMode(BlockState source, BlockState replacement) {
        return source.hasProperty(LegacyVolcanoCoreBlock.MODE)
                && replacement.hasProperty(LegacyVolcanoCoreBlock.MODE)
                ? replacement.setValue(LegacyVolcanoCoreBlock.MODE, source.getValue(LegacyVolcanoCoreBlock.MODE))
                : replacement;
    }

    private boolean applyCraterRadiationMarker(int x, int z, double percent) {
        if (level() instanceof ServerLevel serverLevel) {
            CraterRadiationData.CraterZone zone = craterZoneForFalloutPercent(percent, getScale());
            if (zone != CraterRadiationData.CraterZone.NONE && CraterRadiationData.setZone(serverLevel, x, z, zone)) {
                return CraterBiomeUtil.setCraterBiome(serverLevel, x, z, zone);
            }
        }
        return false;
    }

    private static CraterRadiationData.CraterZone craterZoneForFalloutPercent(double percent, int scale) {
        if (!RadiationConfig.craterBiomeRadiationEnabled()) {
            return CraterRadiationData.CraterZone.NONE;
        }
        if (scale >= 150 && percent < 15.0D) {
            return CraterRadiationData.CraterZone.INNER;
        }
        if (scale >= 100 && percent < 55.0D) {
            return CraterRadiationData.CraterZone.CRATER;
        }
        if (scale >= 25) {
            return CraterRadiationData.CraterZone.OUTER;
        }
        return CraterRadiationData.CraterZone.NONE;
    }

    private boolean canReplaceWithFallout(BlockState state, BlockPos pos) {
        return state.isAir() || (state.canBeReplaced() && state.getFluidState().isEmpty());
    }

    private int legacyFalloutTopY() {
        return Math.min(255, level().getMaxBuildHeight() - 1);
    }

    private boolean isLegacyFalloutWritableY(BlockPos pos) {
        return pos.getY() <= legacyFalloutTopY() && !level().isOutsideBuildHeight(pos);
    }

    private boolean falloutEvalCountsAsSurfaceDepth(BlockState state, BlockPos pos) {
        return state.isSolidRender(level(), pos) && !(state.getBlock() instanceof FalloutLayerBlock);
    }

    private long chunkAtPolar(int distance, int angle, int adjustedMaxAngle) {
        double radians = angle * Math.PI / 180.0D / (adjustedMaxAngle / 360.0D);
        double x = Math.cos(radians) * distance;
        double z = -Math.sin(radians) * distance;
        return ChunkPos.asLong(((int) (getX() + x)) >> 4, ((int) (getZ() + z)) >> 4);
    }

    private void syncQueueProgress() {
        int remaining = chunksToProcess.size() + outerChunksToProcess.size();
        if (entityData.get(TOTAL_CHUNKS) < remaining) {
            entityData.set(TOTAL_CHUNKS, remaining);
        }
        entityData.set(INNER_CHUNKS, chunksToProcess.size());
        entityData.set(OUTER_CHUNKS, outerChunksToProcess.size());
    }

    public void setScale(int scale) {
        this.scale = Math.max(1, scale);
        entityData.set(SCALE, this.scale);
    }

    public int getScale() {
        return Math.max(1, level().isClientSide() ? entityData.get(SCALE) : scale);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SCALE, 1);
        entityData.define(TOTAL_CHUNKS, 0);
        entityData.define(INNER_CHUNKS, 0);
        entityData.define(OUTER_CHUNKS, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setScale(tag.getInt("scale"));
        tickDelay = tag.contains("tickDelay") ? tag.getInt("tickDelay") : BombConfig.falloutDelayTicks();
        firstTick = !tag.contains("firstTick") || tag.getBoolean("firstTick");
        readChunkList(tag.getLongArray("chunks"), chunksToProcess);
        readChunkList(tag.getLongArray("outerChunks"), outerChunksToProcess);
        activeChunk = tag.contains("activeChunk") ? tag.getLong("activeChunk") : Long.MIN_VALUE;
        activeOuterChunk = tag.getBoolean("activeOuterChunk");
        activeColumnIndex = Math.max(0, Math.min(CHUNK_COLUMN_COUNT - 1, tag.getInt("activeColumnIndex")));
        activeBiomeModified = tag.getBoolean("activeBiomeModified");
        readChunkLoader(tag);
        int savedTotal = tag.contains("totalChunks") ? tag.getInt("totalChunks") : chunksToProcess.size() + outerChunksToProcess.size();
        entityData.set(TOTAL_CHUNKS, Math.max(savedTotal, chunksToProcess.size() + outerChunksToProcess.size()));
        syncQueueProgress();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("scale", getScale());
        tag.putInt("tickDelay", tickDelay);
        tag.putBoolean("firstTick", firstTick);
        tag.putLongArray("chunks", chunksToProcess);
        tag.putLongArray("outerChunks", outerChunksToProcess);
        if (activeChunk != Long.MIN_VALUE) {
            tag.putLong("activeChunk", activeChunk);
            tag.putBoolean("activeOuterChunk", activeOuterChunk);
            tag.putInt("activeColumnIndex", activeColumnIndex);
            tag.putBoolean("activeBiomeModified", activeBiomeModified);
        }
        tag.putInt("totalChunks", entityData.get(TOTAL_CHUNKS));
        saveChunkLoader(tag);
    }

    private static void readChunkList(long[] packedChunks, List<Long> target) {
        target.clear();
        for (long packedChunk : packedChunks) {
            target.add(packedChunk);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(getScale());
        buffer.writeVarInt(entityData.get(TOTAL_CHUNKS));
        buffer.writeVarInt(entityData.get(INNER_CHUNKS));
        buffer.writeVarInt(entityData.get(OUTER_CHUNKS));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        setScale(buffer.readVarInt());
        entityData.set(TOTAL_CHUNKS, buffer.readVarInt());
        entityData.set(INNER_CHUNKS, buffer.readVarInt());
        entityData.set(OUTER_CHUNKS, buffer.readVarInt());
    }

    public int getTotalChunks() {
        return Math.max(0, entityData.get(TOTAL_CHUNKS));
    }

    public int getInnerChunksRemaining() {
        return Math.max(0, entityData.get(INNER_CHUNKS));
    }

    public int getOuterChunksRemaining() {
        return Math.max(0, entityData.get(OUTER_CHUNKS));
    }

    public float getFalloutProgress() {
        int total = getTotalChunks();
        if (total <= 0) {
            return 0.0F;
        }
        int remaining = getInnerChunksRemaining() + getOuterChunksRemaining();
        return 1.0F - Math.min(remaining, total) / (float) total;
    }
}
