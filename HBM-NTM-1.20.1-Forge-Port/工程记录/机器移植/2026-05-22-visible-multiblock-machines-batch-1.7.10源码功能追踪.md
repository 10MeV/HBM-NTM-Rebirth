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
