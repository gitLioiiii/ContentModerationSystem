<template>
    <div class="video-review-container" :class="{ 'dark-theme': isDark }">
        <!-- 页面标题 -->
        <div class="page-header">
            <h1 class="page-title">视频内容审核</h1>
            <p class="page-desc">使用AI对视频进行抽帧审核，快速识别违规视频内容</p>
        </div>

        <!-- 上方：左右分栏区域 -->
        <div class="top-section">
            <!-- 左侧：视频上传区 -->
            <ElCard class="upload-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <i class="bi bi-camera-video" style="font-size: 20px;"></i>
                        <span>上传视频</span>
                    </div>
                </template>
                <div class="upload-section">
                    <!-- 视频预览区 -->
                    <div
                        v-if="videoUrl"
                        class="video-preview"
                    >
                        <video
                            ref="videoPlayer"
                            :src="videoUrl"
                            controls
                            @loadedmetadata="handleVideoLoaded"
                        ></video>
                        <div class="video-overlay">
                            <ElButton
                                type="danger"
                                circle
                                @click="removeVideo"
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
                        <img src="@/assets/img/视频上传.png" alt="上传视频" style="width: 80px; height: 80px;" />
                        <p class="upload-text">点击或拖拽视频到此处上传</p>
                        <p class="upload-desc">支持 MP4、MOV、AVI 格式，大小不超过 100MB</p>
                    </div>

                    <!-- 隐藏的文件输入 -->
                    <input
                        ref="fileInput"
                        type="file"
                        accept="video/*"
                        style="display: none"
                        @change="handleFileChange"
                    />

                    <!-- 抽帧参数设置 -->
                    <div v-if="videoUrl" class="frame-settings">
                        <div class="setting-item">
                            <span class="setting-label">抽帧间隔（秒）：</span>
                            <ElInputNumber
                                v-model="frameInterval"
                                :min="3"
                                :max="5"
                                :disabled="loading"
                            />
                        </div>
                        <div class="setting-info">
                            <i class="bi bi-info-circle"></i>
                            <span>视频时长：{{ videoDuration }}秒，预计抽取 {{ estimatedFrames }} 帧</span>
                        </div>
                    </div>

                    <!-- 操作按钮 -->
                    <div class="action-buttons">
                        <ElButton
                            type="primary"
                            size="large"
                            :loading="loading"
                            :disabled="!videoFile"
                            @click="handleModerate"
                        >
                            <i class="bi bi-check-lg"></i>
                            {{ loading ? '审核中...' : '开始审核' }}
                        </ElButton>
                        <ElButton
                            size="large"
                            :disabled="loading || !videoFile"
                            @click="removeVideo"
                        >
                            <i class="bi bi-trash"></i>
                            清空视频
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
                    <!-- 总体审核结论 -->
                    <div class="result-item">
                        <div class="result-label">总体结论</div>
                        <div class="result-value">
                            <ElTag
                                :type="getResultType(result.overallResult)"
                                size="large"
                                effect="dark"
                            >
                                {{ result.overallResult }}
                            </ElTag>
                        </div>
                    </div>

                    <!-- 抽帧统计 -->
                    <div class="result-item">
                        <div class="result-label">抽帧统计</div>
                        <div class="result-value">
                            <div class="frame-stats">
                                <span>总帧数：{{ result.totalFrames }}</span>
                                <span>违规帧数：{{ result.violationFrames }}</span>
                                <span>违规率：{{ result.violationRate }}%</span>
                            </div>
                        </div>
                    </div>

                    <!-- 审核原因 -->
                    <div class="result-item">
                        <div class="result-label">审核原因</div>
                        <div class="result-value reason-text">
                            {{ result.reason }}
                        </div>
                    </div>

                    <!-- 关键帧展示 -->
                    <div v-if="result.keyFrames && result.keyFrames.length > 0" class="result-item">
                        <div class="result-label">关键帧</div>
                        <div class="result-value">
                            <div class="key-frames">
                                <div
                                    v-for="(frame, index) in result.keyFrames"
                                    :key="index"
                                    class="frame-item"
                                >
                                    <img :src="frame.imageUrl" :alt="`第${frame.frameNumber}帧`" />
                                    <div class="frame-info">
                                        <span class="frame-time">{{ frame.timestamp }}s</span>
                                        <ElTag
                                            :type="getResultType(frame.result)"
                                            size="small"
                                        >
                                            {{ frame.result }}
                                        </ElTag>
                                    </div>
                                    <div v-if="frame.reason" class="frame-reason">
                                        {{ frame.reason }}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </ElCard>

            <!-- 空状态提示 -->
            <ElCard v-else class="empty-card" shadow="hover">
                <div class="empty-state">
                    <i class="bi bi-camera-video" style="font-size: 80px; color: #909399;"></i>
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
                    <div class="history-thumbnail">
                        <i class="bi bi-camera-video" style="font-size: 40px; color: #909399;"></i>
                    </div>
                    <div class="history-info">
                        <div class="history-header">
                            <ElTag
                                :type="getResultType(item.overallResult)"
                                size="small"
                            >
                                {{ item.overallResult }}
                            </ElTag>
                            <span class="history-time">{{ item.time }}</span>
                        </div>
                        <div class="history-stats">
                            总帧数：{{ item.totalFrames }} | 违规帧数：{{ item.violationFrames }} | 违规率：{{ item.violationRate }}%
                        </div>
                        <div class="history-reason">{{ truncateText(item.reason) }}</div>
                    </div>
                </div>
            </div>
        </ElCard>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElCard, ElButton, ElMessage, ElTag, ElInputNumber } from 'element-plus'
import request from '@/utils/request'
import { buildImageURL } from '@/utils/helper'
import { useTheme } from '@/utils/useTheme'

const { isDark } = useTheme()

// 响应式数据
const videoFile = ref(null)
const videoUrl = ref('')
const loading = ref(false)
const result = ref(null)
const historyList = ref([])
const isDragover = ref(false)
const fileInput = ref(null)
const videoPlayer = ref(null)
const videoDuration = ref(0)
const frameInterval = ref(3) 

// 计算预计抽取的帧数
const estimatedFrames = computed(() => {
    if (!videoDuration.value || !frameInterval.value) return 0
    return Math.ceil(videoDuration.value / frameInterval.value)
})

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
    if (!file.type.startsWith('video/')) {
        ElMessage.warning('请上传视频文件')
        return
    }

    // 验证文件大小（100MB）
    if (file.size > 100 * 1024 * 1024) {
        ElMessage.warning('视频大小不能超过 100MB')
        return
    }

    videoFile.value = file

    // 生成预览URL
    const url = URL.createObjectURL(file)
    videoUrl.value = url

    // 清空之前的结果
    result.value = null
}

// 处理视频加载完成
const handleVideoLoaded = () => {
    if (videoPlayer.value) {
        videoDuration.value = Math.floor(videoPlayer.value.duration)
    }
}

// 移除视频
const removeVideo = () => {
    if (videoUrl.value) {
        URL.revokeObjectURL(videoUrl.value)
    }
    videoFile.value = null
    videoUrl.value = ''
    result.value = null
    videoDuration.value = 0
    if (fileInput.value) {
        fileInput.value.value = ''
    }
}

// AI审核
const handleModerate = async () => {
    if (!videoFile.value) {
        ElMessage.warning('请先上传视频')
        return
    }

    loading.value = true
    result.value = null

    try {
        // 创建 FormData
        const formData = new FormData()
        formData.append('file', videoFile.value)
        formData.append('frameInterval', frameInterval.value)

        const response = await request.post('/moderation/video', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            timeout: 300000 // 5分钟超时（视频处理可能需要较长时间）
        })

        if (response.data.status === true) {
            const moderationData = response.data.payload.moderation

            // 处理关键帧的图片URL
            if (moderationData.keyFrames && moderationData.keyFrames.length > 0) {
                moderationData.keyFrames = moderationData.keyFrames.map(frame => ({
                    ...frame,
                    imageUrl: buildImageURL(frame.filename)
                }))
            }

            result.value = moderationData

            // 添加到历史记录
            historyList.value.unshift({
                ...moderationData,
                time: new Date().toLocaleString()
            })

            // 限制历史记录数量
            if (historyList.value.length > 10) {
                historyList.value.pop()
            }

            ElMessage.success('审核完成')
        } else {
            ElMessage.error(response.data.message || '审核失败')
        }
    } catch (error) {
        console.error('视频审核失败:', error)
        ElMessage.error('审核失败，请重试')
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
</script>

<style scoped>
.video-review-container {
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
    min-height: 40rem;
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
    min-height: 40rem;
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
    height: 100%;
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
    min-height: 200px;
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

/* 视频预览区 */
.video-preview {
    flex: 1;
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    background-color: #000;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 300px;
}

.video-preview video {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.video-overlay {
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

.video-preview:hover .video-overlay {
    opacity: 1;
}

/* 抽帧参数设置 */
.frame-settings {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    padding: 1rem;
    background-color: #f5f7fa;
    border-radius: 8px;
}

.setting-item {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.setting-label {
    font-size: 14px;
    color: #606266;
    font-weight: 500;
}

.setting-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 13px;
    color: #909399;
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

.frame-stats {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;
}

.frame-stats span {
    padding: 0.5rem 1rem;
    background-color: #f5f7fa;
    border-radius: 4px;
    font-size: 14px;
    color: #606266;
}

/* 关键帧展示 */
.key-frames {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
}

.frame-item {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    overflow: hidden;
    background-color: #fff;
    transition: all 0.3s ease;
}

.frame-item:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
}

.frame-item img {
    width: 100%;
    height: 150px;
    object-fit: cover;
}

.frame-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0.5rem;
    background-color: #f5f7fa;
}

.frame-time {
    font-size: 13px;
    color: #909399;
}

.frame-reason {
    padding: 0.5rem;
    font-size: 13px;
    color: #606266;
    line-height: 1.4;
    background-color: #fff;
}

/* 下方审核历史区域 */
.history-card {
    max-width: 1600px;
    margin: 0 auto;
    transition: all 0.3s ease;
}

.history-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
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

.history-thumbnail {
    width: 80px;
    height: 80px;
    flex-shrink: 0;
    border-radius: 8px;
    overflow: hidden;
    background-color: #dcdfe6;
    display: flex;
    align-items: center;
    justify-content: center;
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

.history-stats {
    font-size: 13px;
    color: #606266;
}

.history-reason {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .top-section {
        grid-template-columns: 1fr;
    }

    .upload-card,
    .result-card,
    .empty-card {
        min-height: 30rem;
    }
}

@media (max-width: 768px) {
    .video-review-container {
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

    .frame-stats {
        flex-direction: column;
        gap: 0.5rem;
    }

    .key-frames {
        grid-template-columns: 1fr;
    }

    .history-list {
        grid-template-columns: 1fr;
        max-height: 300px;
    }

    .history-item {
        flex-direction: column;
    }

    .history-thumbnail {
        width: 100%;
        height: 120px;
    }
}

/* ======= 暗黑主题 ======= */
.video-review-container.dark-theme {
    background-color: #121212;
}

.video-review-container.dark-theme .page-title {
    color: #e5e5e5;
}

.video-review-container.dark-theme .page-desc {
    color: #6a6a6a;
}

.video-review-container.dark-theme .card-header {
    color: #e5e5e5;
}

.video-review-container.dark-theme .upload-area {
    border-color: #3a3a3a;
    background-color: #2a2a2a;
}

.video-review-container.dark-theme .upload-area:hover {
    border-color: #409eff;
    background-color: #1a2a3a;
}

.video-review-container.dark-theme .upload-area.is-dragover {
    border-color: #409eff;
    background-color: #1a2a3a;
}

.video-review-container.dark-theme .upload-text {
    color: #a0a0a0;
}

.video-review-container.dark-theme .upload-desc {
    color: #6a6a6a;
}

.video-review-container.dark-theme .empty-text {
    color: #a0a0a0;
}

.video-review-container.dark-theme .empty-desc {
    color: #6a6a6a;
}

.video-review-container.dark-theme .frame-settings {
    background-color: #2a2a2a;
}

.video-review-container.dark-theme .setting-label {
    color: #a0a0a0;
}

.video-review-container.dark-theme .setting-info {
    color: #6a6a6a;
}

.video-review-container.dark-theme .result-item {
    border-bottom-color: #3a3a3a;
}

.video-review-container.dark-theme .result-label {
    color: #a0a0a0;
}

.video-review-container.dark-theme .result-value {
    color: #e5e5e5;
}

.video-review-container.dark-theme .frame-stats span {
    background-color: #2a2a2a;
    color: #a0a0a0;
}

.video-review-container.dark-theme .frame-item {
    border-color: #3a3a3a;
    background-color: #1e1e1e;
}

.video-review-container.dark-theme .frame-info {
    background-color: #2a2a2a;
}

.video-review-container.dark-theme .frame-reason {
    background-color: #1e1e1e;
    color: #a0a0a0;
}

.video-review-container.dark-theme .history-item {
    background-color: #1e1e1e;
}

.video-review-container.dark-theme .history-item:hover {
    background-color: #2a2a2a;
}

.video-review-container.dark-theme .history-thumbnail {
    background-color: #3a3a3a;
}

.video-review-container.dark-theme .history-time {
    color: #6a6a6a;
}

.video-review-container.dark-theme .history-stats {
    color: #a0a0a0;
}

.video-review-container.dark-theme .history-reason {
    color: #a0a0a0;
}
</style>
