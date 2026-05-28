# 普通奇点与黑洞实体 1.7.10 源码功能追踪

## 范围

- 本批只迁移普通 `singularity`（中文：奇点）。
- 包含普通奇点掉落到地面时生成黑洞的旧版逻辑，以及对应 `EntityVortex` / `EntityBlackHole` 玩法机制。
- 不迁移 `singularity_counter_resonant`、`singularity_super_heated`、`black_hole`、`singularity_spark`、黑洞导弹/手榴弹等变体。
- 旧版 OBJ/贴图式 `RenderBlackHole` 不迁入；现代端改用 `HbmBlackHoleEffects` 的 `black_hole` shader API。

## 1.7.10 源文件

- `src/main/java/com/hbm/items/ModItems.java`
  - `singularity = new ItemDrop().setUnlocalizedName("singularity").setMaxStackSize(1).setCreativeTab(MainRegistry.controlTab).setContainerItem(ModItems.nuclear_waste).setTextureName("hbm:singularity")`
- `src/main/java/com/hbm/items/special/ItemDrop.java`
  - `onEntityItemUpdate(...)` 在 `entityItem.onGround` 时检查普通奇点。
  - 条件：`stack.getItem() == ModItems.singularity && WeaponConfig.dropSing`。
  - 服务端生成 `EntityVortex(world, 1.5F)`，位置为掉落物位置。
  - 之后 `entityItem.setDead()` 并返回 `true`。
- `src/main/java/com/hbm/entity/effect/EntityBlackHole.java`
  - `dataWatcher[16]` 保存 `size`。
  - `breaksBlocks = true`，`noBreak()` 可关闭破坏。
  - 每 tick 随机射线破坏液体/方块，方块转 `EntityRubble`。
  - 范围 `size * 15` 内吸引实体；非掉落物会绕 Y 轴旋转 15 度形成旋吸。
  - 距离 `< size * 1.5` 时用 `ModDamageSource.blackhole` 造成 1000 伤害，非生物实体直接删除。
  - 吃到 `pellet_antimatter` 或 `flame_pony` 时黑洞消失并产生 5F 爆炸。
- `src/main/java/com/hbm/entity/effect/EntityVortex.java`
  - 继承 `EntityBlackHole`。
  - 默认 `shrinkRate = 0.0025F`。
  - 每 tick 先 `size -= shrinkRate`，`size <= 0` 时消失。
  - 普通奇点初始 size `1.5F`，默认约 600 tick 结束。
- `src/main/java/com/hbm/entity/EntityMappings.java`
  - `EntityBlackHole` 注册名 `entity_black_hole`，tracking range 250。
  - `EntityVortex` 注册名 `entity_vortex`，tracking range 250。
- `src/main/java/com/hbm/render/entity/effect/RenderBlackHole.java`
  - 旧渲染用 `Sphere.obj`、`BlackHole.png`、`bhole.png`、`bholeDisc.png`。
  - `EntityVortex` 使用 swirl 色 `0x3898b3`；本批转为 shader 天蓝色吸积盘。
- `src/main/java/com/hbm/config/WeaponConfig.java`
  - `dropSing` 默认 `true`。
  - 配置项名：`10.01_dropBHole`，说明为是否允许奇点和黑洞掉落时生成。
- 资源：
  - `assets/hbm/textures/items/singularity.png`
  - 语言：
    - `item.singularity.name=Singularity`
    - `item.singularity.name=奇点`

## 现代迁移记录

- 物品：
  - 新增 `ModItems.SINGULARITY`，注册名 `hbm:singularity`。
  - 使用 `SingularityItem`，最大堆叠 1，放入现代 `HBM Control` 创造栏。
  - 复制旧版贴图到 `assets/hbm/textures/item/singularity.png`。
  - 固化物品模型 `assets/hbm/models/item/singularity.json`，避免依赖被 `.gitignore` 忽略的 `src/generated`。
- 掉落触发：
  - 复用现有掉落物 hazard 轮询入口 `CommonForgeEvents#applyDroppedItemHazards(...)`。
  - 普通奇点在服务端 `ItemEntity#onGround()` 且 `WeaponConfig.DROP_SINGULARITY` 为真时生成 `VortexEntity(level, 1.5F)`。
  - 掉落物随后 `discard()`，对齐旧版 `setDead()`。
- 配置：
  - 新增现代 `WeaponConfig.DROP_SINGULARITY`，默认 `true`。
  - 路径在 `hbm-common.toml` 的 `drops.dropBlackHoleSingularities`。
- 实体：
  - 新增 `BlackHoleEntity`，注册名 `entity_black_hole`。
  - 新增 `VortexEntity`，注册名 `entity_vortex`。
  - `VortexEntity` 保留旧版 `shrinkRate = 0.0025F`，shader 和玩法 size 同步缩小。
  - 客户端 renderer 使用 `NoopRenderer`，实际视觉由屏幕空间 shader 库绘制。
- 伤害：
  - 新增 `hbm:blackhole` damage type。
  - `ModDamageSources.blackhole(level, source)` 供黑洞近距离吞噬使用。
- Shader 黑洞接入：
  - `HbmBlackHoleEffects` 新增 `updateTrackedBlackHole(key, x, y, z, spec, age)`。
  - `ClientForgeEvents#updateBlackHoleShaders(partialTick)` 在 blackhole render stage 扫描 `VortexEntity` / `BlackHoleEntity`，用渲染插值位置和当前 `size` 更新同一个 shader 黑洞。
  - 普通 `VortexEntity` 调用 `BlackHoleSpec.of(size, lifetime)`；该 API 的第一个参数是旧版 `RenderBlackHole` 的统一 `size` / 事件视界半径。
  - shader 库内部派生外层屏幕空间扭曲范围为 `size * 6.0F`，对齐旧版 swirl 外圈淡出半径。
  - 当前 `black_hole.fsh` 保持现代源 shader 的内部坐标系：`localRo = (worldPos - entityPos) / scale` 后再乘 `effectScale = 8.0`；事件视界、吸积盘和光子环的视觉比例由源 shader 决定。
  - 空间扭曲范围随旧版 `size` 同步缩小；不能为了修视觉单独把黑球缩成 `size * 0.32` 或另行改写 shader 内部 raymarch 单位。
  - 普通奇点吸积盘颜色为天蓝色，主色/ramp：
    - `withDiskColor(0.45F, 0.85F, 1.0F)`
    - `withDiskRamp(0.85F, 1.35F, 1.6F, 0.05F, 0.45F, 1.0F)`
  - 普通奇点当前 shader 调校参数：
    - `withRenderQuality(1.6F, 0.45F)`
    - `withLensBoundarySoftness(0.6F)`
    - `withDiskDetail(1.0F, 0.35F)`
  - 黑洞后处理仍在 `AFTER_LEVEL` 合成最终画面；`modelViewMatrix` 使用当前 camera yaw/pitch 矩阵，不跨渲染阶段复用捕获矩阵。
  - 客户端 shader 位置在 blackhole render stage 内使用 `entity.getPosition(partialTick)` 更新，避免 tick 级位置阶跃造成屏幕空间漂移。
  - 用户实测确认漂移时实体/采样位置并未变化，问题来自屏幕空间黑洞图形随走路/落地 bob 漂移。
  - `black_hole.fsh` 已改为用 `inverse(projectionMatrix * modelViewMatrix)` 的 near/far 点重建射线，ray origin 使用 near plane 点，让 bob 对黑洞几何和背景采样一致生效。
  - 黑洞透镜恢复源 shader 结构：球外也输出 `traceLensedRay(...)` 的透镜采样，外层球只决定是否额外渲染事件视界/吸积盘，不再作为可见球壳边界或透镜硬裁剪。
  - 空间扭曲、事件视界、吸积盘和 haze 全部沿同一个 shader `scale` 缩放；`BlackHoleSpec.of(size, lifetime)` 不能拆成“单独黑球半径”和“单独盘半径”两套尺寸。

## 暂未迁移

- 普通奇点旧版配方尚未迁入；本批仅迁物品、掉落触发、实体机制和视觉。
- `singularity_counter_resonant`、`singularity_super_heated`、`black_hole`、`singularity_spark` 及其不同尺寸/颜色/行为另列后续批次。
- `flame_pony` 等特殊反制物品若现代项目尚未注册，`BlackHoleEntity` 会自动跳过对应判断；`pellet_antimatter` 迁入后可自动生效。

## 验证

- `.\gradlew.bat compileJava processResources --no-daemon` 通过。
- `.\gradlew.bat runData --no-daemon`：
  - `Languages: en_us`、`Languages: zh_cn`、`Item Models: hbm` 已完成，生成了 `singularity` 语言和 item model。
  - 后续卡在既有 `HbmBlockStateProvider#sellafieldSlakedWithItem(...)` 的 `level=0` 重复配置，和本批奇点无关。
- 待实机检查：
  - 扔出普通奇点后，落地应立即消失并生成天蓝色 shader 黑洞。
  - 黑洞约 600 tick 逐渐缩小直至消失，吸积盘和事件视界应同步缩小。
  - 近距离实体被吸入并受 `blackhole` 伤害，方块/液体被逐步吞噬并生成瓦砾。
