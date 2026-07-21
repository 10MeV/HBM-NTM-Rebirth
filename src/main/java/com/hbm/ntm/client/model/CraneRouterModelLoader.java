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

public class CraneRouterModelLoader implements IGeometryLoader<CraneRouterModelLoader.Geometry> {
    private static final ResourceLocation DEFAULT_BASE = texture("crane_in");
    private static final ResourceLocation DEFAULT_OVERLAY = texture("crane_router_overlay");

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation base = texture(json, "base", DEFAULT_BASE);
        ResourceLocation overlay = texture(json, "overlay", DEFAULT_OVERLAY);
        return new Geometry(base, overlay);
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }

    private static ResourceLocation texture(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid crane router texture '" + key + "': " + value);
        }
        return location;
    }

    public record Geometry(ResourceLocation base, ResourceLocation overlay) implements IUnbakedGeometry<Geometry> {
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
            return new CraneRouterBakedModel(baseSprite, overlaySprite, context.getTransforms());
        }

        private static Material material(ResourceLocation texture) {
            return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        }
    }
}
