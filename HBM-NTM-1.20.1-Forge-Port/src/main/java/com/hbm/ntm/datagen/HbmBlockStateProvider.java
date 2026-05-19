package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
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
        sidedCubeWithItem(ModBlocks.DECON,
                "decon_side",
                "decon_top",
                "decon_side",
                "decon_side",
                "decon_side",
                "decon_side");
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
}
