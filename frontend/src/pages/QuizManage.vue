<template>
  <div style="padding:24px;">
    <el-page-header content="测评生成" @back="goBack" />
    <div style="margin:12px 0; display:flex; gap:8px;">
      <el-input v-model="title" placeholder="测评标题" style="width:240px;" />
      <el-input v-model="desc" placeholder="描述" style="width:360px;" />
      <el-input v-model.number="documentId" placeholder="文档ID" style="width:160px;" />
      <el-input v-model.number="count" placeholder="题目数量" style="width:140px;" />
      <el-button type="primary" @click="createAndGenerate">创建并生成</el-button>
    </div>
    <div style="margin:12px 0; display:flex; gap:8px; align-items:center;">
      <el-select v-model="typeSel" placeholder="题型" style="width:160px;">
        <el-option label="单选" value="SINGLE" />
        <el-option label="多选" value="MULTI" />
        <el-option label="判断" value="TRUE_FALSE" />
      </el-select>
      <el-select v-model="difficulty" placeholder="难度" style="width:140px;">
        <el-option label="EASY" value="EASY" />
        <el-option label="MEDIUM" value="MEDIUM" />
        <el-option label="HARD" value="HARD" />
      </el-select>
      <span v-if="typeSel==='MULTI'" style="color:#909399;">多选题预计正确项数量：{{ desiredMultiCount(difficulty || 'MEDIUM') }}</span>
      <el-select v-model="minQuality" placeholder="质量筛选" style="width:140px;">
        <el-option label="不限" value="ALL" />
        <el-option label="≥60" :value="60" />
        <el-option label="≥80" :value="80" />
      </el-select>
      <el-button @click="generateByQuery" :disabled="!quizId || !documentId" >重新生成</el-button>
    </div>
    <el-table :data="viewQuestions" :row-class-name="tableRowClass" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="difficulty" label="难度" width="120" />
      <el-table-column label="质量分" width="100">
        <template #default="{ row }">
          <el-tag :type="qualityTagType(row.qualityScore)" effect="light">{{ row.qualityScore }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stem" label="题干" show-overflow-tooltip />
      <el-table-column label="选项" min-width="240">
        <template #default="{ row }">
          <div v-for="(opt,i) in parseOptions(row)" :key="i">{{ String.fromCharCode(65 + i) }}. {{ opt }}</div>
        </template>
      </el-table-column>
      <el-table-column label="答案" width="160">
        <template #default="{ row }">{{ formatAnswer(row) }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
const courseId = Number(route.params.id)
const title = ref('测评')
const desc = ref('')
const documentId = ref<number>()
const count = ref<number>(5)
const quizId = ref<number>()
const questions = ref<any[]>([])
const loading = ref(false)
const difficulty = ref<string>('')
const typeSel = ref<string>('SINGLE')
const minQuality = ref<string | number>('ALL')
const viewQuestions = computed(()=>{
  const mq = minQuality.value
  return questions.value.filter(q => typeof mq === 'number' ? Number(q.qualityScore || 0) >= mq : true)
})

async function createQuiz(){
  const res = await client.post('/quizzes', { course: { id: courseId }, title: title.value, description: desc.value })
  quizId.value = res.data.id
}

async function createAndGenerate(){
  await createQuiz()
  await generate()
}

async function generate(){
  if(!quizId.value || !documentId.value) return
  loading.value = true
  const params = new URLSearchParams()
  params.set('documentId', String(documentId.value))
  params.set('count', String(count.value))
  if (typeSel.value) params.set('type', typeSel.value)
  const res = await client.post(`/quizzes/${quizId.value}/generate?${params.toString()}`, undefined, { timeout: 0 })
  questions.value = res.data
  loading.value = false
}

async function generateByQuery(){
  if(!quizId.value || !documentId.value) return
  loading.value = true
  const params = new URLSearchParams()
  params.set('documentId', String(documentId.value))
  params.set('count', String(count.value))
  if (difficulty.value) params.set('difficulty', difficulty.value)
  if (typeSel.value) params.set('type', typeSel.value)
  const res = await client.post(`/quizzes/${quizId.value}/generate-by-query?${params.toString()}`, undefined, { timeout: 0 })
  questions.value = res.data
  loading.value = false
}

onMounted(() => {
  const rq = route.query || {}
  if (typeof rq.difficulty === 'string') difficulty.value = rq.difficulty
  if (typeof rq.type === 'string') typeSel.value = String(rq.type)
  if (typeof rq.docId === 'string') {
    const v = Number(rq.docId)
    if (!Number.isNaN(v)) documentId.value = v
  }
  if (typeof rq.count === 'string') {
    const c = Number(rq.count)
    if (!Number.isNaN(c)) count.value = c
  }
  if (rq.auto === '1') {
    createQuiz().then(async ()=>{
      await generateByQuery()
    })
  }
})

function goBack(){
  if (window.history.length > 1) router.back(); else router.push('/pdf-upload')
}

function parseOptions(row:any){
  try {
    const arr = JSON.parse(row.optionsJson || '[]')
    return Array.isArray(arr) ? arr : []
  } catch { return [] }
}

function formatAnswer(row:any){
  const opts = parseOptions(row)
  const a = String(row.answer || '')
  const t = String(row.type || '')
  if (t === 'MULTI') {
    const parts = a.split(/[;,|]/).map(s => s.trim()).filter(Boolean)
    const letters: string[] = []
    for (const p of parts) {
      const m = p.toUpperCase()
      if (/^[A-Z]$/.test(m)) { letters.push(m) ; continue }
      const idx = opts.findIndex(o => String(o) === p)
      if (idx >= 0) letters.push(String.fromCharCode(65 + idx))
    }
    return letters.length ? letters.join(',') : a
  }
  if (t === 'TRUE_FALSE') {
    return a
  }
  if (opts.length) {
    const idx = opts.findIndex(o => String(o) === a)
    if (idx >= 0) return String.fromCharCode(65 + idx)
  }
  return a
}

function desiredMultiCount(d:string){
  const v = String(d || '').toUpperCase()
  if (v === 'EASY') return 1
  if (v === 'HARD') return 3
  return 2
}

function tableRowClass({ row }: any){
  const s = Number(row.qualityScore || 0)
  return s < 60 ? 'row-low' : ''
}

function qualityTagType(s:number){
  if (s >= 80) return 'success'
  if (s >= 60) return 'warning'
  return 'danger'
}
</script>

<style scoped>
.row-low td { background-color: #fff4e6; }
</style>
