<template>
  <div class="generate-exam">
    <h2>生成试卷</h2>
    <p>题库ID: {{ questionBankId || '正在获取...' }}</p>
    
    <div class="info-box">
      <h4>📋 生成说明</h4>
      <p>• 试卷将从当前课程对应的题库中随机选择题目</p>
      <p>• 如果生成失败，可能是题库中的题目数量不足</p>
      <p>• 请确保题库中有足够的各类型题目</p>
      <p>• 点击"检查题库"按钮可以查看当前题库的题目统计</p>
    </div>

    <div class="form-group">
      <label>组卷方式</label>
      <select v-model="examDetails.mode">
        <option value="random">随机</option>
        <option value="difficulty">按难度</option>
        <option value="knowledge">按知识点</option>
      </select>
    </div>

    <div class="form-group">
      <label>题目总数</label>
      <input type="number" v-model.number="examDetails.totalCount" min="1" max="50" />
    </div>

    <div v-if="examDetails.mode === 'difficulty'" class="form-group">
      <label>难度分布（总和应等于题目总数）</label>
      <div class="difficulty-row" v-for="(label, key) in difficultyLabels" :key="key">
        <label>{{ label }}：</label>
        <input
          type="number"
          v-model.number="examDetails.difficulty[key]"
          min="0"
          @input="onDifficultyChange(key)"
        />
      </div>
      <p v-if="difficultySum !== examDetails.totalCount" style="color: red;">
        难度分布总和（{{ difficultySum }}）与题目总数（{{ examDetails.totalCount }}）不一致
      </p>
    </div>

    <div v-if="examDetails.mode === 'knowledge'" class="form-group">
      <label>知识点（可多选）</label>
      <div>
        <label v-for="kp in knowledgePoints" :key="kp.id" style="margin-right: 15px;">
          <input
            type="checkbox"
            :value="kp.id"
            v-model="examDetails.knowledgePointIds"
          />
          {{ kp.name }}
        </label>
      </div>
    </div>

    <button class="btn primary-btn" @click="generateExam" :disabled="generating">
      {{ generating ? '生成中...' : '生成试卷' }}
    </button>
    <button class="btn info-btn" @click="checkQuestionBank" style="margin-left: 10px;">检查题库</button>

    <div v-if="generatedQuestions.length > 0">
      <h3>预览试卷</h3>
      <p>组卷方式: {{ getModeText(examDetails.mode) }}</p>
      <p>题目总数: {{ generatedQuestions.length }}</p>
      <ul>
        <li v-for="(q, index) in generatedQuestions" :key="index">
          <strong>{{ index + 1 }}.</strong> {{ q.content || q.questionId || '题目ID: ' + (q.id || index) }}
        </li>
      </ul>
      <button class="btn secondary-btn" @click="goToPreview">预览所有题目</button>
    </div>
  </div>
</template>

<script>
import api from '@/axios';

export default {
  name: 'GenerateExam',
  data() {
    return {
      examDetails: {
        mode: 'random',
        totalCount: 10,
        difficulty: { easy: 4, medium: 4, hard: 2 },
        knowledgePointIds: []
      },
      knowledgePoints: [],
      difficultyLabels: {
        easy: '简单',
        medium: '中等',
        hard: '困难'
      },
      generatedQuestions: [],
      courseId: '',
      questionBankId: '',
      generating: false,
      taskCode: ''
    };
  },
  computed: {
    difficultySum() {
      const d = this.examDetails.difficulty;
      return (d.easy || 0) + (d.medium || 0) + (d.hard || 0);
    }
  },
  async mounted() {
    this.courseId = this.$route.params.courseId;
    if (!this.courseId) return alert('缺少课程ID');
    await this.fetchCourseDetails();
    await this.fetchKnowledgePoints();
    try {
      const res = await api.get(`/teacher/courses/${this.courseId}/tasks`);
      const tasks = Array.isArray(res) ? res : res.data || [];
      const examTasks = tasks.filter(t => t.type === 'EXAM_QUIZ');
      this.taskCode = `试卷任务-${String(examTasks.length + 1).padStart(3, '0')}`;
    } catch (err) {
      console.error('加载任务编号失败', err);
      alert('任务编号生成失败');
    }
  },
  methods: {
    async fetchCourseDetails() {
      try {
        const res = await api.get(`/teacher/courses/${this.courseId}`);
        const courseData = res.data || res;
        this.questionBankId = courseData.questionBankId || courseData.bankId || `bank_${this.courseId}`;
      } catch (err) {
        console.error('获取课程详情失败', err);
        this.questionBankId = `bank_${this.courseId}`;
        alert('获取课程详情失败，将使用默认题库');
      }
    },
    async fetchKnowledgePoints() {
      try {
        const res = await api.get(`/teacher/courses/${this.courseId}/knowledge-points`);
        this.knowledgePoints = (res.data || []).map(kp => ({
          id: kp.pointId || kp.id,
          name: kp.name
        }));
      } catch (err) {
        console.error('获取知识点失败', err);
      }
    },
    onDifficultyChange() {
      // 不再自动调整难度分布，让用户自行控制
    },
    async generateExam() {
      if (!this.questionBankId || this.questionBankId.trim() === '') {
        alert('题库ID缺失，请刷新页面重试');
        return;
      }

      if (this.examDetails.totalCount < 1) {
        alert('题目总数必须大于0');
        return;
      }

      // 难度模式校验
      if (this.examDetails.mode === 'difficulty') {
        if (this.difficultySum !== this.examDetails.totalCount) {
          alert(`难度分布总和（${this.difficultySum}）与题目总数（${this.examDetails.totalCount}）不一致`);
          return;
        }
      }

      // 知识点模式校验
      if (this.examDetails.mode === 'knowledge') {
        if (this.examDetails.knowledgePointIds.length === 0) {
          return alert('请选择知识点');
        }
      }

      const payload = {
        courseId: this.courseId,
        bankId: this.questionBankId,
        mode: this.examDetails.mode,
        totalCount: this.examDetails.totalCount,
        title: this.taskCode
      };

      if (this.examDetails.mode === 'difficulty') {
        payload.difficultyDistribution = this.examDetails.difficulty;
      }
      
      if (this.examDetails.mode === 'knowledge') {
        payload.knowledgePointIds = this.knowledgePoints
          .filter(kp => this.examDetails.knowledgePointIds.includes(kp.id))
          .map(kp => kp.name);
      }

      this.generating = true;
      try {
        const res = await api.post('/paper/generate', payload);
        
        // 兼容不同的响应格式
        const responseData = res.data || res;
        const paper = responseData.data || responseData;
        const questions = paper.questions || [];

        if (!Array.isArray(questions) || questions.length === 0) {
          throw new Error('未获取到题目数据，可能是题库中题目数量不足');
        }

        this.generatedQuestions = questions;

        // 保存到localStorage供后续使用
        localStorage.setItem('generatedExam', JSON.stringify({
          mode: this.examDetails.mode,
          taskCode: this.taskCode,
          questions,
          questionBankId: this.questionBankId,
          paperId: paper.paperId,
          totalCount: this.examDetails.totalCount
        }));

        alert('✅ 试卷生成成功，将返回任务编辑页面继续填写任务信息');
        this.$router.push({
          name: 'AddTask',
          params: { courseId: this.courseId },
          query: { fromExam: 'true' }
        });
      } catch (err) {
        console.error('试卷生成失败', err);
        const errorMsg = err.response?.data?.message || err.message || '生成失败，请检查参数设置和题库内容';
        alert('❌ ' + errorMsg);
      } finally {
        this.generating = false;
      }
    },
    async checkQuestionBank() {
      if (!this.questionBankId || this.questionBankId.trim() === '') {
        alert('题库ID缺失，无法检查');
        return;
      }

      try {
        // 根据后端API路径调用题库信息接口
        const bankInfoRes = await api.get(`/questionBank/${this.questionBankId}/info`);
        
        // 获取题库中的题目列表来统计数量
        const questionsRes = await api.get(`/questionBank/${this.questionBankId}/question/list`);
        
        const questions = questionsRes.data || questionsRes || [];
        const totalCount = questions.length;
        
        // 处理ApiResponse包装的数据
        const bankInfo = bankInfoRes.data || bankInfoRes;
        const bankName = bankInfo.name || bankInfo.bankName || '未知题库';
        
        let resultMessage = `📊 题库检查结果\n\n`;
        resultMessage += `📚 题库名称：${bankName}\n`;
        resultMessage += `🆔 题库ID：${this.questionBankId}\n\n`;
        resultMessage += `📝 题目统计：\n`;
        resultMessage += `   • 总题目数：${totalCount}\n\n`;
        resultMessage += `⚙️ 当前配置需要：\n`;
        resultMessage += `   • 题目总数：${this.examDetails.totalCount}题\n\n`;
        
        const canGenerate = totalCount >= this.examDetails.totalCount;
        resultMessage += canGenerate ? 
          '✅ 题库题目数量充足，可以生成试卷！' : 
          `❌ 题库题目数量不足，无法生成试卷！\n\n💡 解决方案：\n1. 减少题目数量（当前题库有 ${totalCount} 题）\n2. 联系管理员添加更多题目到题库`;
        
        alert(resultMessage);
      } catch (err) {
        console.error('检查题库失败', err);
        
        let errorMsg = '无法检查题库信息';
        if (err.response?.status === 404) {
          errorMsg = `❌ 题库不存在！\n\n🆔 题库ID: ${this.questionBankId}\n\n💡 可能的解决方案：\n1. 联系管理员创建该题库\n2. 确认课程是否正确配置了题库\n3. 检查题库ID是否正确`;
        } else if (err.response?.data?.message) {
          errorMsg = `❌ ${err.response.data.message}`;
        } else if (err.message) {
          errorMsg = `❌ ${err.message}`;
        }
        
        alert(errorMsg);
      }
    },
    goToPreview() {
      // 检查是否有生成的试卷数据
      if (!this.generatedQuestions.length) {
        alert('请先生成试卷');
        return;
      }
      
      // 确保localStorage中有数据
      const examData = localStorage.getItem('generatedExam');
      if (!examData) {
        alert('试卷数据丢失，请重新生成');
        return;
      }
      
      // 跳转到预览页面，使用临时ID和query参数
      this.$router.push({ 
        name: 'PreviewExam', 
        params: { 
          id: 'temp-preview' // 使用临时ID
        },
        query: {
          fromGenerated: 'true' // 标记来自生成页面
        }
      });
    },
    getModeText(mode) {
      const modeMap = {
        'random': '随机组卷',
        'difficulty': '按难度组卷',
        'knowledge': '按知识点组卷'
      };
      return modeMap[mode] || mode;
    }
  }
};
</script>

<style scoped>
.generate-exam {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  background: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.info-box {
  background: #e8f4fd;
  border-left: 4px solid #4a90e2;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.info-box h4 {
  margin: 0 0 10px 0;
  color: #2c5aa0;
}

.info-box p {
  margin: 5px 0;
  color: #555;
  font-size: 14px;
}

.form-group {
  margin-bottom: 15px;
}
input, select {
  width: 100%;
  margin: 5px 0;
  padding: 8px;
}
.difficulty-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.difficulty-row label {
  width: 60px;
}
.difficulty-row input {
  flex: 1;
}
.btn {
  padding: 10px 20px;
  border-radius: 4px;
  margin-top: 10px;
}
.primary-btn {
  background: #4a90e2;
  color: #fff;
  border: none;
  cursor: pointer;
}
.primary-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.info-btn {
  background: #17a2b8;
  color: #fff;
  border: none;
}
.secondary-btn {
  background: #6c757d;
  color: #fff;
  border: none;
  margin-left: 10px;
}
</style>