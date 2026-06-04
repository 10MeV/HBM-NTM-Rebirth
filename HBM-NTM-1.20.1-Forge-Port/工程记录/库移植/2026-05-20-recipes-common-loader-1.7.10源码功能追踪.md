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

## 2026-06-02 继续推进：按未解析分组补 legacy ore/tag/meta 桥

- 1.7.10 对照：
  - `OreDictManager.DictFrame` 用 `MaterialShapes` 前缀 + 材料名生成旧 ore dict key，例如 `ingotUranium`、`dustCopper`、`plateSteel`、`billetPu238`、`wireDenseBSCCO`。
  - `DictFrame` 多名材料会注册全部别名，例如 `Thorium232` / `Th232` / `Thorium`，`Plutonium238` / `Pu238`。
  - `ItemAutogen` 族使用 material id 作为旧 metadata，不是连续 ordinal；例如 `plate_cast`、`wire_dense`、`pipe` 的 meta 来自 `Mats` 中的 material id。
  - `MaterialShapes.CASTPLATE` 的旧前缀是 `plateTriple`，现代兼容层同时保留 `plateCast` 别名。
- 本批接入：
  - `LegacyOreDictionaryMappings#materialPath(...)` 增加命名修正：
    - 旧 `Aluminum` -> 现代 `aluminium` tag path。
    - 旧 `CMBSteel` -> 现代 `combine_steel` tag path。
  - `HbmItemTagsProvider` 新增集中 `addLegacyMaterialTags()`：
    - 为已迁现代 item 补齐旧 `ingot*`、`dust*`、`plate*`、`nugget*`、`billet*`、`wireFine*`、`wireDense*`、`ntmpipe*`、`plateTriple*` / `plateCast*` 到 `forge:` tag 的桥。
    - 覆盖放射性材料别名（如 `U233`、`Pu238`、`Am241`、`Np237`、`Po210`、`Ra226` 等）和基础/合金材料（如 `Copper`、`Steel`、`AdvancedAlloy`、`BismuthBronze`、`ArsenicBronze`、`BSCCO`）。
    - 继续只加入现代端已经注册的 item；不存在的旧族不在本轮造物品、不猜内容。
    - 补 `ingots/any_bismoid_bronze` 与 `cast_plates/any_bismoid_bronze` 聚合 tag，承接旧 `ANY_BISMOIDBRONZE`。
  - `LegacyMetaItemMappings` 从连续 list 扩展为真实 meta key 映射：
    - 保持 battery/circuit 连续 meta 族兼容。
    - 新增 sparse meta 家族：
      - `hbm:plate_cast` meta `39/46/47` -> `plate_cast_combine_steel` / `plate_cast_bismuth_bronze` / `plate_cast_arsenic_bronze`。
      - `hbm:wire_fine` meta `7900` -> `wire_gold`。
      - `hbm:wire_dense` meta `4100/7900/48` -> `wire_dense_niobium` / `wire_dense_gold` / `wire_dense_bscco`。
      - `hbm:pipe` meta `30` -> `pipes_steel`。
    - `/hbm recipe legacyMeta` 改为显示真实 legacy meta key，而不是列表下标。
  - `HbmRecipeProvider` 修正 `assembly_machine/emptypackage`：
    - 旧空塑料包现代 item 最大堆叠为 1，原配方单输入 count=2 被生成期护栏拦截。
    - 改成两个 count=1 输入，配合当前 runtime 的无序匹配要求两个槽位各放一个。
  - `HbmFluidGuiHelper` 做了一个编译解锁修正：
    - `tankInfo(...)` 局部变量从 `Component` 改为 `MutableComponent`，只为解锁本轮 `runData` 验证，不改变流体行为。
- 迁移边界：
  - 本批只补“现代端已有 item”的 tag/meta 桥，不新增物品、不迁世界矿、不补旧 autogen 全量材料族。
  - 对旧别名 tag 的填充以 1.7.10 `OreDictManager` 注册为来源；如果现代 item 尚未迁移，tag 仍可能为空，后续应由对应物品族迁移补齐。
  - sparse meta 映射只登记已拆出的现代独立物品；旧 `ItemAutogen` 全族仍待后续材料系统/物品族批次继续扩展。
- 本批验证：
  - `.\gradlew.bat compileJava --rerun-tasks --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过，item tag、recipe、loot provider 均完成。

## 2026-06-02 收尾：legacy material tag 去重与剩余空 tag 审计

- 本批接入：
  - `HbmItemTagsProvider` 增加 legacy tag 写入去重表：
    - 同一 `TagKey<Item>` 下同一 item id 只写出一次，避免手工桥与 material alias 桥重复生成相同 JSON value。
  - 删除已由 `addLegacyMaterialTags()` 集中覆盖的早期手工桥：
    - `dusts/*`、`ingots/*`、`plates/*`、`wires/*`、`dense_wires/*`、`pipes/steel`、`cast_plates/*` 的重复来源已合并。
    - 仍保留 material 规则未覆盖的兼容别名，例如 `ingots/pc`、`ingots/tantalium`、塑料聚合 tag 与 `dusts/spark_mix`。
- 静态审计：
  - 扫描当前 main/generated recipe JSON 后，未发现残留 `legacy_ore` / `legacy_meta` 原始字段；旧输入已经经导入器落成现代 exact/tag 输入。
  - 当前 recipe 引用的本地 tag 均有内容；剩余扫描项：
    - `minecraft:logs` 为 vanilla item tag，非本地生成缺口。
    - `forge:dusts/coal` 仍无本地现代 item 内容。1.7.10 来源为 `OreDictManager.COAL.dust(powder_coal)`，现代端尚未迁移 `powder_coal` / `powder_coal_tiny` 物品族，因此本批不把它错误映射到 `minecraft:coal`。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。

## 2026-06-04 继续推进：Press WIRE 首个 legacy meta 闭环

- 1.7.10 对照：
  - `PressRecipes#registerDefaults()` 会遍历 `Mats.orderedList`：
    - 若材料含 `MaterialShapes.WIRE` 且存在对应 `ingot*` ore dict，则生成 `StampType.WIRE + ingot -> ModItems.wire_fine x8`。
  - `Mats.MAT_GOLD` 的 material id 为 `7900`，含 `WIRE` autogen。
  - `OreDictManager` 对 `MaterialShapes.WIRE` 会注册旧 `wireFineGold`，目标为 `ModItems.wire_fine` meta `7900`。
- 本批接入：
  - 现代端已有 `wire_gold`、`forge:wires/gold` tag、旧 `hbm:wire_fine` meta `7900 -> hbm:wire_gold` 映射。
  - `HbmRecipeProvider` 新增压机 recipe 代码：
    - `press/wire_gold`：`StampType.WIRE + minecraft:gold_ingot -> hbm:wire_gold x8`。
  - 本批只接入已完整迁移的 gold wire 单条，不展开全 `WIRE` 材料矩阵，避免给尚未迁移的 `wire_fine` meta 输出制造空洞。
- 迁移边界：
  - 旧 `WIRE` 全矩阵仍待后续按现代已注册 wire item 分批补齐。
  - 本条输入使用 `minecraft:gold_ingot`，对应旧 `ingotGold` 的 vanilla 稳定入口；后续若项目建立 vanilla forge ingot tag provider，可改为 `forge:ingots/gold`。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat compileJava --rerun-tasks --no-daemon` 通过。
  - `.\gradlew.bat runData --rerun-tasks --no-daemon` 未完成：Forge datagen 启动阶段报 `fml.modloading.missingclasses`，`build/resources/main` 被构造成 0 个 mod，但 `mods.toml` 指定了 `hbm`；`build/classes/java/main/com/hbm/ntm/HbmNtm.class` 实际存在。该阻塞发生在 provider 执行前，`press/wire_gold.json` 尚未生成，应在 datagen 环境恢复后复跑。

## 2026-06-04 继续推进：Press FLAT 首批 legacy ore/tag/meta 链路

- 1.7.10 对照：
  - `PressRecipes` 的 `StampType.FLAT` 包含一批粉末/宝石压制、biomass 压缩、焦炭石墨、briquette、树脂配方。
  - `OreDictManager` 定义并注册：
    - `NETHERQUARTZ = new DictFrame("NetherQuartz")`，含 `dustNetherQuartz` / `dustQuartz` 对应旧 `powder_quartz`。
    - `LAPIS = new DictFrame("Lapis")`，含旧 `powder_lapis`。
    - `DIAMOND = new DictFrame("Diamond")`，含旧 `powder_diamond`。
    - `EMERALD = new DictFrame("Emerald")`，含旧 `powder_emerald`。
    - `ANY_COKE = new DictFrame("AnyCoke", "Coke")`，用于 `ingot_graphite` 压制入口。
    - `COAL` / `LIGNITE` 的 dust 注册用于 coal/lignite briquette。
  - 旧 `Blocks.log` meta `3` 在该 recipe 中表示 jungle log，输出 `ball_resin`。
- 本批接入：
  - 现代 `ModItems.EXTRA_PARTS_TAB_ITEMS` 补普通 legacy item 落点：
    - `powder_quartz`
    - `powder_lapis`
    - `powder_diamond`
    - `powder_emerald`
    - `powder_sawdust`
    - `ball_resin`
  - 从 1.7.10 资源复制对应旧贴图到现代 `assets/hbm/textures/item/`。
  - `HbmItemTagsProvider` 补 legacy ore/tag alias：
    - `dustNetherQuartz` / `dustQuartz` -> `forge:dusts/quartz`
    - `dustLapis` -> `forge:dusts/lapis`
    - `dustDiamond` -> `forge:dusts/diamond`
    - `dustEmerald` -> `forge:dusts/emerald`
  - `HbmRecipeProvider` 生成首批 `FLAT` 压机 recipe：
    - `press/flat_quartz`：`forge:dusts/quartz` -> `minecraft:quartz`
    - `press/flat_lapis`：`forge:dusts/lapis` -> `minecraft:lapis_lazuli`
    - `press/flat_diamond`：`forge:dusts/diamond` -> `minecraft:diamond`
    - `press/flat_emerald`：`forge:dusts/emerald` -> `minecraft:emerald`
    - `press/flat_biomass`：`hbm:biomass` -> `hbm:biomass_compressed`
    - `press/flat_graphite`：`forge:gems/coke` -> `hbm:ingot_graphite`
    - `press/flat_briquette_coal`：`forge:dusts/coal` -> `hbm:briquette_coal`
    - `press/flat_briquette_lignite`：`forge:dusts/lignite` -> `hbm:briquette_lignite`
    - `press/flat_briquette_wood`：`hbm:powder_sawdust` -> `hbm:briquette_wood`
    - `press/flat_resin`：`minecraft:jungle_log` -> `hbm:ball_resin`
- 迁移边界：
  - 旧 `Blocks.log` meta `3` 先按 1.20.1 的具体方块 `minecraft:jungle_log` 落地；若后续建立更通用的 legacy block meta bridge，可把该 recipe 改为共享映射输出。
  - `meteorite_sword_reforged -> meteorite_sword_hardened` 的 `FLAT` 配方未迁；对应剑族尚未在当前普通物品/行为链路中闭环。
  - 本批只补 recipe/ore/tag/meta 缺口，不迁完整木材/树脂生态或 sword hardening 行为。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 抽样生成：
    - `press/flat_resin.json`：`minecraft:jungle_log` -> `hbm:ball_resin`，`stamp: "flat"`。
    - `forge:dusts/quartz`、`forge:dusts/lapis`、`forge:dusts/diamond`、`forge:dusts/emerald` 均包含对应 HBM powder。
    - 新增普通 item model：`powder_quartz`、`powder_lapis`、`powder_diamond`、`powder_emerald`、`powder_sawdust`、`ball_resin`。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。

## 2026-06-02 继续推进：煤粉物品族补齐以清理 `dusts/coal` 缺口

- 1.7.10 对照：
  - `ModItems` 注册 `powder_coal` 与 `powder_coal_tiny`，贴图分别为 `textures/items/powder_coal.png`、`textures/items/powder_coal_tiny.png`。
  - `OreDictManager.registerOres()` 中 `COAL.gem(Items.coal).dustSmall(powder_coal_tiny).dust(powder_coal)`：
    - `dustCoal` -> `powder_coal`。
    - `dustTinyCoal` -> `powder_coal_tiny`。
  - `HazardRegistry` 中 `dustCoal` 使用 `COAL` hazard `powder = 3.0F`，`dustTinyCoal` 使用 `powder_tiny = 0.3F`。
- 本批接入：
  - 现代 `ModItems` 补 `powder_coal` / `powder_coal_tiny`，进入 parts tab 和 legacy item lookup。
  - 从 1.7.10 原资源复制煤粉/小撮煤粉贴图到现代 `assets/hbm/textures/item/`。
  - `HbmItemTagsProvider` 补：
    - `forge:dusts/coal` / 聚合 `forge:dusts` -> `hbm:powder_coal`。
    - `forge:tiny_dusts/coal` / 聚合 `forge:tiny_dusts` -> `hbm:powder_coal_tiny`。
  - `HazardRegistry` 补 `forge:tiny_dusts/coal` 与 `forge:tiny_dusts/lignite` 的 `COAL` hazard，数值使用旧 `powder_tiny` 公式 `NUGGET * POWDER_MULTIPLIER`。
  - en_us / zh_cn datagen 增加明确物品名，避免 fallback 名称把 tiny item 显示得过于机械。
  - `HbmIngredient` 普通 tag/item 输入诊断增强：
    - 单对象 `{ "tag": "..." }` 输入输出 `tag #namespace:path` 与 `tag:namespace:path` 分组 key。
    - 单对象 `{ "item": "..." }` 输入输出 `item namespace:path` 与 `item:namespace:path` 分组 key。
    - 这样 `/hbm recipe unresolved` 不再只对 legacy ore/meta 友好，普通现代 tag 空洞也能直接按 tag id 分组补桥。
- 迁移边界：
  - 本批只迁移煤粉基础物品、tag、贴图、语言和 hazard 接线；不迁 `dustSmall` 全材料族、不新增煤焦/煤焦油 metadata 族，也不改变煤矿世界生成。
  - `forge:dusts/coal` 现在承接旧 `dustCoal` 的煤粉条目；`forge:gems/coal` 仍承接 vanilla coal/charcoal。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 静态扫描当前 recipe 引用 tag，除 vanilla `minecraft:logs` 外，所有本地 recipe tag 均有内容。
  - 生成的 `forge:dusts/coal` -> `hbm:powder_coal`，`forge:tiny_dusts/coal` -> `hbm:powder_coal_tiny`。

## 2026-06-02 继续推进：`oil_tar` legacy meta 家族与 tar/dye tag 桥

- 1.7.10 对照：
  - `ModItems.oil_tar` 是 `ItemEnumMulti(EnumTarType.class, true, true)`，旧 meta 顺序为 `CRUDE=0`、`CRACK=1`、`COAL=2`、`WOOD=3`、`WAX=4`、`PARAFFIN=5`。
  - `OreDictManager.registerOres()` 显式注册：
    - `oiltar` -> `oil_tar` meta `CRUDE`。
    - `cracktar` -> `oil_tar` meta `CRACK`。
    - `coaltar` -> `oil_tar` meta `COAL`。
    - `woodtar` -> `oil_tar` meta `WOOD`。
    - `dyeBlack` -> `CRUDE` 与 `CRACK`，`dyeGray` -> `COAL`，`dyeBrown` -> `WOOD`，`dyeCyan` -> `WAX`，`dyeWhite` -> `PARAFFIN`，`dye` -> wildcard `oil_tar`。
  - 贴图来源为 `textures/items/oil_tar.<variant>.png`。
- 本批接入：
  - 现代 `ModItems` 拆出 `oil_tar_crude`、`oil_tar_crack`、`oil_tar_coal`、`oil_tar_wood`、`oil_tar_wax`、`oil_tar_paraffin`，进入 parts tab 与 legacy item lookup。
  - `LegacyMetaItemMappings` 新增旧 `hbm:oil_tar` meta `0..5` 到现代独立 item 的映射，使旧 `ComparableStack(ModItems.oil_tar, meta)` 和旧输出诊断可走统一 HbmIngredient/meta 路径。
  - `HbmItemTagsProvider` 补：
    - `forge:tar` 聚合 tag。
    - `forge:tar/oil`、`forge:tar/crack`、`forge:tar/coal`、`forge:tar/wood`，承接旧 `oiltar/cracktar/coaltar/woodtar`。
    - `forge:dyes` 聚合与 `dyes/black`、`dyes/gray`、`dyes/brown`、`dyes/cyan`、`dyes/white`，承接旧 tar 染料别名。
  - `HbmItemModelProvider` 按 `oil_tar_<variant>` 生成到旧贴图 `item/oil_tar.<variant>`。
  - en_us / zh_cn datagen 补六个 tar 变体名称。
- 迁移边界：
  - 本批只迁 `oil_tar` metadata 物品族和旧 ore/dye tag 桥，不迁 `coke`、`briquette`、焦化/炼油具体 recipe 批量导入，也不新增 tar 方块/流体图标 item 行为。
  - `LegacyOreDictionaryMappings` 原有 `oiltar/cracktar/coaltar/woodtar -> forge:tar/*` 映射保持不变；本批补的是这些 tag 的实际内容。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 生成的 `forge:tar/oil` -> `hbm:oil_tar_crude`，`forge:tar/crack` -> `hbm:oil_tar_crack`，`forge:dyes/black` -> `hbm:oil_tar_crude` / `hbm:oil_tar_crack`。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。

## 2026-06-03 继续推进：`coke` / `briquette` / `powder_ash` legacy meta 家族与 ore/tag 桥

- 1.7.10 对照：
  - `ModItems.coke` 是 `ItemEnumMulti(EnumCokeType.class, true, true)`，旧 meta 顺序为 `COAL=0`、`LIGNITE=1`、`PETROLEUM=2`。
  - `ModItems.briquette` 是 `ItemEnumMulti(EnumBriquetteType.class, true, true)`，旧 meta 顺序为 `COAL=0`、`LIGNITE=1`、`WOOD=2`。
  - `ModItems.powder_ash` 是 `ItemEnumMulti(EnumAshType.class, true, true)`，旧 meta 顺序为 `WOOD=0`、`COAL=1`、`MISC=2`、`FLY=3`、`SOOT=4`、`FULLERENE=5`。
  - `OreDictManager.registerOres()` 显式注册：
    - `CoalCoke.gem()` / `PetCoke.gem()` / `LigniteCoke.gem()` 分别指向三种 coke；`AnyCoke.gem()` 指向全部三种 coke。
    - 兼容旧 ore 名 `coalCoke`、`fuelCoke`、`coke`，其中 `fuelCoke` 与 `coke` 都注册全部三种 coke。
    - `briquetteCoal`、`briquetteLignite`、`briquetteWood` 分别指向三种 briquette。
    - `ANY_ASH.any()` 只包含 `WOOD/COAL/MISC/FLY/SOOT`，不包含 `FULLERENE`。
    - ash 染料别名为 `dyeLightGray=WOOD`、`dyeBlack=COAL+SOOT`、`dyeGray=MISC`、`dyeBrown=FLY`、`dyeMagenta=FULLERENE`，`dye` wildcard 覆盖全部 ash 变体。
  - 贴图来源为 `textures/items/coke.<variant>.png`、`briquette.<variant>.png`、`powder_ash.<variant>.png`。
- 本批接入：
  - 现代 `ModItems` 拆出：
    - `coke_coal`、`coke_lignite`、`coke_petroleum`。
    - `briquette_coal`、`briquette_lignite`、`briquette_wood`。
    - `powder_ash_wood`、`powder_ash_coal`、`powder_ash_misc`、`powder_ash_fly`、`powder_ash_soot`、`powder_ash_fullerene`。
  - `LegacyMetaItemMappings` 新增旧 `hbm:coke`、`hbm:briquette`、`hbm:powder_ash` meta 到现代独立 item 的映射，使旧 recipe、loot、hazard 与物品迁移都走同一解析入口。
  - `LegacyOreDictionaryMappings` 精确补 `fuelCoke` / `coke` -> `forge:gems/coke`；`coalCoke`、`briquette*` 既有映射保持不变。
  - `HbmItemTagsProvider` 补：
    - `forge:gems/coke`、`gems/coal_coke`、`gems/lignite_coke`、`gems/pet_coke` 与聚合 `forge:gems`。
    - `forge:briquettes`、`briquettes/coal`、`briquettes/lignite`、`briquettes/wood`。
    - `forge:any/ash`，按旧 `ANY_ASH` 只放 wood/coal/misc/fly/soot。
    - ash 的 `forge:dyes` 聚合与 `dyes/light_gray`、`black`、`gray`、`brown`、`magenta`。
  - `HbmItemModelProvider` 按拆分 item id 生成到旧 metadata 贴图路径。
  - en_us / zh_cn datagen 补 12 个变体名称。
- 迁移边界：
  - 本批只补 metadata 物品族、tag 内容桥、贴图、模型和语言；不迁 `block_coke`、coker/combination/press/pyro/silex 等具体机器配方，也不新增 ash/coke hazard，因为本次未在旧 `HazardRegistry` 中确认到对应登记。
  - `powder_ash_fullerene` 参与旧 `dye` / `dyeMagenta`，但不参与 `ANY_ASH.any()`，保持 1.7.10 注册差异。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。
  - 生成的 `forge:gems/coke` -> `hbm:coke_coal` / `hbm:coke_lignite` / `hbm:coke_petroleum`。
  - 生成的 `forge:briquettes/coal` -> `hbm:briquette_coal`。
  - 生成的 `forge:any/ash` -> `hbm:powder_ash_wood` / `hbm:powder_ash_coal` / `hbm:powder_ash_misc` / `hbm:powder_ash_fly` / `hbm:powder_ash_soot`，不含 fullerene。
  - 生成的 `forge:dyes/black` -> tar 黑色别名与 `hbm:powder_ash_coal` / `hbm:powder_ash_soot`。

## 2026-06-03 继续推进：轻量 `ItemEnumMulti` 家族 meta 桥批次

- 1.7.10 对照：
  - `chunk_ore = ItemEnumMulti(EnumChunkType.class, true, true)`，旧 meta 顺序为 `RARE=0`、`MALACHITE=1`、`CRYOLITE=2`、`MOONSTONE=3`。
  - `plant_item = ItemEnumMulti(EnumPlantType.class, true, true)`，旧 meta 顺序为 `TOBACCO=0`、`ROPE=1`、`MUSTARDWILLOW=2`。
  - `parts_legendary = ItemEnumMulti(EnumLegendaryType.class, false, true)`，旧 meta 顺序为 `TIER1=0`、`TIER2=1`、`TIER3=2`；旧 lang 是单一 `item.parts_legendary.name=Legendary Parts`。
  - `casing = ItemEnumMulti(EnumCasingType.class, true, true)`，旧 meta 顺序为 `SMALL=0`、`LARGE=1`、`SMALL_STEEL=2`、`LARGE_STEEL=3`、`SHOTSHELL=4`、`BUCKSHOT=5`、`BUCKSHOT_ADVANCED=6`。
  - `fuel_additive = ItemEnumMulti(EnumFuelAdditive.class, true, true)`，旧 meta 顺序为 `ANTIKNOCK=0`、`DEICER=1`。
  - `OreDictManager.registerOres()` 中已确认：
    - `MALACHITE.ingot()` -> `chunk_ore` meta `MALACHITE`。
    - `CRYOLITE.crystal()` -> `chunk_ore` meta `CRYOLITE`。
    - `RAREEARTH.ingot()` -> `chunk_ore` meta `RARE`。
  - 旧配方/物品池引用：
    - `plant_item` 用于烟草、绳、芥子柳叶相关 crafting/crystallizer/smelting。
    - `parts_legendary` tier2 用于 AJR/RPA 装甲配方。
    - `casing` 用于 press、ammo press、assembly、weapon crafting 和 legacy loot pool。
    - `fuel_additive` 用于 chemical plant 输出与 mixer 固体输入。
- 本批接入：
  - 现代 `ModItems` 拆出：
    - `chunk_ore_rare`、`chunk_ore_malachite`、`chunk_ore_cryolite`、`chunk_ore_moonstone`。
    - `plant_item_tobacco`、`plant_item_rope`、`plant_item_mustardwillow`。
    - `parts_legendary_tier1`、`parts_legendary_tier2`、`parts_legendary_tier3`。
    - `casing_small`、`casing_large`、`casing_small_steel`、`casing_large_steel`、`casing_shotshell`、`casing_buckshot`、`casing_buckshot_advanced`。
    - `fuel_additive_antiknock`、`fuel_additive_deicer`；保持旧 `fuel_additive` 的 control tab 归属。
  - `LegacyMetaItemMappings` 新增旧 `hbm:chunk_ore`、`hbm:plant_item`、`hbm:parts_legendary`、`hbm:casing`、`hbm:fuel_additive` meta 到现代独立 item 的映射。
  - `HbmItemTagsProvider` 补旧 `OreDictManager` 明确登记的 chunk ore tag 内容：
    - `forge:ingots/rare_earth` -> `hbm:chunk_ore_rare`。
    - `forge:ingots/malachite` -> `hbm:chunk_ore_malachite`。
    - `forge:crystals/cryolite` -> `hbm:chunk_ore_cryolite`。
    - 对应聚合 `forge:ingots` / `forge:crystals`。
  - `HbmItemModelProvider` 按拆分 item id 生成到旧 metadata 贴图路径：
    - `chunk_ore.<variant>`、`plant_item.<variant>`、`parts_legendary.<tier>`、`casing.<variant>`、`fuel_additive.<variant>`。
  - 从 1.7.10 资源复制 19 张旧贴图到现代 `assets/hbm/textures/item/`。
  - en_us / zh_cn datagen 补 19 个变体名称；`parts_legendary_*` 保持旧单一显示名。
- 迁移边界：
  - 本批只迁轻量 metadata 物品族、meta 映射、明确 ore dict 内容、贴图、模型和语言；不迁 ammo 本体、弹药 press runtime、植物方块行为、月石/稀土完整矿物链，也不凭空为 `plant_item` 猜额外 tag。
  - `chunk_ore_moonstone` 只接 meta 映射和资源；旧 `OreDictManager` 未确认对应 ore dict tag，本批不新增 tag。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。
  - 生成的 `forge:ingots/rare_earth` -> `hbm:chunk_ore_rare`，`forge:ingots/malachite` -> `hbm:chunk_ore_malachite`，`forge:crystals/cryolite` -> `hbm:chunk_ore_cryolite`。
  - 生成的 `chunk_ore_moonstone`、`casing_buckshot_advanced`、`fuel_additive_antiknock` item model 均指向旧 metadata 贴图。
  - 确认未生成本批刻意不猜的 `forge:crops/tobacco`、`forge:ropes`、`forge:ingots/moonstone` tag。

## 2026-06-03 继续推进：`part_generic` meta 桥与旧 dye ore tag 补强

- 1.7.10 对照：
  - `ModItems.part_generic = new ItemGenericPart()`，旧 ID 为 `hbm:part_generic`，进入 `MainRegistry.partsTab`。
  - `ItemGenericPart.EnumPartType` 旧 meta 顺序为：
    - `PISTON_PNEUMATIC=0`，贴图 `textures/items/piston_pneumatic.png`。
    - `PISTON_HYDRAULIC=1`，贴图 `textures/items/piston_hydraulic.png`。
    - `PISTON_ELECTRIC=2`，贴图 `textures/items/piston_electric.png`。
    - `LDE=3`，贴图 `textures/items/low_density_element.png`。
    - `HDE=4`，贴图 `textures/items/heavy_duty_element.png`。
    - `GLASS_POLARIZED=5`，贴图 `textures/items/glass_polarized.png`。
  - 旧配方引用已确认：
    - `AssemblyMachineRecipes` 使用 hydraulic piston、polarized lens 与 LDE。
    - `ChemicalPlantRecipes` 输出 `GLASS_POLARIZED`。
    - `ArcWelderRecipes` 消耗 `LDE`。
    - `PlasmaForgeRecipes` 输出/消耗 `HDE`。
  - `OreDictManager.registerOres()` 显式登记基础 dye 别名：
    - `dyeBlack` / `dye` -> `powder_coal`。
    - `dyeBrown` / `dye` -> `powder_lignite`。
    - `dyeLightGray` / `dye` -> `powder_titanium`。
    - `dyeOrange` / `dye` -> `powder_cadmium`。
    - `dyeRed` / `dye` -> `cinnebar`，`dyeYellow` / `dye` -> `sulfur`，`dyeWhite` / `dye` -> `fluorite`。
  - `OreDictManager` 已明确登记 `LIGNITE.dust(powder_lignite)` 与 `LIMESTONE.dust(powder_limestone)`。
- 本批接入：
  - 现代 `ModItems` 拆出 `part_generic_piston_pneumatic`、`part_generic_piston_hydraulic`、`part_generic_piston_electric`、`part_generic_lde`、`part_generic_hde`、`part_generic_glass_polarized`。
  - `LegacyMetaItemMappings` 新增旧 `hbm:part_generic` meta `0..5` 到现代独立 item 的映射，使旧 assembly/chemical/plasma/arc welder recipe 输入输出能走统一 `HbmIngredient` meta 入口。
  - `HbmItemModelProvider` 为 `part_generic_*` 生成到旧贴图名的模型，其中 `LDE/HDE/GLASS_POLARIZED` 保留旧自定义贴图路径。
  - 从 1.7.10 资源复制 6 张旧贴图到现代 `assets/hbm/textures/item/`。
  - en_us / zh_cn datagen 补 6 个 `part_generic` 变体名称，并补 `powder_lignite` / `powder_limestone` 的显式名称，避免 fallback 名称偏离旧 lang。
  - `HbmItemTagsProvider` 增加旧 dye ore dict 桥；只填现代端已经注册的 item，不为本批未迁的 `powder_lapis` 等缺口造物品。
- 迁移边界：
  - 本批只迁 `part_generic` metadata 物品族、模型/贴图/语言和已确认的 dye tag 桥；不迁 `ore_byproduct`、bedrock ore byproduct、具体机器配方批量导入或 JEI 显示。
  - `powder_lignite` 既有 `HazardRegistry` 煤尘 hazard 保持不变；`part_generic` 未在旧 hazard 注册中确认危险属性，本批不新增 hazard。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。
  - 生成的 `forge:dyes/black` -> tar 黑色别名、`hbm:powder_coal`、`hbm:powder_ash_coal`、`hbm:powder_ash_soot`。
  - 生成的 `forge:dyes/brown` -> `hbm:oil_tar_wood`、`hbm:powder_lignite`、`hbm:powder_ash_fly`。
  - 生成的 `part_generic_lde` / `part_generic_hde` / `part_generic_glass_polarized` item model 分别指向 `low_density_element` / `heavy_duty_element` / `glass_polarized`。

## 2026-06-03 继续推进：`item_expensive` meta 桥与昂贵模式部件

- 1.7.10 对照：
  - `ModItems.item_expensive = new ItemExpensive().setUnlocalizedName("item_expensive").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":item_expensive")`，旧 ID 为 `hbm:item_expensive`。
  - `ItemExpensive` 继承 `ItemEnumMulti(EnumExpensiveType.class, true, true)`，并在 tooltip 追加红色文本 `Expensive mode item`。
  - `ItemEnums.EnumExpensiveType` 旧 meta 顺序为：
    - `STEEL_PLATING=0`，`HEAVY_FRAME=1`，`CIRCUIT=2`，`LEAD_PLATING=3`，`FERRO_PLATING=4`，`COMPUTER=5`，`BRONZE_TUBES=6`，`PLASTIC=7`，`GOLD_DUST=8`，`DEGENERATE_MATTER=9`。
  - 旧配方引用已确认：
    - `AssemblyMachineRecipes` 大量消耗/输出昂贵模式部件。
    - `PlasmaForgeRecipes` 消耗 `BRONZE_TUBES`、`FERRO_PLATING`、`STEEL_PLATING`、`COMPUTER` 等变体。
    - `ParticleAcceleratorRecipes` 使用 `GOLD_DUST` 并输出 `DEGENERATE_MATTER`。
    - `ExposureChamberRecipes` 使用 `DEGENERATE_MATTER`。
  - 旧 lang：
    - en_US：`Bolted Steel Plating`、`Heavy Framework`、`Extensive Circuit Board`、`Radiation Resistant Plating`、`Reinforced Ferrouranium Panels`、`Mainframe`、`Bronze Structural Elements`、`Plastic Panels`、`Ultra Fine Gold Dust`、`Degenerate Matter`。
    - zh_CN：`铆接固定钢板`、`重型框架`、`大型电路板`、`防辐射镀层`、`强化铀铁合金板`、`处理器主机`、`青铜结构件`、`塑料板`、`超精细金粉`、`简并态物质`。
  - 旧贴图位于 `assets/hbm/textures/items/item_expensive.<variant>.png`；同目录存在 `item_expensive.bronze_tubes_alt.png`，但本批未发现枚举或注册使用证据。
- 本批接入：
  - 现代 `ModItems` 拆出 `item_expensive_steel_plating`、`item_expensive_heavy_frame`、`item_expensive_circuit`、`item_expensive_lead_plating`、`item_expensive_ferro_plating`、`item_expensive_computer`、`item_expensive_bronze_tubes`、`item_expensive_plastic`、`item_expensive_gold_dust`、`item_expensive_degenerate_matter`，并保持 parts tab 归属。
  - 新增 `ExpensiveModeItem`，为所有拆分变体保留旧 `Expensive mode item` 红色 tooltip。
  - `LegacyMetaItemMappings` 新增旧 `hbm:item_expensive` meta `0..9` 到现代独立 item 的映射，使旧 recipe、loot、hazard 与物品迁移能共用 `HbmIngredient` / legacy meta 入口。
  - `HbmItemModelProvider` 为 `item_expensive_*` 生成到旧 `item_expensive.<variant>` 贴图路径的模型。
  - 从 1.7.10 资源复制 10 张旧贴图到现代 `assets/hbm/textures/item/`。
  - en_us / zh_cn datagen 补 10 个变体名称；tooltip 保留旧硬编码英文文本。
- 迁移边界：
  - 本批只迁 `item_expensive` metadata 物品族、tooltip、meta 映射、贴图、模型和语言；不迁昂贵模式配置开关、具体机器配方批量导入或 JEI 显示。
  - 未在旧 hazard 注册中确认 `item_expensive` 危险属性，本批不新增 hazard。
  - 未使用 `item_expensive.bronze_tubes_alt.png`，等待后续源码证据。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。
  - 生成的 `item_expensive_steel_plating` / `item_expensive_degenerate_matter` item model 分别指向 `item_expensive.steel_plating` / `item_expensive.degenerate_matter`。
  - 生成的 en_us / zh_cn lang 分别包含旧英文名、旧中文名和旧硬编码 tooltip 文本。

## 2026-06-04 继续推进：`ore_byproduct` meta 桥与彩色副产物碎片

- 1.7.10 对照：
  - `ModItems.ore_byproduct = new ItemByproduct().setUnlocalizedName("ore_byproduct").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":byproduct")`。
  - `ItemByproduct` 继承 `ItemEnumMulti(EnumByproduct.class, true, false)`，旧资源为单张 `textures/items/byproduct.png`，每个 meta 通过 `getColorFromItemStack` 返回枚举颜色。
  - `EnumByproduct` 旧 meta 顺序与颜色：
    - `B_IRON=0` `0xE2C0AA`
    - `B_COPPER=1` `0xEC9A63`
    - `B_LITHIUM=2` `0xEDEDED`
    - `B_SILICON=3` `0xFFFBD1`
    - `B_LEAD=4` `0x646470`
    - `B_TITANIUM=5` `0xF2EFE2`
    - `B_ALUMINIUM=6` `0xE8F2F9`
    - `B_SULFUR=7` `0xEAD377`
    - `B_CALCIUM=8` `0xCFCFA6`
    - `B_BISMUTH=9` `0x8D8577`
    - `B_RADIUM=10` `0xE9FAF6`
    - `B_TECHNETIUM=11` `0xCADFDF`
    - `B_POLONIUM=12` `0xCADFDF`
    - `B_URANIUM=13` `0x868D82`
  - 旧配方引用：
    - `CentrifugeRecipes` 将 bedrock ore / ore centrifuge 副产物输出为 `DictFrame.fromOne(ModItems.ore_byproduct, EnumByproduct, 1)`。
    - `MineralRecipes` 定义 9 合 1 的 byproduct -> powder/billet/sulfur 转换。
- 本批接入：
  - 新增现代 `OreByproductItem`，保存 legacy tint 颜色并通过 client item color handler 对 layer 0 染色。
  - 现代 `ModItems` 拆出 14 个独立物品：
    - `ore_byproduct_b_iron`
    - `ore_byproduct_b_copper`
    - `ore_byproduct_b_lithium`
    - `ore_byproduct_b_silicon`
    - `ore_byproduct_b_lead`
    - `ore_byproduct_b_titanium`
    - `ore_byproduct_b_aluminium`
    - `ore_byproduct_b_sulfur`
    - `ore_byproduct_b_calcium`
    - `ore_byproduct_b_bismuth`
    - `ore_byproduct_b_radium`
    - `ore_byproduct_b_technetium`
    - `ore_byproduct_b_polonium`
    - `ore_byproduct_b_uranium`
  - `LegacyMetaItemMappings` 新增旧 `hbm:ore_byproduct` meta `0..13` 到现代独立 item 的映射，使旧 recipe、loot、hazard 与物品迁移能共用 `HbmIngredient` / legacy meta 入口。
  - `HbmItemModelProvider` 为所有 `ore_byproduct_*` 生成到旧单贴图 `item/byproduct`。
  - 从 1.7.10 资源复制 `byproduct.png` 到现代 `assets/hbm/textures/item/`。
  - en_us / zh_cn datagen 补 14 个旧语言名。
- 迁移边界：
  - 本批只迁 `ore_byproduct` metadata 物品族、legacy meta 映射、单贴图 tint、模型和语言。
  - 旧源码未确认 `ore_byproduct` 有直接 `OreDictManager` 登记，本批不新增 ore/tag 桥。
  - 旧源码未确认 `ore_byproduct` 有直接 hazard 登记，本批不新增 hazard；放射性相关后续应由对应 material/powder/billet 产物链或明确旧 hazard 证据驱动。
  - 不迁 `MineralRecipes` 的 9 合 1 crafting、`CentrifugeRecipes` 批量 recipe、bedrock ore runtime，也不使用未确认的 `byproduct_base_*` 备用贴图。

## 2026-06-04 继续推进：`stamp_book` / `page_of_` 打印 press 链路 meta 桥

- 1.7.10 对照：
  - `ModItems.stamp_book = new ItemStampBook().setUnlocalizedName("stamp_book").setMaxStackSize(1).setCreativeTab(null).setTextureName(RefStrings.MODID + ":stamp_book")`。
  - `ItemStampBook` 继承 `ItemStamp`，旧 meta `0..7` 分别映射到 `StampType.PRINTING1..PRINTING8`，并注册进旧 `ItemStamp.stamps`。
  - `ModItems.page_of_ = new ItemEnumMulti(ItemEnums.EnumPages.class, true, false).setUnlocalizedName("page_of_").setMaxStackSize(1).setCreativeTab(null).setTextureName(RefStrings.MODID + ":page_of_")`，旧 meta 顺序为 `PAGE1..PAGE8`。
  - `PressRecipes` 定义 8 条旧打印配方：
    - `PRINTING1 + paper -> page_of_ PAGE1`
    - `PRINTING2 + paper -> page_of_ PAGE2`
    - `PRINTING3 + paper -> page_of_ PAGE3`
    - `PRINTING4 + paper -> page_of_ PAGE4`
    - `PRINTING5 + paper -> page_of_ PAGE5`
    - `PRINTING6 + paper -> page_of_ PAGE6`
    - `PRINTING7 + paper -> page_of_ PAGE7`
    - `PRINTING8 + paper -> page_of_ PAGE8`
  - `ItemPoolsComponent` 会把 `stamp_book` meta `0..7` 作为旧战利品池条目。
- 本批接入：
  - 扩展现代 `ItemPressStamp.StampType`，新增 `PRINTING1..PRINTING8`，使现代 press recipe serializer/runtime 能识别旧打印 stamp 类型。
  - 现代 `ModItems` 拆出隐藏配方物品：
    - `stamp_book_printing1..stamp_book_printing8`
    - `page_of_page1..page_of_page8`
  - 新增 `ModItems.HIDDEN_RECIPE_ITEMS`，供模型和语言 datagen 遍历隐藏配方/战利品用物品，不把它们放入创造栏。
  - `LegacyMetaItemMappings` 新增：
    - 旧 `hbm:stamp_book` meta `0..7` -> 现代 `stamp_book_printing1..8`
    - 旧 `hbm:page_of_` meta `0..7` -> 现代 `page_of_page1..8`
  - `HbmItemModelProvider` 为隐藏拆分物品生成模型：
    - `stamp_book_*` -> 旧单贴图 `item/stamp_book`
    - `page_of_*` -> 旧单贴图 `item/page_of_`
  - 从 1.7.10 资源复制 `stamp_book.png` 与 `page_of_.png` 到现代 `assets/hbm/textures/item/`。
  - en_us / zh_cn datagen 补 16 个旧语言名。
- 迁移边界：
  - 本批只迁打印 press 链路的隐藏 metadata 物品族、press stamp 类型、legacy meta 映射、模型/贴图/语言。
  - 不批量导入 `PressRecipes` 的 8 条打印 recipe；后续可直接用现代 press recipe JSON 的 `stamp: "printingN"` 与 `outputLegacyMeta(PAGE_OF, n)` 落地。
  - 不迁 `book_of_`、`CraftingManager` 的 8 页合成书、书 GUI 或 lore book 系统。
  - `stamp_book` 在旧版 `creativeTab(null)`，现代拆分项保持隐藏，只进入统一 legacy lookup 和 datagen。

## 2026-06-04 继续推进：`upgrade_template` / `template_folder` 普通 legacy 物品落点

- 1.7.10 对照：
  - `ModItems.upgrade_template = new ItemCustomLore().setUnlocalizedName("upgrade_template").setCreativeTab(MainRegistry.partsTab).setTextureName(RefStrings.MODID + ":upgrade_template")`。
  - `ModItems.template_folder = new Item().setUnlocalizedName("template_folder").setTextureName(RefStrings.MODID + ":template_folder")`，旧注册未设置 creative tab。
  - 旧配方引用：
    - `CraftingManager` 使用 `upgrade_template` 合成多种机器升级，并在早期 crafting 中生成 `upgrade_template`。
    - `SolderingRecipes` 使用 `upgrade_template` 作为多条升级配方输入。
    - `CraftingManager` 使用 paper + dye 合成 `template_folder`。
  - 旧语言：
    - en_US：`Machine Upgrade Template`、`Machine Template Folder`，`template_folder.desc` 为 `$` 分隔的提示文本。
    - zh_CN：`机器升级模板`、`机器模板文件夹`，并有 `template_folder.desc`。
- 本批接入：
  - 现代 `ModItems` 新增 `upgrade_template` 普通 parts-tab 物品，给旧配方 importer / `ModItems.legacyItem("upgrade_template")` 提供稳定落点。
  - 现代 `ModItems` 新增 `template_folder` 普通隐藏物品，并加入 `HIDDEN_RECIPE_ITEMS` 以生成模型/语言但不进入创造栏。
  - 从 1.7.10 资源复制 `upgrade_template.png`、`template_folder.png` 到现代 `assets/hbm/textures/item/`。
  - en_us / zh_cn datagen 补两个物品名和旧 `template_folder.desc` 文本键。
- 迁移边界：
  - 本批只补普通 legacy item ID 的解析落点、贴图、模型和语言，改善配方/战利品/hazard/物品迁移对旧 ID 的共用查找。
  - 不迁 `ItemCustomLore` 通用 tooltip 系统；`template_folder.desc` 只作为旧语言内容保留。
  - 不迁 template tab、blueprints、machine template GUI、assembly/chemistry/crucible template 行为或配方。

## 2026-06-04 继续推进：press recipe datagen 与弹壳 stamp 链路

- 1.7.10 对照：
  - `ItemStamp.StampType` 旧枚举顺序包含 `FLAT`、`PLATE`、`WIRE`、`CIRCUIT`、`C357`、`C44`、`C50`、`C9`、`PRINTING1..PRINTING8`。
  - `ModItems` 注册普通弹壳 stamp：
    - `stamp_357 = new ItemStamp(1000, StampType.C357)`，贴图 `stamp_357`。
    - `stamp_44 = new ItemStamp(1000, StampType.C44)`，贴图 `stamp_44`。
    - `stamp_9 = new ItemStamp(1000, StampType.C9)`，贴图 `stamp_9`。
    - `stamp_50 = new ItemStamp(1000, StampType.C50)`，贴图 `stamp_50`。
  - `PressRecipes` 定义 8 条打印配方：
    - `PRINTING1..8 + paper -> page_of_ PAGE1..8`。
  - `PressRecipes` 定义 4 条弹壳压制配方：
    - `C9 + plateGunMetal -> casing SMALL x4`。
    - `C50 + plateGunMetal -> casing LARGE x2`。
    - `C9 + plateWeaponSteel -> casing SMALL_STEEL x4`。
    - `C50 + plateWeaponSteel -> casing LARGE_STEEL x2`。
- 本批接入：
  - 现代 `ItemPressStamp.StampType` 新增 `C357("357")`、`C44("44")`、`C50("50")`、`C9("9")`，保持现代 press JSON 的 `stamp` 字段可表达旧小/大口径弹壳锻模。
  - 现代 `ModItems` 新增普通 parts-tab stamp：
    - `stamp_357`
    - `stamp_44`
    - `stamp_9`
    - `stamp_50`
  - 四个 stamp 通过 `registerLegacy(...)` 登记，保证后续旧配方 importer、战利品、hazard、物品迁移可以用 `ModItems.legacyItem(...)` 解析旧 ID。
  - 从 1.7.10 资源复制 4 张旧贴图到现代 `assets/hbm/textures/item/`：
    - `stamp_357.png`
    - `stamp_44.png`
    - `stamp_9.png`
    - `stamp_50.png`
  - en_us / zh_cn datagen 补四个旧 stamp 名称。
  - `HbmRecipeProvider` 新增轻量 `PressRecipeBuilder`，统一生成现代 `hbm:press` JSON，字段为：
    - `stamp`
    - `ingredient`
    - `result`
  - 用 `LegacyMetaItemMappings.PAGE_OF` 生成 8 条打印 press 配方：
    - `press/page_of_page1..8`
  - 用 `LegacyMetaItemMappings.CASING` 和旧 `plateGunMetal` tag 桥生成 2 条 gunmetal 弹壳 press 配方：
    - `press/casing_small_gunmetal`：`stamp: "9"`，`forge:plates/gun_metal` -> `casing_small x4`。
    - `press/casing_large_gunmetal`：`stamp: "50"`，`forge:plates/gun_metal` -> `casing_large x2`。
  - 修复 `HbmBlockStateProvider` 中 OBJ datagen loader 构造入口，改用 Forge 公开 `ObjModelBuilder::begin`，解除当前 `compileJava` 阻塞。
- 迁移边界：
  - 本批只迁普通 `stamp_357/44/9/50`，不迁 `stamp_desh_357/44/9/50`，因为 desh stamp 旧版耐久为 0 且需要后续结合完整 stamp 材质族/耐久语义统一处理。
  - 本批只生成 gunmetal 两条弹壳 press 配方；`WeaponSteel` 现代物品和 tag 落点尚未确认，不生成 `casing_small_steel` / `casing_large_steel` 两条配方，避免把旧 `plateWeaponSteel` 静默映射到错误材料。
  - 本批不迁 ammo press runtime、子弹本体、弹药战利品池或弹药 hazard。
  - `PressRecipeBuilder` 是当前 press JSON 的数据生成入口，不改变 press machine runtime 语义。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 生成的 `src/generated/resources/data/hbm/recipes/press/page_of_page1.json` 为 `stamp: "printing1"` + `minecraft:paper` -> `hbm:page_of_page1`。
  - 生成的 `press/casing_small_gunmetal.json` / `press/casing_large_gunmetal.json` 分别为 `stamp: "9"` / `"50"`，输入 `forge:plates/gun_metal`，输出 `hbm:casing_small x4` / `hbm:casing_large x2`。
  - 生成的 `stamp_9` / `stamp_50` item model 指向旧贴图 `hbm:item/stamp_9` / `hbm:item/stamp_50`。
  - 生成的 en_us / zh_cn lang 包含 `stamp_357`、`stamp_44`、`stamp_9`、`stamp_50`。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。

## 2026-06-04 继续推进：通用机器 recipe type 覆盖 PUREX / 精密装配

- 1.7.10 对照：
  - `SerializableRecipe#registerDefaults()` 会登记 `PUREXRecipes.INSTANCE`、`PrecAssRecipes.INSTANCE`、`PlasmaForgeRecipes.INSTANCE`、`FusionRecipes.INSTANCE`、`CrucibleRecipes.INSTANCE` 等可序列化配方集合。
  - `PUREXRecipes extends GenericRecipes<GenericRecipe>`：
    - 文件名 `hbmPUREX.json`。
    - `inputItemLimit=3`、`inputFluidLimit=3`、`outputItemLimit=6`、`outputFluidLimit=1`。
    - 运行时通过 `ModuleMachinePUREX#getRecipeSet()` 接到 `PUREXRecipes.INSTANCE`。
  - `PrecAssRecipes extends GenericRecipes<GenericRecipe>`：
    - 文件名 `hbmPrecisionAssembly.json`。
    - `inputItemLimit=9`、`inputFluidLimit=1`、`outputItemLimit=9`、`outputFluidLimit=1`。
    - 运行时通过 `ModuleMachinePrecAss#getRecipeSet()` 接到 `PrecAssRecipes.INSTANCE`。
  - `PlasmaForgeRecipes extends GenericRecipes<PlasmaForgeRecipe>`，普通通用字段外还要求额外 JSON 字段 `ignitionTemp`。
  - `FusionRecipes extends GenericRecipes<FusionRecipe>`，普通通用字段外还要求 `ignitionTemp`、`outputTemp`、`outputFlux`、`r`、`g`、`b`，并在 `registerPost()` 统计 `maxInput`。
  - `CrucibleRecipes` 虽继承 `GenericRecipes<CrucibleRecipe>`，但自定义 `frequency`、`input/output MaterialStack`、mold/smelting NEI 查询等格式，不属于当前 `GenericMachineRecipe` 普通字段集。
- 本批接入：
  - `GenericMachineRecipe.Machine` 新增：
    - `PUREX(3, 3, 6, 1)`。
    - `PRECASS(9, 1, 9, 1)`。
  - `ModRecipes` 注册现代 recipe type / serializer：
    - `hbm:purex`。
    - `hbm:precass`。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 新增：
    - `purex(...)`。
    - `precass(...)`。
  - 现有 `/hbm recipe audit [machine]`、`/hbm recipe unresolvedInputs [machine]` 等命令会随 `Machine.values()` 自动覆盖这两个新类型。
- 迁移边界：
  - 本批只迁可与当前普通 `GenericMachineRecipe` 字段完全对齐的 recipe type/loader 入口；不批量导入具体 PUREX/精密装配配方。
  - `machine_purex` 现代端已有 block/model 落点，toast symbol 使用 `ModBlocks.MACHINE_PUREX`。
  - 旧 `machine_precass` 现代端尚未注册；`PRECASS` 目前只是 recipe type/loader 落点，toast symbol 暂用同 legacy 模型族的 `machine_assembly_machine` 作为展示兜底。后续迁 `machine_precass` 方块/BlockEntity/GUI 后应改为真实方块。
  - `PlasmaForgeRecipes` / `FusionRecipes` 不能仅靠当前普通 serializer 迁移；必须先补额外字段模型、网络同步、显示行和对应机器运行时，再接入正式机器枚举。
  - `CrucibleRecipes` 属于材料坩埚特殊库，应单独迁移 `CrucibleRecipe` / material stack / mold recipes，不并入普通机器配方。

## 2026-06-04 继续推进：GenericRecipes extra data 承接层

- 1.7.10 对照：
  - `GenericRecipes#readRecipe(...)` 在读取普通字段后调用 `readExtraData(element, recipe)`，写出时在普通字段后调用 `writeExtraData(recipe, writer)`。
  - `PlasmaForgeRecipes#readExtraData(...)` / `writeExtraData(...)` 只读写：
    - `ignitionTemp`：`PlasmaForgeRecipe.ignitionTemp`，含义为 minimum plasma energy，GUI 显示为 `gui.recipe.plasmaIn` + `TU/t`。
  - `FusionRecipes#readExtraData(...)` / `writeExtraData(...)` 读写：
    - `ignitionTemp`：minimum klystron energy，GUI 显示 `gui.recipe.fusionIn` + `KyU/t`。
    - `outputTemp`：plasma output energy，GUI 显示 `gui.recipe.fusionOut` + `TU/t`。
    - `outputFlux`：neutron output flux，GUI 显示 `gui.recipe.fusionFlux` + `flux/t`。
    - `r` / `g` / `b`：fusion recipe color。
  - `FusionRecipes#registerPost()` 会遍历全部 fusion recipe 的 `ignitionTemp` 计算 `maxInput`，供 creative klystron 使用。
- 本批接入：
  - 新增 `GenericMachineRecipeExtraData`，作为现代 `GenericMachineRecipe` 的强类型 extra data 容器：
    - `PlasmaForge(long ignitionTemp)`。
    - `Fusion(long ignitionTemp, long outputTemp, double outputFlux, float r, float g, float b)`。
  - `GenericMachineRecipe`：
    - 构造、getter、JSON 读取、network 同步均保留 extra data。
    - `getDisplayLines()` 增加 legacy 对应的 plasma/fusion 能量与通量显示行。
    - `getSearchText()` 纳入 extra data 数值，方便命令/界面筛选和审计。
  - `LegacyGenericRecipeFormat#writeLegacyRecipe(...)` 写回 legacy JSON 时保留 extra data 字段。
  - `LegacyGenericRecipeImporter#toModernJson(...)` 从 legacy JSON 导入现代 JSON 时保留 `ignitionTemp`、`outputTemp`、`outputFlux`、`r`、`g`、`b`。
  - `HbmRecipeProvider.GenericMachineRecipeBuilder` 新增：
    - `plasmaForgeExtra(long ignitionTemp)`。
    - `fusionExtra(long ignitionTemp, long outputTemp, double outputFlux, float r, float g, float b)`。
- 迁移边界：
  - 本批只迁 `GenericRecipes#readExtraData/writeExtraData` 的现代承接层；不注册 `PLASMA_FORGE` / `FUSION` 为正式 `GenericMachineRecipe.Machine`。
  - 当前现代端只有 fusion/plasma OBJ 模型资源，尚未在 `ModBlocks` 注册 `fusion_plasma_forge`、`fusion_torus` 等运行时机器方块；在机器本体、BlockEntity、GUI、能量/等离子/聚变节点运行时迁完前，不应把这些 recipe type 暴露成可运行机器。
  - extra data 的 JSON 字段名保持 1.7.10 原名大小写，不改成 snake_case，避免 legacy JSON 导入/导出时丢失对应关系。
  - `GenericMachineRecipeExtraData#fromJson(...)` 以 fusion 专属字段 `outputTemp/outputFlux/r/g/b` 判断 fusion；只有 `ignitionTemp` 时按 plasma forge extra data 处理，与 legacy 两个 `readExtraData` 实现一致。
  - `FusionRecipes#maxInput` 尚未迁；后续迁 creative klystron / fusion runtime 时应通过 fusion recipe extra data 计算。

## 2026-06-04 继续推进：legacy generic recipe handler 文件名注册表

- 1.7.10 对照：
  - `SerializableRecipe#registerAllHandlers()` 在 `GENERIC` 段登记：
    - `CrucibleRecipes.INSTANCE`
    - `AssemblyMachineRecipes.INSTANCE`
    - `ChemicalPlantRecipes.INSTANCE`
    - `PUREXRecipes.INSTANCE`
    - `FusionRecipes.INSTANCE`
    - `PrecAssRecipes.INSTANCE`
    - `PlasmaForgeRecipes.INSTANCE`
  - 对应 legacy JSON 文件名：
    - `AssemblyMachineRecipes#getFileName()` -> `hbmAssemblyMachine.json`
    - `ChemicalPlantRecipes#getFileName()` -> `hbmChemicalPlant.json`
    - `PUREXRecipes#getFileName()` -> `hbmPUREX.json`
    - `PrecAssRecipes#getFileName()` -> `hbmPrecisionAssembly.json`
    - `PlasmaForgeRecipes#getFileName()` -> `hbmPlasmaForge.json`
    - `FusionRecipes#getFileName()` -> `hbmFusion.json`
    - `CrucibleRecipes#getFileName()` -> `hbmCrucible.json`
  - 旧 `SerializableRecipe#initialize()` 以 handler 的 `getFileName()` 查配置目录中的 JSON、同步流或模板文件。
- 本批接入：
  - 新增 `LegacyGenericRecipeHandlers`，集中记录 legacy generic recipe 文件名到现代导入目标的映射。
  - 已支持导入的普通 `GenericRecipe` 文件：
    - `hbmAssemblyMachine.json` -> `GenericMachineRecipe.Machine.ASSEMBLY_MACHINE`，现代输出目录 `hbm:assembly_machine`。
    - `hbmChemicalPlant.json` -> `GenericMachineRecipe.Machine.CHEMICAL_PLANT`，现代输出目录 `hbm:chemical_plant`。
    - `hbmPUREX.json` -> `GenericMachineRecipe.Machine.PUREX`，现代输出目录 `hbm:purex`。
    - `hbmPrecisionAssembly.json` -> `GenericMachineRecipe.Machine.PRECASS`，现代输出目录 `hbm:precass`。
  - 明确登记但暂不支持的文件：
    - `hbmPlasmaForge.json`：需要 `fusion_plasma_forge` 机器运行时和 plasma ignition 处理。
    - `hbmFusion.json`：需要 `fusion_torus` 运行时、klystron energy、output flux、recipe color、`maxInput` 处理。
    - `hbmCrucible.json`：使用材料坩埚自定义格式，不属于普通 `GenericRecipe` 字段集。
  - `LegacyGenericRecipeImporter` 新增按 legacy 文件名读取的重载：
    - `read(String legacyFileName, Reader reader)`
    - `readWithReport(String legacyFileName, Reader reader)`
    - `readLenientWithReport(String legacyFileName, Reader reader)`
  - `/hbm recipe legacyHandlers` 输出当前 legacy generic 文件名注册状态，便于后续批量导入前核对。
- 迁移边界：
  - 本批只是 loader/importer registry；不新增具体 recipe JSON，不从磁盘自动扫描或写入 datapack。
  - 旧 `SerializableRecipe` 的配置目录模板写出、客户端同步 `recipeSyncHandlers`、`IRecipeRegisterListener` 仍未迁移；现代端应优先走 datapack reload 与 recipe manager。
  - 对 `hbmPlasmaForge.json` / `hbmFusion.json` / `hbmCrucible.json` 使用 `requireSupported(...)` 会明确失败，而不是静默导入成错误 machine。

## 2026-06-04 继续推进：`SerializableRecipe` 总 handler 覆盖索引

- 1.7.10 对照：
  - `SerializableRecipe#registerAllHandlers()` 会按固定顺序登记 44 个可序列化配方 handler：
    - `PressRecipes`
    - `BlastFurnaceRecipes`
    - `ShredderRecipes`
    - `SolderingRecipes`
    - `CombinationRecipes`
    - `CentrifugeRecipes`
    - `CrystallizerRecipes`
    - `RefineryRecipes`
    - `VacuumRefineryRecipes`
    - `FractionRecipes`
    - `CrackingRecipes`
    - `ReformingRecipes`
    - `HydrotreatingRecipes`
    - `LiquefactionRecipes`
    - `SolidificationRecipes`
    - `CokerRecipes`
    - `PyroOvenRecipes`
    - `BreederRecipes`
    - `CyclotronRecipes`
    - `FuelPoolRecipes`
    - `MixerRecipes`
    - `OutgasserRecipes`
    - `FluidBreederRecipes`
    - `CompressorRecipes`
    - `ElectrolyserFluidRecipes`
    - `ElectrolyserMetalRecipes`
    - `ArcWelderRecipes`
    - `RotaryFurnaceRecipes`
    - `ExposureChamberRecipes`
    - `ParticleAcceleratorRecipes`
    - `AmmoPressRecipes`
    - `AnvilRecipes`
    - `PedestalRecipes`
    - `AnnihilatorRecipes`
    - `CrucibleRecipes.INSTANCE`
    - `AssemblyMachineRecipes.INSTANCE`
    - `ChemicalPlantRecipes.INSTANCE`
    - `PUREXRecipes.INSTANCE`
    - `FusionRecipes.INSTANCE`
    - `PrecAssRecipes.INSTANCE`
    - `PlasmaForgeRecipes.INSTANCE`
    - `MatDistribution`
    - `CustomMachineRecipes`
    - `ArcFurnaceRecipes`
  - 旧初始化流程：
    - 先 `deleteRecipes()`。
    - 若有同步流或 config 目录同名 JSON，读入并标记 `modified=true`。
    - 否则执行 `registerDefaults()`，通知 `IRecipeRegisterListener`，写出带 `_` 前缀的模板文件。
    - 最后每个 handler 都调用 `registerPost()`。
- 本批接入：
  - 新增 `LegacySerializableRecipeHandlers`，集中记录旧 `SerializableRecipe#registerAllHandlers()` 的完整文件名、旧类名、现代分类与导入状态。
  - 当前状态分类：
    - `SUPPORTED_GENERIC`：可由 `LegacyGenericRecipeImporter` 安全读取的普通 `GenericRecipe` 文件。
      - `hbmAssemblyMachine.json`
      - `hbmChemicalPlant.json`
      - `hbmPUREX.json`
      - `hbmPrecisionAssembly.json`
    - `MODERN_SERIALIZER_ONLY`：现代端已有对应 serializer 或部分 JSON 落点，但尚未有完整 legacy bulk importer。
      - `hbmPress.json` -> `hbm:press`
      - `hbmLiquefactor.json` -> `hbm:liquefaction`
    - `UNSUPPORTED`：旧格式需要专用机器/流体/核燃料/材料库迁移，不能丢给普通 `GenericMachineRecipe`。
  - `LegacyGenericRecipeImporter#readWithReport(String, Reader)` 改为先经 `LegacySerializableRecipeHandlers.requireSupportedGeneric(...)` 护栏确认，再进入旧 generic handler 映射。
  - `LegacyGenericRecipeImporter.ImportReport` 新增：
    - `legacyFileName`
    - `legacyClassName`
    - `machine`
    - `outputFolder`
    用于后续批量导入工具或命令输出溯源。
  - `/hbm recipe legacySerializableHandlers` 列出 44 个旧 handler 的覆盖状态和导入边界。
- 迁移边界：
  - 本批不自动扫描 config 目录，也不写出 datapack bulk recipe；只补 1.7.10 handler 总目录与导入护栏。
  - `IRecipeRegisterListener` / `CompatRecipeRegistry` 的动态注册桥尚未迁移；现代端仍应优先通过 datapack recipe reload 和生成器落地。
  - `recipeSyncHandlers` 的客户端同步流不迁为旧式全量 JSON 覆盖；现代配方同步应走 1.20.1 recipe manager 或专用网络同步。
  - 对 `hbmPlasmaForge.json`、`hbmFusion.json`、`hbmCrucible.json` 继续显式拒绝普通导入，避免把 extra data 或 material stack 格式误装成可运行普通机器配方。

## 2026-06-04 继续推进：SA326 / CMB / Saturnite / DuraSteel 材料 tag 与 plate recipe 链路

- 1.7.10 对照：
  - `OreDictManager` 定义并注册：
    - `SA326 = new DictFrame("Schrabidium")`，含 `ingot_schrabidium` / `powder_schrabidium` / `plate_schrabidium`。
    - `DURA = new DictFrame("DuraSteel")`，含 `ingot_dura_steel` / `powder_dura_steel` / `plate_dura_steel`。
    - `CMB = new DictFrame("CMBSteel")`，含 `ingot_combine_steel` / `powder_combine_steel` / `plate_combine_steel`。
    - `BIGMT = new DictFrame("Saturnite")`，含 `ingot_saturnite` / `plate_saturnite`。
  - `AssemblyMachineRecipes` 的 `autoswitch.plates` 组包含：
    - `ass.plateschrab`
    - `ass.platecmb`
    - `ass.plateweaponsteel`
    - `ass.platesaturnite`
    - `ass.platedura`
  - `PressRecipes` 对同一批材料提供 `StampType.PLATE + ingot ore dict -> plate` 的压机配方。
- 本批接入：
  - 现代 `ModItems.EXTRA_PARTS_TAB_ITEMS` 补普通 legacy item 落点：
    - `ingot_dura_steel`
    - `powder_dura_steel`
    - `ingot_combine_steel`
    - `powder_combine_steel`
    - `plate_combine_steel`
    - `ingot_saturnite`
    - `plate_saturnite`
    - `plate_schrabidium`
  - 从 1.7.10 资源复制对应旧贴图到现代 `assets/hbm/textures/item/`。
  - `HbmItemTagsProvider` 补 legacy ore/tag alias：
    - `ingotDuraSteel` / `dustDuraSteel` / `plateDuraSteel`
    - `ingotCMBSteel` / `dustCMBSteel` / `plateCMBSteel`
    - `ingotSaturnite` / `plateSaturnite`
    - `plateSchrabidium`
  - `HbmRecipeProvider` 生成装配机 plate recipe：
    - `assembly_machine/plate_schrabidium`
    - `assembly_machine/plate_combine_steel`
    - `assembly_machine/plate_weaponsteel`
    - `assembly_machine/plate_saturnite`
    - `assembly_machine/plate_dura_steel`
  - 这些装配机 recipe 使用现代 pool `alt.plates`，并保留旧 `autoswitch.plates` 分组。
  - `HbmRecipeProvider` 生成压机 plate recipe：
    - `press/schrabidium_plate`
    - `press/combine_steel_plate`
    - `press/saturnite_plate`
    - `press/dura_steel_plate`
  - 同批前置的 `weaponsteel_plate`、`casing_small_weaponsteel`、`casing_large_weaponsteel` 已通过 `WeaponSteel` tag 落地。
- 迁移边界：
  - 本批只补普通材料 item/tag/recipe 落点，不迁 `ItemCustomLore` tooltip、完整矿物合成/冶炼/anvil/crucible/lemetegon 产线。
  - `CMBSteel` 在现代 tag path 中继续按现有 `LegacyOreDictionaryMappings.materialPath(...)` 规则解析为 `combine_steel`。
  - `SA326` 的 hazard/blinding 语义仍归 `item-hazard-system` 与辐射库后续补齐；本批只避免 recipe/loot/hazard 解析旧 ID 时缺少 item/tag 目标。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 抽样生成：
    - `assembly_machine/plate_combine_steel.json`：`forge:ingots/combine_steel` -> `hbm:plate_combine_steel`，`pools=["alt.plates"]`，`auto_switch_group="autoswitch.plates"`。
    - `press/dura_steel_plate.json`：`stamp: "plate"`，`forge:ingots/dura_steel` -> `hbm:plate_dura_steel`。
    - `forge:ingots/saturnite`、`forge:plates/schrabidium`、`forge:ingots/combine_steel` 均包含对应 HBM item。
  - 生成的 `src/generated/resources/data/forge/tags/items/**/*.json` 已无重复 value。
