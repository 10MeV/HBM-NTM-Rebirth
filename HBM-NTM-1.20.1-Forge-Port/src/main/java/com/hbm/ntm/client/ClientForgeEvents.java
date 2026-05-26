package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.network.packet.EntitySyncPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.radiation.HazardTooltipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static boolean hadLevel;
    private static boolean pushedNukeHudShake;
    private static final Map<Integer, Long> VANISHED_ENTITIES = new HashMap<>();

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        HazardTooltipUtil.addHazardInformation(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            pushedNukeHudShake = false;
            return;
        }

        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && NukeHudEffects.hasFlash()) {
            NukeHudEffects.renderFlash(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(),
                    event.getWindow().getGuiScaledHeight());
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
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }
        if (player == null || minecraft.options.hideGui) {
            return;
        }
        if (RadiationHud.hasGeigerCounter(player)) {
            RadiationHud.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
        ClientInformMessages.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
    }

    @SubscribeEvent
    public static void onGuiPost(RenderGuiEvent.Post event) {
        popNukeHudShake(event);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LegacyHbmAnimations.tick();
        HbmClientKeybinds.tick();
        ClientMuzzleFlashEffects.tick();
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
    }

    private static void pruneNetworkTransfers() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.getGameTime() % 100L != 0L) {
            return;
        }
        ClientBinaryData.pruneExpired(minecraft.level.getGameTime());
        ClientTileBinaryData.pruneExpired(minecraft.level.getGameTime());
    }

    private static void clearNetworkState() {
        ClientBinaryData.clearAll();
        ClientTileBinaryData.clearAll();
        ClientBiomeSyncData.clearAll();
        ClientPermaSyncData.clearAll();
        ClientPlayerSyncData.clearAll();
        ClientRadiationData.clearAll();
        ClientPanelData.clearAll();
        ClientInformMessages.clearAll();
        ClientMuzzleFlashEffects.clearAll();
        NukeHudEffects.clearAll();
        TileSyncPacket.clearClientResyncRequests();
        ClientTileBinaryData.clearClientResyncRequests();
        EntitySyncPacket.clearClientResyncRequests();
        LegacyHbmAnimations.clearAll();
        VANISHED_ENTITIES.clear();
    }

    private static void popNukeHudShake(RenderGuiEvent event) {
        if (!pushedNukeHudShake) {
            return;
        }
        event.getGuiGraphics().pose().popPose();
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
