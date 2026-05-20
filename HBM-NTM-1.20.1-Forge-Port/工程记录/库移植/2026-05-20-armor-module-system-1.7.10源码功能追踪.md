# 护甲模块系统 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 护甲改装槽、NBT 存储、适用性和动力甲/FSB 扩展。
- 该库与防具、防护、能量电池、夜视、喷气背包、特殊能力联动。

## 1.7.10 源文件

- `src/main/java/com/hbm/handler/ArmorModHandler.java`
- `src/main/java/com/hbm/items/armor/ItemArmorMod.java`
- `src/main/java/com/hbm/items/armor/ArmorFSB.java`
- `src/main/java/com/hbm/items/armor/ArmorFSBPowered.java`
- `src/main/java/com/hbm/items/armor/ArmorFSBFueled.java`
- `src/main/java/com/hbm/items/armor/IPAWeaponsProvider.java`
- `src/main/java/com/hbm/interfaces/IArmorModDash.java`
- 具体 mod item：`src/main/java/com/hbm/items/armor/ItemMod*.java`

## 旧版契约

- mod slot 常量：
  - `helmet_only = 0`
  - `plate_only = 1`
  - `legs_only = 2`
  - `boots_only = 3`
  - `servos = 4`
  - `cladding = 5`
  - `kevlar = 6`
  - `extra = 7`
  - `battery = 8`
  - `MOD_SLOTS = 9`
- NBT：
  - 根 key：`ntm_armor_mods`
  - slot key：`mod_slot_<slot>`
  - 每个 slot 保存完整 ItemStack NBT。
- API：
  - `isApplicable(armor, mod)` 根据 armorType 与 mod 支持位判断。
  - `applyMod` 写入 slot。
  - `removeMod` 删除 slot。
  - `clearMods` 清空所有 mod。
  - `hasMods` 判断是否有改装。
  - `pryMods` 和 `pryMod` 读取并自动清理无效 mod。
- UUID：
  - 普通 UUID 与 fixed UUID 数组用于属性修饰器稳定 id。

## 迁移计划

- 保留旧 NBT key 以支持存档/物品迁移。
- 现代端可用 DataComponent 或 ItemStack tag，但接口应封装，不让调用方直接读 NBT。
- slot 类型建议用 enum 表达，同时保留旧 index。
- 防护、能量、喷气背包、夜视等具体 mod 后续逐个补行为。

## 验证清单

- 旧 NBT 物品能读出 mod。
- 不适用的 mod 不能装入对应护甲。
- 移除最后一个 mod 后清理根 compound。
- 属性 UUID 稳定，不重复叠加。
