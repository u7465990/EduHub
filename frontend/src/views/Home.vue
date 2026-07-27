<template>
  <div>
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-inner">
        <router-link to="/home" class="logo">EduHub</router-link>
        <nav class="nav">
          <router-link to="/home">首页</router-link>
          <router-link to="/questions">题库</router-link>
          <router-link to="/questions/create" v-if="userStore.isLoggedIn">发布题目</router-link>
        </nav>
        <div class="user-area">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/favorites" class="header-link">
              <el-icon><Star /></el-icon> 收藏
            </router-link>
            <el-dropdown>
              <span class="user-info">
                <el-avatar :size="32" :icon="UserFilled" />
                <span class="username">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="header-link">登录</router-link>
            <router-link to="/register" class="register-btn">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <!-- 搜索区域 -->
    <section class="hero">
      <div class="hero-content">
        <h1>搜题 · 解题 · 学习</h1>
        <p>智能作业搜题平台，AI 助力高效学习</p>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            size="large"
            placeholder="输入题目关键词搜索..."
            :prefix-icon="Search"
            class="search-input"
            @keyup.enter="doSearch"
          />
          <el-button type="primary" size="large" @click="doSearch" class="search-btn">搜索</el-button>
        </div>
        <div class="hot-tags" v-if="categories.length">
          <span class="tag-label">热门学科：</span>
          <el-tag
            v-for="cat in categories"
            :key="cat.id"
            :type="selectedSubject === cat.name ? 'primary' : 'info'"
            class="hot-tag"
            @click="searchBySubject(cat.name)"
          >
            {{ cat.name }}
          </el-tag>
        </div>
      </div>
    </section>

    <!-- 推荐题目 -->
    <main class="main">
      <section class="section">
        <div class="section-header">
          <h2>{{ searchKeyword ? '搜索结果' : '每日推荐' }}</h2>
          <router-link to="/questions" class="more">查看更多 →</router-link>
        </div>

        <el-skeleton :loading="loading" animated :count="4" v-if="loading">
          <template #template>
            <div style="padding: 14px">
              <el-skeleton-item variant="h3" style="width: 50%; margin-bottom: 10px" />
              <el-skeleton-item variant="text" style="margin-bottom: 6px" />
              <el-skeleton-item variant="text" />
            </div>
          </template>
        </el-skeleton>

        <div v-else-if="questions.length === 0" class="empty">
          <el-empty description="暂无题目" />
        </div>

        <div v-else class="question-grid">
          <el-card
            v-for="q in questions"
            :key="q.id"
            class="question-card"
            shadow="hover"
            @click="router.push(`/questions/${q.id}`)"
          >
            <div class="card-header">
              <el-tag size="small" :type="difficultyType(q.difficulty)">
                {{ ['', '简单', '中等', '困难'][q.difficulty] }}
              </el-tag>
              <el-tag size="small" type="info" v-if="q.subject">{{ q.subject }}</el-tag>
            </div>
            <h3 class="card-title">{{ q.title || q.content.slice(0, 60) + '...' }}</h3>
            <p class="card-excerpt">{{ q.content.slice(0, 100) }}{{ q.content.length > 100 ? '...' : '' }}</p>
            <div class="card-footer">
              <span><el-icon><View /></el-icon> {{ q.viewCount }}</span>
              <span><el-icon><GoodsFilled /></el-icon> {{ q.likeCount }}</span>
              <span class="date">{{ formatDate(q.createdAt) }}</span>
            </div>
          </el-card>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>EduHub 智能作业搜题平台 &copy; 2024</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, View, GoodsFilled, UserFilled, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRandomQuestions, searchQuestions } from '@/api/questions'
import { useUserStore } from '@/stores/user'
import type { Question } from '@/api/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const questions = ref<Question[]>([])
const searchKeyword = ref('')
const categories = ref<{ id: number; name: string }[]>([
  { id: 1, name: '数学' }, { id: 2, name: '语文' }, { id: 3, name: '英语' },
  { id: 4, name: '物理' }, { id: 5, name: '化学' }, { id: 6, name: '生物' },
])
const selectedSubject = ref('')

onMounted(() => {
  userStore.loadFromStorage()
  loadQuestions()
})

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getRandomQuestions(12)
    questions.value = res.data || []
  } catch {
    questions.value = []
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  if (!searchKeyword.value.trim()) {
    loadQuestions()
    return
  }
  loading.value = true
  try {
    const res = await searchQuestions({ keyword: searchKeyword.value, pageSize: 12 })
    questions.value = res.data.list || []
  } catch {
    questions.value = []
  } finally {
    loading.value = false
  }
}

function searchBySubject(subject: string) {
  selectedSubject.value = selectedSubject.value === subject ? '' : subject
  const query = selectedSubject.value ? { subject: selectedSubject.value } : {}
  router.push({ path: '/questions', query })
}

function difficultyType(d: number): 'success' | 'warning' | 'danger' {
  return ['success', 'warning', 'danger'][d - 1] || 'info'
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

function handleLogout() {
  userStore.clearToken()
  ElMessage.success('已退出')
  router.push('/home')
}
</script>

<style scoped>
.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 22px;
  font-weight: 700;
  color: #409eff;
}
.nav { display: flex; gap: 24px; }
.nav a { color: #606266; font-size: 15px; }
.nav a:hover, .nav a.router-link-exact-active { color: #409eff; }
.user-area { display: flex; align-items: center; gap: 16px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { color: #303133; font-size: 14px; }
.header-link { color: #606266; font-size: 14px; }
.header-link:hover { color: #409eff; }
.register-btn {
  background: #409eff;
  color: white;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 14px;
}
.register-btn:hover { background: #66b1ff; }

.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 80px 20px 60px;
  text-align: center;
  color: white;
}
.hero-content { max-width: 700px; margin: 0 auto; }
.hero h1 { font-size: 42px; margin-bottom: 12px; }
.hero p { font-size: 18px; opacity: 0.9; margin-bottom: 32px; }
.search-box { display: flex; gap: 12px; justify-content: center; }
.search-input { max-width: 500px; }
.search-btn { min-width: 100px; }
.hot-tags { margin-top: 20px; display: flex; justify-content: center; align-items: center; gap: 8px; flex-wrap: wrap; }
.tag-label { opacity: 0.9; }
.hot-tag { cursor: pointer; }

.main { max-width: 1200px; margin: 0 auto; padding: 40px 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-header h2 { font-size: 22px; color: #303133; }
.more { color: #409eff; font-size: 14px; }

.question-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.question-card { cursor: pointer; }
.question-card:hover { transform: translateY(-2px); transition: all 0.3s; }
.card-header { display: flex; gap: 8px; margin-bottom: 10px; }
.card-title { font-size: 16px; color: #303133; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-excerpt { font-size: 14px; color: #909399; line-height: 1.6; max-height: 44px; overflow: hidden; }
.card-footer { display: flex; gap: 16px; margin-top: 12px; font-size: 13px; color: #909399; }
.card-footer .date { margin-left: auto; }
.empty { padding: 60px 0; }

.footer { text-align: center; padding: 30px; color: #909399; font-size: 14px; border-top: 1px solid #e4e7ed; }
</style>
