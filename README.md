<h1 align="center">智能家政平台</h1>

<p align="center">
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-2.7.3-6DB33F.svg" alt="Spring Boot 2.7.3"></a>
  <a href="https://www.oracle.com/java/"><img src="https://img.shields.io/badge/JDK-17-orange.svg" alt="JDK 17"></a>
  <a href="https://www.mysql.com/"><img src="https://img.shields.io/badge/MySQL-8-4479A1.svg" alt="MySQL 8"></a>
  <a href="https://redis.io/"><img src="https://img.shields.io/badge/Redis-7-DC382D.svg" alt="Redis"></a>
  <a href="https://www.rabbitmq.com/"><img src="https://img.shields.io/badge/RabbitMQ-4.x-FF6600.svg" alt="RabbitMQ"></a>
  <a href="https://vuejs.org/"><img src="https://img.shields.io/badge/Vue-3.5-42B883.svg" alt="Vue 3"></a>
  <a href="https://developers.weixin.qq.com/miniprogram/dev/framework/"><img src="https://img.shields.io/badge/WeChat-MiniProgram-07C160.svg" alt="WeChat Mini Program"></a>
</p>

<p align="center"><em>Spring Boot 后端 · Web 管理端 · 用户/家政员小程序 · RabbitMQ 异步通知 · WebSocket 实时提醒</em></p>

## 项目概述

本仓库包含智能家政平台的**后端、Web 管理端和微信小程序**，基于 Spring Boot 2.7.3 + MyBatis 构建，面向四类用户：

- **管理端（admin）**：管理员、员工、分类、服务、套餐、优惠券、内容、统计
- **员工端（employee）**：服务管理、家政员档期、订单指派/接单/退款、工作台
- **家政员端（worker）**：小程序登录、我的订单、接单/到达/完成/拒单、个人资料
- **用户端（user）**：微信登录、服务浏览、购物车、下单、支付、优惠券、订单、地址、消息

后端承担 Web 管理端和两个小程序的 HTTP 接口，并通过 RabbitMQ + WebSocket 完成订单事件的异步通知。

## 系统架构

```
Web 管理端（Vue）        用户小程序        家政员小程序
        │                   │                 │
        └──────────── HTTP + JWT（token 请求头）────────────┘
                              │
                    Spring Boot :8080
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   Controller               Service               WebSocket
 admin/employee/worker/user    │              /ws/order-notify
        │                     │                     ▲
        │                  MyBatis                  │
        │                     │                     │
        │                  MySQL 8             RabbitMQ 消费者
        │                     │              order.notify.queue
        │                     │                     ▲
        └── Redis / Redisson ─┘              order.events / 延迟队列
             缓存 / 分布式锁 / 秒杀 Lua          ▲
                                                │
                                      订单业务事务写 mq_outbox
```

## 项目结构

```
smart-housekeeping-system/
├── smart-common/                  # 公共模块
│   ├── constant/                  # 状态码、消息、JWT、订单事件常量
│   ├── exception/                 # 业务异常
│   ├── properties/                # JWT / OSS / 微信配置
│   ├── result/                    # Result、PageResult
│   └── utils/                     # JwtUtil、OSS、微信支付、时段工具
│
├── smart-pojo/                    # 数据对象
│   ├── entity/                    # 数据库实体
│   ├── dto/                       # 请求参数
│   ├── vo/                        # 响应对象
│   └── event/                     # 订单事件、延迟消息
│
├── smart-server/                  # 业务模块
│   ├── controller/
│   │   ├── admin/                 # 管理端
│   │   ├── employee/              # 员工端
│   │   ├── worker/                # 家政员端
│   │   └── user/                  # 用户端
│   ├── service/ + service/impl/   # 业务逻辑
│   ├── mapper/                    # MyBatis Mapper
│   ├── config/                    # Security、WebMvc、Redis、RabbitMQ、OSS
│   ├── interceptor/               # 三套 JWT 拦截器
│   ├── aspect/                    # 角色校验、公共字段填充
│   ├── websocket/                 # 订单提醒 WebSocket + 握手鉴权
│   ├── mq/                        # 订单事件发布、通知/延迟消费者
│   ├── task/                      # OrderTask、优惠券对账、mq_outbox 发送
│   └── resources/
│       ├── mapper/*.xml
│       ├── lua/seckill.lua
│       ├── application.yml
│       └── application-dev.yml.example
│
├── db/                            # 建表脚本（00 → 33 按编号执行）
│   ├── 00_create_database.sql
│   ├── 01~33_*.sql
│   ├── migrate/                   # 老库增量升级脚本（新库不要执行）
│   └── backup/                    # 本机数据快照（不入库）
│
├── miniapp/                       # 微信小程序（用户端 + 家政员端，原生开发）
│   ├── pages/                     # 31 个页面
│   ├── components/                # 自定义导航栏、TabBar
│   ├── utils/                     # request.js、util.js
│   ├── app.js                     # 全局配置（8080 / 5000 地址）
│   ├── app.json
│   └── app.wxss
│
├── web-admin/                     # Web 管理端（Vue 3 + Vite + TS）
│   ├── src/
│   │   ├── api/                   # 接口封装（19 个模块）
│   │   ├── views/                 # 登录、员工、服务、订单、统计等页面
│   │   ├── router/                # 路由
│   │   ├── utils/                 # request、WebSocket 提醒
│   │   └── main.ts
│   ├── vite.config.ts
│   └── package.json
│
├── upload/                        # 本地上传目录映射
├── API接口文档.md                  # 166 个 HTTP 接口说明
├── pom.xml
└── README.md
```

## 核心功能

### 1. 三端登录与鉴权

| 端 | 账号来源 | 令牌 | 请求头 |
|---|---|---|---|
| 管理端 | `administrator` 表 | admin JWT | `token` |
| 员工端 / 家政员端 | `employee` 表 | employee JWT | `token` |
| 用户端 | 微信 code 换 openid | user JWT | `token` |

- `JwtTokenAdminInterceptor`：`/admin/**`
- `JwtTokenEmployeeInterceptor`：`/employee/**`、`/worker/**`
- `JwtTokenUserInterceptor`：`/user/**`
- `@RoleCheck`：在员工内部分角色（`admin` / `manager` / `staff` / `cleaner`）

### 2. 家政业务

- 员工、员工详情、家政员档案与等级
- 服务分类、服务项目、服务套餐、服务需求项
- 家政员档期：可约/占用、批量设置、下单抢位、取消释放
- 订单：下单、支付、指派、接单、到达、完成、拒单、取消、退款
- 购物车、地址簿、优惠券（普通领取 + 秒杀）
- 动态、关于我们、服务介绍、服务推荐、资讯
- 管理端统计报表、员工工作台

### 3. 订单状态机

| 状态 | 含义 |
|---|---|
| 1 | 待付款 |
| 2 | 待接单 |
| 3 | 已接单 |
| 4 | 服务中 |
| 5 | 已完成 |
| 6 | 待退款 |
| 7 | 已取消 |

关键流转：

```
1 待付款 ──支付──▶ 2 待接单 ──指派/接单──▶ 3 已接单
   │                  │                      │
   │ 15 分钟未支付     │ 2 小时前仍无人接单      │ 到达
   ▼                  ▼                      ▼
7 已取消           6 待退款                4 服务中 ──完成──▶ 5 已完成
```

### 4. RabbitMQ 异步事件与通知

订单状态变更不再同步写通知，而是：

```
业务事务 → mq_outbox（本地消息表）
        → Spring Task 每 3 秒发送
        → order.events（topic exchange）
        → order.notify.queue → 通知消费者
        → order_notify / user_notify + WebSocket 推送
```

事件类型：

- `order.created`、`order.paid`、`order.assigned`
- `order.accepted`、`order.arrived`、`order.completed`
- `order.cancelled`、`order.refunded`

延迟任务（TTL + 死信队列）：

| 场景 | 规则 |
|---|---|
| 未支付自动取消 | 下单 15 分钟后仍未支付 |
| 指派未确认提醒 | 指派 30 分钟后仍未接单 |
| 无人接单转退款 | 距服务开始不足 2 小时仍未接单 |

`OrderTask` 保留每分钟巡检，作为 RabbitMQ 不可用时的兜底；所有状态更新均使用 CAS，重复触发不会重复执行。

### 5. WebSocket 实时提醒

- 地址：`ws://{host}:8080/ws/order-notify`
- 可选 `?token={JWT}`：
  - 员工令牌 → 按 `empId` 分组
  - 用户令牌 → 按 `userId` 分组，只收自己的通知
  - 不带 token → 按旧版员工端处理，接收员工端广播
- 消息类型：`newOrder`、`assign`、`accept`、`acceptRemind`、`reject`、`refund` 等

### 6. 缓存、锁与秒杀

- Redis 缓存：服务、分类、家政员、订单详情等，默认 TTL 30 分钟，部分缓存带随机抖动防雪崩
- Redisson 分布式锁：优惠券一人一单
- Redis + Lua：秒杀库存预扣，DB 唯一索引兜底

## 技术栈

| 类别 | 技术 |
|---|---|
| 语言 / 框架 | Java 17、Spring Boot 2.7.3 |
| 持久层 | MyBatis 2.2.0、PageHelper、Druid、MySQL 8 |
| 缓存 / 锁 | Spring Data Redis、Redisson、Redis Lua |
| 消息 | RabbitMQ（spring-boot-starter-amqp） |
| 实时通信 | Spring WebSocket |
| 认证 | JWT（jjwt）、BCrypt |
| 文件 | 阿里云 OSS |
| 报表 | Apache POI（依赖已引入） |
| Web 前端 | Vue 3.5、Vite 8、TypeScript、Element Plus、Vue Router、Axios |
| 小程序 | 微信小程序原生（WXML / WXSS / JS） |
| 构建 | Maven 多模块、npm / Vite |

## 快速开始

### 1. 环境要求

| 组件 | 版本 / 地址 |
|---|---|
| JDK | 17 |
| Maven | 3.8+ |
| MySQL | 8.x，`localhost:3306` |
| Redis | 6/7，`localhost:6379` |
| RabbitMQ | 4.x，`localhost:5672`（管理页 15672） |

### 2. 初始化数据库

按编号顺序执行 `db/` 下的脚本：

```bash
cd db
for f in [0-9][0-9]_*.sql; do echo ">> $f"; mysql -uroot -p --default-character-set=utf8mb4 < "$f" || break; done
```

- 新库只跑 `db/`，不要跑 `db/migrate/`
- `db/backup/` 是本机数据快照，不要执行
- `33_mq_outbox.sql` 是 RabbitMQ 本地消息表

### 3. 配置

复制配置示例：

```bash
cp smart-server/src/main/resources/application-dev.yml.example \
   smart-server/src/main/resources/application-dev.yml
```

填写：

- MySQL 数据库账号密码
- Redis 地址（默认 `localhost:6379`，db 10）
- RabbitMQ 地址与账号
- 阿里云 OSS（上传功能）
- 微信小程序 AppId / Secret（用户登录）

`application-dev.yml` 已被 `.gitignore` 忽略，不会提交到 Git。

### 4. 启动

IDEA 中运行 `smart-server/src/main/java/com/smart/SmartApplication.java`，或：

```bash
mvn -pl smart-server -am spring-boot:run
```

启动后：

- 后端接口：`http://localhost:8080`
- Web 管理端：`web-admin/`，`npm run dev`
- 微信小程序：`miniapp/`，用微信开发者工具导入
- RabbitMQ 管理页：`http://localhost:15672`

### 5. RabbitMQ 说明

后端启动时会自动创建：

- 交换机：`order.events`、`order.dlx`、`order.delay.exchange`、`order.delay.process.exchange`
- 队列：`order.notify.queue`、`order.stats.queue`、`order.cache.queue`、`order.dlq`
- 延迟队列：`order.delay.15m.queue`、`order.delay.5m.queue`、`order.delay.process.queue`

RabbitMQ 未启动时应用仍可启动，但订单通知不会生成，延迟动作由 `OrderTask` 兜底。

## 前端项目

仓库同时包含 Web 管理端和微信小程序源码，均直接对接后端 8080。

### 1. Web 管理端 `web-admin/`

**技术栈**：Vue 3.5、Vite 8、TypeScript、Element Plus、Vue Router、Axios

**页面功能**：

| 分组 | 页面 |
|---|---|
| 登录与首页 | 登录、首页、数据看板（Dashboard） |
| 员工 | 员工管理、员工详情、员工工作台 |
| 家政员 | 家政员管理、档期管理 |
| 服务 | 分类管理、服务项目、服务套餐 |
| 交易 | 订单管理、优惠券管理 |
| 内容 | 动态、关于我们、服务介绍、服务推荐 |
| AI | 聊天助手 |
| 实时提醒 | WebSocket 来单/催单提醒、消息中心未读角标、语音播报 |

**启动**：

```bash
cd web-admin
npm install
npm run dev
```

- 默认访问：`http://localhost:5173`（端口被占用时 Vite 会自动顺延）
- 接口地址：`src/utils/request.ts` 中 `baseURL = http://localhost:8080`
- 生产构建：`npm run build`，产物在 `web-admin/dist/`

### 2. 微信小程序 `miniapp/`

**技术栈**：微信小程序原生（WXML / WXSS / JS），共 31 个页面。

**用户端**（底部 Tab：首页、家政预约、预约清单、我的）：

- 首页、服务介绍、服务详情、家政预约、家政员详情
- 预约清单、订单确认、支付、支付成功、历史订单、预约记录
- 地址管理、优惠券、消息通知、个人中心
- **智能客服** `customer-service`：对接 AI 服务 5000
- **智能搜索** `smart-search`：对接 AI 搜索服务 5001

**家政员端**：

- 登录、工作台、我的订单、订单详情、拒单、个人资料

**运行步骤**：

1. 微信开发者工具 → 导入项目，选择 `miniapp/`
2. 填写 AppID（仓库中为 `wxebf57cb83d999649`，可替换为自己的）
3. 详情 → 本地设置 → 勾选「不校验合法域名」
4. 地址配置：
   - `utils/request.js` 的 `BASE_URL`（业务接口 8080）：
     - 开发者工具：改成 `http://localhost:8080`
     - 真机：`http://192.168.137.1:8080`（电脑热点地址，仓库默认值）
   - `app.js` 的 `aiBaseUrl`（AI 客服 5000）：
     - 开发者工具：`http://127.0.0.1:5000`
     - 真机：`http://192.168.137.1:5000`（电脑热点地址）
   - 智能搜索页会自动从 `aiBaseUrl` 推导 5001，不需要单独写死
5. 真机调试：手机连接电脑热点，Windows 防火墙放行 8080、5000、5001

### 3. 联调顺序

1. 启动 MySQL、Redis、RabbitMQ
2. 启动后端 `SmartApplication`（8080）
3. 启动 Web 管理端或小程序
4. 测试订单通知：确认 `order.notify.queue` 有 1 个消费者
5. 测试智能搜索：额外启动 AI 服务 5001 和 Qdrant

## API 概览

完整接口说明见 [API接口文档.md](./API接口文档.md)，共 166 个接口。

| 前缀 | 端 | 主要功能 |
|---|---|---|
| `/admin/auth`、`/admin/employee` | 管理端 | 登录、员工、角色 |
| `/admin/category`、`/admin/service-*` | 管理端 | 分类、服务、套餐 |
| `/admin/housekeeper`、`/admin/statistics` | 管理端 | 家政员、统计报表 |
| `/admin/about`、`/admin/hot-picks`、`/admin/updates` | 管理端 | 内容管理 |
| `/employee/order`、`/employee/service-*` | 员工端 | 订单、服务、套餐 |
| `/employee/housekeeper-schedule` | 员工端 | 档期管理 |
| `/employee/voucher`、`/employee/workbench` | 员工端 | 优惠券、工作台 |
| `/worker/auth`、`/worker/order`、`/worker/profile` | 家政员端 | 登录、订单、资料 |
| `/user/user`、`/user/order`、`/user/shopping-cart` | 用户端 | 登录、订单、购物车 |
| `/user/address-book`、`/user/voucher`、`/user/notify` | 用户端 | 地址、优惠券、消息 |

## 相关文档

- [API接口文档.md](./API接口文档.md) — HTTP 接口、参数、返回示例，含异步通知说明
- [db/README.md](./db/README.md) — 建表脚本说明

## 注意事项

- 生产环境请替换 `application.yml` 中的 JWT 密钥，并保证三端密钥策略符合部署要求
- `application-dev.yml`、`db/backup/`、`upload/` 中的本机数据不要提交到 Git
- 修改数据库结构后，同时更新 `db/` 脚本和 `API接口文档.md`
- 订单通知依赖 RabbitMQ；排查“前端收不到提醒”时，先看 `order.notify.queue` 的消费者数量和 `mq_outbox.status`
