package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;

public final class HbmDataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new HbmLanguageProvider(output, HbmNtm.MOD_ID, "en_us"));
        generator.addProvider(event.includeClient(), new HbmZhCnLanguageProvider(output, HbmNtm.MOD_ID, "zh_cn"));
        generator.addProvider(event.includeClient(), new HbmItemModelProvider(output, HbmNtm.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), new HbmBlockStateProvider(output, HbmNtm.MOD_ID, existingFileHelper));
        HbmBlockTagsProvider blockTagsProvider = new HbmBlockTagsProvider(output, event.getLookupProvider(), HbmNtm.MOD_ID, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new HbmItemTagsProvider(output, event.getLookupProvider(), blockTagsProvider.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new HbmFluidTagsProvider(output, event.getLookupProvider(), HbmNtm.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeServer(), new HbmRecipeProvider(output));
        generator.addProvider(event.includeServer(), new LegacyGenericRecipeImportProvider(output, projectRoot()));
        generator.addProvider(event.includeServer(), new LegacyJavaRecipeCoverageProvider(output, projectRoot()));
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) lootOutput -> new LootTableProvider(
                lootOutput,
                Collections.emptySet(),
                List.of(
                        new LootTableProvider.SubProviderEntry(HbmBlockLootProvider::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(HbmItemPoolLootProvider::new, LootContextParamSets.CHEST)
                )
        ));
    }

    private static java.nio.file.Path projectRoot() {
        java.nio.file.Path current = java.nio.file.Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        return "run-data".equals(current.getFileName().toString()) && current.getParent() != null
                ? current.getParent()
                : current;
    }

    private HbmDataGenerators() {
    }
}
