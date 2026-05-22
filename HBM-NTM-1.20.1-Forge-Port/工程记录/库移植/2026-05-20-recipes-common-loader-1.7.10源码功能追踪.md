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
