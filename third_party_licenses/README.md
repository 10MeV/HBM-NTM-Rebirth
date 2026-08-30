# Third-Party Source and License Notices

This directory records third-party source and assets incorporated into or adapted by HBM-NTM: Rebirth. Keep it with source distributions.

The Gradle `jar` task packages this directory as `META-INF/third_party_licenses/` and adds the complete license texts at build time:

- `hbm-original/LICENSE` and `hbm-original/LICENSE.LESSER` reproduce the original HBM/NTM GPLv3 + LGPLv3 file arrangement.
- `hbm-modernized/LICENSE` reproduces the GPLv3 text supplied by HBM Modernized.
- `HBM_ORIGINAL.md` and `HBM_MODERNIZED.md` contain attribution and source links.
- `DEPENDENCIES.md` distinguishes external build/runtime dependencies that are not bundled into the Rebirth JAR.

Do not remove an upstream notice, author credit, source link, or license text when copying or redistributing covered material. Add a new notice here before incorporating source or assets from another project.

This is a source-based multi-license layout. It does not grant a choice to redistribute the complete Rebirth JAR under LGPL alone: the combined work includes GPLv3-covered HBM Modernized material and must satisfy GPLv3.
