# 爆炸框架 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 爆炸算法、核爆射线、VNT 模块化爆炸接口和环境效果。
- 该库高风险，后续迁移应在方块/机器/辐射基础稳定后分阶段进行。

## 1.7.10 源文件

- `src/main/java/com/hbm/explosion/ExplosionNT.java`
- `src/main/java/com/hbm/explosion/ExplosionLarge.java`
- `src/main/java/com/hbm/explosion/ExplosionHurtUtil.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeGeneric.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeAdvanced.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeSmall.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeRayBatched.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeRayParallelized.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeRayBalefire.java`
- `src/main/java/com/hbm/explosion/NukeEnvironmentalEffect.java`
- `src/main/java/com/hbm/explosion/vanillant/ExplosionVNT.java`
- `src/main/java/com/hbm/explosion/vanillant/interfaces`
- `src/main/java/com/hbm/explosion/vanillant/standard`

## 旧版契约

- 普通爆炸与核爆是多套实现并存：
  - `ExplosionNT`、`ExplosionLarge` 处理较常规的大范围破坏。
  - `ExplosionNuke*` 处理核爆、射线、分批或并行计算。
  - `ExplosionFleija`、`ExplosionBalefire`、`ExplosionChaos`、`ExplosionThermo` 等是特殊爆炸。
- VNT 是模块化爆炸框架：
  - `IBlockAllocator` 选择受影响方块。
  - `IBlockProcessor` 执行方块破坏。
  - `IBlockMutator` 修改方块结果，如火、碎片、balefire。
  - `IEntityProcessor` 与 `IPlayerProcessor` 处理实体和玩家。
  - `IDropChanceMutator`、`IFortuneMutator` 处理掉落。
  - `ICustomDamageHandler` 自定义伤害。
  - `IExplosionSFX` 控制视觉/声音效果。
- `ExplosionHurtUtil` 与伤害抗性系统交叉。
- `NukeEnvironmentalEffect` 与辐射、污染、方块替换和 fallout 联动。
- 部分爆炸使用压缩 packet 向客户端同步效果。

## 迁移计划

- 不在早期迁移真实核爆破坏，只先记录接口和数据契约。
- 优先迁 VNT 接口层，因为它能为普通武器爆炸提供现代可组合框架。
- 射线核爆需要单独做性能与线程安全设计，不能直接照搬旧并行代码。
- 环境后效应必须等辐射、污染、方块迁移稳定后接入。

## 验证清单

- 普通爆炸不会在客户端修改世界。
- 方块处理、实体处理、掉落处理可独立组合。
- 高强度爆炸分批执行不阻塞主线程过久。
- 客户端效果 packet 解码后只生成表现，不重复破坏世界。
