<template>
  <div class="login-wrap">
    <div class="login-card">
      <!-- 品牌区 -->
      <div class="brand">
        <div class="brand-logo">
          <el-icon :size="20"><Suitcase /></el-icon>
        </div>
        <div class="brand-text">项目全生命周期管理系统</div>
      </div>

      <!-- 主标题区 -->
      <div class="title-block">
        <h2 class="title">欢迎登录</h2>
        <p class="subtitle">请使用账号与密码访问系统</p>
      </div>

      <div class="divider" />

      <!-- 表单 -->
      <el-form class="login-form" size="large" @submit.prevent="doLogin">
        <el-form-item>
          <template #label><span class="form-label">账号</span></template>
          <el-input
            v-model="username"
            placeholder="请输入账号"
            :prefix-icon="User"
            clearable
            @keyup.enter="doLogin"
          />
        </el-form-item>

        <el-form-item>
          <template #label><span class="form-label">密码</span></template>
          <el-input
            v-model="password"
            placeholder="请输入密码"
            type="password"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="doLogin"
          />
        </el-form-item>

        <el-form-item class="submit-item">
          <el-button
            type="primary"
            class="login-btn"
            :loading="authState.loading"
            @click="doLogin"
          >
            <span class="btn-text">{{ authState.loading ? '登录中...' : '登 录' }}</span>
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 错误提示(inline) -->
      <div class="error-slot">
        <span v-if="message" class="error-text">
          <el-icon class="error-icon"><WarningFilled /></el-icon>
          {{ message }}
        </span>
      </div>

      <!-- 种子账号(可折叠) -->
      <el-collapse v-model="seedOpen" class="seed-collapse">
        <el-collapse-item name="seed">
          <template #title>
            <span class="seed-title">种子账号(点击展开)</span>
          </template>
          <div class="seed-list">
            <div v-for="acc in seedAccounts" :key="acc.username" class="seed-row">
              <span class="seed-username">{{ acc.username }}</span>
              <span class="seed-sep">/</span>
              <span class="seed-pwd">{{ acc.password }}</span>
              <span class="seed-role">{{ acc.role }}</span>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <div class="footer">© 2026 · 项目全生命周期管理系统 · v1.0</div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ensureUser, getAuthState, login } from '@/auth/authStore'
import { getLandingPath } from '@/auth/landing'
import { User, Lock, Suitcase, WarningFilled } from '@element-plus/icons-vue'

const SEED_ACCOUNTS = [
  { username: 'admin',     password: '123456', role: '系统管理员' },
  { username: 'oppadmin',  password: '123456', role: '商机管理员' },
  { username: 'cm001',     password: '123456', role: '客户经理' },
  { username: 'sales001',  password: '123456', role: '销售人员' },
  { username: 'sup001',    password: '123456', role: '主管' },
  { username: 'pm001',     password: '123456', role: '项目经理' },
  { username: 'region01',  password: '123456', role: '区总' },
  { username: 'pmo01',     password: '123456', role: '项目管理部' },
]

export default defineComponent({
  name: 'LoginView',
  components: { Suitcase, WarningFilled },
  setup() {
    const router = useRouter()
    const route = useRoute()

    const authState = getAuthState()

    const username = ref('')
    const password = ref('')

    const message = ref('')
    const seedOpen = ref<string[]>([])
    const seedAccounts = SEED_ACCOUNTS

    const explicitRedirect = computed(() => {
      const r = route.query.redirect
      return typeof r === 'string' && r ? r : ''
    })

    // 用户重新输入时清空错误提示
    watch([username, password], () => {
      if (message.value) message.value = ''
    })

    const doLogin = async () => {
      message.value = ''
      if (!username.value.trim() || !password.value) {
        message.value = '请输入账号和密码'
        return
      }
      const ok = await login(username.value.trim(), password.value)
      if (!ok) {
        message.value = '登录失败:请检查账号 / 密码,或确认后端接口是否启动'
        return
      }
      const user = await ensureUser()
      if (!user) {
        message.value = '登录失败:无法获取当前用户信息'
        return
      }
      const target = explicitRedirect.value || getLandingPath(user)
      router.push({ path: target })
    }

    return {
      authState,
      username,
      password,
      message,
      seedOpen,
      seedAccounts,
      doLogin,
      User,
      Lock,
      Suitcase,
      WarningFilled,
    }
  },
})
</script>

<style scoped lang="scss">
.login-wrap {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 50%, #f8fafc 100%);
  padding: 24px;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

/* ---------- 卡片 ---------- */

.login-card {
  width: 440px;
  max-width: 100%;
  padding: 36px 32px 28px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow:
    0 20px 50px -12px rgba(15, 23, 42, 0.12),
    0 4px 12px rgba(15, 23, 42, 0.04);
}

/* ---------- 品牌区 ---------- */

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #0369a1;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-text {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  line-height: 1.3;
}

/* ---------- 主标题区 ---------- */

.title-block {
  margin-bottom: 0;
}

.title {
  font-size: 22px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 6px;
  line-height: 1.3;
}

.subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.divider {
  height: 1px;
  background: #e2e8f0;
  margin: 20px 0;
}

/* ---------- 表单 ---------- */

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    line-height: 1.4;
    padding: 0 0 6px;
  }

  .form-label {
    font-size: 13px;
    font-weight: 500;
    color: #475569;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    box-shadow: none;
    transition: border-color 200ms ease, box-shadow 200ms ease;
    padding: 1px 11px;
  }

  :deep(.el-input__wrapper:hover) {
    border-color: #cbd5e1;
  }

  :deep(.el-input__wrapper.is-focus) {
    border-color: #0369a1;
    box-shadow: 0 0 0 3px rgba(3, 105, 161, 0.12);
  }

  :deep(.el-input__inner) {
    height: 40px;
    color: #0f172a;
    font-size: 14px;
  }

  :deep(.el-input__prefix-inner .el-icon) {
    color: #94a3b8;
    font-size: 16px;
    transition: color 200ms ease;
  }

  :deep(.el-input__wrapper.is-focus .el-input__prefix-inner .el-icon) {
    color: #0369a1;
  }
}

/* ---------- 主按钮 ---------- */

.submit-item {
  margin-top: 24px !important;
  margin-bottom: 0 !important;

  :deep(.el-form-item__content) {
    display: block;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  background: #0369a1;
  border-color: #0369a1;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: background 200ms ease, box-shadow 200ms ease;

  &:hover,
  &:focus {
    background: #0284c7;
    border-color: #0284c7;
    box-shadow: 0 4px 12px rgba(3, 105, 161, 0.25);
  }

  &:active {
    background: #075985;
    border-color: #075985;
  }

  .btn-text {
    font-size: 15px;
  }
}

/* ---------- 错误提示 ---------- */

.error-slot {
  min-height: 22px;
  padding-top: 8px;
  display: flex;
  align-items: center;
}

.error-text {
  display: inline-flex;
  align-items: center;
  font-size: 13px;
  font-weight: 500;
  color: #dc2626;
  line-height: 1.4;
}

.error-icon {
  font-size: 14px;
  margin-right: 6px;
  color: #dc2626;
}

/* ---------- 种子账号 ---------- */

.seed-collapse {
  margin-top: 4px;
  border: none;

  :deep(.el-collapse-item__header) {
    height: 32px;
    line-height: 32px;
    font-size: 12px;
    color: #94a3b8;
    border: none;
    background: transparent;
  }

  :deep(.el-collapse-item__header.is-active) {
    color: #475569;
  }

  :deep(.el-collapse-item__wrap) {
    border: none;
    background: transparent;
  }

  :deep(.el-collapse-item__content) {
    padding: 0;
  }
}

.seed-title {
  font-size: 12px;
  color: inherit;
}

.seed-list {
  margin-top: 4px;
  background: #f8fafc;
  border-radius: 6px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.seed-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #475569;
  font-family: 'Fira Code', 'Cascadia Code', Consolas, monospace;
}

.seed-username {
  min-width: 84px;
  color: #0f172a;
  font-weight: 500;
}

.seed-sep {
  color: #cbd5e1;
}

.seed-pwd {
  min-width: 60px;
  color: #64748b;
}

.seed-role {
  margin-left: auto;
  font-family: 'Inter', system-ui, sans-serif;
  font-size: 11px;
  color: #94a3b8;
}

/* ---------- 页脚 ---------- */

.footer {
  margin-top: 32px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}
</style>
