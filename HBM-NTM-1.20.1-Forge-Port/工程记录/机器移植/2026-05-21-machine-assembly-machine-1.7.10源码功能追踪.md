# 组装机 machine_assembly_machine 1.7.10 源码功能追踪

## 范围

- 迁移旧 `machine_assembly_machine` 的第一切片：注册、旧多方块放置偏移、dummy 占位清理、OBJ 分件渲染入口。
- 本切片不迁移完整配方运行、GUI、能量/流体端口、升级、声音和 recipe selector，只记录契约并保留后续接入点。

## 1.7.10 源文件

- 方块：`src/main/java/com/hbm/blocks/machine/MachineAssemblyMachine.java`
- 方块基类：`src/main/java/com/hbm/blocks/BlockDummyable.java`
- 方块实体：`src/main/java/com/hbm/tileentity/machine/TileEntityMachineAssemblyMachine.java`
- 容器：`src/main/java/com/hbm/inventory/container/ContainerMachineAssemblyMachine.java`
- GUI：`src/main/java/com/hbm/inventory/gui/GUIMachineAssemblyMachine.java`
- 配方模块：`src/main/java/com/hbm/module/machine/ModuleMachineAssembler.java`
- 配方注册：`src/main/java/com/hbm/inventory/recipes/AssemblyMachineRecipes.java`
- 渲染器：`src/main/java/com/hbm/render/tileentity/RenderAssemblyMachine.java`
- 资源入口：`src/main/java/com/hbm/main/ResourceManager.java`
- 注册：`src/main/java/com/hbm/blocks/ModBlocks.java`、`src/main/java/com/hbm/tileentity/TileMappings.java`、`src/main/java/com/hbm/main/ClientProxy.java`
- 声音：`assets/hbm/sounds.json` 中 `block.assemblerOperate`、`block.assemblerStrike`、`block.assemblerStart`、`block.assemblerStop`、`block.assemblerCut`
- 资源：
  - `assets/hbm/models/machines/assembly_machine.obj`
  - `assets/hbm/textures/models/machines/assembly_machine.png`
  - `assets/hbm/textures/gui/processing/gui_assembler.png`
  - `assets/hbm/textures/gui/nei/gui_nei_assembler.png`

## 旧版状态与数据契约

- 方块 ID：`machine_assembly_machine`
- 方块实体 ID：`tileentity_assemblymachine`
- 方块注册：`MachineAssemblyMachine(Material.iron)`，硬度 `5.0F`，抗爆 `30.0F`，创造栏 `machineTab`，方块贴图 `block_steel`。
- 旧 `BlockDummyable` 主方块 metadata：`offset = 10`，metadata `12..15` 表示 core 方向。
- 组装机尺寸：`getDimensions() = {2, 0, 1, 1, 1, 1}`，这是 XR 顺序 `{U,D,N,S,W,E}`，面向 south 的结构表。
- 放置偏移：`getOffset() = 1`。旧放置点会先移除，然后沿机器方向偏移一格放置 core。
- 额外 proxy：`fillSpace` 后把 core 后方中心周围 `3x3` 的非中心位置调用 `makeExtra`，用于 power/fluid/inventory proxy 能力。
- inventory：17 槽。
  - `0` 电池。
  - `1` blueprint。
  - `2..3` 升级。
  - `4..15` 12 个输入。
  - `16` 输出。
- 能量：`power`、`maxPower`，默认 `100_000`；有配方时 `maxPower = recipe.power * 100`，再取 `max(power, maxPower, 100_000)`。
- 流体：`inputTank` 与 `outputTank`，容量各 `4_000`。
- 配方模块：`ModuleMachineAssembler(0, this, slots).itemInput(4).itemOutput(16).fluidInput(inputTank).fluidOutput(outputTank)`。
- NBT：`power`、`maxPower`、`inputTank` key `i`、`outputTank` key `o`、`assemblerModule` 自有字段。
- 网络同步：旧 `networkPackNT(100)` 同步 tank、power、maxPower、didProcess、assemblerModule。

## 旧版交互与运行行为

- 右键：`standardOpenBehavior(..., gui id 0)`，dummy/proxy 位置会先 `findCore` 再打开 core GUI。
- 服务端 tick：
  - 从电池槽充电：`Library.chargeTEFromItems(slots, 0, power, maxPower)`。
  - 检查升级：speed、power、overdrive。
  - 12 个外圈连接点订阅能量/流体网络：
    - x ±2、z -1..1。
    - z ±2、x -1..1。
  - `assemblerModule.update(speed, pow, true, slots[1])` 推进配方。
  - 若产出并且 slot 0 是 `meteorite_sword_alloyed`，替换为 `meteorite_sword_machined`。
- 客户端 tick：
  - `didProcess` 时播放电机循环音、更新两条机械臂和 ring 旋转。
  - ring 随机目标偏转，速度 `10..15`，到达后等待 `20..40` tick。
  - 两条机械臂使用固定候选位置、撞针伸缩，撞针到 `-0.75` 时播放 `assemblerStrike`。

## 旧版渲染契约

- `ResourceManager.assembly_machine = HFRWavefrontObject("models/machines/assembly_machine.obj").asVBO()`
- `ResourceManager.assembly_machine_tex = textures/models/machines/assembly_machine.png`
- TESR 初始变换：
  - 平移到 `x + 0.5, y, z + 0.5`
  - Y 轴旋转 `90`
  - 按 core metadata 方向再旋转：2 -> 0，4 -> 90，3 -> 180，5 -> 270。
- OBJ part：
  - 静态：`Base`
  - 框架：`Frame`，只有 `assembler.frame` 为 true 时渲染。
  - ring：`Ring`
  - arm1：`ArmLower1`、`ArmUpper1`、`Head1`、`Spike1`
  - arm2：`ArmLower2`、`ArmUpper2`、`Head2`、`Spike2`
  - OBJ 内还存在 `Ring2`，本轮现代模型入口并入 ring 分件。
- 配方展示：如果当前 recipe 存在且玩家距离小于 35 格，在机器中心上方渲染 recipe icon item。
- 物品渲染：旧 `IItemRendererProvider` 物品栏平移 `0,-2.75,0`、缩放 `4.5`；普通显示旋转 `90`、缩放 `0.75` 并渲染整模。

## 本轮现代实现

- 新增：
  - `src/main/java/com/hbm/ntm/block/AssemblyMachineBlock.java`
  - `src/main/java/com/hbm/ntm/blockentity/AssemblyMachineBlockEntity.java`
  - `src/main/java/com/hbm/ntm/client/renderer/AssemblyMachineRenderer.java`
- 注册：
  - `ModBlocks.MACHINE_ASSEMBLY_MACHINE`
  - `ModBlockEntities.ASSEMBLY_MACHINE`
  - `ClientModEvents` 注册 `AssemblyMachineRenderer`
- 多方块：
  - 现代 block 实现 `MultiblockCoreBlock`。
  - 保留旧 `getOffset() = 1` 的放置语义：`BlockDummyable` 使用 `o = -getOffset()`，因此点击点沿 facing 反方向一格成为 core。
  - 旧 XR 尺寸 `{U,D,N,S,W,E} = {2,0,1,1,1,1}` 现在通过 `MultiblockExtents.ofLegacyXr(..., SOUTH)` 生成现代 `{+X,-X,+Y,-Y,+Z,-Z} = {1,1,2,0,1,1}`，用于 dummy 占位和拆除清理。
- 渲染：
  - 复制旧 OBJ 与贴图到现代资源树。
  - 为 `Base/Frame/Ring/ArmLower*/ArmUpper*/Head*/Spike*` 拆 Forge OBJ visibility JSON。
  - `ObjMachineModels.ASSEMBLY_MACHINE` 用旧 part 名映射现代分件。
  - BER 按旧 renderer 的平移/旋转/机械臂 pivot 迁移基础动画；模型先平移到 block center，再叠加旧 TESR 的 `90 + facing` Y 旋转。
  - 放置实测发现 Forge baked split OBJ 在 BER 中未可靠显示；当前改走渲染库新增的 `LegacyWavefrontModel`，直接复刻旧 `HFRWavefrontObject.renderPart(...)` group 渲染路径。
  - 组装机 block/item model 直接使用旧 OBJ，避免 `ENTITYBLOCK_ANIMATED` 方块在创造栏显示为占位钢块。
  - dummy 方块保持碰撞/交互/破坏联动，但 `RenderShape.INVISIBLE` 且不作为红石导体，不参与多方块视觉显示。
  - datagen 只生成组装机 blockstate，不覆盖手写 item model display。
- 2026-05-22 修正：
  - 组装机 core relocate 改为 `MultiblockHelper.legacyCoreFromPlacement(pos, facing, 1)`，修正现代端早期把 core 放到 facing 正方向导致近身/前一格放置异常的问题。
  - 放置检查改用多方块库的 `checkSpace(..., temporaryPos)`，临时点击点可被忽略，但最终 core 位置必须可替换。
  - core 破坏粒子走 `hbm:block_steel`，与旧注册贴图契约一致，避免 OBJ/大 shape 默认破坏粒子异常。
  - 组装机物品改用 `MultiblockBlockItem`，直接在最终 core 位置放置并填充 dummy；不再经过原版 `BlockItem` 对临时点击格的大形状碰撞检查，避免玩家面前第一/第二格放置失败和放置瞬间从临时格闪到 core 的错位。
  - 按旧 `MachineAssemblyMachine.fillSpace` 的 `makeExtra` 规则，把 core 同层 `3x3` 外圈 8 个 dummy 标为 proxy；proxy dummy 的 capability 会转发到 core。
  - core `AssemblyMachineBlockEntity` 暴露 `ForgeCapabilities.ITEM_HANDLER`，用于先恢复旧 proxy inventory 通道。
  - core 增加旧 `power/maxPower` scaffold：
    - 内部 `HbmEnergyStorage(100_000, 100_000, 0)`，当前只作为耗能机器对外接收能量。
    - NBT 同时写现代 `Energy` 和旧兼容 `power/maxPower`。
    - proxy dummy 可通过通用 capability 转发访问能量输入。
  - core 增加旧 `inputTank/outputTank` scaffold：
    - `inputTank` 与 `outputTank` 容量各 `4_000`，NBT key 保留旧版 `i` / `o`。
    - Forge fluid capability 只允许向 input tank 填充、从 output tank 抽取，避免普通 Forge handler 把外部输入误写进输出 tank。
    - 配方模块尚未迁移，因此 tank 目前只保存/暴露数据，不进行真实配方消耗与产出。
  - 组装机 block 改为继承多方块库新增的 `LegacyXrMultiblockBlock`：
    - 机器类仅保留旧 `getDimensions()`、`getOffset()`、`makeExtra` 等差异规则。
    - 放置、core relocate、dummy 填充、proxy 标记和拆除清理由库层统一处理。

## 暂缓内容

- 完整 `ModuleMachineAssembler`、`GenericRecipe`、`AssemblyMachineRecipes` 未接入。
- GUI/Menu/recipe selector 未接入；右键目前不打开界面。
- Energy MK2/Fluid MK2 端口能力未接入。旧 `makeExtra` proxy 与 12 个连接点需要多方块 dummy 能力分发层支持后再迁。
- 物品栏 3D item renderer 未接入，当前 item model 是保守 cube 占位。
- recipe icon in-world 渲染未接入。
- 电机循环音、strike/start/stop/cut 声音未接入。
- 升级、蓝图池、流体 tank、meteorite sword 特判未接入。

## 验证清单

- `.\gradlew.bat compileJava processResources --no-daemon`
- 游戏内：
  - 四向放置时 core 偏移和 dummy 体积与旧 XR `{2,0,1,1,1,1}` 一致。
  - 破坏 core 清理 dummy。
  - 破坏 dummy 联动破坏 core。
  - 存档重载后 dummy 仍能定位 core。
  - BER 能显示 Base/Ring/双臂，且无缺失模型紫黑。
  - 上方第 3 格放置方块时 Frame 显示。
