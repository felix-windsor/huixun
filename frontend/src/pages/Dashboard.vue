<template>
  <div style="padding:24px;">
    <el-page-header content="仪表盘" @back="goBack" />
    <div style="margin-top:16px;">
      <el-button type="primary" @click="$router.push('/pdf-upload')">上传 PDF</el-button>
      <el-button style="margin-left:8px;" @click="$router.push('/courses')">课程管理</el-button>
      <el-button style="margin-left:8px;" @click="$router.push('/analytics')">分析报表</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>
