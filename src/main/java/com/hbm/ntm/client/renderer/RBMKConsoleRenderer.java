package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RBMKConsoleBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.RBMKConsoleBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.hbm.ntm.neutron.RBMKWorldRenderPlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RBMKConsoleRenderer implements BlockEntityRenderer<RBMKConsoleBlockEntity> {
    public RBMKConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRender(RBMKConsoleBlockEntity blockEntity, Vec3 cameraPos) {
        return (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer() || hasDynamicOverlay(blockEntity))
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(RBMKConsoleBlockEntity console, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean renderStaticBody = LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
        boolean renderDynamicOverlay = hasDynamicOverlay(console);
        if (!renderStaticBody && !renderDynamicOverlay) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(console, getViewDistance())) {
            return;
        }
        BlockState state = console.getBlockState();
        Direction facing = state.hasProperty(RBMKConsoleBlock.FACING)
                ? state.getValue(RBMKConsoleBlock.FACING)
                : Direction.SOUTH;
        int light = LegacyRenderLighting.resolveMultiblockLight(console, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, legacyYaw(facing));
        poseStack.translate(0.5D, 0.0D, 0.0D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(console)) {
            if (renderStaticBody) {
                ObjRbmkModels.CONSOLE.renderAll(ObjRbmkModels.CONSOLE_TEXTURE, poseStack, buffer, light, packedOverlay,
                        LegacyTexturedRenderMode.CUTOUT_CULL);
            }
            if (renderDynamicOverlay) {
                renderDynamicOverlay(console, poseStack, buffer);
            }
        }
        poseStack.popPose();
    }

    private static void renderDynamicOverlay(RBMKConsoleBlockEntity console, PoseStack poseStack,
            MultiBufferSource buffer) {
        RBMKConsolePlanner.ColumnSnapshot[] columns = console.columns();
        List<RBMKWorldRenderPlanner.ConsoleColumnInput> inputs = new ArrayList<>(columns.length);
        for (RBMKConsolePlanner.ColumnSnapshot column : columns) {
            inputs.add(columnInput(column));
        }

        LegacyUntexturedQuadRenderer.DirectQuadBatch batch =
                LegacyUntexturedQuadRenderer.directQuadBatch(poseStack, buffer,
                        LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        for (RBMKWorldRenderPlanner.ConsoleColumnPlan plan : RBMKWorldRenderPlanner.consoleColumnPlans(inputs)) {
            RBMKConsolePlanner.ColumnSnapshot column = columns[plan.index()];
            LegacyRbmkDisplayRenderer.renderColumn(batch, plan.index(), column,
                    plan.x(), plan.y(), plan.z());
        }

        Font font = Minecraft.getInstance().font;
        RBMKConsolePlanner.ScreenState[] screens = console.screens();
        List<String> rawDisplays = new ArrayList<>(screens.length);
        for (RBMKConsolePlanner.ScreenState screen : screens) {
            rawDisplays.add(screen == null || screen.display() == null ? "" : screen.display());
        }
        List<RBMKWorldRenderPlanner.ConsoleScreenTextPlan> rawPlans =
                RBMKWorldRenderPlanner.consoleScreenPlans(rawDisplays, List.of());
        List<Integer> widths = new ArrayList<>(rawPlans.size());
        for (RBMKWorldRenderPlanner.ConsoleScreenTextPlan plan : rawPlans) {
            widths.add(font.width(resolveScreenText(plan)));
        }
        for (RBMKWorldRenderPlanner.ConsoleScreenTextPlan plan :
                RBMKWorldRenderPlanner.consoleScreenPlans(rawDisplays, widths)) {
            if (!plan.visible()) {
                continue;
            }
            String text = resolveScreenText(plan);
            poseStack.pushPose();
            poseStack.translate(plan.x(), plan.y(), plan.z());
            poseStack.scale(plan.scale(), -plan.scale(), plan.scale());
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            font.drawInBatch(text, -font.width(text) * 0.5F, -font.lineHeight * 0.5F, plan.color(), false,
                    poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
    }

    private static RBMKWorldRenderPlanner.ConsoleColumnInput columnInput(
            RBMKConsolePlanner.ColumnSnapshot column) {
        if (column == null) {
            return null;
        }
        CompoundTag data = column.data() == null ? new CompoundTag() : column.data();
        return new RBMKWorldRenderPlanner.ConsoleColumnInput(column.type(),
                data.contains("color") ? data.getByte("color") : -1,
                data.getByte("indicator"), data.getDouble("heat"), data.getDouble("maxHeat"),
                data.getDouble("enrichment"), data.getDouble("level"));
    }

    private static String resolveScreenText(RBMKWorldRenderPlanner.ConsoleScreenTextPlan plan) {
        return plan.translationKey().isEmpty()
                ? plan.text()
                : Component.translatable(plan.translationKey(), plan.translationFallback()).getString();
    }

    private static boolean hasDynamicOverlay(RBMKConsoleBlockEntity console) {
        for (RBMKConsolePlanner.ColumnSnapshot column : console.columns()) {
            if (column != null) {
                return true;
            }
        }
        for (RBMKConsolePlanner.ScreenState screen : console.screens()) {
            if (screen != null && screen.display() != null && !screen.display().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKConsoleBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    private static float legacyYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }
}
