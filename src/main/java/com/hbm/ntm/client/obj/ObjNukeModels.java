package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjNukeModels {
    public static final LegacyWavefrontModel GADGET = model("bombs/gadget", "gadget").asVBO();
    public static final LegacyWavefrontModel BOY = rootModel("lilboy1", "boy");
    public static final LegacyWavefrontModel BOY_LEGACY = BOY;
    public static final LegacyWavefrontModel MAN = model("bombs/fat_man", "man").asVBO();
    public static final LegacyWavefrontModel TSAR = model("bombs/tsar", "tsar").asVBO();
    public static final LegacyWavefrontModel MIKE = model("bombs/ivymike", "mike");
    public static final LegacyWavefrontModel PROTOTYPE = model("bombs/prototype", "prototype").asVBO();
    public static final LegacyWavefrontModel FLEIJA = model("bombs/fleija", "fleija").asVBO();
    public static final LegacyWavefrontModel SOLINIUM = model("bombs/ufp", "solinium").asVBO();
    public static final LegacyWavefrontModel N2 = model("bombs/n2", "n2").asVBO();
    public static final LegacyWavefrontModel BOMB_MULTI_LEGACY = rootModel("bomb_generic", "bomb_multi_legacy");
    public static final ResourceLocation CUSTOM_NUKE_TEXTURE = directTexture("custom_nuke");
    public static final ResourceLocation GADGET_LEGACY_TEXTURE = texture("gadget_legacy");
    private static final LegacyWavefrontModel.SelectionHandle GADGET_BODY_HANDLE =
            GADGET.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle GADGET_WIRES_HANDLE =
            GADGET.prepareRenderOnlyInCallOrder("Wires");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/" + modelName + ".obj"),
                texture(textureName));
    }

    public static LegacyWavefrontModel rootModel(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return switch (name) {
            case "gadget" -> directBombTexture("gadget");
            case "boy" -> directTexture("lilboy");
            case "man" -> directTexture("fat_man");
            case "tsar" -> directBombTexture("tsar");
            case "mike" -> directBombTexture("ivymike");
            case "prototype" -> directBombTexture("prototype");
            case "fleija" -> directBombTexture("fleija");
            case "solinium" -> directBombTexture("ufp");
            case "n2" -> directBombTexture("n2");
            case "bomb_multi_legacy" -> directTexture("bomb_generic");
            case "custom_nuke" -> directTexture("custom_nuke");
            default -> new ResourceLocation(HbmNtm.MOD_ID, "textures/block/nuke/" + name + ".png");
        };
    }

    private static ResourceLocation directTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
    }

    private static ResourceLocation directBombTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/bombs/" + name + ".png");
    }

    public static void renderGadgetPart(ResourceLocation texture, ObjRenderContext context, String partName) {
        LegacyWavefrontModel.SelectionHandle handle = gadgetHandle(partName);
        if (handle != null) {
            GADGET.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        GADGET.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle gadgetHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Body" -> GADGET_BODY_HANDLE;
            case "Wires" -> GADGET_WIRES_HANDLE;
            default -> null;
        };
    }

    private ObjNukeModels() {
    }
}
