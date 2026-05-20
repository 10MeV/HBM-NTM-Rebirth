# 渲染库 1.7.10 源码功能追踪

## 范围

- 本记录作为后续“渲染库大排查”的总入口，覆盖 1.7.10 客户端渲染注册、OBJ/HMF/动画模型加载、模型资源管理、方块/方块实体/实体/物品 renderer、HUD/overlay 与渲染辅助工具。
- 目标是逐批把 1.7.10 渲染逻辑和可复用接口复刻到 1.20.1 port，使后续机器移植可以稳定接入统一渲染库。
- 本轮只建立入口和第一层接口清单，不迁移 Java 逻辑；后续每一批机器或渲染类型移植前，应先补充本文件中对应 renderer 的完整行为。

## 1.7.10 总入口

- `src/main/java/com/hbm/main/ClientProxy.java`
  - 核心入口方法：`registerRenderInfo()`。
  - 注册资源重载监听：从 `Minecraft.getMinecraft().getResourceManager()` 转为 `IReloadableResourceManager`。
  - 注册客户端事件 handler：`MinecraftForge.EVENT_BUS.register(...)`。
  - 方块实体渲染：大量 `ClientRegistry.bindTileEntitySpecialRenderer(TileEntityX.class, new RenderX())`。
  - 实体渲染：大量 `RenderingRegistry.registerEntityRenderingHandler(EntityX.class, new RenderX())`。
  - ISBRH 方块渲染：大量 `RenderingRegistry.registerBlockHandler(new RenderX())`。
  - 物品自定义渲染：`MinecraftForgeClient.registerItemRenderer(...)`。
- 现代对应入口：
  - `src/main/java/com/hbm/ntm/client/ClientModEvents.java`
  - `EntityRenderersEvent.RegisterRenderers` 注册 `BlockEntityRenderer`/实体 renderer。
  - `ModelEvent.RegisterAdditional` 注册额外 OBJ baked model。
  - `FMLClientSetupEvent` 注册 screen/menu 等客户端初始化。

## 1.7.10 资源与模型库入口

- `src/main/java/com/hbm/main/ResourceManager.java`
  - 旧版最重要的模型常量库，集中声明 `IModelCustom`、`IModelCustomNamed`、`AnimatedModel`、`Animation`、`BusAnimation` 等静态资源。
  - OBJ 加载来源混用：
    - `new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/...obj"))`
    - `new HFRWavefrontObject("models/...obj")`
    - `AdvancedModelLoader.loadModel(new ResourceLocation(...))`
  - 常见链式行为：
    - `.asVBO()`：把 `HFRWavefrontObject` 转为 VBO 版本。
    - `.noSmooth()`：关闭平滑法线/平滑渲染路径。
  - 资源路径以旧资源树为准：`assets/hbm/models/...`、`assets/hbm/textures/models/...`。
- 现代当前对应：
  - `src/main/java/com/hbm/ntm/client/obj/ObjModelLibrary.java`
  - 当前只收录了 press、灯具、trinket 等少量 OBJ 入口；后续不应在各 renderer 内散落硬编码模型路径，优先扩展该库或按同等模式拆分子库。

## 1.7.10 OBJ/模型接口

- `src/main/java/com/hbm/render/loader/HFRWavefrontObject.java`
- `src/main/java/com/hbm/render/loader/HFRWavefrontObjectVBO.java`
- `src/main/java/com/hbm/render/loader/IModelCustomNamed.java`
- `src/main/java/com/hbm/render/loader/S_GroupObject.java`
- `src/main/java/com/hbm/render/loader/S_Face.java`
- `src/main/java/com/hbm/render/loader/ModelRendererObj.java`
- `src/main/java/com/hbm/render/loader/HFRModelReloader.java`
- `src/main/java/com/hbm/render/loader/HmfModelLoader.java`
- `src/main/java/com/hbm/render/loader/HmfController.java`

旧版必须核对的接口语义：

- 整体渲染、按 OBJ group 名称渲染、排除部分 group 渲染。
- VBO 与非 VBO 路径差异。
- `noSmooth()` 对法线、颜色或光照的影响。
- 模型 reload 行为，尤其资源包重载后静态模型是否重建。
- HMF 模型及控制器是否被机器/实体 renderer 使用。

## 1.7.10 渲染辅助接口

- `src/main/java/com/hbm/render/util/ObjUtil.java`
  - `renderWithIcon(HFRWavefrontObject, IIcon, Tessellator, rot, pitch, roll, shadow)`
  - `renderPartWithIcon(HFRWavefrontObject, name, IIcon, Tessellator, rot, pitch, roll, shadow)`
  - `setColor(int)`、`setColor(r,g,b)`、`clearColor()`
  - `getPitch(ForgeDirection)`、`getYaw(ForgeDirection)`
  - 重要旧语义：模型默认朝向 `+X/EAST`；旋转顺序为 roll 绕 X、pitch 绕 Z、rot/yaw 绕 Y；三角面会重复第 3 点以塞进 quad tessellator。
  - `shadow` 路径按旋转后的 normal 计算简易明暗，下限约 `0.45F`。
- 现代当前对应：
  - `src/main/java/com/hbm/ntm/client/obj/ObjRenderUtils.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjModelPart.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjPartTransform.java`
- 当前已支持额外模型注册、direct baked quad 渲染、RenderType 覆盖、lightMultiplier，但尚未完整复刻旧版 `ObjUtil` 的按 group 名渲染、旧式 shadow 明暗、颜色覆盖、旧方向 pitch/yaw 语义。

2026-05-20 第一轮现代对齐：

- `ObjRenderContext` 已增加旧版风格的可选渲染状态：
  - `withColor(int)` / `clearColor()` 对应旧 `ObjUtil.setColor(...)` / `clearColor()`。
  - `fullBright()` 用于旧版 `OpenGlHelper.setLightmapTextureCoords(..., 240F, 240F)` 一类全亮渲染。
  - `withLegacyShadow()` / `withoutLegacyShadow()` 用于开启旧 `ObjUtil` 的简易 normal 明暗近似。
- `ObjModelPart` 会把上述上下文状态传给 `ObjRenderUtils`，旧调用 `new ObjRenderContext(...)` 保持兼容。
- `ObjRenderUtils` 已支持颜色覆盖、全亮 light 值传入、direct baked quad 的旧 shadow 近似；普通 `ModelBlockRenderer` 路径目前只能做整体 shadow 近似，后续如需逐面完全一致，需要切换到 direct quad 路径或自定义 OBJ group 解析。
- 新增 `LegacyObjTransforms`：
  - `yawRadians(Direction)` / `pitchRadians(Direction)` 按 1.7.10 `ObjUtil.getYaw/getPitch` 复刻，旧 OBJ 默认朝向仍按 `+X/EAST` 处理。
  - `applyObjUtilRotation(PoseStack, yaw, pitch, roll)` 保留旧版旋转顺序：roll 绕 X、pitch 绕 Z、yaw 绕 Y。
  - `rotateAroundBlockCenterY(...)` 用作常见机器 facing 旋转辅助。
- `BasicMachineRenderer` 的 press 头部渲染已改用 `LegacyObjTransforms.rotateAroundBlockCenterY(...)` 作为接入样例。

2026-05-20 第二轮现代对齐：

- 新增 `LegacyObjModel`，作为 1.20.1 侧对 `IModelCustom` / `IModelCustomNamed` 常用方法形状的现代对应：
  - `renderAll(ObjRenderContext)`
  - `renderPart(String, ObjRenderContext)`
  - `renderOnly(ObjRenderContext, String...)`
  - `renderAllExcept(ObjRenderContext, String...)`
  - `getPartNames()`
- `ObjAnimatedModel` 已实现 `LegacyObjModel`，因此后续 `ResourceManager` 风格模型常量可以暴露为接口类型，减少 renderer 对具体实现的耦合。
- 现代资源侧已确认当前 OBJ JSON 使用 Forge OBJ loader：
  - `loader: forge:obj`
  - `model: hbm:models/block/...obj`
  - `flip_v: true`
  - `render_type: minecraft:cutout` 或其他现代 RenderType
  - `textures.default` / `textures.texture0` 供应旧模型贴图
- 重要限制：当前 `ObjAnimatedModel` 的 part 是“多个已拆分 baked model 的组合”，不是从单个 OBJ baked model 的 group 动态选择；若要完整复刻旧 `HFRWavefrontObject.renderPart("Group")`，后续需要：
  - 要么为每个旧 group 生成/维护独立 JSON/OBJ 模型入口；
  - 要么实现自定义 OBJ group 解析与缓存，绕开 Forge baked model 对 group 的封装。

2026-05-20 第三轮现代对齐：

- `LegacyObjTransforms` 新增 `applySixFaceAttachmentRotation(PoseStack, Direction)`：
  - 对应旧 `RenderDemonLamp`、`RenderFloodlight` 等按 metadata 0..5 做六面贴附旋转的 GL 逻辑。
  - 该 helper 已接入 `LegacyDemonLampBlockEntityRenderer`。
- 新增 `LegacyUntexturedQuadRenderer`：
  - 封装旧版 `GL11.glDisable(GL_TEXTURE_2D)` + fullbright lightmap + 彩色/透明 Tessellator quad 的现代 VertexConsumer 写法。
  - 当前使用 `RenderType.lightning()` 与 `LightTexture.FULL_BRIGHT` 近似旧的无贴图全亮加色几何。
  - 已接入 `LegacyLanternBlockEntityRenderer` 的暖色 `Light` 几何，以及 `LegacyDemonLampBlockEntityRenderer` 的蓝色 aura 几何。
- `LegacyLightBlockEntityRenderer` 的 spotlight yaw/pitch 已改用 `LegacyObjTransforms`，保留 `-pitchDegrees(...)` 以匹配当前 spotlight 迁移中 UP/DOWN 的既有方向表现。
- 本轮仍未迁移 floodlight 的 `isOn` fullbright / off 灰色灯罩语义；后续需要先确认 `LegacyLightBlockEntity` 是否已保存旧 `TileEntityFloodlight.isOn` 等状态，再把 `Lamps` part 改为 `context.fullBright()` 或 `context.withColor(0x404040)`。

2026-05-20 第四轮现代对齐：

- `LegacyLightBlockEntity` 已补齐旧 `TileEntityFloodlight` 的最小渲染状态契约：
  - `rotation`
  - `power`
  - `delay`
  - `isOn`
  - 以上字段已进入 NBT 保存与 client update tag / packet，同步结构先铺好。
- `LegacyLightBlockEntityRenderer` 的 floodlight `Lamps` part 已复刻旧 `RenderFloodlight` 的视觉分支：
  - `isOn == true`：使用 `ObjRenderContext.fullBright()`，对应旧 `RenderArcFurnace.fullbright(true)`。
  - `isOn == false`：使用 `ObjRenderContext.withColor(0x404040)`，近似旧 `GL11.glColor4f(0.25F, 0.25F, 0.25F, 1F)`。
- 本轮只迁移渲染所需状态与视觉表现，不实现旧 `IEnergyReceiverMK2`、`power >= 100` 消耗、`floodlight_beam` 投射/清理逻辑；这些应归入后续能量网络与光束方块逻辑层。

## 旧版 renderer 分类入口

- 方块实体 renderer：
  - `src/main/java/com/hbm/render/tileentity`
  - 机器、核弹、灯具、trinket、炮塔、反应堆、导弹发射台等大多在这里。
  - 每个 renderer 需要记录：绑定 TileEntity、使用的 `ResourceManager` 模型字段、贴图路径、metadata/facing 映射、GL transform 顺序、动画字段、特殊光照/混合/透明/禁用贴图行为。
- ISBRH 方块 renderer：
  - `src/main/java/com/hbm/render/block`
  - 对应 1.7.10 `RenderingRegistry.registerBlockHandler(...)`，现代通常要改为 baked model、BER、`BlockEntityRenderer`、动态模型或普通 blockstate/model JSON。
- 物品 renderer：
  - `src/main/java/com/hbm/render/item`
  - `src/main/java/com/hbm/render/item/block`
  - `src/main/java/com/hbm/render/item/weapon`
  - 现代对应 `BlockEntityWithoutLevelRenderer`、item model override、或普通 item JSON。
- 实体 renderer：
  - `src/main/java/com/hbm/render/entity`
  - 投射物、导弹、云、特殊爆炸视觉、载具、怪物等，现代对应 `EntityRendererProvider` 注册。
- HUD/overlay/客户端效果：
  - `src/main/java/com/hbm/render/util/RenderScreenOverlay.java`
  - `src/main/java/com/hbm/render/util/RenderOverhead.java`
  - `src/main/java/com/hbm/render/util/RenderMiscEffects.java`
  - `src/main/java/com/hbm/render/util/GaugeUtil.java`
  - 现代对应 Forge GUI overlay、level render events、particle provider 或自定义 entity renderer。

## 当前 1.20.1 port 现状

- 客户端入口：
  - `src/main/java/com/hbm/ntm/client/ClientModEvents.java`
- OBJ 封装：
  - `src/main/java/com/hbm/ntm/client/obj/ObjModelLibrary.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjModelPart.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjRenderUtils.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjAnimatedModel.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjAnimation.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjBlockEntityAnimation.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjRenderContext.java`
  - `src/main/java/com/hbm/ntm/client/obj/ObjPartTransform.java`
- 已接入 renderer：
  - `src/main/java/com/hbm/ntm/client/renderer/BasicMachineRenderer.java`
  - `src/main/java/com/hbm/ntm/client/renderer/LegacyLightBlockEntityRenderer.java`
  - `src/main/java/com/hbm/ntm/client/renderer/LegacyDemonLampBlockEntityRenderer.java`
  - `src/main/java/com/hbm/ntm/client/renderer/TrinketBlockEntityRenderer.java`
  - `src/main/java/com/hbm/ntm/client/renderer/TrinketItemRenderer.java`

## 分批排查与移植计划

1. 渲染库基础层
   - 对照 `HFRWavefrontObject`、`HFRWavefrontObjectVBO`、`IModelCustomNamed`、`ObjUtil`，补齐现代 OBJ 包装层缺失接口。
   - 明确现代 Forge OBJ baked model 能否满足按 group 渲染；若不能，需要建立可按 part/group 查询的解析或缓存层。
2. 客户端注册层
   - 把 `ClientProxy.registerRenderInfo()` 中的 TileEntity/Entity/Block/Item renderer 注册拆成可追踪表。
   - 每个现代迁移项必须写明旧注册入口和现代注册入口。
3. 方块实体/机器 renderer 层
   - 优先处理后续机器移植会复用的 `RenderPress`、基础机器、油处理、流体罐、泵、加热器、反应堆显示部件。
   - 每个 renderer 先记录模型、贴图、transform、动画字段，再移植。
4. 方块 ISBRH 层
   - 梳理哪些旧 `RenderBlock*` 可转为普通 JSON/OBJ baked model，哪些必须保留动态 BER。
5. 物品与实体 renderer 层
   - 武器/导弹/核爆视觉单独分批，不阻塞普通机器渲染库。
6. HUD/overlay/粒子层
   - 与辐射、污染、爆炸视觉系统一起迁移，暂不作为机器渲染库第一批阻塞项。

## 迁移约束

- 旧版 1.7.10 源码和资源是事实来源；参考版 1.20.1 只作为现代 API 结构参考。
- 所有模型与贴图先从 `assets/hbm/models`、`assets/hbm/textures` 的旧资源查找，禁止生成替代资源。
- 后续机器 renderer 接入前，需要先在本文件补齐该 renderer 的旧版行为表，再编辑 Java。
- 每一小批保持 `compileJava processResources` 可过。

## 验证清单

- 命令：`./gradlew.bat compileJava processResources --no-daemon`
- 渲染库基础层验证：
  - OBJ 额外模型能注册并在 BER/BEWLR 中渲染。
  - 按 group/part 渲染行为与 1.7.10 renderer 需求一致。
  - 旧版 `+X/EAST` 默认朝向、pitch/yaw 映射、shadow 明暗、颜色覆盖有明确现代对应。
- 机器接入验证：
  - 每个机器方块放置后 facing 与旧 metadata 方向一致。
  - GUI/BER/物品模型共用模型路径时不会漏注册额外模型。
  - 透明、全亮、禁用贴图、叠加 pass 等特殊渲染有截图或游戏内核验记录。

## 2026-05-20 第五轮现代对齐

- `ObjModelLibrary` 继续向 1.7.10 `ResourceManager` 的集中入口模式靠拢，新增统一模型路径和 part 工厂：
  - `blockModel(String)`：统一生成 `hbm:block/...` 模型资源路径。
  - `blockPart(String)` / `blockPart(String, RenderType)`：普通 OBJ baked model part 入口。
  - `blockPartBuilder(String, RenderType)` / `directBlockPart(String)`：供旧版 TESR 风格 renderer 声明 direct baked quad、lightMultiplier、origin 等属性。
  - `trinketPart(String, RenderType)`：统一 `block/trinkets/...` 模型路径，避免 trinket renderer 自行拼接 `ResourceLocation`。
- 现有 press、lamp、floodlight、demon lamp 模型常量已改为经由这些工厂声明，后续机器 renderer 迁入时应先在 `ObjModelLibrary` 或其子库登记模型常量，再由 renderer 消费。
- `TrinketBlockEntityRenderer` 已改为调用 `ObjModelLibrary.trinketPart(...)`，作为“renderer 不直接硬编码 OBJ 资源路径”的样例。
- 本轮不改变渲染输出，只收束资源入口；后续若模型数量继续膨胀，可按 1.7.10 `ResourceManager` 的领域分组拆出 machine/trinket/light 子库，但仍由统一注册入口汇总到 `ModelEvent.RegisterAdditional`。
