package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjParticleAcceleratorModels {
    public static final LegacyWavefrontModel SOURCE = model("source").asVBO();
    public static final LegacyWavefrontModel BEAMLINE = model("beamline").asVBO();
    public static final LegacyWavefrontModel RFC = model("rfc").asVBO();
    public static final LegacyWavefrontModel QUADRUPOLE = model("quadrupole").asVBO();
    public static final LegacyWavefrontModel DIPOLE = model("dipole").asVBO();
    public static final LegacyWavefrontModel DETECTOR = model("detector").asVBO();

    private static final LegacyWavefrontModel.SelectionHandle BEAMLINE_BODY =
            BEAMLINE.prepareRenderOnlyInCallOrder("Beamline");
    private static final LegacyWavefrontModel.SelectionHandle BEAMLINE_WINDOW =
            BEAMLINE.prepareRenderOnlyInCallOrder("BeamlineWindow");
    private static final LegacyWavefrontModel.SelectionHandle BEAMLINE_GLASS =
            BEAMLINE.prepareRenderOnlyInCallOrder("BeamlineGlass");

    public static final ResourceLocation SOURCE_TEXTURE = texture("source");
    public static final ResourceLocation BEAMLINE_TEXTURE = texture("beamline");
    public static final ResourceLocation RFC_TEXTURE = texture("rfc");
    public static final ResourceLocation QUADRUPOLE_TEXTURE = texture("quadrupole");
    public static final ResourceLocation DIPOLE_TEXTURE = texture("dipole");
    public static final ResourceLocation DETECTOR_TEXTURE = texture("detector");

    public static LegacyWavefrontModel model(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/particleaccelerator/" + name + ".obj"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/particleaccelerator/" + name + ".png");
    }

    public static void renderBeamlinePart(String partName, ObjRenderContext context) {
        renderBeamlinePart(partName, BEAMLINE_TEXTURE, context);
    }

    public static void renderBeamlinePart(String partName, ResourceLocation texture, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = beamlineHandle(partName);
        if (handle != null) {
            BEAMLINE.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        BEAMLINE.renderPart(partName, texture, context);
    }

    public static void renderBeamlinePartUntextured(String partName, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = beamlineHandle(partName);
        if (handle != null) {
            BEAMLINE.renderOnlyUntextured(context, handle);
            return;
        }
        BEAMLINE.renderPartUntextured(partName, context);
    }

    private static LegacyWavefrontModel.SelectionHandle beamlineHandle(String partName) {
        return switch (partName) {
            case "Beamline" -> BEAMLINE_BODY;
            case "BeamlineWindow" -> BEAMLINE_WINDOW;
            case "BeamlineGlass" -> BEAMLINE_GLASS;
            default -> null;
        };
    }

    private ObjParticleAcceleratorModels() {
    }
}
