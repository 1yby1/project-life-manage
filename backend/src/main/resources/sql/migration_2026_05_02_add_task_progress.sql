-- ============================================================================
-- Migration: 给 crm_opp_task 表加 progress INT 列
-- 适用: 已经初始化过 schema.sql(无 progress 列)的老库平滑升级
-- 全新部署直接跑 schema.sql 即可,无需此脚本
-- 创建日期: 2026-05-02
-- ============================================================================

USE projectlife;

-- 加列(默认 0,DONE 状态的任务通过下面 UPDATE 强制为 100)
ALTER TABLE crm_opp_task
    ADD COLUMN progress INT NOT NULL DEFAULT 0
    COMMENT '完成进度 0-100;受理人/铁三角可改;关闭(DONE)时强制为 100,之后不可改'
    AFTER status;

-- 已关闭的历史任务 progress 强制 100
UPDATE crm_opp_task SET progress = 100 WHERE status = 'DONE';

-- 进行中的任务 progress 派生:DOING=50, TODO=0(默认值)
UPDATE crm_opp_task SET progress = 50 WHERE status = 'DOING';
