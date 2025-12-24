
<template>
  <ElForm :model="filterModel" :rules="filterRules" @submit.prevent="fetch" inline>
      <ElFormItem prop="keywordsName" label="用户名、姓名、手机号查询" style="width: 27rem">
        <ElInput v-model="filterModel.keywords" :prefix-icon="Search" placeholder="关键字 用户名、姓名、手机号" clearable />
      </ElFormItem>
      <ElFormItem prop="role" label="角色" style="width: 12rem">
        <ElSelect v-model="filterModel.role" placeholder="全部" clearable>
          <ElOption label="用户" value="user" />
          <ElOption label="管理员" value="admin" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem>
        <ElButton native-type="submit" type="primary" class="shaixuan">筛选</ElButton>
      </ElFormItem>
  </ElForm>
  <ElTable :data="users" style="width: 100%" stripe border :show-header="true">
      <ElTableColumn prop="id" label="#" />
      <ElTableColumn prop="avatar" label="头像">
        <template #default="{ row }">
          <!-- <img v-if="row.avatar" :src="buildURL(row.avatar)" :alt="row.username" :title="row.username"
          style="width: 50px; height: 50px; object-fit: cover;" />
          <span v-else>无头像</span> -->
          <ElImage
            v-if="row.avatar"
            style="width: 50px; height: 50px"
            :src="buildURL(row.avatar)"
            :preview-src-list="srcList"
            fit="cover"
            show-progress
          />
          <span v-else>
            <ElImage
              style="width: 50px; height: 50px"
              :src="noImage"
              fit="cover"
            />
          </span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="username" label="用户名"/>
      <ElTableColumn prop="name" label="姓名"/>
      <ElTableColumn prop="role" label="角色"/>
      <ElTableColumn prop="gender" label="性别">
        <template #default="{ row }">
          <span v-if="row.gender === 'man'">男</span>
          <span v-else-if="row.gender === 'woman'">女</span>
          <span v-else>保密</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="birthday" label="生日"/>
      <ElTableColumn prop="phone" label="手机号"/>
      <ElTableColumn prop="email" label="邮箱"/>
      <ElTableColumn label="地址">
        <template #default="{ row }">
          <span v-if="row.province || row.city">
            {{ row.province || '' }}{{ row.province && row.city ? ' - ' : '' }}{{ row.city || '' }}
          </span>
          <span v-else>未设置</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="status" label="账号状态"/>
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

<!-- z-index放高点覆盖元素 -->
    <ElDrawer
      v-model="drawer"
      title="编辑用户"
      :with-header="true"
      :teleported="true"
      :z-index="100"
      direction="rtl"
      size="30%"
    >
      <ElForm ref="form" :model="model" label-width="80">
        <ElFormItem label="用户名">
          <ElInput v-model="model.username" disabled />
        </ElFormItem>
        <ElFormItem label="姓名">
          <ElInput v-model="model.name" placeholder="修改用户姓名" />
        </ElFormItem>
        <ElFormItem label="密码">
          <ElInput v-model="model.password" type="password" placeholder="修改用户密码" show-password />
        </ElFormItem>
        <ElFormItem label="头像">
          <FileUploader v-model="model.avatarFiles" :limit="1" uploadPath="avatars">
            <template #default="{ file }">
              <ElImage :src="buildURL(file.filename)" style="width: 80px; height: 80px" fit="cover" />
            </template>
            <template #trigger>
              <ElButton type="primary" size="small">上传头像</ElButton>
            </template>
          </FileUploader>
        </ElFormItem>
        <ElFormItem label="角色">
          <ElSelect v-model="model.role" placeholder="请选择角色" style="width: 100%;">
            <ElOption label="用户" value="user" />
            <ElOption label="管理员" value="admin" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="性别">
          <ElRadioGroup v-model="model.gender">
            <ElRadio value="man">男</ElRadio>
            <ElRadio value="woman">女</ElRadio>
            <ElRadio value="none">保密</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="生日">
          <ElDatePicker
            v-model="model.birthday"
            type="date"
            placeholder="请选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </ElFormItem>
        <ElFormItem label="手机号">
          <ElInput v-model="model.phone" placeholder="请输入手机号" />
        </ElFormItem>
        <ElFormItem label="邮箱">
          <ElInput v-model="model.email" placeholder="请输入邮箱" />
        </ElFormItem>
        <ElFormItem label="省份">
          <ElSelect
            v-model="model.province"
            placeholder="请选择省份"
            filterable
            @change="handleProvinceChange"
            style="width: 100%;"
          >
            <ElOption
              v-for="province in provinceList"
              :key="province.value"
              :label="province.label"
              :value="province.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="城市">
          <ElSelect
            v-model="model.city"
            placeholder="请选择城市"
            filterable
            :disabled="cityList.length === 0"
            style="width: 100%;"
          >
            <ElOption
              v-for="city in cityList"
              :key="city.value"
              :label="city.label"
              :value="city.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="账号状态">
          <ElSelect v-model="model.status" placeholder="请选择账号状态" style="width: 100%;">
            <ElOption label="正常" value="active" />
            <ElOption label="禁用" value="ban" />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton type="default" @click.stop="closeDrawer">取消</ElButton>
        <ElButton type="primary" @click.stop="save">保存</ElButton>
      </template>
    </ElDrawer>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElTable, ElTableColumn,  ElPopconfirm, ElButton, ElMessage, ElImage, ElDrawer,
  ElForm, ElFormItem, ElPagination, ElInput, ElSelect, ElDatePicker, ElRadioGroup, ElRadio
} from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { buildURL } from '@/utils/helper'
import noImage from '@/assets/img/图片未上传.png'
import FileUploader from '@/components/FileUploader.vue'
import { getProvinceList, getCitiesByProvince } from '@/utils/regions'


const users = ref([])
const srcList = ref([])

// 筛选表单
const filterModel = reactive({
    keywords: '',
    role: ''
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
            // 更新头像预览列表
            srcList.value = users.value
                .filter(user => user.avatar)
                .map(user => buildURL(user.avatar))
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

// 抽屉
const drawer = ref(false)
const form = ref(null)
const model = reactive({
  id: null,
  username: '',
  password: '',
  name: null,
  avatar: null,
  avatarFiles: [],
  role: 'user',
  gender: 'none',
  birthday: null,
  phone: null,
  email: null,
  province: null,
  city: null,
  status: 'active'
})

// 省份和城市列表
const provinceList = ref(getProvinceList())
const cityList = ref([])

// 监听省份变化
const handleProvinceChange = (province) => {
  cityList.value = getCitiesByProvince(province)
  // 清空城市选择
  model.city = null
}

const openEdit = (row) => {
  model.id = row.id
  model.username = row.username
  model.password = ''
  model.name = row.name || null
  model.avatar = row.avatar || null
  model.avatarFiles = row.avatar ? [{ filename: row.avatar }] : []
  model.role = row.role || 'user'
  model.gender = row.gender || 'none'
  model.birthday = row.birthday || null
  model.phone = row.phone || null
  model.email = row.email || null
  model.province = row.province || null
  model.city = row.city || null
  model.status = row.status || 'active'

  // 如果有省份，加载对应的城市列表
  if (row.province) {
    cityList.value = getCitiesByProvince(row.province)
  } else {
    cityList.value = []
  }

  drawer.value = true
}

const closeDrawer = () => {
  drawer.value = false
}

const save = () => {
    form.value
        .validate()
        .then((result) => {
            if (result === true) {
                // 构建更新数据，包含所有字段
                const updateData = {
                    id: model.id,
                    name: model.name,
                    password: model.password || undefined,
                    avatar: model.avatarFiles.length > 0 ? model.avatarFiles[0].filename : model.avatar,
                    role: model.role,
                    gender: model.gender,
                    birthday: model.birthday,
                    phone: model.phone,
                    email: model.email,
                    province: model.province,
                    city: model.city,
                    status: model.status
                }

                request
                    .post('/user/update', updateData)
                    .then((response) => {
                        if (response.data.status === true) {
                            ElMessage.success('保存成功！')
                            closeDrawer()
                            fetch() // 刷新列表
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
