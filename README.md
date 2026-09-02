# HBM-NTM: Rebirth

HBM-NTM: Rebirth is a Forge 1.20.1 rebuild of Hbm's Nuclear Tech Mod. It starts from the original 1.7.10 source tree and preserves the mod's major gameplay identity, while also serving as a place for new systems, balance changes, expanded content, and Rebirth-specific design work.

The port uses modern Forge systems where Minecraft 1.20.1 requires different registration, rendering, data, networking, menu, and capability code. Legacy behavior remains the baseline for migrated features, but this project is not limited to one-to-one reproduction.

## Status

This is an active rebuild project, not a complete 1.7.10 parity release yet. Ported systems are documented in local engineering notes, and the migration policy is source-first: gameplay behavior, item ids, machine contracts, recipes, renderer semantics, and resource choices are checked against the original 1.7.10 code before implementation.

Rebirth may also introduce original content and design changes after the relevant legacy behavior is understood. New features should be documented clearly, kept compatible with the modern codebase, and marked as Rebirth additions rather than accidental deviations from 1.7.10 behavior.

Current target:

- Minecraft `1.20.1`
- Minecraft Forge `47.2.32`
- Java `21` for Gradle and Forge client/server runs (`--release 17` for published bytecode)
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
run-client.bat
```

The project pins Minecraft run tasks to the JDK 21 toolchain. Do not launch
the client with an external JDK 17.0.8 runtime; that combination can terminate
inside `jvm.dll` with `EXCEPTION_ACCESS_VIOLATION` during world creation.
The mod also rejects Java runtimes older than 21 during loading, before world
generation can reach that native-crash path. Gradle settings cannot change the
JVM already selected by PCL or another external launcher.

Build outputs are written to `build/libs`.

## Project Layout

- `src/main/java` - modern Forge 1.20.1 Java source.
- `src/main/resources/assets/hbm_ntm_rebirth` - client assets under the Rebirth namespace.
- `src/main/resources/data/hbm_ntm_rebirth` - data pack resources under the Rebirth namespace.
- `src/generated/resources` - generated models, tags, lang, loot tables, recipes, and other datagen outputs.
- `legacy_recipes` and `reports` - migration/import support data and audit output.
- Local engineering notes, excluded from Git, are used during development to preserve 1.7.10 behavior contracts and record Rebirth-specific design decisions.

## Source And Attribution

This project is a modified and modernized work derived from [Hbm's Nuclear Tech Mod](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT) for Minecraft 1.7.10. The Bobcat and the original HBM/NTM contributors retain credit and copyright in the original code, assets, gameplay design, and documentation used by Rebirth.

Rebirth also uses and adapts source from [HBM Modernized](https://github.com/Raptor324/HBM-Modernized) as a Forge 1.20.1 reference implementation. Credit and copyright for that work remain with Raptor324/RaptorDev and the HBM Modernized contributors.

HBM-NTM: Rebirth is an independent port and is not an official release of either upstream project. Rebirth contributors are responsible only for the Forge 1.20.1 porting work, modifications, new implementation code, and Rebirth-specific additions in this repository. The repository history records the authorship and dates of those modifications.

This repository intentionally keeps the Java package family `com.hbm.ntm` for source continuity, while using the Forge mod id `hbm_ntm_rebirth` for the modern mod namespace.

Detailed attribution, source links, and retained upstream notices are in [`NOTICE.md`](NOTICE.md) and [`third_party_licenses/`](third_party_licenses/README.md). Those notices and the applicable full license texts are packaged into every release JAR under `META-INF/third_party_licenses/`.

## Compatibility Notes

The original 1.7.10 mod contains broad systems that interact deeply with Minecraft internals, including machines, energy, fluids, radiation, explosions, custom rendering, entities, and world effects. Rebirth ports and rebuilds these systems in stages, then expands on them where the new version has a clear design reason to do so. Until a stable release is declared, expect missing content, changed implementation details, migration diagnostics, and experimental Rebirth additions.

When contributing, prefer small, traceable changes:

1. Read the relevant local engineering note or design note first, if one exists.
2. Check the original 1.7.10 source behavior.
3. Keep old ids and gameplay semantics where possible.
4. Run at least `gradlew.bat compileJava processResources --no-daemon`.
5. Document whether a change is legacy parity work, a modern implementation adjustment, or a Rebirth-specific addition.

## License

This repository uses a source-based multi-license structure. The licenses are divided as follows:

| Scope | License applied to that scope | Credit / copyright | License and notice location |
| --- | --- | --- | --- |
| The combined HBM-NTM: Rebirth source and compiled JAR | `GPL-3.0-only` | All applicable upstream and Rebirth copyright holders | [`LICENSE`](LICENSE), [`NOTICE.md`](NOTICE.md) |
| Code, assets, data, designs, and documentation migrated or adapted from original HBM/NTM 1.7.10 | `LGPL-3.0-only`; conveyed within the GPLv3 combined work while retaining its original notice | The Bobcat and HBM/NTM contributors | [`third_party_licenses/HBM_ORIGINAL.md`](third_party_licenses/HBM_ORIGINAL.md), [`LICENSE.LESSER`](LICENSE.LESSER) |
| Source incorporated or adapted from HBM Modernized | `GPL-3.0-only` | Raptor324/RaptorDev and HBM Modernized contributors | [`third_party_licenses/HBM_MODERNIZED.md`](third_party_licenses/HBM_MODERNIZED.md), [`LICENSE`](LICENSE) |
| Rebirth-authored porting changes and original additions | `GPL-3.0-only` as distributed in this repository, unless a particular file carries another compatible license notice | The individual Rebirth contributors recorded by Git history | [`LICENSE`](LICENSE), repository history |
| Separately installed build/runtime dependencies | Their respective upstream licenses; not bundled or relicensed by Rebirth | Their respective authors and contributors | [`third_party_licenses/DEPENDENCIES.md`](third_party_licenses/DEPENDENCIES.md) and each dependency's distribution |

The Forge `mods.toml` license field names `GPL-3.0-only` because it describes the effective license of the combined distributable mod. It is not written as `GPL AND LGPL` or `GPL OR LGPL`: `AND` would incorrectly claim that every part is simultaneously under both licenses, while `OR` would incorrectly offer an LGPL-only option for HBM Modernized GPLv3 material.

HBM-NTM: Rebirth as a combined work is therefore distributed under the **GNU General Public License version 3 only** (`GPL-3.0-only`). HBM Modernized source used by this project is GPLv3, so the former `LGPL-3.0-only` project-wide label was not sufficient for the combined distribution. The authoritative GPLv3 text is in [`LICENSE`](LICENSE).

Source and assets originating from the original 1.7.10 HBM/NTM distribution remain under **GNU Lesser General Public License version 3 only** (`LGPL-3.0-only`). Its original GPL + LGPL license-file arrangement is retained through [`LICENSE`](LICENSE) and [`LICENSE.LESSER`](LICENSE.LESSER), and both texts are reproduced in the packaged third-party notice directory. Applying GPLv3 to the combined Rebirth work does not erase upstream copyright or the LGPL notice on those portions.

Anyone distributing a compiled Rebirth JAR must also make the complete corresponding source, including build scripts, available by a GPLv3-compliant method. The canonical source repository is [github.com/10MeV/HBM-NTM-Rebirth](https://github.com/10MeV/HBM-NTM-Rebirth); release binaries should be paired with the matching source tag or source archive.

This summary is not a substitute for the license texts. See [`NOTICE.md`](NOTICE.md) and [`third_party_licenses/`](third_party_licenses/README.md) before redistributing source, assets, or binaries.

Minecraft, Forge, and any third-party dependencies or tools retain their own licenses and are not relicensed by this project.
