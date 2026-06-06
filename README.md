# HBM-NTM: Rebirth

HBM-NTM: Rebirth is a Forge 1.20.1 port of Hbm's Nuclear Tech Mod. The project is rebuilt primarily from the original 1.7.10 source tree, with modern Forge systems used where Minecraft 1.20.1 requires different registration, rendering, data, networking, menu, and capability code.

The in-game display name is `HBM-NTM: Rebirth`. The technical Forge mod id and resource namespace are `hbm_ntm_rebirth`; Forge mod ids cannot contain uppercase letters or a colon, so `HBM-NTM:Rebirth` is not a valid `modId`.

## Status

This is an active migration project, not a complete 1.7.10 parity release yet. Ported systems are documented in `工程记录/`, and the migration policy is source-first: gameplay behavior, item ids, machine contracts, recipes, renderer semantics, and resource choices are checked against the original 1.7.10 code before implementation.

Current target:

- Minecraft `1.20.1`
- Minecraft Forge `47.2.32`
- Java `17`
- Gradle / ForgeGradle project layout

## Building

On Windows, run:

```bat
build.bat
```

Or directly:

```bat
gradlew.bat build --no-daemon
```

Useful development commands:

```bat
gradlew.bat compileJava processResources --no-daemon
gradlew.bat runData --no-daemon
gradlew.bat runClient --no-daemon
```

Build outputs are written to `build/libs`.

## Project Layout

- `src/main/java` - modern Forge 1.20.1 Java source.
- `src/main/resources/assets/hbm_ntm_rebirth` - client assets under the Rebirth namespace.
- `src/main/resources/data/hbm_ntm_rebirth` - data pack resources under the Rebirth namespace.
- `src/generated/resources` - generated models, tags, lang, loot tables, recipes, and other datagen outputs.
- `legacy_recipes` and `reports` - migration/import support data and audit output.
- `工程记录` - engineering trace documents used to preserve 1.7.10 behavior contracts during the port.

## Source And Attribution

This project is derived from and guided by Hbm's Nuclear Tech Mod for Minecraft 1.7.10. Credit belongs to the original HBM/NTM authors and contributors for the original mod, code, assets, gameplay design, and documentation. Rebirth port contributors are responsible for the Forge 1.20.1 migration work in this repository.

This repository intentionally keeps the Java package family `com.hbm.ntm` for source continuity, while using the Forge mod id `hbm_ntm_rebirth` for the modern mod namespace.

## Compatibility Notes

The original 1.7.10 mod contains broad systems that interact deeply with Minecraft internals, including machines, energy, fluids, radiation, explosions, custom rendering, entities, and world effects. Rebirth ports these systems in stages. Until a parity release is declared, expect missing content, changed implementation details, and migration-only diagnostics.

When contributing, prefer small, traceable changes:

1. Read the relevant `工程记录` document first.
2. Check the original 1.7.10 source behavior.
3. Keep old ids and gameplay semantics where possible.
4. Run at least `gradlew.bat compileJava processResources --no-daemon`.
5. Update the engineering trace document when a migration decision or bug fix changes the recorded contract.

## License

HBM-NTM: Rebirth follows the same license family as the original 1.7.10 Hbm's Nuclear Tech source distribution.

The software is licensed under the GNU Lesser General Public License version 3. The full GNU GPL v3 text is provided in `LICENSE`, and the GNU LGPL v3 additional terms are provided in `LICENSE.LESSER`, matching the original project's GPL + LGPL license-file arrangement.

In short: you may run, study, modify, and redistribute the software and modified versions under the terms of LGPLv3. The LGPL's linking permissions apply as described by the license text. This summary is not a substitute for the licenses themselves; the files `LICENSE` and `LICENSE.LESSER` are authoritative.

Minecraft, Forge, and any third-party dependencies or tools retain their own licenses and are not relicensed by this project.
