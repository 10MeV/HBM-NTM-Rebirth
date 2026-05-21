# 1.7.10 Chunk Radiation Block Source Reconciliation

Generated: 2026-05-21

Source of truth: 1.7.10 ModBlocks, block classes that call ChunkRadiationManager, HazardRegistry, and OreDictManager radioactive DictFrame chains.

## Summary

- continuous_rows: 41
- unique_continuous_blocks: 36
- mismatch_or_missing_rows: 14
- modern_noncontinuous_review_rows: 22

## Rules Found In 1.7.10

- BlockHazard, BlockHotHazard, BlockNuclearWaste: writes item/block radiation hazard * 0.1 every scheduled update.
- BlockHazardFalling: same hazard * 0.1 rule, then performs falling-block behavior.
- BlockOre: only explicit BlockOre(Material, rad, max) writes chunk radiation; ordinary hazard-only ores do not.
- OreDictManager radioactive block entries use base radiation * 10; ore entries use base radiation * 1.
- BlockSellafield: writes 0.5 * (metadata + 1), independent from the metadata stack hazard table.
- YellowBarrel: yellow barrel writes 5.0, vitrified barrel writes 0.5; explosion adds one-shot 35.
- BlockGasMeltdown: writes 5.0 only when it can see sky.
- BlockAbsorber: subtracts radiation by tier; base block is a negative source, not a polluter.

## Continuous Sources

| block | meta | old class | old chunk rad/update | old item hazard | modern tick | status | note |
|---|---:|---|---:|---:|---:|---|---|
| `block_actinium` |  | `BlockHazard` | 30.0 | 300.0 | 30.0 | aligned_if_bridge_class_matches |  |
| `block_corium` |  | `BlockHazard` | 15.0 | 150.0 | 15.0 | aligned_if_bridge_class_matches |  |
| `block_fallout` |  | `BlockHazardFalling` | 1.05 | 10.5 | 1.05 | aligned_if_bridge_class_matches | Schedules every 20 ticks; then applies falling-block behavior. |
| `block_mox_fuel` |  | `BlockHazard` | 2.5 | 25.0 | 2.5 | aligned_if_bridge_class_matches |  |
| `block_neptunium` |  | `BlockHazard` | 2.5 | 25.0 | 2.5 | aligned_if_bridge_class_matches |  |
| `block_plutonium` |  | `BlockHazard` | 7.5 | 75.0 | 7.5 | aligned_if_bridge_class_matches |  |
| `block_plutonium_fuel` |  | `BlockHazard` | 4.25 | 42.5 | 4.25 | aligned_if_bridge_class_matches |  |
| `block_polonium` |  | `BlockHotHazard` | 75.0 | 750.0 | 75.0 | aligned_if_bridge_class_matches |  |
| `block_pu238` |  | `BlockHotHazard` | 10.0 | 100.0 | 10.0 | aligned_if_bridge_class_matches |  |
| `block_pu239` |  | `BlockHazard` | 5.0 | 50.0 | 5.0 | aligned_if_bridge_class_matches |  |
| `block_pu240` |  | `BlockHazard` | 7.5 | 75.0 | 7.5 | aligned_if_bridge_class_matches |  |
| `block_pu_mix` |  | `BlockHazard` | 6.25 | 62.5 | 7.5 | tick_mismatch |  |
| `block_ra226` |  | `BlockHazard` | 7.5 | 75.0 | 7.5 | aligned_if_bridge_class_matches |  |
| `block_schrabidate` |  | `BlockHazard` | 1.5 | 15.0 | 17.5 | tick_mismatch |  |
| `block_schrabidium` |  | `BlockHazard` | 15.0 | 150.0 | 15.0 | aligned_if_bridge_class_matches |  |
| `block_schrabidium_fuel` |  | `BlockHazard` | 5.85 | 58.5 | 5.85 | aligned_if_bridge_class_matches |  |
| `block_schraranium` |  | `BlockHazard` | 1.5 | 15.0 | 1.5 | aligned_if_bridge_class_matches |  |
| `block_solinium` |  | `BlockHazard` | 17.5 | 175.0 | 15.0 | tick_mismatch |  |
| `block_thorium` |  | `BlockHazard` | 0.1 | 1.0 | 0.1 | aligned_if_bridge_class_matches |  |
| `block_thorium_fuel` |  | `BlockHazard` | 1.75 | 17.5 | 1.75 | aligned_if_bridge_class_matches |  |
| `block_trinitite` |  | `BlockHazard` | 0.1 | 1.0 | 0.1 | aligned_if_bridge_class_matches |  |
| `block_u233` |  | `BlockHazard` | 5.0 | 50.0 | 5.0 | aligned_if_bridge_class_matches |  |
| `block_u235` |  | `BlockHazard` | 1.0 | 10.0 | 1.0 | aligned_if_bridge_class_matches |  |
| `block_u238` |  | `BlockHazard` | 0.25 | 2.5 | 0.25 | aligned_if_bridge_class_matches |  |
| `block_uranium` |  | `BlockHazard` | 0.35 | 3.5 | 0.35 | aligned_if_bridge_class_matches |  |
| `block_uranium_fuel` |  | `BlockHazard` | 0.5 | 5.0 | 0.5 | aligned_if_bridge_class_matches |  |
| `block_waste` |  | `BlockNuclearWaste` | 15.0 | 150.0 | 15.0 | aligned_if_bridge_class_matches | BlockNuclearWaste also emits/spreads radon gas before superclass tick. |
| `block_waste_painted` |  | `BlockNuclearWaste` | 15.0 | 150.0 | 15.0 | aligned_if_bridge_class_matches | BlockNuclearWaste also emits/spreads radon gas before superclass tick. |
| `block_waste_vitrified` |  | `BlockNuclearWaste` | 7.5 | 75.0 | 7.5 | aligned_if_bridge_class_matches | BlockNuclearWaste also emits/spreads radon gas before superclass tick. |
| `block_yellowcake` |  | `BlockHazardFalling` | 1.05 | 10.5 | 1.05 | aligned_if_bridge_class_matches | Schedules every 20 ticks; then applies falling-block behavior. |
| `gas_meltdown` |  | `BlockGasMeltdown` | 5.0 |  |  | missing_special_block | Only while block can see sky; also decays/spreads gas. |
| `ore_schrabidium` |  | `BlockOre` | 0.1 | 15.0 | 1.5 | tick_mismatch | Explicit BlockOre rad constructor; normal hazard-only ores do not tick chunk radiation. |
| `rad_absorber` |  | `BlockAbsorber` | -2.5 |  |  | missing_special_block | Negative source. Tier variants absorb: BASE 2.5, RED 10, GREEN 100, PINK 10000 every 10 ticks. |
| `sellafield` | 0 | `BlockSellafield` | 0.5 | 0.5 |  | missing_special_block | Continuous chunk source uses constructor rad*(meta+1), not stack hazard table. |
| `sellafield` | 1 | `BlockSellafield` | 1.0 | 1.0 |  | missing_special_block | Continuous chunk source uses constructor rad*(meta+1), not stack hazard table. |
| `sellafield` | 2 | `BlockSellafield` | 1.5 | 2.5 |  | missing_special_block | Continuous chunk source uses constructor rad*(meta+1), not stack hazard table. |
| `sellafield` | 3 | `BlockSellafield` | 2.0 | 4.0 |  | missing_special_block | Continuous chunk source uses constructor rad*(meta+1), not stack hazard table. |
| `sellafield` | 4 | `BlockSellafield` | 2.5 | 5.0 |  | missing_special_block | Continuous chunk source uses constructor rad*(meta+1), not stack hazard table. |
| `sellafield` | 5 | `BlockSellafield` | 3.0 | 10.0 |  | missing_special_block | Continuous chunk source uses constructor rad*(meta+1), not stack hazard table. |
| `vitrified_barrel` |  | `YellowBarrel` | 0.5 |  |  | missing_special_block | Explosion has an extra one-shot +35 chunk rad; not counted as continuous source. |
| `yellow_barrel` |  | `YellowBarrel` | 5.0 | 150.0 |  | missing_special_block | Explosion has an extra one-shot +35 chunk rad; not counted as continuous source. |

## Modern Hazard Entries With No 1.7.10 Continuous Chunk Source

These entries may still be valid item/block hazard data. They should not become continuous chunk polluters unless the old class also called ChunkRadiationManager.

| block | old class | old item hazard | modern bridge tick | status |
|---|---|---:|---:|---|
| `ancient_scrap` | `BlockOutgas` | 150.0 | 15.0 | modern_overemits_if_registered_as_radiating_block |
| `block_corium_cobble` | `BlockOutgas` | 150.0 | 15.0 | modern_overemits_if_registered_as_radiating_block |
| `block_euphemium` | `BlockBeaconable` |  | 50000.0 | modern_overemits_if_registered_as_radiating_block |
| `block_euphemium_cluster` | `BlockRotatablePillar` |  | 50000.0 | modern_overemits_if_registered_as_radiating_block |
| `block_red_phosphorus` | `BlockHazardFalling` |  | 0.1 | modern_overemits_if_registered_as_radiating_block |
| `block_schrabidium_cluster` | `BlockRotatablePillar` |  | 15.0 | modern_overemits_if_registered_as_radiating_block |
| `block_tritium` | `BlockRotatablePillar` |  | 0.01 | modern_overemits_if_registered_as_radiating_block |
| `block_white_phosphorus` | `BlockHazard` |  | 0.1 | modern_overemits_if_registered_as_radiating_block |
| `fallout` | `BlockFallout` | 60.0 |  | modern_hazard_only_review |
| `ore_asbestos` | `BlockOutgas` |  | 0.1 | modern_overemits_if_registered_as_radiating_block |
| `ore_gneiss_asbestos` | `BlockOutgas` |  | 0.1 | modern_overemits_if_registered_as_radiating_block |
| `ore_gneiss_gas` | `BlockOre` |  | 0.1 | modern_overemits_if_registered_as_radiating_block |
| `ore_gneiss_schrabidium` | `BlockOre` | 15.0 | 1.5 | modern_overemits_if_registered_as_radiating_block |
| `ore_gneiss_uranium` | `BlockOutgas` | 0.35 | 0.035 | modern_overemits_if_registered_as_radiating_block |
| `ore_gneiss_uranium_scorched` | `BlockOutgas` | 0.35 | 0.035 | modern_overemits_if_registered_as_radiating_block |
| `ore_nether_plutonium` | `BlockGeneric` | 7.5 | 0.75 | modern_overemits_if_registered_as_radiating_block |
| `ore_nether_schrabidium` | `BlockGeneric` | 15.0 | 1.5 | modern_overemits_if_registered_as_radiating_block |
| `ore_nether_uranium` | `BlockOutgas` | 0.35 | 0.035 | modern_overemits_if_registered_as_radiating_block |
| `ore_nether_uranium_scorched` | `BlockOutgas` | 0.35 | 0.035 | modern_overemits_if_registered_as_radiating_block |
| `ore_thorium` | `BlockGeneric` | 0.1 | 0.01 | modern_overemits_if_registered_as_radiating_block |
| `ore_uranium` | `BlockOutgas` | 0.35 | 0.035 | modern_overemits_if_registered_as_radiating_block |
| `ore_uranium_scorched` | `BlockOutgas` | 0.35 | 0.035 | modern_overemits_if_registered_as_radiating_block |

## Generated CSV Files

- `legacy-chunk-radiation-block-sources-2026-05-21.csv`
- `legacy-noncontinuous-modern-hazard-review-2026-05-21.csv`
