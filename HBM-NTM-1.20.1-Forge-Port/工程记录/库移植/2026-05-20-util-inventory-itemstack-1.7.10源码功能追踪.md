# 库存与 ItemStack 工具 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 库存塞入、ItemStack 拷贝、NBT 保存、掉落、矿辞查询等工具。
- 这些工具被机器、配方、容器、物流、战利品系统复用。

## 1.7.10 源文件

- `src/main/java/com/hbm/util/InventoryUtil.java`
- `src/main/java/com/hbm/util/ItemStackUtil.java`
- `src/main/java/com/hbm/util/LootGenerator.java`
- `src/main/java/com/hbm/util/WeightedRandomObject.java`
- `src/main/java/com/hbm/util/WeightedRandomGeneric.java`
- `src/main/java/com/hbm/util/Tuple.java`
- `src/main/java/com/hbm/util/Either.java`
- `src/main/java/com/hbm/util/EnumUtil.java`

## 旧版契约

- `InventoryUtil`：
  - `tryAddItemToInventory` 先合并旧 stack，再占用空 slot。
  - `tryAddItemToExistingStack` 只合并已有 stack。
  - `tryAddItemToNewSlot` 只寻找空 slot。
  - 同时支持 `ItemStack[]` 和 `IInventory`。
  - `tryConsumeAStack` 使用 `RecipesCommon.AStack` 消耗配方输入。
  - `doesStackDataMatch` 比较 item、meta、NBT。
- `ItemStackUtil`：
  - careful copy 与数组 copy。
  - tooltip/lore 写入。
  - 多 stack 保存到一个 ItemStack NBT：key `items`，每项含 `slot`。
  - 从 NBT 读取 stack array。
  - 获取 ore dict names。
  - 获取 mod id。
  - spill inventory items。
- 随机/元组工具为配方、战利品和网络分配辅助。

## 迁移计划

- 现代端优先使用 `ItemStack.copyWithCount`、`ItemStack.matches`、`Container`/`IItemHandler`。
- 旧 NBT key `items` 和 `slot` 需保留兼容。
- ore dict 查询迁移为 tag 查询。
- 库存工具要避免直接修改传入 stack 导致调用方误用，必要时文档化 mutability。

## 验证清单

- 合并 stack 时 NBT 不同不会合并。
- 空 slot 填充和剩余返回语义一致。
- NBT 内嵌物品可保存/读取。
- 掉落物数量和随机偏移合理。

## 2026-06-01 GUI/Menu 库接入补丁

- 新增 `HbmInventoryMenuHelper`：
  - `outputSlot(...)`：统一机器输出槽/返回槽 `mayPlace=false`。
  - `addPlayerInventoryAndHotbar(...)`：保留旧坐标由具体 Menu 传入，不抽象 GUI 布局。
  - `moveMachineStack(...)`：提供机器槽到玩家背包、玩家背包到指定机器槽 range 的 shift-click 规则，具体可插入范围仍由机器 Menu 按旧 Container 传入。
  - `saveLegacyItems(...)` / `loadLegacyItems(...)`：集中旧 ItemStack NBT `items` list + 每项 `slot` key 语义，供 toolbox、crate、容器物品和后续旧库存迁移复用。
  - `clearToDrops(...)`：集中清空 `ItemStackHandler` 并返回掉落 stack 的常用逻辑。
- 新增 `HbmMenuDataSlots`：
  - 统一 64-bit long 拆为四个 16-bit DataSlot 的同步写法。
  - 统一 `progress 0..1 -> 0..10000` 的菜单同步约定。
- 已接入 `AssemblyMachineMenu` 与 `ChemicalPlantMenu`，验证不改变旧 slot 坐标。
