# 爆炸框架 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 爆炸算法、核爆射线、VNT 模块化爆炸接口和环境效果。
- 该库高风险，后续迁移应在方块/机器/辐射基础稳定后分阶段进行。

## 1.7.10 源文件

- `src/main/java/com/hbm/explosion/ExplosionNT.java`
- `src/main/java/com/hbm/explosion/ExplosionLarge.java`
- `src/main/java/com/hbm/explosion/ExplosionHurtUtil.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeGeneric.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeAdvanced.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeSmall.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeRayBatched.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeRayParallelized.java`
- `src/main/java/com/hbm/explosion/ExplosionNukeRayBalefire.java`
- `src/main/java/com/hbm/explosion/NukeEnvironmentalEffect.java`
- `src/main/java/com/hbm/explosion/vanillant/ExplosionVNT.java`
- `src/main/java/com/hbm/explosion/vanillant/interfaces`
- `src/main/java/com/hbm/explosion/vanillant/standard`

## 旧版契约

- 普通爆炸与核爆是多套实现并存：
  - `ExplosionNT`、`ExplosionLarge` 处理较常规的大范围破坏。
  - `ExplosionNuke*` 处理核爆、射线、分批或并行计算。
  - `ExplosionFleija`、`ExplosionBalefire`、`ExplosionChaos`、`ExplosionThermo` 等是特殊爆炸。
- VNT 是模块化爆炸框架：
  - `IBlockAllocator` 选择受影响方块。
  - `IBlockProcessor` 执行方块破坏。
  - `IBlockMutator` 修改方块结果，如火、碎片、balefire。
  - `IEntityProcessor` 与 `IPlayerProcessor` 处理实体和玩家。
  - `IDropChanceMutator`、`IFortuneMutator` 处理掉落。
  - `ICustomDamageHandler` 自定义伤害。
  - `IExplosionSFX` 控制视觉/声音效果。
- `ExplosionHurtUtil` 与伤害抗性系统交叉。
- `NukeEnvironmentalEffect` 与辐射、污染、方块替换和 fallout 联动。
- 部分爆炸使用压缩 packet 向客户端同步效果。

## 迁移计划

- 不在早期迁移真实核爆破坏，只先记录接口和数据契约。
- 优先迁 VNT 接口层，因为它能为普通武器爆炸提供现代可组合框架。
- 射线核爆需要单独做性能与线程安全设计，不能直接照搬旧并行代码。
- 环境后效应必须等辐射、污染、方块迁移稳定后接入。

## 2026-05-21 VNT 框架首批迁移实现

本批只迁移 1.7.10 `com.hbm.explosion.vanillant` 的安全公共骨架，不迁移真实核爆射线、分批核爆、balefire/fleija/chaos/thermo 后效应。

### 1.7.10 细化追踪

- `ExplosionVNT` 是编排器：
  - `IBlockAllocator` 先计算受影响方块集合。
  - `IEntityProcessor` 处理实体伤害/击退并返回玩家击退向量。
  - `IBlockProcessor` 执行方块破坏、掉落、后置变异。
  - `IPlayerProcessor` 在旧版用 `ExplosionKnockbackPacket` 同步玩家击退。
  - `IExplosionSFX` 执行声音、粒子和客户端效果包。
- `BlockAllocatorStandard`：
  - 默认 resolution `16`，扫描立方体外壳射线。
  - 每条射线初始能量为 `size * (0.7 + rand * 0.6)`。
  - 步长 `0.3`，每步基础衰减 `stepSize * 0.75`。
  - 非空气方块按爆炸抗性额外衰减 `(resistance + 0.3) * stepSize`。
- `BlockProcessorStandard`：
  - 默认掉落概率约 `1 / explosion.size`，可由 `IDropChanceMutator` 改写。
  - `IFortuneMutator` 可改写掉落 fortune。
  - `IBlockMutator` 分 `mutatePre` / `mutatePost` 两阶段，旧版常用于起火、碎片、balefire 或 bulkie 替换。
- `BlockProcessorNoDamage`：
  - 不破坏方块，只允许 mutator 观察/处理，并清空 affectedBlocks 让标准 SFX 不生成方块破坏粒子。
- `EntityProcessorStandard`：
  - 范围为 `size * 2`，可由 `IEntityRangeMutator` 修改。
  - 使用 vanilla `getBlockDensity` / 爆炸公式造成伤害和击退。
  - 可允许 self damage，可附加 `ICustomDamageHandler`。
- `EntityProcessorCross` / `EntityProcessorCrossSmooth`：
  - 使用多节点可见性，降低被中心点遮挡导致的伤害误差。
  - Smooth 版本接入旧 `EntityDamageUtil` / `DamageResistanceHandler.DamageClass.EXPLOSIVE`。
  - 本批暂不迁移，等待伤害抗性核心完整后再接。
- `ExplosionEffectStandard`：
  - 服务端播放 `random.explode`，并发送压缩方块位置包给客户端生成爆炸和烟雾。
  - 现代首批只使用原版服务端粒子和声音，不迁移压缩客户端包。

### 现代实现

- 新增 `com.hbm.ntm.explosion.vnt.ExplosionVnt`：
  - 保留旧版 fluent setter 和 `makeStandard()` 组合方式。
  - 只在服务端执行；客户端调用直接返回。
  - 内部保留现代 `Explosion compat`，供 Forge explosion event、方块抗性、loot、damage source 和未来兼容处理使用。
- 新增接口包 `com.hbm.ntm.explosion.vnt.interfaces`：
  - `BlockAllocator`、`BlockProcessor`、`BlockMutator`
  - `EntityProcessor`、`PlayerProcessor`
  - `DropChanceMutator`、`FortuneMutator`
  - `CustomDamageHandler`、`EntityRangeMutator`
  - `ExplosionEffect`
- 新增标准实现包 `com.hbm.ntm.explosion.vnt.standard`：
  - `BlockAllocatorStandard`
  - `BlockProcessorStandard`
  - `BlockProcessorNoDamage`
  - `BlockMutatorFire`
  - `DropChanceMutatorStandard`
  - `EntityProcessorStandard`
  - `PlayerProcessorStandard`
  - `ExplosionEffectStandard`
  - `ExplosionEffectTiny`
- `ModDamageSources` 新增现代 `hbm:explosion` damage type，供 VNT 实体处理器使用。

### 有意延期

- 未迁移旧 `ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket`，因为当前声音/粒子库只有通用 Aux/ParticleBurst；后续如果需要高保真客户端烟尘再补专用 packet。
- 未迁移 `EntityProcessorCross` / `EntityProcessorCrossSmooth`，因为 Smooth 与旧 bullet config、confetti、damage resistance 深耦合。
- 未迁移 `BlockAllocatorWater`、`BlockAllocatorBulkie`、`BlockMutatorBalefire`、`BlockMutatorDebris` 等特殊爆炸件。
- 未迁移核爆射线、并行爆炸、辐射/污染/fallout 环境后效应。

## 2026-05-21 Water/Bulkie allocator 与普通爆炸入口

### 1.7.10 细化追踪

- `BlockAllocatorWater`：
  - 与 `BlockAllocatorStandard` 同样使用外壳射线、默认外部指定 resolution。
  - 液体不消耗射线能量，也不会被加入受影响方块集合。
  - 非空气、非液体方块仍按爆炸抗性衰减并按 `shouldBlockExplode` 判断。
- `BlockAllocatorBulkie`：
  - 用于大范围挖掘/冲击波类爆炸，不使用剩余能量模型，而是沿射线推进到 `explosion.size` 距离。
  - 方块爆炸抗性高于 `maximum` 时停止该射线。
  - 旧版 `BlockAllocatorGlyphidDig` 是 bulkie 变体，额外把 `glyphid_spawner` 视为射线阻断；现代实现先提供 `Predicate<BlockState> immuneBlock` 钩子，不硬编码未迁移方块。
- 普通武器爆炸调用点：
  - 旧 `EntityBulletBaseNT` 对 explosive bullet 使用 `ExplosionVNT`，按 `config.blockDamage` 切换 `BlockProcessorStandard` / `BlockProcessorNoDamage`，按 `config.incendiary` 追加 `BlockMutatorFire`。
  - 旧 `ItemGrenadeDynamite`、`ItemGrenadeFilling.standardExplode/tinyExplode`、Sedna `Lego` / `XFactory*` 也走 VNT，但大量实体/武器类尚未迁入 clean port。

### 现代实现

- 新增 `BlockAllocatorWater`：
  - 现代语义用 `FluidState` / `BlockState#liquid()` 判断液体。
  - 保留旧版“液体不阻挡、不破坏”的水下爆炸语义。
- 新增 `BlockAllocatorBulkie`：
  - 保留 `maximumResistance` 与 resolution 构造。
  - 额外提供 `Predicate<BlockState>` 免疫方块钩子，承接旧 `BlockAllocatorGlyphidDig` 的特殊阻断需求。
- 新增 `WeaponExplosionUtil`：
  - `standard(...)` / `explodeStandard(...)`：普通武器爆炸入口，对齐旧 `EntityBulletBaseNT` 的 blockDamage/incendiary 组合。
  - `tiny(...)` / `explodeTiny(...)`：只处理实体与小型表现，不破坏方块。
  - `bulkie(...)`：给后续大范围工具、volatile/gold creeper、glyphid dig 等调用。
- `ExplosionVnt` 新增 `makeUnderwater(int resolution)` 便捷组合。
- 现有 clean port 内所有普通 `level.explode(...)` 调用点已改走 `WeaponExplosionUtil.explodeStandard(...)`：
  - 掉落物 `HYDROACTIVE` / `EXPLOSIVE` hazard 爆炸。
  - 玩家背包内 `HYDROACTIVE` / `EXPLOSIVE` hazard 触发。
  - `oil` 着火触发的实体爆炸。
- 新增 `data/minecraft/tags/damage_type/is_explosion.json`，让 `hbm:explosion` 被 blast protection 等 vanilla 爆炸标签识别。

### 仍未迁移

- 现代 clean port 尚无旧版 `EntityBulletBaseNT`、`EntityGrenadeUniversal`、Sedna `Lego/XFactory*`、HIMARS/Arty projectile 实体；本批以 `WeaponExplosionUtil` 作为这些后续类的接入点。
- `EntityProcessorCross` / `EntityProcessorCrossSmooth`、穿甲 DT/DR、damage resistance class、confetti、EMP/shrapnel/radiation increment 仍待对应库完整后迁移。

## 2026-05-21 VNT 常用处理器与效果扩展

### 1.7.10 细化追踪

- `BlockMutatorBulkie`：
  - `mutatePre` 只处理 normal cube。
  - 如果方块中心到爆炸中心距离大于等于 `explosion.size - 0.5`，将边缘方块替换成指定 `MetaBlock`。
- `BlockMutatorDebris`：
  - `mutatePost` 在空气位置检查六向邻居。
  - 若邻居是 normal cube 且不是目标 debris 方块，则在当前位置放置目标 debris。
- `BlockMutatorBalefire`：
  - 与火焰 mutator 类似，但放置旧 `ModBlocks.balefire`。
- `EntityProcessorCross`：
  - 对实体 AABB 到爆炸中心的最短距离做距离比例，而不是只用实体中心点。
  - 可配置 6 向辅助节点，取最大 block density，减少中心点遮挡带来的漏伤。
  - 聚合每个实体的最高伤害后再攻击；玩家击退向量仍返回给 player processor。
- `EntityProcessorCrossSmooth`：
  - 使用 `fixedDamage * (1 - distanceScaled)` 的平滑伤害。
  - density 小于 `0.125` 时伤害为 0。
  - 旧版接入 Sedna `DamageClass.EXPLOSIVE`、DT/DR 穿甲和 confetti。
- `CustomDamageHandlerAmat`：
  - 对命中的 living entity 追加 `radiation * (1 - distanceScaled) * explosion.size` 的辐射污染。
- `ExplosionEffectWeapon` / `ExplosionEffectAmat`：
  - Weapon 走旧 `ExplosionSmallCreator` 小型武器爆炸云。
  - Amat 播放普通/大型声音并发送 `type=amat` aux particle。

### 现代实现

- 新增 block mutator：
  - `BlockMutatorBulkie`
  - `BlockMutatorDebris`
  - `BlockMutatorBalefire`
- 新增实体处理器：
  - `EntityProcessorCross`
  - `EntityProcessorCrossSmooth`
- 新增自定义伤害/效果：
  - `CustomDamageHandlerAmat`
  - `ExplosionEffectWeapon`
  - `ExplosionEffectAmat`
- `HbmParticleEffects` 增加 `weaponExplosion` 与 `amat` aux particle 的最小现代表现。
- `WeaponExplosionUtil` 增加：
  - `smooth(...)`
  - `cross(...)`
  - `antimatter(...)`

### 有意降级/延期

- `BlockMutatorBalefire` 暂用 vanilla `SOUL_FIRE` 承接，因为 clean port 尚未迁移旧 `ModBlocks.balefire` 方块。
- `EntityProcessorCrossSmooth.setupPiercing(...)` 只保存 DT/DR 参数，不实际应用。旧穿甲依赖 `EntityDamageUtil.attackEntityFromNT` 与 `DamageResistanceHandler.DamageClass`，需等伤害抗性核心深化。
- `EntityProcessorCross.shouldDealKnockback(...)` 暂不排除旧 `EntityBulletBaseMK4` / `EntityGrenadeUniversal`，因为这些实体尚未迁入 clean port；后续迁入后再以接口或类型标记排除。
- `ExplosionEffectWeapon` 使用现代粒子桥的烟/火最小表现，不复制旧 `ExplosionSmallCreator` 的完整自定义粒子。

## 2026-05-21 旧爆炸工具层兼容迁移

### 1.7.10 细化追踪

- `ExplosionLarge`：
  - `spawnParticles` / `spawnParticlesRadial` / `spawnFoam` / `spawnShock` 通过 aux particle NBT 发送 `type=smoke`，以 `mode=cloud/radial/foamSplash/shock` 区分表现。
  - `spawnBurst` 使用 `ParticleUtil.spawnGasFlame` 生成径向火焰喷发。
  - `spawnRubble` 生成旧 `EntityRubble`；`spawnShrapnels` / `spawnTracers` / `spawnShrapnelShower` 生成旧 `EntityShrapnel`。
  - `spawnMissileDebris` 生成带随机速度的物品实体，稀有掉落有概率出现。
  - `explode` / `explodeFire` 先执行 vanilla 爆炸，再按开关生成 cloud/rubble/shrapnel；`buster` 沿向量连续爆破；`jolt` 沿随机射线清除液体或低抗性方块并抛出碎块。
  - 数量函数：`cloudFunction = 850 * (1 - exp(-i / 15)) + 15`，`rubbleFunction = i / 10`，`shrapnelFunction = i / 3`。
- `ExplosionHurtUtil`：
  - `doRadiation` 在半径 AABB 内枚举 `EntityLivingBase`，按距离在 outer/inner 间线性插值辐射量。
  - 使用 `HazardSystem` 的 `RADIATION + CREATIVE` 污染类型。
- `ExplosionNT`：
  - 旧版是 deprecated vanilla `Explosion` 子类，但仍被部分旧爆炸调用使用。
  - 属性集合包括 `FIRE`、`BALEFIRE`、`DIGAMMA`、`DIGAMMA_CIRCUIT`、`LAVA`、`LAVA_V`、`LAVA_R`、`ERRODE`、`ALLMOD`、`ALLDROP`、`NODROP`、`NOPARTICLE`、`NOSOUND`、`NOHURT`。
  - `nukeAttribs` 等价于 `FIRE + NOPARTICLE + NOSOUND + NODROP + NOHURT`。
  - 方块分配使用标准射线，默认 resolution 为 16。
  - 掉落：`NODROP` 禁掉落，`ALLDROP` 全掉落，否则按爆炸衰减概率。
  - `ERRODE` 把 `concrete/concrete_smooth` 侵蚀成 gravel，`brick_concrete` 侵蚀成 `brick_concrete_broken`，`brick_concrete_broken` 侵蚀成 gravel。
  - `FIRE` / `BALEFIRE` / `LAVA` 在爆后空气且下方实体方块处放置对应方块；非 `ALLMOD` 时约 1/3 概率。

### 现代实现

- 新增 `ExplosionHurtUtil`：
  - 保留 `doRadiation(Level, x, y, z, outer, inner, radius)` 入口。
  - 使用现代 `LivingEntity`、`AABB` 和 `RadiationUtil.contaminate(..., HazardType.RADIATION, ContaminationType.CREATIVE, ...)`。
- 新增 `ExplosionLarge`：
  - 烟云、径向烟、泡沫、shock 继续走 `ParticleUtil.spawnAux`。
  - `spawnBurst` 继续走现代 `ParticleUtil.spawnGasFlame`。
  - `explode` / `explodeFire` 改走 `WeaponExplosionUtil.explodeStandard`，统一进入 VNT。
  - `spawnMissileDebris` 生成现代 `ItemEntity` 并复制 `ItemStack`。
  - `buster` 沿归一化向量连续调用 VNT 普通爆炸。
  - `jolt` 在服务端沿随机射线清液体/低抗性方块，并用 block particle 表现碎块。
- 新增 `ExplosionNT` 兼容层：
  - 保留旧 `ExAttrib` 枚举与 `NUKE_ATTRIBS` 组合。
  - 通过 `ExplosionVnt` + `BlockAllocatorStandard(resolution)` 执行。
  - `NODROP` / `ALLDROP` 接 `BlockProcessorStandard`。
  - `NOPARTICLE` / `NOSOUND` 通过 `ExplosionEffectStandard(sound, particles)` 控制。
  - `NOHURT` 禁用实体处理器；否则使用 `EntityProcessorStandard + PlayerProcessorStandard`。
  - `FIRE` / `BALEFIRE` / `LAVA` / `ERRODE` 通过 block mutator 组合实现。
- 新增 VNT 支撑类：
  - `CompositeBlockMutator`：按顺序组合多个 pre/post block mutator。
  - `BlockMutatorLava`：爆后放置 lava。
  - `BlockMutatorErode`：承接旧 concrete/brick_concrete 侵蚀映射。
  - `BlockMutatorFire` / `BlockMutatorBalefire` 增加 `always` 构造，用于 `ALLMOD`。
  - `ExplosionEffectStandard` 增加 sound/particle 开关，默认行为保持不变。

### 有意降级/延期

- 旧 `EntityRubble` 与 `EntityShrapnel` 尚未迁移；`ExplosionLarge.spawnRubble` / shrapnel/tracer/shower 当前只生成粒子表现，不产生实体碰撞伤害或可拾取碎块。
- `ExplosionLarge.jolt` 不生成旧 rubble 实体，只清方块并发送 block particle；破坏阈值仍保留爆炸抗性 `<= 70`。
- `ExplosionNT.DIGAMMA` / `DIGAMMA_CIRCUIT` / `LAVA_V` / `LAVA_R` 已在 2026-05-24 补齐爆炸属性所需的旧 ID 方块落点；完整 volcanic/rad lava 流体反应仍后续迁。
- `ExplosionNT` 不继承 vanilla `Explosion`，而是作为旧调用语义到 VNT 的兼容适配器；后续迁调用点时应优先直接使用 VNT/WeaponExplosionUtil，只有旧代码属性组合复杂时才用该适配器。

## 2026-05-22 环境后处理与小型核爆入口

### 1.7.10 细化追踪

- `NukeEnvironmentalEffect`：
  - `applyStandardAOE` 在球形/随机边缘区域内调用 `applyStandardEffect`。
  - `applyStandardEffect` 把 sand 转 trinitite，mycelium 转 waste_mycelium，log/planks 转 waste_log/waste_planks，mossy cobblestone 转 oil ore，coal ore 低概率转 diamond ore，uranium/nether uranium/nether plutonium 低概率转 schrabidium 系矿，end stone 低概率转 tikite，clay 转 hardened clay，可燃方块转 fire。
- `ExplosionThermo`：
  - `freeze` / `scorch` / `scorchLight` 都在 `bombStartStrength * 2` 半径内做带随机边缘的球形方块替换。
  - `freezeDest`：草/土/原木/木板转 frozen 系列，石头/圆石/石砖转 packed ice，叶子转 snow，lava 转 obsidian，water 转 ice。
  - `scorchDest`：草转 dirt，dirt 转 netherrack，netherrack/stone/cobble/stonebrick/obsidian 转 lava，log/planks 转 waste 系列，叶子/water/ice 被清除，packed ice 转 water。
  - `scorchDestLight` 是弱版，不把石头大量转 lava；额外支持 waste_earth -> netherrack、obsidian -> gravel_obsidian、sand -> glass、clay -> stained hardened clay。
  - `freezer` 给半径内非 ocelot living entity 造 3x3x3 近似冰笼，并施加 weakness/slowness/mining fatigue。
  - `setEntitiesOnFire` 给半径内实体点燃 10 秒，living 额外 weakness；旧版玩家 asbestos 防火。
- `ExplosionNukeSmall`：
  - 发送 `type=muke` / `tinytot` aux particle，`muke` 有 1% 概率 balefire 特效。
  - 播放 `hbm:weapon.mukeExplosion`。
  - 可生成 shrapnel，mini-nuke 且非 safe 时走 `ExplosionNT` 属性爆炸。
  - `killRadius` 通过 `ExplosionNukeGeneric.dealDamage` 造成高额中心衰减伤害。
  - mini-nuke 在中心周围 5x5 chunk 的曼哈顿菱形区域增加 chunk radiation。

### 现代实现

- 新增 `NukeEnvironmentalEffect`：
  - 保留 `applyStandardAOE` / `applyStandardEffect` 入口。
  - 使用现代 `Level`、`BlockPos`、`BlockTags`、`BaseFireBlock`。
  - 已迁已注册方块映射：`waste_mycelium`、`ore_oil`、`ore_schrabidium`、`ore_nether_schrabidium`、`ore_tikite`、`block_trinitite`。
- 新增 `ExplosionThermo`：
  - 保留 `freeze`、`snow`、`scorch`、`scorchLight`、`freezeDest`、`scorchDest`、`scorchDestLight`、`freezer`、`setEntitiesOnFire`。
  - 现代实体效果使用 `MobEffects.WEAKNESS`、`MOVEMENT_SLOWDOWN`、`DIG_SLOWDOWN`。
  - 玩家 asbestos 防火暂用护甲描述 ID 包含 `asbestos` 的轻量兼容判断。
- 新增 `ExplosionNukeGeneric`：
  - 当前只迁 `dealDamage` / `dealDamage(..., maxDamage)`。
  - 使用视线 `ClipContext` 做遮挡判断，中心距离线性衰减伤害，点燃 5 秒并施加轻微击退。
- 新增 `ExplosionNukeSmall`：
  - 提供 `PARAMS_SAFE`、`PARAMS_TOTS`、`PARAMS_LOW`、`PARAMS_MEDIUM`、`PARAMS_HIGH` 和可复制/链式配置的 `MukeParams`。
  - mini-nuke 破坏走 `ExplosionNT`，辐射走 `ChunkRadiationManager.incrementRadiation`。
- 声音/粒子：
  - 复制旧 `assets/hbm/sounds/weapon/mukeExplosion.ogg`。
  - `ModSounds` / `sounds.json` 注册 `weapon.mukeExplosion`。
  - `HbmParticleEffects` 增加 `muke` / `tinytot` 的最小 mushroom-cloud 表现。

### 有意降级/延期

- `ExplosionNukeSmall.PARAMS_HIGH` 旧版会生成 `EntityNukeExplosionMK5`，现代实现暂不生成完整大型核爆实体；目前以 VNT/`ExplosionNT` 和大烟云作为可编译占位。
- `ExplosionNukeGeneric` 只迁 `dealDamage`，未迁 `vapor`、`solinium`、EMP 等完整核爆方块后处理。
- `ExplosionThermo` 中未注册的旧方块 `frozen_grass/frozen_dirt/frozen_log/frozen_planks/waste_log/waste_planks/waste_trinitite(_red)/volcanic_lava_block` 使用 vanilla 近似或跳过对应替换；等这些方块迁入后可把 helper 映射自动升级为原方块。
- `NukeEnvironmentalEffect` 的旧 red sand -> `waste_trinitite_red` 目前合并到已迁 `block_trinitite`，因为 clean port 尚无对应废土三硝石方块。

## 2026-05-22 可保存进度的核爆柱状处理器

### 1.7.10 细化追踪

- `ExplosionSolinium`：
  - 构造参数：中心 `x/y/z`、`World`、半径 `rad`、上下形变系数 `coefficient/coefficient2`。
  - 保存字段：`posX/posY/posZ`、`lastposX/lastposZ`、`radius/radius2`、`n/nlimit`、`shell/leg/element`、`explosionCoefficient/explosionCoefficient2`。
  - `update()` 每次先处理上一列，再按方形螺旋公式推进到下一列，直到 `n > radius2 * 4`。
  - `breakColumn` 对圆形范围内的每个柱，按 `sqrt(radius2 - x*x - z*z)` 得出高度，纵向调用 `ExplosionNukeGeneric.solinium`。
- `ExplosionBalefire`：
  - 保存字段同样覆盖中心、上一列、半径、螺旋进度。
  - `update()` 采用同一套方形螺旋推进。
  - `breakColumn` 从世界表面向下清柱；深度为 `10 + radius * 0.25` 乘距离比例，再叠加正弦扰动。
  - 遇到 `block_schrabidium_cluster` 时有 1/10 概率在上方放 `balefire`，并把 cluster 转为 `block_euphemium_cluster`，然后停止该列。
  - 清到目标深度后有 1/10 概率在深度上方放 `balefire`；深度下方 5 格内 stone 转 `sellafield_slaked`。
- `ExplosionNukeGeneric.solinium`：
  - 草/mycelium/waste_earth/waste_mycelium 转 dirt。
  - cactus/coral/leaves/plants/sponge/vine/gourd/wood 材质方块清空。

### 现代实现

- 新增 `ExplosionSolinium`：
  - 保留旧字段名、构造参数形状、`saveToNbt(CompoundTag, String)` / `readFromNbt(CompoundTag, String)`。
  - 使用现代 `Level`，服务端执行；螺旋推进和上下系数计算按旧公式迁移。
  - 每列调用现代 `ExplosionNukeGeneric.solinium(Level, BlockPos)`。
- 新增 `ExplosionBalefire`：
  - 保留旧字段名、NBT 保存/读取、螺旋推进。
  - 使用 `Heightmap.Types.WORLD_SURFACE` 获取柱表面。
  - `block_schrabidium_cluster` / `block_euphemium_cluster` / `sellafield_slaked` 使用 clean port 已注册 legacy block。
  - 旧 `balefire` 方块未迁时回退为 vanilla `SOUL_FIRE`。
- 扩展 `ExplosionNukeGeneric`：
  - 新增 `solinium(Level, int, int, int)` 与 `solinium(Level, BlockPos)`。
  - 使用现代标签/方块类型近似旧材质清理：leaves/logs/planks、cactus/vine/melon/pumpkin/sponge、`BushBlock`、可替换方块。

### 有意降级/延期

- `ExplosionBalefire` 只迁核心处理器，不生成旧 `EntityBalefire` 逻辑实体；后续迁实体时可直接持有并 tick 这个处理器。
- `ExplosionSolinium` 只迁核心处理器，不生成旧 `EntityNukeExplosionMK3`；后续 MK3 实体可复用本类的 NBT 字段。
- 旧 `balefire` 方块未迁，现代处理器用 `SOUL_FIRE` 兜底；等方块注册后 `ModBlocks.legacyBlock("balefire")` 会自动优先使用原方块。

## 2026-05-22 ExplosionNukeGeneric 公共工具扩展

### 1.7.10 细化追踪

- `empBlast`：
  - 在 `bombStartStrength` 半径球内调用 `emp`。
  - `emp` 对旧 `IEnergyHandlerMK2` 直接 `setPower(0)`，20% 概率替换成 `block_electrical_scrap`。
  - 对 RF `IEnergyProvider` 六面抽空能量，约 40% 概率替换成 `block_electrical_scrap`。
- `vapor` / `vaporDest`：
  - `vapor` 在 `bombStartStrength * 2` 的球内调用 `vaporDest`。
  - `vaporDest` 清除极低抗性、web、red cable、液体，以及中低抗性且非完整立方体的方块，但保留 chest/farmland。
  - 可燃方块上方为空气时点火。
  - 返回 `explosionResistance / 300`，供旧 `ExplosionNukeAdvanced` 的纵向推进抵消使用。
- `waste` / `wasteNoSchrab`：
  - 在随机边缘球内调用 `wasteDest` / `wasteDestNoSchrab`。
  - grass -> waste_earth，mycelium -> waste_mycelium，sand 低概率 -> waste_trinitite(_red)，clay -> hardened clay，mossy cobble -> coal/oil 相关，coal ore 低概率转 diamond/emerald。
  - log/planks/mushroom stem 转 waste_log/waste_planks，其它 mushroom cap 可清空。
  - `waste` 额外把 uranium/nether uranium/gneiss uranium 低概率转 schrabidium，否则转 scorched uranium；`wasteNoSchrab` 不做 schrabidium 分支。

### 现代实现

- `ExplosionNukeGeneric` 新增：
  - `empBlast(Level, int, int, int, int)`
  - `emp(Level, int, int, int)` / `emp(Level, BlockPos)`
  - `vapor(Level, int, int, int, int)`
  - `vaporDest(Level, int, int, int)` / `vaporDest(Level, BlockPos)`
  - `waste(Level, int, int, int, int)` / `wasteNoSchrab(Level, int, int, int, int)`
  - `wasteDest` / `wasteDestNoSchrab` 的坐标与 `BlockPos` 重载。
- EMP 现代适配：
  - 对 `HbmEnergyHandler` 直接清零。
  - 对 Forge `ForgeCapabilities.ENERGY` 各面和 null side 抽空可抽取能量。
  - 按旧概率替换成已注册 `block_electrical_scrap`。
- vapor 现代适配：
  - 使用 `Block#getExplosionResistance()`、`FluidState`、`BlockState#liquid()`、`isCollisionShapeFullBlock`。
  - 保留 red cable、web、液体、chest/farmland、可燃点火语义。
- waste 现代适配：
  - 使用 `BlockTags.DOORS/LEAVES/LOGS/PLANKS` 覆盖 1.20 方块族。
  - 已注册 uranium/scorched/schrabidium/gneiss/nether 资源方块按 legacy name 映射。

### 有意降级/延期

- `waste_trinitite` / `waste_trinitite_red`、`waste_log`、`waste_planks` 尚未注册；现代实现使用 `block_trinitite`、`waste_leaves` 或 vanilla 木板作为近似回退。
- `VersatileConfig.getSchrabOreChance()` 尚未迁入现代配置；当前 schrabidium 分支使用固定 1/10，后续配置系统补全后应替换。
- `emp` 目前只处理 HBM HE 与 Forge Energy capability；旧 CoFH RF 接口已由 Forge Energy 抽象覆盖，未单独保留 CoFH API 类型。

## 2026-05-22 ExplosionNukeAdvanced 柱状后处理

### 1.7.10 细化追踪

- `ExplosionNukeAdvanced`：
  - 保存字段：`posX/posY/posZ`、`lastposX/lastposZ`、`radius/radius2`、`n/nlimit`、`shell/leg/element`、`explosionCoefficient`、`type`。
  - 构造时 `explosionCoefficient = clamp((rad + coefficient * (y - 60)) / (coefficient * rad), 1 / coefficient, 1)`，用于控制地下/地上纵向形变。
  - `update()` 每次处理上一列，然后用同一套方形螺旋推进下一列。
  - `type=0` 调用 `breakColumn`，逐格执行 `ExplosionNukeGeneric.destruction`；当世界 Y 小于 8 时额外按返回 protection 跳过若干格。
  - `type=1` 调用 `vapor`，逐格执行 `ExplosionNukeGeneric.vaporDest` 并按返回值跳格。
  - `type=2` 调用 `waste`，半径大于等于 95 时用 `wasteDest`，否则用 `wasteDestNoSchrab`。
- `ExplosionNukeGeneric.destruction`：
  - 低抗性方块直接清空。
  - 高抗性方块返回 `explosionResistance / 300` 作为 protection。
  - 特例：`brick_concrete` 小概率转 gravel，`brick_light` 小概率转 waste_planks 或 block_scrap，`brick_obsidian` 小概率转 obsidian，obsidian 转 gravel_obsidian，其它高抗性方块小概率转 block_scrap。

### 现代实现

- 新增 `ExplosionNukeAdvanced`：
  - 保留旧字段名、构造参数形状、NBT 保存/读取、螺旋推进公式。
  - 增加 `TYPE_DESTRUCTION` / `TYPE_VAPOR` / `TYPE_WASTE` 常量，数值仍为 `0/1/2`。
  - 直接复用现代 `ExplosionNukeGeneric.destruction`、`vaporDest`、`wasteDest`、`wasteDestNoSchrab`。
- 扩展 `ExplosionNukeGeneric`：
  - 新增 `destruction(Level, int, int, int)` 与 `destruction(Level, BlockPos)`。
  - 已注册方块映射：`brick_concrete`、`brick_light`、`brick_obsidian`、`gravel_obsidian`、`block_scrap`。

### 有意降级/延期

- `ExplosionNukeAdvanced` 只迁核心柱状后处理器，不生成旧逻辑实体；后续 `EntityNukeExplosionMK3/MK5` 类可持有并 tick 这个处理器。
- `brick_light -> waste_planks` 在 `waste_planks` 未注册时回退到 vanilla 深色木板。
- 旧版 `ExplosionNukeAdvanced` 与完整射线核爆实体的调度关系尚未迁，本批只保证处理器本身可编译、可保存进度、可被后续实体复用。

## 2026-05-22 ExplosionFleija 柱状清除器

### 1.7.10 细化追踪

- `ExplosionFleija`：
  - 保存字段：`posX/posY/posZ`、`lastposX/lastposZ`、`radius/radius2`、`n/nlimit`、`shell/leg/element`、`explosionCoefficient/explosionCoefficient2`。
  - 构造参数：中心坐标、`World`、半径、上下纵向系数。
  - `update()` 与 `ExplosionSolinium`/`ExplosionNukeAdvanced` 相同，按方形螺旋逐列推进。
  - `breakColumn` 在椭球范围内自上而下清空方块，遇到 `DecoBlockAlt` 装饰方块时跳过保护。

### 现代实现

- 新增 `ExplosionFleija`：
  - 保留旧字段名、NBT 保存/读取、构造参数形状和螺旋推进公式。
  - 使用 `Level` 和 `BlockPos.MutableBlockPos` 实现服务端逐列清除。
  - 旧 `DecoBlockAlt` 保护以现代 registry key 近似：`hbm` 命名空间且 legacy path 以 `deco_` 开头的方块不清除。

### 有意降级/延期

- 当前只迁核心处理器，不生成旧 `EntityNukeExplosionMK5` 或 Fleija 实体调度。
- `DecoBlockAlt` 在 1.20.1 clean port 中尚未完整保留类型层级，因此先按 legacy ID 约定保护装饰方块。

## 2026-05-22 ExplosionNukeRayBatched / Balefire

### 1.7.10 细化追踪

- `IExplosionRay`：
  - 过程式爆炸统一接口，分为 `cacheChunksTick`、`destructionTick`、`cancel`、`isComplete`。
- `ExplosionNukeRayBatched`：
  - 构造参数：`World`、中心、`strength`、`speed`、`length`。
  - 使用 generalized spiral points 在球面上生成射线方向。
  - `collectTip` 沿每条射线用方块爆炸抗性消耗剩余强度，记录每条射线最后命中的 tip，并把 tip 按 chunk 分组缓存。
  - chunk 按与爆心 chunk 的曼哈顿距离排序。
  - `processChunk` 逐 chunk 回放射线路径，非 tip 方块清空，tip 由 `handleTip` 处理。
  - `cacheChunksTick` 每 tick 收集 `speed * 10` 条射线；`destructionTick` 按毫秒预算处理 chunk。
  - `masqueradeResistance` 特例：sandstone 视为 stone，obsidian 视为 stone * 3。
- `ExplosionNukeRayBalefire`：
  - 继承 batched 版本，只覆盖 tip 处理。
  - tip 下方顶面实体且 1/5 概率时放 `balefire`，否则清空。

### 现代实现

- 新增 `ExplosionRay`：
  - 保留旧 `IExplosionRay` 的四个调度方法，供后续 MK5 逻辑实体复用。
- 新增 `ExplosionNukeRayBatched`：
  - 使用现代 `Level`、`ChunkPos`、`Vec3`、`BlockPos`。
  - 保留 GSP 射线生成、按 chunk 缓存、chunk 排序、按时间预算销毁的旧结构。
  - `processChunk` 避免访问世界高度外坐标；方块清除使用现代 `setBlock(..., AIR, flags)`。
  - `masqueradeResistance` 使用现代 `BlockState`，保留 sandstone/obsidian 特例。
- 新增 `ExplosionNukeRayBalefire`：
  - tip 下方用 `isFaceSturdy(..., Direction.UP)` 判断支撑面。
  - `balefire` 方块未注册时回退 `SOUL_FIRE`，注册后自动优先使用 `ModBlocks.legacyBlock("balefire")`。

### 有意降级/延期

- 尚未迁 `ExplosionNukeRayParallelized` 的多线程版本；本批选择 batched 版本作为安全的主线程可预算实现。
- 尚未接入 `EntityNukeExplosionMK5`，但现代 `ExplosionRay` 接口和 batched 实现已经保留后续调度入口。
- `balefire` 方块和完整 balefire 环境副作用未迁时只使用 `SOUL_FIRE` 占位表现。

## 2026-05-22 ExplosionChaos / ExplosionTom 安全子集

### 1.7.10 细化追踪

- `ExplosionChaos`：
  - `hardenVirus`：半径球阈值 `r*r/2` 内把 `crystal_virus` 转 `crystal_hardened`。
  - `igniteFlammableBlocks`：同阈值球内，方块可燃且上方空气时点火。
  - `igniteAllBlocks`：同阈值球内，非空气方块上方为空气或雪层时点火。
  - `spawnPoisonCloud` / `spawnVolley`：生成旧 `EntityCloudFX`、`EntityPinkCloudFX`、`EntityOrangeFX` 粒子实体。
  - `cluster`：生成旧 `EntityBulletBaseMK4`，使用 `XFactoryCatapult.cluster_submunition`。
  - `poison`：半径内 living，若没有 GAS_LUNG/GAS_BLISTERING 防护则施加 blindness/poison/wither/slowness/mining fatigue；有防护则损耗滤芯。
  - `pc` / `c`：半径内 living 损耗全套护甲并分别造成 `ModDamageSource.pc` / `cloud` 伤害；`c` 对 hazmat 免疫，并处理旧 taint -> mutation。
  - `floater`：半径球阈值内移动方块到 `y + height`，保留 block/meta。
  - `move`：半径盒内实体被命名 Dinnerbone/Grumm/jeb_，距离内实体整体平移。
- `ExplosionTom`：
  - 保存字段：`posX/posY/posZ`、`lastposX/lastposZ`、`radius/radius2`、`n/nlimit`、`shell/leg/element`。
  - 使用与其它柱状处理器相同的方形螺旋 `update()`。
  - `breakColumn` 基于固定 `terrain=63`、指数 crater floor/ring/rim 公式逐柱生成大型陨坑。
  - crater floor 以下填 `tektite`，1/200 概率填 `ore_tektite_osmiridium`。
  - terrain 以上且 crater rim 内清空旧地形、水/冰/雪/可燃邻域；terrain 以下把水/冰/空气邻域转 lava。

### 现代实现

- 新增 `ExplosionChaos`：
  - 迁入不需要旧弹药实体或未迁系统的安全子集：`hardenVirus`、两类点火、`spawnPoisonCloud`、`spawnVolley`、`poison`、`pc`、`c`、`floater`、`move`。
  - 粒子表现改走现代 `ParticleUtil.spawnAux`，客户端 `HbmParticleEffects` 新增 `chaosCloud`，用 dust + cloud 近似旧彩色 FX。
  - `poison` 使用现代 `ArmorUtil.hasLungGasProtection` 近似旧 GAS_LUNG/GAS_BLISTERING；有防护时损耗头盔耐久。
  - `pc` / `c` 新增现代 damage type：`hbm:pc`、`hbm:cloud`，并损耗穿戴护甲。
  - `c` 暂不处理旧 taint -> mutation，因为现代 clean port 尚无 taint effect。
  - `move` 对 sheep 命名 `jeb_`，其它非玩家 living 随机命名 Dinnerbone/Grumm，并按旧逻辑平移实体。
- 新增 `ExplosionTom`：
  - 保留旧字段名、构造参数形状、NBT 保存/读取、螺旋推进公式。
  - 使用现代 `Level`、`BlockPos.MutableBlockPos`、`BlockTags`。
  - `tektite` / `ore_tektite_osmiridium` 优先查 `ModBlocks.legacyBlock`；未注册时分别回退 `BLACKSTONE` / `DEEPSLATE_DIAMOND_ORE`。
  - 用 `FluidState`、ice、snow/leaves/logs/planks、`ignitedByLava` 近似旧 material water/ice/snow/canBurn。

### 有意降级/延期

- `ExplosionChaos.cluster` 未迁：依赖旧 `EntityBulletBaseMK4` 与 Sedna `XFactoryCatapult` 弹药系统，clean port 当前没有对应实体/工厂。
- 旧 `EntityCloudFX` / `EntityOrangeFX` / `EntityPinkCloudFX` 未逐个迁移；现代先用 Aux 粒子近似，不创建持久客户端实体。
- 旧 `ArmorRegistry.hasAnyProtection` 与 gas mask filter 真实滤芯损耗尚未完整迁；现代只通过 helmet 耐久近似防护消耗。
- `ExplosionTom` 的 tektite 方块与 osmiridium tektite 矿尚未注册，因此当前 fallback 只保证行为/地形流程可运行，后续资源方块迁入后无需改算法即可自动使用 legacy 方块。

## 2026-05-22 VNT GlyphidDig / Parallelized 兼容入口

### 1.7.10 细化追踪

- `BlockAllocatorGlyphidDig`：
  - 与 `BlockAllocatorBulkie` 同样从爆心沿立方体表面方向做 0.3 步长射线。
  - 构造参数为最大可挖抗性 `maximum`，默认 `resolution=16`。
  - 遇到抗性高于 `maximum` 的方块停止该射线。
  - 额外保护 `ModBlocks.glyphid_spawner`：即使抗性不高也停止射线，不加入破坏集合。
  - 旧调用点来自 glyphid 生物挖掘行为，当前 clean port 尚未迁 glyphid 实体。
- `ExplosionNukeRayParallelized`：
  - 旧版用于 MK5 核爆射线，构造参数为 `World`、中心坐标、`strength/speed/radius`。
  - 后台线程生成球面射线，按 subchunk 快照读取方块抗性，并把待破坏位图合并到 chunk destruction map。
  - `cacheChunksTick` 负责预算式拉取/缓存 subchunk 快照；`destructionTick` 再按预算直接修改 chunk storage。
  - 旧 `EntityNukeExplosionMK5` 中 parallelized 调用已被注释，实际默认使用 `ExplosionNukeRayBatched`。

### 现代实现

- 新增 `BlockAllocatorGlyphidDig`：
  - 继承现代 `BlockAllocatorBulkie`，复用最大抗性停止逻辑。
  - 通过 `ModBlocks.legacyBlock("glyphid_spawner")` 做 glyphid spawner 保护；方块未注册时保护条件自然为 false，后续注册后自动生效。
- 扩展 `WeaponExplosionUtil`：
  - 新增 `glyphidDig(...)` 工厂，保留 future glyphid/entity 调用点需要的 VNT 组合。
- 新增 `ExplosionNukeRayParallelized`：
  - 保留现代 `ExplosionRay` 四段接口和旧构造参数形状。
  - 内部委托 `ExplosionNukeRayBatched`，继续使用主线程预算 `cacheChunksTick` / `destructionTick`。

### 有意降级/延期

- 未迁 1.7.10 线程版的 subchunk snapshot / bitset / direct chunk storage 写入；1.20.1 下跨线程读取和直接改 chunk storage 风险高，本批只提供 API 兼容入口。
- `glyphid_spawner` 与 glyphid mob/entity 当前尚未迁入 clean port；本批只把爆炸库侧 allocator 准备好，后续生物迁移可直接接入。

## 2026-05-22 EntityNukeExplosionMK5 核心调度实体

### 1.7.10 细化追踪

- `EntityNukeExplosionMK5`：
  - 继承旧 `EntityExplosionChunkloading`，字段包含 `strength/speed/length`、`fallout`、`falloutAdd`、`explosionStart`、`IExplosionRay explosion`。
  - `onUpdate()` 在服务端加载爆心 chunk，并给玩家触发 Manhattan 成就。
  - 前 10 tick 且 `fallout && strength >= 75` 时调用 `radiate(2_500_000F / (ticksExisted * 5 + 1), length * 2)`。
  - 每 tick 调用 `ExplosionNukeGeneric.dealDamage(world, pos, length * 2)`。
  - 首 tick 初始化 `ExplosionNukeRayBatched`；旧 parallelized 分支在源码中已注释。
  - 未完成时按 `BombConfig.mk5` 调用 `cacheChunksTick` / `destructionTick`。
  - 完成后若 `fallout` 为 true，生成旧 `EntityFalloutRain`，scale 为 `(length * 2.5 + falloutAdd) * BombConfig.falloutRange / 100`。
  - `setDead()` 会 cancel 当前 `IExplosionRay`。
  - NBT 仅保存 `ticksExisted`，旧字段多依赖新创建时设置。

### 现代实现

- 新增 `NukeExplosionMk5Entity`：
  - 注册为 `hbm:entity_nuke_explosion_mk5`，客户端用 `NoopRenderer`，实体本身不可见。
  - 保留 `strength/speed/length/fallout/falloutAdd` 与 `create(...)` / `createNoFallout(...)` / `moreFallout(...)` 入口。
  - 服务端首 tick 初始化 `ExplosionNukeRayBatched`，每 tick按固定 25ms 预算调用 cache/destruction。
  - 保留中心衰减伤害：每 tick 调 `ExplosionNukeGeneric.dealDamage(..., length * 2)`。
  - 迁入早期辐射：对 `length * 2` 范围内 living 按视线路径方块抗性衰减，并通过现代 `RadiationUtil` 施加 `RAD_BYPASS` 辐射。
  - `remove` 时取消 ray worker，避免实体被移除后继续持有破坏任务。

### 有意降级/延期

- 旧 `EntityExplosionChunkloading` 的强制 chunk loader 已在 2026-05-24 迁入现代 forced chunk 基类；本段为 MK5 初始迁入时的历史限制。
- 旧 `EntityFalloutRain` 已在后续批次迁入安全子集并接回 MK5 完成路径；本段为 MK5 初始迁入时的历史限制。
- 旧 `BombConfig.mk5` / `falloutRange` / `enableExtendedLogging` 尚未迁入现代 config，本批使用固定 25ms tick 预算和 100% fallout 范围。
- Manhattan 成就系统尚未迁，现代实体不触发成就。

## 2026-05-22 BombConfig 子集 / ExplosionNukeSmall 大核弹接线

### 1.7.10 细化追踪

- `BombConfig`：
  - nuke radius 默认值：`gadget=150`、`boy=120`、`man=175`、`mike=250`、`tsar=500`、`prototype=150`、`fleija=50`、`solinium=150`、`n2=200`、`missile=100`、`mirv=100`、`fatman=35`、`nuka=25`、`aSchrab=20`。
  - explosion 配置默认值：`mk5=50` ms、`blastSpeed=1024`、`falloutRange=100`、`fDelay=4`、`limitExplosionLifespan=0`、`chunkloading=true`、`explosionAlgorithm=2`。
- `ExplosionNukeSmall.PARAMS_HIGH`：
  - 旧版 `miniNuke=false`，`blastRadius=BombConfig.fatmanRadius`，`shrapnelCount=0`。
  - `explode(...)` 中 `miniNuke=false` 时调用 `WorldUtil.loadAndSpawnEntityInWorld(EntityNukeExplosionMK5.statFac(...))`，真正进入 MK5 射线核爆。

### 现代实现

- 新增现代 `config.BombConfig`：
  - 挂到 `hbm-common.toml`，分为 `nukes` 与 `explosions` 两段。
  - 迁入旧半径默认值和 MK5/fallout/blastSpeed/chunkloading/explosionAlgorithm 等共享爆炸配置项。
- 更新 `NukeExplosionMk5Entity`：
  - `radius=0` 时使用 `BombConfig.NUKA_RADIUS`。
  - tick 预算从 `BombConfig.MK5_BUDGET_MS` 读取。
  - fallback fallout footprint 范围从 `BombConfig.FALLOUT_RANGE_PERCENT` 读取。
- 更新 `ExplosionNukeSmall`：
  - `PARAMS_HIGH` 的默认半径使用 `BombConfig.FATMAN_RADIUS_DEFAULT`。
  - `miniNuke=false` 时除了已迁的烟云表现外，现在会 `addFreshEntity(NukeExplosionMk5Entity.create(...))`，恢复旧版大核弹进入 MK5 调度实体的路径。
  - 新增 `configuredHighParams()` / `explodeConfiguredHigh(...)`，供后续 Fatman、Tier0 missile 等调用点按运行期 `BombConfig.FATMAN_RADIUS` 生成参数，避免静态 `PARAMS_HIGH` 缓存旧半径。

### 有意降级/延期

- 现代 `PARAMS_HIGH` 仍是静态默认参数；后续核弹方块/物品接线应优先使用 `configuredHighParams()` 或 `explodeConfiguredHigh(...)`。
- `BombConfig.CHUNK_LOADING` 和 `EXPLOSION_ALGORITHM` 已迁配置入口，但当前现代实现仍采用安全 batched worker，不启用旧 direct chunk storage/threaded 写入。
- `ExplosionNukeSmall` 的大核弹路径已通过 `NukeExplosionMk5Entity` 继承的现代 forced chunk 基类承接旧强制加载语义；仍未迁 `WorldUtil.loadAndSpawnEntityInWorld` 的其它通用行为。

## 2026-05-22 EntityFalloutRain 安全子集

### 1.7.10 细化追踪

- `EntityFalloutRain`：
  - 继承旧 `EntityExplosionChunkloading`，尺寸 `4 x 20`，不可见、免疫火焰。
  - `scale` 存在 data watcher 16；NBT 保存 `scale`、`chunks`、`outerChunks`。
  - 首 tick 调 `gatherChunks()`，用中心向外的采样圆收集内圈 chunk 和外圈 chunk，之后按预算分批处理。
  - 每 `BombConfig.fDelay` tick 执行一次 chunk 处理，每次处理时间受 `BombConfig.mk5` 限制。
  - `stomp(x,z,dist)` 自世界顶部向下扫描，深度到 3 个 solid 后停止。
  - 顶部可放置时按 `0.1 - (dist/100 - 0.7)^2` 概率铺 `fallout`。
  - `dist < 65` 时可燃方块上方有 1/5 概率点火。
  - 旧版还会处理 `volcano_core -> volcano_rad_core`、`FalloutConfigJSON` 地表替换、crater biome、下方空气时生成 `EntityFallingBlockNT`。

### 现代实现

- 新增 `FalloutRainEntity`：
  - 注册为 `hbm:entity_fallout_rain`，客户端 `NoopRenderer`，保持不可见逻辑实体。
  - 保留 `scale`、`chunksToProcess`、`outerChunksToProcess`、`firstTick`、`tickDelay` 和 NBT 保存/读取。
  - 使用现代 `ChunkPos.asLong` / `long[]` 保存 chunk 队列。
  - 按 `BombConfig.FALLOUT_DELAY` 与 `BombConfig.MK5_BUDGET_MS` 预算分批处理。
  - 对已加载区域按 chunk 扫描，使用 `Heightmap.Types.WORLD_SURFACE` 找地表，迁入 fallout 铺设概率和近中心点火逻辑。
- 更新 `NukeExplosionMk5Entity`：
  - MK5 射线完成后不再直接调用一次性 `ExplosionNukeGeneric.waste` fallback，而是 spawn `FalloutRainEntity.create(...)`，恢复旧版“完成后落尘实体继续分批处理”的结构。

### 有意降级/延期

- 未迁 crater biome 与 `FalloutConfigJSON` 地表替换规则；现代先保留 fallout 层和点火这两个直接可运行行为。
- 未迁 `EntityFallingBlockNT` 抛落地表方块，也未迁 volcano_core 特例；依赖方块/实体尚未进入 clean port。
- 旧 `EntityExplosionChunkloading` 强制加载能力已在 2026-05-24 接入；落尘实体现在会保持爆心 chunk，并在处理每个 chunk 前切换工作 chunk ticket。

## 2026-05-24 EntityNukeExplosionMK3 核心调度实体

### 1.7.10 细化追踪

- `EntityNukeExplosionMK3`：
  - 继承旧 `EntityExplosionChunkloading`，注册 ID 为 `entity_nuke_mk3`。
  - 字段包含 `age/destructionRange/speed/coefficient/coefficient2/did/did2/waste/extType`。
  - `waste=true` 时持有三个 `ExplosionNukeAdvanced`：destruction 半径 `range`、waste 半径 `range * 1.8`、vapor 半径 `range * 2.5`。
  - `waste=false && extType=0` 时持有 `ExplosionFleija`；`extType=1` 时持有 `ExplosionSolinium`。
  - 首 tick 初始化处理器；每 tick `speed += 1`，循环执行处理器 update。
  - destruction 未完成时，普通/Fleija 路径调用 `ExplosionNukeGeneric.dealDamage(range * 2)`；Solinium 路径调用 `ExplosionHurtUtil.doRadiation(15000, 250000, range)`。
  - waste destruction 完成后只生成一次 `EntityFalloutRain`，scale 为 `range * 1.8`。
  - `statFacFleija(...)` 创建 `waste=false` 的 Fleija 实体，`makeSol()` 切为 Solinium。
  - `ATEntry` 反瞬移/干扰器逻辑可取消 300 格内的 Fleija 爆炸，并播放 ufoBlast/plasmablast 效果。

### 现代实现

- 新增 `NukeExplosionMk3Entity`：
  - 注册为 `hbm:entity_nuke_mk3`，客户端 `NoopRenderer`。
  - 保留 `createFleija(...)`、`createWaste(...)`、`makeSol()` 入口，后续核弹方块和武器可直接 spawn。
  - 复用现代 `ExplosionNukeAdvanced`、`ExplosionFleija`、`ExplosionSolinium`、`ExplosionHurtUtil` 和 `FalloutRainEntity`。
  - NBT 保存/读取旧字段名和各处理器进度前缀：`exp_`、`wst_`、`vap_`、`expl_`、`sol_`。
  - `speed` 初始值从现代 `BombConfig.BLAST_SPEED` 读取，并按旧逻辑每 tick 自增。

### 有意降级/延期

- 旧 `EntityExplosionChunkloading` 强制加载能力已在 2026-05-24 接入，MK3 现在会保持爆心 chunk ticket。
- 未迁 `ATEntry` 的 field disturber 抵消逻辑、ufoBlast 音效与 plasmablast 粒子；相关机器/粒子还未完整迁入。
- 未触发旧 Manhattan 成就和 extended logging。

## 2026-05-24 ExplosionLarge / ExplosionNT 旧调用签名兼容

### 1.7.10 细化追踪

- `ExplosionLarge.spawnTracers(world, x, y, z, count)`：旧普通武器常用的无 motion 参数入口，内部生成低速 tracer。
- `ExplosionLarge.spawnShrapnelShower(world, x, y, z, motionX, motionY, motionZ, count, deviation)`：旧版按给定基准速度和高斯偏差发射碎片雨。
- `ExplosionLarge.spawnMissileDebris(world, x, y, z, motionX, motionY, motionZ, deviation, debris, rareDrop)`：
  - 对每个 debris stack 随机生成 `0..stackSize` 个 1-count 掉落物。
  - 掉落物速度继承传入 motion，并叠加 deviation 后乘 `0.85`。
  - 稀有掉落为 `1/10` 概率，偏差缩小到 `deviation * 0.1`。
  - 初始位置沿速度方向前推两格，避免全部堆在爆心。
- `ExplosionLarge.explodeFire(...)`、`buster(...)`、`jolt(...)` 在旧调用点里存在无 source、float depth、double strength 等重载形态。
- `ExplosionNT` 旧代码常用 `addAllAttrib(...)` 链式 API，而当前现代实现只有 `addAttrib(...)`。

### 现代实现

- 扩展 `ExplosionLarge`：
  - 新增无 motion 的 `spawnTracers(...)`，委托到现代 tracer 粒子视觉。
  - 新增带方向/偏差的 `spawnShrapnelShower(...)`，使用 `ServerLevel.sendParticles(..., count=0)` 保留单粒子速度语义。
  - 新增带方向/偏差的 `spawnMissileDebris(...)`，按旧版 stack 随机散落、稀有掉落概率和速度继承逻辑生成 `ItemEntity`。
  - 新增 `explodeFire(..., source=null)`、`buster(..., float depth)`、`jolt(..., double strength)` 兼容入口，减少后续普通武器调用点迁移时的业务改写。
- 扩展 `ExplosionNT`：
  - 新增 `addAllAttrib(ExAttrib...)` 和 `addAllAttrib(Collection<ExAttrib>)`，直接委托现代 `addAttrib`。
  - 新增 `hasAttrib(...)` 供后续迁移旧属性判断逻辑时复用。

### 有意延期

- 旧 `EntityShrapnel` / `EntityRubble` / tracer projectile 已在 2026-05-24 后续批次迁入；本段保留为旧调用签名批次时的历史限制。
- `ExplosionNT.ExAttrib.DIGAMMA`、`DIGAMMA_CIRCUIT`、`LAVA_V`、`LAVA_R` 的爆炸落点已在 2026-05-24 后续批次接通；本段保留为旧调用签名批次时的历史限制。

## 2026-05-24 EntityExplosionChunkloading 现代等价

### 1.7.10 细化追踪

- `EntityExplosionChunkloading`：
  - 继承旧 `Entity`，实现 `IChunkLoader`。
  - `entityInit()` 通过 `ForgeChunkManager.requestTicket(MainRegistry.instance, worldObj, Type.ENTITY)` 获取 entity ticket。
  - `init(ticket)` 在服务端绑定实体并强制加载实体当前 `chunkCoordX/chunkCoordZ`。
  - `loadChunk(x, z)` 只记录并 force 一个额外工作 chunk；旧实现未在切换时主动释放旧工作 chunk。
  - `clearChunkLoader()` 在实体结束时释放 ticket。
- 旧使用方：
  - `EntityNukeExplosionMK5`：持续处理 MK5 射线爆炸，必须保持爆心区域 tick。
  - `EntityNukeExplosionMK3`：持续处理 Fleija/Solinium/waste 三类大爆炸。
  - `EntityFalloutRain`：分批处理落尘 chunk，旧结构允许在处理 chunk 时调用 `loadChunk`。

### 现代实现

- 新增 `ExplosionChunkLoadingEntity`：
  - 使用 Forge 1.20.1 `ForgeChunkManager.forceChunk(ServerLevel, modId, Entity, chunkX, chunkZ, add, ticking)` 迁入 entity ticket 语义。
  - `forceCenterChunk()` 保持爆心 chunk 强制加载。
  - `loadChunk(chunkX, chunkZ)` 迁入旧“当前工作 chunk”入口；现代实现切换时释放上一工作 chunk，避免大范围落尘处理留下过多持久 ticket。
  - `clearChunkLoader()` 在 `remove(...)` 时释放中心 chunk 与工作 chunk。
  - NBT 保存 `loaderCenterChunk` / `loaderWorkChunk`，实体重载后可继续释放/刷新对应 ticket。
- 新增 `ExplosionChunkLoading.registerValidationCallback()`：
  - 在 common setup 中注册 Forge forced chunk 校验回调。
  - 世界加载重建 ticket 时，若 UUID 对应实体不存在或不是 `ExplosionChunkLoadingEntity`，移除该实体 ticket。
- 接入现代实体：
  - `NukeExplosionMk5Entity`、`NukeExplosionMk3Entity` 改为继承 `ExplosionChunkLoadingEntity`，服务端 tick 时刷新爆心 chunk。
  - `FalloutRainEntity` 改为继承 `ExplosionChunkLoadingEntity`，首 tick 刷新爆心 chunk，处理每个 chunk 前调用 `loadChunk(...)`。

### 有意差异

- 现代 `loadChunk` 会释放上一工作 chunk；这是对旧实现的资源安全修正，不改变“当前正在处理的 chunk 需要被加载”的功能契约。
- 仍未迁旧 `IChunkLoader` 接口本身；现代 Forge 已经不需要 request ticket 回调接口，爆炸实体通过基类直接持有 ticket 语义。

## 2026-05-24 ExplosionNT DIGAMMA / volcanic lava 属性落点

### 1.7.10 细化追踪

- `ExplosionNT.ExAttrib.DIGAMMA`：
  - 旧版在被破坏的 normal cube 位置放置 `ModBlocks.ash_digamma`。
  - `1/5` 概率在上方为空气时放置 `ModBlocks.fire_digamma`。
- `ExplosionNT.ExAttrib.DIGAMMA_CIRCUIT`：
  - 若 `x % 3 == 0 && z % 3 == 0`，放置 `ModBlocks.pribris_digamma`。
  - 若 `x % 3 == 0 || z % 3 == 0` 且随机 boolean 为 true，也放置 `pribris_digamma`。
  - 其它位置按 `DIGAMMA` 放置 `ash_digamma`，并保留 `1/5` 上方 `fire_digamma`。
- `ExplosionNT.ExAttrib.LAVA_V` / `LAVA_R`：
  - 旧版分别在被破坏 normal cube 位置放置 `volcanic_lava_block` / `rad_lava_block`。
- `DigammaFlame`：
  - 无碰撞、非完整渲染、无掉落。
  - 只能存在于下方有实心顶面的方块上，否则邻居更新时消失。
  - living 实体碰撞时施加 `HazardType.DIGAMMA`、`ContaminationType.DIGAMMA`、`0.05F`。
- `VolcanicBlock` / `RadBlock`：
  - 旧版是 `BlockFluidClassic`，带流体扩散、相邻水/木/叶/矿石反应、随机固化为 basalt/sellafield 系列矿物。
  - `RadBlock` 额外对 living 实体施加 `HazardType.RADIATION`、`ContaminationType.CREATIVE`、`5F`。

### 现代实现

- 新增旧 ID 方块支撑：
  - `ash_digamma`：按旧爆炸残留灰烬方块注册，保留高爆抗。
  - `fire_digamma`：新增 `DigammaFlameBlock`，迁入无碰撞、支撑面存活、碰撞 DIGAMMA 污染、无掉落。
  - `pribris_digamma`：按旧黑化 RBMK 残骸注册，保留高硬度/高爆抗。
  - `volcanic_lava_block` / `rad_lava_block`：新增 `LegacyHotBlock` 作为爆炸残留旧 ID；触碰造成热地板伤害，`rad_lava_block` 额外施加 bypass radiation。
- 新增 VNT mutator：
  - `BlockMutatorDigamma` 按旧 `DIGAMMA` / `DIGAMMA_CIRCUIT` 方块替换规则运行。
  - `BlockMutatorPlaceBlock` 给 `LAVA_V` / `LAVA_R` 直接放置对应旧 ID 方块。
- 更新 `ExplosionNT.createBlockProcessor()`：
  - `DIGAMMA_CIRCUIT` 优先于 `DIGAMMA`。
  - `LAVA_V` / `LAVA_R` 现在不再是空枚举，会进入 block mutator。
- 资源/数据：
  - 从 1.7.10 资源复制 `ash_digamma`、`fire_digamma`、`rbmk_debris_digamma`、`volcanic_lava_still`、`rad_lava_still` 贴图。
  - 新增 blockstate/model/item model、loot、mineable tag、英文/中文名称。

### 有意延期

- `volcanic_lava_block` / `rad_lava_block` 当前只迁爆炸属性所需的放置旧 ID 和实体接触危险；完整 `BlockFluidClassic` 扩散、邻居反应、随机固化为 basalt/sellafield 矿物仍需等现代流体/矿物支撑迁入后补齐。
- `digamma_matter` 不属于本批 `ExplosionNT` 属性直接放置方块，暂未迁入。

## 2026-05-24 EntityShrapnel / EntityRubble

### 1.7.10 细化追踪

- `EntityShrapnel`：
  - 继承旧 `EntityThrowable`，`dataWatcher[16]` 存模式 byte。
  - 默认火焰免疫，`setTrail()` 模式 1 在客户端生成火焰轨迹。
  - 命中实体时用 `ModDamageSource.shrapnel` 造成 15 点伤害。
  - `ticksExisted > 5` 后命中会结束实体并播放 fizz。
  - 火山模式 2 / 放射火山模式 4：
    - 下落速度 `< -0.2` 时在命中方块上方放置 `volcanic_lava_block` / `rad_lava_block`，并在 3x3x3 区域空气中生成 `gas_monoxide`。
    - 上升速度 `> 0` 时创建强度 7 的 `ExplosionNT`，带 `NODROP`、`LAVA_V`/`LAVA_R`、`NOSOUND`、`ALLMOD`、`NOHURT`。
  - Watz 模式 3 在命中方块上方放置 `mud_block`。
  - 其它模式命中后生成 5 个 lava particle。
- `EntityRubble`：
  - 继承旧 `EntityThrowableNT`，`dataWatcher[16]`/`[17]` 存 block id/meta。
  - 命中实体时用 `ModDamageSource.rubble` 造成 15 点伤害。
  - `ticksExisted > 2` 后命中会结束实体，播放 `hbm:block.debris`，并发送 `ParticleBurstPacket` 在 50 格范围生成该方块的碎裂粒子。
  - 空气阻力为 `1F`，不额外衰减水平速度。
- `ExplosionLarge`：
  - `spawnRubble` 按旧速度公式生成石头 `EntityRubble`。
  - `spawnShrapnels` / `spawnTracers` / `spawnShrapnelShower` 生成 `EntityShrapnel`，其中约 `1/3` 启用 trail。
  - `jolt` 沿随机射线移除液体或低抗性方块，被击碎的方块以 `EntityRubble` 抛出并保留原方块状态。

### 现代实现

- 新增 `LegacyThrowableEntity`：
  - 迁入旧 `EntityThrowableNT` 的基础运动、方块 raytrace、实体碰撞扫描、重力、空气/水阻力、地面超时字段。
  - 作为爆炸库 projectile 的共享基类，避免普通武器调用点重复实现旧投射物物理。
- 新增 `ShrapnelEntity`：
  - 注册为 `hbm:entity_shrapnel`，尺寸 `0.25F`，火焰免疫。
  - 同步旧 mode byte，保留 `setTrail()`、`setVolcano()`、`setWatz()`、`setRadVolcano()` 入口。
  - 命中实体造成 `hbm:shrapnel` 15 点伤害。
  - 火山/放射火山模式接入 `volcanic_lava_block`、`rad_lava_block`、`gas_monoxide` 和 `ExplosionNT` 的 `LAVA_V` / `LAVA_R` 属性。
  - Watz 模式通过 `ModBlocks.legacyBlock("mud_block")` 查找旧 ID；clean port 尚未迁入 `mud_block` 时保持无放置，不用原版方块替代。
- 新增 `RubbleEntity`：
  - 注册为 `hbm:entity_rubble`，同步 block state id。
  - 命中实体造成 `hbm:rubble` 15 点伤害。
  - 落地后复用现有 `ParticleBurstPacket`，客户端按保存的 block state 生成碎裂粒子。
- 新增 damage type：
  - `data/hbm/damage_type/shrapnel.json`
  - `data/hbm/damage_type/rubble.json`
  - 静态 lang 与 datagen lang 均补充死亡文本。
- 更新 `ExplosionLarge`：
  - rubble/shrapnel/tracer/shower 入口不再只发送粒子，改为生成对应实体。
  - `jolt` 现在会抛出带原方块状态的 `RubbleEntity`，再清除方块。

### 有意延期

- 旧 `RenderShrapnel` / `RenderRubble` 的可见飞行模型尚未迁入；当前客户端注册 `NoopRenderer`，依赖 shrapnel trail 和 rubble 落地 burst 表现。后续 render library 继续迁移后补 OBJ/方块飞行渲染。
- 旧 `hbm:block.debris` 声音资源尚未在 clean port 确认注册；现代暂用 `SoundEvents.STONE_BREAK` 表示 rubble 命中碎裂。
- `mud_block` 仍等待流体/机器链路迁移；Watz shrapnel 已保留旧 ID 查找入口。

## 验证清单

- 普通爆炸不会在客户端修改世界。
- 方块处理、实体处理、掉落处理可独立组合。
- 高强度爆炸分批执行不阻塞主线程过久。
- 客户端效果 packet 解码后只生成表现，不重复破坏世界。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `ExplosionFleija`、`ExplosionNukeRayBatched`、`ExplosionNukeRayBalefire` 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `ExplosionChaos` 安全子集、`ExplosionTom`、`hbm:pc` / `hbm:cloud` damage type 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `BlockAllocatorGlyphidDig`、`ExplosionNukeRayParallelized` 兼容入口后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `NukeExplosionMk5Entity` 核心调度实体和实体注册后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `BombConfig` 子集并接通 `ExplosionNukeSmall` 大核弹 -> `NukeExplosionMk5Entity` 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `FalloutRainEntity` 安全子集并接通 MK5 完成后的落尘实体后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `NukeExplosionMk3Entity` 核心调度实体和实体注册后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在补齐 `ExplosionLarge` / `ExplosionNT` 旧调用签名兼容入口后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `EntityExplosionChunkloading` 现代 forced chunk 基类并接入 MK3/MK5/FalloutRain 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `ExplosionNT` 的 `DIGAMMA` / `DIGAMMA_CIRCUIT` / `LAVA_V` / `LAVA_R` 方块落点后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `EntityShrapnel` / `EntityRubble`、注册 damage type 并接回 `ExplosionLarge` 实体生成入口后通过。
