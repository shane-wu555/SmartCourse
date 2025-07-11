<template>
  <div class="student-mgmt">
    <div class="decor-lower-left"></div>
<div class="decor-lower-right"></div>
    <h2>课程学生管理</h2>

    <div class="course-list">
      <div
        v-for="course in paginatedCourses"
        :key="course.courseId"
        class="course-card"
      >
        <h3>{{ course.name }}</h3>
        <p class="course-code">课程代码：{{ course.courseCode }}</p>
        <p class="semester">学期：{{ course.semester }}</p>

        <div class="action-bar">
          <input
            type="file"
            accept=".csv,.xlsx"
            :ref="`fileInput-${course.courseId}`"
            hidden
            @change="(e) => importStudents(e, course.courseId)"
          />
          <button class="btn import-btn" @click="triggerFile(course.courseId)">导入学生</button>
          <button class="btn view-btn" @click="goToStudentList(course.courseId)">查看学生</button>
        </div>
      </div>
    </div>

    <!-- 分页控制 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1" class="page">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="page >= totalPages" class="page">下一页</button>
    </div>
  </div>
</template>

<script>
import axios from '@/axios';

export default {
  name: 'StudentManagement',
  data() {
    return {
      courses: [],
      page: 1,
      size: 6
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.courses.length / this.size) || 1;
    },
    paginatedCourses() {
      const start = (this.page - 1) * this.size;
      return this.courses.slice(start, start + this.size);
    }
  },
  async mounted() {
    try {
      const res = await axios.get('/teacher/courses');
      this.courses = res.data || [];
    } catch (err) {
      console.error('获取课程失败', err);
      alert('加载课程失败');
    }
  },
  methods: {
    triggerFile(courseId) {
      const input = this.$refs[`fileInput-${courseId}`];
      if (input && input.click) {
        input.click();
      } else if (Array.isArray(input)) {
        input[0]?.click();
      } else {
        console.warn('input not found for', courseId, input);
      }
    },
    async importStudents(event, courseId) {
      const file = event.target.files[0];
      if (!file) return;

      const formData = new FormData();
      formData.append('file', file);

      try {
        await axios.post(`/teacher/courses/${courseId}/import-students`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        alert('导入成功');
      } catch (err) {
        console.error('导入失败', err);
        alert(err.response?.data?.message || '导入失败');
      }
    },
    goToStudentList(courseId) {
      this.$router.push({
        name: 'CourseStudents',
        params: { courseId }
      });
    },
    nextPage() {
      if (this.page < this.totalPages) {
        this.page++;
      }
    },
    prevPage() {
      if (this.page > 1) {
        this.page--;
      }
    }
  }
};
</script>

<style scoped>
.student-mgmt {
  padding: 30px;
  background-color: white;
}

h2 {
  text-align: left;
  font-size: 26px;
  color: #333;
  margin-bottom: 30px;
}

.course-list {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  justify-content: flex-start;
}

.course-card {
  background: #ffffff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 300px;
  transition: transform 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);
}

h3 {
  font-size: 18px;
  color: #3498db;
  margin-bottom: 10px;
}

.course-code,
.semester {
  font-size: 14px;
  color: #555;
  margin-bottom: 10px;
}

.action-bar {
  margin-top: 15px;
  display: flex;
  gap: 12px;
  justify-content: space-between;
}

button {
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
 
}

button:hover {
  background: #357abd;
  transform: translateY(-2px);
}

button:disabled {
 
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}



.pagination-btn:hover {
  background-color: #2980b9;
}

.pagination-btn:disabled {
  background-color: #d1d1d1;
  cursor: not-allowed;
}
.page {
  padding: 8px 16px;
  border-radius: 8px; /* Keep the rounded corners */
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s; 
  background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
}

.page:hover {
  background: transparent;  /* Remove the hover background change */
  transform: none;  /* Remove the hover transform effect */
}
/* 左上角圆形装饰 */
.student-mgmt {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.student-mgmt::before {
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
.student-mgmt::after {
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
