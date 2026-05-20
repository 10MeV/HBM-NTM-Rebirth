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
