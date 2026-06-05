# 5714 新版 1.7.10 新增机器方块源码变化追踪

## 范围

- 本记录只追踪 `源码包/old-code/Hbm-s-Nuclear-Tech-GIT-master` 到新版 `源码包/Hbm-s-Nuclear-Tech-GIT-master/Hbm-s-Nuclear-Tech-GIT-master` 的新增/变化机器方块事实。
- 包含：`machine_blast_furnace`、`machine_thresher`、`vending_machine`、`radio_autocal`。
- 不表示这些机器已经迁入现代 port；后续实现时仍需为具体迁移切片补完整 trace。

## 新增 1.7.10 源文件

- 高炉：
  - `src/main/java/com/hbm/blocks/machine/MachineBlastFurnace.java`
  - `src/main/java/com/hbm/tileentity/machine/TileEntityMachineBlastFurnace.java`
  - `src/main/java/com/hbm/inventory/container/ContainerBlastFurnace.java`
  - `src/main/java/com/hbm/inventory/gui/GUIBlastFurnace.java`
  - `src/main/java/com/hbm/inventory/recipes/BlastFurnaceRecipe.java`
  - `src/main/java/com/hbm/inventory/recipes/BlastFurnaceRecipesNT.java`
  - `src/main/java/com/hbm/handler/nei/BlastFurnaceHandler.java`
  - `src/main/java/com/hbm/render/tileentity/RenderBlastFurnace.java`
- 脱粒机：
  - `src/main/java/com/hbm/blocks/machine/MachineThresher.java`
  - `src/main/java/com/hbm/tileentity/machine/TileEntityMachineThresher.java`
  - `src/main/java/com/hbm/render/tileentity/RenderThresher.java`
- 售货机：
  - `src/main/java/com/hbm/blocks/machine/BlockVendingMachine.java`
  - `src/main/java/com/hbm/itempool/ItemPoolsVendingMachine.java`
  - `src/main/java/com/hbm/render/tileentity/RenderVendingMachine.java`
- AUTOCAL：
  - `src/main/java/com/hbm/blocks/network/RadioAUTOCAL.java`
  - `src/main/java/com/hbm/tileentity/network/TileEntityRadioAUTOCAL.java`
  - `src/main/java/com/hbm/inventory/gui/GUIScreenRadioAUTOCAL.java`
  - `src/main/java/com/hbm/render/tileentity/RenderAUTOCAL.java`
  - `src/main/java/com/hbm/module/IParse.java`
  - `src/main/java/com/hbm/module/ParseMSES1.java`

## 注册与资源变化

- `ModBlocks` 新增 `machine_blast_furnace`、`machine_thresher`、`vending_machine`、`radio_autocal`、`logic_block_invis`，并把旧 `machine_difurnace_*`、`machine_rtg_furnace_*`、`machine_bat9000` 标注为 deprecated。
- `TileMappings` 新增 `tilentity_blast_furnace`、`tileentity_thresher`、`tileentity_vending_machine`、`tileentity_rtty_autocal`。
- `ClientProxy` 绑定新增 TESR：`RenderBlastFurnace`、`RenderThresher`、`RenderVendingMachine`、`RenderAUTOCAL`。
- `ResourceManager` 新增 OBJ/贴图入口：
  - `models/machines/blast_furnace.obj` / `textures/models/machines/blast_furnace.png`
  - `models/machines/thresher.obj` / `textures/models/machines/thresher.png`
  - `models/machines/vending_machine.obj` / `textures/models/machines/vending_machine.png`
  - `models/machines/autocal.obj` / `textures/models/machines/autocal.png`
- 新增 GUI/流体资源：`gui_blast_furnace.png`、`gui_rtty_autocal.png`、`airblast.png`、`flue.png`。

## 行为摘要

- `MachineBlastFurnace`：
  - 继承 `BlockDummyable`，`getDimensions()={6,0,1,1,1,1}`，`getOffset()=1`。
  - core metadata `>=12` 创建 `TileEntityMachineBlastFurnace`，proxy metadata `>=6` 创建 inventory/fluid proxy。
  - `fillSpace` 在 core 四侧、前方高位 y+3/y+5、顶部 y+6 添加 extra/proxy。
  - TE 有 5 槽，`slot0` fuel，`slot1/2` 输入，`slot3/4` 输出；`AIRBLAST` tank 4000mB，`FLUE` tank 1000mB。
  - 燃料上限 `MAX_FUEL=FUEL_COAL*16`，每次操作消耗 `FUEL_RATE=200*4`，完成时产出 `FLUE_GAS=100`，FLUE 溢出会释放污染。
- `MachineThresher`：
  - 普通 `BlockContainer`，可用 fluid identifier 切换燃料流体，接受 `TileEntityMachineAutosaw.acceptedFuels`。
  - screwdriver 切换 `isSuspended`，look overlay 显示 tank 与 suspended 状态。
  - TE 每 20 tick 消耗 1mB 燃料，驱动摆臂切割作物/甘蔗/仙人掌/NTM tall plants，并对生物造成 `ModDamageSource.turbofan` 伤害；杀死怪物可掉 `nitra_small`。
- `BlockVendingMachine`：
  - 双高 `BlockDummyable`，`getSubCount()=2`，item damage 奇数代表 snacks 变体。
  - 右键消耗 `coin_token`，按变体从 `POOL_SODA` 或 `POOL_SNACKS` 抽取物品，并向机器朝向前方抛出。
- `RadioAUTOCAL`：
  - 双高 `BlockDummyable`，右键打开 `GUIScreenRadioAUTOCAL`。
  - TE 实现 `IControlReceiver` 与 `IGUIProvider`，保存 `isOn`、`ignoreError`、`autoReboot`、脚本、当前行、clock speed、buffer、variables 与 history。
  - GUI 通过 `NBTControlPacket` 上传 `script.txt`，并可生成本地 `documentation.md`；脚本语言为 MS-ES1，clock speed 上限来自 `ServerConfig.AUTOCAL_MAX_CLOCK`。

## 迁移提示

- 高炉不是旧 `BlastFurnaceRecipes` 的简单 UI 替换；新版已把旧 recipe surface 拆成 `BlastFurnaceRecipesNT` 与 deprecated legacy 文件。
- 脱粒机与 autosaw 共享燃料流体/音频/切割行为，但作物处理合同不同，不能只复制 autosaw。
- 售货机依赖 item pool 库和新增 `coin_token`，迁移前需确保 loot/item pool 抽取与对应食物/饮料物品存在。
- AUTOCAL 依赖 RoR、NBT control packet、文件上传 GUI 和脚本解析库，应作为 RoR 子系统的新机器切片处理。

## 验证清单

- 后续迁移时分别验证：四向放置、dummy/core 解析、OBJ 朝向、GUI 打开、安全控制包、NBT 保存、资源加载、JEI/recipe selector 显示。
- 本记录只做源码差异追踪，未运行现代构建。
