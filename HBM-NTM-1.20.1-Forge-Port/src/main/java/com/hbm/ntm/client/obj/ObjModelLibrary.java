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
    public static final ObjModelPart MACHINE_FIREBOX = ObjMachineModels.FIREBOX;
    public static final ObjModelPart MACHINE_OILBURNER = ObjMachineModels.OILBURNER;
    public static final ObjModelPart MACHINE_ELECTRIC_HEATER = ObjMachineModels.ELECTRIC_HEATER;
    public static final ObjModelPart MACHINE_HEATEX = ObjMachineModels.HEATEX;
    public static final ObjModelPart MACHINE_BOILER = ObjMachineModels.BOILER;
    public static final ObjModelPart MACHINE_BOILER_BURST = ObjMachineModels.BOILER_BURST;
    public static final ObjModelPart MACHINE_INDUSTRIAL_BOILER = ObjMachineModels.INDUSTRIAL_BOILER;
    public static final ObjModelPart MACHINE_HEPHAESTUS = ObjMachineModels.HEPHAESTUS;
    public static final ObjModelPart MACHINE_DERRICK = ObjMachineModels.DERRICK;
    public static final ObjModelPart MACHINE_PUMPJACK = ObjMachineModels.PUMPJACK;
    public static final ObjModelPart MACHINE_FRACKING_TOWER = ObjMachineModels.FRACKING_TOWER;
    public static final ObjModelPart MACHINE_OILFLARE = ObjMachineModels.OILFLARE;
    public static final ObjModelPart MACHINE_CHIMNEY_INDUSTRIAL = ObjMachineModels.CHIMNEY_INDUSTRIAL;
    public static final ObjModelPart MACHINE_BATTERY_REDD = ObjMachineModels.BATTERY_REDD;
    public static final ObjModelPart MACHINE_FENSU = ObjMachineModels.FENSU;
    public static final ObjPartModel MACHINE_ASSEMBLY_MACHINE = ObjMachineModels.ASSEMBLY_MACHINE;

    public static final ObjModelPart NETWORK_CONNECTOR = ObjNetworkModels.CONNECTOR;
    public static final ObjModelPart NETWORK_CONNECTOR_SUPER = ObjNetworkModels.CONNECTOR_SUPER;
    public static final ObjModelPart NETWORK_FLUID_DIODE = ObjNetworkModels.FLUID_DIODE;
    public static final ObjModelPart NETWORK_PIPE_ANCHOR = ObjNetworkModels.PIPE_ANCHOR;
    public static final ObjModelPart NETWORK_PYLON_LARGE = ObjNetworkModels.PYLON_LARGE;
    public static final ObjModelPart NETWORK_PYLON_MEDIUM = ObjNetworkModels.PYLON_MEDIUM;
    public static final ObjModelPart NETWORK_SUBSTATION = ObjNetworkModels.SUBSTATION;

    public static final ObjModelPart DOOR_SILO_HATCH = ObjDoorModels.SILO_HATCH;
    public static final ObjModelPart DOOR_SILO_HATCH_LARGE = ObjDoorModels.SILO_HATCH_LARGE;
    public static final ObjModelPart DOOR_BLAST_DOOR_BASE = ObjDoorModels.BLAST_DOOR_BASE;
    public static final ObjModelPart DOOR_BLAST_DOOR_TOOTH = ObjDoorModels.BLAST_DOOR_TOOTH;
    public static final ObjModelPart DOOR_BLAST_DOOR_SLIDER = ObjDoorModels.BLAST_DOOR_SLIDER;
    public static final ObjModelPart DOOR_BLAST_DOOR_BLOCK = ObjDoorModels.BLAST_DOOR_BLOCK;

    public static final ObjPartModel FUSION_TORUS = ObjFusionModels.TORUS_PARTS;
    public static final ObjPartModel FUSION_KLYSTRON = ObjFusionModels.KLYSTRON_PARTS;
    public static final ObjPartModel FUSION_KLYSTRON_CREATIVE = ObjFusionModels.KLYSTRON_CREATIVE_PARTS;
    public static final ObjPartModel FUSION_BREEDER = ObjFusionModels.BREEDER_PARTS;
    public static final ObjModelPart FUSION_COLLECTOR = ObjFusionModels.COLLECTOR;
    public static final ObjModelPart FUSION_BOILER = ObjFusionModels.BOILER;
    public static final ObjPartModel FUSION_MHDT = ObjFusionModels.MHDT_PARTS;
    public static final ObjModelPart FUSION_COUPLER = ObjFusionModels.COUPLER;
    public static final ObjPartModel FUSION_PLASMA_FORGE = ObjFusionModels.PLASMA_FORGE_PARTS;

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

    public static ObjModelPart networkPart(String name) {
        return ObjNetworkModels.part(name);
    }

    public static ObjModelPart doorPart(String name) {
        return ObjDoorModels.part(name);
    }

    public static ObjModelPart fusionPart(String name) {
        return ObjFusionModels.part(name);
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
