package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjParticleAcceleratorModels {
    public static final LegacyWavefrontModel SOURCE = model("source");
    public static final LegacyWavefrontModel BEAMLINE = model("beamline");
    public static final LegacyWavefrontModel RFC = model("rfc");
    public static final LegacyWavefrontModel QUADRUPOLE = model("quadrupole");
    public static final LegacyWavefrontModel DIPOLE = model("dipole");
    public static final LegacyWavefrontModel DETECTOR = model("detector");

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/particleaccelerator/" + name + ".obj"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/particleaccelerator/" + name + ".png");
    }

    private ObjParticleAcceleratorModels() {
    }
}
