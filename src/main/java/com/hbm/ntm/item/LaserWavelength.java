package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;

public enum LaserWavelength {
    NULL("laser_crystal_none", 0x010101, 0x010101, ChatFormatting.WHITE),
    IR("laser_crystal_co2", 0xBB1010, 0xCC4040, ChatFormatting.RED),
    VISIBLE("laser_crystal_bismuth", 0, 0, ChatFormatting.GREEN),
    UV("laser_crystal_cmb", 0x0A1FC4, 0x00EFFF, ChatFormatting.AQUA),
    GAMMA("laser_crystal_dnt", 0x150560, 0xEF00FF, ChatFormatting.LIGHT_PURPLE),
    DRX("laser_crystal_digamma", 0xFF0000, 0xFF0000, ChatFormatting.DARK_RED);

    private final String legacyItemName;
    private final int renderedBeamColor;
    private final int guiColor;
    private final ChatFormatting textColor;

    LaserWavelength(String legacyItemName, int renderedBeamColor, int guiColor, ChatFormatting textColor) {
        this.legacyItemName = legacyItemName;
        this.renderedBeamColor = renderedBeamColor;
        this.guiColor = guiColor;
        this.textColor = textColor;
    }

    public String legacyItemName() {
        return legacyItemName;
    }

    public int renderedBeamColor() {
        return renderedBeamColor;
    }

    public int guiColor() {
        return guiColor;
    }

    public ChatFormatting textColor() {
        return textColor;
    }

    public boolean canPower(LaserWavelength required) {
        return this != NULL && required != null && ordinal() >= required.ordinal();
    }

    public static LaserWavelength byLegacyItemName(String name) {
        if (name == null) {
            return NULL;
        }
        for (LaserWavelength wavelength : values()) {
            if (wavelength.legacyItemName.equals(name)) {
                return wavelength;
            }
        }
        return NULL;
    }

    public static LaserWavelength byOrdinal(int ordinal) {
        LaserWavelength[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NULL;
    }
}
