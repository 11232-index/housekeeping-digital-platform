<template>
  <div class="hk-management">
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchForm.name" placeholder="家政员名字" clearable style="width:200px;margin-right:10px" @keyup.enter="handleSearch" />
        <el-button type="default" style="background:#000;color:#fff" :loading="searchLoading" @click="handleSearch">查询</el-button>
      </div>
    </div>

    <el-table :data="tableData" style="width:100%;margin-top:20px" border>
      <el-table-column prop="name" label="名字" width="100" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <img v-if="row.picture" :src="row.picture" style="width:50px;height:50px;object-fit:cover;cursor:pointer;border-radius:4px" @click="previewSrc=row.picture;previewVisible=true" />
          <span v-else style="color:#ccc">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="单价" width="100" />
      <el-table-column label="评分" width="160">
        <template #default="{ row }">
          <span v-if="row.rating > 0" class="rating-cell">
            <span class="stars-wrap">
              <span class="stars-empty">★★★★★</span>
              <span class="stars-fill" :style="{ width: starFillPercent(row.rating) + '%' }">★★★★★</span>
            </span>
            <span class="score">{{ row.rating.toFixed(1) }}</span>
          </span>
          <span v-else style="color:#ccc">暂无</span>
        </template>
      </el-table-column>
      <el-table-column label="身价" width="100">
        <template #default="{ row }">
          <el-tag :type="row.rank === 1 ? 'warning' : row.rank === 2 ? 'info' : row.rank === 3 ? '' : ''" size="small">
            {{ rankLabel(row.rank) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="轮播图" width="200">
        <template #default="{ row }">
          <viewer v-if="row.carousel" class="carousel-imgs">
            <img v-for="(url, i) in (row.carousel || '').split(';').filter(Boolean)" :key="i"
                 :src="url" style="width:40px;height:40px;object-fit:cover;margin:2px;border-radius:4px;cursor:pointer"
                 @click="previewSrc=url;previewVisible=true" />
          </viewer>
          <span v-else style="color:#ccc">暂无</span>
        </template>
      </el-table-column>
      <el-table-column prop="review" label="综合评价" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
        :page-sizes="[5,10,20,50]" :total="pagination.total" layout="total,sizes,prev,pager,next,jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog v-model="dialogVisible" title="修改家政员" width="500px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="名字">
          <el-input v-model="form.name" disabled />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload class="picture-upload" :action="uploadAction" :headers="uploadHeaders"
            :show-file-list="false" :on-success="handleUploadSuccess" :on-error="handleUploadError"
            :before-upload="beforeUpload" accept="image/*">
            <img v-if="form.picture" :src="form.picture" class="upload-preview" />
            <el-button v-else type="primary">点击上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="轮播图">
          <div class="carousel-upload">
            <div v-for="(url, i) in carouselList" :key="i" class="carousel-item">
              <img :src="url" class="carousel-thumb" />
              <span class="carousel-del" @click="removeCarousel(i)">✕</span>
            </div>
            <el-upload v-if="carouselList.length < 5" class="carousel-upload-btn"
              :action="uploadAction" :headers="uploadHeaders" :show-file-list="false"
              :on-success="handleCarouselUploadSuccess" :on-error="handleUploadError"
              :before-upload="beforeUpload" accept="image/*">
              <el-button type="primary" plain>+ 上传</el-button>
            </el-upload>
          </div>
          <span class="upload-tip">最多5张（已选{{carouselList.length}}张）</span>
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="form.rating" :min="0" :max="5" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="身价">
          <el-select v-model="form.rank" style="width:100%">
            <el-option label="无牌" :value="0" />
            <el-option label="金牌" :value="1" />
            <el-option label="银牌" :value="2" />
            <el-option label="铜牌" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="综合评价">
          <el-input v-model="form.review" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    <ImageViewer :visible="previewVisible" :src="previewSrc" @close="previewVisible = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import ImageViewer from '@/components/ImageViewer.vue'
import { getHousekeeperPage, updateHousekeeper, type Housekeeper, type HousekeeperDTO, type HousekeeperPageQueryParams } from '@/api/housekeeper'

const rankMap: Record<number, string> = { 0: '无牌', 1: '金牌', 2: '银牌', 3: '铜牌' }
const rankLabel = (r: number) => rankMap[r] || '无牌'

/** 评分转星级显示：用百分比精确填充最后一颗星 */
const starFillPercent = (rating: number) => {
  return (rating / 5) * 100
}

const searchLoading = ref(false)
const searchForm = reactive({ name: '' })
const submitLoading = ref(false)
const tableData = ref<Housekeeper[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const previewVisible = ref(false)
const previewSrc = ref('')
const uploadAction = 'http://localhost:8080/admin/common/upload'
const uploadHeaders = { token: sessionStorage.getItem('token') || '' }
const handleUploadSuccess = (res: any) => {
  if (res.code === 1 && res.data) { form.picture = res.data; ElMessage.success('上传成功') }
  else { ElMessage.error(res.msg || '上传失败') }
}
const handleUploadError = () => ElMessage.error('上传失败')
const beforeUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('不超过5MB'); return false }
  return true
}

// 轮播图列表
const carouselList = ref<string[]>([])

/** 上传轮播图成功 */
const handleCarouselUploadSuccess = (res: any) => {
  if (res.code === 1 && res.data) {
    if (carouselList.value.length >= 5) { ElMessage.warning('最多5张'); return }
    carouselList.value.push(res.data)
    form.carousel = carouselList.value.join(';')
    ElMessage.success('上传成功')
  } else { ElMessage.error(res.msg || '上传失败') }
}

/** 移除轮播图中某张 */
const removeCarousel = (i: number) => {
  carouselList.value.splice(i, 1)
  form.carousel = carouselList.value.join(';')
}

const form = reactive<HousekeeperDTO>({ id: undefined, name: '', picture: '', carousel: '', price: 0, rating: 0, rank: 0, review: '' })

const loadList = async () => {
  const params: HousekeeperPageQueryParams = { page: pagination.page, pageSize: pagination.pageSize, name: searchForm.name || undefined }
  console.log('家政员加载开始', params)
  try {
    const res: any = await getHousekeeperPage(params)
    console.log('家政员API返回', res)
    if (res.code === 1) { tableData.value = res.data.records; pagination.total = res.data.total }
  } catch (e: any) {
    console.error('家政员加载失败', e)
    ElMessage.error('加载失败: ' + (e.message || '未知错误'))
  }
}

const handleSearch = async () => { searchLoading.value = true; pagination.page = 1; try { await loadList() } finally { searchLoading.value = false } }
const handleEdit = (row: Housekeeper) => {
  form.id = row.id; form.name = row.name; form.picture = row.picture || ''; form.price = row.price; form.rating = row.rating; form.rank = row.rank; form.review = row.review
  form.carousel = row.carousel || ''
  carouselList.value = (row.carousel || '').split(';').filter(Boolean)
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (submitLoading.value) return
  submitLoading.value = true
  try {
    const res: any = await updateHousekeeper(form)
    if (res.code === 1) { ElMessage.success('编辑成功'); dialogVisible.value = false; loadList() }
  } catch (e: any) { ElMessage.error(e?.message || '操作失败') } finally { submitLoading.value = false }
}
const handleDialogClose = () => {
  formRef.value?.resetFields()
  form.id = undefined; form.name = ''; form.picture = ''; form.carousel = ''; form.price = 0; form.rating = 0; form.rank = 0; form.review = ''
  carouselList.value = []
}
const handleSizeChange = (v: number) => { pagination.pageSize = v; pagination.page = 1; loadList() }
const handleCurrentChange = (v: number) => { pagination.page = v; loadList() }
onMounted(() => loadList())
</script>

<style scoped>
.hk-management { padding: 0; min-height: 100%; }
.search-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: #fff; border-radius: 4px; }
.search-left, .search-right { display: flex; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; padding: 20px; background: #fff; border-radius: 4px; }
:deep(.el-table) { border-radius: 4px; }
:deep(.el-button--primary) { background-color: #1e3a8a; border-color: #1e3a8a; }
:deep(.el-button--primary:hover) { background-color: #2563eb; border-color: #2563eb; }
.rating-cell { display:flex; align-items:center; gap:6px }
.stars-wrap { position:relative; display:inline-block; font-size:16px; letter-spacing:2px; white-space:nowrap }
.stars-empty { color:#ddd }
.stars-fill { color:#f5a623; position:absolute; left:0; top:0; overflow:hidden; white-space:nowrap }
.score { color:#333; font-weight:bold }
.carousel-upload { display:flex; flex-wrap:wrap; gap:8px; align-items:center }
.carousel-item { position:relative; width:60px; height:60px }
.carousel-thumb { width:60px; height:60px; object-fit:cover; border-radius:4px; border:1px solid #ddd }
.carousel-del { position:absolute; top:-6px; right:-6px; width:18px; height:18px; background:#e94e3c; color:#fff; border-radius:50%; font-size:12px; display:flex; align-items:center; justify-content:center; cursor:pointer }
.upload-tip { font-size:12px; color:#999; display:block; margin-top:4px }
.upload-preview { width: 120px; height: 120px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd; }
</style>
