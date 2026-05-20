# 兼容集成层 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 对外 mod/API 兼容入口。
- 该库涉及 NEI、AE2、OpenComputers、EnergyControl、矿辞、流体、配方、microblocks 等。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/CompatHandler.java`
- `src/main/java/com/hbm/util/Compat.java`
- `src/main/java/com/hbm/util/CompatExternal.java`
- `src/main/java/com/hbm/util/CompatRecipeRegistry.java`
- `src/main/java/com/hbm/util/CompatFluidRegistry.java`
- `src/main/java/com/hbm/util/CompatEnergyControl.java`
- `src/main/java/com/hbm/handler/ae2`
- `src/main/java/com/hbm/handler/imc`
- `src/main/java/com/hbm/handler/microblocks`
- `src/main/java/com/hbm/handler/nei`

## 旧版契约

- `CompatHandler` 负责按已加载 mod 触发兼容初始化。
- `CompatRecipeRegistry` 与 `CompatFluidRegistry` 处理跨 mod 配方/流体注册。
- `CompatEnergyControl` 定义 EnergyControl 使用的 NBT key。
- AE2 兼容包括 mass storage、arc furnace large 外部存储。
- IMC handler 支持其他 mod 注入机器配方。
- NEI handler 大量存在，但现代迁移应转向 JEI/REI 类展示层。

## 迁移计划

- 兼容层不作为第一批强制迁移，先保留接口位置和旧行为记录。
- JEI 展示应基于统一 recipe 数据，不直接搬 NEI handler。
- AE2/OC 等现代版本 API 差异大，需要后续按目标版本重新查官方文档。
- EnergyControl key 若仍需兼容，保留旧 key 名。

## 验证清单

- 未安装兼容 mod 时不加载其类。
- 配方注入在服务端和客户端一致。
- JEI 展示不改变实际机器配方数据。
