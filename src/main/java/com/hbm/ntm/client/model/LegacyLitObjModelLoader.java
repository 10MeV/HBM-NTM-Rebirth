package com.hbm.ntm.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import net.minecraftforge.client.model.obj.ObjTokenizer;
import org.joml.Matrix4f;

/**
 * Forge OBJ delegate that removes cardinal-direction flat shading and restores the
 * continuous 1.7.10 standard-light contribution from each source OBJ normal.
 */
public final class LegacyLitObjModelLoader
        implements IGeometryLoader<LegacyLitObjModelLoader.LegacyLitObjGeometry> {
    @Override
    public LegacyLitObjGeometry read(JsonObject json, JsonDeserializationContext context)
            throws JsonParseException {
        String normalModeName = json.has("legacy_normal_mode")
                ? json.get("legacy_normal_mode").getAsString()
                : "corner";
        NormalMode normalMode = switch (normalModeName) {
            case "corner" -> NormalMode.CORNER;
            case "face" -> NormalMode.FACE;
            default -> throw new JsonParseException(
                    "legacy_normal_mode must be either 'corner' or 'face', got '"
                            + normalModeName + "'");
        };
        String legacyDefaultMaterial = json.has("legacy_default_material")
                ? json.get("legacy_default_material").getAsString().trim()
                : "";
        boolean legacyDoubleSided = false;
        if (json.has("legacy_double_sided")) {
            var value = json.get("legacy_double_sided");
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
                throw new JsonParseException("legacy_double_sided must be a boolean");
            }
            legacyDoubleSided = value.getAsBoolean();
        }
        JsonObject delegatedJson = json.deepCopy();
        delegatedJson.remove("legacy_normal_mode");
        delegatedJson.remove("legacy_default_material");
        delegatedJson.remove("legacy_double_sided");
        delegatedJson.addProperty("shade_quads", false);
        delegatedJson.addProperty("emissive_ambient", false);
        ObjModel model = legacyDefaultMaterial.isEmpty()
                ? ObjLoader.INSTANCE.read(delegatedJson, context)
                : readWithLegacyDefaultMaterial(delegatedJson, legacyDefaultMaterial);
        return new LegacyLitObjGeometry(model, normalMode, legacyDoubleSided);
    }

    /**
     * Forge 47.2.32 intentionally drops every {@code ObjModel.ModelMesh} whose material is null.
     * Some source-authentic HBM OBJ files, including {@code catalytic_reformer.obj}, contain no
     * {@code mtllib}/{@code usemtl} statements because 1.7.10 bound their texture externally.
     * Prefixing one parser-only {@code usemtl} line preserves the source OBJ byte-for-byte while
     * giving Forge's baked path the explicit material it requires.
     */
    private static ObjModel readWithLegacyDefaultMaterial(JsonObject json, String materialName) {
        if (!json.has("model")) {
            throw new JsonParseException("legacy_lit_obj requires a 'model' key");
        }
        if (!json.has("mtl_override")) {
            throw new JsonParseException(
                    "legacy_default_material requires an explicit 'mtl_override'");
        }
        ResourceLocation modelLocation = new ResourceLocation(json.get("model").getAsString());
        ObjModel.ModelSettings settings = new ObjModel.ModelSettings(
                modelLocation,
                !json.has("automatic_culling") || json.get("automatic_culling").getAsBoolean(),
                !json.has("shade_quads") || json.get("shade_quads").getAsBoolean(),
                json.has("flip_v") && json.get("flip_v").getAsBoolean(),
                !json.has("emissive_ambient") || json.get("emissive_ambient").getAsBoolean(),
                json.get("mtl_override").getAsString());
        byte[] prefix = ("usemtl " + materialName + "\n").getBytes(StandardCharsets.UTF_8);
        try {
            InputStream source = Minecraft.getInstance().getResourceManager()
                    .getResource(modelLocation)
                    .orElseThrow(() -> new JsonParseException("Missing OBJ model " + modelLocation))
                    .open();
            try (ObjTokenizer tokenizer = new ObjTokenizer(new SequenceInputStream(
                    new ByteArrayInputStream(prefix), source))) {
                return ObjModel.parse(tokenizer, settings);
            }
        } catch (JsonParseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new JsonParseException("Unable to read material-less OBJ " + modelLocation, exception);
        }
    }

    enum NormalMode {
        CORNER,
        FACE
    }

    public record LegacyLitObjGeometry(ObjModel model, NormalMode normalMode, boolean doubleSided)
            implements IUnbakedGeometry<LegacyLitObjGeometry> {
        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                ItemOverrides overrides, ResourceLocation modelLocation) {
            return new LegacyLitObjBakedModel(
                    model.bake(context, baker, spriteGetter, modelState, overrides, modelLocation),
                    normalMode == NormalMode.FACE,
                    hasNegativeDeterminant(context, modelState),
                    doubleSided);
        }

        private static boolean hasNegativeDeterminant(IGeometryBakingContext context, ModelState modelState) {
            var rootTransform = context.getRootTransform();
            var transform = rootTransform.isIdentity()
                    ? modelState.getRotation()
                    : modelState.getRotation().compose(rootTransform);
            Matrix4f matrix = transform.getMatrix();
            float determinant = matrix.m00() * (matrix.m11() * matrix.m22() - matrix.m12() * matrix.m21())
                    - matrix.m10() * (matrix.m01() * matrix.m22() - matrix.m02() * matrix.m21())
                    + matrix.m20() * (matrix.m01() * matrix.m12() - matrix.m02() * matrix.m11());
            return Float.isFinite(determinant) && determinant < 0.0F;
        }

        @Override
        public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter,
                IGeometryBakingContext context) {
            model.resolveParents(modelGetter, context);
        }

        @Override
        public Set<String> getConfigurableComponentNames() {
            return model.getConfigurableComponentNames();
        }
    }
}
