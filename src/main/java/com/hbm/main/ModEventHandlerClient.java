package com.hbm.main;

/**
 * Legacy client event facade. Modern HUD, overlay, sound, and particle hooks
 * are registered by the current client-side event classes.
 */
@Deprecated(forRemoval = false)
public class ModEventHandlerClient {
    public void onOverlayRender(Object event) {
    }

    public void onHUDRenderShield(Object event) {
    }

    public void onHUDRenderBar(Object event) {
    }

    public void setupFOV(Object event) {
    }

    public void preRenderEvent(Object event) {
    }

    public void onRenderArmorEvent(Object event) {
    }

    public void onPlaySound(Object event) {
    }

    public void drawTooltip(Object event) {
    }

    public void onRenderStorm(Object event) {
    }

    public void clientTick(Object event) {
    }
}
