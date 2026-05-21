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
