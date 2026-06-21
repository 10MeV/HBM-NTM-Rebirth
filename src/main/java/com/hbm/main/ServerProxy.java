package com.hbm.main;

import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import com.hbm.sound.AudioWrapper;
import com.hbm.util.i18n.I18nServer;
import com.hbm.util.i18n.ITranslate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy proxy facade. Client-only registration/rendering methods are no-ops;
 * shared runtime reads delegate to modern systems.
 */
@Deprecated(forRemoval = false)
public class ServerProxy {
    private static final I18nServer I18N = new I18nServer();

    public static final int ID_DUCK = 0;
    public static final int ID_FILTER = 1;
    public static final int ID_COMPASS = 2;
    public static final int ID_CABLE = 3;
    public static final int ID_DRONE = 4;
    public static final int ID_JETPACK = 5;
    public static final int ID_MAGNET = 6;
    public static final int ID_HUD = 7;
    public static final int ID_DETONATOR = 8;
    public static final int ID_FLUID_ID = 9;
    public static final int ID_FAN_MODE = 10;
    public static final int ID_TOOLABILITY = 11;
    public static final int ID_GAS_HAZARD = 12;
    public static final int ID_WRENCH = 13;
    public static final int ID_PAGER_DYN = 1000;

    public ITranslate getI18n() {
        return I18N;
    }

    public void registerPreRenderInfo() {
    }

    public void registerRenderInfo() {
    }

    public void registerTileEntitySpecialRenderer() {
    }

    public void registerItemRenderer() {
    }

    public void registerEntityRenderer() {
    }

    public void registerBlockRenderer() {
    }

    public void registerGunCfg() {
    }

    public void handleNHNEICompat() {
    }

    public void spawnParticle(double x, double y, double z, String type, float[] args) {
    }

    public void effectNT(CompoundTag data) {
    }

    public void registerMissileItems() {
    }

    @Deprecated
    public AudioWrapper getLoopedSound(String sound, float x, float y, float z, float volume, float range, float pitch) {
        return getLoopedSound(sound, x, y, z, volume, range, pitch, 0);
    }

    public AudioWrapper getLoopedSound(String sound, float x, float y, float z, float volume, float range, float pitch, int keepAlive) {
        AudioWrapper audio = new AudioWrapper();
        audio.updatePosition(x, y, z);
        audio.updateVolume(volume);
        audio.updateRange(range);
        audio.updatePitch(pitch);
        audio.setKeepAlive(keepAlive);
        return audio;
    }

    public void playSound(String sound, Object data) {
    }

    public void displayTooltip(String msg, int id) {
        displayTooltip(msg, 1000, id);
    }

    public void displayTooltip(String msg, int time, int id) {
    }

    public boolean getIsKeyPressed(EnumKeybind key) {
        return false;
    }

    public Player me() {
        return null;
    }

    public boolean isVanished(Entity entity) {
        return false;
    }

    public void openLink(String url) {
    }

    public List<ItemStack> getSubItems(ItemStack stack) {
        List<ItemStack> list = new ArrayList<>();
        if (stack != null) {
            list.add(stack.copy());
        }
        return list;
    }

    public float getImpactDust(Level level) {
        return impactData(level).dust();
    }

    public float getImpactFire(Level level) {
        return impactData(level).fire();
    }

    public boolean getImpact(Level level) {
        return impactData(level).impact();
    }

    public void playSoundClient(double x, double y, double z, String sound, float volume, float pitch) {
    }

    public String getLanguageCode() {
        return "en_us";
    }

    public int getStackColor(ItemStack stack, boolean amplify) {
        return 0x000000;
    }

    private TomImpactSavedData.Snapshot impactData(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return TomImpactSavedData.getData(serverLevel).snapshot();
        }
        return new TomImpactSavedData.Snapshot(0.0F, 0.0F, false);
    }
}
