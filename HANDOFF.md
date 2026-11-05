# HANDOFF

更新时间：2026-11-05

## 当前任务

**Phase A：只读确认 X3 Mini 是否在 iPhone 蓝牙连接下获得系统级网络。**

当前不安装 APK、不修改设备设置、不接触健康数据。

## 已完成

- 初始化 All-Watch 仓库结构和项目规则。
- 写入 Bluetooth Network Relay POC 方案。
- 建立最小 Java Android 测试应用源码：poc/relay-probe。
- 测试应用仅声明 INTERNET、ACCESS_NETWORK_STATE；没有蓝牙、定位、健康数据、通知、无障碍或后台服务权限。
- 测试应用只检查 ConnectivityManager 默认网络，并对 example.com、baidu.com 发起前台 HTTPS GET。

## 尚未完成 / 不得声称已完成

- 未在本轮连接真机。
- 未执行 ADB 诊断。
- 未构建 APK。
- 未安装 APK。
- 未验证第三方应用能否使用 OPPO 蓝牙网络代理。
- 未改变手表或 iPhone 状态。

## 背景证据

OPPO 健康 App 4.24 版隐私政策说明：穿戴设备无法通过 eSIM/Wi-Fi 联网时，可通过与健康 App 的蓝牙数据通道转发网络请求；政策举例为小布助手和天气服务。

这只证明 OPPO 自有服务存在请求转发机制，不证明普通第三方 APK 可以复用。详见 docs/bluetooth-network-relay-poc.md。

历史研究记录曾确认 OWW263 / Android 11 / API 30 和 ADB 授权连接，但执行前必须重新确认当前设备与授权状态。

## 下一步：只读诊断

1. 阅读 README.md、AGENTS.md、docs/bluetooth-network-relay-poc.md 和 poc/relay-probe/README.md。
2. 检查本机 ADB、JDK、Gradle、Android SDK 是否已存在；不要为了本轮诊断擅自安装软件或修改 PATH。
3. 让用户确认 iPhone 蓝牙仍连接手表、iPhone 可上网、手表 Wi-Fi 未连接。
4. 先用手表原厂天气等联网功能做正向对照。
5. 执行以下只读命令；只有确认唯一 USB 设备是目标手表后才使用 adb -d：

~~~powershell
adb devices -l
adb -d shell getprop ro.product.model
adb -d shell getprop ro.build.version.release
adb -d shell getprop ro.build.version.sdk
adb -d shell dumpsys connectivity
adb -d shell dumpsys wifi
adb -d shell ip route
~~~

6. 在本地查看完整输出，只把脱敏后的网络能力摘要写入交接文档。不要提交 MAC、SSID、IP、序列号或完整 dumpsys。
7. 若 JDK 17、Gradle 8.9、Android SDK Platform 35 已可用，可执行 gradle assembleDebug 只构建 APK；不要安装。
8. 若 ADB 不可用或设备不唯一，停止并报告，不开启无线调试、不 Root、不改设置。

## 安装门槛

即使 POC 已获准开展，安装测试 APK 仍需单独向用户说明：

- APK 权限和功能；
- 具体安装命令；
- 安装会增加一个测试应用；
- 卸载回滚命令；
- 是否保留测试结果。

取得针对安装动作的明确批准后，才可执行 adb install。不得自行安装或卸载。

## 本轮设备状态

**未连接真机；未执行设备命令；设备状态未改变。**
