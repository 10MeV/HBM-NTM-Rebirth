# 伤害与抗性核心 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 自定义伤害源、实体伤害工具、抗性注册与防具/实体抵抗逻辑。
- 该库被武器、爆炸、辐射、防具、实体、怪物系统复用。

## 1.7.10 源文件

- `src/main/java/com/hbm/lib/ModDamageSource.java`
- `src/main/java/com/hbm/util/EntityDamageUtil.java`
- `src/main/java/com/hbm/util/DamageResistanceHandler.java`
- `src/main/java/api/hbm/entity/IResistanceProvider.java`
- `src/main/java/api/hbm/entity/IRadiationImmune.java`
- `src/main/java/com/hbm/items/armor`
- `src/main/java/com/hbm/explosion`

## 旧版契约

- `ModDamageSource` 集中定义 HBM 自定义伤害字符串和 damage source。
- `EntityDamageUtil`：
  - 包含自定义伤害应用、击退、爆炸相关伤害、穿甲或绕过逻辑。
  - 有旧 SEDNA damage system 的实现片段。
- `DamageResistanceHandler`：
  - 按实体 class 注册抗性。
  - 可从 `IResistanceProvider` 查询实体自定义抗性。
  - 将 damage source 映射到抗性类别，如 physical、fire、explosion 等。
  - 与 FSB/动力甲等防具联动。
- 伤害类别和抗性值会影响武器、爆炸、实体特殊弱点。

## 迁移计划

- 先建立现代 `DamageType`/`DamageSource` 注册与旧字符串映射。
- 抗性注册数据可先迁成 Java registry，后续再考虑 datapack。
- `IResistanceProvider` 应保留为实体扩展接口。
- 爆炸和武器迁移前必须先迁基本伤害类别，否则数值会大幅偏移。

## 验证清单

- 旧伤害字符串能映射到现代 DamageType。
- 防具、实体内置抗性、接口抗性能共同生效。
- 创造模式、无敌、驯服实体等特殊逻辑不破坏原版规则。
