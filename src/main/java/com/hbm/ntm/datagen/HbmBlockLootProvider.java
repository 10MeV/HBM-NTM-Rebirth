package com.hbm.ntm.datagen;

import com.hbm.ntm.block.PileGraphiteDrilledBaseBlock;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class HbmBlockLootProvider extends BlockLootSubProvider {
    public HbmBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        ModBlocks.MACHINE_TAB_BLOCKS.stream()
                .filter(block -> block != ModBlocks.MACHINE_FLUIDTANK
                        && block != ModBlocks.MACHINE_BAT9000
                        && block != ModBlocks.MACHINE_BIGASSTANK)
                .filter(block -> block != ModBlocks.BARREL_PLASTIC
                        && block != ModBlocks.BARREL_STEEL
                        && block != ModBlocks.BARREL_TCALLOY
                        && block != ModBlocks.BARREL_ANTIMATTER)
                .filter(block -> block != ModBlocks.MACHINE_REFINERY
                        && block != ModBlocks.MACHINE_WELL
                        && block != ModBlocks.MACHINE_PUMPJACK
                        && block != ModBlocks.MACHINE_FRACKING_TOWER)
                .filter(block -> block != ModBlocks.CRATE_IRON
                        && block != ModBlocks.CRATE_STEEL)
                .filter(block -> block != ModBlocks.VENDING_MACHINE)
                .forEach(block -> dropSelf(block.get()));
        ModBlocks.TURRET_TAB_BLOCKS.stream()
                .filter(block -> block != ModBlocks.TURRET_HOWARD_DAMAGED
                        && block != ModBlocks.TURRET_SENTRY_DAMAGED)
                .forEach(block -> dropSelf(block.get()));
        add(ModBlocks.TURRET_HOWARD_DAMAGED.get(), noDrop());
        add(ModBlocks.TURRET_SENTRY_DAMAGED.get(), noDrop());
        add(ModBlocks.MACHINE_FLUIDTANK.get(), noDrop());
        add(ModBlocks.MACHINE_BAT9000.get(), noDrop());
        add(ModBlocks.MACHINE_BIGASSTANK.get(), noDrop());
        add(ModBlocks.MACHINE_REFINERY.get(), noDrop());
        add(ModBlocks.BARREL_PLASTIC.get(), noDrop());
        dropSelf(ModBlocks.BARREL_CORRODED.get());
        dropSelf(ModBlocks.MACHINE_FENSU.get());
        add(ModBlocks.BARREL_STEEL.get(), noDrop());
        add(ModBlocks.BARREL_TCALLOY.get(), noDrop());
        add(ModBlocks.BARREL_ANTIMATTER.get(), noDrop());
        add(ModBlocks.MACHINE_WELL.get(), noDrop());
        add(ModBlocks.MACHINE_PUMPJACK.get(), noDrop());
        add(ModBlocks.MACHINE_FRACKING_TOWER.get(), noDrop());
        add(ModBlocks.CRATE_IRON.get(), noDrop());
        add(ModBlocks.CRATE_STEEL.get(), noDrop());
        add(ModBlocks.VENDING_MACHINE.get(), noDrop());
        dropSelf(ModBlocks.MACHINE_SATLINKER.get());
        dropSelf(ModBlocks.SAT_DOCK.get());
        dropSelf(ModBlocks.SOYUZ_CAPSULE.get());
        dropSelf(ModBlocks.SOYUZ_LAUNCHER.get());
        dropSelf(ModBlocks.LAUNCH_PAD.get());
        add(ModBlocks.OIL_PIPE.get(), noDrop());
        add(ModBlocks.CONVEYOR.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_EXPRESS.get(), conveyorWandDrop("EXPRESS"));
        add(ModBlocks.CONVEYOR_DOUBLE.get(), conveyorWandDrop("DOUBLE"));
        add(ModBlocks.CONVEYOR_TRIPLE.get(), conveyorWandDrop("TRIPLE"));
        add(ModBlocks.CONVEYOR_LIFT.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_CHUTE.get(), conveyorWandDrop("REGULAR"));
        ModBlocks.PYLON_BLOCKS.forEach(block -> dropSelf(block.get()));
        ModBlocks.BLOCK_TAB_BLOCKS.stream()
                .filter(block -> !ModBlocks.CAP_BLOCKS.contains(block))
                .forEach(block -> dropSelf(block.get()));
        addCapBlockDrops();
        addPileGraphiteDrops();
        add(ModBlocks.WASTE_LEAVES.get(), noDrop());
        add(ModBlocks.LEAVES_LAYER.get(), noDrop());
        add(ModBlocks.WASTE_LOG.get(), wasteLogDrop());
        add(ModBlocks.FROZEN_GRASS.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FROZEN_DIRT.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FROZEN_LOG.get(), block -> snowballStackDrop(2.0F, 4.0F));
        add(ModBlocks.FROZEN_PLANKS.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FIRE_DIGAMMA.get(), noDrop());
        add(ModBlocks.BALEFIRE.get(), noDrop());
        add(ModBlocks.MUD_BLOCK.get(), noDrop());
        add(ModBlocks.TAINT.get(), noDrop());
        ModBlocks.NUKE_TAB_BLOCKS.stream()
                .filter(block -> block != ModBlocks.CHARGE_DYNAMITE
                        && block != ModBlocks.CHARGE_MINER
                        && block != ModBlocks.CHARGE_C4
                        && block != ModBlocks.CHARGE_SEMTEX
                        && block != ModBlocks.VOLCANO_CORE
                        && block != ModBlocks.VOLCANO_RAD_CORE)
                .forEach(block -> dropSelf(block.get()));
        add(ModBlocks.VOLCANO_CORE.get(), noDrop());
        add(ModBlocks.VOLCANO_RAD_CORE.get(), noDrop());
        add(ModBlocks.CHARGE_DYNAMITE.get(), noDrop());
        add(ModBlocks.CHARGE_MINER.get(), noDrop());
        add(ModBlocks.CHARGE_C4.get(), noDrop());
        add(ModBlocks.CHARGE_SEMTEX.get(), noDrop());
        add(ModBlocks.GAS_RADON.get(), noDrop());
        add(ModBlocks.GAS_RADON_DENSE.get(), noDrop());
        add(ModBlocks.GAS_RADON_TOMB.get(), noDrop());
        add(ModBlocks.GAS_MELTDOWN.get(), noDrop());
        add(ModBlocks.GAS_MONOXIDE.get(), noDrop());
        add(ModBlocks.GAS_ASBESTOS.get(), noDrop());
        add(ModBlocks.GAS_COAL.get(), noDrop());
        add(ModBlocks.CHLORINE_GAS.get(), noDrop());
        add(ModBlocks.TOXIC_BLOCK.get(), noDrop());
        add(ModBlocks.DUMMY_BLOCK.get(), noDrop());
        add(ModBlocks.GLASS_BORON.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_BORON.get()));
        add(ModBlocks.GLASS_LEAD.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_LEAD.get()));
        add(ModBlocks.GLASS_QUARTZ.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_QUARTZ.get()));
        dropSelf(ModBlocks.SAND_BORON.get());
        dropSelf(ModBlocks.SAND_LEAD.get());
        dropSelf(ModBlocks.SAND_URANIUM.get());
        dropSelf(ModBlocks.SAND_POLONIUM.get());
        dropSelf(ModBlocks.SAND_QUARTZ.get());
        add(ModBlocks.GLASS_TRINITITE.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_TRINITITE.get()));
        add(ModBlocks.FALLOUT.get(), block -> createSingleItemTable(ModItems.legacyItem("fallout").get()));
        add(ModBlocks.WASTE_TRINITITE.get(), block -> singleItemDrop(ModItems.legacyItem("trinitite").get()));
        add(ModBlocks.WASTE_TRINITITE_RED.get(), block -> singleItemDrop(ModItems.legacyItem("trinitite").get()));
        add(ModBlocks.TEKTITE.get(), block -> createSingleItemTable(ModBlocks.TEKTITE.get()));
        add(ModBlocks.ORE_TEKTITE_OSMIRIDIUM.get(), block -> singleItemDrop(ModItems.legacyItem("powder_tektite").get()));
        add(ModBlocks.ORE_SELLAFIELD_DIAMOND.get(), block -> singleItemDrop(Items.DIAMOND));
        add(ModBlocks.ORE_SELLAFIELD_EMERALD.get(), block -> singleItemDrop(Items.EMERALD));
        add(ModBlocks.ORE_SELLAFIELD_RADGEM.get(), block -> singleItemDrop(ModItems.legacyItem("gem_rad").get()));
    }

    private void addPileGraphiteDrops() {
        add(ModBlocks.BLOCK_GRAPHITE_DRILLED.get(), pileGraphiteDrop(ModBlocks.BLOCK_GRAPHITE_DRILLED.get()));
        add(ModBlocks.BLOCK_GRAPHITE_FUEL.get(), pileGraphiteFuelDrop());
        add(ModBlocks.BLOCK_GRAPHITE_PLUTONIUM.get(), pileGraphiteDrop(ModBlocks.BLOCK_GRAPHITE_PLUTONIUM.get(), "pile_rod_plutonium"));
        add(ModBlocks.BLOCK_GRAPHITE_ROD.get(), pileGraphiteDrop(ModBlocks.BLOCK_GRAPHITE_ROD.get(), "pile_rod_boron"));
        add(ModBlocks.BLOCK_GRAPHITE_SOURCE.get(), pileGraphiteDrop(ModBlocks.BLOCK_GRAPHITE_SOURCE.get(), "pile_rod_source"));
        add(ModBlocks.BLOCK_GRAPHITE_LITHIUM.get(), pileGraphiteDrop(ModBlocks.BLOCK_GRAPHITE_LITHIUM.get(), "pile_rod_lithium"));
        add(ModBlocks.BLOCK_GRAPHITE_TRITIUM.get(), pileGraphiteDrop(ModBlocks.BLOCK_GRAPHITE_TRITIUM.get(), "cell_tritium"));
        add(ModBlocks.BLOCK_GRAPHITE_DETECTOR.get(), pileGraphiteDrop(ModBlocks.BLOCK_GRAPHITE_DETECTOR.get(), "pile_rod_detector"));
    }

    private void addCapBlockDrops() {
        add(ModBlocks.BLOCK_CAP_NUKA.get(), block -> stackDrop(ModItems.CAP_NUKA.get(), 128.0F));
        add(ModBlocks.BLOCK_CAP_QUANTUM.get(), block -> stackDrop(ModItems.CAP_QUANTUM.get(), 128.0F));
        add(ModBlocks.BLOCK_CAP_SPARKLE.get(), block -> stackDrop(ModItems.CAP_SPARKLE.get(), 128.0F));
        add(ModBlocks.BLOCK_CAP_RAD.get(), block -> stackDrop(ModItems.CAP_RAD.get(), 128.0F));
        add(ModBlocks.BLOCK_CAP_KORL.get(), block -> stackDrop(ModItems.CAP_KORL.get(), 128.0F));
        add(ModBlocks.BLOCK_CAP_FRITZ.get(), block -> stackDrop(ModItems.CAP_FRITZ.get(), 128.0F));
    }

    private LootTable.Builder pileGraphiteDrop(Block block) {
        return pileGraphiteBaseDrop(block);
    }

    private LootTable.Builder pileGraphiteDrop(Block block, String insertedLegacyItem) {
        return pileGraphiteBaseDrop(block)
                .withPool(singleSurvivingItemPool(ModItems.legacyItem(insertedLegacyItem).get()));
    }

    private LootTable.Builder pileGraphiteFuelDrop() {
        Block block = ModBlocks.BLOCK_GRAPHITE_FUEL.get();
        return pileGraphiteBaseDrop(block)
                .withPool(singleSurvivingItemPool(ModItems.legacyItem("pile_rod_uranium").get())
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(PileGraphiteDrilledBaseBlock.ACTIVE, false))))
                .withPool(singleSurvivingItemPool(ModItems.legacyItem("pile_rod_pu239").get())
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(PileGraphiteDrilledBaseBlock.ACTIVE, true))));
    }

    private LootTable.Builder pileGraphiteBaseDrop(Block block) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.legacyItem("ingot_graphite").get())
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(8.0F))))
                        .when(ExplosionCondition.survivesExplosion()))
                .withPool(singleSurvivingItemPool(ModItems.legacyItem("shell_aluminium").get())
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(PileGraphiteDrilledBaseBlock.ALUMINUM, true))));
    }

    private LootPool.Builder singleSurvivingItemPool(Item item) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item))
                .when(ExplosionCondition.survivesExplosion());
    }

    @SuppressWarnings("deprecation")
    private LootTable.Builder conveyorWandDrop(String type) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type);
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.CONVEYOR_WAND.get())
                                .apply(SetNbtFunction.setTag(tag)))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder wasteLogDrop() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.CHARCOAL)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder singleItemDrop(Item item) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(item))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder stackDrop(Item item, float count) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder snowballStackDrop(float min, float max) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.SNOWBALL)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
