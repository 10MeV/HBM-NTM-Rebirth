# 多方块核心 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 多方块结构辅助库、dummy 方块契约和旧方向/尺寸定义。
- 该库被大型机器、泵、精炼、装配、AMS、反应堆等复用。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/MultiblockHandler.java`
- `src/main/java/com/hbm/handler/MultiblockHandlerXR.java`
- `src/main/java/com/hbm/interfaces/IMultiblock.java`
- `src/main/java/com/hbm/interfaces/IDummy.java`
- `src/main/java/com/hbm/tileentity/machine/TileEntityDummy.java`
- `src/main/java/com/hbm/blocks/machine` 中大量多方块机器

## 旧版契约

- 方向：
  - `EnumDirection.North/East/South/West`
  - metadata 映射：North=2、East=5、South=3、West=4。
- 尺寸数组顺序：
  - `{posX, negX, posY, negY, posZ, negZ}`。
  - 不同 facing 的机器有独立尺寸数组，例如 assembler、chemplant、pumpjack、radGen。
- `checkSpace`：
  - 检查主方块周围区域。
  - 允许 air 或 replaceable block。
- `fillUp`：
  - 在区域内放置 dummy block。
  - dummy tile 保存主方块坐标 `targetX/Y/Z`。
- `removeAll`：
  - 删除区域内实现 `IDummy` 的方块。
- `MultiblockHandlerXR` 是更复杂/扩展版本，应单独核对结构表和旋转算法。

## 迁移计划

- 现代端用 `Direction` 与 `BlockPos` 重写，不保留旧 int metadata。
- 旧尺寸数组应保存在结构定义中，便于机器逐个迁移时引用。
- dummy block/block entity 需要先迁移，供所有多方块机器复用。
- 结构占位、拆除、主方块坐标同步必须在服务端执行。

## 2026-05-21 迁移切片

- 新增现代核心类：
  - `src/main/java/com/hbm/ntm/multiblock/MultiblockExtents.java`
  - `src/main/java/com/hbm/ntm/multiblock/MultiblockHelper.java`
  - `src/main/java/com/hbm/ntm/multiblock/MultiblockCoreBlock.java`
  - `src/main/java/com/hbm/ntm/multiblock/DummyBlock.java`
  - `src/main/java/com/hbm/ntm/blockentity/MultiblockDummyBlockEntity.java`
- 注册 `hbm:dummy_block` 与 `hbm:multiblock_dummy` 方块实体。
- `MultiblockExtents` 保留旧 `MultiblockHandler` 数组顺序 `{posX, negX, posY, negY, posZ, negZ}`，并生成相对偏移列表。
- `MultiblockHelper` 迁入旧 `MultiblockHandler` 的尺寸常量、`checkSpace`、`fillUp`、`removeAll` 基本契约：
  - `checkSpace` 只允许 `canBeReplaced()` 的方块。
  - `fillUp` 只在服务端放置 dummy，并写入主方块 `BlockPos`。
  - `removeAll` 使用线程局部清理 guard，避免 dummy 被移除时递归破坏主方块。
- `MultiblockDummyBlockEntity` 同时保存现代 `CorePos` 和旧兼容字段 `tx/ty/tz`，便于后续存档/调试对照。
- `DummyBlock` 行为：
  - 服务端 tick 检查目标方块是否实现 `MultiblockCoreBlock`，失效则自清理。
  - 玩家非潜行右键 dummy 时转发到主方块位置。
  - 非库清理导致 dummy 被破坏时破坏主方块，保留旧 `DummyOldBase.breakBlock` 的联动语义。
  - 无创造栏展示，无掉落，使用 `block_steel` 模型作为临时可见调试外观。

## 2026-05-22 修正

- 核对旧 `BlockDummyable.onBlockPlacedBy`：`int o = -getOffset()`，core 实际放在临时点击点的 `dir * -offset` 位置；现代端新增 `MultiblockHelper.legacyCoreFromPlacement(...)` 统一表达该旧偏移契约。
- `MultiblockHelper.checkSpace(...)` 增加 `temporaryPos` 忽略位，并同时检查 core 位置本身可替换，避免 relocate 时覆盖已有方块。
- dummy 和当前 OBJ core 方块破坏粒子走 Forge `IClientBlockExtensions.addDestroyEffects`，使用旧方块贴图契约 `hbm:block_steel` 的方块粒子，避免隐形 dummy/大 OBJ shape 使用默认破坏粒子时出现异常表现。
- 新增 `LegacyMultiblockPlaceable` 与 `MultiblockBlockItem`：
  - 旧 1.7.10 是先临时放置单格、`onBlockPlacedBy` 内删掉临时格、只用 `MultiblockHandlerXR.checkSpace` 检查结构空间，再 setBlock 到最终 core。
  - 现代原版 `BlockItem` 会在临时点击格对完整 block shape 执行实体碰撞检查；大模型/大选择盒机器会因此在玩家面前前几格放不下，并产生客户端先显示临时位置、服务端再 relocate 的闪现。
  - 新物品路径直接计算最终 core，按多方块空间检查放置并调用库填充 dummy，绕开临时格的大碰撞检查和客户端错位预测。
- `MultiblockDummyBlockEntity` 增加 `Proxy` NBT 标记：
  - 普通 dummy 只承担碰撞、右键转发和破坏联动。
  - proxy dummy 额外把 `getCapability(...)` 转发到 core BlockEntity，用于迁移旧 `TileEntityProxyCombo.inventory().power().fluid()` 这类机器边缘代理。
  - `MultiblockHelper.fillUp(...)` 增加 `Predicate<BlockPos>` 版本，由具体机器按旧 `makeExtra` 逻辑选择哪些相对坐标是 proxy。
- 首个使用者为组装机：
  - core 已暴露 item、energy、fluid 三类 Forge capability scaffold。
  - proxy dummy 不区分具体 capability 类型，只负责把请求转交给 core；具体 side 规则仍由 core capability 实现决定。
- `MultiblockExtents` 增加 `ofLegacyXr(...)` / `rotateLegacyXr(...)`：
  - 旧 `MultiblockHandlerXR` 尺寸表顺序为 `{U,D,N,S,W,E}`，并以 `SOUTH` 为原始方向。
  - 现代内部仍使用 `{+X,-X,+Y,-Y,+Z,-Z}`。
  - 对照旧旋转：
    - `SOUTH`: `{U,D,N,S,W,E}` 原样。
    - `NORTH`: `{U,D,S,N,E,W}`。
    - `EAST`: `{U,D,E,W,N,S}`。
    - `WEST`: `{U,D,W,E,S,N}`。
  - 组装机已改为直接使用旧 `getDimensions() = {2,0,1,1,1,1}` 生成 extents，后续不对称 XR 机器应走该 API，避免手工换算错误。
- 新增 `LegacyXrMultiblockBlock`：
  - 面向旧 `BlockDummyable + MultiblockHandlerXR` 机器族的现代基类。
  - 统一处理 `getOffset()` 旧偏移、`MultiblockBlockItem` 直接 core 放置、按 facing 生成 XR extents、服务端 dummy 填充、proxy predicate、core 移除时清理 dummy。
  - 子类只需要提供旧 `getDimensions()`、旧 `getOffset()`、可选 proxy 坐标规则和 core 移除掉落逻辑。
  - 组装机已经改为继承该基类；`machine_battery_socket` 是 2x2 特制结构，不属于 XR 盒子族，当前继续保留自身逻辑。
- `MultiblockHelper` 增加任意 `BlockPos` offset 集合版本：
  - `checkSpace(level, corePos, offsets, temporaryPos)` 统一检查 core 和 dummy offsets，允许跳过旧式临时点击格。
  - `fillOffsets(...)` 统一设置 dummy、写入 core 坐标，并按 predicate 标记 proxy dummy。
  - `removeOffsets(...)` 统一走清理 guard，避免 core 清理 dummy 时触发递归破坏。
  - `machine_battery_socket` 已改为只提供 2x2 offset 集合，放置/清理/代理标记全部调用该库入口；后续 `fillSpace + makeExtra` 型机器可按同一路径迁入。
- 继续抽象现代结构声明层：
  - 新增 `LegacyMultiblockLayout`，把 core+dummy offsets、proxy offset 判定、额外 `makeExtra` 风格 offsets 合并成一个结构对象。
  - 新增 `LegacyOffsetMultiblockBlock`，用于非 XR 盒子的 offset 多方块，统一处理直接放置、空间检查、dummy 填充、proxy 标记、清理和 core 移除回调。
  - `LegacyXrMultiblockBlock` 内部也改为生成 `LegacyMultiblockLayout` 后再调用 offset helper，后续 XR 机器如需叠加旧 `makeExtra` 额外 proxy 位时不会分叉成第二套清理逻辑。
  - `machine_battery_socket` 迁为 `LegacyOffsetMultiblockBlock` 使用者，块类只保留旧 2x2 足迹定义、GUI/comparator/掉落逻辑。

## 暂缓内容

- `BlockDummyable` 同块 core/proxy metadata 模式未在本切片迁入；现代端当前用独立 dummy block + proxy NBT 表达。
- dummy port 已有通用 capability proxy 标记；具体能量/流体连接限制、压力/网络订阅仍需与 `energy-mk2-network`、`fluid-mk2-network` 文档和机器自身 side 规则一起迁移。
- 具体机器尚未改为实现 `MultiblockCoreBlock` 或调用 `MultiblockHelper`，需要在机器迁移切片逐个接入。

## 验证清单

- 四向放置时 dummy 区域与旧版一致。
- 拆除主方块会清理 dummy。
- 点击 dummy 能转发到主方块。
- 存档重载后 dummy 仍能找到主方块。

## 2026-05-22 可见 OBJ 多方块机器批量接入

触发来源：

- 组装机验证了 `LegacyXrMultiblockBlock` + `MultiblockBlockItem` 的旧偏移放置路径后，继续迁入同属旧 `BlockDummyable + MultiblockHandlerXR` 族的大型机器。
- 目标是先统一“注册、旧 XR 足迹、dummy/proxy 填充、OBJ 可见渲染、物品模型/掉落/标签”这一层，不提前迁完整容器、配方、动态动画和流体/能量网络行为。

本轮新增公共层：

- `LegacyMachineDefinition`：记录旧 XR dimensions、旧 offset、`LegacyMultiblockLayout` 工厂、OBJ/贴图路径、静态 part 列表和朝向旋转偏移。
- `LegacyVisibleMultiblockMachineBlock`：在 `LegacyXrMultiblockBlock` 上接入 `EntityBlock`，保持旧多方块放置/清理语义，并以 block entity renderer 显示 OBJ。
- `LegacyVisibleMachineBlockEntity`：当前仅承载 BER，后续具体机器逻辑仍应迁入各自真实 block entity。

本轮机器足迹：

- `machine_chemical_plant`：
  - 旧 `getDimensions() = {2,0,1,1,1,1}`，`getOffset() = 1`。
  - 额外 `makeExtra` 区域按 core 周围 3x3 地面环迁入 proxy offset。
- `machine_chemical_factory`：
  - 旧 `getDimensions() = {2,0,2,2,2,2}`，`getOffset() = 2`。
  - 额外迁入 5x5 地面外环与顶部两侧轨道 proxy。
- `machine_refinery`：
  - 旧 `getDimensions() = {8,0,1,1,1,1}`，`getOffset() = 1`。
  - 额外迁入 core 周围四角 proxy。
- `machine_fluidtank`：
  - 旧 `getDimensions() = {2,0,1,1,2,2}`，`getOffset() = 1`。
  - 额外迁入 core 周围四角 proxy。
- `machine_pumpjack`：
  - 旧 `getDimensions() = {3,0,0,0,0,6}`，`getOffset() = 0`。
  - 旧 `checkRequirement` 存在两段额外净空检查，且维度数组允许负值；现代端新增 `LegacyMultiblockLayout.checkOffsets()` 表达“只检查、不填充”的空间合同。
  - 旧 `fillSpace` 在侧向偏移 3 格处额外填两段 dummy/proxy，现代端按 `MultiblockHandlerXR.rotate + for` 循环语义生成 offset，避免把负值尺寸误转成普通 extents。

边界说明：

- `LegacyMachineDefinition` 只保留 common-safe 数据，不直接引用 client-only OBJ 模型类；模型创建由 `LegacyVisibleMachineRenderer` 在客户端完成，避免专用服务端加载渲染类。
- `machine_pumpjack` 本轮只迁静态可见和多方块足迹；旧油井逻辑、储罐、能量、GUI、tooltip persistent NBT 和动画状态仍需机器逻辑切片继续迁入。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 Pumpjack XR 额外结构修正

触发来源：

- 实机观察到泵机底层 dummy/占位区域相对模型偏移，顶部结构也表现为与模型范围不吻合。
- 复核 1.7.10 `MachinePumpjack#fillSpace`：
  - 主 XR 区域：`{3,0,0,0,0,6}`。
  - 额外区域原点：`dir.getRotation(ForgeDirection.DOWN) * 3`。
  - 两段额外 `fillSpace` 只生成普通 dummy。
  - 只有额外区域四角调用 `makeExtra(...)`，因此只有四角是 proxy/extra。

本轮现代修正：

- 对照旧 ForgeDirection 旋转矩阵：水平 `dir.getRotation(DOWN)` 对应现代 `Direction#getCounterClockWise()`，此前误用了 `getClockWise()`，导致额外底座镜像到模型另一侧。
- `pumpjackDefinition()` 的额外区域 origin 改为 `facing.getCounterClockWise() * 3`。
- `pumpjackFilledOffsets(...)` 仍生成旧两段额外 dummy，但 proxy predicate 改为只标记旧 `makeExtra` 的四个角，不再把整片额外底座都标成 proxy。
- OBJ 资源复核：`pumpjack.obj` 仅包含 `Base` / `Rotor` / `Carriage` / `Head` 四个 group，旧 `RenderPumpjack` 也只渲染这四组；“缺一层”不是漏 OBJ part，而是占位/朝向对齐问题。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `runData` 已生成四台机器的 blockstate、item model、loot table、pickaxe/iron tool tag 和双语 lang。

## 2026-05-22 多方块渲染剔除盒修正

触发来源：

- 现代 `BlockEntityRenderer.shouldRenderOffScreen()` 只能放宽“离屏/距离”判断，视锥裁剪仍依赖 `BlockEntity#getRenderBoundingBox()`。
- 新增可见多方块机器此前没有覆写 BE 渲染 AABB，实际仍按主方块附近裁剪；当视角移动到看不见主方块的位置时，完整多方块 OBJ 会被错误剔除。
- 1.7.10 大量大型 TileEntity 都覆写 `getRenderBoundingBox()`；例如组装机使用 `x-1,y,z-1` 到 `x+2,y+3,z+2`，泵机使用 `x-7,y,z-7` 到 `x+8,y+6,z+8`。

本轮现代修正：

- `LegacyMultiblockLayout` 新增 `renderBoundingBox(corePos, padding)`：
  - 默认从 `checkOffsets()` 推导渲染 AABB，覆盖 core、dummy offsets 和只检查净空区域。
  - 默认 padding 为 1 block，用于容纳 OBJ 微量越界、动画部件和旧 renderer 中不严格贴合方块格的模型。
- `LegacyMachineDefinition` 新增可选 `renderBoundingBox(...)` 覆写：
  - 普通机器走结构推导。
  - 旧代码有明显更大动态活动范围时可按 1.7.10 精确覆写。
- `LegacyVisibleMachineBlockEntity#getRenderBoundingBox()` 通过 block definition 返回渲染范围，修复主方块不可见时整机消失的问题。
- `machine_pumpjack` 已按旧 `TileEntityMachinePumpjack#getRenderBoundingBox()` 覆写为 `core +/- 7/8`、高度 6 的保守范围。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
