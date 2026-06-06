# Legacy Generic Recipe Templates

Put 1.7.10 `SerializableRecipe` template files here before running `runData`.

Supported for the current machine migration slice:

- `hbmAssemblyMachine.json` or `_hbmAssemblyMachine.json`
- `hbmChemicalPlant.json` or `_hbmChemicalPlant.json`

The datagen provider imports these legacy generic recipe files into modern datapack JSON under
`src/generated/resources/data/hbm/recipes/<machine>/`, preserving the old `recipeOrderedList` index as
`source_order`.

`runData` also writes `reports/legacy_generic_recipe_import_report.json`. Use that report to check which
legacy templates were present, how many recipes imported, and which recipes were skipped because a legacy
item, ore dictionary entry, fluid, or machine limit is not mapped yet.
