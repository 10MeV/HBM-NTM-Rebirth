# HBM NTM 1.20.1 Forge Port

Clean migration scaffold for Hbm's Nuclear Tech on Forge 1.20.1.

Source priority:

1. 1.7.10 Hbm's Nuclear Tech source for gameplay semantics, IDs, naming and systems.
2. 1.20.1 unofficial high-edition port only as a reference for modern Forge structure and asset layout.

Current scope:

- Forge 1.20.1 / Java 17 Gradle project.
- `hbm` mod id.
- Main mod class, config, registries, resource metadata and DataGen entry.
- One legacy smoke-test item: `ingot_uranium`.

Useful commands:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat runData
.\gradlew.bat runClient
```
