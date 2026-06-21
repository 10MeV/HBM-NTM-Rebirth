package com.hbm.config;

import com.hbm.ntm.radiation.LegacyFalloutConversions;
import com.hbm.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Legacy package facade for hbmFallout.json. The modern conversion library owns
 * parsing, defaults, block resolution, and world-height behavior.
 */
@Deprecated(forRemoval = false)
public final class FalloutConfigJSON {
    public static final List<FalloutEntry> entries = new ArrayList<>();
    public static final Random rand = new Random();

    static {
        entries.add(FalloutEntry.delegate());
    }

    public static void initialize() {
        LegacyFalloutConversions.initialize(FMLPaths.CONFIGDIR.get());
        entries.clear();
        entries.add(FalloutEntry.delegate());
    }

    public static class FalloutEntry {
        private boolean delegate;
        private Block matchesBlock;
        private int matchesMeta = -1;
        private Object matchesMaterial;
        private boolean matchesOpaque;
        private Tuple.Triplet<Block, Integer, Integer>[] primaryBlocks;
        private Tuple.Triplet<Block, Integer, Integer>[] secondaryBlocks;
        private double primaryChance = 1.0D;
        private double minDist = 0.0D;
        private double maxDist = 100.0D;
        private double falloffStart = 0.9D;
        private boolean solid;
        private boolean lastRestrictDepth;

        private static FalloutEntry delegate() {
            FalloutEntry entry = new FalloutEntry();
            entry.delegate = true;
            return entry;
        }

        @Override
        public FalloutEntry clone() {
            FalloutEntry entry = new FalloutEntry();
            entry.delegate = delegate;
            entry.matchesBlock = matchesBlock;
            entry.matchesMeta = matchesMeta;
            entry.matchesMaterial = matchesMaterial;
            entry.matchesOpaque = matchesOpaque;
            entry.primaryBlocks = primaryBlocks;
            entry.secondaryBlocks = secondaryBlocks;
            entry.primaryChance = primaryChance;
            entry.minDist = minDist;
            entry.maxDist = maxDist;
            entry.falloffStart = falloffStart;
            entry.solid = solid;
            return entry;
        }

        public FalloutEntry mB(Block block) {
            this.matchesBlock = block;
            return this;
        }

        public FalloutEntry mM(int meta) {
            this.matchesMeta = meta;
            return this;
        }

        public FalloutEntry mMa(Object material) {
            this.matchesMaterial = material;
            return this;
        }

        public FalloutEntry mO(boolean opaque) {
            this.matchesOpaque = opaque;
            return this;
        }

        @SafeVarargs
        public final FalloutEntry prim(Tuple.Triplet<Block, Integer, Integer>... blocks) {
            this.primaryBlocks = blocks;
            return this;
        }

        @SafeVarargs
        public final FalloutEntry sec(Tuple.Triplet<Block, Integer, Integer>... blocks) {
            this.secondaryBlocks = blocks;
            return this;
        }

        public FalloutEntry c(double chance) {
            this.primaryChance = chance;
            return this;
        }

        public FalloutEntry min(double min) {
            this.minDist = min;
            return this;
        }

        public FalloutEntry max(double max) {
            this.maxDist = max;
            return this;
        }

        public FalloutEntry fo(double falloffStart) {
            this.falloffStart = falloffStart;
            return this;
        }

        public FalloutEntry sol(boolean solid) {
            this.solid = solid;
            return this;
        }

        public boolean eval(Level level, BlockPos pos, BlockState state, double distancePercent) {
            if (delegate) {
                LegacyFalloutConversions.Result result = LegacyFalloutConversions.apply(level, pos, state, distancePercent);
                lastRestrictDepth = result.restrictDepth();
                return result.matched();
            }
            if (state == null || distancePercent > maxDist || distancePercent < minDist) {
                return false;
            }
            if (matchesBlock != null && !state.is(matchesBlock)) {
                return false;
            }
            if (matchesOpaque && !state.isSolidRender(level, pos)) {
                return false;
            }
            if (distancePercent > maxDist * falloffStart && shouldSkipFalloff(level, distancePercent)) {
                return false;
            }
            Tuple.Triplet<Block, Integer, Integer>[] choices =
                    primaryChance == 1.0D || rand.nextDouble() < primaryChance ? primaryBlocks : secondaryBlocks;
            Block replacement = chooseRandomOutcome(choices);
            if (replacement == null) {
                return false;
            }
            level.setBlock(pos, replacement.defaultBlockState(), 3);
            lastRestrictDepth = solid;
            return true;
        }

        public boolean eval(Level level, int x, int y, int z, Block block, int meta, double distancePercent) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = block == null ? level.getBlockState(pos) : block.defaultBlockState();
            return eval(level, pos, state, distancePercent);
        }

        public boolean isSolid() {
            return delegate ? lastRestrictDepth : solid;
        }

        private boolean shouldSkipFalloff(Level level, double distancePercent) {
            double denominator = maxDist - maxDist * falloffStart;
            if (denominator <= 0.0D) {
                return false;
            }
            double distance = (distancePercent - maxDist * falloffStart) / denominator;
            return Math.abs(level.random.nextGaussian()) < Math.pow(distance, 2.0D) * 3.0D;
        }

        private Block chooseRandomOutcome(Tuple.Triplet<Block, Integer, Integer>[] blocks) {
            if (blocks == null || blocks.length == 0) {
                return null;
            }
            int weight = 0;
            for (Tuple.Triplet<Block, Integer, Integer> choice : blocks) {
                weight += Math.max(0, choice.getZ());
            }
            if (weight <= 0) {
                return null;
            }
            int r = rand.nextInt(weight);
            for (Tuple.Triplet<Block, Integer, Integer> choice : blocks) {
                r -= Math.max(0, choice.getZ());
                if (r <= 0) {
                    return choice.getX();
                }
            }
            return blocks[0].getX();
        }
    }

    private FalloutConfigJSON() {
    }
}
