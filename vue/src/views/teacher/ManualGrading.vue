<template>
  <div class="manual-grading">
    <h2>主观题批改</h2>

    <!-- 主观题答案 -->
    <div v-if="manualQuestions.length > 0" class="question-section">
      <h3>客观题已自动批改</h3>
      <h3>主观题</h3>
      <div v-for="(q, i) in manualQuestions" :key="q.questionId" class="question-item">
        <div class="question-number">{{ i + 1 }}.</div>
        <div class="question-content">
          <p class="question-text">{{ q.questionText }}</p>
          <p>学生答案：{{ q.answers.join(', ') }}</p>
          <div class="score-input-container">
            <input
              type="number"
              :min="0"
              :max="q.maxScore"
              step="0.5"
              :placeholder="`请输入得分 (0-${q.maxScore})`"
              v-model.number="grades[q.recordId]"
              class="score-input"
            />
            <span class="max-score">/ {{ q.maxScore }}分</span>
          </div>
        </div>
      </div>
    </div>

    <div class="actions">
      <button class="btn secondary-btn" @click="goBack">返回</button>
      <button class="btn primary-btn" @click="submitGrades">提交批改</button>
    </div>
  </div>
</template>

<script>
import api from '@/axios';

export default {
  name: 'ManualGrading',
  data() {
    return {
      submissionId: this.$route.params.submissionId,
      autoQuestions: [],
      manualQuestions: [],
      grades: {}
    };
  },
  methods: {
    async fetchSubmission() {
      try {
        // 拉取所有答题记录
        const res = await api.get(`/grading/manual-questions/${this.submissionId}`);
        const manualList = res;

        // 检查是否有需要手动批改的题目
        if (manualList.length === 0) {
          alert('自动批改已完成');
          this.$router.back();
          return;
        }

        // 获取所有题目的内容和分值
        const questionContents = {};
        const questionScores = {};
        await Promise.all(
          manualList.map(async (record) => {
            try {
              const questionRes = await api.get(`/question/get/${record.questionId}`);
              questionContents[record.questionId] = questionRes.content || '题目内容缺失';
              questionScores[record.questionId] = questionRes.score || questionRes.points || 5; // 默认5分
            } catch (err) {
              console.error(`获取题目${record.questionId}内容失败`, err);
              questionContents[record.questionId] = '题目内容缺失';
              questionScores[record.questionId] = 5; // 默认5分
            }
          })
        );

        // 拆分客观题和主观题
        this.manualQuestions = manualList.map(record => ({
          recordId: record.recordId,
          questionId: record.questionId,
          questionText: questionContents[record.questionId],
          answers: record.answers || [],
          maxScore: questionScores[record.questionId]
        }));


      } catch (err) {
        console.error('加载提交记录失败', err);
        alert('加载提交记录失败');
      }
    },
    getStudentAnswer(q) {
      return q.answers ? q.answers.join(', ') : '未作答';
    },
    getQuestionNumber(recordId) {
      const index = this.manualQuestions.findIndex(q => q.recordId == recordId);
      return index >= 0 ? index + 1 : '未知';
    },
    async submitGrades() {
      // 验证分数范围
      for (const [recordId, score] of Object.entries(this.grades)) {
        if (score === null || score === undefined) {
          alert('请填写所有主观题的得分');
          return;
        }
        
        // 找到对应题目的最大分值
        const question = this.manualQuestions.find(q => q.recordId == recordId);
        const maxScore = question ? question.maxScore : 5;
        
        if (score < 0 || score > maxScore) {
          alert(`分数必须在0-${maxScore}分之间，当前第${this.getQuestionNumber(recordId)}题得分为${score}`);
          return;
        }
      }

      const questionGrades = Object.entries(this.grades).map(([recordId, score]) => ({
        recordId,
        score: Number(score),
        feedback: ''
      }));

      if (questionGrades.length === 0) {
        alert('请至少填写一个题目的得分');
        return;
      }

      try {
        await api.put(`/grading/grade-manual/${this.submissionId}`, {
          questionGrades,
          feedback: '老师手动批改'
        });
        alert('批改成功');
        this.$router.back();
      } catch (err) {
        console.error('提交批改失败', err);
        alert('提交批改失败');
      }
    },
    goBack() {
      this.$router.back();
    }
  },
  mounted() {
    this.fetchSubmission();
  }
};
</script>

<style scoped>
.manual-grading {
  padding: 30px;
  max-width: 900px;
  margin: auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 30px;
}

.question-section {
  margin-bottom: 30px;
}

.question-item {
  display: flex;
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fafafa;
}

.question-number {
  font-weight: bold;
  color: #007bff;
  margin-right: 10px;
  font-size: 16px;
  min-width: 30px;
}

.question-content {
  flex: 1;
}

.question-text {
  margin: 0 0 10px 0;
  font-size: 16px;
  line-height: 1.5;
  color: #333;
}

.score-input {
  width: 150px;
  padding: 6px;
  margin-top: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.actions {
  text-align: center;
  margin-top: 30px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin: 0 10px;
}

.primary-btn {
  background-color: #007bff;
  color: white;
}

.secondary-btn {
  background-color: #6c757d;
  color: white;
}

.primary-btn:hover {
  background-color: #0056b3;
}

.secondary-btn:hover {
  background-color: #5a6268;
}
</style>
