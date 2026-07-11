package com.hbm.ntm.entity.cart;

public enum NtmMinecartType {
    EMPTY("empty"),
    CRATE("crate"),
    POWDER("powder"),
    SEMTEX("semtex"),
    DESTROYER("destroyer");

    private final String legacyName;

    NtmMinecartType(String legacyName) {
        this.legacyName = legacyName;
    }

    public String legacyName() {
        return legacyName;
    }
}
