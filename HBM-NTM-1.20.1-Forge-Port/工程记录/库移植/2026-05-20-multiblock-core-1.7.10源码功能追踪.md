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

### 2026-05-24 多方块 item fit 默认值回调

- 多方块库的库存图标已经改由 OBJ bounds 计算中心与缩放，但上一轮公共默认 `itemFitSize = 0.58` 对大多数 1.7.10 机器偏保守。
- 按旧 `ItemRenderBase` + 各机器 `renderInventory()` 的放大习惯，公共默认目标回调到 `0.72`，让模型占据略小于一个格子的视觉空间。
- 仍保留机器定义层 `.itemFitSize(...)` 作为单台校正入口，避免以后为少数极宽/极高机器再改公共 renderer。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 多方块 item 缩放改为逐机器旧值

- 统一 `itemFitSize` 仍会让部分机器偏大/偏小；1.7.10 的事实来源是每台机器自己的 `renderInventory()`，不是全局 fit。
- `LegacyMachineDefinition` 新增 `.legacyItemScale(...)`：
  - 单参数记录旧端库存最终缩放。
  - 双参数记录 `renderInventory()` 缩放与 `renderCommon()` 静态缩放，构建时相乘。
- 当前已为可见多方块库中已接入的机器补齐旧缩放值；现代 item renderer 使用 OBJ bounds 与旧缩放值换算库存占位，而结构/碰撞/剔除仍走原多方块布局。
- 默认 `itemFitSize` 回到 `0.58`，仅作为未来未核完旧 renderer 的保守兜底；已核对机器优先使用 `.legacyItemScale(...)`。

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
- 继续复核发现泵机模型与碰撞相反的直接原因在渲染旋转：
  - 旧 `RenderPumpjack` 的 metadata 表等价于 `270 - facing.toYRot()`。
  - 现代通用可见机器 renderer 原先使用 `yRotationOffset + facing.toYRot()`，会把泵机 NORTH/SOUTH 翻反。
  - 已为 `LegacyMachineDefinition` 增加单机 `yRotation(...)`，泵机使用旧表等价公式。

## 2026-05-23 多方块库大排查结论

触发来源：

- 用户要求对现有多方块库进行一次大排查，目标是对齐 1.7.10，而不是仅仅“看起来像”旧版。
- 本轮重点复核了：
  - 放置 core / dummy / extra 的偏移
  - proxy 与普通 dummy 的划分
  - 渲染 AABB
  - 泵机和其他可见多方块的旧 renderer 朝向表

本轮结论：

- 结构层整体方向是对的，但不能用一个统一的 `yRotationOffset + facing.toYRot()` 覆盖所有机器。
- 旧 1.7.10 的 `BlockDummyable` 机器经常在 renderer 里单独写 metadata->角度表，尤其是泵机、流体罐、化工机、精炼类。
- 现代端需要把这些差异保留在 `LegacyMachineDefinition` 级别，而不是强行收敛到一个共享旋转公式。
- 已保留的库能力：
  - `checkOffsets()` 表达“只检查、不填充”的旧净空
  - `renderBoundingBox(...)` 表达单机渲染范围
  - `yRotation(...)` 表达单机旧 renderer 旋转表

后续建议：

- 新迁入的可见多方块都先查旧 renderer 的 metadata switch，再决定是否走共享 `yRotationOffset`。
- 如果 1.7.10 原版 renderer 有独立 AABB，就优先按旧 AABB 对齐，不要只靠 layout 推导。

## 2026-05-23 详细碰撞盒契约补记

触发来源：

- 再次核对旧 `BlockDummyable` 后确认：它除了 `checkSpace` / `fillSpace` / `makeExtra` 之外，还允许机器在 `bounding` 列表里声明更细的碰撞与射线盒。
- 这层能力在 1.7.10 里直接影响 `addCollisionBoxesToList`、`collisionRayTrace`、`setBlockBoundsBasedOnState` 和 `drawHighlight`，不应和渲染 AABB 混为一谈。

本轮补进现代库：

- `LegacyMachineDefinition` 新增碰撞形状工厂入口，后续可按单机定义更细的 `VoxelShape`。
- `LegacyVisibleMultiblockMachineBlock` 现在会优先使用定义好的碰撞形状；没有显式配置时仍保持保守方块形状，避免一次性改坏现有机器的手感。

当前判断：

- 这次补的是“库能力”，不是某一台机器的最终碰撞迁移。
- 后续若某台 1.7.10 机器在 `bounding` 上有独立几何，就应把那组 AABB 单独迁成现代 `VoxelShape`，而不是继续复用默认方块盒。

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

## 2026-05-23 可见多方块详细碰撞与第二批机器

触发来源：

- 用户要求继续推进多方块库移植，并允许一次迁更多。
- 复核旧 `BlockDummyable` 发现详细 `bounding` 不只用于 core：dummy 被命中时会先 `findCore`，再用 core 的朝向和整机 AABB 列表参与碰撞、射线命中和高亮。

本轮补齐的库能力：

- `DummyBlock` 在目标 core 是 `LegacyVisibleMultiblockMachineBlock` 且机器声明了详细碰撞盒时，会把 core 的 `VoxelShape` 平移到当前 dummy 局部坐标系。
- 这对齐旧 `BlockDummyable.addCollisionBoxesToList(...)` / `collisionRayTrace(...)` 的“dummy 回溯 core 后按整机盒计算”契约，避免 dummy 仍然只是满方块碰撞。

本轮按 1.7.10 原码接入的资源齐全机器：

- `machine_centrifuge`：
  - 旧类：`MachineCentrifuge`、`TileEntityMachineCentrifuge`、`RenderCentrifuge`。
  - 旧尺寸：`getDimensions() = {3,0,0,0,0,0}`，`getOffset() = 0`。
  - 旧 proxy：上方 dummy 使用 `TileEntityProxyCombo(false, true, true)`，现代端暂按 `offset.y > 0` 标记 proxy。
  - 旧详细碰撞：底部 1x1x1，加上 0.75x3x0.75 的竖筒。
  - 旧渲染旋转表：SOUTH=90、WEST=180、NORTH=270、EAST=0。
- `machine_ore_slopper`：
  - 旧类：`MachineOreSlopper`、`TileEntityMachineOreSlopper`、`RenderOreSlopper`。
  - 旧尺寸：`getDimensions() = {3,0,3,3,1,1}`，`getOffset() = 3`。
  - 旧 `makeExtra`：沿 facing 正负 3、左右 1，以及 facing 正负 2 + 左右 1 的八个 proxy 位。
  - 旧详细碰撞：底座、翻斗、破碎机和出口共 8 个 AABB 已迁入。
  - 旧渲染旋转表：NORTH=180、EAST=270、SOUTH=0、WEST=90。
- `machine_gasflare`：
  - 旧类：`MachineGasFlare`、`TileEntityMachineGasFlare`、`RenderGasFlare`。
  - 旧尺寸：`getDimensions() = {11,0,1,1,1,1}`，`getOffset() = 1`。
  - 旧 `makeExtra`：地面十字四个 proxy 位。
  - 旧详细碰撞：底座、主烟囱、顶部平台、顶段烟囱共 4 个 AABB 已迁入。
  - 现代端使用现有 `flare_stack.obj/png` 资源；倾斜/燃烧动态效果留给后续真实 BlockEntity 逻辑切片。

暂缓内容：

- `machine_sawmill` 旧代码已经确认有独立 `bounding`、proxy 和动态锯片，但现代端当前没有对应 OBJ/贴图资源；不在本轮硬接空模型。
- 三台机器本轮仍是可见多方块 scaffold：GUI、库存、能量/流体规则、动画状态、配方运行和声音粒子留给机器逻辑切片。

## 2026-05-23 炼油后段可见多方块批次

触发来源：

- 用户要求继续推进多方块库移植，并允许一次推进更多。
- 本轮选择 1.7.10 中同属炼油/裂化后段、且现代端 OBJ/贴图资源已经齐全的四台 `BlockDummyable` 静态大机器，继续验证通用可见多方块库是否能承载更多旧 XR 结构。

1.7.10 事实来源：

- 注册：`com.hbm.blocks.ModBlocks`
  - `machine_vacuum_distill = new MachineVacuumDistill(...)`
  - `machine_fraction_tower = new MachineFractionTower(...)`
  - `machine_hydrotreater = new MachineHydrotreater(...)`
  - `machine_coker = new MachineCoker(...)`
- 方块：
  - `com.hbm.blocks.machine.MachineVacuumDistill`
  - `com.hbm.blocks.machine.MachineFractionTower`
  - `com.hbm.blocks.machine.MachineHydrotreater`
  - `com.hbm.blocks.machine.MachineCoker`
- 渲染：
  - `RenderVacuumDistill`：`x + 0.5, y, z + 0.5`，不按 metadata 旋转，平滑着色。
  - `RenderFractionTower`：`x + 0.5, y, z + 0.5`，不按 metadata 旋转，禁用背面剔除。
  - `RenderHydrotreater`：`x + 0.5, y, z + 0.5`，不按 metadata 旋转，平滑着色。
  - `RenderCoker`：`x + 0.5, y, z + 0.5`，不按 metadata 旋转，禁用背面剔除并平滑着色。
- 旧资源：
  - `models/block/machines/vacuum_distill.obj` + `textures/block/machines/vacuum_distill.png`
  - `models/block/machines/fraction_tower.obj` + `textures/block/machines/fraction_tower.png`
  - `models/block/machines/hydrotreater.obj` + `textures/block/machines/hydrotreater.png`
  - `models/block/machines/coker.obj` + `textures/block/machines/coker.png`

旧结构契约：

- `machine_vacuum_distill`
  - `getDimensions() = {8,0,1,1,1,1}`，`getOffset() = 1`。
  - core metadata >= 12；extra/proxy metadata >= 6，旧 `TileEntityProxyCombo().fluid().power()`。
  - `fillSpace` 先填主 XR，再在 core 平面四角 `makeExtra`。
- `machine_fraction_tower`
  - `getDimensions() = {2,0,1,1,1,1}`，`getOffset() = 1`。
  - core metadata >= 12；extra/proxy 使用流体 proxy。
  - `fillSpace` 先填主 XR，再以 core 为中心把东西南北四格 `makeExtra`。
  - 手持 `IItemFluidIdentifier` 右击可切换底部分馏塔流体类型，并有 `ILookOverlay` 显示油品；本轮暂不迁 GUI/流体交互。
- `machine_hydrotreater`
  - `getDimensions() = {6,0,1,1,1,1}`，`getOffset() = 1`。
  - core metadata >= 12；extra/proxy 为 fluid + power。
  - `fillSpace` 与真空炼油厂相同，四角 `makeExtra`。
  - 物品 persistent NBT 会显示四个流体槽；本轮暂不迁 tooltip/persistent 内容。
- `machine_coker`
  - `getDimensions() = {22,0,1,1,1,1}`，`getOffset() = 1`。
  - core metadata >= 12；extra/proxy 为 inventory + fluid。
  - `checkRequirement/fillSpace` 除主 XR 外，还以 core 为原点填固定 NORTH 朝向的额外塔体：
    - `x,y+1,z` 的 `{5,0,2,2,2,2}`。
    - `x±2,y+1,z±2` 的四根 `{0,1,0,0,0,0}` 竖向占位。
  - 最后只把 core 平面四角 `makeExtra`，额外塔体本身是普通 dummy。

本轮现代迁移：

- 将四台机器接入 `LegacyVisibleMultiblockMachineBlock` / `LegacyMachineDefinition`。
- 四台都显式设置 `yRotation(facing -> 0.0F)`，对齐旧 renderer 的“无 metadata 旋转”行为，避免默认可见机器旋转偏移。
- 真空炼油厂、加氢装置使用 `cornerProxyOffsets(...)` 表达旧四角 `makeExtra`。
- 分馏塔使用 `crossProxyOffsets()` 表达旧四向 `makeExtra`。
- 焦化装置额外迁入旧 `MultiblockHandlerXR.checkSpace/fillSpace` 的固定 NORTH 塔体占位；只把旧 `makeExtra` 的四角标记为 proxy。
- `LEGACY_VISIBLE_MACHINE` BlockEntityType 同步加入四台新机器，确保 BER/渲染剔除盒生效。
- 补齐 block model JSON、DataGen blockstate/item model、loot/tag 和双语名称。
- 顺手补正第二批可见机器的 BlockEntityType 覆盖：`machine_centrifuge`、`machine_ore_slopper`、`machine_gasflare` 也加入 `LEGACY_VISIBLE_MACHINE`，避免只有方块没有 BER 的注册缺口。
- `offsetsForLegacyXrBox(...)` 增加“只跳过 core，不跳过额外盒原点”的模式，用于焦化装置这种旧 `fillSpace` 原点不是 core 的结构；否则 `{0,1,0,0,0,0}` 小柱会少掉旧版会放置的原点格。

暂缓内容：

- 四台仍是可见多方块 scaffold：真实 GUI、库存、能量/流体网络、recipe tick、声音/粒子、NEI/JEI 分类、persistent tooltip、分馏塔流体识别工具交互后续按机器逻辑切片迁入。
- 旧 renderer 的 `GL_SMOOTH`、局部禁用背面剔除还未在 `LegacyWavefrontModel` 中分机器表达；若出现模型背面缺面或法线观感问题，应在渲染库补 per-machine render state，而不是在单机方块里绕过。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `.\gradlew.bat runData --no-daemon --rerun-tasks` 通过。
- `runData` 已生成/刷新四台机器的 blockstate、item model、loot table、pickaxe/needs_iron_tool tag 和 en_us/zh_cn lang。

## 2026-05-24 催化裂化与催化重整批次

触发来源：

- 用户要求继续推进多方块库移植，可以一次推进更多。
- 本轮选择旧版已经有完整 OBJ/贴图、且 renderer 朝向表与 XR 占位规则都明确的两台机器：`machine_catalytic_cracker` 与 `machine_catalytic_reformer`。

1.7.10 事实来源：

- 注册：`com.hbm.blocks.ModBlocks`
  - `machine_catalytic_cracker = new MachineCatalyticCracker(...)`
  - `machine_catalytic_reformer = new MachineCatalyticReformer(...)`
- 方块：
  - `com.hbm.blocks.machine.MachineCatalyticCracker`
  - `com.hbm.blocks.machine.MachineCatalyticReformer`
- 渲染：
  - `RenderCatalyticCracker`：`x + 0.5, y, z + 0.5`，禁用背面剔除，按 metadata 相对 `BlockDummyable.offset` 旋转。
  - `RenderCatalyticReformer`：同样禁用背面剔除、按 metadata 旋转，并带 `polaroidID == 11` 的彩蛋覆盖层；本轮只迁基础渲染，不迁彩蛋特效。
- 旧资源：
  - `models/block/machines/catalytic_cracker.obj` + `textures/block/machines/catalytic_cracker.png`
  - `models/block/machines/catalytic_reformer.obj` + `textures/block/machines/catalytic_reformer.png`

旧结构契约：

- `machine_catalytic_cracker`
  - `getDimensions() = {0,0,3,3,2,3}`，`getOffset() = 3`。
  - core metadata >= 12；extra/proxy metadata >= 6，旧 `TileEntityProxyCombo(false,false,true)`。
  - `checkRequirement` 在 core 周围检查 4 个 XR 大盒：
    - `{8,-1,3,-1,2,0}`
    - `{13,0,0,3,2,1}`
    - `{14,-13,-1,2,1,0}`
    - `{3,-1,2,3,-1,3}`
  - `fillSpace` 先铺同样 4 个 XR 大盒，再放 8 个 `makeExtra`，这些 extra 由 `dir.getRotation(UP)` 决定横向偏移。
  - 右击 `IItemFluidIdentifier` 可切换 tank type，`ILookOverlay` 会显示 4 个槽位信息；本轮暂不迁 GUI/overlay。
- `machine_catalytic_reformer`
  - `getDimensions() = {2,0,1,1,2,2}`，`getOffset() = 1`。
  - core metadata >= 12；extra/proxy metadata >= 6，旧 `TileEntityProxyCombo().fluid().power()`。
  - `checkRequirement` 在 core 周围检查 2 个 XR 大盒：
    - `{3,-3,1,0,-1,2}`
    - `{6,-3,1,1,2,0}`
  - `fillSpace` 先铺上述 2 个 XR 大盒，再放 6 个 `makeExtra`，其中 4 个是四角，2 个是按 `rot` 偏移的侧向点。
  - 物品 persistent NBT 显示 4 个流体槽；本轮暂不迁 tooltip/persistent 内容。

本轮现代迁移：

- 将两台机器接入 `LegacyVisibleMultiblockMachineBlock` / `LegacyMachineDefinition`。
- 采用旧版 renderer 的方向表，而不是共享默认旋转：
  - `NORTH -> 90`
  - `WEST -> 180`
  - `SOUTH -> 270`
  - `EAST -> 0`
- 以 `LegacyMultiblockLayout` 组合主 XR 和额外 XR 盒，保留旧 `checkRequirement` / `fillSpace` 的远端占位结构。
- `legacyVisibleMachine` BlockEntityType 继续统一承载这些可见多方块。

暂缓内容：

- `RenderCatalyticReformer` 的 `polaroidID == 11` 彩蛋覆盖层不迁入；这属于独立渲染彩蛋，不影响主结构契约。
- `GL_CULL_FACE` 的逐机启停目前仍未进入 `LegacyVisibleMachineRenderer`；如果后续发现催化塔背面缺面或透明面异常，再补渲染状态开关。
- GUI、真实配方、流体切换与 persistent tooltip 仍在机器逻辑切片处理。

验证：

- 本轮代码改动完成后会执行 `compileJava processResources runData`。

## 2026-05-24 装配/核处理/粒子机器可见多方块批次

触发来源：

- 用户要求继续推进多方块库移植，并允许一次推进更多。
- 本轮选择现代端 OBJ/贴图已齐全、旧版多方块足迹明确的五台机器：
  - `machine_assembly_factory`
  - `machine_purex`
  - `machine_silex`
  - `machine_exposure_chamber`
  - `machine_cyclotron`

1.7.10 事实来源：

- 方块：
  - `MachineAssemblyFactory`
  - `MachinePUREX`
  - `MachineSILEX`
  - `MachineExposureChamber`
  - `MachineCyclotron`
- 渲染：
  - `RenderAssemblyFactory`
  - `RenderPUREX`
  - `RenderSILEX`
  - `RenderExposureChamber`
  - `RenderCyclotron`
- 方块实体：
  - `TileEntityMachineAssemblyFactory`
  - `TileEntityMachinePUREX`
  - `TileEntitySILEX`
  - `TileEntityMachineExposureChamber`
  - `TileEntityMachineCyclotron`
- 旧资源：
  - `assembly_factory.obj/png`
  - `purex.obj/png`
  - `silex.obj/png`
  - `exposure_chamber.obj/png`
  - `cyclotron.obj/png`

旧结构契约：

- `machine_assembly_factory`
  - `getDimensions() = {2,0,2,2,2,2}`，`getOffset() = 2`。
  - 旧 `fillSpace`：主 XR 后，把 core 平面 5x5 外环标记为 extra/proxy；再沿 facing 方向在 `y+2` 的左右边各标记一排 extra/proxy。
  - 旧 proxy：`TileEntityProxyDyn().inventory().power().fluid()`。
  - 旧 renderer：先 Y 轴 90 度，再按 metadata 表旋转；现代等价表为 `NORTH=90,WEST=180,SOUTH=270,EAST=0`。
- `machine_purex`
  - `getDimensions() = {4,0,2,2,2,2}`，`getOffset() = 2`。
  - 旧 `fillSpace`：主 XR 后，将 core 平面 5x5 外环标记为 extra/proxy。
  - 旧 proxy：`TileEntityProxyCombo().inventory().power().fluid()`。
  - 旧 renderer 与装配工厂同样是 90 度基础旋转加 metadata 表。
- `machine_silex`
  - `getDimensions() = {2,0,1,1,1,1}`，`getOffset() = 1`。
  - 旧 `fillSpace`：主 XR 后，在 core 朝向一格的 `y+1` 左右两侧标记两个 extra/proxy。
  - 旧 proxy：`TileEntityProxyCombo(true,false,true)`。
  - 旧 renderer 方向表：`NORTH=90,EAST=0,SOUTH=270,WEST=180`。
- `machine_exposure_chamber`
  - `getDimensions() = {4,0,2,2,2,2}`，`getOffset() = 2`。
  - 旧 `checkRequirement/fillSpace`：主 XR 外，还从 core 原点和 `rot=dir.getRotation(UP).getOpposite()` 的 7 格侧向原点填多段额外 XR 空间。
  - 旧 `makeExtra`：侧向远端的 5 个点标记为 proxy。
  - 旧 proxy：`TileEntityProxyCombo().inventory().power()`。
  - 旧 renderer 方向表同 SILEX。
- `machine_cyclotron`
  - `getDimensions() = {2,0,2,2,2,2}`，`getOffset() = 2`。
  - 旧 `fillSpace`：主 XR 后，将 core 平面边缘 12 个点标记为 extra/proxy。
  - 旧 renderer 不按 block metadata 旋转，直接在 `x+0.5,y,z+0.5` 渲染 `Body/B1/B2/B3/B4`；插头动态贴图和彩蛋文字属于后续真实逻辑/渲染切片。

本轮现代迁移：

- 五台机器接入 `LegacyVisibleMultiblockMachineBlock` / `LegacyMachineDefinition`。
- 继续复用 `LegacyMultiblockLayout` 表达主 XR、额外 XR、check/fill 一体足迹与 proxy offset。
- `LEGACY_VISIBLE_MACHINE` BlockEntityType 加入五台机器。
- 补齐 Forge OBJ block model JSON、DataGen blockstate/item model、loot/tag 和双语名称。
- 只迁静态可见 OBJ 关键分件：
  - 装配工厂：`Base/Frame/Slider*/Arm*/Head*/Striker*/Blade*`。
  - PUREX：`Base/Frame/Fan/Pump`。
  - SILEX：全模型。
  - 辐照舱：`Chamber/Magnets/Core`。
  - 回旋加速器：`Body/B1/B2/B3/B4`。

暂缓内容：

- GUI、库存、能量/流体 side 规则、recipe tick、声音/粒子、动画插值与 overlay 留给各机器逻辑切片。
- 回旋加速器四个 plug 的动态贴图、满 plug 后的环绕文字特效暂缓到真实 `TileEntityMachineCyclotron` 迁移。
- 装配工厂四臂动画、PUREX 风扇/泵动画、辐照舱磁体/核心动画暂使用静态位置，后续由真实 BlockEntity 状态驱动。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `.\gradlew.bat runData --no-daemon --rerun-tasks` 通过。
- `runData` 已生成五台机器的 blockstate、item model、loot table、pickaxe/needs_iron_tool tag 和 en_us/zh_cn lang。

## 2026-05-24 热解炉/固化机/压缩机/大型流体罐批次

触发来源：

- 用户要求继续推进多方块库移植，并允许一次推进更多；如涉及其他库功能，则补其他库。
- 本轮选择现代端 OBJ/贴图已齐全、1.7.10 方块类和 renderer 都明确的四台 `BlockDummyable` 机器。

1.7.10 事实来源：

- 方块：
  - `com.hbm.blocks.machine.MachinePyroOven`
  - `com.hbm.blocks.machine.MachineSolidifier`
  - `com.hbm.blocks.machine.MachineCompressor`
  - `com.hbm.blocks.machine.MachineBigAssTank`
- 方块实体：
  - `TileEntityMachinePyroOven`
  - `TileEntityMachineSolidifier`
  - `TileEntityMachineCompressor`
  - `TileEntityMachineBigAssTank`
- 渲染：
  - `RenderPyroOven`：渲染 `Oven`、动画 `Slider`、动画 `Fan`，朝向表 `NORTH=180,EAST=90,SOUTH=0,WEST=270`。
  - `RenderSolidifier`：渲染 `Main`、按流体量渲染 `Fluid`、透明渲染 `Glass`，朝向表 `NORTH=90,EAST=0,SOUTH=270,WEST=180`。
  - `RenderCompressor`：渲染 `Compressor`、动画 `Pump`、动画 `Fan`，朝向表 `NORTH=90,EAST=0,SOUTH=270,WEST=180`。
  - `RenderBigAssTank`：渲染 `bigasstank` 全模型，朝向表按 `metadata - 10` 计算，等价 `NORTH=270,EAST=180,SOUTH=90,WEST=0`；流体条和危险菱形属于真实逻辑/流体渲染后续切片。
- 旧资源：
  - `models/block/machines/pyrooven.obj` + `textures/block/machines/pyrooven.png`
  - `models/block/machines/solidifier.obj` + `textures/block/machines/solidifier.png`
  - `models/block/machines/compressor.obj` + `textures/block/machines/compressor.png`
  - `models/block/machines/bigasstank.obj` + `textures/block/machines/bigasstank.png`

旧结构契约：

- `machine_pyrooven`
  - `getDimensions() = {2,0,3,3,2,2}`，`getOffset() = 3`。
  - core metadata >= 12；extra/proxy metadata >= 6，旧 `TileEntityProxyCombo().inventory().power().fluid()`。
  - `fillSpace` 先铺主 XR，再沿面对方向从 `-2..2` 于右侧 2 格放一排 `makeExtra`，再在 core 后侧顶层放一个 `makeExtra`。
- `machine_solidifier`
  - `getDimensions() = {3,0,1,1,1,1}`，`getOffset() = 1`。
  - core metadata >= 12；extra/proxy 为 inventory + power + fluid。
  - `fillSpace` 先铺主 XR，再额外放顶端一格和 y+1 十字四格 `makeExtra`。
- `machine_compressor`
  - `getDimensions() = {2,0,1,2,1,1}`，`getOffset() = 2`。
  - core metadata >= 12；extra/proxy 为 fluid + power。
  - 额外 XR 填充 `{3,-3,1,1,1,1}` 与 `{8,-4,0,0,1,1}`，并在 core 前后/左右局部放 3 个 `makeExtra`。
- `machine_bigasstank`
  - `getDimensions() = {5,0,4,4,4,4}`，`getOffset() = 6`。
  - core metadata >= 12；extra/proxy 为 fluid proxy。
  - 额外 XR 填充六个环/管段盒，并在 facing 正负 6 格放两个 `makeExtra`。

本轮现代迁移：

- 四台机器接入 `LegacyVisibleMultiblockMachineBlock` / `LegacyMachineDefinition`，沿用统一多方块放置、dummy 清理、proxy capability 转发和 BER 渲染盒。
- 按旧 renderer 为每台设置独立 `yRotation(...)`，不走默认旋转公式。
- 热解炉、固化机、压缩机暂渲染静态关键部件；动画部件先保持默认位置，真实 tick 状态后续随对应 BlockEntity 迁入。
- 大型流体罐本轮只迁可见外壳、占位与边界；流体持久 NBT、比较器、流体类型切换、危险菱形和液位条留给储罐逻辑切片。
- 为本批机器显式使用结构 `layout(...).shape(1.0D)` 作为碰撞/选择形状，避免可见多方块默认只是一格满方块。

暂缓内容：

- GUI、库存、能量/流体 side 规则、recipe tick、声音/粒子、动画插值、流体显示与 persistent tooltip 均暂缓到机器逻辑切片。
## 2026-05-24 底层钩子补齐与动力/加工可见多方块批次

触发来源：
- 用户要求继续推进多方块库移植，并特别建议检查底层还可以移植什么。
- 本轮先核对 1.7.10 `BlockDummyable` 的通用钩子，再批量迁入一批 OBJ/贴图齐全、结构边界明确的机器。

底层补足：
- `LegacyMachineDefinition` 新增 `legacyHeightOffset`，对应 1.7.10 `BlockDummyable#getHeightOffset()`；`LegacyXrMultiblockBlock#getDirectPlacementCore` 与 `setPlacedBy` 现在会把点击层先按高度偏移再计算 core。
- `LegacyMachineDefinition` 新增 `placementFacing` 钩子，对应 1.7.10 `getDirModified(ForgeDirection)` 的定义层入口。当前批次暂未启用复杂方向修正，但 PA/RBMK 等后续机器可以直接复用。
- `LegacyMachineDefinition` 新增 `modelTranslation`，`LegacyVisibleMachineRenderer` 在旋转后应用模型局部平移；用于对齐 1.7.10 renderer 中 `glTranslated(-0.5,...)`、`glTranslated(2,...)` 这类模型原点补偿。

新增可见多方块机器：
- `machine_arc_welder`
- `machine_soldering_station`
- `machine_mixer`
- `machine_radiolysis`
- `machine_radgen`
- `machine_rotary_furnace`
- `machine_steam_engine`
- `machine_solar_boiler`
- `machine_tower_small`
- `machine_tower_large`
- `machine_turbofan`
- `machine_turbinegas`

1.7.10 对齐来源：
- 方块类：`MachineArcWelder`、`MachineSolderingStation`、`MachineMixer`、`MachineRadiolysis`、`MachineRadGen`、`MachineRotaryFurnace`、`MachineSteamEngine`、`MachineSolarBoiler`、`MachineTowerSmall`、`MachineTowerLarge`、`MachineTurbofan`、`MachineTurbineGas`。
- renderer：`RenderArcWelder`、`RenderSolderingStation`、`RenderMixer`、`RenderRadiolysis`、`RenderRadGen`、`RenderRotaryFurnace`、`RenderSteamEngine`、`RenderSolarBoiler`、`RenderSmallTower`、`RenderLargeTower`、`RenderTurbofan`、`RenderTurbineGas`。
- 结构依据：以旧版 `getDimensions()`、`getOffset()`、`fillSpace(...)`、`makeExtra(...)` 为准；额外代理格按 core-relative offset 映射到 `LegacyMultiblockLayout`。

本轮现代侧改动：
- 上述 12 台机器接入 `LegacyVisibleMultiblockMachineBlock` 和统一 `LEGACY_VISIBLE_MACHINE` BlockEntityType。
- 补齐 Forge OBJ block model JSON、DataGen blockstate/item model/loot/tag/lang 入口。
- 静态渲染先保留主要模型部件：例如搅拌机 `Main/Mixer`、回转炉 `Furnace/Piston`、蒸汽机 `Base/Flywheel/Shaft/Transmission/Piston`、涡扇 `Body/Blades/Afterburner`。
- 真实 GUI、能量/流体/物品 side 规则、工作动画插值、流体可视层、特殊光效与 overlay 暂留给机器逻辑切片。

注意事项：
- `TileEntityProxyCombo` 的 inventory/power/fluid 分类型代理仍未完全移植；当前 dummy proxy 仍是“全能力转发”的粗粒度桥接。后续若机器开始接入真实 capability，应把 proxy 类型拆到 `MultiblockDummyBlockEntity`。
- `ForgeDirection#getRotation(UP/DOWN)` 的左右手方向已经按当前项目既有约定迁入：UP 多数使用 `getClockWise()`，DOWN 使用 `getCounterClockWise()`；如果实机观察某台机器侧面 proxy 反向，优先核对该机器旧 renderer/overlay 的 hitCheck。

验证：
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `.\gradlew.bat runData --no-daemon --rerun-tasks` 通过，DataGenerator 写入新增机器资源。

## 2026-05-24 可见多方块机器物品显示补齐

触发来源：

- 大型多方块机器的世界渲染已能工作，但物品栏中仍走 block item model，遇到大型 Forge OBJ 时表现为紫黑缺失纹理、比例不对或不居中。
- 1.7.10 中这类机器主要通过 `ItemRenderLibrary` 与 TESR `IItemRendererProvider` 把同一套 OBJ renderer 用于库存显示，而不是让普通方块模型承担物品显示。

本轮现代侧改动：

- `MultiblockBlockItem` 识别 `LegacyVisibleMultiblockMachineBlock` 后接入共享物品 renderer。
- `HbmBlockStateProvider` 新增 `visibleMachineWithItemRenderer(...)`，当前所有可见多方块机器的 item model 改生成为 `minecraft:builtin/entity`，由 renderer 控制库存姿态。
- 物品 renderer 以 `LegacyMachineDefinition.renderBoundingBox(...)` 自动适配大机器尺寸，保持多方块库中的渲染 AABB 同时服务视锥剔除和物品居中缩放。
- 二次修正：`MultiblockBlockItem.initializeClient(...)` 不再读取 `getBlock()` 做类型判断，因为 Forge 在 `Item` 构造期间就调用该钩子，此时 `BlockItem` 字段尚未初始化；类型过滤移动到实际 `renderByItem(...)` 阶段。

暂缓内容：

- 后续如果某台机器在 1.7.10 的 item renderer 使用了和世界 renderer 不同的 group 或额外 GL 变换，应继续扩展 `LegacyMachineDefinition` 的 item 渲染参数，而不是回退到普通 block item JSON。

## 2026-05-24 物品栏模型挂接与 core 偏心修正

问题定位：

- `machine_battery_socket` 和 `machine_assembly_machine` 的 item model 仍指向普通 block model，即使 `MultiblockBlockItem` 已提供 BEWLR，Forge 也不会进入专用 OBJ 物品 renderer。
- 旧版多方块 item renderer 依据具体 OBJ 和具体 group 绘制，core 位置不等于模型视觉中心；现代端用结构 AABB 估算比例会让偏心机器在物品栏里偏移或过大。

本轮现代侧改动：

- `HbmBlockStateProvider` 新增 `existingModelWithCustomItem(...)` / `customBlockItem(...)`，让需要 BER 世界模型但 item 走 BEWLR 的机器稳定生成 `minecraft:builtin/entity`。
- `machine_battery_socket` 与 `machine_assembly_machine` 的手写 item JSON 改为 `minecraft:builtin/entity`，并由 datagen 同步生成一致资源。
- 可见多方块 item renderer 现在按实际 OBJ bounds 计算缩放与中心，结构盒继续只负责世界碰撞/选择/剔除，不再混作库存图标边界。
- `machine_solidifier` 的库存 group 按 1.7.10 改为 `Main`，避免把旧世界态的 `Fluid/Glass` 一起塞进创造物品栏图标。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `.\gradlew.bat runData --no-daemon --rerun-tasks` 通过；`machine_solidifier`、`machine_battery_socket`、`machine_assembly_machine` 的生成 item model 均为 `minecraft:builtin/entity`。

## 2026-05-24 物品栏缺模型与比例二次修补

问题定位：

- 物品栏仍存在缺失模型时，问题不在多方块放置/BlockItem 挂接，而是部分旧 OBJ 保留 `mtllib` 引用；1.7.10 自家 OBJ 路径靠代码绑定贴图，现代 Forge OBJ loader 会尝试读取 `.mtl`，遇到缺失文件或大写路径会直接让 block model 烘焙失败。
- 部分机器库存图标偏大，是公共 `LegacyVisibleMachineItemRenderer` 的自动 fit 仍过宽；旧版每台机器的 `renderInventory()` 有独立缩放，现代端需要把这个调节点留在机器定义层。

本轮现代侧改动：

- 清理已知会破坏 Forge OBJ loader 的机器 OBJ `mtllib` 行，修复 `catalytic_cracker`、`fluidtank`、`radiolysis` 等 block model 烘焙失败来源。
- `LegacyMachineDefinition` 增加 `itemFitSize`，多方块机器的库存占位大小可按旧 `renderInventory()` 逐台微调。
- `LegacyMachineDefinition` 增加 `itemPartTextures`，支持旧 renderer 中按 part 切换贴图的库存显示；当前用于涡扇 `Afterburner -> turbofan_back`。
- 公共 item renderer 默认 fit 收紧，降低大机器在创造栏里压线/溢出相邻槽位的概率。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-24 liquefactor 指向项修正

- `machine_liquefactor` 继续显示异常时，按 1.7.10 `ItemRenderLibrary` 复核后确认其库存图标只应渲染 OBJ group `Main`。
- 现代端此前沿用世界渲染 group `Main/Fluid/Glass`，会把旧世界态透明/流体层也带入创造栏图标。
- `liquefactorDefinition` 已补 `itemRenderParts("Main")`。
- 公共 GUI 最大缩放上限恢复到 `0.32`，用于改善部分小/中型机器库存图标偏小；后续仍可用 `itemFitSize` 按单台旧 `renderInventory()` 继续精调。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
