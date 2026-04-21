<template>
  <div class="home">
    <div class="header">
      <h1 class="title">
        <el-icon :size="32" class="title-icon"><Menu /></el-icon>
        业务后台首页
      </h1>
      <el-alert
        class="subtitle-alert"
        type="info"
        :closable="false"
        show-icon
      >
        <template #title>
          按模块进入：客户 → 线索 → 商机 → 合同。当前页面为前端 UI 骨架与交互占位。
        </template>
      </el-alert>
    </div>

    <div class="cards">
      <el-card 
        class="card" 
        shadow="hover" 
        @click="$router.push('/customers/list')"
        @keypress.enter="$router.push('/customers/list')"
        tabindex="0"
        role="button"
        aria-label="进入客户管理模块"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="28"><User /></el-icon>
            <span class="card-title">客户管理</span>
          </div>
        </template>
        <p class="card-desc">录入、派单、走访锁定、信息完善</p>
      </el-card>

      <el-card 
        class="card" 
        shadow="hover" 
        @click="$router.push('/clues/list')"
        @keypress.enter="$router.push('/clues/list')"
        tabindex="0"
        role="button"
        aria-label="进入线索管理模块"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="28"><Opportunity /></el-icon>
            <span class="card-title">线索管理</span>
          </div>
        </template>
        <p class="card-desc">收集、分发、培育转商机</p>
      </el-card>

      <el-card 
        class="card" 
        shadow="hover" 
        @click="$router.push('/opportunities/list')"
        @keypress.enter="$router.push('/opportunities/list')"
        tabindex="0"
        role="button"
        aria-label="进入商机管理模块"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="28"><TrendCharts /></el-icon>
            <span class="card-title">商机管理</span>
          </div>
        </template>
        <p class="card-desc">模板固化、责任人选择、任务跟踪</p>
      </el-card>

      <el-card 
        class="card" 
        shadow="hover" 
        @click="$router.push('/contracts/board')"
        @keypress.enter="$router.push('/contracts/board')"
        tabindex="0"
        role="button"
        aria-label="进入合同管理模块"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="28"><Document /></el-icon>
            <span class="card-title">合同管理</span>
          </div>
        </template>
        <p class="card-desc">创建、节点查看、执行与关闭</p>
      </el-card>

      <el-card 
        class="card" 
        shadow="hover" 
        @click="$router.push('/reports/submit')"
        @keypress.enter="$router.push('/reports/submit')"
        tabindex="0"
        role="button"
        aria-label="进入周报管理模块"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="28"><Tickets /></el-icon>
            <span class="card-title">周报管理</span>
          </div>
        </template>
        <p class="card-desc">提交周报与点评查看</p>
      </el-card>

      <el-card 
        class="card" 
        shadow="hover" 
        @click="$router.push('/analytics/inflight')"
        @keypress.enter="$router.push('/analytics/inflight')"
        tabindex="0"
        role="button"
        aria-label="进入专题分析模块"
      >
        <template #header>
          <div class="card-header">
            <el-icon class="card-icon" :size="28"><DataAnalysis /></el-icon>
            <span class="card-title">专题分析</span>
          </div>
        </template>
        <p class="card-desc">在途合同 / 已验收项目</p>
      </el-card>
    </div>
    <div class="hint">
      <strong>说明：</strong>页面内的列表/表单数据目前为演示用（本地状态）。你可以把每个页面的“接口占位”替换成真实 API。
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { Menu, User, Opportunity, TrendCharts, Document, Tickets, DataAnalysis } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'HomeView',
  components: {
    Menu,
    User,
    Opportunity,
    TrendCharts,
    Document,
    Tickets,
    DataAnalysis,
  },
})
</script>

<style scoped lang="scss">

.home {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 32px;
}

.title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  color: #0f172a;
}

.title .title-icon {
  margin-right: 12px;
  color: #0ea5e9;
}

.subtitle-alert {
  border-radius: 8px;
  background-color: #ffffff !important;
  border: 1px solid #e2e8f0;
}

:deep(.el-alert__title) {
  font-size: 14px;
  line-height: 1.6;
  color: #475569 !important;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.card {
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 12px;
  border: 1px solid #e2e8f0 !important;
  background: #ffffff !important;
  box-shadow: 0 1px 3px 0 rgba(0,0,0,0.05) !important;
  overflow: hidden;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.05) !important;
  border-color: #bae6fd !important;
}

:deep(.el-card__header) {
  padding: 20px 24px 16px;
  background: transparent;
  border-bottom: 1px dashed #e2e8f0;
}

:deep(.el-card__body) {
  padding: 20px 24px;
  background: transparent;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #0f172a;
}

.card-icon {
  color: #0ea5e9;
  flex-shrink: 0;
}

.card-title {
  font-weight: 600;
  font-size: 18px;
}

.card-desc {
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.hint {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  margin-top: 40px;
}

.hint strong {
  color: #f59e0b;
  font-weight: 500;
}

</style>
