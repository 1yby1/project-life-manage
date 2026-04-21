<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="login-title">用户登录</span>
        </div>
      </template>

      <el-alert
        title="演示：支持"后端登录"和"演示角色登录"。"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <div class="mode-row">
        <el-switch
          v-model="useDemo"
          active-text="使用演示角色（本地权限）"
          inactive-text="后端登录"
        />
      </div>

      <el-form v-if="useDemo" label-position="top" class="demo-form">
        <el-form-item label="选择角色">
          <el-select v-model="roleLabel" placeholder="请选择角色" style="width: 100%">
            <el-option label="商机管理员" value="商机管理员" />
            <el-option label="客户经理" value="客户经理" />
            <el-option label="合同管理员" value="合同管理员" />
            <el-option label="线索收集人" value="线索收集人" />
            <el-option label="线索分发人" value="线索分发人" />
            <el-option label="客户/线索培育" value="客户/线索培育" />
            <el-option label="项目经理" value="项目经理" />
            <el-option label="销售人员" value="销售人员" />
            <el-option label="销售主管/管理层" value="销售主管/管理层" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="authState.loading"
            @click="doDemoLogin"
            style="width: 100%"
          >
            <User style="margin-right: 8px" />
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <el-form v-else label-position="top" class="demo-form">
        <el-form-item label="账号">
          <el-input
            v-model="username"
            placeholder="请输入账号"
            :prefix-icon="User"
            clearable
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

        <el-form-item>
          <div class="footer-actions">
            <el-button @click="goHome">
              <HomeFilled style="margin-right: 8px" />
              返回首页
            </el-button>
            <el-button
              type="primary"
              :loading="authState.loading"
              @click="doLogin"
            >
              <Right style="margin-right: 8px" />
              登录
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="message"
        :title="message"
        :type="messageType === 'error' ? 'error' : 'success'"
        :closable="false"
        show-icon
        style="margin-top: 16px"
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: min(480px, 100%);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
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

.footer-actions .el-button {
  flex: 1;
}
</style>
