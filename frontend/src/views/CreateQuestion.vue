<template>
  <div>
    <header class="page-header">
      <div class="header-inner">
        <router-link to="/home" class="logo">EduHub</router-link>
        <nav class="nav">
          <router-link to="/home">首页</router-link>
          <router-link to="/questions">题库</router-link>
          <router-link to="/questions/create" class="active">发布题目</router-link>
        </nav>
      </div>
    </header>

    <main class="main">
      <el-card style="max-width: 800px; margin: 0 auto">
        <template #header>
          <span>发布新题目</span>
        </template>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="题目标题（选填）" />
          </el-form-item>

          <el-form-item label="学科" prop="subject">
            <el-select v-model="form.subject" placeholder="选择学科" style="width: 200px">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.name.toLowerCase()" />
            </el-select>
          </el-form-item>

          <el-form-item label="题型" prop="type">
            <el-radio-group v-model="form.type">
              <el-radio value="choice">选择题</el-radio>
              <el-radio value="fill">填空题</el-radio>
              <el-radio value="judge">判断题</el-radio>
              <el-radio value="essay">解答题</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="难度" prop="difficulty">
            <el-rate v-model="form.difficulty" :colors="['#67c23a', '#e6a23c', '#f56c6c']" show-text :texts="['简单', '中等', '困难']" />
          </el-form-item>

          <el-form-item label="题目" prop="content">
            <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入题目内容，支持 LaTeX 公式" />
          </el-form-item>

          <el-form-item label="答案" prop="answer">
            <el-input v-model="form.answer" type="textarea" :rows="4" placeholder="请输入答案/解析（选填）" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交审核</el-button>
            <el-button @click="router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createQuestion } from '@/api/questions'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive({
  title: '',
  subject: '',
  type: 'essay',
  difficulty: 2,
  content: '',
  answer: '',
})

const rules: FormRules = {
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
}

const categories = [
  { id: 1, name: '数学' }, { id: 2, name: '语文' }, { id: 3, name: '英语' },
  { id: 4, name: '物理' }, { id: 5, name: '化学' }, { id: 6, name: '生物' },
]

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createQuestion({
      title: form.title || undefined,
      content: form.content,
      answer: form.answer || undefined,
      subject: form.subject || undefined,
      difficulty: form.difficulty,
      type: form.type,
    })
    ElMessage.success('提交成功，等待审核')
    router.push('/home')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-header { background: white; border-bottom: 1px solid #e4e7ed; }
.header-inner { max-width: 1200px; margin: 0 auto; padding: 0 20px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.logo { font-size: 22px; font-weight: 700; color: #409eff; }
.nav { display: flex; gap: 24px; }
.nav a { color: #606266; }
.nav a.active, .nav a:hover { color: #409eff; }
.main { max-width: 1200px; margin: 0 auto; padding: 24px 20px; min-height: calc(100vh - 60px); }
</style>
