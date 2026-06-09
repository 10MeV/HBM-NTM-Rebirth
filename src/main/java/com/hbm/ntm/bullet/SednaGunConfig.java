package com.hbm.ntm.bullet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SednaGunConfig(
        String legacyName,
        String sourceClassName,
        String itemClassName,
        WeaponQuality quality,
        List<GunModeConfig> configs,
        String notes) {

    public SednaGunConfig {
        legacyName = clean(legacyName);
        sourceClassName = clean(sourceClassName);
        itemClassName = clean(itemClassName);
        quality = quality == null ? WeaponQuality.A_SIDE : quality;
        configs = configs == null ? List.of() : List.copyOf(configs);
        notes = clean(notes);
    }

    public Optional<LegacySednaMagazineConfigs.DefaultAmmo> defaultAmmo() {
        return LegacySednaMagazineConfigs.defaultAmmo(legacyName);
    }

    public List<SednaMagazineConfig> magazines() {
        List<SednaMagazineConfig> magazines = new ArrayList<>();
        for (GunModeConfig config : configs) {
            for (SednaReceiverConfig receiver : config.receivers()) {
                receiver.magazine().ifPresent(magazines::add);
            }
        }
        return List.copyOf(magazines);
    }

    public List<String> missingMagazineKeys() {
        List<String> missing = new ArrayList<>();
        for (GunModeConfig config : configs) {
            for (SednaReceiverConfig receiver : config.receivers()) {
                if (receiver.magazineKey().isEmpty() || LegacySednaMagazineConfigs.byKey(receiver.magazineKey()).isEmpty()) {
                    missing.add(receiver.magazineKey());
                }
            }
        }
        return List.copyOf(missing);
    }

    public static Builder builder(String legacyName, String sourceClassName, String itemClassName,
            WeaponQuality quality) {
        return new Builder(legacyName, sourceClassName, itemClassName, quality);
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    public record GunModeConfig(
            int configIndex,
            float durability,
            int drawDuration,
            int inspectDuration,
            boolean inspectCancel,
            Crosshair crosshair,
            boolean hideCrosshair,
            boolean thermalSights,
            boolean reloadRequiresTypeChange,
            boolean reloadAnimationsSequential,
            String scopeTexture,
            String smokeHandlerName,
            String orchestraName,
            String pressPrimaryHandlerName,
            String pressSecondaryHandlerName,
            String pressTertiaryHandlerName,
            String pressReloadHandlerName,
            String releasePrimaryHandlerName,
            String releaseSecondaryHandlerName,
            String releaseTertiaryHandlerName,
            String releaseReloadHandlerName,
            String deciderName,
            String animationProfileName,
            List<String> hudComponentNames,
            List<SednaReceiverConfig> receivers,
            String notes) {

        public GunModeConfig {
            crosshair = crosshair == null ? Crosshair.NONE : crosshair;
            scopeTexture = clean(scopeTexture);
            smokeHandlerName = clean(smokeHandlerName);
            orchestraName = clean(orchestraName);
            pressPrimaryHandlerName = clean(pressPrimaryHandlerName);
            pressSecondaryHandlerName = clean(pressSecondaryHandlerName);
            pressTertiaryHandlerName = clean(pressTertiaryHandlerName);
            pressReloadHandlerName = clean(pressReloadHandlerName);
            releasePrimaryHandlerName = clean(releasePrimaryHandlerName);
            releaseSecondaryHandlerName = clean(releaseSecondaryHandlerName);
            releaseTertiaryHandlerName = clean(releaseTertiaryHandlerName);
            releaseReloadHandlerName = clean(releaseReloadHandlerName);
            deciderName = clean(deciderName);
            animationProfileName = clean(animationProfileName);
            hudComponentNames = hudComponentNames == null ? List.of() : List.copyOf(hudComponentNames);
            receivers = receivers == null ? List.of() : List.copyOf(receivers);
            notes = clean(notes);
        }

        public boolean usesStandardConfigurationHandlers() {
            return "Lego.LAMBDA_STANDARD_CLICK_PRIMARY".equals(pressPrimaryHandlerName)
                    && "Lego.LAMBDA_STANDARD_RELOAD".equals(pressReloadHandlerName)
                    && "Lego.LAMBDA_TOGGLE_AIM".equals(pressTertiaryHandlerName)
                    && "GunStateDecider.LAMBDA_STANDARD_DECIDER".equals(deciderName);
        }
    }

    public enum WeaponQuality {
        A_SIDE,
        B_SIDE,
        LEGENDARY,
        SPECIAL,
        UTILITY,
        SECRET,
        DEBUG
    }

    public enum GunState {
        DRAWING,
        IDLE,
        COOLDOWN,
        RELOADING,
        JAMMED
    }

    public enum Crosshair {
        NONE(0, 0, 0),
        CROSS(1, 55, 16),
        CIRCLE(19, 55, 16),
        SEMI(37, 55, 16),
        KRUCK(55, 55, 16),
        DUAL(1, 73, 16),
        SPLIT(19, 73, 16),
        CLASSIC(37, 73, 16),
        BOX(55, 73, 16),
        L_CROSS(0, 90, 32),
        L_KRUCK(32, 90, 32),
        L_CLASSIC(64, 90, 32),
        L_CIRCLE(96, 90, 32),
        L_SPLIT(0, 122, 32),
        L_ARROWS(32, 122, 32),
        L_BOX(64, 122, 32),
        L_CIRCUMFLEX(96, 122, 32),
        L_RAD(0, 154, 32),
        L_MODERN(32, 154, 32),
        L_BOX_OUTLINE(64, 154, 32);

        private final int atlasX;
        private final int atlasY;
        private final int size;

        Crosshair(int atlasX, int atlasY, int size) {
            this.atlasX = atlasX;
            this.atlasY = atlasY;
            this.size = size;
        }

        public int atlasX() {
            return atlasX;
        }

        public int atlasY() {
            return atlasY;
        }

        public int size() {
            return size;
        }
    }

    public static final class Builder {
        private final String legacyName;
        private final String sourceClassName;
        private final String itemClassName;
        private final WeaponQuality quality;
        private final List<GunModeConfig> configs = new ArrayList<>();
        private String notes = "";

        private Builder(String legacyName, String sourceClassName, String itemClassName, WeaponQuality quality) {
            this.legacyName = legacyName;
            this.sourceClassName = sourceClassName;
            this.itemClassName = itemClassName;
            this.quality = quality;
        }

        public Builder config(GunModeConfig config) {
            this.configs.add(config);
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public SednaGunConfig build() {
            return new SednaGunConfig(legacyName, sourceClassName, itemClassName, quality, configs, notes);
        }
    }

    public static final class ModeBuilder {
        private final int configIndex;
        private float durability;
        private int drawDuration;
        private int inspectDuration;
        private boolean inspectCancel = true;
        private Crosshair crosshair = Crosshair.NONE;
        private boolean hideCrosshair = true;
        private boolean thermalSights;
        private boolean reloadRequiresTypeChange;
        private boolean reloadAnimationsSequential;
        private String scopeTexture = "";
        private String smokeHandlerName = "";
        private String orchestraName = "";
        private String pressPrimaryHandlerName = "";
        private String pressSecondaryHandlerName = "";
        private String pressTertiaryHandlerName = "";
        private String pressReloadHandlerName = "";
        private String releasePrimaryHandlerName = "";
        private String releaseSecondaryHandlerName = "";
        private String releaseTertiaryHandlerName = "";
        private String releaseReloadHandlerName = "";
        private String deciderName = "";
        private String animationProfileName = "";
        private final List<String> hudComponentNames = new ArrayList<>();
        private final List<SednaReceiverConfig> receivers = new ArrayList<>();
        private String notes = "";

        public ModeBuilder(int configIndex) {
            this.configIndex = configIndex;
        }

        public ModeBuilder durability(float durability) {
            this.durability = durability;
            return this;
        }

        public ModeBuilder draw(int drawDuration) {
            this.drawDuration = drawDuration;
            return this;
        }

        public ModeBuilder inspect(int inspectDuration) {
            this.inspectDuration = inspectDuration;
            return this;
        }

        public ModeBuilder inspectCancel(boolean inspectCancel) {
            this.inspectCancel = inspectCancel;
            return this;
        }

        public ModeBuilder crosshair(Crosshair crosshair) {
            this.crosshair = crosshair;
            return this;
        }

        public ModeBuilder hideCrosshair(boolean hideCrosshair) {
            this.hideCrosshair = hideCrosshair;
            return this;
        }

        public ModeBuilder thermalSights(boolean thermalSights) {
            this.thermalSights = thermalSights;
            return this;
        }

        public ModeBuilder reloadChangeType(boolean reloadRequiresTypeChange) {
            this.reloadRequiresTypeChange = reloadRequiresTypeChange;
            return this;
        }

        public ModeBuilder reloadSequential(boolean reloadAnimationsSequential) {
            this.reloadAnimationsSequential = reloadAnimationsSequential;
            return this;
        }

        public ModeBuilder scopeTexture(String scopeTexture) {
            this.scopeTexture = scopeTexture;
            return this;
        }

        public ModeBuilder smoke(String smokeHandlerName) {
            this.smokeHandlerName = smokeHandlerName;
            return this;
        }

        public ModeBuilder orchestra(String orchestraName) {
            this.orchestraName = orchestraName;
            return this;
        }

        public ModeBuilder standardConfiguration() {
            this.pressPrimaryHandlerName = "Lego.LAMBDA_STANDARD_CLICK_PRIMARY";
            this.pressReloadHandlerName = "Lego.LAMBDA_STANDARD_RELOAD";
            this.pressTertiaryHandlerName = "Lego.LAMBDA_TOGGLE_AIM";
            this.deciderName = "GunStateDecider.LAMBDA_STANDARD_DECIDER";
            return this;
        }

        public ModeBuilder pressPrimary(String handlerName) {
            this.pressPrimaryHandlerName = handlerName;
            return this;
        }

        public ModeBuilder pressSecondary(String handlerName) {
            this.pressSecondaryHandlerName = handlerName;
            return this;
        }

        public ModeBuilder pressTertiary(String handlerName) {
            this.pressTertiaryHandlerName = handlerName;
            return this;
        }

        public ModeBuilder pressReload(String handlerName) {
            this.pressReloadHandlerName = handlerName;
            return this;
        }

        public ModeBuilder releasePrimary(String handlerName) {
            this.releasePrimaryHandlerName = handlerName;
            return this;
        }

        public ModeBuilder releaseSecondary(String handlerName) {
            this.releaseSecondaryHandlerName = handlerName;
            return this;
        }

        public ModeBuilder releaseTertiary(String handlerName) {
            this.releaseTertiaryHandlerName = handlerName;
            return this;
        }

        public ModeBuilder releaseReload(String handlerName) {
            this.releaseReloadHandlerName = handlerName;
            return this;
        }

        public ModeBuilder decider(String handlerName) {
            this.deciderName = handlerName;
            return this;
        }

        public ModeBuilder animation(String animationProfileName) {
            this.animationProfileName = animationProfileName;
            return this;
        }

        public ModeBuilder hud(String... hudComponentNames) {
            if (hudComponentNames != null) {
                this.hudComponentNames.addAll(List.of(hudComponentNames));
            }
            return this;
        }

        public ModeBuilder receiver(SednaReceiverConfig receiver) {
            this.receivers.add(receiver);
            return this;
        }

        public ModeBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public GunModeConfig build() {
            return new GunModeConfig(configIndex, durability, drawDuration, inspectDuration, inspectCancel, crosshair,
                    hideCrosshair, thermalSights, reloadRequiresTypeChange, reloadAnimationsSequential, scopeTexture,
                    smokeHandlerName, orchestraName, pressPrimaryHandlerName, pressSecondaryHandlerName,
                    pressTertiaryHandlerName, pressReloadHandlerName, releasePrimaryHandlerName,
                    releaseSecondaryHandlerName, releaseTertiaryHandlerName, releaseReloadHandlerName, deciderName,
                    animationProfileName, hudComponentNames, receivers, notes);
        }
    }
}
