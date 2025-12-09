<template>
  <div style="padding:24px;">
    <el-page-header content="题库编辑" />
    <el-table :data="questions" v-loading="loading" style="margin-top:12px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="题干">
        <template #default="{row}"><el-input v-model="row.stem" /></template>
      </el-table-column>
      <el-table-column label="选项">
        <template #default="{row}"><el-input v-model="row.optionsJson" /></template>
      </el-table-column>
      <el-table-column label="答案" width="160">
        <template #default="{row}"><el-input v-model="row.answer" /></template>
      </el-table-column>
      <el-table-column label="难度" width="160">
        <template #default="{row}"><el-select v-model="row.difficulty"><el-option label="EASY" value="EASY"/><el-option label="MEDIUM" value="MEDIUM"/><el-option label="HARD" value="HARD"/></el-select></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="save(row)">保存</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const quizId = Number(route.params.quizId)
const questions = ref<any[]>([])
const loading = ref(false)

async function load(){
  loading.value = true
  const res = await client.get(`/quizzes/${quizId}/questions`)
  questions.value = res.data
  loading.value = false
}

async function save(row:any){
  await client.put(`/questions/${row.id}`, row)
}

async function remove(id:number){
  await client.delete(`/questions/${id}`)
  load()
}

onMounted(load)
</script>
