import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const client = axios.create({
  baseURL: (import.meta as any).env?.VITE_API_BASE || '/api',
  timeout: 10000
})

client.interceptors.request.use(config => {
  const store = useAuthStore()
  if (store.token) {
    if (!config.headers) config.headers = new axios.AxiosHeaders()
    config.headers.set('Authorization', `Bearer ${store.token}`)
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status
    const data = err?.response?.data
    const msg = typeof data === 'string' ? data : (data?.message || err?.message || '请求失败')
    if (status === 401) {
      try { useAuthStore().$reset() } catch {}
      ElMessage.error('未认证或登录已过期，请重新登录')
      window.location.href = '/'
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default client
