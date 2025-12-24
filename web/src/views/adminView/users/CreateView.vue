<template>
    <ElForm ref="form" :model="model" :rules="rules" @submit.prevent="save" label-width="80">
        <ElFormItem prop="avatar" label="头像">
            <FileUploader v-model="model.avatar" :limit="1">
                <template #default="{ file }">
                    <ElImage
                        :src="buildURL(file.filename)"
                        :alt="file.filename"
                        style="width: 80px; height: 80px"
                        fit="cover"
                    />
                </template>
                <template #trigger>
                    <ElButton type="primary" size="small">上传头像</ElButton>
                </template>
            </FileUploader>
        </ElFormItem>
        <ElFormItem prop="username" label="用户名">
            <ElInput v-model="model.username" placeholder="请输入用户名" style="width: 100%;" />
        </ElFormItem>
        <ElFormItem prop="name" label="姓名">
            <ElInput v-model="model.name" placeholder="请输入姓名" style="width: 100%;" />
        </ElFormItem>
        <ElFormItem prop="password" label="密码">
            <ElInput v-model="model.password" type="password" placeholder="请输入密码" show-password style="width: 100%;" />
        </ElFormItem>
        <ElFormItem prop="role" label="角色">
            <ElSelect v-model="model.role" placeholder="请选择角色" style="width: 100%;">
                <ElOption label="用户" value="user" />
                <ElOption label="管理员" value="admin" />
            </ElSelect>
        </ElFormItem>
        <ElFormItem prop="gender" label="性别">
            <ElRadioGroup v-model="model.gender">
                <ElRadio value="man">男</ElRadio>
                <ElRadio value="woman">女</ElRadio>
                <ElRadio value="none">保密</ElRadio>
            </ElRadioGroup>
        </ElFormItem>
        <ElFormItem prop="birthday" label="生日">
            <ElDatePicker
                v-model="model.birthday"
                type="date"
                placeholder="请选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
            />
        </ElFormItem>
        <ElFormItem prop="phone" label="手机号">
            <ElInput v-model="model.phone" placeholder="请输入手机号" style="width: 100%;" />
        </ElFormItem>
        <ElFormItem prop="email" label="邮箱">
            <ElInput v-model="model.email" placeholder="请输入邮箱" style="width: 100%;" />
        </ElFormItem>
        <ElFormItem prop="province" label="省份">
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
        <ElFormItem prop="city" label="城市">
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
        <ElFormItem prop="status" label="账号状态">
            <ElSelect v-model="model.status" placeholder="请选择账号状态" style="width: 100%;">
                <ElOption label="正常" value="active" />
                <ElOption label="禁用" value="ban" />
            </ElSelect>
        </ElFormItem>
        <ElFormItem>
            <ElButton native-type="submit" type="primary">保存</ElButton>
            <ElButton @click="router.back()">取消</ElButton>
        </ElFormItem>
    </ElForm>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElImage,
    ElSelect, ElOption, ElDatePicker, ElRadioGroup, ElRadio
} from 'element-plus'

import FileUploader from '@/components/FileUploader.vue'
import request from '@/utils/request'
import { buildURL } from '@/utils/helper'
import { getProvinceList, getCitiesByProvince } from '@/utils/regions'

const router = useRouter()

const form = ref(null)

const model = reactive({
    avatar: [],
    username: '',
    name: '',
    password: '',
    role: 'user',
    gender: 'none',
    birthday: null,
    phone: null,
    email: null,
    province: null,
    city: null,
    status: 'active'
})

const rules = reactive({
    username: [
        { required: true, message: '请输入用户名。', trigger: 'blur' },
        { min: 2, max: 16, message: '用户名仅限2~16个字符。', trigger: 'change' },
    ],
    password: [
        { required: true, message: '请输入密码。', trigger: 'blur' },
        { min: 6, max: 16, message: '密码仅限6~16个字符。', trigger: 'change' },
    ],
    email: [
        { type: 'email', message: '请输入有效的邮箱地址。', trigger: 'blur' }
    ],
    phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号。', trigger: 'blur' }
    ]
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

const save = () => {
    form.value
        .validate()
        .then((result) => {
            if (result === true) {
                // 处理头像数据
                const userData = {
                    username: model.username,
                    name: model.name,
                    password: model.password,
                    avatar: model.avatar.length > 0 ? model.avatar[0].filename : null,
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
                    .post('/user/create', userData)
                    .then((response) => {
                        if (response.data.status === true) {
                            ElMessage.success('用户创建成功！')
                            router.push({ name: 'user_index' })
                        } else {
                            ElMessage.error(response.data.message || '创建失败！')
                        }
                    })
                    .catch((error) => {
                        console.error('创建用户失败:', error)
                        ElMessage.error('创建失败！')
                    })
            }
        })
        .catch(() => {})
}
</script>
