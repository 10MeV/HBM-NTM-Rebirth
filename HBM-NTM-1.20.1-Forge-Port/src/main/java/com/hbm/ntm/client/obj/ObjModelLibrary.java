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
            .withOrigin(ObjPartTransform.BLOCK_CENTER.withScale(0.99F, 1.0F, 0.99F));
    public static final ObjAnimatedModel PRESS = new ObjAnimatedModel()
            .part("Head", PRESS_HEAD);

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

    public record ObjModelPartBuilder(ResourceLocation modelLocation, net.minecraft.client.renderer.RenderType renderType) {
        public ObjModelPart withOrigin(ObjPartTransform transform) {
            return new ObjModelPart(modelLocation, renderType, transform);
        }
    }
}
