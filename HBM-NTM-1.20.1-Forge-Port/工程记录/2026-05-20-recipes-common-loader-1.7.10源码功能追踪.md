# 配方通用抽象与加载器 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 配方通用输入抽象、可序列化配方、机器配方集合与大量机器配方注册类。
- 该库是后续机器配方迁移的核心数据层。

## 1.7.10 源文件

- `src/main/java/com/hbm/inventory/RecipesCommon.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipe.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipes.java`
- `src/main/java/com/hbm/inventory/recipes/loader/SerializableRecipe.java`
- `src/main/java/com/hbm/inventory/recipes`
- `src/main/java/api/hbm/recipe/IRecipeRegisterListener.java`
- `src/main/java/com/hbm/util/CompatRecipeRegistry.java`

## 旧版契约

- `RecipesCommon.AStack`：
  - 抽象输入项。
  - `matchesRecipe(ItemStack, ignoreSize)`。
  - `copy`、`copy(int)`。
  - `extractForNEI` 与 cycling display。
- `ComparableStack`：
  - item + meta + stacksize。
  - wildcard meta 支持。
  - hash 使用注册名，避免 id 变化。
- `OreDictStack`：
  - 基于 ore dict key 匹配。
- `NBTStack`：
  - 基于 NBT 匹配特殊输入。
- `GenericRecipe`：
  - 支持 item/fluid 输入输出、power、duration、pool、autoSwitchGroup。
- `GenericRecipes`：
  - 保存 recipe list、name map、auto switch groups。
- 大量机器配方类仍是 Java 注册，部分支持 JSON/可序列化。

## 迁移计划

- 先定义现代 `HbmIngredient`，覆盖 item、tag、NBT/data、fluid。
- ore dict 全部映射到 tag。
- `GenericRecipe` 可迁为 codec/datapack，但早期可先保留 Java bootstrap。
- JEI 显示层不要直接依赖旧 NEI handler；应读统一配方数据。

## 验证清单

- Comparable/tag/NBT 输入匹配结果与旧版一致。
- recipe internal name 稳定。
- auto switch group 可被机器模块读取。
- 配方可在服务端和客户端同步。
