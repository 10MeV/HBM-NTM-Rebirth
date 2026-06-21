package com.hbm.ntm.radiation;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.compat.Compat;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HazardRegistry {
    private static final Map<TagKey<Item>, HazardData> TAG_HAZARDS = new LinkedHashMap<>();
    private static final Map<Item, HazardData> ITEM_HAZARDS = new IdentityHashMap<>();
    private static final Map<HazardStackKey, HazardData> STACK_HAZARDS = new LinkedHashMap<>();
    private static final Map<LegacyStateVariantKey, HazardData> LEGACY_STATE_VARIANT_HAZARDS = new LinkedHashMap<>();
    private static final Set<TagKey<Item>> TAG_BLACKLIST = new HashSet<>();
    private static final Set<HazardStackKey> STACK_BLACKLIST = new HashSet<>();
    private static final List<HazardTransformer> TRANSFORMERS = new ArrayList<>();

    public static void registerDefaults() {
        TAG_HAZARDS.clear();
        ITEM_HAZARDS.clear();
        STACK_HAZARDS.clear();
        LEGACY_STATE_VARIANT_HAZARDS.clear();
        TAG_BLACKLIST.clear();
        STACK_BLACKLIST.clear();
        TRANSFORMERS.clear();
        registerTransformers();
        registerVanillaHazards();
        registerLegacyTagHazards();
        registerLegacyTagBlacklists();
        registerLegacyOptionalCompatHazards();
        registerLegacyExplosiveComponentHazards();
        register(ModItems.URANIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.U * RadiationConstants.INGOT);
        register(ModItems.URANIUM_233_INGOT.get(), HazardType.RADIATION, RadiationConstants.U233 * RadiationConstants.INGOT);
        register(ModItems.URANIUM_235_INGOT.get(), HazardType.RADIATION, RadiationConstants.U235 * RadiationConstants.INGOT);
        register(ModItems.URANIUM_238_INGOT.get(), HazardType.RADIATION, RadiationConstants.U238 * RadiationConstants.INGOT);
        register(ModItems.THORIUM_232_INGOT.get(), HazardType.RADIATION, RadiationConstants.TH232 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_238_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU238 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_239_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU239 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_240_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU240 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_241_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU241 * RadiationConstants.INGOT);
        register(ModItems.NEPTUNIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.NP237 * RadiationConstants.INGOT);
        register(ModItems.POLONIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.PO210 * RadiationConstants.INGOT);
        register(ModItems.SCHRABIDIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.SA326 * RadiationConstants.INGOT);

        registerRadioactiveParts();
        registerRadioactiveBlocks();
        registerLegacyMaterialSecondaryHazards();
        registerLegacyFissionFragmentPowderHazards();
        registerExistingLegacyResourceHazards();
        registerLegacyWasteAndCrystalHazards();
        registerLegacyNukePartHazards();
        registerLegacyHolotapeHazards();
        registerLegacyDemonCoreHazards();
        registerLegacyReactorComponentHazards();
        registerLegacyBreedingRodHazards();
        registerLegacyBalefireAndReactorDebrisHazards();

        register(ModItems.URANIUM_POWDER.get(), HazardType.RADIATION, RadiationConstants.U * RadiationConstants.POWDER_MULTIPLIER);
        register(ModItems.PLUTONIUM_POWDER.get(), HazardType.RADIATION, RadiationConstants.PU * RadiationConstants.POWDER_MULTIPLIER);
        register(ModItems.THORIUM_POWDER.get(), HazardType.RADIATION, RadiationConstants.TH232 * RadiationConstants.POWDER_MULTIPLIER);

        register(ModBlocks.NUKE_GADGET.get().asItem(), HazardType.RADIATION, RadiationConstants.PU239 * 10.0F);
        register(ModBlocks.NUKE_BOY.get().asItem(), HazardType.RADIATION, RadiationConstants.U235 * 3.0F);
        register(ModBlocks.NUKE_MAN.get().asItem(), HazardType.RADIATION, RadiationConstants.PU239 * 10.0F);
        register(ModBlocks.NUKE_MIKE.get().asItem(), HazardType.RADIATION, RadiationConstants.U238 * 10.0F);
        register(ModBlocks.NUKE_TSAR.get().asItem(), HazardType.RADIATION, RadiationConstants.PU239 * 15.0F);
        register(ModBlocks.NUKE_FLEIJA.get().asItem(), HazardType.RADIATION, RadiationConstants.SA326);
        register(ModBlocks.NUKE_SOLINIUM.get().asItem(), HazardType.RADIATION, RadiationConstants.SA326 * 8.0F);
        register(ModBlocks.NUKE_SOLINIUM.get().asItem(), HazardType.BLINDING, 45.0F);
        register(ModBlocks.NUKE_FLEIJA.get().asItem(), HazardType.EXPLOSIVE, 8.0F);
        register(ModBlocks.NUKE_FLEIJA.get().asItem(), HazardType.BLINDING, 50.0F);
        register(ModBlocks.NUKE_FSTBMB.get().asItem(), HazardType.DIGAMMA, 0.01F);
        registerBlockByName("lamp_demon", HazardType.RADIATION, 100_000.0F);
        register(ModItems.PARTICLE_DIGAMMA.get(), HazardType.DIGAMMA, 1000.0F / 60.0F);
    }

    public static void registerTransformer(HazardTransformer transformer) {
        TRANSFORMERS.add(transformer);
    }

    public static void registerTransformerBefore(HazardTransformer transformer, HazardTransformer before) {
        int index = TRANSFORMERS.indexOf(before);
        if (index < 0) {
            TRANSFORMERS.add(transformer);
        } else {
            TRANSFORMERS.add(index, transformer);
        }
    }

    public static void replaceTransformer(HazardTransformer previous, HazardTransformer replacement) {
        int index = TRANSFORMERS.indexOf(previous);
        if (index < 0) {
            TRANSFORMERS.add(replacement);
        } else {
            TRANSFORMERS.set(index, replacement);
        }
    }

    public static void unregisterTransformer(HazardTransformer transformer) {
        TRANSFORMERS.remove(transformer);
    }

    private static void registerTransformers() {
        TRANSFORMERS.add(new NbtRadiationHazardTransformer());
        // LBSM hazard bypasses are intentionally not modernized; keep legacy standard-mode behavior.
        TRANSFORMERS.add(new ContainerRadiationHazardTransformer());
        TRANSFORMERS.add(new MeRadiationHazardTransformer());
    }

    private static void registerVanillaHazards() {
        register(net.minecraft.world.item.Items.GUNPOWDER, HazardType.EXPLOSIVE, 1.0F);
        register(net.minecraft.world.item.Items.TNT, HazardType.EXPLOSIVE, 4.0F);
        register(net.minecraft.world.item.Items.PUMPKIN_PIE, HazardType.EXPLOSIVE, 1.0F);
    }

    private static void registerLegacyTagHazards() {
        registerForgeTag("dusts/coal", HazardType.COAL, RadiationConstants.POWDER_MULTIPLIER);
        registerForgeTag("tiny_dusts/coal", HazardType.COAL, RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);
        registerForgeTag("dusts/lignite", HazardType.COAL, RadiationConstants.POWDER_MULTIPLIER);
        registerForgeTag("tiny_dusts/lignite", HazardType.COAL, RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);
        registerForgeTag("gems/lignite", HazardType.COAL, RadiationConstants.INGOT);
    }

    private static void registerLegacyTagBlacklists() {
        blacklist(forgeItemTag("ores/thorium"));
        blacklist(forgeItemTag("ores/uranium"));
    }

    private static void registerLegacyOptionalCompatHazards() {
        registerLegacyReactorCraftWasteHazards();
        registerLegacyGregTechNaquadahHazards();
    }

    private static void registerLegacyReactorCraftWasteHazards() {
        Item reactorCraftWaste = Compat.tryLoadItem(Compat.MOD_REC, "reactorcraft_item_waste");
        if (reactorCraftWaste == null) {
            return;
        }
        for (Compat.ReikaIsotope isotope : Compat.ReikaIsotope.values()) {
            if (isotope.getRad() <= 0.0F) {
                continue;
            }
            ItemStack stack = new ItemStack(reactorCraftWaste);
            stack.setDamageValue(isotope.ordinal());
            registerStack(stack, HazardType.RADIATION, isotope.getRad());
        }
    }

    private static void registerLegacyGregTechNaquadahHazards() {
        if (!Compat.isModLoaded(Compat.MOD_GT6)) {
            return;
        }
        Set<ResourceLocation> registeredTags = new HashSet<>();
        for (LegacyMaterialShape shape : LegacyMaterialShape.STANDARD_AUTOGEN_SHAPES) {
            for (String prefix : shape.prefixes()) {
                registerLegacyGregTechMaterialTag(prefix, "Naquadah", RadiationConstants.U, shape, registeredTags);
                registerLegacyGregTechMaterialTag(prefix, "Naquadah-Enriched", RadiationConstants.U235, shape, registeredTags);
                registerLegacyGregTechMaterialTag(prefix, "Naquadria", RadiationConstants.PU239, shape, registeredTags);
            }
        }
    }

    private static void registerLegacyGregTechMaterialTag(String prefix, String material, float baseRadiation,
                                                          LegacyMaterialShape shape, Set<ResourceLocation> registeredTags) {
        ResourceLocation tagId = LegacyOreDictionaryMappings.itemTagId(prefix + material);
        if (!registeredTags.add(tagId)) {
            return;
        }
        float level = baseRadiation * shape.quantity() / LegacyMaterialShape.INGOT_QUANTITY;
        HazardData data = new HazardData()
                .setMutex(0b1)
                .addEntry(HazardType.RADIATION, level);
        registerTag(TagKey.create(Registries.ITEM, tagId), data);
    }

    private static void registerLegacyExplosiveComponentHazards() {
        registerByName("ball_dynamite", HazardType.EXPLOSIVE, 2.0F);
        registerByName("stick_dynamite", HazardType.EXPLOSIVE, 1.0F);
        registerByName("stick_tnt", HazardType.EXPLOSIVE, 1.5F);
        registerByName("stick_semtex", HazardType.EXPLOSIVE, 2.5F);
        registerByName("stick_c4", HazardType.EXPLOSIVE, 2.5F);
        registerByName("cordite", HazardType.EXPLOSIVE, 2.0F);
        registerByName("ballistite", HazardType.EXPLOSIVE, 1.0F);
    }

    private static void registerRadioactiveParts() {
        registerRad("ingot_pu_mix", RadiationConstants.PU);
        registerRad("ingot_am241", RadiationConstants.AM241);
        registerRad("ingot_am242", RadiationConstants.AM242);
        registerRad("ingot_am_mix", RadiationConstants.AM_MIX);
        registerRad("ingot_technetium", RadiationConstants.TC99);
        registerRad("ingot_co60", RadiationConstants.CO60);
        registerRad("ingot_sr90", RadiationConstants.SR90);
        registerRad("ingot_au198", RadiationConstants.AU198);
        registerRad("ingot_pb209", RadiationConstants.PB209);
        registerRad("ingot_ra226", RadiationConstants.RA226);

        registerRad("ingot_uranium_fuel", RadiationConstants.U_FUEL);
        registerRad("ingot_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerRad("ingot_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerRad("ingot_mox_fuel", RadiationConstants.MOX_FUEL);
        registerRad("ingot_americium_fuel", RadiationConstants.AM_FUEL);
        registerRad("ingot_thorium_fuel", RadiationConstants.TH_FUEL);
        registerRad("ingot_schrabidium_fuel", RadiationConstants.SA_FUEL);
        registerByName("ingot_schrabidium_fuel", HazardType.BLINDING, 5.0F);
        registerRad("ingot_hes", RadiationConstants.SA_FUEL);
        registerRad("ingot_les", RadiationConstants.SA_FUEL);

        registerRad("solid_fuel_bf", 1_000.0F);
        registerRad("solid_fuel_presto_bf", 2_000.0F);
        registerRad("solid_fuel_presto_triplet_bf", 6_000.0F);

        registerNugget("nugget_th232", RadiationConstants.TH232);
        registerNugget("nugget_uranium", RadiationConstants.U);
        registerNugget("nugget_u233", RadiationConstants.U233);
        registerNugget("nugget_u235", RadiationConstants.U235);
        registerNugget("nugget_u238", RadiationConstants.U238);
        registerNugget("nugget_plutonium", RadiationConstants.PU);
        registerNugget("nugget_pu238", RadiationConstants.PU238);
        registerNugget("nugget_pu239", RadiationConstants.PU239);
        registerNugget("nugget_pu240", RadiationConstants.PU240);
        registerNugget("nugget_pu241", RadiationConstants.PU241);
        registerNugget("nugget_pu_mix", RadiationConstants.PU);
        registerNugget("nugget_am241", RadiationConstants.AM241);
        registerNugget("nugget_am242", RadiationConstants.AM242);
        registerNugget("nugget_am_mix", RadiationConstants.AM_MIX);
        registerNugget("nugget_neptunium", RadiationConstants.NP237);
        registerNugget("nugget_polonium", RadiationConstants.PO210);
        registerNugget("nugget_technetium", RadiationConstants.TC99);
        registerNugget("nugget_co60", RadiationConstants.CO60);
        registerNugget("nugget_sr90", RadiationConstants.SR90);
        registerNugget("nugget_au198", RadiationConstants.AU198);
        registerNugget("nugget_pb209", RadiationConstants.PB209);
        registerNugget("nugget_ra226", RadiationConstants.RA226);
        registerNugget("nugget_actinium", RadiationConstants.AC227);

        registerNugget("nugget_uranium_fuel", RadiationConstants.U_FUEL);
        registerNugget("nugget_thorium_fuel", RadiationConstants.TH_FUEL);
        registerNugget("nugget_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerNugget("nugget_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerNugget("nugget_mox_fuel", RadiationConstants.MOX_FUEL);
        registerNugget("nugget_americium_fuel", RadiationConstants.AM_FUEL);
        registerNugget("nugget_schrabidium_fuel", RadiationConstants.SA_FUEL);
        registerByName("nugget_schrabidium_fuel", HazardType.BLINDING, 5.0F * RadiationConstants.NUGGET);
        registerNugget("nugget_hes", RadiationConstants.SA_FUEL);
        registerNugget("nugget_les", RadiationConstants.SA_FUEL);

        registerBillet("billet_uranium", RadiationConstants.U);
        registerBillet("billet_u233", RadiationConstants.U233);
        registerBillet("billet_u235", RadiationConstants.U235);
        registerBillet("billet_u238", RadiationConstants.U238);
        registerBillet("billet_uzh", RadiationConstants.UZH);
        registerBillet("billet_th232", RadiationConstants.TH232);
        registerBillet("billet_plutonium", RadiationConstants.PU);
        registerBillet("billet_pu238", RadiationConstants.PU238);
        registerBillet("billet_pu239", RadiationConstants.PU239);
        registerBillet("billet_pu240", RadiationConstants.PU240);
        registerBillet("billet_pu241", RadiationConstants.PU241);
        registerBillet("billet_pu_mix", RadiationConstants.PU);
        registerBillet("billet_am241", RadiationConstants.AM241);
        registerBillet("billet_am242", RadiationConstants.AM242);
        registerBillet("billet_am_mix", RadiationConstants.AM_MIX);
        registerBillet("billet_neptunium", RadiationConstants.NP237);
        registerBillet("billet_polonium", RadiationConstants.PO210);
        registerBillet("billet_technetium", RadiationConstants.TC99);
        registerBillet("billet_co60", RadiationConstants.CO60);
        registerBillet("billet_sr90", RadiationConstants.SR90);
        registerBillet("billet_au198", RadiationConstants.AU198);
        registerBillet("billet_pb209", RadiationConstants.PB209);
        registerBillet("billet_ra226", RadiationConstants.RA226);
        registerBillet("billet_actinium", RadiationConstants.AC227);
        registerBillet("billet_solinium", RadiationConstants.SA327);
        registerRad("ingot_solinium", RadiationConstants.SA327 * RadiationConstants.INGOT);
        registerNugget("nugget_solinium", RadiationConstants.SA327);
        registerBillet("billet_uranium_fuel", RadiationConstants.U_FUEL);
        registerBillet("billet_thorium_fuel", RadiationConstants.TH_FUEL);
        registerBillet("billet_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerBillet("billet_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerBillet("billet_mox_fuel", RadiationConstants.MOX_FUEL);
        registerBillet("billet_americium_fuel", RadiationConstants.AM_FUEL);
        registerBillet("billet_schrabidium_fuel", RadiationConstants.SA_FUEL);
        registerBillet("billet_hes", RadiationConstants.SA_FUEL);
        registerBillet("billet_les", RadiationConstants.SA_FUEL);
        registerBillet("billet_po210be", RadiationConstants.PO210_BE);
        registerBillet("billet_ra226be", RadiationConstants.RA226_BE);
        registerBillet("billet_pu238be", RadiationConstants.PU238_BE);
        registerRad("billet_nuclear_waste", RadiationConstants.WASTE * RadiationConstants.BILLET);
        registerByName("billet_schrabidium_fuel", HazardType.BLINDING, 5.0F * RadiationConstants.BILLET);
    }

    private static void registerRadioactiveBlocks() {
        registerBlockRad("ore_uranium", RadiationConstants.U);
        registerBlockRad("ore_uranium_scorched", RadiationConstants.U);
        registerBlockRad("ore_thorium", RadiationConstants.TH232);
        registerBlockRad("ore_schrabidium", RadiationConstants.SA326);
        registerBlockRad("ore_nether_uranium", RadiationConstants.U);
        registerBlockRad("ore_nether_uranium_scorched", RadiationConstants.U);
        registerBlockRad("ore_nether_plutonium", RadiationConstants.PU);
        registerBlockRad("ore_nether_schrabidium", RadiationConstants.SA326);
        registerBlockRad("ore_gneiss_uranium", RadiationConstants.U);
        registerBlockRad("ore_gneiss_uranium_scorched", RadiationConstants.U);
        registerBlockRad("ore_gneiss_schrabidium", RadiationConstants.SA326);
        registerBlockRad("ore_sellafield_uranium_scorched", RadiationConstants.U);
        registerBlockRad("ore_sellafield_schrabidium", RadiationConstants.SA326);
        registerBlockRad("ore_sellafield_radgem", 25.0F);
        registerSellafieldItemHazards();
        registerBlockRad("waste_trinitite", RadiationConstants.TRINITITE * RadiationConstants.BLOCK);
        registerBlockRad("waste_trinitite_red", RadiationConstants.TRINITITE * RadiationConstants.BLOCK);

        registerBlockRad("block_uranium", RadiationConstants.U * RadiationConstants.BLOCK);
        registerBlockRad("block_u233", RadiationConstants.U233 * RadiationConstants.BLOCK);
        registerBlockRad("block_u235", RadiationConstants.U235 * RadiationConstants.BLOCK);
        registerBlockRad("block_u238", RadiationConstants.U238 * RadiationConstants.BLOCK);
        registerBlockRad("block_uranium_fuel", RadiationConstants.U_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_thorium", RadiationConstants.TH232 * RadiationConstants.BLOCK);
        registerBlockRad("block_thorium_fuel", RadiationConstants.TH_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_neptunium", RadiationConstants.NP237 * RadiationConstants.BLOCK);
        registerBlockRad("block_polonium", RadiationConstants.PO210 * RadiationConstants.BLOCK);
        registerBlockRad("block_mox_fuel", RadiationConstants.MOX_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_plutonium", RadiationConstants.PU * RadiationConstants.BLOCK);
        registerBlockRad("block_pu238", RadiationConstants.PU238 * RadiationConstants.BLOCK);
        registerBlockRad("block_pu239", RadiationConstants.PU239 * RadiationConstants.BLOCK);
        registerBlockRad("block_pu240", RadiationConstants.PU240 * RadiationConstants.BLOCK);
        registerBlockRad("block_pu_mix", RadiationConstants.PU_REACTOR_GRADE * RadiationConstants.BLOCK);
        registerBlockRad("block_plutonium_fuel", RadiationConstants.PU_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_trinitite", RadiationConstants.TRINITITE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste", RadiationConstants.WASTE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste_painted", RadiationConstants.WASTE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste_vitrified", RadiationConstants.WASTE_VITRIFIED * RadiationConstants.BLOCK);
        registerBlockRad("ancient_scrap", 150.0F);
        registerBlockRad("block_corium", 150.0F);
        registerBlockRad("block_corium_cobble", 150.0F);
        registerBlockRad("block_schraranium", RadiationConstants.SCHRARANIUM * RadiationConstants.BLOCK);
        registerBlockRad("block_schrabidium", RadiationConstants.SA326 * RadiationConstants.BLOCK);
        registerBlockRad("block_schrabidate", RadiationConstants.SA326 * 0.1F * RadiationConstants.BLOCK);
        registerBlockRad("block_solinium", RadiationConstants.SA327 * RadiationConstants.BLOCK);
        registerBlockRad("block_schrabidium_fuel", RadiationConstants.SA_FUEL * RadiationConstants.BLOCK);
        registerBlockByName("block_schrabidium_fuel", HazardType.BLINDING, 5.0F * RadiationConstants.BLOCK);
        registerBlockRad("block_ra226", RadiationConstants.RA226 * RadiationConstants.BLOCK);
        registerBlockRad("block_actinium", RadiationConstants.AC227 * RadiationConstants.BLOCK);
    }

    private static void registerSellafieldItemHazards() {
        registerLegacyStateBlockVariant("sellafield", 0, HazardType.RADIATION, 0.5F);
        registerLegacyStateBlockVariant("sellafield", 1, HazardType.RADIATION, 1.0F);
        registerLegacyStateBlockVariant("sellafield", 2, HazardType.RADIATION, 2.5F);
        registerLegacyStateBlockVariant("sellafield", 3, HazardType.RADIATION, 4.0F);
        registerLegacyStateBlockVariant("sellafield", 4, HazardType.RADIATION, 5.0F);
        registerLegacyStateBlockVariant("sellafield", 5, HazardType.RADIATION, 10.0F);
    }

    private static void registerLegacyMaterialSecondaryHazards() {
        registerShapeByName("ingot_pu238", HazardType.HOT, 3.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_pu238", HazardType.HOT, 3.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_pu238", HazardType.HOT, 3.0F, RadiationConstants.BILLET);
        registerBlockShapeByName("block_pu238", HazardType.HOT, 3.0F, RadiationConstants.BLOCK);

        registerShapeByName("ingot_polonium", HazardType.HOT, 3.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_polonium", HazardType.HOT, 3.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_polonium", HazardType.HOT, 3.0F, RadiationConstants.BILLET);
        registerBlockShapeByName("block_polonium", HazardType.HOT, 3.0F, RadiationConstants.BLOCK);

        registerShapeByName("ingot_co60", HazardType.HOT, 1.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_co60", HazardType.HOT, 1.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_co60", HazardType.HOT, 1.0F, RadiationConstants.BILLET);

        registerShapeByName("ingot_sr90", HazardType.HOT, 1.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_sr90", HazardType.HOT, 1.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_sr90", HazardType.HOT, 1.0F, RadiationConstants.BILLET);
        registerShapeByName("ingot_sr90", HazardType.HYDROACTIVE, 1.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_sr90", HazardType.HYDROACTIVE, 1.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_sr90", HazardType.HYDROACTIVE, 1.0F, RadiationConstants.BILLET);

        registerShapeByName("ingot_au198", HazardType.HOT, 5.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_au198", HazardType.HOT, 5.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_au198", HazardType.HOT, 5.0F, RadiationConstants.BILLET);

        registerShapeByName("ingot_pb209", HazardType.HOT, 7.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_pb209", HazardType.HOT, 7.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_pb209", HazardType.HOT, 7.0F, RadiationConstants.BILLET);
        registerShapeByName("ingot_pb209", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_pb209", HazardType.BLINDING, 50.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_pb209", HazardType.BLINDING, 50.0F, RadiationConstants.BILLET);

        registerShapeByName("ingot_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.BILLET);
        registerShapeByName("plate_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerBlockShapeByName("block_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.BLOCK);
        registerBlockShapeByName("ore_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerBlockShapeByName("ore_gneiss_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerBlockShapeByName("ore_nether_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerBlockShapeByName("ore_sellafield_schrabidium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);

        registerShapeByName("ingot_solinium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerShapeByName("nugget_solinium", HazardType.BLINDING, 50.0F, RadiationConstants.NUGGET);
        registerShapeByName("billet_solinium", HazardType.BLINDING, 50.0F, RadiationConstants.BILLET);
        registerBlockShapeByName("block_solinium", HazardType.BLINDING, 50.0F, RadiationConstants.BLOCK);

        registerShapeByName("ingot_schrabidate", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerBlockShapeByName("block_schrabidate", HazardType.BLINDING, 50.0F, RadiationConstants.BLOCK);
        registerShapeByName("ingot_schraranium", HazardType.BLINDING, 50.0F, RadiationConstants.INGOT);
        registerBlockShapeByName("block_schraranium", HazardType.BLINDING, 50.0F, RadiationConstants.BLOCK);
    }

    private static void registerLegacyFissionFragmentPowderHazards() {
        registerPowderByName("powder_polonium", RadiationConstants.PO210, 3.0F, 0.0F);
        registerPowderByName("powder_co60", RadiationConstants.CO60, 1.0F, 0.0F);
        registerPowderByName("powder_au198", RadiationConstants.AU198, 5.0F, 0.0F);

        registerPowderByName("powder_sr90", RadiationConstants.SR90, 1.0F, 1.0F);
        registerTinyPowderByName("powder_sr90_tiny", RadiationConstants.SR90, 1.0F, 1.0F);

        registerPowderByName("powder_i131", RadiationConstants.I131, 1.0F, 0.0F);
        registerTinyPowderByName("powder_i131_tiny", RadiationConstants.I131, 1.0F, 0.0F);
        registerPowderByName("powder_xe135", RadiationConstants.XE135, 10.0F, 0.0F);
        registerTinyPowderByName("powder_xe135_tiny", RadiationConstants.XE135, 10.0F, 0.0F);
        registerTinyPowderByName("powder_cs137_tiny", RadiationConstants.CS137, 3.0F, 3.0F);
        registerPowderByName("powder_at209", RadiationConstants.AT209, 20.0F, 0.0F);
    }

    private static void registerExistingLegacyResourceHazards() {
        registerRad("billet_yharonite", 25.0F * RadiationConstants.BILLET);
        registerRad("billet_zfb_bismuth", RadiationConstants.U235 * 0.35F * RadiationConstants.BILLET);
        registerRad("billet_zfb_pu241", RadiationConstants.PU241 * 0.5F * RadiationConstants.BILLET);
        registerRad("billet_zfb_am_mix", RadiationConstants.AM_MIX * 0.5F * RadiationConstants.BILLET);
        registerRad("nugget_cobalt", RadiationConstants.CO60 * RadiationConstants.NUGGET);
        registerRad("insert_polonium", 100.0F);
        registerRad("ingot_mud", 1.0F);
        registerBlockRad("block_yellowcake", RadiationConstants.YELLOWCAKE * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER);
        registerBlockRad("block_euphemium", 500_000.0F);
        registerBlockRad("block_euphemium_cluster", 500_000.0F);
        registerBlockRad("block_schrabidium_cluster", RadiationConstants.SA326 * RadiationConstants.BLOCK);
        registerBlockRad("block_tritium", 0.1F);
        registerRad("fallout", RadiationConstants.FALLOUT * RadiationConstants.POWDER_MULTIPLIER * 2.0F);
        registerBlockRad("block_fallout", RadiationConstants.YELLOWCAKE * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER);
        registerBlockByName("yellow_barrel", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.INGOT * 10.0F);
        registerBlockRad("ore_gneiss_gas", 1.0F);
        registerBlockRad("ore_asbestos", 1.0F);
        registerBlockRad("ore_gneiss_asbestos", 1.0F);

        registerByName("powder_lignite", HazardType.COAL, RadiationConstants.POWDER_MULTIPLIER);
        registerByName("lignite", HazardType.COAL, RadiationConstants.INGOT);
        registerByName("rocket_fuel", HazardType.EXPLOSIVE, 1.0F);
        registerByName("solid_fuel", HazardType.EXPLOSIVE, 1.0F);
        registerByName("solid_fuel_presto", HazardType.EXPLOSIVE, 2.0F);
        registerByName("solid_fuel_presto_triplet", HazardType.EXPLOSIVE, 6.0F);
        registerByName("ingot_phosphorus", HazardType.HOT, 5.0F);
        registerByName("lithium", HazardType.HYDROACTIVE, 1.0F);
        registerByName("powder_calcium", HazardType.HYDROACTIVE, 1.0F);
        registerMaterialPowderHazards();

        registerBlockByName("block_semtex", HazardType.EXPLOSIVE, 4.0F);
        registerBlockByName("block_c4", HazardType.EXPLOSIVE, 4.0F);
        registerBlockByName("brick_fire", HazardType.HOT, 2.0F);
        registerBlockByName("ore_nether_fire", HazardType.HOT, 2.0F);
        registerBlockByName("block_white_phosphorus", HazardType.HOT, 5.0F * RadiationConstants.BLOCK);
        registerBlockByName("block_lithium", HazardType.HYDROACTIVE, 1.0F);
        registerBlockByName("ore_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("ore_gneiss_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("block_asbestos", HazardType.ASBESTOS, 5.0F);
        registerBlockByName("deco_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("concrete_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("brick_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("tile_lab_broken", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("ore_nether_coal", HazardType.COAL, 1.0F);
        registerByName("coal_infernal", HazardType.COAL, 10.0F);
    }

    private static void registerLegacyWasteAndCrystalHazards() {
        registerByName("cell_sas3",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SAS3),
                new HazardEntry(HazardType.BLINDING, 60.0F));
        registerByName("nuclear_waste_long", HazardType.RADIATION, 5.0F);
        registerByName("nuclear_waste_long_tiny", HazardType.RADIATION, 0.5F);
        registerByName("nuclear_waste_short",
                new HazardEntry(HazardType.RADIATION, 30.0F),
                new HazardEntry(HazardType.HOT, 5.0F));
        registerByName("nuclear_waste_short_tiny",
                new HazardEntry(HazardType.RADIATION, 3.0F),
                new HazardEntry(HazardType.HOT, 5.0F));
        registerByName("nuclear_waste_long_depleted", HazardType.RADIATION, 0.5F);
        registerByName("nuclear_waste_long_depleted_tiny", HazardType.RADIATION, 0.05F);
        registerByName("nuclear_waste_short_depleted", HazardType.RADIATION, 3.0F);
        registerByName("nuclear_waste_short_depleted_tiny", HazardType.RADIATION, 0.3F);
        registerByName("scrap_nuclear", HazardType.RADIATION, 1.0F);
        registerByName("trinitite", HazardType.RADIATION, RadiationConstants.TRINITITE * RadiationConstants.INGOT);
        registerByName("nuclear_waste", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.INGOT);
        registerByName("billet_nuclear_waste", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.BILLET);
        registerByName("nuclear_waste_tiny", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.NUGGET);
        registerByName("nuclear_waste_vitrified", HazardType.RADIATION, RadiationConstants.WASTE_VITRIFIED * RadiationConstants.INGOT);
        registerByName("nuclear_waste_vitrified_tiny", HazardType.RADIATION, RadiationConstants.WASTE_VITRIFIED * RadiationConstants.NUGGET);
        registerByName("gem_rad", HazardType.RADIATION, 25.0F);
        registerByName("powder_neptunium", HazardType.RADIATION, RadiationConstants.NP237 * RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_yellowcake", HazardType.RADIATION, RadiationConstants.YELLOWCAKE * RadiationConstants.POWDER_MULTIPLIER);
        registerByName("fallout", HazardType.RADIATION, RadiationConstants.FALLOUT * RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_strontium",
                new HazardEntry(HazardType.HYDROACTIVE, 1.0F * RadiationConstants.POWDER_MULTIPLIER),
                new HazardEntry(HazardType.HOT, 1.0F * RadiationConstants.POWDER_MULTIPLIER));
        registerByName("powder_cs137",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.CS137 * RadiationConstants.POWDER_MULTIPLIER),
                new HazardEntry(HazardType.HYDROACTIVE, 3.0F * RadiationConstants.POWDER_MULTIPLIER),
                new HazardEntry(HazardType.HOT, 3.0F * RadiationConstants.POWDER_MULTIPLIER));
        registerByName("powder_caesium",
                new HazardEntry(HazardType.HYDROACTIVE, 1.0F),
                new HazardEntry(HazardType.HOT, 3.0F));
        registerByName("powder_coltan_ore", HazardType.ASBESTOS, 3.0F);
        registerCrystalByName("crystal_uranium", HazardType.RADIATION, RadiationConstants.U);
        registerCrystalByName("crystal_thorium", HazardType.RADIATION, RadiationConstants.TH232);
        registerCrystalByName("crystal_plutonium", HazardType.RADIATION, RadiationConstants.PU);
        registerCrystalByName("crystal_schraranium", HazardType.RADIATION, RadiationConstants.SCHRARANIUM);
        registerCrystalByName("crystal_schrabidium", HazardType.RADIATION, RadiationConstants.SA326);
        registerCrystalByName("crystal_phosphorus", HazardType.HOT, 2.0F);
        registerCrystalByName("crystal_lithium", HazardType.HYDROACTIVE, 1.0F);
        registerCrystalByName("crystal_trixite", HazardType.RADIATION, RadiationConstants.TRIXITE);
    }

    private static void registerLegacyNukePartHazards() {
        registerByName("boy_propellant", HazardType.EXPLOSIVE, 2.0F);
        registerByName("gadget_core", HazardType.RADIATION, RadiationConstants.PU239 * RadiationConstants.NUGGET * 10.0F);
        registerByName("boy_target", HazardType.RADIATION, RadiationConstants.U235 * RadiationConstants.INGOT * 2.0F);
        registerByName("boy_bullet", HazardType.RADIATION, RadiationConstants.U235 * RadiationConstants.INGOT);
        registerByName("man_core", HazardType.RADIATION, RadiationConstants.PU239 * RadiationConstants.NUGGET * 10.0F);
        registerByName("mike_core", HazardType.RADIATION, RadiationConstants.U238 * RadiationConstants.NUGGET * 10.0F);
        registerByName("tsar_core", HazardType.RADIATION, RadiationConstants.PU239 * RadiationConstants.NUGGET * 15.0F);
        registerByName("fleija_propellant",
                new HazardEntry(HazardType.RADIATION, 15.0F),
                new HazardEntry(HazardType.EXPLOSIVE, 8.0F),
                new HazardEntry(HazardType.BLINDING, 50.0F));
        registerByName("fleija_core", HazardType.RADIATION, 10.0F);
        registerByName("solinium_propellant", HazardType.EXPLOSIVE, 10.0F);
        registerByName("solinium_core",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SA327 * RadiationConstants.NUGGET * 8.0F),
                new HazardEntry(HazardType.BLINDING, 45.0F));
    }

    private static void registerLegacyHolotapeHazards() {
        registerByName("holotape_damaged", HazardType.DIGAMMA, 1000.0F);
        registerLegacyMeta(LegacyMetaItemMappings.HOLOTAPE_IMAGE, 1, HazardType.DIGAMMA, 1.0F);
    }

    private static void registerLegacyDemonCoreHazards() {
        register(ModItems.DEMON_CORE_OPEN.get(), HazardType.RADIATION, 5.0F);
        register(ModItems.DEMON_CORE_CLOSED.get(), HazardType.RADIATION, 100_000.0F);
    }

    private static void registerLegacyReactorComponentHazards() {
        registerLegacyDepletedFuelWaste("waste_natural_uranium", RadiationConstants.WASTE * RadiationConstants.BILLET * 11.5F);
        registerLegacyDepletedFuelWaste("waste_uranium", RadiationConstants.WASTE * RadiationConstants.BILLET * 10.0F);
        registerLegacyDepletedFuelWaste("waste_thorium", RadiationConstants.WASTE * RadiationConstants.BILLET * 7.5F);
        registerLegacyDepletedFuelWaste("waste_mox", RadiationConstants.WASTE * RadiationConstants.BILLET * 10.0F);
        registerLegacyDepletedFuelWaste("waste_plutonium", RadiationConstants.WASTE * RadiationConstants.BILLET * 12.5F);
        registerLegacyDepletedFuelWaste("waste_u233", RadiationConstants.WASTE * RadiationConstants.BILLET * 10.0F);
        registerLegacyDepletedFuelWaste("waste_u235", RadiationConstants.WASTE * RadiationConstants.BILLET * 11.0F);
        registerLegacyDepletedFuelWaste("waste_schrabidium", RadiationConstants.WASTE * RadiationConstants.BILLET * 15.0F);
        registerLegacyDepletedFuelWaste("waste_zfb_mox", RadiationConstants.WASTE * RadiationConstants.BILLET * 5.0F);

        registerFuelByName("plate_fuel_u233", RadiationConstants.U233 * RadiationConstants.INGOT, RadiationConstants.WASTE * RadiationConstants.INGOT * 13.0F, false);
        registerFuelByName("plate_fuel_u235", RadiationConstants.U235 * RadiationConstants.INGOT, RadiationConstants.WASTE * RadiationConstants.INGOT * 10.0F, false);
        registerFuelByName("plate_fuel_mox", RadiationConstants.MOX_FUEL * RadiationConstants.INGOT, RadiationConstants.WASTE * RadiationConstants.INGOT * 16.0F, false);
        registerFuelByName("plate_fuel_pu239", RadiationConstants.PU239 * RadiationConstants.INGOT, RadiationConstants.WASTE * RadiationConstants.INGOT * 13.5F, false);
        registerFuelByName("plate_fuel_sa326", RadiationConstants.SA326 * RadiationConstants.INGOT, RadiationConstants.WASTE * RadiationConstants.INGOT * 10.0F, true);
        registerFuelByName("plate_fuel_ra226be", RadiationConstants.RA226_BE * RadiationConstants.BILLET, RadiationConstants.PO210_BE * RadiationConstants.NUGGET * 3.0F, false);
        registerFuelByName("plate_fuel_pu238be", RadiationConstants.PU238_BE * RadiationConstants.BILLET, RadiationConstants.PU238_BE * RadiationConstants.NUGGET, false);

        registerLegacyDepletedFuelWaste("waste_plate_u233", RadiationConstants.WASTE * RadiationConstants.INGOT * 13.0F);
        registerLegacyDepletedFuelWaste("waste_plate_u235", RadiationConstants.WASTE * RadiationConstants.INGOT * 10.0F);
        registerLegacyDepletedFuelWaste("waste_plate_mox", RadiationConstants.WASTE * RadiationConstants.INGOT * 16.0F);
        registerLegacyDepletedFuelWaste("waste_plate_pu239", RadiationConstants.WASTE * RadiationConstants.INGOT * 13.5F);
        registerLegacyDepletedFuelWaste("waste_plate_sa326", RadiationConstants.WASTE * RadiationConstants.INGOT * 10.0F);
        registerLegacyRadSourceWaste("waste_plate_ra226be", RadiationConstants.PO210_BE * RadiationConstants.NUGGET * 3.0F);
        registerLegacyRadSourceWaste("waste_plate_pu238be", RadiationConstants.PU238_BE * RadiationConstants.NUGGET);

        registerByName("pile_rod_uranium", HazardType.RADIATION, RadiationConstants.U * RadiationConstants.BILLET * 3.0F);
        registerByName("pile_rod_pu239", HazardType.RADIATION, RadiationConstants.PU_REACTOR_GRADE * RadiationConstants.BILLET + RadiationConstants.PU239 * RadiationConstants.BILLET + RadiationConstants.U * RadiationConstants.BILLET);
        registerByName("pile_rod_plutonium", HazardType.RADIATION, RadiationConstants.PU_REACTOR_GRADE * RadiationConstants.BILLET * 2.0F + RadiationConstants.U * RadiationConstants.BILLET);
        registerByName("pile_rod_source", HazardType.RADIATION, RadiationConstants.RA226_BE * RadiationConstants.BILLET * 3.0F);

        registerLegacyZirnoxFuelHazards();

        registerByName("rod_zirnox_natural_uranium_fuel_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 11.5F);
        registerByName("rod_zirnox_uranium_fuel_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 10.0F);
        registerByName("rod_zirnox_thorium_fuel_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 7.5F);
        registerByName("rod_zirnox_mox_fuel_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 10.0F);
        registerByName("rod_zirnox_plutonium_fuel_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 12.5F);
        registerByName("rod_zirnox_u233_fuel_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 10.0F);
        registerByName("rod_zirnox_u235_fuel_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 11.0F);
        registerByName("rod_zirnox_les_fuel_depleted",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 15.0F),
                new HazardEntry(HazardType.BLINDING, 20.0F));
        registerByName("rod_zirnox_tritium", HazardType.RADIATION, 0.001F * RadiationConstants.ROD_DUAL);
        registerByName("rod_zirnox_zfb_mox_depleted", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.ROD_DUAL * 5.0F);

        registerRtgPelletByName("pellet_rtg", RadiationConstants.PU238 * RadiationConstants.RTG, 0.0F, 3.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_radium", RadiationConstants.RA226 * RadiationConstants.RTG, 0.0F, 0.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_weak", (RadiationConstants.PU238 + RadiationConstants.U238 * 2.0F) * RadiationConstants.BILLET, 0.0F, 0.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_strontium", RadiationConstants.SR90 * RadiationConstants.RTG, 0.0F, 0.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_cobalt", RadiationConstants.CO60 * RadiationConstants.RTG, 0.0F, 0.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_actinium", RadiationConstants.AC227 * RadiationConstants.RTG, 0.0F, 0.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_polonium", RadiationConstants.PO210 * RadiationConstants.RTG, 0.0F, 3.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_lead", RadiationConstants.PB209 * RadiationConstants.RTG, 0.0F, 7.0F, 50.0F);
        registerRtgPelletByName("pellet_rtg_gold", RadiationConstants.AU198 * RadiationConstants.RTG, 0.0F, 5.0F, 0.0F);
        registerRtgPelletByName("pellet_rtg_americium", RadiationConstants.AM241 * RadiationConstants.RTG, 0.0F, 0.0F, 0.0F);
        registerLegacyMeta(LegacyMetaItemMappings.PELLET_RTG_DEPLETED, 2, HazardType.RADIATION, RadiationConstants.NP237 * RadiationConstants.RTG);
        registerLegacyRbmkFuelHazards();
        registerLegacyPwrFuelHazards();
        registerLegacyWatzPelletHazards();
    }

    private static void registerLegacyZirnoxFuelHazards() {
        float rodDual = RadiationConstants.ROD_DUAL;
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 0, RadiationConstants.U * rodDual, RadiationConstants.WASTE * rodDual * 11.5F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 1, RadiationConstants.U_FUEL * rodDual, RadiationConstants.WASTE * rodDual * 10.0F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 2, RadiationConstants.TH232 * rodDual, RadiationConstants.TH_FUEL * rodDual, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 3, RadiationConstants.TH_FUEL * rodDual, RadiationConstants.WASTE * rodDual * 7.5F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 4, RadiationConstants.MOX_FUEL * rodDual, RadiationConstants.WASTE * rodDual * 10.0F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 5, RadiationConstants.PU_FUEL * rodDual, RadiationConstants.WASTE * rodDual * 12.5F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 6, RadiationConstants.U233 * rodDual, RadiationConstants.WASTE * rodDual * 10.0F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 7, RadiationConstants.U235 * rodDual, RadiationConstants.WASTE * rodDual * 11.0F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 8, RadiationConstants.SA_FUEL * rodDual, RadiationConstants.WASTE * rodDual * 15.0F, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 9, 0.0F, 0.001F * rodDual, false);
        registerLegacyFuelMeta(LegacyMetaItemMappings.ROD_ZIRNOX, 10, RadiationConstants.MOX_FUEL * rodDual, RadiationConstants.WASTE * rodDual * 5.0F, false);
    }

    private static void registerLegacyBalefireAndReactorDebrisHazards() {
        registerByName("cell_tritium", HazardType.RADIATION, 0.001F);
        registerByName("cell_balefire", HazardType.RADIATION, 50.0F);
        registerByName("powder_balefire", HazardType.RADIATION, 500.0F);
        registerByName("egg_balefire_shard", HazardType.RADIATION, RadiationConstants.BALEFIRE * RadiationConstants.NUGGET);
        registerByName("egg_balefire", HazardType.RADIATION, RadiationConstants.BALEFIRE * RadiationConstants.INGOT);
        registerByName("billet_balefire_gold", HazardType.RADIATION, RadiationConstants.AU198 * RadiationConstants.BILLET);
        registerByName("billet_flashlead",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.PB209 * 1.25F * RadiationConstants.BILLET),
                new HazardEntry(HazardType.HOT, 7.0F));

        registerByName("debris_graphite",
                new HazardEntry(HazardType.RADIATION, 70.0F),
                new HazardEntry(HazardType.HOT, 5.0F));
        registerByName("debris_metal", HazardType.RADIATION, 5.0F);
        registerByName("debris_fuel",
                new HazardEntry(HazardType.RADIATION, 500.0F),
                new HazardEntry(HazardType.HOT, 5.0F));
        registerByName("debris_concrete", HazardType.RADIATION, 30.0F);
        registerByName("debris_exchanger", HazardType.RADIATION, 25.0F);
        registerByName("debris_shrapnel", HazardType.RADIATION, 2.5F);
        registerByName("debris_element", HazardType.RADIATION, 100.0F);
    }

    private static void registerLegacyBreedingRodHazards() {
        registerBreedingRodRadiation(1, 0.001F);
        registerBreedingRodRadiation(3, RadiationConstants.CO60);
        registerBreedingRodRadiation(15, RadiationConstants.RA226);
        registerBreedingRodRadiation(16, RadiationConstants.AC227);
        registerBreedingRodRadiation(4, RadiationConstants.TH232);
        registerBreedingRodRadiation(5, RadiationConstants.TH_FUEL);
        registerBreedingRodRadiation(6, RadiationConstants.U235);
        registerBreedingRodRadiation(7, RadiationConstants.NP237);
        registerBreedingRodRadiation(8, RadiationConstants.U238);
        registerBreedingRodRadiation(9, RadiationConstants.PU238);
        registerBreedingRodRadiation(10, RadiationConstants.PU239);
        registerBreedingRodRadiation(11, RadiationConstants.PU_REACTOR_GRADE);
        registerBreedingRodRadiation(12, RadiationConstants.WASTE);
        registerBreedingRodRadiation(14, RadiationConstants.U);
    }

    private static void registerLegacyRbmkFuelHazards() {
        for (LegacyRbmkHazard hazard : legacyRbmkHazards()) {
            var entry = RBMKFuelRodRegistry.find(hazard.rodId());
            if (entry.isEmpty()) {
                continue;
            }
            registerRbmkFuelByName(hazard.rodId(), hazard.rodBase(), hazard.rodDepleted(),
                    true, hazard.linear(), hazard.rodBlinding(), hazard.rodDigamma(),
                    entry.get().spec().totalYield());
            if (!hazard.pelletId().isEmpty()) {
                registerRbmkPelletByName(hazard.pelletId(), hazard.pelletBase(),
                        hazard.pelletDepleted(), hazard.pelletBlinding(), hazard.pelletDigamma());
            }
        }
    }

    private static List<LegacyRbmkHazard> legacyRbmkHazards() {
        float rod = RadiationConstants.ROD_RBMK;
        float pellet = RadiationConstants.BILLET;
        return List.of(
                rbmk("rbmk_fuel_ueu", "rbmk_pellet_ueu", RadiationConstants.U, RadiationConstants.WASTE * 20.0F, false),
                rbmk("rbmk_fuel_meu", "rbmk_pellet_meu", RadiationConstants.U_FUEL, RadiationConstants.WASTE * 21.5F, false),
                rbmk("rbmk_fuel_heu233", "rbmk_pellet_heu233", RadiationConstants.U233, RadiationConstants.WASTE * 31.0F, false),
                rbmk("rbmk_fuel_heu235", "rbmk_pellet_heu235", RadiationConstants.U235, RadiationConstants.WASTE * 30.0F, false),
                rbmk("rbmk_fuel_uzh", "rbmk_pellet_uzh", RadiationConstants.UZH, RadiationConstants.WASTE * 20.0F, false),
                rbmk("rbmk_fuel_thmeu", "rbmk_pellet_thmeu", RadiationConstants.TH_FUEL, RadiationConstants.WASTE * 17.5F, false),
                rbmk("rbmk_fuel_lep", "rbmk_pellet_lep", RadiationConstants.PU_FUEL, RadiationConstants.WASTE * 25.0F, false),
                rbmk("rbmk_fuel_mep", "rbmk_pellet_mep", RadiationConstants.PU_REACTOR_GRADE, RadiationConstants.WASTE * 30.0F, false),
                rbmk("rbmk_fuel_hep239", "rbmk_pellet_hep239", RadiationConstants.PU239, RadiationConstants.WASTE * 32.5F, false),
                rbmk("rbmk_fuel_hep241", "rbmk_pellet_hep241", RadiationConstants.PU241, RadiationConstants.WASTE * 35.0F, false),
                rbmk("rbmk_fuel_lea", "rbmk_pellet_lea", RadiationConstants.AM_FUEL, RadiationConstants.WASTE * 26.0F, false),
                rbmk("rbmk_fuel_mea", "rbmk_pellet_mea", RadiationConstants.AM_MIX, RadiationConstants.WASTE * 30.5F, false),
                rbmk("rbmk_fuel_hea241", "rbmk_pellet_hea241", RadiationConstants.AM241, RadiationConstants.WASTE * 33.5F, false),
                rbmk("rbmk_fuel_hea242", "rbmk_pellet_hea242", RadiationConstants.AM242, RadiationConstants.WASTE * 34.0F, false),
                rbmk("rbmk_fuel_men", "rbmk_pellet_men", RadiationConstants.NP_FUEL, RadiationConstants.WASTE * 22.5F, false),
                rbmk("rbmk_fuel_hen", "rbmk_pellet_hen", RadiationConstants.NP237, RadiationConstants.WASTE * 30.0F, false),
                rbmk("rbmk_fuel_mox", "rbmk_pellet_mox", RadiationConstants.MOX_FUEL, RadiationConstants.WASTE * 25.5F, false),
                rbmk("rbmk_fuel_les", "rbmk_pellet_les", RadiationConstants.SA_FUEL, RadiationConstants.WASTE * 24.5F, false),
                rbmk("rbmk_fuel_mes", "rbmk_pellet_mes", RadiationConstants.SA_FUEL, RadiationConstants.WASTE * 30.0F, false),
                rbmk("rbmk_fuel_hes", "rbmk_pellet_hes", RadiationConstants.SA_FUEL, RadiationConstants.WASTE * 50.0F, false),
                rbmk("rbmk_fuel_leaus", "rbmk_pellet_leaus", 0.0F, RadiationConstants.WASTE * 37.5F, false),
                rbmk("rbmk_fuel_heaus", "rbmk_pellet_heaus", 0.0F, RadiationConstants.WASTE * 32.5F, false),
                rbmk("rbmk_fuel_po210be", "rbmk_pellet_po210be", RadiationConstants.PO210_BE, RadiationConstants.PO210_BE * 0.1F, true),
                rbmk("rbmk_fuel_ra226be", "rbmk_pellet_ra226be", RadiationConstants.RA226_BE, RadiationConstants.RA226_BE * 0.4F, true),
                new LegacyRbmkHazard("rbmk_fuel_pu238be", "rbmk_pellet_pu238be",
                        RadiationConstants.PU238_BE * rod, RadiationConstants.WASTE * rod * 2.5F, false, 0.0F, 0.0F,
                        RadiationConstants.PU238_BE * pellet, RadiationConstants.WASTE * 1.5F, 0.0F, 0.0F),
                rbmk("rbmk_fuel_balefire_gold", "rbmk_pellet_balefire_gold", RadiationConstants.AU198, RadiationConstants.BALEFIRE * 0.5F, true),
                new LegacyRbmkHazard("rbmk_fuel_flashlead", "rbmk_pellet_flashlead",
                        RadiationConstants.PB209 * 1.25F * rod, RadiationConstants.PB209 * RadiationConstants.NUGGET * 0.05F * rod, true, 0.0F, 0.0F,
                        RadiationConstants.PB209 * 1.25F * pellet, RadiationConstants.PB209 * RadiationConstants.NUGGET * 0.05F, 0.0F, 0.0F),
                rbmk("rbmk_fuel_balefire", "rbmk_pellet_balefire", RadiationConstants.BALEFIRE, RadiationConstants.BALEFIRE * 100.0F, true),
                new LegacyRbmkHazard("rbmk_fuel_zfb_bismuth", "rbmk_pellet_zfb_bismuth",
                        RadiationConstants.PU241 * rod * 0.1F, RadiationConstants.WASTE * rod * 5.0F, false, 0.0F, 0.0F,
                        RadiationConstants.PU241 * pellet * 0.1F, RadiationConstants.WASTE * pellet * 5.0F, 0.0F, 0.0F),
                new LegacyRbmkHazard("rbmk_fuel_zfb_pu241", "rbmk_pellet_zfb_pu241",
                        RadiationConstants.PU239 * rod * 0.1F, RadiationConstants.WASTE * rod * 7.5F, false, 0.0F, 0.0F,
                        RadiationConstants.PU239 * pellet * 0.1F, RadiationConstants.WASTE * pellet * 7.5F, 0.0F, 0.0F),
                new LegacyRbmkHazard("rbmk_fuel_zfb_am_mix", "rbmk_pellet_zfb_am_mix",
                        RadiationConstants.PU241 * rod * 0.1F, RadiationConstants.WASTE * rod * 10.0F, false, 0.0F, 0.0F,
                        RadiationConstants.PU241 * pellet * 0.1F, RadiationConstants.WASTE * pellet * 10.0F, 0.0F, 0.0F),
                rbmk("rbmk_fuel_drx", "rbmk_pellet_drx", RadiationConstants.BALEFIRE, RadiationConstants.BALEFIRE * 100.0F, true, 0.0F, 1.0F / 3.0F, 1.0F / 24.0F)
        );
    }

    private static LegacyRbmkHazard rbmk(String rodId, String pelletId, float base, float depleted, boolean linear) {
        return rbmk(rodId, pelletId, base, depleted, linear, 0.0F, 0.0F, 0.0F);
    }

    private static LegacyRbmkHazard rbmk(String rodId, String pelletId, float base, float depleted, boolean linear, float rodBlinding, float rodDigamma, float pelletDigamma) {
        return new LegacyRbmkHazard(rodId, pelletId,
                base * RadiationConstants.ROD_RBMK,
                depleted * RadiationConstants.ROD_RBMK,
                linear,
                rodBlinding,
                rodDigamma,
                base * RadiationConstants.BILLET,
                depleted * RadiationConstants.BILLET,
                0.0F,
                pelletDigamma);
    }

    private record LegacyRbmkHazard(
            String rodId,
            String pelletId,
            float rodBase,
            float rodDepleted,
            boolean linear,
            float rodBlinding,
            float rodDigamma,
            float pelletBase,
            float pelletDepleted,
            float pelletBlinding,
            float pelletDigamma) {
    }

    private static void registerLegacyPwrFuelHazards() {
        registerPwrFuel(0, "meu", RadiationConstants.U_FUEL * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(1, "heu233", RadiationConstants.U233 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(2, "heu235", RadiationConstants.U235 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(3, "men", RadiationConstants.NP_FUEL * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(4, "hen237", RadiationConstants.NP237 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(5, "mox", RadiationConstants.MOX_FUEL * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(6, "mep", RadiationConstants.PU_REACTOR_GRADE * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(7, "hep239", RadiationConstants.PU239 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(8, "hep241", RadiationConstants.PU241 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(9, "mea", RadiationConstants.AM_MIX * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(10, "hea242", RadiationConstants.AM242 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(11, "hes326", RadiationConstants.SA326 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(12, "hes327", RadiationConstants.SA327 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(13, "bfb_am_mix", RadiationConstants.AM_MIX * RadiationConstants.BILLET);
        registerPwrFuel(14, "bfb_pu241", RadiationConstants.PU241 * RadiationConstants.BILLET);
    }

    private static void registerPwrFuel(int legacyMeta, String suffix, float base) {
        registerLegacyMeta(LegacyMetaItemMappings.PWR_FUEL, legacyMeta, HazardType.RADIATION, base);
        registerByName("pwr_fuel_" + suffix, HazardType.RADIATION, base);

        HazardData hot = new HazardData()
                .addEntry(HazardType.RADIATION, base * 10.0F)
                .addEntry(HazardType.HOT, 5.0F);
        registerLegacyMeta(LegacyMetaItemMappings.PWR_FUEL_HOT, legacyMeta, hot);
        registerByName("pwr_fuel_hot_" + suffix,
                new HazardEntry(HazardType.RADIATION, base * 10.0F),
                new HazardEntry(HazardType.HOT, 5.0F));

        registerLegacyMeta(LegacyMetaItemMappings.PWR_FUEL_DEPLETED, legacyMeta, HazardType.RADIATION, base * 10.0F);
        registerByName("pwr_fuel_depleted_" + suffix, HazardType.RADIATION, base * 10.0F);
    }

    private static void registerLegacyWatzPelletHazards() {
        float fourIngots = RadiationConstants.INGOT * 4.0F;
        registerWatzPellet(0, "schrabidium", RadiationConstants.SA326 * fourIngots);
        registerWatzPellet(1, "hes", RadiationConstants.SA_FUEL * fourIngots);
        registerWatzPellet(2, "mes", RadiationConstants.SA_FUEL * fourIngots);
        registerWatzPellet(3, "les", RadiationConstants.SA_FUEL * fourIngots);
        registerWatzPellet(4, "hen", RadiationConstants.NP237 * fourIngots);
        registerWatzPellet(5, "meu", RadiationConstants.U_FUEL * fourIngots);
        registerWatzPellet(6, "mep", RadiationConstants.PU_REACTOR_GRADE * fourIngots);
        registerWatzPellet(9, "du", RadiationConstants.U238 * fourIngots);
        registerWatzPellet(10, "nqd", RadiationConstants.U235 * fourIngots);
        registerWatzPellet(11, "nqr", RadiationConstants.PU239 * fourIngots);
    }

    private static void registerWatzPellet(int legacyMeta, String suffix, float radiation) {
        registerLegacyMeta(LegacyMetaItemMappings.WATZ_PELLET, legacyMeta, HazardType.RADIATION, radiation);
        registerByName("watz_pellet_" + suffix, HazardType.RADIATION, radiation);
    }

    private static void registerMaterialPowderHazards() {
        registerByName("powder_lithium", HazardType.HYDROACTIVE, RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_lithium_tiny", HazardType.HYDROACTIVE, RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_sodium", HazardType.HYDROACTIVE, RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_asbestos", HazardType.ASBESTOS, RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_actinium", HazardType.RADIATION, RadiationConstants.AC227 * RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_actinium_tiny", HazardType.RADIATION, RadiationConstants.AC227 * RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);
        registerByName("powder_schrabidium",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SA326 * RadiationConstants.POWDER_MULTIPLIER),
                new HazardEntry(HazardType.BLINDING, 50.0F * RadiationConstants.POWDER_MULTIPLIER));
        registerByName("powder_schrabidate",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SCHRARANIUM * RadiationConstants.POWDER_MULTIPLIER),
                new HazardEntry(HazardType.BLINDING, 50.0F * RadiationConstants.POWDER_MULTIPLIER));
    }

    private static void registerRad(String itemName, float level) {
        registerByName(itemName, HazardType.RADIATION, level);
    }

    private static void registerNugget(String itemName, float baseLevel) {
        registerRad(itemName, baseLevel * RadiationConstants.NUGGET);
    }

    private static void registerBillet(String itemName, float baseLevel) {
        registerRad(itemName, baseLevel * RadiationConstants.BILLET);
    }

    private static void registerCrystalByName(String itemName, HazardType type, float baseLevel) {
        registerByName(itemName, type, baseLevel * RadiationConstants.BLOCK);
    }

    private static void registerPowderByName(String itemName, float radiation, float hot, float hydroactive) {
        registerSizedPowderByName(itemName, RadiationConstants.POWDER_MULTIPLIER, radiation, hot, hydroactive);
    }

    private static void registerTinyPowderByName(String itemName, float radiation, float hot, float hydroactive) {
        registerSizedPowderByName(itemName, RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER, radiation, hot, hydroactive);
    }

    private static void registerSizedPowderByName(String itemName, float multiplier, float radiation, float hot, float hydroactive) {
        registerShapeByName(itemName, HazardType.RADIATION, radiation, multiplier);
        registerShapeByName(itemName, HazardType.HOT, hot, multiplier);
        registerShapeByName(itemName, HazardType.HYDROACTIVE, hydroactive, multiplier);
    }

    private static void registerShapeByName(String itemName, HazardType type, float baseLevel, float multiplier) {
        registerByName(itemName, type, baseLevel * multiplier);
    }

    private static void registerBlockShapeByName(String blockName, HazardType type, float baseLevel, float multiplier) {
        registerBlockByName(blockName, type, baseLevel * multiplier);
    }

    private static void registerByName(String itemName, HazardType type, float level) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item != null) {
            register(item.get(), type, level);
        }
    }

    private static void registerBlockRad(String blockName, float level) {
        registerBlockByName(blockName, HazardType.RADIATION, level);
    }

    private static void registerBlockByName(String blockName, HazardType type, float level) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(blockName);
        if (block != null) {
            register(block.get().asItem(), type, level);
        }
    }

    public static void register(Item item, HazardType type, float level) {
        ITEM_HAZARDS.computeIfAbsent(item, key -> new HazardData()).addEntry(type, level);
    }

    public static void register(Item item, HazardData data) {
        ITEM_HAZARDS.put(item, data);
    }

    public static void register(Item item, HazardEntry... entries) {
        HazardData data = new HazardData();
        for (HazardEntry entry : entries) {
            data.addEntry(entry);
        }
        register(item, data);
    }

    public static void registerByName(String itemName, HazardEntry... entries) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item != null) {
            register(item.get(), entries);
        }
    }

    public static void registerBlockByName(String blockName, HazardEntry... entries) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(blockName);
        if (block != null) {
            register(block.get().asItem(), entries);
        }
    }

    public static void registerTag(TagKey<Item> tag, HazardType type, float level) {
        TAG_HAZARDS.computeIfAbsent(tag, key -> new HazardData()).addEntry(type, level);
    }

    public static void registerTag(TagKey<Item> tag, HazardData data) {
        TAG_HAZARDS.put(tag, data);
    }

    public static void registerTag(String namespace, String path, HazardType type, float level) {
        registerTag(TagKey.create(Registries.ITEM, new ResourceLocation(namespace, path)), type, level);
    }

    public static void registerTag(String namespace, String path, HazardData data) {
        registerTag(TagKey.create(Registries.ITEM, new ResourceLocation(namespace, path)), data);
    }

    public static void registerForgeTag(String path, HazardType type, float level) {
        registerTag("forge", path, type, level);
    }

    public static void registerForgeTag(String path, HazardData data) {
        registerTag("forge", path, data);
    }

    private static TagKey<Item> forgeItemTag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("forge", path));
    }

    public static void registerStack(ItemStack stack, HazardType type, float level) {
        if (stack.isEmpty()) {
            return;
        }
        STACK_HAZARDS.computeIfAbsent(HazardStackKey.of(stack), key -> new HazardData()).addEntry(type, level);
    }

    public static void registerStack(ItemStack stack, HazardData data) {
        if (stack.isEmpty()) {
            return;
        }
        STACK_HAZARDS.put(HazardStackKey.of(stack), data);
    }

    public static void registerLegacyStateVariant(Item item, int variant, HazardType type, float level) {
        LEGACY_STATE_VARIANT_HAZARDS.computeIfAbsent(new LegacyStateVariantKey(item, variant), key -> new HazardData())
                .addEntry(type, level);
    }

    private static void registerLegacyStateBlockVariant(String blockName, int variant, HazardType type, float level) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(blockName);
        if (block != null) {
            registerLegacyStateVariant(block.get().asItem(), variant, type, level);
        }
    }

    public static void registerLegacyMeta(ResourceLocation legacyId, int legacyMeta, HazardType type, float level) {
        LegacyMetaItemMappings.stack(legacyId, legacyMeta, 1)
                .ifPresent(stack -> registerStack(stack, type, level));
    }

    public static void registerLegacyMeta(ResourceLocation legacyId, int legacyMeta, HazardData data) {
        LegacyMetaItemMappings.stack(legacyId, legacyMeta, 1)
                .ifPresent(stack -> registerStack(stack, data));
    }

    public static void registerLegacyFuelMeta(ResourceLocation legacyId, int legacyMeta, float base, float target, boolean blinding) {
        LegacyMetaItemMappings.stack(legacyId, legacyMeta, 1)
                .ifPresent(stack -> registerFuelRadiation(stack, base, target, blinding));
    }

    private static void registerBreedingRodRadiation(int legacyMeta, float base) {
        registerLegacyMeta(LegacyMetaItemMappings.ROD, legacyMeta, HazardType.RADIATION, base);
        registerLegacyMeta(LegacyMetaItemMappings.ROD_DUAL, legacyMeta, HazardType.RADIATION, base * RadiationConstants.ROD_DUAL);
        registerLegacyMeta(LegacyMetaItemMappings.ROD_QUAD, legacyMeta, HazardType.RADIATION, base * RadiationConstants.ROD_QUAD);
    }

    public static void registerFuelRadiation(Item item, float base, float target, boolean blinding) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(HazardType.RADIATION, base).withModifier(new FuelRadiationModifier(target)));
        if (blinding) {
            data.addEntry(HazardType.BLINDING, 20.0F);
        }
        register(item, data);
    }

    public static void registerFuelRadiation(ItemStack stack, float base, float target, boolean blinding) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(HazardType.RADIATION, base).withModifier(new FuelRadiationModifier(target)));
        if (blinding) {
            data.addEntry(HazardType.BLINDING, 20.0F);
        }
        registerStack(stack, data);
    }

    public static void registerRtgPellet(Item item, float base, float target, float hot, float blinding) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(HazardType.RADIATION, base).withModifier(new RtgRadiationModifier(target)));
        if (hot > 0.0F) {
            data.addEntry(HazardType.HOT, hot);
        }
        if (blinding > 0.0F) {
            data.addEntry(HazardType.BLINDING, blinding);
        }
        register(item, data);
    }

    private static void registerFuelByName(String itemName, float base, float target, boolean blinding) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item != null) {
            registerFuelRadiation(item.get(), base, target, blinding);
        }
    }

    private static void registerRtgPelletByName(String itemName, float base, float target, float hot, float blinding) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item != null) {
            registerRtgPellet(item.get(), base, target, hot, blinding);
        }
    }

    private static void registerRbmkFuelByName(String itemName, float base, float depleted, boolean hot, boolean linear, float blinding, float digamma, double initialYield) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item != null) {
            registerRbmkFuel(item.get(), base, depleted, hot, linear, blinding, digamma, initialYield);
        }
    }

    private static void registerRbmkPelletByName(String itemName, float base, float depleted, float blinding, float digamma) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item != null) {
            registerRbmkPellet(item.get(), base, depleted, blinding, digamma);
        }
    }

    private static void registerLegacyDepletedFuelWaste(String itemName, float base) {
        registerLegacyWasteVariant(itemName, base * 0.075F, base, true);
    }

    private static void registerLegacyRadSourceWaste(String itemName, float base) {
        registerLegacyWasteVariant(itemName, base, base, true);
    }

    private static void registerLegacyWasteVariant(String itemName, float cold, float hot, boolean hotHazard) {
        RegistryObject<Item> item = ModItems.legacyItem(itemName);
        if (item == null) {
            return;
        }
        registerStack(DepletedFuelItem.stack(item.get(), DepletedFuelItem.COLD_DAMAGE),
                new HazardData().addEntry(HazardType.RADIATION, cold));
        HazardData hotData = new HazardData().addEntry(HazardType.RADIATION, hot);
        if (hotHazard) {
            hotData.addEntry(HazardType.HOT, 5.0F);
        }
        registerStack(DepletedFuelItem.stack(item.get(), DepletedFuelItem.HOT_DAMAGE), hotData);
    }

    public static void registerRbmkFuel(Item item, float base, float depleted, boolean hot, boolean linear, float blinding, float digamma) {
        registerRbmkFuel(item, base, depleted, hot, linear, blinding, digamma, 1.0D);
    }

    public static void registerRbmkFuel(Item item, float base, float depleted, boolean hot, boolean linear, float blinding, float digamma, double initialYield) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(HazardType.RADIATION, base).withModifier(new RbmkRadiationModifier(depleted, linear, initialYield)));
        if (hot) {
            data.addEntry(new HazardEntry(HazardType.HOT, 0.0F).withModifier(new RbmkHotModifier()));
        }
        if (blinding > 0.0F) {
            data.addEntry(HazardType.BLINDING, blinding);
        }
        if (digamma > 0.0F) {
            data.addEntry(HazardType.DIGAMMA, digamma);
        }
        register(item, data);
    }

    public static void registerRbmkPellet(ItemStack stack, int legacyMeta, float base, float depleted, float blinding, float digamma) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(HazardType.RADIATION, base).withModifier(new RbmkPelletRadiationModifier(depleted, legacyMeta)));
        if (blinding > 0.0F) {
            data.addEntry(HazardType.BLINDING, blinding);
        }
        if (digamma > 0.0F) {
            data.addEntry(HazardType.DIGAMMA, digamma);
        }
        registerStack(stack, data);
    }

    public static void registerRbmkPellet(Item item, float base, float depleted, float blinding, float digamma) {
        HazardData data = new HazardData()
                .addEntry(new HazardEntry(HazardType.RADIATION, base).withModifier(new RbmkPelletRadiationModifier(depleted)));
        if (blinding > 0.0F) {
            data.addEntry(HazardType.BLINDING, blinding);
        }
        if (digamma > 0.0F) {
            data.addEntry(HazardType.DIGAMMA, digamma);
        }
        register(item, data);
    }

    public static void registerLegacyRbmkPellet(ResourceLocation legacyId, int legacyMeta, float base, float depleted, float blinding, float digamma) {
        LegacyMetaItemMappings.stack(legacyId, legacyMeta, 1)
                .ifPresent(stack -> registerRbmkPellet(stack, legacyMeta, base, depleted, blinding, digamma));
    }

    public static void blacklist(TagKey<Item> tag) {
        TAG_BLACKLIST.add(tag);
    }

    public static void blacklist(ItemStack stack) {
        if (!stack.isEmpty()) {
            STACK_BLACKLIST.add(HazardStackKey.of(stack));
        }
    }

    public static void blacklistLegacyMeta(ResourceLocation legacyId, int legacyMeta) {
        LegacyMetaItemMappings.stack(legacyId, legacyMeta, 1)
                .ifPresent(HazardRegistry::blacklist);
    }


    public static boolean isHazardTypeDisabled(HazardType type) {
        return switch (type) {
            case ASBESTOS -> RadiationConfig.asbestosHazardDisabled();
            case BLINDING -> RadiationConfig.blindingHazardDisabled();
            case COAL -> RadiationConfig.coalHazardDisabled();
            case EXPLOSIVE -> RadiationConfig.explosiveHazardDisabled();
            case HOT -> RadiationConfig.hotHazardDisabled();
            case HYDROACTIVE -> RadiationConfig.hydroactiveHazardDisabled();
            default -> false;
        };
    }

    public static List<HazardEntry> getHazards(ItemStack stack) {
        if (stack.isEmpty() || isBlacklisted(stack)) {
            return List.of();
        }
        List<HazardData> chronological = new ArrayList<>();
        collectTagData(stack, chronological);
        HazardData itemData = ITEM_HAZARDS.get(stack.getItem());
        if (itemData != null) {
            chronological.add(itemData);
        }
        HazardData stackData = STACK_HAZARDS.get(HazardStackKey.of(stack));
        if (stackData != null) {
            chronological.add(stackData);
        }
        HazardData legacyVariantData = LEGACY_STATE_VARIANT_HAZARDS.get(LegacyStateVariantKey.of(stack));
        if (legacyVariantData != null) {
            chronological.add(legacyVariantData);
        }

        List<HazardEntry> entries = new ArrayList<>();
        for (HazardTransformer transformer : TRANSFORMERS) {
            transformer.transformPre(stack, entries);
        }

        int mutex = 0;
        for (HazardData data : chronological) {
            if (data.overrides()) {
                entries.clear();
            }
            if ((data.mutexBits() & mutex) == 0) {
                entries.addAll(data.entries());
                mutex |= data.mutexBits();
            }
        }

        for (HazardTransformer transformer : TRANSFORMERS) {
            transformer.transformPost(stack, entries);
        }
        return List.copyOf(entries);
    }

    private static void collectTagData(ItemStack stack, List<HazardData> chronological) {
        stack.getTags()
                .map(TAG_HAZARDS::get)
                .filter(data -> data != null)
                .forEach(chronological::add);
    }

    public static boolean isBlacklisted(ItemStack stack) {
        if (STACK_BLACKLIST.contains(HazardStackKey.of(stack))) {
            return true;
        }
        for (TagKey<Item> tag : TAG_BLACKLIST) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    public static float getHazardLevel(ItemStack stack, HazardType type) {
        return getHazardLevel(stack, type, null);
    }

    public static float getHazardLevel(ItemStack stack, HazardType type, net.minecraft.world.entity.LivingEntity holder) {
        for (HazardEntry entry : getHazards(stack)) {
            if (entry.type() == type) {
                return entry.modifiedLevel(stack, holder);
            }
        }
        return 0.0F;
    }

    public static float getTotalHazardLevel(ItemStack stack, HazardType type) {
        return getTotalHazardLevel(stack, type, null);
    }

    public static float getTotalHazardLevel(ItemStack stack, HazardType type, net.minecraft.world.entity.LivingEntity holder) {
        float level = 0.0F;
        for (HazardEntry entry : getHazards(stack)) {
            if (entry.type() == type) {
                level += entry.modifiedLevel(stack, holder);
            }
        }
        return level;
    }

    public static float getStackHazardLevel(ItemStack stack, HazardType type) {
        return getHazardLevel(stack, type) * stack.getCount();
    }

    public static float getStackRadiation(ItemStack stack) {
        return getStackHazardLevel(stack, HazardType.RADIATION);
    }

    private record LegacyMaterialShape(int quantity, List<String> prefixes) {
        private static final float INGOT_QUANTITY = 72.0F;
        private static final List<LegacyMaterialShape> STANDARD_AUTOGEN_SHAPES = List.of(
                new LegacyMaterialShape(8, List.of("nugget", "tiny")),
                new LegacyMaterialShape(8, List.of("bedrockorefragment")),
                new LegacyMaterialShape(8, List.of("dustTiny")),
                new LegacyMaterialShape(9, List.of("wireFine")),
                new LegacyMaterialShape(9, List.of("bolt")),
                new LegacyMaterialShape(48, List.of("billet")),
                new LegacyMaterialShape(72, List.of("ingot")),
                new LegacyMaterialShape(72, List.of("gem")),
                new LegacyMaterialShape(72, List.of("crystal")),
                new LegacyMaterialShape(72, List.of("dust")),
                new LegacyMaterialShape(72, List.of("wireDense")),
                new LegacyMaterialShape(72, List.of("plate")),
                new LegacyMaterialShape(216, List.of("plateTriple")),
                new LegacyMaterialShape(432, List.of("plateSextuple")),
                new LegacyMaterialShape(288, List.of("shell")),
                new LegacyMaterialShape(216, List.of("ntmpipe")),
                new LegacyMaterialShape(648, List.of("block")),
                new LegacyMaterialShape(216, List.of("barrelLight")),
                new LegacyMaterialShape(432, List.of("barrelHeavy")),
                new LegacyMaterialShape(288, List.of("receiverLight")),
                new LegacyMaterialShape(648, List.of("receiverHeavy")),
                new LegacyMaterialShape(288, List.of("gunMechanism")),
                new LegacyMaterialShape(288, List.of("stock")),
                new LegacyMaterialShape(144, List.of("grip"))
        );
    }

    private HazardRegistry() {
    }

    private record HazardStackKey(Item item, int damage) {
        private static HazardStackKey of(ItemStack stack) {
            return new HazardStackKey(stack.getItem(), stack.getDamageValue());
        }
    }

    private record LegacyStateVariantKey(Item item, int variant) {
        private static LegacyStateVariantKey of(ItemStack stack) {
            int variant = stack.getItem() instanceof LegacyStateBlockItem stateItem ? stateItem.getVariant(stack) : 0;
            return new LegacyStateVariantKey(stack.getItem(), variant);
        }
    }
}
