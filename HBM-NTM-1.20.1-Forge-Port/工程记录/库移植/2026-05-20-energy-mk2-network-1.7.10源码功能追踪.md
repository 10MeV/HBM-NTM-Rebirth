# Energy MK2 网络 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 HE 能量网络 MK2 的接口、网络分配算法、电池物品契约和机器接入点。
- 该库被大量机器、储能方块、电池、防具和网络导线复用，应作为机器迁移前置基座。

## 1.7.10 源文件

- `src/main/java/api/hbm/energymk2/PowerNetMK2.java`
- `src/main/java/api/hbm/energymk2/Nodespace.java`
- `src/main/java/api/hbm/energymk2/IEnergyConnectorMK2.java`
- `src/main/java/api/hbm/energymk2/IEnergyConnectorBlock.java`
- `src/main/java/api/hbm/energymk2/IEnergyHandlerMK2.java`
- `src/main/java/api/hbm/energymk2/IEnergyProviderMK2.java`
- `src/main/java/api/hbm/energymk2/IEnergyReceiverMK2.java`
- `src/main/java/api/hbm/energymk2/IEnergyConductorMK2.java`
- `src/main/java/api/hbm/energymk2/IBatteryItem.java`
- `src/main/java/com/hbm/lib/Library.java`
- `src/main/java/com/hbm/uninos`

## 旧版契约

- 网络类型：`PowerNetMK2 extends NodeNet<IEnergyReceiverMK2, IEnergyProviderMK2, PowerNode>`。
- 订阅者：
  - provider 通过 `IEnergyProviderMK2` 暴露可提供能量、输出速率、扣能方法。
  - receiver 通过 `IEnergyReceiverMK2` 暴露需求、接收速率、优先级、接收后剩余。
  - handler 同时继承 connector 与 `ILoadedTile`，提供 `getPower()`、`setPower()`、`getMaxPower()`。
- 分配算法：
  - 每 tick 汇总 provider 可用能量与 receiver 总需求。
  - receiver 按 `ConnectionPriority` 从高到低分组。
  - 同优先级内按需求权重分配。
  - 实际接收量从 provider 按输出权重扣除。
  - 舍入剩余通过随机 provider 补扣，最多 100 次。
- 失效清理：
  - provider/receiver entry 使用时间戳，默认超时 `3_000` 毫秒。
  - `NodeNet.isBadLink` 会排除未加载 tile 或 invalid tile。
- 调试与外部信息：
  - `IEnergyHandlerMK2.provideInfoForECMK2` 写入 HE 能量和容量。
  - `particleDebug` 常量用于旧版调试粒子。
- 电池：
  - `IBatteryItem` 负责物品存储、充电速率、放电速率。
  - `Library.chargeItemsFromTE` 和 `chargeTEFromItems` 是机器和电池交互入口。

## 迁移计划

- 先迁移接口与单位语义，保留 long 型 HE 数值。
- 用现代 `BlockEntity` + capability/自定义接口承载能量连接，不直接照搬 1.7.10 world 坐标访问。
- `PowerNetMK2` 的优先级分配算法可以复用，但网络发现、生命周期、chunk 卸载处理要改写。
- 电池物品应迁移为现代 ItemStack NBT/DataComponent 或 capability；旧 key 需记录以便存档迁移。
- 与 UNINOS 的关系：如果同时迁移多种网络，先建通用 `NodeNet` 替代层。

## 现代迁移进度

- 2026-05-20 第一批已迁移：
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyHandler.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyProvider.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyReceiver.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyStorage.java`
  - `src/main/java/com/hbm/ntm/energy/ForgeEnergyAdapter.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyUtil.java`
- 2026-05-20 第二批已迁移：
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyBlockEntity.java`
  - `src/main/java/com/hbm/ntm/energy/HbmBatteryItem.java`
  - `src/main/java/com/hbm/ntm/energy/HbmBatteryItemCapabilityProvider.java`
  - `src/main/java/com/hbm/ntm/energy/ForgeBatteryItemAdapter.java`
- 2026-05-20 第三批已迁移：
  - `ForgeEnergyAdapter` 增加 `canReceive`/`canExtract` 方向性开关。
  - `HbmEnergyBlockEntity` 增加 `canReceiveEnergy(Direction side)` 与 `canExtractEnergy(Direction side)`，按 side 返回输入、输出或双向 FE adapter。
  - `HbmEnergyUtil` 增加电池/物品与 HBM storage 的通用转移方法：
    - `chargeItemFromStorage(ItemStack, HbmEnergyProvider, long)`
    - `chargeStorageFromItem(ItemStack, HbmEnergyReceiver, long)`
    - `moveForgeEnergy(IEnergyStorage, IEnergyStorage, int)`
    - `moveForgeEnergy(ItemStack, ItemStack, int)`
- 2026-05-20 第四批已迁移：
  - `src/main/java/com/hbm/ntm/energy/HbmEnergySideMode.java`
  - `HbmEnergyBlockEntity` 增加 `getEnergySideMode(Direction side)`，用 `NONE`、`INPUT`、`OUTPUT`、`BOTH` 表达侧面能力。
  - `HbmEnergyUtil` 增加世界邻居 FE 转移工具：
    - `pullFromNeighbor(Level, BlockPos, Direction, HbmEnergyReceiver, long)`
    - `pushToNeighbor(Level, BlockPos, Direction, HbmEnergyProvider, long)`
    - `pullFromAllNeighbors(Level, BlockPos, HbmEnergyReceiver, long)`
    - `pushToAllNeighbors(Level, BlockPos, HbmEnergyProvider, long)`
    - `pullFromAllNeighborsCapped(Level, BlockPos, HbmEnergyReceiver, long)`
    - `pushToAllNeighborsCapped(Level, BlockPos, HbmEnergyProvider, long)`
- 2026-05-20 第五批已迁移：
  - `src/main/java/com/hbm/ntm/energy/HbmPowerNet.java`
  - 迁移旧 `PowerNetMK2` 的核心分配算法骨架：
    - provider/receiver 订阅表。
    - 默认 3 秒订阅超时。
    - 每次 update 汇总 provider 可用 HE 和 receiver 总需求。
    - receiver 按 `ConnectionPriority` 从高到低供电。
    - 同优先级 receiver 按需求权重分配。
    - provider 按可用输出权重扣能。
    - 保留最多 100 次的舍入误差补扣逻辑。
    - 保留 `sendPowerDiode(long power)` 语义。
  - 当前 `HbmPowerNet` 只负责算法和订阅生命周期，不负责导线寻路、节点合并、chunk 卸载扫描；这些仍留给后续 UNINOS/节点网络层。
- 2026-05-20 第六批已迁移：
  - `src/main/java/com/hbm/ntm/energy/HbmNetworkNode.java`
  - `src/main/java/com/hbm/ntm/energy/HbmNodeNet.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNode.java`
  - `HbmPowerNet extends HbmNodeNet<HbmEnergyNode>`，开始对齐旧版 `NodeNet` / `Nodespace.PowerNode` 的结构层。
  - 已具备：
    - 节点位置 `BlockPos`。
    - 节点连接面 `Set<Direction>`。
    - 节点 net 归属。
    - `expired` 与 `recentlyChanged` 标记。
    - network valid 状态。
    - `joinLink`、`forceJoinLink`、`leaveLink`、`joinNetwork`、`destroy`。
  - 当前仍未迁移：
    - 世界级 nodespace map。
    - 按方块放置/破坏自动创建和销毁节点。
    - 邻居扫描与连通分量重建。
    - recentlyChanged 二次复查机制。
    - chunk unload 时节点剔除。
- 2026-05-20 第七批已迁移：
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNodespace.java`
  - `src/main/java/com/hbm/ntm/event/CommonForgeEvents.java` 接入服务端 tick 与 level unload。
  - 已具备世界级能量 nodespace 管理：
    - 按 `Level.dimension()` 分世界保存节点 map。
    - `getNode(Level, BlockPos)`。
    - `createNode(Level, HbmEnergyNode)`。
    - `destroyNode(Level, BlockPos)`。
    - `unloadLevel(Level)`。
    - server tick 中调用 `HbmEnergyNodespace.tick(ServerLevel)`。
    - tick 时检查没有有效网络或 recentlyChanged 的节点。
    - 检查相邻节点双向连接面是否匹配。
    - 按旧版逻辑连接节点、合并网络、创建新 `HbmPowerNet`。
    - tick active power nets：先 reset tracker，再 update。
    - 每 5 分钟清理无效或空网络。
  - 当前仍未迁移：
    - 具体导线方块/BlockEntity 调用 create/destroy node。
    - 节点重建时的完整连通分量拆分。
    - chunk unload 细粒度剔除。
    - 旧版 recentlyChanged 二次复查的完整 CPU/稳定性补偿策略。
- 当前现代策略：
  - 内部继续使用 1.7.10 风格 long 型 HE。
  - 对外用 `ForgeEnergyAdapter implements IEnergyStorage` 暴露 Forge Energy capability。
  - FE 的 int 上限只作为外部 capability 限幅，不改变内部 HE 上限。
  - 先迁储能、接收、输出、优先级、直接转移工具；暂不迁 `PowerNetMK2` 节点发现和 UNINOS 网络合并。
- 后续接入机器时：
  - BlockEntity 持有 `HbmEnergyStorage`。
  - `getCapability(ForgeCapabilities.ENERGY, side)` 返回 `LazyOptional<IEnergyStorage>`。
  - NBT 推荐保存 key：`Energy` 或旧兼容 key；若迁旧 `IBatteryItem`，仍需兼容默认 `charge` key。
- 后续接入普通能量机器时，优先继承 `HbmEnergyBlockEntity`：
  - 构造时传入 `new HbmEnergyStorage(maxPower, maxReceive, maxExtract)`。
  - 默认自动保存 `Energy` NBT。
  - 默认自动暴露 `ForgeCapabilities.ENERGY`。
  - 若机器有方向性输入/输出，优先覆写 `getEnergySideMode(Direction side)`。
  - 简单机器也可以只覆写 `canReceiveEnergy(Direction side)` 与 `canExtractEnergy(Direction side)`。
  - `getCapability` 会按 side 自动返回只输入、只输出或双向 `IEnergyStorage`。
- 后续接入电池物品时，优先继承 `HbmBatteryItem`：
  - 构造参数为 `maxCharge`、`chargeRate`、`dischargeRate`。
  - 默认使用旧版 `charge` NBT key。
  - 默认暴露 Forge item energy capability。
  - 默认 tooltip 使用旧翻译 key：`desc.item.battery.charge`、`desc.item.battery.chargeRate`、`desc.item.battery.dischargeRate`。

## 桥接法硬性要求：1.20.1 Forge Energy

- 后续所有迁移到 1.20.1 的 HBM 能量机器、储能方块、电池类物品，必须采用“内部 HBM HE 语义 + 外部 Forge Energy capability”的桥接方案。
- 内部语义：
  - 保留 1.7.10 的 long 型 HE 存储、容量和转移速率。
  - 保留 provider/receiver、`ConnectionPriority`、direct provision、后续 `PowerNetMK2`/UNINOS 网络分配算法。
  - 不允许为了适配 FE 把内部能量上限改成 int，也不允许丢弃旧版优先级和网络权重语义。
- Forge 通用互操作：
  - 方块实体应通过 `getCapability(ForgeCapabilities.ENERGY, side)` 暴露 `IEnergyStorage`。
  - 默认使用 `ForgeEnergyAdapter` 包装 `HbmEnergyHandler`。
  - FE 的 `int` 参数只作为外部输入/输出限幅；内部 HE 仍使用 long。
  - `receiveEnergy(..., simulate=true)` 与 `extractEnergy(..., simulate=true)` 不得改变内部状态。
  - 每个 side 是否可输入/输出，应由 HBM 机器自己的连接规则决定，不能全部无条件开放。
- 推荐接入模板：
  - BlockEntity 字段：`private final HbmEnergyStorage energy = new HbmEnergyStorage(maxPower, maxReceive, maxExtract);`
  - BlockEntity 字段：`private final LazyOptional<IEnergyStorage> forgeEnergy = LazyOptional.of(() -> new ForgeEnergyAdapter(energy));`
  - `invalidateCaps()` 中必须 invalidate `forgeEnergy`。
  - NBT 保存/读取调用 `energy.serializeNBT()` / `energy.deserializeNBT(...)` 或等价封装。
- 电池物品要求：
  - 旧版 `IBatteryItem` 默认 NBT key 为 `charge`，迁移时必须兼容读取。
  - 对外可暴露 Forge Energy item capability，但内部仍应能返回 HBM HE 数值。
- 禁止路线：
  - 禁止直接用 Forge `EnergyStorage` 取代所有 HBM 能量逻辑。
  - 禁止每台机器各自写一套 FE 适配器；应复用 `ForgeEnergyAdapter` 或共享扩展类。

## 2026-05-20 继续推进：节点 BlockEntity 接入层

- 本批新增：
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyNodeBlockEntity.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyNetworkBlockEntity.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNodespace.java` 维度内 active net 管理与拆网重建补强。
- `HbmEnergyNodeBlockEntity` 用于纯导线/节点方块：
  - 服务端 `onLoad()` 自动调用 `HbmEnergyNodespace.createNode(level, node)`。
  - `setRemoved()` 自动调用 `HbmEnergyNodespace.destroyNode(level, pos)`。
  - 默认 `getEnergyConnections()` 为六面连通；后续电缆、二极管、限定面导线应覆盖该方法返回真实可连接方向。
  - `refreshEnergyNode()` 可在邻居、朝向、方块状态变化后由子类调用，重新写入节点连接面。
- `HbmEnergyNetworkBlockEntity` 用于“既有 HBM long HE 储能，又需要挂入 PowerNet 节点”的机器：
  - 继承 `HbmEnergyBlockEntity`，继续复用 `Energy` NBT 保存、FE capability 桥接、side mode。
  - 额外持有一个 `HbmEnergyNode` 并复用同样的 create/destroy/refresh 生命周期。
  - 后续迁移发电机、储能方块、机器输入输出口时，若旧版使用 Energy MK2 网络，应优先继承该类或按其生命周期接入。
- `HbmEnergyNodespace` 本批修正：
  - active `HbmPowerNet` 从全局集合下沉到每个 `EnergyNodeWorld`，避免多维度服务器 tick 时同一网络被重复 update。
  - 增加 `getNodeCount(Level)` 与 `getNetworkCount(Level)`，用于后续调试、测试或信息面板。
  - 节点移除或同位置替换时，会销毁旧网络并标记旧网络剩余节点 `recentlyChanged`，下一次 tick 按当前邻接关系重新合并为新的连通分量。
- 当前仍未完成：
  - 还没有具体 Energy MK2 cable/block 注册和模型资源迁移。
  - 还没有机器每 tick 自动向所在 `HbmPowerNet` 刷新 provider/receiver 订阅的统一基类；后续可以在 `HbmEnergyNetworkBlockEntity` 上再加 tick helper。
  - 还没有端到端游戏内验证电缆破坏后网络拆分，仅完成 `compileJava` 编译验证。

## 2026-05-21 继续推进：订阅与方块刷新层

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNodeHost.java`
  - `src/main/java/com/hbm/ntm/block/HbmEnergyNodeBlock.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyNodeBlockEntity.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyNetworkBlockEntity.java`
- `HbmEnergyNodeHost` 抽出节点宿主契约：
  - `getEnergyNode()` 返回当前 BlockEntity 持有的 HBM 能量节点。
  - `refreshEnergyNode()` 用于放置、邻居变化、朝向或连接状态变化后重建节点。
  - `removeEnergyNode()` 用于方块真正移除时从 nodespace 拆除节点。
- `HbmEnergyNodeBlock` 是后续导线/节点方块的通用方块基类：
  - `onPlace` 时刷新自身和邻居节点。
  - `neighborChanged` 时刷新自身节点。
  - `onRemove` 时移除自身节点并刷新邻居节点。
  - 子类仍需提供具体 BlockEntity，并在 BlockEntity 中覆盖 `getEnergyConnections()` 表达真实可连方向。
- `HbmEnergyNetworkBlockEntity` 增加网络订阅辅助：
  - `getPowerNet()` 返回当前节点所在 `HbmPowerNet`。
  - `serverTick(Level, BlockPos, BlockState, T)` 可作为 BlockEntityTicker 的服务端入口。
  - `refreshEnergyNetworkSubscriptions()` 按 `shouldSubscribeAsProvider()` / `shouldSubscribeAsReceiver()` 刷新订阅时间戳。
  - 默认不自动订阅 provider 或 receiver，避免把储能、发电机、耗电机误接；具体机器按旧版语义覆盖开关或直接调用 `subscribeEnergyProvider` / `subscribeEnergyReceiver`。
  - 默认 provider/receiver 对象是内部 `HbmEnergyStorage`，复杂机器可覆盖 `getNetworkEnergyProvider()` / `getNetworkEnergyReceiver()`。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 2026-05-21 继续推进：导线连接 BlockState 基类

- 本批更新：
  - `src/main/java/com/hbm/ntm/block/HbmEnergyNodeBlock.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyConnectionUtil.java`
- `HbmEnergyNodeBlock` 现在同时承担三层通用职责：
  - 方块生命周期：放置、邻居变化、移除时刷新自身/邻居 `HbmEnergyNode`。
  - 连接识别：实现 `HbmEnergyConnectorBlock`，让邻居即使在 BlockEntity 尚未加载时也能识别该方块可被能量导线连接。
  - 可视状态：维护 `north/east/south/west/up/down` 六个 boolean `BlockStateProperties`，用于后续 multipart 模型或 OBJ/BER 渲染判断连接臂。
- 新增连接状态行为：
  - `getStateForPlacement` 会按当前邻居计算初始六向连接状态。
  - `neighborChanged` 会刷新自身连接状态并重建节点。
  - `onPlace` 会刷新自身和邻居连接状态，再刷新节点网络。
  - `onRemove` 会移除自身节点，并刷新邻居连接状态与节点网络。
  - `getConnectionState(...)` 统一通过 `HbmEnergyConnectionUtil.canConnect(...)` 计算视觉/状态连接。
- `HbmEnergyConnectionUtil` 新增 block-level overload：
  - `collectBlockConnections(BlockGetter, BlockPos, HbmEnergyConnectorBlock)`
  - `canConnect(BlockGetter, BlockPos, HbmEnergyConnectorBlock, Direction)`
- 后续具体电缆迁移建议：
  - 普通红线/包线可继承 `HbmEnergyNodeBlock`，BlockEntity 继承 `HbmEnergyNodeBlockEntity`。
  - 数据生成可用六个 boolean property 输出 multipart cable arm。
  - 特殊导线若有单向、开关、检测器、颜色隔离等限制，覆盖 `canConnectEnergy(BlockGetter, BlockPos, Direction)` 或 BlockEntity 的 `canConnectEnergy(Direction)`。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - 仍有 `neighborChanged/onRemove` 相关弃用提示，当前项目已有同类生命周期用法；后续若统一迁移到新版回调再一起替换。

## 2026-05-21 继续推进：tryProvide/trySubscribe 迁移辅助

- 本批更新：
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyUtil.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyBlockEntity.java`
- 对齐 1.7.10 契约：
  - 旧版 `IEnergyProviderMK2#tryProvide(world, x, y, z, dir)` 会在相邻方块是导线时把 provider 订阅进导线所在 `PowerNetMK2`。
  - 同一方法也会在相邻方块是 receiver 且 `allowDirectProvision()` 为真时，绕过网络直接传输一次 HE。
  - 旧版 `IEnergyReceiverMK2#trySubscribe(...)` 会在相邻导线可从对应面连接时，把 receiver 订阅进该导线所在网络。
- `HbmEnergyUtil` 新增现代辅助：
  - `subscribeProviderToNeighborNetwork(Level, BlockPos, Direction, HbmEnergyProvider)`
  - `subscribeReceiverToNeighborNetwork(Level, BlockPos, Direction, HbmEnergyReceiver)`
  - `subscribeProviderToNetwork(Level, BlockPos, Direction, HbmEnergyProvider)`
  - `subscribeReceiverToNetwork(Level, BlockPos, Direction, HbmEnergyReceiver)`
  - `tryProvideToNeighbor(Level, BlockPos, Direction, HbmEnergyProvider)`
  - `tryProvideToAllNeighbors(Level, BlockPos, HbmEnergyProvider)`
  - `subscribeReceiverToAllNeighborNetworks(Level, BlockPos, HbmEnergyReceiver)`
  - `getConnectablePowerNet(Level, BlockPos, Direction)`
- 现代桥接语义：
  - 网络订阅仍要求相邻方块实体实现 `HbmEnergyConnector`，且相邻方块对应 side 允许连接。
  - `tryProvideToNeighbor` 保留旧版两段式逻辑：先尝试订阅相邻导线网络，再尝试直接 HBM receiver 传输。
  - 若相邻方块不是 HBM receiver，则走现有 `ForgeCapabilities.ENERGY` 推电桥接，保持 1.20.1 FE 通用互操作。
- `HbmEnergyBlockEntity` 新增机器侧便利方法，并全部尊重 side mode：
  - `pullEnergyFromSide(Direction, long)` / `pushEnergyToSide(Direction, long)`
  - `pullEnergyFromAllSides(long)` / `pushEnergyToAllSides(long)`
  - `subscribeEnergyProviderToSide(Direction)` / `subscribeEnergyReceiverToSide(Direction)`
  - `subscribeEnergyProviderToAllSides()` / `subscribeEnergyReceiverToAllSides()`
- 后续机器迁移建议：
  - 普通耗电机 tick 中优先调用 `subscribeEnergyReceiverToAllSides()` 或按旧版方向调用单侧方法。
  - 发电机 tick 中优先调用 `subscribeEnergyProviderToAllSides()`；若旧版允许直接贴脸供电，则调用 `HbmEnergyUtil.tryProvideToNeighbor/AllNeighbors`。
  - 若机器已有 `HbmEnergyNetworkBlockEntity` 自身节点，仍可使用其 `refreshEnergyNetworkSubscriptions()`；若只是普通邻接机器，则使用 `HbmEnergyBlockEntity` helper。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - 当前仅有 `HbmEnergyNodeBlock` 使用/覆盖 Minecraft 弃用 API 的编译提示，属于 `neighborChanged/onRemove` 生命周期入口，后续如项目统一改为新版回调再一起调整。

## 2026-05-21 继续推进：连接契约与连接面计算

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyConnector.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyConnectorBlock.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyConnectionUtil.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyNodeBlockEntity.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyNetworkBlockEntity.java`
- 对齐 1.7.10 契约：
  - 旧版 `IEnergyConnectorMK2#canConnect(ForgeDirection dir)` 中，`dir` 表示“当前方块的被连接面”。
  - 旧版 `IEnergyConductorMK2#createNode()` 会按方向生成 `PowerNode` 连接点；现代版改为 `HbmEnergyConnectionUtil.collectNodeConnections(...)` 统一计算。
  - 旧版 `IEnergyConnectorBlock` 是无 TileEntity 方块的视觉/连接辅助；现代版对应 `HbmEnergyConnectorBlock`。
- 现代连接规则：
  - `HbmEnergyConnector#canConnectEnergy(Direction side)` 默认拒绝 `null`，允许其他方向。
  - 节点连接面只在“双向允许”时写入：本方 `canConnectEnergy(direction)` 为真，邻居方块实体或方块也允许 `direction.getOpposite()`。
  - `HbmEnergyNodeBlockEntity` 默认使用 `HbmEnergyConnectionUtil.collectNodeConnections`，不再无条件六面连通。
  - `HbmEnergyNetworkBlockEntity` 同样使用连接工具，并让 `canConnectEnergy(side)` 尊重 `HbmEnergyBlockEntity` 的 side mode；`NONE` 面不会接入网络。
- 后续具体电缆迁移入口：
  - 普通 Energy MK2 电缆：继承 `HbmEnergyNodeBlock` + `HbmEnergyNodeBlockEntity`，默认即可按邻居 connector 自动计算连接面。
  - 二极管/开关/检测器：覆盖 `canConnectEnergy(Direction side)` 或 `getEnergyConnections()` 表达单向、红石开关、检测输出等旧版语义。
  - 纯 Block 连接体或多方块 dummy：实现 `HbmEnergyConnectorBlock`，用于无独立能量节点但需要被电缆识别连接的方块。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。

## 验证清单

- 多 provider、多 receiver、不同优先级时分配结果与旧算法一致。
- provider/receiver 卸载后从网络移除。
- 电池充放电不产生负数或超过最大值。
- EnergyControl/信息面板数据可显示当前能量与容量。
- 通过 Forge FE capability 插入/抽取能量时，HBM 内部 HE 数值同步变化。
- FE simulate 调用不改变 HBM 内部 HE。
- HE 大于 `Integer.MAX_VALUE` 时，FE getter 只限幅显示，不截断内部存储。
- `HbmEnergyBlockEntity` 子类保存/读取后，`Energy.Power` 数值保留。
- `HbmBatteryItem` 的 item capability 充放电与旧 `charge` NBT 同步。
- 方向性机器从输入面 capability 调用 `extractEnergy` 应返回 0；从输出面 capability 调用 `receiveEnergy` 应返回 0。
- 机器电池槽可通过 `HbmEnergyUtil.chargeStorageFromItem` 和 `chargeItemFromStorage` 与 HBM storage 交换能量。
- 从邻居拉电/推电时必须使用 `side.getOpposite()` 访问邻居 capability；已由 `pullFromNeighbor` / `pushToNeighbor` 封装。
- 若旧版机器总输出速率是全机器共享上限，应使用 `pushToAllNeighborsCapped` 或 `pullFromAllNeighborsCapped`，避免六面各自跑满导致超速。
- 导线/网络层后续接入时：
  - 网络节点只负责发现连通网络与持有一个 `HbmPowerNet`。
  - 机器每 tick 或按旧时机调用 `addProvider` / `addReceiver` 刷新订阅时间戳。
  - 网络 tick 调用 `HbmPowerNet.update()` 完成能量分配。
  - diode 类单向注入应调用 `sendPowerDiode(long power)`。
- 节点层后续接入时：
  - 每个能量导线/管道 BlockEntity 应创建一个或多个 `HbmEnergyNode`。
  - `HbmEnergyNode.connections` 必须反映该导线真实可连接方向。
  - 连通网络重建时使用 `HbmPowerNet.joinLink` 和 `joinNetwork` 合并节点。
  - 导线破坏时调用 `leaveLink` 或 `destroy`，并触发邻居重建。
- 世界 nodespace 接入要求：
  - 导线/管道 BlockEntity 创建时调用 `HbmEnergyNodespace.createNode(level, node)`。
  - 导线/管道 BlockEntity 删除时调用 `HbmEnergyNodespace.destroyNode(level, pos)`。
  - 不要在客户端创建 nodespace 节点。
  - 当前 nodespace 已由 `CommonForgeEvents.onServerTick` 每 tick 更新。
  - Level unload 时已调用 `HbmEnergyNodespace.unloadLevel` 清理对应维度节点。
- `HbmPowerNet` 算法验证应覆盖：
  - 高优先级 receiver 先于低优先级 receiver 获得能量。
  - 同优先级 receiver 按需求比例获得能量。
  - provider 扣能按可用输出比例分摊。
  - 订阅超过 3 秒未刷新后被剔除。
