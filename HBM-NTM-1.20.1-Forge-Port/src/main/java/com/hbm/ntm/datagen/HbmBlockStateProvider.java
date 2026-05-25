package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacySellafieldOreBlock;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
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
        fluidPipeWithItem();
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
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_PUMPJACK, "machines/pumpjack");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_CENTRIFUGE, "machines/centrifuge");
        visibleMachineWithItemRenderer(ModBlocks.MACHINE_ORE_SLOPPER, "machines/ore_slopper");
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

    private void wasteLogWithItem() {
        ResourceLocation side = new ResourceLocation(HbmNtm.MOD_ID, "block/waste_log_side");
        ResourceLocation top = new ResourceLocation(HbmNtm.MOD_ID, "block/waste_log_top");
        axisBlock((net.minecraft.world.level.block.RotatedPillarBlock) ModBlocks.WASTE_LOG.get(), side, top);
        ModelFile model = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/waste_log"));
        simpleBlockItem(ModBlocks.WASTE_LOG.get(), model);
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
        ModelFile core = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/red_cable_core"));
        ModelFile side = new ModelFile.UncheckedModelFile(new ResourceLocation(HbmNtm.MOD_ID, "block/red_cable_side"));
        getMultipartBuilder(ModBlocks.RED_CABLE.get())
                .part().modelFile(core).addModel().end()
                .part().modelFile(side).addModel().condition(HbmEnergyNodeBlock.NORTH, true).end()
                .part().modelFile(side).rotationY(90).addModel().condition(HbmEnergyNodeBlock.EAST, true).end()
                .part().modelFile(side).rotationY(180).addModel().condition(HbmEnergyNodeBlock.SOUTH, true).end()
                .part().modelFile(side).rotationY(270).addModel().condition(HbmEnergyNodeBlock.WEST, true).end()
                .part().modelFile(side).rotationX(-90).addModel().condition(HbmEnergyNodeBlock.UP, true).end()
                .part().modelFile(side).rotationX(90).addModel().condition(HbmEnergyNodeBlock.DOWN, true).end();
        simpleBlockItem(ModBlocks.RED_CABLE.get(), core);
    }

    private void fluidPipeWithItem() {
        ModelFile core = fluidPipeModel("fluid_duct_neo_core", 5.0F, 5.0F, 5.0F, 11.0F, 11.0F, 11.0F);
        ModelFile side = fluidPipeModel("fluid_duct_neo_side", 5.0F, 5.0F, 0.0F, 11.0F, 11.0F, 5.0F);
        getMultipartBuilder(ModBlocks.FLUID_DUCT_NEO.get())
                .part().modelFile(core).addModel().end()
                .part().modelFile(side).addModel().condition(HbmFluidNodeBlock.NORTH, true).end()
                .part().modelFile(side).rotationY(90).addModel().condition(HbmFluidNodeBlock.EAST, true).end()
                .part().modelFile(side).rotationY(180).addModel().condition(HbmFluidNodeBlock.SOUTH, true).end()
                .part().modelFile(side).rotationY(270).addModel().condition(HbmFluidNodeBlock.WEST, true).end()
                .part().modelFile(side).rotationX(-90).addModel().condition(HbmFluidNodeBlock.UP, true).end()
                .part().modelFile(side).rotationX(90).addModel().condition(HbmFluidNodeBlock.DOWN, true).end();
        itemModels().getBuilder(ModBlocks.FLUID_DUCT_NEO.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("minecraft", "item/generated")))
                .texture("layer0", new ResourceLocation(HbmNtm.MOD_ID, "item/duct"))
                .texture("layer1", new ResourceLocation(HbmNtm.MOD_ID, "item/duct_overlay"));
    }

    private ModelFile fluidPipeModel(String name, float x1, float y1, float z1, float x2, float y2, float z2) {
        return models().withExistingParent(name, new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/pipe_neo"))
                .texture("pipe", new ResourceLocation(HbmNtm.MOD_ID, "block/pipe_neo"))
                .texture("overlay", new ResourceLocation(HbmNtm.MOD_ID, "block/pipe_neo_overlay"))
                .renderType("minecraft:cutout")
                .element()
                    .from(x1, y1, z1)
                    .to(x2, y2, z2)
                    .allFaces((direction, face) -> face.texture("#pipe"))
                    .end()
                .element()
                    .from(x1, y1, z1)
                    .to(x2, y2, z2)
                    .allFaces((direction, face) -> face.texture("#overlay").tintindex(1))
                    .end();
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
        ModelFile model = sellafieldSlakedModel(modelName);
        for (int level = 0; level <= 15; level++) {
            getVariantBuilder(block.get())
                    .partialState().with(LegacySellafieldSlakedBlock.LEVEL, level).modelForState()
                    .modelFile(model).addModel();
        }
        simpleBlockItem(block.get(), model);
    }

    private void sellafieldOreWithItem(RegistryObject<Block> block, LegacySellafieldOreBlock.Kind kind) {
        String name = block.getId().getPath();
        ModelFile model = models().withExistingParent(name, new ResourceLocation("block/block"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked"))
                .texture("base", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked"))
                .texture("base1", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_1"))
                .texture("base2", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_2"))
                .texture("base3", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_3"))
                .texture("overlay", new ResourceLocation(HbmNtm.MOD_ID, "block/ore_overlay_" + kind.overlayTexture()))
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F)
                    .allFaces((direction, face) -> face.texture("#base"))
                    .end()
                .element()
                    .from(0.0F, 0.0F, 0.0F)
                    .to(16.0F, 16.0F, 16.0F)
                    .allFaces((direction, face) -> face.texture("#overlay").cullface(direction))
                    .end();
        for (int level = 0; level <= 15; level++) {
            getVariantBuilder(block.get())
                    .partialState().with(LegacySellafieldSlakedBlock.LEVEL, level).modelForState()
                    .modelFile(model).addModel();
        }
        simpleBlockItem(block.get(), model);
    }

    private ModelFile sellafieldSlakedModel(String modelName) {
        return models().withExistingParent(modelName, new ResourceLocation("block/cube"))
                .texture("particle", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked"))
                .texture("down", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked"))
                .texture("up", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_1"))
                .texture("north", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_2"))
                .texture("south", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_3"))
                .texture("east", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_1"))
                .texture("west", new ResourceLocation(HbmNtm.MOD_ID, "block/sellafield_slaked_2"));
    }
}
