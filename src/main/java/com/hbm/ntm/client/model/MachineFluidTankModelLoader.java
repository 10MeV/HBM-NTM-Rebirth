package com.hbm.ntm.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.NotNull;

public class MachineFluidTankModelLoader
        implements IGeometryLoader<MachineFluidTankModelLoader.FluidTankGeometry> {
    private static final Set<String> PARTS = Set.of("Frame", "Tank", "TankInner");

    @Override
    public FluidTankGeometry read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation normalModel = requiredLocation(json, "model");
        ResourceLocation explodedModel = requiredLocation(json, "exploded_model");
        boolean flipV = GsonHelper.getAsBoolean(json, "flip_v", true);
        return new FluidTankGeometry(normalModel, explodedModel, flipV);
    }

    private static ResourceLocation requiredLocation(JsonObject json, String key) {
        String value = GsonHelper.getAsString(json, key);
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new JsonParseException("Invalid fluid tank model resource location for '" + key + "': " + value);
        }
        return location;
    }

    public record FluidTankGeometry(ResourceLocation normalModel, ResourceLocation explodedModel, boolean flipV)
            implements IUnbakedGeometry<FluidTankGeometry> {
        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
        }

        @Override
        public Set<String> getConfigurableComponentNames() {
            return PARTS;
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation) {
            ObjModel normal = loadObj(normalModel);
            ObjModel exploded = loadObj(explodedModel);
            Map<String, BakedModel> parts = new HashMap<>();
            parts.put(MachineFluidTankBakedModel.NORMAL_FRAME,
                    bakePart(normal, context, baker, spriteGetter, modelState, overrides, modelLocation,
                            "Frame", "frame"));
            parts.put(MachineFluidTankBakedModel.NORMAL_TANK,
                    bakePart(normal, context, baker, spriteGetter, modelState, overrides, modelLocation,
                            "Tank", "tank"));
            parts.put(MachineFluidTankBakedModel.EXPLODED_FRAME,
                    bakePart(exploded, context, baker, spriteGetter, modelState, overrides, modelLocation,
                            "Frame", "frame"));
            parts.put(MachineFluidTankBakedModel.EXPLODED_TANK_INNER,
                    bakePart(exploded, context, baker, spriteGetter, modelState, overrides, modelLocation,
                            "TankInner", "inner"));
            parts.put(MachineFluidTankBakedModel.EXPLODED_TANK,
                    bakePart(exploded, context, baker, spriteGetter, modelState, overrides, modelLocation,
                            "Tank", "tank"));
            return new MachineFluidTankBakedModel(parts, context.getTransforms());
        }

        private ObjModel loadObj(ResourceLocation location) {
            try {
                return ObjLoader.INSTANCE.loadModel(
                        new ObjModel.ModelSettings(location, false, true, flipV, true, null));
            } catch (Exception exception) {
                throw new RuntimeException("Failed to load fluid tank OBJ model: " + location, exception);
            }
        }

        private static BakedModel bakePart(ObjModel model, IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
                ResourceLocation modelLocation, String visiblePart, String materialKey) {
            return model.bake(new PartBakingContext(context, visiblePart, materialKey),
                    baker, spriteGetter, modelState, overrides, modelLocation);
        }
    }

    private static final class PartBakingContext implements IGeometryBakingContext {
        private final IGeometryBakingContext parent;
        private final String visiblePart;
        private final String materialKey;

        private PartBakingContext(IGeometryBakingContext parent, String visiblePart, String materialKey) {
            this.parent = parent;
            this.visiblePart = visiblePart;
            this.materialKey = materialKey;
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
            if (parent.hasMaterial(materialKey)) {
                return parent.getMaterial(materialKey);
            }
            if (parent.hasMaterial("default")) {
                return parent.getMaterial("default");
            }
            return parent.getMaterial(name);
        }

        @Override
        public boolean hasMaterial(String name) {
            return parent.hasMaterial(name) || parent.hasMaterial(materialKey) || parent.hasMaterial("default");
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
