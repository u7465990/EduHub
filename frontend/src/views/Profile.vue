<template>
  <div>
    <header class="page-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">EduHub</router-link>
        <nav class="nav">
          <router-link to="/home">首页</router-link>
          <router-link to="/questions">题库</router-link>
          <router-link to="/favorites">收藏</router-link>
          <router-link to="/profile" class="active">个人中心</router-link>
        </nav>
      </div>
    </header>

    <main class="main">
      <div class="profile-layout">
        <!-- 左：用户信息 -->
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="80" :icon="UserFilled" />
            <h3>{{ userStore.user?.nickname || userStore.user?.username }}</h3>
            <p class="role-tag">
              <el-tag :type="userStore.isAdmin ? 'danger' : 'info'" size="small">
                {{ userStore.isAdmin ? '管理员' : '普通用户' }}
              </el-tag>
            </p>
            <div class="coins">
              <el-icon><Coin /></el-icon> {{ userStore.user?.coins || 0 }} 学币
            </div>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item"><span>用户名</span><span>{{ userStore.user?.username }}</span></div>
            <div class="info-item"><span>邮箱</span><span>{{ userStore.user?.email || '未设置' }}</span></div>
            <div class="info-item"><span>手机</span><span>{{ userStore.user?.phone || '未设置' }}</span></div>
            <div class="info-item"><span>注册时间</span><span>{{ userStore.user?.createdAt?.slice(0, 10) }}</span></div>
          </div>
        </el-card>

        <!-- 右：编辑信息 -->
        <el-card class="edit-card">
          <template #header><span>编辑资料</span></template>
          <el-form :model="editForm" label-width="80px">
            <el-form-item label="昵称">
              <el-input v-model="editForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="editForm.email" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="editForm.phone" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
          <el-divider />
          <template #header><span>修改密码</span></template>
          <el-form :model="pwdForm" label-width="80px">
            <el-form-item label="旧密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" :loading="pwdSaving" @click="changePwd">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Coin } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { updateProfile, changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)
const pwdSaving = ref(false)

const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
})

onMounted(() => {
  userStore.loadFromStorage()
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  editForm.nickname = userStore.user?.nickname || ''
  editForm.email = userStore.user?.email || ''
  editForm.phone = userStore.user?.phone || ''
})

async function saveProfile() {
  saving.value = true
  try {
    await updateProfile({
      nickname: editForm.nickname || undefined,
      email: editForm.email || undefined,
      phone: editForm.phone || undefined,
    })
    await userStore.fetchProfile()
    ElMessage.success('保存成功')
  } catch { /* ignore */ }
  finally { saving.value = false }
}

async function changePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  pwdSaving.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch { /* ignore */ }
  finally { pwdSaving.value = false }
}
</script>

<style scoped>
.page-header { background: white; border-bottom: 1px solid #e4e7ed; }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 22px; font-weight: 700; color: #409eff; }
.nav { display: flex; gap: 24px; }
.nav a { color: #606266; }
.nav a.active, .nav a:hover { color: #409eff; }
.main { max-width: 1000px; margin: 0 auto; padding: 24px 20px; }
.profile-layout { display: grid; grid-template-columns: 320px 1fr; gap: 24px; }
.profile-card { text-align: center; }
.avatar-section { padding: 16px 0; }
.avatar-section h3 { margin-top: 12px; font-size: 18px; }
.coins { margin-top: 8px; color: #e6a23c; font-size: 14px; }
.info-list { text-align: left; }
.info-item { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #606266; border-bottom: 1px solid #f5f5f5; }
.info-item:last-child { border: none; }
</style>
