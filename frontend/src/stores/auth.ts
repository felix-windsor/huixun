import { defineStore } from 'pinia'

export const useAuthStore = defineStore<'auth', { token: string; role: string }, {}, { setToken(t: string, r: string): void }>('auth', {
  state: () => ({ token: '', role: '' }),
  actions: {
    setToken(t: string, r: string) { this.token = t; this.role = r }
  }
})
