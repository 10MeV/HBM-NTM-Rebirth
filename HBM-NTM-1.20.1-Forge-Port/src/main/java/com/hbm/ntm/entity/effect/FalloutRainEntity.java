package com.hbm.ntm.entity.effect;

import com.hbm.ntm.block.FalloutLayerBlock;
import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.entity.logic.ExplosionChunkLoadingEntity;
import com.hbm.ntm.radiation.LegacyRadiationWorldUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

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
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                double distance = Math.hypot(x - getX(), z - getZ());
                if (checkDistance && distance > getScale()) {
                    continue;
                }

                double percent = distance * 100.0D / getScale();
                stomp(x, z, percent);
            }
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

            FalloutEval falloutEval = applyLegacyFalloutConversion(pos, state, percent);
            if (falloutEval.restrictDepth()) {
                depth++;
            } else if (!falloutEval.matched()
                    && state.isCollisionShapeFullBlock(level(), pos)
                    && !(state.getBlock() instanceof FalloutLayerBlock)) {
                depth++;
            }
        }
    }

    private FalloutEval applyLegacyFalloutConversion(BlockPos pos, BlockState state, double percent) {
        if (percent <= 65.0D) {
            if (state.is(ModBlocks.WASTE_LEAVES.get()) || state.is(BlockTags.LEAVES) || isLegacyPlantOrVine(state)) {
                level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return FalloutEval.match();
            }

            BlockState replacement = null;
            if (state.is(BlockTags.LOGS)
                    || state.is(Blocks.RED_MUSHROOM_BLOCK)
                    || state.is(Blocks.BROWN_MUSHROOM_BLOCK)) {
                replacement = legacyState("waste_log");
            } else if (state.is(BlockTags.PLANKS)) {
                replacement = legacyState("waste_planks");
            } else if (isLegacyWoodenMaterial(state)) {
                replacement = Blocks.AIR.defaultBlockState();
            } else if (state.is(Blocks.SNOW)) {
                replacement = Blocks.AIR.defaultBlockState();
            } else if (state.is(Blocks.MYCELIUM)) {
                replacement = ModBlocks.WASTE_MYCELIUM.get().defaultBlockState();
            } else if (state.is(Blocks.GRASS_BLOCK)) {
                replacement = ModBlocks.WASTE_EARTH.get().defaultBlockState();
            } else if (isLegacySellafieldSurface(state, percent)) {
                replacement = ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();
                if (setFalloutReplacement(pos, replacement)) {
                    return FalloutEval.matchAndRestrictDepth();
                }
                return FalloutEval.noMatch();
            } else if (state.is(Blocks.CLAY)) {
                replacement = Blocks.TERRACOTTA.defaultBlockState();
            }

            if (setFalloutReplacement(pos, replacement)) {
                return FalloutEval.match();
            }
        }

        if (percent >= 60.0D && state.is(BlockTags.LEAVES) && !state.is(ModBlocks.WASTE_LEAVES.get())) {
            level().setBlock(pos, ModBlocks.WASTE_LEAVES.get().defaultBlockState(), 3);
            return FalloutEval.match();
        }

        return FalloutEval.noMatch();
    }

    private boolean setFalloutReplacement(BlockPos pos, BlockState replacement) {
        if (replacement == null) {
            return false;
        }
        level().setBlock(pos, replacement, 3);
        return true;
    }

    private boolean isLegacySellafieldSurface(BlockState state, double percent) {
        if (percent > 45.0D) {
            return false;
        }
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.MUD)
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL);
    }

    private boolean isLegacyPlantOrVine(BlockState state) {
        return state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.VINE)
                || state.is(Blocks.TWISTING_VINES)
                || state.is(Blocks.TWISTING_VINES_PLANT)
                || state.is(Blocks.WEEPING_VINES)
                || state.is(Blocks.WEEPING_VINES_PLANT)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS);
    }

    private boolean isLegacyWoodenMaterial(BlockState state) {
        return state.is(BlockTags.WOODEN_BUTTONS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_PRESSURE_PLATES)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.SIGNS);
    }

    private static BlockState legacyState(String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block == null ? null : block.get().defaultBlockState();
    }

    private record FalloutEval(boolean matched, boolean restrictDepth) {
        private static FalloutEval noMatch() {
            return new FalloutEval(false, false);
        }

        private static FalloutEval match() {
            return new FalloutEval(true, false);
        }

        private static FalloutEval matchAndRestrictDepth() {
            return new FalloutEval(true, true);
        }
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
