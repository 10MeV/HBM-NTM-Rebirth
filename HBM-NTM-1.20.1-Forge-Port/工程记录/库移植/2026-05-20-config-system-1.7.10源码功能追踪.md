# 配置系统 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 通用配置、机器配置、武器配置、世界生成配置、JSON 动态配置和运行时配置。
- 该库影响几乎所有系统，迁移时必须先确定配置项归属。

## 1.7.10 源文件

- `src/main/java/com/hbm/config/CommonConfig.java`
- `src/main/java/com/hbm/config/GeneralConfig.java`
- `src/main/java/com/hbm/config/ClientConfig.java`
- `src/main/java/com/hbm/config/MachineConfig.java`
- `src/main/java/com/hbm/config/MachineDynConfig.java`
- `src/main/java/com/hbm/config/BombConfig.java`
- `src/main/java/com/hbm/config/WeaponConfig.java`
- `src/main/java/com/hbm/config/RadiationConfig.java`
- `src/main/java/com/hbm/config/WorldConfig.java`
- `src/main/java/com/hbm/config/StructureConfig.java`
- `src/main/java/com/hbm/config/MobConfig.java`
- `src/main/java/com/hbm/config/ToolConfig.java`
- `src/main/java/com/hbm/config/PotionConfig.java`
- `src/main/java/com/hbm/config/VersatileConfig.java`
- `src/main/java/com/hbm/config/RunningConfig.java`
- `src/main/java/com/hbm/config/ServerConfig.java`
- `src/main/java/com/hbm/config/CustomMachineConfigJSON.java`
- `src/main/java/com/hbm/config/FalloutConfigJSON.java`
- `src/main/java/com/hbm/config/ItemPoolConfigJSON.java`

## 旧版契约

- Forge 1.7.10 `.cfg` 配置与自定义 JSON 配置混用。
- `CommonConfig`/`GeneralConfig` 是主入口类之一，被多个系统静态读取。
- `RadiationConfig` 不只控制辐射，也控制污染开关等相关机制。
- `MachineConfig`、`MachineDynConfig` 影响机器速度、能耗、容量、行为开关。
- `BombConfig` 与 `WeaponConfig` 影响爆炸/武器数值。
- `WorldConfig`、`StructureConfig` 影响矿物、结构、地形内容，按迁移策略后置。
- `RunningConfig`/`ServerConfig` 支持 JSON 读写和运行时改值。
- JSON 配置：
  - custom machine 配方/参数。
  - fallout 配置。
  - item pool 配置。

## 迁移计划

- 普通布尔/数值配置迁到 `ForgeConfigSpec`。
- 大型动态配置保留 JSON 或迁到 datapack，不能塞进普通 toml。
- 建立旧配置名到现代配置名的映射表。
- 世界生成配置先记录不启用，等 worldgen 阶段再迁。

## 验证清单

- 服务端配置和客户端配置分离。
- 配置默认值与旧版一致。
- JSON 配置缺失时能生成/使用默认数据。
- 配置变更不会要求客户端读取服务端专有类。

## 2026-06-04 新版源码差异补记

对比 `源码包/old-code/Hbm-s-Nuclear-Tech-GIT-master` 与新版 `源码包/Hbm-s-Nuclear-Tech-GIT-master/Hbm-s-Nuclear-Tech-GIT-master`：

- `RefStrings.VERSION` 从 `1.0.27 BETA (5687)` 更新为 `1.0.27 BETA (5714)`；后续 trace 引用 1.7.10 行为时需标明是否来自 5714 快照。
- `GeneralConfig.enableMachineGravity` 的配置默认值改为 `false`，说明旧端大型机器倾斜/地基检查不再默认启用。
- `ServerConfig` 新增运行时配置 `STRUCTURE_DEBUG=false` 与 `AUTOCAL_MAX_CLOCK=20`，后者是 `radio_autocal` 脚本执行速度上限。
- `WorldConfig` 移除 `geyserWater` 配置，`HbmWorldGen` 同步删除水/蒸汽 geyser 生成路径，并修正 chlorine geyser 使用 `geyserChlorine` 频率。
