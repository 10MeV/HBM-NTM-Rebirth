# Radiation Core Parity Audit - 2026-05-23

Scope:

- Compare the current 1.20.1 radiation library against the 1.7.10 source, using 1.7.10 as the factual source.
- Focus on library-owned behavior: entity contamination data, Simple chunk radiation, hazard protection, long-term disease counters, and runtime event wiring.
- Do not treat the 1.20.1 reference port as parity evidence.

Primary 1.7.10 source files checked:

- `com.hbm.util.ContaminationUtil`
- `com.hbm.extprop.HbmLivingProps`
- `com.hbm.handler.EntityEffectHandler`
- `com.hbm.handler.radiation.ChunkRadiationManager`
- `com.hbm.handler.radiation.ChunkRadiationHandlerSimple`
- `com.hbm.config.RadiationConfig`
- `com.hbm.hazard.type.HazardTypeAsbestos`
- `com.hbm.hazard.type.HazardTypeCoal`
- `com.hbm.hazard.type.HazardTypeBlinding`
- `com.hbm.util.ArmorRegistry`
- `com.hbm.util.ArmorUtil`

Current 1.20.1 files checked:

- `com.hbm.ntm.radiation.RadiationUtil`
- `com.hbm.ntm.radiation.RadiationData`
- `com.hbm.ntm.radiation.ChunkRadiationManager`
- `com.hbm.ntm.radiation.RadiationSavedData`
- `com.hbm.ntm.radiation.ArmorUtil`
- `com.hbm.ntm.event.CommonForgeEvents`
- `com.hbm.ntm.config.RadiationConfig`
- migrated radiation/gas blocks and damage type data.

## Confirmed Aligned

- `RadiationUtil#contaminate` keeps the old ordering: add radEnv first for radiation, then run player armor/creative/tick-age gates, then radiation immunity, then apply resistance unless `RAD_BYPASS`.
- Player radiation is clamped to `0..2500`, and digamma is clamped to `0..10`.
- Contamination effects use the legacy `hfr_cont_count` plus `cont_<index>` NBT shape and compute `maxRad * time / maxTime`.
- Simple chunk radiation uses the 3x3 spread weights `0.6`, `0.075`, `0.025`, and decays old entries with `*0.99 - 0.05`.
- Chunk NBT key remains `hfr_simple_radiation`.
- Radiation fog threshold/chance and particle mass are now aligned with the old `radFog` behavior at the library boundary.
- Gas-mask `HazardClass` helmet/filter checks now exist as the protection bridge for gases, toxins, MKU, and item hazards.

## Corrected In This Pass

- Simple chunk world mutation now follows the old handler constants instead of modern config values:
  - 5 randomly sampled chunks per server tick.
  - 10 operation batches per sampled chunk.
  - hard threshold `10 RAD`.
  - `RADWORLD_01_amount` and `RADWORLD_02_minimum` remain config mirrors but are not used by the Simple handler, matching 1.7.10 source behavior.
- Inventory asbestos hazard now uses `HazardClass.PARTICLE_FINE` helmet/filter protection:
  - unprotected: increments asbestos by `min(level, 10)`.
  - protected: damages gas-mask filter by `(int) level`.
- Inventory coal hazard now uses `HazardClass.PARTICLE_COARSE` helmet/filter protection:
  - unprotected: increments black lung by `min(level * stackSize, 10)`.
  - protected: has `1 / max(65 - stackSize, 1)` chance to damage filter by `(int) level`.
- Inventory blinding hazard now respects `HazardClass.LIGHT`.
- `RadiationData` now mirrors old `HbmLivingProps` disable gates and lethal thresholds:
  - asbestos getter/setter no-op under `DISABLE_ASBESTOS`.
  - black-lung getter/setter no-op under `DISABLE_COAL`.
  - asbestos reaches `60 * 60 * 20`, resets to 0, and deals lethal `hbm:asbestos` damage.
  - black lung reaches `2 * 60 * 60 * 20`, resets to 0, and deals lethal `hbm:blacklung` damage.
- Added `hbm:asbestos` and `hbm:blacklung` damage type data and bypass tags.

## Follow-Up Runtime Corrections

- Nether ambient radiation now uses the old `EntityEffectHandler#handleRadiationFX` contract:
  - start from chunk radiation.
  - if the dimension is Nether and `hellRad` is higher, use `hellRad` as the effective radiation.
  - contaminate all eligible living entities through the same radiation path.
  - do not apply a separate player-only Nether dose.
- The `200..399` stored-radiation band no longer applies a radiation potion effect by itself. This band now only has the old random confusion/weakness effects; explicit radiation potion sources still work through `HbmPotion.radiation` parity.
- `RadawayMobEffect` now only subtracts stored radiation, matching `HbmPotion.radaway`. It no longer clears the radiation potion or mutates `radEnv`.
- `RadiationUtil#applyDigammaDirect` no longer goes through generic contamination. It now increments digamma directly after the old direct checks: positive amount, not radiation-immune, and not creative player.

## Diffusion Timing Correction

- Chunk radiation diffusion now uses a per-dimension timer instead of a single global counter.
- `LevelEvent.Unload` clears that per-dimension timer, so one dimension cannot stall or bunch another dimension's fog/diffusion cycle.

## Still Not Fully Aligned

- PRISM, NT, 3D, and Blank chunk radiation handlers are still not migrated. The current port intentionally mirrors the default Simple handler only.
- Current `RadiationSavedData` also has a modern world-level saved-data store. Legacy chunk data is still written to chunk NBT, but the saved-data store is a modern persistence layer rather than a 1.7.10 save shape.
- Simple chunk unload removes the proper chunk key. 1.7.10 has a likely bug removing `event.getChunk()` from a map keyed by `ChunkCoordIntPair`; the port intentionally avoids preserving that leak.
- Creeper nuclear transformation and duck-to-quackos transformation are incomplete because the target legacy entities are not migrated.
- Client player radiation sickness overlay/effects are not exact old `effectNT` particle paths yet; modern particles are used where entity effects were bridge-migrated.
- Pollution coupling is not fully migrated:
  - soot pollution is not folded into lung disease cough calculation.
  - lead/heavy-metal and poison pollution exposure need a pollution-library pass.
- Actual gas mask/filter item classes and install UI are not migrated. The `GasMask` interface bridge is ready but most registered items are still name/resource-level placeholders.
- Armor mod recursion (`ArmorModHandler` helmet-only attachment masks/filters) is not migrated.
- Some old direct armor `HazardClass` registrations for full armor sets and external compatibility gear still need an armor migration pass.
- Many non-block radiation sources remain owned by their own systems: reactors, machines, fluids, worldgen hotspots, entities, bombs, and item-specific behaviors.

## Follow-Up Plan

1. Migrate the actual gas mask/filter items and filter install/remove NBT contract.
2. Audit `HazardRegistry` registrations against 1.7.10 item and block hazard tables, especially asbestos/coal/blinding/hot/hydro/explosive entries.
3. Finish radiation entity transformations once `EntityCreeperNuclear`, `EntityDuck`, and `EntityQuackos` are migrated.
4. Split a pollution-library pass for soot, poison, and heavy-metal exposure so lung disease and pollution poisoning match `EntityEffectHandler`.
5. Generate a second source table for non-block radiation emitters: machines, fluids, bombs, entities, and worldgen.

Verification:

- 2026-05-23 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
