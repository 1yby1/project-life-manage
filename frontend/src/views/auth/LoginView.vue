<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span class="login-title">登录</span>
        </div>
      </template>
      <p class="login-sub">演示：支持“后端登录”和“演示角色登录”。</p>

      <div class="mode-row">
        <el-switch
          v-model="useDemo"
          active-text="使用演示角色（本地权限）"
          inactive-text="后端登录"
          size="large"
        />
      </div>

      <!-- 演示角色登录 -->
      <el-form v-if="useDemo" class="demo-form" size="large" @submit.prevent="doDemoLogin">
        <el-form-item label="选择角色">
          <el-select v-model="roleLabel" placeholder="请选择角色" style="width: 100%">
            <el-option value="商机管理员" label="商机管理员" />
            <el-option value="客户经理" label="客户经理" />
            <el-option value="合同管理员" label="合同管理员" />
            <el-option value="线索收集人" label="线索收集人" />
            <el-option value="线索分发人" label="线索分发人" />
            <el-option value="客户/线索培育" label="客户/线索培育" />
            <el-option value="项目经理" label="项目经理" />
            <el-option value="销售人员" label="销售人员" />
            <el-option value="销售主管/管理层" label="销售主管/管理层" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :icon="Right"
            :loading="authState.loading"
            style="width: 100%"
            @click="doDemoLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 后端账号登录 -->
      <el-form v-else class="demo-form" size="large" @submit.prevent="doLogin">
        <el-form-item label="账号">
          <el-input
            v-model="username"
            placeholder="请输入账号"
            :prefix-icon="User"
            clearable
            @keyup.enter="doLogin"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="password"
            placeholder="请输入密码"
            type="password"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="doLogin"
          />
        </el-form-item>

        <el-form-item class="footer-actions">
          <el-button :icon="HomeFilled" @click="goHome">返回首页</el-button>
          <el-button
            type="primary"
            :icon="Right"
            :loading="authState.loading"
            @click="doLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="message"
        :title="message"
        :type="messageType"
        show-icon
        :closable="false"
        class="login-alert"
      />
    </el-card>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ensureUser, getAuthState, login, loginAsDemoRole } from '@/auth/authStore'
import { User, Lock, Right, HomeFilled } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'LoginView',
  components: {
    User,
    Lock,
    Right,
    HomeFilled,
  },
  setup() {
    const router = useRouter()
    const route = useRoute()

    const authState = getAuthState()

    const useDemo = ref(true)
    const roleLabel = ref('客户经理')

    const username = ref('')
    const password = ref('')

    const message = ref('')
    const messageType = ref<'error' | 'success'>('success')

    const redirectPath = computed(() => String(route.query.redirect || '/'))

    const goHome = () => router.push({ path: '/' })

    const doDemoLogin = async () => {
      await loginAsDemoRole(roleLabel.value)
      await ensureUser()
      router.push({ path: redirectPath.value })
    }

    const doLogin = async () => {
      message.value = ''
      const ok = await login(username.value, password.value)
      if (!ok) {
        messageType.value = 'error'
        message.value = '登录失败：请检查账号/密码，或确认后端接口配置'
        return
      }
      const user = await ensureUser()
      if (!user) {
        messageType.value = 'error'
        message.value = '登录失败：无法获取当前用户信息'
        return
      }
      router.push({ path: redirectPath.value })
    }

    return {
      authState,
      useDemo,
      roleLabel,
      username,
      password,
      doDemoLogin,
      doLogin,
      goHome,
      message,
      messageType,
      User,
      Lock,
      Right,
      HomeFilled,
    }
  },
})
</script>

<style scoped lang="scss">
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: linear-gradient(135deg, #0F172A 0%, #1E293B 50%, #334155 100%);
}

.login-card {
  width: min(480px, 100%);
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
}

.login-sub {
  text-align: center;
  color: #334155;
  margin-bottom: 24px;
  font-size: 14px;
}

.mode-row {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.demo-form {
  margin-top: 20px;
}

.footer-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.footer-actions :deep(.el-form-item__content) {
  flex-wrap: nowrap;
  gap: 12px;
}

.footer-actions .el-button {
  flex: 1;
}

.login-alert {
  margin-top: 16px;
}

:deep(.el-card__header) {
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
}

:deep(.el-button--primary) {
  background-color: #0369A1;
  border-color: #0369A1;
}

:deep(.el-button--primary:hover) {
  background-color: #0284C7;
  border-color: #0284C7;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #0369A1;
  border-color: #0369A1;
}

:deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #0369A1 inset;
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #0369A1 inset !important;
}
</style>

