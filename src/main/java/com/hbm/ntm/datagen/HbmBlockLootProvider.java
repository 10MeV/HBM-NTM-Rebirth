package com.hbm.ntm.datagen;

import com.hbm.ntm.block.PileGraphiteDrilledBaseBlock;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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
                        && block != ModBlocks.CRATE_STEEL
                        && block != ModBlocks.CRATE_DESH
                        && block != ModBlocks.CRATE_TUNGSTEN
                        && block != ModBlocks.SAFE
                        && block != ModBlocks.MASS_STORAGE)
                .filter(block -> block != ModBlocks.MACHINE_BOILER_OFF
                        && block != ModBlocks.FAN
                        && block != ModBlocks.FILING_CABINET)
                .filter(block -> block != ModBlocks.VENDING_MACHINE)
                .filter(block -> block != ModBlocks.RED_CABLE_BOX)
                .filter(block -> block != ModBlocks.FLUID_DUCT_NEO
                        && block != ModBlocks.FLUID_DUCT_BOX
                        && block != ModBlocks.FLUID_DUCT_EXHAUST)
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
        add(ModBlocks.MACHINE_UF6_TANK.get(), noDrop());
        add(ModBlocks.MACHINE_PUF6_TANK.get(), noDrop());
        add(ModBlocks.MACHINE_REFINERY.get(), noDrop());
        add(ModBlocks.BARREL_PLASTIC.get(), noDrop());
        dropSelf(ModBlocks.BARREL_CORRODED.get());
        dropSelf(ModBlocks.MACHINE_FENSU.get());
        add(ModBlocks.MACHINE_BOILER_OFF.get(), oldBoilerScrapsDrop());
        dropSelf(ModBlocks.FAN.get());
        add(ModBlocks.FILING_CABINET.get(),
                legacyStateVariantDrop(ModBlocks.FILING_CABINET.get(), LegacyFileCabinetBlock.VARIANT, 2));
        add(ModBlocks.RED_CABLE_BOX.get(),
                legacyStateVariantDrop(ModBlocks.RED_CABLE_BOX.get(), RedCableBoxBlock.SIZE, 5));
        add(ModBlocks.FLUID_DUCT_NEO.get(),
                legacyStateVariantDrop(ModBlocks.FLUID_DUCT_NEO.get(), FluidPipeBlock.LEGACY_STYLE,
                        FluidPipeBlock.legacyCreativeStyles()));
        add(ModBlocks.FLUID_DUCT_BOX.get(),
                legacyStateVariantDrop(ModBlocks.FLUID_DUCT_BOX.get(), FluidDuctBoxBlock.LEGACY_METADATA,
                        FluidDuctBoxBlock.boxCreativeMetadata()));
        add(ModBlocks.FLUID_DUCT_EXHAUST.get(),
                legacyStateVariantDrop(ModBlocks.FLUID_DUCT_EXHAUST.get(), FluidDuctBoxBlock.LEGACY_METADATA,
                        FluidDuctBoxBlock.LEGACY_METADATA_COUNT));
        add(ModBlocks.RAD_ABSORBER.get(),
                legacyStateVariantDrop(ModBlocks.RAD_ABSORBER.get(), LegacyRadAbsorberBlock.TIER, 4));
        add(ModBlocks.BARREL_STEEL.get(), noDrop());
        add(ModBlocks.BARREL_TCALLOY.get(), noDrop());
        add(ModBlocks.BARREL_ANTIMATTER.get(), noDrop());
        dropSelf(ModBlocks.MACHINE_MINIRTG.get());
        dropSelf(ModBlocks.MACHINE_POWERRTG.get());
        dropSelf(ModBlocks.FIELD_DISTURBER.get());
        dropSelf(ModBlocks.CAPACITOR_BUS.get());
        add(ModBlocks.MACHINE_RTG_FURNACE.get(), noDrop());
        dropSelf(ModBlocks.MACHINE_BATTERY.get());
        dropSelf(ModBlocks.MACHINE_BATTERY_POTATO.get());
        dropSelf(ModBlocks.MACHINE_LITHIUM_BATTERY.get());
        dropSelf(ModBlocks.MACHINE_SCHRABIDIUM_BATTERY.get());
        dropSelf(ModBlocks.MACHINE_DINEUTRONIUM_BATTERY.get());
        add(ModBlocks.MACHINE_WELL.get(), noDrop());
        add(ModBlocks.MACHINE_PUMPJACK.get(), noDrop());
        add(ModBlocks.MACHINE_FRACKING_TOWER.get(), noDrop());
        add(ModBlocks.CRATE_IRON.get(), noDrop());
        add(ModBlocks.CRATE_STEEL.get(), noDrop());
        add(ModBlocks.CRATE_DESH.get(), noDrop());
        add(ModBlocks.CRATE_TUNGSTEN.get(), noDrop());
        add(ModBlocks.SAFE.get(), noDrop());
        add(ModBlocks.MASS_STORAGE.get(), noDrop());
        add(ModBlocks.VENDING_MACHINE.get(), noDrop());
        dropSelf(ModBlocks.MACHINE_SATLINKER.get());
        dropSelf(ModBlocks.PA_SOURCE.get());
        dropSelf(ModBlocks.PA_BEAMLINE.get());
        dropSelf(ModBlocks.PA_RFC.get());
        dropSelf(ModBlocks.PA_QUADRUPOLE.get());
        dropSelf(ModBlocks.PA_DIPOLE.get());
        dropSelf(ModBlocks.PA_DETECTOR.get());
        dropSelf(ModBlocks.SAT_DOCK.get());
        dropSelf(ModBlocks.SOYUZ_CAPSULE.get());
        add(ModBlocks.SOYUZ_LAUNCHER.get(), noDrop());
        dropSelf(ModBlocks.STRUCT_LAUNCHER.get());
        dropSelf(ModBlocks.STRUCT_SCAFFOLD.get());
        dropSelf(ModBlocks.STRUCT_SOYUZ_CORE.get());
        dropSelf(ModBlocks.LAUNCH_PAD.get());
        dropSelf(ModBlocks.LAUNCH_PAD_LARGE.get());
        dropSelf(ModBlocks.LAUNCH_PAD_RUSTED.get());
        dropSelf(ModBlocks.LAUNCH_TABLE.get());
        dropSelf(ModBlocks.COMPACT_LAUNCHER.get());
        dropSelf(ModBlocks.MACHINE_MISSILE_ASSEMBLY.get());
        add(ModBlocks.OIL_PIPE.get(), noDrop());
        add(ModBlocks.CONVEYOR.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_EXPRESS.get(), conveyorWandDrop("EXPRESS"));
        add(ModBlocks.CONVEYOR_DOUBLE.get(), conveyorWandDrop("DOUBLE"));
        add(ModBlocks.CONVEYOR_TRIPLE.get(), conveyorWandDrop("TRIPLE"));
        add(ModBlocks.CONVEYOR_LIFT.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_CHUTE.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.FLOODLIGHT_BEAM.get(), noDrop());
        dropSelf(ModBlocks.CRANE_EXTRACTOR.get());
        dropSelf(ModBlocks.CRANE_INSERTER.get());
        dropSelf(ModBlocks.CRANE_GRABBER.get());
        dropSelf(ModBlocks.CRANE_ROUTER.get());
        dropSelf(ModBlocks.CRANE_BOXER.get());
        dropSelf(ModBlocks.CRANE_UNBOXER.get());
        dropSelf(ModBlocks.CRANE_PARTITIONER.get());
        dropSelf(ModBlocks.FOUNDRY_MOLD.get());
        dropSelf(ModBlocks.FOUNDRY_BASIN.get());
        dropSelf(ModBlocks.FOUNDRY_CHANNEL.get());
        dropSelf(ModBlocks.FOUNDRY_TANK.get());
        dropSelf(ModBlocks.FOUNDRY_OUTLET.get());
        dropSelf(ModBlocks.FOUNDRY_SLAGTAP.get());
        add(ModBlocks.FOUNDRY_SLAG.get(), noDrop());
        ModBlocks.PYLON_BLOCKS.forEach(block -> dropSelf(block.get()));
        ModBlocks.BLOCK_TAB_BLOCKS.stream()
                .filter(block -> !ModBlocks.CAP_BLOCKS.contains(block))
                .forEach(block -> dropSelf(block.get()));
        addLegacyOreDrops();
        addCapBlockDrops();
        addPileGraphiteDrops();
        add(ModBlocks.WASTE_LEAVES.get(), noDrop());
        add(ModBlocks.LEAVES_LAYER.get(), noDrop());
        add(ModBlocks.BARRICADE.get(), noDrop());
        add(ModBlocks.OIL_SPILL.get(), noDrop());
        add(ModBlocks.WASTE_LOG.get(), wasteLogDrop());
        add(ModBlocks.MUSH_BLOCK.get(), hugeMushDrop());
        add(ModBlocks.MUSH_BLOCK_STEM.get(), hugeMushDrop());
        add(ModBlocks.FROZEN_GRASS.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FROZEN_DIRT.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FROZEN_LOG.get(), block -> snowballStackDrop(2.0F, 4.0F));
        add(ModBlocks.FROZEN_PLANKS.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FIRE_DIGAMMA.get(), noDrop());
        add(ModBlocks.BALEFIRE.get(), noDrop());
        add(ModBlocks.CORIUM_BLOCK.get(), noDrop());
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
        add(ModBlocks.ICF_BLOCK.get(), noDrop());
        add(ModBlocks.PWR_BLOCK.get(), noDrop());
        add(ModBlocks.ZIRNOX_DESTROYED.get(), zirnoxDestroyedDrop());
        add(ModBlocks.SPOTLIGHT_BEAM.get(), noDrop());
        add(ModBlocks.SPOTLIGHT_INCANDESCENT_OFF.get(),
                block -> createSingleItemTable(ModBlocks.legacyBlock("spotlight_incandescent").get()));
        add(ModBlocks.SPOTLIGHT_FLUORO_OFF.get(),
                block -> createSingleItemTable(ModBlocks.legacyBlock("spotlight_fluoro").get()));
        add(ModBlocks.SPOTLIGHT_HALOGEN_OFF.get(),
                block -> createSingleItemTable(ModBlocks.legacyBlock("spotlight_halogen").get()));
        add(ModBlocks.BLOCK_SLAG_BROKEN.get(), block -> createSingleItemTable(ModBlocks.legacyBlock("block_slag").get()));
        add(ModBlocks.GLASS_BORON.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_BORON.get()));
        add(ModBlocks.GLASS_LEAD.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_LEAD.get()));
        add(ModBlocks.GLASS_URANIUM.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_URANIUM.get()));
        add(ModBlocks.GLASS_POLONIUM.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_POLONIUM.get()));
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

    private void addLegacyOreDrops() {
        addLegacyFortuneOreDrop("ore_fluorite", "fluorite", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("ore_niter", "niter", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("ore_sulfur", "sulfur", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("ore_nether_sulfur", "sulfur", 2.0F, 4.0F);
        addLegacySingleOreDrop("ore_lignite", "lignite");
        addLegacySingleOreDrop("ore_cinnebar", "cinnebar");
        addLegacySingleOreDrop("ore_coltan", "fragment_coltan");
        addLegacyFortuneOreDrop("ore_cobalt", "fragment_cobalt", 4.0F, 9.0F);
        addLegacyFortuneOreDrop("ore_nether_cobalt", "fragment_cobalt", 5.0F, 12.0F);
        addLegacyNetherFireOreDrop();
        addLegacyNoSilkFortuneDrop("ore_oil", "oil_tar_crude");
        addLegacySingleOreDrop("block_meteor_cobble", "fragment_meteorite");
    }

    private void addLegacySingleOreDrop(String blockName, String itemName) {
        add(ModBlocks.legacyBlock(blockName).get(), block -> createOreDrop(block, ModItems.legacyItem(itemName).get()));
    }

    private void addLegacyFortuneOreDrop(String blockName, String itemName, float min, float max) {
        add(ModBlocks.legacyBlock(blockName).get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem(itemName).get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    private void addLegacyNoSilkFortuneDrop(String blockName, String itemName) {
        add(ModBlocks.legacyBlock(blockName).get(), block -> LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.legacyItem(itemName).get())
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))
                        .when(ExplosionCondition.survivesExplosion())));
    }

    private void addLegacyNetherFireOreDrop() {
        add(ModBlocks.legacyBlock("ore_nether_fire").get(), block -> createSilkTouchDispatchTable(block,
                AlternativesEntry.alternatives(
                        LootItem.lootTableItem(ModItems.legacyItem("ingot_phosphorus").get())
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                                .when(LootItemRandomChanceCondition.randomChance(0.1F)),
                        LootItem.lootTableItem(ModItems.legacyItem("powder_fire").get())
                                .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))));
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

    private LootTable.Builder oldBoilerScrapsDrop() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("mat", 30);
        tag.putInt("amount", 72);
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(ModItems.FOUNDRY_SCRAPS.get())
                                .apply(SetNbtFunction.setTag(tag))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 6.0F))))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder zirnoxDestroyedDrop() {
        return LootTable.lootTable()
                .withPool(fixedStackPool(ModBlocks.legacyBlock("concrete_smooth").get(), 6.0F))
                .withPool(fixedStackPool(ModItems.legacyItem("pipes_steel").get(), 4.0F))
                .withPool(fixedStackPool(ModBlocks.STEEL_GRATE.get(), 2.0F))
                .withPool(fixedStackPool(ModItems.legacyItem("debris_metal").get(), 6.0F))
                .withPool(fixedStackPool(ModItems.legacyItem("debris_graphite").get(), 2.0F))
                .withPool(fixedStackPool(ModItems.legacyItem("fallout").get(), 4.0F));
    }

    private LootTable.Builder hugeMushDrop() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(ModBlocks.MUSH.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
                                        .when(LootItemRandomChanceCondition.randomChance(0.1F)),
                                LootItem.lootTableItem(ModBlocks.MUSH.get())
                                        .when(LootItemRandomChanceCondition.randomChance(1.0F / 9.0F))))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootPool.Builder fixedStackPool(net.minecraft.world.level.ItemLike item, float count) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))))
                .when(ExplosionCondition.survivesExplosion());
    }

    private LootTable.Builder singleItemDrop(Item item) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(item))
                        .when(ExplosionCondition.survivesExplosion()));
    }

    private LootTable.Builder legacyStateVariantDrop(Block block, IntegerProperty property, int variants) {
        int[] variantValues = new int[variants];
        for (int variant = 0; variant < variants; variant++) {
            variantValues[variant] = variant;
        }
        return legacyStateVariantDrop(block, property, variantValues);
    }

    private LootTable.Builder legacyStateVariantDrop(Block block, IntegerProperty property, int... variantValues) {
        LootTable.Builder table = LootTable.lootTable();
        for (int variant : variantValues) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(LegacyStateBlockItem.TAG_VARIANT, variant);
            table.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(block).apply(SetNbtFunction.setTag(tag)))
                    .when(ExplosionCondition.survivesExplosion())
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(property, variant))));
        }
        return table;
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
