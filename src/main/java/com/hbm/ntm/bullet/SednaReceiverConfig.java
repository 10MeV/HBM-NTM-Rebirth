package com.hbm.ntm.bullet;

import com.hbm.ntm.sound.LegacySoundIds;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record SednaReceiverConfig(
        String legacyKey,
        int receiverIndex,
        String sourceClassName,
        String magazineKey,
        float baseDamage,
        int delayAfterFire,
        int delayAfterDryFire,
        int roundsPerCycle,
        float splitProjectiles,
        float spreadInnate,
        float spreadAmmoMultiplier,
        float spreadHipfire,
        float spreadDurability,
        boolean refireOnHold,
        boolean refireAfterDry,
        boolean doesDryFire,
        boolean doesDryFireAfterAuto,
        boolean ejectOnFire,
        boolean reloadOnEmpty,
        int reloadCockOnEmptyPre,
        int reloadBeginDuration,
        int reloadCycleDuration,
        int reloadEndDuration,
        int reloadCockOnEmptyPost,
        int jamDuration,
        String fireSoundName,
        float fireVolume,
        float firePitch,
        Offset projectileOffset,
        Offset projectileOffsetScoped,
        String canFireHandlerName,
        String fireHandlerName,
        String recoilHandlerName,
        String notes) {

    public SednaReceiverConfig {
        legacyKey = clean(legacyKey);
        sourceClassName = clean(sourceClassName);
        magazineKey = clean(magazineKey);
        roundsPerCycle = Math.max(1, roundsPerCycle);
        splitProjectiles = splitProjectiles <= 0.0F ? 1.0F : splitProjectiles;
        spreadAmmoMultiplier = spreadAmmoMultiplier <= 0.0F ? 1.0F : spreadAmmoMultiplier;
        projectileOffset = projectileOffset == null ? Offset.ZERO : projectileOffset;
        projectileOffsetScoped = projectileOffsetScoped == null ? Offset.ZERO : projectileOffsetScoped;
        canFireHandlerName = clean(canFireHandlerName);
        fireHandlerName = clean(fireHandlerName);
        recoilHandlerName = clean(recoilHandlerName);
        fireSoundName = LegacySoundIds.normalizeIdString(fireSoundName);
        notes = clean(notes);
    }

    public Optional<SednaMagazineConfig> magazine() {
        return LegacySednaMagazineConfigs.byKey(magazineKey);
    }

    public Optional<ResourceLocation> fireSoundLocation() {
        return Optional.ofNullable(LegacySoundIds.resolveLocation(fireSoundName));
    }

    public static Builder builder(String legacyKey, int receiverIndex, String sourceClassName) {
        return new Builder(legacyKey, receiverIndex, sourceClassName);
    }

    public static Builder fromMagazine(SednaMagazineConfig magazine) {
        return builder(magazine.legacyKey(), magazine.index(), magazine.sourceClassName())
                .magazineKey(magazine.legacyKey());
    }

    public static Builder fromMagazine(SednaMagazineConfig magazine, int receiverIndex) {
        return builder(magazine.legacyKey(), receiverIndex, magazine.sourceClassName())
                .magazineKey(magazine.legacyKey());
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    public record Offset(double forward, double up, double side) {
        public static final Offset ZERO = new Offset(0.0D, 0.0D, 0.0D);
    }

    public static final class Builder {
        private final String legacyKey;
        private final int receiverIndex;
        private final String sourceClassName;
        private String magazineKey = "";
        private float baseDamage;
        private int delayAfterFire;
        private int delayAfterDryFire;
        private int roundsPerCycle = 1;
        private float splitProjectiles = 1.0F;
        private float spreadInnate;
        private float spreadAmmoMultiplier = 1.0F;
        private float spreadHipfire = 0.025F;
        private float spreadDurability = 0.125F;
        private boolean refireOnHold;
        private boolean refireAfterDry;
        private boolean doesDryFire = true;
        private boolean doesDryFireAfterAuto;
        private boolean ejectOnFire = true;
        private boolean reloadOnEmpty;
        private int reloadCockOnEmptyPre;
        private int reloadBeginDuration;
        private int reloadCycleDuration;
        private int reloadEndDuration;
        private int reloadCockOnEmptyPost;
        private int jamDuration;
        private String fireSoundName = "";
        private float fireVolume = 1.0F;
        private float firePitch = 1.0F;
        private Offset projectileOffset = Offset.ZERO;
        private Offset projectileOffsetScoped = Offset.ZERO;
        private String canFireHandlerName = "";
        private String fireHandlerName = "";
        private String recoilHandlerName = "";
        private String notes = "";

        private Builder(String legacyKey, int receiverIndex, String sourceClassName) {
            this.legacyKey = legacyKey;
            this.receiverIndex = receiverIndex;
            this.sourceClassName = sourceClassName;
        }

        public Builder magazineKey(String magazineKey) {
            this.magazineKey = magazineKey;
            return this;
        }

        public Builder damage(float damage) {
            this.baseDamage = damage;
            return this;
        }

        public Builder delay(int delay) {
            this.delayAfterFire = delay;
            this.delayAfterDryFire = delay;
            return this;
        }

        public Builder dryDelay(int delay) {
            this.delayAfterDryFire = delay;
            return this;
        }

        public Builder rounds(int rounds) {
            this.roundsPerCycle = rounds;
            return this;
        }

        public Builder split(float split) {
            this.splitProjectiles = split;
            return this;
        }

        public Builder spread(float spread) {
            this.spreadInnate = spread;
            return this;
        }

        public Builder spreadAmmo(float spread) {
            this.spreadAmmoMultiplier = spread;
            return this;
        }

        public Builder spreadHipfire(float spread) {
            this.spreadHipfire = spread;
            return this;
        }

        public Builder spreadDurability(float spread) {
            this.spreadDurability = spread;
            return this;
        }

        public Builder auto(boolean auto) {
            this.refireOnHold = auto;
            return this;
        }

        public Builder autoAfterDry(boolean autoAfterDry) {
            this.refireAfterDry = autoAfterDry;
            return this;
        }

        public Builder dryfire(boolean dryfire) {
            this.doesDryFire = dryfire;
            return this;
        }

        public Builder dryfireAfterAuto(boolean dryfireAfterAuto) {
            this.doesDryFireAfterAuto = dryfireAfterAuto;
            return this;
        }

        public Builder ejectOnFire(boolean ejectOnFire) {
            this.ejectOnFire = ejectOnFire;
            return this;
        }

        public Builder reloadOnEmpty(boolean reloadOnEmpty) {
            this.reloadOnEmpty = reloadOnEmpty;
            return this;
        }

        public Builder reload(int delay) {
            return reload(0, delay, delay, 0, 0);
        }

        public Builder reload(int begin, int cycle, int end, int cock) {
            return reload(0, begin, cycle, end, cock);
        }

        public Builder reload(int pre, int begin, int cycle, int end, int post) {
            this.reloadCockOnEmptyPre = pre;
            this.reloadBeginDuration = begin;
            this.reloadCycleDuration = cycle;
            this.reloadEndDuration = end;
            this.reloadCockOnEmptyPost = post;
            return this;
        }

        public Builder jam(int jam) {
            this.jamDuration = jam;
            return this;
        }

        public Builder sound(String sound, float volume, float pitch) {
            this.fireSoundName = sound;
            this.fireVolume = volume;
            this.firePitch = pitch;
            return this;
        }

        public Builder offset(double forward, double up, double side) {
            this.projectileOffset = new Offset(forward, up, side);
            this.projectileOffsetScoped = new Offset(forward, up, 0.0D);
            return this;
        }

        public Builder offsetScoped(double forward, double up, double side) {
            this.projectileOffsetScoped = new Offset(forward, up, side);
            return this;
        }

        public Builder standardFire() {
            this.canFireHandlerName = "Lego.LAMBDA_STANDARD_CAN_FIRE";
            this.fireHandlerName = "Lego.LAMBDA_STANDARD_FIRE";
            return this;
        }

        public Builder secondFire() {
            this.canFireHandlerName = "Lego.LAMBDA_SECOND_CAN_FIRE";
            this.fireHandlerName = "Lego.LAMBDA_SECOND_FIRE";
            return this;
        }

        public Builder lockonFire() {
            this.canFireHandlerName = "Lego.LAMBDA_LOCKON_CAN_FIRE";
            this.fireHandlerName = "Lego.LAMBDA_STANDARD_FIRE";
            return this;
        }

        public Builder fireHandlers(String canFireHandlerName, String fireHandlerName) {
            this.canFireHandlerName = canFireHandlerName;
            this.fireHandlerName = fireHandlerName;
            return this;
        }

        public Builder recoil(String recoilHandlerName) {
            this.recoilHandlerName = recoilHandlerName;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public SednaReceiverConfig build() {
            return new SednaReceiverConfig(legacyKey, receiverIndex, sourceClassName, magazineKey, baseDamage,
                    delayAfterFire, delayAfterDryFire, roundsPerCycle, splitProjectiles, spreadInnate,
                    spreadAmmoMultiplier, spreadHipfire, spreadDurability, refireOnHold, refireAfterDry,
                    doesDryFire, doesDryFireAfterAuto, ejectOnFire, reloadOnEmpty, reloadCockOnEmptyPre,
                    reloadBeginDuration, reloadCycleDuration, reloadEndDuration, reloadCockOnEmptyPost, jamDuration,
                    fireSoundName, fireVolume, firePitch, projectileOffset, projectileOffsetScoped, canFireHandlerName,
                    fireHandlerName, recoilHandlerName, notes);
        }
    }
}
