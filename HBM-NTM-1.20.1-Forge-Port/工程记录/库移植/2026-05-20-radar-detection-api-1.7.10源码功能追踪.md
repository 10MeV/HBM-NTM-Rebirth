# 雷达检测接口 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 雷达可检测实体接口。
- 该接口服务导弹、炮弹、玩家、火炮和特殊目标扫描。

## 1.7.10 源文件

- `src/main/java/api/hbm/entity/IRadarDetectableNT.java`
- `src/main/java/api/hbm/entity/IRadarDetectable.java`
- `src/main/java/api/hbm/entity/RadarEntry.java`
- 相关实体位于 `src/main/java/com/hbm/entity`
- 相关雷达方块实体位于 `src/main/java/com/hbm/tileentity`

## 旧版契约

- `IRadarDetectableNT` 等级常量：
  - `TIER0` 到 `TIER20`
  - `TIER_AB`
  - `PLAYER`
  - `ARTY`
  - `SPECIAL`
- 核心方法：
  - `getUnlocalizedName()`：雷达显示名。
  - `getBlipLevel()`：雷达点类型和 tier 模式红石等级。
  - `canBeSeenBy(Object radar)`：指定雷达是否可见。
  - `paramsApplicable(RadarScanParams params)`：当前扫描参数是否匹配。
  - `suppliesRedstone(RadarScanParams params)`：是否计入红石输出。
- `RadarScanParams` 包含：
  - scanMissiles
  - scanShells
  - scanPlayers
  - smartMode

## 迁移计划

- 先迁移接口与扫描参数，实体迁移时逐个实现。
- `Object radar` 应在现代端替换为明确的 radar context。
- tier 与红石输出要保留，避免雷达机器行为漂移。

## 验证清单

- 导弹、炮弹、玩家可按开关过滤。
- smartMode 过滤逻辑与旧版一致。
- 红石输出只统计 `suppliesRedstone` 为 true 的目标。
