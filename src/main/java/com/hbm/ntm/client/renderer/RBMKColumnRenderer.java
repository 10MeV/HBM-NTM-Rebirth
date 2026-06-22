package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.RBMKControlRodPlanner;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.hbm.ntm.neutron.RBMKWorldRenderPlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class RBMKColumnRenderer implements BlockEntityRenderer<RBMKColumnBlockEntity> {
    private static final int DEFAULT_FUEL_COLOR = 0x304825;
    private static final double PIPE_PAD_MIN_LOW = 0.0625D;
    private static final double PIPE_PAD_MAX_LOW = 0.4375D;
    private static final double PIPE_PAD_MIN_HIGH = 0.5625D;
    private static final double PIPE_PAD_MAX_HIGH = 0.9375D;
    private static final double PIPE_PAD_MIN_Y = 1.0D;
    private static final double PIPE_PAD_MAX_Y = 1.125D;
    private static volatile ColumnRenderArrays columnRenderArrays =
            new ColumnRenderArrays(-1, new boolean[0], new int[0]);

    private final BlockRenderDispatcher blockRenderer;

    public RBMKColumnRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(RBMKColumnBlockEntity column, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = column.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(column, packedLight);
        renderStaticSegment(blockRenderer, state, 0, columnHeightAbove(), poseStack, buffer, modelLight);
        if (column.getLevel() != null
                && !MultiblockHelper.isOperationalCoreLayoutComplete(column.getLevel(), column.getBlockPos())) {
            return;
        }
        renderDynamicSegment(column, 0, partialTick, poseStack, buffer, modelLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKColumnBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    public static void renderStaticSegment(BlockRenderDispatcher blockRenderer, BlockState state, int segment,
            int heightAbove, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!state.hasProperty(RBMKColumnBlock.LID)) {
            HbmClientRenderUtil.renderBlockModel(blockRenderer, state, poseStack, buffer, packedLight);
            return;
        }
        BlockState segmentState = segment == heightAbove
                ? state
                : state.setValue(RBMKColumnBlock.LID, RBMKColumnBlock.LidType.NONE);
        if (state.getBlock() instanceof RBMKColumnBlock block && block.kind().rod()) {
            renderFuelChannelStaticSegment(block.kind(), segmentState, segment == heightAbove,
                    poseStack, buffer, packedLight);
            return;
        }
        HbmClientRenderUtil.renderBlockModel(blockRenderer, segmentState, poseStack, buffer, packedLight);
        if (segment == heightAbove) {
            renderTopPipePadsIfNeeded(state, poseStack, buffer, packedLight);
        }
    }

    public static boolean hasLegacyTopPipePads(RBMKColumnBlock.Kind kind,
            RBMKColumnBlock.LidType lid) {
        if (kind == RBMKColumnBlock.Kind.BOILER || kind == RBMKColumnBlock.Kind.HEATER) {
            return lid == RBMKColumnBlock.LidType.NONE;
        }
        return kind.control();
    }

    public static void renderDynamicSegment(RBMKColumnBlockEntity column, int segmentIndex, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (segmentIndex < 0 || !column.hasOperationalLayout()) {
            return;
        }
        BlockState state = column.getBlockState();
        RBMKColumnBlock.Kind kind = column.kind();
        if (kind.rod()) {
            renderFuelChannelSegment(column, state, segmentIndex, poseStack, buffer, packedLight, packedOverlay);
        }
        if (kind.control()) {
            renderControlRodSegment(column, kind, state, segmentIndex, partialTick, poseStack, buffer, packedLight,
                    packedOverlay);
        }
    }

    private static void renderFuelChannelSegment(RBMKColumnBlockEntity column, BlockState state, int segmentIndex,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ColumnRenderArrays arrays = columnRenderArrays();
        RBMKWorldRenderPlanner.FuelChannelRenderPlan plan = RBMKWorldRenderPlanner.fuelChannelRenderPlan(
                column.hasFuelRod(),
                (int) Math.round(column.lastFluxQuantity()),
                column.fuelRodRenderColor(),
                arrays.sameColumnAbove(),
                arrays.emptyMetadataAbove());
        if (segmentIndex > plan.columnOffset()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (column.hasFuelRod()) {
            int color = plan.rodRgb() == 0 ? DEFAULT_FUEL_COLOR : plan.rodRgb();
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            ObjRbmkModels.ELEMENT_RODS_VBO.renderPart(plan.part(), ObjRbmkModels.ELEMENT_FUEL_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay, red, green, blue, 255);
        }
        if (plan.cherenkov()) {
            renderCherenkovSegment(new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay),
                    segmentIndex, plan.columnOffset());
        }
        poseStack.popPose();
    }

    private static void renderControlRodSegment(RBMKColumnBlockEntity column, RBMKColumnBlock.Kind kind,
            BlockState state, int segmentIndex, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        RBMKControlRodPlanner.RBMKColor color = column.color();
        RBMKWorldRenderPlanner.ControlRodRenderPlan plan = RBMKWorldRenderPlanner.controlRodRenderPlan(
                !kind.automatic(),
                color,
                column.controlState().lastLevel(),
                column.controlState().level(),
                partialTick,
                columnRenderArrays().sameColumnAbove());
        if (segmentIndex != plan.columnOffset()) {
            return;
        }
        ResourceLocation texture = controlTexture(kind.automatic(), color);

        poseStack.pushPose();
        poseStack.translate(0.5D, plan.lidWorldY() - segmentIndex, 0.5D);
        ObjRbmkModels.RODS_VBO.renderPart(plan.part(), texture,
                new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay));
        poseStack.popPose();
    }

    private static void renderCherenkovSegment(ObjRenderContext context, int segmentIndex, int columnOffset) {
        double globalMax = columnOffset + ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_START_Y;
        double localMin = Math.max(0.0D, ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_START_Y - segmentIndex);
        double localMax = Math.min(1.0D, globalMax - segmentIndex);
        if (localMax < localMin) {
            return;
        }
        LegacyUntexturedQuadRenderer.horizontalSlices(context.withAdditiveTranslucency(),
                -0.5D, -0.5D, 0.5D, 0.5D,
                localMin, localMax, ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_STEP,
                ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_COLOR, ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_ALPHA);
    }

    private static void renderFuelChannelStaticSegment(RBMKColumnBlock.Kind kind, BlockState state,
            boolean topSegment, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        String prefix = fuelChannelTexturePrefix(kind);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight,
                OverlayTexture.NO_OVERLAY);
        renderFuelChannelSideShell(blockSprite(prefix + "_side"), context);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        ObjRenderContext centeredContext = new ObjRenderContext(poseStack, buffer, state, packedLight,
                OverlayTexture.NO_OVERLAY).withLegacyShadow();
        ObjRbmkModels.ELEMENT.renderPartWithSprite("Cap", blockSprite(prefix + "_top"), centeredContext,
                0.0F, 0.0F, 0.0F);
        ObjRbmkModels.ELEMENT.renderPartWithSprite("Inner", blockSprite(prefix + "_inner"), centeredContext,
                0.0F, 0.0F, 0.0F);
        poseStack.popPose();

        if (topSegment && state.getValue(RBMKColumnBlock.LID).hasLid()) {
            renderFuelChannelLidSlab(prefix, state.getValue(RBMKColumnBlock.LID), context);
        }
    }

    private static void renderFuelChannelSideShell(TextureAtlasSprite side, ObjRenderContext context) {
        LegacyTexturedQuadRenderer.spriteQuad(side, context, 0.0F, 0.0F, 1.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 1.0D, 1.0D, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 1.0D, 1.0D, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 0.0D, 1.0D, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 0.0D, 1.0D, 16.0D, 16.0D));
        LegacyTexturedQuadRenderer.spriteQuad(side, context, 1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 1.0D, 1.0D, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 1.0D, 0.0D, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 0.0D, 0.0D, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 0.0D, 1.0D, 16.0D, 16.0D));
        LegacyTexturedQuadRenderer.spriteQuad(side, context, 0.0F, 0.0F, -1.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 1.0D, 0.0D, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 1.0D, 0.0D, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 0.0D, 0.0D, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(1.0D, 0.0D, 0.0D, 16.0D, 16.0D));
        LegacyTexturedQuadRenderer.spriteQuad(side, context, -1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 1.0D, 0.0D, 16.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 1.0D, 1.0D, 0.0D, 0.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 0.0D, 1.0D, 0.0D, 16.0D),
                LegacyTexturedQuadRenderer.spritePixelVertex(0.0D, 0.0D, 0.0D, 16.0D, 16.0D));
    }

    private static void renderFuelChannelLidSlab(String prefix, RBMKColumnBlock.LidType lid,
            ObjRenderContext context) {
        String suffix = lid == RBMKColumnBlock.LidType.GLASS ? "_glass" : "_cover";
        TextureAtlasSprite top = blockSprite(prefix + suffix + "_top");
        TextureAtlasSprite side = blockSprite(prefix + suffix + "_side");
        LegacyAtlasCuboidRenderer.croppedCuboid(top, top, side, side, side, side, context,
                0.0D, 1.0D, 0.0D, 1.0D, 1.25D, 1.0D);
    }

    private static String fuelChannelTexturePrefix(RBMKColumnBlock.Kind kind) {
        return switch (kind) {
            case ROD_MOD -> "rbmk_element_mod";
            case ROD_REASIM -> "rbmk_element_reasim";
            case ROD_REASIM_MOD -> "rbmk_element_reasim_mod";
            default -> "rbmk_element";
        };
    }

    private static void renderTopPipePadsIfNeeded(BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        if (!(state.getBlock() instanceof RBMKColumnBlock block) || !state.hasProperty(RBMKColumnBlock.LID)) {
            return;
        }
        RBMKColumnBlock.LidType lid = state.getValue(RBMKColumnBlock.LID);
        RBMKColumnBlock.Kind kind = block.kind();
        if (!hasLegacyTopPipePads(kind, lid)) {
            return;
        }
        String textureBase = pipeTextureBase(kind);
        if (textureBase == null) {
            return;
        }
        TextureAtlasSprite top = blockSprite(textureBase + "_pipe_top");
        TextureAtlasSprite side = blockSprite(textureBase + "_pipe_side");
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight,
                OverlayTexture.NO_OVERLAY);
        renderPipePad(top, side, context, PIPE_PAD_MIN_LOW, PIPE_PAD_MIN_LOW, PIPE_PAD_MAX_LOW, PIPE_PAD_MAX_LOW);
        renderPipePad(top, side, context, PIPE_PAD_MIN_LOW, PIPE_PAD_MIN_HIGH, PIPE_PAD_MAX_LOW, PIPE_PAD_MAX_HIGH);
        renderPipePad(top, side, context, PIPE_PAD_MIN_HIGH, PIPE_PAD_MIN_HIGH, PIPE_PAD_MAX_HIGH, PIPE_PAD_MAX_HIGH);
        renderPipePad(top, side, context, PIPE_PAD_MIN_HIGH, PIPE_PAD_MIN_LOW, PIPE_PAD_MAX_HIGH, PIPE_PAD_MAX_LOW);
    }

    private static void renderPipePad(TextureAtlasSprite top, TextureAtlasSprite side,
            ObjRenderContext context, double minX, double minZ, double maxX, double maxZ) {
        LegacyAtlasCuboidRenderer.croppedCuboid(top, top, side, side, side, side, context,
                minX, PIPE_PAD_MIN_Y, minZ, maxX, PIPE_PAD_MAX_Y, maxZ);
    }

    private static TextureAtlasSprite blockSprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID,
                "block/rbmk/icons/" + name));
    }

    private static String pipeTextureBase(RBMKColumnBlock.Kind kind) {
        return switch (kind) {
            case BOILER -> "rbmk_boiler";
            case HEATER -> "rbmk_heater";
            case CONTROL -> "rbmk_control";
            case CONTROL_MOD -> "rbmk_control_mod";
            case CONTROL_AUTO -> "rbmk_control_auto";
            case CONTROL_REASIM -> "rbmk_control_reasim";
            case CONTROL_REASIM_AUTO -> "rbmk_control_reasim_auto";
            default -> null;
        };
    }

    private static ResourceLocation controlTexture(boolean automatic, RBMKControlRodPlanner.RBMKColor color) {
        if (automatic) {
            return ObjRbmkModels.CONTROL_AUTO_TEXTURE;
        }
        return color == null
                ? ObjRbmkModels.CONTROL_STANDARD_TEXTURE
                : ObjRbmkModels.manualControlTexture(color.ordinal());
    }

    private static ColumnRenderArrays columnRenderArrays() {
        int heightAbove = columnHeightAbove();
        ColumnRenderArrays cached = columnRenderArrays;
        if (cached.heightAbove() == heightAbove) {
            return cached;
        }
        boolean[] same = new boolean[heightAbove];
        Arrays.fill(same, true);
        ColumnRenderArrays updated = new ColumnRenderArrays(heightAbove, same, new int[heightAbove]);
        columnRenderArrays = updated;
        return updated;
    }

    public static int columnHeightAbove() {
        return RBMKStructureDimensions.columnHeightAboveCore();
    }

    private record ColumnRenderArrays(int heightAbove, boolean[] sameColumnAbove, int[] emptyMetadataAbove) {
    }
}
