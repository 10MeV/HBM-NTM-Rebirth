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

public class FluidDuctPaintableModelLoader
        implements IGeometryLoader<FluidDuctPaintableModelLoader.Geometry> {
    private static final ResourceLocation DEFAULT_BASE = texture("fluid_duct_paintable");
    private static final ResourceLocation DEFAULT_EXHAUST_BASE = texture("fluid_duct_paintable_block_exhaust");
    private static final ResourceLocation DEFAULT_OVERLAY = texture("fluid_duct_paintable_overlay");
    private static final ResourceLocation DEFAULT_COLOR_OVERLAY = texture("fluid_duct_paintable_color");

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        boolean exhaust = GsonHelper.getAsBoolean(json, "exhaust", false);
        ResourceLocation base = texture(json, "base", exhaust ? DEFAULT_EXHAUST_BASE : DEFAULT_BASE);
        ResourceLocation overlay = texture(json, "overlay", DEFAULT_OVERLAY);
        ResourceLocation colorOverlay = texture(json, "color_overlay", DEFAULT_COLOR_OVERLAY);
        ResourceLocation particle = texture(json, "particle", base);
        return new Geometry(base, overlay, colorOverlay, particle, exhaust);
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + path);
    }

    private static ResourceLocation texture(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid paintable fluid duct texture '" + key + "': " + value);
        }
        return location;
    }

    public record Geometry(ResourceLocation base, ResourceLocation overlay, ResourceLocation colorOverlay,
                           ResourceLocation particle, boolean exhaust) implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            TextureAtlasSprite baseSprite = spriteGetter.apply(material(base));
            TextureAtlasSprite overlaySprite = spriteGetter.apply(material(overlay));
            TextureAtlasSprite colorOverlaySprite = spriteGetter.apply(material(colorOverlay));
            TextureAtlasSprite particleSprite = spriteGetter.apply(material(particle));
            return new FluidDuctPaintableBakedModel(baseSprite, overlaySprite, colorOverlaySprite,
                    particleSprite, exhaust, context.getTransforms());
        }

        private static Material material(ResourceLocation texture) {
            return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        }
    }
}
