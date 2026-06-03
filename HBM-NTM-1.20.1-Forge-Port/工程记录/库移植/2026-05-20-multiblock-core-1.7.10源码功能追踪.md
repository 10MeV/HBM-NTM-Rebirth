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

## 2026-05-25 直接放置 core 后置钩子补齐

触发来源：

- 继续复核 1.7.10 `BlockDummyable.onBlockPlacedBy(...)` 后确认，旧版多方块即使最终 core 被迁移到偏移位置，也仍会在放置流程中处理 core 初始化、持久 NBT 恢复、后续方块初始化钩子和 dummy 填充。
- 现代 `MultiblockBlockItem` 为避免原版 `BlockItem` 的大碰撞盒临时放置问题，直接在最终 core 位置 `setBlock(...)`，但此前只调用 `completeDirectMultiblockPlacement(...)` 填充 dummy，没有给 core 一个等价的放置后初始化入口。

问题定位：

- 不能直接在 `MultiblockBlockItem` 中调用 block 的 `setPlacedBy(...)`：
  - `LegacyXrMultiblockBlock#setPlacedBy(...)` 是给原版临时格放置路径保留的兼容入口，会按旧 `getOffset()` 再次迁移 core。
  - 直接放置路径若调用该 override，会把已经正确落在最终位置的 core 二次偏移。

本轮现代侧补齐：

- `LegacyMultiblockPlaceable` 新增 `afterDirectCorePlaced(...)` 钩子，专用于 `MultiblockBlockItem` 已经把 core 放到最终位置后的初始化。
- `MultiblockBlockItem` 在写入方块实体 item NBT 后、填充 dummy 前调用 `afterDirectCorePlaced(...)`。
- `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock` 的 direct-placement 实现只调用 `super.setPlacedBy(...)`，跳过自身兼容 relocation 逻辑，避免二次迁移。
- dummy 填充仍保留在 `completeDirectMultiblockPlacement(...)`，因此直接放置路径的 core 初始化和 dummy 填充顺序明确且不会重复。

效果：

- 后续多方块 core 若依赖 vanilla/modern `setPlacedBy(...)` 链上的初始化，直接放置路径可以稳定触发。
- 保留旧 `BlockDummyable` 的“临时格路径”和现代 `MultiblockBlockItem` 的“最终 core 直放路径”之间的职责分离，减少后续迁入高偏移机器时出现 core 二次偏移。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks` 通过。

## 2026-05-25 公共 core lookup 抽出

触发来源：

- 继续核对 1.7.10 `BlockDummyable#findCore(...)` 后确认，旧版从任意 dummy / extra 回溯 core 是一个共享入口：GUI 打开、碰撞/射线、复制配置、兼容层等都依赖它。
- 现代端此前 `DummyBlock` 内部有私有 `findCore(...)`，`CompatEnergyControl` 又手写了一套 `MultiblockDummyBlockEntity -> core BlockEntity` 解析；后续机器、命令和兼容层继续扩展时容易分叉。

本轮现代侧补齐：

- `MultiblockHelper` 新增公共解析入口：
  - `findCore(BlockGetter, BlockPos)`：从 core 或 dummy 位置解析到 `CoreLookup`。
  - `findCoreAt(BlockGetter, BlockPos corePos)`：验证目标 core 区块已加载且方块实现 `MultiblockCoreBlock`。
  - `resolveCoreBlockEntity(BlockEntity)`：如果传入 dummy BE，则返回 core BE；无法解析时保守返回原 BE。
  - `resolveCoreBlockEntity(BlockGetter, BlockPos)`：从世界坐标解析 core BE；坐标本身为 core 时返回自身 BE。
- `DummyBlock` 的选择盒/碰撞盒、破坏速度、中键拾取改用 `MultiblockHelper.findCore(...)`。
- `CompatEnergyControl.findTileEntity(...)` 改用 `MultiblockHelper.resolveCoreBlockEntity(...)`，避免兼容层复制 dummy 解析规则。

对 1.7.10 的对齐说明：

- 旧版 `findCoreRec(...)` 通过同方块 metadata 指向链寻找 core，且输入 core 自身时也返回 core；现代端 dummy 已直接保存 `CorePos`，因此不复刻递归 metadata 链，而是复刻“core/dummy 坐标可统一解析 core”的功能合同。
- 解析过程中保留现代端已有跨区块保护：core 区块未加载时不强行读取，也不把 dummy 当成孤儿。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-06-02 详细 hitbox 勘误与 gascent/sawmill/crucible 接入

触发来源：

- 继续推进多方块库移植，并对用户指出的 1.7.10 选中框/碰撞/粒子行为重新对齐。
- 本轮优先迁入已有 OBJ/贴图资源且 1.7.10 `BlockDummyable#bounding` 明确的三台机器：`machine_gascent`、`machine_sawmill`、`machine_crucible`。

1.7.10 事实来源：

- `com.hbm.blocks.BlockDummyable`：
  - `useDetailedHitbox()` 为 `!bounding.isEmpty()`。
  - 无 `bounding` 时，`addCollisionBoxesToList(...)` 与 `collisionRayTrace(...)` 调用 `super`，因此选中/碰撞表现为命中到的单个 core/dummy 方块。
  - 有 `bounding` 时，碰撞和射线检测先 `findCore(...)`，再按 core metadata 旋转每个 AABB；`shouldDrawHighlight(...)` 返回 true，并由 `drawHighlight(...)` 取消默认高亮后绘制整组详细线框。
  - `setBlockBoundsBasedOnState(...)` 对详细 hitbox 方块保留 `0..1, maxY=0.999` 的单块 bounds，用于物品碰撞修正。
- `com.hbm.blocks.machine.MachineGasCent`：
  - `getDimensions() = {3,0,0,0,0,0}`，`getOffset() = 0`。
  - `bounding` 为 `(-0.5,0,-0.5 -> 0.5,1,0.5)` 与 `(-0.4375,1,-0.4375 -> 0.4375,4,0.4375)`。
  - `createNewTileEntity(meta >= 12)` 为 `TileEntityMachineGasCent`；`meta >= 6` 理论为 `TileEntityProxyCombo(false,true,true)`，但 `fillSpace(...)` 只调用 super，实际没有 `makeExtra(...)` 生成 proxy。
  - `RenderCentrifuge` 对 gas cent 在普通离心机方向旋转后额外 `GL11.glRotatef(180, 0, 1, 0)`，并渲染 `Centrifuge`、`Flag`；物品只渲染 `Centrifuge`。
- `com.hbm.blocks.machine.MachineSawmill`：
  - `getDimensions() = {1,0,1,1,1,1}`，`getOffset() = 1`。
  - `fillSpace(...)` 先 super，再对 core 周围四个水平 cardinal 位置 `makeExtra(...)`；这些 extra dummy 使用 `TileEntityProxyCombo().inventory()`。
  - `bounding` 为 `(-1.5,0,-1.5 -> 1.5,1,1.5)`、`(-1.25,1,-0.5 -> -0.625,1.875,0.5)`、`(-0.625,1,-1 -> 1.375,2,1)`。
  - `RenderSawmill` 渲染 `Main`、`Blade`、`GearLeft`、`GearRight`，方向为 meta 3/5/2/4 -> 0/90/180/270。
- `com.hbm.blocks.machine.MachineCrucible`：
  - `getDimensions() = {1,0,1,1,1,1}`，`getOffset() = 1`。
  - 所有非 core metadata 都返回 `TileEntityProxyCombo().inventory()`，因此现代端所有 dummy offset 都标记 inventory proxy。
  - `bounding` 为 `(-1.5,0,-1.5 -> 1.5,0.5,1.5)`、四条边框 `(-1.25,0.5,-1.25 -> 1.25,1.5,-1)`、`(-1.25,0.5,-1.25 -> -1,1.5,1.25)`、`(-1.25,0.5,1 -> 1.25,1.5,1.25)`、`(1,0.5,-1.25 -> 1.25,1.5,1.25)`。
  - 碰撞/射线检测继承 `BlockDummyable`，因此按 core 朝向旋转详细 AABB；但 `MachineCrucible#drawHighlight(...)` 自己 override，绘制未旋转的 `bounding`。
- 旧注册材质：
  - `machine_gascent` 与 `machine_sawmill` 的 `setBlockTextureName(...)` 为 `block_steel`。
  - `machine_crucible` 的 `setBlockTextureName(...)` 为 `brick_fire`。
  - 旧 `zh_CN.lang` / `en_US.lang` 名称：气体离心机/Gas Centrifuge、斯特林锯木机/Stirling Sawmill、坩埚/Crucible。

本轮现代侧补齐：

- `LegacyMachineDefinition` 新增：
  - `highlightShape(...)`：默认使用 `collisionShape(...)`，用于承载坩埚这种“碰撞旋转、线框不旋转”的旧特例。
  - `particleState(...)`：默认返回旧结构填充块粒子；可按机器旧 `setBlockTextureName(...)` 覆盖。
- `MultiblockCoreBlock` 新增 `usesForwardedDummyShape(...)`、`usesForwardedDummyCollisionShape(...)` 与 `multiblockParticleState(...)`：
  - 默认不转发整机 shape，dummy 仍显示/碰撞自身一格。
  - 默认粒子为 `block_steel` 结构填充材质，保持 core/dummy 破坏粒子与旧结构填充块一致。
- `DummyBlock` 的 shape/collision 转发改为受 core 明确声明控制：
  - 无 `bounding` 的 legacy visible machine 不再让 dummy 使用整机 shape。
  - 有 `bounding` 的 legacy visible machine 才把 dummy 的选中/碰撞转发到 core 的详细 shape。
  - dummy 破坏粒子从 core 的 `multiblockParticleState(...)` 获取，坩埚这类非钢材质机器可回到旧材质粒子。
- `LegacyVisibleMultiblockMachineBlock`：
  - 无详细 `bounding` 时 core 自身仍是单方块 shape/collision。
  - 有详细 `bounding` 时 core/dummy 使用定义内详细 shape。
  - core 破坏粒子使用定义内 `particleState(...)`。
- 新接入 `machine_gascent`、`machine_sawmill`、`machine_crucible`：
  - 注册到 `ModBlocks` / 机器创造栏 / `LEGACY_VISIBLE_MACHINE` BlockEntity。
  - 接入 legacy XR layout、offset、proxy 标记、OBJ part 渲染、物品渲染、详细 AABB。
  - `gascent` 按旧 renderer 在离心机朝向基础上额外旋转 180 度。
  - `sawmill` 四个 cardinal dummy 使用 inventory proxy。
  - `crucible` 所有非 core dummy 使用 inventory proxy，碰撞 shape 旋转，高亮 shape 不旋转。
  - 新增 `gascent.json`、`sawmill.json` Forge OBJ 模型声明；`crucible.json` 已存在。
  - datagen 生成 blockstate、builtin/entity item model、loot table、pickaxe/iron-tool tags、英文/中文语言。

对 1.7.10 的勘误：

- “多方块不会显示整个碰撞箱，只显示命中方块单个方块体积”只适用于 `bounding` 为空的 `BlockDummyable` 机器。
- `bounding` 非空的机器在 1.7.10 会用详细 AABB 做碰撞和射线检测，并取消默认高亮绘制整组详细线框。
- 坩埚是特殊例外：碰撞/射线检测仍继承 `BlockDummyable` 的旋转详细 AABB，但高亮 override 绘制未旋转的 `bounding`。

仍然延期：

- `TileEntityMachineGasCent`、`TileEntitySawmill`、`TileEntityCrucible` 的真实菜单、配方、热量、材料栈、刀片状态、熔融液面渲染与右键逻辑尚未迁入；本轮只完成多方块库、占位、资源和可见模型脚手架。
- 锯木机旧物品损伤值 `1` 表示无刀片并影响掉落，目前现代注册还是单一 block item，待真实方块实体/掉落逻辑迁入时继续对齐。

验证：

- 已跑：`.\gradlew.bat compileJava --no-daemon --rerun-tasks`，通过。
- 首次 `runData` 在 `downloadAssets` 阶段因 `minecraft/lang/fr_fr.json` 下载超时失败；使用项目代理参数重跑 `runData` 成功。
- 已跑：`$env:JAVA_OPTS='-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Dnet.minecraftforge.gradle.check.certs=false'; .\gradlew.bat runData --no-daemon --rerun-tasks`，通过。

## 2026-06-02 详细 bounding 自定义高亮迁移

触发来源：

- 用户继续要求推进多方块库，并强调 1.7.10 默认不会显示整机碰撞/选择框，只显示当前指向方块的单格体积；需要在这个前提下对齐旧版详细 hitbox 行为。
- 上一轮已把普通无 `bounding` 多方块修回单格选择框/单格默认碰撞；本轮补齐旧 `ICustomBlockHighlight` 客户端高亮路径，避免用 `getShape(...)` 伪装整机高亮。

1.7.10 事实来源：

- `BlockDummyable#useDetailedHitbox()` 只在 `bounding` 非空时返回 true。
- `BlockDummyable#shouldDrawHighlight(...)` 只检查 `!bounding.isEmpty()`；无 `bounding` 的机器不画整机自定义高亮。
- `BlockDummyable#drawHighlight(...)` 通过 `findCore(...)` 从 core/dummy 回溯到 core，按 core metadata 计算 `ForgeDirection.getRotation(ForgeDirection.UP)`，对每个 `bounding` AABB 执行旧旋转、`expand(0.002F)` 和相对玩家/camera 偏移，再用 `RenderGlobal.drawOutlinedBoundingBox(...)` 绘制。
- `ICustomBlockHighlight#setup()` 使用黑色半透明线框、禁纹理、深度 mask false；`ModEventHandlerRenderer#onDrawHighlight(...)` 在命中块实现 `ICustomBlockHighlight` 且允许绘制时调用自定义绘制并取消默认高亮事件。

本轮现代侧补齐：

- 新增 `LegacyMultiblockHighlightRenderer`，订阅现代 Forge `RenderHighlightEvent.Block` 前置高亮事件。
- 命中位置先走 `MultiblockHelper.findCore(...)`，因此 core 与 dummy 都复用同一回溯/归属校验路径；无效残留 dummy 不会触发自定义高亮。
- 仅当 core 是 `LegacyVisibleMultiblockMachineBlock` 且 `LegacyMachineDefinition#hasCollisionShapeFactory()` 为 true 时绘制整机详细线框；这对应旧 `bounding` 非空机器。当前已声明详细 AABB 的机器包括 `machine_centrifuge`、`machine_ore_slopper`、`machine_gasflare`。
- 线框使用 `LegacyMachineDefinition#collisionShape(...)` 的旧 AABB 旋转结果，按 core 坐标减 camera 坐标渲染，逐个 `AABB.inflate(0.002D)` 后用 `LevelRenderer.renderLineBox(...)` 绘制黑色 0.4 alpha 线框，并取消原版单格 outline。

对 1.7.10 的对齐说明：

- 普通无 `bounding` 多方块仍保持原版单格高亮，不显示整机线框。
- 有 `bounding` 的多方块在指向 core 或任意有效 dummy 时显示整机详细线框；这和旧 `BlockDummyable#drawHighlight(...)` 的 core 回溯合同一致。
- 本轮没有把详细高亮混入普通 `getShape(...)`，因此不会再次把“选择框”误修成整机 shape。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-06-02 选择框、默认碰撞与破坏粒子勘误

触发来源：

- 用户指出 1.7.10 多方块不会显示整机碰撞箱，只会显示当前指向方块的单格碰撞体积；破坏时也只掉当前方块粒子，core 破坏粒子同样是结构填充块粒子。
- 复查旧 `BlockDummyable` 后确认，前几轮现代端把 layout shape 同时当作选择框和默认碰撞的做法混入了猜测内容；它能掩盖 dummy 填充缺失，但不是旧版默认合同。

1.7.10 事实来源：

- `BlockDummyable#useDetailedHitbox()` 只在 `bounding` 非空时返回 true。
- `BlockDummyable#addCollisionBoxesToList(...)` 在 `useDetailedHitbox() == false` 时直接调用 `super.addCollisionBoxesToList(...)`，因此默认物理碰撞来自当前 core/dummy 方块自己的 vanilla 单格完整方块。
- `BlockDummyable#collisionRayTrace(...)` 在 `useDetailedHitbox() == false` 时直接调用 `super.collisionRayTrace(...)`，因此默认选择/射线命中只针对当前指向的单个 core/dummy 方块。
- `BlockDummyable#setBlockBoundsBasedOnState(...)` 在无详细 hitbox 时调用 super；有详细 hitbox 时也只把当前方块 bounds 设为 `0..1, 0..0.999, 0..1`，整机详细高亮另由 `ICustomBlockHighlight#drawHighlight(...)` 绘制。
- `BlockDummyable#shouldDrawHighlight(...)` 只有 `bounding` 非空才返回 true；无 `bounding` 的机器没有整机自定义高亮。
- 旧源码未在 `BlockDummyable` 中实现整机破坏粒子；破坏效果沿 vanilla 当前被破坏方块生成。旧注册中组装机等 `BlockDummyable` 机器使用 `hbm:block_steel` 作为 block texture，所以 core/dummy 破坏粒子都是结构填充块/钢块粒子，而不是 OBJ 整机粒子。

本轮现代侧勘误：

- `DummyBlock#getShape(...)` 固定返回单格 `Shapes.block()`，不再把 core 的整机 layout shape 平移到 dummy 位置。
- `LegacyVisibleMultiblockMachineBlock`、`AssemblyMachineBlock`、`LiquefactorBlock`、`FluidTankBlock`、`MachineBatterySocketBlock` 的 `getShape(...)` / `getMultiblockShape(...)` 回到单格 shape，避免玩家指向 core 或 dummy 时显示整机选择框。
- `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock` 的默认 `getMultiblockCollisionShape(...)` 回到单格 shape；默认物理碰撞由实际填充出的 dummy/core 方块集合自然组成，和 1.7.10 无 `bounding` 路径一致。`MachineBatterySocketBlock` 旧源码同属无 `bounding` 的 `BlockDummyable`，现代端也不再保留 2x2 整体 collision shape。
- `MultiblockCoreBlock` 新增 `usesForwardedDummyCollisionShape(...)`。默认 false；只有有明确详细 collision factory 的可见多方块返回 true，使 dummy 只在旧详细 `bounding` 类机器上转发整机/精细碰撞。
- `ModBlocks` 移除所有 `.collisionShape(state -> stateLayoutShape(state))` 的 layout 兜底声明，只保留直接来自旧详细 AABB 的 `legacyRotatedShape(...)`。这一步清理了“用整机 layout shape 近似旧默认碰撞”的猜测内容。
- `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock` 增加统一 `addDestroyEffects(...)`，使用 `MultiblockHelper.steelParticleState()`；此前已有的 dummy、组装机、液化机钢块破坏粒子路径保持一致，流体罐和普通可见多方块 core 也补回结构填充块粒子。

对 1.7.10 的对齐说明：

- 无详细 `bounding` 的机器：选择框、ray trace、默认物理碰撞和破坏粒子都按当前指向/破坏的单个 core 或 dummy 方块处理；外围是否能挡人依赖 dummy 是否按旧结构完整填充。
- 有详细 `bounding` 的机器：仍保留从 dummy 回溯 core 并使用 `legacyRotatedShape(...)` 的整机/精细碰撞路径；旧版自定义整机高亮渲染本轮尚未迁入，后续若要补，应单独实现类似 `ICustomBlockHighlight` 的客户端高亮层，而不是继续滥用 `getShape(...)`。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-31 默认 layout shape 从外接盒改为离散 dummy 方块集合

触发来源：

- 继续推进多方块库移植时复核 1.7.10 `BlockDummyable` 的碰撞路径，发现现代端上一轮把默认 shape 修到三维高度后，仍然使用整机外接包围盒。
- 外接包围盒虽然能避免“只剩一层碰撞”，但会把旧版中真实为空的凹槽、中空和不连续结构也变成实心，不符合没有 `bounding` 详细盒时的旧 dummy 碰撞语义。

1.7.10 事实来源：

- `BlockDummyable#addCollisionBoxesToList(...)` 在 `useDetailedHitbox() == false` 时直接走 `super.addCollisionBoxesToList(...)`，也就是当前被命中的 core/dummy 方块自己的普通整方块碰撞。
- `BlockDummyable#collisionRayTrace(...)` 在无详细 `bounding` 时同样回到 vanilla 单方块 ray trace。
- `MultiblockHandlerXR.fillSpace(...)` 会在每个结构坐标放置一个 dummy metadata；因此默认碰撞不是整机外接盒，而是实际填充出来的 core/dummy 方块集合。
- 有详细 `bounding` 的机器仍走 `findCore(...) -> core 朝向 -> bounding 列表旋转` 的单独路径，现代端对应已有 `legacyRotatedShape(...)`。

本轮现代侧修正：

- `LegacyMultiblockLayout#shape(double height)` 从计算所有 offset 的外接长方体，改为对 `offsets()` 中每个实际填充坐标生成 `1xheightx1` 的 `VoxelShape` 并 union。
- 所有使用默认 `layout(state).shape(1.0D)` 的可见多方块、组装机以及 offset 型电池座，都会按真实 dummy 足迹提供选择/碰撞形状。
- `renderBoundingBox(...)` 仍保留外接盒逻辑，因为渲染包围盒需要覆盖整机可视范围，而不是模拟碰撞实体积。

对 1.7.10 的对齐说明：

- 这一步是对上一轮“碰撞高度修复”的继续收紧：高度仍来自旧 `getDimensions()` / 额外 offset 的实际 Y 层，但 X/Z/Y 空间不再把未填充位置当作实体。
- 已迁入详细 `bounding` 的机器不受默认 shape 变化影响，仍优先使用来源于 1.7.10 renderer/block 构造器的 AABB。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-26 makeExtra 偏移勘误：工业涡轮/液化机/SILEX

触发来源：

- 继续推进多方块库移植时，按 1.7.10 `fillSpace(...)` / `makeExtra(...)` 逐台复核现代端 `LegacyMachineDefinition` 中已迁入的 proxy/extra offset。
- 本轮重点排查“看起来有模型，但 dummy/proxy 位置不对”的机器；这些错误会直接影响外围 proxy 方块、端口连接和整机占位。

1.7.10 事实来源：

- `BlockDummyable#onBlockPlacedBy(...)` 先计算 `o = -getOffset()`，最终 core 放在 `placed + dir * o`，但随后调用各机器 `fillSpace(world, placedX, placedY, placedZ, dir, o)`；每台机器是否在 `fillSpace` 内把局部坐标移动到 core，需要按源码逐台判断。
- `MachineIndustrialTurbine#fillSpace(...)` 在 `x += dir.offsetX * o; z += dir.offsetZ * o;` 后，以 core 为原点调用：
  - `dir * 3 +/- rot * 1` 与 `-dir * 1 +/- rot * 1`；
  - `dir * 3, y + 2` 与 `-dir * 1, y + 2`；
  - `-dir * 3, y + 1`。
- `MachineLiquefactor#fillSpace(...)` 在移动到 core 后只把 `y + 1` 的四个水平相邻点和 `y + 3` 顶点升级为 extra proxy；没有地面额外 proxy，也没有 forward=2 的 proxy。
- `MachineSILEX#fillSpace(...)` 没有先整体平移局部坐标，但 `makeExtra` 使用 `x + dir * o +/- side, y + 1`；换算到 core-relative 后是 `forward = 0, side = +/-1, y = 1`。
- 同批复核确认：`MachineSolidifier` 与液化机同样是 `y + 1` 四邻点加 `y + 3` 顶点，现代端原定义已匹配；`MachineRotaryFurnace`、`MachineSteamEngine`、`MachineRadGen`、`MachinePumpjack`、`MachineCoker`、`MachineOreSlopper` 的 offset 公式本轮未发现偏差。

本轮现代侧修正：

- `LegacyMultiblockOffsets` 新增 `cardinal(int radius, int y)`，用于表达 legacy 中“同一半径但位于指定 Y 层”的四邻点，减少后续手写坐标猜测。
- `industrialTurbineProxyOffsets(...)` 修正为 1.7.10 的 `side = +/-1`、顶层 `y = 2`、背部 `forward = -3, y = 1`；移除此前的 `side = +/-2`、`y = 3`、`forward = -4` 误差。
- `liquefactorProxyOffsets(...)` 改为 `cardinal(1, 1) + (0,3,0)`，移除此前多出来的地面与 forward proxy。
- `silexProxyOffsets(...)` 改为 core-relative 的 `forward = 0, side = +/-1, y = 1`，移除此前错误的 forward 分量。

对 1.7.10 的对齐说明：

- 本轮只修正能够由 1.7.10 源码直接证明的偏移差异；像大型钢罐 `MachineBigAssTank` 的两个端口虽然源码里写法较绕，但换算成集合后与现代 `+/-6 forward` 相同，因此未改。
- 后续继续迁机器时，凡是旧 `fillSpace(...)` 内有 `x += dir * o`、`x -= dir * n` 或直接在 `makeExtra` 里混用 `o` 的，都必须先换算到 core-relative，再写入现代 layout。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过；仍有既有 `ClientModEvents.java` 中 `Biome.BIOME_INFO_NOISE` 过时警告。

## 2026-05-26 offset helper 与 ForgeDirection 手性勘误

触发来源：

- 继续推进多方块库移植时，复查多台旧 `BlockDummyable` 机器的 `fillSpace(...)`，发现现代端仍有一批手写 offset 逻辑散落在 `ModBlocks`，不利于继续做 1.7.10 对齐审计。
- 重点复查 `MachineRadGen`、`MachineRotaryFurnace`、`MachineSteamEngine`、`MachinePyroOven`、`MachineAssemblyFactory`、`MachineOreSlopper`、`MachineTurbofan`、`MachineTurbineGas`、`MachineIndustrialTurbine` 等旧源码。

1.7.10 事实来源：

- 已用 Forge 1.7.10 的 `ForgeDirection#getRotation(...)` 验证：`getRotation(UP)` 等价现代 `Direction#getClockWise()`；`getRotation(DOWN)` 等价现代 `Direction#getCounterClockWise()`。
- 旧 `BlockDummyable#onBlockPlacedBy(...)` 会按 `o = -getOffset()` 计算 core 位置，随后 `fillSpace(world, x, y, z, dir, o)` 中多数机器会先把局部原点移回 core，再按 `dir` 和旋转方向放置 `makeExtra(...)`。
- `MachinePyroOven#fillSpace(...)` 使用的是 `dir.getRotation(ForgeDirection.DOWN)`；现代端此前按 clockwise 表达，属于和 1.7.10 不一致的猜测内容。

本轮现代侧修正：

- `LegacyMultiblockOffsets` 新增并集中表达：
  - `legacyUpSide(facing)` / `legacyDownSide(facing)`；
  - 带显式侧向轴的 `relative(...)`；
  - `lineAlongFacing(...)`、`lineAlongSide(...)`；
  - `combine(...)`；
  - 原先散落的 `xrBox(...)`、`floorCorners(...)`、`cardinal(...)`、`squarePerimeter(...)`、`squareSidesWithoutCorners(...)`、`isSquarePerimeter(...)`。
- `ModBlocks` 中角点、十字、方形外围和 XR box 结构统一改走 `LegacyMultiblockOffsets`，减少后续继续迁入机器时重复手写旋转/范围逻辑。
- `rad_gen`、`rotary_furnace`、`steam_engine`、`tower_large`、`turbofan`、`turbine_gas`、`industrial_turbine`、`ore_slopper`、`assembly_factory`、`liquefactor`、`compressor`、`big_ass_tank`、`catalytic_cracker`、`catalytic_reformer` 等 proxy/structure offset 表达改为库 helper；这些主要是等价重写。
- `pyro_oven` 的 proxy 侧向轴从现代 `clockwise` 修正为 `legacyDownSide(facing)`，对齐旧 `dir.getRotation(DOWN)`。

对 1.7.10 的对齐说明：

- 本轮不是新增机器行为，而是把旧源码中反复出现的“前向 + UP/DOWN 旋转侧向 + 高度”的坐标合同提升到库层，后续勘误时能直接看出某台机器使用的是旧 UP 还是旧 DOWN。
- 已确认存在猜测内容并修正的是 `MachinePyroOven` 的侧向手性；其余本轮改动保持现有 core-relative footprint 行为，只把表达方式换成可审计 helper。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-26 layout proxy mode 叠加语义收紧

触发来源：

- 继续推进多方块库移植时复查 `LegacyMultiblockLayout` 的链式组合 API，发现 `withProxyPredicate(...)` / `withProxyOffsets(...)` 会替换整张 proxy mode 函数。
- 当前已迁机器大多只声明一次 proxy，或先加普通 extra 再声明 proxy，暂时不容易踩中；但旧 1.7.10 `fillSpace(...)` 经常由主 XR、额外 XR 盒、多段 `makeExtra(...)` 叠加组成，后续继续批量迁机器时容易因为第二次 proxy 声明覆盖第一次而漏写 proxy/legacy extra。

1.7.10 对齐依据：

- 旧 `makeExtra(...)` 是对当前位置已有 dummy 的升级或额外放置，不会因为后续再调用一段 `makeExtra(...)` 就取消前面已经放置的 extra/proxy 身份。
- 因此现代 `LegacyMultiblockLayout` 的 proxy mode 组合应接近“追加/升级”，而不是“整表替换”。

本轮现代侧修正：

- `LegacyMultiblockLayout#withProxyModes(...)` 改为与已有 proxy mode 合并：已有 offset 已是 proxy 时保留原 mode；否则采用新声明的 mode。
- `withExtraOffsets(..., Function<BlockPos, LegacyProxyMode>)` 复用同一合并规则，避免多段 extra 或后续 proxy 声明互相覆盖。

效果：

- 后续迁入带多段 `makeExtra(...)` 的机器时，可以安全链式组合多个 proxy/extra offset 来源。
- 当前机器的已验证 footprint 和 proxy mode 不变；这是库层行为收紧。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-26 legacy DirPos 远程端口辅助与组装机网络接入

触发来源：

- 继续推进多方块库移植时复查已迁移的化工厂与组装机，发现两者在 1.7.10 中共享同一套 `DirPos` 远程连接点，但现代端只有化工厂手写了这套 12 点端口，组装机仍只订阅相邻能量网络，且没有接入 HBM 流体网络。

1.7.10 事实来源：

- `MachineAssemblyMachine` 与 `MachineChemicalPlant`：
  - `getDimensions() = {2, 0, 1, 1, 1, 1}`，`getOffset() = 1`。
  - `fillSpace(...)` 先走 `BlockDummyable`/XR 填充，再把坐标后移一格，在核心所在 Y 层补一个 3x3 外围环。
  - `createNewTileEntity(...)` 对 extra dummy 返回 `new TileEntityProxyCombo().inventory().power().fluid()`。
- `TileEntityMachineAssemblyMachine#getConPos()` 与 `TileEntityMachineChemicalPlant#getConPos()` 完全一致，返回核心周围半径 2 的 12 个地面远程连接点：
  - `x + 2, z -1..1` 方向 `POS_X`；
  - `x - 2, z -1..1` 方向 `NEG_X`；
  - `z + 2, x -1..1` 方向 `POS_Z`；
  - `z - 2, x -1..1` 方向 `NEG_Z`。
- 两个旧 TileEntity 的服务端 tick 都对这组 `DirPos` 执行：
  - `trySubscribe(worldObj, pos)` 订阅能量；
  - 输入 tank 类型不是 `Fluids.NONE` 时订阅对应流体；
  - 输出 tank 有内容时向端口 `tryProvide(...)`。

本轮现代侧补齐：

- 新增 `LegacyMultiblockPorts`，提供 `xrFloorRingEnergyPorts(radius)` 与 `xrFloorRingFluidPorts(radius)`，用于生成旧 XR 多方块常见的地面外圈远程端口；`radius = 2` 时顺序与上述 1.7.10 `getConPos()` 一致。
- `ChemicalPlantBlockEntity` 从手写 12 个端口改为使用 `LegacyMultiblockPorts.xrFloorRing*Ports(2)`，行为不变但端口规则收束到库层。
- `AssemblyMachineBlockEntity` 改为实现 `HbmStandardFluidTransceiver`，暴露输入/输出 tank 列表，并在服务端 tick 中：
  - 使用同一组 12 个 legacy 远程端口订阅能量；
  - 当输入 tank 类型有效时订阅 HBM 流体网络；
  - 当输出 tank 有内容时向 HBM 流体网络提供流体。

对 1.7.10 的对齐说明：

- 这次不改变组装机的 Forge fluid handler 行为，只补回旧 HBM 网络路径。
- 组装机没有像化工厂那样的流体容器槽位装卸逻辑；本轮仅迁回 1.7.10 中确定存在的远程端口网络订阅/提供合同。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-26 legacy 多方块 offset 生成规则库化

触发来源：

- 继续推进多方块库移植时发现 `ModBlocks` 内仍保留多组和机器无关的旧 XR 坐标展开/平面 proxy 点集生成逻辑，例如额外 XR 盒、地面四角、十字、方形外环、带 facing 的 forward/side 相对坐标。
- 这些规则本质属于 `BlockDummyable` / `MultiblockHandlerXR` 迁移层；继续散落在机器注册类里会让后续机器对齐时更容易出现坐标语义分叉。

1.7.10 事实来源：

- `MultiblockHandlerXR.fillSpace(...)` 以 `{U,D,N,S,W,E}` 维度、facing 旋转和原点坐标展开一个旧 XR 方盒。
- 大量旧 `fillSpace(...)` 在主 XR 之外叠加额外 `MultiblockHandlerXR.fillSpace(...)` 或 `makeExtra(...)`；现代端需要反复生成“额外 XR 盒 offsets”和常见平面 proxy 点集。

本轮现代侧补齐：

- 新增 `LegacyMultiblockOffsets`：
  - `xrBox(...)`：按旧 XR 维度、facing 和原点展开 offsets，保留“跳过 box 原点”与“只跳过全局 core”的两种语义。
  - `relative(...)`：用 facing + clockwise rot 生成旧机器常用的 forward/side 平面偏移。
  - `floorCorners(radius)`、`cardinal(radius)`、`squarePerimeter(radius)`、`squareSidesWithoutCorners(radius)`、`isSquarePerimeter(...)`：集中表达旧 `makeExtra` 常见平面点集。
- `ModBlocks` 中以下机器的重复坐标逻辑改为调用库 helper，实际足迹和 proxy mode 不变：
  - 化工厂/化工厂大型、炼油厂、真空蒸馏塔、分馏塔、加氢处理器、焦化装置、流体罐、火炬、PUREX、回旋加速器、放射分解机、小冷却塔。
  - 催化裂化装置、催化重整装置、压缩机、大型流体罐、曝光室、泵井的额外 XR 盒 offset 生成。

对 1.7.10 的对齐说明：

- `xrBox(..., includeBoxOrigin = true)` 对应旧额外 XR 盒原点不是 core 时“保留额外盒原点，只跳过全局 core”的场景；这是焦化装置等结构此前已经勘误过的语义。
- 本轮是库化与消重，不引入新机器行为；后续再接入新旧多方块时应优先使用 `LegacyMultiblockOffsets`，避免在注册类里复制 `MultiblockHandlerXR` 展开逻辑。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-25 可见多方块默认 shape 勘误

触发来源：

- 继续推进多方块库时发现，当前大部分可见机器在游戏中容易表现为“只有核心方块和 OBJ 模型，外围像没有填充方块”。
- 复核 1.7.10 `BlockDummyable` 后确认，外围 dummy 本来就不负责独立渲染；它们的核心职责是占位、碰撞/选择、右键转发、破坏联动和 proxy 能力。
- 现代端的 `hbm:dummy_block` 也刻意使用 `RenderShape.INVISIBLE`，因此“看不见钢块外壳”不是错误；真正错误是可见多方块默认选择/碰撞 shape 退回了单格核心。

勘误结论：

- `LegacyMachineDefinition#collisionShape(...)` 已经提供正确默认值：没有单台精细碰撞盒时，应使用 `layout(state).shape(1.0D)` 覆盖整机 layout。
- `LegacyVisibleMultiblockMachineBlock#getMultiblockShape(...)` 此前在没有显式碰撞工厂时返回 `Shapes.block()`，导致 dummy 通过 `DummyBlock` 回溯 core shape 后，只拿到一格核心形状；外围 dummy 实际存在，但选中/碰撞体验像不存在。

本轮现代侧修正：

- `LegacyVisibleMultiblockMachineBlock#getMultiblockShape(...)` 统一返回 `definition.collisionShape(state)`。
- 单台已有 `.collisionShape(...)` 的机器仍使用精细/显式 shape；没有显式配置的可见多方块机器现在默认使用旧 XR layout 的整机占位形状。

对 1.7.10 的对齐说明：

- 1.7.10 `BlockDummyable` 的 dummy 是同一个方块 ID 的 metadata 形态，普通 dummy 并不绘制 OBJ 外围块；现代端用独立隐形 dummy block 表达同一语义是正确路线。
- 1.7.10 `addCollisionBoxesToList(...)` / `collisionRayTrace(...)` 会从任意 dummy `findCore(...)` 回到 core，再按整机 `bounding` 或默认方块盒参与碰撞与射线；现代端现在通过 `MultiblockCoreBlock#getMultiblockShape(...)` 恢复这条路径。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 dummy 跨区块加载保护

触发来源：

- 继续复核 1.7.10 `BlockDummyable.destroyIfOrphan(...)` 后确认，旧版 dummy 在判断自己是否成为孤儿前，会先用 `world.checkChunksExist(...)` 确认周围区块已加载。
- 现代端此前在 `MultiblockDummyBlockEntity.serverTick(...)` 中只要读不到 core 方块就移除 dummy；当 core 所在区块未加载或跨区块边界加载顺序不一致时，可能误删仍属于有效多方块的 dummy。

本轮现代侧补齐：

- `MultiblockDummyBlockEntity.serverTick(...)` 现在只有在 `CorePos` 所在区块已加载，且该位置确实不是 `MultiblockCoreBlock` 时才清理 dummy。
- dummy 右键转发、破坏 core 联动和 capability proxy 转发都会先检查 core 区块是否已加载：
  - 未加载时不强行读取 core block entity。
  - 未加载时不把 dummy 当成孤儿处理。
- `DummyBlock` 的 shape 回溯在 core 区块未加载时回退到单格 shape，避免客户端/服务端为了选择盒读取未加载 core。
- `MultiblockHelper.checkSpace(...)` 的可替换判断增加区块加载门槛；未加载目标位置不再被当作空气或可替换方块处理。

效果：

- 该补丁对齐旧 `destroyIfOrphan` 的防误删语义，降低大型多方块跨区块加载、卸载、远距离重进视野时出现 dummy 自删或错误破坏 core 的风险。
- capability/右键/shape 的未加载 core 行为现在偏向保守等待，而不是制造一次错误世界修改。

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
  - 勘误：旧 `MachineCentrifuge#fillSpace(...)` 只调用 `super.fillSpace(...)`，没有 `makeExtra(...)`；XR 填出的上方结构是普通 dummy，不生成 `TileEntityProxyCombo(false, true, true)`。
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

- 勘误：本轮当时判断 `machine_sawmill` 现代端缺少 OBJ/贴图资源是错误记录；2026-06-02 已确认 `sawmill.obj/png` 存在并接入可见多方块 scaffold，动态锯片状态仍待机器逻辑切片。
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

## 2026-05-25 proxy dummy 分类型能力迁移

触发来源：

- 回到多方块库基础移植后，复核 1.7.10 `TileEntityProxyCombo` 发现旧版 proxy 不是“所有能力全转发”，而是由 `inventory()`、`power()`、`conductor()`、`fluid()`、`heatSource()`、`moltenMetal()` 逐项开关控制。
- 现代端此前只有 `Proxy=true/false`，会把所有 Forge capability 请求转发到 core，后续接入真实机器逻辑和网络时会过宽。

本轮现代侧补齐：

- 新增 `LegacyProxyMode`，表达旧 `TileEntityProxyCombo` 的 inventory/power/conductor/fluid/heat/moltenMetal 开关。
- `MultiblockDummyBlockEntity` 的 `getCapability(...)` 改为按 `LegacyProxyMode` 过滤：
  - `inventory` 对应 `ForgeCapabilities.ITEM_HANDLER`。
  - `power` 或 `conductor` 对应 `ForgeCapabilities.ENERGY`。
  - `fluid` 或 `moltenMetal` 对应 `ForgeCapabilities.FLUID_HANDLER`。
- dummy NBT 继续保存旧 `Proxy` 布尔值，并新增 `ProxyInventory`、`ProxyPower`、`ProxyConductor`、`ProxyFluid`、`ProxyHeat`、`ProxyMoltenMetal`、`ProxyAll`。
  - 旧存档只有 `Proxy=true` 且没有细分字段时，仍按 `ProxyAll` 读取，保证兼容。
- `LegacyMultiblockLayout` 从 `Predicate<BlockPos>` 升级为 offset -> `LegacyProxyMode`：
  - 旧 `withProxyPredicate(...)` / `withProxyOffsets(...)` 仍保留，默认映射为 `LegacyProxyMode.fullCombo()`，即旧 `TileEntityProxyCombo(true,true,true)` 的 inventory/power/fluid 组合；真正的任意 Forge capability 透传只保留给显式 `LegacyProxyMode.all()` 与旧存档兼容读取。
  - 新增带 `LegacyProxyMode` 的 overload，供机器按 1.7.10 proxy 类型精确声明。
- `MultiblockHelper` 新增 `fillOffsetsWithProxyModes(...)` / `fillUpWithProxyModes(...)`，核心填充路径现在把 proxy mode 写入 dummy。
- `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock` 已改用 typed proxy 填充路径。

已按 1.7.10 方块类核对并接入的 proxy 类型：

- `inventory + power + fluid`：组装机、化工厂、流体固化机、热解炉、PUREX、装配工厂、回旋加速器、辐射分解、矿泥机等。
- `inventory + power`：电池座、辐射发电机、辐照舱。
- `inventory + fluid`：SILEX、回转炉、焦化塔。
- `power + fluid`：泵机、离心机、压缩机、真空蒸馏/加氢/催化重整、涡扇、燃气轮机、蒸汽机等。
- `fluid`：流体罐、大小塔、大型流体罐、催化裂化、分馏塔、太阳锅炉等。

暂缓内容：

- 旧 `heatSource()` 与 `moltenMetal()` 已在 mode 层保留字段，但现代端尚无对应完整热力/熔融金属 capability；当前只把 `moltenMetal` 映射到 Forge fluid handler。
- 勘误：旧 `TileEntityProxyDyn` 不只是继承 `TileEntityProxyCombo` 开关；它会在 core 实现 `IProxyDelegateProvider` 时按 proxy 位置返回 delegate。现代端已补 `LegacyProxyDelegateProvider` 库钩子，具体化学工厂/大型装配厂的 delegate 行为留给对应 BlockEntity 迁移切片。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 dummy 形状回溯 core 接口化

触发来源：

- 复核 1.7.10 `BlockDummyable` 后确认，旧版详细 `bounding` 会通过 `findCore(...)` 从任意 dummy 回到 core，再按 core 朝向参与碰撞、射线命中和高亮。
- 现代端此前 `DummyBlock` 只对 `LegacyVisibleMultiblockMachineBlock` 特判转发详细 shape；真实 BlockEntity 机器如组装机、流体固化机、电池座不在这条类型判断内，dummy 会退回满方块。

本轮现代侧补齐：

- `MultiblockCoreBlock` 从纯 marker 扩展为多方块形状接口：
  - `getMultiblockShape(...)`
  - `getMultiblockCollisionShape(...)`
  - 默认仍返回单个满方块，保持未接入机器行为不变。
- `DummyBlock` 的选择盒/碰撞盒不再判断具体 core 类，而是统一通过 `MultiblockCoreBlock` 读取整机 shape，并平移到当前 dummy 的局部坐标。
- `LegacyVisibleMultiblockMachineBlock`、`AssemblyMachineBlock`、`MachineBatterySocketBlock`、`LiquefactorBlock` 已接入该接口。

效果：

- 多方块库现在有统一的 core -> dummy shape 回溯入口，后续新机器只要实现/继承 `MultiblockCoreBlock` 并提供整机 shape，dummy 侧无需再增加特判。
- 这更接近旧 `BlockDummyable.addCollisionBoxesToList(...)` / `collisionRayTrace(...)` 的“从 dummy 找 core 再用 core 几何”的契约。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 dummy 破坏/硬度/拾取回溯 core

触发来源：

- 用户要求继续推进多方块库移植；本轮继续核对 1.7.10 `BlockDummyable` 的通用底层契约，而不是新增单机占位。
- 旧版 `BlockDummyable` 中 dummy 与 core 使用同一个方块 ID/物品，`findCore(...)` 是所有 dummy 回溯 core 的入口；玩家破坏 dummy 时由 `onBlockHarvested(...)` 掉落一个机器方块物品，创造模式不掉落，随后 `breakBlock(...)` 沿 dummy 指向链清理到 core。

问题定位：

- 现代端 dummy 是独立 `hbm:dummy_block`，此前 `DummyBlock#onRemove` 无论移除来源都会 `destroyBlock(core, true)`。
- 这会偏离旧版行为：创造模式破坏 dummy 或非玩家移除 dummy 时也可能让 core 掉落机器物品；同时 dummy 的破坏速度和中键拾取没有统一按 core 回溯。

本轮现代侧补齐：

- `MultiblockDummyBlockEntity` 新增瞬时 `dropCoreOnRemoval` 标记与 `destroyCore(boolean drop)`，不写入 NBT，只用于本次玩家破坏事件。
- `DummyBlock#playerWillDestroy(...)` 根据玩家是否创造模式设置是否允许 core 掉落：
  - 生存/非创造玩家破坏 dummy：销毁 core 并掉落 core 机器物品与 core 方块实体内容。
  - 创造模式破坏 dummy：销毁 core 与其 dummy 结构，但不掉落 core 机器物品。
  - 非玩家来源移除 dummy：默认不掉落 core，避免爆炸/结构清理等路径凭空吐出整机。
- `DummyBlock#getDestroyProgress(...)` 现在回溯 core state 计算破坏速度，让 dummy 区域的挖掘手感按真正机器硬度/工具规则走。
- `DummyBlock#getCloneItemStack(...)` 现在回溯 core block，避免中键拾取拿到不可见 dummy 方块或空物品。
- `DummyBlock` 内部提取 `findCore(...)` 小 helper，形状、硬度与拾取共享同一 core 查找路径，并保持 chunk 未加载时的保守 fallback。

效果：

- 现代 dummy 的掉落/破坏/拾取更接近旧 `BlockDummyable` 的“dummy 只是整机的一部分，物品语义属于 core/原机器”的契约。
- 这为后续更多真实 BlockEntity 多方块机器接入菜单、库存、能量/流体代理时减少底层行为分叉。

验证：

- 首次 `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks` 发现 Forge 1.20.1 的 `BlockState#getCloneItemStack` 签名与 vanilla `Block` 签名不同，已改为调用 core block 的 `getCloneItemStack(...)`。
- 修正后 `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks` 通过。

## 2026-05-25 legacy extra dummy 身份补齐

触发来源：

- 继续核对 1.7.10 `BlockDummyable` 后确认，旧版 metadata `6-11` 是普通 dummy 的 `extra` 变体。
- 旧 `makeExtra(...)` / `removeExtra(...)` 不只是“额外占位”，还会让 `createNewTileEntity(...)` 走 `TileEntityProxyCombo`、门开闭状态、RBMK 顶部代理、涡扇连接点等分支。
- 现代端此前只保存 `Proxy` / typed proxy mode，没有单独保存“这是旧 extra metadata”的身份；后续移植运行时结构切换时会缺少 `hasExtra(meta)` 的等价判断点。

本轮现代侧补齐：

- `MultiblockDummyBlockEntity` 新增 `LegacyExtra` NBT 字段与 `isLegacyExtra()` / `setLegacyExtra(...)`。
- `LegacyMultiblockLayout` 新增 legacy extra offset 记录：
  - proxy offset 自动视为 legacy extra，对齐旧版 `meta >= extra` 才生成 proxy tile 的约定。
  - 保留 `withLegacyExtraOffsets(...)`，用于后续表达“extra 但不是 capability proxy”的旧结构状态。
- `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock` 的填充路径会把 `layout::isLegacyExtraOffset` 写入 dummy。
- `MultiblockHelper` 新增运行时 API：
  - `makeLegacyExtra(level, pos, proxyMode)`
  - `removeLegacyExtra(level, pos[, restoredProxyMode])`
  - `isLegacyExtra(level, pos)`

效果：

- 多方块库现在保留了旧 `BlockDummyable` metadata `6-11` 的语义层，而不是只用 Forge capability proxy 近似表达。
- 后续迁移涡扇重构、门开闭、RBMK 顶部/侧边代理、发射台和其他动态 dummy 升级逻辑时，可以通过库级 API 对齐旧 `makeExtra/removeExtra/hasExtra`。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 layout 级 check/fill/remove 收口

触发来源：

- 继续推进多方块库时复查现代端 `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock`，发现两条基类各自手写了 layout 空间检查、dummy 填充和 core 移除清理。
- 1.7.10 `BlockDummyable`/`MultiblockHandlerXR` 的核心契约是“结构定义决定 check/fill/remove”，不是每个现代基类各自复制一套行为；后续继续迁入新多方块 core 时，重复逻辑容易导致 dummy 漏填、proxy mode 漏写或 core 拆除后外围残留。

本轮现代侧补齐：

- `MultiblockHelper` 新增 layout 级公共入口：
  - `checkLayout(level, corePos, layout, temporaryPos)`：统一使用 `layout.checkOffsets()`，包含“只检查不填充”的旧额外净空。
  - `fillLayout(level, corePos, layout)`：统一写入 dummy 的 core 坐标、typed proxy mode 和 legacy extra 身份。
  - `removeLayout(level, corePos, layout)`：统一按 layout 清理 dummy，并继续走现有 clearing guard。
- `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock` 改为调用上述公共入口。
- `LegacyOffsetMultiblockBlock` 的直接放置空间检查从 `layout.offsets()` 改为 `layout.checkOffsets()`；未来 offset 型结构如果带旧 `checkRequirement` 额外净空，不会只检查实际填充位。

对 1.7.10 的对齐说明：

- 旧版 XR 机器在 `checkRequirement(...)` 与 `fillSpace(...)` 中经常既有真实 dummy 填充，也有额外“只检查不填充”的占位区域；现代 `LegacyMultiblockLayout` 已记录这两类 offset，本轮把消费入口收束到库层。
- 这一步不改变已有机器足迹，只减少后续机器移植时复制底层结构操作的机会。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-26 proxy 默认模式与 HBM 网络连接桥接修正

触发来源：

- 继续推进多方块库移植时复核上一轮 proxy typed 迁移，发现现代端虽然已经保存 `LegacyProxyMode`，但若机器或旧 helper 只使用布尔 proxy 入口，仍会默认落到 `LegacyProxyMode.all()`，这比 1.7.10 `TileEntityProxyCombo` 更宽。
- 现代 HBM 能量/流体网络的连接判断还依赖 `HbmEnergyConnector` / `HbmFluidConnector`，不只看 Forge capability；dummy 只转发 capability 会导致相邻电缆/管道无法把 proxy dummy 识别为旧端口连接器。

1.7.10 事实来源：

- `TileEntityProxyCombo` 实现 `IEnergyReceiverMK2`、`IEnergyConductorMK2`、`IFluidReceiverMK2`、`ISidedInventory` 等接口，但每条接口都先检查独立开关：
  - `power()` 控制能量接收与 `IEnergyConnectorMK2#canConnect(...)`。
  - `conductor()` 只在 core/delegate 是 `IEnergyConductorMK2` 时参与 `canConnect(...)`。
  - `fluid()` 控制流体 tank/demand/transfer 与 `IFluidConnectorMK2#canConnect(...)`。
  - `inventory()` 控制 sided inventory 访问。
- `new TileEntityProxyCombo(true, true, true)` 只等价 inventory + power + fluid，并不是任意能力全透传。

本轮现代侧修正：

- `LegacyProxyMode` 新增 `fullCombo()`，表达旧构造器 `TileEntityProxyCombo(true,true,true)`。
- `LegacyMultiblockLayout`、`MultiblockHelper` 和 `MultiblockDummyBlockEntity#setProxy(true)` 的布尔 proxy 默认值从 `all()` 收窄为 `fullCombo()`；显式 `all()` 仅用于旧 NBT 没有细分字段时的兼容读取，或后续确有来源证明需要全 capability 透传的特殊路径。
- `MultiblockDummyBlockEntity` 现在实现：
  - `HbmEnergyConnector#canConnectEnergy(...)`：只有 `power` / `conductor` / `allCapabilities` proxy 才响应，并优先转发到 core/delegate 的 `HbmEnergyConnector`。
  - `HbmFluidConnector#canConnectFluid(...)`：只有 `fluid` / `moltenMetal` / `allCapabilities` proxy 才响应，并优先转发到 core/delegate 的 `HbmFluidConnector`。
- 两条连接桥接都复用已有 `validCore()` 与 `LegacyProxyDelegateProvider`，保持归属校验和旧 `TileEntityProxyDyn` delegate 语义一致。

效果：

- 后续多方块机器接入真实能量/流体网络时，外部电缆和管道可以从 dummy 端口按旧 proxy 开关识别连接，不需要在每台机器旁边单独加特判。
- 布尔 proxy helper 不再意外开放 heat、moltenMetal 或未知 Forge capability，减少后续机器逻辑接入时的错误访问面。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-25 1.7.10 对齐审计：方向与离心机 proxy 勘误

审计触发：

- 用户要求进行 1.7.10 对齐检查，重点清理未对齐且存在猜测内容的部分。
- 本轮先从多方块库文档中带“暂按/可能/约定”的位置抽查，优先核对会影响 proxy/extra offset 的方向旋转和早期可见机器 proxy 标记。

1.7.10 事实来源：

- 本机 Forge 1.7.10 jar：`net.minecraftforge.common.util.ForgeDirection#getRotation(...)`。
- `com.hbm.blocks.machine.MachineCentrifuge`。
- `com.hbm.handler.MultiblockHandlerXR`。
- `com.hbm.tileentity.TileEntityProxyDyn`。
- `com.hbm.tileentity.machine.TileEntityMachineChemicalFactory`。
- `com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory`。

审计结论：

- 已用 `jshell` 加载 Forge `1.7.10-10.13.4.1614` 直接验证：
  - `NORTH.getRotation(UP)=EAST`、`EAST.getRotation(UP)=SOUTH`、`SOUTH.getRotation(UP)=WEST`、`WEST.getRotation(UP)=NORTH`，等价现代 `Direction#getClockWise()`。
  - `NORTH.getRotation(DOWN)=WEST`、`EAST.getRotation(DOWN)=NORTH`、`SOUTH.getRotation(DOWN)=EAST`、`WEST.getRotation(DOWN)=SOUTH`，等价现代 `Direction#getCounterClockWise()`。
- 旧 `MachineCentrifuge#createNewTileEntity(...)` 虽然对 `meta >= 6` 返回 `TileEntityProxyCombo(false,true,true)`，但 `MachineCentrifuge#fillSpace(...)` 没有 `makeExtra(...)`，只走 `MultiblockHandlerXR.fillSpace(...)`；因此实际放置的上方结构是普通 metadata `UP` dummy，不会生成 proxy tile。
- 旧 `TileEntityProxyDyn#getCoreObject()` 会先取得 core，再在 core 实现 `IProxyDelegateProvider` 时调用 `getDelegateForPosition(x,y,z)`；化学工厂和大型装配厂用这个 delegate 把 coolant line proxy 的流体/能量访问限制到水和低压蒸汽侧。

本轮现代侧修正：

- `machine_centrifuge` 的 `LegacyMachineDefinition.layout(...)` 移除 `offset.getY() > 0` 的 proxy 猜测标记，回到纯 XR 普通 dummy 足迹。
- 文档中原“上方 dummy 使用 proxy”的记录改为勘误说明，避免后续以该错误为基础继续扩展 capability/流体网络接入。
- 新增 `LegacyProxyDelegateProvider`，`MultiblockDummyBlockEntity#getCapability(...)` 在 core 提供 delegate 时把 capability 请求转给 delegate，否则保持转发到 core；这补回旧 `TileEntityProxyDyn` 的库级合同，但不凭空实现尚未迁移的具体机器 delegate。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-25 dummy 归属校验收紧

触发来源：

- 继续推进多方块库时复核旧 `BlockDummyable#findCoreRec(...)` / `destroyIfOrphan(...)`：旧 dummy 不是只保存一个 core 坐标，而是通过自身 metadata 指向相邻同方块并最终回到 core；链路断裂、方向不匹配或旁边不是同一方块时会视为孤儿。
- 现代端 dummy 直接保存 `CorePos`，此前只要 core 仍是 `MultiblockCoreBlock`，shape/右键/capability/破坏回溯就会继续生效；如果旧结构残留、朝向变化或错误填充留下了不属于当前 layout 的 dummy，现代端会比旧版更宽。

本轮现代侧补齐：

- `MultiblockCoreBlock` 新增 `ownsMultiblockDummy(state, level, corePos, dummyPos)`，作为 core 判断某个 dummy 是否属于自己结构的库级入口。
- `LegacyXrMultiblockBlock` 与 `LegacyOffsetMultiblockBlock` 默认用自身 `LegacyMultiblockLayout.offsets()` 校验 `dummyPos - corePos`。
- `MultiblockHelper.findCore(...)` 从 dummy 解析 core 时增加归属校验；不属于该 layout 的 dummy 不再被当作有效 core 成员。
- `MultiblockDummyBlockEntity.serverTick(...)` 在 core 区块已加载时也用同一归属校验，失败则移除 dummy。

对 1.7.10 的对齐说明：

- 这不是复刻旧 metadata 指向链，而是补回“dummy 必须能被当前 core 的结构定义承认”的功能合同。
- core 区块未加载时仍保持前几轮的保守等待策略，避免跨区块加载顺序导致合法 dummy 被误删。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-25 dummy 转发路径归属校验收口

触发来源：

- 上一轮已让 `MultiblockHelper.findCore(...)` 与 dummy tick 使用 `ownsMultiblockDummy(...)`，但 `MultiblockDummyBlockEntity` 内部仍有三条直接读取 `CorePos` 的路径：右键转发、破坏 core、capability proxy。
- 旧 `BlockDummyable` 的这些行为都依赖 `findCore(...)`；如果 metadata 链不成立，就不会把 dummy 当成有效 core 成员。

本轮现代侧补齐：

- `MultiblockDummyBlockEntity` 新增私有 `validCore()`，统一执行：
  - corePos 存在且不是自身；
  - core 区块已加载；
  - 目标仍是 `MultiblockCoreBlock`；
  - `MultiblockCoreBlock#ownsMultiblockDummy(...)` 承认当前 dummy 坐标。
- `forwardUse(...)`、`destroyCore(...)`、`getCapability(...)` 全部改用 `validCore()`。

对 1.7.10 的对齐说明：

- 这样 dummy 的右键、破坏联动和 proxy 能力转发与 shape/硬度/拾取一样，全部收束到同一套“有效 core 成员”判断。
- 不属于当前 layout 的残留 dummy 即使还保存旧 `CorePos`，也不会继续打开 GUI、转发 capability 或破坏 core。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。

## 2026-05-26 多方块碰撞高度重大修复

问题现象：

- 组装机中间可直接走过去，等价于旧 XR dummy 的完整方块碰撞没有生效。
- 多数可见大机器只剩底下一层碰撞，占位高度没有按旧 `getDimensions()` 的 `U/D` 层数扩展。

1.7.10 事实来源：

- `BlockDummyable#fillSpace(...)` 调用 `MultiblockHandlerXR.fillSpace(...)`。
- `MultiblockHandlerXR.fillSpace(...)` 先 `rotate(dim, dir)`，再按 `b = y - D .. y + U` 逐层放置 dummy；因此 `{U,D,N,S,W,E}` 中 `U` 明确代表向上填充层数。
- `MachineAssemblyMachine#getDimensions() = {2,0,1,1,1,1}`，且没有自定义 `bounding`；旧版实际是 3x3x3 范围内每个 core/dummy 方块都提供默认完整方块碰撞。

现代端问题定位：

- `LegacyMultiblockLayout#shape(double height)` 只统计 X/Z 足迹，并固定从 `0..height` 生成 shape；`height=1.0` 时所有走 `stateLayoutShape(...)` 或默认 `LegacyMachineDefinition.collisionShape(...)` 的机器都会变成一层高。
- `AssemblyMachineBlock` 额外手写了 `-1.5..2.5`、高度 `0..2` 的大盒，既不是旧 3x3 足迹，也少了 `U=2` 对应的第三层。

本轮现代侧修复：

- `LegacyMultiblockLayout#shape(double height)` 改为同时统计 offset 的 Y 范围，生成 `minY .. max(offsetY + height)` 的整机占位 shape。
- `AssemblyMachineBlock#getMultiblockShape(...)` / `getMultiblockCollisionShape(...)` 改回使用 `getLayout(state).shape(1.0D)`，由 1.7.10 XR dimensions 直接决定碰撞范围。

对 1.7.10 的对齐说明：

- 没有 `bounding` 详细盒的旧 `BlockDummyable` 机器，默认碰撞应来自实际填充的 core/dummy 方块集合；现代端用 layout 的三维边界近似这组完整方块碰撞。
- 已有详细 `bounding` 的机器仍使用各自迁入的 `legacyRotatedShape(...)`，不受本轮默认 shape 修正影响。

验证：

- 已跑：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`，通过。
