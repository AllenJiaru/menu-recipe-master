# 食遇 Shiyu - 情侣菜谱点餐应用

> 一款支持 AI 智能助手的情侣菜谱点餐全栈应用，包含 Spring Boot 后端、Vue 3 管理后台、Android 移动端和鸿蒙端。

## 项目截图

| 管理后台 | Android 端 | AI 助手 |
|:---:|:---:|:---:|
| Vue 3 + Element Plus | Jetpack Compose + Hilt | 16 家 AI 供应商接入 |

## 技术栈

| 端 | 技术 |
|---|---|
| **后端** | Spring Boot 3.2 · Java 17 · MyBatis-Plus · MySQL · JWT · Spring Security |
| **前端管理** | Vue 3 · Vite 5 · TypeScript · Element Plus · Pinia · ECharts · SCSS |
| **Android** | Kotlin · Jetpack Compose · Hilt · Room · Coil · Retrofit |
| **鸿蒙** | ArkTS · ArkUI · HarmonyOS |

## 功能模块

### 核心功能
- **菜谱管理** - 创建/编辑/删除菜谱，支持分类、食材清单、烹饪步骤、图片上传
- **订单系统** - 食客点餐 → 主厨接单 → 制作中 → 完成上菜，全流程状态追踪
- **相册** - 美食照片上传与管理

### AI 智能助手
- **16 家 AI 供应商** - OpenAI / Claude / Gemini / DeepSeek / 通义千问 / 文心一言 / 智谱 / Moonshot / 百川 / 讯飞 / Ollama 等
- **AI 聊天** - 持久化会话、打字机效果、Markdown 渲染、用户头像
- **智能推荐** - 根据口味和时令推荐菜品
- **膳食计划** - AI 安排多天餐食
- **营养分析** - 分析菜品营养成分
- **剩余食材利用** - 根据剩余食材推荐菜品
- **AI 翻译 / 评分 / 识别 / 烹饪问答**

### 系统管理
- **用户权限** - 角色管理（超级管理员 / 管理员 / 主厨 / 食客）、权限分配
- **组织管理** - 层级组织结构、成员管理、邀请加入
- **数据统计** - 仪表板、菜谱分类分布、订单趋势
- **库存管理** - 食材库存跟踪、低库存预警、自动补货
- **供应商管理** - 供应商信息维护
- **公告通知** - 系统公告、通知中心
- **数据导出** - 菜谱 / 订单 CSV 导出
- **操作日志** - 系统操作审计
- **系统监控** - 数据库状态、系统健康

### 移动端特性
- **离线缓存** - Room 本地数据库
- **自动同步** - 定时云端数据同步
- **烹饪计时器** - 内置计时功能
- **菜谱评分** - 评价与评论系统
- **收藏系统** - 收藏喜欢的菜谱
- **多语言** - 中文 / 英文完整国际化（600+ 词条）
- **深色模式** - 支持浅色 / 深色 / 跟随系统

## 快速开始

### 环境要求

| 工具 | 版本 |
|---|---|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.8+ |
| Android Studio | Hedgehog+ (Android 14) |

### 1. 启动后端

```bash
cd shiyu-backend

# 创建数据库
mysql -u root -p -e "CREATE DATABASE shiyu_db DEFAULT CHARACTER SET utf8mb4;"

# 导入初始数据（可选）
mysql -u root -p shiyu_db < src/main/resources/schema.sql

# 修改数据库配置
# 编辑 src/main/resources/application-dev.yml

# 编译并启动
mvn clean package -DskipTests
java -jar target/shiyu-backend-0.0.1-SNAPSHOT.jar
```

后端运行在 `http://localhost:8081`

默认管理员账号：`admin` / `admin123`

### 2. 启动前端管理后台

```bash
cd shiyu-admin

npm install
npm run dev
```

前端运行在 `http://localhost:5173`

### 3. 编译 Android App

```bash
cd ShiyuApp

# 使用 Android Studio 打开项目
# 或命令行编译
./gradlew assembleDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

> 需要在 `ApiConfig.kt` 中配置后端服务器地址

## 项目结构

```
menu-recipe-master/
├── shiyu-backend/          # Spring Boot 后端
│   ├── src/main/java/com/shiyu/
│   │   ├── controller/     # REST 控制器
│   │   ├── service/        # 业务逻辑
│   │   ├── entity/         # 数据实体
│   │   ├── mapper/         # MyBatis-Plus Mapper
│   │   ├── security/       # JWT 认证
│   │   ├── config/         # 配置类
│   │   └── dto/            # 请求/响应 DTO
│   └── src/main/resources/
│       ├── application.yml
│       └── schema.sql
├── shiyu-admin/            # Vue 3 管理后台
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── api/            # API 接口
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── router/         # 路由配置
│   │   ├── utils/          # 工具函数
│   │   └── components/     # 公共组件
│   └── package.json
├── ShiyuApp/               # Android App
│   └── app/src/main/java/com/example/shiyu/
│       ├── ui/             # Compose UI
│       │   ├── ai/         # AI 聊天
│       │   ├── chef/       # 主厨端
│       │   ├── diner/      # 食客端
│       │   ├── settings/   # 设置
│       │   └── ...
│       ├── api/            # Retrofit API
│       ├── data/           # Room 数据库
│       ├── sync/           # 数据同步
│       └── util/           # 工具类
├── menu-recipe-master/     # 鸿蒙 App (ArkTS)
└── README.md
```

## AI 配置

在管理后台 **AI 设置** 页面配置 AI 供应商：

1. 选择当前使用的 AI 供应商（如 DeepSeek、通义千问等）
2. 填入对应的 API Key
3. 配置 Base URL（国内供应商通常有专用地址）
4. 测试连接成功后即可使用

支持的供应商类型：
- **OpenAI 兼容** - DeepSeek / 通义千问 / 智谱 / Moonshot / 百川 / 讯飞 / 零一万物 / 硅基流动
- **Claude** - Anthropic Claude API
- **Gemini** - Google Gemini API
- **Ollama** - 本地模型（如 Llama 3、Qwen 等）

## API 接口

| 模块 | 端点 | 说明 |
|---|---|---|
| 认证 | `POST /api/auth/login` | 登录 |
| 认证 | `POST /api/auth/register` | 注册 |
| 菜谱 | `GET/POST /api/recipes` | 菜谱 CRUD |
| 订单 | `GET/POST /api/orders` | 订单管理 |
| AI | `POST /api/ai/chat-session` | AI 对话 |
| AI | `GET /api/ai/sessions` | 会话列表 |
| 同步 | `POST /api/sync/push` | 数据推送 |
| 同步 | `GET /api/sync/pull` | 数据拉取 |

## 许可证

MIT License

## 作者

**AllenJiaru** - [GitHub](https://github.com/AllenJiaru)
