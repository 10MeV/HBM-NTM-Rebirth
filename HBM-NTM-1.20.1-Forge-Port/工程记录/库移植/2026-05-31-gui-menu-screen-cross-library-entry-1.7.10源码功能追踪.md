# GUI/Menu/Screen 跨库总入口 1.7.10 源码功能追踪

## 范围

- 本文只记录 HBM 1.7.10 GUI、Container、按钮包、槽位、同步数据、HUD/overlay 与 JEI/recipe selector 在现代 1.20.1 port 中的跨库归属规则。
- 本文是总入口与边界文档，不做独立 GUI 库逻辑，不创建平行的 GUI framework，不替代各功能库自己的实现。
- 后续迁移具体机器、物品容器或 HUD 时，应先查本文确认行为归属，再回到对应库文档和 1.7.10 源码补实现。

## 1.7.10 源文件入口

- `src/main/java/com/hbm/inventory/container`
- `src/main/java/com/hbm/inventory/gui`
- `src/main/java/com/hbm/packet`
- `src/main/java/com/hbm/render/util/RenderScreenOverlay.java`
- 各机器、物品、武器和特殊系统自己的 `Container*`、`GUI*`、按钮包、同步包与 HUD 调用点。

## 归属规则

- `network-packet-library`：
  - GUI 按钮包、C2S 校验、S2C 同步、旧 `AuxButtonPacket` / `NBTControlPacket` / tile sync 的统一安全规则。
  - 只负责传输、校验与协议兼容，不写具体机器业务。
- `machine-module-recipe-runtime`：
  - 机器 Menu 所需的运行时数据：progress、power、recipe internal name、recipe selector、item/fluid 输入输出槽。
  - 只负责机器数据模型和选择语义，不负责屏幕绘制。
- `energy-mk2-network`：
  - 能量条、红石模式按钮、优先级按钮、电池槽 helper、能量机器通用 DataSlot。
  - 旧能量机器的 GUI 能量语义优先走该库。
- `fluid-mk2-network`：
  - 流体槽、tank 同步、流体条、流体容器装卸 slot helper。
  - 旧流体机器的 GUI 流体语义优先走该库。
- `render-library`：
  - GUI 贴图、HUD/overlay、屏幕内 OBJ/item 特殊渲染、旧纹理区域和视觉资源来源。
  - 不负责服务端按钮逻辑和机器状态变更。
- `recipes-common-loader`：
  - JEI/recipe selector 读取统一 recipe data。
  - GUI 不直接解析 datapack JSON，不直接搬旧 NEI handler。
- `util-inventory-itemstack`：
  - slot、shift-click、旧 NBT `items/slot`、容器物品库存 helper。
  - 具体 Menu 复用该库的库存和 NBT 语义，不重复手写旧容器格式。
- 具体机器/物品库：
  - 保存自身旧 `Container*` / `GUI*` 的 slot 坐标、按钮坐标、tooltip 文案、纹理路径、打开条件和业务按钮语义。
  - 只把跨机器可复用的部分下沉到对应功能库，不把所有 GUI 逻辑集中到本文。

## 非目标

- 不建立独立 `gui` 实现库。
- 不把所有 `Menu` / `Screen` 迁移到一个统一基类。
- 不用现代近似界面替代 1.7.10 旧 GUI 坐标、贴图和按钮语义。
- 不让 GUI 层直接绕过能量、流体、配方、网络或库存库修改业务状态。

## 迁移规则

- 迁移一个旧 GUI 前，先在对应机器/物品 trace 文档记录：
  - 旧 `Container*`、`GUI*`、按钮包、同步包、纹理路径、slot 坐标、按钮坐标、tooltip、progress/energy/fluid 区域。
  - 它依赖的库：网络、机器 runtime、能量、流体、配方、库存、渲染。
- 若某个 GUI 行为属于已有库的职责，先补该库，再接具体 GUI。
- 若行为只属于单台机器或单个物品，保留在该机器/物品迁移中，不新增通用抽象。
- 每个 C2S 按钮包必须在服务端校验玩家、距离、打开的菜单或目标方块实体类型。
- 每个客户端显示数据必须有明确来源：Menu/DataSlot、自定义同步包、客户端只读 cache 或 recipe manager。

## 验证清单

- 打开 GUI 不在客户端直接改世界或机器状态。
- slot 坐标、shift-click、按钮坐标、能量/流体/progress 条与旧版 trace 对齐。
- 按钮包在错误玩家、错误距离、错误方块实体或未打开菜单时不会生效。
- JEI/recipe selector 与机器 runtime 读取同一套配方数据。
- GUI 贴图和 HUD/overlay 资源来自 1.7.10 源码资源，缺失时记录缺口，不生成替代资源。

## 2026-06-01 四个 GUI 依赖库小缺口补齐与化工厂闭环

- 本批不建立独立 GUI 实现库，按本文归属规则补四个已有库：
  - `util-inventory-itemstack`：新增 `HbmInventoryMenuHelper` 与 `HbmMenuDataSlots`，集中 output-only slot、玩家背包布局、机器菜单 shift-click、旧 `items`/`slot` NBT 读写和长整型/progress DataSlot 规则。
  - `network-packet-library`：新增 `HbmGuiControlSecurity`，`TileControlPacket`、`LegacyButtonPacket`、`ServerTileActionPacket` 统一校验 sender、距离、chunk、目标方块实体；具体菜单/业务校验仍由 receiver 执行。
  - `fluid-mk2-network`：新增 `HbmFluidGuiHelper`，集中 tank fill/capacity DataSlot、流体条缩放和 tooltip 数据。
  - `machine-module-recipe-runtime`：新增 `GenericMachineRecipeSelector`，集中 `index=0`、`selection`、`null`、internal name、runtime recipe manager 校验约定。
- 化工厂闭环验证：
  - `ChemicalPlantMenu` 改为复用库存/流体/DataSlot helper，slot 坐标和旧贴图语义不变。
  - `ChemicalPlantBlockEntity#canReceiveClientControl` 现在要求玩家正在打开 `ChemicalPlantMenu`，并通过 selector helper 校验 recipe internal name。
  - `ChemicalPlantRecipeSelectorScreen` 不直接构造 packet 类，改走 `ModMessages.sendTileControl(...)`，选择器只读取 runtime recipe manager。
- 同步接入装配机，避免化工厂成为单点特殊写法。

## 2026-06-03 化工厂/装配机标题溢出与 1.7.10 对齐勘误

- 核对 1.7.10：
  - `GUIMachineChemicalPlant` 使用 `textures/gui/processing/gui_chemplant.png`，`xSize=176`，`ySize=256`，标题本地坐标为 `x=70 - width/2, y=6`，玩家物品栏文字为 `x=8, y=ySize - 96 + 2`。
  - `ContainerMachineChemicalPlant` 槽位为 battery `152,81`、blueprint `35,126`、upgrade `152,108/126`、固体输入 `8/26/44,99`、固体输出 `80/98/116,99`、流体输入/返回 `8/26/44,54/72`、流体输出/返回 `80/98/116,54/72`、玩家背包 `8,174`。
  - `GUIMachineAssemblyMachine` 使用 `textures/gui/processing/gui_assembler.png`，`xSize=176`，`ySize=256`，标题与玩家物品栏文字坐标同上。
  - `ContainerMachineAssemblyMachine` 槽位为 battery `152,81`、blueprint `35,126`、upgrade `152,108/126`、输入矩阵 `8,18` 起 4 行 x 3 列、输出 `98,45`、玩家背包 `8,174`。
- 勘误：
  - 截图中上方面板与下方白色玩家物品栏分离不是纹理错位；1.7.10 原 PNG 本身就是 256x256，现代资源与旧资源 hash 一致。
  - 实际破坏 UI 位置感的是 container 标题语言键未进入 `src/main/resources`，长 key 按旧版短标题居中公式绘制后溢出到机器面板。
- 对齐修复：
  - `container.machineAssemblyMachine`、`container.machineChemicalPlant`、`container.machineLiquefactor` 同步写入源码资源和 datagen。
  - 这些旧式居中标题保留 1.7.10 的中心点 `x=70,y=6`，但增加最大宽度保护，防止资源目录未刷新或翻译缺失时长 key 覆盖 GUI。
  - 显示名源头使用 `translatableWithFallback`，缺失语言资源时回退到 1.7.10 英文短名。

## 2026-06-03 GUI 槽位方向与旧流体贴图渲染勘误

- 核对 1.7.10：
  - `ContainerBase#addSlots(inv, from, x, y, rows, cols, slotSize)` 按 `row < rows`、`col < cols` 展开，槽号为 `from + col + row * cols`，坐标为 `x + col * slotSize, y + row * slotSize`。
  - `ContainerMachineAssemblyMachine` 的输入为 `addSlots(assembler, 4, 8, 18, 4, 3)`，因此是 4 行 x 3 列，不是 3 行 x 4 列。
  - `FluidTank#renderTank(x, y, z, width, height)` 的 `y` 参数是旧系统约定的底边；纵向槽先 `y -= height` 后自底向上绘制。`orientation=1` 用于装配机横向槽，自左向右填充但仍以 `y` 为底边。
  - 旧 `renderTankInfo` 使用的是实际绘制区域：化工厂 `8/26/44,18,16x34` 与 `80/98/116,18,16x34`，装配机 `8,99,52x16` 与 `80,99,52x16`，流体罐 `71,17,34x52`，液化机 `71,36,16x52`。
  - 旧流体槽绑定 `FluidType#getTexture()`，路径为 `textures/gui/fluids/<fluid>.png`，默认 GUI tint 为白色；颜色值主要不是普通 GUI fluid icon 的替代材质。
- 勘误：
  - 现代装配机曾把 4x3 输入矩阵写成 3x4，导致本该竖向更高的输入槽变成横向铺开。
  - 化工厂、装配机、流体罐、液化机曾用纯色矩形代替旧 `FluidTank#renderTank`，并且把旧底边 `y` 当作顶部坐标，导致流体条下移。
- 对齐修复：
  - 新增旧式 GUI 流体渲染 helper，按 16x16 旧流体贴图平铺，纵向底部锚定、横向左侧填充。
  - 流体 tooltip 区域改回旧 `renderTankInfo` 坐标，避免把底边坐标误当顶部导致悬停区域下移。
  - 复制 1.7.10 `assets/hbm/textures/gui/fluids/*.png` 到现代资源树，恢复 `FluidType#getTexture()` 所需路径。
  - 现代 `FluidType` 的 GUI tint 改回旧默认白色，避免对已着色旧流体图标二次染色。
  - 装配机输入槽改回 4 行 x 3 列；装配机和化工厂升级槽改回旧 `2 行 x 1 列` 竖排。
  - 化工厂固体输入/输出按旧 `1 行 x 3 列` 改回横排，流体容器槽同样保持横排。

## 2026-06-04 Recipe selector 与 JEI 说明同源

- 1.7.10 对照：
  - 旧 recipe selector 顺序来自 `GenericRecipes.recipeOrderedList`。
  - 旧配方说明来源于 `GenericRecipe#print()` 风格的库层 recipe 数据，不是每台 GUI 单独硬写。
- 本批现代接入：
  - `AssemblyRecipeSelectorScreen` 与 `ChemicalPlantRecipeSelectorScreen` 的 hover 说明改为直接渲染 `GenericMachineRecipe#getDisplayLines()`。
  - 选择页的 recipe 列表继续通过 `GenericMachineRecipeRuntime.recipes(...)` 获取，因此自动继承 `source_order` 排序。
  - JEI category 使用同一个 `getDisplayLines()`，保证选择页和 JEI 对 duration、consumption、输入、输出、pool/auto-switch 的描述同源。
- 迁移边界：
  - GUI 层不解析 legacy template，也不扫描 datapack JSON；模板导入、缺失映射和 skipped 诊断归 `recipes-common-loader`。

## 2026-06-04 新版源码差异补记

对比旧快照与新版 5714 源码：

- 新增 `GUIElements` 作为 1.7.10 GUI helper，包含平滑 gauge、recipe hover tooltip、fluid-colored tooltip 等绘制入口。
- `GUIMachineAssemblyMachine`、`GUIMachineChemicalPlant`、`GUIScreenRecipeSelector` 的配方 hover 从原版 `func_146283_a` 改为 `GUIElements.drawHoveringTextRecipe(...)`，现代 GUI/JEI 同源说明应保留 recipe tooltip 的橙黄边框语义。
- `FluidTank#renderInfo` 改为 `GUIElements.drawHoveringTextFluid(...)`，现代 fluid tooltip 不应只复用普通 item tooltip 样式。
- 新增 `GUIBlastFurnace` 与 `GUIScreenRadioAUTOCAL`，后续机器 GUI 迁移需要单独记录 blast furnace 燃料/流体/进度面板，以及 AUTOCAL 文件上传/文档按钮和 NBT control 包交互。
