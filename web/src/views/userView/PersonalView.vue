<template>
    <div class="personal-view" :class="{ 'dark-theme': isDark }">
      <!-- Tab 切换 -->
        <ElTabs v-model="activeTab" >
            <ElTabPane label="个人信息" name="personal">
                <div class="personal-container" :style="containerBackground">
                    <div class="cards-wrapper">

            <!-- 基本信息卡片 -->
            <ElCard class="settings-card" :body-style="{ padding: '0' }" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <i class="bi bi-person" style="font-size: 20px;"></i>
                        <span>基本信息</span>
                    </div>
                </template>
                <div class="information-list">
                    <!-- 头像 -->
                    <div class="information-item avatar-item" @click="openAvatarDialog">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-camera" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">个人资料照片</div>
                                        <div class="item-desc">更换个人资料照片可帮助您个性化您的账号</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <ElAvatar
                                        :size="60"
                                        :src="model.avatar.length > 0 ? buildURL(model.avatar[0].filename) : undefined"
                                    >
                                        <i class="bi bi-person" style="font-size: 40px;"></i>
                                    </ElAvatar>
                                </div>
                            </div>
                        </ElButton>
                    </div>

                    <!-- 姓名 -->
                    <div class="information-item" @click="openEditDialog('name')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-person" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">姓名</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">{{ model.name || '未设置' }}</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>

                    <!-- 生日 -->
                    <div class="information-item" @click="openEditDialog('birthday')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-calendar" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">生日</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">{{ formatBirthday(model.birthday) || '未设置' }}</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>

                    <!-- 性别 -->
                    <div class="information-item" @click="openEditDialog('gender')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-gender-ambiguous" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">性别</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">{{ formatGender(model.gender) }}</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>
                </div>
            </ElCard>

            <!-- 联系信息卡片 -->
            <ElCard class="settings-card" :body-style="{ padding: '0' }" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <i class="bi bi-envelope" style="font-size: 20px;"></i>
                        <span>联系信息</span>
                    </div>
                </template>
                <div class="information-list">
                    <!-- 邮箱 -->
                    <div class="information-item" @click="openEditDialog('email')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-envelope" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">电子邮箱</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">{{ model.email || '未设置' }}</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>

                    <!-- 手机号 -->
                    <div class="information-item" @click="openEditDialog('phone')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-telephone" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">电话</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">{{ model.phone || '未设置' }}</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>

                    <!-- 省份 -->
                    <div class="information-item" @click="openEditDialog('province')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-geo-alt" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">省份</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">{{ model.province || '未设置' }}</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>

                    <!-- 城市 -->
                    <div class="information-item" @click="openEditDialog('city')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-geo-alt" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">城市</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">{{ model.city || '未设置' }}</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>
                </div>
            </ElCard>

            <!-- 账号安全卡片 -->
            <ElCard class="settings-card" :body-style="{ padding: '0' }" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <i class="bi bi-lock" style="font-size: 20px;"></i>
                        <span>账号安全</span>
                    </div>
                </template>
                <div class="information-list">
                    <!-- 账号 -->
                    <div class="information-item">
                        <div class="information-button-disabled">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-display" style="font-size: 24px; color: #9aa0a6;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">账号</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text disabled-text">{{ model.username }}</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 密码 -->
                    <div class="information-item" @click="openEditDialog('password')">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-lock" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">密码</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <div class="value-text">••••••••</div>
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>

                    <!-- 更换背景 -->
                    <div class="information-item" @click="uploadBackground">
                        <ElButton text class="information-button">
                            <div class="item-content">
                                <div class="item-start">
                                    <div class="item-icon">
                                        <i class="bi bi-image" style="font-size: 24px; color: #5f6368;"></i>
                                    </div>
                                    <div class="item-main">
                                        <div class="item-label">主题背景</div>
                                        <div class="item-desc">更换个人设置页面的背景图片</div>
                                    </div>
                                </div>
                                <div class="item-value">
                                    <i class="bi bi-chevron-right arrow-icon"></i>
                                </div>
                            </div>
                        </ElButton>
                    </div>
                </div>
            </ElCard>
                    </div>
                </div>
            </ElTabPane>
            <!-- 用户作品页面 -->
            <ElTabPane label="我的作品" name="works">
                <WorksView />
            </ElTabPane>
        </ElTabs>

        <!-- 头像编辑模态框 -->
        <ElDialog
            v-model="avatarDialogVisible"
            title="个人资料照片"
            width="500px"
            :close-on-click-modal="true"
        >
            <div class="avatar-dialog-content">
                <div class="avatar-dialog-preview">
                    <ElImage
                        v-if="model.avatar.length > 0"
                        :src="buildURL(model.avatar[0].filename)"
                        fit="cover"
                        class="preview-image"
                    />
                    <div v-else class="no-avatar">
                        <i class="bi bi-person-circle"></i>
                    </div>
                </div>
                <div class="avatar-actions">
                    <ElButton type="primary" @click="changeAvatar">
                        <i class="bi bi-arrow-repeat"></i>
                        更改
                    </ElButton>
                    <ElButton type="danger" @click="removeAvatar" :disabled="model.avatar.length === 0">
                        <i class="bi bi-trash"></i>
                        移除
                    </ElButton>
                </div>
            </div>
        </ElDialog>

        <!-- 编辑信息对话框 -->
        <ElDialog
            v-model="editDialogVisible"
            :title="editDialogTitle"
            width="500px"
            :close-on-click-modal="true"
        >
            <ElForm ref="editForm" :model="editModel" :rules="editRules" label-width="80px">
                <!-- 姓名 -->
                <ElFormItem v-if="editField === 'name'" prop="value" label="姓名">
                    <ElInput v-model="editModel.value" placeholder="请输入姓名" />
                </ElFormItem>

                <!-- 生日 -->
                <ElFormItem v-if="editField === 'birthday'" prop="value" label="生日">
                    <ElDatePicker
                        v-model="editModel.value"
                        type="date"
                        placeholder="请选择生日"
                        format="YYYY年M月D日"
                        value-format="YYYY-MM-DD"
                        style="width: 100%;"
                    />
                </ElFormItem>

                <!-- 性别 -->
                <ElFormItem v-if="editField === 'gender'" prop="value" label="性别">
                    <ElRadioGroup v-model="editModel.value">
                        <ElRadio value="man">男</ElRadio>
                        <ElRadio value="woman">女</ElRadio>
                        <ElRadio value="none">保密</ElRadio>
                    </ElRadioGroup>
                </ElFormItem>

                <!-- 邮箱 -->
                <ElFormItem v-if="editField === 'email'" prop="value" label="邮箱">
                    <ElInput v-model="editModel.value" placeholder="请输入邮箱" />
                </ElFormItem>

                <!-- 手机号 -->
                <ElFormItem v-if="editField === 'phone'" prop="value" label="手机号">
                    <ElInput v-model="editModel.value" placeholder="请输入手机号" />
                </ElFormItem>

                <!-- 省份 -->
                <ElFormItem v-if="editField === 'province'" prop="value" label="省份">
                    <ElSelect
                        v-model="editModel.value"
                        placeholder="请选择省份"
                        filterable
                        @change="handleProvinceChange"
                    >
                        <ElOption
                            v-for="province in provinceList"
                            :key="province.value"
                            :label="province.label"
                            :value="province.value"
                        />
                    </ElSelect>
                </ElFormItem>

                <!-- 城市 -->
                <ElFormItem v-if="editField === 'city'" prop="value" label="城市">
                    <ElSelect
                        v-model="editModel.value"
                        placeholder="请选择城市"
                        filterable
                        :disabled="cityList.length === 0"
                    >
                        <ElOption
                            v-for="city in cityList"
                            :key="city.value"
                            :label="city.label"
                            :value="city.value"
                        />
                    </ElSelect>
                    <div v-if="cityList.length === 0" style="color: #909399; font-size: 12px; margin-top: 4px;">
                        请先选择省份
                    </div>
                </ElFormItem>

                <!-- 密码 -->
                <ElFormItem v-if="editField === 'password'" prop="value" label="新密码">
                    <ElInput
                        v-model="editModel.value"
                        type="password"
                        placeholder="请输入新密码"
                        show-password
                    />
                </ElFormItem>
            </ElForm>

            <template #footer>
                <div class="dialog-footer">
                    <ElButton @click="editDialogVisible = false">取消</ElButton>
                    <ElButton type="primary" @click="saveEdit">保存</ElButton>
                </div>
            </template>
        </ElDialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElCard, ElImage, ElDialog,
  ElDatePicker, ElRadioGroup, ElRadio, ElAvatar, ElSelect, ElOption, ElTabs, ElTabPane
} from 'element-plus'
import 'bootstrap-icons/font/bootstrap-icons.css'

import request from '@/utils/request'
import { buildURL, buildThemeURL } from '@/utils/helper'
import { useUserStore } from '@/stores/user'
import { getProvinceList, getCitiesByProvince } from '@/utils/regions'
import { useTheme } from '@/utils/useTheme'
import WorksView from '@/views/userView/WorksView.vue'

const userStore = useUserStore()
const { isDark } = useTheme()
const activeTab = ref('personal')
const editForm = ref(null)
const avatarDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editField = ref('')
const editDialogTitle = ref('')

const model = reactive({
    id: null,
    avatar: [],
    username: '',
    name: '',
    birthday: '',
    gender: 'none',
    email: '',
    phone: '',
    province: '',
    city: '',
    themeImage: [],
})

const editModel = reactive({
    value: ''
})

// 省份和城市列表
const provinceList = ref(getProvinceList())
const cityList = ref([])

// 监听省份变化
const handleProvinceChange = (province) => {
    // 获取该省份的城市列表
    cityList.value = getCitiesByProvince(province)
    // 清空城市选择
    if (editField.value === 'province') {
        // 如果当前正在编辑省份，同时更新城市为空
        model.city = ''
    }
}

// 字段标题映射
const fieldTitleMap = {
    name: '编辑姓名',
    birthday: '编辑生日',
    gender: '编辑性别',
    email: '编辑邮箱',
    phone: '编辑手机号',
    province: '编辑省份',
    city: '编辑城市',
    password: '修改密码'
}

// 编辑表单验证规则
const editRules = computed(() => {
    const rulesMap = {
        name: [
            { max: 32, message: '姓名最多32个字符。', trigger: 'change' },
        ],
        email: [
            { type: 'email', message: '请输入正确的邮箱格式。', trigger: 'blur' },
            { max: 100, message: '邮箱最多100个字符。', trigger: 'change' },
        ],
        phone: [
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式。', trigger: 'blur' },
        ],
        province: [
            { max: 50, message: '省份最多50个字符。', trigger: 'change' },
        ],
        city: [
            { max: 50, message: '城市最多50个字符。', trigger: 'change' },
        ],
        password: [
            { required: true, message: '请输入新密码。', trigger: 'blur' },
            { min: 6, max: 16, message: '密码仅限6~16个字符。', trigger: 'change' },
        ],
    }

    return {
        value: rulesMap[editField.value] || []
    }
})

// 获取当前用户信息
const fetchUserInfo = () => {
    request.get('/user/current').then((response) => {
        if (response.data.status === true) {
            const userData = response.data.payload.user
            model.id = userData.id
            model.username = userData.username
            model.name = userData.name || ''
            model.birthday = userData.birthday || ''
            model.gender = userData.gender || 'none'
            model.email = userData.email || ''
            model.phone = userData.phone || ''
            model.province = userData.province || ''
            model.city = userData.city || ''
            // 如果有头像，初始化头像数组
            if (userData.avatar) {
                model.avatar = [{ filename: userData.avatar }]
            }
            // 如果有主题背景，初始化主题背景数组
            if (userData.themeImage) {
                model.themeImage = [{ filename: userData.themeImage }]
            }
            userStore.updateUser(userData)
        } else {
            ElMessage.error('获取用户信息失败！')
        }
    }).catch((error) => {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败！')
    })
}

// 计算容器背景样式
const containerBackground = computed(() => {
    if (model.themeImage.length > 0) {
        const bgUrl = buildThemeURL(model.themeImage[0].filename)
        const overlay = isDark.value
            ? 'rgba(18, 18, 18, 0.60)'
            : 'rgba(255, 255, 255, 0.50)'
        return {
            backgroundImage: `linear-gradient(${overlay}, ${overlay}), url('${bgUrl}')`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            backgroundRepeat: 'no-repeat',
        }
    }
    return {}
})

// 上传主题背景
const uploadBackground = () => {
    const fileInput = document.createElement('input')
    fileInput.setAttribute('type', 'file')
    fileInput.setAttribute('accept', 'image/*')
    fileInput.addEventListener('input', (e) => {
        if (e.target.files.length > 0) {
            const formData = new FormData()
            formData.append('file', e.target.files[0], e.target.files[0].name)
            request.post('/upload/themes', formData).then((response) => {
                if (response.data.status === true) {
                    model.themeImage = [response.data.payload]
                    // 保存主题背景到数据库
                    const updateData = {
                        id: model.id,
                        name: model.name,
                        birthday: model.birthday,
                        gender: model.gender,
                        email: model.email,
                        phone: model.phone,
                        province: model.province,
                        city: model.city,
                        avatar: model.avatar.length > 0 ? model.avatar[0].filename : null,
                        themeImage: model.themeImage[0].filename
                    }
                    request.post('/user/update', updateData).then((updateResponse) => {
                        if (updateResponse.data.status === true) {
                            ElMessage.success('主题背景更换成功！')
                            // 更新用户信息到store
                            if (updateResponse.data.payload && updateResponse.data.payload.user) {
                                userStore.updateUser(updateResponse.data.payload.user)
                            }
                        } else {
                            ElMessage.error('主题背景保存失败！')
                        }
                    }).catch(() => {
                        ElMessage.error('主题背景保存失败！')
                    })
                } else {
                    ElMessage.error('主题背景上传失败！')
                }
            }).catch(() => {
                ElMessage.error('主题背景上传失败！')
            })
        }
    })
    fileInput.click()
}

// 页面加载时获取用户信息
onMounted(() => {
    fetchUserInfo()
})

// 格式化生日显示
const formatBirthday = (birthday) => {
    if (!birthday) return ''
    const date = new Date(birthday)
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

// 格式化性别显示
const formatGender = (gender) => {
    const genderMap = {
        man: '男',
        woman: '女',
        none: '保密'
    }
    return genderMap[gender] || '保密'
}

// 打开编辑对话框
const openEditDialog = (field) => {
    editField.value = field
    editDialogTitle.value = fieldTitleMap[field]

    // 设置当前值(密码除外)
    if (field === 'password') {
        editModel.value = ''
    } else {
        editModel.value = model[field]
    }

    // 如果编辑城市，需要根据当前省份加载城市列表
    if (field === 'city') {
        if (model.province) {
            cityList.value = getCitiesByProvince(model.province)
        } else {
            cityList.value = []
        }
    }

    editDialogVisible.value = true
}

// 保存编辑
const saveEdit = () => {
    if (!editForm.value) {
        return
    }

    editForm.value.validate().then((valid) => {
        if (valid) {
            // 构建更新数据
            const updateData = {
                id: model.id,
                name: model.name,
                birthday: model.birthday,
                gender: model.gender,
                email: model.email,
                phone: model.phone,
                province: model.province,
                city: model.city,
                avatar: model.avatar.length > 0 ? model.avatar[0].filename : null,
                themeImage: model.themeImage.length > 0 ? model.themeImage[0].filename : null
            }

            // 更新对应字段
            updateData[editField.value] = editModel.value

            request.post('/user/update', updateData)
                .then((response) => {
                    if (response.data.status === true) {
                        ElMessage.success('保存成功!')
                        // 更新本地数据
                        if (editField.value !== 'password') {
                            model[editField.value] = editModel.value
                        }
                        // 更新用户信息到store
                        if (response.data.payload && response.data.payload.user) {
                            userStore.updateUser(response.data.payload.user)
                        }
                        editDialogVisible.value = false
                        // 重新获取用户信息
                        fetchUserInfo()
                    } else {
                        ElMessage.error(response.data.message || '保存失败!')
                    }
                })
                .catch((error) => {
                    console.error('更新用户信息失败:', error)
                    ElMessage.error('保存失败!')
                })
        }
    }).catch(() => {})
}

// 打开头像编辑对话框
const openAvatarDialog = () => {
    avatarDialogVisible.value = true
}

// 更改头像
const changeAvatar = () => {
    // 触发 FileUploader 的文件选择
    const fileInput = document.createElement('input')
    fileInput.setAttribute('type', 'file')
    fileInput.setAttribute('accept', 'image/*')
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            const formData = new FormData()
            formData.append('file', e.target.files[0], e.target.files[0].name)
            request.post('/upload/avatars', formData).then((response) => {
                if (response.data.status === true) {
                    model.avatar = [response.data.payload]
                    // 保存头像到数据库
                    const updateData = {
                        id: model.id,
                        name: model.name,
                        birthday: model.birthday,
                        gender: model.gender,
                        email: model.email,
                        phone: model.phone,
                        province: model.province,
                        city: model.city,
                        avatar: model.avatar[0].filename,
                        themeImage: model.themeImage.length > 0 ? model.themeImage[0].filename : null
                    }
                    request.post('/user/update', updateData).then((updateResponse) => {
                        if (updateResponse.data.status === true) {
                            ElMessage.success('头像更换成功!')
                            // 更新用户信息到store
                            if (updateResponse.data.payload && updateResponse.data.payload.user) {
                                userStore.updateUser(updateResponse.data.payload.user)
                            }
                            avatarDialogVisible.value = false
                        } else {
                            ElMessage.error('头像保存失败!')
                        }
                    }).catch(() => {
                        ElMessage.error('头像保存失败!')
                    })
                } else {
                    ElMessage.error('头像上传失败!')
                }
            }).catch(() => {
                ElMessage.error('头像上传失败!')
            })
        }
    })
    fileInput.click()
}

// 移除头像
const removeAvatar = () => {
    model.avatar = []
    avatarDialogVisible.value = false
    ElMessage.success('头像已移除!')
}
</script>

<style scoped>

.page-header {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 0 1rem 0;
}

.page-title {
    font-size: 2rem;
    font-weight: 700;
    color: #303133;
    margin: 0;
}

:deep(.user-center-tabs > .el-tabs__header) {
    margin-bottom: 1.5rem;
}

:deep(.user-center-tabs .el-tabs__item) {
    font-size: 16px;
    font-weight: 500;
}

.personal-container {
    display: flex;
    justify-content: center;
    padding: 1rem 0;
    border-radius: 8px;
}

.cards-wrapper {
    width: 100%;
    max-width: 40rem;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

/* 卡片样式 */
.settings-card {
    width: 100%;
    transition: all 0.3s ease;
    overflow: hidden;
    background-color: rgba(255, 255, 255, 0.65);
}

:deep(.settings-card .el-card__header) {
    background-color: rgba(255, 255, 255, 0.80);
}

/* 卡片标题 */
.card-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 1.125rem;
    font-weight: 600;
    color: #303133;
}

.information-list {
    display: flex;
    flex-direction: column;
}

/* 信息卡片项 */
.information-item {
    position: relative;
}

.information-item:not(:last-child)::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 88px;
    right: 24px;
    height: 1px;
    background-color: #ebeef5;
}

.information-button {
    width: 100%;
    height: auto;
    padding: 16px 24px;
    border-radius: 0;
    text-align: left;
    color: inherit;
    display: block;
}

.information-button:hover {
    background-color: #f5f7fa !important;
    color: inherit !important;
}

.information-button:focus {
    background-color: transparent !important;
    color: inherit !important;
}

.information-button-disabled {
    width: 100%;
    padding: 16px 24px;
    cursor: not-allowed;
    opacity: 0.6;
}

.item-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    gap: 16px;
}

.item-start {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
    min-width: 0;
}

.item-icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f0f2f5;
    border-radius: 50%;
    flex-shrink: 0;
}

.item-main {
    flex: 1;
    min-width: 0;
}

.item-label {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
    line-height: 1.5;
}

.item-desc {
    font-size: 13px;
    color: #909399;
    line-height: 1.5;
    margin-top: 2px;
}

.item-value {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
}

.value-text {
    font-size: 14px;
    color: #606266;
    white-space: nowrap;
}

.disabled-text {
    color: #a8abb2;
}

.arrow-icon {
    color: #c0c4cc;
    font-size: 14px;
    transition: transform 0.3s ease;
}

.information-button:hover .arrow-icon {
    transform: translateX(4px);
    color: #909399;
}

/* 头像距离左边的间距 */
.avatar-item .item-content {
    gap: 4rem;
}

:deep(.el-avatar) {
    background-color: #f0f2f5;
    color: #909399;
    border: 2px solid #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 头像对话框样式 */
.avatar-dialog-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 0;
}

.avatar-dialog-preview {
    width: 200px;
    height: 200px;
    border-radius: 50%;
    overflow: hidden;
    margin-bottom: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.no-avatar {
    font-size: 120px;
    color: #c0c4cc;
    display: flex;
    align-items: center;
    justify-content: center;
}

.avatar-actions {
    display: flex;
    gap: 16px;
}

.avatar-actions .el-button {
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 120px;
}

/* 编辑对话框 */
.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

:deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__body) {
    padding: 24px;
}

:deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #ebeef5;
}

/* 响应式布局 */
@media (max-width: 768px) {
    .user-center-container {
        padding: 1rem;
    }

    .page-title {
        font-size: 1.5rem;
    }

    .personal-container {
        padding: 0.5rem 0;
    }

    .cards-wrapper {
        max-width: 100%;
        gap: 1rem;
    }

    .settings-card {
        box-shadow: none;
    }

    .information-button,
    .information-button-disabled {
        padding: 12px 16px;
    }

    .information-item:not(:last-child)::after {
        left: 72px;
        right: 16px;
    }

    .item-start {
        gap: 12px;
    }

    .item-icon {
        width: 36px;
        height: 36px;
    }

    .item-label {
        font-size: 14px;
    }

    .item-desc {
        font-size: 12px;
    }

    .value-text {
        font-size: 13px;
    }

    :deep(.el-avatar) {
        width: 50px !important;
        height: 50px !important;
    }

    .avatar-dialog-preview {
        width: 160px;
        height: 160px;
    }

    .avatar-actions .el-button {
        min-width: 100px;
    }
}

/* ======= 暗黑主题 ======= */
.personal-view.dark-theme .page-title {
    color: #e5e5e5;
}

.personal-view.dark-theme .card-header {
    color: #e5e5e5;
}

.personal-view.dark-theme .settings-card {
    background-color: rgba(30, 30, 30, 0.65);
}

.personal-view.dark-theme :deep(.settings-card .el-card__header) {
    background-color: rgba(30, 30, 30, 0.80);
}

.personal-view.dark-theme .information-item:not(:last-child)::after {
    background-color: #3a3a3a;
}

.personal-view.dark-theme .information-button:hover {
    background-color: #2a2a2a !important;
}

.personal-view.dark-theme .item-icon {
    background-color: #3a3a3a;
}

.personal-view.dark-theme .item-icon i {
    color: #a0a0a0 !important;
}

.personal-view.dark-theme .item-label {
    color: #e5e5e5;
}

.personal-view.dark-theme .item-desc {
    color: #6a6a6a;
}

.personal-view.dark-theme .value-text {
    color: #a0a0a0;
}

.personal-view.dark-theme .disabled-text {
    color: #555;
}

.personal-view.dark-theme .arrow-icon {
    color: #555;
}

.personal-view.dark-theme :deep(.el-avatar) {
    background-color: #3a3a3a;
    border-color: #2a2a2a;
}

.personal-view.dark-theme .avatar-dialog-preview {
    background-color: #2a2a2a;
}

.personal-view.dark-theme :deep(.el-dialog__header) {
    border-bottom-color: #3a3a3a;
}

.personal-view.dark-theme :deep(.el-dialog__footer) {
    border-top-color: #3a3a3a;
}
</style>
