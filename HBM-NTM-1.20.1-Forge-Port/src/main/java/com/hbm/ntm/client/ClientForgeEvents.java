package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.radiation.HazardTooltipUtil;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private static int geigerTickTimer;

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
        Player player = minecraft.player;
        if (player == null || !RadiationHud.hasGeigerCounter(player)) {
            return;
        }

        geigerTickTimer++;
        if (geigerTickTimer < 5) {
            return;
        }
        geigerTickTimer = 0;

        float rate = ClientRadiationData.getEnvironment();
        List<Integer> candidates = new ArrayList<>();
        if (rate < 1.0F) candidates.add(0);
        if (rate < 5.0F) candidates.add(0);
        if (rate < 10.0F) candidates.add(1);
        if (rate > 5.0F && rate < 15.0F) candidates.add(2);
        if (rate > 10.0F && rate < 20.0F) candidates.add(3);
        if (rate > 15.0F && rate < 25.0F) candidates.add(4);
        if (rate > 20.0F && rate < 30.0F) candidates.add(5);
        if (rate > 25.0F) candidates.add(6);

        int sound = candidates.isEmpty() ? 0 : candidates.get(player.getRandom().nextInt(candidates.size()));
        if (sound == 0 && player.getRandom().nextInt(50) != 0) {
            return;
        }
        if (sound <= 0) {
            sound = 1;
        }
        player.playSound(ModSounds.geiger(sound), 1.0F, 1.0F);
    }

    private static void pruneNetworkTransfers() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.getGameTime() % 100L != 0L) {
            return;
        }
        ClientBinaryData.pruneExpired(minecraft.level.getGameTime());
        ClientTileBinaryData.pruneExpired(minecraft.level.getGameTime());
    }

    private ClientForgeEvents() {
    }
}
