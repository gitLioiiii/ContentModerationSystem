<template>
  <div class="review-config-view" :class="{ 'dark-theme': isDark }">
    <div class="header-section">
      <h2 class="page-title">审核阈值配置</h2>
      <p class="page-desc">调整自动审核的判定阈值，影响图片和视频审核的通过/拒绝/人工审核判定标准</p>
    </div>

    <div v-loading="loading" class="config-content">
      <ElForm :model="configForm" label-width="180px" label-position="right" class="config-form">

        <!-- 封面审核模型选择 -->
        <div class="section-card">
          <h3 class="section-title"><i class="bi bi-cpu"></i> 封面审核模型</h3>
          <p class="section-desc">本地模型保护数据隐私（响应较慢），云端模型响应更快</p>

          <ElFormItem label="审核模型">
            <ElRadioGroup v-model="configForm.coverModerationMode">
              <ElRadio value="local">本地模型（隐私优先）</ElRadio>
              <ElRadio value="cloud">云端模型（速度优先）</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </div>

        <!-- 图片审核阈值 -->
        <div class="section-card">
          <h3 class="section-title"><i class="bi bi-image"></i> 图片审核阈值</h3>
          <p class="section-desc">AI 返回 matchScore（0-100），根据以下阈值判定审核结果</p>

          <ElFormItem label="通过阈值（低于此分数）">
            <ElSlider v-model="configForm.imagePassThreshold" :min="0" :max="100" show-input :step="5" />
            <span class="hint">matchScore &lt; {{ configForm.imagePassThreshold }} → 自动通过</span>
          </ElFormItem>

          <ElFormItem label="拒绝阈值（高于等于此分数）">
            <ElSlider v-model="configForm.imageRejectThreshold" :min="0" :max="100" show-input :step="5" />
            <span class="hint">matchScore &ge; {{ configForm.imageRejectThreshold }} → 自动拒绝</span>
          </ElFormItem>

          <div class="threshold-preview">
            <span class="preview-pass">通过: 0 ~ {{ configForm.imagePassThreshold - 1 }}</span>
            <span class="preview-manual">人工审核: {{ configForm.imagePassThreshold }} ~ {{ configForm.imageRejectThreshold - 1 }}</span>
            <span class="preview-reject">拒绝: {{ configForm.imageRejectThreshold }} ~ 100</span>
          </div>
        </div>

        <!-- 视频审核阈值 -->
        <div class="section-card">
          <h3 class="section-title"><i class="bi bi-camera-video"></i> 视频审核阈值</h3>
          <p class="section-desc">根据违规帧占比和最高风险分数判定视频审核结果</p>

          <h4 class="sub-title">拒绝条件（满足任一即拒绝）</h4>
          <ElFormItem label="违规率阈值（%）">
            <ElSlider v-model="configForm.videoRejectViolationRate" :min="0" :max="100" show-input :step="5" />
            <span class="hint">违规帧占比 &ge; {{ configForm.videoRejectViolationRate }}% → 拒绝</span>
          </ElFormItem>
          <ElFormItem label="最高风险分数阈值">
            <ElSlider v-model="configForm.videoRejectMaxScore" :min="0" :max="100" show-input :step="5" />
            <span class="hint">最高帧分数 &ge; {{ configForm.videoRejectMaxScore }} → 拒绝</span>
          </ElFormItem>

          <h4 class="sub-title">人工审核条件（满足任一即转人工）</h4>
          <ElFormItem label="违规率阈值（%）">
            <ElSlider v-model="configForm.videoManualViolationRate" :min="0" :max="100" show-input :step="5" />
            <span class="hint">违规帧占比 &ge; {{ configForm.videoManualViolationRate }}% → 人工审核</span>
          </ElFormItem>
          <ElFormItem label="最高风险分数阈值">
            <ElSlider v-model="configForm.videoManualMaxScore" :min="0" :max="100" show-input :step="5" />
            <span class="hint">最高帧分数 &ge; {{ configForm.videoManualMaxScore }} → 人工审核</span>
          </ElFormItem>
        </div>

        <!-- 视频抽帧配置 -->
        <div class="section-card">
          <h3 class="section-title"><i class="bi bi-film"></i> 视频抽帧配置</h3>
          <ElFormItem label="抽帧间隔（秒）">
            <ElSlider v-model="configForm.videoFrameInterval" :min="1" :max="30" show-input :step="1" />
            <span class="hint">每隔 {{ configForm.videoFrameInterval }} 秒提取一帧进行审核</span>
          </ElFormItem>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <ElButton type="primary" @click="saveConfig" :loading="saving">保存配置</ElButton>
          <ElButton @click="resetConfig">恢复默认</ElButton>
        </div>
      </ElForm>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElForm, ElFormItem, ElSlider, ElButton, ElMessage, ElRadioGroup, ElRadio } from 'element-plus'
import request from '@/utils/request'
import { useTheme } from '@/utils/useTheme'
import 'bootstrap-icons/font/bootstrap-icons.css'

const { isDark } = useTheme()
const loading = ref(false)
const saving = ref(false)

const configForm = reactive({
  imagePassThreshold: 30,
  imageRejectThreshold: 70,
  videoRejectViolationRate: 30,
  videoRejectMaxScore: 70,
  videoManualViolationRate: 10,
  videoManualMaxScore: 30,
  videoFrameInterval: 3,
  coverModerationMode: 'local',
})

const defaultConfig = {
  imagePassThreshold: 30,
  imageRejectThreshold: 70,
  videoRejectViolationRate: 30,
  videoRejectMaxScore: 70,
  videoManualViolationRate: 10,
  videoManualMaxScore: 30,
  videoFrameInterval: 3,
  coverModerationMode: 'local',
}

const loadConfig = () => {
  loading.value = true
  request.get('/review-config').then((response) => {
    if (response.data.status === true) {
      const config = response.data.payload.config
      Object.assign(configForm, config)
    }
  }).catch(() => {
    ElMessage.warning('加载配置失败，使用默认值')
  }).finally(() => {
    loading.value = false
  })
}

const saveConfig = () => {
  if (configForm.imagePassThreshold >= configForm.imageRejectThreshold) {
    ElMessage.warning('图片通过阈值必须小于拒绝阈值')
    return
  }
  if (configForm.videoManualViolationRate >= configForm.videoRejectViolationRate) {
    ElMessage.warning('视频人工审核违规率必须小于拒绝违规率')
    return
  }
  if (configForm.videoManualMaxScore >= configForm.videoRejectMaxScore) {
    ElMessage.warning('视频人工审核分数阈值必须小于拒绝分数阈值')
    return
  }

  saving.value = true
  request.post('/review-config/update', configForm).then((response) => {
    if (response.data.status === true) {
      ElMessage.success('配置保存成功')
    }
  }).catch(() => {
    ElMessage.error('保存配置失败')
  }).finally(() => {
    saving.value = false
  })
}

const resetConfig = () => {
  Object.assign(configForm, defaultConfig)
  ElMessage.info('已恢复默认值，点击保存生效')
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped lang="scss">
.review-config-view {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

.header-section {
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px;
  }

  .page-desc {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.section-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 20px;
}

.sub-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin: 20px 0 12px;
  padding-left: 4px;
  border-left: 3px solid #409eff;
  padding-left: 8px;
}

.hint {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.threshold-preview {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;

  span {
    padding: 4px 12px;
    border-radius: 4px;
  }

  .preview-pass {
    background: #f0f9eb;
    color: #67c23a;
  }

  .preview-manual {
    background: #fdf6ec;
    color: #e6a23c;
  }

  .preview-reject {
    background: #fef0f0;
    color: #f56c6c;
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 8px;
}

// 暗黑主题
.review-config-view.dark-theme {
  .page-title { color: #e5e5e5; }
  .page-desc { color: #6a6a6a; }

  .section-card {
    background: #1e1e1e;
    border-color: #3a3a3a;
  }

  .section-title { color: #e5e5e5; }
  .sub-title { color: #a0a0a0; }
  .hint { color: #6a6a6a; }

  .threshold-preview {
    background: #2a2a2a;

    .preview-pass { background: #1a2e1a; color: #67c23a; }
    .preview-manual { background: #2e2a1a; color: #e6a23c; }
    .preview-reject { background: #2e1a1a; color: #f56c6c; }
  }
}
</style>
