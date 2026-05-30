# 现代渲染 Shader 源码效果说明与 1.20.1 应用指南

## 范围

- 记录 `E:\游戏\我的世界\源码包\render` 下现代化渲染资源的效果、主要配置入口和迁移到 clean Forge 1.20.1 port 的应用方式。
- 本文只整理资源和接入指南，不迁移代码、不复制 shader 文件。
- 这些资源不是 HBM 1.7.10 原版 `com.hbm.render` 的直接旧代码，而是一套额外现代 shader 资源；后续接入 HBM 时应作为新客户端渲染库处理，不能当成服务端逻辑或旧版功能契约。

## 源文件结构

- `render/core/*.json|*.vsh|*.fsh`
  - 材质/RenderType/实体或方块局部渲染 shader。
  - 多数 JSON 内的 `vertex` / `fragment` 仍写为 `arcanevortex:<name>`，迁入 HBM 时必须改为 `hbm:<name>`。
- `render/program/*.json|*.vsh|*.fsh`
  - 全屏后处理 program shader。
  - `invert_colors` 是屏幕反色/扫描/噪声类后处理。
  - `transparency` 是多层 framebuffer 合成与折射玻璃球实验。
- `render/post/*.json`
  - PostChain 管线定义。
  - 当前只有 `invert_colors` 和 `warp`，都把 `minecraft:main` 渲染到 `swap`，再 `minecraft:blit` 回主目标。

## Clean Port 当前状态

- `src/main/resources/assets/hbm/shaders/core` 已接入 `warp_world` 与 `black_hole`。
- `ClientModEvents#registerShaders(...)` 负责注册 `HbmRenderEffects` 与 `HbmBlackHoleEffects` 的 core shader。
- `HbmRenderEffects` 当前承载 `warp_world` 核爆冲击波；`HbmBlackHoleEffects` 当前承载 `black_hole` 全屏引力透镜/吸积盘后处理。
- Program/PostChain 通用管线仍未作为公共库打通；`program/invert_colors`、`program/transparency` 和 `post/*` 仍按后续功能单独评估。
- 其他尚未接入的现代 shader 仍应先补资源注册、uniform 写入、framebuffer copy、reload/resize 和 dedicated server 隔离，再挂到具体内容。

## 通用资源契约

- 常见自动 uniform：
  - `ModelViewMat`、`ProjMat`：Minecraft shader 默认矩阵。
  - `ColorModulator`、`FogStart`、`FogEnd`、`FogColor`、`FogShape`：方块/实体材质类 shader 需要对齐 vanilla fog 和颜色调制。
  - `time` / `GameTime` / `Time`：动画时间；建议统一传客户端 tick + partialTick，按 shader 需求缩放。
  - `screenSize` / `ScreenSize` / `OutSize`：窗口或目标 framebuffer 尺寸，resize 后必须更新。
  - `yaw`、`pitch`、`CameraYaw`、`CameraPitch`：相机角度，天空/星空/体积效果使用。
  - `cameraPos`、`entityPos`、`projectilePos`、`lightPos`：世界空间定位。
- 常见采样器：
  - `Sampler0`：基础贴图或遮罩贴图。
  - `DepthSampler` / `MainDepthSampler` / `DiffuseDepthSampler`：深度纹理；需要可读取的深度拷贝或目标。
  - `MainColorSampler` / `ColorSampler` / `ScreenTexture` / `MainTarget0`：主画面颜色；用于折射、扭曲、屏幕空间混合。
  - `Texture0/1/2`、`ColorTexture0`、`WarpTexture0`、`BloomTexture0`、`StarTexture0`：轨迹/刀光类效果的噪声、色带、扭曲和 bloom mask。
- 常见渲染状态：
  - 多数 core shader 使用 `blend add srcalpha 1-srcalpha`。
  - `flowing_gradient`、`spotlight` 使用加法光照风格 `one + one`。
  - `program/transparency` 使用覆盖式合成 `one + zero`。
- 运行时可配置优先级：
  - JSON uniform 默认值适合资源载入，但真正效果应由 Java 每帧写入。
  - GLSL 内 `const` / `#define` 是编译期参数；如要给配置文件或方块实体动态控制，需要改为 uniform。

## Core Shader 逐项说明

| Shader | 效果 | 可配置点 | 1.20.1 应用建议 |
| --- | --- | --- | --- |
| `black_hole` | 当前正式黑洞库。读取主画面颜色和深度，源式全屏引力透镜，球内渲染不透光事件视界、吸积盘、haze 和光子环。 | `entityPos`、`scale`、`accretionDiskDensity`、`tiltAngle`、`intensity`、`renderQuality`、`ditherStrength`、`diskNoiseStrength`、`diskTextureStrength`、吸积盘颜色/ramp。 | 已由 `HbmBlackHoleEffects` 接入。用于普通奇点、黑洞实体和后续反物质/奇点类效果；调用方使用 `BlackHoleSpec`，不要直接写 shader uniform。 |
| `black_hole_pro` | 简化但仍较重的黑洞体积吸积盘。深度遮挡更直接，常量包含吸积盘内外半径、噪声、颜色 ramp、128 次迭代。 | `entityPos`、`scale`、`time`；吸积盘宽度、颜色和迭代数目前是 const。 | 当前路线按用户要求舍弃 `black_hole_pro`，保留为源码对照，不作为正式库目标。 |
| `black_square` | 基于深度重建世界表面的黑色/发光方块网格，按法线投影，带随机脉冲和噪声。 | `time/iTime`、`screenSize`、camera/matrix；方块密度、尺寸、边框、移动速度为 const。 | 可做维度侵蚀、黑色裂解地表覆盖。依赖主深度，适合后处理或区域屏幕空间效果。 |
| `black_world` | 以 `projectilePos` 为中心的世界空间闪光/暗化/色散爆发。天空和场景几何分别处理。 | `projectilePos`、`lightIntensity`、`effectAlpha`、`time`、camera/matrix。 | 适合弹丸、冲击波、特殊爆炸预闪。应由客户端效果管理器按实体位置驱动。 |
| `chaos` | 全屏/材质混沌背景。`useType` 切换云雾、洞穴/混沌、银河。 | `useType`、`time`、`screenSize`、`yaw`、`pitch`。 | 可作为 GUI/特殊物品材质或维度天空背景。若用于方块表面，需要遮罩纹理或 quad。 |
| `cosmic` | 多模式宇宙材质：原始星云、流光溢彩、梦幻泡泡/萤火虫、混沌反射、黑金碎片、幻境森林等。 | `useType` 0-6、`externalScale`、`opacity`、`cosmicuvs[10]`、`time`、相机角。大量风格参数是 const。 | 最适合特殊物品、能量核心、维度门、块实体内部材质。需要自定义 RenderType，顶点格式含 `Position/Color/UV0/UV2/Normal`。 |
| `cosmic_neo` | `cosmic` 的扩展/重调色版本，`useCosmicType` 提供更多底色和星云类型，`cosmicColor0` 可传主色。 | `useCosmicType`、`cosmicColor0`、`externalScale`、`opacity`、`cosmicuvs`、`screenSize`。 | 推荐作为正式宇宙材质主版本，因为颜色可由 Java 或方块实体控制。 |
| `fantasy_block` | 方块表面梦幻/魔法色彩，混合基础贴图、时间和雾。 | `time`、`screenSize`、`yaw/pitch`、fog/color；核心风格在 GLSL 内。 | 可给幻想类矿石/特殊装饰块。按 block RenderType 接入即可，不需要主画面采样。 |
| `floating_shapes` | 纹理遮罩上叠加漂浮几何/形状动画，受 `Opacity` 和 `GameTime` 控制。 | `Opacity`、`GameTime`、`ColorModulator`。 | 适合 GUI 图层、方块面板、能量护盾局部材质。实现成本低，可作为首批验证 shader。 |
| `flowing_gradient` | 沿遮罩流动的多色渐变。支持最多 16 个 int 颜色和渐变跨度。 | `FlowSpeed`、`colorCount`、`color1`-`color16`、`alpha`、`gradientSpan`、`time`。 | 很适合 HBM 能量管线、激光、充能槽、危险警示流光。建议把 int 颜色统一封装成工具方法写 uniform。 |
| `glow_edge` | 遮罩边缘/实体边缘发光，多模式：银河、折射、流动渐变、粉彩彩虹、原纹理、纯色。 | `uType` 1-5、`uColor`、`color1/2/3`、`screenSize`、`time`，采样 `ScreenTexture/TargetTexture`。 | 可做高亮选择框、护盾外壳、传送门边缘。折射模式需要屏幕纹理；纯色/彩虹模式可先迁。 |
| `iap` | 高对比星空背景，含大星、星河、超新星、虫洞、脉冲星、彗星等高级效果。 | `StarScale`、`MoveSpeed`、`EnableSphericalMapping`、`EnableAdvancedEffects`、`StarCount`、`StarMask`、`StarMSpeed`、`StarMScale`、`MaxStarCount`。 | 适合天空盒、空间维度、宇宙方块内部。比 `starfield` 更像调优版，但性能仍要注意星数上限。 |
| `mandelbrot` | 分形/曼德布罗特风格材质，随时间和相机角变化。 | `time`、`screenSize`、`yaw/pitch`、fog/color；分形细节多为内部常量。 | 可做奇异材料、异次元矿石或 GUI 背景。低接入风险。 |
| `mandelbulb_shader` | 世界空间 Mandelbulb 球体 raymarch，读取深度并遮挡。中心和半径当前写死。 | `time`、camera/matrix、深度；`SPHERE_CENTER`、`SPHERE_RADIUS`、迭代数为 const。 | 若用于实体必须先把球心/半径改成 uniform，避免写死 `vec3(14,-50,0)`。 |
| `melee_trail` | 近战/刀光轨迹材质。支持滚动纹理、扭曲、双纹理叠乘、宇宙填充、色带映射、背景折射混合。 | `useType` 1-5、`speed`、`brightness`、`cosmicScale`、`starUV`、各纹理采样器。 | 适合能量剑、激光刃、粒子轨迹。需要一套轨迹 mesh 或粒子 ribbon，以及多张噪声/色带贴图。 |
| `nebula_cube` | 体积星云立方/球状区域，读取主画面和深度，做体积 raymarch 与遮挡。 | `entityPos`、`scale`、`time`、camera/matrix；步数和完整度是 define/const。 | 可做大型能量核心、空间裂隙。成本较高，应限制屏幕面积和数量。 |
| `partial_darkness` | 以实体位置为中心的局部黑暗/星云暗化，含深度重建、天空处理和色散。 | `entityPos`、`lightIntensity`、`effectAlpha`、`time`、camera/matrix。 | 可做黑洞、负物质、诅咒区域的屏幕空间后效。需要深度和颜色目标。 |
| `ragnarok_block` | 基于噪声的红/黑末日块材质，强混合效果色。 | `time`、`screenSize`、fog/color；噪声 octave 是 define。 | 可用于高危装饰块或末日污染方块。接入简单。 |
| `rainbow_slime_block` | raymarch 彩虹黏液/软体球风格材质。 | `time`、`screenSize`、fog/color；步数、距离、表面阈值为 define。 | 可用于特殊液态/软体块。注意透明排序和性能。 |
| `rendertype_glass_sphere` | 简单玻璃球 Fresnel、反射色、边缘光。 | `GameTime`，其余颜色/alpha 在 GLSL 内。 | 适合作为雪景球、容器玻璃、能量球的首批简单玻璃 shader。 |
| `shader` | 基础屏幕空间球体光照 shader，写死球心/半径，带噪声细节。 | `time`、camera/matrix、深度；球心/半径/光照颜色是 const。 | 可作为学习模板，不建议直接用于正式内容，需参数化中心/半径。 |
| `sky_block` | 天空/云雾块材质，带体积噪声和可选纹理。 | `uColor`、`time`、`screenSize`、`yaw/pitch`；步数和噪声 octave 是 define。 | 可用于天空盒方块、维度门内部、特殊玻璃。 |
| `spotlight` | 体积聚光灯。根据深度重建位置，计算锥角、范围、方向和颜色衰减。 | `lightPos`、`lightDir`、`lightRange`、`innerConeAngle`、`outerConeAngle`、`lightColor`、`lightIntensity`。 | 适合炮塔探照灯、机器照明、警戒光束。需要全屏或体积 pass，并管理深度采样。 |
| `starfield` | 可配置星空背景，支持球面贴图、高级星河/超新星/虫洞/彗星、大星层。 | 与 `iap` 基本相同，额外有 `Opacity`、`ColorModulator`。 | 适合天空、GUI、空间维度。若只要星空，先用这个；若要更强对比，选 `iap`。 |
| `star_sky` | 体积星云天空，分形迭代和体积步进较固定。 | `time`、`iZoom`、`screenSize`、`cameraPos`、`yaw/pitch`；迭代/亮度/饱和度为 define。 | 适合维度天空或传送门背景。接入中等，主要需要相机角同步。 |
| `sun` | Mandelbulb 太阳/能量球，和 `mandelbulb_shader` 类似但偏发光天体。 | `time`、camera/matrix、深度；球心/半径/迭代为 const。 | 可做远景太阳或能量实体。正式使用前参数化球心和半径。 |
| `text` | 文本/图标遮罩内填充特效。`useType` 切换云、纹理混合、纯色、银河。 | `useType` 1-3 和默认银河、`solidColor`、`time`、`screenSize`、`yaw/pitch`、`Texture0/1`。 | 适合 GUI 字体、徽章、说明书标题、特殊物品名称贴花。 |
| `trail` | 简化轨迹 shader：用遮罩亮度作为 alpha，流动渐变并与主画面混合。 | `time`、`screenSize`、`Texture0`、`ColorTexture0`、`MainTarget0`。 | 比 `melee_trail` 更容易接入，可先给弹道/能量线测试。 |
| `volumetric_shader` | 黄金比例/体积光风格全屏图案，ACES tone map。 | `time`、`screenSize`、`yaw/pitch`；颜色矩阵在 GLSL const。 | 适合维度背景或纯视觉测试。无深度依赖，迁移成本低。 |
| `warp` | 玻璃/折射球局部材质，采样屏幕与目标纹理，带 Fresnel 反射。 | `uColor`、`uType`、`time`、`screenSize`；折射步数为 const。 | 可做护盾球、传送门表面。需要提供 `ScreenTexture` 和 `TargetTexture`。 |
| `warp_world` | 世界屏幕扭曲，多模式：玻璃球、冲击波、镜面、高级镜面。 | `useType` 0-3、`intensity`、`uColor`、`time`、`screenSize`。 | 适合爆炸冲击波、空间扭曲、镜面护盾。强依赖主画面采样，应作为中后期迁移。 |
| `water_block` | 动态水面/能量液体材质，基础贴图透明处生成程序水纹。 | `time`、`screenSize`、`yaw/pitch`、fog/color；水纹细节 scale 是 const。 | 适合特殊流体方块或机器液窗。接入简单，但需确认与 Forge fluid render 的关系。 |

## Program 与 PostChain 说明

| Shader | 效果 | 可配置点 | 1.20.1 应用建议 |
| --- | --- | --- | --- |
| `program/invert_colors` | 全屏反色/强度蒙版。`UseType` 0 全屏，1 中心圆扩散，2 自上而下扫描线，3 分形噪声，4 Voronoi 细胞，5 裂缝，6 马赛克，7 径向扭曲，8 额外分支。 | `InvertStrength`、`UseType`、`ScreenSize`。 | 可用于 EMP、闪光、眩晕、辐射异常、传送效果。配合 `post/invert_colors.json` 作为首个 PostChain 试验最合适。 |
| `program/transparency` | 多层透明/粒子/天气/云 framebuffer 合成，并附带屏幕空间折射玻璃球实验。按深度排序若干层后合成。 | `Time`、`OutSize`、`ProjMat`；`NUM_LAYERS`、`STYLE`、球体中心/半径等多为 shader 内部常量或从像素编码读取。 | 高风险。需要 Minecraft Fabulous/半透明目标兼容知识，且当前 clean port 没有对应 framebuffer 管线。建议暂不作为首批迁移。 |
| `post/invert_colors` | PostChain：`minecraft:main -> swap -> minecraft:main`，pass 名为 `arcanevortex:invert_colors`。 | pass program 和 uniform 需由 Java 获取后设置。 | 迁入时改成 `hbm:invert_colors`，路径放 `assets/hbm/shaders/post/invert_colors.json`。 |
| `post/warp` | PostChain：同样主目标到 swap 再 blit，pass 名为 `arcanevortex:warp`。 | 当前 `program` 目录没有 `warp.json`，只有 core `warp`；需要补齐 program 资源或改为 core/RenderType 用法。 | 迁移前先确认缺失 program 是否在其他源码包中，否则该 post 链不能直接工作。 |

## 建议的 1.20.1 接入路线

1. 先建资源目录：
   - `src/main/resources/assets/hbm/shaders/core`
   - `src/main/resources/assets/hbm/shaders/program`
   - `src/main/resources/assets/hbm/shaders/post`
2. 复制目标 shader 三件套或 program 文件，并把 JSON 内 `arcanevortex:<name>` 改为 `hbm:<name>`。
3. 新增客户端 shader 注册类：
   - 在 mod event bus 监听 `RegisterShadersEvent`。
   - 对 core shader 使用 `new ShaderInstance(resourceProvider, HBM.rl("<name>"), vertexFormat)` 注册。
   - 保存 `ShaderInstance`，供自定义 `RenderType.ShaderStateShard` 或 renderer 取用。
4. 新增 runtime uniform 写入层：
   - 每次渲染前写 `time`、`screenSize`、相机角、相机位置。
   - 对实体/方块实体效果写 `entityPos`、`scale`、`intensity`、`useType`。
   - 对颜色类效果统一提供 `setVec3/setVec4/setPackedColorInt` 工具。
5. 对需要主画面采样的 shader，优先接入现有库或补齐专用 framebuffer copy：
   - `black_hole` 已由 `HbmBlackHoleEffects` 封装，调用方只传 `BlackHoleSpec`。
   - `warp_world useType=1` 已由 `HbmRenderEffects` 封装为冲击波 API。
   - `black_square`、`black_world`、`partial_darkness`、`nebula_cube`、`spotlight`、`warp` 仍需单独设计 scene color/depth copy 或 PostChain pass，否则采样器为空会黑屏、白屏或只显示遮罩。
6. 对不依赖深度/主画面的 shader，可作为首批低风险验证：
   - `flowing_gradient`
   - `floating_shapes`
   - `rendertype_glass_sphere`
   - `text`
   - `fantasy_block`
   - `ragnarok_block`
   - `starfield` / `iap`
   - `cosmic_neo`
7. 对 PostChain：
   - 加载 `PostChain` 时机应在 client resource reload 后。
   - 窗口尺寸变化后调用 resize 或重建。
   - 所有后处理应有客户端配置开关和性能档位。
   - `program/invert_colors` 可作为首个后处理验证目标。

## HBM 内容应用映射

- 反物质、奇点、黑洞类：
  - 当前正式路线使用 `black_hole` + `HbmBlackHoleEffects.BlackHoleSpec`。
  - `black_hole_pro` 舍弃为源码对照，不作为正式库目标。
  - 普通 `singularity` 已通过 `VortexEntity` 外挂 shader 黑洞渲染；服务端吸引、吞噬、破坏方块等机制不由 shader 库负责。
  - `partial_darkness` 可在后续作为额外区域暗化叠加，但不替代 `black_hole` 本体。
- 核爆、冲击波、弹丸闪光：
  - `black_world` 用于爆发中心色散和强光。
  - `warp_world useType=1` 用于球形冲击波扭曲。
  - `program/invert_colors UseType=1/2/5` 用于闪屏、扫描和裂缝式冲击。
- 能量管线、激光、机器充能：
  - `flowing_gradient` 适合多色流动材质。
  - `trail` 适合弹道能量线。
  - `melee_trail` 适合武器刀光和复杂轨迹。
- 传送门、维度、宇宙装置：
  - `cosmic_neo`、`cosmic`、`starfield`、`iap`、`star_sky`。
  - 如果只做方块内部材质，优先 `cosmic_neo`。
- 护盾、玻璃、力场：
  - `rendertype_glass_sphere` 是低风险起点。
  - `warp` / `warp_world` 提供更强折射，但需要主画面纹理。
  - `glow_edge` 可做护盾边缘和命中高亮。
- 异常污染、裂解、末日材质：
  - `black_square`、`ragnarok_block`、`mandelbrot`、`volumetric_shader`。
- 探照灯/警示光：
  - `spotlight` 可做体积锥光，但必须等深度采样框架稳定后接入。

## 迁移注意事项

- 不要一次性迁入全部 shader。先用一个低风险 core shader 和一个 post shader 打通资源、注册、uniform、reload、resize。
- 所有客户端类必须放在 client-only 包或通过 `Dist.CLIENT` 事件加载，避免 dedicated server 加载 Minecraft client 类。
- `screenSize`、`ScreenSize`、`OutSize` 命名不统一，Java 写 uniform 时必须按 shader 实际名称逐个处理。
- `modelViewMatrix` 和 `ModelViewMat` 同时出现；部分 shader 用小驼峰版本做深度重建，不能只依赖 vanilla 自动 uniform。
- 写死世界坐标的 shader 包括 `shader`、`sun`、`mandelbulb_shader` 等，正式使用前必须把中心/半径改成 uniform。
- `cosmic` 默认 `externalScale` JSON 值为 `0.0`，shader 内会计算 `1.0 / externalScale`；实际渲染前必须写入非零值。
- `post/warp.json` 引用 `arcanevortex:warp`，但 `program` 目录没有对应 `warp` program 文件。它不能直接作为 PostChain 使用，除非补齐 program 资源或改管线。
- `program/transparency` 读取 Fabulous 风格多目标：translucent、item entity、particles、clouds、weather 及其 depth。clean port 未建立这些目标绑定前不应迁移。

## 推荐首批验证清单

- 资源验证：
  - 迁入 `flowing_gradient` 到 `assets/hbm/shaders/core`。
  - 新增一个临时客户端 RenderType，在简单 quad 或测试 BlockEntity 上渲染。
  - 每帧设置 `time`、`FlowSpeed`、`colorCount`、`color1`-`color3`、`alpha`、`gradientSpan`。
- 后处理验证：
  - 迁入 `program/invert_colors` 和 `post/invert_colors`。
  - 改 namespace 为 `hbm`。
  - 添加配置开关和按键/调试命令触发 `InvertStrength` 0 -> 1。
  - 测试 `UseType` 0、1、2、5、7。
- 稳定性检查：
  - 进入世界、切换全屏、调整窗口大小、F3+T 重载资源。
  - dedicated server 启动不加载任何 client shader 类。
  - shader 编译失败时记录日志并禁用效果，不影响游戏启动。

## 后续工程记录建议

- 新增库移植文档：`工程记录/库移植/<date>-modern-effect-render-library-源码功能追踪.md`
  - 专门记录 Java 侧 shader 注册、PostChain 管理、RenderType 工厂、uniform 写入工具。
- 每个真实内容接入时，在对应机器/物品/实体迁移文档中引用本文，并记录实际使用的 shader、uniform 配置和性能限制。

## 2026-05-27 核爆 warp_world 冲击波试移植

- 范围：
  - 只接入 `core/warp_world` 的 `useType=1` 冲击波模式。
  - 不迁移 `glassBall`、`mirror`、`advancedMirror` 的内容使用点。
- 资源：
  - 新增 `assets/hbm/shaders/core/warp_world.{json,vsh,fsh}`。
  - 原始 JSON 的 attributes 缺少 `Normal`，但 vsh 读取 `in vec3 Normal`；1.20.1 侧改为 `Position, UV0, Color, Normal`，注册格式使用 `DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL`。
- Java 入口：
  - `ClientModEvents#registerShaders(...)` 注册 `hbm:warp_world`。
  - `HbmRenderEffects` 作为客户端渲染库入口，负责 shader 注册后的持有、screen copy、tick、render、离开世界清理。
  - 通用 API：`HbmRenderEffects.spawnWarpWorldShockwave(x, y, z, WarpWorldShockwaveSpec.of(waveScale, lifetime)...)`。
  - 通用同步 API：`HbmRenderEffects.spawnWarpWorldShockwave(x, y, z, spec, initialAge)`；用于爆炸实体/网络包已经有当前年龄时，让 warp 半径直接对齐当前冲击波进度。
  - 核爆便捷 API：`HbmRenderEffects.spawnNuclearWarpShockwave(x, y, z, waveScale, lifetime)`。
  - Torex 核爆 API：`HbmRenderEffects.spawnTorexWarpShockwave(x, y, z, initialAge)`，用于真实核装置/起爆器生成的 `NukeTorexEntity`。
  - `HbmParticleEffects#spawnMuke(...)` 在 `MukeWaveParticle.create(...)` 成功并加入粒子引擎时，同步调用核爆便捷 API。
  - `NukeTorexEntity#clientVisualTick(...)` 在非 warm-start 的首次真实客户端视觉 tick 调用 Torex API，并传入当前 `tickCount` 作为 `initialAge`；起爆器日志中的 `[DET] Tried to detonate...` 路线走这里，而不是 `ParticleUtil.TYPE_MUKE` 粒子路线。
  - `ClientForgeEvents#onRenderLevelStage(AFTER_WEATHER)` 调用 `HbmRenderEffects.render(...)`；`AFTER_WEATHER` 是 Forge 1.20.1 `LevelRenderer` 源码中明确 dispatch 的世界后段阶段。
  - `ClientForgeEvents#onClientTick(END)` 推进特效年龄；离开世界时通过 `clearNetworkState()` 清理。
- 半径与时序约束：
  - warp 冲击波和 `MukeWaveParticle` 同时释放。
  - 半径公式与 `MukeWaveParticle` 保持一致：
    - `waveRadius = (1 - exp(-(age + partialTick) * 0.125)) * waveScale`
    - `warpRadius = waveRadius + 1.0`
  - 因此它会随着核爆冲击粒子一起放大，并始终位于冲击波粒子外缘一格。
  - Torex 核爆的 SHOCK cloudlet 前沿不是上述指数公式，而是：
    - `shockRadius ~= (tickCount * 1.5 + random) * 1.5`
    - warp 线性半径使用 `warpRadius = age * 2.25 + 1.0`，对应 SHOCK cloudlet 外缘再外推一格。
    - SHOCK cloudlet 生成到 `tickCount < 150`；warp 默认持续 `150 + 80 = 230` tick，150 tick 前保持满强度，之后淡出。
  - 核爆 HUD 白闪持续 5 秒，约 100 tick；核爆便捷 API 会先覆盖这段不可见窗口，再额外保留至少 80 tick 的闪光后可见窗口。
  - 默认 45 格 `waveScale` 的 warp 冲击波约持续 180 tick；更大 `waveScale` 会按半径推进到约 99.9% 的时间自动延长。
  - 强度至少在白闪结束前保持满值，并与半径约 97% 的推进点取较大值后才开始淡出，避免原先 25 tick 线性淡出时看不清。
  - y 位置沿用 `MukeWaveParticle` 的渲染偏移：中心渲染时减 `0.25` 格，使球面赤道贴合原平面冲击波视觉高度。
- 渲染路径：
  - 每帧先把主 RenderTarget 颜色 blit 到客户端 `TextureTarget sceneCopy`。
  - `ScreenTexture`、`TargetTexture`、`Sampler0` 均绑定到 `sceneCopy`；当前 `useType=1` 实际只依赖 `ScreenTexture`。
  - 用球面 quad mesh 作为 shader 承载体；shader 通过法线和视角计算边缘扭曲。
- 客户端配置：
  - `hbm-client.toml` / `effects.nukeWarpShockwave`：总开关，默认 `true`。
  - `effects.nukeWarpShockwaveIntensity`：扭曲强度倍率，默认 `1.75`，范围 `0.0-8.0`。
  - `effects.nukeWarpShockwaveMeshSegments`：球面水平分段数，默认 `48`，范围 `12-96`；垂直环数按分段数约三分之一计算。
  - `effects.debugNukeWarpShockwaveWireframe`：调试开关，默认 `false`；开启后渲染淡蓝色线框，用于确认 API 触发和球面 pass 是否实际画出。
- 可见性增强：
  - `warp_world.fsh` 的 `useType=1` 分支将最大扭曲从原始 `0.08 * intensity` 提高到更明显的冲击波级别，并提高边缘高光。
  - 增加轻微时间脉动，让冲击波经过复杂背景时更容易被看见。
- 2026-05-27 形态修正：
  - 用户实测确认 warp 已触发，但视觉像完整镜面/玻璃球。
  - `HbmRenderEffects#render(...)` 重新启用 depth test 和 cull；冲击波球面切入方块、地形或其他已写入深度的几何体时，对应部分不再渲染。
  - `warp_world.fsh` 的 `shockwave(...)` 改为薄壳边带：只保留视线切线附近的折射/淡边，其余球面片段 `discard`，避免完整玻璃球观感。
  - shader 使用顶点 alpha 参与 `effectiveIntensity`，因此 Java 生命周期里的 `alpha(progressAge)` 会真实控制强度和透明度衰减。
  - Torex 核爆 warp 从 `100 tick` 后开始淡出，避开 5 秒白闪后逐渐衰减到 `230 tick` 结束。
- 2026-05-27 衰减优化：
  - 用户实测希望衰减更明显。
  - Java 生命周期 alpha 从线性淡出改为立方曲线：`alpha = (1 - fadeProgress)^3`，白闪结束后强度会更快下降。
  - shader 内 `effectiveIntensity` 改为 `intensity * vertexAlpha * vertexAlpha`，让折射强度和可见边带随顶点 alpha 二次衰减。
  - Torex 核爆 warp 仍从 `100 tick` 后开始淡出，但白闪后窗口扩展到 `120 tick`；整体变成“快速变弱、弱余波保留更久”，而不是最后突然消失。
- 2026-05-27 通用 API 补足：
  - `spawnWarpWorldShockwave(x, y, z, spec, initialAge)` 已公开，不同爆炸可以主动传入冲击波参数并指定当前年龄。
  - `WarpWorldShockwaveSpec` 当前支持两类半径模型：
    - `WarpWorldShockwaveSpec.of(waveScale, lifetime, fadeStartTick)`：指数扩张，半径为 `(1 - exp(-age * 0.125)) * waveScale + outerEdgeOffset`，适合 `MukeWaveParticle` 这类逐渐逼近最大半径的粒子波。
    - `WarpWorldShockwaveSpec.linear(radiusPerTick, lifetime, fadeStartTick)`：线性扩张，半径为 `age * radiusPerTick + outerEdgeOffset`，适合 Torex SHOCK cloudlet、导弹冲击环、EMP 环形外推等恒速前沿。
  - 可配置链式参数：
    - `withIntensity(intensity)`：折射/边带强度；仍会乘客户端总配置 `effects.nukeWarpShockwaveIntensity` 的便捷 API 预设值。
    - `withOuterEdgeOffset(offset)`：让 warp 位于实际粒子前沿外侧，核爆默认 `+1.0` 格。
    - `withYOffset(yOffset)`：调整球心高度，用于贴合地面粒子或空爆中心。
    - `withMeshSegments(segments)`：球面精度，建议普通爆炸 `24-36`，核爆 `48`，极大爆炸再提高。
    - `withFadeStartTick(fadeStartTick)`：衰减开始 tick；之后按立方曲线淡出。
  - `initialAge` 语义：
    - 新爆炸刚生成时传 `0`。
    - 客户端收到已有爆炸实体或同步包时传实体当前年龄，例如 `tickCount`，避免 warp 从爆心重新开始扩散。
    - 如果 `initialAge >= spec.lifetime()`，库会直接忽略，避免生成已经结束的效果。
  - 示例：
    - 小型线性爆炸：`spawnWarpWorldShockwave(x, y, z, WarpWorldShockwaveSpec.linear(1.2F, 70, 20).withIntensity(0.8F).withMeshSegments(24), 0)`。
    - 导弹/高速冲击环：`linear(2.0F, 100, 45).withIntensity(1.2F).withOuterEdgeOffset(1.0F)`。
    - 指数式粒子波：`of(45.0F, 180, 100).withYOffset(-0.25F).withIntensity(1.75F)`。
- 2026-05-27 不可见问题修正：
  - `RenderTarget#bindRead()` 在 1.20.1 中只是绑定颜色纹理，不是绑定 `GL_READ_FRAMEBUFFER`；原先 screen copy 很可能没有真实复制主画面。
  - `HbmRenderEffects#copyMainTarget(...)` 改为显式绑定 `GL_READ_FRAMEBUFFER = mainTarget.frameBufferId` 与 `GL_DRAW_FRAMEBUFFER = sceneCopy.frameBufferId` 后再 blit。
  - 阶段性排查时曾将 warp 冲击波渲染 pass 改为 `disableDepthTest + depthMask(false)`，用于确认 shader pass 是否实际画出；实机确认后已在形态修正中恢复 depth test。
  - shader 输出增加低强度可见边缘 alpha/highlight，便于实机确认 shader pass 是否成功跑起来。
  - 新增调试线框：若线框可见但扭曲不可见，则问题集中在 shader/screen texture；若线框也不可见，则问题集中在核爆触发或 render stage。
  - 2026-05-27 二次定位：不再依赖 `AFTER_LEVEL`，因为该 stage 在当前 Forge 1.20.1 源码中仅定义为“after everything”自定义阶段，并未在检索到的 `LevelRenderer#renderLevel` 主路径中稳定 dispatch；warp pass 改挂 `AFTER_WEATHER`。
  - 线框调试开启时，客户端日志每秒输出一次 `HBM render effects: ...`，并在 spawn 时记录活动数量、位置、`waveScale` 与生命周期。
  - 2026-05-27 三次定位：实机日志显示起爆器成功触发核装置，但没有 muke 粒子路线的证据。核装置由 `NuclearExplosionUtil -> NukeTorexEntity.createStandard(...)` 生成，因此 warp 必须挂到 `NukeTorexEntity` 的客户端视觉生命周期。此前只挂在 `HbmParticleEffects#spawnMuke(...)`，会导致真实核装置爆炸时 warp 根本不生成。
- 验证：
  - `.\gradlew.bat compileJava processResources --no-daemon` 通过。

## black_hole 现有 API 与 1.20.1 应用指南

### 资源与定位

- 正式黑洞路线只使用 `black_hole`，`black_hole_pro` 保留为源码对照，不再作为迁移目标。
- 当前资源：
  - `src/main/resources/assets/hbm/shaders/core/black_hole.json`
  - `src/main/resources/assets/hbm/shaders/core/black_hole.vsh`
  - `src/main/resources/assets/hbm/shaders/core/black_hole.fsh`
- 当前 Java 入口：`com.hbm.ntm.client.render.HbmBlackHoleEffects`。
- 当前内容接入点：`ClientForgeEvents#updateBlackHoleShaders(float partialTick)` 扫描 `VortexEntity` 与 `BlackHoleEntity`，实体 renderer 使用 `NoopRenderer`，视觉完全交给 shader 库。
- 普通奇点的机制仍由 `VortexEntity` / `BlackHoleEntity` 保持：吸引、吞噬、破坏方块、转 rubble、伤害、缩小和消失都不写进渲染库。

### 正确渲染方式

- `HbmBlackHoleEffects` 在 `RenderLevelStageEvent.Stage.AFTER_LEVEL` 执行全屏 pass，基于最终世界主画面做引力透镜和黑洞体积合成。
- 每绘制一个黑洞前都会把当前主 `RenderTarget` 的 color + depth 复制到 `sceneCopy`：
  - `MainColorSampler` 绑定 `sceneCopy` color。
  - `MainDepthSampler` 绑定 `sceneCopy` depth。
  - `TextureSampler` 绑定程序噪声纹理。
  - `ColorSampler` 绑定程序色带纹理。
- 多个黑洞必须逐个复制当前主画面再绘制。这样后一个黑洞采样到前一个黑洞已经合成后的画面，不会互相擦除。
- shader 使用源文件式全屏透镜：
  - 屏幕射线由 `inverse(projectionMatrix * modelViewMatrix)` 的 near/far 点反投影得到，`rayOrigin` 使用 near plane 点。
  - `raySphereIntersect(...)` 只决定是否进入球内体积渲染事件视界、吸积盘和 haze。
  - 球外仍输出 `traceLensedRay(...)` 得到的 `lensedSceneColor`，不能按外层球边界 `discard`，也不能在球边界处把原图和扭曲图混成两层。
  - 不使用外层球壳、圆形边界、`impactDistance` 边界裁剪或边缘重影式混合。
- 事件视界黑球是不透光的；吸积盘和 haze 与 `lensedSceneColor` 混合。若主深度显示实心方块已经挡在黑洞中心或球内体积命中点之前，该像素直接返回原始 `sceneColor`，事件视界、吸积盘和引力透镜都不应透过方块显示。

### 半径与缩放契约

- `BlackHoleSpec.of(radius, lifetime)` 的 `radius` 是 HBM 旧版黑洞 `size` 语义，也就是调用方眼里的事件视界/黑洞大小基准。
- Java 内部统一派生 shader `scale = radius * 6.0F`。这个 `scale` 绑定整个黑洞坐标系：空间扭曲 `WarpSpace(...)`、事件视界、光子环、吸积盘、haze 和外层透镜范围都会一起放大或缩小。
- `withEventHorizonRadius(radius)` 会用同一规则重建整个 `scale`，不是只单独放大黑球。
- 不要为修视觉再拆出“黑球倍率”“透镜倍率”“吸积盘倍率”三套互不相关的大小。普通奇点直接传 `vortex.getSize()`，它缩小时黑洞整体随之缩小。
- `withAccretionDiskScale(radiusScale, thicknessScale)` 只用于特殊黑洞的艺术变体；普通奇点默认保持源 shader 比例。

### API 入口

- 一次性效果：
  - `HbmBlackHoleEffects.spawnBlackHole(x, y, z, spec)`。
  - `HbmBlackHoleEffects.spawnBlackHole(x, y, z, spec, initialAge)`，用于客户端收到已有特效时同步当前年龄。
- 实体追踪效果：
  - `HbmBlackHoleEffects.updateTrackedBlackHole(key, x, y, z, spec, age)`。
  - `key` 必须稳定，通常使用实体 id。
  - 追踪黑洞有短 TTL，调用方需要在渲染阶段或客户端 tick 持续刷新；实体消失后可调用 `removeTrackedBlackHole(key)`，未刷新也会自动清理。
- 世界切换、断线或客户端网络状态清理时调用 `HbmBlackHoleEffects.clearAll()`。

### BlackHoleSpec 可配置项

- 生命周期：
  - `withFade(fadeInTicks, fadeOutStartTick)` 控制淡入和淡出开始 tick；淡出为二次曲线。
- 强度与姿态：
  - `withIntensity(value)` 控制整体可见强度，最终会按生命周期 alpha clamp 到 `0.0-8.0`。
  - `withTiltAngle(radians)` 控制吸积盘倾角。
- 吸积盘：
  - `withAccretionDiskDensity(value)` 控制盘密度，范围 `0.0-1.0`。
  - `withAccretionDiskScale(radiusScale, thicknessScale)` 控制盘半径/厚度相对倍率，范围 `0.25-3.0`。
  - `withDiskDetail(noiseStrength, textureStrength)` 控制噪声颗粒和色带纹理贡献，范围 `0.0-1.0`。
  - `withDiskColor(r, g, b)` 设置吸积盘主色。
  - `withDiskRamp(innerR, innerG, innerB, outerR, outerG, outerB)` 设置内外色带。
- 渲染精度：
  - `withRenderPrecision(RenderPrecision.NATIVE)`：源 shader 基准，`renderQuality=1.0`、`ditherStrength=2.0`。
  - `withRenderPrecision(RenderPrecision.LOW)`：`0.5 / 2.4`，适合远景或大量小黑洞。
  - `withRenderPrecision(RenderPrecision.MEDIUM)`：`0.75 / 2.2`。
  - `withRenderPrecision(RenderPrecision.HIGH)`：`1.15 / 1.35`。
  - `withRenderPrecision(RenderPrecision.ULTRA)`：`1.45 / 0.8`。
  - `withRenderQuality(renderQuality, ditherStrength)` 可直接覆盖精度，当前 clamp 为 `renderQuality=0.35-1.6`、`ditherStrength=0.0-3.0`。
- 兼容字段：
  - `withLensBoundarySoftness(value)` 当前只保留 API 兼容；源式全屏透镜不再用它制造球壳边界，也不建议把它当作主要调参入口。

### 普通奇点接入示例

普通 `singularity` 落地后生成 `VortexEntity(level, 1.5F)`。客户端每帧按实体当前 size 生成一份 tracked black hole spec，天蓝色吸积盘由颜色 API 传入：

```java
float size = Math.max(0.05F, vortex.getSize());
int lifetime = Math.max(1, (int) Math.ceil(size / Math.max(0.0001F, vortex.shrinkRate())) + 2);
float intensity = Mth.clamp(size / 1.5F, 0.05F, 1.0F) * 1.35F;
Vec3 pos = vortex.getPosition(partialTick);

HbmBlackHoleEffects.updateTrackedBlackHole(vortex.getId(), pos.x, pos.y, pos.z,
        HbmBlackHoleEffects.BlackHoleSpec.of(size, lifetime)
                .withFade(0.0F, Math.max(1, lifetime - 20))
                .withAccretionDiskDensity(0.01F)
                .withTiltAngle((float) Math.toRadians(vortex.getId() % 90 - 45))
                .withIntensity(intensity)
                .withRenderQuality(1.6F, 0.45F)
                .withDiskDetail(1.0F, 0.35F)
                .withDiskColor(0.45F, 0.85F, 1.0F)
                .withDiskRamp(0.85F, 1.35F, 1.6F, 0.05F, 0.45F, 1.0F),
        0);
```

普通 `BlackHoleEntity` 兜底视觉可复用同一 API，只需换成橙金色盘面、稍低精度和固定 lifetime。不要给实体再写一个可见 renderer，否则会和全屏 shader 叠出双黑洞。

### 接入注意事项

- 调用方只传世界坐标、半径基准、生命周期和颜色/精度参数，不直接操作 shader uniform。
- `projectionMatrix`、`modelViewMatrix`、`cameraPos` 必须由 `HbmBlackHoleEffects` 当前帧统一写入；不要传单位矩阵，也不要跨渲染阶段缓存旧矩阵。
- 半透明水体、云、粒子和核爆云在最终主画面里会被 `MainColorSampler` 采样并参与透镜；实心方块在黑洞前方时会按 depth 直接遮住事件视界、吸积盘和透镜。
- 如果后续某种黑洞需要更大视觉范围，优先调整传入 `radius` 或库内 `LEGACY_EFFECT_RADIUS_MULTIPLIER` 的统一映射，不要单独改 shader 内某个部件半径。
- 如果吸积盘出现由中心放射的规则扇环块，优先检查 `TextureSampler` / `ColorSampler` 的采样状态是否为平铺 `REPEAT` + 双线性 `LINEAR`；不要先改 `GasDisc` 极坐标公式，也不要回到边界 `discard` 或球壳混合方案。
- 如果吸积盘只是颗粒太重或规则网格感偏强，再降低 `ditherStrength`、提高 `renderQuality` 或调低 `diskTextureStrength`。

### 2026-05-29 大型黑洞近距离与多黑洞遮挡修正

- 问题：
  - 靠近大型黑洞时，透镜射线可能被投影到相机背后或屏幕外。旧逻辑把这种 UV 直接 `clamp` 到屏幕边缘，强扭曲时会反复采样窗口边界像素，表现为画面上下/左右像两张错位图片拼接。
  - 多个黑洞逐个 pass 渲染时必须让后一个黑洞采样到前一个黑洞已经写回主目标的结果，否则远处黑洞的吸积盘光会透过近处事件视界可见。
- Java 侧修正：
  - `HbmBlackHoleEffects` 将一次性黑洞和实体追踪黑洞合并为 render job，并按距离相机从远到近排序。
  - 每个 job 仍先复制当前主 `RenderTarget` 到 `sceneCopy`，再绘制全屏 pass；因此近处黑洞会采样并遮挡远处黑洞合成后的颜色/深度。
- Shader 侧修正：
  - 曾尝试 `worldPosToScreenUVChecked(...)` / `worldDirToScreenUVChecked(...)`、`sceneDepthIsInFrontOfInfluence(...)` 和 `BackgroundColorSampler` / `black_hole_background` 背景补洞 pass，以修正前景被采样到黑洞后的问题；实机显示这些分支会产生空壳轮廓或大范围旋涡式画面崩坏，已全部废弃并从资源/Java 注册中移除。
  - 当前 `black_hole.fsh` 已重新对齐 `E:\游戏\我的世界\源码包\render\core\black_hole.fsh` 的源文件式透镜流程：单一 `MainColorSampler`、源式 `worldDirToScreenUV(...)`、源式遮挡时输出 `lensedSceneColor`。现代端仅保留 `BlackHoleSpec` 需要的颜色、盘缩放、精度、dither、噪声强度等 uniform 参数。
  - 黑洞 pass 仍保持在 `AFTER_LEVEL`，不挪到更早阶段；因此核爆云、粒子和已合成的远处黑洞仍会先进入主画面，再作为当前黑洞的透镜背景参与采样。
  - 2026-05-29 shader 编译修正：`reconstructWorldPos(...)` 会调用 `worldPosToViewDistance(...)`，GLSL 中被调用函数必须先定义或先声明；`black_hole.fsh` 已补前置声明，避免 NVIDIA 编译器报 `undefined variable "worldPosToViewDistance"` 并在资源重载阶段崩溃。
- 2026-05-30 方块遮挡与屏幕边缘采样修正：
  - `HbmBlackHoleEffects` 每个 pass 复制主目标后会显式把 `sceneCopy` color/depth 纹理设为 `GL_CLAMP_TO_EDGE`；color 维持线性过滤，depth 使用 nearest，避免屏幕空间 UV 越界时按 wrap 采到另一侧或 sampler 边界。
  - `black_hole.fsh` 不再把所有透镜 UV 强行 clamp 到固定 `0.001-0.999` 后采样；新逻辑只在 UV 位于屏幕内时采样，越界则回退到当前像素原始 `sceneColor`。这样能去掉遮挡方块边缘由 wrap/clamp 边界采样造成的透明线，同时避免完全遮挡黑洞时只剩一圈边缘采样圆环。
  - 当前像素已有实心场景深度且位于黑洞中心/球体进入点/体积命中点之前时，shader 直接输出原始 `sceneColor` 并保留 `sceneRawDepth`，不再输出 `lensedSceneColor`。这使事件视界、吸积盘和外围空间扭曲都被前景方块完整遮挡。
- 渲染阶段修正：
  - `ClientForgeEvents#onRenderLevelStage(...)` 在 `AFTER_LEVEL` 先渲染 `NukeTorexRenderer` 的核爆云 cloudlets，再执行 `HbmBlackHoleEffects.render(...)`。
  - 因为黑洞每个 pass 都会先复制当前主画面，这样核爆云的所有手绘 cloudlet 已经在 `MainColorSampler` 中，可被黑洞透镜采样；此前黑洞先渲染、核爆云后渲染，导致核爆云永远不会被黑洞采样。
- 验证重点：
  - 在大型黑洞事件视界边缘附近移动视角，屏幕不应出现横向/纵向拼接错位。
  - 黑洞半出屏时，不应出现从屏幕边缘/黑洞边缘延伸出的锐利直线分割。
  - 黑洞一半在地上、一半在地下时，画面不应沿地形/中心深度形成明显的切片分割。
  - 黑洞被地形遮挡一部分时，远景不应同时出现一份正常图和一份扭曲图的重影。
  - 玩家面前的生物或方块不应被透镜重采样到黑洞事件视界/吸积盘后面。
  - 核爆云 cloudlets 位于黑洞背后时，应参与黑洞透镜采样；位于前方时仍按深度挡住黑洞采样。
  - 让一个小黑洞处于另一个大黑洞事件视界背后，小黑洞吸积盘不应透过近处事件视界可见。

### 验证清单

- 同一世界同时存在两个普通奇点时，两个黑洞都能显示且互不擦除。
- 走路、疾跑、跳落着地时，黑洞图形与实体采样点稳定在世界位置，不随屏幕漂移。
- 引力透镜没有圆形球壳边界，也没有旧边界位置的重影。
- 事件视界为不透光黑球；吸积盘、光子环和空间扭曲随 `VortexEntity#getSize()` 一起缩小。
- 实心方块遮挡黑洞时，方块前景像素应保持原画面，不能透出事件视界或引力透镜。
- 水体、云层和核爆云在黑洞背后会被透镜采样，不应比事件视界更亮地盖住黑洞本体。
- `.\gradlew.bat compileJava processResources --no-daemon` 作为资源和 Java 接线回归检查；实机重点看普通/Fabulous 管线下的主画面 copy 与 depth 遮挡。
