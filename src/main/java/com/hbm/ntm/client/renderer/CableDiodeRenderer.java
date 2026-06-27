package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CableDiodeBlock;
import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
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
    private static final TextureAtlasSprite PLATE_SPRITE = sprite("cable_diode");
    private static final TextureAtlasSprite PAD_SPRITE = sprite("hadron_coil_alloy");
    private static final double SLAB_THICKNESS = 0.125D;
    private static final double PAD_RADIUS = 0.375D;

    public CableDiodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CableDiodeBlockEntity diode, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = diode.getBlockState();
        int light = LegacyRenderLighting.resolveMultiblockLight(diode, packedLight);

        renderBody(poseStack, buffer, light, packedOverlay, outputDirection(state));
        renderWorldCableArms(diode.getLevel(), diode.getBlockPos(), state, poseStack, buffer, light, packedOverlay);
    }

    public static void renderItem(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderBody(poseStack, buffer, packedLight, packedOverlay, Direction.UP);
        renderCableArms(poseStack, buffer, packedLight, packedOverlay, true, true, false, true, true, true);
    }

    private static void renderBody(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, Direction output) {
        LegacyAtlasCuboidRenderer.directionalSlab(PLATE_SPRITE, poseStack, buffer, packedLight, packedOverlay,
                output, SLAB_THICKNESS);
        LegacyAtlasCuboidRenderer.centeredCube(PAD_SPRITE, poseStack, buffer, packedLight, packedOverlay,
                PAD_RADIUS);
    }

    private static void renderWorldCableArms(BlockGetter level, BlockPos pos, BlockState state,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean posX = connects(level, pos, state, Direction.EAST);
        boolean negX = connects(level, pos, state, Direction.WEST);
        boolean posY = connects(level, pos, state, Direction.UP);
        boolean negY = connects(level, pos, state, Direction.DOWN);
        boolean posZ = connects(level, pos, state, Direction.SOUTH);
        boolean negZ = connects(level, pos, state, Direction.NORTH);
        renderCableArms(poseStack, buffer, packedLight, packedOverlay, posX, negX, posY, negY, posZ, negZ);
    }

    private static boolean connects(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        return state.getBlock() instanceof HbmEnergyConnectorBlock connector
                && HbmEnergyConnectionUtil.canConnect(level, pos, connector, direction);
    }

    private static void renderCableArms(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, boolean posX, boolean negX, boolean posY, boolean negY, boolean posZ, boolean negZ) {
        RedCableRenderer.renderCableArms(poseStack, buffer, packedLight, packedOverlay,
                posX, negX, posY, negY, posZ, negZ);
    }

    private static Direction outputDirection(BlockState state) {
        Direction facing = state.hasProperty(CableDiodeBlock.FACING)
                ? state.getValue(CableDiodeBlock.FACING)
                : Direction.NORTH;
        return facing.getOpposite();
    }

    private static TextureAtlasSprite sprite(String texture) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + texture));
    }
}
