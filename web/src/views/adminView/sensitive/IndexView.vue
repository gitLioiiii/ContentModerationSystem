
<template>
  <ElForm :model="filterModel" :rules="filterRules" @submit.prevent="fetch" inline>
      <ElFormItem prop="keywords" label="关键字" style="width: 15rem">
        <ElInput v-model="filterModel.keywords" :prefix-icon="Search" placeholder="搜索敏感词" clearable />
      </ElFormItem>
      <ElFormItem prop="category" label="分类" style="width: 12rem">
        <ElSelect v-model="filterModel.category" placeholder="全部" clearable>
          <ElOption label="政治敏感" value="political" />
          <ElOption label="色情低俗" value="porn" />
          <ElOption label="暴力恐怖" value="violence" />
          <ElOption label="垃圾邮件" value="spam_mail" />
          <ElOption label="广告营销" value="advertising" />
          <ElOption label="其他" value="other" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem prop="level" label="等级" style="width: 12rem">
        <ElSelect v-model="filterModel.level" placeholder="全部" clearable>
          <ElOption label="低风险" value="low" />
          <ElOption label="中风险" value="medium" />
          <ElOption label="高风险" value="high" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem>
        <ElButton native-type="submit" type="primary" class="shaixuan">筛选</ElButton>
      </ElFormItem>
  </ElForm>
  <ElTable :data="sensitiveWords" style="width: 100%" stripe border :show-header="true">
      <ElTableColumn prop="id" label="#" width="200" />
      <ElTableColumn prop="word" label="敏感词" width="200" />
      <ElTableColumn prop="category" label="分类" width="150">
        <template #default="{ row }">
          <ElTag v-if="row.category === 'political'" type="danger">政治敏感</ElTag>
          <ElTag v-else-if="row.category === 'porn'" type="warning">色情低俗</ElTag>
          <ElTag v-else-if="row.category === 'violence'" type="danger">暴力恐怖</ElTag>
          <ElTag v-else-if="row.category === 'spam_mail'" type="danger">垃圾邮件</ElTag>
          <ElTag v-else-if="row.category === 'advertising'" type="info">广告营销</ElTag>
          <ElTag v-else>其他</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="level" label="等级" width="120">
        <template #default="{ row }">
          <ElTag v-if="row.level === 'high'" type="danger">高风险</ElTag>
          <ElTag v-else-if="row.level === 'medium'" type="warning">中风险</ElTag>
          <ElTag v-else type="success">低风险</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="effective" label="启用状态" width="120">
        <template #default="{ row }">
          <ElTag v-if="row.effective" type="success">已启用</ElTag>
          <ElTag v-else type="info">已禁用</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="createdAt" label="创建时间" min-width="200" />
      <ElTableColumn label="设置" width="200" header-align="center">
        <template #default = "{ row }">
          <ElButton
            type="primary"
            @click="openEdit(row)"
            style="margin-left: 1rem;"
            >修改</ElButton
          >
          <ElPopconfirm
            title="确认删除该敏感词吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="remove(row)"
          >
            <template #reference>
              <ElButton type="danger">删除</ElButton>
            </template>
          </ElPopconfirm>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElPagination
        layout="prev, pager, next, jumper, sizes, ->, total"
        :page-sizes="[5, 10, 20, 50, 100]"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        background
    />

  <!-- 编辑模态框 -->
  <ElDialog
    v-model="dialogVisible"
    title="编辑敏感词"
    width="500"
    align-center
  >
    <ElForm ref="editForm" :model="editModel" :rules="editRules" label-width="80">
      <ElFormItem prop="word" label="敏感词">
        <ElInput v-model="editModel.word" placeholder="请输入敏感词" style="width: 100%;" />
      </ElFormItem>
      <ElFormItem prop="category" label="分类">
        <ElSelect v-model="editModel.category" placeholder="请选择分类" style="width: 100%;">
          <ElOption label="政治敏感" value="political" />
          <ElOption label="色情低俗" value="porn" />
          <ElOption label="暴力恐怖" value="violence" />
          <ElOption label="垃圾邮件" value="spam_mail" />
          <ElOption label="广告营销" value="advertising" />
          <ElOption label="其他" value="other" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem prop="level" label="等级">
        <ElSelect v-model="editModel.level" placeholder="请选择等级" style="width: 100%;">
          <ElOption label="低风险" value="low" />
          <ElOption label="中风险" value="medium" />
          <ElOption label="高风险" value="high" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem prop="effective" label="启用状态">
        <ElSwitch v-model="editModel.effective" active-text="已启用" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="save">保存</ElButton>
      </div>
    </template>
  </ElDialog>

</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElTable, ElTableColumn, ElPopconfirm, ElButton, ElMessage, ElForm,
          ElFormItem, ElPagination, ElInput, ElSelect, ElOption, ElTag, ElDialog, ElSwitch
} from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'


const sensitiveWords = ref([])

// 筛选表单
const filterModel = reactive({
    keywords: '',
    category: '',
    level: ''
})

const filterRules = reactive({
  keywords: [{ min: 1, max: 50, message: '关键字仅限1~50个字符。', trigger: 'change'}]
})

const pagination = reactive({
    currentPage: 1,
    pageSize: 5,
    total: 0,
})

const fetch = () => {
    let params = new URLSearchParams()
    params.append('page', pagination.currentPage)
    params.append('pageSize', pagination.pageSize)

    if (filterModel.keywords.length > 0) {
        params.append('keywords', filterModel.keywords)
    }

    if (filterModel.category !== '' && filterModel.category !== undefined && filterModel.category !== null) {
        params.append('category', filterModel.category)
    }

    if (filterModel.level !== '' && filterModel.level !== undefined && filterModel.level !== null) {
        params.append('level', filterModel.level)
    }

    request.get('/sensitive', { params }).then((response) => {
        if (response.data.status === true) {
            sensitiveWords.value = response.data.payload.sensitiveWords
            Object.assign(pagination, response.data.payload.pagination)
        }
    })
}

watch(
    () => [pagination.currentPage, pagination.pageSize],
    () => {
        fetch()
    },
    { immediate: true },
)

const remove = (row) => {
    request.post('/sensitive/remove', row).then((response) => {
        if (response.data.status === true) {
            fetch()
            ElMessage.success('删除成功！')
        } else {
            ElMessage.error('删除失败！')
        }
    })
}

// 编辑模态框
const dialogVisible = ref(false)
const editForm = ref(null)
const editModel = reactive({
  id: null,
  word: '',
  category: '',
  level: '',
  effective: true
})

const editRules = reactive({
  word: [
    { required: true, message: '请输入敏感词。', trigger: 'blur' },
    { min: 1, max: 50, message: '敏感词仅限1~50个字符。', trigger: 'change' },
  ],
  category: [
    { required: true, message: '请选择分类。', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择等级。', trigger: 'change' }
  ]
})

const openEdit = (row) => {
  editModel.id = row.id
  editModel.word = row.word
  editModel.category = row.category
  editModel.level = row.level
  editModel.effective = row.effective !== undefined ? row.effective : true
  dialogVisible.value = true
}

const save = () => {
  editForm.value
    .validate()
    .then((result) => {
      if (result === true) {
        const updateData = {
          id: editModel.id,
          word: editModel.word,
          category: editModel.category,
          level: editModel.level,
          effective: editModel.effective
        }

        request
          .post('/sensitive/update', updateData)
          .then((response) => {
            if (response.data.status === true) {
              ElMessage.success('保存成功！')
              dialogVisible.value = false
              fetch()
            } else {
              ElMessage.error('保存失败！')
            }
          })
          .catch(() => {
            ElMessage.error('保存失败！')
          })
      }
    })
    .catch(() => {})
}

</script>
