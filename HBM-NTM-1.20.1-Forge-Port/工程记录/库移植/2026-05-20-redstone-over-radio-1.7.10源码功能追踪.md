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
