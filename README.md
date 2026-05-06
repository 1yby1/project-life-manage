# 项目全生命周期管理系统

> 面向 To-B 销售/项目交付场景的 CRM + 项目全生命周期管理系统,覆盖 **客户 → 线索 → 商机 → 合同** 完整漏斗 + 周报 + 专题 dashboard。

## 技术栈

- **后端**:Spring Boot 3.5.3 + Java 17 + MyBatis Plus 3.5.7 + Spring Security + JJWT 0.11.5 + MySQL 8 + Druid
- **前端**:Vue 3 + Vue Router 4 + Element Plus 2.13 + TypeScript 4.5 + Vite 6
- **部署**:Docker Compose(MySQL + 后端 + 前端 三服务一键启动)

## 5 分钟一键启动(Docker)

前提:已装 [Docker Desktop](https://docs.docker.com/get-docker/)(Windows / macOS) 或 docker + docker-compose(Linux)。

```bash
git clone <repo-url> projectlife
cd projectlife

docker compose up -d              # 首次约 3-5 分钟(构建镜像 + 拉 MySQL + 初始化 DB)
docker compose logs -f backend    # 看后端启动日志,直到 "Started BackendApplication"
```

打开浏览器访问 **http://localhost:8081** ,用下面任一种子账号登录:

| 用户名 | 密码 | 主要角色 | 适合体验 |
|---|---|---|---|
| `admin` | `123456` | ADMIN | 用户/角色/模板管理 |
| `oppadmin` | `123456` | OPP_ADMIN | 客户录入、派单、合同 |
| `cm001` | `123456` | CUSTOMER_MANAGER | 走访客户、线索培育 |
| `sales001` | `123456` | SALES | 提交周报、录入线索 |
| `sup001` | `123456` | SUPERVISOR | 周报点评 + 主管 dashboard |
| `pm001` / `sm001` / `dm001` | `123456` | 铁三角 | 商机管理 |
| `region01` | `123456` | REGION_HEAD | 在途合同专题 |
| `pmo01` | `123456` | PMO | 已验收项目专题 |

### 端口

| 服务 | 主机端口 | 用途 |
|---|---|---|
| `frontend` (nginx) | **8081** | 前端 + `/api/*` 反代 |
| `backend` (Spring Boot) | 8080 | 调试/直连后端 |
| `mysql` | 3306 | mysql client 直连(root / 123456) |

### 常用命令

```bash
docker compose up -d                # 启动
docker compose down                 # 停服(数据保留在 named volume)
docker compose down -v              # 停服并清空数据(重置演示数据)
docker compose logs -f backend      # 查后端日志
docker compose restart backend      # 重启后端(代码改动后需重新 build)
docker compose build backend        # 仅重新构建后端镜像
docker compose exec mysql mysql -uroot -p123456 projectlife   # 进 MySQL CLI
```

## 漏斗演示 — 一遍跑通客户到合同

按下面顺序操作(依次切换登录账号),即可端到端体验完整 CRM 漏斗:

1. **`oppadmin`** 客户管理 → 新建客户 → 派单到 `cm001`(列表显示"已派单")
2. **`cm001`** 客户接触 → 选客户走访 → 暂存(状态 → "走访中") → 完成走访(状态 → "走访完成") → 完善信息
3. **`sales001`** 线索管理 → 录入线索 → 自己确认收集(状态 → "分发")
4. **`oppadmin`** 线索管理 → 分发到 `cm001`(状态 → "培育")
5. **`cm001`** 线索管理 → 培育填全 7 字段(选 `pm001` 为商机负责人)→ 转商机 → 自动跳到商机列表
6. **`pm001`** 商机管理 → 选模板 → 4 个环节自动展开
7. **`pm001`** 商机详情 → 任务管理 → 创建关键任务并指派给 `sm001`
8. **`pm001`** 顶部"推进到下一阶段"→ 商机阶段 VALIDATE → NEGOTIATE
9. **`oppadmin`** 合同管理 → 创建合同(关联该客户,填付款节点,上传 PDF 附件)
10. **`oppadmin`** 合同列表 → 标记付款节点已付 + "标记验收"→ 全部满足时合同**自动**完成(EXECUTING → COMPLETED)
11. **`pmo01`** 已验收项目专题 → 看到这条合同
12. **`region01`** 在途合同专题 → 看到所有 EXECUTING 中的合同
13. **`sales001`** 提交周报 → **`sup001`** 下属周报点评(顶部 dashboard 显示提交率/点评率)

## 目录结构

```
.
├── backend/               # Spring Boot 后端
│   ├── src/main/java/     # Java 源码(controller / service / mapper / model)
│   ├── src/main/resources/sql/schema.sql   # 16 张表 DDL + 种子数据
│   ├── Dockerfile
│   └── CLAUDE.md          # 后端架构说明 + REST endpoints 全表
├── frontend/              # Vue 3 前端
│   ├── src/api/           # 真实 API 封装
│   ├── src/views/         # 页面组件(按模块分目录)
│   ├── Dockerfile + nginx.conf
│   └── CLAUDE.md          # 前端结构说明 + 路由守卫 + 模块对接进度
├── docs/
│   └── 需求分析与数据库设计.md   # 完整需求 + ER + 状态机 + 业务规则 + 11 轮迭代摘要
├── docker-compose.yml
└── README.md(本文件)
```

## 文档索引

- [docs/需求分析与数据库设计.md](docs/需求分析与数据库设计.md) — **从这里开始**:需求文档 + 数据库设计 + 11 轮迭代摘要 + 24 条业务规则
- [backend/CLAUDE.md](backend/CLAUDE.md) — 后端架构、JWT 鉴权、REST endpoints 全表、Authentication / Authorization Flow
- [frontend/CLAUDE.md](frontend/CLAUDE.md) — 前端目录、路由守卫、HTTP 客户端约定、模块对接矩阵

## 开发模式(不用 Docker)

```bash
# 1. 启动 MySQL,执行 schema.sql 初始化 DB
mysql -u root -p < backend/src/main/resources/sql/schema.sql

# 2. 后端
cd backend && ./mvnw spring-boot:run     # http://localhost:8080

# 3. 前端
cd frontend && npm install && npm run dev   # http://localhost:8081(vite proxy /api → 8080)
```

## 核心特性

| 模块 | 状态 | 说明 |
|---|---|---|
| 鉴权 + 多角色 RBAC | ✅ | JWT + `ROLE_*` 权限,11 个内置角色,`@PreAuthorize` 方法级粗细粒度 |
| 客户管理(录入/派单/走访/完善) | ✅ | 同客户唯一派单;走访不可逆 |
| 线索管理 | ✅ | 4 态状态机 ENTRY → COLLECTED → DISTRIBUTED → CONVERTED |
| 商机管理(模板/环节/任务/组员/阶段) | ✅ | 选模板生 4 环节;CORE 仅 PM、SUPPORT 铁三角;阶段 VALIDATE→DELIVERY 顺序推进;任务进度独立列 |
| 合同管理(CRUD/关闭/付款/附件) | ✅ | 合同名唯一、金额=节点和;附件本地上传 ≤20MB;EXECUTING + 全付 + 已验收 → 自动 COMPLETED |
| 周报(提交/点评/主管 dashboard) | ✅ | DRAFT 可改、SUBMITTED 锁;主管 dashboard 显示本周提交/点评率 + 未提交清单 |
| 专题查询 | ✅ | 在途合同(REGION_HEAD)/ 已验收项目(PMO),按年/BU 过滤 |

## 业务规则总览

24 条业务规则详见 [docs/需求分析与数据库设计.md 第八节](docs/需求分析与数据库设计.md)。最关键的 4 条:

1. **越权拦截** — 未登录跳 `/login`,角色不足 → 403
2. **不可逆操作** — 走访完成 / 任务关闭 / 已交付合同 一旦达到终态,所有写操作直接拒绝
3. **状态机顺序** — 商机阶段必须按 VALIDATE→NEGOTIATE→IMPLEMENT→DELIVERY 顺序推进
4. **金额一致性** — `crm_contract.total_amount` 必须严格等于 `SUM(crm_contract_payment.plan_amount)`

## 迭代摘要

11 轮迭代(2026/04/30 - 2026/05/02):

1. 地基(Entity / Mapper / SQL / 鉴权链路 / 多角色 RBAC)
2. 客户走访模块端到端
3. 线索状态机端到端
4. 商机管理 MVP(模板 + 任务派发回复关闭)
5. 合同 + 周报 + 2 专题
6. mockStore 收尾(全项目纯后端)
7. 商机扩展(组员管理 + 阶段推进)
8. 任务进度独立 INT 列
9. 合同附件上传(本地存储)
10. 合同自然完成(双条件触发)
11. 周报主管 dashboard

完整迭代记录 + 决策原因见 [docs/需求分析与数据库设计.md 第九节](docs/需求分析与数据库设计.md)。

## License

仅供学习使用。
