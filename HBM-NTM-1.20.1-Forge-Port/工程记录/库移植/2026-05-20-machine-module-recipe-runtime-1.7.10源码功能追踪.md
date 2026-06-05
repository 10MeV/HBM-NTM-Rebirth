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

## 2026-06-04 新版源码差异补记

对比旧快照与新版 5714 源码：

- `GenericRecipe` 新增 `printNEIExtras()`，默认绘制 duration 与 consumption；`BlastFurnaceRecipe` / `PUREXRecipe` 覆写为机器专用 NEI 信息。现代 `GenericMachineRecipe` 的 display lines/JEI category 后续需要区分“通用说明”和“机器额外说明”。
- `AssemblyMachineRecipes` 新增 `enable528` 关闭时的 chip/chipBismoid/chipQuantum/atomicClock 等组装机配方，并给 cyclotron particle part 配方加入 `autoswitch.cyclotron` group。
- `ChemicalPlantRecipes` 新增 `chem.obsidian`、`chem.aggregate`、`chem.napalm`，并把 `chem.stone` 的 discover pool 改为 `.stone` 子池。
- 上游大量配方把 `ALLOY.*` 替换为 copper、steel、dura、gold、mingrade、titanium 等材料，现代 runtime 的旧配方覆盖率审计需要以新版 5714 recipe list 为准重算。

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

## 2026-05-24 继续推进：化工厂 machine_chemical_plant 运行时接入

- 1.7.10 对照：
  - `TileEntityMachineChemicalPlant`：
    - 槽 `0` 电池。
    - 槽 `1` blueprint/schematic。
    - 槽 `2-3` upgrades。
    - 槽 `4-6` 固体输入。
    - 槽 `7-9` 固体输出。
    - 槽 `10-12` 输入流体容器插槽。
    - 槽 `13-15` 输入流体容器返回槽。
    - 槽 `16-18` 输出流体容器插槽。
    - 槽 `19-21` 输出流体容器返回槽。
  - 旧机器拥有 3 个输入 tank 与 3 个输出 tank，默认容量均为 `24_000`。
  - 旧 NBT 使用 `power` / `maxPower`、`i0..i2`、`o0..o2`、`progress0`、`recipe0`、`didProcess/anim/frame` 语义。
  - `GUIMachineChemicalPlant` 使用 `gui_chemplant.png`，进度条、电量条、三输入/三输出 tank、recipe selector 入口均固定在旧坐标。
  - `RenderChemicalPlant` 渲染 OBJ 部件 `Base`、`Frame`、`Slider`、`Spinner`，处理时推进 `anim`。
- 本批现代接入：
  - 新增 `ChemicalPlantBlock`，让 `machine_chemical_plant` 从纯可见多方块升级为专用可运行多方块。
  - 新增 `ChemicalPlantBlockEntity`：
    - 使用现有 `HbmEnergyReceiver` / `HbmEnergyStorage` / `HbmEnergyUtil` 接入 HE 电池充电和邻接能量网络订阅。
    - 使用现有 `HbmFluidTank` / `ForgeFluidHandlerAdapter` / `HbmFluidItemTransfer` 接入 3 输入 tank、3 输出 tank、Forge fluid capability 与容器槽装卸。
    - 使用 `GenericMachineRecipeRuntime` 跑 `GenericMachineRecipe.Machine.CHEMICAL_PLANT`，恢复 `chem.battery*` 等 datapack 配方的机器运行入口。
    - 输出槽和容器返回槽禁止插入。
    - 保存旧 key：`Inventory`、`Energy`、`power`、`maxPower`、`i0..i2`、`o0..o2`、`progress0`、`recipe0`、`DidProcess`、`Anim`、`Frame`。
  - 新增 `ChemicalPlantMenu`，按 1.7.10 `ContainerMachineChemicalPlant` 坐标恢复 22 槽布局，并同步 power/progress/tank fill。
  - 新增 `ChemicalPlantScreen` 和 `ChemicalPlantRecipeSelectorScreen`，使用旧 `gui_chemplant.png` 与现有 `gui_recipe_selector.png`，通过 `TileControlPacket index=0 selection=<internal_name|null>` 选择 recipe。
  - 新增 `ChemicalPlantRenderer`，继续使用 `chemical_plant.obj` / `chemical_plant.png`，并恢复 `Slider`/`Spinner` 基础动画。
  - `ModBlockEntities` 新增 `CHEMICAL_PLANT`，并从 `LEGACY_VISIBLE_MACHINE` 列表移除 `machine_chemical_plant`，避免同一方块绑定两个 BE 类型。
  - `GenericMachineRecipe#getToastSymbol` 对 `CHEMICAL_PLANT` 返回化工厂方块，不再用电池方块占位。
- 现代等价边界：
  - 旧 `getConPos()` 的 12 个外圈端口订阅尚未完整迁移；当前先复用现有邻接能量网络订阅与 Forge fluid capability。后续需要在多方块代理/端口层补“核心 BE 的远端端口连接”。
  - 旧 `UpgradeManager` 的 speed/power/overdrive 倍率还未启用，因为现代 upgrade item family 与 tooltip 库仍未迁移。
  - blueprint pool gating 仍未启用，因为现代 blueprint/schematic 物品族尚未完成。
  - 旧渲染中的处理液体半透明 `Fluid` 部件与音频循环尚未补齐；本批先恢复基础模型部件动画和 GUI tank 显示。
  - 旧 `meteorite_sword_treated` 电池槽特殊变换暂未迁移，等待相关物品族存在后补。
- 验证：
  - 复制 1.7.10 资源：`assets/hbm/textures/gui/processing/gui_chemplant.png`。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-01 Recipe selector 通用约定收束

- 新增 `GenericMachineRecipeSelector`：
  - 集中旧 `GUIScreenRecipeSelector` 控制 tag 语义：`index=0`、`selection=<internal_name|null>`。
  - 提供 `selectionTag(...)`、`normalize(...)`、`isNullSelection(...)`、`canSelect(...)`、`recipes(...)`。
  - GUI selector 不直接解析 JSON，只通过 `GenericMachineRecipeRuntime.recipes(...)` / recipe manager 读取 runtime recipe。
- `AssemblyMachineBlockEntity` 与 `ChemicalPlantBlockEntity` 改为复用 selector helper：
  - 保存的 `recipe0` 仍是旧 internal name 字符串。
  - `null` 选择仍走 `GenericMachineRecipeRuntime.NULL_RECIPE`。
  - C2S receiver 现在要求玩家打开对应 Menu，并校验 internal name 存在后才接收。
- `AssemblyRecipeSelectorScreen` 与 `ChemicalPlantRecipeSelectorScreen` 改用 `ModMessages.sendTileControl(...)` 发送选择，不直接依赖 packet 构造。
- 验证：`.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-06-04 组装机/化工厂全功能推进：按旧库归属补 ModuleMachineBase 契约

- 本轮边界：
  - 只把 1.7.10 本来就是共享库/模块的行为补入现代共享层。
  - `ModuleMachineBase` 的 recipe/progress/blueprint pool/tank conform/auto-switch/输入槽合法性属于共享 runtime。
  - `ItemBlueprints` 与 `ItemMachineUpgrade`/`UpgradeManagerNT` 是旧版共享物品/升级库，迁入现代共享 item/runtime。
  - 组装机 `meteorite_sword_alloyed -> meteorite_sword_machined`、化工厂 `meteorite_sword_machined -> meteorite_sword_treated` 是各自 TE 的私有完成副作用，不放进共享库。
- 本轮现代接入：
  - `GenericMachineRecipeRuntime.update(...)` 对齐 `ModuleMachineBase#update/process`：
    - recipe 为旧 internal name 字符串。
    - pooled recipe 必须由蓝图槽 `ItemBlueprints.grabPool(...)` 匹配，否则 recipe 归 `null` 且进度清零。
    - 自动切换只按第一个固体输入槽匹配同 auto-switch group。
    - 每 tick 扣 `recipe.power * pow`，进度推进 `speed / recipe.duration`，完成后若仍能继续加工则保留剩余进度，否则清零。
    - tank 按 recipe fluid 输入/输出 conform 类型与压力，并按默认容量/配方用量动态扩容。
  - 输入槽匹配修正为 1.7.10 位置语义：第 `i` 个 recipe item input 只匹配第 `i` 个机器 input slot，不再 shapeless 混槽匹配。
  - 新增 `GenericMachineRecipeRuntime.isItemValidForCurrentRecipe(...)` 与 `isSlotClogged(...)`，用于机器/代理/搬运后续按旧 `ModuleMachineBase#isItemValid/isSlotClogged` 对齐。
  - 新增 `ItemBlueprints`：
    - NBT key `pool`。
    - 右键消耗纸复制，secret pool 不可复制。
    - tooltip 展示 pool 与可查到的 recipe 名。
  - 新增 `ItemMachineUpgrade` 与 `LegacyMachineUpgradeManager`：
    - 支持 `SPEED`、`POWER`、`OVERDRIVE` 三类及 tier。
    - 组装机/化工厂各自按旧源码公式计算 speed/pow。
- 本轮机器接入：
  - 组装机和化工厂 tickRecipe 改用共享 runtime。
  - 两台机器槽位合法性改回 1.7.10：电池、蓝图、升级、recipe 输入/流体容器槽按旧规则限制，输出/返回槽禁止插入。
  - 组装机去掉现代假动画 preview，只按 `didProcess` 驱动机械臂/ring。
- 仍需后续核对：
  - 旧音频循环/strike/start/stop 需要按旧客户端逻辑接入现代 sound helper。
  - 蓝图 NBT 变体贴图目前先保留默认贴图，后续补 model override 或 item color/renderer。
  - 1.7.10 全量配方数量远超当前 datagen 可映射内容，后续应继续用 `LegacyGenericRecipeImporter` 或逐批 datagen 迁入可解析配方。

## 2026-06-04 继续推进：运行时/选择页对齐 legacy source_order

- 1.7.10 对照：
  - `GenericRecipes` 使用 `recipeOrderedList` 保留注册顺序；组装机和化工厂选择按钮读取该顺序，而不是按 recipe name 排序。
  - 旧 `GenericRecipe#print()` 的展示内容来源于同一份 recipe 数据，NEI/GUI 不应各自重算不同说明。
- 本批接入：
  - `GenericMachineRecipeRuntime.recipes(...)` 改用 `GenericMachineRecipe.LEGACY_ORDER`，先按 `source_order`，无旧顺序时再按 recipe id 稳定排序。
  - 组装机/化工厂 recipe selector tooltip 改复用 `GenericMachineRecipe#getDisplayLines()`，与 JEI 展示说明共用库层文本。
  - 既有手写 datagen 的高阶电池、电容、plate 与空流体包配方补入已核对的 1.7.10 source order。
- 迁移边界：
  - 动态 `ass.package*` / `ass.unpackage*` 的旧顺序取决于 `Fluids.getInNiceOrder()`，不在手写 datagen 中猜测；应等 legacy template 导入或按旧流体顺序库确认后生成。
  - 当前运行时已经能消费现代 JSON；全量组装机/化工厂配方仍等待旧 `hbmAssemblyMachine.json` / `hbmChemicalPlant.json` 模板导入和缺失映射补齐。

## 2026-06-04 继续推进：组装机/化工厂配方显示与机器内渲染对齐

- 1.7.10 对照：
  - `GUIMachineAssemblyMachine` 与 `GUIMachineChemicalPlant` 在当前 recipe 存在时：
    - recipe icon 按 `recipe.getIcon()` 渲染到左下 recipe 按钮；
    - 对每个 recipe item input，若对应机器输入槽为空，则按旧 `extractForCyclingDisplay(20)` 轮播显示需求物品；
    - 需求物品上方再以 0.5 alpha 盖回 GUI 贴图对应的 16x16 槽位区域，形成旧版“虚影槽位”效果；
    - 该显示不要求玩家实际把物品放在同一视觉位置，但 runtime 的槽位匹配仍按 `ModuleMachineBase` 的第 i 输入槽契约处理。
  - `RenderAssemblyMachine` 在玩家距离 core 中心小于 35 格时，在机器中心上方渲染当前 recipe icon。
  - `RenderChemicalPlant` 在 `didProcess && recipe != null` 时渲染 OBJ `Fluid` part：
    - 优先使用 recipe output fluids 平均色；
    - 若没有 fluid output，则使用 input fluids 平均色；
    - 使用 `textures/models/machines/chemical_plant_fluid.png`，alpha 0.5；
    - 旧版还会用纹理矩阵按 `anim` 滚动 UV。
- 本批现代接入：
  - 新增 `LegacyRecipeGhostRenderer`，作为 GUI/recipe 显示通用 helper，避免组装机/化工厂各自硬写虚影逻辑。
  - `AssemblyMachineScreen`：
    - 继续使用旧 `gui_assembler.png`；
    - 对 menu slot `4..15` 接入 recipe item input 虚影轮播与半透明槽位覆盖。
  - `ChemicalPlantScreen`：
    - 继续使用旧 `gui_chemplant.png`；
    - 对 menu slot `4..6` 接入 recipe item input 虚影轮播与半透明槽位覆盖。
  - `AssemblyMachineRenderer`：
    - 按旧 35 格距离限制渲染当前 recipe icon；
    - recipe icon 来自 `GenericMachineRecipe#getIcon()`，不是只取第一个 output。
  - `ChemicalPlantRenderer`：
    - `didProcess` 且 recipe 存在时渲染 `Fluid` part；
    - 使用 `ObjMachineModels.CHEMICAL_PLANT_FLUID_TEXTURE` 与平均流体色，半透明 alpha 128。
- 仍需后续补齐：
  - GUI tank 渲染已通过 `LegacyFluidGuiRenderer` 使用每种 `FluidType#getTexture()` 材质贴图；若仍看到纯色，需要检查对应 `textures/gui/fluids/<fluid>.png` 是否缺失或资源路径是否被生成产物覆盖。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 渲染库能力接入：化工厂 Fluid part 旧 texture matrix 对齐

- 前置确认：
  - 现代 `LegacyWavefrontModel` 已有 `ObjRenderContext`、`LegacyTexturedRenderMode`、`UvTransform`、`withLegacyTextureMatrix(...)` 能力。
  - 旧化工厂 `RenderChemicalPlant` 的液体渲染不是机器私有纹理重算，而是 GL texture matrix 对已绑定 `chemical_plant_fluid.png` 进行平移：
    - U translate：`-anim / 100F`
    - V translate：`BobMathUtil.sps(anim * 0.1) * 0.1 - 0.25`
    - 其中 `sps(x) = sin(pi / 2 * cos(x))`
    - 同时 `glDepthMask(false)`，alpha 0.5。
- 本批现代接入：
  - `ChemicalPlantRenderer` 改为通过渲染库 `ObjRenderContext` 渲染 `Fluid` part：
    - `withRgba(avgFluidColor, 128)`
    - `withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE)`
    - `withLegacyTextureMatrix(1.0F, 1.0F, -anim / 100F, sps(anim * 0.1) * 0.1 - 0.25)`
  - 渲染调用走 `LegacyWavefrontModel#renderPart(..., ObjRenderContext)`，不在化工厂 renderer 中直接操作 UV 顶点或新建私有渲染路径。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 非配方缺口核查：音频与外部物品访问对齐

- 1.7.10 对照：
  - `TileEntityMachineAssemblyMachine` 客户端处理时：
    - 循环声为 `NTMSounds.ELECTRIC_MOTOR_LOOP`，玩家距离小于 50，volume `getVolume(0.5F)`，range `15F`，pitch `0.75F`。
    - 冲针 `angles[3]` 到 `-0.75` 时播放 `ASSEMBLER_STRIKE`，volume `0.5F`，pitch `1F`。
    - ring 选择新目标时播放 `ASSEMBLER_START`，volume `0.25F`，pitch `1.25F + rand * 0.25F`。
    - 网络反序列化中 `wasProcessing && !didProcess` 时播放 `ASSEMBLER_STOP`，volume `0.25F`，pitch `1.5F`。
  - `TileEntityMachineChemicalPlant` 只有处理循环声：玩家距离小于 30，volume `getVolume(1F)`，range `15F`，pitch `1F`；没有 start/strike/stop 一次性音效。
  - 旧 `ISidedInventory` 外部槽规则：
    - 组装机 `getAccessibleSlotsFromSide` 为 `{4..15,16}`，`canExtractItem` 只允许 slot `16` 或 `assemblerModule.isSlotClogged(i)`。
    - 化工厂 `getAccessibleSlotsFromSide` 为 `{4,5,6,7,8,9}`，`canExtractItem` 只允许 slot `7..9` 或 `chemplantModule.isSlotClogged(i)`。
    - `ModuleMachineBase#isSlotClogged` 只认 recipe 输入槽，并复用 `isItemValid`，因此 auto-switch group 的第一个输入不会被误判为堵塞。
- 本批现代接入：
  - `LegacyMachineAudioBridge` / `LegacyMachineAudio` 增加：
    - 带 volume/pitch 的循环声更新入口。
    - 客户端本地一次性方块音入口，统一做玩家距离检查与 `playLocalSound`。
  - `AssemblyMachineBlockEntity`：
    - 循环声距离改为 50，volume/pitch 对齐旧版。
    - ring 新目标、冲针命中、处理停止分别接入 start/strike/stop 音效。
    - item capability 改为旧可访问槽视图 `{4..15,16}`，外部抽取只允许输出或 `GenericMachineRecipeRuntime.isSlotClogged(...)` 判定的堵塞输入。
  - `ChemicalPlantBlockEntity`：
    - 循环声显式接入旧 volume/pitch。
    - item capability 改为旧可访问槽视图 `{4..9}`，外部抽取只允许固体输出 `7..9` 或堵塞固体输入 `4..6`。
  - 玩家 GUI/Menu 仍使用内部完整 `ItemStackHandler`，上述限制只作用于 Forge item capability 与多方块 dummy 代理转发后的外部自动化访问。
- 仍需后续核对：
  - 1.7.10 `muffled` 通用 TileEntityLoadedBase 声音衰减字段尚未作为现代共享机器能力迁移；本批不猜测来源，只按未 muffled 默认音量对齐。
  - 全量配方、蓝图模型变体和剩余 recipe 数据仍属后续配方迁移批次。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-05 装配机/化工厂选择闪退与 icon 渲染勘误

- 1.7.10 对照：
  - `GUIScreenRecipeSelector#regenerateRecipes/search` 遍历 `recipeOrderedList`，只加入：
    - 无 pool 的 recipe；
    - 或当前机器蓝图槽 `ItemBlueprints.grabPool(...)` 命中的 pooled recipe。
  - `ModuleMachineBase#update` 对当前 recipe 再做同一层 pool 校验，不匹配时 `recipe = "null"`、`progress = 0`。
  - `GUIMachineAssemblyMachine` / `GUIMachineChemicalPlant` 与 `RenderAssemblyMachine` 均使用 `GenericRecipe#getIcon()`，不是第一个 output。
  - `AssemblyMachineRecipes` 的流体包拆包 recipe 使用 `ItemFluidIcon.make(type, 32_000)` 作为 icon，而输出是空流体包。
  - `RenderAssemblyMachine` 对当前 recipe icon 的旧矩阵：
    - 先绕 Y 旋转 90 度并移动到机器中心上方；
    - 3D 方块物品下移 `1/16`；
    - 非 3D 方块物品下移 `2/16` 并缩放 `0.5`；
    - 普通物品绕 X 轴 `-90` 度并下移 `0.25`；
    - 最后统一缩放 `1.25`。
- 本批现代接入：
  - `GenericMachineRecipeSelector` 增加带 blueprint 的 `recipes(...)` / `canSelect(...)`，共享 1.7.10 pool 判断。
  - 装配机/化工厂 recipe selector 构造时按蓝图槽过滤列表，搜索改用 `GenericMachineRecipe#matchesSearch(...)`，保留旧 `source_order` 顺序。
  - 装配机/化工厂服务端控制接收也按当前蓝图槽校验，避免客户端或命令选择到 UI 中本不应出现的 pooled recipe。
  - 机器主 GUI 与 recipe selector 图标统一改为 `GenericMachineRecipe#getIcon()`。
  - 装配机世界内 recipe icon 渲染补回旧方块/普通物品分支，使普通合成物平躺在机器中心。
  - 现代暂未迁完整 `ItemFluidIcon` 物品族；流体包 package/unpackage datagen 先用带 HBM 流体 NBT 的满流体包作为 icon，对应旧版“显示该流体”意图，避免拆包配方显示空包或空白包。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过，并已重新 `processResources`。
  - 已抽查 `bin/main/data/hbm/recipes/assembly_machine/unpackage_water.json`，确认含有带流体 NBT 的 `icon`。
