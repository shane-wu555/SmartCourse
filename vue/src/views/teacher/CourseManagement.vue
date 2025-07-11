<template>
  <div class="teacher-courses">
    <div class="decor-lower-left"></div>
<div class="decor-lower-right"></div>
    <h2>我的课程</h2>

    <button class="add-btn" @click="showAddForm = true">添加课程</button>

    <div class="course-list">
      <div class="course-card" v-for="course in paginatedCourses" :key="course.courseId">
        <h3>{{ course.name }}</h3>
        <p class="course-code">课程代码: {{ course.courseCode }}</p>
        <p class="semester">学期: {{ course.semester }}</p>
        <p v-if="course.credit !== undefined" class="credit">学分: {{ course.credit }}</p>
        <p v-if="course.hours !== undefined" class="hours">学时: {{ course.hours }}</p>
        <div class="course-actions">
          <button class="danger-btn" @click="deleteCourse(course.courseId)">删除课程</button>
          <button class="warning-btn" @click="editCourse(course)">编辑课程</button>
          <button class="primary-btn">
            <router-link
              :to="{ name: 'CourseDetail', params: { id: course.courseId } }"
              style="color: white; text-decoration: none;"
            >课程详情</router-link>
          </button>
          <button class="info-btn" @click="goToResourceManagement(course.courseId)">资源管理</button>
          <button class="info-btn" @click="goToKnowledgePointManagement(course.courseId)">知识点管理</button>
        </div>
      </div>
    </div>

    <!-- 分页控制 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1" class="page">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="page >= totalPages" class="page">下一页</button>
    </div>

    <!-- 添加课程弹窗 -->
    <div v-if="showAddForm" class="modal-overlay">
      <div class="modal">
        <h3>添加新课程</h3>

        <label>课程名称</label>
        <input v-model="newCourse.name" placeholder="请输入课程名称" />

        <label>课程代码</label>
        <input v-model="newCourse.courseCode" placeholder="请输入课程代码" />

        <label>学期</label>
        <input v-model="newCourse.semester" placeholder="例如：2025春季" />

        <label>学分</label>
        <input v-model.number="newCourse.credit" placeholder="请输入学分" type="number" />

        <label>学时</label>
        <input v-model.number="newCourse.hours" placeholder="请输入学时" type="number" />

        <label>课程描述</label>
        <textarea v-model="newCourse.description" placeholder="请输入课程描述"></textarea>

        <div class="modal-actions">
          <button class="add-btn" @click="createCourse">保存</button>
          <button class="danger-btn cancel-btn" @click="showAddForm = false">取消</button> <!-- 修改了此按钮的样式 -->
        </div>
      </div>
    </div>

    <!-- 编辑课程弹窗 -->
    <div v-if="showEditForm" class="modal-overlay">
      <div class="modal">
        <h3>编辑课程</h3>

        <label>课程名称</label>
        <input v-model="editCourseData.name" placeholder="请输入课程名称" />

        <label>课程代码</label>
        <input v-model="editCourseData.courseCode" placeholder="请输入课程代码" />

        <label>学期</label>
        <input v-model="editCourseData.semester" placeholder="例如：2025春季" />

        <label>学分</label>
        <input v-model.number="editCourseData.credit" placeholder="请输入学分" type="number" />

        <label>学时</label>
        <input v-model.number="editCourseData.hours" placeholder="请输入学时" type="number" />

        <label>课程描述</label>
        <textarea v-model="editCourseData.description" placeholder="请输入课程描述"></textarea>

        <div class="modal-actions">
          <button class="add-btn" @click="updateCourse">保存</button>
          <button class="danger-btn cancel-btn" @click="showEditForm = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '@/axios'

export default {
  name: 'TeacherCourseManagement',
  data() {
    return {
      courses: [],
      page: 1,
      size: 6,
      showAddForm: false,
      showEditForm: false,
      newCourse: {
        name: '', courseCode: '', semester: '', credit: null, hours: null, description: ''
      },
      editCourseData: {
        name: '', courseCode: '', semester: '', credit: null, hours: null, description: ''
      }
    }
  },
  computed: {
    paginatedCourses() {
      const start = (this.page - 1) * this.size;
      const end = this.page * this.size;
      return this.courses.slice(start, end);
    },
    totalPages() {
      return Math.ceil(this.courses.length / this.size) || 1;
    }
  },
  methods: {
    async fetchCourses() {
      try {
        const res = await axios.get('/teacher/courses')
        this.courses = res.data || []
      } catch (err) {
        console.error('获取课程失败', err)
        alert(err.response?.data?.message || '加载课程失败')
      }
    },
    async createCourse() {
      try {
        await axios.post('/teacher/courses', this.newCourse)
        alert('课程添加成功')
        this.showAddForm = false
        this.resetForm()
        this.fetchCourses()
      } catch (err) {
        console.error('添加课程失败', err)
        alert(err.response?.data?.message || '添加失败')
      }
    },
    async deleteCourse(courseId) {
      if (!confirm('确定要删除这门课程吗？')) return
      try {
        await axios.delete(`/teacher/courses/${courseId}`)
        alert('删除成功')
        this.fetchCourses()
      } catch (err) {
        console.error('删除失败', err)
        alert(err.response?.data?.message || '删除失败')
      }
    },
    editCourse(course) {
      this.editCourseData = { ...course }
      this.showEditForm = true
    },
    async updateCourse() {
      try {
        await axios.put(`/teacher/courses/${this.editCourseData.courseId}`, this.editCourseData)
        alert('课程更新成功')
        this.showEditForm = false
        this.fetchCourses()
      } catch (err) {
        console.error('更新课程失败', err)
        alert(err.response?.data?.message || '更新失败')
      }
    },
    goToResourceManagement(courseId) {
      this.$router.push({ name: 'ResourceManagement', params: { courseId } })
    },
    goToKnowledgePointManagement(courseId) {
      this.$router.push({ name: 'KnowledgePointManagement', params: { courseId } })
    },
    resetForm() {
      this.newCourse = {
        name: '', courseCode: '', semester: '', credit: null, hours: null, description: ''
      }
    },
    nextPage() {
      if (this.page < this.totalPages) this.page++
    },
    prevPage() {
      if (this.page > 1) this.page--
    }
  },
  mounted() {
    this.fetchCourses()
  }
}
</script>

<style scoped>
.teacher-courses {
  padding: 30px;
  background-color: white;
}

h2 {
  text-align: left;
  font-size: 26px;
  color: #333;
  margin-bottom: 30px;
}

.add-btn {
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  cursor: pointer;
  margin-bottom: 20px;
}

.add-btn:hover {
  background: #357abd;
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

.course-code, .semester, .credit, .hours {
  font-size: 14px;
  color: #555;
  margin-bottom: 10px;
}

.course-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.danger-btn, .primary-btn, .info-btn, .warning-btn {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  border: none;
}

.danger-btn {
  background: #e74c3c;
  color: white;
}

.danger-btn:hover {
  background: #c0392b;
}

.primary-btn {
  background: #3498db;
  color: white;
}

.primary-btn:hover {
  background: #2980b9;
}

.info-btn {
  background: #1abc9c;
  color: white;
}

.info-btn:hover {
  background: #16a085;
}

.warning-btn {
  background: #f39c12;
  color: white;
}

.warning-btn:hover {
  background: #e67e22;
}

/* Modal Styles */
.modal-overlay {
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

.modal {
  background: #fff;
  padding: 30px;
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.modal input, .modal textarea {
  width: 100%;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.modal label {
  font-weight: bold;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-actions button {
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

/* Pagination Styles */
.pagination {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
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

/* 修改取消按钮的样式使其与保存按钮大小一致 */
.cancel-btn {
  border-radius: 4px;
  padding: 10px 20px;

  margin-bottom: 20px;
  
}
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
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page {
  padding: 8px 16px;
  border-radius: 8px; /* Increase border radius */
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
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
/* 左上角圆形装饰 */
.teacher-courses {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.teacher-courses::before {
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
.teacher-courses::after {
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
