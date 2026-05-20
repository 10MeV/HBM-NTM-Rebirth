package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.CompletableFuture;

public class HbmItemTagsProvider extends ItemTagsProvider {
    public HbmItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, HbmNtm.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(HbmBlockTagsProvider.forgeBlockTag("ores"), forgeItemTag("ores"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/uranium"), forgeItemTag("ores/uranium"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/thorium"), forgeItemTag("ores/thorium"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/schrabidium"), forgeItemTag("ores/schrabidium"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/lignite"), forgeItemTag("ores/lignite"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/asbestos"), forgeItemTag("ores/asbestos"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/coal"), forgeItemTag("ores/coal"));

        addLegacyForgeTag("dusts/lignite", "powder_lignite");
        addLegacyForgeTag("dusts", "powder_lignite");
        addLegacyForgeTag("gems/coal", net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.CHARCOAL);
        addLegacyForgeTag("gems/lignite", "lignite", "coal_infernal");
    }

    private void addLegacyForgeTag(String path, String... itemNames) {
        TagKey<Item> tag = forgeItemTag(path);
        for (String itemName : itemNames) {
            RegistryObject<Item> item = ModItems.legacyItem(itemName);
            if (item != null) {
                tag(tag).add(item.get());
            }
        }
    }

    private void addLegacyForgeTag(String path, Item... items) {
        tag(forgeItemTag(path)).add(items);
    }

    static TagKey<Item> forgeItemTag(String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }
}
