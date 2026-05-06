# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.5.3 backend for a CRM / Project Lifecycle Management System (项目全生命周期管理系统). Java 17, MySQL, MyBatis Plus, JWT authentication.

## Build & Run Commands

```bash
./mvnw clean package          # Build
./mvnw spring-boot:run        # Run (localhost:8080)
./mvnw test                   # Run all tests
./mvnw test -Dtest=ClassName  # Run a single test class
./mvnw com.ly.smart-doc:smart-doc-maven-plugin:html  # Generate API docs
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## Architecture

Standard layered Spring Boot architecture: **Controller → Service → Mapper → MySQL**

- **Authentication**: JWT-based stateless auth via Spring Security. `JwtAuthenticationFilter` intercepts requests, `JwtUtil` handles token operations, passwords use BCrypt.
- **Authorization**: Multi-role per user, loaded from `sys_user_role` join `sys_role` into `CustomUserDetails.authorities` as `SimpleGrantedAuthority("ROLE_" + role_code)`. Two layers of enforcement:
  1. **URL-level (粗粒度)**: Configured in `SecurityConfig.securityFilterChain()` (e.g. `/admin/**` → `hasRole('ADMIN')`)
  2. **Method-level (细粒度)**: `@EnableMethodSecurity` is on, use `@PreAuthorize("hasRole('XXX')")` on Controller methods
- **API responses**: All endpoints return `Result<T>` (util/Result.java) as the unified response wrapper.
- **ORM**: MyBatis Plus with annotation-based table mapping (`@TableName`, `@TableId`). Mappers extend `BaseMapper<T>`.
- **Config**: `SecurityConfig` defines endpoint security rules + RBAC, `MybatisPlusConfig` sets up pagination interceptor.

## Domain Model

CRM domain pipeline: **Leads (`CrmLead`) → Opportunities (`CrmOpportunity`) → Contracts (`CrmContract`)**

| Aggregate | Entity classes |
|---|---|
| Identity | `SysUser`, `SysRole`, `SysUserRole` (多角色关系) |
| Customer | `CrmCustomer`, `CrmCustomerVisit` |
| Lead | `CrmLead` |
| Opportunity | `CrmOpportunity`, `CrmOppTemplate`, `CrmOppTemplateStage`, `CrmOppStage`, `CrmOppTeam`, `CrmOppTask` |
| Contract | `CrmContract`, `CrmContractPayment` |
| Reports | `CrmWeeklyReport`, `CrmWeeklyReportComment` |

**派生字段**: `CrmCustomer` 没有 `stage` 列;前端"已录入/已派单/走访中/走访完成"通过 left join `crm_customer_visit` 拿到的 `visit.status` 派生(见 `CustomerListItemDto.visitStatus`)。同一 customer 在 `crm_customer_visit` 至多一条记录(Service 层强制),因此 LEFT JOIN 不会膨胀行数。

DDL truth source: `src/main/resources/sql/schema.sql`. Field types/comments in entities **must** stay in sync — see top-level `docs/需求分析与数据库设计.md` for the contract.

## REST Endpoints (已实现)

| Path | Method | Role | 说明 |
|---|---|---|---|
| `/auth/login` / `/auth/register` / `/auth/logout` | POST | public | 登录/注册/登出 |
| `/auth/me` | GET | authenticated | 当前用户信息(roles[]) |
| `/admin/users` | GET/POST/PUT/PATCH | ADMIN | 用户管理 |
| `/users/customer-managers` | GET | OPP_ADMIN/ADMIN | 全部 CUSTOMER_MANAGER 用户(派单/分发选择用) |
| `/users/project-managers` | GET | authenticated | 全部 PROJECT_MANAGER 用户(线索培育选商机负责人) |
| `/customers` | GET | authenticated | 客户列表(含 visitStatus 派生) |
| `/customers/{id}` | GET | authenticated | 客户详情(含 visitStatus 派生) |
| `/customers` | POST | OPP_ADMIN | 新增客户 |
| `/customers/{id}` | PUT | OPP_ADMIN/CUSTOMER_MANAGER | 更新客户 |
| `/customer-visits` | POST | OPP_ADMIN | 派单(同客户唯一) |
| `/customer-visits/my` | GET | CUSTOMER_MANAGER | 我的走访清单 |
| `/customer-visits/{id}` | GET | authenticated | 走访详情 |
| `/customer-visits/{id}/record` | PUT | CUSTOMER_MANAGER | 暂存走访记录 |
| `/customer-visits/{id}/complete` | POST | CUSTOMER_MANAGER | 完成走访(不可逆) |
| `/leads` | POST | authenticated | 录入线索(任何已登录用户) |
| `/leads` | GET | authenticated | 线索清单(filter=all/mine/participate/todo) |
| `/leads/{id}` | GET | authenticated | 线索详情(含 cultivateInfo JSON 解析) |
| `/leads/{id}` | PUT | authenticated | 临时保存(entry_by 本人 + ENTRY) |
| `/leads/{id}/collect` | POST | authenticated | 确认收集(entry_by 本人 + ENTRY → COLLECTED) |
| `/leads/{id}/distribute` | POST | OPP_ADMIN | 分发到客户经理(COLLECTED → DISTRIBUTED) |
| `/leads/{id}/cultivate` | PUT | CUSTOMER_MANAGER | 培育(manager_id 本人 + DISTRIBUTED, 可多次) |
| `/leads/{id}/convert` | POST | CUSTOMER_MANAGER | 转商机(manager_id 本人 + DISTRIBUTED → CONVERTED, 创建 crm_opportunity 占位) |
| `/opportunities` | GET | authenticated | 商机列表(filter: keyword/customerId/stage) |
| `/opportunities/{id}` | GET | authenticated | 商机详情(含 stages[] + tasks[]) |
| `/opportunities/{id}/apply-template` | POST | PM(opp.pm_id 本人) | 选模板生成 4 条 stage 实例;已有任务时禁止 |
| `/opportunities/stages/{stageId}/owner` | PUT | PM 或 当前 owner | 设置环节责任人 |
| `/opportunities/{id}/advance-stage` | POST | PM | 推进商机阶段(VALIDATE→NEGOTIATE→IMPLEMENT→DELIVERY,顺序) |
| `/opp-team` | GET | authenticated | 商机组员列表(query: oppId) |
| `/opp-team` | POST | CORE 仅 PM / SUPPORT 铁三角 | 添加组员 |
| `/opp-team/{id}` | DELETE | CORE 仅 PM / SUPPORT 铁三角 | 移除组员 |
| `/users/by-role/{roleCode}` | GET | authenticated | 按角色 code 查用户(组员添加用) |
| `/opp-tasks` | GET | authenticated | 任务列表(filter: oppId/stageId) |
| `/opp-tasks` | POST | 铁三角(PM/SM/DM/manager) | 创建任务 |
| `/opp-tasks/{id}` | PUT | 铁三角 + 未关闭 | 更新任务 |
| `/opp-tasks/{id}/reply` | POST | 受理人或铁三角 + 未关闭 | 任务回复 |
| `/opp-tasks/{id}/progress` | PUT | 受理人或铁三角 + 未关闭 | 更新任务进度 0-100 |
| `/opp-tasks/{id}/close` | POST | 铁三角 + 未关闭 | 关闭任务(不可逆,自动 progress=100) |
| `/opp-templates` | GET | authenticated | 全部商机模板(含环节定义) |
| `/contracts` | GET | authenticated | 合同列表(query: keyword/customerName/status/year/bu) |
| `/contracts/{id}` | GET | authenticated | 合同详情(含 payments[]) |
| `/contracts` | POST | OPP_ADMIN | 新建合同(校验合同名唯一 + 总额=节点总和) |
| `/contracts/{id}/close` | POST | OPP_ADMIN | 关闭合同(仅 EXECUTING; COMPLETED 拒绝) |
| `/contracts/{id}/delivery` | PUT | OPP_ADMIN | 标记验收时间(仅 EXECUTING);若全部 payments 已付,自动 EXECUTING → COMPLETED |
| `/contracts/in-flight` | GET | REGION_HEAD/OPP_ADMIN/ADMIN | 在途合同专题(status=EXECUTING) |
| `/contracts/accepted` | GET | PMO/OPP_ADMIN/ADMIN | 已验收项目专题(status=COMPLETED) |
| `/contract-payments/{id}/pay` | PUT | OPP_ADMIN | 标记付款节点已付(actual_amount + pay_time) |
| `/weekly-reports/my` | GET | SALES | 我的周报列表 |
| `/weekly-reports/team` | GET | SUPERVISOR | 下属周报列表(supervisor_id == self) |
| `/weekly-reports/team/summary` | GET | SUPERVISOR | 主管 dashboard:某周下属提交/点评聚合(query: year/weekNum) |
| `/weekly-reports/{id}` | GET | authenticated | 周报详情(含 comments[]) |
| `/weekly-reports/draft` | POST | SALES | 新建/更新草稿(DRAFT 可改;SUBMITTED 拒绝) |
| `/weekly-reports/{id}/submit` | POST | SALES | 提交(DRAFT → SUBMITTED, 锁定) |
| `/weekly-reports/{id}/comment` | POST | SUPERVISOR | 主管点评(SUBMITTED → COMMENTED) |
| `/files/upload` | POST | OPP_ADMIN | 上传文件(multipart, ≤20MB),返 `{url, originalName, size}` |
| `/uploads/**` | GET | public | 静态资源(已上传文件下载/预览,通过 WebMvcConfig 映射本地 `uploads/` 目录) |

## Authentication / Authorization Flow

1. `POST /auth/login` → `AuthController.login()` calls `AuthenticationManager.authenticate()` → triggers `CustomUserDetailsService.loadUserByUsername()`
2. `CustomUserDetailsService` does:
   - `userMapper.findByUsername()` → load `SysUser`
   - `userRoleMapper.selectRoleCodesByUserId()` → all role codes from `sys_user_role` join `sys_role`
   - Append `USER` if missing (兜底)
   - Wrap each into `SimpleGrantedAuthority("ROLE_" + code)`
   - Return `CustomUserDetails(sysUser, roleCodes, authorities)`
3. On success: `JwtUtil.generateToken(username)` → token, `LoginResponse{token, userId, username, realName, roleId, roles[]}`
4. Subsequent requests: `JwtAuthenticationFilter` extracts JWT → re-runs `loadUserByUsername` to refresh authorities → fills `SecurityContext`
5. `GET /auth/me` (used by frontend `fetchMe()`): returns the same `LoginResponse` shape minus token.

## Key Dependencies

MyBatis Plus 3.5.7, JJWT 0.11.5, Apache POI 5.4.0 (Excel import/export), Lombok, Spring Boot Actuator, Druid 1.2.23.

## Database

MySQL database `projectlife` on localhost:3306. Connection pool: Druid. Config in `application.yml`. Run `schema.sql` once to bootstrap; it will drop+recreate all 16 tables and seed roles + 10 demo users (password `123456`).
