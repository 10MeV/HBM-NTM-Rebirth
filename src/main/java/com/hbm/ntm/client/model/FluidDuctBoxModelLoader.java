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

public class FluidDuctBoxModelLoader implements IGeometryLoader<FluidDuctBoxModelLoader.Geometry> {
    private static final String[] MATERIAL_NAMES = {"silver", "copper", "white"};

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation[] prefixes = new ResourceLocation[MATERIAL_NAMES.length];
        for (int material = 0; material < prefixes.length; material++) {
            String name = MATERIAL_NAMES[material];
            prefixes[material] = texturePrefix(json, name + "_prefix", texture("boxduct_" + name));
        }
        ResourceLocation particle = texture(json, "particle", texture("boxduct_silver_junction_0"));
        return new Geometry(prefixes, particle);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + path);
    }

    private static ResourceLocation texturePrefix(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid fluid duct box texture prefix '" + key + "': " + value);
        }
        return location;
    }

    private static ResourceLocation texture(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid fluid duct box texture '" + key + "': " + value);
        }
        return location;
    }

    public record Geometry(ResourceLocation[] materialPrefixes, ResourceLocation particle)
            implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            FluidDuctBoxBakedModel.TextureSet[] materials =
                    new FluidDuctBoxBakedModel.TextureSet[materialPrefixes.length];
            for (int material = 0; material < materials.length; material++) {
                ResourceLocation prefix = materialPrefixes[material];
                TextureAtlasSprite straight = spriteGetter.apply(material(prefix, "straight"));
                TextureAtlasSprite end = spriteGetter.apply(material(prefix, "end"));
                TextureAtlasSprite curveTl = spriteGetter.apply(material(prefix, "curve_tl"));
                TextureAtlasSprite curveTr = spriteGetter.apply(material(prefix, "curve_tr"));
                TextureAtlasSprite curveBl = spriteGetter.apply(material(prefix, "curve_bl"));
                TextureAtlasSprite curveBr = spriteGetter.apply(material(prefix, "curve_br"));
                TextureAtlasSprite[] junctions = new TextureAtlasSprite[5];
                for (int step = 0; step < junctions.length; step++) {
                    junctions[step] = spriteGetter.apply(material(prefix, "junction_" + step));
                }
                materials[material] = new FluidDuctBoxBakedModel.TextureSet(straight, end, curveTl, curveTr,
                        curveBl, curveBr, junctions);
            }
            TextureAtlasSprite particleSprite = spriteGetter.apply(material(particle));
            return new FluidDuctBoxBakedModel(materials, particleSprite, context.getTransforms());
        }

        private static Material material(ResourceLocation texture) {
            return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        }

        private static Material material(ResourceLocation prefix, String suffix) {
            ResourceLocation texture = new ResourceLocation(prefix.getNamespace(), prefix.getPath() + "_" + suffix);
            return material(texture);
        }
    }
}
