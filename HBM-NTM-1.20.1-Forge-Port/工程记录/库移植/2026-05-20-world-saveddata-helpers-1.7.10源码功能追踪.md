# 世界与 SavedData 辅助库 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 世界访问、坐标、子区块、存档数据和迁移辅助。
- 该库为污染、辐射、卫星、世界生成、爆炸、结构提供底层支持。

## 1.7.10 源文件

- `src/main/java/com/hbm/lib/HbmWorld.java`
- `src/main/java/com/hbm/lib/HbmWorldGen.java`
- `src/main/java/com/hbm/world/WorldUtil.java`
- `src/main/java/com/hbm/util/SubChunkKey.java`
- `src/main/java/com/hbm/util/SubChunkSnapshot.java`
- `src/main/java/com/hbm/util/ChunkShapeHelper.java`
- `src/main/java/com/hbm/util/fauxpointtwelve/BlockPos.java`
- `src/main/java/com/hbm/util/fauxpointtwelve/DirPos.java`
- `src/main/java/com/hbm/util/fauxpointtwelve/Rotation.java`
- `src/main/java/com/hbm/saveddata/TomSaveData.java`
- `src/main/java/com/hbm/saveddata/AnnihilatorSavedData.java`
- `src/main/java/com/hbm/handler/BlockMigrations.java`

## 旧版契约

- `fauxpointtwelve` 是 1.7.10 中仿 1.12 的 BlockPos/Rotation 工具，应迁移到原生现代类。
- `SubChunkKey` 和 `SubChunkSnapshot` 服务爆炸、辐射、世界扫描等分块缓存。
- `HbmWorldGen` 和 `HbmWorld` 包含旧世界生成与世界工具，不应早期全量迁移。
- `BlockMigrations` 用于旧方块/metadata 迁移或兼容。
- SavedData：
  - `TomSaveData`、`AnnihilatorSavedData` 是特定玩法数据，作为现代 `SavedData` 迁移参考。

## 迁移计划

- 先迁移坐标/缓存工具中纯算法部分。
- 世界生成逻辑按总迁移策略后置，不在基础库第一批落地。
- `BlockMigrations` 后续与存档迁移统一设计。
- 现代 SavedData 要按 dimension/level 明确生命周期。

## 验证清单

- 坐标转换不出现 x/z 反向。
- subchunk key hash/equals 稳定。
- 世界卸载时缓存释放。
- SavedData markDirty 调用正确。
