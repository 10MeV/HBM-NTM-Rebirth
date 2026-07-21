package com.hbm.ntm.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hbm.ntm.HbmNtm;
import java.util.LinkedHashMap;
import java.util.Map;
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

public class CraneLogisticsModelLoader implements IGeometryLoader<CraneLogisticsModelLoader.Geometry> {
    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return new Geometry(CraneLogisticsBakedModel.Style.byName(GsonHelper.getAsString(json, "style")));
    }

    public record Geometry(CraneLogisticsBakedModel.Style style) implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            Map<String, TextureAtlasSprite> sprites = new LinkedHashMap<>();
            for (String texture : style.textures()) {
                ResourceLocation location = new ResourceLocation(HbmNtm.MOD_ID, "block/" + texture);
                sprites.put(texture, spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, location)));
            }
            return new CraneLogisticsBakedModel(style, Map.copyOf(sprites), context.getTransforms());
        }
    }
}
