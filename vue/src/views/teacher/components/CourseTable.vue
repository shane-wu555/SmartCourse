<template>
  <div class="teacher-courses">
    <h2>我的课程</h2>

    <button class="add-btn" @click="openAddForm">添加课程</button>

    <!-- 使用 paginatedCourses 而非全部 -->
    <CourseTable
      :courses="paginatedCourses"
      @edit="openEditForm"
      @delete="deleteCourse"
    />

    <!-- 添加/编辑弹窗 -->
    <CourseForm
      :visible="showForm"
      :course="selectedCourse"
      @submit="handleFormSubmit"
      @cancel="closeForm"
    />

    <!-- 分页控制 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="page === 1" class="pagination-btn">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="page >= totalPages" class="pagination-btn">下一页</button>
    </div>
  </div>
</template>

<script>
import CourseTable from '@/teacher/components/CourseTable.vue'
import CourseForm from '@/teacher/components/CourseForm.vue'
import axios from '@/axios'

export default {
  name: 'TeacherCourseManagement',
  components: { CourseTable, CourseForm },
  data() {
    return {
      courses: [],
      page: 1,
      size: 6,
      showForm: false,
      selectedCourse: null
    }
  },
  computed: {
    paginatedCourses() {
      const start = (this.page - 1) * this.size
      const end = this.page * this.size
      return this.courses.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.courses.length / this.size) || 1
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
    async handleFormSubmit(formData) {
      try {
        if (this.selectedCourse && this.selectedCourse.courseId) {
          await axios.put(`/teacher/courses/${this.selectedCourse.courseId}`, formData)
          alert('课程更新成功')
        } else {
          await axios.post('/teacher/courses', formData)
          alert('课程添加成功')
        }
        this.closeForm()
        this.fetchCourses()
      } catch (err) {
        console.error('保存课程失败', err)
        alert(err.response?.data?.message || '保存失败')
      }
    },
    openAddForm() {
      this.selectedCourse = null
      this.showForm = true
    },
    openEditForm(course) {
      this.selectedCourse = course
      this.showForm = true
    },
    closeForm() {
      this.selectedCourse = null
      this.showForm = false
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
  padding: 20px;
}
.add-btn {
  background: #2ecc71;
  color: white;
  border: none;
  padding: 8px 14px;
  margin-bottom: 20px;
  border-radius: 4px;
  cursor: pointer;
}
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
}
.pagination-btn {
  padding: 6px 10px;
  background: #eee;
  border: 1px solid #ccc;
  cursor: pointer;
}
</style>
