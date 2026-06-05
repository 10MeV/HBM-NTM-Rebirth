# 兼容集成层 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 对外 mod/API 兼容入口。
- 该库涉及 NEI、AE2、OpenComputers、EnergyControl、矿辞、流体、配方、microblocks 等。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/CompatHandler.java`
- `src/main/java/com/hbm/util/Compat.java`
- `src/main/java/com/hbm/util/CompatExternal.java`
- `src/main/java/com/hbm/util/CompatRecipeRegistry.java`
- `src/main/java/com/hbm/util/CompatFluidRegistry.java`
- `src/main/java/com/hbm/util/CompatEnergyControl.java`
- `src/main/java/com/hbm/handler/ae2`
- `src/main/java/com/hbm/handler/imc`
- `src/main/java/com/hbm/handler/microblocks`
- `src/main/java/com/hbm/handler/nei`

## 旧版契约

- `CompatHandler` 负责按已加载 mod 触发兼容初始化。
- `CompatRecipeRegistry` 与 `CompatFluidRegistry` 处理跨 mod 配方/流体注册。
- `CompatEnergyControl` 定义 EnergyControl 使用的 NBT key。
- AE2 兼容包括 mass storage、arc furnace large 外部存储。
- IMC handler 支持其他 mod 注入机器配方。
- NEI handler 大量存在，但现代迁移应转向 JEI/REI 类展示层。

## 迁移计划

- 兼容层不作为第一批强制迁移，先保留接口位置和旧行为记录。
- JEI 展示应基于统一 recipe 数据，不直接搬 NEI handler。
- AE2/OC 等现代版本 API 差异大，需要后续按目标版本重新查官方文档。
- EnergyControl key 若仍需兼容，保留旧 key 名。

## 验证清单

- 未安装兼容 mod 时不加载其类。
- 配方注入在服务端和客户端一致。
- JEI 展示不改变实际机器配方数据。

## 2026-06-04 JEI 可选依赖接入

- Gradle 接入 JEI 1.20.1 Forge 最新 Maven release：`15.20.0.130`。
- 依赖策略：
  - `compileOnly`：`jei-1.20.1-common-api` 与 `jei-1.20.1-forge-api`。
  - `runtimeOnly`：`jei-1.20.1-forge`，仅用于开发运行环境验证。
  - `mods.toml` 声明 `jei` 为 `mandatory=false`、`ordering="AFTER"`，玩家不安装 JEI 时 HBM 不应强制失败。
- 代码归属决策：
  - 后续 JEI 插件、recipe category、transfer handler、ingredient hiding 等联动代码放在兼容集成层下的 `com.hbm.ntm.compat.jei`。
  - JEI 展示只读取统一 HBM recipe model/runtime，例如 `GenericMachineRecipe`、`GenericMachineRecipeRuntime` 和普通 `RecipeType`，不把 JEI API 引入机器方块、方块实体、菜单或核心 recipe 数据模型。
  - 若需要 JEI 专用 DTO/渲染包装，放在 `compat.jei` 内部，核心配方库继续保持无 JEI 类引用。

## 2026-06-04 组装机/化工厂 JEI 配方说明接入

- 1.7.10 对照：
  - 旧 NEI handler 是展示层；配方事实仍来自 `AssemblyMachineRecipes` / `ChemicalPlantRecipes` 的 `GenericRecipe` 数据。
  - 现代 JEI 不直接搬旧 NEI handler，而应读取统一 recipe model。
- 本批现代接入：
  - 新增 `com.hbm.ntm.compat.jei.HbmJeiPlugin` 与 `HbmMachineRecipeCategory`。
  - 注册 Assembly Machine / Chemical Plant 两个 JEI category 和对应 catalyst。
  - JEI recipe 列表直接读取 `ModRecipes.ASSEMBLY_MACHINE` / `ModRecipes.CHEMICAL_PLANT`，并按 `GenericMachineRecipe.LEGACY_ORDER` 排序。
  - item/fluid 输入输出从 `GenericMachineRecipe` 的 display API 读取；底部 tooltip 复用 `GenericMachineRecipe#getDisplayLines()`，与机器 selector 保持同源说明。
- 迁移边界：
  - 当前 JEI 布局是通用机器配方布局，不是旧 NEI/GUI 像素级复刻。
  - 无法映射成 Forge `FluidStack` 的 HBM 流体暂不显示为 JEI fluid slot，但仍会出现在库层 display lines 中。
  - JEI 只展示已加载的现代 datapack JSON；若旧模板未导入或导入失败，JEI 不会补造配方。

## 2026-05-23 能量库兼容层推进

- `CompatEnergyControl.dischargeItem` 已按 1.7.10 外层限速语义修正：
  - 实际放电量为 `min(dischargeRate, storedCharge, requested)`。
  - 这与旧 `CompatEnergyControl#dischargeItem` 对 `IBatteryItem` 的处理一致，也避免物品底层去掉内部限速后外部兼容层无限速抽电。
- `HbmEnergyBlockEntity` 现在实现 `HbmEnergyHandler` 并委托内部 `HbmEnergyStorage`：
  - 使 EnergyControl 数据导出、核爆能量识别、通用能量查询都能识别基于该基类的机器。
  - 修正此前 `CompatEnergyControl.getEnergyData(BlockEntity, CompoundTag)` 对这类方块实体只写 `euType`、不写 `energy/capacity` 的问题。
- 外部 OpenComputers/EnergyControl mod 的正式 API 桥仍未接入；本批只迁移 HBM 侧稳定接口与无外部依赖的验证入口。

## 2026-05-23 EnergyControl 旧 helper 尾项接入

- 1.7.10 对照 `com.hbm.util.CompatEnergyControl`：
  - `getHeat(TileEntity)`：旧端只识别 RBMK base，其他反应堆/研究堆未进入该入口。
  - `getAllTanks(TileEntity)`：旧端对 `IFluidUserMK2` 返回 `{fluidName, fill, capacity}` 数组列表，并跳过 smoke/smoke_leaded/smoke_poison。
  - `findTileEntity(World,x,y,z)`：旧端通过 `CompatExternal.getCoreFromPos` 解析 dummy/multiblock core。
  - `getFluidTexture(String)`：旧端通过 `Fluids.fromName(name).getTexture()` 返回 GUI 贴图。
- 现代接入：
  - `CompatEnergyControl.getHeat(BlockEntity)` 现在解析 dummy core 后读取现代 `HeatSource`，没有热源时返回 `-1`。
  - `CompatEnergyControl.getAllTanks(BlockEntity)` 现在解析 dummy core 后读取 `HbmFluidBlockEntity#getAllTanks()`，返回 `{fluidName, fill, capacity}`，空列表返回 `null`。
  - `CompatEnergyControl.findTileEntity(...)` 现在能把 `MultiblockDummyBlockEntity` 解析到 core BlockEntity。
  - `CompatEnergyControl.getFluidTexture(String)` 现在返回现代 `FluidType#getTexture()`。
  - `BoilerBlockEntity` 实现 `HeatSource`，使流体/热量 info panel 和后续外部 EnergyControl 桥能读到 heat。
  - `/hbm energy info <pos>` 现在会通过 `findTileEntity` 解析 dummy，并在输出中附带 `heat` 与 `tanks`。
- 边界：
  - 现代端目前没有完整 RBMK base 运行时对象；热量 helper 先以通用 `HeatSource` 承载旧入口语义。
  - tank 过滤暂不硬编码旧 smoke 三种流体，因为现代 `HbmFluids` 当前未完整暴露对应类型；后续烟雾流体迁入时应按旧入口补过滤。

## 2026-06-04 新版源码差异补记

对比旧快照与新版 5714 源码：

- 旧 `GUIHandler` 抽出统一 `getGUIProvider(...)`，使 TileEntity、Block、手持 Item、Entity 的 GUI provider 查找在 server/client 两侧复用；现代兼容层如果暴露外部 GUI/控制入口，应复用同一 provider 解析语义。
- 新增 `BlastFurnaceHandler` 并从 `NEIRegistry` 移除旧 `AlloyFurnaceRecipeHandler`，说明 NEI/JEI 兼容面的 blast furnace 分类已经从旧 alloy furnace recipe surface 切换到 `BlastFurnaceRecipesNT`。
- `GenericRecipe#printNEIExtras()`、`PUREXRecipe#printNEIExtras()` 与 `BlastFurnaceRecipe#printNEIExtras()` 增加了机器配方的 NEI 额外信息面，现代外部查看器兼容不应只展示输入/输出。
