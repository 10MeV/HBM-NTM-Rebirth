# 可见多方块机器批量 1.7.10 源码功能追踪

## 范围

- 本切片迁入以下旧多方块机器的注册、放置足迹、dummy/proxy 占位、静态 OBJ 显示、物品模型、掉落、工具标签和语言：
  - `machine_chemical_plant`
  - `machine_chemical_factory`
  - `machine_refinery`
  - `machine_fluidtank`
  - `machine_pumpjack`
- 不在本切片迁入完整 GUI、配方处理、能量/流体网络订阅、容器槽位、动态动画、爆炸/维修状态或 NEI/JEI 集成。

## 1.7.10 源文件

- 方块：
  - `src/main/java/com/hbm/blocks/machine/MachineChemicalPlant.java`
  - `src/main/java/com/hbm/blocks/machine/MachineChemicalFactory.java`
  - `src/main/java/com/hbm/blocks/machine/MachineRefinery.java`
  - `src/main/java/com/hbm/blocks/machine/MachineFluidTank.java`
  - `src/main/java/com/hbm/blocks/machine/MachinePumpjack.java`
- TileEntity / 容器 / GUI：
  - `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemicalPlant.java`
  - `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemicalFactory.java`
  - `src/main/java/com/hbm/tileentity/machine/TileEntityMachineRefinery.java`
  - `src/main/java/com/hbm/tileentity/machine/TileEntityMachineFluidTank.java`
  - `src/main/java/com/hbm/tileentity/machine/oil/TileEntityMachinePumpjack.java`
  - 对应 container/gui 文件后续逻辑切片再逐项追踪。
- 渲染：
  - `src/main/java/com/hbm/render/tileentity/RenderChemicalPlant.java`
  - `src/main/java/com/hbm/render/tileentity/RenderChemicalFactory.java`
  - `src/main/java/com/hbm/render/tileentity/RenderRefinery.java`
  - `src/main/java/com/hbm/render/tileentity/RenderFluidTank.java`
  - `src/main/java/com/hbm/render/tileentity/RenderPumpjack.java`
- 共享库：
  - `src/main/java/com/hbm/blocks/machine/BlockDummyable.java`
  - `src/main/java/com/hbm/handler/MultiblockHandlerXR.java`
  - `src/main/java/com/hbm/tileentity/machine/TileEntityProxyCombo.java`
  - `src/main/java/com/hbm/lib/RefStrings.java`
  - `src/main/java/com/hbm/main/ResourceManager.java`

## 旧状态与放置合同

- 四台机器均属于旧 `BlockDummyable` 族，主方块 metadata >= 12 时创建真实 TileEntity，metadata >= 6 时创建 proxy。
- 旧放置逻辑先按点击格临时放置，再按 `getOffset()` 把 core 移到玩家朝向反方向；现代端必须走 `MultiblockBlockItem` 直接计算最终 core，避免大模型碰撞导致前几格放不下和客户端闪现。
- `machine_chemical_plant`：
  - `getDimensions() = {2,0,1,1,1,1}`，`getOffset() = 1`。
  - `fillSpace` 在 XR 盒子之外额外填 core 周围 3x3 地面环。
- `machine_chemical_factory`：
  - `getDimensions() = {2,0,2,2,2,2}`，`getOffset() = 2`。
  - `fillSpace` 额外填 5x5 地面外环与顶部两侧轨道。
- `machine_refinery`：
  - `getDimensions() = {8,0,1,1,1,1}`，`getOffset() = 1`。
  - `fillSpace` 额外填 core 周围四角 proxy。
- `machine_fluidtank`：
  - `getDimensions() = {2,0,1,1,2,2}`，`getOffset() = 1`。
  - `fillSpace` 额外填 core 周围四角 proxy。
- `machine_pumpjack`：
  - `getDimensions() = {3,0,0,0,0,6}`，`getOffset() = 0`。
  - `checkRequirement` 额外检查两段净空：`{0,0,-1,1,-2,4}` 与 `{0,0,1,-1,-1,5}`。
  - `fillSpace` 额外在 `dir.getRotation(DOWN) * 3` 处填两段 dummy/proxy：`{0,0,-1,1,1,1}` 与 `{0,0,1,-1,2,2}`，另调用四角 `makeExtra`。

## 视觉资源

- `chemical_plant.obj` / `chemical_plant.png`，旧静态 part：`Base`、`Frame`、`Slider`、`Spinner`。
- `chemical_factory.obj` / `chemical_factory.png`，旧静态 part：`Base`、`Frame`、`Fan1`、`Fan2`。
- `refinery.obj` / `refinery.png`，另有 `refinery_exploded.obj` / `refinery_exploded.png`。
- `fluidtank.obj` / `fluidtank.png`，另有 `fluidtank_exploded.obj` / `fluidtank_exploded.png` 与 `fluidtank_inner.png`。
- `pumpjack.obj` / `pumpjack.png`，旧 renderer 使用 `Base`、`Rotor`、`Head`、`Carriage`，连杆为 Tessellator 动态绘制。

## 本轮迁移实现

- 在 `ModBlocks` 注册本批机器 block，并加入机器创造栏。
- 统一使用 `LegacyVisibleMultiblockMachineBlock` 与 `LegacyMachineDefinition` 表达旧 XR dimensions、offset、额外 proxy offset 和静态渲染 part。
- 在 `ModBlockEntities` 注册 `LEGACY_VISIBLE_MACHINE`，由 `ClientModEvents` 绑定 `LegacyVisibleMachineRenderer`。
- 添加 Forge OBJ block model JSON，并通过 datagen 生成 blockstate、item model、loot table、语言和工具标签。
- 为泵机新增 `LegacyMultiblockLayout.checkOffsets()` 支持“只检查、不填充”的旧净空要求。
- 为可见多方块机器补 `BlockEntity#getRenderBoundingBox()`：
  - 普通机器按结构布局和检查 offset 推导渲染盒，额外扩 1 格。
  - 泵机按旧 `TileEntityMachinePumpjack#getRenderBoundingBox()` 覆写为 `x-7,y,z-7` 到 `x+8,y+6,z+8`。
- 修正泵机额外底座：
  - 旧 `dir.getRotation(DOWN)` 对应现代 `getCounterClockWise()`。
  - 额外底座普通 dummy 与四角 `makeExtra` 分开表达，避免整片底座被误标为 proxy。
- 修正泵机模型/碰撞反向：
  - 旧 `RenderPumpjack` 方向表为 NORTH=90、WEST=180、SOUTH=270、EAST=0。
  - 现代泵机 definition 使用 `270 - facing.toYRot()`，与旧表对齐。
- 大排查提示：
  - 以后不要默认所有可见多方块共享同一个 Y 旋转或渲染盒公式。
  - 化工厂、化工工厂、流体罐、泵机、精炼厂都应逐台对齐旧 renderer。

## 暂缓内容

- 真实 TileEntity 状态、inventory、energy、fluid tank、recipe processing、GUI/menu、server-client sync 尚未迁入。
- 化工厂/化工厂的动画只保留静态 part；旧动态 fluid、progress、fan/slider 动画后续按机器逻辑迁移。
- 精炼厂和流体储罐的 exploded/repair/inner fluid 状态后续迁移。
- 泵机旧连杆 Tessellator 绘制、`rot/prevRot` 动画、油井检测、储罐和 persistent tooltip 后续迁移。

## 验证清单

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `runData` 已生成四台机器的 blockstate、item model、loot table、pickaxe/needs_iron_tool tag 和双语 lang。
- 实机待测：四向放置足迹、proxy 点击/破坏联动、模型正面、物品栏模型、破坏粒子。
- 实机待测：视角移出主方块但仍看见模型主体时，OBJ 不应再被错误裁剪。
- 实机待测：泵机底层占位应贴合模型底座一侧，顶部 4 层主 XR 占位应覆盖模型高架范围。

## 2026-06-04 新版源码差异补记

对比旧快照与新版 5714 源码：

- 本批已记录的化工厂/化工厂/精炼厂/流体罐/泵机核心方块类未出现同名新增替换，但共享资源 `catalytic_reformer.obj/png` 有变更；后续若迁入催化重整器，应以 5714 资源为准。
- `ChemicalPlantRecipes` 新增 `chem.obsidian`、`chem.aggregate`、`chem.napalm`，`GUIMachineChemicalPlant` recipe hover 改走 `GUIElements.drawHoveringTextRecipe(...)`；化工厂逻辑批次需要同步更新 recipe surface 与 GUI 样式。
- 上游新增 `machine_blast_furnace` 是一个新的 `BlockDummyable` 可见多方块机器，但不属于本文件最初五台机器范围；已在 `2026-06-04-upstream-new-machine-blocks-5714-1.7.10源码变化追踪.md` 单独追踪。

## 2026-06-05 5714 已迁化工厂配方对齐

- 化工厂现代运行时和 JSON recipe surface 已存在，因此本轮把已迁机器可消费的新增 5714 配方补入 datagen：`chem.obsidian`、`chem.aggregate`、`chem.napalm`。
- `chem.stone` 蓝图池已改为 `.stone` 子池，和新增 obsidian/aggregate 共用 discover stone 分组。
- 本轮只补 recipe surface；`GUIMachineChemicalPlant` 新版 hover 样式差异仍归 GUI 样式批次处理，不在配方对齐中改渲染层。

## 2026-06-05 大型组装厂/化工厂运行时迁移

- `machine_assembly_factory` 与 `machine_chemical_factory` 不再挂在静态 `LEGACY_VISIBLE_MACHINE`；现代端新增专用 BlockEntity/Menu/Screen，并继续使用旧可见多方块 layout 和 OBJ 静态 renderer。
- 1.7.10 对齐事实：
  - 大组装厂 60 槽：0 电池，1-3 升级，四模块 stride=14；模块蓝图 `4+i*14`，输入 `5..16+i*14`，输出 `17+i*14`。
  - 大化工厂 32 槽：0 电池，1-3 升级，四模块 stride=7；模块蓝图 `4+i*7`，输入 `5..7+i*7`，输出 `8..10+i*7`。
  - 两者均有四个并行 recipe module，speed/power upgrade 公式沿用普通机器后在 update 前乘 2。
  - 两者均有共享 water/spent steam 冷却槽，容量 4000；模块工作时每 tick 消耗 100mB water 并产生 100mB spent steam，冷却不足则模块不推进。
  - 大化工厂每模块 3 个输入 tank 与 3 个输出 tank，容量 24000；保留旧逻辑中同类型同压力 output tank 以最多 50mB/t 回流到 input tank 的内部流体共享。
- GUI 贴图从 1.7.10 `textures/gui/processing/gui_assembly_factory.png` 与 `gui_chemical_factory.png` 复制；槽位、recipe icon、progress、电力条、recipe tank 与冷却 tank 坐标按旧 Container/GUI 表记录迁移。
- recipe selector 使用现代通用 `GenericMachineRecipeSelector`，新增 index 字段读取 API；普通机器仍使用 index 0，大型工厂用 index 0-3 对应四个模块，并按模块蓝图过滤 recipe pool。
- proxy delegate：
  - 冷却口暴露 water 输入与 spent steam 输出。
  - recipe 口暴露对应模块输入槽/tank 与所有模块输出槽；用于对齐旧 `getAccessibleSlotsFromSide` 的四模块 I/O 口行为。
- 暂缓：
  - 旧 `TragicYuri` 复杂模型分组动画尚未完全迁入；本轮保留处理状态、音效 loop、静态模型和 GUI/recipe 功能，后续按 render-library 批次补四模块臂/滑块/风扇动画。

## 2026-06-05 大型工厂 GUI/运行时勘误修复

- 1.7.10 `GUIMachineAssemblyFactory` / `GUIMachineChemicalFactory` 贴图条纹事实：
  - 大组装厂电力条使用 `u=0, v=232-p, 16xp`，进度条 `u=0,v=240,jx6`，LED 使用 `0/4,236,4x4`。
  - 大化工厂电力条使用 `u=0, v=184-p, 16xp`，进度条 `u=0,v=216,jx6`，LED 使用 `0/4,222,4x4`。
  - 现代端此前误把条纹 UV 写到贴图右边界外，导致能量/进度/LED 片段被错误采样到左下角；已按旧 GUI UV 修复。
- 1.7.10 流体条坐标事实：
  - `renderTank` 使用底边坐标；`renderTankInfo` 使用顶部坐标。
  - 大组装厂 recipe tank info 顶部为 input `y=20`、output `y=54`，冷却槽为 `y=149`；大化工厂 recipe tank info 顶部为 `y=20+j*22`，冷却槽为 `y=125`。
  - 现代大工厂 tooltip 区域此前沿用了底边坐标，已修正为旧 `renderTankInfo` 坐标。
- `ModuleMachineBase#setupTanks` 旧行为是在 recipe 没有对应 fluid slot 时直接 `resetTank()`；现代通用 runtime 和普通化工厂选择配方 helper 之前保留了非空残留 tank，可能造成换配方后运行条件异常。已改回无对应流体约束时清空 tank。
- 1.7.10 大量 fluid-only 化工配方通过 `GenericRecipe#setIcon(ModItems.canister_full/gas_full/fluid_icon, fluidId)` 显示流体图标；现代 recipe JSON 若没有显式 icon 且没有物品输出，`getIcon()` 曾 fallback 到机器 toast symbol，导致选择器出现一排化工厂图标。已在 `GenericMachineRecipe#getIcon()` 中为 fluid output fallback 生成带流体 NBT 的现代容器图标，供 selector 与 JEI 同源使用。
- 现代 GUI selector 点击只发服务端 packet，客户端 BE 的 `selectedRecipe` 会等同步包回来；slot 客户端验证期间可能仍看到 `null` recipe，导致刚选完 recipe 时装配机/化工厂输入槽拒绝放入。已在四个 selector 的 `sendSelection()` 中先本地调用 `selectRecipe(...)`，再发送 packet，由服务端继续做权威校验。

## 2026-06-05 selector/输出槽运行时对齐修复

- 1.7.10 `GUIScreenRecipeSelector#onGuiClosed()` 发送 `NBTControlPacket(index, selection, x/y/z)`；服务端 `NBTControlPacket.Handler` 只按坐标取 `IControlReceiver` 并调用 `hasPermission(player)` / `receiveControl(...)`，不要求玩家当前仍持有原机器 container。
- 现代四个 recipe selector 是普通 `Screen`，从 `AbstractContainerScreen` 切换后可能不再满足 `player.containerMenu instanceof ...Menu`；此前普通/大型装配与化工机器的 `canReceiveClientControl` 额外要求菜单类型，会导致客户端本地选择闪现但服务端拒收，机器实际 `recipe0` 仍为 `null`。
- 已把 `AssemblyMachineBlockEntity`、`ChemicalPlantBlockEntity`、`AssemblyFactoryBlockEntity`、`ChemicalFactoryBlockEntity` 的 recipe selector 控制接收条件改回旧版语义：由通用网络层校验距离/方块实体，再由机器只校验 selector tag、模块 index、蓝图池与 recipe 是否可选。
- 1.7.10 `ModuleMachineBase#canFitOutput` 直接检查 `slots[outputSlots[i]]`，不会走 `isItemValidForSlot`；现代 `GenericMachineRecipeRuntime#canFitItemOutputs` 此前用 `ItemStackHandler.insertItem(..., simulate)` 检查输出，但机器 handler 的输出槽按旧容器语义拒绝手动插入，导致所有有物品输出的化工厂/装配机配方都被判定为“输出塞不下”。已改为直接检查输出槽空位/同栈可合并，并在 `produceOutputs` 中直接写入/合并输出槽。
- GUI LED 对齐旧 `GUIMachineAssemblyMachine` / `GUIMachineChemicalPlant`：左灯工作时亮绿，否则有 recipe 亮红；右灯工作时亮绿，否则有 recipe 且电量满足 `recipe.power` 亮红。不再用客户端完整 `canProcess` 结果决定 LED。
- GUI 半透明输入物品复核：旧 GUI 只在对应输入 `Slot` 为空时渲染 `recipe.inputItem[i].extractForCyclingDisplay(20)`，随后用 GUI 纹理以 alpha 0.5 覆盖槽位；ghost 本身只是配方提示，不负责锁槽。真实 GUI 手动放入仍会锁槽：`ContainerBase#addSlots(...)` 创建 `SlotNonRetarded`，其 `isItemValid` 走机器 `isItemValidForSlot`，最终由 `ModuleMachineBase#isItemValid` 要求第 `i` 个 input item 只能进入 `inputSlots[i]`；`autoSwitchGroup` 只放宽第一个输入槽可接受同组 recipe 的第一个输入。
- 现代普通/大型装配与化工机器继续保留该语义：`SlotItemHandler#mayPlace` 调用机器 handler 的 `isItemValid`，且本轮把 `HbmInventoryMenuHelper#moveMachineStack` 的玩家背包 shift-click 迁移改成旧 `InventoryUtil.mergeItemStack` 风格，合并已有同类堆时也检查目标 slot 的 `mayPlace`，避免快捷转移绕过固定 recipe slot 校验。

## 2026-06-05 recipe icon / ghost 渲染勘误

- 1.7.10 `RenderAssemblyFactory` 在主机器矩阵内为四个模块分别渲染 recipe icon：
  - 距离检查同普通组装机，玩家距 core 中心上方小于 35 格。
  - 每个模块先 `translate(1.5 - i, 0, 0)`，再复用普通组装机的 `rotateY(90)`、`translate(0,1.0625,0)` 和方块/非方块 item 姿态分支。
  - 该图标只显示选定 recipe 的 icon，不参与槽位锁定或配方输入验证。
- 现代 `machine_assembly_factory` 已从通用 `LegacyVisibleMachineRenderer` 切到 `AssemblyFactoryRenderer`：
  - 主体模型仍调用通用 renderer，避免改变现有静态 OBJ 分件路径。
  - 额外按旧四模块局部偏移渲染已选 recipe icon。
- 普通组装机与大装配厂共用 `LegacyRecipeIconRenderer`：
  - 旧 `RenderBlocks.renderItemIn3d(...)` 对应现代 `BakedModel#isGui3d()`。
  - 旧 `RenderItem.renderInFrame` 对应现代 `ItemDisplayContext.FIXED`。
- `LegacyRecipeGhostRenderer` 已按旧 GUI 两阶段渲染：先画所有空输入槽的循环显示物品，不画数量 overlay；再用 GUI 贴图 alpha 0.5、z=300 覆盖空槽区域。该共享 helper 同时影响普通装配机、普通化工厂、大装配厂、大化工厂；它只负责视觉提示，槽位验证由 menu slot/handler 链路负责。

## 2026-06-05 本轮大型工厂 1.7.10 对齐复查

- 复核旧 `TileEntityMachineAssemblyFactory` / `TileEntityMachineChemicalFactory`：
  - 主 recipe fluid 端口只对应 `inputTanks/outputTanks`；water/lps 由 `DelegateAssemblyFactoy` / `DelegateChemicalFactoy` 仅在 `getCoolPos()` 冷却线 proxy 暴露。
  - 现代 `AssemblyFactoryBlockEntity` 与 `ChemicalFactoryBlockEntity` 已把主 `ForgeFluidHandlerAdapter` 改回只暴露 recipe tank；`getAllTanks()` 仍包含 recipe tanks + water/lps，供 GUI/准星信息显示。
  - 大工厂 `maxPower` 按旧源码改回“选中四模块 recipe.power * 100 求和，再与当前电量、1_000_000 取最大”，不再额外把默认值加进 recipe 总和。
- 复核旧 `GUIMachineAssemblyFactory` / `GUIMachineChemicalFactory`：
  - 旧背景不是整张 `xSize/ySize` 一次 blit，而是上半区 + 玩家物品栏区分段绘制：大组装 `0,0,256x140` 和 `25,140,231x100`；大化工 `0,0,248x116` 和 `18,116,230x100`。
  - 现代大组装/大化工 screen 已按上述分段绘制，避免贴图下半区域采样到错误位置导致能量条/按钮纹理错落到左下角。
- 复核旧 `RenderChemicalFactory`：
  - 大化工不应走纯静态可见机器 renderer；旧 TESR 只静态绘制 `Base/Frame`，`Fan1` 围绕局部 `x=1`、`Fan2` 围绕局部 `x=-1`，均按 `-anim * 45 % 360` 旋转。
  - 现代新增 `ChemicalFactoryRenderer`，接入 `LegacyWavefrontModel`/渲染库并在 `ClientModEvents` 注册到 `CHEMICAL_FACTORY`；`ChemicalFactoryBlockEntity` 保存/同步 `Anim/Frame`，客户端处理任一模块时推进 anim。
- 2026-06-05 续修：
  - 大组装旧 `TileEntityMachineAssemblyFactory.TragicYuri` / `AssemblerArm` 已迁入现代 `AssemblyFactoryBlockEntity` 客户端状态机；该状态机不是旧版通用动画库，而是机器自身逻辑。保留旧 `WORKING -> RETIRING -> SLIDING` 滑车状态、`REPOSITION/EXTEND/CUT/RETRACT/RETIRE/WAIT` 机械臂状态、7-15 秒随机重定位、10 tick 滑车移动、锯片每 tick +45 度以及 `assembler_start/cut/strike` 事件音效。
  - 大组装 `AssemblyFactoryRenderer` 不再走纯静态整模渲染；按旧 `RenderAssemblyFactory` 分件绘制 `Base/Frame`、`Slider1-4`、`ArmLower/ArmUpper/Head/Striker1-4`、`Blade2/4`，并保留四模块 recipe icon 的旧局部坐标。
  - 旧装配厂火花是 renderer 内手写 `Tessellator.addVertexWithUV(...)` 贴图面，不属于动画库；现代改为接入 `LegacyTexturedQuadRenderer` 与 `ObjRenderContext.withTranslucencyNoDepthWrite()`，继续使用旧 `assembly_factory_sparks` 贴图、滚动 U、两层左右 blade 火花面和每顶点 alpha 渐隐。
  - 复核旧 `TileEntityMachineChemicalPlant#getConPos()`：小化工 12 个连接点都同时订阅输入流体并提供输出流体，不存在现代 side=DOWN 只输出的拆分。现代 `ChemicalPlantBlockEntity#getCapability(FLUID_HANDLER, side)` 已改为所有 side 暴露同一个 recipe IO handler。
  - 复核旧流体订阅语义：机器只会对 recipe `setupTanks(...)` 后非 `NONE` 的输入 tank 订阅对应 fluid；现代新增 `ForgeRecipeFluidHandlerAdapter`，recipe 流体端不会向 `NONE` 空槽填入任意流体，避免无流体槽位被管道塞入后又被 recipe setup 清空造成“吞流体”。大组装、大化工和小化工的 recipe fluid capability 均接入该 adapter；冷却 water/lps delegate 保持独立固定槽逻辑。

## 2026-06-05 四台装配/化工机器继续对齐

- 复核旧 `TileEntityMachineAssemblyMachine`：
  - 小装配流体连接点与小化工一致，12 个 `getConPos()` 同时订阅能量、订阅已声明的 input fluid、提供非空 output fluid。
  - 现代 `AssemblyMachineBlockEntity` 旧自写 `AssemblyFluidHandler` 会在 input tank 仍为 `NONE` 时接受任意 Forge 流体；已改为 `ForgeRecipeFluidHandlerAdapter(List.of(inputTank), List.of(outputTank), 0, ...)`，对齐旧版只对 recipe 已声明流体开放输入。
- 复核旧 `TileEntityMachineAssemblyFactory` / `RenderAssemblyFactory`：
  - 大装配也有 `frame` 字段，客户端每 20 tick 查 `y+3` 是否非空气；renderer 只在 `frame` 为真时渲染 `Frame`。
  - 现代大装配动态 renderer 之前误把 `Frame` 常显；已补 `AssemblyFactoryBlockEntity#frame/shouldRenderFrame()` 与 NBT/update tag 同步，并让 `AssemblyFactoryRenderer` 条件绘制。
- 复核旧大型工厂 Container/GUI：
  - 大组装玩家物品栏起点为 `(33,158)`，热栏 `(33,216)`，前景 `container.inventory` 文本 x=33；现代此前写成 x=48，导致玩家背包槽横向偏移。已改回 x=33。
  - 大化工玩家物品栏起点为 `(26,134)`，热栏 `(26,192)`，前景文本 x=26；现代此前写成 x=44，已改回 x=26。
  - 大组装 selector 热区旧 `checkClick(6 + ox, 53 + oy, 18,18)`，图标仍渲染在 `(7 + ox,54 + oy)`；大化工 selector 热区旧 `checkClick(74,19 + module*22,18,18)`，图标渲染在 `(75,20 + module*22)`。现代热区已按旧边界对齐。

## 2026-06-05 recipe selector / GUI 默认图标继续勘误

- 复核旧 `GuiInfoContainer#TEMPLATE_FOLDER` 与四个机器 GUI：
  - `GUIMachineAssemblyMachine`、`GUIMachineChemicalPlant`、`GUIMachineAssemblyFactory`、`GUIMachineChemicalFactory` 均使用 `renderItem(recipe != null ? recipe.getIcon() : TEMPLATE_FOLDER, ...)`。
  - 因此机器刚放下、模块未选择配方时，配方按钮显示 `template_folder` 是 1.7.10 正常行为，不是错误机器图标。
  - 现代四台机器 screen 已把 `recipe == null` 的按钮图标改为 `ModItems.TEMPLATE_FOLDER`，不再渲染空白。
- 复核旧 `GUIScreenRecipeSelector`：
  - 搜索框文字坐标为 `(28,111)`，焦点高亮区域为 `(26,108,106x16)`，左下 `(8,108)` 会显示帮助按钮，tooltip 为斜体 `Press ENTER to toggle focus`。
  - 打开 selector 时旧版不会自动聚焦搜索框；按 Enter 切换搜索焦点，点击清除搜索后清空文本并聚焦搜索框。
  - 现代四个 selector 已对齐这些共享交互，并继续用 `GenericMachineRecipeSelector` 按旧 `recipeOrderedList/sourceOrder` 顺序过滤。
- 复核旧 recipe hover：
  - 机器按钮与 selector 悬浮都调用 `GUIElements.drawHoveringTextRecipe(recipe.print(), ...)`；`GenericRecipe#print()` 第一行是黄色本地化显示名，然后是 auto-switch、耗时、耗电、输入、输出，不额外显示 internal recipe key。
  - 现代四个机器按钮和四个 selector 已改为复用 `GenericMachineRecipe#getDisplayLines()`，不再把 `internalName`、`Power:`、`Duration:` 作为硬写调试 tooltip。
- 复核旧大组装/大化工 LED：
  - 旧 GUI 左灯：`didProcess[module]` 为真显示绿色，否则有 recipe 显示待机色。
  - 旧 GUI 右灯：`didProcess[module]` 为真显示绿色，否则有 recipe 且 `power >= recipe.power && canCool()` 显示待机色。
  - 现代此前用 `canProcessSelectedRecipe()` 决定绿色灯，会把物品/流体输入与输出槽容量也算入 GUI 指示，和旧版不一致。现代大组装/大化工 screen 已改回只用 `isProcessing(module)` 与同步的 `canCool` 状态驱动 LED。

## 2026-06-05 四台装配/化工继续源码对齐复查

- 复核旧 `TileEntityMachineAssemblyFactory#canCool()` 与 `TileEntityMachineChemicalFactory#canCool()`：
  - 旧源码只判断 `water.getFill() >= 100 && lps.getFill() <= lps.getMaxFill() - 100`，不额外检查 tank type。
  - 现代大型组装厂/大型化工厂的服务端 `canCool()` 与 menu 同步值已去掉 WATER/SPENTSTEAM 类型条件，避免 GUI 指示与旧源码处理条件不一致。
- 复核旧 `RenderAssemblyMachine` / `RenderAssemblyFactory` 的 recipe icon：
  - 旧版在手动方块/非方块姿态分支后使用 `RenderItem.renderInFrame = true` 渲染 `EntityItem`。
  - 现代共享 `LegacyRecipeIconRenderer` 已按追踪记录改用 `ItemDisplayContext.FIXED`，不再用 `GROUND`，以更接近旧 `renderInFrame` 的平面/缩放姿态；该修复同时影响普通装配机与大型组装厂。
- 复核旧 `ModuleMachineBase#setupTanks(...)`：
  - 旧版 recipe 没有对应 input/output fluid slot 时会对该 tank 调用 `resetTank()`；`FluidTank#setTankType` 类型变化也会清空 fill。
  - 因此选择无流体槽配方后清掉原 recipe tank 内容是 1.7.10 既有行为；现代通用 runtime 当前保持该语义，不应改成保留无槽流体。
- 复核旧 `ModuleMachineBase#isItemValid(...)` / `hasInput(...)` / `consumeInput(...)`：
  - 第 `i` 个 item input 只对应第 `i` 个 input slot；auto-switch 只额外允许第一个输入槽接受同组 recipe 的第一个输入。
  - 现代 `GenericMachineRecipeRuntime#isItemValidForCurrentRecipe(...)`、`matchItemInputs(...)` 与 `LegacyRecipeGhostRenderer` 已保持同一顺序锁槽语义，ghost 只是提示但提示位置与真实可放槽位一致。

## 2026-06-05 四台装配/化工快捷转移继续勘误

- 复核旧四个 Container 的 `transferStackInSlot(...)`：
  - 小装配：玩家背包 shift-click 先尝试电池槽，再蓝图槽，再升级槽，再 recipe input `4..15`。
  - 小化工：玩家背包 shift-click 先尝试电池槽，再蓝图槽，再升级槽，再 solid input `4..6`；旧代码不会自动把容器分流到流体装/卸槽。
  - 大组装/大化工：玩家背包 shift-click 先尝试电池槽，再四个模块蓝图槽，再升级槽，再四个模块 solid input 槽；recipe output 槽不参与插入。
- 现代此前用一个连续机器区间做快捷转移；由于旧版四台机器的 slot0 电池槽 `isItemValidForSlot(0)` 返回 `true`，这可能让蓝图、升级或普通材料被 shift-click 错塞进电池槽，表现为槽位锁定/放入行为不符合旧 GUI。
- 现代 `HbmInventoryMenuHelper` 新增旧式 `moveStackToAnyRange(...)` 和 `isBatteryLike(...)`，四个 Menu 的 `quickMoveStack(...)` 已按旧 Container 分流顺序改写；slot0 仍保留旧版“手动可放任意物品”的机器验证语义，但快捷转移只会把现代 HBM/Forge 能量物品送入电池槽。
- 旧 `ContainerMachineAssemblyFactory` 第四个蓝图分流分支写成 `44..45`，而真实第四模块蓝图槽是 `4 + 3 * 14 = 46`；现代按真实模块蓝图槽 `46` 分流，不复刻这个旧源码复制粘贴错误。

## 2026-06-05 四台装配/化工运行时契约继续勘误

- 复核旧 `ModuleMachineBase#hasInput(...)` / `consumeInput(...)` / `canFitOutput(...)` / `produceItem(...)`：
  - 旧版对 item input、fluid input、item output、fluid output 均使用 `Math.min(recipe数组长度, 机器槽/罐数组长度)` 截断循环；配方定义里超过机器物理槽位/罐位的条目不会让机器直接判定失败。
  - 现代 `GenericMachineRecipeRuntime` 此前在若干检查中用 `recipe.size() > slots/tanks.size()` 直接返回 false，会使部分旧 JSON 化后的装配机/化工厂配方“条件满足但不开工”。本轮已改回旧版截断契约，并让消费/产出循环使用同一截断数量。
- 复核旧 `TileEntityMachineAssemblyMachine#receiveControl(...)`、`TileEntityMachineChemicalPlant#receiveControl(...)`、`TileEntityMachineAssemblyFactory#receiveControl(...)`、`TileEntityMachineChemicalFactory#receiveControl(...)`：
  - 旧版手动选择配方只写入 `module.recipe = selection` 并 `markChanged()`，不会立即清空 `module.progress`。
  - 进度归零仍由后续 `ModuleMachineBase#update(...)` 在 recipe 不允许、条件不足或不能处理时执行；现代四台机器的手动选择 helper 已去掉主动 `progress = 0`，保持旧版时机。
- 复核旧 `ModuleMachineBase#canProcess(...)`：
  - auto-switch 只有在 `inputSlots.length > 0` 且第一个输入槽非空时才检查同组配方；切换成功后本 tick 返回 false，下一 tick 重新按新配方检查。
  - 现代通用 runtime 已补同款首输入槽守卫，避免无 item input 机器复用该 runtime 时越界；切换后仍返回未处理状态并在后续 tick 继续判断。

## 2026-06-05 小装配/小化工动画与声音勘误

- 复核旧 `TileEntityMachineAssemblyMachine` / `RenderAssemblyMachine`：
  - 旧版 `ring`、`ringTarget`、`ringSpeed`、`ringDelay` 与两条 `AssemblerArm` 只在客户端 tick 本地推进，不写入 NBT，也不随 `serialize(ByteBuf)` 同步；服务端只同步 `didProcess` 和 recipe/progress/能量/流体等运行状态。
  - 现代此前把 ring 相关字段写入 update tag，服务端本身不推进这些客户端动画值，会通过 `sendBlockUpdated` 反复覆盖客户端动画，表现为机械臂/环不转或刚动就被打回。现代已移除这些纯客户端动画字段的 NBT/update tag 同步，只由客户端按 `didProcess` 本地推进。
  - 旧 `RenderAssemblyMachine` 机械臂枢轴为 `ArmLower1 (0,1.625,0.9375)`、`ArmUpper1 (0,2.375,0.9375)`、`Head1 (0,2.375,0.4375)` 绕 X；第二条臂同坐标取负 Z 且角度取反。现代 `AssemblyMachineRenderer` 保持该层叠变换。
  - 旧小装配 loop 音为 `NTMSounds.ELECTRIC_MOTOR_LOOP = hbm:block.motor`，音量 `0.5F`、range `15F`、pitch `0.75F`、keepAlive `20`；不是 `assemblerOperate`。现代新增 `block.motor` 声音注册与资源，并让小装配通过 `LegacyMachineAudioBridge` 接入该 loop，同时保留 `assembler_start/strike/stop` 事件音。
- 复核旧 `TileEntityMachineChemicalPlant` / `RenderChemicalPlant`：
  - 旧版 `anim/prevAnim` 只在客户端 tick 本地推进，`didProcess` 为真时 `anim++`；不写入 NBT，也不随网络包同步。
  - 现代此前把 `anim` 写入 update tag，服务端 `anim=0` 会反复覆盖客户端，导致 Spinner 看起来抽搐而非连续旋转。现代已移除 `anim/prevAnim` 的服务端同步，仅同步 `didProcess`，由客户端本地推进。
  - 旧小化工 Spinner 围绕局部 `(0.5,0,0.5)` 按 `(anim * 15) % 360` 绕 Y 轴旋转，Slider 使用 `BobMathUtil.sps(anim * 0.125) * 0.375`。现代 `ChemicalPlantRenderer` 继续走 `LegacyObjTransforms.rotateAroundY(...)` 与 `softPeakSine(...)` 对齐。
  - 旧小化工 loop 音为 `NTMSounds.CHEMPLANT_LOOP = hbm:block.chemicalPlant`，音量 `1F`、range `15F`、pitch `1F`、距离 `30`；不是 `chemplantOperate`。现代小化工已改接 `ModSounds.BLOCK_CHEMICAL_PLANT` 的 loop。
