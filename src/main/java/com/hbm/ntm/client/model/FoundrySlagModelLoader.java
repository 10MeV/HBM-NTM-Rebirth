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

public class FoundrySlagModelLoader implements IGeometryLoader<FoundrySlagModelLoader.FoundrySlagGeometry> {
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "block/slag");

    @Override
    public FoundrySlagGeometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        String texture = GsonHelper.getAsString(json, "texture", DEFAULT_TEXTURE.toString());
        ResourceLocation location = ResourceLocation.tryParse(texture);
        if (location == null) {
            throw new JsonParseException("Invalid foundry slag texture: " + texture);
        }
        return new FoundrySlagGeometry(location);
    }

    public record FoundrySlagGeometry(ResourceLocation texture) implements IUnbakedGeometry<FoundrySlagGeometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            TextureAtlasSprite sprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, texture));
            return new FoundrySlagBakedModel(sprite, context.getTransforms());
        }
    }
}
