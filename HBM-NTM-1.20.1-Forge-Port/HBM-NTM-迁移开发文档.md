# HBM NTM 迁移开发文档

## 文档定位

- 本文记录当前 1.7.10 -> Forge 1.20.1 迁移工程的稳定事实。
- 1.7.10 原版源码是主事实来源；1.20.1 残缺非官方移植版仅作参考，不完全模仿。
- 不允许猜测机制或机制实现方式；所有机制和实现路径必须有来源：要么来自 1.7.10 源码/资源，要么来自额外设定，要么来自明确的额外需求。没有来源时先记录缺口并询问，不直接实现。
- 如果当前 1.20.1 port 不存在相关功能、库或系统，不用近似实现替代；应迁移 1.7.10 旧版实现，或按同等功能契约重写。若当前 port 已有可承接的相关功能，则直接按 1.7.10 源码正常迁移接入。
- 当前阶段优先迁移基础物品、机器、核弹/爆炸物模型与资源。矿石生成和世界生成暂缓到后期统一处理。
- 工作区可能因多线程/并行移植而存在大量未提交或无关改动；每轮只处理当前任务相关文件，不回滚、不清理、不重排无关文件，遇到相关冲突时先兼容现有改动，无法判断再询问。

## 项目基础信息

- 项目名称: HBM NTM Forge 1.20.1 Port
- `modid`: `hbm`
- 来源版本: Minecraft Forge 1.7.10
- 目标版本: Minecraft Forge 1.20.1
- Java: 17
- ForgeGradle: 6.0.25
- 映射: Parchment `2023.09.03-1.20.1`
- 代理: `127.0.0.1:7890`

## 源码定位

- 源码包根目录: `E:\游戏\我的世界\源码包`
- 1.7.10 原版源码: `E:\游戏\我的世界\源码包\Hbm-s-Nuclear-Tech-GIT-master`
- 1.7.10 索引: `E:\游戏\我的世界\开发\HBM-NTM-1.20.1-Forge-Port\1.7.10-源码资源迁移索引.md`
- 1.20.1 参考移植版: `E:\游戏\我的世界\源码包\HBM-NTM-high-edition-test`
- 1.20.1 参考索引: `E:\游戏\我的世界\开发\HBM-NTM-1.20.1-Forge-Port\1.20.1-参考移植版源码资源索引.md`
- 当前移植工程: `E:\游戏\我的世界\开发\HBM-NTM-1.20.1-Forge-Port`
- 源码查找 Python 脚本位于 skill 目录 `C:\Users\Mr.Wu\.codex\skills\hbm-ntm-common\scripts`；不要在当前 port 工程目录里查找、创建或复制这些脚本。

## 当前工程结构

- 主类: `src/main/java/com/hbm/ntm/HbmNtm.java`
- 配置: `src/main/java/com/hbm/ntm/config/HbmCommonConfig.java`
- 方块基类: `src/main/java/com/hbm/ntm/block/HorizontalMachineBlock.java`
- 方块实体机器块: `src/main/java/com/hbm/ntm/block/MachineBlockEntityBlock.java`
- 基础机器方块实体: `src/main/java/com/hbm/ntm/blockentity/BasicMachineBlockEntity.java`
- 基础机器菜单: `src/main/java/com/hbm/ntm/menu/BasicMachineMenu.java`
- 基础机器界面: `src/main/java/com/hbm/ntm/client/screen/BasicMachineScreen.java`
- 基础机器渲染: `src/main/java/com/hbm/ntm/client/renderer/BasicMachineRenderer.java`
- 压机配方: `src/main/java/com/hbm/ntm/recipe/PressRecipe.java`
- 物品注册: `src/main/java/com/hbm/ntm/registry/ModItems.java`
- 方块注册: `src/main/java/com/hbm/ntm/registry/ModBlocks.java`
- 方块实体注册: `src/main/java/com/hbm/ntm/registry/ModBlockEntities.java`
- 菜单注册: `src/main/java/com/hbm/ntm/registry/ModMenuTypes.java`
- 配方注册: `src/main/java/com/hbm/ntm/recipe/ModRecipes.java`
- 创造栏: `src/main/java/com/hbm/ntm/registry/ModCreativeTabs.java`
- DataGen: `src/main/java/com/hbm/ntm/datagen`
- 客户端资源: `src/main/resources/assets/hbm`
- 服务端数据: `src/main/resources/data/hbm`

## 已稳定能力

- 1.20.1 Forge 工程骨架已建立，可进入游戏测试。
- `run-client.bat` 和 `run-client.ps1` 已放在工程根目录，并设置代理启动客户端。
- `build.gradle` 已处理 `processResources` 重复资源策略。
- 物品、机器、核弹各自有创造栏入口。
- 基础物品、基础机器和两批核弹/基础爆炸物模型已注册。
- `HorizontalMachineBlock` 已支持水平朝向，摆放时正面朝向玩家。
- 非完整 OBJ 模型使用 `noOcclusion`，避免像锻压机那样隐藏地面或邻接方块表面。
- `machine_press` 已作为第一个基础机器接入 `BasicMachineBlockEntity`/`BasicMachineMenu`/`BasicMachineScreen`/`BasicMachineRenderer`，具备 NBT、13 槽物品 handler、旧版 GUI 贴图、左上角顺时针表盘指针、更新包入口、server tick、右键打开 GUI、速度/燃料/压头同步、第一版 `hbm:press` 配方处理循环、输入物品动画和破坏掉落内部物品。真实 `press_head.obj` 压头动画仍待后续迁移。
- `stamp_iron_plate` 已迁移为第一枚压机模具，用于测试板材压制。

## 已迁移基础物品

- Ingots: `ingot_uranium`, `ingot_u233`, `ingot_u235`, `ingot_u238`, `ingot_plutonium`, `ingot_pu238`, `ingot_pu239`, `ingot_pu240`, `ingot_pu241`, `ingot_neptunium`, `ingot_polonium`, `ingot_th232`, `ingot_titanium`, `ingot_tungsten`, `ingot_copper`, `ingot_lead`, `ingot_steel`, `ingot_cobalt`, `ingot_aluminium`, `ingot_beryllium`, `ingot_schrabidium`, `ingot_advanced_alloy`
- Plates: `plate_steel`, `plate_iron`, `plate_copper`, `plate_lead`, `plate_titanium`, `plate_aluminium`
- Powders: `powder_uranium`, `powder_plutonium`, `powder_thorium`, `powder_titanium`, `powder_tungsten`, `powder_copper`, `powder_iron`, `powder_steel`, `powder_lead`
- Parts: `coil_copper`, `coil_tungsten`, `coil_gold`, `motor`

## 已迁移基础机器

- `machine_press`: 火力锻压机，OBJ 组合模型，已修正旧 TESR 中心原点偏移；已绑定基础 BlockEntity/Menu/Screen/Recipe/Renderer 链路。
- `machine_difurnace_off`: 高炉，占位多面材质模型。
- `machine_electric_furnace_off`: 电炉，多面材质模型。
- `machine_boiler_off`: 锅炉，多面材质模型。
- `machine_shredder`: 粉碎机，多面材质模型。

## 已迁移核弹模型

- `nuke_gadget`
- `nuke_boy`
- `nuke_man`
- `nuke_tsar`
- `nuke_mike`
- `nuke_prototype`
- `nuke_fleija`
- `nuke_solinium`
- `nuke_n2`
- `nuke_fstbmb`
- `bomb_multi`

这些目前是模型方块，占位逻辑为主，尚未迁移真实爆炸行为、辐射、蘑菇云、GUI、TileEntity/BlockEntity。

## 资源与模型要点

- Forge OBJ 模型 JSON 使用 `loader: forge:obj` 或 `forge:composite`。
- OBJ 资源迁移时需要检查 `.mtl` 是否为标准多行格式，曾因一行 MTL 导致核弹紫黑缺贴图。
- 1.7.10 TESR 常以方块中心为原点，迁移到 1.20.1 block model 时常需对 OBJ 顶点做 `x/z +0.5` 或特定偏移修正。
- 第二批核弹 OBJ 已对顶点 `x/z` 做 `+0.5` 平移，后续不要重复应用同一偏移。
- 非完整方块模型必须避免完整遮挡，否则会使地面或邻接面透明/消失。

## 构建与验证

推荐验证命令:

```powershell
Set-Location -LiteralPath 'E:\游戏\我的世界\开发\HBM-NTM-1.20.1-Forge-Port'
$env:JAVA_OPTS='-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Dnet.minecraftforge.gradle.check.certs=false'
.\gradlew.bat compileJava processResources --no-daemon
```

游戏内验证:

- 运行 `run-client.bat`
- 进入创造模式，检查 `HBM Parts`, `HBM Machines`, `HBM Nukes`
- 检查机器/核弹摆放方向是否面向玩家
- 检查 OBJ 模型是否紫黑缺贴图
- 检查非完整模型是否隐藏地面或邻接方块表面

## 暂缓范围

- 矿石生成与世界生成。
- 核弹真实爆炸、辐射、污染、蘑菇云。
- 完整机器处理逻辑、完整压机配方、预热器、音效和真实 OBJ 压头动画。
- JEI/Jade 兼容。
