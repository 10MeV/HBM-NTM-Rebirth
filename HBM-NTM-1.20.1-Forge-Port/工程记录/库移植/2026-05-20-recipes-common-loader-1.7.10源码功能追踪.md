# 配方通用抽象与加载器 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 配方通用输入抽象、可序列化配方、机器配方集合与大量机器配方注册类。
- 该库是后续机器配方迁移的核心数据层。

## 1.7.10 源文件

- `src/main/java/com/hbm/inventory/RecipesCommon.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipe.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipes.java`
- `src/main/java/com/hbm/inventory/recipes/loader/SerializableRecipe.java`
- `src/main/java/com/hbm/inventory/recipes`
- `src/main/java/api/hbm/recipe/IRecipeRegisterListener.java`
- `src/main/java/com/hbm/util/CompatRecipeRegistry.java`

## 旧版契约

- `RecipesCommon.AStack`：
  - 抽象输入项。
  - `matchesRecipe(ItemStack, ignoreSize)`。
  - `copy`、`copy(int)`。
  - `extractForNEI` 与 cycling display。
- `ComparableStack`：
  - item + meta + stacksize。
  - wildcard meta 支持。
  - hash 使用注册名，避免 id 变化。
- `OreDictStack`：
  - 基于 ore dict key 匹配。
- `NBTStack`：
  - 基于 NBT 匹配特殊输入。
- `GenericRecipe`：
  - 支持 item/fluid 输入输出、power、duration、pool、autoSwitchGroup。
- `GenericRecipes`：
  - 保存 recipe list、name map、auto switch groups。
- 大量机器配方类仍是 Java 注册，部分支持 JSON/可序列化。

## 迁移计划

- 先定义现代 `HbmIngredient`，覆盖 item、tag、NBT/data、fluid。
- ore dict 全部映射到 tag。
- `GenericRecipe` 采用高版本兼容路线：数据包负责声明、重载与分发，HBM 配方库负责旧版匹配语义与机器运行时查询。
- JEI 显示层不要直接依赖旧 NEI handler；应读统一配方数据。

## 高版本兼容移植法

现代端不完整照搬 1.7.10 的 Java recipe loader。目标结构是 `datapack-backed runtime abstraction`：

- 数据包层：
  - 普通 crafting、熔炉与 Forge/vanilla 可表达的配方优先使用原版 `RecipeSerializer`/`RecipeType` 与 JSON。
  - HBM 机器配方使用 `data/hbm/recipes/.../*.json` 声明，并通过现代 `RecipeSerializer`/codec 加载。
  - ore dictionary 输入迁移为 tag ingredient。
  - 数据生成应生成稳定 recipe id，避免后续 JEI、机器缓存和兼容层引用漂移。
- HBM 运行时语义层：
  - 保留现代 `HbmIngredient`，用于承接旧 `AStack`/`ComparableStack`/`OreDictStack`/`NBTStack` 的匹配语义。
  - `ComparableStack` 的旧 item + meta + wildcard 语义不得直接丢给原版 `Ingredient`；若旧 metadata 已拆成现代独立物品，需要建立 legacy id/meta 到现代 item/tag 的映射。
  - NBT/data component 匹配、容器状态、特殊燃料棒/RTG/RBMK/PWR/Watz 等动态输入必须通过 HBM ingredient 或 transformer 层表达。
  - 流体输入输出必须走 HBM fluid 兼容层，不能让机器直接依赖外部 Forge `FluidStack` 作为唯一事实来源。
  - `GenericRecipe` 的 `power`、`duration`、`pool`、`autoSwitchGroup`、internal name 等机器语义字段仍由 HBM recipe model 保存。
  - 机器运行时通过统一 query helper 查找配方，处理输入槽、流体槽、blueprint pool、自动切换组、耗能和时长，不在各机器中散落 JSON 解析逻辑。
- 兼容与显示层：
  - JEI 读取统一 HBM recipe data/model，不搬旧 NEI handler。
  - 服务端 datapack reload 后，客户端 UI/JEI 需要通过现代 recipe 同步或自定义同步获得一致数据。
  - `CompatRecipeRegistry` 迁移时应向统一 recipe model 注册或生成 datapack 数据，不再直接修改散落的旧 Java map。

迁移边界：

- 可以由高版本数据包代替：配方声明、配方重载、tag 输入、普通 item 输出、可序列化机器 recipe 数据。
- 不能只靠数据包代替：旧 meta/wildcard 匹配、NBT/data 条件、HBM fluid 语义、机器运行时 recipe selection、auto switch group、recipe internal name 稳定性、客户端显示/同步缓存。

阶段建议：

1. 先建立 `HbmIngredient` 与 legacy id/meta 映射表，保证旧输入语义有落点。
2. 为首批机器实现现代 recipe type/serializer，字段覆盖 item inputs、fluid inputs、outputs、power、duration、pool、autoSwitchGroup、internal name。
3. 提供 Java bootstrap/data generator 作为过渡，但最终输出 datapack JSON；不要长期维护独立 Java recipe map。
4. 机器、JEI、兼容层全部读取同一套 HBM recipe model。
5. 每迁移一类旧机器配方，先补 trace 中的旧配方来源、旧 meta/NBT/流体需求和当前现代映射缺口。

## 验证清单

- Comparable/tag/NBT 输入匹配结果与旧版一致。
- recipe internal name 稳定。
- auto switch group 可被机器模块读取。
- 配方可在服务端和客户端同步。
- datapack reload 后机器查询与 JEI 显示一致。
- legacy id/meta 映射缺失时应显式跳过或报错，不得静默映射到错误物品。

## 2026-05-22 继续推进：GenericMachineRecipe 最小数据包落点

- 本批接入：
  - 新增现代 `GenericMachineRecipe`，作为旧 `GenericRecipe` 的最小数据载体。
  - 注册 `hbm:chemical_plant` 与 `hbm:assembly_machine` 两个 recipe type/serializer。
  - 字段覆盖：
    - `internal_name`
    - `duration`
    - `power`
    - `input_items`
    - `input_fluids`
    - `output_items`
    - `output_fluids`
    - `pools`
    - `auto_switch_group`
  - item 输入暂用现代 `Ingredient + count`，可承接普通 item/tag 输入；旧 meta 拆分输入应通过统一 legacy 映射或 tag 表达。
  - fluid 输入/输出使用当前 HBM `FluidType` 名称序列化，保留 pressure 字段入口。
- 迁移边界：
  - `matches` 暂不执行机器匹配，避免在机器模块 runtime 未迁移前把槽位/蓝图/流体罐逻辑散落到 recipe 类里。
  - 暂不实现 chance output、NBTStack、ComparableStack wildcard、auto switch runtime 查询。
  - 当前目标是让数据包、同步、JEI/机器后续查询有统一事实来源。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 继续推进：tag 输入、internal name 同步与首批旧 ore dict 桥

- 本批接入：
  - `GenericMachineRecipe` 现在把 `internal_name` 保存为运行时字段，并在网络同步中读写，避免服务端/客户端或后续 JEI 查询只能依赖 recipe id。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 增加：
    - `assembly(...)`
    - `inputTag(TagKey<Item>, count)`
    - `outputFluid(FluidType, amount)`
  - 首批旧 `OreDictStack` 输入不再硬指向单个 item，而是生成现代 tag ingredient。
- 已落地的旧 ore dict -> tag 映射：
  - `ANY_PLASTIC.ingot()` -> `forge:ingots/any_plastic`
  - `ANY_HARDPLASTIC.ingot()` -> `forge:ingots/any_hardplastic`
  - `ANY_BISMOIDBRONZE.plateCast()` -> `forge:cast_plates/any_bismoid_bronze`
  - `GOLD.wireFine()` -> `forge:wires/gold`
  - `GOLD/NB/BSCCO.wireDense()` -> `forge:dense_wires/gold|niobium|bscco`
  - `LI/CO/NA/SA326.dust()` -> `forge:dusts/lithium|cobalt|sodium|schrabidium`
  - `circuit CHIP_QUANTUM` -> `forge:circuits/chip_quantum`
- 当前限制：
  - 这仍是数据包表达层；旧 `AStack.matchesRecipe` 的 NBT、wildcard meta、cycling display、container item 和 auto switch runtime 尚未迁移。
  - `circuit_chip_quantum`、dense wire 和 cast plate 是旧 meta/autogen 拆分入口，完整 meta 映射表应后续扩展成通用 `HbmIngredient`/legacy map。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 继续推进：HbmIngredient 与通用 legacy meta 映射入口

- 本批接入：
  - 新增 `com.hbm.ntm.recipe.HbmIngredient`，作为旧 `RecipesCommon.AStack` / `ComparableStack` / `OreDictStack` 的现代输入承接层。
  - `HbmIngredient` 当前覆盖：
    - 原版 `Ingredient` item/tag 匹配。
    - `count` 数量要求。
    - `exact_stack` NBT 精确匹配。
    - `legacy_id` + `legacy_meta` 记录字段，用于旧单 item + metadata 被现代独立 ID 拆分后的追踪。
  - `GenericMachineRecipe` 的 item 输入从内部 `ItemInput` record 改为 `HbmIngredient`，网络同步同时保留 legacy id/meta。
  - 新增 `LegacyMetaItemMappings`，当前首批落地：
    - `hbm:battery_pack` meta `0..11` -> `battery_redstone`、`battery_lead`、`battery_lithium`、`battery_sodium`、`battery_schrabidium`、`battery_quantum`、`capacitor_copper`、`capacitor_gold`、`capacitor_niobium`、`capacitor_tantalum`、`capacitor_bismuth`、`capacitor_spark`。
    - `hbm:battery_sc` meta `0..9` -> `battery_sc.empty`、`battery_sc.waste`、`battery_sc.ra226`、`battery_sc.tc99`、`battery_sc.co60`、`battery_sc.pu238`、`battery_sc.po210`、`battery_sc.au198`、`battery_sc.pb209`、`battery_sc.am241`。
  - 旧 `HbmLegacyBatteryMaps` 保留为兼容门面，但查询委托到 `LegacyMetaItemMappings`，避免后续配方、战利品、hazard、物品迁移各自维护一套 battery switch。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 现在通过 `HbmIngredient` 生成 item 输入，并新增 `inputLegacyMeta(...)` / `outputLegacyMeta(...)` 供后续机器配方直接按旧 id/meta 编写。
- 迁移边界：
  - 本批只迁共享映射入口和机器配方输入模型；尚未批量迁 `circuit`、`powder`、`billet`、`wire` 等完整旧 meta/autogen 族。
  - 现有 battery/capacitor 机器配方输出仍生成现代 item stack；legacy id/meta 主要作为迁移事实和后续诊断字段，不改变当前输出 JSON 语义。
  - 缺失 legacy id/meta 映射时，datagen/runtime 入口必须报错或显式跳过，不能静默映射到错误物品。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 继续推进：审计命令、pool 索引与生成期护栏

- 本批接入：
  - `GenericMachineRecipeRuntime.Index` 增加 `byPool` 索引，并提供 `findByPool(level, machine, pool)`，为旧 `GenericRecipes` 的 blueprint/pool 查询入口预留统一路径。
  - `Index` 内部 map 改为保留插入顺序的不可变 `LinkedHashMap` 包装，避免 `Map.copyOf` 改变诊断输出顺序。
  - `Audit#problemCount()` 汇总重复 internal name、空输入、空输出、无效 duration，供命令和开发期检查直接使用。
  - `/hbm recipe audit [machine]`：
    - 输出每类 HBM 机器配方数量、internal name 数、pool 数和问题数。
    - 展开重复 internal name、空输入、空输出、无效 duration 的 recipe id。
  - `/hbm recipe legacyMeta [legacyId]`：
    - 列出当前统一 `LegacyMetaItemMappings` 已登记的旧 id/meta -> 现代 item id 映射。
    - 当前可诊断 `hbm:battery_pack` 与 `hbm:battery_sc`。
  - `LegacyMetaItemMappings` 增加：
    - 重复 legacy family 注册保护。
    - `variantCount(...)`、`legacyIds()`、稳定顺序 `mappings()`。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder#save(...)` 增加生成期校验：
    - duration 必须大于 0。
    - item/fluid 输入不能同时为空。
    - item/fluid 输出不能同时为空。
- 迁移边界：
  - 这些命令和校验不改变现有配方 JSON 内容；它们用于后续批量迁旧配方、战利品池和 hazard 时快速发现缺失映射或坏数据。
  - 具体 `circuit`、stamp、autogen material family 的 meta 映射仍未批量落地，需要按旧 `ItemCircuit.EnumCircuitType`、`ItemEnumMulti`/`OreDictManager` 继续补表。
  - `/hbm recipe audit` 读取当前 datapack recipe manager，若未来做缓存必须绑定 reload 生命周期。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
  - `.\gradlew.bat compileJava processResources runData --no-daemon` 通过。
  - 本批没有写入 `src/generated/resources/data/hbm/recipes` 下的 recipe JSON。
  - `.\gradlew.bat runData --no-daemon` 通过。

## 2026-05-30 继续推进：wildcard/display/index/audit 库层补齐

- 本批接入：
  - `HbmIngredient` 增加旧 `ComparableStack` wildcard meta 承接：
    - `WILDCARD_META = Short.MAX_VALUE`。
    - `legacyWildcard(ResourceLocation legacyId, int count)` 通过 `LegacyMetaItemMappings` 展开旧 meta 族的全部现代独立物品。
    - JSON/network 增加 `legacy_wildcard` 字段，已有 `legacy_id` / `legacy_meta` 精确映射保持兼容。
  - `HbmIngredient#test(stack, ignoreSize)` 对齐旧 `AStack.matchesRecipe(ItemStack, ignoreSize)` 的尺寸开关；原 `test(stack)` 保持严格数量检查。
  - `HbmIngredient#displayStacks()` 和 `GenericMachineRecipe#getDisplayItemInputs()` 为后续 JEI/选择界面提供统一展示栈入口：
    - exact NBT 输入显示 exact stack。
    - tag/普通 ingredient 展开为 `Ingredient#getItems()`。
    - legacy wildcard 展开为映射表内所有现代变体。
  - `LegacyMetaItemMappings#stacks(...)` 提供旧 meta 族整体展示/抽取辅助。
  - `GenericMachineRecipeRuntime` 新增 `Index` 与 `Audit`：
    - `index(level, machine)` 缓存本次查询的 recipe list、internal name map、重复 internal name map。
    - `audit(level, machine)` 返回重复 `internal_name`、空输入、空输出、无效 duration 的诊断结果。
    - `recipeNames(...)` / `findByInternalName(...)` 改走索引，保留现有机器调用语义。
  - datagen builder 新增：
    - `inputLegacyWildcard(...)`
    - `pool(...)`
    - `autoSwitchGroup(...)`
- 迁移边界：
  - 本批仍是库层能力，不新增具体旧配方批量内容。
  - `Index` 当前按调用即时构建，避免引入 datapack reload 缓存失效问题；若后续 JEI/GUI 高频调用需要缓存，必须绑定 recipe manager reload 生命周期。
  - `Audit` 只做诊断，不阻断加载；后续可接命令或开发期 datagen 检查。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 继续推进：circuit legacy meta 家族与通用概率输出

- 本批接入：
  - `LegacyMetaItemMappings` 增加 `hbm:circuit` 旧 metadata 家族，按 1.7.10 `ItemCircuit.EnumCircuitType` 枚举 ordinal 映射到现代独立物品：
    - `0` -> `circuit_vacuum_tube`
    - `1` -> `circuit_capacitor`
    - `2` -> `circuit_capacitor_tantalium`
    - `3` -> `circuit_pcb`
    - `4` -> `circuit_silicon`
    - `5` -> `circuit_chip`
    - `6` -> `circuit_chip_bismoid`
    - `7` -> `circuit_analog`
    - `8` -> `circuit_basic`
    - `9` -> `circuit_advanced`
    - `10` -> `circuit_capacitor_board`
    - `11` -> `circuit_bismoid`
    - `12` -> `circuit_controller_chassis`
    - `13` -> `circuit_controller`
    - `14` -> `circuit_controller_advanced`
    - `15` -> `circuit_quantum`
    - `16` -> `circuit_chip_quantum`
    - `17` -> `circuit_controller_quantum`
    - `18` -> `circuit_atomic_clock`
    - `19` -> `circuit_numitron`
  - `ModItems.CIRCUIT_ITEMS` 按旧枚举 ordinal 顺序集中登记，供配方、战利品、hazard、物品迁移共同复用。
  - datagen 为全部 circuit 生成：
    - `forge:circuits` 总标签。
    - `forge:circuits/<legacy_name>` 细标签。
    - item model，贴图路径保持旧资源命名 `textures/item/circuit.<legacy_name>.png`。
    - 英文/中文语言名，来源于 1.7.10 `en_US.lang` / `zh_CN.lang`。
  - 新增 `HbmItemOutput`，作为旧 `GenericRecipes.IOutput` / `ChanceOutput` / `ChanceOutputMulti` 的现代承接层：
    - 普通单输出仍序列化为既有 item stack JSON，不破坏当前已生成 recipe JSON。
    - 单输出可附加 `chance`，按旧版逻辑对 stack 内每个 item 独立掷概率，可能产出 0..stack count 个。
    - 多选输出使用 `{ "type": "one_of", "entries": [...] }`，每个 entry 可带 `weight` 与 `chance`。
    - network 同步保留 one-of、chance、weight 和完整 `ItemStack`。
  - `GenericMachineRecipe`：
    - item 输出内部改为 `List<HbmItemOutput>`。
    - `getItemOutputs()` 保持返回代表 `ItemStack`，兼容现有机器/审计调用。
    - 新增 `getItemOutputEntries()` 与 `getDisplayItemOutputs()`，供后续机器运行、JEI 和 GUI 显示读取完整输出语义。
  - `GenericMachineRecipeRuntime`：
    - `canFitItemOutputs(...)` 对概率/多选输出的每个可能 entry 做槽位容量模拟，避免运行时随机出不能塞进槽的结果。
    - `produceOutputs(...)` 调用 `collapseItemOutputs(...)` 生成本次实际输出，保留输出槽位顺序；概率失败时该槽为空，不把后续输出前移。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 增加：
    - `outputChance(...)`
    - `outputOneOf(...)`
    - `outputLegacyMetaChance(...)`
    - 内部 `WeightedOutput` helper，用于后续从旧 Java 配方按 `ChanceOutput` / `ChanceOutputMulti` 生成 datapack。
- 迁移边界：
  - 本批仍不批量迁具体旧机器 recipe；只是补齐输出语义承接层和 `circuit` meta 族。
  - `HbmItemOutput` 当前不在 tooltip 层写入概率文字；JEI/GUI 后续应读取 `chance/weight` 自行显示，避免把旧 NEI tooltip 逻辑混进 recipe 数据层。
  - `stamp_book`、`template`、ammo、fuel rod、watz pellet、pwr fuel 等旧 meta 家族尚未补齐；需要等现代独立物品或数据化载体稳定后再注册到同一 `LegacyMetaItemMappings`。
  - `circuit_chip_quantum` 是旧 `hbm:circuit` meta `16`，不是显示顺序索引。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-30 继续推进：输入堆叠上限与 blueprint pool 分类

- 1.7.10 对照：
  - `GenericRecipe#checkStackLimit(AStack)` 会在 Java 默认配方注册期检查 `AStack.stacksize` 是否超过物品最大堆叠数。
  - `GenericRecipes` 定义 blueprint pool 前缀：
    - `alt.`
    - `discover.`
    - `secret.`
    - `528.`
  - 旧 `setPools(...)` 会在 528 模式未启用时拒绝默认注册 `528.` pool；旧 JSON 的 `setPoolsAllow528(...)` 则允许读取该 pool。
- 本批接入：
  - `HbmIngredient` 增加：
    - `stackLimit()`
    - `exceedsStackLimit()`
  - 普通 item、exact stack、partial NBT、legacy meta 与 legacy wildcard 都通过统一 display/mapping 路径计算可能的最大堆叠上限；tag 为空时不误报。
  - `GenericMachineRecipe.Serializer#fromJson(...)`、`LegacyGenericRecipeImporter` 与 `HbmRecipeProvider.GenericMachineRecipeBuilder#validate(...)` 现在都会拒绝超过堆叠上限的 item 输入。
  - `GenericMachineRecipeRuntime.Audit` 增加 `oversizedItemInputs` 诊断；`/hbm recipe audit` 会列出这类配方 id。
  - 新增 `LegacyBlueprintPools`：
    - 集中保存 `alt.` / `discover.` / `secret.` / `528.` 前缀。
    - 提供 `kind(...)` 与 `is528(...)`，为后续 blueprint/JEI/解锁 UI 使用。
  - `GenericMachineRecipeRuntime.Index` 增加 `byPoolKind`，按 legacy pool 分类索引配方。
- 迁移边界：
  - 本批只承接旧 pool 分类语义，不迁现代 config 中的 `enable528` 开关；旧 JSON 导入仍按 `setPoolsAllow528(...)` 语义允许 `528.` pool 存在。
  - `HbmIngredient#stackLimit()` 对 tag 输入采用当前可展开候选中的最大堆叠上限；后续若存在运行时 tag 空缺，应由 tag 数据迁移补齐，不在 loader 层猜测。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 被并行流体网络半迁移阻塞，当前报错为：
    - `ModBlockEntities.FLUID_DUCT_GAUGE` / `FLUID_DUCT_BOX` / `FLUID_DUCT_EXHAUST` / `FLUID_PIPE_ANCHOR` 尚未注册。
    - 这些 block entity 对应的 block 注册目前也未落入 `ModBlocks`，不适合在配方库批次内以假 block 补齐。
  - 同次编译没有报告本批 recipe 类新增代码的编译错误。

## 2026-05-30 继续推进：旧 GenericRecipes 导入 ID 护栏与旧格式写出辅助

- 1.7.10 对照：
  - `SerializableRecipe#readRecipeStream(...)` 读取外层 `{ "recipes": [...] }`，每条配方由 `GenericRecipes#readRecipe(...)` 的 `name` 字段作为旧 internal name。
  - 旧 JSON 写出工具使用紧凑数组格式：
    - AStack：`["item", id, count?, meta?]` / `["nbt", id, count?, meta?, nbt?]` / `["dict", ore, count?]`
    - ItemStack：`[id, count?, meta?, nbt?]`
    - FluidStack：`[fluid, amount, pressure?]`
    - Output：`["single", stack, chance?]` 或 `["multi", ...weightedOutputs]`
- 本批接入：
  - `LegacyGenericRecipeImporter` 增加 `readWithReport(...)`：
    - 返回 `ImportReport`，包含源配方数量、导入结果、ID remap 列表和跳过数量。
    - 对由旧 `internal_name` 生成的现代 recipe id 做小写化、非法字符清理和重复去重。
    - 当多个旧配方名映射到同一个 datapack id 时，后续项自动追加 `_2` / `_3` 等后缀，并在 `IdRemap` 中记录。
  - `LegacyGenericRecipeImporter#toModernJson(...)` 现在通过 `Machine#serializerId()` 写入现代 `type`，避免在 registry 未完全可查询时反查 serializer key。
  - `LegacyGenericRecipeFormat` 增加旧格式写出辅助：
    - `writeLegacyAStack(...)`
    - `writeLegacyItemOutput(...)`
    - `writeLegacyFluidStack(...)`
  - 这些写出 helper 主要用于后续批量导入时做对照、报告和调试，不作为长期 recipe 存储格式。
- 迁移边界：
  - `readWithReport(...)` 只处理一次导入调用内的 ID 去重；跨文件/跨机器的最终 datapack ID 冲突仍应由调用方用目标输出路径统一检查。
  - `writeLegacyAStack(...)` 对 tag 且存在多候选的现代输入不会猜测 ore dict 名，会显式报错；后续需要时应从 `LegacyOreDictionaryMappings` 保存源 ore 名。
  - legacy wildcard 当前会按 `HbmIngredient.WILDCARD_META` 写回旧 meta 值；这是迁移诊断用途，不代表所有旧消费端都已支持该现代内部哨兵。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat processResources runData --no-daemon` 的 `processResources` 与 Java 编译通过，`runData` 被并行流体 duct 资源缺口阻塞：
    - `Texture hbm:block/boxduct_silver does not exist in any known resource pack`
  - 本批没有写入 `src/generated/resources/data/hbm/recipes` 下的 recipe JSON。
  - `.\gradlew.bat processResources runData --no-daemon` 通过，DataGenerator `written: 0`。
  - `.\gradlew.bat processResources runData --no-daemon` 通过。
  - 本批没有写入 `src/generated/resources/data/hbm/recipes` 下的 recipe JSON。

## 2026-05-30 继续推进：机器上限元数据与旧 GenericRecipes 文件导入骨架

- 1.7.10 对照：
  - `GenericRecipes` 每个具体机器声明四类槽位上限：
    - `inputItemLimit()`
    - `inputFluidLimit()`
    - `outputItemLimit()`
    - `outputFluidLimit()`
  - 当前已迁现代通用机器对应旧值：
    - `ChemicalPlantRecipes`：item input `3`、fluid input `3`、item output `3`、fluid output `3`。
    - `AssemblyMachineRecipes`：item input `12`、fluid input `1`、item output `1`、fluid output `1`。
  - `SerializableRecipe#readRecipeStream(...)` 读取旧配置文件外层 `{ "recipes": [...] }`，再逐条交给 `GenericRecipes#readRecipe(...)`。
- 本批接入：
  - `GenericMachineRecipe.Machine` 增加机器上限元数据：
    - `inputItemLimit()`
    - `inputFluidLimit()`
    - `outputItemLimit()`
    - `outputFluidLimit()`
    - `validateRecipeLimits(...)`
  - `GenericMachineRecipe.Serializer#fromJson(...)` 在构造 recipe 前校验四类输入/输出数量，外部 datapack 或旧 JSON 兼容字段越界时抛 `JsonSyntaxException`。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 持有目标 `Machine`，datagen `save(...)` 同步执行机器上限校验，把批量迁旧配方时的槽位错误提前到生成期。
  - `GenericMachineRecipeRuntime.Audit` 增加 `overLimit` 诊断；`/hbm recipe audit` 现在输出：
    - 无效 weighted one-of 输出。
    - 超出机器上限的配方 id。
  - 新增 `LegacyGenericRecipeImporter`：
    - 读取旧 `{ "recipes": [...] }` 包装文件。
    - 逐条调用现有 `LegacyGenericRecipeFormat`，转换成现代单条 `GenericMachineRecipe` JSON。
    - 生成稳定导入 id，保留 `internal_name`、duration/power、输入/输出、pool、auto switch、icon、custom localization 与 name wrapper。
    - 只提供库层导入骨架，不自动扫描 config 文件，也不绕过现代 datapack 生命周期。
- 迁移边界：
  - 上限元数据当前只覆盖已经有现代 recipe serializer/机器运行时的 `chemical_plant` 与 `assembly_machine`。
  - `LegacyGenericRecipeImporter` 是后续批量迁旧 GenericRecipes JSON 的转换辅助，不代表旧外部 config 热加载系统已经迁移。
  - 旧 `GenericRecipe#getIcon()` 的 fluid icon fallback 仍未迁；本批只保留显式 `icon` 与 item 输出代表图标。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - `.\gradlew.bat compileJava processResources runData --rerun-tasks --no-daemon` 中 Java 编译和资源处理已通过，随后因 ForgeGradle 重新下载 vanilla asset 时 `minecraft/lang/be_latn.json` SSL 握手失败中止；复用缓存重跑 `runData` 通过。
  - 本批没有写入 `src/generated/resources/data/hbm/recipes` 下的 recipe JSON。

## 2026-05-30 继续推进：旧 NBTStack 子集匹配与 GenericRecipes JSON 兼容加载

- 1.7.10 对照：
  - `RecipesCommon.NBTStack#matchesRecipe(...)`：
    - 先执行旧 `ComparableStack` 规则：item 相同、meta 相同或 wildcard、数量足够。
    - 再检查配方声明 NBT 中的每个 key：输入栈必须存在相同 key 且值相等。
    - 输入栈存在额外 NBT key 时仍然匹配。
  - `SerializableRecipe#readAStack(...)` 支持旧数组格式：
    - `["item", itemId, count?, meta?]`
    - `["nbt", itemId, count?, meta?, nbtString?]`
    - `["dict", oreDictName, count?]`
  - `GenericRecipes#readRecipe(...)` 支持旧字段名：
    - `name`
    - `inputItem`
    - `inputFluid`
    - `outputItem`
    - `outputFluid`
    - `blueprintpool`
    - `autoSwitchGroup`
  - 旧输出数组支持：
    - `["single", itemStackArray, chance?]`
    - `["multi", ...chanceOutputArray]`
    - `ChanceOutputMulti` 内部 entry 既可能带 `"single"` 标识，也可能是 weighted entry `[itemStackArray, chance?, weight?]`。
- 本批接入：
  - `HbmIngredient` 新增 `partial_nbt`：
    - JSON 字段：`partial_nbt`。
    - network 同步：在 `exact_stack` 后写入可选 `CompoundTag`。
    - 匹配规则对齐旧 `NBTStack`：只要求 `partial_nbt` 中声明的 key/value 存在且相等，输入栈额外 tag 不影响匹配。
    - `exact_stack` 与 `partial_nbt` 互斥，JSON 同时声明时抛 `JsonSyntaxException`。
    - `displayStacks()` 会把 `partial_nbt` 写回展示栈，方便后续 JEI/GUI 显示。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 新增 `inputPartialNbt(ItemStack)`，用于后续从旧 `NBTStack` 迁移机器配方。
  - 新增 `LegacyGenericRecipeFormat`，作为旧 `SerializableRecipe` / `GenericRecipes` JSON 到现代 `GenericMachineRecipe` 的兼容读取层：
    - 现代字段优先：`input_items` / `output_items` / `input_fluids` / `output_fluids` / `pools` / `auto_switch_group`。
    - 若现代字段缺失，读取旧字段：`inputItem` / `outputItem` / `inputFluid` / `outputFluid` / `blueprintpool` / `autoSwitchGroup`。
    - 旧未命名空间 item id 归一化为 `hbm:<id>`。
    - 旧 `item` meta 输入优先走 `LegacyMetaItemMappings`；非 0 meta 缺映射时直接报错，不静默退回错误现代物品。
    - 旧 `nbt` 输入转为 `HbmIngredient.partialNbt(...)`。
    - 旧 `dict` 输入按常见 ore dict 名映射到 `forge:` item tag：
      - `ingotX` -> `forge:ingots/x`
      - `dustX` -> `forge:dusts/x`
      - `plateCastX` -> `forge:cast_plates/x`
      - `wireDenseX` -> `forge:dense_wires/x`
      - `wireX` -> `forge:wires/x`
      - `circuitX` -> `forge:circuits/x`
    - 旧 fluid 数组 `[fluidName, amount, pressure?]` 转为当前 `HbmFluidStack`。
    - 旧 `outputItem` 的 `single` / `multi` 转为 `HbmItemOutput`，保留 chance 与 weight。
  - `GenericMachineRecipe.Serializer#fromJson(...)` 改为调用 `LegacyGenericRecipeFormat`，因此同一个 serializer 同时支持现代 datapack JSON 与旧 GenericRecipes 兼容字段。
- 迁移边界：
  - `LegacyGenericRecipeFormat` 是兼容读取层，不是完整旧配置系统；旧 `SerializableRecipe` 的模板文件写出、外部 config 文件发现、IMC listener 等生命周期仍未迁移。
  - 旧 ore dict 到 tag 的自动转换只覆盖常见命名模式；特殊 ore dict 名仍需要在 tag provider 或映射层显式补桥。
  - 旧 item meta 非 0 且没有 `LegacyMetaItemMappings` 记录时会报错，这是为了避免后续批量迁移把 metadata 物品错读成 meta 0 的现代物品。
  - `exact_stack` 仍保留为现代精确 NBT 输入；旧 `NBTStack` 应优先迁为 `partial_nbt`。
- 本批验证：
  - `.\gradlew.bat compileJava --rerun-tasks --no-daemon` 通过。
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat processResources runData --no-daemon` 通过。
  - 本批没有写入 `src/generated/resources/data/hbm/recipes` 下的 recipe JSON。

## 2026-05-30 继续推进：OreDictionary 映射桥与旧配方显示元数据承接

- 1.7.10 对照：
  - `OreDictManager` 的 `DictFrame` 与 `MaterialShapes` 不只是简单 `ingotX` / `dustX` 命名：
    - 标准 key：`stickWood`、`blockGlass`、`paneGlass`、`dyeRed`、`ntmscrewdriver`、`ntmuniversaltank` 等。
    - 材料形状前缀：`oreNether`、`nugget`、`tiny`、`dustTiny`、`wireFine`、`wireDense`、`plateTriple`、`plateSextuple`、`ntmpipe`、`barrelLight`、`receiverHeavy`、`gunMechanism` 等。
  - `GenericRecipe` 还携带旧显示/本地化元数据：
    - `icon`
    - `named`
    - `nameWrapper`
  - `GenericRecipes.ChanceOutputMulti` 依赖 `ChanceOutput.itemWeight` 做 weighted random；权重全为 0 的多选输出在旧逻辑里不是有效随机池。
- 本批接入：
  - 新增 `LegacyOreDictionaryMappings`，把旧 ore dict 名统一映射为现代 `TagKey<Item>`：
    - 标准 key 显式映射到 `forge:` 或 `minecraft:` tag，例如 `stickWood -> forge:rods/wooden`、`logWood -> minecraft:logs`、`dyeLightBlue -> forge:dyes/light_blue`。
    - `MaterialShapes` 前缀按旧源码枚举映射到现代 tag 目录，例如 `plateTripleX -> forge:cast_plates/x`、`plateSextupleX -> forge:welded_plates/x`、`wireFineX -> forge:wires/x`、`ntmpipeX -> forge:pipes/x`。
    - 兼容保留旧 JSON 里可能出现的 `plateCastX` / `circuitX` 等桥接别名。
  - `LegacyGenericRecipeFormat` 的旧 `["dict", oreName, count?]` 输入改为走 `LegacyOreDictionaryMappings.itemTag(...)`，不再在 loader 内部维护一段临时正则。
  - `HbmItemTagsProvider` 暴露 `legacyOreItemTag(...)`，后续 datagen/tag 补桥可以复用同一套旧名映射。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 增加 `inputLegacyOre(...)`，后续从旧 Java recipe 或旧 JSON 批量生成 datapack 时可直接按 1.7.10 ore dict 名写入。
  - `GenericMachineRecipeRuntime.Audit` 增加 `invalidWeightedOutputs`，用于诊断 `one_of` 输出中没有任何正权重的配方，避免旧 `ChanceOutputMulti` 数据被错误读成均匀随机或静默坏池。
  - 旧显示元数据承接已落入通用 recipe：
    - `GenericMachineRecipe` 保存并 network 同步 `icon`、`customLocalization`、`nameWrapper`。
    - `LegacyGenericRecipeFormat` 支持旧字段 `icon`、`named`、`nameWrapper`，现代字段为 `icon`、`custom_localization`、`name_wrapper`。
    - `GenericMachineRecipeRuntime.findAutoSwitchRecipe(...)` 对首个输入使用 `test(firstInput, true)`，对齐旧 `matchesRecipe(..., ignoreSize=true)` 自动切换行为。
- 迁移边界：
  - `LegacyOreDictionaryMappings` 是命名桥，不负责自动补齐 tag 内容；实际 tag 条目仍由 `HbmItemTagsProvider` / `HbmBlockTagsProvider` 或后续物品族迁移提供。
  - `anyX`、容器类、工具类等旧 key 已有稳定 tag 名，但现代物品未全部迁完时 tag 可能为空，这是数据迁移进度问题，不是 loader 语义问题。
  - 流体阀相关未跟踪文件在本轮验证期间进入工作树并造成编译阻塞；只做了不依赖缺失注册项的最小 API 兜底，未进行完整流体阀迁移。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat processResources runData --no-daemon` 曾通过，且未写入 `src/generated/resources/data/hbm/recipes` 下的 recipe JSON。
  - 后续重跑 `.\gradlew.bat compileJava processResources runData --no-daemon` 被并行流体阀半迁移阻塞：
    - `FluidValveBlock` 引用尚未注册的 `ModBlockEntities.FLUID_COUNTER_VALVE`。
    - `FluidValveBlock` 引用当前不存在的 `ModSounds.BLOCK_REACTOR_START`。
  - 该阻塞与本批 recipe/loader 抽象无直接耦合，后续应在 fluid network/valve 迁移批次中补齐注册与声音映射。

## 2026-05-30 继续推进：Generic runtime 无序 item 输入匹配

- 1.7.10 对照：
  - `SerializableRecipe#matchesIngredients(ItemStack[] inputs, AStack[] recipe)` 不按槽位位置匹配。
  - 旧逻辑把配方输入复制为待匹配列表，然后按机器传入的输入槽顺序扫描：
    - 空输入槽忽略。
    - 每个非空输入栈必须匹配一个尚未使用的 `AStack`。
    - 匹配时使用 `matchesRecipe(inputStack, true)`，再单独检查输入栈数量是否达到配方数量。
    - 找到匹配后从待匹配列表移除该配方项。
    - 若存在多余非空输入，或配方项没有全部被认领，则失败。
- 本批接入：
  - `GenericMachineRecipeRuntime` 的 item 输入检查从旧的槽位同序匹配改为无序匹配计划：
    - `canProcess(...)` 通过 `matchItemInputs(...)` 生成匹配计划。
    - `consumeInputs(...)` 重新生成同一规则的匹配计划，并按计划中记录的实际槽位消耗对应 `HbmIngredient#count()`。
    - 匹配仍委托 `HbmIngredient#test(...)`，因此 item/tag、partial NBT、exact NBT、legacy meta 与 legacy wildcard 都走同一语义入口。
  - 匹配顺序保持旧版行为：按输入槽顺序扫描，每个槽从剩余配方项的声明顺序中取第一个可匹配项。
  - 额外非空输入槽会让配方失败，避免机器在塞入无关物品时误启动。
- 迁移边界：
  - 流体输入仍保持当前按 tank 顺序匹配；旧 GenericRecipes 的流体槽位语义后续迁具体机器时再按机器 trace 校验。
  - item 输出放置仍保持输出声明顺序对应输出槽；旧概率输出和 one-of 已由 `HbmItemOutput` 承接，输出槽是否需要无序合并留给后续具体机器/JEI pass。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-30 继续推进：legacy ore 原名保留与旧输出显示元数据

- 1.7.10 对照：
  - `SerializableRecipe#readAStack/writeAStack` 对 `OreDictStack` 使用旧数组 `["dict", oreName, count?]`，这里的 `oreName` 是诊断、外部 JSON 回写和批量迁移 diff 的事实来源。
  - `GenericRecipes.IOutput#getAllPossibilities()` / `getLabel()` 为 NEI/GUI 提供输出展示候选：
    - `ChanceOutput` 单项输出按 item count 逐个 roll，展示 label 包含单项 chance。
    - `ChanceOutputMulti` 先按 `itemWeight` 从 pool 中选一项，再乘以该项自身 chance；展示 label 使用 `weight / totalWeight * chance` 的有效概率。
- 本批接入：
  - `HbmIngredient` 新增 `legacyOreName` / JSON 字段 `legacy_ore` / network 同步字段：
    - `HbmIngredient.legacyOre(legacyOreName, count)` 统一创建旧 ore dict 输入。
    - 仍通过 `LegacyOreDictionaryMappings.itemTag(...)` 映射到现代 tag 参与运行时匹配。
    - 原始旧 ore dict 名随 recipe 数据保留，避免 `forge:` tag 路径反推旧名造成信息损失。
  - `LegacyGenericRecipeFormat` 的旧 `["dict", oreName, count?]` 读取改为 `HbmIngredient.legacyOre(...)`；`writeLegacyAStack(...)` 遇到 `legacyOreName` 时可回写为旧 `dict` 数组。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder#inputLegacyOre(...)` 改为走 `HbmIngredient.legacyOre(...)`，datagen 后续生成的兼容 recipe 同样保留旧 ore 名。
  - `HbmItemOutput` 增加旧显示语义承接 API：
    - `displayOptions()`：返回每个可能输出栈及有效概率。
    - `displayLabels()`：生成旧 `getLabel()` 风格文本，`one_of` 输出带 `One of:` 头。
    - `totalPositiveWeight()` / `hasValidWeightedChoices()`：把 weighted pool 有效性判断收敛到输出对象本身。
  - `GenericMachineRecipeRuntime` 的 invalid weighted output audit 改为调用 `HbmItemOutput#hasValidWeightedChoices()`，避免 audit 与输出语义分叉。
- 迁移边界：
  - `legacy_ore` 只是来源名保留和回写辅助；实际 tag 内容仍由 tag provider/物品族迁移填充。
  - `displayLabels()` 当前是库层纯文本承接，尚未接入 JEI screen tooltip；后续 JEI/GUI 迁移可直接消费 `displayOptions()` 或 `displayLabels()`。
  - `one_of` 权重总和为 0 的数据仍被 audit 标记为无效；运行时保留既有兜底随机选择，避免坏 datapack 直接崩溃。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-30 继续推进：GenericRecipe 显示/搜索语义与导入报告增强

- 1.7.10 对照：
  - `GenericRecipe#getLocalizedName()`：
    - `customLocalization` 为 true 时优先按 internal name 本地化。
    - 否则使用 `getIcon().getDisplayName()`。
    - `nameWrapper` 存在时用 wrapper 本地化 key 包裹最终名称。
  - `GenericRecipe#print()` 生成配方选择器/NEI 说明行：
    - 标题、auto switch、duration、power。
    - item/fluid 输入。
    - item/fluid 输出。
  - `GenericRecipe#matchesSearch(...)` 默认按本地化名称子串搜索；后续具体 GUI/handler 可扩展为按输入输出搜索。
- 本批接入：
  - `GenericMachineRecipe` 新增库层显示 API：
    - `getDisplayName()`：承接 `customLocalization`、`nameWrapper` 与 icon fallback。
    - `getDisplayLines()`：生成旧 `print()` 风格的 `Component` 行，包含输入/输出、duration、power、fluid pressure 与 item output label。
    - `getSearchText()`：汇总 internal name、recipe id、显示名、pool、auto switch、legacy ore 名、输入输出 item/fluid 名。
    - `matchesSearch(query)`：对汇总搜索文本执行大小写无关匹配。
  - `GenericMachineRecipeRuntime` 增加 `search(level, machine, query)`，后续配方选择 GUI/JEI 搜索可直接复用。
  - `LegacyGenericRecipeImporter.ImportReport` 增强：
    - 记录重复旧 `internalName`：`DuplicateInternalName(internalName, occurrence)`。
    - 记录 `LegacyBlueprintPools.Kind` 维度的 pool 引用计数。
    - 提供 `hasDuplicateInternalNames()`、`poolCount(kind)`、`pooledRecipeReferences()`。
- 迁移边界：
  - `getDisplayLines()` 是库层结构化文本，不直接实现 JEI/Screen；后续 UI 可以按需重新排版这些 `Component`。
  - 旧 `ItemFluidIcon.make(...)` 的“流体图标 item”还未迁；现代 `getIcon()` 仍保持显式 icon -> item 输出 -> machine toast symbol 的 fallback。纯 fluid 输出图标 fallback 仍待后续流体 icon item/JEI 承接。
  - 旧 `matchesSearch(...)` 只搜本地化名；现代库层额外纳入输入输出与 pool，这是为了后续配方选择器减少重复索引逻辑，不改变机器运行语义。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-30 继续推进：旧 GenericRecipe 整条回写与宽松批量导入报告

- 1.7.10 对照：
  - `GenericRecipes#writeRecipe(...)` 写出整条旧 JSON：
    - `name`
    - `inputItem` / `inputFluid`
    - `outputItem` / `outputFluid`
    - `duration` / `power`
    - `icon`（仅 `writeIcon` 为 true）
    - `named`
    - `nameWrapper`
    - `blueprintpool`
    - `autoSwitchGroup`
  - `SerializableRecipe#readRecipeStream(...)` 在旧版运行时逐条读取；批量迁移工具需要能一次扫出多条坏配方，而不是第一条失败就终止全部诊断。
- 本批接入：
  - `LegacyGenericRecipeFormat` 新增 `writeLegacyRecipe(GenericMachineRecipe)`：
    - 将现代通用 recipe 回写为旧 `GenericRecipes` 风格 `JsonObject`。
    - 复用已有 `writeLegacyAStack(...)`、`writeLegacyItemOutput(...)`、`writeLegacyFluidStack(...)`。
    - 写出 `name`、输入/输出、duration、power、named、nameWrapper、blueprintpool、autoSwitchGroup。
  - `LegacyGenericRecipeImporter` 保留严格 `readWithReport(...)` 行为，新增宽松导入入口：
    - `readLenientWithReport(...)`。
    - 单条旧配方转换失败时继续处理后续配方。
    - `ImportReport` 新增 `failures` 与 `hasFailures()`。
    - 每个 `ImportFailure` 记录：源数组 index、旧 internal name、请求生成的 id、失败消息。
- 迁移边界：
  - `writeLegacyRecipe(...)` 暂不回写 `icon`，因为现代 recipe 当前只保存 icon 结果，不保存旧 `writeIcon` 布尔状态；直接写回会把“输出 fallback icon”误认成显式旧 icon。
  - 宽松导入只捕获单条 recipe 转换阶段的 `RuntimeException`；外层 JSON 不是对象或缺少 `recipes` 数组仍保持直接失败。
  - 宽松导入中的 ID remap / duplicate internal name / pool kind 统计仍基于源条目执行，即使该条后续转换失败也保留诊断信息。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-30 继续推进：未解析 tag/legacy ore 输入审计

- 1.7.10 对照：
  - 旧 `OreDictStack` 依赖运行时 OreDictionary 内容；如果某个 ore name 没有任何条目，配方虽然可读入，但永远无法匹配。
  - 迁移到 1.20.1 后，旧 ore dict 名会落到现代 item tag；tag 未补内容是配方、战利品、hazard 和物品族迁移之间的共同缺口。
- 本批接入：
  - `HbmIngredient` 增加诊断 API：
    - `hasDisplayStacks()`。
    - `unresolvedDisplayInput()`。
    - `diagnosticName()`，可输出 legacy ore -> modern tag、legacy id/meta/wildcard、exact item 或原始 ingredient JSON。
  - `GenericMachineRecipeRuntime.Audit` 增加 `unresolvedItemInputs`：
    - 检测 item 输入无法展开任何展示/匹配候选的配方。
    - `hasProblems()` 与 `problemCount()` 纳入该类问题。
  - `/hbm recipe audit` 输出 `unresolved item inputs`，并列出具体 recipe id 与未解析输入诊断名。
- 迁移边界：
  - 该审计只诊断“当前数据包/标签状态下无法展开候选”的输入，不阻止 recipe 加载；后续补 tag 或物品族后 audit 会自然消失。
  - 对普通 tag 输入同样生效，但对 `legacy_ore` 会额外显示旧 ore name 与现代 tag id，方便按旧名补桥。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 首次因 `ModCommands` 缺少 `HbmIngredient` import 失败。
  - 补 import 后，`.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-31 继续推进：legacy ore 映射查询与 tag 内容诊断

- 1.7.10 对照：
  - 旧 JSON 中 `["dict", oreName, count?]` 的 `oreName` 不是 tag id，而是旧 OreDictionary 名；迁移时需要知道它经由哪条规则映射到现代 tag。
  - 未解析输入 audit 找到空 tag 后，需要能快速判断是映射规则错了、tag 内容未补，还是旧 ore name 本身只能走 fallback。
- 本批接入：
  - `LegacyOreDictionaryMappings` 增加 `resolve(legacyName)`：
    - 返回 `Mapping(legacyName, tagId, kind, matchedRule, materialOrPath)`。
    - `kind` 分为 `EXACT`、`SHAPE_PREFIX`、`FALLBACK`。
    - 原有 `itemTag(...)` / `itemTagId(...)` 行为不变，只改为复用 `resolve(...)`。
  - `/hbm recipe legacyOre <oreName>`：
    - 输出旧 ore name 对应的现代 tag id。
    - 输出映射类型、命中的 exact/shape rule 或 fallback path。
    - 读取当前服务器 registry tag 内容，显示该 tag 当前 item 条目数量。
- 迁移边界：
  - 该命令只诊断 item tag，不自动补 tag 内容；实际补桥仍应放在 `HbmItemTagsProvider` / 物品族迁移里。
  - fallback 映射不代表旧行为完整，只表示当前没有 exact/shape 专门规则，应优先对照旧 `OreDictManager` 与具体 recipe 决定是否补显式映射。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-31 继续推进：未解析输入分组报告

- 1.7.10 对照：
  - 旧 `OreDictStack` 的失败常常不是单条 recipe 的问题，而是某个 ore name 整个族没有注册条目。
  - metadata 拆分迁移也类似：一个旧 `legacy id/meta` 家族缺口会同时影响配方、战利品、hazard 和物品迁移。
- 本批接入：
  - `HbmIngredient` 增加 `diagnosticKey()`：
    - `legacy_ore:<oldName>-><modernTag>`。
    - `legacy_meta:<legacyId>:<meta|*>`。
    - 普通 exact/item/tag ingredient 也有稳定 key，方便后续审计工具分组。
  - `GenericMachineRecipeRuntime.Audit` 在原有 `unresolvedItemInputs` 之外新增结构化明细：
    - `unresolvedItemInputDetails`：记录 recipe、输入序号和 `HbmIngredient`。
    - `unresolvedItemInputGroups`：按诊断 key 聚合，保留稳定插入顺序。
  - 新增 `/hbm recipe unresolved [machine]`：
    - 按机器输出未解析输入 group 数、entry 数、受影响 recipe 数。
    - 每个 group 显示诊断名、引用次数、受影响 recipe 数，以及 `recipeId#inputIndex` 引用位置。
- 迁移边界：
  - 该命令只做诊断和排序，不自动补 tag，也不猜测旧 ore dict 内容。
  - `problemCount()` 仍按受影响 recipe 计数，避免一次 recipe 多个空输入让原 audit 总数突然改变；详细计数交给 `recipe unresolved`。
  - 后续批量迁具体 recipe 或 tag provider 时，应先看 group 报告，优先补影响面最大的 legacy ore/tag/meta 缺口。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat compileJava --rerun-tasks --no-daemon` 通过。
