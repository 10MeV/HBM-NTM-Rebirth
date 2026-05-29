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

- 本段为首批迁移时的历史限制；VNT 标准 affected-block 客户端烟尘已在 2026-05-25 通过 Aux 粒子桥补入。
- 本段为首批迁移时的历史限制；`EntityProcessorCross` / `EntityProcessorCrossSmooth` 已在 2026-05-21 后续批次补入，穿甲 DT/DR 仍延期。
- 本段为首批迁移时的历史限制；`BlockAllocatorWater`、`BlockAllocatorBulkie`、`BlockMutatorBalefire`、`BlockMutatorDebris` 已由后续批次补入。
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

- `BlockMutatorBalefire` 已在 2026-05-24 后续批次接回旧 `balefire` 方块；本段保留为 Cross/CrossSmooth 迁移批次时的历史限制。
- `ExplosionEffectStandard` 已在 2026-05-25 后续批次补回 VNT affected-block 客户端 smoke/explode 表现；本段的现代粒子桥限制只保留为历史记录。
- `EntityProcessorCrossSmooth.setupPiercing(...)` 只保存 DT/DR 参数，不实际应用。旧穿甲依赖 `EntityDamageUtil.attackEntityFromNT` 与 `DamageResistanceHandler.DamageClass`，需等伤害抗性核心深化。
- `EntityProcessorCross.shouldDealKnockback(...)` 暂不排除旧 `EntityBulletBaseMK4` / `EntityGrenadeUniversal`，因为这些实体尚未迁入 clean port；后续迁入后再以接口或类型标记排除。
- `ExplosionEffectWeapon` 已在 2026-05-25 改回 `ParticleUtil.spawnExplosionSmall(...)` 路径，复用现代 `explosionSmall` aux 粒子、近/远小爆炸延迟音和 debris 表现；旧 `ParticleExplosionSmall` 的逐像素完全一致性仍不追求。

## 2026-05-25 VNT 标准 affected-block 客户端效果桥

### 1.7.10 细化追踪

- `ExplosionEffectStandard.doEffect(...)`：
  - 服务端播放 `random.explode`。
  - 向 250 格范围客户端发送 `ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket`。
  - packet 包含爆心 `x/y/z`、`size` 与 `explosion.compat.affectedBlockPositions`。
- 旧客户端 `performClient(...)`：
  - `size >= 2` 时生成 `hugeexplosion`，否则生成 `largeexplode`。
  - 对每个 affected block：
    - 在方块内部随机取一点。
    - 计算从爆心到该点的归一化方向。
    - `mod = 0.5 / (distance / size + 0.1) * (rand^2 + 0.3)`。
    - 在爆心与方块随机点中点生成 `explode` 粒子，在方块随机点生成 `smoke` 粒子。

### 现代实现

- `ParticleUtil` 新增 `TYPE_VNT_EXPLOSION` 与 `spawnVntExplosion(...)`，通过现有 `AuxParticlePacket` 发送：
  - `size`
  - `blocks` long array，使用 `BlockPos.asLong()`。
- `ExplosionEffectStandard`：
  - 保留旧爆炸声音音量/音高。
  - `particles=true` 时发送 `vntExplosion` aux payload。
  - 对 affected block 坐标最多抽样 512 个，避免大型程序爆炸生成过大网络包；核爆/大型云已有专用实体和效果系统承担主表现。
- `HbmParticleEffects`：
  - 新增 `vntExplosion` 客户端处理。
  - 按旧公式生成中心爆炸粒子、每方块 `POOF` + `SMOKE` 方向粒子。

### 有意延期

- 未完全复刻旧压缩 packet 格式；现代复用已有 Aux 粒子网络管线，保留客户端表现契约。
- 超过 512 个 affected block 的 VNT 标准烟尘会抽样，而不是逐方块全量发送；这是现代网络安全边界，后续如需要可追加专用压缩包。

## 2026-05-25 VNT Tiny 效果与 smooth tiny 入口

### 1.7.10 细化追踪

- `ExplosionEffectTiny.doEffect(...)`：
  - 服务端播放 `hbm:weapon.explosionTiny`，音量 `15.0F`，音高 `1.0F`。
  - 发送 `type=vanillaExt` aux particle，`mode=largeexplode`，`size=1.5F`，`count=1`，范围 100。
- Sedna `Lego.tinyExplode(...)`：
  - 命中面外偏移 `0.25` 后创建 `ExplosionVNT`。
  - `EntityProcessorCrossSmooth(0.5, bullet.damage * damageMod)`。
  - `setupPiercing(config.armorThresholdNegation, config.armorPiercingPercent)`。
  - `setKnockback(0.25D)`，`PlayerProcessorStandard`，`ExplosionEffectTiny`。
- `ItemGrenadeFilling.tinyExplode(...)`：
  - 直接在手雷位置创建 `ExplosionVNT`。
  - `EntityProcessorCrossSmooth(0.5, damage)`，可带 DT/DR，击退 `0.25D`。
  - `PlayerProcessorStandard`，`ExplosionEffectTiny`。

### 现代实现

- `ExplosionEffectTiny`：
  - 改用已注册的 `ModSounds.WEAPON_EXPLOSION_TINY`。
  - 通过 `ParticleUtil.spawnAux(...)` 发送旧 `vanillaExt/largeexplode` payload，复用现有客户端 aux 粒子处理。
- `WeaponExplosionUtil` 新增 tiny smooth 入口：
  - `tinySmooth(...)`
  - `explodeTinySmooth(...)`
  - 默认参数对齐旧 Sedna/手雷 tiny 爆炸：`nodeDistance=0.5`、`knockback=0.25`、无方块破坏、`ExplosionEffectTiny`。
  - 完整重载保留 `pierceDamageThreshold` / `pierceDamageResistance` 参数，供后续 Sedna、手雷、landmine 等调用点无损接入。
- `WeaponExplosionUtil.smooth(...)` 增加 DT/DR 重载，给旧 `standardExplode(...)` / 手雷标准小武器爆炸路径预留穿甲参数。
- `ExplosionEffectWeapon`：
  - 旧版只调用 `ExplosionSmallCreator.composeEffect(...)`，由客户端播放小爆炸近/远声音并生成云团/debris。
  - 现代改为直接调用 `ParticleUtil.spawnExplosionSmall(...)`，不再额外播放原版 `GENERIC_EXPLODE`，避免与客户端旧式小爆炸音叠加。

### 有意延期

- `EntityProcessorCrossSmooth.setupPiercing(...)` 目前仍只保存 DT/DR 参数，实际穿甲计算等待伤害抗性核心迁移。
- 旧 Sedna 子弹、通用手雷、landmine 等具体调用点尚未迁入；本批只补爆炸库公共入口。

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
  - 优先使用 `ModBlocks.legacyBlock("balefire")`；旧 ID 不存在时回退为 vanilla `SOUL_FIRE`。
- 扩展 `ExplosionNukeGeneric`：
  - 新增 `solinium(Level, int, int, int)` 与 `solinium(Level, BlockPos)`。
  - 使用现代标签/方块类型近似旧材质清理：leaves/logs/planks、cactus/vine/melon/pumpkin/sponge、`BushBlock`、可替换方块。

### 有意降级/延期

- 本段为核心处理器迁移时的历史限制；`EntityBalefire` 调度实体已在 2026-05-24 后续批次补入。
- `ExplosionSolinium` 只迁核心处理器，不生成旧 `EntityNukeExplosionMK3`；后续 MK3 实体可复用本类的 NBT 字段。
- `ExplosionBalefire` 仍保留 `SOUL_FIRE` 兜底，但现代路径会优先通过 `ModBlocks.legacyBlock("balefire")` 使用旧 ID 方块。

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
  - `c` 已补回旧 taint -> mutation 联动：命中带 `hbm:taint` 的 living 时移除 taint，并施加 1 小时 `hbm:mutation`。
  - 新增 `hbm:taint` MobEffect 与 `hbm:taint` damage type，保留旧 taint 每 2 tick 判定、约 1/40 概率造成 `amplifier + 1` 伤害的最小效果契约。
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
- `hbm:taint` 本批只迁药水效果与 cloud 转 mutation 联动；旧 taint trail 铺方块、tainted mob 免疫、taint 方块 meta 行为等待 taint 方块/生物批次。
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
  - 保留 `strength/speed/length/fallout/falloutAdd` 与 `create(...)` / `createNoFallout(...)` / `statFac(...)` / `statFacNoRad(...)` / `moreFallout(...)` 入口。
  - 服务端首 tick 初始化 `ExplosionNukeRayBatched`，每 tick 按 `BombConfig.MK5_BUDGET_MS` 预算调用 cache/destruction。
  - 保留中心衰减伤害：每 tick 调 `ExplosionNukeGeneric.dealDamage(..., length * 2)`。
  - 迁入早期辐射：对 `length * 2` 范围内 living 按视线路径方块抗性衰减，并通过现代 `RadiationUtil` 施加 `RAD_BYPASS` 辐射。
  - 读取 NBT 时按 `BombConfig.LIMIT_EXPLOSION_LIFESPAN` 检查保存时间，过期的长时核爆会在下一 tick 自动结束，迁入旧配置的存档寿命约束。
  - `remove` 时取消 ray worker，避免实体被移除后继续持有破坏任务。

### 有意降级/延期

- 旧 `EntityExplosionChunkloading` 的强制 chunk loader 已在 2026-05-24 迁入现代 forced chunk 基类；本段为 MK5 初始迁入时的历史限制。
- 旧 `EntityFalloutRain` 已在后续批次迁入安全子集并接回 MK5 完成路径；本段为 MK5 初始迁入时的历史限制。
- 旧 `enableExtendedLogging` 尚未迁入现代 config。
- Manhattan 成就系统尚未迁，现代实体不触发成就。

## 2026-05-22 BombConfig 子集 / ExplosionNukeSmall 大核弹接线

### 1.7.10 细化追踪

- `BombConfig`：
  - nuke radius 默认值：`gadget=150`、`boy=120`、`man=175`、`mike=250`、`tsar=500`、`prototype=150`、`fleija=50`、`solinium=150`、`n2=200`、`missile=100`、`mirv=100`、`fatman=35`、`nuka=25`、`aSchrab=20`。
  - explosion 配置默认值：`mk5=50` ms、`blastSpeed=1024`、`falloutRange=100`、`fDelay=4`、`limitExplosionLifespan=0`、`chunkloading=true`、`explosionAlgorithm=2`。
- `ExplosionNukeSmall.PARAMS_HIGH`：
  - 旧版 `miniNuke=false`，`blastRadius=BombConfig.fatmanRadius`，`shrapnelCount=0`。
  - `explode(...)` 只有 `miniNuke && !safe` 时才执行 `ExplosionNT` 小核弹方块破坏；`PARAMS_HIGH` 这种 `miniNuke=false` 的大型核弹不能先跑一轮 `ExplosionNT`，否则会在 MK5 射线坑前额外挖出偏圆的标准爆炸坑。
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
  - `ExplosionNT` 预破坏条件改回旧版 `miniNuke && !safe`，大型核弹只进入 MK5 射线调度，避免叠加标准球形/VNT 爆炸导致弹坑过圆。
  - `miniNuke=false` 时除了已迁的烟云表现外，现在会通过 `NuclearExplosionUtil.spawnNuclear(...)` 进入 MK5 调度实体，恢复旧版大核弹路径。
  - 新增 `configuredHighParams()` / `explodeConfiguredHigh(...)`，供后续 Fatman、Tier0 missile 等调用点按运行期 `BombConfig.FATMAN_RADIUS` 生成参数，避免静态 `PARAMS_HIGH` 缓存旧半径。

### 有意降级/延期

- 现代 `PARAMS_HIGH` 仍是静态默认参数；后续核弹方块/物品接线应优先使用 `configuredHighParams()` 或 `explodeConfiguredHigh(...)`。
- `BombConfig.CHUNK_LOADING` 已接入现代 forced chunk 基类；`EXPLOSION_ALGORITHM` 已迁配置入口，但当前现代实现仍采用安全 batched worker，不启用旧 direct chunk storage/threaded 写入。
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
  - 保留 `createFleija(...)`、`createWaste(...)`、`createSolinium(...)`、`statFacFleija(...)`、`makeSol()` 入口，后续核弹方块和武器可直接 spawn。
  - 复用现代 `ExplosionNukeAdvanced`、`ExplosionFleija`、`ExplosionSolinium`、`ExplosionHurtUtil` 和 `FalloutRainEntity`。
  - NBT 保存/读取旧字段名和各处理器进度前缀：`exp_`、`wst_`、`vap_`、`expl_`、`sol_`。
  - `speed` 初始值从现代 `BombConfig.BLAST_SPEED` 读取，并按旧逻辑每 tick 自增。
  - 读取 NBT 时按 `BombConfig.LIMIT_EXPLOSION_LIFESPAN` 检查保存时间，过期的长时 Fleija/Solinium/waste 实体会在下一 tick 自动结束。

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
  - `forceCenterChunk()` 保持爆心 chunk 强制加载，并受 `BombConfig.CHUNK_LOADING` 控制；配置关闭时会释放已持有 ticket。
  - `loadChunk(chunkX, chunkZ)` 迁入旧“当前工作 chunk”入口；现代实现切换时释放上一工作 chunk，避免大范围落尘处理留下过多持久 ticket，并同样受 `BombConfig.CHUNK_LOADING` 控制。
  - `clearChunkLoader()` 在 `remove(...)` 时释放中心 chunk 与工作 chunk。
  - NBT 保存 `loaderCenterChunk` / `loaderWorkChunk` / `loaderSavedMillis`，实体重载后可继续释放/刷新对应 ticket，并供 `LIMIT_EXPLOSION_LIFESPAN` 判断存档后的过期爆炸。
- 新增 `ExplosionChunkLoading.registerValidationCallback()`：
  - 在 common setup 中注册 Forge forced chunk 校验回调。
  - 世界加载重建 ticket 时，若 UUID 对应实体不存在或不是 `ExplosionChunkLoadingEntity`，移除该实体 ticket。
- 接入现代实体：
  - `NukeExplosionMk5Entity`、`NukeExplosionMk3Entity` 改为继承 `ExplosionChunkLoadingEntity`，服务端 tick 时刷新爆心 chunk。
  - `FalloutRainEntity` 改为继承 `ExplosionChunkLoadingEntity`，首 tick 刷新爆心 chunk，处理每个 chunk 前调用 `loadChunk(...)`。

### 有意差异

- 现代 `loadChunk` 会释放上一工作 chunk；这是对旧实现的资源安全修正，不改变“当前正在处理的 chunk 需要被加载”的功能契约。
- 仍未迁旧 `IChunkLoader` 接口本身；现代 Forge 已经不需要 request ticket 回调接口，爆炸实体通过基类直接持有 ticket 语义。

## 2026-05-25 NuclearExplosionUtil 核爆调度门面

### 1.7.10 细化追踪

- 旧版普通弹药、导弹、核弹方块会直接散落调用：
  - `EntityNukeExplosionMK5.statFac(...)` / `statFacNoRad(...)`
  - `EntityNukeExplosionMK3.statFacFleija(...)` / `makeSol()`
  - `ExplosionNukeSmall.explode(..., PARAMS_HIGH/PARAMS_MEDIUM)`
  - `BombConfig.missileRadius` / `mirvRadius` / `fatmanRadius` / `nukaRadius` / `aSchrabRadius`
- 已确认旧调用点：
  - Tier4 nuclear / MIRV / doomsday / rusted doomsday missile。
  - Tier0 micro missile、schrabidium missile。
  - `EntityBulletBaseNT` 的 rainbow / nuke 配置分支。
  - Fatman/Nuka/anti-schrabidium 相关普通武器与掉落物。

### 现代实现

- 新增 `NuclearExplosionUtil`：
  - 集中提供 `spawnNuclear(...)`、`spawnNuclearNoFallout(...)`、`spawnNuclearWithFallout(...)`。
  - 集中提供 `spawnFleija(...)`、`spawnSolinium(...)`、`spawnAntiSchrabidium(...)`。
  - 集中提供核弹方块语义入口：`spawnGadget(...)`、`spawnBoy(...)`、`spawnMan(...)`、`spawnMike(...)`、`spawnTsar(...)`、`spawnPrototype(...)`、`spawnFleijaBomb(...)`、`spawnSoliniumBomb(...)`、`spawnN2(...)`。
  - 集中提供旧导弹语义入口：`spawnMissileNuclear(...)`、`spawnMissileMirv(...)`、`spawnMissileDoomsday(...)`、`spawnMissileDoomsdayRusted(...)`。
  - 集中提供普通武器/小核弹语义入口：`explodeFatman(...)`、`explodeNuka(...)`。
  - 半径读取通过 `BombConfig` 运行期值，config 尚未初始化时回退旧默认值。
- 改接现有调用库：
  - `CustomNukeExplosion` 的 MK3/MK5 分支走 `NuclearExplosionUtil`。
  - `CustomMissileExplosion` 的 `NUCLEAR` / `TX` / `N2` 分支走 `NuclearExplosionUtil`。
  - `ExplosionNukeSmall` 的大核弹路径走 `NuclearExplosionUtil.spawnNuclear(...)`。
- 新增 `NuclearDeviceBlock`：
  - 替换 `nuke_gadget`、`nuke_boy`、`nuke_man`、`nuke_tsar`、`nuke_mike`、`nuke_prototype`、`nuke_fleija`、`nuke_solinium`、`nuke_n2` 的纯 `HorizontalMachineBlock` 占位类型。
  - 保存 `Kind` 枚举，未来 BlockEntity/雷管/GUI 判定 `isReady()` 后可调用 `detonateArmed(...)`，统一移除方块、播放旧 `random.explode` 等价声音并进入对应 MK3/MK5 调度。
  - 当前不在 `neighborChanged` / `use` 中无条件引爆，避免绕过旧版 TileEntity 库存组件和 `isReady()` 保险。

## 2026-05-25 核弹方块库存与红石 ready 判定

### 1.7.10 细化追踪

- 旧核弹方块均通过对应 `TileEntityNuke*` 保存 `ItemStack[] slots`，红石触发前必须通过 `isReady()` / `isFilled()`：
  - Gadget：6 格，0 为 `gadget_wireing`，1-4 为 `early_explosive_lenses`，5 为 `gadget_core`。
  - Boy：5 格，依次为 `boy_shielding`、`boy_target`、`boy_bullet`、`boy_propellant`、`boy_igniter`。
  - Man：6 格，0 为 `man_igniter`，1-4 为 `early_explosive_lenses`，5 为 `man_core`。
  - Tsar：6 格，0-3 为 `explosive_lenses`，4 为 `man_core` 时可按 Man 半径起爆；额外 5 为 `tsar_core` 时按 Tsar 半径起爆。
  - Mike：8 格，0-3 为 `explosive_lenses`，4 为 `man_core` 时可起爆；额外 5 `mike_core`、6 `mike_deut`、7 `mike_cooling_unit` 时视为 filled。
  - Prototype：14 格，0/1/12/13 为 `cell_sas3`，2/3/10/11 为 `rod_quad` uranium，4/5/8/9 为 `rod_quad` lead，6/7 为 `rod_quad` NP237。
  - Fleija：11 格，0/1 为 `fleija_igniter`，2/3/4 为 `fleija_propellant`，5-10 为 `fleija_core`。
  - Solinium：9 格，0/3/5/8 为 `solinium_igniter`，1/2/6/7 为 `solinium_propellant`，4 为 `solinium_core`。
  - N2：12 格均为 `n2_charge`。
- `NukeTsar.igniteTestBomb(..., r)` 使用传入半径；未 filled 时实际为 Man，filled 时为 Tsar。
- `NukeMike.igniteTestBomb(..., r)` 旧源码实际忽略 `r` 并总是使用 `BombConfig.mikeRadius`；现代端保留这个实际行为，避免与旧存档/玩法差异。
- 旧方块破坏时掉落库存；红石起爆前会先 `clearSlots()` 再移除方块，避免装药掉落。

### 现代实现

- 新增 `NuclearDeviceBlockEntity`：
  - 单一 BE 类型覆盖 9 个核弹方块，按 `NuclearDeviceBlock.Kind` 建立旧版槽位数量。
  - 使用 `ItemStackHandler` 保存 `Inventory` NBT，并通过 Forge item capability 暴露库存。
  - `isItemValid(...)`、`isReady()`、`isFilled()` 按旧版槽位和旧 item ID 严格判定；缺失的旧物品不会被近似替代。
  - `detonationKind()` 保留 Tsar 半填充 -> Man、Ivy Mike 半填充 -> Mike 的旧实际语义。
- `NuclearDeviceBlock` 改为 `EntityBlock`：
  - `neighborChanged` / `onPlace` 遇红石信号时调用 `detonateArmed(...)`。
  - `detonateArmed(...)` 必须找到 BE 且 `isReady()` 成功才会清空槽位、移除方块并进入 `NuclearExplosionUtil`。
  - 普通破坏通过 `onRemove(...)` 掉落 BE 库存；起爆路径先清空槽位，因此不会掉落装药。
- 注册与资源：
  - `ModBlockEntities.NUCLEAR_DEVICE` 绑定全部核弹方块。
  - 补入核弹装配组件旧 ID：`early_explosive_lenses`、`explosive_lenses`、`gadget_wireing`、`boy_igniter`、`boy_shielding`、`man_igniter`、`mike_deut`、`mike_cooling_unit`、`fleija_igniter`、`solinium_igniter`、`n2_charge`。
  - 除两种旧爆炸镜片外，核弹装配组件按旧版 `maxStackSize(1)` 注册为单堆叠物品。
  - 从 1.7.10 资源复制对应贴图；`early_explosive_lenses` / `explosive_lenses` 分别映射旧贴图 `gadget_explosive8` / `man_explosive8`。

### 有意延期

- 核弹菜单、GUI、玩家右键打开界面、slot 坐标和同步包尚未迁入；当前库存主要供后续 GUI/自动化/测试接入。
- `rod_quad` 及其旧 `BreedingRodType` metadata 物品体系尚未迁入，Prototype ready 判定已预留 `BreedingRodType` NBT 钩子，但在燃料棒迁入前不会完整可用。
- `ItemFleija`、`ItemSolinium`、`ItemN2` 的特殊 tooltip/效果类本批未迁，只先恢复注册 ID、贴图和装药判定。
- 验证：`.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 核弹装填菜单与旧 GUI 贴图

### 1.7.10 细化追踪

- 旧版 `ContainerNuke*` 只允许 shift-click 从核弹槽合并回玩家背包；玩家背包物品不会自动合并进核弹槽，必须手动放入指定槽位。
- 槽位布局来自：
  - `ContainerNukeGadget` / `ContainerNukeMan`：6 格，窗口 `176x166`，玩家包从 `8,84` 开始。
  - `ContainerNukeBoy`：5 格，窗口 `176x222`，玩家包从 `8,140` 开始。
  - `ContainerNukeTsar`：6 格，窗口 `256x233`，玩家包从 `48,151` 开始。
  - `ContainerNukeMike`：8 格，窗口 `176x217`，玩家包从 `8,135` 开始。
  - `ContainerNukePrototype`：14 格，窗口 `176x166`，玩家包从 `8,84` 开始。
  - `ContainerNukeFleija`：11 格，窗口 `176x222`，玩家包从 `8,140` 开始。
  - `ContainerNukeSolinium`：9 格，窗口 `176x222`，玩家包从 `8,140` 开始。
  - `ContainerNukeN2`：12 格，窗口 `176x222`，玩家包从 `8,140` 开始。
- 旧 GUI 贴图路径：
  - `textures/gui/weapon/gadgetSchematic.png`
  - `fatManSchematic.png`
  - `lilBoySchematic.png`
  - `tsarBombaSchematic.png`
  - `ivyMikeSchematic.png`
  - `gui_prototype.png`
  - `fleijaSchematic.png`
  - `soliniumSchematic.png`
  - `n2Schematic.png`

### 现代实现

- 新增 `NuclearDeviceMenu`：
  - 放在现代 `menu` 层并通过 `ModMenuTypes.NUCLEAR_DEVICE` 注册。
  - 使用 `Layout.forKind(...)` 按核弹类型选择旧版 slot 坐标、窗口尺寸、玩家背包偏移和贴图名。
  - `quickMoveStack(...)` 保留旧版保守语义：核弹槽 -> 玩家背包；玩家背包 -> 核弹槽不自动合并。
- `NuclearDeviceBlockEntity` 实现 `MenuProvider`：
  - `getDisplayName()` 返回对应 `block.hbm.nuke_*`。
  - `createMenu(...)` 返回 `NuclearDeviceMenu`。
- `NuclearDeviceBlock.use(...)`：
  - 非潜行右键在服务端通过 `NetworkHooks.openScreen(...)` 打开装填界面。
  - 潜行右键保持 pass，给后续工具/雷管交互留空间。
- 新增 `NuclearDeviceScreen`：
  - 放在现代 `client.screen` 层并在 `ClientModEvents` 注册。
  - 复用旧 GUI 贴图，按 menu layout 设置窗口尺寸。
  - 迁入 Gadget/Man、Boy、Tsar 的主要旧 ready/组件叠层；Mike/Fleija/Solinium/N2 先迁 ready/filled 关键叠层。
- 从 1.7.10 复制全部上述核弹 GUI 贴图到 `assets/hbm/textures/gui/weapon/`。

### 有意延期

- 旧 `GuiInfoContainer` 左侧 info panel、`desc.gui.nuke*.desc` 多行说明未迁入；需要等通用 GUI info panel/tooltip 样式库稳定后再统一接入。
- Mike/Fleija/Solinium/N2 的部分逐组件视觉贴片尚未完全复刻，本批优先保证装填交互、slot 布局和 ready/filled 状态反馈。
- Prototype 的装填界面已可打开并保留 14 格布局，但 `rod_quad` 物品体系未迁入前仍无法完整 ready。
- 验证：`.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 有意延期

- `EntityNukeTorex` 是完整客户端爆云实体，本批没有用一次性粒子替代，避免破坏旧视觉契约；后续应迁实体/粒子生命周期本身。
- 核弹方块菜单/GUI、库存配方、右键 ready 判定尚未迁入；本批已补方块实体、库存持久化和红石 ready 触发。
- Tier0/Tier4 missile 实体、Sedna 枪弹、Fatman/Nuka/anti-schrabidium 物品尚未迁入；本批先铺好可直接复用的核爆库入口。

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

- 旧 `hbm:block.debris` 声音资源尚未在 clean port 确认注册；现代暂用 `SoundEvents.STONE_BREAK` 表示 rubble 命中碎裂。
- `mud_block` 仍等待流体/机器链路迁移；Watz shrapnel 已保留旧 ID 查找入口。

## 2026-05-24 Projectile renderer / Balefire block

### 1.7.10 细化追踪

- `RenderShrapnel`：
  - 使用 `ModelShrapnel` 的 4x4x4 box。
  - 绑定 `textures/entity/shrapnel.png`。
  - 每 tick 按 `(ticksExisted % 360) * 10 + partialTick` 绕 `(1,1,1)` 旋转。
  - 当 `EntityShrapnel.dataWatcher[16] >= 2` 时缩放到 3 倍，用于火山/放射火山熔岩弹块。
- `RenderRubble`：
  - 使用旧 `ModelRubble` 的多 box 碎块模型。
  - 从 `dataWatcher[16]`/`[17]` 获取 block id/meta，绑定该方块侧面纹理。
  - 每 tick 按 `(ticksExisted + partialTick) * 10` 绕 `(1,1,1)` 旋转。
- `Balefire`：
  - 旧 `BlockFire` 子类，注册 ID `balefire`，贴图 `textures/blocks/balefire.png`，亮度 1。
  - fire tick 开启时按旧 fire spread 逻辑扩散，meta/AGE 上限 15。
  - 可在有可燃邻居或下方实心顶面的情况下存活。
  - 实体碰撞时 `setFire(10)`，living 额外获得强辐射效果。
  - `BlockMutatorBalefire` 在爆炸后若位置为空、下方实体顶面且 `1/3` 命中则放置 `ModBlocks.balefire`。

### 现代实现

- 新增 `ShrapnelRenderer`：
  - 使用现代 `ModelPart` 构造 4x4x4 box，绑定旧 `textures/entity/shrapnel.png`。
  - 按旧 tick 旋转速度旋转；火山/放射火山模式按 3 倍缩放。
  - `ClientModEvents` 改为给 `entity_shrapnel` 注册该 renderer。
- 新增 `RubbleRenderer`：
  - 使用现代 `BlockRenderDispatcher.renderSingleBlock` 渲染 `RubbleEntity` 同步的 `BlockState`，保留旧“瓦砾显示原方块纹理”的视觉契约。
  - 按旧 tick 旋转速度旋转；`ClientModEvents` 改为给 `entity_rubble` 注册该 renderer。
- 新增 `BalefireBlock` 与旧 ID 资源：
  - 注册 `hbm:balefire`，加入 legacy block map，保留无碰撞、全亮、随机 tick、无掉落。
  - 复制旧 `balefire.png` / `.mcmeta`，新增 cross blockstate/model、静态 lang 与 datagen lang。
  - 现代实现按 30-39 tick 继续调度，保留旧版 `tickRate + rand(10)` 节奏。
  - 碰撞时点燃实体，并对 living 设置至少 100 tick 的 `RadiationData.balefire`，同时施加 CREATIVE radiation contamination。
- 更新 `BlockMutatorBalefire`：
  - 现在优先 `ModBlocks.legacyBlock("balefire")`，只有旧 ID 未注册时才回退 `SOUL_FIRE`。
  - `ExplosionNT.ExAttrib.BALEFIRE`、`ExplosionNukeRayBalefire`、`ExplosionBalefire` 现可通过旧 ID 自动落到真实 `balefire` 方块。

### 有意延期

- `BalefireBlock` 当前只迁爆炸落点和基础 `BlockFire` 扩散/实体接触契约；旧版更细的渲染颜色随 meta 变暗尚未迁入。
- balefire 流体/物品/配方仍属于武器/流体/物品后续批次。

## 2026-05-24 EntityBalefire 调度实体

### 1.7.10 细化追踪

- `com.hbm.entity.logic.EntityBalefire` 继承 `EntityExplosionChunkloading`，是 `ExplosionBalefire` 的分 tick 调度壳。
- NBT 字段：`age`、`destructionRange`、`speed`、`did`，并用 `exp_*` 前缀保存 `ExplosionBalefire` 的内部扫描状态。
- 首次 tick 时用实体当前位置整数坐标创建 `ExplosionBalefire((int)posX, (int)posY, (int)posZ, world, destructionRange)`。
- 每 tick 服务器端强制加载当前位置所在 chunk，`speed += 1` 后执行 `speed` 次 `exp.update()`。
- 处理器完成后清除 chunk loader 并结束实体；未完成时调用 `ExplosionNukeGeneric.dealDamage(world, posX, posY, posZ, destructionRange * 2)`。
- 旧调用点包括 `TileEntityNukeBalefire.explode()`、`NukeCustom` antimatter 路径、`EntityMissileCustom` 的 `BALEFIRE` 弹头。

### 现代实现

- 新增 `BalefireExplosionEntity`：
  - 注册为 `hbm:entity_balefire`，尺寸 `0.1F`，`updateInterval=1`，`clientTrackingRange=256`，`fireImmune`，`noSummon`。
  - 继承现代 `ExplosionChunkLoadingEntity`，复用 forced chunk 保存/释放逻辑。
  - 保留旧 `age/destructionRange/speed/did/exp_*` NBT 合同，重载后继续从 `ExplosionBalefire` 保存的螺旋扫描位置推进。
  - 每 tick 先 force center chunk，再按旧逻辑加载工作 chunk，处理完成时通过 `discard()` 触发 chunk loader 清理。
  - 未完成时继续调用现代 `ExplosionNukeGeneric.dealDamage`，伤害范围保持 `destructionRange * 2`。
- `ClientModEvents` 给 `hbm:entity_balefire` 注册 `NoopRenderer`，保持旧实体“只负责逻辑，不直接显示模型”的表现。

### 有意延期

- `TileEntityNukeBalefire`、`NukeCustom`、`EntityMissileCustom` 等具体武器/方块调用点尚未迁入；本批先补足爆炸库可复用的实体调度层。
- 旧 balefire 爆炸的 Torex/大型特效调用仍等待对应客户端特效系统继续迁移。

## 2026-05-24 Balefire Bomb 触发骨架 / 公共 spawn 入口

### 1.7.10 细化追踪

- `ModBlocks.nuke_fstbmb` 注册为 `NukeBalefire`，硬度 5、爆抗 200、非普通方块渲染，旧创造栏为 nukeTab。
- `NukeBalefire`：
  - 继承 `BlockMachineBase`，`rotatable=true`。
  - 邻居更新时若方块被红石供能，调用 `explode(world, x, y, z)`。
  - `explode` 只在服务端执行，要求 `TileEntityNukeBalefire.isLoaded()` 为真，否则返回缺组件错误。
- `TileEntityNukeBalefire`：
  - 2 槽库存：槽 0 要求 `egg_balefire`，槽 1 要求 `battery_spark` 或 `battery_trixite`。
  - 默认 `timer=18000` tick；启动后每秒播放 `hbm:weapon.fstbmbPing`，倒计时归零后爆炸。
  - GUI 按钮 `meta=0` 且 loaded 时播放 `hbm:weapon.fstbmbStart` 并开始计时；`meta=1` 设置秒数。
  - 爆炸时清库存、移除方块，生成 `EntityBalefire`，位置为方块中心，`destructionRange=250`，并调用 `EntityNukeTorex.statFacBale(..., 250)`。
- 声音资源：
  - `assets/hbm/sounds/weapon/fstbmbStart.ogg`
  - `assets/hbm/sounds/weapon/fstbmbPing.ogg`

### 现代实现

- `WeaponExplosionUtil` 新增 `spawnBalefire(Level, double, double, double, int)`：
  - 服务端且 range > 0 时生成 `BalefireExplosionEntity.create(...)`。
  - 作为 `TileEntityNukeBalefire`、自定义核弹、导弹/弹药等后续旧调用点的统一入口。
- 新增 `BalefireBombBlock` 并将 `hbm:nuke_fstbmb` 从模型占位改为该方块：
  - 保留水平朝向、非遮挡、金属材质、硬度 5、爆抗 200。
  - 红石邻居信号、放置时已被供能、被火点燃时，会播放 `weapon.fstbmb_start`、移除方块，并通过 `WeaponExplosionUtil.spawnBalefire(..., 250)` 生成 balefire 爆炸实体。
  - 被其它爆炸摧毁时通过 `wasExploded` 生成同等 balefire 爆炸实体，用于恢复爆炸连锁语义。
- 注册并复制旧声音：
  - `hbm:weapon.fstbmb_start` -> `sounds/weapon/fstbmb_start.ogg`
  - `hbm:weapon.fstbmb_ping` -> `sounds/weapon/fstbmb_ping.ogg`
  - 静态 lang 与 datagen lang 补充 `subtitles.hbm.weapon.fstbmb`。

### 有意延期

- 完整 `TileEntityNukeBalefire`、2 槽库存、GUI、计时器、loaded 检查和 `egg_balefire` / `battery_spark` / `battery_trixite` 依赖尚未迁入；当前 `nuke_fstbmb` 是爆炸库侧可运行触发骨架。
- Torex balefire 大型客户端效果仍等待对应实体/粒子特效系统迁移。
- 旧 `BombReturnCode` / `IBomb` 接口尚未迁入；现代方块通过直接触发公共 spawn 入口承接爆炸库行为。

## 2026-05-24 Custom Nuke 爆炸编排 / FallingNuke

### 1.7.10 细化追踪

- `NukeCustom.explodeCustom(...)` 是自定义核弹的纯爆炸编排函数，参数为 `tnt/nuke/hydro/amat/dirty/schrab/euph` 七个材料强度。
- 旧上限：
  - `maxTnt=150`
  - `maxNuke=200`
  - `maxHydro=350`
  - `maxAmat=350`
  - `maxSchrab=250`
- `dirty` 先限制到 100。
- 爆炸优先级：
  - `euph > 0`：生成 `EntityNukeExplosionMK3`，`destructionRange=150`，`waste=false`，并生成彩虹 Fleija 云。
  - `schrab > 0`：`schrab += amat/2 + hydro/4 + nuke/8 + tnt/16`，上限 250，生成 Fleija MK3 与普通 Fleija 云。
  - `amat > 0`：`amat += hydro/2 + nuke/4 + tnt/8`，上限 350，生成 `EntityBalefire`，并生成 balefire Torex。
  - `hydro > 0`：`hydro += nuke/2 + tnt/4`，上限 350，`dirty *= 0.25`，生成 MK5 并追加 fallout。
  - `nuke > 0`：`nuke += tnt/2`，上限 200，生成 MK5 并追加 fallout。
  - `tnt >= 75`：上限 150，生成无辐射 MK5。
  - `0 < tnt < 75`：调用 `ExplosionLarge.explode(..., cloud=true, rubble=true, shrapnel=true)`。
- `TileEntityNukeCustom` 中的 GUI 预览公式：
  - `getNukeAdj = min(nuke + tnt / 2, maxNuke)`
  - `getHydroAdj = min(hydro + nuke / 2 + tnt / 4, maxHydro)`
  - `getAmatAdj = min(amat + hydro / 2 + nuke / 4 + tnt / 8, maxAmat)`
  - `getSchrabAdj = min(schrab + amat / 2 + hydro / 4 + nuke / 8 + tnt / 16, maxSchrab)`
- `EntityFallingNuke`：
  - 保存同样七个 float NBT：`tnt/nuke/hydro/amat/dirty/schrab/euph`。
  - 每 tick 手动移动，`motionX/Z *= 0.99`，`motionY -= 0.05`，最低 `-1`。
  - pitch 初始 90，每 tick 若 `rotationPitch > -75` 则减 2。
  - 进入非空气方块后，服务端调用 `NukeCustom.explodeCustom(...)` 并结束实体。

### 现代实现

- 新增 `CustomNukeExplosion`：
  - 暴露 `MAX_TNT/MAX_NUKE/MAX_HYDRO/MAX_AMAT/MAX_SCHRAB` 常量。
  - 暴露 `adjustedNuclear/adjustedHydrogen/adjustedAntimatter/adjustedSchrab`，供后续 GUI/tooltip 与爆炸预览复用旧公式。
  - `explode(...)` 按旧优先级接入现代 `NukeExplosionMk3Entity`、`NukeExplosionMk5Entity`、`WeaponExplosionUtil.spawnBalefire` 与 `ExplosionLarge`。
  - 保留旧坐标偏移差异：部分分支额外 `+0.5`，nuclear 分支保留旧 Y+5 的 MK5 生成位置。
- 新增 `FallingNukeEntity`：
  - 注册为 `hbm:entity_falling_nuke`，尺寸 `0.98F`，`clientTrackingRange=256`，`updateInterval=1`。
  - 保存旧七个材料 float NBT，并额外保存旧 metadata 朝向 byte，供后续 renderer 或方块接线使用。
  - 迁入旧手动下落阻力、重力上限和 pitch 旋转；落入非空气方块后调用 `CustomNukeExplosion.explode(...)`。
  - 客户端暂注册 `NoopRenderer`，避免在模型/渲染批次前引入占位模型。

### 有意延期

- `nuke_custom` 方块、`TileEntityNukeCustom` 库存/菜单/GUI、`custom_fall` 组件与物品配方尚未迁入；本批先迁爆炸库公共编排和坠落实体。
- Fleija / balefire / standard Torex 大型云实体尚未完整迁入；现代实现先恢复地形/伤害/后处理实体路径。
- `EntityFallingNuke` 可见模型与旋转渲染等待 `nuke_custom` 资源/renderer 批次补齐。

## 2026-05-24 Custom Missile 弹头爆炸工具

### 1.7.10 细化追踪

- `EntityMissileCustom.onMissileImpact(...)` 通过 warhead item 的 `ItemCustomMissilePart.WarheadType` 与 `strength` 决定爆炸行为。
- 旧 `WarheadType` 枚举：
  - `HE`
  - `INC`
  - `BUSTER`
  - `CLUSTER`
  - `NUCLEAR`
  - `TX`
  - `N2`
  - `BALEFIRE`
  - `SCHRAB`
  - `TAINT`
  - `CLOUD`
  - `TURBINE`
  - `CUSTOM0` - `CUSTOM9`
- `impactCustom` 若存在则先执行自定义 impact 并返回；这是外部兼容覆盖点。
- 已追踪的旧默认 impact：
  - `HE`：`ExplosionLarge.explode(..., cloud=true, rubble=false, shrapnel=true)`，随后 `ExplosionLarge.jolt(strength, strength*50, 0.25)`。
  - `INC`：`ExplosionLarge.explodeFire(...)`，随后 `jolt(strength*1.5, strength*50, 0.25)`。
  - `BUSTER`：`ExplosionLarge.buster(..., motionVec, strength, strength*4)`。
  - `NUCLEAR` / `TX`：生成 `EntityNukeExplosionMK5.statFac(..., strength)` 并生成 standard Torex。
  - `BALEFIRE`：生成 `EntityBalefire`，`destructionRange=strength`，并生成 balefire Torex。
  - `N2`：生成 `EntityNukeExplosionMK5.statFacNoRad(..., strength)` 并生成 standard Torex。
  - `TAINT`：在 `strength` 范围内随机尝试 `strength*10` 次，将普通实心非空气方块替换为 `ModBlocks.taint` 的 meta 4-6。
  - `CLOUD`：播放 potion aux SFX 2002，并在上一 tick 位置生成 `ExplosionChaos.spawnPoisonCloud(..., 750, 2.5, 2)`。
  - `TURBINE`：先做强度 10 的 `ExplosionLarge.explode`，再生成 `strength` 个 turbine bullet blade。
- `CLUSTER` 在追踪片段中为空分支。

### 现代实现

- 新增 `CustomMissileExplosion`：
  - 独立定义现代 `WarheadType` 枚举，保持旧名称，供未来 `EntityMissileCustom` / missile part item 迁入时无损映射。
  - `explode(Level, x, y, z, Vec3 motion, float strength, WarheadType type, Entity source)` 返回 boolean，表示默认 impact 是否已被现代爆炸库处理。
  - 已接通：
    - `HE` -> `ExplosionLarge.explode` + `jolt`
    - `INC` -> `ExplosionLarge.explodeFire` + `jolt`
    - `BUSTER` -> `ExplosionLarge.buster`
    - `NUCLEAR` / `TX` -> `NuclearExplosionUtil.spawnNuclear`
    - `BALEFIRE` -> `WeaponExplosionUtil.spawnBalefire`
    - `N2` -> `NuclearExplosionUtil.spawnNuclearNoFallout`
    - `CLOUD` -> `levelEvent(2002)` + `ExplosionChaos.spawnPoisonCloud`
  - 保留 `double motionX/Y/Z` 重载，方便旧实体迁移时少做临时 `Vec3` 包装。

### 有意延期

- `EntityMissileCustom`、`MissileStruct`、发射台/紧凑发射台、missile part item/metadata、propulsion 和 radar detectability 尚未迁入；本批只迁默认弹头 impact 工具。
- `CLUSTER` 旧分支为空，现代保持未处理。
- `SCHRAB` 未在当前读取片段中出现默认实现，等待更完整导弹/弹药批次追踪。
- `TAINT` 依赖 `ModBlocks.taint` 及旧 meta 语义，clean port 未注册该方块时不做原版替代。
- `TURBINE` 依赖旧 `EntityBulletBaseNT` 与 `BulletConfigSyncingUtil.TURBINE`，等待 Sedna/弹药实体迁移。
- Torex 大型爆炸视觉仍等待特效实体迁移；现代工具只恢复地形/伤害/后效应调度。

## 2026-05-25 核弹 GUI 打开入口复核

触发来源：

- 实机反馈九个基础核弹方块 GUI 仍打不开；本轮只处理 GUI/渲染接入，不新增真实核爆算法。

复核结论：

- `NuclearDeviceBlock` / `NuclearDeviceBlockEntity` / `NuclearDeviceMenu` / `NuclearDeviceScreen` 已存在，`ModMenuTypes.NUCLEAR_DEVICE` 与客户端 `MenuScreens.register(...)` 也已注册。
- 当前问题不属于爆炸算法库，而是新迁核弹方块与现代菜单/渲染库的接入稳定性问题。

现代修正：

- `NuclearDeviceBlock#use(...)` 改为通过 `getMenuProvider(state, level, pos)` 获取菜单 provider，再调用 `NetworkHooks.openScreen(...)`，并与其他机器方块一致返回 `InteractionResult.sidedSuccess(...)`。
- `NuclearDeviceBlock` 显式覆盖 `getMenuProvider(...)`，从当前位置的 `NuclearDeviceBlockEntity` 返回菜单 provider，给现代方块菜单路径一个稳定入口。
- 核弹方块渲染改由渲染库 `NuclearDeviceRenderer` 承担，因此 `NuclearDeviceBlock#getRenderShape(...)` 返回 `INVISIBLE`，避免 baked OBJ 与 BER 双重渲染。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：核弹右键失败容错

- 实机反馈放置后仍打不开 UI。现代端在 `NuclearDeviceBlock#use(...)` 里增加容错：如果当前位置状态确认为当前核弹方块，但缺失 `NuclearDeviceBlockEntity`，服务端会按当前 `BlockState` 立即创建并绑定一个新的 `NuclearDeviceBlockEntity`，再打开菜单。
- 该容错用于修复迁移过程中的旧占位方块、已有存档或方块实体注册切换造成的“方块在但 BE 不在”状态；正常新放置核弹仍应由 `EntityBlock#newBlockEntity` 创建 BE。
- 右键成功打开时返回 `InteractionResult.CONSUME`，没有菜单 provider 时不再伪成功，便于后续实机发现真实交互失败。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：核弹客户端右键返回值修正

- 继续实机反馈：核弹模型可见但 UI 仍打不开，且手持核弹右键容易继续放置新方块。
- 1.7.10 对照：`NukeGadget` / `NukeMan` / `NukeBoy` 等 `onBlockActivated(...)` 在 `world.isRemote` 时直接返回 `true`；服务端非潜行时取 tile entity 并 `FMLNetworkHandler.openGui(...)`，最后也返回 `true`。
- 现代根因：此前 `NuclearDeviceBlock#use(...)` 会先在客户端尝试取菜单 provider；客户端若没有同步到 BE 或 provider 为 `null`，会返回 `PASS`，导致右键交互被手中物品继续处理。
- 现代修正：
  - 潜行仍返回 `PASS`，保留旧版潜行不打开 GUI 的语义。
  - 客户端非潜行固定返回 `InteractionResult.SUCCESS`，对齐旧版 `world.isRemote -> true`，阻止手持物品抢走交互。
  - 服务端非潜行继续通过 `getOrCreateMenuProvider(...)` 容错创建/获取 `NuclearDeviceBlockEntity` 并 `NetworkHooks.openScreen(...)`。
  - 若服务端仍无法取得 provider，也返回 `CONSUME`，避免核弹右键失败时继续触发手持方块放置。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：核弹客户端菜单构造兜底

- 继续实机反馈 UI 仍打不开后，复查注册链：`NUCLEAR_DEVICE` block entity type 已包含九个核弹方块，`ModMenuTypes.NUCLEAR_DEVICE` 已注册，客户端 `MenuScreens.register(...)` 也存在。
- 现代可疑点收敛到客户端 `NuclearDeviceMenu(int, Inventory, FriendlyByteBuf)`：`NetworkHooks.openScreen(...)` 只把 `BlockPos` 发给客户端，客户端在菜单构造瞬间如果还未拿到该位置的 `NuclearDeviceBlockEntity`，此前会直接抛出 `IllegalStateException("Expected nuclear device block entity...")`，表现为服务端尝试打开但客户端界面打不开。
- 现代修正：
  - `NuclearDeviceMenu#getBlockEntity(...)` 在客户端找不到 BE 时，若当前位置仍是 `NuclearDeviceBlock`，用该 `BlockState` 创建一个轻量 `NuclearDeviceBlockEntity` 作为菜单布局/slot 数量载体。
  - 该兜底只服务客户端菜单构造，不写回世界、不保存数据；服务端菜单仍使用真实 BE。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：核弹主体右键代理

- 继续实机反馈 GUI 仍打不开后，复查 1.7.10 GUI 打开路径：
  - 核弹方块 `onBlockActivated(...)` 非客户端/非潜行时调用 `FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z)`。
  - `GUIHandler#getServerGuiElement(...)` / `getClientGuiElement(...)` 均先查 `world.getTileEntity(x,y,z)`，若 tile entity 实现 `IGUIProvider` 则分别返回 container/gui。
  - 即旧版 GUI 开启完全依赖“被右键的位置就是核弹 TileEntity 所在核心块”。
- 现代问题：
  - 核弹 OBJ 在现代端是大模型，玩家常点到可见主体所在的相邻方块空间；普通 `VoxelShape` 超出一格不足以可靠让相邻格把右键发送到核心块。
  - 这会造成没有异常日志、菜单注册完整，但实际右键没有进入 `NuclearDeviceBlock#use(...)`。
- 现代修正：
  - `NuclearDeviceBlock` 接入现有 `MultiblockCoreBlock` / `DummyBlock` 代理体系。
  - 按中心化后的核弹模型 bounds 生成 dummy offset，放置和首次右键时填充 `dummy_block`，dummy 会把右键转发回核心核弹块。
  - 移除核心核弹时同步移除这些 dummy。
- 当前限制：
  - 本轮先恢复交互可靠性；dummy 代理范围来自模型 AABB，后续若需要更细致避免覆盖已有方块，应补放置前空间检查与提示。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 最终追加：核弹 GUI 打开链路修正

- 后续实机日志已确认服务端持续输出 `Opening nuclear device menu...`，说明右键已进入核心核弹方块，且 `NetworkHooks.openScreen(...)` 已被调用；问题不再是模型、选择框或右键代理。
- Forge 1.20.1 `openScreen(player, provider, pos)` 只会稳定发送 `BlockPos`；核弹 GUI 的 layout/slot 数量还依赖核弹 kind。现代修正改为服务端显式写入 `BlockPos + kind.ordinal()`，客户端 `NuclearDeviceMenu` 优先使用 payload kind 构造菜单。
- 客户端菜单不再强依赖当前位置已经同步出 `NuclearDeviceBlockEntity`：
  - 若客户端已有同 kind 的真实 BE，则直接使用真实 item handler。
  - 若客户端 BE 尚未到达，则创建轻量 `ItemStackHandler(kind.slots())` 用于 screen/layout 构造，避免客户端菜单构造阶段因 BE 同步时序失败而界面打不开。
  - `NuclearDeviceBlockEntity.isReady/isFilled` 抽为静态 helper，真实 BE 和客户端轻量 handler 共用同一套组件判断。
- 另一个实际风险点是 1.7.10 GUI 贴图文件名使用驼峰写法，如 `fatManSchematic.png`、`ivyMikeSchematic.png`。1.20 `ResourceLocation` 路径应使用小写资源名；现代资源改名为 `fat_man_schematic.png`、`ivy_mike_schematic.png` 等，并同步 `NuclearDeviceMenu.Layout` / `NuclearDeviceScreen` 引用。
- 本节覆盖前面“客户端菜单构造兜底”的旧方案；后续以显式 payload kind + 小写 GUI 资源路径为核弹 GUI 打开问题的当前修复口径。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：核弹 GUI 组件槽堆叠限制

- 继续实机反馈：核弹 GUI 已能打开，但部分组件槽可塞入一整组物品。
- 1.7.10 复核：
  - `TileEntityNukeGadget` / `TileEntityNukeMan` / `TileEntityNukeMike` / `TileEntityNukeTsar` 的 `getInventoryStackLimit()` 为 `1`。
  - `TileEntityNukeBoy` / `TileEntityNukePrototype` / `TileEntityNukeFleija` / `TileEntityNukeSolinium` / `TileEntityNukeN2` 的库存上限为 `64`，但其核弹部件在 `ModItems` 中大多以 `setMaxStackSize(1)` 注册；GUI 每个部件槽在实际装配语义上仍代表一个实体部件。
  - `early_explosive_lenses` / `explosive_lenses` 旧物品本身可堆叠，因此在现代 `ItemStackHandler` 里必须由槽位侧补充约束，否则可堆叠镜片会绕过旧 GUI 的一槽一件观感。
- 现代修正：
  - `NuclearDeviceBlockEntity#createItemHandler(...)` 统一作为真实 BE 与客户端菜单兜底 handler 的创建入口。
  - 核弹设备槽 `getSlotLimit(...)` 统一返回 `1`，并在 `setStackInSlot(...)` / `insertItem(...)` / `deserializeNBT(...)` 路径中夹紧已有或新插入堆叠。
  - `NuclearDeviceMenu` 的客户端轻量 handler 复用同一工厂，避免客户端 GUI 先构造时显示出与服务端不同的堆叠上限。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 核爆/辐射后效应缺口复核与 Prototype 点火器

复核来源：

- `com.hbm.entity.logic.EntityNukeExplosionMK5`
- `com.hbm.entity.logic.EntityNukeExplosionMK3`
- `com.hbm.entity.effect.EntityFalloutRain`
- `com.hbm.explosion.ExplosionNukeSmall`
- `com.hbm.blocks.bomb.NukePrototype`
- `com.hbm.blocks.bomb.NukeMike`
- `com.hbm.blocks.bomb.NukeTsar`
- `com.hbm.items.ModItems`

核爆/辐射链路结论：

- MK5 旧版完成爆炸后只生成 `EntityFalloutRain`，并在前 10 tick 对实体做一次强直射辐射；当前现代 `NukeExplosionMk5Entity` 已保留该结构。
- MK3 waste 旧版在破坏阶段完成后生成 `EntityFalloutRain`；当前现代 `NukeExplosionMk3Entity` 已保留该结构。
- 旧 `EntityFalloutRain` 不直接增加 `ChunkRadiationManager` 的区块辐射值，而是做落尘层、废土/废木/废叶替换和陨坑生物群系；当前现代 `FalloutRainEntity` 使用 `LegacyFalloutConversions` 与 `CraterRadiationData` 承接该后效应。
- `ExplosionNukeSmall` 的 mini-nuke 区块辐射脚印公式 `50 / (abs(i)+abs(j)+1) * radiationLevel/3` 已在现代 `ExplosionNukeSmall#irradiateMiniNukeFootprint(...)` 中存在。
- `NukeEnvironmentalEffect.applyStandardAOE(...)` 在旧源码中标注 deprecated，且主 MK5/MK3 链路没有调用；当前现代类存在但不作为本轮核爆后效应入口。
- `NukeMike` 旧源码有半装载分支传入 `manRadius`，但 `igniteTestBomb(...)` 内部实际硬编码生成 `BombConfig.mikeRadius` 的 MK5；现代 `MIKE` 继续按 `mikeRadius` 起爆，不改成半装载降级，避免偏离旧实现细节。

现代修正：

- 补回旧通用 `ModItems.igniter`：
  - 注册旧 ID `igniter` 到核弹创造栏。
  - 复制旧贴图 `assets/hbm/textures/items/trigger.png` 到现代 `assets/hbm/textures/item/trigger.png`。
  - 新增 `models/item/igniter.json`，使用旧 `trigger` 贴图。
  - 新增英文/中文语言项。
- `NuclearDeviceBlock#use(...)` 对 `Kind.PROTOTYPE` 增加旧版特殊右键：
  - 非潜行且手持 `hbm:igniter` 时，不打开 GUI；
  - 服务端调用 `detonateArmed(...)`，因此仍要求 Prototype 装载 `isReady()` 成功；
  - 复用当前统一核弹方块起爆路径清空槽位、移除方块并调度 `NuclearExplosionUtil.spawnPrototype(...)`。

当时仍缺/延期：

- 旧 `EntityNukeTorex`、`EntityCloudFleija`、`EntityCloudSolinium` 大型视觉云当时仍未完整迁入；后续 2026-05-25 已补 Fleija/Solinium/Rainbow 与 Torex 大型视觉云。
- `NukeCustom` 方块、完整 `TileEntityNukeCustom` GUI/库存与 custom missile 实体仍待后续迁入。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：Fleija / Solinium / Euphemium 大型云实体

复核来源：

- `com.hbm.entity.effect.EntityCloudFleija`
- `com.hbm.entity.effect.EntityCloudSolinium`
- `com.hbm.entity.effect.EntityCloudFleijaRainbow`
- `com.hbm.render.entity.effect.RenderCloudFleija`
- `com.hbm.render.entity.effect.RenderCloudSolinium`
- `com.hbm.render.entity.effect.RenderCloudRainbow`
- `com.hbm.entity.EntityMappings`
- `com.hbm.blocks.bomb.NukePrototype`
- `com.hbm.blocks.bomb.NukeFleija`
- `com.hbm.blocks.bomb.NukeSolinium`
- `com.hbm.blocks.bomb.NukeCustom`

旧版行为要点：

- `NukePrototype` / `NukeFleija` 起爆时：
  - MK3 爆炸实体位置为 `(x + 0.5, y + 0.5, z + 0.5)`；
  - 同时生成 `EntityCloudFleija(world, r)`，位置为方块坐标 `(x, y, z)`；
  - 云实体每 tick 增加 `age/scale`，设置 `worldObj.lastLightningBolt = 2`，服务端 `age >= maxAge` 后移除。
- `NukeSolinium` 起爆时：
  - MK3 爆炸实体位置同样为方块中心；
  - 同时生成 `EntityCloudSolinium(world, r)`，位置为方块坐标 `(x, y, z)`；
  - 云实体每 tick 在 `(posX, posY + 200, posZ)` 生成闪电实体，`age >= maxAge` 后移除。
- `NukeCustom`：
  - `euph > 0` 分支生成 `EntityNukeExplosionMK3`，`destructionRange = 150`，并生成 `EntityCloudFleijaRainbow(world, 50)`；
  - `schrab > 0` 分支生成 `EntityCloudFleija(world, schrab)`，位置为 `(x + 0.5, y + 0.5, z + 0.5)`。
- 旧 `EntityMappings` 中 `EntityCloudFleija` 注册名为 `entity_cloud_fleija`，`EntityCloudFleijaRainbow` 注册名为 `entity_cloud_rainbow`；`EntityCloudSolinium` 旧源码也写成了 `entity_cloud_rainbow`，但这是旧注册表可容忍的重复名风险。
- 旧 renderer：
  - Fleija 使用 `ResourceManager.sphere_new`，青色核心、暗青加法外层和灰白 shockwave。
  - Solinium 使用 `ResourceManager.sphere_new`，颜色 `0x27FFDA`，外层 alpha `0.125` 加法混合。
  - Rainbow 使用 `models/Sphere.obj`，每次渲染随机 RGB，先 `age` 缩放，再渲染 0.5 内核和 0.6 到 1.0 的外层。

现代迁入：

- 新增现代实体：
  - `CloudFleijaEntity`
  - `CloudSoliniumEntity`
  - `CloudFleijaRainbowEntity`
- 新增客户端 renderer：
  - `CloudFleijaRenderer`
  - `CloudSoliniumRenderer`
  - `CloudFleijaRainbowRenderer`
- 复用现代 OBJ 库中的 `ObjEffectModels.SPHERE_NEW` 渲染 Fleija/Solinium；补入旧 `Sphere.obj` 到 `assets/hbm/models/block/effects/sphere.obj` 并暴露为 `ObjEffectModels.SPHERE`，用于 Rainbow 云。
- `ModEntityTypes` 新增：
  - `entity_cloud_fleija`
  - `entity_cloud_rainbow`
  - `entity_cloud_solinium`
- Solinium 现代注册名使用 `entity_cloud_solinium`，避免旧源码中与 rainbow 重名导致 1.20 注册冲突。
- `NuclearExplosionUtil` 现在在 MK3 Fleija/Solinium 爆炸实体成功加入世界后同步生成对应云实体：
  - 普通 `spawnFleija/spawnSolinium` 保持云位置等于传入爆心，用于 custom/导弹/投掷物语义。
  - 方块核弹入口 `spawnPrototype/spawnFleijaBomb/spawnSoliniumBomb` 使用云位置 `(center - 0.5)`，对齐旧 `NukePrototype/NukeFleija/NukeSolinium` 的方块坐标云。
  - 新增 `spawnFleijaRainbow(...)`，`CustomNukeExplosion.euph > 0` 改回旧彩虹 Fleija 云，半径 150，云寿命 50。

### 2026-05-25 追加：EntityNukeTorex 普通/野火大核爆云

复核来源：

- `com.hbm.entity.effect.EntityNukeTorex`
- `com.hbm.render.entity.effect.RenderTorex`
- `com.hbm.entity.EntityMappings`
- `assets/hbm/textures/particle/particle_base.png`
- `assets/hbm/textures/particle/flare.png`
- `assets/hbm/sounds/weapon/nuclearExplosion.ogg`
- `assets/hbm/sounds.json` 中旧 `weapon.nuclearExplosion`
- 参考版 `com.hbm.entity.effect.EntityNukeTorex` / `EntityTorexRender` 仅作 1.20 API 适配参考；缩放和 cloudlet 生成公式回到 1.7.10。

旧版行为要点：

- `EntityMappings` 注册名为 `entity_effect_torex`，追踪距离旧代码后续通过 `TrackerUtil.setTrackingRange(..., 1000)` 提升。
- 服务端只同步 `scale` 与 `type` 并按 `45 * 20 * scale` 生命周期移除；实体不保存，读取 NBT 时直接死亡。
- `statFacStandard` / `statFacBale` 都调用 `statFac`，缩放公式为 `clamp(squirt(radius * 0.01) * 1.5, 0.5, 5)`；`type=1` 为野火绿色云。
- 客户端模拟 cloudlet：
  - tick 1 将局部仿真 scale 固定为 1.5；
  - 前 100 tick sky flash；
  - 普通 mushroom cloud 每 tick 按 `ceil(10 * simSpeed^2)` 生成；
  - 前 150 tick 生成 shock cloud，并在玩家进入声波半径时播放旧 `weapon.nuclearExplosion`；
  - `tick < 130 * 1.5` 生成 ring cloud；
  - `tick > 130 * 1.5 && tick < 600 * 1.5` 与 `tick > 200 * 1.5 && tick < 600 * 1.5` 生成两层 condensation cloud；
  - core/torus/roller/heat 更新公式沿用 1.7.10。
- 旧 renderer 使用 `particle_base.png` billboard 渲染 cloudlet，按玩家距离从远到近排序；前 100 tick 用 `flare.png` 叠加三张闪光 billboard；播放核爆声后触发玩家 hurtTime 震动。

现代迁入：

- 新增 `NukeTorexEntity`：
  - 注册名 `entity_effect_torex`；
  - `SynchedEntityData` 同步 scale/type；
  - 保留 1.7.10 `squirt(radius * 0.01) * 1.5` 缩放、`45 * 20 * scale` 生命周期、cloudlet 阶段和颜色公式；
  - 客户端播放独立声事件 `weapon.nuclear_explosion`，对应旧 `weapon.nuclearExplosion`。1.20 `ResourceLocation` 不允许大写，因此现代 ID 使用 snake_case，资源来自旧 `nuclearExplosion.ogg`。
- 新增 `NukeTorexRenderer`：
  - 使用普通 `VertexConsumer` billboard 渲染 `particle_base.png` cloudlet；
  - 复刻旧 flare 前 100 tick 闪光；
  - 保留 sound 后客户端 hurtTime 震动。
- `ModEntityTypes` / `ClientModEvents` 接入实体和 renderer。
- `ModSounds`、`sounds.json`、语言项新增 `weapon.nuclear_explosion` / `subtitles.hbm.weapon.nuclear_explosion`。
- 复制旧资源：
  - `textures/particle/flare.png`
  - `sounds/weapon/nuclearExplosion.ogg` -> `sounds/weapon/nuclear_explosion.ogg`
- `NuclearExplosionUtil` 现在在普通 MK5/N2/no-fallout/dirty fallout 核爆实体成功加入世界后同步生成 standard Torex。
- `WeaponExplosionUtil.spawnBalefire(...)` 在生成 `BalefireExplosionEntity` 后同步生成 balefire Torex。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks` 通过。

### 2026-05-25 追加：核爆冲击波粒子与屏幕 jolt 接线复核

复核来源：

- `com.hbm.explosion.ExplosionNukeSmall`
- `com.hbm.explosion.ExplosionLarge`
- `com.hbm.main.ClientProxy`
- `com.hbm.particle.ParticleMukeWave`
- `com.hbm.entity.effect.EntityNukeTorex`
- `com.hbm.render.entity.effect.RenderTorex`

旧版实现位置结论：

- 小/中型 `ExplosionNukeSmall#explode(...)` 的核爆视觉不是直接生成粒子类，而是服务端发送 `type=muke/tinytot` 的 `AuxParticlePacketNT`，客户端由 `ClientProxy` 创建 `ParticleMukeWave` 并设置本地玩家 `hurtTime/maxHurtTime=15`。
- `ParticleMukeWave` 是用户可见的地面冲击波环，使用 `textures/particle/shockwave.png`，默认 25 tick 和 `waveScale=45`。
- 大型 MK5 核爆使用 `EntityNukeTorex` 的 shock cloud；客户端在声波范围到达玩家后播放 `weapon.nuclearExplosion`，`RenderTorex` 随后触发一次 15 tick hurt 动画抖动。

现代接线：

- `ExplosionNukeSmall#explode(...)` 改为通过 `ParticleUtil.spawnNuclearBurstVisual(...)` 发出 `muke/tinytot`，仍保持旧范围 250 和 `muke` 1% `balefire` 标志。
- `HbmParticleEffects#spawnMuke(...)` 补回 `MukeWaveParticle` 冲击波和 15 tick 客户端 jolt；大型 Torex 的声波抖动仍在 `NukeTorexRenderer` 中按旧时机触发。
- 验证：`.\gradlew.bat clean compileJava processResources --no-daemon` 通过。

仍缺/延期：

- `NukeCustom` 方块、完整 `TileEntityNukeCustom` GUI/库存与 custom missile 实体仍待后续迁入；本批只补齐当前已有核爆/野火爆炸入口的 Torex 视觉云。
- `EntityCloudFleijaRainbow` 的武器/子弹/TileEntityCore 等其他调用点属于武器或机器迁移范围，本批没有扩线接入。
- 本批不改变辐射库/区块辐射数据算法，只补核爆大型视觉实体与旧音效。

### 2026-05-25 追加：NukeCustom 方块/库存/GUI 安全闭环

复核来源：

- `com.hbm.blocks.bomb.NukeCustom`
- `com.hbm.tileentity.bomb.TileEntityNukeCustom`
- `com.hbm.inventory.container.ContainerNukeCustom`
- `com.hbm.inventory.gui.GUINukeCustom`
- `com.hbm.render.item.ItemRenderLibrary` 中 `ModBlocks.nuke_custom` 物品渲染
- `ResourceManager.bomb_boy` / `ResourceManager.bomb_custom_tex`
- `assets/hbm/textures/gui/weapon/gunBombSchematic.png`
- `assets/hbm/textures/models/CustomNuke.png`
- `assets/hbm/textures/items/custom_tnt.png` 等 `custom_*` 装料贴图

旧版行为要点：

- `nuke_custom` 是独立自定义核弹，不等同 `bomb_multi` 多用途炸弹。
- 方块为非完整模型，右键打开 `ContainerNukeCustom` / `GUINukeCustom`，潜行右键不打开 GUI。
- 红石供能时调用 `explode(...)`。
- `TileEntityNukeCustom` 有 27 槽库存，所有槽普通容器交互，SidedInventory 不允许自动插入/抽出。
- 每 tick 根据 `registerBombItems()` 条目表重算 `tnt/nuke/hydro/amat/dirty/schrab/euph`：
  - ADD 条目累加强度，MULT 条目用 `value * stackSize` 连乘倍率；
  - 计算后执行门槛：`tnt < 16 -> nuke=0`，`nuke < 100 -> hydro=0`，`nuke < 50 -> amat/schrab=0`，`schrab == 0 -> euph=0`。
- `custom_fall` 存在时旧版起爆不立即爆炸，而生成 `EntityFallingNuke`；该实体承载七项强度。
- 物品渲染复用 Little Boy 模型 `bomb_boy`，贴图换成 `CustomNuke.png`，inventory 缩放 5。
- GUI 使用 `gunBombSchematic.png`，27 槽为 3x9，玩家物品栏从 y=140 起；右侧图标按最高优先级阶段渲染，dirty 在核/热核路径叠加。

现代迁入：

- 新增 `CustomNukeBlock`，注册旧 ID `nuke_custom` 到核弹创造栏：
  - 右键打开独立 `CustomNukeMenu`；
  - 红石供能按当前统计值起爆；
  - 起爆前清空 27 槽并移除方块；
  - 当前批次不实现 falling 路径，见延期。
- 新增 `CustomNukeBlockEntity`：
  - 27 槽 `ItemStackHandler`；
  - 保存到 `Inventory` NBT；
  - 服务端 tick/cache 重算 `CustomNukeStats`；
  - 条目表按旧 `registerBombItems()` 迁入，现代端尚未存在的 item/block 条目采用“注册存在才接入”的条件式条目，避免硬依赖未迁内容。
- 新增 `CustomNukeMenu` / `CustomNukeScreen`：
  - 保留旧 3x9 槽位布局和 `gunBombSchematic` 背景；
  - 同步七项强度和 falling 标记；
  - 补回旧 GUI 阶段图标与 tooltip 要点。
- 新增 custom 装料物品：
  - `custom_tnt`
  - `custom_nuke`
  - `custom_hydro`
  - `custom_amat`
  - `custom_dirty`
  - `custom_schrab`
  - `custom_fall`
- 2026-05-25 启动修正：`custom_*` 物品已由字段显式注册，`NUKE_TAB_ITEMS` 必须复用这些 `RegistryObject`，不能再通过 `simpleStackOneItems("custom_tnt", ...)` 二次注册，否则 Forge 会报 `Duplicate registration custom_tnt`。
- 复制旧资源：
  - `textures/items/custom_*.png` -> `textures/item/custom_*.png`
  - `textures/gui/weapon/gunBombSchematic.png` -> `textures/gui/weapon/gun_bomb_schematic.png`
  - `textures/models/CustomNuke.png` -> `textures/block/nuke/custom_nuke.png`
- 自定义核弹 BER/item renderer 复用 modern Little Boy OBJ，贴图换成 `custom_nuke`，对齐旧 `ResourceManager.bomb_boy + bomb_custom_tex`。

仍缺/延期：

- `custom_fall` 当前只参与统计/GUI 标记，尚未恢复旧 `EntityFallingNuke` 的七强度坠落实体路径。
- 旧 `TileEntityNukeCustom.registerBombItems()` 中依赖未迁物品/方块的条目暂未激活；对应内容迁入后应扩展条件式条目表或转为完整注册。
- `EntityMissileCustom`、missile part item/metadata、发射台结构仍待后续迁入。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks` 通过。

### 2026-05-25 追加：普通核爆 waste 后处理与落尘资源复核

复核来源：

- `com.hbm.explosion.ExplosionNukeGeneric#wasteDest`
- `com.hbm.explosion.ExplosionNukeGeneric#wasteDestNoSchrab`
- `com.hbm.entity.effect.EntityFalloutRain#stomp`
- `com.hbm.config.FalloutConfigJSON#initDefault`
- `com.hbm.crafting.SmeltingRecipes`

旧版行为要点：

- `wasteDest` 和 `wasteDestNoSchrab` 不是同一个方块替换表：
  - 普通 `wasteDest` 只直接清除木门/铁门；
  - `wasteDestNoSchrab` 额外清除玻璃、染色玻璃和树叶；
  - 两者的 mossy cobblestone 都转 `coal_ore`，不转油矿。
- 蘑菇方块旧 metadata 10 是 stem -> `waste_log`，其他蘑菇方块清空；现代端对应 `Blocks.MUSHROOM_STEM` 与红/棕蘑菇方块。
- 核爆和 fallout 直接生成的是 `waste_trinitite` / `waste_trinitite_red`；`glass_trinitite` 来自烧炼两种 waste trinitite，不是爆炸直接落点。
- `EntityFalloutRain` 里还有 `volcano_core -> volcano_rad_core` 特例，但现代端 `BlockVolcano` / `TileEntityVolcanoCore` 尚未迁入，不能安全接入。

现代修正：

- `ExplosionNukeGeneric#wasteDest` 拆回旧分支差异：
  - `allowSchrabidium=true` 的普通 waste 不再错误清掉玻璃/树叶；
  - `allowSchrabidium=false` 的 NoSchrab waste 才清玻璃/树叶；
  - mossy cobblestone 统一改回 `coal_ore`；
  - `MUSHROOM_STEM` -> `waste_log`，红/棕蘑菇块 -> air。
- 注册并接入 `waste_trinitite` / `waste_trinitite_red` 后，`ExplosionNukeGeneric` 的 sand/red sand 低概率替换不再回退到 `block_trinitite`。
- Deprecated 但仍存在的 `NukeEnvironmentalEffect#applyStandardEffect` 也从旧占位 `block_trinitite` 改回 sand -> `waste_trinitite`、red sand -> `waste_trinitite_red`。
- 相关 fallout table、实体落尘雨 renderer、Sellafield/Trinitite 资源和 hazard 接线记录在 radiation-core 文档的 “Nuclear Fallout Sellafield/Trinitite Parity Pass” 中。

仍缺/延期：

- `volcano_core` / `volcano_rad_core` 需要随火山核心方块与 TileEntity 一起迁入；本批只记录差异，不使用其它方块替代。
- `VersatileConfig.getSchrabOreChance()` 仍未迁入现代配置；schrabidium 分支仍是当前固定概率路径。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks` 通过。

### 2026-05-26 追加：Torex 黑幕、追踪重入与 MK5 进度保存修正

复核来源：

- `com.hbm.entity.effect.EntityNukeTorex`
- `com.hbm.render.entity.effect.RenderTorex`
- `com.hbm.explosion.ExplosionNukeRayBatched`
- `com.hbm.entity.logic.EntityNukeExplosionMK5`

旧版行为要点：

- `RenderTorex#flashWrapper` 对 `textures/particle/flare.png` 使用 `GL_SRC_ALPHA, GL_ONE` 加法混合；黑底 flare 贴图不应按普通 alpha 混合画成方形黑幕。
- `RenderTorex#cloudletWrapper` 对云团使用普通 alpha 混合、关闭 depth mask；flare 和 cloudlet 的混合状态不是同一种。
- 声波到达客户端玩家后，旧 `RenderTorex` 只触发一次：`hurtTime = 15`、`maxHurtTime = 15`、`attackedAtYaw = 0F`，作为手持物/物品栏的受击摇晃视觉。
- 旧 `EntityNukeTorex` 自身不写 NBT，并通过 `TrackerUtil.setTrackingRange(world, torex, 1000)` 尽量保持客户端持续追踪；现代测试环境中离开追踪范围后重入会重新从 0 tick 播放，需要额外同步视觉 age。
- `EntityNukeExplosionMK5` 旧路径由 `ExplosionNukeRayBatched` 分批缓存/破坏；现代端如果只保存 tick/strength 而不保存 batched ray 状态，卸载后会重新初始化进度。

现代修正：

- `NukeTorexRenderer` 拆出普通云团与 flare 的 RenderType：
  - cloudlet 保持普通 alpha、无 depth write；
  - flare 改为 `SRC_ALPHA/ONE` 加法混合、无 depth write，避免黑底贴图被当成半透明幕布。
- `NukeTorexEntity` 新增同步 age：
  - 服务端把当前 `tickCount` 写入 entity data；
  - 新客户端追踪到实体时按同步 age 重建 cloudlet 状态，不再从初始核闪重新播放；
  - catch-up 期间标记旧声音/摇晃已播放，避免重入区域后再次触发声波与 jolt。
- `NukeTorexEntity` 现在保存 `ticksExisted`、`scale`、`type`，并允许保存实体；这是现代追踪/卸载需求的补强，旧版本身依赖大追踪半径而不是实体 NBT。
- `NukeExplosionMk5Entity` 现在保存并恢复 `ExplosionNukeRayBatched` 的分批状态，包括 GSP 游标、每 chunk 的射线强度缓存和有序 chunk 列表，避免世界卸载后从头炸第二遍。

仍缺/注意：

- 现代端仍没有直接写旧 `attackedAtYaw` 的等价公开路径；当前保留 `hurtTime/hurtDuration` jolt，字段边界沿用 sound/particle 记录中的限制。
- Torex catch-up 会按同步 age 重放云团生成逻辑来恢复视觉状态；这是为了保持旧 cloudlet 形态，极晚追踪时可能带来一次客户端补算成本。

验证：

- 2026-05-26 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

### 2026-05-26 追加：Torex 重载白块、云团遮挡与 fallout rain 可见性回归修正

实机回归问题：

- 重进游戏/重载核爆中心后，蘑菇云中心可能出现巨大的白色 flare 方片，并触发明显卡顿。
- 蘑菇云透明云团后方的 vanilla 云层/水域会穿透显示。
- `hbm:crater_outer` 外圈草色仍可能显示为普通绿色。
- fallout rain 实体生成后客户端看不到雨，地表 `fallout` 薄层也可能不可见。

复核来源：

- `com.hbm.entity.effect.EntityNukeTorex`
- `com.hbm.render.entity.effect.RenderTorex`
- `com.hbm.entity.effect.EntityFalloutRain`
- `com.hbm.render.entity.effect.RenderFallout`
- `com.hbm.world.biome.BiomeGenCraterBase`
- `com.hbm.blocks.generic.BlockFallout`

修正：

- `NukeTorexEntity` 回到 1.7.10 生命周期：大型 Torex 是临时视觉实体，不保存到 NBT，读取到旧保存数据时直接丢弃；不再在世界重载后恢复半截实体，也不再补算历史 cloudlets。
- `NukeTorexRenderer` 的云团增加 cutout-only 深度预写 pass，随后仍按旧版 alpha pass 上色；这样后续云层/水域不再轻易透过蘑菇云主体，同时透明边缘仍按粒子贴图裁剪。
- `FalloutRainEntity` 初始 `tickDelay` 改回旧 `BombConfig.fDelay` 语义，并显式允许远距离渲染；避免服务端首 tick 处理完并移除实体，客户端还没来得及画雨。
- `fallout` 薄层模型补回旧 `BlockFallout` 的 0.125 方块高度几何，对应 2/16 高度，避免只继承 `thin_block` 而没有实际可见面。
- crater outer 草/叶颜色只对 `hbm:crater_outer` 生效；客户端颜色 handler 现在在 `BlockAndTintGetter` 不能提供 biome key 时，回退到当前客户端世界按坐标读取 biome。

边界：

- `flare.png` 本身是全不透明 alpha，旧版依赖加法混合让黑底不可见；本次白方块主要按“实体重载恢复核闪”处理，保持 flare 的旧加法混合路径不变。
- 云团深度预写是现代渲染顺序补强，旧版 OpenGL pass 本身没有写深度；该补强只为避免 1.20 透明/天气/水 pass 顺序导致的穿透观感。

### 2026-05-26 追加：MK5 流体清理网格与算法缺口复核

复核来源：

- `com.hbm.explosion.ExplosionNukeRayBatched`
- `com.hbm.entity.logic.EntityNukeExplosionMK5`
- `com.hbm.explosion.ExplosionNukeGeneric`
- `com.hbm.entity.logic.EntityFalloutRain`

旧版行为要点：

- 默认 MK5 路径使用 `ExplosionNukeRayBatched`；`EntityNukeExplosionMK5` 源码里的 parallelized 分支被注释，现代继续以 batched 路径为事实来源。
- 旧射线采样遇到液体时不消耗爆炸强度：`!block.getMaterial().isLiquid()` 才扣抗性；但液体仍是非 air 方块，会被加入待删除坐标，后续 `processChunk` 用 `setBlock(..., Blocks.air, 0, 2/3)` 清掉。
- 旧 `processChunk` 按 chunk 缓存射线命中坐标，再分批删除；因此水/熔岩这类方块不会留下现代流体状态独立存在的大片竖直面。
- `EntityNukeExplosionMK5` 每 tick 保持中心 chunk 加载，前 10 tick 在有 fallout 且强度足够时调用直射辐射，射线完成后生成 `EntityFalloutRain`。

现代修正：

- `ExplosionNukeRayBatched` 现在把 `BlockState.isAir()` 但 `FluidState` 非空的位置视为旧版非 air 命中，而不是跳过。
- 二次复核发现 `collectTip` 收集阶段仍只用 `!state.isAir()` 判断是否缓存 lastPos/chunk；这会让只呈现为 `FluidState` 的位置在破坏阶段前就丢失。收集阶段现已改成和 `processChunk` 同一个 `isLegacyEmpty` 判定。
- 命中流体时额外清理 3x3x3 邻域内仍带 `FluidState` 的位置，避免现代水/熔岩只清到射线中心而留下网格状面片。
- 2026-05-27 二次复核后，普通清除恢复旧版 `setBlock(..., flags=2)` 语义，tip 坐标保留 `flags=3`；避免在 MK5 分批破坏期间对每个流体普通命中强制 neighbor update，导致邻近水源立即重铺成新的流动片。
- 2026-05-27 再复核切区块残留水片后，发现现代 `addFluidCleanup` 曾用 `hasChunk` 跳过未加载邻区块；旧版液体本身是 block，不存在跨 chunk 的独立 `FluidState` 漏读。现代端改为处理当前 MK5 work chunk 前接入现有 `NukeExplosionMk5Entity#loadChunk`，并在 3x3x3 流体邻域清理时同步读取相邻 chunk 后再判断 `FluidState`，避免 chunk 边界留下整片流体墙。
- `ExplosionNukeGeneric#destruction` / `vaporDest` 的早退条件也改成 `isLegacyEmpty`，避免 MK3/Advanced 柱状清除提前跳过带 `FluidState` 的位置；`waste` / `solinium` 未扩展清流体，因为旧版对应分支没有“液体即清除”的契约。

仍缺/注意：

- 旧 MK5 每 tick 触发 `MainRegistry.achManhattan`，现代还没有对应 advancement 接线。
- 旧版 extended logging 会在 MK5 初始化和完成时写日志；现代当前没有迁入等价配置项。
- `ExplosionNukeGeneric#isExplosionExempt` 旧列表包含 ocelot、B92 beam、bullet、grenade、creative player 等具体实体；现代只迁了当前已存在实体能表达的安全子集，未注册的旧 projectile 类型仍等待对应武器实体迁入。
- 旧 `WorldUtil.loadAndSpawnEntityInWorld` 的更宽泛强加载/生成语义没有完整移植；现代 MK5 自身的 forced chunk 进度保存已覆盖当前核爆处理需求。
- 本次未恢复或新增地下核爆空腔判断；流体修正只围绕旧 MK5 射线命中/清除与现代 chunk 读取差异。

验证：

- 2026-05-26 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

### 2026-05-26 追加：Torex 云团深度预写回退与 crater/fallout rain 二次修正

回归结论：

- `RenderTorex#cloudletWrapper` 在 1.7.10 中只做普通 alpha blending，并且 `glDepthMask(false)`；现代端尝试增加 cloudlet depth prepass 会把原版云层表现成硬白色块，视觉偏离旧版，已回退。
- `BiomeGenCraterBase` 三个 crater biome 都覆写草色：`crater_outer` 为 `0x776F59/0x6F6752`，`crater` 为 `0x606060/0x505050`，`crater_inner` 为 `0x404040/0x303030`，叶色统一 `0x6A7039`。
- 草方块物品栏渲染没有世界/坐标时不能返回白色，应回退原版 `GrassColor.getDefaultColor()` / `FoliageColor.getDefaultColor()`。
- `RenderFallout` 旧版按相机周围生成雨柱，并通过 tessellator translation 转成相机空间；现代 `EntityRenderer` 已处于实体局部矩阵中，顶点必须相对 fallout entity 原点输出，否则雨柱会被画到错误位置。

现代修正：

- `NukeTorexRenderer` 移除 cloudlet depth prepass，恢复单 pass 普通透明云团；flare 仍保留旧版 `SRC_ALPHA/ONE` 加法混合。
- `ClientModEvents` 对 vanilla grass/foliage 的 crater tint 补齐三圈旧颜色，并修正无世界/坐标时的默认绿色 fallback，避免创造栏草方块变灰白。
- `NukeTorexEntity` 在前 100 tick 将客户端 sky flash 维持至少 4 tick，避免每 tick 写 2 导致现代端视觉频闪，同时仍保留 1.7.10 的持续亮天窗口。
- `FalloutRainRenderer` 改为按实体原点输出玩家周围雨柱顶点；`FalloutRainEntity` 读取旧/缺失 NBT 时保留 `firstTick=true` 默认，避免跳过旧版首 tick chunk 收集。

验证：

- 2026-05-26 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed；当时仍有 `Biome.BIOME_INFO_NOISE` 弃用警告，后续已用同 seed/同 octave 的本地 `PerlinSimplexNoise` 实例替换。

### 2026-05-27 追加：Crater plantNoise 废弃 API 替换

复核来源：

- `net.minecraft.world.level.biome.Biome`
- `net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise`
- `com.hbm.world.biome.BiomeGenCraterBase`

问题与旧版对齐：

- `ClientModEvents#legacyPlantNoise` 之前直接调用 `Biome.BIOME_INFO_NOISE`，能复刻外圈 crater 草色噪声，但 Forge 1.20.1 编译会提示该字段 deprecated for removal。
- 1.20.1 `Biome.BIOME_INFO_NOISE` 的实际初始化为 `new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), ImmutableList.of(0))`。

现代修正：

- `ClientModEvents` 新增本地 `CRATER_PLANT_NOISE`，使用相同 `WorldgenRandom + LegacyRandomSource(2345L) + octave 0` 构造。
- `legacyPlantNoise` 改为读取本地噪声实例，保留 `x * 0.225D / z * 0.225D / useNoiseOffsets=false` 采样公式，输出与旧调用保持一致，同时移除废弃 API 访问。

### 2026-05-26 追加：核爆处理器中心坐标截断复核

复核来源：

- `com.hbm.entity.logic.EntityNukeExplosionMK5`
- `com.hbm.entity.logic.EntityNukeExplosionMK3`
- `com.hbm.explosion.ExplosionNukeRayParallelized`

旧版行为要点：

- MK5 初始化 `ExplosionNukeRayBatched` 时使用 `(int) posX/Y/Z`，Java 对负小数坐标向 0 截断，而不是 floor。
- MK3 初始化 `ExplosionNukeAdvanced` / `ExplosionFleija` / `ExplosionSolinium` 时同样使用 `(int) this.posX/Y/Z`。
- 这只影响带小数的负坐标爆心；正坐标和整数坐标没有差异。

现代修正：

- `NukeExplosionMk5Entity` 创建和恢复 `ExplosionNukeRayBatched` 时改用 `(int) getX/Y/Z()`。
- `NukeExplosionMk3Entity#initProcessors` 改用 `(int) getX/Y/Z()`。
- `ExplosionNukeRayParallelized` 兼容入口也从 `Math.floor(...)` 改回旧式 `(int)` 截断。

边界：

- 本项不是“弹坑太圆”的主因；它修的是负坐标爆心中心偏移一格的对齐问题。

### 2026-05-26 追加：Torex 冲击波抵达玩家 UI 抖动修正

复核来源：

- `com.hbm.entity.effect.EntityNukeTorex`
- `com.hbm.render.entity.effect.RenderTorex`
- Minecraft 1.20.1 `GameRenderer#bobHurt`
- Minecraft 1.20.1 `Player#getHurtDir` / `Player#animateHurt`

旧版行为要点：

- `EntityNukeTorex` 客户端在冲击波前沿抵达本地玩家时播放 `hbm:weapon.nuclearExplosion`，并设置 `didPlaySound = true`。
- `RenderTorex` 同帧检测 `didPlaySound && !didShake`，且全局 `shakeTimestamp` 超过 1 秒后：
  - `player.hurtTime = 15`
  - `player.maxHurtTime = 15`
  - `player.attackedAtYaw = 0F`
- 该效果依赖 Minecraft 受击相机/手持物 bob，不是服务端伤害，也不会扣血。

现代修正：

- 1.20.1 中旧 `attackedAtYaw` 对应 `Player#hurtDir`，`GameRenderer#bobHurt` 实际读取 `hurtTime / hurtDuration / getHurtDir()`。
- `NukeTorexEntity#applyClientShockwaveShake` 现在调用 `player.animateHurt(0.0F)` 对齐旧 `attackedAtYaw = 0F`，随后把 `hurtTime/hurtDuration` 补成旧版 15 tick。
- 摇晃触发从仅依赖 entity renderer 的后置路径，补到 `tryPlayClientSound` 的声波抵达点，确保冲击波抵达时本地玩家立即进入受击 bob 状态。
- 修正客户端 age catch-up：新追踪实体时不再无条件设置 `didPlaySound/didShake = true`；只有同步 age 对应的声波半径已经越过当前本地玩家时，才跳过旧声音/摇晃，避免远处玩家刚开始追踪云团后在冲击波真正抵达前被标记为已摇晃。

验证：

- 2026-05-26 ran `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`: passed.

### 2026-05-26 追加：Torex 核闪遮罩与 HUD 矩阵抖动补齐

复核来源：

- `com.hbm.entity.effect.EntityNukeTorex`
- `com.hbm.render.entity.effect.RenderTorex`
- `com.hbm.main.ModEventHandlerClient`
- `com.hbm.main.ModEventHandlerRenderer`
- `com.hbm.config.ClientConfig`

旧版行为要点：

- `RenderTorex` 在 `ticksExisted < 10` 且上次核闪超过 1 秒时刷新 `ModEventHandlerClient.flashTimestamp`。
- `ModEventHandlerClient` 在 `RenderGameOverlayEvent.Pre` 的 `CROSSHAIRS` 阶段绘制全屏白色 quad，持续 5000 ms，混合方式为 `SRC_ALPHA / ONE`，受 `ClientConfig.NUKE_HUD_FLASH` 控制。
- `RenderTorex` 在 `didPlaySound && !didShake` 且上次摇晃超过 1 秒时刷新 `shakeTimestamp`，同时触发旧版 15 tick 受击 bob。
- `ModEventHandlerRenderer` 在 `RenderGameOverlayEvent.Pre` 的 `HOTBAR` 阶段按旧公式平移 HUD：`mult = remaining / 1500 * 2`，水平 `clamp(sin(now * 0.02), -0.7, 0.7) * 15`，垂直 `clamp(sin(now * 0.01 + 2), -0.7, 0.7) * 3`，受 `ClientConfig.NUKE_HUD_SHAKE` 控制。

现代修正：

- 新增 `NukeHudEffects` 作为客户端全局核爆 HUD 效果状态，保留旧 5000 ms 核闪、1500 ms HUD 抖动与 1 秒防重复触发窗口。
- `NukeTorexEntity` 在客户端爆心前 10 tick 触发核闪；冲击波抵达本地玩家并播放 `weapon.nuclear_explosion` 时触发 HUD 抖动和 15 tick hurt bob。
- `ClientForgeEvents` 在 `RenderGuiOverlayEvent.Pre` 的 `CROSSHAIR` overlay 绘制白色加法遮罩；在 `HOTBAR` overlay 前 push pose 并套用旧正弦平移，Post 阶段渲染本模组 HUD 后 pop pose，避免矩阵状态泄漏。
- `HbmClientConfig` 补回旧 `NUKE_HUD_FLASH` / `NUKE_HUD_SHAKE` 客户端开关，默认均为 `true`。

验证：

- 2026-05-26 ran `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`: passed.

### 2026-05-26 追加：Torex HUD/相机摇晃二次对齐复核

复核来源：

- `com.hbm.render.entity.effect.RenderTorex`
- `com.hbm.main.ModEventHandlerClient`
- `com.hbm.main.ModEventHandlerRenderer`
- Minecraft 1.20.1 `GameRenderer#bobHurt`
- Forge 1.20.1 `ForgeGui` / `VanillaGuiOverlay`

旧版对齐点：

- `shakeTimestamp` 是全局核爆摇晃节流，不只是 HUD 平移计时器；旧 `RenderTorex` 只有在 `now - shakeTimestamp > 1000` 时才同时设置 `didShake`、刷新 HUD 抖动时间戳并写入玩家 `hurtTime/maxHurtTime/attackedAtYaw`。
- 参考移植版曾把 Torex bob 改成按距离计算 `hurtDuration = 100 - distance`，这不是 1.7.10 行为；现代端保持旧版固定 15 tick 和 yaw 0。
- Forge 1.20.1 的 vanilla overlay 顺序从 `HOTBAR` 后继续复用同一个 `GuiGraphics`，旧版 `GL11.glTranslated` 没有 pop；现代端需要在 GUI 帧结束前 pop，才能既保持本帧 HOTBAR 之后 HUD 一起偏移，又不污染下一帧。
- 旧核闪 overlay 关闭 depth write 后绘制白色加法 quad，绘制后恢复 depth mask。

现代修正：

- `NukeHudEffects#triggerShake` 改为返回是否成功刷新全局 `shakeTimestamp`；`NukeTorexEntity#applyClientShockwaveShake` 只有成功拿到旧版 1 秒节流许可后才写玩家 hurt bob 并设置 `didShake`。
- HUD pose 从 `HOTBAR` Pre 开始 push/translate，改为在 `RenderGuiEvent.Post` 统一 pop；这样 HOTBAR 之后的血量、护甲、经验、物品名和本模组 HUD 与旧版一样处于同一轮平移中，同时避免矩阵跨帧泄漏。
- `NukeHudEffects#renderFlash` 补回旧版核闪的 depth mask false/true 状态恢复。

验证：

- 2026-05-26 ran `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`: passed.

### 2026-05-27 追加：核闪遮罩不随 HUD 冲击波抖动修正

复核来源：

- `com.hbm.main.ModEventHandlerClient`
- `com.hbm.main.ModEventHandlerRenderer`
- Forge 1.20.1 `VanillaGuiOverlay`

问题与旧版对齐：

- 1.7.10 中核闪遮罩在 `RenderGameOverlayEvent.Pre` 的 `CROSSHAIRS` 分支直接绘制全屏白色 quad，HUD 抖动在 `HOTBAR` 分支做 `GL11.glTranslated`；两者是独立效果。
- 现代 Forge 1.20.1 的 `HOTBAR` overlay 顺序早于 `CROSSHAIR`，上一轮为了让 HOTBAR 后续 HUD 同步抖动，把 pose 保留到整帧 `RenderGuiEvent.Post` 才 pop，导致后续 `CROSSHAIR` 上绘制的核闪白屏也被平移，出现白屏跟着冲击波晃动。

现代修正：

- `ClientForgeEvents#renderNukeFlash` 在绘制核闪前检测当前 HUD shake pose；若已 push，则临时 pop 回未平移坐标系，绘制全屏核闪后再恢复 HUD shake pose。
- 这样白屏遮罩始终按屏幕坐标满幅绘制，不随冲击波 HUD 矩阵抖动；后续 HUD/准星仍可继续处于本轮 HUD shake pose 中，直到 GUI Post 统一恢复。

验证：

- 2026-05-27 ran `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`: passed.

### 2026-05-27 追加：Torex 重追踪暖启动、FalloutRain 队列与 crater 冷块自融化

复核来源：

- `com.hbm.entity.effect.EntityNukeTorex`
- `com.hbm.entity.effect.EntityFalloutRain`
- `com.hbm.render.entity.effect.RenderFallout`
- `com.hbm.explosion.ExplosionThermo#scorchDest/#scorchDestLight`
- `com.hbm.world.biome.BiomeGenCraterBase`

问题与旧版对齐：

- 旧 `EntityNukeTorex` 不保存 cloudlet 列表，而是依赖 1000 格追踪范围让客户端持续 tick；现代端重追踪/重载时只拿到 age，没有活跃 cloudlet 历史，所以后续标准云又从地面生成，表现成蘑菇云从根部重新长出。
- 现代端为了满足“退出重进后仍保留核爆云”的额外要求，需要保存 Torex 的 age/scale/type；这和旧 `writeToNBTOptional=false` 不同，但客户端重建必须继续使用 1.7.10 的 cloudlet 生成和运动公式。
- 旧 `EntityFalloutRain` 的生命周期不按固定年龄结束，而是保存并处理 `chunksToProcess` / `outerChunksToProcess` 两个队列；队列未空时 renderer 持续画玩家附近的落尘雨。
- 旧 fallout chunk 收集不是矩形排序，而是按爆心半径做环形角度采样，内圈队列与外圈队列分别 reverse 后从中心向外处理。
- 旧 `RenderFallout` 雨幕本身使用固定 `intensity = 1F`，没有独立的随时间 alpha 曲线；核爆后的“圈层/密度”主要来自服务端队列从中心向外处理，以及 `stomp` 中 `0.1 - (dist / 100 - 0.7)^2` 的 fallout layer 放置概率。
- 旧 `ExplosionThermo` 的 scorch 逻辑会清掉雪/冰并把 packed ice 变成水；用户要求将该热环境契约扩展到 crater biome 自融化，避免核爆群系保留雪冰。

现代修正：

- `NukeTorexEntity` 服务端保存 `ticksExisted`、`scale`、`type`，并在云存活期间按爆炸 chunkloader 基类维持爆心 chunk loading，解决离开区块后服务端 age 冻结、回到早期阶段的问题。
- 客户端 spawn/re-track 时按 1.7.10 完整 cloudlet 生成和运动公式从 tick 1 重放到同步 age，跳过闪光/声音副作用；暖启动 cap 提高到 60000，避免截断仍存活的主干/蘑菇冠 cloudlets。初次客户端生成和暖启动都使用同一实体种子，减少重建形态突变。
- `ExplosionChunkLoadingEntity#readChunkLoader` 不再把存档里的 chunk id 当成当前已 force 的内存状态；世界重载后下一 tick 会重新 force center/work chunk，避免爆炸/降尘实体保存后失去 chunk loading。
- `FalloutRainEntity#gatherChunks` 改回旧版环形 `LinkedHashSet` 队列，保留内圈/外圈处理顺序；队列由旧版一致的首个服务端 tick 收集，并同步总数/剩余内外队列供调试观察。
- `FalloutRainEntity` 每 tick 维持爆心 chunk loading，队列未空时持续存在；每 tick 开头记录 `start`，`tickDelay == 0` 时先重置为 `falloutDelay`，再在 `start + mk5` / 现代 `MK5_BUDGET_MS` 预算内循环处理 chunk，最后同一 tick 末尾执行 `tickDelay--`，对齐旧版默认 4 tick 的推进节奏。renderer 的时间/圈层可见差异来源于服务端内圈/外圈队列进度与旧 fallout layer 放置概率公式，而不是旧 `RenderFallout` 自身的独立强度曲线。
- `CraterRadiationData` 暴露按保存标记并回退 biome key 的 zone 查询；`CommonForgeEvents` 在服务器 tick 中对玩家附近 crater zone 随机采样，雪/雪块/细雪消失，ice 消失，packed/blue/frosted ice 变水。
- 三个 crater biome JSON 移除 `minecraft:freeze_top_layer`，避免热核爆群系继续参与顶层结冰特性。

### 2026-05-27 追加：FalloutRain 存续密度曲线二次对齐

复核来源：

- `com.hbm.entity.effect.EntityFalloutRain#onUpdate`
- `com.hbm.entity.effect.EntityFalloutRain#stomp`
- `com.hbm.render.entity.effect.RenderFallout`

问题与旧版对齐：

- 旧 `RenderFallout` 的 `intensity` 固定为 `1F`，客户端雨幕没有随时间降低密度的函数；降尘雨体感变化来自服务端 fallout entity 的 chunk 队列处理时长。
- 旧 `stomp` 固定扫描 `y=255` 到 `0`。现代端之前按 1.20 世界高度扫描到 `level().getMinBuildHeight()`，在 1.20 默认世界会多扫负 Y 和 256 以上高度，改变每个 chunk 的处理成本，从而改变 `mk5` 毫秒预算下每轮能处理的 chunk 数。
- 旧 `while(System.currentTimeMillis() < start + BombConfig.mk5)` 先检查时间再处理 chunk。现代端之前用 `do/while`，即使预算已耗尽也至少处理一个 chunk，低预算或高负载时会改变实体存续曲线。
- 旧实体在首个服务端 tick 里通过 `firstTick` 收集队列；现代端之前在 `create(...)` 里提前收集队列，虽然 renderer 不再消费进度，但生命周期结构应回到旧版 tick 内初始化。

现代修正：

- `FalloutRainEntity#create(...)` 不再提前 `gatherChunks`，恢复旧版首个服务端 tick 收集队列。
- `FalloutRainEntity` 的处理循环改回旧版 `while(now < start + mk5)` 形态，不再强制每轮至少处理一个 chunk。
- `FalloutRainEntity#stomp` 的高度扫描上限限制为 `min(255, maxBuildHeight - 1)`，避免扫描 1.20 的 256 以上高度；下限映射到现代 `level().getMinBuildHeight()`。旧版 `Y=0` 是当时世界底部，不应在 1.20 中作为绝对下限，否则负 Y 深板岩坑底不会参与 `sellafield_slaked` 等 fallout 转换。

### 2026-05-27 追加：FalloutRain 瞬时消失修正

复核来源：

- `com.hbm.entity.effect.EntityFalloutRain#onUpdate`
- `com.hbm.entity.effect.EntityFalloutRain#gatherChunks`

问题与旧版对齐：

- 旧 `EntityFalloutRain` 没有独立年龄上限，实体存在时间由 `chunksToProcess` / `outerChunksToProcess` 是否处理完决定。
- 现代端配置仍是旧默认值：`falloutRange=100`、`falloutDelay=4`、`mk5BlastTime=50`，不是半径或 delay 被配置为 0。
- 现代端的 block state / biome marker / conversion 实现比 1.7.10 的 `stomp`、`WorldUtil.setBiome`、falling block 与同步路径轻得多；若只按 `while(now < start + mk5)` 时间预算，默认 50ms 内可能把小中型 fallout 队列一次吃完，服务端随即 `discard()`，客户端表现为降尘雨核爆后瞬间消失。
- 1.7.10 的语义是“在 `falloutDelay` 间隔内处理一批 chunk，但受 `mk5` 时间预算限制”，不是“现代硬件能在 50ms 内处理多少就处理多少直到队列空”。

现代修正：

- `FalloutRainEntity` 保留旧 `mk5` 时间预算作为上限，同时增加旧版等效 chunk 工作量上限：默认 `mk5BlastTime=50` 时每个处理周期最多推进 5 个 chunk。
- 队列仍按旧版 `if inner else if outer` 顺序处理，内圈处理完前不会提前消耗外圈；实体仍在队列清空后移除，没有客户端假延长。

验证：

- 2026-05-27 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

### 2026-05-27 追加：Fallout/Torex 粒子透明渲染态对齐

复核来源：

- `com.hbm.render.entity.effect.RenderFallout`
- `com.hbm.render.entity.effect.RenderTorex`
- `assets/hbm/textures/entity/fallout.png`
- `assets/hbm/textures/particle/particle_base.png`

问题与旧版对齐：

- 旧 `fallout.png` 和 `particle_base.png` 贴图 alpha 都是 0/255 二值，不是半透明材质。
- 旧 `RenderFallout` 走固定功能管线，启用 blend 与 alpha func，但没有关闭 depth mask；`fallout.png` 本身只有 0/255 alpha，雨幕透明度来自每列顶点 alpha，而不是贴图半透明。
- 旧 `RenderTorex#cloudletWrapper` 使用 `SRC_ALPHA/ONE_MINUS_SRC_ALPHA`、`AlphaFunc > 0`、禁用 alpha test、`DepthMask(false)`；它不写深度，所以水/流体这类后续透明层仍能透过核爆云出现。但云团贴图本身是二值 alpha，旧画面不会把背景 vanilla 云层作为同等透明对象保留下来。
- 现代初版的 Torex 云团完全走透明 RenderType，背景云层和流体都会通过同一透明混合显现，和 1.7.10 观感不一致。
- 1.20.1 `LevelRenderer` 的顺序和 1.7.10 固定管线不同：实体阶段早于 `RenderType.translucent()` 水/透明方块，vanilla 云层和天气还在后续阶段绘制。若 Torex 在普通实体阶段先画，后续水面/云层会再次叠到蘑菇云上，表现成“透过粒子看到的水和云更亮”。
- 曾尝试独立 depth prepass，但预写会先把最近 cloudlet 深度写入，随后的颜色 pass 只能画最前一层，反而削弱多层云团叠加厚度；也会带来硬遮罩观感。

现代修正：

- `FalloutRainRenderer` 恢复旧 `RenderFallout` 的连续雨幕公式：使用 `RenderType.entityTranslucent(fallout.png)`、每个玩家周围格子都绘制一张雨幕 quad，顶点 alpha 使用 `((1 - distanceMod^2) * 0.3 + 0.5)`；不再用 deterministic column coverage 整列跳过，避免表现成一根根烟柱。
- renderer 不再使用 `FalloutRainEntity#getFalloutProgress()` 或爆心半径函数做时间/圈层渐隐；旧版 renderer 的 `intensity` 固定为 `1F`，降尘随时间结束由服务端 `chunksToProcess` / `outerChunksToProcess` 队列消耗和实体移除决定。
- `NukeTorexRenderer` 的 cloudlet pass 从普通实体阶段移到 Forge `RenderLevelStageEvent.Stage.AFTER_LEVEL`：水/透明方块、普通粒子、天气和 vanilla 云层都先画进 color buffer，再按旧版 far-to-near 排序把蘑菇云 alpha 混合到现有画面上。
- 不能使用 `AFTER_WEATHER`：1.20.1 的该阶段仍处于 vanilla 云/天气渲染的 model-view 矩阵上下文中，Torex 又按 `origin - camera` 做相机相对平移，会导致 cloudlet 被云层矩阵二次影响，表现为整团粒子挂到天空并随视角移动。
- `AFTER_LEVEL` 不能直接复用 `event.getPoseStack()`：Forge 47.2.32 在 `GameRenderer#renderLevel` 中给该阶段传入的是投影/FOV 栈 `posestack`，不是 `LevelRenderer#renderLevel` 内部带相机 pitch/yaw 的世界视图栈。把 `origin - camera` 套在这个栈上会让 cloudlet 漂到摄像机后方。
- `ClientForgeEvents` 因此为 after-level Torex pass 创建一条干净的世界视图 `PoseStack`，按 `camera.getXRot()` 与 `camera.getYRot() + 180` 重建 vanilla 实体渲染使用的相机旋转，再交给 `NukeTorexRenderer` 做相机相对坐标渲染。
- cloudlet RenderType 保留 `SRC_ALPHA/ONE_MINUS_SRC_ALPHA` 透明混合和二值贴图 discard，并保留 color+depth write。这样水面和 vanilla 云层都作为已绘制背景被蘑菇云半透明压住；depth write 只用于避免再后续的世界 pass 穿过云团，而不是用来把云层硬挡掉。
- Flare 仍保留旧加法透明、无 depth write 路径。

验证：

- 2026-05-27 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.

### 2026-05-27 追加：Torex 初期中心黄色 cloudlet 淡出对齐

复核来源：

- `com.hbm.render.entity.effect.RenderTorex#tessellateCloudlet`
- `com.hbm.entity.effect.EntityNukeTorex#getAlpha`
- `com.hbm.entity.effect.EntityNukeTorex.Cloudlet#getAlpha`

问题与旧版对齐：

- 1.7.10 的 `Cloudlet#getAlpha()` 使用 `(1F - age / cloudletLife) * EntityNukeTorex#getAlpha()`，冷凝 cloudlet 额外乘 `0.25`。
- 1.7.10 的 `RenderTorex#tessellateCloudlet` 将 `cloud.getAlpha()` 直接传给 `setColorRGBA_F`，没有额外放大或重新映射淡出曲线。
- 现代端 `NukeTorexRenderer#renderBillboard` 曾将 alpha 做 `alpha * 2.5F` 后 clamp，导致初期中心黄白 cloudlet 在较长时间内被顶到满不透明，原始 alpha 低于约 0.4 后才开始明显淡出，观感上更像突然收束消失。

现代修正：

- `NukeTorexRenderer#renderBillboard` 改为直接使用 `cloudlet.getAlpha()` 输出顶点 alpha，保留旧版 cloudlet 生命周期、实体整体 fade-out、冷凝 `0.25` alpha 和颜色/亮度公式。

验证：

- 2026-05-27 ran `.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`: passed.

### 2026-05-28 追加：MK5 explosionAlgorithm 配置接线工况修正

复核来源：

- `com.hbm.config.BombConfig`
- `com.hbm.explosion.ExplosionNukeRayParallelized`
- `com.hbm.explosion.ExplosionNukeRayBatched`
- `com.hbm.entity.logic.EntityNukeExplosionMK5`

问题与旧版对齐：

- 1.7.10 保留 `explosionAlgorithm` 配置，`1` / `2` 指向 threaded DDA 入口，`0` 指向旧批处理射线入口。
- 现代端已经迁入 `BombConfig.EXPLOSION_ALGORITHM`，注释说明 threaded 模式安全委托到 batched worker，但 `NukeExplosionMk5Entity` 仍固定创建 `ExplosionNukeRayBatched`，配置值没有进入调度路径。
- 现代 `ExplosionNukeRayParallelized` 是安全兼容入口，不复刻 1.7.10 worker-thread 直接读写 chunk 的危险实现；但它必须保留 MK5 forced chunk loader 注入，否则后续按配置选择该入口会丢失已迁入的 chunk loading 语义。

现代修正：

- `ExplosionNukeRayParallelized` 改为继承 `ExplosionNukeRayBatched`，保留旧构造签名，并新增带 `BiConsumer<Integer, Integer>` chunk-loader 的构造。
- `NukeExplosionMk5Entity` 新增 `createExplosionWorker(...)`，`explosionAlgorithm == 1 || 2` 时创建 `ExplosionNukeRayParallelized`，`0` 时创建 `ExplosionNukeRayBatched`。
- 两条路径仍共享 batched 的 NBT 保存/读取、`cacheChunksTick` / `destructionTick` 预算推进和强制加载回调；这次只接通旧配置契约，不恢复旧线程直接改 chunk 的高风险实现。

验证：

- 2026-05-28 attempted `.\gradlew.bat compileJava processResources --no-daemon`: online dependency resolution failed to resolve `org.parchmentmc.librarian.forgegradle:1.+` from configured repositories.
- 2026-05-28 attempted `.\gradlew.bat compileJava processResources --no-daemon --offline`: reached `compileJava`, then failed on pre-existing `QuasarEntity` reference to missing `ModEntityTypes.QUASAR`; no MK5/explosion compile error surfaced before that blocker.

### 2026-05-28 追加：waste schrabidium 变异概率配置接回

复核来源：

- `com.hbm.explosion.ExplosionNukeGeneric#wasteDest`
- `com.hbm.config.VersatileConfig#getSchrabOreChance`
- `com.hbm.config.GeneralConfig#enableLBSM`
- `com.hbm.config.GeneralConfig#schrabRate`

问题与旧版对齐：

- 1.7.10 的核爆 waste 后处理把 `ore_uranium` / `ore_nether_uranium` / `ore_gneiss_uranium` 转为对应 schrabidium 矿时，概率来自 `VersatileConfig.getSchrabOreChance()`。
- 旧 `getSchrabOreChance()` 在普通模式返回 `100`；开启 Less Bullshit Mode 时返回 `GeneralConfig.schrabRate`，旧配置项 `LBSM_schrabOreRate` 默认 `20`。
- 现代端此前固定使用 `nextInt(10) == 1`，使普通核爆 waste 生成 schrabidium 的概率变为 1/10，明显高于旧默认 1/100。

现代修正：

- `BombConfig` 新增 `lessBullshitMode.schrabOreRate`，默认 `20`，对应旧 `LBSM_schrabOreRate`。
- `ExplosionNukeGeneric` 新增 `schrabOreChance()` helper：普通模式返回 `100`；`RadiationConfig.ENABLE_LESS_BULLSHIT_MODE` 开启且配置已初始化时返回 `BombConfig.LBSM_SCHRAB_ORE_RATE`。
- 三类 uranium waste 转换都改用该概率；`wasteNoSchrab` 仍保持不生成 schrabidium 的旧语义。

## 验证清单

- 普通爆炸不会在客户端修改世界。
- 方块处理、实体处理、掉落处理可独立组合。
- 高强度爆炸分批执行不阻塞主线程过久。
- 客户端效果 packet 解码后只生成表现，不重复破坏世界。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `ExplosionFleija`、`ExplosionNukeRayBatched`、`ExplosionNukeRayBalefire` 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `ExplosionChaos` 安全子集、`ExplosionTom`、`hbm:pc` / `hbm:cloud` damage type 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在补回 `hbm:taint` effect/damage type 与 `ExplosionChaos.c` 的 taint -> mutation 联动后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `BlockAllocatorGlyphidDig`、`ExplosionNukeRayParallelized` 兼容入口后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `NukeExplosionMk5Entity` 核心调度实体和实体注册后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `BombConfig` 子集并接通 `ExplosionNukeSmall` 大核弹 -> `NukeExplosionMk5Entity` 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `FalloutRainEntity` 安全子集并接通 MK5 完成后的落尘实体后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `NukeExplosionMk3Entity` 核心调度实体和实体注册后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在补齐 MK3/MK5 旧 `statFac` 入口、`BombConfig.CHUNK_LOADING` forced chunk 开关与 `LIMIT_EXPLOSION_LIFESPAN` 存档过期检查后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在新增 `NuclearExplosionUtil` 核爆调度门面，并将 `CustomNukeExplosion` / `CustomMissileExplosion` / `ExplosionNukeSmall` 改接到统一入口后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在扩展 `NuclearExplosionUtil` 核弹方块入口、添加 `NuclearDeviceBlock` 并替换九个核装置占位方块类型后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在补齐 `ExplosionLarge` / `ExplosionNT` 旧调用签名兼容入口后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `EntityExplosionChunkloading` 现代 forced chunk 基类并接入 MK3/MK5/FalloutRain 后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `ExplosionNT` 的 `DIGAMMA` / `DIGAMMA_CIRCUIT` / `LAVA_V` / `LAVA_R` 方块落点后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `EntityShrapnel` / `EntityRubble`、注册 damage type 并接回 `ExplosionLarge` 实体生成入口后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在补齐 `EntityShrapnel` / `EntityRubble` 飞行 renderer，并迁入 `hbm:balefire` 方块与 `BlockMutatorBalefire` 旧 ID 落点后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在迁入 `EntityBalefire` 现代调度实体、实体注册与客户端空渲染后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在新增 `WeaponExplosionUtil.spawnBalefire`、`BalefireBombBlock`、`nuke_fstbmb` 红石/火焰触发骨架和旧 FSTBMB 声音资源后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在新增 `CustomNukeExplosion`、`FallingNukeEntity`、实体注册与客户端空渲染后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在新增 `CustomMissileExplosion` 默认弹头 impact 工具后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在补回 VNT 标准 affected-block 客户端烟尘效果桥后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在对齐 VNT Tiny 旧声音/vanillaExt 粒子并新增 smooth tiny 公共入口后通过。
- `.\gradlew.bat compileJava processResources --no-daemon` 在将 `ExplosionEffectWeapon` 改回 `explosionSmall` aux 粒子路径后通过。
- `.\gradlew.bat compileJava processResources --no-daemon --offline` 在接通 MK5 `explosionAlgorithm` 配置后进入 Java 编译，但被既有 `QuasarEntity` / `ModEntityTypes.QUASAR` 缺口阻断，待 Quasar 注册修复后复跑。
- `.\gradlew.bat compileJava processResources --no-daemon --offline` 在注册 `entity_digamma_quasar`、修复 `SingularityItem` tooltip mutable component、接回 waste schrabidium 概率配置后通过。
