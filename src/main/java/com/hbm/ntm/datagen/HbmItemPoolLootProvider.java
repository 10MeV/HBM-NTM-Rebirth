package com.hbm.ntm.datagen;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.function.BiConsumer;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

/**
 * Datapack source of truth for active legacy ItemPools. Frozen structure/reward pools are not
 * emitted here; ground-refresh meteorite treasure is an explicit non-event exception.
 */
public final class HbmItemPoolLootProvider implements LootTableSubProvider {
    private static final int AMMO_M357_SP = 5;
    private static final int AMMO_M357_FMJ = 6;
    private static final int AMMO_M44_SP = 11;
    private static final int AMMO_M44_FMJ = 12;
    private static final int AMMO_P9_SP = 20;
    private static final int AMMO_P9_FMJ = 21;
    private static final int AMMO_R762_SP = 28;
    private static final int AMMO_G12_BP = 41;
    private static final int AMMO_ROCKET_HE = 58;

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(HbmItemPoolIds.backupTable(), pool(
                entry(Items.BREAD, 1, 3, 10),
                entry(Items.STICK, 2, 5, 10),
                entry(legacyItem("scrap"), 1, 3, 10),
                entry(legacyItem("dust"), 2, 5, 5)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SODA), pool(
                entry(ModItems.BOTTLE_NUKA, 1, 1, 10), entry(ModItems.BOTTLE_CHERRY, 1, 1, 5),
                entry(ModItems.BOTTLE_QUANTUM, 1, 1, 1), entry(ModItems.CAN_BEPIS, 1, 1, 10),
                entry(ModItems.CAN_LUNA, 1, 1, 10), entry(ModItems.CAN_MUG, 1, 1, 10),
                entry(ModItems.CAN_BREEN, 1, 1, 1)));
        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SNACKS), pool(
                entry(ModItems.DEFINITELYFOOD, 1, 1, 10), entry(ModItems.CANNED_BEEF, 1, 1, 5),
                entry(ModItems.CANNED_TUBE, 1, 1, 5), entry(ModItems.TWINKIE, 1, 1, 10),
                entry(ModItems.CHOCOLATE, 1, 1, 10)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SAT_MINER), pool(
                entry(legacyItem("powder_aluminium"), 3, 3, 10), entry(ModItems.IRON_POWDER, 3, 3, 10),
                entry(ModItems.TITANIUM_POWDER, 2, 2, 8), entry(legacyItem("crystal_tungsten"), 2, 2, 7),
                entry(legacyItem("powder_coal"), 4, 4, 15), entry(ModItems.URANIUM_POWDER, 2, 2, 5),
                entry(ModItems.PLUTONIUM_POWDER, 1, 1, 5), entry(ModItems.THORIUM_POWDER, 2, 2, 7),
                entry(legacyItem("powder_desh_mix"), 3, 3, 5), entry(legacyItem("powder_diamond"), 2, 2, 7),
                entry(Items.REDSTONE, 5, 5, 15), entry(legacyItem("powder_nitan_mix"), 2, 2, 5),
                entry(ModItems.POWDER_POWER, 2, 2, 5), entry(ModItems.COPPER_POWDER, 5, 5, 15),
                entry(ModItems.LEAD_POWDER, 3, 3, 10), entry(legacyItem("fluorite"), 4, 4, 15),
                entry(legacyItem("powder_lapis"), 4, 4, 10), entry(legacyItem("crystal_aluminium"), 1, 1, 5),
                entry(legacyItem("crystal_gold"), 1, 1, 5), entry(legacyItem("crystal_phosphorus"), 1, 1, 10),
                entry(ModBlocks.legacyBlock("gravel_diamond"), 1, 1, 3), entry(legacyItem("crystal_uranium"), 1, 1, 3),
                entry(legacyItem("crystal_plutonium"), 1, 1, 3), entry(legacyItem("crystal_trixite"), 1, 1, 1),
                entry(legacyItem("crystal_starmetal"), 1, 1, 1), entry(legacyItem("crystal_lithium"), 2, 2, 4)));
        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SAT_LUNAR), pool(
                entry(ModBlocks.MOON_TURF, 48, 48, 5), entry(ModBlocks.MOON_TURF, 32, 32, 7),
                entry(ModBlocks.MOON_TURF, 16, 16, 5), entry(legacyItem("powder_lithium"), 3, 3, 5),
                entry(ModItems.IRON_POWDER, 3, 3, 5), entry(legacyItem("crystal_iron"), 1, 1, 1),
                entry(legacyItem("crystal_lithium"), 1, 1, 1)));

        // Exact ItemPoolsSingle.POOL_METEORITE_TREASURE entries that have modern counterparts.
        // block_meteor_treasure performs 1..3 independent rolls, matching BlockMeteoriteTreasure.
        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_METEORITE_TREASURE), pool(
                entry(ModItems.COBALT_PICKAXE, 1, 1, 10),
                entry(legacyItem("ingot_zirconium"), 1, 16, 10),
                entry(legacyItem("ingot_niobium"), 1, 16, 10),
                entry(ModItems.COBALT_INGOT, 1, 16, 10),
                entry(legacyItem("ingot_boron"), 1, 16, 10),
                entry(legacyItem("ingot_starmetal"), 1, 1, 5),
                entry(legacyItem("crystal_gold"), 1, 4, 10),
                meta(LegacyMetaItemMappings.CIRCUIT, 0, 4, 8, 10),
                meta(LegacyMetaItemMappings.CIRCUIT, 5, 2, 4, 10),
                entry(ModItems.DEFINITELYFOOD, 16, 32, 25),
                entry(ModItems.PILL_HERBAL, 1, 2, 10), entry(ModItems.SERUM, 1, 1, 5),
                entry(ModItems.HEART_PIECE, 1, 1, 5), entry(ModItems.SCRUMPY, 1, 1, 5),
                entry(ModItems.LAUNCH_CODE_PIECE, 1, 1, 5), entry(ModItems.EGG_GLYPHID, 1, 1, 5),
                entry(legacyItem("gem_alexandrite"), 1, 1, 1), entry(ModItems.BLUEPRINT_FOLDER_DISCOVER, 1, 1, 1)));

        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SUPPLIES), pool(
                entry(ModItems.DEFINITELYFOOD, 3, 10, 25), entry(ModItems.SYRINGE_METAL_STIMPAK, 1, 3, 10),
                entry(ModItems.PILL_IODINE, 1, 2, 2), fluidCanister(HbmFluids.DIESEL, 1, 4, 5),
                entry(ModBlocks.MACHINE_DIESEL, 1, 1, 1),
                entry(ModItems.GEIGER_COUNTER, 1, 1, 2), entry(ModItems.MED_BAG, 1, 1, 3),
                entry(ModItems.RADAWAY, 1, 5, 10)));
        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_WEAPONS), pool(
                entry(ModItems.GUN_LIGHT_REVOLVER, 1, 1, 100), entry(ModItems.GUN_HENRY, 1, 1, 100),
                entry(ModItems.GUN_MARESLEG, 1, 1, 100), entry(ModItems.GUN_GREASEGUN, 1, 1, 100),
                entry(ModItems.GUN_CARBINE, 1, 1, 50), entry(ModItems.GUN_HEAVY_REVOLVER, 1, 1, 50),
                entry(ModItems.GUN_PANZERSCHRECK, 1, 1, 20), entry(ModItems.GUN_DOUBLE_BARREL, 1, 1, 10),
                entry(ModItems.GUN_NI4NI, 1, 1, 1)));
        output.accept(HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_AMMO), pool(
                meta(AMMO_M357_SP, 12, 12, 10), meta(AMMO_M357_FMJ, 6, 6, 10),
                meta(AMMO_M44_SP, 12, 12, 5), meta(AMMO_M44_FMJ, 6, 6, 5),
                meta(AMMO_P9_SP, 12, 12, 10), meta(AMMO_P9_FMJ, 6, 6, 10),
                meta(AMMO_R762_SP, 6, 6, 5), meta(AMMO_G12_BP, 6, 6, 10),
                meta(AMMO_ROCKET_HE, 1, 1, 3), entry(ModItems.AMMO_CONTAINER, 1, 1, 1)));
    }

    private static LootTable.Builder pool(LootPoolSingletonContainer.Builder<?>... entries) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
        for (LootPoolSingletonContainer.Builder<?> entry : entries) pool.add(entry);
        return LootTable.lootTable().withPool(pool);
    }

    private static LootPoolSingletonContainer.Builder<?> meta(int legacyMeta, int min, int max, int weight) {
        return entry(LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.AMMO_STANDARD, legacyMeta), min, max, weight);
    }

    private static LootPoolSingletonContainer.Builder<?> meta(ResourceLocation family, int legacyMeta, int min, int max,
            int weight) {
        return entry(LegacyMetaItemMappings.requireItem(family, legacyMeta), min, max, weight);
    }

    private static LootPoolSingletonContainer.Builder<?> entry(RegistryObject<? extends ItemLike> item, int min, int max, int weight) {
        return entry(item.get(), min, max, weight);
    }

    private static LootPoolSingletonContainer.Builder<?> entry(ItemLike item, int min, int max, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private static LootPoolSingletonContainer.Builder<?> fluidCanister(FluidType type, int min, int max, int weight) {
        CompoundTag tag = new CompoundTag();
        tag.putString("hbm_fluid", type.getName());
        tag.putInt("hbm_fluid_amount", 1_000);
        tag.putInt("hbm_fluid_pressure", 0);
        return entry(ModItems.CANISTER_FULL, min, max, weight).apply(SetNbtFunction.setTag(tag));
    }

    private static RegistryObject<Item> legacyItem(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item == null) throw new IllegalStateException("Missing migrated item for item pool loot table: " + name);
        return item;
    }
}
