package com.hbm.ntm.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hbm.ntm.HbmNtm;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

public final class Mk2PileConnectedTextureModelLoader implements IGeometryLoader<Mk2PileConnectedTextureModelLoader.Geometry> {
    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry(texture(json, "side", "pile_block"), texture(json, "top", "pile_block_top"),
                texture(json, "side_ct", "pile_block_ct"), texture(json, "top_ct", "pile_block_top_ct"),
                texture(json, "input_ct", "pile_block_input_ct"), texture(json, "output_ct", "pile_block_output_ct"),
                texture(json, "control_top_ct", "pile_block_control_top_ct"), texture(json, "core_ct", "pile_block_core_ct"));
    }

    private static ResourceLocation texture(JsonObject json, String key, String fallback) {
        ResourceLocation location = ResourceLocation.tryParse(GsonHelper.getAsString(json, key, HbmNtm.MOD_ID + ":block/" + fallback));
        if (location == null) throw new JsonParseException("Invalid MK2 Pile CT texture '" + key + "'");
        return location;
    }

    public record Geometry(ResourceLocation side, ResourceLocation top, ResourceLocation sideCt, ResourceLocation topCt,
            ResourceLocation inputCt, ResourceLocation outputCt, ResourceLocation controlTopCt, ResourceLocation coreCt)
            implements IUnbakedGeometry<Geometry> {
        @Override public void resolveParents(Function<ResourceLocation, UnbakedModel> getter, IGeometryBakingContext context) { }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites,
                ModelState state, ItemOverrides overrides, ResourceLocation location) {
            return new Mk2PileConnectedTextureBakedModel(sprite(sprites, side), sprite(sprites, top), sprite(sprites, sideCt),
                    sprite(sprites, topCt), sprite(sprites, inputCt), sprite(sprites, outputCt), sprite(sprites, controlTopCt),
                    sprite(sprites, coreCt), context.getTransforms());
        }

        private static TextureAtlasSprite sprite(Function<Material, TextureAtlasSprite> sprites, ResourceLocation texture) {
            return sprites.apply(new Material(InventoryMenu.BLOCK_ATLAS, texture));
        }
    }
}
