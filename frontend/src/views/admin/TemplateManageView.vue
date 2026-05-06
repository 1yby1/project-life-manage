<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">商机模板管理</h2>
    </div>

    <el-card class="template-card" shadow="never">
      <template #header>
        <div class="card-title">
          <el-icon><Files /></el-icon>
          内置商机推进模板
        </div>
      </template>

      <div class="template-list">
        <div v-for="tpl in templates" :key="tpl.id" class="template-item">
          <div class="template-head">
            <div class="template-name">{{ tpl.name }}</div>
            <el-tag v-if="tpl.isDefault" type="primary" effect="light" round size="small">默认</el-tag>
          </div>
          <div class="template-desc">{{ tpl.description }}</div>
          <div class="stage-list">
            <div v-for="(s, idx) in tpl.stages" :key="s.code" class="stage-item">
              <span class="stage-num">{{ idx + 1 }}</span>
              <span class="stage-name">{{ s.name }}</span>
              <el-tag v-if="s.required" type="warning" size="small" effect="plain" round>必含</el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="hint-alert"
    >
      <template #title>
        按需求文档(模块 4 商机管理)设计:推进模板由系统内置、<strong>仅提供选择不开放编辑</strong>,避免模板漂移。验证机会点 / 谈判与签订 / 项目实施 / 验收交付 四个必含环节固化在 <code>crm_opp_template_stage</code> 中,商机详情页 PM 可基于此模板一键展开环节实例。
      </template>
    </el-alert>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { Files } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'TemplateManageView',
  components: { Files },
  setup() {
    const templates = [
      {
        id: 1,
        name: '标准项目推进模板',
        description: '包含验证机会点、谈判与合同签订、项目实施、验收与交付四个必含环节',
        isDefault: true,
        stages: [
          { code: 'VALIDATE',  name: '验证机会点',     required: true },
          { code: 'NEGOTIATE', name: '谈判与合同签订', required: true },
          { code: 'IMPLEMENT', name: '项目实施',       required: true },
          { code: 'DELIVERY',  name: '验收与交付项目', required: true },
        ],
      },
    ]

    return { templates }
  },
})
</script>

<style scoped lang="scss">
.page {
  max-width: 1100px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #0F172A;
  margin: 0;
}

.template-card {
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  margin-bottom: 16px;

  :deep(.el-card__header) {
    padding: 14px 20px;
    background: #F8FAFC;
    border-bottom: 1px solid #E2E8F0;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #0F172A;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.template-item {
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 16px;
  background: #FFFFFF;
  transition: all 0.15s;

  &:hover {
    border-color: #0369A1;
  }
}

.template-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.template-name {
  font-weight: 600;
  font-size: 15px;
  color: #0F172A;
}

.template-desc {
  font-size: 13px;
  color: #64748B;
  margin-bottom: 16px;
  line-height: 1.6;
}

.stage-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 8px;
}

.stage-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.stage-num {
  width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #E0F2FE;
  color: #0369A1;
  border-radius: 50%;
  font-weight: 600;
  font-size: 11px;
  flex-shrink: 0;
}

.stage-name {
  flex: 1;
  font-weight: 500;
  color: #0F172A;
}

.hint-alert {
  border-radius: 10px;
}
</style>
