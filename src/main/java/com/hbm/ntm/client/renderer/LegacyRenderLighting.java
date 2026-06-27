package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class LegacyRenderLighting {
    private static final double INSIDE_EPSILON = 1.0E-4D;
    private static final ThreadLocal<LightProbe> CURRENT_INSTANCE_LIGHT_PROBE = new ThreadLocal<>();

    public static void beginFrame() {
        clearCurrentProbe();
    }

    public static void clearCurrentProbe() {
        CURRENT_INSTANCE_LIGHT_PROBE.remove();
    }

    public static int resolveBlockEntityLight(BlockEntity blockEntity, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            publishUniformProbe(packedLight);
            return packedLight;
        }
        BlockPos pos = blockEntity.getBlockPos();
        LightProbe probe = sampleBoundsProbe(level, new AABB(pos), packedLight);
        int resolved = brightest(packedLight,
                LegacyLightSampleCache.sample(level, pos),
                LegacyLightSampleCache.sample(level, pos.above()),
                probe.c000(),
                probe.c100(),
                probe.c010(),
                probe.c110(),
                probe.c001(),
                probe.c101(),
                probe.c011(),
                probe.c111());
        publishProbe(probe.withResolvedLight(resolved));
        return resolved;
    }

    public static int resolveMultiblockLight(BlockEntity blockEntity, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            publishUniformProbe(packedLight);
            return packedLight;
        }
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof MultiblockCoreBlock coreBlock) {
            BlockPos pos = blockEntity.getBlockPos();
            LegacyMultiblockLayout layout = coreBlock.getMultiblockLayout(state, level, pos);
            if (layout != null) {
                return resolveBoundsLight(blockEntity, layout.structureBoundingBox(pos, 0.0D), packedLight);
            }
        }
        return resolveBlockEntityLight(blockEntity, packedLight);
    }

    public static int resolveMachineLight(BlockEntity blockEntity, BlockState state,
            LegacyMachineDefinition definition, int packedLight) {
        return resolveBoundsLight(blockEntity, definition.lightingBoundingBox(state, blockEntity.getBlockPos()),
                packedLight);
    }

    public static int resolveBoundsLight(BlockEntity blockEntity, AABB bounds, int packedLight) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            publishUniformProbe(packedLight);
            return packedLight;
        }
        if (!isFinite(bounds)) {
            return resolveBlockEntityLight(blockEntity, packedLight);
        }

        BlockPos pos = blockEntity.getBlockPos();
        LightProbe probe = sampleBoundsProbe(level, bounds, packedLight);
        double centerX = (bounds.minX + bounds.maxX) * 0.5D;
        double centerY = (bounds.minY + bounds.maxY) * 0.5D;
        double centerZ = (bounds.minZ + bounds.maxZ) * 0.5D;
        double sampleTopY = insideMax(bounds.maxY);

        int resolved = brightest(packedLight,
                LegacyLightSampleCache.sample(level, pos),
                LegacyLightSampleCache.sample(level, pos.above()),
                LegacyLightSampleCache.sample(level, BlockPos.containing(centerX, bounds.minY, centerZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(centerX, centerY, centerZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(centerX, sampleTopY, centerZ)),
                probe.c000(),
                probe.c100(),
                probe.c010(),
                probe.c110(),
                probe.c001(),
                probe.c101(),
                probe.c011(),
                probe.c111());
        publishProbe(probe.withResolvedLight(resolved));
        return resolved;
    }

    public static LightProbe currentInstanceLightProbe(int packedLight) {
        LightProbe probe = CURRENT_INSTANCE_LIGHT_PROBE.get();
        if (probe != null && probe.resolvedLight() == packedLight) {
            return probe;
        }
        return LightProbe.uniform(packedLight);
    }

    private static double insideMax(double max) {
        return Math.nextAfter(max, Double.NEGATIVE_INFINITY) - INSIDE_EPSILON;
    }

    private static LightProbe sampleBoundsProbe(Level level, AABB bounds, int packedLight) {
        double maxX = insideMax(bounds.maxX);
        double maxY = insideMax(bounds.maxY);
        double maxZ = insideMax(bounds.maxZ);
        return new LightProbe(
                packedLight,
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, bounds.minY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, maxY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, maxY, bounds.minZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, bounds.minY, maxZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, bounds.minY, maxZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(bounds.minX, maxY, maxZ)),
                LegacyLightSampleCache.sample(level, BlockPos.containing(maxX, maxY, maxZ)));
    }

    private static void publishUniformProbe(int packedLight) {
        publishProbe(LightProbe.uniform(packedLight));
    }

    private static void publishProbe(LightProbe probe) {
        CURRENT_INSTANCE_LIGHT_PROBE.set(probe);
    }

    private static boolean isFinite(AABB bounds) {
        return Double.isFinite(bounds.minX) && Double.isFinite(bounds.minY) && Double.isFinite(bounds.minZ)
                && Double.isFinite(bounds.maxX) && Double.isFinite(bounds.maxY) && Double.isFinite(bounds.maxZ);
    }

    private static int brightest(int first, int... candidates) {
        int sky = LightTexture.sky(first);
        int block = LightTexture.block(first);
        for (int candidate : candidates) {
            sky = Math.max(sky, LightTexture.sky(candidate));
            block = Math.max(block, LightTexture.block(candidate));
        }
        return LightTexture.pack(block, sky);
    }

    private LegacyRenderLighting() {
    }

    public record LightProbe(int resolvedLight, int c000, int c100, int c010, int c110,
                             int c001, int c101, int c011, int c111) {
        public static LightProbe uniform(int packedLight) {
            return new LightProbe(packedLight, packedLight, packedLight, packedLight, packedLight,
                    packedLight, packedLight, packedLight, packedLight);
        }

        private LightProbe withResolvedLight(int packedLight) {
            return new LightProbe(packedLight, c000, c100, c010, c110, c001, c101, c011, c111);
        }
    }
}
