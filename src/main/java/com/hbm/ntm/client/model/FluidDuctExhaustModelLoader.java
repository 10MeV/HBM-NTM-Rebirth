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

public class FluidDuctExhaustModelLoader implements IGeometryLoader<FluidDuctExhaustModelLoader.Geometry> {
    private static final ResourceLocation DEFAULT_STRAIGHT = texture("boxduct_exhaust_straight");
    private static final ResourceLocation DEFAULT_END = texture("boxduct_exhaust_end");
    private static final ResourceLocation DEFAULT_CURVE_TL = texture("boxduct_exhaust_curve_tl");
    private static final ResourceLocation DEFAULT_CURVE_TR = texture("boxduct_exhaust_curve_tr");
    private static final ResourceLocation DEFAULT_CURVE_BL = texture("boxduct_exhaust_curve_bl");
    private static final ResourceLocation DEFAULT_CURVE_BR = texture("boxduct_exhaust_curve_br");

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation straight = texture(json, "straight", DEFAULT_STRAIGHT);
        ResourceLocation end = texture(json, "end", DEFAULT_END);
        ResourceLocation curveTl = texture(json, "curve_tl", DEFAULT_CURVE_TL);
        ResourceLocation curveTr = texture(json, "curve_tr", DEFAULT_CURVE_TR);
        ResourceLocation curveBl = texture(json, "curve_bl", DEFAULT_CURVE_BL);
        ResourceLocation curveBr = texture(json, "curve_br", DEFAULT_CURVE_BR);
        ResourceLocation[] junctions = new ResourceLocation[5];
        for (int step = 0; step < junctions.length; step++) {
            junctions[step] = texture(json, "junction_" + step, texture("boxduct_exhaust_junction_" + step));
        }
        ResourceLocation particle = texture(json, "particle", straight);
        return new Geometry(straight, end, curveTl, curveTr, curveBl, curveBr, junctions, particle);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + path);
    }

    private static ResourceLocation texture(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid fluid duct exhaust texture '" + key + "': " + value);
        }
        return location;
    }

    public record Geometry(ResourceLocation straight, ResourceLocation end, ResourceLocation curveTl,
                           ResourceLocation curveTr, ResourceLocation curveBl, ResourceLocation curveBr,
                           ResourceLocation[] junctions, ResourceLocation particle)
            implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            TextureAtlasSprite straightSprite = spriteGetter.apply(material(straight));
            TextureAtlasSprite endSprite = spriteGetter.apply(material(end));
            TextureAtlasSprite curveTlSprite = spriteGetter.apply(material(curveTl));
            TextureAtlasSprite curveTrSprite = spriteGetter.apply(material(curveTr));
            TextureAtlasSprite curveBlSprite = spriteGetter.apply(material(curveBl));
            TextureAtlasSprite curveBrSprite = spriteGetter.apply(material(curveBr));
            TextureAtlasSprite[] junctionSprites = new TextureAtlasSprite[junctions.length];
            for (int step = 0; step < junctionSprites.length; step++) {
                junctionSprites[step] = spriteGetter.apply(material(junctions[step]));
            }
            TextureAtlasSprite particleSprite = spriteGetter.apply(material(particle));
            return new FluidDuctExhaustBakedModel(straightSprite, endSprite, curveTlSprite, curveTrSprite,
                    curveBlSprite, curveBrSprite, junctionSprites, particleSprite, context.getTransforms());
        }

        private static Material material(ResourceLocation texture) {
            return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        }
    }
}
