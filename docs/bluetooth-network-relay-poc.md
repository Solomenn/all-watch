# Bluetooth Network Relay POC

状态：**方案已建立；尚未执行真机验证。**

目标设备：OPPO Watch X3 Mini + iPhone。  
产品归属：All-Watch 阶段 0。  
本轮范围：只验证普通第三方手表 APK 能否通过现有 iPhone—手表连接访问 HTTPS；不开发 Todo UI，不修改原厂应用。

## 1. 已知事实与证据边界

### 用户观察

用户反馈：手表没有连接 Wi-Fi，但仍可通过 iPhone 上网。

这证明用户观察到的某些手表联网功能在该条件下可用，但尚不能证明普通第三方 APK 也能使用该通道。

### OPPO 官方资料

《健康 App 个人信息保护政策》描述：当穿戴设备无法通过 eSIM 或 Wi-Fi 联网时，可通过与健康 App 建立的蓝牙数据通道，由健康 App 转发网络请求或代为发起网络请求。政策举例是手表使用小布助手和天气服务。

来源：[OPPO 健康 App 个人信息保护政策（4.24 版本 PDF）](https://health.heytapdownload.com/agreement/pdfagreements/20250715185538/4_24%E7%89%88%E6%9C%AC-%E5%81%A5%E5%BA%B7APP%E4%B8%AA%E4%BA%BA%E4%BF%A1%E6%81%AF%E4%BF%9D%E6%8A%A4%E6%94%BF%E7%AD%96.pdf)

**解释边界：**该政策证明 OPPO 自有服务存在蓝牙请求转发机制，不是面向第三方开发者的 API 文档，也没有承诺任意 APK 都能调用。

### 设备历史信息

关联研究仓库 may-watch-research 曾记录 OWW263、Android 11、API 30 和 ADB 授权连接。该信息是历史记录，不代表本轮设备仍在线或系统版本未变；执行前必须重新只读确认。该研究还明确区分了 ColorOS Watch 与 Wear OS：本 POC 不把 Android 官方 Wear OS 的“蓝牙连接时经手机代理网络”规则直接套用到 X3 Mini。可参考 [Wear OS 网络通信说明](https://developer.android.com/training/wearables/data/network-communication?hl=zh-cn)，但它不是 X3 Mini 的兼容性证明。

## 2. 要回答的唯一问题

在以下条件同时成立时，一个没有 OPPO 系统签名、没有特殊权限的普通第三方 APK，能否通过 Android 标准网络 API 完成 HTTPS 请求？

- X3 Mini 与 iPhone 已通过官方 OPPO 健康 App 配对；
- 手表 Wi-Fi 关闭或未连接；
- iPhone 蓝牙保持连接，且 iPhone 自身可以访问互联网；
- 手表上的 OPPO 自有联网功能（例如天气）可作为正向对照；
- 测试 APK 只请求公开 HTTPS 测试站点，不读取或上传个人数据。

## 3. 待验证假设

| 假设 | 预期表现 | 解释 |
|---|---|---|
| H1：系统级默认网络可用 | 普通 APK 可读取到可用网络，HTTPS 成功 | 最理想；Todo 手表端可直接请求后端 |
| H2：仅 OPPO 自有服务可用 | 天气/小布助手可联网，普通 APK HTTPS 失败 | 蓝牙通道由厂商服务按业务代理，第三方不能直接复用 |
| H3：当前连接条件未建立代理 | 正向对照和普通 APK 均失败 | 先排查配对、后台状态、iPhone 网络和 Wi-Fi 条件，不能直接判断机制不存在 |

即使 H1 成立，也仍需另外验证后台运行、断线重连、请求超时和功耗；一次前台 HTTPS 成功不代表长期后台可用。

## 4. 测试流程

### Phase A：只读诊断

在 iPhone 蓝牙连接保持、手表未连接 Wi-Fi、iPhone 网络正常的状态下：

1. 确认手表与 iPhone 仍由官方 OPPO 健康 App 配对。
2. 用一个已知依赖联网的手表原厂功能（天气或小布助手）做正向对照。
3. 在 Windows 上重新确认 ADB 目标设备和系统版本。
4. 只读查看 Android 网络状态、Wi-Fi 状态和路由摘要。
5. 输出脱敏摘要，不提交原始设备标识或完整 dumpsys。

建议只读命令（先确认设备后执行）：

~~~powershell
adb devices -l
adb -d shell getprop ro.product.model
adb -d shell getprop ro.build.version.release
adb -d shell getprop ro.build.version.sdk
adb -d shell dumpsys connectivity
adb -d shell dumpsys wifi
adb -d shell ip route
~~~

注意：
- adb -d 仅在确认唯一 USB 设备就是目标手表后使用。
- 不要将包含 MAC、IP、SSID、设备标识的完整输出提交 GitHub；先在本地检查并脱敏。
- 若 ADB 当前不可用，停止，不要为本 POC 开启 Root、无线调试或更改安全设置。
- 只读系统状态不能单独证明第三方 APK 的网络访问能力。

### Phase B：普通 APK 最小验证

只有 Phase A 结果完整、并且用户单独批准安装测试 APK 后才进行。

构建一个最小 Java/Android APK（不依赖 Wear OS 专有 API）。源码骨架已放在 [poc/relay-probe](../poc/relay-probe/README.md)，但尚未构建或安装：

- 权限：INTERNET、ACCESS_NETWORK_STATE；
- 一个页面显示 ConnectivityManager.activeNetwork 和必要的能力摘要；
- 一个按钮通过标准 HttpURLConnection 发起前台 HTTPS GET；
- 显示 DNS、TLS、HTTP 状态和耗时；
- 不启用后台服务、无障碍、开机自启或常驻连接；
- 不读取健康数据、通知、通讯录、定位或设备唯一标识；
- 不包含模型 API Key、账号 Token 或 Cookie；
- 只访问预先写入测试配置的公开 HTTPS 地址。

当前源码使用两个 HTTPS 测试地址，分别记录结果，避免单个域名不可达造成误判：
- https://example.com/
- https://www.baidu.com/

如果需要更可靠的归因，可改用用户自己控制的 HTTPS 测试端点；不得把密钥写入 APK 或仓库。

安装测试 APK 会改变手表应用安装状态。执行前必须向用户展示 APK 权限、安装命令、预期变化和卸载回滚命令，并取得针对该安装的明确批准。不得把“同意开展 POC”视为对所有设备写操作的无限授权。

### Phase C：判定

| 正向对照 | 普通 APK HTTPS | 结论 |
|---|---|---|
| 成功 | 成功 | **已验证**：该测试环境下普通第三方 APK 可以通过标准网络 API 联网 |
| 成功 | 失败 | **已验证**：原厂业务可联网；该普通 APK 未能复用。进一步区分网络能力/权限/代理白名单前，不得断言所有第三方应用永远不行 |
| 失败 | 失败 | **未验证**：本轮代理条件没有建立或正向对照不成立 |
| 失败 | 成功 | **已验证**：普通 APK 的联网路径可用，但原厂正向对照测试条件需复查 |

失败时记录具体错误类别：无默认网络、DNS 失败、连接超时、TLS 失败、HTTP 错误。不要只记录“没网”。

## 5. 成功后的产品路线

若普通 APK 能通过标准 HTTPS API 联网：

1. 为 Todo 后端建立独立 HTTPS API；
2. 手表端只保存最小离线任务缓存和待同步操作队列；
3. 前台同步先行，后台同步另做功耗和生命周期验证；
4. iPhone、Windows 和手表使用统一任务 ID、版本号和幂等操作；
5. 不把蓝牙代理当成唯一网络路径，保留手表 Wi-Fi 和手机/云端同步回退。

## 6. 失败后的产品路线

若原厂天气可用但普通 APK 无法联网：

1. 不调用未公开私有接口，不绕过 OPPO 签名或权限校验；
2. 先查是否有公开、授权的 OPPO 第三方开发接口；
3. 没有公开接口时，评估手表 Wi-Fi 直连 HTTPS；
4. 再评估由手机端 App 主动同步、手表端通过受支持的应用通信机制交换任务；
5. 若 iOS 无法向第三方开放现有 OPPO 蓝牙连接，则不假设能自行接管该连接。

## 7. 完成标准

- [ ] 重新确认目标手表与系统版本；
- [ ] 记录 iPhone 蓝牙配对、手表 Wi-Fi 未连接、手机互联网可用；
- [ ] 原厂联网正向对照结果；
- [ ] 只读网络状态摘要（脱敏）；
- [ ] 普通 APK 标准 HTTPS 测试结果（仅在用户批准安装后）；
- [ ] 明确标注证据等级、失败类别和可复现条件；
- [ ] 更新本文件和 HANDOFF；
- [ ] 设备状态变化如实记录。

**当前结论：** OPPO 自有服务的蓝牙网络请求转发机制有官方文档支持；第三方 APK 是否可直接使用，仍是本 POC 的待验证问题。
