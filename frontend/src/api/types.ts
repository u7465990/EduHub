// ============================================================
// API 类型定义
// ============================================================

/** 统一响应结构 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/** 分页结果 */
export interface PageResult<T> {
  page: number
  pageSize: number
  total: number
  totalPages: number
  list: T[]
}

/** 用户信息 */
export interface UserProfile {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  role: string
  status: number
  coins: number
  lastLoginAt: string
  createdAt: string
}

/** 题目 */
export interface Question {
  id: number
  categoryId: number
  userId: number
  title: string
  content: string
  contentImg: string
  answer: string
  difficulty: number
  subject: string
  grade: string
  type: string
  options: string
  viewCount: number
  likeCount: number
  status: number
  createdAt: string
  updatedAt: string
}

/** 分类 */
export interface Category {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  createdAt: string
}

/** 答题记录 */
export interface UserAnswer {
  id: number
  userId: number
  questionId: number
  userAnswer: string
  isCorrect: number
  aiScore: number
  aiFeedback: string
  answeredAt: string
}
