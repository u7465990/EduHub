<template>
  <div>
    <header class="page-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">EduHub</router-link>
        <nav class="nav">
          <router-link to="/home">首页</router-link>
          <router-link to="/questions" class="active">题库</router-link>
          <router-link to="/questions/create" v-if="userStore.isLoggedIn">发布题目</router-link>
        </nav>
        <div class="user-area">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/favorites"><el-icon><Star /></el-icon></router-link>
            <el-dropdown>
              <span class="user-info">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              <template #dropdown>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出</el-dropdown-item>
              </template>
            </el-dropdown>
          </template>
          <router-link to="/login" v-else>登录</router-link>
        </div>
      </div>
    </header>

    <main class="main">
      <!-- 筛选栏 -->
      <el-card class="filter-card" shadow="never">
        <el-form :model="query" inline size="default">
          <el-form-item label="学科">
            <el-select v-model="query.subject" placeholder="全部" clearable style="width:120px">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="query.difficulty" placeholder="全部" clearable style="width:100px">
              <el-option label="简单" :value="1" />
              <el-option label="中等" :value="2" />
              <el-option label="困难" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序">
            <el-select v-model="query.sortBy" placeholder="默认" clearable style="width:120px">
              <el-option label="最新" value="created_at" />
              <el-option label="最热" value="view_count" />
              <el-option label="最多点赞" value="like_count" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadQuestions">筛选</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 题目列表 -->
      <el-skeleton :loading="loading" animated :count="6" class="skeleton" v-if="loading" />

      <div v-else-if="questions.length === 0" class="empty">
        <el-empty description="暂无题目" />
      </div>

      <div v-else class="question-list">
        <el-card
          v-for="q in questions"
          :key="q.id"
          class="q-item"
          shadow="hover"
          @click="router.push(`/questions/${q.id}`)"
        >
          <div class="q-meta">
            <el-tag size="small" :type="['success','','danger'][q.difficulty-1] || 'info'">
              {{ ['','简单','中等','困难'][q.difficulty] }}
            </el-tag>
            <el-tag size="small" type="info" v-if="q.subject">{{ q.subject }}</el-tag>
            <el-tag size="small" v-if="q.type === 'choice'">选择题</el-tag>
            <el-tag size="small" v-else-if="q.type === 'fill'">填空题</el-tag>
            <el-tag size="small" v-else-if="q.type === 'judge'">判断题</el-tag>
            <el-tag size="small" v-else>解答题</el-tag>
          </div>
          <h3 class="q-title">{{ q.title || q.content.slice(0, 80) }}</h3>
          <p class="q-excerpt">{{ q.content.slice(0, 120) }}{{ q.content.length > 120 ? '...' : '' }}</p>
          <div class="q-footer">
            <span><el-icon><View /></el-icon> {{ q.viewCount }}</span>
            <span><el-icon><GoodsFilled /></el-icon> {{ q.likeCount }}</span>
            <span class="date">{{ q.createdAt?.slice(0, 10) }}</span>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="20"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { View, GoodsFilled, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getQuestions } from '@/api/questions'
import { useUserStore } from '@/stores/user'
import type { Question } from '@/api/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const questions = ref<Question[]>([])
const currentPage = ref(1)
const total = ref(0)

const query = reactive({
  subject: (route.query.subject as string) || '',
  difficulty: route.query.difficulty ? Number(route.query.difficulty) : undefined,
  sortBy: 'created_at',
})

const categories = [
  { id: 1, name: '数学' }, { id: 2, name: '语文' }, { id: 3, name: '英语' },
  { id: 4, name: '物理' }, { id: 5, name: '化学' }, { id: 6, name: '生物' },
]

onMounted(() => {
  userStore.loadFromStorage()
  if (route.query.subject) {
    query.subject = route.query.subject as string
  }
  loadQuestions()
})

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getQuestions({
      page: currentPage.value,
      pageSize: 20,
      subject: query.subject || undefined,
      difficulty: query.difficulty,
      sortBy: query.sortBy || undefined,
    })
    questions.value = res.data.list
    total.value = res.data.total
  } catch {
    questions.value = []
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  query.subject = ''
  query.difficulty = undefined
  query.sortBy = 'created_at'
  currentPage.value = 1
  loadQuestions()
}

function onPageChange(page: number) {
  currentPage.value = page
  loadQuestions()
  window.scrollTo(0, 0)
}

function handleLogout() {
  userStore.clearToken()
  ElMessage.success('已退出')
  router.push('/home')
}
</script>

<style scoped>
.page-header { background: white; border-bottom: 1px solid #e4e7ed; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 22px; font-weight: 700; color: #409eff; }
.nav { display: flex; gap: 24px; }
.nav a { color: #606266; }
.nav a.active, .nav a:hover { color: #409eff; }
.user-area { display: flex; align-items: center; gap: 16px; }
.user-info { cursor: pointer; color: #606266; }

.main { max-width: 1200px; margin: 0 auto; padding: 24px 20px; }
.filter-card { margin-bottom: 20px; }
.question-list { display: flex; flex-direction: column; gap: 16px; }
.q-item { cursor: pointer; }
.q-meta { display: flex; gap: 8px; margin-bottom: 10px; }
.q-title { font-size: 17px; color: #303133; margin-bottom: 8px; }
.q-excerpt { font-size: 14px; color: #909399; line-height: 1.6; max-height: 44px; overflow: hidden; }
.q-footer { display: flex; gap: 16px; margin-top: 12px; font-size: 13px; color: #909399; }
.q-footer .date { margin-left: auto; }
.empty { padding: 60px 0; }
.pagination { margin-top: 24px; display: flex; justify-content: center; }
.skeleton { padding: 20px; }
</style>
