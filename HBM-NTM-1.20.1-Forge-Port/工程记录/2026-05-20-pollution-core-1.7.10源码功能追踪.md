# Pollution 污染核心 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 环境污染系统。
- 该系统独立于辐射核心，但与机器排放、流体 trait、怪物生成、方块破坏和睡眠事件联动。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/pollution/PollutionHandler.java`
- `src/main/java/com/hbm/inventory/fluid/trait/FT_Polluting.java`
- `src/main/java/com/hbm/config/RadiationConfig.java`
- `src/main/java/com/hbm/config/MobConfig.java`
- 相关机器排放调用分布在 `tileentity` 与 `blocks`

## 旧版契约

- 存储：
  - 每个 world 对应 `PollutionPerWorld`。
  - 保存文件：`hbmpollution.dat`。
  - 非主维度使用 `DIM<id>/data`。
  - key 使用 `ChunkCoordIntPair(x >> 6, z >> 6)`，实际是 64x64 区域。
- 类型：
  - soot
  - heavy metal
  - poison
- 基础生成速率：
  - `SOOT_PER_SECOND = 1F / 25F`
  - `HEAVY_METAL_PER_SECOND = 1F / 50F`
  - `POISON_PER_SECOND = 1F / 50F`
- API：
  - `incrementPollution`
  - `decrementPollution`
  - `setPollution`
  - `getPollution`
  - `getPollutionData`
- 更新循环：
  - server tick end。
  - 每 60 tick 扩散/衰减一次。
  - soot 较高时向四邻扩散并快速衰减。
  - heavy metal 缓慢衰减。
  - poison 根据阈值扩散和衰减。
- 额外事件：
  - 世界载入/保存/卸载。
  - living spawn。
  - player sleep。
  - 高污染下世界破坏逻辑。

## 迁移计划

- 使用现代 `SavedData` 或 per-level data 替代手写压缩文件。
- 保留 64x64 区域粒度，除非明确重平衡。
- 污染类型先做 enum 和数据容器，再接机器排放。
- 与辐射配置的旧开关联动要记录，避免配置项丢失。

## 验证清单

- 世界保存/重载后污染数据保留。
- 扩散和衰减周期为 60 tick。
- 机器排放能按倍数写入对应污染类型。
- 污染禁用配置时所有 API 返回安全空值。
