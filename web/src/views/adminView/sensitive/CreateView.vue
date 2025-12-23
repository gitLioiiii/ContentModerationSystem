<template>
    <ElForm ref="form" :model="model" :rules="rules" @submit.prevent="save" label-width="80">
        <ElFormItem prop="word" label="敏感词">
            <ElInput v-model="model.word" placeholder="请输入敏感词" style="width: 100%;" />
        </ElFormItem>
        <ElFormItem prop="category" label="分类">
            <ElSelect v-model="model.category" placeholder="请选择分类" style="width: 100%;">
                <ElOption label="政治敏感" value="political" />
                <ElOption label="色情低俗" value="porn" />
                <ElOption label="暴力恐怖" value="violence" />
                <ElOption label="垃圾邮件" value="spam_mail" />
                <ElOption label="广告营销" value="advertising" />
                <ElOption label="其他" value="other" />
            </ElSelect>
        </ElFormItem>
        <ElFormItem prop="level" label="等级">
            <ElSelect v-model="model.level" placeholder="请选择等级" style="width: 100%;">
                <ElOption label="低风险" value="low" />
                <ElOption label="中风险" value="medium" />
                <ElOption label="高风险" value="high" />
            </ElSelect>
        </ElFormItem>
        <ElFormItem prop="effective" label="启用状态">
            <ElSwitch v-model="model.effective" active-text="已启用" />
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
import { ElForm, ElFormItem, ElInput, ElButton, ElMessage, ElSelect, ElOption, ElSwitch} from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

const form = ref(null)

const model = reactive({
    word: '',
    category: 'other',
    level: 'low',
    effective: true
})

const rules = reactive({
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

const save = () => {
    form.value
        .validate()
        .then((result) => {
            if (result === true) {
                const sensitiveWordData = {
                    word: model.word,
                    category: model.category,
                    level: model.level,
                    effective: model.effective
                }

                request
                    .post('/sensitive/create', sensitiveWordData)
                    .then((response) => {
                        if (response.data.status === true) {
                            ElMessage.success('敏感词创建成功！')
                            router.push({ name: 'sensitive_index' })
                        } else {
                            ElMessage.error(response.data.message || '创建失败！')
                        }
                    })
                    .catch((error) => {
                        console.error('创建敏感词失败:', error)
                        ElMessage.error('创建失败！')
                    })
            }
        })
        .catch(() => {})
}
</script>
