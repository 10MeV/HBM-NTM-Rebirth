# 传送带与物流接口 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 传送带、包裹、可进入方块、物品物流接口。
- 该库后续服务传送带、物流机器、掉落物移动、特殊包裹。

## 1.7.10 源文件

- `src/main/java/api/hbm/conveyor/IConveyorBelt.java`
- `src/main/java/api/hbm/conveyor/IConveyorItem.java`
- `src/main/java/api/hbm/conveyor/IConveyorPackage.java`
- `src/main/java/api/hbm/conveyor/IEnterableBlock.java`

## 旧版契约

- `IConveyorBelt`：
  - `canItemStay(World, x, y, z, Vec3 itemPos)` 判断物品是否仍属于当前传送带。
  - `getTravelLocation(...)` 返回按速度移动后的目标位置。
  - `getClosestSnappingPosition(...)` 返回吸附位置。
- `IConveyorItem`：
  - 由可被传送带特殊处理的物品实现。
- `IConveyorPackage`：
  - 表示物流包裹数据和行为。
- `IEnterableBlock`：
  - 表示实体/物品可进入的特殊方块，如物流入口或机器口。

## 迁移计划

- 先迁移接口和数学语义，不急于迁具体传送带方块。
- 现代端应使用 `Vec3`、`BlockPos`、`Level` 替代旧坐标参数。
- 传送带物品移动要注意服务器权威与客户端插值显示分离。

## 验证清单

- 物品能被吸附到传送带中心线。
- 相邻传送带转角不会丢失物品。
- 特殊包裹与普通 ItemEntity 分开处理。
