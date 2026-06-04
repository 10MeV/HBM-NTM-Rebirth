package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjVehicleModels {
    public static final LegacyWavefrontModel CART = model("cart", "cart_metal");
    public static final LegacyWavefrontModel CART_DESTROYER = model("cart_destroyer");
    public static final LegacyWavefrontModel CART_POWDER = model("cart_powder");
    public static final LegacyWavefrontModel TRAM = model("tram");
    public static final LegacyWavefrontModel TRAM_TRAILER = model("tram_trailer");

    public static final ResourceLocation CART_METAL_TEXTURE = texture("cart_metal");
    public static final ResourceLocation CART_BLANK_TEXTURE = texture("cart_metal_naked");
    public static final ResourceLocation CART_WOOD_TEXTURE = texture("cart_wood");
    public static final ResourceLocation CART_DESTROYER_TEXTURE = texture("cart_destroyer");
    public static final ResourceLocation CART_POWDER_TEXTURE = texture("cart_powder");
    public static final ResourceLocation CART_GUNPOWDER_BLOCK_TEXTURE = CART_POWDER_TEXTURE;
    public static final ResourceLocation CART_SEMTEX_SIDE_TEXTURE = texture("cart_semtex_side");
    public static final ResourceLocation CART_SEMTEX_TOP_TEXTURE = texture("cart_semtex_top");
    public static final ResourceLocation CART_SEMTEX_BOTTOM_BLOCK_TEXTURE = CART_SEMTEX_TOP_TEXTURE;
    public static final ResourceLocation TRAM_TEXTURE = texture("tram");
    public static final ResourceLocation TRAM_TRAILER_TEXTURE = texture("tram_trailer");

    public static LegacyWavefrontModel model(String name) {
        return model(name, name);
    }

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/vehicles/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/vehicles/" + name + ".png");
    }

    private ObjVehicleModels() {
    }
}
