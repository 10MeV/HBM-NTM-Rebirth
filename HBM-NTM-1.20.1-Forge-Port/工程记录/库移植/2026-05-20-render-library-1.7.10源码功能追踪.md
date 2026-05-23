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

### 2026-05-22 修正：battery_pack 透明/黑条图标

- 实机症状：
  - 12 个电池包/电容在创造栏 tooltip 正常，但物品图标基本透明，只剩局部黑条。
- 根因核查：
  - 旧版 `ItemRenderBatteryPack` 不是 atlas item model，而是直接 `bindTexture(pack.texture)` 后渲染 `ResourceManager.battery_socket.renderPart("Battery"/"Capacitor")`。
  - 现代初版用 Forge baked OBJ part，再尝试用 `RenderType.entityCutoutNoCull(dynamicTexture)` 换贴图；但 baked quad 的 sprite 在模型烘焙时已固定，运行时换 RenderType 不能等价替换 OBJ UV 贴图。
  - 初版 GUI 还把旧 `GL11.glScaled(5,5,5)` 直接搬到现代 item 坐标，导致 OBJ 被放大约 16 倍并被 slot 裁切，只剩黑色边缘/阴影。
- 本批库修正：
  - `LegacyWavefrontModel` 增加按调用传入 `ResourceLocation textureLocation` 的 `renderPart` / `renderAll` 重载。
  - `BatteryPackItemRenderer` 改走 `LegacyWavefrontModel` 直绘 `models/block/machines/battery.obj` 的 `Battery` / `Capacitor` group，并按 stack 传入 `textures/block/machines/<legacyTextureName>.png`。
  - GUI 缩放改为现代坐标下的 `0.36` 量级，等价于旧 5 像素级渲染，不再使用错误的 `5.0` block/model 缩放。
- 迁移规则更新：
  - 后续凡是 1.7.10 通过 `bindTexture(...) + HFRWavefrontObject.renderPart(...)` 动态换整张 OBJ 贴图的路径，应优先走 `LegacyWavefrontModel` 或补参数化 legacy OBJ 渲染库，不应依赖 Forge baked OBJ + RenderType 来动态替换 sprite。
- 本批验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

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

### 2026-05-24 修正：方块实体渲染距离

- 实机症状：
  - 远距离俯视大型机器时，地面仍出现机器阴影/黑块，但 OBJ 机器模型消失。
- 1.7.10 对照：
  - 大量机器 TileEntity 同时覆盖 `getRenderBoundingBox()` 与 `getMaxRenderDistanceSquared()`。
  - 已核对样例：
    - `TileEntityMachinePumpjack` 的 AABB 在自身类中给出，最大渲染距离继承 `TileEntityOilDrillBase#getMaxRenderDistanceSquared()`。
    - `TileEntityOilDrillBase#getMaxRenderDistanceSquared()` 返回 `65536.0D`。
    - `TileEntityMachineAssemblyMachine`、`TileEntityMachineChemicalPlant`、`TileEntityMachineLiquefactor`、`TileEntityMachinePress` 也返回 `65536.0D`。
  - `65536.0D` 对应 256 格视距。
- 现代根因：
  - 现有 renderer 只实现 `shouldRenderOffScreen() == true`，这只能避免视锥/离屏裁剪造成的大 AABB 问题。
  - Forge 1.20.1 的 `BlockEntityRenderer` 仍有独立 view distance；默认距离会让远处 BER 不再调用，导致“阴影/占位仍可见但模型被距离裁掉”。
- 本批修正：
  - 新增 `LegacyBlockEntityRenderDistances.MACHINE = 256`，作为 1.7.10 `65536.0D` 的现代常量入口。
  - 为现有机器 BER 覆盖 `getViewDistance()`：
    - `LegacyVisibleMachineRenderer`
    - `AssemblyMachineRenderer`
    - `ChemicalPlantRenderer`
    - `LiquefactorRenderer`
    - `BasicMachineRenderer`
    - `MachineBatterySocketRenderer`
- 后续规则：
  - 后续移植旧 TileEntity renderer 时，凡 1.7.10 覆盖 `getMaxRenderDistanceSquared()`，现代 BER 必须同步覆盖 `getViewDistance()`；不能只补 `shouldRenderOffScreen()`。
- 规则：旧版“实体灯芯/灯罩内发光模型分件”使用 `solid(...)`；旧版“外扩透明光晕/光束”使用 `lightning(...)`。

### 2026-05-22 粒子库交叉记录

- 粒子库的实现进展已记录到 `2026-05-20-sound-particle-effects-library-1.7.10源码功能追踪.md`。
- 本轮粒子侧批量桥接了旧 `ParticleCreators` 剩余高频入口：`explosionLarge`、`explosionSmall`、`blackPowder`、`ashes`、`casingNT`、`skeleton`。
- 新增 `hbm:black_powder_spark` 现代粒子类型与 provider；其余入口先通过 `HbmParticleEffects` 组合现代粒子近似表现。
- 仍未深迁旧自定义模型粒子和 world-last/手写 GL 粒子渲染，如 `ParticleDebris`、`ParticleMukeWave`、`ParticleSpentCasing`、`ParticleSkeleton`。

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
  - 2026-05-23 复查修正：`LegacyWavefrontModel.renderOnlyInCallOrder(...)` 不再通过 name map 只取最后一个同名 group；现在按调用名逐个扫描 `groupOrder`，与旧 `renderPart(...)`/`renderOnly(...)` 的“同名 group 全部渲染”语义保持一致。
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

### 第 33 轮

- 继续补渲染库共享能力与发射台资源入口。
- `LegacyWavefrontModel` 直渲染桥扩展：
  - 实现 `LegacyObjModel`，新增 `renderAll(ObjRenderContext)`、`renderPart(...)`、`renderOnly(...)`、`renderAllExcept(...)`、`getPartNames()`。
  - `renderOnly` / `renderAllExcept` 按旧 `HFRWavefrontObject` 语义使用大小写不敏感 group 名，并保持 OBJ group 顺序渲染。
  - 新增 `groupOrder`，避免同名 `o/g` group 覆盖前面的 group；`renderPart` 会渲染所有同名 group。
  - 面渲染补强：三角面继续补第 3 点为 quad；四边面直接输出；超过 4 点的面改用 fan 三角拆分，避免旧实现只按 4 点步进漏尾面。
  - 法线计算增加退化面兜底，避免零长度 cross product 产生无效 normal。
- 新增领域模型库：
  - `ObjLaunchModels`：`launch_table_base`、大小 pad、大小 scaffold base/connector/empty，以及 Soyuz launcher 的 legs/table/tower_base/tower/support_base/support。
  - `ObjModelLibrary` 暴露 `LAUNCH_TABLE_*`、`SOYUZ_LAUNCHER_*` facade，并新增 `launchPart(String name)`。
- 1.7.10 事实源：
  - `ResourceManager` 模型字段：`launch_table_base`、`launch_table_large_pad`、`launch_table_small_pad`、`launch_table_large_scaffold_base`、`launch_table_large_scaffold_connector`、`launch_table_large_scaffold_empty`、`launch_table_small_scaffold_base`、`launch_table_small_scaffold_connector`、`launch_table_small_scaffold_empty`、`soyuz_launcher_legs`、`soyuz_launcher_table`、`soyuz_launcher_tower_base`、`soyuz_launcher_tower`、`soyuz_launcher_support_base`、`soyuz_launcher_support`。
  - 旧 renderer/helper：`RenderLaunchTable`、`SoyuzLauncherPronter`。
  - 旧贴图路径：`textures/models/missile_parts/launch_table*.png` 与 `textures/models/soyuz_launcher/launcher_*.png`。
- 资源迁入：
  - 复制 15 个旧 OBJ 到 `models/block/launch_table/`。
  - 复制 13 张旧贴图到 `textures/block/launch_table/`。
  - 为所有 OBJ 补本地 `.mtl`、`mtllib`、`usemtl default` 和 Forge OBJ JSON。
  - `launch_table_*_scaffold_empty` 对齐旧 `RenderLaunchTable`：旧代码渲染 empty 模型时绑定的是对应 scaffold base 贴图，现代 JSON 同样复用 base 贴图。
- 本轮没有迁移：
  - `RenderLaunchTable` 的 metadata 朝向、`padSize` 分支、按导弹高度循环渲染 scaffold、connector 层判断、`MissileMultipart` / `MissilePronter` 导弹本体渲染。
  - `SoyuzLauncherPronter` 的完整支架/塔/支撑条件和 transform。
  - `soyuz.obj` 火箭本体、Soyuz capsule/module、多套 `soyuz_luna` / `soyuz_authentic` 贴图变体；这组依赖大量 `renderOnly` 多贴图调用，放到后续火箭专题。
- 对齐清单：
  - `工程记录/库移植/生成报告/render-library-alignment-2026-05-21.csv` 追加本轮 launch table / Soyuz launcher 15 个条目。

### 第 34 轮

- 继续扩大上一轮 `LegacyWavefrontModel.renderOnly(...)` 的使用面，迁入 Soyuz / 胶囊 / 轨道舱模型资源与多贴图 helper。
- 1.7.10 事实源：
  - `ResourceManager` 模型字段：`soyuz`、`soyuz_lander`、`soyuz_module`。
  - 旧模型路径：`models/soyuz.obj`、`models/soyuz_lander.obj`、`models/soyuz_module.obj`。
  - 旧 renderer/helper：`SoyuzPronter`、`RenderCapsule`。
  - 旧贴图路径：
    - `textures/models/soyuz/*.png`
    - `textures/models/soyuz_luna/*.png`
    - `textures/models/soyuz_authentic/*.png`
    - `textures/models/soyuz_capsule/*.png`
    - `textures/items/polaroid_memento.png`
- 新增 `ObjSoyuzModels`：
  - `SOYUZ`、`LANDER`、`MODULE` 使用 `LegacyWavefrontModel`，保留旧 OBJ group 直接渲染路径。
  - `SoyuzTextureSet` 复刻旧 `SoyuzPronter.SoyuzSkin` 的 11 张贴图字段：`engineBlock`、`bottomStage`、`topStage`、`payload`、`payloadBlocks`、`les`、`lesThrusters`、`mainEngines`、`sideEngines`、`booster`、`boosterSide`。
  - 提供三套贴图集：`SOYUZ_TEXTURES`、`LUNA_TEXTURES`、`AUTHENTIC_TEXTURES`。
  - 提供 helper：
    - `renderSoyuz(...)`：按旧 `prontMain + prontBoosters` 顺序渲染整箭体。
    - `renderMain(...)`：逐 group 绑定 `EngineBlock`、`BottomStage`、`TopStage`、`Payload`、`Memento`、`PayloadBlocks`、`LES`、`LESThrusters`、`MainEngines`、`SideEngines`。
    - `renderBoosters(...)`：批量渲染 `Booster.*`、`BoosterEngines.*`、`BoosterSide.*`。
    - `renderLanderCapsule(...)` / `renderLanderChute(...)`：对齐 `RenderCapsule` 和旧测试 renderer 的 `Capsule` / `Chute` 分件。
    - `renderModule(...)`：对齐旧 `prontCapsule` 的 `Dome`、`Capsule`、`Propulsion`、`Solar` 多贴图顺序。
  - `ObjModelLibrary` 暴露 `SOYUZ`、`SOYUZ_LANDER`、`SOYUZ_MODULE`。
- 资源迁入：
  - 复制 3 个旧 Soyuz OBJ 到 `models/block/soyuz/`。
  - 复制普通、Luna、Authentic 三套 Soyuz 皮肤到 `textures/block/soyuz/soyuz*`。
  - 复制 capsule/module 贴图到 `textures/block/soyuz/capsule/`。
  - 复制旧 `polaroid_memento.png` 到 `textures/block/soyuz/polaroid_memento.png`，保持旧 `Memento` group 的贴图来源。
- 本轮没有迁移：
  - Soyuz 实体飞行、发射流程、分级动画、降落伞/返回舱物理。
  - `RenderSoyuz` / `RenderSoyuzCapsule` 实体 renderer 的朝向、缩放、运动状态。
  - launch table 与 Soyuz launcher 的组合渲染；本轮只让 Soyuz 旧 group 多贴图渲染具备现代库入口。
- 对齐清单：
  - `工程记录/库移植/生成报告/render-library-alignment-2026-05-21.csv` 追加 `soyuz`、`soyuz_lander`、`soyuz_module`。

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

## 2026-05-22 Battery Socket Dynamic OBJ Consumer

触发来源：

- `battery_pack` 物品图标透明问题修正后，`machine_battery_socket` 也需要复用同一个旧 `battery.obj`，并在插入电池包时按物品动态切换贴图与 OBJ part。

本轮库使用/约束：

- `MachineBatterySocketRenderer` 继续使用 `LegacyWavefrontModel` 直渲染旧 OBJ group。
- 静态底座使用 `Socket` / `Supports`，动态插入物只在现代 item 是 `HbmBatteryPackItem` 时渲染：
  - 电池包：`Battery`
  - 电容：`Capacitor`
  - 贴图：`textures/block/machines/<legacyTextureName>.png`
- 这与 1.7.10 `RenderBatterySocket` 对 `battery_pack` 的分支一致；`battery_sc` 分支等待对应物品族迁移后再接入，当前不使用默认模型代替。

仍未完全等价：

- 尚未实现旧 VBO 缓存/reload。
- `battery_sc` 的 `battery_sc.png` 插座渲染分支未接入。
- 方块 item 的旧 `IItemRendererProvider` 手持/背包显示仍主要依赖现代 item model，需要后续截图校准。

验证：

- 2026-05-22 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 Battery Socket battery_sc 分支与创造电池特效缺口

本轮对照修正：

- 旧 `RenderBatterySocket` 对插入物有三个分支：
  - `battery_pack`：按 pack 贴图渲染 `Battery` / `Capacitor`。
  - `battery_sc`：绑定 `ResourceManager.battery_sc_tex`，渲染 `Battery`。
  - `battery_creative`：不渲染普通电池模型，而是使用 `textures/models/horse/sunburst.png`、`HorsePronter` 和 `BeamPronter` 画旋转特效与随机光束。
- 此前记录中“普通 `battery_creative` 不会以 3D 电池包模型显示”成立，但不等于“完全不显示”；现代仍缺创造电池插座特效。

本轮现代接入：

- `MachineBatterySocketRenderer` 对非空 `HbmSelfChargingBatteryItem` 增加旧 `battery_sc` 分支：
  - OBJ part：`Battery`
  - 贴图：`textures/block/machines/battery_sc.png`
- `battery_creative` 仍不使用默认电池模型；待后续迁移 `HorsePronter` / `BeamPronter` 或建立等价现代特效渲染入口。

仍未完全等价：

- 旧 `HorsePronter` 与 `BeamPronter` 没有现代库入口。
- 旧电击 beam/爆炸涉及实体 renderer、投射物和爆炸库，本轮只记录、不实现。

验证：

- 2026-05-22 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 BeamPronter SOLID/RANDOM 桥与创造电池光束

1.7.10 对照：

- `src/main/java/com/hbm/render/util/BeamPronter.java`
  - `prontBeam(...)` 会把输入 `Vec3 skeleton` 旋转到局部 Y 轴方向。
  - `EnumBeamType.SOLID` 路径关闭贴图/光照/cull，开启 `SRC_ALPHA, ONE` 加色混合，默认不写深度。
  - `EnumWaveType.RANDOM` 使用 `rand.setSeed(start)` 后每段随机旋转 `spinner`，形成断续闪电状 beam。
  - SOLID 每段按 `layers` 画四组 quad，颜色从 `outerColor` 插值到 `innerColor`，半径为 `thickness / layers * layer`。
- `RenderBatterySocket` 的 `battery_creative` 分支：
  - 随 `worldTime / 5` 建 `Random`，先 `nextBoolean()`，再对四个角点 `rand.nextInt(4) == 0` 触发。
  - 每条 beam 在局部 `translate(0, 0.75, 0)` 后指向 `(0.4375 * i, 1.1875, 0.4375 * j)`。
  - 先画 `segments=15, size=0.0625F, layers=3, thickness=0.025F` 的 RANDOM/SOLID beam，再叠一条 `segments=1, size=0` 的直 beam。
  - 颜色参数为 `outer=0x404040`、`inner=0x002040`。

本轮现代接入：

- 新增 `src/main/java/com/hbm/ntm/client/obj/LegacyBeamRenderer.java`：
  - 复刻旧 `BeamPronter` 的 SOLID beam 几何思路。
  - 使用 `LegacyUntexturedQuadRenderer.lightning(...)`，继承专用 `hbm_legacy_additive_no_cull` RenderType 的无贴图、无 cull、加色混合、不写深度规则。
  - 支持 `WaveType.RANDOM` / `WaveType.SPIRAL`，本轮创造电池只使用 RANDOM。
- `MachineBatterySocketRenderer` 对 `ModItems.BATTERY_CREATIVE` 增加旧分支：
  - 不渲染普通 `Battery`/`Capacitor` OBJ part。
  - 按旧随机门槛和四角坐标生成间歇性蓝灰电弧。
  - 保留旧 `System.currentTimeMillis() % 1000 / 50` 的 start 节奏。

仍未完全等价：

- 旧 `HorsePronter` 核心尚未迁移；创造电池现在恢复的是四角 `BeamPronter` 光束，不包含旧 `textures/models/horse/sunburst.png` + 独角小马模型。
- `LegacyBeamRenderer` 目前只覆盖 SOLID beam，`EnumBeamType.LINE` 仍未迁移。
- 旧 OpenGL 的 shade model/局部旋转矩阵细节仍需实机截图校准。

验证：

- 2026-05-22 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 HorsePronter 桥与 BeamPronter LINE

1.7.10 对照：

- `src/main/java/com/hbm/render/util/HorsePronter.java`
  - 模型：`models/mobs/horse.obj`，旧端用 `HFRWavefrontObject(..., false).asVBO()`。
  - 默认贴图：`textures/models/horse/horse_demo.png`。
  - 重要 part：`Body`、`Head`、`Mane`、`NoseMale`、`NoseFemale`、`HornPointy`、`LeftFrontLeg`、`RightFrontLeg`、`LeftBackLeg`、`RightBackLeg`、`Tail`、`LeftWing`、`RightWing`。
  - API：`reset()`、`enableHorn()`、`enableWings()`、`setMaleSnoot()`、`setAlicorn()`、`poseStandardSit()`、`pose(id,yaw,pitch,roll)`、`pront()`。
  - 各部位旋转中心：
    - head `(0, 1.125, 0.375)`
    - front legs `(±0.125, 0.75, 0.3125)`
    - back legs `(±0.125, 0.75, -0.25)`
    - tail `(0, 1.125, -0.4375)`
- `RenderBatterySocket` 的 `battery_creative` 核心：
  - `scale(0.75)`。
  - `rotate((worldTime % 360 + interp) * 25, 0, -1, 0)`。
  - 绑定 `textures/models/horse/sunburst.png`。
  - `HorsePronter.reset()` -> `enableHorn()` -> `pront()`。
- `BeamPronter.EnumBeamType.LINE`：
  - 逐段输出 outerColor 线段。
  - 最后从原点到 skeleton length 画 innerColor 主线。

本轮现代接入：

- 新增 `src/main/java/com/hbm/ntm/client/obj/LegacyHorseRenderer.java`：
  - 使用 `LegacyWavefrontModel` 直渲染 `models/block/mobs/horse.obj`。
  - 复制旧贴图到 `textures/block/horse/`：`horse_demo.png`、`sunburst.png`、`dyx.png`、`numbernine.png`。
  - 保留旧 pose/horn/wings/male snoot API 语义，但现代调用显式传入 `PoseStack`、`MultiBufferSource`、贴图、light/overlay。
- `MachineBatterySocketRenderer` 的 `battery_creative`：
  - 在旧 socket transform 下恢复 `scale(0.75)` 与 `Axis.YN` 旋转。
  - 使用 `LegacyHorseRenderer.SUNBURST_TEXTURE` 渲染带 horn 的 horse 核心。
  - 继续叠加上一轮恢复的四角随机 `BeamPronter` 风格光束。
- `LegacyBeamRenderer` 新增 `lineBeam(...)`：
  - 作为旧 `EnumBeamType.LINE` 的现代桥，使用 `LegacyUntexturedQuadRenderer.solid(...)` 画窄 quad 线段。
  - 供后续炮塔、粒子加速器、core component 等旧 LINE beam 调用。

仍未完全等价：

- 旧 `HorsePronter` 用 VBO；现代当前仍走 `LegacyWavefrontModel` 逐帧顶点输出。
- `lineBeam(...)` 以窄 quad 模拟旧 GL line primitive；宽度固定为 `0.01`，后续若实机和旧线宽差异明显再按调用点加参数。
- 旧 `GL11.glDisable(GL_CULL_FACE)` 对 horse 模型的状态暂未单独建 textured no-cull RenderType；若 sunburst horse 局部背面消失，再补 `LegacyWavefrontModel` 的 textured no-cull 开关。

验证：

- 2026-05-22 运行 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-21 LegacyWavefrontModel 直渲染桥

触发来源：

- 组装机放置后只显示多方块占位 hitbox/阴影，BER 中通过 Forge baked split OBJ 分件未可靠显示旧 `assembly_machine.obj`。
- 旧版 `RenderAssemblyMachine` 直接调用 `ResourceManager.assembly_machine.renderPart("Base")` 等 `HFRWavefrontObject`/VBO group 渲染接口。

本轮库推进：

- 新增 `src/main/java/com/hbm/ntm/client/obj/LegacyWavefrontModel.java`。
- 该类是旧 `HFRWavefrontObject` 的最小现代承载层：
  - 从 resource manager 读取 OBJ。
  - 支持 `o` / `g` group。
  - 支持 `f v/vt/vn`、`f v/vt`、`f v//vn`、`f v` 与 OBJ 正/负索引。
  - 对齐旧 loader 的 V 翻转：`vt.v -> 1 - v`。
  - 提供 `renderPart(...)` 与 `renderAll(...)`，通过 `RenderType.entityCutoutNoCull(texture)` 直接向 BER 的 `MultiBufferSource` 输出顶点。
- `AssemblyMachineRenderer` 先改用该桥作为验证样本，不再依赖 Forge baked model 分件输出。

仍未完成：

- 尚未实现旧 `HFRWavefrontObjectVBO` 的 VBO 缓存/reload。
- 尚未实现 `renderOnly(...)`、`renderAllExcept(...)` 和 texture offset。
- 尚未把 `ObjPartModel` 与该直渲染桥统一成同一个可替换后端；当前先保留两条路径：静态/拆分 baked model 与旧式动态 group 直渲染。

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

## 2026-05-22 Legacy OBJ Reload Listener

触发来源：

- 1.7.10 `ClientProxy#registerRenderInfo` 会向 `IReloadableResourceManager` 注册 `HFRModelReloader`。
- 旧 `HFRModelReloader` 遍历 `HFRWavefrontObject.allModels`，销毁并重读 OBJ；随后遍历 `HFRWavefrontObject.allVBOs` 重建 VBO。
- 现代 clean port 的 `LegacyWavefrontModel` 已承接 `HFRWavefrontObject` 的 group 直渲染语义，但此前静态 renderer 持有的模型不会响应客户端资源 reload。

本轮现代桥接：

- `LegacyWavefrontModel` 增加弱引用模型登记表，构造时登记所有现代旧 OBJ 直渲染模型。
- `LegacyWavefrontModel.reloadAll(ResourceManager)` 会遍历登记模型，清空 group/part 数据并使用 Forge 传入的客户端 `ResourceManager` 重新读取 OBJ。
- 新增 `LegacyModelReloadListener`，通过 `RegisterClientReloadListenersEvent` 接入 MOD bus。
- `ClientModEvents` 注册该 listener，使 `AssemblyMachineRenderer`、`MachineBatterySocketRenderer`、`BatteryPackItemRenderer`、`ObjSoyuzModels` 等静态持有的 `LegacyWavefrontModel` 可随资源包刷新。

仍未完全等价：

- 旧 `HFRWavefrontObjectVBO` 的 GPU VBO 缓存尚未迁移；当前现代路径仍是每帧向 `MultiBufferSource` 发顶点。
- Forge baked OBJ JSON 模型由 Minecraft/Forge 自身模型管线 reload，本 listener 只负责 `LegacyWavefrontModel` 直渲染模型。

## 2026-05-22 ResourceManager Machine OBJ Batch

触发来源：

- 1.7.10 `ResourceManager` 中仍有大量机器 OBJ 静态字段尚未进入 clean port 的现代模型库。
- 本轮选择低耦合模型入口批量迁移，只建立资源和 `LegacyWavefrontModel` 句柄，不提前迁入机器状态、流体、动画和多方块 renderer 逻辑。

本轮资源/入口：

- 拷贝 1.7.10 OBJ 到 `assets/hbm/models/block/machines/`：
  - `refinery` / `refinery_exploded`
  - `fluidtank` / `fluidtank_exploded`
  - `vacuum_distill`、`catalytic_cracker`、`catalytic_reformer`、`hydrotreater`、`liquefactor`、`solidifier`、`compressor`、`coker`、`pyrooven`
  - `bat9000`、`bigasstank`、`orbus`
  - `turbofan`、`turbinegas`、`steam_engine`、`industrial_turbine`、`chungus`
  - `tower_small`、`tower_large`、`condenser`、`wood_burner`、`combustion_engine`、`pump`
  - `ammo_press`、`annihilator`、`assembly_factory`
  - `chemical_plant`、`chemical_factory`、`purex`、`mixer`
  - `arc_welder`、`soldering_station`、`arc_furnace`
  - `centrifuge`、`gascent`、`silex`、`fel`
  - `autosaw`、`mining_drill`、`ore_slopper`、`mining_laser`
  - `acidizer`、`cyclotron`、`exposure_chamber`、`machine_deuterium_tower`、`radgen`
- 拷贝对应默认贴图到 `assets/hbm/textures/block/machines/`，并补常见动态分层贴图：
  - `fluidtank_inner`
  - `turbofan_back`、`turbofan_afterburner`、`turbofan_blades`
  - `pump_electric`
  - `annihilator_belt`
  - `assembly_factory_sparks`
  - `chemical_plant_fluid`
  - `mining_laser_pivot`、`mining_laser_laser`
- `ObjMachineModels` 新增对应 `LegacyWavefrontModel` 字段；`ObjModelLibrary` 暴露 `MACHINE_*` facade，供后续 renderer 迁移使用。

已确认的重要旧 group：

- `fluidtank`：`Frame` / `Tank`
- `fluidtank_exploded`：`TankInner` / `Tank` / `Frame`
- `compressor`：`Pump` / `Fan` / `Compressor`
- `liquefactor`、`solidifier`：`Fluid` / `Glass` / `Main`
- `pyrooven`：`Fan` / `Slider` / `Oven`
- `steam_engine`：`Shaft` / `Piston` / `Transmission` / `Flywheel` / `Base`
- `industrial_turbine`：`Flywheel` / `Gauge` / `Turbine`
- `turbofan`：`Blades` / `Afterburner` / `Body`
- `assembly_factory`：`Base` / `Frame` / `Slider1..4` / `ArmLower1..4` / `ArmUpper1..4` / `Head1..4` / `Striker1..4` / `Blade2` / `Blade4`
- `chemical_plant`：`Base` / `Slider` / `Frame` / `Spinner` / `Fluid`

仍未完全等价：

- 本轮未接任何机器 renderer；旧 `RenderFluidTank`、`RenderChemicalPlant`、`RenderAssemblyFactory` 等动画、流体颜色、动态贴图切换仍待对应机器迁移时处理。
- `fluidtank.obj` 保留旧 `mtllib/usemtl` 行，但现代 `LegacyWavefrontModel` 直渲染路径当前按调用纹理统一绘制，不解析 MTL 材质。

## 2026-05-22 Radiation Fog Legacy Multi-Quad

触发来源：

- 实机反馈区块辐射黄雾仍不明显，并且区块辐射更新疑似带来卡顿。
- 复核 1.7.10 后确认旧 `ChunkRadiationHandlerSimple` 每次只发一个 `radFog` effect；单个 `ParticleRadiationFog` 内部再用固定随机种子绘制 25 个半透明 quad。

旧版契约补充：

- `ParticleRadiationFog#renderParticle` 每帧 `new Random(50)`，固定生成 25 个局部偏移 quad。
- 偏移范围：X/Z 使用 `(gaussian - 1) * 2.5`，Y 使用 `(gaussian - 1) * 0.15`，每个 quad 还有 `gaussian * 0.5` 的位置抖动。
- 每个 quad 尺寸为 `rand.nextDouble() * particleScale`，默认 `particleScale = 7.5F`。
- 旧区块辐射只创建一个粒子事件，不是发送 25 个独立粒子。

本轮现代修正：

- `RadiationFogParticle#render(...)` 改为单粒子内部绘制 25 个 billboard quad，保留旧固定随机种子、颜色、alpha 曲线、fullbright 和尺寸规则。
- `ChunkRadiationManager#spawnRadiationFog(...)` 将 `sendParticles` 数量从 `25` 改回 `1`，与旧 `radFog` effect 数量对齐，避免现代侧把“粒子个数”和“单粒子内部 quad 个数”重复放大。

仍未完全等价：

- 现代实现仍走 `ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT` 与粒子图集，不直接复刻旧 GL11 状态切换。
- 旧版 `glDepthMask(false)` / blend 状态由现代 translucent particle pipeline 承接，若后续截图仍有遮挡差异，再考虑专用 RenderType。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-23 ObjUtil Sprite Retexture Bridge

触发来源：

- 继续推进渲染库对齐，补齐旧 `ObjUtil.renderWithIcon(...)` / `renderPartWithIcon(...)` 这条 ISBRH 关键路径。
- 旧调用面覆盖 RBMK rod/control、RTG、管道/电缆、铁砧、轨道、灯具、分流器、漏斗等，不应在后续每个 renderer 中重复手写 OBJ UV 到 block icon 的转换。

旧版事实：

- `ObjUtil.renderWithIcon(...)` 遍历模型所有 `groupObjects`，使用 OBJ 原始 UV 对传入 `IIcon` 做 `getInterpolatedU/V(t.u * 16)` 采样。
- `ObjUtil.renderPartWithIcon(...)` 与 `HFRWavefrontObject.renderPart(...)` 不完全一致：它按 `obj.name.equals(name)` 精确匹配，并保留最后一个同名 group；找不到时直接返回。
- `renderWithIcon(...)` 和 `renderPartWithIcon(...)` 的旧阴影公式不同：
  - 全模型路径：`((normal.y + 0.7F) * 0.9F) - abs(normal.x) * 0.1F + abs(normal.z) * 0.1F`
  - 单 part 路径：`normal.y * 0.3F + 0.7F - abs(normal.x) * 0.1F + abs(normal.z) * 0.1F`
- 旧 `ObjUtil` 会把三角面第三个顶点重复提交一次，用 quad tessellator 渲染三角模型。

本轮现代修正：

- `LegacyWavefrontModel` 新增 `renderWithSprite(...)`：
  - 接收 `TextureAtlasSprite`，走 `InventoryMenu.BLOCK_ATLAS`。
  - 复用旧 `ObjUtil` yaw/pitch/roll 旋转顺序。
  - 使用 OBJ UV 的 `u/v * 16` 对 sprite 做 `getU/getV` 采样。
  - 三角面继续重复第 3 个顶点，保留旧 quad tessellator 兼容技巧。
- `LegacyWavefrontModel` 新增 `renderPartWithSprite(...)`：
  - 精确 group 名匹配，取最后一个同名 group，找不到时静默返回，避免污染普通 `renderPart(...)` 的 HFR 语义。
  - 单 part 阴影公式按旧 `ObjUtil.renderPartWithIcon(...)` 独立实现。
- `ObjRenderContext` 重载可直接带现代上下文、颜色和 legacy shadow 状态进入该路径。

noSmooth 对照报告：

- 新增 `工程记录/库移植/生成报告/render-library-nosmooth-audit-2026-05-23.csv`。
- 该报告按旧 `ResourceManager` 中所有 `.noSmooth()` 资源列出：
  - `legacy-nosmooth-hooked`：现代 `LegacyWavefrontModel` 已接 `.noSmooth()`。
  - `modern-entry-needs-nosmooth-audit`：现代已有入口，但多为 split/baked model 或尚未接 `.noSmooth()`，需逐项决定是否回退 legacy OBJ 载体。
  - `missing-modern-entry`：现代还没有对应资源入口。

仍未完全等价：

- `renderWithSprite(...)` 只覆盖旧 `ObjUtil` 的图集重贴图直渲染；仍未迁 `tessellateAll/tessellateOnly/tessellatePart/tessellateAllExcept` 的外部顶点遍历 API。
- 旧固定管线的 `Tessellator.setNormal(...)` 与现代 `VertexConsumer.normal(...)` 已按面法线承接，但实际方块 AO/光照仍需后续实机核对。
- 旧 `ObjUtil` 有全局 `setColor/clearColor` 状态；现代路径通过参数/`ObjRenderContext` 传色，不保留全局 mutable 状态。

验证：

- `.\gradlew.bat compileJava --rerun-tasks --no-daemon` 当前被无关 `ModCommands` 缺失 `getRorFunctions/getRorValue/runRorFunction` 阻塞；本轮新增的 `TextureAtlasSprite#getU/getV` 与渲染库签名未触发编译错误。

## 2026-05-23 Network / Soyuz noSmooth Legacy Entrypoints

触发来源：

- 继续按 `render-library-nosmooth-audit-2026-05-23.csv` 推进旧 `.noSmooth()` 资源对齐。
- 网络连接器/电塔与 Soyuz 发射架现代端已有 baked OBJ 入口，但旧 `ResourceManager` 明确使用 `HFRWavefrontObject(...).noSmooth().asVBO()`；baked JSON 入口不能自动表达旧 HFR 面法线语义。

本轮现代修正：

- `ObjNetworkModels` 保留原 baked `ObjModelPart` 常量，同时新增 legacy noSmooth 入口：
  - `CONNECTOR_LEGACY`
  - `CONNECTOR_SUPER_LEGACY`
  - `PYLON_LARGE_LEGACY`
  - `PYLON_MEDIUM_LEGACY`
- `ObjLaunchModels` 保留原 baked `ObjModelPart` 常量，同时新增 Soyuz launcher legacy noSmooth 入口：
  - `SOYUZ_LAUNCHER_LEGS_LEGACY`
  - `SOYUZ_LAUNCHER_TABLE_LEGACY`
  - `SOYUZ_LAUNCHER_TOWER_BASE_LEGACY`
  - `SOYUZ_LAUNCHER_TOWER_LEGACY`
  - `SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY`
  - `SOYUZ_LAUNCHER_SUPPORT_LEGACY`
- `ObjModelLibrary` 暴露这些 legacy facade，后续迁旧 renderer 时可以选择 HFR 语义路径，不必继续依赖 baked fallback。
- `render-library-nosmooth-audit-2026-05-23.csv` 状态更新：
  - `legacy-nosmooth-hooked`: 19
  - `modern-entry-needs-nosmooth-audit`: 17
  - `missing-modern-entry`: 42

仍未完全等价：

- 旧 VBO 缓存仍未迁；这些 legacy 入口使用现代 `LegacyWavefrontModel` 直提交顶点。
- 现有 baked 常量暂未移除，避免破坏已经接入的现代 renderer；真正迁某个旧 renderer 时应优先选择 `_LEGACY` 常量并做实机对照。

验证：

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

## 2026-05-23 Machine noSmooth Legacy Entrypoints

触发来源：

- 继续推进 `render-library-nosmooth-audit-2026-05-23.csv` 中 `modern-entry-needs-nosmooth-audit` 项。
- 本批选择现代端已有 OBJ/贴图资源，且旧 `ResourceManager` 明确 `.noSmooth()` 的机器模型；只新增 legacy HFR 语义入口，不替换现有 baked 分件入口。

本轮现代修正：

- `ObjMachineModels` 新增以下 legacy noSmooth 入口：
  - `FIREBOX_LEGACY`
  - `HEATING_OVEN_LEGACY`
  - `ELECTRIC_HEATER_LEGACY`
  - `RTG_LEGACY`
  - `RADAR_SCREEN_LEGACY`
  - `SOLAR_MIRROR_LEGACY`
- `ObjModelLibrary` 同步暴露 `MACHINE_*_LEGACY` facade，供后续旧 renderer 迁移时选择对齐 1.7.10 HFR 面法线路径。
- `render-library-nosmooth-audit-2026-05-23.csv` 当前状态：
  - `legacy-nosmooth-hooked`: 25
  - `modern-entry-needs-nosmooth-audit`: 11
  - `missing-modern-entry`: 42

仍未完全等价：

- `radar` / `radar_large` / `radar_body` 旧三件套未在本批补齐；现代端当前只有 `radar_screen` 资源入口。
- `firebox` 已有 split baked 分件和新 legacy 整体入口并存；具体 renderer 迁移时需按旧 `renderPart` 调用决定使用整体 HFR 入口还是继续维护分件桥。

验证：

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

## 2026-05-23 noSmooth Audit Queue Cleared

触发来源：

- 继续推进 `render-library-nosmooth-audit-2026-05-23.csv`，一次性处理剩余 `modern-entry-needs-nosmooth-audit`。
- 本批对象覆盖灯具、雷达本体、旧 ISBRH block OBJ，以及 HEV battery / capacitor 的旧 block 模型入口。

本轮现代修正：

- `ObjLightModels` 新增旧灯具 HFR noSmooth 入口：
  - `CAGE_LAMP_LEGACY`
  - `FLUORESCENT_LAMP_LEGACY`
  - `FLOOD_LAMP_LEGACY`
- `ObjMachineModels` 新增 `RADAR_LEGACY`，并从 1.7.10 复制 `models/machines/radar.obj` 到现代 `models/block/machines/radar.obj`。
- 新增 `ObjBlockModels`，集中承接旧 `ResourceManager` 的 ISBRH block OBJ：
  - `SCAFFOLD`
  - `BEAM`
  - `BARREL`
  - `POLE`
  - `PIPE`
  - `HEV_BATTERY`
  - `CAPACITOR`
- 从 1.7.10 复制对应 OBJ 到 `models/block/legacy_blocks/`；默认贴图只作为直渲染兜底，旧 renderer 的真实动态贴图仍应通过 `renderWithSprite(...)` / `renderPartWithSprite(...)` 接入。
- `ObjModelLibrary` 暴露灯具、ISBRH block、`MACHINE_RADAR_LEGACY` facade。
- `render-library-nosmooth-audit-2026-05-23.csv` 当前状态：
  - `legacy-nosmooth-hooked`: 36
  - `missing-modern-entry`: 42
  - `modern-entry-needs-nosmooth-audit`: 0

旧调用点确认：

- `RenderScaffoldBlock` / `RenderSteelBeam` / `RenderBattery` 使用 `ObjUtil.renderWithIcon(...)`。
- `RenderPipe` / `RenderCapacitor` / `RenderBarrel` 使用 `ObjUtil.renderPartWithIcon(...)`，依赖 OBJ group 名如 `Top` / `Side` / `Barrel` / `InnerTop` 等。

仍未完全等价：

- 本批仅补模型载体和 noSmooth 入口；对应方块 renderer 尚未迁移到现代 `TextureAtlasSprite` 重贴图路径。
- `pipe_rim` / `pipe_quad` / `pipe_frame` 等旧管道变体仍在 `missing-modern-entry`，后续迁 `RenderPipe` 前需要一起补齐。
- `radar_body` / `radar_large` 仍缺现代入口；本批只补 `radar`。

验证：

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

## 2026-05-23 Legacy Block OBJ noSmooth Batch

触发来源：

- `render-library-nosmooth-audit-2026-05-23.csv` 中剩余大多数 `missing-modern-entry` 都来自旧 `ResourceManager` 的 `models/blocks/*.obj`。
- 这些对象后续会被旧 ISBRH / TESR 迁移直接引用，尤其 `RenderPipe` 依赖 `pipe_rim` / `pipe_quad` / `pipe_frame` 的 group 与 `ObjUtil.renderPartWithIcon(...)` 语义。

本轮现代修正：

- 从 1.7.10 复制旧 `models/blocks` noSmooth OBJ 到现代 `models/block/legacy_blocks/`，现代目录现有 42 个旧 block OBJ。
- `ObjBlockModels` 新增以下 noSmooth 入口：
  - `TAPE_RECORDER`, `BARBED_WIRE`, `SPIKES`, `ANTENNA_TOP`, `CONSERVE_CRATE`
  - `PIPE_RIM`, `PIPE_QUAD`, `PIPE_FRAME`
  - `RTTY`, `CRT`, `TOASTER`, `DECO_COMPUTER`
  - `ANVIL`, `CRYSTAL_POWER`, `CRYSTAL_ENERGY`, `CRYSTAL_ROBUST`, `CRYSTAL_TRIXITE`
  - `CABLE_NEO`, `DIFURNACE_EXTENSION`, `SPLITTER`, `CRANE_BUFFER`
  - `RAIL_NARROW_STRAIGHT`, `RAIL_NARROW_CURVE`
  - `RAIL_STANDARD_STRAIGHT`, `RAIL_STANDARD_STRAIGHT_SHORT`, `RAIL_STANDARD_CURVE`, `RAIL_STANDARD_CURVE_WIDE7`, `RAIL_STANDARD_CURVE_WIDE9`, `RAIL_STANDARD_RAMP`, `RAIL_STANDARD_BUFFER`, `RAIL_STANDARD_SWITCH`, `RAIL_STANDARD_SWITCH_FLIPPED`
  - `FUNNEL`, `CHARGE_DYNAMITE`, `CHARGE_C4`
- `ObjModelLibrary` 暴露对应 `BLOCK_*` facade。
- 复制入口默认贴图所需的 1.7.10 `textures/blocks` 贴图到 `textures/block/legacy_blocks/`；这些默认贴图只用于普通直渲染兜底。
- `render-library-nosmooth-audit-2026-05-23.csv` 当前状态：
  - `legacy-nosmooth-hooked`: 71
  - `missing-modern-entry`: 7

仍未完全等价：

- 本批仍未迁具体旧 renderer；动态 block icon 重贴图必须继续通过 `LegacyWavefrontModel.renderWithSprite(...)` / `renderPartWithSprite(...)` 对接。
- 部分模型在旧代码中只作为 item/block 专用 renderer 使用，迁实际 renderer 前还需复查旧 metadata、旋转、group 调用顺序和 texture 选择。
- 剩余缺口为 `radar_body`, `radar_large`, `skeleton_holder`, `lantern`, `chainsaw`, `player_manly_af`, `missileStealth`。

验证：

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

## 2026-05-23 noSmooth Audit Missing Entrypoints Cleared

触发来源：

- `render-library-nosmooth-audit-2026-05-23.csv` 剩余 7 个 `missing-modern-entry` 横跨 radar、block、trinket、weapon、armor、missile 资源域。
- 本轮目标是让旧 `ResourceManager` 中所有明确 `.noSmooth()` 的模型都至少拥有现代 `LegacyWavefrontModel.noSmooth()` 入口。

本轮现代修正：

- `ObjMachineModels` 新增：
  - `RADAR_BODY_LEGACY`
  - `RADAR_LARGE_LEGACY`
- `ObjBlockModels` 新增：
  - `SKELETON_HOLDER`
- `ObjTrinketModels` 新增：
  - `LANTERN`
- 新增 `ObjWeaponModels`：
  - `CHAINSAW`
- 新增 `ObjArmorModels`：
  - `PLAYER_FEM`
- `ObjMissilePartModels` 新增：
  - `MISSILE_STEALTH`
- `ObjModelLibrary` 暴露对应 facade。
- 从 1.7.10 复制对应 OBJ/贴图到现代资源目录。
  - `ResourceManager.radar_body_tex` 旧字段声明为 `textures/models/radar_base.png`，但 1.7.10 实际资源位于 `textures/models/machines/radar_base.png`；现代端按实际资源复制到 `textures/block/machines/radar_base.png`。
- `render-library-nosmooth-audit-2026-05-23.csv` 当前状态：
  - `legacy-nosmooth-hooked`: 78
  - `missing-modern-entry`: 0
  - `modern-entry-needs-nosmooth-audit`: 0

审计结论：

- 按旧 `ResourceManager` 明确 `.noSmooth()` 的资源清单，现代渲染库已全部拥有 noSmooth 入口。

仍未完全等价：

- 旧 `asVBO()` 缓存生命周期仍未迁移；现代入口仍是 `LegacyWavefrontModel` 直提交顶点。
- 旧 renderer 的动态贴图、旋转、分件调用、item/armor/missile 特殊坐标系尚未逐项迁移。
- 下一步应转入具体旧 renderer 对接，优先 `RenderPipe` / `RenderCapacitor` / `RenderBarrel` 这类已具备 OBJ 和 `renderPartWithSprite(...)` 桥的路径。

验证：

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

## 2026-05-23 ISBRH Sprite Renderer Bridge

触发来源：

- noSmooth 入口清零后，下一步应迁旧 ISBRH renderer。直接把旧 `RenderPipe` 挂到现代 `FLUID_DUCT_NEO` 会混淆 1.7.10 `BlockPipe` 与现代流体管道系统，因此先补旧 `ObjUtil` 调用面的可复用桥。

旧版事实：

- `RenderPipe` 对 `pipe` / `pipe_rim` / `pipe_quad` / `pipe_frame` 按 `rType` 调用 `ObjUtil.renderPartWithIcon(...)`，分件名为 `Top` / `Side` / `Frame` / `Mesh`。
- `RenderCapacitor` 调用 `Top` / `Side` / `Bottom` / `InnerTop` / `InnerSide`。
- `RenderBarrel` 调用 `Barrel`，`RenderFluidBarrel` 的连接口使用 `Connector`。

本轮现代修正：

- `LegacyWavefrontModel` 新增 `renderWithSprite(TextureAtlasSprite, ObjRenderContext, ...)`，与已有 `renderPartWithSprite(...)` 对称。
- 新增 `LegacyIsbrhObjRenderer`：
  - 将现代 `ResourceLocation` 图集贴图解析为 `TextureAtlasSprite`。
  - 调用 `LegacyWavefrontModel.renderWithSprite(...)` / `renderPartWithSprite(...)`，保留旧 `ObjUtil` UV 采样和旋转语义。
- 新增专用旧 renderer 桥：
  - `LegacyPipeObjRenderer`
  - `LegacyCapacitorObjRenderer`
  - `LegacyBarrelObjRenderer`

仍未完全等价：

- 本批是旧 ISBRH 调用面的 API 桥，尚未注册具体方块 renderer。
- 现代 `FLUID_DUCT_NEO` 仍沿用现有 multipart cube 模型；旧 `BlockPipe` / `MachineCapacitor` / `BlockFluidBarrel` 的完整方块注册和 metadata 语义需单独迁移后再接这些 helper。

验证：

- `.\gradlew.bat compileJava processResources --rerun-tasks --no-daemon` 通过。

## 2026-05-22 Pumpjack Legacy Y Rotation Fix

触发来源：

- 实机观察到 `machine_pumpjack` 的模型与碰撞/占位区域相反：选框在泵模型底座一侧，但 OBJ 本体朝向另一侧。
- 复核 1.7.10 `RenderPumpjack`：
  - `NORTH(meta 2) -> 90`
  - `WEST(meta 4) -> 180`
  - `SOUTH(meta 3) -> 270`
  - `EAST(meta 5) -> 0`
- 现代 `facing.toYRot()` 映射为 `SOUTH=0, WEST=90, NORTH=180, EAST=270`；此前通用 renderer 使用 `offset + toYRot`，对泵机会把 NORTH/SOUTH 翻反。

本轮现代修正：

- `LegacyMachineDefinition` 增加可选 `yRotation(Function<Direction, Float>)`，允许单机按旧 renderer 的 metadata 表定义 Y 旋转。
- `LegacyVisibleMachineRenderer` 改为调用 `definition.yRotation(state)`，默认仍保留原 `yRotationOffset + facing.toYRot()` 兼容路径。
- `machine_pumpjack` 使用旧表等价公式 `270 - facing.toYRot()`。
- 这次排查也确认了其他可见多方块不应盲目复用同一个旋转偏移：
  - `machine_chemical_plant` / `machine_chemical_factory` 按各自旧 renderer 的 metadata 表迁入。
  - `machine_refinery` 旧 renderer 本身就是固定 180。
  - `machine_fluidtank` 需要单独对齐旧 metadata 表，而不是套用统一的 180。

仍未完全等价：

- 本轮只修泵机模型与 footprint 对齐；其他可见多方块机器如果后续发现模型朝向与 dummy 区域不一致，应逐台按 1.7.10 renderer 的 switch 表迁入 `yRotation(...)`。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 BlockEntity Render Bounding Box

触发来源：

- 实机观察到多方块 OBJ 在某些视角消失：看不见主方块时，虽然模型本体还在视野内，但 BE renderer 已被裁剪。
- 1.7.10 TESR 体系中很多大型机器在 TileEntity 上覆写 `getRenderBoundingBox()`，锻压机现代移植也已有类似做法；新 `LegacyVisibleMachineRenderer` 只实现 `shouldRenderOffScreen()`，还缺少对应的 AABB 数据源。

本轮现代修正：

- 渲染范围不放在 renderer 里硬编码，而是由 `LegacyVisibleMachineBlockEntity#getRenderBoundingBox()` 从 `LegacyMachineDefinition` 获取。
- `LegacyMachineDefinition` 支持两种策略：
  - 默认：按多方块 `LegacyMultiblockLayout.checkOffsets()` 计算覆盖结构的 AABB，并额外扩 1 格。
  - 特例：机器 definition 提供 1.7.10 同款 render AABB 覆写。
- `machine_pumpjack` 已使用旧 `TileEntityMachinePumpjack` 的 `x-7,y,z-7` 到 `x+8,y+6,z+8` 范围，因为旧 renderer 有 Rotor/Head/Carriage 动态摆动和连杆绘制，足迹盒不足以覆盖全部视觉范围。

仍未完全等价：

- 化工厂、化工厂、精炼厂、流体储罐目前先走结构推导 + 1 格 padding；如果后续动态部件或流体面超出此范围，需要按对应 1.7.10 TileEntity 的 `getRenderBoundingBox()` 再加单机覆写。
- `shouldRenderOffScreen()` 仍保留为 true，用于减少距离/边缘情况下的额外裁剪；核心修复是 BE AABB。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 Legacy Visible Multiblock Machine Renderer

触发来源：

- 多方块库开始从单台组装机扩展到一批旧 `BlockDummyable` 机器，需要一个可复用的 OBJ block entity renderer，而不是为每台静态可见机器立刻写完整旧 renderer。

本轮现代接入：

- 新增 `LegacyVisibleMachineRenderer`：
  - 从 block 的 `LegacyMachineDefinition` 获取 OBJ 模型、贴图、part 列表和 Y 轴旋转偏移。
  - 客户端按 definition 缓存 `LegacyWavefrontModel`，避免 common 侧引用 client-only 模型类。
  - 支持 `renderAll` 和指定 part 渲染两种模式，用于先迁静态机体。
- 已接入机器：
  - `machine_chemical_plant`：静态绘制 `Base` / `Frame` / `Slider` / `Spinner`，动态流体面和配方动画暂缓。
  - `machine_chemical_factory`：静态绘制 `Base` / `Frame` / `Fan1` / `Fan2`，动态 fan/recipe 逻辑暂缓。
  - `machine_refinery`：先绘制普通完整模型；旧 exploded/repair 状态暂缓。
  - `machine_fluidtank`：先绘制普通完整模型；旧 exploded/inner fluid 状态暂缓。
  - `machine_pumpjack`：静态绘制 `Base` / `Rotor` / `Head` / `Carriage`；旧 `rot/prevRot` 驱动的连杆、驴头和滑块动画暂缓。

资源入口：

- 新增 Forge OBJ block model JSON：
  - `assets/hbm/models/block/machines/chemical_plant.json`
  - `assets/hbm/models/block/machines/chemical_factory.json`
  - `assets/hbm/models/block/machines/refinery.json`
  - `assets/hbm/models/block/machines/fluidtank.json`
  - `assets/hbm/models/block/machines/pumpjack.json`
- 对应 OBJ 与贴图均来自旧 1.7.10 资源树，路径统一落在 `assets/hbm/models/block/machines/` 与 `assets/hbm/textures/block/machines/`。

仍未完全等价：

- 本轮只迁静态可见层；旧 renderer 中依赖 TileEntity 状态的 progress、fluid texture、repair/exploded、风扇/滑块/pumpjack 连杆细动画尚未接入。
- 当前 rotation offset 按旧 renderer 首次对齐值记录，仍需实机四向截图检查模型正面是否完全等价。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 Legacy Visible Machine 接线闭合

- 本批为并行迁移中的可视多方块机器补齐 `LegacyVisibleMachineBlockEntity` 的 BlockEntityType 与 BER 注册：
  - `machine_chemical_plant`
  - `machine_chemical_factory`
  - `machine_refinery`
  - `machine_fluidtank`
- 这些机器目前共用 `LegacyVisibleMachineRenderer`，通过 `LegacyVisibleMultiblockMachineBlock#definition()` 选择 OBJ 模型与贴图。
- 这只是 render-library/BlockEntity 接线闭合，不新增机器运行时逻辑。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 RBMK Legacy OBJ Batch

触发来源：

- 旧 `ResourceManager` 在 RBMK 段集中声明 `models/rbmk/*.obj`，覆盖燃料通道、控制棒、吊车、控制台、仪表、按钮、终端、自动装载机和 debris。
- 旧 renderer 混合使用两类贴图：
  - `textures/models/machines/*.png` 与 `textures/models/network/*.png`：控制台/吊车/网络显示部件的模型贴图。
  - `textures/blocks/rbmk/*.png`：燃料通道、控制棒、debris 等旧 block icon 贴图，通过 `ObjUtil.renderPartWithIcon(...)` 喂给 OBJ 部件。
- 旧 OBJ 扫描报告中 `rbmk/rbmk_element.obj` 是 RBMK 组内唯一 `REQUIRES_SPLIT_OR_CUSTOM_RENDERER`，含混合 triangle/quad group；继续走 `LegacyWavefrontModel` 直渲染路径更贴近旧端。

本轮资源/入口：

- 新增 `ObjRbmkModels`：
  - 模型：`rbmk_element`、`rbmk_element_rods`、`rbmk_rods`、`crane_console`、`crane`、`autoloader`、`rbmk_console`、`button`、`gauge`、`numitron`、`lever`、`indicator`、`terminal`、`debris`。
  - 贴图常量分两类：
    - `modelTexture(...)` -> `textures/block/rbmk/models/`
    - `iconTexture(...)` -> `textures/block/rbmk/icons/`
  - 提供 `renderFuelChannelRods(...)`、`renderControlLid(...)`、`renderDebris(...)` 和 `manualControlTexture(...)`，承接旧 `RenderRBMKFuelChannel`、`RenderRBMKControlRod`、`RenderPribris` 的基础模型/贴图选择。
- `LegacyWavefrontModel` 新增带 RGBA 顶点色的 `renderPart`、`renderAll`、`renderOnly`、`renderAllExcept` 重载，用来承接旧 `GL11.glColor3f(...)`：
  - RBMK fuel rod 的 `rodColor` 染色。
  - Crane console 指示灯、Gauge/Indicator/Numitron 等后续 fullbright 或着色层。
- 资源拷贝：
  - 14 个旧 `models/rbmk/*.obj` -> `assets/hbm/models/block/rbmk/`
  - 11 个旧模型贴图 -> `assets/hbm/textures/block/rbmk/models/`
  - 131 个旧 RBMK block icon -> `assets/hbm/textures/block/rbmk/icons/`

已确认的重要旧 group：

- `rbmk_element`：`Inner` / `Rods` / `Cap`
- `rbmk_element_rods`：`Rods`
- `rbmk_rods`：`Lid` / `Column`
- `crane_console`：`Console_Coonsole` / `JoyStick` / `Meter1` / `Meter2` / `Lamp1` / `Lamp2`，另保留旧 OBJ 中未启用的 `Shotgun` / `MiniNuke` group。
- `crane`：`Girder` / `Main` / `Tube` / `Carriage` / `Lift`
- `autoloader`：`Base` / `Piston`
- `gauge`：`Gauge` / `Needle`
- `lever`：`Base` / `Lever`
- `indicator`：`Base` / `Light`
- `button`：`Socket` / `Button`

仍未完全等价：

- 本轮只迁 render-library 入口，不迁 RBMK blocks、TileEntities、GUI、网络/中子逻辑。
- `RenderRBMKFuelChannel` 的 cherenkov additive quad、控制棒高度插值、crane console 的灯颜色/仪表抖动/吊车跨度循环、autoloader piston 插值等动态变换仍待对应 TileEntity renderer 迁移时接线。
- 顶层 `textures/block/rbmk/*.png` 中已有/未跟踪的文件不作为本轮新规范；本轮新代码只引用 `rbmk/models/` 与 `rbmk/icons/`。

验证：

- `.\gradlew.bat compileJava --rerun-tasks --no-daemon` 通过。

## 2026-05-22 Missile Parts Legacy OBJ Batch

触发来源：

- 1.7.10 导弹组装渲染由 `MissilePart.registerAllParts()` + `MissilePronter.prontMissile(...)` 驱动，不是方块模型 JSON。
- 旧 `ResourceManager` 只持有 `mp_t_*`、`mp_s_*`、`mp_f_*`、`mp_w_*` OBJ 模型字段；具体物品通过 `MissilePart` 绑定 `PartType`、高度、GUI 高度、模型和贴图。
- 本轮目标是先把完整动态导弹部件渲染库迁入现代端，供后续导弹物品、发射台、实体 renderer 接线。

本轮资源/入口：

- 新增 `ObjMissilePartModels`：
  - 迁入旧 `ResourceManager` 使用的 42 个导弹部件模型句柄：推进器、稳定翼、弹体、弹头。
  - 复刻旧 `MissilePart.registerAllParts()` 的 117 项部件映射，保留旧模型复用、贴图复用、`height`、`guiHeight` 和 `PartType` 语义。
  - 提供 `LegacyMissilePart` record 与 `PartKind` enum，供后续现代物品/实体从旧 item 名称查渲染数据。
  - 提供 `renderMissile(...)`，按旧 `MissilePronter` 顺序渲染：推进器 -> 上移推进器高度 -> 弹体层先画稳定翼再画弹体 -> 上移弹体高度 -> 弹头。
- `ObjModelLibrary` 新增：
  - `missilePartModel(String legacyModelName)`
  - `missilePart(String legacyItemName)`
- 资源拷贝：
  - 48 个旧 `models/missile_parts/*.obj` -> `assets/hbm/models/block/missile_parts/`
  - 139 个贴图文件 -> `assets/hbm/textures/block/missile_parts/`
  - 包含旧共享 `TheGadget3_.png` -> `universal.png`，以及 `boxcar.png`，用于 `mp_stability_20_flat` 与 `mp_warhead_15_boxcar`。

已确认的重要旧契约：

- `mp_thruster_15_kerosene_triple` 使用 triple 模型，但贴图复用 `mp_t_15_kerosene_dual`。
- `mp_thruster_15_hydrogen` / `mp_thruster_15_hydrogen_dual` 复用 kerosene / kerosene_dual 模型，只换 hydrogen 贴图。
- `mp_thruster_20_kerosene_triple` 使用 triple 模型，但贴图复用 `mp_t_20_kerosene_dual`。
- `mp_thruster_20_solid_multier` 复用 `mp_t_20_solid_multi` 模型，只换 `mp_t_20_solid_multier` 贴图。
- `mp_stability_20_flat` 使用 `mp_s_20` 模型和旧 `ResourceManager.universal` 贴图。
- `mp_warhead_10_cloud` 复用 `mp_w_10_taint` 模型，只换 cloud 贴图。
- `mp_warhead_15_nuclear_shark` / `mp_warhead_15_nuclear_mimi` 复用 `mp_w_15_nuclear` 模型。
- `mp_warhead_15_boxcar` 使用 `mp_w_15_boxcar` 模型和旧 `boxcar_tex`。

仍未完全等价：

- 本轮只迁 render-library 与旧映射数据，不注册导弹部件物品。
- `MissileMultipart`、发射台装载状态、导弹实体飞行/分级/爆炸行为、GUI 预览缩放与交互仍待后续系统迁移时接线。
- 现代 `LegacyWavefrontModel` 仍按单次绑定纹理渲染，不解析旧 OBJ MTL；导弹部件旧路径本身也是外部绑定贴图，因此本轮契约可对齐。

验证：

- `.\gradlew.bat compileJava --rerun-tasks --no-daemon` 通过。

## 2026-05-22 ResourceManager Turret/Bomb/Projectile OBJ Batch

触发来源：

- 1.7.10 `ResourceManager` 顶部炮塔、地雷、投射物模型仍大量是 `HFRWavefrontObject` 静态字段。
- 旧炮塔 renderer 通过 `renderPart(...)` 按 group 分层换贴图，例如炮塔通用 `Base` / `Carriage`，Howard 的 `Body` / `BarrelsTop` / `BarrelsBottom`，HIMARS 的 `TubeStandard` / `RocketStandard` / `TubeSingle` / `RocketSingle`。
- 本轮只迁 render-library 资源入口，不迁炮塔 TileEntity、AI、旋转/俯仰动画、弹药状态和地雷方块行为。

本轮资源/入口：

- 新增 `ObjTurretModels`：
  - 模型：`turret_chekhov`、`turret_jeremy`、`turret_tauon`、`turret_richard`、`turret_howard`、`turret_howard_damaged`、`turret_microwave`、`turret_fritz`、`turret_arty`、`turret_himars`、`turret_sentry`
  - 贴图常量：通用 base/carriage/connector、friendly 变体、CIWS 变体、Howard barrel、Sentry damaged、rusted Howard/base/carriage 等。
- 新增 `ObjBombModels`：
  - 地雷/炸弹模型：`ap_mine`、`marelet`、`mine_fat`、`naval_mine`、`fat_man`、`fleija`、`gadget`、`ivymike`、`tsar`、`ufp`、`n2`、`fstbmb`、`dud_balefire`、`dud_conventional`、`dud_nuke`、`dud_salted`
  - 贴图常量：AP mine 草地/沙漠/雪地/石头、shrapnel、marelet、fat/naval 等。
- 新增 `ObjProjectileModels`：
  - 模型：`projectiles`、`leadburster`
  - 贴图常量：HIMARS standard/single 及 HE/lava/mini_nuke/TB/WP 变体，bullet/rocket/grenade/mini_nuke/leadburster 等。
- `ObjModelLibrary` 暴露 `TURRET_*`、`BOMB_*`、`PROJECTILES` facade。
- 拷贝资源到现代路径：
  - `assets/hbm/models/block/turrets/`
  - `assets/hbm/models/block/bombs/`
  - `assets/hbm/models/block/projectiles/`
  - `assets/hbm/textures/block/turrets/`
  - `assets/hbm/textures/block/bombs/`
  - `assets/hbm/textures/block/projectiles/`

已确认的重要旧 group：

- `turret_chekhov`：`Base` / `Connectors` / `Carriage` / `Body` / `Barrels`
- `turret_howard`：`Carriage` / `Body` / `BarrelsTop` / `BarrelsBottom`
- `turret_himars`：`Base` / `Carriage` / `Crane` / `Launcher` / `TubeStandard` / `RocketStandard` / `CapStandard1..6` / `TubeSingle` / `RocketSingle` / `CapSingle`
- `turret_sentry`：`Base` / `Pivot` / `Body` / `Drum` / `BarrelL` / `BarrelR`
- `turret_arty`：`Base` / `Carriage` / `Cannon` / `Barrel`
- `projectiles`：`BulletPistol` / `BulletRifle` / `Buckshot` / `Flechette` / `Grenade` / `Rocket` / `MissileMIRV` / `MiniNuke` / `MiniMIRV` / `Balefire`
- `leadburster`：`Based` / `Based.001` / `Backlight`

仍未完全等价：

- 炮塔 renderer 需要后续结合各 `TileEntityTurret*` 的 yaw/pitch/spin/ammo 字段迁移；本轮只提供模型和贴图句柄。
- `RenderLandmine` 的 biome/遮挡贴图选择尚未接现代方块；本轮只保留 AP mine 多贴图常量。
- `RenderArtilleryRocket` 和 Sedna/旧 bullet renderer 的投射物实体渲染还未接入。

## 2026-05-22 Radiation Fog Legacy Particle RenderType

触发来源：

- 继续对齐旧 `ParticleRadiationFog#renderParticle` 的 GL 状态。
- 1.20.1 vanilla `ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT` 在 begin 阶段会 `depthMask(true)`，而旧版黄雾明确 `GL11.glDepthMask(false)`。

本轮现代修正：

- `RadiationFogParticle` 新增专用 `ParticleRenderType`：
  - 使用粒子图集和 `GameRenderer::getParticleShader`。
  - 使用普通 alpha blend，对齐旧 `OpenGlHelper.glBlendFunc(770, 771, 1, 0)`。
  - begin 时 `RenderSystem.depthMask(false)`，end 后恢复 `depthMask(true)`。
- 该 RenderType 只用于辐射黄雾，不改变其他烟雾/火焰/泡沫粒子的渲染状态。

## 2026-05-23 BlockNTMGlassCT / Boron Glass 追踪

触发来源：

- `runData` 生成配方时在 `HbmRecipeProvider.fluidContainerRecipes(...)` 命中 `block("glass_boron")`，但现代注册表尚无真实 `glass_boron` 方块。
- 1.7.10 `ModBlocks` 中 `glass_boron = new BlockNTMGlassCT(0, RefStrings.MODID + ":glass_boron", Material.glass).setBlockName("glass_boron").setStepSound(Block.soundTypeGlass).setCreativeTab(MainRegistry.machineTab).setHardness(0.3F)`。

旧版契约：

- 旧类：`blocks/generic/BlockNTMGlass` 与 `BlockNTMGlassCT`。
- 行为：非普通整块渲染、透明/不遮挡，普通破坏不掉落，丝触可采。
- 渲染：`BlockNTMGlassCT#getRenderType()` 使用 `render/block/ct/CT.renderID`，通过 `IBlockCT.primeReceiver(...)` 和 `glass_boron_ct.png` 接入 connected texture。
- 集成：矿辞 `blockGlass`，机器创造栏，配方中作为喷散罐、移液管、化学套装、RBMK 玻璃盖等硼玻璃输入。

现代本轮实现：

- 新增 `LegacyNtmGlassBlock` 承接透明、不遮挡、同类相邻面剔除与 skylight 行为。
- 注册 `glass_boron` 到机器创造栏额外列表；该列表不再自动继承机器金属块的铁镐/掉落规则。
- 数据生成使用 `minecraft:translucent` cube model；loot 使用丝触限定掉落，普通破坏无掉落。
- 对齐旧 `OreDictionary.registerOre("blockGlass", glass_boron)`，现代端加入 `forge:glass` block/item tag。

待补：

- CT 子体系仍未完整迁移；当前 `glass_boron` 使用基础透明立方模型，不复刻旧 `glass_boron_ct.png` 的连接纹理拼接。
- 后续迁移 `render/block/ct` 时应把 `BlockNTMGlassCT` 家族统一接入，而不是给每种玻璃单独写 renderer。

仍未完全等价：

- 仍未复刻旧 `GL12.GL_RESCALE_NORMAL`、`RenderHelper.disableStandardItemLighting()` 等固定管线调用；现代粒子管线中 fullbright 已承接亮度语义。
- 若后续实机仍显示被地形/水体遮挡，需要继续检查粒子排序和 texture atlas alpha。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-22 Particle Accelerator / Reactor / RBMK OBJ Batch

触发来源：

- 1.7.10 `ResourceManager` 中 Albion 粒子加速器、反应堆、ZIRNOX 和 RBMK 控件仍是静态 OBJ 模型字段。
- 这些资源被后续 `Render*`、核设施方块实体、RBMK 控制台/吊车/燃料棒渲染反复使用；本轮先迁 render-library 资源入口，不提前迁核反应堆或 RBMK 行为。

本轮资源/入口：

- 新增 `ObjParticleAcceleratorModels`：
  - `source`、`beamline`、`rfc`、`quadrupole`、`dipole`、`detector`
  - 资源路径：`assets/hbm/models/block/particleaccelerator/` 与 `textures/block/particleaccelerator/`
- 新增 `ObjReactorModels`：
  - `reactor_small_base`、`reactor_small_rods`、`breeder`、`icf`、`watz`、`watz_pump`、`lpw2`、`zirnox`、`zirnox_destroyed`
  - 额外贴图常量：`lpw2_term`、`lpw2_term_error`、`zirnox_deb_element`
  - 资源路径：`assets/hbm/models/block/reactors/` 与 `textures/block/reactors/`
- 新增 `ObjRbmkModels`：
  - `rbmk_element`、`rbmk_element_rods`、`rbmk_rods`、`crane_console`、`crane`、`autoloader`、`rbmk_console`、`button`、`gauge`、`numitron`、`lever`、`indicator`、`terminal`、`debris`
  - 额外贴图常量：`numitron_lights`
  - 资源路径：`assets/hbm/models/block/rbmk/` 与 `textures/block/rbmk/`
- `ObjMachineModels` 同步补 `ResourceManager` 同段低耦合机器入口：
  - `radiolysis`、`rotary_furnace`、`electrolyser`、`charger`、`refueler`、`solar_boiler`
  - `core_emitter`、`core_receiver`、`core_injector`
- `ObjModelLibrary` 暴露 PA / REACTOR / RBMK / DFC facade。

已确认的重要旧 group：

- `particleaccelerator/beamline`：`BeamlineGlass` / `BeamlineWindow` / `Beamline`
- `rbmk_element`：`Inner` / `Rods` / `Cap`
- `rbmk_rods`：`Lid` / `Column`
- `rbmk_crane`：`Carriage` / `Girder` / `Tube` / `Lift` / `Main`
- `rbmk_crane_console`：`Console_Coonsole` / `Joystick` / `Meter1` / `Meter2` / `Lamp1` / `Lamp2` / `MiniNuke` / `Shotgun` / `Shotgun1`
- `rbmk_autoloader`：`Base` / `Piston`
- `zirnox` 与 `zirnox_destroyed`：`Plane`

仍未完全等价：

- RBMK 旧代码存在非 VBO 和 VBO 双份模型加载：`rbmk_element`、`rbmk_element_rods`、`rbmk_rods`。现代端目前统一走 `LegacyWavefrontModel` 直渲染，尚未实现旧 VBO 缓存或 tessellation 分支。
- RBMK 燃料棒/控制棒液位、ZIRNOX 碎片 projectile、PA 多方块形态、ICF/Watz/LPW2 状态动画还未接入；本轮只提供模型与贴图句柄。

## 2026-05-22 Legacy OBJ API Parity Pass

触发来源：

- 后续机器 renderer 会大量复用旧 `HFRWavefrontObject` 风格调用：
  - `renderPart(...)`
  - `renderOnly(...)`
  - `renderAllExcept(...)`
  - `getPartNames()`
- 现代端虽然已有 `LegacyWavefrontModel` 直渲染桥，但 `ObjRenderContext` 路径此前未完整承接颜色、fullbright 和 legacy shadow 语义；baked split model 与 legacy OBJ 直渲染路径的接口也不完全一致。

本轮现代修正：

- `LegacyObjModel` 接口补齐默认能力：
  - `renderOnlyInCallOrder(...)`
  - `hasPart(...)`
  - `getAliases()`
  - `getLegacyOrder()`
- `LegacyWavefrontModel`：
  - 增加 `modelLocation()` / `textureLocation()`，方便 renderer 或诊断工具追踪资源来源。
  - `renderPart/renderAll/renderOnly/renderAllExcept` 的 `ObjRenderContext` 路径现在会读取 `context.color()`、`context.hasColor()`、`context.packedLight()`、`context.legacyShadow()`。
  - `renderOnlyInCallOrder(...)` 可按调用参数顺序绘制 group，用于旧 renderer 依赖特定 part 顺序的场景。
  - 增加 `hasPart(...)`，直接查询实际 OBJ group。
  - 缺失 group 时按模型/part 限频 warn，并输出已知 group 列表，方便后续迁移 renderer 时快速发现 1.7.10 group 名拼写差异。
  - legacy shadow 现在可按 OBJ face normal 估算亮度，向 `ObjRenderUtils` 的 baked quad 阴影语义靠拢。

仍未完全等价：

- 旧 `HFRWavefrontObject` 的 `tessellateAll/tessellateOnly/tessellateAllExcept` 系列仍未迁移。
- 旧 VBO 缓存路径仍未迁移，现代 `LegacyWavefrontModel` 继续每帧向 `MultiBufferSource` 直接提交顶点。
  - 旧 `noSmooth()` 的法线/平滑差异已在 2026-05-23 对齐到 `LegacyWavefrontModel.noSmooth()`；split JSON / baked model 路径仍需逐项确认。

## 2026-05-23 Render Library 1.7.10 Alignment Audit

触发来源：

- 用户要求对当前已迁渲染库按 1.7.10 代码进行大排查，目标是对齐旧实现本身，而不是只模仿旧效果。
- 本轮只处理可由 1.7.10 `ResourceManager`、`HFRWavefrontObject`、`HFRWavefrontObjectVBO`、`S_Face` 明确证明的偏差；目录整理类差异单独记录，不误判为行为错误。

已核准的旧语义：

- `HFRWavefrontObject.renderOnly(...)` / `renderPart(...)` / `renderAllExcept(...)` 均按 `groupObjects` 顺序扫描，名称匹配为 `equalsIgnoreCase`。
- 同名 OBJ group 在旧实现中不会被 name map 合并；只要名称匹配，所有同名 group 都会绘制。
- `HFRWavefrontObject.noSmooth()` 令后续解析出来的 `S_Face` 使用面法线，不使用 OBJ 顶点法线；资源 reload 会重新按当前 `smoothing` 状态解析。
- `HFRWavefrontObjectVBO` 也按 group 列表顺序匹配 `renderOnly(...)`，但其 VBO 数据在旧代码中固定按三角形数组提交。
- RBMK 手动控制棒旧枚举顺序为 `RED, YELLOW, GREEN, BLUE, PURPLE`，现代 `manualControlTexture(int ordinal)` 映射与此一致。

本轮现代修正：

- `LegacyWavefrontModel.renderOnlyInCallOrder(...)` 改为按传入名称扫描完整 `groupOrder`，不再通过 `groups.get(key)` 丢失同名 group。
- `LegacyWavefrontModel` 新增链式 `noSmooth()`，渲染顶点时在该模式下强制使用 `faceNormal`，对齐旧 `S_Face.addFaceForRender(...)` 的非平滑路径。
- 已把旧 `ResourceManager` 明确 `.noSmooth()`、且现代端当前是 `LegacyWavefrontModel` 的入口接上：
  - `ObjMachineModels.ARC_WELDER`
  - `ObjMachineModels.SOLDERING_STATION`
  - `ObjMachineModels.AUTOSAW`
  - `ObjRbmkModels.ELEMENT`
  - `ObjRbmkModels.ELEMENT_RODS`
  - `ObjRbmkModels.RODS`
  - `ObjRbmkModels.DEBRIS`

审计结论：

- 现代 `LegacyWavefrontModel` 的 group 匹配、大小写规则、同名 group 处理、`renderAllExcept(...)` 基本对齐旧 HFR OBJ 路径。
- 现代资源目录中存在主动整理：例如旧 `lpw2` / `watz_pump` 模型和 `watz` / `lpw2` 贴图在 `ResourceManager` 中属于 `models/machines`、`textures/models/machines`，现代端集中放在 `models/block/reactors`、`textures/block/reactors`。只要资源内容来自 1.7.10，这属于现代资源树归档差异，不是新增内容；后续 renderer 文档必须记录旧字段名和旧路径，避免把现代目录当成旧事实源。
- `rbmk_element` 旧字段为 mixed-mode 非 VBO 模型，旧直接 `renderAll/renderPart` 会因 `allowMixedMode` 抛错，实际用途依赖 tessellation/ISBRH 路径。现代端统一直渲染该模型是桥接实现差异，后续迁 RBMK 方块渲染时必须按旧调用点决定是否补 tessellation API。

仍未完全等价：

- `tessellateAll/tessellateOnly/tessellatePart/tessellateAllExcept` 仍未迁移；RBMK mixed-mode 和旧 ISBRH 路径会依赖它们。
- 旧 VBO 缓存生命周期和 `HFRModelReloader` 的 VBO 重建路径仍未迁移；现代 `LegacyWavefrontModel` 当前每帧提交顶点。
- split JSON / `ObjModelPart` 路径无法自动继承旧 `.noSmooth()` 语义；已知旧 noSmooth 资源如 firebox、heating_oven、electric_heater、rtg、radar、network connector/pylon、soyuz launch table、灯具、装饰方块等需要逐项核对 baked model 法线或改走 legacy OBJ 载体。
- 旧 OBJ 解析只接受正索引和 3/4 顶点 face；现代解析器支持负索引和 n-gon 三角化。当前现代资源来自旧资产，风险较低，但若后续新资源进入渲染库，应按 1.7.10 兼容范围做校验。

下一步计划：

- 优先补 `LegacyWavefrontModel` 的 tessellation 风格 API，供 RBMK element、旧 ISBRH 和需要逐 face/icon 替换的 renderer 使用。
- 对 `ResourceManager` 中所有 `.noSmooth()` 资源生成现代入口对照表，区分 `LegacyWavefrontModel`、split JSON、尚未迁入三类；对 split JSON 类决定是补 model JSON 法线配置还是回退到 legacy OBJ 载体。
- 继续按旧 `ResourceManager` 字段名核查现代 `Obj*Models` 常量，记录“旧字段名/旧路径/现代路径/默认贴图/已迁状态”，避免出现 1.7.10 不存在的常量或贴图。
- 迁 RBMK renderer 前，先核 `RenderRBMK*`、`ObjUtil.renderPartWithIcon` 和旧 block renderer 对 `rbmk_element` 的真实调用顺序。

## 2026-05-23 Legacy OBJ Parity API补齐

- 本轮按 1.7.10 `HFRWavefrontObject` / `HFRWavefrontObjectVBO` 的调用面补齐现代端老 API 门面：
  - `LegacyObjModel` 增加 `mixedMode()` / `asVBO()` 默认入口。
  - `LegacyWavefrontModel` 增加 `mixedMode()`、`asVBO()`，以及 `tessellateAll/tessellateOnly/tessellatePart/tessellateAllExcept` 兼容方法。
- 语义说明：
  - `mixedMode()` 目前只作为契约标记保留，供后续需要混合三角/四边形的 renderer 识别。
  - `asVBO()` 先保留与旧端同名调用点，现代端仍由直渲路径承接，不额外引入旧 GL15 VBO 生命周期。
- 已对齐的旧入口：
  - `ObjRbmkModels.ELEMENT` 标记为 mixed-mode，对齐 1.7.10 `rbmk_element` 的构造参数。
- 后续注意：
  - 真正需要旧 VBO 生命周期的 renderer 仍要按 1.7.10 调用点继续核查，不能把 `asVBO()` 当成完整 VBO 移植完成。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
