package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.block.BoilerBlock;
import com.hbm.ntm.block.DeconBlock;
import com.hbm.ntm.block.FalloutLayerBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyComplexShapeBlock;
import com.hbm.ntm.block.LegacyDemonLampBlock;
import com.hbm.ntm.block.LegacyGasMeltdownBlock;
import com.hbm.ntm.block.LegacyGasRadonBlock;
import com.hbm.ntm.block.LegacyNuclearWasteBlock;
import com.hbm.ntm.block.LegacyOutgasBlock;
import com.hbm.ntm.block.MachineBlockEntityBlock;
import com.hbm.ntm.block.LegacyLanternBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.MachineBatteryBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacyRadiationBarrelBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacyToxicGasBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.RadiatingHazardBlock;
import com.hbm.ntm.block.RadioactiveWasteEarthBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.block.RedCableBlock;
import com.hbm.ntm.block.TrinketBlock;
import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.block.conveyor.ChuteConveyorBlock;
import com.hbm.ntm.block.conveyor.ConveyorBlock;
import com.hbm.ntm.block.conveyor.DoubleConveyorBlock;
import com.hbm.ntm.block.conveyor.ExpressConveyorBlock;
import com.hbm.ntm.block.conveyor.LiftConveyorBlock;
import com.hbm.ntm.block.conveyor.TripleConveyorBlock;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.MultiblockBlockItem;
import com.hbm.ntm.item.TrinketBlockItem;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.DummyBlock;
import com.hbm.ntm.multiblock.MultiblockExtents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HbmNtm.MOD_ID);
    private static final Map<String, RegistryObject<? extends Block>> BLOCKS_BY_LEGACY_NAME = new LinkedHashMap<>();

    // Legacy 1.7.10 machine IDs. Only machine_press has the first BlockEntity scaffold so far.
    public static final RegistryObject<Block> MACHINE_PRESS = basicMachine("machine_press");
    public static final RegistryObject<Block> MACHINE_DIFURNACE_OFF = machine("machine_difurnace_off");
    public static final RegistryObject<Block> MACHINE_ELECTRIC_FURNACE_OFF = machine("machine_electric_furnace_off");
    public static final RegistryObject<Block> MACHINE_BOILER_OFF = boilerMachine("machine_boiler_off");
    public static final RegistryObject<Block> MACHINE_SHREDDER = machine("machine_shredder");
    public static final RegistryObject<Block> DECON = decon("decon");
    public static final RegistryObject<Block> RED_CABLE = redCable("red_cable");
    public static final RegistryObject<Block> FLUID_DUCT_NEO = fluidPipe("fluid_duct_neo");
    public static final RegistryObject<Block> CONVEYOR = conveyor("conveyor", ConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_EXPRESS = conveyor("conveyor_express", ExpressConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_DOUBLE = conveyor("conveyor_double", DoubleConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_TRIPLE = conveyor("conveyor_triple", TripleConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_LIFT = conveyor("conveyor_lift", LiftConveyorBlock::new);
    public static final RegistryObject<Block> CONVEYOR_CHUTE = conveyor("conveyor_chute", ChuteConveyorBlock::new);
    public static final RegistryObject<Block> MACHINE_BATTERY = machineBattery("machine_battery");
    public static final RegistryObject<Block> MACHINE_BATTERY_SOCKET = machineBatterySocket("machine_battery_socket");
    public static final RegistryObject<Block> GAS_RADON = gasRadon("gas_radon", LegacyGasRadonBlock.Kind.NORMAL);
    public static final RegistryObject<Block> GAS_RADON_DENSE = gasRadon("gas_radon_dense", LegacyGasRadonBlock.Kind.DENSE);
    public static final RegistryObject<Block> GAS_RADON_TOMB = gasRadon("gas_radon_tomb", LegacyGasRadonBlock.Kind.TOMB);
    public static final RegistryObject<Block> GAS_MELTDOWN = gasMeltdown("gas_meltdown");
    public static final RegistryObject<Block> GAS_MONOXIDE = toxicGas("gas_monoxide", LegacyToxicGasBlock.Kind.MONOXIDE);
    public static final RegistryObject<Block> GAS_ASBESTOS = toxicGas("gas_asbestos", LegacyToxicGasBlock.Kind.ASBESTOS);
    public static final RegistryObject<Block> GAS_COAL = toxicGas("gas_coal", LegacyToxicGasBlock.Kind.COAL);
    public static final RegistryObject<Block> CHLORINE_GAS = toxicGas("chlorine_gas", LegacyToxicGasBlock.Kind.CHLORINE);
    public static final RegistryObject<Block> RAD_ABSORBER = radAbsorber("rad_absorber");
    public static final RegistryObject<Block> DUMMY_BLOCK = dummyBlock("dummy_block");
    public static final RegistryObject<Block> MACHINE_ASSEMBLY_MACHINE = assemblyMachine("machine_assembly_machine");
    public static final RegistryObject<Block> MACHINE_CHEMICAL_PLANT = visibleMultiblockMachine("machine_chemical_plant",
            chemicalPlantDefinition());
    public static final RegistryObject<Block> MACHINE_CHEMICAL_FACTORY = visibleMultiblockMachine("machine_chemical_factory",
            chemicalFactoryDefinition());
    public static final RegistryObject<Block> MACHINE_REFINERY = visibleMultiblockMachine("machine_refinery",
            refineryDefinition());
    public static final RegistryObject<Block> MACHINE_FLUIDTANK = visibleMultiblockMachine("machine_fluidtank",
            fluidTankDefinition());
    public static final RegistryObject<Block> MACHINE_PUMPJACK = visibleMultiblockMachine("machine_pumpjack",
            pumpjackDefinition());

    // Legacy 1.7.10 blockTab entries used as an early chunk-radiation test bed.
    public static final RegistryObject<Block> WASTE_EARTH = wasteEarth("waste_earth", false);
    public static final RegistryObject<Block> WASTE_MYCELIUM = wasteEarth("waste_mycelium", true);
    public static final RegistryObject<Block> WASTE_LEAVES = registerBlockWithItem("waste_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.1F)
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)));
    public static final RegistryObject<Block> FALLOUT = falloutLayer("fallout");
    public static final RegistryObject<Block> SELLAFIELD = sellafield("sellafield");
    public static final RegistryObject<Block> SELLAFIELD_SLAKED = simpleBlock("sellafield_slaked", "sellafield_slaked");

    // Legacy 1.7.10 nuclear device IDs. These are model-only placeholders for now.
    public static final RegistryObject<Block> NUKE_GADGET = nonOccludingMachine("nuke_gadget");
    public static final RegistryObject<Block> NUKE_BOY = nonOccludingMachine("nuke_boy");
    public static final RegistryObject<Block> NUKE_MAN = nonOccludingMachine("nuke_man");
    public static final RegistryObject<Block> NUKE_TSAR = nonOccludingMachine("nuke_tsar");
    public static final RegistryObject<Block> NUKE_MIKE = nonOccludingMachine("nuke_mike");
    public static final RegistryObject<Block> NUKE_PROTOTYPE = nonOccludingMachine("nuke_prototype");
    public static final RegistryObject<Block> NUKE_FLEIJA = nonOccludingMachine("nuke_fleija");
    public static final RegistryObject<Block> NUKE_SOLINIUM = nonOccludingMachine("nuke_solinium");
    public static final RegistryObject<Block> NUKE_N2 = nonOccludingMachine("nuke_n2");
    public static final RegistryObject<Block> NUKE_FSTBMB = nonOccludingMachine("nuke_fstbmb");
    public static final RegistryObject<Block> BOMB_MULTI = nonOccludingMachine("bomb_multi");
    public static final RegistryObject<Block> YELLOW_BARREL = radiationBarrel("yellow_barrel", 5.0F);
    public static final RegistryObject<Block> VITRIFIED_BARREL = radiationBarrel("vitrified_barrel", 0.5F);

    public static final List<RegistryObject<Block>> CONVEYOR_BLOCKS = List.of(
            CONVEYOR,
            CONVEYOR_EXPRESS,
            CONVEYOR_DOUBLE,
            CONVEYOR_TRIPLE,
            CONVEYOR_LIFT,
            CONVEYOR_CHUTE
    );

    public static final List<RegistryObject<Block>> MACHINE_TAB_BLOCKS = List.of(
            MACHINE_PRESS,
            MACHINE_DIFURNACE_OFF,
            MACHINE_ELECTRIC_FURNACE_OFF,
            MACHINE_BOILER_OFF,
            MACHINE_SHREDDER,
            DECON,
            RED_CABLE,
            FLUID_DUCT_NEO,
            MACHINE_BATTERY,
            MACHINE_BATTERY_SOCKET,
            MACHINE_ASSEMBLY_MACHINE,
            MACHINE_CHEMICAL_PLANT,
            MACHINE_CHEMICAL_FACTORY,
            MACHINE_REFINERY,
            MACHINE_FLUIDTANK,
            MACHINE_PUMPJACK,
            GAS_RADON,
            GAS_RADON_DENSE,
            GAS_RADON_TOMB,
            GAS_MELTDOWN,
            GAS_MONOXIDE,
            GAS_ASBESTOS,
            GAS_COAL,
            CHLORINE_GAS,
            RAD_ABSORBER
    );

    public static final List<RegistryObject<Block>> EXTRA_BLOCK_TAB_BLOCKS = simpleResourceBlocks(
            "ore_uranium:ore_uranium",
            "ore_uranium_scorched:ore_uranium_scorched",
            "ore_titanium:ore_titanium",
            "ore_sulfur:ore_sulfur",
            "ore_thorium:ore_thorium",
            "ore_niter:ore_niter",
            "ore_copper:ore_copper",
            "ore_tungsten:ore_tungsten",
            "ore_aluminium:ore_aluminium",
            "ore_fluorite:ore_fluorite",
            "ore_lead:ore_lead",
            "ore_schrabidium:ore_schrabidium",
            "ore_beryllium:ore_beryllium",
            "ore_lignite:ore_lignite",
            "ore_asbestos:ore_asbestos",
            "cluster_iron:cluster_iron",
            "cluster_titanium:cluster_titanium",
            "cluster_aluminium:cluster_aluminium",
            "cluster_copper:cluster_copper",
            "ore_nether_coal:ore_nether_coal",
            "ore_nether_smoldering:ore_nether_smoldering",
            "ore_nether_uranium:ore_nether_uranium",
            "ore_nether_uranium_scorched:ore_nether_uranium_scorched",
            "ore_nether_plutonium:ore_nether_plutonium",
            "ore_nether_tungsten:ore_nether_tungsten",
            "ore_nether_sulfur:ore_nether_sulfur",
            "ore_nether_fire:ore_nether_fire",
            "ore_nether_cobalt:ore_nether_cobalt",
            "ore_nether_schrabidium:ore_nether_schrabidium",
            "stone_gneiss:stone_gneiss_var",
            "ore_gneiss_iron:ore_gneiss_iron",
            "ore_gneiss_gold:ore_gneiss_gold",
            "ore_gneiss_uranium:ore_gneiss_uranium",
            "ore_gneiss_uranium_scorched:ore_gneiss_uranium_scorched",
            "ore_gneiss_copper:ore_gneiss_copper",
            "ore_gneiss_asbestos:ore_gneiss_asbestos",
            "ore_gneiss_lithium:ore_gneiss_lithium",
            "ore_gneiss_schrabidium:ore_gneiss_schrabidium",
            "ore_gneiss_rare:ore_gneiss_rare",
            "ore_gneiss_gas:ore_gneiss_gas",
            "gneiss_brick:gneiss_brick",
            "gneiss_tile:gneiss_tile",
            "gneiss_chiseled:gneiss_chiseled",
            "stone_depth:stone_depth",
            "ore_depth_cinnebar:ore_depth_cinnebar",
            "ore_depth_zirconium:ore_depth_zirconium",
            "ore_depth_borax:ore_depth_borax",
            "cluster_depth_iron:cluster_depth_iron",
            "cluster_depth_titanium:cluster_depth_titanium",
            "cluster_depth_tungsten:cluster_depth_tungsten",
            "ore_alexandrite:ore_alexandrite",
            "depth_brick:depth_brick",
            "depth_tiles:depth_tiles",
            "depth_nether_brick:depth_nether_brick",
            "depth_nether_tiles:depth_nether_tiles",
            "depth_dnt:depth_dnt",
            "stone_depth_nether:stone_depth_nether",
            "ore_depth_nether_neodymium:ore_depth_nether_neodymium",
            "stone_porous:stone_porous",
            "basalt:basalt",
            "basalt_smooth:basalt_smooth",
            "basalt_brick:basalt_brick",
            "basalt_polished:basalt_polished",
            "basalt_tiles:basalt_tiles",
            "ore_australium:ore_australium",
            "ore_rare:ore_rare",
            "ore_cobalt:ore_cobalt",
            "ore_cinnebar:ore_cinnebar",
            "ore_coltan:ore_coltan",
            "ore_oil:ore_oil",
            "ore_oil_empty:ore_oil_empty",
            "ore_oil_sand:ore_oil_sand_alt",
            "ore_bedrock_oil:ore_bedrock_oil",
            "ore_tikite:ore_tikite_alt",
            "block_uranium:block_uranium",
            "block_u233:block_u233",
            "block_u235:block_u235",
            "block_u238:block_u238",
            "block_uranium_fuel:block_uranium_fuel",
            "block_thorium:block_thorium",
            "block_thorium_fuel:block_thorium_fuel",
            "block_neptunium:block_neptunium",
            "block_polonium:block_polonium",
            "block_mox_fuel:block_mox_fuel",
            "block_plutonium:block_plutonium",
            "block_pu238:block_pu238",
            "block_pu239:block_pu239",
            "block_pu240:block_pu240",
            "block_pu_mix:block_pu_mix",
            "block_plutonium_fuel:block_plutonium_fuel",
            "block_titanium:block_titanium",
            "block_sulfur:block_sulfur",
            "block_niter:block_niter",
            "block_copper:block_copper",
            "block_red_copper:block_red_copper",
            "block_tungsten:block_tungsten",
            "block_aluminium:block_aluminium",
            "block_fluorite:block_fluorite",
            "block_steel:block_steel",
            "block_tcalloy:block_tcalloy",
            "block_cdalloy:block_cdalloy",
            "block_lead:block_lead",
            "block_bismuth:block_bismuth",
            "block_cadmium:block_cadmium",
            "block_coltan:block_coltan",
            "block_tantalium:block_tantalium",
            "block_trinitite:block_trinitite",
            "block_waste:block_waste",
            "block_waste_painted:block_waste_painted",
            "block_waste_vitrified:block_waste_vitrified",
            "ancient_scrap:ancient_scrap",
            "block_corium:block_corium",
            "block_corium_cobble:block_corium_cobble",
            "block_scrap:block_scrap",
            "block_electrical_scrap:electrical_scrap",
            "block_beryllium:block_beryllium",
            "block_schraranium:block_schraranium",
            "block_schrabidium:block_schrabidium",
            "block_schrabidate:block_schrabidate",
            "block_solinium:block_solinium",
            "block_schrabidium_fuel:block_schrabidium_fuel",
            "block_euphemium:block_euphemium",
            "block_dineutronium:block_dineutronium",
            "block_schrabidium_cluster:block_schrabidium_cluster_side",
            "block_euphemium_cluster:block_euphemium_cluster_side",
            "block_advanced_alloy:block_advanced_alloy",
            "block_magnetized_tungsten:block_magnetized_tungsten",
            "block_combine_steel:block_combine_steel",
            "block_desh:block_desh",
            "block_dura_steel:block_dura_steel",
            "block_starmetal:block_starmetal",
            "block_polymer:block_polymer",
            "block_bakelite:block_bakelite",
            "block_rubber:block_rubber",
            "block_yellowcake:block_yellowcake",
            "block_insulator:block_insulator_side",
            "block_fiberglass:block_fiberglass_side",
            "block_asbestos:block_asbestos",
            "block_cobalt:block_cobalt",
            "block_lithium:block_lithium",
            "block_zirconium:block_zirconium",
            "block_white_phosphorus:block_white_phosphorus",
            "block_red_phosphorus:block_red_phosphorus",
            "block_fallout:ash",
            "block_foam:foam",
            "block_boron:block_boron",
            "block_lanthanium:block_lanthanium",
            "block_ra226:block_ra226",
            "block_actinium:block_actinium",
            "block_tritium:block_tritium_side",
            "block_semtex:block_semtex",
            "block_c4:block_c4",
            "block_smore:block_smore_side",
            "block_slag:block_slag",
            "block_australium:block_australium",
            "deco_titanium:deco_titanium",
            "deco_red_copper:deco_red_copper",
            "deco_tungsten:deco_tungsten",
            "deco_aluminium:deco_aluminium",
            "deco_steel:deco_steel",
            "deco_rusty_steel:deco_rusty_steel",
            "deco_lead:deco_lead",
            "deco_beryllium:deco_beryllium",
            "deco_asbestos:deco_asbestos",
            "deco_rbmk:rbmk/rbmk_top",
            "deco_rbmk_smooth:rbmk/rbmk_blank_top",
            "deco_emitter:emitter",
            "part_emitter:part_top",
            "bobblehead:block_steel",
            "snowglobe:glass_boron",
            "plushie:block_fiberglass_side",
            "gravel_obsidian:gravel_obsidian",
            "gravel_diamond:gravel_diamond",
            "asphalt:asphalt",
            "asphalt_light:asphalt_light",
            "sandbags:sandbags",
            "wood_barrier:wood_barrier",
            "wood_structure:wood_barrier",
            "reinforced_brick:reinforced_brick",
            "reinforced_light:reinforced_light",
            "reinforced_sand:reinforced_sand",
            "reinforced_lamp_off:reinforced_lamp_off",
            "lamp_tritium_green_off:lamp_tritium_green_off",
            "lamp_tritium_blue_off:lamp_tritium_blue_off",
            "lamp_demon:lamp_demon",
            "lantern:block_steel",
            "spotlight_incandescent:cage_lamp",
            "spotlight_fluoro:fluorescent_lamp",
            "spotlight_halogen:flood_lamp",
            "floodlight:block_steel",
            "rebar:rebar",
            "reinforced_stone:reinforced_stone",
            "concrete_smooth:concrete",
            "concrete_colored:concrete",
            "concrete:concrete_tile",
            "concrete_asbestos:concrete_asbestos",
            "concrete_rebar:concrete_rebar",
            "concrete_super_broken:concrete_super_broken",
            "concrete_pillar:concrete_pillar_side",
            "brick_concrete:brick_concrete",
            "brick_concrete_mossy:brick_concrete_mossy",
            "brick_concrete_cracked:brick_concrete_cracked",
            "brick_concrete_broken:brick_concrete_broken",
            "brick_concrete_marked:brick_concrete_marked",
            "brick_obsidian:brick_obsidian",
            "brick_light:brick_light",
            "brick_compound:brick_compound",
            "cmb_brick:cmb_brick",
            "cmb_brick_reinforced:cmb_brick_reinforced",
            "brick_asbestos:brick_asbestos",
            "brick_fire:brick_fire",
            "ducrete_smooth:ducrete",
            "ducrete:ducrete_tile",
            "brick_ducrete:brick_ducrete",
            "reinforced_ducrete:reinforced_ducrete",
            "tile_lab:tile_lab",
            "tile_lab_cracked:tile_lab_cracked",
            "tile_lab_broken:tile_lab_broken",
            "block_meteor:meteor",
            "block_meteor_cobble:meteor_cobble",
            "__end__:__end__"
    );

    public static final List<RegistryObject<Block>> BLOCK_TAB_BLOCKS = Stream.concat(
            Stream.of(WASTE_EARTH, WASTE_MYCELIUM, WASTE_LEAVES, SELLAFIELD, SELLAFIELD_SLAKED),
            EXTRA_BLOCK_TAB_BLOCKS.stream()).toList();

    public static final List<RegistryObject<Block>> NUKE_TAB_BLOCKS = List.of(
            NUKE_GADGET,
            NUKE_BOY,
            NUKE_MAN,
            NUKE_TSAR,
            NUKE_MIKE,
            NUKE_PROTOTYPE,
            NUKE_FLEIJA,
            NUKE_SOLINIUM,
            NUKE_N2,
            NUKE_FSTBMB,
            BOMB_MULTI,
            YELLOW_BARREL,
            VITRIFIED_BARREL
    );

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    public static RegistryObject<? extends Block> legacyBlock(String name) {
        return BLOCKS_BY_LEGACY_NAME.get(name);
    }

    private static RegistryObject<Block> machine(String name) {
        return registerBlockWithItem(name, () -> new HorizontalMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> nonOccludingMachine(String name) {
        return registerBlockWithItem(name, () -> new HorizontalMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), false));
    }

    private static RegistryObject<Block> basicMachine(String name) {
        return registerBlockWithItem(name, () -> new MachineBlockEntityBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion(), false));
    }

    private static RegistryObject<Block> boilerMachine(String name) {
        return registerBlockWithItem(name, () -> new BoilerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> decon(String name) {
        return registerBlockWithItem(name, () -> new DeconBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> redCable(String name) {
        return registerBlockWithItem(name, () -> new RedCableBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> fluidPipe(String name) {
        return registerBlockWithItem(name, () -> new FluidPipeBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static <T extends Block> RegistryObject<T> conveyor(String name, Function<BlockBehaviour.Properties, T> factory) {
        return registerBlockWithItem(name, () -> factory.apply(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0F, 2.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()));
    }

    private static RegistryObject<Block> gasMeltdown(String name) {
        return registerBlockWithItem(name, () -> new LegacyGasMeltdownBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F, 0.0F)
                .noCollission()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> gasRadon(String name, LegacyGasRadonBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyGasRadonBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F, 0.0F)
                .noCollission()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false), kind));
    }

    private static RegistryObject<Block> toxicGas(String name, LegacyToxicGasBlock.Kind kind) {
        return registerBlockWithItem(name, () -> new LegacyToxicGasBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.0F, 0.0F)
                .noCollission()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false), kind));
    }

    private static RegistryObject<Block> radAbsorber(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacyRadAbsorberBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()),
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), LegacyRadAbsorberBlock.TIER, 4,
                        variant -> Component.translatable(variant == 0
                                ? "block.hbm.rad_absorber"
                                : "block.hbm.rad_absorber." + variant)));
    }

    private static RegistryObject<Block> dummyBlock(String name) {
        return registerBlockWithItem(name, () -> new DummyBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
    }

    private static RegistryObject<Block> assemblyMachine(String name) {
        return registerBlockWithItem(
                name,
                () -> new AssemblyMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> visibleMultiblockMachine(String name, LegacyMachineDefinition definition) {
        return registerBlockWithItem(
                name,
                () -> new LegacyVisibleMultiblockMachineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 30.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion(), definition),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static LegacyMachineDefinition chemicalPlantDefinition() {
        return LegacyMachineDefinition.builder(machineModel("chemical_plant"), machineTexture("chemical_plant"))
                .legacyXrDimensions(2, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXr(new int[] { 2, 0, 1, 1, 1, 1 }, facing)
                        .withProxyPredicate(offset -> offset.getY() == 0
                                && Math.abs(offset.getX()) <= 1
                                && Math.abs(offset.getZ()) <= 1
                                && (offset.getX() != 0 || offset.getZ() != 0)))
                .renderParts("Base", "Frame", "Slider", "Spinner")
                .build();
    }

    private static LegacyMachineDefinition chemicalFactoryDefinition() {
        return LegacyMachineDefinition.builder(machineModel("chemical_factory"), machineTexture("chemical_factory"))
                .legacyXrDimensions(2, 0, 2, 2, 2, 2)
                .legacyOffset(2)
                .layout(facing -> {
                    Direction rot = facing.getClockWise();
                    return LegacyMultiblockLayout.ofLegacyXr(new int[] { 2, 0, 2, 2, 2, 2 }, facing)
                            .withProxyPredicate(offset -> isChemicalFactoryProxyOffset(offset, facing, rot));
                })
                .renderParts("Base", "Frame", "Fan1", "Fan2")
                .build();
    }

    private static boolean isChemicalFactoryProxyOffset(BlockPos offset, Direction facing, Direction rot) {
        boolean floorRing = offset.getY() == 0
                && (Math.abs(offset.getX()) == 2 || Math.abs(offset.getZ()) == 2)
                && Math.abs(offset.getX()) <= 2
                && Math.abs(offset.getZ()) <= 2;
        if (floorRing) {
            return true;
        }
        if (offset.getY() != 2) {
            return false;
        }
        int alongFacing = offset.getX() * facing.getStepX() + offset.getZ() * facing.getStepZ();
        int alongRot = offset.getX() * rot.getStepX() + offset.getZ() * rot.getStepZ();
        return Math.abs(alongFacing) <= 2 && Math.abs(alongRot) == 2;
    }

    private static LegacyMachineDefinition refineryDefinition() {
        return LegacyMachineDefinition.builder(machineModel("refinery"), machineTexture("refinery"))
                .legacyXrDimensions(8, 0, 1, 1, 1, 1)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXr(new int[] { 8, 0, 1, 1, 1, 1 }, facing)
                        .withExtraProxyOffsets(cornerProxyOffsets(facing)))
                .yRotationOffset(180.0F)
                .build();
    }

    private static LegacyMachineDefinition fluidTankDefinition() {
        return LegacyMachineDefinition.builder(machineModel("fluidtank"), machineTexture("fluidtank"))
                .legacyXrDimensions(2, 0, 1, 1, 2, 2)
                .legacyOffset(1)
                .layout(facing -> LegacyMultiblockLayout.ofLegacyXr(new int[] { 2, 0, 1, 1, 2, 2 }, facing)
                        .withExtraProxyOffsets(cornerProxyOffsets(facing)))
                .yRotationOffset(180.0F)
                .build();
    }

    private static LegacyMachineDefinition pumpjackDefinition() {
        return LegacyMachineDefinition.builder(machineModel("pumpjack"), machineTexture("pumpjack"))
                .legacyXrDimensions(3, 0, 0, 0, 0, 6)
                .legacyOffset(0)
                .layout(facing -> {
                    Direction rot = facing.getCounterClockWise();
                    BlockPos offsetCore = new BlockPos(rot.getStepX() * 3, 0, rot.getStepZ() * 3);
                    List<BlockPos> filledOffsets = pumpjackFilledOffsets(facing, offsetCore);
                    Set<BlockPos> proxyOffsets = new LinkedHashSet<>(pumpjackCornerProxyOffsets(offsetCore));
                    return LegacyMultiblockLayout.ofLegacyXr(new int[] { 3, 0, 0, 0, 0, 6 }, facing)
                            .withExtraOffsets(filledOffsets, proxyOffsets::contains)
                            .withCheckOnlyOffsets(pumpjackCheckOnlyOffsets(facing, offsetCore));
                })
                .renderParts("Base", "Rotor", "Head", "Carriage")
                .yRotation(facing -> 270.0F - facing.toYRot())
                .renderBoundingBox(pos -> new AABB(pos.offset(-7, 0, -7), pos.offset(8, 6, 8)))
                .build();
    }

    private static List<BlockPos> pumpjackFilledOffsets(Direction facing, BlockPos offsetCore) {
        return Stream.concat(
                offsetsForLegacyXrBox(new int[] { 0, 0, -1, 1, 1, 1 }, facing, offsetCore).stream(),
                offsetsForLegacyXrBox(new int[] { 0, 0, 1, -1, 2, 2 }, facing, offsetCore).stream()).toList();
    }

    private static List<BlockPos> pumpjackCheckOnlyOffsets(Direction facing, BlockPos offsetCore) {
        return Stream.concat(
                offsetsForLegacyXrBox(new int[] { 0, 0, -1, 1, -2, 4 }, facing, BlockPos.ZERO).stream(),
                offsetsForLegacyXrBox(new int[] { 0, 0, 1, -1, -1, 5 }, facing, BlockPos.ZERO).stream()).toList();
    }

    private static List<BlockPos> pumpjackCornerProxyOffsets(BlockPos offsetCore) {
        return List.of(
                offsetCore.offset(1, 0, 1),
                offsetCore.offset(1, 0, -1),
                offsetCore.offset(-1, 0, 1),
                offsetCore.offset(-1, 0, -1));
    }

    private static List<BlockPos> offsetsForLegacyXrBox(int[] dimensions, Direction facing, BlockPos originOffset) {
        int[] rotated = MultiblockExtents.rotateLegacyXr(dimensions, facing);
        return Stream.iterate(originOffset.getX() - rotated[4], x -> x <= originOffset.getX() + rotated[5], x -> x + 1)
                .flatMap(x -> Stream.iterate(originOffset.getY() - rotated[1], y -> y <= originOffset.getY() + rotated[0], y -> y + 1)
                        .flatMap(y -> Stream.iterate(originOffset.getZ() - rotated[2], z -> z <= originOffset.getZ() + rotated[3], z -> z + 1)
                                .map(z -> new BlockPos(x, y, z))))
                .filter(offset -> !offset.equals(originOffset))
                .filter(offset -> !offset.equals(BlockPos.ZERO))
                .toList();
    }

    private static List<BlockPos> cornerProxyOffsets(Direction facing) {
        return List.of(
                new BlockPos(1, 0, 1),
                new BlockPos(1, 0, -1),
                new BlockPos(-1, 0, 1),
                new BlockPos(-1, 0, -1));
    }

    private static ResourceLocation machineModel(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/" + name + ".obj");
    }

    private static ResourceLocation machineTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/machines/" + name + ".png");
    }

    private static RegistryObject<Block> machineBattery(String name) {
        return registerBlockWithItem(name, () -> new MachineBatteryBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static RegistryObject<Block> machineBatterySocket(String name) {
        return registerBlockWithItem(
                name,
                () -> new MachineBatterySocketBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()),
                block -> new MultiblockBlockItem(block.get(), new Item.Properties()));
    }

    private static RegistryObject<Block> wasteEarth(String name, boolean mycelium) {
        return registerBlockWithItem(name, () -> new RadioactiveWasteEarthBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .strength(0.6F)
                .sound(SoundType.GRASS), mycelium));
    }

    private static RegistryObject<Block> falloutLayer(String name) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FalloutLayerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.1F)
                .sound(SoundType.GRAVEL)
                .noOcclusion()
                .noCollission()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)));
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        return block;
    }

    private static RegistryObject<Block> sellafield(String name) {
        return registerBlockWithItem(
                name,
                () -> new LegacySellafieldBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()),
                block -> new LegacyStateBlockItem(block.get(), new Item.Properties(), LegacySellafieldBlock.LEVEL, 6,
                        variant -> Component.translatable(variant == 0
                                ? "block.hbm.sellafield"
                                : "block.hbm.sellafield." + variant)));
    }

    private static RegistryObject<Block> simpleBlock(String name, String textureName) {
        return registerBlockWithItem(name, () -> new Block(simpleResourceProperties(name, textureName)));
    }

    private static RegistryObject<Block> radiationBarrel(String name, float chunkRadiationPerTick) {
        return registerBlockWithItem(name, () -> new LegacyRadiationBarrelBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 2.5F)
                .sound(SoundType.METAL)
                .noOcclusion(),
                chunkRadiationPerTick));
    }

    private static List<RegistryObject<Block>> simpleResourceBlocks(String... specs) {
        return Arrays.stream(specs)
                .filter(spec -> !spec.startsWith("__end__"))
                .map(spec -> {
                    String[] parts = spec.split(":", 2);
                    return simpleResourceBlock(parts[0], parts[1]);
                })
                .toList();
    }

    private static RegistryObject<Block> simpleResourceBlock(String name, String textureName) {
        return switch (name) {
            case "bobblehead" -> trinketResourceBlock(name, textureName, TrinketVariant.Kind.BOBBLEHEAD);
            case "snowglobe" -> trinketResourceBlock(name, textureName, TrinketVariant.Kind.SNOWGLOBE);
            case "plushie" -> trinketResourceBlock(name, textureName, TrinketVariant.Kind.PLUSHIE);
            default -> simpleBlockResourceBlock(name, textureName);
        };
    }

    private static RegistryObject<Block> simpleBlockResourceBlock(String name, String textureName) {
        return registerBlockWithItem(name, () -> switch (name) {
            case "lamp_demon" -> new LegacyDemonLampBlock(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15));
            case "lantern" -> new LegacyLanternBlock(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15));
            case "spotlight_incandescent" -> LegacyComplexShapeBlock.spotlightIncandescent(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15));
            case "spotlight_fluoro" -> LegacyComplexShapeBlock.spotlightFluoro(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15));
            case "spotlight_halogen" -> LegacyComplexShapeBlock.spotlightHalogen(simpleResourceProperties(name, textureName).noOcclusion().lightLevel(state -> 15));
            case "floodlight" -> LegacyComplexShapeBlock.floodlight(simpleResourceProperties(name, textureName).noOcclusion());
            case "rebar" -> LegacyComplexShapeBlock.rebar(simpleResourceProperties(name, textureName).noOcclusion());
            case "wood_barrier" -> LegacyComplexShapeBlock.woodBarrier(simpleResourceProperties(name, textureName).noOcclusion());
            case "sandbags" -> LegacyComplexShapeBlock.sandbags(simpleResourceProperties(name, textureName).noOcclusion());
            case "block_waste", "block_waste_painted", "block_waste_vitrified" -> new LegacyNuclearWasteBlock(name, simpleResourceProperties(name, textureName));
            case "ore_uranium", "ore_uranium_scorched", "ore_gneiss_uranium", "ore_gneiss_uranium_scorched",
                    "ore_nether_uranium", "ore_nether_uranium_scorched" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_RADON::get, true, false);
            case "ancient_scrap" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_RADON_TOMB::get, true, true);
            case "block_corium_cobble" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_RADON::get, true, true);
            case "ore_nether_coal" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName), GAS_MONOXIDE::get, true, false);
            case "ore_asbestos", "ore_gneiss_asbestos", "block_asbestos", "deco_asbestos", "brick_asbestos", "tile_lab_broken" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName).randomTicks(), GAS_ASBESTOS::get, true, false, true);
            case "tile_lab", "tile_lab_cracked" ->
                    new LegacyOutgasBlock(name, simpleResourceProperties(name, textureName), GAS_ASBESTOS::get, true, false);
            default -> new RadiatingHazardBlock(name, simpleResourceProperties(name, textureName));
        });
    }

    private static RegistryObject<Block> trinketResourceBlock(String name, String textureName, TrinketVariant.Kind kind) {
        return registerBlockWithItem(
                name,
                () -> new TrinketBlock(simpleResourceProperties(name, textureName).noOcclusion(), kind),
                block -> new TrinketBlockItem(block.get(), new Item.Properties(), kind));
    }

    private static BlockBehaviour.Properties simpleResourceProperties(String name, String textureName) {
        if (name.contains("sand") || name.contains("gravel") || name.contains("fallout") || name.contains("yellowcake") || textureName.equals("ash")) {
            return BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.6F, 1.0F)
                    .sound(SoundType.GRAVEL)
                    .requiresCorrectToolForDrops();
        }
        if (name.startsWith("block_") || name.startsWith("deco_") || name.startsWith("part_") || name.contains("reinforced")) {
            return BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops();
        }
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops();
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> blockSupplier) {
        return registerBlockWithItem(name, blockSupplier, block -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String name, Supplier<T> blockSupplier, Function<RegistryObject<T>, Item> itemFactory) {
        RegistryObject<T> block = BLOCKS.register(name, blockSupplier);
        BLOCKS_BY_LEGACY_NAME.put(name, block);
        ModItems.ITEMS.register(name, () -> itemFactory.apply(block));
        return block;
    }

    private ModBlocks() {
    }
}

