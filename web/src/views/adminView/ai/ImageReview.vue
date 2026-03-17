<template>
    <div class="image-review-container" :class="{ 'dark-theme': isDark }">
        <!-- 页面标题 -->
        <div class="page-header">
            <h1 class="page-title">图像内容审核</h1>
            <p class="page-desc">使用AI进行图像内容审核，快速识别违规图片</p>
        </div>

        <!-- 上方：左右分栏区域 -->
        <div class="top-section">
            <!-- 左侧：图片上传区 -->
            <ElCard class="upload-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <i class="bi bi-image" style="font-size: 20px;"></i>
                        <span>上传图片</span>
                    </div>
                </template>
                <div class="upload-section">
                    <!-- 图片预览区 -->
                    <div
                        v-if="imageUrl"
                        class="image-preview"
                    >
                        <img :src="imageUrl" alt="预览图片" />
                        <div class="image-overlay">
                            <ElButton
                                type="danger"
                                circle
                                @click="removeImage"
                            >
                                <i class="bi bi-trash"></i>
                            </ElButton>
                        </div>
                    </div>

                    <!-- 上传区域 -->
                    <div
                        v-else
                        class="upload-area"
                        :class="{ 'is-dragover': isDragover }"
                        @drop.prevent="handleDrop"
                        @dragover.prevent="isDragover = true"
                        @dragleave.prevent="isDragover = false"
                        @click="triggerFileInput"
                    >
                        <img src="@/assets/img/图片上传.png" alt="上传图片" style="width: 80px; height: 80px;" />
                        <p class="upload-text">点击或拖拽图片到此处上传</p>
                        <p class="upload-desc">支持 JPG、PNG格式，大小不超过 10MB</p>
                    </div>

                    <!-- 隐藏的文件输入 -->
                    <input
                        ref="fileInput"
                        type="file"
                        accept="image/*"
                        style="display: none"
                        @change="handleFileChange"
                    />

                    <!-- 操作按钮 -->
                    <div class="action-buttons">
                        <ElButton
                            type="primary"
                            size="large"
                            :loading="loading"
                            :disabled="!imageFile"
                            @click="handleModerate"
                        >
                            <i class="bi bi-check-lg"></i>
                            {{ loading ? '测试中...' : '开始测试' }}
                        </ElButton>
                        <ElButton
                            size="large"
                            :disabled="loading || !imageFile"
                            @click="removeImage"
                        >
                            <i class="bi bi-trash"></i>
                            清空图片
                        </ElButton>
                    </div>
                </div>
            </ElCard>

            <!-- 右侧：审核结果区 -->
            <ElCard v-if="result" class="result-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <i class="bi bi-check-circle" style="font-size: 20px;"></i>
                        <span>AI审核结果</span>
                    </div>
                </template>
                <div class="result-content">
                    <!-- 审核结论 -->
                    <div class="result-item">
                        <div class="result-label">审核结论</div>
                        <div class="result-value">
                            <ElTag
                                :type="getResultType(result.result)"
                                size="large"
                                effect="dark"
                            >
                                {{ result.result }}
                            </ElTag>
                        </div>
                    </div>

                    <!-- 匹配分数 -->
                    <div class="result-item">
                        <div class="result-label">匹配分数</div>
                        <div class="result-value">
                            <ElTag
                                :type="getMatchScoreType(result.matchScore)"
                                size="large"
                            >
                                {{ result.matchScore.toFixed(1) }}%
                            </ElTag>
                        </div>
                    </div>

                    <!-- 审核原因 -->
                    <div class="result-item">
                        <div class="result-label">审核原因</div>
                        <div class="result-value reason-text">
                            {{ result.reason }}
                        </div>
                    </div>

                    <!-- 原始图片 -->
                    <div class="result-item">
                        <div class="result-label">原始图片</div>
                        <div class="result-value">
                            <img
                                v-if="result.imageUrl"
                                :src="result.imageUrl"
                                alt="审核图片"
                                class="result-image"
                            />
                        </div>
                    </div>
                </div>
            </ElCard>

            <!-- 空状态提示 -->
            <ElCard v-else class="empty-card" shadow="hover">
                <div class="empty-state">
                    <i class="bi bi-image" style="font-size: 80px; color: #909399;"></i>
                    <p class="empty-text">请在左侧上传图片并点击"开始检测"</p>
                    <p class="empty-desc">审核结果将在这里显示</p>
                </div>
            </ElCard>
        </div>

        <!-- 下方：审核历史区域 -->
        <ElCard v-if="historyList.length > 0" class="history-card" shadow="hover">
            <template #header>
                <div class="card-header">
                    <i class="bi bi-clock-history" style="font-size: 20px;"></i>
                    <span>审核历史</span>
                    <ElButton
                        text
                        type="danger"
                        size="small"
                        @click="clearHistory"
                        style="margin-left: auto;"
                    >
                        清空历史
                    </ElButton>
                </div>
            </template>
            <div class="history-list">
                <div
                    v-for="(item, index) in historyList"
                    :key="index"
                    class="history-item"
                    @click="loadHistoryItem(item)"
                >
                    <div class="history-image">
                        <img :src="item.imageUrl" alt="历史图片" />
                    </div>
                    <div class="history-info">
                        <div class="history-header">
                            <ElTag
                                :type="getResultType(item.result)"
                                size="small"
                            >
                                {{ item.result }}
                            </ElTag>
                            <span class="history-time">{{ item.time }}</span>
                        </div>
                        <div class="history-reason">{{ truncateText(item.reason) }}</div>
                    </div>
                </div>
            </div>
        </ElCard>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElCard, ElButton, ElMessage, ElTag } from 'element-plus'
import request from '@/utils/request'
import { buildImageURL } from '@/utils/helper'
import { useTheme } from '@/utils/useTheme'

const { isDark } = useTheme()

// 响应式数据
const imageFile = ref(null)
const imageUrl = ref('')
const loading = ref(false)
const result = ref(null)
const historyList = ref([])
const isDragover = ref(false)
const fileInput = ref(null)

// 触发文件选择
const triggerFileInput = () => {
    fileInput.value.click()
}

// 处理文件选择
const handleFileChange = (event) => {
    const file = event.target.files[0]
    if (file) {
        processFile(file)
    }
}

// 处理拖拽上传
const handleDrop = (event) => {
    isDragover.value = false
    const file = event.dataTransfer.files[0]
    if (file) {
        processFile(file)
    }
}

// 处理文件
const processFile = (file) => {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
        ElMessage.warning('请上传图片文件')
        return
    }

    // 验证文件大小（10MB）
    if (file.size > 10 * 1024 * 1024) {
        ElMessage.warning('图片大小不能超过 10MB')
        return
    }

    imageFile.value = file

    // 生成预览URL
    const reader = new FileReader()
    reader.onload = (e) => {
        imageUrl.value = e.target.result
    }
    reader.readAsDataURL(file)

    // 清空之前的结果
    result.value = null
}

// 移除图片
const removeImage = () => {
    imageFile.value = null
    imageUrl.value = ''
    result.value = null
    if (fileInput.value) {
        fileInput.value.value = ''
    }
}

// AI审核
const handleModerate = async () => {
    if (!imageFile.value) {
        ElMessage.warning('请先上传图片')
        return
    }

    loading.value = true
    result.value = null

    try {
        // 创建 FormData
        const formData = new FormData()
        formData.append('file', imageFile.value)

        const response = await request.post('/moderation/image', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })

        if (response.data.status === true) {
            // 使用服务器返回的图片URL
            const serverImageUrl = buildImageURL(response.data.payload.filename)

            result.value = {
                ...response.data.payload.moderation,
                imageUrl: serverImageUrl,
                originalFilename: response.data.payload.originalFilename
            }

            // 添加到历史记录
            historyList.value.unshift({
                ...response.data.payload.moderation,
                imageUrl: serverImageUrl,
                originalFilename: response.data.payload.originalFilename,
                time: new Date().toLocaleString()
            })

            // 限制历史记录数量
            if (historyList.value.length > 10) {
                historyList.value.pop()
            }

            ElMessage.success('检测完成')
        } else {
            ElMessage.error(response.data.message || '检测失败')
        }
    } catch (error) {
        console.error('图像审核失败:', error)
        ElMessage.error('检测失败，请重试')
    } finally {
        loading.value = false
    }
}

// 清空历史记录
const clearHistory = () => {
    historyList.value = []
    ElMessage.success('历史记录已清空')
}

// 加载历史项目
const loadHistoryItem = (item) => {
    imageUrl.value = item.imageUrl
    result.value = item
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 截断文本显示
const truncateText = (text) => {
    if (!text) return ''
    return text.length > 50 ? text.substring(0, 50) + '...' : text
}

// 获取审核结果标签类型
const getResultType = (result) => {
    const typeMap = {
        '通过': 'success',
        '不通过': 'danger',
        '人工审核': 'warning'
    }
    return typeMap[result] || 'info'
}

// 获取匹配分数标签类型
const getMatchScoreType = (score) => {
    if (score < 30) {
        return 'success'  // 低风险（0-30%）
    } else if (score < 70) {
        return 'warning'  // 中等风险（30-70%）
    } else {
        return 'danger'   // 高风险（70-100%）
    }
}
</script>

<style scoped>
.image-review-container {
    padding: 2rem;
    min-height: 100vh;
    background-color: #f5f5f5;
}

/* 页面标题 */
.page-header {
    max-width: 1600px;
    margin: 0 auto 1.5rem;
    padding: 0;
}

.page-title {
    font-size: 2rem;
    font-weight: 700;
    color: #303133;
    margin: 0 0 0.5rem 0;
}

.page-desc {
    font-size: 1rem;
    color: #909399;
    margin: 0;
}

/* 上方左右分栏区域 */
.top-section {
    max-width: 1600px;
    margin: 0 auto 1.5rem;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
    align-items: start;
}

/* 左侧上传卡片 */
.upload-card {
    height: 35rem;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
}

.upload-card :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

/* 右侧审核结果卡片 */
.result-card,
.empty-card {
    height: 35rem;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
}

.result-card :deep(.el-card__body),
.empty-card :deep(.el-card__body) {
    flex: 1;
    overflow-y: auto;
}

/* 卡片头部 */
.card-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 1.125rem;
    font-weight: 600;
    color: #303133;
}

/* 上传区域 */
.upload-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    height: 60%;
}

.upload-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    background-color: #fafafa;
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 2rem;
}

.upload-area:hover {
    border-color: #409eff;
    background-color: #f0f9ff;
}

.upload-area.is-dragover {
    border-color: #409eff;
    background-color: #e6f7ff;
}

.upload-text {
    font-size: 1rem;
    color: #606266;
    margin: 1rem 0 0.5rem;
    font-weight: 500;
}

.upload-desc {
    font-size: 0.875rem;
    color: #909399;
    margin: 0;
}

/* 图片预览区 */
.image-preview {
    flex: 1;
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    background-color: #000;
    display: flex;
    align-items: center;
    justify-content: center;
}

.image-preview img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.image-preview:hover .image-overlay {
    opacity: 1;
}

.action-buttons {
    display: flex;
    gap: 1rem;
    justify-content: center;
}

.action-buttons .el-button {
    flex: 1;
    max-width: 180px;
}

/* 空状态 */
.empty-card {
    transition: all 0.3s ease;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 2rem;
    text-align: center;
}

.empty-text {
    font-size: 1rem;
    color: #606266;
    margin: 1rem 0 0.5rem;
    font-weight: 500;
}

.empty-desc {
    font-size: 0.875rem;
    color: #909399;
    margin: 0;
}

/* 审核结果 */
.result-content {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.result-item {
    display: flex;
    align-items: flex-start;
    gap: 1rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid #ebeef5;
}

.result-item:last-child {
    border-bottom: none;
    padding-bottom: 0;
}

.result-label {
    font-size: 15px;
    font-weight: 600;
    color: #606266;
    min-width: 100px;
    flex-shrink: 0;
}

.result-value {
    flex: 1;
    font-size: 15px;
    color: #303133;
    word-break: break-word;
}

.reason-text {
    line-height: 1.6;
}

.result-image {
    max-width: 100%;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 下方审核历史区域 */
.history-card {
    max-width: 1600px;
    margin: 0 auto;
    transition: all 0.3s ease;
}

.history-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1rem;
    max-height: 400px;
    overflow-y: auto;
}

.history-item {
    display: flex;
    gap: 1rem;
    padding: 1rem;
    background-color: #f5f7fa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
    border: 2px solid transparent;
}

.history-item:hover {
    background-color: #ebeef5;
    border-color: #409eff;
}

.history-image {
    width: 80px;
    height: 80px;
    flex-shrink: 0;
    border-radius: 8px;
    overflow: hidden;
    background-color: #000;
}

.history-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.history-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.history-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.history-time {
    font-size: 13px;
    color: #909399;
}

.history-reason {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .top-section {
        grid-template-columns: 1fr;
    }

    .upload-card,
    .result-card,
    .empty-card {
        height: auto;
        min-height: 30rem;
    }
}

@media (max-width: 768px) {
    .image-review-container {
        padding: 1rem;
    }

    .page-header {
        margin-bottom: 1rem;
    }

    .page-title {
        font-size: 1.5rem;
    }

    .page-desc {
        font-size: 0.875rem;
    }

    .top-section {
        gap: 1rem;
    }

    .action-buttons {
        flex-direction: column;
    }

    .action-buttons .el-button {
        max-width: 100%;
    }

    .result-item {
        flex-direction: column;
        gap: 0.5rem;
    }

    .result-label {
        min-width: auto;
    }

    .history-list {
        grid-template-columns: 1fr;
        max-height: 300px;
    }

    .history-item {
        flex-direction: column;
    }

    .history-image {
        width: 100%;
        height: 200px;
    }
}

/* ======= 暗黑主题 ======= */
.image-review-container.dark-theme {
    background-color: #121212;
}

.image-review-container.dark-theme .page-title {
    color: #e5e5e5;
}

.image-review-container.dark-theme .page-desc {
    color: #6a6a6a;
}

.image-review-container.dark-theme .card-header {
    color: #e5e5e5;
}

.image-review-container.dark-theme .upload-area {
    border-color: #3a3a3a;
    background-color: #2a2a2a;
}

.image-review-container.dark-theme .upload-area:hover {
    border-color: #409eff;
    background-color: #1a2a3a;
}

.image-review-container.dark-theme .upload-area.is-dragover {
    border-color: #409eff;
    background-color: #1a2a3a;
}

.image-review-container.dark-theme .upload-text {
    color: #a0a0a0;
}

.image-review-container.dark-theme .upload-desc {
    color: #6a6a6a;
}

.image-review-container.dark-theme .empty-text {
    color: #a0a0a0;
}

.image-review-container.dark-theme .empty-desc {
    color: #6a6a6a;
}

.image-review-container.dark-theme .result-item {
    border-bottom-color: #3a3a3a;
}

.image-review-container.dark-theme .result-label {
    color: #a0a0a0;
}

.image-review-container.dark-theme .result-value {
    color: #e5e5e5;
}

.image-review-container.dark-theme .history-item {
    background-color: #1e1e1e;
}

.image-review-container.dark-theme .history-item:hover {
    background-color: #2a2a2a;
}

.image-review-container.dark-theme .history-time {
    color: #6a6a6a;
}

.image-review-container.dark-theme .history-reason {
    color: #a0a0a0;
}
</style>
