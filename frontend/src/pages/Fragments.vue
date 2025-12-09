<template>
  <div style="padding:24px;">
    <el-page-header content="片段列表" @back="goBack" />
    <el-table :data="fragments" style="width:100%;margin-top:12px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="pageRange" label="页码" width="120" />
      <el-table-column prop="text" label="文本" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const fragments = ref<any[]>([])
const loading = ref(false)

function goBack(){
  router.back()
}

onMounted(async () => {
  loading.value = true
  const docId = route.params.id
  const res = await client.get(`/documents/${docId}/fragments`)
  fragments.value = res.data
  loading.value = false
})
</script>
