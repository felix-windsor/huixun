<template>
  <div style="padding:24px;">
    <el-page-header content="PDF 上传" @back="goBack" />
    <div style="margin:8px 0;">
      <el-button @click="goDocuments">已上传文档</el-button>
    </div>
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
      <div v-if="status==='DONE'" style="margin-top:16px; padding:12px; border:1px solid #e5e7eb; border-radius:8px;">
        <div style="font-weight:600; margin-bottom:8px;">出题</div>
        <div style="display:flex; gap:8px; flex-wrap:wrap;">
          <el-select v-model="selectedCourseId" placeholder="选择或创建课程" style="width:260px;" filterable allow-create clearable>
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-input-number v-model="count" :min="1" :max="50" label="题目数量" />
          <el-select v-model="type" placeholder="题型" style="width:180px;">
            <el-option label="单选" value="SINGLE" />
            <el-option label="多选" value="MULTI" />
            <el-option label="判断" value="TRUE_FALSE" />
          </el-select>
          <el-select v-model="difficulty" placeholder="难度" style="width:140px;">
            <el-option label="EASY" value="EASY" />
            <el-option label="MEDIUM" value="MEDIUM" />
            <el-option label="HARD" value="HARD" />
          </el-select>
          <el-button type="primary" :disabled="!selectedCourseId" @click="goGenerate">去生成题目</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const documentId = ref<number>()
const status = ref('')
const parsing = ref(false)
const progress = ref(0)
const progressStatus = ref<'success' | 'exception' | undefined>(undefined)
const progressText = ref('')
const statusDisplay = ref('')
const courses = ref<any[]>([])
const selectedCourseId = ref<number|string>()
const count = ref<number>(5)
const type = ref<string>('SINGLE')
const difficulty = ref<string>('')

function goBack(){
  if (window.history.length > 1) router.back(); else router.push('/dashboard')
}

function goDocuments(){
  router.push('/documents')
}

async function doUpload(options:any){
  const form = new FormData()
  form.append('file', options.file as File)
  const res = await client.post('/documents/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  documentId.value = res.data.documentId
  try { localStorage.setItem('lastDocumentId', String(documentId.value)) } catch {}
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

onMounted(() => {
  if (!documentId.value) {
    const q = route.query
    if (typeof q.docId === 'string') {
      const v = Number(q.docId)
      if (!Number.isNaN(v)) documentId.value = v
    }
    if (!documentId.value) {
      try {
        const s = localStorage.getItem('lastDocumentId')
        if (s) {
          const v = Number(s)
          if (!Number.isNaN(v)) documentId.value = v
        }
      } catch {}
    }
    if (documentId.value) pollStatus()
  }
  loadCourses()
})

async function loadCourses(){
  const res = await client.get('/courses')
  courses.value = res.data || []
}

function goGenerate(){
  if(!selectedCourseId.value || !documentId.value) return
  const go = async (courseId:number) => {
    const params = new URLSearchParams()
    params.set('docId', String(documentId.value))
    params.set('count', String(count.value))
    if (type.value) params.set('type', type.value)
    if (difficulty.value) params.set('difficulty', difficulty.value)
    params.set('auto','1')
    router.push({ path: `/quiz/${courseId}`, query: Object.fromEntries(params.entries()) })
  }
  if (typeof selectedCourseId.value === 'string') {
    const name = selectedCourseId.value.trim()
    if (!name) return
    client.post('/courses', { name, description: '' }).then(res => {
      const id = Number(res.data.id)
      if (!Number.isNaN(id)) go(id)
    })
  } else {
    go(selectedCourseId.value as number)
  }
}
</script>
