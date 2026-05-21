# 渲染库 1.7.10 源码功能追踪

## 范围

- 本文记录后续“渲染库大排查”的总入口，覆盖 1.7.10 客户端渲染注册、OBJ/HMF/动画模型加载、模型资源管理、方块实体/实体/物品 renderer、HUD/overlay 与渲染辅助工具。
- 目标是逐批复刻 1.7.10 的渲染逻辑与可复用接口，到 1.20.1 port 中形成统一渲染库入口，供后续机器移植稳定接入。
- 本轮先建立入口与第一层接口，不直接迁移 Java 逻辑结构。

## 1.7.10 总入口

- `src/main/java/com/hbm/main/ClientProxy.java`
  - `registerPreRenderInfo()` 是预渲染入口。
    - `AdvancedModelLoader.registerModelHandler(new HmfModelLoader())`
    - `QMAWLoader.registerModFileURL(...)`
  - `registerRenderInfo()` 是核心入口。
  - 注册客户端事件：
    - `ModEventHandlerClient`
    - `ModEventHandlerRenderer`
    - `EventHandlerParticleEngine`
    - `RenderInfoSystem`
  - 注册资源重载监听：`Minecraft.getMinecraft().getResourceManager()` -> `IReloadableResourceManager`。
    - `QMAWLoader`
    - `HFRModelReloader`
  - 注册客户端事件：`MinecraftForge.EVENT_BUS.register(...)`。
  - 方块实体 renderer：`ClientRegistry.bindTileEntitySpecialRenderer(...)`。
  - 实体 renderer：`RenderingRegistry.registerEntityRenderingHandler(...)`。
  - ISBRH 方块 renderer：`RenderingRegistry.registerBlockHandler(...)`。
  - 物品自定义 renderer：`MinecraftForgeClient.registerItemRenderer(...)`。
- 本轮复查计数：
  - `bindTileEntitySpecialRenderer`：235 处。
  - `registerEntityRenderingHandler`：140 处。
  - `registerBlockHandler`：57 处。
  - `registerItemRenderer`：90 处。
  - `registerClientEventHandler`：5 处。
  - `registerReloadListener`：2 处。
- 现代对应：`src/main/java/com/hbm/ntm/client/ClientModEvents.java`
  - `EntityRenderersEvent.RegisterRenderers`
  - `ModelEvent.RegisterAdditional`
  - `FMLClientSetupEvent`

## 1.7.10 资源与模型库入口

- `src/main/java/com/hbm/main/ResourceManager.java`
  - 旧版最重要的模型常量库。
  - 集中保存 `IModelCustom`、`IModelCustomNamed`、`AnimatedModel`、`Animation`、`BusAnimation` 等。
  - 常见加载方式：
    - `new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/...obj"))`
    - `new HFRWavefrontObject("models/...obj")`
    - `AdvancedModelLoader.loadModel(new ResourceLocation(...))`
  - 常见行为：
    - `.asVBO()`
    - `.noSmooth()`
  - 资源路径基于 `assets/hbm/models/...` 和 `assets/hbm/textures/models/...`。
  - 本轮复查计数：
    - `HFRWavefrontObject` 出现约 325 次。
    - `AdvancedModelLoader.loadModel` 出现约 131 次。
    - `ResourceLocation` 常量出现约 1241 次。
- `src/main/java/com/hbm/render/loader`
  - `HFRWavefrontObject`：旧 OBJ 解析器，支持 group、triangle/quad、UV flip、normal、`renderOnly`、`renderPart`、`renderAllExcept`、`tessellate*`、`getPartNames()`。
  - `HFRWavefrontObjectVBO`：把 `HFRWavefrontObject` group 转为 VBO，接口同样保留 `renderOnly` / `renderPart` / `renderAllExcept`。
  - `HFRModelReloader`：资源包重载后重新读取 OBJ 并刷新 VBO。
  - `HmfModelLoader` / `HbmModelObject` / `HmfController`：HMF 模型路径，当前确认 `ResourceManager.tom_flame` 使用 `models/weapons/tom_flame.hmf`，`TomPronter` 会调用 `HmfController.setMod/resetMod` 做动画偏移。
- `src/main/java/com/hbm/animloader`
  - `ColladaLoader`、`AnimatedModel`、`Animation`、`AnimationWrapper`、`AnimationController`、`Transform` 是独立的 DAE/Collada 动画模型系统。
  - 当前确认 `ResourceManager.transition_seal` / `transition_seal_anim` 使用 `models/doors/seal.dae`，`RenderDoorGeneric` 通过 `AnimatedModel.renderAnimated(...)` 驱动门动画。
- `src/main/java/com/hbm/render/anim`
  - `AnimationLoader`、`BusAnimation`、`BusAnimationSequence`、`BusAnimationKeyframe`、`HbmAnimations` 是 JSON/keyframe bus 动画系统，主要服务枪械和部分手持物品，不应与 Collada 动画混为一层。
- 现代对应：`src/main/java/com/hbm/ntm/client/obj/ObjModelLibrary.java`
  - 目前已收录 press、灯具、trinket 等少量 OBJ 入口。
  - 后续不应在 renderer 内继续硬编码资源路径。

## 1.7.10 渲染辅助接口

- `src/main/java/com/hbm/render/util/ObjUtil.java`
  - `renderWithIcon(...)`
  - `renderPartWithIcon(...)`
  - `setColor(...)`
  - `clearColor()`
  - `getPitch(ForgeDirection)`
  - `getYaw(ForgeDirection)`
  - 旧语义：模型默认朝向 `+X/EAST`，旋转顺序为 roll -> pitch -> yaw，shadow 按法线估算。
- 现代对应：
  - `src/main/java/com/hbm/ntm/client/obj/ObjRenderUtils.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjModelPart.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjPartTransform.java`
- 当前已补：颜色、fullBright、legacy shadow、direct baked quad、额外 RenderType 覆盖。

## 1.7.10 事件、贴图与特殊渲染入口

- `src/main/java/com/hbm/main/ModEventHandlerClient.java`
  - 覆盖 HUD、FOV、玩家/生物预渲染、world last render、texture stitch、client tick 等。
  - `TextureStitchEvent.Pre/Post` 注册并刷新粒子、CT、动态图标等资源。
- `src/main/java/com/hbm/main/ModEventHandlerRenderer.java`
  - 覆盖玩家装备/手持物、枪械、block highlight、HUD overlay、雾效等渲染事件。
- `src/main/java/com/hbm/particle/psys/engine/EventHandlerParticleEngine.java`
  - 注册粒子系统 tick 与 `RenderWorldLastEvent` 渲染入口。
- `src/main/java/com/hbm/render/icon`
  - `TextureAtlasSpriteMutatable`、`RGBMutator`、`RGBMutatorInterpolatedComponentRemap` 支持动态变色/变体贴图。
- `src/main/java/com/hbm/render/block/ct`
  - `IBlockCT`、`CTStitchReceiver`、`IconCT`、`CTContext`、`RenderBlocksCT` 形成 connected texture 体系。
- `src/main/java/com/hbm/render/shader`
  - `ShaderManager`、`Shader`、`Uniform` 加载 `.vert/.frag` 并包装 GL20 program。
- `src/main/java/com/hbm/render/world`
  - `RenderNTMSkyboxImpact`、`RenderNTMSkyboxChainloader` 是自定义 skybox/天体渲染器，使用 display list、加色混合、depth mask、fog 等旧 GL 状态。
- `src/main/java/com/hbm/qmaw`
  - `QMAWLoader` 虽属于手册系统，但挂在客户端资源重载链上，迁移客户端 reload 架构时不能遗漏。

## 当前 1.20.1 port 状态

- 已有客户端入口：`ClientModEvents`
- 已有 OBJ 封装：
  - `ObjModelLibrary`
  - `ObjMachineModels`
  - `ObjLightModels`
  - `ObjTrinketModels`
  - `ObjPartModel`
  - `ObjModelPart`
  - `ObjRenderUtils`
  - `ObjAnimatedModel`
  - `ObjAnimation`
  - `ObjBlockEntityAnimation`
  - `ObjRenderContext`
  - `ObjPartTransform`
  - `LegacyObjTransforms`
  - `LegacyUntexturedQuadRenderer`
- 已接入 renderer：
  - `BasicMachineRenderer`
  - `LegacyLightBlockEntityRenderer`
  - `LegacyDemonLampBlockEntityRenderer`
  - `LegacyLanternBlockEntityRenderer`
  - `TrinketBlockEntityRenderer`
  - `TrinketItemRenderer`
- 已确认的渲染库策略：renderer 不直接拼资源路径，统一经由 `ObjModelLibrary` 或子库入口注册。
- 已有现代物品渲染桥入口：
  - `LegacyItemRendererBridge`
  - 用于对齐旧 `IItemRendererProvider` 的“一个 renderer 同时供方块实体与物品显示复用”模式。

## 2026-05-21 补充：电池包/电容与 battery socket 模型入口

- 1.7.10 对照：
  - `ResourceManager.battery_socket` 加载 `models/machines/battery.obj`。
  - `RenderBatterySocket` 渲染 `Socket`、可选 `Supports`，并按插入物品渲染 `Battery` 或 `Capacitor` part。
  - `ItemRenderBatteryPack` 物品渲染也复用同一个 OBJ，只渲染 `Battery` 或 `Capacitor`，贴图取自 `EnumBatteryPack#texture`。
- 本批现代库推进：
  - 复制旧模型到 `src/main/resources/assets/hbm/models/block/machines/battery.obj`。
  - 复制 `battery_socket.png`、`battery_sc.png` 到 `textures/block/machines`。
  - 新增四个 part JSON：
    - `models/block/machines/battery_socket_socket.json`
    - `models/block/machines/battery_socket_supports.json`
    - `models/block/machines/battery_pack_battery.json`
    - `models/block/machines/battery_pack_capacitor.json`
  - `ObjMachineModels.BATTERY_SOCKET` 以旧 part 名注册 `Socket`、`Supports`、`Battery`、`Capacitor`，供后续 `machine_battery_socket` renderer 和电池包 item renderer 共用。
- 后续库缺口：
  - 当前 Forge OBJ JSON 的贴图是静态模型贴图；旧 `ItemRenderBatteryPack` 是按 stack/meta 动态绑定 12 张贴图。后续需要在 `LegacyItemRendererBridge` 下补一个支持按 `ItemStack` 选择纹理/模型的电池包 renderer，或建立 12 个具体 3D item model 入口。
  - 迁移 `RenderBatterySocket` 时应直接使用 `ObjMachineModels.BATTERY_SOCKET`，不要在 renderer 中重新拼 `battery.obj` 路径。

### 2026-05-21 更新：battery_pack 物品动态贴图 renderer

- 已补 `src/main/java/com/hbm/ntm/client/renderer/BatteryPackItemRenderer.java`：
  - 现代 `HbmBatteryPackItem#initializeClient` 通过 `LegacyItemRendererBridge` 挂入 BEWLR。
  - 复用 `battery.obj` 拆出的 `battery_pack_battery` / `battery_pack_capacitor` Forge OBJ part 模型。
  - 按 `ItemStack` 的 `HbmBatteryPackItem#legacyTextureName` 动态绑定 `textures/block/machines/<name>.png`，对齐旧 `EnumBatteryPack#texture`。
  - GUI 场景保留旧 `ItemRenderBatteryPack#renderInventory` 的核心比例：物品栏使用 5 倍 OBJ 缩放。
- DataGen 已将 12 个电池/电容 item model 改为 `builtin/entity`，让自定义 renderer 承担显示。
- 仍需实机截图验证：
  - GUI 中是否居中、是否过大/过小。
  - 第一/第三人称手持角度是否接近旧版。
  - 地上掉落物是否可读。

## 分批对齐记录

### 第 1 轮

- `ObjRenderContext` 增加旧版风格状态：`withColor`、`clearColor`、`fullBright`、`withLegacyShadow`。
- `ObjModelPart` 把上下文状态传递给 `ObjRenderUtils`。
- `ObjRenderUtils` 支持颜色覆盖、fullBright、legacy shadow。
- 新增 `LegacyObjTransforms`。
- `BasicMachineRenderer` 改用 `rotateAroundBlockCenterY(...)`。

### 第 2 轮

- 新增 `LegacyObjModel`，对齐 `IModelCustom` / `IModelCustomNamed` 常用方法。
- `ObjAnimatedModel` 实现 `LegacyObjModel`。
- 确认当前 OBJ JSON 使用 Forge OBJ loader。
- 明确限制：单个 baked model 不能直接按 group 动态选取，需要拆 JSON 或做自定义 group 解析。

### 第 3 轮

- `LegacyObjTransforms` 新增 `applySixFaceAttachmentRotation(...)`。
- 新增 `LegacyUntexturedQuadRenderer`。
- 接入 `LegacyLanternBlockEntityRenderer` 与 `LegacyDemonLampBlockEntityRenderer` 的无贴图全亮几何。
- `LegacyLightBlockEntityRenderer` 的 spotlight yaw/pitch 改为走 `LegacyObjTransforms`。

### 第 4 轮

- `LegacyLightBlockEntity` 补齐 floodlight 的最小状态契约：`rotation`、`power`、`delay`、`isOn`。
- `LegacyLightBlockEntityRenderer` 的 floodlight `Lamps` part 复刻旧版逻辑：
  - 开启时 `fullBright()`
  - 关闭时 `withColor(0x404040)`
- 本轮只迁移状态与视觉，不实现能量消耗和 beam 逻辑。

### 第 5 轮

- `ObjModelLibrary` 向 1.7.10 `ResourceManager` 入口模式靠拢，新增：
  - `blockModel(String)`
  - `blockPart(String)` / `blockPart(String, RenderType)`
  - `blockPartBuilder(String, RenderType)` / `directBlockPart(String)`
  - `trinketPart(String, RenderType)`
  - `machinePart(String)` / `machinePart(String, RenderType)`
  - `machinePartBuilder(String, RenderType)` / `directMachinePart(String)`
- press、lamp、floodlight、demon lamp 等常量统一经由这些工厂声明。
- `TrinketBlockEntityRenderer` 改为调用 `ObjModelLibrary.trinketPart(...)`。

### 第 6 轮

- 当前进度口径：
  - 按“渲染库基础接口骨架”估算，约 `20% - 25%`。
  - 按“1.7.10 render 全覆盖”估算，约 `5% - 8%`。
  - 按“后续机器迁移可用度”估算，约 `30%`。
- 原因：
  - 旧版 `render` 目录约 583 个 Java 文件。
  - 旧 OBJ 资源约 493 个。
  - 当前只覆盖 press、灯具、trinket 等第一批样例。

### 第 7 轮

- `BasicMachineRenderer` 的 press Y 轴旋转已改为与 `machine_press` blockstate JSON 同步的 `Direction.toYRot()`。
- 旧 `RenderPress` 的 body/head 固定 180 度旋转语义，已在现代端收束为 blockstate + BER 组合。
- `RenderEPress` 仍待独立迁移，因为 clean port 尚未注册 `machine_epress` 方块与方块实体链。

### 第 8 轮

- `LegacyUntexturedQuadRenderer.lightning(...)` 不再直接使用 vanilla `RenderType.lightning()`。
- 原因：1.7.10 `RenderDemonLamp` 的光晕状态为 `GL_TEXTURE_2D` off、`GL_LIGHTING` off、`GL_CULL_FACE` off、`GL_BLEND` on、`glBlendFunc(SRC_ALPHA, ONE)`、`glDepthMask(false)`，但 1.20.1 vanilla `RenderType.lightning()` 默认仍会使用 cull 和 depth write。
- 新增专用 `hbm_legacy_additive_no_cull` RenderType：`POSITION_COLOR`、无贴图、`GameRenderer::getRendertypeLightningShader`、`SRC_ALPHA/ONE` 加色混合、`CullStateShard(false)`、`WriteMaskStateShard(true, false)`。
- 规则：旧版无贴图加色光晕优先通过该 RenderType 的 no-cull/no-depth-write 状态复刻，不应通过重复正反面 quad 来模拟双面可见；重复几何会放大透明排序和地形相交裂缝。

### 第 9 轮

- `RenderLantern` 与 `RenderDemonLamp` 的无贴图发光语义不同，不能共用同一个 RenderType：
  - `RenderLantern` 的 `Light` 分件只关闭贴图/光照并设置 fullbright RGB，没有开启 blend，也没有关闭 depth mask，因此应是不透明、写深度的模型分件。
  - `RenderDemonLamp` 的 aura 明确开启 `SRC_ALPHA/ONE` 加色混合并 `glDepthMask(false)`，应是透明光晕。
- `LegacyUntexturedQuadRenderer` 新增 `solid(...)`，使用专用 `hbm_legacy_solid_no_cull` RenderType：无贴图、`GameRenderer::getPositionColorShader`、`CullStateShard(false)`、`WriteMaskStateShard(true, true)`。
- 规则：旧版“实体灯芯/灯罩内发光模型分件”使用 `solid(...)`；旧版“外扩透明光晕/光束”使用 `lightning(...)`。

### 第 10 轮

- 对 1.7.10 源码再次复查后，确认 render-library 文档此前仍有以下缺漏：
  - `registerPreRenderInfo()`、`HmfModelLoader` 与 `QMAWLoader.registerModFileURL(...)` 未写入入口清单。
  - `QMAWLoader` 与 `HFRModelReloader` 两个资源重载监听未单独列为迁移契约。
  - `HFRWavefrontObject` 的 `tessellate*` 系列、VBO reload、HMF 模型、Collada DAE 动画、JSON bus 动画没有拆开记录。
  - `IItemRendererProvider` 是旧 TESR 到物品 renderer 的重要桥，旧版约 112 个 TESR 实现该接口；后续现代端应统一走 BEWLR/item renderer 桥，不应为每台机器散落重复注册逻辑。
  - `ITileActorRenderer` 约 4 个实现，涉及可被 tile actor 驱动的渲染动作，后续迁移对应机器时需要单独查状态源。
  - `ModEventHandlerClient`、`ModEventHandlerRenderer`、`EventHandlerParticleEngine` 覆盖 HUD、world last、texture stitch、FOV、装备/手持物、雾效、粒子等，不属于普通 BER，但属于完整 render-library。
  - `render/icon`、`render/block/ct`、`render/shader`、`render/world` 与 `particle` 包此前只粗略提到或未提到，已补为独立迁移入口。
- 结论：渲染库基础接口骨架仍约 `20% - 25%`，但“完整 render-library 入口追踪”从不完整提升到可继续逐项展开；真正 1.7.10 render 全覆盖仍约 `5% - 8%`。

### 第 11 轮

- 按领域拆分 `ObjModelLibrary`，避免继续膨胀成单文件资源表：
  - `ObjMachineModels`：机器模型入口，当前收录 press head / press animated model，并提供 `part(...)`、`partBuilder(...)`、`directPart(...)`。
  - `ObjLightModels`：灯具/旧光源模型入口，当前收录 cage lamp、fluorescent lamp、flood lamp、floodlight 三分件、demon lamp。
  - `ObjTrinketModels`：trinket 模型路径入口，统一 `block/trinkets/...`。
  - `ObjModelLibrary` 保留为统一注册与兼容 facade，继续负责 `ModelEvent.RegisterAdditional` 汇总注册。
- renderer 接入规则更新：
  - 新代码优先引用领域子库。
  - 旧调用 `ObjModelLibrary.PRESS`、`ObjModelLibrary.CAGE_LAMP` 等常量暂保留兼容，后续迁移时逐步改为领域库。
- 现代物品渲染桥第一层已补：
  - 新增 `LegacyItemRendererBridge.accept(...)`。
  - `TrinketBlockItem.initializeClient(...)` 改走该桥接入口，而不是每个 item 自己临时创建 `IClientItemExtensions`。
- 该桥用于后续对齐旧 `IItemRendererProvider` 的集中注册思路；目前只迁入最小安全骨架，不自动扫描 item/block registry。
- 本轮没有迁移 HMF、Collada、JSON bus 动画，也没有处理 OBJ group 解析；这些仍按后续顺序单独推进。

### 第 12 轮

- 复核 1.7.10 的真实拆分边界后，决定保留现代端“统一 facade + 领域子库”的结构，不合并回单文件：
  - 旧 `ResourceManager` 本身是单文件，但内部通过注释按领域分段，例如 turrets、heaters、heat engines、oil pumps、refinery、tank、press、chemplant、RBMK、ISBRH 等。
  - 旧 `render` 包按渲染机制拆分明确：`anim`、`block`、`entity`、`icon`、`item`、`loader`、`model`、`shader`、`tileentity`、`util`、`world`。
  - 旧 `render/tileentity` 基本是平铺文件，只有 `door` 子包；旧 `render/item` 有 `block` 与 `weapon` 子包。
- 现代端边界策略：
  - `ObjModelLibrary` 保持 1.7.10 `ResourceManager` 风格的总入口与模型注册汇总。
  - 领域子库按旧 `ResourceManager` 的注释分段继续拆，例如 `ObjMachineModels`、`ObjLightModels`、后续可扩展 `ObjTurretModels`、`ObjNukeModels`、`ObjRbmkModels`、`ObjMissileModels`、`ObjIsbrhModels`。
  - 不把所有常量塞回 `ObjModelLibrary`，因为完整迁移时旧资源量超过千级常量，单文件会阻碍并行迁移和审查。
  - 也不把每台机器拆一个模型库类；只有当旧分段或现代复用边界足够清楚时再拆，避免过度碎片化。
- 结论：本轮第 11 轮拆分方向利于后续完整移植，保留。

### 第 13 轮

- 针对旧 `HFRWavefrontObject.renderPart(...)` / `renderOnly(...)` / `renderAllExcept(...)` 的 group 语义，新增现代承载类 `ObjPartModel`：
  - 实现 `LegacyObjModel`。
  - `part(String legacyName, ObjModelPart part, String... legacyAliases)` 用旧 OBJ group 名登记现代拆分后的 `ObjModelPart`。
  - `renderPart(...)` 支持通过旧 group 名或 alias 查找现代 part。
  - `renderOnly(...)` 会去重，避免同一 part 因多个 alias 被重复渲染。
  - `renderAllExcept(...)` 可通过 alias 排除 canonical part。
- `ObjMachineModels.PRESS` 已从 `ObjAnimatedModel` 改为 `ObjPartModel`，作为“旧 group/part 名 -> 现代拆分模型”样例。
- 本轮决策：
  - 先标准化 split model / split JSON 路线，不贸然写完整自定义 OBJ renderer。
  - 原因是当前现代资源已经大量采用“每个动态 part 一个 JSON/OBJ”的方式，且 Forge baked model 路线能继续享受资源重载、RenderType、texture atlas 和模型缓存。
  - 后续若遇到必须从单个旧 OBJ 动态读取 group 的机器，再补 `HFRWavefrontObject` 风格解析器；该解析器应接在 `ObjPartModel` 之下，而不是替代现有 API。
- `ObjAnimatedModel` 暂不删除，后续保留给真正需要时间轴/关键帧/骨架语义的模型；普通旧 OBJ group 映射优先使用 `ObjPartModel`。

### 第 14 轮

- 继续对齐旧 `HFRWavefrontObject` / `HFRWavefrontObjectVBO` 的 group 顺序语义：
  - 旧 `renderOnly(String... groupNames)` 是外层遍历模型内部 `groupObjects` / VBO `groups`，内层匹配传入名称，因此实际渲染顺序以 OBJ 文件内 group 顺序为准，而不是调用参数顺序。
  - `ObjPartModel.renderOnly(...)` 与 `ObjAnimatedModel.renderOnly(...)` 已改为按登记顺序渲染匹配分件。
  - `ObjPartModel` 仍对 alias 做 canonical 去重，避免现代 alias 同指向一个拆分 JSON part 时重复绘制。
- 继续对齐旧 `getPartNames()` 契约：
  - 旧实现返回 OBJ/VBO 内原始 group 名。
  - `ObjPartModel` 与 `ObjAnimatedModel` 现在保留 `part(...)` 登记时的原始大小写名称，`getPartNames()` 不再返回小写化后的 key。
  - `ObjPartModel.getAliases()` 也改为返回登记时的原始 alias 名称，便于工程记录和后续迁移排查直接比对 1.7.10 group 名。
- 本轮仍未引入自定义 OBJ group 解析器；当前策略是把“旧 group 名/顺序/alias -> 现代拆分模型”的行为先稳定下来，再按机器逐个补模型分件。

### 第 15 轮

- 复核 1.7.10 `ClientProxy.registerItemRenderer()` 与 `IItemRendererProvider` 后，继续收束现代物品 renderer 桥：
  - 旧版入口先注册 `ItemRenderLibrary`，再扫描所有 TESR 中实现 `IItemRendererProvider` 的 renderer，并为其 `getItemsForRenderer()` 返回的 item 注册 `IItemRenderer`。
  - 旧版还会扫描 item registry 中直接实现 `IItemRendererProvider` 的 item。
  - 现代端无法原样依赖旧 `MinecraftForgeClient.registerItemRenderer(...)`，但应保留“统一桥接入口，不在每个 item 里散落匿名扩展”的迁移模式。
- `LegacyItemRendererBridge` 新增延迟 renderer supplier 入口：
  - `accept(Consumer<IClientItemExtensions>, Supplier<? extends BlockEntityWithoutLevelRenderer>)`
  - `extensions(Supplier<? extends BlockEntityWithoutLevelRenderer>)`
  - 旧的 direct renderer overload 继续保留。
- `TrinketBlockItem.initializeClient(...)` 改为通过 supplier 接入 `TrinketItemRenderer.INSTANCE`，后续机器 item renderer 可以复用该入口，避免 renderer 静态实例在声明处过早绑定。
- 本轮仍未做全局扫描注册；后续若要完全贴近旧 `IItemRendererProvider`，建议基于现代 `RegisterClientExtensionsEvent` 或明确的 renderer provider 表做集中注册，而不是在 common registry 内反射扫描 client-only renderer。

### 第 16 轮

- 按后续顺序建议，开始单独建立旧 JSON bus 动画子库入口，避免把武器/机器动画继续塞进 OBJ 模型层。
- 1.7.10 事实源：
  - `com.hbm.render.anim.AnimationLoader`：从 `models/weapons/animations/*.json` 读取 `offset`、`rotmode`、`anim` 三段数据，生成 `HashMap<String, BusAnimation>`。
  - `BusAnimation`：按 bus 名保存多个 `BusAnimationSequence`，总时长取最长 sequence。
  - `BusAnimationSequence`：9 个维度 `TX/TY/TZ/RX/RY/RZ/SX/SY/SZ`，输出 15 位 transform 数组。
  - `HbmAnimations`：客户端 hotbar 上保存 `Animation[9][8]`，renderer 通过 bus 名取当前手持物相关 transform，并按 `translate -> rotation order -> -offset -> scale` 应用。
- 现代端新增入口：
  - `client.anim.LegacyBusAnimation`
  - `client.anim.LegacyBusAnimationSequence`
  - `client.anim.LegacyBusAnimationKeyframe`
  - `client.anim.LegacyBusAnimationLoader`
  - `client.anim.LegacyBusAnimationTransforms`
  - `client.anim.LegacyHbmAnimations`
- 已迁入的契约：
  - JSON loader 支持旧 `offset`、`rotmode`、`anim/location/rotation_euler/scale` 结构。
  - transform 数组保持旧布局：`0..2` 平移、`3..5` 旋转、`6..8` 缩放、`9..11` offset、`12..14` rotation order。
  - `LegacyBusAnimationTransforms.apply(...)` 使用现代 `PoseStack` 复刻旧 `HbmAnimations.applyRelevantTransformation(...)` 的应用顺序。
  - `LegacyHbmAnimations.HOTBAR` 保留旧 `9 x 8` 并行动画槽结构，后续 `HbmAnimationPacket`/武器 renderer 可接入。
- 已知未完成：
  - `LegacyBusAnimationKeyframe` 在第 16 轮只完整实现旧 `LINEAR`、`CONSTANT` 相关保持逻辑与 `SIN_UP/SIN_DOWN/SIN_FULL` 时间曲线；`BEZIER` 与 Blender easing 系列字段已解析保留，但精确 easing 公式仍需从旧 `BusAnimationKeyframe` 继续移植。该项已在第 17 轮补齐。
  - 旧 `ClientConfig.GUN_ANIMATION_SPEED` 暂未迁入，当前 keyframe duration 按原值保存；后续配置库完成后再接现代配置。
  - 旧 `HbmAnimationPacket` 的触发、清理过期动画、`holdLastFrame` 生命周期还未接入事件/网络层。

### 第 17 轮

- 按用户要求继续补齐旧 `BusAnimationKeyframe` 的 easing 逻辑。
- 1.7.10 事实源：`com.hbm.render.anim.BusAnimationKeyframe`。
- 已迁入 `LegacyBusAnimationKeyframe` 的完整曲线行为：
  - `BEZIER`：旧版 cubic root 求解、非循环 fcurve handle 修正、cubic bezier 采样。
  - `BACK`：`EASE_IN` / `EASE_OUT` / `EASE_IN_OUT`。
  - `BOUNCE`：`EASE_IN` / `EASE_OUT` / `EASE_IN_OUT`。
  - `CIRC`、`CUBIC`、`EXPO`、`QUAD`、`QUART`、`QUINT`、`SINE`：三种 easing mode。
  - `ELASTIC`：in/out/in-out、默认 period、amplitude blend 与 overshoot 行为。
  - 旧 `SIN_UP` / `SIN_DOWN` / `SIN_FULL` 时间曲线继续保留。
- 当前剩余边界：
  - 旧构造器会用 `ClientConfig.GUN_ANIMATION_SPEED` 缩放 duration；现代配置库尚未对接，因此本轮保持 duration 原值，不在动画库内硬编码配置。
  - `HbmAnimationPacket`、hotbar 动画清理、`holdLastFrame` 生命周期仍待事件/网络层迁移。
- 验证：`.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 第 18 轮

- 继续补齐旧 JSON bus 动画生命周期入口。
- 1.7.10 事实源：
  - `com.hbm.main.ModEventHandlerClient` 客户端 tick 中遍历 `HbmAnimations.hotbar[9][8]`。
  - 对 `null` 动画跳过；对 `holdLastFrame` 动画保留；其余动画在 `Clock.get_ms() - startMillis > animation.getDuration()` 后清空。
  - `com.hbm.packet.toclient.HbmAnimationPacket` 后续会向当前 hotbar slot / rail 写入 `new HbmAnimations.Animation(...)`。
- 现代端已完成：
  - `LegacyHbmAnimations` 新增 `HOTBAR_SLOTS`、`PARALLEL_RAILS` 常量，保留旧 `9 x 8` 并行动画槽。
  - 新增 `tick()`，按旧 tick 清理规则清空过期、非 `holdLastFrame` 动画。
  - 新增 `startForSelectedSlot(...)`、`start(...)`、`clear(...)`，给后续现代 `HbmAnimationPacket`/武器 renderer 接入保留统一入口，避免直接散写 `HOTBAR` 数组。
  - `ClientForgeEvents.onClientTick(...)` 在客户端 tick END 阶段调用 `LegacyHbmAnimations.tick()`。
- 当前剩余边界：
  - 仍未迁移现代网络包触发，`HbmAnimationPacket` 语义要等网络库/武器迁移时接入。
  - 旧 `ClientConfig.GUN_ANIMATION_SPEED` duration 缩放仍未接现代配置库。
  - 具体武器 renderer 对 bus 名称的调用仍待 `render/item/weapon/sedna` 专题迁移。

### 第 19 轮

- 继续收束旧 `HFRWavefrontObject` / `HFRWavefrontObjectVBO` 的 group 顺序语义。
- 1.7.10 事实源：
  - `HFRWavefrontObject.renderOnly(...)`、`renderAllExcept(...)` 与 VBO 版本均以模型内部 group/VBO group 顺序为外层遍历顺序。
  - 调用参数只决定包含/排除，不改变实际绘制顺序。
  - `getPartNames()` 返回旧 OBJ 内登记的原始 group 名，后续迁移排查需要能直接对照旧 group 名。
- 现代端已完成：
  - `ObjPartModel` 新增 `legacyOrder(String...)`，允许现代 split JSON/OBJ 分件显式记录旧 group 顺序。
  - `renderAll(...)`、`renderOnly(...)`、`renderAllExcept(...)` 统一走 `orderedParts()`，优先按 `legacyOrder` 渲染已登记分件；未迁入的旧 group 会被安全跳过；未写入 `legacyOrder` 的现代分件会保留在登记顺序末尾。
  - 新增 `getLegacyOrder()` 与 `hasPart(...)`，方便后续迁移机器时核对旧 group/alias 是否已映射。
  - `ObjMachineModels.PRESS` 记录旧 press body/head 顺序为 `Body`、`Head`；当前 `Body` 由 blockstate/static model 承接，BER 只登记并渲染动态 `Head`。
- 当前剩余边界：
  - 仍未引入完整 OBJ group 几何解析器；本轮只让 split-model 路线能稳定表达旧 group 顺序。
  - 如果后续遇到必须从单个旧 OBJ 动态读取 group 的机器，仍需在 `ObjPartModel` 下方补自定义 OBJ group parser/renderer。

### 第 20 轮

- 为后续机器动态分件迁移补旧 OBJ group 诊断入口。
- 1.7.10 事实源：
  - `HFRWavefrontObject.loadObjModel(...)` 对每行先 `replaceAll("\\s+", " ").trim()`。
  - `g` / `o` 行通过旧正则 `[go] [\\w\\d.]+` 解析 group 名。
  - 若先遇到 `f` 面且还没有当前 group，会创建名为 `Default` 的 group。
  - `groupObjects` 顺序就是 OBJ 文件内 group 读取顺序，也是 `renderOnly(...)` / `renderAllExcept(...)` 的实际外层渲染顺序。
- 现代端已完成：
  - 新增 `LegacyObjGroupReader`，可从 `ResourceLocation` 或 `Reader` 读取旧 OBJ group 顺序。
  - 读取逻辑保留旧版空白归一、`g/o` 识别和无 group 面的 `Default` 行为；只做 group 诊断，不解析几何、不参与渲染。
  - `ObjPartModel` 新增 `legacyOrder(List<String>)`，后续可直接接 `LegacyObjGroupReader.readGroupOrder(...)` 的结果。
- 当前剩余边界：
  - 该工具依赖客户端资源管理器，适合 runtime/调试/后续客户端模型库初始化时使用；DataGen 或命令行资源扫描若需要，应另做文件系统版本。
  - 本轮仍不实现完整 `HFRWavefrontObject` 几何解析、UV 修复或 VBO reload。

### 第 21 轮

- 补齐旧 OBJ group 读取器的文件系统入口，供 DataGen、命令行扫描和 1.7.10 源码资源核对使用。
- 现代端已完成：
  - `LegacyObjGroupReader.readGroupOrder(Path)` 使用 `Files.newBufferedReader(...)` 读取任意 OBJ 文件路径。
  - 继续复用同一个 `Reader` 解析逻辑，避免客户端资源入口和文件系统入口出现不一致。
- 本轮事实核对：
  - 旧 `models/press_body.obj` group：`Cube_Cube.000`。
  - 旧 `models/press_head.obj` group：`Cube.001_Cube.002`。
  - 旧 `models/epress_body.obj` group：`Cube_Cube.001`。
  - 旧 `models/epress_head.obj` group：`Cube.001_Cube.002`。
- 迁移结论：
  - `ObjMachineModels.PRESS` 已改为使用旧 `press_head.obj` 的原始 group 名 `Cube.001_Cube.002` 作为 canonical part name，并保留现代语义名 `Head` 作为 alias。
  - `legacyOrder(...)` 已改为旧 group 顺序 `Cube_Cube.000`、`Cube.001_Cube.002`；其中 body 当前仍由 blockstate/static model 承接，未登记为 BER 动态 part，会被安全跳过。
  - 后续新增机器动态分件时，应优先把旧 OBJ 原始 group 名作为 canonical name，把现代语义名作为 alias，便于 `getPartNames()` 直接和 1.7.10 group 清单对照。
- 当前剩余边界：
  - 该 reader 仍只读取 group 名，不检查 UV、面类型、材质或可 bake 性。
  - 旧 Forge OBJ loader 与 HBM 自定义 OBJ 解析器对无 UV 面容忍度不同，UV/材质诊断仍按后续独立工具推进。

### 第 22 轮

- 开始补 OBJ 诊断层，用于判断旧 OBJ 更适合走 Forge baked split model、几何修复，还是后续自定义 `HFRWavefrontObject` 风格解析器。
- 1.7.10 事实源：
  - `HFRWavefrontObject` 支持 `f v/vt/vn`、`f v/vt`、`f v//vn`、`f v` 四类面。
  - 非 `mixedMode` 下单个 group 不能混用 triangle 与 quad；`mixedMode()` 只允许 ISBRH 等手动访问顶点，不支持直接 `renderAll()`。
  - `parseTextureCoordinate(...)` 会把 V 轴翻转为 `1 - v`，现代 baked OBJ 路线不等同于直接搬旧 parser。
- 现代端已完成：
  - 新增 `LegacyObjDiagnostics`，可从 `Path` 或 `Reader` 统计 group 顺序、`mtllib`、`usemtl`、顶点/UV/法线数量、face 数量、三角/四边/其他面数量、无 UV 面、无法线面。
  - `Summary` 提供 `hasMixedTriangleQuadFaces()`、`hasFacesWithoutTextureCoordinates()`、`hasFacesWithoutNormals()`，供后续批量扫描和迁移决策使用。
  - `LegacyObjGroupReader` 改为复用 `LegacyObjDiagnostics.parseLegacyGroupName(...)`，避免 group 识别规则分叉。
- 本轮抽样核对：
  - `trinkets/lantern.obj`：groups `Light,Lantern`，164 个三角面，其中 8 个 face 无 UV；这解释了静态 bake 需要跳过/拆出 `Light` 分件。
  - `blocks/demon_lamp.obj`：group `Sphere`，552 个三角面，无缺 UV。
  - `press_head.obj`：group `Cube.001_Cube.002`，28 个三角面，无缺 UV。
  - `epress_head.obj`：group `Cube.001_Cube.002`，44 个三角面，无缺 UV。
  - `machines/ammo_press.obj`：groups `Press,Shells,Bullets,Frame`，890 个三角面，无缺 UV。
  - `rbmk/rbmk_rods.obj`：groups `Lid,Column`，96 个三角面，无缺 UV。
- 当前剩余边界：
  - `LegacyObjDiagnostics` 只统计文本结构，不验证 index 是否越界，也不读取 `.mtl` 文件内容。
  - 后续可基于该类补批量扫描命令或 DataGen 检查，优先找出所有 `facesWithoutTextureCoordinates > 0` 的旧 OBJ。

### 第 23 轮

- 继续增强 OBJ 诊断层，补齐材质文件与索引有效性检查。
- 现代端已完成：
  - `LegacyObjDiagnostics.inspect(Path)` 会基于 OBJ 所在目录检查 `mtllib` 指向的 `.mtl` 文件是否存在。
  - 会读取 `.mtl` 内的 `newmtl` 名称，并检查 OBJ `usemtl` 是否能匹配到已声明材质。
  - face 解析现在会检查顶点、UV、法线索引是否为正数且不超过当前已读取的 `v` / `vt` / `vn` 数量。
  - `Summary` 新增 `missingMaterialLibraries`、`declaredMaterials`、`undefinedMaterialUses`、`facesWithInvalidVertexIndices`、`facesWithInvalidTextureCoordinateIndices`、`facesWithInvalidNormalIndices`，并提供 `hasInvalidIndices()`、`hasMissingMaterialLibraries()`、`hasUndefinedMaterialUses()`。
- 迁移用途：
  - 旧资源批量扫描时，可以先筛出 `hasFacesWithoutTextureCoordinates()`、`hasMissingMaterialLibraries()`、`hasUndefinedMaterialUses()`、`hasInvalidIndices()` 为真的 OBJ。
  - 对这些 OBJ 优先选择拆分/修复 bake 资源，或暂时留给后续自定义 OBJ renderer；避免把不兼容资源直接挂进 Forge OBJ loader。
- 当前剩余边界：
  - `.mtl` 目前只读取 `newmtl`，还不验证 `map_Kd` 是否存在、是否为 `#default`、贴图文件是否存在。
  - 还没有 CLI/Gradle/DataGen 批量报告入口；当前诊断类是后续扫描工具的底层 API。

### 第 24 轮

- 继续推进 `.mtl` 诊断，补齐材质漫反射贴图声明读取。
- 本轮事实核对：
  - 旧资源 OBJ 大量保留 `mtllib` / `usemtl` 声明，例如炸弹、机器、灯具、武器模型。
  - 当前旧源码资源树未发现实际 `.mtl` 文件，因此批量扫描时会先体现为 `missingMaterialLibraries`，不能靠猜测路径补材质。
- 现代端已完成：
  - `LegacyObjDiagnostics` 会读取内联或外部 `.mtl` 中的 `map_Kd`，按 `material -> texture` 记录为 `MaterialTexture`。
  - `Summary` 新增 `diffuseTextureMaps`、`materialsWithoutDiffuseTextureMaps`，并提供 `hasMaterialsWithoutDiffuseTextureMaps()`。
  - `inspect(Path)` 若找到 `.mtl` 文件，会同时合并 `newmtl` 与 `map_Kd`；若缺文件，继续保持 `missingMaterialLibraries` 诊断。
- 迁移用途：
  - 后续把旧 OBJ 改成 Forge OBJ JSON 或自定义 renderer 前，可以区分“OBJ 引用了不存在的 `.mtl`”和“`.mtl` 存在但材质没有贴图声明”。
  - 对缺 `.mtl` 的资源，优先回到 1.7.10 `ResourceManager` / renderer 贴图绑定路径核对，而不是直接信任 OBJ 的材质文件。
- 当前剩余边界：
  - 目前只读取 `map_Kd` 原始字符串，不解析相对贴图路径、不检查 png 是否存在，也不解释 `#default`。
  - 全量 `compileJava` 被并行流体库未跟踪文件 `HbmFluidNode` / `HbmFluidNet` 状态阻塞；本轮渲染库类已用单独 `javac` 验证通过。

### 第 25 轮

- 将单文件 OBJ 诊断推进为目录级批量扫描 API，并把面类型风险改为按 group 判断。
- 1.7.10 事实源：
  - `HFRWavefrontObject` 的 triangle / quad 限制发生在 `currentGroupObject.glDrawingMode`，不是整份 OBJ 文件全局限制。
  - 因此现代诊断的 `hasMixedTriangleQuadFaces()` 应表示“至少一个 group 内同时出现 triangle 和 quad”，而不是“文件中既有 triangle 又有 quad”。
- 现代端已完成：
  - `LegacyObjDiagnostics` 新增 `GroupSummary`，逐 group 统计 face、triangle、quad、其他面、缺 UV、缺 normal、非法索引。
  - `Summary.hasMixedTriangleQuadFaces()` 改为基于 group 判断，并新增 `hasOtherFaceShapes()`。
  - 新增 `LegacyObjScan`，可递归扫描目录下所有 `.obj`，输出 `ScanReport` / `ModelReport`，并按 `Compatibility` 分类：
    - `BAKE_CANDIDATE`
    - `NEEDS_MATERIAL_REVIEW`
    - `NEEDS_RENDERER_REVIEW`
    - `REQUIRES_SPLIT_OR_CUSTOM_RENDERER`
    - `REQUIRES_REPAIR`
  - `ScanReport` 提供按分类、缺 `.mtl`、缺 UV、混三角/四边 group 的筛选入口。
- 本轮批量扫描旧资源：
  - 扫描根：`assets/hbm/models`
  - OBJ 总数：493
  - `BAKE_CANDIDATE`：437
  - `NEEDS_MATERIAL_REVIEW`：33
  - `NEEDS_RENDERER_REVIEW`：21
  - `REQUIRES_SPLIT_OR_CUSTOM_RENDERER`：1，代表：`rbmk/rbmk_element.obj`
  - `REQUIRES_REPAIR`：1，代表：`weapons/.obj`
  - 缺 `.mtl`：33；有缺 UV face 的 OBJ：21；存在混合三角/四边 group 的 OBJ：1。
- 迁移用途：
  - 大多数旧 OBJ 可以优先走 Forge baked model / 静态 JSON 路线。
  - `NEEDS_MATERIAL_REVIEW` 应回查旧 renderer 绑定贴图路径，不直接依赖 OBJ `mtllib`。
  - `NEEDS_RENDERER_REVIEW` 包含旧 parser 可容忍但现代 bake 风险高的无 UV 面，优先拆 group 或暂留自定义 renderer。
  - `REQUIRES_SPLIT_OR_CUSTOM_RENDERER` 和 `REQUIRES_REPAIR` 是后续高风险清单入口。
- 当前剩余边界：
  - `LegacyObjScan` 仍是库 API，没有 CLI/Gradle/DataGen 报告文件输出。
  - 分类规则偏保守，后续接入 Forge OBJ loader 实测后可调整。

### 第 26 轮

- 将 OBJ 批量扫描推进到可落盘报告，方便后续按清单推进模型迁移。
- 现代端已完成：
  - `LegacyObjScan.ScanReport` 新增 `countsByCompatibility()`、`toMarkdown()`、`toCsv()`、`writeMarkdown(...)`、`writeCsv(...)`。
  - Markdown 报告包含总数、分类计数、每个 OBJ 的 group 数、face 数和问题摘要。
  - CSV 报告包含 path、compatibility、group/face/triangle/quad/other face 统计、缺材质库、缺 UV/normal、非法索引、混合 group 数，便于表格筛选。
  - 新增 `LegacyObjScanCli`，命令行参数为 `<modelRoot> [markdownOut] [csvOut]`；输出分类计数，报告路径可选。
- 本轮生成报告：
  - Markdown：`工程记录/库移植/生成报告/legacy-obj-scan-2026-05-21.md`
  - CSV：`工程记录/库移植/生成报告/legacy-obj-scan-2026-05-21.csv`
  - CSV 行数：494，即 1 行表头 + 493 个 OBJ。
  - 代表性高风险项：
    - `machines/chemical_factory.obj`：`NEEDS_RENDERER_REVIEW`，8444 faces，其中 4422 faces 无 UV。
    - `rbmk/rbmk_element.obj`：`REQUIRES_SPLIT_OR_CUSTOM_RENDERER`，1 个 group 混合 triangle/quad。
    - `weapons/.obj`：`REQUIRES_REPAIR`，存在非法 vertex/UV/normal 索引。
- 迁移用途：
  - 后续机器/灯具/武器模型迁移可先按 CSV 过滤 `BAKE_CANDIDATE` 批量推进。
  - 对 `NEEDS_RENDERER_REVIEW` 可先打开 group 明细，决定拆 OBJ、补 UV 还是暂走自定义 renderer。
  - 对 `NEEDS_MATERIAL_REVIEW` 继续回查旧 renderer 的显式 `bindTexture(...)`，避免依赖缺失的 `.mtl`。
- 当前剩余边界：
  - CLI 目前是普通 Java main，还未接入 Gradle task。
  - 报告仍不解析贴图实际 png 路径，也不自动生成 Forge OBJ JSON。

### 第 27 轮

- 基于 OBJ 扫描报告，批量接线已注册核装置占位方块的 Forge OBJ block model。
- 选择依据：
  - 现代端已有 `nuke_gadget`、`nuke_boy`、`nuke_man`、`nuke_tsar`、`nuke_mike`、`nuke_prototype`、`nuke_fleija`、`nuke_solinium`、`nuke_n2`、`nuke_fstbmb`、`bomb_multi` 注册、blockstate 与 item model。
  - 现代端已存在 `models/block/nuke/*.obj`、对应 `.mtl` 与 `textures/block/nuke/*.png`。
  - 这些模型不需要新增爆炸逻辑或方块实体逻辑，本轮只补资源模型连接。
- 现代端已完成：
  - 新增 `models/block/nuke_gadget.json`
  - 新增 `models/block/nuke_boy.json`
  - 新增 `models/block/nuke_man.json`
  - 新增 `models/block/nuke_tsar.json`
  - 新增 `models/block/nuke_mike.json`
  - 新增 `models/block/nuke_prototype.json`
  - 新增 `models/block/nuke_fleija.json`
  - 新增 `models/block/nuke_solinium.json`
  - 新增 `models/block/nuke_n2.json`
  - 新增 `models/block/nuke_fstbmb.json`
  - 新增 `models/block/bomb_multi.json`
  - 每个 JSON 使用 `forge:obj`、`flip_v: true`、`minecraft:cutout`，并把 `particle/default/texture0` 指向 `hbm:block/nuke/<name>`。
- 迁移用途：
  - `HbmBlockStateProvider.existingModelWithItem(...)` 已经指向 `hbm:block/<id>`，因此这些占位方块现在可以直接使用旧 OBJ 外观。
  - 该批次只解决模型显示，不迁移 1.7.10 的 `TileEntityNuke*`、装填、点火、爆炸半径、成就或 starter kit 行为。
- 当前剩余边界：
  - display 缩放为保守手调值，需后续进游戏截图确认物品栏和手持比例。
  - 尚未处理 dud 系列、地雷、UFP 等其他炸弹 OBJ，也未处理 `NEEDS_MATERIAL_REVIEW` 清单。

### 第 28 轮

- 基于 OBJ 扫描报告，批量迁移一组低风险机器 OBJ 到现代资源库，供后续机器注册、BER 或 block model 直接复用。
- 选择依据：
  - 均来自 `legacy-obj-scan-2026-05-21.csv` 中 `BAKE_CANDIDATE` 的 `machines/*` 条目。
  - 旧资源存在匹配贴图，且 OBJ 文件本身没有 `mtllib` 依赖；现代 JSON 直接提供 `default/texture0/particle` 贴图。
  - 本轮不新增机器注册、不改方块行为，只迁移模型资源和 Java 模型库入口。
- 现代端已完成：
  - 复制旧 OBJ 到 `models/block/machines/`：
    - `radar_screen`、`solar_mirror`、`drain`、`intake`、`combination_oven`、`rtg`、`telex`、`fraction_tower`、`fan`、`furnace_iron`、`elevator`、`crucible`、`drum`、`fraction_spacer`、`heating_oven`、`chimney_brick`、`turbine`、`dieselgen`
  - 复制对应贴图到 `textures/block/machines/`；`crucible` 使用旧 `crucible_heat.png` 作为默认贴图，`drum` 对齐旧 `ResourceManager.waste_drum_tex` 使用 `drum_gray.png`。
  - 为上述 18 个机器模型新增 Forge OBJ JSON，统一使用 `forge:obj`、`flip_v: true`、`minecraft:cutout`。
  - `ObjMachineModels` 新增 18 个 `ObjModelPart` 常量，统一以 `ObjPartTransform.BLOCK_CENTER` 为原点。
  - `ObjModelLibrary` 暴露对应 `MACHINE_*` 常量，供后续 renderer/BER 直接引用。
- 迁移用途：
  - 后续补机器注册、机器 BER 或动态分件时，可直接接 `ObjModelLibrary.MACHINE_*` 或 `hbm:block/machines/<name>`。
  - 该批资源可作为后续“低风险机器模型批量接线”的样板。
- 当前剩余边界：
  - 没有为这些机器创建 blockstate/item model，因为相应现代方块注册和行为尚未成批迁移。
  - 未迁移 `uv_lamp`，因为旧贴图未按简单同名规则确认；未迁移 `radar`，因为贴图由 base/dish/screen 多贴图组成，需单独核对 group 与材质。

### 第 29 轮

- 本轮进行渲染库对齐审计，不新增移植范围，只检查现代端已推进内容是否有 1.7.10 来源、是否被过度标记为“已完整迁移”。
- 1.7.10 事实源：
  - 核装置模型来源于 `ResourceManager` 的 `bomb_gadget`、`bomb_boy`、`bomb_man`、`bomb_mike`、`bomb_tsar`、`bomb_prototype`、`bomb_fleija`、`bomb_solinium`、`n2`、`fstbmb`、`bomb_multi`。
  - `nuke_n2` 和 `nuke_fstbmb` 在旧 `ResourceManager` 中不是 `bomb_n2` / `bomb_fstbmb` 命名，而是 `n2` / `fstbmb`；现代资源名按注册 ID 归一，属于命名适配。
  - 机器模型入口均能在旧 `ResourceManager` 找到对应模型或贴图字段：`radar_screen`、`solar_mirror`、`drain`、`intake`、`combination_oven`、`rtg`、`telex`、`fraction_tower`、`fan`、`furnace_iron`、`cargo_elevator`、`crucible_heat`、`waste_drum`、`fraction_spacer`、`heater_oven`、`chimney_brick`、`turbine`、`dieselgen`。
  - `LegacyObjScan` / `LegacyObjScanCli` / 生成报告是现代迁移辅助工具，不是 1.7.10 内容移植；保留理由是复刻旧 OBJ parser 兼容边界并服务资源核查。
- 审计发现：
  - 未发现当前渲染库入口中有完全没有 1.7.10 来源的“内容模型”。现代 Forge OBJ JSON、`.mtl` 与 Java facade 是适配层，不按旧版内容项计。
  - 现代端 `models/block/nuke/*.obj` 不是纯哈希复制：抽样 `gadget.obj` 已相对旧 OBJ 做坐标平移并补 `mtllib`，贴图哈希仍与旧资源一致。因此该批应记录为“旧模型资源经现代 block model 坐标适配”，不能记录为原样复制。
  - `nuke_gadget` 的旧 renderer 只固定渲染 `Body`，并在 fancy graphics 下渲染 `Wires`；现代单个 block model 会总是显示完整 OBJ。这是行为未完全对齐，后续如要严格复刻需拆分 `Body` / `Wires` 或改 BER。
  - `nuke_fstbmb` 的旧 renderer 渲染 `Body`、`Balefire`，并对 `Balefire` 调 `RenderMiscEffects.renderClassicGlint(...)`；现代静态 OBJ 只能显示基础模型，缺少经典 glint 效果。
  - `nuke_boy` 旧 block renderer 有朝向后平移；其他 nuke item renderer 也有独立缩放/平移。现代 JSON display 目前是适配值，应标记为“视觉待截图校准”。
  - `radar_screen` 旧 renderer 在模型后叠加 GUI 雷达屏幕与动态光束；现代 `MACHINE_RADAR_SCREEN` 只是基础 OBJ。
  - `fan`、`furnace_iron`、`rtg`、`crucible`、`elevator`、`dieselgen` 均有旧 part/state/动画逻辑：风扇叶片旋转、铁炉 On/Off 全亮层、RTG 多贴图与四向 connector、坩埚 lava/材料面、货运升降机高度与平台伸缩、柴油机 Engine 摆动。
  - `drum` 初次暂存时错误使用旧 `textures/models/machines/drum.png`；旧 `RenderStorageDrum` 与 `ItemRenderLibrary` 实际绑定 `ResourceManager.waste_drum_tex = drum_gray.png`。本轮已将现代 `models/block/machines/drum.json` 改为 `hbm:block/machines/drum_gray`，并补入旧 `drum_gray.png`。
- 状态重分类：
  - “已对齐，可作为静态资源入口”：贴图/模型来源明确，且旧 renderer 仅 `renderAll()` 或无现代行为消费者的资源入口；例如 `telex`、`fraction_spacer`、`drum` 基础模型。
  - “资源来源对齐，但行为未完整迁移”：核装置 block model 批次、`radar_screen`、`fan`、`furnace_iron`、`rtg`、`crucible`、`elevator`、`dieselgen`、`solar_mirror`。
  - “现代辅助，不计入内容迁移”：OBJ 诊断、扫描 CLI、CSV/Markdown 报告。
- 下一步计划：
  - 先建立一张渲染对齐清单，字段固定为 `modern id`、`legacy ResourceManager field`、`legacy model`、`legacy texture`、`legacy renderer`、`现代状态`、`必须复刻的 part/特效/动画`，以后每批迁移前先填清单再接资源。
  - 收敛第 27 轮核装置：把 `nuke_gadget`、`nuke_fstbmb` 标为优先纠偏对象，分别处理 `Body/Wires` 条件显示和 `Balefire` glint；其他核装置先做 display/方位截图校准。
  - 收敛第 28 轮机器资源：不要继续把动态机器简单接成 blockstate；优先挑一个小而完整的旧 renderer 迁移为现代 BER，建议顺序为 `telex` 或 `fraction_spacer` 静态样板，随后 `fan`、`furnace_iron`、`rtg` 这类动态 part。
  - 对 `ObjModelLibrary.MACHINE_*` 常量补注释或配套清单状态，避免后续误以为这些入口已经等于完整旧 TESR 行为。
  - 继续扩展 OBJ 扫描工具时只做“验证/报告”能力，不把报告项自动视为可迁移内容。

### 第 30 轮

- 按第 29 轮审计结论继续推进，不扩大新资源范围，先把“对齐清单”和一个明确偏差项落地。
- 新增对齐清单：
  - `工程记录/库移植/生成报告/render-library-alignment-2026-05-21.csv`
  - 字段固定为 `modern_id`、`legacy_resource_field`、`legacy_model`、`legacy_texture`、`legacy_renderer`、`modern_status`、`required_legacy_parts_effects_animation`。
  - 首批覆盖第 27 轮核装置和第 28 轮机器资源，后续每推进一个渲染项必须先在这张表补足旧 `ResourceManager` 字段、旧 renderer 和缺失行为。
- `nuke_gadget` 静态纠偏：
  - 1.7.10 `RenderNukeGadget` 固定渲染 `ResourceManager.bomb_gadget.renderPart("Body")`，只有 `Minecraft.gameSettings.fancyGraphics` 为真时才渲染 `Wires`。
  - 现代端原 `nuke_gadget.json` 指向完整 `gadget.obj`，会永久显示 `Wires`，与旧默认行为不对齐。
  - 本轮新增派生 OBJ：`models/block/nuke/gadget_body.obj`。
    - 来源为现代已适配坐标的 `models/block/nuke/gadget.obj`。
    - 保留全量 `v` / `vt` / `vn` 表，保留 `mtllib gadget.mtl`，仅保留 `o Body` 后的 `f` 面。
    - 不手工重编号索引，降低破坏 UV/normal 的风险。
  - `models/block/nuke_gadget.json` 改为指向 `hbm:models/block/nuke/gadget_body.obj`。
- 当前状态：
  - `nuke_gadget` 静态 block model 已对齐旧版非 fancy graphics 默认显示。
  - `Wires` 条件显示仍未迁移；后续若要严格复刻旧 TESR，需要为 `nuke_gadget` 建 BER 或找到可靠的 Forge OBJ group 过滤方案，在 fancy graphics 下叠加渲染 `Wires`。
  - 其他核装置仍按第 29 轮状态保留为“资源来源对齐，display/方位待校准”。

### 第 31 轮

- 继续推进渲染库本体，不新增服务端机器逻辑，先批量迁入低耦合 OBJ 资源入口。
- 新增领域模型库：
  - `ObjNetworkModels`：`connector`、`connector_super`、`fluid_diode`、`pipe_anchor`、`pylon_large`、`pylon_medium`、`substation`。
  - `ObjDoorModels`：`silo_hatch`、`silo_hatch_large`、`blast_door_base`、`blast_door_tooth`、`blast_door_slider`、`blast_door_block`。
- 扩展 `ObjMachineModels` 与 `ObjModelLibrary` facade：
  - heaters/boilers：`firebox`、`oilburner`、`electric_heater`、`heatex`、`boiler`、`boiler_burst`、`industrial_boiler`。
  - oil/utility：`derrick`、`pumpjack`、`fracking_tower`、`flare_stack`、`chimney_industrial`。
  - 动态 part 样例：`hephaestus`、`fensu2`、`fensu`。
- 资源迁入与对齐：
  - 旧 OBJ 从 `assets/hbm/models/...` 复制到现代 `assets/hbm/models/block/...`。
  - 旧贴图从 `textures/models/...` 复制到现代 `textures/block/...`。
  - 为 network、doors、上述 machines 补 Forge OBJ model JSON 和本地 `.mtl`。
  - `boiler_burst` 旧 `RenderBoiler` 绑定的是 `ResourceManager.boiler_tex`，现代 `boiler_burst.json` 同样复用 `textures/block/machines/boiler.png`，不创建 1.7.10 不存在的 `boiler_burst.png`。
  - `heater_heatex` 旧贴图常量是 `textures/models/machines/heater_heatex.png`，现代 `heatex.json` 绑定 `heater_heatex`，不误用另一个 `heatex.png`。
- 旧 `renderPart(...)` 语义处理：
  - `firebox` 拆出 `InnerEmpty`、`InnerBurning`、`Door`、`Main` 四个 visibility JSON。
  - `hephaestus` 拆出 `Rotor`、`Core`、`Main` 三个 visibility JSON。
  - `fensu2` 拆出 `Wheel`、`Lights`、`Plasma`、`Base` 四个 visibility JSON。
  - `silo_hatch` / `silo_hatch_large` 拆出 `Hatch`、`Frame` visibility JSON。
  - 避免把旧 group 名全部映射到整模，防止后续 renderer 调 `renderPart("Main")` 时错误绘制整台模型。
- 本轮没有迁移：
  - `RenderConnector`、`RenderPipeAnchor`、`RenderPylon*`、`RenderSubstation` 的动态方向、线缆和变体贴图。
  - `DoorDecl` / `RenderDoorGeneric` 的门动画、Collada seal door、silo hatch 实际开合。
  - `RenderFirebox`、`RenderHephaestus`、`RenderBatteryREDD` 的状态驱动动画、发光分件、旋转/缩放。
  - 炮塔模型库；旧炮塔 renderer 多 part 多贴图，不能用单贴图 Forge OBJ JSON 草率替代。
- 验证：`.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

### 第 32 轮

- 继续推进渲染库资源/入口层，本轮聚焦 1.7.10 `fusion` 模型组，不迁服务端 ITER/融合堆机器逻辑。
- 1.7.10 事实源：
  - `ResourceManager` 字段：`fusion_torus`、`fusion_klystron`、`fusion_breeder`、`fusion_collector`、`fusion_boiler`、`fusion_mhdt`、`fusion_coupler`、`fusion_plasma_forge`。
  - 旧模型路径：`models/fusion/torus.obj`、`klystron.obj`、`breeder.obj`、`collector.obj`、`boiler.obj`、`mhdt.obj`、`coupler.obj`、`plasma_forge.obj`。
  - 旧贴图路径：`textures/models/fusion/torus.png`、`plasma.png`、`plasma_glow.png`、`plasma_sparkle.png`、`klystron.png`、`klystron_creative.png`、`breeder.png`、`collector.png`、`boiler.png`、`mhdt.png`、`coupler.png`、`plasma_forge.png`。
  - 旧 renderer：`RenderFusionTorus`、`RenderFusionKlystron`、`RenderFusionKlystronCreative`、`RenderFusionBreeder`、`RenderFusionCollector`、`RenderFusionBoiler`、`RenderFusionMHDT`、`RenderFusionCoupler`、`RenderFusionPlasmaForge`。
- 新增 `ObjFusionModels`：
  - `TORUS_PARTS` 保留 `Plasma`、`Bolts4`、`Bolts3`、`Bolts2`、`Bolts1`、`Magnet`、`Torus` group 顺序，并额外提供 `plasma_glow`、`plasma_sparkle` 两个旧贴图入口。
  - `KLYSTRON_PARTS` / `KLYSTRON_CREATIVE_PARTS` 保留 `Pipes`、`Rotor`、`Klystron` group；creative 变体复用旧 OBJ、绑定旧 `klystron_creative.png`。
  - `BREEDER_PARTS` 保留 `BreederAlt`、`Breeder`；`MHDT_PARTS` 保留 `Coils`、`Turbine`。
  - `PLASMA_FORGE_PARTS` 保留旧 `RenderFusionPlasmaForge` 使用的 `Body`、`Plasma`、`SliderStriker`、`ArmLowerStriker`、`ArmUpperStriker`、`StrikerMount`、`StrikerLeft/Right`、`PistonLeft/Right`、`SliderJet`、`ArmLowerJet`、`ArmUpperJet`、`Jet`。
- 扩展 `ObjModelLibrary` facade：
  - 暴露 `FUSION_TORUS`、`FUSION_KLYSTRON`、`FUSION_KLYSTRON_CREATIVE`、`FUSION_BREEDER`、`FUSION_COLLECTOR`、`FUSION_BOILER`、`FUSION_MHDT`、`FUSION_COUPLER`、`FUSION_PLASMA_FORGE`。
  - 新增 `fusionPart(String name)` 作为 `block/fusion/` 路径入口。
- 资源迁入：
  - 复制 8 个旧 OBJ 到 `models/block/fusion/`，按现代资源名加 `fusion_` 前缀，避免和已存在 `machines/boiler` 等普通机器资源冲突。
  - 复制 12 张旧 fusion 贴图到 `textures/block/fusion/`。
  - 为 8 个 OBJ 补本地 `.mtl`，并在 OBJ 头部补 `mtllib` / `usemtl default`，纹理仍由 Forge OBJ JSON 提供。
  - 新增全模 JSON 与 visibility 分件 JSON，分件 JSON 明确列出同 OBJ 的所有旧 group，只有目标 group 为 `true`，避免未列 group 的默认显示行为造成误判。
- 本轮没有迁移：
  - `RenderFusionTorus` 的 `tilted` 位移/旋转、`Magnet` 插值旋转、四向 `connections[]` 螺栓显示、plasma 颜色/alpha/贴图滚动/additive glow/sparkle。
  - `RenderFusionKlystron` / creative 的朝向、位移、`Rotor` 插值旋转。
  - `RenderFusionMHDT` 的 turbine 旋转。
  - `RenderFusionPlasmaForge` 的连接螺栓、双机械臂动画、黑色 inactive plasma、plasma glow 层、物品预览、beam、jet 手写特效。
- 对齐清单：
  - `工程记录/库移植/生成报告/render-library-alignment-2026-05-21.csv` 追加本轮 9 个 fusion 条目，状态均标为资源/分件入口已对齐但 renderer 行为未完整迁移，静态候选只限 `collector`、`boiler`、`coupler`。

## 旧版 renderer 分类

- 方块实体 renderer：`src/main/java/com/hbm/render/tileentity`
  - 机器、核设施、灯具、trinket、炮塔、导弹发射台等。
  - 重点记录：绑定 TileEntity、ResourceManager 模型字段、贴图路径、metadata/facing 映射、GL transform 顺序、动画字段、特殊光照、混合/透明/禁用贴图行为。
  - `IItemRendererProvider` 实现者必须同时记录方块实体渲染和物品渲染契约。
  - `ITileActorRenderer` 实现者必须记录 actor/action 状态来源。
- ISBRH renderer：`src/main/java/com/hbm/render/block`
  - 对应现代 baked model / BER / 动态 blockstate / OBJ JSON。
  - `render/block/ct` 是 connected texture 子体系，需要单独迁移。
- 物品 renderer：`src/main/java/com/hbm/render/item`
  - 对应现代 `BlockEntityWithoutLevelRenderer` 或 item model override。
  - `render/item/weapon/sedna` 是枪械手持动画重点区域，需要与 `render/anim`、`HbmAnimations`、`HbmAnimationPacket` 一起迁移。
- 实体 renderer：`src/main/java/com/hbm/render/entity`
  - 对应现代 `EntityRendererProvider`。
- 手写模型：`src/main/java/com/hbm/render/model`
  - `ModelBase` 系列模型，不走 OBJ/Forge baked model。
- 粒子：
  - `src/main/java/com/hbm/particle`
  - `src/main/java/com/hbm/particle/psys`
  - 对应现代 `ParticleType`、`ParticleProvider`、自定义 world render 或 shader path。
- shader / skybox / icon：
  - `src/main/java/com/hbm/render/shader`
  - `src/main/java/com/hbm/render/world`
  - `src/main/java/com/hbm/render/icon`
- HUD / overlay：
  - `src/main/java/com/hbm/render/util/RenderScreenOverlay.java`
  - `src/main/java/com/hbm/render/util/RenderOverhead.java`
  - `src/main/java/com/hbm/render/util/RenderMiscEffects.java`
  - `src/main/java/com/hbm/render/util/GaugeUtil.java`

## 后续顺序建议

1. 处理 `HFRWavefrontObject.renderPart(...)` / `tessellatePart(...)` / VBO reload 的 group 语义缺口，决定是拆 JSON 还是做自定义 OBJ group 解析。
2. 单独建立 HMF、Collada DAE、JSON bus 动画三个子库入口，避免把所有动画塞进 OBJ 库。
3. 迁移 `RenderEPress` / `RenderPress` 的完整 body/head 逻辑。
4. 再推进流体罐、油处理机器、炉类机器的动态状态渲染。
5. 后续专题展开 CT 贴图、粒子、shader/skybox、HUD overlay。

## 验证

- 构建命令：`./gradlew.bat compileJava processResources --no-daemon`
- 已通过多轮验证。
- 未来每轮继续保持“少量改动 -> 编译验证 -> 文档补充”的节奏。
## 2026-05-21 ObjPartModel 编译修正

辐射库批次验证时发现 `ObjPartModel.renderOnlyInCallOrder(...)` 引用了未定义的 `rendered` 集合，导致 `compileJava` 失败。

已完成：

- 在 `renderOnlyInCallOrder(...)` 内补局部 `LinkedHashSet<String>`，保持调用顺序渲染时仍能去重。

验证：

- 2026-05-21 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 Radiation Fog Particle Bridge

触发来源：

- 辐射库复核发现 1.7.10 黄雾不是 vanilla 粒子，而是 `com.hbm.particle.ParticleRadiationFog`。
- 旧粒子由 `ClientProxy#effectNT` 中的 `type == "radFog"` 创建。

旧版契约：

- 纹理：`assets/hbm/textures/particle/fog.png`
- 颜色：`0.85F, 0.9F, 0.5F`
- 生命周期：源码会把 `maxAge` 至少提升到 `400`
- alpha：`sin(age * PI / 400F) * 0.125F`
- scale：默认 `7.5F`
- render layer：旧 `getFXLayer() == 3`，使用半透明 blend。
- 旧 renderer 每个粒子绘制 25 个随机偏移 quad。

本轮现代桥接：

- 新增 `ModParticleTypes.RADIATION_FOG`，注册粒子类型 `hbm:radiation_fog`。
- 新增 `RadiationFogParticle` / provider：
  - 使用 `ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT`
  - 使用旧颜色、旧 400 tick alpha 曲线、旧 `7.5F` scale
  - 禁用物理碰撞，按旧版仅阻尼速度
- 复制旧版 `fog.png` 到 `assets/hbm/textures/particle/radiation_fog.png`。
- 新增 `assets/hbm/particles/radiation_fog.json`，让现代粒子图集加载旧 fog 纹理。

仍未完全等价：

- 现代桥接每个粒子是一张大 sprite，不是旧版 25 quad 手写 GL 雾团。
- 如果后续需要完全复刻旧观感，应继续在渲染库中实现多 quad 粒子或自定义 render type。

验证：

- 2026-05-21 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。
