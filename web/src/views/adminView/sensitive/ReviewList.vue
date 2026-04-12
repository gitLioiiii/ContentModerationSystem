<template>
  <div class="review-report">
    <div class="Cardview">
      <ElCard shadow="hover">
        <div>Ai共处理违规条数</div>
        <div>{{ totalViolations }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <div>今日违规未通过条数</div>
        <div>{{ todayViolations }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <div>待人工审核条数</div>
        <div>{{ pendingReview }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <div>待处理申诉量</div>
        <div>{{ appealCount }}</div>
      </ElCard>
      <ElCard shadow="hover">
        <div>敏感词条数</div>
        <div>{{ violationTypesCount }}</div>
      </ElCard>
    </div>

    <div class="chart-container">
      <div class="chart-view">
        <!-- 柱状图 -->
        <div id="daily-chart"></div>
      </div>
      <div class="chart-view">
        <!-- 饼图 -->
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
    const response = await request.get('/reviewlist/all')

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


// 更新一周违规趋势图（按日期统计）
const updateDailyChart = (trendData) => {
  const dates = trendData.dates
  const counts = trendData.counts

  dailyChart.setOption({
    title: {
      text: `一周违规趋势`,
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
      data: dates,
      axisLabel: {
        fontSize: 11,
        rotate: 0
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
        barMaxWidth: 40,
        itemStyle: {
          color: '#C3E7FE',
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: '#89E9E0'
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
      text: `敏感词分类分布（已启用: ${totalWords}个）`,
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
        color: ['#C3E7FE', '#A8E6CF', '#FFD3B6', '#FFAAA5', '#B5A8E6', '#FFF4A3']
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

.Cardview :deep(.el-card:nth-child(1)) { background: #F3F8FF; border-color: transparent; }
.Cardview :deep(.el-card:nth-child(2)) { background: #F5FFF7; border-color: transparent; }
.Cardview :deep(.el-card:nth-child(3)) { background: #FFF7F5; border-color: transparent; }
.Cardview :deep(.el-card:nth-child(4)) { background: #F9F5FF; border-color: transparent; }
.Cardview :deep(.el-card:nth-child(5)) { background: #FFFDF5; border-color: transparent; }

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
