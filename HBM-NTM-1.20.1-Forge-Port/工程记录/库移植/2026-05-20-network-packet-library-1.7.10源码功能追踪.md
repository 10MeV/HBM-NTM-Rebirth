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

## 2026-05-22 继续实施记录

- 新增 C2S `TileSyncRequestPacket`，对齐参考版 `S2CSyncFailMessage` 的“客户端发现 tile sync 接收端缺失后请求服务端重发”思路。
- `TileSyncPacket` 客户端处理现在在本地方块实体不是 `HbmTileSyncable` 时，限频发送 `TileSyncRequestPacket`：
  - 每个 `BlockPos` 20 tick 内最多请求一次。
  - 避免服务端立即重发导致客户端重复请求形成高频循环。
- `TileSyncRequestPacket` 服务端安全边界：
  - 必须存在 sender。
  - 玩家必须在 32 格半径内。
  - 目标 chunk 必须已加载。
  - 目标方块实体必须实现 `HbmTileSyncable`。
  - 目标实体自己的 `canSendClientSyncTo` 必须允许。
  - handler 始终 enqueue 到服务端主线程。
- `ModMessages.register()` 继续保持 append-only：`TileSyncRequestPacket` 追加在既有 packet 之后。
- `ModMessages` 新增 `syncTileToPlayer(...)`，用于服务端对单个玩家重发 tile sync。
- `HbmTileSyncable` 新增 `canSendClientSyncTo(ServerPlayer)`，供后续锁定机器、权限机器或跨维度/多方块端口限制同步响应。
- `HbmEnergyBlockEntity` 默认实现 `HbmTileSyncable` 的能量同步：
  - `getClientSyncTag` 写入 `Energy`。
  - `handleClientSyncTag` 在客户端读取 `Energy`。
  - 后续能量机器可直接调用 `ModMessages.syncTileToTracking(...)` 或通过 `TileSyncRequestPacket` 响应单玩家重发。

## 2026-05-22 实体同步推进记录

- 新增通用实体同步契约：`src/main/java/com/hbm/ntm/network/HbmEntitySyncable.java`。
  - `getClientSyncTag`/`handleClientSyncTag` 对应参考版 `S2CEntitySyncPacket` 的最小安全形态。
  - `canSendClientSyncTo(ServerPlayer)` 预留给后续导弹、炮塔、枪械或权限实体限制单玩家同步响应。
- 新增 S2C `EntitySyncPacket`：
  - 包体为 `entityId + CompoundTag`。
  - 客户端按当前 level 的 entity id 查找实体。
  - 只有实体实现 `HbmEntitySyncable` 时才应用同步数据。
- `ModMessages.register()` 继续保持 append-only：`EntitySyncPacket` 追加在既有 packet 之后。
- `ModMessages` 新增：
  - `syncEntityToTracking(...)`
  - `syncEntityToPlayer(...)`
- `MovingPackageEntity` 已接入 `HbmEntitySyncable`：
  - 包裹 contents 使用同一套 NBT 读写方法保存和同步。
  - `setItemStacks` 标记需要同步。
  - 服务端 tick 中对 tracking clients 发送一次实体同步，避免客户端只有实体壳但缺包裹内容。
  - 后续包裹 renderer 或 GUI 若需要读取 contents，可直接依赖同步后的客户端数据。

## 2026-05-22 按键包推进记录

- 对齐旧版 `KeybindPacket` / `HbmKeybindsServer` 的网络层契约：
  - 客户端向服务端发送 `keybind + pressed`。
  - 服务端主线程更新玩家按键状态。
  - 若玩家主手物品实现按键接收接口，则分发按键事件给该物品。
- 新增 `HbmKeybind` 枚举，保留旧 `EnumKeybind` 顺序：
  - `JETPACK`、`TOGGLE_JETPACK`、`TOGGLE_MAGNET`、`TOGGLE_HEAD`、`DASH`、`TRAIN`
  - `CRANE_UP`、`CRANE_DOWN`、`CRANE_LEFT`、`CRANE_RIGHT`、`CRANE_LOAD`
  - `ABILITY_CYCLE`、`ABILITY_ALT`、`TOOL_ALT`、`TOOL_CTRL`
  - `GUN_PRIMARY`、`GUN_SECONDARY`、`GUN_TERTIARY`、`RELOAD`
- 新增 `HbmKeybindReceiver`，作为现代 `IKeybindReceiver` 等价接口。
- 新增 `HbmServerKeybinds`：
  - 按玩家 UUID 保存当前按下的 `EnumSet<HbmKeybind>`。
  - 提供 `isPressed`、`setPressed`、`handle`、`clear`。
  - `handle` 会更新状态并分发给主手 `HbmKeybindReceiver`。
- 新增 C2S `KeybindPacket`：
  - 包体为 key ordinal + pressed boolean。
  - 无效 ordinal 会被丢弃并记录 warn。
  - handler 始终 enqueue 到服务端主线程。
- `ModMessages.register()` 继续保持 append-only：`KeybindPacket` 追加在既有 packet 之后。
- `CommonForgeEvents` 在玩家 clone/logout 时清理 `HbmServerKeybinds`，避免断线或死亡复制后残留“按住键”状态。
- 当前限制：
  - 本批只迁移网络与服务端分发层，尚未注册现代 `KeyMapping` 或实现客户端输入扫描。
  - 旧 `HbmPlayerProps#setKeyPressed` 的喷气背包/磁铁/HUD/train GUI 副作用属于玩家扩展属性、装备和载具库，后续按对应库迁移。

## 2026-05-22 客户端按键与常用 S2C 包推进记录

- 新增客户端按键扫描入口：`src/main/java/com/hbm/ntm/client/HbmClientKeybinds.java`。
  - 在 `RegisterKeyMappingsEvent` 注册现代 `KeyMapping`。
  - 在客户端 tick 末尾比较本地按键状态，只在 pressed 状态变化时发送 `KeybindPacket`。
  - 离开世界时发送释放状态并清理本地缓存，降低服务端残留“按住键”的风险。
- `ClientForgeEvents` 已接入：
  - `LegacyHbmAnimations.tick()`
  - `HbmClientKeybinds.tick()`
  - `ClientMuzzleFlashEffects.tick()`
- 新增 S2C `ClientInformPacket`：
  - 包体为 `Component + id + millis`。
  - 客户端由 `ClientInformMessages` 维护按 id 覆盖的短时 HUD 提示，对应旧 `PlayerInformPacket` 的基础能力。
- 新增 S2C `ItemAnimationPacket`：
  - 包体为 hotbar slot、parallel rail、item translation key、动画 JSON `ResourceLocation`、动画名、是否保持最后一帧。
  - 客户端由 `ClientItemAnimationHandler` 缓存加载 `LegacyBusAnimationLoader` 结果，并调用 `LegacyHbmAnimations.start(...)`。
  - 该包是旧 `HbmAnimationPacket` 的现代化底层承载；具体枪械/工具的 legacy enum 到动画名映射后续随对应物品迁移。
- 新增 S2C `MuzzleFlashPacket`：
  - 包体为 `entityId`。
  - 客户端由 `ClientMuzzleFlashEffects` 记录最近闪光时间，供后续枪械 renderer 查询。
- `ModMessages.register()` 继续保持 append-only：`ClientInformPacket`、`ItemAnimationPacket`、`MuzzleFlashPacket` 追加在 `KeybindPacket` 之后。
- `ModMessages` 新增便捷发送方法：
  - `informPlayer(...)`
  - `sendItemAnimation(...)`
  - `sendMuzzleFlash(...)`
- 当前限制：
  - 按键默认值按旧版常用键位映射到 GLFW，部分按键与原版操作存在冲突，后续在具体功能迁移时再按玩法校准。
  - `ClientInformMessages` 先提供轻量 HUD 文本层，未复刻旧 proxy tooltip 的全部 fancy 样式。
  - 枪械 firing、reload、muzzle flash 渲染本体尚未迁移，本批只提供网络与客户端缓存底座。

## 2026-05-22 物品控制、持久同步与爆炸击退推进记录

- 新增 C2S `ItemControlPacket`，对应旧 `NBTItemControlPacket`：
  - 包体为 `InteractionHand + CompoundTag`。
  - 服务端只派发给当前手持物品实现的 `HbmItemControlReceiver`。
  - handler 始终 enqueue 到服务端主线程。
  - `HbmItemControlReceiver` 提供 `canReceiveItemControl(...)` 与 `handleItemControl(...)`，供后续工具、枪械、遥控器、配置器复用。
- 新增 S2C `HeldItemNbtPacket`，对应旧 `HeldItemNBTPacket`：
  - 包体为 `InteractionHand + item registry id + damageValue + CompoundTag`。
  - 客户端仅在当前手持物品 id 与 damage 匹配时替换 NBT，避免误写玩家切换后的物品。
  - `ModMessages.syncHeldItemNbt(...)` 提供服务端便捷同步方法。
- 新增 C2S `LegacyButtonPacket`，作为旧 `AuxButtonPacket` 的安全兼容底座：
  - 包体为 `BlockPos + value + id`。
  - 服务端安全边界沿用 `TileControlPacket`：必须存在 sender、16 格内、chunk 已加载。
  - 只派发给实现 `HbmLegacyButtonReceiver` 的方块实体。
  - 旧 `AuxButtonPacket` 中直接写死的具体机器行为不在包层复刻，后续机器迁移时在各自 BlockEntity 中实现接口。
- 新增 S2C `PermaSyncPacket`，对应旧 `PermaSyncPacket` / `PermaSyncHandler`：
  - 现代包体暂用 `CompoundTag`，避免提前固化尚未迁移的 TOM impact、death potion、pollution 数组布局。
  - 客户端状态放入 `ClientPermaSyncData`，提供 tag copy 与常用 boolean/float 读取方法。
  - 后续污染、全局灾害、玩家扩展属性迁移时再定义具体 key。
- 新增 S2C `ExplosionKnockbackPacket`，对应旧 `ExplosionKnockbackPacket`：
  - 包体为三轴 float motion。
  - 客户端对本地玩家 `deltaMovement` 叠加该向量。
  - `ModMessages.sendExplosionKnockback(...)` 提供服务端便捷发送方法。
- `ModMessages.register()` 继续保持 append-only：上述五个包全部追加在 `MuzzleFlashPacket` 之后。
- 当前限制：
  - `HeldItemNbtPacket` 只同步 NBT，不创建或替换整个 ItemStack。
  - `PermaSyncPacket` 尚未接入固定 tick 发送器；发送频率需要等污染/全局灾害等数据源迁移后统一调度。
  - `LegacyButtonPacket` 不实现旧鸭子彩蛋和具体机器 switch，避免在库层引入未迁移实体/机器依赖。

## 2026-05-22 坐标动作、客户端数据与 tile 事件推进记录

- 新增 C2S `CoordinateActionPacket`，覆盖旧 `ItemDesignatorPacket`、`SatCoordPacket`、`SatLaserPacket` 的共同网络形态：
  - 包体为 `InteractionHand + BlockPos + action + value + frequency + CompoundTag`。
  - 服务端只派发给当前手持物品实现的 `HbmCoordinateActionReceiver`。
  - `HbmCoordinateActionReceiver` 提供 `canReceiveCoordinateAction(...)` 与 `handleCoordinateAction(...)`。
  - 具体 designator 坐标加减、卫星频率校验、laser click/coord action 属于物品/卫星库，后续在对应 item 或 satellite system 中实现。
- 新增 S2C `ClientBinaryDataPacket`，覆盖旧 `SerializableRecipePacket` 的通用二进制同步能力：
  - 包体为 `channel + name + payload + clearChannel`。
  - 客户端数据暂存于 `ClientBinaryData`，按 channel/name 查询。
  - 单包 payload 上限为 1 MiB，后续若有大型配方文件再补分片协议。
- 新增 S2C `ClientTileEventPacket`，作为旧 `TEVaultPacket`、`TESirenPacket`、`TEFFPacket` 等专用 tile packet 的现代底座：
  - 包体为 `BlockPos + eventType + CompoundTag`。
  - 客户端只派发给实现 `HbmClientTileEventReceiver` 的 BlockEntity。
  - 具体 blast door 动画、siren loop sound、force field HUD/renderer 字段不在包层写死。
- 新增 S2C `ClientPanelDataPacket`，覆盖旧 `SatPanelPacket` 的“服务端向客户端面板发送 typed NBT”的通用形态：
  - 包体为 `panelType + legacyType + CompoundTag`。
  - 客户端数据暂存于 `ClientPanelData`。
  - 卫星面板对象重建留给卫星/GUI 迁移时接入。
- 新增 S2C `PlayerPropertiesPacket`，作为旧 `ExtPropPacket` 的现代 NBT 版本：
  - 包体为 `dataType + CompoundTag`。
  - 客户端数据暂存于 `ClientPlayerSyncData`。
  - 旧 `HbmLivingProps`/`HbmPlayerProps` 的二进制字段布局不在网络库层固化，后续按玩家扩展属性库迁移。
- `ModMessages.register()` 继续保持 append-only：上述五个包全部追加在 `ExplosionKnockbackPacket` 之后。
- 当前限制：
  - `ClientBinaryData`、`ClientPanelData`、`ClientPlayerSyncData` 目前只是缓存层，不主动驱动 GUI 重载。
  - `ClientTileEventPacket` 不做类型 switch，所有旧专用 tile 包行为需由目标 BlockEntity 实现接口。
  - `CoordinateActionPacket` 不内置卫星保存数据访问，避免网络库依赖未迁移 satellite saved data。

## 2026-05-22 菜单动作、biome 缓存与压缩爆炸效果推进记录

- 新增 C2S `MenuActionPacket`，覆盖旧 `ItemBobmazonPacket` 与 `AnvilCraftPacket` 的共同 GUI/菜单动作形态：
  - 包体为 `action + value + CompoundTag`。
  - 服务端只派发给当前 `containerMenu` 实现的 `HbmMenuActionReceiver`。
  - handler 始终 enqueue 到服务端主线程。
  - Bobmazon offer 校验、caps 扣费、anvil recipe/tier 校验属于具体菜单迁移范围，不在网络库层硬编码。
- 新增 S2C `ClientBiomeSyncPacket`，对应旧 `BiomeSyncPacket`：
  - 支持单 cell：`blockX/blockZ + biome`。
  - 支持整 chunk：256 个 short biome id。
  - 客户端暂存于 `ClientBiomeSyncData`。
  - 1.20.1 biome registry/Holder 写回 chunk 的稳定路径需要等世界/污染/辐射地形效果迁移时统一实现，本批不直接改客户端 chunk biome container。
- 新增 S2C `CompressedExplosionEffectPacket`，对应旧 `ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket`：
  - 包体保留旧思想：中心点 float xyz、size、受影响方块相对中心的 byte xyz。
  - 编码时过滤超出 byte 相对范围的方块，并限制最多 32768 个位置。
  - 客户端由 `ClientExplosionEffects.standard(...)` 播放本地爆炸音效、核心爆炸粒子，并抽样最多 64 个 affected block 生成轻量烟尘。
  - 后续爆炸效果库成熟后可替换为更接近旧 `ExplosionEffectStandard.performClient(...)` 的完整表现。
- `ModMessages.register()` 继续保持 append-only：上述三个包全部追加在 `PlayerPropertiesPacket` 之后。
- 当前限制：
  - `ClientBiomeSyncPacket` 只缓存，不写入真实 biome 数据。
  - `CompressedExplosionEffectPacket` 只复刻网络压缩格式和轻量客户端表现，不执行完整旧版 block crack/debris 效果。
  - 旧 `TEMissileMultipartPacket` 的具体导弹结构仍应随导弹/发射台库迁移，通过现有 `ClientTileEventPacket` 或后续导弹结构专用接口接入。

## 2026-05-24 线程包调度与诊断推进记录

- 对齐旧版 `PacketThreading` 的运行契约，扩展 `ThreadedPacketDispatcher`：
  - 线程名继续使用 `NTM-Packet-Thread-` 前缀。
  - 保留 50 ms 单任务等待预算，对超时任务执行取消/清队。
  - 记录 total queued/sent/failed/discarded、last flush queued/completed/discarded/wait、连续清队次数与最后错误。
  - 连续清队超过 5 次后触发 main-thread fallback，后续线程化发送会直接在调用线程执行，避免无限堆积。
  - 队列 pending 上限为 4096，超过上限会清理旧队列并对当前包走 fallback 发送。
- `ThreadedPacketDispatcher` 现在覆盖旧 `PacketDispatcher` 常用发送目标：
  - `sendToAllAround`
  - `sendToPlayer`
  - `sendToEntityTrackers`
  - `sendToEntityAndSelf`
  - `sendToDimension`
  - `sendToAll`
  - `sendToTrackingChunk`
- 新增 `/hbm network packetthreading stats`：
  - 输出当前 pending、fallback、累计发送/失败/丢弃、上次 flush 统计和最后一次问题。
- 新增 `/hbm network packetthreading reset`：
  - 清空 pending/执行器队列与统计，并解除 main-thread fallback。
- 当前限制：
  - 现代实现仍不复刻旧 `PrecompilingNetworkCodec`/`PrecompiledPacket` 的 ByteBuf 预编码。
  - executor 中已开始执行的任务无法强制停止；超时清理主要用于丢弃尚未开始和后续排队任务。
  - 线程化入口只应给服务端高频 S2C 使用，C2S handler 仍必须通过 packet 自身 `enqueueWork` 进入服务端主线程。

## 2026-05-24 大二进制同步与 ready 信号推进记录

- 旧版 `SerializableRecipePacket` 行为：
  - 普通包：`filename + fileBytes`，客户端 `SerializableRecipe.receiveRecipes(...)`。
  - 结束包：`reinit = true`，客户端收到后 `SerializableRecipe.initialize()`。
- 现代网络库已有 S2C `ClientBinaryDataPacket`，本批保留其旧 wire layout 不变，并在注册表末尾追加：
  - S2C `ClientBinaryDataChunkPacket`：按 `transferId + channel + name + chunkIndex + chunkCount + payload` 传输大二进制数据。
  - S2C `ClientBinaryDataReadyPacket`：按 channel 标记“该批数据已发送完成”，对应旧 `SerializableRecipePacket(true)` 的库层信号。
- `ClientBinaryData` 新增：
  - chunk 重组缓存，全部 chunk 到齐后写入原 `channel/name` 数据表。
  - `markReady(...)` 与 `readyVersion(...)`，供后续配方/GUI 数据同步在客户端检测批次完成。
  - `pendingTransfers()` 供调试或后续诊断界面查询未完成分片传输数量。
- `ModMessages` 新增/增强：
  - `syncClientBinaryData(...)` 自动判断 payload 大小，小于等于 1 MiB 走旧 `ClientBinaryDataPacket`，更大时按 256 KiB chunk 分片发送。
  - `syncClientBinaryDataBatch(...)` 支持 clear-first、逐项发送与 mark-ready，作为旧配方目录批量同步的现代底座。
  - `markClientBinaryDataReady(...)` 发送独立 ready packet，不改变旧 packet discriminator 的包体格式。
- 当前限制：
  - 分片重组没有落盘；只作为客户端运行期缓存。
  - 分片包最多 512 片，单次 logical payload 当前上限约 128 MiB。
  - ready 信号只是版本号递增，不直接调用具体配方系统；后续配方库接入时监听或比较 `readyVersion(...)`。

## 2026-05-24 tile 二进制同步推进记录

- 旧版 `BufPacket` 行为：
  - 包体为 `x/y/z + ByteBuf payload`。
  - 服务端由实现 `IBufPacketReceiver#serialize(ByteBuf)` 的 TileEntity 写入二进制数据。
  - 客户端按坐标找 TileEntity，若实现 `IBufPacketReceiver` 则调用 `deserialize(ByteBuf)`。
  - 读取异常时只记录 warn，避免单个 tile 同步错误打断客户端。
- 旧版 `TEMissileMultipartPacket` 行为：
  - 包体为 `x/y/z + MissileStruct` 的二进制结构。
  - 客户端按坐标找 compact launcher、launch table 或 missile assembly，再写入 `load` 字段。
  - 具体 `MissileStruct` 解析与目标机器类型仍属于导弹/发射台库，不在网络库硬编码。
- 新增通用客户端接收接口：`HbmClientTileBinaryReceiver`：
  - `handleClientTileBinaryData(ResourceLocation channel, FriendlyByteBuf data)`。
  - 用 `channel` 区分旧 BufPacket 风格同步、导弹 multipart、或机器自定义二进制子协议。
- 新增 S2C `ClientTileBinaryDataPacket`：
  - 包体为 `BlockPos + channel + payload byte[]`。
  - 单包 payload 上限 1 MiB。
  - 客户端只派发给实现 `HbmClientTileBinaryReceiver` 的 BlockEntity。
  - handler 捕获接收端异常并记录 warn，对齐旧 `BufPacket` 的容错思想。
- 新增 S2C `ClientTileBinaryDataChunkPacket` 与客户端 `ClientTileBinaryData` 重组缓存：
  - 大于 1 MiB 的 tile 二进制数据按 256 KiB 分片。
  - 全部分片到齐后按原 `BlockPos + channel` 派发给接收端。
  - 单次 logical tile payload 当前上限约 128 MiB。
- `ModMessages` 新增：
  - `sendClientTileBinaryData(BlockEntity, ResourceLocation, Consumer<FriendlyByteBuf>)`
  - `sendClientTileBinaryData(ServerPlayer, BlockPos, ResourceLocation, Consumer<FriendlyByteBuf>)`
  - byte[] overload 与自动分片发送路径。
- 当前限制：
  - 该层只提供通用 ByteBuf 派发，不复刻 `MissileStruct` 字段布局。
  - tile binary 分片重组只保存在客户端内存中；没有超时清理，后续若出现长期未完成传输再补诊断/TTL。
  - 服务端写 buffer 的调用方必须保证 writer 不跨线程访问不安全对象；高频发送可配合 `ThreadedPacketDispatcher`。

## 2026-05-24 C2S tile 二进制控制推进记录

- 旧版网络库中 C2S 控制主要由 `NBTControlPacket`、`AuxButtonPacket`、`SatCoordPacket`、`NBTItemControlPacket` 等承担；复杂 GUI/机器仍有旧 ByteBuf 风格数据需求。
- 本批新增通用服务端接收接口：`HbmTileBinaryControlReceiver`：
  - `canReceiveClientTileBinaryData(ServerPlayer, ResourceLocation, int readableBytes)` 用于权限、距离以外的机器自定义校验。
  - `handleClientTileBinaryData(ServerPlayer, ResourceLocation, FriendlyByteBuf)` 在服务端主线程执行。
- 新增 C2S `ServerTileBinaryControlPacket`：
  - 包体为 `BlockPos + channel + payload byte[]`。
  - 单包 payload 上限 256 KiB。
  - 安全边界沿用 `TileControlPacket`：必须有 sender、玩家 16 格内、chunk 已加载、目标 BlockEntity 显式实现接收接口。
  - 派发后调用 `setChanged()` 与 `sendBlockUpdated(...)`，保持与 NBT tile control 一致。
- 新增 C2S `ServerTileBinaryControlChunkPacket` 与 `ServerTileBinaryControlTransfers`：
  - 客户端上传超过 256 KiB 时按 64 KiB 分片。
  - 服务端按 `player UUID + transferId` 隔离重组，避免不同玩家或不同上传混淆。
  - 重组完成后复用 `ServerTileBinaryControlPacket` 的安全校验与派发逻辑。
  - 玩家退出时通过 `CommonForgeEvents.onPlayerLogout` 清理该玩家未完成上传。
- `ModMessages` 新增：
  - `sendTileBinaryControl(BlockPos, ResourceLocation, Consumer<FriendlyByteBuf>)`
  - `sendTileBinaryControl(BlockPos, ResourceLocation, byte[])`
  - 自动小包/分片上传路径。
- `/hbm network packetthreading stats` 现在额外输出 `Tile binary control uploads: pending=...`。
- 当前限制：
  - C2S 分片没有 TTL；目前依赖玩家退出清理，后续可在网络诊断中增加 tick 级超时剪枝。
  - 该层不解释 payload 结构，所有 channel 语义由具体机器/GUI/导弹库实现。
  - C2S 默认范围仍是 16 格；远程设备若需要跨距离控制，目标 BlockEntity 应在后续库中显式设计专用包或权限规则。

## 2026-05-24 分片传输生命周期与 packet threading 开关推进记录

- 对齐旧 `CommandPacketInfo` 中 reset/toggle/诊断能力，继续扩展现代网络诊断：
  - `ThreadedPacketDispatcher` 新增 enabled 状态。
  - 关闭 packet threading 时清空 pending/执行器队列，后续线程化发送直接同步执行。
  - `resetState()` 会恢复 enabled=true 并清除 fallback/统计。
  - `/hbm network packetthreading stats` 显示 enabled。
  - 新增 `/hbm network packetthreading toggle`、`enable`、`disable`。
- 给三类分片缓存补生命周期管理：
  - `ClientBinaryData`：S2C 大二进制数据缓存，30 秒未更新自动 prune。
  - `ClientTileBinaryData`：S2C tile 二进制数据缓存，30 秒未更新自动 prune。
  - `ServerTileBinaryControlTransfers`：C2S tile binary 上传缓存，30 秒未更新自动 prune。
- tick 接入：
  - 客户端 tick 每 100 tick 检查并清理 `ClientBinaryData` 与 `ClientTileBinaryData` 的过期分片。
  - 服务端 tick 末尾清理 `ServerTileBinaryControlTransfers` 的过期上传。
  - 玩家登出仍会清理该玩家未完成的 C2S tile binary 上传。
- 当前限制：
  - TTL 只做内存清理，不向对端发送重传/失败通知。
  - 客户端分片缓存的 pending 计数尚未接入可见命令；后续可加客户端调试 HUD 或专用诊断命令。

## 验证清单

- 客户端/服务端协议版本一致。
- 高速发送不会跨线程写坏 buffer。
- GUI/NBT 控制包只允许合法玩家和合法方块实体。
- 配方同步和爆炸效果包在客户端正确解码。
- 2026-05-21：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-22：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-22 实体同步批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-22 按键包批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-22 客户端按键与常用 S2C 包批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-22 物品控制、持久同步与爆炸击退批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-22 坐标动作、客户端数据与 tile 事件批次：第一次 Gradle 依赖解析遇到 MCPRepo SSL handshake 失败；重跑 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-22 菜单动作、biome 缓存与压缩爆炸效果批次：第一次 Gradle 增量编译遇到 `build/classes` 中间产物缺失；重跑 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
