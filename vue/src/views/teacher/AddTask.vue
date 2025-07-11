<template>
  <div class="add-task">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">返回</button>
    <h2>发布新任务</h2>
    <div v-if="task.type === 'EXAM_QUIZ'" class="btn-group">
      <button @click="goToGenerateExam" class="btn generate-btn">生成试卷</button>
      <button @click="previewExam" class="btn preview-btn" :disabled="!generatedExam">预览试卷</button>
      <button @click="clearGeneratedExam" class="btn delete-btn" :disabled="!generatedExam">删除试卷</button>
    </div>

    <div class="form-group">
      <label>任务名称</label>
      <input type="text" v-model="task.title" placeholder="请输入任务名称" required />
    </div>

    <div class="form-group">
      <label>任务类型</label>
      <select v-model="task.type" required>
        <option value="CHAPTER_HOMEWORK">章节作业</option>
        <option value="VIDEO_WATCHING">视频观看</option>
        <option value="MATERIAL_READING">阅读材料</option>
        <option value="PPT_VIEW">PPT浏览</option>
        <option value="REPORT_SUBMISSION">报告上传</option>
        <option value="EXAM_QUIZ">试卷答题</option>
      </select>
    </div>

    <div class="form-group">
      <label>截止时间</label>
      <input type="datetime-local" v-model="task.deadline" :min="minDateTime" required />
    </div>

    <div class="form-group">
      <label>任务描述</label>
      <textarea v-model="task.description" placeholder="任务描述"></textarea>
    </div>

    <div v-if="task.type !== 'EXAM_QUIZ'">
      <div class="form-group">
        <label>选择文件 (可多选)</label>
        <input type="file" multiple @change="handleFileChange" />
      </div>

      <div v-if="files.length > 0" class="file-list">
        <h3>已选文件:</h3>
        <ul>
          <li v-for="(file, index) in files" :key="index">
            {{ file.name }}
            <button @click="removeFile(index)" class="remove-btn">×</button>
          </li>
        </ul>
        <button class="btn primary-btn" @click="uploadFiles">确认上传</button>
      </div>

      <div v-if="uploadedResources.length > 0" class="upload-result">
        <h3>已上传资源:</h3>
        <ul>
          <li v-for="(resource, index) in uploadedResources" :key="index">
            {{ resource.name }} (ID: {{ resource.id }})
          </li>
        </ul>
      </div>
    </div>
    <div v-if="task.type !== 'EXAM_QUIZ'" class="form-group">
  <label>总分</label>
  <input type="number" v-model.number="task.maxScore" min="0" placeholder="请输入总分" required />
</div>

    <button class="btn primary-btn" @click="saveTask">发布任务</button>
  </div>
</template>

<script>
import api from '@/axios';

export default {
  name: 'AddTask',
  data() {
    return {
      task: {
        title: '',
        type: 'CHAPTER_HOMEWORK',
        deadline: '',
        description: '',
        paperId: '' ,// 新增
        maxScore: 0
      },
      minDateTime: new Date().toISOString().slice(0, 16),
      files: [],
      uploadedResources: [],
      generatedExam: null
    };
  },
  mounted() {
    if (this.$route.query.fromExam === 'true') {
      this.task.type = 'EXAM_QUIZ';
      const exam = localStorage.getItem('generatedExam');
      if (exam) {
        try {
          this.generatedExam = JSON.parse(exam);
          this.task.paperId = this.generatedExam.paperId || '';
        } catch (e) {
          console.warn('解析缓存试卷失败', e);
        }
      }
    }
  },
  methods: {
    /**
     * 将日期转换为后端要求的 ISO 8601 格式（带 T 分隔符）
     * 格式：YYYY-MM-DDTHH:mm:ss
     */
    formatDeadlineForBackend(dateTime) {
      if (!dateTime) return null;

      // 情况1: 已经是带 T 的格式 (2026-01-01T01:01:00)
      if (typeof dateTime === 'string' && dateTime.includes('T') && dateTime.length >= 19) {
        return dateTime.substring(0, 19);
      }

      // 情况2: 空格分隔的格式 (2026-01-01 01:01:00)
      if (typeof dateTime === 'string' && dateTime.includes(' ') && dateTime.length >= 19) {
        return dateTime.replace(' ', 'T').substring(0, 19);
      }

      // 情况3: 只有年月日时分 (2026-01-01 01:01)
      if (typeof dateTime === 'string' && dateTime.length === 16) {
        return dateTime.replace(' ', 'T') + ':00';
      }

      // 情况4: 日期选择器返回的日期对象
      if (dateTime instanceof Date) {
        return this.formatDateToISO(dateTime);
      }

      // 情况5: 其他格式 - 尝试转换
      console.warn('无法识别的日期格式:', dateTime);

      try {
        // 尝试解析为Date对象
        const parsedDate = new Date(dateTime);
        if (!isNaN(parsedDate.getTime())) {
          return this.formatDateToISO(parsedDate);
        }

        // 尝试移除时区信息和毫秒
        const cleanDateTime = dateTime.split('.')[0].split('Z')[0];

        // 确保有 T 分隔符
        if (cleanDateTime.includes(' ')) {
          return cleanDateTime.replace(' ', 'T').substring(0, 19);
        }

        // 尝试添加秒数
        if (cleanDateTime.length === 16) {
          return cleanDateTime + ':00';
        }
      } catch (e) {
        console.error('日期解析失败:', e);
      }

      // 最后尝试 - 使用当前时间作为后备
      console.error('日期格式无法解析，使用当前时间');
      return this.formatDateToISO(new Date());
    },

    /**
     * 将Date对象格式化为 ISO 8601 格式（YYYY-MM-DDTHH:mm:ss）
     */
    formatDateToISO(date) {
      const pad = num => num.toString().padStart(2, '0');

      return [
        date.getFullYear(),
        pad(date.getMonth() + 1),
        pad(date.getDate())
      ].join('-') + 'T' + [
        pad(date.getHours()),
        pad(date.getMinutes()),
        pad(date.getSeconds())
      ].join(':');
    },
    previewExam() {
      if (!this.generatedExam) {
        alert('试卷为空，请先生成试卷');
        return;
      }
      if (this.task.paperId) {
        this.$router.push({
          name: 'PreviewExam',
          params: { id: this.task.paperId || 'temp-preview' },
          query: { paperId: this.task.paperId }
        });
      } else {
        this.$router.push({
          name: 'PreviewExam',
          params: { id: 'temp-preview' },
          query: { fromGenerated: 'true' }
        });
      }
    },
    clearGeneratedExam() {
      localStorage.removeItem('generatedExam');
      this.generatedExam = null;
      this.task.paperId = '';
      alert('已清除生成的试卷');
    },
    goToGenerateExam() {
      const courseId = this.$route.params.courseId;
      if (!courseId) {
        alert('课程ID缺失');
        return;
      }
      this.$router.push({ name: 'GenerateExam', params: { courseId } });
    },
    handleFileChange(event) {
      this.files = Array.from(event.target.files);
    },
    removeFile(index) {
      this.files.splice(index, 1);
    },
    async uploadFiles() {
      if (this.files.length === 0) return;

      const courseId = this.$route.params.courseId;
      if (!courseId) {
        alert('缺少课程ID');
        return;
      }

      this.uploading = true;
      const newResources = [];

      try {
        // 使用 Promise.all 并行上传所有文件
        const uploadPromises = this.files.map(file =>
            this.uploadSingleFile(file, courseId)
        );

        // 等待所有文件上传完成
        const results = await Promise.allSettled(uploadPromises);

        // 处理上传结果
        results.forEach(result => {
          if (result.status === 'fulfilled' && result.value) {
            newResources.push(result.value);
          } else if (result.status === 'rejected') {
            console.error('文件上传失败:', result.reason);
            alert(`文件上传失败: ${result.reason.message || '未知错误'}`);
          }
        });

        // 更新已上传资源列表
        this.uploadedResources = [...this.uploadedResources, ...newResources];
        alert(`成功上传 ${newResources.length} 个文件`);
        this.files = [];

      } catch (err) {
        console.error('上传过程中出错:', err);
        alert('上传过程中发生错误');
      } finally {
        this.uploading = false;
      }
    },
    async uploadSingleFile(file, courseId) {
      try {
        const form = new FormData();
        form.append('file', file);
        form.append('name', file.name);
        form.append('type', this.getResourceType(file));
        form.append('description', `任务资源: ${this.task.title}`);

        const res = await api.post(`/teacher/courses/${courseId}/resources`, form);

        // 修复点：直接使用 res.data 获取资源ID
        if (res.data?.resourceId) {
          return {
            id: res.data.resourceId,
            name: res.data.name,
            type: res.data.type || this.getResourceType(file) // 添加类型回退
          };
        } else {
          console.warn('未返回有效资源ID:', res.data);
          throw new Error(`文件 ${file.name} 上传成功但未返回资源ID`);
        }
      } catch (err) {
        console.error(`文件 ${file.name} 上传失败:`, err);
        throw err;
      }
    },

    // 重置表单状态
    resetForm() {
      this.task = {
        title: '',
        description: '',
        type: '',
        deadline: '',
        maxScore: 0
      };
      this.files = [];
      this.uploadedResources = [];
    },
    getResourceType(file) {
      const ext = file.name.split('.').pop().toLowerCase();
      if (['ppt', 'pptx'].includes(ext)) return 'PPT';
      if (['pdf'].includes(ext)) return 'PDF';
      if (['mp4', 'mov', 'avi', 'mkv'].includes(ext)) return 'VIDEO';
      if (['doc', 'docx', 'txt', 'md', 'xls', 'xlsx'].includes(ext)) return 'DOCUMENT';
      if (['jpg', 'png', 'jpeg', 'bmp', 'webp'].includes(ext)) return 'IMAGE';
      if (['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma', 'm4a'].includes(ext)) return 'AUDIO';
      return 'DOCUMENT';
    },
    async saveTask() {
      if (this.task.type !== 'EXAM_QUIZ' && (!this.task.maxScore || this.task.maxScore <= 0)) {
  alert('请填写有效的总分（必须大于0）')
  return
}
      if (!this.task.title || !this.task.type || !this.task.deadline) {
        alert('请填写完整任务信息（标题、类型和截止时间）');
        return;
      }

      const courseId = this.$route.params.courseId;
      if (!courseId) {
        alert('缺少课程ID，无法创建任务');
        return;
      }

      if (this.saving) return;
      this.saving = true;

      try {
        // 1. 确保所有文件上传完成
        if (this.files.length > 0) {
          // 等待所有文件上传完成
          await this.uploadFiles();
        }

        // 2. 打印当前已上传资源状态
        console.log('当前已上传资源:', this.uploadedResources);

        // 3. 构建资源ID列表
        const resourceIds = this.uploadedResources.map(r => r.id);
        console.log('资源ID列表:', resourceIds);

        // 4. 构建任务负载
const payload = {
  courseId,
  title: this.task.title,
  description: this.task.description || '',
  type: this.task.type,
  deadline: this.formatDeadlineForBackend(this.task.deadline),
  maxScore: this.task.maxScore || 0,
  resourceIds,
  pointIds: []
}

// 针对试卷任务
if (this.task.type === 'EXAM_QUIZ') {
  if (!this.generatedExam?.paperId || !this.generatedExam?.questions?.length) {
    alert('试卷任务必须先生成试卷');
    this.saving = false
    return
  }
  payload.examQuestions = this.generatedExam.questions
  payload.paperId = this.generatedExam.paperId
}


        // 5. 发送创建任务请求
        const response = await api.post(`/teacher/courses/${courseId}/tasks`, payload);
        console.log('任务创建响应:', response.data);

        // 6. 成功后清理
        alert('任务发布成功');
        localStorage.removeItem('generatedExam');
        this.resetForm();
        this.$router.push({ name: 'TaskManagement' });
      } catch (err) {
        console.error('任务发布失败:', err);
        let errorMsg = '任务发布失败';

        if (err.response) {
          if (err.response.data?.message) {
            errorMsg += `: ${err.response.data.message}`;
          } else {
            errorMsg += `: HTTP ${err.response.status}`;
          }
        } else if (err.message) {
          errorMsg += `: ${err.message}`;
        }

        alert(errorMsg);
      } finally {
        this.saving = false;
      }
    },
  },



};
</script>

<style scoped>
.btn-group {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.preview-btn {
  background-color: #28a745;
}
.preview-btn:hover {
  background-color: #218838;
}
.delete-btn {
  background-color: #dc3545;
}
.delete-btn:hover {
  background-color: #c82333;
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.back-btn {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 4px;
  margin-bottom: 20px;
  cursor: pointer;
}
.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.generate-btn {
  padding: 10px 20px;
  background-color: #4a90e2;
  color: white;
  border: none;
  cursor: pointer;
}
.generate-btn:hover {
  background-color: #4a90e2;
}
.add-task {
  padding: 20px;
}
.form-group {
  margin-bottom: 15px;
}
label {
  font-weight: bold;
}
input, select, textarea {
  width: 100%;
  padding: 8px;
  margin-top: 5px;
}
button {
  padding: 10px 20px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-top: 10px;
}
button:hover {
  background-color: #357ab7;
}
.file-list, .upload-result {
  margin: 15px 0;
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 5px;
}
ul {
  padding-left: 20px;
}
li {
  margin: 5px 0;
  display: flex;
  justify-content: space-between;
}
.remove-btn {
  padding: 2px 8px;
  background: #ff6b6b;
  margin-left: 10px;
}
.remove-btn:hover {
  background: #ff5252;
}
</style>
