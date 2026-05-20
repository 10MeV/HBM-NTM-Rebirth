package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ObjModelLibrary {
    private static final Set<ResourceLocation> MODELS = new LinkedHashSet<>();

    public static final ObjModelPart PRESS_HEAD = blockCenteredPart("press_head")
            .withRenderType(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS))
            .withLightMultiplier(0.82F)
            .direct()
            .withOrigin(ObjPartTransform.BLOCK_CENTER.withScale(0.99F, 1.0F, 0.99F));
    public static final ObjAnimatedModel PRESS = new ObjAnimatedModel()
            .part("Head", PRESS_HEAD);

    public static final ObjModelPart CAGE_LAMP = blockCenteredPart("legacy/cage_lamp_render")
            .direct()
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLUORESCENT_LAMP_SINGLE = blockCenteredPart("legacy/fluorescent_lamp_single_render")
            .direct()
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOOD_LAMP = blockCenteredPart("legacy/flood_lamp_render")
            .direct()
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_BASE = blockCenteredPart("legacy/floodlight_base_render")
            .direct()
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_LIGHTS = blockCenteredPart("legacy/floodlight_lights_render")
            .direct()
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_LAMPS = blockCenteredPart("legacy/floodlight_lamps_render")
            .direct()
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart DEMON_LAMP = blockCenteredPart("lamp_demon")
            .direct()
            .withOrigin(ObjPartTransform.IDENTITY);

    public static ObjModelPart blockPart(String name) {
        return new ObjModelPart(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name), net.minecraft.client.renderer.RenderType.cutout());
    }

    public static ObjModelPartBuilder blockCenteredPart(String name) {
        return new ObjModelPartBuilder(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name), net.minecraft.client.renderer.RenderType.cutout());
    }

    static void register(ResourceLocation modelLocation) {
        MODELS.add(modelLocation);
    }

    public static Set<ResourceLocation> models() {
        return Collections.unmodifiableSet(MODELS);
    }

    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (ResourceLocation model : MODELS) {
            event.register(model);
        }
    }

    private ObjModelLibrary() {
    }

    public record ObjModelPartBuilder(ResourceLocation modelLocation, net.minecraft.client.renderer.RenderType renderType, float lightMultiplier, boolean directRender) {
        public ObjModelPartBuilder(ResourceLocation modelLocation, net.minecraft.client.renderer.RenderType renderType) {
            this(modelLocation, renderType, 1.0F, false);
        }

        public ObjModelPartBuilder withRenderType(net.minecraft.client.renderer.RenderType renderType) {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, directRender);
        }

        public ObjModelPartBuilder withLightMultiplier(float lightMultiplier) {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, directRender);
        }

        public ObjModelPartBuilder direct() {
            return new ObjModelPartBuilder(modelLocation, renderType, lightMultiplier, true);
        }

        public ObjModelPart withOrigin(ObjPartTransform transform) {
            return new ObjModelPart(modelLocation, renderType, transform, lightMultiplier, directRender);
        }
    }
}
