# 中子节点核心 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 RBMK/石墨堆中子节点与中子流系统。
- 该库是核设施迁移的前置基础，不属于普通辐射核心。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/neutron/NeutronHandler.java`
- `src/main/java/com/hbm/handler/neutron/NeutronNode.java`
- `src/main/java/com/hbm/handler/neutron/NeutronNodeWorld.java`
- `src/main/java/com/hbm/handler/neutron/NeutronStream.java`
- `src/main/java/com/hbm/handler/neutron/RBMKNeutronHandler.java`
- `src/main/java/com/hbm/handler/neutron/PileNeutronHandler.java`
- `src/main/java/api/hbm/block/IPileNeutronReceiver.java`
- RBMK tile 位于 `src/main/java/com/hbm/tileentity/machine/rbmk`

## 旧版契约

- `NeutronNode`：
  - 保存 `NeutronType`、`BlockPos`、来源 `TileEntity`。
  - `data` map 用于缓存 RBMK lid 等额外状态。
- `NeutronStream`：
  - 类型：`DUMMY`、`RBMK`、`PILE`。
  - 保存 origin、fluxQuantity、fluxRatio、vector。
  - 构造后注册到 `NeutronNodeWorld.getOrAddWorld(...).addStream(this)`。
  - `getBlocks(range)` 按 vector 迭代路径上的 BlockPos。
  - `runStreamInteraction` 由具体 stream 实现。
- RBMK 与 Pile 有不同 handler，不能混为一个简单辐射扩散系统。

## 迁移计划

- 先迁移独立数学与数据结构，再接 RBMK tile。
- 旧 `fauxpointtwelve.BlockPos` 替换为现代 `BlockPos`。
- world-level stream registry 应使用现代 level data/tick manager，避免静态跨世界污染。
- fluxQuantity/fluxRatio 数值语义必须保留。

## 验证清单

- 中子流按方向和 range 遍历正确方块。
- RBMK 与 pile 流互不混淆。
- 世界卸载后 stream registry 清空。
