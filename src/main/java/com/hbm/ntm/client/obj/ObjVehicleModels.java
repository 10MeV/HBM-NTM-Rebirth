package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjVehicleModels {
    public static final LegacyWavefrontModel CART = model("cart", entityTexture("cart_metal"));
    public static final LegacyWavefrontModel CART_DESTROYER = model("cart_destroyer", entityTexture("cart_destroyer"));
    public static final LegacyWavefrontModel CART_POWDER = model("cart_powder", blockTexture("block_gunpowder"));
    public static final LegacyWavefrontModel TRAM = model("tram", trainTexture("tram"));
    public static final LegacyWavefrontModel TRAM_TRAILER = model("tram_trailer", trainTexture("tram_trailer"));

    public static final ResourceLocation CART_METAL_TEXTURE = entityTexture("cart_metal");
    public static final ResourceLocation CART_BLANK_TEXTURE = entityTexture("cart_metal_naked");
    public static final ResourceLocation CART_WOOD_TEXTURE = entityTexture("cart_wood");
    public static final ResourceLocation CART_DESTROYER_TEXTURE = entityTexture("cart_destroyer");
    public static final ResourceLocation CART_POWDER_TEXTURE = blockTexture("block_gunpowder");
    public static final ResourceLocation CART_GUNPOWDER_BLOCK_TEXTURE = CART_POWDER_TEXTURE;
    public static final ResourceLocation CART_SEMTEX_SIDE_TEXTURE = blockTexture("semtex_side");
    public static final ResourceLocation CART_SEMTEX_TOP_TEXTURE = blockTexture("semtex_bottom");
    public static final ResourceLocation CART_SEMTEX_BOTTOM_BLOCK_TEXTURE = CART_SEMTEX_TOP_TEXTURE;
    public static final ResourceLocation TRAM_TEXTURE = trainTexture("tram");
    public static final ResourceLocation TRAM_TRAILER_TEXTURE = trainTexture("tram_trailer");

    public static LegacyWavefrontModel model(String name) {
        return model(name, name);
    }

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return model(modelName, entityTexture(textureName));
    }

    public static LegacyWavefrontModel model(String modelName, ResourceLocation texture) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/vehicles/" + modelName + ".obj"),
                texture);
    }

    public static ResourceLocation texture(String name) {
        return entityTexture(name);
    }

    public static ResourceLocation entityTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/" + name + ".png");
    }

    public static ResourceLocation blockTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/" + name + ".png");
    }

    public static ResourceLocation trainTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/trains/" + name + ".png");
    }

    private ObjVehicleModels() {
    }
}
