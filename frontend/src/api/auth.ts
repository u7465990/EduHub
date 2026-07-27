/**
 * 认证相关 API
 */
import http from './request'
import type { ApiResult, UserProfile } from './types'

export function login(data: { username: string; password: string }) {
  return http.post<any, ApiResult<{ token: string; user: UserProfile }>>('/api/auth/login', data)
}

export function register(data: { username: string; password: string; nickname?: string; email?: string }) {
  return http.post<any, ApiResult<UserProfile>>('/api/auth/register', data)
}

export function getUserProfile() {
  return http.get<any, ApiResult<UserProfile>>('/api/user/profile')
}

export function updateProfile(data: Partial<UserProfile>) {
  return http.put<any, ApiResult<void>>('/api/user/profile', data)
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return http.put<any, ApiResult<void>>('/api/user/password', data)
}
