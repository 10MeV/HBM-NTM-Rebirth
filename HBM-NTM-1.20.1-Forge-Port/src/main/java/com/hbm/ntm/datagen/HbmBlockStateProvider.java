package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
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
        simpleSidedCubeWithItem(ModBlocks.DECON,
                "decon_side",
                "decon_top",
                "decon_side",
                "decon_side",
                "decon_side",
                "decon_side");
        redCableWithItem();
        fluidPipeWithItem();
        sidedCubeWithItem(ModBlocks.MACHINE_BATTERY,
                "battery_top",
                "battery_top",
                "battery_side_alt",
                "battery_front_alt",
                "battery_side_alt",
                "battery_side_alt");
        simpleCubeWithItem(ModBlocks.GAS_MELTDOWN, "gas_meltdown");
        radAbsorberWithItem();
        sellafieldWithItem();
        simpleCubeWithItem(ModBlocks.SELLAFIELD_SLAKED, "sellafield_slaked");
        existingModelWithItem(ModBlocks.NUKE_GADGET, "nuke_gadget");
        existingModelWithItem(ModBlocks.NUKE_BOY, "nuke_boy");
        existingModelWithItem(ModBlocks.NUKE_MAN, "nuke_man");
        existingModelWithItem(ModBlocks.NUKE_TSAR, "nuke_tsar");
        existingModelWithItem(ModBlocks.NUKE_MIKE, "nuke_mike");
        existingModelWithItem(ModBlocks.NUKE_PROTOTYPE, "nuke_prototype");
        existingModelWithItem(ModBlocks.NUKE_FLEIJA, "nuke_fleija");
        existingModelWithItem(ModBlocks.NUKE_SOLINIUM, "nuke_solinium");
        existingModelWithItem(ModBlocks.NUKE_N2, "nuke_n2");
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
        ModelFile core = models().cubeAll("fluid_duct_neo_core", new ResourceLocation(HbmNtm.MOD_ID, "block/pipe_neo"));
        ModelFile side = models().cubeAll("fluid_duct_neo_side", new ResourceLocation(HbmNtm.MOD_ID, "block/pipe_neo"));
        getMultipartBuilder(ModBlocks.FLUID_DUCT_NEO.get())
                .part().modelFile(core).addModel().end()
                .part().modelFile(side).addModel().condition(HbmFluidNodeBlock.NORTH, true).end()
                .part().modelFile(side).rotationY(90).addModel().condition(HbmFluidNodeBlock.EAST, true).end()
                .part().modelFile(side).rotationY(180).addModel().condition(HbmFluidNodeBlock.SOUTH, true).end()
                .part().modelFile(side).rotationY(270).addModel().condition(HbmFluidNodeBlock.WEST, true).end()
                .part().modelFile(side).rotationX(-90).addModel().condition(HbmFluidNodeBlock.UP, true).end()
                .part().modelFile(side).rotationX(90).addModel().condition(HbmFluidNodeBlock.DOWN, true).end();
        simpleBlockItem(ModBlocks.FLUID_DUCT_NEO.get(), core);
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
}
