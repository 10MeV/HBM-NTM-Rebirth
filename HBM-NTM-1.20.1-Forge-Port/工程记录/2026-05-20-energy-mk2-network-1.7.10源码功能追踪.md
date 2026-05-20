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
- `HbmPowerNet` 算法验证应覆盖：
  - 高优先级 receiver 先于低优先级 receiver 获得能量。
  - 同优先级 receiver 按需求比例获得能量。
  - provider 扣能按可用输出比例分摊。
  - 订阅超过 3 秒未刷新后被剔除。
