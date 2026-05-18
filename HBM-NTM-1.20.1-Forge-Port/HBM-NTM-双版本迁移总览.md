# HBM NTM 双版本迁移总览

## 项目路径

- 源码包根目录: `E:\游戏\我的世界\源码包`
- 1.7.10 原版源码: `E:\游戏\我的世界\源码包\Hbm-s-Nuclear-Tech-GIT-master`
- 1.7.10 索引: `E:\游戏\我的世界\开发\HBM-NTM-1.20.1-Forge-Port\1.7.10-源码资源迁移索引.md`
- 1.20.1 参考移植版: `E:\游戏\我的世界\源码包\HBM-NTM-high-edition-test`
- 1.20.1 参考索引: `E:\游戏\我的世界\开发\HBM-NTM-1.20.1-Forge-Port\1.20.1-参考移植版源码资源索引.md`
- 当前移植工程: `E:\游戏\我的世界\开发\HBM-NTM-1.20.1-Forge-Port`

## 迁移原则

- 主来源: 1.7.10 原版源码。
- 参考来源: 1.20.1 残缺非官方移植版。
- 当前编辑目标: 干净 Forge 1.20.1 工程。
- 命名策略: 尽量保留 1.7.10 注册 ID。
- 阶段策略: 先资源和模型，再注册和数据，再 BlockEntity/Menu/Screen，最后爆炸/辐射/世界生成。

## 版本差异重点

- 1.7.10 使用 `GameRegistry.registerBlock/registerItem`，1.20.1 使用 `DeferredRegister`。
- 1.7.10 多数机器/核弹依赖 TESR + `ResourceManager` + OBJ，1.20.1 当前优先改为 Forge OBJ block model。
- 1.7.10 资源路径常在 `textures/blocks`, `textures/items`, `textures/models`；1.20.1 需要整理到 `textures/block`, `textures/item` 等现代命名。
- 1.20.1 需要补 blockstates、item models、loot tables、tags、lang。
- 旧版 TileEntity 逻辑不能直接复制，需要先抽出数据状态与行为边界。

## 已迁移模块

- 工程骨架: 已完成。
- 基础配置: 已完成。
- 创造栏: `HBM Parts`, `HBM Machines`, `HBM Nukes`。
- 基础物品: 已迁移一批金属锭、板、粉、线圈、马达。
- 基础机器: 火力锻压机、高炉、电炉、锅炉、粉碎机。
- OBJ 机器模型: 火力锻压机。
- 核弹模型: Gadget, Little Boy, Fat Man, Tsar Bomba, Ivy Mike, The Prototype, F.L.E.I.J.A., The Blue Rinse, N2 Mine, Balefire Bomb, Multi Purpose Bomb。

## 待迁移模块

- 地雷/炸药类基础模型: AP mine, naval mine, fat mine, charges 等。
- 第二批核弹模型游戏内目检: `nuke_mike`, `nuke_prototype`, `nuke_fleija`, `nuke_solinium`, `nuke_n2`, `nuke_fstbmb`, `bomb_multi` 的比例、朝向、贴图与遮挡。
- 核弹/爆炸物 BlockEntity 占位与交互。
- 机器处理逻辑、GUI、配方。
- 爆炸、辐射、污染、粒子/云效果。
- 世界生成和矿石生成。

## 常用源码定位

- 旧版注册: `src/main/java/com/hbm/blocks/ModBlocks.java`, `src/main/java/com/hbm/items/ModItems.java`
- 旧版核弹类: `src/main/java/com/hbm/blocks/bomb`
- 旧版模型/贴图索引: `src/main/java/com/hbm/main/ResourceManager.java`
- 旧版模型: `src/main/resources/assets/hbm/models`
- 旧版贴图: `src/main/resources/assets/hbm/textures`
- 新版注册: `src/main/java/com/hbm/ntm/registry`
- 新版 DataGen: `src/main/java/com/hbm/ntm/datagen`
- 新版资源: `src/main/resources/assets/hbm`

## Skill 与脚本

- 主 skill: `C:\Users\Mr.Wu\.codex\skills\hbm-ntm-migration\SKILL.md`
- 公共脚本: `C:\Users\Mr.Wu\.codex\skills\hbm-ntm-common\scripts`
- 路径解析: `resolve_sources.py --json`
- 符号定位: `locate_hbm_symbol.py <term>`
- 索引生成: `build_source_index.py legacy_1_7_10`
