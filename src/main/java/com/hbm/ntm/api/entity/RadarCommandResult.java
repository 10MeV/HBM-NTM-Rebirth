package com.hbm.ntm.api.entity;

public enum RadarCommandResult {
    UNDEFINED(false),
    QUEUED(true),
    LAUNCHED(true),
    TRIGGERED(true),
    ERROR_MISSING_COMPONENT(false),
    ERROR_INCOMPATIBLE(false),
    ERROR_NO_TARGET(false);

    private final boolean successful;

    RadarCommandResult(boolean successful) {
        this.successful = successful;
    }

    public boolean successful() {
        return successful;
    }

    public static RadarCommandResult fromBoolean(boolean successful, RadarCommandResult successResult) {
        if (!successful) {
            return ERROR_MISSING_COMPONENT;
        }
        return successResult != null ? successResult : TRIGGERED;
    }
}
