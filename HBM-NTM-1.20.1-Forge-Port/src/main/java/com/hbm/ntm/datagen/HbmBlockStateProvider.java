package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctGaugeBlock;
import com.hbm.ntm.block.FluidDuctPaintableBlock;
import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.block.FluidValveBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacySellafieldOreBlock;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.block.RedCableGaugeBlock;
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

public class HbmBlockStateProvider extends BlockStateProvider {
    public HbmBlockStateProvider(net.minecraft.data.PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        existingModelWithItem(ModBlocks.MACHINE_PRESS, "machine_press");
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
        sidedCubeWithItem(ModBlocks.MACHINE_SHREDDER,
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
        simpleSidedCubeWithItem(ModBlocks.DECON,
                "decon_side",
                "decon_top",
                "decon_side",
                "decon_side",
                "decon_side",
                "decon_side");
        redCableWithItem();
        redCableGaugeWithItem();
        fluidPipeWithItem();
        fluidDuctBoxWithItem(ModBlocks.FLUID_DUCT_BOX, "boxduct_silver");
        fluidDuctGaugeWithItem();
        fluidDuctBoxWithItem(ModBlocks.FLUID_DUCT_EXHAUST, "boxduct_exhaust");
        fluidDuctPaintableWithItem(ModBlocks.FLUID_DUCT_PAINTABLE, "fluid_duct_paintable");
        fluidDuctPaintableWithItem(ModBlocks.FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST,
                "fluid_duct_paintable_block_exhaust");
        fluidPipeAnchorWithItem();
        fluidBarrelWithItem(ModBlocks.BARREL_PLASTIC, "barrel_plastic");
        fluidBarrelWithItem(ModBlocks.BARREL_CORRODED, "barrel_corroded");
        fluidBarrelWithItem(ModBlocks.BARREL_IRON, "barrel_iron");
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
        existingModelWithCustomItem(ModBlocks.MACHINE_BATTERY_SOCKET, "machines/battery_socket_socket");
        existingModelBlockOnly(ModBlocks.MACHINE_ASSEMBLY_MACHINE, "machine_assembly_machine");
        customBlockItem(ModBlocks.MACHINE_ASSEMBLY_MACHINE);
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
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_BIGASSTANK, "machines/bigasstank");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FLUIDTANK, "machines/fluidtank");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_WELL, "machines/derrick");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PUMPJACK, "machines/pumpjack");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_FRACKING_TOWER, "machines/fracking_tower");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CENTRIFUGE, "machines/centrifuge");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_GASCENT, "machines/gascent");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ORE_SLOPPER, "machines/ore_slopper");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SAWMILL, "machines/sawmill");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CRUCIBLE, "machines/crucible");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_GASFLARE, "machines/flare_stack");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ASSEMBLY_FACTORY, "machines/assembly_factory");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PUREX, "machines/purex");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SILEX, "machines/silex");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_EXPOSURE_CHAMBER, "machines/exposure_chamber");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CYCLOTRON, "machines/cyclotron");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ARC_WELDER, "machines/arc_welder");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SOLDERING_STATION, "machines/soldering_station");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_MIXER, "machines/mixer");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RADIOLYSIS, "machines/radiolysis");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_RADGEN, "machines/radgen");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ROTARY_FURNACE, "machines/rotary_furnace");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_STEAM_ENGINE, "machines/steam_engine");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_SOLAR_BOILER, "machines/solar_boiler");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TOWER_SMALL, "machines/tower_small");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TOWER_LARGE, "machines/tower_large");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TURBOFAN, "machines/turbofan");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_TURBINEGAS, "machines/turbinegas");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_INDUSTRIAL_TURBINE, "machines/industrial_turbine");
        translucentCubeWithItem(ModBlocks.GLASS_BORON, "glass_boron");
        simpleCubeWithItem(ModBlocks.GAS_RADON, "gas_radon");
        simpleCubeWithItem(ModBlocks.GAS_RADON_DENSE, "gas_radon_dense");
        simpleCubeWithItem(ModBlocks.GAS_RADON_TOMB, "gas_radon_tomb");
        simpleCubeWithItem(ModBlocks.GAS_MELTDOWN, "gas_meltdown");
        simpleCubeWithItem(ModBlocks.GAS_MONOXIDE, "gas_monoxide");
        simpleCubeWithItem(ModBlocks.GAS_ASBESTOS, "gas_asbestos");
        simpleCubeWithItem(ModBlocks.GAS_COAL, "gas_coal");
        simpleCubeWithItem(ModBlocks.CHLORINE_GAS, "chlorine_gas");
        radAbsorberWithItem();
        simpleCubeWithItem(ModBlocks.DUMMY_BLOCK, "block_steel");
        wasteLogWithItem();
        simpleCubeWithItem(ModBlocks.WASTE_PLANKS, "waste_planks");
        leavesLayerWithItem();
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
        simpleCubeWithItem(ModBlocks.PRIBRIS_DIGAMMA, "rbmk_debris_digamma");
        simpleCubeWithItem(ModBlocks.VOLCANIC_LAVA_BLOCK, "volcanic_lava_still");
        simpleCubeWithItem(ModBlocks.RAD_LAVA_BLOCK, "rad_lava_still");
        translucentCubeBlockOnly(ModBlocks.MUD_BLOCK, "mud_still");
        frozenGrassWithItem();
        simpleCubeWithItem(ModBlocks.FROZEN_DIRT, "frozen_dirt");
        frozenLogWithItem();
        simpleCubeWithItem(ModBlocks.FROZEN_PLANKS, "frozen_planks");
        simpleCubeWithItem(ModBlocks.TEKTITE, "tektite");
        simpleCubeWithItem(ModBlocks.ORE_TEKTITE_OSMIRIDIUM, "ore_tektite_osmiridium");
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
        simpleCubeWithItem(ModBlocks.YELLOW_BARREL, "barrel_yellow");
        simpleCubeWithItem(ModBlocks.VITRIFIED_BARREL, "barrel_vitrified");
    }

    private void existingModelWithItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        horizontalBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void existingModelWithCustomItem(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        horizontalBlock(block.get(), model);
        customBlockItem(block);
    }

    private void visibleMachineWithItemRenderer(RegistryObject<Block> block, String modelName) {
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/" + modelName));
        horizontalBlock(block.get(), model);
        customBlockItem(block);
    }

    private void customBlockItem(RegistryObject<Block> block) {
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("builtin/entity")));
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

    private void simpleCubeWithItem(String legacyName, String textureName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block hbm:" + legacyName);
        }
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName));
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void crossBlockOnly(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cross(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:cutout");
        simpleBlock(block.get(), model);
    }

    private void translucentCubeWithItem(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:translucent");
        simpleBlock(block.get(), model);
        simpleBlockItem(block.get(), model);
    }

    private void translucentCubeBlockOnly(RegistryObject<Block> block, String textureName) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + textureName))
                .renderType("minecraft:translucent");
        simpleBlock(block.get(), model);
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

    private void fluidPipeWithItem() {
        ModelFile marker = models().getBuilder(ModBlocks.FLUID_DUCT_NEO.getId().getPath())
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/legacy_blocks/pipe_neo"));
        simpleBlock(ModBlocks.FLUID_DUCT_NEO.get(), marker);
        itemModels().getBuilder(ModBlocks.FLUID_DUCT_NEO.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
    }

    private void fluidValveWithItem(RegistryObject<Block> block, String offTexture, String onTexture) {
        ModelFile off = models().cubeAll(block.getId().getPath() + "_off",
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + offTexture));
        ModelFile on = models().cubeAll(block.getId().getPath() + "_on",
                new ResourceLocation(HbmNtm.MOD_ID, "block/" + onTexture));
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(FluidValveBlock.OPEN) ? on : off)
                        .build());
        itemModels().getBuilder(block.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
    }

    private void fluidDuctBoxWithItem(RegistryObject<Block> block, String texturePrefix) {
        String blockName = block.getId().getPath();
        ModelFile model = models().cubeAll(blockName, new ResourceLocation(HbmNtm.MOD_ID, "block/" + texturePrefix));
        simpleBlock(block.get(), model);
        if (block == ModBlocks.FLUID_DUCT_PAINTABLE_BLOCK_EXHAUST) {
            itemModels().getBuilder(blockName)
                    .parent(new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID,
                            "block/fluid_duct_paintable_block_exhaust_overlay")));
        } else {
            itemModels().getBuilder(blockName)
                    .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                    .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                    .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
        }
    }

    private void fluidDuctGaugeWithItem() {
        ModelFile[] models = new ModelFile[Direction.values().length];
        for (Direction direction : Direction.values()) {
            models[direction.ordinal()] = fluidDuctGaugeModel("fluid_duct_gauge_" + direction.getName(), direction);
        }
        getVariantBuilder(ModBlocks.FLUID_DUCT_GAUGE.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(models[state.getValue(FluidDuctGaugeBlock.FACING).ordinal()])
                        .build());
        itemModels().getBuilder(ModBlocks.FLUID_DUCT_GAUGE.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
    }

    private void fluidDuctPaintableWithItem(RegistryObject<Block> block, String baseTexture) {
        String blockName = block.getId().getPath();
        ModelFile overlay = fluidDuctPaintableModel(blockName + "_overlay", baseTexture,
                "fluid_duct_paintable_overlay", true);
        ModelFile clean = fluidDuctPaintableModel(blockName, baseTexture, "fluid_duct_paintable_color", false);
        getVariantBuilder(block.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.hasProperty(FluidDuctPaintableBlock.OVERLAY)
                                && state.getValue(FluidDuctPaintableBlock.OVERLAY) ? overlay : clean)
                        .build());
        itemModels().getBuilder(blockName)
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
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
                        .rotationY(((int) state.getValue(HorizontalMachineBlock.FACING).toYRot() + 180) % 360)
                        .build());
        itemModels().getBuilder(ModBlocks.FLUID_PUMP.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/network/fluid_diode")));
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
