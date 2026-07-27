<template>
  <div>
    <header class="page-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">EduHub</router-link>
        <nav class="nav">
          <router-link to="/home">首页</router-link>
          <router-link to="/questions">题库</router-link>
          <router-link to="/favorites" class="active">收藏</router-link>
        </nav>
      </div>
    </header>

    <main class="main">
      <h2 style="margin-bottom: 20px">我的收藏</h2>

      <el-skeleton :loading="loading" animated :count="4" v-if="loading" />

      <div v-else-if="list.length === 0">
        <el-empty description="还没有收藏题目">
          <router-link to="/questions">
            <el-button type="primary">去题库看看</el-button>
          </router-link>
        </el-empty>
      </div>

      <div v-else class="q-list">
        <el-card v-for="q in list" :key="q.id" class="q-item" shadow="hover" @click="router.push(`/questions/${q.id}`)">
          <h3>{{ q.title || q.content.slice(0, 80) }}</h3>
          <div class="q-meta">
            <el-tag size="small" v-if="q.subject">{{ q.subject }}</el-tag>
            <span>{{ q.viewCount }} 浏览</span>
            <el-button type="danger" link size="small" @click.stop="handleRemove(q.id)">取消收藏</el-button>
          </div>
        </el-card>
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          :page-size="20"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadFavorites"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api/favorites'
import type { Question } from '@/api/types'

const router = useRouter()
const loading = ref(true)
const list = ref<Question[]>([])
const page = ref(1)
const total = ref(0)

onMounted(() => loadFavorites())

async function loadFavorites() {
  loading.value = true
  try {
    const res = await getFavorites({ page: page.value, pageSize: 20 })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function handleRemove(questionId: number) {
  try {
    await removeFavorite(questionId)
    list.value = list.value.filter(q => q.id !== questionId)
    total.value--
    ElMessage.success('已取消收藏')
  } catch { /* ignore */ }
}
</script>

<style scoped>
.page-header { background: white; border-bottom: 1px solid #e4e7ed; }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 22px; font-weight: 700; color: #409eff; }
.nav { display: flex; gap: 24px; }
.nav a { color: #606266; }
.nav a.active, .nav a:hover { color: #409eff; }
.main { max-width: 1000px; margin: 0 auto; padding: 24px 20px; min-height: calc(100vh - 60px); }
.q-list { display: flex; flex-direction: column; gap: 12px; }
.q-item { cursor: pointer; }
.q-item h3 { font-size: 16px; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.q-meta { display: flex; align-items: center; gap: 12px; font-size: 13px; color: #909399; }
.pagination { margin-top: 24px; display: flex; justify-content: center; }
</style>
