package com.hbm.ntm.radiation;

import com.hbm.ntm.config.RadiationConfig;
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
    private static final Set<TagKey<Item>> TAG_BLACKLIST = new HashSet<>();
    private static final Set<HazardStackKey> STACK_BLACKLIST = new HashSet<>();
    private static final List<HazardTransformer> TRANSFORMERS = new ArrayList<>();

    public static void registerDefaults() {
        TAG_HAZARDS.clear();
        ITEM_HAZARDS.clear();
        STACK_HAZARDS.clear();
        TAG_BLACKLIST.clear();
        STACK_BLACKLIST.clear();
        TRANSFORMERS.clear();
        registerTransformers();
        registerVanillaHazards();
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
        registerExistingLegacyResourceHazards();

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
    }

    private static void registerTransformers() {
        TRANSFORMERS.add(new NbtRadiationHazardTransformer());
        TRANSFORMERS.add(new ContainerRadiationHazardTransformer());
    }

    private static void registerVanillaHazards() {
        register(net.minecraft.world.item.Items.GUNPOWDER, HazardType.EXPLOSIVE, 1.0F);
        register(net.minecraft.world.item.Items.TNT, HazardType.EXPLOSIVE, 4.0F);
        register(net.minecraft.world.item.Items.PUMPKIN_PIE, HazardType.EXPLOSIVE, 1.0F);
        register(net.minecraft.world.item.Items.COAL, HazardType.COAL, RadiationConstants.POWDER_MULTIPLIER);
        register(net.minecraft.world.item.Items.CHARCOAL, HazardType.COAL, RadiationConstants.POWDER_MULTIPLIER);
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

        registerNugget("nugget_uranium_fuel", RadiationConstants.U_FUEL);
        registerNugget("nugget_thorium_fuel", RadiationConstants.TH_FUEL);
        registerNugget("nugget_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerNugget("nugget_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerNugget("nugget_mox_fuel", RadiationConstants.MOX_FUEL);
        registerNugget("nugget_americium_fuel", RadiationConstants.AM_FUEL);
        registerNugget("nugget_schrabidium_fuel", RadiationConstants.SA_FUEL);
        registerByName("nugget_schrabidium_fuel", HazardType.BLINDING, 5.0F * RadiationConstants.NUGGET);

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
        registerBillet("billet_solinium", RadiationConstants.SA326);
        registerBillet("billet_uranium_fuel", RadiationConstants.U_FUEL);
        registerBillet("billet_thorium_fuel", RadiationConstants.TH_FUEL);
        registerBillet("billet_plutonium_fuel", RadiationConstants.PU_FUEL);
        registerBillet("billet_neptunium_fuel", RadiationConstants.NP_FUEL);
        registerBillet("billet_mox_fuel", RadiationConstants.MOX_FUEL);
        registerBillet("billet_americium_fuel", RadiationConstants.AM_FUEL);
        registerBillet("billet_schrabidium_fuel", RadiationConstants.SA_FUEL);
        registerBillet("billet_hes", RadiationConstants.SA_FUEL);
        registerBillet("billet_po210be", RadiationConstants.PO210 * 3.0F);
        registerBillet("billet_ra226be", RadiationConstants.RA226 * 3.0F);
        registerBillet("billet_pu238be", RadiationConstants.PU238 * 3.0F);
        registerRad("billet_nuclear_waste", RadiationConstants.WASTE * RadiationConstants.BILLET);
        registerByName("billet_schrabidium_fuel", HazardType.BLINDING, 5.0F * RadiationConstants.BILLET);
        registerByName("billet_les", HazardType.BLINDING, 20.0F);
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
        registerBlockRad("block_pu_mix", RadiationConstants.PU * RadiationConstants.BLOCK);
        registerBlockRad("block_plutonium_fuel", RadiationConstants.PU_FUEL * RadiationConstants.BLOCK);
        registerBlockRad("block_trinitite", RadiationConstants.TRINITITE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste", RadiationConstants.WASTE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste_painted", RadiationConstants.WASTE * RadiationConstants.BLOCK);
        registerBlockRad("block_waste_vitrified", RadiationConstants.WASTE_VITRIFIED * RadiationConstants.BLOCK);
        registerBlockRad("ancient_scrap", 150.0F);
        registerBlockRad("block_corium", 150.0F);
        registerBlockRad("block_corium_cobble", 150.0F);
        registerBlockRad("block_schraranium", RadiationConstants.SA326 * RadiationConstants.BLOCK);
        registerBlockRad("block_schrabidium", RadiationConstants.SA326 * RadiationConstants.BLOCK);
        registerBlockRad("block_solinium", RadiationConstants.SA326 * RadiationConstants.BLOCK);
        registerBlockRad("block_schrabidium_fuel", RadiationConstants.SA_FUEL * RadiationConstants.BLOCK);
        registerBlockByName("block_schrabidium_fuel", HazardType.BLINDING, 5.0F * RadiationConstants.BLOCK);
        registerBlockRad("block_ra226", RadiationConstants.RA226 * RadiationConstants.BLOCK);
        registerBlockRad("block_actinium", RadiationConstants.AC227 * RadiationConstants.BLOCK);
    }

    private static void registerExistingLegacyResourceHazards() {
        registerRad("billet_yharonite", 25.0F * RadiationConstants.BILLET);
        registerRad("billet_zfb_bismuth", RadiationConstants.U235 * 0.35F * RadiationConstants.BILLET);
        registerRad("billet_zfb_pu241", RadiationConstants.PU241 * 0.5F * RadiationConstants.BILLET);
        registerRad("billet_zfb_am_mix", RadiationConstants.AM_MIX * 0.5F * RadiationConstants.BILLET);
        registerRad("nugget_cobalt", RadiationConstants.CO60 * RadiationConstants.NUGGET);
        registerRad("ingot_mud", 1.0F);
        registerRad("block_yellowcake", RadiationConstants.U * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER);
        registerRad("block_euphemium", 500_000.0F);
        registerRad("block_euphemium_cluster", 500_000.0F);
        registerRad("block_schrabidium_cluster", RadiationConstants.SA326 * RadiationConstants.BLOCK);
        registerRad("block_white_phosphorus", 1.0F);
        registerRad("block_tritium", 0.1F);
        registerRad("block_fallout", RadiationConstants.U * RadiationConstants.BLOCK * RadiationConstants.POWDER_MULTIPLIER);
        registerRad("ore_gneiss_gas", 1.0F);
        registerRad("ore_asbestos", 1.0F);
        registerRad("ore_gneiss_asbestos", 1.0F);
        registerRad("block_schrabidate", 17.5F * RadiationConstants.BLOCK);
        registerRad("block_red_phosphorus", 1.0F);
        registerRad("block_ra226", RadiationConstants.RA226 * RadiationConstants.BLOCK);
        registerRad("block_actinium", RadiationConstants.AC227 * RadiationConstants.BLOCK);

        registerByName("powder_lignite", HazardType.COAL, RadiationConstants.POWDER_MULTIPLIER);
        registerByName("lignite", HazardType.COAL, RadiationConstants.INGOT);
        registerByName("rocket_fuel", HazardType.EXPLOSIVE, 1.0F);
        registerByName("solid_fuel", HazardType.EXPLOSIVE, 1.0F);
        registerByName("solid_fuel_presto", HazardType.EXPLOSIVE, 2.0F);
        registerByName("solid_fuel_presto_triplet", HazardType.EXPLOSIVE, 6.0F);
        registerByName("ingot_phosphorus", HazardType.HOT, 2.0F);
        registerByName("lithium", HazardType.HYDROACTIVE, 1.0F);
        registerByName("powder_calcium", HazardType.HYDROACTIVE, 1.0F);

        registerBlockByName("block_semtex", HazardType.EXPLOSIVE, 4.0F);
        registerBlockByName("block_c4", HazardType.EXPLOSIVE, 4.0F);
        registerBlockByName("brick_fire", HazardType.HOT, 2.0F);
        registerBlockByName("ore_nether_fire", HazardType.HOT, 2.0F);
        registerBlockByName("block_white_phosphorus", HazardType.HOT, 2.0F);
        registerBlockByName("block_red_phosphorus", HazardType.HOT, 1.0F);
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

    private static void registerRad(String itemName, float level) {
        registerByName(itemName, HazardType.RADIATION, level);
    }

    private static void registerNugget(String itemName, float baseLevel) {
        registerRad(itemName, baseLevel * RadiationConstants.NUGGET);
    }

    private static void registerBillet(String itemName, float baseLevel) {
        registerRad(itemName, baseLevel * RadiationConstants.BILLET);
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
        if (level <= 0.0F || isDisabled(type)) {
            return;
        }
        ITEM_HAZARDS.computeIfAbsent(item, key -> new HazardData()).addEntry(type, level);
    }

    public static void register(Item item, HazardData data) {
        if (data.entries().isEmpty()) {
            return;
        }
        mergeData(ITEM_HAZARDS.computeIfAbsent(item, key -> new HazardData()), data);
    }

    public static void registerTag(TagKey<Item> tag, HazardType type, float level) {
        if (level <= 0.0F || isDisabled(type)) {
            return;
        }
        TAG_HAZARDS.computeIfAbsent(tag, key -> new HazardData()).addEntry(type, level);
    }

    public static void registerTag(String namespace, String path, HazardType type, float level) {
        registerTag(TagKey.create(Registries.ITEM, new ResourceLocation(namespace, path)), type, level);
    }

    public static void registerForgeTag(String path, HazardType type, float level) {
        registerTag("forge", path, type, level);
    }

    public static void registerStack(ItemStack stack, HazardType type, float level) {
        if (stack.isEmpty() || level <= 0.0F || isDisabled(type)) {
            return;
        }
        STACK_HAZARDS.computeIfAbsent(HazardStackKey.of(stack), key -> new HazardData()).addEntry(type, level);
    }

    public static void registerStack(ItemStack stack, HazardData data) {
        if (stack.isEmpty() || data.entries().isEmpty()) {
            return;
        }
        mergeData(STACK_HAZARDS.computeIfAbsent(HazardStackKey.of(stack), key -> new HazardData()), data);
    }

    public static void blacklist(TagKey<Item> tag) {
        TAG_BLACKLIST.add(tag);
    }

    public static void blacklist(ItemStack stack) {
        if (!stack.isEmpty()) {
            STACK_BLACKLIST.add(HazardStackKey.of(stack));
        }
    }

    private static void mergeData(HazardData target, HazardData data) {
        if (data.overrides()) {
            target.entries().clear();
            target.setOverrides(true);
        }
        target.setMutex(target.mutexBits() | data.mutexBits());
        for (HazardEntry entry : data.entries()) {
            if (!isDisabled(entry.type())) {
                target.addEntry(entry);
            }
        }
    }

    private static boolean isDisabled(HazardType type) {
        return switch (type) {
            case ASBESTOS -> RadiationConfig.DISABLE_ASBESTOS.get();
            case BLINDING -> RadiationConfig.DISABLE_BLINDING.get();
            case COAL -> RadiationConfig.DISABLE_COAL.get();
            case EXPLOSIVE -> RadiationConfig.DISABLE_EXPLOSIVE.get();
            case HOT -> RadiationConfig.DISABLE_HOT.get();
            case HYDROACTIVE -> RadiationConfig.DISABLE_HYDROACTIVE.get();
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
        for (Map.Entry<TagKey<Item>, HazardData> entry : TAG_HAZARDS.entrySet()) {
            if (stack.is(entry.getKey())) {
                chronological.add(entry.getValue());
            }
        }
    }

    private static boolean isBlacklisted(ItemStack stack) {
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

    private HazardRegistry() {
    }

    private record HazardStackKey(Item item, int damage) {
        private static HazardStackKey of(ItemStack stack) {
            return new HazardStackKey(stack.getItem(), stack.getDamageValue());
        }
    }
}
