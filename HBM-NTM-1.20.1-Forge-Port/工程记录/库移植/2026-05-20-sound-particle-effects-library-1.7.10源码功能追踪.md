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

## 2026-05-27 呕吐/出汗旧协议粒子对齐

1.7.10 对照：

- `ClientProxy#effectNT` 的 `type="vomit"`：
  - 从实体 `posX`, `posY - getYOffset() + getEyeHeight() + (player ? 1 : 0)`, `posZ` 发射。
  - 速度沿 `entity.getLookVec()`，附加高斯扰动。
  - `mode="normal"` 使用 stained hardened clay metadata `5` 或 `13` 的 `EntityBlockDustFX`，速度倍率 `0.2`。
  - `mode="blood"` 使用 redstone block 的 `EntityBlockDustFX`，速度倍率 `0.2`。
  - `mode="smoke"` 使用 `EntitySmokeFX`，速度倍率 `0.05`，scale `0.2F`。
  - normal/blood 寿命强制为 `150 + rand(50)` tick；smoke 寿命强制为 `10 + rand(10)` tick。
  - count 会除以客户端粒子设置 `particleSetting + 1`。
- `type="sweat"`：
  - 在实体包围盒外扩 `0.2` 的随机位置生成指定方块的 `EntityBlockDustFX`。
  - 寿命强制为 `150 + rand(50)` tick。

本批现代迁移：

- `HbmParticleEffects#spawnEntityVomit` 改为从实体头部/眼部沿 `getLookAngle()` 前向喷出，而不是在实体中心随机扩散。
- normal/blood 改回 block dust 形态，并恢复 `150..199` tick 留存；normal 使用现代 `lime_terracotta` / `green_terracotta` 近似旧 stained hardened clay metadata `5/13`。
- smoke 改为使用项目 `HbmSmokeParticle`，恢复前向速度与 `10..19` tick 短寿命。
- `spawnEntitySweat` 改为旧包围盒随机点和 `150..199` tick 留存。
- `ParticleUtil` 增加 `spawnVomit` / `spawnSweat` helper，作为 common 侧旧 NBT 协议入口。
- 2026-06-04 re-check: modern `TerrainParticle` construction can randomize or dilute the constructor velocity, so vomit particles now call `setParticleSpeed` after creation with the exact old look-vector formula. This fixes the visible center-burst behavior and makes normal/blood/smoke actually travel forward from the entity head.
- 2026-06-04 re-check 2: old client code adds the extra `+1` Y offset for every `EntityPlayer`, not only the local player. Modern `spawnEntityVomit` now applies that offset to all `Player` entities so remote players also vomit from the old player head/eye origin.

边界：

- 现代 normal 呕吐的色块为注册方块近似；旧 `Blocks.stained_hardened_clay` metadata 贴图没有作为独立现代块保留。

验证：

- 2026-05-27 ran `.\gradlew.bat compileJava processResources --no-daemon`: passed.
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

## 2026-05-25 第十九批推进：muke/tinytot 核爆冲击波与客户端 jolt

- 1.7.10 对照：
  - 服务端触发：
    - `ExplosionNukeSmall#explode(...)` 在 `params.particle != null` 时发送 `AuxParticlePacketNT`，`type=muke/tinytot`，范围 250；`muke` 有 1% 概率附带 `balefire=true`。
    - `ExplosionLarge#spawnShock(...)` 是普通烟尘 shock mode，不是小核爆的 `ParticleMukeWave` 入口。
  - 客户端实现：
    - `ClientProxy` 的 `muke` 分支生成 `ParticleMukeWave` 与 `ParticleMukeFlash`，随后设置本地玩家 `hurtTime=maxHurtTime=15`、`attackedAtYaw=0`。
    - `ClientProxy` 的 `tinytot` 分支生成 `ParticleMukeWave` 与较小 mushroom cloud，同样触发 15 tick 屏幕 jolt。
    - `ParticleMukeWave` 使用旧 `textures/particle/shockwave.png`，寿命 25 tick，地面加色扩散环，默认 `waveScale=45`。
- 本批现代迁移：
  - `ParticleUtil` 新增公共核爆视觉入口：
    - `TYPE_MUKE/TYPE_TINY_TOT`
    - `spawnMuke(...)`、`spawnTinyTot(...)`、`spawnNuclearBurstVisual(...)`
  - `HbmParticleEffects` 的 `muke/tinytot` 分发改用常量匹配，并补回：
    - `MukeWaveParticle.create(..., 45, 25)`，复用旧 `shockwave.png` 粒子图集；
    - `muke/tinytot` 对应的 stem/ground/mush cloud 数量级；
    - `applyClientJolt(15, 15)`，对齐旧客户端 hurt 动画抖动主体。
  - 现代端仍保留 `Player#hurtDir` 访问边界：1.20 字段受保护，本批不能像旧版一样强制 `attackedAtYaw=0`。
- 边界：
  - 本批只补用户点名的冲击波粒子与屏幕抖动；旧 `ParticleMukeFlash` 的 20 tick flare billboard 与 15 tick 延迟吐云可在后续视觉精修中单独迁入。
  - 大型 MK5/Torex 核爆的 shock cloud 与声波到达后的屏幕 jolt 已由爆炸框架的 `NukeTorexEntity` / `NukeTorexRenderer` 承载，本批不重复在核弹方块层触发。

## 2026-05-25 第十九批验证

- 已运行：`.\gradlew.bat clean compileJava processResources --no-daemon`
- 结果：通过；编译器仅提示部分输入文件使用/覆盖已过时 API。

## 2026-05-25 第二十批推进：muke cloud 帧图、UFO 云、野火云与 gasfire scale

- 1.7.10 对照：
  - `ClientProxy` aux 类型：
    - `ufo`：读取 `motion`，生成 `ParticleMukeCloud`，水平速度为 `randGaussian * motion`，`motionY=0`。
    - `bf`：生成 `ParticleMukeCloudBF`，速度为 0。
    - `gasfire`：读取 `mX/mY/mZ/scale`，生成 `ParticleGasFlame`，`scale <= 0` 时回退 6.5。
  - 旧触发点：
    - `EntityUFO` 在 beam 打击地面时发送两次 `type=ufo` aux，范围 150。
    - `EntityBuilding`、`EntityBoxcar`、`EntityTorpedo`、`EntityDuchessGambit`、`EntityQuackos` 等在生成或特殊效果时发送 `type=bf`。
    - 旧 `ParticleUtil.spawnGasFlame(...)` 不写 `scale`，机器直接发 `gasfire` aux 时可写 `scale`。
  - 旧资源：
    - `textures/particle/explosion.png`
    - `textures/particle/explosion_bf.png`
  - `ParticleMukeCloud` 合同：
    - 使用 5x5 帧图，按 `age * 25 / maxAge` 选帧。
    - `motionY > 0`、`motionY == 0`、`motionY < 0` 分别使用不同寿命与摩擦。
    - `particleScale=3`，fullbright，前 2 tick noClip，onGround 后水平速度额外衰减。
- 本批现代迁移：
  - 复制旧资源：
    - `assets/hbm/textures/particle/explosion.png`
    - `assets/hbm/textures/particle/explosion_bf.png`
  - 新增 `MukeCloudParticle`：
    - 直接绑定旧 5x5 帧图贴图，不拆分到粒子图集。
    - 保留 3.0 quad scale、寿命/摩擦分支、fullbright、前 2 tick 无碰撞和地面速度衰减。
    - 普通与野火分别使用独立 RenderType，避免同批粒子换贴图。
  - `HbmParticleEffects`：
    - `muke/tinytot` 的 mushroom cloud 从 `EX_SMOKE/WITCH` 近似切换为 `MukeCloudParticle`。
    - 新增 `ufo` 和 `bf` aux 分发。
    - `gasfire` 改为读取 `scale` 并直接构造 `GasFlameParticle`；若 sprite 尚未就绪，则退回注册粒子默认路径。
  - `ParticleUtil` 新增公共入口：
    - `TYPE_UFO/TYPE_BALEFIRE_CLOUD`
    - `spawnUfoCloud(...)`、`spawnBalefireCloud(...)`
    - `spawnGasFlame(..., scale)` 重载。
- 边界：
  - `ParticleMukeFlash` 的 flare billboard 尚未补入，本批只补 muke cloud 帧图本体和 `ufo/bf` 两个孤立 aux。
  - `frozen`、`vanish`、`marker` 仍属于客户端状态/HUD 标记库，不在本批粒子效果内迁。

## 2026-05-25 第二十批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-26 第二十一批推进：ParticleMukeFlash 闪光与延迟吐云

- 1.7.10 对照：
  - `com.hbm.particle.ParticleMukeFlash`
    - 绑定 `textures/particle/flare.png`，`getFXLayer() == 3`，20 tick 寿命。
    - 渲染阶段使用加色混合、fullbright、24 个固定随机偏移的 billboard flare。
    - `scale=(age+interp)*3+1`，`alpha=(1-age/maxAge)*0.5`，颜色 `(1.0, 0.9, 0.75)`。
    - `particleAge == 15` 时吐出 mushroom cloud：
      - stem：`d=0..1.8 step 0.1`，水平高斯 `0.05`，垂直 `d + gaussian*0.02`；
      - ground：100 个，`y+0.5`，水平高斯 `0.5`，20% 概率 `motionY=0.02`；
      - mush：75 个，水平高斯 `0.5`，超出半径阈值后减半，垂直公式 `1.8 + (rand*3-1.5)*(0.75-distSq)*0.5 + gaussian*0.02`。
      - `balefire=true` 时改用 `ParticleMukeCloudBF`。
  - `ClientProxy` 的 `muke` 分支只生成 `ParticleMukeWave` + `ParticleMukeFlash`，并触发 15 tick 客户端 hurt jolt；云团由 flash 在第 15 tick 延迟生成。
  - `tinytot` 分支不生成 flash，仍即时生成较小 stem/ground/mush 云团。
- 本批现代迁移：
  - 新增 `MukeFlashParticle`：
    - 直接绑定已有旧资源 `assets/hbm/textures/particle/flare.png`。
    - 复刻 20 tick 寿命、24 个固定偏移 billboard、加色混合、fullbright、颜色/alpha/scale 公式。
    - 第 15 tick 调用 `MukeCloudParticle` 生成普通或 balefire 云团，保留旧版 stem/ground/mush 数量和运动公式。
  - `MukeCloudParticle` 新增 `add(...)` 工具入口，供 `HbmParticleEffects`、`MukeFlashParticle`、`ufo/bf` 分支共用。
  - `HbmParticleEffects#muke`：
    - 非 `tiny` 分支从“立即生成云团”改回旧版“wave + flash，flash 延迟吐云”。
    - 移除上一批临时的 vanilla `EXPLOSION_EMITTER`，避免和旧版 flare 视觉叠加出不属于 1.7.10 的爆心粒子。
  - `HbmParticleEffects#tinytot`：
    - 保持旧版即时小 mushroom cloud，并固定使用普通 `MukeCloudParticle`。
- 边界：
  - 旧版 `ParticleMukeFlash` 在渲染时临时关闭 fog；现代粒子管线未在本批单独接 fog 状态开关，保留加色、fullbright、无剔除和不写深度这几个核心视觉合同。
  - `Player#attackedAtYaw` 对应字段仍受 1.20 访问限制，本批继续只设置 `hurtTime/hurtDuration`。

## 2026-05-26 第二十一批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-30 第二十二批推进：旧 `type=casing` 抛壳器 aux 兼容

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `type=casing` 分支：
    - 读取 `ej/name/pitch/yaw/crouched`。
    - `ej` 通过 `CasingEjector.fromId(...)` 找到旧抛壳器配置。
    - `name` 通过 `SpentCasing.fromName(...)` 找到弹壳材质/模型配置。
    - 按 `ejector.getAmount()` 调用 `ejector.spawnCasing(...)`。
  - `CasingEjector` 合同：
    - 静态 `mappings` 以构造顺序分配 id。
    - 字段包括 `posOffset`、`initialMotion`、`casingAmount`、`randomYaw`、`randomPitch`。
    - 客户端生成时：
      - 初始运动先叠加高斯扰动，再按 pitch/yaw 旋转；
      - 位置偏移按 pitch/yaw 旋转，潜行时 X 偏移归零；
      - `ParticleSpentCasing` 初始旋转为 `degrees(pitch/yaw)`；
      - 旋转动量为 `gaussian*5` / `gaussian*10`；
      - 该路径不启用 smoke。
  - 已核对的旧炮塔抛壳器：
    - `TileEntityTurretChekhov`：`motion=(-0.8,0.8,0)`，`angleRange=(0.1,0.1)`。
    - `TileEntityTurretFriendly`：`motion=(-0.3,0.6,0)`，`angleRange=(0.02,0.05)`。
    - `TileEntityTurretHoward`：运行时改为 `motion=(0.4,0,0)`，`angleRange=(0.02,0.03)`。
    - `TileEntityTurretSentry`：运行时改为 `motion=(0.2,0.2,0)`，`angleRange=(0.01,0.01)`。
  - 旧 `TileMappings#putTurrets()` 顺序会影响这些静态抛壳器的 id；现代端不依赖类加载顺序，改为显式 id。
- 本批现代迁移：
  - 新增 `LegacyCasingEjectors`：
    - 显式登记 `TURRET_CHEKHOV=0`、`TURRET_FRIENDLY=1`、`TURRET_HOWARD=2`、`TURRET_SENTRY=3`。
    - 保留旧运动扰动、pitch/yaw 旋转、位置偏移和潜行 X 偏移合同。
  - `ParticleUtil`：
    - 新增 `TYPE_LEGACY_CASING = "casing"`。
    - 新增 `spawnLegacyCasing(...)`，写入旧 `type=casing` NBT 字段并走 threaded aux 范围 50。
  - `HbmParticleEffects`：
    - `spawnCasing` 同时识别 `casingNT` 和旧 `casing`。
    - `casingNT` 保持第十批已迁移的直接 3D 弹壳粒子路径。
    - `casing` 按 `LegacyCasingEjectors` 展开为一个或多个 `SpentCasingParticle`，后续炮塔迁移可以直接沿用旧包格式。
- 边界：
  - 本批只迁移旧 `type=casing` 的弹壳视觉路径，不迁移炮塔实体、开火逻辑或炮塔 GUI。
  - 旧 `CasingEjector` 的 id 在 1.7.10 中来自静态构造顺序；现代端采用显式常量，后续迁移更多使用 `CasingEjector` 的武器/炮塔时应继续在 `LegacyCasingEjectors` 里追加并记录 id。
  - `TileEntityTurretArty/Jeremy` 在旧版直接调用 `CasingCreator.composeEffect(...)` 生成 `casingNT`，无需走本批 `type=casing` 分支。

## 2026-05-30 第二十二批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 备注：第一次编译曾被工作区并行 damage-resistance 变更的短暂缺口挡住；确认该缺口已补齐后重跑通过。

## 2026-05-30 第二十三批推进：旧 `vanish` / `marker` / `frozen` 客户端状态 aux 兼容

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `type=vanish`：
    - 读取 `ent`，调用 `ClientProxy.vanish(ent)`。
    - `vanish` 表以实体 id 记录 `System.currentTimeMillis() + 2000`，`RenderLivingEvent.Pre` 中 `isVanished(...)` 为真时取消渲染。
  - `ClientProxy#effectNT` 的 `type=marker`：
    - 读取 `color/label/expires/dist`。
    - 写入 `RenderOverhead.queuedMarkers`，坐标为 `BlockPos(x,y,z)`，过期时间为 `now + expires`，`expires <= 0` 表示不按时间过期。
    - `RenderOverhead.renderMarkers(...)` 合并 queued 表后渲染 1x1x1 线框；若超过 `dist` 或超过过期时间则移除；当玩家视线对准标记时追加距离文本。
    - 旧调用点包括 `MachinePWRController`、`MachineICFController` 的多方块错误提示，以及 `ItemModLens` 的矿物/结构标记。
  - `ClientProxy#effectNT` 的 `type=frozen`：
    - 对本地玩家清零水平速度，垂直速度只允许非正值。
    - 清零 `moveForward/moveStrafing`，用于阻止客户端继续输入移动。
- 本批现代迁移：
  - `ParticleUtil`：
    - 新增 `TYPE_VANISH = "vanish"`、`TYPE_MARKER = "marker"`、`TYPE_FROZEN = "frozen"`。
    - 新增 `spawnVanish(...)`、`spawnMarker(...)`、`spawnFrozen(...)` 公共入口，保留旧 NBT 字段名。
  - `HbmParticleEffects`：
    - `vanish` 复用第十一批已有的 `ClientForgeEvents.vanishEntity(...)` 表，保持 2 秒客户端取消 living renderer 合同。
    - `marker` 分发到新 `HbmOverheadMarkers.queue(...)`。
    - `frozen` 对 `LocalPlayer` 清零水平 delta movement、前后/左右 input impulse 与 `zza/xxa`。
  - 新增 `HbmOverheadMarkers`：
    - 保留 queued/active 双表、过期时间、最大距离移除和 label 距离追加语义。
    - 在 `RenderLevelStageEvent.Stage.AFTER_WEATHER` 渲染 1 方块线框和朝向相机的文本。
    - 在客户端 tick 合并/清理，在维度/世界状态清理时清空。
  - `ClientForgeEvents`：
    - 接入 `HbmOverheadMarkers.render/tick/clearAll`。
- 边界：
  - `marker` 本批只迁移旧 `RenderOverhead.renderMarkers(...)` 的 aux 标记路径，不迁热成像 `renderThermalSight(...)`、实体名称牌重写或 `WorldInAJar` action preview。
  - 旧端 `marker` 渲染受 `HbmPlayerProps.enableHUD` 总开关影响；现代端该 HUD 属性系统尚未迁入，本批只受 vanilla `hideGui` 影响，并记录为后续 HUD/玩家属性库耦合点。
  - `frozen` 是客户端瞬时移动钳制，不改变服务端实体状态；后续使用者需要像旧端一样按 tick 持续发送或调用。

## 2026-05-30 第二十三批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 备注：第一次编译曾被工作区并行配方迁移的短暂缺口挡住：
  - `src/main/java/com/hbm/ntm/recipe/GenericMachineRecipe.java:124`：`HbmItemOutput` 找不到 `copy()`。
  - `src/main/java/com/hbm/ntm/recipe/GenericMachineRecipe.java:134`：同上。
  - 确认并行缺口已修正后重跑通过。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/particle/ParticleUtil.java src/main/java/com/hbm/ntm/client/particle/HbmParticleEffects.java src/main/java/com/hbm/ntm/client/ClientForgeEvents.java src/main/java/com/hbm/ntm/client/render/HbmOverheadMarkers.java`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-05-30 补记：现代 sound id snake_case 修正

- 背景：
  - 配方库 `runData` 验证时，现代端声音注册 `block.pipePlaced` 触发 `ResourceLocationException`。
  - 1.20.1 `ResourceLocation` path 不允许大写字符；本工程现代资源命名约定为 snake_case。
- 本批最小修正：
  - `ModSounds.BLOCK_PIPE_PLACED` 注册名从 `block.pipePlaced` 改为 `block.pipe_placed`。
  - `sounds.json` 同步改为：
    - key：`block.pipe_placed`
    - subtitle：`subtitles.hbm.block.pipe_placed`
    - sound resource：`hbm:block/pipe_placed`
  - 资源文件改名为 `src/main/resources/assets/hbm/sounds/block/pipe_placed.ogg`。
- 边界：
  - 这是为解除现代资源 ID 非法导致的 datagen 阻塞；不代表本批迁移了完整声音库或旧音效命名表。

## 2026-05-30 第二十四批推进：通用 `smoke` / `vanillaExt` / `vanillaburst` aux 兼容

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `type=smoke`：
    - `cloud` / `radial` 生成 `ParticleExSmoke`，扩散倍率沿用旧 Java 整数除法：`count / 100`、`count / 150`、`count / 50`。
    - `radialDigamma` 生成 `ParticleDigammaSmoke`，环形速度半径为 `2`，`motionY=0`。
    - `shock` 为固定强度环形 `ParticleExSmoke`；`shockRand` 每个粒子再乘随机 `0..1`。
    - `wave` 在半径 `range` 的环上生成静止 `ParticleExSmoke`，`maxAge=50`。
    - `foamSplash` 在半径 `range` 的环上生成静止 `ParticleFoam`，`maxAge=50`。
  - `ParticleDigammaSmoke`：
    - 贴图为旧 `particleBase`，`maxAge=100+rand(40)`，`noClip=true`，`particleScale=5`。
    - 颜色为红色 `(0.5+rand*0.2, 0, 0)`，每 tick 阻尼 `0.99`，alpha 线性衰减，fullbright。
  - `type=vanillaburst`：
    - 支持 `flame/cloud/reddust/bluedust/greendust/blockdust`。
    - `blockdust` 读取 `block/meta`，速度 Y 额外 `+0.2`，寿命 `50+rand(50)`。
  - `type=vanillaExt`：
    - 支持 `flame/smoke/volcano/cloud/reddust/bluedust/greendust/fireworks/largeexplode/townaura/blockdust/colordust`。
    - `volcano` 是单个超大 smoke：scale `100`，寿命 `200+rand(50)`，`noClip=true`，向上速度 `2.5+rand`，水平高斯 `0.2`。
    - 带 `r/g/b` 的 `cloud` 会改色、scale `7.5` 并清零速度。
    - `largeexplode` 是一个暖色主 `EntityLargeExplodeFX` 加 `count` 个灰色次级 `EntityExplodeFX`，次级按序放大。
    - `townaura` 是淡蓝白 aura，并保留传入速度。
    - `blockdust` / `colordust` 寿命 `10+rand(20)`，速度 Y 额外 `+0.2`；`colordust` 使用白羊毛 blockdust 再染色。
    - 最后可应用 `noclip` 与 `overrideAge`。
- 本批现代迁移：
  - 新增 `DIGAMMA_SMOKE` 粒子类型、`digamma_smoke.json`、`DigammaSmokeParticle` provider：
    - 复刻旧 `ParticleDigammaSmoke` 的红色、scale 5、100-139 tick、0.99 阻尼、无物理碰撞与 fullbright。
    - `smoke/radialDigamma` 改为生成该粒子，不再用临时 `WITCH` 或普通 `EX_SMOKE` 近似。
  - `HbmParticleEffects#spawnSmoke`：
    - `cloud/radial` 扩散恢复旧整数除法语义。
    - `shock` 与 `shockRand` 分离，`shockRand` 保留逐粒子随机强度。
    - `wave` / `foamSplash` 按旧半径环生成，并把寿命设为 50；修正现代 `ParticleEngine#createParticle(...)` 已自动入队导致的重复添加风险。
  - `HbmParticleEffects#spawnVanillaBurst`：
    - `blockdust` 改为 `TerrainParticle`，保留 `block` id、Y 速度 `+0.2` 与 50-99 tick 寿命。
  - `HbmParticleEffects#spawnVanillaExt`：
    - `volcano` 改为单个超大 `HbmSmokeParticle`，保留旧 scale、寿命和速度合同。
    - `cloud+r/g/b`、`blockdust`、`colordust` 增加专门创建逻辑，支持旧寿命/染色/速度语义。
    - `largeexplode` 改为主 `EXPLOSION` 粒子加若干灰色次级 `POOF`，保留暖色主爆与次级逐级放大意图。
    - `overrideAge` 对可直接创建的现代粒子生效。
  - `ParticleUtil`：
    - 新增 `TYPE_SMOKE`、`TYPE_VANILLA_EXT`、`TYPE_VANILLA_BURST` 与旧 mode 常量。
    - 新增 `spawnSmoke(...)`、`spawnSmokeShock(...)`、`spawnSmokeRing(...)`、`spawnVanillaBurst(...)`、`spawnVanillaBlockDustBurst(...)`、`spawnVanillaExt(...)`、`spawnVanillaExtLargeExplode(...)`、`spawnVanillaExtColoredCloud(...)`、`spawnVanillaExtBlockDust(...)`、`spawnVanillaExtColorDust(...)`。
    - `ExplosionLarge` 与 `ExplosionEffectTiny` 现有手写 aux 包改为走这些公共 helper。
- 边界：
  - 现代 `BlockState` id 无法完整表达 1.7.10 `block + meta` 的所有状态，本批保留 `block` id 主体，`meta` 暂记录为后续 legacy blockstate 映射表需求。
  - `noclip` 对 vanilla 粒子没有统一公开入口；本批在自定义 smoke/digamma/地形粒子路径保留无物理或可控寿命，普通 vanilla 分支仍按现代 provider 默认行为。
  - `largeexplode` 仍受现代 vanilla 粒子 provider 限制，不能逐字段完全复制 1.7.10 `EntityLargeExplodeFX` 的内部贴图帧和私有 scale；本批保留主/次爆颜色、数量与放大关系。

## 2026-05-30 第二十四批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-05-31 第二十五批推进：`block/meta` 粒子状态兼容层

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `vanillaburst/blockdust` 读取 `block` 与 `meta`，用 `Block.getBlockById(...)` + `EntityBlockDustFX(..., b, m)`，寿命 `50+rand(50)`，Y 速度额外 `+0.2`。
  - `ClientProxy#effectNT` 的 `vanillaExt/blockdust` 读取 `block`，旧接收端固定 meta `0`；部分旧调用点仍会写入 `meta`，例如 `XFactoryTool` 的硼砂弹药。
  - `ClientProxy#effectNT` 的 `sweat` 读取 `entity/block/meta`，在实体包围盒内生成 `EntityBlockDustFX(..., b, meta)`，寿命 `150+rand(50)`。
  - 已核对旧调用点：红石块碎屑用于锯床/自动锯/弹丸/Crucible，铁块碎屑用于 Ore Slopper，水/foam/sand_mix 用于 `XFactoryTool`，红石块/灵魂沙/煤块用于实体长期状态，羊毛用于 `EntityPigeon`。
- 本批现代迁移：
  - `ParticleUtil` 的现代 helper 不再把 `Block.getId(defaultBlockState)` 写入旧字段 `block`，改写明确的 `state` 字段。
  - `spawnSweat`、`spawnVanillaBlockDustBurst`、`spawnVanillaExtBlockDust` 新增 `BlockState` 重载，后续迁移具体方块状态时可直接传现代状态。
  - `HbmParticleEffects` 对 `vanillaburst/blockdust`、`vanillaExt/blockdust`、`sweat` 统一调用 `blockStateFromParticleData(...)`：
    - 优先读取现代 `state`。
    - 若无 `state`，按旧包的 `block/meta` 映射 1.7.10 vanilla 方块 id 到现代 `BlockState`。
  - 已补常用 legacy vanilla id/meta 映射：石头/草方块/泥土变体、木板/原木/树叶、沙/红沙、羊毛 16 色、铁块/红石块/煤块、灵魂沙、水/岩浆、陶瓦 16 色、石砖/砂岩/石英变体等。
- 边界：
  - 旧 HBM 自定义方块 id 是运行时注册 id，不能从现代端可靠反查。本批不伪造动态 id 表；已迁现代调用应使用 `state` 字段或 helper 的 `BlockState` 重载。
  - `XFactoryTool` 旧 `block_foam` / `sand_mix` 原始 `block/meta` 包在现代迁移该武器时需要改走现代 helper；若未来要兼容录制/回放的旧原始包，再建立明确的 HBM legacy block id 表。

## 2026-05-31 第二十五批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/client/particle/HbmParticleEffects.java src/main/java/com/hbm/ntm/particle/ParticleUtil.java 工程记录/库移植/2026-05-20-sound-particle-effects-library-1.7.10源码功能追踪.md`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-05-31 第二十六批推进：HUD/player props 门控与 legacy blockstate 映射抽出

- 1.7.10 对照：
  - `HbmPlayerProps` 默认 `enableHUD = true`，按键 `EnumKeybind.TOGGLE_HEAD` 切换，并发送 `PlayerInformPacket("HUD ON/OFF", ID_HUD=7, 1000)`。
  - `ModEventHandlerClient` 在渲染 `RenderOverhead.renderMarkers(...)`、热成像 HUD 等前读取 `HbmPlayerProps.getData(player).enableHUD`。
  - 旧 `ClientProxy#effectNT` 的 `vanillaburst/blockdust`、`vanillaExt/blockdust`、`sweat` 依赖 `block/meta`；HBM 自定义方块旧 id 是运行时动态 id，不能直接在现代端可靠复原。
- 本批现代迁移：
  - 新增最小 `HbmPlayerProperties` / `ClientHbmPlayerProperties`：
    - 服务端持久化 `hbm_player_props.enableHUD`，默认开启。
    - `TOGGLE_HEAD` 按键触发 HUD 开关，并保留旧通知 id `7` 与 1000ms 显示时间。
    - 登录、换维度、死亡克隆时同步到客户端。
  - `ClientForgeEvents` 与 `HbmOverheadMarkers` 改为经 `ClientHbmPlayerProperties.shouldRenderHud()` 门控；隐藏原版 GUI 或关闭 HBM HUD 时不渲染现代共享 HUD/marker。
  - 新增 `LegacyBlockStateMappings`：
    - 从 `HbmParticleEffects` 抽出旧 `block/meta` 到现代 `BlockState` 的映射，成为 common-side 可复用入口。
    - 优先解析现代 `state`，再解析 `blockName` / `legacyBlock` / `legacyBlockName` / `block_name` / `legacy_block`，最后兼容旧 numeric `block/meta` vanilla id。
    - 保留常用 1.7.10 vanilla meta：石头变体、泥土变体、木板/原木/树叶、沙/红沙、羊毛、陶瓦、石砖、砂岩、石英、染色玻璃等。
    - HBM 旧名通过 `ModBlocks.legacyBlock(...)` 解析；`sellafield` / `sellafield_slaked` / `sellafield_bedrock` / `ore_sellafield_*` 的 `meta` 写入现代 `level` 属性，`fallout` 的 `meta & 7` 映射为现代 layer 数。
  - `ParticleUtil`：
    - `BlockState` helper 统一使用 `LegacyBlockStateMappings.putState(...)` 写 `state`。
    - 新增 legacy name helper：`spawnSweat(..., String legacyBlockName, int meta, ...)`、`spawnVanillaLegacyBlockDustBurst(...)`、`spawnVanillaExtLegacyBlockDust(...)`。
- 边界：
  - 旧 HBM 自定义方块 numeric id 仍不做猜测映射；后续爆炸、武器、机器迁移应优先发现代 `state`，或发明确的 `blockName/meta`。
  - `sand_mix` / `sand_boron_layer` 当前现代端尚未有完整对应方块；legacy name 解码暂退到 `minecraft:sand` 作为粒子视觉兜底，具体方块本体留给对应方块迁移批次。
  - 本批只迁入 `HbmPlayerProps.enableHUD` 对声音/粒子/HUD 所需的最小桥接，不替代完整 extprop living/player 数据库。

## 2026-05-31 第二十六批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 备注：第一次编译被工作区并行 `ModCommands` fluid anchor 子命令的多余右括号阻塞；已做一行括号收尾修正后重跑通过。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/player/HbmPlayerProperties.java src/main/java/com/hbm/ntm/client/ClientHbmPlayerProperties.java src/main/java/com/hbm/ntm/network/HbmServerKeybinds.java src/main/java/com/hbm/ntm/event/CommonForgeEvents.java src/main/java/com/hbm/ntm/client/ClientForgeEvents.java src/main/java/com/hbm/ntm/client/render/HbmOverheadMarkers.java src/main/java/com/hbm/ntm/client/particle/HbmParticleEffects.java src/main/java/com/hbm/ntm/particle/ParticleUtil.java src/main/java/com/hbm/ntm/particle/LegacyBlockStateMappings.java src/main/java/com/hbm/ntm/command/ModCommands.java 工程记录/库移植/2026-05-20-sound-particle-effects-library-1.7.10源码功能追踪.md`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-02 第二十七批推进：旧导弹/爆炸尾迹与 MK1 云粒子合同补齐

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `waterSplash`：生成 10 个 `EntityCloudFX`，位置为 `x/y/z + gaussian`，速度固定为 `0/0/0`。
  - `cloudFX2`：生成 1 个 `EntityCloudFX`，速度固定为 `0/0.1/0`。
  - `ABMContrail`：创建默认 `ParticleContrail`，默认颜色基底 `0/0/0`、`maxAge=100+rand(40)`、scale `1`。
  - `exKerosene` / `exSolid` / `exHydrogen` / `exBalefire`：创建 `ParticleContrail`，颜色基底分别为黑、`0.3/0.2/0.05`、`0.7/0.7/0.7`、`0.2/0.7/0.2`，scale `1`。
  - `missileContrail`：本地玩家距离超过 350 格直接跳过；读取 `moX/moY/moZ`、可选 `scale` 默认 `1`、可选 `maxAge`，最终只创建 `ParticleRocketFlame`；旧 `ParticleContrail` 代码块已被注释。
- 本批现代迁移：
  - 新增 `LegacyContrailParticle`：
    - 使用现有 `hbm:contrail` 贴图。
    - 复刻旧 `ParticleContrail` 的 6 层 billboard、`100+rand(40)` 寿命、fullbright、颜色基底加 `0.2..0.4` 随机偏移、alpha 线性衰减、scale `alpha + 0.5`。
    - provider 替换 `ModParticleTypes.CONTRAIL` 原来的 `HbmSmokeParticle.ContrailProvider`。
  - `HbmParticleEffects`：
    - `waterSplash` 改为零速度 cloud burst，不再使用带随机速度的通用 `burstSimple`。
    - `ABMContrail` 与 `ex*` 改为生成 `LegacyContrailParticle`，不再用 `SMOKE_PLUME` 或额外 `DustParticleOptions` 近似。
    - `missileContrail` 恢复 350 格客户端距离裁剪，并通过 `RocketFlameParticle.createLegacy(...)` 支持旧 `scale/maxAge`；不再额外叠加普通 contrail。
  - `RocketFlameParticle`：
    - 缓存 provider sprites，提供旧式创建入口，以便分发层直接设置构造期 scale 与寿命。
  - `ParticleUtil`：
    - 新增旧 type 常量：`waterSplash`、`cloudFX2`、`launchSmoke`、`missileContrail`、`ABMContrail`、`exKerosene`、`exSolid`、`exHydrogen`、`exBalefire`。
    - 新增公共 helper：`spawnWaterSplash`、`spawnCloudFx2`、`spawnLaunchSmoke`、`spawnMissileContrail`、`spawnAbmContrail`、`spawnExKerosene`、`spawnExSolid`、`spawnExHydrogen`、`spawnExBalefire`，字段名沿用旧 `moX/moY/moZ/scale/maxAge`。
- 边界：
  - 本批迁移的是旧 aux 粒子接收端与公共发包 helper，不迁移导弹实体、燃料系统或发射逻辑本身。
  - `ParticleSmokePlume` 仍作为 `launchSmoke` / `SMOKE_PLUME` 使用；`CONTRAIL` 类型现在专门回到旧 `ParticleContrail` 视觉合同。
  - 旧 OpenGL 颜色超过 `1.0` 的情况在现代顶点色中做了钳制，避免依赖底层实现。

## 2026-06-02 第二十七批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-06-02 第二十八批推进：旧 `exhaust` 与 `fireworks` aux 合同收紧

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `type=exhaust`：
    - `mode=soyuz`：若本地玩家到粒子坐标距离超过 350 格则直接返回；读取 `count/width`，每个粒子生成 `ParticleRocketFlame(x + gaussian*width, y, z + gaussian*width)`，`motionY = -0.75 + rand*0.5`。
    - `mode=meteor`：同样做 350 格距离裁剪；每个粒子生成 `ParticleRocketFlame(x + gaussian*width, y + gaussian*width, z + gaussian*width)`，不额外设置 motion。
  - `ClientProxy#effectNT` 的 `type=fireworks`：
    - 读取 `color` 与 `char`。
    - 生成 `ParticleLetter`，再生成 50 个 `EntityFireworkSparkFX`，每个火花速度为 `0.4 * gaussian`，并调用 `setColour(color)`。
  - `EntityFireworks#onUpdate`：
    - 旧实体 30 tick 后播放 `fireworks.blast`，发 `type=fireworks` aux，范围为 300。
- 本批现代迁移：
  - `HbmParticleEffects#spawnExhaust`：
    - 恢复 350 格客户端距离裁剪。
    - `soyuz` 只生成 `ROCKET_FLAME`，位置和 `motionY` 按旧合同；去掉上一版额外叠加的 vanilla flame。
    - `meteor` 只生成 `ROCKET_FLAME`，位置三轴高斯扩散且 motion 为 `0/0/0`。
  - `HbmParticleEffects#spawnFireworks`：
    - 保留现代 `FireworkLetterParticle`。
    - 50 个 `ParticleTypes.FIREWORK` 改为通过 `ParticleEngine#createParticle(...)` 创建后按旧 `color` 拆 RGB 写入，恢复旧 `setColour(color)` 语义。
  - `ParticleUtil`：
    - 新增 `TYPE_EXHAUST = "exhaust"`、`EXHAUST_SOYUZ = "soyuz"`、`EXHAUST_METEOR = "meteor"`。
    - 新增 `spawnExhaustSoyuz(...)`、`spawnExhaustMeteor(...)`、`spawnExhaust(...)` helper。
    - `spawnFireworks(...)` 的发送范围从 150 调整为旧实体使用的 300。
- 边界：
  - 本批迁移 aux 粒子接收端和发包 helper，不迁移 `EntityFireworks` 实体、烟花方块、音效播放或发射逻辑。
  - `vanillaExt/mode=fireworks` 仍保持旧单个 firework spark 的通用 vanillaExt 分支；本批只处理顶层 `type=fireworks` 的字母烟花爆开效果。

## 2026-06-02 第二十八批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 备注：编译输出提示部分输入文件使用/覆盖过时 API，未阻塞构建。

## 2026-06-02 第二十九批推进：旧 `radiation` / `schrabfog` aura 与 `radFog` 分流

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `type=radFog` 创建 `ParticleRadiationFog(man, world, x, y, z)`。
  - `ClientProxy#effectNT` 的 `type=radiation`：读取 `count`，在本地玩家周围 `x + gaussian*4`、`y + gaussian*2`、`z + gaussian*4` 生成 `EntityAuraFX`，颜色为 `(0F, 0.75F, 1F)`，速度为三轴 gaussian。
  - `EntityEffectHandler` 在玩家辐射值超过 600 时发送 `type=radiation`，`count` 按 600/800/900 阈值为 1/2/4。
  - `ClientProxy#effectNT` 的 `type=schrabfog` 在包坐标生成 `EntityAuraFX`，速度为 0，颜色为 `(0F, 1F, 1F)`。
  - `SchrabidicBlock#randomDisplayTick` 在方块周围随机偏移坐标发送 `type=schrabfog`。
- 本批现代迁移：
  - 新增 `LegacyAuraParticle`，复用 `TownAuraParticle` 的 sprite provider，提供可指定 RGB 的旧 aura 粒子入口。
  - `TownAuraParticle` 暴露 provider 缓存的 `SpriteSet`，供旧 aura 变体复用同一纹理集。
  - `HbmParticleEffects`：
    - `radiationfog` / `radiation_fog` / `radFog` 明确保留为 `ModParticleTypes.RADIATION_FOG`。
    - `schrabfog` 改走青色 `LegacyAuraParticle`，不再混用 RadiationFog。
    - `radiation` 改为围绕本地玩家生成蓝色 legacy aura，并保留旧 count、位置扩散和 gaussian 速度合同。
  - `ParticleUtil`：
    - 新增 `TYPE_RADIATION_FOG`、`TYPE_RADIATION_FOG_SNAKE`、`TYPE_RAD_FOG`、`TYPE_SCHRAB_FOG` 常量。
    - 新增 `spawnRadiationFog(...)` 与 `spawnSchrabFog(...)` helper，后续辐射、流体和方块迁移可直接复用。
- 边界：
  - `LegacyAuraParticle` 只迁移旧 `EntityAuraFX` 在 HBM 接收端实际使用到的颜色/位置/运动合同，不尝试替代所有 vanilla aura 行为。
  - `SchrabFogParticle` 暂保留注册和类本体，避免影响已有或并行迁移中的直接粒子调用；本批仅修正 legacy aux `type=schrabfog` 的分发语义。

## 2026-06-02 第二十九批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-06-02 第三十批推进：旧 jetpack/bnuuy 粒子设置档位收紧

- 1.7.10 对照：
  - `ClientProxy#effectNT` 在 `jetpack`、`bnuuy`、`jetpack_bj`、`jetpack_dns` 开头检查 `particleSetting == 2`，最低粒子设置直接跳过。
  - `jetpack`：
    - 任意非最低粒子设置都生成左右两侧火焰。
    - 只有 `particleSetting == 0` 时才做地面方块尘 ray trace，并额外生成左右两侧 smoke。
  - `bnuuy`：
    - 任意非最低粒子设置生成左右两侧 smoke，旧代码把 `EntitySmokeFX` scale 设为 `0.5F`。
  - `jetpack_bj` / `jetpack_dns`：
    - 任意非最低粒子设置生成左右两侧染色 red dust。
    - 只有 `particleSetting == 0` 时才做地面方块尘 ray trace。
- 本批现代迁移：
  - `HbmParticleEffects` 新增旧粒子设置档位桥接：
    - `ParticleStatus.MINIMAL` 对应旧 `particleSetting == 2`，直接跳过喷射背包类粒子。
    - `ParticleStatus.ALL` 对应旧 `particleSetting == 0`，启用地面方块尘与 jetpack 额外 smoke。
    - `ParticleStatus.DECREASED` 保留火焰、bnuuy smoke、BJ/DNS 染色 dust，但不生成地面方块尘和 jetpack 额外 smoke。
  - `bnuuy` 改用 `ParticleEngine#createParticle(...)` 创建后缩放，恢复旧 `scale=0.5F` 的小烟雾视觉。
  - `jetpack_bj` / `jetpack_dns` 的地面方块尘现在只在最高粒子质量下生成，避免低粒子设置仍大量出尘。
- 边界：
  - 本批只迁移旧 aux 接收端的粒子设置门控和 bnuuy smoke 缩放，不迁移背包物品、飞行物理或装备逻辑。
  - `vanillaExt` 的 `noclip` 已在第二十四批记录为现代 vanilla 粒子无统一公开入口，本批不引入反射 workaround。

## 2026-06-02 第三十批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 备注：编译输出提示部分输入文件使用/覆盖过时 API，未阻塞构建。

## 2026-06-03 第三十一批推进：旧 `ExplosionCreator` / `ExplosionSmallCreator` 视觉合同收紧

- 1.7.10 对照：
  - `ExplosionCreator#makeParticle`：
    - 读取 `cloudCount/cloudScale/cloudSpeedMult/waveScale/debrisCount/debrisSize/debrisRetry/debrisVelocity/debrisHorizontalDeviation/debrisVerticalOffset/soundRange`。
    - 按玩家距离和旧 `speedOfSound = 17.15 * 0.5` 延迟播放 `weapon.explosionLargeNear/Far`，现代端此前已接入。
    - 在 `(x, y + 2, z)` 生成 `ParticleMukeWave`，`setup(waveScale, (int)(25F * waveScale / 45))`。
    - 每个烟柱粒子是 `ParticleRocketFlame(x,y,z).setScale(cloudScale)`，速度为水平 `gaussian*0.5*cloudSpeedMult`、垂直 `rand*3*cloudSpeedMult`，寿命 `70+rand(20)`，`noClip=true`。
    - debris 是 `ParticleDebris` + `WorldInAJar` 旧碎块实体，当前仍未深迁。
  - `ExplosionSmallCreator#makeParticle`：
    - 按玩家距离和同一声速延迟播放 `weapon.explosionSmallNear/Far`，现代端此前已接入。
    - 生成 `cloudCount` 个 `ParticleExplosionSmall`。
    - 从爆点六向寻找第一个非 air 方块，生成 `debris` 个 `EntityBlockDustFX`，位置 `(x, y+0.1, z)`，速度 `gaussian*0.2 / 0.5+rand*0.7 / gaussian*0.2`，调用 `multipleParticleScaleBy(2)`，寿命 `50+rand(20)`。
- 本批现代迁移：
  - `HbmParticleEffects#spawnExplosionLarge`：
    - 烟柱从默认 `ROCKET_FLAME + EX_SMOKE` 组合收紧为单个旧式 `RocketFlameParticle.createLegacy(...)`。
    - 保留旧 `cloudScale` 与 `70-89 tick` 寿命；provider 不可用时才退回注册粒子。
  - `HbmParticleEffects#spawnExplosionSmall`：
    - debris 从不可控默认 `BlockParticleOption` 改为直接创建 `TerrainParticle`。
    - 恢复旧 `scale=2.0F` 与 `50-69 tick` 寿命，速度和位置沿用旧合同。
- 边界：
  - 大爆炸旧 `ParticleDebris` / `WorldInAJar` 碎块实体仍未深迁，本批只收紧可由现代粒子系统安全表达的烟柱与小爆炸 blockdust 视觉。
  - 旧 `ExplosionCreator` 的 `debrisSize/debrisRetry/debrisVelocity/debrisHorizontalDeviation/debrisVerticalOffset` 字段仍保留为后续深迁记录，现代端暂未完整消费。

## 2026-06-03 第三十一批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 备注：文档更新后复跑同一命令时，被并行 GUI/look-overlay 迁移的 `LegacyLookOverlayProvider` 包名与 `LegacyLookOverlayLines` 组件拼接错误阻塞；该失败不来自本批声音粒子文件。本批相关文件的 `git diff --check` 通过，仅有 Git 对 CRLF 的提示。

## 2026-06-03 第三十二批推进：旧 `tower` / `splash` / `deadleaf` 粒子设置门控

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `type=tower` 只在 `particleSetting == 0`，或 `particleSetting == 1 && rand.nextBoolean()` 时创建 `ParticleCoolingTower`。
  - 旧 `tower` 的寿命写入为 `life / (particleSetting + 1)`，所以低一级粒子质量会同时减少出现概率和单个云柱寿命。
  - `type=splash` 使用同样的可见性门控，颜色存在时再乘 `1 - rand.nextFloat() * 0.2F` 的轻微暗化。
  - `type=deadleaf` 使用同样的可见性门控；最低粒子设置完全跳过。
- 本批现代迁移：
  - `HbmParticleEffects` 新增 `legacyVisibleParticleSetting(...)`：
    - `ParticleStatus.ALL` 对应旧 `particleSetting == 0`，总是显示。
    - `ParticleStatus.DECREASED` 对应旧 `particleSetting == 1`，随机半量显示。
    - `ParticleStatus.MINIMAL` 对应旧 `particleSetting == 2`，直接跳过。
  - `spawnCoolingTower(...)` 接入旧门控，并按 `particleSettingDivisor()` 对 `life` 做旧式寿命缩短。
  - `spawnSplash(...)` 接入旧门控，保留已有 legacy splash 粒子本体。
  - `deadleaf` 分发接入旧门控，避免最低粒子设置仍持续落叶。
- 边界：
  - 本批只收紧旧 aux 接收端的粒子设置合同，不改变 `CoolingTowerParticle` / `LegacySplashParticle` / `DeadLeafParticle` 的渲染几何。
  - `splash` 的颜色随机暗化仍由 `LegacySplashParticle.create(...)` 侧承载；若后续视觉精修发现该类未覆盖，可在粒子类本体补齐。

## 2026-06-03 第三十三批推进：旧 `tau` / `giblets` / RBMK 粒子合同收紧

- 1.7.10 对照：
  - `ClientProxy#effectNT` 的 `type=tau`：
    - 读取 `small` 与 byte `count`。
    - 每个火花为 `ParticleSpark(x,y,z, gaussian*0.05, 0.05, gaussian*0.05).makeSmall(small)`。
    - 额外生成一个 `ParticleHadron(...).makeSmall(small)`。
  - `ParticleSpark`：
    - 普通寿命 `20+rand(10)`，轨迹缓存 `4+rand(3)` 段，重力 `0.5`，fullbright 白色线段，无 alpha 衰减。
    - small 模式寿命 `2+rand(3)`，轨迹缓存 3 段，`motionY=-abs(motionY)`。
  - `type=giblets`：
    - 先调用 `vanish(ent)` 隐藏原实体 2000ms。
    - 数量为 `(int)(width / 0.25F) * 1.5 * (int)(height / 0.25F)`；存在 `cDiv` 时向上取整除以 `cDiv`，旧端没有额外固定上限。
    - `1/15` 概率把所有碎块速度乘以 10。
  - `ParticleGiblet`：
    - 寿命 `140+rand(20)`，普通/黏液重力 2，金属重力 4。
    - 非金属碎块飞行中每 tick 生成红石块或西瓜块 blockdust 尾尘，尾尘寿命 `20+rand(20)`。
  - `rbmkflame/rbmksteam/rbmkmush` 当前现代类已经保留旧贴图、寿命、帧数和加色混合主体合同，本批仅审计，不做额外改动。
- 本批现代迁移：
  - `TauSparkParticle` 去掉现代端额外 alpha 衰减，恢复旧 `ParticleSpark` fullbright 不透明白线段。
  - `HbmParticleEffects#spawnGiblets(...)` 去掉额外 `256` 数量上限，回到旧实体尺寸与 `cDiv` 驱动的数量模型；若计算结果小于等于 0 则直接跳过。
  - `GibletParticle` 的非金属尾尘改为直接创建 `TerrainParticle`，显式设置 `20+rand(20)` 寿命，恢复旧 `ReflectionHelper` 写 `particleMaxAge` 的效果。
- 边界：
  - 本批不迁移新的实体死亡触发点，只收紧已经存在的 aux 接收端与公共 helper 行为。
  - RBMK 三类粒子仍维持当前现代端实现；后续若做视觉截图审计，可再检查 `ParticleRBMKSteam` 旧 `texIndex` 起始帧为 `-1` 的历史边界是否需要像素级复刻。

## 2026-06-03 第三十四批推进：旧 `splash` / `deadleaf` UV 翻转精修

- 1.7.10 对照：
  - `ParticleSplash#renderParticle(...)` 使用 `getEntityId()` 做贴图翻转：
    - `flipU = getEntityId() % 2 == 0`。
    - `flipV = getEntityId() % 4 < 2`。
    - 四个顶点分别使用翻转后的 `minU/maxU/minV/maxV`，让同一水花贴图在大量粒子中产生 4 种朝向变化。
  - `ParticleDeadLeaf#renderParticle(...)` 使用同一套 `getEntityId()` U/V 翻转逻辑。
  - 两者其他主体合同此前已迁入：水花 alpha/scale/lifetime/gravity/落地消失，落叶灰白随机色/scale/lifetime/gravity/横向漂移与下落封顶。
- 本批现代迁移：
  - `LegacySplashParticle` 新增旧式 visual id 分配，按 id 奇偶和模 4 保存 `flipU/flipV`。
  - `LegacySplashParticle#render(...)` 改为自定义 billboard 顶点写入，保留当前 camera-facing 粒子行为，同时按旧 U/V 翻转输出。
  - `DeadLeafParticle` 同样新增 visual id 与自定义 billboard 渲染，恢复旧落叶贴图翻转变化。
- 边界：
  - 现代端没有直接暴露旧 `EntityFX#getEntityId()`；本批使用客户端本地递增 visual id 复刻同样的奇偶分布，保持视觉合同而不绑定实体系统。
  - 本批只精修贴图朝向，不改变两类粒子的物理、寿命、颜色或粒子设置门控。

## 2026-06-03 第三十四批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/client/particle/LegacySplashParticle.java src/main/java/com/hbm/ntm/client/particle/DeadLeafParticle.java`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-03 第三十五批推进：旧旋转粒子 id 奇偶与灰烬落地烟

- 1.7.10 对照：
  - `EntityFXRotating#renderParticleRotated(...)` 使用 `rotationPitch` 作为绕 billboard 法线的旋转角。
  - `ParticleAshes#onUpdate(...)`：
    - 空中每 tick 旋转增量为 `2 * ((getEntityId() % 2) - 0.5)` 度，即按粒子 id 奇偶决定顺/逆时针。
    - 落地瞬间把 `rotationPitch` 随机到 `0..360`。
    - 当 `getEntityId() % 5 == 0`、已经落地且 `rand.nextInt(15) == 0` 时，在 `posY + 0.125` 生成 vanilla `smoke`，速度 `0/0.05/0`。
  - `ParticleBlackPowderSmoke#onUpdate(...)`：
    - 旋转增量为 `(1-ageScaled) * 2 * ((getEntityId() % 2) - 0.5)`。
  - `ParticleExplosionSmall#onUpdate(...)`：
    - 旋转增量为 `(1-ageScaled) * 5 * ((getEntityId() % 2) - 0.5)`。
- 本批现代迁移：
  - `AshesParticle` 新增旧式 visual id：
    - 空中 `rollSpeed` 按 visual id 奇偶决定，复刻旧 `getEntityId()` 的旋转方向分布。
    - 保留落地瞬间随机 roll，并补回 `visualId % 5 == 0` 落地后 `1/15` 概率冒 smoke 的旧细节。
  - `BlackPowderSmokeParticle` 新增 visual id，旋转方向从随机改为按 id 奇偶决定，角速度仍等价旧 `±1 degree/tick` 再乘 `(1-ageScaled)`。
  - `ExplosionSmallParticle` 新增 visual id，旋转方向从随机改为按 id 奇偶决定，角速度等价旧 `±2.5 degree/tick` 再乘 `(1-ageScaled)`。
- 边界：
  - 现代端继续使用本地递增 visual id 复刻旧 `EntityFX#getEntityId()` 的奇偶分布，不引入实体依赖。
  - 本批不改变黑火药/小爆炸烟的颜色、缩放、寿命和运动曲线；只收紧旋转方向来源，并补灰烬落地 smoke。

## 2026-06-03 第三十五批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/client/particle/AshesParticle.java src/main/java/com/hbm/ntm/client/particle/BlackPowderSmokeParticle.java src/main/java/com/hbm/ntm/client/particle/ExplosionSmallParticle.java`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-03 第三十六批推进：旧 `amat` 闪光与 `radFog` 累积几何收紧

- 1.7.10 对照：
  - `ParticleAmatFlash`：
    - `particleMaxAge=10`，`particleScale=scale`。
    - 每帧使用 `Random(432L)`，生成 100 条光束。
    - 每条光束前连续调用 5 次 `GL11.glRotatef(...)`，没有在每条光束之间重置矩阵，因此旋转会逐条累积。
    - 全局缩放为 `0.2F * particleScale`；每条光束用 `GL_TRIANGLE_FAN` 从中心点连到 `y=vert1` 处的 3 个远端截面点，并重复第一个远端点闭合。
  - `ParticleRadiationFog`：
    - 使用 `textures/particle/fog.png`，构造时 `maxAge=100+rand(40)`，`onUpdate` 中若小于 400 则强制到 400。
    - alpha 为 `sin(particleAge * PI / 400F) * 0.125F`，颜色固定 `0.85/0.9/0.5`。
    - 每帧使用 `Random(50)` 生成 25 张雾片。
    - 循环内每张雾片先 `GL11.glTranslatef(dX,dY,dZ)`，因此位移是累积矩阵变换，而不是每张雾片独立偏移。
- 本批现代迁移：
  - `AmatFlashParticle`：
    - 光束循环不再对 `Quaternionf` 做逐条 `identity()` 重置，恢复旧 GL 矩阵的累积旋转分布。
    - 远端三角截面改回位于 `length` 处，并由中心点发散到 3 个远端点，贴近旧 triangle fan 的白心透明尾结构。
  - `RadiationFogParticle`：
    - 渲染 25 张雾片时累加 `cumulativeX/Y/Z`，恢复旧 `glTranslatef` 累积偏移。
    - 保持旧固定 seed、固定颜色、400 tick alpha 曲线和 fullbright 渲染。
- 边界：
  - `ParticleAmatFlash` 旧版会临时关闭纹理、启用 smooth shade 和 additive blend；现代端仍使用已有自定义 `ParticleRenderType` 表达核心 additive/fullbright/no-cull/不写深度合同，不逐项复刻所有 GL 状态。
  - `RadiationFogParticle` 的现代资源仍走粒子图集贴图路径，几何与 alpha/color 合同已按旧类收紧。

## 2026-06-03 第三十六批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/client/particle/AmatFlashParticle.java src/main/java/com/hbm/ntm/client/particle/RadiationFogParticle.java`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-03 第三十七批推进：旧 `gasfire` 烟雾缩放与喷火器旋转奇偶

- 1.7.10 对照：
  - `ParticleGasFlame`：
    - 继承 `EntitySmokeFX`，构造时把 `mY` 放大 1.5 倍，`particleScale=scale`，`noClip=true`。
    - `colorMod=0.8F+rand(0.2F)`，寿命 `30+rand(13)`。
    - 每 tick 保存旧 `motionY`，执行父类 smoke 更新后恢复 `motionY`，随后 `motionX/Z *= 0.75D`、`motionY += 0.005D`。
    - `updateColor()` 使用 HSB：色相 `(60-time*100)/360` 下限 0，饱和 `1-time*0.25`，亮度 `1-time*0.5`，再乘 `colorMod`，亮度 fullbright。
    - 旧父类 `EntitySmokeFX` 使用 `smokeParticleScale` 做起始烟雾膨胀曲线；旧 HBM 在其他 smoke 分支中也通过反射写 `smokeParticleScale`。
  - `ParticleFlamethrower`：
    - 寿命 `20+rand(10)`，基础 scale `0.5F`，水平随机速度 `gaussian*0.02`。
    - 每 tick 速度乘 `0.91D`，`motionY += 0.01D`。
    - `rotationPitch += 30 * ((getEntityId() % 2) - 0.5)`，即按粒子 id 奇偶决定 `±15 degree/tick` 旋转。
    - meta 0/1/2/3/4 分别为普通火、balefire、digamma、oxy、black，颜色/alpha/scale 渲染曲线已在早前批次迁入现代 `FlamethrowerParticle`。
- 本批现代迁移：
  - `GasFlameParticle`：
    - 新增 `baseScale` 并覆盖 `getQuadSize(...)`，使用旧 smoke 的 `clamp((age+partial)/lifetime*32, 0, 1)` 起始膨胀曲线。
    - 去掉现代端额外 `alpha = pow(1-time, 0.35)` 淡出，回到旧 `ParticleGasFlame` 自身只改 RGB、继承 smoke 缩放的表现。
  - `FlamethrowerParticle`：
    - 新增本地递增 visual id，旋转方向由 `visualId % 2` 决定。
    - `rollSpeed` 等价旧 `±15 degree/tick`，替换原先随机布尔方向。
- 边界：
  - 现代端没有旧 `EntityFX#getEntityId()`；本批沿用前面旋转粒子的本地 visual id 方案，复刻奇偶分布而不依赖实体系统。
  - `GasFlameParticle` 的旧父类 `EntitySmokeFX` 贴图帧/内部随机细节没有完整 MCP 源码可直接对照，本批只收紧能从 HBM 旧源码和 `smokeParticleScale` 合同确认的缩放、颜色、寿命、速度与亮度行为。

## 2026-06-03 第三十七批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-06-04 第三十八批推进：旧 `haze` / `plasmablast` / `foam` 视觉合同收紧

- 1.7.10 对照：
  - `ParticleHaze`：
    - 默认构造寿命 `600+rand(100)`，`particleScale=10F`。
    - 每 tick 速度乘 `0.9599999785D`；落地时额外 `motionX/Z *= 0.699999988D`。
    - 每 tick 在粒子周围 `15x15` 范围取地表高度，生成一个 vanilla `lava` 粒子。
    - 渲染使用 `textures/particle/haze.png`，alpha 为 `sin(age * PI / 400F) * 0.25F` 后再乘 `0.1F`，颜色固定白色。
    - 每帧使用 `Random(50)` 生成 25 张雾片；循环内先 `GL11.glTranslatef(dX,dY,dZ)`，所以雾片位移会累积。
  - `ParticlePlasmaBlast`：
    - 使用 `textures/particle/shockwave.png`，寿命 20。
    - `rotationYaw` / `rotationPitch` 来自 packet，渲染时按 yaw 再 pitch 旋转水平 quad。
    - alpha 为 `1 - (age+interp)/maxAge`，scale 为 `(1 - e^(-(age+interp)*0.125)) * particleScale`。
    - 旧渲染禁用 cull、关闭深度写入、加色混合并临时关闭 fog。
  - `ParticleFoam`：
    - 寿命 `60+rand(60)`，`particleGravity=0.005+rand(0.015)`。
    - 默认 `baseScale=1.0F`、`maxScale=1.5F`、`trailLength=15`、`buoyancy=0.05F`、`jitter=0.15F`、`drag=0.96F`。
    - `foamSplash` 旧入口在圆环上生成 `ParticleFoam` 后把 `maxAge=50` 且 `motionX/Y/Z=0`。
    - 每 tick 记录当前位置到 trail；生命周期分三段：
      - `<30%` burst：先强浮力，后按比例减弱，scale 从 base 到 max。
      - `30%-60%` peak：`motionY *= 0.98`，scale 固定 max。
      - `>60%` settle：受 gravity 下落，scale 衰减到 max 的 30%。
    - alpha 为 `0.8F * (1 - phaseRatio^2)`；每 tick 加水平 jitter，再统一乘 drag；落地或进网后消失。
    - 渲染当前点和 trail 点，每个点按阶段生成 8/6/4 个随机白色 bubble，trail alpha 再乘 `0.7` 衰减。
- 本批现代迁移：
  - `HazeParticle`：
    - 颜色从黑色修回旧版白色低透明热雾。
    - tick 补回落地时水平速度额外衰减。
    - 25 张雾片的偏移改为 `cumulativeX/Y/Z` 累积，恢复旧 `glTranslatef` 矩阵行为。
  - `PlasmaBlastParticle`：
    - 增加 `shouldCull=false`，避免大 shockwave quad 被粒子包围盒提前裁剪，贴近旧即时 GL 绘制路径。
  - `FoamParticle`：
    - 从单张 `TextureSheetParticle` 泡沫扩展为旧式多 bubble + trail 渲染。
    - 补回 base/max scale、trailLength、buoyancy、jitter、drag、三阶段 scale/alpha/运动曲线和落地消失。
    - 渲染随机 seed 使用本地 visual id 加坐标复刻旧 `getEntityId()+坐标` 的稳定 bubble 分布。
- 边界：
  - `ParticleHaze` 旧构造存在带 RGB/scale 的重载，当前已迁入口只使用默认 `haze`；本批不新增未调用的参数化 packet。
  - `ParticlePlasmaBlast` 旧端会临时关闭 fog；现代粒子 render type 当前保留加色、禁 cull、不写深度和 fullbright 的核心视觉合同，fog 状态仍不单独切换。
  - `ParticleFoam` 旧类构造会给普通实例随机初速度，但 `foamSplash` 入口随后清零；现代 `spawnRing` 已传零速度并设置 50 tick，本批按当前入口保持零速度起步，非零速度保留给未来调用方。

## 2026-06-04 第三十八批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-06-04 第三十九批推进：旧 `exSmoke` 多子烟团与烟迹随机源收紧

- 1.7.10 对照：
  - `ParticleExSmoke`：
    - 使用 `ModEventHandlerClient.particleBase`，`maxAge=100+rand(40)`。
    - 每 tick：`particleAlpha = 1 - age/maxAge`，随后年龄递增；速度三轴都乘 `0.7599999785D` 并移动。
    - 渲染时用 `Random(getEntityId())`，每帧稳定生成 6 张 billboard。
    - 每张 billboard 的灰度为 `rand*0.25+0.25`，scale 为 `rand+0.5`，位置偏移为 `(gaussian-1)*0.75`。
  - `ParticleContrail`：
    - 每帧使用 `Random(getEntityId())` 生成 6 张 contrail billboard。
    - 每张颜色为基础 RGB 加 `0.2..0.4` 的 mod，scale 为 `(particleAlpha+0.5F)*particleScale`，偏移为 `gaussian*0.5*particleScale`。
  - `ParticleSmokePlume`：
    - 每帧同样用 `Random(getEntityId())` 生成 6 张 plume billboard。
    - 每张灰度为 `rand*0.75+0.1`，位置偏移为 `gaussian*0.5*particleScale`。
    - 运动与缩放此前现代端已迁入：`80+rand(20)` 寿命、alpha 衰减、scale 从 `0.25` 增至 `2.25`、移动时补 `scale-prevScale` 的 Y 抬升、撞地时把 `motionY` 改为原速度长度、速度乘 `0.925`。
- 本批现代迁移：
  - `HbmSmokeParticle`：
    - 新增 `legacyExSmoke` 模式，只用于 `EX_SMOKE` provider 与 `HbmParticleEffects#createExSmoke(...)`。
    - legacy ex smoke 不再使用单张缩放 smoke，而是在 `render(...)` 中按旧合同生成 6 张随机子烟团。
    - 随机源改为本地递增 visual id，复刻旧 `EntityFX#getEntityId()` 的稳定分布。
    - 保持 ex smoke 的旧速度摩擦、alpha 和寿命；`launch_smoke` 继续走原有单粒子缩放路径，不受本批影响。
  - `LegacyContrailParticle`：
    - 随机 seed 从普通随机 int 改成本地 visual id，贴近旧 `getEntityId()` 分布。
  - `SmokePlumeParticle`：
    - 随机 seed 从普通随机 int 改成本地 visual id，贴近旧 `getEntityId()` 分布。
- 边界：
  - 现代端没有旧 `EntityFX#getEntityId()`；本批沿用前面粒子的本地 visual id 方案，只复刻稳定分布，不接入实体系统。
  - `HbmSmokeParticle` 仍保留非 legacy 模式给 launch smoke / 其他现代烟使用，避免误改导弹发射烟的既有表现。

## 2026-06-04 第三十九批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。

## 2026-06-04 第四十批推进：旧 `EntityAuraFX` / `townaura` / `schrabfog` 协议收紧

- 1.7.10 对照：
  - `ClientProxy#effectNT type=vanillaExt, mode=townaura`：
    - 创建 `EntityAuraFX(world, x, y, z, 0, 0, 0)`。
    - 随机 `color=0.5F+rand*0.5F`，颜色设为 `(0.8*color, 0.9*color, 1.0*color)`。
    - 随后 `setVelocity(mX, mY, mZ)`，即外部速度不再走构造器内部的微缩放。
  - `ClientProxy#effectNT type=radiation`：
    - 围绕客户端玩家生成 `EntityAuraFX`，颜色 `(0F, 0.75F, 1F)`。
    - 随后 `setVelocity(randGaussian, randGaussian, randGaussian)`。
  - `ClientProxy#effectNT type=schrabfog`：
    - 在包坐标生成单个 `EntityAuraFX`，颜色 `(0F, 1F, 1F)`，不再额外设置速度。
  - 旧 `EntityAuraFX` 合同已在 2026-05-24 修正中核定：
    - `setSize(0.02F, 0.02F)`。
    - `particleScale *= rand*0.6F+0.5F`。
    - 构造器内速度再乘 `0.02D`。
    - 寿命为 `20 / (rand*0.8D+0.2D)`。
    - tick 摩擦为 `0.99D`，无额外 alpha 淡出或放大曲线。
- 本批现代迁移：
  - `LegacyAuraParticle`：
    - 去掉现代端自定义的 35-55 tick 寿命、上浮、线性 alpha 淡出和逐渐放大曲线。
    - 恢复旧 `EntityAuraFX` 尺寸、scale 随机、寿命、0.99 摩擦和常量 alpha。
    - 保留旧 `setVelocity(...)` 语义，用于 `radiation` 与 `vanillaExt/townaura` 的外部速度覆盖。
  - `TownAuraParticle`：
    - 改回旧 vanilla `townaura` 的短寿命小 aura 行为。
    - 颜色仍按旧 `0.5..1.0` 随机亮度乘 `(0.8,0.9,1.0)`。
    - 保留构造器速度微缩放路径，服务 `world.spawnParticle("townaura", ..., mX,mY,mZ)` 等价分支。
  - `SchrabFogParticle`：
    - 使用本粒子自身随机源计算寿命，避免混用全局 `Math.random()`。
    - 保持旧 `schrabfog` 的单个青色 aura 小粒子形态。
  - `HbmParticleEffects`：
    - `type=schrabfog` 直接发 `ModParticleTypes.SCHRAB_FOG`，让旧 NBT 包和方块随机 tick 共享同一专用 schrab aura provider。
    - `vanillaExt/mode=townaura` 改为通过 `LegacyAuraParticle` 设置旧随机蓝白色与外部速度。
    - `type=vanilla, mode=townaura` 补回到 `ModParticleTypes.TOWN_AURA`，不再掉到默认 `POOF`。
- 边界：
  - 本批只迁移 HBM 调用到的 `EntityAuraFX` 外观与速度合同，不尝试覆盖所有 vanilla portal/aura 粒子的内部贴图帧细节。
  - `schrab_fog.json` 继续使用 `minecraft:generic_0` 单像素 sprite；`town_aura` 继续沿用已有 HBM 粒子 sprite，不在本批调整资源。
  - `TauSparkParticle` 与旧 `ParticleSpark` 已对齐寿命、重力、线宽、轨迹阈值、小型模式和落地反弹，本批不做无差异改写。

## 2026-06-04 第四十批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。
- 已运行：`.\gradlew.bat compileJava processResources --no-daemon --rerun-tasks`
- 结果：通过；仅有既存 deprecation 提示。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/client/particle/HbmParticleEffects.java src/main/java/com/hbm/ntm/client/particle/LegacyAuraParticle.java src/main/java/com/hbm/ntm/client/particle/TownAuraParticle.java src/main/java/com/hbm/ntm/client/particle/SchrabFogParticle.java 工程记录/库移植/2026-05-20-sound-particle-effects-library-1.7.10源码功能追踪.md`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-04 第四十一批推进：旧 `rift` / `foundry` / `fluidfill` 视觉细节收紧

- 1.7.10 对照：
  - `ClientProxy#effectNT type=rift`：
    - 直接创建 `ParticleRift(man, world, x, y, z)`。
    - `ParticleRift` 使用 `textures/particle/hadron.png`，寿命 10，`getFXLayer=3`。
    - 渲染时关闭深度写入，使用反相混合 `ONE_MINUS_DST_COLOR / ONE_MINUS_SRC_COLOR`。
    - 旧 GL 路径会关闭 `TEXTURE_2D` 后启用 `GL_CULL_FACE`，在粒子位置绘制 5 层 `ResourceManager.sphere_uv`，scale 为 `(age+interp)*0.5F` 后按 `1.02/1.05/1.02/1.05` 继续嵌套放大。
  - `ClientProxy#effectNT type=foundry`：
    - 从 NBT 读取 `color`、`dir`、`len`、`base`、`off`，创建 `ParticleFoundry`。
    - `ParticleFoundry` 使用 `textures/blocks/lava_gray.png`，寿命 20，不移动，只按年龄展开/收束熔融金属几何。
    - 宽度为 `0.0625 + progress * 0.0625`，girth 为 `0.125 * (1 - progress)`。
    - 颜色先 `new Color(color).brighter()`，再按 `brightener=0.7` 往白色提亮。
    - UV 滚动使用 `(int)(System.currentTimeMillis() / 100 % 16) / 16D`，不是 world time。
  - `ClientProxy#effectNT type=fluidfill`：
    - 从 NBT 读取 `mX/mY/mZ`，创建 vanilla `EntityCritFX(world, x, y, z, mX, mY, mZ)`。
    - 随后调用 `fx.nextTextureIndexX()`。
    - 若 NBT 存在 `color`，则调用 `setRBGColorF(red/255F, green/255F, blue/255F)`。
- 本批现代迁移：
  - `RiftParticle`：
    - 自定义 render type 的 begin 阶段改为启用 cull，恢复旧 `GL11.glEnable(GL11.GL_CULL_FACE)`。
    - 保留此前已迁入的反相混合、关闭深度写入、5 层球体和寿命合同。
  - `FoundryParticle`：
    - UV 偏移改回 `System.currentTimeMillis() / 100L % 16L`，与旧熔融材质 100ms 帧步进一致。
  - `FluidFillParticle`：
    - 从长寿命自定义 hadron sheet 粒子改为现代 `CritParticle` 等价合同：`friction=0.7`、`gravity=0.5`、速度按旧/vanilla crit 路径加 `m*0.4`、短寿命 `6/(rand*0.8+0.6)`、无碰撞、初始 tick、绿色/蓝色逐 tick 衰减。
    - 支持旧 NBT `color` 覆盖 RGB。
    - 渲染类型改为 `PARTICLE_SHEET_OPAQUE`，`fluid_fill.json` 贴图改为 `minecraft:critical_hit`。
- 边界：
  - 现代端没有 1.7.10 粒子图集上的 `nextTextureIndexX()` 直接等价项；本批使用 1.20.1 `CritParticle` 的 `critical_hit` sprite 与行为合同承接旧 `EntityCritFX` 语义。
  - `FluidFillParticle` 仍保留 HBM 自有 `ModParticleTypes.FLUID_FILL` provider 和 `HbmParticleEffects#spawnFluidFill` 入口，避免影响现有网络包/调用方。
  - `ParticleRift` 旧版在渲染时还临时关闭 `TEXTURE_2D`；现代端球体 mesh 当前仍走已有 shader 顶点颜色路径，本批只收紧可安全表达的 cull/混合/深度合同。

## 2026-06-04 第四十一批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；仅有既存 deprecation 提示。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/client/particle/RiftParticle.java src/main/java/com/hbm/ntm/client/particle/FoundryParticle.java src/main/java/com/hbm/ntm/client/particle/FluidFillParticle.java src/main/resources/assets/hbm/particles/fluid_fill.json 工程记录/库移植/2026-05-20-sound-particle-effects-library-1.7.10源码功能追踪.md`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-04 第四十二批推进：旧 RBMK 条带帧索引历史行为收紧

- 1.7.10 对照：
  - `ParticleRBMKFlame`：
    - 旧贴图 `textures/particle/rbmk_fire.png` 实际资源为 448x64，即 14 个 32px 横向帧。
    - 旧代码先计算 `texIndex = particleAge * 5 % 14`，随后 U 坐标使用 `texIndex % 5 * (1F / 14F)`，因此视觉上只采样前 5 个横向帧，并按 `0,0,0,1,1,1,2,2,...` 一类旧节奏重复。
  - `ParticleRBMKSteam`：
    - 旧贴图 `textures/particle/rbmk_jet_steam.png` 实际资源为 640x64，即 20 个 32px 横向帧。
    - 旧代码帧号为 `((age / maxAge) * 20) % 20 - 1`，第 0 tick 会得到 `-1`，在旧固定管线贴图 wrap 下等价从最后一帧起步。
- 本批现代迁移：
  - `RbmkAnimatedParticle`：
    - `Mode.FLAME` 的 UV 帧选择从完整 14 帧轮播改为旧 `((age * 5) % 14) % 5` 采样。
    - `Mode.STEAM` 的首帧改为按旧 `-1` 行为 wrap 到第 19 帧，后续按旧 `0..18` 进度播放。
- 边界：
  - 本批只复刻旧源码中能确认的条带 UV 索引历史行为，不改变 RBMK flame/steam/mush 的贴图资源、寿命、alpha、加色混合、fullbright 或几何尺寸。
  - 现代端仍通过粒子 atlas 内部 UV 手动切帧承接旧直接绑定整张贴图的行为；贴图 wrap 使用显式第 19 帧模拟，避免采样 atlas 外区域。

## 2026-06-04 第四十二批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过。第一次编译曾被并行机器/多方块迁移的临时 Java 缺口挡住；确认并行缺口补齐后重跑通过。
- 已运行：`git diff --check -- src/main/java/com/hbm/ntm/client/particle/RbmkAnimatedParticle.java 工程记录/库移植/2026-05-20-sound-particle-effects-library-1.7.10源码功能追踪.md`
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-04 第四十三批推进：旧 siren cassette track 与循环警报音基础库

- 1.7.10 对照：
  - `ItemCassette.TrackType`：
    - metadata/id 0 为 `NULL`，1..20 为全部 siren cassette 曲目。
    - 每条曲目记录 GUI 标题、旧 sound event、`SoundType`、cassette overlay 颜色和声音范围/音量字段。
    - `SoundType` 只有 `LOOP`、`PASS`、`SOUND` 三类；`SoundLoopSiren` 只对 `LOOP` 设置 repeat。
  - `SoundLoopSiren`：
    - 继承 `SoundLoopMachine`，静态 `list` 用于按 TileEntity 去重。
    - 默认 `intendedVolume=10F`，关闭 vanilla attenuation。
    - 每 tick 按玩家到 TileEntity 坐标距离计算 `volume=(distance/intendedVolume)*-2+2`。
    - 若 TE 不是 `TileEntityMachineSiren`，则停止；若是 siren，按曲目类型决定是否 repeat。
  - `TESirenPacket`：
    - 字段为 `x,y,z,id,active`。
    - active 且本地没有声音时，`id>0` 才启动声音。
    - active 且已有声音时，曲目路径改变则停止旧声音并重启。
    - inactive 时停止并从静态 list 移除。
  - 旧 `assets/hbm/sounds.json` alarm 声明使用 `category=record`，`alarm.airRaid` 使用 `stream=true`，其余本批曲目为 `stream=false`。
- 本批现代迁移：
  - 新增 `LegacySirenTrack`：
    - 保留旧 id 顺序、旧 enum 名称、标题、旧 sound id、`SoundType`、颜色和 volume 字段。
    - 提供 `byId(int)`，越界按旧逻辑回落 `NULL`。
    - 现代 sound event 使用小写合法路径，例如旧 `hbm:alarm.amsSiren` 映射为现代 `hbm:alarm.ams_siren`。
  - 新增 `SoundLoopSiren`：
    - 继承现代 `SoundLoopMachine`，按 `BlockPos` 去重。
    - 走 `SoundSource.RECORDS`，对应旧 `category=record`。
    - 复刻旧 `func(distance, intendedVolume)` 音量曲线并 clamp 到 `>=0`，避免现代声音系统收到负 volume。
    - `LOOP` 曲目循环播放，`PASS` / `SOUND` 曲目不循环并在播放结束后从本地 map 清理。
    - 支持 `handleClientTileEvent(BlockEntity, CompoundTag)`，直接读取现代 `sendSirenEvent` 的 `trackId` / `active`。
  - `HbmDynamicSound` / `SoundLoopMachine`：
    - 补可选 `SoundSource` 构造器，默认仍为 `BLOCKS`，现有动态声音调用方不变。
  - `ClientTileEventPacket`：
    - 对 `HbmNetworkActions.SIREN` 添加通用客户端分发，后续 siren 机器只要调用 `ModMessages.sendSirenEvent(blockEntity, trackId, active)` 即可接入本批声音库。
  - `ModSounds` / `sounds.json`：
    - 注册 20 条 siren cassette 曲目 sound event。
    - 复制旧版 `.ogg` 到 `src/main/resources/assets/hbm/sounds/alarm/`，文件内容来自 1.7.10 资源，现代文件名统一小写 snake_case。
    - 补 `subtitles.hbm.alarm.siren` 英文/中文字幕与 datagen provider。
- 现代 alarm 事件与旧资源映射：
  - `hbm:alarm.hatch` -> `hbm:alarm.hatch` -> `alarm/hatch.ogg`（旧文件 `lpfhaiwg.ogg`）。
  - `hbm:alarm.autopilot` -> `hbm:alarm.autopilot` -> `alarm/autopilot.ogg`（旧文件 `boeing707AutopilotDisconnected.ogg`）。
  - `hbm:alarm.amsSiren` -> `hbm:alarm.ams_siren` -> `alarm/ams_siren.ogg`。
  - `hbm:alarm.blastDoorAlarm` -> `hbm:alarm.blast_door_alarm` -> `alarm/blast_door_alarm.ogg`。
  - `hbm:alarm.apcLoop` -> `hbm:alarm.apc_loop` -> `alarm/apc_loop.ogg`。
  - `hbm:alarm.klaxon` -> `hbm:alarm.klaxon` -> `alarm/klaxon.ogg`。
  - `hbm:alarm.foKlaxonA` -> `hbm:alarm.fo_klaxon_a` -> `alarm/fo_klaxon_a.ogg`。
  - `hbm:alarm.foKlaxonB` -> `hbm:alarm.fo_klaxon_b` -> `alarm/fo_klaxon_b.ogg`。
  - `hbm:alarm.regularSiren` -> `hbm:alarm.regular_siren` -> `alarm/regular_siren.ogg`。
  - `hbm:alarm.classic` -> `hbm:alarm.classic` -> `alarm/classic_siren.ogg`（旧文件 `classicSiren.ogg`）。
  - `hbm:alarm.bankAlarm` -> `hbm:alarm.bank_alarm` -> `alarm/bank_alarm.ogg`。
  - `hbm:alarm.beepSiren` -> `hbm:alarm.beep_siren` -> `alarm/beep_siren.ogg`。
  - `hbm:alarm.containerAlarm` -> `hbm:alarm.container_alarm` -> `alarm/container_alarm.ogg`。
  - `hbm:alarm.sweepSiren` -> `hbm:alarm.sweep_siren` -> `alarm/sweep_siren.ogg`。
  - `hbm:alarm.striderSiren` -> `hbm:alarm.strider_siren` -> `alarm/strider_siren.ogg`。
  - `hbm:alarm.airRaid` -> `hbm:alarm.air_raid` -> `alarm/air_raid.ogg`，保留 `stream=true`。
  - `hbm:alarm.nostromoSiren` -> `hbm:alarm.nostromo_siren` -> `alarm/nostromo_siren.ogg`。
  - `hbm:alarm.easAlarm` -> `hbm:alarm.eas_alarm` -> `alarm/eas_alarm.ogg`。
  - `hbm:alarm.apcPass` -> `hbm:alarm.apc_pass` -> `alarm/apc_pass.ogg`。
  - `hbm:alarm.razortrainHorn` -> `hbm:alarm.razortrain_horn` -> `alarm/razortrain_horn.ogg`。
- 边界：
  - 本批只迁移 siren 声音库、track 数据表、网络事件客户端承接与音频资源，不迁移 `ItemCassette` 物品、siren 机器方块/GUI、cassette 合成或贴图 overlay。
  - 旧 `SoundLoopSiren` 会检查 TE 类型必须是 `TileEntityMachineSiren`；现代 siren 机器尚未迁入，本批以 `hbm:siren` tile event 作为库级入口，具体机器迁入后再补专属 BlockEntity 状态和交互。
  - 现代 `ResourceLocation` 路径禁止旧 `amsSiren` / `blastDoorAlarm` 等大写字符；因此运行时事件名做小写 snake_case 迁移，但 `LegacySirenTrack#legacySoundId` 保留旧 id 供后续 NBT/命令/文档兼容。

## 2026-06-04 第四十三批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；仅有既存 deprecation 提示。
- 已运行：`git diff --check --` 本批触碰的 siren 声音库源码、`sounds.json`、lang、alarm `.ogg` 目录与本追踪文档。
- 结果：无 whitespace error；仅有 Git 对 CRLF 的提示。

## 2026-06-04 第四十四批推进：旧 `MovingSoundPlayerLoop` 与 chopper 实体循环音基础库

- 1.7.10 对照：
  - `MovingSoundPlayerLoop`：
    - 继承 `MovingSound`，静态 `globalSoundList` 按 `Entity` 引用与 `EnumHbmSound` 去重。
    - `EnumHbmSound` 包含 `soundTauLoop`、`soundChopperLoop`、`soundCrashingLoop`、`soundMineLoop`。
    - 构造时设置 `repeat=true`；若同实体同类型没有声音，则加入全局 list。
    - `update()` 每 tick 把声音坐标跟随实体 `posX/posY/posZ`，实体为空或死亡则 `stop()`。
    - `stop()` 设置 `donePlaying=true`、`repeat=false`，并移除同实体同类型的所有全局声音。
  - `MovingSoundChopper`：
    - 基于 `MovingSoundPlayerLoop`，若 `EntityHunterChopper#getIsDying()` 为 true 则停止飞行循环。
  - `MovingSoundCrashing`：
    - 基于 `MovingSoundPlayerLoop`，若 `EntityHunterChopper#getIsDying()` 为 false 则停止坠毁循环。
  - `MovingSoundChopperMine`：
    - 仅继承基础循环逻辑，无额外停止条件。
  - `ModEventHandlerClient#onPlaySound(PlaySoundEvent17)`：
    - 旧代码拦截占位事件 `hbm:misc.nullChopper`、`hbm:misc.nullCrashing`、`hbm:misc.nullMine`。
    - 通过 `Library.getClosestChopperForSound(...)` / `getClosestMineForSound(...)` 找 2 格内实体。
    - 分别播放 `hbm:entity.chopperFlyingLoop`、`hbm:entity.chopperCrashingLoop`、`hbm:entity.chopperMineLoop`，音量设为 `10.0F`。
    - 每次事件尾部遍历 `globalSoundList`，未 init 或 donePlaying 的声音重新交给 sound handler 播放。
  - 旧 `sounds.json`：
    - `entity.chopperFlyingLoop`：`category=hostile`，资源 `entity/chopperFlyingLoop`，`stream=true`。
    - `entity.chopperCrashingLoop`：`category=hostile`，资源 `entity/chopperCrashingLoop`，`stream=true`。
    - `entity.chopperMineLoop`：`category=hostile`，资源 `entity/chopperMineLoop`，`stream=false`。
    - `entity.chopperDrop`、`entity.chopperCharge`、`entity.chopperDamage` 同属 chopper 音效；`chopperDamage` 为 `stream=true`。
- 本批现代迁移：
  - 新增 `LegacyMovingEntitySound`：
    - 继承现代 `AbstractSoundInstance` + `TickableSoundInstance`。
    - 以 `entityId + LoopType` 去重，承接旧 `getSoundByPlayer(Entity, EnumHbmSound)` 合同。
    - 每 tick 跟随实体坐标；实体移除、死亡或外部 `keepPlaying` predicate 返回 false 时停止并从静态 map 移除。
    - 默认 `looping=true`，使用 `SoundInstance.Attenuation.LINEAR` 与传入 `SoundSource`。
    - 提供 `startForEntity(...)`、`getSoundByEntity(...)`、`stop(...)`，以及 `startChopperFlying(...)` / `startChopperCrashing(...)` / `startChopperMine(...)` 三个旧 chopper 便捷入口。
  - `ModSounds` / `sounds.json`：
    - 注册并声明现代小写事件：
      - `hbm:entity.chopper_flying_loop`
      - `hbm:entity.chopper_crashing_loop`
      - `hbm:entity.chopper_mine_loop`
      - `hbm:entity.chopper_drop`
      - `hbm:entity.chopper_charge`
      - `hbm:entity.chopper_damage`
    - 从 1.7.10 资源复制 chopper `.ogg` 到 `src/main/resources/assets/hbm/sounds/entity/`，文件名改为小写 snake_case。
    - 保留旧 `stream` 设置：飞行 loop、坠毁 loop 和 damage 为 `true`，mine/drop/charge 为 `false`。
    - 补 `subtitles.hbm.entity.chopper` 英文/中文字幕与 datagen provider。
- 现代 chopper 事件与旧资源映射：
  - `hbm:entity.chopperFlyingLoop` -> `hbm:entity.chopper_flying_loop` -> `entity/chopper_flying_loop.ogg`。
  - `hbm:entity.chopperCrashingLoop` -> `hbm:entity.chopper_crashing_loop` -> `entity/chopper_crashing_loop.ogg`。
  - `hbm:entity.chopperMineLoop` -> `hbm:entity.chopper_mine_loop` -> `entity/chopper_mine_loop.ogg`。
  - `hbm:entity.chopperDrop` -> `hbm:entity.chopper_drop` -> `entity/chopper_drop.ogg`。
  - `hbm:entity.chopperCharge` -> `hbm:entity.chopper_charge` -> `entity/chopper_charge.ogg`。
  - `hbm:entity.chopperDamage` -> `hbm:entity.chopper_damage` -> `entity/chopper_damage.ogg`。
- 边界：
  - 现代端尚未迁入 `EntityHunterChopper` / `EntityChopperMine`，本批不伪造实体和旧 `PlaySoundEvent17` 的 `misc.null*` 占位声桥。
  - 旧 chopper 飞行/坠毁状态由 `EntityHunterChopper#getIsDying()` 决定；现代实体迁入后，应通过 `startChopperFlying(entity, e -> !isDying)` 与 `startChopperCrashing(entity, e -> isDying)` 接入，而不是在声音库里猜实体字段。
  - `soundTauLoop` 暂只保留 enum 兼容占位；tau gun loop 已由 `AudioWrapper`/`HbmDynamicSound` 路径服务，具体武器迁入时再按旧 `NTMSounds.GUN_TAU_LOOP` 注册资源。

## 2026-06-04 第四十四批验证

- 已运行：`.\gradlew.bat compileJava processResources --no-daemon`
- 结果：通过；仅有既存 deprecation 提示。

## 2026-06-04 第四十五批推进：旧 `vanillaExt/largeexplode` 大爆炸粒子深迁

- 1.7.10 对照：
  - `ClientProxy#effectNT type=vanillaExt, mode=largeexplode`：
    - 创建 `EntityLargeExplodeFX(man, world, x, y, z, data.getFloat("size"), 0, 0)`。
    - 主粒子颜色为 `r=1-rand*0.2` 后设为 `(r, 0.9*r, 0.5*r)`。
    - 随后循环 `data.getByte("count")` 次创建 `EntityExplodeFX`，颜色为灰阶 `0.5*(1-rand*0.5)`，并调用 `multipleParticleScaleBy(i+1)`。
  - 旧 `EntityLargeExplodeFX`：
    - 贴图为 `textures/entity/explosion.png` 4x4 sheet。
    - 寿命 `6 + rand(4)`，fullbright，帧索引 `(age+interp)*15/maxAge`。
    - 尺寸为 `2.0F * (1.0F - size * 0.5F)`，因此旧调用里 `size=0` 会显示最大主爆。
  - 旧 `EntityExplodeFX`：
    - 构造速度加 `+-0.05` 随机扰动，颜色初始为 `rand*0.3+0.7`，HBM 随后覆盖灰阶。
    - `particleScale = rand*rand*6 + 1`，再由 HBM 的 `multipleParticleScaleBy(i+1)` 放大。
    - 寿命 `16/(rand*0.8+0.2)+2`，每 tick 贴图 index `7-age*8/maxAge`，`motionY += 0.004`，整体摩擦 `0.9`，落地 X/Z 再乘 `0.7`。
- 本批现代迁移：
  - 新增 `ModParticleTypes.LARGE_EXPLODE` 与 `LargeExplodeParticle`。
  - 新增 `assets/hbm/particles/large_explode.json`：
    - 前 16 帧引用原版 `minecraft:explosion_0..15`，承接旧 `EntityLargeExplodeFX` sheet。
    - 后 8 帧引用 `minecraft:generic_7..0`，承接旧 `EntityExplodeFX` 的 texture index 倒序播放。
  - `LargeExplodeParticle`：
    - primary 模式复刻旧大爆炸的 6-9 tick、fullbright、`2*(1-size*0.5)` 尺寸、暖色 RGB 与 `age*15/maxAge` 帧节奏。
    - secondary 模式复刻旧爆炸烟的随机 `+-0.05` 速度、`gravity=-0.1` 对应每 tick 上浮 `+0.004`、`friction=0.9`、地面横向衰减、旧随机缩放和灰阶颜色。
  - `HbmParticleEffects#spawnVanillaExt`：
    - `largeexplode` 不再使用现代 vanilla `ParticleTypes.EXPLOSION` / `POOF` 近似。
    - 按旧 NBT `size/count` 直接加入一个 primary 与 `count` 个 secondary。
    - `count` 改回允许 0，避免旧 `getByte("count")` 缺省为 0 时现代端额外生成一颗 secondary。
  - `ParticleUtil#spawnVanillaExtLargeExplode`：
    - common helper 的 `count` clamp 改为 `0..127`，与旧 NBT 语义一致。
- 边界：
  - 本批迁移的是 HBM `type=vanillaExt, mode=largeexplode` 协议，不替换普通 vanilla `world.spawnParticle("largeexplode")` / `hugeexplosion` 路径；这些普通原版爆炸粒子仍由现代 Minecraft 原生效果承担。
  - 现代资源使用 `minecraft:explosion_*` 与 `minecraft:generic_*` 粒子 atlas 名称承接旧 vanilla 贴图，不复制原版贴图到 HBM 命名空间。
  - 旧源码中的普通爆炸声音、炮塔/boltgun/实体调用方仍按各自系统后续迁移；本批只收紧客户端视觉粒子。

## 2026-06-04 新版源码差异补记

对比旧快照与新版 5714 源码：

- `TileEntityMachineAutosaw` 新增 `AudioWrapper` 循环声，运行且未暂停、玩家 15 格内时播放 `NTMSounds.ENGINE_LOOP`，卸载/失效时停止。
- 新增 `TileEntityMachineThresher` 复用同类 engine loop 声音，并在作物/实体切割时触发烟雾、blockdust、zombie woodbreak 等旧粒子/声音路径。
- `ClientProxy` 移除 `EntityWaterSplash` 的空 renderer 注册，同时新增 blast furnace、thresher、vending machine、AUTOCAL 的 TESR 绑定。
- 新 `TileEntityMachineBlastFurnace` 客户端运行时生成 lava 粒子，并在 FLUE 足量且玩家距离小于 100 格时发送 `type=tower` 黑烟效果；现代粒子库后续需要支持该 NBT effect surface。
