<template>
  <div class="student-task-wrapper">
    <div class="decor-lower-left"></div>
    <div class="decor-lower-right"></div>

    <h2>我的课程任务</h2>

    <div v-for="course in enrolledCourses" :key="course.courseId" class="course-card">
      <div class="course-header">
        <h3>{{ course.name }} ({{ course.courseCode }})</h3>
        <p>学期: {{ course.semester }}</p>
      </div>

      <!-- 课程任务 -->
      <div class="tasks-section" v-if="course.tasks?.length">
        <h4>课程任务</h4>
        <table class="task-table">
          <thead>
            <tr>
              <th>任务名称</th>
              <th>类型</th>
              <th>截止时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in course.tasks" :key="task.taskId">
              <td>{{ task.title }}</td>
              <td>{{ formatTaskType(task.type) }}</td>
              <td>{{ formatDate(task.deadline) }}</td>
              <td :class="getTaskStatusClass(task)">{{ getTaskStatus(task) }}</td>
              <td>
                <button class="btn outline" @click="viewTask(task)">查看</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="empty-state">
        暂无任务
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/axios'
export default {
  name: 'StudentTask',
  data() {
    return { enrolledCourses: [] }
  },
  methods: {
    viewTask(task) {
      if (this.getTaskStatus(task) === '已截止') {
        alert('任务已截止，无法查看');
        return;
      }
      this.$router.push({ name: 'TaskDetail', params: { id: task.taskId } });
    },
    async fetchEnrolledCoursesWithDetails() {
      try {
        const coursesRes = await api.get('/student/courses');
        const courses = coursesRes?.content || [];
        this.enrolledCourses = await Promise.all(
          courses.map(async course => {
            try {
              const tasksRes = await api.get(`/student/courses/${course.courseId}/tasks`, {
                params: { page: 1, size: 100 }
              });
              return { ...course, tasks: tasksRes.data || [] };
            } catch (error) {
              console.error(`加载任务失败:`, error);
              return { ...course, tasks: [] };
            }
          })
        );
      } catch (err) {
        console.error('加载课程失败:', err);
        alert('加载课程数据失败');
      }
    },
    formatTaskType(type) {
      const typeMap = {
        'CHAPTER_HOMEWORK': '章节作业',
        'VIDEO_WATCHING': '视频观看',
        'MATERIAL_READING': '阅读材料',
        'PPT_VIEW': 'PPT浏览',
        'EXAM_QUIZ': '试卷答题',
        'REPORT_SUBMISSION': '报告上传'
      };
      return typeMap[type] || type;
    },
    formatDate(dateStr) {
      if (!dateStr) return '无截止时间';
      return new Date(dateStr).toLocaleString('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      });
    },
    getTaskStatus(task) {
      if (!task.deadline) return '进行中';
      return new Date() > new Date(task.deadline) ? '已截止' : '进行中';
    },
    getTaskStatusClass(task) {
      return {
        'status-ongoing': this.getTaskStatus(task) === '进行中',
        'status-expired': this.getTaskStatus(task) === '已截止'
      };
    }
  },
  mounted() {
    this.fetchEnrolledCoursesWithDetails();
  }
}
</script>

<style scoped>
.student-task-wrapper {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: white;
}

.course-section {
  background: #fff;
  border-radius: 10px; /* 增加圆角 */
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  padding: 20px;
  margin-bottom: 30px;
}

.course-header {
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 30px;
}

h4 {
  font-size: 22px;
  color: #3498db;
  margin-bottom: 20px;
}

.task-table {
  width: 100%;
  border-collapse: collapse;
}

.task-table th,
.task-table td {
  padding: 12px;
  border: 1px solid #eee;
  text-align: left;
}

.status-ongoing {
  color: #4CAF50;
}

.status-expired {
  color: #F44336;
}

.empty-state {
  padding: 20px;
  text-align: center;
  color: #888;
}

.btn {
  padding: 8px 16px;
  border-radius: 8px; /* 增加圆角 */
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn.outline {
  background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
}

.btn.primary {
  background: #4a90e2;
  border: none;
  color: white;
}

.btn.outline:hover {
  background: rgba(74, 144, 226, 0.1);
}

.course-header p {
  color: #666;
  font-size: 14px;
}

/* Add hover effect for course card */
.course-card {
  background: #ffffff;
  padding: 20px;
  border-radius: 10px; /* 圆角 */
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  width: 100%;
  margin-bottom: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);  /* Move the card up */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);  /* Add shadow */
}

@media (max-width: 768px) {
  .task-table {
    display: block;
    overflow-x: auto;
  }
}
.student-task-wrapper {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.student-task-wrapper::before {
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
.student-task-wrapper::after {
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
.student-task-wrapper::part-lower-left {
  content: "";
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
.student-task-wrapper::part-lower-right {
  content: "";
  position: absolute;
  bottom: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.3), rgba(74, 144, 226, 0.4));
  clip-path: polygon(0 100%, 100% 100%, 100% 0);
  z-index: 0;
}
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
