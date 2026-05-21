# 网络包库 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 自定义网络 handler、packet dispatcher、线程包发送与主要 packet 分类。
- 该库是 GUI、机器同步、卫星、粒子、爆炸、配方同步、枪械动画的共同基础。

## 1.7.10 源文件

- `src/main/java/com/hbm/main/NetworkHandler.java`
- `src/main/java/com/hbm/packet/PacketDispatcher.java`
- `src/main/java/com/hbm/handler/threading/PacketThreading.java`
- `src/main/java/com/hbm/packet/threading/ThreadedPacket.java`
- `src/main/java/com/hbm/packet/toclient`
- `src/main/java/com/hbm/packet/toserver`

## 旧版契约

- `PacketDispatcher.wrapper` 使用自定义 `NetworkHandler`，channel name 为 mod id。
- 注册顺序使用递增 discriminator，必须保持服务端/客户端一致。
- 自定义 `NetworkHandler`：
  - 类似 `SimpleNetworkWrapper`，但不会立即 flush。
  - 包含 `PrecompilingNetworkCodec`，支持 `ThreadedPacket` 预编译 buffer。
  - 支持 sendToServer、sendToDimension、sendToAllAround、sendTo、sendToAll 等目标。
  - 对非 packet 线程发送使用 `PacketThreading.lock`。
- Packet 分类：
  - to server：按键、NBT 控制、卫星坐标、物品控制、砧配方。
  - to client：tile sync、粒子、扩展属性、爆炸效果、生物群系同步、配方同步、枪械动画。

## 迁移计划

- 使用现代 Forge `SimpleChannel` 或 1.20.1 推荐 payload API。
- discriminator 可改为 ResourceLocation，但旧注册顺序应记录方便对照。
- 所有 server handler 必须 enqueue 到主线程。
- 爆炸/粒子等高频包需要保留批量或压缩策略。

## 2026-05-21 实施记录

- 当前 clean port 网络入口：`src/main/java/com/hbm/ntm/network/ModMessages.java`。
- 保留现代 Forge `SimpleChannel`，协议版本为 `1`，channel 为 `hbm:main`。
- 已将注册入口改为幂等：`ModMessages.register()` 多次调用不会重复递增 discriminator。
- 已建立旧版 `PacketDispatcher.wrapper` 等价发送 API：
  - `sendToServer`
  - `sendToPlayer`
  - `sendToEntityTrackers`
  - `sendToEntityAndSelf`
  - `sendToDimension`
  - `sendToAllAround`
  - `sendToAll`
  - `sendToTrackingChunk`
- 现有已注册 packet：
  - S2C `PlayerRadiationSyncPacket`
  - S2C `AuxParticlePacket`
  - S2C `ParticleBurstPacket`
  - C2S `MachineBatteryButtonPacket`
- `MachineBatteryScreen` 已改为通过 `ModMessages.sendToServer` 发送按钮包，后续 GUI 不应直接访问 `CHANNEL`。
- 新增 `ThreadedPacketDispatcher` 作为高频包的安全调度入口，先覆盖旧版常用的 `sendToAllAround` 与 `sendToPlayer`，并在服务端 tick 末尾 flush。低频包仍应直接使用 `ModMessages`。
  本轮不复刻 1.7.10 的 Netty `PrecompilingNetworkCodec`/ByteBuf 预编译；爆炸压缩包迁移时再按具体包体评估是否需要现代化批量编码。
- 同步修复 `PlayerRadiationSyncPacket` 与 `ClientRadiationData`/`CommonForgeEvents` 的污染效果列表字段不一致问题，保证当前网络包契约可编译。

## 2026-05-21 继续实施记录

- 新增通用方块实体同步契约：`src/main/java/com/hbm/ntm/network/HbmTileSyncable.java`。
  - `getClientSyncTag`/`handleClientSyncTag` 对应旧版 `BufPacket` 与参考版 S2C tile sync 的最小安全形态。
  - `canReceiveClientControl`/`handleClientControl` 对应旧版 `NBTControlPacket` 的 `IControlReceiver` 契约。
- 新增通用 packet：
  - S2C `TileSyncPacket`：按 `BlockPos + CompoundTag` 将服务端同步数据派发给实现 `HbmTileSyncable` 的客户端方块实体。
  - C2S `TileControlPacket`：按 `BlockPos + CompoundTag` 将 GUI/客户端控制数据提交给服务端方块实体。
- `TileControlPacket` 服务端安全边界：
  - 必须存在 sender。
  - 玩家必须在 16 格半径内。
  - 目标 chunk 必须已加载。
  - 目标方块实体必须实现 `HbmTileSyncable`。
  - 目标实体自己的 `canReceiveClientControl` 必须允许。
  - handler 始终 enqueue 到服务端主线程。
- `ModMessages.register()` 继续保持 append-only：新增 `TileSyncPacket` 与 `TileControlPacket` 追加在旧有 packet 之后，避免改变已存在 packet discriminator。
- `MachineBatteryBlockEntity` 已接入 `HbmTileSyncable` 的 client-control 分支，`MachineBatteryScreen` 已改用 `TileControlPacket`。旧 `MachineBatteryButtonPacket` 保留注册并委派给同一控制方法，以免破坏已有协议顺序或临时调用点。

## 验证清单

- 客户端/服务端协议版本一致。
- 高速发送不会跨线程写坏 buffer。
- GUI/NBT 控制包只允许合法玩家和合法方块实体。
- 配方同步和爆炸效果包在客户端正确解码。
- 2026-05-21：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
