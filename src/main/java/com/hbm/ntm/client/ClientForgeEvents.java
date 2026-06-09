package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.client.overlay.LegacyLookOverlayRenderer;
import com.hbm.ntm.client.overlay.ToolAbilityHudRenderer;
import com.hbm.ntm.client.render.HbmBlackHoleEffects;
import com.hbm.ntm.client.render.HbmOverheadMarkers;
import com.hbm.ntm.client.render.HbmRenderEffects;
import com.hbm.ntm.client.render.LegacyMultiblockHighlightRenderer;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.damage.DamageResistanceTooltipUtil;
import com.hbm.ntm.entity.effect.BlackHoleEntity;
import com.hbm.ntm.client.renderer.NukeTorexRenderer;
import com.hbm.ntm.entity.effect.QuasarEntity;
import com.hbm.ntm.entity.effect.RagingVortexEntity;
import com.hbm.ntm.entity.effect.VortexEntity;
import com.hbm.ntm.entity.effect.NukeTorexEntity;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.network.packet.EntitySyncPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ArmorRegistry;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.HazardTooltipUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static boolean hadLevel;
    private static boolean pushedNukeHudShake;
    private static float renderSoot;
    private static final int NOTICE_EMPTY_GAS_MASK_FILTER = 1;
    private static final int NOTICE_EMPTY_GAS_MASK_FILTER_MILLIS = 1_500;
    private static final Map<Integer, Long> VANISHED_ENTITIES = new HashMap<>();

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        HazardTooltipUtil.addHazardInformation(event.getItemStack(), event.getToolTip());
        DamageResistanceTooltipUtil.addResistanceInformation(event.getItemStack(), event.getToolTip());
        addHazmatProtectionInformation(event.getItemStack(), event.getToolTip());
    }

    private static void addHazmatProtectionInformation(ItemStack stack, List<Component> tooltip) {
        List<HazardClass> protections = ArmorRegistry.hazardClasses.get(stack.getItem());
        if (protections == null || protections.isEmpty()) {
            return;
        }

        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.protection.hold_shift",
                    Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        tooltip.add(Component.translatable("hazard.prot").withStyle(ChatFormatting.GOLD));
        for (HazardClass hazardClass : protections) {
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable(hazardClass.translationKey()))
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    @SubscribeEvent
    public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientHbmPlayerProperties.shouldRenderHud()) {
            popNukeHudShake(event.getGuiGraphics());
            return;
        }

        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && NukeHudEffects.hasFlash()) {
            renderNukeFlash(event);
        }

        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id()) && NukeHudEffects.hasShake()) {
            event.getGuiGraphics().pose().pushPose();
            pushedNukeHudShake = true;
            NukeHudEffects.translateShake(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            LegacyLookOverlayRenderer.render(event);
            if (ClientHbmPlayerProperties.shouldRenderHud()) {
                ToolAbilityHudRenderer.render(event);
            }
            return;
        }
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }
        if (!ClientHbmPlayerProperties.shouldRenderHud()) {
            return;
        }
        if (RadiationHud.hasGeigerCounter(player)) {
            RadiationHud.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
        DashHud.render(event.getGuiGraphics(), event.getWindow().getGuiScaledHeight());
        ClientInformMessages.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }

    @SubscribeEvent
    public static void onGuiPost(RenderGuiEvent.Post event) {
        popNukeHudShake(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            HbmRenderEffects.render(event);
            HbmOverheadMarkers.render(event);
            return;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            renderNukeTorexCloudlets(minecraft, event);
        }

        if (HbmBlackHoleEffects.isRenderStage(event.getStage())) {
            updateBlackHoleShaders(event.getPartialTick());
            HbmBlackHoleEffects.render(event);
        }
    }

    @SubscribeEvent
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        LegacyMultiblockHighlightRenderer.render(event);
    }

    private static void renderNukeTorexCloudlets(Minecraft minecraft, RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        List<NukeTorexEntity> clouds = new ArrayList<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof NukeTorexEntity torex && !torex.cloudlets.isEmpty()) {
                clouds.add(torex);
            }
        }
        if (clouds.isEmpty()) {
            return;
        }

        clouds.sort(Comparator.comparingDouble(
                cloud -> -event.getCamera().getPosition().distanceToSqr(cloud.getX(), cloud.getY(), cloud.getZ())));

        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        PoseStack worldViewPoseStack = createWorldViewPoseStack(event.getCamera());
        for (NukeTorexEntity cloud : clouds) {
            EntityRenderer<? super NukeTorexEntity> renderer = minecraft.getEntityRenderDispatcher().getRenderer(cloud);
            if (renderer instanceof NukeTorexRenderer torexRenderer) {
                torexRenderer.renderCloudletsAfterLevel(cloud, event.getCamera(), event.getPartialTick(),
                        worldViewPoseStack, buffer);
            }
        }
    }

    private static PoseStack createWorldViewPoseStack(net.minecraft.client.Camera camera) {
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        return poseStack;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientHbmPlayerProperties.registerListener();
        LegacyHbmAnimations.tick();
        HbmClientKeybinds.tick();
        ClientMuzzleFlashEffects.tick();
        HbmRenderEffects.tick();
        HbmOverheadMarkers.tick();
        HbmBlackHoleEffects.tick();
        updateSootFog();
        showEmptyGasMaskFilterWarning();
        pruneNetworkTransfers();
        pruneVanishedEntities();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            if (hadLevel) {
                clearNetworkState();
                hadLevel = false;
            }
            return;
        }
        hadLevel = true;
        spawnRadiationAura(minecraft);
    }

    private static void showEmptyGasMaskFilterWarning() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && ArmorUtil.isWearingEmptyMask(minecraft.player)) {
            ClientInformMessages.show(Component.translatable("info.gasmask.no_filter").withStyle(ChatFormatting.RED),
                    NOTICE_EMPTY_GAS_MASK_FILTER, NOTICE_EMPTY_GAS_MASK_FILTER_MILLIS);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float soot = visibleSoot();
        if (soot <= 0.0F) {
            return;
        }

        float farPlaneDistance = Minecraft.getInstance().options.renderDistance().get() * 16.0F;
        float fogDistance = farPlaneDistance
                / (1.0F + soot * 5.0F / RadiationConfig.pollutionSootFogDivisor());
        event.setNearPlaneDistance(0.0F);
        event.setFarPlaneDistance(fogDistance);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float soot = visibleSoot();
        if (soot <= 0.0F) {
            return;
        }

        float interpolation = Math.min(soot / RadiationConfig.pollutionSootFogDivisor(), 1.0F);
        float sootColor = 0.15F;
        event.setRed(Mth.lerp(interpolation, event.getRed(), sootColor));
        event.setGreen(Mth.lerp(interpolation, event.getGreen(), sootColor));
        event.setBlue(Mth.lerp(interpolation, event.getBlue(), sootColor));
    }

    private static void spawnRadiationAura(Minecraft minecraft) {
        if (minecraft.player == null) {
            return;
        }
        float radiation = ClientHbmLivingProperties.getRadiation();
        if (radiation > 600.0F) {
            ParticleUtil.spawnRadiationAura(minecraft.level, radiation > 900.0F ? 4 : radiation > 800.0F ? 2 : 1);
        }
    }

    private static void pruneNetworkTransfers() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.getGameTime() % 100L != 0L) {
            return;
        }
        ClientBinaryData.pruneExpired(minecraft.level.getGameTime());
        ClientTileBinaryData.pruneExpired(minecraft.level.getGameTime());
    }

    private static void updateBlackHoleShaders(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof VortexEntity vortex) {
                float size = Math.max(0.05F, vortex.getSize());
                int lifetime = Math.max(1, (int) Math.ceil(size / Math.max(0.0001F, vortex.shrinkRate())) + 2);
                float intensity = Mth.clamp(size / 1.5F, 0.05F, 1.0F) * 1.35F;
                Vec3 pos = vortex.getPosition(partialTick);
                HbmBlackHoleEffects.updateTrackedBlackHole(vortex.getId(), pos.x, pos.y, pos.z,
                        HbmBlackHoleEffects.BlackHoleSpec.of(size, lifetime)
                                .withFade(0.0F, Math.max(1, lifetime - 20))
                                .withAccretionDiskDensity(0.01F)
                                .withTiltAngle((float) Math.toRadians(vortex.getId() % 90 - 45))
                                .withIntensity(intensity)
                                .withRenderQuality(1.6F, 0.45F)
                                .withLensBoundarySoftness(0.6F)
                                .withDiskDetail(1.0F, 0.35F)
                                .withDiskColor(0.45F, 0.85F, 1.0F)
                                .withDiskRamp(0.85F, 1.35F, 1.6F, 0.05F, 0.45F, 1.0F),
                        0);
            } else if (entity instanceof RagingVortexEntity ragingVortex) {
                float size = Math.max(0.05F, ragingVortex.getSize());
                Vec3 pos = ragingVortex.getPosition(partialTick);
                HbmBlackHoleEffects.updateTrackedBlackHole(ragingVortex.getId(), pos.x, pos.y, pos.z,
                        HbmBlackHoleEffects.BlackHoleSpec.of(size, 20 * 60)
                                .withFade(0.0F, 20 * 60 - 20)
                                .withAccretionDiskDensity(0.01F)
                                .withTiltAngle((float) Math.toRadians(ragingVortex.getId() % 90 - 45))
                                .withIntensity(1.45F)
                                .withRenderQuality(1.45F, 0.55F)
                                .withLensBoundarySoftness(0.6F)
                                .withDiskDetail(1.0F, 0.35F)
                                .withDiskColor(0.82F, 0.68F, 1.0F)
                                .withDiskRamp(1.55F, 1.25F, 2.0F, 0.45F, 0.18F, 0.95F),
                        0);
            } else if (entity instanceof QuasarEntity quasar) {
                float size = Math.max(0.05F, quasar.getSize());
                Vec3 pos = quasar.getPosition(partialTick);
                HbmBlackHoleEffects.updateTrackedBlackHole(quasar.getId(), pos.x, pos.y, pos.z,
                        HbmBlackHoleEffects.BlackHoleSpec.of(size, 20 * 60)
                                .withFade(0.0F, 20 * 60 - 20)
                                .withAccretionDiskDensity(0.01F)
                                .withTiltAngle((float) Math.toRadians(quasar.getId() % 90 - 45))
                                .withIntensity(1.35F)
                                .withRenderQuality(1.35F, 0.7F)
                                .withLensBoundarySoftness(0.6F)
                                .withDiskDetail(1.0F, 0.35F)
                                .withDiskColor(1.0F, 0.08F, 0.05F)
                                .withDiskRamp(1.8F, 0.1F, 0.05F, 0.45F, 0.0F, 0.0F),
                        0);
            } else if (entity instanceof BlackHoleEntity blackHole) {
                float size = Math.max(0.05F, blackHole.getSize());
                Vec3 pos = blackHole.getPosition(partialTick);
                HbmBlackHoleEffects.updateTrackedBlackHole(blackHole.getId(), pos.x, pos.y, pos.z,
                        HbmBlackHoleEffects.BlackHoleSpec.of(size, 20 * 60)
                                .withFade(0.0F, 20 * 60 - 20)
                                .withAccretionDiskDensity(0.01F)
                                .withTiltAngle((float) Math.toRadians(blackHole.getId() % 90 - 45))
                                .withIntensity(1.2F)
                                .withRenderQuality(1.35F, 0.7F)
                                .withLensBoundarySoftness(0.6F)
                                .withDiskDetail(1.0F, 0.35F)
                                .withDiskColor(1.0F, 0.99F, 0.9F)
                                .withDiskRamp(2.0F, 1.95F, 1.68F, 1.0F, 0.88F, 0.46F),
                        0);
            }
        }
    }

    private static void clearNetworkState() {
        ClientBinaryData.clearAll();
        ClientTileBinaryData.clearAll();
        ClientBiomeSyncData.clearAll();
        ClientPermaSyncData.clearAll();
        ClientPollutionData.clearAll();
        ClientTomImpactData.clearAll();
        ClientHbmPlayerProperties.clearAll();
        ClientHbmLivingProperties.clearAll();
        ClientPanelData.clearAll();
        ClientInformMessages.clearAll();
        ClientMuzzleFlashEffects.clearAll();
        HbmRenderEffects.clearAll();
        HbmOverheadMarkers.clearAll();
        HbmBlackHoleEffects.clearAll();
        NukeHudEffects.clearAll();
        TileSyncPacket.clearClientResyncRequests();
        ClientTileBinaryData.clearClientResyncRequests();
        EntitySyncPacket.clearClientResyncRequests();
        LegacyHbmAnimations.clearAll();
        VANISHED_ENTITIES.clear();
        renderSoot = 0.0F;
    }

    private static void updateSootFog() {
        if (!RadiationConfig.pollutionEnabled() || !RadiationConfig.pollutionSootFogEnabled()) {
            renderSoot = 0.0F;
            return;
        }

        float target = ClientPollutionData.getSoot();
        float step = 0.05F;
        if (Math.abs(renderSoot - target) < step) {
            renderSoot = target;
        } else if (renderSoot < target) {
            renderSoot += step;
        } else {
            renderSoot -= step;
        }
    }

    private static float visibleSoot() {
        if (!RadiationConfig.pollutionEnabled() || !RadiationConfig.pollutionSootFogEnabled()) {
            return 0.0F;
        }
        return Math.max(0.0F, renderSoot - RadiationConfig.pollutionSootFogThreshold());
    }

    private static void renderNukeFlash(RenderGuiOverlayEvent.Pre event) {
        GuiGraphics graphics = event.getGuiGraphics();
        boolean restoreShake = pushedNukeHudShake;
        if (restoreShake) {
            graphics.pose().popPose();
            pushedNukeHudShake = false;
        }
        NukeHudEffects.renderFlash(graphics, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        if (restoreShake) {
            graphics.pose().pushPose();
            pushedNukeHudShake = true;
            NukeHudEffects.translateShake(graphics);
        }
    }

    private static void popNukeHudShake(GuiGraphics graphics) {
        if (!pushedNukeHudShake) {
            return;
        }
        graphics.pose().popPose();
        pushedNukeHudShake = false;
    }

    public static void vanishEntity(int entityId) {
        vanishEntity(entityId, 2_000);
    }

    public static void vanishEntity(int entityId, int durationMillis) {
        if (entityId <= 0 || durationMillis <= 0) {
            return;
        }
        VANISHED_ENTITIES.put(entityId, System.currentTimeMillis() + durationMillis);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (isVanished(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static boolean isVanished(LivingEntity entity) {
        Long until = VANISHED_ENTITIES.get(entity.getId());
        return until != null && until > System.currentTimeMillis();
    }

    private static void pruneVanishedEntities() {
        if (VANISHED_ENTITIES.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, Long>> iterator = VANISHED_ENTITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= now) {
                iterator.remove();
            }
        }
    }

    private ClientForgeEvents() {
    }
}
