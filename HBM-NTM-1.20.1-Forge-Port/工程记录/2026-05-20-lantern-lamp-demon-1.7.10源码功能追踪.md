# lantern / lamp_demon 1.7.10 源码功能追踪

## 范围

- 本记录覆盖 1.7.10 方块 `ModBlocks.lantern` 与 `ModBlocks.lamp_demon`。
- 本轮包含注册属性、方块实体、放置/结构、渲染语义、辐射危险、配方和语言名的核对。
- `lantern_behemoth` 是另一种废土世界生成多方块灯，不在本轮范围。

## 1.7.10 来源文件

- `src/main/java/com/hbm/blocks/ModBlocks.java`
  - `lamp_demon = new DemonLamp()`，金属音效，`blockTab`，亮度 `1F`，硬度 `3.0F`，贴图名 `hbm:lamp_demon`。
  - `lantern = new BlockLantern()`，金属音效，`blockTab`，亮度 `1F`，硬度 `3.0F`，贴图名 `hbm:block_steel`。
- `src/main/java/com/hbm/blocks/machine/DemonLamp.java`
  - `BlockContainer`，材质铁，创建 `TileEntityDemonLamp`。
  - `getRenderType() == -1`，非普通块，非不透明整块。
  - `onBlockPlaced(...)` 返回点击面 `side`，metadata 直接保存六面贴附方向。
- `src/main/java/com/hbm/tileentity/machine/TileEntityDemonLamp.java`
  - 服务端每 tick 调用 `radiate(world, x, y, z)`。
  - 基础辐射 `100000F`，范围 `25D`。
  - 对范围内 `EntityLivingBase` 从灯中心到实体眼睛射线采样，每格累加方块爆炸抗性，最低阻挡值为 `1`。
  - 实际辐射为 `100000 / resistance / distance^2`，以 `ContaminationType.CREATIVE` 施加辐射污染。
  - 距离小于 `2` 时额外造成 `DamageSource.inFire` 100 点伤害。
  - 渲染包围盒无限，最大渲染距离平方 `65536`。
- `src/main/java/com/hbm/render/tileentity/RenderDemonLamp.java`
  - 模型 `assets/hbm/models/blocks/demon_lamp.obj`。
  - 贴图 `assets/hbm/textures/models/machines/demon_lamp.png`。
  - 按 metadata 0..5 做 1.7.10 六面旋转。
  - 渲染实体灯体后，叠加无贴图、加色混合、青色透明光束环。
- `src/main/java/com/hbm/blocks/generic/BlockLantern.java`
  - 继承 `BlockDummyable`。
  - 仅 metadata `>= 12` 的主方块创建 `TileEntityLantern`。
  - `getDimensions()` 返回 `{4, 0, 0, 0, 0, 0}`，表示从主方块向上占 4 格 dummy，总高度 5 格。
  - `getOffset()` 返回 `0`。
- `src/main/java/com/hbm/tileentity/deco/TileEntityLantern.java`
  - 服务端每 20 tick 在 `(x + 0.5, y + 5.5, z + 0.5)` 周围 `7.5` 格范围查找 `EntityGlyphid`。
  - 对 Glyphid 添加 `Potion.blindness`，持续 `100` tick，等级 `0`。
  - 渲染包围盒覆盖 `y` 到 `y + 6`，最大渲染距离平方 `65536`。
- `src/main/java/com/hbm/render/tileentity/RenderLantern.java`
  - 模型来自 `ResourceManager.lantern`，路径 `models/trinkets/lantern.obj`。
  - 贴图 `textures/models/trinkets/lantern.png`。
  - TESR 平移到 `x + 0.5, y, z + 0.5`。
  - 先渲染 `Lantern` 分件，再关闭贴图、全亮度、暖色脉动渲染 `Light` 分件。
- `src/main/java/com/hbm/entity/mob/glyphid/EntityGlyphid.java`
  - Glyphid 失明后，大体型个体会射线查找并破坏 `ModBlocks.lantern`。
- `src/main/java/com/hbm/hazard/HazardRegistry.java`
  - `HazardSystem.register(lamp_demon, makeData(RADIATION, 100_000F));`
- `src/main/java/com/hbm/main/CraftingManager.java`
  - `lantern` 配方：`"PGP", " S ", " S "`，`P = KEY_ANYPANE`，`G = glowstone_dust`，`S = ModBlocks.steel_beam`。
- `src/main/java/com/hbm/crafting/WeaponRecipes.java`
  - `lamp_demon` 配方：`" D ", "S S"`，`D = ModItems.demon_core_closed`，`S = STEEL.ingot()`。
- 语言：
  - `en_US.lang`: `tile.lamp_demon.name=Demon Core Lamp`
  - `zh_CN.lang`: `tile.lamp_demon.name=恶魔核心灯`

## 迁移计划

- 立即补齐：
  - `lamp_demon` 改为带 BlockEntity 的六面贴附方块，按 1.7.10 点击面保存方向。
  - `lamp_demon` 服务端 tick 复刻 25 格范围、遮挡衰减、近身火焰伤害。
  - `lamp_demon` 物品危险注册补 `RADIATION = 100000F`。
  - `lantern` 改为 5 格高结构，主方块在底部，顶部 4 格为 dummy；dummy 不掉落，破坏任意段会清除整盏灯。
  - 修正 `lamp_demon` 英文/中文名。
- 暂缓：
  - `lantern` 对 `EntityGlyphid` 的失明和被 Glyphid 破坏联动，因为当前 1.20.1 目标工程尚未迁移 Glyphid 实体。
  - `lantern` 暖色无贴图全亮 `Light` 分件和 `lamp_demon` 青色透明光束属于客户端增强渲染，若后续做完整 BER 再补。
  - 两个配方依赖 `steel_beam` 和 `demon_core_closed`，当前目标工程尚未注册这些材料/方块，等对应来源物品迁移后补数据配方。

## 验证清单

- `./gradlew.bat compileJava processResources --no-daemon`
- 游戏内放置：
  - `lantern` 放置时需要上方 4 格空间；任意段破坏后整体移除且只掉 1 个主物品。
  - `lamp_demon` 六个面放置时模型方向应对应 1.7.10 metadata 0..5。
  - 生存/创造下 `lamp_demon` 近身应造成高额火焰伤害，远距离按遮挡和距离施加辐射。
