# 声音与粒子效果库 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 客户端声音循环、动态音效、粒子工具和效果 packet。
- 该库服务机器、武器、爆炸、污染、导弹、载具和 HUD 表现。

## 1.7.10 源文件

- `src/main/java/com/hbm/sound`
- `src/main/java/com/hbm/particle`
- `src/main/java/com/hbm/util/ParticleUtil.java`
- `src/main/java/com/hbm/handler/EntityEffectHandler.java`
- `src/main/java/com/hbm/packet/toclient/ParticleBurstPacket.java`
- `src/main/java/com/hbm/packet/toclient/AuxParticlePacketNT.java`
- `src/main/java/com/hbm/packet/toclient/TESirenPacket.java`
- 资源：`src/main/resources/assets/hbm/sounds`
- 声音声明：旧 `sounds.json` 或注册入口需后续核对。

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
