package com.hbm.ntm.datagen;

import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiConsumer;

public class HbmItemPoolLootProvider implements LootTableSubProvider {
    private static final int CIRCUIT_VACUUM_TUBE = 0;
    private static final int CIRCUIT_CAPACITOR = 1;
    private static final int CIRCUIT_PCB = 3;
    private static final int CIRCUIT_CHIP = 5;
    private static final int CIRCUIT_ANALOG = 7;
    private static final int CIRCUIT_BASIC = 8;
    private static final int CIRCUIT_ADVANCED = 9;
    private static final int BATTERY_REDSTONE = 0;
    private static final int BATTERY_LEAD = 1;
    private static final int BATTERY_LITHIUM = 2;
    private static final int WIRE_FINE_COPPER = 2_900;
    private static final int CASING_SMALL = 0;
    private static final int CASING_SMALL_STEEL = 2;
    private static final int CASING_SHOTSHELL = 4;
    private static final int CASING_BUCKSHOT = 5;
    private static final int COKE_PETROLEUM = 2;
    private static final int POWDER_ASH_WOOD = 0;

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(HbmItemPoolIds.backupTable(), pool(
                entry(Items.BREAD, 1, 3, 10),
                entry(Items.STICK, 2, 5, 10),
                entry(ModItems.legacyItem("powder_sawdust"), 2, 5, 5)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_VAULT_RUSTY), pool(
                entry(Items.GOLD_INGOT, 3, 14, 1),
                entry(ModItems.COBALT_INGOT, 4, 12, 1),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CHIP, 3, 6, 1),
                entry(Items.DIAMOND, 1, 2, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_VAULT_STANDARD), pool(
                entry(legacyItem("ingot_desh"), 2, 6, 1),
                entry(Items.DIAMOND, 3, 6, 1),
                entry(legacyItem("powder_yellowcake"), 16, 24, 1),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_VACUUM_TUBE, 12, 16, 1),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CHIP, 2, 6, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_VAULT_REINFORCED), pool(
                entry(legacyItem("ingot_desh"), 6, 16, 1),
                entry(ModItems.SAT_CHIP, 1, 1, 1),
                entry(Items.DIAMOND, 5, 9, 1),
                entry(legacyItem("powder_yellowcake"), 26, 42, 1),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CHIP, 18, 32, 1),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_BASIC, 6, 12, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_VAULT_UNBREAKABLE), pool(
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_ADVANCED, 6, 12, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_METEORITE_TREASURE), pool(
                entry(ModItems.legacyItem("ingot_zirconium"), 1, 16, 10),
                entry(ModItems.legacyItem("ingot_niobium"), 1, 16, 10),
                entry(ModItems.COBALT_INGOT, 1, 16, 10),
                entry(ModItems.legacyItem("ingot_boron"), 1, 16, 10),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_VACUUM_TUBE, 4, 8, 10),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CHIP, 2, 4, 10),
                entry(ModItems.SERUM, 1, 1, 5),
                entry(ModItems.HEART_PIECE, 1, 1, 5)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_GENERIC), pool(
                entry(Items.BREAD, 1, 5, 8),
                entry(Items.IRON_INGOT, 2, 6, 10),
                entry(ModItems.STEEL_INGOT, 2, 5, 7),
                entry(ModItems.BERYLLIUM_INGOT, 1, 2, 4),
                entry(ModItems.TITANIUM_INGOT, 1, 1, 3),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_VACUUM_TUBE, 1, 1, 5),
                meta(LegacyMetaItemMappings.CASING, CASING_SMALL, 4, 10, 3),
                meta(LegacyMetaItemMappings.CASING, CASING_SHOTSHELL, 4, 10, 3),
                meta(LegacyMetaItemMappings.BATTERY_PACK, BATTERY_REDSTONE, 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_ANTENNA), pool(
                entry(ModItems.STEEL_INGOT, 1, 2, 7),
                entry(legacyItem("ingot_red_copper"), 1, 1, 4),
                entry(ModItems.TITANIUM_INGOT, 1, 3, 5),
                entry(legacyItem("wire_fine_mingrade"), 2, 3, 7),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_VACUUM_TUBE, 1, 1, 4),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CAPACITOR, 1, 1, 2),
                meta(LegacyMetaItemMappings.BATTERY_PACK, BATTERY_REDSTONE, 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_EXPENSIVE), pool(
                entry(ModItems.CHLORINE_PINWHEEL, 1, 1, 1),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_VACUUM_TUBE, 1, 1, 4),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_ANALOG, 1, 1, 3),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CHIP, 1, 1, 2),
                meta(LegacyMetaItemMappings.BATTERY_PACK, BATTERY_LITHIUM, 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_NUKE_TRASH), pool(
                entry(legacyItem("nugget_u238"), 3, 12, 5),
                entry(legacyItem("nugget_pu240"), 3, 8, 5),
                entry(legacyItem("nugget_neptunium"), 1, 4, 3),
                entry(ModBlocks.YELLOW_BARREL, 1, 1, 2)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_NUKE_MISC), pool(
                entry(legacyItem("nugget_u235"), 3, 12, 5),
                entry(legacyItem("nugget_pu238"), 3, 12, 5),
                entry(legacyItem("nugget_ra226"), 3, 6, 5),
                entry(ModItems.THORIUM_POWDER, 1, 1, 1),
                entry(legacyItem("powder_neptunium"), 1, 1, 1),
                entry(legacyItem("powder_cobalt"), 1, 1, 1),
                entry(legacyItem("pellet_rtg"), 1, 1, 3),
                entry(ModBlocks.YELLOW_BARREL, 1, 3, 3)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_VERTIBIRD), pool(
                entry(legacyItem("billet_uranium_fuel"), 1, 1, 2),
                entry(legacyItem("ingot_uranium_fuel"), 1, 1, 2)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SPACESHIP), pool(
                meta(LegacyMetaItemMappings.BATTERY_PACK, BATTERY_LEAD, 1, 1, 2),
                entry(ModItems.COPPER_COIL, 2, 16, 5),
                entry(legacyItem("wire_fine_mingrade"), 8, 32, 5),
                entry(legacyItem("powder_niobium"), 1, 1, 1),
                entry(ModBlocks.legacyBlock("block_tungsten"), 3, 8, 5),
                entry(ModBlocks.RED_CABLE, 8, 16, 5)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SAT_MINER), pool(
                entry(ModItems.IRON_POWDER, 3, 3, 10),
                entry(ModItems.TITANIUM_POWDER, 2, 2, 8),
                entry(legacyItem("powder_coal"), 4, 4, 15),
                entry(ModItems.URANIUM_POWDER, 2, 2, 5),
                entry(ModItems.PLUTONIUM_POWDER, 1, 1, 5),
                entry(ModItems.THORIUM_POWDER, 2, 2, 7),
                entry(legacyItem("powder_diamond"), 2, 2, 7),
                entry(Items.REDSTONE, 5, 5, 15),
                entry(ModItems.COPPER_POWDER, 5, 5, 15),
                entry(ModItems.LEAD_POWDER, 3, 3, 10),
                entry(legacyItem("fluorite"), 4, 4, 15),
                entry(legacyItem("powder_lapis"), 4, 4, 10),
                entry(legacyItem("crystal_phosphorus"), 1, 1, 10),
                entry(ModBlocks.legacyBlock("gravel_diamond"), 1, 1, 3),
                entry(legacyItem("crystal_uranium"), 1, 1, 3),
                entry(legacyItem("crystal_plutonium"), 1, 1, 3),
                entry(legacyItem("crystal_trixite"), 1, 1, 1),
                entry(legacyItem("crystal_lithium"), 2, 2, 4)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SAT_LUNAR), pool(
                entry(legacyItem("powder_lithium"), 3, 3, 5),
                entry(ModItems.IRON_POWDER, 3, 3, 5),
                entry(legacyItem("crystal_lithium"), 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_MACHINE_PARTS), pool(
                entry(ModItems.STEEL_PLATE, 1, 5, 5),
                entry(legacyItem("shell_steel"), 1, 3, 3),
                entry(legacyItem("bolt_steel"), 4, 16, 3),
                entry(legacyItem("bolt_tungsten"), 4, 16, 3),
                entry(legacyItem("plate_polymer"), 1, 6, 5),
                entry(ModItems.TUNGSTEN_COIL, 1, 2, 5),
                entry(ModItems.MOTOR, 1, 2, 4),
                entry(ModItems.COPPER_COIL, 1, 3, 4),
                entry(legacyItem("wire_fine_mingrade"), 1, 8, 5),
                meta(LegacyMetaItemMappings.BATTERY_PACK, BATTERY_LEAD, 1, 1, 3),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_VACUUM_TUBE, 1, 2, 4),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_PCB, 1, 3, 5),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CAPACITOR, 1, 1, 3)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_NUKE_FUEL), pool(
                entry(legacyItem("billet_uranium"), 1, 4, 4),
                entry(legacyItem("billet_th232"), 1, 3, 3),
                entry(legacyItem("billet_uranium_fuel"), 1, 3, 5),
                entry(legacyItem("billet_mox_fuel"), 1, 3, 5),
                entry(legacyItem("billet_thorium_fuel"), 1, 3, 3),
                entry(legacyItem("billet_ra226be"), 1, 2, 2),
                entry(legacyItem("billet_beryllium"), 1, 1, 1),
                entry(legacyItem("nugget_u233"), 1, 1, 1),
                entry(legacyItem("nugget_uranium_fuel"), 1, 1, 1),
                entry(legacyItem("ingot_graphite"), 1, 4, 3),
                entry(legacyItem("pile_rod_uranium"), 2, 5, 3),
                entry(legacyItem("pile_rod_source"), 1, 2, 2),
                entry(ModItems.SCREWDRIVER, 1, 1, 2)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SILO), pool(
                meta(LegacyMetaItemMappings.BATTERY_PACK, BATTERY_LEAD, 1, 1, 3)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_OFFICE_TRASH), pool(
                entry(Items.PAPER, 1, 12, 10),
                entry(Items.BOOK, 1, 3, 4)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_FILING_CABINET), pool(
                entry(Items.PAPER, 1, 12, 240),
                entry(Items.BOOK, 1, 3, 90),
                entry(Items.MAP, 1, 1, 50),
                entry(Items.WRITABLE_BOOK, 1, 1, 30),
                entry(ModItems.SCREWDRIVER, 1, 1, 10)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SOLID_FUEL), pool(
                entry(legacyItem("solid_fuel"), 1, 5, 1),
                entry(legacyItem("solid_fuel_presto"), 1, 2, 2),
                meta(LegacyMetaItemMappings.COKE, COKE_PETROLEUM, 1, 3, 1),
                entry(Items.REDSTONE, 1, 3, 1),
                entry(legacyItem("niter"), 1, 3, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_VAULT_LAB), pool(
                entry(ModItems.SCREWDRIVER, 1, 1, 10),
                entry(Items.PAPER, 1, 2, 15),
                entry(Items.GLASS_BOTTLE, 1, 1, 5),
                entry(ModItems.MORNING_GLORY, 1, 1, 1),
                entry(legacyItem("filter_coal"), 1, 1, 5),
                entry(legacyItem("cell_empty"), 1, 1, 5),
                entry(legacyItem("powder_cobalt"), 1, 1, 1),
                entry(legacyItem("ingot_boron"), 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_VAULT_LOCKERS), pool(
                entry(Items.PAPER, 1, 6, 7),
                entry(Items.CLOCK, 1, 1, 3),
                entry(Items.BOOK, 1, 5, 10),
                entry(Items.EXPERIENCE_BOTTLE, 1, 3, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_METEOR_SAFE), pool(
                meta(LegacyMetaItemMappings.STAMP_BOOK, 0, 1, 1, 1),
                meta(LegacyMetaItemMappings.STAMP_BOOK, 1, 1, 1, 1),
                meta(LegacyMetaItemMappings.STAMP_BOOK, 2, 1, 1, 1),
                meta(LegacyMetaItemMappings.STAMP_BOOK, 3, 1, 1, 1),
                meta(LegacyMetaItemMappings.STAMP_BOOK, 4, 1, 1, 1),
                meta(LegacyMetaItemMappings.STAMP_BOOK, 5, 1, 1, 1),
                meta(LegacyMetaItemMappings.STAMP_BOOK, 6, 1, 1, 1),
                meta(LegacyMetaItemMappings.STAMP_BOOK, 7, 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_OIL_RIG), pool(
                entry(ModItems.CANISTER_EMPTY, 4, 16, 10),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_ANALOG, 1, 4, 1),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CAPACITOR, 1, 1, 3)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_RTG), pool(
                entry(legacyItem("pellet_rtg_lead"), 1, 1, 40),
                entry(legacyItem("pellet_rtg_weak"), 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_REPAIR_MATERIALS), pool(
                entry(ModItems.ALUMINIUM_INGOT, 2, 8, 3),
                entry(ModItems.STEEL_INGOT, 1, 12, 4),
                entry(ModItems.ALUMINIUM_PLATE, 5, 12, 3),
                entry(ModItems.IRON_PLATE, 6, 16, 3),
                entry(ModItems.STEEL_PLATE, 2, 12, 2),
                entry(ModItems.TUNGSTEN_INGOT, 1, 2, 1),
                entry(ModBlocks.legacyBlock("deco_aluminium"), 12, 24, 4),
                entry(ModBlocks.legacyBlock("deco_steel"), 5, 12, 2),
                entry(ModBlocks.legacyBlock("block_aluminium"), 1, 2, 1),
                entry(ModBlocks.legacyBlock("block_steel"), 1, 1, 1),
                entry(legacyItem("bolt_steel"), 4, 16, 3),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_VACUUM_TUBE, 1, 2, 4),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_ANALOG, 1, 3, 5),
                meta(LegacyMetaItemMappings.CIRCUIT, CIRCUIT_CAPACITOR, 1, 1, 3)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_PILE_HIVE), pool(
                entry(Items.IRON_INGOT, 1, 3, 10),
                entry(ModItems.STEEL_INGOT, 1, 2, 10),
                entry(ModItems.ALUMINIUM_INGOT, 1, 2, 10),
                entry(Items.EXPERIENCE_BOTTLE, 1, 3, 5)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_PILE_BONES), pool(
                entry(Items.BONE, 1, 1, 10),
                entry(Items.ROTTEN_FLESH, 1, 1, 5),
                entry(ModItems.BIOMASS, 1, 1, 2)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_PILE_MED_PILLS), pool(
                entry(ModItems.RADAWAY, 1, 1, 10),
                entry(ModItems.RADX, 1, 1, 10)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_PILE_MAKESHIFT_PLATES), pool(
                entry(ModItems.STEEL_PLATE, 1, 1, 10)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_PILE_OF_GARBAGE), pool(
                entry(Items.STRING, 1, 1, 15),
                entry(legacyItem("powder_cement"), 1, 6, 40),
                meta(LegacyMetaItemMappings.POWDER_ASH, POWDER_ASH_WOOD, 1, 1, 15),
                entry(ModItems.IRON_PLATE, 1, 2, 15),
                entry(ModItems.LEAD_PLATE, 1, 1, 15),
                entry(legacyItem("fallout"), 1, 2, 15),
                entry(ModItems.TUNGSTEN_COIL, 1, 2, 15),
                entry(ModItems.MOTOR, 1, 1, 5)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_PILE_MECHANICAL), pool(
                entry(ModItems.DEFUSER, 1, 1, 30),
                entry(ModItems.SCREWDRIVER, 1, 1, 30),
                meta(LegacyMetaItemMappings.WIRE_FINE, WIRE_FINE_COPPER, 8, 12, 120),
                entry(ModItems.STEEL_PLATE, 3, 8, 40),
                entry(ModItems.COPPER_PLATE, 2, 5, 40),
                entry(ModItems.COPPER_COIL, 2, 5, 40),
                entry(ModItems.TUNGSTEN_COIL, 2, 5, 40)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_PILE_GEAR), pool(
                entry(ModItems.DEFUSER, 1, 1, 40),
                entry(ModItems.SCREWDRIVER, 1, 1, 30),
                meta(LegacyMetaItemMappings.CASING, CASING_SMALL_STEEL, 1, 4, 30),
                meta(LegacyMetaItemMappings.CASING, CASING_SMALL, 3, 8, 40),
                meta(LegacyMetaItemMappings.CASING, CASING_BUCKSHOT, 3, 8, 40)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SUPPLIES), pool(
                entry(ModItems.GEIGER_COUNTER, 1, 1, 2),
                entry(ModItems.RADAWAY, 1, 5, 10)));
    }

    private static LootTable.Builder pool(LootPoolSingletonContainer.Builder<?>... entries) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
        for (LootPoolSingletonContainer.Builder<?> entry : entries) {
            pool.add(entry);
        }
        return LootTable.lootTable().withPool(pool);
    }

    private static LootPoolSingletonContainer.Builder<?> meta(ResourceLocation legacyId, int legacyMeta, int min, int max, int weight) {
        return entry(LegacyMetaItemMappings.requireItem(legacyId, legacyMeta), min, max, weight);
    }

    private static LootPoolSingletonContainer.Builder<?> entry(RegistryObject<? extends ItemLike> item, int min, int max, int weight) {
        if (item == null) {
            throw new IllegalStateException("Missing migrated item for item pool loot table.");
        }
        return entry(item.get(), min, max, weight);
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int min, int max, int weight) {
        return LootItem.lootTableItem(item)
                .setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private static RegistryObject<Item> legacyItem(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item == null) {
            throw new IllegalStateException("Missing migrated item for item pool loot table: " + name);
        }
        return item;
    }
}
