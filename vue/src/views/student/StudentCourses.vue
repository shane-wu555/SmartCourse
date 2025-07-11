<template>
  <div class="student-course-wrapper">
    <div class="decor-lower-left"></div>
    <div class="decor-lower-right"></div>

    <h2>课程中心</h2>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input v-model="keyword" placeholder="按课程名称或编号搜索" class="search-input" />
      <button class="btn primary" @click="fetchAllCourses">搜索课程</button>
    </div>

    <!-- 全部课程 -->
    <h3>可选课程</h3>
    <div class="course-grid">
      <div v-for="course in allCourses" :key="course.courseId" class="course-card">
        <h4>{{ course.name }}</h4>
        <p class="course-info">编号: {{ course.courseCode }}</p>
        <p class="course-info">学期: {{ course.semester }}</p>
        <p class="course-info">学分: {{ course.credit }}，学时: {{ course.hours }}</p>
        <button class="btn primary" @click="enrollCourse(course.courseId)">选课</button>
      </div>
    </div>

    <!-- 分页控制 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1" class="page">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="page >= totalPages" class="page">下一页</button>
    </div>

    <!-- 我已选课程 -->
    <h3>我已选课程</h3>
    <div class="course-grid">
      <div v-for="course in myCourses" :key="course.courseId" class="course-card enrolled">
        <h4>{{ course.name }}</h4>
        <p class="course-info">编号: {{ course.courseCode }}</p>
        <p class="course-info">学期: {{ course.semester }}</p>
        <p class="course-info">学分: {{ course.credit }}，学时: {{ course.hours }}</p>
        <div class="button-group">
          <button class="btn danger" @click="dropCourse(course.courseId)">退课</button>
          <button class="btn primary" @click="goToCourseDetail(course.courseId)">课程详情</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/axios'

export default {
  name: 'StudentCourse',
  data() {
    return {
      allCourses: [],
      myCourses: [],
      keyword: '',
      semester: '',
      page: 1,
      size: 8,
      totalPages: 1
    }
  },
  methods: {
    async fetchAllCourses() {
      try {
        const res = await api.get('/student/courses/search', {
          params: {
            page: this.page,
            size: this.size,
            keyword: this.keyword
          }
        })
        this.allCourses = res?.content || []
        this.totalPages = res?.totalPages || 1
      } catch (err) {
        console.error('获取课程失败', err)
      }
    },

    async fetchMyCourses() {
      try {
        const res = await api.get('/student/courses', {
          params: { page: 1, size: 20 }
        })
        this.myCourses = res?.content || []
      } catch (err) {
        console.error('获取我已选课程失败', err)
      }
    },

    async enrollCourse(courseId) {
      try {
        await api.post(`/student/courses/${courseId}/enroll`)
        alert('选课成功')
        this.fetchMyCourses()
      } catch (err) {
        alert(err.response?.data?.message || '选课失败')
      }
    },

    async dropCourse(courseId) {
      if (!confirm('确认要退选这门课程吗？')) return
      try {
        await api.post(`/student/courses/${courseId}/drop`)
        alert('退课成功')
        this.fetchMyCourses()
      } catch (err) {
        alert(err.response?.data?.message || '退课失败')
      }
    },

    goToCourseDetail(courseId) {
      this.$router.push({
        name: 'CourseDetail',
        params: { id: courseId }
      })
    },

    nextPage() {
      if (this.page < this.totalPages) {
        this.page++
        this.fetchAllCourses()
      }
    },

    prevPage() {
      if (this.page > 1) {
        this.page--
        this.fetchAllCourses()
      }
    }
  },
  mounted() {
    this.fetchAllCourses()
    this.fetchMyCourses()
  }
}
</script>

<style scoped>
.page {
  padding: 8px 16px;
  border-radius: 8px; /* 增加圆角 */
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
   background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
}

.student-course-wrapper {
  padding: 30px;
  background-color: white;
}

h2 {
  text-align: left;
  font-size: 28px;
  color: #333;
  margin-bottom: 30px;
}

h3 {
  font-size: 22px;
  color: #3498db;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.search-input {
  padding: 8px 16px;
  font-size: 16px;
  border-radius: 4px;
  border: 1px solid #ddd;
  width: 100%;
  max-width: 400px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.course-card {
  background: #fff;
  border: 1px solid #ddd;
  padding: 20px;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);
}

.course-card.enrolled {
  border-left: 4px solid #4a90e2;
}

.course-info {
  font-size: 14px;
  color: #666;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.btn {
  padding: 8px 16px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s ease, transform 0.2s ease;
}

.btn.primary {
  background: #4a90e2;
  color: white;
}

.btn.danger {
  background: #e74c3c;
  color: white;
}

.btn:hover {
  background: #357abd;
  transform: translateY(-2px);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
}

.pagination-btn {
  padding: 8px 16px;
  border-radius: 4px;
  background-color: #3498db;
  color: white;
  border: none;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.pagination-btn:hover {
  background-color: #2980b9;
}

.pagination-btn:disabled {
  background-color: #d1d1d1;
  cursor: not-allowed;
}
.student-course-wrapper::before {
  content: "";
  position: absolute;
  top: -30px;
  left: -30px;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle at center, rgba(74, 144, 226, 0.2), transparent 70%);
  border-radius: 50%;
  z-index: 0;
}

.student-course-wrapper::after {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, rgba(52, 152, 219, 0.1), transparent);
  clip-path: polygon(100% 0, 0 0, 100% 100%);
  z-index: 0;
}

.student-course-wrapper {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.student-course-wrapper::before {
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
.student-course-wrapper::after {
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
