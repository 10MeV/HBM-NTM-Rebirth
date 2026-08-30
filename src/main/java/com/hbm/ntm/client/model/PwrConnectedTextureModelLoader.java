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

/** Bakes the two legacy BlockPWR connected-texture palettes. */
public final class PwrConnectedTextureModelLoader implements IGeometryLoader<PwrConnectedTextureModelLoader.Geometry> {
    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry(texture(json, "block", "pwr_block"), texture(json, "block_ct", "pwr_block_ct"),
                texture(json, "port", "pwr_casing_port"), texture(json, "port_ct", "pwr_casing_port_ct"));
    }

    private static ResourceLocation texture(JsonObject json, String key, String fallback) {
        ResourceLocation location = ResourceLocation.tryParse(GsonHelper.getAsString(json, key,
                HbmNtm.MOD_ID + ":block/" + fallback));
        if (location == null) throw new JsonParseException("Invalid PWR CT texture '" + key + "'");
        return location;
    }

    public record Geometry(ResourceLocation block, ResourceLocation blockCt, ResourceLocation port,
            ResourceLocation portCt) implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> getter, IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> sprites, ModelState state, ItemOverrides overrides,
                ResourceLocation location) {
            return new PwrConnectedTextureBakedModel(sprite(sprites, block), sprite(sprites, blockCt),
                    sprite(sprites, port), sprite(sprites, portCt), context.getTransforms());
        }

        private static TextureAtlasSprite sprite(Function<Material, TextureAtlasSprite> sprites,
                ResourceLocation texture) {
            return sprites.apply(new Material(InventoryMenu.BLOCK_ATLAS, texture));
        }
    }
}
