<template>
  <div style="max-width:380px;margin:60px auto;">
    <el-card>
      <el-tabs v-model="tab">
        <el-tab-pane label="登录" name="login">
          <el-form @submit.prevent="login">
            <el-form-item label="用户名">
              <el-input v-model="username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="password" type="password" />
            </el-form-item>
            <el-button type="primary" style="width:100%" @click="login">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form @submit.prevent="register">
            <el-form-item label="用户名">
              <el-input v-model="regUser" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="regPass" type="password" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="regRole" style="width:100%">
                <el-option label="学生(STUDENT)" value="STUDENT" />
                <el-option label="教师(INSTRUCTOR)" value="INSTRUCTOR" />
                <el-option label="管理员(ADMIN)" value="ADMIN" />
              </el-select>
            </el-form-item>
            <el-button type="success" style="width:100%" @click="register">注册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
  </template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import client from '../api/client'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const store = useAuthStore()
const tab = ref<'login'|'register'>('login')
const username = ref('')
const password = ref('')
const regUser = ref('')
const regPass = ref('')
const regRole = ref<'STUDENT'|'INSTRUCTOR'|'ADMIN'>('STUDENT')

async function login() {
  const res = await client.post('/auth/login', { username: username.value, password: password.value })
  store.setToken(res.data.token, res.data.role)
  router.push('/dashboard')
}

async function register() {
  await client.post('/auth/register', { username: regUser.value, password: regPass.value, role: regRole.value })
  const res = await client.post('/auth/login', { username: regUser.value, password: regPass.value })
  store.setToken(res.data.token, res.data.role)
  router.push('/dashboard')
}
</script>
