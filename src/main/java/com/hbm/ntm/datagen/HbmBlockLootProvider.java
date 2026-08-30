package com.hbm.ntm.datagen;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import com.hbm.ntm.block.ConcreteColoredBlock;
import com.hbm.ntm.block.ConcreteColoredExtBlock;
import com.hbm.ntm.block.DecoToasterBlock;
import com.hbm.ntm.block.DecoCrtBlock;
import com.hbm.ntm.block.FluidDuctBoxBlock;
import com.hbm.ntm.block.FluidPipeBlock;
import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.block.LegacyBasaltOreBlock;
import com.hbm.ntm.block.LegacyBiomeStoneBlock;
import com.hbm.ntm.block.PlatemetalBlock;
import com.hbm.ntm.block.LegacyCokeBlock;
import com.hbm.ntm.block.LegacyLightstoneBlock;
import com.hbm.ntm.block.LegacyMultiSlabBlock;
import com.hbm.ntm.block.LegacyRadAbsorberBlock;
import com.hbm.ntm.block.LegacyWoodStructureBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
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
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
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
        ModBlocks.LEGACY_STAIRS.forEach(block -> dropSelf(block.get()));
        add(ModBlocks.LIGHTSTONE.get(), legacyStateVariantDrop(ModBlocks.LIGHTSTONE.get(), LegacyLightstoneBlock.VARIANT, 5));
        add(ModBlocks.CONCRETE_SLAB.get(), legacyStateVariantDrop(ModBlocks.CONCRETE_SLAB.get(), LegacyMultiSlabBlock.VARIANT, 6));
        add(ModBlocks.CONCRETE_BRICK_SLAB.get(), legacyStateVariantDrop(ModBlocks.CONCRETE_BRICK_SLAB.get(), LegacyMultiSlabBlock.VARIANT, 5));
        add(ModBlocks.BRICK_SLAB.get(), legacyStateVariantDrop(ModBlocks.BRICK_SLAB.get(), LegacyMultiSlabBlock.VARIANT, 7));
        add(ModBlocks.STONES_SLAB.get(), legacyStateVariantDrop(ModBlocks.STONES_SLAB.get(), LegacyMultiSlabBlock.VARIANT, 2));
        add(ModBlocks.CONCRETE_DOUBLE_SLAB.get(), legacyDoubleSlabDrop(ModBlocks.CONCRETE_DOUBLE_SLAB.get(), ModBlocks.CONCRETE_SLAB.get(), 6));
        add(ModBlocks.CONCRETE_BRICK_DOUBLE_SLAB.get(), legacyDoubleSlabDrop(ModBlocks.CONCRETE_BRICK_DOUBLE_SLAB.get(), ModBlocks.CONCRETE_BRICK_SLAB.get(), 5));
        add(ModBlocks.BRICK_DOUBLE_SLAB.get(), legacyDoubleSlabDrop(ModBlocks.BRICK_DOUBLE_SLAB.get(), ModBlocks.BRICK_SLAB.get(), 7));
        add(ModBlocks.STONES_DOUBLE_SLAB.get(), legacyDoubleSlabDrop(ModBlocks.STONES_DOUBLE_SLAB.get(), ModBlocks.STONES_SLAB.get(), 2));
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
        addNoDrop(ModBlocks.TURRET_HOWARD_DAMAGED.get());
        addNoDrop(ModBlocks.TURRET_SENTRY_DAMAGED.get());
        dropSelf(ModBlocks.RAIL_NARROW_STRAIGHT.get());
        dropSelf(ModBlocks.RAIL_LARGE_STRAIGHT.get());
        dropSelf(ModBlocks.RAIL_LARGE_STRAIGHT_SHORT.get());
        dropSelf(ModBlocks.RAIL_NARROW_CURVE.get());
        dropSelf(ModBlocks.RAIL_LARGE_CURVE.get());
        dropSelf(ModBlocks.RAIL_LARGE_CURVE_7.get());
        dropSelf(ModBlocks.RAIL_LARGE_CURVE_9.get());
        dropSelf(ModBlocks.RAIL_LARGE_RAMP.get());
        dropSelf(ModBlocks.RAIL_LARGE_BUFFER.get());
        // These two registered blocks are only internal carriers for the
        // 1.7.10 rail multiblock segments. Their owner destroys the rail
        // core, which is the sole block item that may be returned.
        addNoDrop(ModBlocks.RAIL_DUMMY.get());
        addNoDrop(ModBlocks.RAIL_NARROW_DUMMY.get());
        addNoDrop(ModBlocks.DET_MINER.get());
        dropSelf(ModBlocks.RAIL_LARGE_SWITCH.get());
        dropSelf(ModBlocks.RAIL_LARGE_SWITCH_FLIPPED.get());
        dropSelf(ModBlocks.RAIL_WOOD.get());
        dropSelf(ModBlocks.RAIL_NARROW.get());
        dropSelf(ModBlocks.RAIL_HIGHSPEED.get());
        dropSelf(ModBlocks.RAIL_BOOSTER.get());
        dropSelf(ModBlocks.STEEL_POLES.get());
        dropSelf(ModBlocks.STEEL_WALL.get());
        dropSelf(ModBlocks.STEEL_CORNER.get());
        dropSelf(ModBlocks.STEEL_ROOF.get());
        dropSelf(ModBlocks.legacyBlock("crystal_pulsar").get());
        addNoDrop(ModBlocks.MACHINE_FLUIDTANK.get());
        addNoDrop(ModBlocks.MACHINE_BAT9000.get());
        addNoDrop(ModBlocks.MACHINE_BIGASSTANK.get());
        addNoDrop(ModBlocks.MACHINE_UF6_TANK.get());
        addNoDrop(ModBlocks.MACHINE_PUF6_TANK.get());
        addNoDrop(ModBlocks.MACHINE_REFINERY.get());
        addNoDrop(ModBlocks.BARREL_PLASTIC.get());
        dropSelf(ModBlocks.BARREL_CORRODED.get());
        dropSelf(ModBlocks.MACHINE_FENSU.get());
        add(ModBlocks.MACHINE_BOILER_OFF.get(), oldBoilerScrapsDrop());
        add(ModBlocks.FILING_CABINET.get(),
                legacyStateVariantDrop(ModBlocks.FILING_CABINET.get(), LegacyFileCabinetBlock.VARIANT, 2));
        dropSelf(ModBlocks.PEDESTAL.get());
        add(ModBlocks.RED_CABLE_BOX.get(),
                legacyStateVariantDrop(ModBlocks.RED_CABLE_BOX.get(), RedCableBoxBlock.SIZE, 5));
        dropSelf(ModBlocks.PISTON_INSERTER.get());
        add(ModBlocks.FLUID_DUCT_NEO.get(),
                legacyStateVariantDrop(ModBlocks.FLUID_DUCT_NEO.get(), FluidPipeBlock.LEGACY_STYLE,
                        HbmFluidDuctVariants.standardVisibleStyles()));
        add(ModBlocks.FLUID_DUCT_BOX.get(),
                legacyStateVariantDrop(ModBlocks.FLUID_DUCT_BOX.get(), FluidDuctBoxBlock.LEGACY_METADATA,
                        HbmFluidDuctVariants.boxVisibleMetadata()));
        add(ModBlocks.FLUID_DUCT_EXHAUST.get(),
                legacyStateVariantDrop(ModBlocks.FLUID_DUCT_EXHAUST.get(), FluidDuctBoxBlock.LEGACY_METADATA,
                        HbmFluidDuctVariants.BOX_METADATA_COUNT));
        add(ModBlocks.RAD_ABSORBER.get(),
                legacyStateVariantDrop(ModBlocks.RAD_ABSORBER.get(), LegacyRadAbsorberBlock.TIER, 4));
        add(ModBlocks.DECO_TOASTER.get(),
                legacyStateVariantDrop(ModBlocks.DECO_TOASTER.get(), DecoToasterBlock.VARIANT, 3));
        add(ModBlocks.DECO_CRT.get(),
                legacyStateVariantDrop(ModBlocks.DECO_CRT.get(), DecoCrtBlock.VARIANT, 4));
        dropSelf(ModBlocks.DECO_COMPUTER.get());
        addNoDrop(ModBlocks.LANTERN_BEHEMOTH.get());
        dropSelf(ModBlocks.BOXCAR.get());
        add(ModBlocks.CONCRETE_COLORED_EXT.get(),
                legacyStateVariantDrop(ModBlocks.CONCRETE_COLORED_EXT.get(), ConcreteColoredExtBlock.VARIANT, 8));
        RegistryObject<? extends Block> concreteColored = ModBlocks.legacyBlock("concrete_colored");
        add(concreteColored.get(), legacyStateVariantDrop(concreteColored.get(), ConcreteColoredBlock.VARIANT, 16));
        add(ModBlocks.STONE_BIOME.get(),
                legacyStateVariantDrop(ModBlocks.STONE_BIOME.get(), LegacyBiomeStoneBlock.VARIANT, 2));
        add(ModBlocks.PLATEMETAL.get(),
                legacyStateVariantDrop(ModBlocks.PLATEMETAL.get(), PlatemetalBlock.VARIANT, 15));
        dropSelf(ModBlocks.VINE_PHOSPHOR.get());
        addNoDrop(ModBlocks.BARREL_STEEL.get());
        addNoDrop(ModBlocks.BARREL_TCALLOY.get());
        addNoDrop(ModBlocks.BARREL_ANTIMATTER.get());
        dropSelf(ModBlocks.FIELD_DISTURBER.get());
        dropSelf(ModBlocks.CAPACITOR_BUS.get());
        addNoDrop(ModBlocks.MACHINE_RTG_FURNACE.get());
        dropSelf(ModBlocks.MACHINE_BATTERY.get());
        dropSelf(ModBlocks.MACHINE_BATTERY_POTATO.get());
        dropSelf(ModBlocks.MACHINE_LITHIUM_BATTERY.get());
        dropSelf(ModBlocks.MACHINE_SCHRABIDIUM_BATTERY.get());
        dropSelf(ModBlocks.MACHINE_DINEUTRONIUM_BATTERY.get());
        dropSelf(ModBlocks.CAPACITOR_COPPER.get());
        dropSelf(ModBlocks.CAPACITOR_GOLD.get());
        dropSelf(ModBlocks.CAPACITOR_NIOBIUM.get());
        dropSelf(ModBlocks.CAPACITOR_TANTALIUM.get());
        dropSelf(ModBlocks.CAPACITOR_SCHRABIDATE.get());
        addNoDrop(ModBlocks.MACHINE_WELL.get());
        addNoDrop(ModBlocks.MACHINE_PUMPJACK.get());
        addNoDrop(ModBlocks.MACHINE_FRACKING_TOWER.get());
        addNoDrop(ModBlocks.CRATE_IRON.get());
        addNoDrop(ModBlocks.CRATE_STEEL.get());
        addNoDrop(ModBlocks.CRATE_DESH.get());
        addNoDrop(ModBlocks.CRATE_TUNGSTEN.get());
        // BlockSupplyCrate#getItemDropped returns null. The block itself
        // preserves its NBT through playerWillDestroy, while crowbar releases
        // its contents directly.
        addNoDrop(ModBlocks.CRATE_SUPPLY.get());
        addNoDrop(ModBlocks.SAFE.get());
        addNoDrop(ModBlocks.MASS_STORAGE.get());
        addNoDrop(ModBlocks.VENDING_MACHINE.get());
        dropSelf(ModBlocks.MACHINE_SATLINKER.get());
        dropSelf(ModBlocks.MACHINE_SATLINK.get());
        dropSelf(ModBlocks.PA_SOURCE.get());
        dropSelf(ModBlocks.PA_BEAMLINE.get());
        dropSelf(ModBlocks.PA_RFC.get());
        dropSelf(ModBlocks.PA_QUADRUPOLE.get());
        dropSelf(ModBlocks.PA_DIPOLE.get());
        dropSelf(ModBlocks.PA_DETECTOR.get());
        dropSelf(ModBlocks.SAT_DOCK.get());
        dropSelf(ModBlocks.SOYUZ_CAPSULE.get());
        addNoDrop(ModBlocks.SOYUZ_LAUNCHER.get());
        dropSelf(ModBlocks.STRUCT_LAUNCHER.get());
        dropSelf(ModBlocks.STRUCT_SCAFFOLD.get());
        dropSelf(ModBlocks.STRUCT_LAUNCHER_CORE.get());
        dropSelf(ModBlocks.STRUCT_LAUNCHER_CORE_LARGE.get());
        dropSelf(ModBlocks.STRUCT_SOYUZ_CORE.get());
        dropSelf(ModBlocks.LAUNCH_PAD.get());
        dropSelf(ModBlocks.LAUNCH_PAD_LARGE.get());
        // LaunchPadRusted#getItemDropped returns null in 1.7.10.  Its
        // inventory is still handled by RustedLaunchPadBlock#onCoreRemoved,
        // but breaking the pad itself must not recreate the block item.
        addNoDrop(ModBlocks.LAUNCH_PAD_RUSTED.get());
        add(ModBlocks.LAUNCH_TABLE.get(), block -> createSingleItemTable(ModBlocks.STRUCT_LAUNCHER_CORE_LARGE.get()));
        add(ModBlocks.COMPACT_LAUNCHER.get(), block -> createSingleItemTable(ModBlocks.STRUCT_LAUNCHER_CORE.get()));
        dropSelf(ModBlocks.MACHINE_MISSILE_ASSEMBLY.get());
        addNoDrop(ModBlocks.OIL_PIPE.get());
        add(ModBlocks.CONVEYOR.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_EXPRESS.get(), conveyorWandDrop("EXPRESS"));
        add(ModBlocks.CONVEYOR_DOUBLE.get(), conveyorWandDrop("DOUBLE"));
        add(ModBlocks.CONVEYOR_TRIPLE.get(), conveyorWandDrop("TRIPLE"));
        add(ModBlocks.CONVEYOR_LIFT.get(), conveyorWandDrop("REGULAR"));
        add(ModBlocks.CONVEYOR_CHUTE.get(), conveyorWandDrop("REGULAR"));
        addNoDrop(ModBlocks.FLOODLIGHT_BEAM.get());
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
        addNoDrop(ModBlocks.FOUNDRY_SLAG.get());
        ModBlocks.PYLON_BLOCKS.forEach(block -> dropSelf(block.get()));
        ModBlocks.BLOCK_TAB_BLOCKS.stream()
                .filter(block -> !ModBlocks.CAP_BLOCKS.contains(block))
                .filter(block -> block != ModBlocks.BURNING_EARTH && block != ModBlocks.IMPACT_DIRT)
                .filter(block -> !"glyphid_base".equals(block.getId().getPath()))
                .filter(block -> !"glyphid_spawner".equals(block.getId().getPath()))
                .filter(block -> block != ModBlocks.CONCRETE_COLORED_EXT)
                .filter(block -> !"concrete_colored".equals(block.getId().getPath()))
                .filter(block -> block != ModBlocks.BLOCK_COKE)
                .filter(block -> block != ModBlocks.WOOD_STRUCTURE)
                .forEach(block -> dropSelf(block.get()));
        add(ModBlocks.BLOCK_COKE.get(),
                legacyStateVariantDrop(ModBlocks.BLOCK_COKE.get(), LegacyCokeBlock.VARIANT, 3));
        add(ModBlocks.WOOD_STRUCTURE.get(),
                legacyStateVariantDrop(ModBlocks.WOOD_STRUCTURE.get(), LegacyWoodStructureBlock.VARIANT, 3));
        addNoDrop(ModBlocks.legacyBlock("glyphid_base").get());
        add(ModBlocks.legacyBlock("glyphid_spawner").get(), this::glyphidSpawnerDrop);
        addLegacyOreDrops();
        addCapBlockDrops();
        addNoDrop(ModBlocks.WASTE_LEAVES.get());
        addNoDrop(ModBlocks.LEAVES_LAYER.get());
        addNoDrop(ModBlocks.FOAM_LAYER.get());
        addNoDrop(ModBlocks.SAND_BORON_LAYER.get());
        addNoDrop(ModBlocks.BARRICADE.get());
        addNoDrop(ModBlocks.OIL_SPILL.get());
        add(ModBlocks.WASTE_LOG.get(), wasteLogDrop());
        ModBlocks.PLANT_FLOWER_BLOCKS.stream()
                .filter(block -> block != ModBlocks.PLANT_FLOWER_CD1)
                .forEach(block -> dropSelf(block.get()));
        add(ModBlocks.PLANT_FLOWER_CD1.get(), block -> createSingleItemTable(ModBlocks.PLANT_FLOWER_CD0.get()));
        addNoDrop(ModBlocks.PLANT_DEAD_BIGFLOWER.get());
        add(ModBlocks.PLANT_TALL_WEED.get(), block -> tallPlantDrop(block, ModBlocks.PLANT_FLOWER_WEED.get(), false));
        add(ModBlocks.PLANT_TALL_CD2.get(), block -> tallPlantDrop(block, ModBlocks.PLANT_FLOWER_CD0.get(), false));
        add(ModBlocks.PLANT_TALL_CD3.get(), block -> tallPlantDrop(block, ModBlocks.PLANT_FLOWER_CD0.get(), false));
        add(ModBlocks.PLANT_TALL_CD4.get(), block -> tallPlantDrop(block, ModBlocks.PLANT_FLOWER_CD0.get(), true));
        add(ModBlocks.MUSH_BLOCK.get(), hugeMushDrop());
        add(ModBlocks.MUSH_BLOCK_STEM.get(), hugeMushDrop());
        add(ModBlocks.FROZEN_GRASS.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.FROZEN_DIRT.get(), block -> singleItemDrop(Items.SNOWBALL));
        add(ModBlocks.BURNING_EARTH.get(), block -> singleItemDrop(Items.DIRT));
        add(ModBlocks.IMPACT_DIRT.get(), block -> singleItemDrop(Items.DIRT));
        add(ModBlocks.FROZEN_LOG.get(), block -> snowballStackDrop(2.0F, 4.0F));
        add(ModBlocks.FROZEN_PLANKS.get(), block -> singleItemDrop(Items.SNOWBALL));
        addNoDrop(ModBlocks.FIRE_DIGAMMA.get());
        addNoDrop(ModBlocks.BALEFIRE.get());
        addNoDrop(ModBlocks.CORIUM_BLOCK.get());
        addNoDrop(ModBlocks.MUD_BLOCK.get());
        addNoDrop(ModBlocks.TAINT.get());
        ModBlocks.NUKE_TAB_BLOCKS.stream()
                .filter(block -> block != ModBlocks.CHARGE_DYNAMITE
                        && block != ModBlocks.CHARGE_MINER
                        && block != ModBlocks.CHARGE_C4
                        && block != ModBlocks.CHARGE_SEMTEX
                        && block != ModBlocks.VOLCANO_CORE
                        && block != ModBlocks.VOLCANO_RAD_CORE
                        && block != ModBlocks.MINE_NAVAL
                        && block != ModBlocks.MINE_AP
                        && block != ModBlocks.MINE_HE
                        && block != ModBlocks.MINE_SHRAP
                        && block != ModBlocks.MINE_FAT)
                .forEach(block -> dropSelf(block.get()));
        addNoDrop(ModBlocks.MINE_NAVAL.get());
        addNoDrop(ModBlocks.MINE_AP.get());
        addNoDrop(ModBlocks.MINE_HE.get());
        addNoDrop(ModBlocks.MINE_SHRAP.get());
        addNoDrop(ModBlocks.MINE_FAT.get());
        addNoDrop(ModBlocks.VOLCANO_CORE.get());
        addNoDrop(ModBlocks.VOLCANO_RAD_CORE.get());
        addNoDrop(ModBlocks.CHARGE_DYNAMITE.get());
        addNoDrop(ModBlocks.CHARGE_MINER.get());
        addNoDrop(ModBlocks.CHARGE_C4.get());
        addNoDrop(ModBlocks.CHARGE_SEMTEX.get());
        addNoDrop(ModBlocks.GAS_RADON.get());
        addNoDrop(ModBlocks.GAS_RADON_DENSE.get());
        addNoDrop(ModBlocks.GAS_RADON_TOMB.get());
        addNoDrop(ModBlocks.GAS_MELTDOWN.get());
        addNoDrop(ModBlocks.GAS_MONOXIDE.get());
        addNoDrop(ModBlocks.GAS_ASBESTOS.get());
        addNoDrop(ModBlocks.GAS_COAL.get());
        addNoDrop(ModBlocks.CHLORINE_GAS.get());
        addNoDrop(ModBlocks.GAS_FLAMMABLE.get());
        addNoDrop(ModBlocks.GAS_EXPLOSIVE.get());
        addNoDrop(ModBlocks.VENT_CHLORINE.get());
        addNoDrop(ModBlocks.VENT_CLOUD.get());
        addNoDrop(ModBlocks.VENT_PINK_CLOUD.get());
        dropSelf(ModBlocks.VENT_CHLORINE_SEAL.get());
        dropSelf(ModBlocks.BROADCASTER_PC.get());
        addNoDrop(ModBlocks.GEYSIR_CHLORINE.get());
        addNoDrop(ModBlocks.GEYSIR_NETHER.get());
        addNoDrop(ModBlocks.TOXIC_BLOCK.get());
        addNoDrop(ModBlocks.DUMMY_BLOCK.get());
        addNoDrop(ModBlocks.ICF_BLOCK.get());
        addNoDrop(ModBlocks.PWR_BLOCK.get());
        add(ModBlocks.ZIRNOX_DESTROYED.get(), zirnoxDestroyedDrop());
        addNoDrop(ModBlocks.SPOTLIGHT_BEAM.get());
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
        add(ModBlocks.GLASS_POLARIZED.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_POLARIZED.get()));
        add(ModBlocks.GLASS_QUARTZ.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_QUARTZ.get()));
        add(ModBlocks.REINFORCED_GLASS.get(), createSilkTouchOnlyTable(ModBlocks.REINFORCED_GLASS.get()));
        dropSelf(ModBlocks.SAND_BORON.get());
        dropSelf(ModBlocks.SAND_LEAD.get());
        dropSelf(ModBlocks.SAND_URANIUM.get());
        dropSelf(ModBlocks.SAND_POLONIUM.get());
        dropSelf(ModBlocks.SAND_QUARTZ.get());
        add(ModBlocks.GLASS_TRINITITE.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_TRINITITE.get()));
        add(ModBlocks.GLASS_ASH.get(), createSilkTouchOnlyTable(ModBlocks.GLASS_ASH.get()));
        add(ModBlocks.FALLOUT.get(), block -> createSingleItemTable(ModItems.legacyItem("fallout").get()));
        add(ModBlocks.WASTE_TRINITITE.get(), block -> singleItemDrop(ModItems.legacyItem("trinitite").get()));
        add(ModBlocks.WASTE_TRINITITE_RED.get(), block -> singleItemDrop(ModItems.legacyItem("trinitite").get()));
        add(ModBlocks.TEKTITE.get(), block -> createSingleItemTable(ModBlocks.TEKTITE.get()));
        dropSelf(ModBlocks.ORE_TEKTITE_OSMIRIDIUM.get());
        add(ModBlocks.ORE_SELLAFIELD_DIAMOND.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(Items.DIAMOND)));
        add(ModBlocks.ORE_SELLAFIELD_EMERALD.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(Items.EMERALD)));
        add(ModBlocks.ORE_SELLAFIELD_RADGEM.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem("gem_rad").get())));
        add(ModBlocks.ORE_BASALT.get(), block -> basaltOreDrop(block));
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
        addLegacyFortuneOreDrop("deepslate_ore_fluorite", "fluorite", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("ore_niter", "niter", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("deepslate_ore_niter", "niter", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("ore_sulfur", "sulfur", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("deepslate_ore_sulfur", "sulfur", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("ore_nether_sulfur", "sulfur", 2.0F, 4.0F);
        addLegacySingleOreDrop("ore_asbestos", "ingot_asbestos");
        addLegacySingleOreDrop("deepslate_ore_asbestos", "ingot_asbestos");
        addLegacySingleOreDrop("ore_gneiss_asbestos", "ingot_asbestos");
        addLegacySingleOreDrop("ore_rare", "chunk_ore_rare");
        addLegacySingleOreDrop("deepslate_ore_rare", "chunk_ore_rare");
        addLegacySingleOreDrop("ore_gneiss_rare", "chunk_ore_rare");
        addLegacySingleOreDrop("ore_lignite", "lignite");
        addLegacySingleOreDrop("deepslate_ore_lignite", "lignite");
        addLegacySingleOreDrop("ore_nether_coal", "coal_infernal");
        addLegacySingleOreDrop("ore_nether_smoldering", "powder_fire");
        addLegacySingleOreDrop("ore_cinnebar", "cinnebar");
        addLegacySingleOreDrop("deepslate_ore_cinnebar", "cinnebar");
        addLegacyFortuneOreDrop("ore_depth_cinnebar", "cinnebar", 2.0F, 4.0F);
        addLegacyFortuneOreDrop("ore_depth_borax", "powder_borax", 1.0F, 1.0F);
        addLegacyFortuneOreDrop("ore_depth_zirconium", "nugget_zirconium", 2.0F, 3.0F);
        addLegacyFortuneOreDrop("ore_depth_nether_neodymium", "fragment_neodymium", 2.0F, 3.0F);
        addLegacySingleOreDrop("ore_coltan", "fragment_coltan");
        addLegacySingleOreDrop("deepslate_ore_coltan", "fragment_coltan");
        addLegacyFortuneOreDrop("ore_cobalt", "fragment_cobalt", 4.0F, 9.0F);
        addLegacyFortuneOreDrop("deepslate_ore_cobalt", "fragment_cobalt", 4.0F, 9.0F);
        addLegacyFortuneOreDrop("ore_nether_cobalt", "fragment_cobalt", 5.0F, 12.0F);
        addLegacyFortuneOreDrop("stone_resource_malachite", "chunk_ore_malachite", 3.0F, 4.0F);
        addLegacyNoFortuneOreDrop("cluster_iron", "crystal_iron");
        addLegacyNoFortuneOreDrop("cluster_titanium", "crystal_titanium");
        addLegacyNoFortuneOreDrop("cluster_aluminium", "crystal_aluminium");
        addLegacyNoFortuneOreDrop("cluster_copper", "crystal_copper");
        addLegacyFortuneOreDrop("cluster_depth_iron", "crystal_iron", 1.0F, 1.0F);
        addLegacyFortuneOreDrop("cluster_depth_titanium", "crystal_titanium", 1.0F, 1.0F);
        addLegacyFortuneOreDrop("cluster_depth_tungsten", "crystal_tungsten", 1.0F, 1.0F);
        add(ModBlocks.STALACTITE_SULFUR.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem("sulfur").get())));
        add(ModBlocks.STALAGMITE_SULFUR.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem("sulfur").get())));
        add(ModBlocks.STALACTITE_ASBESTOS.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem("powder_asbestos").get())));
        add(ModBlocks.STALAGMITE_ASBESTOS.get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem("powder_asbestos").get())));
        addLegacyNetherFireOreDrop();
        addLegacyNoSilkFortuneDrop("ore_oil", "oil_tar_crude");
        addLegacyNoFortuneOreDrop("block_meteor_cobble", "fragment_meteorite");
        addLegacyNoFortuneOreDrop("block_meteor_broken", "fragment_meteorite", 1.0F, 3.0F);
        addNoDrop(ModBlocks.BLOCK_METEOR_MOLTEN.get());
        addNoDrop(ModBlocks.BLOCK_METEOR_TREASURE.get()); // Runtime drops are POOL_METEORITE_TREASURE rolls.
        dropSelf(ModBlocks.CONCRETE_SUPER.get());
        dropSelf(ModBlocks.METEOR_POLISHED.get());
        dropSelf(ModBlocks.METEOR_BRICK.get());
        dropSelf(ModBlocks.METEOR_BRICK_MOSSY.get());
        dropSelf(ModBlocks.METEOR_BRICK_CRACKED.get());
        dropSelf(ModBlocks.METEOR_PILLAR.get());
        dropSelf(ModBlocks.METEOR_BATTERY.get());
        dropSelf(ModBlocks.ORE_METEOR_IRON.get());
        dropSelf(ModBlocks.ORE_METEOR_COPPER.get());
        dropSelf(ModBlocks.ORE_METEOR_ALUMINIUM.get());
        dropSelf(ModBlocks.ORE_METEOR_RAREEARTH.get());
        dropSelf(ModBlocks.ORE_METEOR_COBALT.get());
        addNoDrop(ModBlocks.ORE_BEDROCK_COLTAN.get());
        addNoDrop(ModBlocks.ORE_BEDROCK_OIL.get());
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

    private void addLegacyNoFortuneOreDrop(String blockName, String itemName) {
        add(ModBlocks.legacyBlock(blockName).get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem(itemName).get())));
    }

    private void addLegacyNoFortuneOreDrop(String blockName, String itemName, float min, float max) {
        add(ModBlocks.legacyBlock(blockName).get(), block -> createSilkTouchDispatchTable(block,
                LootItem.lootTableItem(ModItems.legacyItem(itemName).get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))));
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

    private LootTable.Builder glyphidSpawnerDrop(Block block) {
        return LootTable.lootTable()
                .withPool(glyphidEggPool(4.0F, 6.0F, fortuneAtLeast(3)))
                .withPool(glyphidEggPool(3.0F, 5.0F, fortuneExactly(2)))
                .withPool(glyphidEggPool(2.0F, 4.0F, fortuneExactly(1)))
                .withPool(glyphidEggPool(1.0F, 3.0F, noFortune()));
    }

    private LootPool.Builder glyphidEggPool(float min, float max, LootItemCondition.Builder condition) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(ModItems.EGG_GLYPHID.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))))
                .when(ExplosionCondition.survivesExplosion())
                .when(condition);
    }

    private LootItemCondition.Builder fortuneExactly(int level) {
        return fortune(MinMaxBounds.Ints.exactly(level));
    }

    private LootItemCondition.Builder fortuneAtLeast(int level) {
        return fortune(MinMaxBounds.Ints.atLeast(level));
    }

    private LootItemCondition.Builder noFortune() {
        return InvertedLootItemCondition.invert(fortuneAtLeast(1));
    }

    private LootItemCondition.Builder fortune(MinMaxBounds.Ints levels) {
        return MatchTool.toolMatches(ItemPredicate.Builder.item()
                .hasEnchantment(new EnchantmentPredicate(Enchantments.BLOCK_FORTUNE, levels)));
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

    private LootTable.Builder tallPlantDrop(Block tallPlant, Block flower, boolean mature) {
        LootItemCondition.Builder upperHalf = LootItemBlockStatePropertyCondition.hasBlockStateProperties(tallPlant)
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(flower))
                        .when(ExplosionCondition.survivesExplosion())
                        .when(upperHalf));
        if (mature) {
            table.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(ModItems.legacyItem("plant_item_mustardwillow").get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 6.0F))))
                    .when(ExplosionCondition.survivesExplosion())
                    .when(upperHalf));
        }
        return table;
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

    private LootTable.Builder legacyDoubleSlabDrop(Block doubleSlab, Block singleSlab, int variants) {
        LootTable.Builder table = LootTable.lootTable();
        for (int variant = 0; variant < variants; variant++) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(LegacyStateBlockItem.TAG_VARIANT, variant);
            table.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(singleSlab)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
                            .apply(SetNbtFunction.setTag(tag)))
                    .when(ExplosionCondition.survivesExplosion())
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(doubleSlab)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(LegacyMultiSlabBlock.VARIANT, variant))));
        }
        return table;
    }

    private LootTable.Builder basaltOreDrop(Block block) {
        LootTable.Builder table = LootTable.lootTable();
        for (LegacyBasaltOreBlock.Variant variant : LegacyBasaltOreBlock.Variant.values()) {
            LootPoolEntryContainer.Builder<?> item = basaltOreDropEntry(block, variant);
            table.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(item)
                    .when(ExplosionCondition.survivesExplosion())
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(LegacyBasaltOreBlock.VARIANT, variant.legacyMeta()))));
        }
        return table;
    }

    private LootPoolEntryContainer.Builder<?> basaltOreDropEntry(Block block, LegacyBasaltOreBlock.Variant variant) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(LegacyStateBlockItem.TAG_VARIANT, variant.legacyMeta());
        LootItem.Builder<?> silkTouchBlock = LootItem.lootTableItem(block)
                .apply(SetNbtFunction.setTag(tag))
                .when(HAS_SILK_TOUCH);
        Item item = variant.droppedItem();
        if (item != null) {
            return AlternativesEntry.alternatives(silkTouchBlock, LootItem.lootTableItem(item));
        }
        return LootItem.lootTableItem(block).apply(SetNbtFunction.setTag(tag));
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

    private void addNoDrop(Block block) {
        if (!block.getLootTable().equals(BuiltInLootTables.EMPTY)) {
            add(block, noDrop());
        }
    }

    @Override
    protected void add(Block block, LootTable.Builder builder) {
        if (!block.getLootTable().equals(BuiltInLootTables.EMPTY)) {
            super.add(block, builder);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(RegistryObject::get)
                .filter(block -> block != ModBlocks.ORE_BEDROCK.get())
                .filter(block -> !block.getLootTable().equals(BuiltInLootTables.EMPTY))::iterator;
    }
}
