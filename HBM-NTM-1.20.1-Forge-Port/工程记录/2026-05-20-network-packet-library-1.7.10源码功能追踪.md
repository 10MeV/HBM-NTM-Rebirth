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

## 验证清单

- 客户端/服务端协议版本一致。
- 高速发送不会跨线程写坏 buffer。
- GUI/NBT 控制包只允许合法玩家和合法方块实体。
- 配方同步和爆炸效果包在客户端正确解码。
