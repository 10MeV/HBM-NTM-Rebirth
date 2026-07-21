package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.DroneWaypointBlock;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Direct modern equivalent of the DroneWaypoint/DroneWaypointRequest branch in
 * legacy RenderRTTY: the shared rtty OBJ is textured with each waypoint's block icon.
 */
public class DroneWaypointRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final LegacyWavefrontModel MODEL = ObjBlockModels.RTTY.asVBO();

    /**
     * RenderRTTY queried the current block icon every render.  Keep the resource name rather
     * than an atlas sprite instance so a resource reload cannot leave these waypoint BERs
     * pointing at a pre-reload sprite after {@link LegacyTexturedQuadRenderer#clearSpriteCache()}.
     */
    private final String spriteName;

    public DroneWaypointRenderer(BlockEntityRendererProvider.Context context, boolean requestWaypoint) {
        spriteName = requestWaypoint ? "drone_waypoint_request" : "drone_waypoint";
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(T waypoint, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(waypoint, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(waypoint, getViewDistance());
    }

    @Override
    public void render(T waypoint, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(waypoint, getViewDistance())) {
            return;
        }
        BlockState state = waypoint.getBlockState();
        Direction facing = state.hasProperty(DroneWaypointBlock.FACING)
                ? state.getValue(DroneWaypointBlock.FACING)
                : Direction.UP;
        RadioTorchRenderer.Rotation rotation = RadioTorchRenderer.legacyRotation(facing);
        int light = LegacyRenderLighting.resolveMultiblockLight(waypoint, packedLight);
        TextureAtlasSprite sprite = sprite(spriteName);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(waypoint)) {
            MODEL.renderWithSprite(sprite, poseStack, buffer, light, packedOverlay, rotation.yaw(), rotation.pitch(),
                    0.0F, false);
        }
        poseStack.popPose();
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID, "block/" + name);
    }
}
