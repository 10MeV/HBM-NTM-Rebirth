package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CableDiodeBlock;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidDuctGaugeBlock;
import com.hbm.ntm.block.FluidDuctPaintableBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.block.FluidValveBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.block.ICFAssembledBlock;
import com.hbm.ntm.block.LegacyChargeBlock;
import com.hbm.ntm.block.LegacyChainBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacySellafieldOreBlock;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.block.LegacyNtmGlassPaneBlock;
import com.hbm.ntm.block.MassStorageBlock;
import com.hbm.ntm.block.PileGraphiteDrilledBaseBlock;
import com.hbm.ntm.block.PoweredRedCableBlock;
import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.block.RedCableGaugeBlock;
import com.hbm.ntm.block.SteelScaffoldBlock;
import com.hbm.ntm.block.VendingMachineBlock;
import com.hbm.ntm.block.WatzEndBlock;
import com.hbm.ntm.block.conveyor.ConveyorBlock;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class HbmBlockStateProvider extends BlockStateProvider {
    public HbmBlockStateProvider(net.minecraft.data.PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        existingModelWithItemNoRotation(ModBlocks.MACHINE_PRESS, "machine_press");
        cubeWithItem(ModBlocks.PRESS_PREHEATER, "press_preheater");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_EPRESS, "machines/epress");
        difurnaceWithItem(ModBlocks.MACHINE_DIFURNACE_OFF);
        sidedCubeWithItem(ModBlocks.MACHINE_ELECTRIC_FURNACE_OFF,
                "machine_electric_furnace_bottom",
                "machine_electric_furnace_top",
                "machine_electric_furnace_side",
                "machine_electric_furnace_front_off",
                "machine_electric_furnace_side",
                "machine_electric_furnace_side");
        sidedCubeWithItem(ModBlocks.MACHINE_BOILER_OFF,
                "machine_boiler_base",
                "machine_boiler_base",
                "machine_boiler_side",
                "machine_boiler_front",
                "machine_boiler_side",
                "machine_boiler_side");
        horizontalBlockNoRotationWithItem(ModBlocks.MACHINE_SHREDDER,
                "machine_shredder_bottom_alt",
                "machine_shredder_top_alt",
                "machine_shredder_front_alt",
                "machine_shredder_front_alt",
                "machine_shredder_side_alt",
                "machine_shredder_side_alt");
        sidedCubeWithItem(ModBlocks.MACHINE_TURBINE,
                "machine_turbine_top",
                "machine_turbine_top",
                "machine_turbine_base",
                "machine_turbine_base",
                "machine_turbine_base",
                "machine_turbine_base");
        simpleCubeWithItem(ModBlocks.MACHINE_CONDENSER, "machine_condenser");
        simpleSidedCubeWithItem(ModBlocks.DECON,
                "decon_side",
                "decon_top",
                "decon_side",
                "decon_side",
                "decon_side",
                "decon_side");
        simpleSidedCubeWithItem(ModBlocks.MACHINE_ARMOR_TABLE,
                "armor_table_bottom",
                "armor_table_top",
                "armor_table_side",
                "armor_table_side",
                "armor_table_side",
                "armor_table_side");
        hiddenBerBlockWithItem(ModBlocks.FAN);
        hiddenBerBlockWithItem(ModBlocks.FILING_CABINET);
        simpleSidedCubeWithItem(ModBlocks.MACHINE_WEAPON_TABLE,
                "gun_table_bottom",
                "gun_table_top",
                "gun_table_side",
                "gun_table_side",
                "gun_table_side",
                "gun_table_side");
        redCableWithItem();
        redCableClassicWithItem();
        simpleBlockWithItem(ModBlocks.RED_WIRE_COATED.get(), models().cubeAll("red_wire_coated",
                new ResourceLocation(HbmNtm.MOD_ID, "block/red_wire_coated")));
        redCableBoxWithItem();
        redCableGaugeWithItem();
        poweredRedCableWithItem(ModBlocks.CABLE_SWITCH, "cable_switch_off", "cable_switch_on");
        poweredRedCableWithItem(ModBlocks.CABLE_DETECTOR, "cable_detector_off", "cable_detector_on");
        cableDiodeWithItem();
        pylonWithItemRenderer(ModBlocks.RED_CONNECTOR, "network/connector");
        pylonWithItemRenderer(ModBlocks.RED_CONNECTOR_SUPER, "network/connector_super");
        pylonWithItemRenderer(ModBlocks.RED_PYLON, "network/model_pylon");
        pylonWithItemRenderer(ModBlocks.RED_PYLON_MEDIUM_WOOD, "network/pylon_medium");
        pylonWithItemRenderer(ModBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER, "network/pylon_medium");
        pylonWithItemRenderer(ModBlocks.RED_PYLON_MEDIUM_STEEL, "network/pylon_medium_steel");
        pylonWithItemRenderer(ModBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER, "network/pylon_medium_steel");
        pylonWithItemRenderer(ModBlocks.RED_PYLON_LARGE, "network/pylon_large");
        pylonWithItemRenderer(ModBlocks.SUBSTATION, "network/substation");
        fluidPipeWithItem();
        fluidDuctBoxWithItem(ModBlocks.FLUID_DUCT_BOX);
        fluidDuctGaugeWithItem();
        fluidDuctExhaustWithItem();
        fluidDuctPaintableWithItem(ModBlocks.FLUID_DUCT_PAINTABLE, "fluid_duct_paintable");
        fluidDuctPaintableWithItem(ModBlocks.FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST,
                "fluid_duct_paintable_block_exhaust");
        fluidPipeAnchorWithItem();
        fluidBarrelWithItem(ModBlocks.BARREL_PLASTIC, "barrel_plastic");
        fluidBarrelWithItem(ModBlocks.BARREL_CORRODED, "barrel_corroded");
        fluidBarrelWithItem(ModBlocks.BARREL_STEEL, "barrel_steel");
        fluidBarrelWithItem(ModBlocks.BARREL_TCALLOY, "barrel_tcalloy");
        fluidBarrelWithItem(ModBlocks.BARREL_ANTIMATTER, "barrel_antimatter");
        fluidValveWithItem(ModBlocks.FLUID_VALVE, "fluid_valve_off", "fluid_valve_on");
        fluidValveWithItem(ModBlocks.FLUID_SWITCH, "fluid_switch_off", "fluid_switch_on");
        fluidValveWithItem(ModBlocks.FLUID_COUNTER_VALVE, "fluid_counter_valve_off", "fluid_counter_valve_on");
        fluidPumpWithItem();
        conveyorWithItem(ModBlocks.CONVEYOR, "conveyor");
        conveyorWithItem(ModBlocks.CONVEYOR_EXPRESS, "conveyor_express");
        conveyorWithItem(ModBlocks.CONVEYOR_DOUBLE, "conveyor_double");
        conveyorWithItem(ModBlocks.CONVEYOR_TRIPLE, "conveyor_triple");
        verticalConveyorWithItem(ModBlocks.CONVEYOR_LIFT, "conveyor");
        verticalConveyorWithItem(ModBlocks.CONVEYOR_CHUTE, "conveyor");
        sidedCubeWithItem(ModBlocks.MACHINE_BATTERY,
                "battery_top",
                "battery_top",
                "battery_side_alt",
                "battery_front_alt",
                "battery_side_alt",
                "battery_side_alt");
        sidedCubeWithItem(ModBlocks.MACHINE_BATTERY_POTATO,
                "battery_potato_top",
                "battery_potato_top",
                "battery_potato_side",
                "battery_potato_front",
                "battery_potato_side",
                "battery_potato_side");
        sidedCubeWithItem(ModBlocks.MACHINE_LITHIUM_BATTERY,
                "battery_lithium_top",
                "battery_lithium_top",
                "battery_lithium_side",
                "battery_lithium_front",
                "battery_lithium_side",
                "battery_lithium_side");
        sidedCubeWithItem(ModBlocks.MACHINE_SCHRABIDIUM_BATTERY,
                "battery_schrabidium_top",
                "battery_schrabidium_top",
                "battery_schrabidium_side",
                "battery_schrabidium_front",
                "battery_schrabidium_side",
                "battery_schrabidium_side");
        sidedCubeWithItem(ModBlocks.MACHINE_DINEUTRONIUM_BATTERY,
                "battery_dineutronium_top",
                "battery_dineutronium_top",
                "battery_dineutronium_side",
                "battery_dineutronium_front",
                "battery_dineutronium_side",
                "battery_dineutronium_side");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FENSU, "machines/fensu");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_BATTERY_REDD, "machines/fensu2");
        existingModelWithCustomItem(ModBlocks.MACHINE_BATTERY_SOCKET, "machines/battery_socket_socket");
        storageCrateWithItem(ModBlocks.CRATE_IRON, "crate_iron");
        storageCrateWithItem(ModBlocks.CRATE_STEEL, "crate_steel");
        storageCrateWithItem(ModBlocks.CRATE_DESH, "crate_desh");
        storageCrateWithItem(ModBlocks.CRATE_TUNGSTEN, "crate_tungsten");
        safeWithItem();
        massStorageWithItem();
        existingModelWithCustomItem(ModBlocks.MACHINE_RADAR, "machines/radar");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RADAR_LARGE, "machines/radar_large");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RADAR_SCREEN, "machines/radar_screen");
        vendingMachineWithItemRenderer();
        simpleSidedCubeWithItem(ModBlocks.MACHINE_TELEPORTER,
                "teleporter_bottom",
                "teleporter_top",
                "teleporter_side",
                "teleporter_side",
                "teleporter_side",
                "teleporter_side");
        simpleSidedCubeWithItem(ModBlocks.MACHINE_SATLINKER,
                "machine_satlinker_side",
                "machine_satlinker_top",
                "machine_satlinker_side",
                "machine_satlinker_side",
                "machine_satlinker_side",
                "machine_satlinker_side");
        simpleSidedCubeWithItem(ModBlocks.MACHINE_TRANSFORMER,
                "machine_transformer_top_iron",
                "machine_transformer_top_iron",
                "machine_transformer_iron",
                "machine_transformer_iron",
                "machine_transformer_iron",
                "machine_transformer_iron");
        visibleMachineWithItemRenderer(ModBlocks.SAT_DOCK, "utility/sat_dock");
        soyuzCapsuleWithItem();
        visibleMachineWithItemRenderer(ModBlocks.SOYUZ_LAUNCHER, "launch_table/soyuz_launcher_table");
        simpleCubeWithItem(ModBlocks.STRUCT_LAUNCHER, "struct_launcher");
        simpleCubeWithItem(ModBlocks.STRUCT_SCAFFOLD, "struct_scaffold");
        simpleCubeWithItem(ModBlocks.STRUCT_SOYUZ_CORE, "struct_soyuz_core");
        existingModelWithItem(ModBlocks.LAUNCH_PAD, "launch_pad");
        existingModelWithCustomItem(ModBlocks.LAUNCH_PAD_RUSTED, "launch_pad");
        existingModelWithCustomItem(ModBlocks.LAUNCH_TABLE, "launch_table/launch_table_base");
        existingModelWithCustomItem(ModBlocks.COMPACT_LAUNCHER, "launch_table/compact_launcher");
        existingModelWithCustomItem(ModBlocks.MACHINE_MISSILE_ASSEMBLY, "machine_missile_assembly");
        existingModelWithItem(ModBlocks.RBMK_DISPLAY_BLANK, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_DISPLAY, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_GAUGE, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_GRAPH, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_INDICATOR, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_KEY_PAD, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_LEVER, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_NUMITRON, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_TERMINAL, "rbmk_panel_base");
        rbmkColumnWithItem(ModBlocks.RBMK_BLANK, "rbmk_blank");
        rbmkColumnWithItem(ModBlocks.RBMK_MODERATOR, "rbmk_moderator");
        rbmkColumnWithItem(ModBlocks.RBMK_REFLECTOR, "rbmk_reflector");
        rbmkColumnWithItem(ModBlocks.RBMK_ABSORBER, "rbmk_absorber");
        rbmkColumnWithItem(ModBlocks.RBMK_ROD, "rbmk_element");
        rbmkColumnWithItem(ModBlocks.RBMK_ROD_MOD, "rbmk_element_mod");
        rbmkColumnWithItem(ModBlocks.RBMK_ROD_REASIM, "rbmk_element_reasim");
        rbmkColumnWithItem(ModBlocks.RBMK_ROD_REASIM_MOD, "rbmk_element_reasim_mod");
        rbmkColumnWithItem(ModBlocks.RBMK_BOILER, "rbmk_boiler");
        rbmkColumnWithItem(ModBlocks.RBMK_HEATER, "rbmk_heater");
        rbmkColumnWithItem(ModBlocks.RBMK_COOLER, "rbmk_cooler");
        rbmkColumnWithItem(ModBlocks.RBMK_OUTGASSER, "rbmk_outgasser");
        rbmkColumnWithItem(ModBlocks.RBMK_STORAGE, "rbmk_storage");
        rbmkBerStructureNoRotationWithItem(ModBlocks.RBMK_AUTOLOADER, "rbmk_autoloader");
        rbmkBerStructureWithItem(ModBlocks.RBMK_CONSOLE, "rbmk/rbmk_console");
        rbmkBerStructureWithItem(ModBlocks.RBMK_CRANE_CONSOLE, "rbmk/rbmk_crane_console");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL, "rbmk_control");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_MOD, "rbmk_control_mod");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_AUTO, "rbmk_control_auto");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_REASIM,
                "rbmk_control_reasim", "rbmk_control_reasim_bottom");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_REASIM_AUTO,
                "rbmk_control_reasim_auto", "rbmk_control_reasim_auto_bottom");
        pileGraphiteBlocksWithItems();
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ASSEMBLY_MACHINE, "machines/assembly_machine");
        forceFieldWithItem();
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CHEMICAL_PLANT, "machines/chemical_plant");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_LIQUEFACTOR, "machines/liquefactor");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CHEMICAL_FACTORY, "machines/chemical_factory");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_REFINERY, "machines/refinery");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CATALYTIC_CRACKER, "machines/catalytic_cracker");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CATALYTIC_REFORMER, "machines/catalytic_reformer");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_VACUUM_DISTILL, "machines/vacuum_distill");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FRACTION_TOWER, "machines/fraction_tower");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_HYDROTREATER, "machines/hydrotreater");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COKER, "machines/coker");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PYROOVEN, "machines/pyrooven");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SOLIDIFIER, "machines/solidifier");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COMPRESSOR, "machines/compressor");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_BAT9000, "machines/bat9000");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_BIGASSTANK, "machines/bigasstank");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FLUIDTANK, "machines/fluidtank");
        hiddenBerBlockWithItem(ModBlocks.MACHINE_UF6_TANK);
        hiddenBerBlockWithItem(ModBlocks.MACHINE_PUF6_TANK);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_WELL, "machines/derrick");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PUMPJACK, "machines/pumpjack");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FRACKING_TOWER, "machines/fracking_tower");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CENTRIFUGE, "machines/centrifuge");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_GASCENT, "machines/gascent");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ORE_SLOPPER, "machines/ore_slopper");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SAWMILL, "machines/sawmill");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CRUCIBLE, "machines/crucible");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_GASFLARE, "machines/flare_stack");
        visibleMachineWithItemRenderer(ModBlocks.CHIMNEY_BRICK, "machines/chimney_brick");
        visibleMachineWithItemRenderer(ModBlocks.CHIMNEY_INDUSTRIAL, "machines/chimney_industrial");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_INTAKE, "machines/intake");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_DRAIN, "machines/drain");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CHUNGUS, "machines/chungus");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_HEPHAESTUS, "machines/hephaestus");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_BOILER, "machines/boiler");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_INDUSTRIAL_BOILER, "machines/industrial_boiler");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COMBUSTION_ENGINE, "machines/combustion_engine");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_DIESEL, "machines/dieselgen");
        visibleMachineWithItemRenderer(ModBlocks.PUMP_STEAM, "machines/pump");
        visibleMachineWithItemRenderer(ModBlocks.PUMP_ELECTRIC, "machines/pump_electric");
        visibleMachineWithItemRenderer(ModBlocks.HEATER_HEATEX, "machines/heatex");
        visibleMachineWithItemRenderer(ModBlocks.HEATER_FIREBOX, "machines/firebox");
        visibleMachineWithItemRenderer(ModBlocks.HEATER_OVEN, "machines/heating_oven");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ASHPIT, "machines/heating_oven");
        visibleMachineWithItemRenderer(ModBlocks.HEATER_OILBURNER, "machines/oilburner");
        visibleMachineWithItemRenderer(ModBlocks.HEATER_ELECTRIC, "machines/electric_heater");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CONDENSER_POWERED, "machines/condenser");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COMPRESSOR_COMPACT, "machines/compressor_compact");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_LPW2, "reactors/lpw2");
        visibleMachineWithItemRenderer(ModBlocks.REACTOR_RESEARCH, "reactors/reactor_small_base");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_REACTOR_BREEDING, "reactors/breeder");
        simpleCubeWithItem(ModBlocks.STRUCT_WATZ_CORE, "legacy_blocks/struct_watz_core");
        watzPillarWithItem(ModBlocks.WATZ_ELEMENT, "watz_element");
        watzPillarWithItem(ModBlocks.WATZ_COOLER, "watz_cooler");
        watzEndWithItem();
        simpleCubeWithItem(ModBlocks.STRUCT_TORUS_CORE, "legacy_blocks/struct_torus_core");
        simpleCubeWithItem(ModBlocks.FUSION_COMPONENT_BSCCO, "legacy_blocks/fusion_component");
        simpleCubeWithItem(ModBlocks.FUSION_COMPONENT_BSCCO_WELDED,
                "legacy_blocks/fusion_component.bscco_welded");
        simpleCubeWithItem(ModBlocks.FUSION_COMPONENT_BLANKET, "legacy_blocks/fusion_component.blanket");
        simpleCubeWithItem(ModBlocks.FUSION_COMPONENT_MOTOR, "legacy_blocks/fusion_component.motor");
        simpleCubeWithItem(ModBlocks.STRUCT_ICF_CORE, "legacy_blocks/struct_icf_core");
        simpleCubeWithItem(ModBlocks.ICF_COMPONENT_SCAFFOLD, "legacy_blocks/icf_component");
        simpleCubeWithItem(ModBlocks.ICF_COMPONENT_VESSEL, "legacy_blocks/icf_component.vessel");
        simpleCubeWithItem(ModBlocks.ICF_COMPONENT_VESSEL_WELDED, "legacy_blocks/icf_component.vessel_welded");
        simpleCubeWithItem(ModBlocks.ICF_COMPONENT_STRUCTURE, "legacy_blocks/icf_component.structure");
        simpleCubeWithItem(ModBlocks.ICF_COMPONENT_STRUCTURE_BOLTED,
                "legacy_blocks/icf_component.structure_bolted");
        icfAssembledBlock();
        visibleMachineWithItemRenderer(ModBlocks.CARGO_ELEVATOR, "machines/elevator");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ASSEMBLY_FACTORY, "machines/assembly_factory");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PRECASS, "machines/precass");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PUREX, "machines/purex");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SILEX, "machines/silex");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_EXPOSURE_CHAMBER, "machines/exposure_chamber");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CYCLOTRON, "machines/cyclotron");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CRYSTALLIZER, "machines/acidizer");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ELECTROLYSER, "machines/electrolyser");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ARC_WELDER, "machines/arc_welder");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SOLDERING_STATION, "machines/soldering_station");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_MIXER, "machines/mixer");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RADIOLYSIS, "machines/radiolysis");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RTG_GREY, "machines/rtg");
        sidedCubeWithItem(ModBlocks.MACHINE_MINIRTG,
                "rtg_cell",
                "rtg_cell",
                "rtg_cell",
                "rtg_cell",
                "rtg_cell",
                "rtg_cell");
        sidedCubeWithItem(ModBlocks.MACHINE_POWERRTG,
                "rtg_polonium",
                "rtg_polonium",
                "rtg_polonium",
                "rtg_polonium",
                "rtg_polonium",
                "rtg_polonium");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RADGEN, "machines/radgen");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ROTARY_FURNACE, "machines/rotary_furnace");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STEAM_ENGINE, "machines/steam_engine");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SOLAR_BOILER, "machines/solar_boiler");
        existingModelWithItemNoRotation(ModBlocks.SOLAR_MIRROR, "machines/solar_mirror");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TOWER_SMALL, "machines/tower_small");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TOWER_LARGE, "machines/tower_large");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TURBOFAN, "machines/turbofan");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TURBINEGAS, "machines/turbinegas");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_AMMO_PRESS, "machines/ammo_press");
        visibleMachineWithItemRenderer(ModBlocks.FURNACE_IRON, "machines/furnace_iron");
        visibleMachineWithItemRenderer(ModBlocks.FURNACE_STEEL, "machines/furnace_steel");
        visibleMachineWithItemRenderer(ModBlocks.FURNACE_COMBINATION, "machines/combination_oven");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_BLAST_FURNACE, "machines/blast_furnace");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ARC_FURNACE, "machines/arc_furnace");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ANNIHILATOR, "machines/annihilator");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FEL, "machines/fel");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ORBUS, "machines/orbus");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_MINING_LASER, "machines/mining_laser");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STRAND_CASTER, "machines/strand_caster");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_WOOD_BURNER, "machines/wood_burner");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STIRLING, "machines/stirling");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STIRLING_STEEL, "machines/stirling");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STIRLING_CREATIVE, "machines/stirling");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_DEUTERIUM_TOWER, "machines/machine_deuterium_tower");
        simpleSidedCubeWithItem(ModBlocks.MACHINE_DEUTERIUM_EXTRACTOR,
                "deuterium_extractor_top_water",
                "deuterium_extractor_top_water",
                "deuterium_extractor_side",
                "deuterium_extractor_side",
                "deuterium_extractor_side",
                "deuterium_extractor_side");
        visibleMachineWithItemRenderer(ModBlocks.FRACTION_SPACER, "machines/fraction_spacer");
        simpleSidedCubeWithItem(ModBlocks.TELEANCHOR,
                "tele_anchor_side",
                "tele_anchor_top",
                "tele_anchor_side",
                "tele_anchor_side",
                "tele_anchor_side",
                "tele_anchor_side");
        simpleCubeWithItem(ModBlocks.FIELD_DISTURBER, "field_disturber");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_INDUSTRIAL_TURBINE, "machines/industrial_turbine");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_LARGE_TURBINE, "machines/turbine");
        translucentCubeWithItem(ModBlocks.GLASS_BORON, "glass_boron");
        translucentCubeWithItem(ModBlocks.GLASS_LEAD, "glass_lead");
        translucentCubeWithItem(ModBlocks.GLASS_URANIUM, "glass_uranium");
        translucentCubeWithItem(ModBlocks.GLASS_POLONIUM, "glass_polonium");
        translucentCubeWithItem(ModBlocks.GLASS_QUARTZ, "glass_quartz");
        simpleCubeWithItem(ModBlocks.SAND_BORON, "sand_boron");
        simpleCubeWithItem(ModBlocks.SAND_LEAD, "sand_lead");
        simpleCubeWithItem(ModBlocks.SAND_URANIUM, "sand_uranium");
        simpleCubeWithItem(ModBlocks.SAND_POLONIUM, "sand_polonium");
        simpleCubeWithItem(ModBlocks.SAND_QUARTZ, "sand_quartz");
        simpleCubeWithItem(ModBlocks.MOON_TURF, "moon_turf");
        translucentCubeWithItem(ModBlocks.REINFORCED_LAMINATE, "reinforced_laminate");
        reinforcedLaminatePaneWithItem();
        capBlockWithItem(ModBlocks.BLOCK_CAP_NUKA, "block_cap_nuka");
        capBlockWithItem(ModBlocks.BLOCK_CAP_QUANTUM, "block_cap_quantum");
        capBlockWithItem(ModBlocks.BLOCK_CAP_SPARKLE, "block_cap_sparkle");
        capBlockWithItem(ModBlocks.BLOCK_CAP_RAD, "block_cap_rad");
        capBlockWithItem(ModBlocks.BLOCK_CAP_KORL, "block_cap_korl");
        capBlockWithItem(ModBlocks.BLOCK_CAP_FRITZ, "block_cap_fritz");
        simpleCubeWithItem(ModBlocks.GAS_RADON, "gas_radon");
        simpleCubeWithItem(ModBlocks.GAS_RADON_DENSE, "gas_radon_dense");
        simpleCubeWithItem(ModBlocks.GAS_RADON_TOMB, "gas_radon_tomb");
        simpleCubeWithItem(ModBlocks.GAS_MELTDOWN, "gas_meltdown");
        simpleCubeWithItem(ModBlocks.GAS_MONOXIDE, "gas_monoxide");
        simpleCubeWithItem(ModBlocks.GAS_ASBESTOS, "gas_asbestos");
        simpleCubeWithItem(ModBlocks.GAS_COAL, "gas_coal");
        simpleCubeWithItem(ModBlocks.CHLORINE_GAS, "chlorine_gas");
        simpleCubeWithItem("dirt_dead", "dirt_dead");
        simpleCubeWithItem("dirt_oily", "dirt_oily");
        simpleCubeWithItem("sand_dirty", "sand_dirty");
        simpleCubeWithItem("sand_dirty_red", "sand_dirty_red");
        simpleCubeWithItem("stone_cracked", "stone_cracked");
        radAbsorberWithItem();
        simpleCubeWithItem(ModBlocks.DUMMY_BLOCK, "block_steel");
        steelScaffoldWithItem();
        simpleCubeWithItem(ModBlocks.STEEL_BEAM, "steel_beam");
        steelGrateWithItem(ModBlocks.STEEL_GRATE, "steel_grate");
        steelGrateWithItem(ModBlocks.STEEL_GRATE_WIDE, "steel_grate_wide");
        chainWithItem();
        glowingMushWithItem();
        wasteLogWithItem();
        simpleCubeWithItem(ModBlocks.WASTE_PLANKS, "waste_planks");
        leavesLayerWithItem();
        simpleCubeWithItem(ModBlocks.BARRICADE, "barricade");
        sellafieldWithItem();
        sellafieldSlakedWithItem(ModBlocks.SELLAFIELD_SLAKED, "sellafield_slaked");
        sellafieldSlakedWithItem(ModBlocks.SELLAFIELD_BEDROCK, "sellafield_bedrock");
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_DIAMOND, LegacySellafieldOreBlock.Kind.DIAMOND);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_EMERALD, LegacySellafieldOreBlock.Kind.EMERALD);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED, LegacySellafieldOreBlock.Kind.URANIUM_SCORCHED);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM, LegacySellafieldOreBlock.Kind.SCHRABIDIUM);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_RADGEM, LegacySellafieldOreBlock.Kind.RADGEM);
        simpleCubeWithItem(ModBlocks.WASTE_TRINITITE, "waste_trinitite");
        simpleCubeWithItem(ModBlocks.WASTE_TRINITITE_RED, "waste_trinitite_red");
        translucentCubeWithItem(ModBlocks.GLASS_TRINITITE, "glass_trinitite");
        simpleCubeWithItem(ModBlocks.ASH_DIGAMMA, "ash_digamma");
        crossBlockOnly(ModBlocks.FIRE_DIGAMMA, "fire_digamma");
        crossBlockOnly(ModBlocks.BALEFIRE, "balefire");
        pribrisDebrisWithItem(ModBlocks.PRIBRIS, "rbmk/rbmk_debris");
        pribrisDebrisWithItem(ModBlocks.PRIBRIS_BURNING, "rbmk/rbmk_debris_burning");
        pribrisDebrisAllStatesWithItem(ModBlocks.PRIBRIS_RADIATING, "rbmk/rbmk_debris_radiating");
        pribrisDebrisWithItem(ModBlocks.PRIBRIS_DIGAMMA, "rbmk/rbmk_debris_digamma");
        simpleCubeWithItem(ModBlocks.VOLCANIC_LAVA_BLOCK, "volcanic_lava_still");
        simpleCubeWithItem(ModBlocks.RAD_LAVA_BLOCK, "rad_lava_still");
        volcanoCoreWithItem(ModBlocks.VOLCANO_CORE, "volcano_core");
        volcanoCoreWithItem(ModBlocks.VOLCANO_RAD_CORE, "volcano_rad_core");
        taintWithItem();
        translucentCubeBlockOnly(ModBlocks.MUD_BLOCK, "mud_still");
        frozenGrassWithItem();
        simpleCubeWithItem(ModBlocks.FROZEN_DIRT, "frozen_dirt");
        frozenLogWithItem();
        simpleCubeWithItem(ModBlocks.FROZEN_PLANKS, "frozen_planks");
        simpleCubeWithItem(ModBlocks.TEKTITE, "tektite");
        simpleCubeWithItem(ModBlocks.ORE_TEKTITE_OSMIRIDIUM, "ore_tektite_osmiridium");
        plasticExplosiveWithItem(ModBlocks.BLOCK_SEMTEX, "block_semtex");
        plasticExplosiveWithItem(ModBlocks.BLOCK_C4, "block_c4");
        simpleCubeWithItem("crystal_virus", "legacy_blocks/crystal_virus");
        simpleCubeWithItem("crystal_hardened", "legacy_blocks/crystal_hardened");
        simpleCubeWithItem("glyphid_spawner", "glyphid_eggs_alt");
        existingModelWithCustomItem(ModBlocks.NUKE_GADGET, "nuke_gadget");
        existingModelWithCustomItem(ModBlocks.NUKE_BOY, "nuke_boy");
        existingModelWithCustomItem(ModBlocks.NUKE_MAN, "nuke_man");
        existingModelWithCustomItem(ModBlocks.NUKE_TSAR, "nuke_tsar");
        existingModelWithCustomItem(ModBlocks.NUKE_MIKE, "nuke_mike");
        existingModelWithCustomItem(ModBlocks.NUKE_PROTOTYPE, "nuke_prototype");
        existingModelWithCustomItem(ModBlocks.NUKE_FLEIJA, "nuke_fleija");
        existingModelWithCustomItem(ModBlocks.NUKE_SOLINIUM, "nuke_solinium");
        existingModelWithCustomItem(ModBlocks.NUKE_N2, "nuke_n2");
        existingModelWithCustomItem(ModBlocks.NUKE_CUSTOM, "nuke_custom");
        existingModelWithItem(ModBlocks.NUKE_FSTBMB, "nuke_fstbmb");
        existingModelWithItem(ModBlocks.BOMB_MULTI, "bomb_multi");
        tntBaseWithItem(ModBlocks.DYNAMITE, "dynamite");
        tntBaseWithItem(ModBlocks.TNT_NTM, "tnt");
        tntBaseWithItem(ModBlocks.SEMTEX, "semtex");
        tntBaseWithItem(ModBlocks.C4, "c4");
        legacyChargeWithItem(ModBlocks.CHARGE_DYNAMITE, "charge_dynamite");
        legacyChargeWithItem(ModBlocks.CHARGE_MINER, "charge_miner");
        legacyChargeWithItem(ModBlocks.CHARGE_C4, "charge_c4");
        legacyChargeWithItem(ModBlocks.CHARGE_SEMTEX, "charge_semtex");
        barrelWithItem(ModBlocks.RED_BARREL, "barrel_red");
        barrelWithItem(ModBlocks.PINK_BARREL, "barrel_pink");
        barrelWithItem(ModBlocks.LOX_BARREL, "barrel_lox");
        barrelWithItem(ModBlocks.TAINT_BARREL, "barrel_taint");
        barrelWithItem(ModBlocks.YELLOW_BARREL, "barrel_yellow");
        barrelWithItem(ModBlocks.VITRIFIED_BARREL, "barrel_vitrified");
    }

    private void tntBaseWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile model = models().cube(block.getId().getPath(),
                modLoc("block/" + textureName + "_bottom"),
                modLoc("block/" + textureName + "_top"),
                modLoc("block/" + textureName + "_side"),
                modLoc("block/" + textureName + "_side"),
                modLoc("block/" + textureName + "_side"),
                modLoc("block/" + textureName + "_side"))
                .texture("particle", modLoc("block/" + textureName + "_side"));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        simpleBlockItem(block.get(), model);
    }

    private void barrelWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile model = models().withExistingParent(block.getId().getPath(), modLoc("block/barrel_steel"))
                .texture("particle", modLoc("block/legacy_blocks/" + textureName))
                .texture("default", modLoc("block/legacy_blocks/" + textureName))
                .texture("texture0", modLoc("block/legacy_blocks/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void taintWithItem() {
        ModelFile model = models().cubeAll("taint", modLoc("block/legacy_blocks/taint"));
        getVariantBuilder(ModBlocks.TAINT.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        simpleBlockItem(ModBlocks.TAINT.get(), model);
    }

    private void legacyChargeWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile marker = models().getBuilder(block.getId().getPath())
                .texture("particle", modLoc("block/legacy_blocks/" + textureName));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(marker)
                        .rotationX(rotationX(state.getValue(LegacyChargeBlock.FACING)))
                        .rotationY(rotationY(state.getValue(LegacyChargeBlock.FACING)))
                        .build());
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", modLoc("block/legacy_blocks/" + textureName));
    }

    private void chainWithItem() {
        ModelFile chain = models().cross("chain", modLoc("block/chain"))
                .renderType("minecraft:cutout");
        ModelFile chainEnd = models().cross("chain_end", modLoc("block/chain_end"))
                .renderType("minecraft:cutout");
        getVariantBuilder(ModBlocks.CHAIN.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(LegacyChainBlock.END) ? chainEnd : chain)
                        .rotationY(rotationY(state.getValue(LegacyChainBlock.SUPPORT)))
                        .build());
        simpleBlockItem(ModBlocks.CHAIN.get(), chain);
    }

    private void plasticExplosiveWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile model = models().withExistingParent(block.getId().getPath(), mcLoc("block/cube"))
                .texture("particle", modLoc("block/" + textureName))
                .texture("down", modLoc("block/" + textureName))
                .texture("up", modLoc("block/" + textureName))
                .texture("north", modLoc("block/" + textureName + "_front"))
                .texture("south", modLoc("block/" + textureName))
                .texture("west", modLoc("block/" + textureName))
                .texture("east", modLoc("block/" + textureName));
        directionalBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void existingModelWithItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        horizontalBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void existingModelWithItemNoRotation(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void existingModelWithCustomItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = particleOnlyModel(block.getId().getPath(), modelName);
        horizontalBlock(block.get(), model);
        customBlockItem(block);
    }

    private void visibleMachineWithItemRenderer(RegistryObject<Block> block, String modelName) {
        ModelFile model = particleOnlyModel(block.getId().getPath(), modelName);
        horizontalBlock(block.get(), model);
        customBlockItem(block);
    }

    private void hiddenBerBlockWithItem(RegistryObject<Block> block) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/empty"));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        customBlockItem(block);
    }

    private void pileGraphiteBlocksWithItems() {
        simpleCubeWithItem(ModBlocks.BLOCK_GRAPHITE, "block_graphite");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_DRILLED,
                "block_graphite_drilled",
                "block_graphite_drilled_aluminum",
                "",
                "");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_FUEL,
                "block_graphite_fuel",
                "block_graphite_fuel_aluminum",
                "",
                "");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_PLUTONIUM,
                "block_graphite_plutonium",
                "block_graphite_plutonium_aluminum",
                "",
                "");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_ROD,
                "block_graphite_rod_in",
                "block_graphite_rod_in_aluminum",
                "block_graphite_rod_out",
                "block_graphite_rod_out_aluminum");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_SOURCE,
                "block_graphite_source",
                "block_graphite_source_aluminum",
                "",
                "");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_LITHIUM,
                "block_graphite_lithium",
                "block_graphite_lithium_aluminum",
                "",
                "");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_TRITIUM,
                "block_graphite_tritium",
                "block_graphite_tritium_aluminum",
                "",
                "");
        pileGraphiteColumnWithItem(ModBlocks.BLOCK_GRAPHITE_DETECTOR,
                "block_graphite_detector",
                "block_graphite_detector_aluminum",
                "block_graphite_detector_out",
                "block_graphite_detector_out_aluminum");
    }

    private void rbmkColumnWithItem(RegistryObject<Block> block, String textureBase) {
        ModelFile none = rbmkColumnModel(block.getId().getPath(), textureBase + "_side", textureBase + "_top");
        ModelFile standard = rbmkColumnModel(block.getId().getPath() + "_lid",
                textureBase + "_cover_side", textureBase + "_cover_top");
        ModelFile glass = rbmkColumnModel(block.getId().getPath() + "_glass_lid",
                textureBase + "_glass_side", textureBase + "_glass_top");
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(switch (state.getValue(RBMKColumnBlock.LID)) {
                            case STANDARD -> standard;
                            case GLASS -> glass;
                            case NONE -> none;
                        })
                        .build());
        customBlockItem(block);
    }

    private void watzPillarWithItem(RegistryObject<Block> block, String textureBase) {
        ResourceLocation side = modLoc("block/legacy_blocks/" + textureBase + "_side");
        ResourceLocation end = modLoc("block/legacy_blocks/" + textureBase + "_top");
        axisBlock((net.minecraft.world.level.block.RotatedPillarBlock) block.get(), side, end);
        simpleBlockItem(block.get(), models().withExistingParent(block.getId().getPath(), mcLoc("block/cube_column"))
                .texture("particle", side)
                .texture("side", side)
                .texture("end", end));
    }

    private void watzEndWithItem() {
        ModelFile normal = models().cubeAll("watz_end", modLoc("block/legacy_blocks/watz_casing"));
        ModelFile riveted = models().cubeAll("watz_end_riveted", modLoc("block/legacy_blocks/watz_casing_bolted"));
        getVariantBuilder(ModBlocks.WATZ_END.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(WatzEndBlock.RIVETED) ? riveted : normal)
                        .build());
        simpleBlockItem(ModBlocks.WATZ_END.get(), normal);
    }

    private void rbmkOwnLidColumnWithItem(RegistryObject<Block> block, String textureBase) {
        rbmkOwnLidColumnWithItem(block, textureBase, null);
    }

    private void rbmkOwnLidColumnWithItem(RegistryObject<Block> block, String textureBase, @Nullable String bottomTexture) {
        ModelFile model = bottomTexture == null
                ? rbmkColumnModel(block.getId().getPath(), textureBase + "_side", textureBase + "_top")
                : rbmkColumnBottomTopModel(block.getId().getPath(),
                        textureBase + "_side", textureBase + "_top", bottomTexture);
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        customBlockItem(block);
    }

    private void rbmkBerStructureWithItem(RegistryObject<Block> block, String particleTexture) {
        ModelFile marker = models().getBuilder(block.getId().getPath())
                .texture("particle", modLoc("block/" + particleTexture));
        horizontalBlock(block.get(), marker);
        customBlockItem(block);
    }

    private void rbmkBerStructureNoRotationWithItem(RegistryObject<Block> block, String particleTexture) {
        ModelFile marker = models().getBuilder(block.getId().getPath())
                .texture("particle", modLoc("block/" + particleTexture));
        simpleBlock(block.get(), marker);
        customBlockItem(block);
    }

    private ModelFile rbmkColumnModel(String modelName, String sideTexture, String topTexture) {
        return models().withExistingParent(modelName, mcLoc("block/cube_column"))
                .texture("particle", modLoc("block/rbmk/" + sideTexture))
                .texture("side", modLoc("block/rbmk/" + sideTexture))
                .texture("end", modLoc("block/rbmk/" + topTexture));
    }

    private ModelFile rbmkColumnBottomTopModel(String modelName, String sideTexture, String topTexture,
            String bottomTexture) {
        return models().withExistingParent(modelName, mcLoc("block/cube_bottom_top"))
                .texture("particle", modLoc("block/rbmk/" + sideTexture))
                .texture("side", modLoc("block/rbmk/" + sideTexture))
                .texture("top", modLoc("block/rbmk/" + topTexture))
                .texture("bottom", modLoc("block/rbmk/" + bottomTexture));
    }

    private void pileGraphiteColumnWithItem(
            RegistryObject<Block> block,
            String endTexture,
            String aluminumEndTexture,
            String activeEndTexture,
            String activeAluminumEndTexture) {
        ModelFile normal = pileGraphiteColumnModel(block.getId().getPath(), endTexture);
        ModelFile aluminum = pileGraphiteColumnModel(block.getId().getPath() + "_aluminum",
                fallbackTexture(aluminumEndTexture, endTexture));
        ModelFile active = pileGraphiteColumnModel(block.getId().getPath() + "_active",
                fallbackTexture(activeEndTexture, endTexture));
        ModelFile activeAluminum = pileGraphiteColumnModel(block.getId().getPath() + "_active_aluminum",
                fallbackTexture(activeAluminumEndTexture, fallbackTexture(aluminumEndTexture, endTexture)));

        getVariantBuilder(block.get())
                .forAllStates(state -> {
                    boolean aluminumState = state.getValue(PileGraphiteDrilledBaseBlock.ALUMINUM);
                    boolean activeState = state.getValue(PileGraphiteDrilledBaseBlock.ACTIVE);
                    ModelFile model = activeState
                            ? (aluminumState ? activeAluminum : active)
                            : (aluminumState ? aluminum : normal);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (state.getValue(PileGraphiteDrilledBaseBlock.AXIS)) {
                        case Z -> builder.rotationX(90);
                        case X -> builder.rotationX(90).rotationY(90);
                        case Y -> {
                        }
                    }
                    return builder.build();
                });
        simpleBlockItem(block.get(), normal);
    }

    private ModelFile pileGraphiteColumnModel(String modelName, String endTexture) {
        return models().withExistingParent(modelName, mcLoc("block/cube_column"))
                .texture("particle", modLoc("block/block_graphite"))
                .texture("side", modLoc("block/block_graphite"))
                .texture("end", modLoc("block/" + endTexture));
    }

    private static String fallbackTexture(String preferred, String fallback) {
        return preferred == null || preferred.isEmpty() ? fallback : preferred;
    }

    private void vendingMachineWithItemRenderer() {
        ModelFile marker = models().getBuilder(ModBlocks.VENDING_MACHINE.getId().getPath())
                .texture("particle", modLoc("block/machines/vending_machine"));
        getVariantBuilder(ModBlocks.VENDING_MACHINE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(marker)
                        .rotationY(rotationY(state.getValue(VendingMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.VENDING_MACHINE);
    }

    private void pylonWithItemRenderer(RegistryObject<Block> block, String particleTexture) {
        ModelFile marker = models().getBuilder(block.getId().getPath())
                .texture("particle", modLoc("block/" + particleTexture));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(marker)
                        .build());
        customBlockItem(block);
    }

    private void customBlockItem(RegistryObject<Block> block) {
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("builtin/entity")));
    }

    private void forceFieldWithItem() {
        ModelFile marker = models().getBuilder(ModBlocks.MACHINE_FORCEFIELD.getId().getPath())
                .texture("particle", modLoc("block/machine_forcefield"));
        simpleBlock(ModBlocks.MACHINE_FORCEFIELD.get(), marker);
        generatedBlockItem(ModBlocks.MACHINE_FORCEFIELD, "block/machine_forcefield");
    }

    private void generatedBlockItem(RegistryObject<Block> block, String texturePath) {
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, texturePath));
    }

    private void soyuzCapsuleWithItem() {
        ModelFile model = models().cubeAll("soyuz_capsule",
                new ResourceLocation(HbmNtm.MOD_ID, "block/soyuz/capsule/soyuz_lander"));
        simpleBlock(ModBlocks.SOYUZ_CAPSULE.get(), model);
        generatedBlockItem(ModBlocks.SOYUZ_CAPSULE, "item/soyuz_lander");
    }

    private void fluidBarrelWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().getBuilder(blockName)
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/block/legacy_blocks/barrel.obj"))
                .flipV(true)
                .end()
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/" + textureName))
                .texture("default", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/" + textureName))
                .texture("texture0", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void existingModelBlockOnly(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        horizontalBlock(block.get(), model);
    }

    private void steelScaffoldWithItem() {
        ModelFile model = models().getBuilder("steel_scaffold")
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/block/legacy_blocks/scaffold.obj"))
                .flipV(true)
                .end()
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/scaffold_steel"))
                .texture("default", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/scaffold_steel"))
                .texture("texture0", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/scaffold_steel"));
        getVariantBuilder(ModBlocks.STEEL_SCAFFOLD.get())
                .forAllStates(state -> scaffoldModel(state.getValue(SteelScaffoldBlock.AXIS), model));
        simpleBlockItem(ModBlocks.STEEL_SCAFFOLD.get(), model);
    }

    private void steelGrateWithItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        simpleBlockItem(block.get(), model);
    }

    private void pribrisDebrisWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile model = pribrisDebrisModel(block.getId().getPath(), textureName);
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        simpleBlockItem(block.get(), model);
    }

    private void pribrisDebrisAllStatesWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile model = pribrisDebrisModel(block.getId().getPath(), textureName);
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private ModelFile pribrisDebrisModel(String blockName, String textureName) {
        ResourceLocation texture = new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName);
        return models().getBuilder(blockName)
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/block/rbmk/debris.obj"))
                .flipV(true)
                .end()
                .texture("particle", texture)
                .texture("default", texture);
    }

    private ConfiguredModel[] scaffoldModel(Direction.Axis axis, ModelFile model) {
        ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
        switch (axis) {
            case Y -> builder.rotationX(90);
            case Z -> builder.rotationY(90);
            case X -> {
            }
        }
        return builder.build();
    }

    private void difurnaceWithItem(RegistryObject<Block> block) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cube(
                blockName,
                new ResourceLocation(HbmNtm.MOD_ID, "block/brick_fire"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/difurnace_top_off_alt"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/difurnace_side_alt"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/difurnace_front_off_alt"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/difurnace_side_alt"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/difurnace_side_alt"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/difurnace_side_alt"));
        horizontalBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void cubeWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        horizontalBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void simpleCubeWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void icfAssembledBlock() {
        ModelFile normal = models().cubeAll("icf_block", new ResourceLocation(HbmNtm.MOD_ID,
                "block/legacy_blocks/icf_block"));
        ModelFile port = models().cubeAll("icf_block_port", new ResourceLocation(HbmNtm.MOD_ID,
                "block/legacy_blocks/icf_block_port"));
        getVariantBuilder(ModBlocks.ICF_BLOCK.get())
                .partialState().with(ICFAssembledBlock.PORT, false)
                .modelForState().modelFile(normal).addModel()
                .partialState().with(ICFAssembledBlock.PORT, true)
                .modelForState().modelFile(port).addModel();
    }

    private void simpleCubeAllStatesWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        simpleBlockItem(block.get(), model);
    }

    private void simpleCubeWithItem(String legacyName, String textureName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block hbm_ntm_rebirth:" + legacyName);
        }
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void volcanoCoreWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        simpleBlockItem(block.get(), model);
    }

    private void crossBlockOnly(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cross(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:cutout");
        simpleBlock(block.get(), model);
    }

    private void glowingMushWithItem() {
        crossBlockOnly(ModBlocks.MUSH, "mush");
        hugeMushBlock(ModBlocks.MUSH_BLOCK, "mush_block_skin", "mush_block_skin");
        hugeMushBlock(ModBlocks.MUSH_BLOCK_STEM, "mush_block_stem", "mush_block_inside");
        itemModels().withExistingParent("mush", new ResourceLocation("minecraft", "item/generated"))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "block/mush"));
    }

    private void hugeMushBlock(RegistryObject<Block> block, String sideTexture, String endTexture) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeColumn(blockName,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + sideTexture),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + endTexture));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void translucentCubeWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:translucent");
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void capBlockWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeBottomTop(blockName,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName + "_top"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName + "_top"));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void translucentCubeBlockOnly(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:translucent");
        simpleBlock(block.get(), model);
    }

    private void reinforcedLaminatePaneWithItem() {
        ResourceLocation pane = new ResourceLocation(HbmNtm.MOD_ID, "block/reinforced_laminate_pane");
        ResourceLocation edge = new ResourceLocation(HbmNtm.MOD_ID, "block/reinforced_laminate_pane_edge");
        paneBlockWithRenderType((LegacyNtmGlassPaneBlock) ModBlocks.REINFORCED_LAMINATE_PANE.get(),
                pane, edge, "minecraft:translucent");
        itemModels().withExistingParent("reinforced_laminate_pane",
                        new ResourceLocation("minecraft", "item/generated"))
                .texture("layer0", pane)
                .renderType("minecraft:translucent");
    }

    private void wasteLogWithItem() {
        ResourceLocation side = new ResourceLocation(HbmNtm.MOD_ID, "block/waste_log_side");
        ResourceLocation top = new ResourceLocation(HbmNtm.MOD_ID, "block/waste_log_top");
        axisBlock((net.minecraft.world.level.block.RotatedPillarBlock) ModBlocks.WASTE_LOG.get(), side, top);
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/waste_log"));
        simpleBlockItem(ModBlocks.WASTE_LOG.get(), model);
    }

    private void frozenGrassWithItem() {
        String blockName = ModBlocks.FROZEN_GRASS.getId().getPath();
        ModelFile model = models().cube(
                blockName,
                new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_dirt"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_grass_top"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_grass_side"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_grass_side"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_grass_side"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_grass_side"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_grass_side"));
        simpleBlock(ModBlocks.FROZEN_GRASS.get(), model);
        simpleBlockItem(ModBlocks.FROZEN_GRASS.get(), model);
    }

    private void frozenLogWithItem() {
        ResourceLocation side = new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_log");
        ResourceLocation top = new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_log_top");
        axisBlock((net.minecraft.world.level.block.RotatedPillarBlock) ModBlocks.FROZEN_LOG.get(), side, top);
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/frozen_log"));
        simpleBlockItem(ModBlocks.FROZEN_LOG.get(), model);
    }

    private void leavesLayerWithItem() {
        ModelFile model = models().withExistingParent("leaves_layer", new ResourceLocation("block/carpet"))
                .texture("wool", new ResourceLocation(HbmNtm.MOD_ID, "block/waste_leaves"));
        simpleBlock(ModBlocks.LEAVES_LAYER.get(), model);
        simpleBlockItem(ModBlocks.LEAVES_LAYER.get(), model);
    }

    private void sidedCubeWithItem(
            RegistryObject<Block> block,
            String down,
            String up,
            String north,
            String south,
            String east,
            String west) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cube(
                blockName,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + down),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + up),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + north),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + south),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + east),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + west))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + north));
        horizontalBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void horizontalBlockNoRotationWithItem(
            RegistryObject<Block> block,
            String down,
            String up,
            String north,
            String south,
            String east,
            String west) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cube(
                blockName,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + down),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + up),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + north),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + south),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + east),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + west))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + north));
        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .build());
        simpleBlockItem(block.get(), model);
    }

    private void simpleSidedCubeWithItem(
            RegistryObject<Block> block,
            String down,
            String up,
            String north,
            String south,
            String east,
            String west) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cube(
                blockName,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + down),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + up),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + north),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + south),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + east),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + west))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + north));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void redCableWithItem() {
        getMultipartBuilder(ModBlocks.RED_CABLE.get())
                .part().modelFile(models().getBuilder("red_cable")
                        .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/cable_neo")))
                .addModel().end();
        itemModels().getBuilder(ModBlocks.RED_CABLE.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("builtin/entity")));
    }

    private void redCableClassicWithItem() {
        ModelFile core = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_classic_core"));
        ModelFile north = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_classic_north"));
        ModelFile east = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_classic_east"));
        ModelFile south = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_classic_south"));
        ModelFile west = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_classic_west"));
        ModelFile up = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_classic_up"));
        ModelFile down = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_classic_down"));
        var builder = getMultipartBuilder(ModBlocks.RED_CABLE_CLASSIC.get());
        builder.part().modelFile(core).addModel().end();
        builder.part().modelFile(north).addModel().condition(HbmEnergyNodeBlock.NORTH, true).end();
        builder.part().modelFile(east).addModel().condition(HbmEnergyNodeBlock.EAST, true).end();
        builder.part().modelFile(south).addModel().condition(HbmEnergyNodeBlock.SOUTH, true).end();
        builder.part().modelFile(west).addModel().condition(HbmEnergyNodeBlock.WEST, true).end();
        builder.part().modelFile(up).addModel().condition(HbmEnergyNodeBlock.UP, true).end();
        builder.part().modelFile(down).addModel().condition(HbmEnergyNodeBlock.DOWN, true).end();
        simpleBlockItem(ModBlocks.RED_CABLE_CLASSIC.get(), core);
    }

    private void redCableBoxWithItem() {
        ModelFile model = models().cubeAll("red_cable_box",
                new ResourceLocation(HbmNtm.MOD_ID, "block/boxduct_cable_straight"));
        getVariantBuilder(ModBlocks.RED_CABLE_BOX.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        itemModels().getBuilder(ModBlocks.RED_CABLE_BOX.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/red_cable_box")));
    }

    private void redCableGaugeWithItem() {
        ModelFile[] models = new ModelFile[Direction.values().length];
        for (Direction direction : Direction.values()) {
            models[direction.ordinal()] = redCableGaugeModel("red_cable_gauge_" + direction.getName(), direction);
        }
        var builder = getMultipartBuilder(ModBlocks.RED_CABLE_GAUGE.get());
        for (Direction direction : Direction.values()) {
            builder.part()
                    .modelFile(models[direction.ordinal()])
                    .addModel()
                    .condition(RedCableGaugeBlock.FACING, direction)
                    .end();
        }
        simpleBlockItem(ModBlocks.RED_CABLE_GAUGE.get(), models[Direction.NORTH.ordinal()]);
    }

    private void poweredRedCableWithItem(RegistryObject<Block> block, String offTexture, String onTexture) {
        ModelFile off = models().cubeAll(block.getId().getPath() + "_off",
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + offTexture));
        ModelFile on = models().cubeAll(block.getId().getPath() + "_on",
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + onTexture));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(PoweredRedCableBlock.ACTIVE) ? on : off)
                        .build());
        simpleBlockItem(block.get(), off);
    }

    private void cableDiodeWithItem() {
        ModelFile model = models().getBuilder(ModBlocks.CABLE_DIODE.getId().getPath())
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/cable_diode"));
        getVariantBuilder(ModBlocks.CABLE_DIODE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        itemModels().getBuilder(ModBlocks.CABLE_DIODE.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("builtin/entity")));
    }

    private static int rotationX(Direction direction) {
        return switch (direction) {
            case DOWN -> 180;
            case NORTH, SOUTH, EAST, WEST -> 90;
            case UP -> 0;
        };
    }

    private static int rotationY(Direction direction) {
        return switch (direction) {
            case NORTH -> 180;
            case SOUTH, UP, DOWN -> 0;
            case WEST -> 90;
            case EAST -> 270;
        };
    }

    private void fluidPipeWithItem() {
        ModelFile[] models = new ModelFile[FluidPipeBlock.LEGACY_STYLE_COUNT];
        String[] textures = {"pipe_neo", "pipe_silver", "pipe_colored"};
        for (int style = 0; style < models.length; style++) {
            models[style] = particleOnlyModel("fluid_duct_neo_" + style, textures[style]);
        }
        var builder = getMultipartBuilder(ModBlocks.FLUID_DUCT_NEO.get());
        for (int style = 0; style < models.length; style++) {
            builder.part()
                    .modelFile(models[style])
                    .addModel()
                    .condition(FluidPipeBlock.LEGACY_STYLE, style)
                    .end();
        }
        customBlockItem(ModBlocks.FLUID_DUCT_NEO);
    }

    private void fluidValveWithItem(RegistryObject<Block> block, String offTexture, String onTexture) {
        ModelFile off = models().cubeAll(block.getId().getPath() + "_off",
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + offTexture));
        ModelFile on = models().cubeAll(block.getId().getPath() + "_on",
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + onTexture));
        getMultipartBuilder(block.get())
                .part()
                    .modelFile(off)
                    .addModel()
                    .condition(FluidValveBlock.OPEN, false)
                    .end()
                .part()
                    .modelFile(on)
                    .addModel()
                    .condition(FluidValveBlock.OPEN, true)
                    .end();
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
    }

    private void fluidDuctBoxWithItem(RegistryObject<Block> block) {
        String blockName = block.getId().getPath();
        ModelFile[] models = new ModelFile[FluidDuctBoxBlock.LEGACY_METADATA_COUNT];
        for (int metadata = 0; metadata < models.length; metadata++) {
            models[metadata] = particleOnlyModel(blockName + "_" + metadata,
                    boxDuctTexture(FluidDuctBoxBlock.rectifyLegacyMaterial(metadata),
                            FluidDuctBoxBlock.legacySizeStep(metadata)));
        }
        legacyDuctMetadataMultipart(block, models);
        customBlockItem(block);
    }

    private void fluidDuctExhaustWithItem() {
        String blockName = ModBlocks.FLUID_DUCT_EXHAUST.getId().getPath();
        ModelFile[] models = new ModelFile[FluidDuctBoxBlock.LEGACY_METADATA_COUNT];
        for (int metadata = 0; metadata < models.length; metadata++) {
            models[metadata] = particleOnlyModel(blockName + "_" + metadata,
                    "boxduct_exhaust_junction_" + FluidDuctBoxBlock.legacySizeStep(metadata));
        }
        legacyDuctMetadataMultipart(ModBlocks.FLUID_DUCT_EXHAUST, models);
        customBlockItem(ModBlocks.FLUID_DUCT_EXHAUST);
    }

    private void legacyDuctMetadataMultipart(RegistryObject<Block> block, ModelFile[] models) {
        var builder = getMultipartBuilder(block.get());
        for (int metadata = 0; metadata < models.length; metadata++) {
            builder.part()
                    .modelFile(models[metadata])
                    .addModel()
                    .condition(FluidDuctBoxBlock.LEGACY_METADATA, metadata)
                    .end();
        }
    }

    private ModelFile particleOnlyModel(String name, String texture) {
        return models().getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "block/block")))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + texture));
    }

    private static String boxDuctTexture(int material, int step) {
        String materialName = switch (material) {
            case 1 -> "copper";
            case 2 -> "white";
            default -> "silver";
        };
        return "boxduct_" + materialName + "_junction_" + step;
    }

    private void fluidDuctGaugeWithItem() {
        ModelFile[] models = new ModelFile[Direction.values().length];
        for (Direction direction : Direction.values()) {
            models[direction.ordinal()] = fluidDuctGaugeModel("fluid_duct_gauge_" + direction.getName(), direction);
        }
        var builder = getMultipartBuilder(ModBlocks.FLUID_DUCT_GAUGE.get());
        for (Direction direction : Direction.values()) {
            builder.part()
                    .modelFile(models[direction.ordinal()])
                    .addModel()
                    .condition(FluidDuctGaugeBlock.FACING, direction)
                    .end();
        }
        itemModels().getBuilder(ModBlocks.FLUID_DUCT_GAUGE.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
    }

    private void fluidDuctPaintableWithItem(RegistryObject<Block> block, String baseTexture) {
        String blockName = block.getId().getPath();
        boolean exhaust = block == ModBlocks.FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST;
        ModelFile overlay = fluidDuctPaintableModel(blockName + "_overlay", baseTexture,
                "fluid_duct_paintable_overlay", true);
        ModelFile clean = exhaust
                ? fluidDuctPaintableBaseModel(blockName, baseTexture)
                : fluidDuctPaintableModel(blockName, baseTexture, "fluid_duct_paintable_color", false);
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.hasProperty(FluidDuctPaintableBlock.OVERLAY)
                                && state.getValue(FluidDuctPaintableBlock.OVERLAY) ? overlay : clean)
                        .build());
        if (exhaust) {
            itemModels().getBuilder(blockName)
                    .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + blockName)));
        } else {
            itemModels().getBuilder(blockName)
                    .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                    .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                    .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
        }
    }

    private ModelFile fluidDuctPaintableBaseModel(String modelName, String baseTexture) {
        return models().withExistingParent(modelName, new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + baseTexture))
                .texture("base", new ResourceLocation(HbmNtm.MOD_ID, "block/" + baseTexture))
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction))
                    .end();
    }

    private ModelFile fluidDuctPaintableModel(String modelName, String baseTexture, String overlayTexture,
            boolean tintOverlay) {
        var builder = models().withExistingParent(modelName, new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + baseTexture))
                .texture("base", new ResourceLocation(HbmNtm.MOD_ID, "block/" + baseTexture))
                .texture("overlay", new ResourceLocation(HbmNtm.MOD_ID, "block/" + overlayTexture))
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction))
                    .end()
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F);
        if (tintOverlay) {
            builder.allFaces((direction, face) -> face.texture("#overlay").cullface(direction));
        } else {
            builder.allFaces((direction, face) -> face.texture("#overlay").cullface(direction).tintindex(1));
        }
        return builder.end();
    }

    private ModelFile fluidDuctGaugeModel(String modelName, Direction gaugeFace) {
        return models().withExistingParent(modelName, new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/deco_steel"))
                .texture("base", new ResourceLocation(HbmNtm.MOD_ID, "block/deco_steel"))
                .texture("overlay", new ResourceLocation(HbmNtm.MOD_ID, "block/fluid_duct_paintable_overlay"))
                .texture("gauge", new ResourceLocation(HbmNtm.MOD_ID, "block/pipe_gauge"))
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction))
                    .end()
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F)
                    .allFaces((direction, face) -> face.texture(direction == gaugeFace ? "#gauge" : "#overlay").cullface(direction))
                    .end();
    }

    private ModelFile redCableGaugeModel(String modelName, Direction gaugeFace) {
        float minX = 0.0F;
        float minY = 0.0F;
        float minZ = 0.0F;
        float maxX = 16.0F;
        float maxY = 16.0F;
        float maxZ = 16.0F;
        switch (gaugeFace) {
            case DOWN -> minY = -0.01F;
            case UP -> maxY = 16.01F;
            case NORTH -> minZ = -0.01F;
            case SOUTH -> maxZ = 16.01F;
            case WEST -> minX = -0.01F;
            case EAST -> maxX = 16.01F;
        }
        return models().withExistingParent(modelName, new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/deco_red_copper"))
                .texture("base", new ResourceLocation(HbmNtm.MOD_ID, "block/deco_red_copper"))
                .texture("gauge", new ResourceLocation(HbmNtm.MOD_ID, "block/cable_gauge"))
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F)
                    .allFaces((direction, face) -> face.texture("#base").cullface(direction))
                    .end()
                .element()
                    .from(minX, minY, minZ)
                    .to(maxX, maxY, maxZ)
                    .face(gaugeFace).texture("#gauge").cullface(gaugeFace).end()
                    .end();
    }

    private void fluidPipeAnchorWithItem() {
        ModelFile model = models().getExistingFile(new ResourceLocation(HbmNtm.MOD_ID, "block/network/pipe_anchor"));
        getVariantBuilder(ModBlocks.PIPE_ANCHOR.get())
                .forAllStates(state -> anchorModel(state.getValue(FluidPipeAnchorBlock.FACING), model));
        itemModels().getBuilder(ModBlocks.PIPE_ANCHOR.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/network/pipe_anchor")));
    }

    private ConfiguredModel[] anchorModel(Direction facing, ModelFile model) {
        ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
        switch (facing) {
            case DOWN -> builder.rotationX(180);
            case NORTH -> builder.rotationX(90).rotationY(180);
            case SOUTH -> builder.rotationX(90);
            case WEST -> builder.rotationX(90).rotationY(90);
            case EAST -> builder.rotationX(90).rotationY(270);
            case UP -> {
            }
        }
        return builder.build();
    }

    private void fluidPumpWithItem() {
        ModelFile model = models().getExistingFile(new ResourceLocation(HbmNtm.MOD_ID, "block/network/fluid_diode"));
        getVariantBuilder(ModBlocks.FLUID_PUMP.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(fluidPumpRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        itemModels().getBuilder(ModBlocks.FLUID_PUMP.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/network/fluid_diode")));
    }

    private static int fluidPumpRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180;
            case EAST -> 90;
            case WEST -> 270;
            default -> 0;
        };
    }

    private void conveyorWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile straight = models().withExistingParent(block.getId().getPath(), new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .texture("top", new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .texture("side", new ResourceLocation(HbmNtm.MOD_ID, "block/conveyor_side"))
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 4.0F, 16.0F)
                    .face(Direction.DOWN).texture("#side").cullface(Direction.DOWN).end()
                    .face(Direction.UP).texture("#top").end()
                    .face(Direction.NORTH).texture("#side").cullface(Direction.NORTH).end()
                    .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                    .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                    .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                    .end();
        ModelFile left = conveyorCurveModel(block.getId().getPath() + "_curve_left", textureName + "_curve_left");
        ModelFile right = conveyorCurveModel(block.getId().getPath() + "_curve_right", textureName + "_curve_right");

        getVariantBuilder(block.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(ConveyorBlock.FACING);
                    ModelFile model = switch (state.getValue(ConveyorBlock.PATH)) {
                        case LEFT -> left;
                        case RIGHT -> right;
                        default -> straight;
                    };
                    return ConfiguredModel.builder()
                            .modelFile(model)
                            .rotationY(((int) facing.toYRot() + 180) % 360)
                            .build();
                });
        simpleBlockItem(block.get(), straight);
    }

    private ModelFile conveyorCurveModel(String modelName, String textureName) {
        return models().withExistingParent(modelName, new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .texture("top", new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .texture("side", new ResourceLocation(HbmNtm.MOD_ID, "block/conveyor_side"))
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 4.0F, 16.0F)
                    .face(Direction.DOWN).texture("#side").cullface(Direction.DOWN).end()
                    .face(Direction.UP).texture("#top").end()
                    .face(Direction.NORTH).texture("#side").cullface(Direction.NORTH).end()
                    .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                    .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                    .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                    .end();
    }

    private void verticalConveyorWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile model = models().cubeAll(block.getId().getPath(), new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(model)
                .rotationY(((int) state.getValue(ConveyorBlock.FACING).toYRot() + 180) % 360)
                .build());
        simpleBlockItem(block.get(), model);
    }

    private void storageCrateWithItem(RegistryObject<Block> block, String texturePrefix) {
        ModelFile model = models().cubeBottomTop(block.getId().getPath(),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + texturePrefix + "_side"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + texturePrefix + "_top"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + texturePrefix + "_top"));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void safeWithItem() {
        ModelFile model = models().orientable("safe",
                new ResourceLocation(HbmNtm.MOD_ID, "block/safe_side"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/safe_front"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/safe_side"));
        horizontalBlock(ModBlocks.SAFE.get(), model);
        simpleBlockItem(ModBlocks.SAFE.get(), model);
    }

    private void massStorageWithItem() {
        ModelFile iron = massStorageModel("mass_storage_iron", "mass_storage_top_iron",
                "legacy_blocks/mass_storage_front_iron", "mass_storage_side_iron");
        ModelFile desh = massStorageModel("mass_storage_desh", "mass_storage_top_desh",
                "legacy_blocks/mass_storage_front_desh", "mass_storage_side_desh");
        ModelFile tungsten = massStorageModel("mass_storage_tungsten", "mass_storage_top",
                "legacy_blocks/mass_storage_front", "mass_storage_side");
        ModelFile wood = massStorageModel("mass_storage_wood", "mass_storage_top_wood",
                "legacy_blocks/mass_storage_front_wood", "mass_storage_side_wood");
        getVariantBuilder(ModBlocks.MASS_STORAGE.get()).forAllStates(state -> {
            ModelFile model = switch (state.getValue(MassStorageBlock.VARIANT)) {
                case 1 -> desh;
                case 2 -> tungsten;
                case 3 -> wood;
                default -> iron;
            };
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(((int) state.getValue(HorizontalMachineBlock.FACING).toYRot() + 180) % 360)
                    .build();
        });
        simpleBlockItem(ModBlocks.MASS_STORAGE.get(), iron);
    }

    private ModelFile massStorageModel(String name, String top, String front, String side) {
        return models().orientable(name,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + side),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + front),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + top));
    }

    private void radAbsorberWithItem() {
        getVariantBuilder(ModBlocks.RAD_ABSORBER.get())
                .partialState().with(LegacyRadAbsorberBlock.TIER, 0).modelForState()
                .modelFile(models().cubeAll("rad_absorber", new ResourceLocation(HbmNtm.MOD_ID, "block/absorber"))).addModel()
                .partialState().with(LegacyRadAbsorberBlock.TIER, 1).modelForState()
                .modelFile(models().cubeAll("rad_absorber_red", new ResourceLocation(HbmNtm.MOD_ID, "block/absorber_red"))).addModel()
                .partialState().with(LegacyRadAbsorberBlock.TIER, 2).modelForState()
                .modelFile(models().cubeAll("rad_absorber_green", new ResourceLocation(HbmNtm.MOD_ID, "block/absorber_green"))).addModel()
                .partialState().with(LegacyRadAbsorberBlock.TIER, 3).modelForState()
                .modelFile(models().cubeAll("rad_absorber_pink", new ResourceLocation(HbmNtm.MOD_ID, "block/absorber_pink"))).addModel();
        simpleBlockItem(ModBlocks.RAD_ABSORBER.get(), models().getExistingFile(new ResourceLocation(HbmNtm.MOD_ID, "block/rad_absorber")));
    }

    private void sellafieldWithItem() {
        getVariantBuilder(ModBlocks.SELLAFIELD.get())
                .partialState().with(LegacySellafieldBlock.LEVEL, 0).modelForState()
                .modelFile(models().cubeAll("sellafield", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_0"))).addModel()
                .partialState().with(LegacySellafieldBlock.LEVEL, 1).modelForState()
                .modelFile(models().cubeAll("sellafield_1", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_1"))).addModel()
                .partialState().with(LegacySellafieldBlock.LEVEL, 2).modelForState()
                .modelFile(models().cubeAll("sellafield_2", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_2"))).addModel()
                .partialState().with(LegacySellafieldBlock.LEVEL, 3).modelForState()
                .modelFile(models().cubeAll("sellafield_3", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_3"))).addModel()
                .partialState().with(LegacySellafieldBlock.LEVEL, 4).modelForState()
                .modelFile(models().cubeAll("sellafield_4", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_4"))).addModel()
                .partialState().with(LegacySellafieldBlock.LEVEL, 5).modelForState()
                .modelFile(models().cubeAll("sellafield_5", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_5"))).addModel();
        simpleBlockItem(ModBlocks.SELLAFIELD.get(), models().getExistingFile(new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield")));
    }

    private void sellafieldSlakedWithItem(RegistryObject<Block> block, String modelName) {
        ModelFile[] models = sellafieldSlakedModels(modelName);
        for (int level = 0; level <= 15; level++) {
            getVariantBuilder(block.get())
                    .partialState().with(LegacySellafieldSlakedBlock.LEVEL, level)
                    .setModels(configuredModels(models));
        }
        simpleBlockItem(block.get(), models[0]);
    }

    private void sellafieldOreWithItem(RegistryObject<Block> block, LegacySellafieldOreBlock.Kind kind) {
        String name = block.getId().getPath();
        ModelFile[] models = sellafieldOreModels(name, kind);
        for (int level = 0; level <= 15; level++) {
            getVariantBuilder(block.get())
                    .partialState().with(LegacySellafieldSlakedBlock.LEVEL, level)
                    .setModels(configuredModels(models));
        }
        simpleBlockItem(block.get(), models[0]);
    }

    private ConfiguredModel[] configuredModels(ModelFile[] models) {
        ConfiguredModel[] result = new ConfiguredModel[models.length];
        for (int i = 0; i < models.length; i++) {
            result[i] = new ConfiguredModel(models[i]);
        }
        return result;
    }

    private ModelFile[] sellafieldSlakedModels(String modelName) {
        ModelFile[] result = new ModelFile[4];
        for (int i = 0; i < result.length; i++) {
            String suffix = i == 0 ? "" : "_" + i;
            result[i] = models().withExistingParent(modelName + suffix, new ResourceLocation("block/block"))
                    .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked" + suffix))
                    .texture("all", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked" + suffix))
                    .element()
                        .from(0.0F, 0.0F, 0.0F)
                        .to(16.0F, 16.0F, 16.0F)
                        .allFaces((direction, face) -> face.texture("#all").cullface(direction).tintindex(0))
                        .end();
        }
        return result;
    }

    private ModelFile[] sellafieldOreModels(String name, LegacySellafieldOreBlock.Kind kind) {
        ModelFile[] result = new ModelFile[4];
        for (int i = 0; i < result.length; i++) {
            String suffix = i == 0 ? "" : "_" + i;
            result[i] = models().withExistingParent(name + suffix, new ResourceLocation("block/block"))
                    .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked" + suffix))
                    .texture("base", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked" + suffix))
                    .texture("overlay", new ResourceLocation(HbmNtm.MOD_ID, "block/ore_overlay_" + kind.overlayTexture()))
                    .element()
                        .from(0.0F, 0.0F, 0.0F)
                        .to(16.0F, 16.0F, 16.0F)
                        .allFaces((direction, face) -> face.texture("#base").tintindex(0))
                        .end()
                    .element()
                        .from(0.0F, 0.0F, 0.0F)
                        .to(16.0F, 16.0F, 16.0F)
                        .allFaces((direction, face) -> face.texture("#overlay").cullface(direction))
                        .end();
        }
        return result;
    }
}
