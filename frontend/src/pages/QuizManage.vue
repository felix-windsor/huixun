<template>
  <div style="padding:24px;">
    <el-page-header content="测评生成" />
    <div style="margin:12px 0; display:flex; gap:8px;">
      <el-input v-model="title" placeholder="测评标题" style="width:240px;" />
      <el-input v-model="desc" placeholder="描述" style="width:360px;" />
      <el-input v-model.number="documentId" placeholder="文档ID" style="width:160px;" />
      <el-input v-model.number="count" placeholder="题目数量" style="width:140px;" />
      <el-button type="primary" @click="createQuiz">创建并生成</el-button>
    </div>
    <el-table :data="questions" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="difficulty" label="难度" width="120" />
      <el-table-column prop="stem" label="题干" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const courseId = Number(route.params.id)
const title = ref('测评')
const desc = ref('')
const documentId = ref<number>()
const count = ref<number>(5)
const quizId = ref<number>()
const questions = ref<any[]>([])
const loading = ref(false)

async function createQuiz(){
  const res = await client.post('/quizzes', { course: { id: courseId }, title: title.value, description: desc.value })
  quizId.value = res.data.id
  await generate()
}

async function generate(){
  if(!quizId.value || !documentId.value) return
  loading.value = true
  const res = await client.post(`/quizzes/${quizId.value}/generate?documentId=${documentId.value}&count=${count.value}`)
  questions.value = res.data
  loading.value = false
}
</script>
