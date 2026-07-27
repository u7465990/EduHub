/**
 * 题目相关 API
 */
import http from './request'
import type { ApiResult, PageResult, Question } from './types'

// 获取题目列表
export function getQuestions(params: {
  page?: number
  pageSize?: number
  subject?: string
  difficulty?: number
  keyword?: string
  sortBy?: string
}) {
  return http.get<any, ApiResult<PageResult<Question>>>('/api/questions', { params })
}

// 搜索题目
export function searchQuestions(params: {
  keyword: string
  page?: number
  pageSize?: number
}) {
  return http.get<any, ApiResult<PageResult<Question>>>('/api/questions/search', { params })
}

// 获取题目详情
export function getQuestionDetail(id: number) {
  return http.get<any, ApiResult<Question>>(`/api/questions/${id}`)
}

// 创建题目
export function createQuestion(data: {
  title?: string
  content: string
  answer?: string
  subject?: string
  difficulty?: number
  type?: string
  categoryId?: number
}) {
  return http.post<any, ApiResult<Question>>('/api/questions', data)
}

// 点赞
export function likeQuestion(id: number) {
  return http.post<any, ApiResult<void>>(`/api/questions/${id}/like`)
}

// 随机题目
export function getRandomQuestions(limit = 10) {
  return http.get<any, ApiResult<Question[]>>('/api/questions/random', { params: { limit } })
}
