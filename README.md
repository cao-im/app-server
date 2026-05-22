# App Server - 曹操IM应用服务（C端认证）

> 纯后端 API 服务，依赖 **im-core** JAR，为 Flutter Demo 等客户端提供用户登录、注册、换取 IM Token 的认证接口。

---

## 架构设计

```
┌─────────────────────────────────────────────────────┐
│                   曹操IM 体系                         │
│                                                     │
│  flutter-demo (Flutter客户端)                        │
│       │                                             │
│       ▼  使用 im-sdk-flutter 连接 im-server          │
│                                                     │
│  app-server (本服务)                                 │
│  ├── 依赖: im-core (JAR) ◀── im-server 的核心库      │
│  ├── 职责: 用户认证 (登录/注册/换Token)              │
│  └──  端口: 8080                                     │
│                                                     │
│  im-server (IM核心)                                  │
│  ├── im-core (JAR) ←── app-server 引用这个           │
│  └── im-boot (独立运行, 端口8081)                    │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### app-server 与 im-server 的关系

| 项目 | 职责 | 端口 | 数据库 |
|------|------|------|--------|
| **app-server** | C端用户认证（登录/注册/Token） | 8080 | `cao_im_app_server` |
| **im-server/im-boot** | IM核心业务（消息/群组/好友/WebSocket） | 8081 | `cao_im_db` |
| **im-server/im-core** | IM核心库JAR，被app-server依赖调用 | - | - |

---

## Maven 依赖

app-server 的 pom.xml 中已引入 im-core：

```xml
<dependency>
    <groupId>com.caoim</groupId>
    <artifactId>im-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

通过 `AppImBridgeService` 桥接调用 IM 能力：

```java
@Autowired
private AppImBridgeService imBridge;

// 发送消息
imBridge.sendPrivateMessage(fromId, toId, "你好");

// 获取会话列表
List<Conversation> conversations = imBridge.getConversations(userId);
```

---

## 接口文档

所有接口无需 Token 即可访问。

| 方法 | 路径 | 说明 | 密码要求 |
|------|------|------|---------|
| POST | `/api/client/login` | C端用户登录 | MD5加密后传输 |
| POST | `/api/client/register` | C端用户注册 | MD5加密后传输 |
| POST | `/api/client/refresh-token` | 刷新Token | Bearer refreshToken |

### 登录响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbG...(accessToken)",
    "user": { "id": 1, "username": "test", "nickname": "测试" }
  }
}
```

### 密码安全机制

```
客户端 → [MD5加密] → app-server → [BCrypt存储] → MySQL
```

---

## 环境要求

| 环境 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |

## 快速开始

### 1. 安装 im-core 到本地仓库

```bash
cd im-server
mvn clean install -DskipTests
```

### 2. 初始化数据库

```bash
# app-server 数据库
mysql -u root -p < app-server/src/main/resources/schema.sql

# im-server 数据库（如需独立运行 im-boot）
mysql -u root -p < im-server/im-boot/src/main/resources/schema.sql
```

### 3. 启动 app-server

```bash
cd app-server
mvn spring-boot:run
```

访问：http://localhost:8080

- Swagger: http://localhost:8080/swagger-ui.html

---

## 项目结构

```
app-server/
├── pom.xml                       # Maven配置（含im-core依赖）
├── src/main/java/com/caoim/appserver/
│   ├── AppServerApplication.java # 启动类
│   ├── config/                   # Security/JWT/CORS 配置
│   ├── controller/AuthController.java  # 登录/注册/刷新Token
│   ├── service/
│   │   ├── UserService.java     # 用户业务
│   │   └── AppImBridgeService.java  # ImService桥接（调用im-core）
│   ├── security/                 # JWT工具/过滤器
│   ├── dao/dto/entity/common     # 基础设施层
│   └── config/DataInitializer.java # 启动初始化admin账号
├── src/main/resources/
│   ├── application.yml           # 配置文件
│   └── schema.sql                # 数据库脚本
└── README.md
```
