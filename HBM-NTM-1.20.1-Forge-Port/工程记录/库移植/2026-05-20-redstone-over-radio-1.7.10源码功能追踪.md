# Redstone Over Radio 1.7.10 源码功能追踪

## 范围

- 记录 1.7.10 无线红石/远程控制 ROR 接口。
- 该库服务远程机器控制、信息查询和脚本式命令调用。

## 1.7.10 源文件

- `src/main/java/api/hbm/redstoneoverradio/IRORInfo.java`
- `src/main/java/api/hbm/redstoneoverradio/IRORInteractive.java`
- `src/main/java/api/hbm/redstoneoverradio/IRORValueProvider.java`
- `src/main/java/api/hbm/redstoneoverradio/RORFunctionException.java`

## 旧版契约

- `IRORInfo` 提供组件名称、频率、展示信息等基础数据。
- `IRORInteractive`：
  - `runRORFunction(String name, String[] params)` 执行命令。
  - 命令格式使用 `NAME_SEPARATOR = "!"`。
  - 参数格式使用 `PARAM_SEPARATOR = ":"`。
  - `getCommand`、`getParams`、`parseInt` 为静态解析辅助。
- `IRORValueProvider` 提供可被读取的数值。
- 异常字符串是旧 UI/命令反馈的一部分：
  - null command
  - multiple separators
  - invalid parameter format

## 迁移计划

- 先保留命令格式，以便旧机器行为可查可测。
- 现代端可改成 typed command，但需要兼容旧字符串语义。
- 远程调用必须在服务端执行，客户端仅显示反馈。

## 验证清单

- 命令解析错误能返回旧版等价错误。
- 参数范围校验一致。
- 可交互组件与只读数值组件分离。

## 2026-05-23 能量库首批 ROR 本地兼容接入

- 本批以 `TileEntityMachineBattery`、`TileEntityBatteryBase`、`TileEntityBatterySocket` 的 1.7.10 ROR 用法为落点：
  - 迁入现代本地接口 `RORInfo`、`RORValueProvider`、`RORInteractive`、`RORFunctionException`。
  - 保留旧字符串协议：
    - `VAL:fill`
    - `VAL:fillpercent`
    - `VAL:delta`
    - `FUN:setmode!mode(:fallback)`
    - `FUN:setredmode!mode(:fallback)`
    - `FUN:setpriority!priority`
  - 保留旧异常文本：
    - `Exception: Null Command`
    - `Exception: Multiple Name Separators`
    - `Exception: Parameter in Invalid Format`
- 现代端接入：
  - `MachineBatteryBlockEntity` 实现 ROR 值读取与交互函数。
  - `MachineBatterySocketBlockEntity` 实现 ROR 值读取与交互函数，并补本地 OC 风格 `getEnergyInfo/getPackInfo/getModeInfo/getInfo` 数组返回。
  - `/hbm energy ror functions <pos>`、`/hbm energy ror value <pos> <name>`、`/hbm energy ror run <pos> <command>` 作为无外部 mod 时的调试/验证入口。
- 现代边界：
  - 这不是完整无线红石方块网络或外部集成，只是把旧组件 API 与能量库消费者先固定下来。
  - 后续迁移无线红石方块时，应直接调用这些接口，而不是重新定义一套命令格式。

## 2026-06-04 新版源码差异补记

对比旧快照与新版 5714 源码：

- 新增 `radio_autocal` 方块、`TileEntityRadioAUTOCAL`、`GUIScreenRadioAUTOCAL`、`RenderAUTOCAL` 与 `ParseMSES1`/`IParse` 脚本解析模块。
- AUTOCAL 通过 RoR 读写外部信号，脚本状态包含 `isOn`、`ignoreError`、`autoReboot`、`script[]`、`current`、`clockSpeed`、`buffer`、`variables` 与 6 行 history。
- `ServerConfig.AUTOCAL_MAX_CLOCK` 默认 `20`，GUI 文档说明最大 clock speed 由该 server config 限制。
- 现代 RoR 迁移后续要把 AUTOCAL 作为脚本化 RoR 设备单独建模，而不是只按普通无线红石收发器处理。
