package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class HbmItemTagsProvider extends ItemTagsProvider {
    private final Map<TagKey<Item>, Set<ResourceLocation>> addedLegacyTagItems = new HashMap<>();

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
        copy(HbmBlockTagsProvider.forgeBlockTag("glass"), forgeItemTag("glass"));

        addLegacyForgeTag("dusts/spark_mix", "powder_spark_mix");
        addLegacyForgeTag("dusts", "powder_spark_mix");
        addLegacyForgeTag("ingots/tantalium", "ingot_tantalium");
        addLegacyForgeTag("ingots/pc", "ingot_pc");
        addLegacyForgeTag("ingots/any_plastic", "ingot_polymer", "ingot_bakelite");
        addLegacyForgeTag("ingots/plastic", "ingot_polymer", "ingot_bakelite");
        addLegacyForgeTag("ingots/any_hardplastic", "ingot_pc", "ingot_pvc");
        addLegacyForgeTag("ingots/hard_plastic", "ingot_pc", "ingot_pvc");
        addLegacyMaterialTags();
        addLegacyForgeTag("circuits/vacuum_tube", "circuit_vacuum_tube");
        addLegacyForgeTag("circuits/capacitor", "circuit_capacitor");
        addLegacyForgeTag("circuits/capacitor_tantalium", "circuit_capacitor_tantalium");
        addLegacyForgeTag("circuits/pcb", "circuit_pcb");
        addLegacyForgeTag("circuits/silicon", "circuit_silicon");
        addLegacyForgeTag("circuits/chip", "circuit_chip");
        addLegacyForgeTag("circuits/chip_bismoid", "circuit_chip_bismoid");
        addLegacyForgeTag("circuits/analog", "circuit_analog");
        addLegacyForgeTag("circuits/basic", "circuit_basic");
        addLegacyForgeTag("circuits/advanced", "circuit_advanced");
        addLegacyForgeTag("circuits/capacitor_board", "circuit_capacitor_board");
        addLegacyForgeTag("circuits/bismoid", "circuit_bismoid");
        addLegacyForgeTag("circuits/controller_chassis", "circuit_controller_chassis");
        addLegacyForgeTag("circuits/controller", "circuit_controller");
        addLegacyForgeTag("circuits/controller_advanced", "circuit_controller_advanced");
        addLegacyForgeTag("circuits/quantum", "circuit_quantum");
        addLegacyForgeTag("circuits/chip_quantum", "circuit_chip_quantum");
        addLegacyForgeTag("circuits/controller_quantum", "circuit_controller_quantum");
        addLegacyForgeTag("circuits/atomic_clock", "circuit_atomic_clock");
        addLegacyForgeTag("circuits/numitron", "circuit_numitron");
        tag(forgeItemTag("circuits")).add(ModItems.CIRCUIT_ITEMS.stream()
                .map(RegistryObject::get)
                .toArray(Item[]::new));
        addLegacyForgeTag("gems/coal", net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.CHARCOAL);
        addLegacyForgeTag("gems/lignite", "lignite", "coal_infernal");
        copy(HbmBlockTagsProvider.forgeBlockTag("storage_blocks/lead"), forgeItemTag("storage_blocks/lead"));

        addLegacyTarTags();
        addLegacyDyeTags();
        addLegacyCokeBriquetteAshTags();
        addLegacyEnumMultiBridgeTags();
        addLegacyFluidContainerTags();
    }

    private void addLegacyTarTags() {
        addLegacyForgeTag("tar", "oil_tar_crude", "oil_tar_crack", "oil_tar_coal", "oil_tar_wood", "oil_tar_wax", "oil_tar_paraffin");
        addLegacyForgeTag("tar/oil", "oil_tar_crude");
        addLegacyForgeTag("tar/crack", "oil_tar_crack");
        addLegacyForgeTag("tar/coal", "oil_tar_coal");
        addLegacyForgeTag("tar/wood", "oil_tar_wood");

        addLegacyForgeTag("dyes", "oil_tar_crude", "oil_tar_crack", "oil_tar_coal", "oil_tar_wood", "oil_tar_wax", "oil_tar_paraffin");
        addLegacyForgeTag("dyes/black", "oil_tar_crude", "oil_tar_crack");
        addLegacyForgeTag("dyes/gray", "oil_tar_coal");
        addLegacyForgeTag("dyes/brown", "oil_tar_wood");
        addLegacyForgeTag("dyes/cyan", "oil_tar_wax");
        addLegacyForgeTag("dyes/white", "oil_tar_paraffin");
    }

    private void addLegacyDyeTags() {
        addLegacyForgeTag("dyes", "cinnebar", "sulfur", "powder_coal", "powder_lignite", "powder_titanium",
                "fluorite", "powder_cadmium");
        addLegacyForgeTag("dyes/red", "cinnebar");
        addLegacyForgeTag("dyes/yellow", "sulfur");
        addLegacyForgeTag("dyes/black", "powder_coal");
        addLegacyForgeTag("dyes/brown", "powder_lignite");
        addLegacyForgeTag("dyes/light_gray", "powder_titanium");
        addLegacyForgeTag("dyes/white", "fluorite");
        addLegacyForgeTag("dyes/orange", "powder_cadmium");
    }

    private void addLegacyCokeBriquetteAshTags() {
        addLegacyForgeTag("gems", "coke_coal", "coke_lignite", "coke_petroleum");
        addLegacyForgeTag("gems/coke", "coke_coal", "coke_lignite", "coke_petroleum");
        addLegacyForgeTag("gems/coal_coke", "coke_coal");
        addLegacyForgeTag("gems/lignite_coke", "coke_lignite");
        addLegacyForgeTag("gems/pet_coke", "coke_petroleum");

        addLegacyForgeTag("briquettes", "briquette_coal", "briquette_lignite", "briquette_wood");
        addLegacyForgeTag("briquettes/coal", "briquette_coal");
        addLegacyForgeTag("briquettes/lignite", "briquette_lignite");
        addLegacyForgeTag("briquettes/wood", "briquette_wood");

        addLegacyForgeTag("any/ash", "powder_ash_wood", "powder_ash_coal", "powder_ash_misc", "powder_ash_fly", "powder_ash_soot");
        addLegacyForgeTag("dyes", "powder_ash_wood", "powder_ash_coal", "powder_ash_misc", "powder_ash_fly", "powder_ash_soot", "powder_ash_fullerene");
        addLegacyForgeTag("dyes/light_gray", "powder_ash_wood");
        addLegacyForgeTag("dyes/black", "powder_ash_coal", "powder_ash_soot");
        addLegacyForgeTag("dyes/gray", "powder_ash_misc");
        addLegacyForgeTag("dyes/brown", "powder_ash_fly");
        addLegacyForgeTag("dyes/magenta", "powder_ash_fullerene");
    }

    private void addLegacyEnumMultiBridgeTags() {
        addLegacyForgeTag("ingots/rare_earth", "chunk_ore_rare");
        addLegacyForgeTag("ingots/malachite", "chunk_ore_malachite");
        addLegacyForgeTag("crystals/cryolite", "chunk_ore_cryolite");
        addLegacyForgeTag("ingots", "chunk_ore_rare", "chunk_ore_malachite");
        addLegacyForgeTag("crystals", "chunk_ore_cryolite");

    }

    private void addLegacyMaterialTags() {
        addLegacyMaterialAliases("ingot", "ingots", "ingot_uranium", "Uranium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_u233", "Uranium233", "U233");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_u235", "Uranium235", "U235");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_u238", "Uranium238", "U238");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_th232", "Thorium232", "Th232", "Thorium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_plutonium", "Plutonium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pu238", "Plutonium238", "Pu238");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pu239", "Plutonium239", "Pu239");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pu240", "Plutonium240", "Pu240");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pu241", "Plutonium241", "Pu241");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pu_mix", "PlutoniumRG");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_am241", "Americium241", "Am241");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_am242", "Americium242", "Am242");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_am_mix", "AmericiumRG");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_neptunium", "Neptunium237", "Np237", "Neptunium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_polonium", "Polonium210", "Po210", "Polonium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_technetium", "Technetium99", "Tc99");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_ra226", "Radium226", "Ra226");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_co60", "Cobalt60", "Co60");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_au198", "Gold198", "Au198");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pb209", "Lead209", "Pb209");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_schrabidium", "Schrabidium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_solinium", "Solinium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_mud", "WatzMud");

        addLegacyMaterialAliases("ingot", "ingots", "ingot_titanium", "Titanium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_copper", "Copper");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_red_copper", "Mingrade");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_advanced_alloy", "AdvancedAlloy");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_tungsten", "Tungsten");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_aluminium", "Aluminum");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_steel", "Steel");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_tcalloy", "TcAlloy");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_cdalloy", "CdAlloy");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_bismuth_bronze", "BismuthBronze");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_arsenic_bronze", "ArsenicBronze");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_bscco", "BSCCO");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_lead", "Lead");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_bismuth", "Bismuth");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_calcium", "Calcium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_cadmium", "Cadmium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_tantalium", "Tantalum");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_niobium", "Niobium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_beryllium", "Beryllium");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_cobalt", "Cobalt");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_boron", "Boron");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_graphite", "Graphite", "Carbon");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_polymer", "Polymer");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_bakelite", "Bakelite");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pc", "Polycarbonate");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_pvc", "PVC");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_magnetized_tungsten", "MagnetizedTungsten");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_gunmetal", "GunMetal");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_weaponsteel", "WeaponSteel");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_dura_steel", "DuraSteel");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_combine_steel", "CMBSteel");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_saturnite", "Saturnite");
        addLegacyMaterialAliases("ingot", "ingots", "ingot_cft", "CFT");

        addLegacyMaterialAliases("dust", "dusts", "powder_uranium", "Uranium");
        addLegacyMaterialAliases("dust", "dusts", "powder_plutonium", "Plutonium");
        addLegacyMaterialAliases("dust", "dusts", "powder_thorium", "Thorium232", "Th232", "Thorium");
        addLegacyMaterialAliases("dust", "dusts", "powder_titanium", "Titanium");
        addLegacyMaterialAliases("dust", "dusts", "powder_tungsten", "Tungsten");
        addLegacyMaterialAliases("dust", "dusts", "powder_copper", "Copper");
        addLegacyMaterialAliases("dust", "dusts", "powder_iron", "Iron");
        addLegacyMaterialAliases("dust", "dusts", "powder_steel", "Steel");
        addLegacyMaterialAliases("dust", "dusts", "powder_lead", "Lead");
        addLegacyMaterialAliases("dust", "dusts", "powder_lithium", "Lithium");
        addLegacyMaterialAliases("dust", "dusts", "powder_cobalt", "Cobalt");
        addLegacyMaterialAliases("dust", "dusts", "powder_sodium", "Sodium");
        addLegacyMaterialAliases("dust", "dusts", "powder_schrabidium", "Schrabidium");
        addLegacyMaterialAliases("dust", "dusts", "powder_dura_steel", "DuraSteel");
        addLegacyMaterialAliases("dust", "dusts", "powder_combine_steel", "CMBSteel");
        addLegacyMaterialAliases("dust", "dusts", "powder_calcium", "Calcium");
        addLegacyMaterialAliases("dust", "dusts", "powder_cadmium", "Cadmium");
        addLegacyMaterialAliases("dust", "dusts", "powder_bismuth", "Bismuth");
        addLegacyMaterialAliases("dust", "dusts", "powder_caesium", "Caesium");
        addLegacyMaterialAliases("dust", "dusts", "powder_coltan_ore", "Coltan");
        addLegacyMaterialAliases("dust", "dusts", "powder_coal", "Coal");
        addLegacyMaterialAliases("dustTiny", "tiny_dusts", "powder_coal_tiny", "Coal");
        addLegacyMaterialAliases("dust", "dusts", "powder_lignite", "Lignite");
        addLegacyMaterialAliases("dust", "dusts", "powder_quartz", "NetherQuartz", "Quartz");
        addLegacyMaterialAliases("dust", "dusts", "powder_lapis", "Lapis");
        addLegacyMaterialAliases("dust", "dusts", "powder_diamond", "Diamond");
        addLegacyMaterialAliases("dust", "dusts", "powder_emerald", "Emerald");
        addLegacyMaterialAliases("dust", "dusts", "powder_limestone", "Limestone");

        addLegacyMaterialAliases("plate", "plates", "plate_iron", "Iron");
        addLegacyMaterialAliases("plate", "plates", "plate_gold", "Gold");
        addLegacyMaterialAliases("plate", "plates", "plate_steel", "Steel");
        addLegacyMaterialAliases("plate", "plates", "plate_copper", "Copper");
        addLegacyMaterialAliases("plate", "plates", "plate_lead", "Lead");
        addLegacyMaterialAliases("plate", "plates", "plate_titanium", "Titanium");
        addLegacyMaterialAliases("plate", "plates", "plate_aluminium", "Aluminum");
        addLegacyMaterialAliases("plate", "plates", "plate_advanced_alloy", "AdvancedAlloy");
        addLegacyMaterialAliases("plate", "plates", "plate_dura_steel", "DuraSteel");
        addLegacyMaterialAliases("plate", "plates", "plate_gunmetal", "GunMetal");
        addLegacyMaterialAliases("plate", "plates", "plate_weaponsteel", "WeaponSteel");
        addLegacyMaterialAliases("plate", "plates", "plate_schrabidium", "Schrabidium");
        addLegacyMaterialAliases("plate", "plates", "plate_combine_steel", "CMBSteel");
        addLegacyMaterialAliases("plate", "plates", "plate_saturnite", "Saturnite");

        addLegacyMaterialAliases("nugget", "nuggets", "nugget_uranium", "Uranium");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_u233", "Uranium233", "U233");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_u235", "Uranium235", "U235");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_u238", "Uranium238", "U238");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_th232", "Thorium232", "Th232", "Thorium");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_plutonium", "Plutonium");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_pu238", "Plutonium238", "Pu238");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_pu239", "Plutonium239", "Pu239");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_pu240", "Plutonium240", "Pu240");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_pu241", "Plutonium241", "Pu241");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_pu_mix", "PlutoniumRG");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_am241", "Americium241", "Am241");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_am242", "Americium242", "Am242");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_am_mix", "AmericiumRG");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_neptunium", "Neptunium237", "Np237", "Neptunium");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_polonium", "Polonium210", "Po210", "Polonium");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_technetium", "Technetium99", "Tc99");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_cobalt", "Cobalt");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_co60", "Cobalt60", "Co60");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_sr90", "Strontium90", "Sr90");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_au198", "Gold198", "Au198");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_pb209", "Lead209", "Pb209");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_ra226", "Radium226", "Ra226");
        addLegacyMaterialAliases("nugget", "nuggets", "nugget_solinium", "Solinium");

        addLegacyMaterialAliases("billet", "billets", "billet_uranium", "Uranium");
        addLegacyMaterialAliases("billet", "billets", "billet_u233", "Uranium233", "U233");
        addLegacyMaterialAliases("billet", "billets", "billet_u235", "Uranium235", "U235");
        addLegacyMaterialAliases("billet", "billets", "billet_u238", "Uranium238", "U238");
        addLegacyMaterialAliases("billet", "billets", "billet_th232", "Thorium232", "Th232", "Thorium");
        addLegacyMaterialAliases("billet", "billets", "billet_plutonium", "Plutonium");
        addLegacyMaterialAliases("billet", "billets", "billet_pu238", "Plutonium238", "Pu238");
        addLegacyMaterialAliases("billet", "billets", "billet_pu239", "Plutonium239", "Pu239");
        addLegacyMaterialAliases("billet", "billets", "billet_pu240", "Plutonium240", "Pu240");
        addLegacyMaterialAliases("billet", "billets", "billet_pu241", "Plutonium241", "Pu241");
        addLegacyMaterialAliases("billet", "billets", "billet_pu_mix", "PlutoniumRG");
        addLegacyMaterialAliases("billet", "billets", "billet_am241", "Americium241", "Am241");
        addLegacyMaterialAliases("billet", "billets", "billet_am242", "Americium242", "Am242");
        addLegacyMaterialAliases("billet", "billets", "billet_am_mix", "AmericiumRG");
        addLegacyMaterialAliases("billet", "billets", "billet_neptunium", "Neptunium237", "Np237", "Neptunium");
        addLegacyMaterialAliases("billet", "billets", "billet_polonium", "Polonium210", "Po210", "Polonium");
        addLegacyMaterialAliases("billet", "billets", "billet_technetium", "Technetium99", "Tc99");
        addLegacyMaterialAliases("billet", "billets", "billet_cobalt", "Cobalt");
        addLegacyMaterialAliases("billet", "billets", "billet_co60", "Cobalt60", "Co60");
        addLegacyMaterialAliases("billet", "billets", "billet_sr90", "Strontium90", "Sr90");
        addLegacyMaterialAliases("billet", "billets", "billet_au198", "Gold198", "Au198");
        addLegacyMaterialAliases("billet", "billets", "billet_pb209", "Lead209", "Pb209");
        addLegacyMaterialAliases("billet", "billets", "billet_ra226", "Radium226", "Ra226");
        addLegacyMaterialAliases("billet", "billets", "billet_actinium", "Actinium227", "Ac227");
        addLegacyMaterialAliases("billet", "billets", "billet_solinium", "Solinium");
        addLegacyMaterialAliases("billet", "billets", "billet_beryllium", "Beryllium");
        addLegacyMaterialAliases("billet", "billets", "billet_bismuth", "Bismuth");
        addLegacyMaterialAliases("billet", "billets", "billet_zirconium", "Zirconium");

        addLegacyMaterialAliases("wireFine", "wires", "wire_gold", "Gold");
        addLegacyMaterialAliases("wireDense", "dense_wires", "wire_dense_gold", "Gold");
        addLegacyMaterialAliases("wireDense", "dense_wires", "wire_dense_niobium", "Niobium");
        addLegacyMaterialAliases("wireDense", "dense_wires", "wire_dense_bscco", "BSCCO");
        addLegacyMaterialAliases("ntmpipe", "pipes", "pipes_steel", "Steel");

        addLegacyMaterialAliases("plateTriple", "cast_plates", "plate_cast_bismuth_bronze", "BismuthBronze");
        addLegacyMaterialAliases("plateCast", "cast_plates", "plate_cast_bismuth_bronze", "BismuthBronze");
        addLegacyMaterialAliases("plateTriple", "cast_plates", "plate_cast_arsenic_bronze", "ArsenicBronze");
        addLegacyMaterialAliases("plateCast", "cast_plates", "plate_cast_arsenic_bronze", "ArsenicBronze");
        addLegacyMaterialAliases("plateTriple", "cast_plates", "plate_cast_combine_steel", "CMBSteel");
        addLegacyMaterialAliases("plateCast", "cast_plates", "plate_cast_combine_steel", "CMBSteel");
        addLegacyForgeTag("ingots/any_bismoid_bronze", "ingot_bismuth_bronze", "ingot_arsenic_bronze");
        addLegacyForgeTag("cast_plates/any_bismoid_bronze", "plate_cast_bismuth_bronze", "plate_cast_arsenic_bronze");
    }

    private void addLegacyMaterialAliases(String legacyPrefix, String aggregatePath, String itemName, String... legacyMaterials) {
        for (String legacyMaterial : legacyMaterials) {
            addLegacyForgeTag(LegacyOreDictionaryMappings.itemTagPath(legacyPrefix + legacyMaterial), itemName);
        }
        addLegacyForgeTag(aggregatePath, itemName);
    }

    private void addLegacyFluidContainerTags() {
        for (HbmFluidContainerRegistry.ContainerEntry entry : HbmFluidContainerRegistry.getAllContainers()) {
            if (!entry.supportsItemTag()) {
                continue;
            }
            Item item = entry.fullContainer().getItem();
            tag(forgeItemTag(entry.legacyOreDictionaryName())).add(item);
            tag(forgeItemTag(entry.legacyCompatOreDictionaryName())).add(item);
        }
    }

    private void addLegacyForgeTag(String path, String... itemNames) {
        TagKey<Item> tag = forgeItemTag(path);
        for (String itemName : itemNames) {
            RegistryObject<Item> item = ModItems.legacyItem(itemName);
            if (item != null && markLegacyTagItem(tag, item.getId())) {
                tag(tag).add(item.get());
            }
        }
    }

    private void addLegacyForgeTag(String path, Item... items) {
        TagKey<Item> tag = forgeItemTag(path);
        for (Item item : items) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (itemId != null && markLegacyTagItem(tag, itemId)) {
                tag(tag).add(item);
            }
        }
    }

    private boolean markLegacyTagItem(TagKey<Item> tag, ResourceLocation itemId) {
        return addedLegacyTagItems.computeIfAbsent(tag, ignored -> new HashSet<>()).add(itemId);
    }

    public static TagKey<Item> forgeItemTag(String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }

    public static TagKey<Item> legacyOreItemTag(String legacyOreName) {
        return LegacyOreDictionaryMappings.itemTag(legacyOreName);
    }
}
