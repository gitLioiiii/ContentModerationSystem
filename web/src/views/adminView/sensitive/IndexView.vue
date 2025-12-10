
<template>
  <ElForm :model="filterModel" :rules="filterRules" @submit.prevent="fetch" inline>
      <ElFormItem prop="keywordsName" label="敏感词" style="width: 25rem">
        <ElInput v-model="filterModel.keywords" :prefix-icon="Search" placeholder="关键字 姓名、手机号" clearable />
      </ElFormItem>
      <ElFormItem prop="category" label="分类" style="width: 12rem">
        <ElSelect v-model="filterModel.role" placeholder="全部" clearable>
        </ElSelect>
      </ElFormItem>
            <ElFormItem prop="level" label="水平" style="width: 12rem">
        <ElSelect v-model="filterModel.role" placeholder="全部" clearable>
        </ElSelect>
      </ElFormItem>
      <ElFormItem>
        <ElButton native-type="submit" type="primary" class="shaixuan">筛选</ElButton>
      </ElFormItem>
  </ElForm>
  <ElTable :data="users" style="width: 100%" stripe border :show-header="true">
      <ElTableColumn prop="id" label="#" />
      <ElTableColumn prop="word" label="敏感词"/>
      <ElTableColumn prop="category" label="分类"/>
      <ElTableColumn prop="level" label="水平"/>
      <ElTableColumn prop="createdAt" label="创建时间"/>
      <ElTableColumn label="设置" width="200" header-align="center">
        <template #default = "{ row }">
          <ElButton
            type="primary"
            @click="openEdit(row)"
            style="margin-left: 1rem;"
            >修改</ElButton
          >
          <ElPopconfirm
            title="确认移除该角色吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="remove(row)"
          >
            <template #reference>
              <ElButton type="danger">移除</ElButton>
            </template>
          </ElPopconfirm>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElPagination
        layout="prev, pager, next, jumper, sizes, ->, total"
        :page-sizes="[1, 5, 10, 20, 50, 100]"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        background
    />


</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import {
  ElTable,
  ElTableColumn,
  ElPopconfirm,
  ElButton,
  ElMessage,
  ElForm,
  ElFormItem,
  ElPagination,
  ElInput,
  ElSelect,
} from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'


const users = ref([])

// 筛选表单
const filterModel = reactive({
    keywords: '',
})

const filterRules = reactive({
  keywordsName: [{ min: 1, max:16, message: '关键字仅限1~16个字符。', trigger: 'change'}],
  keywordsOffice: [{ min: 1, max:16, message: '关键字仅限1~16个字符。', trigger: 'change'}]
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

  if (filterModel.role !== '' && filterModel.role !== undefined && filterModel.role !== null) {
    params.append('role', filterModel.role)
  }

    request.get('/user', { params }).then((response) => {
        if (response.data.status === true) {
            users.value = response.data.payload.users
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
    request.post('/user/remove', row).then((response) => {
        if (response.data.status === true) {
            fetch()
            ElMessage.success('移除成功！')
        } else {
            ElMessage.error('移除失败！')
        }
    })
}


</script>
