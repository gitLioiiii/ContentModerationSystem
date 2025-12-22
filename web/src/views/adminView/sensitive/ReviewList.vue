<template>
  <div class="review-report">
    <!-- 统计卡片区域 -->
    <div class="Cardview">
      <ElCard shadow="hover">
        <i class="bi bi-exclamation-triangle card-icon"></i>
        <!-- 所有 -->
        <div>Ai共处理违规条数</div>
        <div>{{ totalViolations }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <i class="bi bi-calendar-day card-icon"></i>
        <!-- 今日 -->
        <div>今日违规未通过条数</div>
        <div>{{ todayViolations }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <i class="bi bi-hourglass-split card-icon"></i>
        <!-- 需人工审核 -->
        <div>待人工审核条数</div>
        <div>{{ pendingReview }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <i class="bi bi-envelope-exclamation card-icon"></i>
        <!-- 需要处理申诉 -->
        <div>待处理申诉量</div>
        <div>{{ appealCount }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <i class="bi bi-tags card-icon"></i>
        <div>敏感词条数</div>
        <div>{{ violationTypesCount }}</div>
      </ElCard>
    </div>

    <!-- ECHARTS图表区域 -->
    <div class="chart-container">
      <div class="chart-view">
        <!-- 今日违规条数柱状图 -->
        <div id="daily-chart"></div>
      </div>
      <div class="chart-view">
        <!-- 敏感词修改饼图 -->
        <div id="type-chart"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { ElCard } from 'element-plus'
import * as echarts from 'echarts'
import request from "@/utils/request"
import 'bootstrap-icons/font/bootstrap-icons.css'

let dailyChart = null
let typeChart = null

// 统计数据
const totalViolations = ref(0)
const todayViolations = ref(0)
const pendingReview = ref(0)
const appealCount = ref(0)
const violationTypesCount = ref(0)

onMounted(() => {
  // 初始化图表
  dailyChart = echarts.init(document.getElementById('daily-chart'))
  typeChart = echarts.init(document.getElementById('type-chart'))

  fetchAllStats()

  window.addEventListener('resize', handleResize)
})

const fetchAllStats = async () => {
  try {
    // 调用综合统计接口
    const response = await request.get('/review-stats/all')

    if (response.data.status === true) {
      const data = response.data.payload

      // 更新概览数据
      if (data.overview) {
        totalViolations.value = data.overview.totalViolations || 0
        todayViolations.value = data.overview.todayViolations || 0
        pendingReview.value = data.overview.pendingReview || 0
        appealCount.value = data.overview.appealCount || 0
        violationTypesCount.value = data.overview.violationTypesCount || 0
      }

      // 更新今日违规趋势图
      if (data.dailyTrend) {
        updateDailyChart(data.dailyTrend)
      }

      // 更新敏感词分类分布图
      if (data.sensitiveWordsDistribution) {
        updateTypeChart(data.sensitiveWordsDistribution)
      }
    } else {
      console.error('获取统计数据失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}


// 更新今日违规趋势图（按小时统计）
const updateDailyChart = (trendData) => {
  const hours = trendData.hours
  const counts = trendData.counts
  const todayDate = new Date().toLocaleDateString('zh-CN')

  dailyChart.setOption({
    title: {
      text: `今日违规趋势（${todayDate}）`,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        return `${params[0].name}<br/>违规数: ${params[0].value}次`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '8%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: hours,
      axisLabel: {
        fontSize: 11,
        rotate: 45,
        interval: 1
      },
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '违规数',
      nameGap: 25,
      axisLine: {
        show: true
      },
      axisTick: {
        show: true
      },
      axisLabel: {
        formatter: '{value}'
      },
      splitNumber: 5,
      minInterval: 1
    },
    series: [
      {
        name: '违规数',
        type: 'bar',
        data: counts,
        barMaxWidth: 30,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF6B6B' },
            { offset: 1, color: '#FFB3B3' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#FF4040' },
              { offset: 1, color: '#FF8888' }
            ])
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: function(params) {
            return params.value > 0 ? params.value : ''
          }
        }
      }
    ]
  })
}

// 更新敏感词分类分布图
const updateTypeChart = (distributionData) => {
  const data = distributionData.distribution
  const totalWords = distributionData.totalWords

  typeChart.setOption({
    title: {
      text: `敏感词分类分布（总计: ${totalWords}个）`,
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        return `${params.name}<br/>数量: ${params.value}个 (${params.percent}%)`
      }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '敏感词分类',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}个\n({d}%)',
          fontSize: 12
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: data,
        color: ['#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8', '#F7DC6F']
      }
    ]
  })
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (dailyChart) {
    dailyChart.dispose()
  }
  if (typeChart) {
    typeChart.dispose()
  }
})

function handleResize() {
  if (dailyChart) {
    dailyChart.resize()
  }
  if (typeChart) {
    typeChart.resize()
  }
}
</script>

<style scoped>
.review-report {
  padding: 20px;
}

.Cardview {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  gap: 15px;
}

.Cardview :deep(.el-card) {
  flex: 1;
  min-width: 120px;
  padding: 0.125rem;
  border-radius: 0.625rem;
  transition: all 0.3s ease;
}

.Cardview :deep(.el-card:hover) {
  transform: translateY(-5px);
}

.Cardview :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
}

.Cardview :deep(.el-card__body > div:first-of-type) {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.Cardview :deep(.el-card__body > div:last-of-type) {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.card-icon {
  font-size: 40px;
}

.Cardview :deep(.el-card:nth-child(1)) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

.Cardview :deep(.el-card:nth-child(1) .el-card__body > div),
.Cardview :deep(.el-card:nth-child(1) .card-icon) {
  color: white !important;
}

.Cardview :deep(.el-card:nth-child(2)) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-color: transparent;
  color: white;
}

.Cardview :deep(.el-card:nth-child(2) .el-card__body > div),
.Cardview :deep(.el-card:nth-child(2) .card-icon) {
  color: white !important;
}

.Cardview :deep(.el-card:nth-child(3)) {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  border-color: transparent;
  color: white;
}

.Cardview :deep(.el-card:nth-child(3) .el-card__body > div),
.Cardview :deep(.el-card:nth-child(3) .card-icon) {
  color: white !important;
}

.Cardview :deep(.el-card:nth-child(4)) {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  border-color: transparent;
  color: white;
}

.Cardview :deep(.el-card:nth-child(4) .el-card__body > div),
.Cardview :deep(.el-card:nth-child(4) .card-icon) {
  color: white !important;
}

.Cardview :deep(.el-card:nth-child(5)) {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
  border-color: transparent;
  color: white;
}

.Cardview :deep(.el-card:nth-child(5) .el-card__body > div),
.Cardview :deep(.el-card:nth-child(5) .card-icon) {
  color: white !important;
}

.chart-container {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.chart-view {
  flex: 1;
  min-width: 400px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

#daily-chart,
#type-chart {
  width: 100%;
  height: 400px;
  min-height: 400px;
}

@media (max-width: 768px) {
  .Cardview {
    flex-wrap: wrap;
  }

  .Cardview :deep(.el-card) {
    flex: 1 1 calc(33.33% - 10px);
    min-width: 150px;
  }

  .chart-container {
    flex-direction: column;
  }

  .chart-view {
    min-width: 100%;
  }
}
</style>
