package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.CapacitorBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.CapacitorBlockEntity;
import com.hbm.ntm.client.obj.LegacyCapacitorObjRenderer;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CapacitorRenderer implements BlockEntityRenderer<CapacitorBlockEntity> {
    private static final float HALF_PI = (float) Math.PI * 0.5F;
    private static final float PI = (float) Math.PI;
    private static final float THREE_HALF_PI = PI * 1.5F;

    private static final CapacitorTextures COPPER_TEXTURES = textures("copper");
    private static final CapacitorTextures GOLD_TEXTURES = textures("gold");
    private static final CapacitorTextures NIOBIUM_TEXTURES = textures("niobium");
    private static final CapacitorTextures TANTALIUM_TEXTURES = textures("tantalium");
    private static final CapacitorTextures SCHRABIDATE_TEXTURES = textures("schrabidate");

    public CapacitorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(CapacitorBlockEntity capacitor, Vec3 cameraPos) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                && BlockEntityRenderer.super.shouldRender(capacitor, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(capacitor, getViewDistance());
    }

    @Override
    public void render(CapacitorBlockEntity capacitor, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(capacitor, getViewDistance())) {
            return;
        }
        BlockState state = capacitor.getBlockState();
        Direction facing = state.hasProperty(CapacitorBlock.FACING)
                ? state.getValue(CapacitorBlock.FACING)
                : Direction.UP;
        float yawRadians = yawRadiansFor(facing);
        float pitchRadians = pitchRadiansFor(facing);
        CapacitorTextures textures = texturesFor(capacitor.legacyTextureName());
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(capacitor, packedLight);

        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(capacitor);
                LegacyRenderLighting.ModelViewSamplingScope ignored =
                        LegacyRenderLighting.pushModelViewSampling(capacitor, poseStack.last().pose())) {
            LegacyCapacitorObjRenderer.render(
                    textures.top(),
                    textures.side(),
                    textures.bottom(),
                    textures.innerTop(),
                    textures.innerSide(),
                    poseStack, buffer, modelLight, packedOverlay, yawRadians, pitchRadians);
        }
    }

    private static CapacitorTextures texturesFor(String legacyName) {
        return switch (legacyName) {
            case "gold" -> GOLD_TEXTURES;
            case "niobium" -> NIOBIUM_TEXTURES;
            case "tantalium" -> TANTALIUM_TEXTURES;
            case "schrabidate" -> SCHRABIDATE_TEXTURES;
            default -> COPPER_TEXTURES;
        };
    }

    private static CapacitorTextures textures(String legacyName) {
        return new CapacitorTextures(
                texture(legacyName, "top"),
                texture(legacyName, "side"),
                texture(legacyName, "bottom"),
                texture(legacyName, "inner_top"),
                texture(legacyName, "inner_side"));
    }

    private static ResourceLocation texture(String legacyName, String part) {
        return ObjBlockModels.texture("capacitor_" + legacyName + "_" + part);
    }

    private static float yawRadiansFor(Direction facing) {
        return switch (facing) {
            case NORTH -> HALF_PI;
            case SOUTH -> THREE_HALF_PI;
            case WEST -> PI;
            default -> 0.0F;
        };
    }

    private static float pitchRadiansFor(Direction facing) {
        return switch (facing) {
            case DOWN -> PI;
            case NORTH, SOUTH, WEST, EAST -> HALF_PI;
            default -> 0.0F;
        };
    }

    private record CapacitorTextures(ResourceLocation top, ResourceLocation side, ResourceLocation bottom,
            ResourceLocation innerTop, ResourceLocation innerSide) {
    }
}
