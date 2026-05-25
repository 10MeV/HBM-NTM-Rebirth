package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.radiation.HazardTooltipUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static boolean hadLevel;

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        HazardTooltipUtil.addHazardInformation(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || !event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }
        if (RadiationHud.hasGeigerCounter(player)) {
            RadiationHud.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
        }
        ClientInformMessages.render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
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
        ClientPanelData.clearAll();
        ClientInformMessages.clearAll();
        ClientMuzzleFlashEffects.clearAll();
        LegacyHbmAnimations.clearAll();
    }

    private ClientForgeEvents() {
    }
}
