<template>
  <div class="page">
    <div class="page-header">
      <h2 class="page-title">商机列表</h2>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="商机名称">
          <el-input v-model="query.keyword" placeholder="输入商机名称" clearable :prefix-icon="Search" @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="阶段">
          <el-select v-model="query.stage" placeholder="选择阶段" clearable style="width: 160px">
            <el-option label="全部" value="" />
            <el-option label="验证机会点" value="VALIDATE" />
            <el-option label="谈判与签约" value="NEGOTIATE" />
            <el-option label="项目实施" value="IMPLEMENT" />
            <el-option label="验收与交付" value="DELIVERY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="reload">搜索</el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe v-loading="loading" empty-text="暂无商机数据">
        <el-table-column prop="oppName" label="商机名称" min-width="200">
          <template #default="{ row }">
            <span class="opp-name">{{ row.oppName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="客户" prop="customerName" min-width="150" />
        <el-table-column label="阶段" width="130">
          <template #default="{ row }">
            <el-tag :type="oppStageTagType(row.stage)" effect="light" round>
              {{ oppStageLabel(row.stage) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="PM" prop="pmName" width="110" />
        <el-table-column label="模板" width="160">
          <template #default="{ row }">
            <span v-if="row.templateName">{{ row.templateName }}</span>
            <el-tag v-else type="warning" size="small" effect="plain">未选模板</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.templateId"
              link
              type="primary"
              :icon="Setting"
              @click="goTemplate(row.id)"
            >
              选择推进模板
            </el-button>
            <el-button v-else link type="primary" :icon="View" @click="goDetail(row.id)">
              进入项目详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft, Setting, View } from '@element-plus/icons-vue'
import { opportunityApi, Opportunity, oppStageLabel, oppStageTagType } from '@/api/opportunity'

export default defineComponent({
  name: 'OpportunityListView',
  setup() {
    const router = useRouter()
    const rows = ref<Opportunity[]>([])
    const loading = ref(false)

    const query = reactive({
      keyword: '',
      stage: '',
    })

    const reload = async () => {
      loading.value = true
      try {
        rows.value = await opportunityApi.list({
          keyword: query.keyword.trim() || undefined,
          stage: query.stage || undefined,
        })
      } catch (e: any) {
        ElMessage.error(e?.message || '加载失败')
        rows.value = []
      } finally {
        loading.value = false
      }
    }

    const resetQuery = () => {
      query.keyword = ''
      query.stage = ''
      reload()
    }

    const goTemplate = (id: number) => router.push({ path: '/opportunities/template', query: { opportunityId: id } })
    const goDetail = (id: number) => router.push({ path: '/opportunities/detail', query: { opportunityId: id } })

    onMounted(reload)

    return {
      rows, loading, query, reload, resetQuery, goTemplate, goDetail,
      oppStageLabel, oppStageTagType,
      Search, RefreshLeft, Setting, View,
    }
  },
})
</script>

<style scoped lang="scss">
.page { max-width: 1400px; margin: 0 auto; }
.page-header { margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #0F172A; margin: 0; }
.filter-card, .table-card {
  border-radius: 12px; border: 1px solid #E2E8F0; margin-bottom: 16px;
  :deep(.el-card__body) { padding: 16px 20px; }
  :deep(.el-table th.el-table__cell) {
    background: #F8FAFC !important; color: #0F172A; font-weight: 600; font-size: 13px;
  }
}
.search-form :deep(.el-form-item) { margin-bottom: 0; margin-right: 16px; }
.opp-name { font-weight: 600; color: #0F172A; }
:deep(.el-button--primary) {
  background-color: #0369A1; border-color: #0369A1;
  &:hover { background-color: #0284C7; border-color: #0284C7; }
}
</style>
