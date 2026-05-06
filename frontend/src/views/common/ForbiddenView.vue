<template>
  <div class="forbidden-wrap">
    <el-result
      icon="warning"
      title="403 无权限访问"
      :sub-title="subTitle"
    >
      <template #extra>
        <el-button type="primary" :icon="HomeFilled" @click="goHome">返回首页</el-button>
        <el-button :icon="SwitchButton" @click="goLogin">重新登录</el-button>
      </template>
    </el-result>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, SwitchButton } from '@element-plus/icons-vue'
import { getAuthState, logout } from '@/auth/authStore'
import { roleCodeToName } from '@/auth/roles'

export default defineComponent({
  name: 'ForbiddenView',
  components: { HomeFilled, SwitchButton },
  setup() {
    const router = useRouter()
    const authState = getAuthState()

    const subTitle = computed(() => {
      const user = authState.user
      if (!user) return '当前未登录或登录已失效'
      const roleNames = (user.roles || []).map((c) => roleCodeToName(c)).join('、')
      return `当前用户「${user.name}」(${roleNames || '无角色'})没有访问该页面的权限`
    })

    const goHome = () => router.push({ path: '/' })
    const goLogin = () => {
      logout()
      router.push({ path: '/login' })
    }

    return { subTitle, goHome, goLogin, HomeFilled, SwitchButton }
  },
})
</script>

<style lang="scss" scoped>
.forbidden-wrap {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
