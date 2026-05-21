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
- `ExplosionNT.DIGAMMA` / `DIGAMMA_CIRCUIT` / `LAVA_V` / `LAVA_R` 仅保留枚举位，具体 digamma、pribris/ash/fire、volcanic/rad lava 需要等相关方块/辐射液体迁移后再补。
- `ExplosionNT` 不继承 vanilla `Explosion`，而是作为旧调用语义到 VNT 的兼容适配器；后续迁调用点时应优先直接使用 VNT/WeaponExplosionUtil，只有旧代码属性组合复杂时才用该适配器。

## 验证清单

- 普通爆炸不会在客户端修改世界。
- 方块处理、实体处理、掉落处理可独立组合。
- 高强度爆炸分批执行不阻塞主线程过久。
- 客户端效果 packet 解码后只生成表现，不重复破坏世界。
