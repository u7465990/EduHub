import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserProfile } from '@/api/auth'
import type { UserProfile } from '@/api/types'

/**
 * 用户状态管理
 *
 * 面试考点：Pinia (Vue3 状态管理)
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<UserProfile | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchProfile() {
    try {
      const res = await getUserProfile()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return res.data
    } catch (e) {
      clearToken()
      return null
    }
  }

  function loadFromStorage() {
    const saved = localStorage.getItem('user')
    if (saved) {
      try { user.value = JSON.parse(saved) } catch { /* ignore */ }
    }
  }

  return {
    token, user, isLoggedIn, isAdmin,
    setToken, clearToken, fetchProfile, loadFromStorage,
  }
})
