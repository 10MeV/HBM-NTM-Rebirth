# 工具与武器 Ability 系统 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 工具/武器能力接口、能力集合、预设切换。
- 该库服务高级工具、矿镐、电钻、武器 modifier 与 tooltip。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/ability/IBaseAbility.java`
- `src/main/java/com/hbm/handler/ability/IToolAreaAbility.java`
- `src/main/java/com/hbm/handler/ability/IToolHarvestAbility.java`
- `src/main/java/com/hbm/handler/ability/IWeaponAbility.java`
- `src/main/java/com/hbm/handler/ability/AvailableAbilities.java`
- `src/main/java/com/hbm/handler/ability/ToolPreset.java`

## 旧版契约

- `IBaseAbility`：
  - `getName()` 返回翻译 key。
  - `getExtension(level)` 可追加等级文本。
  - `getFullName(level)` 客户端格式化。
  - `isAllowed()` 控制是否启用。
  - `levels()` 默认 1，UI 支持 1..10。
  - `sortOrder()` 用于 tooltip 排序。
- 分类接口：
  - `IToolAreaAbility`
  - `IToolHarvestAbility`
  - `IWeaponAbility`
- `AvailableAbilities`：
  - 保存 ability -> max level。
  - `addToolAbilities` 默认加入 area/harvest none。
  - 可按分类返回 tool/weapon abilities。
  - tooltip 分 tool abilities 和 weapon modifiers。
- `ToolPreset` 负责工具能力组合预设。

## 迁移计划

- 先保留接口和 ability registry，具体工具行为逐个迁移。
- tooltip 客户端逻辑应与服务端能力数据分离。
- levels 与 UI 限制需要保留，避免已有工具预设无法表达。

## 验证清单

- ability 排序稳定。
- 不允许的 ability 不加入集合。
- tooltip 能按工具/武器分类显示。
- preset 切换不越界。
