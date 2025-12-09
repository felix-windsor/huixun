<template>
  <div style="padding:24px;">
    <el-page-header content="知识点分析" @back="goBack" />
    <div style="display:flex; gap:8px; margin:12px 0;">
      <el-input v-model.number="quizId" placeholder="测评ID" style="width:160px;" />
      <el-button type="primary" @click="load">加载</el-button>
      <el-button @click="loadDifficulty">难度分布</el-button>
    </div>
    <div id="kpChart" style="height:320px;"></div>
    <div id="diffChart" style="height:320px;margin-top:12px;"></div>
    <el-table :data="rows" style="margin-top:12px;">
      <el-table-column prop="tag" label="知识点" />
      <el-table-column prop="accuracy" label="准确率" />
      <el-table-column prop="correct" label="正确数" />
      <el-table-column prop="total" label="总数" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import client from '../api/client'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

const router = useRouter()
const quizId = ref<number>()
const rows = ref<any[]>([])
const diff = ref<any[]>([])

async function load(){
  const res = await client.get('/analytics/knowledge-points', { params: { quizId: quizId.value } })
  rows.value = res.data
  draw()
}

function draw(){
  const dom = document.getElementById('kpChart') as HTMLElement
  if(!dom) return
  const chart = echarts.init(dom)
  const tags = rows.value.map(x=>x.tag)
  const acc = rows.value.map(x=>Number(x.accuracy))
  chart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: tags },
    yAxis: { type: 'value', max: 1 },
    series: [{ type: 'bar', data: acc, label: { show: true, formatter: (p:any)=> (p.data*100).toFixed(0)+'%' } }]
  })
}

async function loadDifficulty(){
  const res = await client.get('/analytics/difficulty', { params: { quizId: quizId.value } })
  diff.value = res.data
  const dom = document.getElementById('diffChart') as HTMLElement
  if(!dom) return
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: diff.value.map((x:any)=>x.difficulty) },
    yAxis: { type: 'value', max: 1 },
    series: [{ type: 'bar', data: diff.value.map((x:any)=>Number(x.accuracy)), label: { show: true, formatter: (p:any)=> (p.data*100).toFixed(0)+'%' } }]
  })
}

function goBack(){
  if (window.history.length > 1) router.back(); else router.push('/dashboard')
}
</script>
