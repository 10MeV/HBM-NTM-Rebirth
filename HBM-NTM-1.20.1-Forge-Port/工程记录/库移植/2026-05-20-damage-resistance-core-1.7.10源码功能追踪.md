# 伤害与抗性核心 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 自定义伤害源、实体伤害工具、抗性注册与防具/实体抵抗逻辑。
- 该库被武器、爆炸、辐射、防具、实体、怪物系统复用。

## 1.7.10 源文件

- `src/main/java/com/hbm/lib/ModDamageSource.java`
- `src/main/java/com/hbm/util/EntityDamageUtil.java`
- `src/main/java/com/hbm/util/DamageResistanceHandler.java`
- `src/main/java/api/hbm/entity/IResistanceProvider.java`
- `src/main/java/api/hbm/entity/IRadiationImmune.java`
- `src/main/java/com/hbm/items/armor`
- `src/main/java/com/hbm/explosion`

## 旧版契约

- `ModDamageSource` 集中定义 HBM 自定义伤害字符串和 damage source。
- `EntityDamageUtil`：
  - 包含自定义伤害应用、击退、爆炸相关伤害、穿甲或绕过逻辑。
  - 有旧 SEDNA damage system 的实现片段。
- `DamageResistanceHandler`：
  - 按实体 class 注册抗性。
  - 可从 `IResistanceProvider` 查询实体自定义抗性。
  - 将 damage source 映射到抗性类别，如 physical、fire、explosion 等。
  - 与 FSB/动力甲等防具联动。
- 伤害类别和抗性值会影响武器、爆炸、实体特殊弱点。

## 迁移计划

- 先建立现代 `DamageType`/`DamageSource` 注册与旧字符串映射。
- 抗性注册数据可先迁成 Java registry，后续再考虑 datapack。
- `IResistanceProvider` 应保留为实体扩展接口。
- 爆炸和武器迁移前必须先迁基本伤害类别，否则数值会大幅偏移。

## 验证清单

- 旧伤害字符串能映射到现代 DamageType。
- 防具、实体内置抗性、接口抗性能共同生效。
- 创造模式、无敌、驯服实体等特殊逻辑不破坏原版规则。

## 2026-05-30 第一轮核心落地

- 新增现代 `com.hbm.ntm.damage` 包，迁移旧 `DamageResistanceHandler` 的核心数据模型：
  - `DamageClass`：保留 `PHYSICAL`、`FIRE`、`EXPLOSIVE`、`ELECTRIC`、`PLASMA`、`LASER`、`MICROWAVE`、`SUBATOMIC`、`OTHER`。
  - `DamageResistance`：对应旧 `Resistance`，字段为 DT threshold 与 DR resistance。
  - `DamageResistanceStats`：支持 exact、category、other 三层查询，查询顺序保持旧版 exact -> category -> other。
  - `DamageResistanceHandler`：提供 item、armor set、entity class 注册表，保留 `setup/reset` 临时穿甲缓存，并提供 `calculateDamage(...)` / `getDtDr(...)`。
- 新增 `com.hbm.ntm.api.entity.ResistanceProvider`，对应旧 `api.hbm.entity.IResistanceProvider`：
  - `getCurrentDtDr(DamageSource, amount, pierceDt, pierceDr)`。
  - `onDamageDealt(DamageSource, amount)`。
- Forge 事件接入：
  - `LivingAttackEvent`：非 absolute 伤害在 DT 完全吸收或 DR 达到 100% 时取消，保持旧版“攻击阶段可完全拦截”的行为。
  - `LivingHurtEvent`：应用 DT/DR 减免，并在实体实现 `ResistanceProvider` 时回调 `onDamageDealt`。
- 爆炸框架接入：
  - `EntityProcessorCross` 与 `EntityProcessorCrossSmooth` 通过 `EntityDamageUtil.attackEntityFromNt(...)` 攻击 living entity。
  - `EntityProcessorCrossSmooth#setupPiercing(...)` 现在会实际传入 `pierceDamageThreshold` / `pierceDamageResistance`，对齐旧 `EntityDamageUtil.attackEntityFromNT(...)` 的穿甲参数。
- 现代 DamageType 补齐：
  - `ModDamageSources` 新增 legacy key：`nuclear_blast`、`mud_poisoning`、`acid`、`tau_blast`、`suicide`、`blender`、`meteorite`、`boxcar`、`boat`、`building`、`ams`、`ams_core`、`broadcast`、`bang`、`lead`、`enervation`、`electricity`、`exhaust`、`spikes`、`lunar`、`vacuum`、`overdose`、`microwave`、`nitan`、`laser`、`plasma`、`subatomic`、`euthanized_self`、`euthanized_self2`、`revolver_bullet`、`chopper_bullet`、`tau`、`combine_ball`、`acid_player`、`boil`、`ice`、`flamethrower`。
  - 添加对应 `data/hbm/damage_type/*.json`，并将旧 `setDamageBypassesArmor` / `setDamageIsAbsolute` / `setDamageAllowedInCreativeMode` 语义映射到现代 vanilla damage type tags。
- 本轮边界：
  - 未迁 `hbmArmor.json` 配置读写与默认防具套装注册；现代端大量旧防具尚未完成注册，本轮只落核心注册表与计算 API。
  - 未迁旧 SEDNA 对 vanilla armor 内部计算的完整重写；现代端先通过 Forge damage events 与显式爆炸攻击入口承接 DT/DR。
  - 未迁防具 tooltip 展示，待防具物品批量移植时接入。

## 2026-05-30 第二轮扩展

- 新增 `DamageResistanceConfig`：
  - common setup 中从 `config/hbm/hbmArmor.json` 读取旧版同形配置。
  - 若配置不存在，写出 `config/hbm/_hbmArmor.json` 模板。
  - 模板保留旧版 item/entity ID 与 `itemStats`、`setStats`、`entityStats` 数组结构。
  - 现代端尚未注册的旧防具 ID 会在实际读配置时跳过，避免缺失物品导致崩溃；后续防具注册后可直接通过同名旧 ID 生效。
  - 默认实体抗性保留 creeper 爆炸抗性，并按 simple class name 支持未来旧实体类名映射。
- `DamageResistanceHandler` 扩展：
  - 新增 `clear()`、按 simple class name 注册实体抗性、按物品查询 item/set tooltip stats。
  - `DamageResistanceStats` 暴露只读 exact/category/other 视图，用于模板序列化和 tooltip。
- 新增 `DamageResistanceTooltipUtil` 并接入客户端 `ItemTooltipEvent`：
  - 显示套装抗性、单件抗性、类别/精确/other 的 DT/DR。
  - 新增 en_us / zh_cn 语言 key，并同步 datagen 语言 provider。
- 扩大伤害入口接入：
  - `ShrapnelEntity`、`RubbleEntity`。
  - `HbmEnergyDischargeEffects` 的自充电电池放电束和电爆半径伤害。
  - `ExplosionNukeGeneric.dealDamage`。
  - `HbmFluidContactEffects` 的 toxin direct damage。
- 仍未完成：
  - 完整旧默认防具表中 AJR/HEV/BJ/EnvSuit 等所有套装条目还未逐项录入；本轮先覆盖核心高价值/典型套装和模板机制。
  - `hbmArmor.json` 的错误项目前仅跳过，未输出逐项 warning。
  - vanilla armor 内部扣耐久/SEDNA 精确复刻仍未迁。

## 2026-05-30 第三轮扩展

- 默认抗性模板进一步对齐旧 `DamageResistanceHandler#initDefaults()`：
  - 补入 `ajr`、`ajro`、`ncrpa`、`bj`、`bj_jetpack`、`envsuit`、`hev`、`bismuth`、`taurun`、`trenchmaster`、hazmat 变体、liquidator、PAA hazmat 等套装条目。
  - `laser` exact resistance 改为现代 lowercase msg id，避免旧 `DamageClass.LASER.name()` 大写与现代 `DamageSource#getMsgId()` 小写不匹配。
- 修正缺失旧物品的安全边界：
  - 读取 `hbmArmor.json` 时，如果套装任意部件尚未迁移/注册，则跳过该套装，不再注册含 `null` 部件的套装，避免空护甲槽误匹配旧套装。
  - `LoadReport` 增加 skipped item/set 计数，日志可解释模板条目与实际生效条目的差异。
- 继续扩大伤害核心接入：
  - `TaintMobEffect` 的 taint 伤害。
  - `LegacyToxicGasBlock` 的 monoxide 伤害。
  - `ExplosionChaos.pc` / `ExplosionChaos.c` 的 pc/cloud 伤害。
  - `BlackHoleEntity` 的近距离 blackhole 伤害。
- 仍未完成：
  - 逐项 warning 尚未输出具体缺失 ID，只统计 skipped 数量。
  - 原版 `onFire`、`freeze`、`hotFloor` 等 vanilla source 是否纳入 `EntityDamageUtil` 仍需按旧 HBM 语义逐点审查，避免改变 Minecraft 原生环境伤害过多。

## 2026-05-30 第四轮扩展

- exact damage key 兼容层：
  - `DamageResistanceStats#addExact(...)` 现在会通过 `DamageResistanceHandler.exactTypeKey(...)` 规范化 key。
  - 规范化大小写不敏感，并忽略 `_`、`-`、`.`，兼容旧配置和现代 `damage_type` 文件之间的命名差异。
  - 明确映射旧/现代别名：`onFire`/`on_fire` -> `onfire`、`acidPlayer`/`acid_player` -> `acidplayer`、`tauBlast`/`tau_blast` -> `taublast`、`revolverBullet`/`revolver_bullet` -> `revolverbullet`、`chopperBullet`/`chopper_bullet` -> `chopperbullet`、`combine_ball` -> 旧 `cmb`、`nuclearBlast`/`nuclear_blast` -> `nuclearblast`、`mudPoisoning`/`mud_poisoning` -> `mudpoisoning` 等。
  - `typeToCategory(...)` fallback 和 energy 判定改用规范化 exact key，避免 `DamageSource#getMsgId()` 驼峰/下划线差异导致抗性失配。
- Tooltip/lang：
  - 补齐旧版 `damage.exact.drown/fall/laser/onfire` 对应的现代 tooltip key。
  - 补充常见 HBM exact damage key 的 tooltip 名称，包括 acid player、tau blast、revolver bullet、chopper bullet、combine ball、nuclear blast、mud poisoning。
- 清理：
  - 移除 `DamageResistanceStats` 中重复导入。
- 仍未完成：
  - 配置加载 skipped 项仍只统计数量，尚未列出具体缺失 ID。
  - 未扩大原版环境伤害入口，仍需逐点审查旧 HBM 语义后再接入。

## 2026-05-30 第五轮扩展

- `hbmArmor.json` 诊断增强：
  - `DamageResistanceConfig.LoadReport` 现在携带前 20 个缺失迁移 ID。
  - 启动日志会输出缺失 item 或 set component，便于后续防具/物品迁移后对照补齐。
  - 默认模板缺失项也会在 summary 中计数，避免“默认已加载”但大量旧防具尚未迁移时误判生效范围。
- 继续扩大伤害核心接入：
  - `EntityProcessorStandard` 的 VNT 标准爆炸伤害改为走 `EntityDamageUtil.attackEntityFromNt(...)`，与 Cross/CrossSmooth 处理器保持一致。
  - `HbmFluidContactEffects` 的冷热直接伤害改为走 `EntityDamageUtil`，使 HEV/envsuit 等 `onfire`、`fall/drown` exact 或 fire 类抗性可以参与计算。
  - `LegacyDemonLampBlockEntity` 近距离 `inFire` 伤害改为走 `EntityDamageUtil`。
  - `CommonForgeEvents` 中 HBM fire/phosphorus/balefire/black fire 状态造成的 `onFire` tick 伤害改为走 `EntityDamageUtil`。
  - `LegacyHotBlock` 的 `hotFloor` 接触伤害改为走 `EntityDamageUtil`。
- 本轮保留：
  - radiation/digamma/mku/asbestos/blackLung 等 direct `hurt(...)` 暂不改动；这些在现代端被标记为 bypass/absolute 或疾病终点伤害，按旧版 `setDamageIsAbsolute`/`setDamageBypassesArmor` 语义保持直接结算。

## 2026-05-30 第六轮扩展

- `EntityDamageUtil` 旧调用契约补强：
  - 新增现代兼容 overload：`attackEntityFromNt(entity, source, amount, ignoreIFrame, allowSpecialCancel, knockbackMultiplier, pierceDt, pierceDr)`。
  - 保留旧版 PVP/友伤检查入口：目标和来源均为玩家时调用现代 `Player#canHarmPlayer(...)`。
  - 支持 `ignoreIFrame`：攻击前临时清理 `LivingEntity#invulnerableTime`，失败时再清理并按原伤害重试；旧版追加 `lastDamage/lastHurt` 的穿透细节因现代字段访问受限暂不硬复刻。
  - 支持自定义击退倍率：保留攻击前速度并在成功后按旧版 `knockBack(...)` 公式叠加受击方向击退，同时尊重 `KNOCKBACK_RESISTANCE`。
  - 新增 `attackEntityFromIgnoreIFrame(...)` 便于后续化学雾、机枪塔、连续束流等旧调用点直接迁移。
- 高价值入口接入：
  - `EntityProcessorCrossSmooth` 改用高级 overload，按旧版 `EntityProcessorCrossSmooth` 语义启用 `ignoreIFrame=true`、`allowSpecialCancel=false`、无额外击退，并保留 DT/DR pierce。
  - `ExplosionNukeGeneric.dealDamage(...)` 改用高级 overload，按旧版核爆伤害使用 `ignoreIFrame=true` 与 `pierceDT=100`。
  - `HbmFluidContactEffects` 的冷热/毒素 direct damage 改用 `attackEntityFromIgnoreIFrame(...)`，对齐旧 `EntityMist`/`EntityChemical` 中持续流体伤害不会被无敌帧吞掉的行为。
- 仍未完成：
  - 未复刻旧 SEDNA 对 vanilla armor 内部计算和受伤流程的完整替换；现代端仍通过 Forge damage events 介入。
  - `allowSpecialCancel=false` 在现代端仍受原版/Forge `hurt(...)` 事件链约束，暂不绕过事件取消，以免破坏其他系统。

## 2026-05-30 第七轮扩展

- 配置诊断闭环：
  - `DamageResistanceConfig` 缓存最近一次 `LoadReport`，供运行中命令查询。
  - `/hbm damage resistance status` 输出当前默认/外部 `hbmArmor.json` 加载摘要，并列出前 20 个缺失迁移 ID。
  - `/hbm damage resistance reload` 运行中重载 `config/hbm/hbmArmor.json`，便于防具迁移后不重启验证 DT/DR 表。
- 运行中数值探针：
  - 新增 `/hbm damage resistance probe <targets> <damage_type> <amount> [pierce_dt] [pierce_dr]`。
  - 命令只读计算目标玩家当前装备和实体抗性下的 DT、DR、pierce 后最终伤害，不实际扣血。
  - 建议用完整 damage type ID，例如 `hbm:laser`、`hbm:explosion`、`minecraft:on_fire`。
- 计算安全边界：
  - `DamageResistanceHandler.calculateDamage(...)` 现在将 DR clamp 到 `0..1`，并将最终伤害 clamp 到 `>= 0`。
  - 这避免多个装备/实体来源叠加 DR 超过 100% 后产生负伤害，同时保留旧版“完全吸收为 0”的语义。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 新版源码差异补记

对比旧快照与新版 5714 源码：

- `MainRegistry.tMatSteel` 从 harvest level 2 / durability 500 / speed 7.5 改为 level 3 / durability 750 / speed 8.0；`tMatTitan` durability 从 750 改为 1000。
- `MainRegistry.aMatSteel` 从 durability 20、护甲 `{2,6,5,2}` 改为 durability 30、护甲 `{3,8,6,3}`。
- `DamageResistanceHandler` 给 steel armor 增加物理抗性 `dt=2, dr=0.1`，给 titanium armor 增加物理抗性 `dt=3, dr=0.1`。
- `ModItems` 给 steel sword 添加 `STUN`，给 steel pickaxe/axe/shovel 添加 `RECURSION`；这些属于装备能力/抗性迁移的新版事实来源。
  - 验证前清理过一次 `build/resources/main/.cache` 生成缓存；该目录属于 Gradle 输出，原因是 `processResources` 发现旧 MD5 缓存文件缺失。

## 2026-05-30 第八轮扩展

- `ModDamageSources` 旧工厂方法补强：
  - 新增 `bullet(...)`、`displacement(...)`、`tau(...)`、`combineBall(...)`、`subatomic(...)`、`euthanized(...)`。
  - 新增通用 `indirect(Level, ResourceKey<DamageType>, direct, cause)`，对应旧 `EntityDamageSourceIndirect` 工厂语义。
  - 新增现代 `hbm:euthanized` damage type，并将其加入 `bypasses_armor`，对齐旧 `ModDamageSource.euthanized(ent, hit)`。
- 现有投射物接入：
  - `ShrapnelEntity` 与 `RubbleEntity` 命中实体时改用 indirect damage source，direct source 为投射物自身，cause 为 `getOwner()`。
  - 当前 `LegacyThrowableEntity#getOwner()` 仍返回 `null`，但 source 结构已为后续带 owner 的武器/爆炸碎片迁移预留入口。
- exact key 兼容：
  - `subAtomic1` 到 `subAtomic5` 现在统一规范化为 `subatomic`，避免旧随机 subAtomic damage type 无法匹配现代 `hbm:subatomic` 抗性。
- `EntityDamageUtil` 迁移便利重载：
  - 新增 `attackEntityFromNt(..., ignoreIFrame)`、`attackEntityFromNt(..., ignoreIFrame, pierceDt, pierceDr)`、`attackEntityFromNt(..., ignoreIFrame, knockbackMultiplier, pierceDt, pierceDr)`。
  - 这些重载保留旧调用点常见参数形状，后续武器/弹药/连续伤害入口可以少写样板参数。
- 暂缓项：
  - 旧 `DamageResistanceHandler#onEntityDamaged` 中 “electric 伤害打到 `ArmorFSBPowered` 胸甲放大 5 倍” 已确认，但现代端尚无对应 FSB Powered 胸甲类/能量防具契约；本轮不写假判断，留待该防具迁移时接回。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 第九轮扩展

- 旧 `EntityDamageUtil#getMouseOver/rayTrace` 现代化底座：
  - 扩展 `RayTraceUtil#getMouseOver(Player, reach, threshold, partialTick)`。
  - 行为按旧版模型实现：先做方块射线，再沿视线扫描可命中实体，并按实体 pick radius + threshold 扩大命中盒，返回最近的方块或实体命中。
  - 保留 `rayTrace(...)` 与 `getPosition(...)` 作为现代武器/工具射线共用入口。
- `LegacyThrowableEntity` owner 契约：
  - 新增 `setOwner(Entity)`、UUID 保存/读取，以及服务端 `getOwner()` 解析。
  - 旧 `selfDamageDelay()` 现在能真正基于 owner 避免刚发射时打到自身。
  - `ShrapnelEntity` / `RubbleEntity` 的 indirect damage source 现在可以在有 owner 时携带 cause。
- 爆炸碎片 source 传递：
  - `ExplosionLarge` 的 rubble/shrapnel/tracer 生成入口新增带 `@Nullable Entity source` 的 overload。
  - `explode(...)`、`explodeFire(...)` 生成 legacy extras 时会把爆炸 source 传给碎片/弹片。
  - 无 source 的旧入口保留，避免破坏已有调用点。
- 验证中顺手修复的独立编译漂移：
  - `GenericMachineRecipe.Machine#serializer()` 改为 public，供 legacy recipe importer 输出现代 serializer ID。
  - `LegacyGenericRecipeImporter` 使用 `BuiltInRegistries.RECIPE_SERIALIZER.getKey(machine.serializer())` 与现有 `HbmItemOutput.of(...)` API。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 第十轮扩展

- 伤害抗性计算诊断结构：
  - 新增 `DamageResistanceHandler.ResistanceBreakdown`，集中输出 exact key、category、absolute 标记、raw DT/DR、pierce 后 effective DT/DR 与最终伤害。
  - `calculateDamage(...)` 改为复用 breakdown，避免命令、事件和实际伤害入口各自维护公式。
  - `LivingAttackEvent` 的完全吸收取消判定改为使用 `ResistanceBreakdown#fullyAbsorbed(...)`，与 `LivingHurtEvent` 最终伤害计算保持同一套 pierce 处理。
  - 保留旧版“攻击阶段可取消完全吸收伤害”的事件语义，但现代端不再沿用攻击阶段的减法式 DR pierce 漂移。
- 运行中诊断命令增强：
  - `/hbm damage resistance status` 现在额外输出实际注册表条目数：item stats、armor set stats、entity class/simple-name stats。
  - `/hbm damage resistance probe ...` 输出 exact key、category、raw DT/DR、pierce 后 effective DT/DR 与最终 result，便于核查旧配置 key 与现代 damage type 映射。
  - 新增 `/hbm damage resistance armor <targets>`，列出目标玩家四个护甲槽 item id、单件 stats、当前匹配 full set stats 与实体 innate stats。
- 顺手修复独立编译漂移：
  - `ModBlockEntities` 中补齐/保留 `fluid_duct_box`、`fluid_duct_gauge`、`fluid_duct_exhaust`、`fluid_pipe_anchor` 对应 BlockEntityType 注册，修复已有流体管道类引用缺失导致的编译失败。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 第十一轮扩展

- 旧版护甲扣耐久契约核对：
  - 复查旧 `DamageResistanceHandler#onEntityAttacked(...)`：完全吸收并取消攻击时会调用 `EntityDamageUtil.damageArmorNT(e, amount)`。
  - 复查旧 `EntityDamageUtil#damageArmorNT(...)`：该方法在 1.7.10 源码中为空方法。
  - 因此现代端本轮不凭空实现“完全吸收时扣护甲耐久”，保留当前不额外扣耐久的行为；若后续迁 SEDNA/防具模块发现具体实现来源，再单独接回。
- 抗性来源贡献诊断：
  - `DamageResistanceStats` 新增 `match(DamageSource)`，返回命中的层级：`exact`、`category` 或 `other`，以及命中 key 与 DT/DR。
  - `DamageResistanceHandler` 新增 `resistanceContributions(...)`，按旧计算顺序拆出贡献项：`ResistanceProvider`、完整套装、单件护甲、实体 innate stats。
  - `getDtDr(...)` 保持旧 API 形状，但内部改为汇总贡献项，避免贡献明细与总 DT/DR 公式分叉。
- 命令诊断增强：
  - `/hbm damage resistance probe ...` 现在在总结果后列出每个贡献项，显示 source、id、匹配层级、匹配 key、DT 与 DR。
  - 新增 `/hbm damage resistance matches <targets> <damage_type>`，用固定 amount=1 只检查当前装备/实体对某个 damage type 的命中项，适合调试 `hbmArmor.json` 与 exact/category 映射。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 第十二轮扩展

- `EntityDamageUtil` 旧参数语义补强：
  - 复查旧 `attackEntityFromNT(..., allowSpecialCancel, ...)`：SEDNA 路径中 `allowSpecialCancel=false` 会忽略 `ForgeHooks.onLivingAttack(...)` 的取消结果。
  - 现代端新增 `EntityDamageUtil.allowSpecialCancel()` 的 ThreadLocal 运行期状态，并在高级 `attackEntityFromNt(...)` 调用期间按参数设置。
  - `CommonForgeEvents#onLivingAttack(...)` 的 HBM 完全吸收取消现在尊重该状态：`allowSpecialCancel=false` 时不由 damage-resistance-core 取消攻击，让后续 hurt 阶段仍可计算最终伤害。
  - 现代端没有绕过 vanilla/Forge/其他 mod 的事件取消；该参数只承接 HBM 自身完全吸收取消的旧语义边界，避免过度复刻 SEDNA。
- 伤害应用结果诊断：
  - 新增 `EntityDamageUtil.DamageApplication`，记录 `damaged`、`fullyAbsorbed`、`requestedAmount`、`finalAmount` 与 `outcome`。
  - 新增 `attackEntityFromNtDetailed(...)`，保留现有 boolean overload 不变，后续武器/束流迁移可选择读取更细结果。
  - 结果对象中的 final amount 基于当前 `DamageResistanceHandler.ResistanceBreakdown` 预计算；若伤害被外部事件取消则 outcome 为 `canceled`。
- 命令诊断增强：
  - `/hbm damage resistance status` 额外显示运行期 `allowSpecialCancel`、当前 `pierceDT`、当前 `pierceDR`，用于排查嵌套伤害调用是否正确重置。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-30 第十三轮扩展

- HBM damage type 旧语义元数据：
  - `ModDamageSources` 新增 `LegacyDamageType` 元数据表，按旧 `ModDamageSource.java` 的 `setProjectile()`、`setExplosion()`、`setDamageBypassesArmor()`、`setDamageIsAbsolute()`、`setDamageAllowedInCreativeMode()` 记录现代 damage type 的预期语义。
  - 覆盖当前已迁的 HBM damage keys，包括实体/疾病类 absolute 源、投射物源、爆炸源、fire 类源，以及 bullet/tau/combine/subatomic 等 indirect 源。
  - 元数据目前只用于诊断与后续 datagen 对齐，不直接改变伤害结算。
- 运行时 damage type/tag 审计：
  - 新增 `/hbm damage resistance damageTypes`，遍历 `ModDamageSources.legacyDamageTypes()`，检查运行时 registry 是否存在对应 `hbm:*` damage type，并核对 projectile/explosion/fire/bypassesArmor/absolute/creativeAllowed 对应 vanilla damage type tags。
  - 新增 `/hbm damage resistance damageTypes list`，列出每个 legacy damage type 的预期语义，便于对照旧源码与现代资源。
  - 该审计能发现 `data/hbm/damage_type/*.json` 缺失、tag JSON 漏项或语义漂移，后续迁武器/弹药前可先跑一遍。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-31 第十四轮扩展

- 旧 damage string 解析闭环：
  - `ModDamageSources` 新增 `legacyKey(String)`，集中把 1.7.10 `DamageSource` 字符串、旧常量名和现代 namespaced ID 解析为 `ResourceKey<DamageType>`。
  - 覆盖旧 `ModDamageSource` 中的驼峰源：`nuclearBlast`、`mudPoisoning`、`euthanizedSelf`、`euthanizedSelf2`、`tauBlast`、`amsCore`、`blackLung`。
  - 覆盖旧 indirect/武器字符串：`revolverBullet`/`s_bullet`、`chopperBullet`/`s_emplacer`、`tau`/`s_tau`、`cmb`/`combineBall`/`s_combineball`、`subAtomic` 与 `subAtomic1..5`、`electrified`/`s_emp`、`acidPlayer`/`s_acid`、`immolator`/`cryolator` 等。
  - 覆盖常用 vanilla 环境别名：`onFire`、`inFire`、`hotFloor`、`freeze/frozen`、`fall`、`drown`、`playerAttack`、`mobAttack`。
- 现代工厂方法补强：
  - 新增 `source(Level, String)`、`source(Level, String, Entity)`、`indirect(Level, String, Entity, Entity)`，后续旧调用点可直接传旧字符串或 `hbm:*`/`minecraft:*` ID。
  - 新增 `isTau(DamageSource)` 与 `isSubatomic(DamageSource)`，对应旧 `ModDamageSource#getIsTau/getIsSubatomic`，供后续实体弱点/免疫逻辑迁移。
- 命令诊断增强：
  - `/hbm damage resistance matches` 与 `probe` 现在接受旧 damage 名称，如 `nuclearBlast`、`cmb`、`subAtomic3`、`onFire`。
  - `/hbm damage resistance damageTypes list` 显示每个现代 HBM damage type 的旧别名。
  - 新增 `/hbm damage resistance damageTypes resolve <damage_type>`，用于现场确认任意旧名或 ID 解析到的现代 key 与 registry 存在性。
- 验证：
  - 初次增量 `compileJava` 因 Gradle 状态未同步误报 `ModCommands#queryLegacyOreMapping` 缺失；用 `--rerun-tasks` 强制重编后通过。
  - `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks` 通过。

## 2026-05-31 第十五轮核心稳定

- 伤害类别 key 稳定：
  - 新增 `DamageResistanceHandler.categoryKey(...)`，把旧/现代类别写法统一到旧核心 key：`PHYS`、`FIRE`、`EXPL`、`EN`。
  - `DamageResistanceStats#addCategory(String/ DamageClass)` 现在统一走 category normalization，避免使用 `DamageClass.PHYSICAL` 时写入 `PHYSICAL` 而运行期 `typeToCategory(...)` 返回 `PHYS` 导致失配。
  - 能量类 `ELECTRIC`、`PLASMA`、`LASER`、`MICROWAVE`、`SUBATOMIC` 作为 category 输入时统一映射到旧 `EN`。
- 穿甲运行期状态稳定：
  - 新增 `DamageResistanceHandler.PierceState`、`capturePiercing()`、`restorePiercing(...)`。
  - `EntityDamageUtil.attackEntityFromNtDetailed(...)` 现在保存并恢复进入前的 pierce DT/DR，而不是无条件 `reset()`；这样嵌套伤害调用不会清掉外层旧 `EntityDamageUtil.attackEntityFromNT(...)` 语义中的穿甲上下文。
- 实体抗性接口/实体 innate 诊断稳定：
  - `ResistanceProvider#getCurrentDtDr(...)` 仍保持旧 `IResistanceProvider#getCurrentDTDR(...)` 的优先贡献顺序，后续装备、实体 innate 继续叠加。
  - 实体 innate 抗性查询增加 assignable class fallback：精确 class 优先，其次 simple class name，最后兼容父类/接口注册。旧配置中按具体 class 注册仍保持原优先级。
  - 贡献诊断里实体来源现在标明 `class:`、`simpleName:` 或 `assignable:`，便于排查配置命中来源。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。
