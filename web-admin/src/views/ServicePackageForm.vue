<template>
  <div class="package-form-page">
    <!-- 顶部标题栏 -->
    <div class="form-header">
      <el-button link type="primary" @click="handleBack" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="form-title">{{ isEdit ? '编辑套餐' : '新增套餐' }}</h2>
    </div>

    <!-- 表单内容 -->
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="right"
        size="default"
      >
        <el-form-item label="套餐名称" prop="name" required>
          <el-input
            v-model="formData.name"
            placeholder="请输入套餐名称"
            maxlength="32"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="套餐分类" prop="categoryId" required>
          <el-select
            v-model="formData.categoryId"
            placeholder="请选择服务套餐分类"
            style="width: 100%"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="套餐价格" prop="price" required>
          <el-input-number
            v-model="formData.price"
            :precision="2"
            :min="0"
            :max="999999.99"
            placeholder="请输入套餐价格"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 套餐要求 -->
        <el-form-item label="套餐要求">
          <div class="requirements-area">
            <div
              v-for="(req, index) in formData.servicePackageItems"
              :key="index"
              class="requirement-row"
            >
              <el-input
                v-model="req.name"
                placeholder="要求名称"
                maxlength="32"
                clearable
                style="width: 160px; margin-right: 8px; flex-shrink: 0"
              />
              <div class="requirement-tags">
                <el-tag
                  v-for="(tag, tagIdx) in (req as any)._valueTags"
                  :key="tagIdx"
                  closable
                  :disable-transitions="false"
                  size="default"
                  style="margin-right: 6px; margin-bottom: 4px"
                  @close="removeRequirementTag(index, tagIdx)"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="(req as any)._inputVisible"
                  :ref="(el: any) => setTagInputRef(index, el)"
                  v-model="(req as any)._inputValue"
                  size="small"
                  style="width: 100px"
                  @keyup.enter="addRequirementTag(index)"
                  @blur="addRequirementTag(index)"
                />
                <el-button
                  v-else
                  size="small"
                  @click="showTagInput(index)"
                >
                  + 选项
                </el-button>
              </div>
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                style="flex-shrink: 0; margin-left: 8px"
                @click="removeRequirement(index)"
              />
            </div>
            <el-button type="primary" link @click="addRequirement">
              <el-icon><Plus /></el-icon>
              添加套餐要求
            </el-button>
          </div>
        </el-form-item>

        <!-- 套餐图片上传 -->
        <el-form-item label="套餐图片" prop="image" required>
          <div class="upload-area">
            <el-upload
              class="image-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              accept="image/png, image/jpeg, image/jpg"
              drag
            >
              <div v-if="formData.image" class="uploaded-image-wrapper">
                <el-image
                  :src="formData.image"
                  :preview-src-list="[formData.image]"
                  fit="cover"
                  preview-teleported
                  style="width: 148px; height: 148px; border-radius: 6px"
                />
                <div class="image-mask">
                  <el-icon><Edit /></el-icon>
                  <span>重新上传</span>
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span class="upload-text">上传图片</span>
              </div>
            </el-upload>
            <div class="upload-tips">
              <p>仅支持 JPG/PNG 格式</p>
              <p>文件大小不超过 2MB</p>
              <p>建议尺寸：200×200 像素</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="套餐描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            placeholder="请输入套餐描述信息"
            :rows="4"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 底部按钮 -->
      <div class="form-footer">
        <el-button @click="handleBack">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存修改' : '确认新增' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { ArrowLeft, Plus, Delete, Edit } from '@element-plus/icons-vue'
import {
  getServicePackageById,
  addServicePackage,
  updateServicePackage,
  type ServicePackageDTO,
  type ServicePackageItem
} from '@/api/service-package'
import { getCategoryList, type Category } from '@/api/category'

// ──────── 扩展类型：给 ServicePackageItem 附加前端 UI 字段 ────────
interface RequirementRow extends ServicePackageItem {
  _valueTags: string[]
  _inputVisible: boolean
  _inputValue: string
}

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const categoryOptions = ref<Category[]>([])

// 判断是否为编辑模式
const isEdit = computed(() => !!route.query.id)

// 上传地址（后端 CommonController: POST /admin/common/upload）
const uploadUrl = 'http://localhost:8080/admin/common/upload'

// 上传请求头（/admin/ 路径使用 token，非 employeeToken）
const uploadHeaders = computed(() => ({
  token: sessionStorage.getItem('token') || sessionStorage.getItem('employeeToken') || ''
}))

// 表单数据（servicePackageItems 使用扩展类型）
const formData = reactive<ServicePackageDTO & { servicePackageItems: RequirementRow[] }>({
  id: undefined,
  name: '',
  categoryId: undefined,
  price: 0,
  image: '',
  description: '',
  servicePackageItems: []
})

// ──────── 标签输入框 ref 管理（按行索引） ────────
const tagInputRefs = ref<Record<number, any>>({})

const setTagInputRef = (index: number, el: any) => {
  if (el) tagInputRefs.value[index] = el
}

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入套餐名称', trigger: 'blur' },
    { min: 1, max: 32, message: '套餐名称长度在1-32个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择套餐分类', trigger: 'change' },
    { type: 'number', min: 1, message: '请选择套餐分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入套餐价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '套餐价格不能为0', trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传套餐图片', trigger: 'change' }
  ]
}

// 返回列表
const handleBack = () => {
  router.push('/employee/service-package')
}

// 加载分类下拉列表（type=2 服务套餐分类）
const loadCategoryOptions = async () => {
  try {
    const res: any = await getCategoryList(2)
    if (res.code === 1) {
      categoryOptions.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类列表失败', error)
  }
}

// ──────── 将数据库 value (JSON 字符串) 解析为标签数组 ────────
const parseValueToTags = (value: string): string[] => {
  if (!value) return []
  try {
    const arr = JSON.parse(value)
    return Array.isArray(arr) ? arr : []
  } catch {
    return value ? [value] : []
  }
}

// ──────── 将标签数组序列化为数据库 value (JSON 字符串) ────────
const tagsToValue = (tags: string[]): string => {
  return JSON.stringify(tags.filter(t => t.trim()))
}

// 加载编辑数据
const loadPackageDetail = async () => {
  const id = route.query.id
  if (!id) return

  try {
    const res: any = await getServicePackageById(Number(id))
    if (res.code === 1) {
      const data = res.data
      formData.id = data.id
      formData.name = data.name
      formData.categoryId = data.categoryId
      formData.price = data.price
      formData.image = data.image || ''
      formData.description = data.description || ''
      formData.servicePackageItems = (data.servicePackageItems || []).map((r: ServicePackageItem) => ({
        ...r,
        _valueTags: parseValueToTags(r.value),
        _inputVisible: false,
        _inputValue: ''
      }))
    }
  } catch (error) {
    ElMessage.error('加载套餐详情失败')
    router.push('/employee/service-package')
  }
}

// ──────── 套餐要求：增 / 删 ────────
const addRequirement = () => {
  formData.servicePackageItems.push({
    name: '',
    value: '',
    _valueTags: [],
    _inputVisible: false,
    _inputValue: ''
  })
}

const removeRequirement = (index: number) => {
  formData.servicePackageItems.splice(index, 1)
}

// ──────── 标签操作 ────────
const showTagInput = (index: number) => {
  formData.servicePackageItems[index]._inputVisible = true
  nextTick(() => {
    tagInputRefs.value[index]?.focus?.()
  })
}

const addRequirementTag = (index: number) => {
  const row = formData.servicePackageItems[index]
  const val = row._inputValue.trim()
  if (val && !row._valueTags.includes(val)) {
    row._valueTags.push(val)
  }
  row._inputVisible = false
  row._inputValue = ''
}

const removeRequirementTag = (index: number, tagIdx: number) => {
  formData.servicePackageItems[index]._valueTags.splice(tagIdx, 1)
}

// 上传前校验
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 JPG/PNG 格式的图片！')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess: UploadProps['onSuccess'] = (response: any) => {
  if (response.code === 1) {
    formData.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '图片上传失败')
  }
}

// 上传失败
const handleUploadError: UploadProps['onError'] = () => {
  ElMessage.error('图片上传失败，请检查网络连接')
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      // 将前端标签数组序列化为 JSON 字符串，提交给后端
      const submitRequirements: ServicePackageItem[] = formData.servicePackageItems
        .filter(r => r.name.trim() && r._valueTags.length > 0)
        .map(r => ({
          id: r.id,
          servicePackageId: r.servicePackageId,
          name: r.name,
          value: tagsToValue(r._valueTags)
        }))

      const data: ServicePackageDTO = {
        id: formData.id,
        name: formData.name,
        categoryId: formData.categoryId,
        price: formData.price,
        image: formData.image,
        description: formData.description,
        servicePackageItems: submitRequirements
      }
      const res: any = isEdit.value
        ? await updateServicePackage(data)
        : await addServicePackage(data)

      if (res.code === 1) {
        ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
        router.push('/employee/service-package')
      } else {
        ElMessage.error(res.msg || (isEdit.value ? '修改失败' : '新增失败'))
      }
    } catch (error: any) {
      ElMessage.error(error?.message || (isEdit.value ? '修改失败' : '新增失败'))
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadCategoryOptions()
  if (isEdit.value) {
    loadPackageDetail()
  }
})
</script>

<style scoped>
.package-form-page {
  padding: 0;
  background: transparent;
  min-height: 100%;
}

/* 顶部标题栏 */
.form-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 20px;
}

.back-btn {
  font-size: 14px;
}

.form-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 表单容器 */
.form-container {
  padding: 32px 40px;
  background: #fff;
  border-radius: 4px;
  max-width: 700px;
}

.requirements-area {
  width: 100%;
}

.requirement-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.requirement-tags {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0;
  min-height: 32px;
}

/* 图片上传区域 */
.upload-area {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.image-uploader {
  flex-shrink: 0;
}

.upload-placeholder {
  width: 148px;
  height: 148px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8c939d;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  font-size: 12px;
  color: #8c939d;
}

.uploaded-image-wrapper {
  position: relative;
  width: 148px;
  height: 148px;
  border-radius: 6px;
  overflow: hidden;
}

.image-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.uploaded-image-wrapper:hover .image-mask {
  opacity: 1;
}

.upload-tips {
  font-size: 12px;
  color: #999;
  line-height: 1.8;
}

.upload-tips p {
  margin: 0;
}

/* 底部按钮 */
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-button--primary) {
  background-color: #1e3a8a;
  border-color: #1e3a8a;
}

:deep(.el-button--primary:hover) {
  background-color: #2563eb;
  border-color: #2563eb;
}

:deep(.el-upload-dragger) {
  width: 150px;
  height: 150px;
  padding: 0;
  overflow: hidden;
}

:deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  width: 150px;
  height: 150px;
}
</style>
