# Item Pool 与战利品池 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 item pool 系统、结构/卫星/红房间等战利品池与 JSON 配置入口。
- 该库服务结构战利品、空投、卫星奖励、机器/书籍展示和随机物品生成。

## 1.7.10 源文件

- `src/main/java/com/hbm/itempool/ItemPool.java`
- `src/main/java/com/hbm/itempool/ItemPoolsSingle.java`
- `src/main/java/com/hbm/itempool/ItemPoolsLegacy.java`
- `src/main/java/com/hbm/itempool/ItemPoolsSatellite.java`
- `src/main/java/com/hbm/itempool/ItemPoolsRedRoom.java`
- `src/main/java/com/hbm/itempool/ItemPoolsPile.java`
- `src/main/java/com/hbm/itempool/ItemPoolsComponent.java`
- `src/main/java/com/hbm/itempool/ItemPoolsC130.java`
- `src/main/java/com/hbm/config/ItemPoolConfigJSON.java`
- `src/main/java/com/hbm/util/LootGenerator.java`
- `src/main/java/com/hbm/util/WeightedRandomObject.java`
- `src/main/java/com/hbm/util/WeightedRandomGeneric.java`

## 旧版契约

- `ItemPool` 表示可随机抽取的物品集合。
- 具体 `ItemPools*` 类按用途注册多个静态池。
- 池内容常包含权重、数量、meta/NBT 或矿辞相关条目。
- `ItemPoolConfigJSON` 支持通过 JSON 修改或加载池内容。
- `LootGenerator` 与 weighted random 工具负责实际抽取和生成。

## 迁移计划

- 优先迁为 datapack loot table 或 HBM 自定义 pool JSON。
- 保留旧 pool id，方便结构和卫星系统引用。
- weighted random 可复用现代 `WeightedEntry` 或自有轻量实现。
- 结构战利品与世界生成阶段联动，迁移时可先只建 registry。

## 验证清单

- 同一 pool id 能返回等价物品集合。
- 权重抽样不会产生空物品。
- JSON 覆盖/追加语义明确。
- 结构和卫星调用的是统一 pool registry。
