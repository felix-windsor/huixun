<template>
  <div style="padding:24px;">
    <el-page-header content="测评作答" />
    <div style="margin:12px 0;">
      <el-button type="primary" @click="create">创建测评会话</el-button>
      <span v-if="attemptId" style="margin-left:8px;">Attempt ID: {{ attemptId }}</span>
    </div>
    <el-form v-if="questions.length" label-width="120px">
      <div v-for="q in questions" :key="q.id" style="margin-bottom:12px;">
        <div><b>{{ q.stem }}</b>（{{ q.type }} / {{ q.difficulty }}）</div>
        <div v-if="q.type==='SINGLE' || q.type==='TRUE_FALSE'">
          <el-radio-group v-model="answers[q.id]">
            <el-radio v-for="opt in parseOptions(q.optionsJson)" :key="opt" :label="opt">{{ opt }}</el-radio>
          </el-radio-group>
        </div>
        <div v-else-if="q.type==='MULTI'">
          <el-checkbox-group v-model="multi[q.id]">
            <el-checkbox v-for="opt in parseOptions(q.optionsJson)" :key="opt" :label="opt">{{ opt }}</el-checkbox>
          </el-checkbox-group>
        </div>
        <div v-else>
          <el-input v-model="answers[q.id]" type="textarea" />
        </div>
      </div>
      <el-button type="success" @click="submit">提交</el-button>
    </el-form>
    <div v-if="score!==null" style="margin-top:12px;">得分：{{ (score*100).toFixed(0) }}%</div>
    <div v-if="score!==null" id="scoreChart" style="height:240px;margin-top:12px;"></div>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
const route = useRoute()
const quizId = Number(route.params.quizId)
const attemptId = ref<number>()
const questions = ref<any[]>([])
const answers = ref<Record<number, string>>({})
const multi = ref<Record<number, string[]>>({})
const score = ref<number|null>(null)

function parseOptions(json:string){ try { return JSON.parse(json) } catch { return [] } }

async function create(){
  const a = await client.post('/attempts/create?quizId='+quizId)
  attemptId.value = a.data.attemptId
  const res = await client.post('/attempts?quizId='+quizId)
  questions.value = res.data
}

async function submit(){
  const payload:Record<string,string> = {}
  for(const q of questions.value){
    if(q.type==='MULTI') payload[q.id] = (multi.value[q.id] || []).join(',')
    else payload[q.id] = answers.value[q.id] || ''
  }
  const r = await client.put(`/attempts/${attemptId.value}/submit`, payload)
  score.value = r.data.score
  drawChart()
}

function drawChart(){
  const dom = document.getElementById('scoreChart') as HTMLElement
  if(!dom) return
  const chart = echarts.init(dom)
  chart.setOption({
    title: { text: '测评得分' },
    xAxis: { type: 'category', data: ['得分'] },
    yAxis: { type: 'value', max: 1 },
    series: [{ type: 'bar', data: [score.value ?? 0], label: { show: true, formatter: ((score.value ?? 0)*100).toFixed(0)+'%' } }]
  })
}
</script>
