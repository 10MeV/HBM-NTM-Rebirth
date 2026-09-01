package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.UntexturedQuadGroup;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class RBMKColumnRenderer implements BlockEntityRenderer<RBMKColumnBlockEntity> {
    private static final int DEFAULT_FUEL_COLOR = 0x304825;
    private static final double PIPE_PAD_MIN_LOW = 0.0625D;
    private static final double PIPE_PAD_MAX_LOW = 0.4375D;
    private static final double PIPE_PAD_MIN_HIGH = 0.5625D;
    private static final double PIPE_PAD_MAX_HIGH = 0.9375D;
    private static final double PIPE_PAD_MIN_Y = 0.0D;
    private static final double PIPE_PAD_MAX_Y = 0.125D;
    private static final FuelChannelSprites RBMK_ELEMENT_TEXTURES = fuelChannelSprites("rbmk_element");
    private static final FuelChannelSprites RBMK_ELEMENT_MOD_TEXTURES = fuelChannelSprites("rbmk_element_mod");
    private static final FuelChannelSprites RBMK_ELEMENT_REASIM_TEXTURES = fuelChannelSprites("rbmk_element_reasim");
    private static final FuelChannelSprites RBMK_ELEMENT_REASIM_MOD_TEXTURES =
            fuelChannelSprites("rbmk_element_reasim_mod");
    private static final PipePadSprites RBMK_BOILER_PIPE_TEXTURES = pipePadSprites("rbmk_boiler");
    private static final PipePadSprites RBMK_HEATER_PIPE_TEXTURES = pipePadSprites("rbmk_heater");
    private static final PipePadSprites RBMK_CONTROL_PIPE_TEXTURES = pipePadSprites("rbmk_control");
    private static final PipePadSprites RBMK_CONTROL_MOD_PIPE_TEXTURES = pipePadSprites("rbmk_control_mod");
    private static final PipePadSprites RBMK_CONTROL_AUTO_PIPE_TEXTURES = pipePadSprites("rbmk_control_auto");
    private static final PipePadSprites RBMK_CONTROL_REASIM_PIPE_TEXTURES = pipePadSprites("rbmk_control_reasim");
    private static final PipePadSprites RBMK_CONTROL_REASIM_AUTO_PIPE_TEXTURES =
            pipePadSprites("rbmk_control_reasim_auto");
    private static volatile ColumnRenderArrays columnRenderArrays =
            new ColumnRenderArrays(-1, new boolean[0], new int[0]);

    private final BlockRenderDispatcher blockRenderer;

    public RBMKColumnRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(RBMKColumnBlockEntity column, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(column, getViewDistance())) {
            return;
        }
        BlockState state = column.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(column, packedLight);
        if (state.getBlock() instanceof RBMKColumnBlock block && block.kind().rod()) {
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(column)) {
                renderStaticSegment(blockRenderer, state, 0, columnHeightAbove(), poseStack, buffer, modelLight);
            }
        } else {
            renderStaticSegment(blockRenderer, state, 0, columnHeightAbove(), poseStack, buffer, modelLight);
        }
        if (!hasDynamicSegments(column.kind())) {
            return;
        }
        if (column.getLevel() != null
                && !MultiblockHelper.isOperationalCoreLayoutComplete(column.getLevel(), column.getBlockPos())) {
            return;
        }
        renderDynamicSegment(column, 0, partialTick, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKColumnBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(RBMKColumnBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    public static void renderStaticSegment(BlockRenderDispatcher blockRenderer, BlockState state, int segment,
            int heightAbove, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!state.hasProperty(RBMKColumnBlock.LID)) {
            HbmClientRenderUtil.renderBlockModel(blockRenderer, state, poseStack, buffer, packedLight);
            return;
        }
        if (!(state.getBlock() instanceof RBMKColumnBlock block)) {
            HbmClientRenderUtil.renderBlockModel(blockRenderer, state, poseStack, buffer, packedLight);
            return;
        }
        BlockState baseState = state.setValue(RBMKColumnBlock.LID, RBMKColumnBlock.LidType.NONE);
        if (block.kind().rod()) {
            renderFuelChannelStaticSegment(block.kind(), state, segment == heightAbove,
                    poseStack, buffer, packedLight);
            return;
        }
        // Legacy RenderBlocks culls the horizontal faces shared by adjacent
        // column sections.  The modern column uses independent BER calls, so
        // explicitly keep only the bottom of the core and the top of the cap.
        HbmClientRenderUtil.renderBlockModelFaces(blockRenderer, baseState, poseStack, buffer, packedLight,
                segment == 0, segment == heightAbove);
        if (segment == heightAbove) {
            if (state.getValue(RBMKColumnBlock.LID).hasLid() && !block.kind().control()) {
                renderColumnLidSlab(block, state.getValue(RBMKColumnBlock.LID), poseStack, buffer, packedLight);
            }
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
        // The legacy TESRs render the complete moving assembly from the core tile.
        // Rendering a piece from every dummy makes the independently sampled light and
        // transparent Cherenkov seam overlap at each one-block boundary.
        if (segmentIndex != 0 || !hasDynamicSegments(column.kind()) || !column.hasOperationalLayout()) {
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
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (column.hasFuelRod()) {
            int color = plan.rodRgb() == 0 ? DEFAULT_FUEL_COLOR : plan.rodRgb();
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(column)) {
                ObjRbmkModels.renderFuelChannelRods(color, plan.columnOffset(), poseStack, buffer,
                        packedLight, packedOverlay);
            }
        }
        if (plan.cherenkov()) {
            int frozenColumnOffset = plan.columnOffset();
            LegacyMachineEffectPresenter.enqueueUntexturedQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                    buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE,
                    group -> renderCherenkovColumn(group, frozenColumnOffset));
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
        ResourceLocation texture = controlTexture(kind.automatic(), color);

        poseStack.pushPose();
        poseStack.translate(0.5D, plan.lidWorldY(), 0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(column)) {
            ObjRbmkModels.renderControlRodPart(plan.part(), texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderCherenkovColumn(UntexturedQuadGroup group, int columnOffset) {
        double step = ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_STEP;
        if (step <= 0.0D) {
            return;
        }
        for (double y = ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_START_Y;
                y <= columnOffset + 1.0E-6D; y += step) {
            group.add(
                    -0.5D, y, -0.5D,
                    -0.5D, y, 0.5D,
                    0.5D, y, 0.5D,
                    0.5D, y, -0.5D,
                    ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_COLOR,
                    ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_ALPHA,
                    ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_ALPHA,
                    ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_ALPHA,
                    ObjRbmkModels.FUEL_CHANNEL_CHERENKOV_ALPHA);
        }
    }

    private static void renderFuelChannelStaticSegment(RBMKColumnBlock.Kind kind, BlockState state,
            boolean topSegment, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        FuelChannelSprites sprites = fuelChannelTextures(kind);
        renderFuelChannelSideShell(sprites.side(), poseStack, buffer, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        ObjRbmkModels.ELEMENT.renderPartWithSprite("Cap", sprites.top(), poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, 0.0F, 0.0F, 0.0F, 255, 255, 255, 255, true);
        ObjRbmkModels.ELEMENT.renderPartWithSprite("Inner", sprites.inner(), poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, 0.0F, 0.0F, 0.0F, 255, 255, 255, 255, true);
        poseStack.popPose();

        if (topSegment && state.getValue(RBMKColumnBlock.LID).hasLid()) {
            renderFuelChannelLidSlab(sprites, state.getValue(RBMKColumnBlock.LID),
                    poseStack, buffer, packedLight);
        }
    }

    private static void renderFuelChannelSideShell(TextureAtlasSprite side, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        LegacyTexturedQuadRenderer.SpriteQuadBatch batch = LegacyTexturedQuadRenderer.spriteQuadBatch(poseStack,
                buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
        LegacyTexturedQuadRenderer.spritePixelQuadDirect(side, batch, packedLight,
                OverlayTexture.NO_OVERLAY, 0.0F, 0.0F, 1.0F,
                1.0D, 1.0D, 1.0D, 16.0D, 0.0D,
                0.0D, 1.0D, 1.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 1.0D, 0.0D, 16.0D,
                1.0D, 0.0D, 1.0D, 16.0D, 16.0D,
                0xFFFFFF, 255);
        LegacyTexturedQuadRenderer.spritePixelQuadDirect(side, batch, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 0.0F, 0.0F,
                1.0D, 1.0D, 1.0D, 16.0D, 0.0D,
                1.0D, 1.0D, 0.0D, 0.0D, 0.0D,
                1.0D, 0.0D, 0.0D, 0.0D, 16.0D,
                1.0D, 0.0D, 1.0D, 16.0D, 16.0D,
                0xFFFFFF, 255);
        LegacyTexturedQuadRenderer.spritePixelQuadDirect(side, batch, packedLight,
                OverlayTexture.NO_OVERLAY, 0.0F, 0.0F, -1.0F,
                1.0D, 1.0D, 0.0D, 16.0D, 0.0D,
                0.0D, 1.0D, 0.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 0.0D, 0.0D, 16.0D,
                1.0D, 0.0D, 0.0D, 16.0D, 16.0D,
                0xFFFFFF, 255);
        LegacyTexturedQuadRenderer.spritePixelQuadDirect(side, batch, packedLight,
                OverlayTexture.NO_OVERLAY, -1.0F, 0.0F, 0.0F,
                0.0D, 1.0D, 0.0D, 16.0D, 0.0D,
                0.0D, 1.0D, 1.0D, 0.0D, 0.0D,
                0.0D, 0.0D, 1.0D, 0.0D, 16.0D,
                0.0D, 0.0D, 0.0D, 16.0D, 16.0D,
                0xFFFFFF, 255);
    }

    public static boolean hasDynamicSegments(RBMKColumnBlock.Kind kind) {
        return kind != null && (kind.rod() || kind.control());
    }

    private static void renderFuelChannelLidSlab(FuelChannelSprites sprites, RBMKColumnBlock.LidType lid,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        TextureAtlasSprite top = lid == RBMKColumnBlock.LidType.GLASS ? sprites.glassTop() : sprites.coverTop();
        TextureAtlasSprite side = lid == RBMKColumnBlock.LidType.GLASS ? sprites.glassSide() : sprites.coverSide();
        // RenderRBMKRod renders this slab at y + 1.  RenderBlocks suppresses
        // its bottom face against the opaque top section and emits only the
        // top plus four sides.  Submitting a full cuboid creates a coplanar
        // bottom/top pair; culling cannot safely repair that face ownership.
        LegacyAtlasCuboidRenderer.croppedTopAndSides(top, side, side, side, side,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, 255,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0D, 1.0D, 0.0D, 1.0D, 1.25D, 1.0D);
    }

    private static void renderColumnLidSlab(RBMKColumnBlock block, RBMKColumnBlock.LidType lid,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        String prefix = BuiltInRegistries.BLOCK.getKey(block).getPath();
        String material = prefix + (lid == RBMKColumnBlock.LidType.GLASS ? "_glass" : "_cover");
        TextureAtlasSprite top = blockSprite(material + "_top");
        TextureAtlasSprite side = blockSprite(material + "_side");
        LegacyAtlasCuboidRenderer.croppedTopAndSides(top, side, side, side, side,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, 255,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0D, 1.0D, 0.0D, 1.0D, 1.25D, 1.0D);
    }

    private static FuelChannelSprites fuelChannelTextures(RBMKColumnBlock.Kind kind) {
        return switch (kind) {
            case ROD_MOD -> RBMK_ELEMENT_MOD_TEXTURES;
            case ROD_REASIM -> RBMK_ELEMENT_REASIM_TEXTURES;
            case ROD_REASIM_MOD -> RBMK_ELEMENT_REASIM_MOD_TEXTURES;
            default -> RBMK_ELEMENT_TEXTURES;
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
        PipePadSprites sprites = pipePadTextures(kind);
        if (sprites == null) {
            return;
        }
        // RenderBlocks drew these bounds in the air block above the column. Keep
        // the same local Y range so cropped side UVs select pixels 14..16 instead
        // of treating 1..1.125 as out-of-block bounds and sampling the transparent
        // remainder of the legacy icon.
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        LegacyTexturedQuadRenderer.SpriteQuadBatch batch = LegacyTexturedQuadRenderer.spriteQuadBatch(poseStack,
                buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
        renderPipePad(sprites.top(), sprites.side(), batch, packedLight,
                PIPE_PAD_MIN_LOW, PIPE_PAD_MIN_LOW, PIPE_PAD_MAX_LOW, PIPE_PAD_MAX_LOW);
        renderPipePad(sprites.top(), sprites.side(), batch, packedLight,
                PIPE_PAD_MIN_LOW, PIPE_PAD_MIN_HIGH, PIPE_PAD_MAX_LOW, PIPE_PAD_MAX_HIGH);
        renderPipePad(sprites.top(), sprites.side(), batch, packedLight,
                PIPE_PAD_MIN_HIGH, PIPE_PAD_MIN_HIGH, PIPE_PAD_MAX_HIGH, PIPE_PAD_MAX_HIGH);
        renderPipePad(sprites.top(), sprites.side(), batch, packedLight,
                PIPE_PAD_MIN_HIGH, PIPE_PAD_MIN_LOW, PIPE_PAD_MAX_HIGH, PIPE_PAD_MAX_LOW);
        poseStack.popPose();
    }

    private static void renderPipePad(TextureAtlasSprite top, TextureAtlasSprite side,
            LegacyTexturedQuadRenderer.SpriteQuadBatch batch, int packedLight,
            double minX, double minZ, double maxX, double maxZ) {
        // RenderRBMKControl uses renderStandardBlock at the air block above
        // the column.  Its opaque neighbour below removes the bottom face.
        LegacyAtlasCuboidRenderer.croppedTopAndSides(top, side, side, side, side,
                batch, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, 255,
                minX, PIPE_PAD_MIN_Y, minZ, maxX, PIPE_PAD_MAX_Y, maxZ);
    }

    private static TextureAtlasSprite blockSprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID, "block/rbmk/icons/" + name);
    }

    private static PipePadSprites pipePadTextures(RBMKColumnBlock.Kind kind) {
        return switch (kind) {
            case BOILER -> RBMK_BOILER_PIPE_TEXTURES;
            case HEATER -> RBMK_HEATER_PIPE_TEXTURES;
            case CONTROL -> RBMK_CONTROL_PIPE_TEXTURES;
            case CONTROL_MOD -> RBMK_CONTROL_MOD_PIPE_TEXTURES;
            case CONTROL_AUTO -> RBMK_CONTROL_AUTO_PIPE_TEXTURES;
            case CONTROL_REASIM -> RBMK_CONTROL_REASIM_PIPE_TEXTURES;
            case CONTROL_REASIM_AUTO -> RBMK_CONTROL_REASIM_AUTO_PIPE_TEXTURES;
            default -> null;
        };
    }

    private static FuelChannelSprites fuelChannelSprites(String prefix) {
        return new FuelChannelSprites(
                blockSprite(prefix + "_side"),
                blockSprite(prefix + "_top"),
                blockSprite(prefix + "_inner"),
                blockSprite(prefix + "_cover_top"),
                blockSprite(prefix + "_cover_side"),
                blockSprite(prefix + "_glass_top"),
                blockSprite(prefix + "_glass_side"));
    }

    private static PipePadSprites pipePadSprites(String prefix) {
        return new PipePadSprites(blockSprite(prefix + "_pipe_top"), blockSprite(prefix + "_pipe_side"));
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

    private record FuelChannelSprites(TextureAtlasSprite side, TextureAtlasSprite top, TextureAtlasSprite inner,
            TextureAtlasSprite coverTop, TextureAtlasSprite coverSide, TextureAtlasSprite glassTop,
            TextureAtlasSprite glassSide) {
    }

    private record PipePadSprites(TextureAtlasSprite top, TextureAtlasSprite side) {
    }
}
