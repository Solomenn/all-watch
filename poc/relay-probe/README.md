# Relay Probe

一个极小的 Android 前台网络诊断 APK，用来验证 X3 Mini 上的普通第三方应用能否使用系统标准网络 API 访问 HTTPS。

## 当前状态

- 源码已建立；
- 尚未构建；
- 尚未安装；
- 尚未在真机运行；
- 不代表蓝牙代理已验证成功。

## 权限

仅声明：
- INTERNET
- ACCESS_NETWORK_STATE

不声明定位、蓝牙、健康数据、通知、通讯录、麦克风、无障碍或后台服务权限。

## 测试内容

点击“检查网络”后：
1. 读取默认网络是否存在；
2. 读取 INTERNET / VALIDATED 能力和网络传输类型；
3. 对 example.com 与 baidu.com 发起前台 HTTPS GET；
4. 显示 HTTP 状态或 DNS、超时、TLS、I/O 错误类别。

不上传设备标识，不发送用户数据，不保存账号凭据。请求目标网站仍会看到正常网络出口 IP；如不希望访问公共测试站点，应先改为用户自有 HTTPS 测试端点。

## 构建环境

- JDK 17
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- Android SDK Platform 35
- minSdk 26，targetSdk 30

在本目录执行：

~~~powershell
gradle assembleDebug
~~~

APK 预期输出位置：

~~~text
app/build/outputs/apk/debug/app-debug.apk
~~~

构建完成不等于获得安装授权。安装到真实手表前，必须先向用户说明安装命令、预期变化和卸载回滚方式，并取得明确批准。
