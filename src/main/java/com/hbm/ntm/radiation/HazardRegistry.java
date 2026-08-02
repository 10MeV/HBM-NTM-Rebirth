package com.hbm.ntm.radiation;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.Mk2PileRodItem;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
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
    private static final int LEGACY_ORE_DICT_MUTEX = 0b1;
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
        com.hbm.hazard.HazardSystem.clearLegacyMirrors();
        registerTransformers();
        registerVanillaHazards();
        registerLegacyTagHazards();
        registerLegacyTagBlacklists();
        registerLegacyExplosiveComponentHazards();

        registerRadioactiveParts();
        registerRadioactiveBlocks();
        registerExistingLegacyResourceHazards();
        registerLegacyWasteAndCrystalHazards();
        registerLegacyNukePartHazards();
        registerLegacyHolotapeHazards();
        registerLegacyDemonCoreHazards();
        registerLegacyReactorComponentHazards();
        registerLegacyBreedingRodHazards();
        registerLegacyBalefireAndReactorDebrisHazards();

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

    public static void registerDefaultTransformers() {
        TRANSFORMERS.clear();
        com.hbm.hazard.HazardSystem.trafos.clear();
        com.hbm.hazard.HazardSystem.trafos.add(new com.hbm.hazard.transformer.HazardTransformerRadiationNBT());
        // LBSM hazard bypasses are intentionally not modernized; keep legacy standard-mode behavior.
        com.hbm.hazard.HazardSystem.trafos.add(new com.hbm.hazard.transformer.HazardTransformerRadiationContainer());
        // AE2 compatibility is frozen; retain its legacy transformer facade without activating it.
    }

    private static void registerTransformers() {
        registerDefaultTransformers();
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
        registerForgeTag("lithium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.INGOT)));
        registerForgeTag("ingots/lithium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.INGOT)));
        registerForgeTag("dusts/lithium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.POWDER_MULTIPLIER)));
        registerForgeTag("tiny_dusts/lithium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER)));
        registerForgeTag("storage_blocks/lithium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.BLOCK)));
        registerForgeTag("ores/lithium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.INGOT)));
        registerForgeTag("asbestos", legacyOreDictData(
                new HazardEntry(HazardType.ASBESTOS, RadiationConstants.INGOT)));
        registerForgeTag("ingots/asbestos", legacyOreDictData(
                new HazardEntry(HazardType.ASBESTOS, RadiationConstants.INGOT)));
        registerForgeTag("dusts/asbestos", legacyOreDictData(
                new HazardEntry(HazardType.ASBESTOS, RadiationConstants.POWDER_MULTIPLIER)));
        registerForgeTag("storage_blocks/asbestos", legacyOreDictData(
                new HazardEntry(HazardType.ASBESTOS, RadiationConstants.BLOCK)));
        registerForgeTag("ingots/sodium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.INGOT)));
        registerForgeTag("ingots/strontium", legacyOreDictData(
                new HazardEntry(HazardType.HOT, RadiationConstants.INGOT),
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.INGOT)));
        registerForgeTag("dusts/sodium", legacyOreDictData(
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.POWDER_MULTIPLIER)));
        registerForgeTag("dusts/strontium", legacyOreDictData(
                new HazardEntry(HazardType.HOT, RadiationConstants.POWDER_MULTIPLIER),
                new HazardEntry(HazardType.HYDROACTIVE, RadiationConstants.POWDER_MULTIPLIER)));
        registerLegacyUraniumTagHazards();
        registerLegacyEnrichedUraniumTagHazards();
        registerLegacyTh232TagHazards();
        registerLegacyPuPurgPu238TagHazards();
        registerLegacyPu239Pu240Pu241TagHazards();
        registerLegacyAmNpPoTcTagHazards();
        registerLegacyRa226Ac227TagHazards();
        registerLegacySr90TagHazards();
        registerLegacyRadiationHotTagHazards("cobalt60", "co60", RadiationConstants.CO60, 1.0F);
        registerLegacyRadiationHotTagHazards("gold198", "au198", RadiationConstants.AU198, 5.0F);
        registerLegacyBareIngotRadiationHotTagHazards(RadiationConstants.CO60, 1.0F, "cobalt60", "co60");
        registerLegacyBareIngotRadiationHotTagHazards(RadiationConstants.AU198, 5.0F, "gold198", "au198");
        registerLegacyPb209TagHazards();
        registerLegacySchrabidiumFamilyTagHazards();
        registerLegacySa327Gh336TagHazards();
        registerLegacyWatzMudTagHazards();
        registerLegacyFissionFragmentTagHazards();
        registerLegacyWhitePhosphorusTagHazards();
    }

    private static void registerLegacyUraniumTagHazards() {
        registerLegacyRadiationTagHazard("uranium", RadiationConstants.INGOT, RadiationConstants.U);
        registerLegacyRadiationTagHazard("ingots/uranium", RadiationConstants.INGOT, RadiationConstants.U);
        registerLegacyRadiationTagHazard("nuggets/uranium", RadiationConstants.NUGGET, RadiationConstants.U);
        registerLegacyRadiationTagHazard("billets/uranium", RadiationConstants.BILLET, RadiationConstants.U);
        registerLegacyRadiationTagHazard("dusts/uranium", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.U);
        registerLegacyRadiationTagHazard("storage_blocks/uranium", RadiationConstants.BLOCK, RadiationConstants.U);
        registerLegacyRadiationTagHazard("ores/uranium", RadiationConstants.INGOT, RadiationConstants.U);

        registerLegacyRadiationTagHazard("ingots/uranium238", RadiationConstants.INGOT, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("ingots/u238", RadiationConstants.INGOT, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("nuggets/uranium238", RadiationConstants.NUGGET, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("nuggets/u238", RadiationConstants.NUGGET, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("billets/uranium238", RadiationConstants.BILLET, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("billets/u238", RadiationConstants.BILLET, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("storage_blocks/uranium238", RadiationConstants.BLOCK, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("storage_blocks/u238", RadiationConstants.BLOCK, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("uranium238", RadiationConstants.INGOT, RadiationConstants.U238);
        registerLegacyRadiationTagHazard("u238", RadiationConstants.INGOT, RadiationConstants.U238);
    }

    private static void registerLegacyEnrichedUraniumTagHazards() {
        registerLegacyMaterialRadiationTagHazards("uranium233", "u233", RadiationConstants.U233);
        registerLegacyMaterialRadiationTagHazards("uranium235", "u235", RadiationConstants.U235);
    }

    private static void registerLegacyMaterialRadiationTagHazards(String primaryAlias, String shortAlias, float radiation) {
        registerLegacyMaterialRadiationAliasTagHazards(primaryAlias, radiation);
        registerLegacyMaterialRadiationAliasTagHazards(shortAlias, radiation);
        registerLegacyBareIngotRadiationTagHazards(radiation, primaryAlias, shortAlias);
    }

    private static void registerLegacyMaterialRadiationAliasTagHazards(String alias, float radiation) {
        registerLegacyRadiationTagHazard("ingots/" + alias, RadiationConstants.INGOT, radiation);
        registerLegacyRadiationTagHazard("nuggets/" + alias, RadiationConstants.NUGGET, radiation);
        registerLegacyRadiationTagHazard("billets/" + alias, RadiationConstants.BILLET, radiation);
        registerLegacyRadiationTagHazard("storage_blocks/" + alias, RadiationConstants.BLOCK, radiation);
    }

    private static void registerLegacyTh232TagHazards() {
        registerLegacyMaterialRadiationTagHazards("thorium232", "th232", RadiationConstants.TH232);
        registerLegacyMaterialRadiationAliasTagHazards("thorium", RadiationConstants.TH232);
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.TH232, "thorium");
        registerLegacyRadiationTagHazard("dusts/thorium232", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.TH232);
        registerLegacyRadiationTagHazard("dusts/th232", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.TH232);
        registerLegacyRadiationTagHazard("dusts/thorium", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.TH232);
        registerLegacyRadiationTagHazard("ores/thorium232", RadiationConstants.INGOT, RadiationConstants.TH232);
        registerLegacyRadiationTagHazard("ores/th232", RadiationConstants.INGOT, RadiationConstants.TH232);
        registerLegacyRadiationTagHazard("ores/thorium", RadiationConstants.INGOT, RadiationConstants.TH232);
    }

    private static void registerLegacyPuPurgPu238TagHazards() {
        registerLegacyRadiationTagHazard("plutonium", RadiationConstants.INGOT, RadiationConstants.PU);
        registerLegacyRadiationTagHazard("ingots/plutonium", RadiationConstants.INGOT, RadiationConstants.PU);
        registerLegacyRadiationTagHazard("nuggets/plutonium", RadiationConstants.NUGGET, RadiationConstants.PU);
        registerLegacyRadiationTagHazard("billets/plutonium", RadiationConstants.BILLET, RadiationConstants.PU);
        registerLegacyRadiationTagHazard("dusts/plutonium", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.PU);
        registerLegacyRadiationTagHazard("storage_blocks/plutonium", RadiationConstants.BLOCK, RadiationConstants.PU);
        registerLegacyRadiationTagHazard("ores/plutonium", RadiationConstants.INGOT, RadiationConstants.PU);
        registerLegacyRadiationTagHazard("ores/nether/plutonium", RadiationConstants.INGOT, RadiationConstants.PU);

        registerLegacyMaterialRadiationAliasTagHazards("plutonium_rg", RadiationConstants.PU_REACTOR_GRADE);
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.PU_REACTOR_GRADE, "plutonium_rg");

        registerLegacyMaterialRadiationHotAliasTagHazards("plutonium238", RadiationConstants.PU238, 3.0F);
        registerLegacyMaterialRadiationHotAliasTagHazards("pu238", RadiationConstants.PU238, 3.0F);
        registerLegacyBareIngotRadiationHotTagHazards(RadiationConstants.PU238, 3.0F, "plutonium238", "pu238");
    }

    private static void registerLegacyPu239Pu240Pu241TagHazards() {
        registerLegacyMaterialRadiationTagHazards("plutonium239", "pu239", RadiationConstants.PU239);
        registerLegacyMaterialRadiationTagHazards("plutonium240", "pu240", RadiationConstants.PU240);
        registerLegacyMaterialRadiationNoBlockTagHazards("plutonium241", "pu241", RadiationConstants.PU241);
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.PU241, "plutonium241", "pu241");
    }

    private static void registerLegacyMaterialRadiationNoBlockTagHazards(String primaryAlias, String shortAlias,
                                                                         float radiation) {
        registerLegacyMaterialRadiationNoBlockAliasTagHazards(primaryAlias, radiation);
        registerLegacyMaterialRadiationNoBlockAliasTagHazards(shortAlias, radiation);
    }

    private static void registerLegacyMaterialRadiationNoBlockAliasTagHazards(String alias, float radiation) {
        registerLegacyRadiationTagHazard("ingots/" + alias, RadiationConstants.INGOT, radiation);
        registerLegacyRadiationTagHazard("nuggets/" + alias, RadiationConstants.NUGGET, radiation);
        registerLegacyRadiationTagHazard("billets/" + alias, RadiationConstants.BILLET, radiation);
    }

    private static void registerLegacyAmNpPoTcTagHazards() {
        registerLegacyMaterialRadiationNoBlockTagHazards("americium241", "am241", RadiationConstants.AM241);
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.AM241, "americium241", "am241");
        registerLegacyMaterialRadiationNoBlockTagHazards("americium242", "am242", RadiationConstants.AM242);
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.AM242, "americium242", "am242");
        registerLegacyMaterialRadiationNoBlockAliasTagHazards("americium_rg", RadiationConstants.AM_MIX);
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.AM_MIX, "americium_rg");

        registerLegacyNp237AliasTagHazards("neptunium237");
        registerLegacyNp237AliasTagHazards("np237");
        registerLegacyNp237AliasTagHazards("neptunium");

        registerLegacyPo210AliasTagHazards("polonium210");
        registerLegacyPo210AliasTagHazards("po210");
        registerLegacyPo210AliasTagHazards("polonium");

        registerLegacyMaterialRadiationNoBlockTagHazards("technetium99", "tc99", RadiationConstants.TC99);
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.TC99, "technetium99", "tc99");
    }

    private static void registerLegacyNp237AliasTagHazards(String alias) {
        registerLegacyMaterialRadiationAliasTagHazards(alias, RadiationConstants.NP237);
        registerLegacyRadiationTagHazard(alias, RadiationConstants.INGOT, RadiationConstants.NP237);
        registerLegacyRadiationTagHazard("dusts/" + alias, RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.NP237);
    }

    private static void registerLegacyPo210AliasTagHazards(String alias) {
        registerLegacyMaterialRadiationHotAliasTagHazards(alias, RadiationConstants.PO210, 3.0F);
        registerLegacyRadiationHotTagHazard(alias, RadiationConstants.INGOT, RadiationConstants.PO210, 3.0F);
        registerLegacyRadiationHotTagHazard("dusts/" + alias, RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.PO210, 3.0F);
    }

    private static void registerLegacyBareIngotRadiationTagHazards(float radiation, String... aliases) {
        for (String alias : aliases) {
            registerLegacyRadiationTagHazard(alias, RadiationConstants.INGOT, radiation);
        }
    }

    private static void registerLegacyMaterialRadiationHotAliasTagHazards(String alias, float radiation, float hot) {
        registerLegacyRadiationHotTagHazard("ingots/" + alias, RadiationConstants.INGOT, radiation, hot);
        registerLegacyRadiationHotTagHazard("nuggets/" + alias, RadiationConstants.NUGGET, radiation, hot);
        registerLegacyRadiationHotTagHazard("billets/" + alias, RadiationConstants.BILLET, radiation, hot);
        registerLegacyRadiationHotTagHazard("storage_blocks/" + alias, RadiationConstants.BLOCK, radiation, hot);
    }

    private static void registerLegacyRa226Ac227TagHazards() {
        registerLegacyRadiationTagHazard("ingots/radium226", RadiationConstants.INGOT, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("ingots/ra226", RadiationConstants.INGOT, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("nuggets/radium226", RadiationConstants.NUGGET, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("nuggets/ra226", RadiationConstants.NUGGET, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("billets/radium226", RadiationConstants.BILLET, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("billets/ra226", RadiationConstants.BILLET, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("dusts/radium226", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("dusts/ra226", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("storage_blocks/radium226", RadiationConstants.BLOCK, RadiationConstants.RA226);
        registerLegacyRadiationTagHazard("storage_blocks/ra226", RadiationConstants.BLOCK, RadiationConstants.RA226);

        registerLegacyRadiationTagHazard("ingots/actinium227", RadiationConstants.INGOT, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("ingots/ac227", RadiationConstants.INGOT, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("nuggets/actinium227", RadiationConstants.NUGGET, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("nuggets/ac227", RadiationConstants.NUGGET, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("billets/actinium227", RadiationConstants.BILLET, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("billets/ac227", RadiationConstants.BILLET, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("dusts/actinium227", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("dusts/ac227", RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("tiny_dusts/actinium227", RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("tiny_dusts/ac227", RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("storage_blocks/actinium227", RadiationConstants.BLOCK, RadiationConstants.AC227);
        registerLegacyRadiationTagHazard("storage_blocks/ac227", RadiationConstants.BLOCK, RadiationConstants.AC227);

        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.RA226, "radium226", "ra226");
        registerLegacyBareIngotRadiationTagHazards(RadiationConstants.AC227, "actinium227", "ac227");
    }

    private static void registerLegacyRadiationTagHazard(String path, float multiplier, float radiation) {
        registerForgeTag(path, legacyOreDictData(
                new HazardEntry(HazardType.RADIATION, radiation * multiplier)));
    }

    private static void registerLegacySr90TagHazards() {
        registerLegacySr90TagHazard("ingots/strontium90", RadiationConstants.INGOT);
        registerLegacySr90TagHazard("ingots/sr90", RadiationConstants.INGOT);
        registerLegacySr90TagHazard("strontium90", RadiationConstants.INGOT);
        registerLegacySr90TagHazard("sr90", RadiationConstants.INGOT);
        registerLegacySr90TagHazard("nuggets/strontium90", RadiationConstants.NUGGET);
        registerLegacySr90TagHazard("nuggets/sr90", RadiationConstants.NUGGET);
        registerLegacySr90TagHazard("billets/strontium90", RadiationConstants.BILLET);
        registerLegacySr90TagHazard("billets/sr90", RadiationConstants.BILLET);
        registerLegacySr90TagHazard("dusts/strontium90", RadiationConstants.POWDER_MULTIPLIER);
        registerLegacySr90TagHazard("dusts/sr90", RadiationConstants.POWDER_MULTIPLIER);
        registerLegacySr90TagHazard("tiny_dusts/strontium90", RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);
        registerLegacySr90TagHazard("tiny_dusts/sr90", RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);
    }

    private static void registerLegacySr90TagHazard(String path, float multiplier) {
        registerForgeTag(path, legacyOreDictData(
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SR90 * multiplier),
                new HazardEntry(HazardType.HOT, multiplier),
                new HazardEntry(HazardType.HYDROACTIVE, multiplier)));
    }

    private static void registerLegacyRadiationHotTagHazards(String primaryAlias, String shortAlias,
                                                             float radiation, float hot) {
        registerLegacyRadiationHotTagHazard("ingots/" + primaryAlias, RadiationConstants.INGOT, radiation, hot);
        registerLegacyRadiationHotTagHazard("ingots/" + shortAlias, RadiationConstants.INGOT, radiation, hot);
        registerLegacyRadiationHotTagHazard("nuggets/" + primaryAlias, RadiationConstants.NUGGET, radiation, hot);
        registerLegacyRadiationHotTagHazard("nuggets/" + shortAlias, RadiationConstants.NUGGET, radiation, hot);
        registerLegacyRadiationHotTagHazard("billets/" + primaryAlias, RadiationConstants.BILLET, radiation, hot);
        registerLegacyRadiationHotTagHazard("billets/" + shortAlias, RadiationConstants.BILLET, radiation, hot);
        registerLegacyRadiationHotTagHazard("dusts/" + primaryAlias, RadiationConstants.POWDER_MULTIPLIER, radiation, hot);
        registerLegacyRadiationHotTagHazard("dusts/" + shortAlias, RadiationConstants.POWDER_MULTIPLIER, radiation, hot);
    }

    private static void registerLegacyBareIngotRadiationHotTagHazards(float radiation, float hot, String... aliases) {
        for (String alias : aliases) {
            registerLegacyRadiationHotTagHazard(alias, RadiationConstants.INGOT, radiation, hot);
        }
    }

    private static void registerLegacyRadiationHotTagHazard(String path, float multiplier, float radiation, float hot) {
        registerForgeTag(path, legacyOreDictData(
                new HazardEntry(HazardType.RADIATION, radiation * multiplier),
                new HazardEntry(HazardType.HOT, hot * multiplier)));
    }

    private static void registerLegacyPb209TagHazards() {
        registerLegacyPb209TagHazard("ingots/lead209", RadiationConstants.INGOT);
        registerLegacyPb209TagHazard("ingots/pb209", RadiationConstants.INGOT);
        registerLegacyPb209TagHazard("nuggets/lead209", RadiationConstants.NUGGET);
        registerLegacyPb209TagHazard("nuggets/pb209", RadiationConstants.NUGGET);
        registerLegacyPb209TagHazard("billets/lead209", RadiationConstants.BILLET);
        registerLegacyPb209TagHazard("billets/pb209", RadiationConstants.BILLET);
        registerLegacyPb209TagHazard("lead209", RadiationConstants.INGOT);
        registerLegacyPb209TagHazard("pb209", RadiationConstants.INGOT);
    }

    private static void registerLegacyPb209TagHazard(String path, float multiplier) {
        registerForgeTag(path, legacyOreDictData(
                new HazardEntry(HazardType.RADIATION, RadiationConstants.PB209 * multiplier),
                new HazardEntry(HazardType.BLINDING, 50.0F * multiplier),
                new HazardEntry(HazardType.HOT, 7.0F * multiplier)));
    }

    private static void registerLegacySchrabidiumFamilyTagHazards() {
        registerLegacySchrabidiumAliasTagHazards("schrabidium");
        registerLegacySchrabidateAliasTagHazards("schrabidate");
        registerLegacySchraraniumAliasTagHazards("schraranium");
    }

    private static void registerLegacySchrabidiumAliasTagHazards(String alias) {
        registerLegacyRadiationBlindingTagHazard("ingots/" + alias, RadiationConstants.INGOT,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("nuggets/" + alias, RadiationConstants.NUGGET,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("billets/" + alias, RadiationConstants.BILLET,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("dusts/" + alias, RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("plates/" + alias, RadiationConstants.INGOT,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("cast_plates/" + alias, RadiationConstants.INGOT * 3.0F,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("storage_blocks/" + alias, RadiationConstants.BLOCK,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("ores/" + alias, RadiationConstants.INGOT,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard("ores/nether/" + alias, RadiationConstants.INGOT,
                RadiationConstants.SA326, 50.0F);
        registerLegacyRadiationBlindingTagHazard(alias, RadiationConstants.INGOT,
                RadiationConstants.SA326, 50.0F);
    }

    private static void registerLegacySchrabidateAliasTagHazards(String alias) {
        registerLegacyRadiationBlindingTagHazard("ingots/" + alias, RadiationConstants.INGOT,
                RadiationConstants.SCHRABIDATE, 50.0F);
        registerLegacyRadiationBlindingTagHazard("dusts/" + alias, RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.SCHRABIDATE, 50.0F);
        registerLegacyRadiationBlindingTagHazard("storage_blocks/" + alias, RadiationConstants.BLOCK,
                RadiationConstants.SCHRABIDATE, 50.0F);
        registerLegacyRadiationBlindingTagHazard(alias, RadiationConstants.INGOT,
                RadiationConstants.SCHRABIDATE, 50.0F);
    }

    private static void registerLegacySchraraniumAliasTagHazards(String alias) {
        registerLegacyRadiationBlindingTagHazard("ingots/" + alias, RadiationConstants.INGOT,
                RadiationConstants.SCHRARANIUM, 50.0F);
        registerLegacyRadiationBlindingTagHazard("storage_blocks/" + alias, RadiationConstants.BLOCK,
                RadiationConstants.SCHRARANIUM, 50.0F);
        registerLegacyRadiationBlindingTagHazard(alias, RadiationConstants.INGOT,
                RadiationConstants.SCHRARANIUM, 50.0F);
    }
    private static void registerLegacySa327Gh336TagHazards() {
        registerLegacySa327AliasTagHazards("solinium");

        registerLegacyGh336AliasTagHazards("ghiorsium336");
        registerLegacyGh336AliasTagHazards("gh336");
    }

    private static void registerLegacyWatzMudTagHazards() {
        registerLegacyRadiationTagHazard("ingots/watzmud", RadiationConstants.INGOT, RadiationConstants.MUD);
        registerLegacyRadiationTagHazard("watzmud", RadiationConstants.INGOT, RadiationConstants.MUD);
    }

    private static void registerLegacyWhitePhosphorusTagHazards() {
        registerForgeTag("ingots/whitephosphorus", legacyOreDictData(
                new HazardEntry(HazardType.HOT, 5.0F * RadiationConstants.INGOT)));
        registerForgeTag("whitephosphorus", legacyOreDictData(
                new HazardEntry(HazardType.HOT, 5.0F * RadiationConstants.INGOT)));
        registerForgeTag("storage_blocks/whitephosphorus", legacyOreDictData(
                new HazardEntry(HazardType.HOT, 5.0F * RadiationConstants.BLOCK)));
    }

    private static void registerLegacyFissionFragmentTagHazards() {
        registerLegacyRadiationHotTagHazard("dusts/iodine131", RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.I131, 1.0F);
        registerLegacyRadiationHotTagHazard("dusts/i131", RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.I131, 1.0F);
        registerLegacyRadiationHotTagHazard("tiny_dusts/iodine131",
                RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.I131, 1.0F);
        registerLegacyRadiationHotTagHazard("tiny_dusts/i131",
                RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.I131, 1.0F);

        registerLegacyRadiationHotTagHazard("dusts/xenon135", RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.XE135, 10.0F);
        registerLegacyRadiationHotTagHazard("dusts/xe135", RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.XE135, 10.0F);
        registerLegacyRadiationHotTagHazard("tiny_dusts/xenon135",
                RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.XE135, 10.0F);
        registerLegacyRadiationHotTagHazard("tiny_dusts/xe135",
                RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER, RadiationConstants.XE135, 10.0F);

        registerLegacyFissionFragmentCesiumTagHazard("dusts/caesium137", RadiationConstants.POWDER_MULTIPLIER);
        registerLegacyFissionFragmentCesiumTagHazard("dusts/cs137", RadiationConstants.POWDER_MULTIPLIER);
        registerLegacyFissionFragmentCesiumTagHazard("tiny_dusts/caesium137",
                RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);
        registerLegacyFissionFragmentCesiumTagHazard("tiny_dusts/cs137",
                RadiationConstants.NUGGET * RadiationConstants.POWDER_MULTIPLIER);

        registerLegacyRadiationHotTagHazard("dusts/astatine209", RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.AT209, 20.0F);
        registerLegacyRadiationHotTagHazard("dusts/at209", RadiationConstants.POWDER_MULTIPLIER,
                RadiationConstants.AT209, 20.0F);
    }

    private static void registerLegacyFissionFragmentCesiumTagHazard(String path, float multiplier) {
        registerForgeTag(path, legacyOreDictData(
                new HazardEntry(HazardType.RADIATION, RadiationConstants.CS137 * multiplier),
                new HazardEntry(HazardType.HOT, 3.0F * multiplier),
                new HazardEntry(HazardType.HYDROACTIVE, 3.0F * multiplier)));
    }

    private static void registerLegacySa327AliasTagHazards(String alias) {
        registerLegacyRadiationBlindingTagHazard("ingots/" + alias, RadiationConstants.INGOT,
                RadiationConstants.SA327, 50.0F);
        registerLegacyRadiationBlindingTagHazard("nuggets/" + alias, RadiationConstants.NUGGET,
                RadiationConstants.SA327, 50.0F);
        registerLegacyRadiationBlindingTagHazard("billets/" + alias, RadiationConstants.BILLET,
                RadiationConstants.SA327, 50.0F);
        registerLegacyRadiationBlindingTagHazard("storage_blocks/" + alias, RadiationConstants.BLOCK,
                RadiationConstants.SA327, 50.0F);
        registerLegacyRadiationBlindingTagHazard(alias, RadiationConstants.INGOT,
                RadiationConstants.SA327, 50.0F);
    }

    private static void registerLegacyGh336AliasTagHazards(String alias) {
        registerLegacyRadiationTagHazard("ingots/" + alias, RadiationConstants.INGOT, RadiationConstants.GH336);
        registerLegacyRadiationTagHazard("nuggets/" + alias, RadiationConstants.NUGGET, RadiationConstants.GH336);
        registerLegacyRadiationTagHazard("billets/" + alias, RadiationConstants.BILLET, RadiationConstants.GH336);
        registerLegacyRadiationTagHazard(alias, RadiationConstants.INGOT, RadiationConstants.GH336);
    }

    private static void registerLegacyRadiationBlindingTagHazard(String path, float multiplier, float radiation,
                                                                 float blinding) {
        registerForgeTag(path, legacyOreDictData(
                new HazardEntry(HazardType.RADIATION, radiation * multiplier),
                new HazardEntry(HazardType.BLINDING, blinding * multiplier)));
    }

    private static void registerLegacyTagBlacklists() {
        blacklist(forgeItemTag("ores/thorium232"));
        blacklist(forgeItemTag("ores/th232"));
        blacklist(forgeItemTag("ores/thorium"));
        blacklist(forgeItemTag("ores/uranium"));
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

        registerRad("ingot_uranium_fuel", RadiationConstants.U_FUEL);
        registerRad("ingot_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerRad("ingot_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerRad("ingot_mox_fuel", RadiationConstants.MOX_FUEL);
        registerRad("ingot_americium_fuel", RadiationConstants.AM_FUEL);
        registerRad("ingot_thorium_fuel", RadiationConstants.TH_FUEL);
        registerByName("ingot_schrabidium_fuel",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SA_FUEL),
                new HazardEntry(HazardType.BLINDING, 5.0F));
        registerRad("ingot_hes", RadiationConstants.SA_FUEL);
        registerRad("ingot_les", RadiationConstants.SA_FUEL);

        registerRad("solid_fuel_bf", 1_000.0F);
        registerRad("solid_fuel_presto_bf", 2_000.0F);
        registerRad("solid_fuel_presto_triplet_bf", 6_000.0F);


        registerNugget("nugget_uranium_fuel", RadiationConstants.U_FUEL);
        registerNugget("nugget_thorium_fuel", RadiationConstants.TH_FUEL);
        registerNugget("nugget_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerNugget("nugget_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerNugget("nugget_mox_fuel", RadiationConstants.MOX_FUEL);
        registerNugget("nugget_americium_fuel", RadiationConstants.AM_FUEL);
        registerByName("nugget_schrabidium_fuel",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SA_FUEL * RadiationConstants.NUGGET),
                new HazardEntry(HazardType.BLINDING, 5.0F * RadiationConstants.NUGGET));
        registerNugget("nugget_hes", RadiationConstants.SA_FUEL);
        registerNugget("nugget_les", RadiationConstants.SA_FUEL);

        registerBillet("billet_uzh", RadiationConstants.UZH);
        registerBillet("billet_uranium_fuel", RadiationConstants.U_FUEL);
        registerBillet("billet_thorium_fuel", RadiationConstants.TH_FUEL);
        registerBillet("billet_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerBillet("billet_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerBillet("billet_mox_fuel", RadiationConstants.MOX_FUEL);
        registerBillet("billet_americium_fuel", RadiationConstants.AM_FUEL);
        registerByName("billet_schrabidium_fuel",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SA_FUEL * RadiationConstants.BILLET),
                new HazardEntry(HazardType.BLINDING, 5.0F * RadiationConstants.BILLET));
        registerBillet("billet_hes", RadiationConstants.SA_FUEL);
        registerBillet("billet_les", RadiationConstants.SA_FUEL);
        registerBillet("billet_po210be", RadiationConstants.PO210_BE);
        registerBillet("billet_ra226be", RadiationConstants.RA226_BE);
        registerBillet("billet_pu238be", RadiationConstants.PU238_BE);
    }

    private static void registerRadioactiveBlocks() {
        registerBlockRad("ore_sellafield_radgem", 25.0F);
        registerSellafieldItemHazards();
        registerBlockRad("waste_trinitite", RadiationConstants.TRINITITE * RadiationConstants.BLOCK);
        registerBlockRad("waste_trinitite_red", RadiationConstants.TRINITITE * RadiationConstants.BLOCK);

        registerBlockRad("block_uranium_fuel", RadiationConstants.U_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_thorium_fuel", RadiationConstants.TH_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_mox_fuel", RadiationConstants.MOX_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_plutonium_fuel", RadiationConstants.PU_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_trinitite", RadiationConstants.TRINITITE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste", RadiationConstants.WASTE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste_painted", RadiationConstants.WASTE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste_vitrified", RadiationConstants.WASTE_VITRIFIED * RadiationConstants.BLOCK);
        registerBlockRad("ancient_scrap", 150.0F);
        registerBlockRad("block_corium", 150.0F);
        registerBlockRad("block_corium_cobble", 150.0F);
        registerBlockByName("block_schrabidium_fuel",
                new HazardEntry(HazardType.RADIATION, RadiationConstants.SA_FUEL * RadiationConstants.BLOCK),
                new HazardEntry(HazardType.BLINDING, 5.0F * RadiationConstants.BLOCK));
    }

    private static void registerSellafieldItemHazards() {
        registerLegacyStateBlockVariant("sellafield", 0, HazardType.RADIATION, 0.5F);
        registerLegacyStateBlockVariant("sellafield", 1, HazardType.RADIATION, 1.0F);
        registerLegacyStateBlockVariant("sellafield", 2, HazardType.RADIATION, 2.5F);
        registerLegacyStateBlockVariant("sellafield", 3, HazardType.RADIATION, 4.0F);
        registerLegacyStateBlockVariant("sellafield", 4, HazardType.RADIATION, 5.0F);
        registerLegacyStateBlockVariant("sellafield", 5, HazardType.RADIATION, 10.0F);
    }

    private static void registerExistingLegacyResourceHazards() {
        registerRad("insert_polonium", 100.0F);
        registerBlockRad("block_yellowcake", RadiationConstants.YELLOWCAKE * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER);
        registerBlockRad("block_fallout", RadiationConstants.YELLOWCAKE * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER);
        registerBlockByName("yellow_barrel", HazardType.RADIATION, RadiationConstants.WASTE * RadiationConstants.INGOT * 10.0F);
        registerBlockByName("ore_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("ore_gneiss_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("stone_resource_asbestos", HazardType.ASBESTOS, 1.0F);
        registerLegacyStateBlockVariant("ore_basalt", 2, HazardType.ASBESTOS, 1.0F);
        registerBlockByName("brick_asbestos", HazardType.ASBESTOS, 1.0F);
        registerBlockByName("tile_lab_broken", HazardType.ASBESTOS, 1.0F);
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
        registerByName("powder_yellowcake", HazardType.RADIATION, RadiationConstants.YELLOWCAKE * RadiationConstants.POWDER_MULTIPLIER);
        registerByName("fallout", HazardType.RADIATION, RadiationConstants.FALLOUT * RadiationConstants.POWDER_MULTIPLIER);
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

        registerMk2PileRodRadiation(Mk2PileRodItem.RodType.RA226BE,
                RadiationConstants.RA226_BE * RadiationConstants.BILLET * 3.0F);
        registerMk2PileRodRadiation(Mk2PileRodItem.RodType.PO210BE,
                RadiationConstants.PO210_BE * RadiationConstants.BILLET * 3.0F);
        // ZR is the legacy non-radioactive carrier variant and deliberately has no hazard entry.
        registerMk2PileRodRadiation(Mk2PileRodItem.RodType.NU,
                RadiationConstants.U * RadiationConstants.BILLET * 3.0F);
        registerMk2PileRodRadiation(Mk2PileRodItem.RodType.PU239,
                RadiationConstants.PU239 * RadiationConstants.BILLET * 3.0F);
        registerMk2PileRodRadiation(Mk2PileRodItem.RodType.RGP,
                RadiationConstants.PU_REACTOR_GRADE * RadiationConstants.BILLET * 3.0F);
        registerMk2PileRodRadiation(Mk2PileRodItem.RodType.WASTE,
                RadiationConstants.WASTE * RadiationConstants.BILLET * 3.0F);

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
        registerPwrFuel(0, RadiationConstants.U_FUEL * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(1, RadiationConstants.U233 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(2, RadiationConstants.U235 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(3, RadiationConstants.NP_FUEL * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(4, RadiationConstants.NP237 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(5, RadiationConstants.MOX_FUEL * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(6, RadiationConstants.PU_REACTOR_GRADE * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(7, RadiationConstants.PU239 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(8, RadiationConstants.PU241 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(9, RadiationConstants.AM_MIX * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(10, RadiationConstants.AM242 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(11, RadiationConstants.SA326 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(12, RadiationConstants.SA327 * RadiationConstants.BILLET * 2.0F);
        registerPwrFuel(13, RadiationConstants.AM_MIX * RadiationConstants.BILLET);
        registerPwrFuel(14, RadiationConstants.PU241 * RadiationConstants.BILLET);
    }

    private static void registerPwrFuel(int legacyMeta, float base) {
        registerLegacyMeta(LegacyMetaItemMappings.PWR_FUEL, legacyMeta, HazardType.RADIATION, base);

        HazardData hot = new HazardData()
                .addEntry(HazardType.RADIATION, base * 10.0F)
                .addEntry(HazardType.HOT, 5.0F);
        registerLegacyMeta(LegacyMetaItemMappings.PWR_FUEL_HOT, legacyMeta, hot);

        registerLegacyMeta(LegacyMetaItemMappings.PWR_FUEL_DEPLETED, legacyMeta, HazardType.RADIATION, base * 10.0F);
    }

    private static void registerLegacyWatzPelletHazards() {
        float fourIngots = RadiationConstants.INGOT * 4.0F;
        registerWatzPellet(0, RadiationConstants.SA326 * fourIngots);
        registerWatzPellet(1, RadiationConstants.SA_FUEL * fourIngots);
        registerWatzPellet(2, RadiationConstants.SA_FUEL * fourIngots);
        registerWatzPellet(3, RadiationConstants.SA_FUEL * fourIngots);
        registerWatzPellet(4, RadiationConstants.NP237 * fourIngots);
        registerWatzPellet(5, RadiationConstants.U_FUEL * fourIngots);
        registerWatzPellet(6, RadiationConstants.PU_REACTOR_GRADE * fourIngots);
        registerWatzPellet(9, RadiationConstants.U238 * fourIngots);
        registerWatzPellet(10, RadiationConstants.U235 * fourIngots);
        registerWatzPellet(11, RadiationConstants.PU239 * fourIngots);
    }

    private static void registerWatzPellet(int legacyMeta, float radiation) {
        registerLegacyMeta(LegacyMetaItemMappings.WATZ_PELLET, legacyMeta, HazardType.RADIATION, radiation);
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
        registerOptionalShapeByName(itemName, HazardType.HOT, hot, multiplier);
        registerOptionalShapeByName(itemName, HazardType.HYDROACTIVE, hydroactive, multiplier);
    }

    private static void registerOptionalShapeByName(String itemName, HazardType type, float baseLevel, float multiplier) {
        if (baseLevel > 0.0F) {
            registerShapeByName(itemName, type, baseLevel, multiplier);
        }
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
        HazardData data = ITEM_HAZARDS.computeIfAbsent(item, key -> new com.hbm.hazard.HazardData());
        data.addEntry(type, level);
        ITEM_HAZARDS.put(item, com.hbm.hazard.HazardSystem.mirrorItem(item, data));
    }

    public static void register(Item item, HazardData data) {
        ITEM_HAZARDS.put(item, com.hbm.hazard.HazardSystem.mirrorItem(item, data));
    }

    public static HazardData remove(Item item) {
        com.hbm.hazard.HazardSystem.unmirrorItem(item);
        return ITEM_HAZARDS.remove(item);
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
        HazardData data = TAG_HAZARDS.computeIfAbsent(tag, key -> new com.hbm.hazard.HazardData());
        data.addEntry(type, level);
        TAG_HAZARDS.put(tag, com.hbm.hazard.HazardSystem.mirrorTag(tag, data));
    }

    public static void registerTag(TagKey<Item> tag, HazardData data) {
        TAG_HAZARDS.put(tag, com.hbm.hazard.HazardSystem.mirrorTag(tag, data));
    }

    public static HazardData removeTag(TagKey<Item> tag) {
        com.hbm.hazard.HazardSystem.unmirrorTag(tag);
        return TAG_HAZARDS.remove(tag);
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

    private static HazardData legacyOreDictData(HazardEntry... entries) {
        HazardData data = new HazardData().setMutex(LEGACY_ORE_DICT_MUTEX);
        for (HazardEntry entry : entries) {
            data.addEntry(entry);
        }
        return data;
    }

    private static TagKey<Item> forgeItemTag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("forge", path));
    }

    public static void registerStack(ItemStack stack, HazardType type, float level) {
        if (stack.isEmpty()) {
            return;
        }
        HazardStackKey key = HazardStackKey.of(stack);
        HazardData data = STACK_HAZARDS.computeIfAbsent(key, ignored -> new com.hbm.hazard.HazardData());
        data.addEntry(type, level);
        STACK_HAZARDS.put(key, com.hbm.hazard.HazardSystem.mirrorStack(stack, data));
    }

    public static void registerStack(ItemStack stack, HazardData data) {
        if (stack.isEmpty()) {
            return;
        }
        STACK_HAZARDS.put(HazardStackKey.of(stack), com.hbm.hazard.HazardSystem.mirrorStack(stack, data));
    }

    public static HazardData removeStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        com.hbm.hazard.HazardSystem.unmirrorStack(stack);
        return STACK_HAZARDS.remove(HazardStackKey.of(stack));
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

    private static void registerMk2PileRodRadiation(Mk2PileRodItem.RodType type, float level) {
        ItemStack stack = new ItemStack(ModItems.PILE_ROD.get());
        stack.setDamageValue(type.ordinal());
        registerStack(stack, HazardType.RADIATION, level);
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
        com.hbm.hazard.HazardSystem.mirrorTagBlacklist(tag);
    }

    public static boolean unblacklist(TagKey<Item> tag) {
        com.hbm.hazard.HazardSystem.unmirrorTagBlacklist(tag);
        return TAG_BLACKLIST.remove(tag);
    }

    public static void blacklist(ItemStack stack) {
        if (!stack.isEmpty()) {
            STACK_BLACKLIST.add(HazardStackKey.of(stack));
            com.hbm.hazard.HazardSystem.mirrorStackBlacklist(stack);
        }
    }

    public static boolean unblacklist(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        com.hbm.hazard.HazardSystem.unmirrorStackBlacklist(stack);
        return STACK_BLACKLIST.remove(HazardStackKey.of(stack));
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
