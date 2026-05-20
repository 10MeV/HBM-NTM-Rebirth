package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.ModelEvent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ObjModelLibrary {
    private static final Set<ResourceLocation> MODELS = new LinkedHashSet<>();

    public static final ObjModelPart PRESS_HEAD = directBlockPart("press_head")
            .withRenderType(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS))
            .withLightMultiplier(0.82F)
            .withOrigin(ObjPartTransform.BLOCK_CENTER.withScale(0.99F, 1.0F, 0.99F));
    public static final ObjAnimatedModel PRESS = new ObjAnimatedModel()
            .part("Head", PRESS_HEAD);

    public static final ObjModelPart CAGE_LAMP = directBlockPart("legacy/cage_lamp_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLUORESCENT_LAMP_SINGLE = directBlockPart("legacy/fluorescent_lamp_single_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOOD_LAMP = directBlockPart("legacy/flood_lamp_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_BASE = directBlockPart("legacy/floodlight_base_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_LIGHTS = directBlockPart("legacy/floodlight_lights_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart FLOODLIGHT_LAMPS = directBlockPart("legacy/floodlight_lamps_render")
            .withOrigin(ObjPartTransform.IDENTITY);
    public static final ObjModelPart DEMON_LAMP = directBlockPart("lamp_demon")
            .withOrigin(ObjPartTransform.IDENTITY);

    public static ObjModelPart blockPart(String name) {
        return blockPart(name, RenderType.cutout());
    }

    public static ObjModelPart blockPart(String name, RenderType renderType) {
        return new ObjModelPart(blockModel(name), renderType);
    }

    public static ObjModelPartBuilder blockCenteredPart(String name) {
        return blockPartBuilder(name, RenderType.cutout());
    }

    public static ObjModelPartBuilder blockPartBuilder(String name, RenderType renderType) {
        return new ObjModelPartBuilder(blockModel(name), renderType);
    }

    public static ObjModelPartBuilder directBlockPart(String name) {
        return blockCenteredPart(name).direct();
    }

    public static ObjModelPart trinketPart(String name, RenderType renderType) {
        return blockPart("trinkets/" + name, renderType);
    }

    public static ObjModelPart machinePart(String name) {
        return machinePart(name, RenderType.cutout());
    }

    public static ObjModelPart machinePart(String name, RenderType renderType) {
        return blockPart("machines/" + name, renderType);
    }

    public static ObjModelPartBuilder machinePartBuilder(String name, RenderType renderType) {
        return blockPartBuilder("machines/" + name, renderType);
    }

    public static ObjModelPartBuilder directMachinePart(String name) {
        return machinePartBuilder(name, RenderType.cutout()).direct();
    }

    public static ResourceLocation blockModel(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
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

    public record ObjModelPartBuilder(ResourceLocation modelLocation, RenderType renderType, float lightMultiplier, boolean directRender) {
        public ObjModelPartBuilder(ResourceLocation modelLocation, RenderType renderType) {
            this(modelLocation, renderType, 1.0F, false);
        }

        public ObjModelPartBuilder withRenderType(RenderType renderType) {
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
