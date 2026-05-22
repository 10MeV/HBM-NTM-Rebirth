package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public record LegacyMachineDefinition(
        int[] legacyXrDimensions,
        int legacyOffset,
        Function<Direction, LegacyMultiblockLayout> layoutFactory,
        ResourceLocation modelLocation,
        ResourceLocation textureLocation,
        boolean renderAll,
        List<String> renderParts,
        float yRotationOffset,
        Function<Direction, Float> yRotationFactory,
        Function<BlockPos, AABB> renderBoundingBoxFactory) {

    public LegacyMultiblockLayout layout(BlockState state) {
        return layoutFactory.apply(state.getValue(HorizontalMachineBlock.FACING));
    }

    public AABB renderBoundingBox(BlockState state, BlockPos corePos) {
        if (renderBoundingBoxFactory != null) {
            return renderBoundingBoxFactory.apply(corePos);
        }
        return layout(state).renderBoundingBox(corePos, 1.0D);
    }

    public float yRotation(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        if (yRotationFactory != null) {
            return yRotationFactory.apply(facing);
        }
        return yRotationOffset + facing.toYRot();
    }

    public static Builder builder(ResourceLocation modelLocation, ResourceLocation textureLocation) {
        return new Builder(modelLocation, textureLocation);
    }

    public static final class Builder {
        private final ResourceLocation modelLocation;
        private final ResourceLocation textureLocation;
        private int[] legacyXrDimensions = new int[] { 0, 0, 0, 0, 0, 0 };
        private int legacyOffset;
        private Function<Direction, LegacyMultiblockLayout> layoutFactory;
        private boolean renderAll = true;
        private List<String> renderParts = List.of();
        private float yRotationOffset = 90.0F;
        private Function<Direction, Float> yRotationFactory;
        private Function<BlockPos, AABB> renderBoundingBoxFactory;

        private Builder(ResourceLocation modelLocation, ResourceLocation textureLocation) {
            this.modelLocation = modelLocation;
            this.textureLocation = textureLocation;
        }

        public Builder legacyXrDimensions(int... dimensions) {
            this.legacyXrDimensions = dimensions;
            return this;
        }

        public Builder legacyOffset(int legacyOffset) {
            this.legacyOffset = legacyOffset;
            return this;
        }

        public Builder proxyPredicate(Predicate<BlockPos> proxyOffsets) {
            this.layoutFactory = facing -> LegacyMultiblockLayout.ofLegacyXr(legacyXrDimensions, facing, proxyOffsets);
            return this;
        }

        public Builder layout(Function<Direction, LegacyMultiblockLayout> layoutFactory) {
            this.layoutFactory = layoutFactory;
            return this;
        }

        public Builder renderParts(String... renderParts) {
            this.renderAll = false;
            this.renderParts = List.of(renderParts);
            return this;
        }

        public Builder yRotationOffset(float yRotationOffset) {
            this.yRotationOffset = yRotationOffset;
            return this;
        }

        public Builder yRotation(Function<Direction, Float> yRotationFactory) {
            this.yRotationFactory = yRotationFactory;
            return this;
        }

        public Builder renderBoundingBox(Function<BlockPos, AABB> renderBoundingBoxFactory) {
            this.renderBoundingBoxFactory = renderBoundingBoxFactory;
            return this;
        }

        public LegacyMachineDefinition build() {
            Function<Direction, LegacyMultiblockLayout> resolvedLayout = layoutFactory != null
                    ? layoutFactory
                    : facing -> LegacyMultiblockLayout.ofLegacyXr(legacyXrDimensions, facing);
            return new LegacyMachineDefinition(legacyXrDimensions, legacyOffset, resolvedLayout, modelLocation,
                    textureLocation, renderAll, renderParts, yRotationOffset, yRotationFactory, renderBoundingBoxFactory);
        }
    }
}
