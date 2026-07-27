<template>
  <div>
    <header class="page-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">EduHub</router-link>
        <nav class="nav">
          <router-link to="/home">首页</router-link>
          <router-link to="/questions">题库</router-link>
        </nav>
      </div>
    </header>

    <main class="main" v-loading="loading">
      <div v-if="question" class="detail-container">
        <!-- 题目信息 -->
        <el-card class="question-card">
          <div class="q-header">
            <el-tag :type="difficultyType" size="small">
              {{ ['', '简单', '中等', '困难'][question.difficulty] }}
            </el-tag>
            <el-tag type="info" size="small" v-if="question.subject">{{ question.subject }}</el-tag>
            <span class="q-id">#{{ question.id }}</span>
          </div>

          <h1 class="q-title">{{ question.title || '题目' }}</h1>
          <div class="q-content" v-html="renderContent(question.content)"></div>

          <div class="q-stats">
            <span><el-icon><View /></el-icon> {{ question.viewCount }} 次浏览</span>
            <span><el-icon><GoodsFilled /></el-icon> {{ question.likeCount }}</span>
            <el-button
              :type="isFavorited ? 'warning' : 'default'"
              :icon="Star"
              size="small"
              @click="toggleFavorite"
            >
              {{ isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button
              :type="'default'"
              :icon="GoodsFilled"
              size="small"
              @click="handleLike"
            >
              点赞
            </el-button>
          </div>
        </el-card>

        <!-- 答案区域 -->
        <el-card class="answer-card" v-if="question.answer">
          <template #header>
            <span><el-icon><Reading /></el-icon> 参考答案</span>
          </template>
          <div class="answer-content" v-html="renderContent(question.answer)"></div>
        </el-card>

        <!-- 提交答案 -->
        <el-card class="submit-card" v-if="userStore.isLoggedIn">
          <template #header>
            <span><el-icon><Edit /></el-icon> 提交你的答案</span>
          </template>
          <el-input
            v-model="userAnswer"
            type="textarea"
            :rows="4"
            placeholder="输入你的答案..."
          />
          <el-button type="primary" style="margin-top:12px" @click="submitAnswer">
            提交
          </el-button>
        </el-card>

        <!-- 答题记录 -->
        <el-card class="history-card" v-if="answerHistory.length > 0">
          <template #header>
            <span><el-icon><Timer /></el-icon> 答题记录</span>
          </template>
          <div v-for="a in answerHistory" :key="a.id" class="history-item">
            <div class="answer-text">你的答案：{{ a.userAnswer }}</div>
            <div v-if="a.isCorrect !== null" class="result-tag">
              <el-tag :type="a.isCorrect ? 'success' : 'danger'" size="small">
                {{ a.isCorrect ? '正确' : '错误' }}
              </el-tag>
            </div>
            <div class="answer-date">{{ a.answeredAt?.slice(0, 16) }}</div>
          </div>
        </el-card>
      </div>

      <div v-else-if="!loading" class="empty">
        <el-empty description="题目不存在" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { View, GoodsFilled, Star, Reading, Edit, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getQuestionDetail, likeQuestion } from '@/api/questions'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorites'
import { useUserStore } from '@/stores/user'
import type { Question, UserAnswer } from '@/api/types'
import http from '@/api/request'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const question = ref<Question | null>(null)
const isFavorited = ref(false)
const userAnswer = ref('')
const answerHistory = ref<UserAnswer[]>([])

const difficultyType = computed(() => {
  const map: Record<number, 'success' | 'warning' | 'danger'> = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[question.value?.difficulty || 2] || 'info'
})

onMounted(async () => {
  userStore.loadFromStorage()
  const id = Number(route.params.id)
  if (!id) return
  try {
    const res = await getQuestionDetail(id)
    question.value = res.data
    if (userStore.isLoggedIn) {
      const favRes = await checkFavorite(id).catch(() => null)
      if (favRes) isFavorited.value = favRes.data.favorited
    }
  } catch {
    question.value = null
  } finally {
    loading.value = false
  }
})

function renderContent(text: string | undefined) {
  if (!text) return ''
  // 简单转义和换行处理
  return text
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br/>')
}

async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await likeQuestion(question.value!.id)
    question.value!.likeCount++
    ElMessage.success('点赞成功')
  } catch { /* ignore */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isFavorited.value) {
      await removeFavorite(question.value!.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(question.value!.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch { /* ignore */ }
}

async function submitAnswer() {
  if (!userAnswer.value.trim()) {
    ElMessage.warning('请输入答案')
    return
  }
  try {
    const res = await http.post('/api/answers', {
      questionId: question.value!.id,
      userAnswer: userAnswer.value,
    })
    // 添加到历史记录
    answerHistory.value.unshift(res.data)
    userAnswer.value = ''
    ElMessage.success('提交成功')
  } catch { /* ignore */ }
}
</script>

<style scoped>
.page-header { background: white; border-bottom: 1px solid #e4e7ed; }
.header-inner { max-width: 900px; margin: 0 auto; padding: 0 20px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 22px; font-weight: 700; color: #409eff; }
.nav { display: flex; gap: 24px; }
.nav a { color: #606266; }
.nav a:hover, .nav a.router-link-exact-active { color: #409eff; }

.main { max-width: 900px; margin: 0 auto; padding: 24px 20px; min-height: calc(100vh - 60px); }
.detail-container { display: flex; flex-direction: column; gap: 20px; }
.q-header { display: flex; gap: 8px; align-items: center; margin-bottom: 16px; }
.q-id { margin-left: auto; color: #909399; font-size: 13px; }
.q-title { font-size: 24px; color: #303133; margin-bottom: 16px; }
.q-content { font-size: 16px; line-height: 1.8; color: #303133; padding: 16px 0; }
.q-stats { display: flex; align-items: center; gap: 16px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f0f0f0; color: #909399; font-size: 14px; }
.q-stats .el-button { margin-left: auto; }

.answer-content { font-size: 15px; line-height: 1.8; color: #303133; padding: 8px 0; }

.history-item { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.history-item:last-child { border: none; }
.answer-text { flex: 1; font-size: 14px; color: #606266; }
.answer-date { font-size: 12px; color: #c0c4cc; }

.empty { padding: 100px 0; }
</style>
