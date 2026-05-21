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
