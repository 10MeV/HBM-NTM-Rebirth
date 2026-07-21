package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyRawLightmapCoordinates;
import com.hbm.ntm.client.obj.ObjSoyuzModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/** Source-backed client-only Soyuz easter egg from HTTPHandler#loadSoyuz. */
@OnlyIn(Dist.CLIENT)
public final class SoyuzHologramRenderer {
    private static final URL CAPSULE_URL = url("https://gist.githubusercontent.com/HbmMods/"
            + "a1cad71d00b6915945a43961d0037a43/raw/soyuz_holo");
    private static final LegacyRawLightmapCoordinates HOLOGRAM_LIGHTMAP =
            LegacyRawLightmapCoordinates.of(6500, 30);
    private static final double TARGET_X = 0.0D;
    private static final double TARGET_Y = 500.0D;
    private static final double TARGET_Z = 0.0D;
    private static final double MAX_DISTANCE_SQUARED = 262144.0D;
    private static final long STARTUP_TIME = System.currentTimeMillis();
    private static final AtomicBoolean LOAD_STARTED = new AtomicBoolean();
    private static final PoseStack POSE = new PoseStack();
    private static volatile List<String> capsuleLines = List.of();

    private SoyuzHologramRenderer() {
    }

    public static void beginAsyncLoad() {
        if (!LOAD_STARTED.compareAndSet(false, true)) {
            return;
        }
        CompletableFuture.runAsync(SoyuzHologramRenderer::loadCapsuleLines);
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER || capsuleLines.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.level == null || player == null || !minecraft.level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        float partialTick = event.getPartialTick();
        double playerX = Mth.lerp(partialTick, player.xo, player.getX());
        double playerY = Mth.lerp(partialTick, player.yo, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zo, player.getZ());
        double deltaX = TARGET_X - playerX;
        double deltaY = TARGET_Y - playerY;
        double deltaZ = TARGET_Z - playerZ;
        if (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ >= MAX_DISTANCE_SQUARED) {
            return;
        }

        PoseStack poseStack = POSE;
        poseStack.setIdentity();
        // Legacy RenderWorldLast translated by target minus interpolated player,
        // not the third-person camera; keep that source contract exactly.
        poseStack.translate(deltaX, deltaY, deltaZ);
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, 80.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 30.0F);
        double time = System.currentTimeMillis() * 0.0005D;
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (Math.sin(time) * 5.0D));
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (Math.sin(time + Math.PI * 0.5D) * 5.0D));
        poseStack.translate(0.0D, -3.0D, 0.0D);
        ObjSoyuzModels.renderModuleRawLightmap(poseStack, buffer, HOLOGRAM_LIGHTMAP, OverlayTexture.NO_OVERLAY);

        LegacyPoseRotations.rotateYDegrees(poseStack, (float) -(System.currentTimeMillis() * 0.025D % 360.0D));
        String message = capsuleLines.get(new Random(STARTUP_TIME).nextInt(capsuleLines.size()));
        renderMessageRing(minecraft.font, buffer, poseStack, message);
        poseStack.popPose();
    }

    private static void renderMessageRing(Font font, MultiBufferSource.BufferSource buffer, PoseStack poseStack,
            String message) {
        if (message.isEmpty()) {
            return;
        }
        float fullWidth = font.width(message);
        if (fullWidth <= 0.0F) {
            return;
        }
        float scale = 5.0F / fullWidth;
        float rotation = 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.75D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
        for (int index = 0; index < message.length(); index++) {
            String glyph = message.substring(index, index + 1);
            poseStack.pushPose();
            LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
            rotation -= font.width(glyph) * scale * 50.0F;
            poseStack.translate(2.0D, 0.0D, 0.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
            poseStack.scale(scale, scale, scale);
            font.drawInBatch(Component.literal(glyph).getVisualOrderText(), 0.0F, 0.0F, 0xFFFF00FF, false,
                    poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void loadCapsuleLines() {
        List<String> loaded = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(CAPSULE_URL.openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                loaded.add(line);
            }
            capsuleLines = List.copyOf(loaded);
        } catch (IOException exception) {
            HbmNtm.LOGGER.warn("Soyuz hologram text load failed", exception);
        }
    }

    private static URL url(String value) {
        try {
            return new URL(value);
        } catch (IOException exception) {
            throw new IllegalStateException("Invalid source-backed Soyuz hologram URL", exception);
        }
    }
}
