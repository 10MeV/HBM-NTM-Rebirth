# 机器模块与配方运行时 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 机器配方运行模块、过滤/模式匹配模块、数字显示模块。
- 该库服务 assembler、chemplant、fusion、plasma、PUREX、precision assembler 等机器。

## 1.7.10 源文件

- `src/main/java/com/hbm/module/ModulePatternMatcher.java`
- `src/main/java/com/hbm/module/ModuleBurnTime.java`
- `src/main/java/com/hbm/module/NumberDisplay.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineBase.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineAssembler.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineChemplant.java`
- `src/main/java/com/hbm/module/machine/ModuleMachineFusion.java`
- `src/main/java/com/hbm/module/machine/ModuleMachinePlasma.java`
- `src/main/java/com/hbm/module/machine/ModuleMachinePrecAss.java`
- `src/main/java/com/hbm/module/machine/ModuleMachinePUREX.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipe.java`
- `src/main/java/com/hbm/inventory/recipes/loader/GenericRecipes.java`

## 旧版契约

- `ModulePatternMatcher`：
  - 模式：`exact`、`wildcard`、`bedrock`、或 ore dict key。
  - smart init 会优先选择 ingot/block/dust/nugget/plate 等矿辞。
  - `nextMode` 在 exact、bedrock、wildcard、矿辞列表间循环。
  - NBT key：`mode<i>`。
  - ByteBuf 同步每个 mode 字符串。
- `ModuleMachineBase`：
  - 保存能量接口、slots、input/output slots、input/output tanks。
  - `recipe` 字符串保存当前 recipe internal name。
  - `progress` 为 0..1。
  - `setupTanks` 按配方约束 tank 类型和压力。
  - `canProcess` 检查自动切换、能量、输入、输出容量。
  - `process` 扣能、推进进度、完成时消耗输入并产出。
  - `update` 处理 blueprint pool、额外条件、progress reset。
  - `isItemValid` 由当前配方和 auto switch group 决定。

## 迁移计划

- 先迁移 `GenericRecipe`/`GenericRecipes` 数据模型，再迁 `ModuleMachineBase`。
- `ModulePatternMatcher` 的 ore dict 模式要映射为 tag。
- ByteBuf 同步改为现代 packet/menu data slot 或自定义 payload。
- 具体机器模块应作为机器迁移时逐个接入。

## 验证清单

- 配方自动切换行为与旧版一致。
- tank 会随配方变化重设目标流体。
- blueprint pool 不匹配时 recipe 归 null 且进度清零。
- filter exact/wildcard/tag/bedrock 模式匹配正确。
