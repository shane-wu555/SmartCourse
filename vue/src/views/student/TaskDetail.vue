<template>
  <div class="task-detail">
    <div v-if="isLoading">加载中...</div>

    <div v-else-if="task">
      <button class="back-btn" @click="$router.back()">返回</button>
      <h2>{{ task.title }} - 任务详情</h2>

      <div class="task-info">
        <p><strong>任务类型:</strong> {{ displayTaskType(task.type) }}</p>
        <p><strong>截止时间:</strong> {{ task.deadline }}</p>
        <p><strong>任务描述:</strong> {{ task.description }}</p>
      </div>

      <!-- 任务资源 -->
      <div v-if="task.resources?.length" class="task-resources">
        <h3>任务资源</h3>
        <ul>
          <li v-for="res in task.resources" :key="res.resourceId">
            {{ res.name }}
            <button @click="downloadResource(res)">下载</button>

            <template v-if="res.type === 'VIDEO'">
  <button @click="viewVideo(res)">观看</button>
 
  <span v-if="videoProgressMap[res.resourceId]?.isFinished" style="color: green; margin-left: 10px">
    ✅ 已完成
  </span>
</template>


            <template v-else-if="['DOCUMENT', 'PPT'].includes(res.type)">
              <button @click="viewPreview(res)">查看</button>
              <button
                v-if="['PPT_VIEW', 'MATERIAL_READING'].includes(task.type)"
                :disabled="isCompleted"
                @click="markAsCompleted"
              >
                {{ isCompleted ? '已完成' : '我已完成' }}
              </button>
            </template>
          </li>
        </ul>
      </div>
      <!-- 文件提交类 -->
      <div v-if="['CHAPTER_HOMEWORK', 'REPORT_SUBMISSION'].includes(task.type)">
        <input
          type="file"
          ref="fileInput"
          multiple
          style="display: none"
          @change="handleFileChange"
        />
        <button
          :disabled="isCompleted"
          @click="triggerFileInput"
          class="btn primary-btn"
        >
          {{ isCompleted ? '已完成' : '提交' }}
        </button>
      </div>
      <!-- 考试任务 -->
      <div v-if="task.type === 'EXAM_QUIZ'">
        <button
          class="btn primary-btn"
          :disabled="isCompleted"
          @click="startExam"
        >
          {{ isCompleted ? '已完成' : '开始答题' }}
        </button>
      </div>
    </div>

    <div v-else>
      <p>未找到该任务，请检查链接是否正确。</p>
    </div>
  </div>
</template>

<script>
import axios from '@/axios'

export default {
  name: 'TaskDetail',
  props: ['id'],
  data() {
    return {
      task: null,
      isLoading: true,
      isCompleted: false,
      selectedFiles: [],
      videoProgressMap: {} // key 是 resourceId，value 是 { progress, isFinished }
    }
  },
  methods: {
    async fetchVideoProgressForResources() {
  if (!this.task || !this.task.resources) return;

  const results = {};

  for (const res of this.task.resources) {
    if (res.type !== 'VIDEO') continue;

    try {
      const { data } = await axios.get(`/video-progress/${res.resourceId}`);
      const videoLength = res.duration || 1;
      const watched = data?.totalWatched || 0;
      const progress = Math.min(watched / videoLength, 1);
      console.log(`视频 ${res.resourceId} 的观看进度: ${progress}`);
      
      // 检查是否达到90%并自动标记
      if (progress >= 0.9 && !this.isCompleted) {
        await this.markAsCompleted();
      }
      
      results[res.resourceId] = {
        progress,
        isFinished: progress >= 0.9
      };
    } catch (e) {
      console.warn(`无法获取视频 ${res.resourceId} 的观看进度`, e);
    }
  }

  this.videoProgressMap = results;
},

    async fetchTask() {
      try {
        const res = await axios.get(`/student/tasks/${this.id}`)
        this.task = res.data?.data || res.data
      } catch (err) {
        console.error('加载任务失败', err)
        alert(err.response?.data?.message || '加载任务失败')
      } finally {
        this.isLoading = false
      }
    },
    async checkSubmissionStatus() {
      try {
        console.log('我的提交状态:', this.isCompleted)
        const courseId = this.task?.courseId
        if (!courseId) return

        const res = await axios.get(`/submissions/get_submissions_of_course/${courseId}`)
        const submissions = res || []      

        const mySubmission = submissions.find(sub => sub.taskId === (this.task.taskId || this.id))
        console.log('我的提交:', submissions)
        console.log(this.id)
         console.log('我的提交:', mySubmission)
        this.isCompleted = !!mySubmission
        console.log('我的提交状态:', this.isCompleted)
      } catch (e) {
        console.warn('检查提交状态失败', e)
        this.isCompleted = false
      }
    },
    async markAsCompleted() {
      const taskId = this.task.taskId || this.id
      const token = localStorage.getItem('token')

      try {
        await axios.put(`http://localhost:8082/api/submissions/complete/${taskId}`, {}, {
          headers: {
            Authorization: token ? `Bearer ${token}` : undefined
          }
        })
        this.isCompleted = true
        alert('任务已标记为完成')
      } catch (err) {
        console.error('标记任务为完成失败:', err)
        alert(err.response?.data?.message || '提交失败')
      }
    },
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    handleFileChange(event) {
      const files = event.target.files
      if (files.length === 0) return
      this.selectedFiles = Array.from(files)
      this.submitFiles()
    },
    async submitFiles() {
      if (!this.selectedFiles.length) {
        alert('请选择要提交的文件')
        return
      }

      const formData = new FormData()
      const token = localStorage.getItem('token')

      formData.append('task_id', this.task.taskId || this.id)
      this.selectedFiles.forEach(file => {
        formData.append('files', file)
      })

      try {
        await axios.post('http://localhost:8082/api/submissions/submit/files', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: token ? `Bearer ${token}` : undefined
          }
        })
        this.isCompleted = true
        alert('提交成功')
        this.selectedFiles = []
      } catch (err) {
        console.error('提交失败:', err)
        alert(err.response?.data?.message || '提交失败')
      }
    },
    displayTaskType(type) {
      const map = {
        CHAPTER_HOMEWORK: '章节作业',
        VIDEO_WATCHING: '视频观看',
        MATERIAL_READING: '阅读材料',
        PPT_VIEW: 'PPT浏览',
        REPORT_SUBMISSION: '报告上传',
        EXAM_QUIZ: '试卷答题'
      }
      return map[type] || type
    },
    async downloadResource(resource) {
      try {
        const token = localStorage.getItem('token')
        const url = `http://localhost:8082/api/teacher/resources/${resource.resourceId}/download`
        const response = await axios.get(url, {
          responseType: 'blob',
          headers: {
            Authorization: token ? `Bearer ${token}` : undefined
          }
        })

        const blob = new Blob([response.data])
        const link = document.createElement('a')
        const fileName = resource.name || `resource_${resource.resourceId}.ppt`
        link.href = URL.createObjectURL(blob)
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(link.href)
      } catch (err) {
        console.error('下载失败:', err)
        alert('资源下载失败: ' + (err.message || '未知错误'))
      }
    },
    // TaskDetail.vue
viewVideo(res) {
  this.$router.push({ 
    name: 'VideoPlayer', 
    params: { resourceId: res.resourceId },
    query: { taskId: this.id } // 传递任务ID
  })
},
    viewPreview(res) {
      this.$router.push({ name: 'ResourcePreview', params: { resourceId: res.resourceId } })
    },
    startExam() {
      localStorage.setItem('justSubmittedExam', this.id)
      this.$router.push({ name: 'AnswerExam', params: { taskId: this.id } })
    }
  },
  async mounted() {    
    await this.fetchTask()    
    await this.checkSubmissionStatus()
    await this.fetchVideoProgressForResources()


    const justSubmitted = localStorage.getItem('justSubmittedExam')
    if (justSubmitted === this.id) {
      await this.checkSubmissionStatus()
      localStorage.removeItem('justSubmittedExam')
    }
  }
}
</script>

<style scoped>
.back-btn {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 4px;
  margin-bottom: 20px;
  cursor: pointer;
}
.task-detail {
  padding: 20px;
}
.task-info {
  margin-bottom: 20px;
}
.task-resources {
  margin-bottom: 20px;
}
.task-resources ul {
  padding-left: 20px;
}
.task-resources li {
  margin-bottom: 8px;
}
button {
  margin-left: 6px;
  padding: 8px 14px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
button:hover {
  background-color: #357ab7;
}
</style>
