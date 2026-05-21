# 传送带与物流接口 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 传送带、包裹、可进入方块、物品物流接口。
- 该库后续服务传送带、物流机器、掉落物移动、特殊包裹。

## 1.7.10 源文件

- `src/main/java/api/hbm/conveyor/IConveyorBelt.java`
- `src/main/java/api/hbm/conveyor/IConveyorItem.java`
- `src/main/java/api/hbm/conveyor/IConveyorPackage.java`
- `src/main/java/api/hbm/conveyor/IEnterableBlock.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyorBase.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyorBendable.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyor.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyorExpress.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyorDouble.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyorTriple.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyorLift.java`
- `src/main/java/com/hbm/blocks/network/BlockConveyorChute.java`
- `src/main/java/com/hbm/entity/item/EntityMovingConveyorObject.java`
- `src/main/java/com/hbm/entity/item/EntityMovingItem.java`
- `src/main/java/com/hbm/entity/item/EntityMovingPackage.java`
- `src/main/java/com/hbm/items/tool/ItemConveyorWand.java`

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

## 旧版行为要点

- `BlockConveyorBase`：
  - `canItemStay` 恒为 `true`。
  - 移动算法为“先求吸附点，再以 `speed` 朝 `snap - dir * speed` 归一化推进”。
  - 单轨吸附 X/Z 限制在方块内，横向运动保留行进轴坐标，另一轴吸到中心线，Y 为 `y + 0.25`。
  - 原版 `EntityItem` 在传送带碰撞且 `ticksExisted > 10` 后转成 `EntityMovingItem`。
- `BlockConveyorBendable`：
  - metadata `2-5` 为直线，`6-9` 为左弯，`10-13` 为右弯。
  - 输入方向为 `metadata - path * 4`，输出方向为输入反向再按弯道旋转。
  - 物品是否进入 secondary 方向由内角点曼哈顿距离 `abs(dx)+abs(dz) >= 1` 决定。
- `BlockConveyorExpress`：
  - 仅把基础移动速度乘以 `3`。
- `BlockConveyorDouble` / `BlockConveyorTriple`：
  - 双轨侧向偏移为 `0.25`。
  - 三轨侧向偏移为 `0.3125`，中心死区为 `0.15`。
- `BlockConveyorChute`：
  - 有下方传送带/可进入方块，或物品高于 `y + 0.25` 时走垂直下落吸附，速度倍率分别有旧版特殊处理。
  - 输入方向为 `UP`，输出方向为 `DOWN`。
- `BlockConveyorLift`：
  - 输入方向为 `DOWN`，输出方向为 `UP`。
  - 中段沿 Y 轴吸附，顶部才回到水平传送带逻辑。
- `EntityMovingConveyorObject` / `EntityMovingItem`：
  - 默认速度 `0.0625`。
  - 服务端 `ticksExisted > 5` 后检查当前方块是否为 `IConveyorBelt`，否则离开传送带。
  - 跨入 `IEnterableBlock` 时按移动方向计算进入 side；非实心方块下方可进入时按 `UP` 进入。
  - `EntityMovingItem` 离开传送带后生成 vanilla `EntityItem`，并把水平速度放大。
- `ItemConveyorWand`：
  - 四种 subtype：`REGULAR` / `EXPRESS` / `DOUBLE` / `TRIPLE`。
  - 只有 `REGULAR` 支持 lift/chute 垂直“snakes and ladders”。
  - 两点构造算法按 taxi distance 逐格放置，遇阻或距离不缩短时转向，可生成弯道 metadata 或 lift/chute。
  - 创造模式 shift 破坏可沿输入/输出方向递归破坏整条传送带，深度上限 `32`。

## 迁移计划

- 先迁移接口和数学语义，不急于迁具体传送带方块。
- 现代端应使用 `Vec3`、`BlockPos`、`Level` 替代旧坐标参数。
- 传送带物品移动要注意服务器权威与客户端插值显示分离。

## 现代端进度

### 第 1 轮：接口与数学语义

- 新增现代接口：
  - `src/main/java/com/hbm/ntm/api/conveyor/IConveyorBelt.java`
  - `src/main/java/com/hbm/ntm/api/conveyor/IConveyorItem.java`
  - `src/main/java/com/hbm/ntm/api/conveyor/IConveyorPackage.java`
  - `src/main/java/com/hbm/ntm/api/conveyor/IEnterableBlock.java`
- 现代接口对齐旧契约：
  - `World + x/y/z` 改为 `Level + BlockPos`。
  - `ForgeDirection` 改为 `Direction`。
  - `ItemStack` 使用现代 `net.minecraft.world.item.ItemStack`。
- 新增 `ConveyorPathType`：
  - 保留旧 metadata 分段语义：`2-5` 为直线，`6-9` 为左弯，`10-13` 为右弯。
- 新增 `ConveyorMath`：
  - `legacyMetadataForPlacementYaw(...)` 对齐旧 `BlockConveyorBase.onBlockPlacedBy(...)`，将玩家朝向映射为旧 `2/5/3/4` 水平 metadata。
  - `inputDirection(...)` / `outputDirection(...)` 对齐旧 `BlockConveyorBase` 与 `BlockConveyorBendable`。
  - `travelDirection(...)` 复刻旧弯道判定：根据物品相对弯道内角距离决定是否转入 secondary 方向。
  - `closestSnappingPosition(...)` 对齐旧单轨传送带中心线吸附，Y 固定为 `pos.y + 0.25`。
  - `closestDoubleLaneSnappingPosition(...)` 对齐旧双轨传送带 `±0.25` 侧向吸附。
  - `closestTripleLaneSnappingPosition(...)` 对齐旧三轨传送带 `±0.3125` 与中轨阈值 `0.15`。
  - `travelLocation(...)` 复刻旧 `BlockConveyorBase.getTravelLocation(...)` 的“先吸附，再按速度推进”算法，并补零长度保护，避免现代端出现 NaN。
  - `expressTravelLocation(...)` 对齐旧 `BlockConveyorExpress` 的 `speed * 3`。

### 第 2 轮：路线规划 API

- 扩展 `src/main/java/com/hbm/ntm/api/conveyor/ConveyorMath.java`：
  - 集中记录基础速度 `0.0625`、传送带高度 `0.25`、双轨/三轨偏移常量。
  - 对齐旧单轨、双轨、三轨、弯道、lift、chute 的移动/吸附辅助逻辑。
- 新增 `src/main/java/com/hbm/ntm/api/conveyor/ConveyorRoutePlanner.java`：
  - 将旧 `ItemConveyorWand.construct(...)` 的两点路径规划抽成纯 API。
  - 保留四种旧手杖类型与“只有 REGULAR 支持垂直传送带”的规则。
  - 保留旧放置 yaw 到 metadata 的 `2/5/3/4` 映射。
  - 保留旧路线失败语义：成功、阻塞、传送带数量不足。
  - 输出 `Placement(pos, kind, legacyMetadata)`，后续现代 `ItemConveyorWand` 可据此真正 setBlock。
- 清理重复风险：
  - 不再使用临时 `com.hbm.ntm.conveyor` 包，统一归入 `com.hbm.ntm.api.conveyor`，避免后续机器接错接口。

### 第 3 轮：移动实体与进入回调

- 扩展 `src/main/java/com/hbm/ntm/api/conveyor/ConveyorMath.java`：
  - `chuteTravelDirection(...)` / `chuteSnappingPosition(...)` / `chuteTravelLocation(...)` 对齐旧 `BlockConveyorChute` 的垂直下落与速度倍率。
  - `liftTravelDirection(...)` / `liftSnappingPosition(...)` / `liftTravelLocation(...)` 对齐旧 `BlockConveyorLift` 的中段竖直移动与顶部水平输出。
  - `isConveyor(...)` / `isEnterable(...)` / `isConveyorOrEnterable(...)` / `entryDirection(...)` 抽出旧 `EntityMovingConveyorObject` 的邻接检测和进入侧计算。
- 现代移动实体：
  - `src/main/java/com/hbm/ntm/entity/item/MovingConveyorObjectEntity.java`：服务端 5 tick 后按 `IConveyorBelt` 移动，跨格时尝试进入 `IEnterableBlock`，速度改为使用 `ConveyorMath.baseSpeed()`。
  - `src/main/java/com/hbm/ntm/entity/item/MovingItemEntity.java`：已有普通移动物品实体，对齐旧 `EntityMovingItem` 的栈同步、玩家拾取、受击/离带掉落和进入回调。
  - `src/main/java/com/hbm/ntm/entity/item/MovingPackageEntity.java`：新增包裹实体，对齐旧 `EntityMovingPackage` 的多栈 NBT、玩家拆包、受击/离带散落和包裹进入回调。
  - `src/main/java/com/hbm/ntm/registry/ModEntityTypes.java`：除 `entity_c_item` 外新增 `entity_c_package`。
  - `src/main/java/com/hbm/ntm/client/ClientModEvents.java`：注册 moving item 与 moving package renderer；包裹 renderer 目前为占位入口，后续补模型/贴图。

### 第 4 轮：传送带方块最小闭环

- 新增/接入 `src/main/java/com/hbm/ntm/block/conveyor/`：
  - `ConveyorBlock`
  - `ExpressConveyorBlock`
  - `DoubleConveyorBlock`
  - `TripleConveyorBlock`
  - `LiftConveyorBlock`
  - `ChuteConveyorBlock`
- 现代 `ConveyorBlock`：
  - 现代 BlockState 使用 `facing + path` 表达旧 metadata：`2-5` 直线、`6-9` 左弯、`10-13` 右弯。
  - 额外保留 `legacy_metadata`，方便后续手杖和旧路线规划直接落块。
  - 对齐旧 `BlockConveyorBase` 的 1/4 高度碰撞和非完整遮挡；lift/chute 目前使用全高/顶部半高形状。
  - `entityInside(...)` 对齐旧 `onEntityCollidedWithBlock(...)`：服务端 vanilla `ItemEntity` 存活超过 10 tick 后转成 `MovingItemEntity`，生成位置先吸附到传送带。
  - 空手右键临时接入 `Toolable.SCREWDRIVER` 旋转语义，保留旧 `onScrew` 的旋转/切换 path 行为入口。
- 注册旧传送带 ID：
  - `conveyor`
  - `conveyor_express`
  - `conveyor_double`
  - `conveyor_triple`
  - `conveyor_lift`
  - `conveyor_chute`
- 移动逻辑：
  - 普通、加速、双轨、三轨接入 `ConveyorMath` 的旧算法。
  - lift/chute 接入已迁移的 `liftTravelLocation(...)` / `chuteTravelLocation(...)`。
- 搬运旧资源：
  - `textures/blocks/conveyor*.png(.mcmeta)` 改放到现代 `textures/block/`。
  - 新增对应 blockstate、block model、item model JSON。
- 对齐旧版限制：
  - 旧 `ModBlocks` 中这些方块 `setCreativeTab(null)`，现代端同样不放入创造栏；后续由 `ItemConveyorWand` 提供获取/放置入口。
  - 旧方块掉落 `ModItems.conveyor_wand` 的 damage subtype；现代端手杖尚未迁移，本轮暂时提供自掉落 loot 以保证新方块可验证，迁手杖时再对齐为 wand subtype。
- 本轮补充资源/数据：
  - 静态 blockstate/model/item model 让六种传送带无需 datagen 即可显示。
  - `HbmBlockStateProvider` 可按 `facing/path` 生成方向/弯道模型，`legacy_metadata` 仍只服务逻辑。
  - 新增六个 loot table 和 pickaxe/iron tool tag；注册侧新增 `CONVEYOR_BLOCKS`，但不加入机器创造栏。
- 本轮没有迁移：
  - `ItemConveyorWand` 物品注册、NBT 两点选择、预览渲染、实际 setBlock、掉落、damage/meta 与 JEI 隐藏语义。
  - `IEnterableBlock` 的具体物流入口、crane、router、boxer/unboxer 行为。
  - `RenderConveyor` 的 UV 旋转精确复刻，以及 `RenderConveyorChute` / `RenderConveyorLift`。

### 第 5 轮：ConveyorWand 最小闭环

- 新增 `src/main/java/com/hbm/ntm/item/ConveyorWandItem.java`：
  - 现代端用单个 `conveyor_wand` item 保留旧四种 subtype，类型存入 NBT `Type=REGULAR/EXPRESS/DOUBLE/TRIPLE`。
  - 机器创造栏输出四个 NBT 变体栈，对齐旧 `MainRegistry.machineTab`；Forge 1.20.1 创造栏要求输出栈数量为 `1`，堆叠上限仍由物品 `stacksTo(64)` 控制。
  - 名称 key 使用 `item.hbm.conveyor_wand.<type>`，保留旧四种显示名语义。
- 两点铺设：
  - 第一次右键保存 `Start`、`Side`、`Count`。
  - 第二次右键调用 `ConveyorRoutePlanner.plan(...)`，将 `Placement(pos, kind, legacyMetadata)` 真正转换为现代方块状态。
  - 非创造模式按旧版语义消耗玩家背包中同 `Type` 的 `conveyor_wand` 数量。
  - 创造模式最大铺设数沿用 `ConveyorRoutePlanner.CREATIVE_MAX_CONVEYORS = 256`。
- 单格手动放置：
  - 蹲下右键且未选择起点时放置单个对应传送带。
  - `REGULAR` 支持按 UP/DOWN 生成 `conveyor_lift` / `conveyor_chute`，其他类型仍为水平传送带。
- 掉落对齐：
  - 六种传送带 loot 改为掉落 `conveyor_wand`，并通过 `minecraft:set_nbt` 写入对应 `Type`。
  - `conveyor_lift` / `conveyor_chute` 掉落 `REGULAR`，对齐旧版垂直传送带属于普通手杖的语义。
- 资源与数据：
  - 从 1.7.10 `textures/items/wand_s.png` 复制为现代 `textures/item/conveyor_wand.png`。
  - 新增 `models/item/conveyor_wand.json`。
  - 更新 EN/中文 datagen 文本和静态资源处理。
- 本轮仍未迁移：
  - 客户端路线预览：旧版依赖 `WorldInAJar` 与 `RenderOverhead`，需要后续迁渲染/预览库。
  - 创造模式 shift 破坏整条传送带链：旧版依赖 `onBlockStartBreak`，现代 Forge 事件接入尚未迁。
  - 旧螺丝刀正式物品/工具系统；目前传送带方块保留 `Toolable` 入口。

## 下一步建议

1. 迁移客户端路线预览库，替代旧 `WorldInAJar` / `RenderOverhead`，让 `ConveyorWandItem` 能显示规划结果。
2. 接入现代工具系统，恢复正式螺丝刀旋转、弯道切换，以及创造模式 shift 破坏整条传送带链。
3. 精修传送带现代模型：复核 `RenderConveyor` 的顶面/侧面 UV 旋转、弯道纹理方向、lift/chute 形状和物品模型显示。
4. 接 crane/router/boxer/unboxer 等 `IEnterableBlock` 消费方，并补 `conveyor_wand` 与物流机器配方。

## 验证清单

- 物品能被吸附到传送带中心线。
- 相邻传送带转角不会丢失物品。
- 特殊包裹与普通 ItemEntity 分开处理。
- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon`：2026-05-21 通过。
- `.\gradlew.bat compileJava processResources --no-daemon`：2026-05-21 通过，ConveyorWand 最小闭环接线后。
