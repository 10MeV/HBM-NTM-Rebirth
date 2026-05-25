package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public record LegacyMachineDefinition(
        int[] legacyXrDimensions,
        int legacyOffset,
        int legacyHeightOffset,
        Function<Direction, Direction> placementFacingFactory,
        Function<Direction, LegacyMultiblockLayout> layoutFactory,
        ResourceLocation modelLocation,
        ResourceLocation textureLocation,
        boolean renderAll,
        List<String> renderParts,
        boolean itemRenderAll,
        List<String> itemRenderParts,
        Map<String, ResourceLocation> itemPartTextures,
        float itemFitSize,
        float legacyItemScale,
        Function<Direction, Vec3> modelTranslationFactory,
        float yRotationOffset,
        Function<Direction, Float> yRotationFactory,
        Function<BlockPos, AABB> renderBoundingBoxFactory,
        Function<BlockState, VoxelShape> collisionShapeFactory) {

    public LegacyMultiblockLayout layout(BlockState state) {
        return layoutFactory.apply(state.getValue(HorizontalMachineBlock.FACING));
    }

    public Direction placementFacing(Direction facing) {
        return placementFacingFactory == null ? facing : placementFacingFactory.apply(facing);
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

    public Vec3 modelTranslation(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return modelTranslationFactory == null ? Vec3.ZERO : modelTranslationFactory.apply(facing);
    }

    public VoxelShape collisionShape(BlockState state) {
        if (collisionShapeFactory != null) {
            return collisionShapeFactory.apply(state);
        }
        return layout(state).shape(1.0D);
    }

    public boolean hasCollisionShapeFactory() {
        return collisionShapeFactory != null;
    }

    public static Builder builder(ResourceLocation modelLocation, ResourceLocation textureLocation) {
        return new Builder(modelLocation, textureLocation);
    }

    public static final class Builder {
        private final ResourceLocation modelLocation;
        private final ResourceLocation textureLocation;
        private int[] legacyXrDimensions = new int[] { 0, 0, 0, 0, 0, 0 };
        private int legacyOffset;
        private int legacyHeightOffset;
        private Function<Direction, Direction> placementFacingFactory;
        private Function<Direction, LegacyMultiblockLayout> layoutFactory;
        private boolean renderAll = true;
        private List<String> renderParts = List.of();
        private Boolean itemRenderAll;
        private List<String> itemRenderParts;
        private Map<String, ResourceLocation> itemPartTextures = Map.of();
        private float itemFitSize = 0.58F;
        private float legacyItemScale;
        private Function<Direction, Vec3> modelTranslationFactory;
        private float yRotationOffset = 90.0F;
        private Function<Direction, Float> yRotationFactory;
        private Function<BlockPos, AABB> renderBoundingBoxFactory;
        private Function<BlockState, VoxelShape> collisionShapeFactory;

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

        public Builder legacyHeightOffset(int legacyHeightOffset) {
            this.legacyHeightOffset = legacyHeightOffset;
            return this;
        }

        public Builder placementFacing(Function<Direction, Direction> placementFacingFactory) {
            this.placementFacingFactory = placementFacingFactory;
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

        public Builder itemRenderParts(String... itemRenderParts) {
            this.itemRenderAll = false;
            this.itemRenderParts = List.of(itemRenderParts);
            return this;
        }

        public Builder itemPartTextures(Map<String, ResourceLocation> itemPartTextures) {
            this.itemPartTextures = Map.copyOf(itemPartTextures);
            return this;
        }

        public Builder itemFitSize(float itemFitSize) {
            this.itemFitSize = itemFitSize;
            return this;
        }

        public Builder legacyItemScale(float legacyItemScale) {
            this.legacyItemScale = legacyItemScale;
            return this;
        }

        public Builder legacyItemScale(double inventoryScale, double commonScale) {
            return legacyItemScale((float) (inventoryScale * commonScale));
        }

        public Builder modelTranslation(Function<Direction, Vec3> modelTranslationFactory) {
            this.modelTranslationFactory = modelTranslationFactory;
            return this;
        }

        public Builder modelTranslation(double x, double y, double z) {
            return modelTranslation(facing -> new Vec3(x, y, z));
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

        public Builder collisionShape(Function<BlockState, VoxelShape> collisionShapeFactory) {
            this.collisionShapeFactory = collisionShapeFactory;
            return this;
        }

        public LegacyMachineDefinition build() {
            Function<Direction, LegacyMultiblockLayout> resolvedLayout = layoutFactory != null
                    ? layoutFactory
                    : facing -> LegacyMultiblockLayout.ofLegacyXr(legacyXrDimensions, facing);
            boolean resolvedItemRenderAll = itemRenderAll == null ? renderAll : itemRenderAll;
            List<String> resolvedItemRenderParts = itemRenderParts == null ? renderParts : itemRenderParts;
            return new LegacyMachineDefinition(legacyXrDimensions, legacyOffset, legacyHeightOffset,
                    placementFacingFactory, resolvedLayout, modelLocation, textureLocation, renderAll, renderParts,
                    resolvedItemRenderAll, resolvedItemRenderParts, itemPartTextures, itemFitSize,
                    legacyItemScale, modelTranslationFactory, yRotationOffset,
                    yRotationFactory, renderBoundingBoxFactory, collisionShapeFactory);
        }
    }
}
