# 2026-05-21 machine press recipes 1.7.10 source trace

## Scope

This record covers the next small migration slice for the 1.7.10 burner press recipe and stamp surface. The target is the existing 1.20.1 `machine_press` BlockEntity/Menu/Screen/Recipe scaffold. This pass intentionally avoids adding recipes whose input or output item families have not been migrated yet.

## 1.7.10 Source Files

- `com.hbm.inventory.recipes.PressRecipes`
  - Stores recipes in `HashMap<Pair<AStack, StampType>, ItemStack>`.
  - `getOutput(ItemStack ingredient, ItemStack stamp)` requires `stamp.getItem() instanceof ItemStamp`, obtains the `StampType`, and returns the first matching recipe for that stamp type.
  - Default stamp groups include `FLAT`, `PLATE`, `WIRE`, `CIRCUIT`, casing stamps `C357/C44/C50/C9`, and printing stamps `PRINTING1..PRINTING8`.
- `com.hbm.items.machine.ItemStamp`
  - Stores a fixed `StampType` per item and registers the stamp in a static `stamps` lookup.
  - Iron stamps use textures such as `stamp_iron_plate`, `stamp_iron_flat`, `stamp_iron_wire`, and `stamp_iron_circuit`.
- Relevant 1.7.10 resources:
  - `assets/hbm/textures/items/stamp_iron_plate.png`
  - `assets/hbm/textures/items/stamp_iron_flat.png`
  - `assets/hbm/textures/items/stamp_iron_wire.png`
  - `assets/hbm/textures/items/stamp_iron_circuit.png`

## Legacy Recipe Surface

- `PLATE`: iron, gold, titanium, aluminium, steel, lead, copper, advanced alloy, schrabidium, combine steel, gunmetal, weapon steel, saturnite, dura steel.
- `FLAT`: dust/gem compression and special item compression such as quartz, lapis, diamond, emerald, biomass, coke/graphite, resin, and briquettes.
- `WIRE`: generated for materials with wire autogen and matching ingot ore dictionary entry, outputting `wire_fine`.
- `CIRCUIT`: silicon billet to silicon circuit.
- `C357/C44/C50/C9`: gunmetal/weapon steel plate to casing variants.
- `PRINTING1..8`: paper to guide page variants.

## Current 1.20.1 Integration

- Existing recipe type: `hbm:press` with JSON fields `stamp`, `ingredient`, and `result`.
- Existing stamp item before this pass: `stamp_iron_plate`.
- This pass adds:
  - `stamp_iron_flat`
  - `stamp_iron_wire`
  - `stamp_iron_circuit`
- This pass adds only migrated-output `PLATE` recipes:
  - `minecraft:gold_ingot` -> `hbm:plate_gold`
  - `hbm:ingot_advanced_alloy` -> `hbm:plate_advanced_alloy`
  - `hbm:ingot_gunmetal` -> `hbm:plate_gunmetal`

## Deferred

- `wire_fine` output is not registered yet, so `WIRE` recipes are deferred despite the new stamp type.
- `circuit` multi-variant item is not registered yet, so the silicon `CIRCUIT` recipe is deferred.
- Casing stamps and printing stamps are deferred until casing/page item models and metadata replacement semantics are migrated.
- `plate_schrabidium`, `plate_combine_steel`, `plate_weaponsteel`, `plate_saturnite`, and `ingot_dura_steel` inputs or outputs are not fully present in the current clean port, so those plate recipes are deferred.

## Verification

- Run `.\gradlew.bat compileJava processResources --no-daemon`.
