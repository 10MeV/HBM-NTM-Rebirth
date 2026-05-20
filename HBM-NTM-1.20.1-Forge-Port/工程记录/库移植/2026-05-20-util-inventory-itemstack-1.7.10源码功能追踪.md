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
