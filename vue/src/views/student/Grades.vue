<template>
  <div class="student-grades">
    <div class="decor-lower-left"></div>
<div class="decor-lower-right"></div>


    <h2>任务成绩</h2>

    <div v-for="course in courseList" :key="course.courseId" class="course-block">
      <h3>{{ course.name }}（{{ course.courseCode }}）</h3>
      <p>学期：{{ course.semester }}</p>

      <h4>任务成绩</h4>
      <ul v-if="course.tasks && course.tasks.length">
        <li v-for="task in course.tasks" :key="task.taskId">
          {{ task.title }}：{{ task.statusText }}
        </li>
      </ul>
      <p v-else>暂无任务</p>

      <!-- 成绩趋势图 -->
      <h4>成绩趋势</h4>
      <div v-if="course.gradeTrend && course.gradeTrend.scores && course.gradeTrend.scores.length"
        :id="'chart-' + course.courseId" class="grade-trend-chart"></div>
      <p v-else>未做过任务，暂无成绩</p>

      <h4>课程反馈</h4>
      <p style="white-space: pre-wrap;">{{ course.feedback || '暂无反馈' }}</p>

      <!-- AI学习推荐 -->
      <h4>
        AI学习推荐
        <button @click="loadRecommendations(course.courseId)" 
                :disabled="course.loadingRecommendations"
                class="refresh-btn">
          {{ course.loadingRecommendations ? '加载中...' : '刷新推荐' }}
        </button>
      </h4>
      
      <div v-if="course.loadingRecommendations" class="loading-recommendations">
        <div class="loading-spinner"></div>
        <p>AI正在分析您的学习情况，生成个性化推荐...</p>
        <p class="loading-tip">※ AI分析可能需要10-30秒，请耐心等待</p>
      </div>

      <div v-else-if="course.recommendations" class="recommendations-container">
        <!-- 推荐选项卡 -->
        <div class="recommendation-tabs">
          <button 
            v-for="tab in recommendationTabs" 
            :key="tab.key"
            :class="['tab-btn', { active: course.activeTab === tab.key }]"
            @click="setActiveTab(course.courseId, tab.key)">
            {{ tab.label }}
          </button>
        </div>

        <!-- 知识点推荐 -->
        <div v-if="course.activeTab === 'knowledge'" class="recommendation-content">
          <div v-if="course.recommendations.knowledgePointRecommendations && course.recommendations.knowledgePointRecommendations.length" class="knowledge-recommendations">
            <div class="recommendation-mode-notice">
              <div v-if="isGradeBased(course.recommendations)" class="grade-based-notice">
                <i class="info-icon">ℹ️</i>
                <span>基于您的优秀成绩（得分率{{ calculateScoreRate(course.recommendations.currentGrade, course.recommendations.maxScore) }}，班级排名第{{ course.recommendations.classRank }}名）为您推荐适合的学习内容</span>
              </div>
              <div v-else class="knowledge-based-notice">
                <i class="info-icon">📊</i>
                <span>基于您的知识点掌握情况分析推荐学习内容</span>
              </div>
            </div>

            <div v-for="kp in course.recommendations.knowledgePointRecommendations" :key="kp.pointId" class="knowledge-item">
              <div class="knowledge-header">
                <h5>{{ kp.name }}</h5>
                <span class="priority-badge" :class="getPriorityClass(kp.priority)">
                  {{ getPriorityText(kp.priority) }}
                </span>
              </div>
              <p class="knowledge-description">{{ kp.description }}</p>
              <div class="knowledge-stats">
                <span v-if="isGradeBased(course.recommendations)" class="learning-readiness">
                  学习适配度: {{ Math.round(kp.masteryLevel) }}%
                </span>
                <span v-else class="mastery-level">
                  掌握程度: {{ Math.round(kp.masteryLevel) }}%
                </span>
                <span class="difficulty">难度: {{ getDifficultyText(kp.difficultyLevel) }}</span>
              </div>
              <p class="recommendation-reason">{{ kp.reason }}</p>
            </div>
          </div>
          <p v-else class="no-data">暂无知识点推荐</p>
        </div>

        <!-- 资源推荐 -->
        <div v-if="course.activeTab === 'resources'" class="recommendation-content">
          <div v-if="course.recommendations.resourceRecommendations && course.recommendations.resourceRecommendations.length" class="resource-recommendations">
            <div class="recommendation-mode-notice">
              <div v-if="isGradeBased(course.recommendations)" class="grade-based-notice">
                <i class="info-icon">ℹ️</i>
                <span>基于您的学习水平为您推荐合适的学习资源</span>
              </div>
              <div v-else class="knowledge-based-notice">
                <i class="info-icon">📊</i>
                <span>基于薄弱知识点为您推荐相关学习资源</span>
              </div>
            </div>

            <div v-for="resource in course.recommendations.resourceRecommendations.slice(0, 3)" :key="resource.resourceId" class="resource-item">
              <div class="resource-header">
                <h5>{{ resource.name }}</h5>
                <span class="resource-type" :class="getResourceTypeClass(resource.type)">
                  {{ getResourceTypeText(resource.type) }}
                </span>
              </div>
              <p class="resource-description">{{ resource.description }}</p>
              <div class="resource-stats">
                <span class="priority">优先级: {{ resource.priority }}</span>
                <span class="view-count">浏览量: {{ resource.viewCount || 0 }}</span>
                <span class="file-size" v-if="resource.size">大小: {{ formatFileSize(resource.size) }}</span>
              </div>
              <p class="recommendation-reason">{{ resource.reason }}</p>
              <div class="resource-actions">
                <button @click="viewResource(resource.url)" class="view-btn">查看资源</button>
              </div>
            </div>
          </div>
          <p v-else class="no-data">暂无资源推荐</p>
        </div>

        <!-- 综合建议 -->
        <div v-if="course.activeTab === 'comprehensive'" class="recommendation-content">
          <div class="comprehensive-recommendation">
            <div class="learning-status">
              <h5>学习状态分析</h5>
              <div class="status-card">
                <div class="status-item">
                  <span class="label">当前得分率:</span>
                  <span class="value">{{ calculateScoreRate(course.recommendations.currentGrade, course.recommendations.maxScore) }}</span>
                </div>
                <div class="status-item">
                  <span class="label">班级排名:</span>
                  <span class="value">{{ course.recommendations.classRank || '暂无' }}</span>
                </div>
                <div class="status-item" v-if="getSmartLearningStatus(course.recommendations)">
                  <span class="label">学习状态:</span>
                  <span class="value">{{ getSmartLearningStatus(course.recommendations) }}</span>
                </div>
                <div class="status-item" v-if="getSmartExpectedImprovement(course.recommendations) !== null">
                  <span class="label">预期提升:</span>
                  <span class="value">{{ getSmartExpectedImprovement(course.recommendations) }}%</span>
                </div>
              </div>
            </div>

            <div class="learning-path" v-if="formatLearningPath(course.recommendations.learningPath)">
              <h5>推荐学习路径</h5>
              <div class="path-content">
                <p style="white-space: pre-wrap;">{{ formatLearningPath(course.recommendations.learningPath) }}</p>
              </div>
            </div>

            <div class="ai-suggestion">
              <h5>AI综合建议</h5>
              <div class="suggestion-content">
                <p style="white-space: pre-wrap;">{{ formatAISuggestion(course.recommendations.overallSuggestion) }}</p>
              </div>
            </div>

            <div class="recommendation-mode-explanation">
              <h5>推荐说明</h5>
              <div class="explanation-content">
                <div v-if="isGradeBased(course.recommendations)" class="grade-based-explanation">
                  <p><strong>推荐依据：</strong>基于您的课程得分率（{{ calculateScoreRate(course.recommendations.currentGrade, course.recommendations.maxScore) }}）和班级排名（第{{ course.recommendations.classRank }}名）</p>
                  <p><strong>推荐策略：</strong>根据您的优秀表现，为您推荐进阶学习内容和深度学习资源</p>
                  <p><strong>注意事项：</strong>当前课程任务未绑定具体知识点，推荐内容基于成绩水平分析</p>
                </div>
                <div v-else class="knowledge-based-explanation">
                  <p><strong>推荐依据：</strong>基于您在各知识点的具体掌握情况</p>
                  <p><strong>推荐策略：</strong>针对薄弱知识点提供定向学习建议</p>
                  <p><strong>注意事项：</strong>推荐内容基于任务成绩与知识点的关联分析</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="recommendation-footer">
          <p class="generation-time">
            推荐生成时间: {{ course.recommendations.generatedTime }}
          </p>
        </div>
      </div>

      <div v-else-if="course.recommendationError" class="recommendation-error">
        <p>{{ course.recommendationError }}</p>
        <button @click="loadRecommendations(course.courseId)" class="retry-btn">重试</button>
      </div>

      <div v-else class="no-recommendations">
        <p>暂无推荐内容，点击上方按钮获取AI推荐</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '@/axios'
import * as echarts from 'echarts'; // 引入echarts

export default {
  name: 'StudentGradesView',
  data() {
    return {
      courseList: [],
      chartInstances: {}, // 存储图表实例
      recommendationTabs: [
        { key: 'knowledge', label: '知识点推荐' },
        { key: 'resources', label: '资源推荐' },
        { key: 'comprehensive', label: '综合建议' }
      ]
    }
  },
  methods: {
    // 判断是否为基于成绩的推荐
    isGradeBased(recommendations) {
      // 检查知识点推荐的reason字段，如果包含"您的成绩优秀"等字样，则认为是基于成绩的推荐
      if (recommendations.knowledgePointRecommendations && recommendations.knowledgePointRecommendations.length > 0) {
        const firstReason = recommendations.knowledgePointRecommendations[0].reason || '';
        return firstReason.includes('您的成绩优秀') || 
               firstReason.includes('您的成绩良好') || 
               firstReason.includes('建议重点') ||
               firstReason.includes('建议学习');
      }
      return false;
    },

    // 设置活跃的推荐选项卡
    setActiveTab(courseId, tabKey) {
      const course = this.courseList.find(c => c.courseId === courseId);
      if (course) {
        course.activeTab = tabKey;
      }
    },

    // 加载推荐内容
    async loadRecommendations(courseId) {
      const course = this.courseList.find(c => c.courseId === courseId);
      if (!course) return;

      course.loadingRecommendations = true;
      course.recommendationError = null;

      try {
        console.log('开始获取推荐，课程ID:', courseId);
        
        // 获取综合推荐，增加超时处理
        const responseData = await axios.get('/recommendation/comprehensive', {
          params: { courseId },
          timeout: 60000 // 60秒超时
        });

        console.log('推荐响应数据:', responseData);

        // 检查响应数据是否存在
        if (!responseData) {
          course.recommendationError = '服务器返回空数据';
          return;
        }

        // 由于axios拦截器返回的是response.data，所以responseData就是后端返回的数据
        if (responseData.code === 200 || (!responseData.code && typeof responseData === 'object')) {
          // 根据后端日志，数据可能直接包含推荐内容
          const recommendationData = responseData.data || responseData;
          
          console.log('处理推荐数据:', recommendationData);
          
          // 从后端日志看，数据结构包含：
          // knowledgePointRecommendations, resourceRecommendations, comprehensiveRecommendation 等
          course.recommendations = {
            knowledgePointRecommendations: recommendationData.knowledgePointRecommendations || [],
            resourceRecommendations: recommendationData.resourceRecommendations || [],
            currentGrade: recommendationData.currentGrade || '9.0',
            maxScore: recommendationData.maxScore || '10.0',
            classRank: recommendationData.classRank || '1',
            learningStatus: recommendationData.learningStatus || '优秀',
            expectedImprovement: recommendationData.expectedImprovement,
            learningPath: recommendationData.learningPath,
            overallSuggestion: recommendationData.overallSuggestion || recommendationData.comprehensiveRecommendation || '暂无AI建议',
            generatedTime: recommendationData.generatedTime || new Date().toLocaleString()
          };
          course.activeTab = course.activeTab || 'comprehensive'; // 默认显示综合建议
          console.log('成功设置推荐数据:', course.recommendations);
        } else {
          console.error('推荐响应错误:', responseData);
          course.recommendationError = responseData?.message || '获取推荐失败，服务器返回异常';
        }
      } catch (error) {
        console.error('获取推荐失败:', error);
        console.error('错误类型:', error.name);
        console.error('错误代码:', error.code);
        
        // 详细的错误处理
        if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
          course.recommendationError = 'AI分析超时，请稍后重试（推荐生成可能需要较长时间）';
        } else if (error.response) {
          // 服务器响应了错误状态码
          const status = error.response.status;
          const message = error.response.data?.message || '服务器错误';
          course.recommendationError = `服务器错误 (${status}): ${message}`;
        } else if (error.request) {
          // 请求发出但没有收到响应
          course.recommendationError = '网络连接失败，请检查网络设置或稍后重试';
        } else {
          // 其他错误
          course.recommendationError = error.friendlyMessage || error.message || '未知错误，请稍后重试';
        }
      } finally {
        course.loadingRecommendations = false;
      }
    },

    // 获取优先级样式类
    getPriorityClass(priority) {
      if (typeof priority === 'number') {
        if (priority >= 8) return 'priority-high';
        if (priority >= 6) return 'priority-medium';
        return 'priority-low';
      }
      // 字符串类型的优先级
      switch(priority) {
        case 'HIGH': return 'priority-high';
        case 'MEDIUM': return 'priority-medium';
        case 'LOW': return 'priority-low';
        default: return 'priority-medium';
      }
    },

    // 获取优先级文本
    getPriorityText(priority) {
      if (typeof priority === 'number') {
        if (priority >= 8) return '高优先级';
        if (priority >= 6) return '中优先级';
        return '低优先级';
      }
      // 字符串类型的优先级
      switch(priority) {
        case 'HIGH': return '高优先级';
        case 'MEDIUM': return '中优先级';
        case 'LOW': return '低优先级';
        default: return '中优先级';
      }
    },

    // 获取难度文本
    getDifficultyText(difficulty) {
      switch(difficulty) {
        case 'EASY': return '简单';
        case 'MEDIUM': return '中等';
        case 'HARD': return '困难';
        default: return '中等';
      }
    },

    // 获取资源类型样式类
    getResourceTypeClass(type) {
      switch(type) {
        case 'VIDEO': return 'type-video';
        case 'PDF': return 'type-pdf';
        case 'DOCUMENT': return 'type-document';
        case 'PPT': return 'type-ppt';
        default: return 'type-default';
      }
    },

    // 获取资源类型文本
    getResourceTypeText(type) {
      switch(type) {
        case 'VIDEO': return '视频';
        case 'PDF': return 'PDF';
        case 'DOCUMENT': return '文档';
        case 'PPT': return 'PPT';
        default: return '其他';
      }
    },

    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    // 查看资源
    viewResource(url) {
      if (url) {
        window.open(url, '_blank');
      }
    },

    // 渲染趋势图
    renderGradeTrendChart(courseId, trendData) {
      const chartDom = document.getElementById(`chart-${courseId}`);
      if (!chartDom) return;

      // 销毁旧实例（如果存在）
      if (this.chartInstances[courseId]) {
        this.chartInstances[courseId].dispose();
      }

      const chart = echarts.init(chartDom);
      this.chartInstances[courseId] = chart;

      // 计算得分率而不是使用绝对分数
      const scoreRates = trendData.scores.map((score, index) => {
        const totalScore = trendData.totalScores ? trendData.totalScores[index] : 100;
        return totalScore > 0 ? Math.round((score / totalScore) * 100 * 100) / 100 : 0;
      });

      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function (params) {
            const data = params[0];
            const index = data.dataIndex;
            const actualScore = trendData.scores[index];
            const totalScore = trendData.totalScores ? trendData.totalScores[index] : 100;
            const taskName = trendData.taskNames ? trendData.taskNames[index] : data.name;
            return `任务: ${taskName}<br/>得分率：${data.value}%<br/>实际得分：${actualScore}/${totalScore}分`;
          }
        },
        xAxis: {
          type: 'category',
          data: trendData.taskNames || trendData.dates, // 优先使用任务名称，如果没有则使用日期
          axisLabel: {
            rotate: 45,
            interval: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '得分率 (%)',
          min: 0,
          max: 100,
          interval: 10
        },
        series: [{
          name: '得分率',
          type: 'line',
          data: scoreRates,
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            width: 3,
            color: '#1890ff'
          },
          itemStyle: {
            color: '#1890ff'
          },
          markPoint: {
            data: [
              { type: 'max', name: '最高得分率' },
              { type: 'min', name: '最低得分率' }
            ]
          },
          markLine: {
            data: [
              { type: 'average', name: '平均得分率' }
            ]
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(24, 144, 255, 0.4)' },
              { offset: 1, color: 'rgba(24, 144, 255, 0.1)' }
            ])
          }
        }],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '12%',
          containLabel: true
        }
      };

      chart.setOption(option);

      // 响应窗口变化
      window.addEventListener('resize', () => {
        chart.resize();
      });
    },

    // 美化AI建议文本
    formatAISuggestion(suggestion) {
      if (!suggestion || suggestion === '暂无AI建议') {
        return '暂无AI建议';
      }
      
      // 首先提取有效内容，去除<think>标签等
      let content = suggestion;
      
      // 移除<think>...</think>标签及其内容（Ollama DeepSeek-R1 模型的思考过程）
      content = content.replace(/<think>[\s\S]*?<\/think>/gi, '');
      
      // 移除其他技术标记和调试信息
      content = content.replace(/^Ollama原始响应:.*$/gm, '');
      content = content.replace(/^提取的AI内容:.*$/gm, '');
      content = content.replace(/^清理后的内容:.*$/gm, '');
      content = content.replace(/^Response:.*$/gm, '');
      content = content.replace(/^AI建议:.*$/gm, '');
      
      // 移除markdown格式但保留结构
      let formatted = content
        .replace(/\*\*(.*?)\*\*/g, '$1') // 移除markdown加粗符号但保留内容
        .replace(/\*(.*?)\*/g, '$1') // 移除markdown斜体符号但保留内容
        .replace(/\n{3,}/g, '\n\n') // 合并多个换行
        .replace(/^\s+|\s+$/g, '') // 移除首尾空白
        .replace(/^#{1,6}\s*/gm, '') // 移除markdown标题符号
        .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1') // 移除markdown链接但保留文本
        .replace(/```[\s\S]*?```/g, '') // 移除代码块
        .replace(/`([^`]+)`/g, '$1') // 移除行内代码标记但保留内容
        .replace(/---+/g, '') // 移除分隔线
        .replace(/^\s*[-*+]\s*/gm, '• ') // 统一列表符号
        .replace(/^\s*\d+\.\s*/gm, '• ') // 将数字列表转为点列表
        .replace(/【.*?】/g, '') // 移除中文方括号内容
        .replace(/\[.*?\]/g, '') // 移除方括号内容
        .replace(/\s+/g, ' ') // 合并多个空格
        .trim();
      
      // 只有当内容确实无效时才使用默认建议
      if (formatted.length < 5 || 
          formatted.toLowerCase().includes('error') || 
          formatted.toLowerCase().includes('failed') ||
          formatted.toLowerCase().includes('无法') ||
          formatted === '[]' ||
          formatted === '{}') {
        return this.getDefaultSuggestion();
      }
      
      return formatted;
    },

    // 智能判断学习状态
    getSmartLearningStatus(recommendations) {
      if (!recommendations) return '良好';
      
      const scoreRate = this.calculateScoreRateValue(recommendations.currentGrade, recommendations.maxScore);
      const rank = parseInt(recommendations.classRank) || 999;
      
      if (scoreRate >= 95 && rank <= 3) {
        return '优秀';
      } else if (scoreRate >= 85 && rank <= 10) {
        return '良好';
      } else if (scoreRate >= 70) {
        return '一般';
      } else {
        return '需提升';
      }
    },

    // 智能计算预期提升（对于高分学生应该很小或为0）
    getSmartExpectedImprovement(recommendations) {
      if (!recommendations) return null;
      
      const scoreRate = this.calculateScoreRateValue(recommendations.currentGrade, recommendations.maxScore);
      const rank = parseInt(recommendations.classRank) || 999;
      
      // 如果已经是第一名且得分率很高，不需要提升
      if (rank === 1 && scoreRate >= 95) {
        return 0;
      } else if (scoreRate >= 90 && rank <= 5) {
        return Math.max(0, Math.round((100 - scoreRate) * 0.3)); // 很小的提升
      } else if (scoreRate >= 80) {
        return Math.round((95 - scoreRate) * 0.6);
      } else {
        return Math.round((85 - scoreRate) * 0.8);
      }
    },

    // 计算得分率数值（用于判断逻辑）
    calculateScoreRateValue(grade, maxScore) {
      if (!grade || !maxScore) return 0;
      
      const numGrade = parseFloat(grade);
      const numMaxScore = parseFloat(maxScore);
      
      if (isNaN(numGrade) || isNaN(numMaxScore) || numMaxScore === 0) {
        return 0;
      }
      
      return (numGrade / numMaxScore) * 100;
    },

    // 获取默认建议
    getDefaultSuggestion() {
      return '继续保持良好的学习状态，适当拓展学习深度和广度。建议多关注实践应用，提升综合能力。';
    },

    // 美化学习路径
    formatLearningPath(path) {
      if (!path || path.trim() === '') {
        return '根据您当前的学习表现，建议继续按照课程安排稳步学习，适当增加课外拓展。';
      }
      
      return path
        .replace(/\*\*/g, '')
        .replace(/\*/g, '')
        .replace(/^\d+\.\s*/gm, '• ')
        .replace(/^-\s*/gm, '• ')
        .trim();
    },

    // 计算并格式化得分率
    calculateScoreRate(grade, maxScore) {
      if (!grade || grade === '暂无') {
        return '暂无';
      }
      
      const numGrade = parseFloat(grade);
      const numMaxScore = parseFloat(maxScore);
      
      if (isNaN(numGrade) || isNaN(numMaxScore) || numMaxScore === 0) {
        return '暂无';
      }
      
      // 计算得分率：得分 / 总分 * 100%
      const percentage = (numGrade / numMaxScore) * 100;
      
      return Math.round(percentage * 100) / 100 + '%';
    },
  },
  async mounted() {
    try {
      const courseRes = await axios.get('/student/courses', {
        params: { page: 1, size: 10 }
      })
      const courses = courseRes?.content || []

      // 使用Promise.allSettled确保单个课程错误不会中断整个流程
      const coursePromises = courses.map(async course => {
        try {
          // 获取任务列表
          const taskRes = await axios.get(`/teacher/courses/${course.courseId}/tasks`)
          const tasks = taskRes?.data || []

          // 获取提交记录
          let submissions = []
          try {
            const subRes = await axios.get(
              `/submissions/get_submissions_of_course/${course.courseId}`
            )
            submissions = subRes || []
          } catch (e) {
            console.warn(`获取课程 ${course.name} 提交记录失败:`, e)
          }

          // 匹配任务成绩
          const scoredTasks = tasks.map(task => {
            const submission = submissions.find(sub => sub.taskId === task.taskId)
            let statusText = '未提交'
            if (submission) {
              statusText = submission.finalGrade !== null ? `${submission.finalGrade} 分` : '未批改'
            }

            return {
              ...task,
              myScore: submission?.finalGrade ?? null,
              statusText
            }
          })

          // 获取课程反馈
          let feedback = ''
          try {
            const response = await axios.get(`/feedback/${course.courseId}`)
            feedback = response || ''
          } catch (error) {
            if (error.response?.status === 400 &&
              error.response.data?.message?.includes('Grade not found')) {
              feedback = ''
            } else {
              console.error(`获取课程 ${course.name} 反馈失败:`, error)
              feedback = '获取反馈失败'
            }
          }

          // 获取成绩趋势数据
          let gradeTrend = null;
          try {
            const gradeRes = await axios.get(`/grades/course/${course.courseId}/student`)
            if (gradeRes && gradeRes.gradeTrend) {
              gradeTrend = JSON.parse(gradeRes.gradeTrend);
              console.log(`课程 ${course.name} 成绩趋势数据:`, gradeTrend);
            }
          } catch (error) {
            if (error.response?.status === 404) {
              console.log(`课程 ${course.name} 暂无成绩数据`)
            } else {
              console.error(`获取课程 ${course.name} 成绩趋势失败:`, error)
            }
          }

          return {
            ...course,
            tasks: scoredTasks,
            feedback: feedback,
            gradeTrend: gradeTrend,
            // 推荐相关字段
            recommendations: null,
            loadingRecommendations: false,
            recommendationError: null,
            activeTab: 'comprehensive' // 默认显示综合建议
          }
        } catch (courseError) {
          console.error(`处理课程 ${course.name} 时出错:`, courseError)
          return {
            ...course,
            tasks: [],
            feedback: '数据加载失败',
            gradeTrend: null,
            recommendations: null,
            loadingRecommendations: false,
            recommendationError: null,
            activeTab: 'comprehensive'
          }
        }
      })

      // 等待所有课程处理完成
      const results = await Promise.allSettled(coursePromises)

      // 提取成功的课程数据
      this.courseList = results
        .filter(result => result.status === 'fulfilled')
        .map(result => result.value)

      // 渲染所有趋势图
      this.$nextTick(() => {
        this.courseList.forEach(course => {
          if (course.gradeTrend && course.gradeTrend.scores && course.gradeTrend.scores.length) {
            this.renderGradeTrendChart(course.courseId, course.gradeTrend)
          }
        })
      })

    } catch (err) {
      console.error('加载课程与成绩失败:', err)
    }
  }
}
</script>

<style scoped>
.student-grades {
  padding: 20px;
}

.course-block {
  background: white;
  border-radius: 10px; /* 圆角 */
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  margin-bottom: 20px;
  padding: 15px;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.course-block:hover {
  transform: translateY(-5px);  /* 向上移动 */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2); /* 增加阴影 */
}

.course-block h3 {
  margin-bottom: 8px;
}

.course-block ul {
  margin-left: 20px;
}

.course-block li {
  margin-bottom: 6px;
}

.grade-trend-chart {
  height: 350px;
  margin: 15px 0;
}

/* 推荐内容样式 */
.refresh-btn {
  background: #4CAF50; /* 深绿色 */
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px; /* 更圆的按钮 */
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.refresh-btn:hover {
  background: #45a049; /* 深色hover */
}

.refresh-btn:disabled {
  background: #cccccc; /* 禁用时为灰色 */
  cursor: not-allowed;
}

.loading-recommendations {
  text-align: center;
  padding: 20px;
  color: #666;
}

.loading-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.recommendations-container {
  margin-top: 10px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  background: white;
}

.recommendation-tabs {
  display: flex;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
  border-radius: 6px 6px 0 0;
}

.tab-btn {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
}

.tab-btn.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
  background: white;
}

.tab-btn:hover {
  color: #1890ff;
}

.recommendation-content {
  padding: 15px;
}

.knowledge-item, .resource-item {
  background: #fafafa;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.knowledge-header, .resource-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.knowledge-header h5, .resource-header h5 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.priority-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.priority-high {
  background: #ff4d4f;
  color: white;
}

.priority-medium {
  background: #faad14;
  color: white;
}

.priority-low {
  background: #52c41a;
  color: white;
}

.resource-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.type-video {
  background: #722ed1;
  color: white;
}

.type-pdf {
  background: #eb2f96;
  color: white;
}

.type-document {
  background: #13c2c2;
  color: white;
}

.type-ppt {
  background: #fa8c16;
  color: white;
}

.type-default {
  background: #595959;
  color: white;
}

.knowledge-description, .resource-description {
  color: #666;
  margin-bottom: 10px;
  line-height: 1.4;
}

.knowledge-stats, .resource-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.knowledge-stats span, .resource-stats span {
  font-size: 12px;
  color: #999;
}

.mastery-level {
  color: #1890ff !important;
  font-weight: bold;
}

.learning-readiness {
  color: #52c41a !important;
  font-weight: bold;
}

.related-topic {
  color: #722ed1 !important;
  font-weight: bold;
}

.recommendation-reason {
  color: #666;
  font-style: italic;
  margin-bottom: 10px;
}

.resource-actions {
  display: flex;
  gap: 10px;
}

.view-btn {
  background: #1890ff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.view-btn:hover {
  background: #40a9ff;
}

.comprehensive-recommendation {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.learning-status h5, .learning-path h5, .ai-suggestion h5, .recommendation-mode-explanation h5 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.status-card {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 6px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.status-item {
  display: flex;
  justify-content: space-between;
}

.status-item .label {
  color: #666;
  font-weight: bold;
}

.status-item .value {
  color: #1890ff;
  font-weight: bold;
}

.path-content, .suggestion-content {
  background: #f0f8ff;
  padding: 15px;
  border-radius: 6px;
  border-left: 4px solid #1890ff;
}

.recommendation-mode-explanation {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.explanation-content p {
  margin: 8px 0;
  color: #666;
  line-height: 1.5;
}

.grade-based-explanation {
  border-left: 4px solid #52c41a;
  padding-left: 10px;
}

.knowledge-based-explanation {
  border-left: 4px solid #1890ff;
  padding-left: 10px;
}

.recommendation-footer {
  padding: 10px 15px;
  background: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  text-align: center;
}

.generation-time {
  color: #999;
  font-size: 12px;
  margin: 0;
}

.recommendation-error {
  text-align: center;
  padding: 20px;
  color: #ff4d4f;
}

.retry-btn {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

.retry-btn:hover {
  background: #ff7875;
}

.no-recommendations, .no-data {
  text-align: center;
  padding: 20px;
  color: #999;
}
.student-grades {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.student-grades::before {
  content: "";
  position: absolute;
  top: -80px;
  left: -80px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle at center, rgba(170, 205, 245, 0.25), rgba(74, 144, 226, 0.4));
  border-radius: 50%;
  z-index: 0;
  filter: blur(2px);
}

/* 右上角斜切装饰 */
.student-grades::after {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 180px;
  height: 180px;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.3), rgba(74, 144, 226, 0.4));
  clip-path: polygon(100% 0, 0 0, 100% 100%);
  z-index: 0;
}

/* 顶部右边圆点 */
.decor-circle-small {
  position: absolute;
  top: 30px;
  right: 60px;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: rgba(74, 144, 226, 0.25);
  z-index: 0;
}

/* 左下角圆弧 */
.decor-lower-left {
  position: absolute;
  bottom: -60px;
  left: -60px;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle at center, rgba(170, 205, 245, 0.25), rgba(74, 144, 226, 0.4));
  border-radius: 50%;
  z-index: 0;
  filter: blur(2px);
}

/* 右下角斜切图形 */
.decor-lower-right {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.3), rgba(74, 144, 226, 0.4));
  clip-path: polygon(0 100%, 100% 100%, 100% 0);
  z-index: 0;
}

</style>