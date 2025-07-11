<template>
  <div class="preview-exam">
    <h2>作答试卷：{{ exam.taskCode }}（{{ modeLabel[exam.mode] || '未知模式' }}）</h2>

    <!-- 客观题 -->
    <div v-if="mcQuestions && mcQuestions.length > 0" class="question-section">
      <h3>客观题</h3>
      <div v-for="(q, i) in mcQuestions" :key="'mc-' + (q.questionId || i)" class="question-item">
        <div class="question-number">{{ i + 1 }}.</div>
        <div class="question-content">
          <p class="question-text">{{ getQuestionText(q) }}</p>
          <div v-if="getQuestionOptions(q).length > 0" class="options">
            <label
              v-for="(option, optIndex) in getQuestionOptions(q)"
              :key="optIndex"
              class="option"
            >
              <input
                :type="isMultipleChoice(q) ? 'checkbox' : 'radio'"
                :name="'mc-' + q.questionId"
                :value="getOptionLabel(optIndex)"
                :checked="isOptionChecked(q, getOptionLabel(optIndex))"
                @change="handleAnswerChange(q, getOptionLabel(optIndex), $event)"
              />
              {{ getOptionLabel(optIndex) }}. {{ getOptionText(option) }}
            </label>
          </div>
          <div v-else class="no-options">
            <p style="color: #999; font-style: italic;">该题目缺少选项数据</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 主观题 -->
    <div v-if="essayQuestions.length > 0" class="question-section">
      <h3>主观题</h3>
      <div v-for="(q, i) in essayQuestions" :key="'essay-' + i" class="question-item">
        <div class="question-number">{{ i + 1 }}.</div>
        <div class="question-content">
          <p class="question-text">{{ getQuestionText(q) }}</p>
          <textarea
            class="essay-answer"
            v-model="answers[q.questionId]"
            placeholder="请输入你的答案..."
            rows="4"
          ></textarea>
        </div>
      </div>
    </div>

    <div class="actions">
       <button class="btn primary-btn" @click="submitExam">提交试卷</button>
      <button class="btn secondary-btn" @click="$router.back()">取消</button>
    </div>
  </div>
</template>

<script>
import api from '@/axios'

export default {
  name: 'AnswerExam',
  data() {
    return {
      exam: { taskCode: '', mode: '', questions: [] },
      modeLabel: {
        random: '随机组卷',
        difficulty: '按难度组卷',
        knowledge: '按知识点组卷'
      },
      answers: {} // 确保是响应式对象
    }
  },
  computed: {
    mcQuestions() {
      return this.exam.questions.filter(q =>
        ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'MULTIPLE', 'JUDGE', 'TRUE_FALSE'].includes(
          q.type?.toUpperCase()
        )
      )
    },
    essayQuestions() {
      return this.exam.questions.filter(q =>
        ['FILL', 'FILL_BLANK', 'ESSAY', 'SHORT_ANSWER', 'PROGRAMMING'].includes(
          q.type?.toUpperCase()
        )
      )
    }
  },
  methods: {
    isMultipleChoice(q) {
      // 只有多选题使用复选框，其他都使用单选按钮
      return ['MULTIPLE_CHOICE', 'MULTIPLE'].includes(q.type?.toUpperCase())
    },
    handleAnswerChange(question, optionValue, event) {
      const questionId = question.questionId
      
      if (this.isMultipleChoice(question)) {
        // 多选题：使用数组存储
        if (!Array.isArray(this.answers[questionId])) {
          this.answers[questionId] = []
        }
        
        if (event.target.checked) {
          // 添加选项
          if (!this.answers[questionId].includes(optionValue)) {
            this.answers[questionId].push(optionValue)
          }
        } else {
          // 移除选项
          const index = this.answers[questionId].indexOf(optionValue)
          if (index > -1) {
            this.answers[questionId].splice(index, 1)
          }
        }
      } else {
        // 单选题、判断题：直接存储值
        this.answers[questionId] = optionValue
      }
    },
    isOptionChecked(question, optionValue) {
      const questionId = question.questionId
      const answer = this.answers[questionId]
      
      if (this.isMultipleChoice(question)) {
        // 多选题：检查数组中是否包含该选项
        return Array.isArray(answer) && answer.includes(optionValue)
      } else {
        // 单选题、判断题：直接比较值
        return answer === optionValue
      }
    },
    initializeAnswers() {
      // 初始化答案结构
      this.exam.questions.forEach((q, index) => {
        if (!q.questionId) {
          console.warn(`第${index + 1}题缺少questionId:`, q)
          return
        }
        
        if (this.isMultipleChoice(q)) {
          this.answers[q.questionId] = []
        } else {
          this.answers[q.questionId] = ''
        }
      })
    },
    getQuestionText(q) {
      return q.content || q.question || q.stem || q.text || '题目内容缺失'
    },
    getOptionLabel(i) {
      return String.fromCharCode(65 + i)
    },
    getOptionText(opt) {
      return typeof opt === 'string' ? opt : opt.text || opt.content || opt.label || JSON.stringify(opt)
    },
    getQuestionOptions(q) {
      if (Array.isArray(q.options)) return q.options
      if (typeof q.options === 'object') return Object.values(q.options)
      return []
    },
    async submitExam() {
      const taskId = this.$route.params.taskId

      const payload = {
        taskId,
        answerRecordDTO: Object.entries(this.answers).map(([questionId, ans]) => ({
          questionId,
          answers: Array.isArray(ans) ? ans : [ans]
        }))
      }

      try {
        await api.post('/submissions/submit/answers', payload)
        this.$message?.success('提交成功') || alert('提交成功')
        this.$router.back()
            } catch (err) {
        const msg = err?.response?.data?.message || err?.message || '提交失败'
        this.$message?.error(msg) || alert(msg)
      }
    }
  },
  async mounted() {
    const taskId = this.$route.params.taskId
    
    try {
      const paperRes = await api.get(`/paper/task/${taskId}`)
      const paper = paperRes.data || paperRes

      if (!paper || !paper.paperId) {
        throw new Error('试卷信息不完整，缺少paperId')
      }

      const paperId = paper.paperId
      const detailRes = await api.get(`/paper/${paperId}/questions`)
      const detail = detailRes.data || detailRes

      if (!detail || !Array.isArray(detail.questions)) {
        throw new Error('试卷详情不完整，缺少题目数据')
      }

      this.exam = {
        taskCode: paper.title || paper.taskCode || '试卷',
        mode: (paper.generationMethod || 'random').toLowerCase(),
        questions: detail.questions || []
      }
      
      // 初始化答案结构
      this.initializeAnswers()
      
    } catch (e) {
      console.error('加载试卷失败', e)
      
      const errorMsg = e.response?.data?.message || e.message || '试卷加载失败'
      this.$message?.error(errorMsg) || alert(errorMsg)
    }
  }
}
</script>

<style scoped>
@import './PreviewExam.css';

.essay-answer {
  width: 100%;
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>
