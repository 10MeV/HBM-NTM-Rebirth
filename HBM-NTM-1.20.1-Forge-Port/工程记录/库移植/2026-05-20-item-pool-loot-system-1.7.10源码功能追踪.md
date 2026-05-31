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

- 尽量用 1.20.1 loot table/datapack 作为主规则，HBM 只保留一个轻量 pool registry/兼容层。
- 保留旧 pool id，方便结构和卫星系统引用。
- weighted random 可复用现代 `WeightedEntry` 或自有轻量实现。
- 结构战利品与世界生成阶段联动，迁移时可先只建 registry。

## 高版本兼容移植法

现代端不完整照搬 1.7.10 的静态 `ItemPools*` Java 注册。目标结构是 datapack-first：

- 数据包层：
  - 普通随机物品池、结构箱子战利品、空投、红房间、卫星奖励等优先表达为 1.20.1 loot table/datapack JSON。
  - 权重、数量范围、NBT/data 输出和可重载覆盖语义优先使用原版 loot table 能力。
  - 数据生成应从旧 `ItemPools*` 内容迁出稳定 JSON，保留清晰的 loot table id。
- HBM 轻量兼容层：
  - 保留旧 pool id 到现代 loot table id 的映射，供结构、卫星、C130 空投、机器随机产物、书籍/展示等旧调用点使用。
  - 对非箱子场景提供统一抽取入口，例如 `HbmItemPoolRegistry.get(poolId)` 或等价 helper，由内部调用现代 loot table。
  - 旧 metadata 物品、矿辞条目、组件池、pile/pool 嵌套等需要先经过 legacy id/meta 到现代 item/tag/loot table 的映射。
  - 缺失物品或缺失映射必须显式跳过或报错，不得静默变成 air、空栈或错误物品。
  - 如果某个旧池存在 datapack 难以表达的动态条件，再在兼容层中做最小特化，不为普通池重建一套平行 Java pool 系统。

迁移边界：

- 可以由 1.20.1 loot table/datapack 代替：权重抽取、数量范围、普通 item 输出、NBT/data 输出、结构箱子战利品、可重载 JSON 覆盖。
- 仍需 HBM 兼容层保留：旧 pool id 引用、非箱子场景调用、legacy meta/矿辞映射、嵌套池/组件池桥接、旧配置追加/替换语义、缺失内容诊断。

阶段建议：

1. 先建立旧 pool id 到现代 loot table id 的命名规则。
2. 为首批简单 `ItemPoolsSingle`/结构池生成 datapack loot table。
3. 实现轻量 `HbmItemPoolRegistry` 查询入口，让卫星、空投、机器和展示系统不要直接解析 JSON。
4. 再分批处理带 metadata、NBT、矿辞、嵌套 pool 或动态条件的复杂池。
5. 每迁移一个池，记录旧来源类、现代 loot table id、缺失物品和映射策略。

## 验证清单

- 同一 pool id 能返回等价物品集合。
- 权重抽样不会产生空物品。
- JSON 覆盖/追加语义明确。
- 结构和卫星调用的是统一 pool registry。
- datapack reload 后 pool registry 抽取结果与结构/卫星/空投调用一致。
- legacy id/meta 映射缺失时应显式跳过或报错，不得静默抽到错误物品。

## 2026-05-30 追加：战利品池复用 legacy meta 映射的规则

- `LegacyMetaItemMappings` 已在 recipe 库建立，当前首批覆盖 `hbm:battery_pack` 与 `hbm:battery_sc` 两个旧 meta 物品族。
- 后续迁 `ItemPoolsLegacy`、`ItemPoolsComponent`、结构箱子或空投池时，遇到旧 `ItemStack(item, count, meta)` 应优先通过该映射表解析现代物品。
- 如果旧池条目引用尚未登记的 legacy id/meta：
  - 数据生成阶段应报错或在 trace 中显式记录跳过原因。
  - 运行时抽取入口不得把缺失条目变成 air、空栈或错误物品。
- 这条规则与 recipe/hazard 共用同一事实来源，避免 battery/circuit 等拆分物品在战利品池中出现另一套映射。
