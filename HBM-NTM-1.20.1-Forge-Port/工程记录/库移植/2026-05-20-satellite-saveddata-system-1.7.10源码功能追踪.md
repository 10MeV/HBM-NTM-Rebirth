# 卫星 SavedData 系统 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 卫星世界存档、频率映射、卫星类型和具体卫星行为入口。
- 该库服务雷达、扫描、矿机、激光、relay、mapper 等远程玩法。

## 1.7.10 源文件

- `src/main/java/com/hbm/saveddata/SatelliteSavedData.java`
- `src/main/java/com/hbm/saveddata/satellites/Satellite.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteScanner.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteResonator.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteRelay.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteRadar.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteMiner.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteMapper.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteLunarMiner.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteLaser.java`
- `src/main/java/com/hbm/saveddata/satellites/SatelliteHorizons.java`
- 相关 packet：`SatPanelPacket`、`SatCoordPacket`、`SatLaserPacket`

## 旧版契约

- `SatelliteSavedData`：
  - world saved data 名称：`satellites`。
  - `HashMap<Integer, Satellite> sats`，key 为 frequency。
  - `isFreqTaken` 和 `getSatFromFreq` 查询频率。
- NBT：
  - `satCount`
  - `sat_id_<i>`
  - `sat_data_<i>`
  - `sat_freq_<i>`
- `readFromNBT`：
  - 根据 `sat_id` 调用 `Satellite.create(id)`。
  - 每个 satellite 自行读写 data。
- `getData(World)`：
  - 从 `worldObj.perWorldStorage` 读取/创建。

## 迁移计划

- 使用现代 `SavedData` 替代 `WorldSavedData`。
- 保留频率作为核心 key，NBT 字段需要兼容旧存档语义。
- satellite type id 需要建立现代 registry 或 enum 映射。
- 具体卫星行为后续按类型单独追踪。

## 验证清单

- 创建新世界时自动创建 satellites data。
- 保存/读取后频率和 satellite 类型不变。
- 重复频率被拒绝。
- packet 查询结果与服务端数据一致。
