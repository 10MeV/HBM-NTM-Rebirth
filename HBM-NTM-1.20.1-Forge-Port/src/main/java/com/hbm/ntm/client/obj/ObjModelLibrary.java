package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ObjModelLibrary {
    private static final Set<ResourceLocation> MODELS = new LinkedHashSet<>();

    public static final ObjModelPart PRESS_HEAD = ObjMachineModels.PRESS_HEAD;
    public static final ObjPartModel PRESS = ObjMachineModels.PRESS;
    public static final ObjModelPart MACHINE_RADAR_SCREEN = ObjMachineModels.RADAR_SCREEN;
    public static final ObjModelPart MACHINE_SOLAR_MIRROR = ObjMachineModels.SOLAR_MIRROR;
    public static final ObjModelPart MACHINE_DRAIN = ObjMachineModels.DRAIN;
    public static final ObjModelPart MACHINE_INTAKE = ObjMachineModels.INTAKE;
    public static final ObjModelPart MACHINE_COMBINATION_OVEN = ObjMachineModels.COMBINATION_OVEN;
    public static final ObjModelPart MACHINE_RTG = ObjMachineModels.RTG;
    public static final ObjModelPart MACHINE_TELEX = ObjMachineModels.TELEX;
    public static final ObjModelPart MACHINE_FRACTION_TOWER = ObjMachineModels.FRACTION_TOWER;
    public static final ObjModelPart MACHINE_FAN = ObjMachineModels.FAN;
    public static final ObjModelPart MACHINE_FURNACE_IRON = ObjMachineModels.FURNACE_IRON;
    public static final ObjModelPart MACHINE_ELEVATOR = ObjMachineModels.ELEVATOR;
    public static final ObjModelPart MACHINE_CRUCIBLE = ObjMachineModels.CRUCIBLE;
    public static final ObjModelPart MACHINE_DRUM = ObjMachineModels.DRUM;
    public static final ObjModelPart MACHINE_FRACTION_SPACER = ObjMachineModels.FRACTION_SPACER;
    public static final ObjModelPart MACHINE_HEATING_OVEN = ObjMachineModels.HEATING_OVEN;
    public static final ObjModelPart MACHINE_CHIMNEY_BRICK = ObjMachineModels.CHIMNEY_BRICK;
    public static final ObjModelPart MACHINE_TURBINE = ObjMachineModels.TURBINE;
    public static final ObjModelPart MACHINE_DIESELGEN = ObjMachineModels.DIESELGEN;

    public static final ObjModelPart CAGE_LAMP = ObjLightModels.CAGE_LAMP;
    public static final ObjModelPart FLUORESCENT_LAMP_SINGLE = ObjLightModels.FLUORESCENT_LAMP_SINGLE;
    public static final ObjModelPart FLOOD_LAMP = ObjLightModels.FLOOD_LAMP;
    public static final ObjModelPart FLOODLIGHT_BASE = ObjLightModels.FLOODLIGHT_BASE;
    public static final ObjModelPart FLOODLIGHT_LIGHTS = ObjLightModels.FLOODLIGHT_LIGHTS;
    public static final ObjModelPart FLOODLIGHT_LAMPS = ObjLightModels.FLOODLIGHT_LAMPS;
    public static final ObjModelPart DEMON_LAMP = ObjLightModels.DEMON_LAMP;

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
        return ObjTrinketModels.part(name, renderType);
    }

    public static ObjModelPart machinePart(String name) {
        return ObjMachineModels.part(name);
    }

    public static ObjModelPart machinePart(String name, RenderType renderType) {
        return ObjMachineModels.part(name, renderType);
    }

    public static ObjModelPartBuilder machinePartBuilder(String name, RenderType renderType) {
        return ObjMachineModels.partBuilder(name, renderType);
    }

    public static ObjModelPartBuilder directMachinePart(String name) {
        return ObjMachineModels.directPart(name);
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
