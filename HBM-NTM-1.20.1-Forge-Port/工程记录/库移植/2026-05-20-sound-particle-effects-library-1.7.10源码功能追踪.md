# 声音与粒子效果库 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 客户端声音循环、动态音效、粒子工具和效果 packet。
- 该库服务机器、武器、爆炸、污染、导弹、载具和 HUD 表现。

## 1.7.10 源文件

- `src/main/java/com/hbm/sound`
  - `AudioWrapper.java`: common 侧空实现，给服务端/非客户端调用保留同一 API。
  - `AudioWrapperClient.java`: 客户端包装 `AudioDynamic`，代理位置、实体绑定、音量、范围、音调、keep-alive、播放/停止。
  - `AudioDynamic.java`: 旧 `MovingSound` 循环声音；默认重复播放、无 vanilla attenuation；按玩家距离和自定义 range 线性衰减；可绑定实体；`keepAlive` 超时后停止。
  - `SoundLoopMachine.java`: 绑定 TileEntity 的循环声；TileEntity invalid 时停止。
  - `SoundLoopSiren.java`: siren 专用循环声；全局 list 按 TileEntity 去重；按 cassette track 的 loop/volume/path 更新。
- `src/main/java/com/hbm/particle`
- `src/main/java/com/hbm/util/ParticleUtil.java`
- `src/main/java/com/hbm/handler/EntityEffectHandler.java`
- `src/main/java/com/hbm/packet/toclient/ParticleBurstPacket.java`
- `src/main/java/com/hbm/packet/toclient/AuxParticlePacketNT.java`
- `src/main/java/com/hbm/packet/toclient/TESirenPacket.java`
- 资源：`src/main/resources/assets/hbm/sounds`
- 声音声明：旧 `sounds.json` 或注册入口需后续核对。

## 1.7.10 声音资源合同

- 旧资源目录有约 512 个 `.ogg`，根路径为 `assets/hbm/sounds`。
- 旧 `sounds.json` 使用事件名到资源路径的映射，典型事件名：
  - `block.pressOperate` -> `hbm:block/pressOperate`
  - `tool.geiger1..6` -> `hbm:tool/geiger1..6`
  - `tool.techBoop`、`tool.radaway`
  - `player.vomit`
  - 大量 `weapon.*`、`alarm.*`、`entity.*`、`block.*` 事件供后续机器、武器、实体逐批接入。
- 干净 1.20.1 工程已采用 snake_case 现代资源名，例如 `block.press_operate`、`tool.tech_boop`，并已复制少量资源到 `assets/hbm/sounds/...`。后续新增声音时需要同时登记 `ModSounds` 与 `sounds.json`，资源文件优先从 1.7.10 旧资源复制并按现代命名落位。

## 1.7.10 粒子与 packet 合同

- `AuxParticlePacketNT`:
  - 载荷是 NBT，构造时写入 `posX/posY/posZ`。
  - 客户端 handler 调 `MainRegistry.proxy.effectNT(nbt)`，由 proxy/client effect 分发具体粒子。
  - 旧 packet 释放 ByteBuf 的做法属于 1.7.10 threading 包遗留，现代实现不保留该细节。
- `ParticleBurstPacket`:
  - 载荷是方块坐标、旧方块数字 ID、metadata。
  - 客户端调用 `effectRenderer.addBlockDestroyEffects(...)` 生成方块破坏粒子。
  - 现代迁移改为传 `BlockState` registry id，客户端调用 `ParticleEngine.destroy(pos, state)`。
- `ParticleUtil`:
  - `spawnGasFlame`: NBT `type=gasfire`，携带 `mX/mY/mZ`；服务端发 150 格范围包，客户端直接走 effectNT。
  - `spawnDroneLine`: NBT `type=debugdrone`，携带目标坐标 `mX/mY/mZ` 与 `color`；服务端发 150 格范围包。

## 旧版契约

- `sound` 目录包含：
  - 机器循环声音。
  - siren 循环声音。
  - 载具/实体移动声音。
  - 客户端 audio wrapper。
- `particle` 目录包含：
  - 自定义粒子实体/渲染效果。
  - 弹壳、烟雾、爆炸、辐射/污染相关表现。
- `ParticleUtil` 是服务端触发客户端粒子包或客户端直接生成效果的辅助。
- `AuxParticlePacketNT` 使用 NBT 传输粒子参数。
- `TESirenPacket` 同步 tile entity siren 循环声状态。

## 迁移计划

- 声音事件迁移到现代 `SoundEvent` 注册和 `sounds.json`。
- 循环声音迁移为客户端 tickable sound，服务端只同步状态。
- 粒子迁移为 `ParticleType` + provider；复杂参数用自定义 packet 或 particle options。
- 不在服务端引用客户端声音/粒子类。

## 验证清单

- 机器关闭后循环声音停止。
- siren 远离/卸载后不会残留声音。
- 粒子 packet 在客户端只生成表现。
- 声音资源路径与旧资源一致。

## 2026-05-21 首批迁移实现

- 新增现代公共 API：
  - `src/main/java/com/hbm/ntm/sound/AudioWrapper.java`
  - `src/main/java/com/hbm/ntm/particle/ParticleUtil.java`
  - `src/main/java/com/hbm/ntm/particle/ClientParticleBridge.java`
- 新增现代客户端实现：
  - `src/main/java/com/hbm/ntm/client/sound/ClientAudioWrapper.java`
  - `src/main/java/com/hbm/ntm/client/sound/HbmDynamicSound.java`
  - `src/main/java/com/hbm/ntm/client/sound/SoundLoopMachine.java`
  - `src/main/java/com/hbm/ntm/client/particle/HbmParticleEffects.java`
- 新增现代网络包并接入 `ModMessages`：
  - `AuxParticlePacket`: server -> client，传 `CompoundTag`，客户端通过 `ClientParticleBridge.handleAux` 分发。
  - `ParticleBurstPacket`: server -> client，传 `BlockPos` + `BlockState` registry id，客户端生成方块破坏粒子。
  - `ModMessages.sendToTracking(...)`: 封装 `PacketDistributor.NEAR`，供粒子工具按范围发送。
- 旧 `AudioWrapper` 合同已迁出 common 侧无客户端依赖接口；客户端动态循环声用现代 `SoundManager` 与 `TickableSoundInstance` 实现。
- `HbmDynamicSound` 保留旧版关键语义：默认 loop、无 vanilla attenuation、自定义 range 线性衰减、可绑定实体、可 keep-alive 超时停止、支持音量/音调更新。
- `SoundLoopMachine` 保留旧版 TileEntity invalid 停止语义，对现代 `BlockEntity.isRemoved()` 停止。
- `HbmParticleEffects` 目前只提供最小安全映射：
  - `gasfire`: 暂用 vanilla `FLAME` + `SMOKE` 表现，等待旧 `ParticleGasFlame` 深迁。
  - `debugdrone`: 暂用 `END_ROD` 点列表示调试线，等待旧 `ParticleDebugLine` 深迁。
  - `radiationfog/radiation_fog`: 使用已迁移的 `ModParticleTypes.RADIATION_FOG`。
- 本批未迁移：
  - `SoundLoopSiren` 的 cassette track 表、siren 去重列表和 `TESirenPacket`，因为 siren 机器/磁带物品尚未进入本批。
  - 旧 `particle` 目录下 50+ 自定义粒子类与 `EntityEffectHandler.effectNT` 全量分发表。
  - 500+ 声音事件完整注册；后续应按机器、武器、实体迁移切片逐批登记。

## 2026-05-21 验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-21 第二批推进

- 继续追踪 1.7.10 `ClientProxy.effectNT(NBTTagCompound)`，旧分发顺序：
  - 先查 `ParticleCreators.particleCreators`，包含 `explosionLarge`、`casingNT`、`flamethrower`、`explosionSmall`、`blackPowder`、`ashes`、`skeleton`。
  - 再处理旧 MK1/MK2 与通用 NBT 类型：`waterSplash`、`cloudFX2`、`ABMContrail`、`launchSmoke`、`exKerosene`、`exSolid`、`exHydrogen`、`exBalefire`、`radFog`、`missileContrail`、`smoke`、`exhaust`、`fireworks`、`vanillaburst`、`vanillaExt`、`vanilla`、`jetpack*` 等。
- 本批在 `HbmParticleEffects` 中补充旧 NBT 协议的现代安全映射：
  - `vanilla`: 按 `mode/mX/mY/mZ` 映射到现代 vanilla 粒子。
  - `vanillaburst`: 按 `mode/count/motion` 批量生成带随机速度的 vanilla 粒子。
  - `vanillaExt`: 支持 `flame`、`smoke`、`cloud`、`reddust`、`bluedust`、`greendust`、`fireworks`、`largeexplode`、`townaura`、`blockdust`、`colordust` 的近似映射。
  - `smoke`: 支持 `cloud`、`radial`、`radialDigamma`、`shock`、`shockRand`、`wave`、`foamSplash` 的近似映射。
  - `launchSmoke`、`missileContrail`、`ABMContrail`、`exKerosene`、`exSolid`、`exHydrogen`、`exBalefire`、`exhaust`。
  - `flamethrower`: 根据旧 `meta` 选择 flame/soul flame/witch/smoke 近似表现。
  - `sweat`、`vomit`: 按旧 `entity/count/block/mode` 在实体附近生成 block/slime/smoke 粒子。
  - `waterSplash`、`cloudFX2`、`radFog`。
- 重要边界：
  - 这些新增类型是“协议可用 + 视觉近似”的桥接层，不等价于旧版专用粒子完整移植。
  - 旧 `ParticleContrail`、`ParticleSmokePlume`、`ParticleExSmoke`、`ParticleFoam`、`ParticleFlamethrower`、`ParticleExplosionSmall` 等仍需后续按旧贴图与渲染行为深迁。
  - 旧 block 数字 ID 在现代世界里只能近似解读为现代 `BlockState` registry id；用于临时表现可以接受，老存档/旧 packet 完全兼容不在本批范围。

## 2026-05-21 第二批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-21 第三批推进

- 从 1.7.10 资源复制到干净工程：
  - `assets/hbm/textures/particle/particle_base.png`
  - `assets/hbm/textures/particle/contrail.png`
- 新增现代粒子类型注册：
  - `rocket_flame`
  - `contrail`
  - `launch_smoke`
  - `ex_smoke`
  - `foam`
  - `flamethrower`
- 新增粒子图集 JSON：
  - `assets/hbm/particles/rocket_flame.json` -> `particle_base`
  - `assets/hbm/particles/contrail.json` -> `contrail`
  - `assets/hbm/particles/launch_smoke.json` -> `contrail`
  - `assets/hbm/particles/ex_smoke.json` -> `particle_base`
  - `assets/hbm/particles/foam.json` -> `particle_base`
  - `assets/hbm/particles/flamethrower.json` -> `particle_base`
- 新增现代 provider/particle 类：
  - `RocketFlameParticle`: 对应旧 `ParticleRocketFlame` 的基础语义，保留长寿命、逐渐扩散、火焰色衰减和透明衰减。
  - `HbmSmokeParticle`: 承载 `ex_smoke`、`contrail`、`launch_smoke` 三类轻量变体。
  - `FoamParticle`: 对应旧 `ParticleFoam` 的泡沫/喷溅方向，保留上抛、漂浮、衰减和白色泡沫表现。
  - `FlamethrowerParticle`: 对应旧 `ParticleFlamethrower` 的基础火焰色与上升/衰减语义。
- `HbmParticleEffects` 的分发升级：
  - `launchSmoke` 由 vanilla `CAMPFIRE_SIGNAL_SMOKE` 改为 `hbm:launch_smoke`。
  - `missileContrail` 由 vanilla smoke/flame 改为 `hbm:rocket_flame` + `hbm:contrail`。
  - `ABMContrail`、`exKerosene`、`exSolid`、`exHydrogen`、`exBalefire` 主体改为 `hbm:contrail`；固体/氢/恶火仍追加一层现代 dust tint 近似旧色。
  - `smoke.cloud/radial/shock/wave` 改为 `hbm:ex_smoke`。
  - `smoke.foamSplash` 改为 `hbm:foam`。
  - `flamethrower` 默认火焰改为 `hbm:flamethrower`；balefire/digamma/black 仍暂用 vanilla/smoke 近似，待自定义参数粒子或更多类型拆分。
- 边界：
  - 本批仍未实现旧粒子的自定义多 billboard 渲染、完整旋转、trail list 与 GL 状态；现代实现使用 `TextureSheetParticle` 的 provider 体系，先保证注册、贴图、生命周期与主要运动/颜色合同。
  - `ParticleContrail` 旧版每个粒子渲染 6 个 quad，`ParticleRocketFlame` 旧版每个粒子渲染 10 个 quad；现代轻量实现暂为单 quad 动态扩散。

## 2026-05-21 第三批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-22 第四批推进：ParticleCreators 剩余入口批量桥接

- 1.7.10 对照：
  - `ParticleCreators.particleCreators` 在 `ClientProxy#effectNT` 的第一层分发中优先处理：
    - `explosionLarge` -> `ExplosionCreator`
    - `explosionSmall` -> `ExplosionSmallCreator`
    - `blackPowder` -> `BlackPowderCreator`
    - `ashes` -> `AshesCreator`
    - `casingNT` -> `CasingCreator`
    - `skeleton` -> `SkeletonCreator`
    - `flamethrower` 此前已接入现代 `FlamethrowerParticle`
  - 旧 `ExplosionCreator` 会生成冲击波、火焰烟柱、方块碎屑并按距离延迟播放爆炸声音。
  - 旧 `BlackPowderCreator` 按 `hX/hY/hZ` 方向生成黑火药烟与 fullbright 火花。
  - 旧 `AshesCreator` / `SkeletonCreator` 会调用 `ClientProxy.vanish(entityID)` 隐藏实体，并生成灰烬或骨骼分件粒子。
  - 旧 `CasingCreator` 生成带模型和可选烟节点的弹壳粒子。
- 本批现代迁移：
  - 新增 `ModParticleTypes.BLACK_POWDER_SPARK` 与 `BlackPowderSparkParticle`，使用旧 `particle_base` 图集，保留短寿命、暖色 fullbright、重力和摩擦语义。
  - 新增 `assets/hbm/particles/black_powder_spark.json`。
  - `ClientModEvents#registerParticleProviders` 注册黑火药火花 provider。
  - `ParticleUtil` 增加 common 侧 helper/常量：
    - `spawnExplosionLarge`
    - `spawnExplosionSmall`
    - `spawnBlackPowder`
    - `spawnAshes`
    - `spawnCasing`
    - `spawnSkeleton`
  - `HbmParticleEffects` 增加旧 NBT type 的现代分发：
    - `explosionLarge`：`EXPLOSION_EMITTER` + 环形冲击烟 + `rocket_flame`/`ex_smoke` 烟柱 + 邻近方块碎屑。
    - `explosionSmall`：小型爆炸核心 + `rocket_flame`/`ex_smoke` + 邻近方块碎屑。
    - `blackPowder`：按旧 heading 归一化后生成 `ex_smoke` 与 `black_powder_spark`。
    - `ashes`：按实体或坐标周围生成 `ASH` + 少量火焰。
    - `casingNT`：暂以火花与可选烟表现弹壳喷出，不生成旧 3D 弹壳实体。
    - `skeleton`：暂以 `BONE_BLOCK` block particle 表现骨骼化/碎骨，不生成旧 `ParticleSkeleton` 模型分件。
- 边界：
  - 旧 `ParticleDebris` 的 `WorldInAJar` 碎块实体、`ParticleMukeWave` 的水平加色 shockwave quad、`ParticleSpentCasing` 的 3D 弹壳模型、`ParticleSkeleton` 的骨骼模型分件均未完整深迁；本批是“旧 NBT 协议可消费 + 视觉近似”的大面积桥接。
  - 旧爆炸声音延迟播放未在本批接入，后续应通过声音库/`ModSounds` 单独补齐。
  - `ClientProxy.vanish(entityID)` 的实体隐藏语义未迁移，避免在纯粒子库里直接改现代实体可见性状态。

## 2026-05-22 第四批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-23 SCHRAB 粒子形态修正

- 旧版对照：
  - `com.hbm.blocks.fluid.SchrabidicBlock#randomDisplayTick`
  - `com.hbm.blocks.generic.BlockHazard#randomDisplayTick`
  - `com.hbm.main.ClientProxy#effectNT`
- 1.7.10 结论：
  - `type="schrabfog"` 走的是单个 `EntityAuraFX`，颜色为青蓝色，属于小型 aura 粒子，不是整团雾幕。
  - `type="radFog"` 走的是 `ParticleRadiationFog`，同样不应与 `schrabfog` 混用。
- 现代修正：
  - `SchrabFogParticle` 已从多 quad 大雾改为 `EntityAuraFX` 等价的小型青蓝 aura 粒子。
  - 2026-05-24 三次修正：`schrab_fog.json` 不再引用雾状 `haze` 或 HBM 的多点 `particle_base` 贴图，改用 Minecraft 自带单像素 `minecraft:generic_0`。
  - 2026-05-24 四次修正：按 1.7.10 `EntityAuraFX` 精确对齐核心参数：`setSize(0.02, 0.02)`，scale 乘 `rand * 0.6 + 0.5`，super 随机速度再乘 `0.02`，寿命 `20 / (rand * 0.8 + 0.2)`，tick 摩擦 `0.99`；去掉之前额外加入的线性 alpha 淡出和速度叠加。
  - `LegacyHazardSourceBlock` 对 `SCHRAB` 的外露面坐标生成恢复为旧 `BlockHazard#randomDisplayTick` 的方向面偏移逻辑，实际表现应为方块放置后自带的小颗粒，不是辐射雾。
- 边界：
  - 本次只修 `SCHRAB` 显示形态，不改辐射数值和伤害。
  - `radiation_fog` 仍单独保留为区块辐射雾效，后续如仍显得过大再按旧版 `ParticleRadiationFog` 继续收敛。

## 2026-05-24 第五批推进：gasfire 与烟柱底层粒子

- 1.7.10 对照：
  - `ParticleGasFlame`：
    - 继承旧 `EntitySmokeFX`，构造时 `motionY * 1.5`，默认 scale 由 `ClientProxy#effectNT` 在 `scale <= 0` 时设为 `6.5F`。
    - 寿命 `30 + rand.nextInt(13)`，`noClip=true`，fullbright。
    - 每 tick 保留上一帧 Y 速度，再 `motionX/Z *= 0.75`、`motionY += 0.005`。
    - 颜色按年龄从黄/橙向暗色过渡，并乘 `0.8F..1.0F` 随机色偏。
  - `ParticleSmokePlume`：
    - 使用 `textures/particle/contrail.png`，寿命 `80..99`。
    - 每个粒子渲染 6 个随机偏移 quad，颜色为灰阶随机值，fullbright，透明度随年龄线性下降。
    - scale 从 `0.25F` 增长到约 `2.25F`，移动时额外叠加 scale 增量形成上涌烟柱感。
  - 旧调用点：
    - `ParticleUtil.spawnGasFlame` 被爆炸火焰喷发、矿工火箭、飞机燃烧、炼油厂、gas flare、RBMK 棒、湮灭机、流体罐、turbofan、deco geysir、PartEmitter 等复用。
    - `launchSmoke` 在 `ClientProxy#effectNT` 中生成 `ParticleSmokePlume` 并读取 `moX/moY/moZ`。
- 本批现代迁移：
  - 新增粒子类型：
    - `ModParticleTypes.GAS_FLAME`
    - `ModParticleTypes.SMOKE_PLUME`
  - 新增 provider/particle：
    - `GasFlameParticle`：使用旧 `particle_base` 图集，保留 gasfire 的 fullbright、寿命、Y 速度保持、上升、X/Z 衰减、颜色曲线与默认 `6.5F` scale。
    - `SmokePlumeParticle`：使用旧 `contrail` 图集，手写 6 个 billboard quad，保留旧 `ParticleSmokePlume` 的多 quad、随机灰阶、fullbright、scale 增长、透明衰减与上涌运动。
  - 新增粒子图集 JSON：
    - `assets/hbm/particles/gas_flame.json`
    - `assets/hbm/particles/smoke_plume.json`
  - `ClientModEvents#registerParticleProviders` 注册两个 provider。
  - `HbmParticleEffects` 分发更新：
    - `type=gasfire` 从 vanilla `FLAME + SMOKE` 桥接改为 `hbm:gas_flame`。
    - `type=launchSmoke` 从轻量 `launch_smoke` 改为 `hbm:smoke_plume`，并保留 `moX/moY/moZ`。
    - `type=ABMContrail` 改走 `hbm:smoke_plume`，避免继续和普通 `contrail` 混在一起。
- 边界：
  - `gasfire` 的少量旧调用会传 `scale` NBT；当前现代 `SimpleParticleType` provider 固定旧默认 `6.5F`。如后续发现依赖动态 scale 的调用，需要补自定义 `ParticleOptions` 或在 Aux 分发中建立客户端 sprite 工厂。
  - `SmokePlumeParticle` 使用现代 particle atlas 与 `TextureSheetParticle`，不是旧 GL11 直接绑定纹理；多 quad/亮度/透明/运动已按旧语义对齐。

## 2026-05-24 第五批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-24 第六批推进：调试线与喷火器 meta 变体深迁

- 1.7.10 对照：
  - `ParticleDebugLine`：
    - `motionX/Y/Z` 不是速度，而是从粒子起点延伸出去的线段向量。
    - 寿命 60 tick，`getFXLayer()=3`，渲染时禁用纹理/光照/深度测试，直接画 `GL_LINES`。
    - 颜色使用 NBT `color`，亮度/可见性随年龄衰减。
  - `ClientProxy#effectNT`：
    - `debugline` 无额外门槛，直接生成 `ParticleDebugLine`。
    - `debugdrone` 在旧版会检查玩家是否持有无人机/无人机网络相关物品，再生成同一个 `ParticleDebugLine`。
  - `FlameCreator` / `ParticleFlamethrower`：
    - `meta=0` 普通火焰、`1` balefire、`2` digamma、`3` oxy、`4` black。
    - 寿命 `20..29`，初始 X/Z 高斯扰动 `0.02`，每 tick 速度乘 `0.91`、Y 速度加 `0.01`、旋转。
    - 普通/balefire/digamma 使用 HSB 初始色区间；oxy/black 从白色按各自曲线衰减；fullbright。
- 本批现代迁移：
  - 新增 `DebugLineParticle`：
    - 使用现代 `ParticleRenderType` 的 `LINES + POSITION_COLOR` 渲染路径。
    - 保留旧版 60 tick、无深度测试、无贴图、线段向量、颜色和透明度衰减语义。
    - `HbmParticleEffects` 同时支持旧 `type=debugline` 与 `type=debugdrone`。
  - `ParticleUtil` 增加 `TYPE_DEBUG_LINE` 与 `spawnDebugLine(...)` helper，并把 `spawnDroneLine(...)` 参数命名收敛为旧版线段向量语义。
  - `HbmEnergyDebug` 修正 `debugdrone` 载荷：`mX/mY/mZ` 改回线段向量，而不是世界终点坐标。
  - `FlamethrowerParticle` 深化为带 `legacyType` 的旧版 meta 实现：
    - 保留普通、balefire、digamma、oxy、black 的颜色曲线、透明度、fullbright、上升与旋转。
    - 新增粒子类型与图集：
      - `flamethrower_balefire`
      - `flamethrower_digamma`
      - `flamethrower_oxy`
      - `flamethrower_black`
    - `HbmParticleEffects#spawnFlamethrower` 按旧 `meta` 选择 HBM 自定义粒子，不再把 balefire/digamma/black 映射到 vanilla 粒子。
- 边界：
  - 现代端暂不复刻旧 `debugdrone` 的“玩家必须手持无人机相关物品”门槛，因为干净 port 目前尚未迁入对应无人机物品/方块；当前调试线库保持 packet 可见性，由调用方决定是否发送。
  - `ParticleFlamethrower` 仍使用现代单 quad `TextureSheetParticle`，未迁入旧 `EntityFXRotating#renderParticleRotated` 的完全等价顶点路径；运动、颜色、透明度、亮度和 meta 分发表已对齐。

## 2026-05-24 第六批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-24 第七批推进：ParticleCreators 贴图粒子深迁

- 1.7.10 对照：
  - `ExplosionSmallCreator`：
    - 旧 NBT `type=explosionSmall`，字段 `cloudCount/cloudScale/cloudSpeedMult/debris`。
    - 每个云粒子生成 `ParticleExplosionSmall(world, x, y, z, cloudScale, cloudSpeedMult)`。
    - `ParticleExplosionSmall` 使用 `particle_base`，寿命 `25..34`，scale 为 `cloudScale * 0.9 + rand * 0.2`，X/Z 高斯速度乘 `cloudSpeedMult`，随机负重力形成上飘，X/Z 速度每 tick 乘 `0.65`。
    - 颜色 HSB hue `20..40`，随年龄降低饱和度/亮度，透明度 `pow(1-age, 0.25) * 0.5`，scale 随年龄膨胀。
  - `BlackPowderCreator`：
    - 旧 NBT `type=blackPowder`，字段 `hX/hY/hZ/cloudCount/cloudScale/cloudSpeedMult/sparkCount/sparkSpeedMult`。
    - 先归一化 heading；烟粒子为 `ParticleBlackPowderSmoke`，速度沿 heading 加随机扰动；火花为 `ParticleBlackPowderSpark`。
    - `ParticleBlackPowderSmoke` 使用 `particle_base`，寿命 `30..44`，scale 为 `cloudScale * 0.9 + rand * 0.2`，速度每 tick 全轴乘 `0.65`，颜色从橙色向淡烟色衰减，透明度 `pow(1-age, 0.25) * 0.25`。
  - `AshesCreator`：
    - 旧 NBT `type=ashes`，字段 `entityID/ashesCount/ashesScale`。
    - 旧客户端先 `ClientProxy.vanish(entityID)`，再围绕实体体积生成 `ParticleAshes` 和 vanilla flame。
    - `ParticleAshes` 使用 `particle_base`，寿命 `1200..1219`，scale 为 `ashesScale * 0.9 + rand * 0.2`，灰黑色，重力 `0.01`，X/Z 摩擦 `0.95`、Y 摩擦 `0.99`；落地后不再 billboard，而是贴地水平 quad，最后 40 tick 淡出。
- 本批现代迁移：
  - 新增粒子类型与图集：
    - `explosion_small` -> `hbm:particle/particle_base`
    - `black_powder_smoke` -> `hbm:particle/particle_base`
    - `ashes` -> `hbm:particle/particle_base`
  - 新增现代粒子类：
    - `ExplosionSmallParticle`：保留旧小爆炸云的寿命、scale、X/Z 速度衰减、上飘、旋转、HSB 颜色曲线、透明度和膨胀曲线。
    - `BlackPowderSmokeParticle`：保留旧黑火药烟的 heading 速度、scale、寿命、速度阻尼、颜色/透明衰减和旋转。
    - `AshesParticle`：保留旧灰烬长寿命、灰黑色、重力/阻尼、落地后贴地 quad 和末段淡出。
  - `ClientModEvents#registerParticleProviders` 注册三个 provider；provider 同时缓存 `SpriteSet`，供 Aux NBT 分发按旧参数直接构造粒子。
  - `HbmParticleEffects` 分发更新：
    - `explosionSmall` 的云体从 `rocket_flame + ex_smoke` 近似改为 `ExplosionSmallParticle`。
    - `blackPowder` 的烟体从通用 `ex_smoke` 改为 `BlackPowderSmokeParticle`，火花继续使用已迁移的 `BlackPowderSparkParticle`。
    - `ashes` 从 vanilla `ASH` 改为 `AshesParticle`，并按旧版每个灰烬粒子附带一枚 vanilla flame。
- 边界：
  - `ExplosionSmallCreator` 的近/远爆炸声音延迟播放仍未接入；应在声音事件和爆炸声音切片中补 `weapon.explosionSmallNear/Far` 的现代注册与延迟播放。
  - `AshesCreator` 的 `ClientProxy.vanish(entityID)` 暂未复刻，因为现代实体隐藏生命周期需要单独迁移，不能在粒子库里粗暴改实体可见性。
  - 小爆炸 debris 仍用现代 block particle 表现；旧 `ParticleDebris` / `WorldInAJar` 碎块实体仍待后续深迁。

## 2026-05-24 第七批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：未通过；当前失败来自本批外的 `src/main/java/com/hbm/ntm/explosion/vnt/standard/BlockMutatorDigamma.java` 引用尚未注册的 `ModBlocks.PRIBRIS_DIGAMMA`、`ModBlocks.FIRE_DIGAMMA`、`ModBlocks.ASH_DIGAMMA`。本批新增粒子类未出现编译错误。

## 2026-05-24 第八批推进：爆炸粒子联动声音

- 1.7.10 对照：
  - `ExplosionSmallCreator`：
    - 客户端按玩家距离选择 `hbm:weapon.explosionSmallNear` 或 `hbm:weapon.explosionSmallFar`。
    - 声音范围固定 `200F`；距离小于 `soundRange * 0.4` 使用 near。
    - 音量 `100F`，音调 `0.9F + rand.nextFloat() * 0.2F`。
    - 延迟 tick 为 `dist / (17.15 * 0.5)`，模拟音速。
  - `ExplosionCreator`：
    - 读取 NBT `soundRange`，按同一 near/far 规则选择 `hbm:weapon.explosionLargeNear` 或 `hbm:weapon.explosionLargeFar`。
    - 音量 `1000F`，音调同上，延迟同上。
  - 旧资源：
    - `sounds/weapon/explosionLargeNear.ogg`
    - `sounds/weapon/explosionLargeFar.ogg`
    - `sounds/weapon/explosionSmallNear1..3.ogg`
    - `sounds/weapon/explosionSmallFar1..2.ogg`
    - 同目录还包含 `explosionTiny1..2.ogg` 与 `explosion_medium.ogg`，供爆炸/武器切片复用。
- 本批现代迁移：
  - 从 1.7.10 资源复制并按现代命名落位：
    - `weapon/explosion_large_near.ogg`
    - `weapon/explosion_large_far.ogg`
    - `weapon/explosion_small_near1..3.ogg`
    - `weapon/explosion_small_far1..2.ogg`
    - `weapon/explosion_tiny1..2.ogg`
    - `weapon/explosion_medium.ogg`
  - `ModSounds` 新增事件：
    - `weapon.explosion_large_near`
    - `weapon.explosion_large_far`
    - `weapon.explosion_small_near`
    - `weapon.explosion_small_far`
    - `weapon.explosion_tiny`
    - `weapon.explosion_medium`
  - `sounds.json` 登记上述事件，并保持小爆炸 near/far 与 tiny 的多样本随机选择。
  - 新增 `HbmDelayedSounds`：
    - 客户端按当前玩家距离、旧 near/far 阈值和 `17.15 * 0.5` 音速常量计算延迟。
    - 使用 `SimpleSoundInstance` + `SoundManager#playDelayed` 播放本地位置声音。
  - `HbmParticleEffects` 更新：
    - `explosionLarge` 在生成视觉前调用 `HbmDelayedSounds.playExplosionLarge(...)`，读取旧 NBT `soundRange`。
    - `explosionSmall` 在生成视觉前调用 `HbmDelayedSounds.playExplosionSmall(...)`，使用旧固定 200 格范围。
- 边界：
  - 本批只接入 ParticleCreators 的客户端联动爆炸声；其他爆炸逻辑中仍直接使用 vanilla `GENERIC_EXPLODE` 的地方，后续可按爆炸框架切片逐步替换为 HBM 声音事件。
  - 旧事件名 `weapon.explosionSmallNear` 等未直接保留为 registry id；现代端采用现有工程风格的 snake_case 事件名，并在本库分发处统一映射。

## 2026-05-24 声音 ResourceLocation 小写规则修正

- 运行客户端时发现 `ModSounds` 注册旧名 `weapon.reload.tubeFwoomp` 会在 Forge 1.20.1 构造 `ResourceLocation` 时崩溃：现代资源路径只允许 `[a-z0-9/._-]`。
- 气动管道发射声保留 1.7.10 旧资源语义，但现代 ID 和资源路径统一改为：
  - sound event: `hbm:weapon.reload.tube_fwoomp`
  - sound file: `assets/hbm/sounds/weapon/reload/tube_fwoomp.ogg`
- 后续从 1.7.10 复制声音资源时，驼峰旧名必须在现代端落位为小写/下划线，并同步更新 `ModSounds` 与 `sounds.json`。

## 2026-05-24 第八批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-24 第九批推进：大爆炸冲击波粒子

- 1.7.10 对照：
  - `ParticleMukeWave`：
    - 旧纹理：`assets/hbm/textures/particle/shockwave.png`。
    - `getFXLayer()=3`，直接绑定纹理并用加色混合 `SRC_ALPHA, ONE` 渲染。
    - 构造默认寿命 25 tick；`ExplosionCreator` 调 `setup(waveScale, (int)(25F * waveScale / 45))`。
    - 渲染为水平单 quad，Y 偏移 `-0.25`。
    - 透明度 `1 - (age + partial) / maxAge`。
    - 半径/scale 曲线为 `(1 - exp((age + partial) * -0.125)) * waveScale`。
  - `ExplosionCreator`：
    - `explosionLarge` 在云柱和 debris 前先生成 `ParticleMukeWave`，位置为 `(x, y + 2, z)`。
- 本批现代迁移：
  - 从 1.7.10 复制资源：
    - `assets/hbm/textures/particle/shockwave.png`
  - 新增粒子类型与图集：
    - `ModParticleTypes.MUKE_WAVE`
    - `assets/hbm/particles/muke_wave.json` -> `hbm:particle/shockwave`
  - 新增 `MukeWaveParticle`：
    - 保留旧水平单 quad、fullbright、加色混合、无深度写入。
    - 保留旧 `waveScale/maxAge` 参数、透明衰减和指数扩张公式。
    - provider 缓存 `SpriteSet`，供 `explosionLarge` NBT 分发按旧 `waveScale` 直接实例化。
  - `HbmParticleEffects#spawnExplosionLarge` 更新：
    - 将原先 `POOF`/`CLOUD` 环形近似替换为 `MukeWaveParticle.create(level, x, y + 2, z, waveScale, 25 * waveScale / 45)`。
    - 保留已有烟柱、方块碎屑和延迟爆炸声。
- 边界：
  - `ParticleMukeWave` 的旧 GL fog 开关细节未逐项复刻；现代实现通过独立 `ParticleRenderType` 设置加色混合、关闭深度写入和 fullbright，以保留主要视觉合同。
  - `ParticleDebris` / `WorldInAJar` 碎块实体仍未深迁，大爆炸 debris 仍是现代 block particle 桥接。

## 2026-05-24 第九批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；编译器仅提示 `MukeWaveParticle` 使用了 Minecraft/Forge 侧已弃用渲染 API。

## 2026-05-24 第十批推进：3D 弹壳粒子与落地声音

- 1.7.10 对照：
  - `SpentCasing`：
    - 定义弹壳类型 `STRAIGHT/BOTTLENECK/SHOTGUN`，对应 OBJ 分组 `Straight`、`Bottleneck`、`Shotgun + ShotgunCase`。
    - NBT/注册名通过 `casingMap` 查配置；字段包括缩放、颜色、弹跳声音、弹跳旋转系数、最大寿命。
    - 弹跳声音事件为 `hbm:weapon.casing.shell/small/medium/large`，按弹壳体积和类型选择。
  - `CasingCreator`：
    - 旧 NBT `type=casingNT`，字段 `mX/mY/mZ/yaw/pitch/mPitch/mYaw/name/smoking/smokeLife/smokeLift/nodeLife`。
    - 客户端用 `SpentCasing.casingMap.get(name)` 构造 `ParticleSpentCasing`，并把 `yaw/pitch` 写入初始旋转。
  - `ParticleSpentCasing`：
    - 使用 `models/effect/casings.obj` 和 `textures/particle/casings.png`。
    - 物理为重力 `0.04`、速度阻尼 `0.98`、落地 X/Z 阻尼 `0.7`、碰撞反弹 X/Z `-0.25`、Y `-0.5`。
    - 落地/碰撞时按初始 Y 速度调整 pitch/yaw 旋转动量，并在垂直碰撞速度足够时播放 plink 声。
    - smoking 时生成无贴图白色半透明烟带节点，节点按 `smokeLift * 0.05` 上升并按 `nodeLife` 衰减。
- 本批现代迁移：
  - 复制旧资源：
    - `assets/hbm/models/effect/casings.obj`
    - `assets/hbm/textures/particle/casings.png`
    - `assets/hbm/textures/particle/casings_base.png`
    - `assets/hbm/sounds/weapon/casing/{shell,small,medium,large}1..3.ogg`
  - 新增 `SpentCasingDefinition`：
    - 保留旧类型、分组、颜色、缩放、bounce sound、bounce motion、maxAge 合同。
    - 先登记常见 Sedna 口径和火炮弹壳注册名；未知 name 回落到 `default` 黄铜直筒弹壳，避免旧 NBT 丢粒子。
  - 新增 `SpentCasingParticle`：
    - 使用现有 `LegacyWavefrontModel` 渲染旧 `casings.obj` 分组。
    - 保留旧重力、摩擦、碰撞反弹、落地 pitch 归整、旋转动量与弹跳声音逻辑。
    - 自绘无贴图半透明烟带，接入旧 `smoking/smokeLife/smokeLift/nodeLife` 字段。
  - `HbmParticleEffects#spawnCasing` 从火花/vanilla smoke 桥接替换为真正 3D 弹壳粒子。
  - `ParticleUtil#spawnCasing` 新增完整旧 NBT 字段重载，旧简化重载仍保留并写入默认值。
  - `ModSounds` 与 `sounds.json` 新增：
    - `weapon.casing.shell`
    - `weapon.casing.small`
    - `weapon.casing.medium`
    - `weapon.casing.large`
- 边界：
  - `SpentCasingDefinition` 目前不是从完整 Sedna 枪械工厂自动初始化，而是按本批已核对的常见注册名手工登记；后续迁移更多枪械/弹药工厂时，应继续补齐所有 `new SpentCasing(...).register(name)`。
  - 现代粒子渲染路径使用 `LegacyWavefrontModel` + `MultiBufferSource`，主要 OBJ/颜色/缩放合同已对齐；旧 GL `RESCALE_NORMAL` 与标准物品光照开关没有逐项复刻。
  - smoke ribbon 已按旧节点合同自绘，但没有复用旧 `TextureManager`/Tessellator 精确状态；视觉上应为同类白色渐隐烟迹。

## 2026-05-24 第十批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；编译器仅提示部分文件使用/覆盖已过时 API。

## 2026-05-25 第十一批推进：骨骼 OBJ 粒子与实体短暂隐藏

- 1.7.10 对照：
  - `SkeletonCreator`：
    - 旧 NBT `type=skeleton`，字段 `entityID/brightness/gib/force`。
    - 客户端按 `entityID` 找到 `EntityLivingBase`，调用 `ClientProxy.vanish(entityID)` 隐藏原实体约 2 秒。
    - 骨架分解表分为 biped、zombie/skeleton、villager/witch 三类；每类生成头、躯干、四肢 6 个 `BoneDefinition`。
    - gib 模式下，非骷髅实体随机跳过部分骨块；骷髅实体 gib 保持普通骨骼贴图而非血迹贴图。
  - `ParticleSkeleton`：
    - 旧模型：`assets/hbm/models/effect/skeleton.obj`，OBJ 分组 `Skull/Torso/Limb/SkullVillager`。
    - 旧贴图：`textures/particle/skeleton.png`、`skeleton_blood.png`、`skoilet.png`、`skoilet_blood.png`。
    - 普通模式寿命 `1200 + rand(20)`，初始延迟 20 tick，重力 `0.02`。
    - gib 模式寿命 `600 + rand(20)`，跳过初始延迟随机化，重力 `0.04`。
    - 落地后清零速度，并播放 `mob.skeleton.hurt`，音量 0.25、pitch `0.8 + rand * 0.4`。
    - 最后 40 tick 线性淡出。
- 本批现代迁移：
  - 复制旧资源：
    - `assets/hbm/models/effect/skeleton.obj`
    - `assets/hbm/textures/particle/skeleton.png`
    - `assets/hbm/textures/particle/skeleton_blood.png`
    - `assets/hbm/textures/particle/skoilet.png`
    - `assets/hbm/textures/particle/skoilet_blood.png`
  - 新增 `SkeletonParticle`：
    - 使用 `LegacyWavefrontModel` 渲染旧 OBJ 分组。
    - 保留初始延迟、随机旋转动量、普通/gib 寿命、重力、落地停住、骷髅受伤音效和 40 tick 淡出。
    - villager 头部使用 `skoilet*` 贴图，其他部件使用 skeleton 贴图；gib 按旧逻辑切血迹贴图。
  - `HbmParticleEffects#spawnSkeleton`：
    - 从骨块占位粒子替换为真正 legacy 骨位分解。
    - 按现代实体类型映射 `Zombie/AbstractSkeleton/ZombifiedPiglin`、`Villager/Witch`，并保留旧 techguns 简名兼容分支。
    - 读取旧 `brightness/gib/force` 字段并实例化对应 `SkeletonParticle`。
  - `ClientForgeEvents` 新增客户端 vanish 表：
    - `RenderLivingEvent.Pre` 中取消短暂隐藏实体渲染。
    - 每 tick 清理过期项，换世界时清空。
    - `spawnSkeleton` 与已有 `spawnAshes` 均接入该隐藏路径，补齐旧 `SkeletonCreator/AshesCreator` 的共同客户端行为。
  - `LegacyWavefrontModel` 新增窄接口 `renderPartTranslucent(...)`，仅供需要 alpha 淡出的 OBJ 粒子使用，不改变已有机器/弹壳 OBJ 的默认 cutout 渲染。
- 边界：
  - 旧 `EntityDummy` 与 HBM 自定义 undead soldier 现代实体尚未完整迁移；本批对未知 living entity 默认走 biped 骨架分解。
  - vanish 目前只覆盖客户端渲染隐藏，不改变实体逻辑、碰撞或服务端状态；这与旧 `RenderLivingEvent.Pre` 取消渲染的合同一致。
  - 旧标准物品光照/GL cull/blend 细节通过现代 `entityTranslucent` RenderType 近似承载，主要模型、贴图、透明淡出和声音合同已保留。

## 2026-05-25 第十一批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-25 第十二批推进：giblets 血肉/史莱姆/金属碎块粒子

- 1.7.10 对照：
  - `ClientProxy` aux 类型 `giblets`：
    - 旧 NBT 字段 `ent/gibType/cDiv`。
    - 客户端先 `vanish(ent)`，再按实体宽高估算碎块数：`(int)(width / 0.25) * 1.5 * (int)(height / 0.25)`。
    - 若存在 `cDiv`，碎块数按 `ceil(count / cDiv)` 降低。
    - `rand.nextInt(15)==0` 时速度倍率提升到 10。
    - 每块 `ParticleGiblet` 从 aux 位置喷出，速度为高斯 X/Z `0.25 * mult`、Y `rand * mult`。
  - `ParticleGiblet`：
    - 旧贴图：
      - `textures/particle/meat.png`
      - `textures/particle/slime.png`
      - `textures/particle/metal.png`
    - `gibType=0` 肉块，`gibType=1` 史莱姆，`gibType=2` 金属。
    - 寿命 `140 + rand(20)`，重力 `2F`；金属重力翻倍。
    - 飞行中持续旋转；非金属碎块飞行时生成方块尘拖尾，肉为红石块尘，史莱姆为西瓜块尘；金属无拖尾。
  - 旧调用点包括 sawblade/cog/turbofan/ore slopper/glyphid death/confetti 等，常用 `cDiv=5` 做数量削减。
- 本批现代迁移：
  - 复制旧资源：
    - `assets/hbm/textures/particle/meat.png`
    - `assets/hbm/textures/particle/slime.png`
    - `assets/hbm/textures/particle/metal.png`
  - 新增粒子类型与图集：
    - `giblet_meat` -> `hbm:particle/meat`
    - `giblet_slime` -> `hbm:particle/slime`
    - `giblet_metal` -> `hbm:particle/metal`
  - 新增 `GibletParticle`：
    - 保留旧寿命、重力、速度阻尼、飞行旋转和非金属方块尘拖尾。
    - 现代端用 `TextureSheetParticle`/sprite atlas 承载旧单贴图 quad。
  - `ParticleUtil` 新增公共入口：
    - `TYPE_GIBLETS = "giblets"`
    - `GIBLET_MEAT/GIBLET_SLIME/GIBLET_METAL`
    - `spawnGiblets(Entity, gibType, countDivisor)` 与坐标/实体 id 重载。
  - `HbmParticleEffects` 新增 `spawnGiblets`：
    - 读取旧 `ent/gibType/cDiv` 字段。
    - 复用第十一批客户端 vanish 表隐藏实体。
    - 按旧实体尺寸公式生成碎块，并保留 1/15 的高速度倍率。
  - `CommonForgeEvents` 中高辐射直接致死路径改为调用 `ParticleUtil.spawnGiblets(entity, GIBLET_MEAT)`，开始让现代死亡效果走本库入口。
- 边界：
  - 旧 `ParticleGiblet` 渲染代码保存了 pitch/yaw 两轴旋转；现代 `TextureSheetParticle` 主要以 roll 承载可见旋转，物理/贴图/拖尾/数量/速度合同已保留。
  - 旧 sawblade/cog/turbofan/ore slopper 等调用点在 clean port 中尚未完整对应；本批先迁通库和高辐射致死调用点，后续迁对应实体/机器时应改为 `ParticleUtil.spawnGiblets(...)`。

## 2026-05-25 第十二批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；编译器仅提示部分输入文件使用/覆盖已过时 API。

## 2026-05-25 第十三批推进：Tau/Hadron/Amat 闪光粒子

- 1.7.10 对照：
  - `ClientProxy` aux 类型：
    - `hadron`：直接生成 `ParticleHadron`。
    - `tau`：读取 `small/count`，生成 `count` 个 `ParticleSpark.makeSmall(small)`，并生成一个 `ParticleHadron.makeSmall(small)`。
    - `amat`：读取 `scale`，生成 `ParticleAmatFlash`。
  - `ParticleSpark`：
    - 无贴图线段粒子，`getFXLayer()=3`。
    - 记录最近 `thresh=4+rand(3)` 个速度 step，寿命 `20+rand(10)`，重力 `0.5`。
    - 落地时反弹 Y 速度 `-lastY * 0.8`。
    - small 模式：`thresh=3`、寿命 `2+rand(3)`、Y 速度强制向下。
  - `ParticleHadron`：
    - 旧贴图 `textures/particle/hadron.png`。
    - 寿命 10 tick，small 模式 5 tick。
    - 加色混合、fullbright、深度不写入；scale 曲线 `(age + partial) * 0.15 * particleScale`，alpha 线性衰减。
  - `ParticleAmatFlash`：
    - 无贴图三角光束，固定随机种子 `432L`。
    - 寿命 10 tick，按 `0.2 * scale` 缩放。
    - 渲染 100 组三角扇式白色加色光束，随时间伸长并淡出。
- 本批现代迁移：
  - 复制旧资源：
    - `assets/hbm/textures/particle/hadron.png`
  - 新增粒子类型与图集：
    - `hadron` -> `hbm:particle/hadron`
  - 新增 `HadronParticle`：
    - 使用旧 hadron 贴图，保留 fullbright、加色混合、深度不写入、10/5 tick 寿命、scale/alpha 曲线。
    - provider 缓存 sprite，供 `tau` 组合效果直接实例化 small 变体。
  - 新增 `TauSparkParticle`：
    - 保留旧无贴图白线轨迹、step 队列、寿命、重力、落地反弹和 small 模式。
  - 新增 `AmatFlashParticle`：
    - 使用固定随机种子重建 100 组无贴图加色三角光束，保留 `scale`、寿命和淡出合同。
  - `ParticleUtil` 新增公共入口：
    - `TYPE_TAU/TYPE_HADRON/TYPE_AMAT_FLASH`
    - `spawnTau(...)`、`spawnHadron(...)`、`spawnAmatFlash(...)`
  - `HbmParticleEffects` 更新：
    - `tau` 从未实现状态接入真实 `TauSparkParticle + HadronParticle` 组合。
    - `hadron` 接入真实 hadron 贴图闪光。
    - `amat` 从 `WITCH`/`EXPLOSION_EMITTER` 近似替换为 `AmatFlashParticle`。
- 边界：
  - 旧 `ParticleSpark` 的线宽依赖 GL 固定管线；现代端通过 `RenderSystem.lineWidth(3.0F)` 和 `DEBUG_LINE_STRIP` 承载，实际线宽可能受显卡/驱动限制。
  - `ParticleAmatFlash` 现代实现用三角列表重建旧三角扇效果，保留固定随机、加色、淡出和 scale；旧 GL shade model 的平滑插值由顶点 alpha 渐变近似承载。
  - `rift` 仍未迁移，虽然旧端也使用 `hadron.png`；后续可在本批 hadron 贴图基础上继续补。

## 2026-05-25 第十三批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；编译器仅提示 `HadronParticle` 使用/覆盖已过时 API。

## 2026-05-25 第十四批推进：RBMK 动画粒子、冷却塔烟与 splash 粒子

- 1.7.10 对照：
  - `ClientProxy` aux 类型：
    - `rbmkflame`：读取 `maxAge`，生成 `ParticleRBMKFlame`。
    - `rbmksteam`：生成 `ParticleRBMKSteam`。
    - `rbmkmush`：读取 `scale`，生成 `ParticleRBMKMush`。
    - `tower`：读取 `lift/base/max/life/strafe/noWind/alpha/color`，生成 `ParticleCoolingTower`。
    - `splash`：读取可选 `color`，生成 `ParticleSplash`。
  - `ParticleRBMKFlame`：
    - 旧贴图 `textures/particle/rbmk_fire.png`，14 个横向帧。
    - 寿命来自 NBT；scale 为 `randFloat + 1`；加色混合、fullbright、深度不写入。
    - 帧序号 `particleAge * 5 % 14`；前 20 tick 淡入，末 20 tick 淡出，并整体乘 `0.5` alpha。
  - `ParticleRBMKSteam`：
    - 旧贴图 `textures/particle/rbmk_jet_steam.png`，20 个横向帧。
    - 寿命 10 tick，scale 4，alpha 0.25，加色混合、fullbright、深度不写入。
  - `ParticleRBMKMush`：
    - 旧贴图 `textures/particle/rbmk_mush.png`，30 个纵向帧。
    - 寿命 50 tick，scale 来自 NBT，按生命进度播放纵向帧，加色混合、fullbright。
  - `ParticleCoolingTower`：
    - 旧贴图来自 block atlas 的 `textures/blocks/particle/particle_base.png`。
    - 默认颜色 0.9-0.95；alpha 线性衰减；scale 按 `base + pow(max * ageScale - base, 2)` 增长。
    - `lift` 控制 Y 速度趋近，`strafe` 增加随机横移，未设置 `noWind` 时持续施加 +X/-Z 风向，速度阻尼 0.925。
  - `ParticleSplash`：
    - 旧贴图来自 block atlas 的 `textures/blocks/particle/particle_splash.png`。
    - 可选 RGB 着色，否则随机灰白色；alpha 0.5，scale 0.4，寿命 `200+rand(50)`，重力 0.4。
    - 空中随机 X/Z 扰动，Y 下落速度封顶 -0.5，落地移除。
- 本批现代迁移：
  - 复制旧资源：
    - `assets/hbm/textures/particle/rbmk_fire.png`
    - `assets/hbm/textures/particle/rbmk_jet_steam.png`
    - `assets/hbm/textures/particle/rbmk_mush.png`
    - `assets/hbm/textures/particle/particles.png`
    - `assets/hbm/textures/particle/particle_splash.png`
  - 新增粒子类型与图集：
    - `rbmk_flame` -> `hbm:particle/rbmk_fire`
    - `rbmk_steam` -> `hbm:particle/rbmk_jet_steam`
    - `rbmk_mush` -> `hbm:particle/rbmk_mush`
    - `cooling_tower` -> `hbm:particle/particle_base`
    - `splash` -> `hbm:particle/particle_splash`
  - 新增 `RbmkAnimatedParticle`：
    - 使用一张 legacy 条带贴图作为 atlas sprite，并在 sprite 内部按 14/20 横帧与 30 纵帧切 UV。
    - 保留 RBMK flame/steam/mush 的寿命、scale、alpha、fullbright 和加色混合合同。
  - 新增 `CoolingTowerParticle`：
    - 保留旧 `lift/base/max/life/strafe/noWind/alpha/color` 字段与扩散/风向/阻尼逻辑。
  - 新增 `LegacySplashParticle`：
    - 保留旧着色、alpha、寿命、重力、随机扰动、下落封顶和落地移除行为。
  - `ParticleUtil` 新增公共入口：
    - `TYPE_RBMK_FLAME/TYPE_RBMK_STEAM/TYPE_RBMK_MUSH/TYPE_COOLING_TOWER/TYPE_SPLASH`
    - `spawnRbmkFlame(...)`、`spawnRbmkSteam(...)`、`spawnRbmkMush(...)`、`spawnCoolingTower(...)`、`spawnSplash(...)`
  - `HbmParticleEffects` 新增以上 aux 类型分发，后续 RBMK、冷却塔和流体效果迁移可直接走本库入口。
- 边界：
  - 旧 flame/steam 使用接近 yaw billboard 的手写四边形；现代端通过相机 billboard 旋转承载，尺寸、偏移、动画帧和透明曲线已保留。
  - 旧 `ParticleSplash` 根据实体 id 随机翻转 UV；现代端目前使用 atlas 默认方向，物理、颜色和贴图合同已保留，UV 翻转可在后续视觉精修中补。
  - `fluidfill`、`rift` 等仍未迁移；本批优先补齐 RBMK/tower/splash 这一组高复用视觉效果。

## 2026-05-25 第十四批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-25 第十五批推进：rift、deadleaf、fluidfill 与 foundry 粒子

- 1.7.10 对照：
  - `ClientProxy` aux 类型：
    - `rift`：生成 `ParticleRift`。
    - `deadleaf`：受粒子设置控制后生成 `ParticleDeadLeaf`。
    - `fluidfill`：读取 `mX/mY/mZ/color`，生成 `EntityCritFX`，调用 `nextTextureIndexX()`，可选 RGB 着色。
    - `foundry`：读取 `color/dir/len/base/off`，生成 `ParticleFoundry`。
  - `ParticleRift`：
    - 寿命 10 tick，无物理。
    - 旧代码绑定 `hadron.png` 后禁用纹理，使用 `ONE_MINUS_DST_COLOR/ONE_MINUS_SRC_COLOR` 反色混合。
    - scale 为 `(age + partial) * 0.5`，连续渲染 5 层 `sphere_uv` 球体，每层略微放大。
  - `ParticleDeadLeaf`：
    - 旧 block atlas 图标 `particle/dead_leaf`。
    - 灰白色随机 0.8-1.0，scale 0.1，寿命 `200+rand(50)`，重力 0.2。
    - 空中随机 X/Z 漂移，Y 下落速度封顶 -0.025。
  - `ParticleFoundry`：
    - 旧贴图 `textures/blocks/lava_gray.png`。
    - 寿命 20 tick，无移动；按 `ForgeDirection dir` 与 `dir.getRotation(UP)` 生成下落段、上部回流段和弯折段几何。
    - 宽度从 0.0625 扩到 0.125；girth 从 0.125 衰减到 0；颜色取传入 RGB 的 brighter 结果再向白色提亮。
- 本批现代迁移：
  - 复用/补充资源与模型：
    - 复用已存在 `assets/hbm/textures/particle/dead_leaf.png` 与 `dead_leaf` 粒子类型。
    - 复用已迁入 `ObjEffectModels.SPHERE_UV`。
    - 复制旧 `textures/blocks/lava_gray.png` 到 `assets/hbm/textures/particle/lava_gray.png`。
  - 新增 `RiftParticle`：
    - 使用现代无贴图 position/color 粒子渲染和反色混合。
    - 通过 `ObjEffectModels.SPHERE_UV` 保留旧五层球体扩张合同。
    - 为此在 `LegacyWavefrontModel` 增加窄接口 `renderAllUntextured(VertexConsumer, Matrix4f, ...)`，不改变机器 OBJ 默认渲染路径。
  - 新增 `FluidFillParticle`：
    - 作为旧 `EntityCritFX + nextTextureIndexX + color` 的现代承载；读取 `mX/mY/mZ/color`，保留短寿命飞散和着色合同。
  - 新增 `FoundryParticle`：
    - 使用 legacy `lava_gray.png`，以 fullbright 顶点几何重建旧 foundry sploosh 的下落、上部、弯折段。
    - 保留 `color/dir/len/base/off` 字段、20 tick 寿命、宽度/girth 进度和流动 UV 偏移。
  - `ParticleUtil` 新增公共入口：
    - `TYPE_RIFT/TYPE_DEAD_LEAF/TYPE_FLUID_FILL/TYPE_FOUNDRY`
    - `spawnRift(...)`、`spawnDeadLeaf(...)`、`spawnFluidFill(...)`、`spawnFoundry(...)`
  - `HbmParticleEffects` 新增以上 aux 分发：
    - `deadleaf` 直接接入已存在 `ModParticleTypes.DEAD_LEAF`。
    - `rift/fluidfill/foundry` 接入本批真实粒子类。
- 边界：
  - 旧 `fluidfill` 使用原版 crit 粒子的内部贴图序列；现代端使用已有 hadron sprite 承载小型填充飞点，运动和 RGB 着色合同已保留，具体图案后续可用专用 sprite 精修。
  - `ParticleFoundry` 的 ForgeDirection 映射按 1.7.10 方向 ordinal 迁入水平四向；若后续调用传入上下方向，本批会退化为中心竖流。
  - `RiftParticle` 保留反色混合和 `sphere_uv` 多层扩张，但旧固定管线 alpha/depth 状态与现代 RenderType 状态可能存在轻微视觉差异。
  - 本批验证过程中遇到并修复了外部迁移分支留下的 `ModMenuTypes.NUCLEAR_DEVICE` 注册缺口，属于构建恢复项，不改变本库粒子合同。

## 2026-05-25 第十五批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；编译器仅提示部分输入文件使用/覆盖已过时 API。

## 2026-05-25 第十六批推进：debug 文本与 network 调试粒子

- 1.7.10 对照：
  - `ClientProxy` aux 类型：
    - `debug`：读取 `text/color/scale`，生成 `ParticleText` 并按 `scale` 放大。
    - `debugline`：读取 `mX/mY/mZ/color`，生成 `ParticleDebugLine`。
    - `debugdrone`：仅当玩家手持无人机相关物品/方块时生成 `ParticleDebugLine`。
    - `network`：读取 `mX/mY/mZ/mode/color`，`mode=power` 生成 power 图标，`mode=fluid` 生成 fluid 图标并使用 RGB 着色。
  - `ParticleText`：
    - 寿命 100 tick，`motionY=0.01`，`noClip=true`。
    - 面向玩家相机，按 `particleScale * 0.01` 缩放，用 MC 字体阴影绘制文本，fullbright。
  - `ParticleDebug`：
    - 旧贴图：
      - `textures/particle/debug_power.png`
      - `textures/particle/debug_fluid.png`
    - 寿命 10 tick，`noClip=true`，scale 0.05，fullbright。
    - 构造时记录 `motionX/Y/Z`，继承 `EntityFX` 的每 tick 位移。
- 本批现代迁移：
  - 复制旧资源：
    - `assets/hbm/textures/particle/debug_power.png`
    - `assets/hbm/textures/particle/debug_fluid.png`
  - 新增粒子类型与图集：
    - `network_power` -> `hbm:particle/debug_power`
    - `network_fluid` -> `hbm:particle/debug_fluid`
  - 新增 `DebugTextParticle`：
    - 使用现代 `Font#drawInBatch` 在粒子自定义渲染中绘制文本。
    - 保留旧 `text/color/scale`、100 tick 寿命、上浮和 fullbright 文本合同。
  - 新增 `NetworkDebugParticle`：
    - 使用 legacy power/fluid 图标，保留 10 tick 寿命、0.05 scale、motion 位移、fluid RGB 着色和 fullbright。
  - `debugline/debugdrone`：
    - 继续走已有 `DebugLineParticle`，保留线段坐标与颜色合同。
  - `ParticleUtil` 新增公共入口：
    - `TYPE_DEBUG_TEXT/TYPE_NETWORK`
    - `spawnDebugText(...)`、`spawnPowerNetworkDebug(...)`、`spawnFluidNetworkDebug(...)`
  - `HbmParticleEffects` 新增 `debug/network` 分发。
- 边界：
  - 旧 `debugdrone` 的手持物品门槛依赖尚未完整迁移的无人机物品/方块集合；本批不猜测现代对应项，仍保留当前 debugdrone 线粒子入口，后续迁无人机库时应补回门槛。
  - 旧 `ParticleText` 使用 `drawStringWithShadow`；现代端当前使用 `drawInBatch` 普通文本和 fullbright，阴影可在后续视觉精修中增加，但字段、寿命、上浮、朝向和缩放合同已接入。
  - `marker` 需要 `RenderOverhead/Marker` HUD 队列系统；本批未迁，后续应作为 HUD/overhead 标记库处理。

## 2026-05-25 第十六批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-25 第十七批推进：fireworks 字母烟花、plasmablast、haze 与客户端 jolt

- 1.7.10 对照：
  - `ClientProxy` aux 类型：
    - `fireworks`：读取 `color/char`，生成 `ParticleLetter`，并生成 50 个 `EntityFireworkSparkFX`，速度高斯 `0.4`。
    - `haze`：生成 `ParticleHaze`。
    - `plasmablast`：读取 `r/g/b/pitch/yaw/scale`，生成 `ParticlePlasmaBlast` 并设置 scale。
    - `justTilt`：读取 `time`，设置客户端玩家 `hurtTime=maxHurtTime=time`，`attackedAtYaw=0`。
    - `properJolt`：读取 `time/maxTime`，设置客户端玩家 `hurtTime=time`、`maxHurtTime=maxTime`，`attackedAtYaw=0`。
  - `ParticleLetter`：
    - 寿命 30 tick，面向玩家相机。
    - 缩放曲线 `1 - exp(-(age+partial)*4/maxAge)`，alpha 线性衰减但最低 10/255。
    - 使用 MC 字体绘制单字符，fullbright。
  - `ParticlePlasmaBlast`：
    - 旧贴图 `textures/particle/shockwave.png`。
    - 寿命 20 tick，加色混合、fullbright、深度不写入、禁用背面剔除。
    - 以 `yaw/pitch` 旋转水平四边形；alpha 线性衰减，scale 曲线 `(1-exp(-(age+partial)*0.125))*particleScale`。
  - `ParticleHaze`：
    - 旧贴图 `textures/particle/haze.png`。
    - 默认寿命 `600+rand(100)`，颜色黑色，scale 10。
    - 每 tick 阻尼速度并在周围地表生成 lava 粒子。
    - 渲染时固定随机种子 50，绘制 25 张大型雾片；alpha 为 `sin(age*pi/400)*0.025`。
- 本批现代迁移：
  - 复用旧资源：
    - `assets/hbm/textures/particle/shockwave.png`
    - `assets/hbm/textures/particle/haze.png`
  - 新增粒子类型与图集：
    - `plasma_blast` -> `hbm:particle/shockwave`
    - `haze` -> `hbm:particle/haze`
  - 新增 `FireworkLetterParticle`：
    - 以现代字体渲染保留旧单字符、30 tick 寿命、指数缩放曲线、alpha 下限和 fullbright 合同。
    - `HbmParticleEffects#spawnFireworks` 同时生成 50 个原版 firework 粒子，保留旧爆散数量和速度尺度。
  - 新增 `PlasmaBlastParticle`：
    - 使用旧 shockwave 贴图，保留 `r/g/b/pitch/yaw/scale` 字段、20 tick 寿命、加色混合、fullbright、透明衰减和指数 scale 曲线。
  - 新增 `HazeParticle`：
    - 使用旧 haze 贴图，保留 600+rand(100) 寿命、黑色大雾片、固定随机分布、lava 地表粒子和 alpha 曲线。
  - `ParticleUtil` 新增公共入口：
    - `TYPE_FIREWORKS/TYPE_HAZE/TYPE_PLASMA_BLAST/TYPE_JUST_TILT/TYPE_PROPER_JOLT`
    - `spawnFireworks(...)`、`spawnHaze(...)`、`spawnPlasmaBlast(...)`、`spawnJustTilt(...)`、`spawnProperJolt(...)`
  - `HbmParticleEffects` 新增以上 aux 分发。
- 边界：
  - 旧 `ParticleLetter` 使用固定管线和非阴影字体绘制；现代端用 `Font#drawInBatch` 承载，保持单字符、颜色、alpha、缩放和朝向合同。
  - 旧 `EntityFireworkSparkFX#setColour(color)` 的精确颜色未直接暴露在现代 vanilla `FIREWORK` 粒子入口中；本批保留数量、位置和速度爆散，颜色由字母粒子体现。
  - 现代 `Player#hurtDir`/旧 `attackedAtYaw` 对应字段受保护；本批保留 `hurtTime/hurtDuration` 客户端受击动画主体，无法直接强制 yaw 为 0。

## 2026-05-25 第十七批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；编译器仅提示部分输入文件使用/覆盖已过时 API。

## 2026-05-25 第十八批推进：jetpack 系列推进粒子与 radiation aura

- 1.7.10 对照：
  - `ClientProxy` aux 类型：
    - `jetpack`：读取 `player/mode`，按玩家背部双喷口生成 flame；最高粒子质量时额外生成 smoke 和地面方块尘反冲。
    - `bnuuy`：读取 `player`，在背部两个偏移点生成小 smoke。
    - `jetpack_bj`：读取 `player`，在双喷口生成紫色 red dust，并在最高粒子质量时生成地面方块尘反冲。
    - `jetpack_dns`：读取 `player`，在双喷口生成青色 red dust，并在最高粒子质量时生成地面方块尘反冲。
    - `radiation`：读取 `count`，围绕客户端玩家生成 `EntityAuraFX`，颜色 `(0,0.75,1)`，速度高斯随机。
  - 旧 jetpack 位置合同：
    - 以玩家 `renderYawOffset`/头身角换算背部向量和侧向偏移。
    - `jetpack` 背后偏移 0.25、侧向 0.125、高度 `eyeHeight-1`。
    - `bnuuy` 背后偏移 0.6、侧向 0.275、高度 `eyeHeight-1+0.4`，潜行时额外 +0.25。
    - `jetpack_bj` 背后偏移 0.3125、侧向 0.125、高度 `eyeHeight-0.9375`。
    - `jetpack_dns` 使用当前位置、侧向 0.125、高度 `posY-yOffset-0.5`。
  - 旧地面反冲：
    - 从喷口位置沿推力方向 raycast 10 格。
    - 若命中方块上表面，按距离生成最多约 10 个 `EntityBlockDustFX`，速度绕 Y 随机旋转。
- 本批现代迁移：
  - `ParticleUtil` 新增公共入口：
    - `TYPE_JETPACK/TYPE_BNUUY/TYPE_JETPACK_BJ/TYPE_JETPACK_DNS/TYPE_RADIATION`
    - `spawnJetpack(...)`、`spawnBnuuy(...)`、`spawnJetpackBj(...)`、`spawnJetpackDns(...)`、`spawnRadiationAura(...)`
  - `HbmParticleEffects` 新增以上 aux 分发：
    - `jetpack`：读取实体 id 和 mode，生成双 `FLAME`、双 `SMOKE` 与方块尘反冲。
    - `bnuuy`：生成双小烟轨迹。
    - `jetpack_bj`：生成紫色 `DustParticleOptions` 双喷口，并保留地面反冲。
    - `jetpack_dns`：生成青色 `DustParticleOptions` 双喷口，并保留地面反冲。
    - `radiation`：围绕本地玩家生成 `SCHRAB_FOG` 粒子，速度随机，承载旧青色 aura 效果。
- 边界：
  - 现代端没有直接复用旧 `EntitySmokeFX` 的私有 `smokeParticleScale`；`bnuuy` 保留双喷口位置和烟雾运动，烟粒尺寸由现代 vanilla smoke 控制。
  - 旧代码根据 `particleSetting` 跳过或降级细节；现代端未直接读取 vanilla 粒子设置，保留完整效果输出。后续可接入 Options 粒子等级做降级。
  - 现代玩家朝向使用 `Entity#getYRot()` 近似旧 `renderYawOffset`；背部/侧向偏移、双喷口、推力和地面反冲合同已保留。
  - `radiation` 旧用 `EntityAuraFX`；现代端复用已有 `SCHRAB_FOG` 青色粒子承载，位置、数量和随机速度合同已保留。

## 2026-05-25 第十八批验证

- 已运行：`.\gradlew.bat clean compileJava processResources --no-daemon`
- 结果：通过；曾遇到 Gradle 增量编译输出缓存导致的 `bad class file/NoSuchFileException build/classes/...`，执行 `clean` 后通过。
