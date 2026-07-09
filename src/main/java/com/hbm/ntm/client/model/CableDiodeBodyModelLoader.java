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

public class CableDiodeBodyModelLoader implements IGeometryLoader<CableDiodeBodyModelLoader.Geometry> {
    private static final ResourceLocation DEFAULT_PLATE = texture("cable_diode");
    private static final ResourceLocation DEFAULT_PAD = texture("hadron_coil_alloy");

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation plate = texture(json, "plate", DEFAULT_PLATE);
        ResourceLocation pad = texture(json, "pad", DEFAULT_PAD);
        ResourceLocation particle = texture(json, "particle", plate);
        return new Geometry(plate, pad, particle);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + path);
    }

    private static ResourceLocation texture(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid cable diode body texture '" + key + "': " + value);
        }
        return location;
    }

    public record Geometry(ResourceLocation plate, ResourceLocation pad, ResourceLocation particle)
            implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            TextureAtlasSprite plateSprite = spriteGetter.apply(material(plate));
            TextureAtlasSprite padSprite = spriteGetter.apply(material(pad));
            TextureAtlasSprite particleSprite = spriteGetter.apply(material(particle));
            return new CableDiodeBodyBakedModel(plateSprite, padSprite, particleSprite, context.getTransforms());
        }

        private static Material material(ResourceLocation texture) {
            return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        }
    }
}
