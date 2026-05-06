-- ============================================================================
-- 项目全生命周期管理系统 - 数据库初始化脚本
-- Database: projectlife (MySQL 8.0+)
-- Charset: utf8mb4
-- ============================================================================

CREATE DATABASE IF NOT EXISTS projectlife
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE projectlife;

-- 删除顺序: 先子表后父表
DROP TABLE IF EXISTS crm_weekly_report_comment;
DROP TABLE IF EXISTS crm_weekly_report;
DROP TABLE IF EXISTS crm_contract_payment;
DROP TABLE IF EXISTS crm_contract;
DROP TABLE IF EXISTS crm_opp_task;
DROP TABLE IF EXISTS crm_opp_team;
DROP TABLE IF EXISTS crm_opp_stage;
DROP TABLE IF EXISTS crm_opp_template_stage;
DROP TABLE IF EXISTS crm_opp_template;
DROP TABLE IF EXISTS crm_opportunity;
DROP TABLE IF EXISTS crm_lead;
DROP TABLE IF EXISTS crm_customer_visit;
DROP TABLE IF EXISTS crm_customer;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

-- ============================================================================
-- 1. 用户与权限
-- ============================================================================

CREATE TABLE sys_user (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE              COMMENT '登录名',
    password      VARCHAR(100) NOT NULL                     COMMENT 'BCrypt 加密密码',
    real_name     VARCHAR(50)                               COMMENT '真实姓名',
    phone         VARCHAR(20)                               COMMENT '手机号',
    email         VARCHAR(100)                              COMMENT '邮箱',
    supervisor_id BIGINT                                    COMMENT '直属主管 ID,周报点评用',
    role_id       BIGINT                                    COMMENT '主角色 ID(兼容旧代码,实际权限以 sys_user_role 为准)',
    status        TINYINT      NOT NULL DEFAULT 1           COMMENT '1正常 0禁用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supervisor (supervisor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE sys_role (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    role_name   VARCHAR(50) NOT NULL                        COMMENT '角色名称',
    role_code   VARCHAR(50) NOT NULL UNIQUE                 COMMENT '角色编码',
    description VARCHAR(200)                                COMMENT '角色说明'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE sys_user_role (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关系';

-- ============================================================================
-- 2. 客户模块
-- ============================================================================

CREATE TABLE crm_customer (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL                     COMMENT '客户名称(必填)',
    city          VARCHAR(50)  NOT NULL                     COMMENT '地市(必填)',
    legal_person  VARCHAR(50)  NOT NULL                     COMMENT '法人(必填)',
    address       VARCHAR(200)                              COMMENT '公司地址',
    reg_address   VARCHAR(200)                              COMMENT '注册地址',
    reg_agency    VARCHAR(100)                              COMMENT '注册机构',
    credit_code   VARCHAR(50)                               COMMENT '统一社会信用编码',
    industry      VARCHAR(50)                               COMMENT '所属行业',
    contact_name  VARCHAR(50)                               COMMENT '联系人姓名',
    contact_phone VARCHAR(20)                               COMMENT '联系电话',
    contact_title VARCHAR(50)                               COMMENT '联系人职位',
    bu            VARCHAR(50)                               COMMENT '所属业务单元',
    status        TINYINT      NOT NULL DEFAULT 1           COMMENT '1有效 0失效',
    create_by     BIGINT       NOT NULL                     COMMENT '创建人(商机管理员)',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (customer_name),
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

CREATE TABLE crm_customer_visit (
    id           BIGINT      PRIMARY KEY AUTO_INCREMENT,
    customer_id  BIGINT      NOT NULL                       COMMENT '客户 ID',
    manager_id   BIGINT      NOT NULL                       COMMENT '被派单的客户经理',
    assign_by    BIGINT      NOT NULL                       COMMENT '派单人(商机管理员)',
    visit_record TEXT                                        COMMENT '走访记录,走访完成前可多次更新',
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING'     COMMENT 'PENDING待走访 / DOING走访中 / COMPLETED已完成(不可逆)',
    assign_time  DATETIME                                    COMMENT '派单时间',
    visit_time   DATETIME                                    COMMENT '完成走访时间',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer (customer_id),
    INDEX idx_manager (manager_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户走访(兼任派单记录)';

-- ============================================================================
-- 3. 线索模块
-- ============================================================================

CREATE TABLE crm_lead (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT,
    title          VARCHAR(200) NOT NULL                    COMMENT '线索标题',
    status         VARCHAR(20)  NOT NULL DEFAULT 'ENTRY'    COMMENT 'ENTRY录入 / COLLECTED收集完成 / DISTRIBUTED已分发 / NURTURING培育中 / CONVERTED已转商机',
    customer_id    BIGINT                                    COMMENT '关联客户 ID(培育阶段可关联)',
    win_rate       DECIMAL(5,2)                              COMMENT '签约概率 0-100',
    requirement    TEXT                                      COMMENT '客户需求',
    project_scale  VARCHAR(50)                               COMMENT '项目规模',
    progress_desc  TEXT                                      COMMENT '进展描述',
    bu             VARCHAR(50)                               COMMENT '所属 BU,清单可按 BU 过滤',
    entry_by       BIGINT       NOT NULL                    COMMENT '录入人',
    collector_by   BIGINT                                    COMMENT '收集人',
    distributor_by BIGINT                                    COMMENT '分发人',
    manager_id     BIGINT                                    COMMENT '被指派的客户经理',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_customer (customer_id),
    INDEX idx_bu (bu),
    INDEX idx_entry (entry_by),
    INDEX idx_collector (collector_by),
    INDEX idx_distributor (distributor_by),
    INDEX idx_manager (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索表';

-- ============================================================================
-- 4. 商机模块
-- ============================================================================

CREATE TABLE crm_opp_template (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    template_name VARCHAR(100) NOT NULL                     COMMENT '模板名称',
    description   VARCHAR(500)                              COMMENT '模板说明',
    is_default    TINYINT      NOT NULL DEFAULT 0           COMMENT '是否默认模板',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机推进模板';

CREATE TABLE crm_opp_template_stage (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT      NOT NULL,
    stage_code  VARCHAR(20) NOT NULL                        COMMENT '环节编码: VALIDATE/NEGOTIATE/IMPLEMENT/DELIVERY 或自定义',
    stage_name  VARCHAR(50) NOT NULL                        COMMENT '环节名称',
    sort_order  INT         NOT NULL                        COMMENT '顺序',
    required    TINYINT     NOT NULL DEFAULT 0              COMMENT '是否必含环节(验证/谈判/实施/交付为必含)',
    INDEX idx_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板环节定义';

CREATE TABLE crm_opportunity (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    lead_id     BIGINT                                      COMMENT '来源线索 ID',
    customer_id BIGINT       NOT NULL                       COMMENT '客户 ID',
    opp_name    VARCHAR(200) NOT NULL                       COMMENT '商机名称',
    stage       VARCHAR(20)  NOT NULL DEFAULT 'VALIDATE'    COMMENT 'VALIDATE验证 / NEGOTIATE谈判 / IMPLEMENT实施 / DELIVERY交付',
    template_id BIGINT                                      COMMENT '使用的推进模板 ID',
    manager_id  BIGINT                                      COMMENT '商机负责人(创建人)',
    pm_id       BIGINT                                      COMMENT '项目经理(铁三角)',
    sm_id       BIGINT                                      COMMENT '解决方案经理(铁三角)',
    dm_id       BIGINT                                      COMMENT '交付经理(铁三角)',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer (customer_id),
    INDEX idx_stage (stage),
    INDEX idx_pm (pm_id),
    INDEX idx_sm (sm_id),
    INDEX idx_dm (dm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机表';

CREATE TABLE crm_opp_stage (
    id         BIGINT      PRIMARY KEY AUTO_INCREMENT,
    opp_id     BIGINT      NOT NULL                         COMMENT '商机 ID',
    stage_code VARCHAR(20) NOT NULL,
    stage_name VARCHAR(50) NOT NULL,
    sort_order INT         NOT NULL,
    owner_id   BIGINT                                       COMMENT '环节负责人,空则默认为项目经理',
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING'       COMMENT 'PENDING/DOING/DONE',
    start_time DATETIME,
    end_time   DATETIME,
    INDEX idx_opp (opp_id),
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机环节实例(按模板展开)';

CREATE TABLE crm_opp_team (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT,
    opp_id      BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    member_type VARCHAR(10) NOT NULL                        COMMENT 'CORE核心组 / SUPPORT支撑组',
    group_name  VARCHAR(50)                                 COMMENT '分组名称(如研发、测试、商务)',
    role        VARCHAR(50)                                 COMMENT '组内职责',
    add_by      BIGINT                                      COMMENT '添加人',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_opp_user (opp_id, user_id),
    INDEX idx_opp (opp_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机组员';

CREATE TABLE crm_opp_task (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    opp_id        BIGINT       NOT NULL,
    stage_id      BIGINT                                    COMMENT '所属环节 ID',
    task_name     VARCHAR(200) NOT NULL,
    content       TEXT                                      COMMENT '任务内容',
    assignee_id   BIGINT                                    COMMENT '被指派人',
    assign_by     BIGINT                                    COMMENT '指派人',
    reply_content TEXT                                      COMMENT '回复内容,关闭前可多次修改',
    status        VARCHAR(20)  NOT NULL DEFAULT 'TODO'      COMMENT 'TODO待办 / DOING进行中 / DONE已关闭(不可改)',
    progress      INT          NOT NULL DEFAULT 0           COMMENT '完成进度 0-100;受理人/铁三角可改;关闭(DONE)时强制为 100,之后不可改',
    close_time    DATETIME                                  COMMENT '关闭时间',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_opp (opp_id),
    INDEX idx_stage (stage_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机关键任务';

-- ============================================================================
-- 5. 合同模块
-- ============================================================================

CREATE TABLE crm_contract (
    id            BIGINT         PRIMARY KEY AUTO_INCREMENT,
    contract_name VARCHAR(200)   NOT NULL UNIQUE            COMMENT '合同名称(不可重复)',
    customer_id   BIGINT         NOT NULL                   COMMENT '客户 ID',
    opp_id        BIGINT                                    COMMENT '来源商机 ID',
    contract_type VARCHAR(50)                               COMMENT '合同类型(下拉,可写死)',
    total_amount  DECIMAL(18,2)  NOT NULL                   COMMENT '合同金额,须等于付款节点总和',
    status        VARCHAR(20)    NOT NULL DEFAULT 'EXECUTING' COMMENT 'EXECUTING执行中 / COMPLETED已交付 / CLOSED已关闭',
    contract_year INT            NOT NULL                   COMMENT '合同年份,首页按年筛选',
    file_url      VARCHAR(500)                              COMMENT '合同正文附件',
    delivery_time DATETIME                                  COMMENT '验收/交付时间(PMO 专题用)',
    close_time    DATETIME                                  COMMENT '关闭时间',
    close_by      BIGINT                                    COMMENT '关闭人',
    create_by     BIGINT         NOT NULL                   COMMENT '创建人',
    create_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_year (contract_year),
    INDEX idx_status (status),
    INDEX idx_customer (customer_id),
    INDEX idx_opp (opp_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

CREATE TABLE crm_contract_payment (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    contract_id   BIGINT        NOT NULL,
    node_name     VARCHAR(100)  NOT NULL                    COMMENT '付款节点名称',
    plan_amount   DECIMAL(18,2) NOT NULL                    COMMENT '计划金额',
    actual_amount DECIMAL(18,2)                             COMMENT '实际到款',
    status        TINYINT       NOT NULL DEFAULT 0          COMMENT '0未付 1已付',
    plan_date     DATE                                      COMMENT '计划付款日期',
    pay_time      DATETIME                                  COMMENT '实际付款时间',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_contract (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同付款节点';

-- ============================================================================
-- 6. 周报模块
-- ============================================================================

CREATE TABLE crm_weekly_report (
    id             BIGINT      PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL                     COMMENT '提交人(销售)',
    supervisor_id  BIGINT      NOT NULL                     COMMENT '接收主管(冗余,提升查询)',
    year           INT         NOT NULL                     COMMENT '年份',
    week_num       INT         NOT NULL                     COMMENT '第几周(ISO 周数)',
    attendance     TEXT                                     COMMENT '出勤情况,JSON 存每天一条',
    this_week_work TEXT                                     COMMENT '本周工作情况',
    next_week_plan TEXT                                     COMMENT '下周计划',
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT'     COMMENT 'DRAFT草稿 / SUBMITTED已提交 / COMMENTED已点评',
    submit_time    DATETIME                                 COMMENT '提交时间',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_week (user_id, year, week_num),
    INDEX idx_supervisor_status (supervisor_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售周报';

CREATE TABLE crm_weekly_report_comment (
    id           BIGINT   PRIMARY KEY AUTO_INCREMENT,
    report_id    BIGINT   NOT NULL,
    commenter_id BIGINT   NOT NULL                          COMMENT '点评人(主管)',
    content      TEXT     NOT NULL                          COMMENT '点评内容',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周报点评';

-- ============================================================================
-- 7. 初始化角色数据
-- ============================================================================

INSERT INTO sys_role (role_name, role_code, description) VALUES
('系统管理员',   'ADMIN',            '用户管理、角色管理、模板管理'),
('商机管理员',   'OPP_ADMIN',        '录入客户、派单给客户经理、整体调度'),
('客户经理',     'CUSTOMER_MANAGER', '走访客户、完善客户信息、线索培育'),
('销售人员',     'SALES',            '提交周报、录入线索'),
('主管',         'SUPERVISOR',       '审阅并点评下属周报'),
('解决方案经理', 'SOLUTION_MANAGER', '商机方案制定,铁三角成员'),
('交付经理',     'DELIVERY_MANAGER', '项目交付,铁三角成员'),
('项目经理',     'PROJECT_MANAGER',  '项目运作,铁三角核心'),
('区总',         'REGION_HEAD',      '查看在途合同专题'),
('项目管理部',   'PMO',              '查看已验收项目专题、月度汇报'),
('普通用户',     'USER',             '录入线索,所有人默认拥有');

-- ============================================================================
-- 8. 内置默认商机模板
-- ============================================================================

INSERT INTO crm_opp_template (id, template_name, description, is_default)
VALUES (1, '标准项目推进模板', '包含验证机会点、谈判与合同签订、项目实施、验收与交付四个必含环节', 1);

INSERT INTO crm_opp_template_stage (template_id, stage_code, stage_name, sort_order, required) VALUES
(1, 'VALIDATE',  '验证机会点',       1, 1),
(1, 'NEGOTIATE', '谈判与合同签订',   2, 1),
(1, 'IMPLEMENT', '项目实施',         3, 1),
(1, 'DELIVERY',  '验收与交付项目',   4, 1);

-- ============================================================================
-- 9. 种子用户(密码明文为 123456,上线前请改为 BCrypt 加密)
--    BCrypt("123456") = $2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi
-- ============================================================================

INSERT INTO sys_user (id, username, password, real_name, role_id, status) VALUES
(1, 'admin',    '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '系统管理员',   1,  1),
(2, 'oppadmin', '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '商机管理员张', 2,  1),
(3, 'cm001',    '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '客户经理李',   3,  1),
(4, 'sales001', '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '销售小王',     4,  1),
(5, 'sup001',   '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '主管刘',       5,  1),
(6, 'sm001',    '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '解决方案周',   6,  1),
(7, 'dm001',    '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '交付吴',       7,  1),
(8, 'pm001',    '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '项目经理陈',   8,  1),
(9, 'region01', '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', '区总赵',       9,  1),
(10,'pmo01',    '$2b$10$EH74CkxWCj2bmWKOQkU1hOJa89Hshx27Ydk9WAfPVlyxl0u5xRZWi', 'PMO 钱',       10, 1);

-- 设置销售的主管
UPDATE sys_user SET supervisor_id = 5 WHERE id = 4;

-- 用户-角色关系(与主角色一致,可按需追加多角色)
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,1),(2,2),(3,3),(4,4),(5,5),(6,6),(7,7),(8,8),(9,9),(10,10);

-- 所有人都额外持有 USER 角色(兜底)
INSERT INTO sys_user_role (user_id, role_id)
SELECT id, 11 FROM sys_user;
