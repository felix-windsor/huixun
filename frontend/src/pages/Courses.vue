<template>
  <div style="padding:24px;">
    <el-page-header content="课程管理" @back="goBack" />
    <div style="margin:12px 0; display:flex; gap:8px;">
      <el-input v-model="name" placeholder="课程名称" style="width:240px;" />
      <el-input v-model="desc" placeholder="描述" style="width:360px;" />
      <el-button type="primary" @click="create">创建</el-button>
    </div>
    <el-table :data="courses" style="width:100%;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" @click="toQuiz(row.id)">测评</el-button>
          <el-button size="small" @click="toQueryQuiz(row)">按查询生成</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const courses = ref<any[]>([])
const name = ref('')
const desc = ref('')

async function load(){
  const res = await client.get('/courses')
  courses.value = res.data
}

async function create(){
  await client.post('/courses', { name: name.value, description: desc.value })
  name.value = ''
  desc.value = ''
  load()
}

async function remove(id:number){
  await client.delete(`/courses/${id}`)
  load()
}

function toQuiz(id:number){
  router.push(`/quiz/${id}`)
}

function toQueryQuiz(row:any){
  const q = row.name || ''
  const t = (row.description || '').split(/[,;\s]+/).filter((x:string)=>x).join(',')
  router.push({ path: `/quiz/${row.id}`, query: { q, tags: t, auto: '1' } })
}

function goBack(){
  if (window.history.length > 1) router.back(); else router.push('/dashboard')
}

onMounted(load)
</script>
