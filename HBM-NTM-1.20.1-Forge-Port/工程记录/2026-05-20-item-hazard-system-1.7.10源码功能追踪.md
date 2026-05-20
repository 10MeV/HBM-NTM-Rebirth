# 物品 Hazard 系统 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 物品危险属性系统。
- 该系统把物品、矿辞、NBT/容器状态与辐射、热、爆炸、石棉、煤尘、digamma、水活性等危险效果连接起来。

## 1.7.10 源文件

- `src/main/java/com/hbm/hazard/HazardSystem.java`
- `src/main/java/com/hbm/hazard/HazardRegistry.java`
- `src/main/java/com/hbm/hazard/HazardData.java`
- `src/main/java/com/hbm/hazard/HazardEntry.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeBase.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeRadiation.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeHot.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeExplosive.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeAsbestos.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeCoal.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeDigamma.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeHydroactive.java`
- `src/main/java/com/hbm/hazard/type/HazardTypeBlinding.java`
- `src/main/java/com/hbm/hazard/modifier`
- `src/main/java/com/hbm/hazard/transformer`

## 旧版契约

- 注册层级：
  - `oreMap`：矿辞字符串到 `HazardData`，最先评估。
  - `itemMap`：Item 到 `HazardData`。
  - `stackMap`：`ComparableStack` 到 `HazardData`，匹配 item/meta。
  - blacklist 支持 stack 与 ore dict。
- 评估顺序：
  - ore dict
  - item
  - item stack
  - transformer pre
  - data entries 展开
  - transformer post
- `HazardData.doesOverride` 可清空之前已收集条目。
- mutex 位掩码用于互斥危险，防止重复叠加。
- modifier：
  - `HazardModifier` 系列根据 NBT、燃料棒状态、RTG、RBMK 等改变危险等级。
- transformer：
  - 容器、ME、NBT 等可在展开前后插入或变换危险条目。
- 应用入口：
  - `applyHazards(ItemStack, EntityLivingBase)`。
  - `updatePlayerInventory` 扫描主背包和护甲。
  - `updateLivingInventory` 扫描实体装备。
  - `updateDroppedItem` 对掉落物应用环境效果。

## 迁移计划

- 现代端应保留 ore/tag、item、stack/data component 三层注册。
- 旧 ore dict 需要映射到 tag。
- modifier/transformer 必须保留为扩展管线，否则容器辐射、燃料棒辐射会丢行为。
- 与 radiation core、hazmat registry、fluid traits 联动时再补具体危险类型细节。

## 验证清单

- 同一物品的 tag、item、stack 危险按旧顺序叠加。
- override 与 mutex 行为正确。
- 掉落物、玩家背包、实体装备都能触发危险。
- 容器内物品危险可由 transformer 传递。

## 2026-05-20 现代库层 Pass 1

本批目标：在继续给具体物品补 hazard 前，先补旧版 `HazardData` / `HazardModifier` / `HazardTransformer` 的现代库层，避免后续燃料棒、RTG、容器、NBT 辐射行为只能写死在物品注册里。

已完成：

- 新增 `com.hbm.ntm.radiation.HazardData`：
  - 保存 `HazardEntry` 列表。
  - 保留 `overrides` 与 `mutexBits` 字段/API。
  - 当前 item 注册管线已可接收 `HazardData`。
- 扩展 `HazardEntry`：
  - 保留 `type()` / `level()` record API，兼容已有 tooltip 与扫描逻辑。
  - 新增 modifier 列表。
  - 新增 `modifiedLevel(stack, holder)`，按旧版 modifier 顺序计算实际 level。
- 新增 `HazardModifier` 接口：
  - `modify(ItemStack stack, LivingEntity holder, float level)`
  - `applyAll(...)`
- 新增两个基础 modifier：
  - `FuelRadiationModifier`：按旧版 `pow(durability, 0.4)` 从 base 插值到 target。
  - `LinearDepletionRadiationModifier`：线性插值，用于后续 RTG/RBMK 类桥接。
- 新增 `HazardTransformer` 接口：
  - `transformPre`
  - `transformPost`
- 新增两个基础 transformer：
  - `NbtRadiationHazardTransformer`：读取旧版 NBT key `hfrHazRadiation` 并追加 radiation entry。
  - `ContainerRadiationHazardTransformer`：扫描旧版 `slot0..slot107` 与现代 `Items` list，汇总内部物品 radiation。
- `HazardRegistry` 改为存储 `HazardData`，并在 `getHazards(stack)` 中执行：
  - transformer pre
  - item data 展开
  - transformer post
- `HazardRegistry#getHazardLevel(stack, type, holder)` 现在返回 modifier 计算后的 hazard level。
- `HazardTooltipUtil` 和玩家库存 hazard tick 已改用 modifier 后数值。
- 新增掉落物 hazard update：
  - `ItemEntity` 加入世界时登记。
  - server level tick 只更新已登记 item entity。
  - hydroactive 掉落物遇水/雨会消失并爆炸。
  - explosive 掉落物燃烧会消失并爆炸。

仍未完成：

- 现代 tag/ore dict 层注册尚未补；当前仍以 item map 为主。
- stack/meta 精确注册尚未补；需要等现代 meta 替代方案或具体 item NBT/data component 确认。
- mutex/override 字段已经保留，但当前只有 item data 展开，尚未完整复刻 ore -> item -> stack 的跨层互斥评估。
- `ContainerRadiationHazardTransformer` 暂时不区分 containment box / bag / crate 的旧版衰减倍率，只先建立容器辐射汇总入口。
- ME transformer 尚未迁移。
- RBMK/RTG/PWR/Watz 的具体 modifier 注册尚未迁移，需等对应物品库落地。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 现代库层 Pass 2

本批继续补旧版 `HazardRegistry` 的注册/评估契约，目标是在迁更多物品条目前先让库层能够承接 ore dict/tag、item、stack 三层数据。

已完成：

- `HazardRegistry` 新增现代 tag 层：
  - `registerTag(TagKey<Item>, HazardType, float)`
  - `registerTag(namespace, path, type, level)`
  - `registerForgeTag(path, type, level)`，用于后续把旧版 ore dict 字符串桥接到 Forge item tags。
- `HazardRegistry` 新增 stack 层：
  - `registerStack(ItemStack, HazardType, float)`
  - `registerStack(ItemStack, HazardData)`
  - 现代 key 暂以 `Item + damageValue` 近似旧版 `ComparableStack item/meta`。
- 新增 blacklist 层：
  - `blacklist(TagKey<Item>)`
  - `blacklist(ItemStack)`
  - 命中 tag 或 stack blacklist 时直接返回空 hazard 列表。
- `getHazards(stack)` 的现代评估顺序调整为：
  - 收集 tag 数据。
  - 收集 item 数据。
  - 收集 stack 数据。
  - 执行 transformer pre。
  - 按收集顺序展开 `HazardData.entries()`。
  - 执行 transformer post。
- `HazardData.overrides` 与 `mutexBits` 现在参与跨 tag/item/stack 层展开：
  - `overrides` 清空已展开条目。
  - `mutexBits` 按旧版位掩码阻止互斥数据重复叠加。
- `HazardData` 新增 `setOverrides(boolean)`，让合并注册时能够保留 override 语义。

仍未完成：

- 旧版 `HazardRegistry.registerItems()` 的 ore dict/tag 条目尚未批量迁入；库入口已就绪。
- 旧版 stack/meta 特例还需要结合现代具体物品的数据组件/NBT 继续补精确映射。
- `ContainerRadiationHazardTransformer` 的 containment box / bag / crate 衰减倍率仍待按旧版容器物品补齐。
- ME transformer、RTG/RBMK/PWR/Watz 具体 modifier 注册仍待后续库/物品批次。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
