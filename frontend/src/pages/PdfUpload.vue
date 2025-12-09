<template>
  <div style="padding:24px;">
    <el-page-header content="PDF 上传" @back="goBack" />
    <el-upload :http-request="doUpload" accept="application/pdf" :show-file-list="true">
      <el-button type="primary">选择 PDF</el-button>
    </el-upload>
    <div v-if="documentId" style="margin-top:12px;">
      <el-alert type="success" :title="'已上传，文档ID：' + documentId" show-icon />
      <el-button style="margin-top:8px;" @click="startParse" type="primary" :disabled="parsing">{{ parsing ? '正在解析...' : '开始解析' }}</el-button>
      <div style="margin-top:8px;">状态：{{ statusDisplay }}</div>
      <div v-if="parsing" style="margin-top:8px;">
        <el-progress :percentage="progress" :status="progressStatus" :stroke-width="14" :striped="true" :striped-flow="true" />
        <div style="margin-top:8px;color:#6b7280;">{{ progressText }}</div>
      </div>
      <el-button v-if="status==='DONE'" style="margin-top:8px;" @click="viewFragments">查看片段</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const documentId = ref<number>()
const status = ref('')
const parsing = ref(false)
const progress = ref(0)
const progressStatus = ref<'success' | 'exception' | undefined>(undefined)
const progressText = ref('')
const statusDisplay = ref('')

function goBack(){
  if (window.history.length > 1) router.back(); else router.push('/dashboard')
}

async function doUpload(options:any){
  const form = new FormData()
  form.append('file', options.file as File)
  const res = await client.post('/documents/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  documentId.value = res.data.documentId
  pollStatus()
}

async function startParse(){
  if(!documentId.value) return
  parsing.value = true
  progress.value = 10
  progressStatus.value = undefined
  progressText.value = '正在解析PDF内容...'
  statusDisplay.value = '解析中'
  pollStatus()
  client.post(`/parse/${documentId.value}`, undefined, { timeout: 0 }).catch(() => {})
}

async function pollStatus(){
  if(!documentId.value) return
  const res = await client.get(`/documents/${documentId.value}/status`)
  status.value = res.data.status
  if(status.value === 'PARSING') {
    parsing.value = true
    statusDisplay.value = '解析中'
    progressText.value = '正在解析PDF内容...'
    if (progress.value < 60) progress.value = Math.min(60, progress.value + 5)
  } else if(status.value === 'EMBEDDING') {
    parsing.value = true
    statusDisplay.value = '嵌入向量中'
    progressText.value = '正在生成向量并写入数据库...'
    if (progress.value < 95) progress.value = Math.max(65, Math.min(95, progress.value + 3))
  } else if(status.value === 'DONE') {
    parsing.value = false
    statusDisplay.value = '完成'
    progressText.value = '解析完成'
    progress.value = 100
    progressStatus.value = 'success'
  } else if(status.value === 'FAILED') {
    parsing.value = false
    statusDisplay.value = '失败'
    progressStatus.value = 'exception'
    progressText.value = '解析失败，请重试或检查文件'
  }
  if(status.value !== 'DONE' && status.value !== 'FAILED') setTimeout(pollStatus, 1200)
}

function viewFragments(){
  router.push(`/fragments/${documentId.value}`)
}
</script>
