# 2026-05-20 radiation core 1.7.10 source trace

## Scope

本记录用于后续分批排查和移植 HBM NTM 1.7.10 辐射底层，目标是以 1.7.10 源码为事实源，逐项对齐到当前 Forge 1.20.1 port。

本轮只建立审计范围和迁移路线，不直接判定当前实现已经 100% 等价。

## Legacy Source Files

核心实体数据：

- `com.hbm.extprop.HbmLivingProps`
  - 保存 `radiation`、`digamma`、`radEnv`、`radBuf`、`asbestos`、`blacklung`、`contagion`、`oil`、`fire`、`phosphorus`、`balefire`、`blackFire`。
  - 旧 NBT 键：`HbmLivingProps`、`hfr_radiation`、`hfr_digamma`、`hfr_asbestos`、`hfr_bomb`、`hfr_blacklung`、`hfr_oil`、`hfr_fire`、`hfr_phosphorus`、`hfr_balefire`、`hfr_blackfire`、`hfr_cont_count`、`cont_<index>`。
  - 网络序列化包含 radiation、digamma、asbestos、bombTimer、contagion、blacklung、oil、contamination list。

污染入口：

- `com.hbm.util.ContaminationUtil`
  - `calculateRadiationMod`：玩家抗性系数为 `10^-HazmatRegistry.getResistance(player)`，非玩家为 `1`。
  - `isRadImmune`：mutation 药水、核苦力怕、哞菇、僵尸、骷髅、Quackos、豹猫、`IRadiationImmune`、lootable body 免疫。
  - `contaminate` 支持 `RADIATION`、`DIGAMMA`，以及 `FARADAY`、`HAZMAT`、`HAZMAT2`、`DIGAMMA`、`DIGAMMA2`、`CREATIVE`、`RAD_BYPASS`、`NONE` 防护类型。
  - 辐射污染先累加 `radEnv`，再按防护和抗性写入累计 radiation。

区块辐射：

- `com.hbm.handler.radiation.ChunkRadiationManager`
  - 默认 proxy 为 `ChunkRadiationHandlerSimple`。
  - 每秒调用 `proxy.updateSystem()`。
  - 每 server tick 调用 `proxy.handleWorldDestruction()` 和 `proxy.receiveWorldTick(event)`。
- `ChunkRadiationHandlerSimple`
  - 每 chunk 一个辐射值，最大 `100000F`。
  - 扩散权重：中心 `0.6F`，十字邻居 `0.075F`，斜角邻居 `0.025F`。
  - 对已有邻居值执行 `newRad * 0.99F - 0.05F` 衰减并 clamp。
  - chunk NBT 键：`hfr_simple_radiation`。
  - 高辐射区生成 `radFog`，并执行草地/高草/树叶污染。
- `ChunkRadiationHandler3D`
  - 每 chunk 16 个高度段，源码标记 `@Untested`，存在旧版注释说明可能崩溃。
- `ChunkRadiationHandlerPRISM`
  - 由 `RadiationConfig.enablePRISM` 切换，后续需单独审计。
- `ChunkRadiationHandlerBlank`
  - 空实现，配置或调试时使用。

配置：

- `com.hbm.config.RadiationConfig`
  - `fogRad=100`、`fogCh=20`、`hellRad=0.1`、`worldRad=10`、`worldRadThreshold=20`。
  - `worldRadEffects=true`、`cleanupDeadDirt=false`、`enableContamination=true`、`enableChunkRads=true`、`enablePRISM=false`。
  - hazard 开关：asbestos、coal、hot、explosive、hydroactive、blinding、fibrosis。

危险属性：

- `com.hbm.hazard.HazardRegistry`
  - 定义全部核素基础 RAD/s 常量、形态倍率和危险类型。
  - 类型包括 radiation、digamma、hot、blinding、asbestos、coal、hydroactive、explosive。
- `com.hbm.hazard.HazardSystem`
- `com.hbm.hazard.HazardData`
- `com.hbm.hazard.HazardEntry`
- `com.hbm.hazard.type.*`
- `com.hbm.hazard.modifier.*`
- `com.hbm.hazard.transformer.*`

玩家/生物表现与工具：

- `com.hbm.items.tool.ItemGeigerCounter`
- `com.hbm.items.tool.ItemDigammaDiagnostic`
- `com.hbm.render.util.RenderScreenOverlay`
- `com.hbm.particle.ParticleRadiationFog`
- `com.hbm.particle.ParticleDigammaSmoke`
- `com.hbm.commands.CommandRadiation`
- `com.hbm.handler.HazmatRegistry`
- `com.hbm.util.ArmorUtil`
- `com.hbm.util.ArmorRegistry`

辐射源集成点示例：

- `TileEntityStorageDrum`
- `TileEntityZirnoxDestroyed`
- `TileEntityWatz`
- `TileEntityReactorResearch`
- `TileEntityMachineRadGen`
- `FT_VentRadiation`
- `BlockHazard` / `BlockHotHazard` / `BlockHazardFalling`
- `DigammaFlame` / `DigammaMatter`

## Current 1.20.1 Port Surface

当前 port 已有第一批辐射底层文件：

- `src/main/java/com/hbm/ntm/radiation/RadiationData.java`
- `RadiationUtil.java`
- `RadiationConstants.java`
- `RadiationResistance.java`
- `RadiationSavedData.java`
- `ChunkRadiationManager.java`
- `HazardRegistry.java`
- `HazardEntry.java`
- `HazardType.java`
- `HazmatRegistry.java`
- `ArmorUtil.java`
- `ItemRadiationRegistry.java`
- `RadiationShieldingRegistry.java`
- `ModDamageSources.java`

当前 port 已接入事件和客户端表现：

- `CommonForgeEvents.java`
- `PlayerRadiationSyncPacket.java`
- `ClientRadiationData.java`
- `RadiationHud.java`
- `GeigerCounterItem.java`
- `DigammaDiagnosticItem.java`
- `RadawayItem.java`
- `RadiationMobEffect.java`
- `RadawayMobEffect.java`
- `DeconBlockEntity.java`
- `RadioactiveWasteEarthBlock.java`
- `LegacyDemonLampBlockEntity.java`

## Known Audit Gaps

需要逐项核对，不应默认等价：

- 旧版 `HbmLivingProps` 的全部字段、NBT 键、同步字段、死亡/重生保留规则。
- `ContaminationUtil.contaminate` 的所有 `ContaminationType` 防护语义。
- 旧版玩家出生 200 tick 保护、创造模式保护、免疫实体列表、mutation/stability 药水语义。
- `radEnv`/`radBuf` 每秒刷新与盖格计数器显示、声音、HUD 的准确时序。
- `ChunkRadiationHandlerSimple` 扩散、衰减、保存、卸载、fog、世界破坏逻辑。
- PRISM 和 3D handler 是否要复刻旧版行为或保留配置禁用。
- `HazardRegistry.registerItems()` 全量条目、meta item、ore dictionary 条目、modifier、transformer。
- Hazmat/ArmorRegistry 防护类别和抗性权重。
- RadAway、Rad-X、药水、decon、机器、流体、爆炸、核反应堆的辐射入口。
- 粒子、声音、HUD 纹理必须继续从 1.7.10 资源中迁移。

## Migration Plan

建议分批：

1. 数据契约审计：`HbmLivingProps` -> `RadiationData`，补齐字段、NBT、同步和 respawn 行为。
2. 污染入口审计：`ContaminationUtil` -> `RadiationUtil`，完整复刻防护类型、免疫、抗性、创造/出生保护。
3. Chunk radiation 审计：先复刻 `ChunkRadiationHandlerSimple`，包括扩散、衰减、保存键、fog 和 world destruction。
4. Hazard registry 审计：抽取 1.7.10 全量危险属性表，按当前已注册物品分批接入，未迁移物品保留待办。
5. Armor/Hazmat 审计：迁移 HBM 护甲后再按旧版 `HazmatRegistry`/`ArmorUtil` 精确接入。
6. 工具/HUD/音效审计：盖格、剂量计、Digamma Diagnostic、RadAway、Rad-X、overlay 纹理和 sounds。
7. 系统集成审计：机器、流体、方块、爆炸、反应堆、特殊实体逐个恢复辐射源和污染副作用。
8. 配置与命令审计：补齐 ForgeConfigSpec、调试命令、测试入口。

## Verification Checklist

- `.\gradlew.bat compileJava processResources --no-daemon`
- 单元/调试命令：设置玩家 radiation/digamma，验证 NBT 保存、死亡、重生、同步。
- 区块辐射：手动设置 chunk rad，等待 20 秒检查扩散曲线与衰减。
- 防护：无甲、普通护甲、hazmat、heavy hazmat、digamma 防护分别验证污染结果。
- HUD/盖格：背包/副手持有盖格计数器，验证声音阈值、RAD/s、累计 RAD。
- 世界破坏：高辐射 chunk 验证草地、高草、树叶转换。
- 回归：每批完成后更新本记录的已完成范围和仍缺口。

## 2026-05-20 Data Contract Audit Pass 1

已复核当前 port 的辐射骨架：

- `RadiationData` 已有 radiation、digamma、radEnv、radBuf。
- `CommonForgeEvents` 已有服务端 living tick、玩家 tick、clone、玩家同步。
- `PlayerRadiationSyncPacket` / `ClientRadiationData` 已有玩家累计辐射、digamma、环境剂量、chunk 剂量、抗性。

本批已按 1.7.10 `HbmLivingProps` 数据契约补齐：

- `RadiationData` 的现代持久根键改为旧版 `HbmLivingProps`，保留读取旧临时根键 `hbm_radiation` 的迁移兼容。
- 补齐旧版字段：`hfr_asbestos`、`hfr_bomb`、`hfr_contagion`、`hfr_blacklung`、`hfr_oil`、`hfr_fire`、`hfr_phosphorus`、`hfr_balefire`、`hfr_blackfire`。
- 保留并迁移旧版 contamination 列表语义：旧 `hfr_cont_count` + `cont_<index>` 会折叠到现代 `hfr_contamination` list。
- `radEnv` / `radBuf` 不再强制非负，贴近旧版简单 float 缓冲语义。
- 新增 `flushEnvironmentBuffer`，每秒将当秒累计 `radEnv` 刷到 `radBuf` 并清空。
- `copyForRespawn` 会复制旧版根键；如果只存在早期 port 的 `hbm_radiation`，会迁移复制到 `HbmLivingProps`。

本批已按旧版同步契约扩展：

- `PlayerRadiationSyncPacket` 同步 radiation、digamma、radBuf、chunkRadiation、resistance，以及 asbestos、blacklung、bombTimer、contagion、oil、fire、phosphorus、balefire、blackFire。
- `ClientRadiationData` 保留这些字段的客户端镜像和 getter，供后续 HUD、overlay、装备特效、调试 UI 使用。

本批已调整时序：

- 玩家背包/护甲/副手物品辐射从每秒一次总量污染改为每 tick 按 `/20` 累计，保持 RAD/s 语义，同时让 `radEnv` 反映当秒累积剂量。
- living tick 中先处理 chunk 辐射、辐射效果和 digamma，再在每秒边界刷新 `radEnv -> radBuf`，随后同步玩家客户端数据。

仍未完成：

- `ContaminationUtil.ContaminationType` 的完整防护语义尚未迁移，本批仍使用当前 `RadiationUtil.contaminate(entity, amount, bypassResistance)` 简化入口。
- asbestos、blacklung、contagion、oil、fire、phosphorus、balefire、blackFire 字段已保存和同步，但对应完整行为尚未移植。
- 旧版 `HbmLivingProps.ContaminationEffect.load` 源码疑似读错 compound，本批只兼容其保存结构，不重新引入 bug。
- clone 是否应在死亡时清除某些字段仍需结合旧版实际事件链和玩法验证。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 Hazard Library Integration Note

已确认 `工程记录/2026-05-20-item-hazard-system-1.7.10源码功能追踪.md` 中已有物品 Hazard 系统追踪，因此本批没有直接继续往具体燃料棒/RTG 物品上添加硬编码行为，而是先补库层：

- `HazardData`
- `HazardModifier`
- `FuelRadiationModifier`
- `LinearDepletionRadiationModifier`
- `HazardTransformer`
- `NbtRadiationHazardTransformer`
- `ContainerRadiationHazardTransformer`

辐射核心现在通过 `HazardRegistry#getHazards` 与 `getHazardLevel(stack, type, holder)` 使用这个库层。后续具体物品迁移应优先注册 `HazardData + modifier/transformer`，不要绕过库层直接在 item tick 中写特殊分支。

## 2026-05-20 Hazard Registry Pass 2

旧版事实源：

- `hazard/HazardRegistry.java#registerItems`
  - vanilla gunpowder/TNT/pumpkin pie explosive hazards
  - coal/lignite dust coal hazards
  - yellowcake/fallout/corium/waste/phosphorus/lithium/asbestos/basic explosive blocks
  - nuke part blinding/explosive traits
- `hazard/type/HazardTypeHot.java`
  - if not wet and no reacher, `target.setFire(ceil(level))`
- `hazard/type/HazardTypeBlinding.java`
  - if no light protection, add blindness for `ceil(level)` ticks
- `hazard/type/HazardTypeHydroactive.java`
  - if wet/water, consume stack/entity item and explode with strength `level`
- `hazard/type/HazardTypeExplosive.java`
  - if burning, consume stack/entity item and explode with strength `level`

已完成：

- `HazardRegistry` 新增 vanilla hazard：
  - gunpowder explosive `1`
  - TNT explosive `4`
  - pumpkin pie explosive `1`
  - coal/charcoal coal dust bridge
- `HazardRegistry` 继续补当前 1.20.1 已注册 ID 的旧版 hazard：
  - `block_yellowcake` 使用旧版 `u * block * powder_mult`
  - `block_fallout` 使用旧版 `yc * block * powder_mult`
  - `block_schrabidate`
  - `block_red_phosphorus`
  - `block_ra226`
  - `block_actinium`
  - `powder_lignite`
  - `lignite`
  - `rocket_fuel`
  - `solid_fuel`
  - `solid_fuel_presto`
  - `solid_fuel_presto_triplet`
  - `ingot_phosphorus`
  - `lithium`
  - `powder_calcium`
  - `block_semtex`
  - `block_c4`
  - `brick_fire`
  - `ore_nether_fire`
  - `block_white_phosphorus`
  - `block_red_phosphorus`
  - `block_lithium`
  - `tile_lab_broken` asbestos
- Nuke item hazard 补充：
  - `nuke_solinium` blinding `45`
  - `nuke_fleija` explosive `8` and blinding `50`
- `CommonForgeEvents` 的玩家背包 hazard tick 从 asbestos/coal 扩到：
  - HOT：不湿时点燃玩家 `ceil(level)` 秒
  - BLINDING：施加失明 `ceil(level)` tick
  - HYDROACTIVE：湿/雨/水中清空 stack 并爆炸
  - EXPLOSIVE：玩家燃烧时清空 stack 并爆炸

仍未完成：

- 旧版 `reacher` 物品免 HOT 逻辑未迁移，因为 `reacher` 物品尚未注册。
- 旧版 `ArmorRegistry.hasProtection(... LIGHT/...)` 的精确 blinding/hydro/explosive 防护未迁移；现代当前只接了基础 hazard 行为。
- EntityItem 落地物品的 hydro/explosive `updateEntity` 尚未接入；本批只覆盖玩家库存 tick。
- 旧版燃料棒、RTG pellet、RBMK/PWR/Watz、容器/NBT/ME hazard transformer 仍未迁移，需要等对应 item/meta/NBT 模型落地后再做。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 ContaminationUtil Semantics Pass 1

本批目标：把 1.7.10 `ContaminationUtil.contaminate(entity, HazardType, ContaminationType, amount)` 的入口语义补进现代 `RadiationUtil`。

已完成：

- 新增 `RadiationUtil.ContaminationType`：
  - `FARADAY`
  - `HAZMAT`
  - `HAZMAT2`
  - `DIGAMMA`
  - `DIGAMMA2`
  - `CREATIVE`
  - `RAD_BYPASS`
  - `NONE`
- 新增现代重载：`RadiationUtil.contaminate(LivingEntity, HazardType, ContaminationType, float)`。
- 保留旧现代简化入口：`contaminate(entity, amount, bypassResistance)`，内部映射为：
  - `bypassResistance=true` -> `RAD_BYPASS`
  - `bypassResistance=false` -> `CREATIVE`
- 旧版顺序已对齐：辐射 hazard 先累加 `radEnv`，随后才检查防护、创造模式、出生 200 tick 保护和实体免疫。
- 对齐旧版玩家保护：
  - `FARADAY` 调 `ArmorUtil.checkForFaraday`。
  - `HAZMAT` 调 `ArmorUtil.checkForHazmat`。
  - `HAZMAT2` 调 `ArmorUtil.checkForHaz2`。
  - `DIGAMMA` 调 `checkForDigamma` 或 `checkForDigamma2`。
  - `DIGAMMA2` 调 `checkForDigamma2`。
  - `CREATIVE` / `RAD_BYPASS` / `NONE` 不直接被装备阻挡。
  - 创造模式会阻挡除 `NONE` 和 `DIGAMMA2` 外的污染。
  - 玩家出生后 200 tick 内阻挡污染。
- `HazardType.RADIATION` 写入累计 radiation，`RAD_BYPASS` 不套抗性；其他辐射污染套 `10^-resistance`。
- `HazardType.DIGAMMA` 写入 digamma。
- 新增 `applyDigammaData` 和 `applyDigammaDirect` 入口，作为后续迁移旧版 digamma 源的桥。
- `CommonForgeEvents` 的背包辐射和 chunk 辐射已切到明确的 `HazardType.RADIATION + ContaminationType.CREATIVE`。

临时桥接/仍未完成：

- 现代 `ArmorUtil.checkForDigamma` / `checkForDigamma2` 现在只是关键词和抗性桥接：
  - `checkForDigamma` 识别全套 `fau` 或 `dns` 关键词装备。
  - `checkForDigamma2` 识别全套 `robe/robes`，且要求抗性和极低最大生命值。
  - 旧版要求稳定药水、铁 cladding、robes 全套和 maxHealth 条件；这些需等待旧版药水、ArmorModHandler、robes/FAU/DNS 护甲迁移后精确补齐。
- 现代 `isRadImmune` 仍未完整覆盖旧版核苦力怕、Quackos、`IRadiationImmune`、mutation 药水、lootable body 等实体/效果。
- `HAZMAT` 在旧版还会被 mutation 药水视为通过；现代 mutation 效果尚未迁移，本批未接入。
- `NONE` 语义已保留为不被创造模式阻挡，但当前没有调用点；后续特殊污染源迁移时使用。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 Immunity and Status Effect Bridge Pass 1

本批目标：补足 `ContaminationUtil` 语义对齐后仍缺的旧版免疫/状态效果底层入口，使后续实体、护甲、药水和污染源迁移能复用统一判断。

已完成：

- 新增 `com.hbm.ntm.api.RadiationImmune` 标记接口，对应 1.7.10 `api.hbm.entity.IRadiationImmune`。
- 新增 `SimpleMobEffect` 作为无 tick 行为的基础效果类。
- 新增并注册三种旧版语义效果：
  - `hbm:radx`：由 `HazmatRegistry.getResistance` 识别，提供旧版 Rad-X 的 `+0.2` 抗性。
  - `hbm:mutation`：由 `RadiationUtil.isRadImmune` 和 `ArmorUtil.checkForHazmat` 识别，作为旧版 mutation 辐射免疫/防护通过标志。
  - `hbm:stability`：由 `ArmorUtil.checkForDigamma` / `checkForDigamma2` 识别，作为旧版 digamma 防护标志。
- `RadiationUtil.isRadImmune` 现在覆盖：
  - `RadiationImmune` 标记接口。
  - `mutation` 效果。
  - mooshroom、zombie、skeleton、ocelot。
  - 类名桥接：`CreeperNuclear`、`EntityQuackos`、`EntityLootableBody`。
- `RadiationUtil.applyDigammaData` 现在覆盖旧版 duck/ocelot 免疫桥：
  - `Ocelot`
  - 类名桥接 `EntityDuck`
- `zh_cn.json` 已补 `radx`、`mutation`、`stability` 效果名。

仍未完成：

- `radx`、`mutation`、`stability` 的物品来源、配方、图标、时长和旧版药水 ID 还未迁移；本批只补底层效果语义。
- `RadiationImmune` 只是现代接口入口；具体自定义实体迁移时仍要让实体实现该接口。
- 类名桥接用于等待旧版实体迁移，后续如果实体现代类名变化，应在迁移实体时改为显式接口或精确类型判断。
- 旧版 `ArmorModHandler`、robes/FAU/DNS/cladding 的 Digamma2 精确条件仍需等护甲系统迁移。

验证：

- 2026-05-20 使用 `utf-8-sig` 解析 `zh_cn.json` 通过。
- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 Rad-X Item Pass 1

旧版事实源：

- `ModItems.radx = new ItemPill(0).setUnlocalizedName("radx").setCreativeTab(MainRegistry.consumableTab).setTextureName("hbm:radx")`。
- `ItemPill#getMaxItemUseDuration` 返回 `10`。
- `ItemPill#onFoodEaten` 中 `radx` 给玩家 `HbmPotion.radx`，时长 `3 * 60 * 20`，等级 `0`。
- 旧版 tooltip：`item.radx.desc=在3分钟内增加0.2（37%）的抗辐射能力`。
- 旧版贴图：`assets/hbm/textures/items/radx.png`。
- 旧版药水图集：`assets/hbm/textures/gui/potions.png`，`radx` 图标索引为 `(5, 0)`。

已完成：

- 新增 `EffectPillItem`，用于复刻 `ItemPill(0)` 的基础行为：
  - `UseAnim.EAT`
  - 使用时长 `10`
  - 完成使用后添加指定效果
  - 非创造玩家消耗 1 个物品
  - 可选 `.desc` tooltip
- 新增并注册 `ModItems.RADX`：
  - ID：`radx`
  - 堆叠：`16`
  - 效果：`ModEffects.RADX`
  - 时长：`3 * 60 * 20`
  - 等级：`0`
  - 创造栏：沿用当前 `CONSUMABLE_TAB_ITEMS`
- 从 1.7.10 复制 `radx.png` 到 `assets/hbm/textures/item/radx.png`。
- 新增 `assets/hbm/models/item/radx.json`。
- 复制旧版 `potions.png` 到 `assets/hbm/textures/mob_effect/potions.png`，保留原始图集以供后续切独立 effect icon 或自定义 HUD 使用。
- 更新 `zh_cn.json` 和 datagen 语言 provider：
  - `item.hbm.radx`
  - `item.hbm.radx.desc`
  - `effect.hbm.radx`
  - `effect.hbm.mutation`
  - `effect.hbm.stability`

仍未完成：

- 旧版 `VersatileConfig.applyPotionSickness(player, 5)` 和 `hasPotionSickness` 防连续服药机制尚未迁移。
- 现代 mob effect 图标仍未从旧版 `potions.png` 切成独立 `mob_effect/*.png`；当前只保存了旧图集资源。
- Rad-X 配方 `COAL.dust(), COAL.dust(), F.dust()` 尚未迁移，因为煤粉/氟粉及 tag/矿辞桥仍需按物品迁移批次处理。
- `stability` 和 `mutation` 的来源物品/模块还未完整迁移；本批只给它们补了底层效果注册和防护判断入口。

验证：

- 2026-05-20 使用 `utf-8-sig` 解析 `zh_cn.json` 与 `radx.json` 通过。
- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-20 Radiation Config / Chunk / Long-Term Effects Pass 1

旧版事实源：

- `config/RadiationConfig.java`
  - `fogRad = 100`
  - `fogCh = 20`
  - `hellRad = 0.1`
  - `worldRad = 10`
  - `worldRadThreshold = 20`
  - `worldRadEffects = true`
  - `enableContamination = true`
  - `enableChunkRads = true`
  - hazard disable toggles such as asbestos/blinding/coal/explosive/hot/hydroactive
- `handler/radiation/ChunkRadiationHandlerSimple.java`
  - chunk NBT key: `hfr_simple_radiation`
  - simple diffusion weights: center `0.6`, cardinal `0.075`, diagonal `0.025`
  - existing target chunks apply `newRad * 0.99F - 0.05F`, new targets receive raw spread
  - fog threshold/chance read from `RadiationConfig`
  - world mutation targets `waste_earth` and `waste_leaves`
- `hazard/HazardRegistry.java`
  - `block = 10.0F`
- `handler/HazmatRegistry.java`
  - legacy armor coefficients for steel/titanium/alloy/cobalt, hazmat, PAA, liquidator, security, starmetal, CMB, schrabidium, euphemium, gas masks, jackets
- `handler/EntityEffectHandler.java`
  - long-term contagion, lung disease, oil, fire/phosphorus/balefire/blackFire and contamination-list ticking

已完成：

- 新增 `com.hbm.ntm.config.RadiationConfig`，并接入 `HbmCommonConfig`：
  - `radiation.fogRad`
  - `radiation.fogChance`
  - `radiation.hellRad`
  - `radiation.worldRadEffects`
  - `radiation.worldRad`
  - `radiation.worldRadThreshold`
  - `radiation.enableContamination`
  - `radiation.enableChunkRads`
  - `hazards.disableAsbestos`
  - `hazards.disableBlinding`
  - `hazards.disableCoal`
  - `hazards.disableExplosive`
  - `hazards.disableHot`
  - `hazards.disableHydroactive`
- `RadiationData` 的 radiation getter/setter/increment 现在尊重 `enableContamination`。
- `ChunkRadiationManager` 现在尊重 `enableChunkRads` 和 `worldRadEffects`。
- `ChunkRadiationManager` 的 fog 阈值/概率改为走 `RadiationConfig.fogRad/fogChance`。
- `ChunkRadiationManager` 的 world mutation 数量/阈值改为走 `RadiationConfig.worldRad/worldRadThreshold`。
- `RadiationSavedData#updateDiffusion` 已按旧版 simple handler 的已有目标/新目标分支调整扩散。
- `CommonForgeEvents` 新增 `ChunkDataEvent.Load/Save` 兼容旧版 chunk NBT key `hfr_simple_radiation`。
- Nether 环境辐射已按 `hellRad / 20F` 接入玩家 tick。
- 新增 `waste_leaves` 方块、模型、方块状态、物品模型和旧版贴图。
- `RadiationConstants.BLOCK` 从 `9.0F` 修正为旧版 `10.0F`。
- `HazardRegistry` 新增注册清理和 hazard disable 判断。
- `HazardRegistry` 先补一批当前 1.20.1 已存在资源的旧版 hazard 条目：
  - yharonite / ZFB billet
  - cobalt nugget
  - mud / yellowcake / euphemium / schrabidium cluster / phosphorus / tritium / fallout / gas ore
  - asbestos 方块与矿石
  - coal dust/infernal coal 相关入口
- `HazmatRegistry` 按旧版系数表动态注册已存在的 legacy-name 护甲/面罩/夹克；未迁移的物品会自动跳过。
- `HazmatRegistry#getResistance` 兼容 `hfr_cladding`、`ntm_cladding`、`cladding` 三种 NBT 键。
- `ArmorUtil` 扩展 Faraday 关键词桥，覆盖更多旧版金属/防护套装名。
- `CommonForgeEvents` 新增长期状态首批 tick：
  - contamination list 按 `maxRad/maxTime/time/ignoreArmor` 持续加辐射
  - contagion 倒计时、空气传播、物品 `ntmContagion` 标记、虚弱/反胃、伤害和呕吐桥
  - asbestos/blacklung 咳嗽、虚弱/反胃和煤尘/血呕吐粒子桥
  - oil 燃烧爆炸和油粒子桥
  - fire/phosphorus/balefire/blackFire 的倒计时、伤害、辐射和粒子桥
- `ItemRadiationRegistry` 新增 `getHazardLevel` 转发，供库存 asbestos/coal hazard 入口复用。

仍未完成：

- 旧版 chunk radiation 还有 PRISM/3D/Blank handler 和更复杂 NT handler，本批只对齐 simple handler。
- `ChunkRadiationHandlerSimple#receiveChunkUnload` 的旧版 remove 行为存在疑似 bug，现代 SavedData 没刻意复刻卸载删除。
- 世界副作用现在能落 `waste_earth/waste_leaves`，但旧版 fallout、sellafield、rad gas 等更大环境改造还未迁。
- HazardRegistry 只补了当前现代端已注册资源；燃料棒、RTG、容器/NBT/meta modifier、机器/流体/反应堆来源仍需后续批次。
- HazmatRegistry 只会注册已经存在的现代物品；大多数 HBM 护甲本体还未迁移。
- 长期状态的粒子/音效使用现代可用资源桥接，尚未完全还原旧版 AuxParticle 类型、player.cough 音效、MKU 专用 damage type、ArmorRegistry 细粒度防护类。
- contagion 的玩家背包随机感染扫描只补了附近 ItemEntity 标记，尚未复刻玩家背包 unstackable 物品的随机 `ntmContagion` 写入。

验证：

- 2026-05-20 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
## 2026-05-21 Radiation Command Pass 1

Legacy source:

- `com.hbm.commands.CommandRadiation`
  - command name: `ntmrad`
  - usage: `/ntmrad <set/clear>`
  - `clear`: calls `ChunkRadiationManager.proxy.clearSystem(world)` and reports `Cleared radiation data!`
  - `set <amount>`: parses a float clamped to `0..100000`, then sets chunk radiation at the sender coordinates and reports `Radiation set.`

Completed in the 1.20.1 port:

- Added `com.hbm.ntm.command.ModCommands`.
- Registered commands through `RegisterCommandsEvent` in `CommonForgeEvents`.
- Preserved the legacy operator command surface:
  - `/ntmrad clear`
  - `/ntmrad set <amount>`
- Added modern debug aliases for verification:
  - `/hbm radiation chunk get`
  - `/hbm radiation chunk set <amount>`
  - `/hbm radiation chunk add <amount>`
  - `/hbm radiation chunk clear`
  - `/hbm radiation player get [targets]`
  - `/hbm radiation player set <targets> <amount>`
  - `/hbm radiation player add <targets> <amount>`
  - `/hbm radiation player clear <targets>`
  - `/hbm radiation digamma get [targets]`
  - `/hbm radiation digamma set <targets> <amount>`
  - `/hbm radiation digamma add <targets> <amount>`
  - `/hbm radiation digamma clear <targets>`
- Chunk commands use the command source position, matching the legacy sender-coordinate behavior while also working from non-player command sources that provide a position.
- Player radiation and digamma commands write through `RadiationData`, so they reuse the same clamp/NBT/digamma health modifier behavior as the library.

Still incomplete:

- Commands do not force an immediate radiation sync packet; affected players receive the normal periodic sync from `CommonForgeEvents`.
- No dedicated command for asbestos, blacklung, contagion, fire, phosphorus, balefire, blackFire, or contamination-list entries yet.
- The legacy command did not include player radiation setters; these modern debug branches are verification helpers and should be kept aligned with future data-contract changes.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-22 Radiation Fog Debug Command

Legacy source:

- `CommandRadiation` did not expose a direct fog command; this is a modern verification helper for the migrated render-library particle.
- The old runtime fog path remains `ChunkRadiationHandlerSimple` -> `ClientProxy#effectNT(type="radFog")`.

Completed in the 1.20.1 port:

- Added `ChunkRadiationManager.spawnDebugRadiationFog(ServerLevel, BlockPos)` as a small server-side bridge that emits exactly one `RADIATION_FOG` particle event at a position.
- Added `/hbm radiation fog [pos]`:
  - without `pos`, spawns at the command source position.
  - with `pos`, uses `BlockPosArgument.getLoadedBlockPos` for safe loaded-position lookup.
- This command is intentionally under the modern `/hbm radiation` debug surface, not the legacy `/ntmrad` command surface.

Verification:

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 Gas Meltdown Entity Collision Pass

Legacy source:

- `com.hbm.blocks.generic.BlockGasMeltdown#onEntityCollidedWithBlock`
  - For every colliding `EntityLivingBase`, calls `ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F)`.
  - Adds legacy radiation potion for `60 * 20` ticks with amplifier `2`.
  - If `ArmorRegistry.hasAllProtection(entity, 3, HazardClass.PARTICLE_FINE)` succeeds, damages the gas-mask filter by `1`.
  - Otherwise increments `HbmLivingProps` asbestos by `5`.
- `BlockGasMeltdown#updateTick`
  - If sky is visible, increments chunk radiation by `5`.
  - Has a `1/350` chance to remove itself.
  - Can spread `gas_radon_dense` to a random adjacent air block before running the base gas spread tick.

Completed in the 1.20.1 port:

- Added `LegacyGasMeltdownBlock#entityInside` so modern `gas_meltdown` now applies the old contact behavior:
  - `HazardType.RADIATION + ContaminationType.CREATIVE`, amount `0.5F`.
  - radiation poisoning for `60 * 20` ticks, amplifier `2`.
  - asbestos `+5` when the entity lacks fine-particle protection.
- Added `ArmorUtil.hasFineParticleProtection` as the current bridge for old `HazardClass.PARTICLE_FINE` level-3 checks. It maps to the existing full Haz2-level suit check until the old armor/filter registry is fully migrated.
- `LegacyGasMeltdownBlock` now extends the migrated `LegacyGasBlock` base, so it uses the old gas movement contract:
  - first direction: 50% up, otherwise down
  - second direction: random horizontal
  - delay: 2 ticks after the initial 10-tick schedule
- Restored the old `1/7` adjacent-air `gas_radon_dense` spread branch before chunk radiation and dissipation checks.

Still incomplete:

- Old `ArmorUtil.damageGasMaskFilter(entityLiving, 1)` is not migrated yet because the modern port does not have the old gas-mask filter durability path wired in.
- `gas_meltdown` still uses the temporary modern particle in `animateTick`; the old visual particle was `townaura`, while high-radiation yellow fog remains part of the chunk-radiation client/render pass rather than this block collision pass.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-21 after adding `LegacyGasBlock`/radon support, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Chunk Radiation Diffusion/Fog Recheck

User-visible issue:

- Chunk radiation still appeared to spread too far.
- Far from source blocks, some chunks had unexpectedly high values while adjacent chunks were low.
- Yellow radiation fog still did not appear.

Legacy source recheck:

- `ChunkRadiationHandlerSimple#receiveChunkLoad` always inserts the loaded chunk into the runtime map with `event.getData().getFloat("hfr_simple_radiation")`, including `0F`.
- In `updateSystem`, a target chunk that exists in the previous runtime map follows the damped merge path:
  - `(current + spread) * 0.99F - 0.05F`
- A target chunk that did not exist in the previous runtime map follows the raw spread path:
  - `source * percent`
- Therefore loaded zero-radiation chunks matter: they act as old-style damping boundaries for spread and reduce edge spikes.
- Old fog is not a single random entry per second. It is checked inside the 3x3 diffusion loop for each new radiation cell whose post-merge value exceeds `fogRad`, using `rand.nextInt(fogCh) == 0`.
- The old fog spawn chunk is the source `coord`, not necessarily the target `newCoord`, and the y coordinate is `world.getHeightValue(x, z) + rand.nextInt(5)`.

Completed in the 1.20.1 port:

- `ChunkRadiationManager#loadLegacyChunkRadiation` now loads chunks with `0F` as well as positive radiation, matching old `receiveChunkLoad`.
- `CommonForgeEvents#onChunkDataLoad` now calls the chunk radiation load hook for every server chunk load; missing `hfr_simple_radiation` naturally reads as `0F`, matching old `NBTTagCompound#getFloat`.
- `RadiationSavedData#loadChunk` stores the loaded chunk value in the runtime map without forcing positive-only persistence.
- `RadiationSavedData#updateDiffusion` now keeps zero-valued loaded entries in the runtime map for old `containsKey` damping semantics.
- The save path still omits `<= 0F` entries, so zero runtime entries do not bloat persistent saved data.
- Fog candidates are now collected during the diffusion 3x3 loop, matching the old `rad > fogRad` check location and chance frequency.
- `ChunkRadiationManager` now emits the custom `hbm:radiation_fog` particle instead of vanilla dust/smoke.

Still incomplete:

- The simple handler's old unbounded raw spread still exists by design; the loaded-chunk runtime boundary and zero-entry damping now better match 1.7.10, but strict long-distance behavior should be verified in-game with fixed source chunks and chunk loading ranges.
- The custom particle is a modern approximation using the old texture, color, alpha curve, scale, and lifetime; it does not recreate the exact old GL loop that rendered 25 quads per particle.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-21 after aligning chunk-load zero entries, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Chunk Radiation Lag and Fog Repair

User-visible issue:

- Large numbers of radioactive block sources made chunk radiation updates very laggy.
- High-radiation chunks did not show the old yellow radiation fog.
- Some radioactive chunks did not visibly rot grass even when block sources were present.

Legacy source check:

- `ChunkRadiationManager#updateSystem` calls `ChunkRadiationHandlerSimple#updateSystem` every 20 server ticks.
- `ChunkRadiationHandlerSimple#updateSystem` owns both simple 3x3 diffusion and `radFog` spawning.
- The old simple handler only keeps loaded chunks in its runtime map:
  - chunk load reads `hfr_simple_radiation` into the map
  - chunk save writes it back
  - chunk unload removes it from the map
- Old fog is `ParticleRadiationFog`, spawned from the server effect bridge with:
  - threshold `RadiationConfig.fogRad`
  - chance `RadiationConfig.fogCh`
  - y position `world.getHeightValue(x, z) + rand.nextInt(5)`
  - render color `0.85F, 0.9F, 0.5F` with a translucent fog texture
- `handleWorldDestruction` is a radiation-library effect, not per-block logic. In the simple handler it runs every server tick, chooses 5 random loaded radiation chunks, uses 10 operation batches, scans the 16x16 surface with a 1/3 random pass, and converts grass, tallgrass, and leaves.
- The old simple handler contains hardcoded local `threshold = 10` in `handleWorldDestruction`, despite `RadiationConfig.worldRadThreshold` defaulting to `20`.

Bug source in the 1.20.1 port:

- Current `RadiationSavedData` persisted a global per-level map and did not remove chunk entries on unload, so diffusion and terrain mutation could keep iterating stale/offline chunk coordinates.
- Current terrain mutation and fog paths copied all saved entries and queried heightmaps without first enforcing the old loaded-chunk runtime boundary. This can cause expensive chunk work when many radiating sources have expanded the map.
- Current fog used `ParticleTypes.MYCELIUM`, so the visible result did not match the old yellow-green `ParticleRadiationFog`.
- Current world-effect threshold used the config value directly, so chunks in the old simple handler's 10-20 RAD behavior band did not mutate terrain.

Completed in the 1.20.1 port:

- `RadiationSavedData#updateDiffusion` now accepts `ServerLevel` and only diffuses through chunks that are currently loaded, matching the old runtime-map boundary.
- Added `RadiationSavedData#loadedEntries` to prune stale/offline chunk entries before terrain mutation and fog selection.
- `CommonForgeEvents#onChunkUnload` now calls `ChunkRadiationManager.unloadChunk`, mirroring old `receiveChunkUnload`.
- `ChunkRadiationManager` now spawns radiation fog during the once-per-second diffusion tick, matching the old simple handler timing.
- Replaced the `MYCELIUM` fog bridge with a yellow-green `DustParticleOptions` plus light smoke, using the old fog color and height range until the full custom particle renderer is migrated.
- World radiation terrain mutation now keeps the legacy 5 random chunks and configurable 10 operation batches, but clamps the threshold to the old simple-handler `10` behavior so moderate radioactive chunks can rot grass like 1.7.10.
- Terrain mutation and fog now skip entries whose chunk is not loaded before heightmap/block access.

Still incomplete:

- This is still a modern particle bridge. A full `ParticleRadiationFog` port needs a custom particle type/provider and the legacy `textures/particle/fog.png` render behavior.
- The global `SavedData` bridge remains different from the old chunk-local NBT runtime model. The new unload/prune behavior narrows the observable runtime behavior, but a future strict port should consider replacing the global saved list with chunk-attached radiation data.
- Existing worlds with a very large stale `hbm_chunk_radiation` saved-data file will be pruned gradually as levels tick/load; a one-shot cleanup command may be useful later if old test worlds are already bloated.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Special Chunk Radiation Source Block Pass

Legacy source:

- `com.hbm.blocks.generic.BlockSellafield`
  - random tick source; writes `0.5F * (metadata + 1)` chunk radiation.
  - randomly decays metadata downward, then becomes `sellafield_slaked` at level 0.
  - walking entities receive radiation potion for 30 seconds, amplifier `meta` or `meta * 2` for level 5.
- `com.hbm.blocks.generic.YellowBarrel`
  - schedules every 20 ticks.
  - `yellow_barrel` writes `5.0F`; `vitrified_barrel` writes `0.5F`.
  - yellow barrel explosion has an extra one-shot `+35` and radon placement, not included in the continuous source pass.
- `com.hbm.blocks.gas.BlockGasMeltdown`
  - scheduled gas tick; if the block can see sky, writes `5.0F`.
  - can spread dense radon gas and may dissipate.
- `com.hbm.blocks.gas.BlockGasBase`
  - invisible, non-colliding, replaceable gas block.
  - initial schedule delay `10`, movement delay `2`.
  - tries first movement direction, then second movement direction; if both fail, reschedules itself.
- `com.hbm.blocks.gas.BlockGasRadon`
  - contact: if no full particle-fine protection, `RAD_BYPASS` radiation `0.05F` and asbestos `+1`.
  - first direction: 1/5 up, otherwise down; second direction random horizontal.
  - 1/50 chance to dissipate.
- `com.hbm.blocks.gas.BlockGasRadonDense`
  - contact: if no full particle-fine protection, `CREATIVE` radiation `0.5F`, radiation potion `15 * 20` ticks amplifier `0`, and asbestos `+5`.
  - first direction: 1/5 up, otherwise down; second direction random horizontal.
  - 1/20 chance to convert grass below to `waste_earth`.
  - 1/30 chance to dissipate and place `fallout` if it can survive.
- `com.hbm.blocks.gas.BlockGasRadonTomb`
  - contact: removes `radaway` and `radx`, then applies `RAD_BYPASS` radiation `0.5F` and asbestos `+10`.
  - first direction: 1/3 up, otherwise down; second direction random horizontal.
  - 1/10 terrain decay check below the gas:
    - grass -> dirt metadata 1 (`coarse_dirt` in 1.20.1) with 1/5 chance, otherwise `waste_earth`
    - non-normal grass/leaves/plants/vine materials -> air
  - 1/600 chance to dissipate.
- `com.hbm.blocks.gas.BlockGasAsbestos`
  - contact: if no fine-particle protection, asbestos `+1`.
  - first direction: 1/5 down, otherwise random 6-way; second direction random horizontal.
  - 1/50 chance to dissipate.
  - client occasionally emits old `townaura` dust.
- `com.hbm.blocks.gas.BlockGasCoal`
  - contact: if no coarse-particle protection, black lung `+10`.
  - first direction: 1/5 down, otherwise random 6-way; second direction random horizontal.
  - 1/20 chance to dissipate.
  - client emits smoke.
- `com.hbm.blocks.gas.BlockGasMonoxide`
  - contact: if no monoxide gas protection, deals `ModDamageSource.monoxide` damage `1`.
  - first direction down; second direction random horizontal.
  - 1/100 chance to dissipate.
- `com.hbm.blocks.gas.BlockGasClorine`
  - contact: if no lung gas protection, applies blindness 5s, poison 20s amp 2, wither 1s amp 1, slowness 30s amp 1, mining fatigue 30s amp 2.
  - first direction: 1/5 up, otherwise down; second direction random horizontal.
  - 1/10 chance to dissipate.
- `com.hbm.blocks.generic.BlockNuclearWaste`
  - extends `BlockHazard`, so it keeps the normal scheduled chunk-radiation injection.
  - before `super.updateTick`, picks a random side; with 1/2 chance, if the adjacent block is air, places `gas_radon_dense`.
  - legacy registrations use this class for `block_waste`, `block_waste_painted`, and `block_waste_vitrified`.
  - all three are configured with `ExtDisplayEffect.RADFOG`, which emits old `townaura` around adjacent air spaces.
- `com.hbm.blocks.generic.BlockOutgas`
  - extends `BlockOre`; it does not make the block a continuous chunk-radiation source by itself.
  - random tick chooses one random side and places `getGas()` into adjacent air.
  - `dropBlockAsItemWithChance` places `getGas()` at the broken block position when `onBreak=true`.
  - neighbor changes place `getGas()` into all adjacent air blocks with 1/3 chance when `onNeighbour=true`.
  - radon subset:
    - uranium/scorched uranium/gneiss uranium/nether uranium variants -> `gas_radon`
    - `block_corium_cobble` -> `gas_radon`
    - `ancient_scrap` -> `gas_radon_tomb`
  - toxic gas subset:
    - asbestos ore/block/deco/brick/lab tile variants -> `gas_asbestos`
    - `ore_nether_coal` -> `gas_monoxide`
  - for random-ticking asbestos sources, walking on the block can also release `gas_asbestos` above the block with 1/10 chance and client `townaura`.
- `com.hbm.main.ModEventHandler#onBlockBreak`
  - breaking vanilla coal ore, vanilla coal block, or `ore_lignite` tries all 6 adjacent directions.
  - for each side, with 1/2 chance, if the target is air, places `gas_coal`.
- `com.hbm.blocks.generic.BlockAbsorber`
  - schedules every 10 ticks.
  - metadata tiers absorb chunk radiation by `2.5`, `10`, `100`, `10000`.

Completed in the 1.20.1 port:

- Registered special legacy ids:
  - `sellafield`
  - `sellafield_slaked`
  - `yellow_barrel`
  - `vitrified_barrel`
  - `gas_meltdown`
  - `rad_absorber`
- Added block behavior bridges:
  - `LegacySellafieldBlock`
  - `LegacyRadiationBarrelBlock`
  - `LegacyGasMeltdownBlock`
  - `LegacyRadAbsorberBlock`
- Added the minimum gas/radon library bridge:
  - `LegacyGasBlock` for old invisible, replaceable, non-colliding gas movement.
  - `LegacyGasRadonBlock` for `gas_radon`, `gas_radon_dense`, and `gas_radon_tomb`.
- Added `LegacyNuclearWasteBlock` for `block_waste`, `block_waste_painted`, and `block_waste_vitrified`:
  - keeps the `RadiatingHazardBlock` continuous chunk-radiation source table.
  - restores the scheduled random adjacent `gas_radon_dense` placement.
  - restores the local RADFOG/townaura-style visual feedback through the modern `radiation_fog` particle.
- Added `LegacyOutgasBlock` for the currently supported radon subset:
  - uranium ore variants release `gas_radon` on random tick and when broken.
  - `block_corium_cobble` releases `gas_radon` on random tick, break, and neighbor changes.
  - `ancient_scrap` releases `gas_radon_tomb` on random tick, break, and neighbor changes.
- Added `LegacyToxicGasBlock` and registered:
  - `gas_asbestos`
  - `gas_coal`
  - `gas_monoxide`
  - `chlorine_gas`
- Added `monoxide` damage type and language key for the old carbon-monoxide damage path.
- Expanded `LegacyOutgasBlock` toxic gas coverage:
  - asbestos ore/block/deco/brick/lab tile variants now release `gas_asbestos`.
  - `ore_nether_coal` now releases `gas_monoxide` when broken.
- Added a Forge block-break event bridge for the old coal dust gas burst:
  - `Blocks.COAL_ORE`
  - `Blocks.DEEPSLATE_COAL_ORE` as the modern vanilla equivalent
  - `Blocks.COAL_BLOCK`
  - `ore_lignite`
- Registered `gas_radon`, `gas_radon_dense`, and `gas_radon_tomb`; copied their legacy textures, added datagen model/blockstate/item coverage, and marked their block loot as no-drop.
- `gas_meltdown` now uses the shared gas movement bridge and can spawn `gas_radon_dense` like 1.7.10.
- Copied legacy textures for barrels, absorber tiers, meltdown gas, and sellafield/slaked variants.
- Added datagen coverage for blockstates, models, item models, loot tables, language entries, and tags.
- Continuous chunk radiation/absorption behavior is now covered for all special rows from the generated 1.7.10 source table.

Still incomplete:

- `yellow_barrel` explosion behavior is deferred until the legacy detonatable/explosion/waste/radon placement paths are migrated.
- `rad_absorber` higher tiers exist as blockstate/model variants, but creative sub-items and placement metadata for red/green/pink tiers still need a custom BlockItem/data path.
- `sellafield` uses modern blockstate `level=0..5`; worldgen/source placement for non-zero levels still needs the future sellafield worldgen/ore pass.
- The ash-glasses-only gas visibility path is still deferred.
- Old gas-mask filter durability damage is still deferred until the full armor/filter registry is migrated.
- Toxic gas protection currently uses the existing helmet/keyword bridge until the full `ArmorRegistry` hazard-class/filter system is migrated.
- `gas_coal` and `chlorine_gas` are registered and functional, but their legacy placement sources outside direct block placement are still tied to later event/machine/entity passes.
- `gas_coal` coal/lignite block-break source is restored; remaining `gas_coal` sources, if any, need later source-specific passes.

Verification:

- 2026-05-21 ran `.\gradlew.bat runData --no-daemon`: passed after switching special cube models to non-facing blockstates.
- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-21 after adding `gas_radon`/`gas_radon_dense`, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-21 after adding `gas_radon_tomb`, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-22 after adding `LegacyNuclearWasteBlock`, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-22 after adding the radon subset of `LegacyOutgasBlock`, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-22 after adding toxic gas blocks and outgas toxic branches, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-22 after adding the coal/lignite block-break `gas_coal` source, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 1.7.10 Chunk Radiation Block Source Audit

Generated reconciliation reports:

- `工程记录/库移植/生成报告/legacy-chunk-radiation-block-sources-2026-05-21.md`
- `工程记录/库移植/生成报告/legacy-chunk-radiation-block-sources-2026-05-21.csv`
- `工程记录/库移植/生成报告/legacy-noncontinuous-modern-hazard-review-2026-05-21.csv`

Audit source:

- 1.7.10 `com.hbm.blocks.ModBlocks`
- 1.7.10 block classes that call `ChunkRadiationManager`
- 1.7.10 `com.hbm.hazard.HazardRegistry`
- 1.7.10 `com.hbm.inventory.OreDictManager` radioactive `DictFrame` chains
- Current 1.20.1 `ModBlocks`, `HazardRegistry`, and `RadiationConstants`

Findings:

- 1.7.10 has 36 unique continuous chunk-radiation block ids, represented by 41 table rows because `sellafield` has six metadata strengths.
- Generic aligned rule: `BlockHazard`, `BlockHotHazard`, `BlockNuclearWaste`, and `BlockHazardFalling` write `legacy item/block radiation hazard * 0.1` per scheduled update.
- `BlockOre` is not generic continuous radiation. Only `ore_schrabidium = new BlockOre(Material.rock, 0.1F, 0.5F)` continuously writes chunk radiation, at `0.1` per update. Other hazardous ores should remain hazard data only.
- `fallout` layer is not a continuous chunk source in 1.7.10. `block_fallout` is, through `BlockHazardFalling`, at `1.05` per update.
- Current bridge aligns 27 rows, but four ids need exact-strength overrides: `ore_schrabidium` should be `0.1` not `1.5`; `block_pu_mix` should be `6.25` not `7.5`; `block_schrabidate` should be `1.5` not `17.5`; `block_solinium` should be `17.5` not `15.0`.
- Missing special source blocks/classes: `gas_meltdown`, `rad_absorber`, `sellafield`, `yellow_barrel`, `vitrified_barrel`.

Next migration plan from this audit:

1. Add an explicit chunk-radiation-source metadata/override layer for `RadiatingHazardBlock`, so old item hazard and old chunk emission can diverge without corrupting either table.
2. Apply the four exact-strength overrides above and prevent modern hazardous ore entries from becoming continuous sources unless they had an old `ChunkRadiationManager` call.
3. Port special source behavior as small focused block classes: `BlockSellafield`, `YellowBarrel`, `BlockGasMeltdown`, and `BlockAbsorber`.
4. After block-source alignment, verify that chunk radiation world effects reuse the old threshold behavior for rad fog and grass/dead dirt conversion.

## 2026-05-21 Chunk Radiation Source Override Pass

Completed in the 1.20.1 port:

- Changed `RadiatingHazardBlock` from deriving continuous chunk pollution from the item hazard table to using an explicit 1.7.10 chunk-source table keyed by legacy block id.
- Kept item/block hazard data independent from continuous chunk radiation emission.
- Applied the audit strengths for currently registered simple resource blocks, including the four mismatch fixes:
  - `ore_schrabidium`: `0.1` per scheduled tick
  - `block_pu_mix`: `6.25` per scheduled tick
  - `block_schrabidate`: `1.5` per scheduled tick
  - `block_solinium`: `17.5` per scheduled tick
- Prevented modern hazard-only simple blocks from becoming continuous chunk radiation sources just because their item has a radiation hazard.

Still incomplete:

- `yellow_barrel` explosion/radon placement remains deferred to the explosion framework pass; its passive barrel radiation is already bridged.
- `BlockOutgas` asbestos/monoxide branches are deferred until those gas blocks exist.
- In-game verification still needs a chunk with corrected source blocks to confirm old-style rad fog and grass/dead dirt conversion.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Radiation Parity Alignment Pass

Scope:

- Align the already-migrated radiation runtime pieces with the 1.7.10 behavior where the required supporting systems already exist.
- Do not invent missing legacy systems such as PRISM, nuclear creepers, Quackos, full gas mask/filter item logic, or the old custom radiation fog renderer.

Completed in the 1.20.1 port:

- `RadiationData` now treats legacy contamination NBT as canonical:
  - stores the active contamination count in `hfr_cont_count`
  - stores entries as `cont_<index>` compounds with `maxRad/maxTime/time/ignoreArmor`
  - still migrates the temporary modern `hfr_contamination` list into the legacy shape when encountered
- `CommonForgeEvents#handleContaminationEffects` now routes active contamination effects through `RadiationUtil.contaminate`:
  - `ignoreArmor == true` maps to `RAD_BYPASS`
  - `ignoreArmor == false` maps to `CREATIVE`
  - this restores `radEnv` accumulation, player tick-age/creative checks, radiation immunity, and resistance behavior.
- Added modern `RadiationConfig.ENABLE_MKU` to mirror legacy `ServerConfig.ENABLE_MKU`.
- `CommonForgeEvents#handleContagion` now respects `ENABLE_MKU`.
- Removed the modern-only `contagion == 1` final death behavior; legacy source does not actually trigger that branch because it checks the pre-decrement local value.
- Added `ArmorUtil#checkForMkuProtection` as a minimal bridge for the legacy Haz2 plus `HazardClass.BACTERIA` check:
  - requires the existing Haz2 radiation-resistance threshold
  - also requires a helmet that looks like known legacy bacteria-capable gear, or enough helmet resistance to represent a heavy suit head piece
  - this remains a bridge until the full `ArmorRegistry`/gas-mask-filter hazard class system is migrated.
- `FalloutLayerBlock` now allows survival on another fallout layer, matching the old stacked-fallout support intent without adding the non-legacy 8 visible height models.

Still not fully aligned:

- Strict fallout `metadata & 7 == 7` support cannot be represented exactly until a legacy metadata/state compatibility decision is made.
- The MKU bacteria protection bridge is not a full `ArmorRegistry.hasProtection(..., HazardClass.BACTERIA)` port.
- Radiation fog remains the modern particle bridge until `ParticleRadiationFog` and the old proxy effect path are migrated.
- Creeper nuclear transformation and Duck to Quackos remain blocked on missing entity migrations.
- PRISM/3D/NT/Blank radiation handlers and non-block radiation sources remain deferred.
- A generated block-radiation source table is still required before declaring all radiating blocks/ores fully aligned.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Radiation Core Parity Audit

Audit rule:

- 1.7.10 source remains the behavior source of truth.
- Modern helper classes may be kept only when they preserve old behavior or are clearly marked as debug/bridge code.
- Do not treat partially migrated simple chunk radiation as the complete 1.7.10 radiation library.

Confirmed aligned:

- `BlockFallout` does not use eight visible height models. It has metadata low bits for support rules, but its bounds remain a fixed 1/8 block height.
- Simple chunk radiation diffusion uses the old 3x3 percentages: center `0.6`, cardinal `0.075`, diagonal `0.025`, with the same `0.99` decay and `-0.05` subtraction when merging into existing entries.
- World radiation terrain mutation matches the old simple handler shape: 5 random radioactive chunks, 10 operation batches, each scanning 16x16 surface positions with a 1/3 random pass and converting grass/tallgrass/leaves.
- Legacy chunk NBT key is `hfr_simple_radiation`.

Not aligned / needs correction:

- Active contamination effects currently add radiation directly in `CommonForgeEvents#handleContaminationEffects`; legacy calls `ContaminationUtil.contaminate(..., RAD_BYPASS/CREATIVE, con.getRad())`. Current code therefore misses `radEnv` accumulation, player tick-age/creative checks, and radiation immunity checks for these entries.
- `RadiationData` saves migrated contamination entries in a new `hfr_contamination` list. Legacy NBT only writes `hfr_cont_count` and `cont_<index>` compounds. If strict save compatibility is required, write the legacy shape as canonical and treat the list only as an internal bridge or remove it.
- MKU contagion currently kills at `contagion == 1`; old `EntityEffectHandler` checks the local pre-decrement value `contagion == 0` inside `contagion > 0`, so the final kill does not actually fire in source behavior.
- MKU infection protection currently checks only heavy hazmat in some paths. Legacy uses `!ArmorUtil.checkForHaz2(...) || !ArmorRegistry.hasProtection(..., HazardClass.BACTERIA)` and is gated by `ServerConfig.ENABLE_MKU`.
- Creeper radiation transformation is incomplete: old code has a 1/3 chance to spawn `EntityCreeperNuclear`, otherwise deals 100 radiation damage. Current code only damages vanilla creepers because the nuclear creeper entity is not migrated.
- Duck to Quackos radiation transformation is missing because both legacy entities are not migrated.
- Radiation fog is a modern `MYCELIUM` particle bridge. Legacy uses `ParticleRadiationFog` / `radFog` proxy effect with the legacy texture/render path.
- `FalloutLayerBlock` lacks the old support rule for placement on another fallout block whose metadata low bits equal 7. This does not require eight rendered height models, but it does require a compatibility decision for the metadata/state contract.
- `RadiatingHazardBlock` is only a partial bridge for old `BlockHazard`. Old `BlockOre` has an independent `rad` field, and falling/radioactive blocks have separate classes. A per-block audit is required before assuming every radioactive block emits the correct chunk amount.
- `RadioactiveWasteEarthBlock` has now been audited and must not be treated as a chunk-radiation source. Old `WasteEarth` does not call `ChunkRadiationManager`; it is terrain damage output plus local waste-mycelium/entity behavior only.
- PRISM/3D/NT/Blank radiation handlers, pollution coupling, fallout rain entity, nuke fallout passes, gas/radon fallout placement, waste pearl placement, reactor/machine/fluid radiation emission sources, and Geiger machine integrations are not fully migrated.
- `/hbm radiation ...` command expansion is modern debug tooling. The old direct command surface is only `ntmrad clear` and `ntmrad set <amount>`; extra subcommands should remain documented as verification helpers, not legacy parity.

Next migration order:

1. Fix active contamination ticking to route through `RadiationUtil.contaminate` with the same legacy contamination type mapping, so `radEnv`, immunity, creative, and resistance behavior match.
2. Decide and implement canonical contamination NBT shape: prefer writing `hfr_cont_count` plus `cont_<index>` to mirror 1.7.10, while preserving read migration for the temporary list if needed.
3. Revert MKU final-death behavior to the legacy observable behavior unless an explicit intentional-bugfix decision is recorded.
4. Add the missing MKU bacteria armor/config gates before expanding contagion gameplay.
5. Build a generated block-radiation source table from 1.7.10 `ModBlocks` + `BlockHazard`/`BlockOre`/`BlockHazardFalling`/`BlockFallingRad` registrations, then compare it to current `ModBlocks` and `HazardRegistry` before changing more blocks.
6. After the source table is clean, repair chunk-radiation emitters for radiating ores, falling hazard blocks, waste/fallout blocks, and special migrated blocks.
7. Port legacy radiation fog assets/particle path or clearly keep the bridge disabled behind a parity note.
8. Only after chunk radiation sources are correct, migrate fallout rain and major non-block radiation emitters: nuke fallout, fluid vent radiation, gas/radon placement, reactor/machine emissions, and weapon/entity radiation injections.

## 2026-05-21 Contagion and Contamination Runtime Pass

Legacy source:

- `com.hbm.handler.EntityEffectHandler#handleContagion`
  - active MKU contagion decrements every tick from a 3 hour timer.
  - infected players randomly select one main-inventory slot every tick, with a 1% chance to select armor instead.
  - only unstackable items are tagged with `ntmContagion`.
  - uninfected players who carry a tagged unstackable item become infected for `3 * hour` unless protected by the legacy Haz2/bacteria protection checks.
  - nearby item entities are tagged with `ntmContagion` during aerial transmission.
  - late-stage contagion damage uses `ModDamageSource.mku`, not normal radiation damage.
- `com.hbm.extprop.HbmLivingProps.ContaminationEffect`
  - `time` is remaining time.
  - `getRad()` returns `maxRad * time / maxTime`.
  - each server tick applies that current radiation value and then decrements `time`.
- `com.hbm.lib.ModDamageSource`
  - `mku` is an absolute, armor-bypassing damage source.

Completed in the 1.20.1 port:

- Added player inventory contagion handling to `CommonForgeEvents`:
  - infected players tag random unstackable main-inventory/armor stacks with `ntmContagion`.
  - uninfected players can catch contagion from tagged unstackable stacks.
  - dropped item contagion tagging now uses the same tag helper.
- Corrected contamination-list ticking to use legacy remaining-time semantics:
  - per-tick radiation is now `maxRad * time / maxTime`.
  - `time` decrements and entries are removed at zero.
- Added modern `hbm:mku` damage type and `ModDamageSources.mku(Level)`.
- Routed contagion stage damage and final contagion death through `hbm:mku`.
- Tagged `hbm:mku` as bypassing armor, effects, and resistance to match the old absolute armor-bypass source.

Still incomplete:

- Legacy bacteria-specific armor protection is not restored yet; the current bridge uses the existing `ArmorUtil.checkForHaz2` approximation, matching the already-migrated aerial transmission guard.
- No dedicated client-side MKU death message/lang tuning yet beyond the damage type id.
- The old MKU enable config gate (`ServerConfig.ENABLE_MKU`) is not represented as a separate modern config option yet.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Fallout Layer Source Pass

Legacy source:

- `com.hbm.blocks.generic.BlockFallout`
  - registered legacy block id: `fallout`
  - material: sand
  - bounds: full X/Z, `0.125` block high
  - non-opaque and not a normal cube
  - drop item: `ModItems.fallout`
  - cannot be placed on ice or packed ice
  - can survive on leaves, solid movement-blocking opaque blocks, or another `fallout` block with layer metadata 7
  - no legacy dynamic height renderer/model is used; the block constructor sets one fixed 1/8-height bounds box
  - walking entity behavior:
    - server side only
    - living entities get `HbmPotion.radiation` for `10 * 60 * 20` ticks, amplifier `0`
    - creative players are ignored
  - left-click behavior:
    - server side only
    - adds `new ContaminationEffect(1F, 200, false)` to the player's long-term contamination list
- `com.hbm.hazard.HazardRegistry`
  - legacy registers `ModBlocks.fallout` as radiation hazard `fo * powder * 2`
  - legacy registers `ModItems.fallout` as radiation hazard `fo * powder`
  - legacy registers `ModBlocks.block_fallout` as radiation hazard `yc * block * powder_mult`

Completed in the 1.20.1 port:

- Added `FalloutLayerBlock` for legacy block id `fallout`.
- Registered `ModBlocks.FALLOUT` as a block-only registry entry, intentionally without a `BlockItem`, because `hbm:fallout` already exists as the legacy item id.
- Added `assets/hbm/blockstates/fallout.json` and a thin block model using the legacy `ash` block texture.
- Added `data/hbm/loot_tables/blocks/fallout.json` so the layer drops the existing `hbm:fallout` item.
- Added datagen loot support for the block-only fallout layer.
- Added `FalloutLayerBlock#stepOn` to apply the modern radiation effect bridge for 10 minutes at amplifier 0, matching legacy `HbmPotion.radiation`.
- Added `FalloutLayerBlock#attack` to append `RadiationData.addContamination(player, 1F, 200, 200, false)`, using the contamination-list library from Command Pass 3.
- Added `fallout` block hazard registration with `RadiationConstants.FALLOUT * RadiationConstants.POWDER_MULTIPLIER * 2.0F`, matching the old block/item multiplier relationship.
- Added a `layers=1..8` state bridge for the legacy metadata low 3 bits. All values still render with the same fixed 1/8-height model, because 1.7.10 does not use eight visible fallout height models.
- Tightened fallout support parity: another `fallout` block can support fallout above it only when `layers=8`, matching legacy `(metadata & 7) == 7`.
- Unsupported fallout now removes itself without drops on neighbor update, matching the old `setBlockToAir` path.

Deferred:

- The full fallout rain/world placement systems (`EntityFalloutRain`, nuke fallout passes, gas radon fallout placement, waste pearl placement) still need later migration.
- Creative-tab exposure still comes from the existing `fallout` item, not a separate block item, because the legacy item id is already occupied.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Fallout Metadata Audit and Block Radiation Source Pass

Legacy source:

- `com.hbm.blocks.generic.BlockFallout`
  - legacy metadata low 3 bits (`meta & 7`) represent layer height `0..7`.
  - the placed block is replaceable.
  - fallout can only be placed on another fallout block when the lower block metadata is `7`, i.e. a full 8-layer stack.
  - each placed layer still uses the same `fallout` block id and drops `ModItems.fallout`.
  - despite the metadata support rule, the legacy block does not override `setBlockBoundsBasedOnState` and does not use separate height textures/models; visually and collision-wise it remains a fixed 1/8-height layer.
- `com.hbm.blocks.generic.BlockHazard`
  - `onBlockAdded` computes `rad = HazardSystem.getHazardLevelFromStack(new ItemStack(this), HazardRegistry.RADIATION) * 0.1F`.
  - if `rad > 0`, schedules a block update every 20 ticks.
  - `updateTick` calls `ChunkRadiationManager.proxy.incrementRad(world, x, y, z, rad)` and reschedules.
- `com.hbm.blocks.generic.BlockOre`
  - radiating ore blocks with `this.rad > 0` schedule updates and call `ChunkRadiationManager.proxy.incrementRad(world, x, y, z, rad)` every 20 ticks.

Completed in the 1.20.1 port:

- Removed the modern-only 8-height fallout model variants after source audit showed 1.7.10 does not use them.
- Restored `FalloutLayerBlock` to a fixed 1/8-height model and collision shape, matching the legacy constructor bounds.
- Preserved only a metadata/state bridge (`layers=1..8`) for support-rule compatibility; each state maps to the same old thin model.
- Added `RadiatingHazardBlock`, a modern bridge for legacy `BlockHazard`/radiating `BlockOre` behavior:
  - on placement, schedules a server block tick when the block's own item has a registered `HazardType.RADIATION` level.
  - every 20 ticks, injects `HazardRegistry.getHazardLevel(new ItemStack(block.asItem()), RADIATION) * 0.1F` into `ChunkRadiationManager`.
  - reschedules itself while the block remains radiating.
- Routed generic simple resource blocks through `RadiatingHazardBlock`, so already-registered radioactive blocks/ores such as uranium ores, waste blocks, corium, yellowcake, fallout block, schrabidium blocks, radium/actinium blocks, etc. now actually seed chunk radiation after placement/loading ticks.

Still incomplete:

- Placement sources are still deferred: fallout rain, nuke fallout passes, radon/radiation gas placement, and waste pearl placement.
- No separate BlockItem exists for `hbm:fallout`; the existing legacy item remains the placement/creative-tab surface.
- Some non-generic special blocks that should emit chunk radiation may still need direct tick bridges when their full block classes are migrated.
- Server-side chunk radiation fog currently uses modern `MYCELIUM` particles as a bridge, not the old client `ParticleRadiationFog` texture/render path.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Waste Terrain Feedback Audit

User-reported symptom:

- In-game tests still showed effectively unlimited chunk-radiation spread.
- Far-away chunks could retain uneven high/low radiation, suggesting a self-amplifying source or stale runtime entries rather than just the old 3x3 Simple diffusion.

Legacy source re-check:

- `com.hbm.handler.radiation.ChunkRadiationHandlerSimple`
  - Default handler unless `RadiationConfig.enablePRISM=true`.
  - The 3x3 spread has no strict radius cutoff. New runtime entries receive raw spread, while entries already present in the previous buffer receive `*0.99F - 0.05F`.
  - `receiveChunkLoad` always inserts the loaded chunk with `hfr_simple_radiation`, including zero.
  - `receiveChunkUnload` appears to remove with `event.getChunk()` even though the map key is `ChunkCoordIntPair`; this is a legacy bug shape, not a feature to rely on for modern performance.
- `com.hbm.handler.radiation.ChunkRadiationHandlerPRISM`
  - Disabled by default.
  - Has thresholded loaded-chunk spreading (`amount <= 1F` stops) and correct `ChunkCoordIntPair` unload removal.
- `com.hbm.handler.radiation.ChunkRadiationHandlerNT`
  - Not selected by the observed default config path.
  - Uses pocket decay and thresholded connection spreading, so it is not the behavior currently being mirrored.
- `com.hbm.blocks.generic.WasteEarth`
  - Does not import or call `ChunkRadiationManager`.
  - `waste_earth` / `waste_mycelium` are terrain damage output, not continuous chunk-radiation sources.
  - `waste_mycelium` spread is gated by `GeneralConfig.enableMycelium`, default `false`.
  - `waste_earth` and `waste_mycelium` decay to dirt when `RadiationConfig.cleanupDeadDirt=true` or the block above is too dark/opaque.

Root cause in the 1.20.1 port:

- `RadioactiveWasteEarthBlock` added modern-only chunk radiation on placement/removal (`waste_earth=5F`, `waste_mycelium=15F`).
- Because the Simple terrain mutation turns grass into `waste_earth`, the modern port created a feedback loop: high chunk radiation killed grass, the new waste block injected more chunk radiation, then that new radiation spread and killed more terrain.
- This feedback loop is not present in 1.7.10 and can make the spread appear unlimited even beyond the old Simple handler's already unbounded low-value diffusion tail.

Completed:

- Removed chunk-radiation placement/removal injection from `RadioactiveWasteEarthBlock`.
- Changed `ModBlocks.WASTE_EARTH` and `ModBlocks.WASTE_MYCELIUM` registration to no longer carry artificial chunk-radiation values.
- Added config mirrors:
  - `radiation.cleanupDeadDirt`, default `false`, for legacy `RADWORLD_03_regrow`.
  - `radiation.enableMyceliumSpread`, default `false`, for legacy `GeneralConfig 1.01_enableMyceliumSpread`.
- Gated `waste_mycelium` spreading behind `enableMyceliumSpread`.
- Restored old decay-to-dirt behavior for waste terrain under the cleanup/dark-opaque condition.

Verification:

- 2026-05-21 attempted `.\gradlew.bat compileJava processResources --no-daemon`.
- Compile reached `:compileJava` and failed on an unrelated existing missing class: `com.hbm.ntm.menu.MachineBatteryMenu` referenced by `MachineBatteryBlockEntity`.
- No compile error was reported from the waste-terrain/radiation files changed in this pass.

## 2026-05-21 Special Source Metadata Variant Pass

Legacy source:

- `com.hbm.blocks.generic.BlockAbsorber`
  - Same block id `rad_absorber` uses metadata `0..3`.
  - Tiers are `BASE`, `RED`, `GREEN`, `PINK`.
  - Every 10 ticks it calls `ChunkRadiationManager.proxy.decrementRad` with `2.5F`, `10F`, `100F`, or `10000F`.
- `com.hbm.blocks.generic.BlockSellafield`
  - Same block id `sellafield` uses metadata `0..5`.
  - Every random tick it calls `ChunkRadiationManager.proxy.incrementRad(world, x, y, z, 0.5F * (meta + 1))`.
  - Randomly decays metadata downward and eventually becomes `sellafield_slaked`.

Completed in the 1.20.1 port:

- Added `LegacyStateBlockItem`, a small legacy metadata bridge for block items whose modern blockstate uses an `IntegerProperty`.
- Routed `rad_absorber` through `LegacyStateBlockItem`:
  - creative tab now exposes all 4 legacy tiers.
  - placement writes the selected tier into `LegacyRadAbsorberBlock.TIER`.
  - existing block tick behavior then absorbs the correct old amount.
- Routed `sellafield` through `LegacyStateBlockItem`:
  - creative tab now exposes all 6 legacy levels.
  - placement writes the selected level into `LegacySellafieldBlock.LEVEL`.
  - existing random tick behavior then emits `0.5F * (level + 1)` and decays like the old class.
- Added English and Chinese language keys for the new item variants.

Verification:

- 2026-05-21 attempted `.\gradlew.bat compileJava processResources --no-daemon`.
- Compile reached `:compileJava` and failed on an unrelated existing datagen/API mismatch in `HbmItemModelProvider` (`BuiltinEntityModelFile` / `BlocklessModelBuilder` generic signatures).
- No compile error was reported from `LegacyStateBlockItem`, `ModBlocks`, or `ModCreativeTabs`.

## 2026-05-21 Radiation Command Pass 2

Completed in the 1.20.1 port:

- Added immediate server-to-client radiation sync after debug command mutations for player radiation and digamma.
- Exposed `CommonForgeEvents.syncRadiationNow(ServerPlayer)` as the public sync bridge while keeping the existing periodic sync implementation private.
- Added `/hbm radiation status` debug commands for the long-term integer fields stored by `RadiationData`:
  - `asbestos`
  - `blacklung`
  - `bomb_timer`
  - `contagion`
  - `oil`
  - `fire`
  - `phosphorus`
  - `balefire`
  - `black_fire`
- Supported subcommands:
  - `/hbm radiation status get <field> [targets]`
  - `/hbm radiation status set <field> <targets> <amount>`
  - `/hbm radiation status add <field> <targets> <amount>`
  - `/hbm radiation status clear <field> <targets>`
- Status mutations reuse `RadiationData` setters and clamp to non-negative values at the command boundary.

Still incomplete:

- Chunk radiation mutations still do not need a player sync packet unless a later HUD/debug flow expects immediate local chunk readback.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-21 Radiation Command Pass 3

Legacy source:

- `com.hbm.extprop.HbmLivingProps`
  - `getCont(EntityLivingBase)` returns the active `List<ContaminationEffect>`.
  - `addCont(EntityLivingBase, ContaminationEffect)` appends a new long-term contamination effect.
  - `ContaminationEffect` fields:
    - `maxRad`
    - `maxTime`
    - `time`
    - `ignoreArmor`
  - `getRad()` computes `maxRad * time / maxTime`.
  - Legacy NBT stores the count in `hfr_cont_count` and entries as `cont_<index>` compounds with `maxRad/maxTime/time/ignoreArmor`.

Completed in the 1.20.1 port:

- Added safe contamination-list helpers to `RadiationData`:
  - `getContaminationEffects`
  - `getContaminationCount`
  - `removeContamination`
  - `clearContamination`
  - `ContaminationEffect` record with `currentRadiation()`
- Added `/hbm radiation contamination` debug commands:
  - `/hbm radiation contamination list [targets]`
  - `/hbm radiation contamination add <targets> <maxRad> <maxTime> [time] [ignoreArmor]`
  - `/hbm radiation contamination remove <targets> <index>`
  - `/hbm radiation contamination clear <targets>`
- Commands route through `RadiationData` instead of mutating the entity NBT directly.
- Added boolean suggestions for `ignoreArmor` (`false`, `true`) while accepting `true/yes/1` as truthy input.
- `PlayerRadiationSyncPacket` now serializes the active contamination list after the long-term integer fields, matching the old `HbmLivingProps#serialize` packet shape:
  - entry count
  - `maxRad`
  - `maxTime`
  - `time`
  - `ignoreArmor`
- `ClientRadiationData` now keeps an immutable client-side contamination snapshot and exposes:
  - `getContaminationEffects`
  - `getContaminationCount`
  - `ContaminationEffectData#currentRadiation`

Still incomplete:

- Actual gameplay sources for individual contamination-list entries still need follow-up migration where the source blocks/items/entities are ported. Existing obvious legacy source: `BlockFallout` adds `new ContaminationEffect(1F, 200, false)`.
- No HUD/overlay currently renders the contamination list; this pass only restores the old data sync contract for later UI and gameplay consumers.

Verification:

- 2026-05-21 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
- 2026-05-21 after adding contamination-list sync, ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

## 2026-05-22 Radiation Fog Visibility Pass

Legacy source:

- `com.hbm.handler.radiation.ChunkRadiationHandlerSimple`
  - Every 20 server ticks, after the 3x3 Simple diffusion update, any propagated value above `RadiationConfig.fogRad` has a `1:fogCh` chance to spawn `radFog` in the source chunk at `heightValue + rand(5)`.
- `com.hbm.particle.ParticleRadiationFog`
  - The old `radFog` particle uses `textures/particle/fog.png`, fullbright rendering, yellow-green color `(0.85F, 0.9F, 0.5F)`, alpha `sin(age * PI / 400) * 0.125F`, and draws 25 fog quads around one particle origin.

Completed in the 1.20.1 port:

- Kept the existing Simple-handler fog threshold/chance timing and origin-chunk placement rule.
- `ChunkRadiationManager#spawnRadiationFog` now emits one `RADIATION_FOG` particle event per selected old-style fog origin, matching legacy `radFog` event count.
- `RadiationFogParticle` itself now draws the old 25-quad fog mass with the fixed legacy random seed, yellow-green color, 400 tick alpha curve, `7.5F` scale envelope, fullbright light, and no depth writes.
- Added manual blockstate, block model, and item model resources for the registered radiation gases:
  - `gas_radon`
  - `gas_radon_dense`
  - `gas_radon_tomb`
  - `gas_meltdown`

Still incomplete:

- The modern particle renderer uses a dedicated particle render type and texture atlas, not direct legacy GL11 calls; blend/depth semantics are aligned, but exact old OpenGL state stack behavior is not reproduced.
- Gas visibility with `ashglasses` is still only partially represented by current gas particles/resources and needs a later render-library slice if the item/armor path is migrated.

Verification:

- 2026-05-22 ran `.\gradlew.bat processResources --no-daemon`: passed.
- 2026-05-22 attempted `.\gradlew.bat compileJava processResources --no-daemon`.
- Compile reached `:compileJava` and failed on an unrelated current battery-socket migration gap: `MachineBatterySocketBlockEntity` and `ModBlockEntities.MACHINE_BATTERY_SOCKET` are referenced but missing.
- 2026-05-22 after render-library fog correction, `.\gradlew.bat compileJava processResources --no-daemon` passed.
