# UNINOS 节点网络 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 通用节点网络框架 UNINOS。
- 该库为能量、流体、气动、等离子、Klystron、铸造、rebar 等网络提供共同生命周期。

## 1.7.10 源文件

- `src/main/java/com/hbm/uninos/NodeNet.java`
- `src/main/java/com/hbm/uninos/GenNode.java`
- `src/main/java/com/hbm/uninos/UniNodespace.java`
- `src/main/java/com/hbm/uninos/INetworkProvider.java`
- `src/main/java/com/hbm/uninos/networkproviders/PowerNetProvider.java`
- `src/main/java/com/hbm/uninos/networkproviders/FluidNetProvider.java`
- `src/main/java/com/hbm/uninos/networkproviders/PneumaticNetwork.java`
- `src/main/java/com/hbm/uninos/networkproviders/PneumaticNetworkProvider.java`
- `src/main/java/com/hbm/uninos/networkproviders/PlasmaNetwork.java`
- `src/main/java/com/hbm/uninos/networkproviders/KlystronNetwork.java`
- `src/main/java/com/hbm/uninos/networkproviders/FoundryNetwork.java`
- `src/main/java/com/hbm/uninos/networkproviders/RebarNetwork.java`

## 旧版契约

- `NodeNet<R, P, L extends GenNode>`：
  - `links` 保存导线/管道节点。
  - `receiverEntries` 与 `providerEntries` 保存订阅者及最近更新时间。
  - 构造时加入 `UniNodespace.activeNodeNets`。
  - `joinNetworks` 合并两个网络，迁移 links、receiver、provider。
  - `joinLink`、`forceJoinLink`、`leaveLink` 维护节点归属。
  - `invalidate`、`destroy` 负责失效和清空。
- `isBadLink`：
  - `ILoadedTile` 返回未加载时视为坏链接。
  - 旧 `TileEntity.isInvalid()` 也会剔除。
- `UniNodespace`：
  - 维护 active node nets。
  - 各 network provider 负责具体网络发现、更新和构建。
- 泛型设计服务多种网络，实际类型安全较弱，旧代码中有 unchecked cast。

## 迁移计划

- 已开始建立现代 UNINOS 替代层：
  - `src/main/java/com/hbm/ntm/uninos/HbmNetworkNode.java`
  - `src/main/java/com/hbm/ntm/uninos/HbmNodeNet.java`
  - `src/main/java/com/hbm/ntm/uninos/HbmNetworkProvider.java`
  - `src/main/java/com/hbm/ntm/uninos/HbmNodespace.java`
- 能量/流体保留既有外部 API：
  - `com.hbm.ntm.energy.HbmNetworkNode`、`HbmNodeNet` 变为兼容外壳，避免现有机器代码大面积改包名。
  - `HbmEnergyNodespace` 委托通用 `HbmNodespace<BlockPos, HbmEnergyNode, HbmPowerNet>`。
  - `HbmFluidNodespace` 委托通用 `HbmNodespace<NodeKey, HbmFluidNode, HbmFluidNet>`，`NodeKey = (BlockPos, FluidType)`。
- 本轮迁移保留旧版核心行为：
  - 节点可有多个 positions，对齐旧 `GenNode.positions`。
  - 节点以 connection point 发现邻居，对齐旧 `DirPos connections`。
  - 合网时按 link 数选择较大网络承接较小网络。
  - 节点删除、区块卸载、维度卸载都会清理节点和网络引用。
  - 网络空置/失效按 5 分钟 reap timer 清理。
- 本轮刻意保留的现代差异：
  - 活跃网络不再是旧版全局 `UniNodespace.activeNodeNets`，而是由每个 `HbmNodespace` 按 dimension key 隔离。
  - 旧 `INetworkProvider#provideNetwork()` 被现代 `HbmNetworkProvider#provideNetwork(seedNode)` 替代，让流体网络能按 seed node 的 `FluidType` 创建。
  - `isBadLink` 的具体订阅者有效性仍由 `HbmPowerNet`/`HbmFluidNet` 自己判断；能量端已经检查 `HbmLoadedEnergy` 与 `BlockEntity#isRemoved`，流体端目前仍是非空检查，后续迁流体方块实体 loaded contract 时补齐。
- 后续应继续：
  - 把更多直接引用 `com.hbm.ntm.energy.HbmNetworkNode/HbmNodeNet` 的新网络逐步改到 `com.hbm.ntm.uninos`。
  - 为气动、等离子、Klystron、Foundry、rebar 新建 provider/net/node 薄层时复用 `HbmNodespace`。
  - 如果流体出现多方块端口或多位置管线，直接使用新增的 `HbmFluidNode(Set<BlockPos>, FluidType, Set<Direction>)` 构造器。

## 旧版 network provider 事实补充

- `PowerNetProvider`：只负责 `new PowerNetMK2()`。
- `FluidNetProvider`：保存 `FluidType type`，`provideNetwork()` 返回 `new FluidNetMK2(type)`。
- `PlasmaNetwork` / `KlystronNetwork` / `FoundryNetwork` / `RebarNetwork`：
  - 均直接继承旧 `NodeNet`。
  - `update()` 为空。
  - 玩法逻辑主要读取 `links`、`receiverEntries`、`providerEntries` 和节点有效性。
- `PlasmaNetworkProvider` / `KlystronNetworkProvider` / `FoundryNetworkProvider` / `RebarNetworkProvider`：
  - 均提供静态 `THE_PROVIDER`。
  - `provideNetwork()` 只返回对应空更新 network。
- `PneumaticNetwork`：
  - 不只是拓扑网络，还包含物品抽取/插入、过滤器、round-robin/random 接收顺序、slot access、range check、`StackCache`/`ISlotMonitorProvider` 存储缓存联动。
  - 本轮不直接迁实现，避免用不完整现代库存/过滤契约替代旧行为。

## 2026-05-24 第二轮实现记录

- 新增 `HbmSubscribableNodeNet<R, P, L>`：
  - 承接旧 `NodeNet.receiverEntries` / `providerEntries`。
  - 合网时迁移 provider/receiver 订阅表。
  - 默认按 `BlockEntity#isRemoved` 与超时剔除订阅者。
- 新增 `com.hbm.ntm.uninos.networkproviders`：
  - `PowerNetProvider`
  - `FluidNetProvider`
  - `PlasmaNetwork` / `PlasmaNode` / `PlasmaNodespace` / `PlasmaNetworkProvider`
  - `KlystronNetwork` / `KlystronNode` / `KlystronNodespace` / `KlystronNetworkProvider`
  - `FoundryNetwork` / `FoundryNode` / `FoundryNodespace` / `FoundryNetworkProvider`
  - `RebarNetwork` / `RebarNode` / `RebarNodespace` / `RebarNetworkProvider`
- 新增 `com.hbm.ntm.uninos.typed`：
  - `HbmTypedNetworkNode<T>`
  - `HbmTypedNodespace<T, N, NET>`
  - 当前作为后续按类型区分的网络样板/工具层，暂未强行替换已有能量/流体包装层。
- `CommonForgeEvents` 已接入 Plasma/Klystron/Foundry/Rebar nodespace：
  - server tick 更新。
  - chunk unload 清理。
  - level unload 清理。
- 已执行 `gradlew.bat compileJava --no-daemon`，结果通过。

## 2026-05-24 第三轮实现记录

- 新增 `HbmUninosNodespaces`：
  - 聚合 Plasma/Klystron/Foundry/Rebar 的 `tick`、`unloadChunk`、`unloadLevel`。
  - `CommonForgeEvents` 改为调用聚合入口，后续继续加入 Pneumatic 或更多 UNINOS 网络时不需要继续膨胀事件类。
- 新增 `HbmUninosDiagnostics`：
  - 聚合四个现代拓扑型 UNINOS nodespace 的节点位置数、唯一节点数、网络数、invalid network、link refs、dirty/expired/orphan 节点、provider/receiver 订阅数。
  - 各 `*Nodespace` 暴露 `diagnostics(Level)`。
- `/hbm network uninos` 已接入：
  - 输出 UNINOS 总计。
  - 分项输出 `plasma`、`klystron`、`foundry`、`rebar`。
- 已执行 `gradlew.bat compileJava --no-daemon`，结果通过。

## Pneumatic 专项边界

- 当前 clean port 已经有现代 `ItemStackHandler`、`IItemHandler`、`ForgeCapabilities.ITEM_HANDLER` 使用点，但没有旧 `StackCache`、`SlotMonitor`、气动管过滤器与 storage cache 的现代等价层。
- 旧 `PneumaticNetwork#send` 依赖：
  - 源/目标库存 slot 顺序。
  - sided inventory 可抽取/可插入规则。
  - 管道白/黑名单过滤。
  - receiver 轮询顺序与范围检查。
  - `TileEntityMachineAutocrafter` 的特殊单件硬上限。
  - `StackCache` / `ISlotMonitorProvider` 缓存联动。
- 下一步迁 Pneumatic 前，应先为现代端记录并实现这些库存/过滤契约；不能只用一个粗略 `IItemHandler` 循环替代旧行为。

## 2026-05-24 第四轮实现记录：Pneumatic 运行时核心

- 新增 `com.hbm.ntm.uninos.networkproviders.pneumatic`：
  - `PneumaticNetwork`
  - `PneumaticNode`
  - `PneumaticNodespace`
  - `PneumaticNetworkProvider`
  - `PneumaticEndpoint`
  - `PneumaticItemAccess`
  - `PneumaticReceiver`
  - `PneumaticStackCache`
  - `PneumaticSlotMonitor`
  - `PneumaticSlotMonitorProvider`
- 本轮迁移的旧版契约：
  - `SEND_FIRST` / `SEND_LAST` / `SEND_RANDOM`。
  - `RECEIVE_ROBIN` / `RECEIVE_RANDOM`。
  - `ITEMS_PER_TRANSFER = 64` 的“质量”传输预算。
  - 按 stack max size 计算 proportional mass：不可堆叠物品质量为 64，小堆叠物品质量更高。
  - receiver 超时清理，旧版为 1000 ms。
  - receiver round-robin 时按源端距离排序，距离相同用旧 `TileEntityPneumoTube#getIdentifier` 公式排序。
  - source filter 与 receiver endpoint filter 均遵循旧白/黑名单逻辑：`matchesFilter(stack) == whitelist` 才通过。
  - 优先填充兼容的已有堆，再填空槽。
  - `StackCache` / `SlotMonitor` / `SlotMonitorProvider` 的 cache join、availability change、stack type/amount change 更新路径。
- 现代化调整：
  - 库存访问改为 `IItemHandler`，通过 simulate insert/extract 服从现代 sided handler 或 wrapper 的规则。
  - `PneumaticEndpoint` 提供过滤、白名单、位置、loaded 状态和特殊 hard cap 钩子。
  - `PneumaticItemAccess` 保存 handler 和可选世界位置；如果源或目标位置缺失，则跳过距离限制，保留旧版“非 TileEntity inventory 可工作”的宽松语义。
  - `PneumaticNodespace` 已接入 `HbmUninosNodespaces` tick/unload 聚合。
  - `HbmUninosDiagnostics` 已包含 `pneumatic` 分项，`/hbm network uninos` 会显示。
- 尚未迁移/接线：
  - 现代 `PneumoTube` 方块、BlockEntity、Menu、Screen、过滤 GUI 与 NBT。
  - 空气流体压缩、声音、红石逻辑、压力范围控制。
  - 旧 `TileEntityMachineAutocrafter` 特殊 hard cap 目前通过 `PneumaticEndpoint#itemHardCap()` 钩子保留接入点，具体机器接线时实现。
- 已执行 `gradlew.bat compileJava --no-daemon`：
  - 当前编译失败于并行爆炸/方块注册缺口：`BlockMutatorDigamma` 引用未注册的 `ModBlocks.PRIBRIS_DIGAMMA`、`FIRE_DIGAMMA`、`ASH_DIGAMMA`。
  - 本轮未修改该爆炸/方块注册线。

## 2026-05-24 第五轮实现记录：Pneumatic 接线工具与诊断

- 新增 `PneumaticUtil`：
  - `itemAccess(Level, BlockPos, Direction)` 通过 `ForgeCapabilities.ITEM_HANDLER` 解析现代库存 capability，并返回带位置的 `PneumaticItemAccess`。
  - `sourceAccess(Level, BlockPos, Direction)` 对齐旧 `TileEntityPneumoTube` 的 insertion direction：从管道相邻方块按反向 side 抽取。
  - `receiver(Level, BlockPos, Direction, PneumaticEndpoint)` 对齐旧 ejection direction：向管道相邻方块按反向 side 插入。
  - `rangeForPressure(int)` 记录旧压力范围映射：`0 -> 0`、`1 -> 10`、`2 -> 25`、`3 -> 100`、`4 -> 250`、`5 -> 1000`。
  - `identifier(BlockPos)` 暴露旧 `TileEntityPneumoTube#getIdentifier` 排序公式，`PneumaticNetwork` 的 receiver tie-break 改为复用该工具。
- `PneumaticNetwork` 新增 `DebugSnapshot`：
  - 输出 valid、links、receivers、accessors、storages、lastTransfer、timeoutMs。
  - snapshot 会先清理 stale receiver 与过期 cache/storage，避免诊断显示陈旧计数。
- `PneumaticNodespace` 新增：
  - `getNetwork`、`getNetworkDebugSnapshot`、`hasValidNetwork`、`describeNodeConnections`。
  - diagnostics 改为使用 `PneumaticNetwork.DebugSnapshot` 统计 receiver/cache/storage。
- `/hbm network pneumatic <pos>` 已接入：
  - 可查看指定气动节点的连接方向、网络有效性、link 数、receiver 数、cache/storage 数、timeout 与 lastTransfer。
- 仍然未接入实际 `PneumoTube` BlockEntity/NBT/GUI/声音/空气消耗；本轮只是把后续方块实体可直接调用的 UNINOS 气动库 API 铺好。

## 2026-05-24 第六轮实现记录：Pneumatic Tube 服务端闭环

- 迁移旧注册 ID：
  - `pneumatic_tube` 方块与物品已注册，并加入 machine tab。
  - `pneumatic_tube` BlockEntity 已注册，对应旧 `tileentity_pneumatic_tube` 的现代服务端骨架。
- 新增 `PneumaticTubeBlock`：
  - 非整块、带核心/臂碰撞形状。
  - 空手右键暂按现有 conveyor 样板作为螺丝刀占位交互：普通右键循环 insertion direction，潜行右键循环 ejection direction。
  - 相邻气动管与 input/output 方向会影响碰撞/选择框。
- 新增 `PneumaticTubeBlockEntity`：
  - 15 个过滤槽保存到 NBT，当前匹配语义为 `ItemStack.isSameItemSameTags`；旧 `ModulePatternMatcher` 的模式位与 GUI 尚未接入。
  - 保存 `insertionDir`、`ejectionDir`、`whitelist`、`redstone`、`sendOrder`、`receiveOrder`、`sendCounter`、`soundDelay`。
  - 使用 `HbmFluidTank(HbmFluids.AIR, 4000).withPressure(1)` 保存压缩空气。
  - 作为 `HbmStandardFluidReceiver` 接入现代流体网络，只在 compressor 模式下订阅 AIR receiver，接收速度按旧 `(max-fill)/25` clamp 到 `1..100`。
  - 每 10 tick 在 endpoint 模式下向 `PneumaticNetwork` 注册相邻 receiver。
  - 每 5 tick 在 compressor 模式、红石条件允许、空气 >= 50 时调用 `PneumaticNetwork#send`；成功后消耗 50 mB AIR 并播放旧 `weapon.reload.tubeFwoomp`。
  - 使用 `PneumaticNodespace` 创建/销毁管道拓扑节点，连接目前以相邻 `PneumaticTubeBlockEntity` 为准。
- 新增资源：
  - `assets/hbm/blockstates/pneumatic_tube.json`
  - `assets/hbm/models/block/pneumatic_tube.json`
  - `assets/hbm/models/item/pneumatic_tube.json`
  - `data/hbm/loot_tables/blocks/pneumatic_tube.json`
  - 复制旧音频 `assets/hbm/sounds/weapon/reload/tubeFwoomp.ogg` 并注册 `weapon.reload.tubeFwoomp`。
- 本轮刻意保留的后续项：
  - 旧 `GUIPneumoTube` / `ContainerPneumoTube` 未迁移，过滤槽和控制项暂不能通过正式 GUI 编辑。
  - 旧 `ModulePatternMatcher` 的过滤模式、红石/白名单/压力/send/receive 控制包尚需接菜单与网络控制。
  - 旧自定义管道渲染的 in/out/connector/straight 多段贴图还未恢复；当前模型是可见占位。
  - `pneumatic_tube_paintable` 与 pneumatic storage access/clutter/mono 尚未迁移。

## 验证清单

- 网络合并后旧网络失效，节点 net 指向新网络。
- 节点拆除后从 links 移除。
- provider/receiver 订阅超时能清理。
- 世界卸载时无静态引用泄漏。

## 2026-05-24 首轮实现记录

- 新增现代通用核心 `com.hbm.ntm.uninos`。
- `HbmEnergyNodespace` 和 `HbmFluidNodespace` 已改为通用核心的领域包装层。
- `HbmFluidNet#joinNetwork` 补齐合网时迁移 provider/receiver 订阅表，和 `HbmPowerNet` 行为对齐。
- 已执行 `gradlew.bat compileJava --no-daemon`：
  - UNINOS 相关泛型/继承问题已通过编译阶段。
  - 当前编译仍失败于既有并行工作 `LegacyWasteLeavesBlock` 引用未注册的 `ModBlocks.LEAVES_LAYER` 与 `ModParticleTypes.DEAD_LEAF`，不属于本轮 UNINOS 范围。
