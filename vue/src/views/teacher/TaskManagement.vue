<template>
  <div class="teacher-tasks">
    <div class="decor-lower-left"></div>
<div class="decor-lower-right"></div>
    <h2>我的课程及任务</h2>

    <div v-for="course in courses" :key="course.courseId" class="course-card">
      <h3>{{ course.name }} ({{ course.courseCode }})</h3>
      <p>学期: {{ course.semester }}</p>

      <button class="add-task" @click="goToAddTask(course.courseId)">发布任务</button>

      <div v-if="course.tasks.length">
        <h4>已发布任务</h4>
        <table class="task-table">
          <thead>
            <tr>
              <th>任务名称</th>
              <th>类型</th>
              <th>截止时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="task in course.tasks" :key="task.taskId">
              <tr>
                <td>{{ task.title }}</td>
                <td>{{ mapTaskTypeToChinese(task.type) }}</td>
                <td>{{ task.deadline }}</td>
                <td class="operation-cell">
                  <div class="btn-group">
                    <button class="outline-btn" @click="viewTask(task.taskId)">查看</button>
                    <button 
                      v-if="task.type !== 'EXAM_QUIZ'" 
                      class="outline-btn" 
                      @click="downloadTask(task.taskId)"
                    >下载</button>
                    <button 
                      v-else 
                      class="outline-btn disabled-btn"
                      disabled
                    >下载</button>

                    <button class="primary-btn" @click="openEditModal(course.courseId, task.taskId)">编辑</button>
                    <button class="danger-btn" @click="deleteTask(course.courseId, task.taskId)">删除</button>
                  </div>

                  <div class="btn-group">
                    <button
                      v-if="['CHAPTER_HOMEWORK', 'REPORT_SUBMISSION', 'EXAM_QUIZ'].includes(task.type)"
                      class="outline-btn"
                      @click="toggleSubmissions(task)"
                    >
                      {{ task.showSubmissions ? '隐藏提交' : '查看提交' }}
                    </button>
                  </div>
                </td>
              </tr>

              <!-- 提交记录展示区域 -->
              <tr v-if="task.showSubmissions && ['CHAPTER_HOMEWORK', 'REPORT_SUBMISSION', 'EXAM_QUIZ'].includes(task.type)">
                <td colspan="4">
                  <h5>学生提交列表</h5>
                  <table class="task-table nested">
                    <thead>
                      <tr>
                        <th>学生ID</th>
                        <th>任务类型</th>
                        <th>提交时间</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="submission in task.submissions" :key="submission.submissionId">
                        <td>{{ submission.studentId }}</td>
                        <td>{{ mapTaskTypeToChinese(task.type) }}</td>
                        <td>{{ submission.submitTime }}</td>
                        <td>
                          <div class="btn-group">
                            <!-- 非 EXAM_QUIZ 类型显示查看和下载按钮 -->
                            <template v-if="task.type !== 'EXAM_QUIZ'">
                              <button class="outline-btn small-btn" @click="viewSubmission(submission)">查看</button>
                              <button class="outline-btn small-btn" @click="downloadSubmission(submission)">下载</button>
                            </template>

                            <button 
                              v-if="['CHAPTER_HOMEWORK', 'REPORT_SUBMISSION', 'EXAM_QUIZ'].includes(task.type)"
                              :class="['small-btn', submission.fullyGraded ? 'disabled-btn' : 'primary-btn']"
                              :disabled="submission.fullyGraded"
                              @click="!submission.fullyGraded && handleGrading(task, submission)"
                            >
                              {{ submission.fullyGraded ? '已批改' : '批改' }}
                            </button>

                            <span
                              v-if="getGradingStatus(task, submission).show"
                              class="grading-status"
                            >
                              {{ getGradingStatus(task, submission).text }}
                            </span>
                          </div>
                        </td>
                      </tr>
                      <tr v-if="!task.submissions || !task.submissions.length">
                        <td colspan="4" style="text-align: center; color: gray;">暂无提交记录</td>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>

      <div v-else>
        <p>暂无任务</p>
      </div>
    </div>

    <!-- 编辑任务弹窗 -->
    <div class="modal" v-if="editModalVisible">
      <div class="modal-content">
        <h3>编辑任务</h3>
        <div class="form-group">
          <label>任务名称</label>
          <input type="text" v-model="editTask.title" />
        </div>
        <div class="form-group">
          <label>任务类型</label>
          <select v-model="editTask.type">
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
          <input type="datetime-local" v-model="editTask.deadline" />
        </div>
        <div class="form-group">
          <label>任务描述</label>
          <textarea v-model="editTask.description"></textarea>
        </div>
        <div class="modal-actions">
          <div class="modal-actions">
  <button class="add-btn" @click="saveEditedTask">保存</button>
  <button class="danger-btn" @click="editModalVisible = false">取消</button>
</div>

        </div>
      </div>
    </div>
  </div>
</template>
<script>
import api from '@/axios'
import axios from 'axios'

export default {
  name: 'TeacherTaskManagement',
  data() {
    return {
      courses: [],
      editModalVisible: false,
      editTask: {},
      editingCourseId: null,
      editingTaskId: null
    }
  },
  methods: {
    async toggleSubmissions(task) {
  const allowedTypes = ['CHAPTER_HOMEWORK', 'REPORT_SUBMISSION', 'EXAM_QUIZ'];

  // 双重保险：如果按钮已隐藏，可以不写这个判断，但建议保留防止异常调用
  if (!allowedTypes.includes(task.type)) {
    alert('该任务类型不支持查看提交记录');
    return;
  }

  task.showSubmissions = !task.showSubmissions;

  if (task.showSubmissions && !task.submissions) {
    try {
      const res = await api.get(`/submissions/get_submissions_of_task/${task.taskId}`);

      const submissions = res.map(sub => ({
        ...sub,
        // 更详细的批改状态判断
        objectiveGraded: sub.objectiveGraded || false, // 客观题是否已批改
        subjectiveGraded: sub.subjectiveGraded || false, // 主观题是否已批改
        fullyGraded: sub.status === 'GRADED' && typeof sub.finalGrade === 'number' && !isNaN(sub.finalGrade)
      }));

      this.$set(task, 'submissions', submissions);
    } catch (err) {
      console.error('获取提交列表失败', err);
      alert('加载提交记录失败');
      this.$set(task, 'submissions', []);
    }
  }
},
viewSubmission(submission) {
  const fileId = Array.isArray(submission.files) ? submission.files[0] : submission.files;

  if (!fileId) {
    alert('未上传文件');
    return;
  }

  // 判断是否是路径型的文件（学生上传）
  if (fileId.startsWith('/')) {
    const fullUrl = `http://localhost:8082/api/files${fileId}`;
    window.open(fullUrl, '_blank'); // 新窗口打开预览
    return;
  }

  // 否则是资源 ID（老师资源）
  this.$router.push({
    name: 'ResourcePreview',
    params: { resourceId: fileId }
  });
},


downloadSubmission(submission) {
  const fileId = Array.isArray(submission.files) ? submission.files[0] : submission.files;
  if (!fileId) {
    alert('未上传文件');
    return;
  }

  // 判断是否是路径
  if (fileId.startsWith('/')) {
    const url = `http://localhost:8082/api/files${fileId}`;
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '学生提交文件');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    return;
  }

  // 否则走老师资源方式
  const resource = {
    resourceId: fileId,
    name: '学生提交文件',
    type: 'DOCUMENT'
  };
  this.downloadResource(resource);
},
async handleGrading(task, submission) {
  if (task.type === 'EXAM_QUIZ') {
    // 获取该学生提交的手动批改题
    try {
      const res = await api.get(`/grading/manual-questions/${submission.submissionId}`);
      const manualQuestions = res;

      if (!manualQuestions || manualQuestions.length === 0) {
        // 没有主观题，自动批改
        if (confirm('无主观题，是否自动批改客观题？')) {
          await api.post(`/grading/auto/${submission.submissionId}`);
          alert('自动批改完成');
          // 更新状态：客观题已批改，且无主观题时表示完全批改
          submission.objectiveGraded = true;
          submission.subjectiveGraded = true; // 无主观题时设为true
          submission.fullyGraded = true;
        }
      } else {
        // 有主观题，跳转手动批改页面
        this.$router.push({
          name: 'ManualGrading',
          params: { submissionId: submission.submissionId }
        });
      }
    } catch (err) {
      console.error('获取手动批改题失败', err);
      alert('批改初始化失败');
    }
  } else {
    // 章节作业/报告上传
    // 确保任务有maxScore属性
    if (typeof task.maxScore === 'undefined') {
      alert('无法获取任务总分信息');
      return;
    }

    const maxScore = task.maxScore;
    let validScore = false;
    let scoreNum = 0;

    // 使用do-while循环替代while(true)
    do {
      const scoreInput = prompt(`请输入该学生得分（0-${maxScore}分）:`);
      
      // 用户取消输入
      if (scoreInput === null) return;
      
      // 验证输入
      scoreNum = parseFloat(scoreInput);
      
      if (isNaN(scoreNum)) {
        alert('请输入有效的数字');
        continue;
      }
      
      if (scoreNum < 0 || scoreNum > maxScore) {
        alert(`分数必须在0-${maxScore}之间`);
        continue;
      }
      
      validScore = true;
    } while (!validScore);

    try {
      await api.put(`/grading/grade-works`, null, {
        params: {
          submission_id: submission.submissionId,
          grade: scoreNum,
          feedback: '老师手动批改'
        }
      });
      alert('批改成功');
      // 更新当前 submission 状态为已批改
      submission.fullyGraded = true;
    } catch (err) {
      console.error('批改失败', err);
      alert('批改失败');
    }
  }
},
mapTaskTypeToChinese(type) {
  const map = {
    CHAPTER_HOMEWORK: '章节作业',
    REPORT_SUBMISSION: '报告上传',
    EXAM_QUIZ: '试卷答题',
    VIDEO_WATCHING: '视频观看',
    PPT_VIEW: 'PPT浏览',
    MATERIAL_READING: '阅读材料'
  };
  return map[type] || type;
},
    async fetchCoursesAndTasks() {
  try {
    const res = await api.get('/teacher/courses');
    const courses = res.data || [];

    const courseTasksPromises = courses.map(async course => {
      const response = await api.get(`/teacher/courses/${course.courseId}/tasks`);
      const taskList = response?.data || [];

      const tasks = await Promise.all(taskList.map(async task => {
        let submissions = [];
        try {
          const subRes = await api.get(`/submissions/get_submissions_of_task/${task.taskId}`);
          submissions = (subRes || []).map(sub => ({
            ...sub,
            // 更详细的批改状态判断
            objectiveGraded: sub.objectiveGraded || false, // 客观题是否已批改
            subjectiveGraded: sub.subjectiveGraded || false, // 主观题是否已批改
            fullyGraded: sub.status === 'GRADED' && typeof sub.finalGrade === 'number' && !isNaN(sub.finalGrade)
          }));
        } catch (e) {
          console.warn(`获取任务 ${task.taskId} 的提交失败：`, e);
        }

        try {
          const detailRes = await api.get(`/teacher/tasks/${task.taskId}`);
          task.maxScore = detailRes.data.maxScore; // 添加maxScore字段
        } catch (e) {
          console.warn(`获取任务${task.taskId}详情失败:`, e);
          task.maxScore = 100; // 默认值
        }

        return {
          ...task,
          submissions,
          showSubmissions: false
        };
      }));

      return {
        ...course,
        tasks
      };
    });

    this.courses = await Promise.all(courseTasksPromises);
  } catch (err) {
    console.error('加载课程或任务失败', err);
    alert(err.response?.data?.message || '加载失败');
  }
},
    goToAddTask(courseId) {
      this.$router.push({ name: 'AddTask', params: { courseId } })
    },
    // 与ResourceManagement.vue完全一致的查看方法
    async viewTask(taskId) {
      try {
        const res = await api.get(`/teacher/tasks/${taskId}`);
        const task = res.data;

        // 如果是试卷任务，调用新的 paper 接口获取 paperId
        if (task.type === 'EXAM_QUIZ') {
          const paperRes = await api.get(`/paper/task/${taskId}`);
          const paper = paperRes?.data;

          if (!paper || !paper.paperId) {
            alert('未找到绑定的试卷，无法预览');
            return;
          }

          // 跳转到试卷预览页
          this.$router.push({
            name: 'PreviewExam',
            params: { id: taskId },
            query: { paperId: paper.paperId }
          });
          return;
        }

        // 其余类型任务的资源跳转逻辑
        if (!task.resources || task.resources.length === 0) {
          alert('该任务没有关联资源');
          return;
        }

        const resource = task.resources[0];
        this.viewResource(resource);
      } catch (err) {
        console.error('获取任务详情失败:', err);
        alert('加载任务资源失败');
      }
    },
    // 与ResourceManagement.vue完全一致的资源查看方法
    viewResource(resource) {
  
      // 详细的类型判断过程
      const isVideoType1 = resource.type === 'VIDEO';
      const isVideoType2 = resource.type === 'video';
      const isVideoType3 = resource.type?.toLowerCase().startsWith('video/');
      const isVideo = isVideoType1 || isVideoType2 || isVideoType3;
   
      // 增强的文档类型检测
      const isStandardDocType = ['PDF', 'DOCUMENT', 'PPT', 'IMAGE'].includes(resource.type);
      const isPdfMime = resource.type?.toLowerCase().startsWith('application/pdf');
      const isImageMime = resource.type?.toLowerCase().startsWith('image/');
      const isWordMime = resource.type?.toLowerCase().includes('wordprocessingml.document'); // Word文档MIME类型
      const isPptMime = resource.type?.toLowerCase().includes('presentationml.presentation'); // PPT文档MIME类型
      const isExcelMime = resource.type?.toLowerCase().includes('spreadsheetml.sheet'); // Excel文档MIME类型
      const isOfficeDoc = isWordMime || isPptMime || isExcelMime;
      const isPreviewable = isStandardDocType || isPdfMime || isImageMime || isOfficeDoc;
      
      console.log('  📄 在预设类型列表中:', isStandardDocType, ['PDF', 'DOCUMENT', 'PPT', 'IMAGE']);
      console.log('  📄 以"application/pdf"开头:', isPdfMime);
      console.log('  🖼️ 以"image/"开头:', isImageMime);
      console.log('  📝 Word文档MIME类型:', isWordMime);
      console.log('  📊 PPT文档MIME类型:', isPptMime);
      console.log('  📗 Excel文档MIME类型:', isExcelMime);
      console.log('  🏢 Office文档类型:', isOfficeDoc);
      console.log('  📄 综合判断可预览:', isPreviewable);
      
      // 显示可能的MIME类型映射
      if (resource.type) {
        const mimeTypeMapping = this.getMimeTypeInfo(resource.type);
        console.log('  🔄 MIME类型映射信息:', mimeTypeMapping);
      }
      
      let routeAction = '';
      
      // 判断是否为视频类型 - 支持多种视频格式标识
      if (isVideo) {
        routeAction = '跳转到视频播放器';
        console.log('🎬 动作:', routeAction);
        console.log('🛤️ 路由:', 'VideoPlayer');
        console.log('📡 预期后端处理: 视频流处理');
        console.groupEnd();
        this.$router.push({ name: 'VideoPlayer', params: { resourceId: resource.resourceId } });
      } else if (isPreviewable) {
        routeAction = '跳转到资源预览';
        console.log('📖 动作:', routeAction);
        console.log('🛤️ 路由:', 'ResourcePreview');
        
        // 预期的后端处理逻辑
        if (isWordMime || isPptMime || isExcelMime) {
          console.log('📡 预期后端处理: Office文档 -> PDF转换预览');
          console.log('🔧 MIME类型将被PreviewService正确识别并转换');
        } else if (isPdfMime || resource.type === 'PDF') {
          console.log('📡 预期后端处理: PDF直接预览');
        } else if (isImageMime || resource.type === 'IMAGE') {
          console.log('📡 预期后端处理: 图片直接预览');
        } else {
          console.log('📡 预期后端处理: 标准资源类型预览');
        }
        
        console.groupEnd();
        this.$router.push({ name: 'ResourcePreview', params: { resourceId: resource.resourceId } });
      } else {
        routeAction = '显示不支持预览提示';
        console.log('❌ 动作:', routeAction);
        console.log('⚠️ 原因: 资源类型不在支持列表中');
        console.log('💡 建议: 检查resource.type是否为有效的MIME类型或标准类型');
        console.groupEnd();
        alert(`暂不支持预览该资源类型: ${resource.type}`);
      }
    },
    // 与ResourceManagement.vue完全一致的下载方法
    async downloadTask(taskId) {
      try {
        const res = await api.get(`/teacher/tasks/${taskId}`);
        const task = res.data;
        if (!task.resources || task.resources.length === 0) {
          alert('该任务没有关联资源');
          return;
        }
        const resource = task.resources[0];
        this.downloadResource(resource);
      } catch (err) {
        console.error('下载失败:', err);
        alert('任务资源下载失败');
      }
    },
    // 与ResourceManagement.vue完全一致的资源下载方法
    async downloadResource(resource) {
      try {
        const token = localStorage.getItem('token')
        const fullUrl = `http://localhost:8082/api/teacher/resources/${resource.resourceId}/download`
        
        const response = await axios.get(fullUrl, { 
          responseType: 'blob',
          timeout: 30000,
          headers: {
            'Authorization': token ? `Bearer ${token}` : undefined
          }
        })

        if (!response || !response.data) {
          throw new Error('服务器响应异常，未获取到文件数据')
        }
        
        if (response.status && response.status !== 200) {
          throw new Error(`下载失败，状态码: ${response.status}`)
        }

        if (response.data.size === 0) {
          throw new Error('下载的文件大小为0，可能文件不存在或已损坏')
        }

        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        
        let fileName = `resource_${resource.resourceId}`
        
        const contentDisposition = response.headers && response.headers['content-disposition']
        if (contentDisposition) {
          const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/)
          if (utf8Match) {
            fileName = decodeURIComponent(utf8Match[1])
          } else {
            const normalMatch = contentDisposition.match(/filename="?([^";\n]+)"?/)
            if (normalMatch) {
              fileName = normalMatch[1]
            }
          }
        }
        
        if (!fileName || fileName === `resource_${resource.resourceId}`) {
          fileName = resource.name || `resource_${resource.resourceId}`
          
          if (!fileName.includes('.')) {
            let extension = 'pdf'
            
            switch (resource.type?.toUpperCase()) {
              case 'VIDEO':
                extension = 'mp4'
                break
              case 'DOCUMENT':
                extension = 'doc'
                break
              case 'PDF':
                extension = 'pdf'
                break
              case 'PPT':
                extension = 'ppt'
                break
              case 'IMAGE':
                extension = 'jpg'
                break
              default:
                if (resource.url) {
                  const urlExt = resource.url.split('.').pop()
                  if (urlExt && urlExt.length <= 4) {
                    extension = urlExt
                  }
                }
            }
            fileName = `${fileName}.${extension}`
          }
        }
        
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        window.URL.revokeObjectURL(url)
        
      } catch (err) {
        console.error('下载失败:', err)
        
        let errorMessage = '下载失败'
        
        if (err.response) {
          const status = err.response.status
          
          switch (status) {
            case 404:
              errorMessage = '资源文件不存在'
              break
            case 410:
              errorMessage = '资源文件已被删除'
              break
            case 403:
              errorMessage = '没有权限下载此资源'
              break
            case 401:
              errorMessage = '登录已过期，请重新登录'
              break
            case 500:
              errorMessage = '服务器内部错误'
              break
            default:
              errorMessage = `下载失败 (错误码: ${status})`
          }
        } else if (err.code === 'ECONNABORTED') {
          errorMessage = '下载超时，请重试'
        } else if (err.message) {
          errorMessage = `下载失败: ${err.message}`
        }
        
        alert(errorMessage)
        
        if (err.response && err.response.status === 401) {
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        }
      }
    },
    openEditModal(courseId, taskId) {
      this.editingCourseId = courseId;
      this.editingTaskId = taskId;
      const course = this.courses.find(c => c.courseId === courseId);
      const task = course?.tasks.find(t => t.taskId === taskId);
      this.editTask = { ...task };
      this.editModalVisible = true;
    },
    async saveEditedTask() {
      try {
        await api.put(`/teacher/tasks/${this.editingTaskId}`, this.editTask);
        alert('任务更新成功');
        this.editModalVisible = false;
        this.fetchCoursesAndTasks();
      } catch (err) {
        console.error('任务更新失败', err);
        alert('任务更新失败');
      }
    },
    async deleteTask(courseId, taskId) {
      if (!confirm('确定要删除这个任务吗？')) return;
      try {
        await api.delete(`/teacher/tasks/${taskId}`);
        alert('任务删除成功');
        this.fetchCoursesAndTasks();
      } catch (err) {
        console.error('任务删除失败', err);
        alert(err.response?.data?.message || '删除失败');
      }
    },
    
    // 新增：与ResourceManagement.vue一致的辅助方法
    formatSize(bytes) {
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
      return (bytes / 1048576).toFixed(1) + ' MB';
    },
    
    getFileExtension(fileName) {
      if (!fileName) return '';
      const lastDot = fileName.lastIndexOf('.');
      return lastDot !== -1 ? fileName.slice(lastDot + 1).toLowerCase() : '';
    },
    
    getMimeTypeInfo(type) {
      const info = {
        originalType: type,
        isStandardType: ['PDF', 'PPT', 'VIDEO', 'DOCUMENT', 'IMAGE'].includes(type),
        isMimeType: type && type.includes('/'),
        category: 'unknown',
        expectedBackendMapping: 'unknown'
      };
      
      if (type) {
        const lowerType = type.toLowerCase();
        
        // 视频类型
        if (lowerType.startsWith('video/') || lowerType === 'video') {
          info.category = 'video';
          info.expectedBackendMapping = 'ResourceType.VIDEO';
        }
        // 图片类型
        else if (lowerType.startsWith('image/')) {
          info.category = 'image';
          info.expectedBackendMapping = 'ResourceType.IMAGE';
        }
        // PDF类型
        else if (lowerType.startsWith('application/pdf')) {
          info.category = 'pdf';
          info.expectedBackendMapping = 'ResourceType.PDF';
        }
        // Word文档
        else if (lowerType.includes('wordprocessingml.document')) {
          info.category = 'word-document';
          info.expectedBackendMapping = 'ResourceType.DOCUMENT (通过mimeTypeToResourceType映射)';
        }
        // PowerPoint
        else if (lowerType.includes('presentationml.presentation')) {
          info.category = 'powerpoint';
          info.expectedBackendMapping = 'ResourceType.PPT (通过mimeTypeToResourceType映射)';
        }
        // Excel
        else if (lowerType.includes('spreadsheetml.sheet')) {
          info.category = 'excel';
          info.expectedBackendMapping = 'ResourceType.DOCUMENT (通过mimeTypeToResourceType映射)';
        }
        // 标准类型
        else if (['PDF', 'PPT', 'VIDEO', 'DOCUMENT', 'IMAGE'].includes(type)) {
          info.category = 'standard-type';
          info.expectedBackendMapping = `ResourceType.${type}`;
        }
      }
      
      return info;
    },

    // 获取批改状态显示信息
    getGradingStatus(task, submission) {
      // 只有需要批改的任务类型才显示状态
      const needsGrading = ['CHAPTER_HOMEWORK', 'REPORT_SUBMISSION', 'EXAM_QUIZ'].includes(task.type);
      
      if (!needsGrading) {
        return { show: false };
      }

      if (task.type === 'EXAM_QUIZ') {
        // 试卷任务的状态判断
        if (submission.fullyGraded) {
          return {
            show: true,
            text: '批改完成',
            style: 'color: green; margin-left: 8px;'
          };
        } else if (submission.objectiveGraded && !submission.subjectiveGraded) {
          return {
            show: true,
            text: '客观题已自动批改',
            style: 'color: orange; margin-left: 8px;'
          };
        } else {
          return { show: false };
        }
      } else {
        // 章节作业和报告任务的状态判断
        if (submission.fullyGraded) {
          return {
            show: true,
            text: '批改完成',
            style: 'color: green; margin-left: 8px;'
          };
        } else {
          return { show: false };
        }
      }
    },

    handleWindowFocus() {
      // 当窗口重新获得焦点时，重新加载数据以更新批改状态
      // 这样可以捕获从手动批改页面返回的情况
      if (this.$route.name === 'TeacherTaskManagement') {
        this.fetchCoursesAndTasks();
      }
    }
  },
  mounted() {
    this.fetchCoursesAndTasks();
    
    // 监听从手动批改页面返回
    window.addEventListener('focus', this.handleWindowFocus);
  },
  beforeUnmount() {
    window.removeEventListener('focus', this.handleWindowFocus);
  }
}
</script>

<style scoped>

.teacher-tasks {
  padding: 30px;
  background-color: white;
}

h2 {
  text-align: left;
  font-size: 26px;
  color: #333;
  margin-bottom: 30px;
}

.add-task {
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  cursor: pointer;
  margin-bottom: 20px;
}

.add-task:hover {
  background: #357abd;
}

.course-card {
  background: #ffffff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%; 
  margin-bottom: 20px;
  transition: transform 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);
}

.task-table {
  width: 100%; 
  border-collapse: collapse;
}

.task-table th,
.task-table td {
  padding: 12px;
  text-align: left;
  border: 1px solid #ddd;
}

.task-table th {
  background-color: #f4f6f9;
  text-align: center;
}

.operation-cell {
  display: flex;
  justify-content: space-between;
}

.btn-group {
  display: flex;
  gap: 10px;
}

.outline-btn,
.primary-btn,
.danger-btn {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  text-align: center;
  border: none;
}

.outline-btn {
  border: 1px solid #3498db;
  background: transparent;
  color: #3498db;
}

.primary-btn {
  background-color: #3498db;
  color: white;
}

.danger-btn {
  background-color: #e74c3c;
  color: white;
}

.outline-btn:hover,
.primary-btn:hover,
.danger-btn:hover {
  opacity: 0.8;
}

.small-btn {
  padding: 6px 12px;
  font-size: 14px;
}

.disabled-btn {
  background-color: #f0f0f0;
  color: #ccc;
  cursor: not-allowed;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  font-weight: bold;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-group textarea {
  height: 80px;
}

.task-table.nested {
  margin-top: 10px;
  background-color: #fafafa;
}

.task-table.nested th,
.task-table.nested td {
  border-color: #ddd;
}
/* Modify the small-btn and disabled-btn button styles to align text properly */
.small-btn, .disabled-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px; /* Ensure all buttons have consistent padding */
  font-size: 14px;
  height: 36px; /* Set the same height for all buttons to align them */
  text-align: center;
}

/* Adjust the alignment for the disabled button (批改完成) */
.disabled-btn {
  background-color: #f0f0f0;
  color: #ccc;
  cursor: not-allowed;
  line-height: 36px; /* Vertically align the text */
}

/* Same for small buttons */
.outline-btn.small-btn, .primary-btn.small-btn {
  line-height: 36px;
}

.grading-status {
  line-height: 36px; /* Align text of grading status */
  margin-left: 8px; /* Add some space to the left */
}
/* 为所有按钮设置统一的高度和内边距 */
.outline-btn, .primary-btn, .danger-btn, .small-btn, .disabled-btn {
  padding: 8px 16px;  /* 设置统一的内边距 */
  font-size: 14px;  /* 统一字体大小 */
  height: 36px;  /* 统一设置高度 */
  display: inline-flex;
  align-items: center;  /* 确保文字在按钮内垂直居中 */
  justify-content: center;  /* 文字水平居中 */
  text-align: center;
}

/* 设置禁用按钮样式 */
.disabled-btn {
  background-color: #f0f0f0;
  color: #ccc;
  cursor: not-allowed;
  line-height: 36px; /* 确保禁用按钮的文本垂直居中 */
}

/* 调整已批改（灰色）按钮，确保与其他按钮相同 */
.outline-btn.disabled-btn, .disabled-btn {
  background-color: #f0f0f0;  /* 灰色背景 */
  color: #ccc;  /* 灰色文字 */
  cursor: not-allowed;  /* 禁止点击 */
  line-height: 36px;  /* 保证文本垂直居中 */
}

/* 对其他按钮类型进行对齐处理 */
.outline-btn:hover, .primary-btn:hover, .danger-btn:hover, .small-btn:hover {
  opacity: 0.8;
}
.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-actions button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
 
  text-align: center;
  transition: background 0.3s ease, transform 0.2s ease;
}

.modal-actions button:hover {
  opacity: 0.8;
}

/* Save Button - Blue */
.modal-actions .add-btn {
  background-color: #3498db; /* Blue */
  color: white;
  border: none;
}

.modal-actions .add-btn:hover {
  background-color: #2980b9;
}

/* Cancel Button - Red */
.modal-actions .danger-btn {
  background-color: #e74c3c; /* Red */
  color: white;
  border: none;
}

.modal-actions .danger-btn:hover {
  background-color: #c0392b;
}
/* 左上角圆形装饰 */
.teacher-tasks {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.teacher-tasks::before {
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
.teacher-tasks::after {
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