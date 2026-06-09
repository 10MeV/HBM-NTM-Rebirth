package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class RBMKBlockPlanner {
    public static final int DUMMY_EXTRA_OFFSET = 6;
    public static final int CORE_METADATA_OFFSET = 10;
    public static final int DEFAULT_GUI_ID = 0;
    public static final float LEGACY_HARDNESS = 3.0F;
    public static final float LEGACY_RESISTANCE = 30.0F;
    public static final double LID_COLLISION_HEIGHT = 0.25D;
    public static final double DETAILED_HITBOX_MAX_Y = 0.999D;

    private RBMKBlockPlanner() {
    }

    public static MetadataKind metadataKind(int meta) {
        if (meta >= CORE_METADATA_OFFSET + Direction.NORTH.ordinal()) {
            return MetadataKind.CORE;
        }
        if (hasExtra(meta)) {
            return MetadataKind.EXTRA_DUMMY;
        }
        return MetadataKind.DUMMY;
    }

    public static boolean hasExtra(int meta) {
        return meta > 5 && meta < 12;
    }

    public static int removeExtraFlag(int meta) {
        return hasExtra(meta) ? meta - DUMMY_EXTRA_OFFSET : meta;
    }

    public static int addExtraFlag(int meta) {
        return meta >= 0 && meta <= 5 ? meta + DUMMY_EXTRA_OFFSET : meta;
    }

    public static RBMKColumnLifecyclePlanner.LidType lidFromCoreMeta(int meta) {
        return RBMKColumnLifecyclePlanner.lidFromLegacyMeta(meta);
    }

    public static int coreMetaForLid(RBMKColumnLifecyclePlanner.LidType lidType) {
        return RBMKColumnLifecyclePlanner.legacyMetaForLid(lidType);
    }

    public static ColumnStructurePlan planColumnStructure(BlockPos core, int columnHeight) {
        BlockPos safeCore = core == null ? BlockPos.ZERO : core;
        int height = Math.max(1, columnHeight);
        List<BlockPos> columnBlocks = new ArrayList<>(height + 1);
        for (int y = 0; y <= height; y++) {
            columnBlocks.add(safeCore.above(y));
        }
        return new ColumnStructurePlan(
                safeCore,
                new LegacyDimensions(height, 0, 0, 0, 0, 0),
                List.copyOf(columnBlocks),
                safeCore.above(height),
                true);
    }

    public static OpenGuiPlan planOpenGui(boolean remoteLevel, boolean holdingLid, boolean hasLid, boolean sneaking) {
        if (remoteLevel) {
            return new OpenGuiPlan(true, false, DEFAULT_GUI_ID, null);
        }
        RBMKColumnLifecyclePlanner.ActivationPlan activation =
                RBMKColumnLifecyclePlanner.planOpenActivation(holdingLid, hasLid, sneaking);
        return new OpenGuiPlan(
                activation.handled(),
                activation.openGui(),
                DEFAULT_GUI_ID,
                activation.failure());
    }

    public static BreakLidPlan planBreakLidDrop(BlockPos core, int coreMeta, int columnHeight, boolean dropLids) {
        RBMKColumnLifecyclePlanner.LidType lidType = lidFromCoreMeta(coreMeta);
        RBMKColumnLifecyclePlanner.LidDropPlan drop =
                RBMKColumnLifecyclePlanner.planBreakLidDrop(
                        core == null ? BlockPos.ZERO : core,
                        columnHeight,
                        lidType,
                        dropLids);
        return new BreakLidPlan(coreMeta, lidType, drop);
    }

    public static ScrewdriverPlan planScrewdriverLidRemoval(
            BlockPos core,
            int coreMeta,
            int columnHeight,
            boolean hasLid,
            boolean lidRemovable) {
        RBMKColumnLifecyclePlanner.LidType lidType = hasLid
                ? lidFromCoreMeta(coreMeta)
                : RBMKColumnLifecyclePlanner.LidType.NONE;
        RBMKColumnLifecyclePlanner.LidRemovalPlan removal =
                RBMKColumnLifecyclePlanner.planScrewdriverLidRemoval(
                        core == null ? BlockPos.ZERO : core,
                        columnHeight,
                        lidType,
                        lidRemovable);
        return new ScrewdriverPlan(
                removal.removed(),
                coreMeta,
                removal.newLegacyMeta(),
                removal.removeNeutronNodeLid(),
                removal.suppressExplodeOnBrokenDuringMutation(),
                removal.drop());
    }

    public static AABB collisionBox(AABB baseBox, boolean hasLid) {
        AABB safeBox = baseBox == null ? new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) : baseBox;
        return new AABB(
                safeBox.minX,
                safeBox.minY,
                safeBox.minZ,
                safeBox.maxX,
                safeBox.maxY + (hasLid ? LID_COLLISION_HEIGHT : 0.0D),
                safeBox.maxZ);
    }

    public static AABB detailedHitboxBounds() {
        return new AABB(0.0D, 0.0D, 0.0D, 1.0D, DETAILED_HITBOX_MAX_Y, 1.0D);
    }

    public static AABB rotateDetailedBox(AABB localBox, BlockPos core, Direction rotation) {
        AABB box = localBox == null ? new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D) : localBox;
        BlockPos safeCore = core == null ? BlockPos.ZERO : core;
        Direction safeRotation = rotation == null ? Direction.NORTH : rotation;

        AABB rotated = switch (safeRotation) {
            case EAST -> new AABB(-box.maxZ, box.minY, box.minX, -box.minZ, box.maxY, box.maxX);
            case SOUTH -> new AABB(-box.maxX, box.minY, -box.maxZ, -box.minX, box.maxY, -box.minZ);
            case WEST -> new AABB(box.minZ, box.minY, -box.maxX, box.maxZ, box.maxY, -box.minX);
            default -> new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        };
        return rotated.move(safeCore.getX() + 0.5D, safeCore.getY(), safeCore.getZ() + 0.5D);
    }

    public static TexturePlan texturePlan(boolean hasOwnLid, int renderLid, boolean renderPipes, Direction side) {
        Direction safeSide = side == null ? Direction.NORTH : side;
        boolean vertical = safeSide == Direction.DOWN || safeSide == Direction.UP;
        if (renderPipes) {
            return new TexturePlan(vertical ? TextureRole.PIPE_TOP : TextureRole.PIPE_SIDE);
        }
        if (!hasOwnLid) {
            if (renderLid == LegacyRenderLid.STANDARD.id()) {
                return new TexturePlan(vertical ? TextureRole.COVER_TOP : TextureRole.COVER_SIDE);
            }
            if (renderLid == LegacyRenderLid.GLASS.id()) {
                return new TexturePlan(vertical ? TextureRole.GLASS_TOP : TextureRole.GLASS_SIDE);
            }
        }
        return new TexturePlan(vertical ? TextureRole.COLUMN_TOP : TextureRole.COLUMN_SIDE);
    }

    public enum MetadataKind {
        DUMMY,
        EXTRA_DUMMY,
        CORE
    }

    public enum LegacyRenderLid {
        NONE(0),
        STANDARD(1),
        GLASS(2);

        private final int id;

        LegacyRenderLid(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }

    public enum TextureRole {
        COLUMN_TOP,
        COLUMN_SIDE,
        COVER_TOP,
        COVER_SIDE,
        GLASS_TOP,
        GLASS_SIDE,
        PIPE_TOP,
        PIPE_SIDE
    }

    public record LegacyDimensions(int up, int down, int forward, int backward, int left, int right) {
    }

    public record ColumnStructurePlan(
            BlockPos core,
            LegacyDimensions dimensions,
            List<BlockPos> columnBlocks,
            BlockPos extraBlock,
            boolean markExtraBlock) {
    }

    public record OpenGuiPlan(
            boolean handled,
            boolean openGui,
            int guiId,
            RBMKColumnLifecyclePlanner.ActivationFailure failure) {
    }

    public record BreakLidPlan(
            int coreMeta,
            RBMKColumnLifecyclePlanner.LidType lidType,
            RBMKColumnLifecyclePlanner.LidDropPlan drop) {
    }

    public record ScrewdriverPlan(
            boolean removed,
            int oldCoreMeta,
            int newCoreMeta,
            boolean removeNeutronNodeLid,
            boolean suppressExplodeOnBrokenDuringMutation,
            RBMKColumnLifecyclePlanner.LidDropPlan drop) {
    }

    public record TexturePlan(TextureRole textureRole) {
    }
}
