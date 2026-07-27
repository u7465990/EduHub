import http from './request'
import type { ApiResult, PageResult, Question } from './types'

export function addFavorite(questionId: number) {
  return http.post<any, ApiResult<void>>(`/api/favorites/${questionId}`)
}

export function removeFavorite(questionId: number) {
  return http.delete<any, ApiResult<void>>(`/api/favorites/${questionId}`)
}

export function checkFavorite(questionId: number) {
  return http.get<any, ApiResult<{ favorited: boolean }>>(`/api/favorites/${questionId}/check`)
}

export function getFavorites(params: { page?: number; pageSize?: number }) {
  return http.get<any, ApiResult<PageResult<Question>>>('/api/favorites', { params })
}
