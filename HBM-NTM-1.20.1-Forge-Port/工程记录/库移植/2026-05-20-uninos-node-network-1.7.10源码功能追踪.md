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

- 若只迁移能量/流体，可先建简化 `NodeNetwork`。
- 若计划迁移气动、等离子、Klystron、Foundry 等，建议先完整设计现代 UNINOS 替代层。
- 现代端应把 world、dimension、chunk unload 纳入网络 key，避免跨维度静态列表泄漏。
- 旧网络合并算法可参考，但节点发现要改为现代 `BlockPos`、`Level`、`BlockEntity`。

## 验证清单

- 网络合并后旧网络失效，节点 net 指向新网络。
- 节点拆除后从 links 移除。
- provider/receiver 订阅超时能清理。
- 世界卸载时无静态引用泄漏。
