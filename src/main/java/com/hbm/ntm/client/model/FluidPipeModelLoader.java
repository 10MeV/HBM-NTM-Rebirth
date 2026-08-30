package com.hbm.ntm.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
import com.mojang.math.Transformation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.NotNull;

public class FluidPipeModelLoader implements IGeometryLoader<FluidPipeModelLoader.Geometry> {
    private static final String[] PARTS = {
            "pZ", "pX", "nZ", "nX", "pY", "nY",
            "nnn", "nnp", "pnp", "pnn", "ppn", "npn", "npp", "ppp"
    };
    private static final Set<String> PART_SET = Set.of(PARTS);
    private static final ResourceLocation DEFAULT_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/blocks/pipe_neo.obj");

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation model = location(json, "model", DEFAULT_MODEL);
        boolean flipV = GsonHelper.getAsBoolean(json, "flip_v", true);
        ResourceLocation[] base = new ResourceLocation[HbmFluidDuctVariants.STANDARD_STYLE_COUNT];
        ResourceLocation[] overlay = new ResourceLocation[HbmFluidDuctVariants.STANDARD_STYLE_COUNT];
        for (int style = 0; style < base.length; style++) {
            base[style] = texture(json, "base_" + style,
                    legacyBlockTexture(HbmFluidDuctVariants.standardParticleTexture(style)));
            overlay[style] = texture(json, "overlay_" + style,
                    legacyBlockTexture(HbmFluidDuctVariants.standardOverlayTexture(style)));
        }
        ResourceLocation particle = texture(json, "particle", base[0]);
        return new Geometry(model, base, overlay, particle, flipV);
    }

    private static ResourceLocation legacyBlockTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/" + name);
    }

    private static ResourceLocation location(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid fluid pipe model '" + key + "': " + value);
        }
        return location;
    }

    private static ResourceLocation texture(JsonObject json, String key, ResourceLocation fallback) {
        String value = GsonHelper.getAsString(json, key, fallback.toString());
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid fluid pipe texture '" + key + "': " + value);
        }
        return location;
    }

    public record Geometry(ResourceLocation model, ResourceLocation[] baseTextures, ResourceLocation[] overlayTextures,
                           ResourceLocation particle, boolean flipV) implements IUnbakedGeometry<Geometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public Set<String> getConfigurableComponentNames() {
            return PART_SET;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            ObjModel obj = loadObj(model);
            FluidPipeBakedModel.LayerModels[] styles =
                    new FluidPipeBakedModel.LayerModels[HbmFluidDuctVariants.STANDARD_STYLE_COUNT];
            for (int style = 0; style < styles.length; style++) {
                Material baseMaterial = material(baseTextures[style]);
                Material overlayMaterial = material(overlayTextures[style]);
                Map<String, BakedModel> base = new HashMap<>();
                Map<String, BakedModel> overlay = new HashMap<>();
                for (String part : PARTS) {
                    base.put(part, bakePart(obj, context, baker, spriteGetter, modelState, overrides,
                            modelLocation, part, baseMaterial));
                    overlay.put(part, bakePart(obj, context, baker, spriteGetter, modelState, overrides,
                            modelLocation, part, overlayMaterial));
                }
                styles[style] = new FluidPipeBakedModel.LayerModels(Map.copyOf(base), Map.copyOf(overlay));
            }
            TextureAtlasSprite particleSprite = spriteGetter.apply(material(particle));
            return new FluidPipeBakedModel(styles, particleSprite, context.getTransforms());
        }

        private ObjModel loadObj(ResourceLocation location) {
            try {
                return ObjLoader.INSTANCE.loadModel(new ObjModel.ModelSettings(location, false, false, flipV,
                        false, null));
            } catch (Exception exception) {
                throw new RuntimeException("Failed to load fluid pipe OBJ model: " + location, exception);
            }
        }

        private static BakedModel bakePart(ObjModel model, IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation, String visiblePart, Material material) {
            return model.bake(new PartBakingContext(context, visiblePart, material), baker, spriteGetter,
                    modelState, overrides, modelLocation);
        }

        private static Material material(ResourceLocation texture) {
            return new Material(InventoryMenu.BLOCK_ATLAS, texture);
        }
    }

    private static final class PartBakingContext implements IGeometryBakingContext {
        private final IGeometryBakingContext parent;
        private final String visiblePart;
        private final Material material;

        private PartBakingContext(IGeometryBakingContext parent, String visiblePart, Material material) {
            this.parent = parent;
            this.visiblePart = visiblePart;
            this.material = material;
        }

        @Override
        public String getModelName() {
            return parent.getModelName();
        }

        @Override
        public boolean isGui3d() {
            return parent.isGui3d();
        }

        @Override
        public boolean useBlockLight() {
            return parent.useBlockLight();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return parent.useAmbientOcclusion();
        }

        @Override
        public ItemTransforms getTransforms() {
            return parent.getTransforms();
        }

        @Override
        public Material getMaterial(String name) {
            return material;
        }

        @Override
        public boolean hasMaterial(String name) {
            return true;
        }

        @Override
        public boolean isComponentVisible(String component, boolean fallback) {
            return component.equals(visiblePart) || component.startsWith(visiblePart + "/");
        }

        @Override
        public Transformation getRootTransform() {
            return parent.getRootTransform();
        }

        @Override
        public ResourceLocation getRenderTypeHint() {
            return parent.getRenderTypeHint();
        }

        @Override
        public @NotNull String toString() {
            return parent.toString();
        }
    }
}
