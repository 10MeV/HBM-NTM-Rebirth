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
- 2026-05-24 追加：
  - 用户反馈 32 区块能见度下，256 格外的大型机器仍会出现“区块看得见、模型缺席”的观感断层。
  - `LegacyBlockEntityRenderDistances.MACHINE` 改为 `32 * 16 = 512`，对齐 Minecraft 最大 32 区块可视距离。
  - 这超过 1.7.10 的 `65536.0D` 原始距离，但保留在同一个渲染库常量内，后续若需要配置化或分机器降级，可集中调整。
  - 性能风险：更远处的机器 BER 会进入候选渲染，密集机器群、高空俯视、动画 OBJ 与透明分件会增加 CPU 提交和 GPU 绘制压力；区块未加载、AABB/视锥裁剪仍会限制实际渲染数量。
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
- 2026-05-24 运行时修正：`textures/block/horse` 曾误落位为单个 `sunburst.png` 文件，导致 renderer 查找 `textures/block/horse/sunburst.png` 失败；已整理为目录并补齐旧 `horse_demo.png`、`sunburst.png`、`dyx.png`、`numbernine.png`。

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
- `fluidtank.obj` 初迁时保留旧 `mtllib/usemtl` 行；2026-05-24 二次筛查已移除无效 `mtllib`，现代 `LegacyWavefrontModel` 直渲染路径仍按调用纹理统一绘制，不解析 MTL 材质。

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

### 2026-06-03 追加：ILookOverlay 现代 API 化

问题修正：

- 上一轮初版 `LegacyLookOverlayRenderer` 仍直接识别若干具体 `BlockEntity` 类型，虽然能显示信息，但不符合 1.7.10 `ILookOverlay` “方块/物品/实体自行提供信息，渲染库只统一绘制”的库边界。

本轮现代 API：

- 新增 common API，不依赖 client 包：
  - `com.hbm.ntm.api.block.LegacyLookOverlay`
  - `LegacyLookOverlayProvider`
  - `LegacyLookOverlayLines`
  - `LegacyLookOverlayPort`
  - `LegacyLookOverlayPorts`
- `LegacyLookOverlayRenderer` 现在只做三件事：
  - 在 CROSSHAIR overlay 时获取命中方块。
  - 通过 `MultiblockHelper.resolveCoreBlockEntity(...)` 解析 dummy/proxy 到核心方块实体。
  - 若核心实现 `LegacyLookOverlayProvider`，绘制其返回的 `LegacyLookOverlay`。
- 机器/网络方块实体只实现 common API：
  - 普通流体机器继承 `HbmFluidBlockEntity` 后自动获得 `getLookOverlay(...)`，按 `HbmFluidUser` / `HbmStandardFluidReceiver` / `HbmStandardFluidSender` 生成 tank 行。
  - 有额外状态的机器覆盖 `getLookOverlay(...)` 添加热量、警告、能量/spin 等行。
  - 有端口说明的机器通过 `LegacyLookOverlayPort` 声明端口坐标、朝向和文本；渲染器不关心具体机器。

新增 1.7.10 对齐覆盖：

- `FluidDuctStandard#printHook`：
  - 旧版显示当前管道流体名，并用流体颜色染色。
  - 现代 `FluidPipeBlockEntity` 实现 `LegacyLookOverlayProvider`，显示 `getFluidType()` 名称和颜色。
- `FluidDuctGauge#printHook`：
  - 旧版显示当前流体名、`deltaTick mB/t`、`deltaLastSecond mB/s`。
  - 现代 `FluidDuctGaugeBlockEntity` 覆盖 `getLookOverlay(...)` 显示同三行。
- `FluidPump#printHook`：
  - 旧版显示 `-> fluid (pressure PU): bufferSize mB/t ->`、`Priority: ...`、可选 `...mB buffered`。
  - 现代 `FluidPumpBlockEntity` 覆盖 `getLookOverlay(...)` 使用 `LegacyLookOverlayLines.pumpLine/priority/buffered` 生成同类信息。

迁移规则更新：

- 后续不要在 `LegacyLookOverlayRenderer` 中继续 `instanceof` 某台机器。
- 需要准星信息的机器应实现/继承 `LegacyLookOverlayProvider`，并尽量复用：
  - `LegacyLookOverlayLines`：tank、流体名、速率、热量、能量、优先级等文本格式。
  - `LegacyLookOverlayPorts` / `LegacyLookOverlayPort`：端口命中匹配。
  - `HbmFluidBlockEntity` 默认 tank overlay。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-31 ResourceManager VBO 契约补漏：机器、核弹/地雷、Soyuz

1.7.10 对照：

- `ResourceManager` lines 110、303、310：`turbine`、`solar_boiler`、`cargo_elevator` 都是 `HFRWavefrontObject(...).asVBO()`；现代库已有 baked/part 入口，但缺少旧式 facade 或 `SOLAR_BOILER` 未标 VBO。
- `ResourceManager` lines 61-64：`mine_ap`、`mine_marelet`、`mine_naval` 为 `.asVBO()`，`mine_fat` 是 `AdvancedModelLoader.loadModel(...)`，所以本轮保持 `mine_fat` 非 VBO。
- `ResourceManager` lines 279-293：`bomb_gadget`、`bomb_man`、`bomb_tsar`、`bomb_prototype`、`bomb_fleija`、`bomb_solinium`、`n2`、`fstbmb`、四个 dud bomb 为 `.asVBO()`；`bomb_boy` 是 `AdvancedModelLoader`，`bomb_mike` 是普通 `HFRWavefrontObject` 无 `.asVBO()`，所以现代 `BOY/MIKE` 仍保持非 VBO。
- `ResourceManager` lines 1229-1231：`soyuz`、`soyuz_lander`、`soyuz_module` 都是 `.asVBO()`；发射架六个 `soyuz_launcher_*` 的 `.noSmooth().asVBO()` 已在前序批次对齐。

现代侧改动：

- `ObjMachineModels` 增加 `TURBINE_LEGACY`、`ELEVATOR_LEGACY`，并把 `SOLAR_BOILER` 改为 `.asVBO()`；`ObjModelLibrary` 暴露对应 `MACHINE_*` facade。
- `ObjBombModels` 为 1.7.10 明确 `.asVBO()` 的地雷、核弹/哑弹块模型补 `.asVBO()`，同时保留 `MINE_FAT`、`IVYMIKE` 的旧端非 VBO 语义。
- `ObjNukeModels` 为世界核弹 renderer 使用的 `GADGET/MAN/TSAR/PROTOTYPE/FLEIJA/SOLINIUM/N2` 补 `.asVBO()`，保留 `BOY/MIKE` 非 VBO，避免把 1.7.10 不存在的 VBO 契约强行套上去。
- `ObjSoyuzModels` 为 `SOYUZ/LANDER/MODULE` 补 `.asVBO()`，不改分 part 渲染和贴图选择。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 准星信息 overlay 实体分支与文本 helper

1.7.10 对照：

- `ModEventHandlerClient#onOverlayRender` 的实体分支只在 `mop.typeOfHit == ENTITY` 时执行，并且只检查 `entity instanceof ILookOverlay`；手持物品优先级只存在于 block hit 分支。
- 已确认旧端实体侧主要承载点之一是 `EntityRailCarBase implements ILookOverlay`，其 `BoundingBoxDummyEntity implements ILookOverlay` 会把 overlay 委托给关联的 rail car；现代对应火车实体库尚未迁移，本轮只先补共享 API 入口。
- 旧端常见文本 helper 形态包括：`BobMathUtil.getShortNumber(...) + unit`、`Max.: ...HE/t`、`Priority: ...`、`Freq: ...`、`Signal: ...`、物品栈 `-> item xN` / `<- item xN`、`! ! ! WARNING ! ! !`，以及 `BobMathUtil.getBlink()` 的 1000ms 红/黄闪烁。

现代侧改动：

- 新增 `LegacyLookOverlayEntityProvider`，现代实体可通过同一准星 overlay API 提供文字。
- `LegacyLookOverlayRenderer` 增加实体命中分支：block hit 时按手持物品 -> 方块 -> core BlockEntity；entity hit 时只查实体 provider，贴合 1.7.10 顺序。
- `LegacyLookOverlayLines` 补齐可复用行 helper：`shortRate(...)`、`maxRate(...)`、`freq(...)`、`signal(...)`、`itemStack(...)`、`warning(...)`、`blinkingWarning(...)`、`error(...)`、`percent(...)`、`percentColor(...)`。
- `blinkingWarning(...)` 使用 `System.currentTimeMillis() % 1000 < 500` 复刻旧 `BobMathUtil.getBlink()` 节奏。

边界：

- 本轮不迁火车实体本体、不新增 `BlockRemap` 注册、不为未迁现代承载点生造 overlay 行为；这里只补库入口与通用文本 API。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 准星信息 overlay 勘误与收口

1.7.10 对照：

- 准星文字由 `com.hbm.blocks.ILookOverlay` 提供，`ModEventHandlerClient#onOverlayRender` 在 `ElementType.CROSSHAIRS && ClientConfig.DODD_RBMK_DIAGNOSTIC` 下调用。
- 旧版不是所有流体机器都会显示 tank 信息：
  - `MachineSteamEngine`：显示 steam input 与 spent steam output 两行，不显示功率。
  - `MachineTowerSmall` / `MachineTowerLarge`：显示全部 tanks，第 0 个为输入，其余为输出。
  - `MachineFractionTower`：显示全部 tanks，第 0 个为输入，其余为输出。
  - `MachineCatalyticCracker`：显示全部 tanks，前两个为输入，其余为输出。
  - `MachineFluidTank` 与 `MachineRefinery` 的 `printHook` 只调用 `IRepairable.addGenericOverlay(...)`；只有玩家手持喷灯、核心实现 `IRepairable` 且损坏时才显示维修材料，不是常驻 tank overlay。
  - `MachineBigAssTank` / `MachineBigAssTank9000` 没有实现 `ILookOverlay`。
  - `MachineCompressor`、`MachineCompressorCompact`、`MachineLiquefactor`、`MachineGasFlare`、`MachineCoker`、`MachineHydrotreater`、`MachineCatalyticReformer`、`MachineVacuumDistill` 也没有旧版准星 overlay；这些机器的状态主要通过 GUI、持久 NBT tooltip 或其它信息接口体现。
- 因此现代共享实现不能把所有 `HbmFluidBlockEntity` 默认映射成 tank 准星文字，否则会产生猜测内容。

现代侧修正：

- `HbmFluidBlockEntity#getLookOverlay(...)` 改为默认不显示，只有覆写或显式打开的机器提供准星信息。
- `LegacyLookOverlayLines` 增加 `allFluidUserTanks(...)`，供旧版明确“无论 empty/NONE 都按 tank 数组列出”的机器复用。
- 明确接回旧版有 tank 准星 overlay 的机器：
  - `SteamEngineBlockEntity`：仅显示 steam/spent steam 两个 tank。
  - `CoolingTowerBlockEntity`：显示全部 tank。
  - `FractionTowerBlockEntity`：显示全部 tank。
  - `CatalyticCrackerBlockEntity`：显示全部 tank。
- `machine_fluidtank`、`machine_bigasstank`、`compressor`、`liquefactor`、`gas_flare`、`coker`、`hydrotreater`、`catalytic_reformer`、`vacuum_distill` 等现代 BE 不再因继承流体基类而自动显示未对齐的 tank 准星文字。
- 后续若迁移喷灯/维修材料 API，应按 `IRepairable.addGenericOverlay(...)` 的旧契约实现“手持喷灯 + 已损坏”条件，而不是恢复常驻 tank overlay。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 准星 overlay API 扩展到网络与能量存储

1.7.10 对照：

- `ILookOverlay` 仍是准星信息事实入口，`ModEventHandlerClient#onOverlayRender` 命中方块后调用目标 block 的 `printHook(...)`。
- `FluidValve` / `FluidSwitch` / `FluidDuctBox` / `FluidDuctPaintable` / `FluidPipeAnchor` 均只显示当前管道流体名。
- `FluidCounterValve#printHook` 在流体名下追加 `Counter: <value>`。
- `FluidDuctBoxExhaust` 与 `FluidDuctPaintableBlockExhaust` 固定列出 `SMOKE`、`SMOKE_LEADED`、`SMOKE_POISON` 三种可连接流体。
- `MachineBattery#printHook` 显示 `power / maxPower HE` 与红-绿渐变百分比。
- `MachineBatterySocket#printHook` 只有插入电池包时显示，标题为插入物品显示名，正文为该电池包当前能量与百分比。

现代侧：

- `LegacyLookOverlayLines` 增加 `counter(...)`、`energyStored(...)`、`chargePercent(...)`、`energyStorage(...)`、`fluidNames(...)`，让通用准星文本由 API 统一生成。
- `LegacyLookOverlay.withTitle(...)` 支持标题不是方块名的旧行为，例如电池接口显示插入电池包名称。
- `FluidCounterValveBlockEntity`、`FluidDuctExhaustBlockEntity`、`FluidDuctPaintableExhaustBlockEntity`、`MachineBatteryBlockEntity`、`MachineBatterySocketBlockEntity` 接入 `LegacyLookOverlayProvider`。
- `HbmEnergyBlockEntity` 增加标准 BE update packet/tag，使能量型准星 overlay 能读取客户端侧动态能量。

边界：

- 本轮没有迁移尚未注册现代 BE 的旧能量转换器、FENSU 独立方块或 cable gauge；只记录 1.7.10 行格式，后续等对应机器/网络块迁移时接入同一 API。
- `LegacyLookOverlayRenderer` 仍只负责命中解析、core BE 解析和绘制，不含具体机器逻辑。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 ILookOverlay 准星诊断面板迁移

触发来源：

- 实机/迁移反馈：准星指向机器或机器接口时，旧版会显示水、蒸汽输入口、配方接口等信息；现代端缺少这类看向方块的提示。

1.7.10 对照：

- 该功能不属于 Waila，而属于 HBM 自有 `com.hbm.blocks.ILookOverlay`。
- 调用入口在 `com.hbm.main.ModEventHandlerClient#onOverlayRender(RenderGameOverlayEvent.Pre)`：
  - 仅在 `ElementType.CROSSHAIRS` 且 `ClientConfig.DODD_RBMK_DIAGNOSTIC` 为 true 时执行。
  - 若手持物品实现 `ILookOverlay`，优先调用物品 `printHook`。
  - 否则命中方块实现 `ILookOverlay` 时调用方块 `printHook`。
  - 命中实体实现 `ILookOverlay` 时调用实体 `printHook`。
- `ILookOverlay#printGeneric(...)` 的绘制位置固定为屏幕中心右侧：
  - `x = scaledWidth / 2 + 8`
  - `y = scaledHeight / 2`
  - 标题先以 `bgCol` 在 `x+1,y-9` 画阴影，再以 `titleCol` 在 `x,y-10` 画正文。
  - 每行正文 `drawStringWithShadow`，默认白色；以 `&[color&]` 开头的行可覆盖颜色。
- 与此不同，`com.hbm.render.util.RenderInfoSystem` 是短暂提示消息队列：
  - 由 `ClientProxy.theInfoSystem` 注册为客户端事件。
  - `PlayerInformPacket` / `MainRegistry.proxy.displayTooltip(...)` 可推入消息。
  - 现代端已有 `ClientInformMessages` 承担这类短暂提示；本轮不重复迁移。

本轮现代侧改动：

- 新增 `com.hbm.ntm.client.overlay.LegacyLookOverlayProvider`，作为现代端方块实体/机器提供准星诊断数据的接口。
- 新增 `LegacyLookOverlayRenderer`：
  - 挂到 `ClientForgeEvents` 的 `RenderGuiOverlayEvent.Post` / `VanillaGuiOverlay.CROSSHAIR`。
  - 使用 `Minecraft.hitResult` 获取命中方块。
  - 通过 `MultiblockHelper.resolveCoreBlockEntity(...)` 对齐旧 `BlockDummyable#findCore(...)` 行为，使 dummy/proxy/核心都能显示核心方块实体信息。
  - 复刻旧版准星右侧绘制位置、标题颜色、标题阴影与正文阴影。
- 已接入的旧版事实源机器：
  - `SteamEngineBlockEntity`：显示蒸汽输入 tank 与低压蒸汽输出 tank。
  - `BoilerBlockEntity`：显示 TU 热量、进料 tank 与蒸汽输出 tank。
  - `SolarBoilerBlockEntity`：显示水输入、蒸汽输出，并在 `display < 1` 时显示 `Too cold!` 闪烁警告。
  - `IndustrialSteamTurbineBlockEntity`：显示输入/输出 tank 与输出 HE/spin 信息。
  - `LegacySteamTurbineBlockEntity`：显示输入/输出 tank 与最近发电量。
- `AssemblyMachineBlockEntity` 与 `ChemicalPlantBlockEntity` 接入旧端口规则：
  - 旧版 `MachineAssemblyFactory` / `MachineChemicalFactory` 只在 `getCoolPos()` 和 `getIOPos()` 对应接口显示提示。
  - 现代端按 1.7.10 `DirPos.compare(x + dir.offsetX, y, z + dir.offsetZ)` 的命中偏移逻辑判断。
  - 冷却接口显示 `Water` 输入与 `Spent Steam` 输出；配方接口显示 `Recipe field [n]`。

勘误/限制：

- 现代 `AssemblyMachineBlockEntity` / `ChemicalPlantBlockEntity` 当前尚未迁入 1.7.10 的专用冷却 `water` / `lps` tank，只迁了配方流体 tank；因此本轮端口 overlay 只显示端口流体类型，不显示冷却 tank 容量，避免伪造不存在的数据。
- 持有物品和实体实现 `ILookOverlay` 的路径已经记录，但本轮只迁机器/方块实体准星信息；后续结构工具、drone linker、铁路实体等应在对应物品/实体迁移时接入同一接口。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-02 追加：导弹本体 OBJ 与贴图入口批量接入

1.7.10 对照：

- `ResourceManager` lines 1220-1226 声明导弹本体模型：
  - `missileV2`、`missileABM`、`missileStrong`、`missileHuge`、`missileNuclear`、`missileMicro` 全部为 `HFRWavefrontObject(...).asVBO()`。
  - `missileStealth` 为 `HFRWavefrontObject(...).noSmooth().asVBO()`，该 VBO 契约已在前一轮补齐。
- `ResourceManager` lines 1349-1375 声明导弹贴图变体：
  - V2：HE/IN/CL/BU/decoy。
  - ABM：`missile_abm`。
  - Stealth：`missile_stealth`。
  - Strong：HE/EMP/IN/CL/BU。
  - Huge：HE/IN/CL/BU。
  - Atlas：nuclear/thermo/tectonic/doomsday/doomsday weathered。
  - Micro：normal/taint/black hole/schrab/EMP/test。
- 旧 `RenderMissileGeneric`、`RenderMissileStrong`、`RenderMissileHuge`、`RenderMissileNuclear`、`RenderMissileTaint` 和 `ItemRenderMissileGeneric` 都通过这些模型与贴图组合渲染实体/物品；本轮只迁资源库入口，不迁 renderer 逻辑。

现代侧改动：

- 从 1.7.10 原资源复制 `missile_v2.obj`、`missile_abm.obj`、`missile_strong.obj`、`missile_huge.obj`、`missile_atlas.obj`、`missile_micro.obj` 到 `assets/hbm/models/block/missiles/`。
- 从 1.7.10 原资源复制上述 26 张导弹贴图到 `assets/hbm/textures/block/missiles/`，保留旧文件名。
- `ObjMissilePartModels` 增加 `MISSILE_V2`、`MISSILE_ABM`、`MISSILE_STRONG`、`MISSILE_HUGE`、`MISSILE_ATLAS`、`MISSILE_MICRO`，均按旧端 `.asVBO()`；`MISSILE_ATLAS` 默认贴图使用旧 `missile_atlas_nuclear`。
- `ObjMissilePartModels` 补齐导弹本体贴图 `ResourceLocation` 常量，供后续导弹实体/物品 renderer 迁移复用。
- `ObjModelLibrary` 暴露六个新增导弹本体 facade。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-02 ResourceManager VBO 契约补漏：武器、导弹、弹壳与 C130

1.7.10 对照：

- `ResourceManager` line 887：`chainsaw = new HFRWavefrontObject(...).noSmooth().asVBO()`；现代 `ObjWeaponModels.CHAINSAW` 原本只有 `.noSmooth()`。
- `ResourceManager` line 1222：`missileStealth = new HFRWavefrontObject(...).noSmooth().asVBO()`；现代 `ObjMissilePartModels.MISSILE_STEALTH` 原本只有 `.noSmooth()`。
- `ResourceManager` lines 1212、1336：`casings = new HFRWavefrontObject("models/effect/casings.obj").asVBO()`，贴图为 `textures/particle/casings.png`。旧 `ParticleSpentCasing` 绑定该贴图后按 `config.getType().partNames` 调 `ResourceManager.casings.renderPart(name)` 并逐 part 上色；这是粒子/效果资源，不是普通方块模型。
- `ResourceManager` lines 1217、1346 和 `RenderC130`：`c130 = new HFRWavefrontObject("models/weapons/c130.obj").asVBO()`，贴图为 `textures/models/weapons/c130_0.png`；旧 renderer 渲染 `Plane` 与 `Prop1`-`Prop4`，四个螺旋桨有独立旋转变换。

现代侧改动：

- `ObjWeaponModels.CHAINSAW` 改为 `.noSmooth().asVBO()`；`ObjMissilePartModels.MISSILE_STEALTH` 改为 `.noSmooth().asVBO()`。
- `ObjEffectModels` 新增 `CASINGS_TEXTURE` 与 `CASINGS`，路径保留旧资源实际所在的 `models/effect/casings.obj` 与 `textures/particle/casings.png`，并在 `ObjModelLibrary` 暴露 `EFFECT_CASINGS`。
- 从 1.7.10 原资源复制 `models/weapons/c130.obj` 到现代 `assets/hbm/models/block/entities/c130.obj`，复制 `textures/models/weapons/c130_0.png` 到 `assets/hbm/textures/block/entities/c130_0.png`；`ObjEntityModels` 新增 `C130`/`C130_TEXTURE`，并在 `ObjModelLibrary` 暴露 `ENTITY_C130`。
- 本轮只补渲染库资源入口和旧 VBO 契约；C130 实体注册/renderer、弹壳粒子运动与烟雾行为仍应分别在实体/粒子迁移批次中按旧 `RenderC130`、`ParticleSpentCasing` 继续对齐。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-28 ResourceManager 效果、发射架与反应堆 facade 修正

1.7.10 对照：

- `ResourceManager.sphere_ruv`、`sphere_iuv`、`sphere_uv` 使用 `AdvancedModelLoader.loadModel(...)`，不是 VBO；`sphere_new` 才是 `HFRWavefrontObject(...).asVBO()`。
- `RenderDeathBlast` 直接加载 `models/Sphere.obj`，同样是 `AdvancedModelLoader.loadModel(...)`，不属于 `ResourceManager` VBO 字段。
- `ResourceManager.soyuz_launcher_*` 六个发射架部件均为 `HFRWavefrontObject(...).noSmooth().asVBO()`；现代端此前只保留 `.noSmooth()`。
- `ResourceManager.icf`、`lpw2`、`watz`、`watz_pump` 均为 `.asVBO()`；`reactor_small_base/rods` 是 `AdvancedModelLoader`，`breeder` 是普通 `HFRWavefrontObject`，需要保持非 VBO 语义。
- 车辆 `cart/cart_destroyer/cart_powder/tram/tram_trailer` 均为 `AdvancedModelLoader`，本轮确认现代 `ObjVehicleModels` 不补 `.asVBO()`。

现代侧改动：

- `ObjEffectModels` 去掉 `SPHERE_RUV`、`SPHERE_IUV`、`SPHERE_UV`、`SPHERE` 的误加 `.asVBO()`；保留 `SPHERE_NEW.asVBO()`。
- `ObjLaunchModels` 的 `SOYUZ_LAUNCHER_*_LEGACY` 全部改为 `.noSmooth().asVBO()`。
- `ObjReactorModels.ICF`、`LPW2`、`WATZ`、`WATZ_PUMP` 补 `.asVBO()`；`SMALL_BASE`、`SMALL_RODS`、`BREEDER` 保持旧端非 VBO 语义。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-26 ResourceManager 门面修饰符与门模型追加对齐

1.7.10 对照：

- `ResourceManager.silo_hatch` 与 `ResourceManager.silo_hatch_large` 分别来自 `models/doors/silo_hatch.obj`、`models/doors/silo_hatch_large.obj`，均为 `HFRWavefrontObject(...).asVBO()`；贴图为 `textures/models/doors/silo_hatch.png` 与 `silo_hatch_large.png`。
- `DoorDecl.SILO_HATCH` / `SILO_HATCH_LARGE`、`ItemRenderLibraryDoors` 均按 named part `Frame`、`Hatch` 渲染，现代端已有 baked part 入口，但旧式渲染库 facade 还需要保留整 OBJ 入口。
- `ResourceManager.skeleton_holder` 是 `HFRWavefrontObject(...).noSmooth().asVBO()`；现代端此前只有 `.noSmooth()`。
- `ResourceManager.ufo`、`mini_ufo`、`siege_ufo` 是 `new HFRWavefrontObject(...)`，没有 `.asVBO()`；现代端此前误加 `.asVBO()`。
- `ResourceManager.zirnox`、`zirnox_destroyed` 是 `HFRWavefrontObject(...).asVBO()`；现代端此前未标记 `.asVBO()`。
- `transition_seal` 是 Collada `AnimatedModel` / `Animation`，不属于 OBJ 静态模型库，本轮只记录缺口，不用 OBJ facade 生造替代。

现代侧改动：

- `ObjDoorModels` 增加 `SILO_HATCH_LEGACY`、`SILO_HATCH_LARGE_LEGACY` 与旧贴图句柄，保留已有 baked `ObjModelPart`/`ObjPartModel` 入口；`ObjModelLibrary` 增加对应 `DOOR_*_LEGACY` facade。
- `ObjBlockModels.SKELETON_HOLDER` 调整为 `.noSmooth().asVBO()`。
- `ObjEntityModels.UFO`、`MINI_UFO`、`SIEGE_UFO` 去掉误加的 `.asVBO()`，按旧端普通 `HFRWavefrontObject` 语义暴露。
- `ObjReactorModels.ZIRNOX`、`ZIRNOX_DESTROYED` 补 `.asVBO()`。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-26 ResourceManager 通用块、炮塔与 PheoDoors 接入

1.7.10 对照：

- `ResourceManager.pipe_neo` 来自 `models/blocks/pipe_neo.obj`，构造为 `new HFRWavefrontObject(...)`，没有 `.noSmooth()` / `.asVBO()`；旧 `RenderTestPipe`、`RenderFrackingTower` 会直接渲染 `pX/nX/pZ/nZ` 等 part，贴图来自方块 icon `pipe_neo` 与 overlay。
- `ResourceManager.turret_chekhov`、`turret_jeremy`、`turret_tauon`、`turret_richard`、`turret_howard`、`turret_maxwell`、`turret_fritz`、`turret_arty`、`turret_himars`、`turret_sentry` 与 `turret_howard_damaged` 均为 `HFRWavefrontObject(...).asVBO()`。
- `ResourceManager` lines 429-455 声明炮塔贴图：base/carriage/connector、Chekhov/Jeremy/Tauon/Richard/Howard/Maxwell/Fritz/Arty/HIMARS/Sentry，以及 rusted Howard 变体；旧资源还存在 `base_zach`、`carriage_zach`、`zach` 贴图，供炮塔皮肤继续使用。
- `ResourceManager` lines 323-363 声明 PheoDoors：`pheo_fire_door`、`pheo_airlock_door`、`pheo_blast_door`、`pheo_containment_door`、`pheo_seal_door`、`pheo_secure_door`、`pheo_sliding_door`、`pheo_vehicle_door`、`pheo_water_door`、`pheo_vault_door`，全部为 `HFRWavefrontObject(...).asVBO()`。
- PheoDoors 的贴图选择由 `DoorDecl`、`Render*Door` 与 `ItemRenderLibraryDoors` 控制；例如 Vault Door 不存在单独 `vault_door.png` 默认贴图，而是对 `Door` 和 `Label` 分别绑定 `vault/vault_door_*` 与 `vault/label_*`。

现代侧改动：

- 复制 `pipe_neo.obj` 与 `pipe_neo.png` / `pipe_neo_overlay.png` 到 `legacy_blocks` 目录；`ObjBlockModels.PIPE_NEO` 保留旧端无 noSmooth/无 asVBO 语义，并在 `ObjModelLibrary` 暴露 `BLOCK_PIPE_NEO`。
- `ObjTurretModels` 将所有旧炮塔模型入口补 `.asVBO()`，并公开 Jeremy/Tauon/Richard/Maxwell/Fritz/Arty/HIMARS/Sentry、Zach 相关贴图常量；复制缺失的 `base_zach`、`carriage_zach`、`zach`。
- 新增 `ObjPheoDoorModels`，复制 10 个 PheoDoors OBJ 与 31 张贴图，按旧字段建立 legacy OBJ 门面和贴图句柄；`ObjModelLibrary` 增加 `PHEO_*_DOOR` facade。
- 继续记录缺口：旧 `sat_base/sat_radar/sat_resonator/sat_scanner/sat_mapper/sat_laser/sat_foeq` 在 `ResourceManager` 中有声明，但旧资源目录未找到对应 png；真实调用集中在 `sat_foeq_burning` / `sat_foeq_fire` 与 `satDock`，现代端保持不生造缺失贴图。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-26 追加：手雷 OBJ 部位贴图句柄

1.7.10 对照：

- `ResourceManager.grenades` 使用 `models/weapons/grenades.obj`，该模型已随武器 OBJ 批次接入。
- `ItemRenderGrenade` 按 part 绑定 `grenade_frag_*`、`grenade_stick_*`、`grenade_tech_*`、`grenade_nuka_*` 共 16 张贴图后调用 `ResourceManager.grenades.renderPart(...)`。

现代侧改动：

- 从旧 `textures/models/grenades/` 复制 16 张贴图到 `assets/hbm/textures/block/grenades/`。
- `ObjWeaponModels` 新增 `GRENADE_*_TEXTURE` 常量与 `grenadeTexture(...)` helper，供后续手雷 item renderer 直接按旧部位贴图绑定。
- 同段 `ResourceManager` 中的 `ff_gun_bright/dark/normal` 只声明了 `textures/models/weapons/ff/...`，旧资源目录未找到对应 png，且当前旧源码未找到调用点；本轮不补现代常量。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加更正：核弹单方块契约回退

触发来源：

- 实机继续反馈：新世界仍无法打开核弹 GUI；上一轮把选择/交互形状扩成 OBJ bounds 后，只看到选择框/碰撞箱变成核弹整体，实际模型与 GUI 问题没有解决。
- 用户明确指出 1.7.10 核弹没有多方块 dummy，也不是模型尺寸碰撞箱，旧端是单个 `BlockContainer` + TESR 渲染模型。

1.7.10 对照：

- `NukeMan#onBlockActivated(...)`：客户端直接 `return true`；服务端在玩家非潜行且当前位置 `TileEntityNukeMan` 存在时调用 `FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z)`。
- `TileEntityNukeMan` 以及其它普通核弹 TileEntity 实现 `IGUIProvider`，由 `provideContainer(...)` 和 `provideGUI(...)` 返回对应 container/gui。
- 核弹方块类本身只是不透明/非普通方块和 `getRenderType() == -1`；没有 `IMultiblock`/dummy/proxy 填充路径，也没有按 OBJ 外形扩展碰撞箱。
- 世界模型位置仍以旧 `RenderNuke*` 的 `x + 0.5D, y, z + 0.5D` 加 metadata 旋转为准；Little Boy 额外 `-2.0D` 平移是旧模型自身契约。

现代修正：

- 撤回 `NuclearDeviceBlock` 对 `MultiblockCoreBlock`、`MultiblockHelper`、dummy proxy 填充/清理、OBJ bounds 选择形状、OBJ bounds 交互形状和中心化 bounds helper 的依赖。
- `NuclearDeviceBlock` 回到普通 `EntityBlock`：继承 `HorizontalMachineBlock` 的单方块 shape，`RenderShape.INVISIBLE` 由 BER 渲染模型，GUI 打开只走当前 core 方块的 `NuclearDeviceBlockEntity`/`MenuProvider`。
- `NuclearDeviceBlock#use(...)` 对齐旧 `onBlockActivated(...)`：潜行放行；客户端返回成功；服务端从当前位置取得或补建 `NuclearDeviceBlockEntity` 后调用 `NetworkHooks.openScreen(serverPlayer, menuProvider, pos)`。
- 若右键已经命中核弹 core 但仍无法取得/补建方块实体，服务端会输出一次 `Could not open nuclear device menu...` 警告，便于区分“没有命中 core”和“方块实体缺失”。
- `NuclearDeviceRenderer` 撤回中心化额外位移，只保留旧 TESR 原点、旧方向 yaw 和 Little Boy `-2.0D` 平移。
- `NuclearDeviceItemRenderer` 再修库存 3D 图标：按旧 `ItemRenderBase` 的“库存变换后调用 renderCommon”矩阵顺序修正 Fat Man/Fleija/Solinium/Prototype 的 bounds 计算；GUI 拟合按旧 16px 槽位换算，不再把所有核弹都强制拟合到同一最大占比。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 核弹 OBJ / 物品 renderer 接入修正

触发来源：

- 核弹 GUI 仍反馈无法打开，同时 `nuke_prototype` 实机丢贴图，核弹在物品栏的大小/位置需要按 1.7.10 重校。
- 这类问题属于 1.7.10 `TileEntitySpecialRenderer implements IItemRendererProvider` 到现代渲染库的迁移，而不是机器逻辑库或爆炸算法库本体。

1.7.10 对齐依据：

- `ResourceManager` 中核弹模型/贴图：
  - `bomb_gadget` / `textures/models/bombs/gadget.png`
  - `bomb_boy` / `textures/models/lilboy.png`
  - `bomb_man` / `textures/models/FatMan.png`
  - `bomb_mike` / `textures/models/bombs/ivymike.png`
  - `bomb_tsar` / `textures/models/bombs/tsar.png`
  - `bomb_prototype` / `textures/models/bombs/Prototype.png`
  - `bomb_fleija` / `textures/models/bombs/fleija.png`
  - `bomb_solinium` / `textures/models/bombs/ufp.png`
  - `n2` / `textures/models/bombs/n2.png`
- 已逐项核对现代 `textures/block/nuke/*.png`，尺寸与字节数均与上述旧模型贴图一致；`nuke_prototype` 不是误用了 16x16 方块图标。
- 旧物品 renderer 统一走 `ItemRenderBase`，库存基础姿态为 `glTranslated(8,10,0)`、`glRotated(-30,X)`、`glRotated(45,Y)`，各核弹再追加自身 `renderInventory()` / `renderCommon()`：
  - Gadget：库存 `translate(0,-3,0)`、`scale(5)`；common `rotateY(-90)`，只渲染 `Body`，fancy 时渲染 `Wires`。
  - Fat Man：库存 `translate(0,-2,0)`、`scale(5)`；common `rotateY(180)`、`translate(-0.75,0,0)`。
  - Ivy Mike / N2：库存 `translate(0,-5,0)`、`scale(2.25)`。
  - Tsar：库存 `scale(2.25)`；common `translate(1.5,0,0)`。
  - Prototype：库存 `translate(0,0.125,0)`、`scale(3)`；common `rotateY(90)`、`translate(0,0.125,0)`。
  - Fleija：库存 `scale(6.8)`；common `translate(0.125,0,0)`、`rotateY(90)`。
  - Solinium：库存 `translate(0,-0.125,0)`、`scale(5)`；common `rotateY(90)`、`translate(0,-0.125,0)`。
  - Little Boy 旧 TESR 没有 `IItemRendererProvider`，现代先使用核弹统一 renderer，并以实际 OBJ bounds 居中，后续实机可继续微调。

现代实现：

- 新增 `ObjNukeModels`，把九个核弹模型放入专用 `models/block/nuke` / `textures/block/nuke` 入口，避免混用 `ObjBombModels`。
- 新增 `NuclearDeviceRenderer`，核弹方块改走 `BlockEntityRenderer` + `LegacyWavefrontModel` 直绘，继承旧 TESR 的朝向修正、模型贴图绑定和远距离渲染常量。
- 新增 `NuclearDeviceItemRenderer` 与 `NuclearDeviceBlockItem`，核弹物品模型改为 `minecraft:builtin/entity`，通过 `LegacyItemRendererBridge` 执行专用 BEWLR：
  - 用 legacy OBJ bounds 自动居中。
  - 按 1.7.10 `renderInventory()` 的 scale / translation 计算 GUI 占位大小。
  - 按旧 `renderCommon()` 追加各核弹旋转/偏移。
- `HbmBlockStateProvider` 中九个核弹改为 `existingModelWithCustomItem(...)`，防止 runData 再把 item model 覆盖回普通 block parent。
- 旁路修正：`ObjBombModels.FAT_MAN` 贴图从错误的 `gadget` 改为 `fat_man`，并补入旧 `textures/models/FatMan.png` 到 `textures/block/bombs/fat_man.png`，避免后续复用 bomb 模型库时 Fat Man 贴图错绑。

仍未完全等价：

- 旧 Gadget 在 fancy graphics 下额外渲染 `Wires`；现代当前核弹 renderer 先渲染完整模型，后续若需要可按 group 拆成 `Body` / `Wires` 并接客户端图形设置。
- 旧 Little Boy 只有世界 TESR，没有专用物品 renderer；现代为了避免 baked OBJ item parent 的贴图/中心点问题，将它纳入统一核弹 item renderer。
- 本批通过构建验证，仍需实机截图确认创造栏九个核弹的最终比例和原型贴图是否恢复。

### 2026-05-25 追加修正：核弹 UI / 世界模型 / 物品图标漂移

实机反馈：

- 创造栏核弹物品图标和 tooltip 出现明显漂移。
- 多种核弹放置后模型相对核心方块偏离，导致视觉模型与可交互核心不一致。
- 核弹 GUI 叠层贴图全体错位。

根因复核：

- `NuclearDeviceItemRenderer` 初版在计算 bounds 时把 1.7.10 `renderInventory()` 的平移纳入了中心点，但实际渲染时没有应用这段平移，造成 GUI item display 中模型被反向抵消到槽位外。
- 世界 BER 初版直接按旧 TESR 原点绘制。旧 OBJ 有不少模型几何中心本来不在 `(0.5, *, 0.5)`，旧版只负责“模型看起来对”，现代玩家右键需要核心方块与模型视觉更一致，因此应在现代 BER 中把 X/Z bounds 中心压回核心块中心。
- `NuclearDeviceScreen` 初版只迁了少量 readiness overlay，且 Tsar 旧 GUI 实际从 `ivyMikeSchematic.png` 采组件叠层；Mike/Fleija/Solinium/N2 也有各自的完整旧叠层逻辑。

现代修正：

- `NuclearDeviceItemRenderer` 在 `applyBaseDisplay(...)` 后恢复执行 `applyLegacyInventory(...)`，使旧 `renderInventory()` 的平移/缩放语义不只参与 bounds，也参与最终渲染。
- `NuclearDeviceRenderer` 改为按 `LegacyWavefrontModel.boundsAll()` 自动做 X/Z 中心校正，让模型围绕核心方块显示，避免“看见模型但点不到核心”的交互错位。
- `NuclearDeviceScreen`：
  - 所有 blit 改用显式 `256x256` 纹理尺寸重载，固定旧 `drawTexturedModalRect` 的采样语义。
  - Tsar overlay 改回旧 `GUINukeTsar` 的 `ivyMikeSchematic.png` 采样源。
  - Mike、Fleija、Solinium、N2 叠层按 1.7.10 GUI 类逐项补齐。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 ResourceManager 效果/实体/工具 OBJ 批量接入

触发来源：

- 继续推进基础渲染库，不只迁移单台机器 renderer，而是补齐 1.7.10 `ResourceManager` 里被多类 renderer 共用的 OBJ 资源与 API。

1.7.10 对齐依据：

- 无贴图 OBJ：
  - `ResourceManager.sphere_ruv/sphere_iuv/sphere_uv/sphere_new` 来自 `models/sphere_*.obj`。
  - `RenderCore`、`ParticleRift`、`RenderCloudFleija`、`RenderCloudSolinium` 等调用这些球体前会 `GL11.glDisable(GL11.GL_TEXTURE_2D)`，颜色来自当前 GL color，不是绑定贴图。
- 工具/效果类：
  - `geiger` -> `models/blocks/geiger_counter.obj` + `textures/blocks/geiger.png`。
  - `forcefield_top` -> `models/forcefield_top.obj` + `textures/models/forcefield_top.png`，底座贴图为 `forcefield_base.png`。
  - `sat_foeq_burning` / `sat_foeq_fire` -> 对应 OBJ；旧资源包只存在 `textures/models/sat_foeq_burning.png`，`sat_foeq_tex` 在代码中声明但缺少实际 PNG。
  - `satDock` -> `models/sat_dock.obj` + `textures/models/sat_dock.png`。
  - `tesla` -> `models/tesla.obj` + `textures/models/tesla.png`。
  - `file_cabinet` -> `models/file_cabinet.obj`，按 renderer 在普通/钢制贴图间切换。
- 实体/武器/护甲/机器类：
  - `teslacrab/maskman/blockspider/ufo/mini_ufo/siege_ufo/glyphid/quadcopter` 及 glyphid 变体贴图来自 `textures/entity`。
  - `meteor` 使用 `models/weapons/meteor.obj`，`RenderBullet` 绑定 `textures/blocks/block_meteor_molten.png`。
  - `tesla_cannon` 使用 `models/weapons/tesla_cannon.obj` 与 `textures/models/weapons/tesla_cannon.png`，旧 renderer 分组为 `Gun/Extension/Cog/Capacitor`。
  - `armor_mod_tesla` 使用 `models/armor/mod_tesla.obj` 与 `textures/armor/mod_tesla.png`。
  - `delivery_drone` 使用 `models/machines/drone.obj` 与 `drone/drone_express/drone_request` 三张贴图。

本轮现代侧改动：

- `LegacyUntexturedQuadRenderer` 暴露 solid/additive no-cull `RenderType`，供 OBJ 几何直接输出 position+color。
- `LegacyWavefrontModel` 新增无贴图渲染入口：
  - `renderAllUntextured(...)` / `renderAllUntexturedAdditive(...)`
  - `renderOnlyUntextured(...)` / `renderOnlyUntexturedAdditive(...)`
  - `renderPartUntextured(...)` / `renderPartUntexturedAdditive(...)`
  - 增加仅传 model location 的构造器，用于 1.7.10 中本来就关闭贴图的 OBJ。
- 新增资源分组：
  - `ObjEffectModels`：`sphere_ruv/iuv/uv/new`。
  - `ObjUtilityModels`：Geiger、forcefield、FOEQ、sat dock、Tesla、file cabinet。
  - `ObjEntityModels`：Tesla crab、maskman、block spider、UFO、glyphid、quadcopter。
- 扩展已有分组与门面：
  - `ObjWeaponModels`：`METEOR`、`TESLA_CANNON`。
  - `ObjArmorModels`：`MOD_TESLA`。
  - `ObjMachineModels`：`DELIVERY_DRONE` 与三张 drone 贴图。
  - `ObjModelLibrary` 暴露对应 `EFFECT_*`、`UTILITY_*`、`ENTITY_*`、`WEAPON_*`、`ARMOR_*`、`MACHINE_DELIVERY_DRONE` 字段。
- 已从 1.7.10 资源包复制 53 个 OBJ/PNG 到现代 `assets/hbm/models/block/...` 与 `textures/block/...` 分区。

后续注意：

- `sat_foeq_tex` 在 1.7.10 代码中存在声明，但旧资源包缺少 `textures/models/sat_foeq.png`；现代端没有生成替代贴图，后续 FOEQ renderer 应按实际可用的 burning/fire 路径处理，或在确认旧运行包外资源后再补。
- 本轮只接入基础库与资源，不主动注册实体 renderer、物品 renderer 或机器逻辑；后续迁移具体 renderer 时应直接复用这些字段。

## 2026-05-25 projectile debris 与 vehicle OBJ 批量接入

触发来源：

- 继续核查 1.7.10 `ResourceManager` 剩余共享 OBJ；确认 `RenderRBMKDebris`、`RenderZirnoxDebris`、矿车/列车 renderer 依赖的模型仍未在现代渲染库中形成门面。

1.7.10 对齐依据：

- RBMK debris：
  - `ResourceManager.deb_blank/deb_element/deb_fuel/deb_rod/deb_lid/deb_graphite` 分别来自 `models/projectiles/deb_*.obj`。
  - `RenderRBMKDebris` 按 `DebrisType` 选择模型与贴图：
    - `BLANK` -> `textures/blocks/rbmk/rbmk_blank_side.png`
    - `ELEMENT` -> `textures/blocks/rbmk/rbmk_side.png`
    - `FUEL` -> `textures/blocks/rbmk/rbmk_fuel.png`
    - `GRAPHITE` -> `textures/blocks/block_graphite.png`
    - `LID` -> `textures/blocks/rbmk/rbmk_blank_cover_top.png`
    - `ROD` -> `textures/blocks/rbmk/rbmk_control.png`
- ZIRNOX debris：
  - `ResourceManager.deb_zirnox_blank/deb_zirnox_concrete/deb_zirnox_element/deb_zirnox_exchanger/deb_zirnox_shrapnel` 分别来自 `models/zirnox/deb_*.obj`。
  - `RenderZirnoxDebris` 中 `BLANK/SHRAPNEL/EXCHANGER` 使用 `zirnox_tex`，`CONCRETE` 使用 `zirnox_destroyed_tex`，`ELEMENT` 使用 `textures/models/machines/zirnox_deb_element.png`，`GRAPHITE` 复用 RBMK graphite debris 模型与 `block_graphite`。
- 载具：
  - `cart/cart_destroyer/cart_powder/tram/tram_trailer` 来自 `models/vehicles`。
  - `RenderNeoCart` 使用 `cart` 的 `Carriage` / `Bucket` group，并在 `cart_metal/cart_metal_naked/cart_wood` 间切换贴图。
  - `EntityMinecartPowder` / `EntityMinecartSemtex` 对 `cart_powder` 的 `Powder/SemtexTop/SemtexSide` 分组分别绑定 `block_gunpowder`、`semtex_bottom`、`semtex_side`。
  - `EntityMinecartDestroyer` 使用 `cart_destroyer` + `textures/entity/cart_destroyer.png`。
  - `RenderTrainCargoTram` / `RenderTrainCargoTramTrailer` 使用 `tram` / `tram_trailer` 与 `textures/models/trains` 下对应贴图。

本轮现代侧改动：

- 从 1.7.10 资源包复制 34 个 debris/vehicle OBJ 与贴图资源：
  - 现代 OBJ 位置：`models/block/projectiles`、`models/block/vehicles`。
  - 现代贴图位置：`textures/block/projectiles`、`textures/block/vehicles`。
- `ObjProjectileModels` 增加：
  - `DEBRIS_BLANK/ELEMENT/FUEL/ROD/LID/GRAPHITE`
  - `ZIRNOX_DEBRIS_BLANK/CONCRETE/ELEMENT/EXCHANGER/SHRAPNEL`
  - 对应 RBMK、graphite、zirnox 贴图常量。
- 新增 `ObjVehicleModels`：
  - `CART/CART_DESTROYER/CART_POWDER/TRAM/TRAM_TRAILER`
  - 对应 `cart_metal/cart_metal_naked/cart_wood/cart_destroyer/cart_powder/cart_semtex_side/cart_semtex_top/tram/tram_trailer` 贴图常量。
- `ObjModelLibrary` 暴露 projectile debris 与 vehicle facade 字段，后续实体 renderer 可直接复用。

后续注意：

- 本轮只接模型与贴图，不迁 `EntityRBMKDebris`、`EntityZirnoxDebris`、矿车/列车实体和 renderer 的逻辑。
- 迁 debris renderer 时要保留旧实现的位移 `y + 0.125D` 与按 entityId/rot 的旋转规则；ZIRNOX debris 还会临时关闭背面剔除。

## 2026-05-25 ResourceManager 武器 OBJ 批量接入

触发来源：

- 继续推进基础渲染库，优先把 1.7.10 `ResourceManager` 中被 `render/item/weapon`、`render/item/weapon/sedna` 和若干实体/装饰 renderer 共用的武器 OBJ 资源接入现代模型门面。

1.7.10 对齐依据：

- `ResourceManager` 中本批明确存在 OBJ 的字段包括：
  - 近战/道具：`stopsign`、`gavel`、`crucible`、`chainsaw`、`lance`、`grenades`。
  - 枪械/发射器：`boltgun`、`bolter`、`detonator_laser`、`fireext`、`coilgun`、`pepperbox`、`bio_revolver`、`henry`、`greasegun`、`maresleg`、`flaregun`、`am180`、`liberator`、`congolake`、`flamethrower`、`lilmac`、`carbine`、`uzi`、`spas_12`、`panzerschreck`、`star_f`、`g3`、`stinger`、`mk108`、`chemthrower`、`amat`、`m2`、`shredder`、`sexy`、`whiskey`、`quadro`、`mike_hawk`、`minigun`、`missile_launcher`、`tesla_cannon`、`laser_pistol`、`stg77`、`tau`、`fatman`、`lasrifle`、`lasrifle_mods`、`hangman`、`folly`、`double_barrel`、`aberrator`、`mas36`、`charge_thrower`、`drill`、`n_i_4_n_i`。
  - 旧字段 `double_barrel` 实际加载 `models/weapons/sacred_dragon.obj`。
  - 旧字段 `m2` 实际加载 `models/weapons/m2_browning.obj`。
  - 旧字段 `mike_hawk` 使用 `mike_hawk.obj`，默认贴图字段为 `mike_hawk_tex = textures/models/weapons/lag.png`。
  - 旧字段 `folly` 使用 `folly.obj`，默认贴图字段为 `folly_tex = textures/models/weapons/moonlight.png`。
- 旧端多个 renderer 会动态切 part 与贴图：
  - `ItemRenderCrucible` 按 `Hilt/GuardLeft/GuardRight/Blade` 分别绑定 hilt/guard/blade 贴图。
  - `ItemRenderChainsaw` 渲染 `Saw` 与循环的 `Tooth`。
  - Sedna 系列枪械普遍按状态渲染 `Gun/Mag/Bolt/Slide/Silencer/Bullet` 等 group。
  - `RenderBobble` 也复用 `n_i_4_n_i`、`fatman`、`double_barrel` 等武器模型。

本轮现代侧改动：

- 从 1.7.10 资源包复制 53 个武器 OBJ 到 `models/block/weapons`。
- 从 1.7.10 资源包复制 101 张实际存在的武器贴图到 `textures/block/weapons`。
- `ObjWeaponModels` 批量增加上述武器模型入口，并按旧 `ResourceManager` 默认贴图/变体贴图暴露 `ResourceLocation` 常量。
- `ObjModelLibrary` 暴露主要 `WEAPON_*` facade 字段，后续 item/entity renderer 可统一从渲染库取模型。

后续注意：

- 本轮不迁移 `AnimationLoader` / `BusAnimation` / `HbmAnimations`；`spas12`、`congolake`、`am180`、`flamethrower`、`stg77`、`lag` 等动画 JSON 仍需在枪械 renderer 专题中迁移。
- 1.7.10 `ResourceManager` 中存在声明但旧资源包缺少实际 PNG 的武器贴图：
  - `pch_tex = textures/models/weapons/pch.png`，当前未发现 renderer 使用。
  - `bolter_digamma_tex = textures/models/weapons/bolter_digamma.png`。
  - `sky_stinger_tex = textures/models/weapons/sky_stinger.png`。
  - 现代端没有生造这些缺失贴图；后续若发现运行包额外资源，再按真实资源补入。

### 2026-05-24 多方块库存图标默认比例回调

- 实机反馈：一批已经挂上 OBJ 的机器图标又偏小，未达到 1.7.10 库存渲染中“接近一个槽位但保留边距”的观感。
- 复核 1.7.10 多数机器 `renderInventory()`：旧端并不按多方块结构盒保守缩放，而是在 `ItemRenderBase` 基础姿态后继续以 2.5-5 倍级别放大具体 OBJ。
- 现代端仍使用 OBJ bounds 自动居中，但将 `LegacyMachineDefinition.itemFitSize` 默认值从 `0.58` 回调到 `0.72`；`machine_assembly_machine` 与 `machine_battery_socket` 的专用 item renderer 也改用同一默认目标。
- GUI 最大缩放上限继续保留 `0.32`，避免小型 OBJ 或单 part 机器重新溢出槽位；后续个别机器仍通过 `.itemFitSize(...)` 按 1.7.10 单独校正。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 多方块库存缩放改为旧 renderInventory 派生

- 实机反馈：统一回调到 `0.72` 后，部分机器又偏大；继续使用公共目标尺寸不符合 1.7.10 的逐机器库存 renderer。
- 复核旧链路：
  - `ItemRenderBase#renderItem(INVENTORY)` 固定执行 `glTranslated(8,10,0)`、`glRotated(-30,X)`、`glRotated(45,Y)`、`glScaled(-1,-1,-1)`。
  - 每台机器再在 `renderInventory()` 写自己的 `glScaled(...)`。
  - 如果 `renderCommon()` / `renderCommonWithStack()` 还有静态 `glScaled(...)`，它也参与库存最终比例。
- 现代端新增 `LegacyMachineDefinition.legacyItemScale`，记录旧端 `renderInventoryScale * renderCommonScale`。
- `LegacyVisibleMachineItemRenderer` 现在对配置了 `legacyItemScale` 的机器使用：
  - `OBJ bounds maxSize * legacyItemScale / 16`
  - 其中 `16` 对应 1.7.10 库存槽像素基准。
  - 保留现代槽位上限 `0.86`，防止旧端少数会出格的 item renderer 在现代创造栏中继续穿格。
- 当前可见多方块批次已逐台补入旧缩放值，例如：
  - `machine_silex = 3.25`
  - `machine_chemical_factory = 3 * 0.75`
  - `machine_tower_small = 3 * 0.25`
  - `machine_tower_large = 3.8 * 0.25`
  - `machine_catalytic_cracker = 1.8 * 0.5`

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-24 可见多方块机器物品 renderer 桥

触发来源：

- 实机发现新接入的大型 OBJ 多方块机器放置后能由 BER 渲染，但创造栏/物品栏中大量显示为紫黑缺失模型或比例失控。
- 核查 1.7.10 后确认旧端不是依赖普通 block item model，而是：
  - `ClientProxy.registerItemRenderer()` 先注册 `ItemRenderLibrary`；
  - 再自动扫描实现 `IItemRendererProvider` 的 TESR，把对应方块物品接到同一个旧 `IItemRenderer`；
  - 锻压机、泵机、小/大冷却塔等机器在 `ItemRenderLibrary` 或各自 TESR 中单独写 `renderInventory()` 缩放/平移。

现代补齐：

- 新增 `LegacyVisibleMachineItemRenderer`：
  - 复用 `LegacyWavefrontModel`，按 `LegacyMachineDefinition` 的 OBJ、贴图、part 列表直绘。
  - GUI/地面/手持场景统一根据 `definition.renderBoundingBox(...)` 自动计算中心与缩放，让尺寸差异很大的多方块机器能进入 16x16 slot。
  - 渲染时复用和 `LegacyVisibleMachineRenderer` 一致的 `yRotation(...)` 与 `modelTranslation(...)`，避免物品视图和世界视图出现朝向/原点分歧。
- `MultiblockBlockItem.initializeClient(...)` 对 `LegacyVisibleMultiblockMachineBlock` 接入 `LegacyItemRendererBridge`。
- DataGen 为所有当前 `LegacyVisibleMultiblockMachineBlock` 大机器生成 `minecraft:builtin/entity` item model，避免 Forge baked OBJ block parent 继续接管库存显示。
- 修正一次现代 Forge 接线时序问题：
  - `Item` 构造函数内部会调用 `initializeClient(...)`。
  - 此时 `BlockItem` 子类的 `block` 字段还未完成赋值，因此不能在 `MultiblockBlockItem.initializeClient(...)` 里调用 `getBlock()` 判断是否为 `LegacyVisibleMultiblockMachineBlock`。
  - 现代端改为对所有 `MultiblockBlockItem` 挂同一个 renderer；renderer 在真正 `renderByItem(...)` 时再从 `ItemStack` 取 `BlockItem#getBlock()` 并过滤非可见机器。这样对齐旧 1.7.10 “物品 renderer 先注册，渲染时再看具体 item”的模式。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `.\gradlew.bat runData --no-daemon --rerun-tasks` 通过，已确认 `machine_tower_small`、`machine_pumpjack`、`machine_chemical_factory` 等生成 item model parent 为 `minecraft:builtin/entity`。
- 二次修正后 `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-24 ResourceManager 机器 OBJ 补齐批次

触发来源：

- 用户要求回到渲染库移植并一次推进更多内容。
- 本轮继续按 1.7.10 `ResourceManager` 字段核查，而不是凭现代资源目录自行扩展。

1.7.10 事实来源：

- `com.hbm.main.ResourceManager`
  - `stirling = AdvancedModelLoader.loadModel("models/machines/stirling.obj")`
  - `sawmill = AdvancedModelLoader.loadModel("models/machines/sawmill.obj")`
  - `strand_caster = new HFRWavefrontObject("models/machines/strand_caster.obj")`
  - `furnace_steel = AdvancedModelLoader.loadModel("models/machines/furnace_steel.obj")`
  - `conveyor_press = AdvancedModelLoader.loadModel("models/machines/conveyor_press.obj")`
  - `microwave = AdvancedModelLoader.loadModel("models/machines/microwave.obj")`
  - `piston_inserter = AdvancedModelLoader.loadModel("models/machines/piston_inserter.obj")`
  - `igen = new HFRWavefrontObject("models/machines/igen.obj")`
- 旧 renderer 已确认的分组调用：
  - `RenderStirling`：`Base`、`Cog`、`CogSmall`、`Piston`
  - `RenderSawmill`：`Main`、`Blade`、`GearLeft`、`GearRight`
  - `RenderStrandCaster`：`caster`、`plate`
  - `RenderConveyorPress`：`Press`、`Piston`、`Belt`
  - `RenderMicrowave`：`mainbody_Cube.001`、`window_Cube.002`、`plate_Cylinder`
  - `RenderPistonInserter`：`Frame`、`Piston`
  - `RenderIGenerator`：`Body`、`Rotor`

本轮现代补齐：

- 从 1.7.10 资源复制到现代资源树：
  - `models/block/machines/stirling.obj`
  - `models/block/machines/sawmill.obj`
  - `models/block/machines/strand_caster.obj`
  - `models/block/machines/furnace_steel.obj`
  - `models/block/machines/conveyor_press.obj`
  - `models/block/machines/microwave.obj`
  - `models/block/machines/piston_inserter.obj`
  - `models/block/machines/igen.obj`
  - 对应 `textures/block/machines/*.png`，包括旧 renderer 直接引用的 `stirling_steel`、`stirling_creative`、`conveyor_press_belt` 等。
- `ObjMachineModels` 增加与旧 `ResourceManager` 对应的 `LegacyWavefrontModel` 常量：
  - `STIRLING`、`SAWMILL`、`STRAND_CASTER`、`FURNACE_STEEL`、`CONVEYOR_PRESS`、`MICROWAVE`、`PISTON_INSERTER`、`IGEN`
- `ObjMachineModels` 增加旧 renderer 需要的贴图入口：
  - `STIRLING_TEXTURE`、`STIRLING_STEEL_TEXTURE`、`STIRLING_CREATIVE_TEXTURE`
  - `SAWMILL_TEXTURE`、`STRAND_CASTER_TEXTURE`、`FURNACE_STEEL_TEXTURE`
  - `CONVEYOR_PRESS_TEXTURE`、`CONVEYOR_PRESS_BELT_TEXTURE`
  - `MICROWAVE_TEXTURE`、`PISTON_INSERTER_TEXTURE`、`IGEN_TEXTURE`
- `ObjModelLibrary` 暴露同名 `MACHINE_*` 入口，供后续 BER / item renderer 按旧分组名接入。

仍未完全等价：

- 本轮只补资源载体和渲染库入口，未迁具体机器 BER 动画状态。
- `stirling`、`sawmill`、`conveyor_press`、`microwave`、`piston_inserter`、`igen` 的 part 动画与多贴图切换仍需按旧 renderer 单独迁移。

## 2026-05-24 旧 OBJ 机器亮度修正

触发来源：

- 用户反馈大型机器模型整体过暗；之前只要求降低锻压机压头亮度，不应把所有机器整体降暗。

1.7.10 事实核查：

- 旧 `S_Face.addFaceForRender(...)` 只向 `Tessellator` 写入 normal 和顶点/UV，不做颜色降亮。
- 旧 `RenderRefinery`、`RenderVacuumDistill`、`RenderPumpjack` 等机器 renderer 的常规模型段使用 `GL11.glEnable(GL11.GL_LIGHTING)` 并直接 `ResourceManager.xxx.renderAll/renderPart()`。
- 旧版显式发光/叠加段才会单独 `GL11.glDisable(GL11.GL_LIGHTING)` 或设置特殊颜色；普通机器主体没有统一的 `0.82` 或 `0.45` 降亮规则。

现代修正：

- 保留 `ObjMachineModels.PRESS_HEAD.withLightMultiplier(0.82F)`，该降亮只用于锻压机压头。
- 新增 `LegacyRenderLighting`，为旧 OBJ BlockEntity 渲染集中解析 lightmap：
  - 普通单方块/小型 BE：取 BE 坐标与上方坐标中的较亮 lightmap。
  - `LegacyMachineDefinition` 多方块机器：取 BE 坐标、上方坐标、模型渲染包围盒顶部中心与顶部四角中的较亮 lightmap。
- 接入：
  - `LegacyVisibleMachineRenderer`
  - `ChemicalPlantRenderer`
  - `LiquefactorRenderer`
  - `AssemblyMachineRenderer`
  - `BasicMachineRenderer`
  - `MachineBatterySocketRenderer`

迁移说明：

- 这不是给机器做 fullbright；仍然使用 Minecraft lightmap，只是避免现代 BE core 被多方块结构或模型底部遮住时，把整台旧 OBJ 机器错误压暗。
- 后续迁更多 TESR/BER 时，普通旧 OBJ 主体应使用该 lightmap 解析规则；旧版明确关光照的发光层再单独迁 `FULL_BRIGHT` 或专用 emissive 路径。

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

## 2026-05-24 多方块物品 OBJ 边界与专用 item renderer 修正

触发来源：

- 大型多方块机器在世界中已有 BER 渲染，但创造物品栏中仍有未挂上模型的机器；已确认包括 `machine_solidifier`、`machine_battery_socket`、`machine_assembly_machine`。
- 部分已显示的机器比例偏大，且因 core 不一定处于模型中心，使用结构盒或 core 坐标居中会导致库存图标偏移。

1.7.10 对齐依据：

- `ItemRenderBase` 的库存路径通过 `glTranslated(8,10,0)`、`glRotated(-30,X)`、`glRotated(45,Y)` 后调用具体 renderer 的 `renderInventory()` / `renderCommon()`，不是普通方块 JSON。
- `ItemRenderLibrary` 中 `machine_solidifier` 的库存渲染只画 `ResourceManager.solidifier.renderPart("Main")`，`Fluid` 和 `Glass` 属于世界/状态显示。
- `RenderBatterySocket#getRenderer()` 的库存渲染只画 `battery_socket.renderPart("Socket")`。
- `RenderAssemblyMachine#getRenderer()` 的库存渲染使用 `glRotated(90,Y)`、`glScaled(0.75)` 后 `ResourceManager.assembly_machine.renderAll()`。

本轮现代侧改动：

- `LegacyWavefrontModel` 新增 `boundsAll()` / `boundsOnly(...)`，按实际 OBJ group 顶点计算 AABB，供物品显示按模型真实几何居中缩放。
- `LegacyVisibleMachineItemRenderer` 不再用多方块结构/剔除 AABB 估算库存图标大小，改为按渲染 group 的实际 OBJ bounds 套用同一旋转/平移后再居中。
- `LegacyMachineDefinition` 新增 `itemRenderParts(...)`，允许库存显示与世界显示分离；`machine_solidifier` 已按旧代码改为库存仅渲染 `Main`。
- `machine_battery_socket` 与 `machine_assembly_machine` 复用同一个 `MultiblockBlockItem` 物品 renderer 入口，分别按旧 `Socket` 与 `assembly_machine.renderAll()` 路径渲染。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `.\gradlew.bat runData --no-daemon --rerun-tasks` 通过。

## 2026-05-24 多方块物品二次筛查与 OBJ/MTL 兼容修补

触发来源：

- 创造栏大部分多方块机器已能显示，但仍有单个缺失模型，且部分机器图标偏大。
- 客户端日志显示 Forge OBJ loader 读取 `catalytic_cracker`、`fluidtank`、`radiolysis` block model 时失败；其中 `CatalyticCrackerV2.mtl` 还含 1.20 `ResourceLocation` 不允许的大写字符。

本轮现代侧改动：

- 清理会破坏 Forge OBJ loader 的旧 `mtllib` 行：
  - 已直接导致烘焙失败：`catalytic_cracker.obj`、`fluidtank.obj`、`radiolysis.obj`。
  - 同类潜伏资源：`electrolyser.obj`、`machine_deuterium_tower.obj`、`orbus.obj`、`piston_inserter.obj`。
- `LegacyMachineDefinition` 新增 `itemPartTextures`，允许库存渲染中不同 OBJ part 使用不同贴图；`machine_turbofan` 的 `Afterburner` 已按 1.7.10 `RenderTurbofan#getRenderer()` 改用 `turbofan_back`。
- `LegacyMachineDefinition` 新增 `itemFitSize`，把物品栏 OBJ 占位大小从 renderer 硬编码移到机器定义层；默认占位从上一轮偏大的自动 fit 收紧，后续可按 1.7.10 `renderInventory()` 对单台机器独立校正。

验证：

- 已扫描 `assets/hbm/models/block/machines/*.obj`，当前没有剩余非法或缺失的 `mtllib` 引用。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-24 liquefactor 库存渲染复核

- 实机反馈 `machine_liquefactor` 库存图标仍存在贴图/叠层异常。
- 复核 1.7.10 `ItemRenderLibrary`：`machine_liquefactor` 的 item renderer 只执行 `ResourceManager.liquefactor.renderPart("Main")`，不渲染 `Fluid` / `Glass`。
- 现代 `liquefactorDefinition` 已补 `itemRenderParts("Main")`，与 `machine_solidifier` 的旧库存路径一致。
- `LegacyVisibleMachineItemRenderer` 的小/中型库存模型最大 GUI scale 从 `0.28` 恢复为 `0.32`，避免上一轮全局收紧后部分机器过小；大型机器仍受 `itemFitSize / maxSize` 限制。

### 2026-05-24 liquefactor 物品模型入口修正

- 再次实机反馈：`machine_liquefactor` 在创造栏/物品栏仍看不见专用多方块机器图标，只显示 ID 与 tooltip。
- 复核结论：问题不在 fluid MK2 库主动塞入额外渲染层；fluid 库迁入的是该机器的处理逻辑、GUI 与 tank 行为。库存/界面中的 OBJ 图标属于 1.7.10 `ItemRenderLibrary` -> 现代 `MultiblockBlockItem` + `LegacyVisibleMachineItemRenderer` 的渲染库/多方块物品渲染链。
- 现代 datagen 已对 `MACHINE_LIQUEFACTOR` 调用 `visibleMachineWithItemRenderer(...)`，应生成 `minecraft:builtin/entity` 物品模型，以触发 `MultiblockBlockItem.initializeClient(...)` 挂上的 BEWLR。
- 实际资源 `assets/hbm/models/item/machine_liquefactor.json` 仍停留在旧 `hbm:block/machines/liquefactor` 父模型，导致 Forge 走普通 block item baked model，绕开专用 OBJ 物品 renderer。
- 已将该 item model 改为 `minecraft:builtin/entity`；渲染内容仍由 `liquefactorDefinition.itemRenderParts("Main")` 保证只画旧版库存路径中的 `Main` group。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-25 核弹 BER 坐标与选择形状修正

触发来源：

- 实机截图显示 N2、Fat Man 等核弹放置后，模型相对一格核心方块明显外伸；由于现代方块仍只有一格选择/交互范围，玩家对着可见 OBJ 主体右键时可能没有命中核心方块，GUI 表现为打不开。
- 上一轮曾尝试按 OBJ bounds 自动居中世界模型，但这不符合 1.7.10 TESR 的事实源，且会破坏部分核弹模型本身的旧版偏置语义。

1.7.10 对照：

- `RenderNukeGadget` / `RenderNukeMan` / `RenderNukeMike` / `RenderNukeTsar` / `RenderNukePrototype` / `RenderNukeFleija`：世界渲染入口均为 `GL11.glTranslated(x + 0.5D, y, z + 0.5D)` 后按 metadata 旋转。
- `RenderNukeBoy`：同样以 `x + 0.5D, y, z + 0.5D` 为原点，但每个方向分支旋转后额外 `GL11.glTranslated(-2.0D, 0.0D, 0.0D)`。
- `RenderNukeSolinium` / `RenderNukeN2`：先 `GL11.glRotatef(90, Y)`，再按 metadata 追加方向旋转。
- 旧版方块仍是单个 `BlockContainer`，但 1.20.1 的玩家交互更容易暴露“可见大模型”和“一格核心 hitbox”不一致的问题。

现代修正：

- `NuclearDeviceRenderer` 移除自动居中逻辑，改为调用 `NuclearDeviceBlock.legacyRenderYaw(...)`，并只对 `BOY` 追加旧版 `-2.0D` 模型平移。
- `NuclearDeviceBlock` 新增核弹 OBJ 旧资源 bounds 常量，并按旧 TESR 变换计算 `getShape(...)` 与 `getInteractionShape(...)`，让选择框/右键命中范围覆盖实际可见模型。
- `getCollisionShape(...)` 保持 `Shapes.block()`，避免 N2/Mike 这类高大模型把碰撞也扩成多格实体墙；本轮只扩现代交互/轮廓，不改旧一格方块碰撞。
- 胖子核弹 GUI 坐标已复查：`GUINukeMan` 和 `ContainerNukeMan` 与 `GUINukeGadget` 同一套 slot/overlay 坐标，现代 `NuclearDeviceMenu.Layout.MAN` 与 `renderImplosionStatus(...)` 保持一致；本轮不改 GUI 贴图坐标。若后续仍看到“胖子偏移”，应优先继续查 `NuclearDeviceItemRenderer` 的物品栏 3D 图标，而不是配置 GUI 背景。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：核弹模型中心回压核心方块

- 继续实机反馈指出上一轮“只有碰撞箱变成核弹整体样子，模型偏移一点没变”。确认原因：上一轮保留了 1.7.10 单格 TESR 外伸坐标，只扩展了现代选择/交互形状；这不会改变视觉模型位置。
- 现代高版本修正口径调整：
  - 保留 1.7.10 renderer 的原始旋转和 Little Boy 额外 `-2` 平移作为模型姿态事实源。
  - 在这些旧变换之后计算 OBJ 水平包围盒中心，再额外平移到核心方块中心，解决现代端“可见核弹主体偏离核心方块”的交互观感问题。
  - `NuclearDeviceBlock#transformedLegacyBounds(...)` 和 `NuclearDeviceRenderer` 共用同一套中心化平移，因此选择框和可见模型应同步移动，不再出现只动碰撞箱不动模型。
- 仍不改 `NuclearDeviceScreen` 的 Fat Man GUI 背景/slot 坐标：1.7.10 `GUINukeMan` 坐标已经复核一致；“UI 中胖子核弹偏移”若指创造栏/JEI/物品栏里的 3D 图标，需要继续在 `NuclearDeviceItemRenderer` 独立校正。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 追加：核弹物品 3D 图标旧数据修正

触发来源：

- 实机反馈 Fat Man 物品栏/创造栏 3D 图标偏移，其他核弹图标尺寸也不合适。

1.7.10 对照：

- 所有旧 `ItemRenderBase` 库存路径先执行共同变换：
  - `glTranslated(8, 10, 0)`
  - `glRotated(-30, X)`
  - `glRotated(45, Y)`
  - `glScaled(-1, -1, -1)`
- 各核弹 renderer 的库存/通用数据：
  - `RenderNukeGadget`：库存 `translate(0,-3,0)`、`scale(5)`；通用 `rotateY(-90)`。
  - `ItemRenderLibrary` 的 `nuke_boy`：库存 `scale(5)`；通用 `translate(-1,0,0)`。
  - `RenderNukeMan`：库存 `translate(0,-2,0)`、`scale(5)`；通用 `rotateY(180)`、`translate(-0.75,0,0)`。
  - `RenderNukeMike` / `RenderNukeN2`：库存 `translate(0,-5,0)`、`scale(2.25)`。
  - `RenderNukeTsar`：库存 `scale(2.25)`；通用 `translate(1.5,0,0)`。
  - `RenderNukePrototype`：库存 `translate(0,0.125,0)`、`scale(3)`；通用 `rotateY(90)`、`translate(0,0.125,0)`。
  - `RenderNukeFleija`：库存 `scale(6.8)`；通用 `translate(0.125,0,0)`、`rotateY(90)`。
  - `RenderNukeSolinium`：库存 `translate(0,-0.125,0)`、`scale(5)`；通用 `rotateY(90)`、`translate(0,-0.125,0)`。

现代修正：

- `NuclearDeviceItemRenderer` 的 GUI bounds 计算现在按“库存变换 + 通用变换”计算，不再把旧库存 scale 当成拟合参考后又不实际应用。
- `NuclearDeviceItemRenderer` 实际应用各核弹旧库存 `translate/scale`，并补回 Little Boy 旧通用 `translate(-1,0,0)`、库存 `scale(5)`。
- Fat Man 的现代 bounds 与实际渲染都保留旧通用 `rotateY(180)` 后 `translate(-0.75,0,0)`，用于修正图标偏移。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

### 2026-05-25 最终口径：覆盖前两条实验性修正

- 本文前面的“核弹 BER 坐标与选择形状修正”和“核弹模型中心回压核心方块”是实机排查中的实验性方案，已被“核弹单方块契约回退”覆盖。
- 后续继续修核弹时，以当前代码为准：
  - 核弹不接 `MultiblockCoreBlock`，不放 dummy/proxy。
  - 核弹不按 OBJ bounds 扩选择箱、交互箱或碰撞箱。
  - 世界模型不自动中心化，沿用 1.7.10 TESR 原点、yaw 和 Little Boy 额外平移。
  - GUI 打开只从被命中的一格 core 方块进入 `NuclearDeviceBlockEntity`/`NetworkHooks.openScreen(...)`。

### 2026-05-25 追加：核弹 OBJ 坐标回源与 metadata 映射修正

实机反馈：

- 核弹回退到单方块后，选择框/核心块已是一格，但 Mike、Little Boy、Fat Man 等世界模型仍相对核心块半格或错向偏移。
- UI 仍反馈无法打开；`latest.log` 中没有 `Could not open nuclear device menu...` 警告，也没有菜单构造异常，因此需要区分“右键没有进入核弹方块”与“菜单包已发送但客户端 screen 未打开”。

复核发现：

- 现代 `models/block/nuke/*.obj` 不是 1.7.10 原始坐标：除 Little Boy 外，多数核弹顶点整体相对旧资源平移了 `(0.5, 0, 0.5)`；Little Boy 也被改成另一套中心化坐标。
- 这种现代坐标适配只适合普通 baked OBJ block model；当前核弹已经改走旧 TESR 风格的 `BlockEntityRenderer`，继续使用半格中心化 OBJ 会与旧 `x + 0.5, y, z + 0.5` 原点叠加，造成模型偏移。
- 现代 `HorizontalMachineBlock#getStateForPlacement(...)` 保存的是玩家朝向的反向 `FACING`，而 1.7.10 核弹 metadata 是按玩家 yaw 直接写入 `5/3/4/2`；此前直接把现代 `FACING` 套入旧 renderer metadata 表，导致旋转错位。

现代修正：

- 九个核弹 OBJ 和 `gadget_body.obj` 的 `v` 顶点坐标恢复为 1.7.10 原资源坐标；保留现代端 `.mtl`、`mtllib` 和 `usemtl default`，保证 Forge OBJ loader 仍能通过 `#default` 贴图链路烘焙。
- `NuclearDeviceBlock.legacyRenderYaw(...)` 改为按现代 `FACING -> 旧 metadata -> 旧 RenderNuke* yaw` 的映射：
  - 现代 `NORTH/EAST/SOUTH/WEST` 分别等价旧 metadata `5/3/4/2`。
  - Mike/Prototype/Fleija 对应 `90/0/270/180`；Boy/Tsar 对应 `0/270/180/90`；Man 对应 `180/90/0/270`；Gadget 对应 `270/180/90/0`；Solinium/N2 保留旧额外 `90` 度后的结果。
- `NuclearDeviceBlock#use(...)` 在服务端成功进入 `NetworkHooks.openScreen(...)` 前输出 `Opening nuclear device menu...`，便于后续实机 log 精确判断 UI 打不开的断点。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- 编译末尾的 deprecation 注记来自 `MultiblockDummyBlockEntity`，不是本轮核弹渲染/菜单修复造成的失败。

## 2026-05-26 ResourceManager 护甲 OBJ 批量接入

1.7.10 对照：

- `ResourceManager` lines 950-971 声明护甲/穿戴 OBJ：`armor_bj`、`armor_hev`、`armor_ajr`、`armor_t51`、`armor_hat`、`armor_no9`、`armor_goggles`、`armor_fau`、`armor_dnt`、`armor_steamsuit`、`armor_dieselsuit`、`armor_remnant`、`armor_ncr`、`armor_bismuth`、`armor_mod_tesla`、`armor_wings`、`armor_axepack`、`armor_tail`、`player_manly_af`、`armor_envsuit`、`armor_taurun`、`armor_trenchmaster`。
- 除 `player_manly_af` 额外 `.noSmooth()` 外，上述旧模型均通过 `.asVBO()` 暴露；现代 `ObjArmorModels` 按同名契约补齐 `asVBO()` / `noSmooth().asVBO()` 门面。
- `ResourceManager` lines 1107-1194 声明护甲部位贴图：BJ、Envsuit、HEV、AJR/AJRO、T51、FAU、DNT、Steamsuit、Dieselsuit/Bnuuy、RPA、NCRPA、Taurun、Trenchmaster、Mod Tesla、Bismuth、Murk wings、Pheo axepack、Peep tail、hat/no9/goggles，以及 `textures/entity/player_fem.png`。

现代侧改动：

- 从 1.7.10 资源复制 22 个护甲/穿戴 OBJ 到 `assets/hbm/models/block/armor/`，复制 71 张对应贴图到 `assets/hbm/textures/block/armor/`。
- 旧 `BJ.obj` 与 `AJR.obj` 在现代资源树中改名为小写 `bj.obj` / `ajr.obj`，用于满足 1.20 `ResourceLocation` 路径小写要求；OBJ 内容仍来自 1.7.10 原资源。
- `ObjArmorModels` 补齐上述模型常量和部位贴图 `ResourceLocation` 常量；`ObjModelLibrary` 补齐对应 `ARMOR_*` facade 字段。
- 本轮只迁入渲染库资源入口和贴图句柄，不迁护甲 `ModelRendererObj` 动画、按部位绑定贴图、发光/全亮部件和穿戴逻辑；这些应在后续护甲模型 renderer 移植时继续按 `com.hbm.render.model.*` 与 `com.hbm.items.armor.*` 对齐。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-05-28 ResourceManager asVBO facade 批量对齐

1.7.10 对照：

- `ResourceManager` 中大量机器静态模型直接使用 `new HFRWavefrontObject(...).asVBO()`，而现代 `ObjMachineModels` 已有同名 `LegacyWavefrontModel` facade，但其中一批仍只是普通模型入口。
- 本轮只处理旧端明确 `.asVBO()` 的 OBJ 入口；旧端 `AdvancedModelLoader.loadModel(...)`、普通 `new HFRWavefrontObject(...)`、`.noSmooth()` 无 `.asVBO()` 的入口保持不动。
- 覆盖的机器段包括：
  - 炼油/流体/化工/矿业/离心机/大型发电类：`refinery`、`fluidtank`、`vacuum_distill`、`catalytic_cracker`、`liquefactor`、`solidifier`、`compressor`、`coker`、`turbofan`、`steam_engine`、`industrial_turbine`、`cyclotron`、`machine_deuterium_tower` 等。
  - 加热/锅炉/油井/烟囱/柴油/FENSU 旧式 facade：`oilburner`、`heatex`、`boiler`、`industrial_boiler`、`hephaestus`、`derrick`、`pumpjack`、`fracking_tower`、`flare_stack`、`chimney_*`、`dieselgen`、`battery/fensu*`。
- `ResourceManager.pa_source/beamline/rfc/quadrupole/dipole/detector` 全部为 `HFRWavefrontObject(...).asVBO()`。
- `ResourceManager` 的网络模型中 `substation`、`pipe_anchor`、`fluid_diode` 为 `.asVBO()`；`connector/pylon` 旧式 noSmooth VBO facade 已在前序批次存在。
- RBMK 需要保留双入口：`rbmk_element_rods` / `rbmk_rods` 有普通 `.noSmooth()` 字段，也有对应 `*_vbo = ...noSmooth().asVBO()` 字段；控制台/吊车/按钮/仪表/终端等均为 VBO。
- 融合反应堆 `torus/klystron/breeder/collector/boiler/mhdt/coupler/plasma_forge` 在旧端均为 `.asVBO()`；现代已有 baked part 入口，本轮补旧式整 OBJ facade。

现代侧改动：

- `ObjMachineModels` 为上述旧端 VBO 机器 facade 补 `.asVBO()`，并新增加热器、锅炉、油井、烟囱、柴油机、FENSU 等 `*_LEGACY` 入口；`ObjModelLibrary` 暴露对应 `MACHINE_*_LEGACY`。
- `ObjParticleAcceleratorModels` 六个模型全部补 `.asVBO()`。
- `ObjNetworkModels` 增加 `SUBSTATION_LEGACY`、`PIPE_ANCHOR_LEGACY`、`FLUID_DIODE_LEGACY`，并在 `ObjModelLibrary` 暴露。
- `ObjRbmkModels` 增加 `ELEMENT_RODS_VBO`、`RODS_VBO`，并将吊车/控制台/按钮/仪表等旧 VBO 字段补 `.asVBO()`；`ObjModelLibrary` 暴露 RBMK 双入口。
- `ObjFusionModels` 增加 `*_LEGACY` 整 OBJ VBO facade 和旧贴图句柄，保留现有 baked part/分 part 渲染入口。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-02 发射架 erector 与导弹部件库门面补齐

1.7.10 对照：

- `ResourceManager` lines 1220-1226：导弹本体 `missileV2`、`missileABM`、`missileStrong`、`missileHuge`、`missileNuclear/missile_atlas`、`missileMicro` 均为 `HFRWavefrontObject(...).asVBO()`；`missileStealth` 为 `.noSmooth().asVBO()`。
- `ResourceManager` lines 1240-1253：大型发射台同组资源中，`missile_pad`、`missile_assembly`、`compact_launcher`、`launch_table_*` 均为 `AdvancedModelLoader.loadModel(...)`；只有 `missile_erector = new HFRWavefrontObject("models/weapons/launch_pad_erector.obj").asVBO()`。
- `ResourceManager` lines 1433-1451：发射台贴图包括 `textures/models/launchpad/pad.png` 与 `erector_micro/v2/strong/huge/atlas/abm.png`，launch table 子件贴图仍来自 `textures/models/missile_parts/launch_table*.png`。
- `RenderLaunchPadLarge` 使用 `ResourceManager.missile_erector.renderPart("Pad")` 渲染底座，并按导弹规格切换 `ABM/Micro/V2/Strong/Huge/Atlas` 的 `*_Pad`、`*_Erector`、`*_Pivot`、`*_Rope` OBJ group；角度、lift 与待渲染导弹实体行为仍属于后续发射台 renderer/方块实体迁移，不在本轮渲染库基础入口内。
- `MissilePart.registerAllParts()` 真实注册 117 个导弹部件 item->模型/贴图映射；现代 `ObjMissilePartModels.parts()` 已与旧注册数量和 ID 完全一致，没有额外加入 1.7.10 未注册的贴图变体。

现代侧改动：

- 从 1.7.10 复制 `models/weapons/launch_pad_erector.obj` 到 `assets/hbm/models/block/launch_table/launch_pad_erector.obj`，复制 `pad.png` 与 6 张 `erector_*` 贴图到 `assets/hbm/textures/block/launch_table/`。
- `ObjLaunchModels` 新增 `MISSILE_ERECTOR = legacyModel("launch_pad_erector", "pad").asVBO()`，并补齐 `MISSILE_ERECTOR_*_TEXTURE` 贴图句柄；`ObjModelLibrary` 暴露 `MISSILE_ERECTOR`。
- `ObjModelLibrary` 显式暴露 43 个 `MISSILE_PART_MP_*` 模型门面，覆盖旧 `ResourceManager` 中所有 `mp_t_*`、`mp_s_*`、`mp_f_*`、`mp_w_*` OBJ 静态模型入口，方便后续装配台、发射台、GUI/物品 renderer 直接从总渲染库取模型。
- 本轮没有把 `missile_pad`、`missile_assembly`、`compact_launcher` 或 `launch_table_*` 改成 legacy VBO facade：旧端这些入口不是 `HFRWavefrontObject(...).asVBO()`，现代已有 baked/part 入口或需后续专门 renderer 迁移。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 武器/投射物零散 OBJ 入口补齐

1.7.10 对照：

- `ResourceManager` lines 882-883：`shimmer_sledge`、`shimmer_axe` 均为 `AdvancedModelLoader.loadModel("models/shimmer_*.obj")`；`ClientProxy` 将二者绑定到 `ItemRenderShim`，旧 renderer 按 `ResourceManager.shimmer_*_tex` 绑定贴图后 `renderAll()`。
- `ResourceManager` lines 1203-1206：`building` 为 `AdvancedModelLoader.loadModel("models/weapons/building.obj")`，`torpedo` 为普通 `new HFRWavefrontObject("models/weapons/torpedo.obj")`，`tom_main` 为 `AdvancedModelLoader.loadModel("models/weapons/tom_main.obj")`，`tom_flame` 为 `AdvancedModelLoader.loadModel("models/weapons/tom_flame.hmf")`。
- `RenderBoxcar` 对 `EntityBuilding` 使用 `building_tex + building.renderAll()`，对 `EntityTorpedo` 使用 `torpedo_tex + torpedo.renderAll()`，其中 torpedo 还会按存在 tick 做 pitch 旋转并临时切到 smooth shade。
- `TomPronter` 使用 `tom_main_tex + tom_main.renderAll()` 渲染主体；火焰层使用 `tom_flame_tex + tom_flame.renderAll()`，但旧模型格式为 `.hmf`，不属于本轮 OBJ/LegacyWavefrontModel 可承载范围。
- 旧 `ResourceManager` 还声明了 `pch_tex`、`bolter_digamma_tex`、`sky_stinger_tex`、`ff_gun_bright/dark/normal`，但本地 1.7.10 资源树没有对应贴图文件；本轮不生造这些资源，也不在现代渲染库暴露可用句柄。

现代侧改动：

- 从 1.7.10 复制 `shimmer_sledge.obj`、`shimmer_axe.obj`、`building.obj`、`torpedo.obj`、`tom_main.obj` 到 `assets/hbm/models/block/weapons/`，复制对应 `shimmer_*`、`building`、`torpedo`、`tom_main`、`tom_flame` 贴图到 `assets/hbm/textures/block/weapons/`。
- `ObjWeaponModels` 新增 `SHIMMER_SLEDGE`、`SHIMMER_AXE`、`BUILDING`、`TORPEDO`、`TOM_MAIN` 模型 facade，并补 `SHIMMER_*_TEXTURE`、`BUILDING_TEXTURE`、`TORPEDO_TEXTURE`、`TOM_MAIN_TEXTURE`、`TOM_FLAME_TEXTURE` 贴图句柄。
- `ObjModelLibrary` 暴露 `WEAPON_SHIMMER_SLEDGE`、`WEAPON_SHIMMER_AXE`、`WEAPON_BUILDING`、`WEAPON_TORPEDO`、`WEAPON_TOM_MAIN`。
- 本轮不迁 `tom_flame.hmf` 的模型 loader/渲染行为，也不迁 `ItemRenderShim`、`RenderBoxcar`、`TomPronter` 的具体矩阵动画；这些属于后续物品/实体/特效 renderer 移植。

验证：

- 待跑 `.\gradlew.bat compileJava processResources --no-daemon`。

## 2026-06-04 ResourceManager 贴图语义句柄补齐：车辆、反应堆、聚变

1.7.10 对照：

- `ResourceManager.cart_powder_tex` 不是车辆目录贴图，而是 `textures/blocks/block_gunpowder.png`；旧 `EntityMinecartPowder#renderSpecialContent(...)` 绑定它后只渲染 `cart_powder` 模型的 `Powder` part。
- `ResourceManager.cart_semtex_side` 为 `textures/blocks/semtex_side.png`，`cart_semtex_top` 实际为 `textures/blocks/semtex_bottom.png`；旧 `EntityMinecartSemtex#renderSpecialContent(...)` 分别渲染 `SemtexSide` / `SemtexTop` part。
- `reactor_small_base`、`reactor_small_rods` 为 `AdvancedModelLoader.loadModel(...)` 非 VBO；`breeder` 为 `HFRWavefrontObject(...)` 非 VBO；`icf`、`lpw2`、`watz`、`watz_pump`、`zirnox`、`zirnox_destroyed` 为 `.asVBO()`。
- 聚变 `fusion_torus`、`fusion_klystron`、`fusion_breeder`、`fusion_collector`、`fusion_boiler`、`fusion_mhdt`、`fusion_coupler`、`fusion_plasma_forge` 均为 `.asVBO()`；renderer 还直接绑定 `fusion_plasma`、`fusion_plasma_glow`、`fusion_plasma_sparkle` 等替换贴图。

现代侧改动：

- 复核现代 `textures/block/vehicles/cart_powder.png`、`cart_semtex_side.png`、`cart_semtex_top.png` 与旧 `block_gunpowder.png`、`semtex_side.png`、`semtex_bottom.png` 哈希一致；不重复复制贴图。
- `ObjVehicleModels` 增加旧语义别名 `CART_GUNPOWDER_BLOCK_TEXTURE`、`CART_SEMTEX_BOTTOM_BLOCK_TEXTURE`，明确现代车辆目录文件分别承载旧 block 贴图语义。
- `ObjReactorModels` 补齐旧 renderer 常用贴图句柄：`SMALL_BASE_TEXTURE`、`SMALL_RODS_TEXTURE`、`BREEDER_TEXTURE`、`ICF_TEXTURE`、`WATZ_TEXTURE`、`WATZ_PUMP_TEXTURE`、`LPW2_TEXTURE`、`ZIRNOX_TEXTURE`、`ZIRNOX_DESTROYED_TEXTURE`。
- `ObjFusionModels` 补齐旧 renderer 常用贴图句柄：`TORUS_TEXTURE`、`PLASMA_TEXTURE`、`PLASMA_GLOW_TEXTURE`、`PLASMA_SPARKLE_TEXTURE`、`KLYSTRON_TEXTURE`、`KLYSTRON_CREATIVE_TEXTURE`、`BREEDER_TEXTURE`、`COLLECTOR_TEXTURE`、`BOILER_TEXTURE`、`MHDT_TEXTURE`、`COUPLER_TEXTURE`、`PLASMA_FORGE_TEXTURE`。

勘误/边界：

- 本轮只补渲染库 facade 与贴图语义，不迁矿车、反应堆或聚变机器实体 renderer 逻辑。
- 现代资源路径已整理为 `textures/block/vehicles`、`textures/block/reactors`、`textures/block/fusion`；文档以 1.7.10 原 `ResourceManager` 路径作为事实来源，现代常量保留旧语义名以防后续误用。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 missile part / PA / RBMK ResourceManager facade 审计与贴图入口补齐

1.7.10 对照：

- `ResourceManager` 中导弹本体模型 `missileV2`、`missileABM`、`missileStealth`、`missileStrong`、`missileHuge`、`missileNuclear`、`missileMicro` 均为 `HFRWavefrontObject(...).asVBO()`，其中 `missileStealth` 额外 `.noSmooth()`；`missileShuttle`、`minerRocket` 为 `AdvancedModelLoader.loadModel(...)` 非 VBO。
- `ResourceManager` 中 39 个 `mp_*` 导弹零件模型均为 `AdvancedModelLoader.loadModel(...)` 非 VBO；现代 `ObjMissilePartModels` 保持非 VBO。
- `MissilePart.registerAllParts()` 共注册 117 个旧物品到模型、贴图、`height`、`guiheight`；现代 `ObjMissilePartModels.parts()` 数量、ID 集合、类型与高度值逐项对齐。
- 看似可疑但确认为旧端原样的点：`mp_thruster_15_kerosene_triple` / `mp_thruster_20_kerosene_triple` 使用 triple 模型但复用 dual 贴图；`mp_thruster_15_hydrogen` / `mp_thruster_15_hydrogen_dual` 复用 kerosene/dual 模型但绑定 hydrogen 贴图；`mp_stability_20_flat` 使用 `ResourceManager.universal`，`mp_warhead_15_boxcar` 使用 `ResourceManager.boxcar_tex`。
- Albion Particle Accelerator 六个模型 `pa_source`、`pa_beamline`、`pa_rfc`、`pa_quadrupole`、`pa_dipole`、`pa_detector` 均为 `HFRWavefrontObject(...).asVBO()`。
- RBMK 模型 flag 复核：`rbmk_element` 为 `new HFRWavefrontObject(..., true).noSmooth()`，现代以 `mixedMode().noSmooth()` 承载；`rbmk_element_rods`、`rbmk_rods` 同时保留非 VBO 与 `.asVBO()` 入口；crane/console/network 小件均 `.asVBO()`，`rbmk_debris` 为 `.noSmooth()`。

现代侧改动：

- `ObjMissilePartModels` 公开 `missileTexture(...)`、`missilePartTexture(...)`、`textureForPart(...)`，让后续 renderer 能通过旧语义名或旧 item id 查贴图，不在调用点硬写现代路径。
- `ObjParticleAcceleratorModels` 新增 `SOURCE_TEXTURE`、`BEAMLINE_TEXTURE`、`RFC_TEXTURE`、`QUADRUPOLE_TEXTURE`、`DIPOLE_TEXTURE`、`DETECTOR_TEXTURE`。
- `ObjModelLibrary` 导出 `MISSILE_PART_UNIVERSAL_TEXTURE`、`MISSILE_PART_BOXCAR_TEXTURE`、PA 六个贴图常量、RBMK 模型/图标贴图常量，并新增 `missileTexture(...)`、`missilePartTexture(...)`、`missilePartTextureForItem(...)`。

勘误/边界：

- 本轮不改变导弹零件的旧模型/贴图复用关系；旧端故意或历史遗留的复用关系按 1.7.10 原样保留。
- 本轮不新增不存在的 1.7.10 资源名；所有导出的贴图入口都指向已存在且哈希匹配的现代搬迁资源。
- `ObjModelLibrary` 中部分早前新增的 press/door/light/nuke/trinket/entity facade 出口仍保留，本轮只在其基础上补 missile/PA/RBMK 贴图语义出口。

验证：

- 导弹本体 OBJ、导弹零件 OBJ、导弹零件贴图、`universal`/`boxcar` 特殊贴图、PA OBJ/贴图、RBMK OBJ/旧模型贴图均与 1.7.10 源资源 SHA-256 匹配。
- `git diff --check -- src/main/java/com/hbm/ntm/client/obj/ObjMissilePartModels.java src/main/java/com/hbm/ntm/client/obj/ObjParticleAcceleratorModels.java src/main/java/com/hbm/ntm/client/obj/ObjModelLibrary.java` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 ResourceManager facade 审计：door/utility/launch 对齐

1.7.10 对照：

- `blast_door_base`、`blast_door_tooth`、`blast_door_slider`、`blast_door_block` 均为 `AdvancedModelLoader.loadModel(...)`，路径在 `models/*.obj`，不使用 `.asVBO()` / `.noSmooth()`；贴图为 `textures/models/blast_door_*.png`。
- `RenderFOEQ` 主体绑定 `sat_foeq_burning_tex` 并渲染 `sat_foeq_burning`；火焰模型 `sat_foeq_fire` 在 `GL_TEXTURE_2D` 禁用后用加色和颜色绘制，不依赖贴图。`getEntityTexture(...)` 返回 `sat_foeq_tex`，但当前 1.7.10 资源包没有 `textures/models/sat_foeq.png`，只有 `textures/blocks/sat_foeq.png`。
- `SoyuzLauncherPronter` 对六个 Soyuz launcher 模型分别绑定 `launcher_leg`、`launcher_table`、`launcher_tower_base`、`launcher_tower`、`launcher_support_base`、`launcher_support` 贴图；现代资源文件名整理为 `soyuz_launcher_*`，哈希与旧贴图一致。

现代侧改动：

- `ObjDoorModels` 新增 `BLAST_DOOR_BASE_LEGACY`、`BLAST_DOOR_TOOTH_LEGACY`、`BLAST_DOOR_SLIDER_LEGACY`、`BLAST_DOOR_BLOCK_LEGACY`，均保持旧端非 VBO 语义；并补四个贴图句柄。
- `ObjModelLibrary` 暴露四个普通 blast door legacy facade，供后续迁旧 `RenderDoorGeneric` / item renderer 时使用。
- `ObjLaunchModels` 补 `SOYUZ_LAUNCHER_LEG_TEXTURE`、`SOYUZ_LAUNCHER_TABLE_TEXTURE`、`SOYUZ_LAUNCHER_TOWER_BASE_TEXTURE`、`SOYUZ_LAUNCHER_TOWER_TEXTURE`、`SOYUZ_LAUNCHER_SUPPORT_BASE_TEXTURE`、`SOYUZ_LAUNCHER_SUPPORT_TEXTURE`，记录旧 `launcher_*` 贴图语义与现代 `soyuz_launcher_*` 文件的对应。

核查后不改：

- 不给 `ObjUtilityModels` 暴露 `sat_foeq_tex`，因为旧 `ResourceManager.sat_foeq_tex` 指向的 `textures/models/sat_foeq.png` 在当前 1.7.10 资源包不存在；现代端也不应生成假的 `textures/block/utility/sat_foeq.png`。
- `sat_foeq_fire` 继续复用 `SAT_FOEQ_BURNING_TEXTURE` 作为默认 facade 贴图；旧 renderer 真实火焰绘制时禁用纹理，该默认贴图只用于模型 facade 的安全默认值。
- `tesla`、`geiger`、`forcefield_top`、`sat_dock`、`file_cabinet` 现代贴图哈希与旧端实际 renderer 使用贴图一致，本轮不重复复制。

验证：

- 待跑 `git diff --check`。
- 待跑 `.\gradlew.bat compileJava processResources --no-daemon`。

## 2026-06-04 准星信息 overlay 已注册可见多方块端口补齐

1.7.10 对照：

- 继续全量复扫旧 `ILookOverlay`，本轮只迁现代端已有注册方块/BlockEntity 且信息不依赖缺失 runtime 的部分。
- `MachineChemicalFactory#printHook(...)` 与 `MachineAssemblyFactory#printHook(...)`：
  - 命中 `getCoolPos()` 端口外侧相邻块时显示两行：`-> Water`、`<- Spent Steam`。
  - 命中 `getIOPos()` 端口外侧相邻块时显示 `-> Recipe field [1-4]`。
  - 端口坐标来自旧 `TileEntityMachineChemicalFactory#getCoolPos/getIOPos` 与 `TileEntityMachineAssemblyFactory#getCoolPos/getIOPos`，两者公式一致。
- `MachineTurbineGas#printHook(...)`：
  - 旧 `hitCheck(...)` 是命中端口方块本身，不是端口外侧相邻块。
  - `(-1,-1,0)` / `(1,-1,0)` 端口显示 `-> Gas`、`-> Lubricant`。
  - `(-1,4,0)` / `(1,4,0)` 端口显示 `-> Water`。
  - 旧 `hitCheck(dir, ..., 0, 5, 1, ...)` 端口显示 `<- Hot Steam`；现代按 `legacyUpSide(facing)` 折算为 offset `(0,-5,1)`。
  - 旧 `hitCheck(dir, ..., 0, -4, 1, ...)` 端口显示 `<- Power`；现代按 `legacyUpSide(facing)` 折算为 offset `(0,4,1)`。
  - 现代布局使用 `LegacyMultiblockOffsets.legacyUpSide(facing)`，前四个端口与旧参数同号，后两个 side 轴因旧 `getRotation(DOWN)` 与现代布局轴相反需要折算。

现代侧改动：

- `LegacyVisibleMachineBlockEntity` 实现 `LegacyLookOverlayProvider`，但只对白名单旧端确有可静态对齐 overlay 的可见多方块返回内容：
  - `machine_chemical_factory` / `machine_assembly_factory` 复用 `LegacyLookOverlayPorts.factoryMachinePort(...)`。
  - `machine_turbinegas` 复用新增 `LegacyLookOverlayPorts.turbineGasPort(...)`。
- `LegacyLookOverlayLines` 新增 `fluidPorts(...)` 与 `powerPort(...)`，供端口型机器共享输出，不把机器分支写进 renderer。
- `LegacyLookOverlayPorts` 保持两种旧匹配语义：
  - factory 机器继续用 `LegacyLookOverlayPort#matches(...)`，对应旧端 `port.compare(x + dir.offsetX, y, z + dir.offsetZ)`。
  - gas turbine 用 `exactMatches(...)`，对应旧端 `hitCheck(...)` 直接比较命中方块坐标。

缺口矩阵/边界：

- 已覆盖且现代有真实承载：流体管网管/阀/开关/锚/盒/计量管/计数阀/泵、exhaust 三烟气、machine battery/socket、red cable gauge、heat boiler/steam engine/solar boiler/cooling tower/fraction tower/catalytic cracker/industrial turbine、assembly machine/chemical plant 的旧端端口提示、以及本轮三台可见多方块端口提示。
- 旧端有 `ILookOverlay` 但现代仅为模型/可见多方块空壳，暂不迁动态内容：`machine_sawmill`、`machine_rotary_furnace`、`machine_stirling`、`machine_hephaestus`、`machine_strand_caster`、`machine_igenerator`、`MachineHeatBoilerIndustrial` 等；这些旧文本读取热量、库存、进度、转速、blade、流体类型或 powerBuffer，现代无对应 runtime 时不能生造。
- 旧端有 `ILookOverlay` 但现代尚未注册/未迁系统：`CableDiode`、`MachineCapacitor/FENSU`、converter RF/HE、deuterium tower/extractor、drain/intake/condenser、RBMK、wand/guide/storage crate/drone/crane/radio torch、rail car entity 等。
- 旧端确认为 repair-only 或无 overlay 的机器继续不显示猜测 tank overlay：`machine_fluidtank`、`machine_refinery`；`MachineCompressor/Liquefactor/GasFlare/Coker/Hydrotreater/CatalyticReformer/VacuumDistill` 无旧常驻 `ILookOverlay`。

验证：

- `git diff --check -- src/main/java/com/hbm/ntm/api/block/LegacyLookOverlayLines.java src/main/java/com/hbm/ntm/api/block/LegacyLookOverlayPorts.java src/main/java/com/hbm/ntm/blockentity/LegacyVisibleMachineBlockEntity.java 工程记录/库移植/2026-05-20-render-library-1.7.10源码功能追踪.md` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay 可见多方块端口第二轮

1.7.10 对照：

- 继续复扫现代已注册的旧 `ILookOverlay` 可见多方块。
- `MachineRotaryFurnace#printHook(...)` 旧端使用 `hitCheck(...)` 直接命中端口方块本体：
  - `(-1,-1,0)` / `(-1,-2,0)` 显示 `-> Steam` 与 `<- Spent Steam`；这两个 tank 在旧 `TileEntityMachineRotaryFurnace` 构造器中固定为 `Fluids.STEAM`、`Fluids.SPENTSTEAM`。
  - `(1,1,0)` 显示 `-> Fuel`。
  - `(1,2,0)` / `(-1,2,0)` 显示 `furnace.tanks[0].getTankType().getLocalizedName()`；该输入流体由旧运行时配方/物品槽动态决定，现代 `machine_rotary_furnace` 目前只是 `LegacyVisibleMachineBlockEntity` 空壳，不能猜测固定流体。

现代侧改动：

- `LegacyLookOverlayLines` 新增 `itemPort(...)`，用于旧端端口用途类文字，例如 `-> Fuel`。
- `LegacyLookOverlayPorts` 新增 `rotaryFurnacePort(...)`：
  - 使用旧 `hitCheck` 的精确方块命中语义。
  - 只返回旧端可静态确定的蒸汽输入/废蒸汽输出与燃料口。
  - 不迁动态流体输入口，等待 rotary furnace runtime/tank/配方系统迁入后再接。
- `LegacyVisibleMachineBlockEntity` 将 `machine_rotary_furnace` 接入 `rotaryFurnacePort(...)`；renderer 仍不包含机器硬编码。

本轮复核的未迁缺口：

- `MachineSawmill`、`MachineStirling`、`MachineConveyorPress`、`MachineHeatBoilerIndustrial`、`MachineHephaestus`、`MachineStrandCaster`、`MachineChungus` 的旧 overlay 读取热量、powerBuffer、库存、进度条、blade/gear/mold、具体流体量或 power 输出；现代端未迁对应 BE runtime 时继续不显示，避免猜测。
- `MachineTurbofan` 旧端不实现 `ILookOverlay`，现代 `machine_turbofan` 不应新增准星文字。
- `MachineIGenerator` 旧端有固定 memorial overlay，但现代未注册对应方块，本轮不迁。

验证：

- `git diff --check -- src/main/java/com/hbm/ntm/api/block/LegacyLookOverlayLines.java src/main/java/com/hbm/ntm/api/block/LegacyLookOverlayPorts.java src/main/java/com/hbm/ntm/blockentity/LegacyVisibleMachineBlockEntity.java 工程记录/库移植/2026-05-20-render-library-1.7.10源码功能追踪.md` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 飞行器/Boxcar/火箭实体 OBJ 入口补齐

1.7.10 对照：

- `ResourceManager` lines 1201-1202：`boxcar`、`duchessgambit` 均为 `AdvancedModelLoader.loadModel(...)`；`RenderBoxcar` 分别使用 `boxcar_tex`、`duchessgambit_tex` 后 `renderAll()`，`RenderDecoBlock` 与 `ItemRenderLibrary` 也复用这两个模型入口。
- `ResourceManager` lines 1215-1216：`dornier`、`b29` 均为 `AdvancedModelLoader.loadModel(...)`；`RenderBomber` 按实体 data watcher byte 16 切换整机贴图：`0/1/3/default -> dornier_1`，`2 -> dornier_2`，`4 -> dornier_4`，`5 -> b29_0`，`6 -> b29_1`，`7 -> b29_2`，`8 -> b29_3`。旧端不是 OBJ part 分贴图。
- `ResourceManager` lines 1227-1228：`missileShuttle`、`minerRocket` 均为 `AdvancedModelLoader.loadModel(...)`；`RenderMissileShuttle` 绑定 `missileShuttle_tex` 后渲染，`RenderMinerRocket` 对矿工火箭绑定 `minerRocket_tex`，对 Bobmazon 分支绑定另一个贴图但仍复用 `minerRocket` 模型。
- `ResourceManager` line 1207 还声明 `nikonium = AdvancedModelLoader.loadModel("models/nikonium.obj")`，`RendererObjTester` 会测试渲染；但对应 `textures/models/misc/nikonium.png` 在本地 1.7.10 资源树中缺失。本轮不暴露现代 `nikonium` facade，避免后续调用得到缺贴图模型。

现代侧改动：

- 从 1.7.10 复制 `boxcar.obj`、`duchessgambit.obj`、`dornier.obj`、`b29.obj` 到 `assets/hbm/models/block/entities/`，复制对应 `boxcar`、`duchessgambit`、`dornier_1/2/4`、`b29_0/1/2/3` 贴图到 `assets/hbm/textures/block/entities/`。
- 从 1.7.10 复制 `missileShuttle.obj`、`minerRocket.obj` 到现代小写路径 `assets/hbm/models/block/missiles/missile_shuttle.obj`、`miner_rocket.obj`，复制 `missile_shuttle.png`、`minerRocket.png` 到现代 `missile_shuttle.png`、`miner_rocket.png`。
- `ObjEntityModels` 新增 `BOXCAR`、`DUCHESS_GAMBIT`、`DORNIER`、`B29` 模型 facade 与旧贴图句柄；`ObjMissilePartModels` 新增 `MISSILE_SHUTTLE`、`MINER_ROCKET` 模型 facade 与贴图句柄；`ObjModelLibrary` 暴露对应 `ENTITY_*` / `MISSILE_*` 总门面。
- 本轮只接基础渲染库入口，不迁 `RenderBoxcar`、`RenderBomber`、`RenderMissileShuttle`、`RenderMinerRocket` 的矩阵、旋转、实体数据同步和 Bobmazon 分支贴图。

验证：

- 待跑 `.\gradlew.bat compileJava processResources --no-daemon`。

## 2026-06-03 ISBRH 方块/贴附炸药/碎片入口复核

1.7.10 对照：

- `ResourceManager` lines 1598-1615、1669-1671：`scaffold`、`taperecorder`、`beam`、`barrel`、`pole`、`barbed_wire`、`spikes`、`antenna_top`、`conservecrate`、`pipe`、`pipe_rim`、`pipe_quad`、`pipe_frame`、`rtty`、`crt`、`toaster`、`deco_computer`、`hev_battery`、`anvil`、`crystal_*`、`cable_neo`、`pipe_neo`、`difurnace_extension`、`splitter`、`crane_buffer`、`rail_*`、`capacitor`、`funnel`、`charge_dynamite`、`charge_c4` 均在旧端以 `HFRWavefrontObject` 加载，其中除 `pipe_neo` 外均为 `.noSmooth()`，`skeleton_holder` 另有 `.noSmooth().asVBO()`。
- `ClientProxy` lines 783、805-806：`deco_computer` 使用 `RenderBlockDecoModel`，`charge_dynamite`、`charge_c4` 使用 `RenderBlockRotated`。
- `RenderBlockDecoModel`、`RenderBlockRotated` 不是绑定固定 `ResourceLocation` 贴图，而是取方块 `getIcon(...)` 并调用 `ObjUtil.renderWithIcon(...)`；因此现代 `ObjBlockModels` 中的默认 texture 只能作为基础 facade/预览贴图，后续真正迁 renderer 时仍需按旧端 `IIcon`/sprite 路径动态喂贴图。
- `ModBlocks` line 1586：`deco_computer` 方块纹理名为 `hbm:deco_computer`，不是 CRT 的 `crt_clean`。旧 `DecoComputerEnum` 仅有 `IBM_300PL` 一个变体。
- `ResourceManager` lines 1674-1685 与 `RenderRBMKDebris`、`RenderZirnoxDebris`：RBMK 六种碎片和 Zirnox 五种碎片均为 `AdvancedModelLoader.loadModel(...)`，现代 `ObjProjectileModels` 已有 `DEBRIS_*`、`ZIRNOX_DEBRIS_*` facade，且贴图句柄覆盖 `rbmk_*`、`block_graphite`、`zirnox`、`zirnox_destroyed`、`zirnox_deb_element`。

现代侧修正：

- 补入 1.7.10 原始 `textures/blocks/deco_computer.png` 到 `assets/hbm/textures/block/legacy_blocks/deco_computer.png`。
- `ObjBlockModels.DECO_COMPUTER` 默认贴图由 `crt_clean` 改为 `deco_computer`，与旧 `BlockDecoModel#setBlockTextureName(...)` 对齐。
- 复核确认：`charge_dynamite`、`charge_c4`、`charge_thrower`、RBMK/Zirnox debris 的模型与贴图资源入口已存在；本轮不重复复制，也不把旧 `ObjUtil.renderWithIcon` 的动态 sprite 行为压成固定单贴图 renderer。

验证：

- `git diff --check -- src/main/java/com/hbm/ntm/client/obj/ObjMachineModels.java src/main/java/com/hbm/ntm/client/obj/ObjModelLibrary.java 工程记录/库移植/2026-05-20-render-library-1.7.10源码功能追踪.md ...tank resources` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 发射台/导弹装配台 legacy facade 补齐

1.7.10 对照：

- `ResourceManager` lines 1240-1253：`missile_pad`、`missile_assembly`、`strut`、`compact_launcher`、`launch_table_base`、`launch_table_large_pad`、`launch_table_small_pad`、六个 `launch_table_*_scaffold_*` 均为 `AdvancedModelLoader.loadModel(...)`；只有同段 `missile_erector` 是 `HFRWavefrontObject(...).asVBO()`。因此本轮新增入口全部保持非 VBO。
- `ResourceManager` lines 1433-1451：贴图路径分别为 `textures/models/launchpad/silo(.rusted).png`、`textures/models/missile_assembly.png`、`textures/models/strut.png`、`textures/models/compact_launcher.png`、`textures/models/missile_parts/launch_table*.png`。
- `RenderLaunchPad` / `RenderLaunchPadRusted` 使用同一个 `missile_pad` 模型，按普通/锈蚀发射井切换 `silo` / `silo_rusted` 贴图。
- `RenderMissileAssembly` 使用 `missile_assembly` 渲染主体，并按装载导弹高度重复渲染 `strut`。
- `RenderCompactLauncher` 使用 `compact_launcher_tex + compact_launcher.renderAll()`。
- `RenderLaunchTable` 使用 `launch_table_base`、按 `padSize` 切换 small/large pad，并按高度重复渲染 scaffold。旧端 `launch_table_large_scaffold_empty` 绑定 `launch_table_large_scaffold_base_tex`，`launch_table_small_scaffold_empty` 绑定 `launch_table_small_scaffold_base_tex`，不是独立 empty 贴图。

现代侧改动：

- 从 1.7.10 复制 `launch_pad_silo.obj`、`missile_assembly.obj`、`strut.obj`、`compact_launcher.obj` 到 `assets/hbm/models/block/launch_table/`。
- 从 1.7.10 复制 `silo.png`、`silo_rusted.png`、`missile_assembly.png`、`strut.png`、`compact_launcher.png` 到 `assets/hbm/textures/block/launch_table/`。
- `ObjLaunchModels` 新增 `MISSILE_PAD`、`MISSILE_ASSEMBLY`、`STRUT`、`COMPACT_LAUNCHER` 与全部 `LAUNCH_TABLE_*_LEGACY` 普通 legacy facade；保留既有 baked `ObjModelPart` 入口供现代 block model 继续使用。
- `ObjLaunchModels` 补齐上述贴图 `ResourceLocation` 常量，其中两个 scaffold empty legacy facade 默认复用 base 贴图，贴合旧 renderer 绑定行为。
- `ObjModelLibrary` 暴露 `MISSILE_PAD`、`MISSILE_ASSEMBLY`、`LAUNCH_STRUT`、`COMPACT_LAUNCHER` 与全部 `LAUNCH_TABLE_*_LEGACY` 总门面。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 准星信息 overlay API 顺序对齐

1.7.10 对照：

- `ModEventHandlerClient#onOverlayRender` 在 `ElementType.CROSSHAIRS && ClientConfig.DODD_RBMK_DIAGNOSTIC` 时调用准星信息逻辑。
- 旧端解析顺序为：手持物品实现 `ILookOverlay` 时优先调用；否则若命中方块实现 `ILookOverlay` 则调用方块；否则命中实体实现 `ILookOverlay` 时调用实体。
- `ILookOverlay.printGeneric(...)` 允许只显示标题、正文列表为空；例如 `BlockRemap#printHook` 标题为 `Compatibility block, will convert on update tick.`，正文为空。
- 旧端方块级 overlay 会由方块自己查找 dummy/core，例如 `BlockDummyable#findCore(...)` 后读取 core TE；现代侧等价路径应仍通过共享 overlay renderer + multiblock core 解析，不在 renderer 中硬写每台机器。

现代侧改动：

- 新增 `LegacyLookOverlayBlockProvider`，允许没有 BlockEntity 或需要以命中方块为主体的方块直接提供准星 overlay。
- 新增 `LegacyLookOverlayItemProvider`，允许结构工具、传送带工具等旧 `Item implements ILookOverlay` 的手持物品以后按旧端优先级接入。
- `LegacyLookOverlayRenderer` 的解析顺序改为手持物品 -> 命中方块 -> 解析 multiblock core BlockEntity，与 1.7.10 的 block hit 分支顺序对齐；实体分支暂未迁移，等待对应实体库/命中解析迁移。
- `LegacyLookOverlay` 新增 `forBlockState(...)` 与 `forItem(...)` 便捷工厂；renderer 不再因为正文为空而跳过 overlay，以兼容 `BlockRemap` 这类只有标题的旧 overlay。

勘误/边界：

- `machine_turbine` 在 1.7.10 是 `MachineTurbine extends BlockContainer implements ITooltipProvider`，不是 `ILookOverlay`；现代 `SteamTurbineBlockEntity` 虽然有 tank/产能状态，本轮不新增常驻准星文字。
- `machine_compressor` / `machine_compressor_compact` 的旧 `MachineCompressor` 不实现 `ILookOverlay`；现代 `CompressorBlockEntity` 不应因继承流体/能量基类而显示 tank overlay。
- `machine_fluidtank` 旧端 `printHook` 仅调用 `IRepairable.addGenericOverlay(...)`，常规 tank 信息不显示；现代 `FluidTankBlockEntity` 保持无常驻 overlay。
- 电缆流量计 `BlockCableGauge` 与电缆二极管 `CableDiode` 旧端有 `HE/t`、`HE/s`、`Max.`、`Priority` overlay，但现代注册目前只有 `red_cable`，没有对应 BlockEntity/方块承载点；本轮只记录，不生造现代行为。

验证：

- 待跑 `.\gradlew.bat compileJava processResources --no-daemon`。

## 2026-06-03 车辆复核与 Taint Crab 实体入口补齐

1.7.10 对照：

- `ResourceManager` lines 1302-1306：`cart`、`cart_destroyer`、`cart_powder`、`train_cargo_tram`、`train_cargo_tram_trailer` 均为 `AdvancedModelLoader.loadModel(...)`。
- `ResourceManager` lines 1582-1590：矿车贴图来自 `textures/entity/cart_*`，粉末/semtex 变体复用 `textures/blocks/block_gunpowder.png`、`semtex_side.png`、`semtex_bottom.png`；tram 贴图来自 `textures/models/trains/tram*.png`。
- `RenderNeoCart` 使用 `cart.renderPart("Carriage")` 和 `cart.renderPart("Bucket")`，按 cart 类型切换金属/裸金属/木质贴图；`RenderTrainCargoTram` 与 trailer renderer 分别使用 `train_tram` / `tram_trailer`。
- 现代 `ObjVehicleModels` 与 `ObjModelLibrary` 已有上述五个模型 facade 和全部车辆贴图句柄，本轮只复核记录，不重复复制。
- `ResourceManager` lines 383、820：`taintcrab = AdvancedModelLoader.loadModel("models/mobs/taintcrab.obj")`，贴图为 `textures/entity/taintcrab.png`，另有 `taintcrab_clean.png` 资源。
- `ModelTaintCrab` 按 `Body`、`Legs1`、`Legs2` 三个 OBJ part 渲染，并用 `limbSwing` 旋转两组腿；`RenderTaintCrab` 还会按实体 `targets` 绘制随机波形 beam。上述动画/beam 属于后续实体 renderer 迁移，不在本轮渲染库基础入口内。

现代侧改动：

- 从 1.7.10 复制 `models/mobs/taintcrab.obj` 到 `assets/hbm/models/block/entities/taintcrab.obj`。
- 从 1.7.10 复制 `textures/entity/taintcrab.png`、`taintcrab_clean.png` 到 `assets/hbm/textures/block/entities/`。
- `ObjEntityModels` 新增 `TAINTCRAB` facade 与 `TAINTCRAB_TEXTURE`、`TAINTCRAB_CLEAN_TEXTURE` 贴图句柄；`ObjModelLibrary` 暴露 `ENTITY_TAINTCRAB`。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 效果 renderer Ring/EMP/BlackHole 与核弹替换贴图入口补齐

1.7.10 对照：

- `RenderEMPBlast` 私有加载 `models/Ring.obj`，贴图为 `textures/models/EMPBlast.png`，由 `AdvancedModelLoader.loadModel(...)` 加载，不是 VBO。
- `RenderBlackHole`、`RenderCloudRainbow`、`RenderDeathBlast` 复用 `models/Sphere.obj`；现代端已有 `ObjEffectModels.SPHERE`，本轮只复核不重复复制。
- `RenderBlackHole` 绑定 `textures/models/BlackHole.png`。
- `RenderFallingNuke` 使用 `models/LilBoy1.obj` 与 `textures/models/CustomNuke.png`；现代端 `custom_nuke.png` 哈希已与旧端一致。
- `RenderFallingNuke#getEntityTexture(...)` 返回 `textures/models/TheGadget3_.png`，这是旧 renderer 的备用/实体贴图入口，与现代 `gadget.png` 哈希不同。

现代侧改动：

- 从 1.7.10 原资源复制 `Ring.obj` 到 `assets/hbm/models/block/effects/ring.obj`。
- 从 1.7.10 原资源复制 `EMPBlast.png`、`BlackHole.png` 到 `assets/hbm/textures/block/effects/emp_blast.png`、`black_hole.png`。
- `ObjEffectModels` 新增 `RING`、`EMP_BLAST_TEXTURE`、`BLACK_HOLE_TEXTURE`，并新增带贴图参数的 effect model helper；`ObjModelLibrary` 暴露 `EFFECT_RING`。
- 从 1.7.10 原资源复制 `TheGadget3_.png` 到 `assets/hbm/textures/block/nuke/gadget_legacy.png`；`ObjNukeModels` 新增 `CUSTOM_NUKE_TEXTURE` 与 `GADGET_LEGACY_TEXTURE` 贴图句柄。

勘误/边界：

- `BombGenericLarge.obj` 与 `BombGenericLargeLayout.png` 被旧 `RenderBombMultiLarge` 引用，但当前 1.7.10 资源包未发现对应文件；按“不生造资源”规则不迁。
- `RenderMinecartTest` 引用 `textures/models/LilBoy2.png`，但当前 1.7.10 资源包未发现该贴图；不补假贴图。
- 本轮只补渲染库资源与 facade，不迁 EMP/黑洞实体逻辑或现代 shader 行为。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 trinket/entity/projectile 私有 OBJ renderer 入口补齐

1.7.10 对照：

- `RenderBobble` 私有加载 `models/trinkets/bobble.obj`，贴图包含 `socket.png`、`glow.png` 与全部 bobble 变体贴图；该模型由 `AdvancedModelLoader.loadModel(...)` 加载，不是 VBO。
- `RenderPlushie` 私有加载 `yomi.obj`、`hundun.obj`、`derg.obj`，三者均为 `new HFRWavefrontObject(..., false).asVBO()`，贴图为同名 trinket 贴图；`NUMBERNINE` 复用 horse renderer 与 `textures/models/horse/numbernine.png`，本轮不重复迁 horse。
- `RenderSnowglobe` 私有加载 `models/trinkets/snowglobe.obj`，旧端 `.asVBO()`，贴图分为 `snowglobe.png`、`snowglobe_glass.png`、`snowglobe_features.png`。
- `RenderCoin` 私有加载 `models/trinkets/chip.obj`，旧端 `.asVBO()`，贴图为 `chip_gold.png`。
- `RenderWormHead` / `RenderWormBody` 私有加载 `models/mobs/bot_prime_head.obj` / `bot_prime_body.obj`，贴图为 `textures/entity/mark_zero_head.png` / `mark_zero_body.png`，无 `.asVBO()`。
- `RenderPlasticBag` 私有加载 `models/mobs/plasticbag.obj`，贴图为 `textures/entity/plasticbag.png`，旧端 `AdvancedModelLoader.loadModel(...)` 非 VBO。
- `RenderSiegeTunneler` 私有加载 `models/mobs/tunneler.obj`，旧代码按 `textures/entity/siege_drill_<tier>.png` 动态拼贴图；当前 1.7.10 资源包没有这些 tier 贴图文件。
- `RenderBalls` 私有加载 `models/mobs/capsule.obj`，贴图使用 `ResourceManager.universal_bright`，也就是 `textures/models/turbofan_blades.png`。
- `RenderBombletTheta` 私有加载 `models/bombletTheta.obj`；旧端实际注册点只把 `EntityBombletZeta` 交给该 renderer，zeta 分支使用 `textures/models/bombletZetaTexture.png`。

现代侧改动：

- 从 1.7.10 原资源复制 trinket 旧 OBJ 为 `bobble_legacy.obj`、`yomi_legacy.obj`、`hundun_legacy.obj`、`derg_legacy.obj`、`snowglobe_legacy.obj`、`chip_legacy.obj`，避免覆盖现代已存在但哈希不同的拆分/baked 资源。
- `ObjTrinketModels` 新增上述 legacy facade，并按旧端 `.asVBO()` 使用情况只给 plushie、snowglobe、coin 三类设置 `.asVBO()`；bobble 保持普通非 VBO。
- `ObjTrinketModels` 补齐 bobble、plushie、snowglobe、chip 旧 renderer 直接绑定的贴图句柄，并补 `FLUORESCENT_LAMP_TEXTURE` 供 bobble MELLOW 发光层后续复用。
- 从 1.7.10 原资源复制 `bot_prime_head.obj`、`bot_prime_body.obj`、`plasticbag.obj`、`tunneler.obj`、`capsule.obj` 到 `assets/hbm/models/block/entities/`，并复制存在的 `mark_zero_head.png`、`mark_zero_body.png`、`plasticbag.png`。
- `ObjEntityModels` 新增 `BOT_PRIME_HEAD`、`BOT_PRIME_BODY`、`PLASTIC_BAG`、`TUNNELER`、`CAPSULE`；其中 `CAPSULE` 按旧 `ResourceManager.universal_bright` 显式绑定现代 `textures/block/machines/turbofan_blades.png`。
- 从 1.7.10 原资源复制 `bombletTheta.obj` 为 `models/block/projectiles/bomblet_theta.obj`，复制 `bombletZetaTexture.png` 为 `textures/block/projectiles/bomblet_zeta.png`。
- `ObjProjectileModels` 新增 `BOMBLET_ZETA` 与 `BOMBLET_ZETA_TEXTURE`，`ObjModelLibrary` 暴露本轮新增 trinket/entity/projectile facade。

勘误/边界：

- 不迁 `RenderSiegeTunneler` 的 `siege_drill_<tier>.png` 贴图句柄，因为当前 1.7.10 资源包没有这些动态路径对应文件；只保留 `TUNNELER` 模型入口，后续若修旧端资源缺口需单独确认来源。
- 不新增 `bombletThetaTexture.png` 现代贴图，因为 1.7.10 资源包没有该文件，且旧端实际注册的 `EntityBombletZeta` 会走存在的 zeta 贴图分支。
- 不重复迁 `RenderPlushie` 的 `NUMBERNINE` horse 资源；现代已有 `LegacyHorseRenderer` 与 horse 贴图入口。
- 本轮只补渲染库资源/facade，不迁 bobble/plushie/snowglobe 的完整 TESR 矩阵、文字标签、额外手持物品渲染或实体注册行为。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 小型 utility/trinket/卫星入口复核

1.7.10 对照：

- `ResourceManager` lines 375、814-815：`lantern = HFRWavefrontObject("models/trinkets/lantern.obj").noSmooth()`，贴图句柄包含 `textures/models/trinkets/lantern.png` 与 `lantern_rusty.png`。
- `ResourceManager` lines 296-300、766-776：FOEQ 卫星燃烧/火焰模型和卫星 dock 均为 `AdvancedModelLoader.loadModel(...)`；`RenderFOEQ` 绑定 `sat_foeq_burning_tex` 后渲染 `sat_foeq_burning`，火焰层继续复用同一张 `sat_foeq_burning_tex` 渲染 `sat_foeq_fire`。
- 旧 `ResourceManager.sat_foeq_tex` 声明为 `textures/models/sat_foeq.png`，但本地 1.7.10 资源树没有该文件；资源树实际存在的是 `textures/blocks/sat_foeq.png` 与 `textures/items/sat_foeq.png`。本轮不把缺失的 `textures/models/sat_foeq.png` 生造为现代可用模型贴图句柄。
- 现代 `ObjUtilityModels` 已有 `SAT_FOEQ_BURNING`、`SAT_FOEQ_FIRE`、`SAT_DOCK` 和总库 facade；现代 `ObjTrinketModels` 已有 `LANTERN` 模型 facade。

现代侧改动：

- `ObjTrinketModels` 补齐 `LANTERN_TEXTURE` 与 `LANTERN_RUSTY_TEXTURE`，供后续灯笼 renderer 按旧端普通/锈蚀变体切换贴图。
- `ObjUtilityModels` 增加 `SAT_FOEQ_FIRE_TEXTURE = SAT_FOEQ_BURNING_TEXTURE` 显式别名，记录旧 `RenderFOEQ` 火焰层复用燃烧贴图的资源合同。
- 本轮只补资源句柄和复核记录，不迁 `RenderFOEQ` 的矩阵、旋转、四向火焰层或 `RendererObjTester` 调试渲染行为。

验证：

- `git diff --check -- src/main/java/com/hbm/ntm/client/obj/ObjTrinketModels.java src/main/java/com/hbm/ntm/client/obj/ObjUtilityModels.java 工程记录/库移植/2026-05-20-render-library-1.7.10源码功能追踪.md` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 UF6/PUF6 小罐模型入口补齐与 bomb_multi 复核

1.7.10 对照：

- `ResourceManager` line 165：`tank = AdvancedModelLoader.loadModel("models/tank.obj")`，不是 `.asVBO()`。
- `ResourceManager` lines 617-618：小罐贴图为 `textures/models/UF6Tank.png` 与 `textures/models/PUF6Tank.png`。
- `RenderUF6Tank` / `RenderPuF6Tank` 在世界中按方块 metadata 旋转后分别绑定 `uf6_tex` / `puf6_tex`，再 `ResourceManager.tank.renderAll()`。
- `ItemRenderLibrary` 对对应物品也复用同一个 `ResourceManager.tank`，按物品类型切换 UF6/PUF6 贴图。
- `ResourceManager` line 288 的 `bomb_multi = AdvancedModelLoader.loadModel("models/BombGeneric.obj")` 与 `bomb_multi_tex` 已在现代资源中以 `models/block/nuke/bomb_multi.obj`、`textures/block/nuke/bomb_multi.png` 存在，贴图 hash 与 1.7.10 `BombGeneric.png` 一致。但现代 OBJ 已为 block model/baked 路径做了坐标适配，不在本轮新增旧 TESR facade，避免和旧 `RenderBombMulti` 的 `x + 0.5, y + 0.5, z + 0.5` 原点规则混用。
- `RenderBombMultiLarge` 的 `BombGenericLarge.obj` / `BombGenericLargeLayout.png` 是 renderer 内部私有加载，不是 `ResourceManager` 静态 facade；本轮不迁。

现代侧改动：

- 从 1.7.10 复制 `models/tank.obj` 到 `assets/hbm/models/block/machines/tank.obj`。
- 从 1.7.10 复制 `textures/models/tank.png`、`UF6Tank.png`、`PUF6Tank.png` 到 `assets/hbm/textures/block/machines/tank.png`、`uf6_tank.png`、`puf6_tank.png`。
- `ObjMachineModels` 新增 `TANK = legacyModel("tank")` 普通非 VBO facade，并补 `TANK_TEXTURE`、`UF6_TANK_TEXTURE`、`PUF6_TANK_TEXTURE` 贴图句柄。
- `ObjModelLibrary` 暴露 `MACHINE_TANK`，供后续 UF6/PUF6 小罐方块实体 renderer 或物品 renderer 使用。

验证：

- `git diff --check -- src/main/java/com/hbm/ntm/client/obj/ObjMachineModels.java src/main/java/com/hbm/ntm/client/obj/ObjModelLibrary.java 工程记录/库移植/2026-05-20-render-library-1.7.10源码功能追踪.md ...tank resources` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-03 准星信息 overlay API 继续对齐

1.7.10 对照：

- `ILookOverlay#printGeneric(RenderGameOverlayEvent.Pre, String title, int titleCol, int bgCol, List<String> text)` 由每个实现传入标题颜色和标题阴影颜色；常规值为 `0xffff00 / 0x404000`，但 `MachineIGenerator` 使用 `0xff8000 / 0x804000`，`MachineStrandCaster` 与 foundry casting/outlet 使用 `0xFF4000 / 0x401000`。
- 旧正文行可用字符串前缀 `&[<color>&]` 覆盖单行颜色；现代侧以 `Component` 样式承载，不复刻脆弱字符串协议，但需要提供通用 colored helper。
- `ClientConfig.DODD_RBMK_DIAGNOSTIC` 旧默认值为 `true`，是整个准星信息分支的开关；`SHOW_BLOCK_META_OVERLAY` 旧默认值为 `false`，仅为 debug meta overlay，本轮不迁 debug 分支。
- `BlockRemap` 证明旧 overlay 可以只有标题且正文为空。

现代侧改动：

- `LegacyLookOverlay` 新增自定义标题颜色工厂 `withTitle(title, titleColor, titleShadowColor, lines)`，并补 `titleOnly(...)` 便捷入口，供后续兼容块、纪念牌、熔铸系等旧非黄色标题 overlay 接入。
- `LegacyLookOverlayLines` 新增 `colored(message, color)`，作为旧 `&[color&]` 单行颜色协议的现代等价入口。
- `HbmClientConfig` 新增 `hud.legacyLookOverlay`，默认 `true`，注释明确对应旧 `ClientConfig.DODD_RBMK_DIAGNOSTIC`。
- `LegacyLookOverlayRenderer` 在解析命中目标前读取 `HbmClientConfig.LEGACY_LOOK_OVERLAY`，恢复旧端总开关语义。

勘误/边界：

- 现代 `LegacyRemoteFluidMachineBlockEntity` 是共享油处理/远程流体机器基类，但旧端并非所有油处理机器都有 `ILookOverlay`。已复核：`MachineCatalyticCracker` 有 overlay；`MachineCatalyticReformer`、`MachineHydrotreater`、`MachineCoker`、`MachineVacuumDistill`、`MachineGasFlare` 没有 `ILookOverlay`，后续不得因为现代 BE 有 tank 就统一显示常驻准星文字。
- 现代资源已有 `strand_caster.obj` facade，但尚无对应方块/BlockEntity 行为承载；本轮只补 API，不迁 `MachineStrandCaster#printHook`。
- `MachineIGenerator` 旧 overlay 是纪念文字且使用橙色标题；现代端尚未注册对应方块/BE，本轮只补 API 承载能力。

验证：

- 待跑 `.\gradlew.bat compileJava processResources --no-daemon`。

## 2026-06-04 准星信息 overlay 玩家上下文与维修路径预留

1.7.10 对照：

- `ILookOverlay#printHook(...)` 的方法签名不传 `EntityPlayer`，但旧实现可以直接从客户端全局读取玩家；典型例子是 `IRepairable.addGenericOverlay(...)`。
- `IRepairable.addGenericOverlay(...)` 的显示条件不是“机器有 tank/有损坏字段就显示”，而是：
  - 玩家手持 `ItemBlowtorch`。
  - 命中 dummy 后 `BlockDummyable#findCore(...)` 能找到核心。
  - 核心 TileEntity 实现 `IRepairable`。
  - `isDamaged()` 为 true。
  - 然后显示 `Repair with:` 和每个维修材料 `- <displayName> x<count>`。
- `MachineFluidTank#printHook` 与 `MachineRefinery#printHook` 旧端都只是调用上述维修 overlay，因此现代端不能把它们改成常驻 tank 信息。

现代侧改动：

- `LegacyLookOverlayProvider` 新增带 `Player` 的默认重载：`getLookOverlay(Level, Player, BlockPos)`，默认回落到旧的 `getLookOverlay(Level, BlockPos)`，不破坏既有机器实现。
- `LegacyLookOverlayBlockProvider` 新增带 `Player` 的默认重载：`getLookOverlay(Level, Player, BlockPos, BlockState)`，用于后续方块级 overlay 按手持物/玩家状态变化。
- `LegacyLookOverlayRenderer` 在 block provider 与 core BlockEntity provider 路径中传入 `minecraft.player`，把旧端“实现内部读客户端玩家”的隐式依赖改为现代显式上下文。
- `LegacyLookOverlayLines` 新增 `repairHeader()` 与 `repairMaterial(ItemStack)`，作为旧 `IRepairable.addGenericOverlay(...)` 文本格式的现代库入口。

勘误/边界：

- 现代端目前未发现喷灯、`AStack` 维修材料链和 `IRepairable` 机器完整承载；本轮只补准星库 API，不迁实际维修行为，不让 `machine_fluidtank` / `machine_refinery` 显示猜测内容。
- 复扫现代已注册 BE 后，旧 `ILookOverlay` 中仍有许多机器只迁了模型/可见多方块占位，例如 sawmill、rotary furnace、turbine gas、strand caster 等；这些没有现代 runtime 数据承载，本轮继续保持无准星内容。
- `SHOW_BLOCK_META_OVERLAY` 旧端是 debug 分支，默认 false。本轮只记录，不迁为现代用户可见功能；后续若迁，应单独定义现代 blockstate/meta 等价显示规则。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay 手持传送带手杖接入

1.7.10 对照：

- `ItemConveyorWand extends Item implements ILookOverlay`，因此旧端在 block hit 分支中优先于命中方块调用它的 `printHook(...)`。
- `ItemConveyorWand#printHook(...)` 只在玩家存在、正在潜行、且为创造模式时继续。
- 命中方块必须是 `BlockConveyorBase`；满足条件后标题使用命中传送带方块名，正文只有一行 `Break whole conveyor line`。

现代侧改动：

- `ConveyorWandItem` 实现 `LegacyLookOverlayItemProvider`，由 `LegacyLookOverlayRenderer` 的手持物品优先级路径统一调用。
- 现代端仅在 `player.isShiftKeyDown()`、`player.getAbilities().instabuild` 且命中方块为现代 `ConveyorBlock` 家族时返回 overlay。
- 正文保持旧端原文 `Break whole conveyor line`，标题继续通过 `LegacyLookOverlay.forBlockState(...)` 使用命中方块显示名。

勘误/边界：

- 该提示不是普通传送带准星信息，也不是机器侧 overlay；它是手持 `conveyor_wand` 的条件提示。
- 现代 `IConveyorBelt` 可能被后续非 `BlockConveyorBase` 等价物实现，本轮只对齐现代已迁的 `ConveyorBlock` 家族，避免扩大到旧端不属于 `BlockConveyorBase` 的消费方/接口方块。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay 旧颜色协议 helper 补齐

1.7.10 对照：

- `ILookOverlay#printGeneric(...)` 在绘制每一行正文前检查字符串是否以 `&[` 开头。
- 若存在该前缀，旧端用最后一个 `&]` 作为结束标记，`Integer.parseInt(line.substring(2, end))` 得到该行颜色，然后绘制剩余正文。
- 旧端该协议是正文行协议，不影响标题；标题颜色仍由每个 `printGeneric(...)` 调用者独立传入。

现代侧改动：

- `LegacyLookOverlayLines` 新增 `legacyEncoded(String)`，把旧 `&[color&]text` 正文行转为带颜色的 `Component`。
- 新增 `legacyEncodedLines(List<String>)`，供后续迁老 `printHook` 时批量复用旧正文字符串格式。
- `LegacyLookOverlayRenderer` 继续只绘制 `Component`，不解析具体机器和旧字符串协议，保持 renderer 边界干净。

勘误/边界：

- 新 helper 是迁移兼容入口，不要求新代码继续拼 `&[color&]` 字符串；新实现可优先用 `colored(...)` 或直接构造 `Component`。
- 本轮只补库能力，不额外迁尚无现代运行时承载的旧机器 overlay。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay tank 行格式勘误

1.7.10 对照：

- `MachineSteamEngine#printHook(...)` 的 tank 行使用 `String.format(Locale.US, "%,d", ...)`，格式为 `1,234 / 4,000mB`，斜杠两侧有空格且带千分位。
- `MachineSolarBoiler#printHook(...)`、`MachineTowerSmall#printHook(...)`、`MachineTowerLarge#printHook(...)`、`MachineFractionTower#printHook(...)`、`MachineCatalyticCracker#printHook(...)` 直接拼接 `fill + "/" + maxFill + "mB"`，格式为 `1234/4000mB`，无空格、无千分位。
- 上述旧端同样用第一组 tank 为绿色输入箭头，后续输出 tank 为红色输出箭头；催化裂化机前两个 tank 为输入，其余为输出。

现代侧改动：

- `LegacyLookOverlayLines` 新增 `compactTank(...)` 与 `allCompactFluidUserTanks(...)`，供旧端使用紧凑 tank 文本的机器复用。
- `SolarBoilerBlockEntity` 改用 `compactTank(...)`。
- `CoolingTowerBlockEntity`、`FractionTowerBlockEntity`、`CatalyticCrackerBlockEntity` 改用 `allCompactFluidUserTanks(...)`。
- `SteamEngineBlockEntity` 保持现有 `tank(...)` 格式，因为其旧端确实使用千分位和空格。

勘误/边界：

- 本轮只修旧端已确认 `ILookOverlay` 且现代已有真实运行时数据的机器。
- `MachineBoiler` 旧端未实现 `ILookOverlay`，本轮只记录该差异，不因现代 `BoilerBlockEntity` 已有状态而扩大 overlay 行为。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay 流体计数阀 counter 格式勘误

1.7.10 对照：

- `FluidDuctGauge#printHook(...)` 的速率行使用 `String.format(Locale.US, "%,d", ...) + " mB/t"` 和 `" mB/s"`，因此流量计速率应保留千分位和单位前空格。
- `FluidCounterValve#printHook(...)` 的 counter 行直接拼接 `"Counter: " + duct.getCounter()`，不使用 `String.format(...)`，因此旧端显示 `Counter: 1234` 而不是 `Counter: 1,234`。
- 流体泵 `FluidPump#printHook(...)` 使用 `BobMathUtil.format(...)`；复核 `BobMathUtil#format(Number)` 后确认它是 `String.format(Locale.US, "%,d", amount)` 的千分位格式，不是 `getShortNumber(...)` 的短数字格式。

现代侧改动：

- `LegacyLookOverlayLines.counter(...)` 改为直接拼接 long 值，不再调用 `NUMBER_FORMAT.format(...)`。
- 当前只有 `FluidCounterValveBlockEntity` 使用该 helper，因此不会影响其他已迁 overlay 文本。

勘误/边界：

- `LegacyLookOverlayLines.rate(...)` 保持千分位格式，用于旧端明确使用 `String.format(Locale.US, "%,d", ...)` 的流量计。

验证：

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay 流体泵与工业涡轮格式勘误

1.7.10 对照：

- `BobMathUtil#format(Number)` 明确返回 `String.format(Locale.US, "%,d", amount)`，因此 `FluidPump#printHook(...)` 的流量与缓冲行应显示 `10,000mB/t`、`1,234mB buffered` 这类千分位文本，而不是 `10.0k` 短数字。
- `BobMathUtil#getShortNumber(long)` 才是短数字 helper，覆盖 `k/M/G/T/P/E` 档，并按旧端逻辑多数情况下保留两位小数；电池、工业涡轮 HE 输出等旧 overlay 使用的是这个 helper。
- `MachineIndustrialTurbine#printHook(...)` 的 tank 行格式为千分位但斜杠两侧无空格：`1,234/750,000mB`。
- `MachineIndustrialTurbine#printHook(...)` 的输出流体名称不是直接取输出 tank 当前类型，而是从输入流体的 `FT_Coolable.coolsTo` 推导；若输入流体不可涡轮冷却，则显示 `Fluids.NONE`。
- `MachineIndustrialTurbine#printHook(...)` 的 HE/spin 行使用旧正文颜色协议 `&[color&]`，spin 百分比部分颜色按 `spin` 在红到绿之间变化。

现代侧改动：

- `LegacyLookOverlayLines.pumpLine(...)` 与 `buffered(...)` 改回 `NUMBER_FORMAT` 千分位格式，对齐旧 `FluidPump#printHook(...)`。
- `LegacyLookOverlayLines.shortNumber(...)` 对齐旧 `BobMathUtil#getShortNumber(long)` 的 `k/M/G/T/P/E` 档位与舍入规则，避免电池、插座、涡轮等已迁准星数值继续使用现代猜测短写。
- 新增 `groupedCompactTank(...)`，用于工业涡轮这种“千分位、无斜杠空格”的旧 tank 行格式。
- 新增 `industrialTurbineEnergyOut(...)`，承载工业涡轮 HE 输出、spin 字符和红绿渐变颜色，不把该机器逻辑塞进 renderer。
- `IndustrialSteamTurbineBlockEntity#getLookOverlay(...)` 改为按输入 fluid 的 `CoolableFluidTrait` 推导输出显示名，并使用上述工业涡轮专用 helper。

勘误/边界：

- 普通单方块 `MachineTurbine` 旧端不实现 `ILookOverlay`；本轮没有给现代 `machine_turbine` 新增准星 overlay。
- 旧 `machine_boiler_off` 对应 `MachineBoiler`，不实现 `ILookOverlay`；现代 `BoilerBlockEntity` 已在 fluid-library 后续记录中作为旧 `MachineHeatBoiler` 行为承载推进，因此本轮只记录命名/ID 混淆风险，不在准星库里直接拆除锅炉承载逻辑。

验证：

- `git diff --check -- src/main/java/com/hbm/ntm/api/block/LegacyLookOverlayLines.java src/main/java/com/hbm/ntm/blockentity/IndustrialSteamTurbineBlockEntity.java` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 press/epress 与 legacy 核弹静态模型入口补齐

1.7.10 对照：

- `ResourceManager.press_body` / `press_head` / `epress_body` / `epress_head` 均由 `AdvancedModelLoader.loadModel(...)` 加载，路径分别为 `models/press_body.obj`、`models/press_head.obj`、`models/epress_body.obj`、`models/epress_head.obj`，不使用 `HFRWavefrontObject`，也没有 `.asVBO()`。
- 上述四个模型贴图分别为 `textures/models/press_body.png`、`press_head.png`、`epress_body.png`、`epress_head.png`；旧 `RenderPress`、`RenderEPress` 与 `ItemRenderLibrary` 直接绑定这些贴图后渲染模型。
- `ResourceManager.bomb_boy` 使用 `models/LilBoy1.obj` 与 `textures/models/lilboy.png`；`ResourceManager.bomb_multi` 使用 `models/BombGeneric.obj` 与 `textures/models/BombGeneric.png`，同样都是普通 `AdvancedModelLoader` 非 VBO 入口。

现代侧改动：

- 从 1.7.10 原资源复制四个 press/epress OBJ 到 `assets/hbm/models/block/machines/*`，并复制对应贴图到 `assets/hbm/textures/block/machines/*`。
- `ObjMachineModels` 新增 `PRESS_BODY_LEGACY`、`PRESS_HEAD_LEGACY`、`EPRESS_BODY`、`EPRESS_HEAD`，并暴露四个对应贴图 `ResourceLocation`。
- `ObjModelLibrary` 新增 `MACHINE_PRESS_BODY_LEGACY`、`MACHINE_PRESS_HEAD_LEGACY`、`MACHINE_EPRESS_BODY`、`MACHINE_EPRESS_HEAD`，作为旧 `ResourceManager` facade 的现代统一入口。
- 现代已有 `machine_press` baked 方块模型和动态压头 `PRESS_HEAD`，本轮不替换这些现有方块模型；新入口只用于后续迁旧 TESR/item renderer 时按 1.7.10 资源路径取用。
- 从 1.7.10 原资源复制 `LilBoy1.obj` 为 `models/block/nuke/boy_legacy.obj`、`BombGeneric.obj` 为 `models/block/nuke/bomb_multi_legacy.obj`，并复制 `lilboy.png`、`BombGeneric.png` 为独立 legacy 贴图名。
- `ObjNukeModels` 新增 `BOY_LEGACY`、`BOMB_MULTI_LEGACY`，`ObjModelLibrary` 暴露 `NUKE_BOY_LEGACY`、`NUKE_BOMB_MULTI_LEGACY`。现代已有 `BOY` 和 `bomb_multi` 方块模型哈希不同，保留不动。

勘误/边界：

- `fusion_torus`、`fusion_klystron`、`fusion_collector`、`fusion_mhdt`、`fusion_coupler`、`fusion_plasma_forge` 已有现代 `ObjFusionModels` legacy facade，扫描缺失主要来自旧路径 `models/fusion/*.obj` 与现代路径 `models/block/fusion/fusion_*.obj` 命名不同。
- `missileShuttle`、`minerRocket` 已由 `ObjMissilePartModels.MISSILE_SHUTTLE` / `MINER_ROCKET` 覆盖，现代资源采用 `missile_shuttle` / `miner_rocket` 命名。
- `deb_zirnox_*` 已由 `ObjProjectileModels.ZIRNOX_DEBRIS_*` 覆盖，现代资源采用 `zirnox_deb_*` 命名。
- `nikonium` 旧端声明了 `models/nikonium.obj` 与 `textures/models/misc/nikonium.png`，但旧资源包未发现该贴图；继续不暴露可用 facade，避免创造不存在的 1.7.10 资产。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 floodlight/demon_lamp 完整旧 renderer 模型入口补齐

1.7.10 对照：

- `RenderFloodlight` 内部私有加载 `models/blocks/floodlight.obj`，贴图为 `textures/models/machines/floodlight.png`，不是 `ResourceManager` 静态字段；旧 renderer 按 `Base`、`Lights`、`Lamps` 分段渲染。
- `RenderDemonLamp` 内部私有加载 `models/blocks/demon_lamp.obj`，贴图为 `textures/models/machines/demon_lamp.png`，不是 `ResourceManager` 静态字段；旧 renderer 直接 `renderAll()`。
- 两者旧端均为普通 `new HFRWavefrontObject(...)`，没有 `.noSmooth()` 或 `.asVBO()`。

现代侧改动：

- 从 1.7.10 原资源复制 `demon_lamp.obj`、`floodlight.obj` 到 `assets/hbm/models/block/lights/`；现代端已有 `textures/block/machines/demon_lamp.png`、`floodlight.png`，复核哈希与 1.7.10 `textures/models/machines/*` 一致。
- `ObjLightModels` 新增 `FLOODLIGHT_LEGACY`、`DEMON_LAMP_LEGACY` 完整旧模型 facade，并新增可绑定 `textures/block/machines/*` 的 `machineTexture(...)` helper。
- `ObjModelLibrary` 暴露 `FLOODLIGHT_LEGACY`、`DEMON_LAMP_LEGACY`，供后续迁旧 TESR/item renderer 时使用。

勘误/边界：

- 现代已有 `FLOODLIGHT_BASE`、`FLOODLIGHT_LIGHTS`、`FLOODLIGHT_LAMPS` 和 `DEMON_LAMP` baked 分段入口，本轮不替换现有 renderer，只补完整旧模型入口。
- `geiger_counter`、`charger`、`refueler` 虽位于旧 `models/blocks/*`，现代端已有资源且哈希与 1.7.10 原 OBJ 一致；本轮只记录复核结果，不重复复制。
- `models/blocks/arrow.obj` 在旧资源包存在，但未找到 `ResourceManager` 声明或 Java 调用点，也没有明确旧贴图声明；按“不生造功能”规则不迁。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay red_cable_gauge 接入

1.7.10 对照：

- `com.hbm.blocks.network.BlockCableGauge` 是单独方块 `red_cable_gauge`，不是普通 `red_cable` 的附加 overlay；普通 `BlockCable`/`red_cable` 不实现 `ILookOverlay`。
- `BlockCableGauge#printHook(...)` 读取 `TileEntityCableGauge.deltaTick` 与 `deltaLastSecond`，正文两行分别为 `BobMathUtil.getShortNumber(value) + "HE/t"` 和 `"HE/s"`，标题使用 `tile.red_cable_gauge.name`，标题色仍走 `ILookOverlay.printGeneric(...)` 的黄色/暗黄。
- `TileEntityCableGauge` 继承 `TileEntityCableBaseNT`，从 `node.net.energyTracker` 取本 tick 网络传输量；每 20 tick 把 `deltaSecond` 写入 `deltaLastSecond` 并清零，每 tick 累加 `deltaSecond += deltaTick`，`networkPackNT(25)` 只同步 `deltaTick` 与 `deltaLastSecond`。
- 旧端还实现 OC/ROR 值接口：`VAL:deltatick` 与 `VAL:deltasecond`，其中 `deltasecond` 返回 `deltaLastSecond`。
- 旧渲染为完整方块 multipass：第一层 `deco_red_copper`，第二层仅在 placement metadata 指向面叠 `cable_gauge` 透明贴图；`onBlockPlacedBy` 用 `BlockPistonBase.determineOrientation(...)` 写入朝向 metadata。
- 旧语言：`tile.red_cable_gauge.name=Power Gauge`，`zh_CN` 为 `功率计`。
- 旧无序配方为 `red_wire_coated + steel ingot + circuit BASIC`。现代端尚未迁 `red_wire_coated`，本轮不使用 `red_cable` 顶替，避免生成猜测配方。

现代侧改动：

- 新增 `RedCableGaugeBlock`，继承现代能量节点块以参与 `HbmPowerNet`，保留独立 `FACING` 方块状态用于表盘面，不复用普通红线缆的细线缆外观/碰撞。
- 新增 `RedCableGaugeBlockEntity`，通过 `HbmEnergyNode#getPowerNet().getEnergyTracker()` 对齐旧 `PowerNetMK2.energyTracker`，按 20/25 tick 节奏维护并同步 `deltaTick` 与 `deltaLastSecond`。
- `RedCableGaugeBlockEntity` 通过共享 `LegacyLookOverlayProvider` 返回两行 `LegacyLookOverlayLines.shortRate(..., "HE/t|HE/s")`，renderer 无任何机器硬编码。
- 同步实现 ROR 值接口 `VAL:deltatick`、`VAL:deltasecond`，后者对齐旧端返回 `deltaLastSecond`。
- 注册 `red_cable_gauge` 方块、方块实体、机器创造栏、采掘标签、掉落表和语言。
- 从 1.7.10 原资源复制 `textures/blocks/cable_gauge.png` 到现代 `assets/hbm/textures/block/cable_gauge.png`；主资源和 datagen 均生成完整铜纹方块 + 朝向表盘透明叠层模型，叠层略微外扩避免 baked 模型同面闪烁。

勘误/边界：

- 没有给普通 `red_cable` 添加准星文本；旧端普通线缆无 `ILookOverlay`。
- 没有迁 `CableDiode#printHook(...)`，因为现代端尚未有对应已注册方块。
- 没有补 `red_cable_gauge` 配方，等待 `red_wire_coated` 按旧端迁入后再使用真实旧配方。

验证：

- `git diff --check` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## 2026-06-04 准星信息 overlay 第三轮缺口复扫

1.7.10 对照：

- 旧端准星入口仍以 `ILookOverlay` 为唯一事实源。本轮重新扫描 `blocks/network`、`blocks/machine`、`items/tool`、`blocks/generic` 中所有 `implements ILookOverlay` 与 `printHook(...)`。
- 流体网络旧端覆盖 `FluidValve`、`FluidSwitch`、`FluidPipeAnchor`、`FluidDuctStandard`、`FluidDuctPaintable`、`FluidDuctBox`、`FluidDuctGauge`、`FluidCounterValve`、`FluidPump`、`FluidDuctBoxExhaust`、`FluidDuctPaintableBlockExhaust`；现代端均已有对应共享 overlay 承载或通过 `FluidPipeBlockEntity` 继承承载。
- 排烟管旧端只显示 `Smoke`、`Lead Smoke`、`Poison Smoke` 三种流体名；现代 `FluidDuctExhaustBlockEntity` 与 `FluidDuctPaintableExhaustBlockEntity` 的 `fluidNames(SMOKES)` 已对齐。
- 流体泵旧端 `printHook(...)` 使用千分位 `BobMathUtil.format(...)` 显示 `bufferSize` 与 buffered fill，优先级行显示 `Priority: <name>`，只有 `tank[0].getFill() > 0` 时显示 buffered 行；现代 `FluidPumpBlockEntity` 已通过 `LegacyLookOverlayLines.pumpLine(...)`、`priority(...)`、`buffered(...)` 对齐。
- 能量存储旧端 `MachineBattery` 与 `MachineBatterySocket` 均使用 `BobMathUtil.getShortNumber(...)` 容量行和红绿渐变百分比行；现代 `energyStorage(...)` 已集中承载同一格式，插座标题使用电池物品名。
- `sellafield_bedrock` 旧端是 `BlockSellafieldSlaked`，不是 `BlockBedrockOreTE`；`BlockBedrockOreTE` 对应旧 `ore_bedrock`，现代当前未注册同等 TE 承载。
- 现代 `lantern` 对应旧普通 `BlockLantern`，不是旧隐藏 `lantern_behemoth`；旧 `lantern_behemoth` 的准星只是喷灯维修 overlay，现代未注册该方块和维修链承载。

现代侧结论：

- 本轮没有新增代码迁移点。已注册且有真实 runtime 的旧准星交集目前集中在：流体网络、流体泵、流量/计数表、排烟管、能量存储、红线缆功率计、蒸汽发动机、太阳锅炉、冷却塔、分馏塔、催化裂化机、工业涡轮、热锅炉承载线、化工厂/装配厂/燃气涡轮/旋转炉静态端口。
- `HbmFluidBlockEntity#showsLegacyFluidLookOverlay()` 默认保持 `false` 是正确边界；`FluidTankBlockEntity`、`FluidBarrelBlockEntity`、`CompressorBlockEntity`、`LiquefactorBlockEntity`、`OilDrillBlockEntity`、`RefineryBlockEntity` 等不应因继承流体库而自动显示 tank overlay。
- `MachineBoiler` 旧端不实现 `ILookOverlay`；现代 `BoilerBlockEntity` 的准星文本继续按已迁 `MachineHeatBoiler#printHook(...)` 的承载线记录，不按名称把它误判为普通锅炉。
- `MachineTurbine` 旧端不实现 `ILookOverlay`；现代 `SteamTurbineBlockEntity` 也未实现 overlay。`LegacySteamTurbineBlockEntity` 只服务已按旧端有准星的多方块/工业涡轮承载线。

仍不迁的缺口：

- 旧端存在但现代无注册或无等价 runtime 的准星对象：`BlockRemap`、`Guide`、`BlockWand*`、`ItemStructureTool`、`ItemCMStructure`、`BlockBedrockOreTE`、`lantern_behemoth`、radio torch 系列、drone/crane 系列、`CableDiode`、RF/HE 转换器、`MachineCapacitor`/`MachineFENSU`、deuterium/intake/drain/condenser 系列、ICF/RBMK/rail car 等。
- 旧端读取动态状态但现代当前只是可见多方块空壳的机器继续不显示猜测内容，例如 sawmill、stirling、conveyor press、industrial heat boiler、hephaestus、strand caster、chungus 等。

验证：

- `git diff --check -- src/main/java/com/hbm/ntm/api/block/LegacyLookOverlayLines.java src/main/java/com/hbm/ntm/api/block/LegacyLookOverlayPorts.java src/main/java/com/hbm/ntm/blockentity/LegacyVisibleMachineBlockEntity.java 工程记录/库移植/2026-05-20-render-library-1.7.10源码功能追踪.md` 通过；仅有既有 LF/CRLF 提示。
- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
