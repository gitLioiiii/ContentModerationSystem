<template>
    <div class="text-review-container" :class="{ 'dark-theme': isDark }">
        <!-- 页面标题 -->
        <div class="page-header">
            <h1 class="page-title">文本内容审核</h1>
            <p class="page-desc">使用AI进行文本内容审核，快速识别违规内容</p>
        </div>

        <!-- 上方：左右分栏区域 -->
        <div class="top-section">
            <!-- 左侧：文本输入区 -->
            <ElCard class="input-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <i class="bi bi-file-text" style="font-size: 20px;"></i>
                        <span>输入文本内容</span>
                    </div>
                </template>
                <div class="input-section">
                    <ElInput
                        v-model="textContent"
                        type="textarea"
                        placeholder="请输入需要审核的文本内容..."
                        maxlength="5000"
                        show-word-limit
                        :disabled="loading"
                        class="textarea-full"
                    />
                    <div class="action-buttons">
                        <ElButton
                            type="primary"
                            size="large"
                            :loading="loading"
                            :disabled="!textContent.trim()"
                            @click="handleModerate"
                        >
                            <i class="bi bi-check-lg"></i>
                            {{ loading ? 'AI审核中...' : '开始AI审核' }}
                        </ElButton>
                        <ElButton
                            size="large"
                            :disabled="loading"
                            @click="handleClear"
                        >
                            <i class="bi bi-trash"></i>
                            清空内容
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

                    <!-- 风险等级 -->
                    <div class="result-item">
                        <div class="result-label">风险等级</div>
                        <div class="result-value">
                            <ElTag
                                :type="getRiskType(result.riskLevel)"
                                size="large"
                            >
                                {{ result.riskLevel }}
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

                    <!-- 原始内容 -->
                    <div class="result-item">
                        <div class="result-label">原始内容</div>
                        <div class="result-value original-text">
                            {{ result.originalText }}
                        </div>
                    </div>
                </div>
            </ElCard>

            <!-- 空状态提示 -->
            <ElCard v-else class="empty-card" shadow="hover">
                <div class="empty-state">
                    <i class="bi bi-file-text" style="font-size: 80px; color: #909399;"></i>
                    <p class="empty-text">请在左侧输入文本并点击"开始AI审核"</p>
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
                    <div class="history-header">
                        <ElTag
                            :type="getResultType(item.result)"
                            size="small"
                        >
                            {{ item.result }}
                        </ElTag>
                        <span class="history-time">{{ item.time }}</span>
                    </div>
                    <div class="history-text">{{ truncateText(item.originalText) }}</div>
                </div>
            </div>
        </ElCard>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElCard, ElInput, ElButton, ElMessage, ElTag } from 'element-plus'
import request from '@/utils/request'
import { useTheme } from '@/utils/useTheme'

const { isDark } = useTheme()

// 响应式数据
const textContent = ref('')
const loading = ref(false)
const result = ref(null)
const historyList = ref([])

// AI审核
const handleModerate = async () => {
    if (!textContent.value.trim()) {
        ElMessage.warning('请输入需要审核的文本内容')
        return
    }

    loading.value = true
    result.value = null

    try {
        const response = await request.post('/moderation/text', {
            content: textContent.value
        })

        if (response.data.status === true) {
            result.value = response.data.payload.moderation

            // 添加到历史记录
            historyList.value.unshift({
                ...response.data.payload.moderation,
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
        console.error('文本审核失败:', error)
        ElMessage.error('审核失败，请重试')
    } finally {
        loading.value = false
    }
}

// 清空内容
const handleClear = () => {
    textContent.value = ''
    result.value = null
}

// 清空历史记录
const clearHistory = () => {
    historyList.value = []
    ElMessage.success('历史记录已清空')
}

// 加载历史项目
const loadHistoryItem = (item) => {
    textContent.value = item.originalText
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

// 获取风险等级标签类型
const getRiskType = (level) => {
    const typeMap = {
        '低': 'success',
        '中': 'warning',
        '高': 'danger'
    }
    return typeMap[level] || 'info'
}
</script>

<style scoped>
.text-review-container {
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

/* 左侧输入卡片 */
.input-card {
    height: 25rem;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
}

.input-card :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

/* 右侧审核结果卡片 */
.result-card,
.empty-card {
    height: 25rem;
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

/* 输入区域 */
.input-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    height: 100%;
}

.textarea-full {
    flex: 1;
    display: flex;
    flex-direction: column;
}

:deep(.textarea-full .el-textarea__inner) {
    font-size: 15px;
    line-height: 1.6;
    height: 100% !important;
    resize: none;
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

.original-text {
    padding: 1rem;
    background-color: #f5f7fa;
    border-radius: 8px;
    line-height: 1.6;
    white-space: pre-wrap;
    max-height: 200px;
    overflow-y: auto;
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
    max-height: 300px;
    overflow-y: auto;
}

.history-item {
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

.history-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.5rem;
}

.history-time {
    font-size: 13px;
    color: #909399;
}

.history-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .top-section {
        grid-template-columns: 1fr;
    }

    .input-card,
    .result-card,
    .empty-card {
        height: auto;
        min-height: 30rem;
    }
}

@media (max-width: 768px) {
    .text-review-container {
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
        max-height: 250px;
    }
}

/* ======= 暗黑主题 ======= */
.text-review-container.dark-theme {
    background-color: #121212;
}

.text-review-container.dark-theme .page-title {
    color: #e5e5e5;
}

.text-review-container.dark-theme .page-desc {
    color: #6a6a6a;
}

.text-review-container.dark-theme .card-header {
    color: #e5e5e5;
}

.text-review-container.dark-theme .empty-text {
    color: #a0a0a0;
}

.text-review-container.dark-theme .empty-desc {
    color: #6a6a6a;
}

.text-review-container.dark-theme .result-item {
    border-bottom-color: #3a3a3a;
}

.text-review-container.dark-theme .result-label {
    color: #a0a0a0;
}

.text-review-container.dark-theme .result-value {
    color: #e5e5e5;
}

.text-review-container.dark-theme .original-text {
    background-color: #2a2a2a;
}

.text-review-container.dark-theme .history-item {
    background-color: #1e1e1e;
}

.text-review-container.dark-theme .history-item:hover {
    background-color: #2a2a2a;
}

.text-review-container.dark-theme .history-time {
    color: #6a6a6a;
}

.text-review-container.dark-theme .history-text {
    color: #a0a0a0;
}
</style>
