package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.BigAssTankBlock;
import com.hbm.ntm.block.BlastFurnaceBlock;
import com.hbm.ntm.block.CableDiodeBlock;
import com.hbm.ntm.block.CapacitorBlock;
import com.hbm.ntm.block.CargoElevatorBlock;
import com.hbm.ntm.block.ConcreteColoredBlock;
import com.hbm.ntm.block.ConcreteColoredExtBlock;
import com.hbm.ntm.block.DfcMachineBlock;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidDuctGaugeBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.block.FluidValveBlock;
import com.hbm.ntm.block.HeatBoilerBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.block.GasFlareBlock;
import com.hbm.ntm.block.ICFAssembledBlock;
import com.hbm.ntm.block.LegacyBarbedWireBlock;
import com.hbm.ntm.block.LegacyChargeBlock;
import com.hbm.ntm.block.LegacyChainBlock;
import com.hbm.ntm.block.LegacyDeadPlantBlock;
import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.block.LegacyFrameRenderState;
import com.hbm.ntm.block.LegacyGlyphidSpawnerBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacyBasaltOreBlock;
import com.hbm.ntm.block.LegacyBiomeStoneBlock;
import com.hbm.ntm.block.LegacyCokeBlock;
import com.hbm.ntm.block.LegacyLightstoneBlock;
import com.hbm.ntm.block.LegacyMultiSlabBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacySellafieldOreBlock;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.block.LegacyWoodStructureBlock;
import com.hbm.ntm.block.LegacyNtmGlassPaneBlock;
import com.hbm.ntm.block.LegacyNtmFlowerBlock;
import com.hbm.ntm.block.LegacyTallPlantBlock;
import com.hbm.ntm.block.MassStorageBlock;
import com.hbm.ntm.block.PowerDetectorBlock;
import com.hbm.ntm.block.PoweredRedCableBlock;
import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.block.RBMKConsoleBlock;
import com.hbm.ntm.block.RBMKCraneConsoleBlock;
import com.hbm.ntm.block.RadioboxBlock;
import com.hbm.ntm.block.RadioReceiverBlock;
import com.hbm.ntm.block.RedCableBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.block.RedCableGaugeBlock;
import com.hbm.ntm.block.RefineryBlock;
import com.hbm.ntm.block.SteelScaffoldBlock;
import com.hbm.ntm.block.SoyuzCapsuleBlock;
import com.hbm.ntm.block.VendingMachineBlock;
import com.hbm.ntm.block.WatzEndBlock;
import com.hbm.ntm.block.ZirnoxReactorBlock;
import com.hbm.ntm.block.conveyor.ConveyorBlock;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
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
        legacyStairsWithItem(ModBlocks.CONCRETE_SMOOTH_STAIRS, "concrete");
        legacyStairsWithItem(ModBlocks.CONCRETE_STAIRS, "concrete_tile");
        legacyStairsWithItem(ModBlocks.CONCRETE_ASBESTOS_STAIRS, "concrete_asbestos");
        legacyStairsWithItem(ModBlocks.DUCRETE_SMOOTH_STAIRS, "ducrete");
        legacyStairsWithItem(ModBlocks.DUCRETE_STAIRS, "ducrete_tile");
        legacyStairsWithItem(ModBlocks.BRICK_CONCRETE_STAIRS, "brick_concrete");
        legacyStairsWithItem(ModBlocks.BRICK_CONCRETE_MOSSY_STAIRS, "brick_concrete_mossy");
        legacyStairsWithItem(ModBlocks.BRICK_CONCRETE_CRACKED_STAIRS, "brick_concrete_cracked");
        legacyStairsWithItem(ModBlocks.BRICK_CONCRETE_BROKEN_STAIRS, "brick_concrete_broken");
        legacyStairsWithItem(ModBlocks.BRICK_DUCRETE_STAIRS, "brick_ducrete");
        legacyStairsWithItem(ModBlocks.REINFORCED_STONE_STAIRS, "reinforced_stone");
        legacyStairsWithItem(ModBlocks.REINFORCED_BRICK_STAIRS, "reinforced_brick");
        legacyStairsWithItem(ModBlocks.BRICK_OBSIDIAN_STAIRS, "brick_obsidian");
        legacyStairsWithItem(ModBlocks.BRICK_LIGHT_STAIRS, "brick_light");
        legacyStairsWithItem(ModBlocks.BRICK_COMPOUND_STAIRS, "brick_compound");
        legacyStairsWithItem(ModBlocks.BRICK_ASBESTOS_STAIRS, "brick_asbestos");
        legacyStairsWithItem(ModBlocks.BRICK_FIRE_STAIRS, "brick_fire");
        legacyStairsWithItem(ModBlocks.ASPHALT_STAIRS, "asphalt");
        legacyStairsWithItem(ModBlocks.LIGHTSTONE_TILE_STAIRS, "lightstone.tile");
        legacyStairsWithItem(ModBlocks.LIGHTSTONE_BRICKS_STAIRS, "lightstone.bricks");
        legacyLightstoneWithItem();
        legacyMultiSlabWithItem(ModBlocks.CONCRETE_SLAB,
                "concrete", "concrete_tile", "concrete_asbestos", "ducrete", "ducrete_tile", "asphalt");
        legacyMultiSlabWithItem(ModBlocks.CONCRETE_BRICK_SLAB,
                "brick_concrete", "brick_concrete_mossy", "brick_concrete_cracked", "brick_concrete_broken", "brick_ducrete");
        legacyMultiSlabWithItem(ModBlocks.BRICK_SLAB,
                "reinforced_stone", "reinforced_brick", "brick_obsidian", "brick_light", "brick_compound", "brick_asbestos", "brick_fire");
        legacyMultiSlabWithItem(ModBlocks.STONES_SLAB, "lightstone.tile", "lightstone.bricks");
        legacyDoubleSlabBlock(ModBlocks.CONCRETE_DOUBLE_SLAB, "concrete_slab");
        legacyDoubleSlabBlock(ModBlocks.CONCRETE_BRICK_DOUBLE_SLAB, "concrete_brick_slab");
        legacyDoubleSlabBlock(ModBlocks.BRICK_DOUBLE_SLAB, "brick_slab");
        legacyDoubleSlabBlock(ModBlocks.STONES_DOUBLE_SLAB, "stones_slab");
        existingModelWithItemNoRotation(ModBlocks.MACHINE_PRESS, "machine_press");
        cubeWithItem(ModBlocks.PRESS_PREHEATER, "press_preheater");
        electricPressWithItemRenderer(ModBlocks.MACHINE_EPRESS, "machine_epress");
        conveyorPressWithItem();
        pistonInserterWithItemRenderer();
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
        legacyMachineStaticWithCustomItem(ModBlocks.CHARGER, "charger",
                HbmBlockStateProvider::solidifierRotation);
        powerDetectorWithItem();
        hiddenBerBlockWithItem(ModBlocks.REFUELER);
        radioBoxWithItemRenderer();
        radioReceiverWithItemRenderer();
        teslaStaticWithItemRenderer();
        horizontalBlockNoRotationWithItem(ModBlocks.MACHINE_SHREDDER,
                "machine_shredder_bottom_alt",
                "machine_shredder_top_alt",
                "machine_shredder_front_alt",
                "machine_shredder_front_alt",
                "machine_shredder_side_alt",
                "machine_shredder_side_alt");
        simpleSidedCubeWithItem(ModBlocks.MACHINE_AUTOCRAFTER,
                "machine_autocrafter_bottom",
                "machine_autocrafter_top",
                "machine_autocrafter_side",
                "machine_autocrafter_side",
                "machine_autocrafter_side",
                "machine_autocrafter_side");
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
        filingCabinetWithItemRenderer();
        pedestalWithItem();
        existingModelWithCustomItemNoRotation(ModBlocks.BOXCAR, "boxcar");
        simpleSidedCubeWithItem(ModBlocks.MACHINE_WEAPON_TABLE,
                "gun_table_bottom",
                "gun_table_top",
                "gun_table_side",
                "gun_table_side",
                "gun_table_side",
                "gun_table_side");
        redCableWithItem();
        redCableClassicWithItem();
        redCablePaintableWithItem();
        redWireCoatedWithItem();
        redCableBoxWithItem();
        redCableGaugeWithItem();
        poweredRedCableWithItem(ModBlocks.CABLE_SWITCH, "cable_switch_off", "cable_switch_on");
        poweredRedCableWithItem(ModBlocks.CABLE_DETECTOR, "cable_detector_off", "cable_detector_on");
        cableDiodeWithItem();
        pylonWithItemRenderer(ModBlocks.RED_CONNECTOR, "network/connector");
        pylonWithItemRenderer(ModBlocks.RED_CONNECTOR_SUPER, "network/connector_super");
        smallPylonWithItemRenderer();
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
        legacyCapacitorWithItem(ModBlocks.CAPACITOR_COPPER, "capacitor_copper_block", "copper");
        legacyCapacitorWithItem(ModBlocks.CAPACITOR_GOLD, "capacitor_gold_block", "gold");
        legacyCapacitorWithItem(ModBlocks.CAPACITOR_NIOBIUM, "capacitor_niobium_block", "niobium");
        legacyCapacitorWithItem(ModBlocks.CAPACITOR_TANTALIUM, "capacitor_tantalium_block", "tantalium");
        legacyCapacitorWithItem(ModBlocks.CAPACITOR_SCHRABIDATE, "capacitor_schrabidate_block", "schrabidate");
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
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SATLINK, "machines/satlink");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_RADAR_SCREEN, "radar_screen",
                HbmBlockStateProvider::radarScreenChunkRotation);
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
        simpleCubeWithItem(ModBlocks.SEAL_FRAME, "seal_frame");
        simpleCubeWithItem(ModBlocks.SEAL_CONTROLLER, "seal_controller");
        simpleCubeWithItem(ModBlocks.VAULT_DOOR, "vault_door");
        simpleCubeWithItem(ModBlocks.BLAST_DOOR, "blast_door");
        simpleCubeWithItem(ModBlocks.FIRE_DOOR, "fire_door");
        simpleCubeWithItem(ModBlocks.TRANSITION_SEAL, "transition_seal");
        simpleCubeWithItem(ModBlocks.SLIDING_BLAST_DOOR, "sliding_blast_door");
        simpleCubeWithItem(ModBlocks.LARGE_VEHICLE_DOOR, "large_vehicle_door");
        simpleCubeWithItem(ModBlocks.WATER_DOOR, "water_door");
        simpleCubeWithItem(ModBlocks.QE_CONTAINMENT, "qe_containment_door");
        simpleCubeWithItem(ModBlocks.QE_SLIDING_DOOR, "qe_sliding_door");
        simpleCubeWithItem(ModBlocks.ROUND_AIRLOCK_DOOR, "round_airlock_door");
        simpleCubeWithItem(ModBlocks.SECURE_ACCESS_DOOR, "secure_access_door");
        simpleCubeWithItem(ModBlocks.SLIDING_SEAL_DOOR, "sliding_seal_door");
        simpleCubeWithItem(ModBlocks.SILO_HATCH, "silo_hatch");
        simpleCubeWithItem(ModBlocks.SILO_HATCH_LARGE, "silo_hatch_large");
        sidedCubeWithItem(ModBlocks.MACHINE_CONTROLLER,
                "machine_controller_top",
                "machine_controller_top",
                "machine_controller",
                "machine_controller_back",
                "machine_controller_side",
                "machine_controller_side");
        satDockWithItemRenderer();
        satelliteLinkWithItemRenderer();
        soyuzCapsuleWithItem();
        soyuzLauncherWithItemRenderer();
        simpleCubeWithItem(ModBlocks.STRUCT_LAUNCHER, "struct_launcher");
        simpleCubeWithItem(ModBlocks.STRUCT_SCAFFOLD, "struct_scaffold");
        simpleCubeWithItem(ModBlocks.STRUCT_LAUNCHER_CORE, "struct_launcher_core");
        simpleCubeWithItem(ModBlocks.STRUCT_LAUNCHER_CORE_LARGE, "struct_launcher_core_large");
        simpleCubeWithItem(ModBlocks.STRUCT_SOYUZ_CORE, "struct_soyuz_core");
        existingModelWithCustomItem(ModBlocks.LAUNCH_PAD, "launch_pad");
        existingModelWithCustomItem(ModBlocks.LAUNCH_PAD_LARGE, "launch_pad_large");
        existingModelWithCustomItem(ModBlocks.LAUNCH_PAD_RUSTED, "launch_pad_rusted");
        existingModelWithCustomItem(ModBlocks.LAUNCH_TABLE, "launch_table/launch_table_base");
        existingModelWithCustomItem(ModBlocks.COMPACT_LAUNCHER, "launch_table/compact_launcher");
        missileAssemblyWithCustomItem();
        existingModelWithItem(ModBlocks.RBMK_DISPLAY_BLANK, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_DISPLAY, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_GAUGE, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_GRAPH, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_INDICATOR, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_KEY_PAD, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_LEVER, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_NUMITRON, "rbmk_panel_base");
        existingModelWithItem(ModBlocks.RBMK_TERMINAL, "rbmk_panel_base");
        simpleCubeWithItem(ModBlocks.DECO_RBMK, "rbmk/rbmk_top");
        simpleCubeWithItem(ModBlocks.DECO_RBMK_SMOOTH, "rbmk/rbmk_blank_top");
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
        rbmkConsoleWithItem();
        rbmkCraneConsoleWithItem();
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL, "rbmk_control");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_MOD, "rbmk_control_mod");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_AUTO, "rbmk_control_auto");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_REASIM,
                "rbmk_control_reasim", "rbmk_control_reasim_bottom");
        rbmkOwnLidColumnWithItem(ModBlocks.RBMK_CONTROL_REASIM_AUTO,
                "rbmk_control_reasim_auto", "rbmk_control_reasim_auto_bottom");
        graphiteBlockWithItem();
        simpleBlock(ModBlocks.ORE_BEDROCK_OIL.get(),
                models().cubeAll("ore_bedrock_oil", modLoc("block/ore_bedrock_oil")));
        frameStateVisibleMachineWithItemRenderer(ModBlocks.MACHINE_ASSEMBLY_MACHINE,
                HbmBlockStateProvider::assemblyMachineRotation);
        forceFieldWithItem();
        frameStateVisibleMachineWithItemRenderer(ModBlocks.MACHINE_CHEMICAL_PLANT,
                HbmBlockStateProvider::solidifierRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_LIQUEFACTOR, "machines/liquefactor");
        frameStateVisibleMachineWithItemRenderer(ModBlocks.MACHINE_CHEMICAL_FACTORY,
                HbmBlockStateProvider::solidifierRotation);
        refineryWithItemRenderer();
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_CATALYTIC_CRACKER, "machine_catalytic_cracker",
                HbmBlockStateProvider::catalyticRotation);
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_CATALYTIC_REFORMER, "machine_catalytic_reformer",
                HbmBlockStateProvider::catalyticRotation);
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_VACUUM_DISTILL, "machine_vacuum_distill",
                HbmBlockStateProvider::noRotation);
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_FRACTION_TOWER, "machine_fraction_tower",
                HbmBlockStateProvider::noRotation);
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_HYDROTREATER, "machine_hydrotreater",
                HbmBlockStateProvider::noRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COKER, "machines/coker");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PYROOVEN, "machines/pyrooven");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SOLIDIFIER, "machines/solidifier");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COMPRESSOR, "machines/compressor");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_BAT9000, "machine_bat9000",
                HbmBlockStateProvider::bat9000Rotation);
        bigAssTankWithItemRenderer();
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_FLUIDTANK, "machine_fluidtank",
                HbmBlockStateProvider::southZeroRotation);
        hexafluorideTankWithCustomItem(ModBlocks.MACHINE_UF6_TANK, "machine_uf6_tank");
        hexafluorideTankWithCustomItem(ModBlocks.MACHINE_PUF6_TANK, "machine_puf6_tank");
        existingModelWithCustomItemNoRotation(ModBlocks.MACHINE_STORAGE_DRUM, "machine_storage_drum");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_WELL, "machine_well",
                HbmBlockStateProvider::southZeroRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PUMPJACK, "machines/pumpjack");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FRACKING_TOWER, "machines/fracking_tower");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_CENTRIFUGE, "machine_centrifuge",
                HbmBlockStateProvider::centrifugeRotation);
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_GASCENT, "machine_gascent",
                HbmBlockStateProvider::gasCentRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ORE_SLOPPER, "machines/ore_slopper");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SAWMILL, "machines/sawmill");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CRUCIBLE, "machines/crucible_heat");
        gasFlareWithItemRenderer();
        legacyMachineStaticWithCustomItem(ModBlocks.CHIMNEY_BRICK, "chimney_brick",
                HbmBlockStateProvider::fixed180Rotation);
        legacyMachineStaticWithCustomItem(ModBlocks.CHIMNEY_INDUSTRIAL, "chimney_industrial",
                HbmBlockStateProvider::fixed180Rotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_INTAKE, "machines/intake");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_DRAIN, "machine_drain",
                HbmBlockStateProvider::solidifierRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CHUNGUS, "machines/chungus");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_HEPHAESTUS, "machines/hephaestus");
        heatBoilerWithItemRenderer();
        industrialBoilerWithItemRenderer();
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COMBUSTION_ENGINE, "machines/combustion_engine");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_DIESEL, "machines/dieselgen");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_INDUSTRIAL_GENERATOR, "machines/igen");
        visibleMachineWithItemRenderer(ModBlocks.PUMP_STEAM, "machines/pump");
        visibleMachineWithItemRenderer(ModBlocks.PUMP_ELECTRIC, "machines/pump_electric");
        legacyMachineStaticWithCustomItem(ModBlocks.HEATER_HEATEX, "heater_heatex",
                HbmBlockStateProvider::solidifierRotation);
        visibleMachineWithItemRenderer(ModBlocks.HEATER_FIREBOX, "machines/firebox");
        visibleMachineWithItemRenderer(ModBlocks.HEATER_OVEN, "machines/heating_oven");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ASHPIT, "machines/heating_oven");
        legacyMachineStaticWithCustomItem(ModBlocks.HEATER_OILBURNER, "heater_oilburner",
                HbmBlockStateProvider::noRotation);
        legacyMachineStaticWithCustomItem(ModBlocks.HEATER_ELECTRIC, "heater_electric",
                HbmBlockStateProvider::solidifierRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CONDENSER_POWERED, "machines/condenser");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_COMPRESSOR_COMPACT, "machines/compressor_compact");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_LPW2, "reactors/lpw2");
        researchReactorWithItemRenderer();
        zirnoxWithItemRenderer();
        electricPressWithItemRenderer(ModBlocks.MACHINE_REACTOR_BREEDING, "machine_reactor_breeding");
        simpleCubeWithItem(ModBlocks.STRUCT_WATZ_CORE, "legacy_blocks/struct_watz_core");
        watzPillarWithItem(ModBlocks.WATZ_ELEMENT, "watz_element");
        watzPillarWithItem(ModBlocks.WATZ_COOLER, "watz_cooler");
        watzEndWithItem();
        dfcMachineStaticWithItem(ModBlocks.DFC_EMITTER, "dfc_emitter");
        dfcMachineStaticWithItem(ModBlocks.DFC_RECEIVER, "dfc_receiver");
        dfcMachineStaticWithItem(ModBlocks.DFC_INJECTOR, "dfc_injector");
        dfcMachineStaticWithItem(ModBlocks.DFC_STABILIZER, "dfc_stabilizer");
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
        cargoElevatorWithItemRenderer();
        frameStateVisibleMachineWithItemRenderer(ModBlocks.MACHINE_ASSEMBLY_FACTORY,
                HbmBlockStateProvider::solidifierRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PRECASS, "machines/precass");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PUREX, "machines/purex");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_SILEX, "machine_silex",
                HbmBlockStateProvider::solidifierRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_EXPOSURE_CHAMBER, "machines/exposure_chamber");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CYCLOTRON, "machines/cyclotron");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CRYSTALLIZER, "machines/acidizer");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_ELECTROLYSER, "machine_electrolyser",
                HbmBlockStateProvider::pyroOvenRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ARC_WELDER, "machines/arc_welder");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SOLDERING_STATION, "machines/soldering_station");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_MIXER, "machines/mixer");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_RADIOLYSIS, "machine_radiolysis",
                HbmBlockStateProvider::radiolysisRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RTG_GREY, "machines/rtg");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RADGEN, "machines/radgen");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ROTARY_FURNACE, "machines/rotary_furnace");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STEAM_ENGINE, "machines/steam_engine");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_SOLAR_BOILER, "machine_solar_boiler",
                HbmBlockStateProvider::solidifierRotation);
        solarMirrorBaseWithFullItem(ModBlocks.SOLAR_MIRROR);
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_TOWER_SMALL, "machine_tower_small",
                HbmBlockStateProvider::noRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TOWER_LARGE, "machines/tower_large");
        hiddenBerBlockWithItem(ModBlocks.FAN);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TURBOFAN, "machines/turbofan");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TURBINEGAS, "machines/turbinegas");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_AMMO_PRESS, "machines/ammo_press");
        visibleMachineWithItemRenderer(ModBlocks.FURNACE_IRON, "machines/furnace_iron");
        visibleMachineWithItemRenderer(ModBlocks.FURNACE_STEEL, "machines/furnace_steel");
        visibleMachineWithItemRenderer(ModBlocks.FURNACE_COMBINATION, "machines/combination_oven");
        blastFurnaceWithItemRenderer();
        arcFurnaceWithItemRenderer();
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ANNIHILATOR, "machines/annihilator");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_FEL, "machine_fel",
                HbmBlockStateProvider::northZeroRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ORBUS, "machines/orbus");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_MINING_LASER, "machine_mining_laser",
                HbmBlockStateProvider::noRotation);
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_EXCAVATOR, "machine_excavator",
                HbmBlockStateProvider::southZeroRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STRAND_CASTER, "machines/strand_caster");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_WOOD_BURNER, "machine_wood_burner",
                HbmBlockStateProvider::southZeroRotation);
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STIRLING, "machines/stirling");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STIRLING_STEEL, "machines/stirling");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STIRLING_CREATIVE, "machines/stirling");
        legacyMachineStaticWithCustomItem(ModBlocks.MACHINE_DEUTERIUM_TOWER, "machine_deuterium_tower",
                HbmBlockStateProvider::southZeroRotation);
        simpleSidedCubeWithItem(ModBlocks.MACHINE_DEUTERIUM_EXTRACTOR,
                "deuterium_extractor_top_water",
                "deuterium_extractor_top_water",
                "deuterium_extractor_side",
                "deuterium_extractor_side",
                "deuterium_extractor_side",
                "deuterium_extractor_side");
        legacyMachineStaticWithCustomItem(ModBlocks.FRACTION_SPACER, "fraction_spacer",
                HbmBlockStateProvider::noRotation);
        simpleSidedCubeWithItem(ModBlocks.TELEANCHOR,
                "tele_anchor_side",
                "tele_anchor_top",
                "tele_anchor_side",
                "tele_anchor_side",
                "tele_anchor_side",
                "tele_anchor_side");
        simpleCubeWithItem(ModBlocks.FIELD_DISTURBER, "field_disturber");
        industrialTurbineWithItemRenderer();
        largeTurbineWithItemRenderer();
        translucentCubeWithItem(ModBlocks.GLASS_BORON, "glass_boron");
        translucentCubeWithItem(ModBlocks.GLASS_LEAD, "glass_lead");
        translucentCubeWithItem(ModBlocks.GLASS_URANIUM, "glass_uranium");
        translucentCubeWithItem(ModBlocks.GLASS_POLONIUM, "glass_polonium");
        translucentCubeWithItem(ModBlocks.GLASS_POLARIZED, "glass_polarized");
        translucentCubeWithItem(ModBlocks.GLASS_QUARTZ, "glass_quartz");
        translucentCubeWithItem(ModBlocks.REINFORCED_GLASS, "reinforced_glass");
        reinforcedGlassPaneWithItem();
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
        simpleCubeWithItem(ModBlocks.GAS_FLAMMABLE, "gas_flammable");
        simpleCubeWithItem(ModBlocks.GAS_EXPLOSIVE, "gas_explosive");
        ventWithItem(ModBlocks.VENT_CHLORINE, "vent_chlorine");
        ventWithItem(ModBlocks.VENT_CLOUD, "vent_cloud");
        ventWithItem(ModBlocks.VENT_PINK_CLOUD, "vent_pink_cloud");
        cubeTopWithItem(ModBlocks.VENT_CHLORINE_SEAL, "vent_chlorine_seal_side", "vent_chlorine_seal_top");
        simpleCubeWithItem(ModBlocks.BROADCASTER_PC, "broadcaster_pc");
        geysirWithItem(ModBlocks.GEYSIR_CHLORINE, "minecraft:block/stone", "geysir_stone");
        geysirWithItem(ModBlocks.GEYSIR_NETHER, "minecraft:block/netherrack", "geysir_nether");
        simpleCubeWithItem("dirt_dead", "dirt_dead");
        simpleCubeWithItem("dirt_oily", "dirt_oily");
        simpleCubeWithItem("sand_dirty", "sand_dirty");
        simpleCubeWithItem("sand_dirty_red", "sand_dirty_red");
        simpleCubeWithItem("stone_cracked", "stone_cracked");
        simpleCubeWithItem("deepslate_ore_coltan", "deepslate_ore_coltan");
        simpleCubeWithItem("block_meteor", "meteor");
        simpleCubeWithItem("block_meteor_cobble", "meteor_cobble");
        simpleCubeWithItem("block_meteor_broken", "meteor_crushed");
        uberConcreteWithItem();
        simpleCubeWithItem(ModBlocks.BLOCK_METEOR_MOLTEN, "block_meteor_molten");
        simpleCubeWithItem("block_meteor_treasure", "meteor_treasure");
        simpleCubeWithItem(ModBlocks.METEOR_POLISHED, "meteor_polished");
        simpleCubeWithItem(ModBlocks.METEOR_BRICK, "meteor_brick");
        simpleCubeWithItem(ModBlocks.METEOR_BRICK_MOSSY, "meteor_brick_mossy");
        simpleCubeWithItem(ModBlocks.METEOR_BRICK_CRACKED, "meteor_brick_cracked");
        meteorPillarWithItem(ModBlocks.METEOR_PILLAR, "meteor_pillar", "meteor_pillar_top");
        meteorPillarWithItem(ModBlocks.METEOR_BATTERY, "meteor_spawner_side", "meteor_power");
        simpleCubeWithItem(ModBlocks.ORE_METEOR_IRON, "ore_meteor_iron");
        simpleCubeWithItem(ModBlocks.ORE_METEOR_COPPER, "ore_meteor_copper");
        simpleCubeWithItem(ModBlocks.ORE_METEOR_ALUMINIUM, "ore_meteor_aluminium");
        simpleCubeWithItem(ModBlocks.ORE_METEOR_RAREEARTH, "ore_meteor_rareearth");
        simpleCubeWithItem(ModBlocks.ORE_METEOR_COBALT, "ore_meteor_cobalt");
        radAbsorberWithItem();
        simpleCubeWithItem(ModBlocks.DUMMY_BLOCK, "block_steel");
        steelScaffoldWithItem();
        simpleCubeWithItem(ModBlocks.STEEL_BEAM, "steel_beam");
        steelGrateWithItem(ModBlocks.STEEL_GRATE, "steel_grate");
        steelGrateWithItem(ModBlocks.STEEL_GRATE_WIDE, "steel_grate_wide");
        chainWithItem();
        barbedWireWithItem(ModBlocks.BARBED_WIRE, "barbed_wire", "barbed_wire_model");
        barbedWireWithItem(ModBlocks.BARBED_WIRE_FIRE, "barbed_wire_fire", "barbed_wire_fire_model");
        barbedWireWithItem(ModBlocks.BARBED_WIRE_POISON, "barbed_wire_poison", "barbed_wire_poison_model");
        barbedWireWithItem(ModBlocks.BARBED_WIRE_ACID, "barbed_wire_acid", "barbed_wire_acid_model");
        barbedWireWithItem(ModBlocks.BARBED_WIRE_WITHER, "barbed_wire_wither", "barbed_wire_wither_model");
        barbedWireWithItem(ModBlocks.BARBED_WIRE_ULTRADEATH, "barbed_wire_ultradeath", "barbed_wire_ultradeath_model");
        spikesWithItem();
        existingModelWithItemNoRotation(ModBlocks.POLE_TOP, "pole_top");
        existingModelWithItem(ModBlocks.POLE_SATELLITE_RECEIVER, "pole_satellite_receiver");
        glowingMushWithItem();
        ntmFlowersWithItem();
        tallPlantsWithItem();
        deadPlantVariants();
        wasteLogWithItem();
        simpleCubeWithItem(ModBlocks.WASTE_PLANKS, "waste_planks");
        burningEarthWithItem();
        simpleCubeWithItem(ModBlocks.IMPACT_DIRT, "waste_earth_bottom");
        leavesLayerWithItem();
        simpleCubeWithItem(ModBlocks.BARRICADE, "barricade");
        sellafieldWithItem();
        sellafieldSlakedWithItem(ModBlocks.SELLAFIELD_SLAKED, "sellafield_slaked");
        sellafieldSlakedWithItem(ModBlocks.SELLAFIELD_BEDROCK, "sellafield_bedrock");
        bedrockOreDeposit("ore_bedrock_coltan");
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_DIAMOND, LegacySellafieldOreBlock.Kind.DIAMOND);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_EMERALD, LegacySellafieldOreBlock.Kind.EMERALD);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED, LegacySellafieldOreBlock.Kind.URANIUM_SCORCHED);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM, LegacySellafieldOreBlock.Kind.SCHRABIDIUM);
        sellafieldOreWithItem(ModBlocks.ORE_SELLAFIELD_RADGEM, LegacySellafieldOreBlock.Kind.RADGEM);
        trinititeOreWithItem(ModBlocks.WASTE_TRINITITE);
        trinititeOreWithItem(ModBlocks.WASTE_TRINITITE_RED);
        translucentCubeWithItem(ModBlocks.GLASS_TRINITITE, "glass_trinitite");
        simpleCubeWithItem(ModBlocks.ASH_DIGAMMA, "ash_digamma");
        crossBlockOnly(ModBlocks.FIRE_DIGAMMA, "fire_digamma");
        crossBlockOnly(ModBlocks.BALEFIRE, "balefire");
        crossWithItem(ModBlocks.STALACTITE_SULFUR, "stalactite.sulfur");
        crossWithItem(ModBlocks.STALACTITE_ASBESTOS, "stalactite.asbestos");
        crossWithItem(ModBlocks.STALAGMITE_SULFUR, "stalagmite.sulfur");
        crossWithItem(ModBlocks.STALAGMITE_ASBESTOS, "stalagmite.asbestos");
        pribrisDebrisWithItem(ModBlocks.PRIBRIS, "rbmk/rbmk_debris");
        pribrisDebrisWithItem(ModBlocks.PRIBRIS_BURNING, "rbmk/rbmk_debris_burning");
        pribrisDebrisAllStatesWithItem(ModBlocks.PRIBRIS_RADIATING, "rbmk/rbmk_debris_radiating");
        pribrisDebrisWithItem(ModBlocks.PRIBRIS_DIGAMMA, "rbmk/rbmk_debris_digamma");
        cokeBlockWithItem();
        concreteColoredWithItem();
        biomeStoneWithItem();
        phosphorVineWithItem();
        concreteColoredExtWithItem();
        woodStructureWithItem();
        basaltOreWithItem();
        fissureWithItem();
        liquidBlockOnly(ModBlocks.VOLCANIC_LAVA_BLOCK, "volcanic_lava_still", "volcanic_lava_flowing");
        liquidBlockOnly(ModBlocks.RAD_LAVA_BLOCK, "rad_lava_still", "rad_lava_flowing");
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
        simpleCubeWithItem("crystal_pulsar", "legacy_blocks/crystal_pulsar");
        glyphidBaseWithItem();
        glyphidSpawnerWithItem();
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
        empBombWithItem();
        floatBombWithItem();
        thermoBombWithItem(ModBlocks.THERM_ENDO, "therm_endo");
        thermoBombWithItem(ModBlocks.THERM_EXO, "therm_exo");
        landmineWithItem(ModBlocks.MINE_AP, "models/bombs/ap_mine.obj", "models/bombs/mine_ap_grass");
        landmineWithItem(ModBlocks.MINE_HE, "models/bombs/marelet.obj", "models/bombs/mine_marelet");
        landmineWithItem(ModBlocks.MINE_SHRAP, "models/bombs/ap_mine.obj", "models/bombs/mine_shrapnel");
        landmineWithItem(ModBlocks.MINE_FAT, "models/mine_fat.obj", "models/mine_fat");
        navalMineWithItem();
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

    private void empBombWithItem() {
        ModelFile model = models().cube("emp_bomb",
                modLoc("block/bomb_emp_top"),
                modLoc("block/bomb_emp_top"),
                modLoc("block/bomb_emp_side"),
                modLoc("block/bomb_emp_side"),
                modLoc("block/bomb_emp_side"),
                modLoc("block/bomb_emp_side"))
                .texture("particle", modLoc("block/bomb_emp_side"));
        simpleBlock(ModBlocks.EMP_BOMB.get(), model);
        simpleBlockItem(ModBlocks.EMP_BOMB.get(), model);
    }

    private void floatBombWithItem() {
        ModelFile model = models().cube("float_bomb",
                modLoc("block/bomb_float_top"),
                modLoc("block/bomb_float_top"),
                modLoc("block/bomb_float"),
                modLoc("block/bomb_float"),
                modLoc("block/bomb_float"),
                modLoc("block/bomb_float"))
                .texture("particle", modLoc("block/bomb_float"));
        simpleBlock(ModBlocks.FLOAT_BOMB.get(), model);
        simpleBlockItem(ModBlocks.FLOAT_BOMB.get(), model);
    }

    private void thermoBombWithItem(RegistryObject<Block> block, String textureName) {
        ModelFile model = models().cube(block.getId().getPath(),
                modLoc("block/therm_top"),
                modLoc("block/therm_top"),
                modLoc("block/" + textureName),
                modLoc("block/" + textureName),
                modLoc("block/" + textureName),
                modLoc("block/" + textureName))
                .texture("particle", modLoc("block/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void navalMineWithItem() {
        ResourceLocation texture = modLoc("block/bombs/naval_mine");
        ModelFile model = models().getBuilder("mine_naval")
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/block/bombs/naval_mine.obj"))
                .flipV(true)
                .end()
                .texture("particle", texture)
                .texture("default", texture)
                .texture("texture0", texture);
        simpleBlock(ModBlocks.MINE_NAVAL.get(), model);
        simpleBlockItem(ModBlocks.MINE_NAVAL.get(), model);
    }

    private void landmineWithItem(RegistryObject<Block> block, String objModel, String textureName) {
        ModelFile empty = new ModelFile.UncheckedModelFile(modLoc("block/empty"));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(empty).build());
        itemModels().getBuilder(block.getId().getPath())
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(modLoc(objModel))
                .flipV(true)
                .end()
                .texture("particle", modLoc(textureName))
                .texture("default", modLoc(textureName))
                .texture("texture0", modLoc(textureName));
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
        String blockName = block.getId().getPath();
        ModelFile up = new ModelFile.UncheckedModelFile(modLoc("block/" + blockName));
        ModelFile down = new ModelFile.UncheckedModelFile(modLoc("block/" + blockName + "_down"));
        ModelFile horizontal = new ModelFile.UncheckedModelFile(modLoc("block/" + blockName + "_horizontal"));
        getVariantBuilder(block.get())
                .forAllStates(state -> legacyChargeModel(state.getValue(LegacyChargeBlock.FACING),
                        up, down, horizontal));
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", modLoc("block/legacy_blocks/" + textureName));
    }

    private ConfiguredModel[] legacyChargeModel(Direction facing, ModelFile up, ModelFile down,
            ModelFile horizontal) {
        ConfiguredModel.Builder<?> builder = ConfiguredModel.builder();
        switch (facing) {
            case DOWN -> builder.modelFile(down);
            case NORTH -> builder.modelFile(horizontal).rotationY(90);
            case SOUTH -> builder.modelFile(horizontal).rotationY(270);
            case WEST -> builder.modelFile(horizontal).rotationY(180);
            case EAST -> builder.modelFile(horizontal);
            default -> builder.modelFile(up);
        }
        return builder.build();
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

    private void existingModelWithCustomItemNoRotation(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        simpleBlock(block.get(), model);
        customBlockItem(block);
    }

    private void existingModelWithCustomItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        horizontalBlock(block.get(), model);
        customBlockItem(block);
    }

    private void legacyMachineStaticWithCustomItem(RegistryObject<Block> block, String modelName,
            java.util.function.ToIntFunction<Direction> rotation) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(rotation.applyAsInt(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(block);
    }

    private void bigAssTankWithItemRenderer() {
        ModelFile body = new ModelFile.UncheckedModelFile(modLoc("block/machines/bigasstank"));
        ModelFile marker = new ModelFile.UncheckedModelFile(modLoc("block/machine_bigasstank"));
        getVariantBuilder(ModBlocks.MACHINE_BIGASSTANK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(BigAssTankBlock.TILTED) ? marker : body)
                        .rotationY(bat9000Rotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_BIGASSTANK);
    }

    private void blastFurnaceWithItemRenderer() {
        ModelFile body = new ModelFile.UncheckedModelFile(modLoc("block/machines/blast_furnace"));
        ModelFile marker = new ModelFile.UncheckedModelFile(modLoc("block/machine_blast_furnace"));
        getVariantBuilder(ModBlocks.MACHINE_BLAST_FURNACE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(BlastFurnaceBlock.TILTED) ? marker : body)
                        .rotationY(eastZeroRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_BLAST_FURNACE);
    }

    private void gasFlareWithItemRenderer() {
        ModelFile body = new ModelFile.UncheckedModelFile(modLoc("block/machines/flare_stack"));
        ModelFile marker = new ModelFile.UncheckedModelFile(modLoc("block/machine_flare"));
        getVariantBuilder(ModBlocks.MACHINE_GASFLARE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(GasFlareBlock.TILTED) ? marker : body)
                        .rotationY(fixed180Rotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_GASFLARE);
    }

    private void refineryWithItemRenderer() {
        ModelFile body = new ModelFile.UncheckedModelFile(modLoc("block/machines/refinery"));
        ModelFile marker = new ModelFile.UncheckedModelFile(modLoc("block/machine_refinery"));
        getVariantBuilder(ModBlocks.MACHINE_REFINERY.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(RefineryBlock.EXPLODED) || state.getValue(RefineryBlock.TILTED)
                                ? marker
                                : body)
                        .rotationY(fixed180Rotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_REFINERY);
    }

    private void zirnoxWithItemRenderer() {
        ModelFile body = new ModelFile.UncheckedModelFile(modLoc("block/reactors/zirnox"));
        ModelFile marker = new ModelFile.UncheckedModelFile(modLoc("block/reactor_zirnox"));
        getVariantBuilder(ModBlocks.REACTOR_ZIRNOX.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(ZirnoxReactorBlock.TILTED) ? marker : body)
                        .rotationY(zirnoxRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.REACTOR_ZIRNOX);
    }

    private void heatBoilerWithItemRenderer() {
        ModelFile normal = new ModelFile.UncheckedModelFile(modLoc("block/machines/boiler"));
        ModelFile dynamicMarker = new ModelFile.UncheckedModelFile(modLoc("block/machine_boiler"));
        getVariantBuilder(ModBlocks.MACHINE_BOILER.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(HeatBoilerBlock.VISUAL)
                                == HeatBoilerBlock.BoilerVisualState.NORMAL ? normal : dynamicMarker)
                        .rotationY(southZeroRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_BOILER);
    }

    private void industrialBoilerWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/machine_industrial_boiler"));
        getVariantBuilder(ModBlocks.MACHINE_INDUSTRIAL_BOILER.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        customBlockItem(ModBlocks.MACHINE_INDUSTRIAL_BOILER);
    }

    private void dfcMachineStaticWithItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        getVariantBuilder(block.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(DfcMachineBlock.FACING);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (facing) {
                        case DOWN -> builder.rotationX(90).rotationY(90);
                        case UP -> builder.rotationX(270).rotationY(90);
                        case NORTH -> builder.rotationY(180);
                        case EAST -> builder.rotationY(90);
                        case WEST -> builder.rotationY(270);
                        case SOUTH -> {
                        }
                    }
                    return builder.build();
                });
        simpleBlockItem(block.get(), model);
    }

    private void cargoElevatorWithItemRenderer() {
        ModelFile marker = new ModelFile.UncheckedModelFile(modLoc("block/cargo_elevator"));
        ModelFile base = new ModelFile.UncheckedModelFile(modLoc("block/cargo_elevator_base"));
        getVariantBuilder(ModBlocks.CARGO_ELEVATOR.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(CargoElevatorBlock.PLATFORM) ? base : marker)
                        .rotationY(cargoElevatorRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.CARGO_ELEVATOR);
    }

    private void electricPressWithItemRenderer(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(electricPressRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(block);
    }

    private void teslaStaticWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/tesla"));
        getVariantBuilder(ModBlocks.TESLA.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(180)
                        .build());
        customBlockItem(ModBlocks.TESLA);
    }

    private void missileAssemblyWithCustomItem() {
        ModelFile model = new ModelFile.UncheckedModelFile(
                new ResourceLocation(HbmNtm.MOD_ID, "block/machine_missile_assembly"));
        getVariantBuilder(ModBlocks.MACHINE_MISSILE_ASSEMBLY.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(missileAssemblyRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_MISSILE_ASSEMBLY);
    }

    private void hexafluorideTankWithCustomItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(hexafluorideTankRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(block);
    }

    private void filingCabinetWithItemRenderer() {
        ModelFile green = new ModelFile.UncheckedModelFile(
                new ResourceLocation(HbmNtm.MOD_ID, "block/filing_cabinet"));
        ModelFile steel = new ModelFile.UncheckedModelFile(
                new ResourceLocation(HbmNtm.MOD_ID, "block/filing_cabinet_steel"));
        getVariantBuilder(ModBlocks.FILING_CABINET.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(LegacyFileCabinetBlock.VARIANT) == 1 ? steel : green)
                        .rotationY(fileCabinetRotation(state.getValue(LegacyFileCabinetBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.FILING_CABINET);
    }

    private void pedestalWithItem() {
        ModelFile model = models().getExistingFile(new ResourceLocation(HbmNtm.MOD_ID, "block/pedestal"));
        simpleBlock(ModBlocks.PEDESTAL.get(), model);
        simpleBlockItem(ModBlocks.PEDESTAL.get(), model);
    }

    private void solarMirrorBaseWithFullItem(RegistryObject<Block> block) {
        ModelFile model = new ModelFile.UncheckedModelFile(
                new ResourceLocation(HbmNtm.MOD_ID, "block/machines/solar_mirror_base"));
        simpleBlock(block.get(), model);
        itemModels().withExistingParent(block.getId().getPath(),
                new ResourceLocation(HbmNtm.MOD_ID, "block/machines/solar_mirror"));
    }

    private static int electricPressRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static int solidifierRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    /**
     * Forge blockstate Y rotations use the opposite 90-degree sign from the
     * PoseStack/JOML rotation used by the legacy radar-screen BER.  The fixed
     * shell is chunk baked while its CRT overlay remains in the BER, so the
     * blockstate numbers must be the inverse of the 1.7.10 metadata yaw table.
     * Zero and 180 degrees are unchanged, which is why the regression was only
     * visible in the north/south audit captures.
     */
    private static int radarScreenChunkRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270;
            case WEST -> 180;
            case SOUTH -> 90;
            default -> 0;
        };
    }

    private static int assemblyMachineRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90;
            case WEST -> 180;
            case NORTH -> 270;
            default -> 0;
        };
    }

    private static int pyroOvenRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180;
            case WEST -> 270;
            case EAST -> 90;
            default -> 0;
        };
    }

    private static int centrifugeRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static int gasCentRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270;
            case SOUTH -> 90;
            case EAST -> 180;
            default -> 0;
        };
    }

    private static int bat9000Rotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270;
            case EAST -> 180;
            case SOUTH -> 90;
            default -> 0;
        };
    }

    private static int zirnoxRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static int eastZeroRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static int noRotation(Direction facing) {
        return 0;
    }

    private static int catalyticRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static int fixed180Rotation(Direction facing) {
        return 180;
    }

    private static int radiolysisRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180;
            case WEST -> 90;
            case EAST -> 270;
            default -> 0;
        };
    }

    private static int southZeroRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0;
            case EAST -> 90;
            case NORTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static int radioReceiverRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90;
            case NORTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    private static int northZeroRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    private static int cargoElevatorRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static int missileAssemblyRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180;
            case EAST -> 90;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static int largeTurbineRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180;
            case EAST -> 90;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static int industrialTurbineRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 0;
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static int vendingMachineRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case WEST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    private static int hexafluorideTankRotation(Direction facing) {
        return switch (facing) {
            case WEST -> 90;
            case SOUTH -> 180;
            case EAST -> 270;
            default -> 0;
        };
    }

    private static int fileCabinetRotation(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case NORTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    private void visibleMachineWithItemRenderer(RegistryObject<Block> block, String modelName) {
        ModelFile model = particleOnlyModel(block.getId().getPath(), modelName);
        horizontalBlock(block.get(), model);
        customBlockItem(block);
    }

    private void arcFurnaceWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/machines/arc_furnace"));
        getVariantBuilder(ModBlocks.MACHINE_ARC_FURNACE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(eastZeroRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_ARC_FURNACE);
    }

    private void soyuzLauncherWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/soyuz_launcher"));
        getVariantBuilder(ModBlocks.SOYUZ_LAUNCHER.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(eastZeroRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.SOYUZ_LAUNCHER);
    }

    private void satDockWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/sat_dock"));
        getVariantBuilder(ModBlocks.SAT_DOCK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        customBlockItem(ModBlocks.SAT_DOCK);
    }

    private void satelliteLinkWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/machine_satlink"));
        getVariantBuilder(ModBlocks.MACHINE_SATLINK.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(eastZeroRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_SATLINK);
    }

    private void researchReactorWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/reactors/reactor_small_base"));
        getVariantBuilder(ModBlocks.REACTOR_RESEARCH.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        customBlockItem(ModBlocks.REACTOR_RESEARCH);
    }

    private void largeTurbineWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/machine_large_turbine"));
        getVariantBuilder(ModBlocks.MACHINE_LARGE_TURBINE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(largeTurbineRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_LARGE_TURBINE);
    }

    private void industrialTurbineWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/machine_industrial_turbine"));
        getVariantBuilder(ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(industrialTurbineRotation(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.MACHINE_INDUSTRIAL_TURBINE);
    }

    private void frameStateVisibleMachineWithItemRenderer(RegistryObject<Block> block,
            java.util.function.ToIntFunction<Direction> rotation) {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/" + block.getId().getPath()));
        ModelFile frameModel = new ModelFile.UncheckedModelFile(modLoc("block/" + block.getId().getPath() + "_frame"));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(LegacyFrameRenderState.FRAME) ? frameModel : model)
                        .rotationY(rotation.applyAsInt(state.getValue(HorizontalMachineBlock.FACING)))
                        .build());
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

    private void radioBoxWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/radiobox"));
        getVariantBuilder(ModBlocks.RADIOBOX.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(southZeroRotation(state.getValue(RadioboxBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.RADIOBOX);
    }

    private void radioReceiverWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/radiorec"));
        getVariantBuilder(ModBlocks.RADIOREC.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(radioReceiverRotation(state.getValue(RadioReceiverBlock.FACING)))
                        .build());
        customBlockItem(ModBlocks.RADIOREC);
    }

    private void conveyorPressWithItem() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/machine_conveyor_press"));
        getVariantBuilder(ModBlocks.MACHINE_CONVEYOR_PRESS.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(switch (state.getValue(HorizontalMachineBlock.FACING)) {
                            case NORTH -> 90;
                            case WEST -> 180;
                            case SOUTH -> 270;
                            default -> 0;
                        })
                        .build());
        customBlockItem(ModBlocks.MACHINE_CONVEYOR_PRESS);
    }

    private void legacyCapacitorWithItem(RegistryObject<Block> block, String itemName, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile down = new ModelFile.UncheckedModelFile(modLoc("block/" + blockName + "_down"));
        ModelFile up = new ModelFile.UncheckedModelFile(modLoc("block/" + blockName + "_up"));
        ModelFile horizontal = new ModelFile.UncheckedModelFile(modLoc("block/" + blockName + "_horizontal"));
        getVariantBuilder(block.get())
                .forAllStates(state -> capacitorModel(state.getValue(CapacitorBlock.FACING),
                        up, down, horizontal));
        itemModels().getBuilder(itemName)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID,
                        "block/legacy_blocks/capacitor_" + textureName + "_top"));
    }

    private ConfiguredModel[] capacitorModel(Direction facing, ModelFile up, ModelFile down, ModelFile horizontal) {
        ConfiguredModel.Builder<?> builder = ConfiguredModel.builder();
        switch (facing) {
            case DOWN -> builder.modelFile(down);
            case NORTH -> builder.modelFile(horizontal).rotationY(90);
            case SOUTH -> builder.modelFile(horizontal).rotationY(270);
            case WEST -> builder.modelFile(horizontal).rotationY(180);
            case EAST -> builder.modelFile(horizontal);
            default -> builder.modelFile(up);
        }
        return builder.build();
    }

    private void graphiteBlockWithItem() {
        simpleCubeWithItem(ModBlocks.BLOCK_GRAPHITE, "block_graphite");
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
        ModelFile vertical = models().withExistingParent(block.getId().getPath(), mcLoc("block/cube_column"))
                .texture("particle", side)
                .texture("side", side)
                .texture("end", end);
        ModelFile horizontal = models().withExistingParent(block.getId().getPath() + "_horizontal",
                        mcLoc("block/cube_column_horizontal"))
                .texture("particle", side)
                .texture("side", side)
                .texture("end", end);
        getVariantBuilder(block.get())
                .forAllStates(state -> {
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder();
                    switch (state.getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                        case X -> builder.modelFile(horizontal).rotationX(90).rotationY(90);
                        case Z -> builder.modelFile(horizontal).rotationX(90);
                        case Y -> builder.modelFile(vertical);
                    }
                    return builder.build();
                });
        simpleBlockItem(block.get(), vertical);
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

    private void rbmkCraneConsoleWithItem() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/rbmk_crane_console"));
        getVariantBuilder(ModBlocks.RBMK_CRANE_CONSOLE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(switch (state.getValue(RBMKCraneConsoleBlock.FACING)) {
                            case NORTH -> 90;
                            case WEST -> 180;
                            case SOUTH -> 270;
                            default -> 0;
                        })
                        .build());
        customBlockItem(ModBlocks.RBMK_CRANE_CONSOLE);
    }

    private void rbmkConsoleWithItem() {
        ModelFile east = new ModelFile.UncheckedModelFile(modLoc("block/rbmk_console"));
        ModelFile north = new ModelFile.UncheckedModelFile(modLoc("block/rbmk_console_north"));
        ModelFile south = new ModelFile.UncheckedModelFile(modLoc("block/rbmk_console_south"));
        ModelFile west = new ModelFile.UncheckedModelFile(modLoc("block/rbmk_console_west"));
        getVariantBuilder(ModBlocks.RBMK_CONSOLE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(switch (state.getValue(RBMKConsoleBlock.FACING)) {
                            case NORTH -> north;
                            case SOUTH -> south;
                            case WEST -> west;
                            default -> east;
                        })
                        .build());
        customBlockItem(ModBlocks.RBMK_CONSOLE);
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


    private void vendingMachineWithItemRenderer() {
        ModelFile soda = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/vending_machine"));
        ModelFile obamna = new ModelFile.UncheckedModelFile(
                new ResourceLocation(HbmNtm.MOD_ID, "block/vending_machine_obamna"));
        getVariantBuilder(ModBlocks.VENDING_MACHINE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(VendingMachineBlock.VARIANT) == 0 ? soda : obamna)
                        .rotationY(vendingMachineRotation(state.getValue(VendingMachineBlock.FACING)))
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
        ModelFile base = new ModelFile.UncheckedModelFile(
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + ModBlocks.MACHINE_FORCEFIELD.getId().getPath()));
        getVariantBuilder(ModBlocks.MACHINE_FORCEFIELD.get())
                .partialState()
                .setModels(ConfiguredModel.builder()
                        .modelFile(base)
                        .rotationY(180)
                        .build());
        generatedBlockItem(ModBlocks.MACHINE_FORCEFIELD, "block/machine_forcefield");
    }

    private void generatedBlockItem(RegistryObject<Block> block, String texturePath) {
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, texturePath));
    }

    private void soyuzCapsuleWithItem() {
        ModelFile normal = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/soyuz_capsule"));
        ModelFile rusted = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/soyuz_capsule_rusted"));
        getVariantBuilder(ModBlocks.SOYUZ_CAPSULE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(SoyuzCapsuleBlock.RUSTED) ? rusted : normal)
                .build());
        generatedBlockItem(ModBlocks.SOYUZ_CAPSULE, "item/soyuz_lander");
    }

    private void fluidBarrelWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().getBuilder(blockName)
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/blocks/barrel.obj"))
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
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/blocks/scaffold.obj"))
                .flipV(true)
                .end()
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/scaffold_steel"))
                .texture("default", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/scaffold_steel"))
                .texture("texture0", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/scaffold_steel"));
        getVariantBuilder(ModBlocks.STEEL_SCAFFOLD.get())
                .forAllStates(state -> scaffoldModel(state.getValue(SteelScaffoldBlock.AXIS), model));
        simpleBlockItem(ModBlocks.STEEL_SCAFFOLD.get(), model);
    }

    private void barbedWireWithItem(RegistryObject<Block> block, String modelName, String textureName) {
        ModelFile model = models().getBuilder(modelName)
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/blocks/barbed_wire.obj"))
                .flipV(true)
                .automaticCulling(false)
                .end()
                .renderType("minecraft:cutout")
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .texture("default", new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .texture("texture0", new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(rotationY(state.getValue(LegacyBarbedWireBlock.FACING)))
                        .build());
        simpleBlockItem(block.get(), model);
    }

    private void pistonInserterWithItemRenderer() {
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/piston_inserter"));
        getVariantBuilder(ModBlocks.PISTON_INSERTER.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        itemModels().getBuilder(ModBlocks.PISTON_INSERTER.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("minecraft:builtin/entity"));
    }

    private void spikesWithItem() {
        ModelFile model = models().getBuilder("spikes")
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(new ResourceLocation(HbmNtm.MOD_ID, "models/blocks/spikes.obj"))
                .flipV(true)
                .automaticCulling(false)
                .end()
                .renderType("minecraft:cutout")
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/spikes"))
                .texture("default", new ResourceLocation(HbmNtm.MOD_ID, "block/spikes"))
                .texture("texture0", new ResourceLocation(HbmNtm.MOD_ID, "block/spikes"));
        simpleBlock(ModBlocks.SPIKES.get(), model);
        simpleBlockItem(ModBlocks.SPIKES.get(), model);
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

    private void cubeWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void simpleCubeWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void trinititeOreWithItem(RegistryObject<Block> block) {
        String blockName = block.getId().getPath();
        ModelFile[] variants = new ModelFile[4];
        for (int index = 0; index < variants.length; index++) {
            variants[index] = models().cubeAll(blockName + "_" + index,
                    modLoc("block/" + blockName + "_" + index));
        }
        getVariantBuilder(block.get()).forAllStates(state -> new ConfiguredModel[] {
                new ConfiguredModel(variants[0]),
                new ConfiguredModel(variants[1]),
                new ConfiguredModel(variants[2]),
                new ConfiguredModel(variants[3])
        });
        simpleBlockItem(block.get(), variants[0]);
    }

    private void ventWithItem(RegistryObject<Block> block, String sideTexture) {
        String name = block.getId().getPath();
        ModelFile model = models().cubeBottomTop(name,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + sideTexture),
                new ResourceLocation(HbmNtm.MOD_ID, "block/vent_blank"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/vent_blank"));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void cubeTopWithItem(RegistryObject<Block> block, String sideTexture, String topTexture) {
        String name = block.getId().getPath();
        ModelFile model = models().cubeBottomTop(name,
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + sideTexture),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + sideTexture),
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + topTexture));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void geysirWithItem(RegistryObject<Block> block, String sideTexture, String topTexture) {
        String name = block.getId().getPath();
        ModelFile model = models().cubeBottomTop(name,
                new ResourceLocation(sideTexture),
                new ResourceLocation(sideTexture),
                modLoc("block/" + topTexture));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void powerDetectorWithItem() {
        ModelFile off = models().cubeAll("machine_detector_off",
                new ResourceLocation(HbmNtm.MOD_ID, "block/machine_detector_off"));
        ModelFile on = models().cubeAll("machine_detector",
                new ResourceLocation(HbmNtm.MOD_ID, "block/machine_detector"));
        getVariantBuilder(ModBlocks.MACHINE_DETECTOR.get())
                .partialState().with(PowerDetectorBlock.ACTIVE, false)
                .setModels(new ConfiguredModel(off))
                .partialState().with(PowerDetectorBlock.ACTIVE, true)
                .setModels(new ConfiguredModel(on));
        simpleBlockItem(ModBlocks.MACHINE_DETECTOR.get(), off);
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

    private void liquidBlockOnly(RegistryObject<Block> block, String stillTexture, String flowingTexture) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cube(
                blockName,
                modLoc("block/" + stillTexture),
                modLoc("block/" + stillTexture),
                modLoc("block/" + flowingTexture),
                modLoc("block/" + flowingTexture),
                modLoc("block/" + flowingTexture),
                modLoc("block/" + flowingTexture))
                .texture("particle", modLoc("block/" + stillTexture))
                .renderType("minecraft:translucent");
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
    }

    private void basaltOreWithItem() {
        getVariantBuilder(ModBlocks.ORE_BASALT.get());
        for (LegacyBasaltOreBlock.Variant variant : LegacyBasaltOreBlock.Variant.values()) {
            ModelFile model = basaltOreModel(variant.textureName());
            getVariantBuilder(ModBlocks.ORE_BASALT.get())
                    .partialState().with(LegacyBasaltOreBlock.VARIANT, variant.legacyMeta())
                    .modelForState().modelFile(model).addModel();
        }
        simpleBlockItem(ModBlocks.ORE_BASALT.get(), models().getExistingFile(modLoc("block/ore_basalt_sulfur")));
    }

    private void cokeBlockWithItem() {
        getVariantBuilder(ModBlocks.BLOCK_COKE.get());
        for (LegacyCokeBlock.Variant variant : LegacyCokeBlock.Variant.values()) {
            ModelFile model = models().cubeAll(variant.modelName(), modLoc("block/" + variant.textureName()));
            getVariantBuilder(ModBlocks.BLOCK_COKE.get())
                    .partialState().with(LegacyCokeBlock.VARIANT, variant.legacyMeta())
                    .modelForState().modelFile(model).addModel();
        }
        var itemModel = itemModels().withExistingParent("block_coke", modLoc("block/block_coke_coal"));
        itemModel.override()
                .predicate(modLoc("legacy_variant"), 1)
                .model(itemModels().getExistingFile(modLoc("block/block_coke_lignite")))
                .end();
        itemModel.override()
                .predicate(modLoc("legacy_variant"), 2)
                .model(itemModels().getExistingFile(modLoc("block/block_coke_petroleum")))
                .end();
    }

    private void concreteColoredExtWithItem() {
        getVariantBuilder(ModBlocks.CONCRETE_COLORED_EXT.get());
        for (ConcreteColoredExtBlock.Variant variant : ConcreteColoredExtBlock.Variant.values()) {
            ModelFile model = concreteColoredExtModel(variant);
            getVariantBuilder(ModBlocks.CONCRETE_COLORED_EXT.get())
                    .partialState().with(ConcreteColoredExtBlock.VARIANT, variant.legacyMeta())
                    .modelForState().modelFile(model).addModel();
        }

        var itemModel = itemModels().withExistingParent("concrete_colored_ext",
                modLoc("block/concrete_colored_ext_machine"));
        for (ConcreteColoredExtBlock.Variant variant : ConcreteColoredExtBlock.Variant.values()) {
            if (variant.legacyMeta() == 0) {
                continue;
            }
            itemModel.override()
                    .predicate(modLoc("legacy_variant"), variant.legacyMeta())
                    .model(itemModels().getExistingFile(modLoc("block/" + variant.modelName())))
                    .end();
        }
    }

    private ModelFile concreteColoredExtModel(ConcreteColoredExtBlock.Variant variant) {
        if (variant == ConcreteColoredExtBlock.Variant.MACHINE_STRIPE) {
            ResourceLocation machine = modLoc("block/concrete_colored_ext.machine");
            ResourceLocation stripe = modLoc("block/" + variant.textureName());
            return models().cube(variant.modelName(), machine, machine, stripe, stripe, stripe, stripe)
                    .texture("particle", stripe);
        }
        return models().cubeAll(variant.modelName(), modLoc("block/" + variant.textureName()));
    }

    private void concreteColoredWithItem() {
        RegistryObject<? extends Block> block = requireLegacyBlock("concrete_colored");
        getVariantBuilder(block.get());
        for (ConcreteColoredBlock.Variant variant : ConcreteColoredBlock.Variant.values()) {
            ModelFile model = models().cubeAll(variant.modelName(), modLoc("block/" + variant.textureName()));
            getVariantBuilder(block.get())
                    .partialState().with(ConcreteColoredBlock.VARIANT, variant.legacyMeta())
                    .modelForState().modelFile(model).addModel();
        }

        var itemModel = itemModels().withExistingParent("concrete_colored", modLoc("block/concrete_colored_white"));
        for (ConcreteColoredBlock.Variant variant : ConcreteColoredBlock.Variant.values()) {
            if (variant.legacyMeta() == 0) {
                continue;
            }
            itemModel.override()
                    .predicate(modLoc("legacy_variant"), variant.legacyMeta())
                    .model(itemModels().getExistingFile(modLoc("block/" + variant.modelName())))
                    .end();
        }
    }

    private void fissureWithItem() {
        ModelFile model = models().getExistingFile(modLoc("block/ore_volcano"));
        getVariantBuilder(ModBlocks.ORE_VOLCANO.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        simpleBlockItem(ModBlocks.ORE_VOLCANO.get(), model);
    }

    private void woodStructureWithItem() {
        ModelFile roof = models().getExistingFile(modLoc("block/wood_structure_roof"));
        ModelFile ceiling = models().getExistingFile(modLoc("block/wood_structure_ceiling"));
        ModelFile postsShort = models().getExistingFile(modLoc("block/wood_structure_scaffold_posts_short"));
        ModelFile postsTall = models().getExistingFile(modLoc("block/wood_structure_scaffold_posts_tall"));
        ModelFile scaffoldInventory = models().getExistingFile(modLoc("block/wood_structure_scaffold_inventory"));
        ModelFile northBrace = models().getExistingFile(modLoc("block/wood_structure_scaffold_north"));
        ModelFile eastBrace = models().getExistingFile(modLoc("block/wood_structure_scaffold_east"));
        ModelFile southBrace = models().getExistingFile(modLoc("block/wood_structure_scaffold_south"));
        ModelFile westBrace = models().getExistingFile(modLoc("block/wood_structure_scaffold_west"));
        ModelFile top = models().getExistingFile(modLoc("block/wood_structure_scaffold_top"));

        var builder = getMultipartBuilder(ModBlocks.WOOD_STRUCTURE.get());
        builder.part().modelFile(roof).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.ROOF.legacyMeta()).end();
        builder.part().modelFile(ceiling).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.CEILING.legacyMeta()).end();
        builder.part().modelFile(postsShort).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .condition(LegacyWoodStructureBlock.UP, false).end();
        builder.part().modelFile(postsTall).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .condition(LegacyWoodStructureBlock.UP, true).end();
        builder.part().modelFile(northBrace).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .condition(LegacyWoodStructureBlock.NORTH, false).end();
        builder.part().modelFile(eastBrace).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .condition(LegacyWoodStructureBlock.EAST, false).end();
        builder.part().modelFile(southBrace).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .condition(LegacyWoodStructureBlock.SOUTH, false).end();
        builder.part().modelFile(westBrace).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .condition(LegacyWoodStructureBlock.WEST, false).end();
        builder.part().modelFile(top).addModel()
                .condition(LegacyWoodStructureBlock.VARIANT, LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .condition(LegacyWoodStructureBlock.UP, false).end();

        var itemModel = itemModels().withExistingParent("wood_structure", modLoc("block/wood_structure_roof"));
        itemModel.override().predicate(modLoc("legacy_variant"), LegacyWoodStructureBlock.Variant.SCAFFOLD.legacyMeta())
                .model(scaffoldInventory).end();
        itemModel.override().predicate(modLoc("legacy_variant"), LegacyWoodStructureBlock.Variant.CEILING.legacyMeta())
                .model(ceiling).end();
    }

    private ModelFile basaltOreModel(String textureName) {
        return models().cube(
                textureName,
                modLoc("block/" + textureName + "_top"),
                modLoc("block/" + textureName + "_top"),
                modLoc("block/" + textureName),
                modLoc("block/" + textureName),
                modLoc("block/" + textureName),
                modLoc("block/" + textureName))
                .texture("particle", modLoc("block/" + textureName));
    }

    private void crossBlockOnly(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cross(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:cutout");
        simpleBlock(block.get(), model);
    }

    private void crossWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cross(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:cutout");
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void glyphidBaseWithItem() {
        RegistryObject<? extends Block> block = requireLegacyBlock("glyphid_base");
        ModelFile standard = cubeModel("glyphid_base", "glyphid_base");
        ModelFile standardAlt = cubeModel("glyphid_base_alt", "glyphid_base_alt");
        ModelFile infested = cubeModel("glyphid_base_infested", "glyphid_base_infested");
        ModelFile infestedAlt = cubeModel("glyphid_base_infested_alt", "glyphid_base_infested_alt");
        ModelFile rad = cubeModel("glyphid_base_rad", "glyphid_base_rad");
        ModelFile radAlt = cubeModel("glyphid_base_rad_alt", "glyphid_base_rad_alt");
        getVariantBuilder(block.get())
                .partialState().with(LegacyGlyphidSpawnerBlock.VARIANT, 0)
                .setModels(new ConfiguredModel(standard), new ConfiguredModel(standardAlt))
                .partialState().with(LegacyGlyphidSpawnerBlock.VARIANT, 1)
                .setModels(new ConfiguredModel(infested), new ConfiguredModel(infestedAlt))
                .partialState().with(LegacyGlyphidSpawnerBlock.VARIANT, 2)
                .setModels(new ConfiguredModel(rad), new ConfiguredModel(radAlt));
        simpleBlockItem(block.get(), standard);
    }

    private void glyphidSpawnerWithItem() {
        RegistryObject<? extends Block> block = requireLegacyBlock("glyphid_spawner");
        ModelFile standard = cubeModel("glyphid_spawner", "glyphid_eggs_alt");
        ModelFile infested = cubeModel("glyphid_spawner_infested", "glyphid_eggs_infested");
        ModelFile rad = cubeModel("glyphid_spawner_rad", "glyphid_eggs_rad");
        getVariantBuilder(block.get())
                .partialState().with(LegacyGlyphidSpawnerBlock.VARIANT, 0)
                .modelForState().modelFile(standard).addModel()
                .partialState().with(LegacyGlyphidSpawnerBlock.VARIANT, 1)
                .modelForState().modelFile(infested).addModel()
                .partialState().with(LegacyGlyphidSpawnerBlock.VARIANT, 2)
                .modelForState().modelFile(rad).addModel();
        simpleBlockItem(block.get(), standard);
    }

    private RegistryObject<? extends Block> requireLegacyBlock(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block hbm_ntm_rebirth:" + legacyName);
        }
        return block;
    }

    private ModelFile cubeModel(String modelName, String textureName) {
        return models().cubeAll(modelName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
    }

    private void ntmFlowersWithItem() {
        for (LegacyNtmFlowerBlock.Kind kind : LegacyNtmFlowerBlock.Kind.values()) {
            RegistryObject<Block> block = ModBlocks.PLANT_FLOWER_BLOCKS.get(kind.legacyMeta());
            crossWithItem(block, kind.textureName());
        }
    }

    private void simpleCube(String legacyName, String textureName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block hbm_ntm_rebirth:" + legacyName);
        }
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        simpleBlock(block.get(), model);
    }

    private void bedrockOreDeposit(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block hbm_ntm_rebirth:" + legacyName);
        }
        String blockName = block.getId().getPath();
        ModelFile model = models().withExistingParent(blockName, new ResourceLocation("minecraft", "block/bedrock"));
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

    private void reinforcedGlassPaneWithItem() {
        ResourceLocation pane = new ResourceLocation(HbmNtm.MOD_ID, "block/reinforced_glass_pane");
        ResourceLocation edge = new ResourceLocation(HbmNtm.MOD_ID, "block/reinforced_glass_pane_edge");
        paneBlockWithRenderType((LegacyNtmGlassPaneBlock) ModBlocks.REINFORCED_GLASS_PANE.get(),
                pane, edge, "minecraft:translucent");
        itemModels().withExistingParent("reinforced_glass_pane",
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

    private void burningEarthWithItem() {
        ModelFile model = models().cubeBottomTop("burning_earth",
                new ResourceLocation(HbmNtm.MOD_ID, "block/burning_grass_side"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/waste_earth_bottom"),
                new ResourceLocation(HbmNtm.MOD_ID, "block/burning_grass_top"));
        simpleBlock(ModBlocks.BURNING_EARTH.get(), model);
        simpleBlockItem(ModBlocks.BURNING_EARTH.get(), model);
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

    private void legacyLightstoneWithItem() {
        String[] textures = {"lightstone.unrefined", "lightstone.tile", "lightstone.bricks",
                "lightstone.bricks_chiseled", "lightstone.chiseled"};
        for (int variant = 0; variant < textures.length; variant++) {
            String texture = textures[variant];
            ModelFile model = variant < 3
                    ? models().cubeAll("lightstone_" + variant, modLoc("block/" + texture))
                    : models().cube("lightstone_" + variant, modLoc("block/" + texture),
                            modLoc("block/" + texture + ".top"), modLoc("block/" + texture),
                            modLoc("block/" + texture), modLoc("block/" + texture), modLoc("block/" + texture))
                    .texture("particle", modLoc("block/" + texture));
            getVariantBuilder(ModBlocks.LIGHTSTONE.get()).partialState()
                    .with(LegacyLightstoneBlock.VARIANT, variant).modelForState().modelFile(model).addModel();
        }
        var item = itemModels().withExistingParent("lightstone", modLoc("block/lightstone_0"));
        for (int variant = 1; variant < textures.length; variant++) {
            item.override().predicate(modLoc("legacy_variant"), variant)
                    .model(itemModels().getExistingFile(modLoc("block/lightstone_" + variant))).end();
        }
    }

    private void biomeStoneWithItem() {
        ModelFile desertLayer = models().cubeBottomTop("stone_biome_desert_layer",
                modLoc("block/stone_biome_layer.desert"), modLoc("block/stone_biome_top.desert"),
                modLoc("block/stone_biome_top.desert"));
        ModelFile desertBase = models().cubeBottomTop("stone_biome_desert_base",
                modLoc("block/stone_biome.desert"), modLoc("block/stone_biome_top.desert"),
                modLoc("block/stone_biome_top.desert"));
        ModelFile woodlandLayer = models().cubeBottomTop("stone_biome_woodland_layer",
                modLoc("block/stone_biome_layer.woodland"), modLoc("block/stone_biome_top.woodland"),
                modLoc("block/stone_biome_top.woodland"));
        ModelFile woodlandBase = models().cubeBottomTop("stone_biome_woodland_base",
                modLoc("block/stone_biome.woodland"), modLoc("block/stone_biome_top.woodland"),
                modLoc("block/stone_biome_top.woodland"));

        getVariantBuilder(ModBlocks.STONE_BIOME.get())
                .partialState().with(LegacyBiomeStoneBlock.VARIANT, 0).with(LegacyBiomeStoneBlock.SAME_ABOVE, false)
                .modelForState().modelFile(desertLayer).addModel()
                .partialState().with(LegacyBiomeStoneBlock.VARIANT, 0).with(LegacyBiomeStoneBlock.SAME_ABOVE, true)
                .modelForState().modelFile(desertBase).addModel()
                .partialState().with(LegacyBiomeStoneBlock.VARIANT, 1).with(LegacyBiomeStoneBlock.SAME_ABOVE, false)
                .modelForState().modelFile(woodlandLayer).addModel()
                .partialState().with(LegacyBiomeStoneBlock.VARIANT, 1).with(LegacyBiomeStoneBlock.SAME_ABOVE, true)
                .modelForState().modelFile(woodlandBase).addModel();

        itemModels().withExistingParent("stone_biome", modLoc("block/stone_biome_desert_layer"))
                .override().predicate(modLoc("legacy_variant"), 1.0F)
                .model(itemModels().getExistingFile(modLoc("block/stone_biome_woodland_layer"))).end();
    }

    private void phosphorVineWithItem() {
        ModelFile world = models().cross("vine_phosphor", modLoc("block/vine_phosphor"));
        simpleBlock(ModBlocks.VINE_PHOSPHOR.get(), world);
        itemModels().getBuilder("vine_phosphor")
                .parent(itemModels().getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("block/vine_phosphor_item"));
    }

    private void legacyMultiSlabWithItem(RegistryObject<Block> block, String... textures) {
        legacyMultiSlabBlock(block, textures);
        var item = itemModels().withExistingParent(block.getId().getPath(),
                modLoc("block/" + block.getId().getPath() + "_0_bottom"));
        for (int variant = 1; variant < textures.length; variant++) {
            item.override().predicate(modLoc("legacy_variant"), variant)
                    .model(itemModels().getExistingFile(modLoc("block/" + block.getId().getPath() + "_" + variant + "_bottom")))
                    .end();
        }
    }

    private void legacyMultiSlabBlock(RegistryObject<Block> block, String... textures) {
        for (int variant = 0; variant < 8; variant++) {
            String suffix = block.getId().getPath() + "_" + variant;
            ResourceLocation texture = modLoc("block/" + textures[variant % textures.length]);
            ModelFile bottom = models().withExistingParent(suffix + "_bottom", mcLoc("block/slab"))
                    .texture("bottom", texture).texture("top", texture).texture("side", texture);
            ModelFile top = models().withExistingParent(suffix + "_top", mcLoc("block/slab_top"))
                    .texture("bottom", texture).texture("top", texture).texture("side", texture);
            ModelFile full = models().cubeAll(suffix + "_double", texture);
            getVariantBuilder(block.get()).partialState().with(LegacyMultiSlabBlock.VARIANT, variant)
                    .with(SlabBlock.TYPE, SlabType.BOTTOM).modelForState().modelFile(bottom).addModel();
            getVariantBuilder(block.get()).partialState().with(LegacyMultiSlabBlock.VARIANT, variant)
                    .with(SlabBlock.TYPE, SlabType.TOP).modelForState().modelFile(top).addModel();
            getVariantBuilder(block.get()).partialState().with(LegacyMultiSlabBlock.VARIANT, variant)
                    .with(SlabBlock.TYPE, SlabType.DOUBLE).modelForState().modelFile(full).addModel();
        }
    }

    private void legacyDoubleSlabBlock(RegistryObject<Block> block, String singleSlabName) {
        for (int variant = 0; variant < 8; variant++) {
            String source = singleSlabName + "_" + variant;
            getVariantBuilder(block.get()).partialState().with(LegacyMultiSlabBlock.VARIANT, variant)
                    .with(SlabBlock.TYPE, SlabType.BOTTOM).modelForState()
                    .modelFile(models().getExistingFile(modLoc("block/" + source + "_bottom"))).addModel();
            getVariantBuilder(block.get()).partialState().with(LegacyMultiSlabBlock.VARIANT, variant)
                    .with(SlabBlock.TYPE, SlabType.TOP).modelForState()
                    .modelFile(models().getExistingFile(modLoc("block/" + source + "_top"))).addModel();
            getVariantBuilder(block.get()).partialState().with(LegacyMultiSlabBlock.VARIANT, variant)
                    .with(SlabBlock.TYPE, SlabType.DOUBLE).modelForState()
                    .modelFile(models().getExistingFile(modLoc("block/" + source + "_double"))).addModel();
        }
    }

    private void legacyStairsWithItem(RegistryObject<Block> block, String texture) {
        stairsBlock((StairBlock) block.get(), new ResourceLocation(HbmNtm.MOD_ID, "block/" + texture));
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + block.getId().getPath())));
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
        ModelFile core = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_core"));
        ModelFile straightZ = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_straight_z"));
        ModelFile straightX = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_straight_x"));
        ModelFile straightY = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_straight_y"));
        ModelFile north = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_north"));
        ModelFile east = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_east"));
        ModelFile south = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_south"));
        ModelFile west = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_west"));
        ModelFile up = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_up"));
        ModelFile down = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_down"));
        var builder = getMultipartBuilder(ModBlocks.RED_CABLE.get());
        builder.part().modelFile(core).addModel()
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.JUNCTION).end();
        builder.part().modelFile(straightZ).addModel()
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.STRAIGHT_Z).end();
        builder.part().modelFile(straightX).addModel()
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.STRAIGHT_X).end();
        builder.part().modelFile(straightY).addModel()
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.STRAIGHT_Y).end();
        builder.part().modelFile(north).addModel()
                .condition(HbmEnergyNodeBlock.NORTH, true)
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.JUNCTION).end();
        builder.part().modelFile(east).addModel()
                .condition(HbmEnergyNodeBlock.EAST, true)
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.JUNCTION).end();
        builder.part().modelFile(south).addModel()
                .condition(HbmEnergyNodeBlock.SOUTH, true)
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.JUNCTION).end();
        builder.part().modelFile(west).addModel()
                .condition(HbmEnergyNodeBlock.WEST, true)
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.JUNCTION).end();
        builder.part().modelFile(up).addModel()
                .condition(HbmEnergyNodeBlock.UP, true)
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.JUNCTION).end();
        builder.part().modelFile(down).addModel()
                .condition(HbmEnergyNodeBlock.DOWN, true)
                .condition(RedCableBlock.CENTER, RedCableBlock.CenterVisual.JUNCTION).end();
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

    private void tallPlantsWithItem() {
        for (LegacyTallPlantBlock.Kind kind : LegacyTallPlantBlock.Kind.values()) {
            RegistryObject<Block> block = ModBlocks.PLANT_TALL_BLOCKS.get(kind.legacyMeta());
            String name = block.getId().getPath();
            ModelFile lower = models().cross(name + "_lower", modLoc("block/" + kind.textureName() + ".lower"))
                    .renderType("minecraft:cutout");
            ModelFile upper = models().cross(name + "_upper", modLoc("block/" + kind.textureName() + ".upper"))
                    .renderType("minecraft:cutout");
            getVariantBuilder(block.get())
                    .partialState().with(LegacyTallPlantBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)
                    .setModels(new ConfiguredModel(lower))
                    .partialState().with(LegacyTallPlantBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)
                    .setModels(new ConfiguredModel(upper));
            simpleBlockItem(block.get(), lower);
        }
    }

    private void deadPlantVariants() {
        for (LegacyDeadPlantBlock.Type type : LegacyDeadPlantBlock.Type.values()) {
            String name = "plant_dead_bigflower_" + type.getSerializedName();
            ModelFile model = models().cross(name, modLoc("block/plant_dead." + type.getSerializedName()))
                    .renderType("minecraft:cutout");
            getVariantBuilder(ModBlocks.PLANT_DEAD_BIGFLOWER.get())
                    .partialState().with(LegacyDeadPlantBlock.TYPE, type)
                    .setModels(new ConfiguredModel(model));
        }
    }

    private void redCablePaintableWithItem() {
        ModelFile model = new ModelFile.UncheckedModelFile(ModBlocks.RED_CABLE_PAINTABLE.getId()
                .withPrefix("block/"));
        getMultipartBuilder(ModBlocks.RED_CABLE_PAINTABLE.get()).part().modelFile(model).addModel().end();
        itemModels().getBuilder(ModBlocks.RED_CABLE_PAINTABLE.getId().getPath()).parent(model);
    }

    private void redWireCoatedWithItem() {
        ModelFile model = new ModelFile.UncheckedModelFile(ModBlocks.RED_WIRE_COATED.getId().withPrefix("block/"));
        simpleBlock(ModBlocks.RED_WIRE_COATED.get(), model);
        itemModels().getBuilder(ModBlocks.RED_WIRE_COATED.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/cube_all"))
                .texture("all", new ResourceLocation(HbmNtm.MOD_ID, "block/red_wire_coated"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/red_wire_coated"));
    }

    private void redCableBoxWithItem() {
        ModelFile model = new ModelFile.UncheckedModelFile(ModBlocks.RED_CABLE_BOX.getId().withPrefix("block/"));
        simpleBlock(ModBlocks.RED_CABLE_BOX.get(), model);
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
        ModelFile body = new ModelFile.UncheckedModelFile(ModBlocks.CABLE_DIODE.getId().withPrefix("block/"));
        ModelFile north = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_north"));
        ModelFile east = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_east"));
        ModelFile south = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_south"));
        ModelFile west = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_west"));
        ModelFile up = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_up"));
        ModelFile down = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/red_cable_arm_down"));
        var builder = getMultipartBuilder(ModBlocks.CABLE_DIODE.get());
        builder.part().modelFile(body).addModel().end();
        builder.part().modelFile(north).addModel().condition(HbmEnergyNodeBlock.NORTH, true).end();
        builder.part().modelFile(east).addModel().condition(HbmEnergyNodeBlock.EAST, true).end();
        builder.part().modelFile(south).addModel().condition(HbmEnergyNodeBlock.SOUTH, true).end();
        builder.part().modelFile(west).addModel().condition(HbmEnergyNodeBlock.WEST, true).end();
        builder.part().modelFile(up).addModel().condition(HbmEnergyNodeBlock.UP, true).end();
        builder.part().modelFile(down).addModel().condition(HbmEnergyNodeBlock.DOWN, true).end();
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
        ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/fluid_duct_neo"));
        getMultipartBuilder(ModBlocks.FLUID_DUCT_NEO.get())
                .part()
                .modelFile(model)
                .addModel()
                .end();
        customBlockItem(ModBlocks.FLUID_DUCT_NEO);
    }

    private void addFluidPipePart(net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder builder,
            int style, FluidPipeBlock.ShapeVisual shape, ModelFile model) {
        builder.part()
                .modelFile(model)
                .addModel()
                .condition(FluidPipeBlock.LEGACY_STYLE, style)
                .condition(FluidPipeBlock.SHAPE, shape)
                .end();
    }

    private void addFluidPipePart(net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder builder,
            int style, FluidPipeBlock.ShapeVisual shape, ModelFile model,
            net.minecraft.world.level.block.state.properties.BooleanProperty property, boolean value) {
        builder.part()
                .modelFile(model)
                .addModel()
                .condition(FluidPipeBlock.LEGACY_STYLE, style)
                .condition(FluidPipeBlock.SHAPE, shape)
                .condition(property, value)
                .end();
    }

    private void addFluidPipePart(net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder builder,
            int style, String octantModel,
            net.minecraft.world.level.block.state.properties.BooleanProperty firstProperty, boolean firstValue,
            net.minecraft.world.level.block.state.properties.BooleanProperty secondProperty, boolean secondValue,
            net.minecraft.world.level.block.state.properties.BooleanProperty thirdProperty, boolean thirdValue) {
        builder.part()
                .modelFile(new ModelFile.UncheckedModelFile(modLoc("block/fluid_duct_neo/style_" + style + "/"
                        + octantModel)))
                .addModel()
                .condition(FluidPipeBlock.LEGACY_STYLE, style)
                .condition(FluidPipeBlock.SHAPE, FluidPipeBlock.ShapeVisual.COMPLEX)
                .condition(firstProperty, firstValue)
                .condition(secondProperty, secondValue)
                .condition(thirdProperty, thirdValue)
                .end();
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
        ModelFile model = new ModelFile.UncheckedModelFile(block.getId().withPrefix("block/"));
        getMultipartBuilder(block.get())
                .part()
                .modelFile(model)
                .addModel()
                .end();
        customBlockItem(block);
    }

    private void smallPylonWithItemRenderer() {
        ModelFile model = models().getExistingFile(new ResourceLocation(HbmNtm.MOD_ID, "block/red_pylon"));
        getVariantBuilder(ModBlocks.RED_PYLON.get())
                .partialState()
                .setModels(ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        customBlockItem(ModBlocks.RED_PYLON);
        getVariantBuilder(ModBlocks.RED_PYLON_STEEL.get())
                .partialState()
                .setModels(ConfiguredModel.builder()
                        .modelFile(model)
                        .build());
        customBlockItem(ModBlocks.RED_PYLON_STEEL);
    }

    private void fluidDuctExhaustWithItem() {
        ModelFile model = new ModelFile.UncheckedModelFile(
                ModBlocks.FLUID_DUCT_EXHAUST.getId().withPrefix("block/"));
        getMultipartBuilder(ModBlocks.FLUID_DUCT_EXHAUST.get())
                .part()
                .modelFile(model)
                .addModel()
                .end();
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
        ModelFile model = new ModelFile.UncheckedModelFile(block.getId().withPrefix("block/"));
        getMultipartBuilder(block.get())
                .part()
                .modelFile(model)
                .addModel()
                .end();
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

    private void meteorPillarWithItem(RegistryObject<Block> block, String sideTexture, String topTexture) {
        ResourceLocation side = new ResourceLocation(HbmNtm.MOD_ID, "block/" + sideTexture);
        ResourceLocation top = new ResourceLocation(HbmNtm.MOD_ID, "block/" + topTexture);
        axisBlock((net.minecraft.world.level.block.RotatedPillarBlock) block.get(), side, top);
        simpleBlockItem(block.get(), new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                "block/" + block.getId().getPath())));
    }

    private void uberConcreteWithItem() {
        ModelFile intact = models().cubeAll("concrete_super", new ResourceLocation(HbmNtm.MOD_ID, "block/concrete_super"));
        ModelFile m0 = models().cubeAll("concrete_super_m0", new ResourceLocation(HbmNtm.MOD_ID, "block/concrete_super_m0"));
        ModelFile m1 = models().cubeAll("concrete_super_m1", new ResourceLocation(HbmNtm.MOD_ID, "block/concrete_super_m1"));
        ModelFile m2 = models().cubeAll("concrete_super_m2", new ResourceLocation(HbmNtm.MOD_ID, "block/concrete_super_m2"));
        ModelFile m3 = models().cubeAll("concrete_super_m3", new ResourceLocation(HbmNtm.MOD_ID, "block/concrete_super_m3"));
        getVariantBuilder(ModBlocks.CONCRETE_SUPER.get()).forAllStates(state -> {
            int damage = state.getValue(com.hbm.ntm.block.LegacyUberConcreteBlock.DAMAGE);
            ModelFile model = damage == 15 ? m3 : damage >= 14 ? m2 : damage >= 12 ? m1 : damage >= 10 ? m0 : intact;
            return ConfiguredModel.builder().modelFile(model).build();
        });
        simpleBlockItem(ModBlocks.CONCRETE_SUPER.get(), intact);
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
