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

## 暂缓内容

- `MultiblockHandlerXR` 的 `{U,D,N,S,W,E}` 结构旋转和 `BlockDummyable` 同块 core/proxy 模式未在本切片迁入。
- dummy port 的能量/流体连接语义未接入；后续应与 `energy-mk2-network`、`fluid-mk2-network` 文档和能力分发规则一起迁移。
- 具体机器尚未改为实现 `MultiblockCoreBlock` 或调用 `MultiblockHelper`，需要在机器迁移切片逐个接入。

## 验证清单

- 四向放置时 dummy 区域与旧版一致。
- 拆除主方块会清理 dummy。
- 点击 dummy 能转发到主方块。
- 存档重载后 dummy 仍能找到主方块。
