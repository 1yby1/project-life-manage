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
            <span class="btn-text">{{ authState.loading ? '登录中...' : '登录' }}</span>
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

      <!-- 种子账号(仅 DEV 模式可见) -->
      <el-collapse v-if="isDev" v-model="seedOpen" class="seed-collapse">
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
    const isDev = import.meta.env.DEV

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
      isDev,
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
  background: var(--color-bg-soft);
  padding: var(--space-6);
  font-family: var(--font-sans);
}

/* ---------- 卡片(border + shadow 二选一,这里选 shadow) ---------- */

.login-card {
  width: 440px;
  max-width: 100%;
  padding: var(--space-8);
  border-radius: var(--radius-md);
  background: var(--color-bg);
  box-shadow: var(--shadow-2);
}

/* ---------- 品牌区 ---------- */

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  color: var(--color-text-on-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.brand-text {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--color-text-primary);
  line-height: var(--leading-tight);
}

/* ---------- 主标题区 ---------- */

.title {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--color-text-primary);
  margin: 0 0 var(--space-2);
  line-height: var(--leading-tight);
}

.subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
  margin: 0;
  line-height: var(--leading-normal);
}

.divider {
  height: 1px;
  background: var(--color-border);
  margin: var(--space-6) 0;
}

/* ---------- 表单 ---------- */

.login-form {
  :deep(.el-form-item) {
    margin-bottom: var(--space-4);
  }

  :deep(.el-form-item__label) {
    line-height: var(--leading-tight);
    padding: 0 0 var(--space-2);
  }

  .form-label {
    font-size: var(--text-sm);
    font-weight: var(--weight-medium);
    color: var(--color-text-secondary);
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--radius-sm);
    border: 1px solid var(--color-border);
    box-shadow: none;
    transition: border-color var(--duration-base) var(--easing);
    padding: 1px 12px;
  }

  :deep(.el-input__wrapper:hover) {
    border-color: var(--color-border-strong);
  }

  :deep(.el-input__wrapper.is-focus) {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(3, 105, 161, 0.12);
  }

  :deep(.el-input__inner) {
    height: var(--form-control-height);
    color: var(--color-text-primary);
    font-size: var(--text-sm);
  }

  :deep(.el-input__prefix-inner .el-icon) {
    color: var(--color-text-disabled);
    font-size: var(--text-base);
    transition: color var(--duration-base) var(--easing);
  }

  :deep(.el-input__wrapper.is-focus .el-input__prefix-inner .el-icon) {
    color: var(--color-primary);
  }
}

/* ---------- 主按钮(纯色,hover 仅 darken) ---------- */

.submit-item {
  margin-top: var(--space-6) !important;
  margin-bottom: 0 !important;

  :deep(.el-form-item__content) {
    display: block;
  }
}

.login-btn {
  width: 100%;
  height: var(--button-height-lg);
  border-radius: var(--radius-md);
  background: var(--color-primary);
  border-color: var(--color-primary);
  font-weight: var(--weight-semibold);
  letter-spacing: 0.5px;
  transition: background var(--duration-base) var(--easing);

  &:hover,
  &:focus {
    background: var(--color-primary-hover);
    border-color: var(--color-primary-hover);
  }

  &:active {
    background: var(--color-primary-active);
    border-color: var(--color-primary-active);
  }

  .btn-text {
    font-size: var(--text-base);
  }
}

/* ---------- 错误提示 ---------- */

.error-slot {
  min-height: 24px;
  padding-top: var(--space-2);
  display: flex;
  align-items: center;
}

.error-text {
  display: inline-flex;
  align-items: center;
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--color-error);
  line-height: var(--leading-tight);
}

.error-icon {
  font-size: var(--text-sm);
  margin-right: var(--space-2);
  color: var(--color-error);
}

/* ---------- 种子账号(仅 DEV) ---------- */

.seed-collapse {
  margin-top: var(--space-1);
  border: none;

  :deep(.el-collapse-item__header) {
    height: 32px;
    line-height: 32px;
    font-size: var(--text-xs);
    color: var(--color-text-disabled);
    border: none;
    background: transparent;
  }

  :deep(.el-collapse-item__header.is-active) {
    color: var(--color-text-secondary);
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
  font-size: var(--text-xs);
  color: inherit;
}

.seed-list {
  margin-top: var(--space-1);
  background: var(--color-bg-soft);
  border-radius: var(--radius-sm);
  padding: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.seed-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  font-family: var(--font-mono);
}

.seed-username {
  min-width: 84px;
  color: var(--color-text-primary);
  font-weight: var(--weight-medium);
}

.seed-sep {
  color: var(--color-border-strong);
}

.seed-pwd {
  min-width: 60px;
  color: var(--color-text-tertiary);
}

.seed-role {
  margin-left: auto;
  font-family: var(--font-sans);
  font-size: var(--text-xs);
  color: var(--color-text-disabled);
}

/* ---------- 页脚 ---------- */

.footer {
  margin-top: var(--space-8);
  font-size: var(--text-xs);
  color: var(--color-text-disabled);
  text-align: center;
}
</style>
