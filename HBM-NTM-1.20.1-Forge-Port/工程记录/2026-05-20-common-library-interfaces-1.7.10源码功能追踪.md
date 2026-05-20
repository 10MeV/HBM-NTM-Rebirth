# 通用库与接口 1.7.10 源码功能追踪

## 范围

- 本文记录 1.7.10 中可被多种机制复用的基础 API、通用接口、工具类和数据结构。
- 已有独立文档的渲染库、辐射核心不在本文展开。
- 本文只做迁移前事实记录，不直接迁移 Java 逻辑。

## 1.7.10 源文件

- `src/main/java/com/hbm/lib/Library.java`
- `src/main/java/com/hbm/lib/HbmWorld.java`
- `src/main/java/com/hbm/lib/HbmWorldGen.java`
- `src/main/java/com/hbm/lib/HbmCollection.java`
- `src/main/java/com/hbm/lib/HbmChestContents.java`
- `src/main/java/com/hbm/lib/ModDamageSource.java`
- `src/main/java/com/hbm/interfaces`
- `src/main/java/api/hbm/block`
- `src/main/java/api/hbm/entity`
- `src/main/java/api/hbm/item`
- `src/main/java/api/hbm/tile`
- `src/main/java/com/hbm/util`

## 旧版契约

- `Library` 是旧版杂项中心：
  - 能量连接判断：`canConnect(...)`，识别 `IEnergyConnectorBlock` 与 `IEnergyConnectorMK2`。
  - 流体连接判断：`canConnectFluid(...)`，识别 `IFluidConnectorBlockMK2` 与 `IFluidConnectorMK2`。
  - 玩家射线：`rayTrace(...)`、`getPosition(...)`。
  - 实体搜索：最近玩家、最近直升机、最近地雷等。
  - 电池/机器充放电辅助：`chargeItemsFromTE(...)`、`chargeTEFromItems(...)`。
- `interfaces` 目录包含内部标记与机制接口：
  - `IMultiblock`、`IDummy`：多方块与 dummy 方块。
  - `IExplosionRay`：爆炸射线。
  - `IItemHUD`、`IHoldableWeapon`：HUD 与持有武器。
  - `IOrderedEnum`、`HalfLifeType`：枚举排序与半衰期语义。
  - `Spaghetti`、`Untested`、`NotableComments` 是开发标记，不应作为现代 API 重点。
- `api/hbm/block` 是跨系统方块行为接口：
  - `IToolable`：工具交互，工具类型含 screwdriver、hand drill、defuser、wrench、torch、bolt。
  - `ILaserable`：接收激光能量。
  - `IInsertable`：可被外部插入。
  - `IDrillInteraction`、`IMiningDrill`：矿钻交互。
  - `IBlowable`、`IFuckingExplode`：爆炸/冲击相关。
  - `IRadioControllable`：无线控制。
  - `IPileNeutronReceiver`：石墨堆中子接收。
- `api/hbm/entity` 是实体能力接口：
  - `IRadarDetectableNT`：雷达扫描等级、可见性、红石输出。
  - `IRadiationImmune`：辐射免疫。
  - `IResistanceProvider`：伤害抗性提供者。
- `api/hbm/item` 是物品能力接口：
  - `IGasMask`：防毒面具。
  - `IGunHUDProvider`：枪械 HUD。
  - `IDesignatorItem`：坐标/目标指示器。
  - `IDepthRockTool`：深层岩石工具。
- `api/hbm/tile` 是方块实体通用接口：
  - `ILoadedTile`：网络中判断 tile 是否仍加载。
  - `IInfoProviderEC`：给 EnergyControl/探针类信息面板提供数据。
  - `IHeatSource`：热源。

## 迁移计划

- 先把现代端通用接口拆成小包：`api.block`、`api.entity`、`api.item`、`api.tile`、`util`。
- 避免整体搬运 `Library`；按用途拆分为 `EnergyConnectionUtil`、`FluidConnectionUtil`、`RayTraceUtil`、`BatteryItemUtil` 等。
- `IToolable`、`IRadarDetectableNT`、`IGasMask`、`IInfoProviderEC` 应优先建立现代接口，因为后续多系统都会引用。
- 开发标记类不迁移，必要时在工程记录中保留注释即可。

## 验证清单

- 工具接口能被方块和方块实体调用。
- 连接判断同时支持 block 与 block entity。
- 雷达、气体面具、信息面板接口不依赖客户端类。
- 编译命令：`./gradlew.bat compileJava processResources --no-daemon`

## 2026-05-20 第一轮现代接口落地

- 新增现代 common API 壳，保留 1.7.10 语义但改用 1.20.1 类型：
  - `com.hbm.ntm.api.block.Toolable`：对应旧 `api.hbm.block.IToolable`，保留 `SCREWDRIVER`、`HAND_DRILL`、`DEFUSER`、`WRENCH`、`TORCH`、`BOLT` 工具类型。
  - `com.hbm.ntm.api.entity.RadarDetectable` / `RadarEntry`：对应旧 `IRadarDetectableNT` / `RadarEntry`，保留雷达 blip 等级、扫描参数、红石输出语义；维度字段改为 `ResourceLocation`。
  - `com.hbm.ntm.api.item.GasMask` / `HazardClass`：对应旧 `IGasMask` 与 `ArmorRegistry.HazardClass`，暂只迁移防护类别契约，不与当前 `radiation.HazardType` 混用。
  - `com.hbm.ntm.api.item.DesignatorItem`：对应旧 `IDesignatorItem`，坐标接口改用 `BlockPos` 与 `Vec3`。
  - `com.hbm.ntm.api.tile.LoadedTile` / `InfoProviderEC` / `HeatSource`：对应旧 `ILoadedTile`、`IInfoProviderEC`、`IHeatSource`。
- 新增 `com.hbm.ntm.util.ConnectionUtil`：
  - 先迁移旧 `Library.canConnect(...)` 的能量连接判断语义。
  - 支持现代 block 接口、block entity 接口，以及 Forge Energy capability。
  - 暂不迁移 fluid MK2 连接判断，因为已有独立 `fluid-mk2-network` 追踪文档，后续应在该库完成类型后接入。
- 新增 `com.hbm.ntm.util.RayTraceUtil`：
  - 对应旧 `Library.rayTrace(...)` / `getPosition(...)`。
  - 使用现代 `ClipContext`、`Player.getViewVector(...)`、`Level.clip(...)`。
- 明确未迁移项：
  - 旧 `ILoadedTile.TileAccessCache` 依赖 1.7.10 `TileEntity` 和旧 `Compat.getTileStandard(...)`，暂不照搬；后续如网络需要缓存，可基于 `BlockEntity` 与 `Level` 重写。
  - 旧 `IGunHUDProvider` 依赖 `IIcon` 与旧 HUD 渲染，等 HUD/overlay 文档推进后再迁。
  - 旧 `Library.chargeItemsFromTE(...)` / `chargeTEFromItems(...)` 已由现代 `HbmEnergyUtil` 的 Forge Energy item 充放电路径承接，本轮不重复实现。
