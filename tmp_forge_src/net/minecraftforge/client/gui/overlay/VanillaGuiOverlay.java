/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

/**
 * All the vanilla {@linkplain IGuiOverlay HUD overlays} in the order that they render.
 */
public enum VanillaGuiOverlay
{
    VIGNETTE("vignette", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (Minecraft.m_91405_())
        {
            gui.setupOverlayRenderState(true, false);
            gui.m_280154_(guiGraphics, gui.getMinecraft().m_91288_());
        }
    }),
    SPYGLASS("spyglass", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        gui.setupOverlayRenderState(true, false);
        gui.renderSpyglassOverlay(guiGraphics);
    }),
    HELMET("helmet", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        gui.setupOverlayRenderState(true, false);
        gui.renderHelmet(partialTick, guiGraphics);
    }),
    FROSTBITE("frostbite", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        gui.setupOverlayRenderState(true, false);
        gui.renderFrostbite(guiGraphics);
    }),
    PORTAL("portal", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        float f1 = Mth.m_14179_(partialTick, gui.getMinecraft().f_91074_.f_108590_, gui.getMinecraft().f_91074_.f_108589_);
        if (f1 > 0.0F && !gui.getMinecraft().f_91074_.m_21023_(MobEffects.f_19604_)) {
            gui.setupOverlayRenderState(true, false);
            gui.m_280379_(guiGraphics, f1);
        }
    }),
    HOTBAR("hotbar", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.setupOverlayRenderState(true, false);
            if (gui.getMinecraft().f_91072_.m_105295_() == GameType.SPECTATOR)
            {
                gui.m_93085_().m_280623_(guiGraphics);
            }
            else
            {
                gui.m_280518_(partialTick, guiGraphics);
            }
        }
    }),
    CROSSHAIR("crosshair", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.setupOverlayRenderState(true, false);

            guiGraphics.m_280168_().m_85836_();
            guiGraphics.m_280168_().m_252880_(0, 0, -90);
            gui.m_280130_(guiGraphics);
            guiGraphics.m_280168_().m_85849_();
        }
    }),
    BOSS_EVENT_PROGRESS("boss_event_progress", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.setupOverlayRenderState(true, false);

            guiGraphics.m_280168_().m_85836_();
            guiGraphics.m_280168_().m_252880_(0, 0, -90);
            gui.renderBossHealth(guiGraphics);
            guiGraphics.m_280168_().m_85849_();
        }
    }),
    PLAYER_HEALTH("player_health", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_ && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);
            gui.renderHealth(screenWidth, screenHeight, guiGraphics);
        }
    }),
    ARMOR_LEVEL("armor_level", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_ && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);
            gui.renderArmor(guiGraphics, screenWidth, screenHeight);
        }
    }),
    FOOD_LEVEL("food_level", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Entity vehicle = gui.getMinecraft().f_91074_.m_20202_();
        boolean isMounted = vehicle != null && vehicle.m_20152_();
        if (!isMounted && !gui.getMinecraft().f_91066_.f_92062_ && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);
            gui.renderFood(screenWidth, screenHeight, guiGraphics);
        }
    }),
    AIR_LEVEL("air_level", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_ && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);
            gui.renderAir(screenWidth, screenHeight, guiGraphics);
        }
    }),
    MOUNT_HEALTH("mount_health", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_ && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);
            gui.renderHealthMount(screenWidth, screenHeight, guiGraphics);
        }
    }),
    JUMP_BAR("jump_bar", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        PlayerRideableJumping playerRideableJumping = gui.getMinecraft().f_91074_.m_245714_();
        if (playerRideableJumping != null && !gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.setupOverlayRenderState(true, false);
            gui.m_280069_(playerRideableJumping, guiGraphics, screenWidth / 2 - 91);
        }
    }),
    EXPERIENCE_BAR("experience_bar", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (gui.getMinecraft().f_91074_.m_245714_() == null && !gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.setupOverlayRenderState(true, false);
            gui.renderExperience(screenWidth / 2 - 91, guiGraphics);
        }
    }),
    ITEM_NAME("item_name", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.setupOverlayRenderState(true, false);
            if (gui.getMinecraft().f_91072_.m_105295_() != GameType.SPECTATOR)
            {
                gui.m_280295_(guiGraphics);
            }
            else if (gui.getMinecraft().f_91074_.m_5833_())
            {
                gui.m_93085_().m_280365_(guiGraphics);
            }
        }
    }),
    SLEEP_FADE("sleep_fade", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        gui.renderSleepFade(screenWidth, screenHeight, guiGraphics);
    }),
    POTION_ICONS("potion_icons", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        gui.m_280523_(guiGraphics);
    }),
    DEBUG_TEXT("debug_text", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        gui.renderHUDText(screenWidth, screenHeight, guiGraphics);
    }),
    FPS_GRAPH("fps_graph", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        gui.renderFPSGraph(guiGraphics);
    }),
    RECORD_OVERLAY("record_overlay", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.renderRecordOverlay(screenWidth, screenHeight, partialTick, guiGraphics);
        }
    }),
    TITLE_TEXT("title_text", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.renderTitle(screenWidth, screenHeight, partialTick, guiGraphics);
        }
    }),
    SUBTITLES("subtitles", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!gui.getMinecraft().f_91066_.f_92062_)
        {
            gui.renderSubtitles(guiGraphics);
        }
    }),
    SCOREBOARD("scoreboard", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

        Scoreboard scoreboard = gui.getMinecraft().f_91073_.m_6188_();
        Objective objective = null;
        PlayerTeam scoreplayerteam = scoreboard.m_83500_(gui.getMinecraft().f_91074_.m_6302_());
        if (scoreplayerteam != null)
        {
            int slot = scoreplayerteam.m_7414_().m_126656_();
            if (slot >= 0) objective = scoreboard.m_83416_(3 + slot);
        }
        Objective scoreobjective1 = objective != null ? objective : scoreboard.m_83416_(1);
        if (scoreobjective1 != null)
        {
            gui.m_280030_(guiGraphics, scoreobjective1);
        }
    }),
    CHAT_PANEL("chat_panel", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        gui.renderChat(screenWidth, screenHeight, guiGraphics);
    }),
    PLAYER_LIST("player_list", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        gui.renderPlayerList(screenWidth, screenHeight, guiGraphics);
    });

    private final ResourceLocation id;
    final IGuiOverlay overlay;
    NamedGuiOverlay type;

    VanillaGuiOverlay(String id, IGuiOverlay overlay)
    {
        this.id = new ResourceLocation("minecraft", id);
        this.overlay = overlay;
    }

    @NotNull
    public ResourceLocation id()
    {
        return id;
    }

    public NamedGuiOverlay type()
    {
        return type;
    }
}
