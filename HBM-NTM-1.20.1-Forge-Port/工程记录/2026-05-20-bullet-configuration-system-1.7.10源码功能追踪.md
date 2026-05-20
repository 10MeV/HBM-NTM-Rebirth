# 弹药配置系统 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 旧 `BulletConfiguration`、NPC/能量/火箭配置工厂、同步工具、抛壳系统入口。
- 该库服务枪械、NPC 武器、炮弹、特殊 projectile。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/BulletConfiguration.java`
- `src/main/java/com/hbm/handler/BulletConfigSyncingUtil.java`
- `src/main/java/com/hbm/handler/CasingEjector.java`
- `src/main/java/com/hbm/handler/guncfg/BulletConfigFactory.java`
- `src/main/java/com/hbm/handler/guncfg/GunDGKFactory.java`
- `src/main/java/com/hbm/handler/guncfg/GunEnergyFactory.java`
- `src/main/java/com/hbm/handler/guncfg/GunNPCFactory.java`
- `src/main/java/com/hbm/handler/guncfg/GunRocketFactory.java`
- `src/main/java/com/hbm/entity/projectile/EntityBulletBaseNT.java`
- `src/main/java/com/hbm/particle/SpentCasing.java`

## 旧版契约

- `BulletConfiguration` 虽标记 `@Deprecated`，但仍被引用。
- 数据面：
  - ammo、ammoCount、velocity、spread、wear、bulletsMin/Max。
  - damage min/max、headshotMult、gravity、maxAge。
  - ricochet、penetration、spectral、break glass、liveAfterImpact。
  - incendiary、emp、explosive、jolt、rainbow、nuke、shrapnel、chlorine、leadChance、caustic 等效果。
  - behavior hooks：hurt、hit、ricochet、impact、update。
  - style、trail、plink、vanilla particle、spent casing。
  - energy projectile fields：dischargePerShot、modeName、chatColour、firingRate。
  - damage source flags：projectile、fire、explosion、bypass。
- style 常量：
  - normal、pistol、flechette、pellet、bolt、rocket、grenade、orb、meteor、APDS、blade、tau 等。
- sync：
  - `BulletConfigSyncingUtil` 用 int key 保存部分配置，供客户端/实体同步。

## 迁移计划

- 不建议直接保留可变大对象；现代端应拆成 immutable config + behavior strategy。
- 先迁 style/trail/plink/damage flags 的 enum。
- NPC/特殊武器仍需兼容 key-based sync。
- 抛壳 `SpentCasing` 与粒子/声音系统应作为客户端表现层。

## 验证清单

- 同一种弹药的速度、散布、伤害、重力与旧版一致。
- ricochet、penetration、glass break 行为可单独测试。
- 配置同步 key 对应正确 config。
