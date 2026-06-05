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
- `PressRecipe#isSpecial()` returns `true` so these machine recipes stay out of the vanilla recipe book category sync; `machine_press` still queries them directly through `RecipeManager#getAllRecipesFor(hbm:press)`.
- Existing stamp item before this pass: `stamp_iron_plate`.
- This pass adds:
  - `stamp_iron_flat`
  - `stamp_iron_wire`
  - `stamp_iron_circuit`
- This pass adds only migrated-output `PLATE` recipes:
  - `minecraft:gold_ingot` -> `hbm:plate_gold`
  - `hbm:ingot_advanced_alloy` -> `hbm:plate_advanced_alloy`
  - `hbm:ingot_gunmetal` -> `hbm:plate_gunmetal`
- Later recipe-common-loader batches added:
  - `PRINTING1..8 + minecraft:paper -> hbm:page_of_page1..8`
  - `C9/C50 + forge:plates/gun_metal -> hbm:casing_small/large`
  - `C9/C50 + forge:plates/weapon_steel -> hbm:casing_small_steel/large_steel`
  - `PLATE + forge:ingots/weapon_steel -> hbm:plate_weaponsteel`
  - `PLATE + forge:ingots/schrabidium/combine_steel/saturnite/dura_steel -> matching migrated plates`
  - First `FLAT` batch:
    - quartz/lapis/diamond/emerald dust compression to vanilla gem/item outputs.
    - biomass to compressed biomass.
    - coke gem tag to graphite ingot.
    - coal/lignite/sawdust to legacy briquette meta-split items.
    - legacy `Blocks.log` meta `3` to modern `minecraft:jungle_log` for `ball_resin`.
  - First `WIRE` batch in code:
    - `WIRE + minecraft:gold_ingot -> hbm:wire_gold x8`, backed by old `wire_fine` meta `7900`.

## Deferred

- `wire_fine` output is not registered yet, so `WIRE` recipes are deferred despite the new stamp type.
- `circuit` multi-variant item is not registered yet, so the silicon `CIRCUIT` recipe is deferred.
- Casing stamps and printing stamps are now present for the ordinary gunmetal/weapon steel casing and page-printing chains; desh stamp durability/material variants remain deferred.
- `plate_schrabidium`, `plate_combine_steel`, `plate_weaponsteel`, `plate_saturnite`, and `ingot_dura_steel` item/tag落点与对应 `PLATE` recipe 已由 recipes-common-loader 后续批次补齐。
- `FLAT` 首批粉末/biomass/graphite/briquette/resin recipe 已由 recipes-common-loader 后续批次补齐。
- Remaining press surface still deferred: meteorite sword hardening, any still-unmigrated `FLAT` outputs, generated `WIRE` recipe matrix beyond gold, silicon `CIRCUIT` recipe, desh stamp family, ammo item runtime, and non-migrated material families.

## Verification

- Original slice: run `.\gradlew.bat compileJava processResources --no-daemon`.
- 2026-06-04 follow-up material/press batches: `.\gradlew.bat compileJava --no-daemon` and `.\gradlew.bat runData --no-daemon` passed.
- 2026-06-04 gold `WIRE` code pass: `.\gradlew.bat compileJava --no-daemon` and `.\gradlew.bat compileJava --rerun-tasks --no-daemon` passed; `runData --rerun-tasks` reached Forge datagen startup but failed before providers with `fml.modloading.missingclasses`, so generated `press/wire_gold.json` is pending.

## 2026-06-04 New 1.7.10 Source Diff Note

Comparison source: old snapshot under `源码包/old-code/Hbm-s-Nuclear-Tech-GIT-master` versus new upstream snapshot `1.0.27 BETA (5714)`.

- `PressRecipes` removes the `PLATE + ALLOY.ingot() -> plate_advanced_alloy` recipe. The modern press batch should no longer treat `plate_advanced_alloy` as a required 5714 press output.
- `ModItems` removes the normal `ingot_advanced_alloy`, `plate_advanced_alloy`, `powder_advanced_alloy`, `coil_advanced_alloy`, `coil_advanced_torus`, and `blades_advanced_alloy` registrations; related press/tag outputs should be reclassified as legacy-only or removed from the 5714 coverage target.
- Existing modern note above that added `hbm:ingot_advanced_alloy -> hbm:plate_advanced_alloy` now represents the older snapshot, not the current upstream target. Future coverage reports should mark this as superseded by 5714 unless the user explicitly wants backwards compatibility.
