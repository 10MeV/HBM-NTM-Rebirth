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

## 2026-05-20 基础物品 + 多 Hazard 条目批次

本批按“先物品库，后 Hazard 库接入”的顺序迁移废料、晶体、核弹组件等基础物品，并使用 Pass 5 增加的复合注册 API 迁移旧版多 hazard 条目。

旧版来源：

- `ModItems` 中对应物品注册：
  - `nuclear_waste_long/short/...`
  - `cell_empty`
  - `cell_sas3`
  - `scrap_nuclear`
  - `trinitite`
  - `gem_rad`
  - `crystal_*`
  - `powder_yellowcake`
  - `fallout`
  - `powder_caesium`
  - `powder_coltan_ore`
  - `boy_propellant`
  - `gadget_core`
  - `boy_target`
  - `boy_bullet`
  - `man_core`
  - `mike_core`
  - `tsar_core`
  - `fleija_propellant`
  - `fleija_core`
  - `solinium_propellant`
  - `solinium_core`
- `HazardRegistry.registerItems()` 中对应 hazard 注册。
- 贴图来源：1.7.10 `assets/hbm/textures/items/*.png`。

已完成：

- `ModItems` 新增基础物品注册：
  - 废料：`nuclear_waste_long`、`nuclear_waste_long_tiny`、`nuclear_waste_short`、`nuclear_waste_short_tiny`、`nuclear_waste_long_depleted`、`nuclear_waste_long_depleted_tiny`、`nuclear_waste_short_depleted`、`nuclear_waste_short_depleted_tiny`、`nuclear_waste`、`nuclear_waste_tiny`、`nuclear_waste_vitrified`、`nuclear_waste_vitrified_tiny`、`scrap_nuclear`、`trinitite`、`gem_rad`。
  - 晶体：`crystal_uranium`、`crystal_thorium`、`crystal_plutonium`、`crystal_schraranium`、`crystal_schrabidium`、`crystal_phosphorus`、`crystal_lithium`、`crystal_trixite`。
  - 粉末/散落物：`powder_yellowcake`、`fallout`、`powder_caesium`、`powder_coltan_ore`。
  - 单元：`cell_empty`、`cell_sas3`。
  - 核弹组件：`boy_propellant`、`gadget_core`、`boy_target`、`boy_bullet`、`man_core`、`mike_core`、`tsar_core`、`fleija_propellant`、`fleija_core`、`solinium_propellant`、`solinium_core`。
- 新增 `ModItems.NUKE_TAB_ITEMS`，并接入 nuke 创造栏、item model datagen、英/中语言 datagen。
- 已从 1.7.10 复制上述存在的 legacy 贴图到现代 `assets/hbm/textures/item/`。
- `HazardRegistry` 新增并调用：
  - `registerLegacyWasteAndCrystalHazards()`
  - `registerLegacyNukePartHazards()`
- 已迁多 hazard 条目：
  - `cell_sas3`：radiation `SAS3` + blinding `60`。
  - `nuclear_waste_short`：radiation `30` + hot `5`。
  - `nuclear_waste_short_tiny`：radiation `3` + hot `5`。
  - `powder_caesium`：hydroactive `1` + hot `3`。
  - `fleija_propellant`：radiation `15` + explosive `8` + blinding `50`。
  - `solinium_core`：radiation `SA327 * nugget * 8` + blinding `45`。

暂未迁移/保留：

- `yellow_barrel` 旧版 hazard 已核对，但本批未迁移：未在 1.7.10 `textures/items/yellow_barrel.png` 找到同名贴图，避免生成假资产。
- `ItemWasteShort` 的 subtype tooltip 与废料分类枚举尚未迁入；本批先以基础物品承接 hazard。
- `ItemFleija` 的 tooltip/rarity 尚未迁入；本批先迁物品 ID、贴图、创造栏与 hazard。
- `cell_sas3` 的 container item 行为暂未迁入，需等流体/容器物品库更完整后补。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 现代库层 Pass 5

本批继续推进库层而非强行添加缺失物品。核对旧版 `registerItems()` 后，现代项目当前只存在少量对应条目，因此先补常量和复合 hazard 注册 API，避免后续迁 `cell_sas3`、短寿命核废料、FLEIJA 推进剂等多 hazard 物品时重复手写。

已完成：

- `RadiationConstants` 补旧版 HazardRegistry 常量：
  - `SA327 = 17.5F`
  - `SAS3 = 5.0F`
  - `SCHRARANIUM = SA326 * 0.1F`
  - `TRIXITE = 25.0F`
  - `FALLOUT = 10.0F`
  - `YELLOWCAKE = U`
- `HazardRegistry` 新增复合条目注册入口：
  - `register(Item, HazardEntry...)`
  - `registerByName(String, HazardEntry...)`
  - `registerBlockByName(String, HazardEntry...)`
- 现有注册清理：
  - `block_schraranium` 改用 `RadiationConstants.SCHRARANIUM`。
  - `block_yellowcake` / `block_fallout` 改用 `RadiationConstants.YELLOWCAKE`，对应旧版 `yc`。

仍未完成：

- 旧版 `nuclear_waste_long/short`、`crystal_*`、`powder_yellowcake`、`fallout` item、核弹组件、holotape 等大量条目在现代项目中尚无对应物品或 subtype，未强行迁入。
- 下一批可在物品库补齐后，用本批新增的复合注册入口迁移多 hazard 条目。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 现代库层 Pass 4

本批补旧版动态辐射 modifier，先把燃料棒/RTG/RBMK 所需公式移入现代 Hazard 库，避免后续注册批次写散。

旧版来源：

- `com.hbm.hazard.modifier.HazardModifierFuelRadiation`
- `com.hbm.hazard.modifier.HazardModifierRTGRadiation`
- `com.hbm.hazard.modifier.HazardModifierRBMKRadiation`
- `com.hbm.hazard.modifier.HazardModifierRBMKHot`
- `HazardRegistry.registerOtherFuel(...)`
- `HazardRegistry.registerRTGPellet(...)`
- `HazardRegistry.registerRBMK(...)`

已完成：

- 新增 `RtgRadiationModifier`：
  - 对齐旧版 RTG pellet 线性耐久衰减公式。
  - 公式：`level + (target - level) * depletion`。
- 新增 `RbmkRadiationModifier`：
  - 支持旧版 `yield` / `xenon` NBT。
  - 额外兼容现代预留 key：`enrichment` / `poison`。
  - 非线性 depletion：`1 - enrichment^2`。
  - 线性 depletion：`1 - enrichment`。
  - xenon 叠加：`RadiationConstants.XE135 * poison`。
- 新增 `RbmkHotModifier`：
  - 支持旧版 `hull` NBT。
  - 额外兼容现代预留 key：`hullHeat`。
  - 热危害公式按旧版 `(hull - 100) / 10` 向上取整，上限 60；现代侧夹到 `0..60`，避免低温负 hazard。
- `RadiationConstants` 新增 `XE135 = 1250.0F`，对齐旧版 `HazardRegistry.xe135`。
- `HazardRegistry` 新增便捷注册入口：
  - `registerFuelRadiation(Item, base, target, blinding)`
  - `registerFuelRadiation(ItemStack, base, target, blinding)`
  - `registerRtgPellet(Item, base, target, hot, blinding)`
  - `registerRbmkFuel(Item, base, depleted, hot, linear, blinding, digamma[, initialYield])`

仍未完成：

- 现代项目尚未迁 `ItemRBMKRod` / `ItemRBMKPellet` / `ItemRTGPellet` 本体，因此本批只补库层和 NBT 兼容公式。
- 旧版 `registerOtherFuel`、`registerRTGPellet`、`registerRBMK*` 的具体条目尚未批量迁入；下一批可开始按现代已注册物品存在情况分组迁移。
- RBMK pellet 的 meta/xenon overlay 逻辑仍需等对应现代 item/subtype 方案确定后接入 stack 层。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 现代库层 Pass 3

本批继续推进旧版 ore dict/tag 语义，避免 `HazardRegistry.registerForgeTag(...)` 只有代码入口但缺少现代 tag 数据承接。

已完成：

- 新增 `HbmItemTagsProvider`，并接入 `HbmDataGenerators`。
- `HbmBlockTagsProvider` 新增一批旧版矿辞到 Forge block tag 的桥接：
  - `forge:ores/uranium`
  - `forge:ores/thorium`
  - `forge:ores/schrabidium`
  - `forge:ores/lignite`
  - `forge:ores/asbestos`
  - `forge:ores/coal`
- `HbmItemTagsProvider` 将对应 block tag copy 到 item tag，并补部分非方块物品 tag：
  - `forge:dusts/lignite` -> `powder_lignite`
  - `forge:dusts` -> `powder_lignite`
  - `forge:gems/coal` -> vanilla coal/charcoal
  - `forge:gems/lignite` -> `lignite` / `coal_infernal`
- `HazardRegistry` 开始使用 tag API 注册旧版煤尘/褐煤 hazard：
  - `forge:dusts/coal` -> `COAL, powder`
  - `forge:dusts/lignite` -> `COAL, powder`
  - `forge:gems/lignite` -> `COAL, ingot`
- `HazardRegistry` 补旧版铀矿/钍矿 blacklist 入口：
  - `forge:ores/thorium`
  - `forge:ores/uranium`

迁移比例粗估：

- Hazard 库框架：约 55%。
  - 已有 data/entry/modifier/transformer 基础、tag/item/stack/blacklist 注册层、override/mutex 展开、背包/掉落物应用入口。
  - 仍缺 ME transformer、容器衰减细分、RTG/RBMK/PWR/Watz 具体 modifier 与一部分长期效果细节。
- `HazardRegistry.registerItems()` 数据迁移：约 20%。
  - 已迁现代项目已有的主要锭/粒/坯/块、若干特殊资源、爆炸/热/煤/水活性/石棉条目。
  - 旧版燃料棒、RTG pellet、RBMK、矿辞全量材料循环、meta stack 特例仍需分批迁移。

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
## 2026-05-21 Container Radiation Transformer 衰减倍率批次

本批继续补 `ContainerRadiationHazardTransformer`，对齐旧版容器内物品辐射泄露倍率，并补最小物品承接。

旧版来源：

- `com.hbm.hazard.transformer.HazardTransformerRadiationContainer`
  - `BlockStorageCrate`：扫描 `slot0..slot103`，内部辐射原样泄露。
  - `ModItems.containment_box`：扫描 20 格，内部辐射经过 `BobMathUtil.squirt(...)` 衰减。
  - `ModItems.plastic_bag`：扫描 1 格，内部辐射乘以 `2F`。
  - `ModItems.toolbox`：扫描 24 格，内部辐射原样泄露。
- `com.hbm.items.tool.ItemLeadBox`
  - 20 格、最大堆叠 1、禁止放入 storage crate。
- `com.hbm.items.tool.ItemPlasticBag`
  - 1 格、最大堆叠 1。
- `com.hbm.util.BobMathUtil.squirt(double)`
  - 公式：`sqrt(x + 1 / ((x + 2) * (x + 2))) - 1 / (x + 2)`。

已完成：

- `ModItems` 新增 `containment_box` 与 `plastic_bag`，放入现代 consumables tab。
- 复制旧版贴图：
  - `textures/items/containment_box.png`
  - `textures/items/plastic_bag.png`
- `ContainerRadiationHazardTransformer` 对现代 `containment_box` 使用 20 格扫描并套用旧版 `squirt` 衰减。
- `ContainerRadiationHazardTransformer` 对现代 `plastic_bag` 使用 1 格扫描并乘以 `2F`。
- 保留原有默认扫描路径，用于尚未专门识别的旧/现代 `slotN` 或 `Items` 容器 NBT 兼容。

仍未完成：

- 现代端尚未迁 `ItemLeadBox` / `ItemPlasticBag` GUI 和实际库存交互，本批只补物品占位、贴图、语言和 hazard transformer 语义。
- crate 104 格、toolbox 24 格的专门识别已由后续 “Container/Crate/Toolbox 识别补齐批次” 补上；实际 storage crate 方块/BlockEntity/Menu 仍待机器/容器库批次迁入。

验证：

- 2026-05-21 运行 `.\gradlew.bat runData --no-daemon` 通过。
- 2026-05-21 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 ItemDepletedFuel meta 子型桥接

本批补齐上一批留下的旧版 `ItemDepletedFuel` meta 0/1 基础契约，让现代 Hazard stack 层真正承接旧版冷热废料语义。

旧版来源：

- `com.hbm.items.machine.ItemDepletedFuel`
  - `setHasSubtypes(true)`，`setMaxDamage(0)`。
  - 创造栏加入 meta 0 和 meta 1 两个 `ItemStack`。
  - meta 1 使用 `0xFFBFA5` 染色。
  - meta 1 tooltip 添加 `desc.item.wasteCooling`。
- `com.hbm.hazard.HazardRegistry`
  - `registerOtherWaste(...)`：meta 0 = `base * 0.075`，meta 1 = `base` + HOT 5。
  - `registerRadSourceWaste(...)`：meta 0 = `base`，meta 1 = `base` + HOT 5。

已完成：

- 新增 `DepletedFuelItem`：
  - 用 `ItemStack#setDamageValue(0/1)` 桥接旧版 meta。
  - `addCreativeStacks(...)` 输出冷态/热态两个 stack。
  - 热态 stack 追加 `desc.item.wasteCooling` tooltip。
  - 隐藏耐久条，避免 damage bridge 在 UI 上表现成损坏物品。
- `ModItems` 中 `waste_*` / `waste_plate_*` 自动注册为 `DepletedFuelItem`。
- `ModCreativeTabs.PARTS` 对 `DepletedFuelItem` 展开两个创造栏 stack。
- `HazardRegistry.registerLegacyDepletedFuelWaste(...)` 和 `registerLegacyRadSourceWaste(...)` 改为 stack 层注册：
  - damage 0：冷态辐射。
  - damage 1：热态辐射 + HOT 5。
- 语言生成补 `desc.item.wasteCooling`。
- 客户端 item color 注册补旧版 meta 1 的 `0xFFBFA5` 热态染色。

仍未完成：

- 这次只处理 `ItemDepletedFuel`，其他旧版 meta item 仍需逐类确认是否适合继续使用 damage bridge，或改用 NBT/DataComponent。

验证：

- 2026-05-20 运行 `.\gradlew.bat runData --no-daemon` 通过。
- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-20 接入客户端 item color 后再次运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 Reactor Component 基础物品 + Hazard 批次

本批继续按旧版 `HazardRegistry.registerItems()` 向后推进，先补旧版 controlTab / partsTab 中和反应堆燃料链相关的基础物品，再接入已迁好的 fuel / RTG hazard modifier 库。

旧版来源：

- `com.hbm.items.ModItems`
  - `waste_natural_uranium` / `waste_uranium` / `waste_thorium` / `waste_mox` / `waste_plutonium` / `waste_u233` / `waste_u235` / `waste_schrabidium` / `waste_zfb_mox`
  - `waste_plate_u233` / `waste_plate_u235` / `waste_plate_mox` / `waste_plate_pu239` / `waste_plate_sa326` / `waste_plate_ra226be` / `waste_plate_pu238be`
  - `plate_fuel_u233` / `plate_fuel_u235` / `plate_fuel_mox` / `plate_fuel_pu239` / `plate_fuel_sa326` / `plate_fuel_ra226be` / `plate_fuel_pu238be`
  - `pile_rod_uranium` / `pile_rod_pu239` / `pile_rod_plutonium` / `pile_rod_source` / `pile_rod_boron` / `pile_rod_lithium` / `pile_rod_detector`
  - `pellet_rtg` / `pellet_rtg_radium` / `pellet_rtg_weak` / `pellet_rtg_strontium` / `pellet_rtg_cobalt` / `pellet_rtg_actinium` / `pellet_rtg_polonium` / `pellet_rtg_americium` / `pellet_rtg_gold` / `pellet_rtg_lead`
- `com.hbm.hazard.HazardRegistry`
  - `registerOtherWaste(...)`
  - `registerRadSourceWaste(...)`
  - `registerOtherFuel(...)`
  - `registerRTGPellet(...)`
- `com.hbm.items.machine.ItemDepletedFuel`
  - 旧版有 meta 0/1 两个子型：0 为冷态低辐射，1 为热态完整辐射 + HOT 5 tooltip。

已完成：

- `ModItems` 新增上述基础物品。
- `ModCreativeTabs` 新增现代 `HBM Control` 创造栏，对齐旧版 `MainRegistry.controlTab` 的承载位置。
- `HbmItemModelProvider` / `HbmLanguageProvider` / `HbmZhCnLanguageProvider` 接入 `CONTROL_TAB_ITEMS`。
- 从 1.7.10 资源复制对应贴图；旧版复用贴图的 `waste_u233`、`waste_u235`、`waste_plate_u233`、`waste_plate_u235`、`waste_plate_pu239`、`waste_natural_uranium` 在现代资源中生成同名副本，适配 basic item model。
- `RadiationConstants` 新增 `RTG = BILLET * 3`，对齐旧版 `rtg` 常量。
- `HazardRegistry` 新增 `registerLegacyReactorComponentHazards()`：
  - 燃料板使用已迁 `FuelRadiationModifier`，例如 `plate_fuel_u235` 从 `U235 * INGOT` 衰减/耗尽到 `WASTE * INGOT * 10`。
  - RTG pellet 使用已迁 `RtgRadiationModifier`，并补 HOT / BLINDING 组合，例如 `pellet_rtg_lead` = `PB209 * RTG` + HOT 7 + BLINDING 50。
  - pile rod 先迁旧版已有辐射条目；`pile_rod_boron`、`pile_rod_lithium`、`pile_rod_detector` 旧版本批段无 hazard，先仅补物品。
  - 衰变燃料废料基础物品先注册冷态 `base * 0.075` 或 rad source base。

仍未完成：

- `ItemDepletedFuel` 的 meta 0/1 子型、tooltip 与 stack hazard 已由后续 “ItemDepletedFuel meta 子型桥接” 批次补上；仅热态染色仍待客户端 item color 批次。
- `plate_fuel_*` 目前只接入 hazard 语义，旧版 `ItemPlateFuel` 的燃耗函数、寿命、pile 反应堆逻辑仍待独立物品类/反应堆库迁移。
- RTG pellet 目前只接入 hazard 语义，旧版 `ItemRTGPellet` 的热量、半衰期、衰变产物/container 行为尚未迁入。

验证：

- 2026-05-20 运行 `.\gradlew.bat runData --no-daemon` 通过，生成 42 个新/变更数据文件。
- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 基础物品批次 Datagen 收口

本批在新增废料、晶体、核弹组件和复合 hazard 条目后，补齐资源生成链路，确保后续继续迁 `HazardRegistry.registerItems()` 时新增物品能自动落到模型、语言和资源处理流程中。

已完成：

- `HbmBlockStateProvider` 新增 `simpleSidedCubeWithItem(...)`，用于 `decon` 这类无 `FACING` 属性但需要顶/底/侧纹理的方块，避免 `runData` 误用 `horizontalBlock(...)` 生成缺失属性的 blockstate。
- `HbmBlockLootProvider` 改为遍历 `ModBlocks.BLOCK_TAB_BLOCKS` 生成基础 dropSelf loot table，使 `waste_earth`、`waste_leaves` 等已注册方块不再阻塞 Forge datagen 的完整性校验。
- `runData` 已生成本批新增物品的 item model 与 lang 条目，例如：
  - `cell_sas3`
  - `nuclear_waste_short`
  - `nuclear_waste_short_tiny`
  - `fleija_propellant`

验证：

- 2026-05-20 运行 `.\gradlew.bat runData --no-daemon` 通过。
- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 ME Radiation Transformer 批次

本批继续补齐旧版 Hazard transformer 管线，把 AE2 ME 存储单元里的物品辐射汇总语义迁入现代 Hazard 库层。

旧版来源：

- `com.hbm.hazard.transformer.HazardTransformerRadiationME`
  - 只识别类名完全等于 `appeng.items.storage.ItemBasicStorageCell` 或 `appeng.items.tools.powered.ToolPortableCell` 的物品。
  - 对识别到的 ME drive / portable cell 调用 `Compat.scrapeItemFromME(stack)`。
  - 汇总内部 `ItemStack` 的 `HazardSystem.getHazardLevelFromStack(..., HazardRegistry.RADIATION)`，并追加 radiation hazard。
- `com.hbm.util.Compat#scrapeItemFromME`
  - 使用旧 AE2 NBT：`it` 为条目数，`#i` 为第 i 个栈的 compound，`@i` 为第 i 个栈数量。
- `com.hbm.hazard.HazardRegistry`
  - 旧版 transformer 注册条件为 `!(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSafeMEDrives)`。
- `com.hbm.config.GeneralConfig`
  - `enableLessBullshitMode` 默认 `false`。
  - `LBSM_safeMEDrives` 默认 `true`，注释语义为启用后阻止 ME Drives / Portable Cells 变成放射性。

已完成：

- 新增 `MeRadiationHazardTransformer`。
  - 按旧版类名识别 AE2 ME storage cell / portable cell，不引入 AE2 编译依赖。
  - 读取旧版 `it` / `#i` / `@i` NBT 结构。
  - 对内部栈设置保存的数量后复用 `HazardRegistry.getStackRadiation(...)`，保持现代 tag/item/stack/modifier/transformer 管线一致。
  - 只在汇总辐射大于 0 时追加 `HazardType.RADIATION`。
- `HazardRegistry.registerTransformers()` 接入 `MeRadiationHazardTransformer`。
  - 迁入旧版条件：仅当 `enableLessBullshitMode && lbsmSafeMeDrives` 同时为真时跳过 ME 辐射汇总。
- `RadiationConfig` 补最小 LBSM hazard 兼容配置：
  - `enableLessBullshitMode` 默认 `false`。
  - `lbsmSafeMeDrives` 默认 `true`。

暂未完成/保留：

- 本批只对齐 1.7.10 AE2 类名与旧 NBT 契约；现代 AE2 1.20.1 的 item class / data component / capability 若不同，需要后续在 AE2 compat 批次中补桥接。
- 旧版 LBSM 是全局配置组，现代端目前只补 hazard 所需的最小门控；完整 LBSM 配方/进程配置仍待独立配置库批次。

验证：

- 2026-05-21 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 Container/Crate/Toolbox 识别补齐批次

本批继续补 `ContainerRadiationHazardTransformer` 剩余识别分支，目标是让旧版 crate/toolbox/ItemInventory 的 NBT 契约不再只依赖默认大范围扫描。

旧版来源：

- `com.hbm.hazard.transformer.HazardTransformerRadiationContainer`
  - `BlockStorageCrate`：扫描 `slot0..slot103`，原样泄露辐射。
  - `ModItems.toolbox`：通过 `ItemStackUtil.readStacksFromNBT(stack, 24)` 读取 24 格，原样泄露辐射。
  - `ModItems.containment_box`：20 格，`BobMathUtil.squirt(...)` 衰减。
  - `ModItems.plastic_bag`：1 格，辐射乘以 `2F`。
- `com.hbm.util.ItemStackUtil`
  - `ItemInventory` 使用小写 list key `items`，每项使用小写 byte key `slot`。
- `com.hbm.hazard.HazardRegistry`
  - container transformer 注册条件为 `!(GeneralConfig.enableLBSM && GeneralConfig.enableLBSMSafeCrates)`。
- `com.hbm.config.GeneralConfig`
  - `LBSM_safeCrates` 默认 `true`。

已完成：

- `ContainerRadiationHazardTransformer` 新增精确容器类型：
  - `STORAGE_CRATE`：识别现代/后续迁入的 HBM registry path `crate_iron`、`crate_steel`、`crate_desh`、`crate_tungsten`、`safe`，扫描 104 个旧版 `slotN`。
  - `TOOLBOX`：识别现代 `toolbox`，扫描 24 格。
  - `LEAD_BOX`：保留 20 格 + `squirt`。
  - `PLASTIC_BAG`：保留 1 格 + `* 2F`。
- 补 `readLegacyItemInventoryRadiation(...)`，读取旧版小写 `items` / `slot` 列表，承接 toolbox、containment box、plastic bag 的旧 ItemInventory 契约。
- 保留现代 `ContainerHelper` 大写 `Items` 兼容读取，用于后续 1.20.1 容器实现。
- `RadiationConfig` 新增最小兼容配置 `lbsmSafeCrates`，默认 `true`。
- `HazardRegistry.registerTransformers()` 按旧版条件门控 container transformer。
- `ModItems` 新增 `toolbox` 占位物品，放入 consumables tab。
- 复制旧版工具箱贴图：
  - `textures/items/kit_toolbox.png` -> `textures/item/toolbox.png`
  - `textures/items/kit_toolbox_empty.png` -> `textures/item/kit_toolbox_empty.png`

暂未完成/保留：

- `toolbox` 非潜行右键热键栏轮换和 24 格旧版 `items/slot` NBT 已由后续 “ToolboxItem 热键栏轮换批次” 补上；GUI、24 格手动库存交互、打开/关闭贴图层仍待后续批次迁入。
- `crate_iron/crate_steel/crate_desh/crate_tungsten/safe` 的实际方块、BlockEntity、手持打开和锁/蜘蛛/stacklock 行为仍待 storage crate 库批次迁入；本批只提前让注册名落地后能被 hazard transformer 正确识别。

验证：

- 2026-05-21 运行 `.\gradlew.bat runData --no-daemon` 通过。
- 2026-05-21 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 ToolboxItem 热键栏轮换批次

本批在上一批 `toolbox` 占位物品和 container hazard 识别之后，补最小旧版物品行为，让工具箱能实际写入 `ContainerRadiationHazardTransformer` 已支持的旧版 `items/slot` NBT。

旧版来源：

- `com.hbm.items.tool.ItemToolBox`
  - 最大堆叠 1。
  - 非潜行右键调用 `moveRows(...)`。
  - 当前热键栏中除工具箱本体外的 8 个槽位写入工具箱 24 格库存。
  - 工具箱内最上方活动行换回玩家热键栏。
  - 工具箱内部按 3 行 * 8 格组织。
  - 若热键栏里存在额外工具箱，会把额外工具箱丢出，避免工具箱套工具箱。
- `com.hbm.util.ItemStackUtil`
  - 保存 key 为 `items`。
  - 每项保存 byte key `slot`。
- `com.hbm.hazard.transformer.HazardTransformerRadiationContainer`
  - `toolbox` 读取 24 格并让内部辐射原样泄露。

已完成：

- 新增 `ToolboxItem`。
  - 继承现代 `Item`，保持 `stacksTo(1)`。
  - 主手非潜行右键执行旧版热键栏轮换。
  - 使用 24 格数组和 3 行 * 8 格旧版布局。
  - 写入旧版小写 `items` list 和 `slot` byte，直接喂给现有 container hazard transformer。
  - 读取旧 NBT 时也按 `items/slot` 还原栈。
  - 额外工具箱从热键栏丢出，保留旧版“不能工具箱套工具箱”的保护。
- `ModItems.TOOLBOX` 改为注册 `ToolboxItem`，仍保留 legacy name map。
- 补工具箱 tooltip 语言键。

暂未完成/保留：

- 潜行右键 GUI、`ContainerToolBox`、`GUIToolBox`、打开态 `isOpen`、关闭时 `rand` 刷新和双贴图 render pass 尚未迁入。
- 旧版 6KB NBT 超限时把内容喷出的保护尚未迁入；本批先保证热键栏轮换和 hazard NBT 数据契约。

验证：

- 2026-05-21 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-21 运行 `.\gradlew.bat runData --no-daemon` 通过。

## 2026-05-30 追加：stack hazard 接入通用 legacy meta 映射

本批没有新增具体 hazard 条目，而是补迁移入口，避免旧 `ComparableStack(item, meta)` 危险属性继续在 hazard 库内写局部 switch。

已完成：

- `HazardRegistry` 新增：
  - `registerLegacyMeta(ResourceLocation legacyId, int legacyMeta, HazardType type, float level)`
  - `registerLegacyMeta(ResourceLocation legacyId, int legacyMeta, HazardData data)`
  - `blacklistLegacyMeta(ResourceLocation legacyId, int legacyMeta)`
- 上述入口统一通过 `LegacyMetaItemMappings` 解析旧单 item + metadata 到现代独立物品，再落到现有 stack hazard/blacklist 层。
- 当前可用映射由 recipe 库维护，首批包含 `battery_pack` 与 `battery_sc`。后续迁 RTG/RBMK/circuit/assembly component 等旧 meta 族时，应先扩展该表，再注册 hazard。

仍未完成：

- 旧版 `HazardRegistry.registerItems()` 中大量 `ComparableStack` 特例尚未批量迁入。
- `LegacyMetaItemMappings` 仍是静态 Java 表；未来若迁移存档/配置化物品池，可以再补数据驱动导出或诊断命令。

验证：

- 2026-05-30 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-30 运行 `.\gradlew.bat runData --no-daemon` 通过。
