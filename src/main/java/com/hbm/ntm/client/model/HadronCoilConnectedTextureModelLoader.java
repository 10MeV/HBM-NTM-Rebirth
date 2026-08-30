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

/** Bakes the two texture sheets used by one legacy BlockHadronCoil tier. */
public final class HadronCoilConnectedTextureModelLoader
        implements IGeometryLoader<HadronCoilConnectedTextureModelLoader.Geometry> {
    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry(texture(json, "block", "hadron_coil_alloy"),
                texture(json, "block_ct", "hadron_coil_alloy_ct"));
    }

    private static ResourceLocation texture(JsonObject json, String key, String fallback) {
        ResourceLocation location = ResourceLocation.tryParse(GsonHelper.getAsString(json, key,
                HbmNtm.MOD_ID + ":block/" + fallback));
        if (location == null) throw new JsonParseException("Invalid Hadron coil CT texture '" + key + "'");
        return location;
    }

    public record Geometry(ResourceLocation block, ResourceLocation blockCt) implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> getter, IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> sprites, ModelState state, ItemOverrides overrides,
                ResourceLocation location) {
            return new HadronCoilConnectedTextureBakedModel(sprite(sprites, block), sprite(sprites, blockCt),
                    context.getTransforms());
        }

        private static TextureAtlasSprite sprite(Function<Material, TextureAtlasSprite> sprites,
                ResourceLocation texture) {
            return sprites.apply(new Material(InventoryMenu.BLOCK_ATLAS, texture));
        }
    }
}
