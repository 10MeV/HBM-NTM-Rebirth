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

public class PneumaticTubeModelLoader implements IGeometryLoader<PneumaticTubeModelLoader.Geometry> {
    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry(
                texture(json, "base", "pneumatic_tube"),
                texture(json, "straight", "pneumatic_tube_straight"),
                texture(json, "input", "pneumatic_tube_in"),
                texture(json, "output", "pneumatic_tube_out"),
                texture(json, "connector", "pneumatic_tube_connector"),
                texture(json, "overlay", "pneumatic_tube_paintable_overlay"),
                texture(json, "overlay_input", "pneumatic_tube_paintable_overlay_in"),
                texture(json, "overlay_output", "pneumatic_tube_paintable_overlay_out"),
                GsonHelper.getAsBoolean(json, "paintable", false));
    }

    private static ResourceLocation texture(JsonObject json, String key, String fallbackPath) {
        String value = GsonHelper.getAsString(json, key, new ResourceLocation(HbmNtm.MOD_ID,
                "block/" + fallbackPath).toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid pneumatic tube texture '" + key + "': " + value);
        }
        return location;
    }

    public record Geometry(ResourceLocation base, ResourceLocation straight, ResourceLocation input,
                           ResourceLocation output, ResourceLocation connector, ResourceLocation overlay,
                           ResourceLocation overlayInput, ResourceLocation overlayOutput, boolean paintable)
            implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            return new PneumaticTubeBakedModel(
                    spriteGetter.apply(material(base)), spriteGetter.apply(material(straight)),
                    spriteGetter.apply(material(input)), spriteGetter.apply(material(output)),
                    spriteGetter.apply(material(connector)), spriteGetter.apply(material(overlay)),
                    spriteGetter.apply(material(overlayInput)), spriteGetter.apply(material(overlayOutput)),
                    paintable, context.getTransforms());
        }

        private static Material material(ResourceLocation texture) {
            return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        }
    }
}
