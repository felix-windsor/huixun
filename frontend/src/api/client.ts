import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const envBase = (import.meta as any).env?.VITE_API_BASE
let baseURL = envBase
if (!baseURL) {
  const host = typeof window !== 'undefined' ? window.location.hostname : ''
  if (host === 'localhost') baseURL = 'http://localhost:8080/api'
  else baseURL = '/api'
}

const client = axios.create({
  baseURL,
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
    const code = err?.code
    const msg = typeof data === 'string' ? data : (data?.message || err?.message || '请求失败')
    if (!status) {
      if (code === 'ERR_NETWORK') {
        ElMessage.error('网络不可达或后端未启动')
      } else {
        ElMessage.error(msg)
      }
      return Promise.reject(err)
    }
    if (status === 401) {
      try { useAuthStore().$reset() } catch {}
      const path = typeof window !== 'undefined' ? window.location.pathname : ''
      if (path.includes('/login')) {
        ElMessage.error('用户名或密码不正确')
      } else {
        ElMessage.error('未认证或登录已过期，请重新登录')
        window.location.href = '/'
      }
    } else if (status === 404) {
      ElMessage.warning('资源不存在或已删除')
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default client
