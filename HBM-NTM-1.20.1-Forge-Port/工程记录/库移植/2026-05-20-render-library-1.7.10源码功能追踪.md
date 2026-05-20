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

1. 按领域把 `ObjModelLibrary` 再拆子库，避免单文件继续膨胀。
2. 先补现代端的统一物品渲染桥，对齐旧 `IItemRendererProvider`，再让机器/方块实体 renderer 接入。
3. 处理 `HFRWavefrontObject.renderPart(...)` / `tessellatePart(...)` / VBO reload 的 group 语义缺口，决定是拆 JSON 还是做自定义 OBJ group 解析。
4. 单独建立 HMF、Collada DAE、JSON bus 动画三个子库入口，避免把所有动画塞进 OBJ 库。
5. 迁移 `RenderEPress` / `RenderPress` 的完整 body/head 逻辑。
6. 再推进流体罐、油处理机器、炉类机器的动态状态渲染。
7. 后续专题展开 CT 贴图、粒子、shader/skybox、HUD overlay。

## 验证

- 构建命令：`./gradlew.bat compileJava processResources --no-daemon`
- 已通过多轮验证。
- 未来每轮继续保持“少量改动 -> 编译验证 -> 文档补充”的节奏。
