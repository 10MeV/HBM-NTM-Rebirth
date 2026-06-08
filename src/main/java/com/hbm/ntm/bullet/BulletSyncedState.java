package com.hbm.ntm.bullet;

public record BulletSyncedState(int configId, int styleId, int trailId) {
    public static final BulletSyncedState EMPTY = new BulletSyncedState(-1, BulletStyle.NONE.legacyId(), 0);

    public static BulletSyncedState fromConfig(BulletConfig config) {
        if (config == null) {
            return EMPTY;
        }
        return new BulletSyncedState(
                BulletConfigSyncRegistry.getLegacyId(config),
                config.style().legacyId(),
                config.trail());
    }

    public static BulletSyncedState fromLegacyId(int configId) {
        return BulletConfigSyncRegistry.pullConfig(configId)
                .map(config -> new BulletSyncedState(configId, config.style().legacyId(), config.trail()))
                .orElse(EMPTY);
    }

    public byte styleByte() {
        return (byte) styleId;
    }

    public byte trailByte() {
        return (byte) trailId;
    }
}
