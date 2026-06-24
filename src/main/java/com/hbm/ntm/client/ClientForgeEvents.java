package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItem;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.client.overlay.LegacyHelmetOverlayRenderer;
import com.hbm.ntm.client.overlay.LegacyHevHudRenderer;
import com.hbm.ntm.client.overlay.LegacyLookOverlayRenderer;
import com.hbm.ntm.client.obj.ObjArmorModels;
import com.hbm.ntm.client.overlay.ToolAbilityHudRenderer;
import com.hbm.ntm.client.particle.HbmDeferredParticleRenderer;
import com.hbm.ntm.client.render.HbmBlackHoleEffects;
import com.hbm.ntm.client.render.HbmOverheadMarkers;
import com.hbm.ntm.client.render.HbmRenderEffects;
import com.hbm.ntm.client.render.LegacyMultiblockHighlightRenderer;
import com.hbm.ntm.client.renderer.LegacyAccessoryRenderHelper;
import com.hbm.ntm.client.renderer.LegacyHeadArmorRenderer;
import com.hbm.ntm.client.renderer.LegacyJetpackRenderer;
import com.hbm.ntm.client.renderer.LegacyObjArmorRenderer;
import com.hbm.ntm.client.renderer.LegacyScreenQuadRenderer;
import com.hbm.ntm.client.renderer.NukeTorexRenderer;
import com.hbm.ntm.client.screen.ArmorTableScreen;
import com.hbm.ntm.client.renderer.SednaGunHudRenderer;
import com.hbm.ntm.client.renderer.SednaGunItemRenderer;
import com.hbm.ntm.client.sound.LegacyMovingEntitySound;
import com.hbm.ntm.client.sound.LegacyNullSoundRedirects;
import com.hbm.ntm.client.sound.SoundLoopSiren;
import com.hbm.ntm.blockentity.CustomNukeBlockEntity;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.damage.DamageResistanceTooltipUtil;
import com.hbm.ntm.entity.effect.BlackHoleEntity;
import com.hbm.ntm.entity.effect.QuasarEntity;
import com.hbm.ntm.entity.effect.RagingVortexEntity;
import com.hbm.ntm.entity.effect.VortexEntity;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.StingerGunItem;
import com.hbm.ntm.network.packet.EntitySyncPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ArmorRegistry;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.CraterBiomeUtil;
import com.hbm.ntm.radiation.HazardTooltipUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static final ResourceLocation OVERLAY_MISC_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/overlay_misc.png");
    private static boolean hadLevel;
    private static boolean pushedNukeHudShake;
    private static float renderSoot;
    private static final int NOTICE_EMPTY_GAS_MASK_FILTER = 1;
    private static final int NOTICE_EMPTY_GAS_MASK_FILTER_MILLIS = 1_500;
    private static final Map<Integer, Long> VANISHED_ENTITIES = new HashMap<>();
    private static final Map<Integer, LegacyObjArmorRenderer.PartVisibilityState> LEGACY_PART_VISIBILITY = new HashMap<>();

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        HazardTooltipUtil.addHazardInformation(event.getItemStack(), event.getToolTip(), event.getEntity());
        DamageResistanceTooltipUtil.addResistanceInformation(event.getItemStack(), event.getToolTip());
        addHazmatProtectionInformation(event.getItemStack(), event.getToolTip());
        addCustomNukeInformation(event.getItemStack(), event.getToolTip());
        addInstalledArmorModInformation(event);
        addItemTagInformation(event);
    }

    private static void addInstalledArmorModInformation(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ArmorItem) || !ArmorModHandler.hasMods(stack)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!Screen.hasShiftDown() && !(minecraft.screen instanceof ArmorTableScreen)) {
            event.getToolTip().add(Component.literal("Hold <")
                    .append(Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                    .append(Component.literal("> to display installed armor mods"))
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        event.getToolTip().add(Component.literal("Mods:").withStyle(ChatFormatting.YELLOW));
        ItemStack[] mods = ArmorModHandler.pryMods(stack);
        for (int i = 0; i < ArmorModHandler.battery; i++) {
            ItemStack mod = mods[i];
            if (mod.getItem() instanceof ArmorModItem armorMod) {
                armorMod.appendInstalledArmorModTooltip(mod, stack, event.getToolTip(), event.getFlags());
            }
        }
    }

    private static void addItemTagInformation(ItemTooltipEvent event) {
        if (!event.getFlags().isAdvanced() || !HbmClientConfig.itemTagTooltips()) {
            return;
        }
        List<ResourceLocation> tags = event.getItemStack().getTags()
                .map(tag -> tag.location())
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
        if (tags.isEmpty()) {
            return;
        }
        event.getToolTip().add(Component.literal("Item Tags:").withStyle(ChatFormatting.BLUE));
        for (ResourceLocation tag : tags) {
            event.getToolTip().add(Component.literal(" -" + tag).withStyle(ChatFormatting.AQUA));
        }
    }

    private static void addCustomNukeInformation(ItemStack stack, List<Component> tooltip) {
        if (!HbmClientConfig.customNukeTooltips()) {
            return;
        }
        CustomNukeBlockEntity.CustomNukeTooltipEntry entry = CustomNukeBlockEntity.getTooltipEntry(stack);
        if (entry == null) {
            return;
        }
        if (!tooltip.isEmpty()) {
            tooltip.add(Component.empty());
        }
        String prefix = entry.multiplier() ? "Adds multiplier " : "Adds ";
        tooltip.add(Component.literal(prefix + entry.value() + " to the custom nuke stage " + entry.stage())
                .withStyle(ChatFormatting.GOLD));
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
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) {
            sound = event.getOriginalSound();
        }
        if (LegacyNullSoundRedirects.handle(sound)) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof SednaGunItem)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        event.setCanceled(true);
        HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        ItemDisplayContext context = arm == HumanoidArm.LEFT
                ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        SednaGunItemRenderer.INSTANCE.renderByItem(stack, context, poseStack, event.getMultiBufferSource(),
                event.getPackedLight(), OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (LegacyHevHudRenderer.handlePre(event)) {
            return;
        }
        if (!ClientHbmPlayerProperties.shouldRenderHud()) {
            popNukeHudShake(event.getGuiGraphics());
            return;
        }

        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && NukeHudEffects.hasFlash()) {
            renderNukeFlash(event);
        }

        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            renderSednaCrosshair(event, minecraft);
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
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HELMET.id())) {
            LegacyHelmetOverlayRenderer.render(event);
            return;
        }
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
        SednaGunHudRenderer.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight(), player);
        ArmorModuleHud.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(),
                event.getWindow().getGuiScaledHeight(), player);
        DashHud.render(event.getGuiGraphics(), event.getWindow().getGuiScaledHeight());
        ClientInformMessages.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }

    private static void renderSednaCrosshair(RenderGuiOverlayEvent.Pre event, Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null || !HbmClientConfig.customCrosshairsEnabled()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SednaGunItem gun)) {
            return;
        }
        event.setCanceled(true);
        if (gun instanceof StingerGunItem stinger) {
            if (!stinger.shouldRenderLegacyStingerCrosshair(stack)) {
                return;
            }
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            LegacyScreenQuadRenderer.renderCrosshair(OVERLAY_MISC_TEXTURE, event.getGuiGraphics(),
                    screenWidth, screenHeight, stinger.currentCrosshair(stack));
            LegacyScreenQuadRenderer.renderStingerLockon(OVERLAY_MISC_TEXTURE, event.getGuiGraphics(),
                    screenWidth, screenHeight, stinger.legacyStingerLockonProgress(stack));
            return;
        }
        if (gun.shouldHideCrosshair(stack)) {
            return;
        }
        LegacyScreenQuadRenderer.renderCrosshair(OVERLAY_MISC_TEXTURE, event.getGuiGraphics(),
                event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(),
                gun.currentCrosshair(stack));
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
            HbmDeferredParticleRenderer.renderAfterLevel(event.getCamera(), event.getPartialTick(),
                    minecraft.renderBuffers().bufferSource());
            PoseStack torexPoseStack = new PoseStack();
            torexPoseStack.mulPose(Axis.XP.rotationDegrees(event.getCamera().getXRot()));
            torexPoseStack.mulPose(Axis.YP.rotationDegrees(event.getCamera().getYRot() + 180.0F));
            NukeTorexRenderer.renderCloudletsAfterLevel(minecraft.level, event.getCamera(), event.getPartialTick(),
                    torexPoseStack, minecraft.renderBuffers().bufferSource());
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
            HbmDeferredParticleRenderer.clear();
            if (hadLevel) {
                clearNetworkState();
                hadLevel = false;
            }
            return;
        }
        hadLevel = true;
        tickClientArmorMods(minecraft.player);
        spawnRadiationAura(minecraft);
        spawnCraterTownAura(minecraft);
    }

    private static void tickClientArmorMods(Player player) {
        if (player == null) {
            return;
        }
        for (ItemStack armor : player.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorModItem armorMod) {
                    armorMod.onClientArmorModTick(player, armor, mod);
                }
            }
        }
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

    private static void spawnCraterTownAura(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }
        ResourceKey<Biome> biome = minecraft.level.getBiome(player.blockPosition()).unwrapKey().orElse(null);
        if (!CraterBiomeUtil.CRATER.equals(biome) && !CraterBiomeUtil.CRATER_INNER.equals(biome)) {
            return;
        }
        ParticleStatus particleStatus = minecraft.options.particles().get();
        if (particleStatus == ParticleStatus.MINIMAL) {
            return;
        }
        RandomSource random = player.getRandom();
        int count = particleStatus == ParticleStatus.ALL ? 3 : 1;
        for (int i = 0; i < count; i++) {
            ParticleUtil.spawnTownAura(minecraft.level,
                    player.getX() + random.nextGaussian() * 3.0D,
                    player.getY() + random.nextGaussian() * 2.0D,
                    player.getZ() + random.nextGaussian() * 3.0D);
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
        LegacyMovingEntitySound.clearAll();
        SoundLoopSiren.clearAll();
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
            return;
        }
        if (event.getRenderer().getModel() instanceof HumanoidModel<?> humanoid) {
            LEGACY_PART_VISIBILITY.put(event.getEntity().getId(),
                    LegacyObjArmorRenderer.hideLegacyPlayerParts(event.getEntity(), humanoid));
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LegacyObjArmorRenderer.PartVisibilityState visibility = LEGACY_PART_VISIBILITY.remove(event.getEntity().getId());
        try {
            if (event.getRenderer().getModel() instanceof HumanoidModel<?> humanoid) {
                LegacyObjArmorRenderer.renderEquippedArmor(event.getEntity(), humanoid, event.getPoseStack(),
                        event.getMultiBufferSource(), event.getPackedLight());
                LegacyHeadArmorRenderer.renderEquippedHeadArmor(event.getEntity(), humanoid, event.getPoseStack(),
                        event.getMultiBufferSource(), event.getPackedLight());
                if (event.getEntity() instanceof Player player) {
                    LegacyJetpackRenderer.renderEquippedJetpack(player, humanoid, event.getPoseStack(),
                            event.getMultiBufferSource(), event.getPackedLight());
                    renderBackTesla(player, humanoid, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
                    renderWings(player, humanoid, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
                }
            }
        } finally {
            LegacyObjArmorRenderer.restoreLegacyPlayerParts(visibility);
        }
    }

    private static void renderBackTesla(Player player, HumanoidModel<?> humanoid, PoseStack poseStack,
                                        MultiBufferSource buffer, int packedLight) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ArmorModHandler.hasMods(chestplate)) {
            return;
        }
        ItemStack mod = ArmorModHandler.pryMod(chestplate, ArmorModHandler.plate_only);
        if (!(mod.getItem() instanceof ArmorModItems.BackTesla)) {
            return;
        }

        poseStack.pushPose();
        humanoid.body.translateAndRotate(poseStack);
        poseStack.scale(LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE);
        ObjArmorModels.MOD_TESLA.renderAll(ObjArmorModels.MOD_TESLA_TEXTURE, poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderWings(Player player, HumanoidModel<?> humanoid, PoseStack poseStack,
                                    MultiBufferSource buffer, int packedLight) {
        ArmorModItems.Wings wings = directOrInstalledWings(player);
        if (wings == null) {
            return;
        }

        poseStack.pushPose();
        humanoid.body.translateAndRotate(poseStack);
        poseStack.scale(LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE);
        renderWingModel(player, poseStack, buffer, packedLight, wings.isMurky() ? 0 : 1);
        poseStack.popPose();
    }

    private static ArmorModItems.Wings directOrInstalledWings(Player player) {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.getItem() instanceof ArmorModItems.Wings wings) {
            return wings;
        }
        if (!ArmorModHandler.hasMods(chestplate)) {
            return null;
        }
        ItemStack mod = ArmorModHandler.pryMod(chestplate, ArmorModHandler.plate_only);
        return mod.getItem() instanceof ArmorModItems.Wings wings ? wings : null;
    }

    private static void renderWingModel(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                        int legacyMode) {
        double rot = Math.sin(player.tickCount * 0.2D) * 20.0D;
        double rot2 = Math.sin(player.tickCount * 0.2D - Math.PI * 0.5D) * 50.0D + 30.0D;
        if (legacyMode != 1 && player.onGround()) {
            rot = 20.0D;
            rot2 = 160.0D;
        }
        if (legacyMode == 1) {
            if (player.onGround()) {
                rot = 30.0D;
                rot2 = -30.0D;
            } else if (player.getDeltaMovement().y < -0.1D) {
                rot = 0.0D;
                rot2 = 10.0D;
            } else {
                rot = 30.0D;
                rot2 = 20.0D;
            }
        }

        poseStack.translate(0.0D, -2.0D, 0.0D);
        renderLeftWing(player, poseStack, buffer, packedLight, rot, rot2);
        renderRightWing(player, poseStack, buffer, packedLight, rot, rot2);
    }

    private static void renderLeftWing(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                       double rot, double rot2) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-10.0F));
        poseStack.translate(1.0D, 5.0D, 3.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (rot * 0.5D)));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (rot + 5.0D)));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.translate(-1.0D, -5.0D, -3.0D);
        poseStack.translate(1.0D, 5.0D, 3.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) rot));
        poseStack.translate(-1.0D, -5.0D, -3.0D);
        ObjArmorModels.renderPart(ObjArmorModels.WINGS, "LeftBase", ObjArmorModels.WINGS_MURK_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.translate(16.0D, 5.0D, 2.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) rot2));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (rot2 * 0.25D + 5.0D)));
        poseStack.translate(-16.0D, -5.0D, -2.0D);
        ObjArmorModels.renderPart(ObjArmorModels.WINGS, "LeftTip", ObjArmorModels.WINGS_MURK_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderRightWing(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                        double rot, double rot2) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(10.0F));
        poseStack.translate(-1.0D, 5.0D, 3.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (-rot * 0.5D)));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-rot - 5.0D)));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.translate(1.0D, -5.0D, -3.0D);
        poseStack.translate(-1.0D, 5.0D, 3.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) -rot));
        poseStack.translate(1.0D, -5.0D, -3.0D);
        ObjArmorModels.renderPart(ObjArmorModels.WINGS, "RightBase", ObjArmorModels.WINGS_MURK_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.translate(-16.0D, 5.0D, 2.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) -rot2));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-rot2 * 0.25D - 5.0D)));
        poseStack.translate(16.0D, -5.0D, -2.0D);
        ObjArmorModels.renderPart(ObjArmorModels.WINGS, "RightTip", ObjArmorModels.WINGS_MURK_TEXTURE,
                poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
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
