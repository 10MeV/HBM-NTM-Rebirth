package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CableDiodeBlock;
import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.energy.HbmEnergyConnectorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CableDiodeRenderer implements BlockEntityRenderer<CableDiodeBlockEntity> {
    private static final ResourceLocation PLATE_SPRITE =
            new ResourceLocation(HbmNtm.MOD_ID, "block/cable_diode");
    private static final ResourceLocation PAD_SPRITE =
            new ResourceLocation(HbmNtm.MOD_ID, "block/hadron_coil_alloy");
    private static final double SLAB_THICKNESS = 0.125D;
    private static final double PAD_RADIUS = 0.375D;

    public CableDiodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableDiodeBlockEntity diode, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = diode.getBlockState();
        int light = LegacyRenderLighting.resolveBlockEntityLight(diode, packedLight);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);

        renderBody(state, context, outputDirection(state));
        renderWorldCableArms(diode.getLevel(), diode.getBlockPos(), state, context);
    }

    public static void renderItem(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        renderBody(state, context, Direction.UP);
        renderCableArms(context, true, true, false, true, true, true);
    }

    private static void renderBody(BlockState state, ObjRenderContext context, Direction output) {
        TextureAtlasSprite plate = LegacyTexturedQuadRenderer.blockSprite(PLATE_SPRITE);
        TextureAtlasSprite pad = LegacyTexturedQuadRenderer.blockSprite(PAD_SPRITE);
        LegacyAtlasCuboidRenderer.directionalSlab(plate, context, output, SLAB_THICKNESS);
        LegacyAtlasCuboidRenderer.centeredCube(pad, context, PAD_RADIUS);
    }

    private static void renderWorldCableArms(BlockGetter level, BlockPos pos, BlockState state, ObjRenderContext context) {
        boolean posX = connects(level, pos, state, Direction.EAST);
        boolean negX = connects(level, pos, state, Direction.WEST);
        boolean posY = connects(level, pos, state, Direction.UP);
        boolean negY = connects(level, pos, state, Direction.DOWN);
        boolean posZ = connects(level, pos, state, Direction.SOUTH);
        boolean negZ = connects(level, pos, state, Direction.NORTH);
        renderCableArms(context, posX, negX, posY, negY, posZ, negZ);
    }

    private static boolean connects(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        return state.getBlock() instanceof HbmEnergyConnectorBlock connector
                && HbmEnergyConnectionUtil.canConnect(level, pos, connector, direction);
    }

    private static void renderCableArms(ObjRenderContext context,
            boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (posX) {
            ObjBlockModels.CABLE_NEO.renderPart("posX", RedCableRenderer.CABLE_TEXTURE, context);
        }
        if (negX) {
            ObjBlockModels.CABLE_NEO.renderPart("negX", RedCableRenderer.CABLE_TEXTURE, context);
        }
        if (posY) {
            ObjBlockModels.CABLE_NEO.renderPart("posY", RedCableRenderer.CABLE_TEXTURE, context);
        }
        if (negY) {
            ObjBlockModels.CABLE_NEO.renderPart("negY", RedCableRenderer.CABLE_TEXTURE, context);
        }
        if (negZ) {
            ObjBlockModels.CABLE_NEO.renderPart("posZ", RedCableRenderer.CABLE_TEXTURE, context);
        }
        if (posZ) {
            ObjBlockModels.CABLE_NEO.renderPart("negZ", RedCableRenderer.CABLE_TEXTURE, context);
        }
        poseStack.popPose();
    }

    private static Direction outputDirection(BlockState state) {
        Direction facing = state.hasProperty(CableDiodeBlock.FACING)
                ? state.getValue(CableDiodeBlock.FACING)
                : Direction.NORTH;
        return facing.getOpposite();
    }
}
