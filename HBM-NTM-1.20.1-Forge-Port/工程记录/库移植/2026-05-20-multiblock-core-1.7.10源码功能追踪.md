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

## 验证清单

- 四向放置时 dummy 区域与旧版一致。
- 拆除主方块会清理 dummy。
- 点击 dummy 能转发到主方块。
- 存档重载后 dummy 仍能找到主方块。
