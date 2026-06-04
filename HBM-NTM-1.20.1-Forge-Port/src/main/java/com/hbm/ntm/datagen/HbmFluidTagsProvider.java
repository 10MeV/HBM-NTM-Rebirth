package com.hbm.ntm.datagen;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModFluids;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HbmFluidTagsProvider extends FluidTagsProvider {
    public HbmFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            String modId, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addForgeTag("water", HbmFluids.WATER);
        addForgeTag("lava", HbmFluids.LAVA);
        addForgeTag("steam", HbmFluids.STEAM, HbmFluids.HOTSTEAM, HbmFluids.SUPERHOTSTEAM, HbmFluids.ULTRAHOTSTEAM);
        addForgeTag("hot_steam", HbmFluids.HOTSTEAM);
        addForgeTag("superheated_steam", HbmFluids.SUPERHOTSTEAM, HbmFluids.ULTRAHOTSTEAM);
        addForgeTag("super_hot_steam", HbmFluids.SUPERHOTSTEAM, HbmFluids.ULTRAHOTSTEAM);
        addForgeTag("crude_oil", HbmFluids.OIL, HbmFluids.CRACKOIL, HbmFluids.COALOIL, HbmFluids.OIL_DS,
                HbmFluids.CRACKOIL_DS);
        addForgeTag("oil", HbmFluids.OIL, HbmFluids.HEAVYOIL, HbmFluids.CRACKOIL, HbmFluids.COALOIL,
                HbmFluids.OIL_DS, HbmFluids.CRACKOIL_DS);
        addForgeTag("heavy_oil", HbmFluids.HEAVYOIL, HbmFluids.HEAVYOIL_VACUUM);
        addForgeTag("diesel", HbmFluids.DIESEL, HbmFluids.DIESEL_CRACK, HbmFluids.DIESEL_REFORM,
                HbmFluids.DIESEL_CRACK_REFORM);
        addForgeTag("kerosene", HbmFluids.KEROSENE, HbmFluids.KEROSENE_REFORM);
        addForgeTag("gasoline", HbmFluids.GASOLINE, HbmFluids.GASOLINE_LEADED);
        addForgeTag("sulfuric_acid", HbmFluids.SULFURIC_ACID);
        addForgeTag("nitric_acid", HbmFluids.NITRIC_ACID);
        addForgeTag("hydrogen", HbmFluids.HYDROGEN);
        addForgeTag("deuterium", HbmFluids.DEUTERIUM);
        addForgeTag("tritium", HbmFluids.TRITIUM);
        addForgeTag("oxygen", HbmFluids.OXYGEN);
        addForgeTag("chlorine", HbmFluids.CHLORINE);
        addForgeTag("carbon_dioxide", HbmFluids.CARBONDIOXIDE);
        addForgeTag("heavy_water", HbmFluids.HEAVYWATER, HbmFluids.HEAVYWATER_HOT);
        addForgeTag("ethanol", HbmFluids.ETHANOL);
        addForgeTag("biofuel", HbmFluids.BIOFUEL);
        addForgeTag("lubricant", HbmFluids.LUBRICANT);
        addForgeTag("mercury", HbmFluids.MERCURY);
        addForgeTag("coolant", HbmFluids.COOLANT, HbmFluids.COOLANT_HOT);
    }

    private void addForgeTag(String path, FluidType... fluids) {
        TagKey<Fluid> tag = forgeFluidTag(path);
        for (FluidType fluid : fluids) {
            addHbmFluid(tag, fluid);
        }
    }

    private void addHbmFluid(TagKey<Fluid> tag, FluidType fluid) {
        ModFluids.HbmFluidRegistryEntry entry = ModFluids.getEntry(fluid);
        if (entry == null) {
            return;
        }
        tag(tag).add(entry.source().get(), entry.flowing().get());
    }

    static TagKey<Fluid> forgeFluidTag(String path) {
        return FluidTags.create(new ResourceLocation("forge", path));
    }
}
