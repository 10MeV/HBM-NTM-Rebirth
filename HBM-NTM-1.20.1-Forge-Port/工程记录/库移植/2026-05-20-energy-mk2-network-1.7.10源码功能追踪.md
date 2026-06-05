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
- 2026-05-22 deprecation 清理：
  - `HbmEnergyNodespace` 的 chunk 加载检查由 deprecated `hasChunkAt(BlockPos)` 改为 chunk 坐标版 `hasChunk(x >> 4, z >> 4)`，只改变 API 入口，不改变节点剔除语义。
  - 同步复查 `-Xlint:deprecation`，能量网络相关源码不再产生 deprecation 警告。
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

## 2026-05-21 继续推进：基础 `machine_battery` 储能方块接入

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/block/MachineBatteryBlock.java`
  - `src/main/java/com/hbm/ntm/blockentity/MachineBatteryBlockEntity.java`
  - `src/main/java/com/hbm/ntm/registry/ModBlocks.java`
  - `src/main/java/com/hbm/ntm/registry/ModBlockEntities.java`
  - `src/main/java/com/hbm/ntm/datagen/HbmBlockStateProvider.java`
  - `src/main/resources/assets/hbm/blockstates/machine_battery.json`
  - `src/main/resources/assets/hbm/models/block/machine_battery.json`
  - `src/main/resources/assets/hbm/models/item/machine_battery.json`
  - `src/main/resources/assets/hbm/textures/block/battery_front_alt.png`
  - `src/main/resources/assets/hbm/textures/block/battery_side_alt.png`
  - `src/main/resources/assets/hbm/textures/block/battery_top.png`
- 1.7.10 对照：
  - `com.hbm.blocks.ModBlocks#machine_battery`：`new MachineBattery(Material.iron, 1_000_000)`，硬度 5，抗性 10，基础贴图为 `battery_front_alt` / `battery_side_alt` / `battery_top`。
  - `com.hbm.blocks.machine.MachineBattery`：水平朝向、模拟比较器输出、破坏时保留持久 NBT/物品槽、GUI 打开、能量 tooltip。
  - `com.hbm.tileentity.machine.storage.TileEntityMachineBattery`：容量 `1_000_000 HE`，接收速度 `maxPower / 200 = 5_000 HE/t`，输出速度 `maxPower / 600 = 1_666 HE/t`；默认 `redLow = input`、`redHigh = output`、`priority = LOW`。
- 现代迁移语义：
  - `MachineBatteryBlockEntity` 继承 `HbmEnergyBlockEntity`，使用 `HbmEnergyStorage` 保存 long 型 HE，并继续通过 `ForgeEnergyAdapter` 暴露 FE capability。
  - 未通红石时按旧默认 `redLow` 作为输入端：只订阅相邻红线网络的 receiver；不主动从 FE/HBM 邻居 capability 拉电。
  - 通红石时按旧默认 `redHigh` 作为输出端：通过 `HbmEnergyUtil.tryProvideToAllNeighbors` 订阅相邻红线网络 provider，并尝试直接向允许 direct provision 的 HBM receiver 输出最多 `1_666 HE/t`。
  - `mode_buffer` 现在按现代 `HbmEnergyNetworkBlockEntity` 创建自身能量节点，并在同一网络内同时作为 provider/receiver 订阅，用于对齐旧版“储能方块桥接多个拆分网络视为共享网络”的外部语义；跨区块/卸载后的分裂重建仍需实机验证。
  - 比较器输出保留旧公式：空电为 0，非空按 `power / max * 15 + 1` 钳制到 0..15。
  - NBT 保存复用能量库 `Energy.Power`，并保留 `redLow`、`redHigh`、`lastRedstone`、`priority` 字段；旧版掉落持久 NBT key 与命名迁移暂未接入。
- 本批有意不迁移：
  - GUI、Container/Menu/Screen、两个物品槽的电池物品充放电。
  - 右键配置红石低/高模式、优先级切换、OC/ROR/EnergyControl 信息面板。
  - `machine_lithium_battery`、`machine_schrabidium_battery`、`machine_dineutronium_battery`、`machine_battery_potato`、FEnSU 与 battery socket。
  - 旧版 `IPersistentNBT` 掉落继承、已命名方块显示名和完整方块破坏库存掉落。
- 新增验证项：
  - `machine_battery` 应注册为机器标签页方块，拥有水平朝向、loot、pickaxe/iron tool tag 和旧版基础贴图。
  - 未通红石且邻接 `red_cable` 网络时，应作为 LOW 优先级 receiver 被网络分配能量。
  - 通红石且邻接 `red_cable` 网络或 FE receiver 时，应作为 provider 刷新订阅并按 `1_666 HE/t` 输出。
  - 比较器在 0 电量输出 0，在非 0 电量至少输出 1，满电输出 15。

## 2026-05-21 继续推进：首个真实 `HbmBatteryItem` 接入

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmBatteryItem.java`
  - `src/main/java/com/hbm/ntm/registry/ModItems.java`
  - `src/main/resources/assets/hbm/models/item/battery_potato.json`
  - `src/main/resources/assets/hbm/textures/item/battery_potato.png`
- 1.7.10 对照：
  - `com.hbm.items.machine.ItemBattery`：旧版普通电池物品使用 `charge` NBT key；当物品没有 NBT 时，`getCharge` 会写入并返回 `maxCharge`，即默认满电。
  - `com.hbm.items.ModItems#battery_potato`：`new ItemBattery(1000, 0, 100)`，控制标签页，贴图 `textures/items/battery_potato.png`。
  - 旧语言：`item.battery_potato.name=Potato Battery` / `马铃薯电池`，电池 tooltip key 为 `desc.item.battery.charge`、`chargeRate`、`dischargeRate`。
- 现代迁移语义：
  - `HbmBatteryItem` 继续使用旧版 `charge` NBT key，并通过 `ForgeBatteryItemAdapter` 暴露 item FE capability。
  - `HbmBatteryItem#getDefaultCharge` 对齐旧版：没有 `charge` NBT 的新 stack 会写入并返回 `maxCharge`，即默认满电。这样 `battery_potato` 从创造栏/新堆栈取出时可直接放出 `1_000 HE`。
  - `battery_potato` 注册为独立物品 ID，容量 `1_000 HE`、充电速率 `0 HE/t`、放电速率 `100 HE/t`，进入现代 `CONSUMABLE_TAB_ITEMS` 作为当前可见入口。
- 本批有意不迁移：
  - 完整 `battery_pack` 多变体、3D 电池包 OBJ item renderer、`ItemBatteryPack.EnumBatteryPack` 的容量/电容器集合。
  - 自充电电池 `battery_sc`、creative battery、fusion core、energy core、HEV battery。
  - `ItemBattery#getSubItems` 的空/满双堆栈展示；当前创造栏只给默认堆栈，旧版默认满电语义已覆盖 `battery_potato` 的使用场景。

## 2026-05-21 继续推进：`machine_battery` 双物品槽充放电

- 本批更新：
  - `src/main/java/com/hbm/ntm/blockentity/MachineBatteryBlockEntity.java`
  - `src/main/java/com/hbm/ntm/block/MachineBatteryBlock.java`
- 1.7.10 对照：
  - `TileEntityMachineBattery` 构造 2 槽 inventory。
  - tick 顺序中，先执行 `Library.chargeItemsFromTE(slots, 1, power, getMaxPower())`，即用方块能量给 1 号槽电池物品充电。
  - 网络输入/输出处理完成后，再执行 `Library.chargeTEFromItems(slots, 0, power, getMaxPower())`，即从 0 号槽电池物品向方块放电。
  - 破坏方块时旧版会掉落 inventory 内容；完整 persistent NBT 掉落仍属于后续项。
- 现代迁移语义：
  - `MachineBatteryBlockEntity` 新增 2 槽 `ItemStackHandler`，通过 `ForgeCapabilities.ITEM_HANDLER` 暴露给自动化和后续 GUI。
  - `SLOT_DISCHARGE = 0`：每 tick 通过 `HbmEnergyUtil.chargeStorageFromItem` 按 `MAX_RECEIVE = 5_000 HE/t` 从电池物品抽入方块。
  - `SLOT_CHARGE = 1`：每 tick 通过 `HbmEnergyUtil.chargeItemFromStorage` 按 `MAX_EXTRACT = 1_666 HE/t` 从方块给电池物品充电。
  - 保存 key 为 `Inventory`，复用 Forge `ItemStackHandler` NBT；破坏时弹出两槽内物品。
- 本批有意不迁移：
  - GUI/Menu/Screen 和旧版红石模式、优先级按钮。
  - 自动化面向上下/侧面的旧版 `getAccessibleSlotsFromSide` 差异；当前先开放同一个 item handler，后续做 GUI/自动化细分时再拆 wrapper。
  - 旧版 “空电池可从 0 号槽取出 / 满电池可从 1 号槽取出” 的 sided extraction 限制。

## 2026-05-21 能量库检查与优先补齐：receiver 端舍入余量

- 本次检查出的库层高优先级缺口：
  - `HbmPowerNet` 已有 provider 端舍入补扣，但 receiver 端按同优先级需求权重分配时，`long` 截断会让小电量预算在多个 receiver 之间全部变成 `0`。
  - 表现为：网络有 provider、receiver 也有需求，但每个同优先级 receiver 的 `toSend` 被截断为 0，导致该 tick 完全不传电。
  - 这会直接影响后续所有小功率电池、导线和机器联调，因此优先级高于继续新增具体机器。
- 本批更新：
  - `src/main/java/com/hbm/ntm/energy/HbmPowerNet.java`
- 现代修正语义：
  - 每个优先级先按需求权重分配 `priorityBudget`。
  - 权重分配后，如果由于整型截断或 receiver 剩余容量导致 `priorityBudget - priorityUsed` 仍大于 0，则在同优先级 receiver 中继续补发余量。
  - 补发仍遵守 receiver 剩余容量和 `getReceiverSpeed()`，并保留最多 100 轮安全阀。
  - provider 端仍沿用已有权重扣能 + 随机 provider 补扣舍入误差逻辑。
- 本次检查后仍然缺失/待补齐的主要功能：
  - 完整 `battery_pack` 多变体与电容器集合，包括旧版 3D item renderer 语义或现代替代展示。
  - `machine_battery` GUI/Menu/Screen、红石模式按钮、优先级按钮、旧版 sided slot 自动化限制。
  - `HbmEnergyNetworkBlockEntity` 的更完整自节点桥接语义，尤其 `mode_buffer` 作为导线节点桥接拆分网络的行为。
  - 更多导线族：经典线、可涂漆线、盒式线、二极管、开关、检测线、功率计。
  - 网络生命周期仍需游戏内验证：chunk 卸载、跨区块导线、导线拆分后网络重建、订阅超时剔除。
  - EnergyControl/信息面板、调试粒子、旧版 `provideInfoForECMK2` 兼容层尚未迁移。

## 2026-05-21 继续推进：普通 `red_cable` 导线实物接入

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/block/RedCableBlock.java`
  - `src/main/java/com/hbm/ntm/blockentity/RedCableBlockEntity.java`
  - `src/main/java/com/hbm/ntm/registry/ModBlocks.java`
  - `src/main/java/com/hbm/ntm/registry/ModBlockEntities.java`
  - `src/main/resources/assets/hbm/blockstates/red_cable.json`
  - `src/main/resources/assets/hbm/models/block/red_cable_core.json`
  - `src/main/resources/assets/hbm/models/block/red_cable_side.json`
  - `src/main/resources/assets/hbm/models/item/red_cable.json`
  - `src/main/resources/assets/hbm/textures/block/cable_neo.png`
- 1.7.10 对照：
  - `com.hbm.blocks.ModBlocks#red_cable`：`new BlockCable(Material.iron)`，硬度 5、抗性 10、`machineTab`，贴图 `hbm:cable_neo`。
  - `com.hbm.blocks.network.BlockCable`：非完整方块；碰撞/选区按 `Library.canConnect` 六向连接拓展，中心截面为 5.5px 到 10.5px。
  - `com.hbm.tileentity.network.TileEntityCableBaseNT`：服务端创建/销毁 `Nodespace.PowerNode`，六面 `canConnect`。
  - `api.hbm.energymk2.IEnergyConductorMK2#createNode`：默认生成六向连接节点。
- 现代迁移语义：
  - `RedCableBlock` 继承 `HbmEnergyNodeBlock`，复用现有节点刷新、邻居连接刷新、六向 `BlockState` 连接状态。
  - `RedCableBlockEntity` 继承 `HbmEnergyNodeBlockEntity`，通过 `HbmEnergyNodespace.createNode/destroyNode` 接入能量网络。
  - `HbmEnergyBlockEntity` 现在实现 `HbmEnergyConnector`，按 `getEnergySideMode` 暴露连接面；这是对齐旧版 `IEnergyHandlerMK2 extends IEnergyConnectorMK2` 的共享库修正，避免后续每台普通能量机器重复写连接胶水。
  - 碰撞/选区保留旧版 5.5px-10.5px 中心截面，并按 `north/east/south/west/up/down` 状态展开。
  - 贴图使用 1.7.10 资源 `textures/blocks/cable_neo.png`，现代路径为 `textures/block/cable_neo.png`。
- 本批有意不迁移：
  - `red_cable_classic`、`red_cable_paintable`、`red_wire_coated`、`red_cable_box`。
  - `cable_switch`、`cable_detector`、`cable_diode` 的红石/单向传输/功率计逻辑。
  - 机器侧 provider/receiver 实物接入；下一批应选择一个简单发电或耗电机器做端到端验证。
- 新增验证项：
  - `red_cable` 应进入机器创造标签。
  - 两根相邻 `red_cable` 放置后对应方向 `BlockState` 变为 true，破坏后相邻导线刷新为 false。
  - `RedCableBlockEntity` 加载时创建 nodespace 节点，移除时销毁节点。
  - 普通 HBM receiver/provider 邻接该导线时，可通过 `HbmEnergyUtil.subscribeProviderToNeighborNetwork` / `subscribeReceiverToNeighborNetwork` 找到 `HbmPowerNet`。

## 2026-05-21 继续推进：网络桥接、生命周期观测与 EC 信息层

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyNetworkBlockEntity.java`
  - `src/main/java/com/hbm/ntm/blockentity/MachineBatteryBlockEntity.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyBlockEntity.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNodespace.java`
  - `src/main/java/com/hbm/ntm/energy/HbmPowerNet.java`
  - `src/main/java/com/hbm/ntm/event/CommonForgeEvents.java`
  - `src/main/java/com/hbm/ntm/command/ModCommands.java`
  - `src/main/java/com/hbm/ntm/compat/CompatEnergyControl.java`
- 1.7.10 对照：
  - `TileEntityMachineBattery#updateEntity` 在 `mode_buffer` 下会把自身变成导线节点：若 `node` 缺失或过期则创建 `PowerNode`，随后 `tryProvide(..., ForgeDirection.UNKNOWN)` 并向自身节点网络 `addReceiver(this)`。
  - 非 buffer 模式下旧版会销毁自身 `PowerNode`，然后按六向相邻导线网络分别订阅 provider 或 receiver。
  - 旧版 `IEnergyHandlerMK2#provideInfoForECMK2` 默认写入 `energy`、`capacity` 与 `euType=HE` 等 EnergyControl 兼容数据；`TileEntityMachineBattery#provideExtraInfo` 额外写入 `diff`。
  - 旧版 provider/receiver 订阅仍使用 3 秒超时，机器每 tick 通过重新订阅刷新时间戳。
- 现代迁移语义：
  - `HbmEnergyNetworkBlockEntity` 增加 `shouldCreateEnergyNode()` 与 `refreshEnergyNodeState()`，允许机器按当前模式动态创建/释放节点；服务端 tick 会先同步节点状态，再刷新 provider/receiver 订阅。
  - `MachineBatteryBlockEntity` 改为继承 `HbmEnergyNetworkBlockEntity`：
    - `MODE_BUFFER` 时才创建自身 `HbmEnergyNode`，并向该节点所在 `HbmPowerNet` 同时订阅 provider 与 receiver。
    - `MODE_INPUT` / `MODE_OUTPUT` / `MODE_NONE` 时不保留自身节点，继续使用相邻导线网络的旧式订阅/供电语义。
    - 保留旧版 20 tick power log 与 `diff` 计算入口，为 EnergyControl/info panel 提供基础数据。
  - `HbmEnergyNodespace` 增加 chunk unload 清理入口和节点/网络观测查询：
    - `unloadChunk(Level, ChunkPos)` 在 Forge chunk unload 事件中剔除该 chunk 内节点，并触发剩余网络重建。
    - tick 中也会剔除 `level.hasChunkAt(pos)` 已不可用的遗留节点，兜底处理跨 chunk 卸载。
    - `getNodeCount`、`getNetworkCount`、`getNetworkLinkCount`、`getNetworkProviderCount`、`getNetworkReceiverCount`、`getNetworkEnergyTracker`、`hasValidNetwork` 供命令和后续测试使用。
  - `HbmPowerNet` 暴露 provider/receiver 当前订阅数量，并在查询时复用超时剔除逻辑，便于验证 3 秒订阅超时。
  - `HbmEnergyBlockEntity` 现在实现现代 `InfoProviderEC` 默认输出：
    - `euType = HE`
    - `energy = 当前 HE`
    - `capacity = 最大 HE`
  - `CompatEnergyControl` 暂迁旧版 EnergyControl key 常量的能量子集，后续流体/热量/进度类机器可继续扩展。
  - 新增命令验证入口：
    - `/hbm energy nodespace`：查看当前维度节点数与网络数。
    - `/hbm energy network <pos>`：查看指定节点位置网络有效性、link/provider/receiver 数量与最近 tick 转移 HE。
    - `/hbm energy info <pos>`：读取实现 `InfoProviderEC` 的方块实体输出数据。
- 实机验证建议：
  - buffer 模式 `machine_battery` 放在两段 `red_cable` 中间时，`/hbm energy network <battery_pos>` 应显示有效网络且 links 包含 battery 自节点。
  - 用红石切换使 battery 离开 buffer 后，同位置网络应失效或不再包含 battery 自节点，相邻导线网络应在后续 tick 重建。
  - 跨 chunk 摆放红线并卸载其中一个 chunk 后，`/hbm energy nodespace` 节点数应下降，剩余网络不应继续持有卸载 chunk 的节点。
  - 让 provider/receiver 停止刷新订阅超过 3 秒后，`/hbm energy network <pos>` 的 provider/receiver 数应下降。
- 当前仍未完成：
  - 真实客户端 debug 粒子开关和网络包尚未迁移；本批先补服务端观测命令。
  - EnergyControl 外部 mod 适配/卡片集成尚未接入；当前只迁移 HBM 侧 key 和 `InfoProviderEC` 数据出口。
  - 没有自动化集成测试世界，本批通过编译验证，实机验证仍需后续在客户端/专服环境执行。

## 2026-05-21 继续推进：能量网络调试粒子与连接观测

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyDebug.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyUtil.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNodespace.java`
  - `src/main/java/com/hbm/ntm/command/ModCommands.java`
- 1.7.10 对照：
  - `IEnergyHandlerMK2#particleDebug` 是编译期常量，默认 false。
  - `IEnergyProviderMK2#tryProvide` 在尝试导线网络订阅/直连供电后，如果 debug 开启，会发 `AuxParticlePacketNT`，`red` 表示成功接入网络。
  - `IEnergyReceiverMK2#trySubscribe` 在尝试导线网络订阅后，同样按成功/失败状态发送 power network 调试粒子。
- 现代迁移语义：
  - `HbmEnergyDebug` 提供服务端运行期开关，不再使用编译期常量，避免重新编译才能检查网络。
  - `HbmEnergyUtil.subscribeProviderToNeighborNetwork` 与 `subscribeReceiverToNeighborNetwork` 在每次尝试后按成功/失败发调试粒子。
  - `HbmEnergyUtil` 直连 HBM receiver 成功转移时也会发调试粒子。
  - 粒子暂用原版 `END_ROD` 表达成功连接/转移，`SMOKE` 表达尝试失败；后续若迁移旧 `AuxParticlePacketNT` 的自定义 network 粒子，可在 `HbmEnergyDebug` 内替换，不影响调用点。
  - 新增 `/hbm energy debug particles [true|false]`：无参数为切换，有参数为显式设置。
  - `/hbm energy nodespace` 会显示 `debugParticles` 当前状态。
  - `/hbm energy network <pos>` 现在额外显示该节点的 `nodeConnections`，用于检查红线连接面、buffer 电池自节点、拆网/跨区块连接。
- 额外编译修复：
  - 本批编译时发现并行迁移文件 `FalloutLayerBlock` 使用了旧/不适用 API：`BlockState#getMaterial()` 与 `Block#isLeaves(...)`。
  - 已按 1.20.1 改为 `below.blocksMotion()` 与 `BlockTags.LEAVES`，仅为解除当前编译阻塞，未扩展其行为面。
- 实机验证建议：
  - 开启 `/hbm energy debug particles true` 后，provider/receiver 相邻红线每 tick 应在连接面出现成功粒子。
  - 故意让机器面对不可连接面或断开红线时，应出现失败粒子或网络查询显示无有效连接。
  - `/hbm energy network <red_cable_pos>` 的 `nodeConnections` 应随邻居放置/破坏同步变化。

## 2026-05-21 继续推进：能量网络结构化调试快照

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmPowerNet.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNodespace.java`
  - `src/main/java/com/hbm/ntm/command/ModCommands.java`
- 修正命令树：
  - 能量命令现在挂在 `/hbm energy ...`，而不是误挂在 `/hbm radiation energy ...`。
  - 保留并整理当前调试入口：
    - `/hbm energy nodespace`
    - `/hbm energy node <pos>`
    - `/hbm energy network <pos>`
    - `/hbm energy info <pos>`
    - `/hbm energy debug particles [true|false]`
- `HbmPowerNet` 新增 `DebugSnapshot`：
  - `valid`
  - `links`
  - `providers`
  - `receivers`
  - `providerPower`
  - `providerRate`
  - `receiverDemand`
  - `receiverRate`
  - `lastTransfer`
  - `receiversByPriority`
  - 创建快照时会先执行订阅超时/失效剔除，便于观察 3 秒订阅生命周期。
- `HbmEnergyNodespace` 新增 `NetworkDebugSnapshot`：
  - 区分“没有节点”、“有节点但没有网络”、“有节点且有有效网络”三种状态。
  - 输出节点连接面、`recentlyChanged` 状态与对应 `HbmPowerNet.DebugSnapshot`。
- `/hbm energy network <pos>` 现在不再拼零散 getter，而是读取结构化 snapshot；输出会包含：
  - 节点连接面
  - recentlyChanged
  - link/provider/receiver 数量
  - provider 当前总电量与总输出速率
  - receiver 当前总需求与总接收速率
  - receiver 优先级分布
  - 最近 tick 网络转移量
- 用途：
  - 后续一次接入更多机器、导线族、二极管/开关/检测线时，可以用同一命令观察网络结构与订阅分布。
  - 排查“有导线但不传电”时，可区分是节点没建、网络没合并、provider/receiver 没订阅，还是需求/速率为 0。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 继续推进：订阅对象 loaded/removed 契约与显式退订

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmLoadedEnergy.java`
  - `src/main/java/com/hbm/ntm/energy/HbmPowerNet.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyUtil.java`
  - `src/main/java/com/hbm/ntm/blockentity/HbmEnergyBlockEntity.java`
  - `src/main/java/com/hbm/ntm/blockentity/MachineBatteryBlockEntity.java`
- 1.7.10 对照：
  - `com.hbm.uninos.NodeNet#isBadLink` 会剔除：
    - 实现 `ILoadedTile` 且 `isLoaded() == false` 的订阅对象。
    - 旧 `TileEntity#isInvalid()` 为 true 的订阅对象。
  - `IEnergyReceiverMK2#tryUnsubscribe` 用于从相邻导线网络移除 receiver。
  - `TileEntityMachineBattery#updateEntity` 在非当前模式下会主动对相邻网络 `removeProvider(this)` / `removeReceiver(this)`，避免旧订阅留到超时。
- 现代迁移语义：
  - 新增 `HbmLoadedEnergy` 作为 `ILoadedTile` 的现代等价最小接口。
  - `HbmEnergyBlockEntity` 默认实现 `isEnergyLoaded()`，条件为 `level != null && !isRemoved()`。
  - `HbmPowerNet` 的 provider/receiver 有效性现在会剔除：
    - `null`。
    - `HbmLoadedEnergy#isEnergyLoaded() == false`。
    - 直接作为 `BlockEntity` 的订阅对象且 `isRemoved()` 为 true。
  - `HbmPowerNet#getProviderCount()` / `getReceiverCount()` 的命令观测路径也复用同一套 stale 清理，不再只看 3 秒超时。
  - `HbmEnergyUtil` 增加显式退订 helper：
    - `unsubscribeProviderFromNeighborNetwork`
    - `unsubscribeReceiverFromNeighborNetwork`
    - `unsubscribeProviderFromNetwork`
    - `unsubscribeReceiverFromNetwork`
    - `unsubscribeProviderFromAllNeighborNetworks`
    - `unsubscribeReceiverFromAllNeighborNetworks`
  - `HbmEnergyBlockEntity` 增加机器侧便利退订方法：
    - `unsubscribeEnergyProviderFromSide/AllSides`
    - `unsubscribeEnergyReceiverFromSide/AllSides`
  - `MachineBatteryBlockEntity` 记录 `lastMode`，模式切换时会先从相邻网络显式退订 provider/receiver；离开 buffer 时立即释放自身节点。
- 实机验证建议：
  - 放置正在订阅的机器后破坏机器，`/hbm energy network <cable_pos>` 的 provider/receiver 数应在下一次查询或 tick 后立即下降，不应等满 3 秒。
  - `machine_battery` 在 input/output/buffer/none 之间切换时，相邻网络 provider/receiver 计数应随模式变化立即更新。
  - buffer 模式切到非 buffer 后，`/hbm energy network <battery_pos>` 应显示无有效自节点或 `nodeConnections=none`。

## 2026-05-21 继续推进：1.7.10 电池包/电容物品族

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmBatteryItem.java`
  - `src/main/java/com/hbm/ntm/registry/ModItems.java`
  - `src/main/java/com/hbm/ntm/registry/ModCreativeTabs.java`
  - `src/main/java/com/hbm/ntm/datagen/HbmLanguageProvider.java`
  - `src/main/java/com/hbm/ntm/datagen/HbmZhCnLanguageProvider.java`
  - `src/main/resources/assets/hbm/textures/item/battery_*.png`
  - `src/main/resources/assets/hbm/textures/item/capacitor_*.png`
- 1.7.10 对照：
  - `api.hbm.energymk2.IBatteryItem`：物品电池契约使用 `charge` NBT，提供空/满电 stack 工具方法。
  - `com.hbm.items.machine.ItemBatteryPack`：`battery_pack` 通过 meta 区分 12 种电池/电容；真实能量读取时没有 NBT 的普通新 stack 会写入并返回 `charge=0`。但 tooltip 里若完全无 NBT，会临时按满电显示；创造栏额外显式加入空电和满电 stack。
  - 旧创造栏会为每种可充/可放电电池同时加入空电和满电两个 stack。
- 已迁移的旧枚举参数：
  - `battery_redstone`：输出 100 HE/t，容量 `100 * 20 * 60 * 15`，充电速率 1,000 HE/t。
  - `battery_lead`：输出 1,000 HE/t，容量 `1,000 * 20 * 60 * 15`，充电速率 10,000 HE/t。
  - `battery_lithium`：输出 10,000 HE/t，容量 `10,000 * 20 * 60 * 15`，充电速率 100,000 HE/t。
  - `battery_sodium`：输出 50,000 HE/t，容量 `50,000 * 20 * 60 * 15`，充电速率 500,000 HE/t。
  - `battery_schrabidium`：输出 250,000 HE/t，容量 `250,000 * 20 * 60 * 15`，充电速率 2,500,000 HE/t。
  - `battery_quantum`：输出 1,000,000 HE/t，容量 `1,000,000 * 20 * 60 * 60`，充电速率 10,000,000 HE/t。
  - `capacitor_copper/gold/niobium/tantalum/bismuth/spark`：容量均为输出速率 `* 20 * 30`，充电速率等于输出速率。
- 现代迁移语义：
  - 由于 1.20.1 注册系统不适合再用一个 item + metadata，本批将 12 个旧 meta 变体拆成独立注册 ID，并保持旧名称作为现代 ID。
  - 所有新电池/电容复用 `HbmBatteryItem` 的 `charge` NBT 与 Forge Energy 桥接，不新增平行能量存储实现。
  - `ModCreativeTabs` 针对 `HbmBatteryItem` 调用 `addCreativeStacks`，按旧逻辑同时放入空电与满电变体。
  - 已从 1.7.10 `textures/models/machines/<name>.png` 复制对应 12 张贴图到现代 item 贴图目录。
  - 英文/中文语言 key 已按旧 lang 名称补齐，DataGen 可生成 item model 与 lang。
- 本批额外编译修复：
  - `ModCommands#fluidNetworkArgument` 返回类型从 literal builder 修正为通用 argument builder，解除 `/hbm fluid network/node` 命令树编译阻塞。
  - `FalloutLayerBlock#isFalloutItem` 适配当前 `ModItems.legacyItem` 返回的 `Item` 类型，解除并行迁移文件的编译阻塞。
- 当前仍未完成：
  - 旧 `battery_pack` 单 item + metadata 的存档迁移映射未做；目前是新世界/新注册 ID 语义。
  - 旧 3D/手持 renderer 没有迁移，本批采用普通 item generated 模型。
  - `battery_sc`、创造电池、聚变/能量核心类电池、土豆电池族扩展等仍需单独按 1.7.10 源码继续补。
  - 机器 GUI 中的电池槽、充放电按钮/模式图标、JEI/配方接入仍需随机器库迁移继续推进。
- 本批验证：
  - `.\gradlew.bat runData --rerun-tasks --no-daemon` 通过，DataGen 生成新增电池/电容 item model 与 en_us/zh_cn lang。
  - `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。
- 当前能量库移植度估算：
  - 核心 API/存储/Forge Energy 桥接：约 90%。
  - HbmPowerNet 分配算法、订阅、调试快照与 loaded/removed 生命周期：约 82%。
  - 节点空间/线缆：约 68%，已有 red_cable 与基础节点重建，特殊线缆/二极管/开关线仍缺。
  - 机器接入：约 48%，`machine_battery` 已覆盖基础模式与 buffer 自节点，但 GUI、更多机器和实机矩阵未补齐。
  - 电池物品契约：约 60%，本批补齐 12 种旧 `battery_pack` 电池/电容，仍缺特殊电池族与旧 metadata 迁移。
  - EnergyControl/info/debug 兼容层：约 58%，已有 HBM 侧 key、info 出口、命令与调试粒子，外部 mod 卡片/面板集成仍缺。
  - 综合估算：约 72%。这是按功能面而不是文件数量估算；实机生命周期验证、特殊线缆和大面积机器接入会决定下一阶段百分比上限。

## 2026-05-21 复查：电池包/电容牵引的后续库

- 1.7.10 复查结论：
  - `ClientProxy#registerRenderInfo` 对 `ModItems.battery_pack` 注册 `ItemRenderBatteryPack`。
  - `ItemRenderBatteryPack` 不使用普通 2D item icon，而是绑定每个 `EnumBatteryPack#texture` 后渲染 `ResourceManager.battery_socket` 的 `Battery` 或 `Capacitor` part。
  - `RenderBatterySocket` 对机器方块实体也复用同一个 `models/machines/battery.obj`，按 slot 内物品渲染 `Socket`、`Supports`、`Battery`、`Capacitor`。
  - `TileEntityBatterySocket`、`TileEntityBatteryBase`、`TileEntityMachineBattery` 与大量机器容器通过 `IBatteryItem`、`Library.chargeItemsFromTE`、`Library.chargeTEFromItems` 形成机器电池槽行为。
- 迁移这些电池/电容需要优先推进的库：
  - 渲染/OBJ part 库：必须支持旧 `battery.obj` 的按 part 渲染、物品与方块实体共用模型入口、后续按 ItemStack 动态换贴图。
  - 机器菜单/槽位库：需要统一的 battery slot 判定、shift-click 路由、slot valid/extract 规则；旧版几十个 `Container*` 都内联了 `IBatteryItem` 判断。
  - 机器电池转移 helper：迁移旧 `Library.chargeItemsFromTE` / `chargeTEFromItems`，避免每台机器重新写充放电边界、速率 clamp 和 creative battery 特判。
  - 机器同步/GUI 库：`GUIBatterySocket`、`GUIMachineBattery` 依赖能量条、模式按钮、tooltip、服务端按钮包；需要现代 Menu/DataSlot/自定义 packet 统一承载。
  - 配方/ComparableStack/OreDict 兼容层：电池包在 crafting、assembly、chemical plant、plasma forge、weapon/tool recipes、item pools 中大量引用旧 meta；现代独立 ID 需要映射表。
  - 战利品/物品池库：`ItemPoolsLegacy`、`ItemPoolsComponent`、`BlockCrate` 会生成特定电池包，需要在 loot/item pool 迁移时使用独立 ID 映射。
  - EnergyControl/OpenComputers/ROR 兼容层：`TileEntityBatterySocket` 暴露 `IInfoProviderEC`、`IRORValueProvider`、`IRORInteractive` 和 OC component；目前只迁移了 HBM 侧 info 出口。
- 建议推进顺序：
  - 先补渲染库 `battery.obj` part 入口，保证后续物品 renderer 与 `machine_battery_socket` 不各自硬编码模型路径。
  - 再补机器电池转移 helper 与 battery slot helper，因为这会直接影响所有电力机器。
  - 然后迁移 `machine_battery_socket` 最小机器，用它验证 3D 电池包显示、插槽充放电、GUI 能量条和 buffer/input/output 模式。
  - 最后再把 recipes、loot/item pool、外部兼容逐批接入。
- 本批已在渲染库开始推进：
  - 复制 1.7.10 `models/machines/battery.obj` 到 `assets/hbm/models/block/machines/battery.obj`。
  - 复制 `battery_socket.png`、`battery_sc.png` 到现代 `textures/block/machines`。
  - 新增 OBJ JSON：`battery_socket_socket`、`battery_socket_supports`、`battery_pack_battery`、`battery_pack_capacitor`，用 Forge OBJ `parts.visibility` 保持旧 part 边界。
  - `ObjMachineModels.BATTERY_SOCKET` 现在按旧 part 名提供 `Socket`、`Supports`、`Battery`、`Capacitor` 统一入口。
  - 当前限制：
  - `battery_pack_battery` / `battery_pack_capacitor` 目前是库级默认贴图模型；按 12 个具体 ItemStack 动态换贴图仍需后续 `BlockEntityWithoutLevelRenderer` 或可参数化 OBJ 渲染桥。

## 2026-05-21 对齐核查：当前能量库 vs 1.7.10 Energy MK2

- 核查范围：
  - 旧版事实源：`api/hbm/energymk2/*`、`com/hbm/uninos/*`、`com/hbm/lib/Library#chargeItemsFromTE/chargeTEFromItems`、`TileEntityMachineBattery`、`TileEntityBatterySocket`、`ItemBatteryPack`。
  - 现代实现：`com.hbm.ntm.energy.*`、`HbmEnergyBlockEntity`、`HbmEnergyNetworkBlockEntity`、`MachineBatteryBlockEntity`、`HbmBatteryItem`、`red_cable` 接入层。
- 已确认对齐的核心语义：
  - 内部能量单位仍为 long HE，provider/receiver/storage 均保留 `getPower/setPower/getMaxPower` 语义。
  - `ConnectionPriority` 顺序与旧版一致：`LOWEST -> LOW -> NORMAL -> HIGH -> HIGHEST`，网络更新时从高到低遍历。
  - provider/receiver 订阅表使用 3 秒超时刷新模型，机器需要每 tick 重新订阅。
  - `allowDirectProvision()` 默认 true，provider 可在相邻 receiver 允许时绕过导线直接传电。
  - `IBatteryItem` 的核心数据 key 仍为 `charge`；旧 `ItemBattery` / `ItemBatteryPack` 没有 NBT 时默认写入满电。
  - UNINOS/Nodespace 的基本模型已经对齐：节点持有位置、连接面、net、expired、recentlyChanged；节点变化后按邻接关系合并网络。
  - `NodeNet.isBadLink` 的 loaded/invalid 失效清理已用 `HbmLoadedEnergy` 与 `BlockEntity#isRemoved` 近似迁移。
- 已发现并修正的偏差：
  - 现代 `HbmPowerNet` 曾在 receiver 分配后对同优先级 leftover 做二次补发；旧 `PowerNetMK2` 没有 receiver 侧补发，只在 provider 扣能阶段做最多 100 次随机舍入补扣。
  - 本批已移除 receiver 侧二次补发，并继续修正为旧版“按 priorityDemand 权重分配一次，再按全局累计 `energyUsed` 扣 `toTransfer`”的优先级循环结果。
- 允许保留但必须标注为现代适配/诊断的内容：
  - `ForgeEnergyAdapter`、`ForgeBatteryItemAdapter`、FE capability、side mode、FE 邻居 push/pull 都是 1.20.1 互操作层，1.7.10 不存在；它们不能改变内部 HE/PowerNet 行为。
  - `/hbm energy ...` 命令、结构化 debug snapshot、运行时 debug particles 开关是现代验证工具；旧版只有编译期常量 `particleDebug=false` 与 `AuxParticlePacketNT` 调试粒子路径。
  - `HbmEnergyNodespace.unloadChunk/pruneUnloadedChunks` 是现代生命周期防漏补偿；旧 UNINOS 主要按 World map 和 loaded/invalid subscriber 处理，没有现代 chunk capability 生命周期。
- 未完全对齐/需要纠偏的内容：
  - `HbmEnergyUtil.chargeItemFromStorage` / `chargeStorageFromItem` 当前主要走 Forge item energy capability；旧版机器转移入口是 `Library.chargeItemsFromTE` / `chargeTEFromItems`，并且有 `battery_creative` 特判。下一步应补 `HbmBatteryTransfer` 或等价 helper，机器优先走 HBM battery 语义，再降级到 FE。
  - `MachineBatteryBlockEntity` 物品槽 `isItemValid` 当前两个槽都放行所有 stack；旧版代码表面上也 `return true`，但容器 shift-click 和自动化抽出规则依赖 `IBatteryItem` 与空/满电判断，现代 Menu/slot helper 尚未迁移，行为未完整。
  - `MachineBatteryBlockEntity` NBT 使用 `Energy.Power` 包装保存；旧版 `TileEntityMachineBattery` 顶层保存 `power`。若要兼容旧世界，需要读旧 `power` key。
  - `MachineBatteryBlockEntity` 早期实现曾直接拉/推 FE 邻居；2026-05-23 已从旧 `tryProvide`/输入模式路径移除，FE 直连只作为显式现代桥接 API 保留，不再视为旧版功能。
  - `HbmEnergyNodespace` 已支持多 BlockPos 节点与 `DirPos` 式偏移连接点，`machine_battery_socket` 端口已接入；剩余风险转为跨区块/卸载/拆网/重建生命周期实机验证，而不是节点表达能力缺失。
  - `HbmBatteryItem` tooltip 没有完全复刻旧 `ItemBatteryPack#addInformation` 的短数字、百分比、充满耗时、持续时间文案；这属于展示偏差，不影响能量语义，但应在物品 GUI/tooltip 批次补齐。
  - `battery_pack` 由旧单 item + metadata 拆为 12 个独立 ID；这是现代注册适配，仍需旧 meta -> 新 ID 的 recipe/loot/存档映射表，不能把当前独立 ID 当作完全等价迁移。
- 下一步移植计划：
  1. 优先补 `Library` 电池转移等价 helper：精确迁移 `chargeItemsFromTE` / `chargeTEFromItems` 的 clamp、rate、creative battery 特判和返回值语义，并让 `MachineBatteryBlockEntity` 使用该 helper。
  2. 为能量库增加最小行为测试或调试断言：验证 PowerNet receiver 分配无二次补发、provider 舍入补扣仍最多 100 次、订阅超时 3 秒。
  3. 扩展节点库支持多位置节点/端口节点，目标对齐 `GenNode.positions`，为 `machine_battery_socket`、多方块线缆/端口做准备。
  4. 迁移 `machine_battery` 的 Menu/Screen/slot helper，补旧红石低/高模式、优先级按钮、slot shift-click、自动化抽出规则。
  5. 在渲染库动态贴图问题解决后，迁移 `machine_battery_socket` 最小版，用它验证多位置节点、插入电池包显示和 HBM battery helper。
  6. 建立 `battery_pack` 旧 meta 映射表，供 recipes、loot/item pools、starter kit、assembly/chemical/plasma forge 后续迁移复用。

## 2026-05-21 对齐修正：消除当前可直接修正的偏差

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmBatteryTransfer.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyUtil.java`
  - `src/main/java/com/hbm/ntm/blockentity/MachineBatteryBlockEntity.java`
  - `src/main/java/com/hbm/ntm/energy/HbmNetworkNode.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNode.java`
  - `src/main/java/com/hbm/ntm/energy/HbmEnergyNodespace.java`
- 已对齐旧 `Library` 电池转移语义：
  - 新增 `HbmBatteryTransfer` 作为 `Library.chargeItemsFromTE` / `chargeTEFromItems` 的现代等价库。
  - `chargeItemsFromPower(ItemStack, power, maxPower)` 保留旧版边界：
    - `power < 0` 返回 0。
    - `power > maxPower` 返回 `maxPower`。
    - HBM 电池按 `min(power, chargeRate, maxCharge - charge)` 充电。
    - 返回充电后的“机器剩余 power”。
  - `chargePowerFromItem(ItemStack, power, maxPower)` 保留旧版边界：
    - HBM 电池按 `min(maxPower - power, dischargeRate, charge)` 放电。
    - 返回放电后的“机器 power”。
  - `battery_creative` 当前尚未迁移；本批提供 `setCreativeBatteryPredicate(...)` hook，待创造电池注册后补入旧特判。
- `machine_battery` 已改走旧 HBM battery helper：
  - 充电槽不再优先走 Forge item capability，而是调用 `HbmBatteryTransfer.chargeItemFromStorage(...)`。
  - 放电槽不再优先走 Forge item capability，而是调用 `HbmBatteryTransfer.chargeStorageFromItem(...)`。
  - `HbmEnergyUtil.chargeItemFromStorage` / `chargeStorageFromItem` 也改为 HBM battery 优先，FE capability 只作为非 HBM 物品的现代 fallback。
  - `MachineBatteryBlockEntity#load` 增加旧 `power` 顶层 NBT 读取兼容；现代 `Energy.Power` 保存仍作为 1.20.1 当前格式保留。
- 已补齐 UNINOS 多位置/连接点基础：
  - `HbmNetworkNode` 现在持有 `Set<BlockPos> positions`，不再只能代表一个方块位置。
  - 同一个 `HbmEnergyNode` 会被 `HbmEnergyNodespace` 注册到所有 positions，销毁时也从所有 positions 移除，对齐旧 `UniNodeWorld.pushNode/popNode`。
  - `HbmNetworkNode.NodeConnection` 作为旧 `DirPos` 等价结构，包含“连接位置 + 方向”。
  - 普通导线仍可用旧的方向集合构造，内部会自动生成标准连接点：`pos.relative(direction), direction`。
  - `HbmEnergyNodespace#checkNodeConnection` 改为按连接点匹配邻居节点的反向连接点，而不是只看 `pos.relative(direction)`。
  - `getNodeCount` 与 tick 遍历现在按唯一节点对象处理，避免多位置节点被重复计数或重复 tick。
- 仍需等待对应旧内容迁移后才能完全闭合的项：
  - `battery_creative` 物品未迁移，因此创造电池“机器充电槽清零/放电槽填满”特判只有 hook，尚未绑定实际 item。
  - `machine_battery` Menu/Screen/slot helper 尚未迁移；slot shift-click、自动化抽出、模式/优先级按钮仍未完整复刻。
  - `machine_battery_socket` 尚未迁移；多位置节点基础已经就绪，但旧 `TileEntityBatterySocket#getPortPos/getConPos` 的 2x2 端口与连接点还未具体接入。
  - `battery_pack` 旧 meta 到现代独立 ID 映射表尚未建立；配方、物品池、starter kit 迁移时必须补。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

## 2026-05-21 继续推进：machine_battery 菜单/按钮/槽位行为

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/menu/MachineBatteryMenu.java`
  - `src/main/java/com/hbm/ntm/client/screen/MachineBatteryScreen.java`
  - `src/main/java/com/hbm/ntm/network/packet/MachineBatteryButtonPacket.java`
  - `src/main/java/com/hbm/ntm/blockentity/MachineBatteryBlockEntity.java`
  - `src/main/java/com/hbm/ntm/block/MachineBatteryBlock.java`
  - `src/main/java/com/hbm/ntm/registry/ModMenuTypes.java`
  - `src/main/java/com/hbm/ntm/network/ModMessages.java`
  - `src/main/java/com/hbm/ntm/client/ClientModEvents.java`
  - `src/main/resources/assets/hbm/textures/gui/storage/gui_battery.png`
- 1.7.10 对照：
  - `ContainerMachineBattery` 双槽布局：slot 0 放电输入，slot 1 充电输出/充电槽；玩家背包坐标为旧 GUI `84/142` 行。
  - `GUIMachineBattery` 使用 `textures/gui/storage/gui_battery.png`，能量条区域 `62,17,52,52`，低/高红石按钮分别在 `133,16` 与 `133,52`，优先级按钮在 `152,35`。
  - `AuxButtonPacket` 三个按钮语义：0 循环 `redLow`，1 循环 `redHigh`，2 循环优先级。
  - 旧优先级虽然枚举有五档，但 `machine_battery` 会把 `LOWEST/HIGHEST/null` 纠正回 `LOW`，GUI 实际只展示 LOW/NORMAL/HIGH。
- 现代迁移语义：
  - `MachineBatteryBlock` 右键现在打开现代 Menu。
  - `MachineBatteryBlockEntity` 实现 `MenuProvider`，暴露旧双槽菜单与显示名 `container.hbm.battery`。
  - 槽位现在只接受 `HbmBatteryItem`，比旧 `isItemValidForSlot` 的表面 `return true` 更严格；这对齐旧自动化/充放电实际只处理 `IBatteryItem` 的行为，避免 FE 杂物进入旧机器槽。
  - `MachineBatteryMenu` 同步 long HE 数值、delta、redLow、redHigh、priority，并保留旧 shift-click：机器槽进玩家背包，玩家背包优先合并进两个机器槽。
  - `MachineBatteryButtonPacket` 提供现代 C2S 按钮入口，服务端校验距离和方块实体类型后修改模式/优先级。
  - `MachineBatteryScreen` 使用旧 GUI 贴图和旧坐标渲染能量条、模式按钮、优先级按钮，并发送按钮 packet。
- 当前限制：
  - GUI tooltip 文案目前用现代 key 简化表达，尚未完全复刻旧 `battery.priority.*.desc` 多行说明。
  - 自动化抽出规则只通过当前 item handler 的插入限制与机器 tick 生效，尚未按旧 `canExtractItem` 精确限制“slot 0 空电可抽、slot 1 满电可抽”。
  - OpenComputers/ROR 交互函数仍未迁移，仅保留 HBM 侧命令/debug/EC info。
  - 本批未新增运行中截图验证；只完成编译与资源处理验证。
- 本批验证：
  - 首次 `processResources` 因 Gradle 增量缓存缺失失败，清理 `build/resources/main/.cache` 后重跑。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 继续推进：battery_pack 3D 物品渲染与 tooltip 对齐

- 本批新增/更新：
  - `src/main/java/com/hbm/ntm/energy/HbmBatteryPackItem.java`
  - `src/main/java/com/hbm/ntm/client/renderer/BatteryPackItemRenderer.java`
  - `src/main/java/com/hbm/ntm/registry/ModItems.java`
  - `src/main/java/com/hbm/ntm/datagen/HbmItemModelProvider.java`
  - `src/main/resources/assets/hbm/textures/block/machines/battery_*.png`
  - `src/main/resources/assets/hbm/textures/block/machines/capacitor_*.png`
  - `src/generated/resources/assets/hbm/models/item/battery_*.json`
  - `src/generated/resources/assets/hbm/models/item/capacitor_*.json`
- 1.7.10 对照：
  - `ItemBatteryPack.EnumBatteryPack` 的 12 个 meta 变体记录 texture/capacity/chargeRate/dischargeRate。
  - `ItemRenderBatteryPack` 在物品栏平移 `0,-3,0` 并缩放 `5`，绑定 `EnumBatteryPack#texture` 后只渲染 `battery.obj` 的 `Battery` 或 `Capacitor` part。
  - `ItemBatteryPack#addInformation` 显示短数字、百分比、充电速率、放电速率、充满耗时和持续时间。
- 现代迁移语义：
  - 12 个现代独立 ID 现在注册为 `HbmBatteryPackItem`，保存旧 texture 名、旧 meta、是否电容。
  - `HbmBatteryPackItem` 仍继承 `HbmBatteryItem`，继续复用 `charge` NBT、HBM HE 语义和 Forge item energy capability。
  - `BatteryPackItemRenderer` 通过 `LegacyItemRendererBridge` 挂入 BEWLR，按物品选择：
    - battery 类：`models/block/machines/battery_pack_battery.json`
    - capacitor 类：`models/block/machines/battery_pack_capacitor.json`
    - 贴图：`textures/block/machines/<legacyTextureName>.png`
  - DataGen 对 `HbmBatteryPackItem` 输出 `builtin/entity` item model，避免普通 2D generated 模型覆盖自定义 renderer。
  - tooltip 改为旧 `ItemBatteryPack` 风格的短数字与时间信息。
- 当前限制：
  - 现代 `ItemDisplayContext` 的手持/地面变换是按旧 `ItemRenderBatteryPack` 近似迁移；仍需实机截图微调缩放和角度。
  - 旧单 item + meta 的存档/配方/loot 映射表仍未建立；当前只是 12 个独立 ID 承载旧变体参数。
  - `machine_battery_socket` 还未接入；本批先让物品 renderer 具备旧 OBJ part + 动态贴图语义。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 修正：battery_pack 物品图标透明问题

- 1.7.10 对照复核：
  - `ItemRenderBatteryPack#renderCommonWithStack` 每次按 `EnumBatteryPack#texture` 绑定独立整图贴图，然后只渲染 `battery.obj` 的 `Battery` 或 `Capacitor` part。
  - 旧 GUI 缩放 `5` 是像素级矩阵下的缩放，不是现代 item model 坐标下的 5 倍 block/model 缩放。
- 本批修正：
  - `LegacyWavefrontModel` 补动态贴图重载，允许同一个 OBJ group 在调用时传入不同 texture。
  - `BatteryPackItemRenderer` 不再使用 Forge baked OBJ part 尝试动态换 RenderType，改为直绘旧 OBJ 的 `Battery` / `Capacitor` group。
  - GUI 缩放改到 `0.36` 量级，避免创造栏图标被 slot 裁切成黑条。
- 当前仍需实机确认：
  - 12 个电池/电容在创造栏是否都显示对应旧贴图。
  - 手持、地上掉落物的大小和朝向是否需要继续微调。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 继续推进：machine_battery 自动化槽位方向与抽出规则

- 本批更新：
  - `src/main/java/com/hbm/ntm/energy/HbmBatteryTransfer.java`
  - `src/main/java/com/hbm/ntm/blockentity/MachineBatteryBlockEntity.java`
- 1.7.10 对照：
  - `TileEntityMachineBattery#getAccessibleSlotsFromSide`：
    - 顶面只暴露 slot 0。
    - 底面暴露 slot 0 与 slot 1。
    - 侧面只暴露 slot 1。
  - `canExtractItem`：
    - slot 0 只有电量为 0 的 `IBatteryItem` 可以被自动化抽出。
    - slot 1 只有电量等于最大电量的 `IBatteryItem` 可以被自动化抽出。
  - `canInsertItem` 仍走 `isItemValidForSlot`，实际充放电由 `Library.chargeItemsFromTE` / `chargeTEFromItems` 处理。
- 现代迁移语义：
  - `HbmBatteryTransfer` 增加 `isEmptyBattery` / `isFullBattery`，统一表达旧 `canExtractItem` 的空电/满电判定。
  - `MachineBatteryBlockEntity` 的 `ForgeCapabilities.ITEM_HANDLER` 改为按查询方向返回不同 wrapper：
    - `Direction.UP` 映射到旧顶面：只可见放电输入槽 slot 0。
    - `Direction.DOWN` 映射到旧底面：可见 slot 0 与 slot 1。
    - 水平方向映射到旧侧面：只可见充电槽 slot 1。
    - `null` side 保留完整 handler，供内部/无方向调用维持现有行为。
  - 新增 sided item handler wrapper，插入仍复用当前 HBM battery 有效性判断；抽出则严格限制为空电 slot 0、满电 slot 1。
- 当前限制：
  - `battery_creative` 尚未迁移，创造电池的特殊抽取语义仍等待实际 item 绑定到 `HbmBatteryTransfer#setCreativeBatteryPredicate`。
  - `machine_battery_socket` 尚未接入，因此本批只覆盖 `machine_battery` 自身的自动化面向。
  - GUI slot tooltip 与外部 OpenComputers/ROR 函数仍未补齐。
- 本批验证：
  - 首次 `.\gradlew.bat compileJava processResources --no-daemon` 命中已有 `TileSyncRequestPacket`/`ModMessages` 增量状态不一致；确认当前源码已有 `syncTileToPlayer` 与请求包注册后重跑。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 编译断点修正：machine_battery_socket 注册接入

- 背景：
  - clean port 已存在 `MachineBatterySocketBlock`、`MachineBatterySocketBlockEntity`、`MachineBatterySocketMenu`、`MachineBatterySocketScreen`、`MachineBatterySocketRenderer`。
  - 但注册层缺少 `MACHINE_BATTERY_SOCKET` 方块、BlockEntity 与 MenuType，导致全量编译在插座类引用处失败。
- 本批接入：
  - `ModBlocks` 注册 `machine_battery_socket`，使用 `MachineBatterySocketBlock` 与 `MultiblockBlockItem`，并加入 machine tab 列表。
  - `ModBlockEntities` 注册 `machine_battery_socket` BlockEntityType。
  - `ModMenuTypes` 注册 `machine_battery_socket` MenuType。
  - `ClientModEvents` 注册插座 screen 与 block entity renderer。
  - `MachineBatterySocketBlockEntity` 修正 1.20.1 `Connection` 包名，并把内部 `SocketEnergyStorage` 的充/放电逻辑改成显式计算，避免内部类无法调用接口默认方法的编译错误。
- 当前限制：
  - 本批只是接上已有插座实现以恢复构建；GUI 贴图、blockstate/model/lang/loot 等完整资源接入仍应在 machine_battery_socket 独立迁移批次复核。
  - 插座旧版行为与 1.7.10 多方块布局、端口连接规则仍需后续实机验证。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 继续推进：machine_battery_socket 资源与可见性闭环

- 1.7.10 对照：
  - `ModBlocks.machine_battery_socket` 注册到 `machineTab`，方块名 `machine_battery_socket`。
  - `MachineBatterySocket` 是 2x2 占位方块，核心 TileEntity 打开 `ContainerBatterySocket` / `GUIBatterySocket`。
  - `TileEntityBatterySocket#getName()` 返回 `container.batterySocket`。
  - `RenderBatterySocket` 使用 `ResourceManager.battery_socket`，常态渲染 `Socket` part，有上方框架时渲染 `Supports`，插入电池包时按 `Battery` / `Capacitor` part 和动态贴图渲染。
  - `canExtractItem` 对唯一槽位实际只允许满电 `IBatteryItem` 被自动化抽出；`getAccessibleSlotsFromSide` 对所有方向都只暴露 slot 0。
- 本批接入：
  - 补 `machine_battery_socket` 手工 blockstate，按 `facing` 指向已移植的 `machines/battery_socket_socket` OBJ part model。
  - 补 `machine_battery_socket` item model，沿用当前大机器 item 显示变换，避免不跑 DataGen 时缺模型。
  - 补 `machine_battery_socket` loot table，破坏后掉落自身。
  - 补手工语言资源：
    - `block.hbm.machine_battery_socket`
    - `container.batterySocket`
    - `container.hbm.battery` 与 battery/socket 共用 GUI mode/priority tooltip keys。
- 复核结果：
  - 现代 `MachineBatterySocketBlockEntity` 的 item handler 只接受 `HbmBatteryItem`，并且只允许满电电池被抽出；这与旧版唯一 slot 0 的 `canExtractItem` 实际行为对齐。
  - `MachineBatterySocketBlockEntity#createEnergyNode` 已使用 2x2 `positions` 和 8 个 `NodeConnection`，对应旧 `getPortPos/getConPos` 的多位置节点形状。
- 当前限制：
  - 旧版 `battery_sc` 超级电容随机放电/电击爆炸行为仍未迁移，因为现代对应物品与弹丸/电击爆炸链路还未完全闭合。
  - 旧 `ILookOverlay#printHook` 的准星能量提示尚未迁移。
  - OpenComputers/ROR 函数仍未接入现代兼容层。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-05 复查修正：电池座与电池物品行为对齐

- 1.7.10 对照：
  - `ItemBatteryPack#getCharge`：完全无 NBT 的 `battery_pack` 真实读取时会创建 NBT 并写入 `charge=0`，不是默认满电。
  - `ItemBatteryPack#addInformation`：tooltip 先把 `charge` 临时设为 `maxCharge`，只有 stack 已有 NBT 时才读取 NBT 内 `charge`；因此“无 NBT 提示满电、真实能量读取初始化为空”是旧版怪癖。
  - `ContainerBatterySocket` 使用普通 `SlotNonRetarded`，GUI/玩家手动操作不限制取出半电电池。
  - `TileEntityBatterySocket#getAccessibleSlotsFromSide` 对所有方向暴露唯一 slot 0；`canExtractItem` 因 `mode_input=0` 与唯一 slot 0，实际只允许自动化抽出满电 `IBatteryItem`。
  - `GUIBatterySocket` 能量条对超大数值先缩放后计算，避免 `Long.MAX_VALUE` 级创造电池造成乘法溢出。
  - `RenderBatterySocket` 对 `ModItems.battery_sc` 只按 item 类型渲染 `Battery` part，不要求 `battery_sc` 处于非空/加载态。
- 本批接入：
  - `HbmBatteryPackItem#getDefaultCharge` 改为 `0`，保证配方/机器产出的无 NBT 电池包第一次参与能量逻辑时为空电，避免凭空满电。
  - `HbmBatteryPackItem#appendHoverText` 保留旧版 tooltip 怪癖：完全无 NBT 时显示满电，已有 NBT 时按 `charge` 键显示，缺键则为 0。
  - `MachineBatterySocketBlockEntity` 主 `ItemStackHandler` 取消满电取出限制，使 GUI 手动取出与破坏掉落不再被自动化规则误伤。
  - `MachineBatterySocketBlockEntity` 新增侧面 `SocketSidedItemHandler`：任意方向可插入 HBM 电池，但只有满电电池可被自动化抽出，匹配旧 `ISidedInventory` 的实际 slot 0 行为。
  - `MachineBatterySocketBlockEntity` / `MachineBatterySocketMenu` 的能量条高度改为 `double` 比例 + clamp，避免创造电池 `Long.MAX_VALUE / 2` 显示时溢出。
  - `MachineBatterySocketRenderer` 对所有 `HbmSelfChargingBatteryItem` 渲染旧 `battery_sc` 模型；是否加载仍只影响能量与随机放电行为。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-05 继续复查：电池传输 helper 与 GUI 数值显示对齐

- 1.7.10 对照：
  - `Library#chargeItemsFromTE` 先按机器能量、电池充电速率和剩余容量计算 `toCharge`，随后调用 `IBatteryItem#chargeBattery`，不直接写 NBT。
  - `Library#chargeTEFromItems` 先按机器剩余容量、电池放电速率和电池当前能量计算 `toDischarge`，随后调用 `IBatteryItem#dischargeBattery`，不直接写 NBT。
  - 这个虚方法契约对 `battery_sc`、`battery_creative` 和后续特殊电池很重要：这些物品可以选择不改变 NBT，但仍按旧 helper 的返回语义给机器供能或吞能。
  - `GUIBatterySocket` 能量 tooltip 使用 `BobMathUtil.getShortNumber` 显示 `charge/max HE` 与 `delta HE/s`，不是直接显示完整 long。
- 本批接入：
  - `HbmBatteryTransfer#chargeItemsFromPower` / `chargePowerFromItem` 改为通过 `HbmBatteryItem#chargeBattery` / `dischargeBattery` 执行实际写入或扣除，保留旧 `Library` 的虚方法边界。
  - `MachineBatterySocketScreen` 能量 tooltip 改为短数字格式，避免创造电池和高阶电池在 GUI 内显示一整串 long。
  - 上一批的电池座侧面自动化/GUI 主槽拆分复查无新问题：主槽给玩家菜单和破坏掉落使用，侧面 capability 继续执行满电抽出规则。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 继续推进：battery_creative 与旧 Library 特判闭合

- 1.7.10 对照：
  - `ModItems.battery_creative` 使用 `ItemBatteryCreative`，ID `battery_creative`，贴图 `battery_creative_new`，创造栏为 `controlTab`。
  - `ItemBatteryCreative` 实现 `IBatteryItem`：
    - `getCharge` 返回 `Long.MAX_VALUE / 2`。
    - `getMaxCharge` 返回 `Long.MAX_VALUE`。
    - `getChargeRate` / `getDischargeRate` 返回 `Long.MAX_VALUE / 100`。
    - `chargeBattery` / `setCharge` / `dischargeBattery` 都为空操作。
  - `Library#chargeItemsFromTE` 对创造电池直接返回 `0`，即机器能量被视作全部转移给电池。
  - `Library#chargeTEFromItems` 对创造电池直接返回 `maxPower`，即机器能量被视作直接填满。
- 本批接入：
  - 新增 `HbmCreativeBatteryItem`，保留旧无限读数、无限速率和空操作充放电。
  - 注册现代 ID `battery_creative`，加入当前电池/消耗品创造栏列表。
  - `HbmNtm#commonSetup` 绑定 `HbmBatteryTransfer#setCreativeBatteryPredicate`，让旧 `Library` 两个特殊返回值在现代 helper 中生效。
  - 从 1.7.10 资源复制 `textures/items/battery_creative_new.png` 到现代 `textures/item/battery_creative_new.png`。
  - 补 `battery_creative` item model 与中英文名称。
- 当前限制：
  - 旧版位于 `controlTab`；现代当前还没有完全等价的控制/电池分类边界，本批先放入现有电池展示列表，后续创造栏整理时再按 1.7.10 tab 归位。
  - 创造电池作为其他机器 recipe/fuel/shift-click 特判的入口已存在；对应机器尚未迁移时不会触发那些旧路径。
- 本批验证：
  - 首次 `.\gradlew.bat compileJava processResources --no-daemon` 命中 Gradle/Javac 增量 classpath 状态不一致，大量 `build/classes` 缺失类文件报错。
  - 随后运行 `.\gradlew.bat clean compileJava processResources --no-daemon`：通过。

## 2026-05-22 继续推进：machine_battery_socket 最小完整语义闭合

- 1.7.10 对照：
  - `MachineBatterySocket` 是 2x2 多方块，核心方块右键打开 `GUIBatterySocket`。
  - `TileEntityBatterySocket` 只有 1 个电池槽，能量值直接读写槽内 `IBatteryItem`，自身不保存独立能量。
  - 低/高红石模式和优先级继承 `TileEntityBatteryBase`：模式为 input/buffer/output/none，优先级 GUI 只循环 LOW/NORMAL/HIGH。
  - `getPortPos` 覆盖 2x2 足迹，`getConPos` 暴露 8 个连接点，用于让插座像一个跨 2x2 区域的能量节点。
  - 自动化唯一槽位对所有方向可见，实际 `canExtractItem` 只允许满电电池从 slot 0 抽出。
  - `RenderBatterySocket` 只渲染底座 `Socket`、可选 `Supports`，以及插入的 `battery_pack` / `battery_sc`；普通 `battery_creative` 不会以 3D 电池包模型显示。
- 本批接入：
  - 新增 `MachineBatterySocketBlock`，使用现代多方块 helper 放置/清理 2x2 足迹，保留核心方块打开 GUI 与 comparator 输出。
  - 新增 `MachineBatterySocketBlockEntity`，用 `SocketEnergyStorage` 将现代能量网络读写绑定到槽内电池 NBT；插座自身不额外持久化能量。
  - 接入 2x2 `HbmEnergyNode` positions 与 8 个连接点，迁移旧 `getPortPos/getConPos` 的跨格节点形状。
  - 新增 `MachineBatterySocketMenu` / `MachineBatterySocketScreen`，使用旧 `gui_battery_socket.png` 和旧按钮/能量条坐标。
  - 新增 `MachineBatterySocketRenderer`，走 `LegacyWavefrontModel` 直渲染旧 `battery.obj` group；插入 `HbmBatteryPackItem` 时按 `Battery` / `Capacitor` 和旧动态贴图渲染。
  - 注册层接入 `machine_battery_socket` 方块、BlockEntity、MenuType、screen、BER、语言、loot、blockstate/item model。
  - `MultiblockHelper` 增加受保护清理入口，避免核心移除 dummy 时触发递归拆除。
- 对齐修正：
  - 插座 renderer 移除“任意 HBM 电池都显示默认铅电池模型”的现代扩展，只保留旧版已存在的 `battery_pack` 3D 显示；`battery_sc` 等对应物品未迁移前不伪造显示。
  - DataGen 对 `battery_creative` 使用旧资源名 `item/battery_creative_new`，避免默认 `battery_creative` 层名导致缺贴图失败。
  - 之前因现代资源路径规则修正 `weapon.mukeExplosion` 为 `weapon.muke_explosion`，保证 datagen 不被非法 ResourceLocation 阻断。
- 当前限制：
  - `battery_sc` 超级电容物品族、随机衰变倍数、插座放电/爆炸/电击逻辑仍未迁移；这是插座与旧能量库剩余最大缺口之一。
  - `ILookOverlay#printHook`、OpenComputers/ROR 函数、完整 EnergyControl 兼容层仍未闭合。
  - 插座 2x2 放置、区块卸载/重载、网络分裂/重建、跨区块连接与订阅超时仍需要实机生命周期验证。
  - 插座 renderer 朝向和上方 `Supports` 判定已按旧逻辑迁移，但仍需游戏内截图确认模型与碰撞足迹完全重合。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过，生成 `machine_battery_socket` blockstate/item model/loot 和 `battery_creative` 正确 item model。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 当前移植估算：
  - 按本追踪文档口径，能量库核心网络、基础电池机器、电池包/电容物品、创造电池、插座基础语义已闭合，约 `84%`。
  - 剩余约 `16%` 主要集中在 `battery_sc`、旧外部兼容层、look overlay/debug 粒子细节、真实世界生命周期验证和旧配方/创造栏归位。

## 2026-05-22 继续推进：已迁移电池归位旧 controlTab 展示语义

- 1.7.10 对照：
  - `battery_pack`、`battery_creative`、`battery_potato` 均注册到 `MainRegistry.controlTab`。
  - `ItemBattery#getSubItems`：可充电时展示空电栈，可放电时展示满电栈。
  - `ItemBatteryPack#getSubItems`：每个电池/电容 meta 都展示空电与满电两个栈。
- 本批接入：
  - `ModItems` 将当前已迁移的电池组、创造电池和土豆电池从现代消耗品页归入 `CONTROL_TAB_ITEMS`，对齐旧 `controlTab`。
  - `ModCreativeTabs.CONTROL` 现在与消耗品页一样识别 `HbmBatteryItem`，调用 `addCreativeStacks` 展示旧版空/满电变体。
- 当前限制：
  - `cube_power`、`battery_sc`、`battery_potatos`、`hev_battery`、`fusion_core`、`energy_core` 等旧 controlTab 电池仍未迁移；本批只归位已经存在的现代电池 ID。
  - DataGen 仍会按 `CONTROL_TAB_ITEMS` 生成已迁移电池 item model；`HbmBatteryPackItem` 继续输出 `builtin/entity` 以保留 OBJ 物品渲染。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 继续推进：battery_sc 自充电电池族基础语义

- 1.7.10 对照：
  - `ItemBatterySC` 是 `ItemEnumMulti(EnumBatterySC, true, true)`，旧 ID 为 `battery_sc`，通过 meta 区分 10 个变体。
  - 变体与功率：
    - `EMPTY = 0`
    - `WASTE = 150`
    - `RA226 = 200`
    - `TC99 = 500`
    - `CO60 = 750`
    - `PU238 = 1_000`
    - `PO210 = 1_250`
    - `AU198 = 1_500`
    - `PB209 = 2_000`
    - `AM241 = 2_500`
  - `IBatteryItem` 行为：
    - `getCharge` 恒等于 `getMaxCharge`。
    - `getChargeRate` 恒为 `0`。
    - `getDischargeRate` 等于 `getMaxCharge`。
    - `chargeBattery` / `setCharge` / `dischargeBattery` 都为空操作。
  - 旧贴图路径为 `textures/items/battery_sc.<variant>.png`。
  - `TileEntityBatterySocket#hasSCLoaded` 只对非 `EMPTY` 的 `battery_sc` 生效；加载后：
    - `getPower` 会乘以 `scPowerMult`。
    - `scPowerMult` 每 tick 以 `1/100` 随机游走，并夹在 `0.1D..1D`。
    - `damageTarget = 1200 + rand.nextInt(2400)`，达到后触发 `discharge()`，旧版会发射电击 beam 并触发电爆炸。
- 本批接入：
  - 新增 `HbmSelfChargingBatteryItem`，承载旧 `ItemBatterySC` 的只放电/不可充电/恒满电语义。
  - `ModItems` 增加 10 个现代独立 ID：
    - `battery_sc.empty`
    - `battery_sc.waste`
    - `battery_sc.ra226`
    - `battery_sc.tc99`
    - `battery_sc.co60`
    - `battery_sc.pu238`
    - `battery_sc.po210`
    - `battery_sc.au198`
    - `battery_sc.pb209`
    - `battery_sc.am241`
  - 10 个物品加入 `CONTROL_TAB_ITEMS`，继续按旧 controlTab 展示。
  - 复制旧 `battery_sc.*.png` 到现代 `textures/item/`，DataGen 按旧点号贴图名生成 item model。
  - `MachineBatterySocketBlockEntity` 保存/同步 `damageTimer`、`damageTarget`、`scPowerMult`，并在加载非空自充电电池时按旧逻辑随机波动能量读数。
  - `MachineBatterySocketRenderer` 对非空 `HbmSelfChargingBatteryItem` 渲染旧 `battery.obj` 的 `Battery` part，并绑定旧 `textures/block/machines/battery_sc.png`。
- 对齐修正：
  - 语言 DataGen 对已手写名称的 controlTab 电池跳过 fallback，避免 `battery_potato` 等重复 key 阻断 runData。
  - 旧 `RenderBatterySocket` 里 `battery_creative` 还有独立 HorsePronter/BeamPronter 特效；此前“普通创造电池不显示”记录不完整，后续应作为渲染库缺口补齐。
- 当前限制：
  - 本批没有迁移 `TileEntityBatterySocket#discharge()` 的实体 beam、方块破坏、电爆炸和音效，因为旧爆炸/投射物链路仍未完全对齐。
  - 旧配方仍未接入：空自充电电池合成，以及用核废料/各同位素 billet 转换为对应变体。
  - 旧 meta -> 现代独立 ID 的存档/配方映射表仍需后续统一梳理。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过，确认点号 ID 与点号贴图路径可正常生成模型/语言。
- 当前移植估算：
  - `battery_sc` 基础物品与插座能量/显示语义补齐后，能量库约 `87%`。
  - 剩余重点：`battery_sc` 放电事件、创造电池插座特效、旧配方/映射、OC/ROR/look overlay/EnergyControl 完整兼容、真实跨区块/卸载生命周期验证。

## 2026-05-22 继续推进：battery_pack / battery_sc 旧配方与 meta 映射入口

- 1.7.10 对照：
  - `CraftingManager` 里只有两个 `battery_pack` 变体走普通工作台：
    - `EnumBatteryPack.BATTERY_REDSTONE`：`IRON.plate()` + `REDSTONE.block()` + `plate_polymer`。
    - `EnumBatteryPack.CAPACITOR_COPPER`：`STEEL.plate()` + `CU.block()` + `plate_polymer`。
  - `machine_battery_socket` 有两套普通工作台配方：
    - `plate_polymer` + `coil_copper` 的 3x3 空心形。
    - `STEEL.plate()` + `MINGRADE.ingot()` 的 `"PRP"` 形；现代端暂按当前 `ingot_copper` 承接旧 MINGRADE。
  - `battery_sc` 的非空变体通过空壳 + 两个对应 billet 的 shapeless 配方转换。
  - `battery_sc.EMPTY` 本体配方依赖 `GOLD.wireFine()`，现代注册里目前没有精确金细线物品。
  - 更高阶 `battery_pack` / capacitor 不是普通工作台配方：
    - `BATTERY_LEAD`、`BATTERY_LITHIUM`、`BATTERY_SODIUM`、`BATTERY_SCHRABIDIUM`、`BATTERY_QUANTUM` 来自 `ChemicalPlantRecipes`。
    - `CAPACITOR_GOLD`、`CAPACITOR_NIOBIUM`、`CAPACITOR_TANTALUM`、`CAPACITOR_BISMUTH`、`CAPACITOR_SPARK` 来自 `AssemblyMachineRecipes`。
- 本批接入：
  - 新增 `HbmRecipeProvider`，接入 `HbmDataGenerators` server datagen。
  - 生成 13 个普通 crafting recipe：
    - `energy/machine_battery_socket_polymer`
    - `energy/machine_battery_socket_steel`
    - `energy/battery_redstone`
    - `energy/capacitor_copper`
    - `energy/battery_sc_waste`
    - `energy/battery_sc_ra226`
    - `energy/battery_sc_tc99`
    - `energy/battery_sc_co60`
    - `energy/battery_sc_pu238`
    - `energy/battery_sc_po210`
    - `energy/battery_sc_au198`
    - `energy/battery_sc_pb209`
    - `energy/battery_sc_am241`
  - 新增 `HbmLegacyBatteryMaps`，集中保存：
    - `battery_pack` 旧 meta `0..11` -> 现代 12 个独立 ID。
    - `battery_sc` 旧 meta `0..9` -> 现代 10 个独立 ID。
  - `HbmRecipeProvider` 使用该映射表产出 `BATTERY_REDSTONE`、`CAPACITOR_COPPER` 和全部 `battery_sc` 转换配方，避免后续配方/loot/starter kit/机器配方各自重复 switch。
- 当前限制：
  - `battery_sc.EMPTY` 工作台配方暂缓，原因是旧 `GOLD.wireFine()` 没有现代精确注册；不能用 `coil_gold` 伪代。
  - 化工厂/装配机中的高阶电池与电容配方暂缓，原因是 `recipes-common-loader` / `machine-module-recipe-runtime` 已记录但现代端还没有完整 HBM 机器配方 runtime；本轮只保留旧 meta 映射入口供后续机器配方库使用。
  - `battery_sc.PO210` 旧 `PO210.billet()` 暂映射到现代已存在的 `billet_polonium`，不使用 `billet_po210be`，因为后者是铍源相关变体，不等价于旧 PO210 billet。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过，生成 `src/generated/resources/data/hbm/recipes/energy/*.json`。
  - 抽查 `battery_redstone.json`、`battery_sc_po210.json`、`machine_battery_socket_polymer.json`，结果 ID 与旧配方目标一致。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 当前移植估算：
  - 旧普通 crafting 配方与 meta 映射入口补齐后，能量库约 `88%`。
  - 剩余重点：`battery_sc.EMPTY` 的金细线前置材料库、化工厂/装配机/等离子锻炉机器配方 runtime、`battery_sc` 放电事件、创造电池插座特效、OC/ROR/look overlay/EnergyControl 完整兼容、真实跨区块/卸载生命周期验证。

## 2026-05-22 继续推进：高阶电池机器配方首个数据包落点

- 1.7.10 对照：
  - `ChemicalPlantRecipes#chem.batterylead`
    - `setup(100, 100)`
    - 输入 item：`STEEL.plate() x4`、`PB.ingot() x4`
    - 输入 fluid：`SULFURIC_ACID 8000`
    - 输出 item：`battery_pack` meta `BATTERY_LEAD`
- 本批接入：
  - 借助新增 `GenericMachineRecipe` / `hbm:chemical_plant` serializer 生成：
    - `data/hbm/recipes/chemical_plant/batterylead.json`
  - 输出使用 `HbmLegacyBatteryMaps` 的 `battery_pack` meta `1` -> `battery_lead` 映射。
  - 生成 JSON 保留旧 internal name `chem.batterylead`、duration `100`、power `100`。
- 同批构建修正：
  - 给并行迁移中的 `LegacyVisibleMachineBlockEntity` 补 `LEGACY_VISIBLE_MACHINE` BlockEntityType 注册，绑定 `machine_chemical_plant`、`machine_chemical_factory`、`machine_refinery`、`machine_fluidtank`。
  - 注册 `LegacyVisibleMachineRenderer`，让这些可视多方块机器走已有 render-library 桥。
- 当前限制：
  - `BATTERY_LITHIUM`、`BATTERY_SODIUM`、`BATTERY_SCHRABIDIUM`、`BATTERY_QUANTUM` 仍暂缓；旧配方输入中存在 `LI.dust()`、`NA.dust()`、`SA326.dust()`、`BSCCO.wireDense()`、`pellet_charged` 等现代端尚未完整/精确注册的材料或 wire/meta 族。
  - 装配机电容配方仍暂缓；同样受 `wire_dense` 材料族、`circuit` meta、cast plate/tag 映射限制。
  - `GenericMachineRecipe#matches` 暂不执行机器运行时逻辑，需等 `machine-module-recipe-runtime` 后续接入。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 抽查 `src/generated/resources/data/hbm/recipes/chemical_plant/batterylead.json`，字段与旧配方一致。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 当前移植估算：
  - 机器配方数据层和首个高阶电池配方落地后，能量库约 `89%`。
  - 剩余重点：材料/tag/旧 meta 族映射、化工厂/装配机 runtime、`battery_sc.EMPTY` 精确材料配方、`battery_sc` 放电事件、创造电池插座特效和兼容层。

## 2026-05-22 继续推进：高阶电池/电容底层材料与机器配方批量补齐

- 1.7.10 对照：
  - `ChemicalPlantRecipes` 的电池段共 5 条：
    - `chem.batterylead`：`STEEL.plate x4`、`PB.ingot x4`、`SULFURIC_ACID 8000` -> `BATTERY_LEAD`。
    - `chem.batterylithium`：`LI.dust x12`、`CO.dust x8`、`ANY_PLASTIC.ingot x4`、`OXYGEN 2000` -> `BATTERY_LITHIUM`。
    - `chem.batterysodium`：`NA.dust x24`、`IRON.dust x24`、`ANY_HARDPLASTIC.ingot x12` -> `BATTERY_SODIUM`。
    - `chem.batteryschrabidium`：`SA326.dust x24`、`ANY_BISMOIDBRONZE.plateCast x8`、`HELIUM4 8000` -> `BATTERY_SCHRABIDIUM`。
    - `chem.batteryquantum`：`BSCCO.wireDense x24`、`pellet_charged x32`、`ingot_cft x16`、`PERFLUOROMETHYL_COLD 8000` -> `BATTERY_QUANTUM` + `PERFLUOROMETHYL 8000`。
  - `AssemblyMachineRecipes` 的电容段共 5 条：
    - `ass.capacitorgold`：`STEEL.plate x8`、`GOLD.wireDense x16` -> `CAPACITOR_GOLD`。
    - `ass.capacitorniobium`：`ANY_PLASTIC.ingot x12`、`NB.wireDense x24` -> `CAPACITOR_NIOBIUM`。
    - `ass.capacitortantalum`：`ANY_HARDPLASTIC.ingot x16`、`TA.ingot x24` -> `CAPACITOR_TANTALUM`。
    - `ass.capacitorbismuth`：`ANY_HARDPLASTIC.ingot x24`、`BI.ingot x24`、`circuit CHIP_QUANTUM x1` -> `CAPACITOR_BISMUTH`。
    - `ass.capacitorspark`：`CMB.plateCast x12`、`powder_spark_mix x32`、`pellet_charged x32`、`circuit CHIP_QUANTUM x16`、`PERFLUOROMETHYL_COLD 8000` -> `CAPACITOR_SPARK` + `PERFLUOROMETHYL 8000`。
  - 本批底层接入：
  - 注册并拷贝旧贴图的精确旧 ID 材料：
    - `ingot_polymer`、`ingot_bakelite`、`ingot_pc`、`ingot_pvc`
    - `powder_lithium`、`powder_cobalt`、`powder_sodium`、`powder_schrabidium`、`powder_spark_mix`
    - `ingot_bismuth`、`ingot_niobium`、`ingot_tantalium`
    - `pellet_charged`
  - 对旧 autogen/meta 族建立现代拆分落点：
    - `wire_gold` 对应旧 `wire_fine` 的 GOLD meta，模型绑定旧 `wire_gold.png`。
    - `wire_dense_gold`、`wire_dense_niobium`、`wire_dense_bscco` 对应旧 `wire_dense` meta 族，模型共用旧 `wire_dense.png`。
    - `plate_cast_bismuth_bronze`、`plate_cast_arsenic_bronze`、`plate_cast_combine_steel` 对应旧 `plate_cast` meta 族，模型共用旧 `plate_cast.png`。
    - `circuit_chip_quantum` 对应旧 `circuit` meta `EnumCircuitType.CHIP_QUANTUM`，模型绑定旧 `circuit.chip_quantum.png`。
  - `HbmItemTagsProvider` 增加首批 ore dict -> Forge tag 桥：
    - `forge:ingots/any_plastic` = `ingot_polymer` + `ingot_bakelite`。
    - `forge:ingots/any_hardplastic` = `ingot_pc` + `ingot_pvc`。
    - `forge:dusts/lithium|cobalt|sodium|schrabidium`、`forge:dense_wires/*`、`forge:cast_plates/*`、`forge:circuits/chip_quantum`。
  - `HbmItemModelProvider` 增加旧资源名映射，避免 `pellet_charged`、dense wire、cast plate、quantum chip 因现代拆分 ID 找不到同名贴图而透明。
- 本批配方接入：
  - `HbmRecipeProvider` 现在生成：
    - `energy/battery_sc_empty`，用 `ANY_PLASTIC.ingot`、`GOLD.wireFine`、`PB.plate` 对齐旧工作台配方。
    - 5 条 `chemical_plant/battery*.json`。
    - 5 条 `assembly_machine/capacitor*.json`。
  - 所有高阶机器配方保留旧 `internal_name`、`duration`、`power`、流体输入/输出和 `battery_pack` 旧 meta -> 现代独立 ID 映射。
- 当前限制：
  - `wire_dense_*`、`plate_cast_*` 当前是旧 meta/autogen 族的拆分数据落点，视觉先共用旧基础贴图；后续材料库若迁移旧 `NTMMaterial` 调色/材质层，应替换为更完整的材质渲染或生成模型。
  - `circuit_chip_quantum` 只迁了当前能量配方需要的旧 meta；完整 `circuit` meta 族仍应在电路/组装链条迁移时统一补齐。
  - `GenericMachineRecipe#matches` 仍不运行机器处理逻辑，化工厂/装配机 BlockEntity 后续接入 `machine-module-recipe-runtime` 后才会真正消耗输入。
- 本批验证：
  - `.\gradlew.bat runData --no-daemon` 通过。
  - 抽查 `chemical_plant/batteryquantum.json`、`assembly_machine/capacitorspark.json`、`forge:ingots/any_plastic`，字段与旧配方和旧 ore dict 组一致。
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 当前移植估算：
  - 高阶 `battery_pack`/电容配方数据层和底层材料/tag 落点补齐后，能量库约 `92%`。
  - 剩余重点：机器配方 runtime、旧 `NTMMaterial` autogen 族完整迁移、`battery_sc` 放电事件、创造电池插座特效、EnergyControl/info panel/debug particles 兼容层，以及实机跨区块/卸载/网络分裂生命周期验证。

## 2026-05-23 大排查：能量库 1.7.10 语义对齐修正

- 本轮重新对照的 1.7.10 源码：
  - `api/hbm/energymk2/PowerNetMK2.java`
  - `api/hbm/energymk2/IEnergyProviderMK2.java`
  - `api/hbm/energymk2/IEnergyReceiverMK2.java`
  - `api/hbm/energymk2/IEnergyHandlerMK2.java`
  - `api/hbm/energymk2/IBatteryItem.java`
  - `api/hbm/energymk2/Nodespace/NodeNet.java`
  - `api/hbm/energymk2/Nodespace/UniNodespace.java`
  - `api/hbm/energymk2/Nodespace/GenNode.java`
  - `com/hbm/lib/Library.java`
  - `com/hbm/items/tool/ItemBattery.java`
  - `com/hbm/items/tool/ItemBatteryPack.java`
  - `com/hbm/items/tool/ItemBatteryCreative.java`
  - `com/hbm/items/tool/ItemBatterySC.java`
  - `com/hbm/tileentity/machine/TileEntityMachineBattery.java`
  - `com/hbm/tileentity/machine/TileEntityBatteryBase.java`
  - `com/hbm/tileentity/machine/TileEntityBatterySocket.java`
- 已修正的现代端不对齐点：
  - `HbmBatteryItem`：
    - 无 NBT 的旧电池/电容默认是满电；现代端原先把可充电电池默认成空电，已改为默认 `maxCharge`。
    - 旧 `ItemBattery#chargeBattery/dischargeBattery` 本身不做速率限流，速率由 `Library#chargeItemsFromTE/chargeTEFromItems` 调用侧限制；现代端已移除物品方法内部的 charge/discharge rate 二次限流，保留 `HbmBatteryTransfer` 调用侧限流。
  - `HbmPowerNet`：
    - 旧 `NodeNet#joinNetworks` 合网时会把旧网 provider/receiver entries 重新加入新网并刷新时间戳；现代端已在 `HbmPowerNet#joinNetwork` 合并订阅表。
    - 旧 `PowerNetMK2#update` 的优先级循环每个优先级结束后用全局累计 `energyUsed` 扣 `toTransfer`，不是只扣本优先级实际使用量；现代端已按这个外部可见分配结果对齐。
    - 旧普通网络分发会把 `toSend` clamp 到本接收器需求；旧 `sendPowerDiode` 不 clamp 到 entry demand，只依赖接收器返回 leftover；现代端已区分普通分发和 diode 分发。
    - 旧 provider 扣能的加权阶段按计算的 `toUse` 扣 `leftover`，不看 `usePower` 返回值；随机补余阶段最多 100 次且不因 0 使用量提前退出；现代端已按旧结果对齐。`HbmEnergyProvider#usePower` 仍保留现代安全返回值，但核心网络分配不再依赖它改变旧算法结果。
  - `HbmEnergyUtil`：
    - 旧 `tryProvide` 只做导线网络订阅与 `IEnergyReceiverMK2` 直连转移；现代端已要求直连目标同时是 `HbmEnergyConnector` 且连接面可用。
    - 旧 `tryProvide` 没有 Forge Energy 兜底推送；现代端已把 FE 推送从 `tryProvideToNeighbor/tryProvideToAllNeighbors` 旧语义路径移除，只保留 `pushToNeighbor/pushToAllNeighbors` 作为显式现代桥接 API。
  - `MachineBatteryBlockEntity` / `MachineBatterySocketBlockEntity`：
    - 旧 `TileEntityMachineBattery` / `TileEntityBatteryBase` 以 byte ordinal 保存 `priority`；现代端原先只读字符串名，已兼容读取旧 ordinal。
    - 旧电池类每 tick 会把 `LOWEST/HIGHEST` 归一为 `LOW`，GUI 只实际循环 `LOW/NORMAL/HIGH`；现代端已在电池存储 setter 中统一归一。
- 核查后保留的现代结构差异：
  - `HbmEnergyNodespace#popNode` 不逐字照搬旧 `UniNodespace#popNode` 的“销毁整网后等待全局重连”写法，而是移除节点后立即销毁旧网、标记幸存节点 changed，再由同一套 `checkNodeConnection` 重建；行为目标仍是旧端的区块卸载/断线/分网后重建，不额外保留旧实现的无谓等待窗口。
  - `HbmEnergyProvider#usePower` / `HbmEnergyReceiver#transferPower` 保留现代 clamp 和返回值安全语义，但核心 `HbmPowerNet` 分配逻辑已按 1.7.10 计算结果使用，不把现代安全返回值当作旧算法依据。
  - Forge Energy 能力桥仍作为 1.20.1 兼容层存在，但不再被旧 `tryProvide` 语义自动触发；需要 FE 互通的现代机器必须显式调用桥接 API。
- 尚未完全对齐/需要继续推进：
  - `battery_sc` 放电事件与伤害/爆炸/辐射效果仍未完全移植。
  - 创造电池插座的自充电视觉/电弧/爆炸副作用仍是部分实现。
  - OpenComputers / RedstoneOnRails 旧回调未迁移；EnergyControl 当前只有本地 info provider 风格数据键和 `/hbm energy info` 调试入口，尚不是完整外部 mod 兼容层。
  - `HbmEnergyNodespace` 的跨区块、卸载、网络分裂、buffer 模式桥接、订阅超时需要实机验证矩阵；代码路径已更贴近旧端，但还缺世界生命周期回放测试。
  - 流体库 `HbmFluidNet` 仍使用现代化的“本优先级实际使用量”扣减方式；这属于流体库后续单独对照项，本轮只修能量库。
- 本轮验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
- 当前移植估算：
  - 本轮语义对齐修正后，能量库约 `93%`。
  - 下一步优先级：先做 `battery_sc` 放电/副作用，再做创造电池插座特效和 EnergyControl/debug particles 兼容层，最后用实机矩阵压跨区块/卸载/分裂/重建生命周期。

## 2026-05-23 继续推进：`battery_sc` 电池插座放电副作用

- 1.7.10 对照：
  - `ItemBatterySC`：
    - `EnumBatterySC.EMPTY/WASTE/RA226/TC99/CO60/PU238/PO210/AU198/PB209/AM241` 容量分别为 `0/150/200/500/750/1000/1250/1500/2000/2500 HE`。
    - `chargeBattery/setCharge/dischargeBattery` 均为空实现。
    - `getCharge()` 恒等于 `getMaxCharge()`，`getChargeRate()` 为 `0`，`getDischargeRate()` 等于 `getMaxCharge()`。
  - `TileEntityBatterySocket#hasSCLoaded()`：
    - 槽 0 为 `battery_sc` 且 meta 不是 `EMPTY` 时视为加载自充电电池。
  - `TileEntityBatterySocket#updateEntity()`：
    - 非客户端、已加载自充电电池时，若 `damageTarget == 0` 则重新抽取目标时间。
    - `damageTimer++`；达到目标时调用 `discharge()`；每 tick 调用 `fluctuate()`。
    - 目标时间为 `1200 + rand.nextInt(2400)` tick，即 1-3 分钟。
    - `fluctuate()` 每 tick 让 `scPowerMult` 以 `1/100` 步长随机上下波动，并 clamp 到 `0.1..1.0`。
  - `TileEntityBatterySocket#discharge()`：
    - 重新抽取下一次目标时间。
    - 在插座一侧计算放电起点，扫描 15 格内 `EntityLivingBase`，随机顺序对每个目标发射 `EntityBulletBeamBase` 电击束。
    - 电击束配置为 `DamageClass.ELECTRIC`、`50F` 基础伤害、穿透、命中方块会破坏方块并在命中点 `explodeDischarge()`。
    - `explodeDischarge()` 使用半径 5 的 `ExplosionVNT`，实体处理器为 `EntityProcessorCrossSmooth(1, 20).setDamageClass(ELECTRIC)`，播放旧音效 `hbm:entity.ufoBlast`。
- 本批接入：
  - 新增 `HbmEnergyDischargeEffects`：
    - `dischargeSelfChargingSocket(ServerLevel, BlockPos, Direction)` 作为能量库可复用放电效果入口。
    - 按旧参数扫描 15 格内存活生物，随机顺序逐个生成电弧。
    - 电弧命中方块时破坏命中方块并在命中点触发电击爆点；未被方块遮挡时对目标造成 `50F` electric 伤害并触发命中爆点。
    - 电击爆点半径 5，按距离造成最多 `20F` electric 伤害，并生成 `EXPLOSION`/`END_ROD` 服务端粒子。
  - `MachineBatterySocketBlockEntity#tickSelfChargingBattery()` 达到 `damageTarget` 后现在调用 `dischargeSelfChargingBattery()`，不再只是重置计时。
  - 新增 `hbm:electric` damage type 与 `ModDamageSources.electric(Level)`。
  - 从 1.7.10 资源复制旧音效：
    - 旧源 `assets/hbm/sounds/entity/ufoBlast.ogg`
    - 现代落点 `assets/hbm/sounds/entity/ufo_blast.ogg`，使用全小写资源路径。
  - 注册现代 sound event：
    - `hbm:entity.ufo_blast` -> `sounds/entity/ufo_blast.ogg`
  - 更新英文/中文语言与 datagen provider：
    - `death.attack.electric`
    - `subtitles.hbm.entity.ufo_blast`
- 现代等价边界：
  - 旧 `EntityBulletBeamBase` 和 Sedna `BulletConfig` 尚未整体迁移；本批没有创建平行弹丸系统，而是在能量库中实现服务端命中/破坏/爆点/伤害等价层。
  - 旧 `EntityProcessorCrossSmooth` 的节点采样与密度判定未逐字迁移；现代爆点按半径 5、最大 20F、线性衰减执行。后续若 Sedna 弹丸/电击伤害抗性库完整迁移，应把 `HbmEnergyDischargeEffects` 的内部伤害处理替换为统一弹丸/伤害类库。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
  - `.\gradlew.bat runData --no-daemon` 通过，生成语言更新。
- 当前移植估算：
  - `battery_sc` 插座放电副作用落地后，能量库约 `94%`。
  - 剩余重点：创造电池插座更完整视觉/危险副作用、EnergyControl/info panel/debug particles 兼容层、OC/ROR 旧回调、实机跨区块/卸载/分裂/重建生命周期验证。

## 2026-05-23 继续推进：EnergyControl/info/debug particles 兼容层

- 1.7.10 对照：
  - `CompatEnergyControl` 是 EnergyControl 跨 mod 数据桥：
    - `getEnergyData(TileEntity, NBTTagCompound)` 对 `IEnergyHandlerMK2` 写入 `KEY_EUTYPE="HE"`、`L_ENERGY_HE`、`L_CAPACITY_HE`。
    - `getExtraData(TileEntity, NBTTagCompound)` 调用 `IInfoProviderEC#provideExtraInfo`。
    - 常量表包含 `energy/capacity/diff/output/consumption/progress/active/tank/...` 等多个标准 key。
  - `IEnergyHandlerMK2#provideInfoForECMK2` 的默认行为只写 `L_ENERGY_HE` 与 `L_CAPACITY_HE`。
  - `IEnergyHandlerMK2.particleDebug=false` 是旧编译期常量；`IEnergyProviderMK2/IEnergyReceiverMK2#tryProvide/trySubscribe` 只有在开启时才发送 `AuxParticlePacketNT` 调试粒子。
- 本批接入：
  - `CompatEnergyControl` 从 3 个 key 扩展为旧版常量表的现代等价集合，补齐：
    - `KEY_EUTYPE`
    - `L_ENERGY_HE/TU/_`
    - `L_CAPACITY_HE/TU/_`
    - `D_CONSUMPTION_HE/MB`
    - `D_OUTPUT_HE/MB/TU`
    - `L_DIFF_HE`
    - `I_PROGRESS`
    - `B_ACTIVE`
    - 以及 RBMK/热量/流体 tank/旧 deprecated key。
  - 新增现代 helper：
    - `CompatEnergyControl.isElectricItem(ItemStack)`
    - `CompatEnergyControl.dischargeItem(ItemStack, double)`
    - `CompatEnergyControl.getEnergyData(BlockEntity, CompoundTag)`
    - `CompatEnergyControl.getExtraData(BlockEntity, CompoundTag)`
  - `HbmEnergyBlockEntity#provideExtraInfo` 改为调用 `CompatEnergyControl.getEnergyData`，让能量设备信息输出走统一兼容层。
  - `/hbm energy info <pos>` 现在先走 `getEnergyData`，再走 `getExtraData`，并按 key 排序输出，便于对照旧 EnergyControl payload。
  - `HbmEnergyDebug` 的 provider/receiver/direct-transfer 调试粒子从直接 `sendParticles` 改成走现代 `AuxParticlePacket`/`ParticleUtil.TYPE_DEBUG_DRONE`：
    - payload 保留 `role`、`connected`、`side`、`color`。
    - 客户端 `HbmParticleEffects` 的 debug line 读取 `color` 并用 `DustParticleOptions` 渲染彩色线段。
- 现代等价边界：
  - 当前仍是本 mod 内兼容层与调试命令，不是对外部 EnergyControl mod 的正式 IMC/API 集成。
  - `getAllTanks/getFluidTexture/findTileEntity/getHeat` 等 EnergyControl 旧入口尚未补齐；这些需要等流体库、RBMK 与多方块 core lookup 进一步稳定后统一接入。
  - OC/ROR 旧回调仍未迁移。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
- 当前移植估算：
  - EnergyControl/info/debug particles 兼容层补强后，能量库约 `95%`。
  - 剩余重点：外部 EnergyControl 真兼容入口、OC/ROR 旧回调、创造电池插座更完整视觉/危险副作用、实机跨区块/卸载/分裂/重建生命周期验证。

## 2026-05-23 继续推进：OC/ROR 本地兼容语义与 EnergyControl 数据修正

- 1.7.10 对照：
  - `TileEntityMachineBattery`：
    - OC `getComponentName()` 为 `ntm_energy_storage_legacy`。
    - OC `getEnergyInfo/getInfo` 返回 `{power, maxPower}`。
    - ROR 暴露 `VAL:fill`、`VAL:fillpercent`、`VAL:delta` 与 `FUN:setmode`、`FUN:setredmode`、`FUN:setpriority`。
  - `TileEntityBatteryBase`：
    - OC `getComponentName()` 为 `ntm_energy_storage`。
    - OC `getEnergyInfo` 返回 `{power, maxPower}`，`getModeInfo` 返回 `{redLow, redHigh, priority.ordinal() - 1}`，`getInfo` 返回 `{power, maxPower, redLow, redHigh, priority.ordinal() - 1}`。
  - `TileEntityBatterySocket`：
    - ROR 值同电池基类。
    - OC `getEnergyInfo` 返回 `{power, maxPower, delta}`。
    - OC `getPackInfo` 返回 `{unlocalizedName, chargeRate, dischargeRate}`，空/非电池返回 `{"", 0, 0}`。
    - OC `getInfo` 合并 energy/mode/pack 三组数据。
  - `CompatEnergyControl#dischargeItem` 按 `min(dischargeRate, charge, needed)` 限制外部放电量。
- 本批接入：
  - 新增现代本地 ROR API：
    - `com.hbm.ntm.api.redstoneoverradio.RORInfo`
    - `RORValueProvider`
    - `RORInteractive`
    - `RORFunctionException`
  - `MachineBatteryBlockEntity`：
    - 实现 ROR 值读取和 `setmode/setredmode/setpriority`。
    - 补本地 OC 风格 `getEnergyInfo/getInfo` 返回数组。
  - `MachineBatterySocketBlockEntity`：
    - 实现 ROR 值读取和交互函数。
    - 补本地 OC 风格 `getEnergyInfo/getPackInfo/getModeInfo/getInfo`。
  - `/hbm energy ror functions|value|run <pos>` 提供无外部 mod 的旧 ROR 语义验证入口。
  - `HbmEnergyBlockEntity` 实现 `HbmEnergyHandler`，修复通用 EnergyControl 数据导出对基类机器只写 `euType` 的问题。
  - `CompatEnergyControl.dischargeItem` 恢复 1.7.10 外层放电速率限制。
- 现代等价边界：
  - 当前没有引入 OpenComputers 或无线红石方块的外部依赖；只是先把旧 API 语义固定为现代内部接口和可命令验证入口。
  - `MachineBatteryBlockEntity#getInfo()` 保持普通旧电池 `{power, maxPower}`，插座使用旧插座扩展数组。
  - 后续真正迁移 ROR reader/controller 或 OC 外部桥时，应直接调用本批接口。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
- 当前移植估算：
  - OC/ROR 本地语义与 EnergyControl 修正后，能量库约 `96%`。
  - 剩余重点：外部 mod 真桥接、创造电池插座更完整视觉/危险副作用、实机跨区块/卸载/分裂/重建生命周期验证、机器配方 runtime 与材料库尾项。

## 2026-05-23 继续推进：EnergyControl fluid/heat/core helper 尾项

- 1.7.10 对照：
  - `CompatEnergyControl#getAllTanks(TileEntity)` 对 `IFluidUserMK2` 输出 `{fluidName, fill, capacity}`。
  - `CompatEnergyControl#getFluidTexture(String)` 返回 `FluidType#getTexture()`。
  - `CompatEnergyControl#findTileEntity(World,x,y,z)` 通过兼容层解析 dummy/multiblock core。
  - `CompatEnergyControl#getHeat(TileEntity)` 是 EnergyControl 侧热量读取入口，旧端针对 RBMK base。
- 本批接入：
  - `CompatEnergyControl.findTileEntity(BlockEntity/Level, BlockPos)` 可解析现代 `MultiblockDummyBlockEntity` 到 core BlockEntity。
  - `CompatEnergyControl.getAllTanks(BlockEntity)` 可读取现代 `HbmFluidBlockEntity#getAllTanks()`，输出旧 EnergyControl 数组形态。
  - `CompatEnergyControl.getFluidTexture(String)` 可返回现代 `FluidType#getTexture()`。
  - `CompatEnergyControl.getHeat(BlockEntity)` 可读取现代 `HeatSource`，无热源返回 `-1`。
  - `BoilerBlockEntity` 实现 `HeatSource`，为旧 info panel/外部桥提供第一个现代热量读取对象。
  - `/hbm energy info <pos>` 现在会先解析 dummy core，并输出 EnergyControl data、heat、tanks 和 tank texture。
- 复核结果：
  - `ItemBatteryCreative` 旧端 `charge/discharge/setCharge` 全空、`charge=Long.MAX_VALUE/2`、`capacity=Long.MAX_VALUE`、速率 `Long.MAX_VALUE/100`；现代 `HbmCreativeBatteryItem` 已保持该语义，本轮不重复改动。
  - `TileEntityBatterySocket#discharge()` 旧端无论是否命中目标，最后都会在插座附近触发一次随机电爆；现代 `HbmEnergyDischargeEffects#dischargeSelfChargingSocket` 已有同等随机爆点，本轮确认无需补写。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
- 当前移植估算：
  - EnergyControl 旧 helper 尾项落地后，能量库约 `96.5%`。
  - 剩余重点：外部 EnergyControl/OC/ROR 真桥接、创造电池插座更完整客户端视觉、跨区块/卸载/网络分裂生命周期实机验证、机器配方 runtime 与材料库尾项。

## 2026-05-23 继续推进：网络生命周期诊断与强制重建入口

- 1.7.10 对照：
  - `UniNodespace#updateNodespace()` 每 tick 检查无网或 `recentlyChanged` 节点，调用 `checkNodeConnection`。
  - `UniNodeWorld#popNode()` 销毁旧网、移除所有 positions、标记节点 expired；旧网中的剩余节点会因失去有效网络而在后续 update 中重建。
  - `PowerNetMK2` provider/receiver 订阅以 `System.currentTimeMillis()` 时间戳刷新，超过 `3000ms` 移除。
  - `Nodespace.PowerNode` 注释明确提到 recentlyChanged 是为了处理 joining operation 偶发失败后的冗余重查。
- 本批现代接入：
  - `HbmEnergyNodespace.Diagnostics`：
    - `nodePositions`
    - `uniqueNodes`
    - `networks`
    - `invalidNetworks`
    - `linkRefs`
    - `dirtyNodes`
    - `expiredNodes`
    - `orphanNodes`
    - `providerEntries`
    - `receiverEntries`
    - `reapTimer`
  - `/hbm energy nodespace` 现在输出完整诊断，用于实机观察跨区块/卸载/分裂/重建是否留下孤儿节点、脏节点或无效网络。
  - `HbmEnergyNodespace.forceRebuild(Level)`：
    - 销毁当前维度所有 active power nets。
    - 保留仍在 nodespace map 中的节点，清空 net、标记 recentlyChanged，再立即重建。
    - `/hbm energy nodespace rebuild` 暴露该入口，用于实机压测后手动验证网络能否从当前节点集合重建到一致状态。
  - `HbmEnergyNodespace.markNodeAndNeighborsChanged(Level, BlockPos)`：
    - 给目标节点和六向邻接节点标记 changed。
    - `MachineBatteryBlock` 与 `MachineBatterySocketBlock` 的 neighbor change 现在调用该入口，减少机器侧面/红石/邻接网络变化后的等待窗口。
  - `HbmEnergyNodeBlock#neighborChanged` 现在刷新自身节点后也刷新邻接节点，避免线缆连接态改变时只有自身重建而邻居仍保留旧连接。
  - `HbmPowerNet.DebugSnapshot` 增加：
    - `timeoutMs`
    - `oldestProviderAgeMs`
    - `oldestReceiverAgeMs`
    用于观察 3000ms 订阅超时是否按预期刷新/清理。
- 现代等价边界：
  - 强制重建命令是调试/验证入口，不是旧 1.7.10 的玩家功能。
  - 当前仍没有自动化 GameTest 世界矩阵；但命令和诊断字段已经足够实机记录 buffer 桥接、跨区块、卸载、分网重建和订阅超时。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
- 当前移植估算：
  - 生命周期诊断和重建工具补齐后，能量库约 `97%`。
  - 剩余重点：实机矩阵截图/日志验证、外部 EnergyControl/OC/ROR 真桥接、创造电池插座客户端视觉、机器配方 runtime 与材料库尾项。

## 2026-05-23 继续推进：电容族所需装配机配方 runtime 底层接入

- 本批关联 `machine-module-recipe-runtime`：
  - 新增统一 `GenericMachineRecipeRuntime`，用于承载旧 `ModuleMachineBase` 的配方查询、输入/输出检查、消耗和产出。
  - `AssemblyMachineBlockEntity` 现在按旧槽位保存 `recipe0/progress0`，并能处理已迁入 datapack 的 `ass.capacitor*` 配方。
  - 装配机 tank 按旧 `ModuleMachineAssembler#setupTanks` 扩容，`ass.capacitorspark` 的 8000mB perfluoromethyl 输入/输出不再被 4000mB 默认 tank 卡死。
  - 装配机能量容量随 recipe 调整，保持旧 `maxPower = max(power, recipe.power * 100, 100000)` 语义。
- 对能量库影响：
  - 电池/电容物品族的“能量物品 + 配方数据 + 装配机底层处理”链路进一步闭合。
  - 化工厂电池包路线还缺完整化工厂 BE/Menu/recipe selector 接入。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 当前移植估算：
  - 装配机 runtime 底层接入后，能量库约 `97.5%`。
  - 剩余重点：装配机 GUI/recipe selector packet、化工厂 runtime 接入、实机矩阵截图/日志验证、外部 EnergyControl/OC/ROR 真桥接、创造电池插座客户端视觉。

## 2026-05-23 继续推进：装配机电容配方选择入口

- 本批关联 `machine-module-recipe-runtime`：
  - 装配机支持旧 `index/selection` 控制语义，可通过现有 `TileControlPacket` 设置 `recipe0`。
  - 新增 `/hbm machine assembly recipes|info|set|clear`，用于列出/选择/清空/诊断装配机 recipe。
  - 电容族 `ass.capacitor*` 现在可以在实机中通过命令选中并观察 `canProcess/progress/tank/power`。
- 同批修正：
  - 补 `HbmRecipeProvider` 的 `Items` import 与 `inputItem(ItemStack)` builder 重载，使流体包机器配方 datagen 代码重新可编译。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 当前移植估算：
  - 装配机选择入口与实机诊断命令落地后，能量库约 `97.8%`。
  - 剩余重点：装配机正式 Menu/Screen/recipe selector、化工厂 runtime 接入、blueprint pool gating、实机矩阵截图/日志验证、外部 EnergyControl/OC/ROR 真桥接。

## 2026-05-23 继续推进：装配机界面闭环已补齐

- 本批关联 `machine-module-recipe-runtime`：
  - 装配机终于有现代 `Menu` / `Screen` / 选择器屏幕：
    - 主界面按旧纹理显示电量、进度、输入/输出 tank 和 recipe 入口。
    - 选择器按旧纹理支持搜索、翻页和 recipe 选择。
  - `AssemblyMachineBlock#use` 现在会真实开屏，不再只是空的 sidedSuccess。
  - `AssemblyMachineMenu` 继续沿用旧槽位结构和数据同步。
- 对能量库影响：
  - `ass.capacitor*` 电容物品族可以从数据、运行时到交互界面完整走通。
  - 仍未迁移 blueprint pool gating，因此若要继续严格贴旧版，还需补 `ItemBlueprints` 和 pool 选择逻辑。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 当前移植估算：
  - 装配机选择界面闭环落地后，能量库约 `98%`。
  - 剩余重点：化工厂 runtime 接入、blueprint pool gating、实机矩阵截图/日志验证、外部 EnergyControl/OC/ROR 真桥接。

## 2026-05-25 继续推进：远端端口订阅 helper 与化工厂 12 端口接入

- 1.7.10 对照：
  - `IEnergyReceiverMK2#trySubscribe(World, DirPos)` 接收的是旧 `DirPos`：
    - 目标导线坐标 `x/y/z`。
    - 机器相对导线的方向 `dir`。
    - 订阅时检查导线 `canConnect(dir.getOpposite())`，再把 receiver 加入导线节点所在 `PowerNetMK2`。
  - `IEnergyProviderMK2#tryProvide(World,x,y,z,dir)` 同样支持远端坐标，先尝试导线网络订阅，再尝试直连 receiver。
  - `TileEntityMachineChemicalPlant#getConPos()` 不是核心六面，而是核心周围 12 个外圈底面端口：
    - `x+2,z-1/0/+1`，方向 `POS_X`
    - `x-2,z-1/0/+1`，方向 `NEG_X`
    - `x-1/0/+1,z+2`，方向 `POS_Z`
    - `x-1/0/+1,z-2`，方向 `NEG_Z`
- 本批现代接入：
  - `HbmEnergyUtil` 新增通用值对象 `EnergyPort`：
    - `offset`：相对核心的导线/端口坐标。
    - `direction`：旧 `DirPos` 方向，即机器端口朝向导线外侧的方向。
    - `conductorSide()` 自动返回 `direction.getOpposite()`，保持旧 `canConnect(dir.getOpposite())` 语义。
  - `HbmEnergyUtil` 新增远端端口 API：
    - `subscribeProviderToPort`
    - `subscribeReceiverToPort`
    - `subscribeProviderToPorts`
    - `subscribeReceiverToPorts`
    - `unsubscribeProviderFromPort`
    - `unsubscribeReceiverFromPort`
    - `tryProvideToPorts`
  - `HbmEnergyDebug` 新增 remote port provider/receiver 调试粒子入口，payload 标记 `remotePort=true`。
  - `ChemicalPlantBlockEntity` 的服务端 tick 从核心六面订阅改为旧版 12 个外圈 `EnergyPort` 订阅，避免把多方块端口压扁成核心邻接。
- 现代等价边界：
  - 本批只补能量远端端口；流体 MK2 的远端 `IFluidStandardTransceiverMK2` 端口订阅还在流体库后续项。
  - `tryProvideToPorts` 已支持远端导线订阅、HBM receiver 直连和 FE capability 直推；当前化工厂作为 receiver 只使用 `subscribeReceiverToPorts`。
  - 端口列表目前由具体机器按 1.7.10 `getConPos()` 提供；后续可把常见 XR 多方块端口生成器再抽入 multiblock 库。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
- 当前移植估算：
  - 远端端口订阅 helper 落地后，能量库约 `98.3%`。
  - 剩余重点：流体库远端端口配套、实机跨区块/卸载/分裂/重建矩阵、外部 EnergyControl/OC/ROR 真桥接、blueprint/upgrade 等机器依赖库尾项。

## 2026-05-25 继续推进：远端端口 API 下沉到能量基类

- 本批目的：
  - 上一批 `EnergyPort` 仍主要是 `HbmEnergyUtil` 工具函数；后续每台旧多方块机器若直接散调工具函数，容易再次出现核心六面/远端端口混用。
  - 本批把远端端口订阅沉入 `HbmEnergyBlockEntity` / `HbmEnergyNetworkBlockEntity`，让“声明端口列表、库层刷新订阅”成为默认迁移路径。
- 本批现代接入：
  - `HbmEnergyBlockEntity` 新增可覆盖端口入口：
    - `getEnergyPorts()`
    - `subscribeEnergyProviderToPort`
    - `subscribeEnergyReceiverToPort`
    - `subscribeEnergyProviderToPorts`
    - `subscribeEnergyReceiverToPorts`
    - `unsubscribeEnergyProviderFromPort`
    - `unsubscribeEnergyReceiverFromPort`
    - `unsubscribeEnergyProviderFromPorts`
    - `unsubscribeEnergyReceiverFromPorts`
    - `tryProvideEnergyToPorts`
  - `HbmEnergyNetworkBlockEntity#refreshEnergyNetworkSubscriptions()` 现在除自身节点网络外，也会刷新 `getNetworkEnergyPorts()`：
    - provider 机器：本地节点可订阅时加 provider，同时对远端端口执行 provider 订阅。
    - receiver 机器：本地节点可订阅时加 receiver，同时对远端端口执行 receiver 订阅。
  - `getNetworkEnergyPorts()` 默认返回 `getEnergyPorts()`，复杂机器可把“capability 暴露端口”和“网络订阅端口”分开覆盖。
  - `ChemicalPlantBlockEntity` 因 Java 单继承限制仍直接继承 `BlockEntity`，本批保留一个本地小包装调用同一 `HbmEnergyUtil.subscribeReceiverToPorts`，不复制订阅算法。
- 迁移规则更新：
  - 后续若机器能继承 `HbmEnergyBlockEntity` 或 `HbmEnergyNetworkBlockEntity`，应优先覆盖 `getEnergyPorts()` / `getNetworkEnergyPorts()`，而不是在 tick 里手写 `HbmEnergyUtil.subscribe*ToPorts`。
  - 只有像化工厂这种已经需要直接继承别的 BlockEntity 形态、或者需要同时承载独立菜单/流体/渲染状态的机器，才保留本地包装。
- 本批验证：
  - `.\gradlew.bat compileJava --no-daemon` 通过。
- 当前移植估算：
  - 远端端口 API 下沉后，能量库约 `98.5%`。
  - 剩余重点：流体库远端端口配套、实机生命周期矩阵、外部 EnergyControl/OC/ROR 真桥接、upgrade/blueprint 机器依赖库尾项。

## 2026-05-30 修正：`red_cable` 模型与放置/破坏刷新入口复查

- 触发问题：
  - 实机截图中 `red_cable` 显示为粗黑长段，说明现代临时 cube/multipart 模型没有对齐 1.7.10 的电线渲染路径。
- 1.7.10 对照：
  - `com.hbm.blocks.ModBlocks#red_cable`：`new BlockCable(Material.iron)`，贴图名 `hbm:cable_neo`。
  - `com.hbm.blocks.network.BlockCable`：非完整方块，碰撞/选区按 `Library.canConnect` 六向拓展，中心 bounds 为 `5.5px` 到 `10.5px`。
  - `com.hbm.render.block.RenderCable`：
    - 绑定 `ResourceManager.cable_neo`，按 OBJ group 渲染。
    - 普通节点渲染 `Core`，并按连接渲染 `posX/negX/posY/negY/posZ/negZ`。
    - 纯直线时分别渲染 `CX`、`CY`、`CZ`，避免 cube 元素直接铺 `cable_neo.png` 造成错误外观。
  - `com.hbm.tileentity.network.TileEntityCableBaseNT`：
    - 服务端 `updateEntity` 中若节点不存在/过期则通过 `Nodespace.createNode` 创建。
    - `invalidate` 时调用 `Nodespace.destroyNode`。
    - `canConnect` 接受除 `UNKNOWN` 外所有方向。
- 本批现代修正：
  - `RedCableBlock` 改为 `RenderShape.INVISIBLE`，碰撞/选区仍保留旧版 `5.5px-10.5px` 与六向展开。
  - 新增 `RedCableRenderer`，通过现代 `LegacyWavefrontModel` 直渲染 1.7.10 `cable_neo.obj` 的旧 group：
    - `Core`
    - `posX/negX/posY/negY/posZ/negZ`
    - `CX/CY/CZ`
  - 新增 `RedCableBlockItem` 与 `RedCableItemRenderer`，物品栏复用旧 `RenderCable#renderInventoryBlock` 的“核心 + X/Z 四臂”语义，不再使用临时 cube item model。
  - `red_cable` blockstate 改为无条件空模型，仅提供粒子贴图和 blockstate 容器；实际世界渲染由 BER 读取六向 boolean state 决定。
  - `HbmEnergyNodeBlock` 新增 `updateEnergyConnectionGraph(Level, BlockPos)` 公共入口，对齐流体节点库的刷新入口，供后续开关线、二极管、检测器、可染色线在状态变化或工具交互后统一刷新：
    - 更新自身连接 state。
    - 更新邻居连接 state。
    - 刷新自身 nodespace 节点。
    - 刷新邻居 nodespace 节点。
- 现代等价边界：
  - 1.7.10 用 ISBRH 即时渲染；现代用 BlockEntityRenderer 读取同步到客户端的六向 `BlockState`，视觉 contract 与旧 OBJ group 对齐。
  - 连接状态仍由服务端刷新并同步；若后续发现客户端首次看到导线时 state 未及时刷新，应在放置/加载后补一次显式邻居通知，而不是回退到 cube multipart。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 后续实机检查：
  - 单根 `red_cable` 应只渲染旧 `Core`，不再出现粗黑整块。
  - 两根直线相连应走 `CX/CY/CZ` 直线 group；转角/T 字/十字应走 `Core + arms`。
  - 放置/破坏相邻电线后，双方 `north/east/south/west/up/down` 状态和 `/hbm energy network <red_cable_pos>` 的 `nodeConnections` 应同步变化。
  - 后续迁移 `cable_switch`、`cable_detector`、`cable_diode` 时，应走 `HbmEnergyNodeBlock#updateEnergyConnectionGraph`，不要各自复制刷新流程。
