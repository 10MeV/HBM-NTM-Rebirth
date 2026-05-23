# 机器模块与配方运行时 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 机器配方运行模块、过滤/模式匹配模块、数字显示模块。
- 该库服务 assembler、chemplant、fusion、plasma、PUREX、precision assembler 等机器。

## 1.7.10 源文件

- `src/main/java/com/hbm/module/ModulePatternMatcher.java`
- `src/main/java/com/hbm/module/ModuleBurnTime.java`
- `src/main/java/com/hbm/module/NumberDisplay.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineBase.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineAssembler.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineChemplant.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineFusion.java`
- `src/main/java/com/hbm/module/machine/ModuleMachinePlasma.java`
- `src/main/java/com/hbm/module/machine/ModuleMachinePrecAss.java`
- `src/main/java/com/hbm/module/machine/ModuleMachinePUREX.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipe.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipes.java`

## 旧版契约

- `ModulePatternMatcher`：
  - 模式：`exact`、`wildcard`、`bedrock`、或 ore dict key。
  - smart init 会优先选择 ingot/block/dust/nugget/plate 等矿辞。
  - `nextMode` 在 exact、bedrock、wildcard、矿辞列表间循环。
  - NBT key：`mode<i>`。
  - ByteBuf 同步每个 mode 字符串。
- `ModuleMachineBase`：
  - 保存能量接口、slots、input/output slots、input/output tanks。
  - `recipe` 字符串保存当前 recipe internal name。
  - `progress` 为 0..1。
  - `setupTanks` 按配方约束 tank 类型和压力。
  - `canProcess` 检查自动切换、能量、输入、输出容量。
  - `process` 扣能、推进进度、完成时消耗输入并产出。
  - `update` 处理 blueprint pool、额外条件、progress reset。
  - `isItemValid` 由当前配方和 auto switch group 决定。

## 迁移计划

- 先迁移 `GenericRecipe`/`GenericRecipes` 数据模型，再迁 `ModuleMachineBase`。
- `ModulePatternMatcher` 的 ore dict 模式要映射为 tag。
- ByteBuf 同步改为现代 packet/menu data slot 或自定义 payload。
- 具体机器模块应作为机器迁移时逐个接入。

## 验证清单

- 配方自动切换行为与旧版一致。
- tank 会随配方变化重设目标流体。
- blueprint pool 不匹配时 recipe 归 null 且进度清零。
- filter exact/wildcard/tag/bedrock 模式匹配正确。

## 2026-05-22 继续推进：先接机器配方数据，暂不接运行模块

- 本批关联 `recipes-common-loader`，为 `chemical_plant` / `assembly_machine` 注册了现代 recipe type。
- 运行时仍未迁移：
  - `ModuleMachineBase#canProcess/process/update`
  - blueprint pool 筛选
  - auto switch group
  - input/output tank 约束重设
  - `ModulePatternMatcher`
- 后续机器接入原则：
  - 机器 BlockEntity 不直接解析散落 JSON，应通过统一 HBM recipe query helper 读取 `GenericMachineRecipe`。
  - 旧 `recipe` internal name 字段应继续作为存档与 GUI/JEI 的稳定引用。
  - 高阶电池/电容机器配方先进入 datapack，再等化工厂/装配机运行模块完整后启用处理。

## 2026-05-22 继续推进：高阶电池/电容配方已进 datapack，runtime 仍封口

- 本批关联 `energy-mk2-network` 与 `recipes-common-loader`：
  - `ChemicalPlantRecipes` 中 5 条 `chem.battery*` 已生成现代 datapack JSON。
  - `AssemblyMachineRecipes` 中 5 条 `ass.capacitor*` 已生成现代 datapack JSON。
  - `GenericMachineRecipe` 已保留并同步旧 `internal_name`，后续 `ModuleMachineBase` 迁移时可继续使用旧 `recipe` 字符串语义。
- 仍未启用的运行时行为：
  - 不在 `GenericMachineRecipe#matches` 中直接匹配槽位。
  - 不在配方类中扣输入、重设 tank、推进 progress 或处理 auto switch。
  - 化工厂/装配机迁移时必须接入统一 runtime helper，再读取这些 recipe type。
- 验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-23 继续推进：开封 GenericMachineRecipe runtime，先接装配机

- 1.7.10 对照：
  - `ModuleMachineBase#update/canProcess/process` 是配方 runtime 的事实入口，`GenericRecipe` 本身只保存数据。
  - `ModuleMachineAssembler` 固定 12 个输入槽、1 个输出槽、1 个输入 tank、1 个输出 tank。
  - `TileEntityMachineAssemblyMachine` 槽位：
    - `0` 电池
    - `1` blueprint/schematic
    - `2-3` upgrades
    - `4-15` recipe input
    - `16` output
  - 旧装配机处理时：
    - `recipe` 字符串保存当前选择。
    - `progress` 为 `0..1` double。
    - 每 tick 扣 `recipe.power * pow`，推进 `speed / recipe.duration`。
    - tank 会按配方 conform 类型和压力，并扩容到 `max(当前 fill, 配方 fill * 2, 4000)`。
    - `maxPower` 会按当前配方调整到 `max(当前 power, recipe.power * 100, 100000)`。
- 本批现代接入：
  - 新增 `GenericMachineRecipeRuntime`：
    - `recipes/findByInternalName/findAutoSwitchRecipe`
    - `canProcess`
    - `consumeInputs`
    - `produceOutputs`
    - item 输入按旧装配机/化工厂模块的“第 i 个输入约束对应第 i 个输入槽”处理，不把配方输入混成 shapeless 聚合。
  - `GenericMachineRecipe.Machine#type()` 暴露为 runtime 查询入口。
  - `HbmEnergyStorage` 支持动态 `maxPower` 与 transfer rate，供旧机器随配方调整容量。
  - `AssemblyMachineBlockEntity` 接入 runtime：
    - 保存旧 NBT `progress0` / `recipe0`。
    - 使用旧槽位 `4-15` 输入、`16` 输出。
    - 每 tick 先从槽 `0` 电池充电并订阅邻接能量网络，再尝试处理选择的 recipe。
    - 根据 recipe conform/扩容输入输出 tank，解决 `ass.capacitorspark` 8000mB 输入/输出在默认 4000mB tank 下无法运行的问题。
    - 输出槽禁止外部插入。
- 现代等价边界：
  - `GenericMachineRecipe#matches` 仍保持 `false`，不把机器状态逻辑塞进 recipe 类。
  - 当前只接装配机底层 runtime；装配机 GUI/Menu/recipe selector 与 packet 还未迁移，因此实机仍需要后续 UI/命令入口选择 `recipe0`。
  - 化工厂当前目标工程还缺完整 `TileEntityMachineChemicalPlant` 等价 BE/Menu，本批只提供可复用 runtime helper，不假装化工厂已能处理 `chem.battery*`。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-23 继续推进：装配机 recipe 选择后端与命令验收入口

- 1.7.10 对照：
  - `GUIScreenRecipeSelector` 向 `IControlReceiver` 发送 NBT：
    - `index`
    - `selection`
  - `TileEntityMachineAssemblyMachine#receiveControl` 在 `index == 0` 时将 `assemblerModule.recipe = selection` 并标记 changed。
  - 选择器显示的是 `GenericRecipe#getInternalName()`，旧机器存档也保存 internal name 字符串。
- 本批现代接入：
  - `AssemblyMachineBlockEntity` 实现 `HbmTileSyncable`，支持现有 `TileControlPacket`：
    - `index == 0`
    - `selection=<internal_name|null>`
  - 新增 `AssemblyMachineBlockEntity.recipeSelectionTag(String)`，供后续 GUI recipe selector 直接复用。
  - `GenericMachineRecipeRuntime` 增加：
    - `recipeNames`
    - `hasRecipe`
  - 新增命令验收入口：
    - `/hbm machine assembly recipes`
    - `/hbm machine assembly info <pos>`
    - `/hbm machine assembly set <pos> <recipe>`
    - `/hbm machine assembly clear <pos>`
  - `assembly info` 输出当前 recipe、progress、电量、canProcess、输入/输出 tank 状态，便于实机验证 `ass.capacitor*` 处理链路。
- 同批修正：
  - `HbmRecipeProvider` 增加 `Items` import。
  - `GenericMachineRecipeBuilder#inputItem(ItemStack)` 支持带 count 的 ItemStack 输入，修复流体包 unpackaging datagen 编译缺口。
- 现代等价边界：
  - 还没有迁移完整 `GUIScreenRecipeSelector`/装配机 Menu/Screen；当前命令和 `TileControlPacket` 是后端与实机验证入口。
  - blueprint pool gating 仍未启用，因为现代 blueprint item/pool 系统尚未迁移。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-23 继续推进：装配机正式 Menu/Screen/recipe selector

- 1.7.10 对照：
  - `ContainerMachineAssemblyMachine`：
    - 电池 `0`
    - schematic `1`
    - upgrades `2-3`
    - input `4-15`
    - output `16`
  - `GUIMachineAssemblyMachine`：
    - 使用 `gui_assembler.png`
    - 点击左上 recipe 图标打开 `GUIScreenRecipeSelector`
    - 透明/灰显 recipe 提示、输入占位图、进度条、电量条、输入/输出 tank 条均来自旧纹理布局。
  - `GUIScreenRecipeSelector`：
    - 使用 `gui_recipe_selector.png`
    - 支持搜索、翻页、点击 recipe、清空搜索、关闭、返回。
- 本批现代接入：
  - `ModMenuTypes` 新增 `ASSEMBLY_MACHINE`。
  - `AssemblyMachineBlock#use` 现可为主核心开屏。
  - `AssemblyMachineMenu` 接入旧槽位布局和数据同步：
    - power/maxPower
    - progress
    - input/output tank fill/capacity
  - `AssemblyMachineScreen` 使用 1.7.10 原始 `gui_assembler.png`，恢复旧装配机布局、recipe 图标入口、进度、电量和 tank 提示。
  - `AssemblyRecipeSelectorScreen` 使用 1.7.10 原始 `gui_recipe_selector.png`，支持 recipe 搜索/翻页/点击选择/清空选择。
  - `AssemblyMachineBlockEntity` 仍通过 `TileControlPacket` 的 `index=0 selection=<internal_name|null>` 接收选择，和旧版 `receiveControl` 语义对齐。
- 现代等价边界：
  - 还没有迁移旧版 `ItemBlueprints`/pool gating 的完整交互，选择器目前按 internal name + 搜索工作。
  - 还没做旧版那种精细 tooltip/NEI 协作，但功能闭环已可实机验证。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
