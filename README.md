# 奇瑞工程密码计算器

一款专为奇瑞车型设计的工程密码/OTA密码计算工具，基于车架号快速生成当天访问密码。

## 功能特点

- 🔐 **密码计算** - 根据车架号自动计算工程密码和OTA密码
- 📅 **自动日期** - 自动获取当前日期进行计算
- 📝 **历史记录** - 本地保存常用车架号，一键快速计算
- 🎨 **科技UI** - 现代化毛玻璃效果设计
- 📱 **跨平台** - 支持 Android、iOS、Web、Windows、macOS、Linux

## 计算公式

```
密码 = (车架号后6位 + 123456) × 当天日期
结果取后6位
字母用0替换
```

## 适用范围

适用于奇瑞各种车型，OTA密码与工程密码相同。

## 在线体验

Web 版本：https://oliyo2023.github.io/sear.oliyo.com/

## 安装方式

### 从 GitHub Releases 下载

1. 前往 [Releases](https://github.com/oliyo2023/sear.oliyo.com/releases) 页面
2. 下载对应平台的安装包
3. 安装并运行

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/oliyo2023/sear.oliyo.com.git
cd sear.oliyo.com

# 安装依赖
flutter pub get

# 运行应用
flutter run

# 构建发布版本
flutter build apk --release          # Android APK
flutter build appbundle --release    # Android App Bundle
flutter build web --release          # Web
flutter build windows                # Windows
flutter build macos                  # macOS
flutter build ios                    # iOS
```

## 使用方法

1. 输入完整的车架号（17位）
2. 点击"计算密码"按钮
3. 获取当天的工程密码/OTA密码

### 历史记录

- 计算后自动保存车架号后6位
- 点击历史记录项可快速重新计算
- 点击删除图标可移除不需要的记录

## 技术栈

- **Flutter** - 跨平台UI框架
- **Dart** - 编程语言
- **SharedPreferences** - 本地数据存储

## 开发环境

- Flutter SDK: 3.41.8+
- Dart SDK: 3.5.0+
- Android Studio / VS Code

## 项目结构

```
lib/
└── main.dart          # 主应用代码
```

## 许可证

本项目仅供学习和个人使用。请勿用于商业用途。

## 免责声明

本工具仅供学习交流使用，使用者需自行承担使用风险。作者不对使用本工具造成的任何后果负责。

## 联系方式

- GitHub: [@oliyo2023](https://github.com/oliyo2023)

---

⭐ 如果这个项目对你有帮助，请给个 Star！
