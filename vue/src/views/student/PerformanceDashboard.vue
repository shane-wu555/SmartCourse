<template>
  <div class="performance-dashboard">
    <h2>学习表现</h2>
    
    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stat-card">
        <h3>平均得分率</h3>
        <div class="stat-value">{{ averageScoreRate }}%</div>
      </div>
      <div class="stat-card">
        <h3>完成任务数</h3>
        <div class="stat-value">{{ completedTasks }}</div>
      </div>
      <div class="stat-card">
        <h3>总任务数</h3>
        <div class="stat-value">{{ totalTasks }}</div>
      </div>
      <div class="stat-card">
        <h3>完成率</h3>
        <div class="stat-value">{{ completionRate }}%</div>
      </div>
    </div>

    <!-- 成绩趋势图 -->
    <div class="chart-section">
      <h3>成绩趋势图（得分率）</h3>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="chartData.length === 0" class="no-data">暂无成绩数据</div>
      <div v-else class="chart-container">
        <canvas ref="scoreChart" width="800" height="400"></canvas>
      </div>
    </div>

    <!-- 任务完成情况 -->
    <div class="tasks-section">
      <h3>任务完成情况</h3>
      <div v-if="tasksList.length === 0" class="no-data">暂无任务数据</div>
      <div v-else class="tasks-list">
        <div 
          v-for="task in tasksList" 
          :key="task.taskId" 
          class="task-item"
          :class="{ 'completed': task.completed, 'pending': !task.completed }"
        >
          <div class="task-info">
            <h4>{{ task.taskName }}</h4>
            <p class="task-type">{{ task.taskType }}</p>
            <p class="task-date">截止时间: {{ formatDate(task.deadline) }}</p>
          </div>
          <div class="task-score" v-if="task.completed && task.score !== null">
            <div class="score-display">
              <span class="score">{{ task.score }}</span>
              <span class="total">/ {{ task.totalScore }}</span>
            </div>
            <div class="score-rate" :class="getScoreRateClass(task.scoreRate)">
              {{ task.scoreRate }}%
            </div>
          </div>
          <div class="task-status" v-else>
            <span class="status" :class="task.completed ? 'completed' : 'pending'">
              {{ task.completed ? '已完成' : '未完成' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PerformanceDashboard'
}
</script>