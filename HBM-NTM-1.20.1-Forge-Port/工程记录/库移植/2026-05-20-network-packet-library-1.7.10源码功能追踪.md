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

## 2026-05-24 协议稳定性与线程池诊断补强记录

- 校准 `ClientBinaryDataPacket` 的 wire layout：
  - 保持旧现代包体 `channel + clearChannel + name + payload` 不变。
  - `ready` 只通过 append-only 新增的 `ClientBinaryDataReadyPacket` 发送。
  - 这样不会改变已注册 discriminator 对应包体格式，避免客户端/服务端在同协议版本内因字段错位解码。
- `/hbm network packetthreading stats` 继续补齐旧 `ntmpackets info` 的线程池可见性：
  - thread pool total/core/max/active
  - executor queued
  - completed task count
- 当前限制：
  - 未复刻旧 `forceLock`/`forceUnlock`；现代实现不暴露锁对象，避免人为制造主线程冻结。
  - 线程池仍固定为单线程，后续若确实需要多线程包编码，再按实际高频包压测结果调整。

## 2026-05-24 实体事件与实体动作推进记录

- 旧版相关事实：
  - `ExtPropPacket`、`HbmAnimationPacket`、`MuzzleFlashPacket`、实体同步/效果类包都以 entity id 或玩家为目标定位对象。
  - 其中状态型数据已经由现代 `EntitySyncPacket` 承接，枪口焰类固定效果已经由 `MuzzleFlashPacket` 承接。
  - 仍缺少一个不绑定具体实体类的 typed entity event/action 通道，用于后续武器、载具、导弹、特殊实体 GUI 等迁移。
- 新增通用客户端接收接口：`HbmClientEntityEventReceiver`：
  - `handleClientEntityEvent(ResourceLocation eventType, CompoundTag data)`。
  - 由实体自己解释 `eventType` 和 NBT payload，网络库不写死动画、声音或粒子语义。
- 新增 S2C `ClientEntityEventPacket`：
  - 包体为 `entityId + eventType + CompoundTag data`。
  - 客户端按 entity id 找当前 `ClientLevel` 实体。
  - 只有目标实体实现 `HbmClientEntityEventReceiver` 时才派发；否则只记录 debug，避免旧式特效包丢失接收端时刷 warn。
- `ModMessages` 新增发送辅助：
  - `sendClientEntityEvent(Entity, ResourceLocation, CompoundTag)`：发给追踪者。
  - `sendClientEntityEventAndSelf(Entity, ResourceLocation, CompoundTag)`：发给追踪者和自身。
  - `sendClientEntityEvent(ServerPlayer, Entity, ResourceLocation, CompoundTag)`：发给指定玩家。
- 新增通用服务端接收接口：`HbmEntityActionReceiver`：
  - `canReceiveEntityAction(ServerPlayer, ResourceLocation, CompoundTag)` 作为实体级权限/状态校验 hook。
  - `handleEntityAction(ServerPlayer, ResourceLocation, CompoundTag)` 在服务端主线程执行。
- 新增 C2S `ServerEntityActionPacket`：
  - 包体为 `entityId + actionType + CompoundTag data`。
  - 必须有 sender，并在 sender 所在 `ServerLevel` 按 id 查找实体。
  - 默认只允许 64 格内实体动作；超距或不存在时记录 warn 并阻断。
  - 只有目标实体实现 `HbmEntityActionReceiver` 且权限 hook 通过时才派发。
- `ModMessages` 新增 `sendEntityAction(Entity, ResourceLocation, CompoundTag)`，供客户端侧实体 UI/交互发送 typed action。
- 当前限制：
  - 该层只承载小型 NBT 事件/动作，不提供大二进制 payload；大数据仍应使用已有 binary/tile 机制或后续专用协议。
  - 未迁移具体实体动画、武器、导弹或载具行为；后续实体库应实现上述接口并声明自己的 `ResourceLocation` action/event。
  - 处理时实体必须已经存在于客户端/服务端世界；出生包、跨维定位和延迟重放不在本批范围内。

## 2026-05-24 typed 物品/菜单动作与旧 Tile 特化包映射推进记录

- 旧版 C2S 小型交互包继续归拢到库层 typed action：
  - `AnvilCraftPacket`：`recipeIndex + mode`，服务端要求玩家打开 `ContainerAnvil`，再按 recipe/mode 执行批量锻造。
  - `ItemDesignatorPacket`：`operator + value + reference`，服务端修改手持 manual designator 的 `xCoord/zCoord`。
  - `ItemBobmazonPacket`：`offer`，服务端按手持 bobmazon 物品和报价索引扣 caps 并生成投送实体。
  - `SatCoordPacket`：`x/y/z/freq`，服务端校验手持卫星接口频率后调用卫星坐标动作。
  - `SatLaserPacket`：`x/z/freq`，服务端校验手持卫星接口频率后调用卫星点击动作。
- 新增 C2S `ItemActionPacket`：
  - 包体为 `hand + actionType + CompoundTag data`。
  - 服务端只派发给手持物品实现的 `HbmItemActionReceiver`。
  - 与旧 `ItemControlPacket` 并存：旧包保持 `hand + NBT` wire layout；新包为后续多动作物品提供 namespaced `ResourceLocation` action。
- 新增 `HbmItemActionReceiver`：
  - `canReceiveItemAction(ServerPlayer, InteractionHand, ItemStack, ResourceLocation, CompoundTag)`。
  - `handleItemAction(ServerPlayer, InteractionHand, ItemStack, ResourceLocation, CompoundTag)`。
- 新增 C2S `TypedMenuActionPacket`：
  - 包体为 `actionType + value + CompoundTag data`。
  - 服务端只派发给当前打开 menu 实现的 `HbmTypedMenuActionReceiver`。
  - 与旧 `MenuActionPacket` 并存：旧包保留整数 action/value；新包用于 anvil 这类需要稳定命名动作的 GUI。
- `ModMessages` 新增 helper：
  - `sendItemAction(...)`
  - `sendDesignatorAction(...)` 对应旧 `ItemDesignatorPacket` 字段。
  - `sendBobmazonOffer(...)` 对应旧 `ItemBobmazonPacket` offer 索引。
  - `sendSatelliteCoordinateAction(...)` 与 `sendSatelliteLaserAction(...)` 对应旧卫星坐标/激光请求。
  - `sendTypedMenuAction(...)` 与 `sendAnvilCraftAction(...)` 对应旧 `AnvilCraftPacket`。
- 旧版 S2C Tile 特化包映射到现有通道：
  - `TEVaultPacket`：`x/y/z + isOpening + state + sysTime + type`，现代用 `ClientTileEventPacket` 的 `hbm:vault_door` 事件承载。
  - `TESirenPacket`：`x/y/z + id + active`，现代用 `ClientTileEventPacket` 的 `hbm:siren` 事件承载。
  - `TEFFPacket`：`x/y/z + radius + health + maxHealth + power + isOn + color + cooldown`，现代用 `TileSyncPacket` 的 NBT 状态快照承载。
- `ModMessages` 新增 helper：
  - `sendClientTileEvent(ServerPlayer, BlockPos, ResourceLocation, CompoundTag)` 单玩家 tile event。
  - `sendVaultDoorEvent(...)`
  - `sendSirenEvent(...)`
  - `syncForceFieldState(...)`
- 当前限制：
  - 本批只提供网络库级通道与字段映射；Anvil、Bobmazon、Designator、Satellite、BlastDoor、Siren、ForceField 的具体业务逻辑仍由后续对应库/机器/物品迁移实现。
  - `TypedMenuActionPacket` 和 `ItemActionPacket` 不执行具体 recipe/offer/frequency 校验；这些校验必须保留在 receiver 中。
  - `syncForceFieldState(...)` 要求目标 BlockEntity 实现 `HbmTileSyncable` 才会在客户端消费 NBT。

## 2026-05-24 legacy 物品动画兼容与 panel 数据监听推进记录

- 旧版 `HbmAnimationPacket` 行为：
  - 包体为 `short type + int receiverIndex + int itemIndex`。
  - 客户端只读取当前玩家手持物品。
  - 若是 Sedna 枪械，按 `GunAnimation.values()[type]`、receiver index 与 gun index 播放旧 BusAnimation，并处理 cycle/recoil/reload 特例。
  - 若是 `IAnimatedItem`，按物品自己的 enum class 解释 `type`，再写入 `HbmAnimations.hotbar[slot][itemIndex]`。
- 现代已有 `ItemAnimationPacket`：
  - 适合已经知道动画 JSON `ResourceLocation` 与动画名的迁移内容。
  - 但它不能直接承载旧 `HbmAnimationPacket` 的三整数 enum/index 语义。
- 新增 S2C `LegacyItemAnimationPacket`：
  - 包体保留旧三字段：`animationType + receiverIndex + itemIndex`。
  - 客户端按当前主手物品派发给 `HbmLegacyItemAnimationReceiver`。
  - 未找到接收端时只记录 debug，避免尚未迁移枪械/工具期间刷日志。
- 新增 `HbmLegacyItemAnimationReceiver`：
  - `handleLegacyItemAnimation(ItemStack, selectedSlot, animationType, receiverIndex, itemIndex)`。
  - 后续 Sedna 枪械或旧 `IAnimatedItem` 迁移时，在物品中把 legacy enum/index 映射到现代 `LegacyHbmAnimations` 或 `ItemAnimationPacket` 的资源动画。
- 新增 `ClientLegacyItemAnimationHandler`：
  - 客户端安全获取本地玩家和主手物品。
  - 只做派发，不硬编码 `GunAnimation`、recoil 或 trenchmaster reload 加速等枪械库逻辑。
- `ModMessages` 新增 `sendLegacyItemAnimation(ServerPlayer, animationType, receiverIndex, itemIndex)`。
- `ClientPanelData` 增加轻量监听能力：
  - `addListener(...)`、`removeListener(...)`、`clearListeners()`。
  - `ClientPanelDataPacket` 仍保持原 wire layout 和缓存行为不变。
  - 更新 panel data 后同步通知监听器，后续卫星 GUI 可复刻旧 `SatPanelPacket` 的“收到 NBT 即刷新当前面板”体验，而不需要每 tick 轮询缓存。
  - 客户端离开世界时通过 `ClientForgeEvents` 清理监听器，避免 GUI/临时对象残留到下一次进世界。
- 当前限制：
  - 本批不迁移 Sedna 枪械 `GunAnimation`、receiver recoil、reload sequential 等具体行为；这些仍属于枪械库。
  - `LegacyItemAnimationPacket` 只发给单个玩家，不承担第三人称 muzzle flash；第三人称闪光仍由 `MuzzleFlashPacket`/实体事件通道承载。
  - `ClientPanelData` listener 为进程内轻量列表，不做弱引用；GUI 关闭时仍应主动 remove，离开世界清理作为保险。

## 2026-05-24 AuxParticlePacketNT helper 与客户端网络缓存生命周期推进记录

- 旧版 `AuxParticlePacketNT` 行为：
  - 继承 `ThreadedPacket`，大量武器、导弹、RBMK、能量/流体接口和实体特效通过 `PacketThreading.createAllAroundThreadedPacket(...)` 发送。
  - 构造器接收 `NBTTagCompound + x/y/z`，并写入 `posX/posY/posZ`。
  - 客户端若 world 存在则调用 `MainRegistry.proxy.effectNT(nbt)`。
- 现代已有 `AuxParticlePacket`：
  - 包体为 `CompoundTag`。
  - 客户端通过 `ClientParticleBridge.handleAux(...)` 派发到 `HbmParticleEffects.handleAux(...)`。
  - 已覆盖 gasfire/debugline/vanilla/smoke/contrail/explosion/casing/skeleton 等多个旧 `effectNT` 类型。
- 本批在 `ModMessages` 新增旧构造器等价 helper：
  - `sendAuxParticle(ServerLevel, x, y, z, data, range)`：自动写入 `posX/posY/posZ` 并发送到范围内玩家。
  - `sendAuxParticleThreaded(ServerLevel, x, y, z, data, range)`：走 `ThreadedPacketDispatcher`，对应旧 `PacketThreading.createAllAroundThreadedPacket(...)` 的高频特效用法。
  - `sendAuxParticle(ServerPlayer, x, y, z, data)`：发给指定玩家，对应旧少量 `createSendToThreadedPacket(...)` 用法。
  - `auxParticlePacket(x, y, z, data)`：生成已写入坐标的包体，供少数需要自定义发送目标的调用点复用。
- `ParticleUtil.spawnAux(...)` 改为通过 `ModMessages.sendAuxParticle(...)` 发送，避免粒子库和网络库出现两条并行发送路径。
- `ParticleUtil` 新增 `spawnAuxThreaded(...)`，供后续高频武器/导弹/机器特效迁移直接选择线程化发送。
- 客户端网络缓存补充 world lifecycle 清理：
  - `ClientBinaryData.clearAll()`：清理通用二进制数据、ready version 与未完成分片。
  - `ClientTileBinaryData.clearAll()`：清理 tile 二进制未完成分片。
  - `ClientBiomeSyncData.clearAll()`：清理客户端 biome 缓存。
  - `ClientPermaSyncData.clearAll()`：重置持久同步 tag。
  - `ClientPlayerSyncData.clearAll()`：清理玩家 typed sync 缓存。
  - `ClientPanelData.clearAll()`：清理 panel data 与 listener。
  - `ClientForgeEvents` 在客户端离开世界时统一调用上述清理，避免换存档或重连后残留旧网络状态。
- 当前限制：
  - `AuxParticlePacket` 仍是单包 NBT；超大特效数据应改用 binary/chunk 通道或专用压缩包。
  - `sendAuxParticleThreaded(...)` 只线程化发送操作，不改变 NBT 构造线程安全要求；调用方仍应在主线程完成 payload 数据读取。
  - 客户端缓存清理发生在 tick 检测到 `minecraft.level == null` 后；不是登录握手级事件。

## 2026-05-24 实体同步重发与客户端临时状态清理推进记录

- 旧版 `ExtPropPacket` 行为：
  - 继承 `PrecompiledPacket`。
  - 服务端写入 `HbmLivingProps.serialize(buf)` 与 `HbmPlayerProps.serialize(buf)`。
  - 客户端只应用到本地玩家的 living/player 扩展属性。
  - 这是玩家自身扩展属性的全量同步，不是任意实体的局部状态重发协议。
- 现代已有对应拆分：
  - 玩家辐射/污染长效属性：`PlayerRadiationSyncPacket`。
  - typed 玩家扩展属性：`PlayerPropertiesPacket` + `ClientPlayerSyncData`。
  - 任意实体 NBT 状态：`EntitySyncPacket` + `HbmEntitySyncable`。
- 本批补齐实体同步可靠恢复：
  - 新增 C2S `EntitySyncRequestPacket`。
  - 包体为 `entityId`。
  - 服务端要求存在 sender，按 sender 所在 `ServerLevel` 找实体。
  - 默认只允许 64 格内实体重发。
  - 目标实体必须实现 `HbmEntitySyncable`，且 `canSendClientSyncTo(player)` 通过。
  - 通过后调用 `ModMessages.syncEntityToPlayer(...)` 对单个玩家重发。
- `EntitySyncPacket` 客户端处理增强：
  - 若收到的 entity id 当前没有 `HbmEntitySyncable` 接收端，记录 debug 后限频请求重发。
  - 每个 entity id 20 tick 内最多请求一次，避免实体尚未完成客户端构建时形成请求风暴。
- `ModMessages.register()` 继续保持 append-only：
  - `EntitySyncRequestPacket` 追加在当前注册表尾部。
- `ModMessages` 新增 `syncPlayerPropertiesBatch(...)`：
  - 以 `Map<ResourceLocation, CompoundTag>` 批量发送 typed 玩家属性。
  - 用于后续把旧 `ExtPropPacket` 中 living/player props 拆成多个稳定 dataType，同步时仍能一次调度。
- 客户端离开世界时继续补清理临时网络视觉状态：
  - `ClientInformMessages.clearAll()` 清理 HUD 提示。
  - `ClientMuzzleFlashEffects.clearAll()` 清理实体枪口焰时间戳。
  - `LegacyHbmAnimations.clearAll()` 清理 hotbar 动画缓存。
  - 上一批已清理 binary/tile binary/biome/perma/player/panel 数据；现在离开世界时网络驱动的可见临时状态也一起归零。
- 当前限制：
  - `EntitySyncRequestPacket` 只能重发服务端当前同维可见实体；跨维实体、未追踪实体和已移除实体不会创建或等待。
  - 旧 `ExtPropPacket` 的具体字段仍需玩家扩展属性/辐射库继续拆分为 typed dataType；本批只补 batch 发送入口和文档路径。
  - 客户端动画/提示清理发生在离开世界 tick，不处理同一世界内玩家切换维度时的视觉残留。

## 2026-05-24 协议注册表快照与网络诊断命令推进记录

- 旧版 `PacketDispatcher.registerPackets()` 使用连续整数 discriminator，旧版 `CommandPacketInfo` 提供 `/ntmpackets info/resetState/toggleThreadingStatus` 调试入口：
  - `info` 会显示 packet threading 是否 active/errored、线程池总/core/idle/max 数、每条 `PacketThreading.threadPrefix` 线程的 id/state/lock owner、总包数、剩余队列比例和上 tick 等待时间。
  - 现代 `ThreadedPacketDispatcher` 已迁移 pending/queued/sent/failed/discarded、fallback、last flush、线程池统计和 reset/toggle/enable/disable。
- 本批补齐现代协议可观测性：
  - `ModMessages` 在 `registerServerToClient(...)` / `registerClientToServer(...)` 中自动记录 `PacketRegistration(id, direction, typeName)`。
  - 新增 `protocolVersion()`、`registeredPacketCount()`、`packetRegistrations()`，供命令、日志和后续测试读取当前通道协议快照。
  - 注册列表仍保持 append-only；快照从真实注册 helper 生成，避免维护第二份手写包表。
- 新增 `/hbm network protocol` 诊断命令：
  - `/hbm network protocol` 与 `/hbm network protocol summary` 输出 `channel=hbm:main`、协议版本、总包数、S2C/C2S 数量与首尾包 id。
  - `/hbm network protocol packets` 按当前注册顺序列出 `#id direction packetType`，方便多人调试时确认客户端/服务端包顺序一致。
- `ThreadedPacketDispatcher` 新增 `threadSnapshots()`：
  - 通过 `ManagementFactory.getThreadMXBean().dumpAllThreads(false, false)` 筛选 `NTM-Packet-Thread-` 前缀线程。
  - 暴露线程名、id、state、lock owner，对应旧 `CommandPacketInfo info` 的逐线程检查能力。
- `/hbm network packetthreading threads` 新增逐线程列表：
  - 当线程尚未创建时返回空提示。
  - 创建后输出每条 packet thread 的 `id/state/lockOwner`。
- 当前限制：
  - 协议快照只反映当前 JVM 内 `ModMessages.register()` 已执行后的注册状态；它不替代 Forge 握手校验。
  - 命令列表适合管理员/开发调试，`packets` 会输出完整包表，不作为玩家 UI。
  - 未迁移旧版 `forceLock/forceUnlock` 危险调试入口；现代 dispatcher 保留 reset/toggle/fallback 这类安全操作。

## 2026-05-24 导弹 multipart 快照与旧 BufPacket 映射推进记录

- 旧版 `TEMissileMultipartPacket` 行为：
  - S2C 包体为 `x/y/z + MissileStruct`。
  - `MissileStruct.writeToByteBuffer(...)` 顺序写入四个旧整数 item id：`warhead/fuselage/fins/thruster`。
  - 客户端按坐标查找 TileEntity，若是 `TileEntityCompactLauncher`、`TileEntityLaunchTable` 或 `TileEntityMachineMissileAssembly`，把 `load` 字段设为收到的 `MissileStruct`。
- 现代库层新增 `MissileMultipartSnapshot`：
  - 以四个可空 `ResourceLocation` 保存 `warhead/fuselage/fins/thruster`。
  - 支持从四个 `ItemStack` 构造，自动读取 `ForgeRegistries.ITEMS` id。
  - 使用 boolean + resource id 的稳定 wire layout，避免继续依赖 1.7.10 的运行时整数 item id。
- 新增 S2C `ClientMissileMultipartPacket`：
  - 包体为 `BlockPos + MissileMultipartSnapshot`。
  - 客户端查找当前位置 BlockEntity。
  - 只有目标实现 `HbmClientMissileMultipartReceiver` 时才派发。
- 新增 `HbmClientMissileMultipartReceiver`：
  - `handleClientMissileMultipart(MissileMultipartSnapshot multipart)`。
  - 后续 compact launcher、launch table、missile assembly 迁移时由各自 BlockEntity 接收并更新本地渲染/预览状态。
- `ModMessages.register()` 继续 append-only：
  - `ClientMissileMultipartPacket` 追加在当前注册表尾部。
- `ModMessages` 新增 helper：
  - `syncMissileMultipart(BlockEntity, MissileMultipartSnapshot)`：发给追踪该方块实体所在 chunk 的玩家。
  - `syncMissileMultipart(BlockEntity, ItemStack warhead, ItemStack fuselage, ItemStack fins, ItemStack thruster)`：供机器直接按槽位发送。
  - `syncMissileMultipart(Level, BlockPos, MissileMultipartSnapshot)`：供非 BlockEntity 持有者按坐标发送。
  - `syncMissileMultipart(ServerPlayer, BlockPos, MissileMultipartSnapshot)`：供单玩家补发/打开 GUI 同步。
- 旧版 `BufPacket` 映射确认：
  - 旧包为 S2C `x/y/z + IBufPacketReceiver.serialize(ByteBuf)`，客户端坐标查找 TileEntity 后调用 `IBufPacketReceiver.deserialize(ByteBuf)`。
  - 现代已有 `ClientTileBinaryDataPacket` / `ClientTileBinaryDataChunkPacket` + `HbmClientTileBinaryReceiver`，同样按 `BlockPos` 找客户端 BlockEntity，再派发 `FriendlyByteBuf`。
  - 现代路径额外提供 `ResourceLocation channel`、1 MiB 单包阈值、大 payload 分片、30 秒未完成分片清理和异常日志，因此旧 `BufPacket` 不再需要独立专用包。
- 旧版 `SerializableRecipePacket` 映射确认：
  - 旧包用于把服务端磁盘 JSON recipe 文件逐个发给客户端，最后发送 `reinit=true` 触发 `SerializableRecipe.initialize()`。
  - 现代 Forge 1.20.1 已由 datapack recipe manager/Forge recipe sync 承载 recipe 同步；当前 `GenericMachineRecipe`、`LiquefactionRecipe`、`PressRecipe` 都已走 `RecipeSerializer#fromNetwork/toNetwork`。
  - 因此本批不新增旧 `SerializableRecipePacket` 等价包；如后续需要同步 HBM 专用非 recipe-manager 数据，应复用 `ClientBinaryDataPacket` 或新增明确 dataType 的 typed 包。
- 当前限制：
  - `MissileMultipartSnapshot` 只保存四个部件 item id，不验证部件类型是否真为 warhead/fuselage/fins/thruster；类型校验应保留在导弹部件物品或机器槽位逻辑中。
  - 本批不迁移导弹部件物品、发射台、组装机或客户端渲染，只提供网络承载与接收接口。
  - 旧 `MissileStruct` 中 null/0 item id 在现代等价为空 `ResourceLocation`。

## 2026-05-25 typed Tile action 与旧 AuxButton/NBTControl helper 推进记录

- 旧版 `AuxButtonPacket` 行为：
  - C2S 包体为 `x/y/z + value + id`。
  - Handler 在服务端按坐标查找 TileEntity，并硬编码处理 ForceField、MissileAssembly、LaunchTable、CoreEmitter、CoreStabilizer、Barrel、MachineBattery、SoyuzLauncher、MiningLaser、`TileEntityMachineBase.handleButtonPacket(...)`、`TileEntityTickingBase.handleButtonPacket(...)`。
  - 文件本身标记 `@Deprecated`，注释要求改用 `NBTControlPacket`。
  - 特例：当 `te == null && value == 999` 时触发 duck 生成，这是实体/事件业务，不属于通用 tile action。
- 旧版 `NBTControlPacket` 行为：
  - C2S 包体为 `x/y/z + NBTTagCompound`。
  - 服务端按坐标找 TileEntity；若实现 `IControlReceiver` 且 `hasPermission(player)` 通过，则调用 `receiveControl(player, nbt)` 与 `receiveControl(nbt)`。
- 现代已有兼容路径：
  - `LegacyButtonPacket`：保留 `BlockPos + value + id` wire 语义，派发给 `HbmLegacyButtonReceiver`。
  - `TileControlPacket`：保留 `BlockPos + CompoundTag` 控制语义，派发给 `HbmTileSyncable.canReceiveClientControl(...)` / `handleClientControl(...)`。
- 本批新增 typed tile action 路径：
  - 新增 `HbmTypedTileActionReceiver`：
    - `canReceiveTypedTileAction(ServerPlayer, ResourceLocation actionType, int value, CompoundTag data)`。
    - `handleTypedTileAction(ServerPlayer, ResourceLocation actionType, int value, CompoundTag data)`。
  - 新增 C2S `ServerTileActionPacket`：
    - 包体为 `BlockPos + actionType + value + CompoundTag data`。
    - 服务端要求 sender 存在。
    - 默认只允许 16 格内方块实体动作，对应旧 GUI/机器按钮交互距离。
    - 目标 chunk 未加载时丢弃；目标 BlockEntity 必须实现 `HbmTypedTileActionReceiver` 且权限 hook 通过。
    - 处理后调用 `setChanged()` 与 `sendBlockUpdated(..., 3)`。
  - `ModMessages.register()` 继续 append-only：
    - `ServerTileActionPacket` 追加在当前注册表尾部。
- `ModMessages` 新增 helper：
  - `sendAuxButton(BlockPos, value, id)`：旧 `AuxButtonPacket` 字段名别名，内部走 `LegacyButtonPacket`。
  - `sendTileControl(BlockPos, CompoundTag)`：旧 `NBTControlPacket` 的现代直接入口。
  - `sendNbtControl(BlockPos, CompoundTag)`：保留旧包名语义的别名。
  - `sendTypedTileAction(BlockPos, ResourceLocation, int, CompoundTag)` 及简化 overload。
- 当前限制：
  - 本批不把旧 `AuxButtonPacket` 中硬编码的每台机器行为塞入网络库；机器迁移时由对应 BlockEntity 实现 receiver 并保留自己的权限/状态校验。
  - duck 生成特例不迁入 tile action；它应在后续实体/事件或彩蛋逻辑中单独迁移。
  - `TileControlPacket` 与 `ServerTileActionPacket` 并存：前者用于旧 NBT blob 兼容，后者用于后续新迁移 GUI 的稳定命名动作。

## 2026-05-25 玩家/perma/辐射客户端同步监听推进记录

- 旧版 `ExtPropPacket` 行为：
  - 服务端把 `HbmLivingProps` 与 `HbmPlayerProps` 串行写入同一个 buffer。
  - 客户端只应用到本地玩家扩展属性；HUD、装备能力和状态显示直接读这些客户端属性对象。
- 旧版 `PermaSyncPacket` 行为：
  - 服务端通过 `PermaSyncHandler.writePacket(...)` 写入玩家相关全局/持久状态。
  - 客户端通过 `PermaSyncHandler.readPacket(...)` 更新本地可读状态。
- 现代已有拆分：
  - `PlayerRadiationSyncPacket` 更新 `ClientRadiationData`。
  - `PlayerPropertiesPacket` 更新 `ClientPlayerSyncData` 的 typed `ResourceLocation -> CompoundTag`。
  - `PermaSyncPacket` 更新 `ClientPermaSyncData` 的全局 `CompoundTag`。
- 本批补齐客户端监听能力：
  - 新增 `ClientPlayerSyncDataListener`：
    - `onClientPlayerSyncData(ResourceLocation dataType, CompoundTag data)`。
    - `ClientPlayerSyncData.update(...)` 写入缓存后通知监听器。
    - 新增 `addListener/removeListener/clearListeners`。
  - 新增 `ClientPermaSyncDataListener`：
    - `onClientPermaSyncData(CompoundTag data)`。
    - `ClientPermaSyncData.update(...)` 写入缓存后通知监听器。
    - 新增 `addListener/removeListener/clearListeners`。
  - 新增 `ClientRadiationDataListener`：
    - `onClientRadiationData(PlayerRadiationSyncData data)`。
    - `ClientRadiationData.update(...)` 写入字段后用 `snapshot()` 通知监听器。
    - 新增 `snapshot()`、`addListener/removeListener/clearListeners/clearAll`。
- 客户端 world lifecycle 清理增强：
  - `ClientForgeEvents.clearNetworkState()` 现在也调用 `ClientRadiationData.clearAll()`。
  - 玩家 typed data、perma data、radiation data、panel data 的 listener 都会在离开世界时清掉，避免 GUI/HUD 对象残留到下一次进世界。
- 当前限制：
  - listener 是轻量进程内列表，不做弱引用；GUI/Screen 关闭时仍应主动 remove，离开世界清理只是保险。
  - 本批只提供更新通知和缓存快照，不定义具体 `dataType`；旧 `HbmLivingProps` / `HbmPlayerProps` 字段拆分仍由后续玩家属性/辐射库迁移决定。
  - `ClientRadiationData.clearAll()` 不通知 listener；它用于世界卸载时的静默清理。

## 2026-05-25 通用客户端网络缓存监听推进记录

- 现代网络库已有多组客户端缓存：
  - `ClientBinaryData`：承载旧 `SerializableRecipePacket` 替代路径、配方/配置/大块客户端数据等 `channel + name -> byte[]`。
  - `ClientBiomeSyncData`：承载旧 `BiomeSyncPacket` 的单格/整 chunk biome short 数组。
  - `ClientInformMessages`：承载旧 `PlayerInformPacket` 的 HUD 文本通知。
  - `ClientMuzzleFlashEffects`：承载旧 `MuzzleFlashPacket` 的实体 muzzle flash 时间戳。
- 本批补齐这些缓存的事件通知能力：
  - 新增 `ClientBinaryDataListener`：
    - `onClientBinaryData(ResourceLocation channel, String name, byte[] payload, boolean cleared, int readyVersion)`。
    - `put(...)`、`clear(...)`、`markReady(...)` 后通知监听器。
    - 大数据分片完成后仍经 `put(...)`，因此也会通知。
  - 新增 `ClientBiomeSyncDataListener`：
    - `onClientBiomeSyncData(int chunkX, int chunkZ, short[] biomes, boolean fullChunk)`。
    - 单格更新会传回更新后的 chunk 快照并标记 `fullChunk=false`。
    - 整 chunk 更新标记 `fullChunk=true`。
  - 新增 `ClientInformMessageListener`：
    - `onClientInformMessage(Component message, int id, int millis)`。
    - `ClientInformMessages.show(...)` 更新 HUD 缓存后通知。
  - 新增 `ClientMuzzleFlashListener`：
    - `onClientMuzzleFlash(int entityId, long timestampMillis)`。
    - `ClientMuzzleFlashEffects.mark(...)` 写入时间戳后通知。
- 生命周期规则：
  - 上述缓存的 `clearAll()` 均清掉 listener。
  - `ClientForgeEvents.clearNetworkState()` 既有离开世界清理路径会间接清理这些 listener，避免 GUI/HUD/渲染对象跨世界残留。
- 当前限制：
  - listener 仍为进程内轻量列表，不做弱引用；注册者在关闭 GUI 或 renderer 生命周期结束时应主动 remove。
  - `ClientBinaryData.markReady(...)` 的通知使用空 `name/payload`，消费者应通过 `readyVersion` 判断批次完成，而不是把它当作数据条目。
  - `ClientBiomeSyncDataListener` 接收的是 short[] 快照；具体 biome id 到 Holder/ResourceKey 的现代解析仍由世界/biome 库负责。

## 2026-05-25 Tile/Entity 同步重发缓存生命周期与诊断推进记录

- 现代已有可靠恢复路径：
  - `EntitySyncPacket` 客户端找不到 `HbmEntitySyncable` 接收端时，会限频发送 `EntitySyncRequestPacket`。
  - `TileSyncPacket` 客户端找不到 `HbmTileSyncable` 接收端时，会限频发送 `TileSyncRequestPacket`。
  - 二者当前 cooldown 均为 20 tick，避免实体/方块实体还没创建好时形成请求风暴。
- 本批补齐重发请求缓存生命周期：
  - `EntitySyncPacket` 新增：
    - `clearClientResyncRequests()`
    - `pendingClientResyncRequests()`
    - `clientResyncRequestCooldownTicks()`
  - `TileSyncPacket` 新增同名 API。
  - `ClientForgeEvents.clearNetworkState()` 离开世界时清理 tile/entity resync cooldown 表，避免换存档或重连后旧位置/旧 entity id 影响新世界同步恢复。
- 诊断命令增强：
  - `/hbm network packetthreading stats` 追加输出：
    - `Client resync cooldowns: tile=<count> entity=<count> cooldownTicks=<ticks>`。
  - 这项主要服务集成客户端/调试环境；dedicated server 上客户端静态缓存通常为 0。
- 当前限制：
  - 该诊断只统计本 JVM 内的客户端 cooldown map，不代表服务端所有玩家的重发请求状态。
  - 本批不改变请求距离限制：Tile 仍为 32 格，Entity 仍为 64 格。
  - 未增加自动延迟重放；若接收端长期不存在，仍由已有 cooldown 限频重发请求。

## 2026-05-25 旧 C2S 特化操作包 helper 收束推进记录

- 旧版剩余 C2S 特化包确认：
  - `SatCoordPacket`：`x/y/z/freq`，服务端要求玩家手持 `ItemSatInterface`，且手持物品频率等于包内频率，随后调用卫星 `onCoordAction(world, player, x, y, z)`。
  - `SatLaserPacket`：`x/z/freq`，同样校验手持卫星接口和频率，随后调用卫星 `onClick(world, x, z)`。
  - `AnvilCraftPacket`：`recipeIndex/mode`，服务端要求当前 container 为 `ContainerAnvil`、recipe index 合法、铁砧 tier 合法，然后按 mode 尝试批量扣输入并给产物。
  - `NBTItemControlPacket`：单个 `NBTTagCompound`，服务端只派发给手持物品实现 `IItemControlReceiver` 的情况。
  - `ItemDesignatorPacket` 与 `ItemBobmazonPacket` 已可由现代 `ItemActionPacket` 承载。
- 现代承载路径：
  - 卫星坐标/激光仍走 `CoordinateActionPacket` + `HbmCoordinateActionReceiver`，让后续 `sat_interface` 物品保留旧频率校验和卫星 saved data 业务。
  - 铁砧构造动作走 `TypedMenuActionPacket` + `HbmTypedMenuActionReceiver`，让后续现代 anvil menu 在自身权限/recipe/tier 校验后处理。
  - 物品 NBT 控制走 `ItemControlPacket` + `HbmItemControlReceiver`，对应旧 `NBTItemControlPacket`。
- 本批新增 `HbmNetworkActions` 常量表：
  - `DESIGNATOR`
  - `BOBMAZON_OFFER`
  - `ANVIL_CRAFT`
  - `SATELLITE_COORDINATE`
  - `SATELLITE_LASER`
  - `NBT_ITEM_CONTROL`
  - `VAULT_DOOR`
  - `SIREN`
- `ModMessages` helper 收束：
  - `sendNbtItemControl(...)`：旧 `NBTItemControlPacket` 名义入口，内部走 `ItemControlPacket`，并写入 `legacyPacket=hbm:nbt_item_control` 便于接收端区分来源。
  - `sendItemAction(hand, actionType)` overload：发送空 tag typed item action。
  - `sendDesignatorAction(...)` / `sendBobmazonOffer(...)` 改用 `HbmNetworkActions` 常量。
  - `sendSatelliteCoordinateAction(...)` / `sendSatelliteLaserAction(...)` 在 data 中写入 typed action 标识。
  - 新增旧名别名 `sendSatCoord(...)` / `sendSatLaser(...)`。
  - `sendTypedMenuAction(...)` 增加空 tag overload；`sendAnvilCraftAction(...)` 改用 `HbmNetworkActions.ANVIL_CRAFT`；新增旧名别名 `sendAnvilCraft(...)`。
  - `sendVaultDoorEvent(...)` / `sendSirenEvent(...)` 也改用统一常量，避免客户端 tile event 类型字符串散落。
- 当前限制：
  - 本批不实现卫星 saved data、sat interface 物品、Bobmazon 商品逻辑或 anvil recipe 执行；网络库只提供旧包语义的安全入口。
  - 旧 `AnvilCraftPacket` 的批量 craft 规则仍由后续 anvil menu/recipe runtime 迁移时实现。
  - `sendNbtItemControl(...)` 不强制接收端必须识别 `legacyPacket`，它只是兼容调试与逐步迁移标记。

## 2026-05-25 旧 S2C 特化包 helper 与客户端缓存诊断推进记录

- 旧版剩余 S2C 特化包承载确认：
  - `TEVaultPacket`：`x/y/z + isOpening + state + sysTime + type`，客户端直接改 `TileEntityBlastDoor` 的开门状态；现代通过 `ClientTileEventPacket` + `HbmClientTileEventReceiver` 承载。
  - `TESirenPacket`：`x/y/z + id + active`，客户端启动/停止 siren loop sound；现代通过 `ClientTileEventPacket` 承载，并保留 `sendSirenEvent(...)` helper。
  - `TEFFPacket`：`x/y/z + radius + health + maxHealth + power + active + color + cooldown`，客户端更新 force field TE 字段；现代通过 `TileSyncPacket` + `HbmTileSyncable` 承载。
  - `SatPanelPacket`：`type + satellite NBT`，客户端更新卫星面板当前数据；现代通过 `ClientPanelDataPacket` + `ClientPanelData` 承载。
- 本批新增/收束的 action 常量：
  - `SATELLITE_PANEL`
  - `FORCE_FIELD`
- `ModMessages` helper 增强：
  - 新增 `syncSatellitePanelData(ServerPlayer, legacyType, CompoundTag)`，作为旧 `SatPanelPacket` 的现代入口。
  - 新增 `sendForceFieldState(...)` 别名，内部复用既有 `syncForceFieldState(...)`，便于后续旧 `TEFFPacket` 调用点按旧语义迁移。
  - `sendVaultDoorEvent(...)`、`sendSirenEvent(...)` 继续通过 `HbmNetworkActions` 常量走 tile event。
- 客户端缓存只读诊断：
  - `ClientBinaryData` 新增 `channelCount()`、`entryCount()`、`readyChannelCount()`。
  - `ClientBiomeSyncData` 新增 `chunkCount()`。
  - `ClientPanelData` 新增 `panelCount()`。
  - `ClientInformMessages` 新增 `noticeCount()`。
  - `ClientMuzzleFlashEffects` 新增 `flashCount()`。
  - `ClientPermaSyncData` 新增 `keyCount()`。
  - `ClientPlayerSyncData` 新增 `entryCount()`。
  - `ClientTileBinaryData` 新增 `pendingChunkCount()`。
- `/hbm network packetthreading stats` 追加输出：
  - `Client network caches: binaryChannels=... binaryEntries=... readyChannels=... binaryTransfers=... tileBinaryTransfers=... tileBinaryChunks=...`
  - `Client sync caches: biomeChunks=... panelTypes=... playerData=... permaKeys=... radiationEffects=...`
  - `Client transient effects: notices=... muzzleFlashes=...`
- 当前限制：
  - 这些诊断统计的是当前 JVM 静态客户端缓存；dedicated server 上通常为 0。
  - 本批不实现 siren sound 细节、blast door renderer 或卫星对象反序列化，只把网络承载入口和诊断面补齐。
  - `FORCE_FIELD` 目前是常量标识；实际 force field BlockEntity 仍应通过 `HbmTileSyncable` 应用字段。

## 2026-05-25 旧包到现代协议映射诊断推进记录

- 迁移痛点：
  - 1.7.10 `PacketDispatcher` 使用数字 discriminator 注册旧包，很多旧包现在被拆分或合并进 typed action/tile event/cache 包。
  - 只看现代 `SimpleChannel` 注册表时，后续迁移机器/GUI 很难快速判断“旧调用点应该走哪个现代 helper”。
- 本批新增 `ModMessages.LegacyPacketMapping` 只读表：
  - 字段：`legacyName`、`modernName`、`direction`、`notes`。
  - 不参与 `SimpleChannel` 注册，不改变 packet id 或协议兼容顺序。
  - 记录一对多/多对一路径，例如：
    - `ExtPropPacket -> PlayerRadiationSyncPacket / PlayerPropertiesPacket`
    - `AuxButtonPacket -> LegacyButtonPacket / ServerTileActionPacket`
    - `BufPacket -> ClientTileBinaryDataPacket / ClientTileBinaryDataChunkPacket`
    - `SerializableRecipePacket -> ClientBinaryDataPacket / ClientBinaryDataChunkPacket`
    - `TEVaultPacket`、`TESirenPacket` -> `ClientTileEventPacket`
    - `TEFFPacket -> TileSyncPacket`
    - `SatPanelPacket -> ClientPanelDataPacket`
    - `AnvilCraftPacket -> TypedMenuActionPacket`
- `ModMessages` 新增：
  - `legacyPacketMappings()`
  - `legacyPacketMappingCount()`
- 网络诊断命令增强：
  - `/hbm network protocol summary` 现在显示 `legacyMappings=<count>`。
  - `/hbm network protocol packets` 会在每个现代包后显示 `legacyMappings=<count>`。
  - 新增 `/hbm network protocol mappings`，逐条输出旧包名、现代包名、方向和迁移说明。
- 当前限制：
  - 该表是人工维护的工程迁移索引，不是运行时兼容层；后续新增/替换包时需要同步更新。
  - 旧包业务行为仍以各 receiver/menu/item/block entity 迁移为准，mapping 只说明网络承载路径。

## 2026-05-25 旧 PacketDispatcher 覆盖率诊断推进记录

- 旧版事实来源：
  - `com.hbm.packet.PacketDispatcher.registerPackets()` 按 discriminator 0-26 注册 27 个旧包。
  - 该列表是 1.7.10 网络包迁移覆盖率的基准，不包括后续现代新增的 typed/chunk/helper 包。
- 本批新增 `ModMessages.LegacyPacketRegistration` 只读表：
  - 字段：`legacyId`、`legacyName`、`direction`。
  - 按旧 `PacketDispatcher` discriminator 顺序记录：
    - `TESirenPacket`
    - `ItemDesignatorPacket`
    - `SatLaserPacket`
    - `AuxButtonPacket`
    - `TEVaultPacket`
    - `SatPanelPacket`
    - `ParticleBurstPacket`
    - `ExtPropPacket`
    - `TEFFPacket`
    - `ItemBobmazonPacket`
    - `TEMissileMultipartPacket`
    - `AuxParticlePacketNT`
    - `SatCoordPacket`
    - `HbmAnimationPacket`
    - `PlayerInformPacket`
    - `KeybindPacket`
    - `NBTControlPacket`
    - `AnvilCraftPacket`
    - `ExplosionKnockbackPacket`
    - `ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket`
    - `NBTItemControlPacket`
    - `PermaSyncPacket`
    - `BiomeSyncPacket`
    - `BufPacket`
    - `SerializableRecipePacket`
    - `HeldItemNBTPacket`
    - `MuzzleFlashPacket`
- `ModMessages` 新增覆盖率 API：
  - `legacyPacketRegistrations()`
  - `legacyPacketRegistrationCount()`
  - `mappedLegacyPacketCount()`
  - `unmappedLegacyPacketRegistrations()`
- 网络诊断命令增强：
  - `/hbm network protocol summary` 追加 `legacyRegistered=<count>`、`legacyMapped=<count>`、`legacyUnmapped=<count>`。
  - 新增 `/hbm network protocol legacy`，按旧 discriminator 顺序列出旧包，并标记 `mapped=<count>` 或 `unmapped`。
  - `/hbm network protocol mappings` 顶部追加覆盖率摘要；如果存在未映射旧包，会在尾部列出。
- 当前覆盖状态：
  - 27 个旧 `PacketDispatcher` 注册包均已有至少一条现代承载映射。
  - 一对多映射仍会让 mapping row 数大于旧注册包数，这是预期行为，例如 `ExtPropPacket`、`AuxButtonPacket`、`BufPacket`。
- 当前限制：
  - 覆盖率表示“网络承载路径已存在”，不等于对应机器/物品/GUI 业务逻辑已完整迁移。
  - 若后续发现旧分支/条件需要拆出新的现代包或 action，应同步更新 mapping 与 registration 诊断。

## 2026-05-25 网络包库进度口径与 progress 命令推进记录

- 进度拆分口径：
  - 旧包承载覆盖率：`mappedLegacyPacketCount / legacyPacketRegistrationCount`。
  - 网络库基础功能可用度：人工维护的保守百分比，用于表达协议、helper、缓存、诊断、线程/分片基础设施整体成熟度。
- 当前数值：
  - 旧 `PacketDispatcher` 注册包承载覆盖率：27/27 = 100%。
  - 网络包库基础功能可用度：96%。
- 为什么不是 100%：
  - 网络承载路径已覆盖旧注册包，但不少旧包的业务接收端仍依赖后续机器/物品/GUI 迁移，例如卫星 saved data、anvil recipe 执行、siren loop sound、blast door renderer、force field BlockEntity 字段应用。
  - 后续具体功能迁移时仍可能发现旧包分支需要新增 typed action 或额外 helper。
- `ModMessages` 新增：
  - `libraryFoundationProgressPercent()`
  - `legacyPacketCoveragePercent()`
  - `progressSummary()`
- 网络诊断命令增强：
  - 新增 `/hbm network protocol progress`。
- 输出示例：`legacyPacketCoverage=100% (27/27) foundation=96% note=remaining work is mostly receiver/business integration`。

## 2026-05-25 旧 BufPacket provider 接口与 networkPackNT helper 推进记录

- 旧版事实来源：
  - `IBufPacketReceiver` 只有两个方法：`serialize(ByteBuf)` 与 `deserialize(ByteBuf)`。
  - `BufPacket` 包体为 `x/y/z + receiver.serialize(buf)`。
  - 客户端按坐标找 TileEntity，若实现 `IBufPacketReceiver` 则调用 `deserialize(buf)`；异常只记录 warn。
  - `TileEntityLoadedBase#networkPackNT(range)` 是大量旧机器/管线/炮塔复用的入口：构造 `BufPacket`，对比预编译 buffer 避免大多数重复包，并每 20 tick 允许一次重复同步，最后通过 `PacketThreading.createAllAroundThreadedPacket(...)` 按范围发送。
- 现代网络库已有：
  - `ClientTileBinaryDataPacket` / `ClientTileBinaryDataChunkPacket`：按 `BlockPos + channel + payload` 派发给客户端 `HbmClientTileBinaryReceiver`。
  - `sendClientTileBinaryData(...)` helper：支持指定玩家或 chunk tracking，自动按 1 MiB 阈值分片。
- 本批新增现代 provider：
  - `HbmTileBinarySyncProvider`：
    - `getClientTileBinarySyncChannel()` 默认返回 `hbm:buf_packet`。
    - `writeClientTileBinaryData(FriendlyByteBuf data)` 由服务端 BlockEntity 写客户端二进制同步数据。
  - `HbmLegacyBufPacketReceiver`：
    - 同时继承 `HbmTileBinarySyncProvider` 与 `HbmClientTileBinaryReceiver`。
    - 提供旧名风格 `serializeLegacyBufPacket(...)` / `deserializeLegacyBufPacket(...)`。
    - 默认只消费 `hbm:buf_packet` channel，便于旧 `IBufPacketReceiver` 迁移时一对一改名。
- 本批新增 helper：
  - `syncTileBinaryToTracking(provider, blockEntity)`：发送给追踪该 chunk 的玩家。
  - `syncTileBinaryToPlayer(provider, blockEntity, player)`：发送给指定玩家。
  - `syncTileBinaryAround(provider, blockEntity, range)`：按旧 `networkPackNT(range)` 的空间范围发送。
  - `syncTileBinaryAroundThreaded(provider, blockEntity, range)`：范围发送并走 `ThreadedPacketDispatcher`。
  - 上述方法均支持显式 `ResourceLocation channel` overload。
  - 范围发送现在也复用 `ClientTileBinaryDataPacket` / `ClientTileBinaryDataChunkPacket` 的单包/分片路径，避免大型 payload 超过包体上限。
- 当前限制：
  - 本批不把旧 `TileEntityLoadedBase#lastPackedBuf` 去重缓存做成全局状态；这个状态和具体 BlockEntity 生命周期绑定，后续现代机器基类迁移时再按旧语义复刻。
  - `HbmLegacyBufPacketReceiver` 不自动保存 NBT，也不解释旧机器字段；它只提供 ByteBuf 同步路径。
  - writer 仍应在服务端主线程读取方块实体状态后写 buffer；线程化 helper 只线程化发送操作。

## 2026-05-25 旧 networkPackNT 去重/兜底同步推进记录

- 旧版事实来源：
  - `TileEntityLoadedBase#networkPackNT(range)` 先构造 `BufPacket` 并取 `packet.getCompiledBuffer()`。
  - 若本次 buffer 与 `lastPackedBuf` 相同，并且世界时间不是 20 tick 的倍数，则跳过发送。
  - 这样既减少高频重复包，又每秒兜底重发一次，避免客户端卸载/重载 chunk 后只看到默认 TileEntity 状态。
- 本批新增 `HbmTileBinarySyncState`：
  - 保存上一次 payload 快照。
  - 默认 `LEGACY_FORCED_RESEND_INTERVAL_TICKS = 20`。
  - `shouldSend(payload, gameTime)` 复刻旧“内容变化或每 20 tick 兜底发送”的判断。
  - 暴露 `lastPayloadBytes()`、`totalSent()`、`skippedDuplicates()`，便于后续具体机器或诊断查看同步行为。
- `ModMessages` 新增 `IfChanged` helper：
  - `syncTileBinaryToTrackingIfChanged(...)`
  - `syncTileBinaryToPlayerIfChanged(...)`
  - `syncTileBinaryAroundIfChanged(...)`
  - `syncTileBinaryAroundThreadedIfChanged(...)`
  - 这些 helper 会先由 provider 写出 payload，再交给 `HbmTileBinarySyncState` 判断是否发送。
- `ModMessages` 新增旧名入口：
  - `networkPackNT(provider, blockEntity, range, syncState)`
  - `networkPackNT(provider, blockEntity, channel, range, syncState)`
  - 默认走线程化范围发送，对齐旧 `PacketThreading.createAllAroundThreadedPacket(...)`。
- 当前限制：
  - `HbmTileBinarySyncState` 必须由具体 BlockEntity 持有；网络库不把状态存进全局 map，避免方块卸载后残留。
  - `syncState == null` 时 helper 会总是发送，便于临时/一次性同步。
  - 单玩家 `IfChanged` 会使用同一个状态对象；如果需要“每个玩家单独补发”语义，具体机器应为单玩家同步使用独立 state 或直接调用非去重 helper。

## 2026-05-25 Tile binary 客户端缺接收端重发请求推进记录

- 背景：
  - 现代 `TileSyncPacket` / `EntitySyncPacket` 已有“客户端接收端暂缺时限频请求服务端重发”的恢复路径。
  - 旧 `BufPacket` 覆盖大量 TileEntity 的二进制同步；客户端刚加载 chunk、BlockEntity 尚未构建完成时，首次包可能找不到 `HbmClientTileBinaryReceiver`。
- 本批新增 C2S `ClientTileBinarySyncRequestPacket`：
  - 包体为 `BlockPos + ResourceLocation channel`。
  - 服务端要求存在 sender、玩家距离目标 32 格内、目标 chunk 已加载。
  - 目标 BlockEntity 必须实现 `HbmTileBinarySyncProvider`。
  - 通过 `canSendClientTileBinaryDataTo(player, channel)` 后，服务端调用 `ModMessages.syncTileBinaryToPlayer(...)` 重发该 channel 的 tile binary 数据。
- `HbmTileBinarySyncProvider` 新增：
  - `canSendClientTileBinaryDataTo(ServerPlayer, ResourceLocation)`，默认只允许自身 `getClientTileBinarySyncChannel()`。
  - 后续多 channel 机器可按 channel 显式放行，避免客户端随意请求未公开的二进制视图。
- `ClientTileBinaryData` 增强：
  - 当 chunk 已加载但目标 BlockEntity 不实现 `HbmClientTileBinaryReceiver` 时，按 `BlockPos + channel` 20 tick 限频发送重发请求。
  - `clearClientResyncRequests()`、`pendingClientResyncRequests()`、`clientResyncRequestCooldownTicks()` 供 lifecycle 与诊断使用。
  - `clearAll()` 会同时清理未完成分片与重发请求缓存。
- 客户端 lifecycle 与诊断：
  - `ClientForgeEvents.clearNetworkState()` 显式清理 tile binary resync 请求。
  - `/hbm network packetthreading stats` 的 `Client resync cooldowns` 追加 `tileBinary=...`。
- 当前限制：
  - 请求只重发当前 channel 的 provider payload，不保存旧分片 transfer id，也不尝试重传某个丢失 chunk。
  - 若客户端 chunk 尚未加载，仍不发送请求；等待后续服务端周期同步或具体机器再次发送。
  - 对每个玩家独立的权限/频道策略由具体 `HbmTileBinarySyncProvider` 实现。

## 2026-05-25 线程包预备快照推进记录

- 旧版事实来源：
  - `NetworkHandler.PrecompilingNetworkCodec` 在编码 `ThreadedPacket` 时直接写入 `getCompiledBuffer()`。
  - `PacketThreading.preparePacket(...)` 会在入队前调用 `PrecompiledPacket#getCompiledBuffer()`，把可变 packet 数据固化到 ByteBuf，避免异步发送时读取到后续 tick 被改动的对象。
  - `AuxParticlePacketNT` 继承 `ThreadedPacket`，NBT 粒子包是旧高频线程发送的典型使用者。
- 本批新增现代等价契约：
  - `HbmPreparablePacket`：提供 `prepareForThreadedSend()`，作为现代轻量版 `ThreadedPacket#compile()`。
  - `ThreadedPacketDispatcher` 在所有线程化发送入口入队前调用该方法；非实现类沿用原消息对象。
  - prepare 返回 `null` 或抛异常时，该发送会被丢弃并记录 `totalPrepareFailed` / `lastFailureMessage`，避免坏包进入线程队列。
- 已接入的高频/大 payload 包：
  - `AuxParticlePacket`：构造和 prepare 都复制 `CompoundTag`，对齐旧 `AuxParticlePacketNT` 的预编译语义。
  - `ClientTileBinaryDataPacket` / `ClientTileBinaryDataChunkPacket`：prepare 重新构造包并复制 payload，配合 `networkPackNT(...)` 的线程化范围发送。
  - `CompressedExplosionEffectPacket`：prepare 重新构造不可变 affected block 列表，方便后续爆炸效果选择线程化发送时不读共享列表。
- 诊断增强：
  - `ThreadedPacketDispatcher.Snapshot` 新增 `totalPrepared`、`totalPrepareFailed`。
  - `/hbm network packetthreading stats` 输出 prepared 和 prepareFailed 计数。
- 当前限制：
  - 现代实现不复刻旧 Netty `ByteBuf`/discriminator 级预编码，只保证高频线程发送前做安全快照；真正编码仍交给 Forge `SimpleChannel`。
  - 只有实现 `HbmPreparablePacket` 的包会获得自定义快照；后续新增高频包时应显式实现该接口。

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
- 2026-05-24 实体事件与实体动作批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-24 typed 物品/菜单动作与旧 Tile 特化包映射批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-24 legacy 物品动画兼容与 panel 数据监听批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-24 AuxParticlePacketNT helper 与客户端网络缓存生命周期批次：首次编译被当前工作区 casing 粒子半成品阻塞；修正 `SpentCasingDefinition` builder/getter 冲突后，`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-24 实体同步重发与客户端临时状态清理批次：首次编译遇到并行工作区新增 `BalefireBombBlock` 未进入增量编译视图；重跑 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-24 协议注册表快照与网络诊断命令批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-24 导弹 multipart 快照与旧 BufPacket 映射批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 typed Tile action 与旧 AuxButton/NBTControl helper 批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 玩家/perma/辐射客户端同步监听批次：首次编译遇到并行工作区 `HbmParticleEffects` 中 `BoneDefinition` record accessor 未使用导致编译失败；修正后 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 通用客户端网络缓存监听批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 Tile/Entity 同步重发缓存生命周期与诊断批次：首次编译遇到并行工作区 `ChemicalPlantBlockEntity` 流体接口和 `AmatFlashParticle`/`TauSparkParticle` 1.20.1 渲染 API 编译断点；最小修正后 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 旧 C2S 特化操作包 helper 收束批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 旧 S2C 特化包 helper 与客户端缓存诊断批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 旧包到现代协议映射诊断批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 旧 PacketDispatcher 覆盖率诊断批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 2026-05-25 网络包库进度口径与 progress 命令批次：首次增量编译遇到并行工作区旧 `LegacyHazardSourceBlock$Effect.class` 中间产物缺失；重跑 `.\gradlew.bat clean compileJava processResources --no-daemon` 通过。
- 2026-05-25 旧 BufPacket provider 接口与 networkPackNT helper 批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过；网络库基础功能可用度调整为 93%。
- 2026-05-25 旧 networkPackNT 去重/兜底同步批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过；网络库基础功能可用度调整为 94%。
- 2026-05-25 Tile binary 客户端缺接收端重发请求批次：首次编译遇到并行工作区 `ClientModEvents` 缺 `ModBlocks` import；最小补齐后，`.\gradlew.bat compileJava processResources --no-daemon` 通过；网络库基础功能可用度调整为 95%。
- 2026-05-25 线程包预备快照批次：`.\gradlew.bat compileJava processResources --no-daemon` 通过；网络库基础功能可用度调整为 96%。
