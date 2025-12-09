<template>
  <div style="padding:24px;">
    <el-page-header content="已上传文档" @back="goBack" />
    <div style="margin:12px 0; display:flex; gap:8px;">
      <el-input v-model="kw" placeholder="按文件名筛选" style="width:260px;" />
      <el-button type="primary" @click="load">刷新</el-button>
    </div>
    <el-table :data="filtered" v-loading="loading">
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="filename" label="文件名" />
      <el-table-column prop="status" label="状态" width="140" />
      <el-table-column prop="createdAt" label="创建时间" width="220" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="useDoc(row.id)">使用此文档</el-button>
          <el-button size="small" @click="viewFragments(row.id)">查看片段</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
  </template>

<script setup lang="ts">
import client from '../api/client'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const kw = ref('')

function goBack(){
  if (window.history.length > 1) router.back(); else router.push('/dashboard')
}

async function load(){
  loading.value = true
  const res = await client.get('/documents')
  list.value = Array.isArray(res.data) ? res.data : []
  loading.value = false
}

function useDoc(id:number){
  try { localStorage.setItem('lastDocumentId', String(id)) } catch {}
  router.push({ path: '/pdf-upload', query: { docId: String(id) } })
}

function viewFragments(id:number){
  router.push(`/fragments/${id}`)
}

const filtered = computed(() => {
  const k = kw.value.trim().toLowerCase()
  if (!k) return list.value
  return list.value.filter(d => String(d.filename || '').toLowerCase().includes(k))
})

onMounted(load)
</script>
