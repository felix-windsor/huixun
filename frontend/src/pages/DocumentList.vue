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
      <el-table-column label="操作" width="360">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="useDoc(row.id)">使用此文档</el-button>
          <el-button size="small" @click="viewFragments(row.id)">查看片段</el-button>
          <el-button size="small" type="warning" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="removeDoc(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="editVisible" title="编辑文档" width="520px">
      <el-form label-width="96px">
        <el-form-item label="文件名">
          <el-input v-model="editForm.filename" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="状态">
            <el-option label="UPLOADED" value="UPLOADED" />
            <el-option label="PARSING" value="PARSING" />
            <el-option label="EMBEDDING" value="EMBEDDING" />
            <el-option label="DONE" value="DONE" />
            <el-option label="FAILED" value="FAILED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
  </template>

<script setup lang="ts">
import client from '../api/client'
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
const router = useRouter()
const loading = ref(false)
const list = ref<any[]>([])
const kw = ref('')
const editVisible = ref(false)
const editForm = ref<any>({ id: undefined, filename: '', status: '' })

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
  router.push({ path: '/pdf-upload', query: { docId: String(id), noParse: '1' } })
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

function openEdit(row:any){
  editForm.value = { id: row.id, filename: row.filename, status: row.status }
  editVisible.value = true
}

async function submitEdit(){
  const id = Number(editForm.value.id)
  if (!id) return
  const fn = String(editForm.value.filename || '').trim()
  if (!fn) { ElMessage.warning('文件名不能为空'); return }
  const st = String(editForm.value.status || '').toUpperCase()
  const allowed = ['UPLOADED','PARSING','EMBEDDING','DONE','FAILED']
  if (!allowed.includes(st)) { ElMessage.warning('状态无效'); return }
  await client.put(`/documents/${id}`, { filename: fn, status: st })
  ElMessage.success('保存成功')
  editVisible.value = false
  await load()
}

async function removeDoc(row:any){
  const id = Number(row.id)
  if (!id) return
  let count = 0
  try {
    const res = await client.get(`/documents/${id}/fragments`)
    const arr = Array.isArray(res.data) ? res.data : []
    count = arr.length
  } catch {}
  try {
    await ElMessageBox.confirm(`确认删除该文档？其下片段数量：${count}`, '确认删除', { type: 'warning' })
  } catch { return }
  try {
    await client.delete(`/documents/${id}`)
    ElMessage.success('已删除')
  } catch (e:any) {
    const s = e?.response?.status
    if (s === 404) ElMessage.warning('文档不存在或已删除')
  } finally {
    await load()
  }
}
</script>
